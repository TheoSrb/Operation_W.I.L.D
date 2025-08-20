package net.tiew.operationWild.entity.categoy;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.tiew.operationWild.component.OWDataComponentTypes;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.custom.living.ElephantEntity;
import net.tiew.operationWild.item.custom.ElephantSaddle;
import net.tiew.operationWild.utils.OWUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OWGroupEntity extends OWEntity {

    public final boolean canBeAlpha;
    public OWGroupEntity alphaToFollow = null;

    private static final EntityDataAccessor<Boolean> IS_ALPHA = SynchedEntityData.defineId(OWGroupEntity.class, EntityDataSerializers.BOOLEAN);

    protected OWGroupEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed, boolean canBeAlpha) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
        this.canBeAlpha = canBeAlpha;
    }

    public void setAlpha(boolean isAlpha) { this.entityData.set(IS_ALPHA, isAlpha);}
    public boolean isAlpha() { return this.entityData.get(IS_ALPHA);}

    @Override
    public void tick() {
        super.tick();

        if (this.isTame()) setAlpha(false);

        //if (this.isAlpha()) OWUtils.spawnParticles(this, ParticleTypes.DRAGON_BREATH, 0, 0, 0, 50, 3);
        else if (!this.isTame()) {
            List<OWGroupEntity> entities = this.level().getEntitiesOfClass(OWGroupEntity.class, this.getBoundingBox().inflate(50));;

            for (OWGroupEntity entity : entities) {
                if (entity != this && entity.isAlpha()) {
                    alphaToFollow = entity;
                    break;
                }
            }
        }

        if (alphaToFollow != null && !this.isTame()) {
            double distanceBetweenThisAndAlpha = this.distanceTo(alphaToFollow);

            if (distanceBetweenThisAndAlpha >= 20) this.getNavigation().moveTo(alphaToFollow.getX(), alphaToFollow.getY(), alphaToFollow.getZ(), 1.5f);
        }
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        return this.isAlpha() ? super.hurt(damageSource, amount / 3) : super.hurt(damageSource, amount);
    }

    private boolean thereIsAlphaNear(int radius) {
        List<OWGroupEntity> entities = this.level().getEntitiesOfClass(OWGroupEntity.class, this.getBoundingBox().inflate(radius));

        for (OWGroupEntity entity : entities) {
            boolean isSameEntityType = entity.getClass() == this.getClass();
            boolean isThereAlpha = entity.isAlpha();

            if (isSameEntityType && isThereAlpha) {
                return true;
            }
        }
        return false;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance,
                                        MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {

        if (mobSpawnType != MobSpawnType.BREEDING) {
            if (!thereIsAlphaNear(50) && canBeAlpha) {
                this.setAlpha(true);
            }

        }
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_ALPHA, false);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("isAlpha", this.isAlpha());
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(IS_ALPHA, tag.getBoolean("isAlpha"));
    }
}
