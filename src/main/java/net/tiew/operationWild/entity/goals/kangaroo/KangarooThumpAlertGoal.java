package net.tiew.operationWild.entity.goals.kangaroo;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;
import net.tiew.operationWild.sound.OWSounds;

import java.util.EnumSet;
import java.util.List;

public class KangarooThumpAlertGoal extends Goal {

    public static final int THUMP_DURATION = 60;
    private static final int FIRST_SLAM_TICK = 5;
    private static final int SECOND_SLAM_TICK = 12;
    private static final int DRUM_START_TICK = 5;
    private static final int DRUM_END_TICK = 53;
    private static final int DRUM_INTERVAL_TICKS = 3;

    private static final double PLAYER_DETECTION_RANGE = 10.0;
    private static final double PLAYER_LOUD_RANGE = 16.0;
    private static final double MONSTER_DETECTION_RANGE = 12.0;
    private static final double PROPAGATION_RADIUS = 24.0;
    private static final int COOLDOWN_TICKS = 200;
    private static final int MIN_HERD_SIZE = 3;

    private final KangarooEntity kangaroo;

    private LivingEntity threat;
    private List<KangarooEntity> herd = List.of();
    private int thumpTicks;
    private int cooldown;
    private boolean firstSlamDone;
    private boolean secondSlamDone;

    public KangarooThumpAlertGoal(KangarooEntity kangaroo) {
        this.kangaroo = kangaroo;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (!isAvailable()) return false;

        herd = findHerd();
        if (herd.size() + 1 < MIN_HERD_SIZE) return false;
        if (herd.stream().anyMatch(other -> other.isThumping() || other.getHerdThumpCooldown() > 0)) return false;
        if (kangaroo.getHerdThumpCooldown() > 0) return false;

        threat = findThreat();
        return threat != null;
    }

    @Override
    public boolean canContinueToUse() {
        return thumpTicks < THUMP_DURATION && kangaroo.isAlive() && !kangaroo.isVehicle();
    }

    @Override
    public void start() {
        thumpTicks = 0;
        firstSlamDone = false;
        secondSlamDone = false;
        kangaroo.getNavigation().stop();
        kangaroo.setThumping(true);
    }

    @Override
    public void stop() {
        kangaroo.setThumping(false);
        thumpTicks = 0;
        threat = null;
        herd = List.of();
        cooldown = COOLDOWN_TICKS + kangaroo.getRandom().nextInt(COOLDOWN_TICKS / 2);
    }

    @Override
    public void tick() {
        thumpTicks++;

        kangaroo.getNavigation().stop();
        if (kangaroo.onGround()) {
            kangaroo.setDeltaMovement(kangaroo.getDeltaMovement().multiply(0.0, 1.0, 0.0));
        }

        if (threat != null && threat.isAlive()) {
            kangaroo.getLookControl().setLookAt(threat, 30.0f, 30.0f);
        }

        if (!firstSlamDone && thumpTicks >= FIRST_SLAM_TICK) {
            firstSlamDone = true;
            slam(1.0f);
            propagateAlert();
        }

        if (!secondSlamDone && thumpTicks >= SECOND_SLAM_TICK) {
            secondSlamDone = true;
            slam(0.85f);
        }

        if (thumpTicks >= DRUM_START_TICK && thumpTicks <= DRUM_END_TICK
                && thumpTicks != FIRST_SLAM_TICK && thumpTicks != SECOND_SLAM_TICK
                && (thumpTicks - DRUM_START_TICK) % DRUM_INTERVAL_TICKS == 0) {
            drumTap();
        }
    }

    private void drumTap() {
        kangaroo.level().playSound(null, kangaroo.getX(), kangaroo.getY(), kangaroo.getZ(),
                SoundEvents.ROOTED_DIRT_BREAK, SoundSource.NEUTRAL, 0.45f,
                0.5f + kangaroo.getRandom().nextFloat() * 0.1f);

        if (!(kangaroo.level() instanceof ServerLevel serverLevel)) return;

        BlockState ground = kangaroo.level().getBlockState(kangaroo.blockPosition().below());
        if (ground.isAir()) return;
        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                kangaroo.getX(), kangaroo.getY() + 0.05, kangaroo.getZ(),
                4, 0.35, 0.02, 0.35, 0.05);
    }

    private boolean isAvailable() {
        return !kangaroo.isTame()
                && !kangaroo.isBaby()
                && !kangaroo.isAngry()
                && !kangaroo.isVehicle()
                && !kangaroo.isSitting()
                && !kangaroo.isSleeping()
                && !kangaroo.isInWater()
                && kangaroo.onGround();
    }

    private LivingEntity findThreat() {
        Player player = kangaroo.level().getNearestPlayer(kangaroo, PLAYER_LOUD_RANGE);
        if (player != null && !player.isSpectator() && !player.isCreative()) {
            double distance = kangaroo.distanceTo(player);
            boolean loud = player.isSprinting() || player.swinging;
            if (!player.isCrouching() && (distance <= PLAYER_DETECTION_RANGE || (loud && distance <= PLAYER_LOUD_RANGE))
                    && kangaroo.hasLineOfSight(player)) {
                return player;
            }
        }

        List<Monster> monsters = kangaroo.level().getEntitiesOfClass(Monster.class,
                kangaroo.getBoundingBox().inflate(MONSTER_DETECTION_RANGE),
                monster -> monster.isAlive() && kangaroo.hasLineOfSight(monster));
        return monsters.isEmpty() ? null : monsters.get(0);
    }

    private void slam(float volume) {
        kangaroo.level().playSound(null, kangaroo.getX(), kangaroo.getY(), kangaroo.getZ(),
                OWSounds.MINI_EARTHQUAKE.get(), SoundSource.NEUTRAL, 1.1f * volume, 1.35f);
        kangaroo.level().playSound(null, kangaroo.getX(), kangaroo.getY(), kangaroo.getZ(),
                SoundEvents.ROOTED_DIRT_BREAK, SoundSource.NEUTRAL, 1.2f * volume, 0.55f);

        if (!(kangaroo.level() instanceof ServerLevel serverLevel)) return;

        BlockState ground = kangaroo.level().getBlockState(kangaroo.blockPosition().below());
        if (!ground.isAir()) {
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                    kangaroo.getX(), kangaroo.getY() + 0.1, kangaroo.getZ(),
                    26, 0.7, 0.05, 0.7, 0.18);
        }
        serverLevel.sendParticles(ParticleTypes.CLOUD,
                kangaroo.getX(), kangaroo.getY() + 0.05, kangaroo.getZ(),
                12, 0.8, 0.02, 0.8, 0.05);
    }

    private List<KangarooEntity> findHerd() {
        return kangaroo.level().getEntitiesOfClass(KangarooEntity.class,
                kangaroo.getBoundingBox().inflate(PROPAGATION_RADIUS),
                other -> other != kangaroo && other.isAlive() && !other.isTame() && !other.isVehicle());
    }

    private void propagateAlert() {
        if (threat == null) return;

        kangaroo.raiseAlert(threat);
        kangaroo.setHerdThumpCooldown(COOLDOWN_TICKS);

        for (KangarooEntity other : herd) {
            other.raiseAlert(threat);
            other.setHerdThumpCooldown(COOLDOWN_TICKS);
        }
    }
}
