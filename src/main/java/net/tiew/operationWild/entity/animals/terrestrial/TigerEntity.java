package net.tiew.operationWild.entity.animals.terrestrial;

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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
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
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.enchantment.OWEnchantments;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
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

public class TigerEntity extends OWEntity implements IOWEntity, IOWTamable, IOWRideable, IOWGrabberEntity {
    // ==================================================
    //              CONSTANTES PRINCIPALES
    // ==================================================

    public static final double TAMING_EXPERIENCE = 185.0;
    public static final int MAX_HIDING_TIMER = 1000;
    public static final int MAX_NO_HIDING_TIMER = 400;

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

    private static final ResourceLocation NIGHT_RANGE_MODIFIER = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "tiger_night_range");

    public boolean wantToScarifyWood = false;
    public boolean goAway = false;

    private int hideCooldown = MAX_NO_HIDING_TIMER + this.random.nextInt(MAX_NO_HIDING_TIMER / 2);
    private int hideDuration = 0;

    public boolean isLeaping = false;
    public boolean isPreparing = false;

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
                .add(Attributes.MAX_HEALTH, 35.0)
                .add(Attributes.MOVEMENT_SPEED, 0.18D)
                .add(Attributes.FOLLOW_RANGE, 25.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new TigerLeapingGoal(this, 4f, 20f));
        this.goalSelector.addGoal(0, new TigerDistractedByFoodGoal(this));

        this.goalSelector.addGoal(1, new OWAttackGoal(this, this.getSpeed() * 32.5f, 8, 2.5, false));

        this.goalSelector.addGoal(2, new TigerScarifyTreeGoal(this, 20, 0.7D));

        this.goalSelector.addGoal(3, new TigerSmellBloodGoal(this, 32.0));

        this.goalSelector.addGoal(4, new NapGoal(this, 1f, 800, true));

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
        return 4.5f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 2;
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
        return 300 * (1 + ((float) this.getLevel() / 50));
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
        return 0.35f;
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

    // ==================================================
    //             CORPS DU FONCTIONNEMENT
    // ==================================================

    @Override
    public void tick() {
        super.tick();


        // ------------ FONCTIONNEMENT GLOBAL ------------

        if (!this.isGrabbing()) createCombo(16, 10, OWSounds.TIGER_HURTING.get(), 3.0, 3.5, 1.5, actualAttackNumber == 2, actualAttackNumber == 2 ? 2 : 0);
        setTamingPercentage(this.foodGiven, this.foodWanted);

        handleRunningEffects(13, SoundEvents.HORSE_STEP, 0.75f, new int[]{4, 6});

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        if (this.isVehicle() && this.isTame() && !this.isSitting()) setMad(this.isCombo());

        // ------------ FONCTIONNEMENT PROPRE ------------

        if (!this.level().isClientSide()) {
            handleCamouflage();
            handleRoar();

            // Devient plus agressif durant la nuit.
            var followAttr = this.getAttribute(Attributes.FOLLOW_RANGE);
            if (this.level().isNight()) {
                if (!followAttr.hasModifier(NIGHT_RANGE_MODIFIER)) {
                    followAttr.addOrUpdateTransientModifier(new AttributeModifier(
                            NIGHT_RANGE_MODIFIER,
                            15.0,
                            AttributeModifier.Operation.ADD_VALUE
                    ));
                }
            } else {
                followAttr.removeModifier(NIGHT_RANGE_MODIFIER);
            }
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
            grabbed.noPhysics = false;
            if (grabbed.getVehicle() == this) grabbed.stopRiding();
        }
        this.setGrabbing(false, null);
        this.setGrabTimeout(0);
        grabDamageTimer = 0;
        this.setTarget(null);
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

        if (!isTame()) {
            setMad(!isBaby() && target != null && getSleepBarPercent() < 75 && !this.isSitting());
        }

        if (target == null) {
            this.getPersistentData().remove("provokedBy");
        }

        super.setTarget(target);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        ItemStack soulStack = createSoulStack();

        if (canDropSoul() && this.isTame() && !this.isInResurrection() && !isBaby()) {
            this.spawnAtLocation(soulStack);
        }

        if (this.isSaddled()) {
            this.spawnAtLocation(acceptSaddle());
        }
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof TigerEntity otherTiger) {
            if (otherTiger.isBaby()) {
                return true;
            }
            if (this.isTame()) {
                return otherTiger.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherTiger.getOwnerUUID());
            } else {
                return !otherTiger.isTame();
            }
        }
        return super.isAlliedTo(entity);
    }

    private ItemStack createSoulStack() {
        ItemStack soulStack = new ItemStack(OWItems.ANIMAL_SOUL.get());
        Item item = soulStack.getItem();

        if (item instanceof AnimalSoulItem animalSoulItem) {
            UseOnContext fakeContext = new UseOnContext(this.level(), null, InteractionHand.MAIN_HAND, soulStack,
                    new BlockHitResult(this.position(), Direction.UP, this.blockPosition(), false));

            animalSoulItem.saveEntityType(fakeContext, Component.nullToEmpty(this.getClass().getSimpleName()));
            animalSoulItem.saveEntityOwner(fakeContext, Component.nullToEmpty(this.getOwner() != null ? this.getOwner().getName().getString() : ""));
            animalSoulItem.saveEntityGender(fakeContext, this.isMale());
            animalSoulItem.saveEntityMaxHealth(fakeContext, this.getMaxHealth());
            animalSoulItem.saveEntityDamages(fakeContext, this.getDamage());
            animalSoulItem.saveEntitySpeed(fakeContext, this.getSpeed());
            animalSoulItem.saveEntityScale(fakeContext, this.getScale());
            animalSoulItem.saveEntityLevel(fakeContext, this.getLevel());
            animalSoulItem.saveEntityVariant(fakeContext, this.getVariant().getId());
        }

        return soulStack;
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
        if (entity instanceof LivingEntity living && RANDOM(7)) {
            disarmTarget(living);
        }
        super.hurtAfterCombo(entity, comboAttack);
    }

    @Override
    protected boolean isImmobile() {
        return this.isRoaring();
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
        if (this.isTame() || this.isBaby() || this.isNapping() || (this.getTarget() != null && this.level().isDay())) {
            if (this.isHidden()) this.setHidden(false);
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

        if (grabbed == null || !grabbed.isAlive() || (grabbed instanceof Player p && p.isCreative())) {
            releaseGrab();
            return;
        }

        this.getLookControl().setLookAt(grabbed, 30f, 30f);
        this.setLookAt(grabbed.getX(), grabbed.getY(), grabbed.getZ());

        grabbed.noPhysics = true;

        if (grabbed instanceof Player) {
            if (!this.level().isClientSide()) {
                this.setGrabTimeout(this.getGrabTimeout() + 2);
                if (this.getGrabTimeout() >= getGrabMaxTimeout()) {
                    grabbed.kill();
                    releaseGrab();
                    return;
                }

                grabDamageTimer++;
                if (grabDamageTimer >= 10) {
                    grabDamageTimer = 0;
                    grabbed.invulnerableTime = 0;
                    grabbed.hurt(this.damageSource, this.getDamage() * 0.1f);
                }
            }
            if (!grabbed.isPassenger()) {
                grabbed.startRiding(this);
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

            if (!this.level().isClientSide()) {
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
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), OWSounds.TIGER_3.get(), SoundSource.HOSTILE, 1.0f, 0.8f);
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
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), OWSounds.TIGER_ROAR.get(), SoundSource.AMBIENT, 3.0f, (float) OWUtils.generateRandomInterval(0.9, 1.1));

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
     * Fait bondir le tigre vers sa cible.
     * @param maxDistance distance max de projection (plus c'est grand, plus le bond est loin)
     * @param verticalBoost boost vertical supplémentaire (0.3f par défaut dans Alex's Mobs)
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

        OWUtils.spawnServerParticles(this, ParticleTypes.CAMPFIRE_COSY_SMOKE, 0.5, -0.75, 0.5, 10,1);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), OWSounds.TIGER_JUMP.get(), SoundSource.AMBIENT, 3.0f, (float) OWUtils.generateRandomInterval(0.9, 1.1));
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction function) {
        if (passenger == this.getGrabbedTarget()) {
            Vec3 look = this.getLookAngle();
            function.accept(passenger, this.getX() + look.x * 2.65f, this.getY() - 1.0, this.getZ() + look.z * 2.65f);
        } else {
            super.positionRider(passenger, function);
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
        if (entity.getMaxHealth() > 25f) return false;
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

    // ==================================================
    //                   ANIMATIONS
    // ==================================================

    private void setupAnimationState() {
        createIdleAnimation(80, true);
        createSitAnimation(64, true);

        handleMiscIdleAnimations();

        if (this.isNapping()) {
            if (this.napAnimationTimeout <= 0) {
                this.napAnimationTimeout = 64;
                this.napAnimationState.start(this.tickCount);
            } else --this.napAnimationTimeout;
        }

        if (!this.isNapping()) {
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

        if (this.isLeaping) {
            if (this.leapAnimationTimeout <= 0) {
                this.leapAnimationTimeout = 40;
                this.leapAnimationState.start(this.tickCount);
            } else --this.leapAnimationTimeout;
        }

        if (!this.isLeaping) {
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
                    OWSounds.TIGER_3.get(), this.getSoundSource(),
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
            timer = 0;
            animationState.stop();
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

    public void setGrabbing(boolean isGrabbing, @Nullable LivingEntity entity) {

        if (isGrabbing && entity != null) {
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

    // ==================================================
    //              DONNÉES SAUVEGARDÉES
    // ==================================================

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("foodGiven", this.foodGiven);
        tag.putInt("foodWanted", this.foodWanted);
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
        if (this.getSkinIndex() != 0) { this.nbtRestoring = true; this.changeSkin(this.getSkinIndex(), false); this.nbtRestoring = false; }
    }
}
