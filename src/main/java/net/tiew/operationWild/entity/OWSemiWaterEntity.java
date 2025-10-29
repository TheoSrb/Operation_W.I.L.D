package net.tiew.operationWild.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.core.OWDamageSources;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.event.ClientEvents;

public abstract class OWSemiWaterEntity extends OWEntity {

    private static final EntityDataAccessor<Float> TARGET_PITCH = SynchedEntityData.defineId(OWSemiWaterEntity.class, EntityDataSerializers.FLOAT);

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

    public OWSemiWaterEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);

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
    public void travel(Vec3 vec3) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(0.1F, vec3);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.75D));

            int currentAir = this.getAirSupply();
            int maxAir = this.getMaxAirSupply();
            double airPercentage = (double) currentAir / maxAir * 100.0;
            int depth = (int) (this.level().getSeaLevel() - this.getY());

            if (airPercentage < 10.0 || depth >= getMaxDepth()) {
                if (!this.isAtSurface()) {
                    this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.015D, 0.0D));
                }
            }

            if (this.horizontalCollision && this.getDeltaMovement().y < 0.1) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.3D, 0.0D));
            }
        } else {
            super.travel(vec3);
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
        if (this instanceof CrocodileEntity crocodile && crocodile.getGrabbedTarget() != null && !crocodile.isTame()) return;

        LivingEntity rider = this.getControllingPassenger();
        int depth = (int) (this.level().getSeaLevel() - this.getY());

        if (this.isEffectiveAi() && !this.isVehicle() && !this.isSitting()) {
            switchNavigation();

            if (this.isInWater()) {
                handleSmoothSwimming();
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

        float yawSpeed = hasTarget ? TARGET_YAW_SPEED : YAW_SMOOTH_SPEED;
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
        } else {
            float currentPitch = this.getTargetPitch();
            if (Math.abs(currentPitch) > 0.5f) {
                float newPitch = currentPitch * (1.0f - PITCH_SMOOTH_SPEED);
                this.setTargetPitch(newPitch);
            } else {
                this.setTargetPitch(0.0f);
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

    protected void handleFreeSwimming() {
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

        this.setDeltaMovement(this.getDeltaMovement().add(moveX, depth > 1 ? verticalMove + 0.001 : 0, moveZ));
    }

    protected void handleTargetSwimming(LivingEntity target) {
        double yawRadians = Math.toRadians(swimYaw);
        double moveX = -Math.sin(yawRadians) * HORIZONTAL_SPEED * 1.25f;
        double moveZ = Math.cos(yawRadians) * HORIZONTAL_SPEED * 1.25f;

        double deltaY = target.getY() - this.getY();
        double verticalMove = 0;
        double depth = this.level().getSeaLevel() - this.getY();
        if (Math.abs(deltaY) > 0.5) {
            verticalMove = Math.signum(deltaY) * 0.04D * targetModeBlend;
        }

        this.setDeltaMovement(this.getDeltaMovement().add(moveX, depth > 1 ? verticalMove + 0.001 : 0, moveZ));
    }

    public void applyWaterPressureDamage(int depth, Player player) {
        if (!this.isTame()) return;
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

    protected void switchNavigation() {
        if (this.isInWater()) {
            if (this.navigation != this.waterNavigation) {
                this.navigation = this.waterNavigation;
            }
        } else {
            if (this.navigation != this.groundNavigation) {
                this.navigation = this.groundNavigation;
            }
        }
    }
}