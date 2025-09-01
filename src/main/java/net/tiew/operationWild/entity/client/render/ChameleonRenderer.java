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
import net.tiew.operationWild.entity.client.layer.ChameleonLayer;
import net.tiew.operationWild.entity.client.model.ChameleonModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.animals.terrestrial.ChameleonEntity;
import net.tiew.operationWild.entity.variants.ChameleonVariant;

import java.util.Map;

public class ChameleonRenderer extends MobRenderer<ChameleonEntity, ChameleonModel<ChameleonEntity>> {
    private static final Map<ChameleonVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(ChameleonVariant.class), map -> {
        map.put(ChameleonVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/chameleon/chameleon_default.png"));
    });
    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");

    public ChameleonRenderer(EntityRendererProvider.Context context) {
        super(context, new ChameleonModel<>(context.bakeLayer(ChameleonModel.LAYER_LOCATION)), 0.4f);
        this.addLayer(new ChameleonLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(ChameleonEntity chameleon) {
        return LOCATION_BY_VARIANT.get(chameleon.getVariant());
    }

    @Override
    public void render(ChameleonEntity chameleon, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float scale = chameleon.getScale();
        float babyScale = scale / 2.25f;
        int genderPosition = chameleon.isFemale() ? 36 : chameleon.isMale() ? 48 : 0;
        Player player = chameleon.level().getNearestPlayer(chameleon, 64.0D);

        poseStack.pushPose();

        if (chameleon.isBaby()) {
            float maturationPercent = (float) chameleon.getMaturationPercentage() / 100f;
            float currentScale = babyScale + (scale - babyScale) * maturationPercent;
            poseStack.scale(currentScale, currentScale, currentScale);
        } else {
            poseStack.scale(scale, scale, scale);
        }

        super.render(chameleon, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        poseStack.popPose();

        if (!chameleon.isInResurrection()) {
            if (chameleon.isAlive() && !chameleon.isVehicle()) {
                if (chameleon.isTame()) {
                    if (player != null && chameleon.distanceTo(player) > 4.0D) {
                        OWRendererUtils.displayOwnerAboveEntity(chameleon, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayLevelAboveEntity(chameleon, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayImageAboveEntity(ICONS, 0, genderPosition, 12, 256, 0, 0, chameleon, poseStack, bufferSource, packedLight, true);
                        if (chameleon.isPassive())
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 60, 14, 256, 1.5f, 0f, chameleon, poseStack, bufferSource, packedLight, true);
                        if (chameleon.getLevel() >= 50) {
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 143, 10, 256, -1f, 1f, chameleon, poseStack, bufferSource, packedLight, true);
                            OWRendererUtils.displayPrestigeLevelAboveEntity(chameleon, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        }
                    }
                } else {
                    if (chameleon.isSleeping()) {
                        OWRendererUtils.displayBonusPointAboveEntity(chameleon, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, 0);
                    }
                }
            }
        }
        OWRendererUtils.createInformationImage(chameleon, poseStack, bufferSource, packedLight, 0, 0, 0, 0, 1);
    }

    @Override
    protected void renderNameTag(ChameleonEntity chameleon, Component component, PoseStack poseStack, MultiBufferSource bufferSource, int i, float v) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.65D, 0.0D);
        super.renderNameTag(chameleon, component, poseStack, bufferSource, i, v);
        poseStack.popPose();
    }
}
