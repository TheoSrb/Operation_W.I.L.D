package net.tiew.operationWild.entity.AI;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
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
            if (entity instanceof Player || entity instanceof SeaBugEntity) {
                if (!entity.isInvulnerable()) {
                    this.targetPlayer = entity;
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void tick() {
        List<Entity> entitiesCanBeHurt = this.jellyFish.level().getEntitiesOfClass(Entity.class, this.jellyFish.getBoundingBox().inflate(2));

        for (Entity entity : entitiesCanBeHurt) {
            if (entity instanceof Player player && player.isCreative()) continue;
            if (!entity.isInvulnerable() && !(entity instanceof JellyfishEntity)) {
                this.jellyFish.setElectrified(true);
            }
        }

        if (this.targetPlayer != null) {
            this.jellyFish.getNavigation().moveTo(this.targetPlayer, this.speedModifier);
        }
    }

}
