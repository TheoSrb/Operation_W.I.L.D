package net.tiew.operationWild.entity.client.render;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.layer.JellyfishLayer;
import net.tiew.operationWild.entity.client.model.JellyfishModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.animals.aquatic.JellyfishEntity;
import net.tiew.operationWild.entity.variants.JellyfishVariant;

import java.util.Map;

public class JellyfishRenderer extends MobRenderer<JellyfishEntity, JellyfishModel<JellyfishEntity>> {
    private static final Map<JellyfishVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(JellyfishVariant.class), map -> {
        map.put(JellyfishVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/jellyfish/jellyfish_default.png"));
        map.put(JellyfishVariant.PINK, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/jellyfish/jellyfish_pink.png"));
        map.put(JellyfishVariant.ORANGE, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/jellyfish/jellyfish_orange.png"));
        map.put(JellyfishVariant.GREEN, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/jellyfish/jellyfish_green.png"));
        map.put(JellyfishVariant.PURPLE, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/jellyfish/jellyfish_purple.png"));
        map.put(JellyfishVariant.WHITE, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/jellyfish/jellyfish_white.png"));
        map.put(JellyfishVariant.ELECTRIFIED, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/jellyfish/jellyfish_electrified.png"));
    });
    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");

    public JellyfishRenderer(EntityRendererProvider.Context context) {
        super(context, new JellyfishModel<>(context.bakeLayer(JellyfishModel.LAYER_LOCATION)), 0.4f);
        this.addLayer(new JellyfishLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(JellyfishEntity jellyfish) {
        return LOCATION_BY_VARIANT.get(jellyfish.getVariant());
    }

    @Override
    public RenderType getRenderType(JellyfishEntity jellyfish, boolean bodyVisible, boolean translucent, boolean glowing) {
        ResourceLocation texture = this.getTextureLocation(jellyfish);
        return RenderType.entityTranslucent(texture);
    }

    private void renderJellyfish(JellyfishEntity jellyfish, float scale, boolean glowLayer, PoseStack poseStack, MultiBufferSource bufferSource, float opacity) {
        if (jellyfish == null || poseStack == null || bufferSource == null) {
            return;
        }

        opacity = Math.max(0.0f, Math.min(1.0f, opacity));
        int alpha = (int)(opacity * 255.0f);
        int color = 0xFFFFFF | (alpha << 24);

        poseStack.pushPose();
        try {
            poseStack.scale(scale, scale, scale);

            ResourceLocation texture = this.getTextureLocation(jellyfish);
            if (texture == null) {
                return;
            }

            RenderType renderType = glowLayer ? RenderType.eyes(texture) : RenderType.entityTranslucent(texture);
            VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

            int customLight = LightTexture.pack(12, 12);
            int overlay = OverlayTexture.pack(OverlayTexture.u(jellyfish.hurtTime > 0 ? 1.0F : 0.0F), false);
            this.getModel().renderToBuffer(poseStack, vertexConsumer, glowLayer ? 15728640 : customLight, overlay, color);
        } finally {
            poseStack.popPose();
        }
    }

    @Override
    public void render(JellyfishEntity jellyfish, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (jellyfish == null || poseStack == null || bufferSource == null) {
            return;
        }

        float scale = jellyfish.getScale();
        float babyScale = scale / 2.25f;
        int genderPosition = jellyfish.isFemale() ? 36 : jellyfish.isMale() ? 48 : 0;
        Player player = jellyfish.level().getNearestPlayer(jellyfish, 64.0D);

        poseStack.pushPose();
        try {
            if (jellyfish.isBaby()) {
                float maturationPercent = (float) jellyfish.getMaturationPercentage() / 100f;
                float currentScale = babyScale + (scale - babyScale) * maturationPercent;
                poseStack.scale(currentScale, currentScale, currentScale);
            } else {
                poseStack.scale(scale, scale, scale);
            }
        } finally {
            poseStack.popPose();
        }

        float ageInTicks = jellyfish.tickCount + partialTicks;
        float jellyfishYaw = jellyfish.getViewYRot(partialTicks);
        float jellyfishPitch = jellyfish.getViewXRot(partialTicks);
        float speed = (float) jellyfish.getDeltaMovement().length() * 10f;
        float limbSwing = ageInTicks * 0.6662f;
        this.model.setupAnim(jellyfish, limbSwing, speed, ageInTicks, jellyfishYaw, jellyfishPitch);

        poseStack.pushPose();
        try {
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

            if (jellyfish.deathTime > 0) {
                float deathRotation = ((float)jellyfish.deathTime + partialTicks - 1.0F) * 90.0F / 20.0F;
                poseStack.mulPose(Axis.ZP.rotationDegrees(deathRotation));
            }

            poseStack.pushPose();
            try {
                poseStack.translate(0.0D, -1.5D, 0.0D);
                this.renderJellyfish(jellyfish, 1, false, poseStack, bufferSource , 0.8f);
            } finally {
                poseStack.popPose();
            }

            poseStack.pushPose();
            try {
                poseStack.translate(0.0D, -1.55D, 0.0D);
                this.renderJellyfish(jellyfish, 1.2f, true , poseStack, bufferSource , 0.65f);
            } finally {
                poseStack.popPose();
            }

            poseStack.pushPose();
            try {
                poseStack.translate(0.0D, -1.45D, 0.0D);
                this.renderJellyfish(jellyfish, 0.8f, true, poseStack, bufferSource, 0.65f);
            } finally {
                poseStack.popPose();
            }
        } finally {
            poseStack.popPose();
        }

        if (!jellyfish.isInResurrection()) {
            if (jellyfish.isAlive() && !jellyfish.isVehicle()) {
                if (jellyfish.isTame()) {
                    if (player != null && jellyfish.distanceTo(player) > 4.0D) {
                        OWRendererUtils.displayOwnerAboveEntity(jellyfish, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayLevelAboveEntity(jellyfish, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayImageAboveEntity(ICONS, 0, genderPosition, 12, 256, 0, 0, jellyfish, poseStack, bufferSource, packedLight, true);
                        if (jellyfish.isPassive())
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 60, 14, 256, 1.5f, 0f, jellyfish, poseStack, bufferSource, packedLight, true);
                        if (jellyfish.getLevel() >= 50) {
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 143, 10, 256, -1f, 1f, jellyfish, poseStack, bufferSource, packedLight, true);
                            OWRendererUtils.displayPrestigeLevelAboveEntity(jellyfish, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        }
                    }
                } else {
                    if (jellyfish.isSleeping()) {
                        OWRendererUtils.displayBonusPointAboveEntity(jellyfish, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, 0);
                    }
                }
            }
        }
        OWRendererUtils.createInformationImage(jellyfish, poseStack, bufferSource, packedLight, 0, 0.5, 0, 0, 2);
    }

    @Override
    protected void renderNameTag(JellyfishEntity jellyfish, Component component, PoseStack poseStack, MultiBufferSource bufferSource, int i, float v) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.65D, 0.0D);
        super.renderNameTag(jellyfish, component, poseStack, bufferSource, i, v);
        poseStack.popPose();
    }
}
