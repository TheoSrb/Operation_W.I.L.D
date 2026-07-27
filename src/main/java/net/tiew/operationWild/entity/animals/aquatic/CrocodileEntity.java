package net.tiew.operationWild.entity.animals.aquatic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.FollowBoatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.tiew.operationWild.block.OWBlocks;
import net.tiew.operationWild.block.custom.MarkedMudBlock;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.enchantment.OWEnchantments;
import net.tiew.operationWild.entity.OWSemiWaterEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaTailPart;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.attacks.OWAttacksHandler;
import net.tiew.operationWild.entity.behavior.CrocodileBehaviorHandler;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.config.*;
import net.tiew.operationWild.entity.goals.*;
import net.tiew.operationWild.entity.goals.crocodile.CrocodileAttackGoal;
import net.tiew.operationWild.entity.goals.crocodile.CrocodileChargingMouthGoal;
import net.tiew.operationWild.entity.goals.crocodile.CrocodileGoToWaterWithFoodGoal;
import net.tiew.operationWild.entity.goals.crocodile.CrocodileNapGoal;
import net.tiew.operationWild.entity.goals.global.OWBreedGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.taming.TamingCrocodile;
import net.tiew.operationWild.entity.variants.CrocodileVariant;
import net.tiew.operationWild.sound.OWSounds;
import net.tiew.operationWild.core.OWTags;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.AnimalSoulItem;

import java.util.*;

import static net.tiew.operationWild.core.OWUtils.RANDOM;

public class CrocodileEntity extends OWSemiWaterEntity implements IOWEntity, IOWTamable, IOWRideable, IOWGrabberEntity {

    public static final double TAMING_EXPERIENCE = 205.0;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_MAD = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_GRABBING = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> GRABBED_TARGET_ID = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_DEATH_ROLLING = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DEATH_ROLLING_PROGRESS = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GRAB_TIMEOUT = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Float> SACRIFICES_UNITY = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> START_TAMING = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ENTITIES_KILLED_DURING_TAMING = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TAMING_TIMER = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ULTIMATE_KILL_COUNT = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_LUNGING = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> LUNGE_TARGET_ID = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> RIDER_CONTROL_PITCH = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_MOUTH_SLAMMING = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_CHARGING_MOUTH = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> CHARGING_MOUTH_TIMER = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_PLAYER_MOUTH_CHARGING = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.BOOLEAN);

    public CrocodileBehaviorHandler crocodileBehaviorHandler;
    public TamingCrocodile crocodileTaming;

    public CrocodileTailPart[] tailParts = new CrocodileTailPart[3];

    private static final float SEG_DIST = 1.25f;
    private static final float BODY_TAIL_OFFSET = 0.75f;

    public volatile float tail1AnimXRot = 0f, tail1AnimYRot = 0f, tail1AnimZRot = 0f;
    public volatile float tail2AnimXRot = 0f, tail2AnimYRot = 0f, tail2AnimZRot = 0f;
    public volatile float tail3AnimXRot = 0f, tail3AnimYRot = 0f, tail3AnimZRot = 0f;

    private final double[] seg1 = new double[3];
    private final double[] seg2 = new double[3];
    private final double[] seg3 = new double[3];
    private float seg1Yaw = 0f, seg2Yaw = 0f, seg3Yaw = 0f;
    private boolean tailPosInit = false;

    public final AnimationState idleWaterAnimationState = new AnimationState();
    public final AnimationState idleWaterMountedAnimState = new AnimationState();
    public final AnimationState grabHoldAnimState = new AnimationState();
    public final AnimationState grabThrashAnimState = new AnimationState();
    public final AnimationState growlsAnimationState = new AnimationState();
    public final AnimationState gruntAnimationState = new AnimationState();
    public final AnimationState napAnimationState = new AnimationState();
    public final AnimationState deathRollWindupAnimState = new AnimationState();
    public final AnimationState deathRollAnimationState = new AnimationState();
    public final AnimationState mouthSlamAnimState = new AnimationState();

    public int idleWaterAnimationTimeout = 0;
    private int growlsAnimationStartTime = 0;
    private int gruntAnimationStartTime = 0;
    private int napAnimationStartTime = 0;
    public int mouthSlamAnimTimer = 0;
    private int grabThrashAnimStartTime = 0;
    private int mouthSlamServerTimer = 0;
    private int mouthSlamHitTimer = -1;
    private float mouthSlamPendingDamage = 0f;
    private float mouthSlamPendingKnockback = 0f;
    private boolean mouthSlamPendingBleed = false;

    public volatile float bodyAnimY = 0f;
    public volatile float bodyAnimX = 0f;

    /**
     * Matrice de la chaine d'os {@code ALL2 -> ALL -> body}, relevee a chaque image par le modele.
     *
     * <p>Elle porte le pivot REEL des inclinaisons, que {@code bodyAnimX} et {@code bodyAnimY} — de
     * simples sommes de translations — ne peuvent pas donner. Sans elle, faire suivre l'assise a un
     * tangage revenait a deviner la hauteur du pivot, et le cavalier s'enfoncait ou flottait des que
     * le crocodile montait ou descendait dans l'eau.</p>
     */
    public volatile org.joml.Matrix4f boneMatrix = null;

    /**
     * Matrice de la chaine complete jusqu'a la gueule : {@code ALL2 -> ALL -> body -> neck -> head
     * -> mouth}, relevee a chaque image comme {@link #boneMatrix}.
     *
     * <p>C'est elle qui porte la proie. Un decalage fixe pris sur le corps ne pouvait pas suivre le
     * cou qui se cabre, la tete qui secoue sa victime ni le tonneau de la roulade : la prise restait
     * clouee devant le poitrail pendant que les machoires, elles, partaient ailleurs. Nulle cote
     * serveur — la position y retombe sur la pose de repos, exactement comme pour le siege du
     * cavalier.</p>
     */
    public volatile org.joml.Matrix4f mouthMatrix = null;

    /** Orientation de la gueule, en degres, meme convention de signe que {@code bodyXRot}/{@code bodyZRot}. */
    public volatile float mouthXRotDeg = 0f, mouthYRotDeg = 0f, mouthZRotDeg = 0f;

    /**
     * Pose de repos de la chaine, en pixels modele — somme des {@code PartPose.offset} de
     * {@code ALL2} (0,9 / 14 / 3), {@code ALL} (-0,9 / 0 / -3) et {@code body} (0,0617 / -1,4547 / 6).
     *
     * <p>Reference et non valeur absolue : on ne place pas le siege d'apres la matrice, on le DECALE
     * de l'ecart entre la pose courante et celle-ci. A plat l'ecart est nul, et le calcul redonne
     * exactement le placement d'avant — celui qui etait juste.</p>
     */
    private static final float REST_X = 0.0617f, REST_Y = 12.5453f, REST_Z = 6.0f;

    /** Hauteur du repere modele au-dessus de l'origine de l'entite, en blocs (cf. LivingEntityRenderer). */
    private static final float MODEL_ORIGIN_Y = 1.501f;

    public boolean canGrabOnLand = false;

    private int primalDivePhase = 0;
    private int primalDiveTimer = 0;
    private int primalDiveLungeTimer = 0;

    /**
     * Delai avant qu'une NOUVELLE prise soit possible, en ticks, <b>propre a ce crocodile</b>.
     *
     * <p>C'etait auparavant un horodatage {@code static} : un seul crocodile qui mordait imposait
     * son delai de trente secondes a tous les autres de la partie — sur un serveur, la moitie des
     * crocodiles ne saisissait jamais rien. Le compteur appartient donc a la bete, et il est
     * sauvegarde avec elle.</p>
     */
    private int grabCooldown = 0;

    /** Duree restante d'une prise a duree fixe (passif apprivoise, ultime). 0 = pas de minuterie. */
    private int grabHoldTimer = 0;

    /** Delai avant la prochaine roulade, qu'elle vienne de l'IA sauvage ou du clic du cavalier. */
    private int deathRollCooldown = 0;

    private static final int MAX_GRAB_COOLDOWN = 600;

    /** Usure initiale d'une prise : à mi-chemin de {@link #getGrabMaxTimeout()}, jauge à moitié pleine. */
    public static final int GRAB_START_TIMEOUT = 300;

    /**
     * Armement de la roulade : le crocodile se ramasse avant de partir en rotation.
     *
     * <p>Volontairement tres court. Une demi-seconde d'anticipation se justifie sur une attaque
     * qu'on regarde ; sur une commande qu'on declenche soi-meme, elle se ressent comme un temps
     * mort entre le clic et la rotation. L'animation d'armement est jouee acceleree d'autant, elle
     * garde donc sa pose complete — c'est un claquement, plus une respiration.</p>
     */
    public static final int DEATH_ROLL_WINDUP_TICKS = 3;
    /** Rotation proprement dite. L'animation est recalee dessus, quelle que soit sa duree propre. */
    public static final int DEATH_ROLL_SPIN_TICKS = 34;
    public static final int DEATH_ROLL_TOTAL_TICKS = DEATH_ROLL_WINDUP_TICKS + DEATH_ROLL_SPIN_TICKS;
    /** Delai entre deux roulades : laisse la bete se replacer et borne les degats au fil du temps. */
    private static final int DEATH_ROLL_COOLDOWN_TICKS = 60;

    /** Prise passive d'un crocodile apprivoise : 10 s de maintien (cf. « Accrochage Reptilien »). */
    private static final int PASSIVE_GRAB_TICKS = 200;
    /** Prise de l'ultime : 10 s, exactement la duree que la carte du HUD fait descendre. */
    public static final int PRIMAL_DIVE_GRAB_TICKS = 200;

    /** Distance de la prise devant l'origine au repos, en blocs (secours quand la chaine d'os manque). */
    private static final double MOUTH_HOLD_FORWARD = 1.62;
    /** Hauteur des machoires au-dessus de l'origine, en blocs (chaine body/neck/head/mouth). */
    private static final double MOUTH_HOLD_HEIGHT = 0.53;
    /** Avancee de base du point d'accroche DANS l'os de la gueule, en blocs, hors carrure de la proie. */
    private static final float MOUTH_HOLD_LOCAL_Z = 0.12f;

    /** Vitesse d'approche du bond de l'ultime, en blocs par tick, avant lissage. */
    private static final double PRIMAL_DIVE_LUNGE_SPEED = 1.05;
    /** Derive horizontale conservee pendant la roulade : lente, mais jamais nulle. */
    private static final double DEATH_ROLL_DRIFT = 0.06;
    /** Tolerance de sortie d'eau pendant une roulade, en ticks (la surface fait clignoter isInWater). */
    private static final int DEATH_ROLL_DRY_GRACE = 12;
    /** Duree de vie d'une demande de roulade arrivee trop tot : couvre toute la recuperation. */
    private static final int DEATH_ROLL_INPUT_BUFFER_TICKS = 60;

    private int deathRollDryTicks = 0;
    private int deathRollQueued = 0;

    private boolean isPrimalDiveGrab = false;
    private Vec3 lastPitchCheckPos = null;

    private static final int GROWLS_DURATION = 75;
    private static final int GRUNT_DURATION = 55;

    private int growlsCooldown = (int) OWUtils.generateRandomInterval(400, 1200);
    private int gruntCooldown = (int) OWUtils.generateRandomInterval(300, 500);

    public CrocodileEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
        initCrocodileBehaviorAndTaming();
    }

    private void initCrocodileBehaviorAndTaming() {
        this.crocodileBehaviorHandler = new CrocodileBehaviorHandler(this);
        this.crocodileTaming = new TamingCrocodile(this, crocodileBehaviorHandler);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 37.0)
                .add(Attributes.MOVEMENT_SPEED, 0.16D)
                .add(Attributes.FOLLOW_RANGE, 22.0D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(Attributes.ARMOR, 0.2D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        initCrocodileBehaviorAndTaming(); // Create the AI before the goals, otherwise, null error

        this.goalSelector.addGoal(0, new CrocodileGoToWaterWithFoodGoal(this));
        this.goalSelector.addGoal(0, new JumpOutOfTheWaterGoal(this));
        this.goalSelector.addGoal(0, new FollowBoatGoal(this));
        this.goalSelector.addGoal(1, new CrocodileMeleeAttackGoal());
        this.goalSelector.addGoal(2, new CrocodileChargingMouthGoal(this));
        this.goalSelector.addGoal(3, new CrocodileNapGoal(this, 1.25f, 500, true));
        this.goalSelector.addGoal(4, new OWBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(6, new OWRandomLookAroundGoal(this));

        // Le sommeil fige le regard, pas la prise : c'est justement en tenant sa proie que le
        // crocodile doit pouvoir se tourner vers l'eau. L'ancienne condition coupait le contrôle du
        // regard dès qu'une victime était en gueule, et le goal de traînée orientait donc dans le vide.
        this.lookControl = new LookControl(this) {
            @Override
            public void tick() {
                if (this.mob instanceof CrocodileEntity crocodile && !crocodile.isSleeping() && !crocodile.isNapping()) {
                    super.tick();
                }
            }
        };
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(IS_MAD, false);
        builder.define(IS_GRABBING, false);
        builder.define(GRABBED_TARGET_ID, -1);
        builder.define(IS_DEATH_ROLLING, false);
        builder.define(DEATH_ROLLING_PROGRESS, 0);
        builder.define(GRAB_TIMEOUT, 0);
        builder.define(SACRIFICES_UNITY, 0.0f);
        builder.define(START_TAMING, false);
        builder.define(ENTITIES_KILLED_DURING_TAMING, 0);
        builder.define(TAMING_TIMER, 0);
        builder.define(ULTIMATE_KILL_COUNT, 0);
        builder.define(IS_LUNGING, false);
        builder.define(LUNGE_TARGET_ID, -1);
        builder.define(RIDER_CONTROL_PITCH, 0.0f);
        builder.define(IS_MOUTH_SLAMMING, false);
        builder.define(IS_CHARGING_MOUTH, false);
        builder.define(CHARGING_MOUTH_TIMER, 0.0f);
        builder.define(IS_PLAYER_MOUTH_CHARGING, false);
    }

    // Entity Methods
    @Override
    public int getEntityColor() {
        return 0x727957;
    }

    @Override
    public float getTheoreticalScale() {
        return 9;
    }

    @Override
    public double getTamingExperience() {
        return TAMING_EXPERIENCE;
    }

    @Override
    public OWEntityConfig.Archetypes getArchetype() {
        return OWEntityConfig.Archetypes.MARAUDER;
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
        return List.of(Boat.class, Player.class, Animal.class, Monster.class);
    }

    @Override
    public float vehicleRunSpeedMultiplier() {
        return 2.5f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 1.25f;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return 1f;
    }

    @Override
    public float vehicleWaterSpeedDivider() {
        return 0.55f;
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
        return OWItems.CROCODILE_SADDLE.get();
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.CROCODILE_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return 290 * (1 + ((float) this.getLevel() / 50));
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
        return false;
    }

    @Override
    public org.joml.Matrix4f riderBoneMatrix() {
        return this.boneMatrix;
    }

    @Override
    public float getRotationSpeed() {
        return 0.1f;
    }

    @Override
    public float getScale() {
        return super.getScale() <= 0 ? 1f : super.getScale();
    }

    @Override
    public int getMaxAirSupply() {
        return 300 * 10;
    }

    @Override
    protected int increaseAirSupply(int currentAir) {
        return currentAir + 10;
    }

    @Override
    public int getMaxDepth() {
        return this.isTame() ? 30 : 5;
    }

    @Override
    public float getSwimSpeed() {
        return this.getSpeed() * 5;
    }

    /**
     * Monté, le crocodile tient sa profondeur — comme l'orque, et pour la même raison.
     *
     * <p>Le léger poids par défaut de {@code OWEntity} fait redescendre les montures qu'on cesse de
     * diriger vers le haut. Sur un crocodile, dont tout le pilotage aquatique passe déjà par le
     * tangage du regard ({@code RIDER_CONTROL_PITCH}), ce poids se lisait comme un défaut : la bête
     * s'enfonçait toute seule sous son cavalier. Regard à l'horizontale, elle reste maintenant à la
     * profondeur choisie ; c'est le pilote qui décide de monter ou de plonger.</p>
     */
    @Override
    protected double riddenBuoyancy() {
        return 0.0D;
    }

    /** Remontée à la touche de saut, à l'identique de l'orque : regagner la surface sans piquer du nez. */
    @Override
    protected double riddenAscendSpeed() {
        return 0.10D;
    }

    @Override
    protected float nextStep() {
        return this.moveDist + 0.4F;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.CROCODILE.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.CROCODILE_FOOD);
    }

    protected @Nullable SoundEvent getAmbientSound() {
        if (isNapping()) return null;
        return RANDOM(3) ? RANDOM(2) ? OWSounds.CROCODILE_IDLE_2.get() : OWSounds.CROCODILE_IDLE_4.get() : null;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return OWSounds.CROCODILE_DEATH.get();
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return OWSounds.CROCODILE_HURT.get();
    }

    // ==================================================
    //             SONS DE PAS (animation-driven)
    // ==================================================

    private long lastStepSoundMs = 0L;

    /**
     * Suppresses the vanilla automatic step sound (fired by Entity.move() every block traversed).
     * Footstep sounds are handled exclusively by animation events in CrocodileModel
     * via onFrontLeftFootDown() / onFrontRightFootDown().
     */
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
     * Called by CrocodileModel (render thread) when the left foot touches the ground.
     * Plays the step sound of the block under the crocodile with a slightly lower pitch.
     */
    public void onLeftFootDown() {
        playStepSoundFromAnimation(0.85f);
    }

    /**
     * Called by CrocodileModel (render thread) when the right foot touches the ground.
     * Plays the step sound of the block under the crocodile with a slightly higher pitch.
     */
    public void onRightFootDown() {
        playStepSoundFromAnimation(1.05f);
    }

    @Override
    protected double getBaseRiderYOffset() {
        float height = this.getVariant() == CrocodileVariant.Cosmetics.VERMILION_GUARDIAN.variant ? 0.4f : 0.5f;
        return this.getBbHeight() * height * this.getScale();
    }

    @Override
    protected float getRiderAnimYOffset() {
        return -bodyAnimY / 16.0f * this.getScale();
    }

    @Override
    public void aiStep() {
        if (this.entityData.get(IS_LUNGING)) tickPrimalDiveLunge();
        super.aiStep();
        if (this.isInWater() || this.onGround() && !this.isBaby()) {
            BlockPos currentPos = this.blockPosition();
            crocodileBehaviorHandler.trampleLilyPads(currentPos);
            crocodileBehaviorHandler.trampleLilyPads(currentPos.above());
        }
    }

    @Override
    public void travel(Vec3 vec3) {
        if (this.isChargingMouth()) {
            Vec3 movement = this.getDeltaMovement();
            float multiplier = this.isVehicle() ? 0.45f : 0.15f;
            this.setDeltaMovement(movement.x * multiplier, movement.y, movement.z * multiplier);
        }

        super.travel(vec3);

        if (this.onGround() && !isBaby() && this.horizontalCollision && !isSleeping() && !isNapping() && !this.isVehicle())
            this.jumpFromGround();
    }

    public void tick() {
        super.tick();
        crocodileTaming.tick();

        if (!this.isChargingMouth()) {
            if (!this.isTame() && this.isVehicle()) {
                createComboSimple(32, 15, OWSounds.CROCODILE_MOUTH_CRUSH.get(), 3.0, 2, 2.25, 0.15f);
            } else {
                createCombo(32, 15, OWSounds.CROCODILE_MOUTH_CRUSH.get(), 3.0, 2, 2.25, false, 0.15f);
            }
        }

        if (!this.level().isClientSide() && isPlayerMouthCharging()) {
            // Une charge n'appartient qu'à un pilote : sans lui, elle n'a plus de raison d'être et
            // laisserait la gueule ouverte aux yeux de tous les autres joueurs.
            if (this.getControllingPassenger() == null) {
                cancelMouthSlamCharge();
            } else {
                float current = getChargingMouthTimer();
                if (current < 60f) {
                    setChargingMouthTimer(Math.min(current + 1f, 60f));
                }
            }
        }

        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        if (this.isVehicle() && this.isTame() && !this.isSitting() && !this.isBaby()) setMadByRider(this.isCombo());

        if (!this.level().isClientSide()) {
            if (this.mouthSlamServerTimer > 0) {
                this.mouthSlamServerTimer--;
                if (this.mouthSlamServerTimer == 0) setMouthSlamming(false);
            }

            if (this.mouthSlamHitTimer > 0) {
                this.mouthSlamHitTimer--;
                if (this.mouthSlamHitTimer == 0) {
                    mouthSlamHitTimer = -1;
                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                                this.getX(), this.getY() + 0.8, this.getZ(),
                                20, 2.0, 0.4, 2.0, 0.15);
                    }
                    this.crocodileBehaviorHandler.performMouthSlamAttack(
                            mouthSlamPendingDamage, mouthSlamPendingKnockback, mouthSlamPendingBleed);
                }
            }

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

        if (this.getTarget() != null && this.getTarget().hasEffect(OWEffects.FRACTURE.getDelegate())) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.25, 1.0, 1.25));
        }

        if (!this.level().isClientSide() && !this.isBaby()) {
            if (grabCooldown > 0) grabCooldown--;
            if (deathRollCooldown > 0) deathRollCooldown--;
        }

        if (this.isInWater() && !this.isBaby()) {
            // La Gueule Béante est une attaque terrestre : dans l'eau on coupe la charge de l'IA
            // ET celle du pilote, faute de quoi la minuterie partagée oscillait entre 0 et 1 et
            // la gueule restait bloquée entrouverte aux yeux des autres joueurs.
            if (!this.level().isClientSide() && isPlayerMouthCharging()) cancelMouthSlamCharge();
            this.setChargingMouth(false);
            this.setChargingMouthTimer(0);
        }

        if (!this.level().isClientSide()) tickDeathRoll();

        if (primalDivePhase == 1 && !this.level().isClientSide()) {
            if (primalDiveTimer > 0) primalDiveTimer--;
            else cancelPrimalDive();
        }

        if (primalDivePhase == 2 && !this.level().isClientSide()) {
            if (primalDiveLungeTimer > 0) primalDiveLungeTimer--;

            Entity lungeRaw = this.level().getEntity(this.entityData.get(LUNGE_TARGET_ID));
            if (!(lungeRaw instanceof LivingEntity lungeTarget) || !lungeTarget.isAlive()) {
                cancelPrimalDive();
            } else {
                double distSq = this.distanceToSqr(lungeTarget);

                // Le bond expire sans avoir rejoint la proie : elle a distancé le crocodile, on
                // abandonne. L'ancienne version saisissait quand même, ce qui téléportait dans la
                // gueule une cible restée à trente blocs de là.
                if (primalDiveLungeTimer <= 0 && distSq > 9.0) {
                    cancelPrimalDive();
                } else if (distSq <= 6.0 || primalDiveLungeTimer <= 0) {
                    closePrimalDiveGrab(lungeTarget);
                }
            }
        }

        if (!this.level().isClientSide() && !this.isBaby()) tickGrab();

        markMudWithFootprints();
        handleGoldVariantEffects();

        if (!this.level().isClientSide()) {
            for (int i = 0; i < 3; i++) {
                if (tailParts[i] == null || tailParts[i].isRemoved()) {
                    spawnTailParts();
                    break;
                }
            }
            updateTailChain();
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        // La proie survit à son ravisseur : sans ce relâchement elle héritait de la mort du
        // crocodile avec noPhysics actif — traversée du décor et chute hors du monde.
        if (!this.level().isClientSide() && this.isGrabbing()) releaseGrab();

        super.die(damageSource); // le drop générique de l'Âme est géré par OWEntity.die()

        if (this.isSaddled()) {
            this.spawnAtLocation(acceptSaddle());
        }
    }

    /** L'apprivoisement change de camp : une prise en cours n'a plus lieu d'être. */
    @Override
    public void setTame(boolean tame, Player player) {
        if (tame && !this.level().isClientSide() && this.isGrabbing()) {
            releaseGrab();
            cancelPrimalDive();
            grabCooldown = 0;
        }
        super.setTame(tame, player);
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (!this.level().isClientSide()) {
            spawnTailParts();
        }
    }

    private void spawnTailParts() {
        for (int i = 0; i < 3; i++) {
            if (tailParts[i] != null && !tailParts[i].isRemoved()) {
                tailParts[i].discard();
            }
            CrocodileTailPart part = new CrocodileTailPart(
                    net.tiew.operationWild.entity.OWEntityRegistry.CROCODILE_TAIL_PART.get(),
                    this.level(), this, i);
            part.setPos(this.getX(), this.getY(), this.getZ());
            this.level().addFreshEntity(part);
            tailParts[i] = part;
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        // Déchargement de chunk compris : une bête figée par setNoAi resterait inerte à vie si
        // son ravisseur disparaissait sans la relâcher.
        if (!this.level().isClientSide() && this.isGrabbing()) releaseGrab(false);

        super.remove(reason);
        if (!this.level().isClientSide()) {
            for (CrocodileTailPart part : tailParts) {
                if (part != null && !part.isRemoved()) {
                    part.remove(reason);
                }
            }
        }
    }

    private void updateTailChain() {
        float scale = this.getScale();
        float segLen = SEG_DIST * scale;
        float bodyOff = BODY_TAIL_OFFSET * scale;

        float yawRad = (float) Math.toRadians(yBodyRot);
        double attachX = getX() + Math.sin(yawRad) * bodyOff;
        double attachY = getY();
        double attachZ = getZ() - Math.cos(yawRad) * bodyOff;

        if (!tailPosInit) {
            seg1Yaw = seg2Yaw = seg3Yaw = yBodyRot;
            // Initialise les segments à la bonne position de départ
            if (tailParts[0] != null) tailParts[0].setPos(
                    attachX + Math.sin((float) Math.toRadians(yBodyRot)) * segLen,
                    attachY,
                    attachZ - Math.cos((float) Math.toRadians(yBodyRot)) * segLen);
            if (tailParts[1] != null && tailParts[0] != null) tailParts[1].setPos(
                    tailParts[0].getX() + Math.sin((float) Math.toRadians(yBodyRot)) * segLen,
                    attachY,
                    tailParts[0].getZ() - Math.cos((float) Math.toRadians(yBodyRot)) * segLen);
            if (tailParts[2] != null && tailParts[1] != null) tailParts[2].setPos(
                    tailParts[1].getX() + Math.sin((float) Math.toRadians(yBodyRot)) * segLen,
                    attachY,
                    tailParts[1].getZ() - Math.cos((float) Math.toRadians(yBodyRot)) * segLen);
            tailPosInit = true;
        }

        seg1Yaw += Mth.wrapDegrees(yBodyRot - seg1Yaw) * 0.85f;
        seg2Yaw += Mth.wrapDegrees(seg1Yaw - seg2Yaw) * 0.65f;
        seg3Yaw += Mth.wrapDegrees(seg2Yaw - seg3Yaw) * 0.50f;

        // Ancre seg0 = point d'attache du body (Y réel du body)
        seg1[0] = attachX;
        seg1[1] = attachY;
        seg1[2] = attachZ;

        // Ancre seg1 = position réelle de seg0 après physique
        seg2[0] = (tailParts[0] != null && !tailParts[0].isRemoved()) ? tailParts[0].getX() : seg1[0];
        seg2[1] = (tailParts[0] != null && !tailParts[0].isRemoved()) ? tailParts[0].getY() : attachY;
        seg2[2] = (tailParts[0] != null && !tailParts[0].isRemoved()) ? tailParts[0].getZ() : seg1[2];

        // Ancre seg2 = position réelle de seg1 après physique
        seg3[0] = (tailParts[1] != null && !tailParts[1].isRemoved()) ? tailParts[1].getX() : seg2[0];
        seg3[1] = (tailParts[1] != null && !tailParts[1].isRemoved()) ? tailParts[1].getY() : seg2[1];
        seg3[2] = (tailParts[1] != null && !tailParts[1].isRemoved()) ? tailParts[1].getZ() : seg2[2];

        applyToTailPart(0, seg1, seg1Yaw);
        applyToTailPart(1, seg2, seg2Yaw);
        applyToTailPart(2, seg3, seg3Yaw);
    }

    private void applyToTailPart(int index, double[] pos, float yaw) {
        CrocodileTailPart part = tailParts[index];
        if (part == null || part.isRemoved()) {
            part = new CrocodileTailPart(
                    OWEntityRegistry.CROCODILE_TAIL_PART.get(),
                    this.level(), this, index);
            this.level().addFreshEntity(part);
            tailParts[index] = part;
        }
        part.setPos(pos[0], pos[1], pos[2]);
        part.refreshDimensions(); // Met à jour la bbox à la nouvelle position
        part.yRotO = part.getYRot();
        part.setYRot(yaw);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (this.isNapping() || this.isBaby()) {
            return;
        }

        super.setTarget(target);

        if (!isTame()) {
            setMad(!isBaby() && this.getTarget() != null && getSleepBarPercent() < 75 && !this.isSitting());
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        return super.doHurtTarget(entity);
    }

    /**
     * Suite d'une morsure de combo : c'est ici que naissent toutes les prises.
     *
     * <p>Les garde-fous communs (gabarit, alliés, cooldown, état) vivent désormais dans
     * {@code resolveGrabTarget} et {@code canStartGrab} : on ne décide plus ici que du
     * <b>contexte</b> — sauvage ou apprivoisé, sur terre ou dans l'eau.</p>
     */
    @Override
    public void hurtAfterCombo(LivingEntity entity, int comboAttack) {
        this.crocodileTaming.hurtAfterCombo(entity, comboAttack);

        if (this.level().isClientSide() || entity == null) return;
        // Le crocodile prêt à être apprivoisé ne mord plus pour saisir : il attend son dresseur.
        if (crocodileBehaviorHandler.isReadyForTaming()) return;
        if (!canStartGrab()) return;

        if (!this.isTame()) {
            // Sortie de l'eau réussie : la prochaine morsure agrippe, où qu'elle tombe.
            if (canGrabOnLand) {
                this.grabEntity(entity);
                return;
            }
            // Sinon il faut de l'eau à proximité — la proie n'est saisie que pour y être noyée.
            if (crocodileBehaviorHandler.findNearestWaterSource(10) == null) return;

            if (this.isInWater() || comboAttack == 3) {
                this.grabEntity(entity);
            }
            return;
        }

        // Passif « Accrochage Reptilien » : une morsure sur cinq près de l'eau.
        if (crocodileBehaviorHandler.findNearestWaterSource(10) != null
                && this.getRandom().nextInt(100) < 20) {
            grabEntityPassive(entity);
        }
    }

    @Override
    public boolean hurt(DamageSource damageSource, float v) {
        if (!this.isTame()) {
            if (this.isSitting()) this.setSitting(false);

            if (isStartingTaming() && this.getEntitiesKilledDuringTaming() > 0) {
                this.setEntitiesKilledDuringTaming(this.getEntitiesKilledDuringTaming() - 1);
            }
        }

        // Un tiers qui frappe le crocodile desserre sa mâchoire : on peut porter secours à une
        // victime qui ne s'en sortirait pas seule. Les coups de la proie elle-même ne comptent pas,
        // ils feraient double emploi avec le débattement au clic droit.
        if (!this.level().isClientSide() && this.isGrabbing() && this.getGrabTimeout() > 0) {
            Entity attacker = damageSource.getEntity();
            if (attacker != null && attacker != this.getGrabbedTarget()) {
                this.setGrabTimeout(Math.max(0, this.getGrabTimeout() - (int) Math.min(60f, v * 6f)));
            }
        }

        if (this.isInWater()) {
            Vec3 knockback = this.getDeltaMovement();
            boolean wasHurt = super.hurt(damageSource, v);

            if (wasHurt) {
                Vec3 newKnockback = this.getDeltaMovement();
                Vec3 appliedKnockback = newKnockback.subtract(knockback);

                this.setDeltaMovement(knockback.add(appliedKnockback.scale(0.05)));
            }

            return wasHurt;
        }

        return super.hurt(damageSource, v);
    }

    /**
     * Point de passage unique de <b>toutes</b> les mises à mort du crocodile.
     *
     * <p>L'apprivoisement se comptait auparavant dans {@code hurtAfterCombo}, c'est-à-dire au seul
     * moment de la morsure. Or près d'une source d'eau le crocodile ne tue justement pas par
     * morsure : il agrippe, traîne et noie — trois façons de tuer qui ne passaient jamais par là.
     * Les sacrifices offerts au bord de l'eau, comme les proies achevées par une roulade pendant le
     * dressage, n'étaient donc jamais comptés. Tout converge ici désormais.</p>
     */
    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        int kills = getUltimateKillCount();
        if (kills < OWAttacksConstants.Crocodile.PRIMAL_DIVE_KILLS_REQUIRED) {
            setUltimateKillCount(kills + 1);
        }
        this.crocodileTaming.onKilledEntity(entity);
        return super.killedEntity(serverLevel, entity);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof CrocodileEntity otherCrocodile) {
            if (otherCrocodile.isBaby()) {
                return true;
            }
            if (this.isTame()) {
                if (otherCrocodile.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherCrocodile.getOwnerUUID())) {
                    return true;
                }
            } else if (!otherCrocodile.isTame()) {
                return true;
            }
        }
        return super.isAlliedTo(entity);
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction function) {
        if (passenger == this.getGrabbedTarget()) {
            final double s = this.getScale();
            // Suspendue par le milieu du corps, pas posée par les pieds sur la mâchoire.
            final double hang = Math.min(0.9f, passenger.getBbHeight() * 0.5f) * s;
            // Recul dans l'axe des mâchoires : les carrures larges doivent avancer d'autant, sans
            // quoi leur volume rentrait dans le museau.
            final float localZ = -(MOUTH_HOLD_LOCAL_Z + passenger.getBbWidth() * 0.35f);

            double px, py, pz;
            float jawYaw = this.yBodyRot;
            org.joml.Matrix4f jaws = this.mouthMatrix;

            if (jaws != null) {
                // Point d'accroche exprimé DANS l'os de la gueule, puis ramené au monde par la
                // chaîne animée : la proie suit alors tout ce que fait la tête — le fouet du cou,
                // la secousse, le tonneau de la roulade — au lieu de flotter devant le poitrail.
                org.joml.Vector3f hold = jaws.transformPosition(new org.joml.Vector3f(0f, 0f, localZ));

                // Repère modèle → monde : Y descend, l'origine est MODEL_ORIGIN_Y au-dessus de
                // l'entité, et le rendu applique une rotation de (180° − lacet du corps).
                double ax = -hold.x;
                double ay = MODEL_ORIGIN_Y - hold.y;
                double az = hold.z;

                double yawRad = Math.toRadians(this.yBodyRot);
                double sin = Math.sin(yawRad), cos = Math.cos(yawRad);

                px = this.getX() + (-ax * cos + az * sin) * s;
                pz = this.getZ() + (-ax * sin - az * cos) * s;
                py = this.getY() + ay * s - hang;
                jawYaw = this.yBodyRot + this.mouthYRotDeg;

            } else {
                // Côté serveur, la chaîne d'os n'existe pas : on retombe sur la pose de repos, qui
                // redonne exactement les mêmes distances (1,5 bloc de chaîne + le recul local).
                double yawRad = Math.toRadians(this.yBodyRot);
                double forward = (MOUTH_HOLD_FORWARD - MOUTH_HOLD_LOCAL_Z - localZ) * s;
                px = this.getX() - Math.sin(yawRad) * forward;
                pz = this.getZ() + Math.cos(yawRad) * forward;
                py = this.getY() + MOUTH_HOLD_HEIGHT * s - hang;
            }

            passenger.fallDistance = 0f;
            // On oriente la proie dans l'axe des mâchoires, sauf s'il s'agit d'un joueur : lui
            // pivoter la caméra à chaque tick l'empêcherait de voir ce qui lui arrive.
            if (passenger instanceof LivingEntity prey && !(passenger instanceof Player)) {
                prey.setYRot(jawYaw);
                prey.setYBodyRot(jawYaw);
                prey.yHeadRot = jawYaw;
                prey.setXRot(Mth.clamp(-this.mouthXRotDeg, -80f, 80f));
            }
            function.accept(passenger, px, py, pz);
            return;
        }
        if (!this.hasPassenger(passenger) || this.touchingUnloadedChunk()) return;

        final float s = this.getScale();
        final double baseY = getBaseRiderYOffset();

        // Position VOULUE du siege dans le repere du MODELE, au repos : centre en X et en Z, et a la
        // hauteur d'assise. Y descend dans ce repere, d'ou la soustraction.
        float mx = 0f;
        float my = (float) (MODEL_ORIGIN_Y - baseY / s);
        float mz = 0f;

        // Le MEME point, exprime cette fois dans le repere LOCAL du dernier os — c'est ce que
        // transformPosition attend, et non une coordonnee du modele.
        //
        // Le confondre avec la precedente etait le defaut : au repos la chaine ne fait que translater
        // de REST, donc le point reellement pivote se trouvait 0,78 bloc SOUS le siege. A plat l'ecart
        // etait nul et rien ne se voyait, mais des qu'une rotation entrait en jeu le bras de levier
        // etait faux — le tangage enfoncait le cavalier et le roulis le decalait du mauvais montant.
        float lx = mx - REST_X / 16f;
        float ly = my - REST_Y / 16f;
        float lz = mz - REST_Z / 16f;

        // Ecart entre la pose animee et la pose de repos, mesure sur la matrice elle-meme. Elle porte
        // les rotations autant que les translations : le tangage de montee ou de descente y est, avec
        // son vrai pivot, la ou bodyAnimX/bodyAnimY ne rendaient que le glissement des os.
        double dx = 0, dy = 0, dz = 0;
        org.joml.Matrix4f bones = this.boneMatrix;
        if (bones != null) {
            org.joml.Vector3f now = bones.transformPosition(new org.joml.Vector3f(lx, ly, lz));
            dx = now.x - mx;
            dy = now.y - my;
            dz = now.z - mz;
        }

        double seatX = dx * s;
        double seatY = -dy * s;
        double seatZ = -dz * s;

        double yRad = Math.toRadians(-this.yBodyRot);
        double worldX = seatX * Math.cos(yRad) + seatZ * Math.sin(yRad);
        double worldZ = -seatX * Math.sin(yRad) + seatZ * Math.cos(yRad);

        passenger.fallDistance = 0f;
        function.accept(passenger, this.getX() + worldX, this.getY() + baseY + seatY, this.getZ() + worldZ);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        crocodileTaming.mobInteract(player, hand);
        return super.mobInteract(player, hand);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));

            this.setVariant(chooseCrocodileVariant());
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

    public static boolean checkCrocodileSpawnRules(EntityType<? extends Animal> animal, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        BlockState blockBelow = level.getBlockState(pos.below());
        Block blockType = blockBelow.getBlock();

        boolean validBlock = blockType == Blocks.MUD || blockType == Blocks.GRASS_BLOCK || blockType == Blocks.WATER;

        if (!validBlock) {
            return false;
        }

        boolean waterNearby = false;
        int searchRadius = 16;

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos checkPos = pos.offset(x, y, z);
                    BlockState state = level.getBlockState(checkPos);

                    if (state.getBlock() == Blocks.WATER) {
                        waterNearby = true;
                        break;
                    }
                }
                if (waterNearby) break;
            }
            if (waterNearby) break;
        }

        return waterNearby;
    }

    private void markMudWithFootprints() {
        if (this.getTarget() == null && this.tickCount % 40 == 0 && isMoving()) {
            BlockPos blockPos = this.blockPosition();
            BlockState blockState = this.level().getBlockState(blockPos);

            if (blockState.is(Blocks.MUD)) {
                Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(this.level().getRandom());
                BlockState mudState = OWBlocks.MARKED_MUD.get().defaultBlockState().setValue(MarkedMudBlock.FACING, facing);

                this.level().setBlockAndUpdate(blockPos, mudState);
            }
        }
    }

    @Override
    protected int getDefaultSkinIndex() {
        return 7; // index 1 = GOLD, … 7 réservés, 8 = reset (no skin)
    }

    // ==================================================
    //             GRAB & MOBILITÉ
    // ==================================================

    /**
     * Libère proprement une cible attrapée : rend la physique, la démonte, stoppe la roulade et
     * réveille l'IA que l'ultime avait mise en sommeil. <b>Tout</b> relâchement doit passer par ici :
     * une sortie qui oubliait {@code noPhysics} laissait la victime traverser les blocs jusqu'à sa
     * déconnexion, et une qui oubliait {@code setNoAi} laissait une bête inerte à vie.
     */
    public void releaseGrab() {
        releaseGrab(true);
    }

    /**
     * @param notifyNeighbours balaye les congénères pour effacer leur rancune. À laisser à
     *                         {@code false} lors d'un déchargement de chunk : parcourir les entités
     *                         alentour au moment où le monde se range n'apporte rien et coûte cher.
     */
    public void releaseGrab(boolean notifyNeighbours) {
        LivingEntity grabbed = this.getGrabbedTarget();
        if (grabbed != null) {
            grabbed.noPhysics = false;
            if (grabbed.getVehicle() == this) grabbed.stopRiding();
            if (isPrimalDiveGrab && grabbed instanceof Mob grabbedMob) {
                grabbedMob.setNoAi(false);
            }
        }
        isPrimalDiveGrab = false;
        this.setGrabbing(false, null);
        this.setGrabTimeout(0);
        this.setDeathRolling(false);
        this.setDeathRollProgress(0);
        this.setTarget(null);
        grabHoldTimer = 0;
        deathRollQueued = 0;
        deathRollDryTicks = 0;
        if (notifyNeighbours) clearNearbyCrocodileTargets();
    }

    /**
     * Les congénères qui avaient pris ce crocodile pour cible pendant qu'il tenait sa proie
     * oublient leur rancune une fois la prise finie — sans quoi une meute entière se déchirait
     * autour de la carcasse.
     */
    private void clearNearbyCrocodileTargets() {
        if (this.level().isClientSide()) return;
        this.level().getEntitiesOfClass(CrocodileEntity.class, this.getBoundingBox().inflate(30))
                .forEach(otherCroc -> {
                    if (otherCroc != this && otherCroc.getTarget() == this) otherCroc.setTarget(null);
                });
    }

    /**
     * La proie n'est un passager que pour être portée : elle ne pilote rien.
     *
     * <p>Sans cette exception, {@code getFirstPassenger()} rendait la victime, que la classe mère
     * prenait pour un cavalier. Le crocodile passait alors dans la branche « monture » : plus aucun
     * appel à {@code travel}, donc plus le moindre déplacement. Il restait planté à l'endroit exact
     * de la morsure, incapable de rejoindre l'eau — le défaut central du grab.</p>
     */
    @Override
    public LivingEntity getControllingPassenger() {
        LivingEntity grabbed = this.getGrabbedTarget();
        if (grabbed != null && this.getFirstPassenger() == grabbed) return null;
        return super.getControllingPassenger();
    }

    /**
     * Machine d'état de la prise, côté serveur uniquement.
     *
     * <p>Ordre volontaire : on valide d'abord que la cible existe encore, ensuite seulement on
     * applique l'usure. L'ancienne version faisait l'inverse dans un {@code try/catch} qui avalait
     * les {@code NullPointerException} — les prises fantômes (cible morte ou déchargée) survivaient
     * indéfiniment, gueule ouverte et IA bloquée.</p>
     */
    private void tickGrab() {
        if (!this.isGrabbing()) {
            if (grabHoldTimer != 0) grabHoldTimer = 0;
            return;
        }

        LivingEntity grabbed = this.getGrabbedTarget();
        if (grabbed == null || !grabbed.isAlive() || grabbed.isRemoved()
                || grabbed.level() != this.level()
                || (grabbed instanceof Player player && (player.isCreative() || player.isSpectator()))) {
            releaseGrab();
            return;
        }

        // Prise à durée fixe (passif apprivoisé, ultime) : elle prime sur l'usure du timeout.
        if (grabHoldTimer > 0) {
            grabHoldTimer--;
            if (grabHoldTimer <= 0) {
                playGrabReleaseFeedback();
                releaseGrab();
                return;
            }
        }

        grabbed.noPhysics = true;
        grabbed.fallDistance = 0f;

        if (!(grabbed instanceof Player) && grabbed instanceof Mob grabbedMob) {
            grabbedMob.setTarget(null);
        }

        if (this.isInWater()) {
            this.setLookAt(grabbed.getX(), grabbed.getY(), grabbed.getZ());
        }

        // Jauge vidée : la victime s'est libérée. Le relâchement est fait ici et pas seulement à
        // la réception du paquet client, pour que le secours d'un tiers fonctionne aussi sur une
        // proie qui n'est pas un joueur — et qui n'a donc personne pour envoyer ce paquet.
        if (grabHoldTimer <= 0 && this.getGrabTimeout() <= 0) {
            playGrabReleaseFeedback();
            releaseGrab();
            return;
        }

        // Seule une prise SANS minuterie fixe s'use : le joueur la fait reculer au clic droit
        // (voir OWEntityGrabManagerPacket) et la subit s'il ne se débat pas.
        if (grabHoldTimer <= 0 && grabbed instanceof Player) {
            this.setGrabTimeout(this.getGrabTimeout() + 1);

            if (this.getGrabTimeout() >= getGrabMaxTimeout()) {
                this.setGrabTimeout(0);
                // Dégâts attribués au crocodile plutôt qu'un kill() anonyme : l'apprivoisement, la
                // charge de l'ultime, les avancements et les statistiques de mort en dépendent tous.
                grabbed.invulnerableTime = 0;
                grabbed.hurt(this.damageSources().mobAttack(this), Float.MAX_VALUE);
                clearNearbyCrocodileTargets();
                return;
            }
        }

        if (!grabbed.isPassenger()) {
            grabbed.startRiding(this, true);
        }

        // Sous l'eau, la proie tenue en gueule ne respire plus.
        if (this.isInWater() && grabbed.isInWater()
                && !grabbed.getType().is(net.minecraft.tags.EntityTypeTags.CAN_BREATHE_UNDER_WATER)) {
            grabbed.setAirSupply(Math.max(-20, grabbed.getAirSupply() - 4));
        }

        if (this.tickCount % 40 == 0) {
            this.level().playSound(null, getX(), getY(), getZ(), OWSounds.CROCODILE_IDLE_2.get(),
                    SoundSource.HOSTILE, 0.9f, (float) OWUtils.generateRandomInterval(0.7, 0.9));
        }
    }

    /**
     * Séquence complète de la roulade : armement, rotation, dégâts, sortie.
     *
     * <p>La progression est une donnée synchronisée : c'est elle qui pilote les animations sur
     * TOUS les clients, pas seulement celui du cavalier — indispensable en multijoueur.</p>
     */
    private void tickDeathRoll() {
        if (this.isBaby()) return;
        if (!this.isDeathRolling() && !this.isGrabbing()) return;

        LivingEntity grabbed = this.getGrabbedTarget();

        // Une roulade n'a de sens que sur une proie tenue, dans l'eau — mais en surface,
        // isInWater() clignote d'un tick à l'autre au gré des vagues et de la remontée. Le tester
        // sèchement avortait la rotation presque à chaque essai : il fallait un crocodile
        // parfaitement immobile et bien immergé pour qu'elle aille au bout. On tolère donc quelques
        // ticks hors de l'eau avant d'abandonner.
        if (this.isDeathRolling()) {
            if (grabbed == null || !this.isGrabbing()) {
                stopDeathRoll();
                return;
            }
            if (this.isInWaterForDeathRoll()) {
                deathRollDryTicks = 0;
            } else if (++deathRollDryTicks > DEATH_ROLL_DRY_GRACE) {
                stopDeathRoll();
                return;
            }
        } else {
            deathRollDryTicks = 0;
        }

        // Demande de roulade reçue pendant la récupération : elle repart dès que possible.
        if (deathRollQueued > 0 && !this.isDeathRolling()) {
            deathRollQueued--;
            if (deathRollCooldown <= 0) {
                deathRollQueued = 0;
                startDeathRoll();
                return;
            }
        }

        // Crocodile sauvage : il noie sa proie de lui-même, à intervalle régulier.
        if (!this.isDeathRolling() && grabbed != null && !this.isTame()
                && this.isInWaterForDeathRoll() && deathRollCooldown <= 0) {
            startDeathRoll();
            return;
        }

        if (!this.isDeathRolling()) return;

        int progress = this.getDeathRollProgress() + 1;
        this.setDeathRollProgress(progress);

        // On maintient le duo juste sous la surface : la rotation se voit, et la proie ne peut
        // pas s'échapper vers le fond. Le freinage garde un plancher de dérive : l'amortissement
        // sec d'avant clouait la bête à vitesse nulle en trois ticks, ce qui donnait une roulade
        // sur place, raide. Elle continue maintenant d'avancer lentement en tournoyant.
        Vec3 velocity = this.getDeltaMovement();
        double driftX = velocity.x * 0.9;
        double driftZ = velocity.z * 0.9;
        double horizontal = Math.sqrt(driftX * driftX + driftZ * driftZ);

        if (horizontal < DEATH_ROLL_DRIFT) {
            double yawRad = Math.toRadians(this.yBodyRot);
            driftX = -Math.sin(yawRad) * DEATH_ROLL_DRIFT;
            driftZ = Math.cos(yawRad) * DEATH_ROLL_DRIFT;
        }

        double rise = this.level().getSeaLevel() - this.getY() >= 2 ? 0.035 : velocity.y * 0.9;
        this.setDeltaMovement(driftX, rise, driftZ);

        int spinTick = progress - DEATH_ROLL_WINDUP_TICKS;

        // Craquements d'ossements : trois par rotation, espacés, pas à chaque morsure — c'est ce
        // qui les garde saisissants plutôt que bruyants.
        if (spinTick == 7 || spinTick == 19 || spinTick == DEATH_ROLL_SPIN_TICKS - 4) {
            playBoneCrack();
        }

        // Cinq morsures, réparties sur la rotation quelle que soit sa durée : c'est ce total que
        // la description de l'ultime annonce au joueur.
        if (spinTick > 0 && spinTick % 6 == 0 && spinTick <= 30) {
            grabbed.invulnerableTime = 0;
            // Cinq morsures par roulade. La part est plus basse sur un joueur : il encaisse en plus
            // la noyade et doit garder de quoi se débattre, là où une bête n'a que ses points de vie.
            float damage = this.getDamage() * (grabbed instanceof Player ? 0.15f : 0.35f);
            grabbed.hurt(this.damageSources().mobAttack(this), damage);

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SPLASH,
                        this.getX(), this.getY() + 0.5, this.getZ(), 24, 0.9, 0.4, 0.9, 0.12);
                serverLevel.sendParticles(ParticleTypes.BUBBLE,
                        this.getX(), this.getY() + 0.3, this.getZ(), 18, 0.8, 0.4, 0.8, 0.05);
            }
            this.level().playSound(null, getX(), getY(), getZ(),
                    RANDOM(2) ? OWSounds.CROCODILE_HIT_1.get() : OWSounds.CROCODILE_HIT_2.get(),
                    SoundSource.HOSTILE, 1.1f, (float) OWUtils.generateRandomInterval(0.85, 1.05));
        }

        if (progress >= DEATH_ROLL_TOTAL_TICKS) {
            if (grabbed.isAlive()) {
                grabbed.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        OWEffects.FRACTURE.getDelegate(), 200, 0));
            }
            stopDeathRoll();
        }
    }

    /**
     * Test d'eau tolérant, réservé à la roulade.
     *
     * <p>{@code isInWater()} ne vaut vrai que si le volume de la bête chevauche réellement du
     * fluide, et un crocodile qui flotte en surface en ressort d'un tick sur l'autre au gré des
     * vagues. Le tester sèchement rendait la roulade capricieuse : le clic était avalé sans même
     * partir au serveur, et il fallait un crocodile parfaitement immobile et bien enfoncé pour
     * qu'elle se déclenche. On accepte donc aussi le bloc sous les pattes.</p>
     */
    /**
     * Cible recevable par le Plongeon Primal, filtre unique partagé par la désignation et par
     * l'affichage du réticule — les deux divergeaient, et le joueur voyait marquées des créatures
     * que la touche ne prenait pas.
     */
    public boolean canPrimalDiveTarget(LivingEntity candidate) {
        if (candidate == null || candidate == this) return false;
        if (!candidate.isAlive() || candidate.isRemoved() || !candidate.isInWater()) return false;
        if (candidate == this.getControllingPassenger()) return false;
        if (candidate instanceof CrocodileEntity) return false;
        if (candidate instanceof OWEntity owEntity && owEntity.getTheoreticalScale() >= 10) return false;
        if (candidate instanceof Player player && (player.isCreative() || player.isSpectator())) return false;
        if (this.isAlliedTo(candidate) || this.isTameGrabAlly(candidate)) return false;
        return true;
    }

    public boolean isInWaterForDeathRoll() {
        if (this.isInWater()) return true;
        return this.level().getFluidState(this.blockPosition()).is(net.minecraft.tags.FluidTags.WATER)
                || this.level().getFluidState(this.blockPosition().below()).is(net.minecraft.tags.FluidTags.WATER);
    }

    /**
     * Amorce une roulade si l'état le permet.
     *
     * <p>Une demande arrivée pendant la seule récupération n'est pas jetée : elle est <b>mise en
     * attente</b> et repart d'elle-même dès la fin du délai. Sans cette mémoire, le cavalier devait
     * tomber pile dans la fenêtre où la roulade redevient possible ; à côté, son clic disparaissait
     * sans le moindre signe, ce qui donnait l'impression qu'il fallait marteler le bouton vingt fois
     * pour obtenir une rotation.</p>
     *
     * @return {@code true} si la rotation démarre à l'instant.
     */
    public boolean startDeathRoll() {
        if (this.level().isClientSide() || this.isBaby()) return false;
        if (this.isDeathRolling()) return false;
        if (!this.isGrabbing() || this.getGrabbedTarget() == null || !this.isInWaterForDeathRoll()) return false;

        if (deathRollCooldown > 0) {
            deathRollQueued = DEATH_ROLL_INPUT_BUFFER_TICKS;
            return false;
        }
        deathRollQueued = 0;

        this.setDeathRolling(true);
        this.setDeathRollProgress(0);
        deathRollCooldown = DEATH_ROLL_TOTAL_TICKS + DEATH_ROLL_COOLDOWN_TICKS;

        this.level().playSound(null, getX(), getY(), getZ(), OWSounds.CROCODILE_MOUTH_CRUSH.get(),
                SoundSource.HOSTILE, 1.6f, 0.65f);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP,
                    this.getX(), this.getY() + 0.4, this.getZ(), 40, 1.0, 0.4, 1.0, 0.2);
        }
        return true;
    }

    private void stopDeathRoll() {
        this.setDeathRolling(false);
        this.setDeathRollProgress(0);
    }

    /**
     * Craquement d'ossements de la roulade.
     *
     * <p>Le râle de mort du squelette, gardé <b>discret</b> : à volume plein il s'entendrait comme
     * un squelette qui meurt à côté, pas comme une carcasse qui cède sous les mâchoires. Transposé
     * légèrement grave, à faible volume, il passe pour ce qu'on veut lui faire dire. La hauteur est
     * tirée au hasard pour que deux roulades de suite ne sonnent pas identiques.</p>
     */
    private void playBoneCrack() {
        this.level().playSound(null, getX(), getY(), getZ(),
                net.minecraft.sounds.SoundEvents.SKELETON_DEATH, SoundSource.HOSTILE,
                0.42f, (float) OWUtils.generateRandomInterval(0.72, 0.88));
    }

    private void playGrabReleaseFeedback() {
        this.level().playSound(null, getX(), getY(), getZ(), OWSounds.CROCODILE_IDLE_4.get(),
                SoundSource.HOSTILE, 1.2f, (float) OWUtils.generateRandomInterval(0.8, 1.0));
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    this.getX(), this.getY() + 0.5, this.getZ(), 15, 0.7, 0.3, 0.7, 0.08);
        }
    }

    /**
     * Filtre commun à toutes les prises : gabarit, alliés, cible déjà saisie, état du crocodile.
     * Renvoie la cible réellement saisissable (le cavalier plutôt que sa monture) ou {@code null}.
     */
    private LivingEntity resolveGrabTarget(LivingEntity entity) {
        if (entity == null || this.isBaby()) return null;

        if (entity instanceof BoaTailPart tailPart) {
            if (tailPart.getParent() instanceof BoaEntity boaHead) entity = boaHead;
            else return null;
        }

        if (entity instanceof TamableAnimal tamableAnimal && tamableAnimal.getControllingPassenger() instanceof LivingEntity rider) {
            entity = rider;
        }

        if (entity == this) return null;
        if (entity instanceof CrocodileEntity || entity instanceof BoaEntity || entity instanceof BoaTailPart) return null;
        if (entity instanceof OWEntity owEntity && owEntity.getTheoreticalScale() >= 10) return null;
        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) return null;
        if (!entity.isAlive() || entity.isRemoved()) return null;
        if (this.isTameGrabAlly(entity)) return null;
        // Déjà dans la gueule d'un autre : deux crocodiles ne se disputent pas la même proie.
        if (entity.getVehicle() instanceof CrocodileEntity holder && holder.getGrabbedTarget() == entity) return null;

        return entity;
    }

    private boolean canStartGrab() {
        return !this.level().isClientSide()
                && !this.isBaby()
                && !this.isGrabbing()
                && !this.isSleeping()
                && !this.isNapping()
                && !this.isSitting()
                && !this.isChargingMouth()
                && grabCooldown <= 0
                && this.getHealth() >= 10;
    }

    private void grabEntity(LivingEntity rawTarget) {
        LivingEntity entity = resolveGrabTarget(rawTarget);
        if (entity == null || !canStartGrab()) return;

        int[] slidingLevels = getSlidingLevels(entity);
        float[] slidingMultiplier = OWEnchantments.SLIDING_ARMOR_MULTIPLIERS;
        int chance = this.getRandom().nextInt(100);

        float chancesToAvoidingGrab = calculateChanceToAvoidingGrab(slidingLevels, slidingMultiplier);
        if (chance < chancesToAvoidingGrab) return;

        if (entity instanceof OWEntity owEntity && owEntity.getTheoreticalScale() > 20) return;

        this.setGrabbing(true, entity);
        if (!this.isGrabbing()) return;

        this.setGrabTimeout(GRAB_START_TIMEOUT);
        grabHoldTimer = 0;
        grabCooldown = MAX_GRAB_COOLDOWN;
        deathRollCooldown = DEATH_ROLL_WINDUP_TICKS;
        playGrabFeedback();
    }

    private void grabEntityPassive(LivingEntity rawTarget) {
        LivingEntity entity = resolveGrabTarget(rawTarget);
        if (entity == null || !canStartGrab()) return;

        this.setGrabbing(true, entity);
        if (!this.isGrabbing()) return;

        // Prise passive : durée fixe, jauge de débattement pleine dès la morsure. Sans ce
        // timeout de départ, la jauge démarrait à zéro et le premier clic droit libérait
        // aussitôt la victime — le passif ne servait à rien.
        this.setGrabTimeout(GRAB_START_TIMEOUT);
        grabHoldTimer = PASSIVE_GRAB_TICKS;
        grabCooldown = MAX_GRAB_COOLDOWN;
        deathRollCooldown = DEATH_ROLL_WINDUP_TICKS;
        playGrabFeedback();
    }

    private void playGrabFeedback() {
        this.level().playSound(null, getX(), getY(), getZ(), OWSounds.CROCODILE_MOUTH_CRUSH.get(),
                SoundSource.HOSTILE, 1.8f, (float) OWUtils.generateRandomInterval(0.7, 0.85));
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                    this.getX(), this.getY() + 0.7, this.getZ(), 8, 0.6, 0.3, 0.6, 0.05);
        }
    }

    private float calculateChanceToAvoidingGrab(int[] slidingLevels, float[] slidingMultiplier) {
        return (slidingLevels[0] * slidingMultiplier[0]) + (slidingLevels[1] * slidingMultiplier[1]) + (slidingLevels[2] * slidingMultiplier[2]) + (slidingLevels[3] * slidingMultiplier[3]);
    }

    private int[] getSlidingLevels(LivingEntity entity) {
        int[] slidingLevels = new int[4];
        EquipmentSlot[] slots = {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        };

        for (int i = 0; i < slots.length; i++) {
            ItemStack armor = entity.getItemBySlot(slots[i]);
            if (!armor.isEmpty()) {
                slidingLevels[i] = armor.getEnchantmentLevel(this.level().registryAccess()
                        .registryOrThrow(Registries.ENCHANTMENT)
                        .getHolderOrThrow(OWEnchantments.SLIDING));
            }
        }

        return slidingLevels;
    }

    private void handleGoldVariantEffects() {
        if (this.getVariant() == CrocodileVariant.SKIN_GOLD && this.tickCount % 150 == 0) {
            OWUtils.spawnParticles(this, ParticleTypes.END_ROD, 0, 0, 0, 5, 2);
        }
    }

    private CrocodileVariant chooseCrocodileVariant() {
        CrocodileVariant variant;
        if (chance >= 66) variant = CrocodileVariant.GREEN;
        else if (chance >= 33) variant = CrocodileVariant.DARK;
        else variant = CrocodileVariant.DEFAULT;
        return variant;
    }

    protected void handleMiscIdleAnimations() {
        if (this.growlsAnimationState.isStarted()
                && this.tickCount - growlsAnimationStartTime > GROWLS_DURATION) {
            this.growlsAnimationState.stop();
        }
        if (this.gruntAnimationState.isStarted()
                && this.tickCount - gruntAnimationStartTime > GRUNT_DURATION) {
            this.gruntAnimationState.stop();
        }

        if (!this.level().isClientSide()) return;

        boolean canPlay = crocodileBehaviorHandler.canPlayIdleAnimation()
                && !crocodileBehaviorHandler.isAnyIdleAnimationPlaying();

        if (growlsCooldown > 0) {
            growlsCooldown--;
        } else {
            if (canPlay) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(),
                        OWSounds.CROCODILE_IDLE_3.get(), this.getSoundSource(),
                        1.0F, isBaby() ? 2.0F : 1.0F, false);
                if (crocodileBehaviorHandler.canGrowl()) {
                    this.growlsAnimationState.start(this.tickCount);
                    growlsAnimationStartTime = this.tickCount;
                }
            }
            growlsCooldown = (int) OWUtils.generateRandomInterval(400, 1200);
        }

        if (gruntCooldown > 0) {
            gruntCooldown--;
        } else {
            if (canPlay && !this.growlsAnimationState.isStarted()) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(),
                        OWSounds.CROCODILE_IDLE_1.get(), this.getSoundSource(),
                        1.0F, isBaby() ? 2.0F : 1.0F, false);
                if (crocodileBehaviorHandler.canGrunt()) {
                    this.gruntAnimationState.start(this.tickCount);
                    gruntAnimationStartTime = this.tickCount;
                }
            }
            gruntCooldown = (int) OWUtils.generateRandomInterval(300, 500);
        }
    }

    private void setupAnimationState() {
        createIdleAnimation(96, true);
        createSitAnimation(96, true);

        handleMiscIdleAnimations();

        if (this.isInWater()) {
            if (this.idleWaterAnimationTimeout <= 0) {
                this.idleWaterAnimationTimeout = 57;
                this.idleWaterAnimationState.start(this.tickCount);
            } else --this.idleWaterAnimationTimeout;
        }

        if (!this.isInWater()) {
            this.idleWaterAnimationTimeout = 0;
            this.idleWaterAnimationState.stop();
        }

        if (this.isNapping()) {
            if (this.napAnimationStartTime <= 0) {
                this.napAnimationStartTime = 200;
                this.napAnimationState.start(this.tickCount);
            } else --this.napAnimationStartTime;
        }

        if (!this.isNapping()) {
            this.napAnimationStartTime = 0;
            this.napAnimationState.stop();
        }

        setupGrabAnimationStates();

        if (this.isMouthSlamming()) {
            if (this.mouthSlamAnimTimer <= 0) {
                this.mouthSlamAnimTimer = Integer.MAX_VALUE;
                this.mouthSlamAnimState.start(this.tickCount);
            } else --this.mouthSlamAnimTimer;
        } else {
            this.mouthSlamAnimTimer = 0;
            this.mouthSlamAnimState.stop();
        }

        setupComboAnimations();
    }

    /**
     * Aiguillage des trois animations de prise, piloté par la <b>progression synchronisée</b> de la
     * roulade et non par une horloge locale : armement (0 → 10 ticks), rotation (10 → 50), maintien
     * le reste du temps. Tous les clients qui voient le crocodile jouent donc la même chose au même
     * instant, y compris ceux qui ne le montent pas.
     */
    private void setupGrabAnimationStates() {
        boolean rolling = this.isDeathRolling();
        int progress = this.getDeathRollProgress();
        boolean winding = rolling && progress < DEATH_ROLL_WINDUP_TICKS;

        if (winding) {
            this.deathRollWindupAnimState.startIfStopped(this.tickCount);
            this.deathRollAnimationState.stop();
        } else if (rolling) {
            this.deathRollWindupAnimState.stop();
            this.deathRollAnimationState.startIfStopped(this.tickCount);
        } else {
            this.deathRollWindupAnimState.stop();
            this.deathRollAnimationState.stop();
        }

        if (hasGrabSomething() && !rolling) {
            this.grabHoldAnimState.startIfStopped(this.tickCount);
        } else {
            this.grabHoldAnimState.stop();
        }

        // Secousse : une fois à la morsure, puis à intervalle irrégulier tant que la proie tient.
        if (hasGrabSomething() && !rolling) {
            if (this.grabThrashAnimStartTime <= 0) {
                this.grabThrashAnimStartTime = this.tickCount;
                this.grabThrashAnimState.start(this.tickCount);
            } else if (this.tickCount - this.grabThrashAnimStartTime > 60) {
                this.grabThrashAnimStartTime = this.tickCount;
                this.grabThrashAnimState.start(this.tickCount);
            }
        } else {
            this.grabThrashAnimStartTime = 0;
            this.grabThrashAnimState.stop();
        }
    }

    private void setupComboAnimations() {
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, 37);
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, 37);
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, 37);
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
    public void setVariant(OWEntity entity, int variant) {
        if (entity instanceof CrocodileEntity croc) {
            croc.setVariant(CrocodileVariant.byId(variant));
            croc.setInitialVariant(CrocodileVariant.byId(variant));
        }
    }

    public CrocodileVariant getVariant() {
        return CrocodileVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(CrocodileVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
    }

    public void setSkin(CrocodileVariant skin) {
        this.setVariant(skin);
    }

    @Override
    public void changeSkin(int skinIndex, boolean playingEffects) {
        super.changeSkin(skinIndex, playingEffects);
        this.setVariant(getInitialVariant());

        switch (skinIndex) {
            case 1 -> this.setSkin(CrocodileVariant.Cosmetics.GOLD.variant);
            case 2 -> this.setSkin(CrocodileVariant.Cosmetics.VERMILION_GUARDIAN.variant);
            case 3 -> this.setSkin(CrocodileVariant.Cosmetics.TOY.variant);
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

    public CrocodileVariant getInitialVariant() {
        return CrocodileVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(CrocodileVariant variant) {
        this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());
    }

    public void setPlayerMouthCharging(boolean value) { this.entityData.set(IS_PLAYER_MOUTH_CHARGING, value); }
    public boolean isPlayerMouthCharging() { return this.entityData.get(IS_PLAYER_MOUTH_CHARGING); }

    public void setChargingMouth(boolean isChargingMouth) {
        this.entityData.set(IS_CHARGING_MOUTH, isChargingMouth);
    }

    public boolean isChargingMouth() {
        return this.entityData.get(IS_CHARGING_MOUTH);
    }

    public void setMouthSlamming(boolean value) {
        this.entityData.set(IS_MOUTH_SLAMMING, value);
    }

    public boolean isMouthSlamming() {
        return this.entityData.get(IS_MOUTH_SLAMMING);
    }

    public void setMad(boolean isMad) {
        if (isMad) if (this.getCurrentMode() == Mode.Passive) return;
        this.entityData.set(IS_MAD, isMad);
    }

    /**
     * Colère déclenchée par le <b>cavalier</b>, qui ignore le mode passif.
     *
     * <p>Le mode ne règle que l'initiative de l'IA : une monture passive ne part pas d'elle-même à
     * l'attaque. Il n'a rien à dire quand c'est son cavalier qui frappe — or {@link #setMad(boolean)}
     * refusait tout net en passif, et comme une bête apprivoisée l'est par défaut, ses yeux ne
     * s'allumaient jamais en combat monté.</p>
     */
    public void setMadByRider(boolean isMad) {
        this.entityData.set(IS_MAD, isMad);
    }

    public boolean isMad() {
        return this.entityData.get(IS_MAD);
    }

    public void setDeathRollProgress(int getDeathRollProgress) {
        this.entityData.set(DEATH_ROLLING_PROGRESS, getDeathRollProgress);
    }

    public int getDeathRollProgress() {
        return Math.min(this.entityData.get(DEATH_ROLLING_PROGRESS), DEATH_ROLL_TOTAL_TICKS);
    }

    public void setStartingTaming(boolean isStartingTaming) {
        this.entityData.set(START_TAMING, isStartingTaming);
    }

    public boolean isStartingTaming() {
        return this.entityData.get(START_TAMING);
    }

    public void setGrabTimeout(int getGrabMaxTimeout) {
        this.entityData.set(GRAB_TIMEOUT, getGrabMaxTimeout);
    }

    public int getGrabTimeout() {
        return this.entityData.get(GRAB_TIMEOUT);
    }

    public void setGrabbing(boolean isGrabbing, LivingEntity entity) {
        // Apprivoisé : ne jamais saisir un allié de la tribu (joueur membre ou entité de la tribu).
        if (isGrabbing && this.isTameGrabAlly(entity)) return;
        this.entityData.set(IS_GRABBING, isGrabbing);
        this.setGrabbedTarget(entity);
    }

    public boolean isGrabbing() {
        return this.entityData.get(IS_GRABBING);
    }

    public float getRiderControlPitch() {
        return this.entityData.get(RIDER_CONTROL_PITCH);
    }

    public void setEntitiesKilledDuringTaming(int getEntitiesKilledDuringTaming) {
        this.entityData.set(ENTITIES_KILLED_DURING_TAMING, getEntitiesKilledDuringTaming);
    }

    public int getEntitiesKilledDuringTaming() {
        return this.entityData.get(ENTITIES_KILLED_DURING_TAMING);
    }

    public void setTamingTime(int getTamingTime) {
        this.entityData.set(TAMING_TIMER, getTamingTime);
    }

    public int getTamingTime() {
        return this.entityData.get(TAMING_TIMER);
    }

    public void setSacrificesUnity(float sacrificesUnity) {
        this.entityData.set(SACRIFICES_UNITY, sacrificesUnity);
    }

    public float getSacrificesUnity() {
        return this.entityData.get(SACRIFICES_UNITY);
    }

    public void setDeathRolling(boolean isDeathRolling) {
        this.entityData.set(IS_DEATH_ROLLING, isDeathRolling);
    }

    public boolean isDeathRolling() {
        return this.entityData.get(IS_DEATH_ROLLING);
    }

    /**
     * Vraie des DEUX côtés : l'identifiant de la proie et le drapeau de prise sont tous deux
     * synchronisés. La version d'avant renvoyait toujours {@code false} sur le client, si bien
     * que le modèle jouait la course au lieu de la posture de maintien.
     */
    public boolean hasGrabSomething() {
        return this.isGrabbing() && this.getGrabbedTarget() != null;
    }

    public LivingEntity getGrabbedTarget() {
        int id = this.entityData.get(GRABBED_TARGET_ID);
        if (id == -1) return null;
        Entity entity = this.level().getEntity(id);
        return entity instanceof LivingEntity ? (LivingEntity) entity : null;
    }

    public void setGrabbedTarget(LivingEntity target) {
        this.entityData.set(GRABBED_TARGET_ID, target == null ? -1 : target.getId());
    }

    public void setChargingMouthTimer(float chargingMouthTimer) {
        this.entityData.set(CHARGING_MOUTH_TIMER, chargingMouthTimer);
    }

    public float getChargingMouthTimer() {
        return this.entityData.get(CHARGING_MOUTH_TIMER);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("foodGiven", this.foodGiven);
        tag.putInt("foodWanted", this.foodWanted);

        tag.putInt("getEntitiesKilledDuringTaming", this.getEntitiesKilledDuringTaming());
        tag.putInt("getTamingTime", this.getTamingTime());
        tag.putBoolean("isStartingTaming", this.isStartingTaming());
        tag.putFloat("getSacrificesUnity", this.getSacrificesUnity());

        tag.putInt("ultimateKillCount", getUltimateKillCount());
        tag.putInt("grabCooldown", this.grabCooldown);
        tag.putInt("deathRollCooldown", this.deathRollCooldown);
        crocodileTaming.addAdditionalSaveData(tag);
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");

        this.entityData.set(ENTITIES_KILLED_DURING_TAMING, tag.getInt("getEntitiesKilledDuringTaming"));
        this.entityData.set(TAMING_TIMER, tag.getInt("getTamingTime"));
        this.entityData.set(START_TAMING, tag.getBoolean("isStartingTaming"));
        this.entityData.set(SACRIFICES_UNITY, tag.getFloat("getSacrificesUnity"));

        this.entityData.set(ULTIMATE_KILL_COUNT, tag.getInt("ultimateKillCount"));
        this.grabCooldown = tag.getInt("grabCooldown");
        this.deathRollCooldown = tag.getInt("deathRollCooldown");

        // Une prise ne survit pas à une sauvegarde : la victime n'est pas rechargée avec son
        // ravisseur, et un identifiant d'entité repris tel quel désignerait n'importe quoi.
        this.entityData.set(IS_GRABBING, false);
        this.entityData.set(GRABBED_TARGET_ID, -1);
        this.entityData.set(IS_DEATH_ROLLING, false);
        this.entityData.set(DEATH_ROLLING_PROGRESS, 0);
        this.grabHoldTimer = 0;
        this.isPrimalDiveGrab = false;

        crocodileTaming.readAdditionalSaveData(tag);
        if (this.getSkinIndex() != 0) {
            this.nbtRestoring = true;
            this.changeSkin(this.getSkinIndex(), false);
            this.nbtRestoring = false;
        }
    }

    @Override
    public int getGrabMaxTimeout() {
        return 600;
    }

    // ==================================================
    //           MOUTH SLAM (attaque chargée RMB)
    // ==================================================

    public void startMouthSlamCharge() {
        // Attaque terrestre : dans l'eau ou proie en gueule, il n'y a rien à claquer.
        if (this.isInWater() || this.isGrabbing()) return;
        setPlayerMouthCharging(true);
        setChargingMouthTimer(0);
    }

    public void cancelMouthSlamCharge() {
        setPlayerMouthCharging(false);
        setChargingMouthTimer(0);
    }

    // ==================================================
    //           PRIMAL DIVE (ultime)
    // ==================================================

    public void activatePrimalDive() {
        if (getUltimateKillCount() < OWAttacksConstants.Crocodile.PRIMAL_DIVE_KILLS_REQUIRED) return;
        if (!this.isInWater()) return;
        if (this.isGrabbing() || primalDivePhase != 0) return;
        float cost = OWAttacksConstants.Crocodile.PRIMAL_DIVE_ENERGY;
        if (getVitalEnergy() > getMaxVitalEnergy() - cost) {
            canShowVitalEnergyLack = true;
            return;
        }
        setVitalEnergy(getVitalEnergy() + cost);
        this.isChargingAttack = true;
        primalDivePhase = 1;
        primalDiveTimer = 200;

        OWUtils.spawnServerParticles(this, ParticleTypes.BUBBLE, 1.0, 0.5, 1.0, 30, 0.3);
        this.level().playSound(null, getX(), getY(), getZ(),
                OWSounds.CROCODILE_IDLE_1.get(), SoundSource.AMBIENT, 1.5f,
                (float) OWUtils.generateRandomInterval(0.7, 0.9));
    }

    public void executePrimalDive(int targetEntityId) {
        if (primalDivePhase != 1) return;
        Entity raw = this.level().getEntity(targetEntityId);
        // Validation serveur : l'identifiant vient du client, il ne fait donc pas foi. On repasse
        // par le MÊME filtre que la désignation et le réticule, plus une borne de distance
        // (anti-ciblage arbitraire).
        if (!(raw instanceof LivingEntity target) || !canPrimalDiveTarget(target)
                || this.distanceToSqr(target) > 32.0 * 32.0) {
            cancelPrimalDive();
            return;
        }

        this.entityData.set(LUNGE_TARGET_ID, targetEntityId);
        this.entityData.set(IS_LUNGING, true);
        // Allongé depuis que l'approche freine près de la cible : à 40 ticks, un bond parti du
        // bout de la portée (32 blocs) expirait avant d'avoir touché.
        primalDiveLungeTimer = 60;
        primalDivePhase = 2;
        primalDiveTimer = 0;

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, getX(), getY() + 0.5, getZ(), 60, 1.2, 0.4, 1.2, 0.4);
            sl.sendParticles(ParticleTypes.UNDERWATER, getX(), getY() + 0.5, getZ(), 40, 1.0, 0.5, 1.0, 0.05);
        }
        this.level().playSound(null, getX(), getY(), getZ(),
                OWSounds.CROCODILE_IDLE_1.get(), SoundSource.AMBIENT, 2.0f,
                (float) OWUtils.generateRandomInterval(0.7, 0.9));
    }

    /**
     * Trajectoire du bond de l'ultime, rejouée à l'identique sur le serveur et sur le client qui
     * pilote — c'est ce dernier qui fait autorité sur la position d'une monture.
     *
     * <p>Trois défauts se cumulaient dans la version d'un seul appel à {@code setDeltaMovement} :
     * la vitesse était <b>plaquée</b> d'un coup à sa valeur maximale, ce qui donnait un départ et
     * des changements de cap en escalier ; l'axe vertical était ignoré, donc le crocodile n'allait
     * jamais chercher une proie plus haute ou plus basse que lui et tournait autour ; et le cap de
     * la bête n'était jamais corrigé, si bien qu'elle fonçait <b>de travers</b>, museau pointé
     * ailleurs. On vise désormais le centre de la cible, la vitesse est amenée progressivement,
     * freinée à l'approche, et le corps s'aligne sur la trajectoire.</p>
     */
    private void tickPrimalDiveLunge() {
        Entity raw = this.level().getEntity(this.entityData.get(LUNGE_TARGET_ID));
        if (raw == null) return;

        Vec3 aim = raw.getBoundingBox().getCenter().subtract(this.getEyePosition());
        double distance = aim.length();
        if (distance < 1.0E-4) return;

        Vec3 direction = aim.scale(1.0 / distance);

        // Freinage à l'approche : sans lui, le crocodile arrivait à pleine vitesse et traversait
        // sa proie avant que la portée de prise ne soit testée.
        double brake = Mth.clamp(distance / 5.0, 0.30, 1.0);
        Vec3 desired = direction.scale(PRIMAL_DIVE_LUNGE_SPEED * brake);

        // Montée en vitesse lissée plutôt que plaquée : même courbe des deux côtés du réseau,
        // sans dépendre d'un compteur que seul le serveur tient.
        Vec3 current = this.getDeltaMovement();
        this.setDeltaMovement(current.add(desired.subtract(current).scale(0.30)));

        float targetYaw = (float) (Mth.atan2(direction.z, direction.x) * (180.0 / Math.PI)) - 90.0f;
        float smoothedYaw = this.getYRot() + Mth.wrapDegrees(targetYaw - this.getYRot()) * 0.30f;
        this.setYRot(smoothedYaw);
        this.yBodyRot = smoothedYaw;
        this.yHeadRot = smoothedYaw;

        // Assiette visuelle : le corps pique vers la proie au lieu de rester à plat en montant.
        if (!this.level().isClientSide()) {
            float targetPitch = (float) (-Math.toDegrees(Math.asin(Mth.clamp(direction.y, -1.0, 1.0))));
            this.setTargetPitch(Mth.lerp(0.25f, this.getTargetPitch(), Mth.clamp(targetPitch, -45f, 45f)));
        }
    }

    /**
     * Referme la gueule au bout du bond de l'ultime.
     *
     * <p>{@code setGrabbing} peut refuser la prise (allié de tribu, par exemple) : on vérifie donc
     * qu'elle a bien pris avant d'endormir l'IA de la cible et d'armer les minuteries. Sans cette
     * vérification, une cible refusée se retrouvait figée par {@code setNoAi(true)} <b>à vie</b>,
     * sans être tenue par quoi que ce soit.</p>
     */
    private void closePrimalDiveGrab(LivingEntity target) {
        setGrabbing(true, target);

        if (!this.isGrabbing() || this.getGrabbedTarget() != target) {
            cancelPrimalDive();
            return;
        }

        isPrimalDiveGrab = true;
        if (target instanceof Mob grabbedMob) grabbedMob.setNoAi(true);
        setGrabTimeout(GRAB_START_TIMEOUT);
        grabCooldown = MAX_GRAB_COOLDOWN;
        grabHoldTimer = PRIMAL_DIVE_GRAB_TICKS;
        deathRollCooldown = DEATH_ROLL_WINDUP_TICKS;
        setUltimateKillCount(0);
        primalDivePhase = 0;
        primalDiveLungeTimer = 0;
        this.entityData.set(IS_LUNGING, false);
        this.entityData.set(LUNGE_TARGET_ID, -1);
        this.isChargingAttack = false;

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, getX(), getY() + 0.5, getZ(), 70, 1.2, 0.4, 1.2, 0.25);
            sl.sendParticles(ParticleTypes.SPLASH, getX(), getY() + 0.5, getZ(), 50, 0.8, 0.3, 0.8, 0.15);
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, getX(), getY() + 0.5, getZ(), 20, 0.9, 0.3, 0.9, 0.1);
            sl.sendParticles(ParticleTypes.UNDERWATER, getX(), getY() + 0.5, getZ(), 40, 1.0, 0.5, 1.0, 0.05);
        }
        this.level().playSound(null, getX(), getY(), getZ(), OWSounds.CROCODILE_MOUTH_CRUSH.get(), SoundSource.AMBIENT, 2.5f, 0.60f);
        this.level().playSound(null, getX(), getY(), getZ(), OWSounds.CROCODILE_HURT.get(), SoundSource.AMBIENT, 2.0f, 0.70f);
        this.level().playSound(null, getX(), getY(), getZ(), OWSounds.CROCODILE_IDLE_4.get(), SoundSource.AMBIENT, 1.5f, 0.75f);
    }

    public void cancelPrimalDive() {
        primalDivePhase = 0;
        primalDiveTimer = 0;
        primalDiveLungeTimer = 0;
        this.entityData.set(IS_LUNGING, false);
        this.entityData.set(LUNGE_TARGET_ID, -1);
        this.isChargingAttack = false;
    }

    public boolean isInPrimalDivePhase() {
        return primalDivePhase == 1;
    }

    @Override
    protected boolean isLeapingVehicle() {
        return this.entityData.get(IS_LUNGING);
    }

    /**
     * Pendant le bond de l'ultime, le cap appartient à l'attaque, pas au cavalier.
     *
     * <p>{@code smoothRotation} tirait le museau vers le regard du pilote à chaque tick, juste
     * après que {@code tickPrimalDiveLunge} l'ait aligné sur la proie : les deux se disputaient
     * l'orientation et la bête partait en zigzag, de travers, sans jamais viser franchement. Le
     * cavalier reprend la main dès que la gueule s'est refermée.</p>
     */
    @Override
    public void tickRidden(Player player, Vec3 vec3) {
        if (this.isLeapingVehicle()) {
            player.resetFallDistance();
            return;
        }
        super.tickRidden(player, vec3);
    }

    @Override
    public boolean isPlayerControlledDeathRoll() {
        return this.isTame() && this.isGrabbing() && this.getGrabbedTarget() != null;
    }

    /** Vrai tant que la roulade est encore en délai de récupération (affichage et anti-spam client). */
    public boolean isDeathRollOnCooldown() {
        return deathRollCooldown > 0;
    }

    public int getUltimateKillCount() {
        return this.entityData.get(ULTIMATE_KILL_COUNT);
    }

    public void setUltimateKillCount(int count) {
        this.entityData.set(ULTIMATE_KILL_COUNT, count);
    }

    /**
     * Exécute le Mouth Slam après une charge valide (≥ 1 s).
     *
     * @param factor 0.0 = 1 s de charge, 1.0 = 3 s de charge
     */
    public void performMouthSlam(float factor) {
        boolean wasCharging = isPlayerMouthCharging();
        setPlayerMouthCharging(false);
        setChargingMouthTimer(0);
        // Charge interrompue par une entrée dans l'eau ou par une prise : le relâchement du clic
        // arrivait quand même et déclenchait un claquement fantôme, énergie comprise.
        if (!wasCharging || this.isInWater() || this.isGrabbing()) return;
        float energyRequired = OWAttacksConstants.Crocodile.MOUTH_SLAM_ENERGY;
        if (getVitalEnergy() > getMaxVitalEnergy() - energyRequired) {
            canShowVitalEnergyLack = true;
            return;
        }
        setVitalEnergy(getVitalEnergy() + energyRequired);

        float baseDamage = Mth.lerp(factor, 5.0f, this.getDamage());
        mouthSlamPendingDamage = factor >= 1.0f ? baseDamage * 2f : baseDamage;
        mouthSlamPendingKnockback = 1.5f + factor * 2.0f;
        mouthSlamPendingBleed = factor >= 1.0f;
        mouthSlamHitTimer = 8;

        setMouthSlamming(true);
        this.mouthSlamServerTimer = 50;
    }

    public void performWildMouthSlam(float chargeRatio) {
        mouthSlamPendingDamage = this.getDamage() * (0.5f + chargeRatio * 0.5f);
        mouthSlamPendingKnockback = 2.0f + chargeRatio * 1.5f;
        mouthSlamPendingBleed = chargeRatio >= 1.0f;
        mouthSlamHitTimer = 8;

        setMouthSlamming(true);
        this.mouthSlamServerTimer = 50;
    }

    public static class CrocodileNearestAttackableTargetGoal extends NearestAttackableTargetGoal {

        private final CrocodileEntity crocodile;
        private final float attacksMultiplier;

        public CrocodileNearestAttackableTargetGoal(Mob mob, Class targetType, boolean mustSee, float attacksMultiplier) {
            this(mob, targetType, mustSee, attacksMultiplier, null);
        }

        public CrocodileNearestAttackableTargetGoal(Mob mob, Class targetType, boolean mustSee, float attacksMultiplier, java.util.function.Predicate<LivingEntity> selector) {
            super(mob, targetType, 10, mustSee, false, selector);
            this.crocodile = (CrocodileEntity) mob;
            this.attacksMultiplier = attacksMultiplier;
        }

        @Override
        public boolean canUse() {
            if (!super.canUse()) {
                return false;
            }

            if (this.target == null || this.crocodile.isNapping()) return false;

            if (this.crocodile.distanceTo(this.target) <= 6) {
                return true;
            }

            if (this.crocodile.level().isDay() && !this.target.isInWater()) {
                return this.crocodile.getRandom().nextInt((int) (50 / this.attacksMultiplier)) == 0;
            }

            return true;
        }
    }

    class CrocodileMeleeAttackGoal extends MeleeAttackGoal {

        public CrocodileMeleeAttackGoal() {
            super(CrocodileEntity.this, 3.0, true);
        }

        private boolean isCrocodileBlocked() {
            return CrocodileEntity.this.crocodileBehaviorHandler.isReadyForTaming()
                    || CrocodileEntity.this.isBaby()
                    || CrocodileEntity.this.hasGrabSomething()
                    || CrocodileEntity.this.isChargingAttack;
        }

        @Override
        public boolean canContinueToUse() {
            if (isCrocodileBlocked() || CrocodileEntity.this.crocodileBehaviorHandler.isReadyForTaming()) return false;
            return super.canContinueToUse();
        }

        @Override
        public void start() {
            if (isCrocodileBlocked() || CrocodileEntity.this.crocodileBehaviorHandler.isReadyForTaming()) return;
            super.start();
            CrocodileEntity.this.setRunning(true);
        }

        @Override
        public void stop() {
            super.stop();
            CrocodileEntity.this.setRunning(false);
        }

        @Override
        protected boolean canPerformAttack(LivingEntity entity) {
            if (CrocodileEntity.this.isChargingMouth() || CrocodileEntity.this.crocodileBehaviorHandler.isReadyForTaming()) return false;
            double reach = 4;
            return this.isTimeToAttack()
                    && this.mob.distanceToSqr(entity) <= reach * reach
                    && this.mob.getSensing().hasLineOfSight(entity);
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target) {
            if (this.mob.hasEffect(OWEffects.FRACTURE.getDelegate())) return;
            if (!this.canPerformAttack(target)) return;
            if (CrocodileEntity.this.isChargingMouth() || CrocodileEntity.this.crocodileBehaviorHandler.isReadyForTaming()) return;

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