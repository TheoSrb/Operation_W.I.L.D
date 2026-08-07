package net.tiew.operationWild.entity.animals.terrestrial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.core.OWPlacedBlocks;
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

import java.util.HashSet;
import java.util.Set;

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

    /**
     * Teintes des deux bandes de laine de la selle quand la bête n'appartient à aucune tribu :
     * blanc et gris clair, aux valeurs de {@code DyeColor.WHITE} et {@code DyeColor.LIGHT_GRAY}.
     */
    private static final int NO_TRIBE_WOOL_0 = 0xF9FFFE;
    private static final int NO_TRIBE_WOOL_1 = 0x9D9D97;

    // ── Martèlement du sol ──────────────────────────────────────────────────────

    /**
     * Longueur du cycle {@code move.walk} et instants où les pattes touchent le sol, en
     * millisecondes d'animation. Relevés sur les canaux de position des membres dans
     * {@code ElephantAnimations.MOVE_WALK} (3,4115 s) : avant-droite et arrière-gauche à 1,2 s,
     * les deux autres à 2,9 s.
     *
     * <p>Ils vivent ici, et non dans le modèle, parce que les deux côtés du réseau en ont besoin :
     * le client pour caler le bruit de pas sur l'image de contact, le serveur pour secouer le sol
     * au même instant. Une entité commune ne peut pas lire {@code ElephantAnimations}, qui est
     * client seul — d'où la longueur recopiée plutôt que relue.</p>
     */
    private static final long WALK_CYCLE_MS = 3411L;
    public static final long RIGHT_FOOT_CONTACT_MS = 1200L;
    public static final long LEFT_FOOT_CONTACT_MS = 2900L;

    /** Cycles d'animation par unité de {@code walkAnimation}, au pas puis à la course. */
    public static final float WALK_ANIM_SPEED = 6.0f;
    public static final float RUN_ANIM_SPEED = 9.0f;

    /**
     * Position des quatre pattes dans le repère local de la bête, en blocs à l'échelle 1 :
     * {@code x} vers sa gauche, {@code z} vers l'avant.
     *
     * <p>Relevés sur les pivots de {@code ElephantModel.createBodyLayer()} — {@code left_arm} en
     * (+6, −14), {@code right_arm} en (−6, −14), {@code left_leg} en (+8, +14), {@code right_leg}
     * en (−8, +14) —, décalés du {@code z} de repos de {@code ALL2} (+2) et divisés par les seize
     * unités du bloc. Le modèle regarde vers son −Z et son +X tombe à gauche après le retournement
     * du rendu : d'où le {@code z} inversé et le {@code x} conservé.</p>
     */
    private static final Vec3 FRONT_LEFT_FOOT  = new Vec3( 6 / 16.0, 0, 12 / 16.0);
    private static final Vec3 FRONT_RIGHT_FOOT = new Vec3(-6 / 16.0, 0, 12 / 16.0);
    private static final Vec3 BACK_LEFT_FOOT   = new Vec3( 8 / 16.0, 0, -16 / 16.0);
    private static final Vec3 BACK_RIGHT_FOOT  = new Vec3(-8 / 16.0, 0, -16 / 16.0);

    /**
     * Portée du martèlement, et hauteur du sursaut imprimé au centre. Quatre tonnes qui retombent
     * ne projettent personne — elles font perdre l'équilibre. Le sursaut décroît jusqu'à zéro au
     * bord du rayon, et le cavalier en est exempt.
     */
    private static final double FOOTFALL_RADIUS = 4.5;
    private static final double FOOTFALL_HOP = 0.22;

    /**
     * Gerbe d'un seul pied. Serrée à un quart de bloc : elle partait du centre du corps avec un
     * étalement d'un bloc et demi, ce qui donnait un nuage sous le ventre et non des pas.
     */
    private static final int FOOTFALL_PARTICLES = 30;
    private static final double FOOTFALL_SPREAD = 0.22;

    /**
     * Intervalle minimal entre deux martèlements, en ticks. À pleine course l'éléphant pose près de
     * cinq pieds par seconde — la cadence est juste pour l'œil, mais chaque appui sème une gerbe et
     * pousse les voisins. Le même plancher de 250 ms borne déjà le bruit de pas : les deux restent
     * ainsi sur le même appui.
     */
    private static final int FOOTFALL_MIN_INTERVAL = 5;


    /** Au-delà de cette vitesse au sol, en blocs par tick, la bête est considérée lancée. */
    private static final double CHARGE_SPEED = 0.32;

    /**
     * La charge : ce que coûte de se trouver devant quatre tonnes lancées. Le délai borne les coups
     * à un par seconde et par victime — sans lui, une entité coincée devant la trompe encaisserait
     * vingt fois par seconde.
     */
    private static final float CHARGE_DAMAGE = 3.5f;
    private static final float CHARGE_KNOCKBACK = 1.4f;
    private static final int CHARGE_HIT_INTERVAL = 20;

    /**
     * Propagation de la fissure du séisme : chances qu'un bloc voisin cède à son tour, et nombre
     * maximal de voisins entraînés par bloc effondré.
     */
    private static final float EARTHQUAKE_SPREAD_CHANCE = 0.45f;
    private static final int EARTHQUAKE_MAX_SPREAD = 2;

    private static final Direction[] HORIZONTAL_SPREAD = {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };


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

    /** Verrou à un coup : l'impact au sol ne doit partir qu'une fois par séisme. */
    private boolean earthquakeImpactDone = false;

    /**
     * Point d'impact, figé au contact. Les pulsations courent bien plus longtemps que le geste :
     * passé les 4,16 s l'éléphant a repris la main et peut s'éloigner, et un épicentre lu sur sa
     * position courante le suivrait — le sol s'effondrerait sous ses pas au lieu de rester là où il
     * a frappé.
     */
    private Vec3 earthquakeEpicentre = Vec3.ZERO;

    /** Onde de Choc : numéro du tick en cours, origine et cap figés au départ. */
    private int shockwaveTick = 0;
    private Vec3 shockwaveOrigin = Vec3.ZERO;
    private Vec3 shockwaveDirection = Vec3.ZERO;
    /** Altitude courante de l'onde : elle seule bouge en chemin, pour épouser le relief. */
    private double shockwaveY = 0;
    private final Set<Integer> shockwaveStruck = new HashSet<>();

    private long lastStepSoundMs = 0L;

    /** Phase de marche au tick serveur précédent ; {@code -1} tant qu'aucune n'a été relevée. */
    private float previousWalkPhase = -1f;

    private int groundStrikeCooldown = 0;

    /** Victimes déjà bousculées par la charge, vidé une fois par seconde. */
    private final Set<Integer> ploughStruck = new HashSet<>();

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
                        && !elephant.isEarthquakeGesture()) {
                    super.tick();
                }
            }
        };
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(IS_MAD, false);
        builder.define(IS_CALLING, false);
        builder.define(SHOULDER_BASH_TIMER, 0);
        builder.define(SHOULDER_BASH_SIDE, 1);
        builder.define(EARTHQUAKE_TICK, 0);
        builder.define(ULTIMATE_KILL_COUNT, 0);
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
        return 4f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 2f;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return 2f;
    }

    @Override
    public float vehicleWaterSpeedDivider() {
        return 4f;
    }

    @Override
    public boolean canIncreasesSpeedDuringSprint() {
        return true;
    }

    @Override
    public boolean isChangeSpeedDuringCombo() {
        return false;
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
        return 280 * (1 + ((float) this.getLevel() / 50));
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

    /** Volontairement vide : les pas partent des rappels d'animation ci-dessous, pas du socle vanilla. */
    @Override
    public void playStepSound(BlockPos blockPos, BlockState blockState) {
    }

    /**
     * Un pas d'éléphant, c'est deux sons superposés : le bruit du bloc foulé, joué grave et fort, et
     * le coup sourd de la patte elle-même. Le tigre empile sept répétitions à faible volume pour
     * épaissir un pas léger ; ici l'inverse — un seul appel, mais lourd.
     */
    private void playStepSoundFromAnimation(float pitchMod) {
        if (!this.level().isClientSide()) return;
        if (!isFooted()) return;
        if (this.isInWater()) return;

        // Même raison que dans tickGroundStrikes : sur le client d'un spectateur, la monture n'est
        // pas pilotée localement et sa vélocité est remise à zéro par travelRidden. Le pas devenait
        // muet pour tout le monde sauf le cavalier.
        if (this.walkAnimation.speed() < 0.05f) return;

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

    /**
     * Appelé par {@code ElephantModel} (thread de rendu) quand une paire de pattes touche le sol.
     *
     * <p>Bruit <b>et</b> gerbe de terre partent d'ici, côté client. Les particules ont d'abord été
     * semées par le serveur, comme le sursaut des voisins qui, lui, y reste : c'était le bon endroit
     * pour la physique et le mauvais pour le décor. La détection d'appui du serveur, calquée sur
     * celle du modèle, ne se déclenchait pas dans les mêmes conditions — les pas s'entendaient sans
     * qu'aucune poussière ne sorte. Un effet purement visuel n'a rien à faire là-bas : chaque client
     * anime déjà chaque éléphant qu'il voit, donc chacun peut semer sa propre poussière, comme le
     * fait le bond du tigre.</p>
     */
    public void onLeftFootDown() {
        playStepSoundFromAnimation(0.55f);
        spawnFootDust(FRONT_LEFT_FOOT);
        spawnFootDust(BACK_RIGHT_FOOT);
    }

    /** Voir {@link #onLeftFootDown()} : l'autre diagonale du pas. */
    public void onRightFootDown() {
        playStepSoundFromAnimation(0.62f);
        spawnFootDust(FRONT_RIGHT_FOOT);
        spawnFootDust(BACK_LEFT_FOOT);
    }

    /**
     * La gerbe d'un pied, serrée sous la patte.
     *
     * <p>L'offset arrive dans le repère local de la bête — {@code x} vers sa gauche, {@code z} vers
     * l'avant, en blocs à l'échelle 1 — et {@code yRot(-yBodyRot)} le porte dans le monde, comme le
     * fait déjà {@code positionRider} pour l'assise du cavalier. Il suit l'échelle de l'individu,
     * qui est celle qu'applique le renderer.</p>
     */
    private void spawnFootDust(Vec3 localOffset) {
        if (!this.level().isClientSide()) return;

        Vec3 foot = this.position().add(
                localOffset.scale(this.getScale()).yRot((float) -Math.toRadians(this.yBodyRot)));

        BlockState ground = this.level().getBlockState(
                BlockPos.containing(foot.x, foot.y - 0.2, foot.z));
        if (ground.isAir()) return;

        // Une bête lancée laboure : elle soulève plus, et plus haut.
        boolean charging = isChargingForward();
        int count = charging ? FOOTFALL_PARTICLES * 2 : FOOTFALL_PARTICLES;
        double lift = charging ? 0.16 : 0.08;

        BlockParticleOption dirt = new BlockParticleOption(ParticleTypes.BLOCK, ground);

        for (int i = 0; i < count; i++) {
            this.level().addParticle(dirt,
                    foot.x + (this.random.nextDouble() - 0.5) * FOOTFALL_SPREAD * 2,
                    foot.y + 0.05,
                    foot.z + (this.random.nextDouble() - 0.5) * FOOTFALL_SPREAD * 2,
                    (this.random.nextDouble() - 0.5) * 0.08,
                    lift * (0.5 + this.random.nextDouble()),
                    (this.random.nextDouble() - 0.5) * 0.08);
        }

        if (!charging) return;

        for (int i = 0; i < count / 2; i++) {
            this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    foot.x + (this.random.nextDouble() - 0.5) * FOOTFALL_SPREAD,
                    foot.y + 0.1,
                    foot.z + (this.random.nextDouble() - 0.5) * FOOTFALL_SPREAD,
                    (this.random.nextDouble() - 0.5) * 0.03,
                    0.04,
                    (this.random.nextDouble() - 0.5) * 0.03);
        }
    }

    /**
     * Cadence du cycle de marche, en cycles d'animation par unité de {@code walkAnimation}.
     *
     * <p>Un seul point de décision, lu par les deux côtés : le modèle en tire la vitesse de lecture
     * de l'animation, le serveur l'instant du martèlement. Les faire diverger ferait tomber le bruit
     * de pas et la gerbe de terre sur deux appuis différents.</p>
     */
    public float walkAnimationSpeed() {
        return (this.isRunning() || this.getState() == 2) ? RUN_ANIM_SPEED : WALK_ANIM_SPEED;
    }

    /**
     * Vrai sur l'exacte transition où le cycle de marche franchit l'instant demandé.
     *
     * <p>Partagé par le modèle — qui s'en sert sur le fil de rendu pour les bruits de pas — et par
     * {@link #tickGroundStrikes}, côté serveur, pour le martèlement. Les deux lisent la même
     * grandeur ({@code walkAnimation.position()}, tenue à jour des deux côtés), donc ils tombent
     * sur la même image.</p>
     */
    public static boolean walkCycleCrossed(float previousPhase, float phase, float speedScale, long triggerTimeMs) {
        long cur = ((long) (phase * 50f * speedScale)) % WALK_CYCLE_MS;
        long prev = ((long) (previousPhase * 50f * speedScale)) % WALK_CYCLE_MS;

        if (prev <= cur) return prev < triggerTimeMs && cur >= triggerTimeMs;
        return triggerTimeMs <= cur || triggerTimeMs > prev;
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

        // 27 ticks par frappe, dégâts au 17e : calé sur la durée des animations, sinon la machine
        // d'état enchaîne la frappe suivante pendant que la précédente s'achève encore.
        // Toute retouche ici se reporte sur registerComboMaxTimer(ElephantEntity.class, …).
        createCombo(27, 20, OWSounds.ELEPHANT_HURTING.get(), 4.0, 4.0, 2.5, actualAttackNumber == 2, actualAttackNumber == 2 ? 3 : 1);
        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        if (this.isVehicle() && this.isTame() && !this.isSitting()) {
            setMadByRider(this.isCombo() || this.isShoulderBashing() || this.isEarthquakeGesture());
        }

        // L'amortissement du séisme s'applique des DEUX côtés : la position d'une monture appartient
        // au client de son cavalier, et ne freiner que sur le serveur ferait vibrer la bête pendant
        // les quatre secondes du geste.
        if (this.isEarthquakeGesture()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.2, 1.0, 0.2));
        }

        // ------------ FONCTIONNEMENT PROPRE ------------

        if (!this.level().isClientSide()) {
            tickShoulderBash();
            tickEarthquake();
            tickShockwave();
            tickGroundStrikes();
            handleCall();
        }
    }

    // ------------ MARTÈLEMENT DU SOL ------------

    /**
     * Repère les contacts au sol du cycle de marche et les fait payer aux alentours.
     *
     * <p>Le calcul double celui du modèle, et c'est voulu : le modèle tourne sur le fil de rendu du
     * client, où l'on n'a le droit ni de pousser une entité ni de semer des particules pour les
     * autres joueurs. Les deux lisent {@code walkAnimation.position()}, tenue à jour des deux côtés,
     * et {@link #walkCycleCrossed} leur est commun — le son du client et la secousse du serveur
     * tombent donc sur le même pas.</p>
     */
    private void tickGroundStrikes() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        if (groundStrikeCooldown > 0) groundStrikeCooldown--;

        float phase = this.walkAnimation.position();
        float previous = this.previousWalkPhase;
        this.previousWalkPhase = phase;

        if (previous < 0f) return;
        if (this.isBaby()) return;
        if (!isFooted() || this.isInWater()) return;
        if (this.isSleeping() || this.isNapping() || this.isSitting() || this.isEarthquakeGesture()) return;

        // Test de mouvement pris sur walkAnimation et non sur getDeltaMovement : la physique d'une
        // monture pilotée appartient au client du cavalier, et la vélocité que voit le serveur y est
        // quasi nulle — le martèlement s'éteignait précisément quand on chevauchait.
        if (this.walkAnimation.speed() < 0.05f) return;

        if (isChargingForward()) plough(serverLevel);

        if (groundStrikeCooldown > 0) return;

        float speed = walkAnimationSpeed();

        if (walkCycleCrossed(previous, phase, speed, RIGHT_FOOT_CONTACT_MS)
                || walkCycleCrossed(previous, phase, speed, LEFT_FOOT_CONTACT_MS)) {
            groundStrikeCooldown = FOOTFALL_MIN_INTERVAL;
            strikeGround(serverLevel);
        }
    }

    /**
     * Traînée de poussière de la course, semée derrière les pattes arrière plutôt que sous le
     * ventre : c'est le sillage qu'on doit voir depuis la selle, pas un nuage qui suit la bête.
     *
     * <p>Même fumée que le bond du tigre, mais émise en continu et non par bouffée — un éléphant
     * lancé ne décolle pas, il laboure.</p>
     */
    /**
     * L'éléphant est-il lancé à pleine allure ?
     *
     * <p>Cet état commande à la fois la traînée, le labour et l'inclinaison de la tête. Il exige
     * qu'il soit <b>monté</b> : un éléphant sauvage qui fuit ou charge un prédateur ne doit pas
     * raser la forêt sur son passage. C'est le cavalier qui décide de lancer quatre tonnes dans un
     * bosquet, et qui en récolte le bois.</p>
     */
    /**
     * L'éléphant a-t-il les pattes sur quelque chose de dur ?
     *
     * <p>On sonde le bloc au lieu de croire {@code onGround()}, qui <b>ment sur une monture
     * pilotée</b> : le serveur reçoit sa position par {@code ServerboundMoveVehiclePacket}, lequel
     * ne transporte pas l'appui au sol, et {@code handleMoveVehicle} se contente d'un
     * {@code absMoveTo}. Le drapeau reste donc figé sur sa dernière valeur calculée — et tout ce qui
     * en dépendait (traînée, martèlement, sursaut des voisins) s'éteignait dès qu'un joueur montait
     * en selle, c'est-à-dire exactement quand on voulait le voir.</p>
     */
    private boolean isFooted() {
        if (this.onGround()) return true;

        BlockPos below = BlockPos.containing(this.getX(), this.getY() - 0.2, this.getZ());
        return !this.level().getBlockState(below).getCollisionShape(this.level(), below).isEmpty();
    }

    public boolean isChargingForward() {
        if (!this.isVehicle() || !this.isTame() || this.isBaby()) return false;
        return this.isRunning() || measuredSpeed() >= CHARGE_SPEED;
    }

    /**
     * Vitesse horizontale réellement parcourue au dernier tick, en blocs.
     *
     * <p>Mesurée sur les positions plutôt que lue sur {@code isRunning()} : ce drapeau dépend du
     * paquet de sprint, qui exige selle, énergie et poussée avant, et se remet à zéro au moindre
     * relâchement. La traînée doit suivre ce que la bête <b>fait</b>, pas ce que le cavalier a
     * déclaré. C'est la grandeur qu'emploie déjà {@code calculateEntityAnimation}, donc elle vaut
     * des deux côtés du réseau, monture pilotée comprise.</p>
     */
    public double measuredSpeed() {
        double dx = this.getX() - this.xo;
        double dz = this.getZ() - this.zo;
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Le labour : à pleine course, la bête ouvre son chemin dans la végétation et bouscule tout ce
     * qu'elle percute.
     *
     * <p>Seuls le feuillage et le bois cèdent — c'est une charge à travers un bosquet, pas un
     * tunnelier. Les blocs sont <b>récoltés</b> et non pulvérisés : bûches et pousses tombent au sol
     * comme sous la hache, ce qui fait de la charge une façon d'abattre une forêt. L'ouvrage de la
     * tribu tient bon, ici comme sous le séisme.</p>
     */
    private void plough(ServerLevel serverLevel) {
        if (this.tickCount % CHARGE_HIT_INTERVAL == 0) ploughStruck.clear();

        Vec3 ahead = this.position().add(
                Vec3.directionFromRotation(0f, this.yBodyRot).scale(this.getBbWidth() * 0.7));
        AABB front = new AABB(ahead, ahead).inflate(this.getBbWidth() * 0.6, this.getBbHeight() * 0.5, this.getBbWidth() * 0.6)
                .move(0, this.getBbHeight() * 0.5, 0);

        if (serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            BlockPos.betweenClosedStream(front).forEach(pos -> {
                BlockState state = serverLevel.getBlockState(pos);
                if (!state.is(BlockTags.LEAVES) && !state.is(BlockTags.LOGS) && !state.is(BlockTags.SAPLINGS)) return;
                if (OWPlacedBlocks.isProtectedFrom(serverLevel, pos, this.getOwnerUUID())) return;

                serverLevel.destroyBlock(pos.immutable(), true, this);
                OWPlacedBlocks.get(serverLevel).forget(pos);
            });
        }

        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, front)) {
            if (target == this || target.getRootVehicle() == this) continue;
            if (this.isAlliedTo(target)) continue;
            if (!ploughStruck.add(target.getId())) continue;

            target.hurt(this.damageSources().mobAttack(this), CHARGE_DAMAGE);
            target.knockback(CHARGE_KNOCKBACK, this.getX() - target.getX(), this.getZ() - target.getZ());
            target.hurtMarked = true;
        }
    }


    /**
     * La patte qui retombe, côté physique : le sursaut de qui se tient trop près. Pas de dégâts —
     * l'éléphant n'écrase pas en marchant, il déséquilibre. La gerbe de terre, elle, est semée par
     * les clients (cf. {@link #onLeftFootDown()}).
     *
     * <p>Le sursaut se lit à travers la résistance au recul de la victime, et non à travers une
     * liste d'exceptions : un éléphant, qui la porte à 0,95, ne bronche pas sous le pas d'un autre.
     * Sans cela, deux bêtes côte à côte se seraient renvoyées en l'air indéfiniment.</p>
     */
    private void strikeGround(ServerLevel serverLevel) {
        AABB box = this.getBoundingBox().inflate(FOOTFALL_RADIUS, 1.0, FOOTFALL_RADIUS);

        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, box)) {
            if (target == this || target.getRootVehicle() == this) continue;
            if (!target.onGround()) continue;

            double distance = target.distanceTo(this);
            if (distance > FOOTFALL_RADIUS) continue;

            double footing = 1.0 - Mth.clamp(target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.0, 1.0);
            double hop = FOOTFALL_HOP * (1.0 - distance / FOOTFALL_RADIUS) * footing;
            if (hop < 0.01) continue;

            target.push(0, hop, 0);
            target.hurtMarked = true;
        }
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

    /**
     * Le compteur couvre le geste (4,16 s) <b>puis</b> les dix secondes de secousse. Le geste rend la
     * main au cavalier bien avant la fin : passé sa dernière image l'éléphant repart, pendant que le
     * sol qu'il a fracturé continue de s'effondrer derrière lui.
     */
    private void tickEarthquake() {
        int tick = this.entityData.get(EARTHQUAKE_TICK);
        if (tick <= 0) return;

        int windup = OWAttacksConstants.Elephant.EARTHQUAKE_WINDUP_TICKS;

        if (tick == windup && !earthquakeImpactDone) {
            earthquakeImpactDone = true;
            executeEarthquakeImpact();
        }

        int elapsed = tick - windup;

        if (elapsed > 0 && elapsed % OWAttacksConstants.Elephant.EARTHQUAKE_PULSE_INTERVAL == 0
                && this.level() instanceof ServerLevel serverLevel) {
            pulseEarthquake(serverLevel);
        }

        int total = Math.max(OWAttacksConstants.Elephant.EARTHQUAKE_TOTAL_TICKS,
                windup + OWAttacksConstants.Elephant.EARTHQUAKE_DURATION_TICKS);

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
    public boolean killedEntity(ServerLevel level, LivingEntity target) {
        int kills = getUltimateKillCount();
        if (kills < OWAttacksConstants.Elephant.EARTHQUAKE_KILLS_REQUIRED) {
            setUltimateKillCount(kills + 1);
        }
        return super.killedEntity(level, target);
    }

    @Override
    protected boolean isImmobile() {
        return this.isEarthquakeGesture() || isSlammingCombo();
    }

    /**
     * La troisième frappe, celle qui abat le sol et lance l'Onde de Choc. C'est la seule du combo
     * qui cloue la bête sur place : les deux premières sont des coups de trompe, on peut les donner
     * en marchant.
     */
    public boolean isSlammingCombo() {
        return this.isCombo() && this.getComboAttack() == 3;
    }

    /**
     * L'allure sous le combo.
     *
     * <p>Le socle avance tout seul pendant un combo, et ce n'est pas une intention : {@code idle} y
     * vaut {@code player.zza == 0 && !isCombo()}, donc il est faux dès qu'un combo tourne, même sans
     * la moindre entrée du cavalier. La chaîne d'allure retombe alors sur la vitesse de marche, et
     * comme {@code OWEntity} pilote la monture en posant directement {@code setDeltaMovement} le
     * long du regard — sans jamais relire {@code zza} —, la bête se met à dériver pendant les
     * quatre-vingts ticks du combo. Le boa et le kangourou y échappent par une garde nommée dans
     * {@code getRiddenSpeedVehicle} ; l'éléphant pose la sienne ici, sans toucher au socle.</p>
     */
    @Override
    public float getRiddenSpeedVehicle(Player player) {
        if (this.isImmobile()) return 0f;
        if (this.isCombo() && player.zza == 0) return 0f;
        return super.getRiddenSpeedVehicle(player);
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
        if (EARTHQUAKE_TICK.equals(accessor) && !isEarthquakeGesture() && this.level().isClientSide()) {
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
        if (isShoulderBashing() || isEarthquakeGesture()) return;
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

    // ------------ ONDE DE CHOC (TROISIÈME FRAPPE) ------------

    /**
     * La troisième frappe du combo ne s'arrête plus à la portée des défenses : la trompe abat le sol
     * et la fracture part droit devant.
     *
     * <p>Le cap est figé au départ et ne suit plus la bête : une onde qui se recourbe pendant sa
     * course ne se lit pas comme une onde. Les deux frappes précédentes restent du corps à corps
     * pur — c'est cette dissymétrie qui donne un sens à finir le combo.</p>
     */
    @Override
    public void applyComboModification(int timeToHit) {
        super.applyComboModification(timeToHit);

        if (this.level().isClientSide()) return;
        if (this.attackTimer != timeToHit) return;
        if (this.getComboAttack() != 3) return;

        startShockwave();
    }

    private void startShockwave() {
        shockwaveTick = 1;
        shockwaveStruck.clear();
        shockwaveDirection = Vec3.directionFromRotation(0f, this.yBodyRot);

        // L'onde naît juste devant les pattes avant, et non au centre du corps. Partie du centre,
        // elle passait ses premiers blocs à ramper sous le ventre de la bête — invisible, sans effet,
        // et à sa vitesse de propagation cela faisait plusieurs secondes avant qu'elle n'émerge. Le
        // décalage se prend sur la boîte de collision pour suivre l'échelle de l'individu.
        shockwaveOrigin = this.position().add(shockwaveDirection.scale(this.getBbWidth() * 0.55));
        shockwaveY = this.getY();

        // Le bois qui éclate donne la sécheresse du départ, le grondement lui donne sa masse. Les
        // deux ensemble : sans le premier l'onde naît mollement, sans le second elle n'a pas de
        // corps. Même paire à l'impact des pattes du Tremblement de Terre.
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.NEUTRAL,
                3.5f, (float) OWUtils.generateRandomInterval(0.5, 0.65));
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.MINI_EARTHQUAKE.get(), SoundSource.NEUTRAL,
                3.0f, (float) OWUtils.generateRandomInterval(0.7, 0.85));
    }

    /** Remet à zéro tous les drapeaux de l'onde. */
    private void cancelShockwave() {
        shockwaveTick = 0;
        shockwaveStruck.clear();
    }

    /**
     * Altitude de la surface au point donné, cherchée autour d'une hauteur de référence.
     *
     * <p>La fenêtre est volontairement étroite — trois blocs de part et d'autre : l'onde doit
     * pouvoir dévaler un talus ou grimper une marche, mais une falaise doit la garder au pied. Sans
     * référence trouvée, on reste à la hauteur précédente plutôt que de sauter.</p>
     */
    private static double surfaceYNear(ServerLevel serverLevel, double x, double z, double fromY) {
        int base = Mth.floor(fromY);

        for (int dy = 3; dy >= -3; dy--) {
            BlockPos pos = new BlockPos(Mth.floor(x), base + dy, Mth.floor(z));
            if (serverLevel.getBlockState(pos).isAir()) continue;
            if (!serverLevel.isEmptyBlock(pos.above())) continue;
            return pos.getY() + 1.0;
        }
        return fromY;
    }

    private void tickShockwave() {
        if (shockwaveTick <= 0) return;
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            cancelShockwave();
            return;
        }

        double speed = OWAttacksConstants.Elephant.SHOCKWAVE_SPEED;
        double front = shockwaveTick * speed;

        if (front > OWAttacksConstants.Elephant.SHOCKWAVE_LENGTH) {
            cancelShockwave();
            return;
        }

        sweepShockwave(serverLevel, front - speed, front);
        shockwaveTick++;
    }

    /**
     * La tranche de couloir que l'onde vient de balayer : une gerbe du sol sur toute sa largeur, et
     * ce qui s'y tient est soulevé. Une victime n'encaisse qu'une fois — sans le registre, qui court
     * devant l'onde à sa vitesse resterait dans la tranche et se ferait frapper à chaque tick.
     */
    private void sweepShockwave(ServerLevel serverLevel, double back, double front) {
        double half = OWAttacksConstants.Elephant.SHOCKWAVE_HALF_WIDTH;
        Vec3 side = new Vec3(-shockwaveDirection.z, 0, shockwaveDirection.x);

        Vec3 axis = shockwaveOrigin.add(shockwaveDirection.scale(front));

        // L'onde épouse le relief : elle suit la surface au lieu de filer à l'altitude du départ,
        // ce qui l'enterrait à la moindre montée et la laissait flotter au premier creux. La hauteur
        // se reprend de proche en proche, jamais de plus de trois blocs par palier, pour qu'une
        // falaise l'arrête net plutôt que de la téléporter en haut.
        shockwaveY = surfaceYNear(serverLevel, axis.x, axis.z, shockwaveY);

        for (int lane = -1; lane <= 1; lane++) {
            Vec3 point = axis.add(side.scale(lane * half * 0.7));
            double laneY = surfaceYNear(serverLevel, point.x, point.z, shockwaveY);

            BlockState ground = serverLevel.getBlockState(
                    BlockPos.containing(point.x, laneY - 0.1, point.z));
            if (ground.isAir()) continue;

            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                    point.x, laneY + 0.3, point.z, 14, 0.3, 0.2, 0.3, 0.15);
        }

        Vec3 a = new Vec3(shockwaveOrigin.x, shockwaveY, shockwaveOrigin.z)
                .add(shockwaveDirection.scale(back));
        Vec3 b = new Vec3(shockwaveOrigin.x, shockwaveY, shockwaveOrigin.z)
                .add(shockwaveDirection.scale(front));
        AABB box = new AABB(a, b).inflate(half, 2.5, half);

        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, box)) {
            if (target == this || target.getRootVehicle() == this) continue;
            if (this.isAlliedTo(target)) continue;

            Vec3 relative = target.position().subtract(
                    new Vec3(shockwaveOrigin.x, shockwaveY, shockwaveOrigin.z));
            double along = relative.dot(shockwaveDirection);
            if (along <= back || along > front) continue;
            if (Math.abs(relative.dot(side)) > half) continue;
            if (Math.abs(relative.y) > 3.0) continue;

            if (!shockwaveStruck.add(target.getId())) continue;

            target.hurt(this.damageSources().mobAttack(this), OWAttacksConstants.Elephant.SHOCKWAVE_DAMAGE);
            target.push(-shockwaveDirection.x * OWAttacksConstants.Elephant.SHOCKWAVE_PUSHBACK,
                    OWAttacksConstants.Elephant.SHOCKWAVE_LAUNCH,
                    -shockwaveDirection.z * OWAttacksConstants.Elephant.SHOCKWAVE_PUSHBACK);
            target.hurtMarked = true;
        }
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

    /** Remet à zéro tous les drapeaux du séisme — aucun ne doit survivre à sa fin. */
    private void cancelEarthquake() {
        this.entityData.set(EARTHQUAKE_TICK, 0);
        earthquakeImpactDone = false;
    }

    /** L'instant du contact : le grondement part, et l'épicentre se fige là où les pattes ont frappé. */
    private void executeEarthquakeImpact() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        earthquakeEpicentre = this.position();

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.NEUTRAL, 4.0f, 0.4f);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.MINI_EARTHQUAKE.get(), SoundSource.NEUTRAL, 4.0f, 0.6f);
    }

    /**
     * Une pulsation du séisme, une par seconde pendant dix secondes.
     *
     * <p>Huit blocs pris au hasard dans le rayon cèdent, et tout ce qui s'y trouve trébuche. Le tirage
     * aléatoire vaut mieux qu'un balayage : sur un rayon de trente-deux blocs, parcourir les trois
     * mille colonnes du disque à chaque pulsation coûterait cher pour un résultat qui se lit moins
     * bien — huit effondrements dispersés donnent un sol qui se ruine par plaques.</p>
     */
    private void pulseEarthquake(ServerLevel serverLevel) {
        double radius = OWAttacksConstants.Elephant.EARTHQUAKE_RADIUS;

        serverLevel.playSound(null, earthquakeEpicentre.x, earthquakeEpicentre.y, earthquakeEpicentre.z,
                OWSounds.MINI_EARTHQUAKE.get(), SoundSource.NEUTRAL, 2.5f,
                (float) OWUtils.generateRandomInterval(0.55, 0.75));

        collapseScatteredBlocks(serverLevel, radius);
        shakeEntitiesAround(serverLevel, radius);
    }

    /**
     * Les blocs qui cèdent. Chaque tirage vise une colonne au hasard dans le disque et n'y prend que
     * la surface : les blocs à inventaire et l'indestructible sont épargnés — un éléphant ne doit pas
     * pouvoir vider un coffre en tapant du pied — et l'ouvrage du propriétaire ou de sa tribu tient
     * bon (cf. {@link OWPlacedBlocks}).
     */
    private void collapseScatteredBlocks(ServerLevel serverLevel, double radius) {
        if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return;

        BlockPos origin = BlockPos.containing(earthquakeEpicentre);

        for (int attempt = 0; attempt < OWAttacksConstants.Elephant.EARTHQUAKE_BLOCKS_PER_PULSE; attempt++) {
            double angle = this.random.nextDouble() * Math.PI * 2;
            // Racine du tirage : sans elle les points se tassent au centre, où l'aire est la plus
            // faible, et les bords du rayon ne cèdent jamais.
            double distance = Math.sqrt(this.random.nextDouble()) * radius;

            int x = origin.getX() + Mth.floor(Math.cos(angle) * distance);
            int z = origin.getZ() + Mth.floor(Math.sin(angle) * distance);

            collapseSurfaceAt(serverLevel, x, z, origin.getY());
        }
    }

    /** Cherche la surface dans la colonne visée et la fait céder, avec ses voisines. */
    private void collapseSurfaceAt(ServerLevel serverLevel, int x, int z, int aroundY) {
        for (int dy = 3; dy >= -3; dy--) {
            BlockPos pos = new BlockPos(x, aroundY + dy, z);
            if (!serverLevel.getBlockState(pos).isAir() || dy == -3) {
                if (breakSurfaceBlock(serverLevel, pos)) spreadCollapse(serverLevel, pos);
                return;
            }
        }
    }

    /**
     * La fissure gagne de proche en proche : un bloc qui cède peut en entraîner deux autres autour
     * de lui, au hasard.
     *
     * <p>La propagation ne se rappelle pas elle-même — un seul palier, jamais en cascade. Récursive,
     * une suite de tirages heureux aurait pu éventrer tout le rayon d'un coup, et le coût d'une
     * pulsation serait devenu imprévisible.</p>
     */
    private void spreadCollapse(ServerLevel serverLevel, BlockPos origin) {
        int spread = 0;

        // Le tour des voisins démarre à une face tirée au sort : les deux premiers tirages gagnants
        // l'emportent, donc partir toujours du nord y aurait creusé toutes les fissures.
        int first = this.random.nextInt(HORIZONTAL_SPREAD.length);

        for (int step = 0; step < HORIZONTAL_SPREAD.length; step++) {
            Direction direction = HORIZONTAL_SPREAD[(first + step) % HORIZONTAL_SPREAD.length];

            if (spread >= EARTHQUAKE_MAX_SPREAD) return;
            if (this.random.nextFloat() > EARTHQUAKE_SPREAD_CHANCE) continue;

            // On suit le relief d'un cran : une marche voisine cède aussi, pas seulement le plan.
            BlockPos side = origin.relative(direction);
            if (breakSurfaceBlock(serverLevel, side)
                    || breakSurfaceBlock(serverLevel, side.above())
                    || breakSurfaceBlock(serverLevel, side.below())) {
                spread++;
            }
        }
    }

    /**
     * Fait céder un bloc de surface s'il s'y prête. Les blocs à inventaire et l'indestructible sont
     * épargnés — un éléphant ne doit pas pouvoir vider un coffre en tapant du pied — et l'ouvrage du
     * propriétaire ou de sa tribu tient bon.
     *
     * @return vrai si le bloc a effectivement cédé
     */
    private boolean breakSurfaceBlock(ServerLevel serverLevel, BlockPos pos) {
        BlockState state = serverLevel.getBlockState(pos);

        if (state.isAir()) return false;
        if (state.hasBlockEntity()) return false;
        if (!state.getFluidState().isEmpty()) return false;
        if (!serverLevel.isEmptyBlock(pos.above())) return false;

        float hardness = state.getDestroySpeed(serverLevel, pos);
        if (hardness < 0 || hardness > 3.0f) return false;
        if (OWPlacedBlocks.isProtectedFrom(serverLevel, pos, this.getOwnerUUID())) return false;

        serverLevel.destroyBlock(pos, false);
        OWPlacedBlocks.get(serverLevel).forget(pos);
        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                10, 0.3, 0.15, 0.3, 0.1);
        return true;
    }

    /**
     * Ce que la pulsation secoue. Deux points de dégâts et un sursaut : pris isolément ce n'est
     * rien, mais dix fois de suite dans un rayon de trente-deux blocs, c'est une zone qu'on ne
     * traverse pas. Sauter n'y échappe pas — le sol tremble, il ne frappe pas.
     */
    private void shakeEntitiesAround(ServerLevel serverLevel, double radius) {
        AABB box = new AABB(earthquakeEpicentre, earthquakeEpicentre).inflate(radius, radius * 0.5, radius);

        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, box)) {
            if (target == this || target.getRootVehicle() == this) continue;
            if (this.isAlliedTo(target)) continue;
            if (target.position().distanceTo(earthquakeEpicentre) > radius) continue;

            target.hurt(this.damageSources().mobAttack(this), OWAttacksConstants.Elephant.EARTHQUAKE_PULSE_DAMAGE);

            double footing = 1.0 - Mth.clamp(target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.0, 1.0);
            target.push(0, OWAttacksConstants.Elephant.EARTHQUAKE_PULSE_HOP * footing, 0);
            target.hurtMarked = true;
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

        if (this.isEarthquakeGesture()) {
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
     * Les trois frappes durent 1,68 s, soit 33,6 ticks à vitesse 1. Lues à 0,85 / 0,95 / 1,05 elles
     * occupent 39,5 / 35,4 / 32,0 ticks ; les minuteurs ajoutent le tiers de marge pendant lequel
     * l'éléphant tient sa pose finale, sans quoi le geste est tranché avant sa dernière image.
     */
    private void setupComboAnimations() {
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, (int) (53 / comboSpeedMultiplier));
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, (int) (47 / comboSpeedMultiplier));
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, (int) (43 / comboSpeedMultiplier));
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
     * Vrai pendant le geste lui-même — les 4,16 s de {@code super_attack} —, faux ensuite. Le
     * compteur, lui, continue de courir le temps de la réplique : c'est cette distinction qui rend
     * la main au cavalier au moment où l'éléphant se relève, pendant que le sol tremble encore.
     */
    public boolean isEarthquakeGesture() {
        int tick = getEarthquakeTick();
        return tick > 0 && tick <= OWAttacksConstants.Elephant.EARTHQUAKE_TOTAL_TICKS;
    }

    /**
     * Intensité de la secousse de caméra, de l'impact à la fin du séisme. Lue côté client par
     * {@code ClientEvents} pour tous les joueurs à portée, cavalier compris.
     */
    public float getEarthquakeShakeIntensity() {
        int tick = getEarthquakeTick();
        int windup = OWAttacksConstants.Elephant.EARTHQUAKE_WINDUP_TICKS;
        int elapsed = tick - windup;

        if (elapsed < 0 || elapsed > OWAttacksConstants.Elephant.EARTHQUAKE_DURATION_TICKS) return 0f;

        // La secousse retombe entre deux pulsations sans jamais s'annuler : le sol reste instable
        // toute la durée du séisme, mais chaque effondrement se sent passer.
        int interval = OWAttacksConstants.Elephant.EARTHQUAKE_PULSE_INTERVAL;
        float sincePulse = (float) (elapsed % interval) / interval;
        float floor = OWAttacksConstants.Elephant.EARTHQUAKE_SHAKE_FLOOR;

        return OWAttacksConstants.Elephant.EARTHQUAKE_SHAKE_INTENSITY
                * (floor + (1f - floor) * (1f - sincePulse));
    }

    /**
     * Teinte d'une des deux bandes de laine de la selle : les couleurs de la bannière de la tribu.
     *
     * <p>Elles venaient des deux laines posées au Sellier, publiées par données synchronisées parce
     * que l'inventaire de la bête ne franchit pas le réseau. La tribu, elle, est <b>déjà</b>
     * répliquée à tous les clients (cf. {@code OWTribeFlagLayer}) : la selle se lit donc directement,
     * sans synchro ni rafraîchissement périodique.</p>
     *
     * <p>La troisième couleur de la bannière est ignorée : la selle n'a que deux bandes. Hors tribu,
     * on retombe sur le blanc et le gris clair.</p>
     */
    public int getSaddleWoolColor(int layer) {
        if (this.currentTeam == null) return layer == 0 ? NO_TRIBE_WOOL_0 : NO_TRIBE_WOOL_1;
        return layer == 0 ? this.currentTeam.getTeamColor() : this.currentTeam.getTeamSecondaryColor();
    }

    public int getUltimateKillCount() { return this.entityData.get(ULTIMATE_KILL_COUNT); }

    private void setUltimateKillCount(int count) { this.entityData.set(ULTIMATE_KILL_COUNT, Math.max(0, count)); }

    // ==================================================
    //              DONNÉES SAUVEGARDÉES
    // ==================================================

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("foodGiven", this.foodGiven);
        tag.putInt("foodWanted", this.foodWanted);
        tag.putInt("ultimateKillCount", this.getUltimateKillCount());
    }

    @Override
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
