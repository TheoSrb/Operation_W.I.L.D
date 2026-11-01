package net.tiew.operationWild.entity.goals.colossal_squid;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.aquatic.ColossalSquidEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.misc.SeaBugEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;

public class ColossalSquidSeabugAmbushGoal extends Goal {

    private static final int SCAN_INTERVAL = 20;
    private static final int FLOOR_SCAN = 48;
    private static final double FLOOR_MARGIN = 3.0;
    private static final double LUNGE_SPEED = 3.0;
    private static final double DIVE_SPEED = 1.4;

    private final ColossalSquidEntity squid;
    @Nullable
    private SeaBugEntity prey;
    private int scanCooldown;
    private double diveY;

    public ColossalSquidSeabugAmbushGoal(ColossalSquidEntity squid) {
        this.squid = squid;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (--this.scanCooldown > 0) return false;
        this.scanCooldown = SCAN_INTERVAL;
        if (!this.squid.canHuntSeabug()) return false;
        this.prey = findPrey();
        return this.prey != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.squid.getSeabugPhase() != ColossalSquidEntity.SEABUG_NONE;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        if (this.prey != null) this.squid.beginSeabugHunt(this.prey);
        this.diveY = this.squid.getY();
    }

    @Override
    public void stop() {
        if (this.squid.getSeabugPhase() != ColossalSquidEntity.SEABUG_NONE) this.squid.endSeabugAssault(true);
        this.prey = null;
    }

    @Override
    public void tick() {
        SeaBugEntity seabug = this.squid.getSeizedSeabug();
        if (seabug == null) return;
        int phase = this.squid.getSeabugPhase();
        if (phase == ColossalSquidEntity.SEABUG_LUNGE) {
            this.squid.getLookControl().setLookAt(seabug, 30f, 30f);
            double scale = this.squid.getScale();
            if (this.squid.distanceTo(seabug) <= OWAttacksConstants.ColossalSquid.SEABUG_TURN_RANGE * scale) this.squid.requestCombatFacing();
            Vec3 aim = seabug.position().add(0, seabug.getBbHeight() * 0.5, 0);
            this.squid.getMoveControl().setWantedPosition(aim.x, aim.y, aim.z, LUNGE_SPEED);
            if (this.squid.readyToSeizeSeabug()) {
                this.squid.seizeSeabug();
                this.diveY = Math.max(floorBelow() + FLOOR_MARGIN, this.squid.getY() - OWAttacksConstants.ColossalSquid.SEABUG_DIVE_STEP);
            }
            return;
        }
        if (phase == ColossalSquidEntity.SEABUG_SHAKE) {
            Vec3 forward = Vec3.directionFromRotation(0f, this.squid.getYRot());
            this.squid.getMoveControl().setWantedPosition(this.squid.getX() + forward.x, this.diveY, this.squid.getZ() + forward.z, DIVE_SPEED);
        }
    }

    @Nullable
    private SeaBugEntity findPrey() {
        double range = OWAttacksConstants.ColossalSquid.SEABUG_DETECT_RANGE * this.squid.getScale();
        return this.squid.level().getEntitiesOfClass(SeaBugEntity.class, this.squid.getBoundingBox().inflate(range),
                        seabug -> seabug.isAlive() && seabug.isInWater() && !seabug.isPassenger() && this.squid.distanceTo(seabug) <= range)
                .stream()
                .min(Comparator.comparingInt((SeaBugEntity seabug) -> seabug.getFirstPassenger() instanceof Player ? 0 : 1)
                        .thenComparingDouble(this.squid::distanceToSqr))
                .orElse(null);
    }

    private double floorBelow() {
        BlockPos pos = this.squid.blockPosition();
        int floor = pos.getY();
        while (floor > pos.getY() - FLOOR_SCAN && this.squid.level().getFluidState(pos.atY(floor - 1)).is(FluidTags.WATER)) floor--;
        return floor;
    }
}
