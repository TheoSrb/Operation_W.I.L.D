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
import net.tiew.operationWild.entity.client.layer.KodiakLayer;
import net.tiew.operationWild.entity.client.layer.skins.ElephantSkins;
import net.tiew.operationWild.entity.client.layer.skins.KodiakSkins;
import net.tiew.operationWild.entity.client.model.KodiakModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.custom.living.KodiakEntity;
import net.tiew.operationWild.entity.variants.KodiakVariant;

import java.util.Map;

public class KodiakRenderer extends MobRenderer<KodiakEntity, KodiakModel<KodiakEntity>> {
    private static final Map<KodiakVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(KodiakVariant.class), map -> {
        map.put(KodiakVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/kodiak/kodiak_default.png"));
        map.put(KodiakVariant.BLACK, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/kodiak/kodiak_black.png"));
        map.put(KodiakVariant.GREY, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/kodiak/kodiak_grey.png"));
        map.put(KodiakVariant.SKIN_GOLD, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/kodiak/skins/kodiak_skin_gold.png"));
    });
    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");

    public KodiakRenderer(EntityRendererProvider.Context context) {
        super(context, new KodiakModel<>(context.bakeLayer(KodiakModel.LAYER_LOCATION)), 0.4f);
        this.addLayer(new KodiakLayer(this));
        this.addLayer(new KodiakSkins(this));
    }

    @Override
    public ResourceLocation getTextureLocation(KodiakEntity kodiak) {
        return LOCATION_BY_VARIANT.get(kodiak.getVariant());
    }

    @Override
    public void render(KodiakEntity kodiak, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float scale = kodiak.getScale();
        float babyScale = scale / 2.25f;
        int genderPosition = kodiak.isFemale() ? 36 : kodiak.isMale() ? 48 : 0;
        Player player = kodiak.level().getNearestPlayer(kodiak, 64.0D);

        poseStack.pushPose();

        if (kodiak.isBaby()) {
            float maturationPercent = (float) kodiak.getMaturationPercentage() / 100f;
            float currentScale = babyScale + (scale - babyScale) * maturationPercent;
            poseStack.scale(currentScale, currentScale, currentScale);
        } else {
            poseStack.scale(scale, scale, scale);
        }

        super.render(kodiak, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        poseStack.popPose();

        if (!kodiak.isInResurrection()) {
            if (kodiak.isAlive() && !kodiak.isVehicle()) {
                if (kodiak.isTame()) {
                    if (player != null && kodiak.distanceTo(player) > 4.0D) {
                        OWRendererUtils.displayOwnerAboveEntity(kodiak, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayLevelAboveEntity(kodiak, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayImageAboveEntity(ICONS, 0, genderPosition, 12, 256, 0, 0, kodiak, poseStack, bufferSource, packedLight, true);
                        if (kodiak.isPassive())
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 60, 14, 256, 1.5f, 0f, kodiak, poseStack, bufferSource, packedLight, true);
                        if (kodiak.getLevel() >= 50) {
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 143, 10, 256, -1f, 1f, kodiak, poseStack, bufferSource, packedLight, true);
                            OWRendererUtils.displayPrestigeLevelAboveEntity(kodiak, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        }
                    }
                } else {
                    if (kodiak.isSleeping()) {
                        OWRendererUtils.displayBonusPointAboveEntity(kodiak, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, 0);
                    }
                }
            }
        }
        OWRendererUtils.createInformationImage(kodiak, poseStack, bufferSource, packedLight, 0, kodiak.isSitting() ? 1.5f : 0.75f, 0, 0, 3);
    }

    @Override
    protected void renderNameTag(KodiakEntity kodiak, Component component, PoseStack poseStack, MultiBufferSource bufferSource, int i, float v) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.65D, 0.0D);
        super.renderNameTag(kodiak, component, poseStack, bufferSource, i, v);
        poseStack.popPose();
    }
}
