package net.tiew.operationWild.entity;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.tiew.operationWild.entity.animals.aquatic.*;
import net.tiew.operationWild.entity.animals.terrestrial.*;
import net.tiew.operationWild.entity.config.IOWEntity;
import net.tiew.operationWild.entity.config.IOWRideable;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.goals.global.OWFollowOwnerGoal;
import net.tiew.operationWild.entity.goals.global.OWLookAtPlayerGoal;
import net.tiew.operationWild.entity.misc.*;
import net.tiew.operationWild.entity.quests.ascent.AscentMission;
import net.tiew.operationWild.entity.variants.*;
import net.tiew.operationWild.networking.packets.to_client.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.bosses.PlantEmpressEntity;
import net.tiew.operationWild.entity.quests.daily_quests.DailyQuest;
import net.tiew.operationWild.entity.quests.daily_quests.DailyQuestRegistry;
import net.tiew.operationWild.entity.quests.daily_quests.DailyQuestsDate;
import net.tiew.operationWild.event.ClientEvents;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.ConsumeItemPacket;
import net.tiew.operationWild.screen.entity.OWInventoryMenu;
import net.tiew.operationWild.screen.entity.submarine.SeaBugInventoryMenu;
import net.tiew.operationWild.screen.player.OWEntityJournalScreen;
import net.tiew.operationWild.sound.OWSounds;
import net.tiew.operationWild.core.OWUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static net.tiew.operationWild.core.OWUtils.RANDOM;
import static net.tiew.operationWild.core.OWUtils.generateRandomInterval;

public class OWEntity extends TamableAnimal implements MenuProvider, IOWEntity, IOWTamable, IOWRideable {

    public float averageScale;
    public static final Random RANDOM = new Random();
    public LivingEntity TRAPPED_ENTITY = null;
    public int intervallNapTimer = 0;
    public int napTimer = 0;
    private float lastPlay;
    public DamageSource damageSource = this.damageSources().mobAttack(this);
    public int numberFeedsGiven = 0;
    private final ItemStackHandler itemStackHandler = new ItemStackHandler(2);
    public final ItemStackHandler itemStackHandlerSeaBug = new ItemStackHandler(15);
    public int attackTimer;
    public int comboTimer;
    private int runTime;
    public int chance = random.nextInt(100);
    private int fightingTime = 200;
    public boolean canAttack = true;
    private BlockPos lastPosition;
    public LivingEntity lastVisibleTarget = null;
    public int questsReUpdatingTimer = 10;
    private int sittingCooldown = 0;
    private float customWidth = 1.0F;
    private float customHeight = 1.0F;
    public boolean canShowVitalEnergyLack = false;
    private int noJumpDelay;
    private float currentSpeed = 0;
    private float targetSpeed = 0;

    public int continueComboMaxTimer = 0;
    public int actualAttackNumber = 0;
    public final int MAX_ATTACKS_IN_COMBO = 3;

    public static float comboSpeedMultiplier = 1.0f;

    public static final float SAVAGE_ENTITY_DAMAGE_MULTIPLIER = 1.4f;

    private static int killCounter = 0;
    private static int killCounter2 = 0;
    private static int hitCounter = 0;
    private static long lastKillTime = 0;
    private static long lastKillTime2 = 0;
    private static long lastHurtTime = 0;
    private int healAmount = 0;
    private int hurtAmount = 0;
    private int sleepBarDownSpeed;
    public int maxSleepBar;
    public float maxHealthBeforeResurrection;
    public int resurrectionTimer = 0;
    public float actualMaturation = 0;
    public float maxHealth;
    public float maxMaturation;
    public static final int DELAY_BEFORE_BABY_TASK_MAX = 18000;
    public int delayBeforeBabyTask = 0;
    public boolean babyQuestIsInProgress = false;
    public int babyQuestProgressTimer = 0;
    public Item choosenFood = null;
    public int ultimateCooldown = 0;

    public Map<Integer, String> babyQuests = new HashMap<>();
    public int choosenQuest;
    public String choosenQuestStr;

    public int foodGiven = 0;
    public int foodWanted;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState sittingAnimationState = new AnimationState();
    public int idleAnimationTimeout = 0;
    public int attackAnimationTimeout = 0;
    public int sittingAnimationTimeout = 0;

    public AnimationState transitionIdleSit = new AnimationState();
    public AnimationState transitionSitIdle = new AnimationState();
    public AnimationState transitionIdleSleep = new AnimationState();
    public AnimationState transitionSleepIdle = new AnimationState();

    public boolean playerContinueCombo = false;
    public AnimationState attackState = new AnimationState();
    public AnimationState attackState2 = new AnimationState();
    public AnimationState attackState3 = new AnimationState();

    public static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> SITTING = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> SADDLED = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_RUNNING = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_ATTACKING = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_IN_FIGHT = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Float> XP = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Integer> STAGE = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> PRESTIGE_STAGE = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> LEVEL = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> PRESTIGE_LEVEL = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Float> ACCELERATION = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Integer> LEVEL_POINTS = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> VITAL_ENERGY = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> PREPARE_NAP = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> NAPPING = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_COMBO = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> BODY_Z_ROT = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BODY_X_ROT = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BODY_Y_OFFSET = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Boolean> IS_COMBO_PAUSED = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> GET_COMBO_ATTACK = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_FALLING = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Float> BASE_HEALTH = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> BASE_DAMAGE = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> BASE_SPEED = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> DAMAGE_TO_CLIENT = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Boolean> IS_FED = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_FEMALE = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_PASSIVE = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> NECKLACE_COLOR = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<ItemStack> ITEM_FOOD = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> FOOD_COUNT = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> AUTO_PICKUP = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> QUESTS_ARE_UPDATED = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> TAMING_PERCENTAGE = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> MATURATION_PERCENTAGE = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_SLEEPING = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ULTIMATE = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ACTUAL_SLEEPING_BAR = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RESURRECTION_MAX_TIMER = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_SLEEPING_BAR = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> RE_UPDATED_QUESTS = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_IN_RESURRECTION = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CAN_DROP_SOUL = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_BABY = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ATTACK_ANIMATION_ID = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_ANIMATION_TICK = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);

    public int quest0Progression = 0;
    public int quest1Progression = 0;
    public int quest2Progression = 0;
    public int quest3Progression = 0;
    public int quest4Progression = 0;
    public int quest5Progression = 0;
    public int quest6Progression = 0;
    public int quest7Progression = 0;
    public int quest8Progression = 0;
    public int quest9Progression = 0;
    public int quest10Progression = 0;

    public boolean quest0isLocked = false;
    public boolean quest1isLocked = false;
    public boolean quest2isLocked = false;
    public boolean quest3isLocked = false;
    public boolean quest4isLocked = false;
    public boolean quest5isLocked = false;
    public boolean quest6isLocked = false;
    public boolean quest7isLocked = false;
    public boolean quest8isLocked = false;
    public boolean quest9isLocked = false;
    public boolean quest10isLocked = false;

    protected OWEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level);
        averageScale = scale;
        this.lastPosition = this.blockPosition();
        this.sleepBarDownSpeed = sleepBarDownSpeed;
        this.maxSleepBar = maxSleepBar;

        babyQuests.put(0, "quest.babyQuest0");
        babyQuests.put(1, "quest.babyQuest1");
        babyQuests.put(2, "quest.babyQuest2");
    }

    protected void registerGoals() {
        this.registerBehaviorGoals(this);

        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new OWFollowOwnerGoal(this, this.getSpeed() * 30f, 15, 3));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25));
        this.goalSelector.addGoal(6, new OWLookAtPlayerGoal(this, Player.class, 6.0F));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
    }

    public static final List<Class<?>> TANK_ENTITIES = new ArrayList<>();
    public static final List<Class<?>> ASSASSIN_ENTITIES = new ArrayList<>();
    public static final List<Class<?>> MARAUDER_ENTITIES = new ArrayList<>();

    public static final List<Object> CARNIVOROUS_ENTITIES = List.of(
            OWEntityRegistry.TIGER.get(),
            OWEntityRegistry.HYENA.get(),
            OWEntityRegistry.BOA.get(),
            OWEntityRegistry.CHAMELEON.get(),
            OWEntityRegistry.JELLYFISH.get(),
            OWEntityRegistry.KODIAK.get(),
            OWEntityRegistry.MANDRILL.get(),
            OWEntityRegistry.TIGER_SHARK.get(),
            OWEntityRegistry.WALRUS.get()
    );

    public static final List<Object> VEGETARIAN_ENTITIES = List.of(
            OWEntityRegistry.ELEPHANT.get(),
            OWEntityRegistry.MANTA.get(),
            OWEntityRegistry.PEACOCK.get(),
            OWEntityRegistry.RED_PANDA.get()
    );

    public float getBaseHealth() { return this.entityData.get(BASE_HEALTH);}
    public void setBaseHealth(float health) { this.entityData.set(BASE_HEALTH, health);}
    public float getBaseDamage() { return this.entityData.get(BASE_DAMAGE);}
    public void setBaseDamage(float damage) { this.entityData.set(BASE_DAMAGE, damage);}
    public float getBaseSpeed() { return this.entityData.get(BASE_SPEED);}
    public void setBaseSpeed(float speed) { this.entityData.set(BASE_SPEED, speed);}

    private void executeQuestProgression(byte id) {
        if (!this.isTame() || this.isInResurrection() || this.isBaby()) return;

        if (id == 0 && !this.quest0isLocked) {
            this.quest0Progression += (int) this.getDamage();
            if (quest0Progression >= 300) this.finishQuest((byte) 0);
        }
        if (id == 1 && !this.quest1isLocked) {
            this.quest1Progression += hurtAmount;
            if (quest1Progression >= 200) this.finishQuest((byte) 1);
        }
        if (id == 2 && !this.quest2isLocked) {
            this.quest2Progression += healAmount;
            if (quest2Progression >= 100) this.finishQuest((byte) 2);
        }
        if (id == 3 && !this.quest3isLocked) {
            BlockPos currentPosition = this.blockPosition();
            if (!currentPosition.equals(lastPosition)) {
                this.quest3Progression++;
                lastPosition = currentPosition;
            }
            if (quest3Progression >= 2000) this.finishQuest((byte) 3);
        }
        if (id == 4 && !this.quest4isLocked) {
            this.quest4Progression++;
            if (quest4Progression >= 1) this.finishQuest((byte) 4);
        }
        if (id == 5 && !this.quest5isLocked) {
            this.quest5Progression++;
            if (quest5Progression >= 1) this.finishQuest((byte) 5);
        }
        if (id == 6 && !this.quest6isLocked) {
            this.quest6Progression++;
            if (quest6Progression >= 25) this.finishQuest((byte) 6);
        }
        if (id == 7 && !this.quest7isLocked) {
            this.quest7Progression++;
            if (quest7Progression >= 1) this.finishQuest((byte) 7);
        }
        if (id == 8 && !this.quest8isLocked) {
            this.quest8Progression++;
            if (quest8Progression >= 1) this.finishQuest((byte) 8);
        }
        if (id == 9 && !this.quest9isLocked) {
            this.quest9Progression += (int) this.getDamage();
            if (quest9Progression >= 50) this.finishQuest((byte) 9);
        }
        if (id == 10 && !this.quest10isLocked) {
            this.quest10Progression ++;
            if (quest10Progression >= 8) this.finishQuest((byte) 10);
        }
    }

    private void finishQuest(byte id) {
        if (!this.isTame()) return;
        double pitch = OWUtils.generateRandomInterval(0.8, 1.3);
        float xpReward = 0;

        if (id == 0) {
            this.quest0Progression = 0;
            this.quest0isLocked = true;
            xpReward = 7.5f;
        }
        if (id == 1) {
            this.quest1Progression = 0;
            this.quest1isLocked = true;
            xpReward = 7.5f;
        }
        if (id == 2) {
            this.quest2Progression = 0;
            this.quest2isLocked = true;
            xpReward = 7.5f;
        }
        if (id == 3) {
            this.quest3Progression = 0;
            this.quest3isLocked = true;
            xpReward = 7.5f;
        }
        if (id == 4) {
            this.quest4Progression = 0;
            this.quest4isLocked = true;
            xpReward = 7.5f;
        }
        if (id == 5) {
            this.quest5Progression = 0;
            this.quest5isLocked = true;
            xpReward = 7.5f;
        }
        if (id == 6) {
            this.quest6Progression = 0;
            this.quest6isLocked = true;
            xpReward = 7.5f;
        }
        if (id == 7) {
            this.quest7Progression = 0;
            this.quest7isLocked = true;
            xpReward = 7.5f;
        }
        if (id == 8) {
            this.quest8Progression = 0;
            this.quest8isLocked = true;
            xpReward = 7.5f;
        }
        if (id == 9) {
            this.quest9Progression = 0;
            this.quest9isLocked = true;
            xpReward = 7.5f;
        }
        if (id == 10) {
            this.quest10Progression = 0;
            this.quest10isLocked = true;
            xpReward = 7.5f;
        }

        if (this.getLevel() >= 50) {
            int xpStage = this.getPrestigeLevel() / 2;
            int prestigeReward = (int) (xpReward * 3);
            setPrestigeXpStage(xpStage);

            while (prestigeReward > 0 && this.getLevel() >= 50 && this.getPrestigeLevel() < 999) {
                float xpStageRest = getPrestigeXpStage() - this.getXp();

                if (prestigeReward >= xpStageRest) {
                    prestigeReward -= (int) xpStageRest;
                    passingPrestigeLevel(true);
                } else {
                    this.setXp(this.getXp() + prestigeReward);
                    prestigeReward = 0;
                }
            }
        } else {
            int xpStage = OWUtils.generateExponentialExp(this.getLevel(), 20);
            setXpStage(xpStage);
            boolean hasLeveledUp = false;

            while (xpReward > 0 && this.getLevel() < 50) {
                float xpStageRest = getXpStage() - this.getXp();

                if (xpReward >= xpStageRest) {
                    xpReward -= xpStageRest;
                    passingLevel(!hasLeveledUp);
                    hasLeveledUp = true;

                } else {
                    this.setXp(this.getXp() + xpReward);
                    xpReward = 0;
                }
            }
        }
        this.playSound(OWSounds.TAME_SUCCESS.get(), 1.0f, (float) pitch);
    }

    public boolean isQuestInProgress(DailyQuest quest) {
        if (quest == null) return false;

        return (DailyQuestsDate.savedQuests[0] != null && Objects.equals(DailyQuestsDate.savedQuests[0].getName(), quest.getName())) ||
                (DailyQuestsDate.savedQuests[1] != null && Objects.equals(DailyQuestsDate.savedQuests[1].getName(), quest.getName())) ||
                (DailyQuestsDate.savedQuests[2] != null && Objects.equals(DailyQuestsDate.savedQuests[2].getName(), quest.getName()));
    }

    public int getState() { return this.entityData.get(STATE);}

    public void setState(int state) { this.entityData.set(STATE, state);}

    public boolean isFalling() { return this.entityData.get(IS_FALLING);}

    public void setFalling(boolean isFalling) { this.entityData.set(IS_FALLING, isFalling);}

    public boolean isPassive() { return this.entityData.get(IS_PASSIVE);}

    public void setPassive(boolean isPassive) { this.entityData.set(IS_PASSIVE, isPassive);}

    public float getAcceleration() { return this.entityData.get(ACCELERATION);}

    public void setAcceleration(float getAcceleration) { this.entityData.set(ACCELERATION, getAcceleration);}

    public int getNecklaceColor() { return this.entityData.get(NECKLACE_COLOR);}

    public void setNecklaceColor(int necklaceColor) { this.entityData.set(NECKLACE_COLOR, necklaceColor);}

    public void setSitting(boolean sitting) {
        this.entityData.set(SITTING, sitting); this.setOrderedToSit(sitting);
        if (sitting) {
            this.ejectPassengers();
        }
    }

    public boolean isSitting() { return this.entityData.get(SITTING);}

    public void setUltimate(boolean isUltimate) { this.entityData.set(ULTIMATE, isUltimate);}

    public boolean isUltimate() { return this.entityData.get(ULTIMATE);}

    public float getXp() { return this.entityData.get(XP);}

    public void setXp(float xp) { this.entityData.set(XP, xp);}

    public boolean isBaby() { return this.entityData.get(IS_BABY);}

    public void setBaby(boolean isBaby) { this.entityData.set(IS_BABY, isBaby);}

    public int getXpStage() { return this.entityData.get(STAGE);}

    public void setXpStage(int xpStage) { this.entityData.set(STAGE, xpStage);}

    public int getPrestigeXpStage() { return this.entityData.get(PRESTIGE_STAGE);}

    public void setPrestigeXpStage(int prestigeXpStage) { this.entityData.set(PRESTIGE_STAGE, prestigeXpStage);}

    public int getLevel() { return this.entityData.get(LEVEL);}

    public void setLevel(int level) { this.entityData.set(LEVEL, level);}

    public int getPrestigeLevel() { return this.entityData.get(PRESTIGE_LEVEL);}

    public void setPrestigeLevel(int prestigeLevel) { this.entityData.set(PRESTIGE_LEVEL, prestigeLevel);}

    public int getLevelPoints() { return this.entityData.get(LEVEL_POINTS);}

    public void setLevelPoints(int level) { this.entityData.set(LEVEL_POINTS, level);}

    public boolean canReUpdatedDailyQuests() { return this.entityData.get(RE_UPDATED_QUESTS);}

    public void setReUpdatedDailyQuests(boolean canReUpdatedDailyQuests) { this.entityData.set(RE_UPDATED_QUESTS, canReUpdatedDailyQuests);}

    public double getResurrectionPercentage() {
        if (this.resurrectionTimer <= 0 || this.getResurrectionMaxTimer() <= 0) return 0;
        return ((double) this.resurrectionTimer / this.getResurrectionMaxTimer()) * 100;
    }

    public int getTamingPercentage() { return this.entityData.get(TAMING_PERCENTAGE);}

    public void setTamingPercentage(int actualTaming, int maxTaming) {
        if (actualTaming <= 0 && maxTaming <= 0 && this.isTame()) return;
        this.entityData.set(TAMING_PERCENTAGE, (int)(((double) actualTaming / maxTaming) * 100));
    }

    public float getMaturationPercentage() { return this.entityData.get(MATURATION_PERCENTAGE);}

    public void setMaturationPercentage(float actualMaturation, float maxMaturation) {
        if (actualMaturation <= 0 && maxMaturation <= 0 && this.isTame()) return;
        this.entityData.set(MATURATION_PERCENTAGE, (((float) actualMaturation / maxMaturation) * 100));
    }

    public int getSleepBarPercent() { return (int) (((double) getActualSleepingBar() / getMaxSleepingBar()) * 100);}

    public void resetSleepBar() { setActualSleepingBarTo(0); }

    public boolean isInFight() { return this.entityData.get(IS_IN_FIGHT);}

    public void setFighting(boolean isInFight) {
        if (isBaby()) isInFight = false;
        this.entityData.set(IS_IN_FIGHT, isInFight);
    }

    public float getDamageToClient() { return this.entityData.get(DAMAGE_TO_CLIENT);}

    public void setDamageToClient(float damage) { this.entityData.set(DAMAGE_TO_CLIENT, damage);}

    public boolean questsAreUpdated() { return this.entityData.get(QUESTS_ARE_UPDATED);}

    public void setUpdatingQuests(boolean questsAreUpdated) { this.entityData.set(QUESTS_ARE_UPDATED, questsAreUpdated);}

    public ItemStackHandler getInventory() {
        return this.itemStackHandler;
    }

    public void passingLevel(boolean playSound) {
        setXp(Math.max(0, this.getXp() - this.getXpStage()));
        this.setLevel(this.getLevel() + 1);
        this.setLevelPoints(this.getLevelPoints() + 1);
        ExperienceOrb orb = new ExperienceOrb(this.level(), this.getX(), this.getY(), this.getZ(),  this.getLevel());
        this.level().addFreshEntity(orb);

        if (playSound) {
            this.playSound(SoundEvents.PLAYER_LEVELUP);
        }

        if (this.getLevel() >= 50) this.setPrestigeXpStage(3);

        LivingEntity rider = this.getControllingPassenger();
        if (rider != null) {
            if (rider instanceof ServerPlayer serverPlayer) {
                OWUtils.showTitle(serverPlayer, "tooltip.levelUp", TextColor.fromRgb(0x00FF00), String.valueOf(this.getLevel() - 1) + " ➾ " + this.getLevel(), TextColor.fromRgb(0xb8e45a));
            }
        }
        int xpStage = OWUtils.generateExponentialExp(this.getLevel(), 20);
        setXpStage(xpStage);
        if (isQuestInProgress(DailyQuestRegistry.quest6) && !this.level().isClientSide()) {
            this.executeQuestProgression((byte) 5);
        }
    }

    public void passingPrestigeLevel(boolean playSound) {
        setXp(Math.max(0, this.getXp() - this.getPrestigeXpStage()));
        this.setPrestigeLevel(this.getPrestigeLevel() + 1);
        if (playSound) {
            this.playSound(SoundEvents.PLAYER_LEVELUP);
        }

        LivingEntity rider = this.getControllingPassenger();
        if (rider != null) {
            if (rider instanceof ServerPlayer serverPlayer) {
                OWUtils.showTitle(serverPlayer, "tooltip.prestigeLevelUp", TextColor.fromRgb(0x96efe3), String.valueOf(this.getPrestigeLevel() - 1) + " ➾ " + this.getPrestigeLevel(), TextColor.fromRgb(0x61b0b4));
            }
        }
        int xpStage = this.getPrestigeLevel() / 2;
        setPrestigeXpStage(xpStage);
    }

    public boolean causeFallDamage(float v, float v1, DamageSource damageSource) {
        int i = this.calculateFallDamage(v, v1);
        if (i <= 0) {
            return false;
        } else {
            this.hurt(damageSource, (float)i);
            if (this.isVehicle()) {
                for(Entity entity : this.getIndirectPassengers()) {
                    entity.hurt(damageSource, 0);
                }
            }

            this.playBlockFallSound();
            return true;
        }
    }

    public void healWithFavoriteFood(float healMultiplier, boolean preferRawMeat, boolean preferCookedMeat) {
        ItemStack food = this.getItemFood();
        if (CARNIVOROUS_ENTITIES.contains(this.getType())) {
            if (food.is(ItemTags.MEAT) || food.is(ItemTags.FISHES)) {
                if (preferRawMeat) {
                    if (food.is(Tags.Items.FOODS_RAW_MEAT) || food.is(Tags.Items.FOODS_RAW_FISH)) {
                        this.heal(4 * healMultiplier);
                        healAmount = (int) (4 * healMultiplier);
                        if (isQuestInProgress(DailyQuestRegistry.quest3) && !this.level().isClientSide()) {
                            this.executeQuestProgression((byte) 2);
                        }
                    } else {
                        this.heal(2 * healMultiplier);
                        healAmount = (int) (2 * healMultiplier);
                        if (isQuestInProgress(DailyQuestRegistry.quest3) && !this.level().isClientSide()) {
                            this.executeQuestProgression((byte) 2);
                        }
                    }
                    this.playSound(SoundEvents.CAMEL_EAT);
                } else if (preferCookedMeat) {
                    if (food.is(Tags.Items.FOODS_COOKED_MEAT) || food.is(Tags.Items.FOODS_COOKED_FISH)) {
                        this.playSound(SoundEvents.CAMEL_EAT);
                        this.heal(4 * healMultiplier);
                        healAmount = (int) (4 * healMultiplier);
                        if (isQuestInProgress(DailyQuestRegistry.quest3) && !this.level().isClientSide()) {
                            this.executeQuestProgression((byte) 2);
                        }
                    } else {
                        this.heal(2 * healMultiplier);
                        healAmount = (int) (2 * healMultiplier);
                        if (isQuestInProgress(DailyQuestRegistry.quest3) && !this.level().isClientSide()) {
                            this.executeQuestProgression((byte) 2);
                        }
                    }
                    this.playSound(SoundEvents.CAMEL_EAT);
                }
            }
        } else if (food.is(Tags.Items.FOODS_VEGETABLE)) {
            this.playSound(SoundEvents.CAMEL_EAT);
            this.heal(3 * healMultiplier);
            healAmount = (int) (3 * healMultiplier);
            if (isQuestInProgress(DailyQuestRegistry.quest3) && !this.level().isClientSide()) {
                this.executeQuestProgression((byte) 2);
            }
        }
    }

    protected int calculateFallDamage(float v, float v1) {
        return Mth.ceil((v * 0.5F - 3.0F) * v1);
    }

    public float getScale() { return this.entityData.get(SCALE);}

    public void setScale(float scale) { this.entityData.set(SCALE, scale);}

    public boolean isFemale() { return this.entityData.get(IS_FEMALE);}

    public boolean isMale() { return !isFemale();}

    public void setGender(int gender) { this.entityData.set(IS_FEMALE, gender == 0);}

    public void setPrepareNap(boolean prepareNap) { this.entityData.set(PREPARE_NAP, prepareNap);}

    public boolean isPreparingNapping() { return this.entityData.get(PREPARE_NAP);}

    public void setAttacking(boolean isAttacking) {
        this.entityData.set(IS_ATTACKING, isAttacking);
    }

    public boolean isAttacking() { return this.entityData.get(IS_ATTACKING);}

    public void setRunning(boolean isRunning) {
        this.entityData.set(IS_RUNNING, isRunning);
    }

    public boolean isRunning() { return this.entityData.get(IS_RUNNING);}

    public float getVitalEnergy() { return this.entityData.get(VITAL_ENERGY);}

    public void setVitalEnergy(float getVitalEnergy) { this.entityData.set(VITAL_ENERGY, getVitalEnergy);}

    public void setItemFood(ItemStack food) {
        this.entityData.set(ITEM_FOOD, food);
        this.setFoodCount(food.isEmpty() ? 0 : food.getCount());
    }

    public ItemStack getItemFood() { return this.entityData.get(ITEM_FOOD);}

    public int getFoodCount() { return this.entityData.get(FOOD_COUNT);}

    public void setFoodCount(int count) { this.entityData.set(FOOD_COUNT, count);}

    public int getResurrectionMaxTimer() { return this.entityData.get(RESURRECTION_MAX_TIMER);}

    public void setResurrectionMaxTimer(int maxTimer) { this.entityData.set(RESURRECTION_MAX_TIMER, maxTimer);}

    public boolean canDropSoul() { return this.entityData.get(CAN_DROP_SOUL);}

    public void setCanDropSoul(boolean canDropSoul) { this.entityData.set(CAN_DROP_SOUL, canDropSoul);}

    public boolean isInResurrection() { return this.entityData.get(IS_IN_RESURRECTION);}

    public void setResurrection(boolean isInResurrection) { this.entityData.set(IS_IN_RESURRECTION, isInResurrection);}

    public void setSleeping(boolean isSleeping) {
        this.entityData.set(IS_SLEEPING, isSleeping);
        if (isSleeping) {
            if (this instanceof TigerEntity tiger) tiger.setMad(false);
        }
    }

    public boolean isSleeping() { return this.entityData.get(IS_SLEEPING);}

    public void setMaxSleepingBarTo(int maxSleep) { this.entityData.set(MAX_SLEEPING_BAR, maxSleep);}

    public int getMaxSleepingBar() { return this.entityData.get(MAX_SLEEPING_BAR);}

    public void setActualSleepingBarTo(int actualSleepingBar) { this.entityData.set(ACTUAL_SLEEPING_BAR, actualSleepingBar);}

    public int getActualSleepingBar() { return this.entityData.get(ACTUAL_SLEEPING_BAR);}

    public void setBodyZRot(float getBodyZRot) {
        if (this.hasEffect(OWEffects.FEAR_EFFECT.getDelegate())) this.entityData.set(BODY_Z_ROT, 0.0f);
        else this.entityData.set(BODY_Z_ROT, getBodyZRot);
    }
    public float getBodyZRot() { return this.entityData.get(BODY_Z_ROT);}

    public void setBodyXRot(float getBodyXRot) {
        if (this.hasEffect(OWEffects.FEAR_EFFECT.getDelegate())) this.entityData.set(BODY_X_ROT, 0.0f);
        else this.entityData.set(BODY_X_ROT, getBodyXRot);
    }
    public float getBodyXRot() { return this.entityData.get(BODY_X_ROT);}

    public void setBodyYOffset(float getBodyXRot) { this.entityData.set(BODY_Y_OFFSET, getBodyXRot);}
    public float getBodyYOffset() { return this.entityData.get(BODY_Y_OFFSET);}

    public boolean ownerIsRiding() {
        if (this.getOwner() != null) {
            LivingEntity rider = this.getControllingPassenger();
            if (rider != null) return true;
        }
        return false;
    }

    public void setNap(boolean nap) { this.entityData.set(NAPPING, nap);}
    public boolean isNapping() { return this.entityData.get(NAPPING);}

    public float getSpeed() { return (float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED);}
    public float getDamage() { return (float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);}

    public void createLevelSystem(LivingEntity target) {
        if (!isTame() || this.getLevel() >= 50) return;

        float xpAmount = target.getMaxHealth() / 8;
        int xpStage = OWUtils.generateExponentialExp(this.getLevel(), 20);
        setXpStage(xpStage);
        boolean hasLeveledUp = false;

        while (xpAmount > 0 && this.getLevel() < 50) {
            float xpStageRest = getXpStage() - this.getXp();

            if (xpAmount >= xpStageRest) {
                xpAmount -= xpStageRest;
                passingLevel(!hasLeveledUp);
                hasLeveledUp = true;

                if (this.getLevel() >= 50) {
                    this.setXp(0);
                    int prestigeXpStage = OWUtils.generateExponentialExp(this.getPrestigeLevel(), 2);
                    setPrestigeXpStage(prestigeXpStage);
                    return;
                }

            } else {
                this.setXp(this.getXp() + xpAmount);
                xpAmount = 0;
            }
        }
    }

    public void createPrestigeLevelSystem(LivingEntity target) {
        if (!isTame() || this.getLevel() < 50 || this.getPrestigeLevel() >= 999) return;

        boolean firstPrestigeKill = this.getXp() == 0;

        float xpAmount = target.getMaxHealth() / 2;
        int xpStage = this.getPrestigeLevel() / 2;
        setPrestigeXpStage(xpStage);
        boolean hasLeveledUp = false;

        if (firstPrestigeKill) {
            this.setXp(xpAmount);
            return;
        }

        while (xpAmount > 0 && this.getLevel() >= 50 && this.getPrestigeLevel() < 999) {
            float xpStageRest = getPrestigeXpStage() - this.getXp();

            if (xpAmount >= xpStageRest) {
                xpAmount -= xpStageRest;
                passingPrestigeLevel(!hasLeveledUp);
                hasLeveledUp = true;
            } else {
                this.setXp(this.getXp() + xpAmount);
                xpAmount = 0;
            }
        }
    }

    public InteractionResult createFoodHealingSystem(Player player, ItemStack itemStack, boolean preferRawMeat, boolean preferCookedMeat, boolean preferVegetables, float healingMultiplier) {
        if (this.isTame() && !this.level().isClientSide() && !isBaby()) {
            Item item = itemStack.getItem();
            if (this.getHealth() < getMaxHealth()) {
                if (CARNIVOROUS_ENTITIES.contains(this.getType())) {
                    if (itemStack.is(ItemTags.MEAT)) {
                        itemStack.shrink(1);
                        boolean itemIsCookedMeat = itemStack.is(Tags.Items.FOODS_RAW_MEAT) || itemStack.is(Tags.Items.FOODS_RAW_FISH);
                        boolean itemIsRawMeat = itemStack.is(Tags.Items.FOODS_COOKED_MEAT) || itemStack.is(Tags.Items.FOODS_COOKED_FISH);
                        if (itemIsRawMeat) {
                            if (preferRawMeat) {
                                heal(4 * healingMultiplier);
                                healAmount = (int) (4 * healingMultiplier);
                                if (isQuestInProgress(DailyQuestRegistry.quest3) && !this.level().isClientSide()) {
                                    this.executeQuestProgression((byte) 2);
                                }
                            }
                            else {
                                heal(2 * healingMultiplier);
                                healAmount = (int) (2 * healingMultiplier);
                                if (isQuestInProgress(DailyQuestRegistry.quest3) && !this.level().isClientSide()) {
                                    this.executeQuestProgression((byte) 2);
                                }
                            }
                        } else if (itemIsCookedMeat) {
                            if (preferCookedMeat) {
                                heal(4 * healingMultiplier);
                                healAmount = (int) (4 * healingMultiplier);
                                if (isQuestInProgress(DailyQuestRegistry.quest3) && !this.level().isClientSide()) {
                                    this.executeQuestProgression((byte) 2);
                                }
                            }
                            else {
                                heal(2 * healingMultiplier);
                                healAmount = (int) (2 * healingMultiplier);
                                if (isQuestInProgress(DailyQuestRegistry.quest3) && !this.level().isClientSide()) {
                                    this.executeQuestProgression((byte) 2);
                                }
                            }
                        }
                        this.playSound(SoundEvents.CAMEL_EAT);
                        return InteractionResult.SUCCESS;
                    }
                    player.swing(InteractionHand.MAIN_HAND, true);
                    return InteractionResult.SUCCESS;
                }
                if (VEGETARIAN_ENTITIES.contains(this.getType())) {
                    if (itemStack.is(Tags.Items.FOODS_VEGETABLE)) {
                        if (preferRawMeat) {
                            itemStack.shrink(1);
                            this.heal(4 * healingMultiplier);
                            healAmount = (int) (4 * healingMultiplier);
                            if (isQuestInProgress(DailyQuestRegistry.quest3) && !this.level().isClientSide()) {
                                this.executeQuestProgression((byte) 2);
                            }
                            this.playSound(SoundEvents.CAMEL_EAT);
                            return InteractionResult.SUCCESS;
                        }
                    }
                    player.swing(InteractionHand.MAIN_HAND, true);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    public void setRandomScale(float actualScale, double minPurcent, double maxPurcent) {
        float randomScaleMultiplicator = (float) (minPurcent + (Math.random() * (maxPurcent  - minPurcent)));
        float newScale = randomScaleMultiplicator * actualScale;
        this.setScale(newScale);
    }

    public void setRandomAttributes(OWEntity entity, double actualHealth, double actualDamage, double actualSpeed) {
        double randomHealthMultiplicator = 0.8 + (Math.random() * (1.2 - 0.8));
        double randomDamageMultiplicator = 0.9 + (Math.random() * (1.1 - 0.9));
        double randomSpeedMultiplicator = 0.85 + (Math.random() * (1.05 - 0.85));

        double newHealth = randomHealthMultiplicator * actualHealth;
        double newDamage = randomDamageMultiplicator * actualDamage;
        double newSpeed = randomSpeedMultiplicator * actualSpeed;

        entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newHealth);
        entity.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(newDamage);
        entity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(newSpeed);

        entity.setHealth(entity.getMaxHealth());
        entity.setDamageToClient((float) newDamage);

        System.out.println("Health: " + entity.getAttributeBaseValue(Attributes.MAX_HEALTH) + " Damage: " + entity.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) + " Speed: " + entity.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
    }

    public void setTamedAttributes(OWEntity entity, double actualHealth) {
        double healthMultiplicator = 1;
        if (actualHealth >= 80) healthMultiplicator = 1.05;
        else if (actualHealth >= 70) healthMultiplicator = 1.1;
        else if (actualHealth >= 60) healthMultiplicator = 1.2;
        else healthMultiplicator = entity.isBaby() ? 1.0f : 1.3f;

        entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(entity.getAttributeBaseValue(Attributes.MAX_HEALTH) * healthMultiplicator);
        System.out.println("New Health: " + entity.getAttributeBaseValue(Attributes.MAX_HEALTH) + " New Damage: " + entity.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) + " New Speed: " + entity.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
    }

    public void upgradeAttributes(OWEntity entity, Holder<Attribute> attribute) {
        if (attribute == null) return;
        if (attribute == Attributes.MAX_HEALTH) {
            entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(entity.getAttribute(attribute).getBaseValue() + (1 * getArchetype().getHealthMultiplier()));
        } else if (attribute == Attributes.ATTACK_DAMAGE) {
            entity.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(entity.getAttribute(attribute).getBaseValue() + (0.15 * getArchetype().getDamageMultiplier()));
            setDamageToClient(entity.getDamage());
        } else if (attribute == Attributes.MOVEMENT_SPEED) {
            entity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(entity.getAttribute(attribute).getBaseValue() + (0.002 * getArchetype().getDamageMultiplier()));
        }

        this.setLevelPoints(this.getLevelPoints() - 1);
    }

    public double chooseValueForUpgradingAttributes(double min, double max, Holder<Attribute> attributes) {
        double multiplicator = 1.0;
        if (attributes == Attributes.ATTACK_DAMAGE) multiplicator = 0.1;
        else if (attributes == Attributes.MOVEMENT_SPEED) multiplicator = 0.002;
        return OWUtils.generateRandomInterval(min * multiplicator, max * multiplicator);
    }

    public void setLookAt(double targetX, double targetY, double targetZ) {
        this.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(targetX, targetY, targetZ));
    }

    public void lookAtPosition(Vec3 targetPos) {
        Vec3 lookVector = targetPos.subtract(this.position());
        double horizontalDistance = Math.sqrt(lookVector.x * lookVector.x + lookVector.z * lookVector.z);
        float yaw = (float) (Math.atan2(lookVector.z, lookVector.x) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) -(Math.atan2(lookVector.y, horizontalDistance) * 180.0 / Math.PI);

        this.setYRot(yaw);
        this.setXRot(pitch);
    }

    public void destroyingBlock(int radius, Block block) {
        BlockPos entityPos = this.blockPosition();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos checkPos = entityPos.offset(x, y, z);
                    BlockState state = this.level().getBlockState(checkPos);

                    if (block instanceof AirBlock) this.level().destroyBlock(checkPos, true);
                    else if (state.getBlock().defaultBlockState().is(block)) {
                        this.level().destroyBlock(checkPos, true);
                    }
                }
            }
        }
    }

    public boolean getDayOrNightTimeInterval(int hour1, int hour2) {
        int hour1Convert = hour1 - 6;
        int hour2Convert = hour2 - 6;

        int hour1Converted = hour1Convert * 1000;
        int hour2Converted = hour2Convert * 1000;

        if (this.level().getDayTime() >= hour1Converted && this.level().getDayTime() < hour2Converted) {
            return true;
        }
        return false;
    }

    public boolean isEntityLookingAtThis(Entity entity, double threshold) {
        Vec3 lookVec = entity.getLookAngle();
        Vec3 toThisVec = new Vec3(this.getX() - entity.getX(), this.getY() - entity.getY(), this.getZ() - entity.getZ()).normalize();

        double dotProduct = lookVec.x * toThisVec.x + lookVec.y * toThisVec.y + lookVec.z * toThisVec.z;
        return dotProduct > threshold;
    }

    private boolean hasReachedEnergyLimit = false;

    public boolean hasReachedEnergyLimit() {
        return hasReachedEnergyLimit;
    }

    public void setHasReachedEnergyLimit(boolean hasReachedEnergyLimit) {
        this.hasReachedEnergyLimit = hasReachedEnergyLimit;
    }

    private boolean hasReachedAttackEnergyLimit = false;

    public boolean hasReachedAttackEnergyLimit() {
        return hasReachedAttackEnergyLimit;
    }

    public void setHasReachedAttackEnergyLimit(boolean hasReachedAttackEnergyLimit) {
        this.hasReachedAttackEnergyLimit = hasReachedAttackEnergyLimit;
    }

    public void resetState() {
        if (this.getTarget() != null) {
            this.setState(0);
        } else this.setState(0);
    }

    public long gameTime() { return this.level().getGameTime();}

    public void swing(InteractionHand hand) {
        if (this instanceof TigerEntity tigerEntity) {
            if (tigerEntity.isTrappingEntity() || tigerEntity.isJumpingOnTarget()) return;
        }
        this.setState(1);
        this.lastPlay = gameTime();
        super.swing(hand);
    }

    public void createAttackSystem(int attackTime) {
        if (this.getState() != 0) {
            if (this.getState() == 1) {
                if (this.gameTime() - this.lastPlay > attackTime) {
                    this.resetState();
                }
            }
        }
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return this instanceof SeaBugEntity ? new SeaBugInventoryMenu(id, inventory, this.itemStackHandlerSeaBug) : new OWInventoryMenu(id, inventory, this.itemStackHandler);
    }

    private float getRiddenSpeedVehicle(Player player) {
        if (this.isSitting() || this.jumping) return 0.0f;

        if (player.zza == 0 && !this.isCombo()) {
            if (!canIncreasesSpeedDuringSprint()) {
                currentSpeed *= 0.75f;
            } else {
                currentSpeed = 0;
            }
            return currentSpeed;
        }

        if (!this.onGround() && !this.isInWater()) return this.getSpeed();

        if (isCombo()) {
            if (this instanceof ElephantEntity elephant) {
                if (elephant.getComboAttack() == 3) {
                    return 0;
                }
            } else if (this instanceof KodiakEntity kodiak) {
                if (kodiak.getComboAttack() == 3) {
                    return (this.getSpeed() / 3) * (vehicleComboSpeedMultiplier() / 4);
                }
            }
            if (vehicleComboSpeedMultiplier() != -1) {
                if (isChangeSpeedDuringCombo()) {
                    targetSpeed = (this.getSpeed() / 3) * (vehicleComboSpeedMultiplier() / 3);
                } else {
                    return currentSpeed;
                }
            }
        }

        else if (isRunning()) {
            if (canIncreasesSpeedDuringSprint()) {
                return ((this.getSpeed() / 3) * ((this.vehicleRunSpeedMultiplier() * (0.5f + ((float) (Math.min(100, getAcceleration())) / 100))) / 2) * 1.75f);
            }
            if (MARAUDER_ENTITIES.contains(this.getClass()) && this.isInFight()) {
                targetSpeed = (this.getSpeed() / 3) * (vehicleRunSpeedMultiplier() * 1.75f) * 1.15f;
            }
            targetSpeed = this.getSpeed() * (vehicleRunSpeedMultiplier() / 1.75f);
        }

        else {
            targetSpeed = (this.getSpeed() / 3) * (vehicleWalkSpeedMultiplier() / 2);
        }

        if (canIncreasesSpeedDuringSprint()) {
            currentSpeed = targetSpeed;
        } else {
            currentSpeed += (targetSpeed - currentSpeed) * 0.15f;
        }
        return currentSpeed;
    }

    private void travelRidden(Player player, Vec3 travelVector) {
        Vec3 vec3 = this.getRiddenInput(player, travelVector);
        this.tickRidden(player, vec3);
        if (this.isControlledByLocalInstance()) {
            Vec3 lookDirection = Vec3.directionFromRotation(isInWater() ? player.getXRot() : 0, player.getYRot()).normalize();
            double speedPerTick = getRiddenSpeedVehicle(player) / (isInWater() ? vehicleWaterSpeedDivider() : 1);

            Vec3 currentMovement = this.getDeltaMovement();
            double yMovement = isInWater() ? lookDirection.y * speedPerTick - 0.01 : currentMovement.y;

            Vec3 newMovement = new Vec3(lookDirection.x * speedPerTick, yMovement, lookDirection.z * speedPerTick);

            this.setDeltaMovement(newMovement);
            this.travel(vec3);
        } else {
            this.calculateEntityAnimation(false);
            this.setDeltaMovement(Vec3.ZERO);
            this.tryCheckInsideBlocks();
        }
    }

    @Override
    public void aiStep() {
        if (this instanceof Submarine) {
            super.aiStep();
            return;
        }

        if (this.noJumpDelay > 0) {
            --this.noJumpDelay;
        }

        if (this.isControlledByLocalInstance()) {
            this.lerpSteps = 0;
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());

            if (this.jumping && this.isAffectedByFluids()) {
                FluidType fluidType = this.getMaxHeightFluidType();
                double d3;
                if (!fluidType.isAir()) {
                    d3 = this.getFluidTypeHeight(fluidType);
                } else if (this.isInLava()) {
                    d3 = this.getFluidHeight(FluidTags.LAVA);
                } else {
                    d3 = this.getFluidHeight(FluidTags.WATER);
                }

                boolean flag = this.isInWater() && d3 > (double)0.0F;
                double d4 = this.getFluidJumpThreshold();
                if (!flag || this.onGround() && !(d3 > d4)) {
                    if (!this.isInLava() || this.onGround() && !(d3 > d4)) {
                        if (fluidType.isAir() || this.onGround() && !(d3 > d4)) {
                            if ((this.onGround() || flag && d3 <= d4) && this.noJumpDelay == 0) {
                                this.jumpFromGround();
                                this.noJumpDelay = 10;
                            }
                        } else {
                            this.jumpInFluid(fluidType);
                        }
                    } else {
                        this.jumpInFluid((FluidType)NeoForgeMod.LAVA_TYPE.value());
                    }
                } else {
                    this.jumpInFluid((FluidType)NeoForgeMod.WATER_TYPE.value());
                }
            } else {
                this.noJumpDelay = 0;
            }
        }

        if (this.lerpSteps > 0) {
            this.lerpPositionAndRotationStep(this.lerpSteps, this.lerpX, this.lerpY, this.lerpZ, this.lerpYRot, this.lerpXRot);
            --this.lerpSteps;
        } else if (!this.isEffectiveAi()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        }

        if (this.lerpHeadSteps > 0) {
            this.lerpHeadRotationStep(this.lerpHeadSteps, this.lerpYHeadRot);
            --this.lerpHeadSteps;
        }

        Vec3 vec3 = this.getDeltaMovement();
        double d0 = vec3.x;
        double d1 = vec3.y;
        double d2 = vec3.z;
        if (java.lang.Math.abs(vec3.x) < 0.003) {
            d0 = (double)0.0F;
        }

        if (java.lang.Math.abs(vec3.y) < 0.003) {
            d1 = (double)0.0F;
        }

        if (java.lang.Math.abs(vec3.z) < 0.003) {
            d2 = (double)0.0F;
        }

        this.setDeltaMovement(d0, d1, d2);
        this.level().getProfiler().push("ai");
        if (this.isImmobile()) {
            this.jumping = false;
            this.xxa = 0.0F;
            this.zza = 0.0F;
        } else if (this.isEffectiveAi()) {
            this.level().getProfiler().push("newAi");
            this.serverAiStep();
            this.level().getProfiler().pop();
        }

        this.level().getProfiler().pop();
        this.level().getProfiler().push("jump");
        if (this.jumping && this.isAffectedByFluids()) {
            FluidType fluidType = this.getMaxHeightFluidType();
            double d3;
            if (!fluidType.isAir()) {
                d3 = this.getFluidTypeHeight(fluidType);
            } else if (this.isInLava()) {
                d3 = this.getFluidHeight(FluidTags.LAVA);
            } else {
                d3 = this.getFluidHeight(FluidTags.WATER);
            }

            boolean flag = this.isInWater() && d3 > (double)0.0F;
            double d4 = this.getFluidJumpThreshold();
            if (!flag || this.onGround() && !(d3 > d4)) {
                if (!this.isInLava() || this.onGround() && !(d3 > d4)) {
                    if (fluidType.isAir() || this.onGround() && !(d3 > d4)) {
                        if ((this.onGround() || flag && d3 <= d4) && this.noJumpDelay == 0) {
                            this.jumpFromGround();
                            this.noJumpDelay = 10;
                        }
                    } else {
                        this.jumpInFluid(fluidType);
                    }
                } else {
                    this.jumpInFluid((FluidType)NeoForgeMod.LAVA_TYPE.value());
                }
            } else {
                this.jumpInFluid((FluidType)NeoForgeMod.WATER_TYPE.value());
            }
        } else {
            this.noJumpDelay = 0;
        }

        this.level().getProfiler().pop();
        this.level().getProfiler().push("travel");
        this.xxa *= 0.98F;
        this.zza *= 0.98F;
        this.updateFallFlying();
        AABB aabb = this.getBoundingBox();
        Vec3 vec31 = new Vec3((double)this.xxa, (double)this.yya, (double)this.zza);
        if (this.hasEffect(MobEffects.SLOW_FALLING) || this.hasEffect(MobEffects.LEVITATION)) {
            this.resetFallDistance();
        }

        label111: {
            LivingEntity var17 = this.getControllingPassenger();
            if (var17 instanceof Player player) {
                if (this.isAlive()) {
                    this.travelRidden(player, vec31);
                    break label111;
                }
            }

            if (this.getControllingPassenger() == null) this.travel(vec31);
        }

        this.level().getProfiler().pop();
        this.level().getProfiler().push("freezing");
        if (!this.level().isClientSide && !this.isDeadOrDying()) {
            int i = this.getTicksFrozen();
            if (this.isInPowderSnow && this.canFreeze()) {
                this.setTicksFrozen(java.lang.Math.min(this.getTicksRequiredToFreeze(), i + 1));
            } else {
                this.setTicksFrozen(java.lang.Math.max(0, i - 2));
            }
        }

        this.removeFrost();
        this.tryAddFrost();
        if (!this.level().isClientSide && this.tickCount % 40 == 0 && this.isFullyFrozen() && this.canFreeze()) {
            this.hurt(this.damageSources().freeze(), 1.0F);
        }

        this.level().getProfiler().pop();
        this.level().getProfiler().push("push");
        if (this.autoSpinAttackTicks > 0) {
            --this.autoSpinAttackTicks;
            this.checkAutoSpinAttack(aabb, this.getBoundingBox());
        }

        this.pushEntities();
        this.level().getProfiler().pop();
        if (!this.level().isClientSide && this.isSensitiveToWater() && this.isInWaterRainOrBubble()) {
            this.hurt(this.damageSources().drown(), 1.0F);
        }


        if (!this.level().isClientSide()) {
            IItemHandler itemHandler = this.getInventory();

            if (itemHandler != null) {
                ItemStack foodStack = itemHandler.getStackInSlot(1);

                if (!foodStack.isEmpty() && foodStack.getCount() < foodStack.getMaxStackSize()) {
                    List<ItemEntity> nearbyItems = this.level().getEntitiesOfClass(
                            ItemEntity.class,
                            this.getBoundingBox().inflate(1.5),
                            entity -> !entity.isRemoved() && entity.getItem().getItem() == foodStack.getItem()
                    );

                    if (!nearbyItems.isEmpty()) {
                        for (ItemEntity itemEntity : nearbyItems) {
                            ItemStack groundStack = itemEntity.getItem();

                            if (groundStack.getItem() == foodStack.getItem()) {
                                int spaceLeft = foodStack.getMaxStackSize() - foodStack.getCount();
                                int amountToAdd = java.lang.Math.min(spaceLeft, groundStack.getCount());

                                if (amountToAdd > 0) {
                                    foodStack.grow(amountToAdd);

                                    this.setFoodCount(foodStack.getCount());

                                    groundStack.shrink(amountToAdd);
                                    if (groundStack.isEmpty()) {
                                        itemEntity.discard();
                                    }

                                    this.playSound(SoundEvents.ITEM_PICKUP, 0.2F,
                                            ((this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);

                                    if (foodStack.getCount() >= foodStack.getMaxStackSize()) {
                                        break;
                                    }
                                }
                            }
                        }

                        if (!this.isFed() && !foodStack.isEmpty()) {
                            this.setFed(true);
                        }
                    }
                }
            }
        }

    }

    public boolean canAttack() {
        return canAttack;
    }

    public void setCanAttack(boolean canAttack) {
        this.canAttack = canAttack;
    }

    private void updateFallFlying() {
        boolean flag = this.getSharedFlag(7);
        if (flag && !this.onGround() && !this.isPassenger() && !this.hasEffect(MobEffects.LEVITATION)) {
            ItemStack itemstack = this.getItemBySlot(EquipmentSlot.CHEST);
            flag = itemstack.canElytraFly(this) && itemstack.elytraFlightTick(this, this.fallFlyTicks);
        } else {
            flag = false;
        }

        if (!this.level().isClientSide) {
            this.setSharedFlag(7, flag);
        }

    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (this.isSleeping()) return false;
        if (this.isTame()) {
            this.setFighting(true);
            fightingTime = 200;
        }
        if (isQuestInProgress(DailyQuestRegistry.quest1) && !this.level().isClientSide()) {
            if (!ownerIsRiding()) {
                this.executeQuestProgression((byte) 0);
            }
        }
        if (isQuestInProgress(DailyQuestRegistry.quest10) && entity instanceof LivingEntity living && living.getMaxHealth() > this.getMaxHealth() && !this.level().isClientSide()) {
            if (!ownerIsRiding()) {
                this.executeQuestProgression((byte) 9);
            }
        }
        return super.doHurtTarget(entity);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        boolean isTankAndReduceDmg = this.isTank() && (damageSource.is(DamageTypes.MOB_ATTACK) || damageSource.is(DamageTypes.PLAYER_ATTACK) || damageSource.is(DamageTypes.GENERIC));
        boolean willTakeDamage = super.hurt(damageSource, amount * (isTankAndReduceDmg ? 0.8f : 1.0f));

        if (willTakeDamage) {
            if (damageSource.getDirectEntity() instanceof TranquilizerArrow sedativeArrow) {
                if (isSleeping()) return false;
                int tranquilizerPower = amount > 0.5 ? (int) (sedativeArrow.tranquilizerEffectiveness * (amount / 2)) : 0;
                this.setActualSleepingBarTo(this.getActualSleepingBar() + tranquilizerPower);
                if (!isSleeping() && getActualSleepingBar() >= getMaxSleepingBar()) {
                    setSleeping(true);
                }
            } else if (damageSource.getDirectEntity() instanceof SlingshotProjectile slingshotProjectile) {
                if (isSleeping()) return false;
                int tranquilizerPower = amount > 0.5 ? (int) (slingshotProjectile.tranquilizerEffectiveness * (amount / 2)) : 0;
                this.setActualSleepingBarTo(this.getActualSleepingBar() + tranquilizerPower);
                if (!isSleeping() && getActualSleepingBar() >= getMaxSleepingBar()) {
                    setSleeping(true);
                }
            } else if (damageSource.getDirectEntity() instanceof TranquilizerWoodenStinger tranquilizerWoodenStinger) {
                if (isSleeping()) return false;
                int tranquilizerPower = tranquilizerWoodenStinger.tranquilizerEffectiveness;
                this.setActualSleepingBarTo(this.getActualSleepingBar() + tranquilizerPower);
                if (!isSleeping() && getActualSleepingBar() >= getMaxSleepingBar()) {
                    setSleeping(true);
                }
            }
        }

        lastVisibleTarget = (LivingEntity) damageSource.getEntity();
        quest10Progression = 0;
        this.setNap(false);
        fightingTime = 200;
        this.setFighting(true);
        hurtAmount = (int) amount;
        if (isQuestInProgress(DailyQuestRegistry.quest2) && !this.level().isClientSide()) {
            this.executeQuestProgression((byte) 1);
        }

        return willTakeDamage;
    }

    public void spawnBabyOfParents(OWEntity partner1, OWEntity partner2, boolean spawnTwins) {
        Vec3 vec3 = this.blockPosition().getCenter();
        DifficultyInstance difficulty = level().getCurrentDifficultyAt(this.blockPosition());
        partner1.setAge((int) partner1.getMaxHealth() * 1800);
        partner2.setAge((int) partner2.getMaxHealth() * 1800);
        partner1.resetLove();
        partner2.resetLove();

        OWEntity entity = (OWEntity) partner1.getType().create(level());
        entity.finalizeSpawn((ServerLevelAccessor) level(), difficulty, MobSpawnType.BREEDING, null);

        float health = adaptHealthForBaby(RANDOM(2) ? partner1.getMaxHealth() : partner2.getMaxHealth());
        float damages = adaptDamagesForBaby(RANDOM(2) ? partner1.getDamage() : partner2.getDamage());
        float speed = adaptSpeedForBaby(RANDOM(2) ? partner1.getSpeed() : partner2.getSpeed());
        int variant = RANDOM(2) ? partner1.getTypeVariant() : partner2.getTypeVariant();

        entity.setBaby(true);
        entity.maxHealth = health;
        entity.maxMaturation = (int) (2000 * health + 10000 * this.getDamage());
        entity.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damages);
        entity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
        entity.setBaseHealth(health);
        entity.setDamageToClient(damages);
        entity.setBaseDamage(damages);
        entity.setBaseSpeed(speed);
        entity.setScale((partner1.getScale() + partner2.getScale()) / 2);
        entity.setHealth(1);
        entity.setVariant(entity, variant);
        entity.level().addFreshEntity(new ExperienceOrb(level(), this.getX(), this.getY(), this.getZ(), this.getRandom().nextInt(7) + 1));

        entity.moveTo(vec3.x(), vec3.y(), vec3.z(), Mth.wrapDegrees(level().random.nextFloat() * 360.0F), 0.0F);
        level().addFreshEntity(entity);


        if (spawnTwins) {
            OWEntity twin = (OWEntity) partner1.getType().create(level());
            twin.finalizeSpawn((ServerLevelAccessor) level(), difficulty, MobSpawnType.BREEDING, null);

            twin.setBaby(true);
            twin.maxHealth = health;
            twin.maxMaturation = (int) (2000 * health + 10000 * this.getDamage());
            twin.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damages);
            twin.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
            twin.setBaseHealth(health);
            twin.setDamageToClient(damages);
            twin.setBaseDamage(damages);
            twin.setBaseSpeed(speed);
            twin.setScale((partner1.getScale() + partner2.getScale()) / 2);
            twin.setHealth(1);
            twin.setVariant(twin, variant);
            twin.level().addFreshEntity(new ExperienceOrb(level(), this.getX(), this.getY(), this.getZ(), this.getRandom().nextInt(7) + 1));

            twin.moveTo(vec3.x(), vec3.y(), vec3.z(), Mth.wrapDegrees(level().random.nextFloat() * 360.0F), 0.0F);
            level().addFreshEntity(twin);
        }

    }

    private float adaptHealthForBaby(float health) {
        if (health >= this.getAttribute(Attributes.MAX_HEALTH).getBaseValue() * 1.5) return (float) (this.getAttribute(Attributes.MAX_HEALTH).getBaseValue() * 1.5);
        return health;
    }

    private float adaptDamagesForBaby(float damages) {
        if (damages >= this.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() * 1.35) return (float) (this.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() * 1.35);
        return damages;
    }

    private float adaptSpeedForBaby(float speed) {
        if (speed >= this.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue() * 1.2) return (float) (this.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue() * 1.2);
        return speed;
    }

    public void setVariant(OWEntity entity, int variant) {
        if (entity instanceof TigerEntity tiger) {
            tiger.setVariant(TigerVariant.byId(variant));
            tiger.setInitialVariant(TigerVariant.byId(variant));
        }
        else if (entity instanceof BoaEntity boa) {
            boa.setVariant(BoaVariant.byId(variant));
            boa.setInitialVariant(BoaVariant.byId(variant));
        }
        else if (entity instanceof PeacockEntity peacock) {
            peacock.setVariant(PeacockVariant.byId(variant));
            peacock.setInitialVariant(PeacockVariant.byId(variant));
        }
        else if (entity instanceof ElephantEntity elephant) {
            elephant.setVariant(ElephantVariant.byId(variant));
            elephant.setInitialVariant(ElephantVariant.byId(variant));
        }
        else if (entity instanceof KodiakEntity kodiak) {
            kodiak.setVariant(KodiakVariant.byId(variant));
            kodiak.setInitialVariant(KodiakVariant.byId(variant));
        }
    }

    public boolean isMoving() {
        return this.getDeltaMovement().horizontalDistanceSqr() != 0.0f;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        boolean hasCamouflage = target != null && target.hasEffect(OWEffects.CAMOUFLAGE_EFFECT.getDelegate()) && target.isSteppingCarefully();

        if (target != null && target.isBaby()) {
            super.setTarget(null);
            return;
        }

        if (isSleeping() || isInResurrection()) {
            super.setTarget(null);
            return;
        }

        if (target instanceof TamableAnimal tamable && tamable.getOwnerUUID() != null && tamable.getOwnerUUID().equals(this.getOwnerUUID())) {
            super.setTarget(null);
            return;
        }

        if (target != null && this.hasLineOfSight(target) && !hasCamouflage) {
            lastVisibleTarget = target;
        }

        if (hasCamouflage) {
            if (lastVisibleTarget != null && lastVisibleTarget == target && this.hasLineOfSight(target)) {
                super.setTarget(target);
            } else {
                super.setTarget(null);
            }
            return;
        }

        if (!hasCamouflage && !isBaby()) {
            setFighting(!ownerIsRiding() && target != null);
        }

        if (this.isTame() && this.getCurrentMode() == Mode.Passive) {
            super.setTarget(null);
            return;
        }

        if (ownerIsRiding() || this.isSitting()) {
            super.setTarget(null);
            return;
        }

        if (!this.level().isClientSide()) {
            setRunning(target != null);
        }

        if (target == null) {
            lastVisibleTarget = null;
        }

        super.setTarget(target);
    }

    public void forceSetTarget(@Nullable LivingEntity target) {
        try {
            java.lang.reflect.Field targetField = Mob.class.getDeclaredField("target");
            targetField.setAccessible(true);
            targetField.set(this, target);

            this.setTarget(null);
            this.setRunning(false);

            if (target == null) {
                this.lastVisibleTarget = null;
                this.setRunning(false);
                this.setFighting(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetBabyQuest(boolean win) {
        if (win) {
            this.actualMaturation += (float) OWUtils.generateRandomInterval(3600, 7200);
            this.setLevelPoints(this.getLevelPoints() + 1);
            this.level().addFreshEntity(new ExperienceOrb(level(), this.getX(), this.getY(), this.getZ(), this.getRandom().nextInt(7) + 1));
        }
        babyQuestIsInProgress = false;
        babyQuestProgressTimer = 0;
        choosenFood = null;
    }

    public float getMaxHeight() {
        return this.getBbHeight() / this.getScale();
    }

    public float getMaxWidth() {
        return this.getBbWidth() / this.getScale();
    }

    public boolean accelerationIsAtMax() {
        return this.getAcceleration() >= 100;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            handleClientAnimationSync();
        }

        if (this.isCombo()) {
            this.setAcceleration(0);
        }

        createTransitionAnimation("idleSit", transitionIdleSit, this.isSitting(), 13);
        createTransitionAnimation("sitIdle", transitionSitIdle, !this.isSitting(), 13);
        createTransitionAnimation("idleSleep", transitionIdleSleep, this.isNapping(), 20 * (this instanceof WalrusEntity ? 2 : 1));
        createTransitionAnimation("sleepIdle", transitionSleepIdle, !this.isNapping(), 20 * (this instanceof WalrusEntity ? 2 : 1));

        if (sittingCooldown > 0) sittingCooldown--;

        if (!this.level().isClientSide()) {
            if (this.level() instanceof ServerLevel serverLevel) {
                for (ServerPlayer player : serverLevel.players()) {
                    OWNetworkHandler.sendToClient(new OWFoodPacketClient(this.getId(), this.foodGiven, this.foodWanted), player);
                }
            }
        }

        if (this.getArchetype() == OWEntityConfig.Archetypes.BERSERKER) {
            if (this.getHealth() <= (this.getMaxHealth() * 0.25)) {
                comboSpeedMultiplier = 1.3f;
            } else comboSpeedMultiplier = 1.0f;
        } else comboSpeedMultiplier = 1.0f;

        if (!this.level().isClientSide()) {
            if (this.isRunning() && this.isVehicle()) {
                boolean isWalrusInWater = this instanceof WalrusEntity walrus && walrus.isInWater();
                boolean isCrocodileInWater = this instanceof CrocodileEntity crocodile && crocodile.isInWater();
                setVitalEnergy(getVitalEnergy() + ((!isWalrusInWater && !isCrocodileInWater) ? 1 : 0.5f));
            }

            if (!isRunning() && getVitalEnergy() > 0 && !isCombo()) {
                setVitalEnergy(getVitalEnergy() - getVitalEnergyRecuperation());
            }
        }

        if (this.hasEffect(OWEffects.FEAR_EFFECT.getDelegate())) {
            this.setTarget(null);
        }

        if (this.isInResurrection() && !this.level().isClientSide()) {
            if (maxHealthBeforeResurrection > 0) {
                this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1);
            } else this.maxHealthBeforeResurrection = (float) this.getAttribute(Attributes.MAX_HEALTH).getValue();

            if (resurrectionTimer < getResurrectionMaxTimer()) {
                resurrectionTimer++;
            } else {
                this.setResurrection(false);
                this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealthBeforeResurrection);
                this.setHealth(maxHealthBeforeResurrection);
                this.resurrectionTimer = 0;
            }
        }

        if (!this.level().isClientSide()) {
            if (this.level() instanceof ServerLevel serverLevel) {
                for (ServerPlayer player : serverLevel.players()) {
                    OWNetworkHandler.sendToClient(new OWPacketSendToClient(this.getId(), this.actualMaturation, this.maxMaturation, this.delayBeforeBabyTask, this.choosenQuestStr, this.babyQuestIsInProgress, this.babyQuestProgressTimer, this.choosenFood, this.canShowVitalEnergyLack), player);
                }
            }
        }

        if (this.isInResurrection() && tickCount % 10 == 0) OWUtils.spawnParticles(this, ParticleTypes.SOUL, 0, 0, 0, 25, this.getBbHeight() * this.getBbWidth());

        if (this.isTame()) {
            if (this.isUltimate()) {
            } else {
                if (ultimateCooldown >= 1) {
                    ultimateCooldown++;
                    if (ultimateCooldown >= 6000) ultimateCooldown = 0;
                }
            }
        }





        LivingEntity rider = this.getControllingPassenger();

        if (this.isTame()) {
            this.setActualSleepingBarTo(0);
            this.setTamingPercentage(100, 100);
        }

        if (isBaby()) {
            float percent = this.getMaturationPercentage();
            float width = (float) (getMaxWidth() + (getMaxWidth() - getMaxWidth()) * (percent / 100.0F));
            float height = (float) (getMaxHeight() + (getMaxHeight() - getMaxHeight()) * (percent / 100.0F));
            setHitboxSize(width, height);
        }

        if (!this.level().isClientSide() && this.isBaby()) {
            if (actualMaturation < maxMaturation) {
                actualMaturation++;
                setMaturationPercentage((int) this.actualMaturation, this.maxMaturation);
                float healthMultiplier = 0.1f + (0.9f * ((float) getMaturationPercentage() / 100));
                this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealth * healthMultiplier);
            } else if (actualMaturation >= maxMaturation) {
                this.setBaby(false);
                setHitboxSize(getMaxWidth() * getScale(), getMaxHeight() * getScale());
                this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealth);
                this.setHealth(maxHealth);
                actualMaturation = 0;
            }
            if (isTame()) {
                if (delayBeforeBabyTask < DELAY_BEFORE_BABY_TASK_MAX) {
                    if (!babyQuestIsInProgress) delayBeforeBabyTask++;
                } else if (delayBeforeBabyTask >= DELAY_BEFORE_BABY_TASK_MAX) {
                    delayBeforeBabyTask = 0;
                    List<Integer> keys = new ArrayList<>(babyQuests.keySet());
                    List<String> values = new ArrayList<>(babyQuests.values());

                    choosenQuest = /*keys.get(random.nextInt(keys.size()))*/ 0;
                    choosenQuestStr = values.get(choosenQuest);
                    if (choosenQuest == 0) {

                        choosenFood = VEGETARIAN_ENTITIES.contains(this.getType())
                                ? getRandomItemFromTag(Tags.Items.FOODS_VEGETABLE)
                                : getRandomItemFromTag(this.random.nextBoolean()
                                ? (this.random.nextBoolean() ? Tags.Items.FOODS_COOKED_MEAT : Tags.Items.FOODS_RAW_MEAT)
                                : (this.random.nextBoolean() ? Tags.Items.FOODS_COOKED_FISH : Tags.Items.FOODS_RAW_FISH));

                        System.out.println(choosenFood);
                    }
                    babyQuestIsInProgress = true;
                }
            }
        }

        if (babyQuestIsInProgress) {
            babyQuestProgressTimer++;

            if (babyQuestProgressTimer >= 6000) {
                resetBabyQuest(false);
            }
        }

        if (isSleeping()) {
            if (tickCount % 200 == 0) {
                if (this instanceof TigerEntity) {
                    SoundEvent sound = RANDOM(3) ? OWSounds.TIGER_SNORE_1.get() : RANDOM(2) ? OWSounds.TIGER_SNORE_2.get() : OWSounds.TIGER_SNORE_3.get() ;
                    this.playSound(sound);
                }
            }
            if (this.onGround()) {
                this.setDeltaMovement(0, 0, 0);
                this.hasImpulse = false;
                this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,5, 255, false, false, false));
            }
        }


        if (getActualSleepingBar() > 0 && !(this instanceof PlantEmpressEntity)) {
            int decreaseRate = isSleeping() ? sleepBarDownSpeed * 2 : sleepBarDownSpeed;
            if (tickCount % decreaseRate == 0) setActualSleepingBarTo(getActualSleepingBar() - 1);;
        }

        if (isSleeping() && getActualSleepingBar() <= 0) {
            setSleeping(false);
        }

        if (this.getLevelPoints() < 0) {
            setLevelPoints(0);
        }

        if (!this.level().isClientSide()) {
            if (this.level() instanceof ServerLevel serverLevel) {
                for (ServerPlayer player : serverLevel.players()) {
                    OWNetworkHandler.sendToClient(new OWQuestProgressToClient(this.getId(), this.quest0Progression, this.quest1Progression, this.quest2Progression,
                            this.quest3Progression, this.quest4Progression, this.quest5Progression, this.quest6Progression, this.quest7Progression,
                            this.quest8Progression, this.quest9Progression, this.quest10Progression, this.quest0isLocked, this.quest1isLocked, this.quest2isLocked, this.quest3isLocked,
                            this.quest4isLocked, this.quest5isLocked, this.quest6isLocked, this.quest7isLocked, this.quest8isLocked, this.quest9isLocked, this.quest10isLocked), player);

                    OWNetworkHandler.sendToClient(new OWEntityUtilsToClient(this.getId(), this.resurrectionTimer, this.attackTimer), player);
                }
            }
        }

        if (this.hasEffect(OWEffects.FEAR_EFFECT.getDelegate())) {
            this.setState(2);
            this.setRunning(true);
        }

        if (this.isTame() && this.getCurrentMode() == Mode.Aggressive) {
            LivingEntity owner = this.getOwner();
            List<LivingEntity> livingEntities = this.level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(20));

            for (LivingEntity targetWhoFocusOwner : livingEntities) {
                if (targetWhoFocusOwner instanceof Mob mob) {
                    if (mob.getTarget() == owner) {
                        if (mob.isAlive()) {
                            this.setTarget(mob);
                        }
                    }
                }
            }

        }



        if (isQuestInProgress(DailyQuestRegistry.quest4) && !this.level().isClientSide()) {
            this.executeQuestProgression((byte) 3);
        }


        if (this.isFed()) {
            if (!this.isInFight() && this.getHealth() < this.getMaxHealth()) {
                if (tickCount % 60 == 0) {
                    if (this.level().isClientSide()) OWNetworkHandler.sendToServer(new ConsumeItemPacket(this.getId()));
                }
            }
        }


        if (rider != null) {
            LivingEntity attacker = rider.getLastAttacker();

            if (attacker != null) {
                if (attacker instanceof Mob mob) {
                    mob.setTarget((LivingEntity) rider.getRootVehicle());
                }
            }
        }

        if (this.isTame() && ownerIsRiding()) {
            if (this.isInFight()) {
                if (fightingTime > 0) fightingTime--;
                else {
                    this.setFighting(false);
                    fightingTime = 200;
                    this.setTarget(null);
                }
            }
        }

        if (this.isNapping()) {
            getNavigation().stop();
            getMoveControl().setWantedPosition(getX(), getY(), getZ(), 0);
        }

        this.setFalling(!this.onGround() && !this.isUnderWater() && !this.isInWater() && this.getBlockY() - this.level().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos((int) this.getX(), (int) this.getY(), (int) this.getZ())).getY() >= 3);

        if (isSitting()) {
            if (this.onGround()) {
                this.setDeltaMovement(0, 0, 0);
                this.hasImpulse = false;
                this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,5, 255, false, false, false));
            }
        }

        if (!this.isInFight() && this.isTame()) if (tickCount % 200 == 0) {
            this.heal(1);
            healAmount = 1;
            if (isQuestInProgress(DailyQuestRegistry.quest3) && !this.level().isClientSide()) {
                this.executeQuestProgression((byte) 2);
            }
        }
    }

    private Item getRandomItemFromTag(TagKey<Item> tag) {
        List<Item> items = BuiltInRegistries.ITEM.stream()
                .filter(item -> item.getDefaultInstance().is(tag))
                .toList();

        if (items.isEmpty()) {
            return Items.APPLE;
        }

        return items.get(this.random.nextInt(items.size()));
    }

    public void createComboAttackSystem(int timeMax, int timeToHit, SoundEvent sound, double width, double height, double reach, boolean spawnBlurr, float backMultiplier) {
        if (this.isCombo()) {
            boolean isRided = this.getControllingPassenger() != null;
            if (isRided && this.getTarget() != null) this.setLookAt(this.getTarget().getX(), this.getTarget().getY(),this.getTarget().getZ());
            if (attackTimer < timeMax) attackTimer++;
            else {
                attackTimer = 0;
                setCombo(false, 0);
                return;
            }
            if (attackTimer == timeToHit - (this instanceof ElephantEntity ? 3 : 0)) {
                attackEntitiesInFront((float) ((this.getDamage() / MAX_ATTACKS_IN_COMBO) * (isTame() ? 1.0 : SAVAGE_ENTITY_DAMAGE_MULTIPLIER)), sound, width * (isRided ? 1 : 1.5f), height * (isRided ? 1 : 1.5f), reach * (isRided ? 1 : 1.5f), backMultiplier);
                if (spawnBlurr) {
                    OWUtils.spawnBlurrParticle(this.level(), this, 1, 1, 1);
                }
            }

            applyComboModification(timeToHit);

            if (attackTimer == timeToHit + 2) {
                setPauseCombo(true);
            }
        }
    }

    public void applyComboModification(int timeToHit) {

        if (this instanceof TigerEntity tiger) {
            if (attackTimer >= timeToHit - 1 && attackTimer < timeToHit + 1) {
                if (tiger.getComboAttack() == 3) {
                    Vec3 lookDirection = this.getLookAngle();
                    Vec3 forwardPush = lookDirection.scale(1.5);

                    this.move(MoverType.SELF, forwardPush);

                    this.hasImpulse = true;
                    OWUtils.spawnBlurrParticle(this.level(), this, 1, 1, 1);
                }
            }
        }

        if (this instanceof ElephantEntity elephant) {
            if (attackTimer == timeToHit) {
                float pitch = (float) (OWUtils.generateRandomInterval(1.15, 1.4));
                elephant.level().playSound(null, elephant.getX(), elephant.getY(), elephant.getZ(), OWSounds.LEG_HURT.get(), SoundSource.HOSTILE, 1.0f, pitch);
                if (elephant.getComboAttack() == 3) {
                    elephant.createShockWave();
                }
            }
        }

        if (this instanceof KodiakEntity kodiak) {
            if (attackTimer == timeToHit) {
                float pitch = (float) (OWUtils.generateRandomInterval(1.15, 1.4));
                kodiak.level().playSound(null, kodiak.getX(), kodiak.getY(), kodiak.getZ(), OWSounds.LEG_HURT.get(), SoundSource.HOSTILE, 1.0f, pitch);
                if (getComboAttack() == 3) {
                    kodiak.createMiniShockwave();
                }
            }
        }

        if (this instanceof CrocodileEntity crocodile) {
            if (attackTimer == timeToHit) {
                float pitch = (float) (OWUtils.generateRandomInterval(0.8, 1.0));
                crocodile.level().playSound(null, crocodile.getX(), crocodile.getY(), crocodile.getZ(), OWSounds.LEG_HURT.get(), SoundSource.HOSTILE, 1.0f, pitch);
            }
        }

        if (this instanceof WalrusEntity walrus) {
            if (attackTimer == timeToHit) {
                float pitch = (float) (OWUtils.generateRandomInterval(1.15, 1.4));
                walrus.level().playSound(null, walrus.getX(), walrus.getY(), walrus.getZ(), OWSounds.LEG_HURT.get(), SoundSource.HOSTILE, 1.0f, pitch);
            }
        }

        if (this instanceof TigerEntity tiger) {
            if (attackTimer == timeToHit) {
                if (this.getComboAttack() < 3) {
                    float pitch = (float) (OWUtils.generateRandomInterval(1.15, 1.4));
                    tiger.level().playSound(null, tiger.getX(), tiger.getY(), tiger.getZ(), OWSounds.LEG_HURT.get(), SoundSource.HOSTILE, 1.0f, pitch);
                } else if (this.getComboAttack() == 3) {
                    float pitch = (float) (OWUtils.generateRandomInterval(1.15, 1.4));
                    tiger.level().playSound(null, tiger.getX(), tiger.getY(), tiger.getZ(), OWSounds.TIGER_SHARK_CRUSH_MOUTH.get(), SoundSource.HOSTILE, 1.0f, pitch);
                }
            }
        }

    }

    public void resetCombo(int numberOfAttacks) {
        continueComboMaxTimer = 0;
        setPauseCombo(false);
        setCombo(false, numberOfAttacks);
        attackTimer = 0;
        playerContinueCombo = false;
    }

    public void createCombo(int timeMax, int timeToHit, SoundEvent sound, double width, double height, double reach, boolean spawnBlurr, float backMultiplier) {
        if (isPauseCombo()) {
            continueComboMaxTimer++;

            if (this.playerContinueCombo && actualAttackNumber < (MAX_ATTACKS_IN_COMBO - 1)) {
                continueComboMaxTimer = 0;

                actualAttackNumber++;

                resetCombo(actualAttackNumber);
                if (this instanceof WalrusEntity walrus) {
                    if (walrus.isInWater()) {
                        if (getComboAttack() < 2) {
                            setCombo(true, actualAttackNumber + 1);
                        }
                    } else {
                        setCombo(true, actualAttackNumber + 1);
                    }
                } else {
                    setCombo(true, actualAttackNumber + 1);
                }

                setPauseCombo(false);
            }

            if (continueComboMaxTimer >= 10) {
                resetCombo(0);
                actualAttackNumber = 0;
            }
        } else {
            if (this instanceof WalrusEntity && getComboAttack() == 3) {
                timeToHit = 10;
            }
            createComboAttackSystem(timeMax, timeToHit, sound, width, height, reach, spawnBlurr, backMultiplier);
        }
    }

    public void createTameAttackSystem(int timeMax, int timeToHit, SoundEvent sound, double width, double height, double reach, boolean spawnBlurr) {
        if (this.isAttacking()) {
            if (attackTimer < timeMax) attackTimer++;
            else {
                attackTimer = 0;
                setAttacking(false);
                return;
            }
            if (attackTimer == timeToHit) {
                attackEntitiesInFront(this.getDamage(), sound, width, height, reach, 1.0f);
                if (spawnBlurr) OWUtils.spawnBlurrParticle(this.level(), this, 1, 1, 1);
            }
        }
    }

    public void setAttackAnimation(int animationId) {
        this.entityData.set(ATTACK_ANIMATION_ID, animationId);
        this.entityData.set(ATTACK_ANIMATION_TICK, this.tickCount);
    }

    public int getAttackAnimation() {
        return this.entityData.get(ATTACK_ANIMATION_ID);
    }

    public int getAttackAnimationTick() {
        return this.entityData.get(ATTACK_ANIMATION_TICK);
    }

    public boolean breakBlocksAround(Vec3 center, float radius, boolean square, float dropChance) {
        if (this.isBaby() || !net.neoforged.neoforge.event.EventHooks.canEntityGrief(this.level(), this) || level().isClientSide) {
            return false;
        }
        boolean flag = false;
        for (BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(center.x - radius),
                Mth.floor(center.y - radius), Mth.floor(center.z - radius),
                Mth.floor(center.x + radius), Mth.floor(center.y + radius),
                Mth.floor(center.z + radius))) {
            BlockState blockstate = this.level().getBlockState(blockpos);

            if (blockstate.blocksMotion() && (blockstate.getBlock().getExplosionResistance() <= 15)
                    && (square || blockpos.distToCenterSqr(center.x, center.y, center.z) < radius * radius)) {
                Random random = new Random();
                if (random.nextFloat() <= dropChance) {
                    level().destroyBlock(blockpos, true);
                }
                flag = true;
            }
        }
        return flag;
    }

    private void handleClientAnimationSync() {
        int currentAnimId = getAttackAnimation();
        int animStartTick = getAttackAnimationTick();

        if (currentAnimId > 0) {
            switch (currentAnimId) {
                case 1:
                    if (!this.attackState.isStarted()) {
                        this.attackState.start(animStartTick);
                    }
                    break;
                case 2:
                    if (!this.attackState2.isStarted()) {
                        this.attackState.stop();
                        this.attackState2.start(animStartTick);
                    }
                    break;
                case 3:
                    if (!this.attackState3.isStarted()) {
                        this.attackState2.stop();
                        this.attackState3.start(animStartTick);
                    }
                    break;
            }
        } else {
            if (this.attackState.isStarted() || this.attackState2.isStarted() || this.attackState3.isStarted()) {
                this.attackState.stop();
                this.attackState2.stop();
                this.attackState3.stop();
            }
        }
    }

    public void setPauseCombo(boolean isCombo) {
        this.entityData.set(IS_COMBO_PAUSED, isCombo);
    }

    public boolean isPauseCombo() {
        return this.entityData.get(IS_COMBO_PAUSED);
    }

    public void setCombo(boolean isCombo, int numberOfAttacks) {
        this.entityData.set(IS_COMBO, isCombo);
        this.entityData.set(GET_COMBO_ATTACK, numberOfAttacks);
    }

    public boolean isCombo(int numberOfAttack) {
        return this.entityData.get(IS_COMBO) && this.entityData.get(GET_COMBO_ATTACK) == numberOfAttack;
    }

    public boolean isCombo() {
        return this.entityData.get(IS_COMBO);
    }

    public int getComboAttack() {
        return this.entityData.get(GET_COMBO_ATTACK);
    }

    public float calculateAnimatedYOffset(float animLength, float speedMultiplier, float heightMax, float heightMin, float amplitudeMultiplier) {
        float walkSpeed = this.walkAnimation.speed();
        float walkPos = this.walkAnimation.position();

        float animProgress = (walkPos * speedMultiplier / 20.0F) % animLength / animLength;
        float yOffset;

        if (animProgress < 0.24f / animLength) {
            float progress = animProgress / (0.24f / animLength);
            yOffset = heightMax + (heightMin - heightMax) * progress;
        } else if (animProgress < 0.4f / animLength) {
            yOffset = heightMin;
        } else if (animProgress < 0.56f / animLength) {
            float progress = (animProgress - (0.4f / animLength)) / ((0.56f - 0.4f) / animLength);
            yOffset = heightMin + (heightMax - heightMin) * progress;
        } else if (animProgress < 0.72f / animLength) {
            yOffset = heightMax;
        } else if (animProgress < 0.92f / animLength) {
            float progress = (animProgress - (0.72f / animLength)) / ((0.92f - 0.72f) / animLength);
            yOffset = heightMax + (heightMin - heightMax) * progress;
        } else if (animProgress < 1.12f / animLength) {
            yOffset = heightMin;
        } else if (animProgress < 1.28f / animLength) {
            float progress = (animProgress - (1.12f / animLength)) / ((1.28f - 1.12f) / animLength);
            yOffset = heightMin + (heightMax - heightMin) * progress;
        } else {
            yOffset = heightMax;
        }

        float amplitudeFactor = Math.min(1.0F, walkSpeed * amplitudeMultiplier);
        yOffset *= amplitudeFactor;
        yOffset /= 16.0F;

        return yOffset;
    }

    public void openChestAnimation(ChestBlockEntity chestBlockEntity) {
        if (chestBlockEntity.getLevel() != null && !chestBlockEntity.getLevel().isClientSide()) {
            chestBlockEntity.getLevel().playSound(
                    null,
                    chestBlockEntity.getBlockPos(),
                    SoundEvents.CHEST_OPEN,
                    SoundSource.BLOCKS,
                    0.5F,
                    chestBlockEntity.getLevel().random.nextFloat() * 0.1F + 0.9F
            );

            chestBlockEntity.getLevel().blockEvent(
                    chestBlockEntity.getBlockPos(),
                    chestBlockEntity.getBlockState().getBlock(),
                    1,
                    1
            );
        }
    }

    public void closeChestAnimation(ChestBlockEntity chestBlockEntity) {
        if (chestBlockEntity.getLevel() != null && !chestBlockEntity.getLevel().isClientSide()) {
            chestBlockEntity.getLevel().playSound(
                    null,
                    chestBlockEntity.getBlockPos(),
                    SoundEvents.CHEST_CLOSE,
                    SoundSource.BLOCKS,
                    0.5F,
                    chestBlockEntity.getLevel().random.nextFloat() * 0.1F + 0.9F
            );

            chestBlockEntity.getLevel().blockEvent(
                    chestBlockEntity.getBlockPos(),
                    chestBlockEntity.getBlockState().getBlock(),
                    1,
                    0
            );
        }
    }

    public void tickRidden(Player player, Vec3 vec3) {
        super.tickRidden(player, vec3);
        Vec2 vec2 = this.getRiddenRotation(player);
        if (!(this instanceof SeaBugEntity)) {
            this.setRot(vec2.y, vec2.x);
            this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
        }
        player.resetFallDistance();
    }

    public Vec2 getRiddenRotation(LivingEntity livingEntity) { return new Vec2(livingEntity.getXRot() * 0.5F, livingEntity.getYRot());}

    @javax.annotation.Nullable
    public LivingEntity getControllingPassenger() {
        if (this.hasEffect(OWEffects.FEAR_EFFECT.getDelegate())) return null;

        Entity entityRiding = this.getFirstPassenger();
        if (entityRiding != null && entityRiding instanceof LivingEntity living && living.zza == 0) {
            //return null;
        }
        if (entityRiding instanceof Mob) {
            return (Mob)entityRiding;
        } else {
            if (this.isSaddled()) {
                entityRiding = this.getFirstPassenger();
                if (entityRiding instanceof Player) {
                    return (Player)entityRiding;
                }
            }

            return null;
        }
    }

    @Override
    public boolean isInWall() {
        return false;
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        ItemStack stack = this.itemStackHandler.getStackInSlot(1);

        System.out.println(damageSource);

        if (!stack.isEmpty() && !this.level().isClientSide()) {
            this.spawnAtLocation(stack);
        }


        if (this.isInResurrection() && this.level().isClientSide()) {
            LivingEntity owner = this.getOwner();

            if (owner != null && owner instanceof Player) {
                owner.sendSystemMessage(Component.translatable("tooltip.resurrection_failed", this.getName().getString()).setStyle(Style.EMPTY.withBold(true).withColor(0xd94747)));
            }
        }
    }

    public void setHitboxSize(float width, float height) {
        this.customWidth = width;
        this.customHeight = height;
        this.refreshDimensions();
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return isBaby() ? EntityDimensions.scalable(customWidth, customHeight) : EntityDimensions.scalable(this.getBbWidth(), this.getBbHeight());
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity target) {
        createLevelSystem(target);
        createPrestigeLevelSystem(target);

        if (isQuestInProgress(DailyQuestRegistry.quest11) && !this.level().isClientSide()) {
            this.executeQuestProgression((byte) 10);
        }

        long currentTime = System.currentTimeMillis();
        if (lastKillTime == 0 || currentTime - lastKillTime > 250) {
            killCounter = 1;
        } else {
            killCounter++;
        }
        lastKillTime = currentTime;

        if (killCounter >= 5) {
            if (isQuestInProgress(DailyQuestRegistry.quest8) && !this.level().isClientSide()) {
                this.executeQuestProgression((byte) 7);
            }
            killCounter = 0;
        }

        if (lastKillTime2 == 0 || currentTime - lastKillTime2 > 5000) {
            killCounter2 = 1;
        } else {
            killCounter2++;
        }
        lastKillTime2 = currentTime;

        if (killCounter2 >= 10) {
            if (isQuestInProgress(DailyQuestRegistry.quest9) && !this.level().isClientSide()) {
                this.executeQuestProgression((byte) 8);
            }
            killCounter2 = 0;
        }

        if (isQuestInProgress(DailyQuestRegistry.quest7) && !this.level().isClientSide()) {
            this.executeQuestProgression((byte) 6);
        }

        return super.killedEntity(level, target);
    }

    private InteractionResult saveColor(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        Item item = itemstack.getItem();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

        int newColor = switch (itemId.toString()) {
            case "minecraft:white_dye" -> 0xD7CEC5;
            case "minecraft:orange_dye" -> 0xD87F33;
            case "minecraft:magenta_dye" -> 0xB24CD8;
            case "minecraft:light_blue_dye" -> 0x6699D8;
            case "minecraft:yellow_dye" -> 0xE5E533;
            case "minecraft:lime_dye" -> 0x7FCC19;
            case "minecraft:pink_dye" -> 0xF27FA5;
            case "minecraft:gray_dye" -> 0x4C4C4C;
            case "minecraft:light_gray_dye" -> 0x999999;
            case "minecraft:cyan_dye" -> 0x4C7F99;
            case "minecraft:purple_dye" -> 0x7F3FB2;
            case "minecraft:blue_dye" -> 0x3366CC;
            case "minecraft:brown_dye" -> 0x664C33;
            case "minecraft:green_dye" -> 0x667F33;
            case "minecraft:red_dye" -> 0x993333;
            case "minecraft:black_dye" -> 0x191919;
            default -> -1;
        };

        if (newColor == -1) {
            return InteractionResult.PASS;
        }

        if (getNecklaceColor() != newColor) {
            setNecklaceColor(newColor);
            if (!player.isCreative()) itemstack.shrink(1);
            player.swing(hand, true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.SUCCESS;
    }


    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        Item item = itemstack.getItem();

        if (isTame() && isBaby() && babyQuestIsInProgress && choosenQuest == 0 && !level().isClientSide()) {
            if (item == choosenFood) {
                resetBabyQuest(true);
                itemstack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        if (this.getHealth() < this.getMaxHealth() && itemstack.is(Tags.Items.FOODS) && this.isTame()) {
            itemstack.shrink(1);
            healWithFavoriteFood(1.5f, preferRawMeat(), preferCookedMeat());
            return InteractionResult.SUCCESS;
        }

        if (itemstack.is(Tags.Items.DYES) && isTame()) {
            saveColor(player, hand);
            return InteractionResult.SUCCESS;
        }

        if (this.isTame() && !isBaby() && !this.level().isClientSide) {
            if (itemstack.is(Items.STICK)) {
                Player owner = (Player) this.getOwner();
                if (player == owner) {
                    this.switchMode(player);
                }
                return InteractionResult.SUCCESS;
            }
        }

        if (this.isTame() && !isBaby() && !this.level().isClientSide() && hand == InteractionHand.MAIN_HAND) {
            if (player.isSteppingCarefully() && !this.isInResurrection()) {
                if (this.sittingCooldown > 0) return InteractionResult.PASS;

                if (this instanceof TigerEntity tiger) {
                    if (!tiger.isJumpingOnTarget() && !tiger.isTrappingEntity()) {
                        tiger.setSitting(!isSitting());
                        this.sittingCooldown = 20;
                        if (player instanceof ServerPlayer serverPlayer) {
                            if (!this.isSitting()) OWUtils.showMessage(serverPlayer, "tooltip.following", TextColor.fromRgb(0xFFFFFF), false);
                            else OWUtils.showMessage(serverPlayer, "tooltip.sitting", TextColor.fromRgb(0xFFFFFF), false);
                        }
                    }
                } else {
                    this.setSitting(!isSitting());
                    this.sittingCooldown = 20;
                    if (player instanceof ServerPlayer serverPlayer) {
                        if (!this.isSitting()) OWUtils.showMessage(serverPlayer, "tooltip.following", TextColor.fromRgb(0xFFFFFF), false);
                        else OWUtils.showMessage(serverPlayer, "tooltip.sitting", TextColor.fromRgb(0xFFFFFF), false);
                    }
                }
                return InteractionResult.SUCCESS;
            } else {
                if (!this.isSitting() && !this.isInResurrection() && !itemstack.is(Tags.Items.FOODS)) {
                    if (!this.isBaby()) player.startRiding(this);
                }
            }
        }

        return super.mobInteract(player, hand);
    }

    public void handleRunningEffects(int maxRunTime, SoundEvent soundEvent, float pitch, int[] runTimeSound) {
        if (((this.isRunning()) || getTarget() != null)) {
            if (this.level().isClientSide()) {
                if (this.getDeltaMovement().horizontalDistanceSqr() > 0.01) {
                    runTime++;
                    if (runTime >= maxRunTime) runTime = 0;
                    if ((runTime == runTimeSound[0] || runTime == runTimeSound[1]) && this.onGround()) {
                        this.level().playLocalSound(
                                this.getX(), this.getY(), this.getZ(),
                                soundEvent,
                                this.getSoundSource(),
                                0.8f, pitch,
                                false
                        );
                    }
                } else {
                    runTime = 0;
                }
            }
        }
    }

    @Override
    public PlayerTeam getTeam() { return super.getTeam();}

    @Override
    public boolean removeWhenFarAway(double v) { return false;}

    @Override
    public boolean canBeLeashed() { return super.canBeLeashed();}

    public void setSaddle(boolean isSaddled) { this.entityData.set(SADDLED, isSaddled);}

    public boolean isSaddled() { return this.entityData.get(SADDLED); }

    public void setFed(boolean isFed) { this.entityData.set(IS_FED, isFed);}

    public boolean isFed() { return this.entityData.get(IS_FED); }

    public boolean isTame() { return (this.entityData.get(DATA_FLAGS_ID) & 4) != 0;}

    public void addTamingExperience(double experience, Player player) {
        double oldExperience = ClientEvents.tamingExperience;
        ClientEvents.tamingExperience += experience;

        for (double threshold : OWEntityJournalScreen.THRESHOLDS) {
            if (threshold > OWEntityJournalScreen.lastReachedThreshold && oldExperience < threshold && ClientEvents.tamingExperience >= threshold) {
                OWEntityJournalScreen.lastReachedThreshold = threshold;

                Set<String> unlockedEntities = new HashSet<>();
                if (ClientEvents.tamingExperience >= TigerEntity.TAMING_EXPERIENCE) unlockedEntities.add("tiger");
                if (ClientEvents.tamingExperience >= TigerSharkEntity.TAMING_EXPERIENCE) unlockedEntities.add("tiger_shark");
                if (ClientEvents.tamingExperience >= BoaEntity.TAMING_EXPERIENCE) unlockedEntities.add("boa");
                if (ClientEvents.tamingExperience >= PeacockEntity.TAMING_EXPERIENCE) unlockedEntities.add("peacock");
                if (ClientEvents.tamingExperience >= HyenaEntity.TAMING_EXPERIENCE) unlockedEntities.add("hyena");
                if (ClientEvents.tamingExperience >= JellyfishEntity.TAMING_EXPERIENCE) unlockedEntities.add("jellyfish");
                if (ClientEvents.tamingExperience >= KodiakEntity.TAMING_EXPERIENCE) unlockedEntities.add("kodiak");
                if (ClientEvents.tamingExperience >= ChameleonEntity.TAMING_EXPERIENCE) unlockedEntities.add("chameleon");
                if (ClientEvents.tamingExperience >= WalrusEntity.TAMING_EXPERIENCE) unlockedEntities.add("walrus");
                if (ClientEvents.tamingExperience >= MantaEntity.TAMING_EXPERIENCE) unlockedEntities.add("manta");
                if (ClientEvents.tamingExperience >= RedPandaEntity.TAMING_EXPERIENCE) unlockedEntities.add("red_panda");
                if (ClientEvents.tamingExperience >= ElephantEntity.TAMING_EXPERIENCE) unlockedEntities.add("elephant");
                if (ClientEvents.tamingExperience >= MandrillEntity.TAMING_EXPERIENCE) unlockedEntities.add("mandrill");

                for (String entityType : unlockedEntities) {
                    if (!OWEntityJournalScreen.newEntitiesTamed.contains(entityType)) {
                        OWNetworkHandler.sendToClient(new BookNotificationPacket(entityType, true), (ServerPlayer) player);
                    }
                }
                break;
            }
        }

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getServer().getCommands().performPrefixedCommand(serverPlayer.getServer().createCommandSourceStack().withSuppressedOutput(), "advancement grant " + serverPlayer.getGameProfile().getName() + " only " + OperationWild.MOD_ID + ":" + selectTamingAdvancement(ClientEvents.tamingExperience));
        }
        saveTamingExperience(player);
    }

    private String selectTamingAdvancement(double tamingExperience) {
        if (tamingExperience >= 4000) return "legendary_adventurer";
        else if (tamingExperience >= 2700) return "professional_tamer";
        else if (tamingExperience >= 1500) return "renowned_tamer";
        else if (tamingExperience >= 300) return "novice_tamer";
        return "";
    }

    public static void saveTamingExperience(Player player) {
        try {
            String worldName = ClientEvents.getWorldName(player);
            String filePath = "saves/" + worldName + "/owDatas.properties";

            Properties props = new Properties();
            File file = new File(filePath);

            if (file.exists()) {
                try (FileInputStream input = new FileInputStream(filePath)) {
                    props.load(input);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            props.setProperty("tamingExperience", String.valueOf(ClientEvents.tamingExperience));

            file.getParentFile().mkdirs();

            try (FileOutputStream output = new FileOutputStream(filePath)) {
                props.store(output, "Operation Wild - Data");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 8) {
            if (this.level().isClientSide()) {
                for (int i = 0; i < 50; i++) {
                    OWUtils.spawnParticles(this, ParticleTypes.TOTEM_OF_UNDYING, 0, 0, 0, i, (double) i / 5);
                }
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    public void setTame(boolean tame, Player player) {
        if (!this.level().isClientSide()) {
            byte b0 = this.entityData.get(DATA_FLAGS_ID);
            if (tame) {
                setNecklaceColor(0x993333);
                int levelPointsBonus = 0;
                if (!(this instanceof BoaEntity boa)) {
                    levelPointsBonus = this.getHealth() < this.getMaxHealth() / 2 ? 0 : (int) ((this.getHealth() - (this.getMaxHealth() / 2)) / (this.getMaxHealth() / 10));
                } else {
                    levelPointsBonus = boa.numberOfError >= 5 ? 0 : 5 - boa.numberOfError;
                }
                if (!isBaby()) this.setLevelPoints(levelPointsBonus);
                this.entityData.set(DATA_FLAGS_ID, (byte) (b0 | 4));
                this.setTamedAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH));
                this.navigation.recomputePath();
                this.setTarget(null);
                this.level().broadcastEntityEvent(this, (byte) 7);
                this.setSitting(false);
                this.setHealth(this.getMaxHealth());
                double pitch = OWUtils.generateRandomInterval(0.8, 1.0);
                this.playSound(OWSounds.TAME_SUCCESS.get(), 1.0f, (float) pitch);
                this.playSound(SoundEvents.TOTEM_USE);
                this.level().broadcastEntityEvent(this, (byte) 8);
                this.setOwnerUUID(player.getUUID());
                this.setDamageToClient(this.getDamage());
                this.setCurrentMode(Mode.Passive);
                this.setPassive(true);

                if (player instanceof ServerPlayer serverPlayer) {
                    AdvancementHolder advancement = player.getServer().getAdvancements().get(this.getTamingAdvancement());
                    if (advancement != null) {
                        serverPlayer.getAdvancements().award(advancement, "tamed_" + this.getClass().getSimpleName().toLowerCase().split("entity")[0]);
                    }
                }

            } else {
                this.entityData.set(DATA_FLAGS_ID, (byte) (b0 & -5));
            }
        }
    }

    public static void addExperienceCommand(OWEntity entity, int amount) {
        if (!entity.isTame() || entity.getLevel() >= 50) return;

        float xpAmount = amount;
        int xpStage = OWUtils.generateExponentialExp(entity.getLevel(), 20);
        entity.setXpStage(xpStage);

        boolean hasLeveledUp = false;

        while (xpAmount > 0 && entity.getLevel() < 50) {
            float xpStageRest = entity.getXpStage() - entity.getXp();

            if (xpAmount >= xpStageRest) {
                xpAmount -= xpStageRest;

                entity.passingLevel(!hasLeveledUp);
                hasLeveledUp = true;

                if (entity.getLevel() >= 50) {
                    entity.setXp(0);
                    int prestigeXpStage = OWUtils.generateExponentialExp(entity.getPrestigeLevel(), 2);
                    entity.setPrestigeXpStage(prestigeXpStage);
                    return;
                }

            } else {
                entity.setXp(entity.getXp() + xpAmount);
                xpAmount = 0;
            }
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity living) {
        this.setRunning(false);
        return super.getDismountLocationForPassenger(living);
    }

    private ResourceLocation selectAdvancementByEntity() {
        return this.getTamingAdvancement();
    }

    public boolean shouldTryTeleportToOwner() {
        if (this.isBaby()) return false;
        if (this instanceof TigerEntity tiger) if (tiger.isJumpingOnTarget() || tiger.isTrappingEntity()) return false;
        LivingEntity livingentity = this.getOwner();
        return livingentity != null && this.distanceToSqr(this.getOwner()) >= (double) 432.0F;
    }

    @Override
    public int getEntityColor() {
        return 0x000000;
    }

    @Override
    public float getTheoreticalScale() {
        return 0;
    }

    @Override
    public double getTamingExperience() {
        return 0;
    }

    @Override
    public OWEntityConfig.Archetypes getArchetype() {
        return null;
    }

    @Override
    public OWEntityConfig.Diet getDiet() {
        return null;
    }

    @Override
    public OWEntityConfig.Temperament getTemperament() {
        return null;
    }

    @Override
    public float vehicleRunSpeedMultiplier() {
        return 0;
    }

    @Override
    public float vehicleWalkSpeedMultiplier() {
        return 0;
    }

    @Override
    public float vehicleComboSpeedMultiplier() {
        return 0;
    }

    @Override
    public float vehicleWaterSpeedDivider() {
        return 0;
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
        return null;
    }

    @Override
    public ResourceLocation getTamingAdvancement() {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "");
    }

    @Override
    public float getMaxVitalEnergy() {
        return 0;
    }

    @Override
    public float getVitalEnergyRecuperation() {
        return 0;
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
        return false;
    }

    public enum Mode {
        Passive,
        Aggressive;
    }
    public Mode currentMode;
    public Mode getCurrentMode() { return currentMode;}
    public void setCurrentMode(Mode mode) { this.currentMode = mode;}

    public void switchMode(Player player) {
        Component tooltipMode = null;
        if (currentMode == null) {
            currentMode = Mode.Passive;
        }

        switch (currentMode) {
            case Passive:
                currentMode = Mode.Aggressive;
                tooltipMode = Component.translatable("tooltip.modeAggressive");
                this.setPassive(false);
                break;
            case Aggressive:
                currentMode = Mode.Passive;
                tooltipMode = Component.translatable("tooltip.modePassive");
                this.setPassive(true);
                break;
        }
        if (player != null) {
            boolean isAggressive = currentMode == Mode.Aggressive;
            OWUtils.showMode((ServerPlayer) player, isAggressive);
        }

    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        setMaxSleepingBarTo((int) OWUtils.determinateMinAndMax(maxSleepBar, 20));
        if (!(this instanceof SeaBugEntity) && !(this instanceof JellyfishEntity)) this.setRandomScale(averageScale, 0.95, 1.05);
        else this.setScale(1.0f);
        this.setGender(this.random.nextInt(2));
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
    }

    @Override
    public boolean isFood(ItemStack itemStack) { return false;}

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    public void setAscentMissionValue(AscentMission mission, double $$0) {
        if (!this.level().isClientSide() && this.isTame()) {
            mission.setActualValue($$0);
        }
    }

    public void attackEntitiesInFront(float attackDamage, SoundEvent sound, double width, double height, double reach, float $$1) {
        float pitch = (float) OWUtils.generateRandomInterval(0.8, 1.1f);
        double yaw = Math.toRadians(this.getYRot());
        double centerX = this.getX() - Math.sin(yaw) * reach;
        double centerZ = this.getZ() + Math.cos(yaw) * reach;
        double centerY = this.getY() + 0.5;

        double extendedHeight = height * 2;

        AABB attackBox = new AABB(
                centerX - width / 2, centerY - extendedHeight / 2, centerZ - width / 2,
                centerX + width / 2, centerY + extendedHeight / 2, centerZ + width / 2
        );

        List<Entity> entitiesInRange = this.level().getEntities(
                this,
                attackBox,
                entity -> entity instanceof LivingEntity
        );

        UUID ownerUUID = null;
        if (this instanceof TamableAnimal tamable) ownerUUID = tamable.getOwnerUUID();

        for (Entity entity : entitiesInRange) {
            if (entity instanceof LivingEntity livingEntity) {
                if (ownerUUID != null && entity instanceof Player player && player.getUUID().equals(ownerUUID)) continue;
                if (this.isAlliedTo(livingEntity)) continue;
                if (ownerUUID != null && entity instanceof TamableAnimal otherTamable && otherTamable.getOwnerUUID() != null && otherTamable.getOwnerUUID().equals(ownerUUID)) continue;
                if (livingEntity instanceof Player player && player.getVehicle() != null) continue;
                if (this.isAssassin() && OWUtils.RANDOM(10)) {
                    livingEntity.hurt(this.damageSources().mobAttack(this), attackDamage *= 1.25f);

                    if ($$1 > 0) {
                        Vec3 knockbackDirection = livingEntity.position().subtract(this.position()).normalize();
                        Vec3 knockback = knockbackDirection.scale($$1 * 0.5);
                        livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(knockback.x, knockback.y * 0.3, knockback.z));
                    }

                    OWUtils.spawnParticles(livingEntity, ParticleTypes.CRIT, 0, 0.5, 0, 20, 3);
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.NEUTRAL, 1.0F, pitch);
                } else {
                    boolean isTigerInUltimate = this instanceof TigerEntity tiger && tiger.isUltimate();
                    if (this instanceof BoaEntity boa) boa.doHurtTarget(livingEntity);
                    else livingEntity.hurt(this.damageSources().mobAttack(this), isTigerInUltimate ? attackDamage * 1.5f : attackDamage);

                    if ($$1 > 0) {
                        Vec3 knockbackDirection = livingEntity.position().subtract(this.position()).normalize();
                        Vec3 knockback = knockbackDirection.scale($$1 * 0.4);
                        livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(knockback.x, knockback.y * 0.3, knockback.z));
                    }

                    if (this.isTame() && ownerIsRiding()) {
                        this.setFighting(true);
                    }
                    if (this instanceof BoaEntity boa) {
                        if (boa.canVenom) {
                            livingEntity.addEffect(new MobEffectInstance(OWEffects.VENOM_EFFECT.getDelegate(), (int) generateRandomInterval(3600, 6000), 0));
                            boa.venomCooldown = 800;
                            boa.canVenom = false;
                        }
                        else if (RANDOM(3))livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, (int) generateRandomInterval(176, 352), 2));
                        OWUtils.spawnBlurrParticle(this.level(), this, 0.75f, 1f, 0.67f);
                    }
                }

                if (isQuestInProgress(DailyQuestRegistry.quest1) && !this.level().isClientSide()) {
                    this.executeQuestProgression((byte) 0);
                }
                if (isQuestInProgress(DailyQuestRegistry.quest10) && livingEntity.getMaxHealth() > this.getMaxHealth() && !this.level().isClientSide()) {
                    this.executeQuestProgression((byte) 9);
                }

                long currentTime = System.currentTimeMillis();
                if (lastHurtTime == 0 || currentTime - lastHurtTime > 5000) {
                    hitCounter = 1;
                } else {
                    hitCounter += (int) attackDamage;
                }
                lastHurtTime = currentTime;

                if (hitCounter >= 40) {
                    if (isQuestInProgress(DailyQuestRegistry.quest5)) {
                        this.executeQuestProgression((byte) 4);
                    }
                    hitCounter = 0;
                }

            }
        }
        fightingTime = 200;
        this.setFighting(true);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), sound, SoundSource.NEUTRAL, 1.0F, pitch);
    }

    public final Map<String, TransitionData> transitions = new HashMap<>();

    public void createTransitionAnimation(String name, AnimationState animationState, boolean condition, int maxDuration) {
        if (this.level().isClientSide()) {
            TransitionData data = transitions.computeIfAbsent(name, k -> new TransitionData(animationState));

            if (condition && !data.shouldPlay) {
                data.animationState.start(this.tickCount);
                data.shouldPlay = true;
                data.timer = 0;
            }

            if (!condition) {
                data.shouldPlay = false;
            }

            if (data.shouldPlay && data.timer < maxDuration) {
                data.timer++;
            } else if (data.timer >= maxDuration) {
                data.animationState.stop();
            }

            if (this.tickCount < 60 && data.shouldPlay) {
                data.animationState.stop();
            }
        }
    }

    public static class TransitionData {
        AnimationState animationState;
        boolean shouldPlay = false;
        int timer = 0;

        TransitionData(AnimationState animationState) {
            this.animationState = animationState;
        }
    }

    public int getTypeVariant() { return this.entityData.get(VARIANT);}

    public void createIdleAnimation(int maxDuration, boolean condition) {
        if (!condition) return;

        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = maxDuration;
            this.idleAnimationState.start(this.tickCount);
        } else --this.idleAnimationTimeout;
    }

    public void createAttackAnimation(int maxDuration, boolean condition) {
        if (!condition) return;

        if ((this.getState() == 1 || this.isAttacking())&& this.isAlive() && this.attackAnimationTimeout <= 0) {
            this.attackAnimationTimeout = maxDuration;
            this.attackAnimationState.start(this.tickCount);
        } else --this.attackAnimationTimeout;

        if (this.getState() != 1 && !this.isAttacking()) {
            attackAnimationState.stop();
        }
    }

    public void createSitAnimation(int maxDuration, boolean condition) {
        if (!condition) return;

        if (this.isSitting()) {
            if (this.sittingAnimationTimeout <= 0) {
                this.sittingAnimationTimeout = maxDuration;
                this.sittingAnimationState.start(this.tickCount);
            } else --this.sittingAnimationTimeout;
        } else {
            this.sittingAnimationTimeout = 0;
            this.sittingAnimationState.stop();
        }
    }


    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, 0);
        builder.define(STATE, 0);
        builder.define(XP, 0f);
        builder.define(STAGE, 0);
        builder.define(PRESTIGE_STAGE, 0);
        builder.define(LEVEL, 0);
        builder.define(PRESTIGE_LEVEL, 0);
        builder.define(LEVEL_POINTS, 0);
        builder.define(NECKLACE_COLOR, 0);
        builder.define(SCALE, 1.0f);
        builder.define(VITAL_ENERGY, 0.0f);
        builder.define(BASE_HEALTH, 0.0f);
        builder.define(BASE_DAMAGE, 0.0f);
        builder.define(BASE_SPEED, 0.0f);
        builder.define(DAMAGE_TO_CLIENT, 0.0f);
        builder.define(ATTACK_ANIMATION_ID, 0);
        builder.define(ATTACK_ANIMATION_TICK, 0);
        builder.define(ACCELERATION, 0.0f);
        builder.define(NAPPING, false);
        builder.define(PREPARE_NAP, false);
        builder.define(IS_IN_FIGHT, false);
        builder.define(SITTING, false);
        builder.define(IS_ATTACKING, false);
        builder.define(IS_COMBO, false);
        builder.define(IS_COMBO_PAUSED, false);
        builder.define(GET_COMBO_ATTACK, 0);
        builder.define(SADDLED, false);
        builder.define(IS_FEMALE, false);
        builder.define(IS_RUNNING, false);
        builder.define(IS_PASSIVE, false);
        builder.define(BODY_Z_ROT, 0.0f);
        builder.define(BODY_X_ROT, 0.0f);
        builder.define(BODY_Y_OFFSET, 0.0f);
        builder.define(IS_FALLING, false);
        builder.define(IS_FED, false);
        builder.define(ITEM_FOOD, ItemStack.EMPTY);
        builder.define(FOOD_COUNT, 0);
        builder.define(AUTO_PICKUP, true);
        builder.define(ULTIMATE, false);
        builder.define(QUESTS_ARE_UPDATED, false);
        builder.define(TAMING_PERCENTAGE, 0);
        builder.define(MATURATION_PERCENTAGE, 0f);
        builder.define(IS_SLEEPING, false);
        builder.define(RE_UPDATED_QUESTS, false);
        builder.define(IS_IN_RESURRECTION, false);
        builder.define(IS_BABY, false);
        builder.define(CAN_DROP_SOUL, true);
        builder.define(ACTUAL_SLEEPING_BAR, 0);
        builder.define(MAX_SLEEPING_BAR, 0);
        builder.define(RESURRECTION_MAX_TIMER, 0);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        if (!this.getActiveEffects().isEmpty()) {
            ListTag effectsTag = new ListTag();
            for (MobEffectInstance effect : this.getActiveEffects()) {
                CompoundTag effectTag = (CompoundTag) effect.save();
                effectsTag.add(effectTag);
            }
            tag.put("ActiveEffects", effectsTag);
        }

        if (level() != null && level().getServer() != null) {
            HolderLookup.Provider provider = level().getServer().registryAccess();
            tag.put("ItemStackHandler", itemStackHandler.serializeNBT(provider));
        }
        if (level() != null && level().getServer() != null) {
            HolderLookup.Provider provider = level().getServer().registryAccess();
            tag.put("itemStackHandlerSeaBug", itemStackHandlerSeaBug.serializeNBT(provider));
        }
        tag.putFloat("XP", this.getXp());
        tag.putInt("getXpStage", this.getXpStage());
        tag.putInt("getPrestigeXpStage", this.getPrestigeXpStage());
        tag.putInt("Level", this.getLevel());
        tag.putInt("getPrestigeLevel", this.getPrestigeLevel());
        tag.putInt("getNecklaceColor", this.getNecklaceColor());
        tag.putInt("LevelPoints", this.getLevelPoints());
        tag.putFloat("Scale", this.getScale());
        tag.putBoolean("isPassive", this.isPassive());
        tag.putBoolean("isFemale", this.isFemale());
        tag.putBoolean("isPreparingNapping", this.isPreparingNapping());
        tag.putBoolean("isFed", this.isFed());
        tag.putBoolean("isSitting", this.isSitting());
        tag.putBoolean("isSaddled", this.isSaddled());
        tag.putBoolean("isBaby", this.isBaby());
        tag.putFloat("getBaseHealth", this.getBaseHealth());
        tag.putFloat("getBaseDamage", this.getBaseDamage());
        tag.putFloat("getBaseSpeed", this.getBaseSpeed());
        tag.putFloat("getDamageToClient", this.getDamageToClient());
        tag.putInt("getFoodCount", this.getFoodCount());
        tag.putBoolean("questsAreUpdated", this.questsAreUpdated());
        tag.putInt("getTamingPercentage", this.getTamingPercentage());
        tag.putFloat("getMaturationPercentage", this.getMaturationPercentage());
        tag.putBoolean("isSleeping", this.isSleeping());
        tag.putInt("getActualSleepingBar", this.getActualSleepingBar());
        tag.putInt("getMaxSleepingBar", this.getMaxSleepingBar());
        tag.putBoolean("canReUpdatedDailyQuests", this.canReUpdatedDailyQuests());
        tag.putBoolean("isInResurrection", this.isInResurrection());
        tag.putBoolean("canDropSoul", this.canDropSoul());
        tag.putInt("getResurrectionMaxTimer", this.getResurrectionMaxTimer());

        tag.putFloat("actualMaturation", this.actualMaturation);

        ItemStack foodStack = this.entityData.get(ITEM_FOOD);
        if (!foodStack.isEmpty() && level() != null) {
            CompoundTag itemTag = (CompoundTag) foodStack.save(level().registryAccess());
            tag.put("ItemFood", itemTag);
        }

        if (choosenFood != null) {
            ItemStack choosenFoodStack = choosenFood.getDefaultInstance();
            if (!choosenFoodStack.isEmpty() && level() != null) {
                CompoundTag itemTag = (CompoundTag) choosenFoodStack.save(level().registryAccess());
                tag.put("choosenFood", itemTag);
            }
        }

        if (currentMode != null) {
            tag.putString("CurrentMode", currentMode.name());
        }

        tag.putFloat("maxHealthBeforeResurrection", this.maxHealthBeforeResurrection);
        tag.putInt("resurrectionTimer", this.resurrectionTimer);
        tag.putFloat("maxMaturation", this.maxMaturation);
        tag.putFloat("actualMaturation", this.actualMaturation);
        tag.putFloat("maxHealth", this.maxHealth);
        tag.putInt("delayBeforeBabyTask", this.delayBeforeBabyTask);
        tag.putInt("choosenQuest", this.choosenQuest);
        tag.putString("choosenQuestStr", this.choosenQuestStr != null ? this.choosenQuestStr : "");
        tag.putInt("babyQuestProgressTimer", this.babyQuestProgressTimer);
        tag.putBoolean("babyQuestIsInProgress", this.babyQuestIsInProgress);
        tag.putInt("ultimateCooldown", this.ultimateCooldown);


        tag.putInt("quest0Progression", this.quest0Progression);
        tag.putInt("quest1Progression", this.quest1Progression);
        tag.putInt("quest2Progression", this.quest2Progression);
        tag.putInt("quest3Progression", this.quest3Progression);
        tag.putInt("quest4Progression", this.quest4Progression);
        tag.putInt("quest5Progression", this.quest5Progression);
        tag.putInt("quest6Progression", this.quest6Progression);
        tag.putInt("quest7Progression", this.quest7Progression);
        tag.putInt("quest8Progression", this.quest8Progression);
        tag.putInt("quest9Progression", this.quest9Progression);
        tag.putInt("quest10Progression", this.quest10Progression);

        tag.putBoolean("quest0isLocked", this.quest0isLocked);
        tag.putBoolean("quest1isLocked", this.quest1isLocked);
        tag.putBoolean("quest2isLocked", this.quest2isLocked);
        tag.putBoolean("quest3isLocked", this.quest3isLocked);
        tag.putBoolean("quest4isLocked", this.quest4isLocked);
        tag.putBoolean("quest5isLocked", this.quest5isLocked);
        tag.putBoolean("quest6isLocked", this.quest6isLocked);
        tag.putBoolean("quest7isLocked", this.quest7isLocked);
        tag.putBoolean("quest8isLocked", this.quest8isLocked);
        tag.putBoolean("quest9isLocked", this.quest9isLocked);
        tag.putBoolean("quest10isLocked", this.quest10isLocked);
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("ActiveEffects")) {
            ListTag effectsTag = tag.getList("ActiveEffects", 10);
            for (int i = 0; i < effectsTag.size(); i++) {
                CompoundTag effectTag = effectsTag.getCompound(i);
                MobEffectInstance effect = MobEffectInstance.load(effectTag);
                if (effect != null) {
                    this.addEffect(effect);
                }
            }
        }

        if (tag.contains("ItemStackHandler") && level() != null && level().getServer() != null) {
            HolderLookup.Provider provider = level().getServer().registryAccess();
            itemStackHandler.deserializeNBT(provider, tag.getCompound("ItemStackHandler"));
        }
        if (tag.contains("itemStackHandlerSeaBug") && level() != null && level().getServer() != null) {
            HolderLookup.Provider provider = level().getServer().registryAccess();
            itemStackHandlerSeaBug.deserializeNBT(provider, tag.getCompound("itemStackHandlerSeaBug"));
        }
        this.entityData.set(XP, tag.getFloat("XP"));
        this.entityData.set(STAGE, tag.getInt("getXpStage"));
        this.entityData.set(PRESTIGE_STAGE, tag.getInt("getPrestigeXpStage"));
        this.entityData.set(LEVEL, tag.getInt("Level"));
        this.entityData.set(PRESTIGE_LEVEL, tag.getInt("getPrestigeLevel"));
        this.entityData.set(NECKLACE_COLOR, tag.getInt("getNecklaceColor"));
        this.entityData.set(LEVEL_POINTS, tag.getInt("LevelPoints"));
        this.entityData.set(SCALE, tag.getFloat("Scale"));
        this.entityData.set(IS_PASSIVE, tag.getBoolean("isPassive"));
        this.entityData.set(IS_FEMALE, tag.getBoolean("isFemale"));
        this.entityData.set(SADDLED, tag.getBoolean("isSaddled"));
        this.entityData.set(IS_FED, tag.getBoolean("isFed"));
        this.entityData.set(PREPARE_NAP, tag.getBoolean("isPreparingNapping"));
        this.entityData.set(SITTING, tag.getBoolean("isSitting"));
        this.entityData.set(IS_BABY, tag.getBoolean("isBaby"));
        this.entityData.set(BASE_HEALTH, tag.getFloat("getBaseHealth"));
        this.entityData.set(BASE_DAMAGE, tag.getFloat("getBaseDamage"));
        this.entityData.set(BASE_SPEED, tag.getFloat("getBaseSpeed"));
        this.entityData.set(DAMAGE_TO_CLIENT, tag.getFloat("getDamageToClient"));
        this.entityData.set(FOOD_COUNT, tag.getInt("getFoodCount"));
        this.entityData.set(QUESTS_ARE_UPDATED, tag.getBoolean("questsAreUpdated"));
        this.entityData.set(TAMING_PERCENTAGE, tag.getInt("getTamingPercentage"));
        this.entityData.set(MATURATION_PERCENTAGE, tag.getFloat("getMaturationPercentage"));
        this.entityData.set(IS_SLEEPING, tag.getBoolean("isSleeping"));
        this.entityData.set(ACTUAL_SLEEPING_BAR, tag.getInt("getActualSleepingBar"));
        this.entityData.set(MAX_SLEEPING_BAR, tag.getInt("getMaxSleepingBar"));
        this.entityData.set(RE_UPDATED_QUESTS, tag.getBoolean("canReUpdatedDailyQuests"));
        this.entityData.set(IS_IN_RESURRECTION, tag.getBoolean("isInResurrection"));
        this.entityData.set(CAN_DROP_SOUL, tag.getBoolean("canDropSoul"));
        this.entityData.set(RESURRECTION_MAX_TIMER, tag.getInt("getResurrectionMaxTimer"));

        if (tag.contains("ItemFood", Tag.TAG_COMPOUND)) {
            CompoundTag itemTag = tag.getCompound("ItemFood");
            Optional<ItemStack> optionalStack = ItemStack.parse(level().registryAccess(), itemTag);
            optionalStack.ifPresent(stack -> this.entityData.set(ITEM_FOOD, stack));
        }

        if (tag.contains("choosenFood", Tag.TAG_COMPOUND)) {
            CompoundTag itemTag = tag.getCompound("choosenFood");
            Optional<ItemStack> optionalStack = ItemStack.parse(level().registryAccess(), itemTag);
            optionalStack.ifPresent(stack -> choosenFood = stack.getItem());
        }

        if (tag.contains("CurrentMode")) {
            currentMode = Mode.valueOf(tag.getString("CurrentMode"));
        }

        this.actualMaturation = tag.getFloat("actualMaturation");

        this.maxHealthBeforeResurrection = tag.getFloat("maxHealthBeforeResurrection");
        this.resurrectionTimer = tag.getInt("resurrectionTimer");
        this.maxHealth = tag.getFloat("maxHealth");
        this.maxMaturation = tag.getFloat("maxMaturation");
        this.actualMaturation = tag.getFloat("actualMaturation");
        this.delayBeforeBabyTask = tag.getInt("delayBeforeBabyTask");
        this.choosenQuest = tag.getInt("choosenQuest");
        this.choosenQuestStr = tag.getString("choosenQuestStr");
        this.babyQuestProgressTimer = tag.getInt("babyQuestProgressTimer");
        this.babyQuestIsInProgress = tag.getBoolean("babyQuestIsInProgress");
        this.ultimateCooldown = tag.getInt("ultimateCooldown");

        this.quest0Progression = tag.getInt("quest0Progression");
        this.quest1Progression = tag.getInt("quest1Progression");
        this.quest2Progression = tag.getInt("quest2Progression");
        this.quest3Progression = tag.getInt("quest3Progression");
        this.quest4Progression = tag.getInt("quest4Progression");
        this.quest5Progression = tag.getInt("quest5Progression");
        this.quest6Progression = tag.getInt("quest6Progression");
        this.quest7Progression = tag.getInt("quest7Progression");
        this.quest8Progression = tag.getInt("quest8Progression");
        this.quest9Progression = tag.getInt("quest9Progression");
        this.quest10Progression = tag.getInt("quest10Progression");

        this.quest0isLocked = tag.getBoolean("quest0isLocked");
        this.quest1isLocked = tag.getBoolean("quest1isLocked");
        this.quest2isLocked = tag.getBoolean("quest2isLocked");
        this.quest3isLocked = tag.getBoolean("quest3isLocked");
        this.quest4isLocked = tag.getBoolean("quest4isLocked");
        this.quest5isLocked = tag.getBoolean("quest5isLocked");
        this.quest6isLocked = tag.getBoolean("quest6isLocked");
        this.quest7isLocked = tag.getBoolean("quest7isLocked");
        this.quest8isLocked = tag.getBoolean("quest8isLocked");
        this.quest9isLocked = tag.getBoolean("quest9isLocked");
        this.quest10isLocked = tag.getBoolean("quest10isLocked");
    }
}
