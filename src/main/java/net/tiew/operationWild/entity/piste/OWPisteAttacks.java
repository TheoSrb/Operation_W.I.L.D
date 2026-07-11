package net.tiew.operationWild.entity.piste;

import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;

import java.util.HashMap;
import java.util.Map;

/**
 * Petit registre des attaques débloquables sur la Piste (paliers {@link OWPisteNode.Type#ATTACK}).
 * Défini simplement : chaque attaque = un nom, une description et une image (icône).
 */
public final class OWPisteAttacks {

    private OWPisteAttacks() {}

    public record AttackDef(String nameKey, String descKey, ResourceLocation icon) {}

    private static final Map<Integer, AttackDef> BY_ID = new HashMap<>();

    static {
        register(0, "piste.attack.0.name", "piste.attack.0.desc", "textures/misc/attacks/tiger_passive_misc_0.png");
        register(1, "piste.attack.1.name", "piste.attack.1.desc", "textures/misc/attacks/tiger_passive_misc_1.png");
    }

    private static void register(int id, String nameKey, String descKey, String iconPath) {
        BY_ID.put(id, new AttackDef(nameKey, descKey,
                ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, iconPath)));
    }

    public static AttackDef get(int id) { return BY_ID.get(id); }
}
