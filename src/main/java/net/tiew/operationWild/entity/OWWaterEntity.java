package net.tiew.operationWild.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.core.OWDamageSources;
import net.tiew.operationWild.event.ClientEvents;

public abstract class OWWaterEntity extends OWEntity {

    private static final EntityDataAccessor<Float> TARGET_PITCH =
            SynchedEntityData.defineId(OWWaterEntity.class, EntityDataSerializers.FLOAT);

    public float damageTimer = 0;
    public boolean firstTimeToDeep = true;

    public OWWaterEntity(EntityType<? extends TamableAnimal> entityType, Level level,
                         float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TARGET_PITCH, 0.0f);
    }

    public abstract int getMaxDepth();
    public abstract float getSwimSpeed();

    public float getTargetPitch() { return this.entityData.get(TARGET_PITCH); }
    public void setTargetPitch(float pitch) { this.entityData.set(TARGET_PITCH, pitch); }


    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(this.getSpeed(), travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
        } else {
            super.travel(travelVector);
        }
    }

    @Override
    public boolean isPushedByFluid() { return false; }

    @Override
    public float getWaterSlowDown() { return 0.98F; }

    @Override
    protected int decreaseAirSupply(int currentAir) {
        return currentAir;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.isInWater()) {
            Vec3 mv = this.getDeltaMovement();
            if (mv.y > 0.3) {
                this.setDeltaMovement(mv.x * 0.5, 0.3, mv.z * 0.5);
            }
        }

        if (!this.level().isClientSide()) {
            if (!this.isInWater()) {
                float p = this.getTargetPitch();
                if (Math.abs(p) > 0.1f) this.setTargetPitch(p * 0.85f);
                else if (p != 0f) this.setTargetPitch(0f);
            }
        }

        int depth = (int) (this.level().getSeaLevel() - this.getY());
        if (depth >= this.getMaxDepth()) {
            LivingEntity rider = this.getControllingPassenger();
            if (rider instanceof Player player) {
                if (!player.isCreative()) applyWaterPressureDamage(depth, player);
            } else {
                applyWaterPressureDamage(depth, null);
            }
        } else {
            damageTimer = 0.0f;
            firstTimeToDeep = true;
        }
    }

    public void applyWaterPressureDamage(int depth, Player player) {
        if (!this.isTame() || !this.isInWater()) return;
        int waterPressure = (int) ClientEvents.getWaterPressure(depth);
        float damageInterval = (Math.max((-1.25f * waterPressure + 65) / 30.0f, 0.1f));
        float normalizedPressure = waterPressure / 4.0f;
        float intensity = 0.05f * (float) Math.pow(normalizedPressure, 2f);

        ClientEvents.shakeCamera(intensity, player);

        if (this.level().isClientSide) return;

        damageTimer += 0.05f;
        if (damageTimer >= damageInterval) {
            this.invulnerableTime = 0;
            DamageSource src = OWDamageSources.createWaterPressureDamage((ServerLevel) this.level());
            this.hurt(src, 4);
            this.invulnerableTime = 0;
            damageTimer = 0.0f;
        }

        if (this.tickCount % 100 == 0 || firstTimeToDeep) {
            Component message = Component.translatable("tooHighPressure")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW));
            firstTimeToDeep = false;
        }
    }

    protected boolean isAtSurface() {
        BlockPos posAbove = this.blockPosition().above();
        return this.level().isEmptyBlock(posAbove) || !this.level().getFluidState(posAbove).isEmpty() == false;
    }


    public static class OWSwimMoveControl extends MoveControl {
        private final OWWaterEntity entity;

        public OWSwimMoveControl(OWWaterEntity entity) {
            super(entity);
            this.entity = entity;
        }

        @Override
        public void tick() {
            if (this.entity.isInWater()) {
                this.entity.setDeltaMovement(this.entity.getDeltaMovement().add(0.0D, 0.003D, 0.0D));
            }

            if (this.operation == Operation.MOVE_TO) {
                double dx = this.wantedX - this.entity.getX();
                double dy = this.wantedY - this.entity.getY();
                double dz = this.wantedZ - this.entity.getZ();
                double distSq = dx * dx + dy * dy + dz * dz;

                if (distSq < 2.5E-7F) {
                    this.mob.setZza(0.0F);
                    return;
                }

                float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
                this.entity.setYRot(this.rotlerp(this.entity.getYRot(), yaw, 10.0F));
                this.entity.yBodyRot = this.entity.getYRot();
                this.entity.yHeadRot = this.entity.getYRot();

                float speed = (float) (this.speedModifier *
                        this.entity.getAttributeValue(Attributes.MOVEMENT_SPEED));

                if (this.entity.isInWater()) {
                    this.entity.setSpeed(speed * 0.02F);

                    float cosX = Mth.cos(this.entity.getXRot() * Mth.DEG_TO_RAD);
                    float sinX = Mth.sin(this.entity.getXRot() * Mth.DEG_TO_RAD);
                    this.entity.zza = cosX * speed;
                    this.entity.yya = -sinX * speed;
                } else {
                    this.entity.setSpeed(speed * 0.1F);
                }
            } else {
                this.entity.setSpeed(0.0F);
                this.entity.setXxa(0.0F);
                this.entity.setYya(0.0F);
                this.entity.setZza(0.0F);

                if (!this.entity.level().isClientSide()) {
                    float p = this.entity.getTargetPitch();
                    if (Math.abs(p) > 0.1f) this.entity.setTargetPitch(p * 0.88f);
                    else if (p != 0f) this.entity.setTargetPitch(0f);
                }
            }
        }
    }
}