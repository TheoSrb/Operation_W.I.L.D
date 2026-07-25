package net.tiew.operationWild.worldgen.dimension;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
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

    public static final int AQUATIC_VERSION = 9;

    /** Grille du colisée aquatique : 5×40 = 200 couvrent le carré d'eau 200×200 sans marge d'air. */
    public static final int PART_SIZE_AQ = 40;
    public static final int GRID_AQ = 5;
    public static final int LAYERS_AQ = 2;
    public static final int ORIGIN_AQ_X = OWArena.ARENA_AQUATIC_OFFSET_X - (PART_SIZE_AQ * GRID_AQ) / 2;
    public static final int ORIGIN_AQ_Z = -(PART_SIZE_AQ * GRID_AQ) / 2;
    public static final int PART_COUNT_AQ = GRID_AQ * GRID_AQ * LAYERS_AQ;

    private static ResourceLocation partId(int col, int row, int layer) {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID,
                "arena_" + col + "_" + row + "_" + layer);
    }

    private static ResourceLocation partIdAq(int col, int row, int layer) {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID,
                "arena_aq_" + col + "_" + row + "_" + layer);
    }

    private static BlockPos partOrigin(int col, int row, int layer) {
        return new BlockPos(
                ORIGIN_X + col * PART_SIZE,
                OWArena.ARENA_FLOOR_Y + layer * PART_HEIGHT,
                ORIGIN_Z + row * PART_SIZE);
    }

    private static BlockPos partOriginAq(int col, int row, int layer) {
        return new BlockPos(
                ORIGIN_AQ_X + col * PART_SIZE_AQ,
                OWArena.ARENA_FLOOR_Y + layer * PART_HEIGHT,
                ORIGIN_AQ_Z + row * PART_SIZE_AQ);
    }

    /**
     * Pose l'arène si elle ne l'est pas déjà (ou si sa version a changé). Sans effet ensuite.
     *
     * <p>Appelé au démarrage d'un duel plutôt qu'au chargement du monde : les chunks concernés sont
     * de toute façon forcés à ce moment-là, et un monde où personne ne se bat jamais n'a aucune
     * raison de porter le décor.</p>
     */
    public static void ensureBuilt(ServerLevel arena, OWArena.Terrain terrain) {
        if (arena == null || !arena.dimension().equals(OWDimensions.ARENA)) return;
        BuildState state = BuildState.get(arena.getServer());
        if (terrain == OWArena.Terrain.AQUATIC) {
            if (state.aquaticVersion >= AQUATIC_VERSION) return;
            buildAquatic(arena);
            state.aquaticVersion = AQUATIC_VERSION;
            state.setDirty();
            LOGGER.info("[Arène] Colisée englouti posé (tracé procédural).");
        } else {
            if (state.terrestrialVersion >= VERSION) return;
            int placed = buildTerrestrial(arena);
            state.terrestrialVersion = VERSION;
            state.setDirty();
            LOGGER.info("[Arène] Décor posé ({}).", placed > 0 ? placed + " pièce(s) de structure" : "tracé procédural");
        }
    }

    /** Repose les deux décors immédiatement, quelle que soit la version enregistrée (commande d'admin). */
    public static int rebuild(ServerLevel arena) {
        if (arena == null || !arena.dimension().equals(OWDimensions.ARENA)) return 0;
        int placed = buildTerrestrial(arena);
        buildAquatic(arena);
        BuildState state = BuildState.get(arena.getServer());
        state.terrestrialVersion = VERSION;
        state.aquaticVersion = AQUATIC_VERSION;
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
    private static int buildTerrestrial(ServerLevel arena) {
        applyBiome(arena, OWBiomes.ARENA_BIOME, 0);
        if (countTemplates(arena) == 0) {
            OWArenaLayout.generate(arena);
            return 0;
        }
        // Les pièces exportées ne contiennent pas l'air : sans table rase préalable, les couloirs
        // et les arches resteraient bouchés par ce qui occupait déjà le terrain.
        OWArenaLayout.wipe(arena);
        return placeTemplates(arena);
    }

    private static void buildAquatic(ServerLevel arena) {
        applyBiome(arena, OWBiomes.ARENA_AQUATIC_BIOME, OWArena.ARENA_AQUATIC_OFFSET_X);
        if (countTemplatesAq(arena) == 0) {
            OWAquaticArenaLayout.generate(arena, OWArena.ARENA_AQUATIC_OFFSET_X);
            return;
        }
        // Les pièces aquatiques sont exportées sans l'eau (exclue à la capture) : on rétablit d'abord
        // le socle inondé, puis on pose les solides gelés par-dessus.
        OWAquaticArenaLayout.base(arena, OWArena.ARENA_AQUATIC_OFFSET_X);
        placeTemplatesAq(arena);
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

    /** Nombre de pièces du colisée aquatique présentes. */
    public static int countTemplatesAq(ServerLevel arena) {
        var manager = arena.getStructureManager();
        int found = 0;
        for (int col = 0; col < GRID_AQ; col++) {
            for (int row = 0; row < GRID_AQ; row++) {
                for (int layer = 0; layer < LAYERS_AQ; layer++) {
                    if (manager.get(partIdAq(col, row, layer)).isPresent()) found++;
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
     * Enregistre le colisée aquatique en pièces {@code .nbt}, comme l'arène terrestre.
     *
     * <p><b>L'eau est exclue</b> (et non l'air) : le décor englouti est entièrement immergé, donc
     * capturer l'eau gonflerait les fichiers de millions de blocs. Le socle inondé est rétabli à la
     * pose ({@link OWAquaticArenaLayout#base}) avant que les solides gelés ne soient posés par-dessus.
     * La boîte 200×200 est intégralement sous l'eau : rien d'air à y capturer.</p>
     */
    public static int exportAquatic(ServerLevel arena) {
        if (arena == null || !arena.dimension().equals(OWDimensions.ARENA)) return 0;
        loadAquaticBox(arena);
        var manager = arena.getStructureManager();
        Vec3i size = new Vec3i(PART_SIZE_AQ, PART_HEIGHT, PART_SIZE_AQ);

        int written = 0;
        for (int col = 0; col < GRID_AQ; col++) {
            for (int row = 0; row < GRID_AQ; row++) {
                for (int layer = 0; layer < LAYERS_AQ; layer++) {
                    ResourceLocation id = partIdAq(col, row, layer);
                    StructureTemplate template = manager.getOrCreate(id);
                    template.fillFromWorld(arena, partOriginAq(col, row, layer), size, false, Blocks.WATER);
                    template.setAuthor(OperationWild.MOD_ID);
                    if (manager.save(id)) written++;
                }
            }
        }
        LOGGER.info("[Arène] Export aquatique : {} / {} pièce(s) écrite(s).", written, PART_COUNT_AQ);
        return written;
    }

    /** Charge les chunks de la boîte aquatique avant l'export : sans terrain chargé, on ne capture rien. */
    private static void loadAquaticBox(ServerLevel arena) {
        int minCx = ORIGIN_AQ_X >> 4, maxCx = (ORIGIN_AQ_X + PART_SIZE_AQ * GRID_AQ) >> 4;
        int minCz = ORIGIN_AQ_Z >> 4, maxCz = (ORIGIN_AQ_Z + PART_SIZE_AQ * GRID_AQ) >> 4;
        for (int c = minCx; c <= maxCx; c++) {
            for (int z = minCz; z <= maxCz; z++) arena.getChunk(c, z);
        }
    }

    /**
     * Réécrit le biome des chunks de l'arène.
     *
     * <p>Indispensable sur un monde existant : le biome est <b>gravé dans le chunk à sa génération</b>.
     * Changer celui du générateur de la dimension ne touche que les chunks encore à naître ; ceux qui
     * portent déjà l'arène garderaient leur biome d'origine, donc son ciel, son brouillard et ses
     * teintes de végétation. On les repeint donc explicitement, comme le fait {@code /fillbiome}.</p>
     */
    private static void applyBiome(ServerLevel arena, ResourceKey<Biome> key, int centerX) {
        Holder<Biome> biome = arena.registryAccess()
                .registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(key);
        Climate.Sampler sampler = arena.getChunkSource().randomState().sampler();
        BiomeResolver resolver = (x, y, z, ignored) -> biome;

        int chunks = BIOME_RADIUS >> 4;
        int centerChunk = centerX >> 4;
        for (int cx = centerChunk - chunks; cx <= centerChunk + chunks; cx++) {
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

    private static int placeTemplatesAq(ServerLevel arena) {
        var manager = arena.getStructureManager();
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(Rotation.NONE)
                .setIgnoreEntities(false);

        int placed = 0;
        for (int col = 0; col < GRID_AQ; col++) {
            for (int row = 0; row < GRID_AQ; row++) {
                for (int layer = 0; layer < LAYERS_AQ; layer++) {
                    Optional<StructureTemplate> template = manager.get(partIdAq(col, row, layer));
                    if (template.isEmpty()) continue;

                    BlockPos at = partOriginAq(col, row, layer);
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

        private int terrestrialVersion = 0;
        private int aquaticVersion = 0;

        public static BuildState get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(
                    new SavedData.Factory<>(BuildState::new, BuildState::load, null), DATA_NAME);
        }

        private static BuildState load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
            BuildState state = new BuildState();
            state.terrestrialVersion = tag.contains("terrestrialVersion") ? tag.getInt("terrestrialVersion") : tag.getInt("version");
            state.aquaticVersion = tag.getInt("aquaticVersion");
            return state;
        }

        @Override
        public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
            tag.putInt("terrestrialVersion", terrestrialVersion);
            tag.putInt("aquaticVersion", aquaticVersion);
            return tag;
        }
    }
}
