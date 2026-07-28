package net.tiew.operationWild.entity.animals.terrestrial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.core.OWIndication;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.neoforged.neoforge.common.CommonHooks;
import net.tiew.operationWild.entity.config.IOWRideable;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.variants.KangarooVariant;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class KangarooEntity extends OWEntity implements IOWEntity, IOWTamable, IOWRideable, PlayerRideableJumping {

    public static final double TAMING_EXPERIENCE = 65.0;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> IS_SPINNING = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_WEARING_BOXING_GLOVES = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> WHIRLWIND_COOLDOWN = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.INT);

    // ── Pilon Tellurique (ultime) ─────────────────────────────────────────────
    private static final EntityDataAccessor<Integer> ULTIMATE_KILL_COUNT = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.INT);
    /** Phase du Pilon Tellurique, cf. les constantes {@code STOMP_PHASE_*}. */
    private static final EntityDataAccessor<Integer> TELLURIC_STOMP_PHASE = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.INT);

    public static final int STOMP_PHASE_NONE   = 0;
    /** Ancrage au sol : la bête se ramasse sur ses pattes avant de défoncer le sol. */
    public static final int STOMP_PHASE_WINDUP = 1;
    public static final int STOMP_PHASE_LEAP   = 2;
    public static final int STOMP_PHASE_HOVER  = 3;
    public static final int STOMP_PHASE_DIVE   = 4;

    private int telluricStompWindupTimer = 0;
    private int telluricStompLeapTimer = 0;
    private int telluricStompLeapElapsed = 0;
    private int telluricStompHoverTimer = 0;
    private int telluricStompDiveTimer = 0;
    private int telluricStompDiveElapsed = 0;
    private boolean telluricLeapImpulseApplied = false;

    /** Ticks écoulés dans la phase courante — base de temps des animations (client). */
    public int telluricStompPhaseTicks = 0;
    private int telluricStompLastPhase = STOMP_PHASE_NONE;
    /** Réception après l'impact : décompte client, l'ultime est déjà terminé côté serveur. */
    public int telluricStompOutroTicks = 0;
    /** Tourbillon : angle et vitesse de rotation du corps sur lui-même (degrés, client). */
    public float telluricSpinAngle = 0f;
    public float telluricSpinSpeed = 0f;

    private int spinTicks = 0;
    private int sinceLastDamage = 0;
    private int whirlwindSoundTimer = 0;

    private float spinStopSpeed = 0f;
    private boolean wasSpinningForSpeed = false;

    public static final int WHIRLWIND_OUTRO_TICKS = 10;

    public int clientSpinTicks = 0;
    public float clientAnimTimeMs = 0f;
    public float clientSpinSpeed = 1f;
    public int clientOutroTicks = 0;
    private boolean clientWasSpinning = false;



    public boolean fourthHitFired = false;

    public volatile float bodyAnimY = 0f;

    protected float playerJumpPendingScale = 0f;
    private boolean isRidingJump = false;
    private int ridingJumpTimer = 0;

    public KangarooEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 22.0)
                .add(Attributes.MOVEMENT_SPEED, 0.21D)
                .add(Attributes.FOLLOW_RANGE, 30.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D)
                .add(Attributes.JUMP_STRENGTH, 1.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(9, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(10, new OWRandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(IS_SPINNING, false);
        builder.define(WHIRLWIND_COOLDOWN, 0);
        builder.define(IS_WEARING_BOXING_GLOVES, false);
        builder.define(ULTIMATE_KILL_COUNT, 0);
        builder.define(TELLURIC_STOMP_PHASE, 0);
    }

    @Override
    public int getEntityColor() {
        return 0xd7b17d;
    }

    @Override
    public float getTheoreticalScale() {
        return 7.5f;
    }

    @Override
    public double getTamingExperience() {
        return TAMING_EXPERIENCE;
    }

    @Override
    public OWEntityConfig.Archetypes getArchetype() {
        return OWEntityConfig.Archetypes.SCOUT;
    }

    @Override
    public OWEntityConfig.Diet getDiet() {
        return OWEntityConfig.Diet.VEGETARIAN;
    }

    @Override
    public OWEntityConfig.Temperament getTemperament() {
        return OWEntityConfig.Temperament.NEUTRAL;
    }

    @Override
    public float vehicleRunSpeedMultiplier() {
        return 3.75f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 2f;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return -1f;
    }

    @Override
    public float vehicleWaterSpeedDivider() {
        return 3f;
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
        return OWItems.TIGER_SADDLE.get();
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.TIGER_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return 150 * (1 + ((float) this.getLevel() / 50));
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 1.0f * (1 + ((float) this.getLevel() / 50));
    }

    @Override
    public boolean preferRawMeat() {
        return false;
    }

    @Override
    public boolean preferCookedMeat() {
        return false;
    }

    @Override
    public boolean preferVegetables() {
        return true;
    }

    @Override
    public boolean riderCameraFollowsBodyTilt() {
        return false;
    }

    @Override
    public float getRotationSpeed() {
        return 0.225f;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.KANGAROO.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.TIGER_FOOD);
    }

    @Override
    public float getScale() {
        return super.getScale() <= 0 ? 1f : super.getScale();
    }

    @Override
    protected double getBaseRiderYOffset() {
        return this.getBbHeight() * 0.625 * this.getScale();
    }

    @Override
    protected float getRiderAnimYOffset() {
        return -bodyAnimY / 16.0f * this.getScale();
    }

    @Override
    public int getComboPauseDelay() {
        return getComboAttack() == 3 ? 22 : 2;
    }

    @Override
    protected boolean isImmobile() {
        return isSpinning() || isTelluricStomping() || super.isImmobile();
    }

    /**
     * Pendant le Pilon Tellurique, on traite le kangourou comme un véhicule en bond : {@code travelRidden}
     * cesse alors de réécrire la vélocité horizontale depuis le regard aplati et laisse la {@code deltaMovement}
     * balistique (le plongeon piqué dans la direction du regard) s'appliquer telle quelle.
     */
    @Override
    protected boolean isLeapingVehicle() {
        return isTelluricStomping() || super.isLeapingVehicle();
    }

    @Override
    public float getRiddenSpeedVehicle(Player player) {
        if (isSpinning()) {
            wasSpinningForSpeed = true;
            spinStopSpeed *= 0.65f;
            if (spinStopSpeed < 0.005f) spinStopSpeed = 0f;
            return spinStopSpeed;
        }
        if (wasSpinningForSpeed) {
            wasSpinningForSpeed = false;
            resetRiddenSpeed();
        }
        float speed = super.getRiddenSpeedVehicle(player);
        spinStopSpeed = speed;
        return speed;
    }

    @Override
    protected boolean forceRiderLookBodyRotation() {
        return isSpinning() || isTelluricStomping();
    }

    @Override
    protected int calculateFallDamage(float fallDistance, float multiplier) {
        return 0;
    }

    @Override
    public void tickRidden(Player player, Vec3 travelVector) {
        super.tickRidden(player, travelVector);

        if (isRidingJump) {
            ridingJumpTimer++;
            if (ridingJumpTimer > 5 && this.onGround()) {
                isRidingJump = false;
                ridingJumpTimer = 0;
            }
        } else {
            ridingJumpTimer = 0;
        }

        if (this.isControlledByLocalInstance() && this.onGround() && !isRidingJump) {
            if (playerJumpPendingScale > 0f && !isSpinning()) {
                executeRidersJump(playerJumpPendingScale);
            }
            playerJumpPendingScale = 0f;
        }

        // Pilon Tellurique : le bond est piloté ici (instance contrôlée), juste avant travel() — seul
        // endroit fiable pour poser la vélocité d'une monture montée. Courbe ease-out : gros BOOM au
        // départ puis montée de plus en plus douce jusqu'à l'apogée.
        if (this.isControlledByLocalInstance() && getTelluricStompPhase() == STOMP_PHASE_LEAP) {
            float progress = Mth.clamp(
                    (float) telluricStompLeapElapsed / OWAttacksConstants.Kangaroo.TELLURIC_STOMP_LEAP_TICKS, 0f, 1f);
            float ease = (1f - progress) * (1f - progress);
            // Élan horizontal vers le yaw du rider → trajectoire en arc (parabole) plutôt qu'un saut vertical.
            Vec3 flat = Vec3.directionFromRotation(0f, player.getYRot());
            double fwd = OWAttacksConstants.Kangaroo.TELLURIC_STOMP_LEAP_FORWARD;
            this.setDeltaMovement(
                    flat.x * fwd,
                    OWAttacksConstants.Kangaroo.TELLURIC_STOMP_LEAP_POWER * ease,
                    flat.z * fwd);
            this.hasImpulse = true;
            if (!telluricLeapImpulseApplied) {
                CommonHooks.onLivingJump(this);
                telluricLeapImpulseApplied = true;
            }
        }
    }

    private void executeRidersJump(float scale) {
        double verticalPower = this.getAttributeValue(Attributes.JUMP_STRENGTH) * scale
                * (double) this.getBlockJumpFactor()
                + (double) (this.getJumpBoostPower() * 3);

        Vec3 lookFlat = this.getLookAngle().multiply(1, 0, 1);
        if (lookFlat.lengthSqr() > 1.0E-7D) lookFlat = lookFlat.normalize();

        Vec3 movement = this.getDeltaMovement();
        this.setDeltaMovement(
                movement.x + lookFlat.x * 0.4 * scale,
                verticalPower,
                movement.z + lookFlat.z * 0.4 * scale
        );
        this.hasImpulse = true;
        CommonHooks.onLivingJump(this);

        this.level().playLocalSound(getX(), getY(), getZ(),
                SoundEvents.HORSE_JUMP, SoundSource.NEUTRAL, 0.4f, 1.0f, false);

        isRidingJump = true;
        ridingJumpTimer = 0;
    }

    @Override
    public void onPlayerJump(int jumpPower) {
        if (!this.isSaddled()) return;
        if (jumpPower < 0) jumpPower = 0;
        this.playerJumpPendingScale = jumpPower >= 90 ? 1.0f : 0.4f + 0.4f * jumpPower / 90.0f;
    }

    @Override
    public boolean canJump() {
        return this.isSaddled() && !isSpinning();
    }

    @Override
    public void handleStartJump(int jumpPower) { }

    @Override
    public void handleStopJump() { }

    @Override
    public void tick() {
        super.tick();

        int timeToHit = getComboAttack() == 3 ? 6 : 10;
        int timeMax = getComboAttack() == 3 ? 35 : 20;
        createCombo(timeMax, timeToHit, SoundEvents.PLAYER_ATTACK_STRONG, 3.0, 3.0, 1.5, false, 1.5f);

        if (isPauseCombo() && getComboAttack() == 3) {
            resetCombo(0);
            actualAttackNumber = 0;
        }

        tickWhirlwind();
        tickTelluricStomp();

        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);
    }

    public boolean isSpinning() { return this.entityData.get(IS_SPINNING); }
    private void setSpinning(boolean value) { this.entityData.set(IS_SPINNING, value); }

    public int getWhirlwindCooldownTicks() { return this.entityData.get(WHIRLWIND_COOLDOWN); }
    private void setWhirlwindCooldownTicks(int value) { this.entityData.set(WHIRLWIND_COOLDOWN, Math.max(0, value)); }

    public void startWhirlwind() {
        if (this.level().isClientSide()) return;
        if (isSpinning()) return;
        if (this.isInWater() || !this.onGround()) return;
        if (getWhirlwindCooldownTicks() > 0) return;
        if (getVitalEnergy() > getMaxVitalEnergy() - OWAttacksConstants.Kangaroo.WHIRLWIND_ENERGY) {
            canShowVitalEnergyLack = true;
            return;
        }
        setSpinning(true);
        spinTicks = 0;
        sinceLastDamage = Integer.MAX_VALUE / 2;
        whirlwindSoundTimer = 0;
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.NEUTRAL, 1.1f, 0.6f);
    }

    public void stopWhirlwind() {
        if (this.level().isClientSide()) return;
        if (!isSpinning()) return;
        boolean cooldownEarned = spinTicks >= OWAttacksConstants.Kangaroo.WHIRLWIND_COOLDOWN_THRESHOLD_TICKS;
        setSpinning(false);
        spinTicks = 0;
        if (cooldownEarned) {
            setWhirlwindCooldownTicks(OWAttacksConstants.Kangaroo.WHIRLWIND_COOLDOWN_TICKS);
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.PLAYER_ATTACK_WEAK, SoundSource.NEUTRAL, 0.9f, 0.7f);
    }

    private void tickWhirlwind() {
        if (this.level().isClientSide()) {
            if (isSpinning()) {
                clientSpinTicks++;
                clientSpinSpeed = computeAnimSpeedMultiplier(clientSpinTicks);
                clientAnimTimeMs += 50f * clientSpinSpeed;
                clientWasSpinning = true;
            } else {
                if (clientWasSpinning) {
                    clientWasSpinning = false;
                    clientOutroTicks = WHIRLWIND_OUTRO_TICKS;
                }
                if (clientOutroTicks > 0) clientOutroTicks--;
                clientSpinTicks = 0;
                clientSpinSpeed = 1f;
                clientAnimTimeMs = 0f;
            }
            return;
        }

        if (isSpinning()) {
            Player rider = (getFirstPassenger() instanceof Player p) ? p : null;
            if (rider == null || (getOwnerUUID() != null && !getOwnerUUID().equals(rider.getUUID()))) {
                stopWhirlwind();
                return;
            }

            if (this.isInWater() || !this.onGround()) {
                stopWhirlwind();
                return;
            }

            spinTicks++;

            if (spinTicks >= OWAttacksConstants.Kangaroo.WHIRLWIND_DAMAGE_START_TICKS) {
                sinceLastDamage++;
                if (sinceLastDamage >= OWAttacksConstants.Kangaroo.WHIRLWIND_DAMAGE_INTERVAL_TICKS) {
                    sinceLastDamage = 0;
                    dealWhirlwindDamage(computeWhirlwindDamage(spinTicks));
                }
            }

            float speedFactor = Mth.clamp((float) spinTicks / OWAttacksConstants.Kangaroo.WHIRLWIND_DAMAGE_PEAK_TICKS, 0f, 1f);
            int soundInterval = Math.max(2, (int) (8 - 6 * speedFactor));
            if (++whirlwindSoundTimer >= soundInterval) {
                whirlwindSoundTimer = 0;
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.NEUTRAL,
                        0.55f, 0.7f + 0.9f * speedFactor);
            }

            if (spinTicks >= OWAttacksConstants.Kangaroo.WHIRLWIND_MAX_DURATION_TICKS) {
                stopWhirlwind();
            }
        } else if (getWhirlwindCooldownTicks() > 0) {
            setWhirlwindCooldownTicks(getWhirlwindCooldownTicks() - 1);
        }
    }

    private float computeWhirlwindDamage(int ticks) {
        int start = OWAttacksConstants.Kangaroo.WHIRLWIND_DAMAGE_START_TICKS;
        int peak = OWAttacksConstants.Kangaroo.WHIRLWIND_DAMAGE_PEAK_TICKS;
        float f = Mth.clamp((float) (ticks - start) / (peak - start), 0f, 1f);
        return OWAttacksConstants.Kangaroo.WHIRLWIND_DAMAGE_MIN
                + (OWAttacksConstants.Kangaroo.WHIRLWIND_DAMAGE_MAX - OWAttacksConstants.Kangaroo.WHIRLWIND_DAMAGE_MIN) * f;
    }

    private void dealWhirlwindDamage(float amount) {
        double r = OWAttacksConstants.Kangaroo.WHIRLWIND_RADIUS;
        AABB area = this.getBoundingBox().inflate(r, 1.0, r);
        UUID owner = this.getOwnerUUID();

        double yaw = Math.toRadians(this.getYRot());
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);

        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, area, target -> {
            if (target == this) return false;
            if (this.getPassengers().contains(target)) return false;
            if (isAlliedTo(target)) return false;
            if (owner != null) {
                if (target.getUUID().equals(owner)) return false;
                if (target instanceof TamableAnimal ta && owner.equals(ta.getOwnerUUID())) return false;
            }
            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            double distSq = dx * dx + dz * dz;
            double reach = r + target.getBbWidth() / 2.0;
            if (distSq > reach * reach) return false;
            if (distSq > 1.0e-4) {
                double dot = (dx * fx + dz * fz) / Math.sqrt(distSq);
                if (dot < OWAttacksConstants.Kangaroo.WHIRLWIND_FRONT_DOT) return false;
            }
            return true;
        });

        for (LivingEntity target : targets) {
            target.hurt(this.damageSources().mobAttack(this), amount);
        }
    }

    private float computeAnimSpeedMultiplier(int ticks) {
        float f = Mth.clamp((float) ticks / OWAttacksConstants.Kangaroo.WHIRLWIND_DAMAGE_PEAK_TICKS, 0f, 1f);
        f = f * f;
        return 1f + 4f * f;
    }

    // ==================================================
    //          PILON TELLURIQUE (ultime)
    // ==================================================

    public int getUltimateKillCount() { return this.entityData.get(ULTIMATE_KILL_COUNT); }
    private void setUltimateKillCount(int count) { this.entityData.set(ULTIMATE_KILL_COUNT, Math.max(0, count)); }

    public int getTelluricStompPhase() { return this.entityData.get(TELLURIC_STOMP_PHASE); }
    private void setTelluricStompPhase(int phase) { this.entityData.set(TELLURIC_STOMP_PHASE, phase); }
    public boolean isTelluricStomping() { return getTelluricStompPhase() != STOMP_PHASE_NONE; }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        int kills = getUltimateKillCount();
        if (kills < OWAttacksConstants.Kangaroo.TELLURIC_STOMP_KILLS_REQUIRED) {
            setUltimateKillCount(kills + 1);
        }
        return super.killedEntity(serverLevel, entity);
    }

    /**
     * Détection « au sol » robuste pour décider du bond initial. {@code onGround()} est peu fiable côté
     * serveur pour une monture chevauchée ; on teste donc une collision de bloc juste sous les pieds
     * (tolérance 0,5 bloc), ce qui est déterministe quelle que soit la position synchronisée.
     */
    private boolean isGroundedForStomp() {
        if (this.onGround()) return true;
        AABB box = this.getBoundingBox();
        AABB probe = new AABB(box.minX, box.minY - 0.5, box.minZ, box.maxX, box.minY, box.maxZ);
        return !this.level().noCollision(this, probe);
    }

    /** Déclenche le Pilon Tellurique (serveur). Au sol : bond initial → suspension → plongeon. En l'air : suspension → plongeon. */
    public void activateTelluricStomp() {
        if (this.level().isClientSide()) return;
        if (isTelluricStomping()) return;
        if (getUltimateKillCount() < OWAttacksConstants.Kangaroo.TELLURIC_STOMP_KILLS_REQUIRED) return;
        if (getVitalEnergy() > getMaxVitalEnergy() - OWAttacksConstants.Kangaroo.TELLURIC_STOMP_ENERGY) {
            canShowVitalEnergyLack = true;
            return;
        }

        setVitalEnergy(getVitalEnergy() + OWAttacksConstants.Kangaroo.TELLURIC_STOMP_ENERGY);
        setUltimateKillCount(0);

        telluricLeapImpulseApplied = false;
        this.fallDistance = 0f;
        this.hasImpulse = true;

        if (isGroundedForStomp()) {
            // Au sol (ou tout près) : ancrage sur les pattes, puis bond.
            setTelluricStompPhase(STOMP_PHASE_WINDUP);
            telluricStompWindupTimer = OWAttacksConstants.Kangaroo.TELLURIC_STOMP_WINDUP_TICKS;
        } else {
            // Déjà en l'air : suspension directe.
            setTelluricStompPhase(STOMP_PHASE_HOVER);
            telluricStompHoverTimer = OWAttacksConstants.Kangaroo.TELLURIC_STOMP_HOVER_TICKS;
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.LEG_HURT.get(), SoundSource.NEUTRAL, 1.2f, 0.7f);
    }

    private void cancelTelluricStomp() {
        setTelluricStompPhase(STOMP_PHASE_NONE);
        telluricStompWindupTimer = 0;
        telluricStompLeapTimer = 0;
        telluricStompLeapElapsed = 0;
        telluricStompHoverTimer = 0;
        telluricStompDiveTimer = 0;
        telluricStompDiveElapsed = 0;
        telluricLeapImpulseApplied = false;
        this.setNoGravity(false);
    }

    private void tickTelluricStomp() {
        if (this.level().isClientSide()) tickTelluricStompVisuals();

        if (!isTelluricStomping()) {
            // Réarme les compteurs pour le prochain usage (le client n'exécute pas activate/cancel).
            telluricLeapImpulseApplied = false;
            telluricStompLeapElapsed = 0;
            telluricStompDiveElapsed = 0;
            return;
        }

        this.fallDistance = 0f;
        int phase = getTelluricStompPhase();

        // ── Phase 1 : ancrage au sol ─────────────────────────────────────────
        // Les pattes se plient sous la masse : la bête freine sur place, le temps que le geste se
        // lise, puis détend tout d'un coup. Sans ce temps mort, le bond partait sur une pose de
        // repos et le « coup de pied au sol » n'existait tout simplement pas à l'écran.
        if (phase == STOMP_PHASE_WINDUP) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.55, 1.0, 0.55));

            if (this.level().isClientSide()) return;

            if (telluricStompWindupTimer > 0) {
                telluricStompWindupTimer--;
            } else {
                setTelluricStompPhase(STOMP_PHASE_LEAP);
                telluricStompLeapTimer = OWAttacksConstants.Kangaroo.TELLURIC_STOMP_LEAP_TICKS;
                telluricLeapImpulseApplied = false;
                spawnTelluricLaunchBurst();
            }
            return;
        }

        // ── Phase 2 : bond initial (depuis le sol) ───────────────────────────
        // La vitesse verticale (courbe ease-out) est pilotée dans tickRidden ; ici on coupe la gravité
        // (on contrôle entièrement la montée) et on avance le compteur des deux côtés.
        if (phase == STOMP_PHASE_LEAP) {
            this.setNoGravity(true);
            telluricStompLeapElapsed++;

            if (this.level().isClientSide()) return;

            if (telluricStompLeapTimer > 0) {
                telluricStompLeapTimer--;
            } else {
                setTelluricStompPhase(STOMP_PHASE_HOVER);
                telluricStompHoverTimer = OWAttacksConstants.Kangaroo.TELLURIC_STOMP_HOVER_TICKS;
            }
            return;
        }

        // ── Phase 3 : suspension en l'air ────────────────────────────────────
        if (phase == STOMP_PHASE_HOVER) {
            this.setNoGravity(true);
            // Décélération douce vers l'immobilité (pas d'arrêt sec) : la vélocité du bond
            // s'amortit progressivement jusqu'à la suspension.
            this.setDeltaMovement(this.getDeltaMovement().scale(OWAttacksConstants.Kangaroo.TELLURIC_STOMP_HOVER_DAMPING));
            this.hasImpulse = true;

            if (this.level().isClientSide()) return;

            if (telluricStompHoverTimer > 0) {
                telluricStompHoverTimer--;
            } else {
                setTelluricStompPhase(STOMP_PHASE_DIVE);
                telluricStompDiveTimer = OWAttacksConstants.Kangaroo.TELLURIC_STOMP_MAX_DIVE_TICKS;
            }
            return;
        }

        // ── Phase 4 : plongeon dans la direction du regard du rider ──────────
        this.setNoGravity(true);
        telluricStompDiveElapsed++;
        LivingEntity rider = this.getControllingPassenger();
        // On suit le yaw du regard, mais on borne le pitch vers le bas (jamais en l'air) :
        // entre MIN_DIVE_PITCH (45° sous l'horizontale) et 90° (verticale).
        float lookPitch = rider != null ? rider.getXRot() : this.getXRot();
        float lookYaw = rider != null ? rider.getYRot() : this.getYRot();
        float divePitch = Mth.clamp(lookPitch, OWAttacksConstants.Kangaroo.TELLURIC_STOMP_MIN_DIVE_PITCH, 90f);
        Vec3 dir = Vec3.directionFromRotation(divePitch, lookYaw);
        // Montée en puissance douce : départ progressif (ease-in) puis pleine vitesse de plongeon.
        float ramp = Mth.clamp((float) telluricStompDiveElapsed / OWAttacksConstants.Kangaroo.TELLURIC_STOMP_DIVE_RAMP_TICKS, 0f, 1f);
        double speed = OWAttacksConstants.Kangaroo.TELLURIC_STOMP_DIVE_SPEED * (0.3 + 0.7 * (ramp * ramp));
        this.setDeltaMovement(dir.scale(speed));
        this.hasImpulse = true;

        if (this.level().isClientSide()) return;

        // Sifflement de chute, de plus en plus aigu : la frappe s'entend venir avant qu'on la voie.
        if (telluricStompDiveElapsed % 3 == 1) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.NEUTRAL,
                    0.75f, 0.55f + 0.09f * Math.min(telluricStompDiveElapsed, 12));
        }

        if (telluricStompDiveTimer > 0) telluricStompDiveTimer--;

        if (this.onGround() || this.verticalCollision || this.horizontalCollision || telluricStompDiveTimer <= 0) {
            executeTelluricStompImpact();
            cancelTelluricStomp();
        }
    }

    /**
     * Base de temps et effets du Pilon Tellurique, côté client uniquement.
     *
     * <p>Le serveur ne réplique que la phase : le compteur de trames est reconstruit ici, et remis à
     * zéro à chaque changement de phase — c'est lui qui donne son point de départ à l'animation
     * correspondante. La réception, elle, n'a pas de phase du tout (l'ultime est déjà clos côté
     * serveur au moment de l'impact) et vit sur son propre décompte.</p>
     */
    private void tickTelluricStompVisuals() {
        int phase = getTelluricStompPhase();

        // Décompte AVANT l'armement : sinon la trame d'impact consommait aussitôt le premier tick de
        // la réception, et l'encaissement — l'image la plus courte du geste — passait à la trappe.
        if (telluricStompOutroTicks > 0) telluricStompOutroTicks--;

        if (phase != telluricStompLastPhase) {
            if (phase == STOMP_PHASE_NONE && telluricStompLastPhase == STOMP_PHASE_DIVE) {
                telluricStompOutroTicks = OWAttacksConstants.Kangaroo.TELLURIC_STOMP_OUTRO_TICKS;
            }
            telluricStompLastPhase = phase;
            telluricStompPhaseTicks = 0;
        } else if (phase != STOMP_PHASE_NONE) {
            telluricStompPhaseTicks++;
        }

        tickTelluricSpin(phase);
        spawnTelluricStompParticles(phase);
    }

    /**
     * Tourbillon : la bête se met à tourner sur elle-même dès qu'elle quitte le sol, de plus en plus
     * vite jusqu'au plongeon. À la réception, la rotation retombe en se recalant sur le tour complet
     * le plus proche — sans quoi le corps resterait figé de travers à un angle arbitraire.
     */
    private void tickTelluricSpin(int phase) {
        switch (phase) {
            case STOMP_PHASE_LEAP  -> telluricSpinSpeed = Math.min(telluricSpinSpeed + 2.0f, 14f);
            case STOMP_PHASE_HOVER -> telluricSpinSpeed = Math.min(telluricSpinSpeed + 5.0f, 34f);
            case STOMP_PHASE_DIVE  -> telluricSpinSpeed = Math.min(telluricSpinSpeed + 6.0f, 52f);
            case STOMP_PHASE_WINDUP -> telluricSpinSpeed = 0f;
            default -> telluricSpinSpeed *= 0.7f;
        }

        telluricSpinAngle += telluricSpinSpeed;

        if (phase == STOMP_PHASE_NONE) {
            if (telluricSpinSpeed < 0.5f) telluricSpinSpeed = 0f;
            float snap = Math.round(telluricSpinAngle / 360f) * 360f;
            telluricSpinAngle = Mth.lerp(0.3f, telluricSpinAngle, snap);
            if (telluricSpinSpeed == 0f && Math.abs(telluricSpinAngle - snap) < 0.5f) telluricSpinAngle = 0f;
        }
    }

    /** Poussière au sol pendant l'ancrage, puis spirale d'air autour du corps une fois en l'air. */
    private void spawnTelluricStompParticles(int phase) {
        if (phase == STOMP_PHASE_WINDUP) {
            BlockState ground = this.level().getBlockState(this.blockPosition().below());
            if (ground.isAir()) return;
            BlockParticleOption dust = new BlockParticleOption(ParticleTypes.BLOCK, ground);
            for (int i = 0; i < 4; i++) {
                double a = this.random.nextDouble() * Math.PI * 2.0;
                double r = 0.9 + this.random.nextDouble() * 0.8;
                this.level().addParticle(dust,
                        this.getX() + Math.cos(a) * r, this.getY() + 0.1, this.getZ() + Math.sin(a) * r,
                        Math.cos(a) * 0.12, 0.14, Math.sin(a) * 0.12);
            }
            return;
        }

        if (phase != STOMP_PHASE_HOVER && phase != STOMP_PHASE_DIVE) return;

        boolean diving = phase == STOMP_PHASE_DIVE;
        int arms = diving ? 4 : 3;
        double height = this.getBbHeight();
        for (int i = 0; i < arms; i++) {
            double a = Math.toRadians(telluricSpinAngle) + i * (Math.PI * 2.0 / arms);
            double r = (diving ? 1.1 : 1.5) + this.random.nextDouble() * 0.5;
            double px = this.getX() + Math.cos(a) * r;
            double pz = this.getZ() + Math.sin(a) * r;
            double py = this.getY() + this.random.nextDouble() * height;
            // Vitesse tangentielle : les traînées s'enroulent autour du corps au lieu de gicler.
            double vx = -Math.sin(a) * (diving ? 0.55 : 0.35);
            double vz = Math.cos(a) * (diving ? 0.55 : 0.35);
            this.level().addParticle(ParticleTypes.CLOUD, px, py, pz, vx, diving ? 0.18 : 0.04, vz);
            if (diving && this.random.nextInt(2) == 0) {
                this.level().addParticle(ParticleTypes.CRIT, px, py, pz, vx * 0.5, 0.1, vz * 0.5);
            }
        }

        if (diving) {
            spawnTelluricSoleTrail();
            spawnTelluricImpactTelegraph();
        }
    }

    /**
     * Sillage sous les semelles : c'est le pied qui fend l'air, pas le corps. Les traînées naissent
     * juste sous les pattes et filent vers le bas, pour que l'œil suive la semelle et non la bête.
     */
    private void spawnTelluricSoleTrail() {
        double spread = this.getBbWidth() * 0.35;
        for (int i = 0; i < 5; i++) {
            double px = this.getX() + (this.random.nextDouble() - 0.5) * spread;
            double pz = this.getZ() + (this.random.nextDouble() - 0.5) * spread;
            double py = this.getY() - 0.15 - this.random.nextDouble() * 0.5;
            this.level().addParticle(ParticleTypes.CLOUD, px, py, pz, 0.0, -0.55, 0.0);
            if (this.random.nextInt(2) == 0) {
                this.level().addParticle(ParticleTypes.CRIT, px, py, pz,
                        (this.random.nextDouble() - 0.5) * 0.2, -0.35, (this.random.nextDouble() - 0.5) * 0.2);
            }
        }
    }

    /**
     * Zone d'impact projetée au sol pendant la chute : anneau tourbillonnant sur le rayon de l'onde,
     * d'autant plus dense que la semelle approche.
     *
     * <p>Sans elle, le plongeon se lisait dans le vide — ni le cavalier ni ceux qui sont dessous ne
     * savaient où la frappe allait tomber, alors même que c'est la seule information qui compte
     * pendant cette seconde-là. Le rayon dessiné est exactement celui des dégâts.</p>
     */
    private void spawnTelluricImpactTelegraph() {
        Vec3 from = this.position();
        net.minecraft.world.phys.BlockHitResult hit = this.level().clip(new net.minecraft.world.level.ClipContext(
                from, from.add(0.0, -32.0, 0.0),
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, this));
        if (hit.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK) return;

        double groundY = hit.getLocation().y + 0.08;
        double fall = Math.max(0.0, this.getY() - groundY);
        // Proximité : de 0 (loin) à 1 (semelle sur le point de toucher).
        float closeness = (float) Mth.clamp(1.0 - fall / 12.0, 0.15, 1.0);
        double radius = OWAttacksConstants.Kangaroo.TELLURIC_STOMP_RADIUS;

        int points = 6 + (int) (10 * closeness);
        double baseAngle = Math.toRadians(telluricSpinAngle * 0.5);
        for (int i = 0; i < points; i++) {
            double a = baseAngle + (Math.PI * 2.0 * i) / points;
            double px = this.getX() + Math.cos(a) * radius;
            double pz = this.getZ() + Math.sin(a) * radius;
            this.level().addParticle(ParticleTypes.CRIT, px, groundY, pz,
                    -Math.sin(a) * 0.18, 0.04 + 0.1 * closeness, Math.cos(a) * 0.18);
        }

        BlockState ground = this.level().getBlockState(net.minecraft.core.BlockPos.containing(
                hit.getLocation().x, hit.getLocation().y - 0.1, hit.getLocation().z));
        if (ground.isAir()) return;
        BlockParticleOption dust = new BlockParticleOption(ParticleTypes.BLOCK, ground);
        int inner = 1 + (int) (3 * closeness);
        for (int i = 0; i < inner; i++) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            double r = radius * (0.15 + this.random.nextDouble() * 0.5);
            this.level().addParticle(dust,
                    this.getX() + Math.cos(a) * r, groundY, this.getZ() + Math.sin(a) * r,
                    0.0, 0.08 + 0.22 * closeness, 0.0);
        }
    }

    /** Décollage : la terre part en gerbe sous les pattes au moment de la détente. */
    private void spawnTelluricLaunchBurst() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        BlockState ground = this.level().getBlockState(this.blockPosition().below());
        if (!ground.isAir()) {
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                    this.getX(), this.getY() + 0.1, this.getZ(), 120, 1.2, 0.1, 1.2, 0.45);
        }
        serverLevel.sendParticles(ParticleTypes.CLOUD,
                this.getX(), this.getY() + 0.1, this.getZ(), 40, 1.4, 0.05, 1.4, 0.2);
        serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                this.getX(), this.getY() + 0.4, this.getZ(), 8, 1.2, 0.1, 1.2, 0.0);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.NEUTRAL, 0.7f, 1.5f);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ROOTED_DIRT_BREAK, SoundSource.NEUTRAL, 1.3f, 0.6f);
    }

    /** Onde de choc à l'atterrissage : dégâts dégressifs, Lenteur I et léger pop vertical. */
    private void executeTelluricStompImpact() {
        double radius = OWAttacksConstants.Kangaroo.TELLURIC_STOMP_RADIUS;
        AABB area = this.getBoundingBox().inflate(radius, 2.0, radius);
        UUID owner = this.getOwnerUUID();

        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, area, target -> {
            if (target == this) return false;
            if (this.getPassengers().contains(target)) return false;
            if (isAlliedTo(target)) return false;
            if (owner != null) {
                if (target.getUUID().equals(owner)) return false;
                if (target instanceof TamableAnimal ta && owner.equals(ta.getOwnerUUID())) return false;
            }
            return this.distanceToSqr(target) <= radius * radius;
        });

        float baseDamage = this.getDamage();
        for (LivingEntity target : targets) {
            double dist = Math.sqrt(this.distanceToSqr(target));
            float t = (float) Mth.clamp(dist / radius, 0.0, 1.0);
            float mult = Mth.lerp(t,
                    OWAttacksConstants.Kangaroo.TELLURIC_STOMP_DAMAGE_CENTER_MULT,
                    OWAttacksConstants.Kangaroo.TELLURIC_STOMP_DAMAGE_EDGE_MULT);

            target.hurt(this.damageSources().mobAttack(this), baseDamage * mult);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                    OWAttacksConstants.Kangaroo.TELLURIC_STOMP_SLOWNESS_TICKS, 0));

            // Léger envol, dans l'esprit de l'uppercut du combo 3.
            Vec3 outward = target.position().subtract(this.position());
            outward = outward.lengthSqr() > 1.0e-4 ? outward.multiply(1, 0, 1).normalize() : Vec3.ZERO;
            Vec3 motion = target.getDeltaMovement();
            target.setDeltaMovement(motion.x * 0.3 + outward.x * 0.3, 0.6, motion.z * 0.3 + outward.z * 0.3);
            target.hasImpulse = true;
            target.hurtMarked = true;
        }

        createMiniShockwave();

        if (this.level() instanceof ServerLevel serverLevel) {
            // Gros boom au sol : grosse déflagration centrale + nuées d'explosion, terre et poussière.
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 0.2, this.getZ(),
                    1, 0.0, 0.0, 0.0, 0.0);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.3, this.getZ(),
                    24, radius * 0.6, 0.15, radius * 0.6, 0.0);
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY() + 0.5, this.getZ(),
                    30, radius * 0.6, 0.2, radius * 0.6, 0.1);
            serverLevel.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.1, this.getZ(),
                    60, radius * 0.65, 0.05, radius * 0.65, 0.15);
            BlockParticleOption dirtParticle = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState());
            serverLevel.sendParticles(dirtParticle, this.getX(), this.getY() + 0.1, this.getZ(),
                    240, radius * 0.6, 0.3, radius * 0.6, 0.5);

            // Couronne de poussière posée sur le rayon exact de l'onde : le joueur voit d'un coup
            // d'œil jusqu'où l'ultime a porté, plutôt que d'avoir à le deviner d'un nuage informe.
            int ringPoints = 48;
            for (int i = 0; i < ringPoints; i++) {
                double angle = (Math.PI * 2.0 * i) / ringPoints;
                double rx = this.getX() + Math.cos(angle) * radius;
                double rz = this.getZ() + Math.sin(angle) * radius;
                serverLevel.sendParticles(ParticleTypes.CLOUD, rx, this.getY() + 0.25, rz,
                        2, 0.1, 0.1, 0.1, 0.03);
                serverLevel.sendParticles(dirtParticle, rx, this.getY() + 0.1, rz,
                        3, 0.15, 0.1, 0.15, 0.12);
            }
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.NEUTRAL, 1.6f, 0.7f);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.LEG_HURT.get(), SoundSource.NEUTRAL, 1.4f, 0.6f);
    }

    public void createMiniShockwave() {
        Vec3 look = this.getLookAngle();
        double x = this.getX() + look.x * 2.0;
        double z = this.getZ() + look.z * 2.0;
        BlockParticleOption particleOption = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState());
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(particleOption, x, this.getY(), z, 60, 1.0, 0.1, 1.0, 0.25);
        } else {
            AABB area = new AABB(x - 1.0, this.getY() - 0.1, z - 1.0, x + 1.0, this.getY() + 0.2, z + 1.0);
            for (int i = 0; i < 60; i++) {
                double px = area.minX + Math.random() * (area.maxX - area.minX);
                double py = area.minY + Math.random() * (area.maxY - area.minY);
                double pz = area.minZ + Math.random() * (area.maxZ - area.minZ);
                this.level().addParticle(particleOption, px, py, pz, 0, 0.1, 0);
            }
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ROOTED_DIRT_HIT, SoundSource.AMBIENT, 1.0f, 1.0f);
    }

    @Override
    protected void onSuccessfulHit(LivingEntity entity) {
        super.onSuccessfulHit(entity);

        if (this.isWearingBoxingGloves()) {
            int exp = (int) OWUtils.generateRandomInterval(1, 2);

            if (!entity.level().isClientSide()) {
                ExperienceOrb.award((ServerLevel) entity.level(), entity.position(), exp);
            }
        }
    }

    @Override
    protected int getDefaultSkinIndex() { return 2; }   // « Kangourou par défaut »

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof KangarooEntity otherKangaroo) {
            if (otherKangaroo.isBaby()) {
                return true;
            }
            if (this.isTame()) {
                if (otherKangaroo.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherKangaroo.getOwnerUUID())) {
                    return true;
                }
            } else if (!otherKangaroo.isTame()) {
                return true;
            }
        }
        return super.isAlliedTo(entity);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {

    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));

            this.setVariant(chooseKangarooVariant());
            this.setInitialVariant(this.getVariant());
        }
        this.foodWanted = (int) OWUtils.generateRandomInterval(8, 15);
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    private KangarooVariant chooseKangarooVariant() {
        int roll = this.random.nextInt(100);
        if (roll < 33) return KangarooVariant.ORANGE;
        if (roll < 66) return KangarooVariant.BROWN;
        return KangarooVariant.DEFAULT;
    }

    private void setupAnimationState() {
        createIdleAnimation(80, true);
        createSitAnimation(80, true);

        setupComboAnimations();
    }

    /**
     * Duree de vie des animations de combo, en ticks. Chacune doit couvrir le geste ENTIER, plus
     * environ un tiers de rabiot pendant lequel il tient sa pose finale et se melange au coup
     * suivant — c'est la marge du crocodile, qui sert de reference. En dessous, l'animation est
     * tranchee avant sa derniere image et l'enchainement saccade.
     *
     * <p>Gestes de 1,2917 / 1,2917 / 1,75 s lus a 1,0 / 1,1 / 1,25 : 25,8 / 23,5 / 28,0 ticks. Le premier, a 24, etait coupe ; le troisieme ignorait le multiplicateur de vitesse.</p>
     */
    private void setupComboAnimations() {
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, (int) (35 / comboSpeedMultiplier));
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, (int) (32 / comboSpeedMultiplier));
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, (int) (50 / comboSpeedMultiplier));
    }

    private void setupComboAnimation(int comboNumber, AnimationState animationState, int timer, int maxTimer) {
        // Le kangourou tait son troisieme coup une fois le quatrieme parti.
        boolean shouldPlay = this.isCombo(comboNumber) && !(comboNumber == 3 && fourthHitFired);
        timer = tickComboAnimation(comboNumber, animationState, timer, maxTimer, shouldPlay);

        switch (comboNumber) {
            case 1: attack1ComboTimer = timer; break;
            case 2: attack2ComboTimer = timer; break;
            case 3: attack3ComboTimer = timer; break;
        }
    }

    public KangarooVariant getVariant() {
        return KangarooVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(KangarooVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
    }

    public void setSkin(KangarooVariant skin) {
        this.setVariant(skin);
    }

    @Override
    public void changeSkin(int skinIndex, boolean playingEffects) {
        super.changeSkin(skinIndex, playingEffects);
        this.setVariant(getInitialVariant());

        switch (skinIndex) {
            case 1 -> this.setSkin(KangarooVariant.Cosmetics.GOLD.variant);
            default -> this.setVariant(getInitialVariant());
        }
    }

    @Override
    public void changeSkinSilent(int skinIndex) {
        changeSkin(skinIndex, false);
    }

    @Override
    public void setVariant(OWEntity entity, int variant) {
        if (entity instanceof KangarooEntity kangaroo) {
            kangaroo.setVariant(KangarooVariant.byId(variant));
            kangaroo.setInitialVariant(KangarooVariant.byId(variant));
        }
    }

    /** Variante naturelle exposée sous forme générique (cf. {@code OWEntity}). */
    @Override
    public int getInitialTypeVariant() { return this.getInitialVariant().getId(); }

    public KangarooVariant getInitialVariant() {
        return KangarooVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(KangarooVariant variant) {
        this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());
    }

    public boolean isWearingBoxingGloves() {
        return this.entityData.get(IS_WEARING_BOXING_GLOVES);
    }

    public void setWearingBoxingGloves(boolean gloves) {
        this.entityData.set(IS_WEARING_BOXING_GLOVES, gloves);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("foodGiven", this.foodGiven);
        tag.putInt("foodWanted", this.foodWanted);
        tag.putBoolean("isWearingBoxingGloves", this.isWearingBoxingGloves());
        tag.putInt("ultimateKillCount", this.getUltimateKillCount());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.entityData.set(IS_WEARING_BOXING_GLOVES, tag.getBoolean("isWearingBoxingGloves"));
        this.entityData.set(ULTIMATE_KILL_COUNT, tag.getInt("ultimateKillCount"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");

        if (this.getSkinIndex() != 0) {
            this.nbtRestoring = true;
            this.changeSkin(this.getSkinIndex(), false);
            this.nbtRestoring = false;
        }
    }
}
