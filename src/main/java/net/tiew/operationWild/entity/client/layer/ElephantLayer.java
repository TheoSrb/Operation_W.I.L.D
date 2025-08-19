package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.model.ElephantModel;
import net.tiew.operationWild.entity.client.render.ElephantRenderer;
import net.tiew.operationWild.entity.custom.living.ElephantEntity;
import net.tiew.operationWild.item.custom.ElephantSaddle;

import java.util.*;

public class ElephantLayer extends RenderLayer<ElephantEntity, ElephantModel<ElephantEntity>> {
    private static final ResourceLocation RESURRECTION_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/in_resurrection.png");
    private static final ResourceLocation RESURRECTION_GLOWING_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/skins/elephant_skin_gold_glowing.png");
    private static final ResourceLocation BLOODY_STAGE_0_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/elephant_bloody_stage_0.png");
    private static final ResourceLocation BLOODY_STAGE_1_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/elephant_bloody_stage_1.png");
    private static final ResourceLocation BLOODY_STAGE_2_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/elephant_bloody_stage_2.png");

    private static final ResourceLocation SADDLE_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/elephant_saddle.png");
    private static final ResourceLocation SADDLE_WOOL_LAYER_0_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/elephant_saddle_wool_layer_0.png");
    private static final ResourceLocation SADDLE_WOOL_LAYER_1_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/elephant_saddle_wool_layer_1.png");

    private static final ResourceLocation NECKLACE_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/elephant_necklace.png");
    private static final ResourceLocation NECKLACE_SPIKES_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/elephant_necklace_spikes.png");

    public ElephantLayer(ElephantRenderer elephantRenderer) {
        super(elephantRenderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, ElephantEntity elephant, float v, float v1, float v2, float v3, float v4, float v5) {
        double elephantHealthTier = elephant.getMaxHealth() / 4;

        if (elephant.isInResurrection()) {
            float opacity = (float) (0.75 * (1 - elephant.getResurrectionPercentage() / 100.0f));
            renderOverlayWithOpacity(poseStack, multiBufferSource, RESURRECTION_TEXTURE, false, packedLight, opacity);
            renderOverlay(poseStack, multiBufferSource, RESURRECTION_GLOWING_TEXTURE, false, packedLight);
        }

        if (elephant.isTame() && !elephant.isInResurrection()) {
            renderOverlayWithColor(poseStack, multiBufferSource, NECKLACE_TEXTURE, false, packedLight, elephant.getNecklaceColor());
            renderOverlay(poseStack, multiBufferSource, NECKLACE_SPIKES_TEXTURE, false, packedLight);
        }

        if (elephant.isSaddled()) {
            renderOverlay(poseStack, multiBufferSource, SADDLE_TEXTURE, false, packedLight);

            List<Item> wools = elephant.getSaddleWools();
            Item firstWool = wools.size() > 0 ? wools.get(0) : Items.WHITE_WOOL;
            Item secondWool = wools.size() > 1 ? wools.get(1) : Items.WHITE_WOOL;

            boolean canBeDarkerFirstWool = getDyeColorFromWool(firstWool) == DyeColor.GREEN || getDyeColorFromWool(firstWool) == DyeColor.GRAY || getDyeColorFromWool(firstWool) == DyeColor.CYAN;
            boolean canBeDarkerSecondWool = getDyeColorFromWool(secondWool) == DyeColor.GREEN || getDyeColorFromWool(secondWool) == DyeColor.GRAY || getDyeColorFromWool(secondWool) == DyeColor.CYAN;

            renderOverlayWithColor(poseStack, multiBufferSource, SADDLE_WOOL_LAYER_0_TEXTURE, true, packedLight,
                    darkenColor(getDyeColorFromWool(firstWool).getTextColor(), canBeDarkerFirstWool ? 0.5f : 0.9f));
            renderOverlayWithColor(poseStack, multiBufferSource, SADDLE_WOOL_LAYER_1_TEXTURE, true, packedLight,
                    darkenColor(getDyeColorFromWool(secondWool).getTextColor(), canBeDarkerSecondWool ? 0.5f : 0.9f));
        }

        if (elephant.getHealth() < elephantHealthTier) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_2_TEXTURE, false, packedLight);
        else if (elephant.getHealth() < (elephantHealthTier * 2)) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_1_TEXTURE, false, packedLight);
        else if (elephant.getHealth() < (elephantHealthTier * 3)) renderOverlay(poseStack, multiBufferSource, BLOODY_STAGE_0_TEXTURE, false, packedLight);
    }

    private int darkenColor(int color, float factor) {
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return (r << 16) | (g << 8) | b | (color & 0xFF000000);
    }

    private DyeColor getDyeColorFromWool(Item woolItem) {
        if (woolItem == Items.WHITE_WOOL) return DyeColor.WHITE;
        if (woolItem == Items.ORANGE_WOOL) return DyeColor.ORANGE;
        if (woolItem == Items.MAGENTA_WOOL) return DyeColor.MAGENTA;
        if (woolItem == Items.LIGHT_BLUE_WOOL) return DyeColor.LIGHT_BLUE;
        if (woolItem == Items.YELLOW_WOOL) return DyeColor.YELLOW;
        if (woolItem == Items.LIME_WOOL) return DyeColor.LIME;
        if (woolItem == Items.PINK_WOOL) return DyeColor.PINK;
        if (woolItem == Items.GRAY_WOOL) return DyeColor.GRAY;
        if (woolItem == Items.LIGHT_GRAY_WOOL) return DyeColor.LIGHT_GRAY;
        if (woolItem == Items.CYAN_WOOL) return DyeColor.CYAN;
        if (woolItem == Items.PURPLE_WOOL) return DyeColor.PURPLE;
        if (woolItem == Items.BLUE_WOOL) return DyeColor.BLUE;
        if (woolItem == Items.BROWN_WOOL) return DyeColor.BROWN;
        if (woolItem == Items.GREEN_WOOL) return DyeColor.GREEN;
        if (woolItem == Items.RED_WOOL) return DyeColor.RED;
        if (woolItem == Items.BLACK_WOOL) return DyeColor.BLACK;

        return DyeColor.WHITE;
    }

    private void renderOverlay(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, boolean glowLayer, int packedLight) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(glowLayer ? RenderType.eyes(texture) : RenderType.entityCutout(texture));
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, glowLayer ? 15728640 : packedLight, OverlayTexture.NO_OVERLAY);
    }

    private void renderOverlayWithColor(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, boolean glowLayer, int packedLight, int color) {
        poseStack.pushPose();

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        RenderType renderType = RenderType.entityCutoutNoCull(texture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        int opaqueColor = (color & 0x00FFFFFF) | 0xFF000000;

        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, opaqueColor);

        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    private void renderOverlayWithOpacity(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, boolean glowLayer, int packedLight, float opacity) {
        opacity = Math.max(0.0f, Math.min(1.0f, opacity));
        int alpha = (int)(opacity * 255.0f);
        int color = 0xFFFFFF | (alpha << 24);
        RenderType renderType = glowLayer ? RenderType.eyes(texture) : RenderType.entityTranslucent(texture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, glowLayer ? 15728640 : packedLight, OverlayTexture.NO_OVERLAY, color);
    }
}
