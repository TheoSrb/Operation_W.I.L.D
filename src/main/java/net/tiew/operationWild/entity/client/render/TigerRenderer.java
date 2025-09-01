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
import net.tiew.operationWild.entity.client.layer.TigerLayer;
import net.tiew.operationWild.entity.client.layer.skins.TigerSkins;
import net.tiew.operationWild.entity.client.model.TigerModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.entity.variants.TigerVariant;

import java.util.Map;

public class TigerRenderer extends MobRenderer<TigerEntity, TigerModel<TigerEntity>> {
    private static final Map<TigerVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(TigerVariant.class), map -> {
        map.put(TigerVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_default.png"));
        map.put(TigerVariant.LIGHT_ORANGE, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_light_orange.png"));
        map.put(TigerVariant.GOLDEN, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_golden.png"));
        map.put(TigerVariant.WHITE, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_white.png"));

        map.put(TigerVariant.SKIN_GOLD, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_gold.png"));
        map.put(TigerVariant.SKIN_MAGMA, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_magma.png"));
        map.put(TigerVariant.SKIN_VIRUS, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_virus.png"));
        map.put(TigerVariant.SKIN_DAMNED, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/skins/tiger_skin_damned.png"));
    });
    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");

    public TigerRenderer(EntityRendererProvider.Context context) {
        super(context, new TigerModel<>(context.bakeLayer(TigerModel.LAYER_LOCATION)), 1.0f);
        this.addLayer(new TigerSkins(this));
        this.addLayer(new TigerLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(TigerEntity tigerEntity) {
        return LOCATION_BY_VARIANT.get(tigerEntity.getVariant());
    }

    @Override
    public void render(TigerEntity tiger, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float scale = tiger.getScale();
        float babyScale = scale / 2.0f;
        int genderPosition = tiger.isFemale() ? 36 : tiger.isMale() ? 48 : 0;
        Player player = tiger.level().getNearestPlayer(tiger, 64.0D);

        if (tiger.isBaby()) {
            float maturationPercent = (float) tiger.getMaturationPercentage() / 100f;
            float currentScale = babyScale + (scale - babyScale) * maturationPercent;
            poseStack.scale(currentScale, currentScale, currentScale);
        } else poseStack.scale(scale, scale, scale);

        if (tiger.isUltimate()) poseStack.scale(scale * 1.25f, scale * 1.25f, scale * 1.25f);

        super.render(tiger, entityYaw, partialTicks, poseStack, bufferSource, packedLight);

        if (!tiger.isInResurrection()) {
            if (tiger.isAlive() && !tiger.isVehicle()) {
                if (tiger.isTame()) {
                    if (tiger.isBaby() && player != null) {
                        OWRendererUtils.displayTimeLeftBeforeBabyTaskAboveEntity(tiger, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, tiger.distanceTo(player) > 4 ? 0 : Minecraft.getInstance().options.hideGui ? 0 : 0.75f);
                        OWRendererUtils.displayImageAboveEntity(ICONS, 0, 154, 48, 256, -2.5f, tiger.distanceTo(player) > 4 ? 2.6f : Minecraft.getInstance().options.hideGui ? 2.6f : 5.5f, tiger, poseStack, bufferSource, packedLight, false);
                    }
                    if (player != null && tiger.distanceTo(player) > 4.0D) {
                        OWRendererUtils.displayOwnerAboveEntity(tiger, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayLevelAboveEntity(tiger, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayImageAboveEntity(ICONS, 0, genderPosition, 12, 256, 0, 0, tiger, poseStack, bufferSource, packedLight, true);
                        if (tiger.isPassive())
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 60, 14, 256, 1.5f, 0f, tiger, poseStack, bufferSource, packedLight, true);
                        if (tiger.getLevel() >= 50) {
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 143, 10, 256, -1f, 1f, tiger, poseStack, bufferSource, packedLight, true);
                            OWRendererUtils.displayPrestigeLevelAboveEntity(tiger, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        }
                    }
                } else {
                    if (tiger.isSleeping()) {
                        OWRendererUtils.displayBonusPointAboveEntity(tiger, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, 0);
                    }
                }
            }
        }
        OWRendererUtils.createInformationImage(tiger, poseStack, bufferSource, packedLight, 0, 0.5, 0, 0, 3);

    }

    @Override
    protected void renderNameTag(TigerEntity tiger, Component component, PoseStack poseStack, MultiBufferSource bufferSource, int i, float v) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.65D, 0.0D);
        super.renderNameTag(tiger, component, poseStack, bufferSource, i, v);
        poseStack.popPose();
    }
}
