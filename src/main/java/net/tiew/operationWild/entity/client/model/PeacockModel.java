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
import net.tiew.operationWild.entity.animals.terrestrial.PeacockEntity;
import net.tiew.operationWild.entity.client.animation.OWSedationAnimationSet;
import net.tiew.operationWild.entity.client.animation.OWSedationAnimator;
import net.tiew.operationWild.entity.client.animation.PeacockAnimations;
import org.joml.Vector3f;

public class PeacockModel<T extends PeacockEntity> extends OWComboModel<T> implements OWFlagModel {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "peacock_default"), "main");

    private static final ResourceLocation FLAG_POLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tiger/tiger_flag.png");

    private static final Anchor FLAG_ANCHOR = new Anchor(-5.75f, 11f, 0.5f, 19f);

    private static final float REST_POSE_Y_SUM = 13f;
    private static final float COMBO_ANIMATION_SPEED = 1.0f;
    private static final float WALK_ANIM_SPEED = 5.6f;
    private static final float FAN_WALK_ANIM_SPEED = 7.5f;
    private static final float FAN_FADE_MS = 250f;
    private static final float RUN_ANIM_SPEED = 1.3f;
    private static final float WALK_WEIGHT_FACTOR = 4.5f;
    private static final float RUN_WEIGHT_FACTOR = 1.25f;
    private static final float STEP_WEIGHT_MIN = 0.3f;
    private static final Vector3f ANIM_VECTOR = new Vector3f();
    private static final OWSedationAnimationSet SEDATION = new OWSedationAnimationSet(PeacockAnimations.SEDATED_FALL, PeacockAnimations.SEDATED_REST, PeacockAnimations.SEDATED_WAKE);

    private float prevLimbSwing = 0f;

    private final ModelPart ALL2;
    private final ModelPart ALL;
    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart left_eyeBall;
    private final ModelPart right_eyeBall;
    private final ModelPart crest;
    private final ModelPart left_wing;
    private final ModelPart right_wing;
    private final ModelPart tail;
    private final ModelPart train;
    private final ModelPart left_train1;
    private final ModelPart right_train1;
    private final ModelPart left_train2;
    private final ModelPart right_train2;
    private final ModelPart left_train3;
    private final ModelPart right_train3;
    private final ModelPart mainFlag;
    private final ModelPart flag;
    private final ModelPart left_leg;
    private final ModelPart right_leg;

    public PeacockModel(ModelPart root) {
        this.ALL2 = root.getChild("ALL2");
        this.ALL = this.ALL2.getChild("ALL");
        this.body = this.ALL.getChild("body");
        this.neck = this.body.getChild("neck");
        this.head = this.neck.getChild("head");
        this.left_eyeBall = this.head.getChild("left_eyeBall");
        this.right_eyeBall = this.head.getChild("right_eyeBall");
        this.crest = this.head.getChild("crest");
        this.left_wing = this.body.getChild("left_wing");
        this.right_wing = this.body.getChild("right_wing");
        this.tail = this.body.getChild("tail");
        this.train = this.tail.getChild("train");
        this.left_train1 = this.tail.getChild("left_train1");
        this.right_train1 = this.tail.getChild("right_train1");
        this.left_train2 = this.tail.getChild("left_train2");
        this.right_train2 = this.tail.getChild("right_train2");
        this.left_train3 = this.tail.getChild("left_train3");
        this.right_train3 = this.tail.getChild("right_train3");
        this.mainFlag = this.body.hasChild("mainFlag") ? this.body.getChild("mainFlag") : null;
        this.flag = this.mainFlag != null ? this.mainFlag.getChild("flag") : null;
        this.left_leg = this.ALL.getChild("left_leg");
        this.right_leg = this.ALL.getChild("right_leg");

        if (this.mainFlag != null) this.mainFlag.visible = false;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 13.0F, 0.0F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -5.0F, 8.0F, 8.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(19, 40).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 20).addBox(-4.0F, -4.0F, -5.0F, 8.0F, 8.0F, 11.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(64, 0).addBox(-1.5F, -7.0F, -1.5F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, -4.5F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(78, 0).addBox(-2.0F, -3.5F, -4.5F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(98, 0).addBox(-1.0F, -1.5F, -6.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(19, 50).addBox(-2.0F, -3.5F, -4.5F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.25F))
                .texOffs(44, 20).addBox(-1.0F, -1.5F, -5.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.3F))
                .texOffs(39, 20).addBox(2.3F, -2.0F, -5.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.01F))
                .texOffs(39, 20).mirror().addBox(-2.3F, -2.0F, -5.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(0, 40).addBox(2.45F, -1.5F, -4.5F, 0.0F, 6.0F, 9.0F, new CubeDeformation(0.01F))
                .texOffs(0, 40).mirror().addBox(-2.45F, -1.5F, -4.5F, 0.0F, 6.0F, 9.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(0.0F, -7.5F, 1.0F));

        head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(108, 0).addBox(0.0F, -1.0F, -1.0F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.025F)), PartPose.offset(2.0F, -2.5F, -1.5F));

        head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(108, 0).mirror().addBox(0.0F, -1.0F, -1.0F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.025F)).mirror(false), PartPose.offset(-2.0F, -2.5F, -1.5F));

        head.addOrReplaceChild("crest", CubeListBuilder.create().texOffs(80, 5).addBox(0.0F, -7.0F, -2.5F, 0.0F, 8.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.5F, -1.0F));

        body.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(40, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 5.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -3.0F, -3.0F));

        body.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(40, 0).mirror().addBox(-1.0F, 0.0F, 0.0F, 1.0F, 5.0F, 10.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-4.0F, -3.0F, -3.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -3.5F, 5.5F, -0.1484F, 0.0F, 0.0F));

        tail.addOrReplaceChild("train", CubeListBuilder.create().texOffs(-40, 22).addBox(-4.5F, 0.0F, 0.0F, 9.0F, 0.0F, 96.0F, new CubeDeformation(0.025F)), PartPose.offset(0.0F, 0.0F, -1.5F));

        tail.addOrReplaceChild("left_train1", CubeListBuilder.create().texOffs(-6, 22).addBox(-4.5F, 0.0F, 0.0F, 9.0F, 0.0F, 80.0F, new CubeDeformation(0.025F)), PartPose.offset(5.0F, -0.1F, -0.5F));

        tail.addOrReplaceChild("right_train1", CubeListBuilder.create().texOffs(-6, 22).mirror().addBox(-4.5F, 0.0F, 0.0F, 9.0F, 0.0F, 80.0F, new CubeDeformation(0.025F)).mirror(false), PartPose.offset(-5.0F, -0.1F, -0.5F));

        PartDefinition left_train2 = tail.addOrReplaceChild("left_train2", CubeListBuilder.create(), PartPose.offset(2.6F, -0.2F, 2.785F));

        left_train2.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(30, 22).addBox(-4.5F, 0.0F, 0.0F, 9.0F, 0.0F, 62.0F, new CubeDeformation(0.025F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.1745F, 0.0F));

        PartDefinition right_train2 = tail.addOrReplaceChild("right_train2", CubeListBuilder.create(), PartPose.offset(-2.6F, -0.2F, 2.785F));

        right_train2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(30, 22).mirror().addBox(-4.5F, 0.0F, 0.0F, 9.0F, 0.0F, 62.0F, new CubeDeformation(0.025F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.1745F, 0.0F));

        PartDefinition left_train3 = tail.addOrReplaceChild("left_train3", CubeListBuilder.create(), PartPose.offset(0.647F, -0.3F, -7.388F));

        left_train3.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(64, 22).addBox(-4.5F, 0.0F, 0.0F, 9.0F, 0.0F, 46.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.1745F, 0.0F));

        PartDefinition right_train3 = tail.addOrReplaceChild("right_train3", CubeListBuilder.create(), PartPose.offset(-0.647F, -0.3F, -7.388F));

        right_train3.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(64, 22).mirror().addBox(-4.5F, 0.0F, 0.0F, 9.0F, 0.0F, 46.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.1745F, 0.0F));

        addFlagParts(body);

        ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(120, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(98, 4).addBox(-2.5F, 7.0F, -3.5F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.01F)), PartPose.offset(2.0F, 4.0F, 1.0F));

        ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(120, 0).mirror().addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(98, 4).mirror().addBox(-2.5F, 7.0F, -3.5F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(-2.0F, 4.0F, 1.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public static void addFlagParts(PartDefinition body) {
        PartDefinition mainFlag = body.addOrReplaceChild("mainFlag", CubeListBuilder.create().texOffs(15, 1).addBox(-0.5F, -18.75F, -0.5F, 1.0F, 20.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-0.5F, -18.75F, 0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-0.5F, -6.75F, 0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(7, 15).addBox(-1.0F, -3.725F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.1F))
                .texOffs(7, 15).addBox(-1.0F, 0.275F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.1F))
                .texOffs(0, 8).addBox(-1.0F, -2.725F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.1F))
                .texOffs(0, 0).addBox(-1.0F, -20.75F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, -1.0F, 4.0F));

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
            case 1 -> PeacockAnimations.ATTACK_STRIKE;
            case 2 -> PeacockAnimations.ATTACK_STRIKE_2;
            case 3 -> PeacockAnimations.ATTACK_STRIKE_3;
            default -> null;
        };
    }

    @Override
    protected float comboSpeed(int index) {
        return switch (index) {
            case 1 -> 1.2f;
            case 2 -> 1.35f;
            case 3 -> 1.5f;
            default -> 1.0f;
        };
    }

    @Override
    public void setupAnim(T peacock, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        float sedation = OWSedationAnimator.weight(peacock, SEDATION, ageInTicks);
        if (sedation < 1f) this.setupAwakeAnim(peacock, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        OWSedationAnimator.apply(this, peacock, SEDATION, ageInTicks);
        this.head.xRot += peacock.getSedatedHeadNod(ageInTicks) * sedation;

        if (peacock.isBaby()) {
            float maturationPercent = (float) peacock.getMaturationPercentage() / 100f;
            float headScale = 1.6f - (1.6f - 1.0f) * maturationPercent;

            this.head.xScale *= headScale;
            this.head.yScale *= headScale;
            this.head.zScale *= headScale;
            this.tail.visible = maturationPercent > 0.35f;
        } else {
            this.tail.visible = true;
        }

        this.prevLimbSwing = limbSwing;

        captureBodyState(peacock);
    }

    private void setupAwakeAnim(T peacock, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float pt = Mth.clamp(ageInTicks - peacock.tickCount, 0f, 1f);
        long time = (long) (ageInTicks * 50f);

        float sweep = peacock.sweepAnimationState.isStarted() ? 1f : 0f;
        float glide = smoothStep(peacock.glideBlend(pt)) * (1f - sweep);
        float rest = smoothStep(peacock.restBlend(pt)) * (1f - sweep) * (1f - glide);
        float sit = smoothStep(peacock.sitBlend(pt)) * (1f - rest) * (1f - sweep) * (1f - glide);
        float link = smoothStep(peacock.linkBlend(pt)) * (1f - rest) * (1f - sit) * (1f - sweep) * (1f - glide);
        float aim = smoothStep(peacock.aimBlend(pt)) * (1f - rest) * (1f - sit) * (1f - sweep) * (1f - glide);
        float stance = (1f - rest) * (1f - sit) * (1f - sweep) * (1f - glide);
        int fanPhase = peacock.getFanPhase();
        long fanMs = fanMillis(peacock, ageInTicks);
        float fan = fanCover(peacock, fanPhase, fanMs);
        float fanLook = fanPhase == PeacockEntity.FAN_SHOW ? 0.5f * fan : fan;

        float look = (1f - rest) * (1f - 0.4f * sit) * (1f - link) * (1f - sweep) * (1f - 0.7f * glide) * (1f - 0.6f * aim) * (1f - fanLook);
        this.applyHeadRotation(netHeadYaw * look, headPitch * look);

        if (sit > 0.001f) KeyframeAnimations.animate(this, PeacockAnimations.SIT, time, sit, ANIM_VECTOR);
        if (rest > 0.001f) KeyframeAnimations.animate(this, PeacockAnimations.NAP, time, rest, ANIM_VECTOR);
        if (link > 0.001f) KeyframeAnimations.animate(this, PeacockAnimations.LIFE_LINK, time, link, ANIM_VECTOR);
        if (aim > 0.001f) KeyframeAnimations.animate(this, PeacockAnimations.FEATHER_AIM, time, aim, ANIM_VECTOR);
        if (glide > 0.001f) KeyframeAnimations.animate(this, PeacockAnimations.GLIDE, time, glide, ANIM_VECTOR);

        float swim = swimWeight(peacock, ageInTicks);
        float land = stance * (1f - swim);
        if (land > 0.001f) {
            float moving = animateLocomotion(peacock, limbSwing, limbSwingAmount, pt, land * (1f - fan));
            float idle = land * (1f - moving) * (1f - link) * (1f - aim) * (1f - fan);
            if (idle > 0.001f) {
                KeyframeAnimations.animate(this, PeacockAnimations.MISC_IDLE, time, idle, ANIM_VECTOR);
                animateGesture(peacock.idleGestureAnimationState, PeacockAnimations.MISC_IDLE_2, ageInTicks, idle);
                animateGesture(peacock.peckAnimationState, PeacockAnimations.MISC_PECK, ageInTicks, idle);
                animateGesture(peacock.callAnimationState, PeacockAnimations.CALL, ageInTicks, idle);
            }
        }
        animateSwim(peacock, PeacockAnimations.SWIM, ageInTicks, stance * swim);
        if (fanPhase != PeacockEntity.FAN_NONE) animateFan(peacock, fanPhase, fanMs, fan * stance, limbSwing, limbSwingAmount);

        if (glide < 0.5f) {
            if (peacock.featherShotRightState.isStarted()) {
                this.animate(peacock.featherShotRightState, PeacockAnimations.FEATHER_SHOT_RIGHT, ageInTicks, 1f);
            }
            if (peacock.featherShotLeftState.isStarted()) {
                this.animate(peacock.featherShotLeftState, PeacockAnimations.FEATHER_SHOT_LEFT, ageInTicks, 1f);
            }
        }

        float takeoff = peacock.glideFlapSeconds(pt);
        if (takeoff >= 0f && takeoff < PeacockAnimations.GLIDE_TAKEOFF.lengthInSeconds()) {
            KeyframeAnimations.animate(this, PeacockAnimations.GLIDE_TAKEOFF, (long) (takeoff * 1000f), 1f - 0.5f * glide, ANIM_VECTOR);
        }
        float landing = peacock.glideLandSeconds(pt);
        if (landing >= 0f && landing < PeacockAnimations.GLIDE_LAND.lengthInSeconds()) {
            KeyframeAnimations.animate(this, PeacockAnimations.GLIDE_LAND, (long) (landing * 1000f), 1f, ANIM_VECTOR);
        }
        if (glide > 0.001f) applyGlideAttitude(peacock, pt, glide);
        if (peacock.sweepAnimationState.isStarted()) {
            this.animate(peacock.sweepAnimationState, PeacockAnimations.TAIL_SWEEP, ageInTicks, 1f);
        }

        animateCombos(peacock, ageInTicks, COMBO_ANIMATION_SPEED);
    }

    private void applyGlideAttitude(T peacock, float pt, float glide) {
        float tilt = peacock.glideTilt(pt);
        float roll = peacock.glideRoll(pt);
        this.ALL.xRot += tilt * glide * Mth.DEG_TO_RAD;
        this.ALL.zRot -= roll * glide * Mth.DEG_TO_RAD;
        this.neck.xRot -= tilt * 0.35f * glide * Mth.DEG_TO_RAD;

        float dive = Mth.clamp(tilt / 40f, 0f, 1f) * glide;
        float sweepBack = dive * 28f * Mth.DEG_TO_RAD;
        float fold = dive * 22f * Mth.DEG_TO_RAD;
        float lean = Mth.clamp(roll / 38f, -1f, 1f) * glide * 10f * Mth.DEG_TO_RAD;
        this.left_wing.xRot += sweepBack;
        this.right_wing.xRot += sweepBack;
        this.left_wing.zRot += fold - lean;
        this.right_wing.zRot -= fold + lean;
        this.tail.yRot += Mth.clamp(roll / 38f, -1f, 1f) * glide * 12f * Mth.DEG_TO_RAD;
    }

    public void translateToWingTip(PoseStack poseStack, boolean left) {
        this.ALL2.translateAndRotate(poseStack);
        this.ALL.translateAndRotate(poseStack);
        this.body.translateAndRotate(poseStack);
        (left ? this.left_wing : this.right_wing).translateAndRotate(poseStack);
        poseStack.translate((left ? 0.5f : -0.5f) / 16f, 4.2f / 16f, 7.5f / 16f);
    }

    private static long fanMillis(PeacockEntity peacock, float ageInTicks) {
        AnimationState state = peacock.fanPhaseState;
        if (!state.isStarted()) return 0L;
        state.updateTime(ageInTicks, 1f);
        return state.getAccumulatedTime();
    }

    private static float fanCover(PeacockEntity peacock, int phase, long ms) {
        if (phase == PeacockEntity.FAN_NONE) return 0f;
        if (phase == PeacockEntity.FAN_OPEN && peacock.fanPrevPhase == PeacockEntity.FAN_NONE) return smoothStep(ms / FAN_FADE_MS);
        if (phase == PeacockEntity.FAN_CLOSE) return smoothStep((PeacockEntity.FAN_CLOSE_TICKS * 50L - ms) / FAN_FADE_MS);
        return 1f;
    }

    private void animateFan(PeacockEntity peacock, int phase, long ms, float weight, float limbSwing, float limbSwingAmount) {
        float move = phase == PeacockEntity.FAN_SHOW ? Math.min(limbSwingAmount * WALK_WEIGHT_FACTOR, 1f) : 0f;
        peacock.fanShowMove = move;
        float previous = peacock.fanPrevPhase == PeacockEntity.FAN_NONE ? 0f : 1f - smoothStep(ms / FAN_FADE_MS);
        if (previous > 0.001f) {
            animateFanPhase(peacock.fanPrevPhase, peacock.fanPrevMs, weight * previous, limbSwing, peacock.fanPrevMove);
        }
        animateFanPhase(phase, ms, weight * (1f - previous), limbSwing, move);
    }

    private void animateFanPhase(int phase, long ms, float weight, float limbSwing, float move) {
        if (weight <= 0.001f) return;
        switch (phase) {
            case PeacockEntity.FAN_OPEN -> KeyframeAnimations.animate(this, PeacockAnimations.FAN_OPEN, ms, weight, ANIM_VECTOR);
            case PeacockEntity.FAN_SHAKE -> KeyframeAnimations.animate(this, PeacockAnimations.FAN_SHAKE, ms, weight, ANIM_VECTOR);
            case PeacockEntity.FAN_DANCE -> KeyframeAnimations.animate(this, PeacockAnimations.FAN_DANCE, ms, weight, ANIM_VECTOR);
            case PeacockEntity.FAN_SHOW -> {
                if (move < 0.999f) KeyframeAnimations.animate(this, PeacockAnimations.FAN_IDLE, ms, weight * (1f - move), ANIM_VECTOR);
                if (move > 0.001f) KeyframeAnimations.animate(this, PeacockAnimations.FAN_WALK, (long) (limbSwing * 50f * FAN_WALK_ANIM_SPEED), weight * move, ANIM_VECTOR);
            }
            case PeacockEntity.FAN_CLOSE -> KeyframeAnimations.animate(this, PeacockAnimations.FAN_CLOSE, ms, weight, ANIM_VECTOR);
            default -> { }
        }
    }

    private void animateGesture(AnimationState state, AnimationDefinition animation, float ageInTicks, float weight) {
        if (!state.isStarted()) return;
        state.updateTime(ageInTicks, 1f);
        long ms = state.getAccumulatedTime();
        long length = (long) (animation.lengthInSeconds() * 1000f);
        if (ms >= length) return;
        KeyframeAnimations.animate(this, animation, ms, weight * gestureEnvelope(ms, length), ANIM_VECTOR);
    }

    private static float gestureEnvelope(long ms, long length) {
        float fade = 250f;
        float in = Mth.clamp(ms / fade, 0f, 1f);
        float out = Mth.clamp((length - ms) / fade, 0f, 1f);
        return smoothStep(Math.min(in, out));
    }

    private float animateLocomotion(T peacock, float limbSwing, float limbSwingAmount, float pt, float stance) {
        float run = smoothStep(peacock.runBlend(pt));
        float moving = Mth.lerp(run, Math.min(limbSwingAmount * WALK_WEIGHT_FACTOR, 1f), Math.min(limbSwingAmount * RUN_WEIGHT_FACTOR, 1f));
        float walkWeight = moving * (1f - run) * stance;
        float runWeight = moving * run * stance;

        if (walkWeight > 0.001f) {
            KeyframeAnimations.animate(this, PeacockAnimations.MOVE_WALK, (long) (limbSwing * 50f * WALK_ANIM_SPEED), walkWeight, ANIM_VECTOR);
        }
        if (runWeight > 0.001f) {
            KeyframeAnimations.animate(this, PeacockAnimations.MOVE_RUN, (long) (limbSwing * 50f * RUN_ANIM_SPEED), runWeight, ANIM_VECTOR);
        }

        if (run >= 0.5f && runWeight > STEP_WEIGHT_MIN) {
            if (walkAnimCrossed(PeacockAnimations.MOVE_RUN, limbSwing, RUN_ANIM_SPEED, 83L)) peacock.onRightFootDown();
            if (walkAnimCrossed(PeacockAnimations.MOVE_RUN, limbSwing, RUN_ANIM_SPEED, 333L)) peacock.onLeftFootDown();
        } else if (run < 0.5f && walkWeight > STEP_WEIGHT_MIN) {
            if (walkAnimCrossed(PeacockAnimations.MOVE_WALK, limbSwing, WALK_ANIM_SPEED, 83L)) peacock.onRightFootDown();
            if (walkAnimCrossed(PeacockAnimations.MOVE_WALK, limbSwing, WALK_ANIM_SPEED, 583L)) peacock.onLeftFootDown();
        }
        return moving;
    }

    private static float smoothStep(float t) {
        float u = Mth.clamp(t, 0f, 1f);
        return u * u * (3f - 2f * u);
    }

    private void captureBodyState(PeacockEntity peacock) {
        if (!peacock.level().isClientSide()) return;
        peacock.setBodyZRot((float) Math.toDegrees(this.ALL.zRot + this.body.zRot));
        peacock.setBodyXRot((float) -Math.toDegrees(this.ALL.xRot + this.body.xRot));
        float ySum = this.ALL2.y + this.ALL.y + this.body.y;
        peacock.bodyAnimY = ySum - (REST_POSE_Y_SUM * peacock.getScale());
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
        pNetHeadYaw = Mth.clamp(pNetHeadYaw, -40.0F, 40.0F);
        pHeadPitch = Mth.clamp(pHeadPitch, -30.0F, 30.0F);

        this.neck.yRot = pNetHeadYaw * 0.4f * ((float) Math.PI / 180F);
        this.head.yRot = pNetHeadYaw * 0.6f * ((float) Math.PI / 180F);
        this.head.xRot = pHeadPitch * ((float) Math.PI / 180F);
    }

    @Override
    public ModelPart root() {
        return this.ALL2;
    }

    public void copyPoseFrom(PeacockModel<?> src) {
        this.ALL2.copyFrom(src.ALL2);
        this.ALL.copyFrom(src.ALL);
        this.body.copyFrom(src.body);
        this.neck.copyFrom(src.neck);
        this.head.copyFrom(src.head);
        this.left_eyeBall.copyFrom(src.left_eyeBall);
        this.right_eyeBall.copyFrom(src.right_eyeBall);
        this.crest.copyFrom(src.crest);
        this.left_wing.copyFrom(src.left_wing);
        this.right_wing.copyFrom(src.right_wing);
        this.tail.copyFrom(src.tail);
        this.train.copyFrom(src.train);
        this.left_train1.copyFrom(src.left_train1);
        this.right_train1.copyFrom(src.right_train1);
        this.left_train2.copyFrom(src.left_train2);
        this.right_train2.copyFrom(src.right_train2);
        this.left_train3.copyFrom(src.left_train3);
        this.right_train3.copyFrom(src.right_train3);
        this.left_leg.copyFrom(src.left_leg);
        this.right_leg.copyFrom(src.right_leg);
        this.tail.visible = src.tail.visible;
    }
}
