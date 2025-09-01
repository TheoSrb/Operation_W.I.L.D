package net.tiew.operationWild.entity.misc;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.entity.OWEntity;

public class SeabugShard extends OWEntity {
    protected SeabugShard(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return false;
    }

    @Override
    public boolean isPushedByFluid(FluidType type) {
        return false;
    }

    @Override
    public void onInsideBubbleColumn(boolean b) {
        if (!isVehicle()) {
            super.onAboveBubbleCol(b);
        }
        this.resetFallDistance();
    }

    @Override
    public void onAboveBubbleCol(boolean b) {
        if (!isVehicle()) {
            super.onAboveBubbleCol(b);
        }
    }

    @Override
    public boolean addEffect(MobEffectInstance mobEffectInstance, @Nullable Entity entity) {
        if (mobEffectInstance.getEffect() == MobEffects.GLOWING) {
            return super.addEffect(mobEffectInstance, entity);
        }

        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isInWater()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.02, 0.0));
        }
    }
}
