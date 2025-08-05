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
import net.tiew.operationWild.entity.client.layer.HyenaLayer;
import net.tiew.operationWild.entity.client.model.HyenaModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.custom.living.HyenaEntity;
import net.tiew.operationWild.entity.variants.HyenaVariant;

import java.util.Map;

public class HyenaRenderer extends MobRenderer<HyenaEntity, HyenaModel<HyenaEntity>> {
    private static final Map<HyenaVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(HyenaVariant.class), map -> {
        map.put(HyenaVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/hyena/hyena_default.png"));
    });
    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");

    public HyenaRenderer(EntityRendererProvider.Context context) {
        super(context, new HyenaModel<>(context.bakeLayer(HyenaModel.LAYER_LOCATION)), 0.4f);
        this.addLayer(new HyenaLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(HyenaEntity hyena) {
        return LOCATION_BY_VARIANT.get(hyena.getVariant());
    }

    @Override
    public void render(HyenaEntity hyena, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float scale = hyena.getScale();
        float babyScale = scale / 2.25f;
        int genderPosition = hyena.isFemale() ? 36 : hyena.isMale() ? 48 : 0;
        Player player = hyena.level().getNearestPlayer(hyena, 64.0D);

        poseStack.pushPose();

        if (hyena.isBaby()) {
            float maturationPercent = (float) hyena.getMaturationPercentage() / 100f;
            float currentScale = babyScale + (scale - babyScale) * maturationPercent;
            poseStack.scale(currentScale, currentScale, currentScale);
        } else {
            poseStack.scale(scale, scale, scale);
        }

        super.render(hyena, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        poseStack.popPose();

        if (!hyena.isInResurrection()) {
            if (hyena.isAlive() && !hyena.isVehicle()) {
                if (hyena.isTame()) {
                    if (player != null && hyena.distanceTo(player) > 4.0D) {
                        OWRendererUtils.displayOwnerAboveEntity(hyena, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayLevelAboveEntity(hyena, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayImageAboveEntity(ICONS, 0, genderPosition, 12, 256, 0, 0, hyena, poseStack, bufferSource, packedLight, true);
                        if (hyena.isPassive())
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 60, 14, 256, 1.5f, 0f, hyena, poseStack, bufferSource, packedLight, true);
                        if (hyena.getLevel() >= 50) {
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 143, 10, 256, -1f, 1f, hyena, poseStack, bufferSource, packedLight, true);
                            OWRendererUtils.displayPrestigeLevelAboveEntity(hyena, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        }
                    }
                } else {
                    if (hyena.isSleeping()) {
                        OWRendererUtils.displayBonusPointAboveEntity(hyena, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, 0);
                    }
                }
            }
        }
        OWRendererUtils.createInformationImage(hyena, poseStack, bufferSource, packedLight, 0, 0, 0, 0, 2);
    }

    @Override
    protected void renderNameTag(HyenaEntity hyena, Component component, PoseStack poseStack, MultiBufferSource bufferSource, int i, float v) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.65D, 0.0D);
        super.renderNameTag(hyena, component, poseStack, bufferSource, i, v);
        poseStack.popPose();
    }
}
