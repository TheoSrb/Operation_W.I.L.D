package net.tiew.operationWild.entity.AI;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.goals.*;
import net.tiew.operationWild.entity.variants.KodiakVariant;

import java.util.*;
import java.util.function.Predicate;

public abstract class AIKodiak extends OWEntity {

    protected boolean canAttack = false;
    public ItemStack foodPick = ItemStack.EMPTY;

    private boolean startEatingTimer = false;
    private boolean startHoneyTimer = false;
    private static final int MAX_EATING_TIMER = 400;
    private static final int MAX_HONEY_TIMER = 750;
    private int eatingTimer = 0;
    private int honeyTimer = 0;

    private static final EntityDataAccessor<ItemStack> FOOD_PICK = SynchedEntityData.defineId(AIKodiak.class, EntityDataSerializers.ITEM_STACK);

    protected AIKodiak(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.registerBasicsGoals();

        this.goalSelector.addGoal(3, new KodiakAttractedToGoal<>(this, (KodiakEntity) this, Blocks.BEE_NEST,
              1.75f,  25, 2.0f, this::lookForHoneyInTheBeeNest, true));
        this.goalSelector.addGoal(4, new KodiakAttractedToGoal<>(this, (KodiakEntity) this, Blocks.CAMPFIRE,
                1.0f, 60, 1.0f, () -> pickupItemInHisMouth(foodPick), this.getFoodPick().isEmpty()));
        this.goalSelector.addGoal(5, new NapGoal((KodiakEntity) this, 1.5f, 700, true));

    }

    private void registerBasicsGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new OWAttackGoal(this, this.getSpeed() * 30f, 8, 3, canAttack()));
        this.goalSelector.addGoal(6, new OWBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new OWRandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.75D));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FOOD_PICK, ItemStack.EMPTY);
    }

    @Override
    public void tick() {
        super.tick();
        boolean hasSomethingInHisMouth = getFoodPick() != null && !getFoodPick().isEmpty();

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
                        warnBeesAround(10);
                    }
                }
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

        // TODO particles don't render.
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            double x = this.getX();
            double y = this.getY() + 0.5;
            double z = this.getZ();

            serverLevel.sendParticles(ParticleTypes.DRIPPING_HONEY, x, y, z, 15, 0.5, 0.3, 0.5, 0.1);
        }
    }

    protected void warnBeesAround(int radius) {
        List<Bee> bees = this.level().getEntitiesOfClass(Bee.class, this.getBoundingBox().inflate(radius));

        for (Bee bee : bees) {
            bee.setTarget(this);
        }
    }

    protected void eatFoodInHisMouth(ItemStack itemStack) {
        eatingTimer = 0;
        startEatingTimer = false;
        this.playSound(SoundEvents.GENERIC_EAT);

        if (!this.level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            Vec3 lookDirection = this.getLookAngle();
            double spawnX = this.getX() + lookDirection.x * 2.0;
            double spawnY = this.getY() + 0.8;
            double spawnZ = this.getZ() + lookDirection.z * 2.0;

            for (int i = 0; i < 16; i++) {
                serverLevel.sendParticles(
                        new ItemParticleOption(ParticleTypes.ITEM, itemStack != null ? itemStack : Items.APPLE.getDefaultInstance()),
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

        this.setFoodPick(ItemStack.EMPTY);
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

    protected boolean canAttack() {
        return canAttack;
    }

    protected void setCanAttack(boolean canAttack) {
        this.canAttack = canAttack;
    }


    public static class KodiakAttractedToGoal<T> extends Goal {

        private final AIKodiak aiKodiak;
        private final KodiakEntity kodiak;
        private final T target;
        private final float speedModifier;
        private final int radiusToSearch;
        private final float attractionFrequencyMultiplier;
        private final Runnable actionAtTheEnd;
        private final boolean conditionToWork;

        private BlockPos targetPos;
        protected List<ItemStack> campfireItems = new ArrayList<>();
        private ItemStack foodPick = ItemStack.EMPTY;

        public KodiakAttractedToGoal(AIKodiak aiKodiak, KodiakEntity kodiak, T target, float speedModifier, int radiusToSearch, float attractionFrequencyMultiplier, Runnable actionAtTheEnd, boolean conditionToWork) {
            this.aiKodiak = aiKodiak;
            this.kodiak = kodiak;
            this.target = target;
            this.speedModifier = speedModifier;
            this.radiusToSearch = radiusToSearch;
            this.conditionToWork = conditionToWork;
            this.actionAtTheEnd = actionAtTheEnd;
            this.attractionFrequencyMultiplier = attractionFrequencyMultiplier;

            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public void tick() {
            if (!kodiak.getFoodPick().isEmpty()) {
                stop();
                return;
            }
            if (targetPos != null) {
                if (target instanceof Block && ((Block) target) == Blocks.CAMPFIRE) {
                    BlockEntity blockEntity = kodiak.level().getBlockEntity(targetPos);
                    if (blockEntity instanceof CampfireBlockEntity campfire) {
                        campfireItems = campfire.getItems();
                        if (!campfireItems.isEmpty() || kodiak.level().isNight()) {
                            kodiak.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), speedModifier);

                            double distanceBetweenKodiakAndTarget = distanceRest(kodiak, targetPos);
                            boolean isArrived = distanceBetweenKodiakAndTarget <= 4;

                            if (isArrived) {
                                if (campfireItems != null) {
                                    int $$0 = kodiak.getRandom().nextInt(campfireItems.size());
                                    foodPick = campfireItems.get($$0);
                                    campfire.getItems().set($$0, ItemStack.EMPTY);
                                    campfire.setChanged();
                                    if (!kodiak.level().isClientSide) {
                                        BlockState state = kodiak.level().getBlockState(targetPos);
                                        kodiak.level().sendBlockUpdated(targetPos, state, state, Block.UPDATE_CLIENTS);
                                    }
                                    aiKodiak.foodPick = foodPick;
                                }
                                actionAtTheEnd.run();
                                stop();
                            }

                        } else {
                            stop();
                            return;
                        }
                    }
                } else {
                    kodiak.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), speedModifier);
                    double distanceBetweenKodiakAndTarget = distanceRest(kodiak, targetPos);
                    boolean isArrived = distanceBetweenKodiakAndTarget <= 4;

                    if (isArrived) {
                        actionAtTheEnd.run();
                        stop();
                    }
                }
            }
        }

        @Override
        public void start() {
            targetPos = findTargetPos(target);
        }

        @Override
        public void stop() {
            super.stop();
        }

        @Override
        public boolean canContinueToUse() {
            if (targetPos == null || !conditionToWork) return false;

            double distance = distanceRest(kodiak, targetPos);

            if (target instanceof Block && ((Block) target) == Blocks.CAMPFIRE) {
                return distance > 2 && kodiak.getFoodPick().isEmpty();
            } else {
                return distance > 3;
            }
        }

        @Override
        public boolean canUse() {
            return kodiak.getRandom().nextInt((int) (200 / attractionFrequencyMultiplier)) == 0
                    && kodiak.getTarget() == null && kodiak.onGround()
                    && !kodiak.isNapping() && conditionToWork;
        }

        public T getTarget() {
            return target;
        }

        private BlockPos findTargetPos(T target) {
            BlockPos kodiakPos = kodiak.blockPosition();

            for (int x = -radiusToSearch; x <= radiusToSearch; x++) {
                for (int y = -radiusToSearch; y <= radiusToSearch; y++) {
                    for (int z = -radiusToSearch; z <= radiusToSearch; z++) {
                        BlockPos pos = kodiakPos.offset(x, y, z);
                        if (target instanceof Block) {
                            if (kodiak.level().getBlockState(pos).is((Block) target)) {
                                if (kodiak.level().getBlockState(pos).is(Blocks.CAMPFIRE)) {
                                    if (distanceRest(kodiak, pos) >= (float) radiusToSearch / 3) {
                                        return pos;
                                    }
                                } else {
                                    return pos;
                                }
                            }
                        } else if (target instanceof ItemEntity itemEntity) {
                            if (kodiak.distanceToSqr(itemEntity) <= radiusToSearch * radiusToSearch) {
                                return itemEntity.blockPosition();
                            }
                        } else if (target instanceof LivingEntity entity) {
                            if (kodiak.distanceToSqr(entity) <= radiusToSearch * radiusToSearch) {
                                return entity.blockPosition();
                            }
                        }
                    }
                }
            }
            return null;
        }

        public float distanceRest(LivingEntity livingEntity, BlockPos target) {
            float f = (float)(livingEntity.getX() - target.getX());
            float f1 = (float)(livingEntity.getY() - target.getY());
            float f2 = (float)(livingEntity.getZ() - target.getZ());
            return Mth.sqrt(f * f + f1 * f1 + f2 * f2);
        }
    }
}
