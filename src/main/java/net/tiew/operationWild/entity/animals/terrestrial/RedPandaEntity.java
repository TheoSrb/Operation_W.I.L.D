package net.tiew.operationWild.entity.animals.terrestrial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.attacks.OWPistePassives;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.goals.global.OWBreedGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.misc.HealOrbEntity;
import net.tiew.operationWild.entity.variants.RedPandaVariant;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static net.tiew.operationWild.core.OWUtils.RANDOM;

public class RedPandaEntity extends OWEntity implements IOWEntity, IOWTamable {

    public static final double TAMING_EXPERIENCE = 40.0;
    public static final int ENTITY_COLOR = 0xec8925;

    private static final double SHOULDER_SIDE_OFFSET = 0.44;
    private static final double SHOULDER_HEIGHT_OFFSET = 1.02;
    private static final double SHOULDER_FORWARD_OFFSET = -0.05;
    private static final double CROUCH_DROP = 0.28;
    private static final double CROUCH_FORWARD = 0.09;
    public static final float CROUCH_PITCH_DEGREES = 9f;
    private static final float CROUCH_BLEND = 0.35f;

    private static final int SHOULDER_TOGGLE_COOLDOWN = 10;
    /**
     * Delai avant qu'une bete tout juste descendue puisse s'endormir.
     *
     * <p>On repose son compagnon pour une raison — le faire garder un endroit, l'ecarter d'un
     * combat, lui faire suivre une piste. Le voir se rouler en boule dans la seconde annulait
     * l'intention du geste. Dix secondes suffisent a laisser la main au joueur.</p>
     */
    private static final int NAP_COOLDOWN_AFTER_DISMOUNT = 200;
    private static final int SHOULDER_RESTORE_TIMEOUT = 600;
    private static final double SHOULDER_RESTORE_RANGE = 8.0;

    private static final int MISC_IDLE_DURATION = 54;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(RedPandaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> THROW_TIMER = SynchedEntityData.defineId(RedPandaEntity.class, EntityDataSerializers.INT);
    public static final float HEAL_POWER_BASE = 1.0f;
    public static final float HEAL_POWER_ROLL_MIN = 0.9f;
    public static final float HEAL_POWER_ROLL_MAX = 1.1f;
    public static final int DEFAULT_SKIN_INDEX = 2;

    public static final float HEAL_POWER_MAX = 1.85f;
    public static final float HEAL_POWER_STEP = 0.02f;

    private static final EntityDataAccessor<Float> HEAL_POWER =
            SynchedEntityData.defineId(RedPandaEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> CLIMB_TIMER =
            SynchedEntityData.defineId(RedPandaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CLIMB_MOUNTING =
            SynchedEntityData.defineId(RedPandaEntity.class, EntityDataSerializers.BOOLEAN);

    public static final int CLIMB_TICKS = 20;
    /** Duree du raccord de reveil, calee sur celle que pose {@code createTransitionAnimation}. */
    private static final int WAKE_TRANSITION_TICKS = 20;

    private static final EntityDataAccessor<Integer> AURA_TIMER = SynchedEntityData.defineId(RedPandaEntity.class, EntityDataSerializers.INT);

    public final AnimationState napAnimationState = new AnimationState();
    public final AnimationState throwAnimationState = new AnimationState();
    /**
     * Etat propre au repos d'epaule.
     *
     * <p>Il ne peut pas partager {@code idleAnimationState} : {@code createIdleAnimation} le relance
     * toutes les quatre-vingts images de tick, ce qui tranchait net une animation de dix secondes et
     * la faisait repartir du debut. Celui-ci demarre une fois en montant sur l'epaule et court sans
     * interruption — la definition boucle d'elle-meme.</p>
     */
    public final AnimationState shoulderIdleAnimationState = new AnimationState();
    public final AnimationState miscIdleAnimationState = new AnimationState();

    public int napAnimationTimeout = 0;
    public int miscIdleAnimationStartTime = 0;

    private int healOrbCooldown = 0;
    private int twinOrbDelay = 0;
    private int twinOrbTargetId = -1;
    private int auraPulseTimer = 0;
    private double auraAnchorY = Double.NaN;
    private int shoulderCooldown = 0;
    private int napCooldown = 0;
    /**
     * Montee differee, le temps que la bete se reveille.
     *
     * <p>Cliquer sur un panda endormi le faisait bondir a l'epaule sans transition, l'animation de
     * reveil etant tranchee par celle d'escalade. Le clic ouvre donc le reveil, et la montee attend
     * que le raccord soit joue.</p>
     */
    private int wakeThenClimbDelay = 0;
    private int wakeThenClimbCarrierId = -1;

    private Vec3 climbStart = null;
    private int climbCarrierId = -1;

    /**
     * Ressort de la queue, cote client.
     *
     * <p>Porte par l'entite et non par le modele : une seule instance de modele sert TOUS les pandas
     * a l'ecran, et y ranger l'etat ferait battre leurs queues a l'unisson. Meme raison qui pousse
     * le rendu de drapeau a tenir une table indexee par porteur.</p>
     */
    public float tailSwing = 0f;
    public float tailSwingVelocity = 0f;
    public float tailLastAge = Float.NaN;

    /** Cabrage du saut et de la chute, sa vitesse, et l'ecrasement de la reception. */
    public float perchPitch = 0f;
    public float perchPitchVelocity = 0f;
    public float perchLandSquash = 0f;
    public float perchSquashVelocity = 0f;
    public float perchLastVerticalSpeed = 0f;

    /**
     * Lacet du porteur a l'image precedente, sa vitesse lissee, et le roulis qui en decoule.
     *
     * <p>Le lacet est mesure ici et nulle part ailleurs. {@code Entity.turn} ajoute le mouvement de
     * souris A LA FOIS a {@code yRot} et a {@code yRotO} : leur difference reste donc constante
     * pendant qu'on tourne le regard, et toute mesure fondee dessus rend zero.</p>
     */
    public float perchLastCarrierYaw = Float.NaN;
    public float perchTurnRate = 0f;
    public float perchRoll = 0f;
    /** Relevement de la queue du a l'allure du porteur, poursuivi mollement. */
    public float perchRunLift = 0f;
    public boolean perchWasAirborne = false;

    public float crouchAmount = 0f;
    public float crouchAmountO = 0f;
    private int miscIdleCooldown = (int) OWUtils.generateRandomInterval(300, 700);

    private UUID pendingCarrier = null;
    private int pendingCarrierTicks = 0;

    private final List<HealOverTime> healOverTime = new ArrayList<>();

    public RedPandaEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Sieste ouverte aux deux etats, apprivoise compris : c'est la seule espece du mod qui ne se
        // monte pas, donc la seule ou l'endormissement d'un compagnon ne prive de rien.
        this.goalSelector.addGoal(5, new net.tiew.operationWild.entity.goals.NapGoal(this, 1f, 800, true, true));
        this.goalSelector.addGoal(10, new OWBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(11, new OWRandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(THROW_TIMER, 0);
        builder.define(AURA_TIMER, 0);
        builder.define(HEAL_POWER, HEAL_POWER_BASE);
        builder.define(CLIMB_TIMER, 0);
        builder.define(CLIMB_MOUNTING, true);
    }

    public boolean isClimbing() { return this.entityData.get(CLIMB_TIMER) > 0; }

    public boolean isClimbingUp() { return this.entityData.get(CLIMB_MOUNTING); }

    public float climbProgress() {
        return 1f - this.entityData.get(CLIMB_TIMER) / (float) CLIMB_TICKS;
    }

    public float getHealPower() {
        return this.entityData.get(HEAL_POWER);
    }

    public void setHealPower(float power) {
        this.entityData.set(HEAL_POWER, Mth.clamp(power, HEAL_POWER_ROLL_MIN, HEAL_POWER_MAX));
    }

    public boolean upgradeHealPower() {
        float current = getHealPower();
        if (current >= HEAL_POWER_MAX) return false;
        setHealPower(current + HEAL_POWER_STEP);
        return true;
    }

    @Override
    public int getEntityColor() {
        return ENTITY_COLOR;
    }

    @Override
    public float getTheoreticalScale() {
        return 4f;
    }

    @Override
    public double napParticleHeight() { return 0.15; }

    @Override
    public double napParticleForward() { return 0.75; }

    @Override
    public double getTamingExperience() {
        return TAMING_EXPERIENCE;
    }

    @Override
    public OWEntityConfig.Archetypes getArchetype() {
        return OWEntityConfig.Archetypes.HEALER;
    }

    @Override
    public OWEntityConfig.Diet getDiet() {
        return OWEntityConfig.Diet.VEGETARIAN;
    }

    @Override
    public OWEntityConfig.Temperament getTemperament() {
        return OWEntityConfig.Temperament.PASSIVE;
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.RED_PANDA_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return 250f;
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 0.175f * (1 + ((float) this.getLevel() / 50));
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
    public float getRotationSpeed() {
        return 0.3f;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.RED_PANDA.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.RED_PANDA_FOOD);
    }

    @Override
    public float getScale() {
        return super.getScale() <= 0 ? 1f : super.getScale();
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (isNapping() || isSleeping()) return null;
        return RANDOM(2) ? SoundEvents.FOX_AMBIENT : SoundEvents.FOX_SNIFF;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.FOX_HURT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.FOX_DEATH;
    }

    private long lastStepSoundMs = 0L;

    @Override
    public void playStepSound(BlockPos blockPos, BlockState blockState) {
    }

    private void playStepSoundFromAnimation(float pitchMod) {
        if (!this.level().isClientSide()) return;
        if (!this.onGround()) return;
        if (this.isInWater()) return;
        if (this.isPassenger()) return;

        if (this.getDeltaMovement().horizontalDistanceSqr() < 0.0001) return;

        long now = System.currentTimeMillis();
        if (now - lastStepSoundMs < 200L) return;
        lastStepSoundMs = now;

        BlockState blockState = this.getBlockStateOn();
        if (blockState.isAir()) return;

        BlockPos pos = this.blockPosition();
        SoundType soundtype = blockState.getSoundType(this.level(), pos, this);

        for (int i = 0; i < 3; i++) {
            this.level().playLocalSound(
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    soundtype.getStepSound(),
                    this.getSoundSource(),
                    soundtype.getVolume() * 0.08F,
                    soundtype.getPitch() * pitchMod,
                    false
            );
        }
    }

    public void onLeftFootDown() {
        playStepSoundFromAnimation(1.35f);
    }

    public void onRightFootDown() {
        playStepSoundFromAnimation(1.55f);
    }

    @Override
    public void tick() {
        super.tick();

        if (healOrbCooldown > 0) healOrbCooldown--;
        if (shoulderCooldown > 0) shoulderCooldown--;
        if (napCooldown > 0) napCooldown--;

        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        if (!this.level().isClientSide()) {
            if (getThrowTimer() > 0) setThrowTimer(getThrowTimer() - 1);

            handleWakeThenClimb();
            handleShoulderClimb();
            handleLifeAura();
            handleTwinOrb();
            handleHealOverTime();
            handleShoulderRestore();

            if (isOnShoulder() && (!this.isTame() || this.isInResurrection())) dismountFromShoulder();
        }
    }

    private void handleShoulderRestore() {
        if (pendingCarrier == null) return;

        if (isOnShoulder() || ++pendingCarrierTicks > SHOULDER_RESTORE_TIMEOUT) {
            pendingCarrier = null;
            pendingCarrierTicks = 0;
            return;
        }

        Player carrier = this.level().getPlayerByUUID(pendingCarrier);
        if (carrier == null || carrier.isRemoved()) return;
        if (this.distanceTo(carrier) > SHOULDER_RESTORE_RANGE) return;
        if (!carrier.getPassengers().isEmpty()) return;

        pendingCarrier = null;
        pendingCarrierTicks = 0;
        climbOnShoulder(carrier);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide()) return super.mobInteract(player, hand);
        if (hand != InteractionHand.MAIN_HAND) return super.mobInteract(player, hand);
        if (!this.isTame() || this.isBaby() || this.isInResurrection()) return super.mobInteract(player, hand);

        ItemStack stack = player.getItemInHand(hand);
        if (isFood(stack) && this.getHealth() < this.getMaxHealth()) return super.mobInteract(player, hand);

        if (isOnShoulder()) {
            if (!player.isSteppingCarefully()) return InteractionResult.PASS;
            if (this.getVehicle() != player) return InteractionResult.PASS;
            dismountFromShoulder();
            return InteractionResult.SUCCESS;
        }

        if (player.isSteppingCarefully()) return super.mobInteract(player, hand);
        if (shoulderCooldown > 0 || isClimbing() || wakeThenClimbDelay > 0) return InteractionResult.PASS;
        if (!this.hasTribePermission(player, net.tiew.operationWild.team.OWTribePermission.CONTROL)) {
            return InteractionResult.PASS;
        }
        if (!player.getPassengers().isEmpty()) return InteractionResult.PASS;

        if (isNapping() || isSleeping()) {
            setNap(false);
            setSleeping(false);
            wakeThenClimbDelay = WAKE_TRANSITION_TICKS;
            wakeThenClimbCarrierId = player.getId();
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.FOX_SNIFF, SoundSource.NEUTRAL, 0.6f,
                    (float) OWUtils.generateRandomInterval(0.8, 1.0));
            return InteractionResult.SUCCESS;
        }

        return beginClimb(player, true) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private boolean climbOnShoulder(Player player) {
        this.setSitting(false);
        this.getNavigation().stop();
        if (!this.startRiding(player, true)) return false;

        syncPassengersToCarrier(player);

        shoulderCooldown = SHOULDER_TOGGLE_COOLDOWN;
        this.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.NEUTRAL, 0.8f,
                (float) OWUtils.generateRandomInterval(1.2, 1.4));
        return true;
    }

    public void dismountFromShoulder() {
        Entity vehicle = this.getVehicle();
        this.stopRiding();
        shoulderCooldown = SHOULDER_TOGGLE_COOLDOWN;
        napCooldown = NAP_COOLDOWN_AFTER_DISMOUNT;

        if (vehicle instanceof Player player) {
            syncPassengersToCarrier(player);
            beginClimb(player, false);
            return;
        }

        if (vehicle != null) this.setPos(vehicle.getX(), vehicle.getY(), vehicle.getZ());
    }

    private boolean beginClimb(Player carrier, boolean mounting) {
        if (this.level().isClientSide()) return false;

        this.setSitting(false);
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.noPhysics = true;

        this.climbStart = this.position();
        this.climbCarrierId = carrier.getId();
        this.entityData.set(CLIMB_MOUNTING, mounting);
        this.entityData.set(CLIMB_TIMER, CLIMB_TICKS);
        shoulderCooldown = SHOULDER_TOGGLE_COOLDOWN + CLIMB_TICKS;

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                mounting ? SoundEvents.FOX_AMBIENT : SoundEvents.FOX_SNIFF, SoundSource.NEUTRAL, 0.7f,
                (float) OWUtils.generateRandomInterval(1.2, 1.45));
        return true;
    }

    private void handleWakeThenClimb() {
        if (wakeThenClimbDelay <= 0) return;
        if (--wakeThenClimbDelay > 0) return;

        int carrierId = wakeThenClimbCarrierId;
        wakeThenClimbCarrierId = -1;

        // Le porteur a pu s'eloigner, mourir ou prendre un autre passager pendant le reveil : la
        // bete reste alors simplement debout, ce qui est un aboutissement acceptable.
        if (!(this.level().getEntity(carrierId) instanceof Player carrier) || !carrier.isAlive()) return;
        if (!carrier.getPassengers().isEmpty()) return;
        if (this.distanceTo(carrier) > SHOULDER_RESTORE_RANGE) return;

        beginClimb(carrier, true);
    }

    private void handleShoulderClimb() {
        int remaining = this.entityData.get(CLIMB_TIMER);
        if (remaining <= 0) return;

        if (!(this.level().getEntity(climbCarrierId) instanceof Player carrier) || !carrier.isAlive()) {
            abortClimb();
            return;
        }

        this.entityData.set(CLIMB_TIMER, --remaining);
        float progress = 1f - remaining / (float) CLIMB_TICKS;

        boolean mounting = this.entityData.get(CLIMB_MOUNTING);
        Vec3 shoulder = carrier.position().add(
                shoulderOffset(carrier.yBodyRot, this.getScale(), this.crouchAmount));
        Vec3 ground = carrier.position().add(
                shoulderOffset(carrier.yBodyRot, this.getScale(), 0f).multiply(1.6, 0, 1.6));

        Vec3 from = mounting ? climbStart : shoulder;
        Vec3 to = mounting ? shoulder : ground;
        this.setPos(climbCurve(from, to, progress));

        double dx = carrier.getX() - this.getX(), dz = carrier.getZ() - this.getZ();
        if (dx * dx + dz * dz > 1.0e-4) {
            this.setYRot((float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f);
            this.yBodyRot = this.getYRot();
            this.setYHeadRot(this.getYRot());
        }

        if (remaining > 0) return;

        this.noPhysics = false;
        this.climbStart = null;
        this.climbCarrierId = -1;

        if (mounting && this.startRiding(carrier, true)) {
            syncPassengersToCarrier(carrier);
            this.level().playSound(null, carrier.getX(), carrier.getY(), carrier.getZ(),
                    SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.NEUTRAL, 0.8f,
                    (float) OWUtils.generateRandomInterval(1.2, 1.4));
        }
    }

    private void abortClimb() {
        this.entityData.set(CLIMB_TIMER, 0);
        this.noPhysics = false;
        this.climbStart = null;
        this.climbCarrierId = -1;
    }

    private static Vec3 climbCurve(Vec3 from, Vec3 to, float progress) {
        double approach = 1 - Math.pow(1 - progress, 3);
        double x = Mth.lerp(approach, from.x, to.x);
        double z = Mth.lerp(approach, from.z, to.z);

        double rise = Mth.clamp((progress - 0.15) / 0.85, 0, 1);
        double eased = rise < 0.5 ? 2 * rise * rise : 1 - Math.pow(-2 * rise + 2, 2) / 2;
        double hop = Math.sin(rise * Math.PI) * 0.22;

        return new Vec3(x, Mth.lerp(eased, from.y, to.y) + hop, z);
    }

    private static void syncPassengersToCarrier(Player carrier) {
        if (carrier instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetPassengersPacket(serverPlayer));
        }
    }

    @Override
    public boolean canStartNap() {
        // Le delai court a partir de la descente, mais la garde couvre aussi le trajet lui-meme :
        // une bete en pleine escalade n'a rien a faire endormie.
        return napCooldown <= 0 && !isClimbing() && !isOnShoulder();
    }

    public boolean isOnShoulder() {
        return this.getVehicle() instanceof Player;
    }

    public static @Nullable RedPandaEntity getShoulderPanda(@Nullable Player player) {
        if (player == null) return null;
        for (Entity passenger : player.getPassengers()) {
            if (passenger instanceof RedPandaEntity redPanda) return redPanda;
        }
        return null;
    }

    public static @Nullable OWEntity resolveControlledEntity(@Nullable Player player) {
        if (player == null) return null;
        if (player.getRootVehicle() instanceof OWEntity mount) return mount;
        return getShoulderPanda(player);
    }

    public @Nullable Player getCarrier() {
        return this.getVehicle() instanceof Player player ? player : null;
    }

    public static Vec3 shoulderOffset(float yawDegrees, double scale, float crouch) {
        float yaw = yawDegrees * Mth.DEG_TO_RAD;
        double rightX = -Mth.cos(yaw);
        double rightZ = -Mth.sin(yaw);
        double forwardX = -Mth.sin(yaw);
        double forwardZ = Mth.cos(yaw);

        double side = SHOULDER_SIDE_OFFSET * scale;
        double forward = SHOULDER_FORWARD_OFFSET * scale + CROUCH_FORWARD * crouch;

        return new Vec3(
                rightX * side + forwardX * forward,
                SHOULDER_HEIGHT_OFFSET - CROUCH_DROP * crouch,
                rightZ * side + forwardZ * forward);
    }

    @Override
    public void rideTick() {
        super.rideTick();

        Player carrier = getCarrier();
        if (carrier == null) return;

        this.getNavigation().stop();

        this.crouchAmountO = this.crouchAmount;
        this.crouchAmount += ((carrier.isCrouching() ? 1f : 0f) - this.crouchAmount) * CROUCH_BLEND;

        Vec3 offset = shoulderOffset(carrier.yBodyRot, this.getScale(), this.crouchAmount);
        this.setPos(carrier.getX() + offset.x, carrier.getY() + offset.y, carrier.getZ() + offset.z);
        this.setYRot(carrier.yBodyRot);
        this.yBodyRot = carrier.yBodyRot;
        this.yHeadRot = carrier.yHeadRot;
        this.setYHeadRot(carrier.yHeadRot);
        this.fallDistance = 0f;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (isOnShoulder() && isShieldedWhileCarried(damageSource)) return false;
        return super.hurt(damageSource, amount);
    }

    /**
     * Seule la melee est ecartee.
     *
     * <p>Tout le reste porte : noyade, etouffement, chute, feu, potion, foudre, explosion. Un
     * passager partage le sort de son porteur, et c'est ce qui donne du poids au fait de l'emmener
     * partout — mais il n'a pas a encaisser les coups qui visent celui-ci.</p>
     */
    private boolean isShieldedWhileCarried(DamageSource source) {
        // Une explosion de creeper et une fleche portent toutes deux un tireur vivant : sans ces deux
        // ecarts, le test « quelque chose de vivant l'a touche » les aurait prises pour de la melee.
        if (source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.IS_PROJECTILE)) return false;
        return source.getDirectEntity() instanceof LivingEntity;
    }

    @Override
    public boolean startRiding(Entity vehicle, boolean force) {
        if (vehicle instanceof Player && !force) return false;
        return super.startRiding(vehicle, force);
    }

    @Override
    protected boolean canRide(Entity vehicle) {
        return vehicle instanceof Player || super.canRide(vehicle);
    }

    @Override
    public boolean isPushable() {
        return !isOnShoulder() && super.isPushable();
    }

    @Override
    public boolean isPickable() {
        return !isOnShoulder() && super.isPickable();
    }

    @Override
    public boolean canBeHitByProjectile() {
        return this.isAlive();
    }

    @Override
    public void die(DamageSource damageSource) {
        if (isOnShoulder()) this.stopRiding();
        super.die(damageSource);
    }

    @Override
    public int arenaTerrainMask() {
        return net.tiew.operationWild.core.OWArena.Terrain.TERRESTRIAL.bit();
    }

    public void throwHealOrb() {
        if (this.level().isClientSide()) return;
        if (healOrbCooldown > 0) return;

        Player carrier = getCarrier();
        if (carrier != null && carrier.isSteppingCarefully()) return;
        if (carrier != null && carrier.getVehicle() != null) return;

        LivingEntity aimer = carrier != null ? carrier : this.getOwner();
        if (aimer == null) return;

        LivingEntity target = pickHealTarget(aimer);
        if (target == null) {
            playAimFailure(aimer);
            return;
        }

        float cost = OWAttacksConstants.RedPanda.HEAL_ORB_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            return;
        }
        setVitalEnergy(getVitalEnergy() + cost);
        healOrbCooldown = OWAttacksConstants.RedPanda.HEAL_ORB_COOLDOWN_TICKS;
        setThrowTimer(OWAttacksConstants.RedPanda.HEAL_ORB_THROW_TICKS);

        if (OWPistePassives.has(this, OWPistePassives.TWIN_ORB)) {
            twinOrbDelay = OWPistePassives.TWIN_ORB_DELAY_TICKS;
            twinOrbTargetId = target.getId();
        }

        launchHealOrb(target);
    }

    private void launchHealOrb(LivingEntity target) {
        HealOrbEntity orb = new HealOrbEntity(this.level(), this, target);
        orb.setPos(this.getX(), this.getEyeY() - 0.1, this.getZ());
        orb.aimAt(target);
        this.level().addFreshEntity(orb);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SPLASH_POTION_THROW, SoundSource.NEUTRAL, 0.8f,
                (float) OWUtils.generateRandomInterval(1.3, 1.5));
    }

    private void playAimFailure(LivingEntity aimer) {
        this.level().playSound(null, aimer.getX(), aimer.getY(), aimer.getZ(),
                SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), SoundSource.NEUTRAL, 0.5f, 0.6f);
    }

    public @Nullable LivingEntity pickHealTarget(LivingEntity aimer) {
        double range = OWAttacksConstants.RedPanda.HEAL_ORB_RANGE;
        double tolerance = OWAttacksConstants.RedPanda.HEAL_ORB_AIM_TOLERANCE;
        double minDistance = OWAttacksConstants.RedPanda.HEAL_ORB_MIN_AIM_DISTANCE;

        Vec3 eye = aimer.getEyePosition();
        Vec3 look = aimer.getLookAngle().normalize();

        AABB box = aimer.getBoundingBox().inflate(range);
        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;

        for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class, box)) {
            if (candidate == this || candidate == aimer || !candidate.isAlive()) continue;
            if (!isHealAlly(candidate)) continue;

            Vec3 center = candidate.position().add(0, candidate.getBbHeight() * 0.5, 0);
            Vec3 delta = center.subtract(eye);
            double along = delta.dot(look);
            if (along < minDistance || along > range) continue;

            double offset = delta.subtract(look.scale(along)).length() - candidate.getBbWidth() * 0.5;
            if (offset > tolerance) continue;

            double score = offset * 4 + along * 0.05;
            if (score < bestScore) {
                bestScore = score;
                best = candidate;
            }
        }

        if (best == null && isHealAlly(aimer) && aimer.getHealth() < aimer.getMaxHealth()) return aimer;

        return best;
    }

    public boolean isHealAlly(LivingEntity candidate) {
        if (candidate == this.getOwner()) return true;
        if (this.isTameGrabAlly(candidate)) return true;
        return candidate instanceof TamableAnimal tamed && tamed.isOwnedBy(this.getOwner());
    }

    private void handleTwinOrb() {
        if (twinOrbDelay <= 0) return;
        if (--twinOrbDelay > 0) return;

        Entity found = this.level().getEntity(twinOrbTargetId);
        twinOrbTargetId = -1;
        if (!(found instanceof LivingEntity target) || !target.isAlive()) return;

        launchHealOrb(target);
        setThrowTimer(OWAttacksConstants.RedPanda.HEAL_ORB_THROW_TICKS);
    }

    public void applyOrbHeal(LivingEntity target) {
        if (this.level().isClientSide() || target == null || !target.isAlive()) return;

        float max = target.getMaxHealth();
        float power = getHealPower();
        target.heal(max * OWAttacksConstants.RedPanda.HEAL_ORB_INSTANT_RATIO * power);

        int pulses = OWAttacksConstants.RedPanda.HEAL_ORB_OVER_TIME_TICKS
                / OWAttacksConstants.RedPanda.HEAL_ORB_OVER_TIME_INTERVAL;
        float perPulse = (max * OWAttacksConstants.RedPanda.HEAL_ORB_OVER_TIME_RATIO * power) / pulses;

        healOverTime.removeIf(entry -> entry.targetId == target.getId());
        healOverTime.add(new HealOverTime(target.getId(), pulses, perPulse));

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HEART,
                    target.getX(), target.getY() + target.getBbHeight() * 0.9, target.getZ(),
                    4, 0.3, 0.2, 0.3, 0.02);
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    24, target.getBbWidth() * 0.6, target.getBbHeight() * 0.5, target.getBbWidth() * 0.6, 0.1);
        }

        this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 0.9f,
                (float) OWUtils.generateRandomInterval(1.1, 1.3));
        this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 0.6f,
                (float) OWUtils.generateRandomInterval(1.2, 1.5));
    }

    private void handleHealOverTime() {
        if (healOverTime.isEmpty()) return;
        if (this.tickCount % OWAttacksConstants.RedPanda.HEAL_ORB_OVER_TIME_INTERVAL != 0) return;

        Iterator<HealOverTime> iterator = healOverTime.iterator();
        while (iterator.hasNext()) {
            HealOverTime entry = iterator.next();
            Entity found = this.level().getEntity(entry.targetId);

            if (!(found instanceof LivingEntity target) || !target.isAlive()) {
                iterator.remove();
                continue;
            }

            target.heal(entry.amountPerPulse);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.COMPOSTER,
                        target.getX(), target.getY() + target.getBbHeight() * 0.4, target.getZ(),
                        3, target.getBbWidth() * 0.5, target.getBbHeight() * 0.4, target.getBbWidth() * 0.5, 0.01);
            }

            if (--entry.remainingPulses <= 0) iterator.remove();
        }
    }

    public boolean activateLifeAura() {
        if (this.level().isClientSide()) return false;
        if (isAuraActive()) return false;

        Player carrier = getCarrier();
        if (carrier != null && carrier.getVehicle() != null) return false;

        float cost = OWAttacksConstants.RedPanda.LIFE_AURA_ENERGY;
        if (getVitalEnergy() > getVitalEnergyCapacity() - cost) {
            canShowVitalEnergyLack = true;
            return false;
        }
        setVitalEnergy(getVitalEnergy() + cost);

        setAuraTimer(auraDurationTicks());
        auraPulseTimer = 0;
        auraAnchorY = auraFeetY();

        OWUtils.spawnServerParticles(this, ParticleTypes.FLASH, 0, 0.5, 0, 1, 0);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.COMPOSTER,
                    this.getX(), this.getY() + 0.4, this.getZ(), 60, 0.5, 0.5, 0.5, 0.35);
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    this.getX(), this.getY() + 0.4, this.getZ(), 25, 0.3, 0.4, 0.3, 0.12);
            serverLevel.sendParticles(ParticleTypes.HEART,
                    this.getX(), this.getY() + 1.0, this.getZ(), 8, 0.6, 0.4, 0.6, 0.02);
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.NEUTRAL, 1.2f, 1.4f);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.NEUTRAL, 1.4f, 0.7f);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.0f, 1.6f);
        return true;
    }

    public void cancelLifeAura() {
        setAuraTimer(0);
        auraPulseTimer = 0;
    }

    private void handleLifeAura() {
        if (!isAuraActive()) return;

        setAuraTimer(getAuraTimer() - 1);

        double feet = auraFeetY();
        auraAnchorY = Double.isNaN(auraAnchorY) ? feet : auraAnchorY + (feet - auraAnchorY) * 0.1;

        spawnAuraWaves();
        spawnAuraColumn();

        if (++auraPulseTimer >= OWAttacksConstants.RedPanda.LIFE_AURA_PULSE_INTERVAL) {
            auraPulseTimer = 0;
            pulseLifeAura();
        }

        if (getAuraTimer() <= 0) {
            cancelLifeAura();
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.NEUTRAL, 0.9f, 1.5f);
        }
    }

    private int auraDurationTicks() {
        int base = OWAttacksConstants.RedPanda.LIFE_AURA_DURATION_TICKS;
        return OWPistePassives.has(this, OWPistePassives.WIDE_AURA)
                ? Math.round(base * OWPistePassives.WIDE_AURA_DURATION_FACTOR)
                : base;
    }

    private double auraRadius() {
        double base = OWAttacksConstants.RedPanda.LIFE_AURA_RADIUS;
        return OWPistePassives.has(this, OWPistePassives.WIDE_AURA)
                ? base * OWPistePassives.WIDE_AURA_RADIUS_FACTOR
                : base;
    }

    private void pulseLifeAura() {
        double radius = auraRadius();
        AABB box = this.getBoundingBox().inflate(radius);

        for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class, box)) {
            if (!candidate.isAlive() || candidate == this) continue;
            if (this.distanceTo(candidate) > radius) continue;
            if (!isHealAlly(candidate)) continue;
            if (candidate.getHealth() >= candidate.getMaxHealth()) continue;

            // Montant FIXE, seul soin du panda a echapper a la Puissance de Soin.
            //
            // L'aura arrose tout un cercle pendant huit secondes : la moindre multiplication y pese
            // huit fois, sur tout le monde a la fois. A pleine statistique elle rendait pres de six
            // points par seconde et par bete, soit le double de ce qu'elle doit valoir. Le stat
            // garde son emprise sur l'orbe, ou il porte sur une cible et une seule.
            candidate.heal(OWAttacksConstants.RedPanda.LIFE_AURA_HEAL_PER_PULSE);

            if (this.level() instanceof ServerLevel serverLevel) {
                spawnAuraLink(serverLevel, candidate);
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        candidate.getX(), candidate.getY() + candidate.getBbHeight() * 0.5, candidate.getZ(),
                        10, candidate.getBbWidth() * 0.5, candidate.getBbHeight() * 0.6, candidate.getBbWidth() * 0.5, 0.08);
                serverLevel.sendParticles(ParticleTypes.HEART,
                        candidate.getX(), candidate.getY() + candidate.getBbHeight() + 0.25, candidate.getZ(),
                        2, 0.2, 0.1, 0.2, 0.01);
            }
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 0.7f,
                (float) OWUtils.generateRandomInterval(0.9, 1.2));
    }

    private void spawnAuraWaves() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        double maxRadius = auraRadius();
        int travel = OWAttacksConstants.RedPanda.LIFE_AURA_WAVE_TRAVEL_TICKS;
        int elapsed = auraDurationTicks() - getAuraTimer();

        for (int wave = 0; wave < OWAttacksConstants.RedPanda.LIFE_AURA_WAVE_COUNT; wave++) {
            int offset = wave * OWAttacksConstants.RedPanda.LIFE_AURA_WAVE_INTERVAL;
            double progress = ((elapsed + offset) % travel) / (double) travel;

            double radius = progress * maxRadius;
            if (radius < 0.35) continue;

            double y = auraAnchorY + 0.05 + progress * OWAttacksConstants.RedPanda.LIFE_AURA_WAVE_LIFT;

            int points = Math.max(10, (int) (radius * 7));
            double spin = elapsed * 0.06;

            for (int i = 0; i < points; i++) {
                double angle = spin + (Math.PI * 2 * i) / points;
                double x = this.getX() + Math.cos(angle) * radius;
                double z = this.getZ() + Math.sin(angle) * radius;

                serverLevel.sendParticles(WAVE_DUST, x, y, z, 1, 0, 0, 0, 0);

                if (i % 5 == 0) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD, x, y + 0.1, z, 1, 0.02, 0.01, 0.02, 0.004);
                }
            }
        }
    }

    private static final net.minecraft.core.particles.DustParticleOptions WAVE_DUST =
            new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.25f, 1.0f, 0.42f), 1.1f);

    private double auraFeetY() {
        Player carrier = getCarrier();
        return carrier != null ? carrier.getY() : this.getY();
    }

    private void spawnAuraColumn() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        double height = OWAttacksConstants.RedPanda.LIFE_AURA_COLUMN_HEIGHT;
        double base = isOnShoulder() ? this.getY() - 0.6 : this.getY();

        for (int strand = 0; strand < 3; strand++) {
            double t = ((this.tickCount * 0.09) + strand / 3.0) % 1.0;
            double angle = this.tickCount * 0.35 + strand * (Math.PI * 2 / 3);
            double ray = 0.55 * (1.0 - t * 0.7);

            serverLevel.sendParticles(ParticleTypes.COMPOSTER,
                    this.getX() + Math.cos(angle) * ray,
                    base + t * height,
                    this.getZ() + Math.sin(angle) * ray,
                    1, 0, 0.01, 0, 0.0);
        }
    }

    private void spawnAuraLink(ServerLevel serverLevel, LivingEntity target) {
        double sourceY = this.getY() + this.getBbHeight() * 0.6;
        double targetY = target.getY() + target.getBbHeight() * 0.6;
        int points = OWAttacksConstants.RedPanda.LIFE_AURA_LINK_POINTS;

        for (int i = 1; i <= points; i++) {
            double t = i / (double) (points + 1);
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    Mth.lerp(t, this.getX(), target.getX()),
                    Mth.lerp(t, sourceY, targetY) + Math.sin(t * Math.PI) * 0.45,
                    Mth.lerp(t, this.getZ(), target.getZ()),
                    1, 0.02, 0.02, 0.02, 0.0);
        }
    }

    private void setupAnimationState() {
        if (isOnShoulder()) this.shoulderIdleAnimationState.startIfStopped(this.tickCount);
        else this.shoulderIdleAnimationState.stop();

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

        if (getThrowTimer() > 0) {
            this.throwAnimationState.startIfStopped(this.tickCount);
        } else {
            this.throwAnimationState.stop();
        }

        if (isAuraActive()) {
        } else {
        }
    }

    public boolean canPlayIdleAnimation() {
        return this.getTarget() == null && !this.isNapping() && !this.isMoving()
                && !this.isPassenger() && !this.isInWater();
    }

    public boolean isAnyIdleAnimationPlaying() {
        return this.miscIdleAnimationState.isStarted();
    }

    protected void handleMiscIdleAnimations() {
        if (this.miscIdleAnimationState.isStarted()
                && this.tickCount - miscIdleAnimationStartTime > MISC_IDLE_DURATION) {
            this.miscIdleAnimationState.stop();
        }

        if (miscIdleCooldown > 0) {
            miscIdleCooldown--;
            return;
        }

        if (this.canPlayIdleAnimation() && !this.isAnyIdleAnimationPlaying()) {
            this.miscIdleAnimationState.start(this.tickCount);
            miscIdleAnimationStartTime = this.tickCount;
        }

        miscIdleCooldown = (int) OWUtils.generateRandomInterval(300, 700);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));

            this.setHealPower((float) OWUtils.generateRandomInterval(HEAL_POWER_ROLL_MIN, HEAL_POWER_ROLL_MAX));

            this.setVariant(chooseRedPandaVariant());
            this.setInitialVariant(this.getVariant());
        }
        this.foodWanted = (int) OWUtils.generateRandomInterval(6, 10);
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (OWEntity.NAPPING.equals(accessor) && !isNapping() && !isSleeping() && this.level().isClientSide()) {
            napAnimationTimeout = 0;
            napAnimationState.stop();
        }
    }

    private RedPandaVariant chooseRedPandaVariant() {
        int roll = this.random.nextInt(100);

        if (roll < 15) return RedPandaVariant.BROWN;
        if (roll < 40) return RedPandaVariant.RED;
        return RedPandaVariant.DEFAULT;
    }

    @Override
    public void setVariant(OWEntity entity, int variant) {
        if (entity instanceof RedPandaEntity redPanda) {
            redPanda.setVariant(RedPandaVariant.byId(variant));
            redPanda.setInitialVariant(RedPandaVariant.byId(variant));
        }
    }

    public RedPandaVariant getVariant() {
        return RedPandaVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(RedPandaVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
    }

    public void setSkin(RedPandaVariant skin) {
        this.setVariant(skin);
    }

    @Override
    public void changeSkin(int skinIndex, boolean playingEffects) {
        super.changeSkin(skinIndex, playingEffects);
        this.setVariant(getInitialVariant());

        switch (skinIndex) {
            case 1 -> this.setSkin(RedPandaVariant.Cosmetics.GOLD.variant);
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

    public RedPandaVariant getInitialVariant() {
        return RedPandaVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(RedPandaVariant variant) {
        this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());
    }

    public int getThrowTimer() { return this.entityData.get(THROW_TIMER); }

    public void setThrowTimer(int timer) { this.entityData.set(THROW_TIMER, timer); }

    public int getAuraTimer() { return this.entityData.get(AURA_TIMER); }

    public void setAuraTimer(int timer) { this.entityData.set(AURA_TIMER, Math.max(0, timer)); }

    public boolean isAuraActive() { return getAuraTimer() > 0; }

    public int getHealOrbCooldownTicks() { return healOrbCooldown; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("foodGiven", this.foodGiven);
        tag.putInt("foodWanted", this.foodWanted);
        tag.putFloat("healPower", this.getHealPower());

        Player carrier = getCarrier();
        if (carrier != null) tag.putUUID("shoulderCarrier", carrier.getUUID());
        else if (pendingCarrier != null) tag.putUUID("shoulderCarrier", pendingCarrier);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
        if (tag.contains("healPower")) setHealPower(tag.getFloat("healPower"));
        this.pendingCarrier = tag.hasUUID("shoulderCarrier") ? tag.getUUID("shoulderCarrier") : null;
        this.pendingCarrierTicks = 0;
        if (this.getSkinIndex() != 0) { this.nbtRestoring = true; this.changeSkin(this.getSkinIndex(), false); this.nbtRestoring = false; }
    }

    @Override
    protected int getDefaultSkinIndex() { return DEFAULT_SKIN_INDEX; }

    private static final class HealOverTime {
        private final int targetId;
        private int remainingPulses;
        private final float amountPerPulse;

        private HealOverTime(int targetId, int remainingPulses, float amountPerPulse) {
            this.targetId = targetId;
            this.remainingPulses = remainingPulses;
            this.amountPerPulse = amountPerPulse;
        }
    }
}
