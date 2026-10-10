package net.tiew.operationWild.datagen.wiki;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.tiew.operationWild.worldgen.OWBiomeModifiers;

import java.util.Comparator;
import java.util.List;

final class OWWikiSpawnRarity {

    private static final String METRIC = "groups_per_100_chunks";
    private static final String FORMULA = "100 * p / (1 - p) * species_weight / pool_weight / chunk_one_in";
    private static final String[] TIERS = {"common", "uncommon", "rare", "very_rare", "exceptional"};
    private static final double[] TIER_MIN = {8.0, 3.0, 0.75, 0.25, 0.0};

    private final List<Holder.Reference<Biome>> biomes;

    OWWikiSpawnRarity(HolderLookup.Provider registries) {
        this.biomes = registries.lookupOrThrow(Registries.BIOME).listElements()
                .sorted(Comparator.comparing(holder -> holder.key().location().toString()))
                .toList();
    }

    JsonObject of(EntityType<?> type) {
        int chunkOneIn = 1;
        for (OWBiomeModifiers.SpawnRule rule : OWBiomeModifiers.SPAWN_RULES) {
            if (rule.entity().get() == type) chunkOneIn = Math.max(chunkOneIn, rule.chunkOneIn());
        }

        JsonArray entries = new JsonArray();
        JsonObject best = null;
        if (type.getCategory() == MobCategory.CREATURE) {
            for (Holder.Reference<Biome> holder : biomes) {
                JsonObject entry = biomeEntry(holder, type, chunkOneIn);
                if (entry == null) continue;
                entries.add(entry);
                if (best == null || entry.get(METRIC).getAsDouble() > best.get(METRIC).getAsDouble()) best = entry;
            }
        }

        JsonObject rarity = new JsonObject();
        rarity.addProperty("spawns_naturally", best != null);
        if (best == null) {
            rarity.addProperty("tier", "none");
            return rarity;
        }
        rarity.add("tier", best.get("tier"));
        rarity.add("best_biome", best.get("biome"));
        rarity.add(METRIC, best.get(METRIC));
        rarity.add("individuals_per_100_chunks", best.get("individuals_per_100_chunks"));
        rarity.add("biomes", entries);
        return rarity;
    }

    static JsonObject scale() {
        JsonObject scale = new JsonObject();
        scale.addProperty("metric", METRIC);
        scale.addProperty("formula", FORMULA);
        JsonArray tiers = new JsonArray();
        for (int i = 0; i < TIERS.length; i++) {
            JsonObject tier = new JsonObject();
            tier.addProperty("tier", TIERS[i]);
            tier.addProperty("min", TIER_MIN[i]);
            tiers.add(tier);
        }
        scale.add("tiers", tiers);
        return scale;
    }

    private static JsonObject biomeEntry(Holder.Reference<Biome> holder, EntityType<?> type, int chunkOneIn) {
        ResourceKey<Biome> key = holder.key();
        MobSpawnSettings settings = holder.value().getMobSettings();

        int pool = 0;
        int weight = 0;
        double members = 0.0;
        for (MobSpawnSettings.SpawnerData data : settings.getMobs(MobCategory.CREATURE).unwrap()) {
            int entryWeight = data.getWeight().asInt();
            pool += entryWeight;
            if (data.type != type) continue;
            weight += entryWeight;
            members += entryWeight * (data.minCount + data.maxCount) / 2.0;
        }
        for (OWBiomeModifiers.SpawnRule rule : OWBiomeModifiers.SPAWN_RULES) {
            if (!rule.biomes().contains(key) || rule.entity().get().getCategory() != MobCategory.CREATURE) continue;
            pool += rule.weight();
            if (rule.entity().get() != type) continue;
            weight += rule.weight();
            members += rule.weight() * (rule.minCount() + rule.maxCount()) / 2.0;
        }
        if (weight <= 0) return null;

        double probability = settings.getCreatureProbability();
        double groups = 100.0 * probability / (1.0 - probability) * weight / pool / chunkOneIn;

        JsonObject entry = new JsonObject();
        entry.addProperty("biome", key.location().toString());
        entry.addProperty("tier", tierOf(groups));
        entry.addProperty(METRIC, round(groups));
        entry.addProperty("individuals_per_100_chunks", round(groups * members / weight));
        entry.addProperty("species_weight", weight);
        entry.addProperty("pool_weight", pool);
        entry.addProperty("creature_spawn_probability", round(probability));
        entry.addProperty("chunk_one_in", chunkOneIn);
        return entry;
    }

    private static String tierOf(double groups) {
        for (int i = 0; i < TIERS.length; i++) {
            if (groups >= TIER_MIN[i]) return TIERS[i];
        }
        return TIERS[TIERS.length - 1];
    }

    private static double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
