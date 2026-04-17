package net.tiew.operationWild.entity.goals.tiger;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;

import java.util.EnumSet;
import java.util.List;

public class TigerDistractedByFoodGoal extends Goal {
    private final TigerEntity tiger;
    private ItemEntity targetItem;
    private int eatTick = 0;
    private static final int EAT_DURATION = 30;

    private int foodEatenCount = 0;
    private int foodCooldownTick = 0;
    private static final int MAX_FOOD_PER_CYCLE = 3;
    private static final int FOOD_CYCLE_DURATION = 6000;

    public TigerDistractedByFoodGoal(TigerEntity tiger) {
        this.tiger = tiger;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {

        if (foodCooldownTick > 0) {
            foodCooldownTick--;
            if (foodCooldownTick == 0) foodEatenCount = 0;
        }

        if (foodEatenCount >= MAX_FOOD_PER_CYCLE) return false;

        if (this.tiger.getPersistentData().contains("last_hurt_tick")) {
            int lastHurt = this.tiger.getPersistentData().getInt("last_hurt_tick");
            if (this.tiger.tickCount - lastHurt < 800) {
                return false;
            }
        }

        if (tiger.isPreparing || tiger.isGrabbing() || tiger.isRoaring() || tiger.isNapping()) {
            return false;
        }

        if (this.tiger.getTarget() != null) {
            List<ItemEntity> items = this.tiger.level().getEntitiesOfClass(ItemEntity.class,
                    this.tiger.getBoundingBox().inflate(1.5D));
            for (ItemEntity item : items) {
                if (item.getItem().is(OWTags.Items.TIGER_FOOD)) {
                    this.targetItem = item;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void start() {
        this.eatTick = 0;
        this.tiger.setEating(true);
        this.tiger.setTarget(null);
        this.tiger.getNavigation().stop();
        System.out.println("start");
    }

    @Override
    public void tick() {
        System.out.println("tick");
        if (targetItem != null && targetItem.isAlive()) {
            this.tiger.getLookControl().setLookAt(targetItem, 30.0F, 30.0F);

            this.eatTick++;

            if (this.eatTick % 10 == 0) {
                this.tiger.level().playSound(null, tiger.getX(), tiger.getY(), tiger.getZ(),
                        SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.5F, 1.0F);
                if (this.tiger.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, targetItem.getItem()),
                            targetItem.getX(), targetItem.getY(), targetItem.getZ(), 10, 0.1, 0.1, 0.1, 0.05);
                }
            }

            if (this.eatTick >= EAT_DURATION) {
                this.finalizeEating();
            }
        }
    }

    private void finalizeEating() {
        if (targetItem != null) {
            targetItem.getItem().shrink(1);
            if (targetItem.getItem().isEmpty()) targetItem.discard();
        }
        this.eatTick = EAT_DURATION + 1;

        foodEatenCount++;
        if (foodEatenCount >= MAX_FOOD_PER_CYCLE && foodCooldownTick == 0) {
            foodCooldownTick = FOOD_CYCLE_DURATION;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (this.tiger.getPersistentData().contains("last_hurt_tick")) {
            int lastHurt = this.tiger.getPersistentData().getInt("last_hurt_tick");
            if (this.tiger.tickCount - lastHurt < 800) return false;
        }

        if (foodEatenCount >= MAX_FOOD_PER_CYCLE) return false;

        return targetItem != null && targetItem.isAlive() && eatTick <= EAT_DURATION;
    }

    @Override
    public void stop() {
        this.tiger.setEating(false);
        this.targetItem = null;
        System.out.println("stop");
    }
}