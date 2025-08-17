package net.tiew.operationWild.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.custom.living.*;

public class OWEntityHud {

    public static final ResourceLocation HUD = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/hud/owentity_hud.png");

    public static void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        Player rider = Minecraft.getInstance().player;

        if (Minecraft.getInstance().options.hideGui) return;

        int x = screenWidth / 2;
        int y = screenHeight / 2;

        if (rider != null) {
            LivingEntity entity = (LivingEntity) rider.getVehicle();
            if (entity != null) {
                if (entity instanceof OWEntity owEntity) {
                    createHUD(guiGraphics, owEntity, x, y);
                }
            }
        }
    }

    public static void createHUD(GuiGraphics guiGraphics, OWEntity entity, int x, int y) {
        createHealthBar(guiGraphics, entity, x, y);
        createVitalEnergyBar(guiGraphics, entity, x, y);
    }

    public static int getEntitySpace(OWEntity entity) {
        switch (entity.getClass().getSimpleName()) {
            case "TigerEntity": return 0;
            case "ElephantEntity": return 1;
            case "BoaEntity": return 2;
            case "PeacockEntity": return 3;
            case "TigerSharkEntity": return 4;
            case "MandrillEntity": return 5;
            default: return 0;
        }
    }

    public static void createHealthBar(GuiGraphics guiGraphics, OWEntity entity, int x, int y) {
        int xPlacement = x + 10;
        int yPlacement = y + 89;

        guiGraphics.blit(HUD, xPlacement, yPlacement, 0, 0, 81, 9);

        int healthWidth = (int) (79 * ((float) (entity.getHealth() / entity.getMaxHealth())));
        guiGraphics.blit(HUD, xPlacement + 1, yPlacement + 1, 81, 10 + (7 * getEntitySpace(entity)), 79, 7);

        if ((entity.tickCount / 5) % 2 == 0 && ((float) (entity.getHealth() / entity.getMaxHealth())) <= 0.25f) {
            RenderSystem.setShaderColor(1.0f, 0.0f, 0.0f, 0.75f);
            guiGraphics.blit(HUD, xPlacement + 1 + (79 - healthWidth), yPlacement + 1 + entity.getRandom().nextInt(2), 80 - healthWidth, 10 + (7 * getEntitySpace(entity)), healthWidth, 7);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        } else guiGraphics.blit(HUD, xPlacement + 1 + (79 - healthWidth), yPlacement + 1, 80 - healthWidth, 10 + (7 * getEntitySpace(entity)), healthWidth, 7);

        if (entity instanceof TigerEntity) guiGraphics.blit(HUD, xPlacement + 9 + 31 - (15 / 2), yPlacement - 7, 241, 21, 15, 15);
        else if (entity instanceof ElephantEntity) guiGraphics.blit(HUD, xPlacement + 9 + 31 - (31 / 2), yPlacement - 12, 225, 0, 31, 21);
        else if (entity instanceof BoaEntity) guiGraphics.blit(HUD, xPlacement + 9 + 31 - (17 / 2), yPlacement - 2, 239, 36,17, 11);
        else if (entity instanceof PeacockEntity) guiGraphics.blit(HUD, xPlacement + 9 + 31 - (9 / 2), yPlacement - 10, 247, 47, 9, 19);
        else if (entity instanceof TigerSharkEntity) guiGraphics.blit(HUD, xPlacement + 9 + 31 - (19 / 2), yPlacement - 4, 237, 66, 19, 13);
        else if (entity instanceof MandrillEntity) guiGraphics.blit(HUD, xPlacement + 9 + 31 - (15 / 2), yPlacement - 12, 241, 79, 15, 21);
    }

    public static void createVitalEnergyBar(GuiGraphics guiGraphics, OWEntity entity, int x, int y) {
        int xPlacement = x + 10;
        int yPlacement = y + 89;

        guiGraphics.blit(HUD, xPlacement + 81 + 5, yPlacement, 0, 230, 8, 14);

        if ((entity.tickCount / 5) % 2 == 0 && entity.isRunning() && ((float) (entity.getVitalEnergy() / entity.getMaxVitalEnergy())) < 0.75) {
            RenderSystem.setShaderColor(0.81f, 0.85f, 0.91f, 0.75f);
            guiGraphics.blit(HUD, xPlacement + 81 + 5 + 1, yPlacement + 1, 1, 244, 6, 12);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        } else guiGraphics.blit(HUD, xPlacement + 81 + 5 + 1, yPlacement + 1, ((float) (entity.getVitalEnergy() / entity.getMaxVitalEnergy())) >= 0.75 && (entity.tickCount / 5) % 2 == 0 ? 13 : 1, 244, 6, 12);

        guiGraphics.blit(HUD, xPlacement + 81 + 5 + 1, yPlacement + 1, 7, 244, 6, (int) (12 * (((float) (entity.getVitalEnergy() / entity.getMaxVitalEnergy())))));
    }
}
