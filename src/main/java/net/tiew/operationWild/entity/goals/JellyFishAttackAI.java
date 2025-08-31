package net.tiew.operationWild.entity.goals;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.tiew.operationWild.entity.custom.living.JellyfishEntity;
import net.tiew.operationWild.entity.custom.vehicle.SeaBugEntity;

import java.util.EnumSet;
import java.util.List;

public class JellyFishAttackAI extends Goal {

    private final JellyfishEntity jellyFish;
    private Entity targetPlayer;
    private final double followDistance;
    private final double speedModifier;

    public JellyFishAttackAI(JellyfishEntity jellyFish, double followDistance, double speedModifier) {
        this.jellyFish = jellyFish;
        this.followDistance = followDistance;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        List<Entity> players = this.jellyFish.level().getEntitiesOfClass(Entity.class, this.jellyFish.getBoundingBox().inflate(this.followDistance));

        for (Entity entity : players) {
            if (entity instanceof Player || entity instanceof SeaBugEntity || entity instanceof Boat || entity instanceof Drowned) {
                if (entity instanceof Player player && player.isCreative()) return false;
                if (entity.isAlive()) {
                    this.targetPlayer = entity;
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void tick() {
        if (this.targetPlayer != null) {
            double targetX = this.targetPlayer.getX();
            double targetY = this.targetPlayer.getY();
            double targetZ = this.targetPlayer.getZ();

            if (!this.targetPlayer.isInWater()) {
                int seaLevel = this.jellyFish.level().getSeaLevel();
                targetY = Math.min(targetY, seaLevel - 1);
            }

            this.jellyFish.getNavigation().moveTo(targetX, targetY, targetZ, this.speedModifier);
        }
    }

}
