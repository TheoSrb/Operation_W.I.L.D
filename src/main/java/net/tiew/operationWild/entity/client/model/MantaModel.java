package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.aquatic.MantaEntity;

public class MantaModel<T extends MantaEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "manta_default"), "main");

	private final ModelPart body;
	private final ModelPart left_wing_1;
	private final ModelPart left_wing_2;
	private final ModelPart right_wing_1;
	private final ModelPart right_wing_2;
	private final ModelPart body2;
	private final ModelPart tail;

    public MantaModel(ModelPart root) {

		this.body = root.getChild("body");
		this.left_wing_1 = this.body.getChild("left_wing_1");
		this.left_wing_2 = this.left_wing_1.getChild("left_wing_2");
		this.right_wing_1 = this.body.getChild("right_wing_1");
		this.right_wing_2 = this.right_wing_1.getChild("right_wing_2");
		this.body2 = this.body.getChild("body2");
		this.tail = this.body2.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {

		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-13.0F, -3.0F, -15.0F, 26.0F, 5.0F, 32.0F, new CubeDeformation(0.0F))
		.texOffs(0, 63).addBox(-13.0F, 2.0F, -11.0F, 26.0F, 2.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 22.0F, 5.0F));
		PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(71, 134).addBox(13.0F, -6.0F, -27.0F, 0.0F, 10.0F, 17.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(5.0F, 2.0F, 0.0F, 0.0F, 0.4363F, 0.0F));
		PartDefinition cube_r2 = body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(71, 134).mirror().addBox(-13.0F, -6.0F, -27.0F, 0.0F, 10.0F, 17.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(-5.0F, 2.0F, 0.0F, 0.0F, -0.4363F, 0.0F));
		PartDefinition left_wing_1 = body.addOrReplaceChild("left_wing_1", CubeListBuilder.create().texOffs(66, 86).addBox(0.0F, -1.0F, -11.0F, 15.0F, 2.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offset(13.0F, -1.0F, 0.0F));
		PartDefinition left_wing_2 = left_wing_1.addOrReplaceChild("left_wing_2", CubeListBuilder.create().texOffs(94, 63).addBox(0.0F, -1.0F, -8.0F, 14.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(15.0F, 0.0F, -1.0F));
		PartDefinition right_wing_1 = body.addOrReplaceChild("right_wing_1", CubeListBuilder.create().texOffs(66, 86).mirror().addBox(-15.0F, -1.0F, -11.0F, 15.0F, 2.0F, 24.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-13.0F, -1.0F, 0.0F));
		PartDefinition right_wing_2 = right_wing_1.addOrReplaceChild("right_wing_2", CubeListBuilder.create().texOffs(94, 63).mirror().addBox(-14.0F, -1.0F, -8.0F, 14.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-15.0F, 0.0F, -1.0F));
		PartDefinition body2 = body.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(0, 150).addBox(0.0F, -10.0F, 0.0F, 0.0F, 9.0F, 15.0F, new CubeDeformation(0.05F)), PartPose.offset(0.0F, 0.0F, 17.0F));
		PartDefinition cube_r3 = body2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 37).addBox(-10.0F, -3.0F, -8.0F, 23.0F, 2.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 2.0F, 1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition tail = body2.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(192, -27).addBox(0.0F, -2.0F, -1.0F, 0.0F, 4.0F, 31.0F, new CubeDeformation(0.05F)), PartPose.offset(0.0F, 0.0F, 16.0F));
		return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(MantaEntity manta, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root() {
        return this.body;
    }
}
