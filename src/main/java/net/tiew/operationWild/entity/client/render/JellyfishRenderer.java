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
import net.tiew.operationWild.entity.client.layer.JellyfishLayer;
import net.tiew.operationWild.entity.client.model.JellyfishModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.custom.living.JellyfishEntity;
import net.tiew.operationWild.entity.variants.JellyfishVariant;

import java.util.Map;

public class JellyfishRenderer extends MobRenderer<JellyfishEntity, JellyfishModel<JellyfishEntity>> {
    private static final Map<JellyfishVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(JellyfishVariant.class), map -> {
        map.put(JellyfishVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/jellyfish/jellyfish_default.png"));
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
    public void render(JellyfishEntity jellyfish, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float scale = jellyfish.getScale();
        float babyScale = scale / 2.25f;
        int genderPosition = jellyfish.isFemale() ? 36 : jellyfish.isMale() ? 48 : 0;
        Player player = jellyfish.level().getNearestPlayer(jellyfish, 64.0D);

        poseStack.pushPose();

        if (jellyfish.isBaby()) {
            float maturationPercent = (float) jellyfish.getMaturationPercentage() / 100f;
            float currentScale = babyScale + (scale - babyScale) * maturationPercent;
            poseStack.scale(currentScale, currentScale, currentScale);
        } else {
            poseStack.scale(scale, scale, scale);
        }

        super.render(jellyfish, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        poseStack.popPose();

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
        OWRendererUtils.createInformationImage(jellyfish, poseStack, bufferSource, packedLight, 0, 0, 0, 0, 2);
    }

    @Override
    protected void renderNameTag(JellyfishEntity jellyfish, Component component, PoseStack poseStack, MultiBufferSource bufferSource, int i, float v) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.65D, 0.0D);
        super.renderNameTag(jellyfish, component, poseStack, bufferSource, i, v);
        poseStack.popPose();
    }
}
