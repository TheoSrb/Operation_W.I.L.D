package net.tiew.operationWild.worldgen.biome;

import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class OWRedwoodGroves {

    public interface RawBiomes {
        Holder<Biome> at(int quartX, int quartY, int quartZ);
    }

    private static final int CELL = 640;
    private static final int JITTER = 150;
    private static final int RADIUS_MIN = 150;
    private static final int RADIUS_MAX = 240;
    private static final double WOBBLE = 0.2;
    private static final int SEED_PROBE_Y = 128;

    private static final List<ResourceKey<Biome>> SEEDS = List.of(
            Biomes.TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA);
    private static final List<ResourceKey<Biome>> HOSTS = List.of(
            Biomes.TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA,
            Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.FOREST, Biomes.FLOWER_FOREST,
            Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST, Biomes.DARK_FOREST,
            Biomes.MEADOW, Biomes.WINDSWEPT_FOREST);

    private final Holder<Biome> redwood;
    private final Set<Holder<Biome>> seeds;
    private final Set<Holder<Biome>> hosts;
    private final Map<Long, Boolean> activeCells = new ConcurrentHashMap<>();
    @Nullable
    private volatile Climate.Sampler boundSampler;
    private volatile long salt;

    private OWRedwoodGroves(Holder<Biome> redwood, Set<Holder<Biome>> seeds, Set<Holder<Biome>> hosts) {
        this.redwood = redwood;
        this.seeds = seeds;
        this.hosts = hosts;
    }

    @Nullable
    public static OWRedwoodGroves create(Set<Holder<Biome>> possibleBiomes) {
        Holder<Biome> redwood = null;
        Set<Holder<Biome>> seeds = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<Holder<Biome>> hosts = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Holder<Biome> biome : possibleBiomes) {
            if (biome.is(OWBiomes.REDWOOD_FOREST_BIOME)) redwood = biome;
            if (SEEDS.stream().anyMatch(biome::is)) seeds.add(biome);
            if (HOSTS.stream().anyMatch(biome::is)) hosts.add(biome);
        }
        return redwood == null || seeds.isEmpty() ? null : new OWRedwoodGroves(redwood, seeds, hosts);
    }

    public Holder<Biome> apply(Holder<Biome> raw, int quartX, int quartZ, Climate.Sampler sampler, RawBiomes rawBiomes) {
        if (!hosts.contains(raw)) return raw;
        bind(sampler);
        return inGrove(QuartPos.toBlock(quartX), QuartPos.toBlock(quartZ), rawBiomes) ? redwood : raw;
    }

    private void bind(Climate.Sampler sampler) {
        if (boundSampler == sampler) return;
        synchronized (this) {
            if (boundSampler == sampler) return;
            Climate.TargetPoint probe = sampler.sample(QuartPos.fromBlock(CELL * 7), 0, QuartPos.fromBlock(-CELL * 5));
            salt = probe.temperature() * 31L + probe.humidity() * 17L + probe.weirdness();
            activeCells.clear();
            boundSampler = sampler;
        }
    }

    private boolean inGrove(int blockX, int blockZ, RawBiomes rawBiomes) {
        int cellX = Math.floorDiv(blockX, CELL);
        int cellZ = Math.floorDiv(blockZ, CELL);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int cx = cellX + dx;
                int cz = cellZ + dz;
                double centerX = cx * CELL + CELL / 2.0 + (unit(cx, cz, 1) * 2 - 1) * JITTER;
                double centerZ = cz * CELL + CELL / 2.0 + (unit(cx, cz, 2) * 2 - 1) * JITTER;
                double radius = RADIUS_MIN + unit(cx, cz, 3) * (RADIUS_MAX - RADIUS_MIN);
                double offsetX = blockX - centerX;
                double offsetZ = blockZ - centerZ;
                double distanceSqr = offsetX * offsetX + offsetZ * offsetZ;
                double outer = radius * (1 + WOBBLE);
                if (distanceSqr > outer * outer) continue;
                if (!isActive(cx, cz, centerX, centerZ, rawBiomes)) continue;
                double angle = Mth.atan2(offsetZ, offsetX);
                double edge = radius * (1 + WOBBLE * (0.6 * Math.sin(2 * angle + unit(cx, cz, 4) * Mth.TWO_PI)
                        + 0.3 * Math.sin(3 * angle + unit(cx, cz, 5) * Mth.TWO_PI)
                        + 0.1 * Math.sin(5 * angle + unit(cx, cz, 6) * Mth.TWO_PI)));
                if (distanceSqr <= edge * edge) return true;
            }
        }
        return false;
    }

    private boolean isActive(int cellX, int cellZ, double centerX, double centerZ, RawBiomes rawBiomes) {
        long key = ((long) cellX << 32) ^ (cellZ & 0xFFFFFFFFL);
        Boolean cached = activeCells.get(key);
        if (cached != null) return cached;
        boolean active = seeds.contains(rawBiomes.at(QuartPos.fromBlock(Mth.floor(centerX)),
                QuartPos.fromBlock(SEED_PROBE_Y), QuartPos.fromBlock(Mth.floor(centerZ))));
        activeCells.put(key, active);
        return active;
    }

    private double unit(int cellX, int cellZ, int channel) {
        long hash = salt ^ (cellX * 0x9E3779B97F4A7C15L) ^ (cellZ * 0xC2B2AE3D27D4EB4FL) ^ (channel * 0x165667B19E3779F9L);
        hash = (hash ^ (hash >>> 30)) * 0xBF58476D1CE4E5B9L;
        hash = (hash ^ (hash >>> 27)) * 0x94D049BB133111EBL;
        hash ^= hash >>> 31;
        return (hash >>> 11) * 0x1.0p-53;
    }
}
