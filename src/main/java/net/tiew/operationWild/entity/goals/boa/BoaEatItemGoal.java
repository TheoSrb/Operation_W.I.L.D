package net.tiew.operationWild.entity.goals.boa;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;

import java.util.Comparator;
import java.util.EnumSet;

public class BoaEatItemGoal extends Goal {

    private static final double SEARCH_RADIUS = 8.0;
    private static final double EAT_RANGE = 1.8;
    private static final int TRIGGER_CHANCE = 12;
    private static final int HIT_TICK = 10;
    private static final int FINISH_TICK = 22;

    private final BoaEntity boa;
    private ItemEntity targetItem;

    private boolean bitten = false;
    private boolean swallowed = false;
    private int biteTick = 0;
    private ItemStack eatenStack = ItemStack.EMPTY;
    private int cooldown = 0;

    public BoaEatItemGoal(BoaEntity boa) {
        this.boa = boa;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (!isEligible()) return false;
        if (boa.getRandom().nextInt(TRIGGER_CHANCE) != 0) return false;

        ItemEntity found = findMeatItem();
        if (found == null) return false;
        this.targetItem = found;
        return true;
    }

    private boolean isEligible() {
        if (boa.isTame()) return false;
        if (boa.getTarget() != null) return false;
        if (boa.isVehicle()) return false;
        if (boa.isSleeping() || boa.isNapping()) return false;
        if (boa.isConstricting()) return false;
        if (boa.isInWater()) return false;
        return true;
    }

    private ItemEntity findMeatItem() {
        AABB box = boa.getBoundingBox().inflate(SEARCH_RADIUS);
        return boa.level().getEntitiesOfClass(ItemEntity.class, box, this::isEdibleMeat).stream()
                .min(Comparator.comparingDouble(boa::distanceToSqr))
                .orElse(null);
    }

    private boolean isEdibleMeat(ItemEntity item) {
        return item.isAlive() && item.getItem().is(Tags.Items.FOODS_RAW_MEAT);
    }

    @Override
    public void start() {
        this.bitten = false;
        this.swallowed = false;
        this.biteTick = 0;
        this.eatenStack = ItemStack.EMPTY;
        boa.getNavigation().moveTo(targetItem, 1.0);
    }

    @Override
    public boolean canContinueToUse() {
        if (!isEligible()) return false;
        if (swallowed) return boa.tickCount - biteTick < FINISH_TICK;
        return targetItem != null && targetItem.isAlive();
    }

    @Override
    public void tick() {
        if (targetItem != null) {
            boa.getLookControl().setLookAt(
                    targetItem.getX(), targetItem.getY(), targetItem.getZ(), 30f, 30f);
        }

        if (!bitten) {
            boa.getNavigation().moveTo(targetItem, 1.0);
            if (boa.distanceToSqr(targetItem) <= EAT_RANGE * EAT_RANGE) {
                this.eatenStack = targetItem.getItem().copy();
                boa.setCombo(true, 1);
                boa.getNavigation().stop();
                this.bitten = true;
                this.biteTick = boa.tickCount;
            }
            return;
        }

        boa.getNavigation().stop();
        spawnMouthParticles();

        int elapsed = boa.tickCount - biteTick;
        if (!swallowed && elapsed >= HIT_TICK) {
            swallow();
        }
    }

    private void spawnMouthParticles() {
        if (!(boa.level() instanceof ServerLevel server)) return;
        if (eatenStack.isEmpty()) return;
        Vec3 mouth = boa.getEyePosition().add(boa.getLookAngle().scale(0.4));
        server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, eatenStack),
                mouth.x, mouth.y, mouth.z, 3, 0.12, 0.12, 0.12, 0.02);
    }

    private void swallow() {
        this.swallowed = true;
        if (targetItem != null && targetItem.isAlive()) {
            ItemStack stack = targetItem.getItem();
            stack.shrink(1);
            if (stack.isEmpty()) targetItem.discard();
        }

        FoodProperties food = eatenStack.get(DataComponents.FOOD);
        float power = food != null ? Mth.clamp(food.nutrition() * 2.5f, 4f, 30f) : 8f;
        boa.triggerItemDigestion(power);

        float pitch = (float) OWUtils.generateRandomInterval(0.8, 1.0);
        boa.level().playSound(null, boa.getX(), boa.getY(), boa.getZ(),
                SoundEvents.GENERIC_EAT, SoundSource.HOSTILE, 1.0f, pitch);
    }

    @Override
    public void stop() {
        this.targetItem = null;
        this.bitten = false;
        this.swallowed = false;
        this.eatenStack = ItemStack.EMPTY;
        boa.getNavigation().stop();
        this.cooldown = (int) OWUtils.generateRandomInterval(100, 240);
    }
}
