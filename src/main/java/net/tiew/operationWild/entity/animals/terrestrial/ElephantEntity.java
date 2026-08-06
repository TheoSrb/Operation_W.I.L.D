package net.tiew.operationWild.entity.animals.terrestrial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.component.OWDataComponentTypes;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWRideable;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.goals.global.OWBreedGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.variants.ElephantVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.tiew.operationWild.core.OWUtils.RANDOM;

public class ElephantEntity extends OWEntity implements IOWEntity, IOWTamable, IOWRideable {
    // ==================================================
    //              CONSTANTES PRINCIPALES
    // ==================================================

    public static final double TAMING_EXPERIENCE = 300.0;

    /**
     * Cadence des barrissements d'oisiveté, en ticks. Beaucoup plus espacée que les grognements du
     * tigre : un cri d'éléphant porte loin et dure longtemps, l'entendre toutes les vingt secondes
     * le banaliserait.
     */
    private static final int CALL_MIN_COOLDOWN = 700;
    private static final int CALL_MAX_COOLDOWN = 1400;
    private static final int CALL_DURATION = 90;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_MAD = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_CALLING = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Integer> SHOULDER_BASH_TIMER = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SHOULDER_BASH_SIDE = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> EARTHQUAKE_TICK = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ULTIMATE_KILL_COUNT = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> SADDLE_WOOL_0 = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SADDLE_WOOL_1 = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);

    /** Teinte des deux calques de laine quand la selle n'en déclare aucune. */
    private static final int DEFAULT_WOOL_COLOR = 0xD7CEC5;

    /** Emplacement de la selle dans l'inventaire de la créature (cf. {@code OWEntity.die}). */
    private static final int SADDLE_SLOT = 1;

    // ==================================================
    //             COMPTEURS ET ANIMATIONS
    // ==================================================

    public final AnimationState callAnimationState = new AnimationState();
    public final AnimationState earthquakeAnimationState = new AnimationState();

    public int callAnimationStartTime = 0;

    // ==================================================
    //                VARIABLES PROPRES
    // ==================================================

    public volatile float bodyAnimY = 0f;

    private int shoulderBashCooldown = 0;
    private int callCooldown = (int) OWUtils.generateRandomInterval(CALL_MIN_COOLDOWN, CALL_MAX_COOLDOWN);

    /** Vrai entre l'impact au sol et la fin de la réplique : c'est la fenêtre où les caméras tremblent. */
    private boolean earthquakeImpactDone = false;

    // ==================================================
    //            INTELLIGENCE ARTIFICIELLE
    // ==================================================

    public ElephantEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 80.0)
                .add(Attributes.MOVEMENT_SPEED, 0.14D)
                .add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95D)
                .add(Attributes.ARMOR, 3.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new ElephantMeleeAttackGoal());

        this.goalSelector.addGoal(10, new OWBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new RandomStrollGoal(this, 0.8D));

        this.goalSelector.addGoal(11, new OWRandomLookAroundGoal(this));

        this.lookControl = new LookControl(this) {
            @Override
            public void tick() {
                if (this.mob instanceof ElephantEntity elephant && !elephant.isSleeping() && !elephant.isNapping()
                        && !elephant.isEarthquaking()) {
                    super.tick();
                }
            }
        };
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(IS_MAD, false);
        builder.define(IS_CALLING, false);
        builder.define(SHOULDER_BASH_TIMER, 0);
        builder.define(SHOULDER_BASH_SIDE, 1);
        builder.define(EARTHQUAKE_TICK, 0);
        builder.define(ULTIMATE_KILL_COUNT, 0);
        builder.define(SADDLE_WOOL_0, DEFAULT_WOOL_COLOR);
        builder.define(SADDLE_WOOL_1, DEFAULT_WOOL_COLOR);
    }

    // ==================================================
    //             MÉTHODES PRINCIPALES
    // ==================================================

    @Override
    public int getEntityColor() {
        return 0x776a5e;
    }

    @Override
    public float getTheoreticalScale() {
        return 20f;
    }

    @Override
    public double getTamingExperience() {
        return TAMING_EXPERIENCE;
    }

    @Override
    public OWEntityConfig.Archetypes getArchetype() {
        return OWEntityConfig.Archetypes.TANK;
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
        return 2.8f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 1.5f;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return 1.5f;
    }

    @Override
    public float vehicleWaterSpeedDivider() {
        return 4f;
    }

    @Override
    public boolean canIncreasesSpeedDuringSprint() {
        return false;
    }

    @Override
    public boolean isChangeSpeedDuringCombo() {
        return true;
    }

    @Override
    public Item acceptSaddle() {
        return OWItems.ELEPHANT_SADDLE.get();
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.ELEPHANT_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return 380 * (1 + ((float) this.getLevel() / 50));
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 0.75f * (1 + ((float) this.getLevel() / 50));
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

    /**
     * Le cap se rattrape à 5 % de l'écart par tick — la valeur la plus basse du mod avec le boa
     * libre. Un éléphant ne pivote pas, il décrit une courbe ; c'est ce réglage, bien plus que la
     * vitesse, qui fait sentir les quatre tonnes sous la selle.
     */
    @Override
    public float getRotationSpeed() {
        return 0.05f;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.ELEPHANT.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.ELEPHANT_FOOD);
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

    /**
     * Un pas d'éléphant, c'est deux sons superposés : le bruit du bloc foulé, joué grave et fort, et
     * le coup sourd de la patte elle-même. Le tigre empile sept répétitions à faible volume pour
     * épaissir un pas léger ; ici l'inverse — un seul appel, mais lourd.
     */
    private void playStepSoundFromAnimation(float pitchMod) {
        if (!this.level().isClientSide()) return;
        if (!this.onGround()) return;
        if (this.isInWater()) return;

        if (this.getDeltaMovement().horizontalDistanceSqr() < 0.0001) return;

        long now = System.currentTimeMillis();
        if (now - lastStepSoundMs < 250L) return;
        lastStepSoundMs = now;

        BlockState blockState = this.getBlockStateOn();
        if (blockState.isAir()) return;

        BlockPos pos = this.blockPosition();
        SoundType soundtype = blockState.getSoundType(this.level(), pos, this);

        this.level().playLocalSound(
                this.getX(),
                this.getY(),
                this.getZ(),
                soundtype.getStepSound(),
                this.getSoundSource(),
                soundtype.getVolume() * 1.5F,
                soundtype.getPitch() * pitchMod,
                false
        );

        this.level().playLocalSound(
                this.getX(),
                this.getY(),
                this.getZ(),
                OWSounds.ELEPHANT_FOOTSTEP.get(),
                this.getSoundSource(),
                0.9F,
                pitchMod,
                false
        );
    }

    /** Appelé par {@code ElephantModel} (thread de rendu) quand la patte gauche touche le sol. */
    public void onLeftFootDown() {
        playStepSoundFromAnimation(0.55f);
    }

    /** Appelé par {@code ElephantModel} (thread de rendu) quand la patte droite touche le sol. */
    public void onRightFootDown() {
        playStepSoundFromAnimation(0.62f);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (isNapping() || isSleeping()) return null;
        if (RANDOM(4)) return OWSounds.ELEPHANT_IDLE.get();
        if (RANDOM(3)) return OWSounds.ELEPHANT_IDLE_2.get();
        if (RANDOM(2)) return OWSounds.ELEPHANT_IDLE_3.get();
        return OWSounds.ELEPHANT_IDLE_4.get();
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return OWSounds.ELEPHANT_HURT.get();
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return RANDOM(2) ? OWSounds.ELEPHANT_HURTING.get() : OWSounds.ELEPHANT_HURTING_2.get();
    }

    // ==================================================
    //             CORPS DU FONCTIONNEMENT
    // ==================================================

    @Override
    public void tick() {
        super.tick();

        // ------------ FONCTIONNEMENT GLOBAL ------------

        if (shoulderBashCooldown > 0) shoulderBashCooldown--;

        createCombo(24, 15, OWSounds.ELEPHANT_HURTING.get(), 4.0, 4.0, 2.5, actualAttackNumber == 2, actualAttackNumber == 2 ? 3 : 1);
        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        if (this.isVehicle() && this.isTame() && !this.isSitting()) {
            setMadByRider(this.isCombo() || this.isShoulderBashing() || this.isEarthquaking());
        }

        // L'amortissement du séisme s'applique des DEUX côtés : la position d'une monture appartient
        // au client de son cavalier, et ne freiner que sur le serveur ferait vibrer la bête pendant
        // les quatre secondes du geste.
        if (this.isEarthquaking()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.2, 1.0, 0.2));
        }

        // ------------ FONCTIONNEMENT PROPRE ------------

        if (!this.level().isClientSide()) {
            tickShoulderBash();
            tickEarthquake();
            handleCall();
            if (this.tickCount % 20 == 0) refreshSaddleWoolColors();
        }
    }

    /**
     * Publie vers le client les deux teintes de laine portées par la selle.
     *
     * <p>L'inventaire de la créature ne franchit pas le réseau — il n'est désérialisé que côté
     * serveur —, alors que le calque de rendu, lui, est purement client. Les deux couleurs passent
     * donc par des données synchronisées, comme la teinte du collier. Un rafraîchissement par
     * seconde suffit : on ne reteint pas une selle à chaque tick.</p>
     */
    private void refreshSaddleWoolColors() {
        int first = DEFAULT_WOOL_COLOR;
        int second = DEFAULT_WOOL_COLOR;

        ItemStack saddle = this.getInventory().getStackInSlot(SADDLE_SLOT);
        List<Item> wools = saddle.isEmpty() ? null : saddle.get(OWDataComponentTypes.SADDLE_WOOLS.get());

        if (wools != null && !wools.isEmpty()) {
            first = woolColor(wools.get(0));
            second = woolColor(wools.size() > 1 ? wools.get(1) : wools.get(0));
        }

        if (this.entityData.get(SADDLE_WOOL_0) != first) this.entityData.set(SADDLE_WOOL_0, first);
        if (this.entityData.get(SADDLE_WOOL_1) != second) this.entityData.set(SADDLE_WOOL_1, second);
    }

    /** Même palette que celle des teintures du collier, pour que les deux se répondent en jeu. */
    private static int woolColor(Item wool) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(wool);
        return switch (id.toString()) {
            case "minecraft:white_wool" -> 0xD7CEC5;
            case "minecraft:orange_wool" -> 0xD87F33;
            case "minecraft:magenta_wool" -> 0xB24CD8;
            case "minecraft:light_blue_wool" -> 0x6699D8;
            case "minecraft:yellow_wool" -> 0xE5E533;
            case "minecraft:lime_wool" -> 0x7FCC19;
            case "minecraft:pink_wool" -> 0xF27FA5;
            case "minecraft:gray_wool" -> 0x4C4C4C;
            case "minecraft:light_gray_wool" -> 0x999999;
            case "minecraft:cyan_wool" -> 0x4C7F99;
            case "minecraft:purple_wool" -> 0x7F3FB2;
            case "minecraft:blue_wool" -> 0x3366CC;
            case "minecraft:brown_wool" -> 0x664C33;
            case "minecraft:green_wool" -> 0x667F33;
            case "minecraft:red_wool" -> 0x993333;
            case "minecraft:black_wool" -> 0x191919;
            default -> DEFAULT_WOOL_COLOR;
        };
    }

    /**
     * Le coup d'épaule n'est qu'une fenêtre : la poussée elle-même appartient au client du cavalier
     * (cf. {@code localEffect}). Ici on ne tient que la durée du geste et le moment où les dégâts
     * partent, un tick après le départ pour que la masse ait commencé à se déporter.
     */
    private void tickShoulderBash() {
        int timer = this.entityData.get(SHOULDER_BASH_TIMER);
        if (timer <= 0) return;

        if (timer == OWAttacksConstants.Elephant.SHOULDER_BASH_DURATION_TICKS - 1) {
            applyShoulderBashDamage();
        }

        this.entityData.set(SHOULDER_BASH_TIMER, timer - 1);
    }

    private void tickEarthquake() {
        int tick = this.entityData.get(EARTHQUAKE_TICK);
        if (tick <= 0) return;

        if (tick == OWAttacksConstants.Elephant.EARTHQUAKE_WINDUP_TICKS && !earthquakeImpactDone) {
            earthquakeImpactDone = true;
            executeEarthquakeImpact();
        }

        int total = OWAttacksConstants.Elephant.EARTHQUAKE_TOTAL_TICKS
                + OWAttacksConstants.Elephant.EARTHQUAKE_AFTERSHOCK_TICKS;

        if (tick >= total) {
            cancelEarthquake();
            return;
        }

        this.entityData.set(EARTHQUAKE_TICK, tick + 1);
    }

    private void handleCall() {
        if (this.isCalling() && this.tickCount - callAnimationStartTime > CALL_DURATION) {
            this.setCalling(false);
        }

        if (callCooldown > 0) {
            callCooldown--;
            return;
        }

        callCooldown = (int) OWUtils.generateRandomInterval(CALL_MIN_COOLDOWN, CALL_MAX_COOLDOWN);

        if (!canPlayIdleAnimation()) return;

        this.setCalling(true);
        this.callAnimationStartTime = this.tickCount;
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.ELEPHANT_CALL.get(), SoundSource.NEUTRAL,
                2.5f, isBaby() ? 1.4f : (float) OWUtils.generateRandomInterval(0.9, 1.05));
    }

    @Override
    public void hurtAfterCombo(LivingEntity entity, int comboAttack) {
        super.hurtAfterCombo(entity, comboAttack);
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity target) {
        int kills = getUltimateKillCount();
        if (kills < OWAttacksConstants.Elephant.EARTHQUAKE_KILLS_REQUIRED) {
            setUltimateKillCount(kills + 1);
        }
        return super.killedEntity(level, target);
    }

    @Override
    protected boolean isImmobile() {
        return this.isEarthquaking();
    }

    @Override
    public float getRiddenSpeedVehicle(Player player) {
        return this.isImmobile() ? 0 : super.getRiddenSpeedVehicle(player);
    }

    @Override
    protected double getBaseRiderYOffset() {
        return this.getBbHeight() * 0.72 * this.getScale();
    }

    @Override
    protected float getRiderAnimYOffset() {
        return -bodyAnimY / 16.0f * this.getScale();
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction function) {
        if (!this.hasPassenger(passenger) || this.touchingUnloadedChunk()) return;

        Vec3 seatOffset = new Vec3(0, 0, 0.35).yRot((float) Math.toRadians(-this.yBodyRot));
        double baseY = getBaseRiderYOffset();
        float animY = getRiderAnimYOffset();

        passenger.fallDistance = 0f;
        function.accept(passenger, this.getX() + seatOffset.x, this.getY() + baseY + animY, this.getZ() + seatOffset.z);
    }

    @Override
    public void setTame(boolean tame, Player player) {
        super.setTame(tame, player);
        if (tame) {
            this.setMad(false);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));

            this.setVariant(chooseElephantVariant());
            this.setInitialVariant(this.getVariant());
        }
        this.foodWanted = (int) OWUtils.generateRandomInterval(12, 20);
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    private ElephantVariant chooseElephantVariant() {
        int roll = this.random.nextInt(100);

        if (roll < 2) return ElephantVariant.PINK;
        if (roll < 30) return ElephantVariant.GREY;
        return ElephantVariant.DEFAULT;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (EARTHQUAKE_TICK.equals(accessor) && !isEarthquaking() && this.level().isClientSide()) {
            earthquakeAnimationState.stop();
        }
    }

    // ------------ COUP D'ÉPAULE ------------

    /**
     * Côté vers lequel l'éléphant se déporte, déduit de l'écart entre le regard du cavalier et l'axe
     * du corps. Le calcul est volontairement <b>reproductible des deux côtés du réseau</b> : le
     * serveur s'en sert pour la boîte de dégâts, le client du cavalier pour la poussée, et aucun des
     * deux n'attend l'autre. Une synchronisation ferait arriver la valeur un tick trop tard, et
     * l'éléphant partirait du mauvais côté à chaque clic.
     *
     * @return {@code 1} pour la droite du corps, {@code -1} pour sa gauche
     */
    public static int computeBashSide(float riderYaw, float bodyYaw) {
        return Mth.wrapDegrees(riderYaw - bodyYaw) >= 0 ? 1 : -1;
    }

    /** Vecteur horizontal unitaire pointant du côté du déport. */
    public Vec3 getBashDirection(int side) {
        return Vec3.directionFromRotation(0f, this.yBodyRot + 90f * side);
    }

    public void performShoulderBash() {
        if (this.level().isClientSide()) return;
        if (isShoulderBashing() || isEarthquaking()) return;
        if (shoulderBashCooldown > 0) return;

        float cost = OWAttacksConstants.Elephant.SHOULDER_BASH_ENERGY;
        if (getVitalEnergy() > getMaxVitalEnergy() - cost) {
            canShowVitalEnergyLack = true;
            cancelShoulderBash();
            return;
        }
        setVitalEnergy(getVitalEnergy() + cost);

        LivingEntity rider = this.getControllingPassenger();
        int side = computeBashSide(rider != null ? rider.getYRot() : this.getYRot(), this.yBodyRot);

        this.entityData.set(SHOULDER_BASH_SIDE, side);
        this.entityData.set(SHOULDER_BASH_TIMER, OWAttacksConstants.Elephant.SHOULDER_BASH_DURATION_TICKS);
        shoulderBashCooldown = OWAttacksConstants.Elephant.SHOULDER_BASH_COOLDOWN_TICKS;

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.ELEPHANT_SCREAM.get(), SoundSource.NEUTRAL,
                2.0f, (float) OWUtils.generateRandomInterval(0.85, 1.0));
    }

    /** Remet à zéro tous les drapeaux du coup d'épaule — aucun ne doit survivre à un échec. */
    public void cancelShoulderBash() {
        if (this.level().isClientSide()) return;
        this.entityData.set(SHOULDER_BASH_TIMER, 0);
        this.entityData.set(SHOULDER_BASH_SIDE, 1);
    }

    private void applyShoulderBashDamage() {
        int side = getShoulderBashSide();
        Vec3 dir = getBashDirection(side);
        double radius = OWAttacksConstants.Elephant.SHOULDER_BASH_RADIUS;

        Vec3 center = this.position().add(dir.scale(radius * 0.5)).add(0, this.getBbHeight() * 0.5, 0);
        AABB box = new AABB(center, center).inflate(radius * 0.75, this.getBbHeight() * 0.6, radius * 0.75);

        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, box)) {
            if (target == this || target == this.getControllingPassenger()) continue;
            if (this.isAlliedTo(target)) continue;

            target.hurt(this.damageSources().mobAttack(this), OWAttacksConstants.Elephant.SHOULDER_BASH_DAMAGE);
            target.push(dir.x * OWAttacksConstants.Elephant.SHOULDER_BASH_KNOCKBACK,
                    0.45,
                    dir.z * OWAttacksConstants.Elephant.SHOULDER_BASH_KNOCKBACK);
            target.hurtMarked = true;
        }

        OWUtils.spawnServerParticles(this, ParticleTypes.CLOUD, dir.x * 2, 0.4, dir.z * 2, 14, 0.4);
    }

    // ------------ TREMBLEMENT DE TERRE ------------

    /**
     * L'éléphant se cabre lentement, puis retombe de tout son poids. L'impact ne tombe pas à la fin
     * du geste mais à 3,0 s sur les 4,16 s de l'animation — c'est l'image où les pattes touchent le
     * sol. Le reste est la réception, pendant laquelle le sol continue de trembler.
     */
    public void activateEarthquake() {
        if (this.level().isClientSide()) return;
        if (isEarthquaking()) return;
        if (getUltimateKillCount() < OWAttacksConstants.Elephant.EARTHQUAKE_KILLS_REQUIRED) return;

        float cost = OWAttacksConstants.Elephant.EARTHQUAKE_ENERGY;
        if (getVitalEnergy() > getMaxVitalEnergy() - cost) {
            canShowVitalEnergyLack = true;
            return;
        }
        setVitalEnergy(0);

        setUltimateKillCount(0);
        cancelShoulderBash();

        earthquakeImpactDone = false;
        this.entityData.set(EARTHQUAKE_TICK, 1);
        this.getNavigation().stop();

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.ELEPHANT_CALL.get(), SoundSource.NEUTRAL, 3.0f, 0.75f);
    }

    private void cancelEarthquake() {
        this.entityData.set(EARTHQUAKE_TICK, 0);
        earthquakeImpactDone = false;
    }

    private void executeEarthquakeImpact() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        double radius = OWAttacksConstants.Elephant.EARTHQUAKE_RADIUS;
        AABB box = this.getBoundingBox().inflate(radius, radius * 0.5, radius);

        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, box)) {
            if (target == this || target == this.getControllingPassenger()) continue;
            if (this.isAlliedTo(target)) continue;
            if (!target.onGround()) continue;

            double distance = target.distanceTo(this);
            if (distance > radius) continue;

            float ratio = (float) Mth.clamp(distance / radius, 0.0, 1.0);
            float damage = Mth.lerp(ratio,
                    OWAttacksConstants.Elephant.EARTHQUAKE_DAMAGE_CENTER,
                    OWAttacksConstants.Elephant.EARTHQUAKE_DAMAGE_EDGE);

            target.hurt(this.damageSources().mobAttack(this), damage);
            target.push(0, OWAttacksConstants.Elephant.EARTHQUAKE_LAUNCH * (1f - ratio * 0.6f), 0);
            target.hurtMarked = true;
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                    OWAttacksConstants.Elephant.EARTHQUAKE_SLOWNESS_TICKS, 1));
        }

        breakGroundAround(serverLevel);

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.NEUTRAL, 4.0f, 0.35f);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.ELEPHANT_SCREAM.get(), SoundSource.NEUTRAL, 3.0f, 0.7f);

        OWUtils.spawnServerParticles(this, ParticleTypes.EXPLOSION, 3, 0.2, 3, 30, 0.1);

        BlockState ground = serverLevel.getBlockState(this.blockPosition().below());
        if (!ground.isAir()) {
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                    this.getX(), this.getY() + 0.2, this.getZ(), 200, radius * 0.4, 0.3, radius * 0.4, 0.35);
        }
    }

    /**
     * Le cratère. Seuls les blocs de surface tendres cèdent, et pas tous : {@code BREAK_CHANCE}
     * laisse un sol déchiqueté plutôt qu'un disque rasé, ce qui se lit mieux comme une secousse que
     * comme une explosion. Les blocs à inventaire et l'indestructible sont épargnés — un éléphant ne
     * doit pas pouvoir vider un coffre en tapant du pied.
     */
    private void breakGroundAround(ServerLevel serverLevel) {
        if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return;

        double radius = OWAttacksConstants.Elephant.EARTHQUAKE_BREAK_RADIUS;
        int r = Mth.ceil(radius);
        BlockPos origin = this.blockPosition();

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz > radius * radius) continue;
                if (this.random.nextFloat() > OWAttacksConstants.Elephant.EARTHQUAKE_BREAK_CHANCE) continue;

                for (int dy = 1; dy >= -1; dy--) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    BlockState state = serverLevel.getBlockState(pos);

                    if (state.isAir()) continue;
                    if (state.hasBlockEntity()) continue;
                    if (!state.getFluidState().isEmpty()) continue;

                    float hardness = state.getDestroySpeed(serverLevel, pos);
                    if (hardness < 0 || hardness > 3.0f) continue;
                    if (!serverLevel.isEmptyBlock(pos.above())) continue;

                    serverLevel.destroyBlock(pos, false);
                    break;
                }
            }
        }
    }

    // ==================================================
    //                   ANIMATIONS
    // ==================================================

    private void setupAnimationState() {
        createIdleAnimation(96, true);
        createSitAnimation(122, true);

        if (this.isCalling()) {
            callAnimationState.startIfStopped(this.tickCount);
        } else {
            callAnimationState.stop();
        }

        if (this.isEarthquaking()) {
            earthquakeAnimationState.startIfStopped(this.tickCount);
        } else {
            earthquakeAnimationState.stop();
        }

        setupComboAnimations();
    }

    public boolean canPlayIdleAnimation() {
        return this.getTarget() == null && !this.isNapping() && !this.isSleeping() && !this.isMoving()
                && !this.isVehicle() && !this.isInWater() && !this.isEarthquaking();
    }

    /**
     * Les trois frappes durent 1,68 s, soit 33,6 ticks à vitesse 1. Lues à 0,925 / 1,05 / 1,15 elles
     * occupent 36,3 / 32,0 / 29,2 ticks ; les minuteurs ajoutent le tiers de marge pendant lequel
     * l'éléphant tient sa pose finale, sans quoi le geste est tranché avant sa dernière image.
     */
    private void setupComboAnimations() {
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, (int) (48 / comboSpeedMultiplier));
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, (int) (42 / comboSpeedMultiplier));
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, (int) (39 / comboSpeedMultiplier));
    }

    private void setupComboAnimation(int comboNumber, AnimationState animationState, int timer, int maxTimer) {
        timer = tickComboAnimation(comboNumber, animationState, timer, maxTimer, this.isCombo(comboNumber));

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
        if (entity instanceof ElephantEntity elephant) {
            elephant.setVariant(ElephantVariant.byId(variant));
            elephant.setInitialVariant(ElephantVariant.byId(variant));
        }
    }

    public ElephantVariant getVariant() {
        return ElephantVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(ElephantVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
    }

    public void setSkin(ElephantVariant skin) {
        this.setVariant(skin);
    }

    @Override
    public void changeSkin(int skinIndex, boolean playingEffects) {
        super.changeSkin(skinIndex, playingEffects);
        this.setVariant(getInitialVariant());

        switch (skinIndex) {
            case 1 -> this.setSkin(ElephantVariant.Cosmetics.GOLD.variant);
            case 2 -> this.setSkin(ElephantVariant.Cosmetics.DEMON.variant);
            case 3 -> this.setSkin(ElephantVariant.Cosmetics.ZOMBIE.variant);
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

    public ElephantVariant getInitialVariant() {
        return ElephantVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(ElephantVariant variant) {
        this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());
    }

    public void setMad(boolean isMad) {
        if (isMad) if (this.getCurrentMode() == Mode.Passive) return;
        this.entityData.set(IS_MAD, isMad);
    }

    /** Colère déclenchée par le cavalier, qui ignore le mode passif (cf. {@code TigerEntity}). */
    public void setMadByRider(boolean isMad) {
        this.entityData.set(IS_MAD, isMad);
    }

    public boolean isMad() { return this.entityData.get(IS_MAD); }

    public void setCalling(boolean calling) { this.entityData.set(IS_CALLING, calling); }

    public boolean isCalling() { return this.entityData.get(IS_CALLING); }

    public int getShoulderBashSide() { return this.entityData.get(SHOULDER_BASH_SIDE); }

    public int getShoulderBashTimer() { return this.entityData.get(SHOULDER_BASH_TIMER); }

    public boolean isShoulderBashing() { return this.entityData.get(SHOULDER_BASH_TIMER) > 0; }

    public int getEarthquakeTick() { return this.entityData.get(EARTHQUAKE_TICK); }

    public boolean isEarthquaking() { return this.entityData.get(EARTHQUAKE_TICK) > 0; }

    /**
     * Intensité de la secousse de caméra, nulle avant l'impact puis décroissante jusqu'à la fin de
     * la réplique. Lue côté client par {@code ClientEvents} pour tous les joueurs à portée.
     */
    public float getEarthquakeShakeIntensity() {
        int tick = getEarthquakeTick();
        int windup = OWAttacksConstants.Elephant.EARTHQUAKE_WINDUP_TICKS;
        int aftershock = OWAttacksConstants.Elephant.EARTHQUAKE_AFTERSHOCK_TICKS;

        if (tick < windup) return 0f;
        float progress = (float) (tick - windup) / aftershock;
        if (progress > 1f) return 0f;
        return OWAttacksConstants.Elephant.EARTHQUAKE_SHAKE_INTENSITY * (1f - progress);
    }

    public int getSaddleWoolColor(int layer) {
        return this.entityData.get(layer == 0 ? SADDLE_WOOL_0 : SADDLE_WOOL_1);
    }

    public int getUltimateKillCount() { return this.entityData.get(ULTIMATE_KILL_COUNT); }

    private void setUltimateKillCount(int count) { this.entityData.set(ULTIMATE_KILL_COUNT, Math.max(0, count)); }

    // ==================================================
    //              DONNÉES SAUVEGARDÉES
    // ==================================================

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("foodGiven", this.foodGiven);
        tag.putInt("foodWanted", this.foodWanted);
        tag.putInt("ultimateKillCount", this.getUltimateKillCount());
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
        if (tag.contains("ultimateKillCount")) {
            setUltimateKillCount(tag.getInt("ultimateKillCount"));
        }
        if (this.getSkinIndex() != 0) { this.nbtRestoring = true; this.changeSkin(this.getSkinIndex(), false); this.nbtRestoring = false; }
    }

    @Override
    protected int getDefaultSkinIndex() { return 7; }   // « Éléphant Par Défaut »

    class ElephantMeleeAttackGoal extends MeleeAttackGoal {

        public ElephantMeleeAttackGoal() {
            super(ElephantEntity.this, 7, true);
        }

        @Override
        public void start() {
            super.start();
            ElephantEntity.this.setMad(true);
            ElephantEntity.this.setRunning(true);
        }

        @Override
        public void stop() {
            super.stop();
            ElephantEntity.this.setMad(false);
            ElephantEntity.this.setRunning(false);
        }

        @Override
        protected boolean canPerformAttack(LivingEntity entity) {
            double reach = 4.5;
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
