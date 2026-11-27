package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.AnimationChannel;
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
import net.tiew.operationWild.entity.animals.flying.CondorEntity;
import net.tiew.operationWild.entity.client.animation.CondorAnimations;
import net.tiew.operationWild.entity.client.animation.OWSedationAnimationSet;
import net.tiew.operationWild.entity.client.animation.OWSedationAnimator;
import org.joml.Vector3f;

public class CondorModel<T extends CondorEntity> extends OWComboModel<T> implements OWFlagModel {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "condor_default"), "main");

    private static final ResourceLocation FLAG_POLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/elephant_flag.png");

    private static final Anchor FLAG_ANCHOR = new Anchor(-5.75f, 11f, 0.5f, 19f);

    private static final float WALK_ANIM_SPEED = 3.5f;
    private static final float RUN_ANIM_SPEED = 1.0f;
    private static final float WALK_WEIGHT_FACTOR = 4.0f;
    private static final float RUN_WEIGHT_FACTOR = 1.25f;
    private static final float STEP_WEIGHT_MIN = 0.3f;
    private static final float GESTURE_FADE_MS = 300f;
    private static final float ONE_SHOT_FADE_MS = 200f;
    private static final Vector3f ANIM_VECTOR = new Vector3f();
    private static final AnimationDefinition GRAB_LEGS = legsOnly(CondorAnimations.FLY_GRAB);
    private static final AnimationDefinition CARRY_LEGS = legsOnly(CondorAnimations.FLY_CARRY);
    private static final AnimationDefinition DROP_LEGS = legsOnly(CondorAnimations.FLY_DROP);
    private static final OWSedationAnimationSet SEDATION =new OWSedationAnimationSet(CondorAnimations.SEDATED_FALL, CondorAnimations.SEDATED_REST, CondorAnimations.SEDATED_WAKE);

    public float externalRiderPitch = 0f;
    public float externalBankRoll = 0f;

    private float prevLimbSwing = 0f;

    private final ModelPart ALL2;
    private final ModelPart ALL;
    private final ModelPart body;
    private final ModelPart body2;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart beak;
    private final ModelPart left_eyeBall;
    private final ModelPart right_eyeBall;
    private final ModelPart left_wing;
    private final ModelPart left_wing2;
    private final ModelPart right_wing;
    private final ModelPart right_wing2;
    private final ModelPart tail;
    private final ModelPart mainFlag;
    private final ModelPart flag;
    private final ModelPart left_leg;
    private final ModelPart right_leg;
    private final org.joml.Matrix4f talonRestInverse;

    public CondorModel(ModelPart root) {
        this.ALL2 = root.getChild("ALL2");
        this.ALL = this.ALL2.getChild("ALL");
        this.body = this.ALL.getChild("body");
        this.body2 = this.body.getChild("body2");
        this.neck = this.body2.getChild("neck");
        this.head = this.neck.getChild("head");
        this.beak = this.head.getChild("beak");
        this.left_eyeBall = this.head.getChild("left_eyeBall");
        this.right_eyeBall = this.head.getChild("right_eyeBall");
        this.left_wing = this.body2.getChild("left_wing");
        this.left_wing2 = this.left_wing.getChild("left_wing2");
        this.right_wing = this.body2.getChild("right_wing");
        this.right_wing2 = this.right_wing.getChild("right_wing2");
        this.tail = this.body.getChild("tail");
        this.mainFlag = this.body.hasChild("mainFlag") ? this.body.getChild("mainFlag") : null;
        this.flag = this.mainFlag != null ? this.mainFlag.getChild("flag") : null;
        this.left_leg = this.ALL.getChild("left_leg");
        this.right_leg = this.ALL.getChild("right_leg");

        if (this.mainFlag != null) this.mainFlag.visible = false;

        PoseStack rest = new PoseStack();
        this.ALL2.translateAndRotate(rest);
        this.ALL.translateAndRotate(rest);
        this.talonRestInverse = new org.joml.Matrix4f(rest.last().pose()).invert();
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 9.0F, 1.0F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(60, 0).addBox(-6.0F, -1.0F, -2.0F, 12.0F, 10.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 0.0F));

        PartDefinition body2 = body.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -7.0F, -11.0F, 14.0F, 14.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(116, 0).addBox(-6.0F, -11.0F, -12.0F, 12.0F, 6.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(90, 52).addBox(-6.0F, -5.0F, -12.0F, 12.0F, 5.0F, 0.0F, new CubeDeformation(0.01F))
                .texOffs(0, 26).addBox(-4.0F, -13.0F, -10.0F, 8.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(107, 16).addBox(-6.0F, -11.0F, -12.0F, 12.0F, 6.0F, 9.0F, new CubeDeformation(0.25F))
                .texOffs(159, 14).addBox(-7.0F, -7.0F, -11.0F, 14.0F, 14.0F, 11.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 1.0F, -2.0F));

        PartDefinition neck = body2.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(204, 0).addBox(-2.0F, -6.0F, -3.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -9.0F, -11.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(160, 0).addBox(-3.0F, -5.0F, -6.0F, 6.0F, 6.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(224, 0).addBox(0.0F, -9.0F, -9.0F, 0.0F, 7.0F, 8.0F, new CubeDeformation(0.01F))
                .texOffs(51, 22).addBox(-3.0F, -5.0F, -6.0F, 6.0F, 6.0F, 7.0F, new CubeDeformation(0.25F))
                .texOffs(107, 0).addBox(3.3F, -2.0F, -7.5F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.01F))
                .texOffs(107, 0).mirror().addBox(-3.3F, -2.0F, -7.5F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(210, 16).addBox(3.5F, -4.0F, -6.5F, 0.0F, 6.0F, 14.0F, new CubeDeformation(0.01F))
                .texOffs(210, 16).mirror().addBox(-3.5F, -4.0F, -6.5F, 0.0F, 6.0F, 14.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(0.0F, -4.0F, -2.0F));

        head.addOrReplaceChild("beak", CubeListBuilder.create().texOffs(120, 60).addBox(-1.5F, -1.5F, -4.0F, 3.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(51, 0).addBox(-1.5F, -1.5F, -1.0F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.3F)), PartPose.offset(0.0F, -0.5F, -6.0F));

        head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(246, 0).addBox(0.0F, -1.0F, -1.5F, 0.0F, 1.0F, 3.0F, new CubeDeformation(0.025F)), PartPose.offset(3.0F, -3.0F, -2.5F));

        head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(246, 0).mirror().addBox(0.0F, -1.0F, -1.5F, 0.0F, 1.0F, 3.0F, new CubeDeformation(0.025F)).mirror(false), PartPose.offset(-3.0F, -3.0F, -2.5F));

        PartDefinition left_wing = body2.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(0, 40).addBox(-1.5F, -8.0F, -1.0F, 3.0F, 16.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offset(8.5F, -1.0F, -8.0F));

        left_wing.addOrReplaceChild("left_wing2", CubeListBuilder.create().texOffs(46, 40).addBox(0.0F, -5.0F, -2.0F, 0.0F, 16.0F, 20.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 2.0F, 17.0F));

        PartDefinition right_wing = body2.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(0, 40).mirror().addBox(-1.5F, -8.0F, -1.0F, 3.0F, 16.0F, 18.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-8.5F, -1.0F, -8.0F));

        right_wing.addOrReplaceChild("right_wing2", CubeListBuilder.create().texOffs(46, 40).mirror().addBox(0.0F, -5.0F, -2.0F, 0.0F, 16.0F, 20.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(0.0F, 2.0F, 17.0F));

        body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(90, 40).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 0.0F, 11.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 8.0F, 8.0F));

        addFlagParts(body);

        ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(150, 60).addBox(-1.5F, 1.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(166, 60).addBox(-3.5F, 8.0F, -7.0F, 7.0F, 0.0F, 9.0F, new CubeDeformation(0.01F)), PartPose.offset(4.0F, 7.0F, 0.0F));

        ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(150, 60).mirror().addBox(-1.5F, 1.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(166, 60).mirror().addBox(-3.5F, 8.0F, -7.0F, 7.0F, 0.0F, 9.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(-4.0F, 7.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    public static void addFlagParts(PartDefinition body) {
        PartDefinition mainFlag = body.addOrReplaceChild("mainFlag", CubeListBuilder.create().texOffs(15, 1).addBox(-0.5F, -18.75F, -0.5F, 1.0F, 20.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-0.5F, -18.75F, 0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-0.5F, -6.75F, 0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(7, 15).addBox(-1.0F, -3.725F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.1F))
                .texOffs(7, 15).addBox(-1.0F, 0.275F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.1F))
                .texOffs(0, 8).addBox(-1.0F, -2.725F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.1F))
                .texOffs(0, 0).addBox(-1.0F, -20.75F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(4.5F, -1.5F, 7.0F));

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
            case 1 -> CondorAnimations.ATTACK_STRIKE;
            case 2 -> CondorAnimations.ATTACK_STRIKE_2;
            case 3 -> CondorAnimations.ATTACK_STRIKE_3;
            default -> null;
        };
    }

    @Override
    protected float comboSpeed(int index) {
        return switch (index) {
            case 2 -> 1.05f;
            case 3 -> 1.1f;
            default -> 1.0f;
        };
    }

    @Override
    public void setupAnim(T condor, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        if (Math.abs(externalRiderPitch) > 0.01f) this.ALL2.xRot = externalRiderPitch * Mth.DEG_TO_RAD;
        if (Math.abs(externalBankRoll) > 0.01f) this.ALL2.zRot = externalBankRoll * Mth.DEG_TO_RAD;

        float sedation = OWSedationAnimator.weight(condor, SEDATION, ageInTicks);
        if (sedation < 1f) this.setupAwakeAnim(condor, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        OWSedationAnimator.apply(this, condor, SEDATION, ageInTicks);
        this.head.xRot += condor.getSedatedHeadNod(ageInTicks) * sedation;

        if (condor.isBaby()) {
            float maturationPercent = (float) condor.getMaturationPercentage() / 100f;
            float headScale = 1.6f - 0.6f * maturationPercent;
            this.head.xScale *= headScale;
            this.head.yScale *= headScale;
            this.head.zScale *= headScale;
        }

        this.prevLimbSwing = limbSwing;

        captureBodyState(condor);
    }

    private void setupAwakeAnim(T condor, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float pt = Mth.clamp(ageInTicks - condor.tickCount, 0f, 1f);
        long time = (long) (ageInTicks * 50f);

        float air = smoothStep(condor.airBlend(pt));
        float soar = smoothStep(condor.soarBlend(pt));
        float dash = smoothStep(condor.dashBlend(pt));
        float ground = 1f - air;

        float rest = smoothStep(condor.restBlend(pt)) * ground;
        float sit = smoothStep(condor.sitBlend(pt)) * (1f - rest) * ground;
        float stance = (1f - rest) * (1f - sit) * ground;

        float look = (1f - rest) * (1f - 0.4f * sit) * (1f - 0.6f * air);
        this.applyHeadRotation(netHeadYaw * look, headPitch * look);

        if (sit > 0.001f) KeyframeAnimations.animate(this, CondorAnimations.SIT, time, sit, ANIM_VECTOR);
        if (rest > 0.001f) KeyframeAnimations.animate(this, CondorAnimations.NAP, time, rest, ANIM_VECTOR);

        float swim = swimWeight(condor, ageInTicks);
        float land = stance * (1f - swim);
        if (land > 0.001f) {
            float moving = animateLocomotion(condor, limbSwing, limbSwingAmount, pt, land);
            float idle = land * (1f - moving);
            if (idle > 0.001f) {
                KeyframeAnimations.animate(this, CondorAnimations.MISC_IDLE, time, idle, ANIM_VECTOR);
                animateGesture(condor.sunbatheAnimationState, CondorAnimations.MISC_SUNBATHE, ageInTicks, idle);
                animateGesture(condor.preenAnimationState, CondorAnimations.MISC_PREEN, ageInTicks, idle);
                animateGesture(condor.shakeAnimationState, CondorAnimations.MISC_SHAKE, ageInTicks, idle);
                animateGesture(condor.displayAnimationState, CondorAnimations.MISC_DISPLAY, ageInTicks, idle);
            }
        }
        animateSwim(condor, CondorAnimations.SWIM, ageInTicks, stance * swim);

        float drop = oneShotWeight(CondorAnimations.FLY_DROP, condor.talonDropSeconds(pt));
        float grab = oneShotWeight(CondorAnimations.FLY_GRAB, condor.talonGrabSeconds(pt)) * (1f - drop);
        float carry = smoothStep(condor.carryBlend(pt)) * (1f - grab) * (1f - drop);
        float flapping = 1f - soar;
        float talon = Mth.clamp((grab + drop + carry) * flapping, 0f, 1f);

        float flight = air * (1f - talon);
        if (flight > 0.001f) animateFlight(condor, pt, time, flight, soar, dash);
        if (air * talon > 0.001f) animateTalons(condor, pt, air * grab * flapping, air * carry * flapping, air * drop * flapping);
        float glidingTalons = air * soar * Mth.clamp(grab + drop + carry, 0f, 1f);
        if (glidingTalons > 0.001f) holdTalonLegs(condor, pt, grab, carry, drop, glidingTalons);

        animateOneShot(CondorAnimations.TAKEOFF, condor.takeoffSeconds(pt), 1f);
        animateOneShot(CondorAnimations.LANDING, condor.landingSeconds(pt), 1f);

        animateCombos(condor, ageInTicks, 1.0f);
    }

    private void animateFlight(T condor, float pt, long time, float air, float soar, float dash) {
        float flap = air * (1f - soar);
        float phase = condor.flapPhase(pt);
        float cruise = flap * (1f - dash);
        float fast = flap * dash;

        if (cruise > 0.001f) {
            long ms = (long) (phase * CondorAnimations.FLY.lengthInSeconds() * 1000f);
            KeyframeAnimations.animate(this, CondorAnimations.FLY, ms, cruise, ANIM_VECTOR);
        }
        if (fast > 0.001f) {
            long ms = (long) (phase * CondorAnimations.RUN_FLY.lengthInSeconds() * 1000f);
            KeyframeAnimations.animate(this, CondorAnimations.RUN_FLY, ms, fast, ANIM_VECTOR);
        }
        if (air * soar > 0.001f) {
            KeyframeAnimations.animate(this, CondorAnimations.FLY_GLIDE, time, air * soar, ANIM_VECTOR);
        }

        float hover = smoothStep(condor.hoverBlend(pt)) * flap;
        if (hover > 0.001f) {
            this.body2.xRot -= 14f * hover * Mth.DEG_TO_RAD;
            this.neck.xRot += 10f * hover * Mth.DEG_TO_RAD;
            this.tail.xRot += 22f * hover * Mth.DEG_TO_RAD;
            this.left_leg.xRot += 18f * hover * Mth.DEG_TO_RAD;
            this.right_leg.xRot += 18f * hover * Mth.DEG_TO_RAD;
        }

        float climb = condor.climbBlend(pt) * flap;
        if (climb > 0.001f) {
            this.body2.xRot -= 12f * climb * Mth.DEG_TO_RAD;
            this.neck.xRot += 8f * climb * Mth.DEG_TO_RAD;
            this.tail.xRot += 18f * climb * Mth.DEG_TO_RAD;
        }

        float roll = Mth.clamp(externalBankRoll / 50f, -1f, 1f) * air;
        this.tail.yRot += roll * 14f * Mth.DEG_TO_RAD;
        this.neck.zRot -= roll * 8f * Mth.DEG_TO_RAD;
        this.left_wing2.yRot += Math.max(0f, roll) * 10f * Mth.DEG_TO_RAD * soar;
        this.right_wing2.yRot -= Math.max(0f, -roll) * 10f * Mth.DEG_TO_RAD * soar;
    }

    private void animateTalons(T condor, float pt, float grab, float carry, float drop) {
        if (grab > 0.001f) {
            KeyframeAnimations.animate(this, CondorAnimations.FLY_GRAB, (long) (condor.talonGrabSeconds(pt) * 1000f), grab, ANIM_VECTOR);
        }
        if (carry > 0.001f) {
            long ms = (long) (condor.flapPhase(pt) * CondorAnimations.FLY_CARRY.lengthInSeconds() * 1000f);
            KeyframeAnimations.animate(this, CondorAnimations.FLY_CARRY, ms, carry, ANIM_VECTOR);
        }
        if (drop > 0.001f) {
            KeyframeAnimations.animate(this, CondorAnimations.FLY_DROP, (long) (condor.talonDropSeconds(pt) * 1000f), drop, ANIM_VECTOR);
        }
    }

    private void holdTalonLegs(T condor, float pt, float grab, float carry, float drop, float weight) {
        float total = grab + carry + drop;
        if (total <= 0.001f) return;
        PartPose leftBefore = this.left_leg.storePose();
        PartPose rightBefore = this.right_leg.storePose();
        this.left_leg.resetPose();
        this.right_leg.resetPose();
        if (grab > 0.001f) {
            KeyframeAnimations.animate(this, GRAB_LEGS, (long) (condor.talonGrabSeconds(pt) * 1000f), grab / total, ANIM_VECTOR);
        }
        if (carry > 0.001f) {
            long ms = (long) (condor.flapPhase(pt) * CARRY_LEGS.lengthInSeconds() * 1000f);
            KeyframeAnimations.animate(this, CARRY_LEGS, ms, carry / total, ANIM_VECTOR);
        }
        if (drop > 0.001f) {
            KeyframeAnimations.animate(this, DROP_LEGS, (long) (condor.talonDropSeconds(pt) * 1000f), drop / total, ANIM_VECTOR);
        }
        blendPart(this.left_leg, leftBefore, weight);
        blendPart(this.right_leg, rightBefore, weight);
    }

    private static void blendPart(ModelPart part, PartPose from, float weight) {
        part.x = Mth.lerp(weight, from.x, part.x);
        part.y = Mth.lerp(weight, from.y, part.y);
        part.z = Mth.lerp(weight, from.z, part.z);
        part.xRot = Mth.lerp(weight, from.xRot, part.xRot);
        part.yRot = Mth.lerp(weight, from.yRot, part.yRot);
        part.zRot = Mth.lerp(weight, from.zRot, part.zRot);
    }

    private static AnimationDefinition legsOnly(AnimationDefinition source) {
        AnimationDefinition.Builder builder = AnimationDefinition.Builder.withLength(source.lengthInSeconds());
        if (source.looping()) builder.looping();
        for (String bone : new String[]{"left_leg", "right_leg"}) {
            for (AnimationChannel channel : source.boneAnimations().getOrDefault(bone, java.util.List.of())) {
                builder.addAnimation(bone, channel);
            }
        }
        return builder.build();
    }

    private static float oneShotWeight(AnimationDefinition animation, float seconds) {
        if (seconds < 0f) return 0f;
        long ms = (long) (seconds * 1000f);
        long total = (long) (animation.lengthInSeconds() * 1000f);
        if (ms >= total) return 0f;
        float in = Mth.clamp(ms / ONE_SHOT_FADE_MS, 0f, 1f);
        float out = Mth.clamp((total - ms) / ONE_SHOT_FADE_MS, 0f, 1f);
        return smoothStep(Math.min(in, out));
    }

    private void animateOneShot(AnimationDefinition animation, float seconds, float weight) {
        if (seconds < 0f) return;
        float length = animation.lengthInSeconds();
        if (seconds >= length) return;
        long ms = (long) (seconds * 1000f);
        long total = (long) (length * 1000f);
        float in = Mth.clamp(ms / ONE_SHOT_FADE_MS, 0f, 1f);
        float out = Mth.clamp((total - ms) / ONE_SHOT_FADE_MS, 0f, 1f);
        KeyframeAnimations.animate(this, animation, ms, weight * smoothStep(Math.min(in, out)), ANIM_VECTOR);
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

    private float animateLocomotion(T condor, float limbSwing, float limbSwingAmount, float pt, float stance) {
        float run = smoothStep(condor.runBlend(pt));
        float moving = Mth.lerp(run, Math.min(limbSwingAmount * WALK_WEIGHT_FACTOR, 1f), Math.min(limbSwingAmount * RUN_WEIGHT_FACTOR, 1f));
        float walkWeight = moving * (1f - run) * stance;
        float runWeight = moving * run * stance;

        if (walkWeight > 0.001f) {
            KeyframeAnimations.animate(this, CondorAnimations.MOVE_WALK, (long) (limbSwing * 50f * WALK_ANIM_SPEED), walkWeight, ANIM_VECTOR);
        }
        if (runWeight > 0.001f) {
            KeyframeAnimations.animate(this, CondorAnimations.MOVE_RUN, (long) (limbSwing * 50f * RUN_ANIM_SPEED), runWeight, ANIM_VECTOR);
        }

        if (run >= 0.5f && runWeight > STEP_WEIGHT_MIN) {
            if (walkAnimCrossed(CondorAnimations.MOVE_RUN, limbSwing, RUN_ANIM_SPEED, 0L)) condor.onLeftFootDown();
            if (walkAnimCrossed(CondorAnimations.MOVE_RUN, limbSwing, RUN_ANIM_SPEED, 250L)) condor.onRightFootDown();
        } else if (run < 0.5f && walkWeight > STEP_WEIGHT_MIN) {
            if (walkAnimCrossed(CondorAnimations.MOVE_WALK, limbSwing, WALK_ANIM_SPEED, 0L)) condor.onLeftFootDown();
            if (walkAnimCrossed(CondorAnimations.MOVE_WALK, limbSwing, WALK_ANIM_SPEED, 500L)) condor.onRightFootDown();
        }
        return moving;
    }

    private static float smoothStep(float t) {
        float u = Mth.clamp(t, 0f, 1f);
        return u * u * (3f - 2f * u);
    }

    private void captureBodyState(CondorEntity condor) {
        if (!condor.level().isClientSide()) return;
        condor.setBodyZRot((float) Math.toDegrees(this.ALL2.zRot + this.ALL.zRot + this.body.zRot + this.body2.zRot));
        condor.setBodyXRot((float) -Math.toDegrees(this.ALL2.xRot + this.ALL.xRot + this.body.xRot + this.body2.xRot));

        PoseStack bones = new PoseStack();
        this.ALL2.translateAndRotate(bones);
        this.ALL.translateAndRotate(bones);
        this.body.translateAndRotate(bones);
        this.body2.translateAndRotate(bones);
        condor.boneMatrix = new org.joml.Matrix4f(bones.last().pose());

        PoseStack talons = new PoseStack();
        this.ALL2.translateAndRotate(talons);
        this.ALL.translateAndRotate(talons);
        condor.talonFrame = new org.joml.Matrix4f(talons.last().pose()).mul(this.talonRestInverse);
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
        pNetHeadYaw = Mth.clamp(pNetHeadYaw, -45.0F, 45.0F);
        pHeadPitch = Mth.clamp(pHeadPitch, -30.0F, 30.0F);

        this.neck.yRot += pNetHeadYaw * 0.4f * Mth.DEG_TO_RAD;
        this.head.yRot += pNetHeadYaw * 0.6f * Mth.DEG_TO_RAD;
        this.head.xRot += pHeadPitch * Mth.DEG_TO_RAD;
    }

    @Override
    public ModelPart root() {
        return this.ALL2;
    }

    public void copyPoseFrom(CondorModel<?> src) {
        this.ALL2.copyFrom(src.ALL2);
        this.ALL.copyFrom(src.ALL);
        this.body.copyFrom(src.body);
        this.body2.copyFrom(src.body2);
        this.neck.copyFrom(src.neck);
        this.head.copyFrom(src.head);
        this.beak.copyFrom(src.beak);
        this.left_eyeBall.copyFrom(src.left_eyeBall);
        this.right_eyeBall.copyFrom(src.right_eyeBall);
        this.left_wing.copyFrom(src.left_wing);
        this.left_wing2.copyFrom(src.left_wing2);
        this.right_wing.copyFrom(src.right_wing);
        this.right_wing2.copyFrom(src.right_wing2);
        this.tail.copyFrom(src.tail);
        this.left_leg.copyFrom(src.left_leg);
        this.right_leg.copyFrom(src.right_leg);
    }
}
