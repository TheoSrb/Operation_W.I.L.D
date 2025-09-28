package net.tiew.operationWild.entity.AI;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.EventHooks;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.goals.*;
import net.tiew.operationWild.entity.goals.kodiak.KodiakAttractedToGoal;
import net.tiew.operationWild.entity.goals.kodiak.KodiakRollGoal;
import net.tiew.operationWild.entity.goals.kodiak.KodiakSearchInsideChestGoal;
import net.tiew.operationWild.entity.goals.kodiak.KodiakTemptGoal;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AIKodiak extends OWEntity {

    public enum KodiakState {
        IDLE,
        GOING_TO_BEE_NEST,
        GOING_TO_CROPS,
        GOING_TO_CHEST,
        GOING_TO_CAMPFIRE,
        GOING_TO_ITEMS,
        ROLLING,
        NAPPING;
    }

    protected KodiakEntity kodiak;

    protected boolean canAttack = false;
    public ItemStack foodPick = ItemStack.EMPTY;

    private boolean startEatingTimer = false;
    private boolean startHoneyTimer = false;
    private static final int MAX_EATING_TIMER = 400;
    private static final int MAX_HONEY_TIMER = 750;
    private int eatingTimer = 0;
    private int honeyTimer = 0;

    private Player lastPlayerWhoFeedHim = null;
    private int numberOfBonusSearching = 0;
    private int numberOfBonusSearchingMax = this.random.nextInt(7) + 5;
    private int cropCheckTimer = 0;
    private BlockPos targetCrop = null;
    private int cropRadiusSearch = 0;

    private final int MAX_DIRTY_TIMER = 1200;
    private int dirtyTimer = 0;

    public ChestBlockEntity chestBlockEntity = null;
    public boolean isSearchingInsideChest = false;

    protected KodiakState currentKodiakState = KodiakState.IDLE;
    private int goalCommitmentTicks = 0;
    private final int COMMITMENT_DURATION = 200;

    private static final EntityDataAccessor<ItemStack> FOOD_PICK = SynchedEntityData.defineId(AIKodiak.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> IS_DIRTY = SynchedEntityData.defineId(AIKodiak.class, EntityDataSerializers.BOOLEAN);

    protected AIKodiak(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
        kodiak = (KodiakEntity) this;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.registerBasicsGoals();

        this.goalSelector.addGoal(1, new KodiakAttractedToGoal<>(this, (KodiakEntity) this, ItemEntity.class,
                1.75f, 15, 5.0f, () -> pickupItemInHisMouth(foodPick), this.getFoodPick().isEmpty()));

        this.goalSelector.addGoal(2, new KodiakSearchInsideChestGoal(this, (KodiakEntity) this, 2.0f, 35,
                1.5f, () -> openChest(chestBlockEntity)));

        this.goalSelector.addGoal(3, new NapGoal((KodiakEntity) this, 0.5f, 700, true));

        this.goalSelector.addGoal(5, new KodiakAttractedToGoal<>(this, (KodiakEntity) this, Blocks.BEE_NEST,
                1.75f, 25, 2.0f, this::lookForHoneyInTheBeeNest, this.getFoodPick().isEmpty()));

        this.goalSelector.addGoal(7, new KodiakRollGoal(this, (KodiakEntity) this, 0.9f));

        this.goalSelector.addGoal(11, new KodiakAttractedToGoal<>(this, (KodiakEntity) this, Blocks.CAMPFIRE,
                1.0f, 60, 1.0f, () -> pickupItemInHisMouth(foodPick), this.getFoodPick().isEmpty()));

        this.goalSelector.addGoal(13, new KodiakAttractedToGoal<>(this, (KodiakEntity) this, BlockTags.CROPS,
                1.15f, 80, 0.5f, () -> goToNewCropBlock(20), this.getFoodPick().isEmpty()));
    }

    private void registerBasicsGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new KodiakTemptGoal((KodiakEntity) this, 2D, Ingredient.of(Tags.Items.FOODS), false));
        this.goalSelector.addGoal(3, new OWAttackGoal(this, this.getSpeed() * 30f, 8, 3, canAttack()));
        this.goalSelector.addGoal(6, new OWBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new OWRandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8D));

        /*this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, Monster.class, true));*/
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FOOD_PICK, ItemStack.EMPTY);
        builder.define(IS_DIRTY, false);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.onGround()) {
            this.trampleCrops(this.blockPosition());
            this.trampleCrops(this.blockPosition().below());
        }
    }

    private void handleTamingSystem() {
        List<ItemEntity> items = this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(1));

        if (kodiak.isRolling() && kodiak.isDirty()) {
            for (ItemEntity item : items) {
                if (item.getItem().is(OWTags.Items.KODIAK_FOOD)) {
                    LivingEntity target = (LivingEntity) item.getOwner();
                    if (target != null) {
                        kodiak.setRolling(false);

                        if (!EventHooks.onAnimalTame(this, (Player) target)) {
                            if (!this.level().isClientSide() && this.foodGiven >= (this.foodWanted - 1)) {
                                this.setTame(true, (Player) target);
                                this.setDirty(false);

                                if (this.getFoodPick() != null && !this.getFoodPick().isEmpty()) {
                                    this.eatFoodInHisMouth(this.getFoodPick());
                                }

                                this.setSleeping(false);
                                resetSleepBar();
                            } else {
                                if (target instanceof Player player) {
                                    if (!player.isCreative()) {
                                        this.setTarget(player);
                                    }
                                } else {
                                    this.setTarget(target);
                                }
                                this.foodGiven++;
                                this.playSound(SoundEvents.GENERIC_EAT);

                                ItemStack itemStack = item.getItem();
                                Vec3 lookDirection = this.getLookAngle();
                                double spawnX = this.getX() + lookDirection.x * 2.0;
                                double spawnY = this.getY() + 0.8;
                                double spawnZ = this.getZ() + lookDirection.z * 2.0;

                                spawnItemParticles(itemStack, spawnX, spawnY, spawnZ);

                                spawnComposterParticlesAround();
                            }
                            item.discard();
                            break;
                        }
                    }
                }
            }
        }
    }

    private void spawnItemParticles(ItemStack itemStack, double x, double y, double z) {
        if (!this.level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            for (int i = 0; i < 16; i++) {
                serverLevel.sendParticles(
                        new ItemParticleOption(ParticleTypes.ITEM, (itemStack != null && !itemStack.isEmpty()) ? itemStack : Items.APPLE.getDefaultInstance()),
                        x + (this.random.nextDouble() - 0.5) * 0.5,
                        y,
                        z + (this.random.nextDouble() - 0.5) * 0.5,
                        1,
                        (this.random.nextDouble() - 0.5) * 0.1,
                        this.random.nextDouble() * 0.1,
                        (this.random.nextDouble() - 0.5) * 0.1,
                        0.0
                );
            }
        } else {
            for (int i = 0; i < 16; i++) {
                this.level().addParticle(
                        new ItemParticleOption(ParticleTypes.ITEM, (itemStack != null && !itemStack.isEmpty()) ? itemStack : Items.APPLE.getDefaultInstance()),
                        x + (this.random.nextDouble() - 0.5) * 0.5,
                        y,
                        z + (this.random.nextDouble() - 0.5) * 0.5,
                        (this.random.nextDouble() - 0.5) * 0.1,
                        this.random.nextDouble() * 0.1,
                        (this.random.nextDouble() - 0.5) * 0.1
                );
            }
        }
    }

    private void spawnComposterParticlesAround() {
        double centerX = this.getX();
        double centerY = this.getY();
        double centerZ = this.getZ();

        if (!this.level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            for (int i = 0; i < 100; i++) {
                double angle = (2 * Math.PI * i) / 100.0;
                double radius = 0.5 + this.random.nextDouble() * 1.5f;

                double particleX = centerX + Math.cos(angle) * radius;
                double particleZ = centerZ + Math.sin(angle) * radius;
                double particleY = centerY + this.random.nextDouble() * 2.0;

                serverLevel.sendParticles(
                        ParticleTypes.COMPOSTER,
                        particleX,
                        particleY,
                        particleZ,
                        1,
                        (this.random.nextDouble() - 0.5) * 0.2,
                        this.random.nextDouble() * 0.2,
                        (this.random.nextDouble() - 0.5) * 0.2,
                        0.1f
                );
            }

            for (int i = 0; i < 50; i++) {
                double randomX = centerX + (this.random.nextDouble() - 0.5) * 6.0;
                double randomY = centerY + this.random.nextDouble() * 3.0;
                double randomZ = centerZ + (this.random.nextDouble() - 0.5) * 6.0;

                serverLevel.sendParticles(
                        ParticleTypes.COMPOSTER,
                        randomX,
                        randomY,
                        randomZ,
                        1,
                        (this.random.nextDouble() - 0.5) * 0.3,
                        this.random.nextDouble() * 0.3,
                        (this.random.nextDouble() - 0.5) * 0.3,
                        0.15f
                );
            }
        } else {
            for (int i = 0; i < 100; i++) {
                double angle = (2 * Math.PI * i) / 100.0;
                double radius = 0.5 + this.random.nextDouble() * 1.5f;

                double particleX = centerX + Math.cos(angle) * radius;
                double particleZ = centerZ + Math.sin(angle) * radius;
                double particleY = centerY + this.random.nextDouble() * 2.0;

                this.level().addParticle(
                        ParticleTypes.COMPOSTER,
                        particleX,
                        particleY,
                        particleZ,
                        (this.random.nextDouble() - 0.5) * 0.2,
                        this.random.nextDouble() * 0.2,
                        (this.random.nextDouble() - 0.5) * 0.2
                );
            }

            for (int i = 0; i < 50; i++) {
                double randomX = centerX + (this.random.nextDouble() - 0.5) * 6.0;
                double randomY = centerY + this.random.nextDouble() * 3.0;
                double randomZ = centerZ + (this.random.nextDouble() - 0.5) * 6.0;

                this.level().addParticle(
                        ParticleTypes.COMPOSTER,
                        randomX,
                        randomY,
                        randomZ,
                        (this.random.nextDouble() - 0.5) * 0.3,
                        this.random.nextDouble() * 0.3,
                        (this.random.nextDouble() - 0.5) * 0.3
                );
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        boolean hasSomethingInHisMouth = getFoodPick() != null && !getFoodPick().isEmpty();

        handleTamingSystem();

        if (goalCommitmentTicks > 0) {
            goalCommitmentTicks--;
            if (goalCommitmentTicks == 0) {
                currentKodiakState = KodiakState.IDLE;
            }
        }

        if (isSearchingInsideChest) this.setNap(false);

        if (hasSomethingInHisMouth) {
            if (startEatingTimer) {
                if (eatingTimer < MAX_EATING_TIMER) eatingTimer++;
                else {
                    eatFoodInHisMouth(getFoodPick());
                }
            }

            if (getFoodPick() == Items.HONEYCOMB.getDefaultInstance()) {
                if (startHoneyTimer) {
                    if (honeyTimer < MAX_HONEY_TIMER) honeyTimer++;
                    else {
                        eatFoodInHisMouth(getFoodPick());
                    }
                }
            }
        }


        if (kodiak.isRolling()) {
            kodiak.rollTimer++;

            this.trampleCrops(this.blockPosition());
            this.trampleCrops(this.blockPosition().below());

            Vec3 lookDirection = kodiak.getLookAngle();
            Vec3 leftDirection = new Vec3(lookDirection.z, 0, -lookDirection.x);

            double rollSpeed = 0.075;
            kodiak.setDeltaMovement(leftDirection.scale(rollSpeed));
            kodiak.setDeltaMovement(kodiak.getDeltaMovement().x, -1, kodiak.getDeltaMovement().z);

            if (kodiak.tickCount % 15 == 0) {
                kodiak.playStepSound(kodiak.blockPosition(), kodiak.getBlockStateOn());

                double particleX = kodiak.getX();
                double particleY = kodiak.getY();
                double particleZ = kodiak.getZ();

                if (!kodiak.level().isClientSide) {
                    if (kodiak.level() instanceof ServerLevel serverLevel) {
                        BlockParticleOption dirtParticle = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState());
                        serverLevel.sendParticles(dirtParticle,
                                particleX, particleY, particleZ,
                                8,
                                0.5, 0.1, 0.5,
                                0.2);
                    }
                } else {
                    if (kodiak.level() instanceof ClientLevel clientLevel) {
                        BlockParticleOption dirtParticle = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState());

                        for (int i = 0; i < 8; i++) {
                            double offsetX = (kodiak.getRandom().nextDouble() - 0.5) * 1.0;
                            double offsetY = (kodiak.getRandom().nextDouble() - 0.5) * 0.2;
                            double offsetZ = (kodiak.getRandom().nextDouble() - 0.5) * 1.0;

                            double velocityX = (kodiak.getRandom().nextDouble() - 0.5) * 0.4;
                            double velocityY = kodiak.getRandom().nextDouble() * 0.2;
                            double velocityZ = (kodiak.getRandom().nextDouble() - 0.5) * 0.4;

                            clientLevel.addParticle(dirtParticle,
                                    particleX + offsetX,
                                    particleY + offsetY,
                                    particleZ + offsetZ,
                                    velocityX, velocityY, velocityZ);
                        }
                    }
                }
            }

            if (kodiak.rollTimer >= 80) {
                kodiak.rollTimer = 0;
                kodiak.setRolling(false);
            }
        }

        if (isSearchingInsideChest) {
            this.setDeltaMovement(0,0,0);

            if (chestBlockEntity != null) {
                BlockPos chestPos = chestBlockEntity.getBlockPos();
                this.setLookAt(chestPos.getX(), chestPos.getY(), chestPos.getZ());
            }
        }

        if (cropCheckTimer > 0) {
            cropCheckTimer--;
            if (cropCheckTimer == 0 && targetCrop != null) {
                if (distanceRest(this, targetCrop) <= 3) {
                    numberOfBonusSearching++;

                    if (numberOfBonusSearching >= numberOfBonusSearchingMax) {
                        numberOfBonusSearching = 0;
                        this.getNavigation().stop();
                    } else {
                        goToNewCropBlock(cropRadiusSearch);
                    }
                }
                targetCrop = null;
            }
        }

        if (this.isDirty()) {
            if (dirtyTimer <= MAX_DIRTY_TIMER) {
                dirtyTimer++;
            } else {
                dirtyTimer = 0;
                setDirty(false);
            }
        }
    }

    protected void pickupItemInHisMouth(ItemStack itemStack) {
        setFoodPick(itemStack);
        if (!itemStack.isEmpty()) this.playSound(SoundEvents.ITEM_PICKUP);
    }

    protected void lookForHoneyInTheBeeNest() {
        pickupItemInHisMouth(Items.HONEYCOMB.getDefaultInstance());
        this.playSound(SoundEvents.HONEY_BLOCK_PLACE);
        this.setDirty(true);
        warnBeesAround(10);

        if (!this.level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            Vec3 lookDirection = this.getLookAngle();
            double spawnX = this.getX() + lookDirection.x * 2.0;
            double spawnY = this.getY() + 0.8;
            double spawnZ = this.getZ() + lookDirection.z * 2.0;

            for (int i = 0; i < 16; i++) {
                serverLevel.sendParticles(
                        new ItemParticleOption(ParticleTypes.ITEM, Items.HONEYCOMB.getDefaultInstance()),
                        spawnX + (this.random.nextDouble() - 0.5) * 0.5,
                        spawnY,
                        spawnZ + (this.random.nextDouble() - 0.5) * 0.5,
                        1,
                        (this.random.nextDouble() - 0.5) * 0.1,
                        this.random.nextDouble() * 0.1,
                        (this.random.nextDouble() - 0.5) * 0.1,
                        0.0
                );
            }
        }
    }

    protected void openChest(ChestBlockEntity chestBlockEntity) {
        openChestAnimation(chestBlockEntity);

        int timeForClosingChest = this.random.nextInt(2000) + 3000;

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                closeChest(chestBlockEntity);
            }
        }, timeForClosingChest);
    }

    protected void closeChest(ChestBlockEntity chestBlockEntity) {
        List<Integer> foodSlots = new ArrayList<>();

        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                closeChestAnimation(chestBlockEntity);
            }
        }, 500);

        for (int i = 0; i < chestBlockEntity.getContainerSize(); i++) {
            ItemStack item = chestBlockEntity.getItem(i);
            if (!item.isEmpty() && item.is(Tags.Items.FOODS)) {
                foodSlots.add(i);
            }
        }

        if (!foodSlots.isEmpty()) {
            int randomSlot = foodSlots.get(this.random.nextInt(foodSlots.size()));
            ItemStack itemChoose = chestBlockEntity.getItem(randomSlot);

            if (!itemChoose.isEmpty() && itemChoose.is(Tags.Items.FOODS)) {
                ItemStack itemCopy = itemChoose.copy();
                itemCopy.setCount(1);

                chestBlockEntity.removeItem(randomSlot, 1);
                chestBlockEntity.setChanged();

                if (chestBlockEntity.getLevel() != null && !chestBlockEntity.getLevel().isClientSide()) {
                    chestBlockEntity.getLevel().sendBlockUpdated(
                            chestBlockEntity.getBlockPos(),
                            chestBlockEntity.getBlockState(),
                            chestBlockEntity.getBlockState(),
                            Block.UPDATE_CLIENTS
                    );
                }

                pickupItemInHisMouth(itemCopy);
            }
        }

        isSearchingInsideChest = false;
    }

    protected void scheduleNextCropCheck(BlockPos target, int radiusToSearch) {
        this.targetCrop = target;
        this.cropRadiusSearch = radiusToSearch;
        this.cropCheckTimer = 40;
    }

    protected void goToNewCropBlock(int radiusToSearch) {
        BlockPos target = findNewCropBlock(radiusToSearch);

        if (target != null) {
            kodiak.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.15f);

            kodiak.level().scheduleTick(kodiak.blockPosition(), Blocks.AIR, 40);

            scheduleNextCropCheck(target, radiusToSearch);

        } else {
            numberOfBonusSearching = 0;
            kodiak.getNavigation().stop();
        }
    }

    protected BlockPos findNewCropBlock(int radiusToSearch) {
        BlockPos kodiakPos = kodiak.blockPosition();

        for (int x = -radiusToSearch; x <= radiusToSearch; x++) {
            for (int y = -radiusToSearch; y <= radiusToSearch; y++) {
                for (int z = -radiusToSearch; z <= radiusToSearch; z++) {
                    BlockPos pos = kodiakPos.offset(x, y, z);

                    if (distanceRest(kodiak, pos) >= 2 && kodiak.level().getBlockState(pos).is(BlockTags.CROPS)) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    protected void warnBeesAround(int radius) {
        List<Bee> bees = this.level().getEntitiesOfClass(Bee.class, this.getBoundingBox().inflate(radius));

        for (Bee bee : bees) {
            bee.setTarget(this);
        }
    }

    private void trampleCrops(BlockPos pos) {
        BlockState blockState = this.level().getBlockState(pos);

        if (blockState.getBlock() instanceof FarmBlock) {
            FarmBlock.turnToDirt(null, blockState, this.level(), pos);
        }
    }

    public void eatFoodInHisMouth(ItemStack itemStack) {
        eatingTimer = 0;
        startEatingTimer = false;
        this.playSound(SoundEvents.GENERIC_EAT);

        Vec3 lookDirection = this.getLookAngle();
        double spawnX = this.getX() + lookDirection.x * 2.0;
        double spawnY = this.getY() + 0.8;
        double spawnZ = this.getZ() + lookDirection.z * 2.0;

        if (!this.level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            for (int i = 0; i < 16; i++) {
                serverLevel.sendParticles(
                        new ItemParticleOption(ParticleTypes.ITEM, (itemStack != null && !itemStack.isEmpty()) ? itemStack : Items.APPLE.getDefaultInstance()),
                        spawnX + (this.random.nextDouble() - 0.5) * 0.5,
                        spawnY,
                        spawnZ + (this.random.nextDouble() - 0.5) * 0.5,
                        1,
                        (this.random.nextDouble() - 0.5) * 0.1,
                        this.random.nextDouble() * 0.1,
                        (this.random.nextDouble() - 0.5) * 0.1,
                        0.0
                );
            }
        } else {
            for (int i = 0; i < 16; i++) {
                this.level().addParticle(
                        new ItemParticleOption(ParticleTypes.ITEM, (itemStack != null && !itemStack.isEmpty()) ? itemStack : Items.APPLE.getDefaultInstance()),
                        spawnX + (this.random.nextDouble() - 0.5) * 0.5,
                        spawnY,
                        spawnZ + (this.random.nextDouble() - 0.5) * 0.5,
                        (this.random.nextDouble() - 0.5) * 0.1,
                        this.random.nextDouble() * 0.1,
                        (this.random.nextDouble() - 0.5) * 0.1
                );
            }
        }

        if (this.getFoodPick().is(OWTags.Items.KODIAK_DANGEROUS_FOOD)) {
            this.addEffect(new MobEffectInstance(MobEffects.POISON, 350, 0));

            if (lastPlayerWhoFeedHim != null) {
                this.setTarget(lastPlayerWhoFeedHim);
            }
        } else lastPlayerWhoFeedHim = null;

        if (this.getFoodPick().is(Items.HONEYCOMB)) {
            this.setDirty(true);
        }

        this.setFoodPick(ItemStack.EMPTY);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        if (target != null) {
            if (this.getFoodPick() != null && !this.getFoodPick().isEmpty()) {
                eatFoodInHisMouth(this.getFoodPick());
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        Item heldItem = itemStack.getItem();
        if (this.getTarget() == null && !this.isNapping() && (this.getFoodPick() == ItemStack.EMPTY || this.getFoodPick() == null)) {
            if (itemStack.is(Tags.Items.FOODS) || itemStack.is(Items.HONEYCOMB)) {
                this.pickupItemInHisMouth(heldItem.getDefaultInstance().copy());
                itemStack.shrink(1);
                lastPlayerWhoFeedHim = player;

                return InteractionResult.SUCCESS;
            }
        }

        if (this.getFoodPick() != null && !this.getFoodPick().isEmpty()) {
            if (player.getMainHandItem().isEmpty()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, this.getFoodPick().copy());
                this.setFoodPick(ItemStack.EMPTY);
                this.playSound(SoundEvents.ITEM_PICKUP);
                this.playSound((OWUtils.RANDOM(2) ? OWSounds.KODIAK_HURTING.get() : OWSounds.KODIAK_HURTING_2.get()), 1.0f, (float) OWUtils.generateRandomInterval(0.9f, 1.1f));
                setNap(false);
                this.setTarget(player);

                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    public ItemStack getFoodPick() {
        return this.entityData.get(FOOD_PICK);
    }

    public void setFoodPick(ItemStack food) {
        this.entityData.set(FOOD_PICK, food);
        if (!food.isEmpty()) {
            startEatingTimer = true;
        }
    }

    public boolean canBeTamable() {
        return isDirty() && !isNapping();
    }

    public boolean isDirty() {
        return this.entityData.get(IS_DIRTY);
    }

    public void setDirty(boolean isDirty) {
        this.entityData.set(IS_DIRTY, isDirty);
        this.playSound(SoundEvents.HONEY_BLOCK_PLACE);
    }

    protected boolean canAttack() {
        return canAttack;
    }

    protected void setCanAttack(boolean canAttack) {
        this.canAttack = canAttack;
    }

    public KodiakState getKodiakState() {
        return currentKodiakState;
    }

    public void setKodiakState(KodiakState kodiakState) {
        this.currentKodiakState = kodiakState;
        this.goalCommitmentTicks = COMMITMENT_DURATION;
    }

    public boolean isCommittedToGoal() {
        return goalCommitmentTicks > 0 && currentKodiakState != KodiakState.IDLE;
    }

    public boolean canStartNewGoal(KodiakState newState) {
        if (currentKodiakState == KodiakState.NAPPING && newState != KodiakState.NAPPING) {
            return false;
        }
        return currentKodiakState == KodiakState.IDLE || !isCommittedToGoal();
    }

    public static float distanceRest(LivingEntity livingEntity, BlockPos target) {
        float f = (float) (livingEntity.getX() - target.getX());
        float f1 = (float) (livingEntity.getY() - target.getY());
        float f2 = (float) (livingEntity.getZ() - target.getZ());
        return Mth.sqrt(f * f + f1 * f1 + f2 * f2);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        if (lastPlayerWhoFeedHim != null) {
            tag.putUUID("LastFeederUUID", lastPlayerWhoFeedHim.getUUID());
        }
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.hasUUID("LastFeederUUID")) {
            UUID feederUUID = tag.getUUID("LastFeederUUID");
            lastPlayerWhoFeedHim = this.level().getPlayerByUUID(feederUUID);
        }
    }
}
