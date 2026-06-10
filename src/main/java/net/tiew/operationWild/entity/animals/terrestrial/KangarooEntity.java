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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class KangarooEntity extends OWEntity implements IOWEntity, IOWTamable, IOWRideable, PlayerRideableJumping {

    public static final double TAMING_EXPERIENCE = 65.0;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> IS_SPINNING = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_WEARING_BOXING_GLOVES = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> WHIRLWIND_COOLDOWN = SynchedEntityData.defineId(KangarooEntity.class, EntityDataSerializers.INT);

    private int spinTicks = 0;
    private int sinceLastDamage = 0;
    private int whirlwindSoundTimer = 0;

    // Derniere vitesse de monture connue avant la tornade : sert a freiner en douceur
    // (au lieu d'un arret net) quand on declenche la tornade en pleine course.
    private float spinStopSpeed = 0f;
    // Suit la fin du spin pour remettre la vitesse a zero (sinon on repart a l'ancienne allure).
    private boolean wasSpinningForSpeed = false;

    public static final int WHIRLWIND_OUTRO_TICKS = 10;

    public int clientSpinTicks = 0;
    public float clientAnimTimeMs = 0f;
    public float clientSpinSpeed = 1f;
    public int clientOutroTicks = 0;
    private boolean clientWasSpinning = false;

    public final AnimationState attack1Combo = new AnimationState();
    public final AnimationState attack2Combo = new AnimationState();
    public final AnimationState attack3Combo = new AnimationState();

    public int attack1ComboTimer = 0;
    public int attack2ComboTimer = 0;
    public int attack3ComboTimer = 0;

    public boolean fourthHitFired = false;

    public volatile float bodyAnimY = 0f;

    // ── Saut monture (PlayerRideableJumping) ──────────────────────────────
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
                .add(Attributes.JUMP_STRENGTH, 0.8D);
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
    }

    // ==================================================
    //             MÉTHODES HÉRITÉES OWEntity
    // ==================================================

    @Override
    public int getEntityColor() {
        return 0xd7b17d; // 0xA87B4F — brun kangourou
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
        return OWItems.TIGER_SADDLE.get(); // provisoire — à remplacer par KANGAROO_SADDLE
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.TIGER_TAMED_ADVANCEMENT; // provisoire — à remplacer par KANGAROO_TAMED_ADVANCEMENT
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
    public float getRotationSpeed() {
        return 0.225f;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.KANGAROO.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.TIGER_FOOD); // provisoire — à remplacer par KANGAROO_FOOD
    }

    @Override
    public float getScale() {
        return super.getScale() <= 0 ? 1f : super.getScale();
    }

    /** Hauteur d'assise du rider (au-dessus des pieds). Abaissée : le défaut OWEntity (0.75×h) était trop haut. */
    @Override
    protected double getBaseRiderYOffset() {
        return this.getBbHeight() * 0.625 * this.getScale();
    }

    /** Le rider suit verticalement le bone "body" (même principe que le Kodiak). */
    @Override
    protected float getRiderAnimYOffset() {
        return -bodyAnimY / 16.0f * this.getScale();
    }

    @Override
    public int getComboPauseDelay() {
        return getComboAttack() == 3 ? 22 : 2;
    }

    /** Pendant la Tornade de Poings, le kangourou est enraciné : aucune IA ni saut. */
    @Override
    protected boolean isImmobile() {
        return isSpinning() || super.isImmobile();
    }

    /** Pendant la tornade le kangourou s'immobilise, mais en freinant progressivement (pas d'arret net). */
    @Override
    public float getRiddenSpeedVehicle(Player player) {
        if (isSpinning()) {
            wasSpinningForSpeed = true;
            spinStopSpeed *= 0.65f;
            if (spinStopSpeed < 0.005f) spinStopSpeed = 0f;
            return spinStopSpeed;
        }
        if (wasSpinningForSpeed) {
            // Sortie de tornade : on repart de zero pour ne pas conserver l'allure d'avant.
            wasSpinningForSpeed = false;
            resetRiddenSpeed();
        }
        float speed = super.getRiddenSpeedVehicle(player);
        spinStopSpeed = speed; // memorise la vitesse courante pour un arret en douceur
        return speed;
    }

    /** Pendant la tornade, le corps suit le regard du rider pour pouvoir viser le cone frontal, même à l'arrêt. */
    @Override
    protected boolean forceRiderLookBodyRotation() {
        return isSpinning();
    }

    // ==================================================
    //         PASSIF — PATTES-RESSORT (Spring Step)
    // ==================================================

    /** Passif : les pattes puissantes du kangourou amortissent tout : aucun dégât de chute. */
    @Override
    protected int calculateFallDamage(float fallDistance, float multiplier) {
        return 0;
    }

    // ==================================================
    //          SAUT MONTURE (PlayerRideableJumping)
    // ==================================================

    /** Appelé chaque tick côté client contrôlant : gère l'atterrissage et déclenche le saut. */
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

    // ==================================================
    //                  CORPS / COMBO
    // ==================================================

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

        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);
    }

    // ==================================================
    //          TORNADE DE POINGS (attaque secondaire)
    // ==================================================

    public boolean isSpinning() { return this.entityData.get(IS_SPINNING); }
    private void setSpinning(boolean value) { this.entityData.set(IS_SPINNING, value); }

    public int getWhirlwindCooldownTicks() { return this.entityData.get(WHIRLWIND_COOLDOWN); }
    private void setWhirlwindCooldownTicks(int value) { this.entityData.set(WHIRLWIND_COOLDOWN, Math.max(0, value)); }

    /** Démarre la rotation (appelé côté serveur via le packet de maintien). */
    public void startWhirlwind() {
        if (this.level().isClientSide()) return;
        if (isSpinning()) return;
        // Pas de tornade dans l'eau ni en l'air : uniquement les pieds sur le sol.
        if (this.isInWater() || !this.onGround()) return;
        if (getWhirlwindCooldownTicks() > 0) return;
        if (getVitalEnergy() > getMaxVitalEnergy() - OWAttacksConstants.Kangaroo.WHIRLWIND_ENERGY) {
            canShowVitalEnergyLack = true;
            return;
        }
        setSpinning(true);
        spinTicks = 0;
        // Grand compteur pour que le 1er dégât tombe pile à l'entrée en phase offensive (0,5 s).
        sinceLastDamage = Integer.MAX_VALUE / 2;
        whirlwindSoundTimer = 0;
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.NEUTRAL, 1.1f, 0.6f);
    }

    /** Arrête la rotation. Le cooldown de 35 s n'est appliqué que si ≥ 3 s consécutives. */
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
        // ── Accumulateur client : avance l'animation à vitesse variable (1× → 5×) ──
        if (this.level().isClientSide()) {
            if (isSpinning()) {
                clientSpinTicks++;
                clientSpinSpeed = computeAnimSpeedMultiplier(clientSpinTicks);
                clientAnimTimeMs += 50f * clientSpinSpeed; // 50 ms par tick × vitesse
                clientWasSpinning = true;
            } else {
                // Front descendant (relâche) → déclenche l'outro (retour à la pose d'origine).
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

        // ── Serveur ────────────────────────────────────────────────────────────
        if (isSpinning()) {
            // Stop défensif : plus de pilote valide (démontage, perte du propriétaire…).
            Player rider = (getFirstPassenger() instanceof Player p) ? p : null;
            if (rider == null || (getOwnerUUID() != null && !getOwnerUUID().equals(rider.getUUID()))) {
                stopWhirlwind();
                return;
            }

            // Stop défensif : le kangourou est entré dans l'eau ou a quitté le sol (chute…).
            if (this.isInWater() || !this.onGround()) {
                stopWhirlwind();
                return;
            }

            spinTicks++;

            // Coups à cadence fixe (0,5 s) ; le MONTANT croît avec le temps (rien avant 0,5 s).
            if (spinTicks >= OWAttacksConstants.Kangaroo.WHIRLWIND_DAMAGE_START_TICKS) {
                sinceLastDamage++;
                if (sinceLastDamage >= OWAttacksConstants.Kangaroo.WHIRLWIND_DAMAGE_INTERVAL_TICKS) {
                    sinceLastDamage = 0;
                    dealWhirlwindDamage(computeWhirlwindDamage(spinTicks));
                }
            }

            // Bruitage « moteur » : cadence et hauteur qui montent avec la vitesse.
            float speedFactor = Mth.clamp((float) spinTicks / OWAttacksConstants.Kangaroo.WHIRLWIND_DAMAGE_PEAK_TICKS, 0f, 1f);
            int soundInterval = Math.max(2, (int) (8 - 6 * speedFactor));
            if (++whirlwindSoundTimer >= soundInterval) {
                whirlwindSoundTimer = 0;
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.NEUTRAL,
                        0.55f, 0.7f + 0.9f * speedFactor);
            }

            // Durée maximale : 15 s.
            if (spinTicks >= OWAttacksConstants.Kangaroo.WHIRLWIND_MAX_DURATION_TICKS) {
                stopWhirlwind();
            }
        } else if (getWhirlwindCooldownTicks() > 0) {
            setWhirlwindCooldownTicks(getWhirlwindCooldownTicks() - 1);
        }
    }

    /** Dégâts par coup : croissent de MIN (0,5 s) à MAX (3 s), puis restent au plafond. */
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

        // Direction « devant » du kangourou (à plat) : yaw 0 = +Z.
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
            // On ne touche QUE ce qui est devant (cône frontal) — pas les côtés ni le dos.
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

    /**
     * Multiplicateur de vitesse de lecture de l'animation (client) : 1× au départ, accélère
     * jusqu'au pic 5× à 3 s, puis reste à 5×. Courbe ease-in (« démarre pas très vite »).
     */
    private float computeAnimSpeedMultiplier(int ticks) {
        float f = Mth.clamp((float) ticks / OWAttacksConstants.Kangaroo.WHIRLWIND_DAMAGE_PEAK_TICKS, 0f, 1f);
        f = f * f;
        return 1f + 4f * f; // 1× → 5×
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
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof KangarooEntity otherKangaroo) {
            if (otherKangaroo.isBaby()) {
                return true;
            }
            if (this.isTame()) {
                return otherKangaroo.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherKangaroo.getOwnerUUID());
            } else {
                return !otherKangaroo.isTame();
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

    // ==================================================
    //                   ANIMATIONS
    // ==================================================

    private void setupAnimationState() {
        createIdleAnimation(80, true);
        createSitAnimation(80, true);

        setupComboAnimations();
    }

    private void setupComboAnimations() {
        setupComboAnimation(1, attack1Combo, attack1ComboTimer, (int) (24 / comboSpeedMultiplier));
        setupComboAnimation(2, attack2Combo, attack2ComboTimer, (int) (24 / comboSpeedMultiplier));
        setupComboAnimation(3, attack3Combo, attack3ComboTimer, 50);
    }

    private void setupComboAnimation(int comboNumber, AnimationState animationState, int timer, int maxTimer) {
        boolean shouldPlay = this.isCombo(comboNumber);
        if (comboNumber == 3 && fourthHitFired) shouldPlay = false;

        if (shouldPlay) {
            if (timer <= 0) {
                timer = maxTimer;
                animationState.start(this.tickCount);
            } else {
                --timer;
            }
        } else {
            if (timer > 0) {
                // Laisse l'AnimationState terminer sa course naturellement (combos 1-2-3).
                --timer;
            } else {
                timer = 0;
                animationState.stop();
            }
            if (!this.isCombo(comboNumber)) fourthHitFired = false;
        }

        switch (comboNumber) {
            case 1: attack1ComboTimer = timer; break;
            case 2: attack2ComboTimer = timer; break;
            case 3: attack3ComboTimer = timer; break;
        }
    }

    // ==================================================
    //               VARIANTES & SKINS
    // ==================================================

    public KangarooVariant getVariant() {
        return KangarooVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(KangarooVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
    }

    @Override
    public void setVariant(OWEntity entity, int variant) {
        if (entity instanceof KangarooEntity kangaroo) {
            kangaroo.setVariant(KangarooVariant.byId(variant));
            kangaroo.setInitialVariant(KangarooVariant.byId(variant));
        }
    }

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
        tag.putBoolean("isWearingBoxingGloves", this.isWearingBoxingGloves());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.entityData.set(IS_WEARING_BOXING_GLOVES, tag.getBoolean("isWearingBoxingGloves"));
        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
    }
}
