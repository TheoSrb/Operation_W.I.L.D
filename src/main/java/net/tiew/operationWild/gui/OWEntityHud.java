package net.tiew.operationWild.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.*;
import net.tiew.operationWild.entity.config.IOWDiver;
import net.tiew.operationWild.entity.taming.TamingCrocodile;

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

                    // Lu sur le contrat de plongée et non sur une famille : l'orque n'hérite pas de
                    // OWSemiWaterEntity, et c'est ce qui la privait de la jauge jusqu'ici.
                    boolean depthShown = owEntity instanceof IOWDiver && owEntity.isInWater();

                    if (depthShown) {
                        IOWDiver diver = (IOWDiver) owEntity;
                        int actualDepth = (int) (owEntity.level().getSeaLevel() - owEntity.getY());
                        boolean isTooDeep = actualDepth >= diver.getMaxDepth();

                        // Passé la limite, la profondeur clignote en rouge : c'est la seule chose
                        // à l'écran qui explique les dégâts encaissés, et le jaune fixe d'avant ne
                        // se distinguait pas assez du chiffre de limite, écrit dans la même teinte.
                        int depthColor = isTooDeep
                                ? ((owEntity.tickCount / 5) % 2 == 0 ? 0xFF4040 : 0xF3C83B)
                                : 0xFFFFFF;

                        guiGraphics.drawString(Minecraft.getInstance().font, String.valueOf(actualDepth) + "m", (screenWidth / 2) - 23, 9, depthColor);
                        guiGraphics.drawString(Minecraft.getInstance().font, String.valueOf(diver.getMaxDepth()), (screenWidth / 2) + 12, 9, 0xf3c83b);
                        guiGraphics.blit(TEXTURE, (screenWidth / 2) - 23, 20, 40, 52, 46, 7);

                        if (isTooDeep) renderPressureWarning(guiGraphics, screenWidth, owEntity.tickCount);
                    }

                    createEntityAirBubbles(guiGraphics, owEntity, screenWidth, depthShown);
                }
            }
        }
    }

    public static void createHUD(GuiGraphics guiGraphics, OWEntity entity, int x, int y) {
        createHealthBar(guiGraphics, entity, x, y);
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
            guiGraphics.drawString(Minecraft.getInstance().font, entitiesBonus, startX + (183 / 2) - (entitiesBonus.toString().length() / 2), startY - 10, 0x888888, true);
        } else {
            guiGraphics.drawString(Minecraft.getInstance().font, tamingPercentage, startX + (183 / 2) - (tamingPercentage.toString().length() / 2), startY - 10, color, true);
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
            case "LionEntity": return 10;
            case "OrcaEntity": return 11;
            case "KangarooEntity": return 12;
            default: return 0;
        }
    }

    public static void createBar(GuiGraphics guiGraphics, OWEntity entity, int x, int y) {
        int xPlacement = x / 2 + 217;;
        int yPlacement = y - 113;

        if (entity instanceof OWEntity owEntity && owEntity.canIncreasesSpeedDuringSprint()) {
            guiGraphics.blit(HUD, xPlacement, yPlacement, 173, 0, 10, 103);

            // L'accélération n'a pas de plafond : le paquet de sprint l'incrémente d'un par tick
            // sans jamais s'arrêter, et seul le calcul de vitesse la borne à cent. Sans le même
            // plafond ici, la jauge dépassait sa hauteur au bout de cinq secondes de course et se
            // redessinait une seconde fois AU-DESSUS du cadre, avec un décalage de texture négatif.
            float filled = Math.min(100f, owEntity.getAcceleration()) / 100f;

            int barHeight = (int) (103 * filled);
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

        EntityIconData iconData = getEntityIconData(entity);
        if (iconData != null) {
            guiGraphics.blit(HUD,
                    xPlacement + 9 + 31 + iconData.offsetX,
                    yPlacement + iconData.offsetY,
                    iconData.textureX,
                    iconData.textureY,
                    iconData.width,
                    iconData.height
            );
        }
    }

    public static EntityIconData getEntityIconData(OWEntity entity) {
        if (entity instanceof KodiakEntity) {
            return new EntityIconData(239, 102, 17, 16, -(17 / 2), -7);
        } else if (entity instanceof TigerEntity) {
            return new EntityIconData(237, 21, 19, 17, -(19 / 2), -8);
        } else if (entity instanceof CrocodileEntity) {
            return new EntityIconData(243, 155, 13, 14, -(13 / 2), -5);
        } else if (entity instanceof OrcaEntity) {
            return new EntityIconData(241, 186, 15, 17, -(15 / 2), -8);
        } else if (entity instanceof BoaEntity) {
            return new EntityIconData(242, 38, 14, 11, -(14 / 2), -2);
        } else if (entity instanceof KangarooEntity) {
            return new EntityIconData(239, 203, 17, 23, -(17 / 2), -14);
        } else if (entity instanceof ElephantEntity) {
            // Vignette la plus large du lot (31 px) : elle déborde des deux côtés de l'ancrage, d'où
            // un décalage horizontal deux fois plus grand que celui du kodiak.
            return new EntityIconData(225, 0, 31, 21, -(31 / 2), -12);
        }
        return null;
    }

    public static void createVitalEnergyBar(GuiGraphics guiGraphics, OWEntity entity, int x, int y) {
        int xPlacement = x / 2 + 10;
        int yPlacement = y - 39;

        guiGraphics.blit(HUD, xPlacement + 81 + 5, yPlacement, 0, 230, 8, 14);

        boolean isActuallySpendingEnergy = entity.isCombo()
                || (entity.isRunning() && entity.isVehicle() && entity.isTame()
                    && entity.getDeltaMovement().horizontalDistanceSqr() > 0.00005);

        if ((entity.tickCount / 5) % 2 == 0 && isActuallySpendingEnergy && ((float) (entity.getVitalEnergy() / entity.getMaxVitalEnergy())) < 0.75) {
            RenderSystem.setShaderColor(0.81f, 0.85f, 0.91f, 0.75f);
            guiGraphics.blit(HUD, xPlacement + 81 + 5 + 1, yPlacement + 1, 1, 244, 6, 12);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        } else guiGraphics.blit(HUD, xPlacement + 81 + 5 + 1, yPlacement + 1, ((float) (entity.getVitalEnergy() / entity.getMaxVitalEnergy())) >= 0.75 && (entity.tickCount / 5) % 2 == 0 ? 13 : 1, 244, 6, 12);

        guiGraphics.blit(HUD, xPlacement + 81 + 5 + 1, yPlacement + 1, 7, 244, 6, (int) (12 * (((float) (entity.getVitalEnergy() / entity.getMaxVitalEnergy())))));
    }

    /**
     * Alerte de pression, affichée sous le bloc de profondeur.
     *
     * <p>Le message existait, était traduit dans les quatorze langues, et n'a jamais été montré : il
     * était construit côté SERVEUR dans {@code OWSemiWaterEntity}, {@code OWWaterEntity} et le
     * submersible, puis abandonné là — l'appel d'affichage y est commenté avec la mention « ne
     * marche pas côté serveur ». Résultat, la monture encaissait des dégâts de pression sans que
     * rien à l'écran ne les explique, et le joueur les attribuait naturellement à sa jauge
     * d'oxygène, encore pleine. Le HUD tourne sur le client et connaît déjà la profondeur : il n'y
     * avait aucune raison de faire voyager quoi que ce soit.</p>
     */
    private static void renderPressureWarning(GuiGraphics guiGraphics, int screenWidth, int tickCount) {
        Component warning = Component.translatable("tooHighPressure");
        int color = (tickCount / 5) % 2 == 0 ? 0xFF4040 : 0xFFD24A;
        int width = Minecraft.getInstance().font.width(warning);
        guiGraphics.drawString(Minecraft.getInstance().font, warning,
                (screenWidth / 2) - width / 2, 41, color, true);
    }

    private static final ResourceLocation AIR_SPRITE = ResourceLocation.withDefaultNamespace("hud/air");
    private static final ResourceLocation AIR_BURSTING_SPRITE = ResourceLocation.withDefaultNamespace("hud/air_bursting");

    /** Rangee de bulles, juste sous l'indicateur de profondeur quand il est affiche. */
    private static final int ENTITY_AIR_ROW_Y_UNDER_DEPTH = 30;
    /** Et a sa place des qu'il disparait : la rangee ne reste pas suspendue dans le vide. */
    private static final int ENTITY_AIR_ROW_Y_ALONE = 10;
    private static final int ENTITY_AIR_BUBBLES = 7;

    private static float airRowY = ENTITY_AIR_ROW_Y_ALONE;
    private static boolean airRowWasVisible = false;
    private static long airRowLastMs = 0L;

    /**
     * Souffle de la monture, en bulles vanilla.
     *
     * <p>L'ancienne jauge etait un carre de 15 px pose juste a gauche du centre, sur la meme bande
     * que la rangee d'armure et les bulles du joueur : elle mordait sur le HUD vanilla des que la
     * barre d'armure etait pleine, et sur n'importe quel HUD moddé occupant cette bande. Le bas de
     * l'ecran est de toute facon sature — coeurs, barre d'experience, vie de monture et bulles du
     * joueur se partagent deja chaque ligne, il n'y restait aucune place franche.</p>
     *
     * <p>Les bulles rejoignent donc le bloc de plongee, en haut, ou vivent deja la profondeur et sa
     * limite : meme sprite et meme regle d'eclatement que vanilla, donc rien a apprendre, et une
     * rangee qui se retracte vers le centre a mesure que l'air manque. Elle ne s'affiche que
     * lorsqu'elle a quelque chose a dire — sous l'eau, ou tant que le souffle n'est pas revenu.</p>
     */
    public static void createEntityAirBubbles(GuiGraphics guiGraphics, OWEntity entity, int screenWidth,
                                              boolean depthShown) {
        int air = entity.getAirSupply();
        int maxAir = entity.getMaxAirSupply();
        if (maxAir <= 0 || (air >= maxAir && !entity.isInWater())) {
            airRowWasVisible = false;
            return;
        }

        int full = Mth.clamp(Mth.ceil((double) (air - 2) * ENTITY_AIR_BUBBLES / maxAir), 0, ENTITY_AIR_BUBBLES);
        int bursting = Mth.clamp(Mth.ceil((double) air * ENTITY_AIR_BUBBLES / maxAir) - full, 0, ENTITY_AIR_BUBBLES - full);
        int shown = full + bursting;
        if (shown <= 0) {
            airRowWasVisible = false;
            return;
        }

        // Le bloc de profondeur ne s'affiche que sous l'eau : à la remontée il disparaît alors que
        // le souffle se refait encore, et la rangée resterait plantée à sa hauteur, seule et trop
        // basse. Elle remonte donc à sa place — en glissant, car un saut sec de vingt pixels au
        // moment précis où l'on crève la surface se remarquerait plus que le décalage lui-même.
        float target = depthShown ? ENTITY_AIR_ROW_Y_UNDER_DEPTH : ENTITY_AIR_ROW_Y_ALONE;
        long now = System.currentTimeMillis();

        if (!airRowWasVisible) {
            airRowY = target;
            airRowWasVisible = true;
        } else {
            float elapsed = Math.min((now - airRowLastMs) / 1000f, 0.1f);
            airRowY = Mth.lerp(Math.min(1f, elapsed * 9f), airRowY, target);
        }
        airRowLastMs = now;

        int rowY = Math.round(airRowY);
        int rowWidth = (shown - 1) * 8 + 9;
        int startX = screenWidth / 2 - rowWidth / 2;

        for (int i = 0; i < shown; i++) {
            guiGraphics.blitSprite(i < full ? AIR_SPRITE : AIR_BURSTING_SPRITE,
                    startX + i * 8, rowY, 9, 9);
        }
    }

    public static class EntityIconData {
        public final int textureX;
        public final int textureY;
        public final int width;
        public final int height;
        public final int offsetX;
        public final int offsetY;

        public EntityIconData(int textureX, int textureY, int width, int height, int offsetX, int offsetY) {
            this.textureX = textureX;
            this.textureY = textureY;
            this.width = width;
            this.height = height;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
    }
}
