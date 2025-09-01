package net.tiew.operationWild.entity.client.render;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.layer.SeaBugLayer;
import net.tiew.operationWild.entity.client.model.SeaBugModel;
import net.tiew.operationWild.entity.misc.SeaBugEntity;
import net.tiew.operationWild.entity.variants.SeaBugVariant;

import java.util.Map;
public class SeaBugRenderer extends MobRenderer<SeaBugEntity, SeaBugModel<SeaBugEntity>> {

    private static final Map<SeaBugVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(SeaBugVariant.class), map -> {
        map.put(SeaBugVariant.DEFAULT, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/seabug/seabug.png"));
    });

    public SeaBugRenderer(EntityRendererProvider.Context context) {
        super(context, new SeaBugModel<>(context.bakeLayer(SeaBugModel.LAYER_LOCATION)), 0.4f);;
        this.addLayer(new SeaBugLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(SeaBugEntity seabug) {
        return LOCATION_BY_VARIANT.get(seabug.getVariant());
    }

    @Override
    public void render(SeaBugEntity seabug, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        renderSubmarine(seabug, partialTicks, poseStack, bufferSource, packedLight, true);
    }

    @Override
    protected void renderNameTag(SeaBugEntity seabug, Component component, PoseStack poseStack, MultiBufferSource bufferSource, int i, float v) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.65D, 0.0D);
        super.renderNameTag(seabug, component, poseStack, bufferSource, i, v);
        poseStack.popPose();
    }

    public void renderSubmarine(SeaBugEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource source, int lightIn, boolean maskWater) {
        Player player = (Player) entity.getControllingPassenger();
        float ageInTicks = entity.tickCount + partialTicks;
        float submarineYaw = entity.getViewYRot(partialTicks);
        float submarinePitch = entity.getViewXRot(partialTicks);

        boolean playerIsRiding = entity.getPassengers().contains(player);

        float pitch;
        if (playerIsRiding) {
            pitch = submarinePitch;
            entity.setLastPlayerPitch(pitch);
        } else {
            pitch = entity.getLastPlayerPitch();
        }

        poseStack.pushPose();

        poseStack.translate(0.0D, 1.7D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - submarineYaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        if (!entity.isOff()) poseStack.mulPose(Axis.XP.rotationDegrees(pitch));

        float $$1 = (5 - (4 * (entity.getHealth() / entity.getMaxHealth()))) * 1.5f;

        poseStack.mulPose(Axis.ZP.rotationDegrees((float) ((Math.sin(ageInTicks * 0.1F) * 0.5F) * $$1)));
        poseStack.mulPose(Axis.XP.rotationDegrees((float) ((Math.sin(ageInTicks * 0.1F + 1.3F) * 0.5F) * $$1)));


        float speed = (float) entity.getDeltaMovement().length() * 10f;
        float limbSwing = ageInTicks * 0.6662f;
        this.model.setupAnim(entity, limbSwing, speed, ageInTicks, submarineYaw, pitch);

        for (Entity passenger : entity.getPassengers()) {
            poseStack.pushPose();
            poseStack.translate(0, 0.65F, -0.75F);
            poseStack.mulPose(Axis.XN.rotationDegrees(180F));
            poseStack.mulPose(Axis.YN.rotationDegrees(360 - submarineYaw));

            if (passenger instanceof Player) {
                Player firstPlayer = (Player) passenger;
                float headYaw = (firstPlayer.yHeadRotO + (firstPlayer.getYHeadRot() - firstPlayer.yHeadRotO) * partialTicks);
                float xRot = firstPlayer.getViewXRot(partialTicks) + pitch;
                float yRot = Mth.approachDegrees(submarineYaw, headYaw, 60);

                poseStack.pushPose();

                poseStack.translate(0, 0.75F, -2.4F);
                poseStack.mulPose(Axis.YN.rotationDegrees(submarineYaw));
                poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
                poseStack.mulPose(Axis.XN.rotationDegrees(90 - xRot));
                poseStack.scale(3, 1, 1);
                poseStack.translate(0, -1, 0F);

                poseStack.popPose();
            }

            poseStack.popPose();
        }

        VertexConsumer buffer = source.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, buffer, lightIn, OverlayTexture.NO_OVERLAY);

        for (var layer : this.layers) {
            layer.render(poseStack, source, lightIn, entity, limbSwing, speed, partialTicks, ageInTicks, submarineYaw, pitch);
        }

        if (shouldRenderLights(entity)) {
            renderLightCones(entity, partialTicks, poseStack, source, submarineYaw);
        }

        poseStack.popPose();
    }

    private boolean shouldRenderLights(SeaBugEntity entity) {
        return !entity.isOff() && entity.isLightOn();
    }

    private void renderLightCones(SeaBugEntity entity, float partialTicks, PoseStack poseStack,
                                  MultiBufferSource source, float submarineYaw) {

        Player player = (Player) entity.getControllingPassenger();

        if (player != null && entity.hasPassenger(player) && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            return;
        }

        float xRot = entity.getViewXRot(partialTicks);
        float yRot = entity.getViewYRot(partialTicks);

        float coneLength = 6.0F;
        float coneWidth = 1.2F;

        renderSingleLightCone(poseStack, source, submarineYaw, yRot, xRot,
                0.3F, 0.0F, coneLength, coneWidth, true);
        renderSingleLightCone(poseStack, source, submarineYaw, yRot, xRot,
                -0.3F, 0.0F, coneLength, coneWidth, false);
    }

    private void renderSingleLightCone(PoseStack poseStack, MultiBufferSource source,
                                       float submarineYaw, float yRot, float xRot,
                                       float offsetX, float offsetY, float length, float width, boolean isLeft) {
        poseStack.pushPose();

        poseStack.translate(offsetX, 0.75F + offsetY + 0.35, -1.5F);

        poseStack.mulPose(Axis.YN.rotationDegrees(submarineYaw - 90 + (isLeft ? 10 : -10)));
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XN.rotationDegrees(xRot));

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();

        VertexConsumer lightConsumer = source.getBuffer(RenderType.lightning());

        createLightCone(lightConsumer, matrix4f, length, width);

        poseStack.popPose();
    }

    private void createLightCone(VertexConsumer consumer, Matrix4f matrix4f,
                                 float length, float width) {

        int segments = 3;

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * ((i + 1) % segments) / segments);

            float x1 = (float) (Math.cos(angle1) * width);
            float z1 = (float) (Math.sin(angle1) * width);
            float x2 = (float) (Math.cos(angle2) * width);
            float z2 = (float) (Math.sin(angle2) * width);

            addLightVertex(consumer, matrix4f, 0, 0, 0, 0.3f);
            addLightVertex(consumer, matrix4f, length, x2, z2, 0.05f);
            addLightVertex(consumer, matrix4f, length, x1, z1, 0.05f);
        }

        float[] xPoints = new float[segments];
        float[] zPoints = new float[segments];
        for (int i = 0; i < segments; i++) {
            float angle = (float) (2 * Math.PI * i / segments);
            xPoints[i] = (float) (Math.cos(angle) * width);
            zPoints[i] = (float) (Math.sin(angle) * width);
        }

        addLightVertex(consumer, matrix4f, length, xPoints[0], zPoints[0], 0.05f);
        addLightVertex(consumer, matrix4f, length, xPoints[2], zPoints[2], 0.05f);
        addLightVertex(consumer, matrix4f, length, xPoints[1], zPoints[1], 0.05f);

        addLightVertex(consumer, matrix4f, length, xPoints[0], zPoints[0], 0.05f);
        addLightVertex(consumer, matrix4f, length, xPoints[1], zPoints[1], 0.05f);
        addLightVertex(consumer, matrix4f, length, xPoints[2], zPoints[2], 0.05f);
    }




    private void addLightVertex(VertexConsumer consumer, Matrix4f matrix4f,
                                float x, float y, float z, float alpha) {
        consumer.addVertex(matrix4f, x, y, z)
                .setColor(1.0f, 1.0f, 0.8f, alpha)
                .setUv(0, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0, 1, 0);
    }
}
