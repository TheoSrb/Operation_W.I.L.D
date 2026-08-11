package net.tiew.operationWild.core;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.tiew.operationWild.entity.attacks.OWAttacksHandler;
import org.lwjgl.glfw.GLFW;

public class OWKeysBinding {
    public static final String OW_ULTIMATE_KEY = "key.ow.ultimate_key";
    public static final String OW_ATTACKS_INFO_KEY = "key.ow.attacks_info_key";
    public static final String OW_ENTITY_JOURNAL_KEY = "key.ow.entity_journal_key";

    public static final String OW_CATEGORY = "key.categories.operationwild";

    public static final KeyMapping OW_ATTACK_0 = new KeyMapping(
            "key.ow.attack_0", KeyConflictContext.IN_GAME,
            InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_RIGHT,
            OWAttacksHandler.OW_CATEGORY
    );
    public static final KeyMapping OW_ATTACK_1 = new KeyMapping(
            "key.ow.attack_1", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X,
            OWAttacksHandler.OW_CATEGORY
    );


    /**
     * Modificateur de changement de carte secondaire : maintenu, la molette fait défiler les
     * attaques secondaires de la monture au lieu de la barre d'inventaire.
     */
    public static final String OW_SWITCH_ATTACK_KEY = "key.ow.switch_attack";
    public static final KeyMapping OW_SWITCH_ATTACK = new KeyMapping(
            OW_SWITCH_ATTACK_KEY, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT,
            OWAttacksHandler.OW_CATEGORY
    );

    public static final String PET_INVENTORY_KEY = "key.pet.inventory_key";
    public static final KeyMapping PET_INVENTORY = new KeyMapping(PET_INVENTORY_KEY, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_E, OW_CATEGORY);



    public static final KeyMapping OW_ULTIMATE = new KeyMapping(OW_ULTIMATE_KEY, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, OW_CATEGORY);
    public static final KeyMapping OW_ATTACKS_INFO = new KeyMapping(OW_ATTACKS_INFO_KEY, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_SEMICOLON, OW_CATEGORY);
    public static final KeyMapping OW_ENTITY_JOURNAL = new KeyMapping(OW_ENTITY_JOURNAL_KEY, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, OW_CATEGORY);

    // Menu de tribu (refonte player-centric) — ouvre l'écran de gestion / création / découverte de tribu.
    public static final String OW_TRIBE_MENU_KEY = "key.ow.tribe_menu";
    public static final KeyMapping OW_TRIBE_MENU = new KeyMapping(OW_TRIBE_MENU_KEY, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_T, OW_CATEGORY);

    /**
     * Renoncement aux didacticiels.
     *
     * <p>Contexte {@link KeyConflictContext#UNIVERSAL} volontaire : certains didacticiels s'affichent
     * par-dessus l'écran d'inventaire de la monture, où le contexte « en jeu » ne s'applique plus. La
     * touche doit répondre dans les deux situations, sans quoi le seul didacticiel qu'on ne pourrait
     * pas passer serait celui qui bloque déjà l'écran.</p>
     */
    public static final String OW_SKIP_TUTORIAL_KEY = "key.ow.skip_tutorial";
    public static final KeyMapping OW_SKIP_TUTORIAL = new KeyMapping(OW_SKIP_TUTORIAL_KEY, KeyConflictContext.UNIVERSAL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, OW_CATEGORY);
}