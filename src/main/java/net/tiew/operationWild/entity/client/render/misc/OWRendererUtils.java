package net.tiew.operationWild.entity.client.render.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTeamBannerShape;
import net.tiew.operationWild.team.OWTeamMosaicPattern;

import java.util.*;

public class OWRendererUtils {

    private static final ResourceLocation ICONS       = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");
    private static final ResourceLocation OW_TEAMS_GUI = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/ow_teams_interface.png");
    private static final ResourceLocation WHITE        = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/misc/white.png");
    private static final ResourceLocation INFO_OVERLAY = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/overlay/image_information.png");
    private static final ResourceLocation INFO_OVERLAY_TAMED = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/overlay/image_information_tamed.png");
    private static final ResourceLocation INFO_BARS = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/overlay/image_information_bars.png");
    private static final ResourceLocation FOOD_EMPTY_HUNGER = ResourceLocation.withDefaultNamespace("textures/gui/sprites/hud/food_empty_hunger.png");
    private static final ResourceLocation FOOD_FULL = ResourceLocation.withDefaultNamespace("textures/gui/sprites/hud/food_full.png");
    private static final ResourceLocation FOOD_HALF = ResourceLocation.withDefaultNamespace("textures/gui/sprites/hud/food_half.png");
    private static final ResourceLocation FOOD_EMPTY = ResourceLocation.withDefaultNamespace("textures/gui/sprites/hud/food_empty.png");

    // Origine UV (dans ow_teams_banners_styles.png) de la silhouette en cours de rendu, positionnée
    // par renderBannerPattern selon la forme de la bannière. Le rendu 3D est mono-thread (render thread).
    private static int bnUBase = 0, bnVBase = 0;

    public static void displayOverlayOnEntity(OWEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int opacity, double offsetX, double offsetY, double offsetZ) {
        ResourceLocation overlayTexture = INFO_OVERLAY;
        ResourceLocation overlayTextureTamed = INFO_OVERLAY_TAMED;
        ResourceLocation barTexture = INFO_BARS;
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        if (minecraft.options.hideGui) {
            return;
        }

        poseStack.pushPose();

        poseStack.translate(0.0D + offsetX, entity.getBbHeight() / 2 + 0.35D + offsetY, 0.0D + offsetZ);

        poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(2.5F, 2.5F, 2.5F);
        poseStack.translate(0.1D, 0.0D, 0.0D);

        RenderType renderType = RenderType.entityTranslucent(entity.isTame() ? overlayTextureTamed : overlayTexture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        Matrix4f matrix = poseStack.last().pose();

        float width = 1.0F;
        float height = 1.0F;
        float u0 = 0.0F;
        float u1 = 1.0F;
        float v0 = 0.0f;
        float v1 = 1.0f;

        int lightU = packedLight & 0xFFFF;
        int lightV = (packedLight >> 16) & 0xFFFF;

        vertexConsumer.addVertex(matrix, -width / 2, -height / 2, 0.0F).setColor(255, 255, 255, opacity).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        vertexConsumer.addVertex(matrix, width / 2, -height / 2, 0.0F).setColor(255, 255, 255, opacity).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        vertexConsumer.addVertex(matrix, width / 2, height / 2, 0.0F).setColor(255, 255, 255, opacity).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        vertexConsumer.addVertex(matrix, -width / 2, height / 2, 0.0F).setColor(255, 255, 255, opacity).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);




        int fullBrightLight = 0xF000F0;


        float tamingBarProgress = entity.getTamingPercentage() / 100.0F;

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 0.003D);

        VertexConsumer tamingBarVertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(barTexture));
        Matrix4f tamingBarMatrix = poseStack.last().pose();

        float tamingBarScale = 0.004F;
        float tamingBarWidth = 166.0F * tamingBarScale;
        float tamingBarHeight = 10.0F * tamingBarScale;
        float tamingRenderedBarWidth = tamingBarWidth * tamingBarProgress;
        float tamingBarY = entity.isTame() ? 0.12F : 0.2F;

        int fullBrightU = fullBrightLight & 0xFFFF;
        int fullBrightV = (fullBrightLight >> 16) & 0xFFFF;

        tamingBarVertexConsumer.addVertex(tamingBarMatrix, -0.43F, tamingBarY, 0.0F).setColor(255, 255, 255, opacity).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(fullBrightU, fullBrightV).setNormal(0.0F, 1.0F, 0.0F);
        tamingBarVertexConsumer.addVertex(tamingBarMatrix, -0.43F + tamingRenderedBarWidth, tamingBarY, 0.0F).setColor(255, 255, 255, opacity).setUv((166.0F * tamingBarProgress) / 256.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(fullBrightU, fullBrightV).setNormal(0.0F, 1.0F, 0.0F);
        tamingBarVertexConsumer.addVertex(tamingBarMatrix, -0.43F + tamingRenderedBarWidth, tamingBarY - tamingBarHeight, 0.0F).setColor(255, 255, 255, opacity).setUv((166.0F * tamingBarProgress) / 256.0F, 10.0F / 256.0F).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(fullBrightU, fullBrightV).setNormal(0.0F, 1.0F, 0.0F);
        tamingBarVertexConsumer.addVertex(tamingBarMatrix, -0.43F, tamingBarY - tamingBarHeight, 0.0F).setColor(255, 255, 255, opacity).setUv(0.0F, 10.0F / 256.0F).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(fullBrightU, fullBrightV).setNormal(0.0F, 1.0F, 0.0F);

        poseStack.popPose();

        // Second bar: maturation for babies, somnolence for wild — hidden for tamed non-babies
        if (!entity.isTame() || entity.isBaby()) {
            float secondBarProgress = entity.isBaby() ? entity.getMaturationPercentage() / 100.0F : entity.getSleepBarPercent() / 100.0F;

            poseStack.pushPose();
            poseStack.translate(0.0D, 0.0D, 0.003D);

            VertexConsumer secondBarVertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(barTexture));
            Matrix4f secondBarMatrix = poseStack.last().pose();

            float secondBarScale = 0.004F;
            float secondBarWidth = entity.isBaby() ? 159.0F * secondBarScale : 166.0F * secondBarScale;
            float secondBarHeight = entity.isBaby() ? 7.0F * secondBarScale : 10.0F * secondBarScale;
            float secondRenderedBarWidth = secondBarWidth * secondBarProgress;

            float uvTop = entity.isBaby() ? 49.0F / 256.0F : 10.0F / 256.0F;
            float uvBottom = entity.isBaby() ? 56.0F / 256.0F : 20.0F / 256.0F;
            float uvRight = entity.isBaby() ? (159.0F * secondBarProgress) / 256.0F : (166.0F * secondBarProgress) / 256.0F;

            secondBarVertexConsumer.addVertex(secondBarMatrix, -0.43F, 0.12F, 0.0F).setColor(255, 255, 255, opacity).setUv(0.0F, uvTop).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(fullBrightU, fullBrightV).setNormal(0.0F, 1.0F, 0.0F);
            secondBarVertexConsumer.addVertex(secondBarMatrix, -0.43F + secondRenderedBarWidth, 0.12F, 0.0F).setColor(255, 255, 255, opacity).setUv(uvRight, uvTop).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(fullBrightU, fullBrightV).setNormal(0.0F, 1.0F, 0.0F);
            secondBarVertexConsumer.addVertex(secondBarMatrix, -0.43F + secondRenderedBarWidth, 0.12F - secondBarHeight, 0.0F).setColor(255, 255, 255, opacity).setUv(uvRight, uvBottom).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(fullBrightU, fullBrightV).setNormal(0.0F, 1.0F, 0.0F);
            secondBarVertexConsumer.addVertex(secondBarMatrix, -0.43F, 0.12F - secondBarHeight, 0.0F).setColor(255, 255, 255, opacity).setUv(0.0F, uvBottom).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(fullBrightU, fullBrightV).setNormal(0.0F, 1.0F, 0.0F);

            poseStack.popPose();
        }

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 0.003D);

        int iconU = 0;
        int iconV = entity.isMale() ? 48 : 36;
        int iconWidth = 12;
        int iconHeight = 12;

        VertexConsumer iconVertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(ICONS));
        Matrix4f iconMatrix = poseStack.last().pose();

        float iconScale2 = 0.004F;
        float renderedIconWidth = iconWidth * iconScale2;
        float renderedIconHeight = iconHeight * iconScale2;
        float iconX = 0.215F;
        float iconY = 0.47F;

        float minU = iconU / 256.0F;
        float minV = iconV / 256.0F;
        float maxU = (iconU + iconWidth) / 256.0F;
        float maxV = (iconV + iconHeight) / 256.0F;

        iconVertexConsumer.addVertex(iconMatrix, iconX, iconY, 0.0F).setColor(255, 255, 255, opacity).setUv(minU, minV).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        iconVertexConsumer.addVertex(iconMatrix, iconX + renderedIconWidth, iconY, 0.0F).setColor(255, 255, 255, opacity).setUv(maxU, minV).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        iconVertexConsumer.addVertex(iconMatrix, iconX + renderedIconWidth, iconY - renderedIconHeight, 0.0F).setColor(255, 255, 255, opacity).setUv(maxU, maxV).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        iconVertexConsumer.addVertex(iconMatrix, iconX, iconY - renderedIconHeight, 0.0F).setColor(255, 255, 255, opacity).setUv(minU, maxV).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);

        poseStack.popPose();

        // ── Mini team banner (name left-aligned like other lines, banner just right of it) ──
        // Y reference (2.5F scale): "Passif" ≈ +0.336, "Apprivoisement" ≈ +0.252
        // textStartX = lefttextX * textScale = -132 * 0.0035 = -0.462
        if (entity.isTame() && entity.currentTeam != null) {
            OWTeam bTeam = entity.currentTeam;
            int bPrimary   = bTeam.getTeamColor();
            int bSecondary = bTeam.getTeamSecondaryColor();
            int bTertiary = bTeam.getTertiaryColor();
            int bpr = (bPrimary >> 16) & 0xFF,   bpg = (bPrimary >> 8) & 0xFF,   bpb = bPrimary & 0xFF;
            int bsr = (bSecondary >> 16) & 0xFF,  bsg = (bSecondary >> 8) & 0xFF, bsb = bSecondary & 0xFF;
            int btr = (bTertiary >> 16) & 0xFF,   btg = (bTertiary >> 8) & 0xFF,  btb = bTertiary & 0xFF;

            Font bFont = Minecraft.getInstance().font;
            String rawTeamName = bTeam.getTeamName();
            if (rawTeamName.length() > 15) rawTeamName = rawTeamName.substring(0, 15);
            Component bName = Component.literal(rawTeamName)
                    .withStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(bPrimary)));

            // Text world-space metrics (same scale as existing overlay text: 0.0035F)
            final float TEXT_SCALE  = 0.0035f;
            final float textStartX  = -0.462f;  // = lefttextX(-132) * TEXT_SCALE
            final float bcy         = 0.268f;   // vertical center of the row
            final float nameWorldW  = bFont.width(bName) * TEXT_SCALE;

            // Team name — left-aligned, same X as "Passif" / "Vie:" etc.
            poseStack.pushPose();
            poseStack.translate(textStartX, bcy, 0.003f);
            poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
            bFont.drawInBatch(bName, 0, -(float) bFont.lineHeight / 2f, -1, true,
                    poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
            poseStack.popPose();

            // Banner — immediately to the right of the team name
            final float BBW  = 56 * 0.003f * 0.24f;
            final float BBH  = 93 * 0.003f * 0.24f;
            final float bbx0 = textStartX + nameWorldW + 0.010f;
            final float bbx1 = bbx0 + BBW;
            final float bby0 = bcy - BBH / 2f;
            final float bby1 = bcy + BBH / 2f;

            poseStack.pushPose();
            poseStack.translate(0, 0, 0.003f);
            VertexConsumer vcB = bufferSource.getBuffer(RenderType.entityTranslucent(OWTeamBannerShape.TEXTURE));
            renderBannerPattern(vcB, poseStack.last().pose(),
                    bbx0, bby0, bbx1, bby1,
                    bbx0, bby0, BBW, BBH,
                    bTeam.getBannerShape(), bTeam.getTeamMosaicPattern(),
                    bpr, bpg, bpb, bsr, bsg, bsb,
                    btr, btg, btb, bTeam.isUseTertiary(),
                    bTeam.getPaintPixels(), lightU, lightV, true, true);
            poseStack.popPose();
        }

        if (entity instanceof KodiakEntity kodiak && !kodiak.isTame()) {
            poseStack.pushPose();
            poseStack.translate(-0.03D, 0.285D, 0.003D);

            float foodValue = kodiak.getFoodBarValue();
            int fullDrumsticks = (int) (foodValue / 2.0F);
            boolean hasHalfDrumstick = (foodValue % 2.0F) >= 1.0F;

            float iconScale = 0.004F;
            float drumstickWidth = 9.0F * iconScale;
            float drumstickHeight = 9.0F * iconScale;
            float startX = -0.43F;
            float startY = 0.04F;
            float spacing = 0.038F;

            for (int i = 0; i < 5; i++) {
                float x = startX + (i * spacing);

                ResourceLocation spriteLocation;

                if (kodiak.isHungry()) {
                    spriteLocation = FOOD_EMPTY_HUNGER;
                } else {
                    int reverseIndex = 4 - i;

                    if (reverseIndex < fullDrumsticks) {
                        spriteLocation = FOOD_FULL;
                    } else if (reverseIndex == fullDrumsticks && hasHalfDrumstick) {
                        spriteLocation = FOOD_HALF;
                    } else {
                        spriteLocation = FOOD_EMPTY;
                    }
                }

                VertexConsumer foodBarVertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(spriteLocation));
                Matrix4f foodBarMatrix = poseStack.last().pose();

                foodBarVertexConsumer.addVertex(foodBarMatrix, x, startY, 0.0F).setColor(255, 255, 255, opacity).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(fullBrightU, fullBrightV).setNormal(0.0F, 1.0F, 0.0F);
                foodBarVertexConsumer.addVertex(foodBarMatrix, x + drumstickWidth, startY, 0.0F).setColor(255, 255, 255, opacity).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(fullBrightU, fullBrightV).setNormal(0.0F, 1.0F, 0.0F);
                foodBarVertexConsumer.addVertex(foodBarMatrix, x + drumstickWidth, startY - drumstickHeight, 0.0F).setColor(255, 255, 255, opacity).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(fullBrightU, fullBrightV).setNormal(0.0F, 1.0F, 0.0F);
                foodBarVertexConsumer.addVertex(foodBarMatrix, x, startY - drumstickHeight, 0.0F).setColor(255, 255, 255, opacity).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(fullBrightU, fullBrightV).setNormal(0.0F, 1.0F, 0.0F);
            }

            poseStack.popPose();
        }

        poseStack.scale(0.0035F, -0.0035F, 0.0035F);
        poseStack.translate(0.0D, 0.0D, 0.003D);

        Component healthComponent = Component.empty()
                .append(Component.translatable("imageHealth").withStyle(style -> style.withColor(0x8e9eb9).withBold(true)))
                .append(" " + Math.round(entity.getHealth() * 2.0f) / 2.0f + " / " + Math.round(entity.getMaxHealth() * 2.0f) / 2.0f);

        Component damagesComponent = Component.empty()
                .append(Component.translatable("imageDamages").withStyle(style -> style.withColor(0x8e9eb9).withBold(true)))
                .append(" " + Math.round(entity.getDamageToClient() * 10.0f) / 10.0f);

        Component speedComponent = Component.empty()
                .append(Component.translatable("imageSpeed").withStyle(style -> style.withColor(0x8e9eb9).withBold(true)))
                .append(" " + Math.round(OWUtils.getSpeedBlocksPerSecond(entity) * 100) / 100.0)
                .append(Component.translatable("tooltip.entitySpeed").withStyle(style -> style.withBold(false)));

        Component stateComponent = Component.empty()
                .append(Component.translatable(entity.isPassive() ? "tooltip.modePassive" : "tooltip.modeAggressive").withStyle(style -> style.withColor(entity.isPassive() ? 0x55FF55 : 0xFF5555).withBold(true)));

        Component tamingComponent = Component.empty()
                .append(Component.translatable("imageTaming").withStyle(style -> style.withColor(0x8e9eb9).withBold(true)))
                .append(" " + Math.round(entity.getTamingPercentage() * 10.0f) / 10.0f + "%");


        String timeDisplay = "";

        if (entity.isBaby()) {
            if (entity instanceof OWEntity owEntity) {
                int timeRemaining = (int) (owEntity.maxMaturation - owEntity.actualMaturation);
                int secondsRemaining = timeRemaining / 20;

                if (secondsRemaining >= 3600) {
                    int hours = secondsRemaining / 3600;
                    int minutes = (secondsRemaining % 3600) / 60;
                    int seconds = secondsRemaining % 60;
                    timeDisplay = hours + "h " + minutes + "m " + seconds + "s";
                } else if (secondsRemaining >= 60) {
                    int minutes = secondsRemaining / 60;
                    int seconds = secondsRemaining % 60;
                    timeDisplay = minutes + "m " + seconds + "s";
                } else {
                    timeDisplay = secondsRemaining + "s";
                }
            }

        }

        float percentage = entity.isBaby() ? entity.getMaturationPercentage() : entity.getSleepBarPercent();

        // Somnolence only relevant for wild or baby entities
        Component sleepingComponent = (!entity.isTame() || entity.isBaby()) ? Component.empty()
                .append(Component.translatable(entity.isBaby() ? "imageMaturation" : "sleepingTaming").withStyle(style -> style.withColor(0x8e9eb9).withBold(true)))
                .append(" " + Math.round(percentage * 10.0f) / 10.0f + "%")
                .append(entity.isBaby() ? Component.literal(" | ").withStyle(Style.EMPTY.withItalic(false)) : Component.empty())
                .append(entity.isBaby() ? Component.literal("(" + timeDisplay + ")").withStyle(Style.EMPTY.withItalic(true)) : Component.empty())
                : null;

        Component animalTypeComponent = Component.empty()
                .append(Component.translatable(chooseAnimalMaturation(entity)).withStyle(style -> style.withColor(entity.getEntityColor()).withBold(true)))
                .append(entity.isBaby() ? " " : "")
                .append(Component.translatable(chooseAnimalType(entity)).withStyle(style -> style.withColor(entity.getEntityColor()).withBold(true)));

        Component animalLevelComponent = Component.empty()
                .append(Component.translatable("tooltip.lvlImage").withStyle(style -> style.withColor(0x8e9eb9).withBold(true)))
                .append(Component.literal(String.valueOf(entity.getLevel())).withStyle(style -> style.withColor(entity.getLevel() >= 50 ? 0xdd9847 : entity.getLevelPoints() > 0 ? 0xb8e45a : 0x8e9eb9).withBold(false)));

        String ownerName = "";
        if (entity.getOwnerUUID() != null) {
            Player onlineOwner = Minecraft.getInstance().level != null
                    ? Minecraft.getInstance().level.getPlayerByUUID(entity.getOwnerUUID())
                    : null;
            ownerName = onlineOwner != null ? onlineOwner.getName().getString() : entity.getCachedOwnerName();
        }

        Component animalOwnerComponent = Component.empty()
                .append(Component.translatable("tooltip.ownerImage").withStyle(style -> style.withColor(0x8e9eb9).withBold(true)))
                .append(Component.literal(ownerName).withStyle(style -> style.withColor(0x8e9eb9).withBold(false)));

        float lefttextX = -132;
        float righttextX = 60;
        float textY = -132;
        int leftPadding = 12;
        int rightPadding = 20;

        float tamingTextWidth = font.width(tamingComponent);
        float sleepingTextWidth = sleepingComponent != null ? font.width(sleepingComponent) : 0;

        float centerX = 0;
        float tamingTextX = centerX - (tamingTextWidth / 2.0f);
        float sleepingTextX = centerX - (sleepingTextWidth / 2.0f);

        float animalTypeTextWidth = font.width(animalTypeComponent);
        float animalTypeTextX = righttextX - animalTypeTextWidth;

        float animalLevelTextWidth = font.width(animalLevelComponent);
        float animalLevelTextX = righttextX - animalLevelTextWidth;

        float animalOwnerTextWidth = font.width(animalOwnerComponent);
        float animalOwnerTextX = righttextX - animalOwnerTextWidth;

        font.drawInBatch(healthComponent, lefttextX, textY, 0x8e9eb9, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
        font.drawInBatch(damagesComponent, lefttextX, textY + (leftPadding * 1), 0x8e9eb9, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
        font.drawInBatch(speedComponent, lefttextX, textY + (leftPadding * 2), 0x8e9eb9, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);

        if (entity.isTame()) {
            font.drawInBatch(stateComponent, lefttextX, textY + (leftPadding * 3), entity.isPassive() ? 0x55FF55 : 0xFF5555, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
        }

        font.drawInBatch(tamingComponent, tamingTextX - 25, entity.isTame() ? textY + (20 * 4.4f) : textY + (20 * 3), 0x8e9eb9, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
        if (sleepingComponent != null) font.drawInBatch(sleepingComponent, sleepingTextX - 25, textY + (20 * 4.4f), 0x8e9eb9, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);

        font.drawInBatch(animalTypeComponent, animalTypeTextX, textY, 0x8e9eb9, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);

        font.drawInBatch(animalLevelComponent, animalLevelTextX + 11, textY + (rightPadding * 1), entity.getLevelPoints() > 0 ? 0xb8e45a : 0x8e9eb9, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);

        if (entity.getOwner() != null) font.drawInBatch(animalOwnerComponent, animalOwnerTextX + 11, textY + (rightPadding * 2), 0x8e9eb9, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);

        poseStack.popPose();

    }

    private static String chooseAnimalMaturation(OWEntity entity) {
        if (!entity.isBaby()) return "";
        return entity.getMaturationPercentage() >= 60 ? "tooltip.adolescent" : (entity.getMaturationPercentage() >= 20 ? "tooltip.juvenile" : "tooltip.baby");
    }

    private static String chooseAnimalType(OWEntity entity) {
        switch (entity.getClass().getSimpleName()) {
            case "TigerEntity" -> {
                return "entity.ow.tiger";
            }
            case "BoaEntity" -> {
                return "entity.ow.boa";
            }
            case "PeacockEntity" -> {
                return "entity.ow.peacock";
            }
            case "TigerSharkEntity" -> {
                return "entity.ow.tiger_shark";
            }
            case "ChameleonEntity" -> {
                return "entity.ow.chameleon";
            }
            case "HyenaEntity" -> {
                return "entity.ow.hyena";
            }
            case "JellyfishEntity" -> {
                return "entity.ow.jellyfish";
            }
            case "KodiakEntity" -> {
                return "entity.ow.kodiak";
            }
            case "MantaEntity" -> {
                return "entity.ow.manta";
            }
            case "RedPandaEntity" -> {
                return "entity.ow.red_panda";
            }
            case "WalrusEntity" -> {
                return "entity.ow.walrus";
            }
            case "ElephantEntity" -> {
                return "entity.ow.elephant";
            }
            case "MandrillEntity" -> {
                return "entity.ow.mandrill";
            }
            case "CrocodileEntity" -> {
                return "entity.ow.crocodile";
            }
            case "LionEntity" -> {
                return "entity.ow.lion";
            }
            default -> {
                return "entity.ow.tiger";
            }
        }
    }

    public static void displayLevelAboveEntity(OWEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, EntityRenderDispatcher entityRenderDispatcher, double upOffset) {
        int textColor = 0xdfdfdf;
        int levelColor = entity.getLevel() >= 50 ? 0xdd9847 : entity.getLevelPoints() > 0 ? 0xb8e45a : 0xFFFFFF;

        Font font = Minecraft.getInstance().font;
        EntityTexts texts = textsOf(entity);

        if (texts.nickname.refresh(java.util.Objects.hash(
                entity.getNickname(), entity.getEntityColor(), languageSignature()))) {
            texts.nickname.set(font, Component.literal(entity.getNickname())
                    .withStyle(Style.EMPTY.withBold(true))
                    .withColor(TextColor.fromRgb(entity.getEntityColor()).getValue()));
            texts.spaceWidth = font.width(" ");
        }

        if (texts.level.refresh(java.util.Objects.hash(entity.getLevel(), levelColor, languageSignature()))) {
            texts.level.set(font, Component.translatable("tooltip.lvl",
                    Component.literal(String.valueOf(entity.getLevel()))
                            .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(levelColor).getValue()).withBold(true))));
        }

        poseStack.pushPose();
        poseStack.translate(0, entity.getBbHeight() + 0.75f + upOffset, 0);
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.scale(0.025F, -0.025F, 0.025F);

        float scale = 0.65F;
        float t0Width = texts.nickname.width;

        float totalWidth = t0Width + texts.spaceWidth + texts.level.width * scale;
        float startX = -totalWidth / 2f;

        font.drawInBatch(texts.nickname.text, startX, 0, -1, true, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);

        float t1StartX = startX + t0Width + texts.spaceWidth;
        poseStack.pushPose();
        poseStack.translate(t1StartX, 2f, 0f);
        poseStack.scale(scale, scale, scale);
        font.drawInBatch(texts.level.text, 0, 0, -1, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
        poseStack.popPose();

        poseStack.popPose();
    }

    public static void displayResurrectionTimeEntity(OWEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, EntityRenderDispatcher entityRenderDispatcher, int opacity, float yOffsetMultiplier) {
        ResourceLocation barTexture = INFO_BARS;
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        int textColor = 0xdfdfdf;
        int valueColor = 0xb1b1b1;

        double percentage = Math.round(entity.getResurrectionPercentage() * 10) / 10.0;
        int timeRemaining = entity.getResurrectionMaxTimer() - entity.resurrectionTimer;
        int secondsRemaining = timeRemaining / 20;

        String timeDisplay;
        if (secondsRemaining >= 3600) {
            int hours = secondsRemaining / 3600;
            int minutes = (secondsRemaining % 3600) / 60;
            int seconds = secondsRemaining % 60;
            timeDisplay = hours + "h " + minutes + "m " + seconds + "s";
        } else if (secondsRemaining >= 60) {
            int minutes = secondsRemaining / 60;
            int seconds = secondsRemaining % 60;
            timeDisplay = minutes + "m " + seconds + "s";
        } else {
            timeDisplay = secondsRemaining + "s";
        }

        Component resurrectionText = Component.translatable("tooltip.resurrection")
                .setStyle(Style.EMPTY.withItalic(true).withColor(0x00ffaf));

        Component percentageText = Component.literal(percentage + "%")
                .withStyle(Style.EMPTY.withBold(true).withItalic(true))
                .withColor(TextColor.fromRgb(0x00ffaf).getValue());

        Component timeText = Component
                .translatable("tooltip.resurrectionTime")
                .withStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(textColor)))
                .append(Component.literal(timeDisplay)
                        .withStyle(Style.EMPTY.withBold(false).withItalic(true).withColor(TextColor.fromRgb(valueColor))));

        poseStack.pushPose();

        poseStack.translate(0, entity.getBbHeight() + 0.6F, 0);
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.scale(0.025F, -0.025F, 0.025F);

        Matrix4f textMatrix = poseStack.last().pose();

        float resurrectionWidth = (float)(-font.width(resurrectionText) / 2);
        float percentageWidth = (float)(-font.width(percentageText) / 2);
        float timeWidth = (float)(-font.width(timeText) / 2);

        font.drawInBatch(resurrectionText, resurrectionWidth, -15 + yOffsetMultiplier, -1, false, textMatrix, bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
        font.drawInBatch(percentageText, percentageWidth, 15 + yOffsetMultiplier, -1, false, textMatrix, bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
        font.drawInBatch(timeText, timeWidth, 1 + yOffsetMultiplier, -1, false, textMatrix, bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 0.001D);

        float barProgress = (float)(percentage / 100.0);

        int bgBarU = 0;
        int bgBarV = 39;
        int bgBarWidth = 159;
        int bgBarHeight = 7;

        int fgBarU = 0;
        int fgBarV = 33;
        int fgBarWidth = 157;
        int fgBarHeight = 5;

        float bgMinU = bgBarU / 256.0F;
        float bgMinV = bgBarV / 256.0F;
        float bgMaxU = bgMinU + bgBarWidth / 256.0F;
        float bgMaxV = bgMinV + bgBarHeight / 256.0F;

        float fgMinU = fgBarU / 256.0F;
        float fgMinV = fgBarV / 256.0F;
        float fgMaxU = fgMinU + (fgBarWidth * barProgress) / 256.0F;
        float fgMaxV = fgMinV + fgBarHeight / 256.0F;

        VertexConsumer barVertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(barTexture));
        Matrix4f barMatrix = poseStack.last().pose();
        int lightU = packedLight & 0xFFFF;
        int lightV = (packedLight >> 16) & 0xFFFF;

        float barXOffset = -80.0F;
        float barYOffset = 30.0F + yOffsetMultiplier;
        float renderedFgBarWidth = fgBarWidth * barProgress;

        barVertexConsumer.addVertex(barMatrix, barXOffset, barYOffset, 0.0F).setColor(255, 255, 255, opacity).setUv(bgMinU, bgMinV).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        barVertexConsumer.addVertex(barMatrix, barXOffset + bgBarWidth, barYOffset, 0.0F).setColor(255, 255, 255, opacity).setUv(bgMaxU, bgMinV).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        barVertexConsumer.addVertex(barMatrix, barXOffset + bgBarWidth, barYOffset + bgBarHeight, 0.0F).setColor(255, 255, 255, opacity).setUv(bgMaxU, bgMaxV).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        barVertexConsumer.addVertex(barMatrix, barXOffset, barYOffset + bgBarHeight, 0.0F).setColor(255, 255, 255, opacity).setUv(bgMinU, bgMaxV).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);

        float fgBarXOffset = barXOffset + (bgBarWidth - fgBarWidth) / 2.0F;
        float fgBarYOffset = barYOffset + (bgBarHeight - fgBarHeight) / 2.0F;

        barVertexConsumer.addVertex(barMatrix, fgBarXOffset, fgBarYOffset, 0.001F).setColor(255, 255, 255, opacity).setUv(fgMinU, fgMinV).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        barVertexConsumer.addVertex(barMatrix, fgBarXOffset + renderedFgBarWidth, fgBarYOffset, 0.001F).setColor(255, 255, 255, opacity).setUv(fgMaxU, fgMinV).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        barVertexConsumer.addVertex(barMatrix, fgBarXOffset + renderedFgBarWidth, fgBarYOffset + fgBarHeight, 0.001F).setColor(255, 255, 255, opacity).setUv(fgMaxU, fgMaxV).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        barVertexConsumer.addVertex(barMatrix, fgBarXOffset, fgBarYOffset + fgBarHeight, 0.001F).setColor(255, 255, 255, opacity).setUv(fgMinU, fgMaxV).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);

        poseStack.popPose();

        poseStack.popPose();
    }

    public static void renderVerticalBar(ResourceLocation texture, float textureX, float textureY, float textureWidth, float textureHeight, float barWidth, float barHeight, float fillPercentage, float posX, float posY, float posZ, int opacity, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(posX, posY, posZ);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(texture));
        Matrix4f matrix = poseStack.last().pose();

        float renderedHeight = barHeight * fillPercentage;
        float u0 = textureX / 256.0F;
        float u1 = (textureX + textureWidth) / 256.0F;
        float v0 = (textureY + textureHeight * (1.0F - fillPercentage)) / 256.0F;
        float v1 = (textureY + textureHeight) / 256.0F;

        int lightU = packedLight & 0xFFFF;
        int lightV = (packedLight >> 16) & 0xFFFF;

        vertexConsumer.addVertex(matrix, 0.0F, 0.0F, 0.0F).setColor(255, 255, 255, opacity).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        vertexConsumer.addVertex(matrix, barWidth, 0.0F, 0.0F).setColor(255, 255, 255, opacity).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        vertexConsumer.addVertex(matrix, barWidth, renderedHeight, 0.0F).setColor(255, 255, 255, opacity).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        vertexConsumer.addVertex(matrix, 0.0F, renderedHeight, 0.0F).setColor(255, 255, 255, opacity).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);

        poseStack.popPose();
    }

    public static void displayImageAboveEntity(ResourceLocation image, float positionX, float positionY, float elementScale, float imageScale, float rightOffset, float upOffset, OWEntity entity, PoseStack poseStack, MultiBufferSource buffer, int packedLight, boolean followCamera) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        Component tooltip = Component.translatable("tooltip.lvl");
        int textWidthPx = font.width(tooltip);
        float offsetX = textWidthPx * (1.0F / 9.0F);
        poseStack.pushPose();
        poseStack.translate(0.0D, entity.getBbHeight() + 0.5D, 0.0D);
        if (followCamera) poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        else {
            Quaternionf cameraOrientation = minecraft.getEntityRenderDispatcher().cameraOrientation();
            float yaw = cameraOrientation.getEulerAnglesYXZ(new Vector3f()).y;
            poseStack.mulPose(Axis.YP.rotation(yaw));
        }
        poseStack.scale(0.25F, 0.25F, 0.25F);
        poseStack.translate(offsetX - 1.25f + rightOffset, 0.1D + upOffset, 0.0D);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(image));
        Matrix4f matrix = poseStack.last().pose();
        float width = 1.0F;
        float height = 1.0F;
        float u0 = positionX / imageScale;
        float u1 = u0 + elementScale / imageScale;
        float v0 = positionY / imageScale;
        float v1 = v0 + elementScale / imageScale;
        int lightU = packedLight & 0xFFFF;
        int lightV = (packedLight >> 16) & 0xFFFF;
        vertexConsumer.addVertex(matrix, -width / 2, -height / 2, 0.0F).setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        vertexConsumer.addVertex(matrix, width / 2, -height / 2, 0.0F).setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        vertexConsumer.addVertex(matrix, width / 2, height / 2, 0.0F).setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        vertexConsumer.addVertex(matrix, -width / 2, height / 2, 0.0F).setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        poseStack.popPose();
    }

    public static void displayImageAboveEntity(ResourceLocation image, float positionX, float positionY, float elementScaleX, float elementScaleY, float imageScale, float scale, float rightOffset, float upOffset, float zOffset, OWEntity entity, PoseStack poseStack, MultiBufferSource buffer, int packedLight, boolean followCamera) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        Component tooltip = Component.translatable("tooltip.lvl");
        int textWidthPx = font.width(tooltip);
        float offsetX = textWidthPx * (1.0F / 9.0F);
        poseStack.pushPose();
        poseStack.translate(0.0D, entity.getBbHeight() + 0.5D, 0.0D);
        if (followCamera) poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        else {
            Quaternionf cameraOrientation = minecraft.getEntityRenderDispatcher().cameraOrientation();
            float yaw = cameraOrientation.getEulerAnglesYXZ(new Vector3f()).y;
            poseStack.mulPose(Axis.YP.rotation(yaw));
        }
        poseStack.scale(0.25F * scale, 0.25F * scale, 0.25F * scale);
        poseStack.translate(offsetX - 1.25f + rightOffset, 0.1D + upOffset, zOffset);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(image));
        Matrix4f matrix = poseStack.last().pose();
        float width = 1.0F;
        float height = 1.0F;
        float u0 = positionX / imageScale;
        float u1 = u0 + elementScaleX / imageScale;
        float v0 = positionY / imageScale;
        float v1 = v0 + elementScaleY / imageScale;
        int lightU = packedLight & 0xFFFF;
        int lightV = (packedLight >> 16) & 0xFFFF;
        vertexConsumer.addVertex(matrix, -width / 2, -height / 2, 0.0F).setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        vertexConsumer.addVertex(matrix, width / 2, -height / 2, 0.0F).setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        vertexConsumer.addVertex(matrix, width / 2, height / 2, 0.0F).setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        vertexConsumer.addVertex(matrix, -width / 2, height / 2, 0.0F).setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        poseStack.popPose();
    }

    public static void displayOwnerAboveEntity(OWEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, EntityRenderDispatcher entityRenderDispatcher, double upOffset) {
        if (entity.getOwnerUUID() == null) return;

        String ownerName;
        Player onlineOwner = Minecraft.getInstance().level != null
                ? Minecraft.getInstance().level.getPlayerByUUID(entity.getOwnerUUID())
                : null;
        ownerName = onlineOwner != null ? onlineOwner.getName().getString() : entity.getCachedOwnerName();

        if (ownerName == null || ownerName.isEmpty()) return;

        int textColor = 0xdfdfdf;
        int ownerColor = 0xFFFFFF;

        Font font = Minecraft.getInstance().font;
        InfoText cached = textsOf(entity).owner;

        if (cached.refresh(java.util.Objects.hash(ownerName, languageSignature()))) {
            Component owner = Component.literal(ownerName)
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(ownerColor).getValue()).withBold(true));
            cached.set(font, Component.translatable("tooltip.owner", owner)
                    .withStyle(Style.EMPTY).withColor(TextColor.fromRgb(textColor).getValue()));
        }

        poseStack.pushPose();
        poseStack.translate(0, entity.getBbHeight() + 0.5F + upOffset, 0);
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.scale(0.0175F, -0.0175F, 0.0175F);
        Matrix4f matrix4f = poseStack.last().pose();
        font.drawInBatch(cached.text, (float) (-cached.width / 2), 0, -1, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
        poseStack.popPose();
    }

    public static void displayBonusPointAboveEntity(OWEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, EntityRenderDispatcher entityRenderDispatcher, double offsetY) {
        int textColor = 0xdfdfdf;
        Component text = Component.translatable("tooltip.lvlBonusPoints", entity.getHealth() < entity.getMaxHealth() / 2 ? 0 : (int) ((entity.getHealth() - (entity.getMaxHealth() / 2)) / (entity.getMaxHealth() / 10))).withStyle(Style.EMPTY).withColor(TextColor.fromRgb(textColor).getValue());
        poseStack.pushPose();
        poseStack.translate(0, entity.getBbHeight() + offsetY, 0);
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.scale(0.02F, -0.02F, 0.02F);
        Matrix4f matrix4f = poseStack.last().pose();
        Font font = Minecraft.getInstance().font;
        float textWidth = (float)(-font.width(text) / 2);
        font.drawInBatch(text, textWidth, 0, -1, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
        poseStack.popPose();
    }

    public static void displayTimeLeftBeforeBabyTaskAboveEntity(OWEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, EntityRenderDispatcher entityRenderDispatcher, double offsetY) {
        int textColor = 0xdfdfdf;
        int valueColor = 0xFFFFFF;

        int minutes_before_task = (OWEntity.DELAY_BEFORE_BABY_TASK_MAX - entity.delayBeforeBabyTask) / 1200;
        int seconds_before_task = (OWEntity.DELAY_BEFORE_BABY_TASK_MAX - entity.delayBeforeBabyTask) % 1200 / 20;

        Component timeLeft = Component.literal(String.valueOf(minutes_before_task)).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(valueColor)))
                .append(Component.literal("m ").withStyle(Style.EMPTY.withBold(false).withColor(TextColor.fromRgb(textColor))))
                .append(Component.literal(String.valueOf(seconds_before_task)).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(valueColor))))
                .append(Component.literal("s").withStyle(Style.EMPTY.withBold(false).withColor(TextColor.fromRgb(textColor))));

        Component text = Component.translatable("tooltip.delayBeforeBabyTask", timeLeft).withStyle(Style.EMPTY.withBold(true).withItalic(true)).withColor(TextColor.fromRgb(textColor).getValue());

        poseStack.pushPose();
        poseStack.translate(0, entity.getBbHeight() + 1f + offsetY, 0);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(0.02F, -0.02F, 0.02F);
        Matrix4f matrix4f = poseStack.last().pose();
        Font font = Minecraft.getInstance().font;

        if (entity.babyQuestIsInProgress) {
            int animalColor = entity.getEntityColor();

            Component animalMaturation = Component.translatable(chooseAnimalMaturation(entity))
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(animalColor)).withBold(true));
            Component animalName = Component.translatable(chooseAnimalType(entity))
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(animalColor)).withBold(true));

            Component wantText = Component.translatable("quest.babyQuest0")
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(textColor)).withBold(false));

            Component questTextWithoutItem = animalMaturation.copy().append(" ").append(animalName).append(" ").append(wantText);

            float textWidth = (float)(-font.width(questTextWithoutItem) / 2);

            font.drawInBatch(questTextWithoutItem, textWidth, 0, -1, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);

            if (entity.choosenFood != null) {
                ItemStack itemStack = entity.choosenFood.getDefaultInstance();
                if (!itemStack.isEmpty()) {
                    poseStack.pushPose();

                    float itemX = textWidth + font.width(questTextWithoutItem) + 15;
                    float itemY = -2;

                    poseStack.scale(25F, 25F, 25F);
                    poseStack.translate(itemX / 25F, itemY / 25F, 0);

                    poseStack.mulPose(Axis.ZP.rotationDegrees(180F));
                    poseStack.mulPose(Axis.YP.rotationDegrees(180F));

                    RenderSystem.disableBlend();
                    RenderSystem.enableDepthTest();
                    Minecraft.getInstance().getItemRenderer().renderStatic(itemStack, ItemDisplayContext.GUI, packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, entity.level(), 0);
                    RenderSystem.enableBlend();

                    poseStack.popPose();
                }
            }

        } else {
            float textWidth = (float)(-font.width(text) / 2);
            font.drawInBatch(text, textWidth, 0, -1, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
        }

        poseStack.popPose();
    }


    // ── Team banner ──────────────────────────────────────────────────────────────

    /**
     * Renders a mini team banner centered above the entity.
     * Uses the flag texture (OW_TEAMS_GUI) with vertex color tinting, matching the GUI approach.
     */
    public static void displayTeamBannerAboveEntity(OWEntity entity, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight,
            EntityRenderDispatcher entityRenderDispatcher, double upOffset) {
        if (entity.currentTeam == null) return;

        OWTeam team = entity.currentTeam;
        int primary = team.getTeamColor();

        poseStack.pushPose();
        poseStack.translate(0, entity.getBbHeight() + upOffset, 0);
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());

        final float BW = 56 * 0.003f, BH = 93 * 0.003f;
        final float x0 = -BW / 2f, x1 = BW / 2f;
        final float y0 = -BH / 2f, y1 = BH / 2f;

        VertexConsumer vc = bufferSource.getBuffer(RenderType.entityTranslucent(OWTeamBannerShape.TEXTURE));
        renderTeamBanner(vc, poseStack.last().pose(), team, x0, y0, x1, y1, packedLight, true, true);

        // ── Team name below the banner ─────────────────────────────────────────
        Font font = Minecraft.getInstance().font;
        InfoText cached = textsOf(entity).teamName;

        if (cached.refresh(java.util.Objects.hash(team.getTeamName(), primary, languageSignature()))) {
            cached.set(font, Component.literal(team.getTeamName())
                    .withStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(primary))));
        }

        poseStack.pushPose();
        poseStack.translate(0, y0 - 0.04f, 0);
        poseStack.scale(0.012f, -0.012f, 0.012f);
        font.drawInBatch(cached.text, -cached.width / 2f, 0, -1, true,
                poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
        poseStack.popPose();

        poseStack.popPose();
    }

    /**
     * Dessine la bannière d'une tribu dans le rectangle [x0..x1] × [y0..y1] du repère courant
     * (Y vers le haut, quad en Z = 0), forme, motif et couleurs comprises.
     *
     * <p>Point d'entrée commun à tous les rendus 3D de bannière : la vignette au-dessus de
     * l'entité comme la toile du drapeau porté
     * ({@link net.tiew.operationWild.entity.client.layer.OWTribeFlagLayer}). Le tampon doit
     * avoir été obtenu sur la texture {@link OWTeamBannerShape#TEXTURE}.</p>
     *
     * @param highlights superpose le calque d'ombrage de la forme. Pertinent sur une vignette vue
     *                   de face, à désactiver sur une toile portée : ce calque est peint pour une
     *                   bannière droite et immobile, il contredit l'ondulation et l'éclairage réel.
     */
    /**
     * Textes affichés au-dessus de chaque bête, mémorisés sous leur forme finale.
     *
     * <p>Un {@code Component} traduit est redécomposé à chaque mesure et à chaque tracé : pour trois
     * lignes par créature, à soixante images par seconde et vingt créatures à l'écran, cela fait
     * sept mille décompositions par seconde. On conserve donc la séquence déjà décomposée et sa
     * largeur, réévaluées seulement quand la donnée affichée change — la langue sélectionnée fait
     * partie de la signature, un changement de langue régénère tout.</p>
     */
    private static final class InfoText {
        int signature = Integer.MIN_VALUE;
        FormattedCharSequence text;
        float width;

        boolean refresh(int newSignature) {
            if (newSignature == signature) return false;
            signature = newSignature;
            return true;
        }

        void set(Font font, Component component) {
            text = component.getVisualOrderText();
            width = font.width(text);
        }
    }

    private static final class EntityTexts {
        final InfoText owner = new InfoText();
        final InfoText nickname = new InfoText();
        final InfoText level = new InfoText();
        final InfoText teamName = new InfoText();
        float spaceWidth;
    }

    private static final java.util.Map<OWEntity, EntityTexts> ENTITY_TEXTS = new java.util.WeakHashMap<>();

    private static EntityTexts textsOf(OWEntity entity) {
        return ENTITY_TEXTS.computeIfAbsent(entity, e -> new EntityTexts());
    }

    private static int languageSignature() {
        return Minecraft.getInstance().getLanguageManager().getSelected().hashCode();
    }

    private static final int PAINT_W = 55, PAINT_H = 93;
    private static final java.util.Map<byte[], int[]> PAINT_RECTS = new java.util.WeakHashMap<>();

    /**
     * Découpe une peinture libre en rectangles maximaux de teinte uniforme. Le tableau retourné
     * enchaîne des quintuplets {@code (col0, row0, col1, row1, teinte)}, bornes hautes exclues.
     *
     * <p>Mémorisé sur l'instance du tableau de pixels : {@code setPaintPixels} remplace la
     * référence à chaque repeinte, une bannière modifiée est donc recalculée d'elle-même.</p>
     */
    private static int[] paintRectangles(byte[] pixels) {
        int[] cached = PAINT_RECTS.get(pixels);
        if (cached != null) return cached;

        boolean[] used = new boolean[PAINT_W * PAINT_H];
        int[] rects = new int[PAINT_W * PAINT_H * 5];
        int count = 0;

        for (int row = 0; row < PAINT_H; row++) {
            for (int col = 0; col < PAINT_W; col++) {
                int index = row * PAINT_W + col;
                if (used[index]) continue;

                int tint = pixels[index] & 3;

                int col1 = col + 1;
                while (col1 < PAINT_W && !used[row * PAINT_W + col1]
                        && (pixels[row * PAINT_W + col1] & 3) == tint) col1++;

                int row1 = row + 1;
                while (row1 < PAINT_H && rowMatches(pixels, used, row1, col, col1, tint)) row1++;

                for (int r = row; r < row1; r++) {
                    for (int c = col; c < col1; c++) used[r * PAINT_W + c] = true;
                }

                rects[count++] = col;
                rects[count++] = row;
                rects[count++] = col1;
                rects[count++] = row1;
                rects[count++] = tint;
            }
        }

        int[] trimmed = java.util.Arrays.copyOf(rects, count);
        PAINT_RECTS.put(pixels, trimmed);
        return trimmed;
    }

    private static boolean rowMatches(byte[] pixels, boolean[] used, int row, int col0, int col1, int tint) {
        for (int col = col0; col < col1; col++) {
            int index = row * PAINT_W + col;
            if (used[index] || (pixels[index] & 3) != tint) return false;
        }
        return true;
    }

    public static void renderTeamBanner(VertexConsumer vc, Matrix4f mat, OWTeam team,
            float x0, float y0, float x1, float y1, int packedLight, boolean highlights) {
        renderTeamBanner(vc, mat, team, x0, y0, x1, y1, packedLight, highlights, false);
    }

    /**
     * @param mergePaint fusionne les pixels contigus de même teinte d'une peinture libre en
     *                   rectangles. Le résultat à l'écran est identique — même couverture, même
     *                   couleur, même échantillonnage linéaire de la silhouette — mais une bannière
     *                   passe de 5115 quads à quelques dizaines. À laisser à {@code false} sur une
     *                   toile ondulante : sa tesselation dépend de la taille des quads reçus, et
     *                   fusionner changerait la finesse de l'ondulation.
     */
    public static void renderTeamBanner(VertexConsumer vc, Matrix4f mat, OWTeam team,
            float x0, float y0, float x1, float y1, int packedLight, boolean highlights,
            boolean mergePaint) {
        int primary   = team.getTeamColor();
        int secondary = team.getTeamSecondaryColor();
        int tertiary  = team.getTertiaryColor();
        int pr = (primary >> 16) & 0xFF,   pg = (primary >> 8) & 0xFF,   pb = primary & 0xFF;
        int sr = (secondary >> 16) & 0xFF, sg = (secondary >> 8) & 0xFF, sb = secondary & 0xFF;
        int tr = (tertiary >> 16) & 0xFF,  tg = (tertiary >> 8) & 0xFF,  tb = tertiary & 0xFF;

        renderBannerPattern(vc, mat, x0, y0, x1, y1, x0, y0, x1 - x0, y1 - y0,
                team.getBannerShape(), team.getTeamMosaicPattern(),
                pr, pg, pb, sr, sg, sb, tr, tg, tb, team.isUseTertiary(),
                team.getPaintPixels(), packedLight & 0xFFFF, (packedLight >> 16) & 0xFFFF,
                highlights, mergePaint);
    }

    /**
     * Renders all pattern fills for a banner rectangle using the OW_TEAMS_GUI flag texture.
     * bx0/by0/bw/bh describe the full banner bounds (used for UV sub-region mapping).
     * The quad to fill is [wx0..wx1] × [wy0..wy1].
     */
    private static void renderBannerPattern(VertexConsumer vc, Matrix4f mat,
            float bx0, float by0, float bx1, float by1,
            float bbx0, float bby0, float bbw, float bbh,
            OWTeamBannerShape shape, OWTeamMosaicPattern pattern,
            int pr, int pg, int pb, int sr, int sg, int sb,
            int tr, int tg, int tb, boolean useTertiary,
            byte[] paintPixels, int lightU, int lightV, boolean highlights, boolean mergePaint) {

        if (shape == null) shape = OWTeamBannerShape.CLASSIC;
        // La silhouette de la forme (dans ow_teams_banners_styles.png) découpe le motif : flagRect
        // & co. lisent bnUBase/bnVBase pour échantillonner la bonne région, sa transparence masque.
        bnUBase = shape.getBaseU();
        bnVBase = shape.getBaseV();

        final float xM = (bx0 + bx1) / 2f, yM = (by0 + by1) / 2f;
        final float BW = bx1 - bx0, BH = by1 - by0;
        // 3ᵉ bande = tertiaire si activée, sinon primaire.
        final int qr = useTertiary ? tr : pr, qg = useTertiary ? tg : pg, qb = useTertiary ? tb : pb;

        if (pattern == OWTeamMosaicPattern.CUSTOM_PAINT) {
            if (paintPixels != null && paintPixels.length >= 55 * 93) {
                int[] rects = mergePaint ? paintRectangles(paintPixels) : null;

                if (rects != null) {
                    for (int i = 0; i < rects.length; i += 5) {
                        int v = rects[i + 4];
                        int cr = v == 2 ? tr : (v == 1 ? sr : pr);
                        int cg = v == 2 ? tg : (v == 1 ? sg : pg);
                        int cb = v == 2 ? tb : (v == 1 ? sb : pb);
                        float wx0 = bx0 + (float) rects[i] / 55 * BW;
                        float wx1 = bx0 + (float) rects[i + 2] / 55 * BW;
                        float wy1 = by1 - (float) rects[i + 1] / 93 * BH;
                        float wy0 = by1 - (float) rects[i + 3] / 93 * BH;
                        flagRect(vc, mat, wx0, wy0, wx1, wy1, bbx0, bby0, bbw, bbh, lightU, lightV, cr, cg, cb);
                    }
                } else {
                    for (int row = 0; row < 93; row++) {
                        for (int col = 0; col < 55; col++) {
                            int v = paintPixels[row * 55 + col] & 3;
                            int cr = v == 2 ? tr : (v == 1 ? sr : pr);
                            int cg = v == 2 ? tg : (v == 1 ? sg : pg);
                            int cb = v == 2 ? tb : (v == 1 ? sb : pb);
                            float wx0 = bx0 + (float) col / 55 * BW;
                            float wx1 = bx0 + (float)(col + 1) / 55 * BW;
                            float wy1 = by1 - (float) row / 93 * BH;
                            float wy0 = by1 - (float)(row + 1) / 93 * BH;
                            flagRect(vc, mat, wx0, wy0, wx1, wy1, bbx0, bby0, bbw, bbh, lightU, lightV, cr, cg, cb);
                        }
                    }
                }
            } else {
                flagRect(vc, mat, bx0, by0, bx1, by1, bbx0, bby0, bbw, bbh, lightU, lightV, pr, pg, pb);
            }
            if (highlights) renderBannerHighlights(vc, mat, bx0, by0, bx1, by1, shape, lightU, lightV);
            return;
        }

        final int STRIPS = 32;
        switch (pattern) {
            case GRADIENT_DOWN -> {
                if (useTertiary) {
                    flagRectGradV(vc, mat, bx0, yM, bx1, by1, bbx0, bby0, bbw, bbh, lightU, lightV, sr, sg, sb, pr, pg, pb);
                    flagRectGradV(vc, mat, bx0, by0, bx1, yM, bbx0, bby0, bbw, bbh, lightU, lightV, tr, tg, tb, sr, sg, sb);
                } else {
                    flagRectGradV(vc, mat, bx0, by0, bx1, by1, bbx0, bby0, bbw, bbh, lightU, lightV, sr, sg, sb, pr, pg, pb);
                }
            }
            case GRADIENT_RIGHT -> {
                if (useTertiary) {
                    flagRectGradH(vc, mat, bx0, by0, xM, by1, bbx0, bby0, bbw, bbh, lightU, lightV, pr, pg, pb, sr, sg, sb);
                    flagRectGradH(vc, mat, xM, by0, bx1, by1, bbx0, bby0, bbw, bbh, lightU, lightV, sr, sg, sb, tr, tg, tb);
                } else {
                    flagRectGradH(vc, mat, bx0, by0, bx1, by1, bbx0, bby0, bbw, bbh, lightU, lightV, pr, pg, pb, sr, sg, sb);
                }
            }
            case SPLIT_H -> {
                flagRect(vc, mat, bx0, yM, bx1, by1, bbx0, bby0, bbw, bbh, lightU, lightV, pr, pg, pb);
                flagRect(vc, mat, bx0, by0, bx1, yM,  bbx0, bby0, bbw, bbh, lightU, lightV, sr, sg, sb);
            }
            case SPLIT_V -> {
                flagRect(vc, mat, bx0, by0, xM, by1, bbx0, bby0, bbw, bbh, lightU, lightV, pr, pg, pb);
                flagRect(vc, mat, xM, by0, bx1, by1, bbx0, bby0, bbw, bbh, lightU, lightV, sr, sg, sb);
            }
            case DIAGONAL_TL_BR -> {
                for (int j = 0; j < STRIPS; j++) {
                    float wyTop = by1 - (float) j / STRIPS * BH;
                    float wyBot = by1 - (float)(j + 1) / STRIPS * BH;
                    if (useTertiary) {
                        float t = (j + 0.5f) / STRIPS;
                        float sx1 = bx0 + Math.max(0f, Math.min(1f, 2f / 3f - t)) * BW;
                        float sx2 = bx0 + Math.max(0f, Math.min(1f, 4f / 3f - t)) * BW;
                        if (sx1 > bx0) flagRect(vc, mat, bx0, wyBot, sx1, wyTop, bbx0, bby0, bbw, bbh, lightU, lightV, pr, pg, pb);
                        if (sx2 > sx1) flagRect(vc, mat, sx1, wyBot, sx2, wyTop, bbx0, bby0, bbw, bbh, lightU, lightV, sr, sg, sb);
                        if (sx2 < bx1) flagRect(vc, mat, sx2, wyBot, bx1, wyTop, bbx0, bby0, bbw, bbh, lightU, lightV, tr, tg, tb);
                    } else {
                        float splitX = bx1 - (float) j / STRIPS * BW;
                        if (splitX > bx0) flagRect(vc, mat, bx0, wyBot, splitX, wyTop, bbx0, bby0, bbw, bbh, lightU, lightV, pr, pg, pb);
                        if (splitX < bx1) flagRect(vc, mat, splitX, wyBot, bx1, wyTop, bbx0, bby0, bbw, bbh, lightU, lightV, sr, sg, sb);
                    }
                }
            }
            case THIRDS_H -> {
                float y2 = by0 + BH / 3f, y3 = by0 + 2f * BH / 3f;
                flagRect(vc, mat, bx0, y3,  bx1, by1, bbx0, bby0, bbw, bbh, lightU, lightV, pr, pg, pb);
                flagRect(vc, mat, bx0, y2,  bx1, y3,  bbx0, bby0, bbw, bbh, lightU, lightV, sr, sg, sb);
                flagRect(vc, mat, bx0, by0, bx1, y2,  bbx0, bby0, bbw, bbh, lightU, lightV, qr, qg, qb);
            }
            case THIRDS_V -> {
                float x2 = bx0 + BW / 3f, x3 = bx0 + 2f * BW / 3f;
                flagRect(vc, mat, bx0, by0, x2,  by1, bbx0, bby0, bbw, bbh, lightU, lightV, pr, pg, pb);
                flagRect(vc, mat, x2,  by0, x3,  by1, bbx0, bby0, bbw, bbh, lightU, lightV, sr, sg, sb);
                flagRect(vc, mat, x3,  by0, bx1, by1, bbx0, bby0, bbw, bbh, lightU, lightV, qr, qg, qb);
            }
            case CHECKER -> {
                flagRect(vc, mat, bx0, yM,  xM,  by1, bbx0, bby0, bbw, bbh, lightU, lightV, pr, pg, pb);
                flagRect(vc, mat, xM,  yM,  bx1, by1, bbx0, bby0, bbw, bbh, lightU, lightV, sr, sg, sb);
                flagRect(vc, mat, bx0, by0, xM,  yM,  bbx0, bby0, bbw, bbh, lightU, lightV, sr, sg, sb);
                flagRect(vc, mat, xM,  by0, bx1, yM,  bbx0, bby0, bbw, bbh, lightU, lightV, pr, pg, pb);
            }
            case STRIPES -> {
                for (int j = 0; j < STRIPS; j++) {
                    float wyTop = by1 - (float) j / STRIPS * BH;
                    float wyBot = by1 - (float)(j + 1) / STRIPS * BH;
                    int guiRow = j * 93 / STRIPS;
                    int total = 56 + 93;
                    int s1 = Math.max(0, Math.min(total / 3 - guiRow, 56));
                    int s2 = Math.max(0, Math.min(total * 2 / 3 - guiRow, 56));
                    float ws1 = bx0 + (float) s1 / 56 * BW;
                    float ws2 = bx0 + (float) s2 / 56 * BW;
                    if (s1 > 0)  flagRect(vc, mat, bx0, wyBot, ws1, wyTop, bbx0, bby0, bbw, bbh, lightU, lightV, pr, pg, pb);
                    if (s2 > s1) flagRect(vc, mat, ws1, wyBot, ws2, wyTop, bbx0, bby0, bbw, bbh, lightU, lightV, sr, sg, sb);
                    if (s2 < 56) flagRect(vc, mat, ws2, wyBot, bx1, wyTop, bbx0, bby0, bbw, bbh, lightU, lightV, qr, qg, qb);
                }
            }
            case DIAMOND -> {
                float dScale = 0.78f;
                float halfH = (BH / 2f) * dScale;
                for (int j = 0; j < STRIPS; j++) {
                    float wyTop = by1 - (float) j / STRIPS * BH;
                    float wyBot = by1 - (float)(j + 1) / STRIPS * BH;
                    float wyMid = (wyTop + wyBot) / 2f;
                    float distY = Math.abs(wyMid - yM);
                    if (distY >= halfH) {
                        flagRect(vc, mat, bx0, wyBot, bx1, wyTop, bbx0, bby0, bbw, bbh, lightU, lightV, pr, pg, pb);
                    } else {
                        float hw = (1f - distY / halfH) * (BW / 2f) * dScale;
                        float dx1 = Math.max(bx0, xM - hw), dx2 = Math.min(bx1, xM + hw);
                        if (dx1 > bx0) flagRect(vc, mat, bx0, wyBot, dx1, wyTop, bbx0, bby0, bbw, bbh, lightU, lightV, pr, pg, pb);
                        flagRect(vc, mat, dx1, wyBot, dx2, wyTop, bbx0, bby0, bbw, bbh, lightU, lightV, sr, sg, sb);
                        if (dx2 < bx1) flagRect(vc, mat, dx2, wyBot, bx1, wyTop, bbx0, bby0, bbw, bbh, lightU, lightV, pr, pg, pb);
                    }
                }
            }
            case CIRCLE_PRI -> {
                float rx = (Math.min(BW, BH) / 3f) + BW / 56f;
                float ry = Math.min(BW, BH) / 3f;
                for (int j = 0; j < STRIPS; j++) {
                    float wyTop = by1 - (float) j / STRIPS * BH;
                    float wyBot = by1 - (float)(j + 1) / STRIPS * BH;
                    float dy = (wyTop + wyBot) / 2f - yM;
                    if (Math.abs(dy) > ry) {
                        flagRect(vc, mat, bx0, wyBot, bx1, wyTop, bbx0, bby0, bbw, bbh, lightU, lightV, pr, pg, pb);
                    } else {
                        float dx = rx * (float) Math.sqrt(1f - (dy * dy) / (ry * ry));
                        float wx0c = Math.max(bx0, xM - dx), wx1c = Math.min(bx1, xM + dx);
                        if (wx0c > bx0) flagRect(vc, mat, bx0,  wyBot, wx0c, wyTop, bbx0, bby0, bbw, bbh, lightU, lightV, pr, pg, pb);
                        flagRect(vc, mat, wx0c, wyBot, wx1c, wyTop, bbx0, bby0, bbw, bbh, lightU, lightV, sr, sg, sb);
                        if (wx1c < bx1) flagRect(vc, mat, wx1c, wyBot, bx1,  wyTop, bbx0, bby0, bbw, bbh, lightU, lightV, pr, pg, pb);
                    }
                }
            }
            default -> flagRect(vc, mat, bx0, by0, bx1, by1, bbx0, bby0, bbw, bbh, lightU, lightV, pr, pg, pb);
        }

        if (highlights) renderBannerHighlights(vc, mat, bx0, by0, bx1, by1, shape, lightU, lightV);
    }

    /**
     * Superpose le calque de highlights (ombrage/brillance) de la forme, aligné en bas de la bannière,
     * teinté en blanc (couleurs d'origine de la texture). Reproduit {@code OWBannerRenderer.renderHighlights}.
     */
    private static void renderBannerHighlights(VertexConsumer vc, Matrix4f mat,
            float bx0, float by0, float bx1, float by1,
            OWTeamBannerShape shape, int lightU, int lightV) {
        int hlH = shape.getHighlightH();
        if (hlH <= 0) return;
        final float BH = by1 - by0;
        // Le highlight occupe la bande basse de la bannière (hlH px sur 93).
        float hy0 = by0;                                                    // bas
        float hy1 = by0 + BH * ((float) hlH / OWTeamBannerShape.BASE_H);    // haut du calque
        float u0 = shape.getHighlightU() / (float) OWTeamBannerShape.TEXTURE_SIZE;
        float u1 = (shape.getHighlightU() + OWTeamBannerShape.BASE_W) / (float) OWTeamBannerShape.TEXTURE_SIZE;
        float v0 = shape.getHighlightV() / (float) OWTeamBannerShape.TEXTURE_SIZE;         // haut
        float v1 = (shape.getHighlightV() + hlH) / (float) OWTeamBannerShape.TEXTURE_SIZE; // bas
        vc.addVertex(mat, bx0, hy0, 0).setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0, 1, 0);
        vc.addVertex(mat, bx1, hy0, 0).setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0, 1, 0);
        vc.addVertex(mat, bx1, hy1, 0).setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0, 1, 0);
        vc.addVertex(mat, bx0, hy1, 0).setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0, 1, 0);
    }

    // Renders a sub-rectangle of the banner silhouette texture (region set via bnUBase/bnVBase) with vertex color tinting.
    private static void flagRect(VertexConsumer vc, Matrix4f mat,
            float wx0, float wy0, float wx1, float wy1,
            float bx0, float by0, float bw, float bh,
            int lightU, int lightV, int r, int g, int b) {
        float fu0 = (bnUBase + (wx0 - bx0) / bw * OWTeamBannerShape.BASE_W) / OWTeamBannerShape.TEXTURE_SIZE;
        float fu1 = (bnUBase + (wx1 - bx0) / bw * OWTeamBannerShape.BASE_W) / OWTeamBannerShape.TEXTURE_SIZE;
        float fv0 = (bnVBase + (1f - (wy1 - by0) / bh) * OWTeamBannerShape.BASE_H) / OWTeamBannerShape.TEXTURE_SIZE;
        float fv1 = (bnVBase + (1f - (wy0 - by0) / bh) * OWTeamBannerShape.BASE_H) / OWTeamBannerShape.TEXTURE_SIZE;
        vc.addVertex(mat, wx0, wy0, 0).setColor(r, g, b, 255).setUv(fu0, fv1).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0, 1, 0);
        vc.addVertex(mat, wx1, wy0, 0).setColor(r, g, b, 255).setUv(fu1, fv1).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0, 1, 0);
        vc.addVertex(mat, wx1, wy1, 0).setColor(r, g, b, 255).setUv(fu1, fv0).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0, 1, 0);
        vc.addVertex(mat, wx0, wy1, 0).setColor(r, g, b, 255).setUv(fu0, fv0).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0, 1, 0);
    }

    // Vertical gradient: bottom = rBot/gBot/bBot, top = rTop/gTop/bTop.
    private static void flagRectGradV(VertexConsumer vc, Matrix4f mat,
            float wx0, float wy0, float wx1, float wy1,
            float bx0, float by0, float bw, float bh,
            int lightU, int lightV,
            int rBot, int gBot, int bBot, int rTop, int gTop, int bTop) {
        float fu0 = (bnUBase + (wx0 - bx0) / bw * OWTeamBannerShape.BASE_W) / OWTeamBannerShape.TEXTURE_SIZE;
        float fu1 = (bnUBase + (wx1 - bx0) / bw * OWTeamBannerShape.BASE_W) / OWTeamBannerShape.TEXTURE_SIZE;
        float fv0 = (bnVBase + (1f - (wy1 - by0) / bh) * OWTeamBannerShape.BASE_H) / OWTeamBannerShape.TEXTURE_SIZE;
        float fv1 = (bnVBase + (1f - (wy0 - by0) / bh) * OWTeamBannerShape.BASE_H) / OWTeamBannerShape.TEXTURE_SIZE;
        vc.addVertex(mat, wx0, wy0, 0).setColor(rBot, gBot, bBot, 255).setUv(fu0, fv1).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0, 1, 0);
        vc.addVertex(mat, wx1, wy0, 0).setColor(rBot, gBot, bBot, 255).setUv(fu1, fv1).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0, 1, 0);
        vc.addVertex(mat, wx1, wy1, 0).setColor(rTop, gTop, bTop, 255).setUv(fu1, fv0).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0, 1, 0);
        vc.addVertex(mat, wx0, wy1, 0).setColor(rTop, gTop, bTop, 255).setUv(fu0, fv0).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0, 1, 0);
    }

    // Horizontal gradient: left = rLeft/gLeft/bLeft, right = rRight/gRight/bRight.
    private static void flagRectGradH(VertexConsumer vc, Matrix4f mat,
            float wx0, float wy0, float wx1, float wy1,
            float bx0, float by0, float bw, float bh,
            int lightU, int lightV,
            int rLeft, int gLeft, int bLeft, int rRight, int gRight, int bRight) {
        float fu0 = (bnUBase + (wx0 - bx0) / bw * OWTeamBannerShape.BASE_W) / OWTeamBannerShape.TEXTURE_SIZE;
        float fu1 = (bnUBase + (wx1 - bx0) / bw * OWTeamBannerShape.BASE_W) / OWTeamBannerShape.TEXTURE_SIZE;
        float fv0 = (bnVBase + (1f - (wy1 - by0) / bh) * OWTeamBannerShape.BASE_H) / OWTeamBannerShape.TEXTURE_SIZE;
        float fv1 = (bnVBase + (1f - (wy0 - by0) / bh) * OWTeamBannerShape.BASE_H) / OWTeamBannerShape.TEXTURE_SIZE;
        vc.addVertex(mat, wx0, wy0, 0).setColor(rLeft,  gLeft,  bLeft,  255).setUv(fu0, fv1).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0, 1, 0);
        vc.addVertex(mat, wx1, wy0, 0).setColor(rRight, gRight, bRight, 255).setUv(fu1, fv1).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0, 1, 0);
        vc.addVertex(mat, wx1, wy1, 0).setColor(rRight, gRight, bRight, 255).setUv(fu1, fv0).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0, 1, 0);
        vc.addVertex(mat, wx0, wy1, 0).setColor(rLeft,  gLeft,  bLeft,  255).setUv(fu0, fv0).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0, 1, 0);
    }

    // ── Team banner end ──────────────────────────────────────────────────────────

    private static final Map<UUID, EntityInfoState> entityInfoStates = new HashMap<>();

    private static class EntityInfoState {
        public int opacity = 0;
        public boolean showInfo = false;
    }

    public static void createInformationImage(OWEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, double offsetX, double offsetY, double offsetZ, float offsetYMultiplier, int distanceToShow) {
        Player player = Minecraft.getInstance().player;
        UUID boaId = entity.getUUID();
        EntityInfoState infoState = entityInfoStates.computeIfAbsent(boaId, id -> new EntityInfoState());

        if (player != null) {
            int distance = (int) entity.distanceTo(player);
            infoState.showInfo = distance <= distanceToShow;
        }

        int smoothAnimationTimeInSeconds = 1;

        if (infoState.showInfo) {
            if (infoState.opacity < (255 - (int) (smoothAnimationTimeInSeconds * 6.375)))
                infoState.opacity += (int) (smoothAnimationTimeInSeconds * 6.375);
        } else {
            if (infoState.opacity > (int) (smoothAnimationTimeInSeconds * 6.375))
                infoState.opacity -= (int) (smoothAnimationTimeInSeconds * 6.375);
        }
        if (player != null && infoState.opacity > (int) (smoothAnimationTimeInSeconds * 6.375) && !entity.isVehicle()) {
            if (entity.isInResurrection()) OWRendererUtils.displayResurrectionTimeEntity(entity, poseStack, bufferSource, packedLight, Minecraft.getInstance().getEntityRenderDispatcher(), infoState.opacity, offsetYMultiplier);
            else OWRendererUtils.displayOverlayOnEntity(entity, poseStack, bufferSource, packedLight, infoState.opacity, offsetX, offsetY, offsetZ);
        }
    }
}
