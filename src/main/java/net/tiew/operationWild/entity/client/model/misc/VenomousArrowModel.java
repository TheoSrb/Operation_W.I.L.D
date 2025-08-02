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
import net.tiew.operationWild.entity.custom.misc.VenomousArrow;

public class VenomousArrowModel extends EntityModel<VenomousArrow> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "venomous_arrow"), "main");
    private final ModelPart back;
    private final ModelPart cross_1;
    private final ModelPart cross_2;

    public VenomousArrowModel(ModelPart root) {
        this.back = root.getChild("back");
        this.cross_1 = root.getChild("cross_1");
        this.cross_2 = root.getChild("cross_2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition back = partdefinition.addOrReplaceChild("back", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -2.5F, -2.5F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 20.5F, 7.0F, -2.3562F, 1.5708F, 0.0F));

        PartDefinition cross_1 = partdefinition.addOrReplaceChild("cross_1", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -2.5F, 0.0F, 16.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 20.5F, -4.0F, -2.3562F, 1.5708F, 0.0F));

        PartDefinition cross_2 = partdefinition.addOrReplaceChild("cross_2", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -2.5F, 0.0F, 16.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 20.5F, -4.0F, -0.7854F, 1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(VenomousArrow entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        back.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        cross_1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        cross_2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
