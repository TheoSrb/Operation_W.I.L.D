package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileTailPart;
import net.tiew.operationWild.entity.client.model.CrocodileTailPartModel;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;
import net.tiew.operationWild.entity.variants.CrocodileVariant;

import java.util.HashMap;
import java.util.Map;

public class CrocodileTailPartRenderer extends EntityRenderer<CrocodileTailPart> {

    private static final float[] YAW_SPEEDS = { 0.025f, 0.01f, 0.0075f };
    private static final float SEG_DIST     = 1.25f;
    private static final float BODY_OFF     = 0.75f;

    private final CrocodileTailPartModel[] models = new CrocodileTailPartModel[3];

    private static class SmoothChain {
        final double[][] pos = new double[3][3];
        final float[]    yaw = new float[3];
        boolean init = false;
    }

    private final Map<Integer, SmoothChain> chains = new HashMap<>();

    public CrocodileTailPartRenderer(EntityRendererProvider.Context context) {
        super(context);
        models[0] = new CrocodileTailPartModel(context.bakeLayer(CrocodileTailPartModel.LAYER_TAIL1));
        models[1] = new CrocodileTailPartModel(context.bakeLayer(CrocodileTailPartModel.LAYER_TAIL2));
        models[2] = new CrocodileTailPartModel(context.bakeLayer(CrocodileTailPartModel.LAYER_TAIL3));
    }

    @Override
    public ResourceLocation getTextureLocation(CrocodileTailPart entity) {
        CrocodileEntity parent = entity.getParent();
        CrocodileVariant variant = (parent != null) ? parent.getVariant() : CrocodileVariant.DEFAULT;
        return SkinRegistry.CrocodileSkins.get(variant).getTexture();
    }

    @Override
    public boolean shouldRender(CrocodileTailPart entity,
                                net.minecraft.client.renderer.culling.Frustum frustum,
                                double x, double y, double z) {
        CrocodileEntity parent = entity.getParent();
        if (parent == null) return false;
        return frustum.isVisible(parent.getBoundingBox().inflate(5.0));
    }

    @Override
    public void render(CrocodileTailPart entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        CrocodileEntity parent = entity.getParent();
        if (parent == null || parent.isRemoved() || parent.isBaby()) return;

        int seg = entity.getSegmentIndex();

        double entityLerpX = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double entityLerpY = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double entityLerpZ = Mth.lerp(partialTick, entity.zOld, entity.getZ());

        SmoothChain chain = chains.computeIfAbsent(parent.getId(), k -> new SmoothChain());
        if (seg == 0) {
            updateSmoothChain(chain, parent, partialTick);
        }

        float  smoothYaw = chain.yaw[seg];
        double smoothX   = chain.pos[seg][0];
        double smoothY   = chain.pos[seg][1];
        double smoothZ   = chain.pos[seg][2];

        poseStack.pushPose();
        poseStack.translate(smoothX - entityLerpX, smoothY - entityLerpY, smoothZ - entityLerpZ);

        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - smoothYaw));

        if (parent.deathTime > 0) {
            float deathRot = Math.min(parent.deathTime / 20.0f * 1.6f, 1.0f) * 90.0f;
            poseStack.mulPose(Axis.ZP.rotationDegrees(deathRot));
        }

        poseStack.scale(-1f, -1f, 1f);

        float scale = parent.getScale();
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0f, -1.501f, 0f);

        int overlay = OverlayTexture.pack(0, parent.hurtTime > 0 || parent.deathTime > 0);
        var vc = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));

        models[seg].setupAnim(entity, 0f, 0f, entity.tickCount + partialTick, 0f, 0f);
        switch (seg) {
            case 0 -> models[0].applyTailRotation(parent.tail1AnimXRot, parent.tail1AnimYRot, parent.tail1AnimZRot);
            case 1 -> models[1].applyTailRotation(parent.tail2AnimXRot, parent.tail2AnimYRot, parent.tail2AnimZRot);
            case 2 -> models[2].applyTailRotation(parent.tail3AnimXRot, parent.tail3AnimYRot, parent.tail3AnimZRot);
        }
        models[seg].renderToBuffer(poseStack, vc, packedLight, overlay, -1);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void updateSmoothChain(SmoothChain chain, CrocodileEntity parent, float partialTick) {
        float scale   = parent.getScale();
        float segLen  = SEG_DIST * scale;
        float bodyOff = BODY_OFF * scale;

        double px  = Mth.lerp(partialTick, parent.xOld, parent.getX());
        double py  = Mth.lerp(partialTick, parent.yOld, parent.getY());
        double pz  = Mth.lerp(partialTick, parent.zOld, parent.getZ());
        float pYaw = Mth.rotLerp(partialTick, parent.yBodyRotO, parent.yBodyRot);

        double bodyAnimYOffset = -parent.bodyAnimY / 16.0f * scale;
        py += bodyAnimYOffset;

        if (!chain.init) {
            float yr = (float) Math.toRadians(pYaw);
            for (int i = 0; i < 3; i++) {
                chain.pos[i][0] = px + Math.sin(yr) * segLen * (i + 1);
                chain.pos[i][1] = py;
                chain.pos[i][2] = pz - Math.cos(yr) * segLen * (i + 1);
                chain.yaw[i]    = pYaw;
            }
            chain.init = true;
            return;
        }

        float runMultiplier = parent.isRunning() ? 3.0f : 1.0f;

        chain.yaw[0] += Mth.wrapDegrees(pYaw         - chain.yaw[0]) * (YAW_SPEEDS[0] * runMultiplier);
        chain.yaw[1] += Mth.wrapDegrees(chain.yaw[0] - chain.yaw[1]) * (YAW_SPEEDS[1] * runMultiplier);
        chain.yaw[2] += Mth.wrapDegrees(chain.yaw[1] - chain.yaw[2]) * (YAW_SPEEDS[2] * runMultiplier);

        float yr0 = (float) Math.toRadians(pYaw);
        chain.pos[0][0] = px + Math.sin(yr0) * bodyOff;
        chain.pos[0][1] = (parent.tailParts[0] != null && !parent.tailParts[0].isRemoved())
                ? Mth.lerp(partialTick, parent.tailParts[0].yOld, parent.tailParts[0].getY()) : py;
        chain.pos[0][2] = pz - Math.cos(yr0) * bodyOff;

        float r0 = (float) Math.toRadians(chain.yaw[0]);
        chain.pos[1][0] = chain.pos[0][0] + Math.sin(r0) * segLen;
        chain.pos[1][1] = (parent.tailParts[1] != null && !parent.tailParts[1].isRemoved())
                ? Mth.lerp(partialTick, parent.tailParts[1].yOld, parent.tailParts[1].getY()) : chain.pos[0][1];
        chain.pos[1][2] = chain.pos[0][2] - Math.cos(r0) * segLen;

        float r1 = (float) Math.toRadians(chain.yaw[1]);
        chain.pos[2][0] = chain.pos[1][0] + Math.sin(r1) * segLen;
        chain.pos[2][1] = (parent.tailParts[2] != null && !parent.tailParts[2].isRemoved())
                ? Mth.lerp(partialTick, parent.tailParts[2].yOld, parent.tailParts[2].getY()) : chain.pos[1][1];
        chain.pos[2][2] = chain.pos[1][2] - Math.cos(r1) * segLen;
    }
}