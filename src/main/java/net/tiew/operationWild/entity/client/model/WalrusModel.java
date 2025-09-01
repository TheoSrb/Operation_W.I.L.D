package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.WalrusEntity;

public class WalrusModel<T extends WalrusEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "walrus_default"), "main");

	private final ModelPart body;
	private final ModelPart neck;
	private final ModelPart head;
	private final ModelPart tail_1;
	private final ModelPart tail_2;
	private final ModelPart left_leg;
	private final ModelPart right_leg;
	private final ModelPart tail_3;
	private final ModelPart left_arm;
	private final ModelPart right_arm;

    public WalrusModel(ModelPart root) {

		this.body = root.getChild("body");
		this.neck = this.body.getChild("neck");
		this.head = this.neck.getChild("head");
		this.tail_1 = this.body.getChild("tail_1");
		this.tail_2 = this.tail_1.getChild("tail_2");
		this.left_leg = this.tail_2.getChild("left_leg");
		this.right_leg = this.tail_2.getChild("right_leg");
		this.tail_3 = this.tail_2.getChild("tail_3");
		this.left_arm = this.body.getChild("left_arm");
		this.right_arm = this.body.getChild("right_arm");
    }

    public static LayerDefinition createBodyLayer() {

		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 9.0F, 0.0F));
		PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -2.0F, -6.0F, 24.0F, 22.0F, 33.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, -9.0F, -0.0873F, 0.0F, 0.0F));
		PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(82, 55).addBox(-8.0F, -23.0F, -9.0F, 16.0F, 24.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, -8.0F, 0.1745F, 0.0F, 0.0F));
		PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(114, 0).addBox(-6.0F, -12.0F, -7.0F, 12.0F, 13.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -21.0F, -2.0F, -0.1745F, 0.0F, 0.0F));
		PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(130, 133).mirror().addBox(-4.0F, 1.0F, -14.0F, 2.0F, 17.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(130, 133).addBox(3.0F, 1.0F, -14.0F, 2.0F, 17.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(114, 27).addBox(-5.0F, -11.0F, -16.0F, 11.0F, 11.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 0.1309F, 0.0F, 0.0F));
		PartDefinition cube_r3 = head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(102, 133).addBox(-6.0F, -5.0F, -16.0F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(124, 110).addBox(2.0F, -5.0F, -16.0F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, -0.025F, 0.1309F, 0.0F, 0.0F));
		PartDefinition tail_1 = body.addOrReplaceChild("tail_1", CubeListBuilder.create().texOffs(0, 55).addBox(-10.0F, -7.0F, 0.0F, 20.0F, 18.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 16.0F));
		PartDefinition tail_2 = tail_1.addOrReplaceChild("tail_2", CubeListBuilder.create().texOffs(0, 94).addBox(-7.0F, -6.0F, 0.0F, 14.0F, 14.0F, 19.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 21.0F));
		PartDefinition left_leg = tail_2.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(66, 121).addBox(-1.0F, -1.0F, -6.0F, 14.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 6.975F, 15.0F, 0.0F, -0.2618F, 0.0F));
		PartDefinition right_leg = tail_2.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(66, 121).mirror().addBox(-13.0F, -1.025F, -6.0F, 14.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.0F, 7.0F, 15.0F, 0.0F, 0.2618F, 0.0F));
		PartDefinition tail_3 = tail_2.addOrReplaceChild("tail_3", CubeListBuilder.create().texOffs(66, 97).addBox(-6.0F, -4.0F, 0.0F, 12.0F, 7.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, 19.0F));
		PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(46, 133).addBox(-1.0F, -2.0F, -6.0F, 3.0F, 16.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(13.0F, 1.0F, -5.0F));
		PartDefinition cube_r4 = left_arm.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(66, 121).addBox(-7.0F, 18.0F, 23.0F, 14.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.025F, -29.0F, 0.0F, 0.2618F, 0.0F));
		PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(46, 133).mirror().addBox(-2.0F, -2.0F, -6.0F, 3.0F, 16.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-13.0F, 1.0F, -5.0F));
		PartDefinition cube_r5 = right_arm.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(66, 121).mirror().addBox(-7.0F, 18.0F, 23.0F, 14.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -6.025F, -29.0F, 0.0F, -0.2618F, 0.0F));
		return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(WalrusEntity walrus, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        if (walrus.isBaby()) {
            float maturationPercent = (float) walrus.getMaturationPercentage() / 100f;
            float headScale = 2f - (2f - 1.0f) * maturationPercent;

            this.head.xScale *= headScale;
            this.head.yScale *= headScale;
            this.head.zScale *= headScale;
        }
        this.applyHeadRotation(netHeadYaw, headPitch);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch) {
        pNetHeadYaw = Mth.clamp(pNetHeadYaw, -30.0F, 30.0F);
        pHeadPitch = Mth.clamp(pHeadPitch, -30.0F, 30.0F);

        this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);
    }

    @Override
    public ModelPart root() {
        return this.body;
    }
}
