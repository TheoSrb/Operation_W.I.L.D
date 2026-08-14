package net.tiew.operationWild.entity.goals.red_panda;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class RedPandaIntimidationGoal extends Goal {

    private static final double PLAYER_RANGE = 9.0;
    private static final double PLAYER_LOUD_RANGE = 14.0;
    private static final double MONSTER_RANGE = 10.0;
    private static final double HUNTER_RANGE = 12.0;

    private static final double SCARE_RADIUS = 6.5;
    private static final int SCARE_TICKS = 110;
    private static final float STANDING_HEIGHT_FACTOR = 2.0f;

    private static final int PEAK_TICK = 22;
    private static final int COOLDOWN_TICKS = 260;

    private final RedPandaEntity panda;

    private LivingEntity threat;
    private int elapsed;
    private int cooldown;
    private boolean peaked;

    public RedPandaIntimidationGoal(RedPandaEntity panda) {
        this.panda = panda;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (!isAvailable()) return false;

        threat = findThreat();
        return threat != null;
    }

    @Override
    public boolean canContinueToUse() {
        return panda.isIntimidating() && panda.isAlive() && !panda.isVehicle() && !panda.isPassenger();
    }

    @Override
    public void start() {
        elapsed = 0;
        peaked = false;

        panda.getNavigation().stop();
        panda.setSitting(false);
        panda.setIntimidateTimer(RedPandaEntity.INTIMIDATE_TICKS);

        panda.level().playSound(null, panda.getX(), panda.getY(), panda.getZ(),
                SoundEvents.FOX_SNIFF, SoundSource.NEUTRAL, 0.7f,
                (float) OWUtils.generateRandomInterval(0.75, 0.9));
    }

    @Override
    public void stop() {
        panda.setIntimidateTimer(0);
        if (threat != null && threat.isAlive()) panda.raiseAlert(threat);
        threat = null;
        elapsed = 0;
        peaked = false;
        cooldown = COOLDOWN_TICKS + panda.getRandom().nextInt(COOLDOWN_TICKS / 2);
    }

    @Override
    public void tick() {
        elapsed++;

        panda.getNavigation().stop();
        if (panda.onGround()) {
            panda.setDeltaMovement(panda.getDeltaMovement().multiply(0.0, 1.0, 0.0));
        }

        if (threat != null && threat.isAlive()) {
            panda.getLookControl().setLookAt(threat, 30f, 30f);
        }

        if (peaked || elapsed < PEAK_TICK) return;
        peaked = true;
        scareNearby();
    }

    private void scareNearby() {
        float ceiling = panda.getBbHeight() * STANDING_HEIGHT_FACTOR;

        List<Mob> nearby = panda.level().getEntitiesOfClass(Mob.class,
                panda.getBoundingBox().inflate(SCARE_RADIUS),
                candidate -> candidate != panda
                        && candidate.isAlive()
                        && candidate instanceof Enemy
                        && candidate.getBbHeight() <= ceiling);

        for (Mob victim : nearby) {
            panda.scareAway(victim, SCARE_TICKS);
        }

        panda.level().playSound(null, panda.getX(), panda.getY(), panda.getZ(),
                SoundEvents.FOX_SCREECH, SoundSource.NEUTRAL, 1.0f,
                (float) OWUtils.generateRandomInterval(1.25, 1.45));

        if (!(panda.level() instanceof ServerLevel serverLevel)) return;
        serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                panda.getX(), panda.getY() + panda.getBbHeight() * 1.6, panda.getZ(),
                3, 0.25, 0.15, 0.25, 0.01);
    }

    private boolean isAvailable() {
        return !panda.isTame()
                && !panda.isBaby()
                && panda.isAlive()
                && panda.onGround()
                && !panda.isSitting()
                && !panda.isSleeping()
                && !panda.isNapping()
                && !panda.isInWater()
                && !panda.isPassenger()
                && !panda.isVehicle()
                && !panda.isTreeClimbing()
                && !panda.isClimbing()
                && !panda.isEatingEgg()
                && !panda.isAlerted();
    }

    private @Nullable LivingEntity findThreat() {
        Player player = panda.level().getNearestPlayer(panda, PLAYER_LOUD_RANGE);
        if (player != null && !player.isSpectator() && !player.isCreative() && !player.isCrouching()) {
            double distance = panda.distanceTo(player);
            boolean loud = player.isSprinting() || player.swinging;
            if ((distance <= PLAYER_RANGE || (loud && distance <= PLAYER_LOUD_RANGE))
                    && panda.hasLineOfSight(player)) {
                return player;
            }
        }

        List<Monster> monsters = panda.level().getEntitiesOfClass(Monster.class,
                panda.getBoundingBox().inflate(MONSTER_RANGE),
                monster -> monster.isAlive() && panda.hasLineOfSight(monster));
        if (!monsters.isEmpty()) return monsters.get(0);

        List<Mob> hunters = panda.level().getEntitiesOfClass(Mob.class,
                panda.getBoundingBox().inflate(HUNTER_RANGE),
                mob -> mob != panda && mob.isAlive() && mob.getTarget() == panda);
        return hunters.isEmpty() ? null : hunters.get(0);
    }
}
