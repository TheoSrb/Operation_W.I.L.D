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
import net.tiew.operationWild.entity.client.animation.MandrillAnimations;
import net.tiew.operationWild.entity.custom.living.MandrillEntity;

public class MandrillModel<T extends MandrillEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "mandrill_default"), "main");
	private final ModelPart ALL2;
	private final ModelPart ALL;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart left_eyeBall;
	private final ModelPart right_eyeBall;
	private final ModelPart tail;
	private final ModelPart right_arm;
	private final ModelPart left_arm;
	private final ModelPart right_leg;
	private final ModelPart right_leg2;
	private final ModelPart left_leg;
	private final ModelPart left_leg2;

    public MandrillModel(ModelPart root) {
		this.ALL2 = root.getChild("ALL2");
		this.ALL = this.ALL2.getChild("ALL");
		this.body = this.ALL.getChild("body");
		this.head = this.body.getChild("head");
		this.left_eyeBall = this.head.getChild("left_eyeBall");
		this.right_eyeBall = this.head.getChild("right_eyeBall");
		this.tail = this.body.getChild("tail");
		this.right_arm = this.ALL.getChild("right_arm");
		this.left_arm = this.ALL.getChild("left_arm");
		this.right_leg = this.ALL.getChild("right_leg");
		this.right_leg2 = this.right_leg.getChild("right_leg2");
		this.left_leg = this.ALL.getChild("left_leg");
		this.left_leg2 = this.left_leg.getChild("left_leg2");
    }

    public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 10.0F, 0.0F));

		PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -5.0F, -8.0F, 10.0F, 10.0F, 19.0F, new CubeDeformation(0.0F))
				.texOffs(49, -19).addBox(4.5F, 5.0F, -8.0F, 0.0F, 2.0F, 19.0F, new CubeDeformation(0.01F))
				.texOffs(49, -19).mirror().addBox(-4.5F, 5.0F, -8.0F, 0.0F, 2.0F, 19.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 29).addBox(-4.0F, -5.0F, -7.0F, 8.0F, 10.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(0, 46).addBox(-2.0F, -2.0F, -10.0F, 4.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(20, 46).addBox(-2.0F, 4.05F, -9.45F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offset(0.0F, -3.0F, -8.0F));

		PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 59).addBox(-4.0F, -10.0F, 4.0F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-9.85F, 2.5F, -1.5F, 0.4188F, -0.8435F, 1.2972F));

		PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 59).mirror().addBox(4.0F, -10.0F, 4.0F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(9.85F, 2.5F, -1.5F, 0.4188F, 0.8435F, -1.2972F));

		PartDefinition cube_r3 = head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 59).addBox(-4.0F, -10.0F, 4.0F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-11.35F, 4.5F, 4.5F, 1.3211F, -1.074F, 0.6336F));

		PartDefinition cube_r4 = head.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 59).mirror().addBox(4.0F, -10.0F, 4.0F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(11.35F, 4.5F, 4.5F, 1.3211F, 1.074F, -0.6336F));

		PartDefinition cube_r5 = head.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 71).mirror().addBox(4.0F, -10.0F, 4.0F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(7.6F, 8.5F, 4.5F, 1.6712F, 0.5812F, -0.2613F));

		PartDefinition cube_r6 = head.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 71).addBox(-4.0F, -10.0F, 4.0F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-7.6F, 8.5F, 4.5F, 1.6712F, -0.5812F, 0.2613F));

		PartDefinition left_eyeBall = head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(0, 59).addBox(-1.5F, -0.5F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offset(-2.5F, -3.5F, -7.05F));

		PartDefinition right_eyeBall = head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(0, 59).mirror().addBox(-1.5F, -0.5F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offset(2.5F, -3.5F, -7.05F));

		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(52, 29).addBox(0.0F, -5.0F, -3.0F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, -4.0F, 11.0F));

		PartDefinition right_arm = ALL.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(52, 55).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, 5.0F, -5.5F));

		PartDefinition left_arm = ALL.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(52, 43).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, 5.0F, -5.5F));

		PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(30, 43).addBox(-2.0F, -3.0F, -3.0F, 5.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 3.0F, 7.0F));

		PartDefinition right_leg2 = right_leg.addOrReplaceChild("right_leg2", CubeListBuilder.create().texOffs(32, 57).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 5.0F, -1.0F));

		PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(30, 29).addBox(-3.0F, -3.0F, -3.0F, 5.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 3.0F, 7.0F));

		PartDefinition left_leg2 = left_leg.addOrReplaceChild("left_leg2", CubeListBuilder.create().texOffs(20, 57).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 5.0F, -1.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(MandrillEntity mandrill, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        if (mandrill.isBaby()) {
            float maturationPercent = (float) mandrill.getMaturationPercentage() / 100f;
            float headScale = 2f - (2f - 1.0f) * maturationPercent;

            this.head.xScale *= headScale;
            this.head.yScale *= headScale;
            this.head.zScale *= headScale;
        }
        this.applyHeadRotation(netHeadYaw, headPitch);

		this.animate(mandrill.idleAnimationState, MandrillAnimations.MISC_IDLE, ageInTicks, 1.0f);

        this.animateWalk(MandrillAnimations.MOVE_WALK, limbSwing, limbSwingAmount, 5f, 4f);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch) {
        pNetHeadYaw = Mth.clamp(pNetHeadYaw, -30.0F, 30.0F);
        pHeadPitch = Mth.clamp(pHeadPitch, -30.0F, 30.0F);

        this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);
    }

    @Override
    public ModelPart root() {
        return this.ALL2;
    }
}
