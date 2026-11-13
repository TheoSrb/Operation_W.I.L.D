package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.bosses.plantempress.EmpressThornBush;
import net.tiew.operationWild.entity.client.model.PlantEmpressModel;

public class EmpressSproutRenderer extends EntityRenderer<EmpressThornBush> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/plant_empress/plant_empress.png");
    private static final float SCALE = 0.3f;

    private final ModelPart stalk;
    private final ModelPart head;
    private final ModelPart upper;
    private final ModelPart lower;

    public EmpressSproutRenderer(EntityRendererProvider.Context context) {
        super(context);
        ModelPart root = context.bakeLayer(PlantEmpressModel.LAYER_LOCATION);
        this.stalk = root.getChild("ALL").getChild("body_4").getChild("body_5").getChild("body_6");
        this.head = this.stalk.getChild("head2");
        this.upper = this.head.getChild("mouthUp2");
        this.lower = this.head.getChild("mouthDown2");
        this.shadowRadius = 0.4f;
    }

    @Override
    public void render(EmpressThornBush entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float grow = entity.visualWidth(partialTicks) / 1.2f;
        if (grow <= 0.01f) return;
        float time = entity.tickCount + partialTicks;
        Player player = Minecraft.getInstance().player;
        boolean near = player != null && player.distanceToSqr(entity) < 9.0;
        float speed = near ? 0.9f : 0.18f;
        float snap = Math.max(0f, Mth.sin(time * speed + entity.getId()));

        this.stalk.resetPose();
        this.head.resetPose();
        this.upper.resetPose();
        this.lower.resetPose();
        this.stalk.x = 0f;
        this.stalk.y = 0f;
        this.stalk.z = 0f;
        this.stalk.zRot = Mth.sin(time * 0.07f + entity.getId()) * 0.12f;
        this.stalk.xRot = 0.15f + Mth.cos(time * 0.05f + entity.getId()) * 0.08f + (near ? 0.25f : 0f);
        this.head.xRot += near ? 0.35f : 0.1f;
        this.upper.xRot -= snap * (near ? 0.75f : 0.35f);
        this.lower.xRot += snap * (near ? 0.65f : 0.3f);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180f - entity.visualSpin(partialTicks)));
        poseStack.scale(-SCALE * grow, -SCALE * grow, SCALE * grow);
        poseStack.translate(0f, -0.1f, 0f);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.stalk.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EmpressThornBush entity) {
        return TEXTURE;
    }
}
