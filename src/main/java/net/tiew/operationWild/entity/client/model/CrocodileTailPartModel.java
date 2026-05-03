package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileTailPart;

public class CrocodileTailPartModel extends EntityModel<CrocodileTailPart> {

    public static final ModelLayerLocation LAYER_TAIL1 = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "crocodile_tail_1"), "main");
    public static final ModelLayerLocation LAYER_TAIL2 = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "crocodile_tail_2"), "main");
    public static final ModelLayerLocation LAYER_TAIL3 = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "crocodile_tail_3"), "main");

    // Y pivot commun : ALL2.y(14) + body.y(-1.4547) + tail1.y(+2) = 14.5453
    private static final float TAIL_Y = 14.5453f;
    // tail3 est 2 unités plus bas (+ tail3.y(+2)) = 16.5453
    private static final float TAIL3_Y = 16.5453f;

    private final ModelPart segment;

    public CrocodileTailPartModel(ModelPart root) {
        this.segment = root.getChild("segment");
    }

    // ─── LayerDefinitions ─────────────────────────────────────────────────

    /** tail1 — 11×9×20 — centré en Z (décalage -10) */
    public static LayerDefinition createTail1Layer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("segment",
                CubeListBuilder.create()
                        .texOffs(0, 38)
                        .addBox(-5.5f, -4.0f, 0.0f, 11, 9, 20, new CubeDeformation(0f))
                        .texOffs(67, 84)
                        .addBox(-3.5f, -7.0f, 0.0f, 0, 3, 20, new CubeDeformation(0.01f))
                        .texOffs(67, 84).mirror()
                        .addBox(3.5f, -7.0f, 0.0f, 0, 3, 20, new CubeDeformation(0.01f)).mirror(false),
                PartPose.offset(0f, TAIL_Y, 0f));
        return LayerDefinition.create(mesh, 256, 256);
    }

    public static LayerDefinition createTail2Layer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("segment",
                CubeListBuilder.create()
                        .texOffs(62, 38)
                        .addBox(-3.5f, -2.0f, 0.0f, 7, 7, 20, new CubeDeformation(0f))
                        .texOffs(67, 84)
                        .addBox(-2.5f, -5.0f, 0.0f, 0, 3, 20, new CubeDeformation(0.01f))
                        .texOffs(67, 84).mirror()
                        .addBox(2.5f, -5.0f, 0.0f, 0, 3, 20, new CubeDeformation(0.01f)).mirror(false),
                PartPose.offset(0f, TAIL_Y, 0f));
        return LayerDefinition.create(mesh, 256, 256);
    }

    public static LayerDefinition createTail3Layer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("segment",
                CubeListBuilder.create()
                        .texOffs(153, 32)
                        .addBox(0f, -6.0f, 0.0f, 0, 9, 22, new CubeDeformation(0.01f)),
                PartPose.offset(0f, TAIL3_Y, 0f));
        return LayerDefinition.create(mesh, 256, 256);
    }

    // ─── Rendu ────────────────────────────────────────────────────────────

    @Override
    public void setupAnim(CrocodileTailPart entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.segment.resetPose();
        this.segment.visible = entity.getParent() != null && !entity.getParent().isInWater();
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer,
                               int packedLight, int packedOverlay, int color) {
        if (this.segment.visible) {
            this.segment.render(poseStack, consumer, packedLight, packedOverlay, color);
        }
    }

    public void applyTailRotation(float xRot, float yRot, float zRot) {
        this.segment.xRot = xRot;
        this.segment.yRot = yRot;
        this.segment.zRot = zRot;
    }
}