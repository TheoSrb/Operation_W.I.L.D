package net.tiew.operationWild.entity.client.model.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.custom.misc.PrimitiveSpearProjectileEntity;

public class PrimitiveSpearProjectileModel extends EntityModel<PrimitiveSpearProjectileEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "primitive_spear"), "main");
    private final ModelPart spear;

    public PrimitiveSpearProjectileModel(ModelPart root) {
        this.spear = root.getChild("spear");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition spear = partdefinition.addOrReplaceChild("spear", CubeListBuilder.create().texOffs(17, 3).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 22.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition cube_r1 = spear.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(26, 26).addBox(-5.0F, -2.0F, -1.0F, 3.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2F, -3.0F, 1.6F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r2 = spear.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(23, 26).addBox(2.0F, -2.0F, -1.0F, 3.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, -3.0F, 1.6F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r3 = spear.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(23, 26).addBox(2.0F, -2.0F, 1.0F, 3.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, -3.0F, -1.6F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r4 = spear.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(26, 26).addBox(-5.0F, -2.0F, 1.0F, 3.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2F, -3.0F, -1.6F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r5 = spear.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -7.0F, 0.0F, 5.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.3F, -6.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition cube_r6 = spear.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, -5).mirror().addBox(-7.1F, -32.0F, 3.9F, 0.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(7.1F, 9.0F, -24.6F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r7 = spear.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(24, 0).addBox(-8.0F, -25.0F, 7.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.0F, 21.0F, -10.6F, 0.0F, 0.7854F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(PrimitiveSpearProjectileEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        spear.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
