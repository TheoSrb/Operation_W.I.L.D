package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.ChameleonEntity;
import net.tiew.operationWild.entity.client.animation.ChameleonAnimations;
import net.tiew.operationWild.entity.client.animation.OWSedationAnimationSet;
import net.tiew.operationWild.entity.client.animation.OWSedationAnimator;

public class ChameleonModel<T extends ChameleonEntity> extends OWComboModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "chameleon"), "main");

    private static final float WALK_SPEED = 4.0f;
    private static final float WALK_AMOUNT_GAIN = 12f;
    private static final long STEP_RIGHT_MS = 0L;
    private static final long STEP_LEFT_MS = 1000L;
    private static final float TONGUE_HOLD_FADE_MS = 200f;
    private static final float PERCH_TRANSITION_SPEED = 2.08f;
    private static final org.joml.Vector3f FROZEN_VECTOR = new org.joml.Vector3f();

    private static final OWSedationAnimationSet SEDATION = new OWSedationAnimationSet(
            ChameleonAnimations.SEDATED_FALL, ChameleonAnimations.SEDATED_REST, ChameleonAnimations.SEDATED_WAKE);

    public static boolean RENDER_AS_GROUNDED = false;

    private final ModelPart ALL2;
    private final ModelPart ALL;
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart head;
    private final ModelPart left_eyeBall;
    private final ModelPart right_eyeBall;
    private final ModelPart tongue;
    private final ModelPart tongue_tip;
    private final ModelPart left_arm;
    private final ModelPart right_arm;
    private final ModelPart left_leg;
    private final ModelPart right_leg;

    private float prevLimbSwing = 0f;

    public ChameleonModel(ModelPart root) {
        this.ALL2 = root.getChild("ALL2");
        this.ALL = this.ALL2.getChild("ALL");
        this.body = this.ALL.getChild("body");
        this.tail = this.body.getChild("tail");
        this.head = this.body.getChild("head");
        this.left_eyeBall = this.head.getChild("left_eyeBall");
        this.right_eyeBall = this.head.getChild("right_eyeBall");
        this.tongue = this.head.getChild("tongue");
        this.tongue_tip = this.tongue.getChild("tongue_tip");
        this.left_arm = this.ALL.getChild("left_arm");
        this.right_arm = this.ALL.getChild("right_arm");
        this.left_leg = this.ALL.getChild("left_leg");
        this.right_leg = this.ALL.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 19.0F, 0.0F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -3.0F, -5.0F, 5.0F, 6.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 26).addBox(0.0F, -5.0F, -4.0F, 0.0F, 2.0F, 9.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(30, 0).addBox(-1.0F, -2.5F, 0.0F, 2.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.5F, 5.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 16).addBox(-2.5F, -3.0F, -5.0F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(20, 16).addBox(-1.5F, -7.0F, -4.0F, 3.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(48, 0).addBox(2.0F, -2.9F, -4.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(48, 0).mirror().addBox(-4.0F, -2.9F, -4.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(36, 16).addBox(0.0F, 2.0F, -5.0F, 0.0F, 2.0F, 4.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 0.0F, -5.0F));

        head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(56, 6).addBox(0.0F, -0.5F, -1.5F, 0.0F, 1.0F, 3.0F, new CubeDeformation(0.025F)), PartPose.offset(4.0F, -2.4F, -2.5F));

        head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(56, 6).mirror().addBox(0.0F, -0.5F, -1.5F, 0.0F, 1.0F, 3.0F, new CubeDeformation(0.025F)).mirror(false), PartPose.offset(-4.0F, -2.4F, -2.5F));

        PartDefinition tongue = head.addOrReplaceChild("tongue", CubeListBuilder.create().texOffs(48, 14).addBox(-0.5F, -0.5F, 0.5F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.5F, -5.0F));

        tongue.addOrReplaceChild("tongue_tip", CubeListBuilder.create().texOffs(48, 6).addBox(-1.0F, -1.0F, 0.1F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        ALL.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(20, 26).addBox(0.0F, 0.0F, -2.0F, 8.0F, 0.0F, 4.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(2.0F, 2.0F, -2.0F, 0.0F, 0.0F, 0.4363F));

        ALL.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(20, 26).mirror().addBox(-8.0F, 0.0F, -2.0F, 8.0F, 0.0F, 4.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-2.0F, 2.0F, -2.0F, 0.0F, 0.0F, -0.4363F));

        ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(20, 26).addBox(0.0F, 0.0F, -2.0F, 8.0F, 0.0F, 4.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(2.0F, 2.0F, 3.0F, 0.0F, 0.0F, 0.4363F));

        ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(20, 26).mirror().addBox(-8.0F, 0.0F, -2.0F, 8.0F, 0.0F, 4.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-2.0F, 2.0F, 3.0F, 0.0F, 0.0F, -0.4363F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    protected AnimationDefinition comboAnimation(int index) {
        return null;
    }

    @Override
    public void setupAnim(T chameleon, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        float sedation = OWSedationAnimator.weight(chameleon, SEDATION, ageInTicks);
        boolean carried = !RENDER_AS_GROUNDED && chameleon.isCarried();

        if (!RENDER_AS_GROUNDED && chameleon.isMimicryFrozen()) {
            AnimationDefinition still = !carried ? ChameleonAnimations.MISC_IDLE
                    : chameleon.getPerch() == ChameleonEntity.PERCH_HEAD ? ChameleonAnimations.HEAD_IDLE : ChameleonAnimations.BACK_IDLE;
            KeyframeAnimations.animate(this, still, 0L, 1.0f, FROZEN_VECTOR);
            this.prevLimbSwing = limbSwing;
            return;
        }

        if (sedation < 1f) {
            if (RENDER_AS_GROUNDED || carried) limbSwingAmount = 0f;
            if (!carried && !chameleon.isNapping()) this.applyHeadRotation(netHeadYaw, headPitch);

            if (carried) setupCarriedAnim(chameleon, ageInTicks);
            else setupGroundAnim(chameleon, limbSwing, limbSwingAmount, ageInTicks);

            if (!RENDER_AS_GROUNDED) {
                setupActionAnims(chameleon, ageInTicks);
                applyAim(chameleon, ageInTicks);
            }
        }

        OWSedationAnimator.apply(this, chameleon, SEDATION, ageInTicks);
        this.head.xRot += chameleon.getSedatedHeadNod(ageInTicks) * sedation;

        this.prevLimbSwing = limbSwing;
    }

    private void setupCarriedAnim(T chameleon, float ageInTicks) {
        if (chameleon.transitionBackHeadAnimationState.isStarted()) {
            this.animate(chameleon.transitionBackHeadAnimationState, ChameleonAnimations.TRANSITION_BACK_HEAD, ageInTicks, PERCH_TRANSITION_SPEED);
            return;
        }
        if (chameleon.transitionHeadBackAnimationState.isStarted()) {
            this.animate(chameleon.transitionHeadBackAnimationState, ChameleonAnimations.TRANSITION_HEAD_BACK, ageInTicks, PERCH_TRANSITION_SPEED);
            return;
        }
        if (chameleon.headIdleAnimationState.isStarted()) {
            this.animate(chameleon.headIdleAnimationState, ChameleonAnimations.HEAD_IDLE, ageInTicks, 1.0f);
            return;
        }
        this.animate(chameleon.backIdleAnimationState, ChameleonAnimations.BACK_IDLE, ageInTicks, 1.0f);
    }

    private void setupGroundAnim(T chameleon, float limbSwing, float limbSwingAmount, float ageInTicks) {
        if (chameleon.climbOnAnimationState.isStarted() && !RENDER_AS_GROUNDED) {
            this.animate(chameleon.climbOnAnimationState, ChameleonAnimations.CLIMB_ON, ageInTicks, 1.0f);
            return;
        }

        if (chameleon.jumpOffAnimationState.isStarted() && !RENDER_AS_GROUNDED) {
            this.animate(chameleon.jumpOffAnimationState, ChameleonAnimations.JUMP_OFF, ageInTicks, 1.0f);
            return;
        }

        if (chameleon.transitionIdleSleep.isStarted()) {
            this.animate(chameleon.transitionIdleSleep, ChameleonAnimations.TRANSITION_IDLE_NAP, ageInTicks, 1.0f);
            return;
        }

        if (chameleon.transitionSleepIdle.isStarted()) {
            this.animate(chameleon.transitionSleepIdle, ChameleonAnimations.TRANSITION_NAP_IDLE, ageInTicks, 1.0f);
            return;
        }

        if (chameleon.isNapping() && !RENDER_AS_GROUNDED) {
            this.animate(chameleon.restAnimationState, ChameleonAnimations.NAP, ageInTicks, 1.0f);
            return;
        }

        if (chameleon.transitionIdleSit.isStarted()) {
            this.animate(chameleon.transitionIdleSit, ChameleonAnimations.TRANSITION_IDLE_SIT, ageInTicks, 1.0f);
            return;
        }

        if (chameleon.transitionSitIdle.isStarted()) {
            this.animate(chameleon.transitionSitIdle, ChameleonAnimations.TRANSITION_SIT_IDLE, ageInTicks, 1.0f);
            return;
        }

        if (chameleon.isSitting()) {
            this.animate(chameleon.sittingAnimationState, ChameleonAnimations.SIT, ageInTicks, 1.0f);
            return;
        }

        float swim = RENDER_AS_GROUNDED ? 0f : swimWeight(chameleon, ageInTicks);
        float land = 1f - swim;
        this.animateWeighted(chameleon.idleAnimationState, ChameleonAnimations.MISC_IDLE, ageInTicks, 1.0f, land);
        this.animateWeighted(chameleon.miscIdleAnimationState, ChameleonAnimations.MISC_IDLE_2, ageInTicks, 1.0f, land);
        this.animateWalkWeighted(ChameleonAnimations.MOVE_WALK, limbSwing, limbSwingAmount, WALK_SPEED, WALK_AMOUNT_GAIN, land);
        animateSwim(chameleon, ChameleonAnimations.SWIM, ageInTicks, swim);

        if (!RENDER_AS_GROUNDED && land > 0.5f) {
            if (walkAnimCrossed(ChameleonAnimations.MOVE_WALK, limbSwing, WALK_SPEED, STEP_RIGHT_MS)) chameleon.onRightFootDown();
            if (walkAnimCrossed(ChameleonAnimations.MOVE_WALK, limbSwing, WALK_SPEED, STEP_LEFT_MS)) chameleon.onLeftFootDown();
        }
    }

    private void setupActionAnims(T chameleon, float ageInTicks) {
        if (chameleon.tongueHoldAnimationState.isStarted()) {
            float weight = Mth.clamp(chameleon.tongueHoldAnimationState.getAccumulatedTime() / TONGUE_HOLD_FADE_MS, 0f, 1f);
            this.animateWeighted(chameleon.tongueHoldAnimationState, ChameleonAnimations.TONGUE_HOLD, ageInTicks, 1.0f,
                    weight * weight * (3f - 2f * weight));
        }
        this.animate(chameleon.tongueShotAnimationState, ChameleonAnimations.TONGUE_SHOT, ageInTicks, 1.0f);
        this.animate(chameleon.tongueCatchAnimationState, ChameleonAnimations.TONGUE_CATCH, ageInTicks, 1.0f);
        this.animate(chameleon.camouflageCastAnimationState, ChameleonAnimations.CAMOUFLAGE_CAST, ageInTicks, 1.0f);
    }

    private void applyAim(T chameleon, float ageInTicks) {
        float partialTicks = Mth.clamp(ageInTicks - chameleon.tickCount, 0f, 1f);
        float[] share = chameleon.aimShare(partialTicks);
        if (share == null) return;

        if (!chameleon.isCarried()) {
            this.ALL.yRot += share[0];
            this.ALL.xRot += share[1];
        }
        this.head.yRot += share[2];
        this.head.xRot += share[3];
    }

    private boolean walkAnimCrossed(AnimationDefinition animation, float limbSwing, float speedScale, long triggerTimeMs) {
        long durationMs = (long) (animation.lengthInSeconds() * 1000f);
        if (durationMs <= 0) return false;

        long cur = ((long) (limbSwing * 50f * speedScale)) % durationMs;
        long prev = ((long) (prevLimbSwing * 50f * speedScale)) % durationMs;

        if (prev <= cur) return prev < triggerTimeMs && cur >= triggerTimeMs;
        return triggerTimeMs <= cur || triggerTimeMs > prev;
    }

    private void applyHeadRotation(float netHeadYaw, float headPitch) {
        netHeadYaw = Mth.clamp(netHeadYaw, -40.0F, 40.0F);
        headPitch = Mth.clamp(headPitch, -30.0F, 30.0F);

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
    }

    public void renderGeometryOnly(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        renderGeometryOnly(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root() {
        return this.ALL2;
    }
}
