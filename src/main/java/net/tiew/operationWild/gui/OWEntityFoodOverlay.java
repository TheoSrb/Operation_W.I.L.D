package net.tiew.operationWild.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.tiew.operationWild.entity.OWEntity;

public class OWEntityFoodOverlay { ;

    public static void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        Player rider = Minecraft.getInstance().player;

        int x = screenWidth / 2;
        int y = screenHeight / 2;
        if (rider != null) {
            LivingEntity entity = (LivingEntity) rider.getVehicle();
            if (entity != null) {
                if (entity instanceof OWEntity owEntity) {
                    createFoodOverlay(guiGraphics, owEntity, x, y);
                }
            }
        }
    }

    public static void createFoodOverlay(GuiGraphics guiGraphics, OWEntity entity, int x, int y) {
        ItemStack foodItem = entity.getItemFood();
        if (!foodItem.isEmpty()) {
            int count = entity.getFoodCount();

            guiGraphics.renderItem(foodItem, x + 100, y + 76);


            if (count > 0) {
                PoseStack poseStack = guiGraphics.pose();
                poseStack.pushPose();

                String countText = String.valueOf(count);
                Font font = Minecraft.getInstance().font;

                float textX = x + 110 - font.width(countText) / 2f;
                float textY = y + 94;

                guiGraphics.drawString(font, countText, (int) textX, (int) textY, 0xFFFFFF);

                poseStack.popPose();
            }
        }
    }
}
