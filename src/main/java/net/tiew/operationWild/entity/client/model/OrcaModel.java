package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;
import net.tiew.operationWild.entity.attacks.OWAttackLogic;
import net.tiew.operationWild.entity.attacks.OWAttacksHandler;
import net.tiew.operationWild.entity.client.animation.CrocodileAnimations;
import net.tiew.operationWild.entity.client.animation.OrcaAnimations;

import javax.swing.text.html.parser.Entity;

public class OrcaModel<T extends OrcaEntity> extends HierarchicalModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "orca_default"), "main");

	private float prevLimbSwing = 0f;

	public float externalRiderPitch = 0f;

	private final ModelPart ALL2;
	private final ModelPart ALL;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart mouth;
	private final ModelPart mouth_down;
	private final ModelPart mouth_up;
	private final ModelPart tail;
	private final ModelPart front_tail;
	private final ModelPart back_tail;
	private final ModelPart left_fan;
	private final ModelPart right_fan;

	public OrcaModel(ModelPart root) {
		this.ALL2 = root.getChild("ALL2");
		this.ALL = this.ALL2.getChild("ALL");
		this.body = this.ALL.getChild("body");
		this.head = this.body.getChild("head");
		this.mouth = this.head.getChild("mouth");
		this.mouth_down = this.mouth.getChild("mouth_down");
		this.mouth_up = this.mouth.getChild("mouth_up");
		this.tail = this.body.getChild("tail");
		this.front_tail = this.tail.getChild("front_tail");
		this.back_tail = this.tail.getChild("back_tail");
		this.left_fan = this.body.getChild("left_fan");
		this.right_fan = this.body.getChild("right_fan");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 7.0F, 0.0F));

		PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -14.0F, -19.0F, 24.0F, 30.0F, 38.0F, new CubeDeformation(0.0F))
				.texOffs(237, 0).addBox(-12.0F, -14.0F, -19.0F, 24.0F, 30.0F, 38.0F, new CubeDeformation(0.5F))
				.texOffs(161, 38).addBox(-11.0F, -16.0F, 5.0F, 10.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(178, 79).addBox(-11.0F, -28.0F, 17.0F, 10.0F, 14.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(178, 79).mirror().addBox(1.0F, -28.0F, 17.0F, 10.0F, 14.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(148, 79).mirror().addBox(-6.0F, -28.0F, -8.0F, 12.0F, 14.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(161, 38).mirror().addBox(1.0F, -16.0F, 5.0F, 10.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(154, 21).addBox(-6.0F, -16.0F, -19.0F, 12.0F, 2.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(124, 28).addBox(-1.0F, -44.0F, 20.0F, 3.0F, 17.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 6.0F, -35.6F, -0.3054F, 0.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(252, 136).addBox(-15.0F, -2.4F, -9.0F, 30.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, -19.0F));

		PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(228, 92).mirror().addBox(0.0F, -28.0F, -1.0F, 0.0F, 29.0F, 12.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(15.0F, -2.0F, -7.0F, -0.2712F, -0.0482F, -0.1276F));

		PartDefinition cube_r3 = head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(228, 92).addBox(0.0F, -28.0F, -1.0F, 0.0F, 29.0F, 12.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-15.0F, -2.0F, -7.0F, -0.2712F, 0.0482F, 0.1276F));

		PartDefinition mouth = head.addOrReplaceChild("mouth", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition mouth_down = mouth.addOrReplaceChild("mouth_down", CubeListBuilder.create().texOffs(9, 159).addBox(-9.0F, -2.0F, -24.0F, 18.0F, 7.0F, 24.0F, new CubeDeformation(0.0F))
				.texOffs(5, 115).addBox(-8.0F, -4.0F, -23.9F, 0.0F, 3.0F, 23.0F, new CubeDeformation(0.025F))
				.texOffs(3, 132).addBox(-8.0F, -4.0F, -23.5F, 16.0F, 3.0F, 0.0F, new CubeDeformation(0.025F))
				.texOffs(5, 115).mirror().addBox(8.0F, -4.0F, -23.9F, 0.0F, 3.0F, 23.0F, new CubeDeformation(0.025F)).mirror(false), PartPose.offset(0.0F, 2.0F, 0.0F));

		PartDefinition mouth_up = mouth.addOrReplaceChild("mouth_up", CubeListBuilder.create().texOffs(123, 168).addBox(-9.0F, -9.0F, -21.0F, 18.0F, 13.0F, 24.0F, new CubeDeformation(0.0F))
				.texOffs(60, 120).addBox(-8.0F, 4.0F, -20.9F, 0.0F, 3.0F, 23.0F, new CubeDeformation(0.025F))
				.texOffs(60, 120).mirror().addBox(8.0F, 4.0F, -20.9F, 0.0F, 3.0F, 23.0F, new CubeDeformation(0.025F)).mirror(false)
				.texOffs(60, 135).addBox(-8.0F, 4.0F, -20.5F, 16.0F, 3.0F, 0.0F, new CubeDeformation(0.025F)), PartPose.offset(0.0F, -4.0F, -3.0F));

		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 19.0F));

		PartDefinition front_tail = tail.addOrReplaceChild("front_tail", CubeListBuilder.create().texOffs(0, 68).addBox(-8.0F, -11.0F, 0.0F, 16.0F, 23.0F, 35.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition back_tail = tail.addOrReplaceChild("back_tail", CubeListBuilder.create().texOffs(102, 113).addBox(-15.0F, -1.0F, -3.0F, 30.0F, 2.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, 34.0F));

		PartDefinition left_fan = body.addOrReplaceChild("left_fan", CubeListBuilder.create().texOffs(124, 0).addBox(0.0F, -1.0F, -6.0F, 21.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(12.0F, 10.0F, -10.0F));

		PartDefinition right_fan = body.addOrReplaceChild("right_fan", CubeListBuilder.create().texOffs(124, 0).mirror().addBox(-21.0F, -1.0F, -6.0F, 21.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-12.0F, 10.0F, -10.0F));

		return LayerDefinition.create(meshdefinition, 512, 512);
	}

	@Override
	public void setupAnim(OrcaEntity orca, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		if (Math.abs(externalRiderPitch) > 0.01f) {
			this.ALL2.xRot = (float) Math.toRadians(externalRiderPitch);
		}

		if (orca.isBaby()) {
			float maturationPercent = (float) orca.getMaturationPercentage() / 100f;
			float headScale = 1.5f - (1.6f - 1.0f) * maturationPercent;

			this.head.xScale *= headScale;
			this.head.yScale *= headScale;
			this.head.zScale *= headScale;
		}

		this.applyHeadRotation(netHeadYaw, headPitch);

		if (orca.isCombo(1)) {
			this.animate(orca.attack1Combo, OrcaAnimations.ATTACK_STRIKE, ageInTicks, 1.0f);
		}
		if (orca.isCombo(2)) {
			this.animate(orca.attack2Combo, OrcaAnimations.ATTACK_STRIKE_2, ageInTicks, 1.0f);
		}
		if (orca.isCombo(3)) {
			this.animate(orca.attack3Combo, OrcaAnimations.ATTACK_STRIKE_3, ageInTicks, 1.0f);
		}

		/*if (orca.transitionIdleSit.isStarted()) {
			this.animate(orca.transitionIdleSit, CrocodileAnimations.TRANSITION_IDLE_SIT, ageInTicks, 2.0f);
			captureBodyState(orca, 12.5453f, this.ALL2, this.ALL, this.body);
			return;
		}

		if (orca.transitionSitIdle.isStarted()) {
			this.animate(orca.transitionSitIdle, CrocodileAnimations.TRANSITION_SIT_IDLE, ageInTicks, 2.0f);
			captureBodyState(orca, 12.5453f, this.ALL2, this.ALL, this.body);
			return;
		}

		if (orca.transitionIdleSleep.isStarted()) {
			this.animate(orca.transitionIdleSleep, CrocodileAnimations.TRANSITION_IDLE_NAP, ageInTicks, 1.0f);
			captureBodyState(orca, 12.5453f, this.ALL2, this.ALL, this.body);
			return;
		}

		if (orca.transitionSleepIdle.isStarted()) {
			this.animate(orca.transitionSleepIdle, CrocodileAnimations.TRANSITION_NAP_IDLE, ageInTicks, 1.0f);
			captureBodyState(orca, 12.5453f, this.ALL2, this.ALL, this.body);
			return;
		}

		if (orca.growlsAnimationState.isStarted()) {
			this.animate(orca.growlsAnimationState, CrocodileAnimations.MISC_IDLE_2, ageInTicks, 1.0f);
		}

		if (orca.gruntAnimationState.isStarted()) {
			this.animate(orca.gruntAnimationState, CrocodileAnimations.MISC_IDLE_3, ageInTicks, 1.0f);
		}

		if (orca.isNapping()) {
			this.animate(orca.napAnimationState, CrocodileAnimations.NAP, ageInTicks, 1.0f);
			captureBodyState(orca, 12.5453f, this.ALL2, this.ALL, this.body);
			return;
		}

		if (orca.isSitting()) {
			this.animate(orca.sittingAnimationState, CrocodileAnimations.SIT, ageInTicks, 1.0f);
			captureBodyState(orca, 12.5453f, this.ALL2, this.ALL, this.body);
			return;
		}*/

		if ((orca.isRunning() || orca.getState() == 2)) {
			float speed = orca.getControllingPassenger() != null ? 1.2f : 1.0f;
			this.animateWalk(OrcaAnimations.MOVE_SWIM, limbSwing, limbSwingAmount, speed, speed);

			if (walkAnimCrossed(OrcaAnimations.MOVE_SWIM, limbSwing, speed, 300L)) orca.onRightFootDown();
			if (walkAnimCrossed(OrcaAnimations.MOVE_SWIM, limbSwing, speed, 300L)) orca.onLeftFootDown();

		} else {
			float speed = orca.getControllingPassenger() != null ? 1.5f : 0.85f;
			this.animateWalk(OrcaAnimations.MOVE_SWIM, limbSwing, limbSwingAmount, speed, speed);

			if (walkAnimCrossed(OrcaAnimations.MOVE_SWIM, limbSwing, speed, 300L)) orca.onRightFootDown();
			if (walkAnimCrossed(OrcaAnimations.MOVE_SWIM, limbSwing, speed, 300L)) orca.onLeftFootDown();
		}

		this.animate(orca.idleAnimationState, OrcaAnimations.MISC_IDLE, ageInTicks, 1.0f);

		this.prevLimbSwing = limbSwing;

		captureBodyState(orca, 7f, orca.isCombo() ? 1.0f : 1.0f, this.ALL2, this.ALL, this.body);


		/*if (orca.level().isClientSide() && orca.isGrabbing()) {
			if (!orca.isInWater()) {
				orca.setBodyZRot((float) ((float) Math.toDegrees(this.head.zRot) + Math.toDegrees(this.neck.zRot)));
				orca.setBodyXRot((float) ((float) Math.toDegrees(this.head.xRot) + Math.toDegrees(this.neck.xRot)));
				orca.setBodyYRot((float) ((float) Math.toDegrees(this.head.yRot) + Math.toDegrees(this.neck.yRot)));
			} else {
				orca.setBodyZRot(0);
				orca.setBodyYRot(0);
				orca.setBodyXRot(0);
			}
		}*/
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		this.ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	private void captureBodyState(OrcaEntity orca, float restPoseYSum, float riderRotIntensity, ModelPart... boneChain) {
		if (!orca.level().isClientSide()) return;
		orca.setBodyZRot((float) Math.toDegrees((this.ALL2.zRot + this.ALL.zRot + this.body.zRot) * riderRotIntensity));
		orca.setBodyXRot((float) -Math.toDegrees((this.ALL2.xRot + this.ALL.xRot + this.body.xRot) * riderRotIntensity));
		float ySum = 0f;
		float xSum = 0f;
		for (ModelPart bone : boneChain) { ySum += bone.y; xSum += bone.x; }
		orca.bodyAnimY = ySum - restPoseYSum;
		orca.bodyAnimX = xSum - 0;
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

	/**
	 * Returns {@code true} on the <em>exact frame</em> a looping walk animation crosses a keyframe time.
	 * Use this to fire one-shot events (sounds, particles) at precise moments of a walk cycle.
	 *
	 * <p>How to find {@code triggerTimeMs}: open Blockbench, look at the keyframe timestamp of the
	 * moment you want (e.g. foot hits the ground), multiply seconds × 1000.</p>
	 *
	 * @param animation     the AnimationDefinition played via {@code animateWalk()}
	 * @param limbSwing     current limbSwing value (first param of setupAnim)
	 * @param speedScale    the {@code maxAnimationCycles} value passed to {@code animateWalk()}
	 * @param triggerTimeMs keyframe time in <strong>milliseconds</strong> to fire at
	 */
	private boolean walkAnimCrossed(AnimationDefinition animation, float limbSwing, float speedScale, long triggerTimeMs) {
		long durationMs = (long)(animation.lengthInSeconds() * 1000f);
		if (durationMs <= 0) return false;

		long cur  = ((long)(limbSwing     * 50f * speedScale)) % durationMs;
		long prev = ((long)(prevLimbSwing * 50f * speedScale)) % durationMs;

		// Normal case: no wrap-around in this frame
		if (prev <= cur) return prev < triggerTimeMs && cur >= triggerTimeMs;
		// Wrap-around: the cycle looped between prev and cur → target in [0, cur] OR in (prev, duration)
		return triggerTimeMs <= cur || triggerTimeMs > prev;
	}
}
