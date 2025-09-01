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
import net.tiew.operationWild.entity.client.layer.MantaLayer;
import net.tiew.operationWild.entity.client.model.MantaModel;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;
import net.tiew.operationWild.entity.animals.aquatic.MantaEntity;
import net.tiew.operationWild.entity.variants.MantaVariant;

import java.util.Map;

public class MantaRenderer extends MobRenderer<MantaEntity, MantaModel<MantaEntity>> {
    private static final Map<MantaVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(MantaVariant.class), map -> {
        map.put(MantaVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/manta/manta_default.png"));
    });
    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");

    public MantaRenderer(EntityRendererProvider.Context context) {
        super(context, new MantaModel<>(context.bakeLayer(MantaModel.LAYER_LOCATION)), 0.4f);
        this.addLayer(new MantaLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(MantaEntity manta) {
        return LOCATION_BY_VARIANT.get(manta.getVariant());
    }

    @Override
    public void render(MantaEntity manta, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float scale = manta.getScale() / 1.4f;
        float babyScale = scale / 2.25f;
        int genderPosition = manta.isFemale() ? 36 : manta.isMale() ? 48 : 0;
        Player player = manta.level().getNearestPlayer(manta, 64.0D);

        poseStack.pushPose();

        if (manta.isBaby()) {
            float maturationPercent = (float) manta.getMaturationPercentage() / 100f;
            float currentScale = babyScale + (scale - babyScale) * maturationPercent;
            poseStack.scale(currentScale, currentScale, currentScale);
        } else {
            poseStack.scale(scale, scale, scale);
        }

        super.render(manta, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        poseStack.popPose();

        if (!manta.isInResurrection()) {
            if (manta.isAlive() && !manta.isVehicle()) {
                if (manta.isTame()) {
                    if (player != null && manta.distanceTo(player) > 4.0D) {
                        OWRendererUtils.displayOwnerAboveEntity(manta, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayLevelAboveEntity(manta, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        OWRendererUtils.displayImageAboveEntity(ICONS, 0, genderPosition, 12, 256, 0, 0, manta, poseStack, bufferSource, packedLight, true);
                        if (manta.isPassive())
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 60, 14, 256, 1.5f, 0f, manta, poseStack, bufferSource, packedLight, true);
                        if (manta.getLevel() >= 50) {
                            OWRendererUtils.displayImageAboveEntity(ICONS, 0, 143, 10, 256, -1f, 1f, manta, poseStack, bufferSource, packedLight, true);
                            OWRendererUtils.displayPrestigeLevelAboveEntity(manta, poseStack, bufferSource, packedLight, this.entityRenderDispatcher);
                        }
                    }
                } else {
                    if (manta.isSleeping()) {
                        OWRendererUtils.displayBonusPointAboveEntity(manta, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, 0);
                    }
                }
            }
        }
        OWRendererUtils.createInformationImage(manta, poseStack, bufferSource, packedLight, 0, 0, 0, 0, 2);
    }

    @Override
    protected void renderNameTag(MantaEntity manta, Component component, PoseStack poseStack, MultiBufferSource bufferSource, int i, float v) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.65D, 0.0D);
        super.renderNameTag(manta, component, poseStack, bufferSource, i, v);
        poseStack.popPose();
    }
}
