package net.tiew.operationWild.entity.goals.lion;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.clan.LionPride;

import java.util.EnumSet;

public class LionConcedeFleeGoal extends Goal {

    private static final double FLEE_STEP = 24.0;
    private static final double FLEE_SPEED = 2.2;
    private static final int REPATH_TICKS = 20;

    private final LionEntity lion;
    private int repath;

    public LionConcedeFleeGoal(LionEntity lion) {
        this.lion = lion;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return !lion.isTame() && lion.wild.fleeThreat() != null && !lion.isKnockedOut() && !lion.isVehicle();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        repath = 0;
        if (lion.isNapping()) lion.setNap(false);
        if (lion.isSitting()) lion.setSitting(false);
        lion.setTarget(null);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity threat = lion.wild.fleeThreat();
        if (threat == null) return;
        lion.setRunning(true);
        if (--repath > 0) return;
        repath = REPATH_TICKS;
        Vec3 away = new Vec3(lion.getX() - threat.getX(), 0, lion.getZ() - threat.getZ());
        if (away.lengthSqr() < 1.0E-4) away = Vec3.directionFromRotation(0f, lion.getYRot());
        Vec3 point = lion.position().add(away.normalize().scale(FLEE_STEP));
        net.minecraft.core.BlockPos ground = LionPride.surface(lion.level(), point);
        lion.getNavigation().moveTo(ground.getX() + 0.5, ground.getY(), ground.getZ() + 0.5, FLEE_SPEED);
    }

    @Override
    public void stop() {
        lion.setRunning(false);
        lion.getNavigation().stop();
    }
}
