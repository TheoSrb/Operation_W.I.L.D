package net.tiew.operationWild.worldgen.dimension;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
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

import java.nio.file.Path;
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
 * côté. Le bâtiment est donc découpé en {@link #GRID}×{@link #GRID} pièces de {@link #PART_SIZE}
 * blocs sur {@link #LAYERS} étages de {@link #PART_HEIGHT}, nommées
 * {@code arena_<colonne>_<rangée>_<étage>.nbt}. Une pièce absente est simplement ignorée : une arène
 * partiellement exportée se pose quand même, et le mod fonctionne sans aucune pièce.</p>
 *
 * <p><b>Priorité aux fichiers</b> : dès qu'au moins une pièce existe, c'est elle qui est posée et le
 * tracé du code n'est plus joué. C'est ce qui permet de retoucher l'arène à la main puis de figer le
 * résultat avec {@link #export(ServerLevel)}.</p>
 */
public final class OWArenaBuilder {

    private static final Logger LOGGER = LogUtils.getLogger();

    private OWArenaBuilder() {}

    /** Côté d'une pièce, sous la limite de 48 blocs d'un bloc de structure. */
    public static final int PART_SIZE = 44;
    /** Hauteur d'une pièce, également sous la limite de 48 blocs. */
    public static final int PART_HEIGHT = 32;
    /** Grille horizontale : 4×4 pièces de 44 couvrent 176 blocs, soit tout le bâtiment et sa marge. */
    public static final int GRID = 4;
    /** Étages de pièces : 2 × 32 couvrent de l'aire au sommet de la façade. */
    public static final int LAYERS = 2;

    /** Coin nord-ouest de la grille : la construction est centrée sur l'origine de l'arène. */
    public static final int ORIGIN_X = -(PART_SIZE * GRID) / 2;
    public static final int ORIGIN_Z = -(PART_SIZE * GRID) / 2;

    /** Nombre total de pièces d'un export complet. */
    public static final int PART_COUNT = GRID * GRID * LAYERS;

    /**
     * Version du décor. L'incrémenter refait poser l'arène sur les mondes existants — c'est le seul
     * moyen de livrer une correction du décor à des sauvegardes déjà créées.
     */
    public static final int VERSION = 21;

    private static ResourceLocation partId(int col, int row, int layer) {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID,
                "arena_" + col + "_" + row + "_" + layer);
    }

    private static BlockPos partOrigin(int col, int row, int layer) {
        return new BlockPos(
                ORIGIN_X + col * PART_SIZE,
                OWArena.ARENA_FLOOR_Y + layer * PART_HEIGHT,
                ORIGIN_Z + row * PART_SIZE);
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
        if (countTemplates(arena) == 0) {
            OWArenaLayout.generate(arena);
            return 0;
        }
        // Les pièces exportées ne contiennent pas l'air : sans table rase préalable, les couloirs
        // et les arches resteraient bouchés par ce qui occupait déjà le terrain.
        OWArenaLayout.wipe(arena);
        return placeTemplates(arena);
    }

    /**
     * Dossier où atterrissent les pièces exportées.
     *
     * <p>Demandé au gestionnaire de structures plutôt qu'écrit en dur : le nom de ce dossier
     * ({@code structures}) diffère de celui des datapacks ({@code structure}), et rien ne garantit
     * qu'il ne changera pas.</p>
     */
    public static Path exportFolder(ServerLevel arena) {
        return arena.getStructureManager()
                .createAndValidatePathToGeneratedStructure(partId(0, 0, 0), ".nbt")
                .getParent();
    }

    /** Nombre de pièces livrées présentes, sans rien poser. */
    public static int countTemplates(ServerLevel arena) {
        var manager = arena.getStructureManager();
        int found = 0;
        for (int col = 0; col < GRID; col++) {
            for (int row = 0; row < GRID; row++) {
                for (int layer = 0; layer < LAYERS; layer++) {
                    if (manager.get(partId(col, row, layer)).isPresent()) found++;
                }
            }
        }
        return found;
    }

    /**
     * Enregistre l'arène telle qu'elle est en jeu, en pièces {@code .nbt} relisibles.
     *
     * <p>Les fichiers partent dans {@code <monde>/generated/ow/structure/}, exactement là où le
     * chargeur de structures ira les chercher : une arène retouchée à la main est donc reprise
     * telle quelle à la reconstruction suivante, sans toucher au code. Les mêmes fichiers, copiés
     * dans {@code data/ow/structure/} d'un datapack ou des ressources du mod, la livrent à tout le
     * monde.</p>
     *
     * <p>L'air est volontairement exclu : il représente l'essentiel du volume, et la table rase qui
     * précède la pose le rétablit de toute façon.</p>
     *
     * @return le nombre de pièces écrites
     */
    public static int export(ServerLevel arena) {
        if (arena == null || !arena.dimension().equals(OWDimensions.ARENA)) return 0;
        var manager = arena.getStructureManager();
        Vec3i size = new Vec3i(PART_SIZE, PART_HEIGHT, PART_SIZE);

        int written = 0;
        for (int col = 0; col < GRID; col++) {
            for (int row = 0; row < GRID; row++) {
                for (int layer = 0; layer < LAYERS; layer++) {
                    ResourceLocation id = partId(col, row, layer);
                    StructureTemplate template = manager.getOrCreate(id);
                    template.fillFromWorld(arena, partOrigin(col, row, layer), size, false, Blocks.AIR);
                    template.setAuthor(OperationWild.MOD_ID);
                    if (manager.save(id)) written++;
                }
            }
        }
        LOGGER.info("[Arène] Export : {} / {} pièce(s) écrite(s).", written, PART_COUNT);
        return written;
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
                for (int layer = 0; layer < LAYERS; layer++) {
                    Optional<StructureTemplate> template = manager.get(partId(col, row, layer));
                    if (template.isEmpty()) continue;

                    BlockPos at = partOrigin(col, row, layer);
                    if (template.get().placeInWorld(arena, at, at, settings, arena.getRandom(), 2)) placed++;
                }
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
