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
    private static final EntityDataAccessor<Float>   RIDER_CONTROL_PITCH  = SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_DASHING           = SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_BEACHED           = SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> PACK_ROLE            = SynchedEntityData.defineId(OrcaEntity.class, EntityDataSerializers.INT);

    private int dashTicksLeft = 0;
    private Vec3 dashDirection = Vec3.ZERO;

    /** Server-side pack coordinator (pack-hunting mechanic for wild orcas). */
    public final OrcaBehaviorHandler orcaBehaviorHandler = new OrcaBehaviorHandler(this);
    /** Transient reference to the pack leader (server-only, never serialized). */
    private OrcaEntity packLeader = null;



    public volatile float bodyAnimY = 0f;
    public volatile float bodyAnimXRot = 0f;


    public volatile float bodyAnimX = 0f;
    public volatile float bodyAnimY_passenger = 0f;
    public volatile float bodyZRot_passenger  = 0f;
    public volatile float bodyXRot_passenger  = 0f;
    public volatile float bodyAnimX_passenger = 0f;

    /**
     * Matrice de la chaine d'os {@code ALL2 -> ALL -> body}, relevee a chaque image par le modele.
     *
     * <p>C'est elle qui porte la verite : pivots, ordre de composition des rotations et translations
     * d'animation. Le siege s'en deduit au lieu d'etre reconstruit en trigonometrie, ce qui evitait
     * mal les pieges du repere modele (Y vers le bas, pivots herites, axes retournes au rendu).</p>
     */
    public volatile org.joml.Matrix4f boneMatrix = null;

    /**
     * Pose de repos de la chaine, en pixels modele : {@code ALL2} est pose a (0, 7, -2),
     * {@code ALL} et {@code body} n'ajoutent rien (cf. {@code OrcaModel#createBodyLayer}).
     *
     * <p>Elle sert de reference : on ne place pas le siege d'apres la matrice, on le DECALE de
     * l'ecart entre la pose courante et celle-ci. Au repos l'ecart est nul, et le calcul redonne
     * donc exactement la position d'aujourd'hui — celle qui est juste. Rien ne peut se degrader a
     * plat, et le mouvement des os vient s'y ajouter proprement.</p>
     */
    private static final float REST_X = 0f, REST_Y = 7f, REST_Z = -2f;

    /** Hauteur du repere modele au-dessus de l'origine de l'entite, en blocs (cf. LivingEntityRenderer). */
    private static final float MODEL_ORIGIN_Y = 1.501f;

    /** Avance permanente des places, en blocs. Retouche fine du calage de la selle. */
    private static final float SEAT_FORWARD = 0.1f;

    /**
     * De combien les places avancent pendant un combo.
     *
     * <p>Valeur posée à l'œil, en plus de ce que la matrice d'os mesure : un simple réglage de
     * l'assise pendant une attaque, à retoucher librement.</p>
     */
    private static final float COMBO_SEAT_FORWARD = 0.2f;

    /**
     * Lacet du corps, pour que le cavalier suive l'orque sur les trois axes et non plus seulement en
     * roulis et tangage.
     */
    public volatile float bodyYRot = 0f;
    public volatile float bodyYRot_passenger = 0f;

    /**
     * Orientation a suivre par la CAMERA : {@code ALL + body} seuls, sans {@code ALL2}.
     *
     * <p>{@code ALL2} porte le pique commande au regard — c'est lui qui fait plonger l'orque quand
     * le pilote baisse les yeux. Le repercuter sur la camera reviendrait a compter deux fois le
     * meme geste : le joueur regarde deja vers le bas. Le modele du cavalier, lui, doit bien
     * l'inclure, sans quoi il resterait droit pendant que sa monture pique.</p>
     */
    public volatile float camXRot = 0f, camYRot = 0f, camZRot = 0f;


    private int orcaUltimateKillCount = 0;

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
        this.goalSelector.addGoal(1, new FollowBoatGoal(this));
        this.goalSelector.addGoal(2, new OWAttackGoal(this, this.getSpeed() * 20f, 28, 4, false) {
            // Pour les orques sauvages : OWOrcaBeachingGoal gère les targets sur la côte.
            // Ce goal s'arrête dès que la target quitte l'eau pour libérer les flags MOVE+LOOK.
            private boolean isBlockedForWild() {
                if (OrcaEntity.this.isTame()) return false;
                LivingEntity t = OrcaEntity.this.getTarget();
                return t != null && !t.isInWater();
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
        // Pack hunting takes precedence over the solo attack goal above (shares MOVE+LOOK).
        // A lone orca with no recruitable packmates fails to form a pack and falls back to it.
        this.goalSelector.addGoal(2, new OWOrcaPackHuntGoal(this));
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
    }

    @Override
    protected boolean isLeapingVehicle() {
        return this.entityData.get(IS_DASHING);
    }

    public void setDashing(boolean dashing) {
        this.entityData.set(IS_DASHING, dashing);
    }

    public boolean isDashing() {
        return this.entityData.get(IS_DASHING);
    }

    /**
     * Durée du tonneau, en ticks — un peu plus courte que la ruée, qui en dure 30.
     *
     * <p>Deux raisons. La figure se referme pendant que la bête file encore, ce qui la rend plus
     * lisible qu'un tour qui s'achèverait à l'arrêt. Et surtout elle survit à l'annulation : le
     * cavalier peut couper la ruée dès le quinzième tick en poussant en avant, or un tonneau
     * interrompu à mi-course laisserait l'orque sur le flanc.</p>
     *
     * <p>C'est une borne, pas un rythme : la courbe de {@link #getBarrelRoll} place l'essentiel du
     * tour dans les premiers ticks. Rallonger cette durée n'étale pas la figure, ça allonge le palier
     * à plat qui la suit.</p>
     */
    private static final int BARREL_DURATION = 24;

    /** Un tour complet, vers la droite de la bête. */
    private static final float BARREL_TURN = -360f;

    private float barrelProgress = 1f;
    private float barrelProgressPrev = 1f;
    private boolean barrelRunning = false;
    private boolean wasDashing = false;

    /**
     * Angle du tonneau, en degrés, interpolé sur la fraction de tick.
     *
     * <p>La progression passe par une décélération cubique : la vitesse angulaire part à <b>trois
     * fois</b> la moyenne puis retombe à zéro. Le tour s'enclenche donc d'un coup de rein, sans la
     * montée en régime d'une courbe symétrique où les premiers ticks ne bougeaient presque pas et
     * donnaient l'impression d'un départ en retard.</p>
     *
     * <p>L'exposant est le réglage de violence. En puissance quatrième, le départ tapait à quatre
     * fois la moyenne et la moitié du tour passait en quatre ticks — trop sec ; en cube, il reste
     * franc sans arracher. Le monter le durcit, le descendre vers 2 l'adoucit encore.</p>
     */
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

    private void tickBarrelRoll() {
        this.barrelProgressPrev = this.barrelProgress;

        // Front montant du drapeau de ruée, déjà synchronisé : tous les clients enclenchent la figure
        // au même tick et déroulent la même courbe. Rien à transmettre.
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
        }
    }

    public boolean isBeached() { return this.entityData.get(IS_BEACHED); }
    public void setBeached(boolean beached) { this.entityData.set(IS_BEACHED, beached); }

    // ── Pack hunting (wild orcas) ──────────────────────────────────────────────
    public OrcaBehaviorHandler getOrcaBehaviorHandler() { return this.orcaBehaviorHandler; }

    /** Attack order within the pack: -1 = none, 0 = leader, 1..2 = followers. */
    public int getPackRole() { return this.entityData.get(PACK_ROLE); }
    public void setPackRole(int role) { this.entityData.set(PACK_ROLE, role); }

    public @Nullable OrcaEntity getPackLeader() { return this.packLeader; }
    public void setPackLeader(@Nullable OrcaEntity leader) { this.packLeader = leader; }

    protected PathNavigation createNavigation(Level worldIn) {
        return new SwimmerJumpPathNavigator(this, worldIn);
    }

    // Entity Methods
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

    /** Vrai : l'orque se couche franchement en virage, la vue gagne à rester solidaire du corps. */
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
        return currentAir + 4;
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

    private void playStepSoundFromAnimation(float pitchMod) {
        if (!this.onGround()) return;
        if (this.isInWater()) return;
        if (this.getDeltaMovement().horizontalDistanceSqr() < 0.0001) return;
        long now = System.currentTimeMillis();
        if (now - lastStepSoundMs < 150L) return; // croc walks slower than tiger
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

        createCombo((int) (28 / comboSpeedMultiplier), (int) (18 / comboSpeedMultiplier), OWSounds.CROCODILE_MOUTH_CRUSH.get(), 4.5, 4, 3, false, 0.5f);
        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) {
            setupAnimationState();
            tickBarrelRoll();
        }
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
                    this.setDeltaMovement(
                            this.dashDirection.x * speed,
                            current.y,
                            this.dashDirection.z * speed
                    );
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
        super.aiStep();

        if (!this.isInWater()) {
            if (this.onGround()) {
                this.setDeltaMovement(
                        this.getDeltaMovement().x + (this.random.nextFloat() * 2.0f - 1.0f) * 0.2f,
                        0.5,
                        this.getDeltaMovement().z + (this.random.nextFloat() * 2.0f - 1.0f) * 0.2f
                );
                this.setYRot(this.random.nextFloat() * 360.0f);
                this.setOnGround(false);
                this.hasImpulse = true;
            }

            int air = this.getAirSupply();
            this.setAirSupply(air - 1);
            if (this.getAirSupply() <= -20) {
                this.setAirSupply(0);
                this.hurt(this.damageSources().dryOut(), 2.0f);
            }

            this.setXRot(90.0f);
            this.xRotO = 90.0f;
        } else {
            if (this.getAirSupply() < this.getMaxAirSupply()) {
                this.setAirSupply(Math.min(this.getAirSupply() + 4, this.getMaxAirSupply()));
            }
            this.setXRot(0.0f);
            this.xRotO = 0.0f;
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource); // le drop générique de l'Âme est géré par OWEntity.die()

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

    @Override
    public boolean doHurtTarget(Entity entity) {
        return super.doHurtTarget(entity);
    }

    @Override
    public void hurtAfterCombo(LivingEntity entity, int comboAttack) {

    }

    // ── TIDAL RUSH ─────────────────────────────────────────────────────────────

    /**
     * Damage dealt by the Tidal Rush dash. Full for a tamed orca; reduced for a wild one,
     * where the dash is used mainly as a mobility boost rather than a heavy hit.
     */
    private float getRushDamage() {
        return this.isTame()
                ? this.getDamage()
                : this.getDamage() * OWAttacksConstants.Orca.TIDAL_RUSH_WILD_DAMAGE_MULTIPLIER;
    }

    /**
     * Cibles déjà touchées par la ruée en cours. Vidée à chaque nouveau départ.
     *
     * <p>La ruée frappe désormais sur toute sa course et non plus pendant ses dix premiers ticks, ce
     * qui suppose de retenir qui a déjà encaissé : sans cela une cible restée devant l'orque prendrait
     * le coup à chaque tick de contact.</p>
     */
    private final java.util.Set<java.util.UUID> dashHits = new java.util.HashSet<>();

    /**
     * Dégâts de la ruée, appliqués au CONTACT, à chaque tick de la course.
     *
     * <p>La boîte de test balaie le trajet du tick au lieu de se poser sur la position courante :
     * lancée, l'orque franchit près de quatre blocs entre deux ticks, et une boîte figée laisserait
     * simplement passer tout ce qui se trouve entre les deux.</p>
     */
    private void applyDashContactDamage() {
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

        // Sauvegarde la direction horizontale pour le tick handler
        Vec3 look = this.getLookAngle();
        this.dashDirection = new Vec3(look.x, 0, look.z).normalize();

        // Kick initial — vitesse max dès le premier frame
        this.setDeltaMovement(this.dashDirection.scale(3.8));

        this.entityData.set(IS_DASHING, true);
        this.dashTicksLeft = 30; // 1.5s au lieu de 1s

        // Plus de gerbe de dégâts au déclenchement : elle frappait à 2,5 blocs devant, que l'orque
        // atteigne sa cible ou non. Le balayage par contact du tick handler s'en charge dès la
        // première image de course, et seulement si la bête touche vraiment quelque chose.
        this.dashHits.clear();

        // Particules & sons (inchangés)
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


    public void activateOrcaCall() {
        float damage = 15f + 10f * (this.getLevel() / 50f);
        AABB hitBox = this.getBoundingBox().inflate(12.0);
        this.level().getEntitiesOfClass(LivingEntity.class, hitBox).forEach(target -> {
            if (target != this && target != this.getFirstPassenger() && !this.isAlliedTo(target)) {
                target.hurt(this.damageSources().mobAttack(this), damage);
            }
        });

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                OWSounds.CROCODILE_MOUTH_CRUSH.get(), SoundSource.AMBIENT, 3.0f, 0.5f);
        orcaUltimateKillCount = 0;
    }

    public int getOrcaUltimateKillCount() {
        return orcaUltimateKillCount;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float v) {
        return super.hurt(damageSource, v);
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        orcaUltimateKillCount++;
        return super.killedEntity(serverLevel, entity);
    }

    /** Une orque hors de l'eau n'est pas une combattante, c'est une échouée. */
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
        if (!this.hasPassenger(passenger) || this.touchingUnloadedChunk()) return;

        int idx = this.getPassengers().indexOf(passenger);

        // Assises avancées de 0,45 : le cycle de nage, qui ne tournait plus sous un cavalier, berce à
        // nouveau le corps et emporte les places vers l'arrière. On reprend ce recul ici plutôt que
        // de brider l'animation.
        float seatZ, seatX;
        switch (idx) {
            case 1  -> { seatZ = -0.45f; seatX =  0.45f; }
            case 2  -> { seatZ = -0.45f; seatX = -0.45f; }
            default -> { seatZ =  1.1f;  seatX = 0f;     }
        }

        // Retouches de calage, sur la position de repos. Le mouvement des os, lui, est desormais
        // mesure sur la matrice plus bas : ces deux valeurs ne compensent plus rien, elles reglent
        // simplement ou l'on s'assoit. COMBO_SEAT_FORWARD peut donc revenir a 0 si l'assise animee
        // tombe juste.
        seatZ += SEAT_FORWARD;
        if (this.isCombo()) seatZ += COMBO_SEAT_FORWARD;

        final float s = this.getScale();
        double baseY = getBaseRiderYOffset(idx);

        // Le siege, exprime dans le repere du MODELE (blocs). Y descend, et l'avant de la bete est
        // vers les Z negatifs — d'ou les deux inversions.
        float lx =  (float) (seatX / s);
        float ly =  (float) (MODEL_ORIGIN_Y - baseY / s);
        float lz = -(float) (seatZ / s);

        // Ecart entre la pose animee et la pose de repos, mesure sur la matrice elle-meme.
        double dx = 0, dy = 0, dz = 0;
        org.joml.Matrix4f bones = this.boneMatrix;
        if (bones != null) {
            org.joml.Vector3f now = bones.transformPosition(new org.joml.Vector3f(lx, ly, lz));
            dx = now.x - (lx + REST_X / 16f);
            dy = now.y - (ly + REST_Y / 16f);
            dz = now.z - (lz + REST_Z / 16f);
        }

        // Retour au repere de l'entite : memes inversions qu'a l'aller, remises a l'echelle.
        double ex =  dx * s;
        double ey = -dy * s;
        double ez = -dz * s;

        Vec3 seatOffset = new Vec3(seatX + ex, ey, seatZ + ez)
                .yRot((float) Math.toRadians(-this.yBodyRot));

        double riderY = this.getY() + baseY + seatOffset.y;

        passenger.fallDistance = 0f;
        function.accept(passenger,
                this.getX() + seatOffset.x,
                riderY,
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

    /**
     * Durée de vie d'une animation de combo, en ticks.
     *
     * <p>Le geste dure 1,4286 s à vitesse 1, soit <b>28,6 ticks</b> : le minuteur, fixé à 28, le
     * coupait un tick avant sa dernière image — en plein élan de fin, là où la pose est la plus
     * éloignée du repos. D'où les saccades résiduelles.</p>
     *
     * <p>La marge est calée sur celle du crocodile, dont l'enchaînement sert de référence : son
     * animation de 1,8857 s lue à 1,35 dure 27,9 ticks pour un minuteur de 37, soit un tiers de
     * rabiot pendant lequel elle tient sa pose finale et se mélange au coup suivant. Même
     * proportion ici : 28,6 × 1,32 ≈ 38.</p>
     */
    private static final int COMBO_ANIM_TICKS = 38;

    private void setupComboAnimations() {
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, COMBO_ANIM_TICKS);
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, COMBO_ANIM_TICKS);
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, COMBO_ANIM_TICKS);
    }

    private void setupComboAnimation(int comboNumber, AnimationState animationState, int timer, int maxTimer) {
        timer = tickComboAnimation(comboNumber, animationState, timer, maxTimer, this.isCombo(comboNumber));

        switch (comboNumber) {
            case 1: attack1ComboTimer = timer; break;
            case 2: attack2ComboTimer = timer; break;
            case 3: attack3ComboTimer = timer; break;
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

    public float getRiderControlPitch() { return this.entityData.get(RIDER_CONTROL_PITCH); }

    public float getBodyYRot() { return bodyYRot; }
    public float getBodyYRot_passenger() { return bodyYRot_passenger; }
    public float getBodyZRot_passenger() { return bodyZRot_passenger; }
    public float getBodyXRot_passenger() { return bodyXRot_passenger; }

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

    /** Variante naturelle exposée sous forme générique (cf. {@code OWEntity}). */
    @Override
    public int getInitialTypeVariant() { return this.getInitialVariant().getId(); }

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
        tag.putInt("orcaUltimateKillCount", this.orcaUltimateKillCount);
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
        this.orcaUltimateKillCount = tag.getInt("orcaUltimateKillCount");

        if (this.getSkinIndex() != 0) { this.nbtRestoring = true; this.changeSkin(this.getSkinIndex(), false); this.nbtRestoring = false; }
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

                boolean hitCeiling = goingUp  && currentY >= maxY;
                boolean hitFloor   = !goingUp && currentY <= minY;

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
            double dist  = 4 + orca.getRandom().nextDouble() * 6;
            targetX = orca.getX() + Math.sin(angle) * dist;
            targetZ = orca.getZ() + Math.cos(angle) * dist;
            targetY = orca.getY();
        }

        private void pickHorizontalTarget() {
            isVerticalPhase = false;

            double angle = orca.getRandom().nextDouble() * Math.PI * 2;
            double dist  = 8 + orca.getRandom().nextDouble() * 12;
            targetX = orca.getX() + Math.sin(angle) * dist;
            targetZ = orca.getZ() + Math.cos(angle) * dist;

            double seaLevel = orca.level().getSeaLevel();
            double minY = seaLevel - orca.getMaxDepth() + 5;
            double maxY = seaLevel - 2;
            targetY = Mth.clamp(orca.getY() + (orca.getRandom().nextDouble() - 0.5) * 4, minY, maxY);
        }
    }
}