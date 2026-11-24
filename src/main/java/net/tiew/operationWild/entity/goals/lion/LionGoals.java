package net.tiew.operationWild.entity.goals.lion;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.clan.LionClanData;
import net.tiew.operationWild.entity.clan.LionHunt;
import net.tiew.operationWild.particle.OWParticles;
import org.jetbrains.annotations.Nullable;

public final class LionGoals {

    private static final int NAP_PARTICLE_CYCLE = 23;

    private LionGoals() {
    }

    public static boolean isFreeWild(LionEntity lion) {
        return lion.isAlive() && !lion.isTame() && !lion.isVehicle() && !lion.isPassenger() && !lion.isSleeping()
                && !lion.isKnockedOut() && !lion.isSedatedFleeing() && !lion.isInResurrection() && !lion.isRoaring()
                && !lion.isCalling() && !lion.isDistressing() && !lion.isLatchActive() && !lion.isLeashed()
                && !lion.isFleeingDanger() && !lion.wild.isConceding();
    }

    public static boolean isCalm(LionEntity lion) {
        return isFreeWild(lion) && lion.getTarget() == null && !lion.isSedationAgitated();
    }

    public static boolean onSchedule(LionEntity lion, int interval) {
        return (lion.tickCount / 2 + lion.getId()) % Math.max(1, interval / 2) == 0;
    }

    @Nullable
    public static LionHunt.Role huntRole(LionEntity lion, @Nullable LionClanData.Clan clan) {
        if (clan == null || clan.state.hunt == null || clan.state.hunt.isFinished()) return null;
        return clan.state.hunt.roleOf(lion);
    }

    public static double horizontalDistance(LionEntity lion, Vec3 point) {
        double dx = point.x - lion.getX();
        double dz = point.z - lion.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static void faceTowards(LionEntity lion, Vec3 point, float maxStep) {
        double dx = point.x - lion.getX();
        double dz = point.z - lion.getZ();
        if (dx * dx + dz * dz < 1.0E-4) return;
        float yaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90f;
        lion.setYRot(net.minecraft.util.Mth.approachDegrees(lion.getYRot(), yaw, maxStep));
        lion.yBodyRot = lion.getYRot();
    }

    public static void napEffects(LionEntity lion, int napTicks) {
        if (napTicks % NAP_PARTICLE_CYCLE != 0 || !(lion.level() instanceof ServerLevel level)) return;
        Vec3 look = lion.getLookAngle();
        double forward = lion.napParticleForward();
        level.sendParticles(OWParticles.NAP_PARTICLES.get(),
                lion.getX() + look.x * forward, lion.getY() + lion.napParticleHeight(), lion.getZ() + look.z * forward,
                1, 0.1, 0.1, 0.1, 0.0);
    }

    public static void holdStill(LionEntity lion) {
        lion.getNavigation().stop();
        if (lion.onGround()) lion.setDeltaMovement(0, lion.getDeltaMovement().y, 0);
    }
}
