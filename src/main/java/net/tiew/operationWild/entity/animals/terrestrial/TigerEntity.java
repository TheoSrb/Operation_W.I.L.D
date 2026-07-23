package net.tiew.operationWild.entity.animals.terrestrial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.enchantment.OWEnchantments;
import net.tiew.operationWild.entity.IOWWaypointEntity;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.attacks.OWAttacksHandler;
import net.tiew.operationWild.entity.config.*;
import net.tiew.operationWild.entity.goals.NapGoal;
import net.tiew.operationWild.entity.goals.global.OWAttackGoal;
import net.tiew.operationWild.entity.goals.global.OWBreedGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.goals.kodiak.*;
import net.tiew.operationWild.entity.goals.tiger.*;
import net.tiew.operationWild.entity.variants.KodiakVariant;
import net.tiew.operationWild.entity.variants.TigerVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.AnimalSoulItem;
import net.tiew.operationWild.networking.packets.to_client.TigerLeapStatePacket;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.tiew.operationWild.core.OWUtils.RANDOM;

public class TigerEntity extends OWEntity implements IOWEntity, IOWTamable, IOWRideable, IOWGrabberEntity, PlayerRideableJumping {
    // ==================================================
    //              CONSTANTES PRINCIPALES
    // ==================================================

    public static final double TAMING_EXPERIENCE = 185.0;
    public static final int MAX_HIDING_TIMER = 1000;
    public static final int MAX_NO_HIDING_TIMER = 400;
    public static final int DROWSY_FLEE_THRESHOLD = 80;

    private static final int SCRATCHES_DURATION = 60;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_MAD = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_HIDDEN = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_ROARING = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_SCARIFYING = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_EATING = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> IS_GRABBING = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> GRABBED_TARGET_ID = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GRAB_TIMEOUT = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_PLAYER_GRAB = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> GRAB_PUNCH_TIMER = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> IS_SHADOW_STRIKE_ACTIVE = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> SHADOW_STRIKE_KILL_COUNT = SynchedEntityData.defineId(TigerEntity.class, EntityDataSerializers.INT);
    // ==================================================
    //             COMPTEURS ET ANIMATIONS
    // ==================================================

    public final AnimationState attack1Combo = new AnimationState();
    public final AnimationState attack2Combo = new AnimationState();
    public final AnimationState attack3Combo = new AnimationState();
    public final AnimationState napAnimationState = new AnimationState();
    public final AnimationState preparingLeapAnimationState = new AnimationState();
    public final AnimationState leapAnimationState = new AnimationState();
    public final AnimationState scratchesAnimationState = new AnimationState();
    public final AnimationState roaringAnimationState = new AnimationState();
    public final AnimationState scarifyAnimationState = new AnimationState();

    public int attack1ComboTimer = 0;
    public int attack2ComboTimer = 0;
    public int attack3ComboTimer = 0;
    public int napAnimationTimeout = 0;
    public int preparingLeapAnimationTimeout = 0;
    public int leapAnimationTimeout = 0;
    public int roaringAnimationTimeout = 0;
    public int scratchesAnimationStartTime = 0;

    // ==================================================
    //                VARIABLES PROPRES
    // ==================================================

    private static final ResourceLocation SHADOW_STRIKE_DAMAGE_MODIFIER = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "tiger_shadow_strike_damage");

    public boolean wantToScarifyWood = false;
    public boolean goAway = false;

    private int hideCooldown = MAX_NO_HIDING_TIMER + this.random.nextInt(MAX_NO_HIDING_TIMER / 2);
    private int hideDuration = 0;

    public boolean isLeaping = false;
    public boolean isPreparing = false;

    public volatile float bodyAnimY = 0f;

    private boolean isJumpCharging = false;
    private int jumpAttackCooldown = 0;
    private boolean isPlayerLeaping = false;
    private boolean isRidingJump = false;
    protected float playerJumpPendingScale;
    private int leapJumpTimer = 0;

    private int shadowStrikeDurationTimer = 0;
    private int shadowStrikeDamageBonusTimer = 0;

    public boolean isPlayerGrab = false;
    private int playerGrabPunchCooldown = 0;
    private int playerGrabDuration = 0;

    private int roarTimer = 0;
    private int grabDamageTimer = 0;

    private int suspicionLevel = 0;
    private static final int MAX_SUSPICION = 100;

    private int scratchesCooldown = (int) OWUtils.generateRandomInterval(400, 1200);

    // ==================================================
    //            INTÉLLIGENCE ARTIFICIELLE
    // ==================================================

    public TigerEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.18D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D)
                .add(Attributes.JUMP_STRENGTH, 0.9);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new TigerLeapingGoal(this, 4f, 20f));
        this.goalSelector.addGoal(0, new TigerDistractedByFoodGoal(this));

        this.goalSelector.addGoal(1, new TigerDrowsyFleeGoal(this, 6f));
        this.goalSelector.addGoal(2, new TigerMeleeAttackGoal());

        this.goalSelector.addGoal(3, new TigerScarifyTreeGoal(this, 30, 0.7D));

        this.goalSelector.addGoal(4, new TigerSmellBloodGoal(this, 32.0));

        this.goalSelector.addGoal(5, new NapGoal(this, 1f, 800, true));

        this.goalSelector.addGoal(10, new OWBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new RandomStrollGoal(this, 0.8D));

        this.goalSelector.addGoal(11, new OWRandomLookAroundGoal(this));

        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Animal.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Monster.class, true));

        this.lookControl = new LookControl(this) {
            @Override
            public void tick() {
                if (this.mob instanceof TigerEntity tiger && !tiger.isSleeping() && !tiger.isNapping() && tiger.getGrabbedTarget() == null) {
                    super.tick();
                }
            }
        };
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(IS_MAD, false);
        builder.define(IS_HIDDEN, false);
        builder.define(IS_ROARING, false);
        builder.define(IS_SCARIFYING, false);
        builder.define(IS_EATING, false);

        builder.define(IS_GRABBING, false);
        builder.define(GRABBED_TARGET_ID, -1);
        builder.define(GRAB_TIMEOUT, 0);
        builder.define(IS_PLAYER_GRAB, false);
        builder.define(GRAB_PUNCH_TIMER, 0);
        builder.define(IS_SHADOW_STRIKE_ACTIVE, false);
        builder.define(SHADOW_STRIKE_KILL_COUNT, 0);
    }


    // ==================================================
    //             MÉTHODES PRINCIPALES
    // ==================================================

    @Override
    public int getEntityColor() {
        return 14251827;
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
        return OWEntityConfig.Archetypes.ASSASSIN;
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
        return List.of(Animal.class, Player.class);
    }

    @Override
    public float vehicleRunSpeedMultiplier() {
        return isShadowStrikeActive()
                ? 5f * OWAttacksConstants.Tiger.SHADOW_STRIKE_SPEED_FACTOR
                : 5f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return isShadowStrikeActive()
                ? 2f * OWAttacksConstants.Tiger.SHADOW_STRIKE_SPEED_FACTOR
                : 2f;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return 3f;
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
        int max = this.level().isNight() ? 400 : 300;
        return max * (1 + ((float) this.getLevel() / 50));
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 1.15f * (1 + ((float) this.getLevel() / 50));
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
    public float getRotationSpeed() {
        return isPreparing ? 1 : 0.35f;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.TIGER.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.TIGER_FOOD);
    }

    @Override
    public int getGrabMaxTimeout() {
        return 300;
    }

    @Override
    public float getScale() {
        return super.getScale() <= 0 ? 1f : super.getScale();
    }

    private long lastStepSoundMs = 0L;

    @Override
    public void playStepSound(BlockPos blockPos, BlockState blockState) {
        // Intentionally empty — replaced by animation callbacks below
    }

    private void playStepSoundFromAnimation(float pitchMod) {
        if (!this.level().isClientSide()) return;
        if (!this.onGround()) return;
        if (this.isInWater()) return;

        if (this.getDeltaMovement().horizontalDistanceSqr() < 0.0001) return;

        long now = System.currentTimeMillis();
        if (now - lastStepSoundMs < 200L) return;
        lastStepSoundMs = now;

        BlockState blockState = this.getBlockStateOn();
        if (blockState.isAir()) return;

        BlockPos pos = this.blockPosition();
        SoundType soundtype = blockState.getSoundType(this.level(), pos, this);

        for (int i = 0; i < 7; i++) {
            this.level().playLocalSound(
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    soundtype.getStepSound(),
                    this.getSoundSource(),
                    soundtype.getVolume() * 0.15F,
                    soundtype.getPitch() * pitchMod,
                    false
            );
        }
    }

    /**
     * Called by TigerModel (render thread) when the left paw touches the ground.
     * Plays the step sound of the block under the tiger with a slightly lower pitch.
     */
    public void onLeftFootDown() {
        playStepSoundFromAnimation(0.9f);
    }

    /**
     * Called by TigerModel (render thread) when the right paw touches the ground.
     * Plays the step sound of the block under the tiger with a slightly higher pitch.
     */
    public void onRightFootDown() {
        playStepSoundFromAnimation(1.1f);
    }


    // ==================================================
    //             CORPS DU FONCTIONNEMENT
    // ==================================================

    @Override
    public void tick() {
        super.tick();

        // ------------ FONCTIONNEMENT GLOBAL ------------

        if (jumpAttackCooldown > 0) jumpAttackCooldown--;
        if (isJumpCharging && !this.isVehicle()) cancelJumpCharge();
        if (!this.level().isClientSide() && this.entityData.get(GRAB_PUNCH_TIMER) > 0)
            this.entityData.set(GRAB_PUNCH_TIMER, this.entityData.get(GRAB_PUNCH_TIMER) - 1);

        if (!this.level().isClientSide()) {
            // Phase cachée : décompte et révélation automatique si le timer expire sans coup
            if (isShadowStrikeActive()) {
                shadowStrikeDurationTimer--;
                if (shadowStrikeDurationTimer <= 0) revealShadowStrike(false);
            }
            // Bonus dégâts post-révélation : retire le modificateur quand il expire
            if (shadowStrikeDamageBonusTimer > 0) {
                shadowStrikeDamageBonusTimer--;
                if (shadowStrikeDamageBonusTimer == 0) {
                    var dmgAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);
                    if (dmgAttr != null) dmgAttr.removeModifier(SHADOW_STRIKE_DAMAGE_MODIFIER);
                }
            }
        }

        // Landing detection — reset isLeaping when player-triggered jump ends (ground or water)
        if (isPlayerLeaping) {
            leapJumpTimer++;
            boolean landed = leapJumpTimer > 5 && this.onGround();
            boolean hitWater = this.isInWater();
            // Le bond saisit sa proie en plein vol : c'est le trajet qui accroche, pas l'atterrissage.
            // Le délai de deux ticks évite d'agripper ce qui se tenait déjà collé au tigre au départ.
            boolean caughtMidAir = false;
            if (!this.level().isClientSide() && !landed && !hitWater
                    && leapJumpTimer >= 2 && !this.isGrabbing()) {
                tryPlayerGrab();
                caughtMidAir = this.isGrabbing();
            }
            if (landed || hitWater || caughtMidAir) {
                isPlayerLeaping = false;
                isRidingJump = false;
                leapJumpTimer = 0;
                this.isLeaping = false;
                if (!this.level().isClientSide()) {
                    syncLeapState(false, false);
                    if (landed && !this.isGrabbing()) tryPlayerGrab();
                }
            }
        } else {
            leapJumpTimer = 0;
        }

        if (!this.isGrabbing()) createCombo(16, 10, getVariant() != TigerVariant.Cosmetics.VIRUS.variant ? OWSounds.TIGER_HURTING.get() : OWSounds.TIGER_HURTING_VIRUS.get(), 3.0, 3.5, 1.5, actualAttackNumber == 2, actualAttackNumber == 2 ? 2 : 0);
        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        if (this.isVehicle() && this.isTame() && !this.isSitting()) setMad(this.isCombo() || (this.isLeaping && !isRidingJump));

        // ------------ FONCTIONNEMENT PROPRE ------------

        if (!this.level().isClientSide()) {
            handleCamouflage();
            handleRoar();
        }

        handleGrab();

        if (tickCount % 800 == 0 && !isBaby() && !isNapping()) this.wantToScarifyWood = true;

        if (this.isNapping()) {
            if (this.tickCount % 100 == 0) this.heal(4);

            // Mécanique de détection du bruit pendant le sommeil
            if (!this.level().isClientSide()) {
                handleNoiseSuspicion();
            }
        } else {
            // Si le tigre est réveillé pour une autre raison, on réinitialise la suspicion
            if (this.suspicionLevel > 0) {
                this.suspicionLevel = 0;
            }
        }
    }

    public void releaseGrab() {
        LivingEntity grabbed = this.getGrabbedTarget();
        if (grabbed != null) {
            boolean stillHeldByOther = this.level().getEntitiesOfClass(TigerEntity.class,
                            grabbed.getBoundingBox().inflate(8))
                    .stream()
                    .anyMatch(t -> t != this && t.isGrabbing() && t.getGrabbedTarget() == grabbed);
            if (!stillHeldByOther) {
                grabbed.noPhysics = false;
                if (grabbed.getVehicle() == this) grabbed.stopRiding();
            }
        }
        this.setGrabbing(false, null);
        this.setGrabTimeout(0);
        grabDamageTimer = 0;
        isPlayerGrab = false;
        playerGrabPunchCooldown = 0;
        playerGrabDuration = 0;
        this.entityData.set(IS_PLAYER_GRAB, false);
        this.entityData.set(GRAB_PUNCH_TIMER, 0);
        this.setTarget(null);
    }

    private void tryPlayerGrab() {
        if (isGrabbing()) return;
        float radius = this.getBbWidth() + 1.5f;
        List<LivingEntity> candidates = this.level().getEntitiesOfClass(
                LivingEntity.class, this.getBoundingBox().inflate(radius));
        for (LivingEntity rawTarget : candidates) {
            LivingEntity target = rawTarget instanceof BoaTailPart tailPart
                    && tailPart.getParent() instanceof BoaEntity boaHead ? boaHead : rawTarget;
            if (!isEnemyForOwner(target)) continue;
            if (!canGrabEntity(target)) continue;
            if (target.getVehicle() != null) target.stopRiding();
            setGrabbing(true, target);
            if (!isGrabbing()) continue;

            boolean targetIsPlayer = target instanceof Player;
            boolean forcePlayerGrab = this.isTame() && this.isVehicle();
            isPlayerGrab = forcePlayerGrab || !targetIsPlayer;
            this.entityData.set(IS_PLAYER_GRAB, forcePlayerGrab || !targetIsPlayer);
            this.entityData.set(GRAB_PUNCH_TIMER, 0);
            return;
        }
    }

    private boolean isEnemyForOwner(LivingEntity target) {
        if (target == this.getFirstPassenger()) return false;
        LivingEntity owner = this.getOwner();
        if (owner != null && target == owner) return false;
        if (target instanceof TamableAnimal tamed && tamed.isOwnedBy(owner)) return false;
        // Ne pas cibler un allié de la tribu (joueur membre, ou entité de la tribu / d'un membre).
        if (this.isTameGrabAlly(target)) return false;
        return true;
    }

    public void playerGrabPunch() {
        if (!isGrabbing() || !isPlayerGrab) return;
        if (playerGrabPunchCooldown > 0) return;
        LivingEntity grabbed = getGrabbedTarget();
        if (grabbed == null) return;
        grabbed.invulnerableTime = 0;
        grabbed.hurt(this.damageSource, this.getDamage() / 6f);
        playerGrabPunchCooldown = 10;
        this.entityData.set(GRAB_PUNCH_TIMER, 8);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.TIGER_HURTING.get(), SoundSource.AMBIENT, 1.5f,
                (float) OWUtils.generateRandomInterval(0.9, 1.1));
    }

    public void disarmTarget(LivingEntity target) {
        if (target == null) return;
        if (this.level().isClientSide()) return;

        ItemStack itemstack = target.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemstack.isEmpty()) return;

        ItemStack toDrop = itemstack.copy();
        target.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

        ItemEntity dropped = new ItemEntity(
                this.level(),
                target.getX(),
                target.getY() + 0.5,
                target.getZ(),
                toDrop
        );
        dropped.setPickUpDelay(40);
        this.level().addFreshEntity(dropped);

        if (this.level() instanceof ServerLevel serverLevel) {
            float pitch = ((serverLevel.random.nextFloat() - serverLevel.random.nextFloat()) * 0.7F + 1.0F) * 2.0F;
            serverLevel.playSound(null, target, SoundEvents.ITEM_BREAK, SoundSource.NEUTRAL, 1.0F, pitch);
            serverLevel.playSound(null, target, SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 1.0F, (float) (pitch * 0.75));
        }
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != null && isNapping()) {
            return;
        }

        if (target != null && !isTame() && getSleepBarPercent() >= DROWSY_FLEE_THRESHOLD) {
            return;
        }

        if (target != null && this.level().isDay() && this.distanceTo(target) > 25.0) {
            return;
        }

        if (!isTame()) {
            setMad(!isBaby() && target != null && getSleepBarPercent() < 75 && !this.isSitting());
        }

        if (target == null) {
            this.getPersistentData().remove("provokedBy");
        }

        super.setTarget(target);
    }

    public float getRiddenSpeedVehicle(Player player) {
        return this.isImmobile() || this.isPreparing ? 0 : super.getRiddenSpeedVehicle(player);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide()) {
            ItemStack stack = player.getItemInHand(hand);

            if (this.isSleeping() && !this.isTame() && stack.is(OWTags.Items.TIGER_FOOD)) {
                if (!player.isCreative()) stack.shrink(1);
                this.playSound(SoundEvents.CAMEL_EAT, 1.0f, (float) OWUtils.generateRandomInterval(0.9, 1.1));
                this.foodGiven++;

                if (this.foodGiven >= this.foodWanted) {
                    this.setTame(true, player);
                    this.setSleeping(false);
                    this.resetSleepBar();
                    this.foodGiven = foodWanted;
                }

                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void die(DamageSource damageSource) {
        if (this.isGrabbing()) releaseGrab();
        super.die(damageSource); // le drop générique de l'Âme est géré par OWEntity.die()

        if (this.isSaddled()) {
            this.spawnAtLocation(acceptSaddle());
        }
    }

    /** Le tigre nage, mais ne se bat pas sous l'eau : duels terrestres uniquement. */
    @Override
    public int arenaTerrainMask() {
        return net.tiew.operationWild.core.OWArena.Terrain.TERRESTRIAL.bit();
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof TigerEntity otherTiger) {
            if (otherTiger.isBaby()) {
                return true;
            }
            if (this.isTame()) {
                if (otherTiger.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherTiger.getOwnerUUID())) {
                    return true;
                }
            } else if (!otherTiger.isTame()) {
                return true;
            }
        }
        return super.isAlliedTo(entity);
    }


    @Override
    public boolean hurt(DamageSource damageSource, float v) {
        if (!this.isTame() && this.isSitting()) {
            this.setSitting(false);
        }

        if (damageSource.getEntity() instanceof LivingEntity attacker) {
            this.getPersistentData().putInt("last_hurt_tick", this.tickCount);

            var tag = this.getPersistentData();
            if (this.getTarget() == null) {
                tag.putUUID("provokedBy", attacker.getUUID());
            }
        }

        return super.hurt(damageSource, v);
    }

    @Override
    public void hurtAfterCombo(LivingEntity entity, int comboAttack) {
        super.hurtAfterCombo(entity, comboAttack);
    }

    @Override
    protected void onSuccessfulHit(LivingEntity entity) {
        // Désarmement : le tigre sauvage le fait naturellement ; le tigre apprivoisé seulement
        // s'il a débloqué le passif « Griffe Désarmante » sur la Piste. 1 chance sur 7 (comme sauvage).
        boolean canDisarm = !this.isTame()
                || net.tiew.operationWild.entity.attacks.OWPistePassives.has(this, net.tiew.operationWild.entity.attacks.OWPistePassives.DISARM);
        if (canDisarm && RANDOM(7)) disarmTarget(entity);

        if (isShadowStrikeActive() && !this.level().isClientSide() && !entity.isDeadOrDying()) {
            float hpRatio = entity.getHealth() / entity.getMaxHealth();
            if (hpRatio <= OWAttacksConstants.Tiger.PREDATOR_THRESHOLD) {
                entity.kill();
                if (this.level() instanceof ServerLevel sl) {
                    this.killedEntity(sl, entity);
                }
            }
        }
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity target) {
        int kills = getShadowStrikeKillCount();
        if (kills < OWAttacksConstants.Tiger.SHADOW_STRIKE_KILLS_REQUIRED) {
            setShadowStrikeKillCount(kills + 1);
        }
        return super.killedEntity(level, target);
    }

    @Override
    protected boolean isImmobile() {
        return this.isRoaring() || this.isJumpCharging || this.isPlayerGrab || this.isGrabbing();
    }

    @Override
    protected boolean isLeapingVehicle() {
        return this.isLeaping;
    }

    private InteractionHand getHandWithItem(LivingEntity entity) {
        if (!entity.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            return InteractionHand.MAIN_HAND;
        }
        if (!entity.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
            return InteractionHand.OFF_HAND;
        }

        return null;
    }


    /**
     * Méthode permettant de gérer la logique de camouflage aléatoire du Tigre.
     */
    private void handleCamouflage() {
        if (this.isTame() || this.isBaby() || this.isNapping() || this.isSleeping() || (this.getTarget() != null && this.level().isDay())) {
            // Ne pas forcer la révélation si le Shadow Strike est actif
            if (this.isHidden() && !isShadowStrikeActive()) this.setHidden(false);
            return;
        }

        if (this.isHidden()) {
            hideDuration--;
            if (hideDuration <= 0) {
                this.setHidden(false);
                hideCooldown = MAX_HIDING_TIMER + this.random.nextInt(MAX_HIDING_TIMER / 2);
            }
        } else {
            if (hideCooldown > 0) {
                hideCooldown--;
            } else {
                this.setHidden(true);
                hideDuration = MAX_NO_HIDING_TIMER + this.random.nextInt(MAX_NO_HIDING_TIMER / 2);
            }
        }
    }

    /**
     * Méthode permettant de gérer la logique d'attrapage.
     */
    private void handleGrab() {
        if (!this.isGrabbing() || this.isBaby()) return;

        LivingEntity grabbed = this.getGrabbedTarget();

        if (this.level().isClientSide()) {
            if (grabbed != null && (!isPlayerGrab || this.entityData.get(GRAB_PUNCH_TIMER) > 0)) {
                this.setLookAt(grabbed.getX(), grabbed.getY(), grabbed.getZ());
            }
            return;
        }

        if (grabbed == null || !grabbed.isAlive() || (grabbed instanceof Player p && p.isCreative())) {
            releaseGrab();
            return;
        }

        if (!isPlayerGrab || this.entityData.get(GRAB_PUNCH_TIMER) > 0) {
            this.getLookControl().setLookAt(grabbed, 30f, 30f);
            this.setLookAt(grabbed.getX(), grabbed.getY(), grabbed.getZ());
        }

        grabbed.noPhysics = true;

        // Freeze tiger movement during player-initiated grab + auto-release after 5 s (100 ticks)
        if (isPlayerGrab && !this.level().isClientSide()) {
            Vec3 cur = this.getDeltaMovement();
            this.setDeltaMovement(0, cur.y, 0);
            if (playerGrabPunchCooldown > 0) playerGrabPunchCooldown--;

            playerGrabDuration++;
            if (playerGrabDuration >= 100) {
                releaseGrab();
                return;
            }
        }

        if (grabbed instanceof Player) {
            if (!this.level().isClientSide()) {
                this.setGrabTimeout(this.getGrabTimeout() + 2);
                if (this.getGrabTimeout() >= getGrabMaxTimeout()) {
                    grabbed.kill();
                    releaseGrab();
                    return;
                }

                if (!isPlayerGrab) {
                    grabDamageTimer++;
                    if (grabDamageTimer >= 10) {
                        grabDamageTimer = 0;
                        grabbed.invulnerableTime = 0;
                        grabbed.hurt(this.damageSource, this.getDamage() * 0.2f);
                    }
                }
            }
            if (!grabbed.isPassenger()) {
                grabbed.startRiding(this, true);
            }
        } else {
            Vec3 look = this.getLookAngle();
            double targetX = this.getX() + look.x * 1.5f;
            double targetY = this.getY();
            double targetZ = this.getZ() + look.z * 1.5f;

            float smoothing = 0.2f;
            double newX = Mth.lerp(smoothing, grabbed.getX(), targetX);
            double newY = Mth.lerp(smoothing, grabbed.getY(), targetY);
            double newZ = Mth.lerp(smoothing, grabbed.getZ(), targetZ);

            grabbed.setDeltaMovement(
                    newX - grabbed.getX(),
                    newY - grabbed.getY(),
                    newZ - grabbed.getZ()
            );
            grabbed.setPos(newX, newY, newZ);

            if (!this.level().isClientSide() && !isPlayerGrab) {
                grabDamageTimer++;
                if (grabDamageTimer >= 8) {
                    grabDamageTimer = 0;
                    grabbed.invulnerableTime = 0;
                    grabbed.hurt(this.damageSource, this.getDamage() * 0.075f);
                }
            }
        }
    }

    /**
     * Méthode permettant de simuler la mécanique de détection de bruit durant le sommeil du Tigre.
     */
    private void handleNoiseSuspicion() {
        List<Player> nearbyPlayers = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(12.0D));
        boolean heardNoise = false;
        Player noisyPlayer = null;

        for (Player player : nearbyPlayers) {
            if (!player.isSpectator() && !player.isCreative()) {
                // Si le joueur ne s'accroupit pas (sneaking)
                if (!player.isCrouching()) {
                    if (!player.isCrouching() &&
                            (player.walkDist != player.walkDistO || player.swinging || player.isSprinting())) {
                        heardNoise = true;
                        noisyPlayer = player;
                        break;
                    }
                }
            }
        }

        if (heardNoise) {
            this.suspicionLevel += 2;

            // Avertissement sonore à la moitié de la jauge
            if (this.suspicionLevel == MAX_SUSPICION / 2) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), getVariant() != TigerVariant.Cosmetics.VIRUS.variant ? OWSounds.TIGER_3.get() : OWSounds.TIGER_3_VIRUS.get(), SoundSource.HOSTILE, 1.0f, 0.8f);
            }

            // Réveil !
            if (this.suspicionLevel >= MAX_SUSPICION) {
                this.suspicionLevel = 0;

                this.setNap(false);
            }
        } else if (this.suspicionLevel > 0) {
            this.suspicionLevel -= 0.1;
        }
    }

    /**
     * Méthode permettant de gérer la logique de hurlement du Tigre.
     */
    private void handleRoar() {
        if (this.isRoaring()) {
            this.roarTimer++;

            if (this.getTarget() != null) {
                this.getLookControl().setLookAt(this.getTarget(), 30f, 30f);
                this.setLookAt(this.getTarget().getX(), this.getTarget().getY(), this.getTarget().getZ());
            }

            if (this.roarTimer == 15) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), getVariant() != TigerVariant.Cosmetics.VIRUS.variant ? OWSounds.TIGER_ROAR.get() : OWSounds.TIGER_ROAR_VIRUS.get(), SoundSource.AMBIENT, 3.0f, (float) OWUtils.generateRandomInterval(0.9, 1.1));

                double radius = 15.0D;
                List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(
                        LivingEntity.class,
                        this.getBoundingBox().inflate(radius)
                );

                for (LivingEntity entity : nearbyEntities) {
                    if (entity.isAlive() && entity != this && !this.isAlliedTo(entity)) {
                        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0));

                        if (entity == this.getTarget()) {
                            if (this.getTarget() != null) {
                                this.getTarget().addEffect(new MobEffectInstance(OWEffects.FEAR_EFFECT.getDelegate(), 100, 0));
                            }
                        }

                    }
                }
            }

            if (this.roarTimer >= 15 && this.roarTimer < 50) {
                this.heal(0.1f);
            }

            if (this.roarTimer >= 60) {
                this.roarTimer = 0;
                this.setRoar(false);
            }
        }
    }


    /**
     * Démarre la charge du saut joueur : immobilise le tigre et joue l'animation PREPARING.
     * Appelé côté serveur via OWAttackPacket(1).
     */
    public void startJumpCharge() {
        if (isJumpCharging) return;
        isJumpCharging = true;  // toujours enregistrer la charge pour que performJumpAttack s'exécute
        if (jumpAttackCooldown > 0) return;  // cooldown désynchronisé côté serveur : charge acceptée sans animation
        this.isPreparing = true;
        this.setDeltaMovement(0, 0, 0);
        this.getNavigation().stop();
        syncLeapState(false, true);
    }

    /**
     * Annule la charge (relâchement trop court ou démontage). Appelé via OWAttackPacket(2).
     */
    public void cancelJumpCharge() {
        isJumpCharging = false;
        this.isPreparing = false;
        syncLeapState(false, false);
    }

    @Override
    public void onPlayerJump(int jumpPower) {
        if (!this.isSaddled()) return;
        if (jumpPower < 0) jumpPower = 0;
        this.playerJumpPendingScale = jumpPower >= 90 ? 1.0F : 0.4F + 0.4F * jumpPower / 90.0F;
    }

    @Override
    public boolean canJump() {
        return this.isSaddled();
    }

    @Override
    public void handleStartJump(int jumpPower) {
    }

    @Override
    public void handleStopJump() {
    }

    @Override
    public void tickRidden(Player player, Vec3 travelVector) {
        super.tickRidden(player, travelVector);
        if (this.isControlledByLocalInstance() && this.onGround()) {
            if (this.playerJumpPendingScale > 0.0F && !this.isLeaping && !this.isPreparing && !this.isGrabbing()) {
                executeRidersJump(this.playerJumpPendingScale);
            }
            this.playerJumpPendingScale = 0.0F;
        }
    }

    private void executeRidersJump(float scale) {
        double verticalPower = this.getAttributeValue(Attributes.JUMP_STRENGTH) * scale * 0.80
                * (double) this.getBlockJumpFactor()
                + (double) (this.getJumpBoostPower() * 3);

        Vec3 lookFlat = this.getLookAngle().multiply(1, 0, 1);
        if (lookFlat.lengthSqr() > 1.0E-7D) lookFlat = lookFlat.normalize();

        Vec3 movement = this.getDeltaMovement();
        this.setDeltaMovement(
                movement.x + lookFlat.x * 0.5 * scale,
                verticalPower,
                movement.z + lookFlat.z * 0.5 * scale
        );
        this.hasImpulse = true;
        CommonHooks.onLivingJump(this);

        playJumpParticles();

        if (scale >= 1.0f) {
            this.level().playLocalSound(getX(), getY(), getZ(),
                    getVariant() != TigerVariant.Cosmetics.VIRUS.variant ? OWSounds.TIGER_IDLE.get() : OWSounds.TIGER_IDLE_VIRUS.get(),
                    SoundSource.AMBIENT, 1.5f, (float) OWUtils.generateRandomInterval(0.9, 1.1), false);
        }

        this.isLeaping = true;
        this.isPlayerLeaping = true;
        this.isRidingJump = true;
    }

    public void setPlayerLeaping(boolean value) {
        isPlayerLeaping = value;
        if (!value) leapJumpTimer = 0;
    }

    public void playJumpSound() {
        this.level().playLocalSound(getX(), getY(), getZ(),
                getVariant() != TigerVariant.Cosmetics.VIRUS.variant ? OWSounds.TIGER_JUMP.get() : OWSounds.TIGER_JUMP_VIRUS.get(),
                SoundSource.AMBIENT, 3.0f, (float) OWUtils.generateRandomInterval(0.9, 1.1), false);
    }

    public void playJumpParticles() {
        for (int i = 0; i < 10; i++) {
            this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    getX() + (random.nextDouble() - 0.5) * getBbWidth(),
                    getY() + 0.1,
                    getZ() + (random.nextDouble() - 0.5) * getBbWidth(),
                    (random.nextDouble() - 0.5) * 0.1,
                    0.05,
                    (random.nextDouble() - 0.5) * 0.1);
        }
    }

    public void playShadowStrikeSound() {
        this.level().playLocalSound(getX(), getY(), getZ(),
                getVariant() != TigerVariant.Cosmetics.VIRUS.variant ? OWSounds.TIGER_ROAR.get() : OWSounds.TIGER_ROAR_VIRUS.get(),
                SoundSource.AMBIENT, 2.5f, (float) OWUtils.generateRandomInterval(0.9, 1.1), false);
    }

    // ==================================================
    //               SHADOW STRIKE (ultime)
    // ==================================================

    public void activateShadowStrike() {
        if (getShadowStrikeKillCount() < OWAttacksConstants.Tiger.SHADOW_STRIKE_KILLS_REQUIRED) return;

        float cost = OWAttacksConstants.Tiger.SHADOW_STRIKE_ENERGY;
        if (getVitalEnergy() > getMaxVitalEnergy() - cost) {
            canShowVitalEnergyLack = true;
            return;
        }
        setVitalEnergy(0);

        setShadowStrikeKillCount(0);
        setShadowStrikeActive(true);
        shadowStrikeDurationTimer = OWAttacksConstants.Tiger.SHADOW_STRIKE_DURATION_TICKS;
        this.setHidden(true);

        // Bonus dégâts actif dès l'activation pour toute la durée de l'ultime
        shadowStrikeDamageBonusTimer = OWAttacksConstants.Tiger.SHADOW_STRIKE_DURATION_TICKS;
        var dmgAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmgAttr != null) {
            dmgAttr.removeModifier(SHADOW_STRIKE_DAMAGE_MODIFIER);
            dmgAttr.addOrUpdateTransientModifier(new AttributeModifier(
                    SHADOW_STRIKE_DAMAGE_MODIFIER,
                    OWAttacksConstants.Tiger.SHADOW_STRIKE_DAMAGE_BONUS,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }

        // Particules wow effect côté serveur
        OWUtils.spawnServerParticles(this, ParticleTypes.FLASH, 0, 0.5, 0, 1, 0);
        OWUtils.spawnServerParticles(this, ParticleTypes.END_ROD, 1.5, 0.75, 1.5, 20, 0.2);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                getVariant() != TigerVariant.Cosmetics.VIRUS.variant
                        ? OWSounds.TIGER_ROAR.get() : OWSounds.TIGER_ROAR_VIRUS.get(),
                SoundSource.AMBIENT, 1.5f,
                (float) OWUtils.generateRandomInterval(0.9, 1.1));
    }

    /**
     * Révèle le Tigre à la fin du Shadow Strike.
     * @param applyDamageBonus true si révélé par un coup (bonus dégâts actif pour le temps restant),
     *                         false si le timer a expiré naturellement.
     */
    private void revealShadowStrike(boolean applyDamageBonus) {
        int remainingTicks = shadowStrikeDurationTimer;
        setShadowStrikeActive(false);
        shadowStrikeDurationTimer = 0;
        this.setHidden(false);

        if (applyDamageBonus) {
            // Durée du bonus = temps restant (minimum 1 seconde)
            shadowStrikeDamageBonusTimer = Math.max(remainingTicks, 20);
            var dmgAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);
            if (dmgAttr != null) {
                dmgAttr.removeModifier(SHADOW_STRIKE_DAMAGE_MODIFIER);
                dmgAttr.addOrUpdateTransientModifier(new AttributeModifier(
                        SHADOW_STRIKE_DAMAGE_MODIFIER,
                        OWAttacksConstants.Tiger.SHADOW_STRIKE_DAMAGE_BONUS,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }
    }

    /**
     * Exécute le saut joueur après une charge valide (≥ 1s).
     * @param chargeFactor 0.0 = charge minimale (1s), 1.0 = charge maximale (3s)
     */
    public void performJumpAttack(float chargeFactor) {
        if (!isJumpCharging) return;

        isJumpCharging = false;

        float energyRequired = OWAttacksConstants.Tiger.JUMP_ENERGY;
        if (getVitalEnergy() > getMaxVitalEnergy() - energyRequired) {
            canShowVitalEnergyLack = true;
            cancelJumpCharge();
            return;
        }
        setVitalEnergy(getVitalEnergy() + energyRequired);

        jumpAttackCooldown = OWAttacksConstants.Tiger.JUMP_ATTACK_COOLDOWN_TICKS;

        Vec3 lookDir;
        Entity rider = this.getFirstPassenger();
        if (rider instanceof LivingEntity living) {
            Vec3 look = living.getLookAngle();
            lookDir = new Vec3(look.x, 0, look.z);
        } else {
            lookDir = this.getLookAngle().multiply(1, 0, 1);
        }
        if (lookDir.lengthSqr() > 1.0E-7D) {
            lookDir = lookDir.normalize();
        }

        this.setYRot(-(float) (Mth.atan2(lookDir.x, lookDir.z)) * Mth.RAD_TO_DEG);
        this.yBodyRot = this.getYRot();

        this.isPreparing = false;
        this.isLeaping = true;
        this.isPlayerLeaping = true;
        this.leapJumpTimer = 0;

        OWUtils.spawnServerParticles(this, ParticleTypes.CAMPFIRE_COSY_SMOKE, 0.5, 0.1, 0.5, 10, 1);
        // Exclure le rider : il entend le son via playJumpSound() dans localEffect (côté client, sans latence)
        Player riderPlayer = rider instanceof Player p ? p : null;
        this.level().playSound(riderPlayer, this.getX(), this.getY(), this.getZ(),
                getVariant() != TigerVariant.Cosmetics.VIRUS.variant ? OWSounds.TIGER_JUMP.get() : OWSounds.TIGER_JUMP_VIRUS.get(),
                SoundSource.AMBIENT, 3.0f, (float) OWUtils.generateRandomInterval(0.9, 1.1));

        syncLeapState(true, false);
    }

    private void syncLeapState(boolean leaping, boolean preparing) {
        if (this.level() instanceof ServerLevel serverLevel) {
            for (net.minecraft.server.level.ServerPlayer sp : serverLevel.players()) {
                net.tiew.operationWild.networking.OWNetworkHandler.sendToClient(
                        new TigerLeapStatePacket(this.getId(), leaping, preparing), sp);
            }
        }
    }

    /**
     * Fait bondir le tigre vers sa cible.
     * @param maxDistance distance max de projection (plus c'est grand, plus le bond est loin)
     * @param verticalBoost boost vertical supplémentaire
     */
    public void leapToTarget(LivingEntity target, float maxDistance, float verticalBoost) {
        if (target == null || !target.isAlive()) return;

        double dist = this.distanceTo(target);

        Vec3 vec = target.position().subtract(this.position());
        this.setYRot(-((float) Mth.atan2(vec.x, vec.z)) * Mth.RAD_TO_DEG);
        this.yBodyRot = this.getYRot();

        Vec3 direction = new Vec3(target.getX() - this.getX(), 0.0D, target.getZ() - this.getZ());
        if (direction.lengthSqr() > 1.0E-7D) {
            direction = direction.normalize().scale(Math.min(dist, maxDistance) * 0.2F);
        }

        float clampedVertical = (float) Mth.clamp(target.getEyeY() - this.getY(), 0, 2);
        this.setDeltaMovement(direction.x, direction.y + verticalBoost + 0.1F * clampedVertical, direction.z);
        this.hasImpulse = true;

        OWUtils.spawnServerParticles(this, ParticleTypes.CAMPFIRE_COSY_SMOKE, 0.5, 0.1, 0.5, 10, 1);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), getVariant() != TigerVariant.Cosmetics.VIRUS.variant ? OWSounds.TIGER_JUMP.get() : OWSounds.TIGER_JUMP_VIRUS.get(), SoundSource.AMBIENT, 3.0f, (float) OWUtils.generateRandomInterval(0.9, 1.1));
    }

    @Override
    protected double getBaseRiderYOffset() {
        return this.getBbHeight() * 0.6 * this.getScale();
    }

    @Override
    protected float getRiderAnimYOffset() {
        return -bodyAnimY / 16.0f * this.getScale();
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        if (isPlayerGrab && isGrabbing()) return null;
        return super.getControllingPassenger();
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction function) {
        if (this.getGrabbedTargetId() == passenger.getId()) {
            Vec3 look = this.getLookAngle();
            function.accept(passenger, this.getX() + look.x * 2.65f, this.getY() - 1.0, this.getZ() + look.z * 2.65f);
            return;
        }
        if (!this.hasPassenger(passenger) || this.touchingUnloadedChunk()) return;

        Vec3 seatOffset = new Vec3(0, 0, 0).yRot((float) Math.toRadians(-this.yBodyRot));
        double baseY  = getBaseRiderYOffset();
        float  animY  = getRiderAnimYOffset();
        double riderY = this.getY() + baseY + animY;

        passenger.fallDistance = 0f;
        function.accept(passenger, this.getX() + seatOffset.x, riderY, this.getZ() + seatOffset.z);
    }

    @Override
    public void setTame(boolean tame, Player player) {
        super.setTame(tame, player);
        if (tame) {
            this.setMad(false);
        }
    }

    @Nullable
    public LivingEntity getGrabbedTarget() {
        int id = this.entityData.get(GRABBED_TARGET_ID);
        if (id == -1) return null;
        Entity entity = this.level().getEntity(id);
        return entity instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    public boolean canGrabEntity(LivingEntity entity) {
        if (this.isBaby()) return false;
        if (entity instanceof BoaTailPart) return false;
        if (entity.getMaxHealth() > 60f) return false;
        if (entity instanceof OWEntity owEntity && owEntity.getTheoreticalScale() > 5f) return false;
        return true;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));

            this.setVariant(chooseTigerVariant());
            this.setInitialVariant(this.getVariant());
        }
        this.foodWanted = (int) OWUtils.generateRandomInterval(8, 15);
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    private TigerVariant chooseTigerVariant() {
        int roll = this.random.nextInt(100);

        if (roll < 3)  return TigerVariant.WHITE;
        if (roll < 15) return TigerVariant.GOLDEN;
        if (roll < 40) return TigerVariant.LIGHT_ORANGE;
        return TigerVariant.DEFAULT;
    }

    @Override
    public void onSyncedDataUpdated(net.minecraft.network.syncher.EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (OWEntity.NAPPING.equals(accessor) && !isNapping() && !isSleeping() && this.level().isClientSide()) {
            napAnimationTimeout = 0;
            napAnimationState.stop();
        }
    }

    // ==================================================
    //                   ANIMATIONS
    // ==================================================

    private void setupAnimationState() {
        createIdleAnimation(80, true);
        createSitAnimation(83, true);

        handleMiscIdleAnimations();

        if (this.isNapping() || this.isSleeping()) {
            if (this.napAnimationTimeout <= 0) {
                this.napAnimationTimeout = 64;
                this.napAnimationState.start(this.tickCount);
            } else --this.napAnimationTimeout;
        }

        if (!this.isNapping() && !this.isSleeping()) {
            this.napAnimationTimeout = 0;
            this.napAnimationState.stop();
        }

        if (this.isRoaring()) {
            if (this.roaringAnimationTimeout <= 0) {
                this.roaringAnimationTimeout = 90;
                this.roaringAnimationState.start(this.tickCount);
            } else --this.roaringAnimationTimeout;
        }

        if (!this.isRoaring()) {
            this.roaringAnimationTimeout = 0;
            this.roaringAnimationState.stop();
        }

        boolean shouldPreparePose = this.isPreparing || this.isGrabbing();
        if (shouldPreparePose) {
            if (this.preparingLeapAnimationTimeout <= 0) {
                this.preparingLeapAnimationTimeout = 20;
                this.preparingLeapAnimationState.start(this.tickCount);
            } else --this.preparingLeapAnimationTimeout;
        }
        if (!shouldPreparePose) {
            this.preparingLeapAnimationTimeout = 0;
            this.preparingLeapAnimationState.stop();
        }

        if (this.isLeaping && !this.isGrabbing()) {
            if (this.leapAnimationTimeout <= 0) {
                this.leapAnimationTimeout = 40;
                this.leapAnimationState.start(this.tickCount);
            } else --this.leapAnimationTimeout;
        }

        if (!this.isLeaping || this.isGrabbing()) {
            this.leapAnimationTimeout = 0;
            this.leapAnimationState.stop();
        }

        if (isScarifying()) {
            scarifyAnimationState.startIfStopped(this.tickCount);
        } else {
            scarifyAnimationState.stop();
        }

        setupComboAnimations();
    }

    public boolean canPlayIdleAnimation() {
        return this.getTarget() == null && !this.isNapping() && !this.isNapping() && !this.isMoving() && !this.isVehicle() && !this.isInWater();
    }

    public boolean canGrowl() {
        return canPlayIdleAnimation();
    }

    public boolean isAnyIdleAnimationPlaying() {
        return this.scratchesAnimationState.isStarted();
    }

    protected void handleMiscIdleAnimations() {
        if (this.scratchesAnimationState.isStarted() &&
                this.tickCount - scratchesAnimationStartTime > SCRATCHES_DURATION) {
            this.scratchesAnimationState.stop();
        }

        if (scratchesCooldown > 0) {
            scratchesCooldown--;
            return;
        }

        if (this.level().isClientSide) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(),
                    getVariant() != TigerVariant.Cosmetics.VIRUS.variant ? OWSounds.TIGER_3.get() : OWSounds.TIGER_3_VIRUS.get(), this.getSoundSource(),
                    1.0F, isBaby() ? 2.0F : 1.0F, false);
        }

        if (this.canGrowl() && this.canPlayIdleAnimation() && !this.isAnyIdleAnimationPlaying()) {
            this.scratchesAnimationState.start(this.tickCount);
            scratchesAnimationStartTime = this.tickCount;
        }

        scratchesCooldown = (int) OWUtils.generateRandomInterval(400, 800);
    }

    private void setupComboAnimations() {
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, (int) (20 / comboSpeedMultiplier));
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, (int) (20 / comboSpeedMultiplier));
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, (int) (30 / comboSpeedMultiplier));
    }

    private void setupComboAnimation(int comboNumber, AnimationState animationState, int timer, int maxTimer) {
        if (this.isCombo(comboNumber)) {
            if (timer <= 0) {
                timer = maxTimer;
                animationState.start(this.tickCount);
            } else {
                --timer;
            }
        } else {
            if (comboNumber != 3 && timer > 0) {
                --timer;
            } else {
                timer = 0;
                animationState.stop();
            }
        }

        switch (comboNumber) {
            case 1: attack1ComboTimer = timer; break;
            case 2: attack2ComboTimer = timer; break;
            case 3: attack3ComboTimer = timer; break;
        }
    }

    // ==================================================
    //                   ACCESSEURS
    // ==================================================

    @Override
    public void setVariant(OWEntity entity, int variant) {
        if (entity instanceof TigerEntity tiger) {
            tiger.setVariant(TigerVariant.byId(variant));
            tiger.setInitialVariant(TigerVariant.byId(variant));
        }
    }

    public TigerVariant getVariant() {
        return TigerVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(TigerVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
    }

    public void setSkin(TigerVariant skin) {
        this.setVariant(skin);
    }

    @Override
    public void changeSkin(int skinIndex, boolean playingEffects) {
        super.changeSkin(skinIndex, playingEffects);
        this.setVariant(getInitialVariant());

        switch (skinIndex) {
            case 1 -> this.setSkin(TigerVariant.Cosmetics.GOLD.variant);
            case 2 -> this.setSkin(TigerVariant.Cosmetics.BOSS.variant);
            case 3 -> this.setSkin(TigerVariant.Cosmetics.VIRUS.variant);
            case 4 -> this.setSkin(TigerVariant.Cosmetics.SEVEN_SEAS.variant);
            case 5 -> this.setSkin(TigerVariant.Cosmetics.SCARLET_PIRATE.variant);
            case 6 -> this.setSkin(TigerVariant.Cosmetics.CARTOON.variant);
            case 7 -> this.setSkin(TigerVariant.Cosmetics.PIZZA_CHEF.variant);
            default -> this.setVariant(getInitialVariant());
        }
    }

    @Override
    public void changeSkinSilent(int skinIndex) {
        changeSkin(skinIndex, false);
    }

    /** Variante naturelle exposée sous forme générique (cf. {@code OWEntity}). */
    @Override
    public int getInitialTypeVariant() { return this.getInitialVariant().getId(); }

    public TigerVariant getInitialVariant() {
        return TigerVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(TigerVariant variant) {
        this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());
    }

    public void setMad(boolean isMad) {
        if (isMad) if (this.getCurrentMode() == Mode.Passive) return;
        this.entityData.set(IS_MAD, isMad);
    }

    public boolean isMad() { return this.entityData.get(IS_MAD);}

    public void setHidden(boolean isHidden) {
        if (isHidden && isSleeping()) return;
        this.entityData.set(IS_HIDDEN, isHidden);
    }

    public boolean isHidden() { return this.entityData.get(IS_HIDDEN);}

    public void setRoar(boolean isRoaring) {
        this.entityData.set(IS_ROARING, isRoaring);
    }

    public boolean isRoaring() { return this.entityData.get(IS_ROARING);}

    public void setEating(boolean eating) {
        this.entityData.set(IS_EATING, eating);
    }

    public boolean isEating() {
        return this.entityData.get(IS_EATING);
    }

    public void setScarifying(boolean value) {
        this.entityData.set(IS_SCARIFYING, value);
    }

    public boolean isScarifying() {
        return this.entityData.get(IS_SCARIFYING);
    }

    public int getGrabbedTargetId() {
        return this.entityData.get(GRABBED_TARGET_ID);
    }

    public void setGrabbing(boolean isGrabbing, @Nullable LivingEntity entity) {

        if (isGrabbing && entity != null) {
            // Prevent two tigers from grabbing the same target simultaneously
            boolean alreadyGrabbedByOther = this.level().getEntitiesOfClass(TigerEntity.class,
                            entity.getBoundingBox().inflate(8))
                    .stream()
                    .anyMatch(t -> t != this && t.isGrabbing() && t.getGrabbedTarget() == entity);
            if (alreadyGrabbedByOther) return;

            Holder<Enchantment> slidingHolder = entity.level().registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT)
                    .getHolderOrThrow(OWEnchantments.SLIDING);

            int slidingLevel = EnchantmentHelper.getEnchantmentLevel(slidingHolder, entity);

            if (slidingLevel > 0) {
                float chanceToAvoid = 0.25f * slidingLevel;
                if (entity.getRandom().nextFloat() < (1 - chanceToAvoid)) {
                    return;
                }
            }
        }

        this.entityData.set(IS_GRABBING, isGrabbing);
        this.entityData.set(GRABBED_TARGET_ID, entity == null ? -1 : entity.getId());
    }

    public boolean isGrabbing() {
        return this.entityData.get(IS_GRABBING);
    }

    public void setGrabTimeout(int timeout) {
        this.entityData.set(GRAB_TIMEOUT, timeout);
    }

    public int getGrabTimeout() {
        return this.entityData.get(GRAB_TIMEOUT);
    }

    public int getGrabPunchTimer() {
        return this.entityData.get(GRAB_PUNCH_TIMER);
    }

    public boolean isShadowStrikeActive() { return this.entityData.get(IS_SHADOW_STRIKE_ACTIVE); }
    private void setShadowStrikeActive(boolean active) { this.entityData.set(IS_SHADOW_STRIKE_ACTIVE, active); }

    public int getShadowStrikeKillCount() { return this.entityData.get(SHADOW_STRIKE_KILL_COUNT); }
    private void setShadowStrikeKillCount(int count) { this.entityData.set(SHADOW_STRIKE_KILL_COUNT, count); }

    // ==================================================
    //              DONNÉES SAUVEGARDÉES
    // ==================================================

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("foodGiven", this.foodGiven);
        tag.putInt("foodWanted", this.foodWanted);
        tag.putInt("shadowStrikeKillCount", this.getShadowStrikeKillCount());
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
        if (tag.contains("shadowStrikeKillCount")) {
            setShadowStrikeKillCount(tag.getInt("shadowStrikeKillCount"));
        }
        if (this.getSkinIndex() != 0) { this.nbtRestoring = true; this.changeSkin(this.getSkinIndex(), false); this.nbtRestoring = false; }
    }

    @Override
    protected int getDefaultSkinIndex() { return 8; }

    class TigerMeleeAttackGoal extends MeleeAttackGoal {

        public TigerMeleeAttackGoal() {
            super(TigerEntity.this, 7, true);
        }

        @Override
        public boolean canUse() {
            if (!TigerEntity.this.isTame() && TigerEntity.this.getSleepBarPercent() >= DROWSY_FLEE_THRESHOLD) return false;
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            if (!TigerEntity.this.isTame() && TigerEntity.this.getSleepBarPercent() >= DROWSY_FLEE_THRESHOLD) return false;
            return super.canContinueToUse();
        }

        @Override
        public void start() {
            super.start();
            TigerEntity.this.setMad(true);
            TigerEntity.this.setRunning(true);
        }

        @Override
        public void stop() {
            super.stop();
            TigerEntity.this.setMad(false);
            TigerEntity.this.setRunning(false);
        }

        @Override
        protected boolean canPerformAttack(LivingEntity entity) {
            double reach = 2.5;
            return this.isTimeToAttack()
                    && this.mob.distanceToSqr(entity) <= reach * reach
                    && this.mob.getSensing().hasLineOfSight(entity);
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target) {
            if (this.mob.hasEffect(OWEffects.FRACTURE.getDelegate())) return;
            if (!this.canPerformAttack(target)) return;

            if (this.mob instanceof OWEntity owEntity) {
                if (!owEntity.isCombo()) {
                    owEntity.setCombo(true, 1);
                } else if (owEntity.isPauseCombo()) {
                    owEntity.playerContinueCombo = true;
                }
            }
        }

    }
}