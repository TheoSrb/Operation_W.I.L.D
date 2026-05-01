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
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.core.OWDamageSources;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.event.ClientEvents;

public abstract class OWWaterEntity extends OWEntity {

    private static final EntityDataAccessor<Float> TARGET_PITCH = SynchedEntityData.defineId(OWWaterEntity.class, EntityDataSerializers.FLOAT);

    private float swimYaw = 0;
    private float targetYaw = 0;
    private float yawChangeTimer = 0;
    private float verticalWave = 0;
    private float targetDepth = 0;
    private float depthChangeTimer = 0;

    public float damageTimer = 0;
    public boolean firstTimeToDeep = true;

    private final float YAW_CHANGE_INTERVAL;
    private final float YAW_SMOOTH_SPEED;
    private final float HORIZONTAL_SPEED;
    private final float VERTICAL_WAVE_SPEED = 0.03f;
    private final float VERTICAL_WAVE_AMPLITUDE = 0.008f;
    private final float DEPTH_CHANGE_INTERVAL;
    private final float DEPTH_CHANGE_SPEED;
    private final float SURFACE_RISE_SPEED;

    private final float TARGET_YAW_SPEED = 0.03f;
    private final float PITCH_SMOOTH_SPEED = 0.08f;
    private final float TARGET_TRANSITION_SPEED = 0.1f;
    private float targetModeBlend = 0.0f;

    private GroundPathNavigation groundNavigation;
    private WaterBoundPathNavigation waterNavigation;

    public OWWaterEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);

        this.moveControl = new OWSwimMoveControl(this);

        this.groundNavigation = new GroundPathNavigation(this, level);
        this.waterNavigation = new WaterBoundPathNavigation(this, level);

        this.YAW_SMOOTH_SPEED = 0.015f * getSwimSpeed();
        this.HORIZONTAL_SPEED = 0.02f * getSwimSpeed();
        this.DEPTH_CHANGE_SPEED = 0.02f * getSwimSpeed();
        this.SURFACE_RISE_SPEED = 0.02f * getSwimSpeed();

        this.swimYaw = this.random.nextFloat() * 360f;
        this.targetYaw = this.random.nextFloat() * 360f;
        this.yawChangeTimer = this.random.nextFloat() * 200f;
        this.verticalWave = this.random.nextFloat() * (float)(Math.PI * 2);
        this.depthChangeTimer = this.random.nextFloat() * 300f;
        this.targetDepth = Float.MAX_VALUE;

        this.YAW_CHANGE_INTERVAL = 150f + this.random.nextFloat() * 100f;
        this.DEPTH_CHANGE_INTERVAL = 250f + this.random.nextFloat() * 100f;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TARGET_PITCH, 0.0f);
    }

    public float getTargetPitch() {
        return this.entityData.get(TARGET_PITCH);
    }

    public void setTargetPitch(float pitch) {
        this.entityData.set(TARGET_PITCH, pitch);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
    }

    public abstract int getMaxDepth();
    public abstract float getSwimSpeed();

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(this.getSpeed(), travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
            if (this.getTarget() == null) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.005D, 0.0D));
            }
        } else {
            super.travel(travelVector);
        }
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public float getWaterSlowDown() {
        return 0.98F;
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity rider = this.getControllingPassenger();
        int depth = (int) (this.level().getSeaLevel() - this.getY());

        if (this.isEffectiveAi() && !this.isVehicle() && !this.isSitting()) {
            if (this.isInWater()) {
                handleSmoothSwimming();
            } else if (!this.level().isClientSide()) {
                float p = this.getTargetPitch();
                if (Math.abs(p) > 0.1f) {
                    this.setTargetPitch(p * 0.82f);
                } else if (p != 0f) {
                    this.setTargetPitch(0f);
                }
            }
        }

        if (depth >= this.getMaxDepth()) {
            if (rider != null) {
                if (rider instanceof Player player) {
                    if (!player.isCreative()) {
                        applyWaterPressureDamage(depth, player);
                    }
                }
            } else applyWaterPressureDamage(depth, null);
        } else {
            damageTimer = 0.0f;
            firstTimeToDeep = true;
        }
    }

    protected void handleSmoothSwimming() {
        LivingEntity target = this.getTarget();
        boolean hasTarget = target != null;

        if (hasTarget) {
            targetModeBlend = Math.min(1.0f, targetModeBlend + TARGET_TRANSITION_SPEED);
        } else {
            targetModeBlend = Math.max(0.0f, targetModeBlend - TARGET_TRANSITION_SPEED);
        }

        if (hasTarget) {
            double deltaX = target.getX() - this.getX();
            double deltaZ = target.getZ() - this.getZ();
            targetYaw = (float)(Math.toDegrees(Math.atan2(deltaZ, deltaX))) - 90f;
        } else {
            yawChangeTimer++;
            if (yawChangeTimer >= YAW_CHANGE_INTERVAL) {
                targetYaw = this.getRandom().nextFloat() * 360f;
                yawChangeTimer = 0;
            }

            if (this.horizontalCollision) {
                targetYaw = swimYaw + 90f + (this.getRandom().nextFloat() * 180f - 90f);
                yawChangeTimer = 0;
            }
        }

        while (targetYaw > 360f) targetYaw -= 360f;
        while (targetYaw < 0f) targetYaw += 360f;

        float yawDiff = targetYaw - swimYaw;
        while (yawDiff > 180f) yawDiff -= 360f;
        while (yawDiff < -180f) yawDiff += 360f;

        float yawSpeed = hasTarget ? 0.18f : YAW_SMOOTH_SPEED;
        swimYaw += yawDiff * yawSpeed;

        while (swimYaw > 360f) swimYaw -= 360f;
        while (swimYaw < 0f) swimYaw += 360f;

        this.setYRot(swimYaw);
        this.setYHeadRot(swimYaw);
        this.setYBodyRot(swimYaw);

        if (hasTarget) {
            double deltaY = target.getY() - this.getY();
            double deltaX = target.getX() - this.getX();
            double deltaZ = target.getZ() - this.getZ();
            double distance3D = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

            if (distance3D > 0.1) {
                float desiredPitch = (float)Math.toDegrees(Math.asin(-deltaY / distance3D));
                float currentPitch = this.getTargetPitch();
                float pitchDiff = desiredPitch - currentPitch;
                float newPitch = currentPitch + pitchDiff * PITCH_SMOOTH_SPEED;
                newPitch = Math.max(-90f, Math.min(90f, newPitch));
                this.setTargetPitch(newPitch);
            }
        }

        if (!hasTarget) {
            depthChangeTimer++;
            if (depthChangeTimer >= DEPTH_CHANGE_INTERVAL) {
                int maxDepthLimit = Math.max((int)(this.level().getSeaLevel() - getMaxDepth()), -64);
                int surfaceLevel = (int)this.level().getSeaLevel();
                float safeMinY = Math.max(maxDepthLimit, (float)this.getY() - 5f);
                float safeMaxY = Math.min(surfaceLevel, (float)this.getY() + 5f);
                targetDepth = safeMinY + this.getRandom().nextFloat() * (safeMaxY - safeMinY);
                depthChangeTimer = 0;
            }
        }

        int currentAir = this.getAirSupply();
        int maxAir = this.getMaxAirSupply();
        double airPercentage = (double) currentAir / maxAir * 100.0;
        int depth = (int) (this.level().getSeaLevel() - this.getY());

        if (airPercentage < 15.0 || depth >= getMaxDepth()) {
            if (!this.isAtSurface()) {
                double yawRadians = Math.toRadians(swimYaw);
                double moveX = -Math.sin(yawRadians) * HORIZONTAL_SPEED * 0.5;
                double moveZ = Math.cos(yawRadians) * HORIZONTAL_SPEED * 0.5;
                this.setDeltaMovement(this.getDeltaMovement().add(moveX, SURFACE_RISE_SPEED, moveZ));
                return;
            }
        }

        if (hasTarget) {
            handleTargetSwimming(target);
            return;
        }

        if (this.getNavigation().getPath() != null) {
            handlePathSwimming();
            return;
        }

        handleFreeSwimming();
    }

    @Override
    protected int decreaseAirSupply(int currentAir) {
        return currentAir;
    }

    protected void handleFreeSwimming() {
        if (targetDepth == Float.MAX_VALUE) targetDepth = (float) this.getY();

        double yawRadians = Math.toRadians(swimYaw);
        double moveX = -Math.sin(yawRadians) * HORIZONTAL_SPEED;
        double moveZ = Math.cos(yawRadians) * HORIZONTAL_SPEED;

        verticalWave += VERTICAL_WAVE_SPEED;
        double verticalMove = Math.sin(verticalWave) * VERTICAL_WAVE_AMPLITUDE;

        double currentY = this.getY();
        double depth = this.level().getSeaLevel() - this.getY();
        double depthDiff = targetDepth - currentY;

        if (Math.abs(depthDiff) > 1.0) {
            if (depthDiff > 0) {
                verticalMove += DEPTH_CHANGE_SPEED;
            } else {
                verticalMove -= DEPTH_CHANGE_SPEED;
            }
        } else if (Math.abs(depthDiff) > 0.3) {
            double smoothFactor = Math.abs(depthDiff);
            if (depthDiff > 0) {
                verticalMove += DEPTH_CHANGE_SPEED * smoothFactor;
            } else {
                verticalMove -= DEPTH_CHANGE_SPEED * smoothFactor;
            }
        }

        // Pitch visuel proportionnel à la direction verticale de nage
        float desiredPitch;
        if (Math.abs(depthDiff) > 0.5) {
            double pitchMagnitude = Math.min(Math.abs(depthDiff) / 6.0, 1.0) * 38.0;
            desiredPitch = (float)(depthDiff > 0 ? -pitchMagnitude : pitchMagnitude);
        } else {
            desiredPitch = 0f;
        }
        float newPitch = this.getTargetPitch() + (desiredPitch - this.getTargetPitch()) * PITCH_SMOOTH_SPEED;
        this.setTargetPitch(newPitch);

        this.setDeltaMovement(this.getDeltaMovement().add(moveX, depth > 1 ? verticalMove + 0.001 : 0, moveZ));
    }

    protected void handleTargetSwimming(LivingEntity target) {
        double deltaX = target.getX() - this.getX();
        double deltaY = target.getY() - this.getY();
        double deltaZ = target.getZ() - this.getZ();
        double horizDist = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double dist3D   = Math.sqrt(horizDist * horizDist + deltaY * deltaY);

        // Burst à courte portée pour le lunge final
        float speedMult = (float)(horizDist < 4.0 ? 3.5 : 2.5);
        double yawRadians = Math.toRadians(swimYaw);
        double moveX = -Math.sin(yawRadians) * HORIZONTAL_SPEED * speedMult;
        double moveZ =  Math.cos(yawRadians) * HORIZONTAL_SPEED * speedMult;

        // Poursuite verticale proportionnelle — réaction immédiate, pas de blend
        double depth = this.level().getSeaLevel() - this.getY();
        double verticalMove = 0;
        if (dist3D > 0.5) {
            verticalMove = (deltaY / dist3D) * HORIZONTAL_SPEED * speedMult;
        }

        this.setDeltaMovement(this.getDeltaMovement().add(
                moveX, depth > 1 ? verticalMove + 0.001 : 0, moveZ));
    }

    public void applyWaterPressureDamage(int depth, Player player) {
        if (!this.isTame() || !this.isInWater()) return;
        int waterPressure = (int) ClientEvents.getWaterPressure(depth);
        float damageInterval = (Math.max((-1.25f * waterPressure + 65) / 30.0f, 0.1f));
        float normalizedPressure = waterPressure / 4.0f;
        float intensity = 0.05f * (float) Math.pow(normalizedPressure, 2f);

        ClientEvents.shakeCamera(intensity, player);

        if (this.level().isClientSide) {
            return;
        }

        damageTimer += 0.05f;

        if (damageTimer >= damageInterval) {
            this.invulnerableTime = 0;

            DamageSource waterPressureDamage = OWDamageSources.createWaterPressureDamage((ServerLevel) this.level());
            this.hurt(waterPressureDamage, 4);

            this.invulnerableTime = 0;
            damageTimer = 0.0f;
        }

        if (this.tickCount % 100 == 0 || firstTimeToDeep) {
            Component message = Component.translatable("tooHighPressure")
                    .setStyle(Style.EMPTY
                            .withColor(ChatFormatting.YELLOW));
            //Minecraft.getInstance().gui.setOverlayMessage(message, true); /!\ SERVER DON'T WORK
            firstTimeToDeep = false;
        }
    }

    protected void handlePathSwimming() {
        BlockPos targetPos = this.getNavigation().getTargetPos();
        if (targetPos == null) return;

        double yDiff = targetPos.getY() - this.getY();
        double yawRadians = Math.toRadians(swimYaw);
        double depth = this.level().getSeaLevel() - this.getY();
        double moveX = -Math.sin(yawRadians) * HORIZONTAL_SPEED * 1.5;
        double moveZ = Math.cos(yawRadians) * HORIZONTAL_SPEED * 1.5;

        double verticalMove = 0;
        if (yDiff > 0.5D) {
            verticalMove = 0.03D;
        } else if (yDiff < -0.5D) {
            verticalMove = -0.03D;
        }

        this.setDeltaMovement(this.getDeltaMovement().add(moveX, depth > 1 ? verticalMove + 0.001 : 0, moveZ));
    }

    protected boolean isAtSurface() {
        BlockPos posAbove = this.blockPosition().above();
        return !this.level().getFluidState(posAbove).isEmpty() == false || this.level().isEmptyBlock(posAbove);
    }

    // Classe interne dans OWWaterEntity
    public static class OWSwimMoveControl extends MoveControl {
        private final OWWaterEntity entity;

        public OWSwimMoveControl(OWWaterEntity entity) {
            super(entity);
            this.entity = entity;
        }

        @Override
        public void tick() {
            if (this.entity.isInWater()) {
                this.entity.setDeltaMovement(this.entity.getDeltaMovement().add(0.0D, 0.005D, 0.0D));
            }

            if (this.operation == MoveControl.Operation.MOVE_TO && !this.entity.getNavigation().isDone()) {
                double d0 = this.wantedX - this.entity.getX();
                double d1 = this.wantedY - this.entity.getY();
                double d2 = this.wantedZ - this.entity.getZ();
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                if (d3 < 2.5000003E-7F) {
                    this.mob.setZza(0.0F);
                } else {
                    float f = (float)(Mth.atan2(d2, d0) * Mth.RAD_TO_DEG) - 90.0F;
                    this.entity.setYRot(this.rotlerp(this.entity.getYRot(), f, 10.0F));
                    this.entity.yBodyRot = this.entity.getYRot();
                    this.entity.yHeadRot = this.entity.getYRot();

                    float f1 = (float)(this.speedModifier * this.entity.getAttributeValue(Attributes.MOVEMENT_SPEED));

                    if (this.entity.isInWater()) {
                        this.entity.setSpeed(f1 * 0.02F);
                        float f2 = -(float)(Mth.atan2(d1, Mth.sqrt((float)(d0 * d0 + d2 * d2))) * Mth.RAD_TO_DEG);
                        f2 = Mth.clamp(Mth.wrapDegrees(f2), -85.0F, 85.0F);
                        this.entity.setXRot(this.rotlerp(this.entity.getXRot(), f2, 5.0F));

                        float xRotRad = this.entity.getXRot() * Mth.DEG_TO_RAD;
                        float f3 = Mth.cos(xRotRad);
                        float f4 = Mth.sin(xRotRad);
                        this.entity.zza = f3 * f1;
                        this.entity.yya = -f4 * f1;
                    } else {
                        this.entity.setSpeed(f1 * 0.1F);
                    }
                }
            } else {
                this.entity.setSpeed(0.0F);
                this.entity.setXxa(0.0F);
                this.entity.setYya(0.0F);
                this.entity.setZza(0.0F);
            }
        }
    }
}