package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;

public class OWEntityFoodOverlay {
    ;

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

    private static final ResourceLocation FOOD_OVERLAY_BG =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/food_overlay.png");
    private static final int MARGIN_X = 6;
    private static final int MARGIN_Y = 6;
    private static final int ICON = 16;
    private static final int BG_W = 28;
    private static final int BG_H = 41;

    public static void createFoodOverlay(GuiGraphics guiGraphics, OWEntity entity, int screenWidth, int screenHeight) {
        ItemStack foodItem = entity.getItemFood();
        if (foodItem.isEmpty()) return;

        int count = entity.getFoodCount();
        Font font = Minecraft.getInstance().font;

        int bx = MARGIN_X - 3;
        int by = screenHeight - BG_H - MARGIN_Y;

        guiGraphics.blit(FOOD_OVERLAY_BG, bx, by, 0, 0, BG_W, BG_H, BG_W, BG_H);

        // Contenu (icône + nombre) CENTRÉ dans le fond, horizontalement ET verticalement.
        final int GAP = 2;
        boolean hasCount = count > 0;
        int contentH = ICON + (hasCount ? GAP + font.lineHeight : 0);
        int iconY = by + (BG_H - contentH) / 2;
        int iconX = bx + (BG_W - ICON) / 2;

        guiGraphics.renderItem(foodItem, iconX, iconY);

        if (hasCount) {
            Component countText = Component.literal("x" + count).setStyle(Style.EMPTY.withBold(true));
            int textX = bx + (BG_W - font.width(countText)) / 2;   // centré dans le fond
            int textY = iconY + ICON + GAP;                         // juste sous l'icône
            guiGraphics.drawString(font, countText, textX, textY, 0xFFFFFF);
        }
    }
}
