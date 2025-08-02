package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.event.ClientEvents;

public class RightClickAlertOverlay {

    public static void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        Font font = Minecraft.getInstance().font;
        Player rider = Minecraft.getInstance().player;
        if (rider != null) {
            boolean isWhite = (rider.tickCount / 5) % 2 == 0;
            int color = isWhite ? 0xFFFFFF : 0x8b8b8b;

            Component questsUpdated = Component.translatable("tooltip.rightClickAlert")
                    .setStyle(Style.EMPTY.withBold(true).withColor(color).withUnderlined(true));

            float scale = (float) ClientEvents.rightClickNips / 10 + 1.0f;

            int textWidth = font.width(questsUpdated);
            int textHeight = font.lineHeight;

            float scaledTextWidth = textWidth * scale;
            float scaledTextHeight = textHeight * scale;

            float textX = (screenWidth - scaledTextWidth) / 2;
            float textY = (screenHeight - scaledTextHeight) / 2 - 15;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(scale, scale, scale);

            guiGraphics.drawString(font, questsUpdated, (int) (textX / scale), (int) (textY / scale), color);

            guiGraphics.pose().popPose();
        }
    }
}
