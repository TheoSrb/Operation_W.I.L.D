package net.tiew.operationWild.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWSemiWaterEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.aquatic.TigerSharkEntity;
import net.tiew.operationWild.entity.animals.aquatic.WalrusEntity;
import net.tiew.operationWild.entity.animals.terrestrial.*;
import net.tiew.operationWild.entity.misc.SeaBugEntity;
import net.tiew.operationWild.entity.taming.TamingCrocodile;

import java.awt.*;

public class OWEntityHud {

    public static final ResourceLocation HUD = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/hud/owentity_hud.png");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/seabug_gui.png");

    public static void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        Player rider = Minecraft.getInstance().player;

        if (Minecraft.getInstance().options.hideGui) return;

        if (rider != null) {
            LivingEntity entity = (LivingEntity) rider.getVehicle();
            if (entity != null) {
                if (entity instanceof OWEntity owEntity) {

                    if (owEntity instanceof CrocodileEntity crocodile && crocodile.isStartingTaming() && !crocodile.isTame()) {
                        createCrocodileTamingHUD(guiGraphics, crocodile, screenWidth, screenHeight, rider);
                    }

                    createHUD(guiGraphics, owEntity, screenWidth, screenHeight);

                    if (entity instanceof OWSemiWaterEntity waterEntity) {

                        if (waterEntity.isInWater()) {
                            int actualDepth = (int) (waterEntity.level().getSeaLevel() - waterEntity.getY());
                            boolean isTooDeep = actualDepth >= waterEntity.getMaxDepth();

                            guiGraphics.drawString(Minecraft.getInstance().font, String.valueOf(actualDepth) + "m", (screenWidth / 2) - 23, 9, isTooDeep ? 0xf3c83b : 0xFFFFFF);
                            guiGraphics.drawString(Minecraft.getInstance().font, String.valueOf(waterEntity.getMaxDepth()), (screenWidth / 2) + 12, 9, 0xf3c83b);
                            guiGraphics.blit(TEXTURE, (screenWidth / 2) - 23, 20, 40, 52, 46, 7);
                        }
                    }
                }
            }
        }
    }

    public static void createHUD(GuiGraphics guiGraphics, OWEntity entity, int x, int y) {
        createHealthBar(guiGraphics, entity, x, y);
        createAirBar(guiGraphics, entity, x, y);
        createVitalEnergyBar(guiGraphics, entity, x, y);
        createBar(guiGraphics, entity, x, y);
    }

    public static void createCrocodileTamingHUD(GuiGraphics guiGraphics, CrocodileEntity crocodile, int x, int y, Player player) {
        ResourceLocation CROCODILE_TAMING = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/crocodile_taming.png");

        int xPlacement = x / 2;
        int yPlacement = y - 245;

        int tamingTicksRest = crocodile.getTamingTime();

        int totalSeconds = tamingTicksRest / 20;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;

        Component timer = Component.literal(String.valueOf(minutes) + ":" + String.valueOf(seconds));

        guiGraphics.drawString(Minecraft.getInstance().font, timer, xPlacement - (timer.toString().length() / 2), yPlacement, getBlinkingColorForTimer(player.tickCount, crocodile));

        int barHeight = 5;
        int barWidth = Math.min((crocodile.getEntitiesKilledDuringTaming() * 182) / TamingCrocodile.ENTITIES_REQUIRED, 182);
        int startX = x / 2 - (182 / 2);
        int startY = y - 29;
        int textureY = 0;

        guiGraphics.blit(CROCODILE_TAMING, startX, startY, 0, 5, 182, 5);

        guiGraphics.blit(CROCODILE_TAMING, startX, startY, textureY, crocodile.getEntitiesKilledDuringTaming() >= TamingCrocodile.ENTITIES_REQUIRED ? 10 : 0, barWidth, barHeight);


        Component entitiesBonus = Component.literal("+" + String.valueOf(Math.min((crocodile.getEntitiesKilledDuringTaming() - TamingCrocodile.ENTITIES_REQUIRED), 20)));
        Component tamingPercentage = Component.literal(String.valueOf((int)((crocodile.getEntitiesKilledDuringTaming() / (float)TamingCrocodile.ENTITIES_REQUIRED) * 100) + "%"));
        int color = getBlinkingColor(player.tickCount, crocodile);

        if (crocodile.getEntitiesKilledDuringTaming() >= TamingCrocodile.ENTITIES_REQUIRED) {
            guiGraphics.drawString(Minecraft.getInstance().font, entitiesBonus, startX + (183 / 2) - (entitiesBonus.toString().length() / 2), startY - 10, color, true);
        } else {
            guiGraphics.drawString(Minecraft.getInstance().font, tamingPercentage, startX + (183 / 2) - (tamingPercentage.toString().length() / 2), startY - 10, 0x888888, true);
        }
    }

    private static int getBlinkingColor(int tickCount, CrocodileEntity crocodile) {
        if (crocodile.getEntitiesKilledDuringTaming() >= 60) {
            return 0xefb02a;
        }
        return (tickCount / 10) % 2 == 0 ? 0xFFFFFF : 0x888888;
    }

    private static int getBlinkingColorForTimer(int tickCount, CrocodileEntity crocodile) {
        int frequency = Math.max(1, (int) (((float) crocodile.getTamingTime() / TamingCrocodile.MAX_TAMING_TIME) * 40));
        return (tickCount / frequency) % 2 == 0 ? 0xFFFFFF : 0xFF0000;
    }

    public static int getEntitySpace(OWEntity entity) {
        switch (entity.getClass().getSimpleName()) {
            case "TigerEntity": return 0;
            case "ElephantEntity": return 1;
            case "BoaEntity": return 2;
            case "PeacockEntity": return 3;
            case "TigerSharkEntity": return 4;
            case "MandrillEntity": return 5;
            case "KodiakEntity": return 6;
            case "HyenaEntity": return 7;
            case "WalrusEntity": return 8;
            case "CrocodileEntity": return 9;
            default: return 0;
        }
    }

    public static void createBar(GuiGraphics guiGraphics, OWEntity entity, int x, int y) {
        int xPlacement = x / 2 + 217;;
        int yPlacement = y - 113;

        if (entity instanceof OWEntity owEntity && owEntity.canIncreasesSpeedDuringSprint()) {
            guiGraphics.blit(HUD, xPlacement, yPlacement, 173, 0, 10, 103);

            int barHeight = (int) (103 * (owEntity.getAcceleration() / 100.0f));
            int startY = yPlacement + (103 - barHeight);
            int textureY = 103 - barHeight;

            guiGraphics.blit(HUD, xPlacement, startY, 183, textureY, 10, barHeight);

            guiGraphics.blit(HUD, xPlacement - (19 / 2) + 2, yPlacement - 22, 193, owEntity.getAcceleration() >= 100 ? 0 : 17, 19, 17);
        }
    }

    public static void createHealthBar(GuiGraphics guiGraphics, OWEntity entity, int x, int y) {
        int xPlacement = x / 2 + 10;
        int yPlacement = y - 39;

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
        else if (entity instanceof KodiakEntity) guiGraphics.blit(HUD, xPlacement + 9 + 31 - (17 / 2), yPlacement - 7, 239, 100, 17, 16);
        else if (entity instanceof HyenaEntity) guiGraphics.blit(HUD, xPlacement + 9 + 31 - (17 / 2), yPlacement - 7, 239, 116, 17, 16);
        else if (entity instanceof WalrusEntity) guiGraphics.blit(HUD, xPlacement + 9 + 31 - (12 / 2), yPlacement - 7, 244, 132, 12, 21);
        else if (entity instanceof CrocodileEntity) guiGraphics.blit(HUD, xPlacement + 9 + 31 - (13 / 2), yPlacement - 5, 243, 153, 13, 14);
    }

    public static void createVitalEnergyBar(GuiGraphics guiGraphics, OWEntity entity, int x, int y) {
        int xPlacement = x / 2 + 10;
        int yPlacement = y - 39;

        guiGraphics.blit(HUD, xPlacement + 81 + 5, yPlacement, 0, 230, 8, 14);

        if ((entity.tickCount / 5) % 2 == 0 && (entity.isRunning() || entity.isCombo()) && ((float) (entity.getVitalEnergy() / entity.getMaxVitalEnergy())) < 0.75) {
            RenderSystem.setShaderColor(0.81f, 0.85f, 0.91f, 0.75f);
            guiGraphics.blit(HUD, xPlacement + 81 + 5 + 1, yPlacement + 1, 1, 244, 6, 12);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        } else guiGraphics.blit(HUD, xPlacement + 81 + 5 + 1, yPlacement + 1, ((float) (entity.getVitalEnergy() / entity.getMaxVitalEnergy())) >= 0.75 && (entity.tickCount / 5) % 2 == 0 ? 13 : 1, 244, 6, 12);

        guiGraphics.blit(HUD, xPlacement + 81 + 5 + 1, yPlacement + 1, 7, 244, 6, (int) (12 * (((float) (entity.getVitalEnergy() / entity.getMaxVitalEnergy())))));
    }

    public static void createAirBar(GuiGraphics guiGraphics, OWEntity entity, int x, int y) {
        int barSize = 15;

        int xPlacement = x / 2 - (barSize / 2);
        int yPlacement = y - 51;

        int air = entity.getAirSupply();
        int maxAir = entity.getMaxAirSupply();

        if (air < maxAir || entity.isInWater()) {
            ResourceLocation EMPTY_AIR = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/sprites/hud/empty_air.png");
            ResourceLocation MAX_AIR = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/sprites/hud/max_air.png");

            guiGraphics.blit(EMPTY_AIR, xPlacement, yPlacement, 0, 0, barSize, barSize, barSize, barSize);

            int offset = 180;
            int visualAir = Math.max(0, air + offset);
            int visualMaxAir = maxAir + offset;

            visualAir = Math.min(visualAir, visualMaxAir);

            double fillRatio = (double) visualAir / (double) visualMaxAir;
            int fillHeight = (int) Math.ceil(barSize * fillRatio);

            if (visualAir > 0 && fillHeight == 0) {
                fillHeight = 1;
            }

            if (fillHeight > 0 && visualAir > 0) {
                int startY = yPlacement + (barSize - fillHeight);
                int textureY = barSize - fillHeight;

                if ((entity.tickCount / 5) % 2 == 0 && fillRatio < 0.25) {
                    RenderSystem.setShaderColor(1.0f, 0.0f, 0.0f, 0.75f);
                    guiGraphics.blit(MAX_AIR, xPlacement, startY, 0, textureY, barSize, fillHeight, barSize, barSize);
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                } else {
                    guiGraphics.blit(MAX_AIR, xPlacement, startY, 0, textureY, barSize, fillHeight, barSize, barSize);
                }
            }
        }
    }
}
