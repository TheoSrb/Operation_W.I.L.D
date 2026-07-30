package net.tiew.operationWild.core;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;

import java.util.ArrayList;
import java.util.List;

/**
 * Décor d'arène choisi par le chef qui déclare le combat, une fois le terrain arrêté.
 *
 * <p>Chaque décor appartient à un seul terrain, et chaque terrain a le sien de gratuit : c'est lui
 * qui garantit qu'une tribu qui n'a jamais rien acheté peut toujours se battre, sur la terre ferme
 * comme au fond de l'eau.</p>
 *
 * <p>Les vignettes suivent le nommage {@code textures/gui/arena/<terrain>_<index>.png}. Tant que
 * toutes ne sont pas livrées, l'écran de sélection affiche {@link #FALLBACK_TEXTURE} pour tout le
 * monde ; passer à l'art définitif se fait en une ligne, {@link #getTexture()} étant déjà résolu.</p>
 */
public enum OWArenaVenue {

    ANCIENT_CIVILIZATION(0, "ancient_civilization", "terrestrial_0", 0,   OWArena.Terrain.TERRESTRIAL),
    STONE_PIT           (2, "stone_pit",            "terrestrial_1", 150, OWArena.Terrain.TERRESTRIAL),
    ASH_CRATER          (3, "ash_crater",           "terrestrial_2", 150, OWArena.Terrain.TERRESTRIAL),
    FROZEN_STEPPE       (4, "frozen_steppe",        "terrestrial_3", 150, OWArena.Terrain.TERRESTRIAL),

    SEA_CHASM           (1, "sea_chasm",            "aquatic_0",     0,   OWArena.Terrain.AQUATIC),
    CORAL_BASIN         (5, "coral_basin",          "aquatic_1",     150, OWArena.Terrain.AQUATIC),
    ABYSSAL_TRENCH      (6, "abyssal_trench",       "aquatic_2",     150, OWArena.Terrain.AQUATIC),
    SUNKEN_TEMPLE       (7, "sunken_temple",        "aquatic_3",     150, OWArena.Terrain.AQUATIC);

    /** Rapport largeur/hauteur des captures livrées, pour en découper une bande sans la déformer. */
    public static final float TEXTURE_ASPECT = 2560f / 1369f;

    private final int id;
    private final String name;
    private final String texture;
    private final int price;
    private final OWArena.Terrain terrain;

    OWArenaVenue(int id, String name, String texture, int price, OWArena.Terrain terrain) {
        this.id = id;
        this.name = name;
        this.texture = texture;
        this.price = price;
        this.terrain = terrain;
    }

    public int getId() { return id; }

    public int getPrice() { return price; }

    public boolean isPurchasable() { return price > 0; }

    public OWArena.Terrain getTerrain() { return terrain; }

    public String getTranslationKey() { return "owteams.arena.venue." + name; }

    public Component getLabel() { return Component.translatable(getTranslationKey()); }

    public ResourceLocation getTexture() {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/arena/" + texture + ".png");
    }

    /**
     * Vignette réellement affichée aujourd'hui.
     *
     * <p>Seul l'index 0 de chaque terrain est livré ; les autres afficheraient le damier rose et noir
     * des textures manquantes. Chaque décor emprunte donc celle de sa famille en attendant la sienne
     * — remplacer ce corps par {@link #getTexture()} suffira le jour où les huit seront là.</p>
     */
    public ResourceLocation getDisplayTexture() {
        return defaultFor(terrain).getTexture();
    }

    public boolean fits(OWArena.Terrain t) {
        return t == null || this.terrain == t;
    }

    /** Décor gratuit du terrain : le repli quand rien n'a été acheté ou que le choix reçu est invalide. */
    public static OWArenaVenue defaultFor(OWArena.Terrain terrain) {
        return terrain == OWArena.Terrain.AQUATIC ? SEA_CHASM : ANCIENT_CIVILIZATION;
    }

    /** Décors proposables pour ce terrain, dans l'ordre de déclaration (le gratuit en tête). */
    public static List<OWArenaVenue> forTerrain(OWArena.Terrain terrain) {
        List<OWArenaVenue> list = new ArrayList<>();
        for (OWArenaVenue v : values()) if (v.fits(terrain)) list.add(v);
        return list;
    }

    public static OWArenaVenue byId(int id) {
        for (OWArenaVenue v : values()) if (v.id == id) return v;
        return ANCIENT_CIVILIZATION;
    }
}
