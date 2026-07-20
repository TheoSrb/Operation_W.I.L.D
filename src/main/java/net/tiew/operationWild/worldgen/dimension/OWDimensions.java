package net.tiew.operationWild.worldgen.dimension;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.tiew.operationWild.OperationWild;

/**
 * Dimensions custom du mod.
 *
 * <p>L'<b>arène</b> est un monde plat sans structure ni faune, défini par datapack
 * ({@code data/ow/dimension_type/arena.json} et {@code data/ow/dimension/arena.json}) plutôt que par
 * datagen : les deux fichiers sont statiques et l'API {@code FlatLevelGeneratorSettings} est bien
 * plus fragile d'une version à l'autre que le JSON qu'elle produit.</p>
 *
 * <p>Tous les combats se déroulent au <b>même endroit</b>, autour de l'origine : l'arène n'en
 * accueille qu'un à la fois (une seule world border par dimension), et une aire fixe permet d'y
 * bâtir un décor permanent. Le terrain y est protégé par {@code OWArenaProtection}.</p>
 */
public final class OWDimensions {

    private OWDimensions() {}

    /** Clé du niveau (dimension) d'arène, à passer à {@code MinecraftServer#getLevel}. */
    public static final ResourceKey<Level> ARENA = ResourceKey.create(
            Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "arena"));
}
