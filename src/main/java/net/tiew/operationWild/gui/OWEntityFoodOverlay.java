package net.tiew.operationWild.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.tiew.operationWild.entity.OWEntity;

public class OWEntityFoodOverlay { ;

    public static void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        Player rider = Minecraft.getInstance().player;

        if (rider != null) {
            LivingEntity entity = (LivingEntity) rider.getVehicle();

            if (Minecraft.getInstance().options.hideGui) return;

            if (entity != null) {
                if (entity instanceof OWEntity owEntity) {
                    createFoodOverlay(guiGraphics, owEntity, screenWidth, screenHeight);
                }
            }
        }
    }

    public static void createFoodOverlay(GuiGraphics guiGraphics, OWEntity entity, int x, int y) {
        ItemStack foodItem = entity.getItemFood();
        if (!foodItem.isEmpty()) {
            int count = entity.getFoodCount();

            guiGraphics.renderItem(foodItem, (x / 2) - 110, y - 18);


            if (count > 0) {
                PoseStack poseStack = guiGraphics.pose();
                poseStack.pushPose();

                Component countText = Component.literal(String.valueOf(count)).setStyle(Style.EMPTY.withBold(true));
                Font font = Minecraft.getInstance().font;

                float textX = (((float) x / 2) - 116 + (count >= 10 ? 0 : 8)) + font.width(countText) / 2f;
                float textY = y - 27;

                guiGraphics.drawString(font, countText, (int) textX, (int) textY, 0xFFFFFF);

                poseStack.popPose();
            }
        }
    }
}
