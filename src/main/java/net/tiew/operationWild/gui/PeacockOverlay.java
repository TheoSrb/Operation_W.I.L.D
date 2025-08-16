package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.custom.living.PeacockEntity;

public class PeacockOverlay {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/overlay/boa_cards.png");

    public static void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        Player rider = Minecraft.getInstance().player;
        if (rider != null) {
            LivingEntity entity = (LivingEntity) rider.getVehicle();
            if (entity != null) {
                if (entity instanceof OWEntity owEntity) {
                    if (owEntity instanceof PeacockEntity boa) {
                        createOverlayCharge(guiGraphics, screenWidth, screenHeight, 256, owEntity);
                    }
                }
            }
        }
    }

    public static void createOverlayCharge(GuiGraphics guiGraphics, int screenWidth, int screenHeight, int maxScale, OWEntity owEntity) {
        int baseX = (screenWidth / 2) + 96;
        int baseY = screenHeight - 22;
        int cardsHeightOnImage = 80;

        int spacing = 25;

        guiGraphics.blit(TEXTURE, baseX, baseY, 0, cardsHeightOnImage, 20, 20, maxScale, maxScale);
        guiGraphics.blit(TEXTURE, baseX + spacing, baseY, 20, cardsHeightOnImage, 20, 20, maxScale, maxScale);
        guiGraphics.blit(TEXTURE, baseX + spacing * 2, baseY, 40, cardsHeightOnImage, 20, 20, maxScale, maxScale);

        if (owEntity.attackTimer >= 1) {
            guiGraphics.blit(TEXTURE, baseX, baseY, 0, cardsHeightOnImage + 20, 20, 20, maxScale, maxScale);

            int height = Math.min(20, (int) (20f / 10 * owEntity.attackTimer));
            int offsetY = 20 - height;

            guiGraphics.blit(TEXTURE, baseX, baseY + offsetY, 0, cardsHeightOnImage + offsetY, 20, height, maxScale, maxScale);
        }
    }
}
