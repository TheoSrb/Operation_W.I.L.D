package net.tiew.operationWild.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.core.OWKeysBinding;

public class OWBookNotifyOverlay {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/overlay/ow_book.png");

    public static void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        long currentTime = System.currentTimeMillis();

        int x = screenWidth / 2;
        int y = screenHeight / 2;

        float oscillation = (float)(Math.sin(currentTime * 0.008) * 37.5 + 75);
        float opacity = Math.min(1.0f, oscillation / 100.0f);
        int verticalOffset = (int)(oscillation / 10);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, opacity);
        guiGraphics.blit(TEXTURE, x - 235, ((y + 88) - verticalOffset), 0, 0, 40, 40, 40, 40);
        guiGraphics.drawString(Minecraft.getInstance().font, OWKeysBinding.OW_ENTITY_JOURNAL.getTranslatedKeyMessage(), x - 235 + 20 - (Minecraft.getInstance().font.width(OWKeysBinding.OW_ENTITY_JOURNAL.getTranslatedKeyMessage()) / 2), (y + 88 - 15) - verticalOffset, 0xFFFFFF);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}