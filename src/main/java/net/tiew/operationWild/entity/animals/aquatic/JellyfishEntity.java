package net.tiew.operationWild.entity.animals.aquatic;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.goals.*;
import net.tiew.operationWild.entity.OWWaterEntity;
import net.tiew.operationWild.entity.misc.SeaBugEntity;
import net.tiew.operationWild.particle.OWParticles;
import net.tiew.operationWild.sound.OWSounds;
import net.tiew.operationWild.core.OWDamageSources;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.entity.OWEntityUtils;
import net.tiew.operationWild.entity.variants.JellyfishVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.core.OWUtils;

import java.util.List;

import static net.tiew.operationWild.core.OWUtils.RANDOM;

public class JellyfishEntity extends OWWaterEntity implements OWEntityUtils {

    public static final double TAMING_EXPERIENCE = 0.0;

    public String[] quests = {};
    public int foodGiven = 0;
    public int foodWanted;

    public int electrifiedTimer = 0;
    public boolean canPlayElectricalSound = true;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(JellyfishEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_ELECTRIFIED = SynchedEntityData.defineId(JellyfishEntity.class, EntityDataSerializers.BOOLEAN);

    public JellyfishVariant getVariant() { return JellyfishVariant.byId(this.getTypeVariant() & 255);}
    public void setVariant(JellyfishVariant variant) { this.entityData.set(VARIANT, variant.getId() & 255);}
    public JellyfishVariant getInitialVariant() { return JellyfishVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));}
    public void setInitialVariant(JellyfishVariant variant) { this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());}

    public JellyfishEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    // Entity's AI
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new TryFindWaterGoal(this));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(8, new FollowBoatGoal(this));

        this.goalSelector.addGoal(0, new JellyFishAttackAI(this, 40, 0.75));

        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));

        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this).setAlertOthers(new Class[0])));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes().add(Attributes.MAX_HEALTH, 14.0D).add(Attributes.MOVEMENT_SPEED, 0.12D).add(Attributes.FOLLOW_RANGE, 40.0D).add(Attributes.ATTACK_DAMAGE, 1.0D).add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    public static boolean checkSurfaceWaterAnimalSpawnRules(EntityType<? extends JellyfishEntity> waterAnimal, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getFluidState(pos.below()).is(FluidTags.WATER) && level.getBlockState(pos.above()).is(Blocks.WATER);
    }

    protected @Nullable SoundEvent getAmbientSound() {
        return RANDOM(3) ? SoundEvents.DOLPHIN_SWIM : null;
    }

    protected float getSoundVolume() { return 1f;}
    
    @Override
    public void travel(Vec3 vec3) {
        super.travel(vec3);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
    }

    public void tick() {
        super.tick();
        setTamingPercentage(this.foodGiven, this.foodWanted);
        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        setNoGravity(isInWater());

        List<Entity> entitiesCanBeHurt = this.level().getEntitiesOfClass(Entity.class, this.getBoundingBox().inflate(2));

        for (Entity entity : entitiesCanBeHurt) {
            if (!this.isInWater() || !this.isAlive()) return;
            if (entity instanceof Player player && player.isCreative()) continue;
            if (!entity.isInvulnerable() && !(entity instanceof JellyfishEntity) && electrifiedTimer == 0) {
                this.setElectrified(true);
            }
        }

        if (isElectrified()) {
            electrifiedTimer++;
            if (canPlayElectricalSound) {
                this.playSound(OWSounds.JELLYFISH_ELECTRIFIED.get(), 1.0f, 0.8f);
                canPlayElectricalSound = false;
            }

            List<Entity> livingEntitiesCanBeHurt = this.level().getEntitiesOfClass(Entity.class, this.getBoundingBox().inflate(2));

            for (Entity entity : livingEntitiesCanBeHurt) {
                if (entity instanceof ItemEntity) continue;
                if (entity instanceof Player player && player.getVehicle() instanceof WalrusEntity) continue;
                if (!entity.isInvulnerable() && entity.isAlive() && !(entity instanceof JellyfishEntity)) {
                    if (!this.level().isClientSide() && tickCount % 20 == 0) {
                        DamageSource electricDamages = OWDamageSources.createElectrifiedDamage((ServerLevel) this.level(), this);
                        if (!(entity instanceof WalrusEntity)) {
                            entity.hurt(electricDamages, this.getDamage());
                        }
                        if (entity instanceof LivingEntity livingEntity && livingEntity.getVehicle() == null) {
                            if (livingEntity instanceof Player player && player.isCreative()) return;
                            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 255, false, false, false));
                            livingEntity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 255, false, false, false));

                            if (tickCount % 100 == 0) {
                                if (livingEntity instanceof Player player) {
                                    ItemStack mainHandItem = player.getMainHandItem();
                                    if (mainHandItem != null && !mainHandItem.isEmpty()) {
                                        if (mainHandItem.getItem() == OWItems.BATTERY.get()) {
                                            if (mainHandItem.getDamageValue() > 0) {
                                                int currentDamage = mainHandItem.getDamageValue();
                                                mainHandItem.setDamageValue(Math.max(0, currentDamage - 1));
                                            }
                                        }
                                    }

                                    ItemStack offHandItem = player.getOffhandItem();
                                    if (offHandItem != null && !offHandItem.isEmpty()) {
                                        if (offHandItem.getItem() == OWItems.BATTERY.get()) {
                                            if (offHandItem.getDamageValue() > 0) {
                                                int currentDamage = offHandItem.getDamageValue();
                                                offHandItem.setDamageValue(Math.max(0, currentDamage - 1));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (entity instanceof SeaBugEntity seaBug) {
                            seaBug.setOff(true);
                            seaBug.setEnergy(0);
                        }

                        if (entity instanceof Player) {
                            Vec3 direction = new Vec3(
                                    this.getX() - entity.getX(),
                                    this.getY() - entity.getY(),
                                    this.getZ() - entity.getZ()
                            );

                            direction = direction.normalize();

                            double speed = 0.25;
                            Vec3 velocity = direction.scale(speed);

                            entity.setDeltaMovement(velocity);
                        }
                    }
                    if (tickCount % 3 == 0) {
                        OWUtils.spawnParticles(entity, OWParticles.ELECTRIC_PARTICLES.get(), 0, 0, 0, 5, 4);
                    }
                }
            }

            if (electrifiedTimer % 3 == 0) {
                this.setVariant(JellyfishVariant.ELECTRIFIED);
                OWUtils.spawnParticles(this, OWParticles.ELECTRIC_PARTICLES.get(), 0, 0, 0, 5, 4);
            }
            if (electrifiedTimer % 10 == 0) this.setVariant(this.getInitialVariant());

            if (electrifiedTimer >= 40) {
                electrifiedTimer = 0;
                canPlayElectricalSound = true;
                this.setElectrified(false);
            }
        }
    }

    public void setElectrified(boolean isElectrified) {this.entityData.set(IS_ELECTRIFIED, isElectrified);}
    public boolean isElectrified() { return this.entityData.get(IS_ELECTRIFIED); }

    @Override
    public boolean doHurtTarget(Entity entity) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float v) {
        boolean hurtByWalrus = damageSource.getEntity() instanceof WalrusEntity;
        return super.hurt(damageSource, v * (hurtByWalrus ? 1.5f : 1.0f));
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        return super.killedEntity(serverLevel, entity);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof JellyfishEntity otherJellyfish) {
            if (this.isTame()) return otherJellyfish.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherJellyfish.getOwnerUUID());
            else return !otherJellyfish.isTame();
        }
        return super.isAlliedTo(entity);
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


            this.setVariant(chooseJellyfishVariant());
            this.setInitialVariant(this.getVariant());
        }
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader level) {
        return level.isUnobstructed(this);
    }

    private JellyfishVariant chooseJellyfishVariant() {
        JellyfishVariant variant;
        if (chance >= 83.33) variant = JellyfishVariant.ORANGE;
        else if (chance >= 66.66) variant = JellyfishVariant.PINK;
        else if (chance >= 50) variant = JellyfishVariant.GREEN;
        else if (chance >= 33.33) variant = JellyfishVariant.PURPLE;
        else if (chance >= 16.66) variant = JellyfishVariant.WHITE;
        else variant = JellyfishVariant.DEFAULT;
        return variant;
    }

    private void setupAnimationState() {
        createIdleAnimation(213, true);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
        builder.define(IS_ELECTRIFIED, false);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putBoolean("isElectrified", this.isElectrified());
        tag.putInt("numberFeedsGiven", this.numberFeedsGiven);
        tag.putInt("numberFeedsGiven", this.numberFeedsGiven);

    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.entityData.set(IS_ELECTRIFIED, tag.getBoolean("isElectrified"));
        this.numberFeedsGiven = tag.getInt("numberFeedsGiven");
        this.numberFeedsGiven = tag.getInt("numberFeedsGiven");
    }

    @Override
    public int getEntityColor() {
        return 8836061;
    }

    @Override
    public float getEntityScale() {
        return 0;
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return (int) OWUtils.generateRandomInterval(3, 6);
    }
}

