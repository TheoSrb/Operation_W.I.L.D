package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.aquatic.HippopotamusEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.client.animation.HippopotamusAnimations;
import org.joml.Vector3f;

public class HippopotamusModel<T extends HippopotamusEntity> extends OWComboModel<T> implements OWFlagModel {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "hippopotamus_default"), "main");

    private static final ResourceLocation FLAG_POLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/elephant_flag.png");

    private static final Anchor FLAG_ANCHOR = new Anchor(-5.75f, 11f, 0.5f, 19f);

    private static final float REST_POSE_Y_SUM = 5.0f;

    private static final float MOVE_BLEND_FACTOR = 4.0f;
    private static final float WALK_ANIM_SPEED = 3.4f;
    private static final float RUN_ANIM_SPEED = 1.0f;
    private static final long WALK_STEP_RIGHT_MS = 830L;
    private static final long WALK_STEP_LEFT_MS = 1830L;
    private static final long RUN_STEP_RIGHT_MS = 290L;
    private static final long RUN_STEP_LEFT_MS = 40L;

    private static final float COMBO_ANIMATION_SPEED = 1.0f;

    private static final long SEDATED_COLLAPSE_MS = (long) (HippopotamusAnimations.SEDATED_COLLAPSE.lengthInSeconds() * 1000f);
    private static final float FURY_POSE_FADE_START = 18f;
    private static final float FURY_POSE_FADE_TICKS = 7f;
    private static final float FURY_ROAR_RELEASE_TICKS = 6f;
    private static final float RUN_ANIM_AMPLITUDE = 1.35f;
    private static final float ROLL_AXIS_HEIGHT = 17f;
    private static final float ROLL_HALF_WIDTH = 10f;
    private static final float ROLL_HALF_HEIGHT = 9.5f;
    private static final float QUARTER_TURN = (float) (Math.PI / 2.0);

    private static final Vector3f ANIM_VECTOR = new Vector3f();

    public float externalRiderPitch = 0f;
    public float externalBankRoll = 0f;

    private float prevLimbSwing = 0f;

    private final ModelPart ALL2;
    private final ModelPart ALL;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart mouth_Up;
    private final ModelPart mouth_Down;
    private final ModelPart left_Ear;
    private final ModelPart right_Ear;
    private final ModelPart right_eyeBall;
    private final ModelPart left_eyeBall;
    private final ModelPart left_Arm;
    private final ModelPart right_Arm;
    private final ModelPart right_Leg;
    private final ModelPart left_Leg;
    private final ModelPart mainFlag;
    private final ModelPart flag;

    public HippopotamusModel(ModelPart root) {
        this.ALL2 = root.getChild("ALL2");
        this.ALL = this.ALL2.getChild("ALL");
        this.body = this.ALL.getChild("body");
        this.head = this.body.getChild("head");
        this.mouth_Up = this.head.getChild("mouth_Up");
        this.mouth_Down = this.head.getChild("mouth_Down");
        this.left_Ear = this.head.getChild("left_Ear");
        this.right_Ear = this.head.getChild("right_Ear");
        this.right_eyeBall = this.head.getChild("right_eyeBall");
        this.left_eyeBall = this.head.getChild("left_eyeBall");
        this.left_Arm = this.ALL.getChild("left_Arm");
        this.right_Arm = this.ALL.getChild("right_Arm");
        this.right_Leg = this.ALL.getChild("right_Leg");
        this.left_Leg = this.ALL.getChild("left_Leg");

        this.mainFlag = this.body.hasChild("mainFlag") ? this.body.getChild("mainFlag") : null;
        this.flag = this.mainFlag != null ? this.mainFlag.getChild("flag") : null;
        if (this.mainFlag != null) this.mainFlag.visible = false;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 7.0F, 2.0F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, -8.0F, -14.0F, 20.0F, 19.0F, 30.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(5, 158).addBox(-6.5F, -6.0F, -7.0F, 13.0F, 11.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(52, 79).addBox(1.5F, -8.0F, -7.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(52, 79).mirror().addBox(-5.5F, -8.0F, -7.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, -15.0F));

        head.addOrReplaceChild("mouth_Up", CubeListBuilder.create().texOffs(8, 57).addBox(-6.5F, -4.0F, -13.0F, 13.0F, 6.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, -6.0F));

        head.addOrReplaceChild("mouth_Down", CubeListBuilder.create().texOffs(74, 57).addBox(-6.5F, -2.0F, -13.0F, 13.0F, 5.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(103, 83).addBox(-5.5F, -6.0F, -12.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(103, 83).mirror().addBox(2.5F, -6.0F, -12.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 2.0F, -6.0F));

        head.addOrReplaceChild("left_Ear", CubeListBuilder.create().texOffs(52, 83).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, -6.0F, -0.5F));

        head.addOrReplaceChild("right_Ear", CubeListBuilder.create().texOffs(78, 89).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, -6.0F, -0.5F));

        head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(0, 93).mirror().addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.025F)).mirror(false), PartPose.offset(-3.5F, -7.5F, -7.0F));

        head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(0, 93).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.025F)), PartPose.offset(3.5F, -7.5F, -7.0F));

        ALL.addOrReplaceChild("left_Arm", CubeListBuilder.create().texOffs(0, 75).mirror().addBox(-3.0F, 0.0F, -3.0F, 6.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(5.0F, 9.0F, -10.0F));

        ALL.addOrReplaceChild("right_Arm", CubeListBuilder.create().texOffs(0, 75).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 9.0F, -10.0F));

        ALL.addOrReplaceChild("right_Leg", CubeListBuilder.create().texOffs(0, 75).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-7.0F, 9.0F, 10.0F));

        ALL.addOrReplaceChild("left_Leg", CubeListBuilder.create().texOffs(0, 75).mirror().addBox(-3.0F, 0.0F, -3.0F, 6.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(7.0F, 9.0F, 10.0F));

        addFlagParts(body);

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    public static void addFlagParts(PartDefinition body) {
        PartDefinition mainFlag = body.addOrReplaceChild("mainFlag", CubeListBuilder.create().texOffs(15, 1).addBox(-0.5F, -18.75F, -0.5F, 1.0F, 20.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-0.5F, -18.75F, 0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-0.5F, -6.75F, 0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(7, 15).addBox(-1.0F, -3.725F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.1F))
                .texOffs(7, 15).addBox(-1.0F, 0.275F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.1F))
                .texOffs(0, 8).addBox(-1.0F, -2.725F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.1F))
                .texOffs(0, 0).addBox(-1.0F, -20.75F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, -9.3F, 9.0F));

        mainFlag.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(0, 98).addBox(0.0F, -5.75F, 0.5F, 0.0F, 11.0F, 19.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, -12.0F, 0.0F));
    }

    @Override
    public boolean hasTribeFlag() {
        return this.mainFlag != null && this.flag != null;
    }

    @Override
    public void renderTribeFlagPole(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
        if (!hasTribeFlag()) return;
        poseStack.pushPose();
        this.ALL2.translateAndRotate(poseStack);
        this.ALL.translateAndRotate(poseStack);
        this.body.translateAndRotate(poseStack);
        this.mainFlag.visible = true;
        this.mainFlag.render(poseStack, buffer, packedLight, packedOverlay, 0xFFFFFFFF);
        this.mainFlag.visible = false;
        poseStack.popPose();
    }

    @Override
    public void translateToTribeFlag(PoseStack poseStack) {
        if (!hasTribeFlag()) return;
        this.ALL2.translateAndRotate(poseStack);
        this.ALL.translateAndRotate(poseStack);
        this.body.translateAndRotate(poseStack);
        this.mainFlag.translateAndRotate(poseStack);
        this.flag.translateAndRotate(poseStack);
    }

    @Override
    public ResourceLocation tribeFlagPoleTexture() {
        return FLAG_POLE_TEXTURE;
    }

    @Override
    public Anchor tribeFlagAnchor() {
        return FLAG_ANCHOR;
    }

    @Override
    protected AnimationDefinition comboAnimation(int index) {
        return switch (index) {
            case 1 -> HippopotamusAnimations.ATTACK_STRIKE;
            case 2 -> HippopotamusAnimations.ATTACK_STRIKE_2;
            case 3 -> HippopotamusAnimations.ATTACK_STRIKE_3;
            default -> null;
        };
    }

    @Override
    protected float comboSpeed(int index) {
        return switch (index) {
            case 1 -> 0.9f;
            case 2 -> 0.99f;
            case 3 -> 1.125f;
            default -> 1.0f;
        };
    }

    @Override
    public void setupAnim(T hippo, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float prevSwing = this.prevLimbSwing;
        this.prevLimbSwing = limbSwing;
        this.root().getAllParts().forEach(ModelPart::resetPose);

        float pt = partialTick(hippo, ageInTicks);
        float rollWeight = HippopotamusEntity.smoothStep(hippo.rollBlend(pt));

        if (Math.abs(externalRiderPitch) > 0.01f) this.ALL2.xRot = (float) Math.toRadians(externalRiderPitch);
        applyLean(hippo, pt, rollWeight);

        if (hippo.isBaby()) {
            float maturation = (float) hippo.getMaturationPercentage() / 100f;
            float headScale = 1.5f - (1.5f - 1.0f) * maturation;
            this.head.xScale *= headScale;
            this.head.yScale *= headScale;
            this.head.zScale *= headScale;
        }

        if (hippo.sedatedCollapseAnimationState.isStarted()) {
            hippo.sedatedCollapseAnimationState.updateTime(ageInTicks, 1.0f);
            long collapseTime = hippo.sedatedCollapseAnimationState.getAccumulatedTime();
            if (collapseTime < SEDATED_COLLAPSE_MS) {
                KeyframeAnimations.animate(this, HippopotamusAnimations.SEDATED_COLLAPSE, collapseTime, 1.0f, ANIM_VECTOR);
                captureBodyState(hippo);
                return;
            }
        }

        if (hippo.transitionIdleSleep.isStarted()) {
            this.animate(hippo.transitionIdleSleep, HippopotamusAnimations.TRANSITION_IDLE_NAP, ageInTicks, 1.0f);
            captureBodyState(hippo);
            return;
        }

        if (hippo.transitionSleepIdle.isStarted()) {
            this.animate(hippo.transitionSleepIdle, HippopotamusAnimations.TRANSITION_NAP_IDLE, ageInTicks, 1.0f);
            captureBodyState(hippo);
            return;
        }

        if (hippo.isNapping() || hippo.isSleeping()) {
            this.animate(hippo.restAnimationState, HippopotamusAnimations.NAP, ageInTicks, 1.0f);
            this.head.xRot += hippo.getSedatedHeadNod(ageInTicks);
            captureBodyState(hippo);
            return;
        }

        float roarWeight = roarWeight(hippo, pt);

        if (hippo.isMad()) {
            this.left_eyeBall.xScale = 0;
            this.left_eyeBall.yScale = 0;
            this.left_eyeBall.zScale = 0;

            this.right_eyeBall.xScale = 0;
            this.right_eyeBall.yScale = 0;
            this.right_eyeBall.zScale = 0;
        }

        this.applyHeadRotation(netHeadYaw, headPitch, (1f - rollWeight) * (1f - roarWeight));

        if (hippo.transitionIdleSit.isStarted()) {
            this.animate(hippo.transitionIdleSit, HippopotamusAnimations.TRANSITION_IDLE_SIT, ageInTicks, 1.0f);
            captureBodyState(hippo);
            return;
        }

        if (hippo.transitionSitIdle.isStarted()) {
            this.animate(hippo.transitionSitIdle, HippopotamusAnimations.TRANSITION_SIT_IDLE, ageInTicks, 1.0f);
            captureBodyState(hippo);
            return;
        }

        if (hippo.isSitting()) {
            this.animate(hippo.sittingAnimationState, HippopotamusAnimations.SIT, ageInTicks, 1.0f);
            captureBodyState(hippo);
            return;
        }

        float stanceWeight = 1f - rollWeight;
        animateStance(hippo, limbSwing, limbSwingAmount, ageInTicks, prevSwing, pt, stanceWeight);

        if (hippo.miscIdleAnimationState.isStarted() && stanceWeight > 0.999f) {
            this.animate(hippo.miscIdleAnimationState, HippopotamusAnimations.MISC_IDLE_2, ageInTicks, 1.0f);
        }

        animateCombos(hippo, ageInTicks, COMBO_ANIMATION_SPEED);

        if (hippo.furyRoarAnimationState.isStarted()) {
            this.animate(hippo.furyRoarAnimationState, HippopotamusAnimations.FURY_ROAR, ageInTicks, 1.0f);
        }

        float furyPoseWeight = furyPoseWeight(hippo, pt) * stanceWeight;
        if (furyPoseWeight > 0.001f) {
            KeyframeAnimations.animate(this, HippopotamusAnimations.FURY_POSE,
                    (long) (ageInTicks * 50.0f), furyPoseWeight, ANIM_VECTOR);
        }

        float dizzyWeight = HippopotamusEntity.smoothStep(hippo.dizzyBlend(pt)) * stanceWeight;
        if (dizzyWeight > 0.001f) {
            KeyframeAnimations.animate(this, HippopotamusAnimations.DIZZY,
                    (long) (ageInTicks * 50.0f), dizzyWeight, ANIM_VECTOR);
        }

        captureBodyState(hippo);

        applyRoll(hippo, ageInTicks, pt, rollWeight);
    }

    private void animateStance(T hippo, float limbSwing, float limbSwingAmount, float ageInTicks, float prevSwing,
                               float pt, float stanceWeight) {
        if (stanceWeight <= 0.001f) return;

        float swimWeight = HippopotamusEntity.smoothStep(hippo.swimBlend(pt));
        float landWeight = stanceWeight * (1f - swimWeight);
        float moveWeight = Math.min(limbSwingAmount * MOVE_BLEND_FACTOR, 1.0f);

        if (swimWeight > 0.001f) {
            KeyframeAnimations.animate(this, HippopotamusAnimations.SWIM,
                    (long) (ageInTicks * 50.0f), swimWeight * stanceWeight, ANIM_VECTOR);
        }

        if (landWeight <= 0.001f) return;

        if (moveWeight < 1.0f && !hippo.isCombo()) {
            KeyframeAnimations.animate(this, HippopotamusAnimations.MISC_IDLE,
                    (long) (ageInTicks * 50.0f), (1.0f - moveWeight) * landWeight, ANIM_VECTOR);
        }

        if (moveWeight <= 0f) return;

        float run = HippopotamusEntity.smoothStep(hippo.runBlend(pt));
        float walkPart = moveWeight * (1f - run) * landWeight;
        float runPart = moveWeight * run * landWeight;

        if (walkPart > 0.001f) {
            KeyframeAnimations.animate(this, HippopotamusAnimations.MOVE_WALK,
                    (long) (limbSwing * 50.0f * WALK_ANIM_SPEED), walkPart, ANIM_VECTOR);
        }
        if (runPart > 0.001f) {
            KeyframeAnimations.animate(this, HippopotamusAnimations.MOVE_RUN,
                    (long) (limbSwing * 50.0f * RUN_ANIM_SPEED), runPart * RUN_ANIM_AMPLITUDE, ANIM_VECTOR);
        }

        boolean running = run >= 0.5f;
        AnimationDefinition move = running ? HippopotamusAnimations.MOVE_RUN : HippopotamusAnimations.MOVE_WALK;
        float speed = running ? RUN_ANIM_SPEED : WALK_ANIM_SPEED;
        long rightMark = running ? RUN_STEP_RIGHT_MS : WALK_STEP_RIGHT_MS;
        long leftMark = running ? RUN_STEP_LEFT_MS : WALK_STEP_LEFT_MS;
        if (walkAnimCrossed(move, limbSwing, prevSwing, speed, rightMark)) hippo.onRightFootDown();
        if (walkAnimCrossed(move, limbSwing, prevSwing, speed, leftMark)) hippo.onLeftFootDown();
    }

    private void applyRoll(T hippo, float ageInTicks, float pt, float rollWeight) {
        if (rollWeight <= 0.001f) return;

        KeyframeAnimations.animate(this, HippopotamusAnimations.ROLL_TUCK,
                (long) (ageInTicks * 50.0f), rollWeight, ANIM_VECTOR);

        float angle = hippo.rollAngle(pt);
        float extent = Math.abs(ROLL_HALF_WIDTH * Mth.sin(angle)) + Math.abs(ROLL_HALF_HEIGHT * Mth.cos(angle));

        this.ALL2.yRot += QUARTER_TURN * rollWeight;
        this.ALL.y += (ROLL_AXIS_HEIGHT - extent) * rollWeight;
        this.ALL.zRot += angle;
    }

    private void applyLean(T hippo, float pt, float rollWeight) {
        if (Math.abs(externalBankRoll) <= 0.01f) return;

        float wholeBody = Math.max(HippopotamusEntity.smoothStep(hippo.swimBlend(pt)), rollWeight);
        float bank = (float) Math.toRadians(externalBankRoll);
        this.ALL2.zRot += bank * wholeBody;
        this.body.zRot += bank * (1f - wholeBody);
    }

    private static float roarWeight(HippopotamusEntity hippo, float pt) {
        int elapsed = hippo.furyElapsed();
        if (elapsed < 0) return 0f;
        float time = elapsed + pt;
        float windup = OWAttacksConstants.Hippopotamus.FURY_WINDUP_TICKS;
        if (time < windup) return 1f;
        return 1f - HippopotamusEntity.smoothStep((time - windup) / FURY_ROAR_RELEASE_TICKS);
    }

    private static float furyPoseWeight(HippopotamusEntity hippo, float pt) {
        float blend = HippopotamusEntity.smoothStep(hippo.furyBlend(pt));
        if (blend <= 0f) return 0f;
        if (!hippo.isFuryWindup()) return blend;
        float time = hippo.furyElapsed() + pt;
        return blend * HippopotamusEntity.smoothStep((time - FURY_POSE_FADE_START) / FURY_POSE_FADE_TICKS);
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

    private static float partialTick(HippopotamusEntity hippo, float ageInTicks) {
        return Mth.clamp(ageInTicks - hippo.tickCount, 0f, 1f);
    }

    private void applyHeadRotation(float netHeadYaw, float headPitch, float weight) {
        netHeadYaw = Mth.clamp(netHeadYaw, -25.0F, 25.0F);
        headPitch = Mth.clamp(headPitch, -20.0F, 20.0F);

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F) * weight;
        this.head.xRot = headPitch * ((float) Math.PI / 180F) * weight;
    }

    private void captureBodyState(T hippo) {
        if (!hippo.level().isClientSide()) return;

        float ySum = this.ALL2.y + this.ALL.y + this.body.y;
        hippo.bodyAnimY = ySum - REST_POSE_Y_SUM;

        hippo.setBodyZRot((float) Math.toDegrees(this.ALL2.zRot + this.ALL.zRot + this.body.zRot));
        hippo.setBodyXRot((float) -Math.toDegrees(this.ALL2.xRot + this.ALL.xRot + this.body.xRot));
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root() {
        return this.ALL2;
    }

    public void copyPoseFrom(HippopotamusModel<?> src) {
        this.ALL2.copyFrom(src.ALL2);
        this.ALL.copyFrom(src.ALL);
        this.body.copyFrom(src.body);
        this.head.copyFrom(src.head);
        this.mouth_Up.copyFrom(src.mouth_Up);
        this.mouth_Down.copyFrom(src.mouth_Down);
        this.left_Ear.copyFrom(src.left_Ear);
        this.right_Ear.copyFrom(src.right_Ear);
        this.right_eyeBall.copyFrom(src.right_eyeBall);
        this.left_eyeBall.copyFrom(src.left_eyeBall);
        this.left_Arm.copyFrom(src.left_Arm);
        this.right_Arm.copyFrom(src.right_Arm);
        this.right_Leg.copyFrom(src.right_Leg);
        this.left_Leg.copyFrom(src.left_Leg);
    }
}
