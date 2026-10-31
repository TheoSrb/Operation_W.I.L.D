package net.tiew.operationWild.entity.goals.colossal_squid;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.tiew.operationWild.entity.animals.aquatic.ColossalSquidEntity;

import java.util.EnumSet;

/**
 * Errance du calamar sur toute la colonne d'eau : chaque destination est choisie entre le fond et
 * la surface du point visé, avec une préférence pour l'espace libre, donc vers le haut quand il
 * frôle le sol marin et vers le bas quand il touche la surface. Il ne s'arrête jamais : à peine
 * arrivé, il repart vers le point suivant.
 */
public class ColossalSquidWanderGoal extends Goal {

    private static final int PICK_ATTEMPTS = 10;
    private static final int COLUMN_SCAN = 48;
    private static final double MIN_DISTANCE = 10.0;
    private static final double MAX_DISTANCE = 24.0;
    private static final double WANDER_ARC = Math.PI * 0.9;
    private static final double FLOOR_MARGIN = 2.5;
    private static final double SURFACE_MARGIN = 2.5;
    private static final double EDGE_BAND = 5.0;
    private static final double ARRIVAL_SQR = 6.25;
    private static final int STALL_LIMIT = 60;

    private final ColossalSquidEntity squid;
    private double targetX;
    private double targetY;
    private double targetZ;
    private double lastX;
    private double lastY;
    private double lastZ;
    private int stallTicks;

    public ColossalSquidWanderGoal(ColossalSquidEntity squid) {
        this.squid = squid;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return isFree() && pickTarget();
    }

    @Override
    public boolean canContinueToUse() {
        return isFree();
    }

    @Override
    public void start() {
        this.stallTicks = 0;
        this.lastX = this.squid.getX();
        this.lastY = this.squid.getY();
        this.lastZ = this.squid.getZ();
    }

    @Override
    public void stop() {
        this.squid.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.squid.distanceToSqr(this.targetX, this.targetY, this.targetZ) <= ARRIVAL_SQR) pickTarget();
        this.squid.getMoveControl().setWantedPosition(this.targetX, this.targetY, this.targetZ, 1.0);

        double moved = this.squid.distanceToSqr(this.lastX, this.lastY, this.lastZ);
        this.lastX = this.squid.getX();
        this.lastY = this.squid.getY();
        this.lastZ = this.squid.getZ();
        if (moved > 0.0016) {
            this.stallTicks = 0;
        } else if (++this.stallTicks >= STALL_LIMIT) {
            this.stallTicks = 0;
            pickTarget();
        }
    }

    private boolean isFree() {
        return this.squid.isInWater() && !this.squid.isVehicle() && this.squid.getTarget() == null
                && !this.squid.isSitting() && !this.squid.isNapping() && !this.squid.isSleeping()
                && !this.squid.isCombatFacing() && this.squid.getGrabPhase() == ColossalSquidEntity.GRAB_NONE;
    }

    private boolean pickTarget() {
        Level level = this.squid.level();
        double heading = -this.squid.getYRot() * Mth.DEG_TO_RAD;

        for (int attempt = 0; attempt < PICK_ATTEMPTS; attempt++) {
            double angle = heading + (this.squid.getRandom().nextDouble() - 0.5) * WANDER_ARC * (attempt < PICK_ATTEMPTS / 2 ? 1 : 2);
            double distance = MIN_DISTANCE + this.squid.getRandom().nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);
            double x = this.squid.getX() + Math.sin(angle) * distance;
            double z = this.squid.getZ() + Math.cos(angle) * distance;

            BlockPos start = BlockPos.containing(x, this.squid.getY(), z);
            if (!isWater(level, start)) continue;

            int floor = start.getY();
            while (floor > start.getY() - COLUMN_SCAN && isWater(level, start.atY(floor - 1))) floor--;
            int surface = start.getY();
            while (surface < start.getY() + COLUMN_SCAN && isWater(level, start.atY(surface + 1))) surface++;

            double low = Math.max(floor + FLOOR_MARGIN, surface + 1 - this.squid.getMaxDepth());
            double high = surface + 1 - SURFACE_MARGIN;
            if (high <= low) {
                low = high = (floor + surface + 1) * 0.5;
            }

            this.targetX = x;
            this.targetZ = z;
            this.targetY = pickDepth(low, high);
            return true;
        }
        return false;
    }

    private double pickDepth(double low, double high) {
        double y = this.squid.getY();
        double span = high - low;
        if (span <= 0) return low;

        double roll = this.squid.getRandom().nextDouble();
        if (y - low < EDGE_BAND) return Mth.lerp(0.4 + roll * 0.6, low, high);
        if (high - y < EDGE_BAND) return Mth.lerp(roll * 0.6, low, high);
        return Mth.lerp(roll, low, high);
    }

    private static boolean isWater(Level level, BlockPos pos) {
        return level.getFluidState(pos).is(FluidTags.WATER) && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }
}
