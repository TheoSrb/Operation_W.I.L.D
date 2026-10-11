package net.tiew.operationWild.entity.breakout;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.tiew.operationWild.OperationWild;

import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class OWBlockDamageTracker {

    private static final int SWEEP_INTERVAL = 10;
    private static final int REGEN_DELAY = 120;
    private static final float REGEN_PER_SECOND = 0.06f;
    private static final int CRACK_STAGES = 10;
    private static final Map<ServerLevel, OWBlockDamageTracker> TRACKERS = new WeakHashMap<>();
    private static int nextCrackId = -1;

    private final ServerLevel level;
    private final Long2ObjectMap<Wound> wounds = new Long2ObjectOpenHashMap<>();

    private OWBlockDamageTracker(ServerLevel level) {
        this.level = level;
    }

    public static OWBlockDamageTracker of(ServerLevel level) {
        return TRACKERS.computeIfAbsent(level, OWBlockDamageTracker::new);
    }

    public float remainingHealth(BlockPos pos, BlockState state) {
        Wound wound = wounds.get(pos.asLong());
        if (wound == null || wound.state != state) return OWBlockToughness.healthOf(state, level, pos);
        return Math.max(0f, wound.health - wound.damage);
    }

    public float progress(BlockPos pos) {
        Wound wound = wounds.get(pos.asLong());
        return wound == null ? 0f : wound.damage / wound.health;
    }

    public boolean damage(BlockPos pos, BlockState state, float amount) {
        long key = pos.asLong();
        Wound wound = wounds.get(key);
        if (wound == null || wound.state != state) {
            if (wound != null) clearCrack(wound);
            wound = new Wound(pos.immutable(), state, OWBlockToughness.healthOf(state, level, pos), takeCrackId());
            wounds.put(key, wound);
        }
        wound.damage = Math.min(wound.health, wound.damage + amount);
        wound.lastHit = level.getGameTime();
        if (wound.damage >= wound.health) {
            wounds.remove(key);
            clearCrack(wound);
            return true;
        }
        refreshCrack(wound);
        return false;
    }

    public void forget(BlockPos pos) {
        Wound wound = wounds.remove(pos.asLong());
        if (wound != null) clearCrack(wound);
    }

    private void sweep() {
        if (wounds.isEmpty() || level.getGameTime() % SWEEP_INTERVAL != 0) return;
        long now = level.getGameTime();
        ObjectIterator<Long2ObjectMap.Entry<Wound>> iterator = wounds.long2ObjectEntrySet().iterator();
        while (iterator.hasNext()) {
            Wound wound = iterator.next().getValue();
            if (!level.isLoaded(wound.pos) || level.getBlockState(wound.pos) != wound.state) {
                clearCrack(wound);
                iterator.remove();
                continue;
            }
            if (now - wound.lastHit < REGEN_DELAY) continue;
            wound.damage -= wound.health * REGEN_PER_SECOND * SWEEP_INTERVAL / 20f;
            if (wound.damage <= 0f) {
                clearCrack(wound);
                iterator.remove();
                continue;
            }
            refreshCrack(wound);
        }
    }

    private void refreshCrack(Wound wound) {
        int stage = Mth.clamp((int) (wound.damage / wound.health * CRACK_STAGES), 0, CRACK_STAGES - 1);
        if (stage == wound.stage) return;
        wound.stage = stage;
        level.destroyBlockProgress(wound.crackId, wound.pos, stage);
    }

    private void clearCrack(Wound wound) {
        level.destroyBlockProgress(wound.crackId, wound.pos, -1);
    }

    private static int takeCrackId() {
        int id = nextCrackId;
        nextCrackId = id <= Integer.MIN_VALUE + 1 ? -1 : id - 1;
        return id;
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        OWBlockDamageTracker tracker = TRACKERS.get(level);
        if (tracker != null) tracker.sweep();
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) TRACKERS.remove(level);
    }

    private static final class Wound {
        private final BlockPos pos;
        private final BlockState state;
        private final float health;
        private final int crackId;
        private float damage;
        private long lastHit;
        private int stage = -1;

        private Wound(BlockPos pos, BlockState state, float health, int crackId) {
            this.pos = pos;
            this.state = state;
            this.health = health;
            this.crackId = crackId;
        }
    }
}
