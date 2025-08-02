package net.tiew.operationWild.utils;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class OWKeysBinding {
    public static final String OW_ULTIMATE_KEY = "key.ow.ultimate_key";
    public static final String OW_ATTACKS_INFO_KEY = "key.ow.attacks_info_key";
    public static final String OW_ENTITY_JOURNAL_KEY = "key.ow.entity_journal_key";

    public static final KeyMapping OW_ULTIMATE = new KeyMapping(OW_ULTIMATE_KEY, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, OW_ULTIMATE_KEY);
    public static final KeyMapping OW_ATTACKS_INFO = new KeyMapping(OW_ATTACKS_INFO_KEY, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, OW_ATTACKS_INFO_KEY);
    public static final KeyMapping OW_ENTITY_JOURNAL = new KeyMapping(OW_ENTITY_JOURNAL_KEY, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, OW_ENTITY_JOURNAL_KEY);
}
