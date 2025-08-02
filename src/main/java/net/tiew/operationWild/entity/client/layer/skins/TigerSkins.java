package net.tiew.operationWild.entity.client.layer.skins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.model.TigerModel;
import net.tiew.operationWild.entity.client.render.TigerRenderer;
import net.tiew.operationWild.entity.custom.living.TigerEntity;
import net.tiew.operationWild.entity.variants.TigerVariant;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TigerSkins extends RenderLayer<TigerEntity, TigerModel<TigerEntity>> {
    private static final ResourceLocation SKIN_GOLD_GLOWING_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_gold_glowing.png");
    private static final ResourceLocation SKIN_BOSS_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_boss.png");
    private static final ResourceLocation SKIN_BOSS_GLOWING_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_boss_glowing.png");
    private static final ResourceLocation SKIN_PIZZA_CHEF_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_pizza_chef.png");
    private static final ResourceLocation SKIN_DETECTIVE_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_detective.png");
    private static final ResourceLocation SKIN_MAGMA_GLOWING_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_magma_glowing.png");
    private static final ResourceLocation SKIN_VIRUS_GLOWING_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_virus_glowing.png");
    private static final ResourceLocation SKIN_DAMNED_GLOWING_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_damned_glowing.png");

    private static final ResourceLocation SKIN_VIRUS_FACE_1_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_virus_face_1.png");
    private static final ResourceLocation SKIN_VIRUS_FACE_2_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_virus_face_2.png");
    private static final ResourceLocation SKIN_VIRUS_FACE_3_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_virus_face_3.png");
    private static final ResourceLocation SKIN_VIRUS_FACE_4_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_virus_face_4.png");
    private static final ResourceLocation SKIN_VIRUS_FACE_5_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_virus_face_5.png");

    public TigerSkins(TigerRenderer tigerRenderer) {
        super(tigerRenderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, TigerEntity tiger, float v, float v1, float v2, float v3, float v4, float v5) {
        if (tiger.getVariant() == TigerVariant.SKIN_GOLD) renderOverlay(poseStack, multiBufferSource, SKIN_GOLD_GLOWING_TEXTURE, true, packedLight);
        if (tiger.getVariant() == TigerVariant.SKIN_MAGMA) renderOverlay(poseStack, multiBufferSource, SKIN_MAGMA_GLOWING_TEXTURE, true, packedLight);
        if (tiger.getVariant() == TigerVariant.SKIN_DAMNED) renderOverlay(poseStack, multiBufferSource, SKIN_DAMNED_GLOWING_TEXTURE, true, packedLight);
        if (tiger.getVariant() == TigerVariant.SKIN_VIRUS) {
            renderOverlay(poseStack, multiBufferSource, SKIN_VIRUS_GLOWING_TEXTURE, true, packedLight);

            if (tiger.isMad()) {
                renderOverlay(poseStack, multiBufferSource, SKIN_VIRUS_FACE_1_TEXTURE, true, packedLight);
            } else if (!tiger.isSleeping() && !tiger.isNapping() && !tiger.isSitting()) {
                ResourceLocation faceTexture = getAnimatedFaceTexture(tiger);
                renderOverlay(poseStack, multiBufferSource, faceTexture, true, packedLight);
            }

            if (tiger.isSleeping() || tiger.isNapping() || tiger.isSitting()) {
                renderOverlay(poseStack, multiBufferSource, SKIN_VIRUS_FACE_3_TEXTURE, true, packedLight);
            }
        }

        if (tiger.isBoss()) {
            renderOverlay(poseStack, multiBufferSource, SKIN_BOSS_TEXTURE, false, packedLight);
            renderOverlay(poseStack, multiBufferSource, SKIN_BOSS_GLOWING_TEXTURE, true, packedLight);
        }
        if (tiger.isPizzaChef()) renderOverlay(poseStack, multiBufferSource, SKIN_PIZZA_CHEF_TEXTURE, false, packedLight);
        if (tiger.isDetective()) renderOverlay(poseStack, multiBufferSource, SKIN_DETECTIVE_TEXTURE, false, packedLight);
    }

    private final Map<UUID, Long> nextChangeTimeMap = new HashMap<>();
    private final Map<UUID, Integer> currentTextureMap = new HashMap<>();

    private ResourceLocation getAnimatedFaceTexture(TigerEntity tiger) {
        long currentTime = tiger.level().getGameTime();
        UUID tigerID = tiger.getUUID();

        if (!nextChangeTimeMap.containsKey(tigerID) || currentTime >= nextChangeTimeMap.get(tigerID)) {
            int randomInterval = tiger.getRandom().nextInt(50) + 25;
            nextChangeTimeMap.put(tigerID, currentTime + randomInterval);
            int newTextureIndex = tiger.getRandom().nextInt(3);
            currentTextureMap.put(tigerID, newTextureIndex);
        }

        int textureIndex = currentTextureMap.getOrDefault(tigerID, 0);

        switch (textureIndex) {
            case 0:
                return SKIN_VIRUS_FACE_2_TEXTURE;
            case 1:
                return SKIN_VIRUS_FACE_4_TEXTURE;
            case 2:
                return SKIN_VIRUS_FACE_5_TEXTURE;
            default:
                return SKIN_VIRUS_FACE_2_TEXTURE;
        }
    }

    private void renderOverlay(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, boolean glowLayer, int packedLight) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(glowLayer ? RenderType.eyes(texture) : RenderType.entityCutout(texture));
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, glowLayer ? 15728640 : packedLight, OverlayTexture.NO_OVERLAY);
    }

    private void renderOverlayWithColor(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, boolean glowLayer, int packedLight, int color) {
        RenderType renderType = glowLayer ? RenderType.eyes(texture) : RenderType.entityCutoutNoCull(texture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, glowLayer ? 15728640 : packedLight, OverlayTexture.NO_OVERLAY, color);
    }


}