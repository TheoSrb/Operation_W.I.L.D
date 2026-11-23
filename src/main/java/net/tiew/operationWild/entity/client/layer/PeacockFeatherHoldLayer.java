package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.PeacockEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.client.model.PeacockModel;
import net.tiew.operationWild.entity.client.render.PeacockFeatherRenderer;
import net.tiew.operationWild.entity.client.render.PeacockRenderer;

public class PeacockFeatherHoldLayer extends RenderLayer<PeacockEntity, PeacockModel<PeacockEntity>> {

    private static final int MAX_PER_WING = 3;
    private static final float FAN_DEGREES = 18f;
    private static final float BASE_DEGREES = 25f;
    private static final float LENGTH = 0.5f;
    private static final float SHOW_THRESHOLD = 0.2f;

    public PeacockFeatherHoldLayer(PeacockRenderer renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, PeacockEntity peacock,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        float aim = peacock.aimBlend(partialTick);
        if (aim < SHOW_THRESHOLD || peacock.isInvisible()) return;

        int stock = peacock.getFeatherStock();
        boolean nextRight = PeacockEntity.isRightWingShot(peacock.getFeatherShot() + 1);
        int nextCount = Math.min(MAX_PER_WING, (stock + 1) / 2);
        int otherCount = Math.min(MAX_PER_WING, stock / 2);

        int elapsed = OWAttacksConstants.Peacock.FEATHER_SHOT_ANIMATION_TICKS - peacock.clientShotTicksLeft();
        boolean justFired = peacock.clientShotTicksLeft() > 0 && elapsed < OWAttacksConstants.Peacock.FEATHER_RELEASE_TICKS;
        if (justFired) {
            otherCount = Math.min(MAX_PER_WING, otherCount + 1);
        }

        float grow = Math.min(1f, (aim - SHOW_THRESHOLD) / (1f - SHOW_THRESHOLD));
        renderWing(poseStack, buffers, packedLight, peacock, false, nextRight ? otherCount : nextCount, grow, ageInTicks);
        renderWing(poseStack, buffers, packedLight, peacock, true, nextRight ? nextCount : otherCount, grow, ageInTicks);
    }

    private void renderWing(PoseStack poseStack, MultiBufferSource buffers, int light, PeacockEntity peacock,
                            boolean right, int count, float grow, float ageInTicks) {
        if (count <= 0) return;
        poseStack.pushPose();
        this.getParentModel().translateToWingTip(poseStack, !right);
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(
                PeacockFeatherRenderer.texture(peacock.getVariant().getId())));
        float length = LENGTH * grow;
        float width = length * 0.5f;
        for (int i = 0; i < count; i++) {
            float fan = (i - (count - 1) / 2f) * FAN_DEGREES;
            float quiver = (float) Math.sin((ageInTicks + i * 7f) * 0.45f) * 3f;
            float angle = (BASE_DEGREES + fan + quiver) * Mth.DEG_TO_RAD;
            Vec3 dir = new Vec3(0, Mth.sin(angle), Mth.cos(angle));
            Vec3 across = new Vec3(0, Mth.cos(angle), -Mth.sin(angle)).scale(width * 0.5f);
            Vec3 lift = new Vec3((right ? -1 : 1) * i * 0.012f, 0, 0);
            Vec3 tail = lift;
            Vec3 tip = lift.add(dir.scale(length));
            PeacockFeatherRenderer.quad(poseStack.last(), consumer, light, tip, tail, across, new Vec3(right ? -1 : 1, 0, 0));
        }
        poseStack.popPose();
    }
}
