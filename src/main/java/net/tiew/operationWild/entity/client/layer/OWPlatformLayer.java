package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.tiew.operationWild.client.platform.OWPlatformClient;
import net.tiew.operationWild.client.platform.OWPlatformRenderer;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.client.model.IOWPlatformModel;
import net.tiew.operationWild.entity.platform.OWPlatform;
import org.joml.Vector3f;

public class OWPlatformLayer<T extends OWEntity, M extends EntityModel<T> & IOWPlatformModel> extends RenderLayer<T, M> {

    public OWPlatformLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        OWPlatform platform = entity.getPlatform();
        if (platform == null) return;
        poseStack.pushPose();
        this.getParentModel().translateToPlatformDeck(poseStack);
        float scale = poseStack.last().pose().getScale(new Vector3f()).x;
        if (scale > 1.0E-4f) poseStack.scale(1f / scale, 1f / scale, 1f / scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(180f));
        poseStack.translate(-platform.profile().centerX(), 0, -platform.profile().centerZ());
        OWPlatformClient.recordVisualPose(platform, poseStack.last().pose());
        OWPlatformRenderer.renderGrid(platform, partialTick, poseStack, bufferSource);
        poseStack.popPose();
    }
}
