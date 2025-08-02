package net.tiew.operationWild.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.utils.OWKeysBinding;

public class OWBookNotifyOverlay {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/overlay/ow_book.png");

    public static int $$0 = 50;
    public static boolean canDecrease = false;

    public static void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {

        int x = screenWidth / 2;
        int y = screenHeight / 2;

        if (canDecrease) {
            $$0--;
            if ($$0 <= 50) {
                canDecrease = false;
            }
        } else {
            $$0++;
            if ($$0 >= 100) {
                canDecrease = true;
            }
        }


        float opacity = $$0 / 100.0f;

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, opacity);
        guiGraphics.blit(TEXTURE, x - 235, ((y + 88) - ($$0 / 10)), 0, 0, 40, 40, 40, 40);
        guiGraphics.drawString(Minecraft.getInstance().font, OWKeysBinding.OW_ENTITY_JOURNAL.getTranslatedKeyMessage(), x - 235 + 20 - (Minecraft.getInstance().font.width(OWKeysBinding.OW_ENTITY_JOURNAL.getTranslatedKeyMessage()) / 2), (y + 88 - 15) - ($$0 / 10), 0xFFFFFF);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
