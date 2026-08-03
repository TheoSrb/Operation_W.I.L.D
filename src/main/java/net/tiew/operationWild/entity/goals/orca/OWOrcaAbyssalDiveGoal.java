package net.tiew.operationWild.entity.goals.orca;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;

import java.util.EnumSet;
import java.util.List;

public class OWOrcaAbyssalDiveGoal extends Goal {

    private static final double SEARCH_RANGE = 18.0;

    private static final double BITE_RANGE = 4.2;

    private static final double MIN_WATER_COLUMN = 16.0;

    private static final double FLOOR_MARGIN = 2.5;

    private static final double DIVE_SPEED = 0.48;

    private static final double DIVE_FORWARD = 0.34;

    private static final float DIVE_TURN_MAX = 1.4f;

    private static final int STALK_TIMEOUT = 220;
    private static final int BITE_TIMEOUT = 45;
    private static final int DIVE_TIMEOUT = 400;

    private static final int TRIGGER_ODDS = 60;

    private static final int COOLDOWN_MIN = 700;
    private static final int COOLDOWN_MAX = 1800;

    /** Temps pendant lequel l'orque laisse sa victime tranquille après l'avoir rendue. */
    private static final int DISINTEREST_TICKS = 260;

    private enum Phase { STALK, BITE, DIVE }

    private final OrcaEntity orca;
    private LivingEntity prey;
    private Phase phase = Phase.STALK;
    private int phaseTicks;
    private int cooldown;
    private boolean done;
    private float diveTurn;

    public OWOrcaAbyssalDiveGoal(OrcaEntity orca) {
        this.orca = orca;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        if (!isUsable()) return false;
        if (this.orca.getRandom().nextInt(TRIGGER_ODDS) != 0) return false;
        if (this.orca.waterColumnBelow() < MIN_WATER_COLUMN) return false;

        this.prey = findPrey();
        return this.prey != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.done || !isUsable()) return false;
        if (this.prey == null || !this.prey.isAlive()) return false;
        if (this.phase != Phase.DIVE && this.orca.distanceTo(this.prey) > SEARCH_RANGE + 8.0) return false;
        return this.phaseTicks <= timeoutFor(this.phase);
    }

    @Override
    public void start() {
        this.phase = Phase.STALK;
        this.phaseTicks = 0;
        this.done = false;
        this.orca.setCombo(false, 0);
    }

    @Override
    public void stop() {
        if (this.orca.hasSwallowed()) this.orca.beginSpit();
        this.orca.setAbyssalHold(false);
        this.orca.getNavigation().stop();
        this.prey = null;
        this.cooldown = COOLDOWN_MIN + this.orca.getRandom().nextInt(COOLDOWN_MAX - COOLDOWN_MIN);
    }

    @Override
    public void tick() {
        if (this.prey == null) return;
        this.phaseTicks++;

        switch (this.phase) {
            case STALK -> tickStalk();
            case BITE -> tickBite();
            case DIVE -> tickDive();
        }
    }

    private void tickStalk() {
        this.orca.getLookControl().setLookAt(
                this.prey.getX(), this.prey.getEyeY(), this.prey.getZ());

        if (this.orca.distanceTo(this.prey) <= BITE_RANGE && this.orca.hasMouthTarget()) {
            this.orca.getNavigation().stop();
            this.orca.activateBigMouth();
            this.phase = Phase.BITE;
            this.phaseTicks = 0;
            return;
        }
        if (this.phaseTicks % 8 == 0) {
            this.orca.getNavigation().moveTo(
                    this.prey.getX(), this.prey.getY(), this.prey.getZ(), 1.5);
        }
    }

    private void tickBite() {
        this.orca.getLookControl().setLookAt(
                this.prey.getX(), this.prey.getEyeY(), this.prey.getZ());

        if (this.orca.hasSwallowed()) {
            this.orca.setAbyssalHold(true);
            this.orca.holdSwallowedFor(DIVE_TIMEOUT + 80);
            this.phase = Phase.DIVE;
            this.phaseTicks = 0;
            this.diveTurn = (this.orca.getRandom().nextFloat() * 2f - 1f) * DIVE_TURN_MAX;

            this.orca.level().playSound(null, this.orca.getX(), this.orca.getY(), this.orca.getZ(),
                    SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 2.0F, 0.5F);
            return;
        }
        if (this.orca.getMouthLungeTicks() <= 0) this.done = true;
    }

    private void tickDive() {
        this.orca.getNavigation().stop();

        double seabed = this.orca.seabedYBelow();
        boolean touched = !Double.isNaN(seabed) && this.orca.getY() <= seabed + FLOOR_MARGIN;
        if (touched || this.phaseTicks >= DIVE_TIMEOUT || !this.orca.hasSwallowed()) {
            releaseAtDepth();
            return;
        }

        Vec3 forward = Vec3.directionFromRotation(0f, this.orca.getYRot());
        this.orca.setDeltaMovement(
                forward.x * DIVE_FORWARD, -DIVE_SPEED, forward.z * DIVE_FORWARD);
        this.orca.hasImpulse = true;

        this.orca.setYRot(this.orca.getYRot() + this.diveTurn);
        this.orca.yBodyRot = this.orca.getYRot();
        this.orca.yHeadRot = this.orca.getYRot();

        if (this.orca.level() instanceof ServerLevel serverLevel && this.phaseTicks % 3 == 0) {
            serverLevel.sendParticles(ParticleTypes.BUBBLE,
                    this.orca.getX(), this.orca.getY() + this.orca.getBbHeight(), this.orca.getZ(),
                    8, 0.8, 0.4, 0.8, 0.02);
        }
        if (this.phaseTicks % 40 == 0) {
            this.orca.level().playSound(null, this.orca.getX(), this.orca.getY(), this.orca.getZ(),
                    SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 1.6F, 0.45F);
        }
    }

    private void releaseAtDepth() {
        this.done = true;
        this.orca.setAbyssalHold(false);

        // Elle en a fini : elle rend sa proie et s'en va. La reprendre en chasse dans la seconde
        // annulerait tout le sens de la descente, qui est de rendre la liberté — et le fond de la
        // mer est déjà bien assez punitif sans qu'elle y remette la gueule.
        this.orca.setDisinterest(DISINTEREST_TICKS);
        this.orca.forceSetTarget(null);

        double x = this.orca.getX(), y = this.orca.getY(), z = this.orca.getZ();
        this.orca.beginSpit();

        if (this.orca.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.BUBBLE,
                    x, y, z, 60, 1.6, 0.8, 1.6, 0.12);
        }
    }

    private static int timeoutFor(Phase phase) {
        return switch (phase) {
            case STALK -> STALK_TIMEOUT;
            case BITE -> BITE_TIMEOUT;
            case DIVE -> DIVE_TIMEOUT + 40;
        };
    }

    private boolean isUsable() {
        // Proie déjà en gueule : on ne réexamine plus que le strict nécessaire.
        //
        // Toutes les conditions d'entrée décrivent une orque LIBRE de ses mouvements, et la prise
        // elle-même en viole plusieurs : la victime devient sa passagère, donc l'orque devient un
        // « véhicule ». Les appliquer telles quelles pendant la descente revient à la faire annuler
        // par son propre succès — et l'arrêt du goal recrache la proie, d'où une prise qui ne tient
        // pas. Une fois la gueule refermée, seule compte la capacité à plonger.
        if (this.orca.hasSwallowed()) {
            return !this.orca.isTame()
                    && !this.orca.isBeached()
                    && this.orca.isInWater();
        }

        return !this.orca.isTame()
                && !this.orca.isBaby()
                && !this.orca.isBeached()
                && !this.orca.isSleeping()
                && this.orca.getControllingPassenger() == null
                && !this.orca.isSpyhopping()
                && !this.orca.isWaveEngaged()
                && this.orca.isInWater();
    }

    private LivingEntity findPrey() {
        AABB box = this.orca.getBoundingBox().inflate(SEARCH_RANGE);
        List<LivingEntity> candidates = this.orca.level().getEntitiesOfClass(
                LivingEntity.class, box,
                e -> e.isInWater()
                        && !(e instanceof OrcaEntity)
                        && !this.orca.isAlliedTo(e)
                        && this.orca.canSwallow(e));

        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity candidate : candidates) {
            double dist = this.orca.distanceToSqr(candidate);
            if (dist < bestDist) {
                bestDist = dist;
                best = candidate;
            }
        }
        return best;
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
