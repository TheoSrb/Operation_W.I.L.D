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
import net.tiew.operationWild.entity.client.layer.ElephantLayer;
import net.tiew.operationWild.entity.client.layer.skins.ElephantSkins;
import net.tiew.operationWild.entity.client.model.ElephantModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.variants.ElephantVariant;

import java.util.Map;public class ElephantRenderer extends MobRenderer<ElephantEntity, ElephantModel<ElephantEntity>> {
    private static final Map<ElephantVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(ElephantVariant.class), map -> {
        map.put(ElephantVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/elephant_default.png"));
        map.put(ElephantVariant.GREY, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/elephant_grey.png"));
        map.put(ElephantVariant.PINK, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/elephant_pink.png"));

        map.put(ElephantVariant.SKIN_DEMON, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/skins/elephant_skin_demon.png"));
        map.put(ElephantVariant.SKIN_ZOMBIE, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/skins/elephant_skin_zombie.png"));
        map.put(ElephantVariant.SKIN_GOLD, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/skins/elephant_skin_gold.png"));
    });

    public final ElephantLayer elephantLayer;

    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");

    public ElephantRenderer(EntityRendererProvider.Context context) {
        super(context, new ElephantModel<>(context.bakeLayer(ElephantModel.LAYER_LOCATION)), 1.5f);
        this.elephantLayer = new ElephantLayer(this);
        this.addLayer(this.elephantLayer);
        this.addLayer(new ElephantSkins(this));
    }

    @Override
    public ResourceLocation getTextureLocation(ElephantEntity elephant) {
        return LOCATION_BY_VARIANT.get(elephant.getVariant());
    }

    @Override
    public void render(ElephantEntity elephant, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float scale = elephant.getScale();
        float babyScale = scale / 2.75f;
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
                    if (elephant.isBaby() && player != null) {
                        OWRendererUtils.displayTimeLeftBeforeBabyTaskAboveEntity(elephant, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, elephant.distanceTo(player) > 4 ? 0 : Minecraft.getInstance().options.hideGui ? 0 : 0.75f);
                        OWRendererUtils.displayImageAboveEntity(ICONS, 0, 154, 48, 256, -2.5f, elephant.distanceTo(player) > 4 ? 2.6f : Minecraft.getInstance().options.hideGui ? 2.6f : 5.5f, elephant, poseStack, bufferSource, packedLight, false);
                    }
                    if (player != null && elephant.distanceTo(player) > 4.0D) {
                        OWRendererUtils.displayOwnerAboveEntity(elephant, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayLevelAboveEntity(elephant, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayImageAboveEntity(ICONS, 0, genderPosition, 12, 256, 0, 4f, elephant, poseStack, bufferSource, packedLight, true);
                        if (elephant.isPassive())
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 60, 14, 256, 1.5f, 4f, elephant, poseStack, bufferSource, packedLight, true);
                        if (elephant.getLevel() >= 50) {
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 143, 10, 256, -1f, 5f, elephant, poseStack, bufferSource, packedLight, true);
                            OWRendererUtils.displayPrestigeLevelAboveEntity(elephant, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        }
                    }
                } else {
                    if (elephant.isSleeping()) {
                        OWRendererUtils.displayBonusPointAboveEntity(elephant, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, 1f);
                    }
                }
            }
        }
        OWRendererUtils.createInformationImage(elephant, poseStack, bufferSource, packedLight, 0, elephant.getBbHeight() - (elephant.isBaby() ? (elephant.getMaturationPercentage() / 100) * 1.85 : 1.85), 0, 0, 4);
    }

    @Override
    protected void renderNameTag(ElephantEntity elephant, Component component, PoseStack poseStack, MultiBufferSource bufferSource, int i, float v) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.65D, 0.0D);
        super.renderNameTag(elephant, component, poseStack, bufferSource, i, v);
        poseStack.popPose();
    }
}
