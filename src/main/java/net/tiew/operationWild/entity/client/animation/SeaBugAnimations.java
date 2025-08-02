package net.tiew.operationWild.entity.client.animation;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public class SeaBugAnimations {

public static final AnimationDefinition MOVE_RUN = AnimationDefinition.Builder.withLength(1f).looping()
.addAnimation("front_screw",
	new AnimationChannel(AnimationChannel.Targets.ROTATION,
		new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
			AnimationChannel.Interpolations.LINEAR),
		new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 1080f),
			AnimationChannel.Interpolations.LINEAR)))
.addAnimation("back_screw",
	new AnimationChannel(AnimationChannel.Targets.ROTATION,
		new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
			AnimationChannel.Interpolations.LINEAR),
		new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, -990f),
			AnimationChannel.Interpolations.LINEAR)))
.addAnimation("wheel",
	new AnimationChannel(AnimationChannel.Targets.POSITION, 
		new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, -0.5f),
			AnimationChannel.Interpolations.LINEAR)))
.addAnimation("wheel",
	new AnimationChannel(AnimationChannel.Targets.ROTATION,
		new Keyframe(0f, KeyframeAnimations.degreeVec(5f, 0f, 0f),
			AnimationChannel.Interpolations.LINEAR))).build();
public static final AnimationDefinition LEFT = AnimationDefinition.Builder.withLength(0.32f)
.addAnimation("wheel",
	new AnimationChannel(AnimationChannel.Targets.ROTATION,
		new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
			AnimationChannel.Interpolations.CATMULLROM),
		new Keyframe(0.32f, KeyframeAnimations.degreeVec(0f, 0f, 45f),
			AnimationChannel.Interpolations.CATMULLROM))).build();
public static final AnimationDefinition RIGHT = AnimationDefinition.Builder.withLength(0.32f)
.addAnimation("wheel",
	new AnimationChannel(AnimationChannel.Targets.ROTATION,
		new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
			AnimationChannel.Interpolations.CATMULLROM),
		new Keyframe(0.32f, KeyframeAnimations.degreeVec(0f, 0f, -45f),
			AnimationChannel.Interpolations.CATMULLROM))).build();

    }
