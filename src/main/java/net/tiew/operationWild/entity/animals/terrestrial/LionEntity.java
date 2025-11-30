package net.tiew.operationWild.entity.animals.terrestrial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.advancements.OWAdvancements;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.behavior.lion.LionBehaviorHandler;
import net.tiew.operationWild.entity.behavior.lion.LionClan;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWRideable;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.goals.global.OWAttackGoal;
import net.tiew.operationWild.entity.goals.global.OWBreedGoal;
import net.tiew.operationWild.entity.goals.global.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.goals.lion.LionAttackGoal;
import net.tiew.operationWild.entity.goals.lion.LionessFollowAlphaGoal;
import net.tiew.operationWild.entity.taming.TamingLion;
import net.tiew.operationWild.entity.variants.LionVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.AnimalSoulItem;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.LionClanSyncPacket;
import net.tiew.operationWild.networking.packets.to_server.LionHealLionessesPacket;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.tiew.operationWild.core.OWUtils.RANDOM;

public class LionEntity extends OWEntity implements IOWEntity, IOWTamable, IOWRideable {

    public static final double TAMING_EXPERIENCE = 95.0;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_MAD = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_ROAR = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> CLAN_COLOR = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);

    public LionBehaviorHandler lionBehaviorHandler;
    public TamingLion lionTaming;

    public static int clanId = 0;
    public int myClanId = -1;

    public boolean needsClanReconstruction = false;
    private int clanReconstructionTimer = 0;

    public LionClan clan = null;
    private LionEntity male = null;

    private int roarTimer = 0;
    private int roarCooldown = 0;
    private static final int ROAR_COOLDOWN_MAX = 800;

    public final AnimationState attack1Combo = new AnimationState();
    public final AnimationState attack2Combo = new AnimationState();
    public final AnimationState attack3Combo = new AnimationState();
    public final AnimationState roarAnimationState = new AnimationState();

    public int attack1ComboTimer = 0;
    public int attack2ComboTimer = 0;
    public int attack3ComboTimer = 0;
    public int roarAnimationTimer = 0;

    public LionEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
        initLionBehaviorAndTaming();
    }

    private void initLionBehaviorAndTaming() {
        this.lionBehaviorHandler = new LionBehaviorHandler(this);
        this.lionTaming = new TamingLion(this, lionBehaviorHandler);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.19D)
                .add(Attributes.FOLLOW_RANGE, 27.5D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        initLionBehaviorAndTaming(); // Create the AI before the goals, otherwise, null error

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new LionAttackGoal(this, this.getSpeed() * (this.isMale() ? 20.0f : 23.5f), 8, 3, false));
        this.goalSelector.addGoal(1, new LionessFollowAlphaGoal(this));
        this.goalSelector.addGoal(3, new OWBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(4, new OWRandomLookAroundGoal(this));
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(IS_MAD, false);
        builder.define(IS_ROAR, false);
        builder.define(CLAN_COLOR, 0);
    }

    // Entity Methods
    @Override
    public int getEntityColor() {
        return 0xc59963;
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
        return OWEntityConfig.Archetypes.HEALER;
    }

    @Override
    public OWEntityConfig.Diet getDiet() {
        return OWEntityConfig.Diet.CARNIVOROUS;
    }

    @Override
    public OWEntityConfig.Temperament getTemperament() {
        return OWEntityConfig.Temperament.NEUTRAL;
    }

    @Override
    public List<Class<?>> getFavoriteTargets() {
        return List.of(Player.class, Animal.class);
    }

    @Override
    public float vehicleRunSpeedMultiplier() {
        return this.isMale() ? 4.0f : 4.5f;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 1.5f;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return 7.5f;
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
        return true;
    }

    @Override
    public Item acceptSaddle() {
        return OWItems.KODIAK_SADDLE.get();
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return OWAdvancements.KODIAK_TAMED_ADVANCEMENT;
    }

    @Override
    public float getMaxVitalEnergy() {
        return 450 * (1 + ((float) this.getLevel() / 50));
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 1.0f * (1 + ((float) this.getLevel() / 50));
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
        return 0.15f;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return OWEntityRegistry.LION.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(OWTags.Items.LION_FOOD);
    }

    protected @Nullable SoundEvent getAmbientSound() {
        if (isNapping()) return null;
        return  RANDOM(2) ? OWSounds.KODIAK_IDLE_1.get() : RANDOM(2) ? OWSounds.KODIAK_IDLE_2.get() : OWSounds.KODIAK_IDLE_3.get();
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return OWSounds.KODIAK_MISC.get();
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return RANDOM(2) ? OWSounds.KODIAK_HURT.get() : OWSounds.KODIAK_MISC.get();
    }

    @Override
    public void playStepSound(BlockPos blockPos, BlockState blockState) {
        if (!isRunning()) super.playStepSound(blockPos, blockState);
    }

    @Override
    public void travel(Vec3 vec3) {
        super.travel(vec3);
        if (this.onGround() && !isBaby() && this.horizontalCollision && !isSleeping() && !isNapping() && !this.isVehicle()) this.jumpFromGround();
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (!isTame()) {
            setMad(!isBaby() && target != null && getSleepBarPercent() < 75 && !this.isSitting());
        }
        super.setTarget(target);
    }

    public void tick() {
        super.tick();
        lionTaming.tick();

        createCombo(22, 11, OWSounds.KODIAK_HURTING_2.get(), 3.0, 2, 2.25, false, isFemale() ? 0.35f : 0.5f);
        setTamingPercentage(this.foodGiven, this.foodWanted);

        if (this.isVehicle() && this.isTame() && !this.isSitting()) setMad(this.isCombo());

        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        if (!this.level().isClientSide()) {
            LionClanSyncPacket packet = new LionClanSyncPacket(this.getId(), this.clan, this.myClanId);

            for (ServerPlayer player : this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(200))) {
                OWNetworkHandler.sendToClient(packet, player);
            }
        }

        if (this.needsClanReconstruction) {
            clanReconstructionTimer++;

            if (clanReconstructionTimer >= 5) {
                reconstructClanMembership();
                this.needsClanReconstruction = false;
                clanReconstructionTimer = 0;
            }
        }

        if (this.isFemale()) {
            if (this.clan != null && this.clan.getAlpha() != null) {
                this.setClanColor(this.clan.getAlpha().getClanColor());
            }

            if (this.clan != null && (this.clan.getAlpha() == null || !this.clan.getAlpha().isAlive())) {
                this.clan = null;
            }
        }

        if (this.isMale() && !this.isTame() && this.tickCount % 50 == 0) {
            if (this.clan != null) {
                System.out.println("==================");
                System.out.println("Clan ? " + this.clan);
                System.out.println("Clan : " + this.clan.getClanID());
                System.out.println("Nombre de femelles: " + (this.clan.getLionesses() != null ? this.clan.getLionesses().size() : -1));
                System.out.println("Couleur de l'alpha du clan: " + this.clan.getAlpha().getVariant().getId());
                System.out.println("==================");
            }
        }

        if (this.roarCooldown > 0) {
            this.roarCooldown--;
        }

        if (this.isRoaring()) {
            this.roarTimer++;
            this.setDeltaMovement(0, 0, 0);

            if (this.getTarget() != null) {
                this.setLookAt(this.getTarget().getX(), this.getTarget().getY(), this.getTarget().getZ());
            }

            if (roarTimer >= 12 && roarTimer < 80) {
                if (this.isMale() && this.clan != null && this.clan.getLionesses() != null) {
                    OWNetworkHandler.sendToServer(new LionHealLionessesPacket(this.getId(), true));
                }
            }

            if (this.roarTimer >= 90) {
                this.roarTimer = 0;
                this.setRoar(false);

                if (this.isMale() && this.clan != null && this.clan.getLionesses() != null) {
                    OWNetworkHandler.sendToServer(new LionHealLionessesPacket(this.getId(), false));
                }
            }
        }

        if (this.tickCount % 200 == 0 && this.isFemale()) {
            if (this.clan == null) {
                List<LionEntity> lionsNearby = this.level().getEntitiesOfClass(LionEntity.class, this.getBoundingBox().inflate(20));
                male = null;

                for (LionEntity lion : lionsNearby) {
                    if (lion.isAlive() && !lion.isTame() && lion.isMale() && lion.clan != null) {
                        male = lion;
                        break;
                    }
                }

                if (male != null && male.getScale() > this.getScale()) {
                    male.clan.addLionessToClan(this);

                    this.clan = male.clan;
                    this.myClanId = male.myClanId;
                }
            }
        }

        handleRunningEffects(12, SoundEvents.HORSE_STEP, 0.5f, new int[]{5, 7});
        handleGoldVariantEffects();
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        ItemStack soulStack = createSoulStack();

        if (canDropSoul() && this.isTame() && !this.isInResurrection() && !isBaby()) {
            this.spawnAtLocation(soulStack);
        }

        if (this.isSaddled()) this.spawnAtLocation(acceptSaddle());

        if (!this.isTame() && this.clan != null) {
            if (this.isMale()) {
                this.clan.setLionesses(null);

                if (this.clan != null && this.clan.getLionesses() != null) {
                    for (LionEntity lioness : this.clan.getLionesses()) {
                        this.clan.excludeLionessFromClan(lioness);
                    }
                }

                this.clan = null;
            } else {
                this.clan.excludeLionessFromClan(this);
            }
        }
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
    public boolean doHurtTarget(Entity entity) {
        return super.doHurtTarget(entity);
    }

    @Override
    public void hurtAfterCombo(LivingEntity entity, int comboAttack) {
        if (this.isMale()) {
            if (this.random.nextInt(3) == 0 && !entity.hasEffect(OWEffects.BLEEDING_EFFECT.getDelegate())) {
                entity.addEffect(new MobEffectInstance(OWEffects.BLEEDING_EFFECT.getDelegate(), this.random.nextInt(150) + 150, this.random.nextInt(1)));
            }
        }
        if (!entity.isAlive()) {
            if (entity instanceof LionEntity lion) {
                if (this.clan != null && this.clan.getLionesses() != null && lion.clan != null && lion.clan.getLionesses() != null) {
                    int lionessesInLion2Clan = lion.clan.getLionesses().size();

                    if (lionessesInLion2Clan > 0) {
                        lion.clan.switchAlpha(this);
                        this.clan = lion.clan;
                    }
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource damageSource, float v) {
        if (!this.isTame()) {
            if (this.isSitting()) this.setSitting(false);

            if (this.isMale() && !this.isRoaring()) {
                if (damageSource.getDirectEntity() instanceof Player player) {
                    if (!player.isCreative()) {
                        this.setRoar(true);
                    }
                } else this.setRoar(true);
            }

            if (this.clan != null && this.isMale()) {
                LionEntity alpha = this.clan.getAlpha();

                if (alpha != null) {
                    int lionessInClan = this.clan.getLionesses().size();

                    if (this == alpha) {
                        Entity attacker = damageSource.getEntity();

                        if (attacker instanceof LivingEntity target) {
                            for (LionEntity lioness : this.clan.getLionesses()) {
                                if (target instanceof Player player) {
                                    if (!player.isCreative()) {
                                        lioness.setTarget(target);
                                    }
                                } else {
                                    lioness.setTarget(target);
                                }
                            }
                        }
                    }

                    if (lionessInClan > 0) {
                        float v0 = v / (1 + ((float) lionessInClan / LionClan.MAX_LIONESS_IN_CLAN));

                        return super.hurt(damageSource, v0);
                    }
                }
            }
        }
        return super.hurt(damageSource, v);
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        return super.killedEntity(serverLevel, entity);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof LionEntity otherLion) {
            if (otherLion.isBaby() || this.isBaby()) {
                return true;
            }

            if (this.isTame()) {
                return otherLion.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherLion.getOwnerUUID());
            }

            if (this.myClanId != -1 && this.myClanId == otherLion.myClanId) {
                return true;
            }

            return false;
        }
        return super.isAlliedTo(entity);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) / (this.isFemale() ? 1.5f : 1.0f), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) / (this.isFemale() ? 1.5f : 1.0f));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));

            this.setGender(this.random.nextInt(2));

            this.setVariant(chooseLionVariant());
            this.setInitialVariant(this.getVariant());

            if (this.isMale()) {
                this.myClanId = clanId;
                clan = new LionClan(this.myClanId, this);
                this.setClanColor(clan.generateNewClanColor());
                clanId++;
            } else {
                if (this.clan == null) {
                    List<LionEntity> lionsNearby = this.level().getEntitiesOfClass(LionEntity.class, this.getBoundingBox().inflate(20));
                    LionEntity male = null;

                    for (LionEntity lion : lionsNearby) {
                        if (lion.isAlive() && !lion.isTame() && lion.isMale() && lion.clan != null) {
                            if (lion.clan.getLionesses().size() >= LionClan.MAX_LIONESS_IN_CLAN) continue;
                            male = lion;
                            break;
                        }
                    }

                    if (male != null && male.getScale() > this.getScale()) {
                        male.clan.addLionessToClan(this);

                        this.clan = male.clan;
                        this.myClanId = male.myClanId;
                    }
                }
            }
        }
        this.foodWanted = (int) OWUtils.generateRandomInterval(6, 11);
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    public void createMiniShockwave() {
        Vec3 look = this.getLookAngle();
        double x = this.getX() + look.x * 2.0;
        double z = this.getZ() + look.z * 2.0;
        AABB area = new AABB(x - 1, this.getY() - 1, z - 1, x + 1, this.getY() + 1, z + 1);

        for (int i = 0; i < 50; i++) {
            double px = area.minX + Math.random() * (area.maxX - area.minX);
            double py = area.minY + Math.random() * (area.maxY - area.minY);
            double pz = area.minZ + Math.random() * (area.maxZ - area.minZ);
            BlockParticleOption particleOption = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState());
            this.level().addParticle(particleOption, px, py, pz, 0, 0, 0);
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ROOTED_DIRT_HIT, SoundSource.AMBIENT, 1.0f, 1.0f);
    }

    private void reconstructClanMembership() {
        if (this.myClanId == -1) return;

        List<LionEntity> nearbyLions = this.level().getEntitiesOfClass(LionEntity.class, this.getBoundingBox().inflate(150));

        if (this.isMale()) {
            if (this.clan == null) {
                this.clan = new LionClan(this.myClanId, this);
            }

            for (LionEntity lion : nearbyLions) {
                if (lion != this && lion.myClanId == this.myClanId && lion.isFemale()) {
                    if (!this.clan.getLionesses().contains(lion)) {
                        this.clan.addLionessToClan(lion);
                    }
                    lion.clan = this.clan;
                }
            }
        } else {
            for (LionEntity lion : nearbyLions) {
                if (lion.myClanId == this.myClanId && lion.isMale() && lion.clan != null) {
                    this.clan = lion.clan;

                    if (!lion.clan.getLionesses().contains(this)) {
                        lion.clan.addLionessToClan(this);
                    }
                    break;
                }
            }
        }
    }

    public void changeSkin(int skinIndex) {
        this.setVariant(getInitialVariant());

        if (skinIndex == 1) {
            setVariant(LionVariant.SKIN_GOLD);
        } else if (skinIndex == 7) {
            setVariant(getInitialVariant());
        }

        if (!this.level().isClientSide()) {
            Level world = this.level();
            if (world instanceof ServerLevel) {
                ServerLevel serverWorld = (ServerLevel) world;
                serverWorld.sendParticles(ParticleTypes.ITEM_SLIME,
                        this.getX(), this.getY() + 1, this.getZ(),
                        100,
                        0.5, 0.5, 0.5,
                        0.02
                );
            }
        }
    }

    private void handleGoldVariantEffects() {
        if (this.getVariant() == LionVariant.SKIN_GOLD && this.tickCount % 150 == 0) {
            OWUtils.spawnParticles(this, ParticleTypes.END_ROD, 0, 0, 0, 5, 2);
        }
    }

    private LionVariant chooseLionVariant() {
        LionVariant variant;
        if (this.isFemale()) {
            if (chance >= 80) variant = LionVariant.LIONESS_WHITE;
            else if (chance >= 50) variant = LionVariant.LIONESS_DARK;
            else variant = LionVariant.LIONESS_DEFAULT;
        } else {
            if (chance >= 95) variant = LionVariant.WHITE;
            else if (chance >= 75) variant = LionVariant.DARK;
            else variant = LionVariant.DEFAULT;
        }
        return variant;
    }

    private void setupAnimationState() {
        createIdleAnimation(53, true);
        createSitAnimation(83, true);

        setupComboAnimations();

        if (this.isRoaring()) {
            if (this.roarAnimationTimer <= 0) {
                this.roarAnimationTimer = 90;
                this.roarAnimationState.start(this.tickCount);
            } else --this.roarAnimationTimer;
        }

        if (!this.isRoaring()) {
            this.roarAnimationTimer = 0;
            this.roarAnimationState.stop();
        }
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

    public LionVariant getVariant() {
        return LionVariant.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(LionVariant variant) {
        this.entityData.set(VARIANT, variant.getId() & 255);
    }

    public LionVariant getInitialVariant() {
        return LionVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));
    }

    public void setInitialVariant(LionVariant variant) {
        this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());
    }

    public void setClanColor(int getClanColor) {
        this.entityData.set(CLAN_COLOR, getClanColor);
    }

    public int getClanColor() { return this.entityData.get(CLAN_COLOR);}

    public void setMad(boolean isMad) {
        if (isMad) if (this.getCurrentMode() == Mode.Passive) return;
        this.entityData.set(IS_MAD, isMad);
    }

    public boolean isMad() { return this.entityData.get(IS_MAD);}

    public void setRoar(boolean isRoaring) {
        if (this.isFemale()) return;

        if (isRoaring && this.roarCooldown > 0) {
            return;
        }

        this.entityData.set(IS_ROAR, isRoaring);

        if (isRoaring) {
            this.roarCooldown = ROAR_COOLDOWN_MAX;
        }
    }

    public boolean isRoaring() { return this.entityData.get(IS_ROAR);}

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putBoolean("isRoaring", this.isRoaring());
        tag.putInt("getClanColor", this.getClanColor());
        tag.putInt("foodGiven", this.foodGiven);
        tag.putInt("foodWanted", this.foodWanted);
        tag.putInt("myClanId", this.myClanId);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.entityData.set(IS_ROAR, tag.getBoolean("isRoaring"));
        this.entityData.set(CLAN_COLOR, tag.getInt("getClanColor"));

        this.foodGiven = tag.getInt("foodGiven");
        this.foodWanted = tag.getInt("foodWanted");
        this.myClanId = tag.getInt("myClanId");

        if (this.myClanId >= clanId) {
            clanId = this.myClanId + 1;
        }

        if (this.myClanId != -1) {
            this.needsClanReconstruction = true;
        }
    }
}
