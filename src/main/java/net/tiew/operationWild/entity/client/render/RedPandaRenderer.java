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
import net.tiew.operationWild.entity.client.layer.RedPandaLayer;
import net.tiew.operationWild.entity.client.model.RedPandaModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.custom.living.RedPandaEntity;
import net.tiew.operationWild.entity.variants.RedPandaVariant;

import java.util.Map;

public class RedPandaRenderer extends MobRenderer<RedPandaEntity, RedPandaModel<RedPandaEntity>> {
    private static final Map<RedPandaVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(RedPandaVariant.class), map -> {
        map.put(RedPandaVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/red_panda/red_panda_default.png"));
        map.put(RedPandaVariant.DARK, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/red_panda/red_panda_dark.png"));
    });
    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");

    public RedPandaRenderer(EntityRendererProvider.Context context) {
        super(context, new RedPandaModel<>(context.bakeLayer(RedPandaModel.LAYER_LOCATION)), 0.4f);
        this.addLayer(new RedPandaLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(RedPandaEntity red_panda) {
        return LOCATION_BY_VARIANT.get(red_panda.getVariant());
    }

    @Override
    public void render(RedPandaEntity red_panda, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float scale = red_panda.getScale();
        float babyScale = scale / 2.25f;
        int genderPosition = red_panda.isFemale() ? 36 : red_panda.isMale() ? 48 : 0;
        Player player = red_panda.level().getNearestPlayer(red_panda, 64.0D);

        poseStack.pushPose();

        if (red_panda.isBaby()) {
            float maturationPercent = (float) red_panda.getMaturationPercentage() / 100f;
            float currentScale = babyScale + (scale - babyScale) * maturationPercent;
            poseStack.scale(currentScale, currentScale, currentScale);
        } else {
            poseStack.scale(scale, scale, scale);
        }

        super.render(red_panda, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        poseStack.popPose();

        if (!red_panda.isInResurrection()) {
            if (red_panda.isAlive() && !red_panda.isVehicle()) {
                if (red_panda.isTame()) {
                    if (player != null && red_panda.distanceTo(player) > 4.0D) {
                        OWRendererUtils.displayOwnerAboveEntity(red_panda, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayLevelAboveEntity(red_panda, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayImageAboveEntity(ICONS, 0, genderPosition, 12, 256, 0, 0, red_panda, poseStack, bufferSource, packedLight, true);
                        if (red_panda.isPassive())
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 60, 14, 256, 1.5f, 0f, red_panda, poseStack, bufferSource, packedLight, true);
                        if (red_panda.getLevel() >= 50) {
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 143, 10, 256, -1f, 1f, red_panda, poseStack, bufferSource, packedLight, true);
                            OWRendererUtils.displayPrestigeLevelAboveEntity(red_panda, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        }
                    }
                } else {
                    if (red_panda.isSleeping()) {
                        OWRendererUtils.displayBonusPointAboveEntity(red_panda, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, 0);
                    }
                }
            }
        }
        OWRendererUtils.createInformationImage(red_panda, poseStack, bufferSource, packedLight, 0, 0, 0, 0, 2);
    }

    @Override
    protected void renderNameTag(RedPandaEntity red_panda, Component component, PoseStack poseStack, MultiBufferSource bufferSource, int i, float v) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.65D, 0.0D);
        super.renderNameTag(red_panda, component, poseStack, bufferSource, i, v);
        poseStack.popPose();
    }
}
