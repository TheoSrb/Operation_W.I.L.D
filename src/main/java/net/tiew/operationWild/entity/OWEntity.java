package net.tiew.operationWild.entity;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.entity.ai.control.LookControl;
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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.tiew.operationWild.component.OWDataComponentTypes;
import net.tiew.operationWild.component.SoulData;
import net.tiew.operationWild.core.OWDatasSave;
import net.tiew.operationWild.core.OWGameRules;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.enchantment.OWEnchantments;
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
import net.tiew.operationWild.screen.entity.OWChooseNameScreen;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTribesSavedData;
import java.util.UUID;
import net.tiew.operationWild.team.OWTeamMosaicPattern;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.bosses.PlantEmpressEntity;
import net.tiew.operationWild.entity.quests.daily_quests.DailyQuest;
import net.tiew.operationWild.entity.quests.daily_quests.DailyQuestRegistry;
import net.tiew.operationWild.entity.quests.daily_quests.OWDailyQuests;
import net.tiew.operationWild.event.ClientEvents;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.ConsumeItemPacket;
import net.tiew.operationWild.screen.entity.OWInventoryMenu;
import net.tiew.operationWild.screen.entity.submarine.SeaBugInventoryMenu;
import net.tiew.operationWild.sound.OWSounds;
import net.tiew.operationWild.core.OWUtils;

import java.util.*;

import static net.tiew.operationWild.core.OWUtils.RANDOM;
import static net.tiew.operationWild.core.OWUtils.generateRandomInterval;

public class OWEntity extends TamableAnimal implements MenuProvider, IOWEntity, IOWTamable, IOWRideable, IOWWaypointEntity {

    private static final org.slf4j.Logger OW_LOGGER = com.mojang.logging.LogUtils.getLogger();

    public float averageScale;
    public static final Random RANDOM = new Random();
    public LivingEntity TRAPPED_ENTITY = null;
    public int intervallNapTimer = 0;
    public int napTimer = 0;
    private float lastPlay;
    public DamageSource damageSource = this.damageSources().mobAttack(this);
    public int numberFeedsGiven = 0;
    // 3 slots : 0 = selle, 1 = nourriture, 2 = gants de boxe (kangourou uniquement).
    public static final int FOOD_SLOT = 1;
    private final ItemStackHandler itemStackHandler = new ItemStackHandler(3) {
        @Override
        public void deserializeNBT(net.minecraft.core.HolderLookup.Provider provider, net.minecraft.nbt.CompoundTag nbt) {
            // Anciennes sauvegardes (Size=2) : on force 3 slots AVANT le chargement des items
            // (setSize vide tout), pour garantir le slot gants sans perdre selle/nourriture.
            if (nbt.getInt("Size") < 3) {
                nbt = nbt.copy();
                nbt.putInt("Size", 3);
            }
            super.deserializeNBT(provider, nbt);
        }
    };
    public final ItemStackHandler itemStackHandlerSeaBug = new ItemStackHandler(15);
    public int attackTimer;
    public int comboTimer;
    private int runTime;
    private double prevTickX, prevTickZ;
    public int chance = random.nextInt(100);
    /** Maintien à plein après un coup (5 s), puis fonte de la jauge (5 s) : 10 s en tout. */
    public static final int FIGHT_HOLD_TICKS = 100;
    public static final int FIGHT_DECAY_TICKS = 100;
    public static final int FIGHT_COOLDOWN_TICKS = FIGHT_HOLD_TICKS + FIGHT_DECAY_TICKS;

    /** Intervalle entre deux bouchées, une fois le combat quitté. */
    public static final int FEED_INTERVAL_TICKS = 60;

    /**
     * Multiplicateur de soin du ravitaillement <b>automatique</b>, bien plus sobre que celui de la
     * main du joueur (1,5) : nourrir soi-même est un geste, se servir tout seul toutes les trois
     * secondes n'en est pas un. Un aliment de prédilection rend ainsi 1,6 PV là où la main en rend
     * six — de quoi tenir sur la durée sans jamais remplacer les soins du maître.
     */
    public static final float AUTO_FEED_HEAL_MULTIPLIER = 0.4f;

    /**
     * Régénération passive au repos : de quoi ne pas rester éternellement blessé faute de vivres,
     * sans jamais se substituer au ravitaillement. Une demi-vie toutes les vingt secondes, contre un
     * point toutes les dix auparavant — quatre fois moins de récupération à l'heure.
     */
    public static final int PASSIVE_REGEN_INTERVAL_TICKS = 400;
    public static final float PASSIVE_REGEN_AMOUNT = 0.5f;

    private int fightingTime = 200;
    private int feedCooldown = 0;
    public boolean canAttack = true;
    private BlockPos lastPosition;
    // Quête « parcourir X blocs » : accumulation de la distance HORIZONTALE réelle (ignore Y, sinon un
    // animal qui rebondit sur place — le kangourou par ex. — gonflerait la progression).
    private double lastTravelX = Double.NaN;
    private double lastTravelZ = Double.NaN;
    private double travelAccumulator = 0.0;
    public LivingEntity lastVisibleTarget = null;
    public int questsReUpdatingTimer = 10;
    private int sittingCooldown = 0;
    private float customWidth = 1.0F;
    private float customHeight = 1.0F;
    public boolean canShowVitalEnergyLack = false;
    public boolean isChargingAttack = false;
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
    /** PV réellement rendus par le dernier {@link #heal(float)} (borné par la vie max). */
    private int lastRealHealDelta = 0;
    private int hurtAmount = 0;

    // Entites reellement TOUCHEES par le dernier appel a attackEntitiesInFront (cote serveur).
    // Permet aux effets post-impact (ex : uppercut du combo 3 du kangourou) de ne s'appliquer
    // qu'aux ennemis effectivement frappes, et pas a tout ce qui passe dans une zone elargie.
    public final List<LivingEntity> lastAttackHitEntities = new ArrayList<>();
    private int sleepBarDownSpeed;
    public int maxSleepBar;
    public float maxHealthBeforeResurrection;
    public int resurrectionTimer = 0;
    /** Quand true, le taming n'ouvre pas l'écran de nommage (ex : résurrection, le nom est déjà restauré). */
    public boolean skipNameSelection = false;
    public float actualMaturation = 0;
    public float maxHealth;
    public float maxMaturation;
    public static final int DELAY_BEFORE_BABY_TASK_MAX = 18000;
    public int delayBeforeBabyTask = 0;
    public boolean babyQuestIsInProgress = false;
    public int babyQuestProgressTimer = 0;
    public Item choosenFood = null;
    public int ultimateCooldown = 0;
    private float targetYRot = 0;

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

    public OWTeam currentTeam = null;

    public static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> SITTING = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> SADDLED = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_RUNNING = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_ATTACKING = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_IN_FIGHT = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Float> XP = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Integer> STAGE = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> LEVEL = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Float> ACCELERATION = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Integer> LEVEL_POINTS = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> VITAL_ENERGY = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> PREPARE_NAP = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> NAPPING = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_COMBO = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> BODY_Z_ROT = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BODY_X_ROT = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BODY_Y_ROT = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.FLOAT);
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
    private static final EntityDataAccessor<Boolean> SHOW_TRIBE_FLAG = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.BOOLEAN);
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
    private static final EntityDataAccessor<String> NAME = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> CACHED_OWNER_NAME = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.STRING);

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

    /**
     * Jour (epoch day) de la période pour laquelle la progression des quêtes de CETTE entité a été
     * reset. Comparé à {@link OWDailyQuests#computePeriodDay()} sur le tick serveur : dès qu'ils
     * diffèrent, cette entité re-tire ses quêtes et remet sa progression à zéro. Ce mécanisme
     * paresseux gère aussi les entités dans des chunks non chargés au moment du reset (elles se
     * remettent à jour à leur prochain chargement).
     */
    private long lastQuestResetDay = Long.MIN_VALUE;

    /** Ids des 3 quêtes actives PROPRES à cette entité (-1 = non tiré). Persistés en NBT, synchronisés au cavalier. */
    public int activeQuest0 = -1;
    public int activeQuest1 = -1;
    public int activeQuest2 = -1;

    /**
     * Récompense pré-tirée de chaque quête active (code : &gt;0 = orbes d'XP, &lt;0 = -pièces, 0 = non tiré).
     * Tirée au moment du reroll (déterministe et affichable), versée à la complétion de la quête.
     */
    public int questReward0 = 0;
    public int questReward1 = 0;
    public int questReward2 = 0;

    /** Reroll manuel d'une quête : disponible une seule fois par jour (remis à true à chaque reset). NBT + synchronisé. */
    public boolean dailyRerollAvailable = true;

    /** Valeur en XP d'un « orbe » de récompense (moyenne d'un orbe absorbé). */
    private static final float ORB_XP_VALUE = 4.0f;

    /** Tire 3 nouvelles quêtes distinctes pour cette entité, et pré-tire leur récompense (XP ou pièces). */
    public void rerollDailyQuests() {
        int[] ids = net.tiew.operationWild.entity.quests.daily_quests.OWDailyQuests.pickRandomQuestIds(this::questAllowed);
        this.activeQuest0 = ids[0];
        this.activeQuest1 = ids[1];
        this.activeQuest2 = ids[2];
        this.questReward0 = rollQuestReward(ids[0]);
        this.questReward1 = rollQuestReward(ids[1]);
        this.questReward2 = rollQuestReward(ids[2]);
    }

    /**
     * Reroll manuel d'UNE quête (emplacement 0..2) : tire une nouvelle quête distincte des deux autres
     * actives, pré-tire sa récompense, remet sa progression à zéro, et consomme le reroll du jour.
     */
    public void rerollSingleQuest(int slot) {
        if (!this.dailyRerollAvailable || slot < 0 || slot > 2) return;
        int newId = pickQuestIdExcluding(this.activeQuest0, this.activeQuest1, this.activeQuest2);
        if (newId < 0) return;
        switch (slot) {
            case 0 -> { this.activeQuest0 = newId; this.questReward0 = rollQuestReward(newId); }
            case 1 -> { this.activeQuest1 = newId; this.questReward1 = rollQuestReward(newId); }
            case 2 -> { this.activeQuest2 = newId; this.questReward2 = rollQuestReward(newId); }
        }
        resetQuestById(newId);
        this.dailyRerollAvailable = false;
    }

    /** Tire un id de quête au hasard, distinct de {@code a}, {@code b} et {@code c} et autorisé ; -1 si impossible. */
    private int pickQuestIdExcluding(int a, int b, int c) {
        int n = net.tiew.operationWild.entity.quests.daily_quests.DailyQuestRegistry.ALL.length;
        java.util.List<Integer> pool = new java.util.ArrayList<>();
        for (int id = 0; id < n; id++) {
            if (id != a && id != b && id != c && questAllowed(id)) pool.add(id);
        }
        if (pool.isEmpty()) return -1;
        return pool.get(this.getRandom().nextInt(pool.size()));
    }

    /** Une quête est-elle attribuable à cette entité ? (ex : « passer un niveau » interdit au niveau max). */
    private boolean questAllowed(int id) {
        net.tiew.operationWild.entity.quests.daily_quests.DailyQuest q =
                net.tiew.operationWild.entity.quests.daily_quests.DailyQuestRegistry.getById(id);
        if (q == null) return false;
        if (q.requiresLevelUp() && this.getLevel() >= 50) return false;
        return true;
    }

    /** Remet à zéro la progression et le verrou d'une seule quête (par id). */
    private void resetQuestById(int id) {
        switch (id) {
            case 0 -> { quest0Progression = 0; quest0isLocked = false; }
            case 1 -> { quest1Progression = 0; quest1isLocked = false; }
            case 2 -> { quest2Progression = 0; quest2isLocked = false; }
            case 3 -> { quest3Progression = 0; quest3isLocked = false; }
            case 4 -> { quest4Progression = 0; quest4isLocked = false; }
            case 5 -> { quest5Progression = 0; quest5isLocked = false; }
            case 6 -> { quest6Progression = 0; quest6isLocked = false; }
            case 7 -> { quest7Progression = 0; quest7isLocked = false; }
            case 8 -> { quest8Progression = 0; quest8isLocked = false; }
            case 9 -> { quest9Progression = 0; quest9isLocked = false; }
            case 10 -> { quest10Progression = 0; quest10isLocked = false; }
        }
    }

    /** Tire la récompense d'une quête selon son palier (code : &gt;0 orbes, &lt;0 -pièces). */
    private int rollQuestReward(int questId) {
        net.tiew.operationWild.entity.quests.daily_quests.DailyQuest q =
                net.tiew.operationWild.entity.quests.daily_quests.DailyQuestRegistry.getById(questId);
        if (q == null || q.getTier() == null) return 0;
        return q.getTier().rollReward(this.getRandom());
    }

    /** Verse la récompense pré-tirée de la quête d'id {@code questId} (XP au pet, ou pièces au propriétaire). */
    private void grantQuestReward(int questId) {
        int code = 0;
        if (activeQuest0 == questId) code = questReward0;
        else if (activeQuest1 == questId) code = questReward1;
        else if (activeQuest2 == questId) code = questReward2;

        if (code > 0) {
            ServerPlayer rider = (this.getControllingPassenger() instanceof ServerPlayer sp) ? sp : null;
            ServerPlayer owner = (this.getOwner() instanceof ServerPlayer op) ? op : null;

            if (this.getLevel() >= 50) {
                // Entité au niveau max : la récompense d'XP devient de l'Expérience d'Apprivoisement
                // (cagnotte du propriétaire), divisée par deux (ex : 18 XP → 9 exp d'apprivoisement).
                int tamingGain = Math.max(1, Math.round(code / 2f));
                Player rewardPlayer = (owner != null) ? owner : rider;
                if (rewardPlayer != null) {
                    this.addTamingExperience(tamingGain, rewardPlayer);
                    ServerPlayer animTarget = (owner != null) ? owner : rider;
                    if (animTarget != null) {
                        OWNetworkHandler.sendToClient(
                                new net.tiew.operationWild.networking.packets.to_client.OWXpGainPacket(tamingGain, true),
                                animTarget);
                    }
                }
            } else {
                gainLevelXp(code * ORB_XP_VALUE);
                ServerPlayer target = (rider != null) ? rider : owner;
                if (target != null) {
                    OWNetworkHandler.sendToClient(
                            new net.tiew.operationWild.networking.packets.to_client.OWXpGainPacket(code, false),
                            target);
                }
            }
        } else if (code < 0) {
            if (this.getOwner() instanceof ServerPlayer owner) {
                net.tiew.operationWild.core.OWCurrency.grantWildCoins(owner, -code);
            }
        }
    }

    /** Remet à zéro la progression et le verrouillage des 11 quêtes (nouvelle journée). */
    public void resetDailyQuestProgress() {
        quest0Progression = 0;
        quest1Progression = 0;
        quest2Progression = 0;
        quest3Progression = 0;
        quest4Progression = 0;
        quest5Progression = 0;
        quest6Progression = 0;
        quest7Progression = 0;
        quest8Progression = 0;
        quest9Progression = 0;
        quest10Progression = 0;

        quest0isLocked = false;
        quest1isLocked = false;
        quest2isLocked = false;
        quest3isLocked = false;
        quest4isLocked = false;
        quest5isLocked = false;
        quest6isLocked = false;
        quest7isLocked = false;
        quest8isLocked = false;
        quest9isLocked = false;
        quest10isLocked = false;
    }

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
        this.goalSelector.addGoal(2, new OWFollowOwnerGoal(this, this.getSpeed() * (this instanceof OWWaterEntity ? 5 : 20), 15, 3));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25));
        this.goalSelector.addGoal(6, new OWLookAtPlayerGoal(this, Player.class, 6.0F));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));

        this.lookControl = new LookControl(this) {
            @Override
            public void tick() {
                if (this.mob.getTarget() == null) {
                    super.tick();
                }
            }
        };
    }

    public static final List<Class<?>> TANK_ENTITIES = new ArrayList<>();
    public static final List<Class<?>> ASSASSIN_ENTITIES = new ArrayList<>();
    public static final List<Class<?>> MARAUDER_ENTITIES = new ArrayList<>();

    public static final List<Object> CARNIVOROUS_ENTITIES = List.of(
            OWEntityRegistry.KODIAK.get(),
            OWEntityRegistry.CROCODILE.get()
    );

    public static final List<Object> VEGETARIAN_ENTITIES = List.of(

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
            // Compte les PV RÉELLEMENT régénérés : à pleine vie le delta est 0 → pas de progression.
            this.quest2Progression += lastRealHealDelta;
            if (quest2Progression >= 100) this.finishQuest((byte) 2);
        }
        if (id == 3 && !this.quest3isLocked) {
            double x = this.getX(), z = this.getZ();
            if (!Double.isNaN(lastTravelX)) {
                double dx = x - lastTravelX, dz = z - lastTravelZ;
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d < 20.0) travelAccumulator += d;   // ignore les téléportations
                while (travelAccumulator >= 1.0 && quest3Progression < 2000) {
                    quest3Progression++;
                    travelAccumulator -= 1.0;
                }
            }
            lastTravelX = x;
            lastTravelZ = z;
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

        // Récompense selon le palier de la quête (pré-tirée au reroll) : soit des orbes d'XP versées
        // dans le pipeline de leveling, soit des Pièces Sauvages pour le propriétaire.
        grantQuestReward(id);

        // La petite fanfare de quête s'adresse au maître <b>en selle</b>, à lui seul. Diffusée dans
        // le monde, elle partait à la cantonade — n'importe quel passant à portée l'entendait, le
        // propriétaire resté trop loin la manquait, et une créature qui bouclait une quête dans son
        // coin sonnait pour personne. Envoyée directement au cavalier, elle arrive à coup sûr, et
        // seulement quand il est aux commandes.
        if (this.getControllingPassenger() instanceof ServerPlayer rider
                && rider.getUUID().equals(this.getOwnerUUID())) {
            rider.playNotifySound(OWSounds.TAME_SUCCESS.get(), SoundSource.PLAYERS, 1.0f, (float) pitch);
        }
    }

    public boolean isQuestInProgress(DailyQuest quest) {
        if (quest == null) return false;
        int id = quest.getId();
        return this.activeQuest0 == id || this.activeQuest1 == id || this.activeQuest2 == id;
    }

    public int getState() { return this.entityData.get(STATE);}

    public void setState(int state) { this.entityData.set(STATE, state);}

    public boolean isFalling() { return this.entityData.get(IS_FALLING);}

    public void setFalling(boolean isFalling) { this.entityData.set(IS_FALLING, isFalling);}

    public boolean isPassive() { return this.entityData.get(IS_PASSIVE);}

    public void setPassive(boolean isPassive) { this.entityData.set(IS_PASSIVE, isPassive);}

    public boolean isAutoPickup() { return this.entityData.get(AUTO_PICKUP); }

    public void setAutoPickup(boolean value) { this.entityData.set(AUTO_PICKUP, value); }

    /**
     * Port de la banniere de tribu sur le dos de l'entite. Synchronise : le drapeau etant rendu
     * par tous les clients qui voient l'entite, le choix doit valoir pour tout le monde et pas
     * seulement pour son proprietaire.
     */
    public boolean isShowTribeFlag() { return this.entityData.get(SHOW_TRIBE_FLAG); }

    public void setShowTribeFlag(boolean value) { this.entityData.set(SHOW_TRIBE_FLAG, value); }

    /**
     * Cette créature arbore-t-elle l'étendard de sa tribu ?
     *
     * <p>Règle unique, partagée par tous les layers de drapeau (entités d'un seul tenant comme
     * segments de boa) : le port de l'étendard est un <b>honneur</b>, réservé aux champions
     * désignés par le chef, et non un signe d'appartenance porté par toute la tribu.</p>
     *
     * <p>Trois conditions cumulées : être champion, porter la selle (c'est elle qui tient la
     * hampe), et ne pas avoir été masquée par son propriétaire. S'y ajoutent les états où la
     * créature ne se montre pas du tout.</p>
     */
    public boolean carriesTribeFlag() {
        return this.currentTeam != null && this.currentTeam.isChampion(this.getUUID())
                && this.isSaddled() && this.isShowTribeFlag()
                && this.isTame() && this.isAlive()
                && !this.isInvisible() && !this.isInResurrection();
    }

    public float getAcceleration() { return this.entityData.get(ACCELERATION);}

    public void setAcceleration(float getAcceleration) { this.entityData.set(ACCELERATION, getAcceleration);}

    public int getNecklaceColor() { return this.entityData.get(NECKLACE_COLOR);}

    public void setNecklaceColor(int necklaceColor) { this.entityData.set(NECKLACE_COLOR, necklaceColor);}

    public static final EntityDataAccessor<Integer> SKIN_INDEX = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);
    // CSV des indices de skins débloqués (achetés avec des Pièces Sauvages). Appartient au pet, donc
    // stocké et synchronisé sur l'entité (serveur-autoritaire), sauvegardé en NBT et restauré à la résurrection.
    public static final EntityDataAccessor<String> SKINS_UNLOCKED = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Integer> COSMETIC_QUEST_KILLS = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> FIGHT_COOLDOWN = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);
    public boolean nbtRestoring = false;

    // --- Piste Sauvage (labyrinthe de progression par individu) ---
    // Avancement payé en Pièces Sauvages (OWCurrency, par joueur) ; ici on ne stocke que la progression.
    // Nœud courant du pion sur la Piste (0 = départ).
    public static final EntityDataAccessor<Integer> PISTE_NODE = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.INT);
    // CSV des ids de nœuds débloqués (contient toujours "0"), et des nœuds verrouillés à vie par un choix exclusif.
    public static final EntityDataAccessor<String> PISTE_UNLOCKED = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> PISTE_LOCKED = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.STRING);
    // Option choisie sur chaque palier à choix : CSV "nodeId:optionIndex".
    public static final EntityDataAccessor<String> PISTE_CHOICES = SynchedEntityData.defineId(OWEntity.class, EntityDataSerializers.STRING);
    /** DEBUG/TEST : à true, remet à zéro la progression de la Piste de chaque entité au chargement. Repasser à false une fois le reset fait. */
    public static final boolean DEBUG_RESET_PISTE = false;

    public int getSkinIndex() {
        return this.entityData.get(SKIN_INDEX);
    }

    protected int getDefaultSkinIndex() { return 0; }

    public void changeSkin(int skinIndex, boolean playingEffects) {
        this.entityData.set(SKIN_INDEX, skinIndex);
        if (playingEffects) playSkinChangeEffect();
    }

    public void changeSkinSilent(int skinIndex) {
        this.entityData.set(SKIN_INDEX, skinIndex);
    }

    /**
     * Pose le skin « Par Défaut » de l'espèce sur une créature qui n'en porte aucun.
     *
     * <p>L'indice 0 ne désigne pas une apparence, mais son absence : la bête s'affiche alors dans
     * son habit sauvage, alors que chaque espèce a un cosmétique de base fait pour elle. Le geste
     * n'avait lieu qu'à l'apprivoisement, si bien qu'une créature apprivoisée avant que son espèce
     * ne déclare son indice restait nue pour toujours. On le rejoue donc tant que le compte n'y est
     * pas — une seule fois en pratique, puisque le skin s'installe aussitôt.</p>
     */
    private void equipDefaultSkinIfNeeded() {
        if (!this.isTame() || this.getSkinIndex() != 0) return;
        int defaultSkin = this.getDefaultSkinIndex();
        if (defaultSkin > 0) this.changeSkinSilent(defaultSkin);
    }

    // --- Skins débloqués (achetés) : appartiennent au pet, serveur-autoritaire + synchronisés ---

    /** CSV brut des indices de skins débloqués (pour la sérialisation Âme / NBT). */
    public String getUnlockedSkinsRaw() { return this.entityData.get(SKINS_UNLOCKED); }

    /** Restaure l'ensemble des skins débloqués depuis un CSV (NBT / résurrection). */
    public void setUnlockedSkinsRaw(String csv) { this.entityData.set(SKINS_UNLOCKED, csv == null ? "" : csv); }

    public java.util.Set<Integer> getUnlockedSkins() { return parsePisteIds(this.entityData.get(SKINS_UNLOCKED)); }

    /** Le skin d'index 0 (défaut) est toujours débloqué. */
    public boolean isSkinUnlocked(int skinIndex) {
        return skinIndex == 0 || getUnlockedSkins().contains(skinIndex);
    }

    /**
     * Proies abattues par cette créature, décomptées pour les quêtes de skins.
     *
     * <p>Le compte vit <b>sur la créature</b> et voyage jusqu'au client, comme les skins débloqués.
     * Il tenait auparavant dans une table statique alimentée par le seul serveur : en solo les deux
     * moitiés du jeu partagent la même mémoire et l'illusion tenait, mais en partie multijoueur le
     * client n'y lisait jamais rien — la barre d'avancement restait plantée à 0 % et le skin ne se
     * débloquait pas.</p>
     */
    public int getCosmeticQuestKills() { return this.entityData.get(COSMETIC_QUEST_KILLS); }

    public void setCosmeticQuestKills(int kills) {
        this.entityData.set(COSMETIC_QUEST_KILLS, Math.max(0, kills));
    }

    /** Ajoute une proie au compte. À n'appeler que côté serveur. */
    public void addCosmeticQuestKill() {
        setCosmeticQuestKills(getCosmeticQuestKills() + 1);
    }

    /** Débloque un skin (idempotent). À n'appeler que côté serveur. */
    public void unlockSkin(int skinIndex) {
        if (skinIndex == 0) return;
        java.util.Set<Integer> ids = getUnlockedSkins();
        if (ids.add(skinIndex)) this.entityData.set(SKINS_UNLOCKED, joinPisteIds(ids));
    }

    protected void playSkinChangeEffect() {
        if (nbtRestoring) return;
        double cx = this.getX();
        double cy = this.getY();
        double cz = this.getZ();
        double halfHeight = this.getBbHeight() / 2.0;
        double ringRadius = this.getBbWidth() * 1.4;

        if (this.level() instanceof ServerLevel serverWorld) {
            serverWorld.sendParticles(ParticleTypes.FLASH, cx, cy + halfHeight, cz, 1, 0, 0, 0, 0);
            serverWorld.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, cx, cy + halfHeight, cz, 200, 0.4, 0.6, 0.4, 0.35);
            for (int i = 0; i < 80; i++) {
                double angle = (2 * Math.PI * i) / 16;
                serverWorld.sendParticles(ParticleTypes.END_ROD, cx + ringRadius * Math.cos(angle), cy + 0.1, cz + ringRadius * Math.sin(angle), 4, 0.05, 0.3, 0.05, 0.02);
            }
            for (int i = 0; i < 120; i++) {
                double t = (double) i / 24;
                double angle = t * 4 * Math.PI;
                double r = ringRadius * (1.0 - t * 0.5);
                serverWorld.sendParticles(ParticleTypes.ENCHANT, cx + r * Math.cos(angle), cy + t * this.getBbHeight() * 1.5, cz + r * Math.sin(angle), 2, 0.05, 0.05, 0.05, 0.01);
            }
            float pitch = (float) OWUtils.generateRandomInterval(0.9, 1.1);
            this.level().playSound(null, cx, cy, cz, SoundEvents.SLIME_JUMP, SoundSource.NEUTRAL, 0.6f, pitch);
        } else {
            var rand = this.getRandom();
            this.level().addParticle(ParticleTypes.FLASH, cx, cy + halfHeight, cz, 0, 0, 0);
            for (int i = 0; i < 200; i++) {
                this.level().addParticle(ParticleTypes.TOTEM_OF_UNDYING, cx, cy + halfHeight, cz,
                        (rand.nextDouble() - 0.5) * 0.7, (rand.nextDouble() - 0.5) * 1.2, (rand.nextDouble() - 0.5) * 0.7);
            }
            for (int i = 0; i < 80; i++) {
                double angle = (2 * Math.PI * i) / 16;
                double rx = cx + ringRadius * Math.cos(angle);
                double rz = cz + ringRadius * Math.sin(angle);
                for (int j = 0; j < 4; j++) {
                    this.level().addParticle(ParticleTypes.END_ROD, rx, cy + 0.1, rz,
                            (rand.nextDouble() - 0.5) * 0.1, rand.nextDouble() * 0.3, (rand.nextDouble() - 0.5) * 0.1);
                }
            }
            for (int i = 0; i < 120; i++) {
                double t = (double) i / 24;
                double angle = t * 4 * Math.PI;
                double r = ringRadius * (1.0 - t * 0.5);
                for (int j = 0; j < 2; j++) {
                    this.level().addParticle(ParticleTypes.ENCHANT,
                            cx + r * Math.cos(angle), cy + t * this.getBbHeight() * 1.5, cz + r * Math.sin(angle),
                            (rand.nextDouble() - 0.5) * 0.1, (rand.nextDouble() - 0.5) * 0.1, (rand.nextDouble() - 0.5) * 0.1);
                }
            }
            float pitch = (float) OWUtils.generateRandomInterval(0.9, 1.1);
            this.level().playLocalSound(cx, cy, cz, SoundEvents.SLIME_JUMP, SoundSource.NEUTRAL, 0.6f, pitch, false);
        }
    }

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

    public int getLevel() { return this.entityData.get(LEVEL);}

    public void setLevel(int level) { this.entityData.set(LEVEL, level);}

    public int getLevelPoints() { return this.entityData.get(LEVEL_POINTS);}

    public void setLevelPoints(int level) { this.entityData.set(LEVEL_POINTS, level);}

    // --- Piste Sauvage : progression (l'avancement se paie en Pièces Sauvages, voir OWCurrency) ---
    public int getPisteCurrentNode() { return this.entityData.get(PISTE_NODE); }

    public void setPisteCurrentNode(int nodeId) { this.entityData.set(PISTE_NODE, nodeId); }

    public java.util.Set<Integer> getPisteUnlockedNodes() { return parsePisteIds(this.entityData.get(PISTE_UNLOCKED)); }

    public boolean isPisteNodeUnlocked(int nodeId) { return getPisteUnlockedNodes().contains(nodeId); }

    public void addPisteUnlockedNode(int nodeId) {
        java.util.Set<Integer> ids = getPisteUnlockedNodes();
        if (ids.add(nodeId)) this.entityData.set(PISTE_UNLOCKED, joinPisteIds(ids));
    }

    public java.util.Set<Integer> getPisteLockedNodes() { return parsePisteIds(this.entityData.get(PISTE_LOCKED)); }

    public boolean isPisteNodeLocked(int nodeId) { return getPisteLockedNodes().contains(nodeId); }

    public void addPisteLockedNode(int nodeId) {
        java.util.Set<Integer> ids = getPisteLockedNodes();
        if (ids.add(nodeId)) this.entityData.set(PISTE_LOCKED, joinPisteIds(ids));
    }

    /** CSV brut des choix de paliers (debug). */
    public String getPisteChoicesRaw() { return this.entityData.get(PISTE_CHOICES); }

    /** Option choisie sur un palier à choix, ou -1 si aucun choix fait. */
    public int getPisteChoice(int nodeId) {
        String csv = this.entityData.get(PISTE_CHOICES);
        if (csv == null || csv.isEmpty()) return -1;
        for (String part : csv.split(",")) {
            int sep = part.indexOf(':');
            if (sep <= 0) continue;
            try {
                if (Integer.parseInt(part.substring(0, sep).trim()) == nodeId) {
                    return Integer.parseInt(part.substring(sep + 1).trim());
                }
            } catch (NumberFormatException ignored) {}
        }
        return -1;
    }

    public void setPisteChoice(int nodeId, int optionIndex) {
        if (getPisteChoice(nodeId) != -1) return; // définitif : on ne réécrit jamais
        String csv = this.entityData.get(PISTE_CHOICES);
        String entry = nodeId + ":" + optionIndex;
        this.entityData.set(PISTE_CHOICES, (csv == null || csv.isEmpty()) ? entry : csv + "," + entry);
    }

    private static java.util.Set<Integer> parsePisteIds(String csv) {
        java.util.Set<Integer> ids = new java.util.LinkedHashSet<>();
        if (csv == null || csv.isEmpty()) return ids;
        for (String part : csv.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                try { ids.add(Integer.parseInt(trimmed)); } catch (NumberFormatException ignored) {}
            }
        }
        return ids;
    }

    private static String joinPisteIds(java.util.Set<Integer> ids) {
        StringBuilder sb = new StringBuilder();
        for (int id : ids) {
            if (sb.length() > 0) sb.append(',');
            sb.append(id);
        }
        return sb.toString();
    }

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
        if (isInFight) this.fightingTime = FIGHT_COOLDOWN_TICKS;   // chaque déclenchement rafraîchit le timer
        else this.fightingTime = 0;
        if (!this.level().isClientSide()) this.entityData.set(FIGHT_COOLDOWN, this.fightingTime);
        this.entityData.set(IS_IN_FIGHT, isInFight);
    }

    /**
     * Décompte avant la sortie de combat, en ticks, répliqué jusqu'au client.
     *
     * <p>Deux temps de cinq secondes : le premier fige le compteur à plein — un coup vient d'être
     * porté, rien ne redescend encore —, le second le fait fondre jusqu'à zéro. Toucher ou être
     * touché relance le tout depuis le début. Tant qu'il n'est pas épuisé, la créature ne mange
     * pas : on ne se ravitaille pas au milieu d'un échange.</p>
     */
    public int getFightCooldown() { return this.entityData.get(FIGHT_COOLDOWN); }

    /** Part de la jauge à afficher [0..1] : pleine pendant le maintien, décroissante ensuite. */
    public float getFightCooldownFraction() {
        int t = getFightCooldown();
        if (t <= 0) return 0f;
        if (t >= FIGHT_DECAY_TICKS) return 1f;
        return t / (float) FIGHT_DECAY_TICKS;
    }

    public float getDamageToClient() { return this.entityData.get(DAMAGE_TO_CLIENT);}

    public void setDamageToClient(float damage) { this.entityData.set(DAMAGE_TO_CLIENT, damage);}

    public boolean questsAreUpdated() { return this.entityData.get(QUESTS_ARE_UPDATED);}

    public void setUpdatingQuests(boolean questsAreUpdated) { this.entityData.set(QUESTS_ARE_UPDATED, questsAreUpdated);}

    /**
     * Le maître de cette créature a-t-il déjà consulté les quêtes de la période {@code period} ?
     *
     * <p>Maître hors ligne : on répond non, et la pastille se lève. Il la verra en revenant, et elle
     * s'effacera partout dès qu'il aura ouvert l'onglet une fois — mieux vaut une pastille de trop
     * qu'une journée entière passée sous silence.</p>
     */
    private boolean ownerHasSeenQuestPeriod(long period) {
        net.minecraft.server.MinecraftServer server = this.getServer();
        UUID owner = this.getOwnerUUID();
        if (server == null || owner == null) return false;
        ServerPlayer master = server.getPlayerList().getPlayer(owner);
        return master != null && OWDailyQuests.hasSeenPeriod(master, period);
    }

    public ItemStackHandler getInventory() {
        return this.itemStackHandler;
    }

    public void passingLevel(boolean playSound) {
        setXp(Math.max(0, this.getXp() - this.getXpStage()));
        this.setLevel(this.getLevel() + 1);
        this.setLevelPoints(this.getLevelPoints() + 1);

        if (playSound) {
            this.playSound(SoundEvents.PLAYER_LEVELUP);
        }

        // Tous les 10 niveaux atteints par n'importe quel pet, l'owner gagne 1 Pièce Sauvage.
        if (!this.level().isClientSide() && this.getLevel() % 10 == 0
                && this.getOwner() instanceof ServerPlayer owner) {
            net.tiew.operationWild.core.OWCurrency.grantWildCoins(owner, 1);
        }

        notifyLevelUpClient();

        int xpStage = OWUtils.xpToNextLevel(this.getLevel());
        setXpStage(xpStage);
        if (isQuestInProgress(DailyQuestRegistry.quest6) && !this.level().isClientSide()) {
            this.executeQuestProgression((byte) 5);
        }
    }

    /**
     * Envoie au client concerné le déclencheur de l'animation de passage de niveau
     * (anneaux concentriques + chiffre). La cible est le rider s'il y en a un, sinon le propriétaire
     * en ligne et proche. Ne fait rien côté client.
     */
    private void notifyLevelUpClient() {
        if (this.level().isClientSide()) return;

        ServerPlayer target = null;
        LivingEntity rider = this.getControllingPassenger();
        if (rider instanceof ServerPlayer sp) {
            target = sp;
        } else if (this.getOwner() instanceof ServerPlayer owner
                && owner.level() == this.level() && owner.distanceToSqr(this) <= 48 * 48) {
            // Pas chevauché : on prévient le propriétaire seulement s'il est proche et dans le même monde,
            // pour éviter une animation surprise venue d'un pet à l'autre bout de la carte.
            target = owner;
        }

        if (target != null) {
            OWNetworkHandler.sendToClient(
                    new net.tiew.operationWild.networking.packets.to_client.OWLevelUpPacket(
                            this.getLevel(), this.getEntityColor()),
                    target);
        }
    }

    /**
     * Ajoute de l'XP de niveau (1 → 50). Unique point d'entrée du leveling : l'XP provient
     * exclusivement de l'absorption de boules d'expérience (voir {@link #absorbNearbyXpOrbs()})
     * et des récompenses de quêtes.
     */
    public void gainLevelXp(float amount) {
        if (!isTame() || this.getLevel() >= 50 || amount <= 0) return;

        setXpStage(OWUtils.xpToNextLevel(this.getLevel()));
        boolean hasLeveledUp = false;

        while (amount > 0 && this.getLevel() < 50) {
            float rest = getXpStage() - this.getXp();
            if (amount >= rest) {
                amount -= rest;
                passingLevel(!hasLeveledUp);
                hasLeveledUp = true;
                if (this.getLevel() >= 50) {
                    this.setXp(0);
                    break;
                }
            } else {
                this.setXp(this.getXp() + amount);
                amount = 0;
            }
        }
    }

    /** Facteur d'échelle des récompenses d'XP de quête pour rester cohérent avec la nouvelle courbe. */
    private static final float QUEST_XP_SCALE = 6.0f;

    private static final double XP_ORB_ATTRACT_RADIUS = 8.0;
    private static final double XP_ORB_PICKUP_RADIUS = 1.25;

    /**
     * Comme un joueur, une entité apprivoisée non encore au niveau max attire et absorbe les
     * boules d'expérience proches. Une fois le niveau 50 atteint, elle n'aspire plus rien : les
     * orbes retournent naturellement au joueur (rider compris). Appelé côté serveur uniquement.
     */
    private void absorbNearbyXpOrbs() {
        java.util.List<ExperienceOrb> orbs = this.level().getEntitiesOfClass(
                ExperienceOrb.class, this.getBoundingBox().inflate(XP_ORB_ATTRACT_RADIUS));
        if (orbs.isEmpty()) return;

        Vec3 center = new Vec3(this.getX(), this.getY() + this.getBbHeight() * 0.5, this.getZ());
        for (ExperienceOrb orb : orbs) {
            if (!orb.isAlive()) continue;
            Vec3 toEntity = center.subtract(orb.position());
            double dist = toEntity.length();

            if (dist <= XP_ORB_PICKUP_RADIUS) {
                int value = orb.getValue();
                if (value > 0) {
                    gainLevelXp(value);
                    this.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.15f,
                            ((this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.35f + 1.0f) * 2.0f);
                }
                orb.discard();
                if (this.getLevel() >= 50) return; // niveau max atteint pendant l'absorption
            } else if (dist > 0.01) {
                double pull = (1.0 - dist / XP_ORB_ATTRACT_RADIUS) * 0.12;
                orb.setDeltaMovement(orb.getDeltaMovement().add(toEntity.normalize().scale(pull)));
            }
        }
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

    /**
     * Suit le gain de PV réellement appliqué (borné par la vie max) dans {@link #lastRealHealDelta}.
     * La quête « régénérer des PV » (id 2) s'appuie dessus : à pleine vie, {@code super.heal} ne rend
     * rien → delta 0 → la quête ne progresse pas (correction du comptage à vide).
     */
    @Override
    public void heal(float amount) {
        float before = this.getHealth();
        super.heal(amount);
        this.lastRealHealDelta = Math.max(0, Math.round(this.getHealth() - before));
    }

    /**
     * Vrai si ce régime accepte cet aliment. Un carnivore ne touche pas aux légumes, un herbivore
     * pas à la viande : rien ne sert de laisser une créature grignoter ce qui ne la nourrira pas.
     */
    public boolean canEatFood(ItemStack stack) {
        return foodHealAmount(stack, preferRawMeat(), preferCookedMeat()) > 0f;
    }

    /**
     * Se ravitaille sur la nourriture rangée dans son inventaire, hors combat uniquement.
     *
     * <p>Une bouchée toutes les trois secondes, et seulement si la créature est blessée : elle ne
     * gaspille pas les réserves de son maître à pleine vie. Le compteur de bouchées ne court pas
     * pendant le combat — c'est le fait d'en sortir qui ouvre le repas, pas le simple fait
     * d'attendre. Ce que rend chaque aliment dépend du régime et de la cuisson, comme lorsqu'on la
     * nourrit à la main (cf. {@link #healWithFavoriteFood}).</p>
     */
    private void tickFeeding() {
        if (this.isInFight() || this.getFightCooldown() > 0) {
            feedCooldown = FEED_INTERVAL_TICKS;   // sortie de combat = plein délai avant la 1re bouchée
            return;
        }
        if (this.getHealth() >= this.getMaxHealth() || this.isDeadOrDying()) return;
        if (feedCooldown > 0) { feedCooldown--; return; }

        ItemStackHandler inventory = this.getInventory();
        if (inventory == null) return;
        ItemStack food = inventory.getStackInSlot(FOOD_SLOT);
        if (!canEatFood(food)) return;

        this.setItemFood(food.copy());
        healWithFavoriteFood(AUTO_FEED_HEAL_MULTIPLIER, preferRawMeat(), preferCookedMeat());
        this.playSound(SoundEvents.CAMEL_EAT, 1.0f, 1.0f);
        food.shrink(1);
        this.setFoodCount(food.getCount());
        if (food.isEmpty()) {
            this.setFed(false);
            this.setItemFood(ItemStack.EMPTY);
        }
        feedCooldown = FEED_INTERVAL_TICKS;
    }

    /**
     * PV rendus par un aliment, <b>avant</b> multiplicateur. {@code 0} si le régime le refuse.
     *
     * <p>Une seule décision, un seul chiffre. Le calcul tenait auparavant dans deux blocs
     * indépendants — l'un pour la viande, l'autre pour le végétal — chacun avec ses branches et son
     * propre appel à {@code heal} : un aliment portant les deux étiquettes se voyait soigné deux
     * fois, et il était impossible de dire d'un coup d'œil combien un repas rendait.</p>
     */
    public float foodHealAmount(ItemStack food, boolean preferRawMeat, boolean preferCookedMeat) {
        if (food == null || food.isEmpty()) return 0f;

        boolean raw = food.is(Tags.Items.FOODS_RAW_MEAT) || food.is(Tags.Items.FOODS_RAW_FISH);
        boolean cooked = food.is(Tags.Items.FOODS_COOKED_MEAT) || food.is(Tags.Items.FOODS_COOKED_FISH);
        boolean meat = raw || cooked || food.is(ItemTags.MEAT) || food.is(ItemTags.FISHES);
        boolean plant = food.is(Tags.Items.FOODS_VEGETABLE) || food.is(Tags.Items.FOODS_FRUIT);

        // La viande prime pour qui la mange : un omnivore devant un aliment doublement étiqueté
        // n'en tire qu'un seul repas.
        if (meat && (this.isCarnivorous() || this.isOmnivorous())) {
            if (preferRawMeat) return raw ? 4f : 2f;
            if (preferCookedMeat) return cooked ? 4f : 2f;
            return 3f;   // espèce sans penchant déclaré : la viande la nourrit tout de même
        }
        if (plant && (this.isVegetarian() || this.isOmnivorous())) return 3f;
        return 0f;
    }

    /**
     * Applique le soin d'un aliment selon le régime et la cuisson — <b>un seul</b> {@code heal},
     * quel que soit l'aliment.
     *
     * <p>Ne joue aucun son : la bouchée appartient à l'appelant. Les branches de ce calcul en
     * émettaient chacune un, parfois deux, et l'appelant en ajoutait un troisième — un seul repas
     * déclenchait donc une rafale de mastications.</p>
     */
    public void healWithFavoriteFood(float healMultiplier, boolean preferRawMeat, boolean preferCookedMeat) {
        float base = foodHealAmount(this.getItemFood(), preferRawMeat, preferCookedMeat);
        if (base <= 0f) return;

        this.heal(base * healMultiplier);
        healAmount = (int) (base * healMultiplier);
        if (isQuestInProgress(DailyQuestRegistry.quest3) && !this.level().isClientSide()) {
            this.executeQuestProgression((byte) 2);
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

    public boolean isPlayerControlledDeathRoll() { return false; }

    public boolean isGrabbing() { return false; }

    public String getNickname() { return this.entityData.get(NAME);}

    public void setNickname(String getNickname) { this.entityData.set(NAME, getNickname);}

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

    // ==================== Système d'Âme / Résurrection (générique, tout OWEntity) ====================

    /** Construit le snapshot complet et générique de ce compagnon, indépendant du type d'entité. */
    public SoulData buildSoulData() {
        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(this.getType());
        UUID ownerUuid = this.getOwnerUUID() != null ? this.getOwnerUUID() : SoulData.NO_UUID;
        String ownerName = this.getOwner() != null ? this.getOwner().getName().getString()
                : (this.getCachedOwnerName() != null ? this.getCachedOwnerName() : "");
        String nickname = this.getNickname() != null ? this.getNickname() : "";
        CompoundTag teamTag = new CompoundTag();
        if (this.currentTeam != null) this.writeTeamToTag(teamTag);
        return new SoulData(typeId, this.getUUID(), ownerUuid, ownerName, nickname,
                this.isMale(), this.getMaxHealth(), this.getDamage(), this.getSpeed(),
                this.getScale(), this.getLevel(), this.getTypeVariant(), this.getSkinIndex(),
                this.getUnlockedSkinsRaw(), teamTag);
    }

    /** Crée l'item Âme portant le snapshot de ce compagnon. */
    public ItemStack captureSoul() {
        ItemStack soulStack = new ItemStack(OWItems.ANIMAL_SOUL.get());
        soulStack.set(OWDataComponentTypes.SOUL_DATA.get(), this.buildSoulData());
        return soulStack;
    }

    /** Doit-on droper l'Âme à la mort ? Conditions communes à toutes les entités. */
    public boolean shouldDropSoulOnDeath() {
        return this.canDropSoul() && this.isTame() && !this.isInResurrection() && !this.isBaby();
    }

    /** Restaure intégralement ce compagnon (fraîchement spawn) depuis un snapshot d'Âme. */
    public void restoreFromSoul(SoulData data) {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(data.maxHealth());
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(data.damage());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(data.speed());
        this.setDamageToClient(data.damage());
        this.setGender(data.male() ? 1 : 0);
        this.setLevel(data.level());
        this.setLevelPoints(0);
        this.setScale(data.scale());
        this.setVariant(this, data.variant());
        this.setBaseHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 1.3f);
        this.setBaseDamage((float) this.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
        this.setBaseSpeed((float) this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
        if (!data.nickname().isEmpty()) this.setNickname(data.nickname());
        try {
            this.setUnlockedSkinsRaw(data.skinsUnlocked());
            if (data.skinIndex() != 0) this.changeSkinSilent(data.skinIndex());
        } catch (Exception ignored) {
            // Restauration de skin non critique : ne doit jamais interrompre la résurrection.
        }
        if (data.hasTeam()) this.applyTeamFromTag(data.teamTag());
        this.setHealth(this.getMaxHealth());
    }

    /** Écrit les données de la tribu courante dans le tag fourni (même format que la sauvegarde NBT). */
    public void writeTeamToTag(CompoundTag teamTag) {
        if (this.currentTeam == null) return;
        teamTag.putInt("teamId", currentTeam.getTeamId());
        teamTag.putString("teamName", currentTeam.getTeamName());
        teamTag.putString("teamOwnerUUID", currentTeam.getTeamOwnerUUID().toString());
        teamTag.putInt("teamColor", currentTeam.getTeamColor());
        teamTag.putInt("teamSecondaryColor", currentTeam.getTeamSecondaryColor());
        teamTag.putInt("teamMosaicPatternId", currentTeam.getTeamMosaicPattern().getId());
        teamTag.putString("teamCreationDate", currentTeam.getTeamCreationDate());

        ListTag pNames = new ListTag();
        for (String n : currentTeam.getPlayerNames()) pNames.add(net.minecraft.nbt.StringTag.valueOf(n));
        teamTag.put("playerNames", pNames);

        ListTag eNames = new ListTag();
        for (String n : currentTeam.getEntityNames()) eNames.add(net.minecraft.nbt.StringTag.valueOf(n));
        teamTag.put("entityNames", eNames);

        ListTag eUUIDs = new ListTag();
        for (UUID u : currentTeam.getEntityUUIDs()) eUUIDs.add(net.minecraft.nbt.StringTag.valueOf(u.toString()));
        teamTag.put("entityUUIDs", eUUIDs);

        ListTag pUUIDs = new ListTag();
        for (UUID u : currentTeam.getPlayerUUIDs()) pUUIDs.add(net.minecraft.nbt.StringTag.valueOf(u.toString()));
        teamTag.put("playerUUIDs", pUUIDs);

        byte[] pixels = currentTeam.getPaintPixels();
        teamTag.putByteArray("paintPixels", OWTeamMosaicPattern.packPixels3(pixels != null ? pixels : new byte[0]));
    }

    /** Reconstruit et applique une tribu depuis un tag (même format que la sauvegarde NBT). */
    public void applyTeamFromTag(CompoundTag teamTag) {
        if (teamTag == null || teamTag.isEmpty()) return;
        List<String> pNames = new ArrayList<>();
        ListTag pTag = teamTag.getList("playerNames", Tag.TAG_STRING);
        for (int i = 0; i < pTag.size(); i++) pNames.add(pTag.getString(i));

        List<String> eNames = new ArrayList<>();
        ListTag eTag = teamTag.getList("entityNames", Tag.TAG_STRING);
        for (int i = 0; i < eTag.size(); i++) eNames.add(eTag.getString(i));

        List<UUID> eUUIDs = new ArrayList<>();
        if (teamTag.contains("entityUUIDs")) {
            ListTag euTag = teamTag.getList("entityUUIDs", Tag.TAG_STRING);
            for (int i = 0; i < euTag.size(); i++) {
                try { eUUIDs.add(UUID.fromString(euTag.getString(i))); }
                catch (IllegalArgumentException ignored) {}
            }
        }

        UUID ownerUUID = UUID.fromString(teamTag.getString("teamOwnerUUID"));
        List<UUID> pUUIDs = new ArrayList<>();
        if (teamTag.contains("playerUUIDs")) {
            ListTag puTag = teamTag.getList("playerUUIDs", Tag.TAG_STRING);
            for (int i = 0; i < puTag.size(); i++) {
                try { pUUIDs.add(UUID.fromString(puTag.getString(i))); }
                catch (IllegalArgumentException ignored) {}
            }
        }
        // Migration des anciennes sauvegardes (aucun UUID joueur stocké) : le chef reste membre.
        if (pUUIDs.isEmpty()) pUUIDs.add(ownerUUID);

        byte[] savedPixels = OWTeamMosaicPattern.unpackPixels3(
                teamTag.contains("paintPixels") ? teamTag.getByteArray("paintPixels") : new byte[0],
                OWTeamMosaicPattern.CUSTOM_PAINT_PIXEL_COUNT);

        this.currentTeam = new OWTeam(
                teamTag.getInt("teamId"),
                teamTag.getString("teamName"),
                ownerUUID,
                teamTag.getInt("teamColor"),
                teamTag.contains("teamSecondaryColor") ? teamTag.getInt("teamSecondaryColor") : 0xFFFFFF,
                OWTeamMosaicPattern.byId(teamTag.contains("teamMosaicPatternId") ? teamTag.getInt("teamMosaicPatternId") : 0),
                new UUID[]{}, new OWEntity[]{},
                teamTag.getString("teamCreationDate"),
                pNames, eNames, savedPixels);
        this.currentTeam.setEntityUUIDs(eUUIDs);
        this.currentTeam.setPlayerUUIDs(pUUIDs);
    }

    public void setSleeping(boolean isSleeping) {
        this.entityData.set(IS_SLEEPING, isSleeping);
    }

    public boolean isSleeping() { return this.entityData.get(IS_SLEEPING);}

    public void setMaxSleepingBarTo(int maxSleep) { this.entityData.set(MAX_SLEEPING_BAR, maxSleep);}

    public int getMaxSleepingBar() { return this.entityData.get(MAX_SLEEPING_BAR);}

    public void setActualSleepingBarTo(int actualSleepingBar) { this.entityData.set(ACTUAL_SLEEPING_BAR, actualSleepingBar);}

    public int getActualSleepingBar() { return this.entityData.get(ACTUAL_SLEEPING_BAR);}

    public void setBodyXRot(float getBodyXRot) {
        if (this.hasEffect(OWEffects.FEAR_EFFECT.getDelegate())) this.entityData.set(BODY_X_ROT, 0.0f);
        else this.entityData.set(BODY_X_ROT, getBodyXRot);
    }
    public float getBodyXRot() { return this.entityData.get(BODY_X_ROT);}

    public void setBodyYRot(float getBodYXRot) {
        if (this.hasEffect(OWEffects.FEAR_EFFECT.getDelegate())) this.entityData.set(BODY_Y_ROT, 0.0f);
        else this.entityData.set(BODY_Y_ROT, getBodYXRot);
    }
    public float getBodyYRot() { return this.entityData.get(BODY_Y_ROT);}

    public void setBodyZRot(float getBodyZRot) {
        if (this.hasEffect(OWEffects.FEAR_EFFECT.getDelegate())) this.entityData.set(BODY_Z_ROT, 0.0f);
        else this.entityData.set(BODY_Z_ROT, getBodyZRot);
    }
    public float getBodyZRot() { return this.entityData.get(BODY_Z_ROT);}

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
        // Garde serveur : sans point de niveau disponible, aucune amélioration (empêche le spam de
        // paquets qui ferait grimper les stats gratuitement et passerait les points en négatif).
        if (this.getLevelPoints() <= 0) return;
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

    // Vrai si la monture etait dans l'eau au tick precedent : sert a annuler
    // l'elan vertical de nage quand elle sort de l'eau (sinon boost violent vers le haut).
    private boolean wasInWaterWhileRidden = false;

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

    /** Remet la vitesse de monture lissée à zéro (ex : après une attaque qui immobilise la monture,
     *  pour ne pas relancer le mouvement à l'ancienne allure conservée). */
    protected void resetRiddenSpeed() {
        this.currentSpeed = 0f;
        this.targetSpeed = 0f;
    }

    /**
     * Réactivité du lissage de vitesse, par tick. Assez haut pour que la monture réponde tout de
     * suite au doigt, assez bas pour qu'elle ne change pas d'allure d'un seul coup.
     */
    private static final float RIDDEN_SPEED_RESPONSE = 0.3f;

    public float getRiddenSpeedVehicle(Player player) {
        if (this.isSitting() || this.jumping) return 0.0f;

        final boolean reversing = player.zza < 0 && !isRunning();
        final boolean idle = player.zza == 0 && !this.isCombo();

        // En l'air, on garde l'élan tel quel : il n'y a rien à lisser tant qu'on ne touche pas le sol.
        if (!reversing && !idle && !this.onGround() && !this.isInWater()) return this.getSpeed();

        if (!reversing && !idle && isCombo()) {
            if ((this instanceof BoaEntity || this instanceof KangarooEntity) && this.isTame() && player.zza == 0) return 0.0f;
            if (this instanceof KodiakEntity kodiak) {
                if (kodiak.getComboAttack() == 3) {
                    return (this.getSpeed() / 3) * (vehicleComboSpeedMultiplier() / 4);
                }
            }
            if (vehicleComboSpeedMultiplier() != -1) {
                if (isChangeSpeedDuringCombo()) {
                    targetSpeed = (this.getSpeed() / 3) * (vehicleComboSpeedMultiplier() / 3);
                    return targetSpeed;
                }
            }
        }

        // Une seule cible, un seul lissage, quel que soit le cas.
        //
        // Le recul et l'arrêt court-circuitaient l'interpolation : le premier renvoyait sa vitesse
        // sans jamais toucher à {@code currentSpeed}, qui restait donc figé à l'allure de course.
        // Quand on relâchait la marche arrière, l'arrêt faisait décroître cette valeur périmée — et
        // la monture repartait en avant à presque pleine vitesse. Les faire passer par la même cible
        // règle le défaut et rend au passage tous les changements d'allure progressifs.
        float target;
        if (reversing) {
            target = -this.getSpeed() * 0.2f;
        } else if (idle) {
            target = 0f;
        } else if (isRunning()) {
            if (canIncreasesSpeedDuringSprint()) {
                return ((this.getSpeed() / 3) * ((this.vehicleRunSpeedMultiplier() * (0.5f + ((float) (Math.min(100, getAcceleration())) / 100))) / 2) * 1.75f);
            }
            target = this.getSpeed() * (vehicleRunSpeedMultiplier() / 1.75f);
        } else {
            target = (this.getSpeed() / 3) * (vehicleWalkSpeedMultiplier() / 2);
        }

        targetSpeed = target;
        if (canIncreasesSpeedDuringSprint()) {
            currentSpeed = target;
            return currentSpeed;
        }
        currentSpeed += (target - currentSpeed) * RIDDEN_SPEED_RESPONSE;
        if (Math.abs(currentSpeed) < 1.0e-4f) currentSpeed = 0f;   // évite une dérive résiduelle
        return currentSpeed;
    }

    /**
     * Dérive verticale par tick d'une monture en nage, cavalier regardant l'horizon. Négative, la
     * bête s'enfonce ; nulle, elle tient sa profondeur.
     *
     * <p>Ce léger poids convient aux semi-aquatiques, qu'on veut voir redescendre d'elles-mêmes vers
     * le fond quand on cesse de les diriger vers la surface. Les nageuses à part entière l'annulent
     * (cf. {@code OWWaterEntity}) : couler sous son cavalier n'est pas un comportement d'orque.</p>
     */
    protected double riddenBuoyancy() {
        return -0.01D;
    }

    /**
     * Poussée verticale, par tick, quand le cavalier tient la touche de saut en nage.
     *
     * <p>{@code 0} par défaut : sauter n'a rien à dire à une monture terrestre plongée dans l'eau,
     * et le saut sert déjà d'autre chose à certaines espèces. Les nageuses la relèvent pour offrir
     * une remontée à la demande, à la manière du submersible (cf. {@code OWWaterEntity}).</p>
     */
    protected double riddenAscendSpeed() {
        return 0.0D;
    }

    private void travelRidden(Player player, Vec3 travelVector) {
        Vec3 vec3 = this.getRiddenInput(player, travelVector);
        this.tickRidden(player, vec3);

        try {
            if (this.isControlledByLocalInstance()) {
                if (!this.isLeapingVehicle()) {
                    boolean inWater = this.isInWater();
                    Vec3 lookDirection = Vec3.directionFromRotation(inWater ? this.getXRot() : 0, this.getYRot()).normalize();
                    double speedPerTick = getRiddenSpeedVehicle(player) / (inWater ? vehicleWaterSpeedDivider() : 1);
                    Vec3 currentMovement = this.getDeltaMovement();
                    double yMovement;
                    if (inWater) {
                        yMovement = lookDirection.y * speedPerTick + riddenBuoyancy();
                        // Touche de saut : remontée à la demande, sans avoir à piquer du nez vers le
                        // haut. S'ajoute au pilotage au regard, comme sur le submersible. On lit la
                        // touche et non l'état de saut du cavalier, protégé et hors de portée d'ici ;
                        // cette branche ne tourne de toute façon que sur le client qui pilote.
                        if (riddenAscendSpeed() != 0.0D && this.level().isClientSide()
                                && net.tiew.operationWild.client.OWClientHooks.isJumpKeyDown()) {
                            yMovement += riddenAscendSpeed();
                        }
                    } else if (this.wasInWaterWhileRidden && currentMovement.y > 0.1) {
                        // On vient de sortir de l'eau : on coupe l'elan de nage vers le
                        // haut pour eviter une propulsion violente une fois hors de l'eau.
                        yMovement = 0.1;
                    } else {
                        yMovement = currentMovement.y;
                    }
                    this.setDeltaMovement(new Vec3(lookDirection.x * speedPerTick, yMovement, lookDirection.z * speedPerTick));
                    this.wasInWaterWhileRidden = inWater;
                }
                this.travel(this.isLeapingVehicle() ? Vec3.ZERO : vec3);

            } else if (this.level().isClientSide()) {
                this.calculateEntityAnimation(false);
                this.setDeltaMovement(Vec3.ZERO);
                this.tryCheckInsideBlocks();

            } else {
                if (this instanceof CrocodileEntity) {
                    Vec3 cur = this.getDeltaMovement();
                    double yVel;

                    if (this.isLeapingVehicle()) {
                        if (!this.onGround() && !this.isInWater() && !this.isNoGravity() && !this.hasEffect(MobEffects.LEVITATION)) {
                            yVel = Math.max(cur.y - this.getGravity(), -0.08);
                        } else {
                            yVel = cur.y;
                        }
                        this.setDeltaMovement(cur.x, yVel, cur.z);
                        this.move(MoverType.SELF, new Vec3(cur.x, yVel, cur.z));
                    } else if (!this.onGround() && !this.isInWater() && !this.isNoGravity() && !this.hasEffect(MobEffects.LEVITATION)) {
                        yVel = Math.max(cur.y - this.getGravity(), -0.08);
                        this.setDeltaMovement(0, yVel, 0);
                        this.move(MoverType.SELF, new Vec3(0, yVel, 0));
                    } else {
                        yVel = cur.y;
                        this.setDeltaMovement(0, yVel, 0);
                    }

                    this.tryCheckInsideBlocks();
                }
            }
        } catch (Exception e) {
            this.setRunning(false);
            this.setDeltaMovement(Vec3.ZERO);
            OW_LOGGER.warn("Mouvement de monture {} en echec, setRunning(false) applique", this.getName().getString(), e);
        }
    }

    @Override
    public boolean isControlledByLocalInstance() {
        if (this.getFirstPassenger() instanceof Player && !this.level().isClientSide()) {
            return false;
        }
        return super.isControlledByLocalInstance();
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return super.canCollideWith(entity);
    }

    @Override
    protected double getDefaultGravity() {
        return super.getDefaultGravity();
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

    /**
     * Variante <b>naturelle</b> de la créature (son pelage d'origine), indépendamment du skin
     * cosmétique qu'elle porte.
     *
     * <p>{@link #getTypeVariant()} rend la variante <i>courante</i>, qui vaut la variante cosmétique
     * dès qu'un skin est appliqué. Recréer une créature à partir de celle-ci donne une bête dont le
     * corps est peint avec la texture de l'accessoire — c'est ce qu'il faut pour un skin de
     * remplacement, et absolument pas pour un skin en surcouche, dont le corps doit garder son
     * pelage. Les sous-classes qui distinguent les deux surchargent cette méthode.</p>
     */
    /**
     * Tribu de cette créature, retrouvée depuis le registre si la référence en mémoire manque.
     *
     * <p>{@link #currentTeam} n'est <b>pas sérialisé</b> : il est posé par la synchronisation de
     * tribu et repart à {@code null} à chaque rechargement de chunk, changement de dimension ou
     * recréation d'entité. Une créature dont le maître est hors ligne pouvait ainsi rester sans
     * tribu indéfiniment — et donc s'en prendre à ses propres alliés, faute de les reconnaître.</p>
     *
     * <p>Le repli interroge la source de vérité par l'UUID du maître, et <b>réamorce</b> le champ au
     * passage : la recherche n'a lieu qu'une fois par créature. Côté client, où le registre n'existe
     * pas, on s'en tient à ce qui a été répliqué.</p>
     */
    public OWTeam resolvedTeam() {
        if (this.currentTeam != null) return this.currentTeam;
        if (this.level().isClientSide() || this.getServer() == null) return null;
        UUID owner = this.getOwnerUUID();
        if (owner == null) return null;
        this.currentTeam = OWTribesSavedData.get(this.getServer()).findTeamByMember(owner);
        return this.currentTeam;
    }

    public int getInitialTypeVariant() {
        return this.getTypeVariant();
    }

    public void setVariant(OWEntity entity, int variant) {
        if (entity instanceof KodiakEntity kodiak) {
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

        // Allie (meme proprietaire, ou segment de queue d'un Boa allie) : pas de ciblage.
        if (target != null && this.isAlliedTo(target)) {
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
            // Monté : NE PAS forcer l'état de combat ici (sinon ça écrase le setFighting(true) des
            // attaques avant que le timer fightingTime n'agisse → le boost « marauder » ne s'active jamais).
            // Quand on est monté, le combat est piloté par les attaques + le timer (cf. tick).
            if (!ownerIsRiding()) {
                setFighting(target != null);
            }
        }

        if (this.isTame() && this.getCurrentMode() == Mode.Passive) {
            super.setTarget(null);
            return;
        }

        if (ownerIsRiding() || this.isSitting()) {
            super.setTarget(null);
            return;
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

    /** Vrai si le joueur (UUID) fait partie de la tribu de cette entité (chef inclus). */
    public boolean isInMyTribe(UUID playerUuid) {
        if (playerUuid == null || this.currentTeam == null) return false;
        return this.currentTeam.isMember(playerUuid);
    }

    /**
     * Vrai si ce joueur a le droit d'agir sur cette entité comme un propriétaire :
     * il en est le propriétaire, OU il appartient à la même tribu que l'entité.
     * Sert de base à toutes les permissions de contrôle (montée d'attaques, inventaire,
     * assis/suivre, …). Des permissions plus fines par membre pourront s'y greffer plus tard.
     */
    public boolean canBeControlledBy(Player player) {
        if (player == null) return false;
        UUID id = player.getUUID();
        if (id.equals(this.getOwnerUUID())) return true;
        return this.isInMyTribe(id);
    }

    /**
     * Vrai si {@code player} a la permission {@code perm} sur cette entité. Le propriétaire, le chef et
     * les chefs adjoints ont toujours l'accès complet ; un membre simple doit posséder le bit de
     * permission correspondant (cf. onglet Permissions de la tribu).
     */
    public boolean hasTribePermission(Player player, net.tiew.operationWild.team.OWTribePermission perm) {
        if (player == null) return false;
        UUID id = player.getUUID();
        if (id.equals(this.getOwnerUUID())) return true;
        net.tiew.operationWild.team.OWTeam t = this.currentTeam;
        if (t == null || !t.isMember(id)) return false;
        if (t.isChief(id)) return true;              // le chef seul a l'accès total
        return t.hasPermissionBit(id, perm.bit());   // adjoint ET membre : selon leur bitmask
    }

    /** Permissions par défaut selon le rôle (utilisé à l'entrée / promotion). */
    public static int defaultPermissionsFor(net.tiew.operationWild.team.OWTeam team, java.util.UUID uuid) {
        if (team != null && team.isDeputy(uuid)) return net.tiew.operationWild.team.OWTribePermission.DEPUTY_DEFAULT;
        return net.tiew.operationWild.team.OWTribePermission.MEMBER_DEFAULT;
    }

    /**
     * Terrains d'arène sur lesquels cette espèce sait se battre, en masque de bits
     * ({@link net.tiew.operationWild.core.OWArena.Terrain#bit()}).
     *
     * <p>Le défaut est la <b>terre ferme seule</b>, et il est porté par la hiérarchie plutôt que
     * redéclaré espèce par espèce : {@link OWSemiWaterEntity} l'ouvre aux deux éléments,
     * {@link OWWaterEntity} le restreint à l'eau. Une créature qui hérite directement de cette
     * classe est un animal terrestre — le supposer à l'aise partout laissait un kangourou descendre
     * combattre au fond de l'eau, et aurait laissé passer chaque nouvelle espèce terrestre.</p>
     */
    public int arenaTerrainMask() {
        return net.tiew.operationWild.core.OWArena.Terrain.TERRESTRIAL.bit();
    }

    /**
     * Le porteur de tribu derrière une entité : l'entité elle-même, ou le Boa parent d'un segment de
     * queue. Toute autre entité (joueur, mob vanilla) n'en a pas.
     */
    @Nullable
    private static OWEntity tribeCarrierOf(@Nullable Entity entity) {
        if (entity instanceof OWEntity owE) return owE;
        if (entity instanceof BoaTailPart part && part.getParent() instanceof OWEntity parent) return parent;
        return null;
    }

    /**
     * Vrai si les deux entités appartiennent à la <b>même tribu</b>. Filet de sécurité serveur des
     * tirs alliés : les gardes par attaque (ciblage, zones d'effet) peuvent être contournés par une
     * attaque exotique, celui-ci ne l'est pas — il s'applique au moment où les dégâts arrivent.
     *
     * <p>Côté client, la tribu n'est répliquée que partiellement : la question ne s'y pose pas, les
     * dégâts sont décidés par le serveur.</p>
     */
    public static boolean shareTribe(@Nullable Entity a, @Nullable Entity b) {
        OWEntity first = tribeCarrierOf(a);
        OWEntity second = tribeCarrierOf(b);
        if (first == null || second == null || first == second) return false;
        if (first.level().isClientSide() || second.level().isClientSide()) return false;
        OWTeam teamA = first.resolvedTeam();
        if (teamA == null) return false;
        OWTeam teamB = second.resolvedTeam();
        return teamB != null && teamA.getTeamId() == teamB.getTeamId();
    }

    /**
     * Vrai si {@code target} est un allié de cette entité <b>apprivoisée</b> qu'une attaque de grab ne
     * doit jamais saisir : le propriétaire, un membre joueur de la tribu, une entité de la même tribu,
     * ou une entité possédée par le propriétaire / un membre de la tribu. Toujours {@code false} à l'état
     * sauvage (aucune protection). Sert de garde commun aux grabbers (Tigre, Crocodile, Boa).
     */
    public boolean isTameGrabAlly(Entity target) {
        if (target == null || !this.isTame()) return false;

        // Joueur : propriétaire ou membre de la tribu.
        if (target instanceof Player player) {
            return this.canBeControlledBy(player);
        }

        // Entité OW : même tribu, ou possédée par le propriétaire / un membre de la tribu.
        if (target instanceof OWEntity owE) {
            if (owE == this) return true;
            if (this.currentTeam != null && owE.currentTeam != null
                    && this.currentTeam.getTeamId() == owE.currentTeam.getTeamId()) return true;
            UUID myOwner = this.getOwnerUUID();
            UUID theirOwner = owE.getOwnerUUID();
            if (myOwner != null && myOwner.equals(theirOwner)) return true;
            return theirOwner != null && this.isInMyTribe(theirOwner);
        }

        return false;
    }

    @Override
    public void remove(net.minecraft.world.entity.Entity.RemovalReason reason) {
        // Réputation : ne purge l'entrée que lors d'une suppression réelle (mort / discard),
        // jamais lors d'un simple déchargement de chunk (sinon on perdrait les contributions
        // des créatures des membres hors-ligne).
        if (!this.level().isClientSide
                && (reason == net.minecraft.world.entity.Entity.RemovalReason.KILLED
                || reason == net.minecraft.world.entity.Entity.RemovalReason.DISCARDED)) {
            net.minecraft.server.MinecraftServer srv = this.level().getServer();
            if (srv != null) net.tiew.operationWild.team.OWReputationData.get(srv).removeEntity(this.getUUID());
        }
        super.remove(reason);
    }

    private static final float BANK_MAX_ANGLE = 34.0f;
    private static final float BANK_REFERENCE_YAW_RATE = 5.5f;
    private static final float BANK_SHARPNESS = 1.15f;
    private static final float BANK_RATE_SMOOTHING = 0.55f;
    private static final float BANK_RISE = 0.3f;
    private static final float BANK_FALL = 0.12f;
    private static final float BANK_CAMERA_SHARE = 0.4f;

    private static final float PITCH_MAX_ANGLE = 45.0f;
    private static final float PITCH_RISE = 0.22f;
    private static final float PITCH_FALL = 0.12f;
    private static final float FREE_PITCH_GAIN = 1.25f;

    private static final float LEAN_CRUISE_STEP = 0.1f;
    private static final float LEAN_IDLE_DRIVE = 0.3f;

    /**
     * Réglages propres à la nage LIBRE, plus doux que ceux du pilotage.
     *
     * <p>Le signal de lacet d'une bête livrée à son IA est bien plus bruité que celui d'une monture.
     * Vanilla ne réoriente son corps sur sa direction de marche qu'au-delà de 0,05 bloc parcouru par
     * tick ({@code LivingEntity#tick}) : une nageuse en vadrouille flotte autour de ce seuil, et la
     * réorientation s'allume et s'éteint d'un tick à l'autre. Ses positions arrivent en prime par
     * paquets, interpolées sur trois ticks, ce qui hache encore le déplacement mesuré.</p>
     *
     * <p>Filtrer plus fort et monter plus lentement n'y coûte rien — une orque sauvage ne vire pas
     * dans l'urgence — et supprime les à-coups que ce bruit provoquait.</p>
     */
    private static final float BANK_RATE_SMOOTHING_FREE = 0.25f;
    private static final float BANK_RISE_FREE = 0.15f;

    /** Lissage de l'allure, pour que le facteur d'entraînement ne saute pas d'un tick à l'autre. */
    private static final float LEAN_STEP_SMOOTHING = 0.25f;

    private float bankRoll = 0f;
    private float bankRollPrev = 0f;
    private float bankYawRate = 0f;
    private float leanPitch = 0f;
    private float leanPitchPrev = 0f;
    private float leanStep = 0f;

    public float getBankRoll(float partialTick) {
        return Mth.lerp(partialTick, this.bankRollPrev, this.bankRoll);
    }

    public float getBankCameraRoll(float partialTick) {
        return this.getBankRoll(partialTick) * BANK_CAMERA_SHARE;
    }

    public float getRidePitch(float partialTick) {
        return Mth.lerp(partialTick, this.leanPitchPrev, this.leanPitch);
    }

    /**
     * Ouvre l'inclinaison de nage. Fausse par défaut : une bête terrestre qui traverse une rivière
     * n'a pas à s'incliner dans ses virages. Les familles aquatiques la relèvent.
     */
    protected boolean canLean() { return false; }

    /**
     * Vrai pendant une FIGURE en roulis — un tonneau, par opposition à l'assiette d'un virage.
     *
     * <p>Sert à la vue à la première personne : elle suit l'arc que décrit l'œil du cavalier quand
     * son corps s'incline, ce qui n'a de sens que pour une inclinaison bornée. Sur un tour complet,
     * le même calcul promènerait la caméra sur un cercle d'un mètre et demi.</p>
     */
    public boolean isRollingFigure() { return false; }

    /**
     * La vue à la première personne suit-elle l'inclinaison du corps du cavalier ?
     *
     * <p>Le modèle du cavalier pivote autour de ses PIEDS : dès que la monture penche, son œil décrit
     * un arc. À {@code true}, la caméra se pose sur cet œil et accompagne le mouvement ; à
     * {@code false}, elle reste à l'aplomb de la position d'entité, comme avant l'ajout de ce
     * rattrapage.</p>
     *
     * <p>Ce n'est pas un défaut à corriger partout : sur une monture qui se couche franchement, suivre
     * l'arc rend la vue solidaire du corps ; sur une monture au roulis discret, ça n'apporte qu'un
     * ballant. Chaque espèce tranche donc pour elle-même, et la réponse est {@code false} par
     * défaut.</p>
     */
    public boolean riderCameraFollowsBodyTilt() { return false; }

    /**
     * Matrice des os qui portent le cavalier, ou {@code null} si l'espèce n'en publie pas.
     *
     * <p>Quand elle existe, l'orientation du cavalier s'en déduit EXACTEMENT au lieu d'être
     * reconstruite en additionnant les angles d'Euler de chaque os. Cette somme n'est juste qu'aux
     * petits angles et ignore l'ordre de composition : le cavalier était posé au bon endroit — la
     * position, elle, vient déjà de la matrice — mais orienté de travers dès que la bête penchait
     * franchement.</p>
     */
    public org.joml.Matrix4f riderBoneMatrix() { return null; }

    /**
     * {@code positionRider} rend-il une assise juste à l'IMAGE, ou seulement au tick ?
     *
     * <p>Le rattrapage d'assise de {@code OWRiderSmoothing} vaut {@code siège exact − position
     * interpolée par le moteur}. Il ne redresse quelque chose que si le premier terme est réellement
     * recalculé à chaque image — c'est le cas des espèces dont le siège se déduit d'une matrice d'os,
     * relevée au rendu.</p>
     *
     * <p>{@code false} pour une espèce dont le siège est figé sur le tick : la soustraction y annule
     * purement l'interpolation du moteur et cloue le cavalier sur sa position de tick, soit vingt
     * pas par seconde. Mieux vaut alors ne rien corriger du tout et laisser le moteur interpoler.</p>
     */
    public boolean riderSeatIsFrameAccurate() { return true; }

    protected float bankMaxAngle() { return BANK_MAX_ANGLE; }

    /**
     * Vitesse de lacet, en degrés par tick, à laquelle le roulis atteint son amplitude maximale.
     *
     * <p>Le régime n'a rien de commun entre les deux cas, d'où le paramètre. Livrée à son IA, une
     * bête tourne d'une fraction de degré par tick ; sous les mains d'un cavalier, elle rattrape le
     * regard à {@code getRotationSpeed()} de l'écart et peut monter à plusieurs degrés. Un seuil
     * réglé sur la vadrouille sature donc au moindre geste de souris.</p>
     */
    protected float bankReferenceYawRate(boolean ridden) { return BANK_REFERENCE_YAW_RATE; }

    /** {@code 0} désactive le canal de tangage, pour une espèce qui gère le sien autrement. */
    protected float pitchMaxAngle() { return PITCH_MAX_ANGLE; }

    /**
     * En nage LIBRE, le tangage se déduit-il de la pente réellement parcourue ?
     *
     * <p>{@code false} laisse ce cas à l'espèce, qui a son propre pilotage — le tangage monté, lui,
     * reste pris en charge dans les deux cas.</p>
     */
    protected boolean leanPitchWhenFree() { return true; }

    protected LivingEntity leaningRider() {
        if (!this.canLean()) return null;
        return this.getControllingPassenger();
    }

    private void tickLean() {
        this.bankRollPrev = this.bankRoll;
        this.leanPitchPrev = this.leanPitch;

        float rollTarget = 0f;
        float pitchTarget = 0f;

        if (this.canLean()) {
            double dx = this.getX() - this.xOld;
            double dy = this.getY() - this.yOld;
            double dz = this.getZ() - this.zOld;
            float step = (float) java.lang.Math.sqrt(dx * dx + dy * dy + dz * dz);

            // Allure lissee, et non le pas du tick : une nageuse en vadrouille croise justement le
            // seuil de LEAN_CRUISE_STEP, si bien que le facteur d'entrainement sautait entre 0,3 et 1
            // d'un tick a l'autre — et le roulis avec lui.
            this.leanStep += (step - this.leanStep) * LEAN_STEP_SMOOTHING;
            float drive = Mth.clamp(this.leanStep / LEAN_CRUISE_STEP, LEAN_IDLE_DRIVE, 1f);

            LivingEntity rider = this.leaningRider();
            boolean ridden = rider != null;

            float yawRate = Mth.wrapDegrees(this.yBodyRot - this.yBodyRotO);
            this.bankYawRate += (yawRate - this.bankYawRate)
                    * (ridden ? BANK_RATE_SMOOTHING : BANK_RATE_SMOOTHING_FREE);

            float reference = this.bankReferenceYawRate(ridden);
            float normalized = Mth.clamp(java.lang.Math.abs(this.bankYawRate) / reference, 0f, 1f);
            float shaped = (float) java.lang.Math.pow(normalized, BANK_SHARPNESS);
            rollTarget = -java.lang.Math.signum(this.bankYawRate) * shaped * this.bankMaxAngle() * drive;

            float maxPitch = this.pitchMaxAngle();
            if (maxPitch > 0f) {
                if (ridden) {
                    pitchTarget = Mth.clamp(this.getRiddenRotation(rider).x, -maxPitch, maxPitch) * drive;
                } else if (this.leanPitchWhenFree() && step > 1.0E-5f) {
                    double horizontal = java.lang.Math.sqrt(dx * dx + dz * dz);
                    float slope = (float) java.lang.Math.toDegrees(java.lang.Math.atan2(-dy, horizontal));
                    pitchTarget = Mth.clamp(slope * FREE_PITCH_GAIN, -maxPitch, maxPitch) * drive;
                }
            }
        } else {
            this.bankYawRate *= 0.5f;
            this.leanStep *= 0.5f;
        }

        float rise = this.getControllingPassenger() != null ? BANK_RISE : BANK_RISE_FREE;
        float rollResponse = java.lang.Math.abs(rollTarget) > 0.5f ? rise : BANK_FALL;
        this.bankRoll += (rollTarget - this.bankRoll) * rollResponse;
        if (java.lang.Math.abs(this.bankRoll) < 0.01f) this.bankRoll = 0f;

        float pitchResponse = java.lang.Math.abs(pitchTarget) > 0.5f ? PITCH_RISE : PITCH_FALL;
        this.leanPitch += (pitchTarget - this.leanPitch) * pitchResponse;
        if (java.lang.Math.abs(this.leanPitch) < 0.01f) this.leanPitch = 0f;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) this.tickLean();

        if (!this.level().isClientSide) {
            // Résout la tribu depuis le propriétaire (registre serveur) une fois après le chargement.
            resolveTeamFromOwnerIfNeeded();
            equipDefaultSkinIfNeeded();
            if (this.level().getGameRules().getBoolean(OWGameRules.ANIMALS_NO_EFFORT)) {
                this.setVitalEnergy(0);
            }
            // Leveling par absorption d'orbes (comme le joueur) tant que le niveau max n'est pas atteint.
            if (this.isTame() && this.getLevel() < 50) {
                if (this.getLevel() < 1) {
                    this.setLevel(1);
                    this.setXpStage(OWUtils.xpToNextLevel(1));
                }
                absorbNearbyXpOrbs();
            }
            // Réputation de tribu : upsert throttlé de cette créature dans le registre serveur persistant.
            if ((this.tickCount + this.getId()) % 100 == 0) {
                net.minecraft.server.MinecraftServer srv = this.level().getServer();
                if (srv != null) {
                    net.tiew.operationWild.team.OWReputationData rep = net.tiew.operationWild.team.OWReputationData.get(srv);
                    if (this.isTame() && this.getOwnerUUID() != null) rep.upsertEntity(this);
                    else rep.removeEntity(this.getUUID());
                }
            }
        }

        if (this.isTame()) {
            this.setCustomName(Component.nullToEmpty(this.getNickname()));
        }

        if (this.level().isClientSide) {
            handleClientAnimationSync();
        }

        if (this.isCombo()) {
            this.setAcceleration(0);
        }

        createTransitionAnimation("idleSit", transitionIdleSit, this.isSitting(), 13);
        createTransitionAnimation("sitIdle", transitionSitIdle, !this.isSitting(), 13);
        createTransitionAnimation("idleSleep", transitionIdleSleep, this.isNapping() || this.isSleeping(), 20);
        createTransitionAnimation("sleepIdle", transitionSleepIdle, !this.isNapping() && !this.isSleeping(), 20);

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
            double dx = this.getX() - prevTickX;
            double dz = this.getZ() - prevTickZ;
            boolean isActuallyMoving = dx * dx + dz * dz > 0.00005;

            if (this.isRunning() && this.isVehicle() && this.isTame() && isActuallyMoving) {
                boolean isCrocodileInWater = this instanceof CrocodileEntity crocodile && crocodile.isInWater();

                if (this instanceof KangarooEntity) return;

                setVitalEnergy(getVitalEnergy() + ((!isCrocodileInWater) ? 0.75f : 0.5f));
            }

            if (!isRunning() && getVitalEnergy() > 0 && !isCombo()) {
                setVitalEnergy(getVitalEnergy() - getVitalEnergyRecuperation());
            }

            prevTickX = this.getX();
            prevTickZ = this.getZ();
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
                // Fin de la fragilité : le compagnon stabilisé pourra de nouveau livrer son Âme s'il meurt.
                this.setCanDropSoul(true);
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

                    choosenQuest = 0;
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
            if (this.onGround()) {
                this.setDeltaMovement(0, 0, 0);
                this.hasImpulse = false;
                this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,5, 255, false, false, false));
            }
        }

        if (this.isNapping()) {
            getNavigation().stop();
            getMoveControl().setWantedPosition(getX(), getY(), getZ(), 0);
            if (this.onGround()) {
                this.setDeltaMovement(0, 0, 0);
                this.hasImpulse = false;
                this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 255, false, false, false));
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

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            // Quêtes PROPRES À CHAQUE INDIVIDU : à chaque nouvelle journée (14h heure réelle), cette
            // entité tire ses 3 propres quêtes, remet sa progression à zéro et lève sa notification.
            // Vérifié une fois par seconde (l'horloge réelle n'a pas besoin d'être lue 20x/s).
            if (this.tickCount % 20 == 0) {
                long period = OWDailyQuests.computePeriodDay();
                if (period != this.lastQuestResetDay || this.activeQuest0 < 0) {
                    // Nouveau jour (ou entité jamais initialisée) : reroll complet + reset + notification.
                    this.rerollDailyQuests();
                    this.resetDailyQuestProgress();
                    this.lastQuestResetDay = period;
                    this.dailyRerollAvailable = true;   // le reroll manuel se recharge chaque jour
                    this.setUpdatingQuests(!ownerHasSeenQuestPeriod(period));
                } else if (this.questReward0 == 0) {
                    // Migration : quêtes déjà en cours mais récompenses pas encore tirées (sauvegarde
                    // antérieure à ce champ) → on tire seulement les récompenses, sans toucher aux quêtes.
                    this.questReward0 = rollQuestReward(this.activeQuest0);
                    this.questReward1 = rollQuestReward(this.activeQuest1);
                    this.questReward2 = rollQuestReward(this.activeQuest2);
                }

                // Journée déjà lue ailleurs : on éteint la pastille. Le cas se présente pour une
                // créature qui tire ses quêtes après coup — déchargée au changement de jour, ou
                // revenue en jeu plus tard : elle n'annoncerait rien que son maître n'ait déjà vu.
                if (this.questsAreUpdated() && ownerHasSeenQuestPeriod(period)) {
                    this.setUpdatingQuests(false);
                }
            }

            // La progression des quêtes n'est consultée que par le joueur qui chevauche l'entité
            // (l'écran de quêtes exige de la chevaucher) : inutile de diffuser à tout le monde.
            if (this.getControllingPassenger() instanceof ServerPlayer questRider) {
                OWNetworkHandler.sendToClient(new OWQuestProgressToClient(this.getId(),
                        this.activeQuest0, this.activeQuest1, this.activeQuest2,
                        this.questReward0, this.questReward1, this.questReward2,
                        this.dailyRerollAvailable,
                        this.quest0Progression, this.quest1Progression, this.quest2Progression,
                        this.quest3Progression, this.quest4Progression, this.quest5Progression, this.quest6Progression, this.quest7Progression,
                        this.quest8Progression, this.quest9Progression, this.quest10Progression, this.quest0isLocked, this.quest1isLocked, this.quest2isLocked, this.quest3isLocked,
                        this.quest4isLocked, this.quest5isLocked, this.quest6isLocked, this.quest7isLocked, this.quest8isLocked, this.quest9isLocked, this.quest10isLocked), questRider);
            }

            for (ServerPlayer player : serverLevel.players()) {
                OWNetworkHandler.sendToClient(new OWEntityUtilsToClient(this.getId(), this.resurrectionTimer, this.attackTimer), player);
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
                if (fightingTime > 0) {
                    fightingTime--;
                    if (!this.level().isClientSide()) this.entityData.set(FIGHT_COOLDOWN, fightingTime);
                } else {
                    this.setFighting(false);
                    this.setTarget(null);
                }
            }
        }

        if (!this.level().isClientSide() && this.isTame()) tickFeeding();

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

        // Régénération passive : un fond de récupération au repos, rien de plus. Elle ne doit
        // jamais rivaliser avec le ravitaillement — c'est la nourriture rangée dans l'inventaire qui
        // doit remettre une bête sur pied, pas le simple fait d'attendre. Coupée dès qu'un combo est
        // engagé, au même titre qu'en combat : une créature en train de frapper ne se soigne pas.
        if (this.isTame() && !this.isInFight() && !this.isCombo()
                && tickCount % PASSIVE_REGEN_INTERVAL_TICKS == 0) {
            this.heal(PASSIVE_REGEN_AMOUNT);
            healAmount = 1;
            if (isQuestInProgress(DailyQuestRegistry.quest3) && !this.level().isClientSide()) {
                this.executeQuestProgression((byte) 2);
            }
        }
    }

    public int getComboPauseDelay() {
        return 2;
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
            if (this.getTarget() != null) this.setLookAt(this.getTarget().getX(), this.getTarget().getY(),this.getTarget().getZ());
            if (attackTimer < timeMax) attackTimer++;
            else {
                attackTimer = 0;
                setCombo(false, 0);
                return;
            }
            if (attackTimer == timeToHit) {
                float d0 = (float) ((this.getDamage() / MAX_ATTACKS_IN_COMBO) * (isTame() ? 1.0 : SAVAGE_ENTITY_DAMAGE_MULTIPLIER));
                attackEntitiesInFront(d0, sound, width * (isRided ? 1 : 1.5f), height * (isRided ? 1 : 1.5f), reach * (isRided ? 1 : 1.5f), backMultiplier);

                if (spawnBlurr) {
                    OWUtils.spawnBlurrParticle(this.level(), this, 1, 1, 1);
                }
            }

            applyComboModification(timeToHit);

            if (attackTimer == timeToHit + getComboPauseDelay()) {
                setPauseCombo(true);
            }
        }
    }

    public void applyComboModification(int timeToHit) {
        if (this instanceof KodiakEntity kodiak) {
            if (attackTimer == timeToHit) {
                float pitch = (float) (OWUtils.generateRandomInterval(1.15, 1.4));
                kodiak.level().playSound(null, kodiak.getX(), kodiak.getY(), kodiak.getZ(), OWSounds.LEG_HURT.get(), SoundSource.HOSTILE, 1.0f, pitch);
                if (getComboAttack() == 3) {
                    kodiak.createMiniShockwave();
                }
            }
        }
        // Tigre : une gerbe de terre sous la patte a CHACUN des trois coups, la ou le kodiak ne
        // secoue le sol qu'au dernier.
        if (this instanceof TigerEntity tigerCombo) {
            if (attackTimer == timeToHit) tigerCombo.createPawImpact();
        }
        if (this instanceof CrocodileEntity crocodile) {
            if (attackTimer == 1) {
                float pitch = (float) (OWUtils.generateRandomInterval(1.1, 1.25));
                SoundEvent sound = RANDOM(2) ? OWSounds.CROCODILE_HIT_1.get() : OWSounds.CROCODILE_HIT_2.get();
                crocodile.level().playSound(null, crocodile.getX(), crocodile.getY(), crocodile.getZ(), sound, SoundSource.HOSTILE, 1.0f, pitch);
            }
            if (attackTimer == timeToHit - 3) {
                float pitch = (float) (OWUtils.generateRandomInterval(0.8, 1.0));
                crocodile.level().playSound(null, crocodile.getX(), crocodile.getY(), crocodile.getZ(), OWSounds.LEG_HURT.get(), SoundSource.HOSTILE, 1.0f, pitch);
            }
        }
        if (this instanceof KangarooEntity kangaroo) {
            if (attackTimer == timeToHit) {
                float pitch = (float) (OWUtils.generateRandomInterval(1.3, 1.65));
                kangaroo.level().playSound(null, kangaroo.getX(), kangaroo.getY(), kangaroo.getZ(), OWSounds.LEG_HURT.get(), SoundSource.HOSTILE, 1.0f, getComboAttack() == 3 ? pitch / 1.5f : pitch);

                if (getComboAttack() == 3) {
                    // Uppercut : on ne propulse QUE les ennemis reellement frappes par l'attaque
                    // (remplis par attackEntitiesInFront), pas tout ce qui passe dans une zone elargie.
                    for (LivingEntity target : kangaroo.lastAttackHitEntities) {
                        Vec3 motion = target.getDeltaMovement();
                        target.setDeltaMovement(motion.x * 0.3, 0.65, motion.z * 0.3);
                        target.hurtMarked = true;

                        if (kangaroo.level() instanceof ServerLevel serverLevel) {
                            Vec3 look = kangaroo.getLookAngle();
                            double spawnX = kangaroo.getX() + look.x * 1.5;
                            double spawnY = kangaroo.getY() + kangaroo.getBbHeight() * 0.5;
                            double spawnZ = kangaroo.getZ() + look.z * 1.5;

                            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                                    spawnX, spawnY, spawnZ,
                                    1, 0.0, 0.0, 0.0, 0.0);
                        }
                    }
                }
            }

            if (attackTimer == timeToHit + 21) {
                if (getComboAttack() == 3) {
                    boolean isRided = this.getControllingPassenger() != null;
                    float d0 = (float) ((this.getDamage() / MAX_ATTACKS_IN_COMBO) * (isTame() ? 1.0 : SAVAGE_ENTITY_DAMAGE_MULTIPLIER));
                    attackEntitiesInFront(d0, SoundEvents.PLAYER_ATTACK_STRONG, 3.0f * (isRided ? 1 : 1.5f), 3.0f * (isRided ? 1 : 1.5f), 1.5f * (isRided ? 1 : 1.5f), 0.5f);
                    kangaroo.createMiniShockwave();
                    kangaroo.fourthHitFired = true;
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
        // Le kangourou lève ce drapeau au quatrième coup pour taire l'animation du troisième. Il
        // n'était jamais rabaissé : une fois le geste joué, la condition restait fausse à vie et
        // l'animation ne revenait plus jamais. La fin d'un enchaînement le remet à zéro.
        if (this instanceof net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity kangaroo) {
            kangaroo.fourthHitFired = false;
        }
    }

    public void createCombo(int timeMax, int timeToHit, SoundEvent sound, double width, double height, double reach, boolean spawnBlurr, float backMultiplier) {
        if (!this.isAlive()) return;
        // Cible morte (ex : entité tuée à l'instant, encore référencée le temps de son anim de mort /
        // avant despawn) : on l'OUBLIE au lieu de bloquer. Sinon la machine d'état du combo se fige
        // (attackTimer gelé, isCombo jamais remis à false) et l'attaque « boucle sans finir ».
        if (this.getTarget() != null && this.getTarget().getHealth() <= 0.0F) {
            this.setTarget(null);
        }
        if (isPauseCombo()) {
            continueComboMaxTimer++;

            if (this.playerContinueCombo && actualAttackNumber < (MAX_ATTACKS_IN_COMBO - 1)) {
                continueComboMaxTimer = 0;

                actualAttackNumber++;

                resetCombo(actualAttackNumber);
                setCombo(true, actualAttackNumber + 1);
                setPauseCombo(false);
            }

            int comboContinueWindow = timeMax - timeToHit;
            if (continueComboMaxTimer >= comboContinueWindow) {
                resetCombo(0);
                actualAttackNumber = 0;
            }
        } else {
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

    public void attackEntitiesInFrontSimple(float attackDamage, SoundEvent sound, double width, double height, double reach, float knockbackMultiplier) {
        if (this.level().isClientSide()) {
            return;
        }

        Entity passenger = this.getControllingPassenger();
        float yawToUse = this.getYRot();
        Vec3 posToUse = this.position();

        if (passenger != null) {
            yawToUse = passenger.getYRot();
            posToUse = passenger.position();
        }

        float pitch = (float) OWUtils.generateRandomInterval(0.8, 1.1f);
        double yaw = Math.toRadians(yawToUse);
        double centerX = posToUse.x - Math.sin(yaw) * reach;
        double centerZ = posToUse.z + Math.cos(yaw) * reach;
        double centerY = posToUse.y;

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

        for (Entity entity : entitiesInRange) {
            if (entity instanceof LivingEntity livingEntity) {
                if (this.isAlliedTo(livingEntity)) {
                    continue;
                }

                if (livingEntity instanceof Player player && player.getVehicle() != null) {
                    continue;
                }

                boolean hurtResult = livingEntity.hurt(this.damageSources().mobAttack(this), attackDamage);

                if (hurtResult && !(livingEntity instanceof Player player && player.isCreative())) {
                    this.onSuccessfulHit(livingEntity);
                }

                if (knockbackMultiplier > 0 && hurtResult) {
                    if (!(livingEntity instanceof Player player && player.isCreative())) {
                        this.hurtAfterCombo(livingEntity, this.getComboAttack());

                        Vec3 knockbackDirection = livingEntity.position().subtract(this.position()).normalize();
                        Vec3 knockback = knockbackDirection.scale(knockbackMultiplier * 0.4);
                        livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(knockback.x, knockback.y * 0.3, knockback.z));
                    }
                }
            }
        }

        this.setFighting(true);
        fightingTime = 200;
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), sound, SoundSource.NEUTRAL, 1.0F, pitch);
    }

    public void createComboSimple(int timeMax, int timeToHit, SoundEvent sound, double width, double height, double reach, float backMultiplier) {
        if (!this.isAlive()) return;

        if (isPauseCombo()) {
            continueComboMaxTimer++;

            if (this.playerContinueCombo && actualAttackNumber < (MAX_ATTACKS_IN_COMBO - 1)) {
                continueComboMaxTimer = 0;

                actualAttackNumber++;

                resetCombo(actualAttackNumber);

                setCombo(true, actualAttackNumber + 1);


                setPauseCombo(false);
            }

            int comboContinueWindow = timeMax - timeToHit;
            if (continueComboMaxTimer >= comboContinueWindow) {
                resetCombo(0);
                actualAttackNumber = 0;
            }
        } else {
            if (this.isCombo()) {
                if (this.getTarget() != null) {
                    this.setLookAt(this.getTarget().getX(), this.getTarget().getY(), this.getTarget().getZ());
                }

                if (attackTimer < timeMax) {
                    attackTimer++;
                } else {
                    attackTimer = 0;
                    setCombo(false, 0);
                    return;
                }

                if (attackTimer == timeToHit) {
                    boolean isRided = this.getControllingPassenger() != null;
                    attackEntitiesInFrontSimple(
                            (float) ((this.getDamage() / MAX_ATTACKS_IN_COMBO) * (this.isVehicle() && !this.isTame() ? 1.0 : SAVAGE_ENTITY_DAMAGE_MULTIPLIER)),
                            sound,
                            width * (isRided ? 1 : 1.5f),
                            height * (isRided ? 1 : 1.5f),
                            reach * (isRided ? 1 : 1.5f),
                            backMultiplier
                    );
                }

                applyComboModification(timeToHit);

                if (attackTimer == timeToHit + 2) {
                    setPauseCombo(true);
                }
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

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (this.currentTeam != null) {
            OWNetworkHandler.sendToClient(SyncOWTeamPacket.of(this.getId(), this.currentTeam), player);
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
        smoothRotation(vec2, player);
        player.resetFallDistance();
    }

    private void smoothRotation(Vec2 vec2, Player player) {
        targetYRot = vec2.y;

        float currentYRot = this.getYRot();
        float deltaRot = Mth.wrapDegrees(targetYRot - currentYRot);

        float newYRot = currentYRot + deltaRot * getRotationSpeed();

        if (player.zza == 0 && player.xxa == 0 && !this.isCombo() && getRotationSpeed() < 1.0f && !forceRiderLookBodyRotation()) {
            this.yHeadRot = newYRot;
            this.setXRot(vec2.x);
        } else {
            this.setRot(newYRot, vec2.x);
            this.yRotO = this.yBodyRot = this.yHeadRot = newYRot;
        }
    }

    /**
     * Force la rotation du CORPS de la monture vers le regard du rider même à l'arrêt
     * (sinon seule la tête suit quand le rider n'avance pas). Surchargé par les entités
     * qui ont besoin de viser à l'arrêt (ex : tornade du kangourou).
     */
    protected boolean forceRiderLookBodyRotation() { return false; }

    /**
     * Empêche le sneak + clic droit de basculer assis/debout (ex : pendant une attaque ultime).
     * Surchargé par les entités qui ont besoin de verrouiller cette interaction.
     */
    protected boolean isSittingToggleLocked() { return false; }

    public Vec2 getRiddenRotation(LivingEntity livingEntity) { return new Vec2(livingEntity.getXRot() * 0.5F, livingEntity.getYRot());}

    @Override
    public float maxUpStep() {
        return this.getFirstPassenger() instanceof Player ? 1.0f : super.maxUpStep();
    }

    /**
     * États d'animation des trois coups de combo, et leurs minuteurs de vie.
     *
     * <p>Déclarés ici plutôt que recopiés dans chaque espèce : ils étaient identiques aux six
     * exemplaires, et {@code OWComboModel} s'appuie dessus pour rendre l'enchaînement une bonne fois
     * pour toutes.</p>
     */
    public final AnimationState attack1Combo = new AnimationState();
    public final AnimationState attack2Combo = new AnimationState();
    public final AnimationState attack3Combo = new AnimationState();

    public int attack1ComboTimer = 0;
    public int attack2ComboTimer = 0;
    public int attack3ComboTimer = 0;

    /** Coup de combo actif au tick précédent, par emplacement (1..3). Sert à repérer un redémarrage. */
    private final boolean[] comboWasActive = new boolean[4];

    /**
     * Fait vivre l'animation d'un coup de combo et renvoie son minuteur mis à jour.
     *
     * <p>Le redémarrage se décide sur le <b>front montant</b> de {@code active}, et non sur un
     * minuteur épuisé. C'est la correction d'un défaut de conception : l'animation ne repartait que
     * si son minuteur était retombé à zéro, si bien qu'un coup relancé alors que sa queue courait
     * encore — ce qui arrive dès qu'on enchaîne vite, la chaîne revenant sur un emplacement déjà
     * joué — voyait son {@code start()} purement sauté. Le geste ne se rejouait pas. Le défaut
     * dormait tant que les queues étaient courtes ; les allonger l'a réveillé.</p>
     *
     * <p>Mutualisé ici plutôt que recopié dans chaque espèce : la logique était identique aux six
     * exemplaires, et un correctif appliqué à cinq d'entre eux n'aurait servi à rien.</p>
     *
     * @param active vrai si ce coup doit jouer ce tick (l'espèce décide : état de combo, garde propre…)
     */
    protected int tickComboAnimation(int comboNumber, AnimationState state, int timer, int maxTimer, boolean active) {
        if (comboNumber < 1 || comboNumber >= comboWasActive.length) return timer;
        boolean was = comboWasActive[comboNumber];
        comboWasActive[comboNumber] = active;

        if (active) {
            if (!was) {
                state.start(this.tickCount);   // front montant : le geste repart de sa première image
                return maxTimer;
            }
            return Math.max(0, timer - 1);
        }
        // Queue : le geste finit de se jouer et se mélange au coup suivant, puis s'éteint.
        if (timer > 0) return timer - 1;
        state.stop();
        return 0;
    }

    /**
     * Où se trouve la place de ce cavalier <b>à cet instant</b>, sans l'y déplacer.
     *
     * <p>Rejoue le calcul de siège de l'espèce en lui passant une fonction qui se contente de noter
     * la position au lieu de l'appliquer. Aucune formule n'est donc recopiée ailleurs : le rattrapage
     * visuel du cavalier (cf. {@code OWRiderSmoothing}) s'appuie sur la même arithmétique que le
     * placement réel, et suit automatiquement toute espèce ajoutée par la suite.</p>
     *
     * <p>{@code null} si l'espèce refuse de placer ce passager — chunk non chargé, passager qui n'est
     * plus à bord : il n'y a alors rien à corriger.</p>
     */
    public Vec3 captureSeatPosition(Entity passenger) {
        final double[] pos = new double[3];
        final boolean[] placed = { false };
        this.positionRider(passenger, (e, x, y, z) -> {
            pos[0] = x; pos[1] = y; pos[2] = z; placed[0] = true;
        });
        return placed[0] ? new Vec3(pos[0], pos[1], pos[2]) : null;
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction function) {
        if (!this.hasPassenger(passenger) || this.touchingUnloadedChunk()) return;
        double riderY = this.getY() + getBaseRiderYOffset() + getRiderAnimYOffset();
        passenger.fallDistance = 0f;
        function.accept(passenger, this.getX(), riderY, this.getZ());
    }

    protected double getBaseRiderYOffset() {
        return this.getBbHeight() * 0.75;
    }

    protected float getRiderAnimYOffset() {
        return 0f;
    }

    @javax.annotation.Nullable
    public LivingEntity getControllingPassenger() {
        if (this.hasEffect(OWEffects.FEAR_EFFECT.getDelegate())) return null;

        Entity entityRiding = this.getFirstPassenger();
        if (entityRiding != null && entityRiding instanceof LivingEntity living && living.zza == 0) {
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

        if (!stack.isEmpty() && !this.level().isClientSide()) {
            this.spawnAtLocation(stack);
        }

        // Drop générique de l'Âme : capture le snapshot complet de TOUT OWEntity apprivoisé (hors véhicules),
        // sans code dédié par type. Remplace les anciennes méthodes createSoulStack() dupliquées.
        if (!this.level().isClientSide() && shouldDropSoulOnDeath()) {
            this.spawnAtLocation(this.captureSoul());
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
        // Le leveling 1→50 provient uniquement de l'absorption d'orbes (voir absorbNearbyXpOrbs),
        // pas des kills.

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
            this.playSound(SoundEvents.CAMEL_EAT, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        if (itemstack.is(Tags.Items.DYES) && isTame()) {
            saveColor(player, hand);
            return InteractionResult.SUCCESS;
        }

        if (this.isTame() && !isBaby() && !this.level().isClientSide() && hand == InteractionHand.MAIN_HAND) {
            if (player.isSteppingCarefully() && !this.isInResurrection()) {
                if (this.isSittingToggleLocked()) return InteractionResult.PASS;

                // Propriétaire OU membre de la tribu : peut basculer assis/suivre.
                if (!this.hasTribePermission(player, net.tiew.operationWild.team.OWTribePermission.CONTROL)) return InteractionResult.PASS;
                if (this.getControllingPassenger() != null) return InteractionResult.PASS;
                if (this.sittingCooldown > 0) return InteractionResult.PASS;

                this.setSitting(!isSitting());
                this.sittingCooldown = 20;
                if (player instanceof ServerPlayer serverPlayer) {
                    if (!this.isSitting())
                        OWUtils.showMessage(serverPlayer, "tooltip.following", TextColor.fromRgb(0xFFFFFF), false);
                    else OWUtils.showMessage(serverPlayer, "tooltip.sitting", TextColor.fromRgb(0xFFFFFF), false);
                }

                return InteractionResult.SUCCESS;
            } else {
                if (!this.isSitting() && !this.isInResurrection() && !itemstack.is(Tags.Items.FOODS) && !this.isBaby()) {
                    // Monter exige d'être le propriétaire ou un membre de tribu autorisé : on ne
                    // chevauche pas la bête d'un autre dresseur.
                    if (!this.hasTribePermission(player, net.tiew.operationWild.team.OWTribePermission.CONTROL)) {
                        return InteractionResult.PASS;
                    }
                    player.startRiding(this);
                }
            }
        }

        return super.mobInteract(player, hand);
    }

    public void handleRunningEffects(int maxRunTime, SoundEvent soundEvent, float pitch, int[] runTimeSound) {
        if (this.isInWater()) return;
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
        // Cagnotte par joueur, serveur-autoritaire (voir OWTamingXp). Ne fait rien hors serveur.
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        net.tiew.operationWild.core.OWTamingXp.grantTamingXp(serverPlayer, experience);

        double total = net.tiew.operationWild.core.OWTamingXp.getTamingXp(serverPlayer);
        String advancement = selectTamingAdvancement(total);
        if (!advancement.isEmpty()) {
            serverPlayer.getServer().getCommands().performPrefixedCommand(serverPlayer.getServer().createCommandSourceStack().withSuppressedOutput(),
                    "advancement grant " + serverPlayer.getGameProfile().getName() + " only " + OperationWild.MOD_ID + ":" + advancement);
        }
    }

    private String selectTamingAdvancement(double tamingExperience) {
        if (tamingExperience >= 4000) return "legendary_adventurer";
        else if (tamingExperience >= 2700) return "professional_tamer";
        else if (tamingExperience >= 1500) return "renowned_tamer";
        else if (tamingExperience >= 300) return "novice_tamer";
        return "";
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
                levelPointsBonus = this.getHealth() < this.getMaxHealth() / 2 ? 0 : (int) ((this.getHealth() - (this.getMaxHealth() / 2)) / (this.getMaxHealth() / 10));
                if (!isBaby()) this.setLevelPoints(levelPointsBonus);
                this.entityData.set(DATA_FLAGS_ID, (byte) (b0 | 4));
                this.setTamedAttributes(this, this.getAttributeBaseValue(Attributes.MAX_HEALTH));
                this.navigation.recomputePath();
                this.setTarget(null);
                this.level().broadcastEntityEvent(this, (byte) 7);
                this.setSitting(false);
                this.setNap(false);

                this.setHealth(this.getMaxHealth());
                double pitch = OWUtils.generateRandomInterval(0.8, 1.0);
                this.playSound(OWSounds.TAME_SUCCESS.get(), 1.0f, (float) pitch);
                this.playSound(SoundEvents.TOTEM_USE);
                this.level().broadcastEntityEvent(this, (byte) 8);
                this.setOwnerUUID(player.getUUID());
                if (player != null) {
                    setCachedOwnerName(player.getName().getString());
                }
                this.setDamageToClient(this.getDamage());
                this.setCurrentMode(Mode.Passive);
                this.setPassive(true);
                int defaultSkin = this.getDefaultSkinIndex();
                if (this.getSkinIndex() == 0 && defaultSkin > 0) this.changeSkin(defaultSkin, false);

                if (player instanceof ServerPlayer serverPlayer) {
                    AdvancementHolder advancement = player.getServer().getAdvancements().get(this.getTamingAdvancement());
                    if (advancement != null) {
                        serverPlayer.getAdvancements().award(advancement, "tamed_" + this.getClass().getSimpleName().toLowerCase().split("entity")[0]);
                    }
                }

                if (this.getOwner() != null && !this.skipNameSelection) {
                    boolean isOwnerNearby = this.getOwner().distanceTo(this) <= 20;

                    if (isOwnerNearby) {
                        OWNetworkHandler.sendToClient(new OpenChooseNameScreen(this.getId()), (ServerPlayer) player);
                    } else this.setNickname(String.valueOf(Component.translatable("entity.ow." + this.getClass().getSimpleName().toLowerCase().split("entity")[0])));
                }
            } else {
                this.entityData.set(DATA_FLAGS_ID, (byte) (b0 & -5));
            }
        }
    }

    public static void addExperienceCommand(OWEntity entity, int amount) {
        entity.gainLevelXp(amount);
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
    public void hurtAfterCombo(LivingEntity entity, int comboAttack) {

    }

    protected void onSuccessfulHit(LivingEntity entity) {

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

    protected boolean isLeapingVehicle() {
        return false;
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

    @Override
    public float getRotationSpeed() {
        return 0;
    }

    public enum Mode {
        Passive,
        Aggressive;
    }
    public Mode currentMode;
    /**
     * Le champ est transitoire : il repart à {@code null} au moindre rechargement de chunk ou
     * changement de dimension, alors que l'indicateur passif/agressif, lui, est bien sauvegardé et
     * répliqué. On le redéduit donc de celui-ci plutôt que de laisser la créature sans mode — une
     * créature « sans mode » ne prend jamais l'initiative du combat, quoi qu'affiche son état.
     */
    public Mode getCurrentMode() {
        if (currentMode == null) currentMode = this.isPassive() ? Mode.Passive : Mode.Aggressive;
        return currentMode;
    }
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
        if (!(this instanceof SeaBugEntity)) {

            boolean isBoa = this instanceof BoaEntity;
            float[] scales = new float[] {0.95f, 1.05f};
            float[] boaScales = new float[] {0.9f, 1.15f};

            this.setRandomScale(this.averageScale, isBoa ? boaScales[0] : scales[0], isBoa ? boaScales[1] : scales[1]);
        } else {
            this.setScale(1.0f);
        }
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

    /**
     * Alliance commune a toutes les entites du mod. Les overrides par espece gerent
     * l'alliance intra-espece puis appellent super (= cette methode).
     *
     * Ajouts centraux :
     *   - Un segment de queue de Boa (BoaTailPart) redirige ses degats vers le Boa parent :
     *     on raisonne donc sur le Boa parent (sinon les allies peuvent frapper la queue).
     *   - Deux animaux apprivoises du meme proprietaire sont allies, toutes especes confondues.
     */
    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof BoaTailPart tailPart) {
            Entity parent = tailPart.getParent();
            if (parent == this) return true;                         // sa propre queue
            if (parent instanceof LivingEntity parentLiving) return this.isAlliedTo(parentLiving);
        }

        if (this.isTame() && this.getOwnerUUID() != null
                && entity instanceof TamableAnimal tamable
                && this.getOwnerUUID().equals(tamable.getOwnerUUID())) {
            return true;
        }

        // Alliance de tribu : deux entités d'une même tribu ne se ciblent/blessent pas...
        OWTeam myTeam = this.resolvedTeam();
        if (myTeam != null) {
            if (entity instanceof OWEntity other) {
                OWTeam otherTeam = other.resolvedTeam();
                if (otherTeam != null && otherTeam.getTeamId() == myTeam.getTeamId()) return true;
            }
            // ...et les membres joueurs de la tribu ne sont pas des cibles.
            if (entity instanceof Player player && myTeam.isMember(player.getUUID())) {
                return true;
            }
        }

        return super.isAlliedTo(entity);
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

        this.lastAttackHitEntities.clear();

        UUID ownerUUID = null;
        if (this instanceof TamableAnimal tamable) {
            ownerUUID = tamable.getOwnerUUID();
        }

        int entitiesProcessed = 0;
        for (Entity entity : entitiesInRange) {
            if (entity instanceof LivingEntity livingEntity) {
                entitiesProcessed++;

                if (ownerUUID != null && entity instanceof Player player && player.getUUID().equals(ownerUUID)) {
                    continue;
                }

                if (this.isAlliedTo(livingEntity)) {
                    continue;
                }

                if (ownerUUID != null && entity instanceof TamableAnimal otherTamable && otherTamable.getOwnerUUID() != null && otherTamable.getOwnerUUID().equals(ownerUUID)) {
                    continue;
                }

                if (livingEntity instanceof Player player && player.getVehicle() != null) {
                    continue;
                }

                if (this instanceof BoaEntity) {
                    if (livingEntity instanceof BoaTailPart boaTail) {
                        continue;
                    }
                }

                if (this.isAssassin() && OWUtils.RANDOM(10)) {

                    if (!this.level().isClientSide()) {
                        boolean hurtResult = livingEntity.hurt(this.damageSources().mobAttack(this), attackDamage *= 1.25f);
                        if (hurtResult && !(livingEntity instanceof Player p && p.isCreative())) {
                            this.onSuccessfulHit(livingEntity);
                            this.lastAttackHitEntities.add(livingEntity);
                        }
                    }

                    if ($$1 > 0) {
                        if (livingEntity instanceof Player player && player.isCreative()) {
                            return;
                        }
                        Vec3 knockbackDirection = livingEntity.position().subtract(this.position()).normalize();
                        Vec3 knockback = knockbackDirection.scale($$1 * 0.5);
                        livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(knockback.x, knockback.y * 0.3, knockback.z));
                    }

                    OWUtils.spawnParticles(livingEntity, ParticleTypes.CRIT, 0, 0.5, 0, 20, 3);
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.NEUTRAL, 1.0F, pitch);
                } else {

                    float finalDamage = attackDamage;

                    boolean heartShot = false;
                    if (this instanceof BoaEntity boaHeart
                            && this.getControllingPassenger() instanceof Player heartRider
                            && boaHeart.isThermalHeartTarget(livingEntity)
                            && boaHeart.isLookingAtHeart(heartRider, livingEntity)) {
                        finalDamage *= net.tiew.operationWild.entity.attacks.OWAttacksConstants.Boa.THERMAL_HEART_MULT;
                        heartShot = true;
                    }

                    if (!this.level().isClientSide()) {
                        boolean hurtResult = livingEntity.hurt(this.damageSources().mobAttack(this), finalDamage);
                        if (hurtResult && !(livingEntity instanceof Player p && p.isCreative())) {
                            this.onSuccessfulHit(livingEntity);
                            this.lastAttackHitEntities.add(livingEntity);
                            if (heartShot && this instanceof BoaEntity boaBleed) {
                                boaBleed.spawnHeartBleed(livingEntity);
                                if (this.getControllingPassenger() instanceof net.minecraft.server.level.ServerPlayer sp) {
                                    net.tiew.operationWild.networking.OWNetworkHandler.sendToClient(
                                            new net.tiew.operationWild.networking.packets.to_client.HeartShotPacket(livingEntity.getId()), sp);
                                }
                            }
                        }
                    }

                    this.hurtAfterCombo(livingEntity, this.getComboAttack());

                    if ($$1 > 0) {
                        if (livingEntity instanceof Player player && player.isCreative()) {
                            return;
                        }
                        Vec3 knockbackDirection = livingEntity.position().subtract(this.position()).normalize();
                        Vec3 knockback = knockbackDirection.scale($$1 * 0.4);
                        livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(knockback.x, knockback.y * 0.3, knockback.z));
                    }

                    if (this.isTame() && ownerIsRiding()) {
                        this.setFighting(true);
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
                if (data.shouldPlay) {
                    data.animationState.stop();
                    data.timer = 0;
                }
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

    public String getCachedOwnerName() { return this.entityData.get(CACHED_OWNER_NAME); }
    public void setCachedOwnerName(String name) {
        if (name != null && !name.isEmpty()) this.entityData.set(CACHED_OWNER_NAME, name);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, 0);
        builder.define(STATE, 0);
        builder.define(XP, 0f);
        builder.define(STAGE, 0);
        builder.define(LEVEL, 0);
        builder.define(LEVEL_POINTS, 0);
        builder.define(PISTE_NODE, 0);
        builder.define(PISTE_UNLOCKED, "0");
        builder.define(PISTE_LOCKED, "");
        builder.define(PISTE_CHOICES, "");
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
        builder.define(BODY_Y_ROT, 0.0f);
        builder.define(BODY_Y_OFFSET, 0.0f);
        builder.define(IS_FALLING, false);
        builder.define(IS_FED, false);
        builder.define(ITEM_FOOD, ItemStack.EMPTY);
        builder.define(FOOD_COUNT, 0);
        builder.define(AUTO_PICKUP, true);
        builder.define(SHOW_TRIBE_FLAG, true);
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
        builder.define(NAME, "");
        builder.define(SKIN_INDEX, 0);
        builder.define(SKINS_UNLOCKED, "");
        builder.define(COSMETIC_QUEST_KILLS, 0);
        builder.define(FIGHT_COOLDOWN, 0);
        builder.define(CACHED_OWNER_NAME, "");
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
        tag.putInt("Level", this.getLevel());
        tag.putInt("getNecklaceColor", this.getNecklaceColor());
        tag.putInt("LevelPoints", this.getLevelPoints());
        tag.putInt("PisteNode", this.getPisteCurrentNode());
        tag.putString("PisteUnlocked", this.entityData.get(PISTE_UNLOCKED));
        tag.putString("PisteLocked", this.entityData.get(PISTE_LOCKED));
        tag.putString("PisteChoices", this.entityData.get(PISTE_CHOICES));
        tag.putFloat("Scale", this.getScale());
        tag.putBoolean("isPassive", this.isPassive());
        tag.putBoolean("autoPickup", this.isAutoPickup());
        tag.putBoolean("showTribeFlag", this.isShowTribeFlag());
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
        tag.putString("getNickname", this.getNickname());

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
        tag.putLong("lastQuestResetDay", this.lastQuestResetDay);
        tag.putInt("activeQuest0", this.activeQuest0);
        tag.putInt("activeQuest1", this.activeQuest1);
        tag.putInt("activeQuest2", this.activeQuest2);
        tag.putInt("questReward0", this.questReward0);
        tag.putInt("questReward1", this.questReward1);
        tag.putInt("questReward2", this.questReward2);
        tag.putBoolean("dailyRerollAvailable", this.dailyRerollAvailable);

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
        tag.putInt("skinIndex", this.getSkinIndex());
        tag.putString("SkinsUnlocked", this.entityData.get(SKINS_UNLOCKED));
        tag.putInt("CosmeticQuestKills", this.entityData.get(COSMETIC_QUEST_KILLS));

        tag.putString("cachedOwnerName", this.getCachedOwnerName());

        // Refonte player-centric : la tribu n'est plus persistée sur l'entité. Elle est portée par
        // le registre serveur (OWTribesSavedData) et dérivée à l'exécution du propriétaire de l'entité.
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
        this.entityData.set(LEVEL, tag.getInt("Level"));
        this.entityData.set(NECKLACE_COLOR, tag.getInt("getNecklaceColor"));
        this.entityData.set(LEVEL_POINTS, tag.getInt("LevelPoints"));
        if (DEBUG_RESET_PISTE) {
            // Debug/test : remet à zéro la progression de la Piste de chaque entité au chargement.
            this.entityData.set(PISTE_NODE, 0);
            this.entityData.set(PISTE_UNLOCKED, "0");
            this.entityData.set(PISTE_LOCKED, "");
            this.entityData.set(PISTE_CHOICES, "");
        } else {
            this.entityData.set(PISTE_NODE, tag.getInt("PisteNode"));
            this.entityData.set(PISTE_UNLOCKED, tag.contains("PisteUnlocked") ? tag.getString("PisteUnlocked") : "0");
            this.entityData.set(PISTE_LOCKED, tag.getString("PisteLocked"));
            this.entityData.set(PISTE_CHOICES, tag.getString("PisteChoices"));
        }
        this.entityData.set(SCALE, tag.getFloat("Scale"));
        this.entityData.set(IS_PASSIVE, tag.getBoolean("isPassive"));
        this.entityData.set(AUTO_PICKUP, tag.contains("autoPickup") ? tag.getBoolean("autoPickup") : true);
        // Absent des entites d'avant la feature : le drapeau est porte par defaut.
        this.entityData.set(SHOW_TRIBE_FLAG, !tag.contains("showTribeFlag") || tag.getBoolean("showTribeFlag"));
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
        this.entityData.set(NAME, tag.getString("getNickname"));

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
        this.lastQuestResetDay = tag.contains("lastQuestResetDay") ? tag.getLong("lastQuestResetDay") : Long.MIN_VALUE;
        this.activeQuest0 = tag.contains("activeQuest0") ? tag.getInt("activeQuest0") : -1;
        this.activeQuest1 = tag.contains("activeQuest1") ? tag.getInt("activeQuest1") : -1;
        this.activeQuest2 = tag.contains("activeQuest2") ? tag.getInt("activeQuest2") : -1;
        this.questReward0 = tag.getInt("questReward0");
        this.questReward1 = tag.getInt("questReward1");
        this.questReward2 = tag.getInt("questReward2");
        this.dailyRerollAvailable = !tag.contains("dailyRerollAvailable") || tag.getBoolean("dailyRerollAvailable");

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
        this.entityData.set(SKIN_INDEX, tag.getInt("skinIndex"));
        this.entityData.set(SKINS_UNLOCKED, tag.contains("SkinsUnlocked") ? tag.getString("SkinsUnlocked") : "");
        this.entityData.set(COSMETIC_QUEST_KILLS, tag.getInt("CosmeticQuestKills"));

        this.setCachedOwnerName(tag.getString("cachedOwnerName"));

        // Refonte player-centric : la tribu n'est plus lue depuis l'entité (les anciennes tribus
        // hébergées par entité sont volontairement abandonnées). currentTeam est résolu au tick
        // serveur depuis OWTribesSavedData via le propriétaire (cf. resolveTeamFromOwner).
    }

    /**
     * Résout {@code currentTeam} depuis le registre serveur en fonction du propriétaire de l'entité,
     * une seule fois après le chargement. Les changements ultérieurs de tribu sont poussés par
     * {@link net.tiew.operationWild.team.OWTribeManager}.
     */
    private boolean teamResolvedFromOwner = false;

    public void resolveTeamFromOwnerIfNeeded() {
        if (teamResolvedFromOwner) return;
        if (this.level().isClientSide) return;
        net.minecraft.server.MinecraftServer server = this.level().getServer();
        if (server == null) return;
        // On attend de connaître le propriétaire (apprivoisement) avant de figer la résolution,
        // afin qu'une entité apprivoisée après coup hérite bien de la tribu de son maître.
        if (this.getOwnerUUID() == null) return;
        teamResolvedFromOwner = true;
        net.tiew.operationWild.team.OWTribeManager.refreshEntityTeam(server, this);
    }
}
