package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.client.render.misc.OWRendererUtils;

public abstract class OWEntityRenderer<T extends OWEntity, M extends EntityModel<T>> extends MobRenderer<T, M> {

    protected static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");
    public abstract double distanceToShowRealInfos();
    public abstract double infosUpOffset();

    public OWEntityRenderer(EntityRendererProvider.Context context, M model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float scale = entity.getScale();
        float babyScale = scale / 2.25f;
        Player player = entity.level().getNearestPlayer(entity, 64.0D);

        poseStack.pushPose();

        if (entity.isBaby()) {
            float maturationPercent = (float) entity.getMaturationPercentage() / 100f;
            float currentScale = babyScale + (scale - babyScale) * maturationPercent;
            poseStack.scale(currentScale, currentScale, currentScale);
        } else {
            poseStack.scale(scale, scale, scale);
        }

        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        poseStack.popPose();

        renderEntityInfo(entity, poseStack, bufferSource, packedLight, player);
    }

    protected void renderEntityInfo(T entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Player player) {
        if (!entity.isInResurrection()) {
            if (entity.isAlive() && !entity.isVehicle()) {
                if (entity.isTame()) {
                    if (entity.isBaby() && player != null) {
                        OWRendererUtils.displayTimeLeftBeforeBabyTaskAboveEntity(entity, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, entity.distanceTo(player) > 4 ? 0 : Minecraft.getInstance().options.hideGui ? 0 : 0.75f);
                        OWRendererUtils.displayImageAboveEntity(ICONS, getIconX(entity), getIconY(entity), 48, 256, -2.5f, entity.distanceTo(player) > 4 ? 2.6f : Minecraft.getInstance().options.hideGui ? 2.6f : 5.5f, entity, poseStack, bufferSource, packedLight, false);
                    }
                    if (player != null && entity.distanceTo(player) > distanceToShowRealInfos()) {
                        OWRendererUtils.displayOwnerAboveEntity(entity, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, infosUpOffset());
                        OWRendererUtils.displayLevelAboveEntity(entity, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, infosUpOffset());
                    }
                } else {
                    if (entity.isSleeping()) {
                        OWRendererUtils.displayBonusPointAboveEntity(entity, poseStack, bufferSource, packedLight, this.entityRenderDispatcher, 0);
                    }
                }
            }
        }
        OWRendererUtils.createInformationImage(entity, poseStack, bufferSource, packedLight, 0, 1.0f + infosUpOffset(), 0, 0, (int) (distanceToShowRealInfos() - 1));
    }

    protected int getIconX(T entity) {
        return 0;
    }

    protected int getIconY(T entity) {
        return 154;
    }

    @Override
    protected void renderNameTag(T entity, Component component, PoseStack poseStack, MultiBufferSource bufferSource, int i, float v) {

    }
}