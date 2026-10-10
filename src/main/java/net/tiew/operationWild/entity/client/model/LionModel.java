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
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.client.animation.LionAnimations;
import org.joml.Vector3f;

public class LionModel<T extends LionEntity> extends OWComboModel<T> implements OWFlagModel {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "lion_default"), "main");

    private static final ResourceLocation FLAG_POLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_flag.png");

    private static final Anchor FLAG_ANCHOR = new Anchor(-5.75f, 11f, 0.5f, 19f);

    private static final float REST_POSE_Y_SUM = 14f;
    private static final float COMBO_ANIMATION_SPEED = 1.0f;
    private static final float WALK_ANIM_SPEED = 4.5f;
    private static final float RUN_ANIM_SPEED = 1.1f;
    private static final float WALK_WEIGHT_FACTOR = 4.5f;
    private static final float RUN_WEIGHT_FACTOR = 1.25f;
    private static final float STEP_WEIGHT_MIN = 0.3f;
    private static final float LEG_LENGTH = 12f;
    private static final float MANE_BRISTLE = 0.14f;
    private static final Vector3f ANIM_VECTOR = new Vector3f();

    private float prevLimbSwing = 0f;

    private final ModelPart ALL2;
    private final ModelPart ALL;
    private final ModelPart body;
    private final ModelPart tail_1;
    private final ModelPart tail_2;
    private final ModelPart head;
    private final ModelPart left_ear;
    private final ModelPart right_ear;
    private final ModelPart left_eyeBall;
    private final ModelPart right_eyeBall;
    private final ModelPart mane;
    private final ModelPart mainFlag;
    private final ModelPart flag;
    private final ModelPart left_arm;
    private final ModelPart left_leg;
    private final ModelPart right_arm;
    private final ModelPart right_leg;

    public LionModel(ModelPart root) {
        this.ALL2 = root.getChild("ALL2");
        this.ALL = this.ALL2.getChild("ALL");
        this.body = this.ALL.getChild("body");
        this.tail_1 = this.body.getChild("tail_1");
        this.tail_2 = this.tail_1.getChild("tail_2");
        this.head = this.body.getChild("head");
        this.left_ear = this.head.getChild("left_ear");
        this.right_ear = this.head.getChild("right_ear");
        this.left_eyeBall = this.head.getChild("left_eyeBall");
        this.right_eyeBall = this.head.getChild("right_eyeBall");
        this.mane = this.head.getChild("mane");
        this.mainFlag = this.body.hasChild("mainFlag") ? this.body.getChild("mainFlag") : null;
        this.flag = this.mainFlag != null ? this.mainFlag.getChild("flag") : null;
        this.left_arm = this.ALL.getChild("left_arm");
        this.left_leg = this.ALL.getChild("left_leg");
        this.right_arm = this.ALL.getChild("right_arm");
        this.right_leg = this.ALL.getChild("right_leg");

        if (this.mainFlag != null) this.mainFlag.visible = false;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 9.0F, -1.0F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -10.0F, -8.0F, 10.0F, 10.0F, 20.0F, new CubeDeformation(0.25F))
                .texOffs(0, 81).addBox(-7.0F, -10.0F, -8.0F, 10.0F, 10.0F, 20.0F, new CubeDeformation(0.3F))
                .texOffs(0, 115).addBox(-6.5F, -12.0F, -2.0F, 9.0F, 2.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(86, 35).addBox(3.0F, 0.25F, -8.0F, 0.0F, 2.0F, 20.0F, new CubeDeformation(0.01F))
                .texOffs(86, 35).mirror().addBox(-7.0F, 0.25F, -8.0F, 0.0F, 2.0F, 20.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(2.0F, 5.0F, -2.0F));

        body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(100, 71).mirror().addBox(-6.5F, -5.5F, -0.5F, 12.0F, 11.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.75F, -6.5F, 6.5F, -1.5708F, -0.0835F, -1.5706F));

        body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(100, 71).addBox(-5.5F, -5.5F, -0.5F, 12.0F, 11.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.75F, -6.5F, 6.5F, -1.5708F, 0.0835F, 1.5706F));

        PartDefinition tail_1 = body.addOrReplaceChild("tail_1", CubeListBuilder.create().texOffs(16, 47).addBox(-1.5F, -2.0F, 0.0F, 3.0F, 3.0F, 8.0F, new CubeDeformation(-0.25F)), PartPose.offset(-2.0F, -8.0F, 12.0F));

        tail_1.addOrReplaceChild("tail_2", CubeListBuilder.create().texOffs(38, 47).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 3.0F, 8.0F, new CubeDeformation(-0.25F))
                .texOffs(52, 66).addBox(-2.0F, -2.5F, 7.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(-0.25F))
                .texOffs(71, 57).addBox(-2.0F, -2.3F, 8.5F, 4.0F, 0.0F, 2.0F, new CubeDeformation(0.01F))
                .texOffs(71, 57).mirror().addBox(-2.0F, 1.3F, 8.5F, 4.0F, 0.0F, 2.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(72, 59).addBox(1.8F, -2.5F, 8.5F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.01F))
                .texOffs(78, 59).mirror().addBox(-1.8F, -2.5F, 8.5F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(0.0F, 0.0F, 8.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 31).addBox(-6.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(30, 58).addBox(-4.5F, -1.0F, -13.0F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(108, 126).addBox(-6.5F, 3.0F, -12.0F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(60, 0).addBox(-4.5F, 4.0F, -13.0F, 5.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, -8.0F));

        head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(75, 75).mirror().addBox(0.0F, -8.5F, -0.5F, 0.0F, 9.0F, 21.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-6.5F, 3.5F, -11.5F, 0.1002F, -0.1995F, 0.2146F));

        head.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(75, 75).addBox(0.0F, -8.5F, -0.5F, 0.0F, 9.0F, 21.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(2.5F, 3.5F, -11.5F, 0.1002F, 0.1995F, -0.2146F));

        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(60, 26).addBox(-0.5F, -2.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -3.0F, -4.5F));

        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(60, 26).mirror().addBox(-2.5F, -2.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-5.0F, -3.0F, -4.5F));

        head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(0, 17).addBox(-2.0F, -0.5F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.025F)), PartPose.offset(-4.0F, -2.5F, -8.0F));

        head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(0, 17).mirror().addBox(-1.0F, -0.5F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.025F)).mirror(false), PartPose.offset(0.0F, -2.5F, -8.0F));

        head.addOrReplaceChild("mane", CubeListBuilder.create().texOffs(79, 19).addBox(-9.5F, -7.5F, -4.0F, 15.0F, 15.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(48, 14).addBox(-9.5F, 7.5F, -4.0F, 15.0F, 2.0F, 0.0F, new CubeDeformation(0.01F))
                .texOffs(48, 14).addBox(-9.5F, 7.5F, 4.0F, 15.0F, 2.0F, 0.0F, new CubeDeformation(0.01F))
                .texOffs(86, 1).addBox(5.5F, 7.5F, -4.0F, 0.0F, 2.0F, 8.0F, new CubeDeformation(0.01F))
                .texOffs(86, 1).mirror().addBox(-9.5F, 7.5F, -4.0F, 0.0F, 2.0F, 8.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        addFlagParts(body);

        ALL.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(48, 30).addBox(-1.025F, -1.0F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 3.0F, -6.0F));

        ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(48, 30).addBox(-1.975F, -1.0F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(3.475F, 3.0F, 9.0F));

        ALL.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(48, 30).mirror().addBox(-3.0F, -1.0F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.975F, 3.0F, -6.0F));

        ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(48, 30).mirror().addBox(-2.025F, -1.0F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.475F, 3.0F, 9.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public static void addFlagParts(PartDefinition body) {
        PartDefinition mainFlag = body.addOrReplaceChild("mainFlag", CubeListBuilder.create().texOffs(15, 1).addBox(-0.5F, -18.75F, -0.5F, 1.0F, 20.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-0.5F, -18.75F, 0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-0.5F, -6.75F, 0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(7, 15).addBox(-1.0F, -3.725F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.1F))
                .texOffs(7, 15).addBox(-1.0F, 0.275F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.1F))
                .texOffs(0, 8).addBox(-1.0F, -2.725F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.1F))
                .texOffs(0, 0).addBox(-1.0F, -20.75F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, -7.0F, 5.5F));

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
            case 1 -> LionAnimations.ATTACK_STRIKE;
            case 2 -> LionAnimations.ATTACK_STRIKE_2;
            case 3 -> LionAnimations.ATTACK_STRIKE_3;
            default -> null;
        };
    }

    @Override
    protected float comboSpeed(int index) {
        return switch (index) {
            case 1 -> 0.925f;
            case 2 -> 1.05f;
            case 3 -> 1.15f;
            default -> 1.0f;
        };
    }

    @Override
    public void setupAnim(T lion, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.mane.visible = lion.isMale();

        float pt = Mth.clamp(ageInTicks - lion.tickCount, 0f, 1f);
        long time = (long) (ageInTicks * 50f);

        if (lion.isBaby()) {
            float maturationPercent = (float) lion.getMaturationPercentage() / 100f;
            float headScale = 1.6f - (1.6f - 1.0f) * maturationPercent;

            this.head.xScale *= headScale;
            this.head.yScale *= headScale;
            this.head.zScale *= headScale;
        }

        float rest = smoothStep(lion.restBlend(pt));
        float sit = smoothStep(lion.sitBlend(pt)) * (1f - rest);
        float roar = smoothStep(lion.roarBlend(pt)) * (1f - rest);
        float stance = (1f - rest) * (1f - sit) * (1f - roar);
        float call = callEnvelope(lion, ageInTicks);

        float look = (1f - rest) * (1f - roar) * (1f - 0.4f * sit) * (1f - call);
        this.applyHeadRotation(netHeadYaw * look, headPitch * look);

        float seated = rest + sit;
        if (seated > 0.001f) {
            KeyframeAnimations.animate(this, LionAnimations.SIT, time, seated, ANIM_VECTOR);
            this.head.xRot += lion.getSedatedHeadNod(ageInTicks) * rest;
            if (lion.isKnockedOut()) {
                this.head.xRot += 0.3f * rest;
                this.left_ear.xRot += 0.4f * rest;
                this.right_ear.xRot += 0.4f * rest;
            }
        }

        if (stance > 0.001f) {
            KeyframeAnimations.animate(this, LionAnimations.MISC_IDLE, time, stance, ANIM_VECTOR);
            animateLocomotion(lion, limbSwing, limbSwingAmount, pt, stance);
        }

        if (roar > 0.001f && lion.roarAnimationState.isStarted()) {
            lion.roarAnimationState.updateTime(ageInTicks, 1f);
            KeyframeAnimations.animate(this, LionAnimations.ROAR, lion.roarAnimationState.getAccumulatedTime(), roar, ANIM_VECTOR);
            applyRoarFlourish(lion, ageInTicks, roar);
        }

        animateCombos(lion, ageInTicks, COMBO_ANIMATION_SPEED);

        applyCallGesture(lion);
        applyArrival(lion, ageInTicks);
        applyRespond(lion, ageInTicks);
        applyHuntingPosture(lion, ageInTicks, pt, stance);
        applyEarFlick(lion, pt, stance);

        if (lion.isMad() || lion.isEmpowered() || lion.isRallied()) hideEyeBalls();

        this.prevLimbSwing = limbSwing;

        captureBodyState(lion);
    }

    private void animateLocomotion(T lion, float limbSwing, float limbSwingAmount, float pt, float stance) {
        float run = smoothStep(lion.runBlend(pt));
        float walkWeight = Math.min(limbSwingAmount * WALK_WEIGHT_FACTOR, 1f) * (1f - run) * stance;
        float runWeight = Math.min(limbSwingAmount * RUN_WEIGHT_FACTOR, 1f) * run * stance;

        if (walkWeight > 0.001f) {
            KeyframeAnimations.animate(this, LionAnimations.MOVE_WALK, (long) (limbSwing * 50f * WALK_ANIM_SPEED), walkWeight, ANIM_VECTOR);
        }
        if (runWeight > 0.001f) {
            KeyframeAnimations.animate(this, LionAnimations.MOVE_RUN, (long) (limbSwing * 50f * RUN_ANIM_SPEED), runWeight, ANIM_VECTOR);
        }

        if (run >= 0.5f && runWeight > STEP_WEIGHT_MIN) {
            if (walkAnimCrossed(LionAnimations.MOVE_RUN, limbSwing, RUN_ANIM_SPEED, 180L)) lion.onRightFootDown();
            if (walkAnimCrossed(LionAnimations.MOVE_RUN, limbSwing, RUN_ANIM_SPEED, 570L)) lion.onLeftFootDown();
        } else if (run < 0.5f && walkWeight > STEP_WEIGHT_MIN) {
            if (walkAnimCrossed(LionAnimations.MOVE_WALK, limbSwing, WALK_ANIM_SPEED, 230L)) lion.onRightFootDown();
            if (walkAnimCrossed(LionAnimations.MOVE_WALK, limbSwing, WALK_ANIM_SPEED, 1030L)) lion.onLeftFootDown();
        }
    }

    private void applyRoarFlourish(T lion, float ageInTicks, float weight) {
        float t = lion.roarAnimationState.getAccumulatedTime() / 50f;
        float bristle = window(t, 8f, 12f, 70f, 86f) * weight;
        if (bristle <= 0.001f) return;

        bristleMane(bristle * (MANE_BRISTLE + Mth.sin(ageInTicks * 1.9f) * 0.012f));
        this.left_ear.xRot -= 0.55f * bristle;
        this.right_ear.xRot -= 0.55f * bristle;
        this.tail_1.xRot -= 0.35f * bristle;
        this.tail_2.yRot += Mth.sin(ageInTicks * 0.8f) * 0.4f * bristle;
    }

    private float callEnvelope(T lion, float ageInTicks) {
        if (!lion.callAnimationState.isStarted()) return 0f;
        lion.callAnimationState.updateTime(ageInTicks, 1f);
        return window(lion.callAnimationState.getAccumulatedTime() / 50f, 0f, 4f, 22f, 28f);
    }

    private void applyCallGesture(T lion) {
        if (!lion.callAnimationState.isStarted()) return;
        float t = lion.callAnimationState.getAccumulatedTime() / 50f;

        float gather = window(t, 0f, 4f, 5f, 9f);
        float rear = window(t, 5f, 10f, 15f, 21f);
        float stomp = window(t, 18f, 20f, 21f, 28f);
        if (gather + rear + stomp <= 0.001f) return;

        crouch(1.6f * gather + 1.5f * stomp);
        this.ALL.y -= 3.2f * rear;
        this.ALL.xRot += -0.38f * rear + 0.07f * stomp;
        this.left_leg.xRot += 0.38f * rear;
        this.right_leg.xRot += 0.38f * rear;
        this.left_arm.xRot -= 0.95f * rear;
        this.right_arm.xRot -= 0.8f * rear;
        this.left_arm.z -= 1.5f * rear;
        this.right_arm.z -= 1.5f * rear;

        this.head.xRot += 0.3f * gather - 0.75f * rear + 0.18f * stomp;
        this.left_ear.xRot -= 0.5f * rear;
        this.right_ear.xRot -= 0.5f * rear;
        this.tail_1.xRot -= 0.5f * rear;
        this.tail_2.yRot += Mth.sin(t * 0.9f) * 0.45f * rear;

        bristleMane(MANE_BRISTLE * 1.15f * rear + 0.05f * stomp);
    }

    private void applyArrival(T lion, float ageInTicks) {
        if (!lion.arrivalAnimationState.isStarted()) return;
        lion.arrivalAnimationState.updateTime(ageInTicks, 1f);
        float t = lion.arrivalAnimationState.getAccumulatedTime() / 50f;

        float land = window(t, 0f, 1f, 3f, 11f);
        float shake = window(t, 6f, 8f, 10f, 14f);
        if (land + shake <= 0.001f) return;

        crouch(3.2f * land);
        this.ALL.xRot += 0.12f * land;
        this.head.xRot -= 0.25f * land;
        this.head.yRot += Mth.sin(t * 2.2f) * 0.35f * shake;
        this.tail_1.xRot -= 0.5f * land;
        this.left_ear.xRot -= 0.3f * land;
        this.right_ear.xRot -= 0.3f * land;
    }

    private void applyRespond(T lion, float ageInTicks) {
        if (!lion.respondAnimationState.isStarted()) return;
        lion.respondAnimationState.updateTime(ageInTicks, 1f);
        float t = lion.respondAnimationState.getAccumulatedTime() / 50f;

        float raise = window(t, 0f, 4f, 9f, 16f);
        if (raise <= 0.001f) return;

        this.ALL.xRot -= 0.1f * raise;
        this.left_arm.xRot += 0.1f * raise;
        this.right_arm.xRot += 0.1f * raise;
        this.head.xRot -= 0.6f * raise;
        this.left_ear.xRot -= 0.4f * raise;
        this.right_ear.xRot -= 0.4f * raise;
        this.tail_1.xRot -= 0.4f * raise;
        this.tail_2.yRot += Mth.sin(t * 1.1f) * 0.35f * raise;
    }

    private void applyHuntingPosture(T lion, float ageInTicks, float pt, float stance) {
        float hunt = smoothStep(lion.empowerBlend(pt)) * stance;
        if (hunt <= 0.001f) return;

        crouch(1.2f * hunt);
        this.head.xRot += 0.15f * hunt;
        this.head.y += 0.5f * hunt;
        this.left_ear.xRot -= 0.35f * hunt;
        this.right_ear.xRot -= 0.35f * hunt;
        this.tail_1.yRot += Mth.sin(ageInTicks * 0.45f) * 0.35f * hunt;
        this.tail_2.yRot += Mth.sin(ageInTicks * 0.45f - 0.9f) * 0.5f * hunt;
    }

    private void applyEarFlick(T lion, float pt, float stance) {
        float flick = lion.earFlick(pt) * stance;
        if (flick <= 0.001f) return;

        ModelPart ear = lion.isEarFlickLeft() ? this.left_ear : this.right_ear;
        ear.zRot += (lion.isEarFlickLeft() ? -0.6f : 0.6f) * flick;
        ear.xRot -= 0.2f * flick;
    }

    private void crouch(float pixels) {
        if (pixels <= 0.001f) return;
        float angle = (float) Math.acos(Mth.clamp((LEG_LENGTH - pixels) / LEG_LENGTH, 0f, 1f));
        this.ALL.y += pixels;
        this.left_arm.xRot -= angle;
        this.right_arm.xRot -= angle;
        this.left_leg.xRot += angle;
        this.right_leg.xRot += angle;
    }

    private void bristleMane(float amount) {
        if (amount <= 0f) return;
        this.mane.xScale += amount;
        this.mane.yScale += amount;
        this.mane.zScale += amount;
    }

    private void hideEyeBalls() {
        this.left_eyeBall.xScale = 0;
        this.left_eyeBall.yScale = 0;
        this.left_eyeBall.zScale = 0;

        this.right_eyeBall.xScale = 0;
        this.right_eyeBall.yScale = 0;
        this.right_eyeBall.zScale = 0;
    }

    private static float smoothStep(float t) {
        float u = Mth.clamp(t, 0f, 1f);
        return u * u * (3f - 2f * u);
    }

    private static float window(float t, float fadeInStart, float fadeInEnd, float fadeOutStart, float fadeOutEnd) {
        if (t <= fadeInStart || t >= fadeOutEnd) return 0f;
        if (t < fadeInEnd) return smoothStep((t - fadeInStart) / (fadeInEnd - fadeInStart));
        if (t <= fadeOutStart) return 1f;
        return 1f - smoothStep((t - fadeOutStart) / (fadeOutEnd - fadeOutStart));
    }

    private void captureBodyState(LionEntity lion) {
        if (!lion.level().isClientSide()) return;
        lion.setBodyZRot((float) Math.toDegrees(this.ALL.zRot + this.body.zRot));
        lion.setBodyXRot((float) -Math.toDegrees(this.ALL.xRot + this.body.xRot));
        float ySum = this.ALL2.y + this.ALL.y + this.body.y;
        lion.bodyAnimY = ySum - (REST_POSE_Y_SUM * lion.getScale());
    }

    private boolean walkAnimCrossed(AnimationDefinition animation, float limbSwing, float speedScale, long triggerTimeMs) {
        long durationMs = (long) (animation.lengthInSeconds() * 1000f);
        if (durationMs <= 0) return false;

        long cur = ((long) (limbSwing * 50f * speedScale)) % durationMs;
        long prev = ((long) (prevLimbSwing * 50f * speedScale)) % durationMs;

        if (prev <= cur) return prev < triggerTimeMs && cur >= triggerTimeMs;
        return triggerTimeMs <= cur || triggerTimeMs > prev;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch) {
        pNetHeadYaw = Mth.clamp(pNetHeadYaw, -30.0F, 30.0F);
        pHeadPitch = Mth.clamp(pHeadPitch, -30.0F, 30.0F);

        this.head.yRot = pNetHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = pHeadPitch * ((float) Math.PI / 180F);
    }

    @Override
    public ModelPart root() {
        return this.ALL2;
    }

    public void copyPoseFrom(LionModel<?> src) {
        this.ALL2.copyFrom(src.ALL2);
        this.ALL.copyFrom(src.ALL);
        this.body.copyFrom(src.body);
        this.head.copyFrom(src.head);
        this.left_ear.copyFrom(src.left_ear);
        this.right_ear.copyFrom(src.right_ear);
        this.left_eyeBall.copyFrom(src.left_eyeBall);
        this.right_eyeBall.copyFrom(src.right_eyeBall);
        this.mane.copyFrom(src.mane);
        this.tail_1.copyFrom(src.tail_1);
        this.tail_2.copyFrom(src.tail_2);
        this.left_arm.copyFrom(src.left_arm);
        this.left_leg.copyFrom(src.left_leg);
        this.right_arm.copyFrom(src.right_arm);
        this.right_leg.copyFrom(src.right_leg);
        this.mane.visible = src.mane.visible;
    }
}
