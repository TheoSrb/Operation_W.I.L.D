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
        register(2, "piste.attack.2.name", "piste.attack.2.desc", "textures/misc/attacks/red_panda_upgrading_skill_0.png");
        register(3, "piste.attack.3.name", "piste.attack.3.desc", "textures/misc/attacks/red_panda_upgrading_skill_1.png");
        register(4, "piste.attack.4.name", "piste.attack.4.desc", "textures/misc/attacks/peacock_upgrading_skill_0.png");
        register(5, "piste.attack.5.name", "piste.attack.5.desc", "textures/misc/attacks/peacock_upgrading_skill_1.png");
        register(6, "piste.attack.6.name", "piste.attack.6.desc", "textures/misc/attacks/chameleon_upgrading_skill_0.png");
        register(7, "piste.attack.7.name", "piste.attack.7.desc", "textures/misc/attacks/chameleon_upgrading_skill_1.png");
        register(8, "piste.attack.8.name", "piste.attack.8.desc", "textures/misc/attacks/condor_upgrading_skill_0.png");
        register(9, "piste.attack.9.name", "piste.attack.9.desc", "textures/misc/attacks/condor_upgrading_skill_1.png");
        register(10, "piste.attack.10.name", "piste.attack.10.desc", "textures/misc/attacks/tarantula_upgrading_skill_0.png");
        register(11, "piste.attack.11.name", "piste.attack.11.desc", "textures/misc/attacks/tarantula_upgrading_skill_1.png");
    }

    private static void register(int id, String nameKey, String descKey, String iconPath) {
        BY_ID.put(id, new AttackDef(nameKey, descKey,
                ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, iconPath)));
    }

    public static AttackDef get(int id) { return BY_ID.get(id); }
}
