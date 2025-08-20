package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.animation.ElephantAnimations;
import net.tiew.operationWild.entity.client.animation.TigerAnimations;
import net.tiew.operationWild.entity.custom.living.TigerEntity;

public class TigerModel<T extends TigerEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "tiger_default"), "main");
    private final ModelPart ALL2;
    private final ModelPart ALL;
    private final ModelPart right_arm;
    private final ModelPart right_leg;
    private final ModelPart left_leg;
    private final ModelPart left_arm;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart left_ear;
    private final ModelPart right_ear;
    private final ModelPart left_eyeBall;
    private final ModelPart right_eyeBall;
    private final ModelPart tail;
    private final ModelPart front;
    private final ModelPart back;

    public TigerModel(ModelPart root) {
        this.ALL2 = root.getChild("ALL2");
        this.ALL = this.ALL2.getChild("ALL");
        this.right_arm = this.ALL.getChild("right_arm");
        this.right_leg = this.ALL.getChild("right_leg");
        this.left_leg = this.ALL.getChild("left_leg");
        this.left_arm = this.ALL.getChild("left_arm");
        this.body = this.ALL.getChild("body");
        this.head = this.body.getChild("head");
        this.left_ear = this.head.getChild("left_ear");
        this.right_ear = this.head.getChild("right_ear");
        this.left_eyeBall = this.head.getChild("left_eyeBall");
        this.right_eyeBall = this.head.getChild("right_eyeBall");
        this.tail = this.body.getChild("tail");
        this.front = this.tail.getChild("front");
        this.back = this.tail.getChild("back");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 9.0F, 1.0F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition right_arm = ALL.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(50, 14).mirror().addBox(-2.0F, 1.0F, -3.0F, 5.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(60, 35).mirror().addBox(0.0F, 5.0F, -6.0F, 1.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(51, 30).mirror().addBox(0.0F, 3.0F, -11.0F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(69, 0).mirror().addBox(-2.5F, 5.0F, -3.5F, 6.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(70, 0).mirror().addBox(-0.5F, 4.5F, -3.6F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-5.0F, 6.0F, -7.0F));

        PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(50, 14).mirror().addBox(-2.0F, 0.0F, -3.0F, 5.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-4.0F, 7.0F, 8.0F));

        PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(50, 14).addBox(-3.0F, 0.0F, -3.0F, 5.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 7.0F, 8.0F));

        PartDefinition left_arm = ALL.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(50, 14).addBox(-3.0F, 1.0F, -3.0F, 5.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 6.0F, -7.0F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 25).addBox(-7.0F, -6.0F, -12.0F, 14.0F, 13.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(0, 25).addBox(-7.0F, -6.0F, -12.0F, 14.0F, 13.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(45, 143).addBox(-5.0F, -7.0F, -11.0F, 10.0F, 1.0F, 9.0F, new CubeDeformation(0.5F))
                .texOffs(0, 104).addBox(-7.0F, -6.0F, -12.0F, 14.0F, 13.0F, 11.0F, new CubeDeformation(0.5F))
                .texOffs(106, -5).mirror().addBox(-7.0F, 7.0F, -12.0F, 0.0F, 1.0F, 11.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(0, 0).addBox(-6.0F, -5.0F, -1.0F, 12.0F, 12.0F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(175, 153).addBox(-6.0F, -5.0F, -1.0F, 12.0F, 12.0F, 13.0F, new CubeDeformation(0.5F))
                .texOffs(102, -13).addBox(6.0F, 7.0F, -1.0F, 0.0F, 1.0F, 13.0F, new CubeDeformation(0.01F))
                .texOffs(102, -13).mirror().addBox(-6.0F, 7.0F, -1.0F, 0.0F, 1.0F, 13.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(106, -5).addBox(7.0F, 7.0F, -12.0F, 0.0F, 1.0F, 11.0F, new CubeDeformation(0.01F))
                .texOffs(86, 86).addBox(-6.0F, -5.0F, -12.7F, 12.0F, 9.0F, 1.0F, new CubeDeformation(0.25F))
                .texOffs(82, 109).addBox(-8.0F, -7.0F, -12.2F, 16.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 49).addBox(-6.0F, -4.0F, -6.0F, 12.0F, 9.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(182, 191).addBox(-6.0F, -4.0F, -6.0F, 12.0F, 9.0F, 7.0F, new CubeDeformation(0.25F))
                .texOffs(0, 209).addBox(-7.0F, -7.0F, -7.0F, 14.0F, 12.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(5, 185).mirror().addBox(4.0F, -9.0F, -2.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(5, 185).addBox(-7.0F, -9.0F, -2.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(42, 69).addBox(-4.0F, -6.0F, -6.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(22, 80).addBox(-3.5F, -13.0F, -5.5F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 105).addBox(2.0F, -2.5F, -7.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(58, 87).addBox(-3.5F, -15.0F, -6.0F, 7.0F, 11.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(85, 17).addBox(-5.0F, -2.5F, -7.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(85, 17).addBox(1.0F, -2.5F, -7.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(85, 17).addBox(-5.0F, -2.5F, -7.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(83, 13).addBox(-6.0F, -3.5F, -7.0F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(92, 18).mirror().addBox(-6.5F, -3.5F, -6.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(92, 18).addBox(5.5F, -3.5F, -6.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 65).addBox(-3.0F, -1.0F, -11.0F, 6.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(2, 142).addBox(-7.0F, 2.0F, -10.0F, 14.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(6, 135).mirror().addBox(-7.0F, -8.0F, -10.0F, 0.0F, 11.0F, 16.0F, new CubeDeformation(0.1F)).mirror(false)
                .texOffs(6, 135).addBox(7.0F, -8.0F, -10.0F, 0.0F, 11.0F, 16.0F, new CubeDeformation(0.1F))
                .texOffs(2, 172).mirror().addBox(-5.6F, 2.0F, -10.5F, 11.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(123, 189).addBox(-3.0F, -1.0F, -11.0F, 6.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(147, 192).addBox(-3.0F, 5.0F, -11.0F, 6.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(123, 206).addBox(-3.0F, 2.0F, -11.0F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(104, 207).addBox(2.0F, 2.0F, -11.0F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(113, 183).addBox(-3.0F, 2.0F, -9.025F, 6.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(80, 73).addBox(3.0F, -3.0F, -11.0F, 5.0F, 6.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(80, 73).mirror().addBox(-8.0F, -3.0F, -11.0F, 5.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -1.0F, -13.0F));

        PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(25, 95).addBox(-4.0F, -1.0F, -19.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.5F, 3.5F, -21.0F, 0.0F, -2.3562F, 0.0F));

        PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(1, 83).mirror().addBox(-6.5F, -5.0F, -6.0F, 0.0F, 5.0F, 7.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(-1.25F, 6.0F, 1.3F, -0.2052F, -0.2273F, 0.1916F));

        PartDefinition cube_r3 = head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(1, 83).addBox(6.5F, -5.0F, -6.0F, 0.0F, 5.0F, 7.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(1.25F, 6.0F, 1.3F, -0.2052F, 0.2273F, -0.1916F));

        PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(50, 42).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(4.5F, -4.0F, -0.5F));

        PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(50, 45).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.5F, -4.0F, -0.5F));

        PartDefinition left_eyeBall = head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(32, 77).addBox(-1.0F, -0.5F, -0.025F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offset(3.0F, -2.5F, -6.0F));

        PartDefinition right_eyeBall = head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(32, 77).mirror().addBox(-1.0F, -0.5F, -0.025F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offset(-3.0F, -2.5F, -6.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 12.0F));

        PartDefinition front = tail.addOrReplaceChild("front", CubeListBuilder.create().texOffs(38, 49).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition back = tail.addOrReplaceChild("back", CubeListBuilder.create().texOffs(76, 45).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 11.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(TigerEntity tiger, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        if (tiger.isBaby()) {
            float maturationPercent = (float) tiger.getMaturationPercentage() / 100f;
            float headScale = 1.6f - (1.6f - 1.0f) * maturationPercent;

            this.head.xScale *= headScale;
            this.head.yScale *= headScale;
            this.head.zScale *= headScale;
        }
        if (tiger.getMaturationPercentage() < 60 && tiger.getMaturationPercentage() > 0) {
            this.left_eyeBall.xScale = 0;
            this.left_eyeBall.yScale = 0;
            this.left_eyeBall.zScale = 0;
            this.right_eyeBall.xScale = 0;
            this.right_eyeBall.yScale = 0;
            this.right_eyeBall.zScale = 0;
        }

        this.applyHeadRotation(netHeadYaw, headPitch);

        if (tiger.isCombo(1)) {
            this.animate(tiger.attack1Combo, TigerAnimations.ATTACK_STRIKE, ageInTicks, 1.0f);
        }
        if (tiger.isCombo(2)) {
            this.animate(tiger.attack2Combo, TigerAnimations.ATTACK_STRIKE2, ageInTicks, 0.8f);
        }
        if (tiger.isCombo(3)) {
            this.animate(tiger.attack3Combo, TigerAnimations.ATTACK_STRIKE3, ageInTicks, 0.55f);
        }






        if (tiger.transitionIdleSit.isStarted()) {
            this.animate(tiger.transitionIdleSit, TigerAnimations.TRANSITION_IDLE_SIT, ageInTicks, 1.0f);
            return;
        }

        if (tiger.transitionSitIdle.isStarted()) {
            this.animate(tiger.transitionSitIdle, TigerAnimations.TRANSITION_SIT_IDLE, ageInTicks, 1.0f);
            return;
        }

        if (tiger.isTameJumping()) {
            this.animate(tiger.jumpAnimationState, TigerAnimations.JUMP, ageInTicks, 1.0f);
            return;
        }

        if (tiger.isPreparingNapping()) {
            this.animate(tiger.preparingToNapAnimationState, TigerAnimations.NAP_TRANSITION, ageInTicks, 1.0f);
            return;
        }

        if (tiger.isNapping()) {
            this.animate(tiger.napAnimationState, TigerAnimations.NAP, ageInTicks, 1.0f);
            return;
        }

        if (tiger.isSitting()) {
            this.animate(tiger.sittingAnimationState, TigerAnimations.SIT, ageInTicks, 1.0f);
            return;
        }

        if (tiger.isFalling() && !tiger.isCombo()) this.animate(tiger.jumpAnimationState, TigerAnimations.JUMP, ageInTicks, 1.0f);

        if (tiger.isSleeping()) {
            this.animate(tiger.sleepingAnimationState, TigerAnimations.SLEEP, ageInTicks, 1.0f);
            return;
        }

        if (tiger.isJumpingOnTarget() || tiger.isTameJumping()) {
            this.animate(tiger.jumpAnimationState, TigerAnimations.JUMP, ageInTicks, 1.0f);
        } else {
            if (tiger.isTrappingEntity()) {
                this.animate(tiger.trappingAnimationState, TigerAnimations.TRAPPING, ageInTicks, 1.0f);
            } else {
                float runSpeed = tiger.isVehicle() ? 1.75f : 2.5f;

                if ((tiger.isVehicle() && tiger.getControllingPassenger() instanceof Player player && player.zza > 0 && tiger.isRunning())) {
                    this.animateWalk(TigerAnimations.MOVE_RUN, limbSwing, limbSwingAmount, tiger.isVehicle() ? 0.75f : 1f, runSpeed);
                    return;
                }


                if ((tiger.getSleepBarPercent() >= 75 || tiger.getState() == 2 || tiger.isRunning()) && !tiger.isBaby()) {
                    this.animateWalk(TigerAnimations.MOVE_RUN, limbSwing, limbSwingAmount, tiger.isVehicle() ? 0.75f : 1f, runSpeed);
                } else {
                    this.animateWalk(TigerAnimations.MOVE_WALK, limbSwing, limbSwingAmount, 3f, 2.5f);
                }

                this.animate(tiger.idleAnimationState, TigerAnimations.IDLE, ageInTicks, 1.0f);
            }
        }

        if (tiger.level().isClientSide()) {
            tiger.setBodyZRot((float) Math.toDegrees(this.body.zRot) / 2);
            tiger.setBodyXRot((float) Math.toDegrees(this.body.xRot) / 2);
        }
    }

    private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch) {
        pNetHeadYaw = Mth.clamp(pNetHeadYaw, -30.0F, 30.0F);
        pHeadPitch = Mth.clamp(pHeadPitch, -30.0F, 30.0F);

        this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root() {
        return ALL2;
    }
}
