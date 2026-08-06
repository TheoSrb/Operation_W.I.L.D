package net.tiew.operationWild.entity.resurrection;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.tiew.operationWild.entity.OWEntityRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Roster des vagues de monstres attirés par l'âme pendant le Rituel de Communion.
 *
 * <p><b>Entièrement éditable.</b> La force des monstres dépend de DEUX facteurs :</p>
 * <ul>
 *   <li>le <b>palier de puissance</b> du compagnon ressuscité (1 à 5, cf. {@link #POWER_TIERS}),</li>
 *   <li>l'<b>avancement</b> dans les vagues (les dernières vagues montent en difficulté).</li>
 * </ul>
 * On combine les deux en une "difficulté effective" 1→5 qui choisit dans quel
 * {@link #DIFFICULTY_POOLS palier de monstres} piocher.
 */
public final class ResurrectionWaveRoster {

    private ResurrectionWaveRoster() {}

    // ===================== RÉGLAGES AJUSTABLES =====================
    /** Nombre de monstres de base par vague. */
    public static int BASE_MOBS = 2;
    /** Monstres ajoutés par index de vague. */
    public static int PER_WAVE = 1;
    /** Monstres ajoutés par palier de puissance du compagnon (au-dessus de 1). */
    public static int PER_TIER = 1;
    /** Plafond de monstres dans une vague. */
    public static int MAX_MOBS = 12;

    /** Palier de puissance par défaut si l'espèce n'est pas listée. */
    public static int DEFAULT_TIER = 2;

    /** Palier de puissance (1 à 5) par espèce. Modifiable librement. */
    public static final Map<String, Integer> POWER_TIERS = new HashMap<>(Map.of(
            "boa", 2,
            "tiger", 3,
            "crocodile", 3,
            "kodiak", 3,
            "orca", 4,
            "elephant", 5
    ));

    /**
     * 5 paliers de monstres, du plus faible (indice 0 = difficulté 1) au plus fort
     * (indice 4 = difficulté 5). Mélange OWEntity sauvages + vanilla.
     */
    public static final List<List<EntityType<?>>> DIFFICULTY_POOLS = List.of(
            List.of(EntityType.ZOMBIE, EntityType.SPIDER),                                   // 1 — charognards
            List.of(EntityType.ZOMBIE, EntityType.SPIDER),      // 2 — rôdeurs
            List.of(EntityType.ZOMBIE, EntityType.SPIDER),
            List.of(EntityType.ZOMBIE, EntityType.SPIDER),
            List.of(EntityType.ZOMBIE, EntityType.SPIDER)
            );
    // ==============================================================

    /** Palier de puissance (1 à 5) du compagnon, d'après son type. */
    public static int powerTier(ResourceLocation type) {
        if (type == null) return DEFAULT_TIER;
        return POWER_TIERS.getOrDefault(type.getPath(), DEFAULT_TIER);
    }

    /** Difficulté effective d'une vague (1 à 5) : palier du compagnon ± selon l'avancement. */
    public static int effectiveDifficulty(int companionTier, int waveIndex, int totalWaves) {
        float progress = totalWaves <= 1 ? 1f : (float) waveIndex / (totalWaves - 1);
        // Premières vagues : palier - 1 ; dernière vague : palier + 1.
        int diff = Math.round(companionTier - 1 + progress * 2f);
        return Mth.clamp(diff, 1, 5);
    }

    /** Nombre de monstres dans une vague. */
    public static int mobCountForWave(int waveIndex, int companionTier) {
        return Math.min(MAX_MOBS, BASE_MOBS + waveIndex * PER_WAVE + (companionTier - 1) * PER_TIER);
    }

    /** Tire la composition complète d'une vague (liste de types à faire apparaître). */
    public static List<EntityType<?>> rollWave(RandomSource rng, int waveIndex, int totalWaves, int companionTier) {
        int diff = effectiveDifficulty(companionTier, waveIndex, totalWaves);
        List<EntityType<?>> pool = DIFFICULTY_POOLS.get(diff - 1);
        int count = mobCountForWave(waveIndex, companionTier);
        List<EntityType<?>> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(pool.get(rng.nextInt(pool.size())));
        }
        return result;
    }
}
