package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.GorillaEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.client.animation.GorillaAnimations;
import org.joml.Vector3f;

public class GorillaModel<T extends GorillaEntity> extends OWComboModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "gorilla"), "main");

    private static final float REST_POSE_Y_SUM = 12.0f;

    private static final ItemStack HELD_ROCK = new ItemStack(Blocks.COBBLESTONE);

    private static final float HELD_ROCK_SCALE = 4.5f;
    private static final float HELD_ROCK_SCALE_SELF = 2.1f;

    private static final float MOVE_BLEND_FACTOR = 4.0f;
    private static final float WALK_ANIM_SPEED = 3.6f;
    private static final float RUN_ANIM_SPEED = 1.2f;

    private static final float COMBO_ANIMATION_SPEED = 1.0f;

    private static final float RIDDEN_IDLE_BODY_PITCH = 0.61f;
    private static final float RIDDEN_IDLE_HEAD_COMP = -0.42f;

    private static final float AIM_BODY_PITCH = 0.17f;
    private static final float AIM_BODY_LIFT = -0.30f;
    private static final float AIM_BODY_SINK = 2.1f;
    private static final float AIM_HEAD_TUCK = 0.34f;
    private static final float AIM_HEAD_DROP = 1.8f;
    private static final float AIM_HEAD_BACK = 1.6f;
    private static final int AIM_TREMBLE_RAMP = 45;

    private static final float CLIMB_BODY_PITCH = -1.30f;
    private static final float CLIMB_ARM_BASE = -1.60f;
    private static final float CLIMB_ARM_SWING = 0.40f;
    private static final float CLIMB_ARM_REACH_SPREAD = 0.22f;
    private static final float CLIMB_SWING_SHAPE = 0.65f;
    private static final float CLIMB_LEG_BASE = 0.75f;
    private static final float CLIMB_LEG_SWING = 0.40f;
    private static final float CLIMB_WALL_HUG = 5.0f;
    private static final float CLIMB_FREE_ARM_HANG = 0.95f;

    private static final float CHEST_BEAT_BLEND_TICKS = 6f;

    private static final float SLIDE_DROP = 4.2f;
    private static final float SLIDE_LEAN_BACK = -0.30f;
    private static final float SLIDE_BANK = 0.26f;
    private static final float SLIDE_DRIFT = 0.18f;
    private static final float SLIDE_ARM_LIFT = -0.95f;
    private static final float SLIDE_ARM_DRAG = -0.28f;
    private static final float SLIDE_LEG_FORWARD = -0.95f;
    private static final float SLIDE_SWAY_RATE = 0.28f;

    private static final float ROCK_STRIKE_TICKS = 3.5f;

    private static final float CLIMB_HANG_HEAD_DROP = 0.55f;
    private static final float HANG_REGRIP_PERIOD = 90f;
    private static final float VAULT_PRESS_END = 0.6f;
    private static final float VAULT_REACH_START = 0.4f;
    private static final float CLIMB_LOOK_TWIST_RANGE = 65f;
    private static final float CLIMB_LOOK_TWIST = 0.55f;
    private static final float CLIMB_LOOK_CHEST_OFF = 0.14f;
    private static final float CLIMB_LOOK_HEAD_ROLL = 0.15f;
    private static final float CLIMB_LOOK_ARM_OPEN = 0.25f;

    private static final float WALL_THROW_COCK_X = -2.25f;
    private static final float WALL_THROW_COCK_Z = 0.55f;
    private static final float WALL_THROW_FOLLOW_X = -3.5f;
    private static final float WALL_THROW_TWIST = 0.42f;
    private static final float WALL_THROW_CHEST_OFF = -0.12f;
    private static final float WALL_THROW_HEAD_LOOK = -0.75f;
    private static final float WALL_THROW_HEAD_TURN = 0.35f;
    private static final float WALL_THROW_GRIP_REACH = -0.15f;

    private static final float LAUNCH_COIL_TICKS = 30f;
    private static final float LAUNCH_CROUCH = 2.4f;
    private static final float LAUNCH_GRIP_ARM = 2.55f;
    private static final float LAUNCH_HURL_TICKS = 4f;
    private static final float LAUNCH_FOLLOW_ARM = 4.45f;
    private static final float LAUNCH_REAR_TICKS = 9f;
    private static final float FULL_TURN = (float) (Math.PI * 2.0);

    private static final Vector3f ANIM_VECTOR = new Vector3f();

    private final ModelPart ALL2;
    private final ModelPart ALL;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart left_ear;
    private final ModelPart right_ear;
    private final ModelPart left_eyeBall;
    private final ModelPart right_eyeBall;
    private final ModelPart left_arm;
    private final ModelPart right_arm;
    private final ModelPart left_leg;
    private final ModelPart right_leg;

    private static final long WALK_STEP_RIGHT_MS = 833L;
    private static final long WALK_STEP_LEFT_MS = 1833L;
    private static final long RUN_STEP_RIGHT_MS = 267L;
    private static final long RUN_STEP_LEFT_MS = 433L;

    private float prevLimbSwing = 0f;

    private T currentEntity;

    private MultiBufferSource currentBufferSource;

    public GorillaModel(ModelPart root) {
        this.ALL2 = root.getChild("ALL2");
        this.ALL = this.ALL2.getChild("ALL");
        this.body = this.ALL.getChild("body");
        this.head = this.body.getChild("head");
        this.left_ear = this.head.getChild("left_ear");
        this.right_ear = this.head.getChild("right_ear");
        this.left_eyeBall = this.head.getChild("left_eyeBall");
        this.right_eyeBall = this.head.getChild("right_eyeBall");
        this.left_leg = this.ALL.getChild("left_leg");
        this.right_leg = this.ALL.getChild("right_leg");
        this.right_arm = this.ALL.getChild("right_arm");
        this.left_arm = this.ALL.getChild("left_arm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 9.0F, 0.0F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 26).addBox(-7.0F, -8.0F, -6.0F, 14.0F, 13.0F, 9.0F, new CubeDeformation(0.0F))
        .texOffs(0, 0).addBox(-8.5F, -9.0F, -18.0F, 17.0F, 14.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 7.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(80, 9).addBox(-4.5F, -12.0F, -9.0F, 9.0F, 15.0F, 11.0F, new CubeDeformation(0.0F))
        .texOffs(0, 71).addBox(-3.0F, -3.0F, -13.0F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, -17.0F));

        PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(58, 21).addBox(0.0F, -1.5F, -0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(4.5F, -5.5F, -5.5F));

        PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(58, 21).mirror().addBox(-2.0F, -1.5F, -0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-4.5F, -5.5F, -5.5F));

        PartDefinition left_eyeBall = head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(86, 39).addBox(-1.5F, -1.0F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.offset(2.0F, -4.0F, -9.0F));

        PartDefinition right_eyeBall = head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(86, 39).mirror().addBox(-2.5F, -1.0F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(-2.0F, -4.0F, -9.0F));

        PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(52, 51).addBox(-4.0F, -1.0F, -3.5F, 7.0F, 14.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, 2.0F, 7.5F));

        PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(52, 51).mirror().addBox(-3.0F, -1.0F, -3.5F, 7.0F, 14.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-7.0F, 2.0F, 7.5F));

        PartDefinition right_arm = ALL.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 48).mirror().addBox(-3.0F, -1.0F, -3.0F, 7.0F, 17.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-8.0F, -1.0F, -10.0F));

        PartDefinition left_arm = ALL.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 48).addBox(-4.0F, -1.0F, -3.0F, 7.0F, 17.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, -1.0F, -10.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    protected AnimationDefinition comboAnimation(int index) {
        return switch (index) {
            case 1 -> GorillaAnimations.ATTACK_STRIKE;
            case 2 -> GorillaAnimations.ATTACK_STRIKE_2;
            case 3 -> GorillaAnimations.ATTACK_STRIKE_3;
            default -> null;
        };
    }

    @Override
    protected float comboSpeed(int index) {
        return switch (index) {
            case 1 -> 1.2f;
            case 2 -> 1.2f;
            case 3 -> 1.1f;
            default -> 1.0f;
        };
    }

    @Override
    public void setupAnim(T gorilla, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.currentEntity = gorilla;
        float prevSwing = this.prevLimbSwing;
        this.prevLimbSwing = limbSwing;
        this.root().getAllParts().forEach(ModelPart::resetPose);

        if (gorilla.isBaby()) {
            float maturation = (float) gorilla.getMaturationPercentage() / 100f;
            float headScale = 1.5f - (1.5f - 1.0f) * maturation;
            this.head.xScale *= headScale;
            this.head.yScale *= headScale;
            this.head.zScale *= headScale;
        }

        if (gorilla.isNapping() || gorilla.isSleeping()) {
            this.animate(gorilla.restAnimationState, GorillaAnimations.SIT, ageInTicks, 1.0f);
            this.head.xRot += gorilla.getSedatedHeadNod(ageInTicks);
            captureBodyState(gorilla);
            return;
        }

        float pt = partialTick(gorilla, ageInTicks);
        float climbWeight = smoothStep(gorilla.climbBlend(pt));
        this.applyHeadRotation(netHeadYaw, headPitch * (1f - climbWeight));

        if (gorilla.isClimbing() && climbWeight >= 0.999f) {
            float lookTwist = applyClimb(gorilla, ageInTicks, 1f, netHeadYaw);
            float twist = applyWallRockThrow(gorilla, pt, 1f) + applyWallRiderLaunch(gorilla, pt, 1f);
            captureBodyState(gorilla, 0f, twist + lookTwist);
            return;
        }

        if (gorilla.isVaulting() && gorilla.vaultProgress(pt) < 1f) {
            applyVault(gorilla, pt);
            captureBodyState(gorilla, 0f);
            return;
        }

        if (gorilla.isMad()) {
            this.left_eyeBall.xScale = 0;
            this.left_eyeBall.yScale = 0;
            this.left_eyeBall.zScale = 0;

            this.right_eyeBall.xScale = 0;
            this.right_eyeBall.yScale = 0;
            this.right_eyeBall.zScale = 0;
        }

        float ultimateWeight = chestBeatWeight(gorilla, pt);

        if (gorilla.isChestBeating() && ultimateWeight >= 0.999f) {
            this.animate(gorilla.chestBeatAnimationState, GorillaAnimations.ULTIMATE, ageInTicks, 1.0f);
            captureBodyState(gorilla, this.ALL.xRot);
            return;
        }

        if (gorilla.transitionIdleSit.isStarted()) {
            this.animate(gorilla.transitionIdleSit, GorillaAnimations.TRANSITION_IDLE_SIT, ageInTicks, 1.0f);
            captureBodyState(gorilla);
            return;
        }

        if (gorilla.transitionSitIdle.isStarted()) {
            this.animate(gorilla.transitionSitIdle, GorillaAnimations.TRANSITION_SIT_IDLE, ageInTicks, 1.0f);
            captureBodyState(gorilla);
            return;
        }

        if (gorilla.isSitting()) {
            this.animate(gorilla.sittingAnimationState, GorillaAnimations.SIT, ageInTicks, 1.0f);
            captureBodyState(gorilla);
            return;
        }

        float slideWeight = smoothStep(gorilla.slideBlend(pt));
        float stanceWeight = 1f - ultimateWeight;

        animateStance(gorilla, limbSwing, limbSwingAmount * (1f - slideWeight), ageInTicks, prevSwing, pt, stanceWeight);

        if (comboOwnsArms(gorilla)) {
            this.left_arm.resetPose();
            this.right_arm.resetPose();
        }

        animateCombos(gorilla, ageInTicks, COMBO_ANIMATION_SPEED);

        float ignoredXRot = 0f;
        if (ultimateWeight > 0.001f) {
            float before = this.ALL.xRot;
            gorilla.chestBeatAnimationState.updateTime(ageInTicks, 1.0f);
            gorilla.chestBeatAnimationState.ifStarted(state -> KeyframeAnimations.animate(
                    this, GorillaAnimations.ULTIMATE, state.getAccumulatedTime(), ultimateWeight, ANIM_VECTOR));
            ignoredXRot = this.ALL.xRot - before;
        }

        applyRideReactions(gorilla, pt);
        applyTerrain(gorilla, pt);
        ignoredXRot += gorilla.slopePitch(pt);
        applySlide(gorilla, ageInTicks, pt, slideWeight);
        applyRockThrow(gorilla, ageInTicks, pt, 1f - climbWeight);
        applyRiderLaunch(gorilla, pt, 1f - climbWeight);

        float ignoredZRot = 0f;
        if (climbWeight > 0.001f) {
            ignoredZRot = applyClimb(gorilla, ageInTicks, climbWeight, netHeadYaw);
            ignoredZRot += applyWallRockThrow(gorilla, pt, climbWeight) + applyWallRiderLaunch(gorilla, pt, climbWeight);
        }

        captureBodyState(gorilla, ignoredXRot, ignoredZRot);
    }

    private void animateStance(T gorilla, float limbSwing, float limbSwingAmount, float ageInTicks, float prevSwing,
                               float pt, float stanceWeight) {
        if (stanceWeight <= 0.001f) return;

        float moveWeight = Math.min(limbSwingAmount * MOVE_BLEND_FACTOR, 1.0f);
        float rideWeight = smoothStep(gorilla.rideBlend(pt));

        if (moveWeight < 1.0f && !gorilla.isCombo()) {
            float idleWeight = 1.0f - moveWeight;
            KeyframeAnimations.animate(this, GorillaAnimations.MISC_IDLE,
                    (long) (ageInTicks * 50.0f), idleWeight * stanceWeight, ANIM_VECTOR);

            this.body.xRot += RIDDEN_IDLE_BODY_PITCH * idleWeight * rideWeight * stanceWeight;
            this.head.xRot += RIDDEN_IDLE_HEAD_COMP * idleWeight * rideWeight * stanceWeight;

            if (gorilla.miscIdleAnimationState.isStarted()) {
                this.animate(gorilla.miscIdleAnimationState, GorillaAnimations.MISC_IDLE_2, ageInTicks, 1.0f);
            }
        }

        if (moveWeight > 0f) {
            float run = smoothStep(gorilla.runBlend(pt));
            float walkPart = moveWeight * (1f - run) * stanceWeight;
            float runPart = moveWeight * run * stanceWeight;

            if (walkPart > 0.001f) {
                KeyframeAnimations.animate(this, GorillaAnimations.MOVE_WALK,
                        (long) (limbSwing * 50.0f * WALK_ANIM_SPEED), walkPart, ANIM_VECTOR);
            }
            if (runPart > 0.001f) {
                KeyframeAnimations.animate(this, GorillaAnimations.MOVE_RUN,
                        (long) (limbSwing * 50.0f * RUN_ANIM_SPEED), runPart, ANIM_VECTOR);
            }

            boolean running = run >= 0.5f;
            AnimationDefinition move = running ? GorillaAnimations.MOVE_RUN : GorillaAnimations.MOVE_WALK;
            float speed = running ? RUN_ANIM_SPEED : WALK_ANIM_SPEED;
            long rightMark = running ? RUN_STEP_RIGHT_MS : WALK_STEP_RIGHT_MS;
            long leftMark = running ? RUN_STEP_LEFT_MS : WALK_STEP_LEFT_MS;
            if (walkAnimCrossed(move, limbSwing, prevSwing, speed, rightMark)) gorilla.onRightFootDown();
            if (walkAnimCrossed(move, limbSwing, prevSwing, speed, leftMark)) gorilla.onLeftFootDown();
        }
    }

    private boolean walkAnimCrossed(AnimationDefinition animation, float limbSwing, float prevSwing,
                                    float speedScale, long triggerTimeMs) {
        long durationMs = (long) (animation.lengthInSeconds() * 1000f);
        if (durationMs <= 0) return false;

        long cur = ((long) (limbSwing * 50f * speedScale)) % durationMs;
        long prev = ((long) (prevSwing * 50f * speedScale)) % durationMs;

        if (prev <= cur) return prev < triggerTimeMs && cur >= triggerTimeMs;
        return triggerTimeMs <= cur || triggerTimeMs > prev;
    }

    private static boolean comboOwnsArms(GorillaEntity gorilla) {
        return gorilla.isCombo()
                || gorilla.attack1Combo.isStarted()
                || gorilla.attack2Combo.isStarted()
                || gorilla.attack3Combo.isStarted();
    }

    private static float smoothStep(float t) {
        float u = Mth.clamp(t, 0f, 1f);
        return u * u * (3f - 2f * u);
    }

    private static float swell(float t) {
        float u = Mth.clamp(t, 0f, 1f);
        return u < 0.5f ? smoothStep(u * 2f) : 1f - smoothStep((u - 0.5f) * 2f);
    }

    private static float easeOutCubic(float t) {
        float u = 1f - Mth.clamp(t, 0f, 1f);
        return 1f - u * u * u;
    }

    private static float partialTick(GorillaEntity gorilla, float ageInTicks) {
        return Mth.clamp(ageInTicks - gorilla.tickCount, 0f, 1f);
    }

    private static float chestBeatWeight(GorillaEntity gorilla, float pt) {
        int tick = gorilla.getChestBeatTick();
        if (tick <= 0) return 0f;
        float total = OWAttacksConstants.Gorilla.CHEST_BEAT_WINDUP_TICKS + OWAttacksConstants.Gorilla.CHEST_BEAT_GESTURE_TICKS;
        float elapsed = total - tick + pt;
        float remaining = Math.max(0f, tick - pt);
        return Math.min(smoothStep(elapsed / CHEST_BEAT_BLEND_TICKS), smoothStep(remaining / CHEST_BEAT_BLEND_TICKS));
    }

    private void applyRideReactions(T gorilla, float pt) {
        float mount = gorilla.mountReactProgress(pt);
        if (mount >= 0f) {
            float fade = (1f - mount) * (1f - mount);
            float settle = (float) Math.sin(Math.PI * Math.min(mount * 2.2f, 1f));
            float sway = (float) Math.sin(mount * Math.PI * 3f) * fade;

            this.ALL.y += 1.8f * settle * (1f - mount);
            this.ALL.zRot += 0.06f * sway;
            this.body.yRot += 0.16f * sway;
            this.body.xRot += 0.08f * settle;
            this.head.yRot += 0.42f * settle * (1f - mount * 0.5f);
            this.head.xRot -= 0.18f * settle;
            this.left_arm.xRot -= 0.12f * settle;
            this.right_arm.xRot -= 0.12f * settle;
        }

        float dismount = gorilla.dismountReactProgress(pt);
        if (dismount >= 0f) {
            float fade = (1f - dismount) * (1f - dismount);
            float shake = (float) Math.sin(dismount * Math.PI * 5f) * fade;
            float rise = (float) Math.sin(Math.PI * Math.min(dismount * 2f, 1f)) * (1f - dismount);

            this.ALL.y -= 1.0f * rise;
            this.ALL.zRot += 0.09f * shake;
            this.body.zRot += 0.07f * shake;
            this.body.xRot -= 0.10f * rise;
            this.head.yRot -= 0.35f * rise;
            this.head.zRot -= 0.10f * shake;
        }
    }

    private void applyTerrain(T gorilla, float pt) {
        this.ALL.xRot += gorilla.slopePitch(pt);

        float land = gorilla.landPulse(pt);
        if (land > 0.001f) {
            this.ALL.y += 2.2f * land;
            this.body.xRot += 0.10f * land;
            this.head.xRot += 0.10f * land;
            this.left_arm.xRot -= 0.15f * land;
            this.right_arm.xRot -= 0.15f * land;
        }
    }

    private void applySlide(T gorilla, float ageInTicks, float pt, float weight) {
        if (weight <= 0.001f) return;

        float speed = gorilla.slideSpeedFactor(pt);
        float steer = Mth.clamp(gorilla.slideSteer(pt), -1f, 1f);
        float impact = gorilla.slideImpact(pt);
        float sway = (float) Math.sin(ageInTicks * SLIDE_SWAY_RATE) * (0.4f + 0.6f * speed);
        float drift = (float) Math.sin(ageInTicks * SLIDE_SWAY_RATE * 0.6f + 1.1f) * speed;

        this.ALL.y += (SLIDE_DROP + impact * 1.6f) * weight;
        this.ALL.xRot += (SLIDE_LEAN_BACK - impact * 0.08f - speed * 0.04f) * weight;
        this.ALL.zRot += (-steer * SLIDE_BANK + sway * 0.03f) * weight;
        this.ALL.yRot += (steer * SLIDE_DRIFT + drift * 0.04f) * weight;

        this.body.xRot += (-0.12f + impact * 0.10f) * weight;
        this.body.zRot += (-steer * 0.08f + sway * 0.02f) * weight;

        this.head.xRot += (0.42f + speed * 0.04f - impact * 0.10f) * weight;
        this.head.yRot -= (steer * 0.12f + drift * 0.05f) * weight;

        float rightInner = Mth.clamp(0.5f + 0.5f * steer, 0f, 1f);
        float leftInner = 1f - rightInner;

        this.right_arm.xRot += (Mth.lerp(rightInner, SLIDE_ARM_LIFT, SLIDE_ARM_DRAG) - sway * 0.06f + impact * 0.3f) * weight;
        this.left_arm.xRot += (Mth.lerp(leftInner, SLIDE_ARM_LIFT, SLIDE_ARM_DRAG) + sway * 0.06f + impact * 0.3f) * weight;
        this.right_arm.zRot += (0.12f + 0.45f * (1f - rightInner)) * weight;
        this.left_arm.zRot -= (0.12f + 0.45f * (1f - leftInner)) * weight;

        this.left_leg.xRot += (SLIDE_LEG_FORWARD + sway * 0.04f) * weight;
        this.right_leg.xRot += (SLIDE_LEG_FORWARD - sway * 0.04f) * weight;
        this.left_leg.zRot -= (0.18f + steer * 0.06f) * weight;
        this.right_leg.zRot += (0.18f - steer * 0.06f) * weight;
    }

    private static float rockThrowKeep(GorillaEntity gorilla, float pt) {
        int tick = gorilla.getRockThrowTick();
        if (tick <= 0) return 0f;
        float duration = OWAttacksConstants.Gorilla.ROCK_THROW_RELEASE_TICKS;
        float elapsed = duration - tick + pt;
        return 1f - smoothStep((elapsed - ROCK_STRIKE_TICKS) / (duration - ROCK_STRIKE_TICKS));
    }

    private static float rockArmWeight(GorillaEntity gorilla, float pt) {
        if (gorilla.getRockThrowTick() > 0) return rockThrowKeep(gorilla, pt) * gorilla.rockReleaseWeight();
        return smoothStep(gorilla.rockAim(pt));
    }

    private static float launchThrowKeep(GorillaEntity gorilla, float pt) {
        int tick = gorilla.getRiderLaunchTick();
        if (tick <= 0) return 0f;
        float duration = OWAttacksConstants.Gorilla.RIDER_LAUNCH_RELEASE_TICKS;
        float elapsed = duration - tick + pt;
        return 1f - smoothStep((elapsed - LAUNCH_HURL_TICKS) / (duration - LAUNCH_HURL_TICKS));
    }

    private static float launchArmWeight(GorillaEntity gorilla, float pt) {
        if (gorilla.getRiderLaunchTick() > 0) return launchThrowKeep(gorilla, pt) * gorilla.launchReleaseWeight();
        return smoothStep(gorilla.launchAim(pt));
    }

    private float applyWallRockThrow(T gorilla, float pt, float weight) {
        if (weight <= 0.001f) return 0f;
        float side = gorilla.wallThrowSide();
        int tick = gorilla.getRockThrowTick();

        if (tick <= 0) {
            float aim = smoothStep(gorilla.rockAim(pt)) * weight;
            if (aim <= 0.001f) return 0f;
            float held = gorilla.isRockCharging() ? gorilla.clientRockChargeTicks + pt : 0f;
            float tremble = Mth.clamp(held / AIM_TREMBLE_RAMP, 0f, 1f) * (float) Math.sin(held * 1.05f);
            float breathe = (float) Math.sin(held * 0.16f);
            return wallCock(side, aim, tremble, breathe);
        }

        float duration = OWAttacksConstants.Gorilla.ROCK_THROW_RELEASE_TICKS;
        float elapsed = duration - tick + pt;
        return wallRelease(side, gorilla.rockReleaseWeight(),
                easeOutCubic(elapsed / ROCK_STRIKE_TICKS),
                rockThrowKeep(gorilla, pt) * weight,
                swell(elapsed / (duration * 0.7f)) * weight);
    }

    private float applyWallRiderLaunch(T gorilla, float pt, float weight) {
        if (weight <= 0.001f) return 0f;
        float side = gorilla.wallThrowSide();
        int tick = gorilla.getRiderLaunchTick();

        if (tick <= 0) {
            float aim = smoothStep(gorilla.launchAim(pt)) * weight;
            if (aim <= 0.001f) return 0f;
            float held = gorilla.isLaunchCharging() ? gorilla.clientLaunchChargeTicks + pt : 0f;
            float coil = Mth.clamp(held / LAUNCH_COIL_TICKS, 0f, 1f);
            float tremble = coil * (float) Math.sin(held * 1.3f);
            float breathe = (float) Math.sin(held * 0.22f);
            return wallCock(side, aim, tremble, breathe);
        }

        float duration = OWAttacksConstants.Gorilla.RIDER_LAUNCH_RELEASE_TICKS;
        float elapsed = duration - tick + pt;
        return wallRelease(side, gorilla.launchReleaseWeight(),
                easeOutCubic(elapsed / LAUNCH_HURL_TICKS),
                launchThrowKeep(gorilla, pt) * weight,
                swell(elapsed / (duration * 0.6f)) * weight);
    }

    private float wallCock(float side, float aim, float tremble, float breathe) {
        float twistBefore = this.body.zRot;
        ModelPart arm = side > 0f ? this.right_arm : this.left_arm;
        ModelPart grip = side > 0f ? this.left_arm : this.right_arm;

        arm.xRot += (WALL_THROW_COCK_X + tremble * 0.04f) * aim;
        arm.zRot += side * (WALL_THROW_COCK_Z + breathe * 0.03f) * aim;
        this.body.zRot += side * WALL_THROW_TWIST * aim;
        this.body.xRot += WALL_THROW_CHEST_OFF * aim;
        this.head.xRot += WALL_THROW_HEAD_LOOK * aim;
        this.head.yRot += side * WALL_THROW_HEAD_TURN * aim;
        grip.xRot += WALL_THROW_GRIP_REACH * aim;
        return this.body.zRot - twistBefore;
    }

    private float wallRelease(float side, float start, float strike, float keep, float lunge) {
        float twistBefore = this.body.zRot;
        ModelPart arm = side > 0f ? this.right_arm : this.left_arm;
        ModelPart grip = side > 0f ? this.left_arm : this.right_arm;

        arm.xRot += Mth.lerp(strike, WALL_THROW_COCK_X * start, WALL_THROW_FOLLOW_X) * keep;
        arm.zRot += side * Mth.lerp(strike, WALL_THROW_COCK_Z * start, 0.25f) * keep;
        this.body.zRot += side * Mth.lerp(strike, WALL_THROW_TWIST * start, -0.14f) * keep;
        this.body.xRot += WALL_THROW_CHEST_OFF * start * keep - 0.10f * lunge;
        this.head.xRot += Mth.lerp(strike, WALL_THROW_HEAD_LOOK * start, -0.45f) * keep;
        this.head.yRot += side * Mth.lerp(strike, WALL_THROW_HEAD_TURN * start, -0.10f) * keep;
        grip.xRot += WALL_THROW_GRIP_REACH * start * keep;
        return this.body.zRot - twistBefore;
    }

    private void applyRockThrow(T gorilla, float ageInTicks, float pt, float weight) {
        if (weight <= 0.001f) return;
        int tick = gorilla.getRockThrowTick();
        boolean throwing = tick > 0;
        float aim = smoothStep(gorilla.rockAim(pt)) * weight;

        if (aim > 0.001f) {
            float held = gorilla.isRockCharging() ? gorilla.clientRockChargeTicks + pt : 0f;
            float sway = (float) Math.sin(held * 0.16f);
            float tremble = Mth.clamp(held / AIM_TREMBLE_RAMP, 0f, 1f) * (float) Math.sin(held * 1.05f);

            this.ALL.xRot += (AIM_BODY_PITCH + sway * 0.022f) * aim;
            this.ALL.y += AIM_BODY_SINK * aim;
            this.ALL.zRot += tremble * 0.012f * aim;

            this.head.xRot += (AIM_HEAD_TUCK + sway * 0.03f) * aim;
            this.head.y += AIM_HEAD_DROP * aim;
            this.head.z += AIM_HEAD_BACK * aim;
            this.body.xRot += AIM_BODY_LIFT * aim;
            this.left_arm.xRot += 0.22f * aim;

            if (!throwing) {
                this.head.yRot += 0.22f * aim;
                this.body.yRot -= 0.30f * aim;
                this.right_arm.xRot -= (2.35f + tremble * 0.05f) * aim;
                this.right_arm.zRot -= (0.35f + tremble * 0.04f) * aim;
            }
        }

        if (!throwing) return;

        float duration = OWAttacksConstants.Gorilla.ROCK_THROW_RELEASE_TICKS;
        float start = gorilla.rockReleaseWeight();
        float elapsed = duration - tick + pt;
        float strike = easeOutCubic(elapsed / ROCK_STRIKE_TICKS);
        float keep = (1f - smoothStep((elapsed - ROCK_STRIKE_TICKS) / (duration - ROCK_STRIKE_TICKS))) * weight;
        float lunge = swell(elapsed / (duration * 0.8f)) * weight;

        this.right_arm.xRot += Mth.lerp(strike, -2.35f * start, -0.55f) * keep;
        this.right_arm.zRot += Mth.lerp(strike, -0.35f * start, 0.12f) * keep;
        this.body.yRot += Mth.lerp(strike, -0.30f * start, 0.30f) * keep;
        this.head.yRot += Mth.lerp(strike, 0.22f * start, -0.20f) * keep;

        this.ALL.xRot += 0.20f * lunge;
        this.ALL.y += 1.0f * lunge;
        this.head.xRot -= 0.12f * lunge;
        this.left_arm.xRot -= 0.35f * lunge;
    }

    private void applyRiderLaunch(T gorilla, float pt, float weight) {
        if (weight <= 0.001f) return;
        int tick = gorilla.getRiderLaunchTick();
        boolean releasing = tick > 0;
        float aim = smoothStep(gorilla.launchAim(pt)) * weight;

        if (aim > 0.001f) {
            float held = gorilla.isLaunchCharging() ? gorilla.clientLaunchChargeTicks + pt : 0f;
            float coil = Mth.clamp(held / LAUNCH_COIL_TICKS, 0f, 1f);
            float tremble = coil * (float) Math.sin(held * 1.3f);
            float breathe = (float) Math.sin(held * 0.22f);

            this.ALL.y += (LAUNCH_CROUCH + coil * 0.6f) * aim;
            this.ALL.xRot += (0.10f + coil * 0.04f) * aim;
            this.ALL.zRot += tremble * 0.014f * aim;
            this.body.xRot += (-0.22f + breathe * 0.02f) * aim;
            this.head.xRot += 0.38f * aim;
            this.left_leg.xRot += 0.28f * aim;
            this.right_leg.xRot += 0.28f * aim;

            if (!releasing) {
                float armX = (LAUNCH_GRIP_ARM + tremble * 0.05f + breathe * 0.03f) * aim;
                this.left_arm.xRot += armX;
                this.right_arm.xRot += armX;
                this.left_arm.zRot -= 0.32f * aim;
                this.right_arm.zRot += 0.32f * aim;
            }
        }

        if (!releasing) return;

        float duration = OWAttacksConstants.Gorilla.RIDER_LAUNCH_RELEASE_TICKS;
        float start = gorilla.launchReleaseWeight();
        float elapsed = duration - tick + pt;
        float hurl = easeOutCubic(elapsed / LAUNCH_HURL_TICKS);
        float settle = smoothStep((elapsed - LAUNCH_HURL_TICKS) / (duration - LAUNCH_HURL_TICKS));
        float rear = (float) Math.sin(Math.PI * Mth.clamp(elapsed / LAUNCH_REAR_TICKS, 0f, 1f)) * weight;
        float wobblePhase = Mth.clamp((elapsed - LAUNCH_REAR_TICKS) / (duration - LAUNCH_REAR_TICKS), 0f, 1f);
        float wobble = (float) Math.sin(wobblePhase * Math.PI * 2f) * (1f - wobblePhase) * weight;

        float armX = Mth.lerp(settle, Mth.lerp(hurl, LAUNCH_GRIP_ARM * start, LAUNCH_FOLLOW_ARM), FULL_TURN);
        if (weight < 1f) armX = Mth.wrapDegrees(armX * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD * weight;
        this.left_arm.xRot += armX;
        this.right_arm.xRot += armX;
        float spread = (1f - settle) * Mth.lerp(hurl, 0.32f * start, 0.18f) * weight;
        this.left_arm.zRot -= spread;
        this.right_arm.zRot += spread;

        this.ALL.xRot += -0.48f * rear + 0.06f * wobble;
        this.ALL.y += -3.0f * rear + 0.4f * wobble;
        this.body.xRot += -0.20f * rear;
        this.head.xRot += -0.55f * rear;
        this.left_leg.xRot += 0.35f * rear;
        this.right_leg.xRot += 0.35f * rear;
    }

    private void applyHeadRotation(float netHeadYaw, float headPitch) {
        netHeadYaw = Mth.clamp(netHeadYaw, -35.0F, 35.0F);
        headPitch = Mth.clamp(headPitch, -30.0F, 30.0F);

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
    }

    private void captureBodyState(T gorilla) {
        captureBodyState(gorilla, 0f, 0f);
    }

    private void captureBodyState(T gorilla, float ignoredXRot) {
        captureBodyState(gorilla, ignoredXRot, 0f);
    }

    private void captureBodyState(T gorilla, float ignoredXRot, float ignoredZRot) {
        if (!gorilla.level().isClientSide()) return;

        float ySum = this.ALL2.y + this.ALL.y + this.body.y;
        gorilla.bodyAnimY = ySum - REST_POSE_Y_SUM;

        gorilla.setBodyZRot((float) Math.toDegrees(this.ALL.zRot + this.body.zRot - ignoredZRot));
        gorilla.setBodyXRot((float) -Math.toDegrees(this.ALL.xRot + this.body.xRot - ignoredXRot));
    }

    private float applyClimb(T gorilla, float ageInTicks, float weight, float lookYaw) {
        float pt = partialTick(gorilla, ageInTicks);
        float elapsed = gorilla.climbPhase(pt);

        float theta = elapsed / GorillaEntity.CLIMB_SURGE_TICKS * (float) Math.PI;
        float hang = smoothStep(gorilla.hangBlend(pt));
        float active = 1f - hang;
        float cycle = (float) Math.cos(theta);
        float swing = Math.signum(cycle) * (float) Math.pow(Math.abs(cycle), CLIMB_SWING_SHAPE) * active;
        float lift = (float) Math.sin(theta * 2f) * active;
        float leftReach = Math.max(-swing, 0f);
        float rightReach = Math.max(swing, 0f);

        float steer = Mth.clamp(gorilla.climbSteer(pt), -1f, 1f);
        float idle = (float) Math.sin(ageInTicks * 0.09f);
        float breathe = idle * hang;

        float throwSide = gorilla.wallThrowSide();
        float throwWeight = Mth.clamp(Math.max(rockArmWeight(gorilla, pt), launchArmWeight(gorilla, pt)), 0f, 1f);
        float throwFree = 1f - throwWeight;
        float rightAvailable = throwSide > 0f ? throwFree : 1f;
        float leftAvailable = throwSide < 0f ? throwFree : 1f;

        float grip = Mth.clamp(gorilla.hangGrip(pt), -1f, 1f);
        float leftGrip = smoothStep(grip + 1f);
        float rightGrip = smoothStep(1f - grip);
        if (throwSide > 0f) leftGrip = Math.max(leftGrip, throwWeight);
        else rightGrip = Math.max(rightGrip, throwWeight);
        float leftFree = 1f - leftGrip;
        float rightFree = 1f - rightGrip;

        float rest = hang * weight;
        float dangle = (float) Math.sin(ageInTicks * 0.07f + 1.3f);
        float regrip = swell(((ageInTicks % HANG_REGRIP_PERIOD) / HANG_REGRIP_PERIOD - 0.8f) / 0.2f);

        float gripX = CLIMB_ARM_BASE - 0.10f - regrip * 0.25f;
        float freeX = CLIMB_FREE_ARM_HANG + idle * 0.06f + dangle * 0.10f;
        float freeZ = 0.15f + idle * 0.04f + dangle * 0.06f;

        float leftHangX = Mth.lerp(leftGrip, freeX, gripX);
        float leftHangZ = -Mth.lerp(leftGrip, freeZ, 0.26f);
        float rightHangX = Mth.lerp(rightGrip, freeX, gripX);
        float rightHangZ = Mth.lerp(rightGrip, freeZ, 0.26f);

        float leftClimbX = CLIMB_ARM_BASE + swing * CLIMB_ARM_SWING;
        float leftClimbZ = -(0.16f + leftReach * CLIMB_ARM_REACH_SPREAD);
        float rightClimbX = CLIMB_ARM_BASE - swing * CLIMB_ARM_SWING;
        float rightClimbZ = 0.16f + rightReach * CLIMB_ARM_REACH_SPREAD;

        this.ALL.xRot += CLIMB_BODY_PITCH * weight;
        this.ALL.y += (3.0f - lift * 1.1f + hang * 0.8f) * weight;
        this.ALL.z -= CLIMB_WALL_HUG * weight;
        this.ALL.zRot += (swing * 0.16f + steer * 0.13f * hang) * weight;

        this.left_arm.xRot += Mth.lerp(hang, leftClimbX, leftHangX) * weight * leftAvailable;
        this.left_arm.zRot += Mth.lerp(hang, leftClimbZ, leftHangZ) * weight * leftAvailable;
        this.right_arm.xRot += Mth.lerp(hang, rightClimbX, rightHangX) * weight * rightAvailable;
        this.right_arm.zRot += Mth.lerp(hang, rightClimbZ, rightHangZ) * weight * rightAvailable;

        this.left_leg.xRot += (CLIMB_LEG_BASE - swing * CLIMB_LEG_SWING + hang * 0.22f) * weight;
        this.right_leg.xRot += (CLIMB_LEG_BASE + swing * CLIMB_LEG_SWING + hang * 0.34f) * weight;
        this.left_leg.zRot -= (0.20f + hang * 0.08f) * weight;
        this.right_leg.zRot += (0.20f + hang * 0.14f) * weight;

        this.body.xRot += (0.10f + lift * 0.05f + breathe * 0.035f) * weight;
        this.body.zRot += swing * 0.10f * weight;
        this.body.yRot += steer * 0.22f * hang * weight;
        this.head.xRot += (0.42f - lift * 0.12f + hang * CLIMB_HANG_HEAD_DROP) * weight;
        this.head.yRot += swing * 0.16f * weight;

        if (rest > 0.001f) {
            float breath = (float) Math.sin(ageInTicks * 0.11f);
            float sway = (float) Math.sin(ageInTicks * 0.045f);
            float kick = (float) Math.sin(ageInTicks * 0.09f + 0.6f);
            float scan = (float) Math.sin(ageInTicks * 0.028f);

            this.ALL.y += breath * 0.35f * rest;
            this.ALL.zRot += sway * 0.045f * rest;
            this.body.xRot += breath * 0.05f * rest;
            this.left_leg.xRot += kick * 0.10f * rest;
            this.right_leg.xRot -= kick * 0.10f * rest;
            this.head.yRot += scan * 0.18f * rest;
            this.head.zRot += sway * 0.05f * rest;
        }

        float look = Mth.clamp(lookYaw / CLIMB_LOOK_TWIST_RANGE, -1f, 1f);
        float lookWeight = hang * throwFree * weight;
        float lookTwist = look * CLIMB_LOOK_TWIST * lookWeight;
        this.body.zRot += lookTwist;
        this.body.xRot -= Math.abs(look) * CLIMB_LOOK_CHEST_OFF * lookWeight;
        this.head.zRot += look * CLIMB_LOOK_HEAD_ROLL * lookWeight;
        if (look > 0f) this.right_arm.zRot += look * CLIMB_LOOK_ARM_OPEN * lookWeight * rightFree;
        if (look < 0f) this.left_arm.zRot += look * CLIMB_LOOK_ARM_OPEN * lookWeight * leftFree;
        return lookTwist;
    }

    private float applyVault(T gorilla, float pt) {
        float p = gorilla.vaultProgress(pt);
        float unwind = GorillaEntity.vaultUnwind(p);
        float press = swell(p / VAULT_PRESS_END);
        float reach = swell((p - VAULT_REACH_START) / (1f - VAULT_REACH_START));
        float upright = 1f - unwind;

        this.ALL.xRot += CLIMB_BODY_PITCH * upright + reach * 0.18f;
        this.ALL.y += 3.0f * upright - press * 0.6f;
        this.ALL.z -= CLIMB_WALL_HUG * upright;

        float arms = CLIMB_ARM_BASE * upright + press * 0.85f + reach * 0.40f;
        this.left_arm.xRot += arms;
        this.right_arm.xRot += arms;
        this.left_arm.zRot -= 0.16f * upright;
        this.right_arm.zRot += 0.16f * upright;

        float legs = CLIMB_LEG_BASE * upright + press * 0.30f - reach * 0.45f;
        this.left_leg.xRot += legs;
        this.right_leg.xRot += legs;

        this.body.xRot += press * 0.12f;
        this.head.xRot += 0.35f * upright - press * 0.18f;
        return (float) -Math.toDegrees(CLIMB_BODY_PITCH * upright);
    }

    public void renderGeometryOnly(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        renderGeometryOnly(poseStack, vertexConsumer, packedLight, packedOverlay, color);

        if (this.currentEntity != null && this.currentEntity.isRockCharging()) {
            float scale = ownFirstPersonView(this.currentEntity) ? HELD_ROCK_SCALE_SELF : HELD_ROCK_SCALE;
            renderRockInHand(this.currentEntity, poseStack, packedLight, scale);
        }
    }

    private static boolean ownFirstPersonView(GorillaEntity gorilla) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        if (!mc.options.getCameraType().isFirstPerson()) return false;
        return gorilla.getControllingPassenger() == mc.player;
    }

    public void setBufferSource(MultiBufferSource bufferSource) {
        this.currentBufferSource = bufferSource;
    }

    private void renderRockInHand(T gorilla, PoseStack poseStack, int packedLight, float scale) {
        MultiBufferSource bufferSource = this.currentBufferSource;
        if (bufferSource == null) return;

        poseStack.pushPose();

        boolean leftHand = gorilla.wallThrowSide() < 0f;
        this.ALL2.translateAndRotate(poseStack);
        this.ALL.translateAndRotate(poseStack);
        (leftHand ? this.left_arm : this.right_arm).translateAndRotate(poseStack);

        poseStack.translate(leftHand ? -0.03D : 0.03D, 1.0D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        poseStack.scale(scale, scale, scale);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                HELD_ROCK,
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                gorilla.level(),
                0
        );

        poseStack.popPose();
    }

    @Override
    public ModelPart root() {
        return this.ALL2;
    }

    public void copyPoseFrom(GorillaModel<?> src) {
        this.ALL2.copyFrom(src.ALL2);
        this.ALL.copyFrom(src.ALL);
        this.body.copyFrom(src.body);
        this.head.copyFrom(src.head);
        this.left_ear.copyFrom(src.left_ear);
        this.right_ear.copyFrom(src.right_ear);
        this.left_eyeBall.copyFrom(src.left_eyeBall);
        this.right_eyeBall.copyFrom(src.right_eyeBall);
        this.left_arm.copyFrom(src.left_arm);
        this.right_arm.copyFrom(src.right_arm);
        this.left_leg.copyFrom(src.left_leg);
        this.right_leg.copyFrom(src.right_leg);
    }
}
