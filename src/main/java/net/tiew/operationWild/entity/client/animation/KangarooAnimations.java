package net.tiew.operationWild.entity.client.animation;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Animations du Kangaroo (export Blockbench 5.1.4).
 * Certains canaux de la SIT référencent des os renommés (head_neck, body_2, tail_1...) :
 * ils sont silencieusement ignorés par {@code KeyframeAnimations.animate} (os introuvable).
 */
public class KangarooAnimations {
	public static final AnimationDefinition MISC_IDLE = AnimationDefinition.Builder.withLength(4.0F).looping()
			.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 2.5F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(2.5F, 0.0F, -2.5F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 2.5F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("body2", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.scaleVec(1.019F, 1.048F, 1.019F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0F, KeyframeAnimations.scaleVec(1.019F, 1.048F, 1.019F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.5417F, KeyframeAnimations.scaleVec(1.006F, 1.014F, 1.006F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.scaleVec(1.0F, 0.9F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0F, KeyframeAnimations.scaleVec(1.0F, 0.9F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.25F, KeyframeAnimations.degreeVec(2.32F, -8.05F, -2.41F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(5.0F, -9.51F, -2.85F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.2083F, KeyframeAnimations.degreeVec(-0.87F, -1.72F, -0.51F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-70.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(-69.99F, 0.22F, -14.75F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.degreeVec(-70.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(78.06F, 10.43F, -31.09F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0833F, KeyframeAnimations.degreeVec(79.14F, 9.34F, -31.16F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.125F, KeyframeAnimations.degreeVec(79.04F, 10.15F, -31.15F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.1667F, KeyframeAnimations.degreeVec(78.73F, 11.13F, -30.92F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2083F, KeyframeAnimations.degreeVec(78.51F, 11.21F, -30.35F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.1667F, KeyframeAnimations.degreeVec(91.31F, 0.57F, -0.12F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.1667F, KeyframeAnimations.degreeVec(78.07F, -9.85F, 30.89F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.1667F, KeyframeAnimations.degreeVec(91.65F, 0.13F, 0.09F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.degreeVec(78.06F, 10.43F, -31.09F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.scaleVec(1.0F, 0.9F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0F, KeyframeAnimations.scaleVec(1.0F, 0.9F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0417F, KeyframeAnimations.degreeVec(1.22F, -25.0F, 0.63F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.degreeVec(4.07F, -25.17F, 3.01F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7083F, KeyframeAnimations.degreeVec(4.17F, -28.29F, 3.14F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.75F, KeyframeAnimations.degreeVec(4.55F, -35.08F, 3.45F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7917F, KeyframeAnimations.degreeVec(4.94F, -35.21F, 3.72F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.875F, KeyframeAnimations.degreeVec(5.64F, -21.25F, 4.17F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.9583F, KeyframeAnimations.degreeVec(6.34F, -11.61F, 4.58F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.125F, KeyframeAnimations.degreeVec(7.66F, -23.34F, 5.62F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.1667F, KeyframeAnimations.degreeVec(8.14F, -24.32F, 5.8F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(13.81F, -24.64F, 8.28F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.7083F, KeyframeAnimations.degreeVec(9.74F, -24.7F, 6.65F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.2083F, KeyframeAnimations.degreeVec(4.55F, -24.85F, 3.54F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.2917F, KeyframeAnimations.degreeVec(5.66F, -24.87F, 3.13F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.3333F, KeyframeAnimations.degreeVec(8.39F, -24.88F, 2.92F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.375F, KeyframeAnimations.degreeVec(6.05F, -24.89F, 2.66F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.4583F, KeyframeAnimations.degreeVec(-3.12F, -24.9F, 2.21F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.5417F, KeyframeAnimations.degreeVec(1.22F, -24.92F, 1.97F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, -25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(14.32F, 24.61F, -9.13F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.6667F, KeyframeAnimations.degreeVec(10.26F, 35.96F, -7.17F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.875F, KeyframeAnimations.degreeVec(8.33F, 11.25F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0833F, KeyframeAnimations.degreeVec(6.61F, 24.65F, -5.09F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.scaleVec(1.0F, 0.9F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0F, KeyframeAnimations.scaleVec(1.0F, 0.9F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.3323F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.3333F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.499F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.5F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.6657F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.6667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.8323F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.8333F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.999F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(4.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("right_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.3323F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.3333F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.499F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.5F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.6657F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.6667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.8323F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.8333F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.999F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(4.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.build();

	public static final AnimationDefinition MOVE_WALK = AnimationDefinition.Builder.withLength(1.0F).looping()
			.addAnimation("body2", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.1111F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3333F, KeyframeAnimations.posVec(0.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7778F, KeyframeAnimations.posVec(0.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(32.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(62.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(32.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-29.61F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(-44.96F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-29.93F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.9583F, KeyframeAnimations.degreeVec(-23.53F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.125F, KeyframeAnimations.degreeVec(-10.46F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3333F, KeyframeAnimations.degreeVec(17.1F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(17.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-12.19F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.75F, KeyframeAnimations.degreeVec(-16.33F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7778F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.875F, KeyframeAnimations.degreeVec(-16.01F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-12.51F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-37.75F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.375F, KeyframeAnimations.degreeVec(-6.04F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5833F, KeyframeAnimations.degreeVec(10.57F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.625F, KeyframeAnimations.degreeVec(12.67F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.degreeVec(12.83F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.75F, KeyframeAnimations.degreeVec(4.48F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-2.82F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8333F, KeyframeAnimations.degreeVec(-10.36F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.875F, KeyframeAnimations.degreeVec(-17.33F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2778F, KeyframeAnimations.degreeVec(24.7555F, -5.1243F, -9.0873F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6111F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.1667F, KeyframeAnimations.posVec(2.0F, 7.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6111F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3889F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8333F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2778F, KeyframeAnimations.posVec(0.0F, 15.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.posVec(0.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3889F, KeyframeAnimations.degreeVec(52.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg2", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3889F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5556F, KeyframeAnimations.posVec(0.0F, 2.78F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8333F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.1667F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3889F, KeyframeAnimations.degreeVec(74.17F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(42.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8333F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg3", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2778F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 2.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.posVec(0.0F, -1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8333F, KeyframeAnimations.posVec(0.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-25.0F, 15.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0417F, KeyframeAnimations.degreeVec(-30.76F, 15.0F, -0.31F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0833F, KeyframeAnimations.degreeVec(-28.96F, 15.0F, 1.81F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-24.01F, 15.0F, 2.41F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-24.99F, 15.0F, 1.51F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.375F, KeyframeAnimations.degreeVec(-29.96F, 15.0F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(-31.85F, 15.0F, 1.76F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-28.69F, 15.0F, 2.18F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-22.98F, 15.0F, 2.79F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(-25.0F, 15.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-25.0F, -15.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0417F, KeyframeAnimations.degreeVec(-30.76F, -15.0F, 0.31F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0833F, KeyframeAnimations.degreeVec(-28.96F, -15.0F, -1.81F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-24.01F, -15.0F, -2.41F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-24.99F, -15.0F, -1.51F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.375F, KeyframeAnimations.degreeVec(-29.96F, -15.0F, -1.5F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(-31.85F, -15.0F, -1.76F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-28.69F, -15.0F, -2.18F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-22.98F, -15.0F, -2.79F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(-25.0F, -15.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2778F, KeyframeAnimations.degreeVec(24.7555F, 5.1243F, 9.0873F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6111F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.1667F, KeyframeAnimations.posVec(-2.0F, 7.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6111F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3889F, KeyframeAnimations.degreeVec(52.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg2", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3889F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5556F, KeyframeAnimations.posVec(0.0F, 2.78F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8333F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.1667F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3889F, KeyframeAnimations.degreeVec(74.17F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(42.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8333F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg3", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2778F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 2.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.posVec(0.0F, -1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8333F, KeyframeAnimations.posVec(0.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.build();

	public static final AnimationDefinition SIT = AnimationDefinition.Builder.withLength(4.0268F).looping()
		.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.0134F, KeyframeAnimations.posVec(0.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(4.0268F, KeyframeAnimations.posVec(0.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.0134F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(4.0268F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-45.16F, -7.05F, 2.58F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.6779F, KeyframeAnimations.degreeVec(-45.11F, 6.99F, -2.56F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.3557F, KeyframeAnimations.degreeVec(-45.14F, -6.88F, 2.52F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(4.0268F, KeyframeAnimations.degreeVec(-45.16F, -7.05F, 2.58F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(46.14F, -20.98F, -11.14F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4167F, KeyframeAnimations.degreeVec(46.24F, -21.19F, -11.29F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5833F, KeyframeAnimations.degreeVec(46.19F, -21.22F, -11.3F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.625F, KeyframeAnimations.degreeVec(46.18F, -21.06F, -11.21F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6711F, KeyframeAnimations.degreeVec(46.17F, -20.7F, -11.01F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.349F, KeyframeAnimations.degreeVec(46.2F, 20.78F, 11.06F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(4.0268F, KeyframeAnimations.degreeVec(46.14F, -20.98F, -11.14F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-90.0F, -25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.0134F, KeyframeAnimations.degreeVec(-90.0F, -27.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(4.0268F, KeyframeAnimations.degreeVec(-90.0F, -25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -11.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(4.0268F, KeyframeAnimations.posVec(0.0F, -11.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_leg2", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(4.0268F, KeyframeAnimations.posVec(0.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.0134F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(4.0268F, KeyframeAnimations.degreeVec(40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-34.0724F, 8.5373F, 12.3796F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.0134F, KeyframeAnimations.degreeVec(-34.589F, 5.7162F, 8.2189F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(4.0268F, KeyframeAnimations.degreeVec(-34.0724F, 8.5373F, 12.3796F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.0134F, KeyframeAnimations.degreeVec(7.45F, 0.0F, -0.16F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.0417F, KeyframeAnimations.degreeVec(3.89F, 0.1F, -0.21F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.0833F, KeyframeAnimations.degreeVec(3.82F, -0.53F, -0.21F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.1667F, KeyframeAnimations.degreeVec(3.52F, -8.54F, -0.22F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.3333F, KeyframeAnimations.degreeVec(2.68F, 10.52F, -0.3F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.5F, KeyframeAnimations.degreeVec(1.93F, 0.14F, -0.21F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(4.0268F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.1667F, KeyframeAnimations.degreeVec(5.46F, -0.65F, -0.07F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2083F, KeyframeAnimations.degreeVec(5.35F, 2.1F, -0.09F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.25F, KeyframeAnimations.degreeVec(5.54F, 9.5F, -0.09F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2917F, KeyframeAnimations.degreeVec(5.73F, 9.82F, -0.09F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.375F, KeyframeAnimations.degreeVec(6.1F, -3.49F, -0.1F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.4583F, KeyframeAnimations.degreeVec(6.45F, -12.29F, -0.12F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.5417F, KeyframeAnimations.degreeVec(6.74F, -8.18F, -0.13F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.5833F, KeyframeAnimations.degreeVec(6.89F, -4.15F, -0.13F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.6667F, KeyframeAnimations.degreeVec(7.14F, -0.21F, -0.04F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.0134F, KeyframeAnimations.degreeVec(7.42F, 0.15F, -0.07F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.7917F, KeyframeAnimations.degreeVec(5.84F, -0.01F, -0.13F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(4.0268F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-90.0F, 25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.0134F, KeyframeAnimations.degreeVec(-90.0F, 27.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(4.0268F, KeyframeAnimations.degreeVec(-90.0F, 25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(4.0268F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_leg2", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(4.0268F, KeyframeAnimations.posVec(0.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.0134F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(4.0268F, KeyframeAnimations.degreeVec(40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-34.0724F, -8.5373F, -12.3796F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.0134F, KeyframeAnimations.degreeVec(-34.589F, -5.7162F, -8.2189F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(4.0268F, KeyframeAnimations.degreeVec(-34.0724F, -8.5373F, -12.3796F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(3.1879F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(3.3547F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(3.3557F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(3.5225F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(3.5235F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(3.6903F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(3.6913F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(3.8581F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(3.8591F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(4.0268F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("right_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(3.1879F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(3.3547F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(3.3557F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(3.5225F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(3.5235F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(3.6903F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(3.6913F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(3.8581F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(3.8591F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(4.0268F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("body2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.1667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 2.31F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(3.0833F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -2.44F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(4.0268F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("body2", new AnimationChannel(AnimationChannel.Targets.SCALE, 
			new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2917F, KeyframeAnimations.scaleVec(1.018F, 1.037F, 1.018F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(2.9583F, KeyframeAnimations.scaleVec(1.001F, 1.002F, 1.001F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(4.0268F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.build();

	public static final AnimationDefinition TRANSITION_IDLE_SIT = AnimationDefinition.Builder.withLength(1.0F)
		.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(2.5F, 0.11F, 2.5F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2083F, KeyframeAnimations.posVec(0.0F, 0.17F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3333F, KeyframeAnimations.posVec(0.0F, 0.65F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.posVec(0.0F, 1.64F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7083F, KeyframeAnimations.posVec(0.0F, 3.56F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("body2", new AnimationChannel(AnimationChannel.Targets.SCALE, 
			new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3333F, KeyframeAnimations.degreeVec(5.28F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.625F, KeyframeAnimations.degreeVec(8.33F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.SCALE, 
			new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-3.33F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4167F, KeyframeAnimations.degreeVec(1.81F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5F, KeyframeAnimations.degreeVec(11.72F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5833F, KeyframeAnimations.degreeVec(21.73F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.625F, KeyframeAnimations.degreeVec(23.05F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6667F, KeyframeAnimations.degreeVec(21.91F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7083F, KeyframeAnimations.degreeVec(17.66F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8333F, KeyframeAnimations.degreeVec(2.93F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-85.57F, -1.43F, 13.82F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-55.8F, -5.08F, 5.25F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(-45.16F, -7.05F, 2.58F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(78.06F, 10.44F, -31.09F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.25F, KeyframeAnimations.degreeVec(79.34F, 8.86F, -31.19F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.375F, KeyframeAnimations.degreeVec(77.99F, 6.03F, -31.39F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5F, KeyframeAnimations.degreeVec(74.96F, 6.76F, -31.33F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.625F, KeyframeAnimations.degreeVec(74.66F, 14.34F, -30.84F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7917F, KeyframeAnimations.degreeVec(78.02F, 13.32F, -30.91F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.875F, KeyframeAnimations.degreeVec(77.48F, 9.56F, -31.15F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(78.06F, 10.43F, -31.09F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2083F, KeyframeAnimations.degreeVec(-4.11F, -1.14F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-17.87F, -4.96F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-33.23F, -9.23F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-42.64F, -11.84F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5417F, KeyframeAnimations.degreeVec(-60.22F, -16.73F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-80.28F, -22.3F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-88.61F, -24.61F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(-90.0F, -25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.1667F, KeyframeAnimations.posVec(0.0F, 0.17F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 0.68F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4167F, KeyframeAnimations.posVec(0.0F, 2.76F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 2.78F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7083F, KeyframeAnimations.posVec(0.0F, -0.34F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.75F, KeyframeAnimations.posVec(0.0F, -0.66F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7917F, KeyframeAnimations.posVec(0.0F, -0.86F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2083F, KeyframeAnimations.posVec(0.0F, -0.41F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3333F, KeyframeAnimations.posVec(0.0F, -1.8F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.posVec(0.0F, -4.47F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5833F, KeyframeAnimations.posVec(0.0F, -7.56F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7083F, KeyframeAnimations.posVec(0.0F, -9.79F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, -11.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_leg2", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.75F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.25F, KeyframeAnimations.degreeVec(-0.05F, 0.01F, 0.02F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-3.87F, 0.97F, 1.41F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.625F, KeyframeAnimations.degreeVec(-19.25F, 4.82F, 6.99F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-26.24F, 6.57F, 9.53F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.875F, KeyframeAnimations.degreeVec(-33.59F, 8.42F, 12.2F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(-34.07F, 8.54F, 12.38F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.SCALE, 
			new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.25F, KeyframeAnimations.degreeVec(-0.4F, -19.92F, 0.15F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-2.87F, -13.63F, 0.71F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5417F, KeyframeAnimations.degreeVec(-0.02F, -11.2F, -0.03F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.625F, KeyframeAnimations.degreeVec(5.86F, -8.64F, -1.06F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7083F, KeyframeAnimations.degreeVec(4.2F, -6.23F, -0.48F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.75F, KeyframeAnimations.degreeVec(0.3F, -5.08F, 0.05F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8333F, KeyframeAnimations.degreeVec(-2.95F, -3.02F, 0.14F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.25F, KeyframeAnimations.degreeVec(-0.4F, 19.92F, -0.17F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-2.85F, 13.63F, -0.8F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5417F, KeyframeAnimations.degreeVec(-0.03F, 11.2F, 0.06F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.625F, KeyframeAnimations.degreeVec(5.86F, 8.64F, 1.15F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7083F, KeyframeAnimations.degreeVec(4.21F, 6.23F, 0.48F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.75F, KeyframeAnimations.degreeVec(0.3F, 5.08F, -0.06F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8333F, KeyframeAnimations.degreeVec(-2.95F, 3.02F, -0.19F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2083F, KeyframeAnimations.degreeVec(-4.11F, 1.14F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-17.87F, 4.96F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-33.23F, 9.23F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-42.64F, 11.84F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5417F, KeyframeAnimations.degreeVec(-60.22F, 16.73F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-80.28F, 22.3F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-88.61F, 24.61F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(-90.0F, 25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.1667F, KeyframeAnimations.posVec(0.0F, 0.17F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 0.66F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4167F, KeyframeAnimations.posVec(0.0F, 2.69F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 2.87F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7083F, KeyframeAnimations.posVec(0.0F, 0.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.75F, KeyframeAnimations.posVec(0.0F, 0.26F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7917F, KeyframeAnimations.posVec(0.0F, 0.11F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_leg2", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.75F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.04F, 0.01F, 0.02F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-3.25F, -0.81F, -1.18F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-14.65F, -3.67F, -5.32F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-25.67F, -6.43F, -9.33F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.875F, KeyframeAnimations.degreeVec(-33.55F, -8.41F, -12.19F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(-34.07F, -8.54F, -12.38F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.SCALE, 
			new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.build();

	public static final AnimationDefinition TRANSITION_SIT_IDLE = AnimationDefinition.Builder.withLength(1.25F)
		.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(2.62F, 0.11F, 2.63F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.25F, KeyframeAnimations.degreeVec(2.5F, 0.11F, 2.5F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.375F, KeyframeAnimations.posVec(0.0F, 3.37F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.75F, KeyframeAnimations.posVec(0.0F, 0.42F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("body2", new AnimationChannel(AnimationChannel.Targets.SCALE, 
			new Keyframe(1.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4167F, KeyframeAnimations.degreeVec(9.44F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(4.93F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.25F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.SCALE, 
			new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.25F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-0.34F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(-5.01F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.25F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-45.16F, -7.05F, 2.58F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.25F, KeyframeAnimations.degreeVec(-46.47F, -6.89F, 2.71F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-52.34F, -5.96F, 4.06F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-65.35F, -3.72F, 7.86F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-77.19F, -2.12F, 11.66F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-84.31F, -1.7F, 13.75F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(-90.02F, -0.75F, 15.02F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.25F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.25F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(78.06F, 10.43F, -31.09F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.625F, KeyframeAnimations.degreeVec(76.53F, 11.8F, -31.01F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7917F, KeyframeAnimations.degreeVec(78.28F, 9.51F, -31.15F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9167F, KeyframeAnimations.degreeVec(79.22F, 6.05F, -31.38F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(77.89F, 9.02F, -31.18F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.125F, KeyframeAnimations.degreeVec(77.7F, 11.37F, -31.04F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.25F, KeyframeAnimations.degreeVec(78.06F, 10.43F, -31.09F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-90.0F, -25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.375F, KeyframeAnimations.degreeVec(-72.73F, -20.2F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-8.39F, -2.33F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.75F, KeyframeAnimations.degreeVec(-3.29F, -0.91F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-0.5F, -0.14F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8333F, KeyframeAnimations.degreeVec(-0.04F, -0.01F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2083F, KeyframeAnimations.posVec(0.0F, -0.71F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, -0.46F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2917F, KeyframeAnimations.posVec(0.0F, -0.08F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3333F, KeyframeAnimations.posVec(0.0F, 0.44F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.375F, KeyframeAnimations.posVec(0.0F, 1.09F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4167F, KeyframeAnimations.posVec(0.0F, 1.77F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 2.85F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5833F, KeyframeAnimations.posVec(0.0F, 2.68F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.75F, KeyframeAnimations.posVec(0.0F, 0.31F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7917F, KeyframeAnimations.posVec(0.0F, 0.02F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -11.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, -10.52F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.375F, KeyframeAnimations.posVec(0.0F, -9.28F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.posVec(0.0F, -7.81F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, -6.89F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5417F, KeyframeAnimations.posVec(0.0F, -5.91F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5833F, KeyframeAnimations.posVec(0.0F, -4.85F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.625F, KeyframeAnimations.posVec(0.0F, -3.8F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7083F, KeyframeAnimations.posVec(0.0F, -1.89F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8333F, KeyframeAnimations.posVec(0.0F, -0.1F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_leg2", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.75F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-34.07F, 8.54F, 12.38F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-28.91F, 7.24F, 10.5F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8333F, KeyframeAnimations.degreeVec(-2.72F, 0.68F, 0.99F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.53F, -0.13F, -0.19F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.SCALE, 
			new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.25F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9583F, KeyframeAnimations.degreeVec(-0.11F, -24.48F, 0.37F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.03F, -24.88F, 0.26F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0417F, KeyframeAnimations.degreeVec(0.0F, -24.99F, 0.17F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0833F, KeyframeAnimations.degreeVec(-0.07F, -24.99F, 0.09F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.25F, KeyframeAnimations.degreeVec(0.0F, -25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9583F, KeyframeAnimations.degreeVec(-0.35F, 24.48F, 0.15F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(-0.17F, 24.88F, 0.17F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0417F, KeyframeAnimations.degreeVec(-0.12F, 24.99F, 0.09F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0833F, KeyframeAnimations.degreeVec(-0.12F, 24.99F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.25F, KeyframeAnimations.degreeVec(0.0F, 25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-90.0F, 25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.375F, KeyframeAnimations.degreeVec(-72.73F, 20.2F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-8.39F, 2.33F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.75F, KeyframeAnimations.degreeVec(-3.29F, 0.91F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-0.5F, 0.14F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8333F, KeyframeAnimations.degreeVec(-0.04F, 0.01F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2083F, KeyframeAnimations.posVec(0.0F, 0.22F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2917F, KeyframeAnimations.posVec(0.0F, 0.72F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.posVec(0.0F, 2.62F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 2.95F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5417F, KeyframeAnimations.posVec(0.0F, 2.91F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5833F, KeyframeAnimations.posVec(0.0F, 2.62F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.625F, KeyframeAnimations.posVec(0.0F, 2.05F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6667F, KeyframeAnimations.posVec(0.0F, 1.39F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.75F, KeyframeAnimations.posVec(0.0F, 0.27F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8333F, KeyframeAnimations.posVec(0.0F, -0.01F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_leg2", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.75F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-34.07F, -8.54F, -12.38F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-33.21F, -8.32F, -12.07F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-29.54F, -7.4F, -10.73F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.625F, KeyframeAnimations.degreeVec(-18.89F, -4.73F, -6.86F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.75F, KeyframeAnimations.degreeVec(-8.96F, -2.24F, -3.25F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.8333F, KeyframeAnimations.degreeVec(-3.14F, -0.79F, -1.14F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.5F, 0.12F, 0.18F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.SCALE, 
			new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.25F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.build();

	public static final AnimationDefinition ATTACK_STRIKE = AnimationDefinition.Builder.withLength(1.2917F)
		.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2917F, KeyframeAnimations.degreeVec(2.9547F, -4.5947F, 6.6889F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-7.4717F, -0.6518F, -4.9574F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.1667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2917F, KeyframeAnimations.posVec(0.0F, -1.39F, 0.61F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5833F, KeyframeAnimations.posVec(0.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.1667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("body2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.degreeVec(2.2046F, -12.3071F, -15.2377F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9167F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("body2", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.125F, KeyframeAnimations.posVec(0.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5833F, KeyframeAnimations.posVec(2.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.1667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -15.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-7.8144F, 7.0591F, 12.5192F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.1667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2917F, KeyframeAnimations.posVec(1.0F, -0.33F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.posVec(0.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.1667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.125F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4167F, KeyframeAnimations.degreeVec(7.7835F, 4.4519F, 14.0702F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5833F, KeyframeAnimations.degreeVec(25.3061F, 8.6788F, -7.3978F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.1667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.125F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4167F, KeyframeAnimations.posVec(0.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5833F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.62F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.1667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-40.6369F, 45.8297F, 62.1291F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.degreeVec(6.629F, 44.2638F, 4.2629F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.1667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.1667F, KeyframeAnimations.posVec(2.8F, 5.36F, -12.04F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.375F, KeyframeAnimations.posVec(-1.0F, -1.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9583F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.02F, 0.0F, 0.03F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-25.44F, 4.96F, 0.86F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-55.94F, 9.16F, 3.47F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-63.01F, 9.88F, 1.22F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-67.59F, 8.78F, 7.5F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-68.92F, 3.62F, 4.62F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5F, KeyframeAnimations.degreeVec(-62.81F, -6.15F, -7.25F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5417F, KeyframeAnimations.degreeVec(-42.7F, -15.68F, -24.97F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-26.6F, -19.94F, -33.61F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-27.06F, -19.02F, -23.68F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-17.71F, -16.01F, -22.19F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-11.37F, -9.97F, -12.3F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.1667F, KeyframeAnimations.degreeVec(0.11F, -0.16F, -0.59F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.02F, 0.0F, 0.03F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-25.47F, 4.96F, 0.84F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-56.32F, 9.16F, 3.45F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-64.21F, 9.88F, 1.1F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-65.77F, 8.78F, 7.36F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-64.24F, 3.62F, 4.57F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5F, KeyframeAnimations.degreeVec(-58.59F, -6.15F, -7.47F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5417F, KeyframeAnimations.degreeVec(-43.96F, -15.68F, -26.09F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-29.81F, -19.94F, -35.54F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-26.03F, -19.02F, -23.03F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-18.37F, -16.01F, -22.45F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-11.57F, -9.97F, -12.05F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.1667F, KeyframeAnimations.degreeVec(0.3F, -0.16F, -0.58F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.125F, KeyframeAnimations.degreeVec(-98.7448F, 18.0576F, -26.3921F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-93.7448F, 18.0576F, -26.3921F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.375F, KeyframeAnimations.degreeVec(-85.2563F, -49.199F, -6.3646F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-143.0465F, -86.3907F, 55.2984F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-165.5465F, -86.3907F, 55.2984F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9583F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.125F, KeyframeAnimations.posVec(-5.0F, 4.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2917F, KeyframeAnimations.posVec(-5.0F, 4.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3333F, KeyframeAnimations.posVec(-2.5F, 3.5F, -9.75F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5417F, KeyframeAnimations.posVec(7.0F, 2.0F, -16.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7083F, KeyframeAnimations.posVec(5.0F, 0.0F, -11.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.875F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.SCALE, 
			new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.125F, KeyframeAnimations.scaleVec(1.0F, 0.9784F, 0.9784F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2917F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.625F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.875F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.01F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-0.33F, 1.03F, 0.02F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4167F, KeyframeAnimations.degreeVec(2.96F, -3.1F, -0.05F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.625F, KeyframeAnimations.degreeVec(-4.09F, 2.44F, 0.04F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-3.11F, 2.94F, 0.04F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.875F, KeyframeAnimations.degreeVec(0.89F, -0.81F, -0.01F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2083F, KeyframeAnimations.degreeVec(-0.56F, 1.34F, 0.08F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.degreeVec(3.48F, -2.86F, -0.17F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-1.7F, -0.62F, -0.04F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-5.33F, 3.38F, 0.21F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.75F, KeyframeAnimations.degreeVec(-3.01F, 3.54F, 0.22F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.875F, KeyframeAnimations.degreeVec(2.58F, -1.68F, -0.1F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9583F, KeyframeAnimations.degreeVec(1.86F, -2.3F, -0.14F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0833F, KeyframeAnimations.degreeVec(-0.98F, 0.67F, 0.04F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.build();

	public static final AnimationDefinition ATTACK_STRIKE_2 = AnimationDefinition.Builder.withLength(1.2917F)
		.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2917F, KeyframeAnimations.degreeVec(2.9547F, 4.5947F, -6.6889F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-7.4717F, 0.6518F, 4.9574F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.1667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2917F, KeyframeAnimations.posVec(0.0F, -1.39F, 0.61F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5833F, KeyframeAnimations.posVec(0.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.1667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("body2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.degreeVec(2.2046F, 12.3071F, 15.2377F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9167F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("body2", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.125F, KeyframeAnimations.posVec(0.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5833F, KeyframeAnimations.posVec(-2.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.1667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-7.8144F, -7.0591F, -12.5192F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.1667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2917F, KeyframeAnimations.posVec(-1.0F, -0.33F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.posVec(0.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.1667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.125F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4167F, KeyframeAnimations.degreeVec(7.7835F, -4.4519F, -14.0702F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5833F, KeyframeAnimations.degreeVec(25.3061F, -8.6788F, 7.3978F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.1667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.125F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4167F, KeyframeAnimations.posVec(0.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5833F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.62F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.1667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.125F, KeyframeAnimations.degreeVec(-98.7448F, -18.0576F, 26.3921F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-93.7448F, -18.0576F, 26.3921F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.375F, KeyframeAnimations.degreeVec(-85.2563F, 49.199F, 6.3646F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-143.0465F, 86.3907F, -55.2984F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-165.5465F, 86.3907F, -55.2984F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9583F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.125F, KeyframeAnimations.posVec(5.0F, 4.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2917F, KeyframeAnimations.posVec(5.0F, 4.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3333F, KeyframeAnimations.posVec(2.5F, 3.5F, -9.75F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5417F, KeyframeAnimations.posVec(-7.0F, 2.0F, -16.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7083F, KeyframeAnimations.posVec(-5.0F, 0.0F, -11.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.875F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.SCALE, 
			new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.125F, KeyframeAnimations.scaleVec(1.0F, 0.9784F, 0.9784F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2917F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.625F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.875F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("left_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.02F, 0.0F, -0.03F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-25.47F, -4.96F, -0.84F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-56.32F, -9.16F, -3.45F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-64.21F, -9.88F, -1.1F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-65.77F, -8.78F, -7.36F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-64.24F, -3.62F, -4.57F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5F, KeyframeAnimations.degreeVec(-58.59F, 6.15F, 7.47F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5417F, KeyframeAnimations.degreeVec(-43.96F, 15.68F, 26.09F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-29.81F, 19.94F, 35.54F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-26.03F, 19.02F, 23.03F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-18.37F, 16.01F, 22.45F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-11.57F, 9.97F, 12.05F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.1667F, KeyframeAnimations.degreeVec(0.3F, 0.16F, 0.58F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.02F, 0.0F, -0.03F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-25.44F, -4.96F, -0.86F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-55.94F, -9.16F, -3.47F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-63.01F, -9.88F, -1.22F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-67.59F, -8.78F, -7.5F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-68.92F, -3.62F, -4.62F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5F, KeyframeAnimations.degreeVec(-62.81F, 6.15F, 7.25F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5417F, KeyframeAnimations.degreeVec(-42.7F, 15.68F, 24.97F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-26.6F, 19.94F, 33.61F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-27.06F, 19.02F, 23.68F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-17.71F, 16.01F, 22.19F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-11.37F, 9.97F, 12.3F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.1667F, KeyframeAnimations.degreeVec(0.11F, 0.16F, 0.59F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-40.6369F, -45.8297F, -62.1291F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.degreeVec(6.629F, -44.2638F, -4.2629F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.1667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.POSITION, 
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.1667F, KeyframeAnimations.posVec(-2.8F, 5.36F, -12.04F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.375F, KeyframeAnimations.posVec(1.0F, -1.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9583F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -0.01F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-0.33F, -1.03F, -0.02F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4167F, KeyframeAnimations.degreeVec(2.96F, 3.1F, 0.05F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.625F, KeyframeAnimations.degreeVec(-4.09F, -2.44F, -0.04F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-3.11F, -2.94F, -0.04F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.875F, KeyframeAnimations.degreeVec(0.89F, 0.81F, 0.01F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.2083F, KeyframeAnimations.degreeVec(-0.56F, -1.34F, -0.08F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.4583F, KeyframeAnimations.degreeVec(3.48F, 2.86F, 0.17F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-1.7F, 0.62F, 0.04F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-5.33F, -3.38F, -0.21F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.75F, KeyframeAnimations.degreeVec(-3.01F, -3.54F, -0.22F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.875F, KeyframeAnimations.degreeVec(2.58F, 1.68F, 0.1F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(0.9583F, KeyframeAnimations.degreeVec(1.86F, 2.3F, 0.14F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0833F, KeyframeAnimations.degreeVec(-0.98F, -0.67F, -0.04F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.2917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.build();

	public static final AnimationDefinition ATTACK_STRIKE_3 = AttackStrike3.DEFINITION;

	public static final AnimationDefinition ATTACK_SECONDARY = AttackSecondary.DEFINITION;

	// ──────────────────────────────────────────────────────────────────────────
	//  PILON TELLURIQUE (ultime) — une animation par phase
	//
	//  L'ultime dure ce que dure sa trajectoire : le plongeon s'arrête au sol, pas au
	//  bout d'un compte de trames. Une animation unique de longueur fixe s'éteignait donc
	//  bien avant l'impact et laissait la bête figée en pose de repos pour tout le reste
	//  de l'ultime. Chaque phase a désormais la sienne, jouée sur son propre compteur par
	//  KangarooModel, les deux phases de durée variable étant en boucle.
	//
	//  Le geste est porté par deux exagérations d'échelle, dans l'esprit des coups de poing
	//  du combo (qui gonflent déjà le bras à 1,5) :
	//    • l'os ALL s'écrase à l'ancrage puis s'étire au bond — seuls X et Z bougent
	//      franchement, le siège du cavalier suivant la position et non l'échelle ;
	//    • les SEMELLES enflent tout au long de la chute jusqu'à 2,5× en épaisseur, puis
	//      s'étalent à l'impact. C'est elles qu'on doit lire, pas la bête : le corps
	//      s'enroule au-dessus des pattes au lieu de piquer du nez, pour les laisser seules
	//      en tête de la silhouette.
	// ──────────────────────────────────────────────────────────────────────────

	/** Ancrage : contre-mouvement vers le haut, puis ecrasement sur les pattes, prete a detoner. */
	public static final AnimationDefinition TELLURIC_STOMP_WINDUP = AnimationDefinition.Builder.withLength(0.35F)
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.08F, KeyframeAnimations.posVec(0.0F, 2.5F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.posVec(0.0F, -5.5F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.26F, KeyframeAnimations.posVec(0.0F, -4.2F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.35F, KeyframeAnimations.posVec(0.0F, -4.8F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.08F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(16.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.35F, KeyframeAnimations.degreeVec(13.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.08F, KeyframeAnimations.scaleVec(0.94F, 1.1F, 0.94F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.scaleVec(1.18F, 0.84F, 1.18F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.35F, KeyframeAnimations.scaleVec(1.14F, 0.87F, 1.14F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.35F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("body2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.08F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.35F, KeyframeAnimations.degreeVec(17.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.08F, KeyframeAnimations.degreeVec(-16.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(26.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.35F, KeyframeAnimations.degreeVec(22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.08F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.35F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0417F, KeyframeAnimations.degreeVec(-7.97F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.08F, KeyframeAnimations.degreeVec(-9.15F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.125F, KeyframeAnimations.degreeVec(5.52F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(31.28F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2083F, KeyframeAnimations.degreeVec(38.75F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(40.38F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.35F, KeyframeAnimations.degreeVec(34.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("front_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.35F, KeyframeAnimations.degreeVec(12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.02F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0417F, KeyframeAnimations.degreeVec(4.84F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(14.21F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(20.4F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.35F, KeyframeAnimations.degreeVec(17.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.08F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(-92.0F, 0.0F, -34.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.35F, KeyframeAnimations.degreeVec(-84.0F, 0.0F, -31.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.08F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 6.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(-92.0F, 0.0F, 34.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.35F, KeyframeAnimations.degreeVec(-84.0F, 0.0F, 31.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("left_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0417F, KeyframeAnimations.degreeVec(-13.65F, 0.0F, 0.01F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.08F, KeyframeAnimations.degreeVec(-19.34F, 0.0F, -0.13F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.125F, KeyframeAnimations.degreeVec(-1.38F, 0.0F, -0.21F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(38.61F, 0.0F, 0.11F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2083F, KeyframeAnimations.degreeVec(49.65F, 0.0F, 0.25F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2917F, KeyframeAnimations.degreeVec(37.95F, 0.0F, -0.01F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.35F, KeyframeAnimations.degreeVec(36.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0417F, KeyframeAnimations.degreeVec(-13.65F, 0.0F, -0.01F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.08F, KeyframeAnimations.degreeVec(-19.34F, 0.0F, 0.13F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.125F, KeyframeAnimations.degreeVec(-1.38F, 0.0F, 0.21F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(38.61F, 0.0F, -0.11F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2083F, KeyframeAnimations.degreeVec(49.65F, 0.0F, -0.25F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2917F, KeyframeAnimations.degreeVec(37.95F, 0.0F, 0.01F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.35F, KeyframeAnimations.degreeVec(36.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.08F, KeyframeAnimations.degreeVec(12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(-58.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.35F, KeyframeAnimations.degreeVec(-58.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("left_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.08F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(88.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.35F, KeyframeAnimations.degreeVec(88.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("left_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.08F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(-36.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.35F, KeyframeAnimations.degreeVec(-36.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.08F, KeyframeAnimations.degreeVec(12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(-58.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.35F, KeyframeAnimations.degreeVec(-58.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("right_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.08F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(88.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.35F, KeyframeAnimations.degreeVec(88.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("right_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.08F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(-36.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.35F, KeyframeAnimations.degreeVec(-36.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("left_leg3", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.scaleVec(1.18F, 1.25F, 1.08F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.35F, KeyframeAnimations.scaleVec(1.14F, 1.2F, 1.06F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("right_leg3", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.scaleVec(1.18F, 1.25F, 1.08F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.35F, KeyframeAnimations.scaleVec(1.14F, 1.2F, 1.06F), AnimationChannel.Interpolations.LINEAR)
			))
			.build();

	/** Bond : detente explosive des pattes -- le coup de pied au sol -- etirement, puis repli. */
	public static final AnimationDefinition TELLURIC_STOMP_LEAP = AnimationDefinition.Builder.withLength(0.55F)
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -4.8F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.1F, KeyframeAnimations.posVec(0.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.22F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.38F, KeyframeAnimations.posVec(0.0F, 0.5F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.55F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(13.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.1F, KeyframeAnimations.degreeVec(-26.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.22F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.38F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.55F, KeyframeAnimations.degreeVec(2.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.14F, 0.87F, 1.14F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.1F, KeyframeAnimations.scaleVec(0.82F, 1.26F, 0.82F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.22F, KeyframeAnimations.scaleVec(0.94F, 1.09F, 0.94F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.38F, KeyframeAnimations.scaleVec(1.03F, 0.97F, 1.03F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.55F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.1F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.55F, KeyframeAnimations.degreeVec(4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("body2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(17.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.1F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.22F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.55F, KeyframeAnimations.degreeVec(12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.1F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.22F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.55F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.1F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.3F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.55F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(34.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0833F, KeyframeAnimations.degreeVec(-26.56F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.1F, KeyframeAnimations.degreeVec(-34.81F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.125F, KeyframeAnimations.degreeVec(-32.62F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-19.99F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.24F, KeyframeAnimations.degreeVec(-0.33F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.38F, KeyframeAnimations.degreeVec(-3.35F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.55F, KeyframeAnimations.degreeVec(32.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("front_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.1F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.28F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.55F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(17.02F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0417F, KeyframeAnimations.degreeVec(0.02F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0833F, KeyframeAnimations.degreeVec(-20.07F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.1F, KeyframeAnimations.degreeVec(-25.52F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.125F, KeyframeAnimations.degreeVec(-25.74F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2083F, KeyframeAnimations.degreeVec(-11.95F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.32F, KeyframeAnimations.degreeVec(13.46F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4583F, KeyframeAnimations.degreeVec(16.18F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.55F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-84.0F, 0.0F, -31.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.1F, KeyframeAnimations.degreeVec(-152.0F, 0.0F, -48.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.22F, KeyframeAnimations.degreeVec(-126.0F, 0.0F, -54.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.55F, KeyframeAnimations.degreeVec(-100.0F, 0.0F, -58.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-84.0F, 0.0F, 31.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.1F, KeyframeAnimations.degreeVec(-152.0F, 0.0F, 48.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.22F, KeyframeAnimations.degreeVec(-126.0F, 0.0F, 54.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.55F, KeyframeAnimations.degreeVec(-100.0F, 0.0F, 58.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.1F, KeyframeAnimations.scaleVec(1.22F, 1.22F, 1.22F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.28F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.55F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.1F, KeyframeAnimations.scaleVec(1.22F, 1.22F, 1.22F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.28F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.55F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("left_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(35.81F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0417F, KeyframeAnimations.degreeVec(-7.98F, 0.0F, 0.01F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.1F, KeyframeAnimations.degreeVec(-49.82F, 0.0F, -0.09F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.125F, KeyframeAnimations.degreeVec(-42.49F, 0.0F, -0.25F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-23.44F, 0.0F, -0.27F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2083F, KeyframeAnimations.degreeVec(-8.79F, 0.0F, 0.06F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.26F, KeyframeAnimations.degreeVec(15.18F, 0.0F, 0.16F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2917F, KeyframeAnimations.degreeVec(20.7F, 0.0F, 0.04F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.55F, KeyframeAnimations.degreeVec(38.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(35.81F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0417F, KeyframeAnimations.degreeVec(-7.98F, 0.0F, -0.01F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.1F, KeyframeAnimations.degreeVec(-49.82F, 0.0F, 0.09F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.125F, KeyframeAnimations.degreeVec(-42.49F, 0.0F, 0.25F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-23.44F, 0.0F, 0.27F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2083F, KeyframeAnimations.degreeVec(-8.79F, 0.0F, -0.06F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.26F, KeyframeAnimations.degreeVec(15.18F, 0.0F, -0.16F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2917F, KeyframeAnimations.degreeVec(20.7F, 0.0F, -0.04F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.55F, KeyframeAnimations.degreeVec(38.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-58.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.1F, KeyframeAnimations.degreeVec(62.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.26F, KeyframeAnimations.degreeVec(62.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.55F, KeyframeAnimations.degreeVec(-38.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("left_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(88.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.1F, KeyframeAnimations.degreeVec(-38.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.26F, KeyframeAnimations.degreeVec(-38.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.55F, KeyframeAnimations.degreeVec(68.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("left_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-36.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.1F, KeyframeAnimations.degreeVec(64.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.26F, KeyframeAnimations.degreeVec(64.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.55F, KeyframeAnimations.degreeVec(-32.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-58.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.1F, KeyframeAnimations.degreeVec(62.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.26F, KeyframeAnimations.degreeVec(62.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.55F, KeyframeAnimations.degreeVec(-38.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("right_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(88.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.1F, KeyframeAnimations.degreeVec(-38.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.26F, KeyframeAnimations.degreeVec(-38.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.55F, KeyframeAnimations.degreeVec(68.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("right_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-36.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.1F, KeyframeAnimations.degreeVec(64.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.26F, KeyframeAnimations.degreeVec(64.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.55F, KeyframeAnimations.degreeVec(-32.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("left_leg3", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.14F, 1.2F, 1.06F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.1F, KeyframeAnimations.scaleVec(1.45F, 1.55F, 1.2F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.26F, KeyframeAnimations.scaleVec(1.2F, 1.28F, 1.1F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.55F, KeyframeAnimations.scaleVec(1.25F, 1.35F, 1.12F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("right_leg3", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.14F, 1.2F, 1.06F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.1F, KeyframeAnimations.scaleVec(1.45F, 1.55F, 1.2F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.26F, KeyframeAnimations.scaleVec(1.2F, 1.28F, 1.1F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.55F, KeyframeAnimations.scaleVec(1.25F, 1.35F, 1.12F), AnimationChannel.Interpolations.LINEAR)
			))
			.build();

	/** Suspension : la bete se met en boule a l'apogee, pattes armees sous elle. */
	public static final AnimationDefinition TELLURIC_STOMP_HOVER = AnimationDefinition.Builder.withLength(0.5F).looping()
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 1.4F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(2.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 6.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(2.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.04F, 0.96F, 1.04F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.scaleVec(0.98F, 1.05F, 0.98F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.scaleVec(1.04F, 0.96F, 1.04F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("body2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(-16.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(32.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0417F, KeyframeAnimations.degreeVec(31.39F, -1.34F, -0.02F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.125F, KeyframeAnimations.degreeVec(36.46F, -1.84F, -0.03F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(49.49F, 2.01F, 0.03F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.375F, KeyframeAnimations.degreeVec(45.69F, 3.61F, 0.05F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(32.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("front_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0833F, KeyframeAnimations.degreeVec(20.56F, -0.86F, -0.05F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(35.03F, 2.27F, 0.13F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.375F, KeyframeAnimations.degreeVec(31.37F, 2.77F, 0.16F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-100.0F, 0.0F, -58.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(-118.0F, 0.0F, -70.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(-100.0F, 0.0F, -58.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-100.0F, 0.0F, 58.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(-118.0F, 0.0F, 70.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(-100.0F, 0.0F, 58.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(38.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0417F, KeyframeAnimations.degreeVec(36.21F, 0.0F, -4.02F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.125F, KeyframeAnimations.degreeVec(51.73F, 0.0F, 6.89F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(59.97F, 0.0F, 5.27F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2917F, KeyframeAnimations.degreeVec(60.09F, 0.0F, 5.84F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.375F, KeyframeAnimations.degreeVec(50.82F, 0.0F, 5.51F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(38.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(38.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0417F, KeyframeAnimations.degreeVec(37.45F, 0.0F, -3.93F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.125F, KeyframeAnimations.degreeVec(49.32F, 0.0F, 6.64F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(56.54F, 0.0F, 5.61F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2917F, KeyframeAnimations.degreeVec(56.26F, 0.0F, 6.25F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.375F, KeyframeAnimations.degreeVec(48.72F, 0.0F, 5.33F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(38.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-38.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(-52.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(-38.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(68.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(82.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(68.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-32.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(-40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(-32.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-38.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(-52.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(-38.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(68.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(82.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(68.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-32.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(-40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(-32.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg3", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.25F, 1.35F, 1.12F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.scaleVec(1.45F, 1.6F, 1.2F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.scaleVec(1.25F, 1.35F, 1.12F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg3", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.25F, 1.35F, 1.12F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.scaleVec(1.45F, 1.6F, 1.2F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.scaleVec(1.25F, 1.35F, 1.12F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.build();

	/** Plongeon : le pilon -- pattes tendues, semelles gonflees en tete, corps enroule au-dessus. */
	public static final AnimationDefinition TELLURIC_STOMP_DIVE = AnimationDefinition.Builder.withLength(0.4F).looping()
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.1F, KeyframeAnimations.posVec(0.0F, -0.8F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2F, KeyframeAnimations.posVec(0.0F, 0.4F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3F, KeyframeAnimations.posVec(0.0F, -0.8F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.1F, KeyframeAnimations.degreeVec(7.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2F, KeyframeAnimations.degreeVec(5.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3F, KeyframeAnimations.degreeVec(7.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(5.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.9F, 1.14F, 0.9F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2F, KeyframeAnimations.scaleVec(0.93F, 1.1F, 0.93F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.scaleVec(0.9F, 1.14F, 0.9F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(34.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2F, KeyframeAnimations.degreeVec(38.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(34.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("body2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2F, KeyframeAnimations.degreeVec(22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(16.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(16.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(66.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0833F, KeyframeAnimations.degreeVec(62.99F, -9.97F, -0.16F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2F, KeyframeAnimations.degreeVec(72.52F, -0.52F, -0.01F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(70.17F, -7.86F, -0.13F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2917F, KeyframeAnimations.degreeVec(68.13F, -10.13F, -0.16F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3333F, KeyframeAnimations.degreeVec(67.82F, -5.33F, -0.09F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(66.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("front_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2F, KeyframeAnimations.degreeVec(24.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(26.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0833F, KeyframeAnimations.degreeVec(23.87F, -7.25F, -0.45F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2F, KeyframeAnimations.degreeVec(34.13F, -0.77F, -0.05F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(36.1F, -6.45F, -0.4F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2917F, KeyframeAnimations.degreeVec(34.12F, -7.69F, -0.48F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3333F, KeyframeAnimations.degreeVec(31.19F, -3.81F, -0.24F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(26.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-168.0F, 0.0F, -24.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2F, KeyframeAnimations.degreeVec(-176.0F, 0.0F, -32.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(-168.0F, 0.0F, -24.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-168.0F, 0.0F, 24.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2F, KeyframeAnimations.degreeVec(-176.0F, 0.0F, 32.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(-168.0F, 0.0F, 24.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(68.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0417F, KeyframeAnimations.degreeVec(64.66F, 0.0F, 5.2F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.1667F, KeyframeAnimations.degreeVec(82.36F, 0.0F, -7.42F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2F, KeyframeAnimations.degreeVec(78.53F, 0.0F, 0.12F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(73.92F, 0.0F, 6.53F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3333F, KeyframeAnimations.degreeVec(76.26F, 0.0F, -8.88F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(68.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(68.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0417F, KeyframeAnimations.degreeVec(65.33F, 0.0F, 5.33F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.1667F, KeyframeAnimations.degreeVec(81.63F, 0.0F, -7.7F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2F, KeyframeAnimations.degreeVec(78.48F, 0.0F, -0.11F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(74.31F, 0.0F, 6.46F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3333F, KeyframeAnimations.degreeVec(75.53F, 0.0F, -9.06F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(68.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg2", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.14F, 1.06F, 1.14F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2F, KeyframeAnimations.scaleVec(1.2F, 1.1F, 1.2F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.scaleVec(1.14F, 1.06F, 1.14F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg2", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.14F, 1.06F, 1.14F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2F, KeyframeAnimations.scaleVec(1.2F, 1.1F, 1.2F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.scaleVec(1.14F, 1.06F, 1.14F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg3", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.75F, 2.15F, 1.28F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2F, KeyframeAnimations.scaleVec(1.95F, 2.5F, 1.38F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.scaleVec(1.75F, 2.15F, 1.28F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg3", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.75F, 2.15F, 1.28F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2F, KeyframeAnimations.scaleVec(1.95F, 2.5F, 1.38F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.scaleVec(1.75F, 2.15F, 1.28F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.build();

	/** Reception : la semelle s'ecrase et s'etale, le corps encaisse, rebond, retour au repos. */
	public static final AnimationDefinition TELLURIC_STOMP_LAND = AnimationDefinition.Builder.withLength(0.5F)
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.06F, KeyframeAnimations.posVec(0.0F, -7.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.posVec(0.0F, 2.5F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.32F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.06F, KeyframeAnimations.degreeVec(18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(-9.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.32F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.9F, 1.14F, 0.9F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.06F, KeyframeAnimations.scaleVec(1.3F, 0.72F, 1.3F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.scaleVec(0.92F, 1.12F, 0.92F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.32F, KeyframeAnimations.scaleVec(1.04F, 0.97F, 1.04F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.5F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(34.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.06F, KeyframeAnimations.degreeVec(26.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("body2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.06F, KeyframeAnimations.degreeVec(28.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(16.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.06F, KeyframeAnimations.degreeVec(34.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.06F, KeyframeAnimations.degreeVec(24.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(66.01F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0417F, KeyframeAnimations.degreeVec(1.59F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.06F, KeyframeAnimations.degreeVec(-12.1F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0833F, KeyframeAnimations.degreeVec(-11.07F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.125F, KeyframeAnimations.degreeVec(1.84F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(15.11F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.32F, KeyframeAnimations.degreeVec(-8.71F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-2.74F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("front_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.06F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.24F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(26.1F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0417F, KeyframeAnimations.degreeVec(-3.8F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.06F, KeyframeAnimations.degreeVec(-11.89F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0833F, KeyframeAnimations.degreeVec(-15.65F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.125F, KeyframeAnimations.degreeVec(-15.09F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-9.43F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.28F, KeyframeAnimations.degreeVec(9.33F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-168.0F, 0.0F, -24.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.06F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -62.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(-58.0F, 0.0F, -28.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-168.0F, 0.0F, 24.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.06F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 62.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(-58.0F, 0.0F, 28.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("left_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(67.99F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0417F, KeyframeAnimations.degreeVec(-6.15F, 0.0F, -0.02F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.06F, KeyframeAnimations.degreeVec(-19.96F, 0.0F, 0.04F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0833F, KeyframeAnimations.degreeVec(-13.8F, 0.0F, 0.21F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.125F, KeyframeAnimations.degreeVec(0.22F, 0.0F, 0.19F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(8.21F, 0.0F, -0.13F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2917F, KeyframeAnimations.degreeVec(12.35F, 0.0F, 0.05F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(67.99F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0417F, KeyframeAnimations.degreeVec(-6.15F, 0.0F, 0.02F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.06F, KeyframeAnimations.degreeVec(-19.96F, 0.0F, -0.04F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0833F, KeyframeAnimations.degreeVec(-13.8F, 0.0F, -0.21F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.125F, KeyframeAnimations.degreeVec(0.22F, 0.0F, -0.19F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(8.21F, 0.0F, 0.13F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2917F, KeyframeAnimations.degreeVec(12.35F, 0.0F, -0.05F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, -5.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.06F, KeyframeAnimations.degreeVec(-58.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("left_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.06F, KeyframeAnimations.degreeVec(86.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("left_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.06F, KeyframeAnimations.degreeVec(-38.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 5.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.06F, KeyframeAnimations.degreeVec(-58.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("right_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.06F, KeyframeAnimations.degreeVec(86.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("right_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.06F, KeyframeAnimations.degreeVec(-38.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("left_leg2", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.14F, 1.06F, 1.14F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.06F, KeyframeAnimations.scaleVec(1.22F, 0.9F, 1.22F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.24F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.5F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("right_leg2", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.14F, 1.06F, 1.14F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.06F, KeyframeAnimations.scaleVec(1.22F, 0.9F, 1.22F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.24F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.5F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("left_leg3", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.75F, 2.15F, 1.28F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.06F, KeyframeAnimations.scaleVec(2.3F, 1.2F, 1.55F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.scaleVec(1.45F, 1.45F, 1.18F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.5F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("right_leg3", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.75F, 2.15F, 1.28F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.06F, KeyframeAnimations.scaleVec(2.3F, 1.2F, 1.55F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.18F, KeyframeAnimations.scaleVec(1.45F, 1.45F, 1.18F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.5F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.build();

	public static final AnimationDefinition TRANSITION_IDLE_NAP = AnimationDefinition.Builder.withLength(1.0F)
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2083F, KeyframeAnimations.posVec(0.0F, 0.25F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3333F, KeyframeAnimations.posVec(0.0F, 0.97F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4583F, KeyframeAnimations.posVec(0.0F, 2.46F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5833F, KeyframeAnimations.posVec(0.0F, 4.09F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7083F, KeyframeAnimations.posVec(0.0F, 5.34F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.375F, KeyframeAnimations.degreeVec(3.61F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.degreeVec(13.41F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(16.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.15F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4167F, KeyframeAnimations.degreeVec(6.45F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5833F, KeyframeAnimations.degreeVec(26.42F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7083F, KeyframeAnimations.degreeVec(44.82F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.875F, KeyframeAnimations.degreeVec(57.22F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(58.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-2.01F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.75F, KeyframeAnimations.degreeVec(-15.81F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-10.93F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-58.06F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.625F, KeyframeAnimations.degreeVec(-63.07F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-66.57F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-71.56F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(-72.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2083F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3333F, KeyframeAnimations.degreeVec(21.33F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4583F, KeyframeAnimations.degreeVec(49.9F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(60.13F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5417F, KeyframeAnimations.degreeVec(69.73F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.degreeVec(88.84F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7917F, KeyframeAnimations.degreeVec(95.35F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(96.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-10.93F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-58.06F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.625F, KeyframeAnimations.degreeVec(-63.07F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-66.57F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-71.56F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(-72.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2083F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3333F, KeyframeAnimations.degreeVec(21.33F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4583F, KeyframeAnimations.degreeVec(49.9F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(60.13F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5417F, KeyframeAnimations.degreeVec(69.73F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.degreeVec(88.84F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7917F, KeyframeAnimations.degreeVec(95.35F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(96.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-2.63F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-6.52F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-5.78F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.875F, KeyframeAnimations.degreeVec(-10.4F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-1.94F, 0.0F, -0.81F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-9.55F, 0.0F, -3.98F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.75F, KeyframeAnimations.degreeVec(-19.71F, 0.0F, -8.21F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.875F, KeyframeAnimations.degreeVec(-23.51F, 0.0F, -9.8F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(-24.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-1.58F, 0.0F, 0.66F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-8.84F, 0.0F, 3.69F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.75F, KeyframeAnimations.degreeVec(-19.36F, 0.0F, 8.07F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.875F, KeyframeAnimations.degreeVec(-23.46F, 0.0F, 9.78F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(-24.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.01F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2083F, KeyframeAnimations.degreeVec(-0.4F, 0.0F, 0.03F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-3.64F, 0.0F, -0.05F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-0.15F, 0.0F, -1.75F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(3.96F, 0.0F, -2.72F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5833F, KeyframeAnimations.degreeVec(20.58F, 0.0F, -5.71F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.degreeVec(29.42F, 0.0F, -8.73F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.75F, KeyframeAnimations.degreeVec(44.67F, 0.0F, -11.9F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(46.0F, 0.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.01F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-3.81F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-1.65F, 0.0F, 1.31F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(2.12F, 0.0F, 2.18F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5833F, KeyframeAnimations.degreeVec(18.55F, 0.0F, 5.09F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.degreeVec(27.79F, 0.0F, 8.23F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.75F, KeyframeAnimations.degreeVec(43.92F, 0.0F, 11.64F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.9167F, KeyframeAnimations.degreeVec(47.66F, 0.0F, 14.06F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(46.0F, 0.0F, 14.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2083F, KeyframeAnimations.degreeVec(0.43F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(-2.55F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.75F, KeyframeAnimations.degreeVec(4.43F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-0.48F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.build();

	public static final AnimationDefinition TRANSITION_NAP_IDLE = AnimationDefinition.Builder.withLength(1.0F)
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2083F, KeyframeAnimations.posVec(0.0F, 5.75F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3333F, KeyframeAnimations.posVec(0.0F, 5.03F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4583F, KeyframeAnimations.posVec(0.0F, 3.54F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5833F, KeyframeAnimations.posVec(0.0F, 1.91F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7083F, KeyframeAnimations.posVec(0.0F, 0.66F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(16.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.375F, KeyframeAnimations.degreeVec(12.39F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.degreeVec(2.59F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(58.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2083F, KeyframeAnimations.degreeVec(56.82F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3333F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(28.79F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5833F, KeyframeAnimations.degreeVec(16.52F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7083F, KeyframeAnimations.degreeVec(4.13F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-16.39F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-1.77F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-72.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-63.59F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.625F, KeyframeAnimations.degreeVec(-19.27F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-13.84F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-9.25F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-3.18F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(96.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3333F, KeyframeAnimations.degreeVec(84.79F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.625F, KeyframeAnimations.degreeVec(25.69F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.degreeVec(18.45F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7083F, KeyframeAnimations.degreeVec(12.34F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7917F, KeyframeAnimations.degreeVec(4.24F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-72.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-63.59F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.625F, KeyframeAnimations.degreeVec(-19.27F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-13.84F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-9.25F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-3.18F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(96.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3333F, KeyframeAnimations.degreeVec(84.79F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.625F, KeyframeAnimations.degreeVec(25.69F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.degreeVec(18.45F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7083F, KeyframeAnimations.degreeVec(12.34F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7917F, KeyframeAnimations.degreeVec(4.24F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(-10.02F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-5.19F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.75F, KeyframeAnimations.degreeVec(-5.54F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.875F, KeyframeAnimations.degreeVec(-1.61F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-24.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-14.64F, 0.0F, -6.1F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-24.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-14.64F, 0.0F, 6.1F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(45.99F, 0.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(41.71F, 0.0F, -11.92F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4167F, KeyframeAnimations.degreeVec(21.91F, 0.0F, -5.15F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4583F, KeyframeAnimations.degreeVec(15.16F, 0.0F, -3.47F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(6.14F, 0.0F, -2.11F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5833F, KeyframeAnimations.degreeVec(1.31F, 0.0F, -0.66F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-5.98F, 0.0F, -0.06F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.75F, KeyframeAnimations.degreeVec(-2.79F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(45.99F, 0.0F, 14.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2917F, KeyframeAnimations.degreeVec(38.92F, 0.0F, 10.87F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4167F, KeyframeAnimations.degreeVec(23.95F, 0.0F, 5.79F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4583F, KeyframeAnimations.degreeVec(17.11F, 0.0F, 4.07F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(7.92F, 0.0F, 2.64F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5833F, KeyframeAnimations.degreeVec(2.26F, 0.0F, 0.94F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-5.63F, 0.0F, 0.17F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.75F, KeyframeAnimations.degreeVec(-2.78F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2083F, KeyframeAnimations.degreeVec(-0.47F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(2.62F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.75F, KeyframeAnimations.degreeVec(-4.43F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8333F, KeyframeAnimations.degreeVec(-3.1F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.9167F, KeyframeAnimations.degreeVec(0.4F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.build();

	public static final AnimationDefinition NAP = AnimationDefinition.Builder.withLength(4.0F).looping()
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 6.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.posVec(0.0F, 6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(16.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.degreeVec(16.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.scaleVec(1.03F, 1.03F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(58.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(61.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.degreeVec(58.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(-23.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-72.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.degreeVec(-72.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(96.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.degreeVec(96.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-72.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.degreeVec(-72.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(96.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.degreeVec(96.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(-9.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-24.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.degreeVec(-24.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-24.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.degreeVec(-24.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(46.0F, 0.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.4583F, KeyframeAnimations.degreeVec(43.0F, -7.51F, -14.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.625F, KeyframeAnimations.degreeVec(49.43F, 8.58F, -14.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.8333F, KeyframeAnimations.degreeVec(46.02F, 0.04F, -14.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.degreeVec(46.0F, 0.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(46.0F, 0.0F, 14.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0F, KeyframeAnimations.degreeVec(46.0F, 0.0F, 14.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0417F, KeyframeAnimations.degreeVec(43.87F, 6.38F, 14.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0833F, KeyframeAnimations.degreeVec(44.14F, 5.59F, 14.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.1667F, KeyframeAnimations.degreeVec(47.84F, -5.52F, 14.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.2083F, KeyframeAnimations.degreeVec(48.65F, -7.96F, 14.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.2917F, KeyframeAnimations.degreeVec(47.23F, -3.69F, 14.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.375F, KeyframeAnimations.degreeVec(46.07F, -0.21F, 14.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.degreeVec(46.0F, 0.0F, 14.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("body2", new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.scaleVec(1.02F, 1.029F, 1.01F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.25F, KeyframeAnimations.degreeVec(2.26F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.4167F, KeyframeAnimations.degreeVec(-2.62F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.6667F, KeyframeAnimations.degreeVec(-0.08F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.build();

	public static final AnimationDefinition DROWN_WINDUP = AnimationDefinition.Builder.withLength(1.1F)
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.45F, KeyframeAnimations.posVec(0.0F, 3.4F, -2.2F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.85F, KeyframeAnimations.posVec(0.0F, 3.8F, -2.6F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, -0.6F, 2.4F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.45F, KeyframeAnimations.degreeVec(-26.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.85F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.1F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("body2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.45F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.85F, KeyframeAnimations.degreeVec(-26.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.1F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.45F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.85F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.1F, KeyframeAnimations.degreeVec(28.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.45F, KeyframeAnimations.degreeVec(22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.85F, KeyframeAnimations.degreeVec(26.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.1F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.01F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-27.73F, 0.0F, -7.68F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.45F, KeyframeAnimations.degreeVec(-34.22F, 0.0F, -11.94F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-13.3F, 0.0F, -15.11F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8333F, KeyframeAnimations.degreeVec(2.2F, 0.0F, -16.3F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.9167F, KeyframeAnimations.degreeVec(6.78F, 0.0F, -15.78F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.1F, KeyframeAnimations.degreeVec(30.0F, 0.0F, -18.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.01F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-27.73F, 0.0F, 7.68F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.45F, KeyframeAnimations.degreeVec(-34.22F, 0.0F, 11.94F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-13.3F, 0.0F, 15.11F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8333F, KeyframeAnimations.degreeVec(2.2F, 0.0F, 16.3F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.9167F, KeyframeAnimations.degreeVec(6.78F, 0.0F, 15.78F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.1F, KeyframeAnimations.degreeVec(30.0F, 0.0F, 18.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.45F, KeyframeAnimations.degreeVec(-142.0F, 0.0F, -30.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.85F, KeyframeAnimations.degreeVec(-156.0F, 0.0F, -36.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.1F, KeyframeAnimations.degreeVec(-58.0F, 0.0F, -16.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.45F, KeyframeAnimations.degreeVec(-142.0F, 0.0F, 30.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.85F, KeyframeAnimations.degreeVec(-156.0F, 0.0F, 36.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.1F, KeyframeAnimations.degreeVec(-58.0F, 0.0F, 16.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.45F, KeyframeAnimations.degreeVec(-32.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.85F, KeyframeAnimations.degreeVec(-36.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.1F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.45F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.1F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.45F, KeyframeAnimations.degreeVec(-46.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.1F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.45F, KeyframeAnimations.degreeVec(58.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.1F, KeyframeAnimations.degreeVec(42.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.45F, KeyframeAnimations.degreeVec(-46.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.1F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.45F, KeyframeAnimations.degreeVec(58.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.1F, KeyframeAnimations.degreeVec(42.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.build();

	public static final AnimationDefinition DROWN_HOLD = AnimationDefinition.Builder.withLength(1.6F).looping()
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.posVec(0.0F, -1.4F, 0.6F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8F, KeyframeAnimations.posVec(0.0F, 0.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, -1.4F, 0.6F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.6F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.2F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.6F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("body2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(24.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8F, KeyframeAnimations.degreeVec(12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.2F, KeyframeAnimations.degreeVec(24.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.6F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(24.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(32.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8F, KeyframeAnimations.degreeVec(22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.2F, KeyframeAnimations.degreeVec(32.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.6F, KeyframeAnimations.degreeVec(24.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(26.0F, -4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8F, KeyframeAnimations.degreeVec(16.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.2F, KeyframeAnimations.degreeVec(26.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.6F, KeyframeAnimations.degreeVec(18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(36.0F, 0.0F, -18.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0417F, KeyframeAnimations.degreeVec(33.08F, 0.0F, -17.87F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.125F, KeyframeAnimations.degreeVec(40.14F, 0.0F, -18.43F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(40.43F, 0.0F, -18.74F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4583F, KeyframeAnimations.degreeVec(45.34F, 0.0F, -19.79F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5417F, KeyframeAnimations.degreeVec(43.19F, 0.0F, -19.7F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8F, KeyframeAnimations.degreeVec(39.24F, 0.0F, -19.84F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0833F, KeyframeAnimations.degreeVec(42.47F, 0.0F, -19.44F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.2083F, KeyframeAnimations.degreeVec(44.81F, 0.0F, -19.11F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.3333F, KeyframeAnimations.degreeVec(41.21F, 0.0F, -18.78F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.6F, KeyframeAnimations.degreeVec(36.0F, 0.0F, -18.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(36.0F, 0.0F, 18.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.0417F, KeyframeAnimations.degreeVec(33.04F, 0.0F, 17.82F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.125F, KeyframeAnimations.degreeVec(40.05F, 0.0F, 18.36F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(40.41F, 0.0F, 18.62F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4167F, KeyframeAnimations.degreeVec(45.25F, 0.0F, 19.22F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8F, KeyframeAnimations.degreeVec(39.23F, 0.0F, 19.78F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.2083F, KeyframeAnimations.degreeVec(44.47F, 0.0F, 19.52F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.3333F, KeyframeAnimations.degreeVec(40.98F, 0.0F, 18.82F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.6F, KeyframeAnimations.degreeVec(36.0F, 0.0F, 18.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-54.0F, 0.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(-72.0F, 0.0F, -20.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8F, KeyframeAnimations.degreeVec(-50.0F, 0.0F, -13.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.2F, KeyframeAnimations.degreeVec(-72.0F, 0.0F, -20.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.6F, KeyframeAnimations.degreeVec(-54.0F, 0.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-54.0F, 0.0F, 14.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(-72.0F, 0.0F, 20.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8F, KeyframeAnimations.degreeVec(-50.0F, 0.0F, 13.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.2F, KeyframeAnimations.degreeVec(-72.0F, 0.0F, 20.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.6F, KeyframeAnimations.degreeVec(-54.0F, 0.0F, 14.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(-26.0F, 6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8F, KeyframeAnimations.degreeVec(-16.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.2F, KeyframeAnimations.degreeVec(-26.0F, -6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.6F, KeyframeAnimations.degreeVec(-18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(-14.0F, 10.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.2F, KeyframeAnimations.degreeVec(-14.0F, -10.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.6F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-34.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8F, KeyframeAnimations.degreeVec(-34.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.2F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.6F, KeyframeAnimations.degreeVec(-34.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(48.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(26.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8F, KeyframeAnimations.degreeVec(48.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.2F, KeyframeAnimations.degreeVec(26.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.6F, KeyframeAnimations.degreeVec(48.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(-34.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.2F, KeyframeAnimations.degreeVec(-34.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.6F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(26.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4F, KeyframeAnimations.degreeVec(48.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8F, KeyframeAnimations.degreeVec(26.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.2F, KeyframeAnimations.degreeVec(48.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.6F, KeyframeAnimations.degreeVec(26.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.build();

	public static final AnimationDefinition ALERT_THUMP = AnimationDefinition.Builder.withLength(3.0F)
			.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5417F, KeyframeAnimations.degreeVec(-31.5971F, -28.9242F, -7.3017F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.7083F, KeyframeAnimations.degreeVec(-38.421F, -32.892F, -3.7539F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.5417F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("body2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.75F, KeyframeAnimations.degreeVec(7.9147F, 30.7882F, 23.146F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.2083F, KeyframeAnimations.degreeVec(14.9641F, 20.1368F, 26.8774F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.7917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(34.7493F, -4.7306F, 5.8307F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.375F, KeyframeAnimations.degreeVec(25.0172F, 0.9982F, -2.3734F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(-1.2737F, -21.9037F, 3.7163F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.25F, KeyframeAnimations.degreeVec(13.9771F, 19.6306F, 18.802F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.25F, KeyframeAnimations.degreeVec(25.0172F, 0.9982F, -2.3734F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.01F, 0.01F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-5.61F, 2.75F, 0.04F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2083F, KeyframeAnimations.degreeVec(-8.74F, 2.36F, 0.04F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-20.13F, -0.82F, -0.01F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(-29.95F, -2.57F, -0.04F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.625F, KeyframeAnimations.degreeVec(-31.45F, -1.87F, -0.03F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.75F, KeyframeAnimations.degreeVec(-24.73F, 0.55F, 0.01F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-23.15F, 0.77F, 0.01F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5F, KeyframeAnimations.degreeVec(-5.54F, -0.35F, -0.01F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.875F, KeyframeAnimations.degreeVec(-4.59F, -1.83F, -0.03F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.125F, KeyframeAnimations.degreeVec(-0.96F, -0.5F, -0.01F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.5F, KeyframeAnimations.degreeVec(1.46F, 1.54F, 0.02F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.625F, KeyframeAnimations.degreeVec(3.69F, 3.42F, 0.05F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.75F, KeyframeAnimations.degreeVec(-0.08F, -0.05F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.8333F, KeyframeAnimations.degreeVec(-1.38F, -1.18F, -0.02F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.75F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.01F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2083F, KeyframeAnimations.degreeVec(28.64F, 3.17F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(57.86F, -4.64F, 0.1F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8333F, KeyframeAnimations.degreeVec(68.41F, -8.18F, -8.66F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0833F, KeyframeAnimations.degreeVec(66.06F, -18.47F, -18.42F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.75F, KeyframeAnimations.degreeVec(56.21F, -32.08F, -35.69F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.6667F, KeyframeAnimations.degreeVec(15.17F, -4.37F, -8.6F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.8333F, KeyframeAnimations.degreeVec(2.89F, -5.64F, -3.68F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(28.2556F, 12.7002F, -15.5793F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.25F, KeyframeAnimations.degreeVec(-19.3185F, -4.5874F, -4.3222F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.25F, KeyframeAnimations.posVec(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.04F, 0.0F, 0.01F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(-41.45F, -13.14F, -14.39F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5F, KeyframeAnimations.degreeVec(-34.9F, -14.32F, 53.22F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.5F, KeyframeAnimations.degreeVec(37.92F, 0.25F, 10.35F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.5417F, KeyframeAnimations.degreeVec(40.17F, 0.86F, 6.97F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.5833F, KeyframeAnimations.degreeVec(38.5F, 1.19F, 4.77F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.6667F, KeyframeAnimations.degreeVec(22.3F, 1.14F, 4.38F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.875F, KeyframeAnimations.degreeVec(-1.18F, 0.01F, -0.13F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(-18.79F, -6.62F, -14.11F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-27.8F, -9.33F, -13.43F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-38.89F, -12.64F, -15.95F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(-41.08F, -13.36F, -14.16F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5F, KeyframeAnimations.degreeVec(-35.03F, -14.32F, 53.24F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.5F, KeyframeAnimations.degreeVec(37.91F, 0.25F, 10.28F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.625F, KeyframeAnimations.degreeVec(31.25F, 1.26F, 4.39F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.6667F, KeyframeAnimations.degreeVec(22.69F, 1.14F, 4.34F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.875F, KeyframeAnimations.degreeVec(-1.01F, 0.01F, -0.14F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 32.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.25F, KeyframeAnimations.degreeVec(5.1653F, 27.0478F, 11.2432F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.75F, KeyframeAnimations.posVec(-2.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.posVec(-2.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.75F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg2", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3333F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4167F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5833F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.75F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7917F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8333F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.875F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.9167F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.9583F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0417F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0833F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.125F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.1667F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.2083F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.25F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.2917F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.3333F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.375F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.4167F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.4583F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5417F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5833F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.625F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.6667F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.7083F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.75F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.7917F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.8333F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.875F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.9167F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.9583F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0417F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0833F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.125F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.1667F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.2083F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.375F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.4583F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.5833F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.6667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.75F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8333F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.875F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.9583F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0417F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0833F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.125F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.1667F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.2083F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.25F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.2917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.3333F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.375F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.4167F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.4583F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5417F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5833F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.625F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.6667F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.7083F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.75F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.7917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.8333F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.875F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.9167F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.9583F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0417F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0833F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.125F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.1667F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.2083F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.375F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.4583F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.5833F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.6667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_leg3", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(28.2556F, -12.7002F, 15.5793F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.25F, KeyframeAnimations.degreeVec(-19.3185F, 4.5874F, 4.3222F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.25F, KeyframeAnimations.posVec(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(3.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			))
			.addAnimation("left_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.999F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.124F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.125F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.249F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.374F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.375F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.499F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.addAnimation("right_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(0.999F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.124F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.125F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.249F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.374F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.375F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.499F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
					new Keyframe(1.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			))
			.build();

	public static final AnimationDefinition SEDATED_FALL = SedatedFall.DEFINITION;
	public static final AnimationDefinition SEDATED_REST = SedatedRest.DEFINITION;
	public static final AnimationDefinition SEDATED_WAKE = SedatedWake.DEFINITION;

	private static final class SedatedFall {
		private static final AnimationDefinition DEFINITION = AnimationDefinition.Builder.withLength(1.8F)
				.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1F, KeyframeAnimations.degreeVec(0.57F, 0.78F, -1.3F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2F, KeyframeAnimations.degreeVec(2.04F, 2.22F, -3.7F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(4.0F, 3.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(5.5F, 2.86F, -4.22F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(7.69F, 2.51F, -2.36F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(12.78F, 1.49F, 1.72F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(15.0F, 1.0F, 2.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(19.47F, -0.22F, 2.2F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(22.0F, -1.0F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(18.52F, -0.74F, 0.62F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.degreeVec(11.59F, -0.26F, -0.59F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(6.5F, 0.0F, -1.2F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.degreeVec(4.21F, 0.0F, -0.73F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(2.48F, 0.0F, 0.13F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(1.8F, 0.0F, 0.6F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(3.6F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.degreeVec(2.9F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.05F, KeyframeAnimations.posVec(0.0F, -0.047F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1F, KeyframeAnimations.posVec(0.0F, -0.176F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.15F, KeyframeAnimations.posVec(0.0F, -0.372F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2F, KeyframeAnimations.posVec(0.0F, -0.618F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, -0.9F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.posVec(0.0F, -1.2F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.posVec(0.0F, -1.594F, -0.027F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.posVec(0.0F, -2.135F, -0.098F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.posVec(0.0F, -2.78F, -0.195F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, -3.484F, -0.301F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.posVec(0.0F, -4.2F, -0.4F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.posVec(0.0F, -4.908F, -0.513F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.posVec(0.0F, -5.663F, -0.645F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.posVec(0.0F, -6.537F, -0.754F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.posVec(0.0F, -7.6F, -0.8F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.posVec(0.0F, -9.551F, -0.753F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.posVec(0.0F, -11.937F, -0.639F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.posVec(0.0F, -13.1F, -0.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.posVec(0.0F, -12.968F, -0.265F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, -12.776F, -0.029F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.posVec(0.0F, -12.795F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, -12.971F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.posVec(0.0F, -13.08F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.35F, KeyframeAnimations.posVec(0.0F, -12.97F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.posVec(0.0F, -13.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.SCALE,
						new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.scaleVec(1.0078F, 0.9909F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.scaleVec(1.0222F, 0.9741F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.scaleVec(1.03F, 0.965F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.scaleVec(1.0222F, 0.9819F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.scaleVec(1.0078F, 1.0131F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.scaleVec(1.0F, 1.03F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.scaleVec(1.0101F, 1.0148F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3F, KeyframeAnimations.scaleVec(1.0118F, 1.0128F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.scaleVec(1.01F, 1.02F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.scaleVec(1.01F, 1.0175F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("body2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1F, KeyframeAnimations.degreeVec(0.78F, 0.0F, 0.52F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2F, KeyframeAnimations.degreeVec(2.22F, 0.0F, 1.48F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(1.72F, 0.0F, 0.94F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-0.84F, 0.0F, -0.69F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(-2.0F, 0.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(-4.5F, 0.0F, -0.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(-4.06F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.degreeVec(-0.44F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(1.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(-1.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3F, KeyframeAnimations.degreeVec(0.4F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.05F, KeyframeAnimations.degreeVec(1.04F, -0.44F, 0.3F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1F, KeyframeAnimations.degreeVec(3.63F, -1.56F, 1.04F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.15F, KeyframeAnimations.degreeVec(7.0F, -3.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2F, KeyframeAnimations.degreeVec(10.37F, -4.44F, 2.96F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(12.96F, -5.56F, 3.7F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(14.0F, -6.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(12.96F, -5.73F, 3.38F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(10.48F, -5.02F, 1.89F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(7.52F, -4.05F, 0.11F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(5.04F, -2.99F, -1.38F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(4.0F, -2.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(5.12F, -1.02F, -1.69F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(8.15F, 0.05F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(12.6F, 1.1F, -0.31F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(18.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(28.27F, 2.72F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.degreeVec(43.63F, 3.36F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(58.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.degreeVec(70.57F, 4.81F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(82.35F, 5.63F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(90.0F, 6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(94.19F, 5.74F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.degreeVec(97.29F, 5.26F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.degreeVec(98.5F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(98.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.35F, KeyframeAnimations.degreeVec(95.8F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4F, KeyframeAnimations.degreeVec(95.3F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6F, KeyframeAnimations.degreeVec(96.4F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(96.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.05F, KeyframeAnimations.degreeVec(0.89F, -0.59F, 0.44F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1F, KeyframeAnimations.degreeVec(3.11F, -2.07F, 1.56F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.15F, KeyframeAnimations.degreeVec(6.0F, -4.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2F, KeyframeAnimations.degreeVec(8.89F, -5.93F, 4.44F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(11.11F, -7.41F, 5.56F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(12.0F, -8.0F, 6.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(10.6F, -7.64F, 5.06F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(7.07F, -6.72F, 2.83F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(2.45F, -5.48F, 0.17F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-2.25F, -4.16F, -2.06F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(-6.0F, -3.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(-8.43F, -1.9F, -2.69F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(-12.64F, 0.29F, -0.95F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-16.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(-23.64F, 1.38F, 0.88F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.degreeVec(-35.61F, 1.65F, 1.86F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(-48.0F, 2.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.degreeVec(-60.94F, 2.72F, 4.64F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-74.35F, 3.58F, 6.56F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(-84.0F, 4.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(-90.2F, 4.0F, 8.94F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.degreeVec(-95.04F, 4.0F, 9.69F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.degreeVec(-97.0F, 4.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(-95.98F, 4.0F, 9.78F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3F, KeyframeAnimations.degreeVec(-93.75F, 4.0F, 9.3F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.35F, KeyframeAnimations.degreeVec(-91.52F, 4.0F, 8.82F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4F, KeyframeAnimations.degreeVec(-90.5F, 4.0F, 8.6F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6F, KeyframeAnimations.degreeVec(-92.4F, 4.0F, 9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(-92.0F, 4.0F, 9.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.05F, KeyframeAnimations.degreeVec(-0.34F, 0.0F, 0.32F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.15F, KeyframeAnimations.degreeVec(-2.63F, 0.0F, 2.34F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(-6.14F, 0.0F, 4.94F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 6.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(-10.62F, 0.0F, 6.98F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(-14.24F, 0.0F, 8.05F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(-17.95F, 0.0F, 9.02F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-20.84F, 0.0F, 9.73F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(-20.44F, 0.0F, 9.38F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(-17.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(-13.56F, 0.0F, 6.62F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 6.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(-14.77F, 0.0F, 6.93F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.degreeVec(-21.55F, 0.0F, 9.2F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 12.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.degreeVec(-45.19F, 0.0F, 16.87F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-64.49F, 0.0F, 22.99F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(-74.0F, 0.0F, 26.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(-68.3F, 0.0F, 23.41F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.degreeVec(-57.7F, 0.0F, 18.59F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.degreeVec(-52.0F, 0.0F, 16.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(-53.72F, 0.0F, 16.78F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3F, KeyframeAnimations.degreeVec(-57.5F, 0.0F, 18.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.35F, KeyframeAnimations.degreeVec(-61.28F, 0.0F, 20.22F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4F, KeyframeAnimations.degreeVec(-63.0F, 0.0F, 21.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.45F, KeyframeAnimations.degreeVec(-62.44F, 0.0F, 20.81F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.55F, KeyframeAnimations.degreeVec(-59.96F, 0.0F, 19.99F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6F, KeyframeAnimations.degreeVec(-59.4F, 0.0F, 19.8F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(-60.0F, 0.0F, 20.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.05F, KeyframeAnimations.degreeVec(-0.45F, 0.0F, -0.2F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.15F, KeyframeAnimations.degreeVec(-3.42F, 0.0F, -1.45F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(-7.8F, 0.0F, -3.2F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(-12.8F, 0.0F, -4.88F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(-16.44F, 0.0F, -5.93F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(-20.08F, 0.0F, -6.94F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-22.88F, 0.0F, -7.7F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(-24.0F, 0.0F, -8.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(-22.44F, 0.0F, -7.53F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(-19.0F, 0.0F, -6.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(-15.56F, 0.0F, -5.47F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(-17.12F, 0.0F, -5.6F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.degreeVec(-24.68F, 0.0F, -7.09F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(-34.0F, 0.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.degreeVec(-50.06F, 0.0F, -12.72F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-70.14F, 0.0F, -17.58F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(-80.0F, 0.0F, -20.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(-74.3F, 0.0F, -17.67F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.degreeVec(-63.7F, 0.0F, -13.33F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.degreeVec(-58.0F, 0.0F, -11.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(-59.56F, 0.0F, -11.62F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3F, KeyframeAnimations.degreeVec(-63.0F, 0.0F, -13.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.35F, KeyframeAnimations.degreeVec(-66.44F, 0.0F, -14.38F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4F, KeyframeAnimations.degreeVec(-68.0F, 0.0F, -15.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6F, KeyframeAnimations.degreeVec(-65.4F, 0.0F, -13.8F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(-66.0F, 0.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.05F, KeyframeAnimations.degreeVec(-0.35F, 0.0F, -0.07F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1F, KeyframeAnimations.degreeVec(-1.34F, 0.0F, -0.27F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.15F, KeyframeAnimations.degreeVec(-2.89F, 0.0F, -0.58F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(-7.3F, 0.0F, -1.46F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(-15.46F, 0.0F, -3.07F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(-24.6F, 0.0F, -4.86F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(-35.02F, 0.0F, -6.92F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-44.29F, 0.0F, -8.78F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(-50.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(-54.49F, 0.0F, -10.73F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(-64.01F, 0.0F, -11.37F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(-72.15F, 0.0F, -11.83F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-79.07F, 0.0F, -12.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(-84.38F, 0.0F, -11.44F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.degreeVec(-86.34F, 0.0F, -10.22F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(-83.68F, 0.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.degreeVec(-77.57F, 0.0F, -7.78F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-72.08F, 0.0F, -6.56F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(-70.28F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.degreeVec(-74.13F, 0.0F, -6.42F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(-76.91F, 0.0F, -7.08F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.45F, KeyframeAnimations.degreeVec(-75.77F, 0.0F, -7.07F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(-75.43F, 0.0F, -7.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.05F, KeyframeAnimations.degreeVec(-0.52F, 0.0F, 0.12F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1F, KeyframeAnimations.degreeVec(-1.98F, 0.0F, 0.46F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.15F, KeyframeAnimations.degreeVec(-4.21F, 0.0F, 0.96F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2F, KeyframeAnimations.degreeVec(-7.07F, 0.0F, 1.59F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(-10.38F, 0.0F, 2.28F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(-20.47F, 0.0F, 3.97F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(-30.63F, 0.0F, 5.29F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(-41.75F, 0.0F, 6.73F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-51.12F, 0.0F, 8.05F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(-56.0F, 0.0F, 9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(-56.9F, 0.0F, 9.7F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(-61.82F, 0.0F, 10.35F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(-70.32F, 0.0F, 10.82F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-77.68F, 0.0F, 11.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(-83.8F, 0.0F, 10.48F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.degreeVec(-86.64F, 0.0F, 9.29F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(-84.42F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.degreeVec(-77.94F, 0.0F, 6.46F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-71.77F, 0.0F, 4.78F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(-69.62F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(-71.29F, 0.0F, 4.12F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.degreeVec(-73.67F, 0.0F, 4.42F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.degreeVec(-75.6F, 0.0F, 4.78F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(-76.66F, 0.0F, 5.08F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.45F, KeyframeAnimations.degreeVec(-75.53F, 0.0F, 5.07F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(-75.19F, 0.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1F, KeyframeAnimations.degreeVec(-0.65F, -1.04F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2F, KeyframeAnimations.degreeVec(-1.97F, -2.96F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(-3.0F, -4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(-3.56F, -3.57F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(-4.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(-2.75F, -1.26F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(0.0F, -0.29F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(2.75F, 0.84F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(4.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(2.72F, 3.43F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.degreeVec(-0.35F, 4.98F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(-4.0F, 6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.degreeVec(-9.73F, 6.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-16.64F, 6.86F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(-19.69F, 7.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(-19.06F, 6.69F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.degreeVec(-14.94F, 5.31F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(-14.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3F, KeyframeAnimations.degreeVec(-14.37F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4F, KeyframeAnimations.degreeVec(-16.33F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.degreeVec(-17.6F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(-17.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1F, KeyframeAnimations.degreeVec(-0.31F, -0.78F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2F, KeyframeAnimations.degreeVec(-1.06F, -2.22F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(-2.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-5.27F, -2.32F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(-6.66F, -1.4F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-8.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(-5.12F, 3.55F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.degreeVec(0.86F, 5.22F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(6.0F, 6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.degreeVec(9.58F, 4.36F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(12.68F, 0.94F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(14.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(11.81F, -4.03F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.degreeVec(7.0F, -5.97F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.degreeVec(2.19F, -7.43F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(0.0F, -8.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3F, KeyframeAnimations.degreeVec(0.62F, -7.74F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4F, KeyframeAnimations.degreeVec(3.89F, -6.38F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.degreeVec(6.0F, -5.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(4.0F, -6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.05F, KeyframeAnimations.degreeVec(-0.8F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1F, KeyframeAnimations.degreeVec(-3.15F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.15F, KeyframeAnimations.degreeVec(-6.65F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2F, KeyframeAnimations.degreeVec(-11.01F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(-16.22F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(-22.37F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(-31.69F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(-43.77F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(-55.27F, 0.0F, -0.55F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-64.5F, 0.0F, -2.07F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(-70.43F, 0.0F, -4.38F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(-73.21F, 0.0F, -7.26F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(-74.41F, 0.0F, -10.53F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(-74.46F, 0.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-73.9F, 0.0F, -17.47F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(-75.97F, 0.0F, -20.74F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.degreeVec(-79.38F, 0.0F, -23.62F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(-77.24F, 0.0F, -25.93F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.degreeVec(-71.75F, 0.0F, -27.45F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-70.85F, 0.0F, -28.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(-71.55F, 0.0F, -28.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(-73.53F, 0.0F, -28.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.degreeVec(-74.91F, 0.0F, -28.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.45F, KeyframeAnimations.degreeVec(-73.51F, 0.0F, -28.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(-73.82F, 0.0F, -28.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.posVec(0.0F, 0.089F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.331F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.posVec(0.0F, 0.694F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.posVec(0.0F, 1.142F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.posVec(0.0F, 1.642F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.posVec(0.0F, 2.158F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.posVec(0.0F, 2.658F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.posVec(0.0F, 3.106F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.posVec(0.0F, 3.469F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.posVec(0.0F, 3.711F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.posVec(0.0F, 3.8F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.posVec(0.0F, 3.8F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.05F, KeyframeAnimations.degreeVec(1.03F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1F, KeyframeAnimations.degreeVec(4.02F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.15F, KeyframeAnimations.degreeVec(8.38F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2F, KeyframeAnimations.degreeVec(13.66F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(19.85F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(27.03F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(37.72F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(50.77F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(62.05F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(70.25F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(75.62F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(78.75F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(81.18F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(83.9F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(87.95F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(101.07F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.degreeVec(118.78F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(125.61F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.degreeVec(122.59F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(122.19F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(123.42F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(125.35F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.degreeVec(126.25F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(124.97F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.35F, KeyframeAnimations.degreeVec(124.33F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(124.74F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1F, KeyframeAnimations.degreeVec(-1.46F, -0.74F, 1.32F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2F, KeyframeAnimations.degreeVec(-4.83F, -1.91F, 3.88F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(-8.9F, -2.2F, 5.4F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(-11.71F, -1.96F, 4.71F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(-14.78F, -1.83F, 2.92F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(-17.03F, -2.06F, 1.34F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-18.49F, -2.6F, 1.19F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(-20.16F, -3.17F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(-22.71F, -3.51F, 6.36F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(-26.21F, -3.59F, 10.38F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(-30.55F, -3.19F, 14.85F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-35.48F, -1.98F, 19.52F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(-41.76F, 2.16F, 23.93F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.degreeVec(-46.9F, 8.52F, 26.53F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(-49.04F, 12.91F, 27.6F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.degreeVec(-48.6F, 13.95F, 28.11F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-47.49F, 13.5F, 27.76F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(-47.49F, 13.3F, 27.23F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(-47.63F, 13.32F, 28.21F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.degreeVec(-47.5F, 13.49F, 27.98F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(-47.54F, 13.48F, 28.01F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.05F, KeyframeAnimations.degreeVec(-3.55F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1F, KeyframeAnimations.degreeVec(-10.53F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.15F, KeyframeAnimations.degreeVec(-17.88F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2F, KeyframeAnimations.degreeVec(-24.9F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(-31.36F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(-37.06F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(-43.05F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(-50.12F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(-56.76F, 0.0F, 0.55F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-61.93F, 0.0F, 2.07F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(-65.73F, 0.0F, 4.38F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(-67.97F, 0.0F, 7.26F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(-68.65F, 0.0F, 10.53F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(-68.32F, 0.0F, 14.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-67.86F, 0.0F, 17.47F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(-72.41F, 0.0F, 20.74F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.degreeVec(-80.2F, 0.0F, 23.62F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(-80.87F, 0.0F, 25.93F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.degreeVec(-73.92F, 0.0F, 27.45F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-70.47F, 0.0F, 28.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(-69.75F, 0.0F, 28.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(-71.96F, 0.0F, 28.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.degreeVec(-73.97F, 0.0F, 28.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.degreeVec(-74.61F, 0.0F, 28.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.45F, KeyframeAnimations.degreeVec(-73.51F, 0.0F, 28.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(-73.82F, 0.0F, 28.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.posVec(0.0F, 0.089F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.331F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.posVec(0.0F, 0.694F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.posVec(0.0F, 1.142F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.posVec(0.0F, 1.642F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.posVec(0.0F, 2.158F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.posVec(0.0F, 2.658F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.posVec(0.0F, 3.106F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.posVec(0.0F, 3.469F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.posVec(0.0F, 3.711F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.posVec(0.0F, 3.8F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.posVec(0.0F, 3.8F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.05F, KeyframeAnimations.degreeVec(4.91F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1F, KeyframeAnimations.degreeVec(14.2F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.15F, KeyframeAnimations.degreeVec(23.41F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2F, KeyframeAnimations.degreeVec(31.65F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(38.78F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(44.73F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(50.46F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(56.64F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(61.81F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(65.33F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(68.59F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(71.68F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(74.32F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(77.4F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(82.09F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(98.29F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.degreeVec(120.37F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(129.55F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.degreeVec(125.08F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(121.74F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(121.35F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(123.6F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.degreeVec(125.21F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.35F, KeyframeAnimations.degreeVec(124.33F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(124.74F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.05F, KeyframeAnimations.degreeVec(-1.52F, -0.21F, 0.38F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1F, KeyframeAnimations.degreeVec(-4.27F, -0.68F, 1.35F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.15F, KeyframeAnimations.degreeVec(-6.81F, -1.19F, 2.66F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(-10.59F, -1.89F, 5.06F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(-11.9F, -1.92F, 5.51F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(-13.09F, -1.84F, 4.76F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(-14.31F, -1.85F, 2.91F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(-15.29F, -1.74F, 0.06F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-16.12F, -1.18F, -3.55F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(-17.78F, -0.15F, -7.02F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(-20.82F, 1.17F, -10.46F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(-25.01F, 2.33F, -14.48F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(-30.03F, 2.73F, -18.85F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-35.38F, 1.82F, -23.11F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(-42.2F, -2.21F, -25.81F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.degreeVec(-47.87F, -7.66F, -26.02F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(-50.12F, -11.25F, -25.98F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.degreeVec(-49.41F, -13.01F, -27.07F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-47.33F, -13.66F, -27.95F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(-46.81F, -14.04F, -28.1F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(-47.14F, -14.03F, -28.07F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(-47.63F, -13.32F, -28.21F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(-47.54F, -13.48F, -28.01F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(0.3465F, KeyframeAnimations.posVec(0.0F, -0.5F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(0.495F, KeyframeAnimations.posVec(0.0F, -0.5F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(0.505F, KeyframeAnimations.posVec(0.0F, -0.15F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(0.99F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(1.8F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
				))
				.addAnimation("right_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(0.3465F, KeyframeAnimations.posVec(0.0F, -0.5F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(0.495F, KeyframeAnimations.posVec(0.0F, -0.5F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(0.505F, KeyframeAnimations.posVec(0.0F, -0.15F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(0.99F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(1.8F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
				))
				.build();
	}

	private static final class SedatedRest {
		private static final AnimationDefinition DEFINITION = AnimationDefinition.Builder.withLength(4.0F).looping()
				.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -13.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, -12.88F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, -13.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.0F, KeyframeAnimations.posVec(0.0F, -13.12F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.posVec(0.0F, -13.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.SCALE,
						new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.01F, 1.0175F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.scaleVec(1.02F, 1.035F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.scaleVec(1.01F, 1.0175F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.scaleVec(1.01F, 1.0175F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("body2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-0.9F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.9F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(96.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(96.5F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.degreeVec(96.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.0F, KeyframeAnimations.degreeVec(95.5F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.degreeVec(96.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-92.0F, 4.0F, 9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.degreeVec(-92.0F, 4.0F, 9.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-60.0F, 0.0F, 20.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(-60.0F, 0.0F, 20.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(-60.0F, 0.0F, 20.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(-45.0F, 0.0F, 27.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-64.0F, 0.0F, 17.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(-52.0F, 0.0F, 23.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(-61.5F, 0.0F, 19.4F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4F, KeyframeAnimations.degreeVec(-60.0F, 0.0F, 20.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.55F, KeyframeAnimations.degreeVec(-60.0F, 0.0F, 20.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.degreeVec(-60.0F, 0.0F, 20.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-66.0F, 0.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.3F, KeyframeAnimations.degreeVec(-66.0F, 0.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.4F, KeyframeAnimations.degreeVec(-66.0F, 0.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.5F, KeyframeAnimations.degreeVec(-51.0F, 0.0F, -22.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.6F, KeyframeAnimations.degreeVec(-70.0F, 0.0F, -11.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.7F, KeyframeAnimations.degreeVec(-58.0F, 0.0F, -18.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.85F, KeyframeAnimations.degreeVec(-67.5F, 0.0F, -13.4F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.0F, KeyframeAnimations.degreeVec(-66.0F, 0.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.15F, KeyframeAnimations.degreeVec(-66.0F, 0.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.degreeVec(-66.0F, 0.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-75.43F, 0.0F, -7.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.degreeVec(-75.43F, 0.0F, -7.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-75.19F, 0.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.degreeVec(-75.19F, 0.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-17.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.degreeVec(-17.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(4.0F, -6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.degreeVec(4.0F, -6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-73.82F, 0.0F, -28.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.degreeVec(-73.82F, 0.0F, -28.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 3.8F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.posVec(0.0F, 3.8F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(124.74F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.degreeVec(124.74F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-47.54F, 13.48F, 28.01F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6F, KeyframeAnimations.degreeVec(-47.54F, 13.48F, 28.01F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.7F, KeyframeAnimations.degreeVec(-47.54F, 13.48F, 28.01F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(-59.54F, 13.48F, 28.01F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.9F, KeyframeAnimations.degreeVec(-49.54F, 13.48F, 28.01F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.degreeVec(-55.54F, 13.48F, 28.01F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.15F, KeyframeAnimations.degreeVec(-48.34F, 13.48F, 28.01F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.3F, KeyframeAnimations.degreeVec(-47.54F, 13.48F, 28.01F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.45F, KeyframeAnimations.degreeVec(-47.54F, 13.48F, 28.01F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.degreeVec(-47.54F, 13.48F, 28.01F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-73.82F, 0.0F, 28.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.degreeVec(-73.82F, 0.0F, 28.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 3.8F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.posVec(0.0F, 3.8F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(124.74F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.degreeVec(124.74F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-47.54F, -13.48F, -28.01F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.degreeVec(-47.54F, -13.48F, -28.01F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(4.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
				))
				.addAnimation("right_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(4.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
				))
				.build();
	}

	private static final class SedatedWake {
		private static final AnimationDefinition DEFINITION = AnimationDefinition.Builder.withLength(2.2F)
				.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(3.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1F, KeyframeAnimations.degreeVec(2.4F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(-1.75F, 0.0F, 0.1F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(-4.55F, 0.0F, 0.35F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(-7.58F, 0.0F, 0.65F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(-10.01F, 0.0F, 0.9F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-11.0F, 0.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(-10.27F, 0.0F, 0.3F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(-8.75F, 0.0F, -0.79F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(-7.15F, 0.0F, -0.9F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(-5.08F, 0.0F, -0.35F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(-5.35F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(-8.42F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.degreeVec(-11.79F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.35F, KeyframeAnimations.degreeVec(-16.75F, 0.0F, 1.27F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.45F, KeyframeAnimations.degreeVec(-16.49F, 0.0F, 0.95F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.55F, KeyframeAnimations.degreeVec(-13.6F, 0.0F, -1.45F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.65F, KeyframeAnimations.degreeVec(-10.32F, 0.0F, -1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.degreeVec(-6.54F, 0.0F, 0.7F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 1.2F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.9F, KeyframeAnimations.degreeVec(-2.56F, 0.0F, 0.4F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.degreeVec(-1.0F, 0.0F, -0.4F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -13.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.05F, KeyframeAnimations.posVec(0.0F, -12.978F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.15F, KeyframeAnimations.posVec(0.0F, -12.83F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, -12.6F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.posVec(0.0F, -12.424F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.posVec(0.0F, -12.165F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.posVec(0.0F, -11.854F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.posVec(0.0F, -11.522F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, -11.2F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.posVec(0.0F, -10.9F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.posVec(0.0F, -10.6F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.posVec(0.0F, -10.281F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.posVec(0.0F, -9.921F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.posVec(0.0F, -9.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.posVec(0.0F, -8.973F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.posVec(0.0F, -8.328F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.posVec(0.0F, -7.596F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.posVec(0.0F, -6.81F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, -6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.posVec(0.0F, -5.041F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, -3.937F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.posVec(0.0F, -2.914F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, -2.2F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.posVec(0.0F, -1.747F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3F, KeyframeAnimations.posVec(0.0F, -1.369F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.35F, KeyframeAnimations.posVec(0.0F, -1.081F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4F, KeyframeAnimations.posVec(0.0F, -0.9F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.45F, KeyframeAnimations.posVec(0.0F, -0.8F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6F, KeyframeAnimations.posVec(0.0F, -0.6F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.posVec(0.0F, -0.325F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, -0.05F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.2F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.SCALE,
						new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.01F, 1.0175F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.scaleVec(1.02F, 1.03F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.scaleVec(1.0148F, 1.0222F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.scaleVec(1.0052F, 1.0078F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.2F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("body2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2F, KeyframeAnimations.degreeVec(-2.3F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(-2.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.degreeVec(-4.81F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.35F, KeyframeAnimations.degreeVec(-5.9F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.45F, KeyframeAnimations.degreeVec(-5.67F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 6.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.55F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6F, KeyframeAnimations.degreeVec(-3.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.65F, KeyframeAnimations.degreeVec(-2.46F, 0.0F, -0.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.7F, KeyframeAnimations.degreeVec(-2.0F, 0.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.degreeVec(-1.5F, 0.0F, 0.75F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(-1.0F, 0.0F, -3.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.85F, KeyframeAnimations.degreeVec(-0.37F, 0.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.9F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -0.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(96.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.05F, KeyframeAnimations.degreeVec(95.31F, 4.94F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1F, KeyframeAnimations.degreeVec(93.45F, 4.78F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.15F, KeyframeAnimations.degreeVec(90.74F, 4.54F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2F, KeyframeAnimations.degreeVec(87.48F, 4.27F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(84.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(79.06F, 3.69F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(72.09F, 3.3F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(64.39F, 2.86F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(57.26F, 2.42F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(52.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(48.25F, 1.54F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(44.86F, 1.01F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(41.95F, 0.51F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(39.63F, 0.14F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(38.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(37.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.degreeVec(35.07F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(34.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(31.67F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(27.95F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.degreeVec(23.75F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(16.57F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3F, KeyframeAnimations.degreeVec(13.1F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.35F, KeyframeAnimations.degreeVec(10.07F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.45F, KeyframeAnimations.degreeVec(6.89F, 4.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.degreeVec(6.0F, 9.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.55F, KeyframeAnimations.degreeVec(4.92F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6F, KeyframeAnimations.degreeVec(4.0F, -11.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.65F, KeyframeAnimations.degreeVec(3.46F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.7F, KeyframeAnimations.degreeVec(3.0F, 9.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.degreeVec(2.5F, 1.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(2.0F, -6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.85F, KeyframeAnimations.degreeVec(1.46F, -1.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.9F, KeyframeAnimations.degreeVec(1.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.95F, KeyframeAnimations.degreeVec(0.71F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.5F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-92.0F, 4.0F, 9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.05F, KeyframeAnimations.degreeVec(-91.32F, 3.94F, 8.88F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1F, KeyframeAnimations.degreeVec(-89.48F, 3.78F, 8.57F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.15F, KeyframeAnimations.degreeVec(-86.78F, 3.54F, 8.12F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2F, KeyframeAnimations.degreeVec(-83.52F, 3.27F, 7.57F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(-80.0F, 3.0F, 7.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(-74.76F, 2.66F, 6.21F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(-67.18F, 2.23F, 5.1F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(-58.82F, 1.77F, 3.9F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(-51.24F, 1.34F, 2.79F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-46.0F, 1.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(-42.67F, 0.73F, 1.43F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(-39.8F, 0.46F, 0.88F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(-37.39F, 0.22F, 0.43F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(-35.45F, 0.06F, 0.12F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-34.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(-33.03F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.degreeVec(-31.08F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(-27.67F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(-23.95F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.degreeVec(-19.75F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.degreeVec(-16.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(-12.57F, 0.42F, 1.25F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3F, KeyframeAnimations.degreeVec(-9.1F, 1.63F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.35F, KeyframeAnimations.degreeVec(-6.07F, 3.52F, 6.75F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4F, KeyframeAnimations.degreeVec(-4.0F, 6.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.45F, KeyframeAnimations.degreeVec(-2.89F, 14.68F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.degreeVec(-2.0F, 22.0F, -12.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.55F, KeyframeAnimations.degreeVec(-0.75F, -1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6F, KeyframeAnimations.degreeVec(0.0F, -24.0F, 14.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.65F, KeyframeAnimations.degreeVec(0.0F, -2.0F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.7F, KeyframeAnimations.degreeVec(0.0F, 20.0F, -11.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.degreeVec(0.0F, 3.5F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(0.0F, -13.0F, 7.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.85F, KeyframeAnimations.degreeVec(0.0F, -3.5F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.9F, KeyframeAnimations.degreeVec(0.0F, 6.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.95F, KeyframeAnimations.degreeVec(0.0F, 2.25F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, -1.5F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.05F, KeyframeAnimations.degreeVec(0.0F, -1.27F, 0.84F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-60.0F, 0.0F, 20.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.15F, KeyframeAnimations.degreeVec(-59.24F, 0.0F, 19.6F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(-58.0F, 0.0F, 19.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(-55.83F, 0.0F, 18.3F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(-51.42F, 0.0F, 17.04F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(-45.69F, 0.0F, 15.43F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(-39.57F, 0.0F, 13.68F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-34.0F, 0.0F, 12.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(-28.63F, 0.0F, 10.26F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(-22.83F, 0.0F, 8.32F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(-17.12F, 0.0F, 6.34F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(-12.01F, 0.0F, 4.51F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(-4.65F, 0.0F, 1.66F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.degreeVec(-1.41F, 0.0F, 0.31F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(1.35F, 0.0F, -0.86F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.degreeVec(3.28F, 0.0F, -1.69F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(4.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(3.29F, 0.0F, -1.83F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.degreeVec(-0.39F, 0.0F, -0.73F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(-2.98F, 0.0F, 1.36F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.35F, KeyframeAnimations.degreeVec(-4.64F, 0.0F, 5.2F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, 6.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.45F, KeyframeAnimations.degreeVec(-10.43F, 0.0F, -8.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, -22.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.55F, KeyframeAnimations.degreeVec(-9.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, 30.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.65F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.7F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, -26.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.degreeVec(-9.5F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, 18.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.85F, KeyframeAnimations.degreeVec(-4.3F, 0.0F, 4.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.9F, KeyframeAnimations.degreeVec(-3.0F, 0.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.95F, KeyframeAnimations.degreeVec(-1.81F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.degreeVec(-1.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.05F, KeyframeAnimations.degreeVec(-0.6F, 0.0F, 2.53F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.15F, KeyframeAnimations.degreeVec(-0.08F, 0.0F, 0.47F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-66.0F, 0.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1F, KeyframeAnimations.degreeVec(-65.65F, 0.0F, -13.79F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(-64.0F, 0.0F, -13.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(-62.03F, 0.0F, -12.62F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(-58.09F, 0.0F, -12.11F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(-52.94F, 0.0F, -11.49F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(-47.32F, 0.0F, -10.78F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-42.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(-36.59F, 0.0F, -9.04F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(-30.55F, 0.0F, -7.84F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(-24.42F, 0.0F, -6.51F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(-18.73F, 0.0F, -5.19F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-14.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(-9.73F, 0.0F, -2.78F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.degreeVec(-5.44F, 0.0F, -1.45F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(-1.68F, 0.0F, -0.24F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.degreeVec(0.99F, 0.0F, 0.65F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(2.0F, 0.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(0.5F, 0.0F, 0.71F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.degreeVec(-2.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(-2.89F, 0.0F, -1.18F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.35F, KeyframeAnimations.degreeVec(-4.67F, 0.0F, -5.14F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.45F, KeyframeAnimations.degreeVec(-9.4F, 0.0F, 9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 24.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.55F, KeyframeAnimations.degreeVec(-9.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, -28.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.65F, KeyframeAnimations.degreeVec(-9.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.7F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 24.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.degreeVec(-9.5F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, -16.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.85F, KeyframeAnimations.degreeVec(-4.3F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.9F, KeyframeAnimations.degreeVec(-3.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.95F, KeyframeAnimations.degreeVec(-1.81F, 0.0F, 2.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.degreeVec(-1.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.05F, KeyframeAnimations.degreeVec(-0.6F, 0.0F, -2.53F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.15F, KeyframeAnimations.degreeVec(-0.08F, 0.0F, -0.47F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-75.43F, 0.0F, -7.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.05F, KeyframeAnimations.degreeVec(-74.7F, 0.0F, -6.94F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1F, KeyframeAnimations.degreeVec(-72.76F, 0.0F, -6.78F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.15F, KeyframeAnimations.degreeVec(-69.94F, 0.0F, -6.54F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2F, KeyframeAnimations.degreeVec(-66.62F, 0.0F, -6.27F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(-63.12F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(-57.53F, 0.0F, -5.66F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(-48.75F, 0.0F, -5.23F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(-38.49F, 0.0F, -4.77F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(-28.78F, 0.0F, -4.34F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-22.45F, 0.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(-20.11F, 0.0F, -3.76F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(-19.91F, 0.0F, -3.55F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(-20.48F, 0.0F, -3.37F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(-20.37F, 0.0F, -3.19F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-19.96F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(-19.58F, 0.0F, -1.77F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(-13.87F, 0.0F, -1.51F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.degreeVec(-7.23F, 0.0F, -1.26F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.degreeVec(-2.0F, 0.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(1.98F, 0.0F, -0.77F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3F, KeyframeAnimations.degreeVec(5.88F, 0.0F, -0.54F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.35F, KeyframeAnimations.degreeVec(8.83F, 0.0F, -0.3F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.55F, KeyframeAnimations.degreeVec(8.71F, 0.0F, 1.75F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.65F, KeyframeAnimations.degreeVec(6.97F, 0.0F, 1.53F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.degreeVec(4.12F, 0.0F, -0.53F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(3.0F, 0.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.9F, KeyframeAnimations.degreeVec(1.44F, 0.0F, -0.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-75.19F, 0.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.05F, KeyframeAnimations.degreeVec(-74.46F, 0.0F, 4.93F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1F, KeyframeAnimations.degreeVec(-72.51F, 0.0F, 4.74F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.15F, KeyframeAnimations.degreeVec(-69.7F, 0.0F, 4.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2F, KeyframeAnimations.degreeVec(-66.37F, 0.0F, 4.23F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(-62.88F, 0.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(-57.22F, 0.0F, 3.8F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(-48.25F, 0.0F, 3.6F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(-37.68F, 0.0F, 3.4F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(-27.59F, 0.0F, 3.2F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-20.96F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(-18.8F, 0.0F, 2.8F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(-19.92F, 0.0F, 2.4F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(-21.45F, 0.0F, 2.2F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-21.02F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(-18.21F, 0.0F, 1.77F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.degreeVec(-18.7F, 0.0F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(-17.54F, 0.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(-11.77F, 0.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.degreeVec(-5.12F, 0.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(3.71F, 0.0F, 0.91F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3F, KeyframeAnimations.degreeVec(7.27F, 0.0F, 0.67F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.35F, KeyframeAnimations.degreeVec(9.95F, 0.0F, 0.34F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4F, KeyframeAnimations.degreeVec(11.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.degreeVec(9.56F, 0.0F, -1.17F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6F, KeyframeAnimations.degreeVec(7.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.65F, KeyframeAnimations.degreeVec(5.7F, 0.0F, -1.53F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.degreeVec(2.9F, 0.0F, 0.53F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(2.0F, 0.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.95F, KeyframeAnimations.degreeVec(0.73F, 0.0F, 0.16F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-17.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.05F, KeyframeAnimations.degreeVec(-16.84F, 4.94F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.15F, KeyframeAnimations.degreeVec(-15.34F, 4.54F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(-13.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(-10.73F, 3.69F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(-7.13F, 3.3F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(-3.28F, 2.86F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(-0.25F, 2.42F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.85F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(-0.64F, 1.01F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(-3.35F, 0.14F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(-6.46F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(-7.14F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(-7.61F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.degreeVec(-12.11F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.degreeVec(-18.79F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(-23.14F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.35F, KeyframeAnimations.degreeVec(-25.4F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4F, KeyframeAnimations.degreeVec(-25.96F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.45F, KeyframeAnimations.degreeVec(-25.78F, -0.94F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.degreeVec(-24.99F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.55F, KeyframeAnimations.degreeVec(-21.53F, -5.06F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6F, KeyframeAnimations.degreeVec(-14.99F, -6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.65F, KeyframeAnimations.degreeVec(-7.38F, -4.28F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.7F, KeyframeAnimations.degreeVec(-0.39F, -0.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.degreeVec(2.91F, 3.28F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(3.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.85F, KeyframeAnimations.degreeVec(2.75F, 3.98F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.9F, KeyframeAnimations.degreeVec(2.17F, 1.75F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.95F, KeyframeAnimations.degreeVec(1.5F, -0.48F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.degreeVec(1.0F, -1.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(4.0F, -6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.15F, KeyframeAnimations.degreeVec(3.14F, -5.57F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(2.0F, -5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(-0.26F, -3.96F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(-3.09F, -2.57F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(-5.14F, -0.95F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(-5.89F, -0.13F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(-5.81F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(-4.41F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-2.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(0.99F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(6.09F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.degreeVec(11.64F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.degreeVec(16.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(19.37F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3F, KeyframeAnimations.degreeVec(22.61F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.35F, KeyframeAnimations.degreeVec(25.04F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4F, KeyframeAnimations.degreeVec(26.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.45F, KeyframeAnimations.degreeVec(24.0F, 0.62F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.degreeVec(19.33F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.55F, KeyframeAnimations.degreeVec(14.0F, 3.38F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6F, KeyframeAnimations.degreeVec(10.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.65F, KeyframeAnimations.degreeVec(7.33F, 2.13F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.7F, KeyframeAnimations.degreeVec(4.89F, -2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.degreeVec(3.0F, -6.12F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(2.0F, -8.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.85F, KeyframeAnimations.degreeVec(1.64F, -6.28F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.9F, KeyframeAnimations.degreeVec(1.4F, -2.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.95F, KeyframeAnimations.degreeVec(1.21F, 1.28F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.degreeVec(1.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-73.82F, 0.0F, -28.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.05F, KeyframeAnimations.degreeVec(-73.45F, 0.0F, -28.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.15F, KeyframeAnimations.degreeVec(-70.99F, 0.0F, -28.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(-67.19F, 0.0F, -28.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(-64.16F, 0.0F, -28.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(-61.01F, 0.0F, -27.69F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(-60.43F, 0.0F, -26.8F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(-63.37F, 0.0F, -25.42F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-70.03F, 0.0F, -23.62F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(-79.52F, 0.0F, -21.51F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(-89.44F, 0.0F, -19.14F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(-97.21F, 0.0F, -16.61F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(-100.21F, 0.0F, -14.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-98.97F, 0.0F, -11.39F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(-94.89F, 0.0F, -8.86F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.degreeVec(-89.34F, 0.0F, -6.49F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(-83.42F, 0.0F, -4.37F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.degreeVec(-77.78F, 0.0F, -2.58F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-71.76F, 0.0F, -1.2F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(-61.52F, 0.0F, -0.31F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(-47.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.degreeVec(-32.11F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.degreeVec(-20.95F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(-13.77F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3F, KeyframeAnimations.degreeVec(-7.69F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.35F, KeyframeAnimations.degreeVec(-2.97F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4F, KeyframeAnimations.degreeVec(-0.01F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.45F, KeyframeAnimations.degreeVec(1.54F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.55F, KeyframeAnimations.degreeVec(3.29F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6F, KeyframeAnimations.degreeVec(3.42F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.65F, KeyframeAnimations.degreeVec(1.78F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.7F, KeyframeAnimations.degreeVec(-1.21F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.degreeVec(-3.97F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(-5.31F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.85F, KeyframeAnimations.degreeVec(-4.99F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.9F, KeyframeAnimations.degreeVec(-3.63F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.95F, KeyframeAnimations.degreeVec(-1.6F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.05F, KeyframeAnimations.degreeVec(0.45F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 3.8F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.posVec(0.0F, 3.8F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.posVec(0.0F, 3.766F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.posVec(0.0F, 3.67F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.posVec(0.0F, 3.519F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.posVec(0.0F, 3.083F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.posVec(0.0F, 2.815F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.posVec(0.0F, 2.523F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.posVec(0.0F, 2.215F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.posVec(0.0F, 1.9F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.posVec(0.0F, 1.585F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.posVec(0.0F, 1.277F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.posVec(0.0F, 0.985F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.posVec(0.0F, 0.717F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.posVec(0.0F, 0.281F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, 0.13F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.posVec(0.0F, 0.034F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.2F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(124.74F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.05F, KeyframeAnimations.degreeVec(124.49F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.15F, KeyframeAnimations.degreeVec(122.84F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(120.29F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(117.22F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(118.08F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(121.46F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(126.34F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(131.65F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(136.04F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(138.06F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(136.56F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(132.31F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(126.11F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.degreeVec(118.72F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(111.24F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.degreeVec(104.64F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(98.43F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(89.82F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(78.26F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.degreeVec(65.64F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.degreeVec(55.09F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(47.58F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3F, KeyframeAnimations.degreeVec(41.07F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.35F, KeyframeAnimations.degreeVec(35.77F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4F, KeyframeAnimations.degreeVec(32.01F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.45F, KeyframeAnimations.degreeVec(28.79F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.degreeVec(24.93F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.55F, KeyframeAnimations.degreeVec(20.8F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6F, KeyframeAnimations.degreeVec(17.59F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.65F, KeyframeAnimations.degreeVec(16.87F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.degreeVec(18.27F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(17.36F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.85F, KeyframeAnimations.degreeVec(14.51F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.9F, KeyframeAnimations.degreeVec(10.27F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.95F, KeyframeAnimations.degreeVec(5.46F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.degreeVec(1.9F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.05F, KeyframeAnimations.degreeVec(0.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.1F, KeyframeAnimations.degreeVec(-0.04F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-47.54F, 13.48F, 28.01F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(-46.7F, 14.03F, 27.1F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(-45.58F, 14.37F, 25.13F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(-43.39F, 13.17F, 21.18F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-41.44F, 11.67F, 19.58F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(-38.26F, 9.7F, 18.43F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(-33.99F, 7.6F, 17.34F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(-29.61F, 5.72F, 15.99F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(-26.43F, 4.38F, 14.19F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-24.59F, 3.47F, 11.93F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(-23.64F, 2.76F, 9.37F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(-22.65F, 1.56F, 4.45F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-22.67F, 0.54F, 1.08F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(-22.83F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(-18.69F, -0.08F, -0.22F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.35F, KeyframeAnimations.degreeVec(-16.04F, -0.35F, -1.22F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.45F, KeyframeAnimations.degreeVec(-13.84F, -0.23F, -0.93F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.55F, KeyframeAnimations.degreeVec(-10.48F, 0.26F, 1.43F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6F, KeyframeAnimations.degreeVec(-9.01F, 0.31F, 1.98F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.65F, KeyframeAnimations.degreeVec(-8.33F, 0.22F, 1.48F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.degreeVec(-7.76F, -0.09F, -0.69F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.85F, KeyframeAnimations.degreeVec(-5.78F, -0.1F, -0.95F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.degreeVec(-0.9F, 0.01F, 0.4F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-73.82F, 0.0F, 28.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.05F, KeyframeAnimations.degreeVec(-73.45F, 0.0F, 28.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.15F, KeyframeAnimations.degreeVec(-70.99F, 0.0F, 28.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(-67.19F, 0.0F, 28.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(-63.86F, 0.0F, 28.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(-60.0F, 0.0F, 27.69F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(-58.56F, 0.0F, 26.8F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(-60.69F, 0.0F, 25.42F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-66.81F, 0.0F, 23.62F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(-76.63F, 0.0F, 21.51F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(-88.18F, 0.0F, 19.14F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(-98.55F, 0.0F, 16.61F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(-103.77F, 0.0F, 14.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-102.94F, 0.0F, 11.39F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(-97.89F, 0.0F, 8.86F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.degreeVec(-91.17F, 0.0F, 6.49F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(-84.28F, 0.0F, 4.37F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.degreeVec(-78.01F, 0.0F, 2.58F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-71.76F, 0.0F, 1.2F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(-61.52F, 0.0F, 0.31F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(-47.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.degreeVec(-32.11F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.degreeVec(-20.95F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(-13.27F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3F, KeyframeAnimations.degreeVec(-5.9F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.35F, KeyframeAnimations.degreeVec(0.41F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4F, KeyframeAnimations.degreeVec(4.4F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.45F, KeyframeAnimations.degreeVec(4.53F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.degreeVec(1.83F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.55F, KeyframeAnimations.degreeVec(-1.44F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6F, KeyframeAnimations.degreeVec(-3.44F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.65F, KeyframeAnimations.degreeVec(-3.69F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.7F, KeyframeAnimations.degreeVec(-2.84F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(0.2F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.85F, KeyframeAnimations.degreeVec(-0.05F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.95F, KeyframeAnimations.degreeVec(-2.35F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.05F, KeyframeAnimations.degreeVec(-2.29F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 3.8F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.posVec(0.0F, 3.8F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.posVec(0.0F, 3.766F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.posVec(0.0F, 3.67F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.posVec(0.0F, 3.519F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.posVec(0.0F, 3.083F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.posVec(0.0F, 2.815F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.posVec(0.0F, 2.523F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.posVec(0.0F, 2.215F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.posVec(0.0F, 1.9F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.posVec(0.0F, 1.585F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.posVec(0.0F, 1.277F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.posVec(0.0F, 0.985F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.posVec(0.0F, 0.717F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.posVec(0.0F, 0.281F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, 0.13F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.posVec(0.0F, 0.034F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.2F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(124.74F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.05F, KeyframeAnimations.degreeVec(124.49F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.15F, KeyframeAnimations.degreeVec(122.84F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(120.29F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3F, KeyframeAnimations.degreeVec(118.23F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(116.01F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4F, KeyframeAnimations.degreeVec(115.89F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(118.51F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(123.11F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(129.08F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(135.05F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(139.03F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(139.04F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(135.16F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8F, KeyframeAnimations.degreeVec(128.41F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.degreeVec(120.25F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(112.01F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.95F, KeyframeAnimations.degreeVec(104.86F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(98.43F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.05F, KeyframeAnimations.degreeVec(89.82F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(78.26F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.degreeVec(65.64F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2F, KeyframeAnimations.degreeVec(55.09F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(46.86F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3F, KeyframeAnimations.degreeVec(38.49F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.35F, KeyframeAnimations.degreeVec(30.82F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4F, KeyframeAnimations.degreeVec(25.52F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.45F, KeyframeAnimations.degreeVec(24.39F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.55F, KeyframeAnimations.degreeVec(27.77F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6F, KeyframeAnimations.degreeVec(27.72F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.65F, KeyframeAnimations.degreeVec(24.96F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.7F, KeyframeAnimations.degreeVec(20.12F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.degreeVec(14.1F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(9.18F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.85F, KeyframeAnimations.degreeVec(7.16F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.degreeVec(6.02F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.05F, KeyframeAnimations.degreeVec(4.6F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.15F, KeyframeAnimations.degreeVec(1.05F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-47.54F, -13.48F, -28.01F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(-46.7F, -14.03F, -27.1F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.35F, KeyframeAnimations.degreeVec(-45.11F, -14.77F, -25.68F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.45F, KeyframeAnimations.degreeVec(-42.48F, -14.27F, -22.57F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-40.77F, -12.97F, -21.1F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.55F, KeyframeAnimations.degreeVec(-38.12F, -10.77F, -19.63F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6F, KeyframeAnimations.degreeVec(-34.11F, -8.02F, -17.8F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.65F, KeyframeAnimations.degreeVec(-29.36F, -5.33F, -15.51F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7F, KeyframeAnimations.degreeVec(-25.61F, -3.43F, -12.84F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-23.71F, -2.44F, -10.16F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.85F, KeyframeAnimations.degreeVec(-22.83F, -1.59F, -5.63F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9F, KeyframeAnimations.degreeVec(-22.58F, -1.29F, -3.8F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-22.67F, -0.54F, -1.08F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1F, KeyframeAnimations.degreeVec(-22.83F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.15F, KeyframeAnimations.degreeVec(-21.75F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(-18.48F, -0.07F, -0.22F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.35F, KeyframeAnimations.degreeVec(-14.48F, -0.32F, -1.23F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4F, KeyframeAnimations.degreeVec(-12.91F, -0.34F, -1.46F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.45F, KeyframeAnimations.degreeVec(-12.42F, -0.2F, -0.93F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.55F, KeyframeAnimations.degreeVec(-12.73F, 0.32F, 1.42F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6F, KeyframeAnimations.degreeVec(-12.27F, 0.43F, 1.95F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.65F, KeyframeAnimations.degreeVec(-10.94F, 0.28F, 1.47F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.7F, KeyframeAnimations.degreeVec(-8.86F, 0.06F, 0.4F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.degreeVec(-6.41F, -0.08F, -0.7F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8F, KeyframeAnimations.degreeVec(-4.38F, -0.09F, -1.2F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.85F, KeyframeAnimations.degreeVec(-3.37F, -0.06F, -0.95F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.degreeVec(-2.27F, 0.02F, 0.4F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(0.44F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(0.77F, KeyframeAnimations.posVec(0.0F, -0.45F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(0.924F, KeyframeAnimations.posVec(0.0F, -0.45F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(0.934F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(1.11F, KeyframeAnimations.posVec(0.0F, -0.2F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(1.32F, KeyframeAnimations.posVec(0.0F, -0.2F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(1.33F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(1.452F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(1.462F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(2.2F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
				))
				.addAnimation("right_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(0.44F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(0.77F, KeyframeAnimations.posVec(0.0F, -0.45F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(0.924F, KeyframeAnimations.posVec(0.0F, -0.45F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(0.934F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(1.11F, KeyframeAnimations.posVec(0.0F, -0.2F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(1.32F, KeyframeAnimations.posVec(0.0F, -0.2F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(1.33F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(1.452F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(1.462F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
						new Keyframe(2.2F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
				))
				.build();
	}

	private static final class AttackSecondary {
		private static final AnimationDefinition DEFINITION = AnimationDefinition.Builder.withLength(15.0F)
				.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.0F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.0F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.0F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.0F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.0F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(15.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.5F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.5F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.5F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.5F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.5F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.5F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.5F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.5F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.5F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.5F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.5F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.5F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.5F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(15.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.5F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.5F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.5F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.5F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.5F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.5F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.5F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.5F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.5F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.5F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.5F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.5F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.5F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(15.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-70.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.degreeVec(-70.0F, 0.0F, -15.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.degreeVec(-70.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.0F, KeyframeAnimations.degreeVec(-70.0F, 0.0F, -15.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.0F, KeyframeAnimations.degreeVec(-70.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.0F, KeyframeAnimations.degreeVec(-70.0F, 0.0F, -15.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.0F, KeyframeAnimations.degreeVec(-70.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.5833F, KeyframeAnimations.degreeVec(-70.0F, 0.0F, -15.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(15.0F, KeyframeAnimations.degreeVec(-70.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(15.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(78.059F, 10.4335F, -31.0942F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(92.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.25F, KeyframeAnimations.degreeVec(78.059F, -10.4335F, 31.0942F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.25F, KeyframeAnimations.degreeVec(92.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0833F, KeyframeAnimations.degreeVec(78.059F, 10.4335F, -31.0942F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.0833F, KeyframeAnimations.degreeVec(92.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.0833F, KeyframeAnimations.degreeVec(78.059F, -10.4335F, 31.0942F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.0833F, KeyframeAnimations.degreeVec(92.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.9167F, KeyframeAnimations.degreeVec(78.059F, 10.4335F, -31.0942F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.9167F, KeyframeAnimations.degreeVec(92.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.9167F, KeyframeAnimations.degreeVec(78.059F, -10.4335F, 31.0942F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.9167F, KeyframeAnimations.degreeVec(92.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.75F, KeyframeAnimations.degreeVec(78.059F, 10.4335F, -31.0942F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.5833F, KeyframeAnimations.degreeVec(92.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.4583F, KeyframeAnimations.degreeVec(78.059F, -10.4335F, 31.0942F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.25F, KeyframeAnimations.degreeVec(92.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.9583F, KeyframeAnimations.degreeVec(78.059F, 10.4335F, -31.0942F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0833F, KeyframeAnimations.posVec(0.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.0833F, KeyframeAnimations.posVec(0.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.9167F, KeyframeAnimations.posVec(0.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.9167F, KeyframeAnimations.posVec(0.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.75F, KeyframeAnimations.posVec(0.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.4583F, KeyframeAnimations.posVec(0.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.9583F, KeyframeAnimations.posVec(0.0F, 0.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.2083F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.625F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0417F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3333F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4583F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5833F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.875F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.1667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.2917F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.4167F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.5833F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.7083F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.8333F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.0F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.125F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.25F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.4167F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.5417F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.6667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.8333F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.9583F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0833F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.25F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.375F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.5F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.6667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.7917F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.9167F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.0833F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.2083F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.3333F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.5F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.625F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.75F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.9167F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.0417F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.1667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.3333F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.4583F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.5833F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.75F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.875F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.0F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.1667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.2917F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.4167F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.5833F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.7083F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.8333F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.0F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.125F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.25F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.4167F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.5417F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.6667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.8333F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.9583F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.0833F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.25F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.375F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.5F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.6667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.7917F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.9167F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.0833F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.2083F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.3333F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.5F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.625F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.75F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.9167F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.0417F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.1667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.3333F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.4583F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.5833F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.75F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.875F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.0F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.1667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.2917F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.4167F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.5833F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.7083F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.8333F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.0F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.125F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.25F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.4167F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.5417F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.6667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.8333F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.9583F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.0833F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.25F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.375F, KeyframeAnimations.degreeVec(-92.2279F, 14.8052F, -10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.5F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(15.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.125F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4167F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5417F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6667F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8333F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9583F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0833F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.375F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6667F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.7917F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.9167F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0833F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.2083F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.3333F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.5F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.625F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.75F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.9167F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.0417F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.1667F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.3333F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.4583F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.5833F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.75F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.875F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.1667F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.2917F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.4167F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.5833F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.7083F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.8333F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.0F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.125F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.25F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.4167F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.5417F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.6667F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.8333F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.9583F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.0833F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.25F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.375F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.5F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.6667F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.7917F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.9167F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.0833F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.2083F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.3333F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.5F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.625F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.75F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.9167F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.0417F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.1667F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.3333F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.4583F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.5833F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.75F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.875F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.0F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.1667F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.2917F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.4167F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.5833F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.7083F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.8333F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.0F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.125F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.25F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.4167F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.5417F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.6667F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.8333F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.9583F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.0833F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.25F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.375F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.5F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.6667F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.7917F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.9167F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.0833F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.2083F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.3333F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.5F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.625F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.75F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.9167F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.0417F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.1667F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.3333F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.4583F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.5833F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.75F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.875F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.0F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.1667F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.2917F, KeyframeAnimations.posVec(1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.4167F, KeyframeAnimations.posVec(1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.9167F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.SCALE,
						new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.625F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9167F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0417F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4583F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5833F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.875F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.1667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.2917F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.4167F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.5833F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.7083F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.8333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.125F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.25F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.4167F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.5417F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.6667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.8333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.9583F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0833F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.25F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.375F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.5F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.6667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.7917F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.9167F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.0833F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.2083F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.3333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.5F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.625F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.75F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.9167F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.0417F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.1667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.3333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.4583F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.5833F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.75F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.875F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.1667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.2917F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.4167F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.5833F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.7083F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.8333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.125F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.25F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.4167F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.5417F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.6667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.8333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.9583F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.0833F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.25F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.375F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.5F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.6667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.7917F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.9167F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.0833F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.2083F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.3333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.5F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.625F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.75F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.9167F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.0417F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.1667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.3333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.4583F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.5833F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.75F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.875F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.1667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.2917F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.4167F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.5833F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.7083F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.8333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.125F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.25F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.4167F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.5417F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.6667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.8333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.9583F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.0833F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.25F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.375F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.5F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.7917F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.2083F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.875F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2917F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4167F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5833F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.7083F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8333F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.125F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.25F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.4167F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.5417F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.6667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.8333F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.9583F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.0833F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.25F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.375F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.5F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.6667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.7917F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.9167F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0833F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.2083F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.3333F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.5F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.625F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.75F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.9167F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.0417F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.1667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.3333F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.4583F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.5833F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.75F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.875F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.0F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.1667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.2917F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.4167F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.5833F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.7083F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.8333F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.0F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.125F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.25F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.4167F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.5417F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.6667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.8333F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.9583F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.0833F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.25F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.375F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.5F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.6667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.7917F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.9167F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.0833F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.2083F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.3333F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.5F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.625F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.75F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.9167F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.0417F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.1667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.3333F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.4583F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.5833F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.75F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.875F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.0F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.1667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.2917F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.4167F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.5833F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.7083F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.8333F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.0F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.125F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.25F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.4167F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.5417F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.6667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.8333F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.9583F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.0833F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.25F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.375F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.5F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.6667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.7917F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.9167F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.0833F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.2083F, KeyframeAnimations.degreeVec(-92.2279F, -14.8052F, 10.3371F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.3333F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(15.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.125F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.375F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.6667F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7917F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9167F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0833F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2083F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3333F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.625F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.9167F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0417F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.1667F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.3333F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.4583F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.5833F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.75F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.875F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.0F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.1667F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.2917F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.4167F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.5833F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.7083F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.8333F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.125F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.25F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.4167F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.5417F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.6667F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.8333F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.9583F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.0833F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.25F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.375F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.5F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.6667F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.7917F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.9167F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.0833F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.2083F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.3333F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.5F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.625F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.75F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.9167F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.0417F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.1667F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.3333F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.4583F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.5833F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.75F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.875F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.0F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.1667F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.2917F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.4167F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.5833F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.7083F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.8333F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.0F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.125F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.25F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.4167F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.5417F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.6667F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.8333F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.9583F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.0833F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.25F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.375F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.5F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.6667F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.7917F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.9167F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.0833F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.2083F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.3333F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.5F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.625F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.75F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.9167F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.0417F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.1667F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.3333F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.4583F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.5833F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.75F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.875F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.0F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.1667F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.2917F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.4167F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.5833F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.7083F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.8333F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.0F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.125F, KeyframeAnimations.posVec(-1.0F, 5.0F, -9.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.25F, KeyframeAnimations.posVec(-1.0F, 5.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.9167F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.SCALE,
						new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4583F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5833F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.875F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2917F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4167F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5833F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.7083F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.8333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.125F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.25F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.4167F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.5417F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.6667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.8333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(2.9583F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.0833F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.25F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.375F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.5F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.6667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.7917F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(3.9167F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.0833F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.2083F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.3333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.5F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.625F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.75F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(4.9167F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.0417F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.1667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.3333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.4583F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.5833F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.75F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(5.875F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.1667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.2917F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.4167F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.5833F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.7083F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(6.8333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.125F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.25F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.4167F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.5417F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.6667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.8333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(7.9583F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.0833F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.25F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.375F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.5F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.6667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.7917F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(8.9167F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.0833F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.2083F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.3333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.5F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.625F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.75F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(9.9167F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.0417F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.1667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.3333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.4583F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.5833F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.75F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(10.875F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.1667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.2917F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.4167F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.5833F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.7083F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(11.8333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.125F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.25F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.4167F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.5417F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.6667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.8333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(12.9583F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.0833F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.25F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.375F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.5F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.6667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.7917F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(13.9167F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.0833F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.2083F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.3333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(14.7917F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.build();
	}

	private static final class AttackStrike3 {
		private static final AnimationDefinition DEFINITION = AnimationDefinition.Builder.withLength(1.75F)
				.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2083F, KeyframeAnimations.degreeVec(8.1344F, -16.119F, 6.7872F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-29.1847F, -2.7721F, 5.0278F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-15.4379F, 37.6079F, -21.9609F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("body2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2917F, KeyframeAnimations.degreeVec(22.5F, -7.5F, -7.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-15.0F, -7.5F, -7.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2917F, KeyframeAnimations.degreeVec(-15.0F, -7.5F, -7.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4583F, KeyframeAnimations.degreeVec(29.7243F, 1.1778F, -2.518F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("body2", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5417F, KeyframeAnimations.posVec(2.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0833F, KeyframeAnimations.posVec(2.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.375F, KeyframeAnimations.posVec(2.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 17.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, -2.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, -2.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4583F, KeyframeAnimations.degreeVec(37.5F, 0.0F, -2.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5833F, KeyframeAnimations.posVec(0.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0833F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.375F, KeyframeAnimations.posVec(0.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4583F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.625F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -20.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.degreeVec(-16.6125F, -46.4926F, 8.4176F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.125F, KeyframeAnimations.degreeVec(-16.6125F, -46.4926F, 8.4176F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4167F, KeyframeAnimations.degreeVec(27.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5833F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.01F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(3.13F, 16.7F, 16.28F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4167F, KeyframeAnimations.degreeVec(1.25F, 33.04F, 26.11F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5417F, KeyframeAnimations.degreeVec(-3.72F, 35.09F, 29.18F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7917F, KeyframeAnimations.degreeVec(1.47F, 31.58F, 31.29F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9583F, KeyframeAnimations.degreeVec(4.07F, 24.98F, 25.69F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0833F, KeyframeAnimations.degreeVec(16.84F, 8.49F, 7.37F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1667F, KeyframeAnimations.degreeVec(27.3F, -8.7F, -9.9F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2083F, KeyframeAnimations.degreeVec(30.61F, -16.89F, -18.11F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3333F, KeyframeAnimations.degreeVec(30.71F, -29.19F, -32.94F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4583F, KeyframeAnimations.degreeVec(28.88F, -25.3F, -28.95F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5417F, KeyframeAnimations.degreeVec(23.46F, -15.44F, -20.24F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("back_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2083F, KeyframeAnimations.degreeVec(0.83F, -0.65F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7083F, KeyframeAnimations.degreeVec(31.33F, 39.3F, 9.33F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9167F, KeyframeAnimations.degreeVec(28.27F, 63.97F, 60.83F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(28.15F, 62.94F, 67.51F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2083F, KeyframeAnimations.degreeVec(32.88F, 34.68F, 31.31F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2917F, KeyframeAnimations.degreeVec(38.29F, 21.93F, 9.38F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.degreeVec(45.75F, -7.79F, -30.25F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.625F, KeyframeAnimations.degreeVec(24.7555F, -5.1243F, -9.0873F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9583F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.125F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3333F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.posVec(2.0F, 10.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9583F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2917F, KeyframeAnimations.posVec(0.0F, 6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4583F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.375F, KeyframeAnimations.degreeVec(0.0F, -360.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3333F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.posVec(0.0F, 15.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0417F, KeyframeAnimations.posVec(0.0F, 17.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.posVec(0.0F, -5.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7083F, KeyframeAnimations.degreeVec(52.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_leg2", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.125F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3333F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7083F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.875F, KeyframeAnimations.posVec(0.0F, 2.78F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1667F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3333F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7083F, KeyframeAnimations.degreeVec(74.17F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8333F, KeyframeAnimations.degreeVec(42.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1667F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_leg3", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.125F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1667F, KeyframeAnimations.posVec(0.0F, 0.31F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2083F, KeyframeAnimations.posVec(0.0F, 1.02F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 1.45F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2917F, KeyframeAnimations.posVec(0.0F, 1.32F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3333F, KeyframeAnimations.posVec(0.0F, 0.4F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.375F, KeyframeAnimations.posVec(0.0F, 0.02F, -0.01F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.625F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8333F, KeyframeAnimations.posVec(0.0F, 2.01F, 1.01F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1667F, KeyframeAnimations.posVec(0.0F, 0.99F, 0.99F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3333F, KeyframeAnimations.posVec(0.0F, 0.02F, 0.02F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.posVec(0.0F, 0.09F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5417F, KeyframeAnimations.posVec(0.0F, 0.45F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5833F, KeyframeAnimations.posVec(0.0F, 0.54F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2083F, KeyframeAnimations.degreeVec(-10.3453F, 14.7669F, 77.336F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4167F, KeyframeAnimations.degreeVec(34.2405F, 16.3256F, -37.4402F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9583F, KeyframeAnimations.degreeVec(-50.7595F, 16.3256F, -37.4402F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.375F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.125F, KeyframeAnimations.posVec(7.0F, 3.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3333F, KeyframeAnimations.posVec(2.03F, -0.03F, -0.03F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.875F, KeyframeAnimations.posVec(1.56F, 6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1667F, KeyframeAnimations.posVec(0.26F, 1.16F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2083F, KeyframeAnimations.posVec(0.11F, 0.48F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2917F, KeyframeAnimations.posVec(0.0F, -0.01F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.375F, KeyframeAnimations.posVec(0.0F, 0.6F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4583F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("left_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.03F, 0.0F, -0.01F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(42.09F, 0.0F, 0.32F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.375F, KeyframeAnimations.degreeVec(8.0F, -4.4F, -5.24F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5417F, KeyframeAnimations.degreeVec(-37.04F, -12.6F, -14.43F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-29.95F, -28.29F, -41.14F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.125F, KeyframeAnimations.degreeVec(-1.35F, -13.51F, -23.93F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2083F, KeyframeAnimations.degreeVec(19.45F, 0.98F, -0.62F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2917F, KeyframeAnimations.degreeVec(32.89F, 12.74F, 15.77F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3333F, KeyframeAnimations.degreeVec(33.51F, 15.62F, 15.47F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4167F, KeyframeAnimations.degreeVec(35.32F, 16.19F, 21.73F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.degreeVec(38.69F, 13.01F, 18.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5833F, KeyframeAnimations.degreeVec(17.04F, 8.04F, 9.24F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_Ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.02F, 0.0F, -0.01F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.degreeVec(42.09F, 0.0F, 0.32F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-7.51F, -6.39F, -5.76F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5417F, KeyframeAnimations.degreeVec(-36.66F, -12.6F, -14.65F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-42.33F, -25.08F, -33.92F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-30.39F, -28.29F, -41.79F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.125F, KeyframeAnimations.degreeVec(-1.5F, -13.51F, -25.54F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2083F, KeyframeAnimations.degreeVec(20.61F, 0.98F, -1.04F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2917F, KeyframeAnimations.degreeVec(32.69F, 12.74F, 15.76F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3333F, KeyframeAnimations.degreeVec(33.68F, 15.62F, 15.39F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.degreeVec(38.65F, 13.01F, 17.97F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5833F, KeyframeAnimations.degreeVec(17.0F, 8.04F, 9.21F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.625F, KeyframeAnimations.degreeVec(24.7555F, 5.1243F, 9.0873F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9583F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.125F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3333F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.posVec(-2.0F, 10.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9583F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2917F, KeyframeAnimations.posVec(0.0F, 6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7083F, KeyframeAnimations.degreeVec(52.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_leg2", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.125F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3333F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7083F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.875F, KeyframeAnimations.posVec(0.0F, 2.78F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1667F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3333F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.7083F, KeyframeAnimations.degreeVec(74.17F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8333F, KeyframeAnimations.degreeVec(42.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1667F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_leg3", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.125F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1667F, KeyframeAnimations.posVec(0.0F, 0.31F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2083F, KeyframeAnimations.posVec(0.0F, 1.02F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 1.45F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.2917F, KeyframeAnimations.posVec(0.0F, 1.32F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3333F, KeyframeAnimations.posVec(0.0F, 0.4F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.375F, KeyframeAnimations.posVec(0.0F, 0.02F, -0.01F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.625F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.8333F, KeyframeAnimations.posVec(0.0F, 2.01F, 1.01F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.1667F, KeyframeAnimations.posVec(0.0F, 0.99F, 0.99F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3333F, KeyframeAnimations.posVec(0.0F, 0.02F, 0.02F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.posVec(0.0F, 0.09F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5417F, KeyframeAnimations.posVec(0.0F, 0.45F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5833F, KeyframeAnimations.posVec(0.0F, 0.54F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
						new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-147.5F, 17.5F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-147.5F, 17.5F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-154.4617F, 19.1735F, 10.4248F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.degreeVec(-202.5F, 17.5F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0417F, KeyframeAnimations.degreeVec(-11.1136F, -9.2753F, 3.204F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2083F, KeyframeAnimations.degreeVec(-11.1136F, -9.2753F, 3.204F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3333F, KeyframeAnimations.degreeVec(-11.1136F, -9.2753F, 3.204F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4583F, KeyframeAnimations.degreeVec(-23.195F, -32.1717F, 14.3107F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5417F, KeyframeAnimations.degreeVec(-23.195F, -32.1717F, 14.3107F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5833F, KeyframeAnimations.degreeVec(-23.195F, -32.1717F, 14.3107F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6667F, KeyframeAnimations.degreeVec(-23.195F, -32.1717F, 14.3107F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.POSITION,
						new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.1667F, KeyframeAnimations.posVec(3.0F, -6.99F, -7.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3333F, KeyframeAnimations.posVec(0.99F, -6.98F, -7.01F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.5F, KeyframeAnimations.posVec(-3.0F, 14.0F, -16.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.9167F, KeyframeAnimations.posVec(0.02F, 24.08F, -11.97F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.0833F, KeyframeAnimations.posVec(0.0F, 24.01F, -12.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2083F, KeyframeAnimations.posVec(-0.01F, 23.99F, -12.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.25F, KeyframeAnimations.posVec(-0.91F, 21.45F, -12.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2917F, KeyframeAnimations.posVec(-2.1F, 17.8F, -12.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.3333F, KeyframeAnimations.posVec(-2.49F, 14.11F, -12.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.375F, KeyframeAnimations.posVec(-1.35F, 10.75F, -12.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4167F, KeyframeAnimations.posVec(0.58F, 8.39F, -12.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4583F, KeyframeAnimations.posVec(2.02F, 5.29F, -12.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5F, KeyframeAnimations.posVec(2.28F, 3.57F, -12.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5417F, KeyframeAnimations.posVec(2.01F, 2.82F, -12.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5833F, KeyframeAnimations.posVec(1.99F, 2.01F, -12.04F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6667F, KeyframeAnimations.posVec(2.99F, 6.95F, -11.97F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.SCALE,
						new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.25F, KeyframeAnimations.scaleVec(1.0F, 0.5F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.3333F, KeyframeAnimations.scaleVec(1.2812F, 2.0F, 1.2812F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.4167F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(0.75F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2083F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.2917F, KeyframeAnimations.scaleVec(1.1667F, 1.9667F, 1.1667F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.375F, KeyframeAnimations.scaleVec(1.5F, 1.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.4583F, KeyframeAnimations.scaleVec(1.5F, 0.9F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5417F, KeyframeAnimations.scaleVec(1.5F, 1.0F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.5833F, KeyframeAnimations.scaleVec(1.5F, 1.0F, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.6667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
						new Keyframe(1.75F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.build();
	}
}
