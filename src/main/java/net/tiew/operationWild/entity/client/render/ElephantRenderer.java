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
import net.tiew.operationWild.entity.client.layer.ElephantLayer;
import net.tiew.operationWild.entity.client.model.ElephantModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.custom.living.ElephantEntity;
import net.tiew.operationWild.entity.variants.ElephantVariant;

import java.util.Map;public class ElephantRenderer extends MobRenderer<ElephantEntity, ElephantModel<ElephantEntity>> {
    private static final Map<ElephantVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(ElephantVariant.class), map -> {
        map.put(ElephantVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/elephant_default.png"));
        map.put(ElephantVariant.GREY, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/elephant_grey.png"));
        map.put(ElephantVariant.PINK, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/elephant_pink.png"));
    });
    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");

    public ElephantRenderer(EntityRendererProvider.Context context) {
        super(context, new ElephantModel<>(context.bakeLayer(ElephantModel.LAYER_LOCATION)), 1.5f);
        this.addLayer(new ElephantLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(ElephantEntity elephant) {
        return LOCATION_BY_VARIANT.get(elephant.getVariant());
    }

    @Override
    public void render(ElephantEntity elephant, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float scale = elephant.getScale();
        float babyScale = scale / 2.25f;
        int genderPosition = elephant.isFemale() ? 36 : elephant.isMale() ? 48 : 0;
        Player player = elephant.level().getNearestPlayer(elephant, 64.0D);

        poseStack.pushPose();

        if (elephant.isBaby()) {
            float maturationPercent = (float) elephant.getMaturationPercentage() / 100f;
            float currentScale = babyScale + (scale - babyScale) * maturationPercent;
            poseStack.scale(currentScale, currentScale, currentScale);
        } else {
            poseStack.scale(scale, scale, scale);
        }

        super.render(elephant, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        poseStack.popPose();

        if (!elephant.isInResurrection()) {
            if (elephant.isAlive() && !elephant.isVehicle()) {
                if (elephant.isTame()) {
                    if (player != null && elephant.distanceTo(player) > 4.0D) {
                        OWRendererUtils.displayOwnerAboveEntity(elephant, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayLevelAboveEntity(elephant, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayImageAboveEntity(ICONS, 0, genderPosition, 12, 256, 0, 0, elephant, poseStack, bufferSource, packedLight, true);
                        if (elephant.isPassive())
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 60, 14, 256, 1.5f, 0f, elephant, poseStack, bufferSource, packedLight, true);
                        if (elephant.getLevel() >= 50) {
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 143, 10, 256, -1f, 1f, elephant, poseStack, bufferSource, packedLight, true);
                            OWRendererUtils.displayPrestigeLevelAboveEntity(elephant, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        }
                    }
                } else {
                    if (elephant.isSleeping()) {
                        OWRendererUtils.displayBonusPointAboveEntity(elephant, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, 0);
                    }
                }
            }
        }
        OWRendererUtils.createInformationImage(elephant, poseStack, bufferSource, packedLight, 0, 1.5, 0, 0, 4);
    }

    @Override
    protected void renderNameTag(ElephantEntity elephant, Component component, PoseStack poseStack, MultiBufferSource bufferSource, int i, float v) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.65D, 0.0D);
        super.renderNameTag(elephant, component, poseStack, bufferSource, i, v);
        poseStack.popPose();
    }
}
