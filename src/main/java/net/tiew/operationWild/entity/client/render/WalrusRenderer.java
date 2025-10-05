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
import net.tiew.operationWild.entity.client.layer.WalrusLayer;
import net.tiew.operationWild.entity.client.model.WalrusModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.animals.terrestrial.WalrusEntity;
import net.tiew.operationWild.entity.variants.WalrusVariant;

import java.util.Map;

public class WalrusRenderer extends MobRenderer<WalrusEntity, WalrusModel<WalrusEntity>> {
    private static final Map<WalrusVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(WalrusVariant.class), map -> {
        map.put(WalrusVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/walrus/walrus_default.png"));
        map.put(WalrusVariant.RED, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/walrus/walrus_red.png"));
    });
    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");

    public WalrusRenderer(EntityRendererProvider.Context context) {
        super(context, new WalrusModel<>(context.bakeLayer(WalrusModel.LAYER_LOCATION)), 0.4f);
        this.addLayer(new WalrusLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(WalrusEntity walrus) {
        return LOCATION_BY_VARIANT.get(walrus.getVariant());
    }

    @Override
    public void render(WalrusEntity walrus, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float scale = walrus.getScale() / 1.5f;
        float babyScale = scale / 2.25f;
        int genderPosition = walrus.isFemale() ? 36 : walrus.isMale() ? 48 : 0;
        Player player = walrus.level().getNearestPlayer(walrus, 64.0D);

        poseStack.pushPose();

        if (walrus.isBaby()) {
            float maturationPercent = (float) walrus.getMaturationPercentage() / 100f;
            float currentScale = babyScale + (scale - babyScale) * maturationPercent;
            poseStack.scale(currentScale, currentScale, currentScale);
        } else {
            poseStack.scale(scale, scale, scale);
        }

        super.render(walrus, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        poseStack.popPose();

        if (!walrus.isInResurrection()) {
            if (walrus.isAlive() && !walrus.isVehicle()) {
                if (walrus.isTame()) {
                    if (player != null && walrus.distanceTo(player) > 4.0D) {
                        OWRendererUtils.displayOwnerAboveEntity(walrus, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayLevelAboveEntity(walrus, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayImageAboveEntity(ICONS, 0, genderPosition, 12, 256, 0, 0, walrus, poseStack, bufferSource, packedLight, true);
                        if (walrus.isPassive())
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 60, 14, 256, 1.5f, 0f, walrus, poseStack, bufferSource, packedLight, true);
                        if (walrus.getLevel() >= 50) {
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 143, 10, 256, -1f, 1f, walrus, poseStack, bufferSource, packedLight, true);
                            OWRendererUtils.displayPrestigeLevelAboveEntity(walrus, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        }
                    }
                } else {
                    if (walrus.isSleeping()) {
                        OWRendererUtils.displayBonusPointAboveEntity(walrus, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, 0);
                    }
                }
            }
        }
        OWRendererUtils.createInformationImage(walrus, poseStack, bufferSource, packedLight, 0, 0, 0, 0, 2);
    }

    @Override
    protected void renderNameTag(WalrusEntity walrus, Component component, PoseStack poseStack, MultiBufferSource bufferSource, int i, float v) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.65D, 0.0D);
        super.renderNameTag(walrus, component, poseStack, bufferSource, i, v);
        poseStack.popPose();
    }
}
