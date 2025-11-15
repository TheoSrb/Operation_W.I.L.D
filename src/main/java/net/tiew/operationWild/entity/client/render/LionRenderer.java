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
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.client.layer.KodiakLayer;
import net.tiew.operationWild.entity.client.layer.LionLayer;
import net.tiew.operationWild.entity.client.layer.skins.KodiakSkins;
import net.tiew.operationWild.entity.client.model.KodiakModel;
import net.tiew.operationWild.entity.client.model.LionModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.variants.KodiakVariant;
import net.tiew.operationWild.entity.variants.LionVariant;

import java.util.Map;

public class LionRenderer extends MobRenderer<LionEntity, LionModel<LionEntity>> {
    private static final Map<LionVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(LionVariant.class), map -> {
        map.put(LionVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/lion/lion_default.png"));
        map.put(LionVariant.DARK, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/lion/lion_dark.png"));
        map.put(LionVariant.WHITE, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/lion/lion_white.png"));
        map.put(LionVariant.LIONESS_DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/lion/lioness_default.png"));
        map.put(LionVariant.LIONESS_DARK, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/lion/lioness_dark.png"));
        map.put(LionVariant.LIONESS_WHITE, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/lion/lioness_white.png"));
        map.put(LionVariant.SKIN_GOLD, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/lion/lion_skin_gold.png"));
        map.put(LionVariant.LIONESS_SKIN_GOLD, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/lion/lioness_skin_gold.png"));
    });
    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");

    private static final ResourceLocation HUNGRY_BAR = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/hud/hungry_bar.png");

    public LionRenderer(EntityRendererProvider.Context context) {
        super(context, new LionModel<>(context.bakeLayer(LionModel.LAYER_LOCATION)), 0.9f);
        this.addLayer(new LionLayer(this));
        //this.addLayer(new KodiakSkins(this));
    }

    @Override
    public ResourceLocation getTextureLocation(LionEntity lionEntity) {
        return LOCATION_BY_VARIANT.get(lionEntity.getVariant());
    }

    @Override
    public void render(LionEntity lion, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float scale = lion.getScale();
        float babyScale = scale / 2.25f;
        int genderPosition = lion.isFemale() ? 36 : lion.isMale() ? 48 : 0;
        Player player = lion.level().getNearestPlayer(lion, 64.0D);

        poseStack.pushPose();

        if (lion.isBaby()) {
            float maturationPercent = (float) lion.getMaturationPercentage() / 100f;
            float currentScale = babyScale + (scale - babyScale) * maturationPercent;
            poseStack.scale(currentScale, currentScale, currentScale);
        } else {
            poseStack.scale(scale, scale, scale);
        }

        super.render(lion, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        poseStack.popPose();

        if (!lion.isInResurrection()) {
            if (lion.isAlive() && !lion.isVehicle()) {
                if (lion.isTame()) {
                    if (lion.isBaby() && player != null) {
                        OWRendererUtils.displayTimeLeftBeforeBabyTaskAboveEntity(lion, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, lion.distanceTo(player) > 4 ? 0 : Minecraft.getInstance().options.hideGui ? 0 : 0.75f);
                        OWRendererUtils.displayImageAboveEntity(ICONS, 0, 154, 48, 256, -2.5f, lion.distanceTo(player) > 4 ? 2.6f : Minecraft.getInstance().options.hideGui ? 2.6f : 5.5f, lion, poseStack, bufferSource, packedLight, false);
                    }
                    if (player != null && lion.distanceTo(player) > 4.0D) {
                        OWRendererUtils.displayOwnerAboveEntity(lion, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayLevelAboveEntity(lion, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayImageAboveEntity(ICONS, 0, genderPosition, 12, 256, 0, 0, lion, poseStack, bufferSource, packedLight, true);
                        if (lion.isPassive())
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 60, 14, 256, 1.5f, 0f, lion, poseStack, bufferSource, packedLight, true);
                        if (lion.getLevel() >= 50) {
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 143, 10, 256, -1f, 1f, lion, poseStack, bufferSource, packedLight, true);
                            OWRendererUtils.displayPrestigeLevelAboveEntity(lion, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        }
                    }
                } else {
                    if (lion.isSleeping()) {
                        OWRendererUtils.displayBonusPointAboveEntity(lion, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, 0);
                    }
                }
            }
        }
        OWRendererUtils.createInformationImage(lion, poseStack, bufferSource, packedLight, 0, lion.isSitting() ? 2f : 0.75f, 0, 0, 3);
    }

    @Override
    protected void renderNameTag(LionEntity lion, Component component, PoseStack poseStack, MultiBufferSource bufferSource, int i, float v) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.65D, 0.0D);
        super.renderNameTag(lion, component, poseStack, bufferSource, i, v);
        poseStack.popPose();
    }
}
