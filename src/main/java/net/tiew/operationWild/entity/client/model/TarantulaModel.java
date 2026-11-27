package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.TarantulaEntity;
import net.tiew.operationWild.entity.client.animation.OWSedationAnimationSet;
import net.tiew.operationWild.entity.client.animation.OWSedationAnimator;
import net.tiew.operationWild.entity.client.animation.TarantulaAnimations;

public class TarantulaModel<T extends TarantulaEntity> extends OWComboModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "tarantula"), "main");

    private static final float WALK_SPEED = 6.0f;
    private static final float WALK_AMOUNT_GAIN = 8f;
    private static final float RUN_THRESHOLD = 0.45f;
    private static final float RUN_BLEND_RANGE = 0.3f;
    private static final long STEP_FIRST_MS = 0L;
    private static final long STEP_SECOND_MS = 500L;
    private static final float WEB_SHOT_SPEED = 3.5f;
    private static final float SCUTTLE_TIME_SCALE = 0.4f;
    private static final float SPINNERET_Z = 7.5f / 16f;

    private static final OWSedationAnimationSet SEDATION = new OWSedationAnimationSet(
            TarantulaAnimations.SEDATED_FALL, TarantulaAnimations.SEDATED_REST, TarantulaAnimations.SEDATED_WAKE);

    public static boolean RENDER_AS_GROUNDED = false;
    public static boolean RENDER_ON_ARM = false;
    public static boolean RENDER_IN_HAND_VIEW = false;
    public static float ARM_SCUTTLE = 0f;

    private final ModelPart ALL2;
    private final ModelPart ALL;
    private final ModelPart body;
    private final ModelPart abdomen;
    private final ModelPart head;

    private float prevLimbSwing = 0f;
    private T currentEntity;

    public TarantulaModel(ModelPart root) {
        this.ALL2 = root.getChild("ALL2");
        this.ALL = this.ALL2.getChild("ALL");
        this.body = this.ALL.getChild("body");
        this.abdomen = this.body.getChild("abdomen");
        this.head = this.body.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, -3.0F, -1.0F));

        body.addOrReplaceChild("abdomen", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -3.0F, 0.0F, 8.0F, 6.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(32, 8).addBox(-2.0F, 0.0F, 7.0F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.0F, -1.0F, 1.0F, 0.3491F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 14).addBox(-3.0F, -2.0F, -5.0F, 6.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.5F, -0.0873F, 0.0F, 0.0F));

        head.addOrReplaceChild("left_chelicerae", CubeListBuilder.create().texOffs(24, 14).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 0.5F, -5.0F, -0.1949F, 0.2012F, -0.0197F));

        head.addOrReplaceChild("right_chelicerae", CubeListBuilder.create().texOffs(24, 14).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.5F, 0.5F, -5.0F, -0.1949F, -0.2012F, 0.0197F));

        ALL.addOrReplaceChild("left_leg_0", CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -4.0F, -1.0F, -0.3448F, -0.5618F, -0.107F));

        ALL.addOrReplaceChild("right_leg_0", CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.0F, -4.0F, -1.0F, -0.3448F, 0.5618F, 0.107F));

        ALL.addOrReplaceChild("left_leg_1", CubeListBuilder.create().texOffs(40, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.3F, -4.0F, 0.5F, 0.0F, 0.0F, -0.3491F));

        ALL.addOrReplaceChild("right_leg_1", CubeListBuilder.create().texOffs(40, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-4.3F, -4.0F, 0.5F, 0.0F, 0.0F, 0.3491F));

        ALL.addOrReplaceChild("left_leg_2", CubeListBuilder.create().texOffs(48, 0).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -4.0F, 2.5F, 0.0873F, -0.1745F, -0.3491F));

        ALL.addOrReplaceChild("right_leg_2", CubeListBuilder.create().texOffs(48, 0).mirror().addBox(-1.0F, 0.0F, -0.5F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-4.5F, -4.0F, 2.5F, 0.0873F, 0.1745F, 0.3491F));

        ALL.addOrReplaceChild("left_leg_3", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.3F, -4.0F, 4.5F, 0.411F, -0.5946F, -0.5445F));

        ALL.addOrReplaceChild("right_leg_3", CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-1.0F, 0.0F, 0.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-4.3F, -4.0F, 4.5F, 0.411F, 0.5946F, 0.5445F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    protected AnimationDefinition comboAnimation(int index) {
        return null;
    }

    @Override
    public void setupAnim(T tarantula, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.currentEntity = tarantula;

        float sedation = OWSedationAnimator.weight(tarantula, SEDATION, ageInTicks);

        if (sedation < 1f) {
            if (RENDER_ON_ARM) {
                setupArmAnim(tarantula, ageInTicks);
            } else {
                if (RENDER_AS_GROUNDED) limbSwingAmount = 0f;
                if (!tarantula.isNapping()) {
                    float freedom = RENDER_AS_GROUNDED ? 1f : tarantula.wallLookFreedom(Mth.clamp(ageInTicks - tarantula.tickCount, 0f, 1f));
                    this.applyHeadRotation(netHeadYaw * freedom, headPitch * freedom);
                }
                setupGroundAnim(tarantula, limbSwing, limbSwingAmount, ageInTicks);
            }
        }

        OWSedationAnimator.apply(this, tarantula, SEDATION, ageInTicks);
        this.head.xRot += tarantula.getSedatedHeadNod(ageInTicks) * sedation;

        this.prevLimbSwing = limbSwing;
    }

    private void setupArmAnim(T tarantula, float ageInTicks) {
        float partialTicks = Mth.clamp(ageInTicks - tarantula.tickCount, 0f, 1f);
        float hold = Mth.lerp(partialTicks, tarantula.webHoldWeightO, tarantula.webHoldWeight);
        float eased = hold * hold * (3f - 2f * hold);

        if (tarantula.climbOnAnimationState.isStarted()) {
            this.animate(tarantula.climbOnAnimationState, TarantulaAnimations.ARM_CLIMB_ON, ageInTicks, 1.0f);
            return;
        }

        this.animateWeighted(tarantula.armIdleAnimationState, TarantulaAnimations.ARM_IDLE, ageInTicks, 1.0f, 1f - eased);
        this.animateWeighted(tarantula.webHoldAnimationState, TarantulaAnimations.ARM_WEB_HOLD, ageInTicks, 1.0f, eased);
        this.animateWeighted(tarantula.webShotAnimationState, TarantulaAnimations.ARM_WEB_SHOT, ageInTicks, WEB_SHOT_SPEED, 1f - eased);
        this.animate(tarantula.refuseAnimationState, TarantulaAnimations.ARM_REFUSE, ageInTicks, 1.0f);
        this.animate(tarantula.eatAnimationState, TarantulaAnimations.ARM_EAT, ageInTicks, 1.0f);
        this.animate(tarantula.selectAnimationState, TarantulaAnimations.ARM_SELECT, ageInTicks, 1.0f);
        if (ARM_SCUTTLE > 0.01f) {
            this.animateWalkWeighted(TarantulaAnimations.MOVE_RUN, ageInTicks * SCUTTLE_TIME_SCALE, 1f, WALK_SPEED, WALK_AMOUNT_GAIN, ARM_SCUTTLE);
        }
    }

    private void setupGroundAnim(T tarantula, float limbSwing, float limbSwingAmount, float ageInTicks) {
        if (!RENDER_AS_GROUNDED) {
            if (tarantula.climbOnAnimationState.isStarted()) {
                this.animate(tarantula.climbOnAnimationState, TarantulaAnimations.ARM_CLIMB_ON, ageInTicks, 1.0f);
                return;
            }
            if (tarantula.jumpOffAnimationState.isStarted()) {
                this.animate(tarantula.jumpOffAnimationState, TarantulaAnimations.ARM_JUMP_OFF, ageInTicks, 1.0f);
                return;
            }
        }

        if (tarantula.transitionIdleSleep.isStarted()) {
            this.animate(tarantula.transitionIdleSleep, TarantulaAnimations.TRANSITION_IDLE_NAP, ageInTicks, 1.0f);
            return;
        }

        if (tarantula.transitionSleepIdle.isStarted()) {
            this.animate(tarantula.transitionSleepIdle, TarantulaAnimations.TRANSITION_NAP_IDLE, ageInTicks, 1.0f);
            return;
        }

        if (tarantula.isNapping() && !RENDER_AS_GROUNDED) {
            this.animate(tarantula.restAnimationState, TarantulaAnimations.NAP, ageInTicks, 1.0f);
            return;
        }

        if (tarantula.transitionIdleSit.isStarted()) {
            this.animate(tarantula.transitionIdleSit, TarantulaAnimations.TRANSITION_IDLE_SIT, ageInTicks, 1.0f);
            return;
        }

        if (tarantula.transitionSitIdle.isStarted()) {
            this.animate(tarantula.transitionSitIdle, TarantulaAnimations.TRANSITION_SIT_IDLE, ageInTicks, 1.0f);
            return;
        }

        if (tarantula.isSitting()) {
            this.animate(tarantula.sittingAnimationState, TarantulaAnimations.SIT, ageInTicks, 1.0f);
            return;
        }

        float swim = RENDER_AS_GROUNDED ? 0f : swimWeight(tarantula, ageInTicks);
        float land = 1f - swim;
        float run = Mth.clamp((limbSwingAmount - RUN_THRESHOLD) / RUN_BLEND_RANGE, 0f, 1f);

        this.animateWeighted(tarantula.idleAnimationState, TarantulaAnimations.MISC_IDLE, ageInTicks, 1.0f, land);
        this.animateWeighted(tarantula.miscIdleAnimationState, TarantulaAnimations.MISC_IDLE_2, ageInTicks, 1.0f, land);
        this.animateWeighted(tarantula.threatAnimationState, TarantulaAnimations.MISC_THREAT, ageInTicks, 1.0f, land);
        this.animateWeighted(tarantula.hairFlickAnimationState, TarantulaAnimations.MISC_HAIR_FLICK, ageInTicks, 1.0f, land);
        this.animateWalkWeighted(TarantulaAnimations.MOVE_WALK, limbSwing, limbSwingAmount, WALK_SPEED, WALK_AMOUNT_GAIN, land * (1f - run));
        this.animateWalkWeighted(TarantulaAnimations.MOVE_RUN, limbSwing, limbSwingAmount, WALK_SPEED, WALK_AMOUNT_GAIN, land * run);
        animateSwim(tarantula, TarantulaAnimations.SWIM, ageInTicks, swim);

        if (!RENDER_AS_GROUNDED && land > 0.5f) {
            AnimationDefinition gait = run > 0.5f ? TarantulaAnimations.MOVE_RUN : TarantulaAnimations.MOVE_WALK;
            long half = (long) (gait.lengthInSeconds() * 500f);
            if (walkAnimCrossed(gait, limbSwing, WALK_SPEED, STEP_FIRST_MS)) tarantula.onFirstTetrapodDown();
            if (walkAnimCrossed(gait, limbSwing, WALK_SPEED, run > 0.5f ? half : STEP_SECOND_MS)) tarantula.onSecondTetrapodDown();
        }
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
        netHeadYaw = Mth.clamp(netHeadYaw, -30.0F, 30.0F);
        headPitch = Mth.clamp(headPitch, -20.0F, 20.0F);

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        captureSpinneret(poseStack);
    }

    private void captureSpinneret(PoseStack poseStack) {
        if (RENDER_AS_GROUNDED || RENDER_IN_HAND_VIEW || currentEntity == null || !currentEntity.isWebOut()) return;
        poseStack.pushPose();
        this.ALL2.translateAndRotate(poseStack);
        this.ALL.translateAndRotate(poseStack);
        this.body.translateAndRotate(poseStack);
        this.abdomen.translateAndRotate(poseStack);
        poseStack.translate(0f, 0f, SPINNERET_Z);
        org.joml.Vector3f spinneret = poseStack.last().pose().transformPosition(new org.joml.Vector3f());
        poseStack.popPose();
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        currentEntity.renderedSpinneret = camera.add(spinneret.x, spinneret.y, spinneret.z);
        currentEntity.renderedSpinneretFrame = System.nanoTime();
    }

    @Override
    public ModelPart root() {
        return this.ALL2;
    }
}
