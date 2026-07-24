package net.tiew.operationWild.worldgen.dimension;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.tiew.operationWild.worldgen.biome.OWBiomes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.saveddata.SavedData;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.core.OWArena;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * Bâtit l'arène livrée avec le mod, dans la dimension de duel.
 *
 * <p>Le décor est posé <b>une fois</b>, à la première ouverture d'un duel sur le monde, puis
 * conservé dans la sauvegarde comme n'importe quelle construction. Il est ainsi identique chez tous
 * les joueurs et sur tous les serveurs, sans que personne ait à le construire ni à recopier un
 * dossier de dimension.</p>
 *
 * <p><b>Pourquoi plusieurs morceaux</b> : un bloc de structure ne peut enregistrer que 48 blocs de
 * côté. Une aire de 100×100 est donc exportée en grille de {@link #GRID}×{@link #GRID} pièces de
 * {@link #PART_SIZE} blocs, nommées {@code arena_<colonne>_<rangée>.nbt}. Une pièce absente est
 * simplement ignorée : une arène partiellement exportée se pose quand même, et le mod fonctionne
 * sans aucune pièce.</p>
 */
public final class OWArenaBuilder {

    private static final Logger LOGGER = LogUtils.getLogger();

    private OWArenaBuilder() {}

    /** Côté d'une pièce, sous la limite de 48 blocs d'un bloc de structure. */
    public static final int PART_SIZE = 34;
    /** Grille de pièces : 3×3 pièces de 34 couvrent 102 blocs, soit l'aire de 100×100 avec un peu de marge. */
    public static final int GRID = 3;

    /** Coin nord-ouest de la grille : la construction est centrée sur l'origine de l'arène. */
    public static final int ORIGIN_X = -(PART_SIZE * GRID) / 2;
    public static final int ORIGIN_Z = -(PART_SIZE * GRID) / 2;

    /**
     * Version du décor. L'incrémenter refait poser l'arène sur les mondes existants — c'est le seul
     * moyen de livrer une correction du décor à des sauvegardes déjà créées.
     */
    public static final int VERSION = 18;

    private static ResourceLocation partId(int col, int row) {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "arena_" + col + "_" + row);
    }

    /**
     * Pose l'arène si elle ne l'est pas déjà (ou si sa version a changé). Sans effet ensuite.
     *
     * <p>Appelé au démarrage d'un duel plutôt qu'au chargement du monde : les chunks concernés sont
     * de toute façon forcés à ce moment-là, et un monde où personne ne se bat jamais n'a aucune
     * raison de porter le décor.</p>
     */
    public static void ensureBuilt(ServerLevel arena) {
        if (arena == null || !arena.dimension().equals(OWDimensions.ARENA)) return;
        BuildState state = BuildState.get(arena.getServer());
        if (state.version >= VERSION) return;

        int placed = build(arena);
        state.version = VERSION;
        state.setDirty();
        LOGGER.info("[Arène] Décor posé ({}).", placed > 0 ? placed + " pièce(s) de structure" : "tracé procédural");
    }

    /** Repose le décor immédiatement, quelle que soit la version enregistrée (commande d'admin). */
    public static int rebuild(ServerLevel arena) {
        if (arena == null || !arena.dimension().equals(OWDimensions.ARENA)) return 0;
        int placed = build(arena);
        BuildState state = BuildState.get(arena.getServer());
        state.version = VERSION;
        state.setDirty();
        return placed;
    }

    /**
     * Pose le décor : les pièces de structure livrées si elles existent, le tracé procédural sinon.
     *
     * <p>La priorité va aux fichiers : c'est ce qui permet de remplacer l'arène par une construction
     * faite à la main sans toucher au code, en déposant simplement les {@code .nbt}.</p>
     *
     * @return le nombre de pièces de structure posées ; 0 si le tracé procédural a servi
     */
    private static int build(ServerLevel arena) {
        applyBiome(arena);
        int placed = placeTemplates(arena);
        if (placed == 0) OWArenaLayout.generate(arena);
        return placed;
    }

    /**
     * Réécrit le biome des chunks de l'arène.
     *
     * <p>Indispensable sur un monde existant : le biome est <b>gravé dans le chunk à sa génération</b>.
     * Changer celui du générateur de la dimension ne touche que les chunks encore à naître ; ceux qui
     * portent déjà l'arène garderaient leur biome d'origine, donc son ciel, son brouillard et ses
     * teintes de végétation. On les repeint donc explicitement, comme le fait {@code /fillbiome}.</p>
     */
    private static void applyBiome(ServerLevel arena) {
        Holder<Biome> biome = arena.registryAccess()
                .registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(OWBiomes.ARENA_BIOME);
        Climate.Sampler sampler = arena.getChunkSource().randomState().sampler();
        BiomeResolver resolver = (x, y, z, ignored) -> biome;

        int chunks = BIOME_RADIUS >> 4;
        for (int cx = -chunks; cx <= chunks; cx++) {
            for (int cz = -chunks; cz <= chunks; cz++) {
                ChunkAccess chunk = arena.getChunk(cx, cz);
                chunk.fillBiomesFromNoise(resolver, sampler);
                chunk.setUnsaved(true);
            }
        }
    }

    /** Rayon, en blocs, sur lequel le biome de l'arène est imposé. */
    private static final int BIOME_RADIUS = 128;

    private static int placeTemplates(ServerLevel arena) {
        var manager = arena.getStructureManager();
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(Rotation.NONE)
                .setIgnoreEntities(false);

        int placed = 0;
        for (int col = 0; col < GRID; col++) {
            for (int row = 0; row < GRID; row++) {
                Optional<StructureTemplate> template = manager.get(partId(col, row));
                if (template.isEmpty()) continue;

                BlockPos at = new BlockPos(
                        ORIGIN_X + col * PART_SIZE,
                        OWArena.ARENA_FLOOR_Y,
                        ORIGIN_Z + row * PART_SIZE);
                if (template.get().placeInWorld(arena, at, at, settings, arena.getRandom(), 2)) placed++;
            }
        }
        return placed;
    }

    /**
     * Retient quelle version du décor est posée sur ce monde.
     *
     * <p>Rangée sur l'overworld comme les autres données du mod : la dimension d'arène peut ne
     * jamais être chargée, et une donnée qu'on n'atteint pas ne sert à rien.</p>
     */
    public static class BuildState extends SavedData {

        public static final String DATA_NAME = "ow_arena_build";

        private int version = 0;

        public static BuildState get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(
                    new SavedData.Factory<>(BuildState::new, BuildState::load, null), DATA_NAME);
        }

        private static BuildState load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
            BuildState state = new BuildState();
            state.version = tag.getInt("version");
            return state;
        }

        @Override
        public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
            tag.putInt("version", version);
            return tag;
        }
    }
}
