package net.tiew.operationWild.entity.animals.aquatic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.goal.FollowBoatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.vehicle.Boat;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.core.OWUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.attacks.OWAttacksHandler;
import net.tiew.operationWild.entity.behavior.OrcaBehaviorHandler;
import net.tiew.operationWild.entity.goals.orca.OWOrcaPackHuntGoal;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.entity.OWWaterEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.config.*;
import net.tiew.operationWild.entity.goals.global.OWAttackGoal;
import net.tiew.operationWild.entity.goals.orca.OWOrcaBeachingGoal;
import net.tiew.operationWild.entity.navigation.SwimmerJumpPathNavigator;
import net.tiew.operationWild.entity.variants.OrcaVariant;
import net.tiew.operationWild.sound.OWSounds;
import net.tiew.operationWild.core.OWTags;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.AnimalSoulItem;

import java.util.*;

import static net.tiew.operationWild.core.OWUtils.RANDOM;

public class OrcaEntity extends OWWaterEntity implements IOWEntity, IOWTamable, IOWRideable {

    public static final double TAMING_EXPERIENCE = 270.0;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> RIDER_CONTROL_PITCH = SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_DASHING = SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_BEACHED = SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> PACK_ROLE = SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SWALLOWED_TARGET_ID = SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MOUTH_LUNGE_TICKS = SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MOUTH_SPIT_TICKS = SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ULTIMATE_KILL_COUNT = SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_SPYHOPPING = SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.BOOLEAN);

    private int dashTicksLeft = 0;
    private Vec3 dashDirection = Vec3.ZERO;
    private float dashPeak = RIDDEN_DASH_PEAK;
    private int dashDuration = RIDDEN_DASH_TICKS;
    private Vec3 dashStart = Vec3.ZERO;
    private double dashMaxRange = RIDDEN_DASH_RANGE;

    private static final double RIDDEN_DASH_RANGE = 34.0;

    private static final float RIDDEN_DASH_PEAK = 3.8f;
    private static final int RIDDEN_DASH_TICKS = 30;

    public final OrcaBehaviorHandler orcaBehaviorHandler = new OrcaBehaviorHandler(this);
    private OrcaEntity packLeader = null;


    public volatile float bodyAnimY = 0f;
    public volatile float bodyAnimXRot = 0f;


    public volatile float bodyAnimX = 0f;
    public volatile float bodyAnimY_passenger = 0f;
    public volatile float bodyZRot_passenger = 0f;
    public volatile float bodyXRot_passenger = 0f;
    public volatile float bodyAnimX_passenger = 0f;

    public volatile org.joml.Matrix4f boneMatrix = null;

    private static final float REST_X = 0f, REST_Y = 7f, REST_Z = -2f;

    private static final float MODEL_ORIGIN_Y = 1.501f;

    private static final float SEAT_FORWARD = 0.1f;

    private static final float COMBO_SEAT_FORWARD = 0.2f;

    public volatile float bodyYRot = 0f;
    public volatile float bodyYRot_passenger = 0f;

    public volatile float camXRot = 0f, camYRot = 0f, camZRot = 0f;


    private Vec3 lastPitchCheckPos = null;

    public OrcaEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
        this.moveControl = new OWSwimMoveControl(this);
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 70.0)
                .add(Attributes.MOVEMENT_SPEED, 0.18D)
                .add(Attributes.FOLLOW_RANGE, 34.0D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new TryFindWaterGoal(this));

        this.goalSelector.addGoal(1, new net.tiew.operationWild.entity.goals.orca.OWOrcaWaveWashGoal(this));
        this.goalSelector.addGoal(1, new net.tiew.operationWild.entity.goals.orca.OWOrcaAbyssalDiveGoal(this));
        this.goalSelector.addGoal(1, new net.tiew.operationWild.entity.goals.orca.OWOrcaPreyToyGoal(this));
        this.goalSelector.addGoal(1, new net.tiew.operationWild.entity.goals.orca.OWOrcaBreathingHoleGoal(this));
        this.goalSelector.addGoal(1, new net.tiew.operationWild.entity.goals.orca.OWOrcaSpyhopGoal(this));
        this.goalSelector.addGoal(1, new FollowBoatGoal(this) {
            @Override
            public boolean canUse() {
                return OrcaEntity.this.isTame() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return OrcaEntity.this.isTame() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(2, new net.tiew.operationWild.entity.goals.orca.OWOrcaBoatStrikeGoal(this));
        this.goalSelector.addGoal(2, new OWOrcaPackHuntGoal(this));
        this.goalSelector.addGoal(2, new OWAttackGoal(this, this.getSpeed() * 11f, 28, 4, false) {
            private int retreatTicks = 0;
            private boolean wasCombo = false;

            private boolean isBlockedForWild() {
                if (OrcaEntity.this.isTame()) return false;
                if (OrcaEntity.this.isDisinterested()) return true;
                LivingEntity t = OrcaEntity.this.getTarget();
                return t != null && !isReachableFromWater(t);
            }

            @Override
            public void tick() {
                if (OrcaEntity.this.isTame()) {
                    super.tick();
                    return;
                }

                if (this.retreatTicks > 0) {
                    this.retreatTicks--;
                    tickRetreat();
                    return;
                }

                LivingEntity target = OrcaEntity.this.getTarget();
                boolean atStandoff = target != null
                        && !OrcaEntity.this.isDashing()
                        && OrcaEntity.this.distanceTo(target) <= WILD_STANDOFF_DISTANCE;

                if (atStandoff) tickAtStandoff(target);
                else super.tick();

                tickDisengageDecision();
            }

            private void tickAtStandoff(LivingEntity target) {
                if (!OrcaEntity.this.getNavigation().isDone()) {
                    OrcaEntity.this.getNavigation().stop();
                }
                OrcaEntity.this.setLookAt(target.getX(), target.getY(), target.getZ());

                double centerY = OrcaEntity.this.getY() + OrcaEntity.this.getBbHeight() * 0.5;
                double targetY = target.getY() + target.getBbHeight() * 0.5;
                double dy = targetY - centerY;

                if (Math.abs(dy) > BITE_DEPTH_TOLERANCE) {
                    Vec3 mv = OrcaEntity.this.getDeltaMovement();
                    OrcaEntity.this.setDeltaMovement(
                            mv.x, Mth.clamp(dy * 0.18, -0.14, 0.14), mv.z);
                    OrcaEntity.this.hasImpulse = true;
                }

                this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
                if (this.ticksUntilNextAttack <= 0
                        && Math.abs(dy) <= BITE_DEPTH_TOLERANCE
                        && OrcaEntity.this.getSensing().hasLineOfSight(target)) {
                    this.performAttack(target);
                    this.ticksUntilNextAttack = this.attackCooldown;
                }
            }

            private void tickDisengageDecision() {
                boolean combo = OrcaEntity.this.isCombo();
                boolean comboJustEnded = this.wasCombo && !combo;
                this.wasCombo = combo;

                if (!comboJustEnded) return;
                if (OrcaEntity.this.getRandom().nextInt(100) >= WILD_DISENGAGE_CHANCE) return;

                this.retreatTicks = WILD_DISENGAGE_TICKS_MIN
                        + OrcaEntity.this.getRandom().nextInt(WILD_DISENGAGE_SPREAD);
            }

            private void tickRetreat() {
                LivingEntity t = OrcaEntity.this.getTarget();
                if (t == null || !t.isAlive()) {
                    this.retreatTicks = 0;
                    return;
                }
                OrcaEntity.this.getLookControl().setLookAt(t.getX(), t.getEyeY(), t.getZ());

                if (this.retreatTicks % 10 != 0) return;
                Vec3 away = OrcaEntity.this.position().subtract(t.position());
                away = away.lengthSqr() > 1.0E-4 ? away.normalize() : OrcaEntity.this.getLookAngle();
                Vec3 to = OrcaEntity.this.position().add(away.scale(WILD_DISENGAGE_DISTANCE));
                OrcaEntity.this.getNavigation().moveTo(to.x, to.y, to.z, 1.15);
            }


            @Override
            public boolean canUse() {
                if (isBlockedForWild()) return false;
                return super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                if (isBlockedForWild()) return false;
                return super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(3, new OWOrcaBeachingGoal(this));
        this.goalSelector.addGoal(4, new OrcaWanderGoal(this));
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(RIDER_CONTROL_PITCH, 0.0f);
        builder.define(IS_DASHING, false);
        builder.define(IS_BEACHED, false);
        builder.define(PACK_ROLE, OrcaBehaviorHandler.PACK_ROLE_NONE);
        builder.define(SWALLOWED_TARGET_ID, -1);
        builder.define(MOUTH_LUNGE_TICKS, 0);
        builder.define(MOUTH_SPIT_TICKS, 0);
        builder.define(ULTIMATE_KILL_COUNT, 0);
        builder.define(IS_SPYHOPPING, false);
        builder.define(IS_WAVE_CHARGING, false);
        builder.define(TAIL_FLICK_ID, 0);
        builder.define(WAVE_BREACH_ID, 0);
        builder.define(FLOP_SLAM_ID, 0);
    }

    @Override
    protected boolean isLeapingVehicle() {
        return this.entityData.get(IS_DASHING) || this.flopHopTicks > 0;
    }

    public void setDashing(boolean dashing) {
        this.entityData.set(IS_DASHING, dashing);
    }

    public boolean isDashing() {
        return this.entityData.get(IS_DASHING);
    }

    private static final int BARREL_DURATION = 24;

    private static final float BARREL_TURN = -360f;

    private float barrelProgress = 1f;
    private float barrelProgressPrev = 1f;
    private boolean barrelRunning = false;
    private boolean wasDashing = false;

    public float getBarrelRoll(float partialTick) {
        if (this.barrelProgressPrev >= 1f) return 0f;
        float t = Mth.clamp(Mth.lerp(partialTick, this.barrelProgressPrev, this.barrelProgress), 0f, 1f);
        float remaining = 1f - t;
        float eased = 1f - remaining * remaining * remaining;
        return BARREL_TURN * eased;
    }

    @Override
    public boolean isRollingFigure() {
        return this.barrelProgressPrev < 1f;
    }

    private static final double BARREL_BUBBLE_RADIUS = 2.2;
    private static final int BARREL_BUBBLES_PER_TICK = 5;

    private void spawnBarrelBubbles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        if (!this.isInWater()) return;

        double angle = Math.toRadians(getBarrelRoll(1f));
        double yawRad = Math.toRadians(this.yBodyRot);
        double axisX = -Math.sin(yawRad), axisZ = Math.cos(yawRad);
        double sideX = Math.cos(yawRad), sideZ = Math.sin(yawRad);

        double scale = this.getScale();

        for (int i = 0; i < BARREL_BUBBLES_PER_TICK; i++) {
            double along = (-1.6 + 3.6 * (i / (double) (BARREL_BUBBLES_PER_TICK - 1))) * scale;
            double phase = angle - along * 0.45;
            double radius = BARREL_BUBBLE_RADIUS * scale * (0.75 + this.random.nextDouble() * 0.35);

            double px = this.getX() + axisX * along + sideX * Math.cos(phase) * radius;
            double pz = this.getZ() + axisZ * along + sideZ * Math.cos(phase) * radius;
            double py = this.getY() + this.getBbHeight() * 0.5 + Math.sin(phase) * radius;

            serverLevel.sendParticles(ParticleTypes.BUBBLE, px, py, pz, 1, 0.05, 0.05, 0.05, 0.01);
            if (i % 2 == 0) {
                serverLevel.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, px, py, pz, 1, 0.02, 0.02, 0.02, 0.0);
            }
        }
    }

    private void tickBarrelRoll() {
        this.barrelProgressPrev = this.barrelProgress;

        boolean dashing = this.isDashing();
        if (dashing && !this.wasDashing) {
            this.barrelProgress = 0f;
            this.barrelProgressPrev = 0f;
            this.barrelRunning = true;
        }
        this.wasDashing = dashing;

        if (this.barrelRunning) {
            this.barrelProgress += 1f / BARREL_DURATION;
            if (this.barrelProgress >= 1f) {
                this.barrelProgress = 1f;
                this.barrelRunning = false;
            }
            spawnBarrelBubbles();
        }
    }

    private static final float SPYHOP_PITCH = -78.0f;
    private static final float SPYHOP_RISE = 0.09f;
    private static final float SPYHOP_FALL = 0.13f;

    private static final int SPYHOP_MAX_RISE = 6;

    private static final double SPYHOP_SUBMERSION = 0.85;

    private float spyhopBlend = 0f;
    private float spyhopBlendPrev = 0f;
    private int spyhopTicksLeft = 0;

    public boolean isSpyhopping() {
        return this.entityData.get(IS_SPYHOPPING);
    }

    public void startSpyhop(int ticks) {
        if (this.level().isClientSide()) return;
        this.spyhopTicksLeft = Math.max(0, ticks);
        this.entityData.set(IS_SPYHOPPING, this.spyhopTicksLeft > 0);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                net.minecraft.sounds.SoundEvents.DOLPHIN_JUMP, SoundSource.NEUTRAL, 0.9f, 0.55f);
    }

    public void stopSpyhop() {
        if (this.level().isClientSide()) return;
        this.spyhopTicksLeft = 0;
        this.entityData.set(IS_SPYHOPPING, false);
    }

    public float getSpyhopAmount(float partialTick) {
        return Mth.lerp(partialTick, this.spyhopBlendPrev, this.spyhopBlend);
    }

    @Override
    public float getRidePitch(float partialTick) {
        float base = super.getRidePitch(partialTick);
        float amount = this.getSpyhopAmount(partialTick);
        return amount <= 0.001f ? base : Mth.lerp(amount, base, SPYHOP_PITCH);
    }

    private void tickSpyhop() {
        this.spyhopBlendPrev = this.spyhopBlend;
        float target = this.isSpyhopping() ? 1f : 0f;
        this.spyhopBlend += (target - this.spyhopBlend)
                * (target > this.spyhopBlend ? SPYHOP_RISE : SPYHOP_FALL);
        if (this.spyhopBlend < 0.001f) this.spyhopBlend = 0f;

        if (this.level().isClientSide()) return;

        if (this.spyhopTicksLeft <= 0) {
            if (this.isSpyhopping()) stopSpyhop();
            return;
        }
        if (--this.spyhopTicksLeft <= 0) {
            stopSpyhop();
            return;
        }

        double surface = surfaceYAbove();
        if (Double.isNaN(surface)) {
            stopSpyhop();
            return;
        }
        holdAtSurface(surface);
        if (this.spyhopTicksLeft % 5 == 0) spawnSpyhopSpray(surface);
    }

    private void holdAtSurface(double surfaceY) {
        double wanted = surfaceY - this.getBbHeight() * SPYHOP_SUBMERSION;
        double climb = Mth.clamp((wanted - this.getY()) * 0.25, -0.08, 0.08);
        Vec3 mv = this.getDeltaMovement();
        this.setDeltaMovement(mv.x * 0.6, climb, mv.z * 0.6);
        this.setXxa(0f);
        this.setYya(0f);
        this.setZza(0f);
        this.setXRot(0f);
        this.xRotO = 0f;
    }

    private double surfaceYAbove() {
        BlockPos.MutableBlockPos cursor = this.blockPosition().mutable();
        if (!this.level().getFluidState(cursor).is(net.minecraft.tags.FluidTags.WATER)) return Double.NaN;
        for (int i = 0; i < SPYHOP_MAX_RISE; i++) {
            cursor.move(Direction.UP);
            if (this.level().getFluidState(cursor).is(net.minecraft.tags.FluidTags.WATER)) continue;
            return this.level().getBlockState(cursor).isAir() ? cursor.getY() : Double.NaN;
        }
        return Double.NaN;
    }

    public boolean canSpyhopHere() {
        if (!this.isInWater()) return false;
        double surface = surfaceYAbove();
        if (Double.isNaN(surface)) return false;
        return surface - (this.getY() + this.getBbHeight()) <= SPYHOP_SURFACE_MARGIN;
    }

    private static final double SPYHOP_SURFACE_MARGIN = 1.5;

    public void lookAtUnlessOverhead(double x, double y, double z) {
        double dx = x - this.getX();
        double dz = z - this.getZ();
        if (dx * dx + dz * dz <= LOOK_OVERHEAD_DEAD_ZONE) return;
        this.getLookControl().setLookAt(x, y, z);
    }

    private static final double LOOK_OVERHEAD_DEAD_ZONE = 0.36;

    private static final int ICE_SCAN_HEIGHT = 16;

    public @Nullable BlockPos iceCapAbove() {
        BlockPos.MutableBlockPos cursor = this.blockPosition().mutable();
        if (!this.level().getFluidState(cursor).is(net.minecraft.tags.FluidTags.WATER)) return null;
        for (int i = 0; i < ICE_SCAN_HEIGHT; i++) {
            cursor.move(Direction.UP);
            if (this.level().getFluidState(cursor).is(net.minecraft.tags.FluidTags.WATER)) continue;
            return isThinIce(this.level().getBlockState(cursor)) ? cursor.immutable() : null;
        }
        return null;
    }

    private static final EntityDataAccessor<Boolean> IS_WAVE_CHARGING =
            SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.BOOLEAN);

    private static final float WAVE_CHARGE_RISE = 0.16f;
    private static final float WAVE_CHARGE_FALL = 0.09f;

    private float waveChargeBlend = 0f;
    private float waveChargeBlendPrev = 0f;

    private boolean waveEngaged = false;

    public boolean isWaveEngaged() {
        return this.waveEngaged;
    }

    public void setWaveEngaged(boolean engaged) {
        this.waveEngaged = engaged;
    }

    public boolean isWaveCharging() {
        return this.entityData.get(IS_WAVE_CHARGING);
    }

    public void setWaveCharging(boolean charging) {
        if (this.level().isClientSide()) return;
        this.entityData.set(IS_WAVE_CHARGING, charging);
    }

    public float getWaveChargeAmount(float partialTick) {
        return Mth.lerp(partialTick, this.waveChargeBlendPrev, this.waveChargeBlend);
    }

    @Override
    protected float pitchMaxAngle() {
        return this.isWaveCharging() || this.isWaveBreaching() ? 0f : super.pitchMaxAngle();
    }

    public static final int WAVE_BREACH_DURATION = 26;

    public static final int WAVE_BREACH_SLAM = 7;

    private static final float WAVE_SPIN_TURN = 360f;

    private static final EntityDataAccessor<Integer> WAVE_BREACH_ID =
            SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.INT);

    private int waveBreachTicksLeft = 0;
    private float waveBreachAge = -1f;
    private float waveBreachAgePrev = -1f;
    private int lastSeenBreachId = Integer.MIN_VALUE;

    public boolean isWaveBreaching() {
        return this.level().isClientSide() ? this.waveBreachAge >= 0f : this.waveBreachTicksLeft > 0;
    }

    public void startWaveBreach() {
        if (this.level().isClientSide()) return;
        this.waveBreachTicksLeft = WAVE_BREACH_DURATION;
        this.entityData.set(WAVE_BREACH_ID, this.entityData.get(WAVE_BREACH_ID) + 1);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                net.minecraft.sounds.SoundEvents.DOLPHIN_JUMP, SoundSource.HOSTILE, 2.0f, 0.5f);
    }

    public boolean isWaveBreachSlam() {
        return this.waveBreachTicksLeft == WAVE_BREACH_DURATION - WAVE_BREACH_SLAM;
    }

    private boolean isWaveBreachFlickTick() {
        int lead = FLICK_ANIM_WINDUP + FLICK_ANIM_SNAP / 2;
        return this.waveBreachTicksLeft == WAVE_BREACH_DURATION - Math.max(0, WAVE_BREACH_SLAM - lead);
    }

    public boolean hasWaveBreachSlammed() {
        return this.waveBreachTicksLeft > 0
                && this.waveBreachTicksLeft <= WAVE_BREACH_DURATION - WAVE_BREACH_SLAM;
    }

    public float getWaveBreachAge(float partialTick) {
        if (this.waveBreachAge < 0f || this.waveBreachAgePrev < 0f) return -1f;
        return Mth.lerp(partialTick, this.waveBreachAgePrev, this.waveBreachAge);
    }

    public float getWaveSpinYaw(float partialTick) {
        if (this.waveBreachAge < 0f || this.waveBreachAgePrev < 0f) return 0f;
        float elapsed = Mth.lerp(partialTick, this.waveBreachAgePrev, this.waveBreachAge);
        float t = Mth.clamp(elapsed / WAVE_BREACH_DURATION, 0f, 1f);
        float remaining = 1f - t;
        float eased = 1f - remaining * remaining * remaining;
        return WAVE_SPIN_TURN * eased;
    }

    private void tickWaveBreach() {
        this.waveBreachAgePrev = this.waveBreachAge;

        int id = this.entityData.get(WAVE_BREACH_ID);
        if (this.lastSeenBreachId == Integer.MIN_VALUE) {
            this.lastSeenBreachId = id;
        } else if (id != this.lastSeenBreachId) {
            this.lastSeenBreachId = id;
            this.waveBreachAge = 0f;
            this.waveBreachAgePrev = 0f;
        } else if (this.waveBreachAge >= 0f) {
            this.waveBreachAge += 1f;
            if (this.waveBreachAge >= WAVE_BREACH_DURATION) this.waveBreachAge = -1f;
        }

        if (this.level().isClientSide() || this.waveBreachTicksLeft <= 0) return;
        if (isWaveBreachFlickTick()) startTailFlick();
        this.waveBreachTicksLeft--;
    }

    private void tickWaveCharge() {
        this.waveChargeBlendPrev = this.waveChargeBlend;
        float target = this.isWaveCharging() ? 1f : 0f;
        this.waveChargeBlend += (target - this.waveChargeBlend)
                * (target > this.waveChargeBlend ? WAVE_CHARGE_RISE : WAVE_CHARGE_FALL);
        if (this.waveChargeBlend < 0.001f) this.waveChargeBlend = 0f;
    }

    public static final int FLICK_ANIM_WINDUP = 7;
    public static final int FLICK_ANIM_SNAP = 4;
    public static final int FLICK_ANIM_RECOVER = 9;
    public static final int FLICK_ANIM_DURATION =
            FLICK_ANIM_WINDUP + FLICK_ANIM_SNAP + FLICK_ANIM_RECOVER;

    private static final EntityDataAccessor<Integer> TAIL_FLICK_ID =
            SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.INT);

    private int tailFlickTicksLeft = 0;

    private float tailFlickAge = -1f;
    private float tailFlickAgePrev = -1f;
    private int lastSeenFlickId = Integer.MIN_VALUE;

    public boolean isTailFlicking() {
        return this.level().isClientSide() ? this.tailFlickAge >= 0f : this.tailFlickTicksLeft > 0;
    }

    public void startTailFlick() {
        if (this.level().isClientSide()) return;
        this.tailFlickTicksLeft = FLICK_ANIM_DURATION;
        this.entityData.set(TAIL_FLICK_ID, this.entityData.get(TAIL_FLICK_ID) + 1);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                net.minecraft.sounds.SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.NEUTRAL, 1.4f, 0.7f);
    }

    public boolean isTailFlickImpact() {
        return this.tailFlickTicksLeft
                == FLICK_ANIM_DURATION - (FLICK_ANIM_WINDUP + FLICK_ANIM_SNAP / 2);
    }

    public float getTailFlickAge(float partialTick) {
        if (this.tailFlickAge < 0f || this.tailFlickAgePrev < 0f) return -1f;
        return Mth.lerp(partialTick, this.tailFlickAgePrev, this.tailFlickAge);
    }

    private void tickTailFlick() {
        this.tailFlickAgePrev = this.tailFlickAge;

        int id = this.entityData.get(TAIL_FLICK_ID);
        if (this.lastSeenFlickId == Integer.MIN_VALUE) {
            this.lastSeenFlickId = id;
        } else if (id != this.lastSeenFlickId) {
            this.lastSeenFlickId = id;
            this.tailFlickAge = 0f;
            this.tailFlickAgePrev = 0f;
        } else if (this.tailFlickAge >= 0f) {
            this.tailFlickAge += 1f;
            if (this.tailFlickAge >= FLICK_ANIM_DURATION) this.tailFlickAge = -1f;
        }

        if (!this.level().isClientSide() && this.tailFlickTicksLeft > 0) this.tailFlickTicksLeft--;
    }

    public static final int FLOP_SLAM_DURATION = 12;

    private static final EntityDataAccessor<Integer> FLOP_SLAM_ID =
            SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.INT);

    private float flopSlamAge = -1f;
    private float flopSlamAgePrev = -1f;
    private int lastSeenSlamId = Integer.MIN_VALUE;

    private static final double FLOP_CRUSH_RADIUS = 1.1;

    private static final float FLOP_CRUSH_RATIO = 0.45f;

    public float getFlopSlamAge(float partialTick) {
        if (this.flopSlamAge < 0f || this.flopSlamAgePrev < 0f) return -1f;
        return Mth.lerp(partialTick, this.flopSlamAgePrev, this.flopSlamAge);
    }

    private void tickFlopSlam() {
        this.flopSlamAgePrev = this.flopSlamAge;

        int id = this.entityData.get(FLOP_SLAM_ID);
        if (this.lastSeenSlamId == Integer.MIN_VALUE) {
            this.lastSeenSlamId = id;
        } else if (id != this.lastSeenSlamId) {
            this.lastSeenSlamId = id;
            this.flopSlamAge = 0f;
            this.flopSlamAgePrev = 0f;
        } else if (this.flopSlamAge >= 0f) {
            this.flopSlamAge += 1f;
            if (this.flopSlamAge >= FLOP_SLAM_DURATION) this.flopSlamAge = -1f;
        }
    }

    private void crushOnFlopSlam() {
        if (this.level().isClientSide()) return;

        this.entityData.set(FLOP_SLAM_ID, this.entityData.get(FLOP_SLAM_ID) + 1);

        AABB crushBox = this.getBoundingBox().inflate(FLOP_CRUSH_RADIUS, 0.6, FLOP_CRUSH_RADIUS);
        float damage = this.getDamage() * FLOP_CRUSH_RATIO;

        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, crushBox)) {
            if (victim == this || this.hasPassenger(victim) || this.isAlliedTo(victim)) continue;
            if (victim instanceof Player player && (player.isCreative() || player.isSpectator())) continue;

            victim.invulnerableTime = 0;
            victim.hurt(this.damageSources().mobAttack(this), damage);

            Vec3 push = victim.position().subtract(this.position());
            push = push.lengthSqr() > 1.0E-4 ? push.normalize() : this.getLookAngle();
            victim.setDeltaMovement(victim.getDeltaMovement().add(push.x * 0.45, 0.32, push.z * 0.45));
            victim.hurtMarked = true;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            BlockPos under = this.blockPosition().below();
            BlockState ground = this.level().getBlockState(under);
            if (!ground.isAir()) {
                serverLevel.sendParticles(
                        new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, ground),
                        this.getX(), this.getBoundingBox().minY + 0.1, this.getZ(),
                        26, this.getBbWidth() * 0.5, 0.08, this.getBbWidth() * 0.5, 0.18);
            }
        }
    }

    public static boolean isThinIce(BlockState state) {
        return state.is(net.minecraft.world.level.block.Blocks.ICE)
                || state.is(net.minecraft.world.level.block.Blocks.FROSTED_ICE);
    }

    private void spawnSpyhopSpray(double surfaceY) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        double radius = this.getBbWidth() * 0.45;
        serverLevel.sendParticles(ParticleTypes.SPLASH,
                this.getX(), surfaceY, this.getZ(), 6, radius, 0.05, radius, 0.02);
        serverLevel.sendParticles(ParticleTypes.BUBBLE,
                this.getX(), surfaceY - 0.4, this.getZ(), 3, radius, 0.2, radius, 0.01);
    }

    public boolean isBeached() {
        return this.entityData.get(IS_BEACHED);
    }

    public void setBeached(boolean beached) {
        this.entityData.set(IS_BEACHED, beached);
    }

    public OrcaBehaviorHandler getOrcaBehaviorHandler() {
        return this.orcaBehaviorHandler;
    }

    public int getPackRole() {
        return this.entityData.get(PACK_ROLE);
    }

    public void setPackRole(int role) {
        this.entityData.set(PACK_ROLE, role);
    }

    public @Nullable OrcaEntity getPackLeader() {
        return this.packLeader;
    }

    public void setPackLeader(@Nullable OrcaEntity leader) {
        this.packLeader = leader;
    }

    protected PathNavigation createNavigation(Level worldIn) {
        return new SwimmerJumpPathNavigator(this, worldIn);
    }

    private static boolean isReachableFromWater(LivingEntity target) {
        if (target.isInWater()) return true;
        if (target.getVehicle() instanceof Boat) return true;
        BlockPos underfoot = BlockPos.containing(target.getX(), target.getY() - 0.1, target.getZ());
        return target.level().getFluidState(underfoot).is(net.minecraft.tags.FluidTags.WATER);
    }

    public static final int ENTITY_COLOR = 0x28313e;

    @Override
    public int getEntityColor() {
        return ENTITY_COLOR;
    }

    @Override
    public float getTheoreticalScale() {
        return 18;
    }

    @Override
    public double getTamingExperience() {
        return TAMING_EXPERIENCE;
    }

    @Override
    public OWEntityConfig.Archetypes getArchetype() {
        return OWEntityConfig.Archetypes.BERSERKER;
    }

    @Override
    public OWEntityConfig.Diet getDiet() {
        return OWEntityConfig.Diet.CARNIVOROUS;
    }

    @Override
    public OWEntityConfig.Temperament getTemperament() {
        return OWEntityConfig.Temperament.AGGRESSIVE;
    }

    @Override
    public List<Class<?>> getFavoriteTargets() {
        return net.tiew.operationWild.entity.config.OWTargetLists.ORCA;
    }

    @Override
    public float vehicleRunSpeedMultiplier() {
        return 3f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 2f;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return 1f;
    }

    @Override
    public float vehicleWaterSpeedDivider() {
        return 0.5f;
    }

    @Override
    public boolean canIncreasesSpeedDuringSprint() {
        return false;
    }

    @Override
    public boolean isChangeSpeedDuringCombo() {
        return false;
    }

    @Override
    public Item acceptSaddle() {
        return OWItems.ORCA_SADDLE.get();
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.ORCA_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return 315f;
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 1f * (1 + ((float) this.getLevel() / 50));
    }

    @Override
    public boolean preferRawMeat() {
        return true;
    }

    @Override
    public boolean preferCookedMeat() {
        return false;
    }

    @Override
    public boolean preferVegetables() {
        return false;
    }

    @Override
    public boolean riderCameraFollowsBodyTilt() {
        return true;
    }

    @Override
    public float getRotationSpeed() {
        return 0.075f;
    }

    @Override
    public float getScale() {
        return super.getScale() <= 0 ? 1f : super.getScale();
    }

    @Override
    public int getMaxAirSupply() {
        return 300;
    }

    @Override
    protected int increaseAirSupply(int currentAir) {
        return currentAir;
    }

    @Override
    public int getMaxDepth() {
        return this.isTame() ? 65 : 22;
    }

    @Override
    public float getSwimSpeed() {
        return this.getSpeed() * 10;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.ORCA.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.CROCODILE_FOOD);
    }

    private static final float ORCA_VOICE_PITCH = 0.55f;

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * ORCA_VOICE_PITCH;
    }

    protected @Nullable SoundEvent getAmbientSound() {
        if (isNapping()) return null;
        if (!RANDOM(3)) return null;
        return this.isInWater()
                ? net.minecraft.sounds.SoundEvents.DOLPHIN_AMBIENT_WATER
                : net.minecraft.sounds.SoundEvents.DOLPHIN_AMBIENT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return net.minecraft.sounds.SoundEvents.DOLPHIN_DEATH;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return net.minecraft.sounds.SoundEvents.DOLPHIN_HURT;
    }

    private long lastStepSoundMs = 0L;

    @Override
    public void playStepSound(BlockPos blockPos, BlockState blockState) {
    }

    public void onLeftFootDown() {
        playStepSoundFromAnimation(0.85f);
    }

    public void onRightFootDown() {
        playStepSoundFromAnimation(1.05f);
    }

    private void playStepSoundFromAnimation(float pitchMod) {
        if (!this.onGround()) return;
        if (this.isInWater()) return;
        if (this.isFlopping()) return;
        if (this.getDeltaMovement().horizontalDistanceSqr() < 0.0001) return;
        long now = System.currentTimeMillis();
        if (now - lastStepSoundMs < 150L) return;
        lastStepSoundMs = now;

        var pos = this.blockPosition();
        BlockState blockState = this.level().getBlockState(pos);
        if (blockState.isAir()) blockState = this.level().getBlockState(pos.below());

        net.minecraft.world.level.block.SoundType sound = blockState.getSoundType();
        this.level().playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                sound.getStepSound(),
                this.getSoundSource(),
                sound.getVolume() * 0.65f,
                sound.getPitch() * pitchMod * (0.85f + this.random.nextFloat() * 0.3f)
        );
    }

    @Override
    protected double getBaseRiderYOffset() {
        return this.getBbHeight() * 0.75 * this.getScale();
    }

    protected double getBaseRiderYOffset(int idx) {
        return this.getBbHeight() * 0.75 * this.getScale();
    }

    @Override
    protected float getRiderAnimYOffset() {
        return -bodyAnimY / 16.0f * this.getScale();
    }

    @Override
    public void travel(Vec3 vec3) {
        super.travel(vec3);
    }

    public void tick() {
        super.tick();

        boolean wild = !this.isTame();
        createCombo((int) (28 / comboSpeedMultiplier), (int) (18 / comboSpeedMultiplier),
                OWSounds.CROCODILE_MOUTH_CRUSH.get(),
                wild ? WILD_BITE_WIDTH : 6.5,
                wild ? WILD_BITE_HEIGHT : 5,
                wild ? WILD_BITE_REACH : 4,
                false, 0.5f);
        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (!this.level().isClientSide()) {
            tickBigMouth();
            tickSlipstream();
        }

        if (this.level().isClientSide()) {
            setupAnimationState();
            tickBarrelRoll();
        }
        tickSpyhop();
        tickWaveCharge();
        tickWaveBreach();
        tickWaterFx();
        tickSpeedGuard();
        tickDepthCeiling();
        if (!this.level().isClientSide() && this.isCombo() && strikesAreSuppressed()) {
            this.setCombo(false, 0);
        }
        tickDisinterest();
        tickFlopSlam();
        tickTailFlick();
        if (this.isInResurrection()) this.setSleeping(true);

        if (!this.level().isClientSide() && false) {
            Vec3 currentPos = this.position();
            boolean isMovingHorizontally = lastPitchCheckPos != null
                    && (Math.pow(currentPos.x - lastPitchCheckPos.x, 2)
                    + Math.pow(currentPos.z - lastPitchCheckPos.z, 2)) > 1e-6;
            lastPitchCheckPos = currentPos;

            if (this.isTame() && this.isVehicle() && !this.isSitting() && !this.isBaby() && this.isInWater()) {
                LivingEntity rider = this.getControllingPassenger();
                if (rider != null && isMovingHorizontally) {
                    float target = Mth.clamp(rider.getXRot(), -45f, 45f);
                    float smoothed = Mth.clamp(Mth.lerp(0.15f, this.entityData.get(RIDER_CONTROL_PITCH), target), -45f, 45f);
                    this.entityData.set(RIDER_CONTROL_PITCH, smoothed);
                }
            } else {
                float current = this.entityData.get(RIDER_CONTROL_PITCH);
                if (Math.abs(current) > 0.05f) {
                    this.entityData.set(RIDER_CONTROL_PITCH, current * 0.95f);
                } else if (current != 0f) {
                    this.entityData.set(RIDER_CONTROL_PITCH, 0f);
                }
            }
        }


        if (!this.level().isClientSide()) {
            if (this.dashTicksLeft > 0) {
                if (this.position().distanceToSqr(this.dashStart)
                        > this.dashMaxRange * this.dashMaxRange) {
                    this.dashTicksLeft = 0;
                    this.entityData.set(IS_DASHING, false);
                    return;
                }

                float t = this.dashTicksLeft / (float) this.dashDuration;
                float speed = this.dashPeak * (t * t * t);

                applyDashContactDamage();

                if (this.getControllingPassenger() == null) {
                    turnTowards(this.dashDirection);
                }

                Entity rider = this.getFirstPassenger();
                if (rider instanceof Player player && player.zza > 0 && this.dashTicksLeft <= 15) {
                    this.dashTicksLeft = 0;
                    this.entityData.set(IS_DASHING, false);
                    return;
                }

                Vec3 current = this.getDeltaMovement();
                if (speed > 0.08f) {
                    if (this.isInWater()) {
                        this.setDeltaMovement(this.dashDirection.scale(speed));
                    } else {
                        this.setDeltaMovement(
                                this.dashDirection.x * speed * AIR_DASH_DRIVE,
                                current.y,
                                this.dashDirection.z * speed * AIR_DASH_DRIVE
                        );
                    }
                }

                this.dashTicksLeft--;

                if (this.dashTicksLeft == 0) {
                    this.entityData.set(IS_DASHING, false);
                }
            }
        }

        handleGoldVariantEffects();
    }

    @Override
    public void aiStep() {
        mirrorRiddenDeltaMovement();
        super.aiStep();

        boolean inWater = this.isInWater();
        if (this.wasInWaterLastTick && !inWater && this.isBreaching()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(BREACH_EXIT_DAMPING));
        }
        this.wasInWaterLastTick = inWater;

        if (!this.isInWater()) {
            boolean onFloor = isFlopGrounded();

            if (this.isDashing()) breachTicks = BREACH_GRACE_TICKS;
            else if (breachTicks > 0 && !onFloor) breachTicks--;
            else breachTicks = 0;

            boolean grounded = onFloor && !this.isBreaching();

            if (grounded) {
                flopGroundTicks = FLOP_GROUND_GRACE_TICKS;
                flopHopTicks = 0;
            } else {
                if (flopGroundTicks > 0) flopGroundTicks--;
                if (flopHopTicks > 0) flopHopTicks--;
            }
            if (flopHopCooldown > 0) flopHopCooldown--;

            if (this.isFlopping()) {
                if (grounded && flopHopCooldown == 0) {
                    Vec3 escape = this.getControllingPassenger() == null ? cachedWaterDirection() : null;
                    boolean seeking = escape != null;

                    this.flopHopTicks = FLOP_HOP_AIR_TICKS;
                    this.flopHopCooldown = FLOP_HOP_INTERVAL;

                    if (this.isControlledByLocalInstance()) {
                        Vec3 hop = seeking ? escape.scale(FLOP_SEEK_DRIVE) : flopHopDrive();
                        if (this.getControllingPassenger() == null) {
                            turnTowards(hop, FLOP_TURN_STEP);
                        }
                        this.setDeltaMovement(hop.x, FLOP_VERTICAL_IMPULSE, hop.z);
                        this.hasImpulse = true;
                    }

                    if (!this.level().isClientSide()) {
                        this.playSound(net.minecraft.sounds.SoundEvents.COD_FLOP,
                                this.getSoundVolume() * FLOP_SOUND_VOLUME,
                                this.getVoicePitch() * FLOP_SOUND_PITCH);
                        crushOnFlopSlam();
                    }
                }

                this.setXRot(0.0f);
                this.xRotO = 0.0f;
            }

            int air = this.getAirSupply() - AIR_LOSS_OUT_OF_WATER;
            if (air > 0) {
                this.setAirSupply(air);
                this.dryOutTicks = 0;
            } else {
                this.setAirSupply(0);
                if (++this.dryOutTicks >= DRY_OUT_DAMAGE_INTERVAL) {
                    this.dryOutTicks = 0;
                    this.hurt(this.damageSources().dryOut(), DRY_OUT_DAMAGE);
                }
            }
        } else {
            breachTicks = 0;
            flopGroundTicks = 0;
            flopHopTicks = 0;
            flopHopCooldown = 0;
            dryOutTicks = 0;
            if (this.getAirSupply() < this.getMaxAirSupply()) {
                this.setAirSupply(Math.min(this.getAirSupply() + AIR_GAIN_IN_WATER, this.getMaxAirSupply()));
            }
            this.setXRot(0.0f);
            this.xRotO = 0.0f;
        }
    }

    private static final int BREACH_GRACE_TICKS = 45;
    private int breachTicks = 0;

    private static final double BREACH_EXIT_DAMPING = 0.55;
    private static final double AIR_DASH_DRIVE = 0.45;

    private boolean wasInWaterLastTick = false;

    public static final int AIR_LOSS_OUT_OF_WATER = 2;
    private static final int AIR_GAIN_IN_WATER = 8;

    private static final int DRY_OUT_DAMAGE_INTERVAL = 20;
    private static final float DRY_OUT_DAMAGE = 2.0f;

    private int dryOutTicks = 0;

    @Override
    protected boolean isBreaching() {
        return this.isDashing() || breachTicks > 0 || this.isWaveBreaching();
    }

    private void mirrorRiddenDeltaMovement() {
        if (this.level().isClientSide()) return;
        if (!(this.getControllingPassenger() instanceof Player)) return;

        this.setDeltaMovement(this.getX() - this.xOld, this.getY() - this.yOld, this.getZ() - this.zOld);
    }

    private static final float FLOP_HORIZONTAL_IMPULSE = 0.05f;
    private static final float FLOP_VERTICAL_IMPULSE = 0.4f;
    private static final float FLOP_SOUND_PITCH = 0.5f;
    private static final float FLOP_SOUND_VOLUME = 0.55f;

    private static final int FLOP_GROUND_GRACE_TICKS = 20;
    private static final int FLOP_HOP_AIR_TICKS = 14;
    private static final int FLOP_HOP_INTERVAL = 18;
    private static final double FLOP_GROUND_PROBE = 0.1;

    private static final double FLOP_HOP_DRIVE = 0.22;
    private static final double FLOP_HOP_BACKWARD_RATIO = 0.4;

    public static final float FLOP_BODY_ROLL = 90.0f;
    public static final float FLOP_SIDE_OFFSET = 1.0f;
    public static final float FLOP_GROUND_LIFT = 0.75f;

    private int flopGroundTicks = 0;
    private int flopHopTicks = 0;
    private int flopHopCooldown = 0;

    private boolean isFlopGrounded() {
        AABB box = this.getBoundingBox();
        AABB probe = new AABB(box.minX, box.minY - FLOP_GROUND_PROBE, box.minZ, box.maxX, box.minY, box.maxZ);
        return !this.level().noCollision(this, probe);
    }

    public boolean isAground() {
        if (!this.isInWater()) return true;
        return this.onGround() && !this.isUnderWater();
    }

    public boolean isFlopping() {
        return !this.isInWater() && this.flopGroundTicks > 0;
    }

    @Override
    protected boolean keepsVerticalImpulseOutOfWater() {
        return this.flopHopTicks > 0;
    }

    private Vec3 flopHopDrive() {
        if (this.getControllingPassenger() instanceof Player player) {
            float strafe = player.xxa;
            float forward = player.zza;
            if (strafe == 0f && forward == 0f) return Vec3.ZERO;

            Vec3 input = new Vec3(strafe, 0, forward).normalize()
                    .scale(forward < 0 ? FLOP_HOP_DRIVE * FLOP_HOP_BACKWARD_RATIO : FLOP_HOP_DRIVE);

            float yawRad = this.getYRot() * Mth.DEG_TO_RAD;
            float sin = Mth.sin(yawRad);
            float cos = Mth.cos(yawRad);
            return new Vec3(input.x * cos - input.z * sin, 0, input.z * cos + input.x * sin);
        }

        return new Vec3(
                (this.random.nextFloat() * 2.0F - 1.0F) * FLOP_HORIZONTAL_IMPULSE, 0,
                (this.random.nextFloat() * 2.0F - 1.0F) * FLOP_HORIZONTAL_IMPULSE);
    }

    private static final int FLOP_WATER_SEEK_RADIUS = 14;

    private static final double FLOP_SEEK_DRIVE = 0.16;

    private static final float FLOP_TURN_STEP = 0.34f;

    private Vec3 waterDirCache = null;
    private int waterDirCacheAge = 0;

    private @Nullable Vec3 cachedWaterDirection() {
        if (this.waterDirCache != null && --this.waterDirCacheAge > 0) return this.waterDirCache;
        this.waterDirCache = nearestWaterDirection();
        this.waterDirCacheAge = WATER_DIR_CACHE_TICKS;
        return this.waterDirCache;
    }

    private static final int WATER_DIR_CACHE_TICKS = 40;

    private static final float DRYING_OUT_FRACTION = 0.55f;

    public boolean isDryingOut() {
        return !this.isInWater() && this.getAirSupply() < this.getMaxAirSupply() * DRYING_OUT_FRACTION;
    }

    private @Nullable Vec3 nearestWaterDirection() {
        BlockPos origin = this.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (int dx = -FLOP_WATER_SEEK_RADIUS; dx <= FLOP_WATER_SEEK_RADIUS; dx++) {
            for (int dz = -FLOP_WATER_SEEK_RADIUS; dz <= FLOP_WATER_SEEK_RADIUS; dz++) {
                for (int dy = -8; dy <= 3; dy++) {
                    cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (!this.level().getFluidState(cursor).is(net.minecraft.tags.FluidTags.WATER)) continue;
                    double dist = cursor.distSqr(origin);
                    if (dist < bestDist) {
                        bestDist = dist;
                        best = cursor.immutable();
                    }
                }
            }
        }
        if (best == null) return null;

        Vec3 toward = new Vec3(best.getX() + 0.5 - this.getX(), 0.0, best.getZ() + 0.5 - this.getZ());
        return toward.lengthSqr() < 1.0E-4 ? null : toward.normalize();
    }

    @Override
    public float getRiddenSpeedVehicle(Player player) {
        if (this.isFlopping()) {
            this.resetRiddenSpeed();
            return 0f;
        }
        return super.getRiddenSpeedVehicle(player);
    }

    public Vec3 getDashAimDirection() {
        LivingEntity rider = this.getControllingPassenger();
        float pitch = rider != null ? this.getRiddenRotation(rider).x : this.getTargetPitch();
        pitch = Mth.clamp(pitch, -75f, 75f);

        Vec3 aim = Vec3.directionFromRotation(pitch, this.getYRot());
        return aim.lengthSqr() < 1.0E-6 ? new Vec3(0, 0, 1) : aim.normalize();
    }

    @Override
    protected float comboTrackingDegreesPerTick() {
        return this.isVehicle() ? 360f : 14f;
    }

    private static final double WILD_BITE_WIDTH = 3.6;
    private static final double WILD_BITE_HEIGHT = 2.6;
    private static final double WILD_BITE_REACH = 2.4;


    private static final double BITE_DEPTH_TOLERANCE = 1.6;

    private static final double WILD_STANDOFF_DISTANCE = 3.0;

    private static final int WILD_DISENGAGE_CHANCE = 55;

    private static final int WILD_DISENGAGE_TICKS_MIN = 45;
    private static final int WILD_DISENGAGE_SPREAD = 40;

    private static final double WILD_DISENGAGE_DISTANCE = 13.0;

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide()) {
            if (hasSwallowed()) releaseSwallowed(true);
            clearSlipstream();
        }

        super.die(damageSource);

        if (this.isSaddled()) {
            this.spawnAtLocation(acceptSaddle());
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (!hurt || this.level().isClientSide() || this.isTame() || this.isBaby()) return hurt;

        this.disinterestTicks = 0;
        if (source.getEntity() instanceof LivingEntity attacker
                && attacker != this
                && !this.isAlliedTo(attacker)) {
            this.forceSetTarget(attacker);
        }
        return hurt;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (this.isNapping() || this.isBaby()) {
            return;
        }

        super.setTarget(target);
    }

    private boolean playingWithPrey = false;

    public boolean isPlayingWithPrey() {
        return this.playingWithPrey;
    }

    public void setPlayingWithPrey(boolean value) {
        this.playingWithPrey = value;
    }

    private boolean strikesAreSuppressed() {
        if (this.isTame()) return false;
        if (this.playingWithPrey) return true;
        return this.isBeached() || this.isAground();
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (strikesAreSuppressed()) return false;
        return super.doHurtTarget(entity);
    }

    @Override
    public void attackEntitiesInFront(float attackDamage, SoundEvent sound, double width,
                                      double height, double reach, float knockbackMultiplier) {
        if (strikesAreSuppressed()) return;
        super.attackEntitiesInFront(attackDamage, sound, width, height, reach, knockbackMultiplier);
        reinforceBiteImpact(sound, reach);
    }

    private void reinforceBiteImpact(SoundEvent sound, double reach) {
        if (this.level().isClientSide() || sound == null) return;

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                sound, SoundSource.HOSTILE, 1.6f, 0.5f);

        if (this.lastAttackHitEntities.isEmpty()) return;
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        Vec3 forward = Vec3.directionFromRotation(0f, this.getYRot());
        serverLevel.sendParticles(ParticleTypes.SPLASH,
                this.getX() + forward.x * reach,
                this.getY() + this.getBbHeight() * 0.6,
                this.getZ() + forward.z * reach,
                18, 0.5, 0.35, 0.5, 0.25);

        for (LivingEntity victim : this.lastAttackHitEntities) {
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                    victim.getX(), victim.getY() + victim.getBbHeight() * 0.5, victim.getZ(),
                    6, 0.25, 0.25, 0.25, 0.02);
        }
    }

    @Override
    public void attackEntitiesInFrontSimple(float attackDamage, SoundEvent sound, double width,
                                            double height, double reach, float knockbackMultiplier) {
        if (strikesAreSuppressed()) return;
        super.attackEntitiesInFrontSimple(attackDamage, sound, width, height, reach, knockbackMultiplier);
    }

    @Override
    public void hurtAfterCombo(LivingEntity entity, int comboAttack) {

    }

    private float getRushDamage() {
        return this.isTame()
                ? this.getDamage()
                : this.getDamage() * OWAttacksConstants.Orca.TIDAL_RUSH_WILD_DAMAGE_MULTIPLIER;
    }

    private final java.util.Set<java.util.UUID> dashHits = new java.util.HashSet<>();

    private void applyDashContactDamage() {
        if (strikesAreSuppressed()) return;

        double travelX = this.getX() - this.xOld;
        double travelY = this.getY() - this.yOld;
        double travelZ = this.getZ() - this.zOld;

        AABB sweep = this.getBoundingBox()
                .expandTowards(-travelX, -travelY, -travelZ)
                .inflate(0.6);

        Vec3 push = this.dashDirection.lengthSqr() > 1.0E-6 ? this.dashDirection : this.getLookAngle();

        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, sweep)) {
            if (target == this || this.hasPassenger(target) || this.isAlliedTo(target)) continue;
            if (!this.dashHits.add(target.getUUID())) continue;

            target.hurt(this.damageSources().mobAttack(this), this.getRushDamage());
            double shove = this.isTame() ? 1.2 : 0.55;
            target.setDeltaMovement(target.getDeltaMovement()
                    .add(push.x * shove, this.isTame() ? 0.25 : 0.14, push.z * shove));
        }
    }

    public void performOrcaDash() {
        float cost = OWAttacksConstants.Orca.TIDAL_RUSH_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            return;
        }
        setVitalEnergy(getVitalEnergy() + cost);
        launchDash(getDashAimDirection());
    }

    private void launchDash(Vec3 direction) {
        launchDash(direction, RIDDEN_DASH_PEAK, RIDDEN_DASH_TICKS, RIDDEN_DASH_RANGE);
    }

    private void launchDash(Vec3 direction, float peak, int ticks) {
        launchDash(direction, peak, ticks, RIDDEN_DASH_RANGE);
    }

    private void launchDash(Vec3 direction, float peak, int ticks, double maxRange) {
        this.dashDirection = direction;
        this.dashPeak = peak;
        this.dashDuration = ticks;
        this.dashStart = this.position();
        this.dashMaxRange = maxRange;

        this.setDeltaMovement(this.dashDirection.scale(peak));

        this.entityData.set(IS_DASHING, true);
        this.dashTicksLeft = ticks;

        this.dashHits.clear();

        if (this.level() instanceof ServerLevel serverLevel) {
            Vec3 pos = this.position();
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    pos.x, pos.y + 0.5, pos.z, 35, 1.1, 0.4, 1.1, 0.25);
            serverLevel.sendParticles(ParticleTypes.BUBBLE,
                    pos.x, pos.y + 0.2, pos.z, 25, 0.9, 0.3, 0.9, 0.08);
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                net.minecraft.sounds.SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.AMBIENT, 2.5f, 0.8f);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                net.minecraft.sounds.SoundEvents.DOLPHIN_SPLASH, SoundSource.AMBIENT, 1.8f, 1.0f);
    }


    private static final double MOUTH_REACH = 6.0;
    private static final double MOUTH_CONE = 0.35;
    public static final int MOUTH_ANIM_WINDUP = 6;
    public static final int MOUTH_ANIM_TENSE = 4;
    public static final int MOUTH_ANIM_STRIKE = 4;
    public static final int MOUTH_ANIM_RECOVER = 8;

    private static final int MOUTH_LUNGE_DURATION =
            MOUTH_ANIM_WINDUP + MOUTH_ANIM_TENSE + MOUTH_ANIM_STRIKE + MOUTH_ANIM_RECOVER;

    public static final int MOUTH_SPIT_HEAVE = 5;
    public static final int MOUTH_SPIT_BURST = 4;
    public static final int MOUTH_SPIT_RECOVER = 8;

    private static final int MOUTH_SPIT_DURATION =
            MOUTH_SPIT_HEAVE + MOUTH_SPIT_BURST + MOUTH_SPIT_RECOVER;
    public static final int MOUTH_HOLD_ALLY_TICKS = 1000;
    public static final int MOUTH_HOLD_ENEMY_TICKS = 200;
    private static final int MOUTH_DAMAGE_INTERVAL = 20;
    private static final float MOUTH_BITE_RATIO = 0.20f;

    private static final double MOUTH_HOLD_FORWARD = 2.1;
    private static final double MOUTH_HOLD_HEIGHT = 0.25;

    private int mouthHoldTicks = 0;
    private boolean mouthTargetWasInvisible = false;
    private int pendingPreyId = -1;
    private boolean mouthTargetHadNoAi = false;

    @Override
    public LivingEntity getControllingPassenger() {
        LivingEntity swallowed = getSwallowedTarget();
        if (swallowed != null && this.getFirstPassenger() == swallowed) return null;
        return super.getControllingPassenger();
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!this.level().isClientSide()) {
            if (hasSwallowed()) releaseSwallowed(false);
            clearSlipstream();
        }
        super.remove(reason);
    }

    public boolean activateBigMouth() {
        if (this.level().isClientSide()) return false;

        if (hasSwallowed()) {
            beginSpit();
            return true;
        }

        LivingEntity prey = findMouthTarget();
        if (prey == null) {
            this.entityData.set(MOUTH_LUNGE_TICKS, MOUTH_LUNGE_DURATION);
            this.level().playSound(null, getX(), getY(), getZ(),
                    net.minecraft.sounds.SoundEvents.DOLPHIN_ATTACK, SoundSource.HOSTILE, 1.1f, 0.74f);
            return false;
        }

        this.pendingPreyId = prey.getId();
        this.entityData.set(MOUTH_LUNGE_TICKS, MOUTH_LUNGE_DURATION);

        setOrcaUltimateKillCount(0);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.BUBBLE,
                    getX(), getY() + 0.6, getZ(), 18, 1.0, 0.5, 1.0, 0.06);
        }
        return true;
    }

    private void closeMouthOnPrey() {
        int reserved = this.pendingPreyId;
        this.pendingPreyId = -1;
        if (reserved == -1) return;

        if (!(this.level().getEntity(reserved) instanceof LivingEntity prey)
                || !canSwallow(prey)
                || prey.distanceToSqr(this) > (MOUTH_REACH + 3.0) * (MOUTH_REACH + 3.0)) {
            this.level().playSound(null, getX(), getY(), getZ(),
                    net.minecraft.sounds.SoundEvents.DOLPHIN_ATTACK, SoundSource.HOSTILE, 1.1f, 0.74f);
            return;
        }

        this.entityData.set(SWALLOWED_TARGET_ID, prey.getId());
        this.mouthHoldTicks = (this.isAlliedTo(prey) || this.isTameGrabAlly(prey))
                ? MOUTH_HOLD_ALLY_TICKS : MOUTH_HOLD_ENEMY_TICKS;

        this.mouthTargetWasInvisible = prey.isInvisible();
        prey.setInvisible(true);
        if (prey instanceof Mob preyMob) {
            this.mouthTargetHadNoAi = preyMob.isNoAi();
            preyMob.setNoAi(true);
        }
        prey.startRiding(this, true);

        this.level().playSound(null, getX(), getY(), getZ(),
                net.minecraft.sounds.SoundEvents.DOLPHIN_ATTACK, SoundSource.HOSTILE, 3.0f, 0.5f);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.BUBBLE,
                    getX(), getY() + 0.6, getZ(), 45, 1.4, 0.6, 1.4, 0.15);
        }
    }

    @Override
    public void setCombo(boolean isCombo, int numberOfAttacks) {
        if (isCombo && hasSwallowed()) return;
        if (isCombo && strikesAreSuppressed()) return;
        super.setCombo(isCombo, numberOfAttacks);
    }

    public boolean hasMouthTarget() {
        return findMouthTarget() != null;
    }

    private LivingEntity findMouthTarget() {
        Vec3 forward = Vec3.directionFromRotation(0, this.getYRot());
        AABB box = this.getBoundingBox().inflate(MOUTH_REACH);

        LivingEntity best = null;
        double bestDot = MOUTH_CONE;

        for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class, box, this::canSwallow)) {
            Vec3 toward = candidate.getBoundingBox().getCenter().subtract(this.getEyePosition());
            if (toward.lengthSqr() > MOUTH_REACH * MOUTH_REACH) continue;
            if (toward.lengthSqr() < 1.0E-6) continue;

            double dot = forward.dot(toward.normalize());
            if (dot > bestDot) {
                bestDot = dot;
                best = candidate;
            }
        }
        return best;
    }

    private static final float MOUTH_MAX_OW_SCALE = 10f;
    private static final float MOUTH_MAX_WIDTH = 2.0f;
    private static final float MOUTH_MAX_HEIGHT = 2.4f;

    public static boolean fitsInMouth(LivingEntity candidate) {
        if (candidate instanceof net.tiew.operationWild.entity.OWEntity owEntity) {
            return owEntity.getTheoreticalScale() <= MOUTH_MAX_OW_SCALE;
        }
        return candidate.getBbWidth() <= MOUTH_MAX_WIDTH
                && candidate.getBbHeight() <= MOUTH_MAX_HEIGHT;
    }

    public boolean canSwallow(LivingEntity candidate) {
        if (candidate == null || candidate == this) return false;
        if (!candidate.isAlive() || candidate.isRemoved()) return false;
        if (candidate instanceof Player player && (player.isCreative() || player.isSpectator())) return false;
        if (candidate.isPassenger() || !candidate.getPassengers().isEmpty()) return false;
        return isWorthSwallowing(candidate) && fitsInMouth(candidate);
    }

    public static boolean isWorthSwallowing(LivingEntity candidate) {
        if (candidate instanceof net.minecraft.world.entity.animal.AbstractFish) return false;
        if (candidate.isBaby()) return false;
        return candidate.getBbWidth() * candidate.getBbWidth() * candidate.getBbHeight()
                >= MIN_SWALLOW_VOLUME;
    }

    private static final float MIN_SWALLOW_VOLUME = 0.35f;

    public boolean hasSwallowed() {
        return this.entityData.get(SWALLOWED_TARGET_ID) != -1;
    }

    public static boolean isSwallowed(Entity entity) {
        return entity != null
                && entity.getVehicle() instanceof OrcaEntity orca
                && orca.getSwallowedTarget() == entity;
    }

    public LivingEntity getSwallowedTarget() {
        int id = this.entityData.get(SWALLOWED_TARGET_ID);
        if (id == -1) return null;
        return this.level().getEntity(id) instanceof LivingEntity living ? living : null;
    }

    public int getMouthLungeTicks() {
        return this.entityData.get(MOUTH_LUNGE_TICKS);
    }

    public int getMouthSpitTicks() {
        return this.entityData.get(MOUTH_SPIT_TICKS);
    }

    public static int getMouthSpitDuration() {
        return MOUTH_SPIT_DURATION;
    }

    public static int getMouthLungeDuration() {
        return MOUTH_LUNGE_DURATION;
    }

    public float getMouthLungeProgress() {
        int left = getMouthLungeTicks();
        return left <= 0 ? 0f : left / (float) MOUTH_LUNGE_DURATION;
    }

    public void beginSpit() {
        if (this.level().isClientSide() || !hasSwallowed()) return;
        if (this.entityData.get(MOUTH_SPIT_TICKS) > 0) return;

        this.entityData.set(MOUTH_SPIT_TICKS, MOUTH_SPIT_DURATION);
        this.level().playSound(null, getX(), getY(), getZ(),
                net.minecraft.sounds.SoundEvents.DOLPHIN_ATTACK, SoundSource.HOSTILE, 1.0f, 0.5f);
    }

    private boolean abyssalHold = false;

    public boolean isAbyssalHold() {
        return this.abyssalHold;
    }

    private int disinterestTicks = 0;

    public void setDisinterest(int ticks) {
        this.disinterestTicks = Math.max(this.disinterestTicks, ticks);
    }

    public boolean isDisinterested() {
        return this.disinterestTicks > 0;
    }

    private static final double WILD_SPEED_CAP = 1.0;

    private void tickSpeedGuard() {
        if (this.level().isClientSide() || this.isTame()) return;
        if (this.getControllingPassenger() != null) return;
        if (this.dashTicksLeft > 0 || this.isWaveCharging() || this.isWaveBreaching()) return;
        if (this.hasSwallowed() || this.isFlopping()) return;

        Vec3 mv = this.getDeltaMovement();
        double horizontalSq = mv.x * mv.x + mv.z * mv.z;
        if (horizontalSq <= WILD_SPEED_CAP * WILD_SPEED_CAP) return;

        double scale = WILD_SPEED_CAP / Math.sqrt(horizontalSq);
        this.setDeltaMovement(mv.x * scale, mv.y, mv.z * scale);
    }

    private static final double FX_MIN_SPEED = 0.06;

    private double fxLastX = Double.NaN;
    private double fxLastZ = Double.NaN;

    private static final double FX_SURFACE_BAND = 0.9;

    private void tickWaterFx() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        double dx = Double.isNaN(this.fxLastX) ? 0.0 : this.getX() - this.fxLastX;
        double dz = Double.isNaN(this.fxLastX) ? 0.0 : this.getZ() - this.fxLastZ;
        this.fxLastX = this.getX();
        this.fxLastZ = this.getZ();

        if (this.isSleeping() || this.isFlopping()) return;

        double speed = Math.sqrt(dx * dx + dz * dz);
        if (speed < FX_MIN_SPEED) return;

        double surface = surfaceYAbove();
        double back = this.getBbWidth() * 1.35;
        Vec3 forward = Vec3.directionFromRotation(0f, this.getYRot());
        double tailX = this.getX() - forward.x * back;
        double tailZ = this.getZ() - forward.z * back;
        double backTop = this.getY() + this.getBbHeight();

        if (!Double.isNaN(surface) && surface - backTop < FX_SURFACE_BAND) {
            int count = 2 + (int) Math.min(6, speed * 8);
            double spread = this.getBbWidth() * 0.4;
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    this.getX(), surface, this.getZ(), count, spread, 0.02, spread, 0.02);
            serverLevel.sendParticles(ParticleTypes.BUBBLE,
                    tailX, surface - 0.25, tailZ, count, spread * 1.3, 0.1, spread * 1.3, 0.01);
        }

        if (!Double.isNaN(surface) && backTop > surface && this.tickCount % 2 == 0) {
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    tailX, surface, tailZ, 8, 0.35, 0.15, 0.35, 0.22);
        }

        if (this.isUnderWater() && speed > 0.22 && this.tickCount % 2 == 0) {
            int count = 1 + (int) Math.min(4, speed * 4);
            serverLevel.sendParticles(ParticleTypes.BUBBLE,
                    tailX, this.getY() + this.getBbHeight() * 0.6, tailZ,
                    count, 0.28, 0.18, 0.28, 0.015);
        }
    }

    private static final double DEPTH_RECOVERY_LIFT = 0.06;

    private void tickDepthCeiling() {
        if (this.level().isClientSide() || this.isTame()) return;
        if (this.hasSwallowed() || this.isAbyssalHold()) return;
        if (this.getControllingPassenger() != null || !this.isInWater()) return;

        double depth = this.level().getSeaLevel() - this.getY();
        int limit = this.getMaxDepth();
        if (depth <= limit) return;

        double excess = Math.min(1.0, (depth - limit) / 8.0);
        Vec3 mv = this.getDeltaMovement();
        this.setDeltaMovement(mv.x, mv.y + DEPTH_RECOVERY_LIFT * excess, mv.z);
    }

    private void tickDisinterest() {
        if (this.level().isClientSide() || this.disinterestTicks <= 0) return;
        if (this.getLastHurtByMob() != null) {
            this.disinterestTicks = 0;
            return;
        }
        this.disinterestTicks--;
    }

    public void setAbyssalHold(boolean value) {
        this.abyssalHold = value;
    }

    public void holdSwallowedFor(int ticks) {
        if (this.level().isClientSide() || !hasSwallowed()) return;
        this.mouthHoldTicks = Math.max(this.mouthHoldTicks, ticks);
    }

    private static final int SEABED_SCAN_DEPTH = 128;

    public double seabedYBelow() {
        BlockPos.MutableBlockPos cursor = this.blockPosition().mutable();
        if (!this.level().getFluidState(cursor).is(net.minecraft.tags.FluidTags.WATER)) return Double.NaN;
        for (int i = 0; i < SEABED_SCAN_DEPTH; i++) {
            cursor.move(Direction.DOWN);
            if (this.level().getFluidState(cursor).is(net.minecraft.tags.FluidTags.WATER)) continue;
            return cursor.getY() + 1;
        }
        return Double.NaN;
    }

    public double waterColumnBelow() {
        double seabed = seabedYBelow();
        return Double.isNaN(seabed) ? 0.0 : Math.max(0.0, this.getY() - seabed);
    }

    public void releaseSwallowed(boolean spit) {
        LivingEntity prey = getSwallowedTarget();
        if (prey != null) {
            if (prey.getVehicle() == this) prey.stopRiding();
            if (!mouthTargetWasInvisible) prey.setInvisible(false);
            if (!mouthTargetHadNoAi && prey instanceof Mob preyMob) preyMob.setNoAi(false);

            if (spit) {
                Vec3 push = Vec3.directionFromRotation(0, this.getYRot()).scale(0.9);
                prey.setDeltaMovement(push.x, 0.35, push.z);
                prey.hurtMarked = true;
                this.level().playSound(null, getX(), getY(), getZ(),
                        net.minecraft.sounds.SoundEvents.DOLPHIN_ATTACK, SoundSource.HOSTILE, 1.5f, 0.69f);
            }
        }
        mouthTargetWasInvisible = false;
        mouthTargetHadNoAi = false;
        mouthHoldTicks = 0;
        this.entityData.set(SWALLOWED_TARGET_ID, -1);
        this.entityData.set(MOUTH_SPIT_TICKS, 0);
    }

    private void tickBigMouth() {
        int lunge = this.entityData.get(MOUTH_LUNGE_TICKS);
        if (lunge > 0) {
            this.entityData.set(MOUTH_LUNGE_TICKS, lunge - 1);

            int elapsed = MOUTH_LUNGE_DURATION - (lunge - 1);
            if (elapsed == MOUTH_ANIM_WINDUP + MOUTH_ANIM_TENSE) closeMouthOnPrey();
        } else if (this.pendingPreyId != -1) {
            this.pendingPreyId = -1;
        }

        int spit = this.entityData.get(MOUTH_SPIT_TICKS);
        if (spit > 0) {
            this.entityData.set(MOUTH_SPIT_TICKS, spit - 1);
            int spitElapsed = MOUTH_SPIT_DURATION - (spit - 1);
            if (spitElapsed == MOUTH_SPIT_HEAVE && hasSwallowed()) {
                releaseSwallowed(true);
                this.entityData.set(MOUTH_SPIT_TICKS, MOUTH_SPIT_BURST + MOUTH_SPIT_RECOVER);
            }
        }

        if (!hasSwallowed()) return;

        LivingEntity prey = getSwallowedTarget();
        if (prey == null || !prey.isAlive() || prey.isRemoved() || prey.level() != this.level()
                || (prey instanceof Player player && (player.isCreative() || player.isSpectator()))) {
            releaseSwallowed(false);
            return;
        }

        if (--mouthHoldTicks <= 0) {
            beginSpit();
            return;
        }

        prey.fallDistance = 0f;
        if (!prey.isPassenger()) prey.startRiding(this, true);
        if (prey instanceof Mob mob) mob.setTarget(null);

        boolean friendly = this.isAlliedTo(prey) || this.isTameGrabAlly(prey);

        prey.setAirSupply(prey.getMaxAirSupply());

        if (!friendly && !this.abyssalHold && this.tickCount % MOUTH_DAMAGE_INTERVAL == 0) {
            prey.invulnerableTime = 0;
            prey.hurt(this.damageSources().mobAttack(this), this.getDamage() * MOUTH_BITE_RATIO);
            this.level().playSound(null, getX(), getY(), getZ(),
                    net.minecraft.sounds.SoundEvents.DOLPHIN_ATTACK, SoundSource.HOSTILE, 0.8f, 0.5f);
        }

        if (this.tickCount % 10 == 0 && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.BUBBLE,
                    getX(), getY() + 0.8, getZ(), 4, 0.5, 0.3, 0.5, 0.02);
        }
    }

    public static final double SLIPSTREAM_RADIUS = 32.0;
    public static final double SLIPSTREAM_SPEED_BONUS = 0.15;
    public static final int SLIPSTREAM_BREATH_NUM = 7;
    public static final int SLIPSTREAM_BREATH_DEN = 27;

    private static final int SLIPSTREAM_SCAN_INTERVAL = 10;

    private static final ResourceLocation SLIPSTREAM_SPEED_MODIFIER =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "orca_slipstream_speed");

    private final java.util.Set<Integer> slipstreamTargets = new java.util.HashSet<>();

    public static int slipstreamBreathPercent() {
        return Math.round(100f * (SLIPSTREAM_BREATH_DEN / (float) (SLIPSTREAM_BREATH_DEN - SLIPSTREAM_BREATH_NUM) - 1f));
    }

    private void tickSlipstream() {
        if (!this.isTame() || this.isBaby()) {
            if (!slipstreamTargets.isEmpty()) clearSlipstream();
            return;
        }

        if (this.tickCount % SLIPSTREAM_SCAN_INTERVAL == 0) refreshSlipstreamTargets();
        if (slipstreamTargets.isEmpty()) return;

        for (int id : slipstreamTargets) {
            if (!(this.level().getEntity(id) instanceof LivingEntity ally)) continue;
            if (!ally.isInWater() || !ally.isAlive()) continue;
            if ((ally.tickCount * SLIPSTREAM_BREATH_NUM) % SLIPSTREAM_BREATH_DEN >= SLIPSTREAM_BREATH_NUM) continue;
            if (ally.getAirSupply() < ally.getMaxAirSupply()) {
                ally.setAirSupply(Math.min(ally.getMaxAirSupply(), ally.getAirSupply() + 1));
            }
        }
    }

    private void refreshSlipstreamTargets() {
        java.util.Set<Integer> previous = new java.util.HashSet<>(slipstreamTargets);
        slipstreamTargets.clear();

        AABB box = this.getBoundingBox().inflate(SLIPSTREAM_RADIUS);
        for (LivingEntity ally : this.level().getEntitiesOfClass(LivingEntity.class, box, this::isSlipstreamAlly)) {
            if (this.distanceToSqr(ally) > SLIPSTREAM_RADIUS * SLIPSTREAM_RADIUS) continue;
            if (!ally.isInWater()) continue;
            slipstreamTargets.add(ally.getId());
            applySlipstreamSpeed(ally, true);
        }

        for (int id : previous) {
            if (slipstreamTargets.contains(id)) continue;
            if (this.level().getEntity(id) instanceof LivingEntity gone) applySlipstreamSpeed(gone, false);
        }
    }

    private void applySlipstreamSpeed(LivingEntity ally, boolean active) {
        var speed = ally.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        if (active) {
            speed.addOrUpdateTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    SLIPSTREAM_SPEED_MODIFIER, SLIPSTREAM_SPEED_BONUS,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else {
            speed.removeModifier(SLIPSTREAM_SPEED_MODIFIER);
        }
    }

    private boolean isSlipstreamAlly(LivingEntity candidate) {
        if (candidate == this || !candidate.isAlive()) return false;
        if (this.isTameGrabAlly(candidate)) return true;

        if (candidate instanceof TamableAnimal pet && pet.isTame()) {
            UUID petOwner = pet.getOwnerUUID();
            if (petOwner == null) return false;
            return petOwner.equals(this.getOwnerUUID()) || this.isInMyTribe(petOwner);
        }
        return false;
    }

    private void clearSlipstream() {
        for (int id : slipstreamTargets) {
            if (this.level().getEntity(id) instanceof LivingEntity ally) applySlipstreamSpeed(ally, false);
        }
        slipstreamTargets.clear();
    }

    public int getOrcaUltimateKillCount() {
        return this.entityData.get(ULTIMATE_KILL_COUNT);
    }

    public void setOrcaUltimateKillCount(int count) {
        this.entityData.set(ULTIMATE_KILL_COUNT, count);
    }


    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        int kills = getOrcaUltimateKillCount();
        if (kills < OWAttacksConstants.Orca.BIG_MOUTH_KILLS_REQUIRED) {
            setOrcaUltimateKillCount(kills + 1);
        }
        return super.killedEntity(serverLevel, entity);
    }

    @Override
    public int arenaTerrainMask() {
        return net.tiew.operationWild.core.OWArena.Terrain.AQUATIC.bit();
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof OrcaEntity otherOrca) {
            if (otherOrca.isBaby()) {
                return true;
            }
            if (this.isTame()) {
                if (otherOrca.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherOrca.getOwnerUUID())) {
                    return true;
                }
            } else if (!otherOrca.isTame()) {
                return true;
            }
        }
        return super.isAlliedTo(entity);
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction function) {
        if (passenger == this.getSwallowedTarget()) {
            double s = this.getScale();
            double yawRad = Math.toRadians(this.yBodyRot);
            double forward = MOUTH_HOLD_FORWARD * s;

            passenger.fallDistance = 0f;
            function.accept(passenger,
                    this.getX() - Math.sin(yawRad) * forward,
                    this.getY() + MOUTH_HOLD_HEIGHT * s,
                    this.getZ() + Math.cos(yawRad) * forward);
            return;
        }

        if (!this.hasPassenger(passenger) || this.touchingUnloadedChunk()) return;

        int idx = this.getPassengers().indexOf(passenger);

        float seatZ, seatX;
        switch (idx) {
            case 1 -> {
                seatZ = -0.45f;
                seatX = 0.45f;
            }
            case 2 -> {
                seatZ = -0.45f;
                seatX = -0.45f;
            }
            default -> {
                seatZ = 1.1f;
                seatX = 0f;
            }
        }

        seatZ += SEAT_FORWARD;
        if (this.isCombo()) seatZ += COMBO_SEAT_FORWARD;

        final float s = this.getScale();
        double baseY = getBaseRiderYOffset(idx);

        float lx = (float) (seatX / s);
        float ly = (float) (MODEL_ORIGIN_Y - baseY / s);
        float lz = -(float) (seatZ / s);

        double dx = 0, dy = 0, dz = 0;
        org.joml.Matrix4f bones = this.boneMatrix;
        if (bones != null) {
            org.joml.Vector3f now = bones.transformPosition(new org.joml.Vector3f(lx, ly, lz));
            dx = now.x - (lx + REST_X / 16f);
            dy = now.y - (ly + REST_Y / 16f);
            dz = now.z - (lz + REST_Z / 16f);
        }

        double ex = dx * s;
        double ey = -dy * s;
        double ez = -dz * s;

        double localX = seatX + ex;
        double localY = baseY + ey;
        double localZ = seatZ + ez;

        if (this.isFlopping()) {
            double rolledX = localY - FLOP_SIDE_OFFSET * s;
            double rolledY = -localX + FLOP_GROUND_LIFT * s;
            localX = rolledX;
            localY = rolledY;
        }

        Vec3 seatOffset = new Vec3(localX, 0, localZ)
                .yRot((float) Math.toRadians(-this.yBodyRot));

        passenger.fallDistance = 0f;
        function.accept(passenger,
                this.getX() + seatOffset.x,
                this.getY() + localY,
                this.getZ() + seatOffset.z);

        if (idx == 0 && passenger instanceof LivingEntity living) {
            living.yBodyRot = this.yBodyRot;
        }
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() < 3;
    }

    @Override
    public boolean isControlledByLocalInstance() {
        Entity controlling = this.getControllingPassenger();
        if (controlling == null) {
            return super.isControlledByLocalInstance();
        }
        return this.getPassengers().indexOf(controlling) == 0 && super.isControlledByLocalInstance();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return super.mobInteract(player, hand);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));

            this.setVariant(chooseOrcaVariant());
            this.setInitialVariant(this.getVariant());
        }
        this.foodWanted = (int) OWUtils.generateRandomInterval(6, 11);

        if (this.isBaby()) {
            maxHealth = (float) this.getAttribute(Attributes.MAX_HEALTH).getValue();
            maxMaturation = (int) (2000 * maxHealth + 10000 * this.getDamage());
            this.setHealth(1);
            foodWanted = (int) this.getMaxHealth();
        }
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    @Override
    protected int getDefaultSkinIndex() {
        return 7;
    }

    private void handleGoldVariantEffects() {
        if (this.getVariant() == OrcaVariant.SKIN_GOLD && this.tickCount % 150 == 0) {
            OWUtils.spawnParticles(this, ParticleTypes.END_ROD, 0, 0, 0, 5, 2);
        }
    }

    private OrcaVariant chooseOrcaVariant() {
        OrcaVariant variant;
        if (chance >= 66) variant = OrcaVariant.BLACK;
        else if (chance >= 33) variant = OrcaVariant.AQUA;
        else variant = OrcaVariant.DEFAULT;
        return variant;
    }

    private void setupAnimationState() {
        createIdleAnimation(60, true);
        createSitAnimation(96, true);

        setupComboAnimations();
    }

    private static final int COMBO_ANIM_TICKS = 38;

    private void setupComboAnimations() {
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, COMBO_ANIM_TICKS);
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, COMBO_ANIM_TICKS);
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, COMBO_ANIM_TICKS);
    }

    private void setupComboAnimation(int comboNumber, AnimationState animationState, int timer, int maxTimer) {
        timer = tickComboAnimation(comboNumber, animationState, timer, maxTimer, this.isCombo(comboNumber));

        switch (comboNumber) {
            case 1:
                attack1ComboTimer = timer;
                break;
            case 2:
                attack2ComboTimer = timer;
                break;
            case 3:
                attack3ComboTimer = timer;
                break;
        }
    }


    @Override
    public void setVariant(net.tiew.operationWild.entity.OWEntity entity, int variant) {
        if (entity instanceof OrcaEntity orca) {
            orca.setVariant(OrcaVariant.byId(variant));
            orca.setInitialVariant(OrcaVariant.byId(variant));
        }
    }

    public OrcaVariant getVariant() {
        return OrcaVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(OrcaVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
    }

    public void setSkin(OrcaVariant skin) {
        this.setVariant(skin);
    }

    public float getRiderControlPitch() {
        return this.entityData.get(RIDER_CONTROL_PITCH);
    }

    public float getBodyYRot() {
        return bodyYRot;
    }

    public float getBodyYRot_passenger() {
        return bodyYRot_passenger;
    }

    public float getBodyZRot_passenger() {
        return bodyZRot_passenger;
    }

    public float getBodyXRot_passenger() {
        return bodyXRot_passenger;
    }

    @Override
    public void changeSkin(int skinIndex, boolean playingEffects) {
        super.changeSkin(skinIndex, playingEffects);
        this.setVariant(getInitialVariant());

        switch (skinIndex) {
            case 1 -> this.setSkin(OrcaVariant.Cosmetics.GOLD.variant);
            default -> this.setVariant(getInitialVariant());
        }
    }

    @Override
    public void changeSkinSilent(int skinIndex) {
        changeSkin(skinIndex, false);
    }

    @Override
    public int getInitialTypeVariant() {
        return this.getInitialVariant().getId();
    }

    public OrcaVariant getInitialVariant() {
        return OrcaVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(OrcaVariant variant) {
        this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("foodGiven", this.foodGiven);
        tag.putInt("foodWanted", this.foodWanted);
        tag.putInt("orcaUltimateKillCount", getOrcaUltimateKillCount());
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
        this.entityData.set(ULTIMATE_KILL_COUNT, tag.getInt("orcaUltimateKillCount"));

        if (this.getSkinIndex() != 0) {
            this.nbtRestoring = true;
            this.changeSkin(this.getSkinIndex(), false);
            this.nbtRestoring = false;
        }
    }

    static class OrcaWanderGoal extends Goal {

        private final OrcaEntity orca;
        private double targetX, targetY, targetZ;

        private boolean isVerticalPhase = false;
        private int verticalTimer = 0;
        private boolean goingUp = false;

        private int stallTicks = 0;
        private double lastX, lastZ;

        OrcaWanderGoal(OrcaEntity orca) {
            this.orca = orca;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!orca.isInWater()) return false;
            if (orca.getTarget() != null) return false;
            if (orca.isVehicle()) return false;
            if (orca.isSitting()) return false;
            if (orca.isNapping()) return false;
            pickHorizontalTarget();
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (!orca.isInWater()) return false;
            if (orca.getTarget() != null) return false;
            if (orca.isVehicle()) return false;
            if (orca.isSitting()) return false;
            double dx = orca.getX() - targetX;
            double dz = orca.getZ() - targetZ;
            if (dx * dx + dz * dz <= 6.25) pickHorizontalTarget();
            return true;
        }

        @Override
        public void tick() {
            if (isVerticalPhase) {
                verticalTimer--;

                double seaLevel = orca.level().getSeaLevel();
                double currentY = orca.getY();
                double minY = seaLevel - orca.getMaxDepth() + 5;
                double maxY = seaLevel - 2;

                boolean hitCeiling = goingUp && currentY >= maxY;
                boolean hitFloor = !goingUp && currentY <= minY;

                if (verticalTimer <= 0 || hitCeiling || hitFloor) {
                    isVerticalPhase = false;
                    orca.setTargetPitch(0f);
                    pickHorizontalTarget();
                } else {
                    double force = goingUp ? 0.025 : -0.025;
                    orca.setDeltaMovement(orca.getDeltaMovement().add(0, force, 0));

                    float pitchTarget = goingUp ? -25f : 25f;
                    float smooth = orca.getTargetPitch() + (pitchTarget - orca.getTargetPitch()) * 0.1f;
                    orca.setTargetPitch(smooth);
                }

            } else {
                if (orca.getRandom().nextInt(100) == 0) {
                    startVerticalPhase();
                    return;
                }
                float p = orca.getTargetPitch();
                if (Math.abs(p) > 0.1f) orca.setTargetPitch(p * 0.9f);
                else orca.setTargetPitch(0f);
            }

            tickStallCheck();
            orca.getMoveControl().setWantedPosition(targetX, targetY, targetZ, 1.0);
        }

        private void tickStallCheck() {
            double dx = orca.getX() - this.lastX;
            double dz = orca.getZ() - this.lastZ;
            this.lastX = orca.getX();
            this.lastZ = orca.getZ();

            if (dx * dx + dz * dz > 0.0016) {
                this.stallTicks = 0;
                return;
            }
            if (++this.stallTicks < STALL_LIMIT) return;

            this.stallTicks = 0;
            pickHorizontalTarget();
        }

        private static final int STALL_LIMIT = 60;

        private void startVerticalPhase() {
            isVerticalPhase = true;
            verticalTimer = 60 + orca.getRandom().nextInt(100);
            goingUp = orca.getRandom().nextBoolean();

            double angle = orca.getRandom().nextDouble() * Math.PI * 2;
            double dist = 4 + orca.getRandom().nextDouble() * 6;
            targetX = orca.getX() + Math.sin(angle) * dist;
            targetZ = orca.getZ() + Math.cos(angle) * dist;
            targetY = orca.getY();
        }

        private void pickHorizontalTarget() {
            isVerticalPhase = false;

            double seaLevel = orca.level().getSeaLevel();
            double minY = seaLevel - orca.getMaxDepth() + 5;
            double maxY = seaLevel - 2;

            double heading = -orca.getYRot() * Mth.DEG_TO_RAD;

            for (int attempt = 0; attempt < WATER_PICK_ATTEMPTS; attempt++) {
                double angle = heading + (orca.getRandom().nextDouble() - 0.5) * WANDER_ARC;
                double dist = 8 + orca.getRandom().nextDouble() * 12;
                double x = orca.getX() + Math.sin(angle) * dist;
                double z = orca.getZ() + Math.cos(angle) * dist;
                double y = Mth.clamp(orca.getY() + (orca.getRandom().nextDouble() - 0.5) * 4, minY, maxY);

                boolean last = attempt == WATER_PICK_ATTEMPTS - 1;
                if (!last && !orca.level().getFluidState(BlockPos.containing(x, y, z))
                        .is(net.minecraft.tags.FluidTags.WATER)) {
                    continue;
                }
                targetX = x;
                targetZ = z;
                targetY = y;
                return;
            }
        }

        private static final int WATER_PICK_ATTEMPTS = 8;

        private static final double WANDER_ARC = Math.toRadians(220);
    }
}