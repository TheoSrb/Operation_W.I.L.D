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
import net.tiew.operationWild.entity.client.animation.HyenaAnimations;
import net.tiew.operationWild.entity.custom.living.HyenaEntity;

public class HyenaModel<T extends HyenaEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "hyena_default"), "main");

	private final ModelPart ALL;
	private final ModelPart left_arm;
	private final ModelPart left_leg;
	private final ModelPart right_arm;
	private final ModelPart right_leg;
	private final ModelPart body;
	private final ModelPart neck;
	private final ModelPart head;
	private final ModelPart left_ear;
	private final ModelPart right_ear;
	private final ModelPart left_eyeBall;
	private final ModelPart right_eyeBall;
	private final ModelPart tail;

    public HyenaModel(ModelPart root) {

		this.ALL = root.getChild("ALL");
		this.left_arm = this.ALL.getChild("left_arm");
		this.left_leg = this.ALL.getChild("left_leg");
		this.right_arm = this.ALL.getChild("right_arm");
		this.right_leg = this.ALL.getChild("right_leg");
		this.body = this.ALL.getChild("body");
		this.neck = this.body.getChild("neck");
		this.head = this.neck.getChild("head");
		this.left_ear = this.head.getChild("left_ear");
		this.right_ear = this.head.getChild("right_ear");
		this.left_eyeBall = this.head.getChild("left_eyeBall");
		this.right_eyeBall = this.head.getChild("right_eyeBall");
		this.tail = this.body.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {

		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition ALL = partdefinition.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, -1.0F));
		PartDefinition left_arm = ALL.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offsetAndRotation(2.0F, 3.5F, -5.5F, -0.0873F, 0.0F, 0.0F));
		PartDefinition cube_r1 = left_arm.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(52, 33).addBox(3.0F, 1.0F, -3.0F, 2.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -1.7F, 1.5F, 0.0873F, 0.0F, 0.0F));
		PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offsetAndRotation(2.0F, 4.5F, 6.5F, -0.0873F, 0.0F, 0.0F));
		PartDefinition cube_r2 = left_leg.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(40, 45).addBox(3.0F, 1.0F, -3.0F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -2.7F, 1.5F, 0.0873F, 0.0F, 0.0F));
		PartDefinition right_arm = ALL.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, 3.5F, -5.5F, -0.0873F, 0.0F, 0.0F));
		PartDefinition cube_r3 = right_arm.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(52, 33).mirror().addBox(-5.0F, 1.0F, -3.0F, 2.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.0F, -1.7F, 1.5F, 0.0873F, 0.0F, 0.0F));
		PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, 4.5F, 6.5F, -0.0873F, 0.0F, 0.0F));
		PartDefinition cube_r4 = right_leg.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(40, 45).mirror().addBox(-6.0F, 1.0F, -3.0F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.0F, -2.7F, 1.5F, 0.0873F, 0.0F, 0.0F));
		PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -5.0F, -7.0F, 9.0F, 9.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(67, -11).addBox(1.5F, -10.0F, -10.0F, 0.0F, 5.0F, 11.0F, new CubeDeformation(0.0F))
		.texOffs(67, -11).mirror().addBox(-1.5F, -10.0F, -10.0F, 0.0F, 5.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 19).addBox(-3.5F, -5.0F, 3.0F, 7.0F, 9.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.0F, 0.0F));
		PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(28, 33).addBox(-3.5F, -3.0F, -4.0F, 7.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, -8.0F));
		PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 19).addBox(-4.0F, -4.0F, -5.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(20, 45).addBox(-2.5F, 0.0F, -10.0F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.1743F, -2.9924F, 5F, 0.0F, 0.0F));
		PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(12, 54).addBox(-2.0F, -4.0F, -0.5F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -2.5F, 0.0F, 0.0F, 0.0F, 0.4363F));
		PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(12, 54).mirror().addBox(-2.0F, -4.0F, -0.5F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.0F, -2.5F, 0.0F, 0.0F, 0.0F, -0.4363F));
		PartDefinition left_eyeBall = head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(25, 58).addBox(-1.0F, -0.5F, -0.025F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.025F)), PartPose.offset(3.0F, -1.5F, -5.0F));
		PartDefinition right_eyeBall = head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(25, 58).mirror().addBox(-1.0F, -0.5F, -0.025F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.025F)).mirror(false), PartPose.offset(-3.0F, -1.5F, -5.0F));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, 10.0F));
		PartDefinition cube_r5 = tail.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 78).addBox(5.0F, -5.0F, -1.0F, 0.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 3.5F, 0.5F, 0.0873F, 0.0F, 0.0F));
		PartDefinition cube_r6 = tail.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 78).addBox(-1.0F, -5.0F, 5.0F, 2.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, -4.5F, 0.0873F, 0.0F, 0.0F));
		return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(HyenaEntity hyena, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
		this.applyHeadRotation(netHeadYaw, headPitch);

		if (hyena.isBaby()) {
            float maturationPercent = (float) hyena.getMaturationPercentage() / 100f;
            float headScale = 2f - (2f - 1.0f) * maturationPercent;

            this.head.xScale *= headScale;
            this.head.yScale *= headScale;
            this.head.zScale *= headScale;
        }

		this.neck.xRot = (float) Math.toRadians(hyena.getHeadX());
		this.head.xRot = (float) Math.toRadians(5 + (-hyena.getHeadX()));
		this.head.y = hyena.getHeadX() / 20;
	}

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.ALL.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch) {
        pNetHeadYaw = Mth.clamp(pNetHeadYaw, -30.0F, 30.0F);
        pHeadPitch = Mth.clamp(pHeadPitch, -30.0F, 30.0F);

        this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);
    }

    @Override
    public ModelPart root() {
        return this.ALL;
    }
}
