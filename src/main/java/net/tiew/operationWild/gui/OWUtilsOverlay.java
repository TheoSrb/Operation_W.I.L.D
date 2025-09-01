package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.misc.Submarine;

public class OWUtilsOverlay { ;

    public static void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        Player rider = Minecraft.getInstance().player;
        Font font = Minecraft.getInstance().font;

        int x = screenWidth / 2;
        int y = screenHeight / 2;
        if (rider != null) {
            LivingEntity entity = (LivingEntity) rider.getVehicle();
            if (entity != null) {
                if (entity instanceof OWEntity owEntity && !(owEntity instanceof Submarine)) {

                    if (owEntity.questsAreUpdated()) {
                        boolean isWhite = (owEntity.tickCount / 7) % 2 == 0;
                        int color = isWhite ? 0xFFFFFF : 0x8b8b8b;

                        Component questsUpdated = Component.translatable("tooltip.questsUpdated")
                                .setStyle(Style.EMPTY.withBold(true).withColor(color).withUnderlined(true));

                        int textWidth = font.width(questsUpdated);
                        int textX = (screenWidth - textWidth) / 2;
                        int textY = screenHeight / 2 - questsUpdated.getString().length();

                        guiGraphics.drawString(font, questsUpdated, textX, textY - 85, color);
                    }

                }
            }
        }
    }
}
