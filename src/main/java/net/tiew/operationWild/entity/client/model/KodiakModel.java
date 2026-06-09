package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.item.ItemDisplayContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.attacks.OWAttackLogic;
import net.tiew.operationWild.entity.client.animation.KodiakAnimations;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.client.animation.TigerAnimations;

public class KodiakModel<T extends KodiakEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "kodiak_default"), "main");

    public final ModelPart ALL2;
    public final ModelPart ALL;
    private final ModelPart right_leg;
    private final ModelPart left_leg;
    public final ModelPart body;
    public final ModelPart body_2;
    public final ModelPart body_1;
    public final ModelPart head;
    private final ModelPart left_ear;
    private final ModelPart right_ear;
    private final ModelPart left_eyeBall;
    private final ModelPart right_eyeBall;
    private final ModelPart muzzle;
    private final ModelPart left_arm;
    private final ModelPart right_arm;

    private KodiakEntity currentEntity;

    public KodiakModel(ModelPart root) {
        this.ALL2 = root.getChild("ALL2");
        this.ALL = this.ALL2.getChild("ALL");
        this.right_leg = this.ALL.getChild("right_leg");
        this.left_leg = this.ALL.getChild("left_leg");
        this.body = this.ALL.getChild("body");
        this.body_2 = this.body.getChild("body_2");
        this.body_1 = this.body_2.getChild("body_1");
        this.head = this.body_1.getChild("head");
        this.left_ear = this.head.getChild("left_ear");
        this.right_ear = this.head.getChild("right_ear");
        this.left_eyeBall = this.head.getChild("left_eyeBall");
        this.right_eyeBall = this.head.getChild("right_eyeBall");
        this.muzzle = this.head.getChild("muzzle");
        this.left_arm = this.body_1.getChild("left_arm");
        this.right_arm = this.body_1.getChild("right_arm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 6.0F, 0.0F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(68, 105).addBox(-2.5F, 0.0F, -3.5F, 7.0F, 10.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(228, 0).addBox(-2.5F, 0.0F, -3.5F, 7.0F, 10.0F, 7.0F, new CubeDeformation(0.1F)), PartPose.offset(-7.5F, 8.0F, 13.5F));

        PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(68, 105).mirror().addBox(-4.5F, 0.0F, -3.5F, 7.0F, 10.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(228, 0).mirror().addBox(-4.5F, 0.0F, -3.5F, 7.0F, 10.0F, 7.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offset(7.5F, 8.0F, 13.5F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, 0.0F));

        PartDefinition body_2 = body.addOrReplaceChild("body_2", CubeListBuilder.create().texOffs(0, 0).addBox(-11.0F, -15.0F, -12.0F, 22.0F, 19.0F, 20.0F, new CubeDeformation(0.0F))
                .texOffs(172, 32).addBox(-11.0F, -15.0F, -12.0F, 22.0F, 19.0F, 20.0F, new CubeDeformation(0.1F))
                .texOffs(82, 137).addBox(-11.0F, -15.0F, -12.0F, 22.0F, 19.0F, 20.0F, new CubeDeformation(0.5F))
                .texOffs(125, 38).addBox(-11.0F, 4.0F, 8.0F, 22.0F, 5.0F, 0.0F, new CubeDeformation(0.05F))
                .texOffs(125, 94).addBox(-11.0F, 4.0F, 8.0F, 22.0F, 5.0F, 0.0F, new CubeDeformation(0.075F))
                .texOffs(125, 94).addBox(-11.0F, 4.0F, 7.9F, 22.0F, 5.0F, 0.0F, new CubeDeformation(0.075F))
                .texOffs(121, -20).addBox(-11.0F, 4.0F, -12.0F, 0.0F, 5.0F, 20.0F, new CubeDeformation(0.05F))
                .texOffs(121, 36).addBox(-11.0F, 4.0F, -12.0F, 0.0F, 5.0F, 20.0F, new CubeDeformation(0.075F))
                .texOffs(121, 36).addBox(-10.9F, 4.0F, -12.0F, 0.0F, 5.0F, 20.0F, new CubeDeformation(0.075F))
                .texOffs(121, -20).mirror().addBox(11.0F, 4.0F, -12.0F, 0.0F, 5.0F, 20.0F, new CubeDeformation(0.05F)).mirror(false)
                .texOffs(121, 36).mirror().addBox(11.0F, 4.0F, -12.0F, 0.0F, 5.0F, 20.0F, new CubeDeformation(0.075F)).mirror(false)
                .texOffs(121, 36).mirror().addBox(10.9F, 4.0F, -12.0F, 0.0F, 5.0F, 20.0F, new CubeDeformation(0.075F)).mirror(false)
                .texOffs(0, 127).addBox(-7.0F, -18.0F, -10.0F, 14.0F, 5.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, 12.0F));

        PartDefinition cube_r1 = body_2.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(225, 76).mirror().addBox(-6.0F, -11.0F, 0.0F, 12.0F, 22.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-12.0F, -7.0F, -2.0F, -1.6107F, 0.0948F, -1.5274F));

        PartDefinition cube_r2 = body_2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(225, 76).addBox(-6.0F, -11.0F, 0.0F, 12.0F, 22.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(12.0F, -7.0F, -2.0F, -1.6107F, -0.0948F, 1.5274F));

        PartDefinition body_1 = body_2.addOrReplaceChild("body_1", CubeListBuilder.create().texOffs(0, 39).addBox(-9.0F, -9.0F, -14.0F, 18.0F, 17.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(192, 0).addBox(-9.0F, -9.0F, -14.0F, 18.0F, 17.0F, 14.0F, new CubeDeformation(0.1F))
                .texOffs(0, 170).addBox(-9.0F, -9.0F, -14.0F, 18.0F, 17.0F, 14.0F, new CubeDeformation(0.5F))
                .texOffs(0, 127).addBox(-7.0F, -12.0F, -14.0F, 14.0F, 5.0F, 14.0F, new CubeDeformation(-0.1F))
                .texOffs(125, 4).addBox(-9.0F, 8.0F, -14.0F, 0.0F, 5.0F, 14.0F, new CubeDeformation(0.05F))
                .texOffs(125, 60).addBox(-9.0F, 8.0F, -14.0F, 0.0F, 5.0F, 14.0F, new CubeDeformation(0.075F))
                .texOffs(125, 60).addBox(-8.9F, 8.0F, -14.0F, 0.0F, 5.0F, 14.0F, new CubeDeformation(0.075F))
                .texOffs(125, 4).mirror().addBox(9.0F, 8.0F, -14.0F, 0.0F, 5.0F, 14.0F, new CubeDeformation(0.05F)).mirror(false)
                .texOffs(125, 60).mirror().addBox(9.0F, 8.0F, -14.0F, 0.0F, 5.0F, 14.0F, new CubeDeformation(0.075F)).mirror(false)
                .texOffs(125, 60).mirror().addBox(8.9F, 8.0F, -14.0F, 0.0F, 5.0F, 14.0F, new CubeDeformation(0.075F)).mirror(false), PartPose.offset(0.0F, -4.0F, -12.0F));

        PartDefinition head = body_1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(64, 39).addBox(-6.0F, -5.0F, -12.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(208, 0).addBox(-6.0F, -5.0F, -12.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.1F))
                .texOffs(230, 243).addBox(-6.0F, -5.0F, -1.0F, 12.0F, 12.0F, 1.0F, new CubeDeformation(0.5F))
                .texOffs(232, 224).addBox(6.0F, -5.0F, -1.0F, 12.0F, 12.0F, 0.0F, new CubeDeformation(0.05F))
                .texOffs(231, 198).addBox(-6.0F, -17.0F, -0.5F, 12.0F, 12.0F, 0.0F, new CubeDeformation(0.05F))
                .texOffs(232, 224).mirror().addBox(-18.0F, -5.0F, -1.0F, 12.0F, 12.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false)
                .texOffs(69, 202).addBox(-6.0F, -5.0F, -12.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.5F))
                .texOffs(62, 126).addBox(-6.0F, -5.0F, -16.0F, 0.0F, 12.0F, 4.0F, new CubeDeformation(0.05F))
                .texOffs(62, 126).mirror().addBox(6.0F, -5.0F, -16.0F, 0.0F, 12.0F, 4.0F, new CubeDeformation(0.05F)).mirror(false)
                .texOffs(84, 24).addBox(-4.0F, 1.0F, -17.025F, 8.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(230, 0).addBox(-4.0F, 1.0F, -17.025F, 8.0F, 6.0F, 5.0F, new CubeDeformation(0.1F))
                .texOffs(4, 158).addBox(-7.0F, 5.0F, -17.0F, 14.0F, 1.0F, 1.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, -1.0F, -14.025F));

        PartDefinition cube_r3 = head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 213).mirror().addBox(6.0F, -13.0F, -9.0F, 0.0F, 11.0F, 14.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(1.25F, -1.5F, 1.0F, 0.0F, -0.2618F, 0.0F));

        PartDefinition cube_r4 = head.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 213).addBox(-6.0F, -13.0F, -9.0F, 0.0F, 11.0F, 14.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.25F, -1.5F, 1.0F, 0.0F, 0.2618F, 0.0F));

        PartDefinition cube_r5 = head.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 229).addBox(-6.0F, -13.0F, -9.0F, 0.0F, 11.0F, 14.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-2.0F, 8.0F, -8.0F, 0.0F, -0.2618F, 0.0F));

        PartDefinition cube_r6 = head.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 229).mirror().addBox(6.0F, -13.0F, -9.0F, 0.0F, 11.0F, 14.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(2.0F, 8.0F, -8.0F, 0.0F, 0.2618F, 0.0F));

        PartDefinition cube_r7 = head.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(231, 181).addBox(-6.0F, -12.0F, 6.0F, 12.0F, 12.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(0.0F, 7.0F, -6.5F, 0.0F, 0.0F, -3.1416F));

        PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(40, 70).addBox(-2.0F, -2.0F, -0.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(246, 0).addBox(-2.0F, -2.0F, -0.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.1F)), PartPose.offset(6.0F, -5.0F, -4.5F));

        PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(50, 70).addBox(-2.0F, -2.0F, -0.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(246, 0).addBox(-2.0F, -2.0F, -0.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.1F)), PartPose.offset(-6.0F, -5.0F, -4.5F));

        PartDefinition left_eyeBall = head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(77, 63).addBox(-1.5F, -0.5F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offset(3.5F, -0.5F, -12.025F));

        PartDefinition right_eyeBall = head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(77, 63).mirror().addBox(-1.5F, -0.5F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offset(-3.5F, -0.5F, -12.025F));

        PartDefinition muzzle = head.addOrReplaceChild("muzzle", CubeListBuilder.create().texOffs(89, 27).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, -17.0F));

        PartDefinition left_arm = body_1.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(68, 88).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 10.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(228, 0).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 10.0F, 7.0F, new CubeDeformation(0.1F)), PartPose.offset(4.5F, 8.0F, -10.5F));

        PartDefinition right_arm = body_1.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(68, 88).mirror().addBox(-3.5F, 0.0F, -3.5F, 7.0F, 10.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(228, 0).mirror().addBox(-3.5F, 0.0F, -3.5F, 7.0F, 10.0F, 7.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offset(-4.5F, 8.0F, -10.5F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(KodiakEntity kodiak, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.currentEntity = kodiak;

        if (kodiak.isBaby()) {
            float maturationPercent = (float) kodiak.getMaturationPercentage() / 100f;
            float headScale = 1.6f - (1.6f - 1.0f) * maturationPercent;

            this.head.xScale *= headScale;
            this.head.yScale *= headScale;
            this.head.zScale *= headScale;
        }
        this.applyHeadRotation(netHeadYaw, headPitch);

        if (kodiak.isCombo(1) || kodiak.attack1Combo.isStarted()) {
            this.animate(kodiak.attack1Combo, KodiakAnimations.ATTACK_STRIKE, ageInTicks, 1.0f * OWEntity.comboSpeedMultiplier);
        }
        if (kodiak.isCombo(2) || kodiak.attack2Combo.isStarted()) {
            this.animate(kodiak.attack2Combo, KodiakAnimations.ATTACK_STRIKE2, ageInTicks, 1.1f * OWEntity.comboSpeedMultiplier);
        }
        if (kodiak.isCombo(3)) {
            this.animate(kodiak.attack3Combo, KodiakAnimations.ATTACK_STRIKE3, ageInTicks, 1.25f * OWEntity.comboSpeedMultiplier);
        }

        if (kodiak.isMad()) {
            this.left_eyeBall.xScale = 0;
            this.left_eyeBall.yScale = 0;
            this.left_eyeBall.zScale = 0;

            this.right_eyeBall.xScale = 0;
            this.right_eyeBall.yScale = 0;
            this.right_eyeBall.zScale = 0;
        }

        if (kodiak.isPawSlamCharging()) {
            Minecraft mc = Minecraft.getInstance();
            boolean isLocalRider = mc.player != null && mc.player.getVehicle() == kodiak;
            boolean someoneElseRiding = kodiak.getControllingPassenger() != null && !isLocalRider;

            if ((isLocalRider && OWAttackLogic.isCharging) || someoneElseRiding) {
                this.animate(kodiak.pawSlamChargeAnimState, KodiakAnimations.PAW_SLAM_CHARGE, ageInTicks, 1.0f);
                if (kodiak.pawSlamChargeAnimState.getAccumulatedTime() >= 3000L) {
                    this.animate(kodiak.pawSlamChargeFullAnimState, KodiakAnimations.PAW_SLAM_CHARGE_FULL, ageInTicks, 1.0f);
                }
                float chargeProgress = Math.min(kodiak.pawSlamChargeAnimState.getAccumulatedTime() / 2500f, 1.0f);
                kodiak.pawSlamRiderYExtra = chargeProgress * 0.7f;
                kodiak.pawSlamRiderZExtra = chargeProgress * -0.6f;
                return;
            }
        }

        if (kodiak.isPawSlamStriking()) {
            this.animate(kodiak.pawSlamStrikeAnimState, KodiakAnimations.PAW_SLAM_STRIKE, ageInTicks, 1.0f);
            float strikeElapsed = kodiak.pawSlamStrikeAnimState.getAccumulatedTime() - 250f;
            float strikeProgress = Math.max(0f, Math.min(strikeElapsed / 350f, 1.0f));
            kodiak.pawSlamRiderYExtra = (1.0f - strikeProgress) * 0.7f;
            kodiak.pawSlamRiderZExtra = (1.0f - strikeProgress) * -0.6f;
            return;
        }

        if (kodiak.isRubs()) {
            this.animate(kodiak.rubsAnimationState, KodiakAnimations.RUBS, ageInTicks, 1.0f);
            captureBodyState(kodiak, 10f, false, this.ALL2, this.ALL, this.body, this.body_2, this.body_1);
            captureBodyState(kodiak, 14f, true, this.ALL2, this.ALL, this.body, this.body_2);
            return;
        }

        if (kodiak.isRolling()) {
            this.animate(kodiak.rollingAnimationState, KodiakAnimations.ROLL, ageInTicks, 1.0f);
            captureBodyState(kodiak, 10f, false, this.ALL2, this.ALL, this.body, this.body_2, this.body_1);
            captureBodyState(kodiak, 14f, true, this.ALL2, this.ALL, this.body, this.body_2);
            return;
        }

        if (kodiak.transitionIdleSit.isStarted()) {
            this.animate(kodiak.transitionIdleSit, KodiakAnimations.TRANSITION_IDLE_SIT, ageInTicks, 1.0f);
            captureBodyState(kodiak, 10f, false, this.ALL2, this.ALL, this.body, this.body_2, this.body_1);
            captureBodyState(kodiak, 14f, true, this.ALL2, this.ALL, this.body, this.body_2);
            return;
        }

        if (kodiak.transitionSitIdle.isStarted()) {
            this.animate(kodiak.transitionSitIdle, KodiakAnimations.TRANSITION_SIT_IDLE, ageInTicks, 1.0f);
            captureBodyState(kodiak, 10f, false, this.ALL2, this.ALL, this.body, this.body_2, this.body_1);
            captureBodyState(kodiak, 14f, true, this.ALL2, this.ALL, this.body, this.body_2);
            return;
        }

        if (kodiak.transitionIdleSleep.isStarted()) {
            this.animate(kodiak.transitionIdleSleep, KodiakAnimations.TRANSITION_IDLE_SLEEP, ageInTicks, 2.0f);
            captureBodyState(kodiak, 10f, false, this.ALL2, this.ALL, this.body, this.body_2, this.body_1);
            captureBodyState(kodiak, 14f, true, this.ALL2, this.ALL, this.body, this.body_2);
            return;
        }

        if (kodiak.transitionSleepIdle.isStarted()) {
            this.animate(kodiak.transitionSleepIdle, KodiakAnimations.TRANSITION_SLEEP_IDLE, ageInTicks, 2.0f);
            captureBodyState(kodiak, 10f, false, this.ALL2, this.ALL, this.body, this.body_2, this.body_1);
            captureBodyState(kodiak, 14f, true, this.ALL2, this.ALL, this.body, this.body_2);
            return;
        }

        if (kodiak.transitionIdleStandingUp.isStarted()) {
            this.animate(kodiak.transitionIdleStandingUp, KodiakAnimations.TRANSITION_IDLE_STAND_UP, ageInTicks, 1.0f);
            captureBodyState(kodiak, 10f, false, this.ALL2, this.ALL, this.body, this.body_2, this.body_1);
            captureBodyState(kodiak, 14f, true, this.ALL2, this.ALL, this.body, this.body_2);
            return;
        }

        if (kodiak.transitionStandingUpIdle.isStarted()) {
            this.animate(kodiak.transitionStandingUpIdle, KodiakAnimations.TRANSITION_STAND_UP_IDLE, ageInTicks, 1.0f);
            captureBodyState(kodiak, 10f, false, this.ALL2, this.ALL, this.body, this.body_2, this.body_1);
            captureBodyState(kodiak, 14f, true, this.ALL2, this.ALL, this.body, this.body_2);
            return;
        }

        if (kodiak.isNapping()) {
            this.animate(kodiak.napAnimationState, KodiakAnimations.SLEEP, ageInTicks, 1.0f);
            captureBodyState(kodiak, 10f, false, this.ALL2, this.ALL, this.body, this.body_2, this.body_1);
            captureBodyState(kodiak, 14f, true, this.ALL2, this.ALL, this.body, this.body_2);
            return;
        }


        if (kodiak.isSitting()) {
            this.animate(kodiak.sittingAnimationState, KodiakAnimations.SIT, ageInTicks, 1.0f);
            captureBodyState(kodiak, 10f, false, this.ALL2, this.ALL, this.body, this.body_2, this.body_1);
            captureBodyState(kodiak, 14f, true, this.ALL2, this.ALL, this.body, this.body_2);
            return;
        }

        if (kodiak.isSniffing()) {
            this.animate(kodiak.sniffingAnimationState, KodiakAnimations.SNIFFS, ageInTicks, 0.75f);
        }

        if (kodiak.isRejectingItem()) {
            this.animate(kodiak.rejectingAnimationState, KodiakAnimations.REJECTION, ageInTicks, 1.0f);
        }


        this.animate(kodiak.idleAnimationState, KodiakAnimations.MISC_IDLE, ageInTicks, 1.0f);

        if (kodiak.isRunning() || kodiak.getState() == 2) {
            float animSpeed = kodiak.isVehicle() ? 1.0f : 1.25f;

            this.animateWalk(KodiakAnimations.MOVE_RUN, limbSwing, limbSwingAmount, animSpeed, 1.35f);

            if (walkAnimCrossed(KodiakAnimations.MOVE_RUN, limbSwing, animSpeed, 240L)) kodiak.onRightFootDown();
            if (walkAnimCrossed(KodiakAnimations.MOVE_RUN, limbSwing, animSpeed, 240L)) kodiak.onLeftFootDown();
            if (walkAnimCrossed(KodiakAnimations.MOVE_RUN, limbSwing, animSpeed, 920L)) kodiak.onLeftFootDown();
            if (walkAnimCrossed(KodiakAnimations.MOVE_RUN, limbSwing, animSpeed, 920L)) kodiak.onRightFootDown();
        } else {
            this.animateWalk(KodiakAnimations.MOVE_WALK, limbSwing, limbSwingAmount, 6f, 6f);

            if (walkAnimCrossed(KodiakAnimations.MOVE_WALK, limbSwing, 6f, 550L)) kodiak.onRightFootDown();
            if (walkAnimCrossed(KodiakAnimations.MOVE_WALK, limbSwing, 6f, 1350L)) kodiak.onLeftFootDown();
        }

        this.prevLimbSwing = limbSwing;
        captureBodyState(kodiak, 10f, false, this.ALL2, this.ALL, this.body, this.body_2, this.body_1);
        captureBodyState(kodiak, 12f, true, this.ALL2, this.ALL, this.body, this.body_2);
    }

    /** Renders only the model geometry — no items in mouth. Used by all skin/effect layers. */
    public void renderGeometryOnly(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        renderGeometryOnly(poseStack, vertexConsumer, packedLight, packedOverlay, color);

        if (this.currentEntity != null) {
            if (!this.currentEntity.getFoodPick().isEmpty()) {
                renderItemOnHead(this.currentEntity, poseStack, packedLight);
            }
            if (this.currentEntity.isCatchingSalmon()) {
                renderSalmonInMouth(this.currentEntity, poseStack, packedLight, new Salmon(EntityType.SALMON, currentEntity.level()));
            }
        }
    }

    private float prevLimbSwing = 0f;

    private void captureBodyState(KodiakEntity kodiak, float restPoseYSum, boolean isPassenger, ModelPart... boneChain) {
        if (!kodiak.level().isClientSide()) return;

        float ySum = 0f;
        for (ModelPart bone : boneChain) ySum += bone.y;
        float animY = ySum - (restPoseYSum * kodiak.getScale());

        if (isPassenger) {
            kodiak.bodyAnimY_passenger = animY;
            kodiak.bodyZRot_passenger = (float) Math.toDegrees(this.ALL.zRot + this.body.zRot + this.body_2.zRot);
            kodiak.bodyXRot_passenger = (float) -Math.toDegrees(this.ALL.xRot + this.body.xRot + this.body_2.xRot);
        } else {
            kodiak.setBodyZRot((float) Math.toDegrees(this.ALL.zRot + this.body.zRot + this.body_2.zRot + this.body_1.zRot));
            kodiak.setBodyXRot((float) -Math.toDegrees(this.ALL.xRot + this.body.xRot + this.body_1.xRot + this.body_2.xRot));
            kodiak.bodyZRotCamera = (float) Math.toDegrees(this.body_1.zRot);
            kodiak.bodyXRotCamera = (float) -Math.toDegrees(this.body_1.xRot);
            kodiak.bodyAnimY = animY;
        }
    }

    private MultiBufferSource currentBufferSource;

    public void setBufferSource(MultiBufferSource bufferSource) {
        this.currentBufferSource = bufferSource;
    }

    private void renderItemOnHead(KodiakEntity kodiak, PoseStack poseStack, int packedLight) {
        MultiBufferSource bufferSource = this.currentBufferSource;
        if (bufferSource == null) return;

        poseStack.pushPose();

        this.ALL2.translateAndRotate(poseStack);
        this.ALL.translateAndRotate(poseStack);
        this.body.translateAndRotate(poseStack);
        this.body_2.translateAndRotate(poseStack);
        this.body_1.translateAndRotate(poseStack);
        this.head.translateAndRotate(poseStack);

        poseStack.translate(0.0D, 0.35D, -1D);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        poseStack.scale(1.2f, 1.2f, 1.2f);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                kodiak.getFoodPick(),
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                kodiak.level(),
                0
        );

        poseStack.popPose();
    }

    private void renderSalmonInMouth(KodiakEntity kodiak, PoseStack poseStack, int packedLight, LivingEntity livingEntity) {
        MultiBufferSource bufferSource = this.currentBufferSource;
        if (bufferSource == null) return;

        LivingEntity fakeEntity = (LivingEntity) livingEntity.getType().create(kodiak.level());
        if (fakeEntity == null) return;

        poseStack.pushPose();

        this.head.translateAndRotate(poseStack);

        poseStack.translate(0.0D, 0.75f, -1.05D);
        poseStack.mulPose(Axis.YP.rotationDegrees(90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        poseStack.scale(0.8f, 0.8f, 0.8f);

        fakeEntity.yBodyRot = 0;
        fakeEntity.yBodyRotO = 0;
        fakeEntity.yHeadRot = 0;
        fakeEntity.yHeadRotO = 0;
        fakeEntity.setXRot(0);
        fakeEntity.xRotO = 0;
        fakeEntity.setYRot(0);
        fakeEntity.yRotO = 0;
        fakeEntity.setAirSupply(fakeEntity.getMaxAirSupply());
        fakeEntity.tickCount = kodiak.tickCount;

        var entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        var salmonRenderer = entityRenderDispatcher.getRenderer(fakeEntity);

        salmonRenderer.render(fakeEntity, 0, 1.0f, poseStack, bufferSource, packedLight);

        poseStack.popPose();
    }

    /**
     * Returns {@code true} on the <em>exact frame</em> a looping walk animation crosses a keyframe time.
     * Use this to fire one-shot events (sounds, particles) at precise moments of a walk cycle.
     *
     * <p>How to find {@code triggerTimeMs}: open Blockbench, look at the keyframe timestamp of the
     * moment you want (e.g. foot hits the ground), multiply seconds × 1000.</p>
     *
     * @param animation     the AnimationDefinition played via {@code animateWalk()}
     * @param limbSwing     current limbSwing value (first param of setupAnim)
     * @param speedScale    the {@code maxAnimationCycles} value passed to {@code animateWalk()}
     * @param triggerTimeMs keyframe time in <strong>milliseconds</strong> to fire at
     */
    private boolean walkAnimCrossed(AnimationDefinition animation, float limbSwing, float speedScale, long triggerTimeMs) {
        long durationMs = (long) (animation.lengthInSeconds() * 1000f);
        if (durationMs <= 0) return false;

        long cur = ((long) (limbSwing * 50f * speedScale)) % durationMs;
        long prev = ((long) (prevLimbSwing * 50f * speedScale)) % durationMs;

        // Normal case: no wrap-around in this frame
        if (prev <= cur) return prev < triggerTimeMs && cur >= triggerTimeMs;
        // Wrap-around: the cycle looped between prev and cur → target in [0, cur] OR in (prev, duration)
        return triggerTimeMs <= cur || triggerTimeMs > prev;
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

    /**
     * Copie la pose résolue (translation + rotation + échelle) de chaque os depuis un autre
     * KodiakModel déjà animé (le modèle de base). Utilisé par les skins en mode OVERLAY pour
     * que le modèle d'overlay suive parfaitement la base sans désynchronisation ni z-fighting.
     */
    public void copyPoseFrom(KodiakModel<?> src) {
        this.ALL2.copyFrom(src.ALL2);
        this.ALL.copyFrom(src.ALL);
        this.right_leg.copyFrom(src.right_leg);
        this.left_leg.copyFrom(src.left_leg);
        this.body.copyFrom(src.body);
        this.body_2.copyFrom(src.body_2);
        this.body_1.copyFrom(src.body_1);
        this.head.copyFrom(src.head);
        this.left_ear.copyFrom(src.left_ear);
        this.right_ear.copyFrom(src.right_ear);
        this.left_eyeBall.copyFrom(src.left_eyeBall);
        this.right_eyeBall.copyFrom(src.right_eyeBall);
        this.muzzle.copyFrom(src.muzzle);
        this.left_arm.copyFrom(src.left_arm);
        this.right_arm.copyFrom(src.right_arm);
    }
}
