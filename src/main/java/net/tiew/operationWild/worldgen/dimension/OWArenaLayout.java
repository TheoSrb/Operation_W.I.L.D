package net.tiew.operationWild.worldgen.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.tiew.operationWild.core.OWArena;

/**
 * L'amphithéâtre de duel, bâti par le code et livré avec le mod.
 *
 * <p>Chaque monde reçoit la même arène sans que personne ait à la construire ni à recopier un
 * dossier de dimension. Une construction faite à la main reste prioritaire : déposer les pièces
 * {@code .nbt} suffit à remplacer ce tracé (cf. {@link OWArenaBuilder}).</p>
 *
 * <h2>Le bâtiment</h2>
 * De l'intérieur vers l'extérieur, comme un amphithéâtre romain :
 * <ol>
 *   <li><b>L'aire</b> (r ≤ 40) — sable, en plaques irrégulières de sable meuble, de grès poli et de
 *       grès usé. Aucun motif géométrique : un sol de combat doit avoir l'air foulé, pas dessiné.</li>
 *   <li><b>Le podium</b> (r 41-42) — muraille de pierre noire qui sépare l'aire des gradins, percée
 *       de niches grillagées, rythmée de pilastres, jalonnée de trophées.</li>
 *   <li><b>Les portes</b> — deux tunnels voûtés perçant tout le bâtiment sur l'axe des camps, clos
 *       de grilles côté aire.</li>
 *   <li><b>La cavea</b> (r 43-52) — cinq rangs de gradins en <i>escaliers</i>, coupés de murs
 *       radiaux, avec deux loges d'honneur à colonnade sur l'axe transversal.</li>
 *   <li><b>Le mur d'enceinte</b> (r 53-54) — adossé au dernier rang, percé de vomitoires.</li>
 *   <li><b>La façade</b> (r 55-60) — trois niveaux d'arcades séparés par des corniches,
 *       couronnés des mâts du velum.</li>
 * </ol>
 *
 * <h2>Contraintes de conception</h2>
 * <ul>
 *   <li><b>Symétrie stricte</b> selon les deux axes, vérifiée colonne par colonne. Les camps se font
 *       face en {@code x = ±16} : le moindre relief présent d'un seul côté serait un avantage
 *       offert. Les secteurs d'arcades sont de plus <i>centrés sur les axes</i>, si bien qu'une
 *       arche — et non un trumeau — accueille chaque camp.</li>
 *   <li><b>Aire parfaitement plate</b>, sans marche ni estrade. Les points d'apparition sont placés
 *       par recherche de hauteur libre : un relief à cet endroit décalerait les combattants.</li>
 *   <li><b>Le ciel reste ouvert</b> au-dessus de l'aire : rien ne s'avance en surplomb.</li>
 *   <li><b>Rien n'entre dans l'aire.</b> Tout ce qui est bâti commence au rayon 41 ; le sol de
 *       combat reste dégagé sur toute sa hauteur.</li>
 *   <li><b>Rien ne dépasse de la dimension</b>, haute de 128 blocs : le sommet des mâts culmine à
 *       y=113.</li>
 * </ul>
 */
public final class OWArenaLayout {

    private OWArenaLayout() {}

    // ── Rayons, du centre vers l'extérieur ───────────────────────────────────────
    private static final int FLOOR_RADIUS = 40;
    private static final int PODIUM_R0 = 41, PODIUM_R1 = 42;
    private static final int CAVEA_R0 = 43, TIERS = 5, TIER_W = 2;
    private static final int CAVEA_R1 = CAVEA_R0 + TIERS * TIER_W - 1;        // 52
    private static final int BACK_R0 = CAVEA_R1 + 1, BACK_R1 = CAVEA_R1 + 2;  // 53-54
    private static final int FACADE_R0 = BACK_R1 + 1, FACADE_R1 = BACK_R1 + 6; // 55-60

    // ── Hauteurs ─────────────────────────────────────────────────────────────────
    private static final int Y = OWArena.ARENA_FLOOR_Y;                 // 63
    private static final int PODIUM_H = 6;
    private static final int TIER_RISE = 2;
    private static final int TIER_Y0 = Y + 7;                           // 70
    private static final int CAVEA_TOP = TIER_Y0 + (TIERS - 1) * TIER_RISE;   // 78
    private static final int FACADE_Y0 = Y + 2;                         // 65
    private static final int FACADE_LEVELS = 3, LEVEL_H = 14;
    /** Sommet des arcades : l'édifice s'arrête là, les mâts le couronnent directement. */
    private static final int ARCADE_TOP = FACADE_Y0 + FACADE_LEVELS * LEVEL_H;   // 107
    private static final int MAST_H = 5;
    /** Hauteur dégagée avant reconstruction : couvre tout ce que ce tracé peut poser. */
    private static final int CLEAR_TOP = ARCADE_TOP + MAST_H + 3;       // 115

    // ── Découpes angulaires ──────────────────────────────────────────────────────
    /** Arcades de la façade. Les centres tombent sur les axes : un camp fait face à une arche. */
    private static final int FACADE_SECTORS = 24;
    private static final int CAVEA_SECTORS = 12;
    private static final int PODIUM_SECTORS = 16;

    /** Demi-largeur d'une ouverture, en fraction de secteur : 0,5 ouvrirait tout. */
    private static final double ARCH_HALF = 0.32;
    private static final int ARCH_STRAIGHT = 6, ARCH_RISE = 4;

    /** Demi-largeur des tunnels d'entrée, en blocs. */
    private static final int GATE_HALF = 3;

    /**
     * (Re)dessine l'amphithéâtre. Idempotent : rejouer la génération redonne exactement le même
     * bâtiment, ce qui permet d'itérer sur le tracé avec la commande de reconstruction.
     */
    public static void generate(ServerLevel level) {
        clear(level);
        arenaFloor(level);
        podium(level);
        cavea(level);
        imperialBoxes(level);
        outerWall(level);
        facade(level);
        gates(level);
        masts(level);
    }

    // ── 0. Table rase ────────────────────────────────────────────────────────────

    /**
     * Efface tout ce qui surmonte le sol : terrain plat d'origine comme tracé précédent.
     *
     * <p>Les colonnes déjà vides sont sautées — sur un monde neuf, presque tout l'est, et le test
     * coûte bien moins cher que la pose.</p>
     */
    private static void clear(ServerLevel level) {
        BlockState air = Blocks.AIR.defaultBlockState();
        forEachColumn(FACADE_R1 + 1, (x, z) -> {
            for (int y = Y + 1; y <= CLEAR_TOP; y++) {
                BlockPos pos = new BlockPos(x, y, z);
                if (!level.getBlockState(pos).isAir()) {
                    level.setBlock(pos, air, Block.UPDATE_CLIENTS);
                }
            }
        });
    }

    // ── 1. L'aire ────────────────────────────────────────────────────────────────

    /**
     * Le sol de combat : plaques de sable irrégulières sur un lit de grès.
     *
     * <p>Aucun motif géométrique, aucune ligne tracée. Les taches viennent d'un relief lissé —
     * deux ondes de longueurs différentes, plus un grain fin sur les bords — de sorte que les
     * plaques aient des contours mous plutôt que des frontières nettes. Le tout ne dépend que des
     * distances <b>absolues</b> à l'origine : les quatre quadrants s'usent donc à l'identique, et
     * aucun camp ne combat sur un terrain différent de l'autre.</p>
     */
    private static void arenaFloor(ServerLevel level) {
        BlockState loose = Blocks.SAND.defaultBlockState();
        BlockState packed = Blocks.SMOOTH_SANDSTONE.defaultBlockState();
        BlockState worn = Blocks.SANDSTONE.defaultBlockState();

        forEachColumn(FLOOR_RADIUS, (x, z) -> {
            int ax = Math.abs(x), az = Math.abs(z);
            double n = patchNoise(ax, az);
            // Grain fin, pour que la limite entre deux plaques ne soit jamais une courbe parfaite.
            n += (noise(ax, az, 9) - 4) * 0.035;

            BlockState state;
            if (n > 0.34) state = loose;
            else if (n < -0.40) state = worn;
            else state = packed;
            set(level, x, Y, z, state);
        });
    }

    /** Relief lissé à deux longueurs d'onde, symétrique par construction (il ne lit que |x| et |z|). */
    private static double patchNoise(int ax, int az) {
        return 0.62 * Math.sin(ax * 0.137 + 0.7) * Math.cos(az * 0.119 - 0.3)
                + 0.38 * Math.sin((ax + az) * 0.081)
                + 0.24 * Math.cos((ax - az) * 0.067 + 1.1);
    }

    // ── 2. Le podium ─────────────────────────────────────────────────────────────

    /**
     * La muraille qui ceint l'aire : c'est elle qui fait de l'endroit une arène plutôt qu'un pré.
     *
     * <p>Percée de niches grillagées — l'œil lit des portes, mais rien ne passe : une ouverture
     * franche laisserait les créatures filer dans les gradins.</p>
     */
    private static void podium(ServerLevel level) {
        BlockState wall = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        BlockState pilaster = Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
        BlockState bars = Blocks.IRON_BARS.defaultBlockState();
        BlockState coping = Blocks.POLISHED_BLACKSTONE_SLAB.defaultBlockState();

        forEachRing(PODIUM_R0, PODIUM_R1, (x, z) -> {
            double band = sectorBand(x, z, PODIUM_SECTORS);
            boolean niche = band < 0.20;
            boolean edge = band > 0.44;

            set(level, x, Y, z, wall);
            for (int dy = 1; dy <= PODIUM_H; dy++) {
                BlockState state;
                if (niche && dy <= 4) {
                    state = (x * x + z * z <= PODIUM_R0 * PODIUM_R0) ? bars : wall;
                } else {
                    state = edge ? pilaster : wall;
                }
                set(level, x, Y + dy, z, state);
            }
            set(level, x, Y + PODIUM_H + 1, z, coping);
        });

        trophies(level);
    }

    /** Trophées dressés sur le couronnement du podium, entre deux niches. */
    private static void trophies(ServerLevel level) {
        BlockState shaft = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        BlockState crown = Blocks.GOLD_BLOCK.defaultBlockState();
        int top = Y + PODIUM_H + 1;
        for (int s = 0; s < PODIUM_SECTORS; s++) {
            // Décalés d'un demi-secteur : ils tombent entre les niches, jamais devant.
            double a = 2 * Math.PI * (s + 0.5) / PODIUM_SECTORS;
            int r = PODIUM_R1;
            int x = (int) Math.round(Math.cos(a) * r);
            int z = (int) Math.round(Math.sin(a) * r);
            for (int dy = 1; dy <= 2; dy++) set(level, x, top + dy, z, shaft);
            set(level, x, top + 3, z, crown);
        }
    }

    // ── 3. Les gradins ───────────────────────────────────────────────────────────

    /**
     * Cinq rangs en escalier, coupés de murs radiaux.
     *
     * <p>Les marches sont de vraies <b>marches</b> : des blocs d'escalier tournés vers le centre,
     * et non des cubes empilés. La pente descend vers l'aire, comme dans une cavea antique.</p>
     */
    private static void cavea(ServerLevel level) {
        BlockState riser = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState seat = Blocks.SMOOTH_STONE.defaultBlockState();

        for (int tier = 0; tier < TIERS; tier++) {
            int r0 = CAVEA_R0 + tier * TIER_W;
            int r1 = r0 + TIER_W - 1;
            int top = TIER_Y0 + tier * TIER_RISE;
            forEachRing(r0, r1, (x, z) -> {
                for (int y = Y; y < top; y++) set(level, x, y, z, riser);
                // Le rang intérieur porte la contremarche en escalier, l'extérieur l'assise plate.
                boolean front = (x * x + z * z) <= r0 * r0;
                set(level, x, top, z, front
                        ? Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                                .setValue(StairBlock.FACING, inward(x, z))
                        : seat);
            });
        }

        // Murs radiaux : ils découpent la cavea en coins et cassent la monotonie de l'anneau.
        // Décalés d'un demi-secteur pour laisser les axes libres — les tunnels y passent.
        BlockState divider = Blocks.CHISELED_TUFF.defaultBlockState();
        BlockState lantern = Blocks.LANTERN.defaultBlockState();
        for (int s = 0; s < CAVEA_SECTORS; s++) {
            double a = 2 * Math.PI * (s + 0.5) / CAVEA_SECTORS;
            for (int r = CAVEA_R0; r <= CAVEA_R1; r++) {
                int x = (int) Math.round(Math.cos(a) * r);
                int z = (int) Math.round(Math.sin(a) * r);
                int tier = Math.min(TIERS - 1, (r - CAVEA_R0) / TIER_W);
                int top = TIER_Y0 + tier * TIER_RISE;
                for (int y = top + 1; y <= top + 2; y++) set(level, x, y, z, divider);
                if (r == CAVEA_R1) set(level, x, top + 3, z, lantern);
            }
        }
    }

    /**
     * Les deux loges d'honneur, face à face sur l'axe transversal.
     *
     * <p>Placées à 90° des camps : elles dominent le combat sans favoriser ni l'un ni l'autre, et
     * donnent au bâtiment un axe secondaire — sans quoi l'anneau de gradins tourne indéfiniment
     * sans que l'œil trouve où se poser.</p>
     */
    private static void imperialBoxes(ServerLevel level) {
        BlockState floor = Blocks.POLISHED_ANDESITE.defaultBlockState();
        BlockState column = Blocks.TUFF_WALL.defaultBlockState();
        BlockState canopy = Blocks.POLISHED_BLACKSTONE_SLAB.defaultBlockState();
        BlockState back = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        BlockState trim = Blocks.GOLD_BLOCK.defaultBlockState();

        final int boxY = TIER_Y0 + 2 * TIER_RISE;      // de plain-pied avec le 3ᵉ rang
        final double halfAngle = Math.toRadians(13);

        for (int sz = -1; sz <= 1; sz += 2) {
            double axis = sz > 0 ? Math.PI / 2 : -Math.PI / 2;
            forEachRing(CAVEA_R0 + 2, CAVEA_R1, (x, z) -> {
                if (angleDistance(x, z, axis) > halfAngle) return;
                int r2 = x * x + z * z;

                for (int y = boxY + 1; y <= boxY + 6; y++) set(level, x, y, z, Blocks.AIR.defaultBlockState());
                set(level, x, boxY, z, floor);

                if (r2 > (CAVEA_R1 - 1) * (CAVEA_R1 - 1)) {
                    for (int y = boxY + 1; y <= boxY + 5; y++) set(level, x, y, z, back);
                }
                set(level, x, boxY + 6, z, canopy);
            });

            // Colonnade de façade : fût de tuf sur base et chapiteau dorés, bandeau d'or continu
            // au-dessus. C'est ce qui rend les loges reconnaissables de l'autre bout de l'aire —
            // seule tache chaude d'un bâtiment entièrement minéral.
            for (int k = -3; k <= 3; k++) {
                double a = axis + Math.toRadians(k * 4.0);
                int r = CAVEA_R0 + 2;
                int x = (int) Math.round(Math.cos(a) * r);
                int z = (int) Math.round(Math.sin(a) * r);
                set(level, x, boxY + 1, z, trim);                      // base
                for (int y = boxY + 2; y <= boxY + 4; y++) set(level, x, y, z, column);
                set(level, x, boxY + 5, z, trim);                      // chapiteau
                set(level, x, boxY + 6, z, canopy);
            }
            // Bandeau doré courant sur tout le devant de la loge, entre les colonnes.
            for (double a = axis - halfAngle; a <= axis + halfAngle; a += 0.006) {
                int r = CAVEA_R0 + 2;
                int x = (int) Math.round(Math.cos(a) * r);
                int z = (int) Math.round(Math.sin(a) * r);
                set(level, x, boxY + 6, z, trim);
            }
        }
    }

    // ── 4. Le mur d'enceinte ─────────────────────────────────────────────────────

    /** Adossé au dernier rang, il ferme les gradins, sert d'assise à la façade et porte les vomitoires. */
    private static void outerWall(ServerLevel level) {
        BlockState bricks = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState worn = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        BlockState mossy = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        int top = CAVEA_TOP + 4;

        forEachRing(BACK_R0, BACK_R1, (x, z) -> {
            double band = sectorBand(x, z, CAVEA_SECTORS);
            for (int y = Y; y <= top; y++) {
                int n = noise(Math.abs(x), Math.abs(z) + y, 13);
                set(level, x, y, z, n == 0 ? worn : (n == 1 ? mossy : bricks));
            }
            // Vomitoire : bouche d'accès sombre, alignée sur chaque coin de gradins.
            if (band < 0.10) {
                for (int y = CAVEA_TOP + 1; y <= CAVEA_TOP + 3; y++) set(level, x, y, z, air);
            }
        });
    }

    // ── 5. La façade ─────────────────────────────────────────────────────────────

    /**
     * Trois niveaux d'arcades superposées, séparés par des corniches et scandés de colonnes
     * engagées.
     *
     * <p>Chaque ouverture est un <b>arc en plein cintre</b> : sa hauteur suit un demi-cercle sur la
     * largeur du secteur. C'est ce qui distingue une vraie arcade d'une rangée de trous carrés, et
     * cela ne coûte qu'une racine carrée par colonne.</p>
     */
    private static void facade(ServerLevel level) {
        BlockState stone = Blocks.STONE_BRICKS.defaultBlockState();
        // Le tuf signale ce qui porte : trumeaux, clefs de voûte, seuils, corniches.
        // La masse reste en briques de pierre, si bien que la structure du bâtiment se
        // lit d'un coup d'œil au lieu de se fondre dans un mur uniforme.
        BlockState pier = Blocks.CHISELED_TUFF.defaultBlockState();
        BlockState cornice = Blocks.TUFF_SLAB.defaultBlockState();
        BlockState sill = Blocks.POLISHED_TUFF.defaultBlockState();
        BlockState keystone = Blocks.POLISHED_TUFF.defaultBlockState();

        forEachRing(FACADE_R0, FACADE_R1, (x, z) -> {
            for (int y = Y; y < FACADE_Y0; y++) set(level, x, y, z, stone);

            double band = sectorBand(x, z, FACADE_SECTORS);
            boolean isPier = band > 0.42;
            boolean outerSkin = x * x + z * z > (FACADE_R1 - 1) * (FACADE_R1 - 1);

            for (int lvl = 0; lvl < FACADE_LEVELS; lvl++) {
                int base = FACADE_Y0 + lvl * LEVEL_H;
                int archTop = archHeight(band, base);
                boolean open = archTop >= base;

                for (int y = base; y < base + LEVEL_H; y++) {
                    if (open && y <= archTop) continue;              // le vide de l'arche
                    set(level, x, y, z, isPier ? pier : stone);
                }
                if (open) {
                    set(level, x, base, z, sill);                    // seuil
                    if (band < 0.04) set(level, x, archTop, z, keystone);  // clef de voûte
                }
                set(level, x, base + LEVEL_H - 1, z, cornice);
            }
            // Colonne engagée sur la peau extérieure des trumeaux : le relief de la silhouette.
            if (isPier && outerSkin) {
                for (int y = FACADE_Y0; y < ARCADE_TOP; y++) {
                    if (y % LEVEL_H == (FACADE_Y0 + LEVEL_H - 1) % LEVEL_H) continue;
                    set(level, x, y, z, Blocks.TUFF_WALL.defaultBlockState());
                }
            }
            set(level, x, ARCADE_TOP, z, cornice);
        });
    }

    /**
     * Sommet du vide d'une arche, ou {@code base - 1} si la colonne est pleine.
     *
     * <p>{@code u} est la position dans l'ouverture, de 0 (clef) à 1 (piédroit) ; la hauteur ajoutée
     * suit {@code √(1−u²)}, c'est-à-dire le demi-cercle de l'arc.</p>
     */
    private static int archHeight(double band, int base) {
        if (band >= ARCH_HALF) return base - 1;
        double u = band / ARCH_HALF;
        return base + ARCH_STRAIGHT + (int) Math.round(ARCH_RISE * Math.sqrt(1.0 - u * u));
    }

    // ── 6. Les portes ────────────────────────────────────────────────────────────

    /**
     * Les deux tunnels voûtés qui percent le bâtiment sur l'axe des camps.
     *
     * <p>Chaque camp apparaît face à sa porte : c'est ce qui donne un sens à l'orientation de
     * l'aire, jusque-là parfaitement circulaire et donc sans repère. La voûte s'abaisse sur les
     * bords, et une grille scelle la bouche côté aire — l'œil lit une entrée, mais rien ne
     * passe.</p>
     *
     * <p>Percés en dernier : le tunnel doit traverser le podium, les gradins, l'enceinte et la
     * façade, tous déjà bâtis.</p>
     */
    private static void gates(ServerLevel level) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState floor = Blocks.SMOOTH_STONE.defaultBlockState();
        BlockState frame = Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
        BlockState bars = Blocks.IRON_BARS.defaultBlockState();
        BlockState lantern = Blocks.LANTERN.defaultBlockState();

        for (int sx = -1; sx <= 1; sx += 2) {
            for (int r = PODIUM_R0; r <= FACADE_R1; r++) {
                int x = sx * r;
                for (int z = -GATE_HALF; z <= GATE_HALF; z++) {
                    int h = vaultHeight(Math.abs(z));
                    for (int dy = 1; dy <= h; dy++) set(level, x, Y + dy, z, air);
                    set(level, x, Y, z, floor);
                    // Encadrement : les rangs qui bordent la voûte, taillés.
                    if (Math.abs(z) == GATE_HALF) set(level, x, Y + h + 1, z, frame);
                }
                // Éclairage du tunnel, un fanal tous les quatre blocs.
                if ((r - PODIUM_R0) % 4 == 2) {
                    set(level, x, Y + 5, GATE_HALF, lantern);
                    set(level, x, Y + 5, -GATE_HALF, lantern);
                }
            }
            // Grille à l'aplomb de la face intérieure du podium.
            int gateX = sx * PODIUM_R0;
            for (int z = -GATE_HALF; z <= GATE_HALF; z++) {
                for (int dy = 1; dy <= vaultHeight(Math.abs(z)); dy++) set(level, gateX, Y + dy, z, bars);
            }
        }
    }

    /** Hauteur de la voûte du tunnel : elle retombe vers les piédroits. */
    private static int vaultHeight(int az) {
        return switch (az) {
            case 0, 1 -> 7;
            case 2 -> 6;
            default -> 4;
        };
    }

    // ── 7. Les mâts ──────────────────────────────────────────────────────────────

    /**
     * Les mâts du velum, hérissant le couronnement.
     *
     * <p>Ils prennent appui sur les <b>limites de secteurs</b>, c'est-à-dire sur les trumeaux
     * pleins : un mât planté au-dessus d'une arche flotterait dans le vide.</p>
     *
     * <p>La toile elle-même n'est pas tendue : à cette hauteur, un anneau d'étoffe se lit comme un
     * disque flottant détaché du bâtiment plutôt que comme un auvent.</p>
     */
    private static void masts(ServerLevel level) {
        BlockState mast = Blocks.STRIPPED_OAK_LOG.defaultBlockState();
        BlockState tip = Blocks.LANTERN.defaultBlockState();

        for (int s = 0; s < FACADE_SECTORS; s++) {
            double a = 2 * Math.PI * (s + 0.5) / FACADE_SECTORS;
            int r = (FACADE_R0 + FACADE_R1) / 2;
            int x = (int) Math.round(Math.cos(a) * r);
            int z = (int) Math.round(Math.sin(a) * r);
            for (int dy = 1; dy <= MAST_H; dy++) set(level, x, ARCADE_TOP + dy, z, mast);
            set(level, x, ARCADE_TOP + MAST_H + 1, z, tip);
        }
    }

    // ── Outils ───────────────────────────────────────────────────────────────────

    /**
     * Distance angulaire au secteur le plus proche, de 0 (au centre d'un secteur) à 0,5 (à la
     * limite entre deux).
     *
     * <p>Les centres tombent sur les multiples exacts de l'angle de secteur, donc <b>sur les
     * axes</b> : chaque camp fait face au milieu d'une arche, jamais à un trumeau. La mesure est
     * la distance au multiple le plus proche, une fonction paire — elle est donc rigoureusement
     * identique dans les quatre quadrants, ce qui garantit que les deux camps voient le même
     * bâtiment.</p>
     */
    private static double sectorBand(int x, int z, int sectors) {
        double angle = Math.atan2(z, x);
        if (angle < 0) angle += 2 * Math.PI;
        double sector = 2 * Math.PI / sectors;
        double m = angle % sector;
        return Math.min(m, sector - m) / sector;
    }

    /** Écart angulaire absolu entre la colonne et une direction donnée, en radians. */
    private static double angleDistance(int x, int z, double target) {
        double d = Math.abs(Math.atan2(z, x) - target) % (2 * Math.PI);
        return Math.min(d, 2 * Math.PI - d);
    }

    /**
     * Direction cardinale pointant vers le centre — celle vers laquelle une marche doit descendre.
     * Le choix bascule sur les diagonales, ce qui dessine un joint radial, comme dans un vrai
     * appareillage de gradins.
     */
    private static Direction inward(int x, int z) {
        if (Math.abs(x) >= Math.abs(z)) return x > 0 ? Direction.WEST : Direction.EAST;
        return z > 0 ? Direction.NORTH : Direction.SOUTH;
    }

    /** Bruit stable et déterministe, pour l'usure. Ne dépend que de ses entrées. */
    private static int noise(int a, int b, int mod) {
        int h = a * 73856093 ^ b * 19349663;
        h = (h ^ (h >>> 13)) * 1274126177;
        return Math.floorMod(h ^ (h >>> 16), mod);
    }

    private interface Column { void at(int x, int z); }

    /** Parcourt les colonnes du disque de rayon {@code radius} centré sur l'origine. */
    private static void forEachColumn(int radius, Column action) {
        int r2 = radius * radius;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= r2) action.at(x, z);
            }
        }
    }

    /** Parcourt les colonnes de l'anneau compris entre {@code r0} et {@code r1} inclus. */
    private static void forEachRing(int r0, int r1, Column action) {
        int inner = (r0 - 1) * (r0 - 1), outer = r1 * r1;
        for (int x = -r1; x <= r1; x++) {
            for (int z = -r1; z <= r1; z++) {
                int d2 = x * x + z * z;
                if (d2 <= outer && d2 > inner) action.at(x, z);
            }
        }
    }

    /**
     * Pose un bloc sans propager de mise à jour aux voisins : à cette échelle, les cascades de
     * mises à jour coûteraient bien plus cher que la pose elle-même — et le sable de l'aire
     * tomberait dès la première pose si on les laissait se déclencher.
     */
    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
    }
}
