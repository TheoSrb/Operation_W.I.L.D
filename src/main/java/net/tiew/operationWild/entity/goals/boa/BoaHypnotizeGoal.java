package net.tiew.operationWild.entity.goals.boa;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import org.joml.Vector3f;

import java.util.EnumSet;

public class BoaHypnotizeGoal extends Goal {

    private final BoaEntity boa;
    private final double range;
    private final int    durationTicks;
    private final int    cooldownTicks;
    private final float  turnSpeed;
    private final float  slowFactor;

    private LivingEntity target;
    private int timer;

    public BoaHypnotizeGoal(BoaEntity boa, double range, int durationTicks, int cooldownTicks,
                            float turnSpeed, float slowFactor) {
        this.boa = boa;
        this.range = range;
        this.durationTicks = durationTicks;
        this.cooldownTicks = cooldownTicks;
        this.turnSpeed = turnSpeed;
        this.slowFactor = slowFactor;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        if (boa.isVehicle()) return false;
        if (boa.isConstricting()) return false;
        if (boa.isSleeping() || boa.isNapping()) return false;
        if (boa.getHypnosisCooldown() > 0) return false;
        if (boa.getRandom().nextInt(START_CHANCE_DENOM) != 0) return false;
        return canHypnotize(boa.getTarget(), true);
    }

    private static final double MIN_RANGE = 5.0;
    private static final int START_CHANCE_DENOM = 300;

    private boolean canHypnotize(LivingEntity t, boolean isStarting) {
        if (t == null || !t.isAlive()) return false;
        if (t instanceof Player p && (p.isCreative() || p.isSpectator())) return false;
        if (boa.isAlliedTo(t)) return false;
        double d2 = boa.distanceToSqr(t);
        if (d2 > this.range * this.range) return false;
        if (isStarting) {
            if (d2 < MIN_RANGE * MIN_RANGE) return false;
        }
        if (!boa.getSensing().hasLineOfSight(t)) return false;
        boolean takenByLowerBoa = boa.level()
                .getEntitiesOfClass(BoaEntity.class, t.getBoundingBox().inflate(24.0))
                .stream()
                .anyMatch(o -> o != boa && o.getId() < boa.getId()
                        && o.isHypnotizing() && o.getHypnosisTargetId() == t.getId());
        return !takenByLowerBoa;
    }

    @Override
    public void start() {
        this.target = boa.getTarget();
        this.timer = this.durationTicks;
        boa.setHypnotizing(true);
        boa.setHypnosisTargetId(this.target != null ? this.target.getId() : -1);
        // Nausée appliquée UNE seule fois pour 10 s, tous les flags à false (comme la lenteur).
        if (this.target != null) {
            this.target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0, false, false, false));
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (boa.isVehicle() || boa.isConstricting()) return false;
        if (this.timer <= 0) return false;
        return canHypnotize(this.target, false);
    }

    @Override
    public void tick() {
        if (target == null) return;
        this.timer--;

        boa.getLookControl().setLookAt(target, 30f, 30f);

        Vec3 eyes = boa.getEyePosition();
        double dx = eyes.x - target.getX();
        double dz = eyes.z - target.getZ();
        double dy = eyes.y - target.getEyeY();
        double horiz = Math.sqrt(dx * dx + dz * dz);

        float wantedYaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f;
        float wantedPitch = (float) (-(Mth.atan2(dy, horiz) * Mth.RAD_TO_DEG));

        if (target instanceof Player) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 0, false, false, false));
        } else {
            target.setYRot(approach(target.getYRot(), wantedYaw, this.turnSpeed));
            target.yHeadRot = approach(target.yHeadRot, wantedYaw, this.turnSpeed);
            target.yBodyRot = approach(target.yBodyRot, wantedYaw, this.turnSpeed);
            target.setXRot(approach(target.getXRot(), wantedPitch, this.turnSpeed));
            if (target instanceof Mob m) m.getLookControl().setLookAt(eyes.x, eyes.y, eyes.z);
            Vec3 v = target.getDeltaMovement();
            target.setDeltaMovement(v.x * this.slowFactor, v.y, v.z * this.slowFactor);
            target.hurtMarked = true;
        }

        if (boa.level() instanceof ServerLevel server) {
            float a = boa.tickCount * 0.4f;
            spawnHypnoSwirl(server, eyes, a);
            spawnHypnoSwirl(server, target.getBoundingBox().getCenter(), a + Mth.PI);
        }
    }

    private static final DustParticleOptions YELLOW  = new DustParticleOptions(new Vector3f(1.0f, 0.85f, 0.0f), 1.0f);
    private static final DustParticleOptions MAGENTA = new DustParticleOptions(new Vector3f(0.9f, 0.0f, 0.9f), 1.0f);

    private static void spawnHypnoSwirl(ServerLevel server, Vec3 center, float angle) {
        final double r = 0.45;
        for (int i = 0; i < 3; i++) {
            double ang = angle + i * (Math.PI * 2.0 / 3.0);
            double px = center.x + Math.cos(ang) * r;
            double pz = center.z + Math.sin(ang) * r;
            double py = center.y + Math.sin(ang * 1.5) * 0.15;
            DustParticleOptions col = (i % 2 == 0) ? YELLOW : MAGENTA;
            server.sendParticles(col, px, py, pz, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void stop() {
        boa.setHypnotizing(false);
        boa.setHypnosisTargetId(-1);
        boa.setHypnosisCooldown(this.cooldownTicks);
        this.target = null;
    }

    private static float approach(float current, float wanted, float fraction) {
        float delta = Mth.wrapDegrees(wanted - current);
        return current + delta * fraction;
    }
}
