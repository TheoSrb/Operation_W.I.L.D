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
    /**
     * Seul le fait d'observer voyage — jamais le décompte.
     *
     * <p>Un compteur synchronisé changerait de valeur à chaque tick, donc émettrait un paquet par
     * tick et par orque pendant toute la dressée. Le client n'a besoin que du booléen : il en déduit
     * son propre fondu.</p>
     */
    private static final EntityDataAccessor<Boolean> IS_SPYHOPPING = SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.BOOLEAN);

    private int dashTicksLeft = 0;
    private Vec3 dashDirection = Vec3.ZERO;

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
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.MOVEMENT_SPEED, 0.18D)
                .add(Attributes.FOLLOW_RANGE, 22.0D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new TryFindWaterGoal(this));

        // ── Rang 1 : tout ce qui doit pouvoir interrompre la chasse (rang 2) ────────────────
        //
        // À rang égal, aucun goal n'en préempte un autre : c'est l'ORDRE D'ENREGISTREMENT qui
        // départage, et il se lit donc comme un ordre de préséance.
        //
        // La vague passe en tête. Elle était derrière le trou de respiration, or sous une banquise
        // celui-ci trouve toujours une calotte au-dessus de lui : il partait systématiquement
        // percer la glace et la vague ne se déclenchait jamais. Une orque ne suffoque de toute
        // façon pas sous l'eau — sa réserve ne baisse qu'à l'air libre —, si bien que le trou de
        // respiration relève de la mise en scène quand la chasse, elle, n'attend pas.
        this.goalSelector.addGoal(1, new net.tiew.operationWild.entity.goals.orca.OWOrcaWaveWashGoal(this));
        // Une fois la proie en gueule, plus rien ne doit défaire la descente : le goal se déclare
        // lui-même non interruptible.
        this.goalSelector.addGoal(1, new net.tiew.operationWild.entity.goals.orca.OWOrcaAbyssalDiveGoal(this));
        // C'est précisément au moment où l'orque a gagné qu'elle cesse de tuer pour s'amuser.
        this.goalSelector.addGoal(1, new net.tiew.operationWild.entity.goals.orca.OWOrcaPreyToyGoal(this));
        this.goalSelector.addGoal(1, new net.tiew.operationWild.entity.goals.orca.OWOrcaBreathingHoleGoal(this));
        // La curiosité en dernier. En rang inférieur, le spyhop préemptait la charge à l'instant
        // précis où la ligne arrivait sur la proie : les orques se dressaient pour regarder au lieu
        // de déferler. Une manœuvre qui met dix secondes à se monter ne peut pas céder le pas à une
        // envie de lever la tête.
        this.goalSelector.addGoal(1, new net.tiew.operationWild.entity.goals.orca.OWOrcaSpyhopGoal(this));
        // Le suivi de bateau est réservé aux orques apprivoisées. Une orque sauvage qui escorte les
        // coques comme un dauphin contredit frontalement la percussion ajoutée plus bas — et,
        // prioritaire, elle lui aurait pris le déplacement en permanence.
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
        // La meute AVANT la passe solitaire : enregistrée après, elle n'aurait plus jamais eu la
        // main, la passe se déclenchant dans exactement les mêmes conditions. Une orque isolée
        // échoue à former sa meute et retombe naturellement sur la passe.
        this.goalSelector.addGoal(2, new OWOrcaPackHuntGoal(this));
        this.goalSelector.addGoal(2, new OWAttackGoal(this, this.getSpeed() * 20f, 28, 4, false) {
            private int dashCooldown = WILD_DASH_COOLDOWN_MIN;

            private boolean isBlockedForWild() {
                if (OrcaEntity.this.isTame()) return false;
                LivingEntity t = OrcaEntity.this.getTarget();
                return t != null && !isReachableFromWater(t);
            }

            /**
             * Ponctue la poursuite d'une Ruée, au lieu de n'enchaîner que des morsures.
             *
             * <p>Le goal parent ne connaît que le corps à corps : il approche, mord, recommence. La
             * Ruée est ce qui manquait à une chasse en pleine eau — une charge qui traverse, bouscule
             * et repart, là où la morsure suppose d'être déjà au contact.</p>
             *
             * <p>Elle ne part qu'à distance moyenne : trop près elle n'aurait pas d'élan, trop loin
             * la proie a le temps de s'écarter. Les dégâts sont ceux prévus de longue date pour une
             * charge sauvage — trois dixièmes de la morsure —, l'intérêt étant la bousculade et le
             * terrain gagné, pas la blessure.</p>
             */
            @Override
            public void tick() {
                super.tick();
                if (OrcaEntity.this.isTame()) return;
                if (this.dashCooldown > 0) {
                    this.dashCooldown--;
                    return;
                }
                if (OrcaEntity.this.isDashing() || OrcaEntity.this.isCombo()) return;
                if (!OrcaEntity.this.isInWater()) return;

                LivingEntity t = OrcaEntity.this.getTarget();
                if (t == null || !t.isAlive() || !t.isInWater()) return;
                double distance = OrcaEntity.this.distanceTo(t);
                if (distance < WILD_DASH_MIN_RANGE || distance > WILD_DASH_MAX_RANGE) return;
                if (!OrcaEntity.this.getSensing().hasLineOfSight(t)) return;

                OrcaEntity.this.performWildDashAt(t);
                this.dashCooldown = WILD_DASH_COOLDOWN_MIN
                        + OrcaEntity.this.getRandom().nextInt(WILD_DASH_COOLDOWN_SPREAD);
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
        builder.define(TAIL_FLICK_TICKS, 0);
        builder.define(WAVE_BREACH_TICKS, 0);
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

    /**
     * Tangage, nez vers le ciel, d'une orque dressée à la verticale.
     *
     * <p>Négatif car l'assiette de nage compte la montée en négatif : {@code tickLean} déduit sa
     * pente d'un {@code atan2(-dy, …)}. Le spyhop se branche donc sur la même convention que le
     * reste du tangage, et non sur une seconde qui lui serait propre.</p>
     */
    private static final float SPYHOP_PITCH = -78.0f;
    private static final float SPYHOP_RISE = 0.09f;
    private static final float SPYHOP_FALL = 0.13f;

    /** Épaisseur d'eau maximale au-dessus de l'orque pour qu'elle puisse encore percer la surface. */
    private static final int SPYHOP_MAX_RISE = 6;

    /**
     * Part du gabarit maintenue sous la ligne d'eau pendant l'observation.
     *
     * <p>L'orque reste volontairement immergée : le modèle est bien plus long que sa boîte de
     * collision, et le basculement du corps suffit à sortir la tête. La faire réellement émerger
     * lui coûterait son souffle et la ferait battre de la queue comme un poisson échoué.</p>
     */
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

    /** Avancement de la dressée [0 – 1], interpolé pour le rendu. */
    public float getSpyhopAmount(float partialTick) {
        return Mth.lerp(partialTick, this.spyhopBlendPrev, this.spyhopBlend);
    }

    /**
     * Le tangage du spyhop se substitue progressivement à l'assiette de nage, au lieu de s'y ajouter.
     *
     * <p>Une orque qui se dresse a cessé de nager : additionner les deux la ferait basculer au-delà
     * de la verticale à la moindre pente résiduelle.</p>
     */
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

    /**
     * Maintient l'orque juste sous la ligne d'eau, immobile.
     *
     * <p>Les entrées de nage sont remises à zéro en plus de la vélocité : le contrôle de mouvement
     * les réalimente chaque tick tant qu'un chemin subsiste, et l'orque dérivait pendant qu'elle
     * était censée observer.</p>
     */
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

    /**
     * Altitude de la surface libre au-dessus de l'orque, ou {@code NaN} si elle ne peut pas percer.
     *
     * <p>Il ne suffit pas de trouver la fin de l'eau : sous une banquise, un ponton ou un bateau,
     * la tête ne sortirait de rien. Le bloc qui coiffe la colonne doit être du vide.</p>
     */
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

    /** Vrai si l'orque peut se dresser ici : de l'eau sous elle, du ciel au-dessus. */
    public boolean canSpyhopHere() {
        return this.isInWater() && !Double.isNaN(surfaceYAbove());
    }

    /** Hauteur d'eau sondée à la recherche d'une calotte : une orque peut chasser loin sous la banquise. */
    private static final int ICE_SCAN_HEIGHT = 16;

    /**
     * Position de la calotte de glace qui ferme la colonne d'eau au-dessus de l'orque, ou
     * {@code null} si la colonne débouche à l'air libre — ou sur autre chose que de la glace fine.
     *
     * <p>Seules la glace et la glace fondante sont retenues. La glace compactée et la glace bleue
     * sont de la matière de construction : les percer transformerait la feature en démolition
     * d'igloo, alors qu'il ne s'agit que de crever une banquise.</p>
     */
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

    // ── Charge de la vague de chasse ──────────────────────────────────────────

    private static final EntityDataAccessor<Boolean> IS_WAVE_CHARGING =
            SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.BOOLEAN);

    private static final float WAVE_CHARGE_RISE = 0.16f;
    private static final float WAVE_CHARGE_FALL = 0.09f;

    private float waveChargeBlend = 0f;
    private float waveChargeBlendPrev = 0f;

    /**
     * Assaut de vague en cours, mise en place comprise — état serveur, jamais synchronisé.
     *
     * <p>Distinct de {@link #isWaveCharging()}, qui ne couvre que la course elle-même et pilote
     * l'animation. Celui-ci vaut du premier repli jusqu'au déferlement, et sert à ce qu'aucune autre
     * envie ne vienne défaire une ligne qui met dix secondes à se former. Il n'a pas à voyager :
     * seuls les goals le consultent, et ils ne tournent que côté serveur.</p>
     */
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

    /**
     * Intensité de la charge [0 – 1], interpolée pour le rendu.
     *
     * <p>Elle monte vite et retombe lentement : l'animal se jette d'un coup, mais son élan met du
     * temps à se dissiper. L'inverse donnerait une charge qui s'arme mollement et se coupe net.</p>
     */
    public float getWaveChargeAmount(float partialTick) {
        return Mth.lerp(partialTick, this.waveChargeBlendPrev, this.waveChargeBlend);
    }

    /**
     * Pendant la charge, l'orque reste à plat : aucune assiette de nage ne s'applique.
     *
     * <p>La proie d'une vague se tient sur la glace, donc <b>au-dessus</b> de l'eau. Le tangage
     * libre se déduisant de la pente réellement parcourue, la remontée vers elle faisait pointer le
     * nez vers le ciel : au lieu d'un bélier arrivant à l'horizontale, on voyait trois orques
     * grimper vers la surface le museau levé.</p>
     *
     * <p>Couper la source vaut mieux que la compenser : {@code tickLean} laisse alors son tangage
     * retomber tout seul, sans à-coup, et la seule inclinaison qui subsiste est le nez plongeant
     * que l'animation d'assaut pose volontairement.</p>
     */
    @Override
    protected float pitchMaxAngle() {
        // Le bond y est joint : en l'air, l'assiette se déduirait de la pente balistique et
        // viendrait s'ajouter à la cambrure, qui est déjà chargée de dire la même chose.
        return this.isWaveCharging() || this.isWaveBreaching() ? 0f : super.pitchMaxAngle();
    }

    // ── Bond de la vague : sortie d'eau, vrille complète, coup de queue ────────

    /**
     * Découpage du bond qui clôt la charge.
     *
     * <p>Le déferlement ne tombe pas à la fin du geste mais à {@link #WAVE_BREACH_SLAM}, quand la
     * caudale claque : c'est le coup de queue qui lève la vague, pas la retombée.</p>
     */
    public static final int WAVE_BREACH_DURATION = 26;
    public static final int WAVE_BREACH_SLAM = 18;

    /** Tour complet à plat, autour de l'axe vertical. */
    private static final float WAVE_SPIN_TURN = 360f;

    private static final EntityDataAccessor<Integer> WAVE_BREACH_TICKS =
            SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.INT);

    public int getWaveBreachTicks() {
        return this.entityData.get(WAVE_BREACH_TICKS);
    }

    public boolean isWaveBreaching() {
        return this.getWaveBreachTicks() > 0;
    }

    public void startWaveBreach() {
        if (this.level().isClientSide()) return;
        this.entityData.set(WAVE_BREACH_TICKS, WAVE_BREACH_DURATION);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                net.minecraft.sounds.SoundEvents.DOLPHIN_JUMP, SoundSource.HOSTILE, 2.0f, 0.5f);
    }

    /** Image exacte du claquement — pour ce qui ne doit se déclencher qu'une fois, ici même. */
    public boolean isWaveBreachSlam() {
        return this.getWaveBreachTicks() == WAVE_BREACH_DURATION - WAVE_BREACH_SLAM;
    }

    /**
     * Le claquement est-il atteint ou passé ?
     *
     * <p>Pour qui l'observe de l'extérieur. Une égalité stricte suppose de tomber pile sur la bonne
     * image ; un tick perdu — et il en suffit d'un — et la vague ne partirait jamais.</p>
     */
    public boolean hasWaveBreachSlammed() {
        int left = this.getWaveBreachTicks();
        return left > 0 && left <= WAVE_BREACH_DURATION - WAVE_BREACH_SLAM;
    }

    /**
     * Vrille du bond [0 – 360°] autour de l'axe vertical, interpolée pour le rendu.
     *
     * <p>Un pivot à plat, et non un tonneau : le corps reste horizontal et fait le tour sur
     * lui-même, la queue balayant tout ce qui est autour — ce qui donne au coup qui suit la
     * trajectoire d'un revers.</p>
     *
     * <p>Amortie en fin de course : le tour part sec, porté par l'élan de la charge, et s'achève en
     * douceur au moment où la queue prend le relais. Une rotation linéaire aurait tourné comme une
     * pièce mécanique.</p>
     */
    public float getWaveSpinYaw(float partialTick) {
        int left = this.getWaveBreachTicks();
        if (left <= 0) return 0f;
        float elapsed = WAVE_BREACH_DURATION - (left - partialTick);
        float t = Mth.clamp(elapsed / WAVE_BREACH_DURATION, 0f, 1f);
        float remaining = 1f - t;
        float eased = 1f - remaining * remaining * remaining;
        return WAVE_SPIN_TURN * eased;
    }

    private void tickWaveBreach() {
        if (this.level().isClientSide()) return;
        int left = this.getWaveBreachTicks();
        if (left <= 0) return;
        // Le coup de queue est armé ici et non par le goal : le geste appartient au bond, il doit
        // partir à la même image quelle que soit la raison qui a déclenché celui-ci.
        if (isWaveBreachSlam()) startTailFlick();
        this.entityData.set(WAVE_BREACH_TICKS, left - 1);
    }

    private void tickWaveCharge() {
        this.waveChargeBlendPrev = this.waveChargeBlend;
        float target = this.isWaveCharging() ? 1f : 0f;
        this.waveChargeBlend += (target - this.waveChargeBlend)
                * (target > this.waveChargeBlend ? WAVE_CHARGE_RISE : WAVE_CHARGE_FALL);
        if (this.waveChargeBlend < 0.001f) this.waveChargeBlend = 0f;
    }

    // ── Jeu de la proie : coup de queue ───────────────────────────────────────

    /**
     * Découpage du coup de queue, sur le même modèle que la happe.
     *
     * <p>L'armement est plus long que la détente : c'est le rapport entre les deux qui fait le
     * claquement. Une queue qui s'enroule aussi vite qu'elle se détend ne fouette pas, elle
     * balaie.</p>
     */
    public static final int FLICK_ANIM_WINDUP = 7;
    public static final int FLICK_ANIM_SNAP = 4;
    public static final int FLICK_ANIM_RECOVER = 9;
    public static final int FLICK_ANIM_DURATION =
            FLICK_ANIM_WINDUP + FLICK_ANIM_SNAP + FLICK_ANIM_RECOVER;

    private static final EntityDataAccessor<Integer> TAIL_FLICK_TICKS =
            SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.INT);

    public int getTailFlickTicks() {
        return this.entityData.get(TAIL_FLICK_TICKS);
    }

    public static int getTailFlickDuration() {
        return FLICK_ANIM_DURATION;
    }

    public boolean isTailFlicking() {
        return this.getTailFlickTicks() > 0;
    }

    public void startTailFlick() {
        if (this.level().isClientSide()) return;
        this.entityData.set(TAIL_FLICK_TICKS, FLICK_ANIM_DURATION);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                net.minecraft.sounds.SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.NEUTRAL, 1.4f, 0.7f);
    }

    /**
     * Instant exact où la nageoire touche la proie : à mi-claquement, pas à son terme.
     *
     * <p>Attendre la fin du geste ferait décoller la proie une fois la queue déjà redescendue.</p>
     */
    public boolean isTailFlickImpact() {
        return this.getTailFlickTicks()
                == FLICK_ANIM_DURATION - (FLICK_ANIM_WINDUP + FLICK_ANIM_SNAP / 2);
    }

    private void tickTailFlick() {
        if (this.level().isClientSide()) return;
        int left = this.getTailFlickTicks();
        if (left > 0) this.entityData.set(TAIL_FLICK_TICKS, left - 1);
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

    /**
     * Cible qu'une orque sauvage peut atteindre sans quitter son élément.
     *
     * <p>La règle voulue est « on ne poursuit pas une proie hors de l'eau ». Elle se lisait sur la
     * seule immersion de la cible, ce qui rangeait un joueur en bateau avec un joueur réfugié sur
     * une falaise : la chasse se fermait, et l'orque tournait sous la coque sans jamais rien
     * tenter. Ce qui est porté par la surface reste frappable d'en dessous.</p>
     */
    private static boolean isReachableFromWater(LivingEntity target) {
        if (target.isInWater()) return true;
        if (target.getVehicle() instanceof Boat) return true;
        BlockPos underfoot = BlockPos.containing(target.getX(), target.getY() - 0.1, target.getZ());
        return target.level().getFluidState(underfoot).is(net.minecraft.tags.FluidTags.WATER);
    }

    @Override
    public int getEntityColor() {
        return 0x28313e;
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
        return List.of(Boat.class, Player.class, Animal.class, Monster.class);
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
        return 315 * (1 + ((float) this.getLevel() / 50));
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
        return 65;
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

        // Le gabarit dépend de qui frappe. Une orque montée garde la portée large qui rend le combat
        // au dos agréable ; une orque sauvage mord à la taille de sa gueule. L'ancienne boîte —
        // six blocs et demi de large sur dix de haut, portée à quatre — englobait tout ce qui se
        // trouvait devant : aucun déplacement du joueur ne pouvait l'en faire sortir, et c'est de là
        // que venait la série qui ne rate jamais.
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
                float t = this.dashTicksLeft / 30f;
                float speed = 3.8f * (t * t * t);

                applyDashContactDamage();

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
                    this.flopHopTicks = FLOP_HOP_AIR_TICKS;
                    this.flopHopCooldown = FLOP_HOP_INTERVAL;

                    if (this.isControlledByLocalInstance()) {
                        Vec3 hop = flopHopDrive();
                        this.setDeltaMovement(hop.x, FLOP_VERTICAL_IMPULSE, hop.z);
                        this.hasImpulse = true;
                    }

                    if (!this.level().isClientSide()) {
                        this.playSound(net.minecraft.sounds.SoundEvents.COD_FLOP,
                                this.getSoundVolume() * FLOP_SOUND_VOLUME,
                                this.getVoicePitch() * FLOP_SOUND_PITCH);
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

    /**
     * Cadence de l'asphyxie à l'air libre, calée sur la noyade vanilla.
     *
     * <p>Celle-ci retire un point de souffle par tick puis frappe au seuil de −20 : un coup toutes
     * les vingt ticks. L'orque en consomme deux par tick — c'est voulu, la jauge doit se vider à vue
     * d'œil — mais elle héritait du même seuil, ce qui divisait par deux l'intervalle entre deux
     * coups. Le décompte des dégâts est donc découplé de la vitesse de vidange : la réserve descend
     * toujours aussi vite, seule la cadence des coups rejoint celle de la noyade.</p>
     */
    private static final int DRY_OUT_DAMAGE_INTERVAL = 20;
    private static final float DRY_OUT_DAMAGE = 2.0f;

    private int dryOutTicks = 0;

    @Override
    protected boolean isBreaching() {
        // Le bond de la vague compte comme un envol volontaire : sans cela, la bride de sortie d'eau
        // écrêterait son impulsion à trois dixièmes de bloc par tick et l'orque ne décollerait pas.
        return this.isDashing() || breachTicks > 0 || this.isWaveBreaching();
    }

    /**
     * Recale la vélocité que le SERVEUR garde en mémoire sur le mouvement réellement observé.
     *
     * <p>Une monture pilotée par un joueur n'est jamais déplacée par le serveur : celui-ci n'applique
     * donc jamais cette vélocité, et surtout ne la décroît jamais. La dernière valeur qu'on y a écrite
     * — l'élan d'un bond, la poussée d'une Ruée — y reste indéfiniment.</p>
     *
     * <p>Ça ne serait qu'un champ mort si {@code hurt()} ne levait pas {@code hurtMarked}, qui diffuse
     * cette vélocité à tous les clients, cavalier compris. Chaque dégât de dessèchement réinjectait
     * ainsi un vieux bond dans le client qui pilote : l'orque décollait. On lui rend la vérité, mesurée
     * sur le déplacement du tick.</p>
     */
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

    /**
     * Contact avec le sol mesuré sur le monde, et non lu sur {@code onGround()}.
     *
     * <p>Le drapeau du moteur n'est tenu à jour que par {@code move()}, et une monture pilotée par un
     * joueur n'est jamais déplacée par le serveur : celui-ci se contente d'appliquer la position que
     * le client lui envoie. Le drapeau y reste donc figé sur sa dernière valeur, et tout ce qui s'y
     * fiait — déclenchement du bond, son, phase aérienne — partait en morceaux dès qu'un cavalier
     * montait. La sonde, elle, ne dépend que de la position, qui est synchronisée.</p>
     */
    private boolean isFlopGrounded() {
        AABB box = this.getBoundingBox();
        AABB probe = new AABB(box.minX, box.minY - FLOP_GROUND_PROBE, box.minZ, box.maxX, box.minY, box.maxZ);
        return !this.level().noCollision(this, probe);
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

    /**
     * Recalage du combo sur la cible.
     *
     * <p>L'orque sauvage revient au comportement commun du mod : elle <b>fixe sa cible</b> pendant
     * toute la morsure, sans bride, comme n'importe quelle autre bête. Les brides essayées ici —
     * quatorze degrés, puis quatre, puis zéro — la faisaient mordre de biais, ce qui se lisait comme
     * une maladresse plutôt que comme une esquive du joueur.</p>
     *
     * <p>L'échec de la morsure ne tient donc plus à l'orientation mais au <b>gabarit</b> : la gueule
     * d'une orque sauvage ne couvre que 2,4 blocs de large sur 2,8 de portée, contre la boîte de
     * 6,5 sur 4 d'autrefois. S'écarter latéralement ne suffit plus — il faut sortir de sa portée,
     * en profondeur ou en hauteur. C'est le prix assumé d'une bête qui vise droit.</p>
     */
    @Override
    protected float comboTrackingDegreesPerTick() {
        if (this.isVehicle()) return 360f;
        return this.isTame() ? 14f : 360f;
    }

    /**
     * Gabarit de morsure d'une orque sauvage : une gueule, pas un mur.
     *
     * <p>Seul levier restant de l'esquive, l'orientation étant désormais libre. Il se règle donc à
     * la portée et non à l'angle : la boîte couvre en gros de un et demi à cinq blocs devant la
     * bête, soit tout juste de quoi atteindre une cible restée là où l'attaque s'est déclenchée.
     * S'éloigner, plonger ou remonter la fait manquer ; ne pas bouger, non.</p>
     *
     * <p>Trop resserré, l'échec cesse d'être une esquive et devient une infirmité — l'orque
     * traversait sa proie sans jamais l'atteindre. L'ancienne boîte, elle, faisait 6,5 de large
     * sur 4 de portée : elle englobait tout ce qui se trouvait devant.</p>
     */
    private static final double WILD_BITE_WIDTH = 3.5;
    private static final double WILD_BITE_HEIGHT = 2.5;
    private static final double WILD_BITE_REACH = 3.2;

    /** Fenêtre de distance dans laquelle une orque sauvage juge la Ruée intéressante. */
    private static final double WILD_DASH_MIN_RANGE = 5.0;
    private static final double WILD_DASH_MAX_RANGE = 16.0;

    /** Cadence de la Ruée sauvage : environ toutes les cinq à onze secondes de poursuite. */
    private static final int WILD_DASH_COOLDOWN_MIN = 100;
    private static final int WILD_DASH_COOLDOWN_SPREAD = 120;

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
    public void setTarget(@Nullable LivingEntity target) {
        if (this.isNapping() || this.isBaby()) {
            return;
        }

        super.setTarget(target);
    }

    /**
     * Jeu de la proie en cours : l'orque ne porte plus aucun coup ordinaire.
     *
     * <p>État serveur, jamais synchronisé — seul le goal le pose et le lit.</p>
     */
    private boolean playingWithPrey = false;

    public boolean isPlayingWithPrey() {
        return this.playingWithPrey;
    }

    public void setPlayingWithPrey(boolean value) {
        this.playingWithPrey = value;
    }

    /**
     * Cas où une orque sauvage ne porte aucun coup.
     *
     * <p><b>Hors de l'eau.</b> Privée de son élément, elle ne peut ni se tourner, ni se caler, ni
     * prendre appui : la voir continuer à frapper pendant qu'elle bat de la queue sur la berge la
     * faisait passer pour un monstre terrestre. L'échouage volontaire ({@code isBeached()}) fait
     * exception — c'est une chasse à part entière, et la brider là reviendrait à supprimer
     * {@code OWOrcaBeachingGoal}, qui n'existe que pour ça.</p>
     *
     * <p><b>Pendant le jeu de la proie.</b> Toute la feature tient sur une promesse : la victime
     * ressort vivante. Or l'orque frappe pour un tiers de sa vie d'un seul coup, si bien que le
     * palier de « proie vaincue » se franchit en un coup et que le suivant l'achevait avant même que
     * le jeu n'ait commencé — ou pendant, un combo déjà lancé continuant de porter tout seul. Le
     * plafond posé sur les dégâts de projection ne suffisait pas : il ne bornait que SES propres
     * coups. Ici, la mâchoire se tait entièrement, et seule la projection blesse.</p>
     *
     * <p>Les orques apprivoisées ne sont concernées par aucun des deux : leur cavalier attend
     * d'elles qu'elles frappent où il les mène.</p>
     */
    private boolean strikesAreSuppressed() {
        if (this.isTame()) return false;
        if (this.playingWithPrey) return true;
        return !this.isInWater() && !this.isBeached();
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
        // La Ruée traverse volontiers la surface : sans ce refus, une orque sauvage encaissait
        // toujours ses dégâts de contact en plein vol, soit la manière la plus courante de frapper
        // hors de l'eau.
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
            target.setDeltaMovement(target.getDeltaMovement()
                    .add(push.x * 1.2, 0.25, push.z * 1.2));
        }
    }

    public void performOrcaDash() {
        float cost = OWAttacksConstants.Orca.TIDAL_RUSH_ENERGY;
        if (getVitalEnergy() > getMaxVitalEnergy() - cost) {
            canShowVitalEnergyLack = true;
            return;
        }
        setVitalEnergy(getVitalEnergy() + cost);
        launchDash(getDashAimDirection());
    }

    /**
     * Ruée déclenchée par l'IA, vers une cible et non vers le regard d'un cavalier.
     *
     * <p>La visée est prise en trois dimensions, du regard de l'orque vers le centre de la proie :
     * {@link #getDashAimDirection()} lit le tangage du pilotage, remis à zéro sous l'eau chez une
     * bête libre, et n'aurait donc produit qu'une ruée rigoureusement horizontale — inoffensive dès
     * que la proie nage un peu plus haut ou plus bas.</p>
     *
     * <p>Elle ne consulte pas non plus la jauge d'énergie vitale. Celle-ci est un instrument de
     * pilotage, que le cavalier voit se remplir ; sur une bête sauvage elle ne serait qu'un refus
     * silencieux et imprévisible. La cadence est tenue par le goal, qui sait pourquoi il attend.</p>
     */
    public void performWildDashAt(LivingEntity target) {
        if (this.level().isClientSide() || target == null) return;
        Vec3 aim = target.getBoundingBox().getCenter().subtract(this.getEyePosition());
        if (aim.lengthSqr() < 1.0E-6) return;
        launchDash(aim.normalize());
    }

    private void launchDash(Vec3 direction) {
        this.dashDirection = direction;

        this.setDeltaMovement(this.dashDirection.scale(3.8));

        this.entityData.set(IS_DASHING, true);
        this.dashTicksLeft = 30;

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

    public void activateBigMouth() {
        if (this.level().isClientSide()) return;

        if (hasSwallowed()) {
            beginSpit();
            return;
        }

        LivingEntity prey = findMouthTarget();
        if (prey == null) {
            this.entityData.set(MOUTH_LUNGE_TICKS, MOUTH_LUNGE_DURATION);
            this.level().playSound(null, getX(), getY(), getZ(),
                    OWSounds.CROCODILE_MOUTH_CRUSH.get(), SoundSource.HOSTILE, 1.1f, 1.35f);
            return;
        }

        this.pendingPreyId = prey.getId();
        this.entityData.set(MOUTH_LUNGE_TICKS, MOUTH_LUNGE_DURATION);

        setOrcaUltimateKillCount(0);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.BUBBLE,
                    getX(), getY() + 0.6, getZ(), 18, 1.0, 0.5, 1.0, 0.06);
        }
    }

    private void closeMouthOnPrey() {
        int reserved = this.pendingPreyId;
        this.pendingPreyId = -1;
        if (reserved == -1) return;

        if (!(this.level().getEntity(reserved) instanceof LivingEntity prey)
                || !canSwallow(prey)
                || prey.distanceToSqr(this) > (MOUTH_REACH + 3.0) * (MOUTH_REACH + 3.0)) {
            this.level().playSound(null, getX(), getY(), getZ(),
                    OWSounds.CROCODILE_MOUTH_CRUSH.get(), SoundSource.HOSTILE, 1.1f, 1.35f);
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
                OWSounds.CROCODILE_MOUTH_CRUSH.get(), SoundSource.HOSTILE, 3.0f, 0.55f);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.BUBBLE,
                    getX(), getY() + 0.6, getZ(), 45, 1.4, 0.6, 1.4, 0.15);
        }
    }

    @Override
    public void setCombo(boolean isCombo, int numberOfAttacks) {
        if (isCombo && hasSwallowed()) return;
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
        return fitsInMouth(candidate);
    }

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
                OWSounds.CROCODILE_MOUTH_CRUSH.get(), SoundSource.HOSTILE, 1.0f, 0.4f);
    }

    // ── Descente aux abysses (Grande Gueule sauvage) ──────────────────────────

    /**
     * Prise destinée à la descente : la mâchoire cesse de mordre, seule la profondeur menace.
     *
     * <p>État serveur, jamais synchronisé — il ne change rien de visible, seulement la conduite de
     * la prise, et les goals qui le posent ne tournent que là.</p>
     */
    private boolean abyssalHold = false;

    public boolean isAbyssalHold() {
        return this.abyssalHold;
    }

    public void setAbyssalHold(boolean value) {
        this.abyssalHold = value;
    }

    /**
     * Prolonge la prise en cours.
     *
     * <p>La durée de morsure ordinaire — dix secondes — ne laisse pas le temps d'atteindre le fond.
     * La descente réclame donc de tenir bien plus longtemps, sans quoi l'orque recracherait sa proie
     * à mi-eau, ce qui n'aurait aucun intérêt.</p>
     */
    public void holdSwallowedFor(int ticks) {
        if (this.level().isClientSide() || !hasSwallowed()) return;
        this.mouthHoldTicks = Math.max(this.mouthHoldTicks, ticks);
    }

    /** Profondeur d'eau sondée sous l'orque à la recherche du fond. */
    private static final int SEABED_SCAN_DEPTH = 128;

    /**
     * Altitude du fond sous l'orque, ou {@code NaN} si elle n'a pas d'eau sous elle.
     *
     * <p>Renvoie le premier niveau encore liquide au-dessus du sol, et non le sol lui-même : c'est
     * là que la descente doit s'arrêter.</p>
     */
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

    /** Hauteur d'eau libre sous l'orque, ou 0 si le fond n'a pas été trouvé. */
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
                        OWSounds.CROCODILE_MOUTH_CRUSH.get(), SoundSource.HOSTILE, 1.5f, 1.25f);
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

        // Prise en gueule, la proie ne se noie pas : elle est TENUE, pas maintenue sous l'eau. La
        // réserve d'air ne servait qu'aux prises amicales, ce qui suffisait tant que la morsure
        // durait dix secondes ; une descente vers le fond, elle, tuerait par asphyxie avant même
        // d'avoir touché le sable, et tout l'enjeu de la remontée disparaîtrait avec la victime.
        prey.setAirSupply(prey.getMaxAirSupply());

        // La descente aux abysses ne mord pas : sa menace est la profondeur, pas la mâchoire. Les
        // deux cumulées ne laisseraient rien à relâcher au fond.
        if (!friendly && !this.abyssalHold && this.tickCount % MOUTH_DAMAGE_INTERVAL == 0) {
            prey.invulnerableTime = 0;
            prey.hurt(this.damageSources().mobAttack(this), this.getDamage() * MOUTH_BITE_RATIO);
            this.level().playSound(null, getX(), getY(), getZ(),
                    OWSounds.CROCODILE_MOUTH_CRUSH.get(), SoundSource.HOSTILE, 0.8f, 0.5f);
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
    public boolean hurt(DamageSource damageSource, float v) {
        return super.hurt(damageSource, v);
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

            orca.getMoveControl().setWantedPosition(targetX, targetY, targetZ, 1.0);
        }

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

            double angle = orca.getRandom().nextDouble() * Math.PI * 2;
            double dist = 8 + orca.getRandom().nextDouble() * 12;
            targetX = orca.getX() + Math.sin(angle) * dist;
            targetZ = orca.getZ() + Math.cos(angle) * dist;

            double seaLevel = orca.level().getSeaLevel();
            double minY = seaLevel - orca.getMaxDepth() + 5;
            double maxY = seaLevel - 2;
            targetY = Mth.clamp(orca.getY() + (orca.getRandom().nextDouble() - 0.5) * 4, minY, maxY);
        }
    }
}