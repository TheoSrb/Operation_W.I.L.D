package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.client.animation.ElephantAnimations;
import net.tiew.operationWild.entity.variants.ElephantVariant;
import net.tiew.operationWild.event.ClientEvents;

public class ElephantModel<T extends ElephantEntity> extends OWComboModel<T> implements OWFlagModel {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "elephant_default"), "main");

    private static final ResourceLocation FLAG_POLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/elephant_flag.png");

    private static final Anchor FLAG_ANCHOR = new Anchor(-5.75f, 11f, 0.5f, 19f);

    private static final float REST_POSE_Y_SUM = -9f;

    private static final org.joml.Vector3f WALK_ANIM_VECTOR = new org.joml.Vector3f();

    public float externalChargeHeadPitch = 0f;

    public double externalWalkTimeMs = 0;

    public float externalBankRoll = 0f;


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
    private final ModelPart trunk;
    private final ModelPart trunk2;
    private final ModelPart left_eyeBall;
    private final ModelPart right_eyeBall;
    private final ModelPart tail;
    private final ModelPart tail2;
    private final ModelPart demon_left_wing;
    private final ModelPart demon_right_wing;
    private final ModelPart mainFlag;
    private final ModelPart flag;

    public ElephantModel(ModelPart root) {
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
        this.trunk = this.head.getChild("trunk");
        this.trunk2 = this.trunk.getChild("trunk2");
        this.left_eyeBall = this.head.getChild("left_eyeBall");
        this.right_eyeBall = this.head.getChild("right_eyeBall");
        this.tail = this.body.getChild("tail");
        this.tail2 = this.tail.getChild("tail2");
        this.demon_left_wing = this.body.getChild("demon_left_wing");
        this.demon_right_wing = this.body.getChild("demon_right_wing");

        this.mainFlag = this.body.hasChild("mainFlag") ? this.body.getChild("mainFlag") : null;
        this.flag = this.mainFlag != null ? this.mainFlag.getChild("flag") : null;
        if (this.mainFlag != null) this.mainFlag.visible = false;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, -7.0F, 2.0F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition right_arm = ALL.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(60, 70).mirror().addBox(-4.5F, 0.0F, -6.0F, 9.0F, 20.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-6.0F, 11.0F, -14.0F));

        PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(60, 70).mirror().addBox(-4.5F, 0.0F, -6.0F, 9.0F, 20.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-8.0F, 11.0F, 14.0F));

        PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(60, 70).addBox(-4.5F, 0.0F, -6.0F, 9.0F, 20.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 11.0F, 14.0F));

        PartDefinition left_arm = ALL.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(60, 70).addBox(-4.5F, 0.0F, -6.0F, 9.0F, 20.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 11.0F, -14.0F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-13.5F, -13.0F, -21.0F, 27.0F, 26.0F, 44.0F, new CubeDeformation(0.0F))
        .texOffs(114, 186).addBox(-13.5F, -13.0F, -21.0F, 27.0F, 26.0F, 44.0F, new CubeDeformation(0.5F))
        .texOffs(242, 108).addBox(9.5F, -43.0F, -15.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(0, 220).addBox(-11.5F, -45.0F, -15.0F, 23.0F, 2.0F, 34.0F, new CubeDeformation(0.0F))
        .texOffs(121, 132).addBox(-13.5F, -47.0F, -18.0F, 27.0F, 2.0F, 40.0F, new CubeDeformation(0.0F))
        .texOffs(160, 111).addBox(-13.5F, -45.0F, 22.0F, 27.0F, 11.0F, 0.0F, new CubeDeformation(0.0F))
        .texOffs(160, 111).addBox(-13.5F, -45.0F, -18.0F, 27.0F, 11.0F, 0.0F, new CubeDeformation(0.0F))
        .texOffs(242, 108).mirror().addBox(-11.5F, -43.0F, -15.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
        .texOffs(242, 108).mirror().addBox(-11.5F, -43.0F, 17.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
        .texOffs(242, 108).addBox(9.5F, -43.0F, 17.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(176, 0).addBox(-12.5F, -15.0F, -6.0F, 25.0F, 2.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, -1.0F));

        PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(160, 96).mirror().addBox(-11.0F, 13.0F, -28.0F, 27.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-14.5F, -58.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r2 = body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(160, 96).addBox(-16.0F, 13.0F, -28.0F, 27.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.5F, -58.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition cube_r3 = body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(222, 47).mirror().addBox(0.0F, -5.0102F, -3.29F, 0.0F, 23.0F, 8.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(0.6F, 7.75F, -22.25F, 0.0F, 1.5708F, -1.8326F));

        PartDefinition cube_r4 = body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(204, 41).mirror().addBox(0.1881F, -11.6264F, -0.2154F, 0.0F, 14.0F, 8.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(16.65F, -1.0F, -22.25F, -0.0918F, -0.3457F, 0.0316F));

        PartDefinition cube_r5 = body.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(120, -11).mirror().addBox(0.1158F, -8.8537F, -5.3647F, 0.0F, 9.0F, 30.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(15.7F, -11.0F, -18.3F, 0.3752F, -0.3683F, -0.5972F));

        PartDefinition cube_r6 = body.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(222, 47).addBox(0.0F, -5.0102F, -3.29F, 0.0F, 23.0F, 8.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-0.6F, 7.75F, -22.25F, 0.0F, -1.5708F, 1.8326F));

        PartDefinition cube_r7 = body.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(204, 41).addBox(-0.1881F, -11.6264F, -0.2154F, 0.0F, 14.0F, 8.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-16.65F, -1.0F, -22.25F, -0.0918F, 0.3457F, -0.0316F));

        PartDefinition cube_r8 = body.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(120, -11).addBox(-0.1158F, -8.8537F, -5.3647F, 0.0F, 9.0F, 30.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-15.7F, -11.0F, -18.3F, 0.3752F, 0.3683F, 0.5972F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 70).addBox(-7.5F, -9.0F, -15.0F, 15.0F, 18.0F, 15.0F, new CubeDeformation(0.0F))
        .texOffs(52, 108).addBox(-7.5F, -7.0F, -0.5F, 15.0F, 17.0F, 1.0F, new CubeDeformation(0.4F))
        .texOffs(98, 108).addBox(-7.5F, -12.9F, 0.0F, 15.0F, 6.0F, 0.0F, new CubeDeformation(0.05F))
        .texOffs(98, 114).addBox(-7.5F, 10.5F, -0.1F, 15.0F, 6.0F, 0.0F, new CubeDeformation(0.05F))
        .texOffs(99, 127).addBox(-13.4F, -9.0F, -0.1F, 6.0F, 20.0F, 0.0F, new CubeDeformation(0.05F))
        .texOffs(99, 127).mirror().addBox(7.4F, -9.0F, -0.1F, 6.0F, 20.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false)
        .texOffs(142, 37).addBox(-7.5F, -9.0F, -15.0F, 15.0F, 18.0F, 15.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, -7.0F, -21.0F));

        PartDefinition cube_r9 = head.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(140, 83).mirror().addBox(-9.0F, -5.0F, -9.0F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
        .texOffs(116, 132).mirror().addBox(-8.0F, 17.0F, -17.0F, 3.0F, 3.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false)
        .texOffs(140, 110).mirror().addBox(-8.0F, 3.0F, -8.0F, 3.0F, 17.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 12.0F, -9.0F, -0.3442F, 0.0594F, 0.1642F));

        PartDefinition cube_r10 = head.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(223, 211).mirror().addBox(-8.0F, 3.0F, -8.0F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
        .texOffs(223, 184).mirror().addBox(-9.0F, -5.0F, -9.0F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
        .texOffs(61, 163).mirror().addBox(-8.0F, 11.0F, -17.0F, 3.0F, 3.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.0F, 11.0F, -6.0F, -0.2898F, 0.1975F, 0.582F));

        PartDefinition cube_r11 = head.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(61, 163).addBox(5.0F, 11.0F, -17.0F, 3.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
        .texOffs(223, 211).addBox(5.0F, 3.0F, -8.0F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.0F))
        .texOffs(223, 184).addBox(4.0F, -5.0F, -9.0F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 11.0F, -6.0F, -0.2898F, -0.1975F, -0.582F));

        PartDefinition cube_r12 = head.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(116, 132).addBox(5.0F, 17.0F, -17.0F, 3.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
        .texOffs(140, 110).addBox(5.0F, 3.0F, -8.0F, 3.0F, 17.0F, 3.0F, new CubeDeformation(0.0F))
        .texOffs(140, 83).addBox(4.0F, -5.0F, -9.0F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 12.0F, -9.0F, -0.3442F, -0.0594F, -0.1642F));

        PartDefinition cube_r13 = head.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(144, 187).addBox(6.0F, -10.5F, -1.5F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5F, -5.5F, -11.5F, 0.1666F, -0.0859F, -0.3564F));

        PartDefinition cube_r14 = head.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(144, 187).mirror().addBox(-9.0F, -10.5F, -1.5F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.5F, -5.5F, -11.5F, 0.1666F, 0.0859F, 0.3564F));

        PartDefinition cube_r15 = head.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(24, 166).mirror().addBox(-9.0F, -1.5F, -1.5F, 9.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-6.5F, -7.5F, -11.5F, 0.139F, 0.126F, 0.0962F));

        PartDefinition cube_r16 = head.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(24, 166).addBox(0.0F, -1.5F, -1.5F, 9.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.5F, -7.5F, -11.5F, 0.139F, -0.126F, -0.0962F));

        PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(0, 129).addBox(-1.0F, -12.0F, -1.0F, 19.0F, 24.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(7.5F, -1.0F, -7.0F));

        PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(0, 103).mirror().addBox(-18.0F, -12.0F, -1.0F, 19.0F, 24.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-7.5F, -1.0F, -7.0F));

        PartDefinition trunk = head.addOrReplaceChild("trunk", CubeListBuilder.create().texOffs(42, 132).addBox(-3.5F, -3.0F, -4.0F, 7.0F, 16.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, -16.0F));

        PartDefinition trunk2 = trunk.addOrReplaceChild("trunk2", CubeListBuilder.create().texOffs(70, 132).addBox(-2.5F, 0.0F, -3.0F, 5.0F, 16.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 13.0F, 0.0F));

        PartDefinition left_eyeBall = head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(44, 105).addBox(-2.5F, -0.5F, 0.0F, 5.0F, 1.0F, 0.0F, new CubeDeformation(0.1F)), PartPose.offset(5.0F, -1.5F, -15.0F));

        PartDefinition right_eyeBall = head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(44, 105).mirror().addBox(-2.5F, -0.5F, 0.0F, 5.0F, 1.0F, 0.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offset(-5.0F, -1.5F, -15.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 185).addBox(-1.5F, -1.0F, 0.05F, 3.0F, 22.0F, 0.0F, new CubeDeformation(0.1F))
        .texOffs(0, 182).addBox(0.05F, -1.0F, -1.5F, 0.0F, 22.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, 23.0F, 0.0873F, 0.0F, 0.0F));

        PartDefinition tail2 = tail.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(45, 190).addBox(-3.5F, -1.0F, 0.05F, 7.0F, 28.0F, 0.0F, new CubeDeformation(0.1F))
        .texOffs(45, 183).addBox(0.05F, -1.0F, -3.5F, 0.0F, 28.0F, 7.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 15.0F, 0.0F));

        PartDefinition demon_left_wing = body.addOrReplaceChild("demon_left_wing", CubeListBuilder.create().texOffs(44, 191).addBox(-30.0F, 0.0F, -16.0F, 30.0F, 0.0F, 36.0F, new CubeDeformation(0.01F)), PartPose.offset(-13.5F, -8.0F, -10.0F));

        PartDefinition demon_right_wing = body.addOrReplaceChild("demon_right_wing", CubeListBuilder.create().texOffs(44, 191).mirror().addBox(0.0F, 0.0F, -16.0F, 30.0F, 0.0F, 36.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(13.5F, -8.0F, -10.0F));

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
                .texOffs(0, 0).addBox(-1.0F, -20.75F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, -47.0F, 12.0F));

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
            case 1 -> ElephantAnimations.ATTACK_STRIKE;
            case 2 -> ElephantAnimations.ATTACK_STRIKE_2;
            case 3 -> ElephantAnimations.ATTACK_STRIKE_3;
            default -> null;
        };
    }

    private static final float COMBO_ANIMATION_SPEED = 1.0f;

    @Override
    protected float comboSpeed(int index) {
        return switch (index) {
            case 1 -> 1.0f;
            case 2 -> 1.05f;
            case 3 -> 1.30f;
            default -> 1.0f;
        };
    }

    @Override
    public void setupAnim(ElephantEntity elephant, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        boolean demon = elephant.getVariant() == ElephantVariant.Cosmetics.DEMON.variant;
        this.demon_left_wing.visible = demon;
        this.demon_right_wing.visible = demon;

        if (elephant.isBaby()) {
            float maturationPercent = (float) elephant.getMaturationPercentage() / 100f;
            float headScale = 1.6f - (1.6f - 1.0f) * maturationPercent;

            this.head.xScale *= headScale;
            this.head.yScale *= headScale;
            this.head.zScale *= headScale;
        }
        this.applyHeadRotation(netHeadYaw, headPitch);

        if (Math.abs(externalBankRoll) > 0.01f) {
            this.body.zRot += (float) Math.toRadians(externalBankRoll);
        }

        animateCombos(elephant, ageInTicks, COMBO_ANIMATION_SPEED);
        captureBodyState(elephant, REST_POSE_Y_SUM, this.ALL2, this.ALL, this.body);

        double walkTimeMs = externalWalkTimeMs;

        if (elephant.isMad()) {
            this.left_eyeBall.xScale = 0;
            this.left_eyeBall.yScale = 0;
            this.left_eyeBall.zScale = 0;

            this.right_eyeBall.xScale = 0;
            this.right_eyeBall.yScale = 0;
            this.right_eyeBall.zScale = 0;
        }

        if (elephant.isEarthquakeGesture()) {
            this.animate(elephant.earthquakeAnimationState, ElephantAnimations.EARTHQUAKE, ageInTicks, 1.0f);
            captureBodyState(elephant, REST_POSE_Y_SUM, this.ALL2, this.ALL, this.body);
            return;
        }

        if (elephant.transitionSitIdle.isStarted()) {
            this.animate(elephant.transitionSitIdle, ElephantAnimations.TRANSITION_SIT_IDLE, ageInTicks, 1.0f);
            captureBodyState(elephant, REST_POSE_Y_SUM, this.ALL2, this.ALL, this.body);
            return;
        }

        if (elephant.transitionIdleSit.isStarted()) {
            this.animate(elephant.transitionIdleSit, ElephantAnimations.TRANSITION_IDLE_SIT, ageInTicks, 1.0f);
            captureBodyState(elephant, REST_POSE_Y_SUM, this.ALL2, this.ALL, this.body);
            return;
        }

        if (elephant.isNapping() || elephant.isSleeping() || elephant.isSitting()) {
            this.animate(elephant.sittingAnimationState, ElephantAnimations.SIT, ageInTicks, 1.0f);
            captureBodyState(elephant, REST_POSE_Y_SUM, this.ALL2, this.ALL, this.body);
            return;
        }

        this.animate(elephant.idleAnimationState, ElephantAnimations.MISC_IDLE, ageInTicks, 1.0f);

        KeyframeAnimations.animate(this, ElephantAnimations.MOVE_WALK, (long) walkTimeMs,
                Math.min(limbSwingAmount * 7.5f, 1.0f), WALK_ANIM_VECTOR);

        applyShoulderBash(elephant, ageInTicks);
        applyTrunkAim(elephant, ageInTicks);

        if (externalChargeHeadPitch > 0.01f) {
            this.head.xRot += (float) Math.toRadians(externalChargeHeadPitch);
        }

        captureBodyState(elephant, REST_POSE_Y_SUM, this.ALL2, this.ALL, this.body);
    }

    private static final float HALF_PI = (float) (Math.PI / 2.0);

    /**
     * Temps écoulé du coup d'épaule, en ticks fractionnaires.
     *
     * <p>Le minuteur est une donnée synchronisée : il ne bouge qu'une fois par tick, et le geste
     * saccadait à vingt images par seconde. On lui rend la partie fractionnaire du tick en cours.</p>
     */
    private static float shoulderBashTime(ElephantEntity elephant, float ageInTicks) {
        float partial = Mth.clamp(ageInTicks - elephant.tickCount, 0f, 1f);
        int elapsed = elephant.clientBashElapsed >= 0
                ? elephant.clientBashElapsed
                : OWAttacksConstants.Elephant.SHOULDER_BASH_DURATION_TICKS - elephant.getShoulderBashTimer();
        return elapsed + partial;
    }

    /** Rampe en S : pente nulle aux deux extrémités, donc aucun raccord anguleux entre phases. */
    private static float smoothStep(float t) {
        float u = Mth.clamp(t, 0f, 1f);
        return u * u * (3f - 2f * u);
    }

    /** Repli d'anticipation [0..1] : la bête se ramasse du côté opposé avant de se jeter. */
    private static float shoulderBashCoil(float t) {
        int windup = OWAttacksConstants.Elephant.SHOULDER_BASH_WINDUP_TICKS;
        if (t < windup) return smoothStep(t / windup);
        float p = (t - windup) / OWAttacksConstants.Elephant.SHOULDER_BASH_DASH_TICKS;
        return 1f - smoothStep(p / 0.30f);
    }

    /**
     * Engagement de l'épaule [0..1], puis retour élastique.
     *
     * <p>La réception dépasse la verticale avant de se replacer — c'est ce contre-mouvement qui
     * donne son poids à la masse. Une simple rampe descendante rendait l'arrêt caoutchouteux.</p>
     */
    private static float shoulderBashThrust(float t) {
        int windup = OWAttacksConstants.Elephant.SHOULDER_BASH_WINDUP_TICKS;
        int dash = OWAttacksConstants.Elephant.SHOULDER_BASH_DASH_TICKS;
        if (t < windup) return 0f;
        if (t < windup + dash) {
            float p = (t - windup) / dash;
            return p < 0.35f ? smoothStep(p / 0.35f) : 1f;
        }
        float p = Mth.clamp((t - windup - dash) / OWAttacksConstants.Elephant.SHOULDER_BASH_RECOVER_TICKS, 0f, 1f);
        // Amorti en S plutôt qu'en carré : la valeur ET sa pente valent zéro à l'arrivée, si bien
        // que le retour au repos ne se termine pas par une saccade.
        float damping = 1f - smoothStep(p);
        return damping * (float) Math.cos(p * Math.PI * 1.5);
    }

    /**
     * Coup d'épaule : le geste est dans le <b>déplacement</b>, pas dans la pose.
     *
     * <p>L'éléphant se déporte réellement de deux ou trois blocs sur le côté, et c'est cela qui doit
     * se lire. Une bascule marquée du buste avait été tentée : basculer une masse pareille d'un
     * quart de tour ne fait pas « puissant », ça fait cassé. Il ne reste donc qu'une inclinaison de
     * quelques degrés et un regard qui accompagne le côté visé — juste de quoi ne pas glisser
     * latéralement comme un bloc de pierre.</p>
     */
    private void applyShoulderBash(ElephantEntity elephant, float ageInTicks) {
        if (elephant.getShoulderBashTimer() <= 0) return;

        float t = shoulderBashTime(elephant, ageInTicks);
        float coil = shoulderBashCoil(t);
        float thrust = shoulderBashThrust(t);
        float side = elephant.getShoulderBashSide();

        float lean = (thrust * 0.24f - coil * 0.11f) * side;

        this.ALL.zRot += lean;
        // Le buste s'affaisse à l'impact puis se relève : c'est ce coup de rein qui donne la
        // brutalité, bien mieux qu'une bascule prononcée — celle-ci ne faisait que casser la bête.
        this.ALL.y += thrust * 1.6f - coil * 0.9f;

        this.body.zRot += lean * 0.35f;
        this.head.yRot += (thrust * 0.30f - coil * 0.20f) * side;
        this.head.zRot -= lean * 0.45f;
        this.trunk.zRot += lean * 2.0f;
        this.trunk2.zRot += lean * 1.4f;
        this.left_ear.yRot += thrust * 0.35f;
        this.right_ear.yRot -= thrust * 0.35f;
        this.tail.zRot -= lean * 1.5f;
    }

    /**
     * Pose de trompe pilotée à la visée, superposée aux animations clés.
     *
     * <p>Le poids monte et descend progressivement ({@code trunkAimWeight}), de sorte que la trompe
     * quitte et retrouve son animation de repos sans à-coup quand on change de carte secondaire.
     * Le second segment traîne sur le premier : c'est ce retard qui fait le fouet plutôt que la
     * barre rigide.</p>
     */
    private void applyTrunkAim(ElephantEntity elephant, float ageInTicks) {
        float partial = Mth.clamp(ageInTicks - elephant.tickCount, 0f, 1f);
        float weight = elephant.getTrunkAimWeight(partial);
        if (weight <= 0.01f) return;

        // L'encolure descend AVANT que la trompe ne se calcule : celle-ci retranche ensuite la
        // rotation de la tête, si bien qu'elle continue de pointer où il faut tout en partant de
        // plus bas. C'est ce gain d'allonge qui lui fait atteindre une eau trop profonde pour elle.
        float dip = (float) Math.toRadians(elephant.getTrunkHeadDip(partial));
        if (Math.abs(dip) > 0.001f) this.head.xRot += dip;

        float yaw = (float) Math.toRadians(elephant.getTrunkAimYaw(partial));
        float pitch = (float) Math.toRadians(elephant.getTrunkAimPitch(partial));

        // La trompe PEND au repos : son orientation naturelle est déjà un quart de tour sous
        // l'horizontale. Sans retrancher ce quart de tour, viser droit devant obligeait à lever les
        // yeux au ciel. On retranche aussi la rotation de la tête, dont la trompe est fille, pour
        // que l'angle final soit bien celui du regard et non sa somme avec celui du crâne.
        float targetX = pitch - HALF_PI - this.head.xRot;
        float targetY = yaw - this.head.yRot;

        // La rotation se RÉPARTIT sur les deux segments au lieu de se concentrer sur le premier.
        // Tout mettre à la base donnait une barre rigide qu'on pivote ; en la partageant, la trompe
        // s'incurve, et c'est cette courbe qui la fait lire comme un muscle. Comme les os sont
        // chaînés, la somme des deux angles vaut toujours la direction visée : la pointe désigne
        // exactement le curseur malgré la courbure.
        float baseShare = 0.60f;
        float tipShare = 1f - baseShare;

        // Le segment du bas traîne sur celui du haut : le retard fait le fouet quand on balaie du
        // regard, et évite que les deux moitiés ne pivotent d'un seul bloc.
        float whipYaw = (float) Math.toRadians(elephant.trunkAimYaw - elephant.trunkAimYawO) * 1.4f;
        float whipPitch = (float) Math.toRadians(elephant.trunkAimPitch - elephant.trunkAimPitchO) * 1.4f;

        // Enroulement lent et permanent : une trompe au repos n'est jamais parfaitement droite.
        float breath = Mth.sin((elephant.tickCount + partial) * 0.045f) * 0.10f;
        float sway = Mth.sin((elephant.tickCount + partial) * 0.031f) * 0.08f;

        this.trunk.xRot = Mth.lerp(weight, this.trunk.xRot, targetX * baseShare - breath);
        this.trunk.yRot = Mth.lerp(weight, this.trunk.yRot, targetY * baseShare - sway);
        this.trunk.zRot = Mth.lerp(weight, this.trunk.zRot, sway * 0.5f);

        this.trunk2.xRot = Mth.lerp(weight, this.trunk2.xRot, targetX * tipShare - whipPitch + breath * 2f);
        this.trunk2.yRot = Mth.lerp(weight, this.trunk2.yRot, targetY * tipShare - whipYaw + sway * 2f);
        this.trunk2.zRot = Mth.lerp(weight, this.trunk2.zRot, -sway * 0.4f);

        float swell = elephant.getTrunkSwell(partial) * weight;
        if (swell > 0.001f) {
            float grow = 1f + swell * 0.28f;
            this.trunk.xScale *= grow;
            this.trunk.zScale *= grow;
            this.trunk2.xScale *= 1f + swell * 0.36f;
            this.trunk2.zScale *= 1f + swell * 0.36f;
        }
    }

    /**
     * Empile les transformations jusqu'à la <b>pointe</b> de la trompe, telle qu'elle est
     * réellement dessinée cette image-ci.
     *
     * <p>C'est le seul moyen d'ancrer les gouttes sur une trompe qui ondule : recalculer la pointe
     * à partir des angles synchronisés donnerait une position juste au tick près, donc fausse entre
     * deux ticks et décalée de tout ce que les animations clés ajoutent par-dessus.</p>
     */
    public void translateToTrunkTip(PoseStack poseStack) {
        this.ALL2.translateAndRotate(poseStack);
        this.ALL.translateAndRotate(poseStack);
        this.body.translateAndRotate(poseStack);
        this.head.translateAndRotate(poseStack);
        this.trunk.translateAndRotate(poseStack);
        this.trunk2.translateAndRotate(poseStack);
    }

    /** Longueur du second segment de trompe, en unités de modèle : la pointe est à son extrémité. */
    public static final float TRUNK_TIP_LENGTH = 16.0f;

    private void captureBodyState(ElephantEntity elephant, float restPoseYSum, ModelPart... boneChain) {
        if (!elephant.level().isClientSide()) return;
        elephant.setBodyZRot((float) Math.toDegrees(this.ALL.zRot + this.body.zRot));
        elephant.setBodyXRot((float) -Math.toDegrees(this.ALL.xRot + this.body.xRot));
        float ySum = 0f;
        for (ModelPart bone : boneChain) ySum += bone.y;
        elephant.bodyAnimY = ySum - restPoseYSum;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch) {
        pNetHeadYaw = Mth.clamp(pNetHeadYaw, -22.0F, 22.0F);
        pHeadPitch = Mth.clamp(pHeadPitch, -22.0F, 22.0F);

        this.head.yRot = pNetHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = pHeadPitch * ((float) Math.PI / 180F);
    }

    @Override
    public ModelPart root() {
        return this.ALL2;
    }

    public void copyPoseFrom(ElephantModel<?> src) {
        this.ALL2.copyFrom(src.ALL2);
        this.ALL.copyFrom(src.ALL);
        this.right_arm.copyFrom(src.right_arm);
        this.right_leg.copyFrom(src.right_leg);
        this.left_leg.copyFrom(src.left_leg);
        this.left_arm.copyFrom(src.left_arm);
        this.body.copyFrom(src.body);
        this.head.copyFrom(src.head);
        this.left_ear.copyFrom(src.left_ear);
        this.right_ear.copyFrom(src.right_ear);
        this.trunk.copyFrom(src.trunk);
        this.trunk2.copyFrom(src.trunk2);
        this.left_eyeBall.copyFrom(src.left_eyeBall);
        this.right_eyeBall.copyFrom(src.right_eyeBall);
        this.tail.copyFrom(src.tail);
        this.tail2.copyFrom(src.tail2);
        this.demon_left_wing.copyFrom(src.demon_left_wing);
        this.demon_right_wing.copyFrom(src.demon_right_wing);
    }
}
