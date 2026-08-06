package net.tiew.operationWild.entity.client.animation;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Animations de l'elephant, exportees de Blockbench 5.1.6.
 *
 * <p>{@link #EARTHQUAKE} a ete animee sur une version anterieure du squelette : son export
 * portait des canaux vers des os qui n'existent plus ({@code right_Leg_1}, {@code neck},
 * {@code nose}, {@code eye_Ball_1}...). Les pattes et la tete etaient doublees sous les deux
 * nomenclatures et ont ete degraissees ; la trompe et les paupieres, elles, n'existaient que
 * sous les anciens noms et ont ete rebranchees sur {@code trunk}, {@code trunk2} et les deux
 * {@code eyeBall}. Sans cela la trompe restait figee pendant tout le seisme.</p>
 *
 * <p>L'impact au sol tombe a 3,0 s des 4,16 s du geste, pas a la fin : c'est ce tick que
 * {@code OWAttacksConstants.Elephant.EARTHQUAKE_WINDUP_TICKS} reprend.</p>
 */
public class ElephantAnimations {

    public static final AnimationDefinition MISC_IDLE = AnimationDefinition.Builder.withLength(4.8F).looping()
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -15.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -15.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 2.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.4F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -2.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(4.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 2.5F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.4F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(4.8F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-10.8425F, -12.3914F, -7.6799F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.4F, KeyframeAnimations.degreeVec(-2.349F, -0.4232F, 1.8373F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(4.8F, KeyframeAnimations.degreeVec(-10.8425F, -12.3914F, -7.6799F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-10.8425F, 12.3914F, 7.6799F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.4F, KeyframeAnimations.degreeVec(-2.349F, 0.4232F, -1.8373F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(4.8F, KeyframeAnimations.degreeVec(-10.8425F, 12.3914F, 7.6799F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("trunk", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(-22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(4.4F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("trunk2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.4F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(4.8F, KeyframeAnimations.degreeVec(25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.88F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.04F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.2F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.36F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.6F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.88F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.04F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.2F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.36F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.6F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -25.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.4F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 25.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(4.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -25.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("tail2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -25.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.4F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 25.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(4.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -25.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("demon_left_wing", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(15.1235F, 7.243F, -35.5484F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.4F, KeyframeAnimations.degreeVec(18.8291F, 12.1032F, -44.5227F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(4.8F, KeyframeAnimations.degreeVec(15.1235F, 7.243F, -35.5484F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("demon_left_wing", new AnimationChannel(AnimationChannel.Targets.SCALE, 
                new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.5F, 1.0F, 1.3F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("demon_right_wing", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(15.1235F, -7.243F, 35.5484F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.4F, KeyframeAnimations.degreeVec(18.8291F, -12.1032F, 44.5227F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(4.8F, KeyframeAnimations.degreeVec(15.1235F, -7.243F, 35.5484F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("demon_right_wing", new AnimationChannel(AnimationChannel.Targets.SCALE, 
                new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.5F, 1.0F, 1.3F), AnimationChannel.Interpolations.LINEAR)
            ))
            .build();

    public static final AnimationDefinition MOVE_WALK = AnimationDefinition.Builder.withLength(3.4115F).looping()
            .addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8529F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.7058F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.5586F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.4115F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(30.4351F, 10.4892F, 11.8737F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8529F, KeyframeAnimations.degreeVec(0.4631F, 19.9804F, 5.1393F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.7058F, KeyframeAnimations.degreeVec(-27.337F, 9.0694F, 6.1888F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.4115F, KeyframeAnimations.degreeVec(30.4351F, 10.4892F, 11.8737F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8529F, KeyframeAnimations.posVec(0.0F, 7.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, -1.77F, 0.77F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.7058F, KeyframeAnimations.posVec(0.0F, -0.77F, 0.77F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.4115F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-27.337F, 9.0694F, 6.1888F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.7058F, KeyframeAnimations.degreeVec(30.4351F, 10.4892F, 11.8737F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.5586F, KeyframeAnimations.degreeVec(0.4631F, 19.9804F, 5.1393F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.4115F, KeyframeAnimations.degreeVec(-27.337F, 9.0694F, 6.1888F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -0.77F, 0.77F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.7058F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.5586F, KeyframeAnimations.posVec(0.0F, 7.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.9F, KeyframeAnimations.posVec(0.0F, -1.77F, 0.77F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.4115F, KeyframeAnimations.posVec(0.0F, -0.77F, 0.77F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(30.4351F, -10.4892F, -11.8737F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8529F, KeyframeAnimations.degreeVec(0.4631F, -19.9804F, -5.1393F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.7058F, KeyframeAnimations.degreeVec(-27.337F, -9.0694F, -6.1888F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.4115F, KeyframeAnimations.degreeVec(30.4351F, -10.4892F, -11.8737F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8529F, KeyframeAnimations.posVec(0.0F, 7.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, -1.77F, 0.77F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.7058F, KeyframeAnimations.posVec(0.0F, -0.77F, 0.77F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.4115F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-27.337F, -9.0694F, -6.1888F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.7058F, KeyframeAnimations.degreeVec(30.4351F, -10.4892F, -11.8737F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.5586F, KeyframeAnimations.degreeVec(0.4631F, -19.9804F, -5.1393F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.4115F, KeyframeAnimations.degreeVec(-27.337F, -9.0694F, -6.1888F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -0.77F, 0.77F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.7058F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.5586F, KeyframeAnimations.posVec(0.0F, 7.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.9F, KeyframeAnimations.posVec(0.0F, -1.77F, 0.77F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.4115F, KeyframeAnimations.posVec(0.0F, -0.77F, 0.77F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0256F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 5.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.4115F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, -5.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0661F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0256F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.7719F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.4115F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(10.952F, 6.0144F, 8.7342F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.5992F, KeyframeAnimations.degreeVec(7.0346F, -4.3121F, -7.884F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.4115F, KeyframeAnimations.degreeVec(10.952F, 6.0144F, 8.7342F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.5992F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.4115F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-6.1816F, -9.9136F, -7.6144F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.5992F, KeyframeAnimations.degreeVec(2.3862F, 12.9525F, -4.9325F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.4115F, KeyframeAnimations.degreeVec(-6.1816F, -9.9136F, -7.6144F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-6.1816F, 9.9136F, 7.6144F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.5992F, KeyframeAnimations.degreeVec(2.3862F, -12.9525F, 4.9325F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.4115F, KeyframeAnimations.degreeVec(-6.1816F, 9.9136F, 7.6144F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("trunk", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.1727F, KeyframeAnimations.degreeVec(5.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.8785F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("trunk2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.5331F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 25.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.7058F, KeyframeAnimations.degreeVec(15.0F, 0.0F, -25.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.4115F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 25.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0661F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.3859F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.7058F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0256F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.3454F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.4115F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0661F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.3859F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.7058F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0256F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.3454F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.4115F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.8124F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -15.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.4115F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("tail2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.8124F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -15.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.4115F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("demon_left_wing", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(33.4877F, -10.1649F, -30.7454F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.7F, KeyframeAnimations.degreeVec(-5.9229F, -5.0752F, 28.0775F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.4F, KeyframeAnimations.degreeVec(33.4877F, -10.1649F, -30.7454F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("demon_left_wing", new AnimationChannel(AnimationChannel.Targets.SCALE, 
                new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.5F, 1.0F, 1.2F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("demon_right_wing", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(33.4877F, 10.1649F, 30.7454F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.7F, KeyframeAnimations.degreeVec(-5.9229F, 5.0752F, -28.0775F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.4F, KeyframeAnimations.degreeVec(33.4877F, 10.1649F, 30.7454F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("demon_right_wing", new AnimationChannel(AnimationChannel.Targets.SCALE, 
                new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.5F, 1.0F, 1.2F), AnimationChannel.Interpolations.LINEAR)
            ))
            .build();

    public static final AnimationDefinition SIT = AnimationDefinition.Builder.withLength(6.08F).looping()
            .addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -14.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-90.0F, 25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.04F, KeyframeAnimations.degreeVec(-90.0F, 20.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(6.08F, KeyframeAnimations.degreeVec(-90.0F, 25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(90.0F, -25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.04F, KeyframeAnimations.degreeVec(90.0F, -20.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(6.08F, KeyframeAnimations.degreeVec(90.0F, -25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(90.0F, 25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.04F, KeyframeAnimations.degreeVec(90.0F, 20.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(6.08F, KeyframeAnimations.degreeVec(90.0F, 25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-90.0F, -25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.04F, KeyframeAnimations.degreeVec(-90.0F, -20.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(6.08F, KeyframeAnimations.degreeVec(-90.0F, -25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(2.4976F, -0.109F, 2.4976F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.04F, KeyframeAnimations.degreeVec(0.0024F, 0.109F, -2.4976F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(6.08F, KeyframeAnimations.degreeVec(2.4976F, -0.109F, 2.4976F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.44F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.04F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(4.64F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(6.08F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-7.336F, -14.7669F, -10.3453F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.96F, KeyframeAnimations.degreeVec(4.0932F, -6.6552F, -6.3597F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(6.08F, KeyframeAnimations.degreeVec(-7.336F, -14.7669F, -10.3453F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-7.336F, 14.7669F, 10.3453F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.96F, KeyframeAnimations.degreeVec(4.0932F, 6.6552F, 6.3597F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(6.08F, KeyframeAnimations.degreeVec(-7.336F, 14.7669F, 10.3453F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("trunk", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-24.9164F, -2.1109F, -4.5336F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.56F, KeyframeAnimations.degreeVec(-24.9164F, 2.1109F, 4.5336F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(5.6F, KeyframeAnimations.degreeVec(-24.9164F, -2.1109F, -4.5336F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("trunk2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.48F, KeyframeAnimations.degreeVec(65.5374F, 17.3239F, -24.034F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.04F, KeyframeAnimations.degreeVec(65.5374F, -17.3239F, 24.034F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(6.08F, KeyframeAnimations.degreeVec(65.5374F, 17.3239F, -24.034F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.4F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.64F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.88F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.12F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.36F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.4F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.64F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.88F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.12F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.36F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.04F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(6.08F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("tail2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.04F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(6.08F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("demon_left_wing", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, -17.5F, -25.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.88F, KeyframeAnimations.degreeVec(-7.9923F, -1.5268F, -16.272F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(6.08F, KeyframeAnimations.degreeVec(5.0F, -17.5F, -25.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("demon_left_wing", new AnimationChannel(AnimationChannel.Targets.SCALE, 
                new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.5F, 1.0F, 1.2F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("demon_right_wing", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, 17.5F, 25.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.88F, KeyframeAnimations.degreeVec(-7.9923F, 1.5268F, 16.272F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(6.08F, KeyframeAnimations.degreeVec(5.0F, 17.5F, 25.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("demon_right_wing", new AnimationChannel(AnimationChannel.Targets.SCALE, 
                new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.5F, 1.0F, 1.2F), AnimationChannel.Interpolations.LINEAR)
            ))
            .build();

    public static final AnimationDefinition TRANSITION_IDLE_SIT = AnimationDefinition.Builder.withLength(0.6667F)
            .addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.posVec(0.0F, -14.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-90.0F, 25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(90.0F, -25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -15.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(90.0F, 25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -15.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-90.0F, -25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 2.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(2.4976F, -0.109F, 2.4976F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.6667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.posVec(0.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-10.8425F, -12.3914F, -7.6799F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-7.336F, -14.7669F, -10.3453F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-10.8425F, 12.3914F, 7.6799F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-7.336F, 14.7669F, 10.3453F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("trunk", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-24.9164F, -2.1109F, -4.5336F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("trunk2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(65.5374F, 17.3239F, -24.034F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -25.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("tail2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -25.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("demon_left_wing", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(15.1235F, 7.243F, -35.5484F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(5.0F, -17.5F, -25.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("demon_left_wing", new AnimationChannel(AnimationChannel.Targets.SCALE, 
                new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.5F, 1.0F, 1.2F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("demon_right_wing", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(15.1235F, -7.243F, 35.5484F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(5.0F, 17.5F, 25.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("demon_right_wing", new AnimationChannel(AnimationChannel.Targets.SCALE, 
                new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.5F, 1.0F, 1.2F), AnimationChannel.Interpolations.LINEAR)
            ))
            .build();

    public static final AnimationDefinition TRANSITION_SIT_IDLE = AnimationDefinition.Builder.withLength(0.6667F)
            .addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -14.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-90.0F, 25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(90.0F, -25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(0.0F, 15.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(90.0F, 25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(0.0F, -15.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-90.0F, -25.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(0.0F, -15.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(2.4976F, -0.109F, 2.4976F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 2.5F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-7.336F, -14.7669F, -10.3453F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-10.8425F, -12.3914F, -7.6799F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-7.336F, 14.7669F, 10.3453F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-10.8425F, 12.3914F, 7.6799F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("trunk", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-24.9164F, -2.1109F, -4.5336F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("trunk2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(65.5374F, 17.3239F, -24.034F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -25.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("tail2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -25.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("demon_left_wing", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, -17.5F, -25.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(15.1235F, 7.243F, -35.5484F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("demon_left_wing", new AnimationChannel(AnimationChannel.Targets.SCALE, 
                new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.5F, 1.0F, 1.2F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("demon_right_wing", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, 17.5F, 25.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6667F, KeyframeAnimations.degreeVec(15.1235F, -7.243F, 35.5484F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("demon_right_wing", new AnimationChannel(AnimationChannel.Targets.SCALE, 
                new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.5F, 1.0F, 1.2F), AnimationChannel.Interpolations.LINEAR)
            ))
            .build();

    public static final AnimationDefinition ATTACK_STRIKE = AnimationDefinition.Builder.withLength(1.68F)
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.68F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 7.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.08F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -17.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.92F, KeyframeAnimations.posVec(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.08F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.52F, KeyframeAnimations.degreeVec(23.459F, 23.5373F, 28.6754F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.72F, KeyframeAnimations.degreeVec(33.357F, 32.4315F, 23.8298F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.96F, KeyframeAnimations.degreeVec(-75.5413F, -45.7962F, 9.5192F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.04F, KeyframeAnimations.degreeVec(-85.6683F, -46.8284F, 16.6048F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.32F, KeyframeAnimations.posVec(5.0F, -4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.posVec(5.0F, -4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.88F, KeyframeAnimations.posVec(-7.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.04F, KeyframeAnimations.posVec(-7.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.16F, KeyframeAnimations.posVec(-7.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.12F, KeyframeAnimations.degreeVec(-10.8425F, -12.3914F, -7.6799F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.76F, KeyframeAnimations.degreeVec(-9.7206F, 13.2844F, 12.7258F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.16F, KeyframeAnimations.degreeVec(-24.3854F, -48.6981F, -2.0417F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.degreeVec(-10.8425F, -12.3914F, -7.6799F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.12F, KeyframeAnimations.degreeVec(-10.8425F, 12.3914F, 7.6799F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.76F, KeyframeAnimations.degreeVec(-2.3673F, 16.2422F, 41.0226F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.16F, KeyframeAnimations.degreeVec(-35.8837F, 35.4531F, -50.0548F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.degreeVec(-10.8425F, 12.3914F, 7.6799F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("trunk", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(16.7363F, -5.188F, 16.7363F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.92F, KeyframeAnimations.degreeVec(-70.6017F, 3.4519F, -16.9001F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.52F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("trunk2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.16F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.56F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 35.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.04F, KeyframeAnimations.degreeVec(-43.4403F, -29.6144F, -49.0983F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.72F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.88F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.72F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.88F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.68F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -15.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.96F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 65.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.36F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("tail2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.68F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -15.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.96F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 65.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.36F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("demon_left_wing", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(15.1833F, 0.7666F, -42.0686F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.76F, KeyframeAnimations.degreeVec(9.9205F, 14.303F, -54.0108F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.08F, KeyframeAnimations.degreeVec(-11.4747F, 16.4932F, 14.5466F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.degreeVec(15.1833F, 0.7666F, -42.0686F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("demon_left_wing", new AnimationChannel(AnimationChannel.Targets.SCALE, 
                new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.5F, 1.0F, 1.2F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("demon_right_wing", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(15.1833F, -0.7666F, 42.0686F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.76F, KeyframeAnimations.degreeVec(9.9205F, -14.303F, 54.0108F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.08F, KeyframeAnimations.degreeVec(-11.4747F, -16.4932F, -14.5466F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.degreeVec(15.1833F, -0.7666F, 42.0686F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("demon_right_wing", new AnimationChannel(AnimationChannel.Targets.SCALE, 
                new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.5F, 1.0F, 1.2F), AnimationChannel.Interpolations.LINEAR)
            ))
            .build();

    public static final AnimationDefinition ATTACK_STRIKE_2 = AnimationDefinition.Builder.withLength(1.68F)
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.68F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -7.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.08F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 17.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.92F, KeyframeAnimations.posVec(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.08F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.52F, KeyframeAnimations.degreeVec(23.459F, -23.5373F, -28.6754F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.72F, KeyframeAnimations.degreeVec(33.357F, -32.4315F, -23.8298F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.96F, KeyframeAnimations.degreeVec(-75.5413F, 45.7962F, -9.5192F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.04F, KeyframeAnimations.degreeVec(-85.6683F, 46.8284F, -16.6048F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.32F, KeyframeAnimations.posVec(-5.0F, -4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6F, KeyframeAnimations.posVec(-5.0F, -4.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.88F, KeyframeAnimations.posVec(7.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.04F, KeyframeAnimations.posVec(7.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.16F, KeyframeAnimations.posVec(7.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.12F, KeyframeAnimations.degreeVec(-10.8425F, -12.3914F, -7.6799F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.76F, KeyframeAnimations.degreeVec(-9.7206F, -13.2844F, -12.7258F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.16F, KeyframeAnimations.degreeVec(-24.3854F, 48.6981F, 2.0417F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.degreeVec(-10.8425F, -12.3914F, -7.6799F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.12F, KeyframeAnimations.degreeVec(-10.8425F, 12.3914F, 7.6799F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.76F, KeyframeAnimations.degreeVec(-2.3673F, -16.2422F, -41.0226F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.16F, KeyframeAnimations.degreeVec(-35.8837F, -35.4531F, 50.0548F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.degreeVec(-10.8425F, 12.3914F, 7.6799F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("trunk", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.4F, KeyframeAnimations.degreeVec(16.7363F, 5.188F, -16.7363F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.92F, KeyframeAnimations.degreeVec(-70.6017F, -3.4519F, 16.9001F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.52F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("trunk2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.16F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.56F, KeyframeAnimations.degreeVec(20.0F, 0.0F, -35.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.04F, KeyframeAnimations.degreeVec(-43.4403F, 29.6144F, 49.0983F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.72F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.88F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.72F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.88F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.68F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.96F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -65.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.36F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("tail2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.68F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.96F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -65.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.36F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("demon_left_wing", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(15.1833F, 0.7666F, -42.0686F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.76F, KeyframeAnimations.degreeVec(9.9205F, 14.303F, -54.0108F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.08F, KeyframeAnimations.degreeVec(-11.4747F, 16.4932F, 14.5466F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.degreeVec(15.1833F, 0.7666F, -42.0686F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("demon_left_wing", new AnimationChannel(AnimationChannel.Targets.SCALE, 
                new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.5F, 1.0F, 1.2F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("demon_right_wing", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(15.1833F, -0.7666F, 42.0686F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.76F, KeyframeAnimations.degreeVec(9.9205F, -14.303F, 54.0108F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.08F, KeyframeAnimations.degreeVec(-11.4747F, -16.4932F, -14.5466F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.68F, KeyframeAnimations.degreeVec(15.1833F, -0.7666F, 42.0686F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("demon_right_wing", new AnimationChannel(AnimationChannel.Targets.SCALE, 
                new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.5F, 1.0F, 1.2F), AnimationChannel.Interpolations.LINEAR)
            ))
            .build();

    public static final AnimationDefinition ATTACK_STRIKE_3 = AnimationDefinition.Builder.withLength(1.6807F)
            .addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7731F, KeyframeAnimations.degreeVec(-27.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0084F, KeyframeAnimations.degreeVec(-32.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.1765F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6807F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7731F, KeyframeAnimations.posVec(0.0F, 8.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0084F, KeyframeAnimations.posVec(0.0F, 9.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.1765F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6807F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6723F, KeyframeAnimations.degreeVec(-27.9428F, 12.3206F, 1.9848F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0084F, KeyframeAnimations.degreeVec(-34.0724F, 8.5373F, 12.3796F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.1765F, KeyframeAnimations.degreeVec(-5.0304F, 16.2377F, 13.6448F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6807F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7731F, KeyframeAnimations.posVec(5.0F, 10.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0084F, KeyframeAnimations.posVec(0.0F, 12.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.1765F, KeyframeAnimations.posVec(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6807F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7731F, KeyframeAnimations.degreeVec(27.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0084F, KeyframeAnimations.degreeVec(32.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.1765F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6807F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7731F, KeyframeAnimations.degreeVec(27.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0084F, KeyframeAnimations.degreeVec(32.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.1765F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6807F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6723F, KeyframeAnimations.degreeVec(-27.9428F, -12.3206F, -1.9848F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0084F, KeyframeAnimations.degreeVec(-34.0724F, -8.5373F, -12.3796F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.1765F, KeyframeAnimations.degreeVec(-5.0304F, -16.2377F, -13.6448F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6807F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7731F, KeyframeAnimations.posVec(-5.0F, 10.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0084F, KeyframeAnimations.posVec(0.0F, 12.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.1765F, KeyframeAnimations.posVec(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6807F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 2.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.437F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -2.5F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7731F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 2.5F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8403F, KeyframeAnimations.degreeVec(45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.042F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2773F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6807F, KeyframeAnimations.degreeVec(12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.1344F, KeyframeAnimations.degreeVec(-10.8425F, -12.3914F, -7.6799F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7731F, KeyframeAnimations.degreeVec(-6.5826F, -15.0644F, -25.4892F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.1765F, KeyframeAnimations.degreeVec(-35.8837F, -35.4531F, 50.0548F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6807F, KeyframeAnimations.degreeVec(-10.8425F, -12.3914F, -7.6799F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.1344F, KeyframeAnimations.degreeVec(-10.8425F, 12.3914F, 7.6799F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7731F, KeyframeAnimations.degreeVec(-6.5826F, 15.0644F, 25.4892F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.1765F, KeyframeAnimations.degreeVec(-35.8837F, 35.4531F, -50.0548F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6807F, KeyframeAnimations.degreeVec(-10.8425F, 12.3914F, 7.6799F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("trunk", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7731F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0084F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.2773F, KeyframeAnimations.degreeVec(-22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.5462F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("trunk2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.1344F, KeyframeAnimations.degreeVec(32.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.9076F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.1429F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.4118F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6807F, KeyframeAnimations.degreeVec(32.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6387F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7059F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8067F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.874F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.6387F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7059F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.8067F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.874F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("demon_left_wing", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(15.1833F, 0.7666F, -42.0686F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7731F, KeyframeAnimations.degreeVec(9.9205F, 14.303F, -54.0108F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0756F, KeyframeAnimations.degreeVec(0.4016F, 19.9969F, 51.2859F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6807F, KeyframeAnimations.degreeVec(15.1833F, 0.7666F, -42.0686F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("demon_left_wing", new AnimationChannel(AnimationChannel.Targets.SCALE, 
                new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.5F, 1.0F, 1.2F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("demon_right_wing", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(15.1833F, -0.7666F, 42.0686F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(0.7731F, KeyframeAnimations.degreeVec(9.9205F, -14.303F, 54.0108F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.0756F, KeyframeAnimations.degreeVec(0.4016F, -19.9969F, -51.2859F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.6807F, KeyframeAnimations.degreeVec(15.1833F, -0.7666F, 42.0686F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("demon_right_wing", new AnimationChannel(AnimationChannel.Targets.SCALE, 
                new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.5F, 1.0F, 1.2F), AnimationChannel.Interpolations.LINEAR)
            ))
            .build();

    public static final AnimationDefinition EARTHQUAKE = AnimationDefinition.Builder.withLength(4.16F)
            .addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.0F, KeyframeAnimations.degreeVec(-35.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.24F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.84F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(4.16F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("ALL", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.0F, KeyframeAnimations.posVec(0.0F, 6.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.24F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(4.16F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(27.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.0F, KeyframeAnimations.degreeVec(-34.0724F, 8.5373F, 12.3796F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.24F, KeyframeAnimations.degreeVec(-5.0304F, 16.2377F, 13.6448F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.84F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(4.16F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.posVec(7.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.0F, KeyframeAnimations.posVec(5.0F, 12.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.24F, KeyframeAnimations.posVec(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.36F, KeyframeAnimations.posVec(-0.32F, 4.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.84F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(4.16F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.0F, KeyframeAnimations.degreeVec(35.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.36F, KeyframeAnimations.degreeVec(-10.0643F, -0.1186F, 1.1969F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(4.16F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.0F, KeyframeAnimations.degreeVec(35.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.36F, KeyframeAnimations.degreeVec(-10.16F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(4.16F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(27.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.0F, KeyframeAnimations.degreeVec(-34.0724F, -8.5373F, -12.3796F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.24F, KeyframeAnimations.degreeVec(-5.0304F, -16.2377F, -13.6448F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.84F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(4.16F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.posVec(-7.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.0F, KeyframeAnimations.posVec(-5.0F, 12.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.24F, KeyframeAnimations.posVec(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.36F, KeyframeAnimations.posVec(0.32F, 4.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.84F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(4.16F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(54.88F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.0F, KeyframeAnimations.degreeVec(-27.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.32F, KeyframeAnimations.degreeVec(17.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.84F, KeyframeAnimations.degreeVec(12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(4.16F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 8.37F, -8.04F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.0F, KeyframeAnimations.posVec(0.0F, 6.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.16F, KeyframeAnimations.posVec(0.0F, 4.0F, -4.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.32F, KeyframeAnimations.posVec(0.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(4.16F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, -10.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, -10.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.24F, KeyframeAnimations.degreeVec(0.0F, -40.0F, 10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(4.16F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 10.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 10.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.24F, KeyframeAnimations.degreeVec(0.0F, 40.0F, -10.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(4.16F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("trunk", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(1.92F, KeyframeAnimations.degreeVec(-67.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.64F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.6F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(4.16F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("trunk2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
                new Keyframe(0.24F, KeyframeAnimations.degreeVec(37.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.16F, KeyframeAnimations.degreeVec(-57.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.88F, KeyframeAnimations.degreeVec(62.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.84F, KeyframeAnimations.degreeVec(37.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.28F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.16F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_eyeBall", new AnimationChannel(AnimationChannel.Targets.POSITION, 
                new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(2.28F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(3.16F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .build();
}
