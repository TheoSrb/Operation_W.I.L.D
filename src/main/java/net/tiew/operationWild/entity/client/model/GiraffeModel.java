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
import net.minecraft.world.entity.AnimationState;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.GiraffeEntity;
import net.tiew.operationWild.entity.client.animation.GiraffeAnimations;
import net.tiew.operationWild.entity.client.animation.OWSedationAnimationSet;
import net.tiew.operationWild.entity.client.animation.OWSedationAnimator;
import org.joml.Vector3f;

public class GiraffeModel<T extends GiraffeEntity> extends OWComboModel<T> implements OWFlagModel, IOWPlatformModel {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "giraffe_default"), "main");

    private static final ResourceLocation FLAG_POLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/elephant_flag.png");

    private static final Anchor FLAG_ANCHOR = new Anchor(-5.75f, 11f, 0.5f, 19f);

    private static final float REST_POSE_Y_SUM = -17f;
    private static final float COMBO_ANIMATION_SPEED = 1.0f;
    private static final float WALK_ANIM_SPEED = 2.8f;
    private static final float RUN_ANIM_SPEED = 1.5f;
    private static final float WALK_WEIGHT_FACTOR = 4.0f;
    private static final float RUN_WEIGHT_FACTOR = 1.25f;
    private static final float STEP_WEIGHT_MIN = 0.3f;
    private static final long WALK_STEP_RIGHT_MS = 250L;
    private static final long WALK_STEP_LEFT_MS = 1250L;
    private static final long RUN_STEP_RIGHT_MS = 100L;
    private static final long RUN_STEP_LEFT_MS = 600L;
    private static final float GESTURE_FADE_MS = 300f;
    private static final long BLESS_HOLD_START_MS = 625L;
    private static final long BLESS_HOLD_LOOP_MS = 250L;
    private static final Vector3f ANIM_VECTOR = new Vector3f();
    private static final OWSedationAnimationSet SEDATION = new OWSedationAnimationSet(GiraffeAnimations.SEDATED_FALL, GiraffeAnimations.SEDATED_REST, GiraffeAnimations.SEDATED_WAKE);

    public float externalBankRoll = 0f;
    private float prevLimbSwing = 0f;

    private final ModelPart ALL2;
    private final ModelPart ALL;
    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart left_ear;
    private final ModelPart right_ear;
    private final ModelPart left_horn;
    private final ModelPart right_horn;
    private final ModelPart left_eyeBall;
    private final ModelPart right_eyeBall;
    private final ModelPart tail;
    private final ModelPart front_tail;
    private final ModelPart back_tail;
    private final ModelPart mainFlag;
    private final ModelPart flag;
    private final ModelPart left_arm;
    private final ModelPart right_arm;
    private final ModelPart right_leg;
    private final ModelPart left_leg;

    public GiraffeModel(ModelPart root) {
        this.ALL2 = root.getChild("ALL2");
        this.ALL = this.ALL2.getChild("ALL");
        this.body = this.ALL.getChild("body");
        this.neck = this.body.getChild("neck");
        this.head = this.neck.getChild("head");
        this.left_ear = this.head.getChild("left_ear");
        this.right_ear = this.head.getChild("right_ear");
        this.left_horn = this.head.getChild("left_horn");
        this.right_horn = this.head.getChild("right_horn");
        this.left_eyeBall = this.head.getChild("left_eyeBall");
        this.right_eyeBall = this.head.getChild("right_eyeBall");
        this.tail = this.body.getChild("tail");
        this.front_tail = this.tail.getChild("front_tail");
        this.back_tail = this.tail.getChild("back_tail");
        this.mainFlag = this.body.getChild("mainFlag");
        this.flag = this.mainFlag.getChild("flag");
        this.left_arm = this.ALL.getChild("left_arm");
        this.right_arm = this.ALL.getChild("right_arm");
        this.right_leg = this.ALL.getChild("right_leg");
        this.left_leg = this.ALL.getChild("left_leg");
        this.mainFlag.visible = false;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, -16.0F, 0.0F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -10.0F, -16.0F, 16.0F, 21.0F, 33.0F, new CubeDeformation(0.0F))
                .texOffs(120, 71).addBox(-5.0F, -13.0F, 2.0F, 10.0F, 3.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(88, 131).addBox(-9.0F, -11.0F, 2.0F, 4.0F, 1.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(45, 167).addBox(-9.0F, -10.0F, 2.0F, 1.0F, 18.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(160, 221).addBox(-24.0F, 8.0F, -13.0F, 16.0F, 2.0F, 32.0F, new CubeDeformation(0.0F))
                .texOffs(160, 221).mirror().addBox(8.0F, 8.0F, -13.0F, 16.0F, 2.0F, 32.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(45, 167).mirror().addBox(8.0F, -10.0F, 2.0F, 1.0F, 18.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(88, 131).mirror().addBox(5.0F, -11.0F, 2.0F, 4.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(139, 0).addBox(-8.0F, -10.0F, -16.0F, 16.0F, 21.0F, 33.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, -1.0F, 0.0F));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(56, 54).addBox(-4.0F, -35.0F, -14.0F, 8.0F, 40.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(113, 66).addBox(-1.0F, -35.0F, -5.0F, 2.0F, 26.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(62, 126).addBox(0.0F, -36.0F, -4.0F, 0.0F, 22.0F, 3.0F, new CubeDeformation(0.025F))
                .texOffs(120, 0).addBox(0.0F, -12.0F, -4.0F, 0.0F, 3.0F, 9.0F, new CubeDeformation(0.025F))
                .texOffs(0, 54).addBox(-6.0F, -9.0F, -10.0F, 12.0F, 14.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(170, 55).addBox(6.3F, -37.0F, -19.0F, 0.0F, 34.0F, 31.0F, new CubeDeformation(0.01F))
                .texOffs(170, 55).mirror().addBox(-6.3F, -37.0F, -19.0F, 0.0F, 34.0F, 31.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(0.0F, -8.0F, -4.0F, 0.3491F, 0.0F, 0.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(3, 155).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(62, 103).addBox(-3.0F, -8.0F, -12.0F, 6.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(113, 66).addBox(-1.0F, -7.0F, 5.0F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(62, 120).addBox(0.0F, -7.0F, 6.0F, 0.0F, 6.0F, 3.0F, new CubeDeformation(0.025F))
                .texOffs(0, 85).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 9.0F, new CubeDeformation(0.25F))
                .texOffs(120, 86).addBox(-3.0F, -8.0F, -12.0F, 6.0F, 8.0F, 8.0F, new CubeDeformation(0.25F))
                .texOffs(99, 0).addBox(-6.0F, -2.0F, -9.0F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.05F))
                .texOffs(126, 0).addBox(6.1F, -3.0F, -10.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.01F))
                .texOffs(126, 0).mirror().addBox(-6.1F, -3.0F, -10.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(0.0F, -35.0F, -10.0F));

        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(120, 17).mirror().addBox(0.0F, -2.0F, -0.5F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(4.0F, -5.0F, 2.5F));

        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(120, 17).addBox(-8.0F, -2.0F, -0.5F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -5.0F, 2.5F));

        head.addOrReplaceChild("left_horn", CubeListBuilder.create().texOffs(48, 84).mirror().addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0F, -7.0F, 2.0F, -0.4363F, 0.0F, 0.0F));

        head.addOrReplaceChild("right_horn", CubeListBuilder.create().texOffs(48, 84).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -7.0F, 2.0F, -0.4363F, 0.0F, 0.0F));

        head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(14, 142).addBox(0.0F, -0.5F, -1.0F, 0.0F, 1.0F, 3.0F, new CubeDeformation(0.01F))
                .texOffs(18, 145).addBox(-1.0F, -0.5F, -1.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.offset(4.0F, -6.5F, -3.0F));

        head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(14, 142).mirror().addBox(0.0F, -0.5F, -1.0F, 0.0F, 1.0F, 3.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(18, 145).mirror().addBox(0.0F, -0.5F, -1.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(-4.0F, -6.5F, -3.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, -6.5F, 17.0F));

        tail.addOrReplaceChild("front_tail", CubeListBuilder.create().texOffs(98, 36).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition back_tail = tail.addOrReplaceChild("back_tail", CubeListBuilder.create().texOffs(0, 104).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(112, 101).addBox(0.0F, -5.5F, 12.0F, 0.0F, 11.0F, 11.0F, new CubeDeformation(0.025F)), PartPose.offset(0.0F, 0.0F, 15.0F));

        back_tail.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(112, 101).addBox(-1.0F, -23.0F, 29.0F, 0.0F, 11.0F, 11.0F, new CubeDeformation(0.025F)), PartPose.offsetAndRotation(18.0F, -1.5F, -17.0F, 0.0F, 0.0F, -1.5708F));

        addFlagParts(body);

        ALL.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(90, 54).addBox(-2.6F, 0.0F, -3.0F, 5.0F, 30.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(4.6F, 10.0F, -12.0F));

        ALL.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(90, 54).mirror().addBox(-2.4F, 0.0F, -3.0F, 5.0F, 30.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-4.6F, 10.0F, -12.0F));

        ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(90, 54).mirror().addBox(-2.4F, 0.0F, -3.0F, 5.0F, 30.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-4.6F, 10.0F, 15.0F));

        ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(90, 54).addBox(-2.6F, 0.0F, -3.0F, 5.0F, 30.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(4.6F, 10.0F, 15.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    public static void addFlagParts(PartDefinition body) {
        PartDefinition mainFlag = body.addOrReplaceChild("mainFlag", CubeListBuilder.create().texOffs(15, 1).addBox(-0.5F, -18.75F, -0.5F, 1.0F, 20.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-0.5F, -18.75F, 0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-0.5F, -6.75F, 0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(7, 15).addBox(-1.0F, -3.725F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.1F))
                .texOffs(7, 15).addBox(-1.0F, 0.275F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.1F))
                .texOffs(0, 8).addBox(-1.0F, -2.725F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.1F))
                .texOffs(0, 0).addBox(-1.0F, -20.75F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(5.5F, -11.3F, 14.0F));

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
    public void translateToPlatformDeck(PoseStack poseStack) {
        this.ALL2.translateAndRotate(poseStack);
        this.ALL.translateAndRotate(poseStack);
        this.body.translateAndRotate(poseStack);
        poseStack.translate(0.0, 8.0 / 16.0, 3.0 / 16.0);
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
            case 1 -> GiraffeAnimations.ATTACK_STRIKE;
            case 2 -> GiraffeAnimations.ATTACK_STRIKE_2;
            case 3 -> GiraffeAnimations.ATTACK_STRIKE_3;
            default -> null;
        };
    }

    @Override
    protected float comboSpeed(int index) {
        return switch (index) {
            case 1 -> 1.0f;
            case 2 -> 1.1f;
            case 3 -> 1.2f;
            default -> 1.0f;
        };
    }

    @Override
    public void setupAnim(T giraffe, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.mainFlag.visible = false;

        float sedation = OWSedationAnimator.weight(giraffe, SEDATION, ageInTicks);
        if (sedation < 1f) this.setupAwakeAnim(giraffe, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        OWSedationAnimator.apply(this, giraffe, SEDATION, ageInTicks);
        this.head.xRot += giraffe.getSedatedHeadNod(ageInTicks) * sedation;

        if (giraffe.isBaby()) {
            float maturationPercent = (float) giraffe.getMaturationPercentage() / 100f;
            float headScale = 1.5f - 0.5f * maturationPercent;
            this.head.xScale *= headScale;
            this.head.yScale *= headScale;
            this.head.zScale *= headScale;
        }

        this.prevLimbSwing = limbSwing;

        captureBodyState(giraffe);
    }

    private void setupAwakeAnim(T giraffe, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float pt = Mth.clamp(ageInTicks - giraffe.tickCount, 0f, 1f);
        long time = (long) (ageInTicks * 50f);

        float bless = smoothStep(giraffe.blessBlend(pt));
        float look = giraffe.sentinelAnimationState.isStarted() ? 0f : 1f - bless;

        if (giraffe.transitionIdleSit.isStarted()) {
            this.animate(giraffe.transitionIdleSit, GiraffeAnimations.TRANSITION_IDLE_SIT, ageInTicks, 1.0f);
            return;
        }
        if (giraffe.transitionSitIdle.isStarted()) {
            this.animate(giraffe.transitionSitIdle, GiraffeAnimations.TRANSITION_SIT_IDLE, ageInTicks, 1.0f);
            return;
        }
        if (giraffe.isSitting()) {
            this.applyHeadRotation(netHeadYaw * 0.6f, headPitch * 0.6f);
            this.animate(giraffe.sittingAnimationState, GiraffeAnimations.SIT, ageInTicks, 1.0f);
            return;
        }
        if (giraffe.napInState.isStarted()) {
            this.animate(giraffe.napInState, GiraffeAnimations.TRANSITION_IDLE_NAP, ageInTicks, 1.0f);
            return;
        }
        if (giraffe.isNapping() || (giraffe.isSleeping() && !giraffe.hasSedationAnimations())) {
            KeyframeAnimations.animate(this, GiraffeAnimations.NAP, time, 1.0f, ANIM_VECTOR);
            return;
        }
        if (giraffe.napOutState.isStarted()) {
            this.animate(giraffe.napOutState, GiraffeAnimations.TRANSITION_NAP_IDLE, ageInTicks, 1.0f);
            return;
        }

        this.applyHeadRotation(netHeadYaw * look, headPitch * look);
        if (Math.abs(externalBankRoll) > 0.01f) this.body.zRot += externalBankRoll * Mth.DEG_TO_RAD;

        float swim = swimWeight(giraffe, ageInTicks);
        float land = 1f - swim;
        if (land > 0.001f) {
            float moving = animateLocomotion(giraffe, limbSwing, limbSwingAmount, pt, land);
            float idle = land * (1f - moving);
            if (idle > 0.001f) {
                KeyframeAnimations.animate(this, GiraffeAnimations.MISC_IDLE, time, idle, ANIM_VECTOR);
                animateGesture(giraffe.idleGestureAnimationState, GiraffeAnimations.MISC_IDLE_2, ageInTicks, idle);
            }
        }
        animateSwim(giraffe, GiraffeAnimations.SWIM, ageInTicks, swim);

        if (bless > 0.001f) {
            KeyframeAnimations.animate(this, GiraffeAnimations.BLESSING, blessingMillis(giraffe.blessSeconds(pt)), bless, ANIM_VECTOR);
        }
        if (giraffe.sentinelAnimationState.isStarted()) {
            this.animate(giraffe.sentinelAnimationState, GiraffeAnimations.SENTINEL_CAST, ageInTicks, 1.0f);
        }

        animateCombos(giraffe, ageInTicks, COMBO_ANIMATION_SPEED);
    }

    private static long blessingMillis(float seconds) {
        long ms = (long) (seconds * 1000f);
        if (ms <= BLESS_HOLD_START_MS) return ms;
        return BLESS_HOLD_START_MS + (ms - BLESS_HOLD_START_MS) % BLESS_HOLD_LOOP_MS;
    }

    private void animateGesture(AnimationState state, AnimationDefinition animation, float ageInTicks, float weight) {
        if (!state.isStarted()) return;
        state.updateTime(ageInTicks, 1f);
        long ms = state.getAccumulatedTime();
        long length = (long) (animation.lengthInSeconds() * 1000f);
        if (ms >= length) return;
        float in = Mth.clamp(ms / GESTURE_FADE_MS, 0f, 1f);
        float out = Mth.clamp((length - ms) / GESTURE_FADE_MS, 0f, 1f);
        KeyframeAnimations.animate(this, animation, ms, weight * smoothStep(Math.min(in, out)), ANIM_VECTOR);
    }

    private float animateLocomotion(T giraffe, float limbSwing, float limbSwingAmount, float pt, float stance) {
        float run = smoothStep(giraffe.runBlend(pt));
        float moving = Mth.lerp(run, Math.min(limbSwingAmount * WALK_WEIGHT_FACTOR, 1f), Math.min(limbSwingAmount * RUN_WEIGHT_FACTOR, 1f));
        float walkWeight = moving * (1f - run) * stance;
        float runWeight = moving * run * stance;

        if (walkWeight > 0.001f) {
            KeyframeAnimations.animate(this, GiraffeAnimations.MOVE_WALK, (long) (limbSwing * 50f * WALK_ANIM_SPEED), walkWeight, ANIM_VECTOR);
        }
        if (runWeight > 0.001f) {
            KeyframeAnimations.animate(this, GiraffeAnimations.MOVE_RUN, (long) (limbSwing * 50f * RUN_ANIM_SPEED), runWeight, ANIM_VECTOR);
        }

        if (run >= 0.5f && runWeight > STEP_WEIGHT_MIN) {
            if (walkAnimCrossed(GiraffeAnimations.MOVE_RUN, limbSwing, RUN_ANIM_SPEED, RUN_STEP_RIGHT_MS)) giraffe.onRightFootDown();
            if (walkAnimCrossed(GiraffeAnimations.MOVE_RUN, limbSwing, RUN_ANIM_SPEED, RUN_STEP_LEFT_MS)) giraffe.onLeftFootDown();
        } else if (run < 0.5f && walkWeight > STEP_WEIGHT_MIN) {
            if (walkAnimCrossed(GiraffeAnimations.MOVE_WALK, limbSwing, WALK_ANIM_SPEED, WALK_STEP_RIGHT_MS)) giraffe.onRightFootDown();
            if (walkAnimCrossed(GiraffeAnimations.MOVE_WALK, limbSwing, WALK_ANIM_SPEED, WALK_STEP_LEFT_MS)) giraffe.onLeftFootDown();
        }
        return moving;
    }

    private static float smoothStep(float t) {
        float u = Mth.clamp(t, 0f, 1f);
        return u * u * (3f - 2f * u);
    }

    private void captureBodyState(GiraffeEntity giraffe) {
        if (!giraffe.level().isClientSide()) return;
        giraffe.setBodyZRot((float) Math.toDegrees(this.ALL.zRot + this.body.zRot));
        giraffe.setBodyXRot((float) -Math.toDegrees(this.ALL.xRot + this.body.xRot));
        float ySum = this.ALL2.y + this.ALL.y + this.body.y;
        giraffe.bodyAnimY = ySum - (REST_POSE_Y_SUM * giraffe.getScale());

        PoseStack bones = new PoseStack();
        this.ALL2.translateAndRotate(bones);
        this.ALL.translateAndRotate(bones);
        this.body.translateAndRotate(bones);
        giraffe.boneMatrix = new org.joml.Matrix4f(bones.last().pose());
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

    public void renderHorns(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        poseStack.pushPose();
        this.ALL2.translateAndRotate(poseStack);
        this.ALL.translateAndRotate(poseStack);
        this.body.translateAndRotate(poseStack);
        this.neck.translateAndRotate(poseStack);
        this.head.translateAndRotate(poseStack);
        this.left_horn.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.right_horn.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        poseStack.popPose();
    }

    private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch) {
        pNetHeadYaw = Mth.clamp(pNetHeadYaw, -45.0F, 45.0F);
        pHeadPitch = Mth.clamp(pHeadPitch, -25.0F, 30.0F);

        this.neck.yRot += pNetHeadYaw * 0.45f * Mth.DEG_TO_RAD;
        this.head.yRot += pNetHeadYaw * 0.55f * Mth.DEG_TO_RAD;
        this.head.xRot += pHeadPitch * Mth.DEG_TO_RAD;
    }

    @Override
    public ModelPart root() {
        return this.ALL2;
    }

    public void copyPoseFrom(GiraffeModel<?> src) {
        this.ALL2.copyFrom(src.ALL2);
        this.ALL.copyFrom(src.ALL);
        this.body.copyFrom(src.body);
        this.neck.copyFrom(src.neck);
        this.head.copyFrom(src.head);
        this.left_ear.copyFrom(src.left_ear);
        this.right_ear.copyFrom(src.right_ear);
        this.left_horn.copyFrom(src.left_horn);
        this.right_horn.copyFrom(src.right_horn);
        this.left_eyeBall.copyFrom(src.left_eyeBall);
        this.right_eyeBall.copyFrom(src.right_eyeBall);
        this.tail.copyFrom(src.tail);
        this.front_tail.copyFrom(src.front_tail);
        this.back_tail.copyFrom(src.back_tail);
        this.left_arm.copyFrom(src.left_arm);
        this.right_arm.copyFrom(src.right_arm);
        this.right_leg.copyFrom(src.right_leg);
        this.left_leg.copyFrom(src.left_leg);
    }
}
