package net.tiew.operationWild.entity.misc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

public class AdventurerManuscript extends Mob {

    public final AnimationState openAnimationState = new AnimationState();
    public final AnimationState nextPageAnimationState = new AnimationState();
    public final AnimationState precedentPageAnimationState = new AnimationState();
    public int openAnimationTimeout = 0;
    public int nextPageAnimationTimeout = 0;
    public int precedentPageAnimationTimeout = 0;

    public AdventurerManuscript(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes().add(Attributes.MAX_HEALTH, 1).add(Attributes.MOVEMENT_SPEED, 0.D).add(Attributes.FOLLOW_RANGE, 10.0D).add(Attributes.ATTACK_DAMAGE, 0.0D).add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public void tick() {
        super.tick();
    }

    public void setupAnimationState() {
        if (this.openAnimationTimeout > 0) {
            --this.openAnimationTimeout;
            if (this.openAnimationTimeout <= 0) {
                this.openAnimationState.stop();
            }
        }

        if (this.nextPageAnimationTimeout > 0) {
            --this.nextPageAnimationTimeout;
            if (this.nextPageAnimationTimeout <= 0) {
                this.nextPageAnimationState.stop();
            }
        }

        if (this.precedentPageAnimationTimeout > 0) {
            --this.precedentPageAnimationTimeout;
            if (this.precedentPageAnimationTimeout <= 0) {
                this.precedentPageAnimationState.stop();
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }
}
