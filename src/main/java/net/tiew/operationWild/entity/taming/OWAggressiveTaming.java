package net.tiew.operationWild.entity.taming;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.particle.OWParticles;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OWAggressiveTaming {

    public static final int FLEE_THRESHOLD = 90;
    public static final int MAX_BONUS_POINTS = 5;
    public static final int DIZZY_STAR_RING = 3;
    public static final int DIZZY_STAR_LIFETIME = 60;
    public static final int DIZZY_STAR_INTERVAL = DIZZY_STAR_LIFETIME / DIZZY_STAR_RING;

    private static final int MEAL_INTERVAL_MIN = 200;
    private static final int MEAL_INTERVAL_MAX = 800;
    private static final int NOTICE_DELAY_MIN = 15;
    private static final int NOTICE_DELAY_MAX = 40;
    private static final int MEAL_DURATION = 60;
    private static final int MEAL_BITE_INTERVAL = 9;
    private static final int MEAL_RETRY_DELAY = 40;
    private static final int TAME_REVEAL_DELAY = 40;
    private static final int COLLAPSE_DURATION = 18;
    private static final double COLLAPSE_MAX_SPEED = 0.45;
    private static final double COLLAPSE_MIN_SPEED = 0.02;
    private static final double MEAL_REACH = 1.6;
    private static final double MEAL_VERTICAL_REACH = 1.5;
    private static final float SOMNOLENCE_FOOD_RESTORE = 0.15f;
    private static final int SOMNOLENCE_FOOD_REFUSE_PERCENT = 85;
    private static final int SOMNOLENCE_FOOD_URGENT_PERCENT = 35;
    private static final float SOMNOLENCE_GAIN_RATE = 0.1f;
    private static final double THREAT_RADIUS = 32.0;
    private static final double SEDATOR_MEMORY_RADIUS = 48.0;
    private static final double FEEDER_NOTIFY_RADIUS = 64.0;
    private static final double OWNER_FALLBACK_RADIUS = 24.0;

    private static final int RED = 0xFF6B6B;

    private final OWEntity entity;
    private final Map<UUID, Integer> feeders = new LinkedHashMap<>();
    @Nullable
    private UUID lastFeeder;
    @Nullable
    private UUID sedator;
    @Nullable
    private ItemEntity mealItem;
    private int mealCooldown;
    private int noticeDelay;
    private int mealTicks;
    private int collapseTicks;
    private int tameRevealTicks;
    private Vec3 collapseVelocity = Vec3.ZERO;
    private float pendingSomnolence;
    private boolean knockedOutLastTick;
    private boolean primed;

    public OWAggressiveTaming(OWEntity entity) {
        this.entity = entity;
    }

    public static boolean isSomnolenceFood(ItemStack stack) {
        return !stack.isEmpty() && stack.is(OWTags.Items.SOMNOLENCE_FOOD);
    }

    public int getBonusPoints() {
        return Math.max(0, MAX_BONUS_POINTS - entity.getTamingHits());
    }

    public boolean isCollapsing() {
        return collapseTicks > 0;
    }

    public void onSedated(DamageSource source) {
        Entity culprit = source.getEntity();
        if (culprit != null) sedator = culprit.getUUID();
    }

    @Nullable
    public Entity getThreat() {
        if (!(entity.level() instanceof ServerLevel level)) return null;
        if (sedator != null) {
            Entity culprit = level.getEntity(sedator);
            if (culprit != null && culprit.isAlive()
                    && culprit.distanceToSqr(entity) <= SEDATOR_MEMORY_RADIUS * SEDATOR_MEMORY_RADIUS) {
                return culprit;
            }
        }
        return level.getNearestPlayer(entity, THREAT_RADIUS);
    }

    public void knockOut() {
        Vec3 motion = new Vec3(entity.getX() - entity.xo, 0.0, entity.getZ() - entity.zo);
        double speed = motion.horizontalDistance();
        if (speed < COLLAPSE_MIN_SPEED) motion = Vec3.ZERO;
        else if (speed > COLLAPSE_MAX_SPEED) motion = motion.scale(COLLAPSE_MAX_SPEED / speed);
        collapseVelocity = motion;
        collapseTicks = COLLAPSE_DURATION;
        entity.setDeltaMovement(motion.x, Math.min(entity.getDeltaMovement().y, 0.0), motion.z);
        entity.hasImpulse = true;

        entity.getNavigation().stop();
        stopRunningGoals();
        entity.setTarget(null);
        entity.setRunning(false);
        entity.resetState();
        entity.setNap(false);
        entity.setSitting(false);
        entity.onSedationKnockOut();
        entity.ejectPassengers();

        if (entity.foodWanted <= 0) entity.foodWanted = 6 + entity.getRandom().nextInt(6);

        feeders.clear();
        lastFeeder = null;
        pendingSomnolence = 0f;
        entity.setTamingHits(0);
        cancelMeal();
        scheduleMeal(rollMealInterval());
        knockedOutLastTick = true;
        primed = true;

        entity.playSedationVoice(0.55f);
    }

    public void onHit(DamageSource source) {
        int hits = entity.getTamingHits();
        if (hits < MAX_BONUS_POINTS) entity.setTamingHits(hits + 1);
        sendHeadParticles(ParticleTypes.ANGRY_VILLAGER, 3, 0.25);
        entity.playSound(SoundEvents.ITEM_BREAK, 0.7f, 0.6f);
        if (source.getEntity() instanceof ServerPlayer player) {
            player.displayClientMessage(Component.translatable("taming.ow.bonus_lost", getBonusPoints(), MAX_BONUS_POINTS)
                    .withStyle(style -> style.withColor(RED)), true);
        }
    }

    public void tick() {
        boolean knockedOut = entity.isKnockedOut();
        if (!primed) {
            primed = true;
            knockedOutLastTick = knockedOut;
        }
        if (knockedOutLastTick && !knockedOut) onWakeUp();
        knockedOutLastTick = knockedOut;
        if (!knockedOut) return;

        tickCollapse();
        tickFloating();
        tickSomnolenceGain();
        tickMeal();
    }

    private void tickCollapse() {
        if (collapseTicks <= 0) return;
        collapseTicks--;
        double remaining = collapseTicks / (double) COLLAPSE_DURATION;
        double ease = remaining * remaining * remaining;
        Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(collapseVelocity.x * ease, motion.y, collapseVelocity.z * ease);
        if (collapseTicks == 0) landImpact();
    }

    private void landImpact() {
        if (!(entity.level() instanceof ServerLevel level)) return;
        boolean heavy = entity.getBbWidth() >= 1.5f;
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                heavy ? SoundEvents.GENERIC_BIG_FALL : SoundEvents.GENERIC_SMALL_FALL,
                SoundSource.NEUTRAL, 0.9f, 0.75f);
        if (!entity.onGround()) return;
        BlockState ground = entity.getBlockStateOn();
        if (ground.isAir()) return;
        double spread = entity.getBbWidth() * 0.4;
        int count = 8 + (int) (entity.getBbWidth() * 10);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                entity.getX(), entity.getY() + 0.1, entity.getZ(), count, spread, 0.05, spread, 0.15);
    }

    private void tickFloating() {
        if (!entity.isInWater()) return;
        if (entity.getFluidHeight(FluidTags.WATER) <= entity.getBbHeight() * 0.55) return;
        Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(motion.x, Math.min(Math.max(motion.y, 0.0) + 0.03, 0.08), motion.z);
    }

    private void tickSomnolenceGain() {
        if (pendingSomnolence <= 0f) return;
        float step = Math.min(pendingSomnolence, Math.max(1f, pendingSomnolence * SOMNOLENCE_GAIN_RATE));
        pendingSomnolence -= step;
        int gained = entity.getActualSleepingBar() + Math.round(step);
        entity.setActualSleepingBarTo(Math.min(entity.getMaxSleepingBar(), gained));
    }

    private void tickMeal() {
        if (mealTicks > 0) {
            tickEating();
            return;
        }
        if (entity.foodWanted > 0 && entity.foodGiven >= entity.foodWanted) {
            holdAsleep();
            if (tameRevealTicks > 0) {
                if (--tameRevealTicks == 0) completeTaming();
            } else if ((entity.tickCount + entity.getId()) % 20 == 0) {
                completeTaming();
            }
            return;
        }
        if (mealCooldown > 0) {
            mealCooldown--;
            return;
        }
        if (noticeDelay > 0) {
            if (!canEat(mealItem)) {
                mealItem = null;
                noticeDelay = 0;
                return;
            }
            if (--noticeDelay == 0) startMeal(mealItem);
            return;
        }
        if ((entity.tickCount + entity.getId()) % 5 != 0) return;
        ItemEntity food = findMeal();
        if (food == null) return;
        mealItem = food;
        noticeDelay = NOTICE_DELAY_MIN + entity.getRandom().nextInt(NOTICE_DELAY_MAX - NOTICE_DELAY_MIN + 1);
    }

    @Nullable
    private ItemEntity findMeal() {
        Vec3 mouth = mouthPosition();
        double reach = mealReach();
        AABB zone = new AABB(mouth, mouth).inflate(reach, MEAL_VERTICAL_REACH, reach);
        List<ItemEntity> items = entity.level().getEntitiesOfClass(ItemEntity.class, zone, this::canEat);
        if (items.isEmpty()) return null;
        boolean urgent = entity.getSleepBarPercent() < SOMNOLENCE_FOOD_URGENT_PERCENT;
        return items.stream()
                .min(Comparator.comparingInt((ItemEntity item) -> mealPriority(item.getItem(), urgent))
                        .thenComparingDouble(item -> item.distanceToSqr(mouth)))
                .orElse(null);
    }

    private int mealPriority(ItemStack stack, boolean urgent) {
        if (entity.isTamingFood(stack)) return urgent ? 1 : 0;
        return urgent ? 0 : 1;
    }

    private boolean canEat(@Nullable ItemEntity item) {
        if (item == null || !item.isAlive()) return false;
        ItemStack stack = item.getItem();
        if (stack.isEmpty() || !wants(stack)) return false;
        Vec3 mouth = mouthPosition();
        double reach = mealReach();
        double dx = item.getX() - mouth.x;
        double dz = item.getZ() - mouth.z;
        return dx * dx + dz * dz <= reach * reach && Math.abs(item.getY() - mouth.y) <= MEAL_VERTICAL_REACH;
    }

    private boolean wants(ItemStack stack) {
        if (entity.isTamingFood(stack)) return true;
        return isSomnolenceFood(stack) && entity.getSleepBarPercent() < SOMNOLENCE_FOOD_REFUSE_PERCENT;
    }

    private void startMeal(ItemEntity item) {
        mealItem = item;
        mealTicks = MEAL_DURATION;
        item.setPickUpDelay(MEAL_DURATION + 20);
        entity.setTamingMeal(item.getItem().copyWithCount(1));
    }

    private void tickEating() {
        ItemEntity item = mealItem;
        if (item == null || !item.isAlive() || item.getItem().isEmpty()) {
            cancelMeal();
            scheduleMeal(MEAL_RETRY_DELAY);
            return;
        }
        int elapsed = MEAL_DURATION - mealTicks;
        mealTicks--;
        if (elapsed % MEAL_BITE_INTERVAL == MEAL_BITE_INTERVAL / 2) bite(item);
        if (mealTicks == 0) finishMeal(item);
    }

    private void bite(ItemEntity item) {
        if (entity.level() instanceof ServerLevel level) {
            level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, item.getItem().copyWithCount(1)),
                    item.getX(), item.getY() + 0.15, item.getZ(), 6, 0.12, 0.08, 0.12, 0.06);
        }
        entity.playSound(SoundEvents.GENERIC_EAT, 0.7f, 0.8f + entity.getRandom().nextFloat() * 0.4f);
    }

    private void finishMeal(ItemEntity item) {
        ItemStack stack = item.getItem();
        ItemStack eaten = stack.copyWithCount(1);
        ItemStack rest = stack.copy();
        rest.shrink(1);
        Entity thrower = item.getOwner();
        if (rest.isEmpty()) {
            item.discard();
        } else {
            item.setItem(rest);
            item.setPickUpDelay(10);
        }
        mealItem = null;
        entity.setTamingMeal(ItemStack.EMPTY);

        if (entity.level() instanceof ServerLevel level) {
            level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, eaten),
                    item.getX(), item.getY() + 0.2, item.getZ(), 12, 0.15, 0.1, 0.15, 0.08);
        }
        entity.playSound(SoundEvents.PLAYER_BURP, 0.6f, 0.65f + entity.getRandom().nextFloat() * 0.2f);

        if (!entity.isTamingFood(eaten)) {
            pendingSomnolence += entity.getMaxSleepingBar() * SOMNOLENCE_FOOD_RESTORE;
            sendHeadParticles(OWParticles.NAP_PARTICLES.get(), 3, 0.2);
            sendHeadParticles(ParticleTypes.WITCH, 8, 0.35);
            scheduleMeal(rollMealInterval());
            return;
        }

        entity.foodGiven = Math.min(entity.foodWanted, entity.foodGiven + 1);
        if (thrower instanceof Player player) {
            feeders.merge(player.getUUID(), 1, Integer::sum);
            lastFeeder = player.getUUID();
        }
        int wanted = Math.max(1, entity.foodWanted);
        entity.setTamingPercentage(entity.foodGiven, wanted);
        float progress = entity.foodGiven / (float) wanted;
        entity.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0f, 0.7f + progress * 0.9f);
        sendHeadParticles(ParticleTypes.HAPPY_VILLAGER, 6 + (int) (progress * 8), 0.4);

        if (entity.foodGiven >= entity.foodWanted) {
            tameRevealTicks = TAME_REVEAL_DELAY;
            return;
        }
        scheduleMeal(rollMealInterval());
    }

    private void holdAsleep() {
        if (entity.getActualSleepingBar() < 2) entity.setActualSleepingBarTo(2);
    }

    private void completeTaming() {
        ServerPlayer owner = resolveOwner();
        if (owner == null || EventHooks.onAnimalTame(entity, owner)) return;

        int bonus = getBonusPoints();
        knockedOutLastTick = false;
        entity.setTame(true, owner);
        entity.setSleeping(false);
        entity.resetSleepBar();
        entity.foodGiven = entity.foodWanted;
        if (!entity.isBaby()) entity.setLevelPoints(bonus);
        clearState();
    }

    @Nullable
    private ServerPlayer resolveOwner() {
        MinecraftServer server = entity.getServer();
        if (server == null) return null;
        List<Map.Entry<UUID, Integer>> ranking = new ArrayList<>(feeders.entrySet());
        ranking.sort((a, b) -> {
            int byMeals = Integer.compare(b.getValue(), a.getValue());
            if (byMeals != 0) return byMeals;
            if (a.getKey().equals(lastFeeder)) return -1;
            if (b.getKey().equals(lastFeeder)) return 1;
            return 0;
        });
        for (Map.Entry<UUID, Integer> entry : ranking) {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player != null && !player.isSpectator()) return player;
        }
        return entity.level().getNearestPlayer(entity, OWNER_FALLBACK_RADIUS) instanceof ServerPlayer nearest ? nearest : null;
    }

    private void onWakeUp() {
        if (entity.isTame() || !entity.isAlive()) {
            clearState();
            return;
        }
        if (entity.foodGiven > 0) {
            notifyTamers(Component.translatable("taming.ow.woke_up", entity.getType().getDescription())
                    .withStyle(style -> style.withColor(RED)));
        }
        entity.foodGiven = 0;
        entity.setTamingPercentage(0, Math.max(1, entity.foodWanted));
        clearState();
        sendHeadParticles(ParticleTypes.CLOUD, 10, 0.3);
        entity.playSedationVoice(1.0f);
    }

    private void notifyTamers(Component message) {
        MinecraftServer server = entity.getServer();
        if (server == null) return;
        List<UUID> tamers = new ArrayList<>(feeders.keySet());
        if (sedator != null && !tamers.contains(sedator)) tamers.add(sedator);
        for (UUID id : tamers) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null && player.level() == entity.level()
                    && player.distanceToSqr(entity) <= FEEDER_NOTIFY_RADIUS * FEEDER_NOTIFY_RADIUS) {
                player.displayClientMessage(message, true);
            }
        }
    }

    private void clearState() {
        feeders.clear();
        lastFeeder = null;
        sedator = null;
        cancelMeal();
        mealCooldown = 0;
        collapseTicks = 0;
        tameRevealTicks = 0;
        collapseVelocity = Vec3.ZERO;
        pendingSomnolence = 0f;
        entity.setTamingHits(0);
    }

    private void cancelMeal() {
        mealItem = null;
        mealTicks = 0;
        noticeDelay = 0;
        entity.setTamingMeal(ItemStack.EMPTY);
    }

    private void scheduleMeal(int ticks) {
        mealCooldown = ticks;
        noticeDelay = 0;
    }

    private int rollMealInterval() {
        return MEAL_INTERVAL_MIN + entity.getRandom().nextInt(MEAL_INTERVAL_MAX - MEAL_INTERVAL_MIN + 1);
    }

    private void stopRunningGoals() {
        for (WrappedGoal goal : entity.goalSelector.getAvailableGoals()) {
            if (goal.isRunning()) goal.stop();
        }
        for (WrappedGoal goal : entity.targetSelector.getAvailableGoals()) {
            if (goal.isRunning()) goal.stop();
        }
    }

    private void sendHeadParticles(ParticleOptions particle, int count, double spread) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        Vec3 head = entity.getSedationHeadPosition();
        level.sendParticles(particle, head.x, head.y, head.z, count, spread, spread * 0.6, spread, 0.02);
    }

    private Vec3 mouthPosition() {
        double yaw = Math.toRadians(entity.yBodyRot);
        double forward = entity.napParticleForward();
        return new Vec3(entity.getX() - Math.sin(yaw) * forward, entity.getY() + 0.25, entity.getZ() + Math.cos(yaw) * forward);
    }

    private double mealReach() {
        return MEAL_REACH + entity.getBbWidth() * 0.25;
    }

    public void save(CompoundTag tag) {
        CompoundTag data = new CompoundTag();
        ListTag list = new ListTag();
        for (Map.Entry<UUID, Integer> entry : feeders.entrySet()) {
            CompoundTag feeder = new CompoundTag();
            feeder.putUUID("Id", entry.getKey());
            feeder.putInt("Meals", entry.getValue());
            list.add(feeder);
        }
        data.put("Feeders", list);
        if (lastFeeder != null) data.putUUID("LastFeeder", lastFeeder);
        if (sedator != null) data.putUUID("Sedator", sedator);
        data.putInt("Hits", entity.getTamingHits());
        data.putInt("MealCooldown", mealCooldown);
        data.putFloat("PendingSomnolence", pendingSomnolence);
        tag.put("AggressiveTaming", data);
    }

    public void load(CompoundTag tag) {
        feeders.clear();
        lastFeeder = null;
        sedator = null;
        primed = false;
        if (!tag.contains("AggressiveTaming", Tag.TAG_COMPOUND)) return;
        CompoundTag data = tag.getCompound("AggressiveTaming");
        ListTag list = data.getList("Feeders", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag feeder = list.getCompound(i);
            if (feeder.hasUUID("Id")) feeders.put(feeder.getUUID("Id"), feeder.getInt("Meals"));
        }
        if (data.hasUUID("LastFeeder")) lastFeeder = data.getUUID("LastFeeder");
        if (data.hasUUID("Sedator")) sedator = data.getUUID("Sedator");
        entity.setTamingHits(data.getInt("Hits"));
        mealCooldown = data.getInt("MealCooldown");
        pendingSomnolence = data.getFloat("PendingSomnolence");
    }
}
