package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.custom.living.TigerEntity;

public class TigerOverlay {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/overlay/boa_cards.png");

    public static void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        Player rider = Minecraft.getInstance().player;

        if (Minecraft.getInstance().options.hideGui) return;

        if (rider != null) {
            LivingEntity entity = (LivingEntity) rider.getVehicle();
            if (entity != null) {
                if (entity instanceof OWEntity owEntity) {
                    if (owEntity instanceof TigerEntity tiger && rider.getRootVehicle() == tiger) {
                        createOverlayCharge(guiGraphics, screenWidth, screenHeight, 256, owEntity, tiger.isRunning() ? 13 : 9);
                    }
                }
            }
        }
    }

    public static void createOverlayCharge(GuiGraphics guiGraphics, int screenWidth, int screenHeight, int maxScale, OWEntity owEntity, int timeMax) {
        int baseX = (screenWidth / 2) + 96;
        int baseY = screenHeight - 22;

        int spacing = 25;

        guiGraphics.blit(TEXTURE, baseX, baseY, 0, 40, 20, 20, maxScale, maxScale);
        guiGraphics.blit(TEXTURE, baseX + spacing, baseY, 20, 40, 20, 20, maxScale, maxScale);
        guiGraphics.blit(TEXTURE, baseX + spacing * 2, baseY, 40, 40, 20, 20, maxScale, maxScale);

        if (owEntity.attackTimer >= 1) {
            guiGraphics.blit(TEXTURE, baseX, baseY, 0, 60, 20, 20, maxScale, maxScale);

            int height = Math.min(20, (int) (20f / timeMax * owEntity.attackTimer));
            int offsetY = 20 - height;

            guiGraphics.blit(TEXTURE, baseX, baseY + offsetY, 0, 40 + offsetY, 20, height, maxScale, maxScale);

            String timeString = String.valueOf(Math.round(((float) (timeMax - owEntity.attackTimer) / 20) * 10) / 10.0) + "s";
            Font fontRenderer = Minecraft.getInstance().font;

        }

        if (owEntity instanceof TigerEntity tiger) {
            if (tiger.chargeTimer >= 1) {
                int baseX2 = (screenWidth / 2) + 96 + spacing;
                int baseY2 = screenHeight - 22;
                guiGraphics.blit(TEXTURE, baseX2, baseY2, 20, 60, 20, 20, maxScale, maxScale);

                int height = Math.min(20, (int) (20f / 200 * tiger.chargeTimer));
                int offsetY = 20 - height;

                guiGraphics.blit(TEXTURE, baseX2, baseY2 + offsetY, 20, 40 + offsetY, 20, height, maxScale, maxScale);

                if (tiger.chargeTimer >= 200) {
                    guiGraphics.setColor(1.5f, 1.5f, 1.5f, 1.0f);
                    guiGraphics.blit(TEXTURE, baseX2, baseY, 20, 40, 20, 20, maxScale, maxScale);
                    guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                }
            }
            if (tiger.cooldownJump > 0) {
                int baseX2 = (screenWidth / 2) + 96 + spacing;
                int baseY2 = screenHeight - 22;
                guiGraphics.blit(TEXTURE, baseX2, baseY2, 20, 60, 20, 20, maxScale, maxScale);

                int height = Math.min(20, (int) (20f / 600 * tiger.cooldownJump));
                int offsetY = 20 - height;

                guiGraphics.blit(TEXTURE, baseX2, baseY2 + offsetY, 20, 40 + offsetY, 20, height, maxScale, maxScale);
            }
            if (tiger.ultimateCooldown > 0) {
                int baseX3 = (screenWidth / 2) + 96 + (spacing * 2);
                int baseY3 = screenHeight - 22;
                guiGraphics.blit(TEXTURE, baseX3, baseY3, 40, 60, 20, 20, maxScale, maxScale);

                int height = Math.min(20, (int) (20f / 6000 * tiger.ultimateCooldown));
                int offsetY = 20 - height;

                guiGraphics.blit(TEXTURE, baseX3, baseY3 + offsetY, 40, 40 + offsetY, 20, height, maxScale, maxScale);
            }
        }
    }
}