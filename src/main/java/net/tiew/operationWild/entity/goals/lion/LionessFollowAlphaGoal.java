package net.tiew.operationWild.entity.goals.lion;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;

import java.util.EnumSet;

public class LionessFollowAlphaGoal extends Goal {

    private LionEntity lion;
    private int clanCheckTimer = 0;

    public LionessFollowAlphaGoal(LionEntity lion) {
        this.lion = lion;

        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public void tick() {
        super.tick();
        if (lion.isAlive() && !lion.isTame() && lion.clan != null && lion.getTarget() == null) {
            clanCheckTimer++;

            if (clanCheckTimer >= 40) {
                clanCheckTimer = 0;

                LionEntity alpha = lion.clan.getAlpha();

                if (alpha != null && alpha.isAlive()) {
                    double distance = lion.distanceTo(alpha);

                    if (distance >= 10) {
                        Vec3 toAlpha = new Vec3(alpha.getX() - lion.getX(), 0, alpha.getZ() - lion.getZ()).normalize();

                        double randomAngle = (lion.getRandom().nextDouble() - 0.5) * Math.PI / 2;
                        double cos = Math.cos(randomAngle);
                        double sin = Math.sin(randomAngle);

                        double newX = toAlpha.x * cos - toAlpha.z * sin;
                        double newZ = toAlpha.x * sin + toAlpha.z * cos;

                        double targetDistance = 2 + lion.getRandom().nextDouble() * 3;
                        double targetX = lion.getX() + newX * targetDistance;
                        double targetZ = lion.getZ() + newZ * targetDistance;

                        lion.getNavigation().moveTo(targetX, lion.getY(), targetZ, 0.9D);
                    }
                }
            }
        }
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public boolean canContinueToUse() {
        return lion.isFemale() && lion.clan != null;
    }

    @Override
    public boolean canUse() {
        return lion.isFemale() && lion.clan != null;
    }
}
