package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.item.ItemDisplayContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.client.animation.KodiakAnimations;
import net.tiew.operationWild.entity.client.animation.LionAnimations;

public class LionModel<T extends LionEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "lion_default"), "main");

	private final ModelPart ALL2;
	private final ModelPart ALL;
	private final ModelPart body;
	private final ModelPart tail_1;
	private final ModelPart tail_2;
	private final ModelPart head;
	private final ModelPart left_ear;
	private final ModelPart right_ear;
	private final ModelPart left_eyeBall;
	private final ModelPart right_eyeBall;
	private final ModelPart mane;
	private final ModelPart left_arm;
	private final ModelPart left_leg;
	private final ModelPart right_arm;
	private final ModelPart right_leg;

    public LionModel(ModelPart root) {
		this.ALL2 = root.getChild("ALL2");
		this.ALL = this.ALL2.getChild("ALL");
		this.body = this.ALL.getChild("body");
		this.tail_1 = this.body.getChild("tail_1");
		this.tail_2 = this.tail_1.getChild("tail_2");
		this.head = this.body.getChild("head");
		this.left_ear = this.head.getChild("left_ear");
		this.right_ear = this.head.getChild("right_ear");
		this.left_eyeBall = this.head.getChild("left_eyeBall");
		this.right_eyeBall = this.head.getChild("right_eyeBall");
		this.mane = this.head.getChild("mane");
		this.left_arm = this.ALL.getChild("left_arm");
		this.left_leg = this.ALL.getChild("left_leg");
		this.right_arm = this.ALL.getChild("right_arm");
		this.right_leg = this.ALL.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 9.0F, -1.0F));

		PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -10.0F, -8.0F, 10.0F, 10.0F, 20.0F, new CubeDeformation(0.25F))
				.texOffs(0, 81).addBox(-7.0F, -10.0F, -8.0F, 10.0F, 10.0F, 20.0F, new CubeDeformation(0.3F))
				.texOffs(0, 115).addBox(-6.5F, -12.0F, -2.0F, 9.0F, 2.0F, 11.0F, new CubeDeformation(0.0F))
				.texOffs(86, 35).addBox(3.0F, 0.25F, -8.0F, 0.0F, 2.0F, 20.0F, new CubeDeformation(0.01F))
				.texOffs(86, 35).mirror().addBox(-7.0F, 0.25F, -8.0F, 0.0F, 2.0F, 20.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(2.0F, 6.0F, -2.0F));

		PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(100, 71).mirror().addBox(-6.5F, -5.5F, -0.5F, 12.0F, 11.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.75F, -6.5F, 6.5F, -1.5708F, -0.0835F, -1.5706F));

		PartDefinition cube_r2 = body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(100, 71).addBox(-5.5F, -5.5F, -0.5F, 12.0F, 11.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.75F, -6.5F, 6.5F, -1.5708F, 0.0835F, 1.5706F));

		PartDefinition tail_1 = body.addOrReplaceChild("tail_1", CubeListBuilder.create().texOffs(16, 47).addBox(-1.5F, -2.0F, 0.0F, 3.0F, 3.0F, 8.0F, new CubeDeformation(-0.25F)), PartPose.offset(-2.0F, -8.0F, 12.0F));

		PartDefinition tail_2 = tail_1.addOrReplaceChild("tail_2", CubeListBuilder.create().texOffs(38, 47).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 8.0F, new CubeDeformation(-0.25F))
				.texOffs(52, 66).addBox(-2.0F, -2.5F, 7.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(-0.25F))
				.texOffs(71, 57).addBox(-2.0F, -2.3F, 8.5F, 4.0F, 0.0F, 2.0F, new CubeDeformation(0.01F))
				.texOffs(71, 57).mirror().addBox(-2.0F, 1.3F, 8.5F, 4.0F, 0.0F, 2.0F, new CubeDeformation(0.01F)).mirror(false)
				.texOffs(72, 59).addBox(1.8F, -2.5F, 8.5F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.01F))
				.texOffs(78, 59).mirror().addBox(-1.8F, -2.5F, 8.5F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(0.0F, 0.0F, 8.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 31).addBox(-6.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(30, 58).addBox(-4.5F, -1.0F, -13.0F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(108, 126).addBox(-6.5F, 3.0F, -12.0F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(60, 0).addBox(-4.5F, 4.0F, -13.0F, 5.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, -8.0F));

		PartDefinition cube_r3 = head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(75, 75).mirror().addBox(0.0F, -8.5F, -0.5F, 0.0F, 9.0F, 21.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-6.5F, 3.5F, -11.5F, 0.1002F, -0.1995F, 0.2146F));

		PartDefinition cube_r4 = head.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(75, 75).addBox(0.0F, -8.5F, -0.5F, 0.0F, 9.0F, 21.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(2.5F, 3.5F, -11.5F, 0.1002F, 0.1995F, -0.2146F));

		PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(60, 26).addBox(-0.5F, -2.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -3.0F, -4.5F));

		PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(60, 26).mirror().addBox(-2.5F, -2.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-5.0F, -3.0F, -4.5F));

		PartDefinition left_eyeBall = head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offset(-4.0F, -2.5F, -8.0F));

		PartDefinition right_eyeBall = head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(0, 17).mirror().addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offset(0.0F, -2.5F, -8.0F));

		PartDefinition mane = head.addOrReplaceChild("mane", CubeListBuilder.create().texOffs(79, 19).addBox(-9.5F, -7.5F, -4.0F, 15.0F, 15.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(48, 14).addBox(-9.5F, 7.5F, -4.0F, 15.0F, 2.0F, 0.0F, new CubeDeformation(0.01F))
				.texOffs(48, 14).addBox(-9.5F, 7.5F, 4.0F, 15.0F, 2.0F, 0.0F, new CubeDeformation(0.01F))
				.texOffs(86, 1).addBox(5.5F, 7.5F, -4.0F, 0.0F, 2.0F, 8.0F, new CubeDeformation(0.01F))
				.texOffs(86, 1).mirror().addBox(-9.5F, 7.5F, -4.0F, 0.0F, 2.0F, 8.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition left_arm = ALL.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(48, 30).addBox(-1.025F, -1.0F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 3.0F, -6.0F));

		PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(48, 30).addBox(-1.975F, -1.0F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(3.475F, 3.0F, 9.0F));

		PartDefinition right_arm = ALL.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(48, 30).mirror().addBox(-3.0F, -1.0F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.975F, 3.0F, -6.0F));

		PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(48, 30).mirror().addBox(-2.025F, -1.0F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.475F, 3.0F, 9.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(LionEntity lion, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        if (lion.isBaby()) {
            float maturationPercent = (float) lion.getMaturationPercentage() / 100f;
            float headScale = 1.6f - (1.6f - 1.0f) * maturationPercent;

            this.head.xScale *= headScale;
            this.head.yScale *= headScale;
            this.head.zScale *= headScale;
        }

        this.applyHeadRotation(netHeadYaw, headPitch);

		if (lion.isMad()) {
			this.left_eyeBall.xScale = 0;
			this.left_eyeBall.yScale = 0;
			this.left_eyeBall.zScale = 0;

			this.right_eyeBall.xScale = 0;
			this.right_eyeBall.yScale = 0;
			this.right_eyeBall.zScale = 0;
		}

		if (lion.isCombo()) {
			if (lion.isCombo(1)) {
				this.animate(lion.attack1Combo, LionAnimations.ATTACK_STRIKE, ageInTicks, 0.925f * OWEntity.comboSpeedMultiplier);
			}
			if (lion.isCombo(2)) {
				this.animate(lion.attack2Combo, LionAnimations.ATTACK_STRIKE_2, ageInTicks, 1.05f * OWEntity.comboSpeedMultiplier);
			}
			if (lion.isCombo(3)) {
				this.animate(lion.attack3Combo, LionAnimations.ATTACK_STRIKE_3, ageInTicks, 1.15f * OWEntity.comboSpeedMultiplier);
			}

			return;
		}

		/*if (lion.transitionIdleSit.isStarted()) {
			this.animate(lion.transitionIdleSit, KodiakAnimations.TRANSITION_IDLE_SIT, ageInTicks, 1.0f);
			return;
		}

		if (lion.transitionSitIdle.isStarted()) {
			this.animate(lion.transitionSitIdle, KodiakAnimations.TRANSITION_SIT_IDLE, ageInTicks, 1.0f);
			return;
		}

		if (lion.transitionIdleSleep.isStarted()) {
			this.animate(lion.transitionIdleSleep, KodiakAnimations.TRANSITION_IDLE_SLEEP, ageInTicks, 2.0f);
			return;
		}

		if (lion.transitionSleepIdle.isStarted()) {
			this.animate(lion.transitionSleepIdle, KodiakAnimations.TRANSITION_SLEEP_IDLE, ageInTicks, 2.0f);
			return;
		}*/


		if (lion.isSitting()) {
			this.animate(lion.sittingAnimationState, LionAnimations.SIT, ageInTicks, 1.0f);
			return;
		}


		this.animate(lion.idleAnimationState, LionAnimations.MISC_IDLE, ageInTicks, 1.0f);

		if (lion.isRunning() || lion.getState() == 2) {
			if (lion.isVehicle()) {
				this.animateWalk(LionAnimations.MOVE_RUN, limbSwing, limbSwingAmount, 1.25f, 1.0f);
			} else {
				this.animateWalk(LionAnimations.MOVE_RUN, limbSwing, limbSwingAmount, 1.25f, 1.35f);
			}
		} else {
			this.animateWalk(LionAnimations.MOVE_WALK, limbSwing, limbSwingAmount, 6f, 6f);
		}


		if (lion.level().isClientSide()) {
			lion.setBodyZRot((float) Math.toDegrees(this.ALL.zRot));
			lion.setBodyXRot((float) Math.toDegrees(this.ALL.xRot));
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
