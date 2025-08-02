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
import net.tiew.operationWild.entity.client.layer.PeacockLayer;
import net.tiew.operationWild.entity.client.layer.skins.PeacockSkins;
import net.tiew.operationWild.entity.client.model.PeacockModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.custom.living.PeacockEntity;
import net.tiew.operationWild.entity.variants.PeacockVariant;

import java.util.Map;

public class PeacockRenderer extends MobRenderer<PeacockEntity, PeacockModel<PeacockEntity>> {
    private static final Map<PeacockVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(PeacockVariant.class), map -> {
        map.put(PeacockVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/peacock/peacock_default.png"));
        map.put(PeacockVariant.RED, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/peacock/peacock_red.png"));
        map.put(PeacockVariant.ALBINO, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/peacock/peacock_albino.png"));
        map.put(PeacockVariant.GREEN, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/peacock/peacock_green.png"));
        map.put(PeacockVariant.BLUE, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/peacock/peacock_blue.png"));

        map.put(PeacockVariant.SKIN_GOLD, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/peacock/skins/peacock_skin_gold.png"));
    });
    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");

    public PeacockRenderer(EntityRendererProvider.Context context) {
        super(context, new PeacockModel<>(context.bakeLayer(PeacockModel.LAYER_LOCATION)), 0.4f);
        this.addLayer(new PeacockSkins(this));
        this.addLayer(new PeacockLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(PeacockEntity tigerEntity) {
        return LOCATION_BY_VARIANT.get(tigerEntity.getVariant());
    }

    @Override
    public void render(PeacockEntity peacock, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float scale = peacock.getScale();
        float babyScale = scale / 3.0f;
        int genderPosition = peacock.isFemale() ? 36 : peacock.isMale() ? 48 : 0;
        Player player = peacock.level().getNearestPlayer(peacock, 64.0D);

        if (peacock.isBaby()) {
            float maturationPercent = (float) peacock.getMaturationPercentage() / 100f;
            float currentScale = babyScale + (scale - babyScale) * maturationPercent;
            poseStack.scale(currentScale, currentScale, currentScale);
        } else poseStack.scale(scale, scale, scale);

        super.render(peacock, entityYaw, partialTicks, poseStack, bufferSource, packedLight);

        if (!peacock.isInResurrection()) {
            if (peacock.isAlive() && !peacock.isVehicle()) {
                if (peacock.isTame()) {
                    if (peacock.isBaby() && player != null) {
                        OWRendererUtils.displayTimeLeftBeforeBabyTaskAboveEntity(peacock, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, peacock.distanceTo(player) > 3 ? 0 : Minecraft.getInstance().options.hideGui ? 0 : 0.75f);
                        OWRendererUtils.displayImageAboveEntity(ICONS, 0, 154, 48, 256, -2.5f, peacock.distanceTo(player) > 3 ? 2.6f : Minecraft.getInstance().options.hideGui ? 2.6f : 5.5f, peacock, poseStack, bufferSource, packedLight, false);
                    }
                    if (player != null && peacock.distanceTo(player) > 3D) {
                        OWRendererUtils.displayOwnerAboveEntity(peacock, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayLevelAboveEntity(peacock, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayImageAboveEntity(ICONS, 0, genderPosition, 12, 256, 0, 0, peacock, poseStack, bufferSource, packedLight, true);
                        if (peacock.isPassive())
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 60, 14, 256, 1.5f, 0f, peacock, poseStack, bufferSource, packedLight, true);
                        if (peacock.getLevel() >= 50) {
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 143, 10, 256, -1f, 1f, peacock, poseStack, bufferSource, packedLight, true);
                            OWRendererUtils.displayPrestigeLevelAboveEntity(peacock, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        }
                    }
                }
            }
        }
        if (!peacock.isFearEntities()) OWRendererUtils.createInformationImage(peacock, poseStack, bufferSource, packedLight, 0, 0.5, 0, 0, 2);
    }

    @Override
    protected void renderNameTag(PeacockEntity tiger, Component component, PoseStack poseStack, MultiBufferSource bufferSource, int i, float v) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.65D, 0.0D);
        super.renderNameTag(tiger, component, poseStack, bufferSource, i, v);
        poseStack.popPose();
    }
}
