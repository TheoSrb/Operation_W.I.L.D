package net.tiew.operationWild.entity.client.render;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.layer.TigerSharkLayer;
import net.tiew.operationWild.entity.client.model.TigerSharkModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.animals.aquatic.TigerSharkEntity;
import net.tiew.operationWild.entity.variants.TigerSharkVariant;

import java.util.Map;

public class TigerSharkRenderer extends MobRenderer<TigerSharkEntity, TigerSharkModel<TigerSharkEntity>> {
    private static final Map<TigerSharkVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(TigerSharkVariant.class), map -> {
        map.put(TigerSharkVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger_shark/tiger_shark_default.png"));
        map.put(TigerSharkVariant.BLUE, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger_shark/tiger_shark_blue.png"));
        map.put(TigerSharkVariant.GREY, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger_shark/tiger_shark_grey.png"));

        map.put(TigerSharkVariant.SKIN_GOLD, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger_shark/skins/tiger_shark_skin_gold.png"));
    });
    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");

    public TigerSharkRenderer(EntityRendererProvider.Context context) {
        super(context, new TigerSharkModel<>(context.bakeLayer(TigerSharkModel.LAYER_LOCATION)), 0.6f);
        //this.addLayer(new TigerSharkSkins(this));
        this.addLayer(new TigerSharkLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(TigerSharkEntity tigerSharkEntity) {
        return LOCATION_BY_VARIANT.get(tigerSharkEntity.getVariant());
    }

    @Override
    public void render(TigerSharkEntity tigerShark, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float scale = tigerShark.getScale();
        float babyScale = scale / 3.0f;
        int genderPosition = tigerShark.isFemale() ? 36 : tigerShark.isMale() ? 48 : 0;
        Player player = tigerShark.level().getNearestPlayer(tigerShark, 64.0D);

        if (tigerShark.isBaby()) {
            float maturationPercent = (float) tigerShark.getMaturationPercentage() / 100f;
            float currentScale = babyScale + (scale - babyScale) * maturationPercent;
            poseStack.scale(currentScale, currentScale, currentScale);
        } else poseStack.scale(scale, scale, scale);

        super.render(tigerShark, entityYaw, partialTicks, poseStack, bufferSource, packedLight);

        if (!tigerShark.isInResurrection()) {
            if (tigerShark.isAlive() && !tigerShark.isVehicle()) {
                if (tigerShark.isTame()) {
                    if (tigerShark.isBaby() && player != null) {
                        OWRendererUtils.displayTimeLeftBeforeBabyTaskAboveEntity(tigerShark, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, tigerShark.distanceTo(player) > 3 ? 0 : Minecraft.getInstance().options.hideGui ? 0 : 0.75f);
                        OWRendererUtils.displayImageAboveEntity(ICONS, 0, 154, 48, 256, -2.5f, tigerShark.distanceTo(player) > 3 ? 2.6f : Minecraft.getInstance().options.hideGui ? 2.6f : 5.5f, tigerShark, poseStack, bufferSource, packedLight, false);
                    }
                    if (player != null && tigerShark.distanceTo(player) > 3D) {
                        OWRendererUtils.displayOwnerAboveEntity(tigerShark, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayLevelAboveEntity(tigerShark, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayImageAboveEntity(ICONS, 0, genderPosition, 12, 256, 0, 0, tigerShark, poseStack, bufferSource, packedLight, true);
                        if (tigerShark.isPassive())
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 60, 14, 256, 1.5f, 0f, tigerShark, poseStack, bufferSource, packedLight, true);
                        if (tigerShark.getLevel() >= 50) {
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 143, 10, 256, -1f, 1f, tigerShark, poseStack, bufferSource, packedLight, true);
                            OWRendererUtils.displayPrestigeLevelAboveEntity(tigerShark, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        }
                    }
                }
            }
        }
        OWRendererUtils.createInformationImage(tigerShark, poseStack, bufferSource, packedLight, 0, 0.25, 0, 0, 3);
    }

    @Override
    protected void renderNameTag(TigerSharkEntity tigerSharkEntity, Component component, PoseStack poseStack, MultiBufferSource bufferSource, int i, float v) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.65D, 0.0D);
        super.renderNameTag(tigerSharkEntity, component, poseStack, bufferSource, i, v);
        poseStack.popPose();
    }
}
