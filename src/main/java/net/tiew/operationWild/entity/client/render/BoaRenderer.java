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
import net.tiew.operationWild.entity.client.layer.BoaLayer;
import net.tiew.operationWild.entity.client.layer.skins.BoaSkins;
import net.tiew.operationWild.entity.client.model.BoaModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.variants.BoaVariant;

import java.util.Map;

public class BoaRenderer extends MobRenderer<BoaEntity, BoaModel<BoaEntity>> {
    private static final Map<BoaVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(BoaVariant.class), map -> {
        map.put(BoaVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_default.png"));
        map.put(BoaVariant.YELLOW, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_yellow.png"));
        map.put(BoaVariant.BROWN, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_brown.png"));
        map.put(BoaVariant.DARK_GREEN, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_dark.png"));
        map.put(BoaVariant.LIME, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_lime.png"));
        map.put(BoaVariant.ALBINO, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_albino.png"));
        map.put(BoaVariant.CORAL, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_coral.png"));

        map.put(BoaVariant.SKIN_GOLD, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/skins/boa_skin_gold.png"));
        map.put(BoaVariant.SKIN_LEVIATHAN, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/skins/boa_skin_leviathan.png"));
        map.put(BoaVariant.SKIN_PLUSH, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/skins/boa_skin_plush.png"));

    });
    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");

    public BoaRenderer(EntityRendererProvider.Context context) {
        super(context, new BoaModel<>(context.bakeLayer(BoaModel.LAYER_LOCATION)), 1.0f);
        this.addLayer(new BoaSkins(this));
        this.addLayer(new BoaLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(BoaEntity tigerEntity) {
        return LOCATION_BY_VARIANT.get(tigerEntity.getVariant());
    }

    @Override
    public void render(BoaEntity boa, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float scale = boa.getScale();
        float babyScale = scale / 2.0f;
        int genderPosition = boa.isFemale() ? 36 : boa.isMale() ? 48 : 0;
        Player player = boa.level().getNearestPlayer(boa, 64.0D);

        if (boa.isBaby()) {
            float maturationPercent = (float) boa.getMaturationPercentage() / 100f;
            float currentScale = babyScale + (scale - babyScale) * maturationPercent;
            poseStack.scale(currentScale, currentScale, currentScale);
        } else poseStack.scale(scale, scale, scale);

        super.render(boa, entityYaw, partialTicks, poseStack, bufferSource, packedLight);

        if (!boa.isInResurrection()) {
            if (boa.isAlive() && !boa.isVehicle()) {
                if (boa.isTame()) {
                    if (boa.isBaby() && player != null) {
                        OWRendererUtils.displayTimeLeftBeforeBabyTaskAboveEntity(boa, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, boa.distanceTo(player) > 4 ? 0 : Minecraft.getInstance().options.hideGui ? 0 : 1.25f);
                        OWRendererUtils.displayImageAboveEntity(ICONS, 0, 154, 48, 256, -2.5f, boa.distanceTo(player) > 4 ? 2.6f : Minecraft.getInstance().options.hideGui ? 2.6f : 7.75f, boa, poseStack, bufferSource, packedLight, false);
                    }
                    if (player != null && boa.distanceTo(player) > 4.0D) {
                        OWRendererUtils.displayOwnerAboveEntity(boa, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayLevelAboveEntity(boa, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayImageAboveEntity(ICONS, 0, genderPosition, 12, 256, 0, 0, boa, poseStack, bufferSource, packedLight, true);
                        if (boa.isPassive())
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 60, 14, 256, 1.5f, 0f, boa, poseStack, bufferSource, packedLight, true);
                        if (boa.getLevel() >= 50) {
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 143, 10, 256, -1f, 1f, boa, poseStack, bufferSource, packedLight, true);
                            OWRendererUtils.displayPrestigeLevelAboveEntity(boa, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        }
                    }
                } else {
                    if (boa.foodGiven > 0) {
                        OWRendererUtils.displayBonusPointAboveEntity(boa, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, 0.5);
                    }
                }
            }
        }
        OWRendererUtils.createInformationImage(boa, poseStack, bufferSource, packedLight, 0, 0.5, 0, -30f, 3);


    }

    @Override
    protected void renderNameTag(BoaEntity boa, Component component, PoseStack poseStack, MultiBufferSource bufferSource, int i, float v) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.65D, 0.0D);
        super.renderNameTag(boa, component, poseStack, bufferSource, i, v);
        poseStack.popPose();
    }
}