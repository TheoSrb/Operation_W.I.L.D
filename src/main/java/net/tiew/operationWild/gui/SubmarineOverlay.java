package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.misc.SeaBugEntity;

public class SubmarineOverlay {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/seabug_gui.png");

    public static void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        Player rider = Minecraft.getInstance().player;

        if (rider != null) {
            LivingEntity entity = (LivingEntity) rider.getVehicle();
            if (entity != null) {
                if (entity instanceof OWEntity owEntity) {
                    if (owEntity instanceof SeaBugEntity seabug) {
                        int actualDepth = (int) (seabug.level().getSeaLevel() - seabug.getY());
                        boolean isTooDeep = actualDepth >= SeaBugEntity.MAX_DEPTH;

                        guiGraphics.drawString(Minecraft.getInstance().font, String.valueOf(actualDepth) + "m", (screenWidth / 2) - 23, 9, isTooDeep ? 0xf3c83b : 0xFFFFFF);
                        guiGraphics.drawString(Minecraft.getInstance().font, String.valueOf(SeaBugEntity.MAX_DEPTH), (screenWidth / 2) + 12, 9, 0xf3c83b);
                        guiGraphics.blit(TEXTURE, (screenWidth / 2) - 23, 20, 40, 52, 46, 7);


                        guiGraphics.blit(TEXTURE, (screenWidth / 2) - 50, 200, 0, 0, 101, 43);
                        guiGraphics.drawString(Minecraft.getInstance().font, String.valueOf((int) (seabug.getHealth())), (screenWidth / 2) - 18, 216, 0xFFFFFF);
                        guiGraphics.drawString(Minecraft.getInstance().font, String.valueOf((int) (seabug.getEnergy())), (screenWidth / 2) + 26, 216, 0xFFFFFF);

                        int healthBarHeight = (int) (9 * (seabug.getHealth() / (float) seabug.getMaxHealth()));
                        guiGraphics.blit(TEXTURE, ((screenWidth / 2) - 50) + 18, (200) + 10 + 9 - healthBarHeight, 1, 72 + 9 - healthBarHeight, 9, healthBarHeight);

                        int energyBarHeight = (int) (24 * (seabug.getEnergy() / 100.0f));
                        int energyYOffset = 24 - energyBarHeight;
                        guiGraphics.blit(TEXTURE, ((screenWidth / 2) - 50) + 56, (200) + 9 + energyYOffset, 0, 42 + energyYOffset, 18, energyBarHeight);
                    }
                }
            }
        }
    }

}
