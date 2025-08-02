package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.custom.living.boss.PlantEmpressEntity;

import java.util.List;

public class PlantEmpressBossBar {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/overlay/ow_boss_bars.png");
    private static final double RENDER_DISTANCE = 100.0;

    public static void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        Player player = Minecraft.getInstance().player;
        if (player == null || player.level() == null) return;

        List<PlantEmpressEntity> nearbyBosses = player.level().getEntitiesOfClass(
                PlantEmpressEntity.class,
                player.getBoundingBox().inflate(RENDER_DISTANCE),
                boss -> boss.isAlive() && player.distanceTo(boss) <= RENDER_DISTANCE
        );

        for (PlantEmpressEntity boss : nearbyBosses) {
            int yOffset = nearbyBosses.indexOf(boss) * 30;
            createOverlayCharge(guiGraphics, screenWidth, yOffset, 256, boss);
        }
    }

    public static void createOverlayCharge(GuiGraphics guiGraphics, int screenWidth, int yOffset, int maxScale, OWEntity owEntity) {
        int baseX = (screenWidth / 2) - (222 / 2);
        int baseY = 17 + yOffset;

        if (owEntity instanceof PlantEmpressEntity plantEmpress) {
            guiGraphics.blit(TEXTURE, baseX, baseY, 0, 22, 222, 22, maxScale, maxScale);
            guiGraphics.blit(TEXTURE, baseX, baseY, 0, 0, (int) (222 * (plantEmpress.getHealth() / plantEmpress.getMaxHealth())), 22, maxScale, maxScale);
            guiGraphics.blit(TEXTURE, baseX + 57, baseY + 4, 0, 59, 21, 7, maxScale, maxScale);
            guiGraphics.blit(TEXTURE, baseX + 144, baseY + 4, 0, 59, 21, 7, maxScale, maxScale);
            guiGraphics.blit(TEXTURE, baseX + 93, baseY, 0, 70, 36, 11, maxScale, maxScale);

            float head1Health = plantEmpress.getHead1Life();
            float head2Health = plantEmpress.getHead2Life();
            float head3Health = plantEmpress.getHead3Life();
            int bar1Width = (int) (36 * (head1Health / 150.0f));
            int bar2Width = (int) (21 * (head2Health / 75.0f));
            int bar3Width = (int) (21 * (head3Health / 75.0f));

            guiGraphics.blit(TEXTURE, baseX + 93, baseY, 93, 0, bar1Width, 11, maxScale, maxScale);
            guiGraphics.blit(TEXTURE, baseX + 57, baseY + 4, 57, 4, bar2Width, 7, maxScale, maxScale);
            guiGraphics.blit(TEXTURE, baseX + 144, baseY + 4, 57, 4, bar3Width, 7, maxScale, maxScale);

            Component entity = Component.translatable("entity.ow.plant_empress");
            Component title = Component.literal("-- ").append(entity).append(" --").setStyle(Style.EMPTY.withBold(true).withColor(0xa3d976));
            int textWidth = Minecraft.getInstance().font.width(title);
            int textX = baseX + (222 / 2) - (textWidth / 2);
            int textY = baseY - 10;

            guiGraphics.drawString(Minecraft.getInstance().font, title, textX, textY, 0xFFFFFF);
        }
    }
}