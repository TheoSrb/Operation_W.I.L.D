package net.tiew.operationWild.entity.client.render;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.layer.MandrillLayer;
import net.tiew.operationWild.entity.client.model.MandrillModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.custom.living.MandrillEntity;
import net.tiew.operationWild.entity.variants.MandrillVariant;

import java.util.Map;public class MandrillRenderer extends MobRenderer<MandrillEntity, MandrillModel<MandrillEntity>> {
    private static final Map<MandrillVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(MandrillVariant.class), map -> {
        map.put(MandrillVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/mandrill/mandrill_default.png"));
        map.put(MandrillVariant.BLUE, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/mandrill/mandrill_blue.png"));
        map.put(MandrillVariant.SILVER, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/mandrill/mandrill_silver.png"));
    });
    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");

    public MandrillRenderer(EntityRendererProvider.Context context) {
        super(context, new MandrillModel<>(context.bakeLayer(MandrillModel.LAYER_LOCATION)), 0.7f);
        this.addLayer(new MandrillLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(MandrillEntity mandrill) {
        return LOCATION_BY_VARIANT.get(mandrill.getVariant());
    }

    @Override
    public void render(MandrillEntity mandrill, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float scale = mandrill.getScale();
        float babyScale = scale / 2.25f;
        int genderPosition = mandrill.isFemale() ? 36 : mandrill.isMale() ? 48 : 0;
        Player player = mandrill.level().getNearestPlayer(mandrill, 64.0D);

        poseStack.pushPose();

        if (mandrill.isBaby()) {
            float maturationPercent = (float) mandrill.getMaturationPercentage() / 100f;
            float currentScale = babyScale + (scale - babyScale) * maturationPercent;
            poseStack.scale(currentScale, currentScale, currentScale);
        } else {
            poseStack.scale(scale, scale, scale);
        }

        super.render(mandrill, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        poseStack.popPose();

        if (!mandrill.isInResurrection()) {
            if (mandrill.isAlive() && !mandrill.isVehicle()) {
                if (mandrill.isTame()) {
                    if (player != null && mandrill.distanceTo(player) > 4.0D) {
                        OWRendererUtils.displayOwnerAboveEntity(mandrill, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayLevelAboveEntity(mandrill, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayImageAboveEntity(ICONS, 0, genderPosition, 12, 256, 0, 0, mandrill, poseStack, bufferSource, packedLight, true);
                        if (mandrill.isPassive())
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 60, 14, 256, 1.5f, 0f, mandrill, poseStack, bufferSource, packedLight, true);
                        if (mandrill.getLevel() >= 50) {
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 143, 10, 256, -1f, 1f, mandrill, poseStack, bufferSource, packedLight, true);
                            OWRendererUtils.displayPrestigeLevelAboveEntity(mandrill, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        }
                    }
                } else {
                    if (mandrill.isSleeping()) {
                        OWRendererUtils.displayBonusPointAboveEntity(mandrill, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, 0);
                    }
                }
            }
        }
        OWRendererUtils.createInformationImage(mandrill, poseStack, bufferSource, packedLight, 0, 0.5, 0, 0, 2);
    }

    @Override
    protected void renderNameTag(MandrillEntity mandrill, Component component, PoseStack poseStack, MultiBufferSource bufferSource, int i, float v) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.65D, 0.0D);
        super.renderNameTag(mandrill, component, poseStack, bufferSource, i, v);
        poseStack.popPose();
    }
}
