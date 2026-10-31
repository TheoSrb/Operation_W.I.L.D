package net.tiew.operationWild.entity.misc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.particle.OWParticles;
import net.tiew.operationWild.sound.OWSounds;

import javax.annotation.Nullable;

public class TranquilizerDart extends AbstractArrow {
    public static final int TRANQUILIZER_EFFECTIVENESS = 120;
    public static final int CONCENTRATED_TRANQUILIZER_EFFECTIVENESS = 160;
    public static final float DAMAGE_FACTOR = 0.15f;
    public static final double BASE_DAMAGE = 0.05;

    public static final double RANGE_MULTIPLIER = 2;
    public static final double AIR_GRAVITY = 0.03 / RANGE_MULTIPLIER;
    public static final double WATER_GRAVITY = 0.01 / RANGE_MULTIPLIER;
    public static final float WATER_INERTIA = 0.9f;

    private static final int TRAIL_PARTICLES_PER_TICK = 4;

    private static final EntityDataAccessor<Boolean> CONCENTRATED = SynchedEntityData.defineId(TranquilizerDart.class, EntityDataSerializers.BOOLEAN);

    public TranquilizerDart(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.setBaseDamage(BASE_DAMAGE);
    }

    public TranquilizerDart(LivingEntity shooter, Level level, ItemStack ammo, @Nullable ItemStack weapon, boolean concentrated) {
        super(OWEntityRegistry.TRANQUILIZER_DART.get(), shooter, level, ammo, weapon);
        this.setBaseDamage(BASE_DAMAGE);
        this.setConcentrated(concentrated);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CONCENTRATED, false);
    }

    public boolean isConcentrated() {
        return this.entityData.get(CONCENTRATED);
    }

    public void setConcentrated(boolean concentrated) {
        this.entityData.set(CONCENTRATED, concentrated);
    }

    public int getTranquilizerEffectiveness() {
        return this.isConcentrated() ? CONCENTRATED_TRANQUILIZER_EFFECTIVENESS : TRANQUILIZER_EFFECTIVENESS;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide && !this.inGround && this.tickCount > 1) {
            for (int i = 0; i < TRAIL_PARTICLES_PER_TICK; i++) {
                double t = (double) i / TRAIL_PARTICLES_PER_TICK;
                this.level().addParticle(OWParticles.TRANQUILIZER_DART_TRAIL.get(), Mth.lerp(t, this.xo, this.getX()), Mth.lerp(t, this.yo, this.getY()), Mth.lerp(t, this.zo, this.getZ()), 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected double getDefaultGravity() {
        return this.isInWater() ? WATER_GRAVITY : AIR_GRAVITY;
    }

    @Override
    protected float getWaterInertia() {
        return WATER_INERTIA;
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return OWSounds.TRANQUILIZER_DART_HIT.get();
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(this.isConcentrated() ? OWItems.CONCENTRATED_TRANQUILIZER_DART.get() : OWItems.TRANQUILIZER_DART.get());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        Vec3 motion = target.getDeltaMovement();
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            target.setDeltaMovement(motion);
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }

    @Override
    protected void doKnockback(LivingEntity target, DamageSource damageSource) {
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Concentrated", this.isConcentrated());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setConcentrated(compound.getBoolean("Concentrated"));
    }
}
