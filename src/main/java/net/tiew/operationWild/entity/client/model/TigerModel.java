package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.item.ItemDisplayContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.entity.client.animation.CrocodileAnimations;
import net.tiew.operationWild.entity.client.animation.KodiakAnimations;
import net.tiew.operationWild.entity.client.animation.TigerAnimations;
import net.tiew.operationWild.entity.client.model.skin.TigerModelSkins;
import net.tiew.operationWild.entity.client.render.TigerRenderer;
import net.tiew.operationWild.entity.variants.TigerVariant;

public class TigerModel<T extends TigerEntity> extends HierarchicalModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "tiger_default"), "main");

	// Tracks the limbSwing value from the previous frame to detect animation crossings.
	private float prevLimbSwing = 0f;

	private final ModelPart ALL2;
	private final ModelPart ALL;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart left_ear;
	private final ModelPart right_ear;
	private final ModelPart left_eyeBall;
	private final ModelPart right_eyeBall;
	private final ModelPart tail;
	private final ModelPart front_tail;
	private final ModelPart back_tail;
	private final ModelPart left_arm;
	private final ModelPart left_leg;
	private final ModelPart right_leg;
	private final ModelPart right_arm;

	public TigerModel(ModelPart root) {
		this.ALL2 = root.getChild("ALL2");
		this.ALL = this.ALL2.getChild("ALL");
		this.body = this.ALL.getChild("body");
		this.head = this.body.getChild("head");
		this.left_ear = this.head.getChild("left_ear");
		this.right_ear = this.head.getChild("right_ear");
		this.left_eyeBall = this.head.getChild("left_eyeBall");
		this.right_eyeBall = this.head.getChild("right_eyeBall");
		this.tail = this.body.getChild("tail");
		this.front_tail = this.tail.getChild("front_tail");
		this.back_tail = this.front_tail.getChild("back_tail");
		this.left_arm = this.ALL.getChild("left_arm");
		this.left_leg = this.ALL.getChild("left_leg");
		this.right_leg = this.ALL.getChild("right_leg");
		this.right_arm = this.ALL.getChild("right_arm");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 2.0F));

		PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -2.0F));

		PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -7.0F, -11.0F, 10.0F, 12.0F, 24.0F, new CubeDeformation(0.0F))
				.texOffs(60, 57).addBox(-5.0F, -7.0F, -11.0F, 10.0F, 12.0F, 24.0F, new CubeDeformation(0.25F))
				.texOffs(76, 110).addBox(-4.0F, -7.75F, -6.0F, 8.0F, 1.0F, 9.0F, new CubeDeformation(0.5F))
				.texOffs(0, 56).addBox(5.0F, 5.0F, -11.0F, 0.0F, 2.0F, 24.0F, new CubeDeformation(0.01F))
				.texOffs(0, 56).mirror().addBox(-5.0F, 5.0F, -11.0F, 0.0F, 2.0F, 24.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(0.0F, 1.0F, 1.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 36).addBox(-5.0F, -4.0F, -7.0F, 10.0F, 8.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(57, 66).addBox(-5.0F, -4.0F, -1.2F, 10.0F, 8.0F, 1.0F, new CubeDeformation(0.25F))
				.texOffs(28, 87).addBox(-7.0F, -6.0F, -0.7F, 14.0F, 12.0F, 0.0F, new CubeDeformation(0.05F))
				.texOffs(124, 122).mirror().addBox(3.5F, 4.0F, -9.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false)
				.texOffs(124, 122).addBox(-5.5F, 4.0F, -9.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.01F))
				.texOffs(109, 126).addBox(-3.5F, 4.0F, -9.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(82, 42).addBox(4.0F, -4.0F, -4.0F, 3.0F, 9.0F, 0.0F, new CubeDeformation(0.01F))
				.texOffs(82, 42).mirror().addBox(-7.0F, -4.0F, -4.0F, 3.0F, 9.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false)
				.texOffs(68, 13).addBox(-2.5F, 0.0F, -10.0F, 5.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, -11.0F));

		PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(48, 86).mirror().addBox(0.0F, -12.5F, 0.0F, 0.0F, 12.0F, 17.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(5.5F, 5.5F, -9.0F, 0.0F, 0.1309F, -0.1745F));

		PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(48, 86).addBox(0.0F, -12.5F, 0.0F, 0.0F, 12.0F, 17.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-5.5F, 5.5F, -9.0F, 0.0F, -0.1309F, 0.1745F));

		PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(18, 51).addBox(-1.0F, -2.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -3.5F, -4.5F));

		PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(26, 51).addBox(-2.0F, -2.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -3.5F, -4.5F));

		PartDefinition left_eyeBall = head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(0, 54).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.025F)), PartPose.offset(2.0F, -1.5F, -7.0F));

		PartDefinition right_eyeBall = head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(0, 54).mirror().addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.025F)).mirror(false), PartPose.offset(-2.0F, -1.5F, -7.0F));

		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.5F, -3.5F, 13.0F));

		PartDefinition front_tail = tail.addOrReplaceChild("front_tail", CubeListBuilder.create().texOffs(18, 58).addBox(-2.0F, -1.5F, 0.0F, 3.0F, 3.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition back_tail = front_tail.addOrReplaceChild("back_tail", CubeListBuilder.create().texOffs(68, 0).addBox(-2.0F, -1.5F, 0.0F, 3.0F, 3.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 10.0F));

		PartDefinition left_arm = ALL.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(34, 36).addBox(-2.5F, -1.0F, -2.5F, 4.0F, 17.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(4.5F, 0.0F, -5.5F));

		PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(52, 36).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, 3.0F, 10.5F));

		PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(52, 36).mirror().addBox(-2.5F, -1.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.5F, 3.0F, 10.5F));

		PartDefinition right_arm = ALL.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(34, 36).mirror().addBox(-1.5F, -1.0F, -2.5F, 4.0F, 17.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-4.5F, 0.0F, -5.5F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(TigerEntity tiger, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		if (tiger.isBaby()) {
			float maturationPercent = (float) tiger.getMaturationPercentage() / 100f;
			float headScale = 1.6f - (1.6f - 1.0f) * maturationPercent;

			this.head.xScale *= headScale;
			this.head.yScale *= headScale;
			this.head.zScale *= headScale;
		}
		this.applyHeadRotation(netHeadYaw, headPitch);

		if (!tiger.isGrabbing()) {
			if (tiger.isCombo(1)) {
				this.animate(tiger.attack1Combo, TigerAnimations.ATTACK_STRIKE, ageInTicks, 0.925f * OWEntity.comboSpeedMultiplier);
				captureBodyState(tiger, 9f, this.ALL2, this.ALL, this.body);
				return;
			}
			if (tiger.isCombo(2)) {
				this.animate(tiger.attack2Combo, TigerAnimations.ATTACK_STRIKE_2, ageInTicks, 1.05f * OWEntity.comboSpeedMultiplier);
				captureBodyState(tiger, 9f, this.ALL2, this.ALL, this.body);
				return;
			}
			if (tiger.isCombo(3)) {
				this.animate(tiger.attack3Combo, TigerAnimations.ATTACK_STRIKE_3, ageInTicks, 1.15f * OWEntity.comboSpeedMultiplier);
				captureBodyState(tiger, 9f, this.ALL2, this.ALL, this.body);
				return;
			}
		}


		if (tiger.isMad() && !tiger.isEating()) {
			this.left_eyeBall.xScale = 0;
			this.left_eyeBall.yScale = 0;
			this.left_eyeBall.zScale = 0;

			this.right_eyeBall.xScale = 0;
			this.right_eyeBall.yScale = 0;
			this.right_eyeBall.zScale = 0;
		}

		/*if (tiger.transitionIdleSit.isStarted()) {
			this.animate(tiger.transitionIdleSit, TigerAnimations.TRANSITION_IDLE_SIT, ageInTicks, 1.0f);
			return;
		}

		if (tiger.transitionSitIdle.isStarted()) {
			this.animate(tiger.transitionSitIdle, TigerAnimations.TRANSITION_SIT_IDLE, ageInTicks, 1.0f);
			return;
		}*/

		if (tiger.scarifyAnimationState.isStarted()) {
			this.animate(tiger.scarifyAnimationState, TigerAnimations.SCARIFY, ageInTicks, 1.0f);
		}

		if (tiger.scratchesAnimationState.isStarted()) {
			this.animate(tiger.scratchesAnimationState, TigerAnimations.MISC_IDLE_2, ageInTicks, 1.0f);
		}

		if (tiger.transitionIdleSleep.isStarted()) {
			this.animate(tiger.transitionIdleSleep, TigerAnimations.TRANSITION_IDLE_NAP, ageInTicks, 1.0f);
			captureBodyState(tiger, 9f, this.ALL2, this.ALL, this.body);
			return;
		}

		if (tiger.transitionSleepIdle.isStarted()) {
			this.animate(tiger.transitionSleepIdle, TigerAnimations.TRANSITION_NAP_IDLE, ageInTicks, 1.0f);
			captureBodyState(tiger, 9f, this.ALL2, this.ALL, this.body);
			return;
		}

		if (tiger.transitionSitIdle.isStarted()) {
			this.animate(tiger.transitionSitIdle, TigerAnimations.TRANSITION_SIT_IDLE, ageInTicks, 1.0f);
			captureBodyState(tiger, 9f, this.ALL2, this.ALL, this.body);
			return;
		}

		if (tiger.transitionIdleSit.isStarted()) {
			this.animate(tiger.transitionIdleSit, TigerAnimations.TRANSITION_IDLE_SIT, ageInTicks, 1.0f);
			captureBodyState(tiger, 9f, this.ALL2, this.ALL, this.body);
			return;
		}

		if (tiger.isNapping()) {
			this.animate(tiger.napAnimationState, TigerAnimations.NAP, ageInTicks, 1.0f);
			captureBodyState(tiger, 9f, this.ALL2, this.ALL, this.body);
			return;
		}

		if (tiger.isRoaring()) {
			this.animate(tiger.roaringAnimationState, TigerAnimations.ROAR, ageInTicks, 1.0f);
			captureBodyState(tiger, 9f, this.ALL2, this.ALL, this.body);
			return;
		}

		if (tiger.isPreparing && !tiger.isGrabbing()) {
			this.animate(tiger.preparingLeapAnimationState, TigerAnimations.PREPARING_LEAP, ageInTicks, 1.0f);
			this.animate(tiger.idleAnimationState, TigerAnimations.MISC_IDLE, ageInTicks, 1.0f);
			captureBodyState(tiger, 9f, this.ALL2, this.ALL, this.body);
			return;
		}

		if (tiger.isGrabbing()) {
			this.animate(tiger.preparingLeapAnimationState, TigerAnimations.PREPARING_LEAP, ageInTicks, 1.0f);
			this.animate(tiger.idleAnimationState, TigerAnimations.MISC_IDLE, ageInTicks, 1.0f);
		}

		if (tiger.isGrabbing() || tiger.isEating()) {
			boolean isRiddenPlayerGrab = tiger.isTame() && tiger.isVehicle() && tiger.isGrabbing();
			if (!isRiddenPlayerGrab || tiger.getGrabPunchTimer() > 0) {
				float chewSpeed = 0.75f;
				float chewAmplitude = 0.5f;

				float chew = Mth.sin(ageInTicks * chewSpeed) * chewAmplitude;
				float tear = Mth.sin(ageInTicks * chewSpeed * 0.7f + 1.2f) * 0.08f;

				this.head.xRot += chew;
				this.head.yRot += tear;
			}
		}

		if (tiger.isEating()) {
			float dropAmount = 3.5f;

			this.body.y += dropAmount;
			this.head.y += dropAmount;
		}

		if (tiger.isLeaping) {
			this.animate(tiger.leapAnimationState, TigerAnimations.LEAP, ageInTicks, 1.0f);
			captureBodyState(tiger, 9f, this.ALL2, this.ALL, this.body);
			return;
		}


		if (tiger.isSitting()) {
			this.animate(tiger.sittingAnimationState, TigerAnimations.SIT, ageInTicks, 1.0f);
			return;
		}


		this.animate(tiger.idleAnimationState, TigerAnimations.MISC_IDLE, ageInTicks, 1.0f);

		if (tiger.isRunning() || tiger.getState() == 2) {
			this.animateWalk(TigerAnimations.MOVE_RUN, limbSwing, limbSwingAmount, 1.1f, 1.25f);

			if (walkAnimCrossed(TigerAnimations.MOVE_RUN, limbSwing, 1.1f, 150L)) tiger.onRightFootDown();
			if (walkAnimCrossed(TigerAnimations.MOVE_RUN, limbSwing, 1.1f, 340L)) tiger.onLeftFootDown();

		} else {
			this.animateWalk(TigerAnimations.MOVE_WALK, limbSwing, limbSwingAmount, 4.5f, 4.5f);

			if (walkAnimCrossed(TigerAnimations.MOVE_WALK, limbSwing, 4.5f, 300L)) tiger.onRightFootDown();
			if (walkAnimCrossed(TigerAnimations.MOVE_WALK, limbSwing, 4.5f, 1000L)) tiger.onLeftFootDown();
		}

		// Must be updated AFTER the event checks above, so they see the previous frame's value.
		this.prevLimbSwing = limbSwing;

		captureBodyState(tiger, 9f, this.ALL2, this.ALL, this.body);
	}

	/**
	 * Captures the animated bone-chain Y delta into {@code tiger.bodyAnimY} so that
	 * {@code positionRider()} (game thread) can read it without re-running setupAnim.
	 * Must be called at every exit point of setupAnim, including early returns.
	 *
	 * @param tiger        the entity whose fields are written
	 * @param restPoseYSum sum of the <em>rest-pose</em> Y offsets for every bone in the chain
	 *                     (e.g. ALL2=8, ALL=0, body=1 → pass 9f)
	 * @param boneChain    the bones from root down to the seat bone, in order
	 *                     (e.g. {@code this.ALL2, this.ALL, this.body})
	 */
	private void captureBodyState(TigerEntity tiger, float restPoseYSum, ModelPart... boneChain) {
		if (!tiger.level().isClientSide()) return;
		tiger.setBodyZRot((float) Math.toDegrees(this.ALL.zRot));
		tiger.setBodyXRot((float) -Math.toDegrees(this.ALL.xRot));
		// Sum current Y positions of every bone in the chain, then subtract the rest-pose sum
		// to get the pure animated delta. Negative = bone visually UP (Blockbench Y inverted).
		float ySum = 0f;
		for (ModelPart bone : boneChain) ySum += bone.y;
		tiger.bodyAnimY = ySum - (restPoseYSum * tiger.getScale());
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

	/**
	 * Returns {@code true} on every frame a named (non-looping) animation is within
	 * {@code toleranceMs} milliseconds of {@code triggerTimeMs}.
	 *
	 * <p>A tolerance of 25 ms ensures the event fires exactly once at ~60 fps.
	 * Use a wider tolerance if the animation is sped up and you risk missing the window.</p>
	 *
	 * @param animState     the AnimationState of the animation
	 * @param triggerTimeMs keyframe time in <strong>milliseconds</strong> to fire at
	 * @param toleranceMs   half-window size in ms (25 is a safe default for 60 fps)
	 */
	private boolean namedAnimAt(AnimationState animState, long triggerTimeMs, long toleranceMs) {
		if (!animState.isStarted()) return false;
		long elapsed = animState.getAccumulatedTime();
		return Math.abs(elapsed - triggerTimeMs) <= toleranceMs;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		int alpha = (int)(TigerRenderer.currentAlpha * 255.0f);
		int transparentColor = (color & 0x00FFFFFF) | (alpha << 24);
		this.ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, transparentColor);
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
