package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.custom.living.BoaEntity;

public class BoaOverlay {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/overlay/boa_cards.png");

    public static void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        Player rider = Minecraft.getInstance().player;
        if (rider != null) {
            LivingEntity entity = (LivingEntity) rider.getVehicle();
            if (entity != null) {
                if (entity instanceof OWEntity owEntity) {
                    if (owEntity instanceof BoaEntity boa) {
                        createOverlayCharge(guiGraphics, screenWidth, screenHeight, 256, owEntity);
                    }
                }
            }
        }
    }

    public static void createOverlayCharge(GuiGraphics guiGraphics, int screenWidth, int screenHeight, int maxScale, OWEntity owEntity) {
        int baseX = (screenWidth / 2) + 100;
        int baseY = screenHeight - 22;

        int spacing = 25;

        guiGraphics.blit(TEXTURE, baseX, baseY, 0, 0, 20, 20, maxScale, maxScale);

        if (owEntity instanceof BoaEntity boa && boa.canVenom) {
            guiGraphics.setColor(1.5f, 1.5f, 1.5f, 1.0f);
            guiGraphics.blit(TEXTURE, baseX + spacing, baseY, 20, 0, 20, 20, maxScale, maxScale);
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        } else guiGraphics.blit(TEXTURE, baseX + spacing, baseY, 20, 0, 20, 20, maxScale, maxScale);

        guiGraphics.blit(TEXTURE, baseX + spacing * 2, baseY, 40, 0, 20, 20, maxScale, maxScale);

        if (owEntity.attackTimer >= 1) {
            guiGraphics.blit(TEXTURE, baseX, baseY, 0, 20, 20, 20, maxScale, maxScale);

            int height = Math.min(20, (int) (20f / 11 * owEntity.attackTimer));
            int offsetY = 20 - height;

            guiGraphics.blit(TEXTURE, baseX, baseY + offsetY, 0, 0 + offsetY, 20, height, maxScale, maxScale);

            String timeString = String.valueOf(Math.round(((float) (11 - owEntity.attackTimer) / 20) * 10) / 10.0) + "s";
            Font fontRenderer = Minecraft.getInstance().font;
        }

        if (owEntity instanceof BoaEntity boa) {
            if (boa.venomCooldown >= 1) {
                if (boa.venomCooldown != 801) guiGraphics.blit(TEXTURE, baseX + 25, baseY, 20, 20, 20, 20, maxScale, maxScale);

                int height = Math.min(20, (int) (20f / 800 * (800 - boa.venomCooldown)));
                int offsetY = 20 - height;

                guiGraphics.blit(TEXTURE, baseX + 25, baseY + offsetY, 20, offsetY, 20, height, maxScale, maxScale);

                String timeString = String.valueOf(Math.round(((float) boa.venomCooldown / 20) * 10) / 10.0) + "s";
                Font fontRenderer = Minecraft.getInstance().font;
            }
        }
    }
}
