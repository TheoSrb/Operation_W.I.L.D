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
import net.tiew.operationWild.entity.animals.aquatic.WalrusEntity;
import net.tiew.operationWild.entity.client.animation.WalrusAnimations;

public class WalrusModel<T extends WalrusEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "walrus_default"), "main");

	private final ModelPart ALL2;
	private final ModelPart ALL;
	private final ModelPart left_arm;
	private final ModelPart left_arm2;
	private final ModelPart right_arm;
	private final ModelPart right_arm2;
	private final ModelPart body;
	private final ModelPart neck;
	private final ModelPart head;
	private final ModelPart left_eyeBall;
	private final ModelPart right_eyeBall;
	private final ModelPart left_tooth;
	private final ModelPart right_tooth;
	private final ModelPart tail_1;
	private final ModelPart tail_2;
	private final ModelPart left_leg;
	private final ModelPart right_leg;

    public WalrusModel(ModelPart root) {
		this.ALL2 = root.getChild("ALL2");
		this.ALL = this.ALL2.getChild("ALL");
		this.left_arm = this.ALL.getChild("left_arm");
		this.left_arm2 = this.left_arm.getChild("left_arm2");
		this.right_arm = this.ALL.getChild("right_arm");
		this.right_arm2 = this.right_arm.getChild("right_arm2");
		this.body = this.ALL.getChild("body");
		this.neck = this.body.getChild("neck");
		this.head = this.neck.getChild("head");
		this.left_eyeBall = this.head.getChild("left_eyeBall");
		this.right_eyeBall = this.head.getChild("right_eyeBall");
		this.left_tooth = this.head.getChild("left_tooth");
		this.right_tooth = this.head.getChild("right_tooth");
		this.tail_1 = this.body.getChild("tail_1");
		this.tail_2 = this.tail_1.getChild("tail_2");
		this.left_leg = this.tail_2.getChild("left_leg");
		this.right_leg = this.tail_2.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 10.0F, 7.0F));

		PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition left_arm = ALL.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(46, 133).addBox(-1.0F, -2.0F, -6.0F, 3.0F, 16.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(13.0F, 0.0F, -12.0F));

		PartDefinition left_arm2 = left_arm.addOrReplaceChild("left_arm2", CubeListBuilder.create(), PartPose.offset(1.0F, 12.0F, 0.0F));

		PartDefinition cube_r1 = left_arm2.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(66, 121).addBox(-7.0F, 18.0F, 23.0F, 14.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -18.025F, -29.0F, 0.0F, 0.2618F, 0.0F));

		PartDefinition right_arm = ALL.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(46, 133).mirror().addBox(-2.0F, -2.0F, -6.0F, 3.0F, 16.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-13.0F, 0.0F, -12.0F));

		PartDefinition right_arm2 = right_arm.addOrReplaceChild("right_arm2", CubeListBuilder.create(), PartPose.offset(-1.0F, 12.0F, 0.0F));

		PartDefinition cube_r2 = right_arm2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(66, 121).mirror().addBox(-7.0F, 18.0F, 23.0F, 14.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, -18.025F, -29.0F, 0.0F, -0.2618F, 0.0F));

		PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, -7.0F));

		PartDefinition cube_r3 = body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -2.0F, -6.0F, 24.0F, 22.0F, 33.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, -9.0F, -0.0873F, 0.0F, 0.0F));

		PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(82, 55).addBox(-8.0F, -23.0F, -9.0F, 16.0F, 24.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, -8.0F, 0.1745F, 0.0F, 0.0F));

		PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(114, 0).addBox(-6.0F, -12.0F, -7.0F, 12.0F, 13.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -21.0F, -2.0F, -0.1745F, 0.0F, 0.0F));

		PartDefinition cube_r4 = head.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(102, 133).addBox(-6.0F, -5.0F, -16.0F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(124, 110).addBox(2.0F, -5.0F, -16.0F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, -0.025F, 0.1309F, 0.0F, 0.0F));

		PartDefinition cube_r5 = head.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(114, 27).addBox(-6.0F, -11.0F, -16.0F, 11.0F, 11.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition left_eyeBall = head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(142, 12).addBox(0.0F, -1.0F, -1.5F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.05F)), PartPose.offset(6.05F, -10.0F, -3.5F));

		PartDefinition right_eyeBall = head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(142, 12).mirror().addBox(0.0F, -1.0F, -1.5F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offset(-6.05F, -10.0F, -3.5F));

		PartDefinition left_tooth = head.addOrReplaceChild("left_tooth", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r6 = left_tooth.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(130, 133).addBox(2.0F, 1.0F, -14.0F, 2.0F, 17.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition right_tooth = head.addOrReplaceChild("right_tooth", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r7 = right_tooth.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(130, 133).mirror().addBox(-4.0F, 1.0F, -14.0F, 2.0F, 17.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition tail_1 = body.addOrReplaceChild("tail_1", CubeListBuilder.create().texOffs(0, 55).addBox(-10.0F, -7.0F, 0.0F, 20.0F, 18.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 16.0F));

		PartDefinition tail_2 = tail_1.addOrReplaceChild("tail_2", CubeListBuilder.create().texOffs(0, 94).addBox(-7.0F, -6.0F, 0.0F, 14.0F, 14.0F, 19.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 21.0F));

		PartDefinition left_leg = tail_2.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(66, 121).addBox(-1.0F, -1.0F, -6.0F, 14.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 6.975F, 15.0F, 0.0F, -0.2618F, 0.0F));

		PartDefinition right_leg = tail_2.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(66, 121).mirror().addBox(-13.0F, -1.025F, -6.0F, 14.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.0F, 7.0F, 15.0F, 0.0F, 0.2618F, 0.0F));

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

		if (walrus.transitionIdleSleep.isStarted()) {
			this.animate(walrus.transitionIdleSleep, WalrusAnimations.TRANSITION_IDLE_SLEEP, ageInTicks, 1f);
			return;
		}

		if (walrus.transitionSleepIdle.isStarted()) {
			this.animate(walrus.transitionSleepIdle, WalrusAnimations.TRANSITION_SLEEP_IDLE, ageInTicks, 1f);
			return;
		}

		if (walrus.isNapping()) {
			this.animate(walrus.napAnimationState, WalrusAnimations.NAP, ageInTicks, 1.0f);
			return;
		}

		if (walrus.scratchAnimationState.isStarted()) {
			this.animate(walrus.scratchAnimationState, WalrusAnimations.SCRATCH, ageInTicks, 1.0f);
		}
		if (walrus.stretchesAnimationState.isStarted()) {
			this.animate(walrus.stretchesAnimationState, WalrusAnimations.STRETCHES, ageInTicks, 1.25f);
		}
		if (walrus.laughAnimationState.isStarted()) {
			this.animate(walrus.laughAnimationState, WalrusAnimations.LAUGH, ageInTicks, 1.1f);
		}

		if (walrus.isInWater()) {
			this.animate(walrus.idleWaterAnimationState, WalrusAnimations.MISC_IDLE_UNDER_WATER, ageInTicks, 1.0f);
		} else {
			this.animate(walrus.idleAnimationState, WalrusAnimations.MISC_IDLE, ageInTicks, 1.0f);
		}

		if (!walrus.isInWater()) {
			this.animateWalk(WalrusAnimations.MOVE_WALK, limbSwing, limbSwingAmount, 6f, 6f);
		} else {
			if (walrus.isVehicle()) {
				this.animateWalk(WalrusAnimations.MOVE_SWIM, limbSwing, limbSwingAmount, 1.5f, 1.5f);
			} else {
				this.animateWalk(WalrusAnimations.MOVE_SWIM, limbSwing, limbSwingAmount, 2f, 2f);
			}
		}

		if (walrus.level().isClientSide()) {
			if (walrus.isInWater()) {
				walrus.setBodyZRot((float) Math.toDegrees(this.ALL.zRot));
				walrus.setBodyXRot((float) Math.toDegrees(this.ALL.xRot));
			} else {
				walrus.setBodyZRot((float) Math.toDegrees(this.body.zRot));
				walrus.setBodyXRot((float) Math.toDegrees(this.body.xRot));
			}
		}
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
