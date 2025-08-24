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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.tiew.operationWild.entity.custom.living.ElephantEntity;
import net.tiew.operationWild.item.custom.ElephantSaddle;
import net.tiew.operationWild.utils.OWUtils;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;

import java.util.*;

public class OWRendererUtils {

    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");

    public static void displayOverlayOnEntity(OWEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int opacity, double offsetX, double offsetY, double offsetZ) {
        ResourceLocation overlayTexture = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/overlay/image_information.png");
        ResourceLocation barTexture = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/overlay/image_information_bars.png");
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

        RenderType renderType = RenderType.entityTranslucent(overlayTexture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        Matrix4f matrix = poseStack.last().pose();

        float width = 1.0F;
        float height = 1.0F;
        float u0 = 0.0F;
        float u1 = 1.0F;
        float v0 = 0.0F;
        float v1 = 1.0F;

        int lightU = packedLight & 0xFFFF;
        int lightV = (packedLight >> 16) & 0xFFFF;

        vertexConsumer.addVertex(matrix, -width / 2, -height / 2, 0.0F).setColor(255, 255, 255, opacity).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        vertexConsumer.addVertex(matrix, width / 2, -height / 2, 0.0F).setColor(255, 255, 255, opacity).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        vertexConsumer.addVertex(matrix, width / 2, height / 2, 0.0F).setColor(255, 255, 255, opacity).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);
        vertexConsumer.addVertex(matrix, -width / 2, height / 2, 0.0F).setColor(255, 255, 255, opacity).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightU, lightV).setNormal(0.0F, 1.0F, 0.0F);




        int fullBrightLight = 0xF000F0;


        float tamingBarProgress = entity.getTamingPercentage() / 100.0F;

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 0.002D);

        VertexConsumer tamingBarVertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(barTexture));
        Matrix4f tamingBarMatrix = poseStack.last().pose();

        float tamingBarScale = 0.004F;
        float tamingBarWidth = 166.0F * tamingBarScale;
        float tamingBarHeight = 10.0F * tamingBarScale;
        float tamingRenderedBarWidth = tamingBarWidth * tamingBarProgress;

        int fullBrightU = fullBrightLight & 0xFFFF;
        int fullBrightV = (fullBrightLight >> 16) & 0xFFFF;

        tamingBarVertexConsumer.addVertex(tamingBarMatrix, -0.43F, 0.2F, 0.0F).setColor(255, 255, 255, opacity).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(fullBrightU, fullBrightV).setNormal(0.0F, 1.0F, 0.0F);
        tamingBarVertexConsumer.addVertex(tamingBarMatrix, -0.43F + tamingRenderedBarWidth, 0.2F, 0.0F).setColor(255, 255, 255, opacity).setUv((166.0F * tamingBarProgress) / 256.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(fullBrightU, fullBrightV).setNormal(0.0F, 1.0F, 0.0F);
        tamingBarVertexConsumer.addVertex(tamingBarMatrix, -0.43F + tamingRenderedBarWidth, 0.2F - tamingBarHeight, 0.0F).setColor(255, 255, 255, opacity).setUv((166.0F * tamingBarProgress) / 256.0F, 10.0F / 256.0F).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(fullBrightU, fullBrightV).setNormal(0.0F, 1.0F, 0.0F);
        tamingBarVertexConsumer.addVertex(tamingBarMatrix, -0.43F, 0.2F - tamingBarHeight, 0.0F).setColor(255, 255, 255, opacity).setUv(0.0F, 10.0F / 256.0F).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(fullBrightU, fullBrightV).setNormal(0.0F, 1.0F, 0.0F);

        poseStack.popPose();

        float secondBarProgress = entity.isBaby() ? entity.getMaturationPercentage() / 100.0F : entity.getSleepBarPercent() / 100.0F;

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 0.002D);

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





        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 0.002D);

        int iconU = 0;
        int iconV = entity.isMale() ? 48 : 36;
        int iconWidth = 12;
        int iconHeight = 12;

        VertexConsumer iconVertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(ICONS));
        Matrix4f iconMatrix = poseStack.last().pose();

        float iconScale = 0.004F;
        float renderedIconWidth = iconWidth * iconScale;
        float renderedIconHeight = iconHeight * iconScale;
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


        poseStack.scale(0.0035F, -0.0035F, 0.0035F);
        poseStack.translate(0.0D, 0.0D, 0.001D);

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

        Component sleepingComponent = Component.empty()
                .append(Component.translatable(entity.isBaby() ? "imageMaturation" : "sleepingTaming").withStyle(style -> style.withColor(0x8e9eb9).withBold(true)))
                .append(" " + Math.round(percentage * 10.0f) / 10.0f + "%")
                .append(entity.isBaby() ? Component.literal(" | ").withStyle(Style.EMPTY.withItalic(false)) : Component.empty())
                .append(entity.isBaby() ? Component.literal("(" + timeDisplay + ")").withStyle(Style.EMPTY.withItalic(true)) : Component.empty());

        Component animalTypeComponent = Component.empty()
                .append(Component.translatable(chooseAnimalMaturation(entity)).withStyle(style -> style.withColor(entity.getEntityColor()).withBold(true)))
                .append(entity.isBaby() ? " " : "")
                .append(Component.translatable(chooseAnimalType(entity)).withStyle(style -> style.withColor(entity.getEntityColor()).withBold(true)));

        Component animalLevelComponent = Component.empty()
                .append(Component.translatable("tooltip.lvlImage").withStyle(style -> style.withColor(0x8e9eb9).withBold(true)))
                .append(Component.literal(String.valueOf(entity.getLevel())).withStyle(style -> style.withColor(entity.getLevel() >= 50 ? 0xdd9847 : entity.getLevelPoints() > 0 ? 0xb8e45a : 0x8e9eb9).withBold(false)));

        String ownerName = Minecraft.getInstance().level != null && entity.getOwnerUUID() != null ?
                Optional.ofNullable(Minecraft.getInstance().level.getPlayerByUUID(entity.getOwnerUUID()))
                        .map(player -> player.getName().getString())
                        .orElse("") : "";

        Component animalOwnerComponent = Component.empty()
                .append(Component.translatable("tooltip.ownerImage").withStyle(style -> style.withColor(0x8e9eb9).withBold(true)))
                .append(Component.literal(ownerName).withStyle(style -> style.withColor(0x8e9eb9).withBold(false)));

        float lefttextX = -132;
        float righttextX = 60;
        float textY = -132;
        int leftPadding = 12;
        int rightPadding = 20;

        float tamingTextWidth = font.width(tamingComponent);
        float sleepingTextWidth = font.width(sleepingComponent);

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
        if (entity.isTame()) font.drawInBatch(stateComponent, lefttextX, textY + (leftPadding * 3), entity.isPassive() ? 0x55FF55 : 0xFF5555, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
        font.drawInBatch(tamingComponent, tamingTextX - 25, textY + (20 * 3), 0x8e9eb9, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
        font.drawInBatch(sleepingComponent, sleepingTextX - 25, textY + (20 * 4.4f), 0x8e9eb9, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);

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
            default -> {
                return "entity.ow.tiger";
            }
        }
    }

    public static void displayPrestigeLevelAboveEntity(OWEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, EntityRenderDispatcher entityRenderDispatcher) {
        int levelColor = 0xc8f6ff;
        Component level = Component.literal(String.valueOf(entity.getPrestigeLevel())).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(levelColor).getValue()).withBold(true));
        poseStack.pushPose();
        poseStack.translate(0,  entity.getBbHeight() + (entity instanceof ElephantEntity ? 1.8F : 0.8F), 0);
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.scale(0.015F, -0.015F, 0.015F);
        Matrix4f matrix4f = poseStack.last().pose();
        Font font = Minecraft.getInstance().font;
        float textWidth = (float)(-font.width(level) / 2);
        font.drawInBatch(level, textWidth, 0, -1, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
        poseStack.popPose();
    }

    public static void displayLevelAboveEntity(OWEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, EntityRenderDispatcher entityRenderDispatcher) {
        int textColor = 0xdfdfdf;
        int levelColor = entity.getLevel() >= 50 ? 0xdd9847 : entity.getLevelPoints() > 0 ? 0xb8e45a : 0xFFFFFF;
        Component level = Component.literal(String.valueOf(entity.getLevel())).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(levelColor).getValue()).withBold(true));
        Component text = Component.translatable("tooltip.lvl", level).withStyle(Style.EMPTY).withColor(TextColor.fromRgb(textColor).getValue());
        poseStack.pushPose();
        poseStack.translate(0, entity.getBbHeight() + (entity instanceof ElephantEntity ? 1.6F : 0.6F), 0);
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.scale(0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = poseStack.last().pose();
        Font font = Minecraft.getInstance().font;
        float textWidth = (float)(-font.width(text) / 2);
        font.drawInBatch(text, textWidth, 0, -1, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
        poseStack.popPose();
    }

    public static void displayResurrectionTimeEntity(OWEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, EntityRenderDispatcher entityRenderDispatcher, int opacity, float yOffsetMultiplier) {
        ResourceLocation barTexture = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/overlay/image_information_bars.png");
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
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, image);
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


    public static void displayOwnerAboveEntity(OWEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, EntityRenderDispatcher entityRenderDispatcher) {
        int textColor = 0xdfdfdf;
        int ownerColor = 0xFFFFFF;

        Component owner = Component.literal(String.valueOf(Objects.requireNonNull(Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getPlayerByUUID(Objects.requireNonNull(entity.getOwnerUUID())) : null).getName().getString())).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(ownerColor).getValue()).withBold(true));
        Component text = Component.translatable("tooltip.owner", owner).withStyle(Style.EMPTY).withColor(TextColor.fromRgb(textColor).getValue());
        poseStack.pushPose();
        poseStack.translate(0,  entity.getBbHeight() + (entity instanceof ElephantEntity ? 1.35F : 0.35F), 0);
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.scale(0.0175F, -0.0175F, 0.0175F);
        Matrix4f matrix4f = poseStack.last().pose();
        Font font = Minecraft.getInstance().font;
        float textWidth = (float)(-font.width(text) / 2);
        font.drawInBatch(text, textWidth, 0, -1, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
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
