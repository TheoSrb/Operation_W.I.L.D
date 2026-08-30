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
    private static final float CLIMB_ARM_BASE = -2.40f;
    private static final float CLIMB_ARM_SWING = 0.62f;
    private static final float CLIMB_LEG_BASE = 0.95f;
    private static final float CLIMB_LEG_SWING = 0.45f;

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

        this.applyHeadRotation(netHeadYaw, headPitch);

        if (gorilla.isClimbing()) {
            applyClimb(gorilla, ageInTicks);
            applyRockThrow(gorilla, ageInTicks);
            applyRiderLaunch(gorilla, ageInTicks);
            captureBodyState(gorilla, CLIMB_BODY_PITCH);
            return;
        }

        if (gorilla.isVaulting()) {
            applyVault(gorilla, ageInTicks);
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

        if (gorilla.isChestBeating()) {
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

        if (gorilla.isNapping() || gorilla.isSleeping() || gorilla.isSitting()) {
            this.animate(gorilla.sittingAnimationState, GorillaAnimations.SIT, ageInTicks, 1.0f);
            captureBodyState(gorilla);
            return;
        }

        animateStance(gorilla, limbSwing, limbSwingAmount, ageInTicks, prevSwing);

        if (comboOwnsArms(gorilla)) {
            this.left_arm.resetPose();
            this.right_arm.resetPose();
        }

        animateCombos(gorilla, ageInTicks, COMBO_ANIMATION_SPEED);

        applyRockThrow(gorilla, ageInTicks);
        applyRiderLaunch(gorilla, ageInTicks);

        captureBodyState(gorilla);
    }

    private void animateStance(T gorilla, float limbSwing, float limbSwingAmount, float ageInTicks, float prevSwing) {
        float moveWeight = Math.min(limbSwingAmount * MOVE_BLEND_FACTOR, 1.0f);
        boolean running = gorilla.isRunning();

        if (moveWeight < 1.0f && !gorilla.isCombo()) {
            float idleWeight = 1.0f - moveWeight;
            KeyframeAnimations.animate(this, GorillaAnimations.MISC_IDLE,
                    (long) (ageInTicks * 50.0f), idleWeight, ANIM_VECTOR);

            if (gorilla.isVehicle()) {
                this.body.xRot += RIDDEN_IDLE_BODY_PITCH * idleWeight;
                this.head.xRot += RIDDEN_IDLE_HEAD_COMP * idleWeight;
            }

            if (gorilla.miscIdleAnimationState.isStarted()) {
                this.animate(gorilla.miscIdleAnimationState, GorillaAnimations.MISC_IDLE_2, ageInTicks, 1.0f);
            }
        }

        if (moveWeight > 0f) {
            float speed = running ? RUN_ANIM_SPEED : WALK_ANIM_SPEED;
            AnimationDefinition move = running ? GorillaAnimations.MOVE_RUN : GorillaAnimations.MOVE_WALK;
            KeyframeAnimations.animate(this, move, (long) (limbSwing * 50.0f * speed), moveWeight, ANIM_VECTOR);

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

    private static float partialTick(GorillaEntity gorilla, float ageInTicks) {
        return Mth.clamp(ageInTicks - gorilla.tickCount, 0f, 1f);
    }

    private void applyRockThrow(T gorilla, float ageInTicks) {
        if (gorilla.isRockCharging()) {
            float held = gorilla.clientRockChargeTicks + partialTick(gorilla, ageInTicks);
            float settle = smoothStep(held / 6f);
            float sway = (float) Math.sin(held * 0.16f);
            float tremble = Mth.clamp(held / AIM_TREMBLE_RAMP, 0f, 1f) * (float) Math.sin(held * 1.05f);

            this.ALL.xRot += (AIM_BODY_PITCH + sway * 0.022f) * settle;
            this.ALL.y += AIM_BODY_SINK * settle;
            this.ALL.zRot += tremble * 0.012f;

            this.head.xRot += (AIM_HEAD_TUCK + sway * 0.03f) * settle;
            this.head.y += AIM_HEAD_DROP * settle;
            this.head.z += AIM_HEAD_BACK * settle;
            this.head.yRot += 0.22f * settle;

            this.body.yRot -= 0.30f * settle;
            this.body.xRot += AIM_BODY_LIFT * settle;

            this.right_arm.xRot -= (2.35f + tremble * 0.05f) * settle;
            this.right_arm.zRot -= (0.35f + tremble * 0.04f) * settle;
            this.left_arm.xRot += 0.22f * settle;
            return;
        }

        int tick = gorilla.getRockThrowTick();
        if (tick <= 0) return;

        float elapsed = OWAttacksConstants.Gorilla.ROCK_THROW_RELEASE_TICKS - tick + partialTick(gorilla, ageInTicks);
        float progress = Mth.clamp(elapsed / OWAttacksConstants.Gorilla.ROCK_THROW_RELEASE_TICKS, 0f, 1f);
        float whip = smoothStep(progress);

        this.right_arm.xRot += -2.35f + whip * 4.2f;
        this.right_arm.zRot += -0.35f + whip * 0.5f;
        this.body.yRot += -0.30f + whip * 0.55f;
        this.head.yRot += 0.22f - whip * 0.35f;
    }

    private void applyRiderLaunch(T gorilla, float ageInTicks) {
        if (gorilla.isLaunchCharging()) {
            this.right_arm.xRot += 0.85f;
            this.left_arm.xRot += 0.85f;
            this.ALL.y += 1.6f;
            this.body.xRot += 0.22f;
            this.head.xRot -= 0.28f;
            return;
        }

        int tick = gorilla.getRiderLaunchTick();
        if (tick <= 0) return;

        float elapsed = OWAttacksConstants.Gorilla.RIDER_LAUNCH_RELEASE_TICKS - tick + partialTick(gorilla, ageInTicks);
        float progress = Mth.clamp(elapsed / OWAttacksConstants.Gorilla.RIDER_LAUNCH_RELEASE_TICKS, 0f, 1f);
        float sweep = smoothStep(progress);

        this.right_arm.xRot += 0.85f - sweep * 3.6f;
        this.left_arm.xRot += 0.85f - sweep * 3.6f;
        this.ALL.y += 1.6f - sweep * 2.4f;
        this.body.xRot += 0.22f - sweep * 0.45f;
        this.head.xRot += -0.28f + sweep * 0.55f;
    }

    private void applyHeadRotation(float netHeadYaw, float headPitch) {
        netHeadYaw = Mth.clamp(netHeadYaw, -35.0F, 35.0F);
        headPitch = Mth.clamp(headPitch, -30.0F, 30.0F);

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
    }

    private void captureBodyState(T gorilla) {
        captureBodyState(gorilla, 0f);
    }

    private void captureBodyState(T gorilla, float ignoredXRot) {
        if (!gorilla.level().isClientSide()) return;

        float ySum = this.ALL2.y + this.ALL.y + this.body.y;
        gorilla.bodyAnimY = ySum - REST_POSE_Y_SUM;

        gorilla.setBodyZRot((float) Math.toDegrees(this.ALL.zRot + this.body.zRot));
        gorilla.setBodyXRot((float) -Math.toDegrees(this.ALL.xRot + this.body.xRot - ignoredXRot));
    }

    private void applyClimb(T gorilla, float ageInTicks) {
        float elapsed = (gorilla.clientClimbElapsed >= 0 ? gorilla.clientClimbElapsed : gorilla.getClimbTick())
                + partialTick(gorilla, ageInTicks);

        float theta = elapsed / GorillaEntity.CLIMB_SURGE_TICKS * (float) Math.PI;
        float active = 1f - Mth.clamp(gorilla.clientHangBlend, 0f, 1f);
        float swing = (float) Math.cos(theta) * active;
        float lift = (float) Math.sin(theta * 2f) * active;
        float hang = 1f - active;

        float steer = Mth.clamp(gorilla.clientClimbSteer, -1f, 1f);
        float idle = (float) Math.sin(elapsed * 0.09f);
        float breathe = idle * hang;

        boolean freeArmBusy = gorilla.isRockCharging() || gorilla.getRockThrowTick() > 0
                || gorilla.isLaunchCharging() || gorilla.getRiderLaunchTick() > 0;

        this.ALL.xRot += CLIMB_BODY_PITCH;
        this.ALL.y += 3.0f - lift * 0.55f + hang * 0.8f;
        this.ALL.zRot += swing * 0.10f + steer * 0.13f * hang;
        this.ALL.yRot += steer * 0.30f * hang;

        this.left_arm.xRot += CLIMB_ARM_BASE + swing * CLIMB_ARM_SWING - hang * 0.10f;
        this.left_arm.zRot -= 0.16f + swing * 0.07f + hang * 0.10f;

        if (!freeArmBusy) {
            float reachOut = CLIMB_ARM_BASE - swing * CLIMB_ARM_SWING;
            float relaxed = -0.28f + idle * 0.09f;
            this.right_arm.xRot += Mth.lerp(hang, reachOut, relaxed);
            this.right_arm.zRot += Mth.lerp(hang, 0.16f - swing * 0.07f, 0.24f + idle * 0.05f);
        }

        this.left_leg.xRot += CLIMB_LEG_BASE - swing * CLIMB_LEG_SWING + hang * 0.22f;
        this.right_leg.xRot += CLIMB_LEG_BASE + swing * CLIMB_LEG_SWING + hang * 0.34f;
        this.left_leg.zRot -= 0.20f + hang * 0.08f;
        this.right_leg.zRot += 0.20f + hang * 0.14f;

        this.body.xRot += 0.10f + lift * 0.05f + breathe * 0.035f;
        this.body.zRot += swing * 0.05f;
        this.body.yRot += steer * 0.22f * hang;
        this.head.xRot += 0.42f - lift * 0.12f - hang * 0.20f;
        this.head.yRot += swing * 0.16f;
    }

    private void applyVault(T gorilla, float ageInTicks) {
        int tick = gorilla.getVaultTick();
        float elapsed = 5f - tick + partialTick(gorilla, ageInTicks);
        float p = Mth.clamp(elapsed / 5f, 0f, 1f);
        float unwind = smoothStep(p);

        this.ALL.xRot += CLIMB_BODY_PITCH * (1f - unwind) + unwind * 0.22f;
        this.ALL.y += (3.0f) * (1f - unwind);

        float arms = CLIMB_ARM_BASE * (1f - unwind) + unwind * 0.45f;
        this.left_arm.xRot += arms;
        this.right_arm.xRot += arms;

        float legs = CLIMB_LEG_BASE * (1f - unwind) - unwind * 0.55f;
        this.left_leg.xRot += legs;
        this.right_leg.xRot += legs;

        this.head.xRot += 0.35f * (1f - unwind);
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

        this.ALL2.translateAndRotate(poseStack);
        this.ALL.translateAndRotate(poseStack);
        this.right_arm.translateAndRotate(poseStack);

        poseStack.translate(0.03D, 1.0D, 0.0D);
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
