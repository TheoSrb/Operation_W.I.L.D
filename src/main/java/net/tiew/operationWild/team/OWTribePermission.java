package net.tiew.operationWild.team;

import net.minecraft.network.chat.Component;

/**
 * Permissions granulaires accordées à un membre simple d'une tribu sur les entités des AUTRES membres.
 * Le propriétaire d'une entité, le chef et les chefs adjoints ont toujours l'accès complet ; ces
 * permissions ne restreignent que les membres simples. Stockées en bitmask par membre sur {@link OWTeam}.
 */
public enum OWTribePermission {
    CONTROL   ("owteams.perm.control"),    // monter / attaquer / asseoir-suivre
    INVENTORY ("owteams.perm.inventory"),  // ouvrir l'inventaire
    RENAME    ("owteams.perm.rename"),      // renommer
    LEVEL_UP  ("owteams.perm.level_up"),    // monter en niveau
    SKINS     ("owteams.perm.skins");       // variantes & skins

    private final String key;

    OWTribePermission(String key) { this.key = key; }

    public int bit() { return 1 << ordinal(); }
    public String getKey() { return key; }
    public Component getLabel() { return Component.translatable(key); }
    public Component getDescription() { return Component.translatable(key + ".desc"); }

    public static final int ALL = (1 << values().length) - 1;

    /** Permissions par défaut d'un membre simple qui rejoint : Contrôle + Inventaire. */
    public static final int MEMBER_DEFAULT = CONTROL.bit() | INVENTORY.bit();
    /** Permissions par défaut d'un chef adjoint : tout sauf le renommage. */
    public static final int DEPUTY_DEFAULT = ALL & ~RENAME.bit();
}
