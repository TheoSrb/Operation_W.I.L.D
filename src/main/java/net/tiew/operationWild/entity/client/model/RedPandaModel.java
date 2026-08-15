package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.client.animation.RedPandaAnimations;

public class RedPandaModel<T extends RedPandaEntity> extends OWComboModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "red_panda"), "main");

    private static final float WALK_SPEED = 3.5f;
    private static final float FLEE_WALK_SPEED = 2.1f;
    private static final long STEP_RIGHT_MS = 240L;
    private static final long STEP_LEFT_MS = 920L;
    private static final float PAW_TRAIL = 0.045f;

    private static final double TAIL_REACH = 0.75;
    private static final double TAIL_PROBE_DEPTH = 2.5;
    private static final float TAIL_GRAVITY_GAIN = 42f;
    private static final float TAIL_GRAVITY_DROOP_MAX = 58f;
    private static final float TAIL_GRAVITY_LIFT_MAX = 20f;
    private static final float TAIL_GRAVITY_RESPONSE = 0.22f;

    private static final float TREE_UPRIGHT_PITCH = (float) (Math.PI / 2.0);
    private static final float TREE_CYCLE_RATE = 0.42f;
    private static final float TREE_EFFORT_RESPONSE = 0.30f;
    private static final float TREE_ARM_BASE = -1.35f;
    private static final float TREE_ARM_SWING = 0.55f;
    private static final float TREE_ARM_SPREAD = 0.30f;
    private static final float TREE_LEG_BASE = 1.15f;
    private static final float TREE_LEG_SWING = 0.42f;
    private static final float TREE_LEG_SPREAD = 0.26f;
    private static final float TREE_BODY_LIFT = -2.0f;

    private final ModelPart ALL2;
    private final ModelPart ALL;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart left_Ear;
    private final ModelPart right_Ear;
    private final ModelPart left_Eyeball;
    private final ModelPart right_Eyeball;
    private final ModelPart tong;
    private final ModelPart tail;
    private final ModelPart left_arm;
    private final ModelPart left_leg;
    private final ModelPart right_leg;
    private final ModelPart right_arm;

    public static boolean RENDER_AS_GROUNDED = false;

    private static final double MOUTH_DROP = 0.13;
    private static final double MOUTH_FORWARD = -0.38;
    private static final float MOUTH_ITEM_SCALE = 0.9f;
    private static final float MOUTH_ITEM_SPIN = 45f;
    private static final float GROUND_DISPLAY_LIFT = 2f / 16f;

    private T currentEntity;
    private MultiBufferSource currentBufferSource;

    private float prevLimbSwing = 0f;

    public void setBufferSource(MultiBufferSource bufferSource) {
        this.currentBufferSource = bufferSource;
    }

    public RedPandaModel(ModelPart root) {
        this.ALL2 = root.getChild("ALL2");
        this.ALL = this.ALL2.getChild("ALL");
        this.body = this.ALL.getChild("body");
        this.head = this.body.getChild("head");
        this.left_Ear = this.head.getChild("left_Ear");
        this.right_Ear = this.head.getChild("right_Ear");
        this.left_Eyeball = this.head.getChild("left_Eyeball");
        this.right_Eyeball = this.head.getChild("right_Eyeball");
        this.tong = this.head.getChild("tong");
        this.tail = this.body.getChild("tail");
        this.left_arm = this.ALL.getChild("left_arm");
        this.left_leg = this.ALL.getChild("left_leg");
        this.right_leg = this.ALL.getChild("right_leg");
        this.right_arm = this.ALL.getChild("right_arm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 17.0F, 0.0F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -4.0F, -6.0F, 7.0F, 7.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.5F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(34, 20).addBox(-4.0F, -3.0F, -5.0F, 8.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(38, 7).addBox(-2.0F, 0.0F, -7.0F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.5F, -6.0F));

        PartDefinition left_Ear = head.addOrReplaceChild("left_Ear", CubeListBuilder.create().texOffs(38, 12).mirror().addBox(-1.5F, -1.5F, -0.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(3.5F, -2.5F, -1.5F));

        PartDefinition right_Ear = head.addOrReplaceChild("right_Ear", CubeListBuilder.create().texOffs(38, 12).addBox(-1.5F, -1.5F, -0.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.5F, -2.5F, -1.5F));

        PartDefinition left_Eyeball = head.addOrReplaceChild("left_Eyeball", CubeListBuilder.create().texOffs(0, 30).addBox(-1.0F, -0.5F, -0.025F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.offset(3.0F, -0.5F, -5.0F));

        PartDefinition right_Eyeball = head.addOrReplaceChild("right_Eyeball", CubeListBuilder.create().texOffs(0, 30).mirror().addBox(-1.0F, -0.5F, -0.025F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(-3.0F, -0.5F, -5.0F));

        PartDefinition tong = head.addOrReplaceChild("tong", CubeListBuilder.create().texOffs(59, 44).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 2.0F, -5.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 20).addBox(-3.0F, -2.0F, 0.0F, 6.0F, 5.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 6.0F));

        PartDefinition left_arm = ALL.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(14, 36).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(1.75F, 3.5F, -3.5F));

        PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(34, 31).addBox(-1.5F, -0.5F, -2.0F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.75F, 3.5F, 3.0F));

        PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(34, 31).mirror().addBox(-1.5F, -0.5F, -2.0F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.75F, 3.5F, 3.0F));

        PartDefinition right_arm = ALL.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(14, 36).mirror().addBox(-1.5F, -0.5F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.75F, 3.5F, -3.5F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    protected AnimationDefinition comboAnimation(int index) {
        return null;
    }

    @Override
    public void setupAnim(T redPanda, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.currentEntity = redPanda;

        boolean treePosed = !RENDER_AS_GROUNDED && redPanda.isTreePosed();

        if (RENDER_AS_GROUNDED || treePosed) limbSwingAmount = 0f;

        boolean posed = redPanda.isOnShoulder() || treePosed
                || redPanda.intimidateAnimationState.isStarted()
                || redPanda.isCowering()
                || redPanda.transitionIdleFear.isStarted()
                || redPanda.getFearRecoverTimer() > 0
                || redPanda.playAnimationState.isStarted();
        if (RENDER_AS_GROUNDED || !posed) {
            this.applyHeadRotation(netHeadYaw, headPitch);
        }

        setupBaseAnim(redPanda, limbSwing, limbSwingAmount, ageInTicks, treePosed);

        if (treePosed) animateTreeClimb(redPanda, ageInTicks);
        if (redPanda.swipeAnimationState.isStarted() && !RENDER_AS_GROUNDED) {
            animateHarvestSwipe(redPanda, ageInTicks);
        }

        this.prevLimbSwing = limbSwing;
    }

    private void setupBaseAnim(T redPanda, float limbSwing, float limbSwingAmount, float ageInTicks, boolean treePosed) {
        if (redPanda.isClimbing() && !RENDER_AS_GROUNDED) {
            this.animate(redPanda.idleAnimationState, RedPandaAnimations.MISC_IDLE, ageInTicks, 0.4f);
            animateClimb(redPanda, ageInTicks);
            return;
        }

        if (redPanda.isOnShoulder() && !RENDER_AS_GROUNDED) {
            this.animate(redPanda.shoulderIdleAnimationState, RedPandaAnimations.SHOULDER_IDLE, ageInTicks, 1.0f);
            animatePerchPhysics(redPanda, ageInTicks);
            animateGestures(redPanda, ageInTicks);
            return;
        }

        if (redPanda.intimidateAnimationState.isStarted() && !RENDER_AS_GROUNDED) {
            this.animate(redPanda.intimidateAnimationState, RedPandaAnimations.INTIMIDATION, ageInTicks, 1.0f);
            return;
        }

        if (!RENDER_AS_GROUNDED) {
            if (redPanda.transitionIdleFear.isStarted()) {
                this.animate(redPanda.transitionIdleFear, RedPandaAnimations.TRANSITION_IDLE_FEAR, ageInTicks, 1.0f);
                return;
            }

            if (redPanda.isCowering()) {
                this.animate(redPanda.fearAnimationState, RedPandaAnimations.FEAR, ageInTicks, 1.0f);
                return;
            }

            if (redPanda.transitionFearIdle.isStarted() && !redPanda.isMoving()) {
                this.animate(redPanda.transitionFearIdle, RedPandaAnimations.TRANSITION_FEAR_IDLE, ageInTicks, 1.0f);
                return;
            }

            if (redPanda.playAnimationState.isStarted()) {
                this.animate(redPanda.playAnimationState, RedPandaAnimations.MISC_PLAY, ageInTicks, 1.0f);
                return;
            }
        }

        if (redPanda.transitionIdleSit.isStarted()) {
            this.animate(redPanda.transitionIdleSit, RedPandaAnimations.TRANSITION_IDLE_SIT, ageInTicks, 1.0f);
            return;
        }

        if (redPanda.transitionSitIdle.isStarted()) {
            this.animate(redPanda.transitionSitIdle, RedPandaAnimations.TRANSITION_SIT_IDLE, ageInTicks, 1.0f);
            return;
        }

        if (redPanda.transitionIdleSleep.isStarted()) {
            this.animate(redPanda.transitionIdleSleep, RedPandaAnimations.TRANSITION_IDLE_NAP, ageInTicks, 1.0f);
            return;
        }

        if (redPanda.transitionSleepIdle.isStarted()) {
            this.animate(redPanda.transitionSleepIdle, RedPandaAnimations.TRANSITION_NAP_IDLE, ageInTicks, 1.0f);
            return;
        }

        if (redPanda.isNapping() || redPanda.isSleeping()) {
            this.animate(redPanda.napAnimationState, RedPandaAnimations.NAP, ageInTicks, 1.0f);
            return;
        }

        if (redPanda.isSitting()) {
            this.animate(redPanda.sittingAnimationState, RedPandaAnimations.SIT, ageInTicks, 1.0f);
            animateTailGravity(redPanda, ageInTicks);
            animateGestures(redPanda, ageInTicks);
            return;
        }

        this.animate(redPanda.idleAnimationState, RedPandaAnimations.MISC_IDLE, ageInTicks, 1.0f);

        float walkSpeed = redPanda.isAlerted() ? FLEE_WALK_SPEED : WALK_SPEED;
        this.animateWalk(RedPandaAnimations.MOVE_WALK, limbSwing, limbSwingAmount, walkSpeed, walkSpeed * 1.2f);

        if (!RENDER_AS_GROUNDED && !treePosed) {
            if (walkAnimCrossed(RedPandaAnimations.MOVE_WALK, limbSwing, walkSpeed, STEP_RIGHT_MS))
                redPanda.onRightFootDown();
            if (walkAnimCrossed(RedPandaAnimations.MOVE_WALK, limbSwing, walkSpeed, STEP_LEFT_MS))
                redPanda.onLeftFootDown();
        }

        if (!treePosed) animateTailGravity(redPanda, ageInTicks);
        animateGestures(redPanda, ageInTicks);
    }

    private void animateTailGravity(T redPanda, float ageInTicks) {
        if (RENDER_AS_GROUNDED) return;

        if (redPanda.tailGroundProbeTick != redPanda.tickCount) {
            redPanda.tailGroundProbeTick = redPanda.tickCount;
            redPanda.tailGroundDrop = probeTailDrop(redPanda);
        }

        float dt = Float.isNaN(redPanda.tailGroundLastAge) ? 1f
                : Mth.clamp(ageInTicks - redPanda.tailGroundLastAge, 0f, 2f);
        redPanda.tailGroundLastAge = ageInTicks;

        float target = Mth.clamp(redPanda.tailGroundDrop * TAIL_GRAVITY_GAIN,
                -TAIL_GRAVITY_LIFT_MAX, TAIL_GRAVITY_DROOP_MAX);
        redPanda.tailGroundPitch += (target - redPanda.tailGroundPitch)
                * Math.min(1f, TAIL_GRAVITY_RESPONSE * dt);

        this.tail.xRot += -redPanda.tailGroundPitch * Mth.DEG_TO_RAD;
    }

    private static float probeTailDrop(RedPandaEntity redPanda) {
        float yaw = redPanda.yBodyRot * Mth.DEG_TO_RAD;
        double reach = TAIL_REACH * redPanda.getScale();

        double x = redPanda.getX() + Mth.sin(yaw) * reach;
        double z = redPanda.getZ() - Mth.cos(yaw) * reach;

        Vec3 from = new Vec3(x, redPanda.getY() + 0.3, z);
        Vec3 to = new Vec3(x, redPanda.getY() - TAIL_PROBE_DEPTH, z);

        BlockHitResult hit = redPanda.level().clip(new ClipContext(
                from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, redPanda));

        if (hit.getType() == HitResult.Type.MISS) return (float) TAIL_PROBE_DEPTH;
        return (float) (redPanda.getY() - hit.getLocation().y);
    }

    private void animateGestures(T redPanda, float ageInTicks) {
        if (redPanda.mealAnimationState.isStarted()) animateMeal(redPanda, ageInTicks);
        else if (redPanda.feastAnimationState.isStarted()) animateFeastCast(redPanda, ageInTicks);
        else if (redPanda.throwAnimationState.isStarted()) animateOrbThrow(redPanda, ageInTicks);
    }

    private void animateMeal(T redPanda, float ageInTicks) {
        redPanda.mealAnimationState.updateTime(ageInTicks, 1f);

        float total = RedPandaEntity.MEAL_TICKS;
        float elapsed = redPanda.mealAnimationState.getAccumulatedTime() / 50f;
        float progress = Mth.clamp(elapsed / total, 0f, 1f);

        float stoop = band(progress, 0f, 0.14f) - band(progress, 0.86f, 1f);
        float grab = band(progress, 0.10f, 0.24f) - band(progress, 0.30f, 0.40f);
        float cradle = band(progress, 0.26f, 0.38f) - band(progress, 0.82f, 0.94f);
        float chew = band(progress, 0.38f, 0.46f) - band(progress, 0.74f, 0.84f);
        float lick = band(progress, 0.84f, 0.90f) - band(progress, 0.94f, 1f);

        float nibble = Mth.sin(ageInTicks * 2.4f);
        float rock = Mth.sin(ageInTicks * 0.8f);

        this.ALL.xRot += -0.44f * cradle + 0.26f * stoop;
        this.ALL.y += -1.5f * cradle + 1.1f * stoop;
        this.left_leg.xRot += 0.44f * cradle;
        this.right_leg.xRot += 0.44f * cradle;

        float hold = -1.28f * cradle - 0.38f * grab + 0.11f * nibble * chew;
        this.left_arm.xRot += hold;
        this.right_arm.xRot += hold;
        this.left_arm.zRot += 0.34f * cradle - 0.07f * nibble * chew;
        this.right_arm.zRot += -0.34f * cradle + 0.07f * nibble * chew;
        this.left_arm.z += -2.3f * cradle;
        this.right_arm.z += -2.3f * cradle;
        this.left_arm.y += -1.2f * cradle;
        this.right_arm.y += -1.2f * cradle;

        this.head.xRot += 0.58f * stoop + 0.30f * grab - 0.12f * cradle + 0.13f * nibble * chew;
        this.head.y += -0.9f * cradle;
        this.head.zRot += 0.09f * rock * cradle;

        this.tong.xRot += -0.30f * chew * (0.5f + 0.5f * nibble) - 0.46f * lick;
        this.tong.z += -0.7f * lick;

        this.tail.xRot += -0.36f * cradle;
        this.tail.yRot += 0.24f * rock * cradle;

        this.left_Ear.zRot += 0.20f * cradle + 0.11f * nibble * chew;
        this.right_Ear.zRot += -0.20f * cradle - 0.11f * nibble * chew;

        this.body.yScale *= 1f - 0.07f * chew;
        this.head.yScale *= 1f + 0.05f * chew * nibble;
    }

    private void animateHarvestSwipe(T redPanda, float ageInTicks) {
        redPanda.swipeAnimationState.updateTime(ageInTicks, 1f);

        float total = RedPandaEntity.SWIPE_TICKS;
        float elapsed = redPanda.swipeAnimationState.getAccumulatedTime() / 50f;
        float progress = Mth.clamp(elapsed / total, 0f, 1f);

        float coil = band(progress, 0f, 0.28f) - band(progress, 0.28f, 0.46f);
        float strike = band(progress, 0.30f, 0.46f) - band(progress, 0.58f, 0.86f);
        float snap = band(progress, 0.40f, 0.48f) - band(progress, 0.48f, 0.62f);

        this.right_arm.xRot += 0.55f * coil - 1.30f * strike;
        this.right_arm.zRot += 0.30f * coil - 0.45f * strike;
        this.right_arm.y += -1.4f * strike;

        this.left_arm.xRot += -0.18f * strike;
        this.left_arm.zRot += 0.14f * strike;

        this.head.xRot += -0.30f * coil - 0.16f * strike;
        this.head.zRot += 0.22f * snap;
        this.head.yRot += -0.18f * strike;

        this.body.zRot += -0.10f * strike;
        this.ALL.zRot += 0.07f * snap;

        this.left_Ear.zRot += 0.26f * snap;
        this.right_Ear.zRot += -0.26f * snap;

        this.tail.yRot += 0.24f * strike;
        this.tong.xRot += -0.20f * snap;
    }

    private void animateTreeClimb(T redPanda, float ageInTicks) {
        float partial = Mth.clamp(ageInTicks - redPanda.tickCount, 0f, 1f);
        float lean = Mth.lerp(partial, redPanda.treeLeanO, redPanda.treeLean);
        float flip = Mth.lerp(partial, redPanda.treeFlipO, redPanda.treeFlip);

        this.ALL2.xRot += -lean * TREE_UPRIGHT_PITCH;
        this.ALL2.zRot += flip * Mth.PI;
        this.ALL2.y += TREE_BODY_LIFT * lean;

        float dt = Float.isNaN(redPanda.climbLastAge) ? 0f
                : Mth.clamp(ageInTicks - redPanda.climbLastAge, 0f, 2f);
        redPanda.climbLastAge = ageInTicks;

        float pace = redPanda.getClimbPace();
        redPanda.climbEffort += (Mth.clamp(pace, 0f, 1f) - redPanda.climbEffort)
                * Math.min(1f, TREE_EFFORT_RESPONSE * dt);
        redPanda.climbCycle += pace * TREE_CYCLE_RATE * dt;

        float reach = Mth.sin(redPanda.climbCycle) * redPanda.climbEffort;
        float pull = Mth.cos(redPanda.climbCycle) * redPanda.climbEffort;

        this.left_arm.xRot += lean * (TREE_ARM_BASE + TREE_ARM_SWING * reach);
        this.right_arm.xRot += lean * (TREE_ARM_BASE - TREE_ARM_SWING * reach);
        this.left_arm.zRot += lean * (-TREE_ARM_SPREAD - 0.12f * reach);
        this.right_arm.zRot += lean * (TREE_ARM_SPREAD + 0.12f * reach);

        this.left_leg.xRot += lean * (TREE_LEG_BASE - TREE_LEG_SWING * reach);
        this.right_leg.xRot += lean * (TREE_LEG_BASE + TREE_LEG_SWING * reach);
        this.left_leg.zRot += lean * -TREE_LEG_SPREAD;
        this.right_leg.zRot += lean * TREE_LEG_SPREAD;

        this.body.xRot += lean * 0.09f * pull;
        this.ALL.y += lean * 0.8f * pull;
        this.ALL.zRot += lean * 0.06f * reach;

        this.head.xRot += lean * -0.34f;
        this.head.yRot += lean * 0.16f * reach;

        this.tail.xRot += lean * (-0.28f + 0.14f * pull);
        this.tail.yRot += lean * 0.20f * reach;

        this.left_Ear.xRot += lean * -0.18f;
        this.right_Ear.xRot += lean * -0.18f;
        this.left_Ear.zRot += lean * 0.14f;
        this.right_Ear.zRot += lean * -0.14f;

        this.body.yScale *= 1f - lean * 0.06f * pull;
        this.body.xScale *= 1f + lean * 0.05f * pull;
    }

    private void animateFeastCast(T redPanda, float ageInTicks) {
        redPanda.feastAnimationState.updateTime(ageInTicks, 1f);

        float total = OWAttacksConstants.RedPanda.FEAST_CAST_TICKS;
        float elapsed = redPanda.feastAnimationState.getAccumulatedTime() / 50f;
        float progress = Mth.clamp(elapsed / total, 0f, 1f);

        float rise = band(progress, 0f, 0.16f) - band(progress, 0.82f, 1f);
        float reach = band(progress, 0.10f, 0.30f) - band(progress, 0.44f, 0.56f);
        float heave = band(progress, 0.32f, 0.46f) - band(progress, 0.46f, 0.54f);
        float sweep = band(progress, 0.46f, 0.60f) - band(progress, 0.60f, 0.80f);
        float shake = band(progress, 0.62f, 0.72f) - band(progress, 0.80f, 0.92f);
        float settle = band(progress, 0.86f, 0.93f) - band(progress, 0.93f, 1f);

        float wobble = Mth.sin(ageInTicks * 3.2f);
        float strain = Mth.sin(ageInTicks * 2.6f) * 0.05f * heave;

        this.ALL.xRot += -0.48f * rise + 0.10f * reach + 0.22f * heave - 0.30f * sweep + 0.12f * settle;
        this.ALL.y += -1.4f * rise + 0.9f * heave - 1.3f * sweep + 0.8f * settle;
        this.ALL.zRot += 0.11f * shake * wobble;

        this.left_leg.xRot += 0.48f * rise + 0.30f * sweep;
        this.right_leg.xRot += 0.48f * rise + 0.30f * sweep;
        this.left_leg.zRot += -0.15f * rise;
        this.right_leg.zRot += 0.15f * rise;

        float armLift = 0.95f * reach - 0.35f * heave - 0.95f * sweep - 0.55f * shake
                + 0.22f * shake * wobble + 0.30f * settle;
        this.left_arm.xRot += armLift;
        this.right_arm.xRot += armLift;

        float spread = 1.05f * sweep + 0.75f * shake;
        this.left_arm.zRot += -spread + 0.18f * reach - strain;
        this.right_arm.zRot += spread - 0.18f * reach + strain;

        this.head.xRot += 0.20f * rise + 0.26f * heave - 0.30f * sweep;
        this.head.zRot += -0.14f * shake * wobble;

        this.tail.xRot += -0.45f * rise + 0.20f * heave + 0.65f * sweep - 0.20f * settle;
        this.tail.yRot += 0.16f * shake * wobble;

        this.left_Ear.xRot += -0.15f * rise;
        this.right_Ear.xRot += -0.15f * rise;
        this.left_Ear.zRot += 0.24f * sweep + 0.18f * shake * wobble;
        this.right_Ear.zRot += -0.24f * sweep + 0.18f * shake * wobble;

        this.body.yScale *= 1f - 0.18f * heave + 0.15f * sweep - 0.07f * settle;
        this.body.xScale *= 1f + 0.13f * heave - 0.09f * sweep + 0.05f * settle;
        this.body.zScale *= 1f + 0.13f * heave - 0.09f * sweep + 0.05f * settle;

        this.head.yScale *= 1f - 0.10f * heave + 0.09f * sweep;
        this.head.xScale *= 1f + 0.08f * heave - 0.05f * sweep;
        this.head.zScale *= 1f + 0.08f * heave - 0.05f * sweep;
    }

    private void animateOrbThrow(T redPanda, float ageInTicks) {
        redPanda.throwAnimationState.updateTime(ageInTicks, 1f);

        float total = OWAttacksConstants.RedPanda.HEAL_SNACK_THROW_TICKS;
        float elapsed = redPanda.throwAnimationState.getAccumulatedTime() / 50f;
        float progress = Mth.clamp(elapsed / total, 0f, 1f);
        float trailing = Math.max(0f, progress - PAW_TRAIL);

        float rise = band(progress, 0f, 0.18f) - band(progress, 0.80f, 1f);
        float cradle = band(progress, 0.10f, 0.30f) - band(progress, 0.62f, 0.76f);
        float hold = band(progress, 0.38f, 0.48f) - band(progress, 0.48f, 0.56f);
        float launch = band(progress, 0.46f, 0.64f) - band(progress, 0.64f, 0.86f);
        float watch = band(progress, 0.62f, 0.72f) - band(progress, 0.86f, 1f);
        float settle = band(progress, 0.84f, 0.92f) - band(progress, 0.92f, 1f);
        float launchTrail = band(trailing, 0.48f, 0.62f) - band(trailing, 0.62f, 0.86f);

        float knead = Mth.cos(ageInTicks * 0.9f) * 0.10f * cradle;
        float cup = Mth.sin(ageInTicks * 0.9f) * 0.08f * cradle;
        float tremble = Mth.sin(ageInTicks * 2.6f) * 0.04f * hold;

        this.ALL.xRot += -0.42f * rise + 0.16f * hold - 0.34f * launch + 0.12f * settle;
        this.ALL.y += -1.3f * rise + 0.8f * hold - 1.5f * launch + 0.9f * settle;

        this.left_leg.xRot += 0.42f * rise + 0.34f * launch;
        this.right_leg.xRot += 0.42f * rise + 0.34f * launch;
        this.left_leg.zRot += -0.13f * rise;
        this.right_leg.zRot += 0.13f * rise;

        float reach = -1.10f * cradle + 1.55f * hold - 1.45f * launch + 0.30f * watch + 0.26f * settle;
        float reachTrail = -1.10f * cradle + 1.55f * hold - 1.45f * launchTrail + 0.30f * watch + 0.26f * settle;

        this.left_arm.xRot += reachTrail + knead;
        this.right_arm.xRot += reach - knead;
        this.left_arm.zRot += 0.36f * cradle - 0.60f * launchTrail + cup - tremble;
        this.right_arm.zRot += -0.36f * cradle + 0.60f * launch + cup + tremble;

        float armPush = -1.4f * cradle + 1.9f * hold - 3.0f * launch;
        float armLift = 0.9f * hold - 2.2f * launch;
        this.left_arm.z += armPush;
        this.right_arm.z += armPush;
        this.left_arm.y += armLift;
        this.right_arm.y += armLift;

        this.head.xRot += 0.10f * rise + 0.26f * cradle + 0.10f * hold
                - 0.70f * launch - 0.22f * watch;
        this.head.y += -1.6f * hold - 2.4f * launch;
        this.head.zRot += 0.16f * cradle - 0.16f * launch;

        this.tail.xRot += -0.42f * rise + 0.22f * hold + 0.70f * launch - 0.20f * settle;
        this.tail.yRot += Mth.sin(ageInTicks * 0.7f) * 0.18f * cradle;

        this.left_Ear.xRot += -0.14f * rise - 0.12f * watch
                + Mth.sin(ageInTicks * 0.5f) * 0.12f * cradle;
        this.right_Ear.xRot += -0.14f * rise - 0.12f * watch;
        this.left_Ear.zRot += 0.26f * launch;
        this.right_Ear.zRot += -0.26f * launch;
        this.tong.xRot += -0.22f * cradle - 0.30f * launch;

        this.body.yScale *= 1f - 0.10f * cradle - 0.16f * hold + 0.16f * launch - 0.08f * settle;
        this.body.xScale *= 1f + 0.07f * cradle + 0.12f * hold - 0.10f * launch + 0.06f * settle;
        this.body.zScale *= 1f + 0.07f * cradle + 0.12f * hold - 0.10f * launch + 0.06f * settle;

        this.head.yScale *= 1f - 0.09f * hold + 0.09f * launch;
        this.head.xScale *= 1f + 0.07f * hold - 0.05f * launch;
        this.head.zScale *= 1f + 0.07f * hold - 0.05f * launch;
    }

    private static float band(float value, float from, float to) {
        if (value <= from) return 0f;
        if (value >= to) return 1f;
        float t = (value - from) / (to - from);
        return t * t * (3f - 2f * t);
    }

    private void animateClimb(T redPanda, float ageInTicks) {
        float progress = redPanda.climbProgress();
        float pitch = redPanda.climbPitch() * Mth.DEG_TO_RAD;
        float effort = Mth.sin(progress * Mth.PI);
        float reach = Mth.sin(ageInTicks * 1.35f);

        this.ALL.xRot += -pitch * 0.85f;
        this.ALL.y += -1.4f * effort;
        this.ALL.zRot += 0.22f * effort;

        this.left_arm.xRot += -1.25f + 0.85f * reach * effort;
        this.right_arm.xRot += -1.25f - 0.85f * reach * effort;
        this.left_leg.xRot += -0.60f - 0.60f * reach * effort;
        this.right_leg.xRot += -0.60f + 0.60f * reach * effort;

        this.left_arm.zRot += -0.30f * effort;
        this.right_arm.zRot += 0.30f * effort;

        this.tail.xRot += pitch * 0.55f - 0.30f * effort;
        this.tail.yRot += 0.28f * reach * effort;

        this.head.xRot += pitch * 0.60f;
        this.left_Ear.zRot += 0.22f * effort;
        this.right_Ear.zRot += -0.22f * effort;

        float gather = band(progress, 0f, 0.13f) - band(progress, 0.13f, 0.32f);
        float arrive = band(progress, 0.80f, 0.93f) - band(progress, 0.93f, 1f);
        float squash = gather + arrive;

        this.body.yScale *= 1f - 0.21f * squash + 0.14f * effort;
        this.body.xScale *= 1f + 0.15f * squash - 0.07f * effort;
        this.body.zScale *= 1f + 0.15f * squash - 0.07f * effort;

        this.head.yScale *= 1f - 0.10f * squash + 0.07f * effort;
        this.head.xScale *= 1f + 0.08f * squash - 0.04f * effort;
        this.head.zScale *= 1f + 0.08f * squash - 0.04f * effort;

        this.ALL.y += 1.5f * squash;
    }

    private void animatePerchPhysics(T redPanda, float ageInTicks) {
        float dt = Float.isNaN(redPanda.tailLastAge) ? 0f
                : Mth.clamp(ageInTicks - redPanda.tailLastAge, 0f, 2f);
        redPanda.tailLastAge = ageInTicks;

        net.minecraft.world.entity.player.Player carrier = redPanda.getCarrier();

        float partial = Mth.clamp(ageInTicks - redPanda.tickCount, 0f, 1f);
        float carrierYaw = Mth.rotLerp(partial, redPanda.yBodyRotO, redPanda.yBodyRot);
        float yawDelta = Float.isNaN(redPanda.perchLastCarrierYaw)
                ? 0f : Mth.wrapDegrees(carrierYaw - redPanda.perchLastCarrierYaw);
        redPanda.perchLastCarrierYaw = carrierYaw;

        float instantRate = dt > 1.0e-4f ? yawDelta / dt : 0f;
        redPanda.perchTurnRate += (instantRate - redPanda.perchTurnRate)
                * Math.min(1f, dt * PERCH_TURN_SMOOTHING);

        float turnRate = redPanda.perchTurnRate;
        float swingTarget = Mth.clamp(-turnRate * TAIL_SWING_GAIN, -TAIL_SWING_MAX, TAIL_SWING_MAX);

        float normalized = Mth.clamp(Math.abs(turnRate) / PERCH_ROLL_REFERENCE, 0f, 1f);
        float shaped = (float) Math.pow(normalized, PERCH_ROLL_SHARPNESS);
        float rollTarget = -Math.signum(turnRate) * shaped * PERCH_ROLL_MAX;

        float rollResponse = Math.abs(rollTarget) > Math.abs(redPanda.perchRoll)
                ? PERCH_ROLL_RISE : PERCH_ROLL_FALL;
        redPanda.perchRoll += (rollTarget - redPanda.perchRoll) * Math.min(1f, rollResponse * dt);

        redPanda.tailSwingVelocity += (swingTarget - redPanda.tailSwing) * TAIL_STIFFNESS * dt;
        redPanda.tailSwingVelocity *= (float) Math.pow(TAIL_DAMPING, dt);
        redPanda.tailSwing += redPanda.tailSwingVelocity * dt;

        boolean airborne = carrier != null && !carrier.onGround();
        double verticalSpeed = carrier != null ? carrier.getDeltaMovement().y : 0;
        float pitchTarget = airborne
                ? Mth.clamp((float) Math.abs(verticalSpeed) * PERCH_PITCH_GAIN, 0f, PERCH_PITCH_MAX)
                : 0f;

        redPanda.perchPitchVelocity += (pitchTarget - redPanda.perchPitch) * PERCH_PITCH_STIFFNESS * dt;
        redPanda.perchPitchVelocity *= (float) Math.pow(PERCH_PITCH_DAMPING, dt);
        redPanda.perchPitch += redPanda.perchPitchVelocity * dt;

        if (redPanda.perchWasAirborne && !airborne) {
            float impact = Mth.clamp(Math.abs(redPanda.perchLastVerticalSpeed) / PERCH_IMPACT_REFERENCE, 0f, 1f);
            redPanda.perchSquashVelocity += PERCH_SQUASH_IMPULSE * impact;
        }

        if (!redPanda.perchWasAirborne && airborne && verticalSpeed > PERCH_LEAP_SPEED) {
            redPanda.perchSquashVelocity -= PERCH_LEAP_IMPULSE;
        }

        redPanda.perchWasAirborne = airborne;
        if (airborne) redPanda.perchLastVerticalSpeed = (float) verticalSpeed;

        redPanda.perchSquashVelocity += (0f - redPanda.perchLandSquash) * PERCH_SQUASH_STIFFNESS * dt;
        redPanda.perchSquashVelocity *= (float) Math.pow(PERCH_SQUASH_DAMPING, dt);
        redPanda.perchLandSquash += redPanda.perchSquashVelocity * dt;

        redPanda.perchLandSquash = Mth.clamp(redPanda.perchLandSquash, PERCH_STRETCH_LIMIT, 1f);

        double pace = carrier != null ? carrier.getDeltaMovement().horizontalDistance() : 0;
        float paceTarget = Mth.clamp((float) pace / TAIL_RUN_REFERENCE, 0f, 1f) * TAIL_RUN_LIFT;
        redPanda.perchRunLift += (paceTarget - redPanda.perchRunLift) * Math.min(1f, TAIL_RUN_RESPONSE * dt);

        float pitchRad = redPanda.perchPitch * Mth.DEG_TO_RAD;
        float squash = redPanda.perchLandSquash;

        this.ALL.xRot += -pitchRad;
        this.ALL.zRot += redPanda.perchRoll * Mth.DEG_TO_RAD;
        this.ALL.y += 2.8f * squash;

        float air = Mth.clamp(redPanda.perchPitch / PERCH_PITCH_MAX, 0f, 1f);

        this.body.yScale *= 1f - 0.34f * squash + 0.20f * air;
        this.body.xScale *= 1f + 0.23f * squash - 0.10f * air;
        this.body.zScale *= 1f + 0.23f * squash - 0.10f * air;

        this.head.yScale *= 1f - 0.11f * squash + 0.08f * air;
        this.head.xScale *= 1f + 0.08f * squash - 0.04f * air;
        this.head.zScale *= 1f + 0.08f * squash - 0.04f * air;

        float swingRad = redPanda.tailSwing * Mth.DEG_TO_RAD;
        float swingArc = Math.abs(redPanda.tailSwing) * TAIL_SWING_ARC * Mth.DEG_TO_RAD;

        this.tail.xRot += redPanda.perchRunLift * Mth.DEG_TO_RAD + swingArc
                - pitchRad * 0.75f + 0.48f * squash;
        this.tail.yRot += swingRad;
        this.tail.zRot += swingRad * TAIL_SWING_TWIST;

        float earFlap = -pitchRad * 0.55f + 0.44f * squash;
        this.left_Ear.xRot += earFlap;
        this.right_Ear.xRot += earFlap;

        this.head.xRot += pitchRad * 0.45f;
    }

    private static final float PERCH_TURN_SMOOTHING = 0.55f;

    private static final float PERCH_ROLL_REFERENCE = 11f;
    private static final float PERCH_ROLL_SHARPNESS = 0.7f;
    private static final float PERCH_ROLL_MAX = 19f;
    private static final float PERCH_ROLL_RISE = 0.35f;
    private static final float PERCH_ROLL_FALL = 0.14f;

    private static final float TAIL_SWING_GAIN = 6f;
    private static final float TAIL_SWING_MAX = 58f;
    private static final float TAIL_STIFFNESS = 0.26f;
    private static final float TAIL_DAMPING = 0.86f;

    private static final float TAIL_SWING_ARC = 0.45f;
    private static final float TAIL_SWING_TWIST = 0.15f;

    private static final float TAIL_RUN_REFERENCE = 0.22f;
    private static final float TAIL_RUN_LIFT = 46f;
    private static final float TAIL_RUN_RESPONSE = 0.12f;

    private static final float PERCH_PITCH_GAIN = 55f;
    private static final float PERCH_PITCH_MAX = 32f;
    private static final float PERCH_PITCH_STIFFNESS = 0.30f;
    private static final float PERCH_PITCH_DAMPING = 0.80f;
    private static final float PERCH_SQUASH_IMPULSE = 0.68f;
    private static final float PERCH_SQUASH_STIFFNESS = 0.45f;
    private static final float PERCH_SQUASH_DAMPING = 0.68f;
    private static final float PERCH_IMPACT_REFERENCE = 0.7f;
    private static final float PERCH_LEAP_IMPULSE = 0.34f;
    private static final float PERCH_STRETCH_LIMIT = -0.4f;
    private static final double PERCH_LEAP_SPEED = 0.08;

    private boolean walkAnimCrossed(AnimationDefinition animation, float limbSwing, float speedScale, long triggerTimeMs) {
        long durationMs = (long) (animation.lengthInSeconds() * 1000f);
        if (durationMs <= 0) return false;

        long cur = ((long) (limbSwing * 50f * speedScale)) % durationMs;
        long prev = ((long) (prevLimbSwing * 50f * speedScale)) % durationMs;

        if (prev <= cur) return prev < triggerTimeMs && cur >= triggerTimeMs;
        return triggerTimeMs <= cur || triggerTimeMs > prev;
    }

    private void applyHeadRotation(float netHeadYaw, float headPitch) {
        netHeadYaw = Mth.clamp(netHeadYaw, -35.0F, 35.0F);
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

        if (RENDER_AS_GROUNDED || currentEntity == null || currentBufferSource == null) return;
        if (!currentEntity.hasMouthItem()) return;

        renderMouthItem(currentEntity, poseStack, packedLight);
    }

    private void renderMouthItem(T redPanda, PoseStack poseStack, int packedLight) {
        poseStack.pushPose();

        this.ALL2.translateAndRotate(poseStack);
        this.ALL.translateAndRotate(poseStack);
        this.body.translateAndRotate(poseStack);
        this.head.translateAndRotate(poseStack);

        poseStack.translate(0.0, MOUTH_DROP, MOUTH_FORWARD);
        poseStack.mulPose(Axis.ZP.rotationDegrees(90f));
        poseStack.mulPose(Axis.YP.rotationDegrees(90f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(MOUTH_ITEM_SPIN));
        poseStack.scale(MOUTH_ITEM_SCALE, MOUTH_ITEM_SCALE, MOUTH_ITEM_SCALE);
        poseStack.translate(0.0, -GROUND_DISPLAY_LIFT, 0.0);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                redPanda.getMouthItem(),
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                currentBufferSource,
                redPanda.level(),
                0);

        poseStack.popPose();
    }

    @Override
    public ModelPart root() {
        return this.ALL2;
    }

    public void copyPoseFrom(RedPandaModel<?> src) {
        this.ALL2.copyFrom(src.ALL2);
        this.ALL.copyFrom(src.ALL);
        this.body.copyFrom(src.body);
        this.head.copyFrom(src.head);
        this.left_Ear.copyFrom(src.left_Ear);
        this.right_Ear.copyFrom(src.right_Ear);
        this.left_Eyeball.copyFrom(src.left_Eyeball);
        this.right_Eyeball.copyFrom(src.right_Eyeball);
        this.tong.copyFrom(src.tong);
        this.tail.copyFrom(src.tail);
        this.left_arm.copyFrom(src.left_arm);
        this.left_leg.copyFrom(src.left_leg);
        this.right_leg.copyFrom(src.right_leg);
        this.right_arm.copyFrom(src.right_arm);
    }
}
