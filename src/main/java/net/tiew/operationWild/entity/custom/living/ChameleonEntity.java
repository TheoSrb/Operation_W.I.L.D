package net.tiew.operationWild.entity.custom.living;

import net.minecraft.client.renderer.BiomeColors;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.event.EventHooks;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.ChameleonUtilsSendToClient;
import org.jetbrains.annotations.Nullable;
import net.tiew.operationWild.entity.AI.OWFollowOwnerGoal;
import net.tiew.operationWild.entity.AI.OWRandomLookAroundGoal;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityUtils;
import net.tiew.operationWild.entity.variants.ChameleonVariant;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.AnimalSoulItem;

import static net.tiew.operationWild.utils.OWUtils.RANDOM;

public class ChameleonEntity extends OWEntity implements OWEntityUtils {

    public static final double TAMING_EXPERIENCE = 10.0;

    public ResourceLocation CAMOUFLAGE_TEXTURE;
    public ResourceLocation PREVIOUS_CAMOUFLAGE_TEXTURE;
    public Block previousBlock = null;
    public int camouflageTimer = 0;
    public int fadeTimer = 0;
    public static final int FADE_DURATION = 150;
    public static final int CLIMBING_FADE_DURATION = 10;
    public boolean isTransitioning = false;
    public boolean hasInitialTexture = false;

    public boolean isStartingClimbing = false;
    public boolean isStopingClimbing = false;
    public int climbingTimer = 0;

    public String[] quests = {};
    public int foodGiven = 0;
    public int foodWanted;

    private static final EntityDataAccessor<Integer> DATA_INITIAL_VARIANT = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.INT);

    public ChameleonVariant getVariant() { return ChameleonVariant.byId(this.getTypeVariant() & 255);}
    public void setVariant(ChameleonVariant variant) { this.entityData.set(VARIANT, variant.getId() & 255);}
    public ChameleonVariant getInitialVariant() { return ChameleonVariant.byId(this.entityData.get(DATA_INITIAL_VARIANT));}
    public void setInitialVariant(ChameleonVariant variant) { this.entityData.set(DATA_INITIAL_VARIANT, variant.getId());}

    public ChameleonEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }


    // Entity's AI
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(7, new OWRandomLookAroundGoal(this));
        this.goalSelector.addGoal(2, new OWFollowOwnerGoal(this, this.getSpeed() * 20f, 15, 3));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
    }

    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes().add(Attributes.MAX_HEALTH, 8.0D).add(Attributes.MOVEMENT_SPEED, 0.17D).add(Attributes.FOLLOW_RANGE, 10.0D).add(Attributes.ATTACK_DAMAGE, 0.0D).add(Attributes.KNOCKBACK_RESISTANCE, 0.1D);
    }

    protected @Nullable SoundEvent getAmbientSound() {
        return RANDOM(5) ? null : null;
    }

    protected float getSoundVolume() { return 1f;}

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        super.playStepSound(blockPos, blockState);
    }

    public void setBuyingSkin(int skinIndex) {
        switch (skinIndex) {
            default -> throw new IllegalArgumentException("Invalid skin index: " + skinIndex);
        }
    }

    @Override
    public void travel(Vec3 vec3) {
        super.travel(vec3);
    }

    public boolean isClimbing() {
        return ((Byte)this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setClimbing(boolean climbing) {
        byte b0 = (Byte)this.entityData.get(DATA_FLAGS_ID);
        if (climbing) {
            b0 = (byte)(b0 | 1);
        } else {
            b0 = (byte)(b0 & -2);
        }

        this.entityData.set(DATA_FLAGS_ID, b0);
    }

    @Override
    public boolean onClimbable() {
        return this.isClimbing();
    }

    @Override
    protected float getJumpPower() {
        return 0.0f;
    }

    @Override
    public void jumpFromGround() {
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        ItemStack soulStack = new ItemStack(OWItems.ANIMAL_SOUL.get());

        Item item = soulStack.getItem();
        if (item instanceof AnimalSoulItem animalSoulItem) {
            UseOnContext fakeContext = new UseOnContext(this.level(), null, InteractionHand.MAIN_HAND, soulStack, new BlockHitResult(this.position(), Direction.UP, this.blockPosition(), false));

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

        if (canDropSoul() && this.isTame() && !this.isInResurrection() && !isBaby()) {
            this.spawnAtLocation(soulStack);
        }
        /*if (this.isSaddled()) this.spawnAtLocation(OWItems.CHAMELEON_SADDLE.get());*/
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
    }

    public void changeSkin(int skinIndex) {
        this.setVariant(getInitialVariant());
        
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

    public void tick() {
        super.tick();
        setTamingPercentage(this.foodGiven, this.foodWanted);
        if (this.level().isClientSide()) setupAnimationState();
        if (this.isInResurrection()) this.setSleeping(true);

        if (!this.level().isClientSide) {
            boolean wasClimbing = this.isClimbing();
            this.setClimbing(this.horizontalCollision);
            boolean isNowClimbing = this.isClimbing();

            if (!wasClimbing && isNowClimbing && !isStartingClimbing) {
                isStartingClimbing = true;
                isStopingClimbing = false;
            }

            if (wasClimbing && !isNowClimbing && !isStopingClimbing) {
                isStopingClimbing = true;
                isStartingClimbing = false;
            }
        }

        if (isStartingClimbing) {
            if (climbingTimer < CLIMBING_FADE_DURATION) {
                climbingTimer++;
            } else {
                climbingTimer = CLIMBING_FADE_DURATION;
                isStartingClimbing = false;
            }
        }

        if (isStopingClimbing) {
            if (climbingTimer > 0) {
                climbingTimer--;
            } else {
                climbingTimer = 0;
                isStopingClimbing = false;
            }
        }


        if (!this.level().isClientSide()) {
            if (this.level() instanceof ServerLevel serverLevel) {
                for (ServerPlayer player : serverLevel.players()) {
                    OWNetworkHandler.sendToClient(new ChameleonUtilsSendToClient(
                            this.getId(),
                            this.CAMOUFLAGE_TEXTURE,
                            this.PREVIOUS_CAMOUFLAGE_TEXTURE,
                            camouflageTimer,
                            fadeTimer,
                            isTransitioning,
                            climbingTimer,
                            isStopingClimbing
                    ), player);
                }
            }
        }

        if (isTransitioning) {
            fadeTimer++;
            if (fadeTimer >= FADE_DURATION) {
                PREVIOUS_CAMOUFLAGE_TEXTURE = null;
                isTransitioning = false;
                fadeTimer = 0;
            }
        }

        int changeColorInterval = 300;

        if (tickCount % changeColorInterval == 0 && !this.level().isClientSide()) {
            BlockPos blockPosBelow = this.blockPosition().below();
            Block blockBelow = this.level().getBlockState(blockPosBelow).getBlock();

            String[] $$0 = blockBelow.asItem().toString().split(":");

            boolean canChangeColor = !isWaxedCopperBlock(blockBelow) && isFullBlock(blockBelow);


            if ($$0[1].equals("snow_block")) $$0[1] = "snow";
            if ($$0[1].equals("mycelium")) $$0[1] = "mycelium_top";
            if ($$0[1].equals("podzol")) $$0[1] = "podzol_top";
            if ($$0[1].equals("dried_kelp_block")) $$0[1] = "dried_kelp_side";
            if ($$0[1].equals("bee_nest")) $$0[1] = "bee_nest_side";
            if ($$0[1].equals("hay_block")) $$0[1] = "hay_block_side";
            if ($$0[1].equals("melon")) $$0[1] = "melon_side";
            if ($$0[1].contains("leaves") && !$$0[1].contains("cherry")) $$0[1] = "azalea_leaves";


            if (canChangeColor) {
                if (blockBelow != previousBlock) {
                    previousBlock = blockBelow;

                    ResourceLocation newTexture;

                    if ($$0[1].equals("grass_block")) {
                        int grassColor = BiomeColors.getAverageGrassColor(this.level(), this.getBlockPosBelowThatAffectsMyMovement());
                        newTexture = ResourceLocation.fromNamespaceAndPath("color", String.format("%06x", grassColor));
                    }
                    else if (this.isInWater()) {
                        int waterColor = BiomeColors.getAverageWaterColor(this.level(), this.getBlockPosBelowThatAffectsMyMovement());
                        newTexture = ResourceLocation.fromNamespaceAndPath("color", String.format("%06x", waterColor));
                    }
                    else {
                        newTexture = ResourceLocation.fromNamespaceAndPath($$0[0], "textures/block/" + $$0[1] + ".png");
                    }

                    if (!hasInitialTexture) {
                        CAMOUFLAGE_TEXTURE = newTexture;
                        PREVIOUS_CAMOUFLAGE_TEXTURE = null;
                        isTransitioning = true;
                        fadeTimer = 0;
                        hasInitialTexture = true;
                    } else {
                        PREVIOUS_CAMOUFLAGE_TEXTURE = CAMOUFLAGE_TEXTURE;
                        CAMOUFLAGE_TEXTURE = newTexture;
                        isTransitioning = true;
                        fadeTimer = 0;
                    }
                }
            } else {
                ResourceLocation newTexture = null;

                if (!hasInitialTexture) {
                    CAMOUFLAGE_TEXTURE = newTexture;
                    PREVIOUS_CAMOUFLAGE_TEXTURE = null;
                    isTransitioning = true;
                    fadeTimer = 0;
                    hasInitialTexture = true;
                } else {
                    PREVIOUS_CAMOUFLAGE_TEXTURE = CAMOUFLAGE_TEXTURE;
                    CAMOUFLAGE_TEXTURE = newTexture;
                    isTransitioning = true;
                    fadeTimer = 0;
                }
            }
        }



        /*if (this.getVariant() == ChameleonVariant.SKIN_GOLD && this.tickCount % 150 == 0) {
            OWUtils.spawnParticles(this, ParticleTypes.END_ROD, 0, 0, 0, 5, 2);
        }*/
    }

    public float getFadeOpacity() {
        if (!isTransitioning) return 1.0f;
        return (float) fadeTimer / FADE_DURATION;
    }

    private boolean isFullBlock(Block block) {
        if (block.defaultBlockState().is(Blocks.WATER)) return true;

        BlockState blockState = block.defaultBlockState();
        VoxelShape shape = blockState.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);

        if (shape.isEmpty()) return false;

        boolean isFullShape = shape.bounds().equals(Shapes.block().bounds());

        if (!isFullShape) return false;

        if (block instanceof SlabBlock) return false;
        if (block instanceof StairBlock) return false;
        if (block instanceof WallBlock) return false;
        if (block instanceof FenceBlock) return false;
        if (block instanceof FenceGateBlock) return false;
        if (block instanceof DoorBlock) return false;
        if (block instanceof TrapDoorBlock) return false;
        if (block instanceof ButtonBlock) return false;
        if (block instanceof PressurePlateBlock) return false;
        if (block instanceof CarpetBlock) return false;
        if (block instanceof ChainBlock) return false;
        if (block instanceof LanternBlock) return false;
        if (block instanceof TorchBlock) return false;
        if (block instanceof RedstoneTorchBlock) return false;
        if (block instanceof LadderBlock) return false;
        if (block instanceof VineBlock) return false;
        if (block instanceof TintedGlassBlock) return false;
        if (block instanceof StainedGlassBlock) return false;
        if (block instanceof IceBlock) return false;
        if (block instanceof BarrierBlock) return false;
        if (block instanceof BeaconBlock) return false;

        return blockState.isSolid() && blockState.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
    }

    private boolean isWaxedCopperBlock(Block block) {
        return block == Blocks.WAXED_COPPER_BLOCK ||
                block == Blocks.WAXED_EXPOSED_COPPER ||
                block == Blocks.WAXED_WEATHERED_COPPER ||
                block == Blocks.WAXED_OXIDIZED_COPPER ||
                block == Blocks.WAXED_CUT_COPPER ||
                block == Blocks.WAXED_EXPOSED_CUT_COPPER ||
                block == Blocks.WAXED_WEATHERED_CUT_COPPER ||
                block == Blocks.WAXED_OXIDIZED_CUT_COPPER;
    }

    public float getPreviousFadeOpacity() {
        if (!isTransitioning) return 0.0f;
        return 1.0f - ((float) fadeTimer / FADE_DURATION);
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        return super.doHurtTarget(entity);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float v) {
        return super.hurt(damageSource, v);
    }

    @Override
    protected void positionRider(Entity entity, MoveFunction function) {
        super.positionRider(entity, function);
        function.accept(entity, entity.getX(), entity.getY() - 1, entity.getZ());
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity entity) {
        return super.killedEntity(serverLevel, entity);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof ChameleonEntity otherChameleon) {
            if (this.isTame()) return otherChameleon.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherChameleon.getOwnerUUID());
            else return !otherChameleon.isTame();
        }
        return super.isAlliedTo(entity);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (/*itemStack.is(OWItems.SAVAGE_BERRIES.get()) &&*/ !this.isTame() && this.isBaby()) {
            foodGiven++;
            this.playSound(SoundEvents.CAMEL_EAT);
            itemStack.shrink(1);

            if (!EventHooks.onAnimalTame(this, player)) {
                if (!this.level().isClientSide() && foodGiven >= foodWanted) {
                    this.setTame(true, player);
                    this.setSleeping(false);
                    resetSleepBar();
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (itemStack.is(OWItems.SAVAGE_BERRIES.get())) return InteractionResult.PASS;
        return super.mobInteract(player, hand);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (mobSpawnType != MobSpawnType.BREEDING) {
            this.setRandomAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH), this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
            this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));


            this.setVariant(ChameleonVariant.DEFAULT);
            this.setInitialVariant(this.getVariant());
        }

        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }


    private void setupAnimationState() {
        createIdleAnimation(48, true);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_INITIAL_VARIANT, -1);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("getInitialVariant", this.getInitialVariant().getId());
        tag.putInt("Variant", this.getTypeVariant());
        tag.putInt("numberFeedsGiven", this.numberFeedsGiven);
        tag.putInt("numberFeedsGiven", this.numberFeedsGiven);
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_INITIAL_VARIANT, tag.getInt("getInitialVariant"));
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.numberFeedsGiven = tag.getInt("numberFeedsGiven");
        this.numberFeedsGiven = tag.getInt("numberFeedsGiven");
    }

    @Override
    public int getEntityColor() {
        return 7309894;
    }

    @Override
    public float getEntityScale() {
        return 1.5f;
    }
}

