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
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.animation.BoaAnimations;
import net.tiew.operationWild.entity.custom.living.BoaEntity;

public class BoaModel<T extends BoaEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "boa_default"), "main");private final ModelPart ALL2;
    private final ModelPart ALL;
    private final ModelPart body5;
    private final ModelPart body4;
    private final ModelPart body3;
    private final ModelPart body2;
    private final ModelPart body1;
    private final ModelPart head;
    private final ModelPart tong;
    private final ModelPart tong2;
    private final ModelPart mouthUp;
    private final ModelPart mouthDown;
    private final ModelPart body6;
    private final ModelPart body7;
    private final ModelPart body8;


    public BoaModel(ModelPart root) {
        this.ALL2 = root.getChild("ALL2");
        this.ALL = this.ALL2.getChild("ALL");
        this.body5 = this.ALL.getChild("body5");
        this.body4 = this.body5.getChild("body4");
        this.body3 = this.body4.getChild("body3");
        this.body2 = this.body3.getChild("body2");
        this.body1 = this.body2.getChild("body1");
        this.head = this.body1.getChild("head");
        this.tong = this.head.getChild("tong");
        this.tong2 = this.head.getChild("tong2");
        this.mouthUp = this.head.getChild("mouthUp");
        this.mouthDown = this.head.getChild("mouthDown");
        this.body6 = this.body5.getChild("body6");
        this.body7 = this.body6.getChild("body7");
        this.body8 = this.body7.getChild("body8");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 20.0F, 5.0F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body5 = ALL.addOrReplaceChild("body5", CubeListBuilder.create().texOffs(44, 24).addBox(-4.0F, -3.5F, 0.0F, 8.0F, 7.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(135, 138).addBox(0.0F, -9.5F, 3.0F, 0.0F, 6.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.5F, 0.0F));

        PartDefinition cube_r1 = body5.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(88, 151).addBox(6.0F, -12.0F, -1.0F, 13.0F, 12.0F, 0.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(3.0F, 2.0F, -7.0F, 1.0653F, -1.3361F, -2.7575F));

        PartDefinition cube_r2 = body5.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(157, 199).addBox(6.0F, -11.0F, -1.0F, 10.0F, 11.0F, 0.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-9.0F, -2.0F, 2.0F, -0.1117F, -0.0577F, 0.2342F));

        PartDefinition body4 = body5.addOrReplaceChild("body4", CubeListBuilder.create().texOffs(0, 45).mirror().addBox(-4.0F, -3.5F, -14.0F, 8.0F, 7.0F, 14.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 107).addBox(-4.0F, -3.5F, -13.0F, 8.0F, 7.0F, 14.0F, new CubeDeformation(0.5F))
                .texOffs(135, 138).addBox(0.0F, -9.5F, -13.0F, 0.0F, 6.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r3 = body4.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(80, 114).mirror().addBox(0.0F, -4.5F, 0.0F, 0.0F, 7.0F, 14.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(-4.8F, -2.5F, -14.3F, 0.2992F, 0.0952F, 0.2992F));

        PartDefinition cube_r4 = body4.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(80, 114).addBox(0.0F, -4.5F, 0.0F, 0.0F, 7.0F, 14.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(4.8F, -2.5F, -14.3F, 0.2992F, -0.0952F, -0.2992F));

        PartDefinition body3 = body4.addOrReplaceChild("body3", CubeListBuilder.create().texOffs(0, 24).addBox(-4.0F, -3.5F, -14.0F, 8.0F, 7.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(57, 202).addBox(-3.5F, -13.0F, -2.0F, 16.0F, 16.0F, 0.0F, new CubeDeformation(0.1F))
                .texOffs(192, 113).addBox(-4.5F, -4.0F, -14.0F, 9.0F, 8.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(135, 138).addBox(0.0F, -13.5F, -14.0F, 0.0F, 10.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -14.0F));

        PartDefinition cube_r5 = body3.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(71, 98).mirror().addBox(0.0F, -5.5F, 0.0F, 0.0F, 8.0F, 14.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(-5.9F, 0.5F, -14.3F, 0.0F, 0.0873F, 0.0F));

        PartDefinition cube_r6 = body3.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(71, 98).addBox(0.0F, -5.5F, 0.0F, 0.0F, 8.0F, 14.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(5.9F, 0.5F, -14.3F, 0.0F, -0.0873F, 0.0F));

        PartDefinition cube_r7 = body3.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(205, 180).addBox(5.0F, -11.0F, -1.0F, 11.0F, 11.0F, 0.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-8.0F, 6.0F, -13.0F, -0.0128F, -0.4635F, -0.5778F));

        PartDefinition body2 = body3.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(0, 66).addBox(-3.0F, -3.5F, -9.0F, 6.0F, 6.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(146, 78).addBox(0.0F, -7.5F, -9.0F, 0.0F, 4.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(43, 124).addBox(5.9F, -1.0F, -9.3F, 0.0F, 1.0F, 9.0F, new CubeDeformation(0.1F))
                .texOffs(43, 124).mirror().addBox(-5.9F, -1.0F, -9.3F, 0.0F, 1.0F, 9.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offset(0.0F, 1.0F, -14.0F));

        PartDefinition cube_r8 = body2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(179, 239).mirror().addBox(-8.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-4.0F, -1.0F, -4.0F, 0.2533F, 0.7519F, 0.3622F));

        PartDefinition cube_r9 = body2.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(179, 239).addBox(-2.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -1.0F, -4.0F, 0.2533F, -0.7519F, -0.3622F));

        PartDefinition body1 = body2.addOrReplaceChild("body1", CubeListBuilder.create().texOffs(0, 81).mirror().addBox(-3.0F, -3.475F, -9.0F, 6.0F, 6.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(146, 78).addBox(0.0F, -7.5F, -9.0F, 0.0F, 4.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(42, 119).mirror().addBox(5.9F, -0.975F, -9.3F, 0.0F, 1.0F, 9.0F, new CubeDeformation(0.1F)).mirror(false)
                .texOffs(42, 119).addBox(-5.9F, -0.975F, -9.3F, 0.0F, 1.0F, 9.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 0.0F, -9.0F));

        PartDefinition head = body1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(42, 111).addBox(-5.0F, -1.0F, -10.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -9.0F));

        PartDefinition cube_r10 = head.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(52, 113).mirror().addBox(0.0F, -0.5F, 0.0F, 0.0F, 1.0F, 10.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(-5.0F, -0.5F, -10.3F, 0.0F, -0.0873F, 0.0F));

        PartDefinition cube_r11 = head.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(52, 113).addBox(0.0F, -0.5F, 0.0F, 0.0F, 1.0F, 10.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(5.0F, -0.5F, -10.3F, 0.0F, 0.0873F, 0.0F));

        PartDefinition cube_r12 = head.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(179, 239).mirror().addBox(-8.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-5.0F, -2.0F, -1.0F, 0.2533F, 0.7519F, 0.3622F));

        PartDefinition cube_r13 = head.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(179, 239).addBox(-2.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -2.0F, -1.0F, 0.2533F, -0.7519F, -0.3622F));

        PartDefinition tong = head.addOrReplaceChild("tong", CubeListBuilder.create().texOffs(107, 0).addBox(-2.5F, 0.5F, -4.0F, 5.0F, 0.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, -7.0F));

        PartDefinition tong2 = head.addOrReplaceChild("tong2", CubeListBuilder.create().texOffs(156, 9).addBox(-2.5F, 0.5F, -4.0F, 5.0F, 0.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -1.0F, -12.5F, 0.5918F, 0.6148F, 0.3409F));

        PartDefinition mouthUp = head.addOrReplaceChild("mouthUp", CubeListBuilder.create().texOffs(44, 65).addBox(-4.0F, -1.5F, -10.0F, 8.0F, 3.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(187, 230).addBox(3.0F, -4.5F, -10.0F, 0.0F, 3.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(187, 230).mirror().addBox(-3.0F, -4.5F, -10.0F, 0.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(138, 250).addBox(4.0F, -0.5F, -7.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(138, 250).mirror().addBox(-6.0F, -0.5F, -7.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(156, 169).addBox(-9.0F, -3.5F, -10.0F, 6.0F, 7.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(156, 169).mirror().addBox(3.0F, -3.5F, -10.0F, 6.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(97, 173).addBox(-4.0F, -4.5F, -7.0F, 8.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(167, 105).mirror().addBox(-7.0F, -8.5F, -4.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(166, 105).addBox(6.0F, -8.5F, -4.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(164, 142).mirror().addBox(-7.0F, -4.5F, -4.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(163, 142).addBox(4.0F, -4.5F, -4.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(56, 72).addBox(3.0F, 1.5F, -9.0F, 0.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(56, 72).mirror().addBox(-3.0F, 1.5F, -9.0F, 0.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(121, 101).addBox(-4.0F, -5.5F, -8.0F, 8.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(138, 115).addBox(-1.0F, -5.5F, -9.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(125, 126).addBox(-4.0F, -2.5F, -11.0F, 8.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, -1.0F));

        PartDefinition mouthDown = head.addOrReplaceChild("mouthDown", CubeListBuilder.create().texOffs(44, 85).addBox(-4.0F, -1.5F, -10.0F, 8.0F, 3.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(72, 73).addBox(3.0F, -3.5F, -10.0F, 0.0F, 3.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(72, 73).mirror().addBox(-3.0F, -3.5F, -10.0F, 0.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 1.0F, -1.0F));

        PartDefinition body6 = body5.addOrReplaceChild("body6", CubeListBuilder.create().texOffs(44, 45).addBox(-3.0F, -3.5F, 0.0F, 6.0F, 6.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(146, 78).addBox(0.0F, -7.5F, 3.0F, 0.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 14.0F));

        PartDefinition cube_r14 = body6.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(205, 202).addBox(6.0F, -7.0F, -1.0F, 8.0F, 7.0F, 0.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-3.0F, -6.0F, -1.0F, -0.5628F, -0.7972F, 0.7507F));

        PartDefinition body7 = body6.addOrReplaceChild("body7", CubeListBuilder.create().texOffs(84, 45).mirror().addBox(-3.0F, -3.5F, 0.0F, 6.0F, 6.0F, 14.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(146, 78).addBox(0.0F, -7.5F, 2.0F, 0.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 14.0F));

        PartDefinition body8 = body7.addOrReplaceChild("body8", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.5F, 0.0F, 4.0F, 5.0F, 19.0F, new CubeDeformation(0.0F))
                .texOffs(14, 171).addBox(-6.0F, 0.5F, 13.0F, 12.0F, 0.0F, 25.0F, new CubeDeformation(0.0F))
                .texOffs(135, 138).addBox(0.0F, -8.5F, 2.0F, 0.0F, 6.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 14.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    private ModelPart getBodyPartByIndex(int index) {
        switch (index) {
            case 0: return body1;
            case 1: return body2;
            case 2: return body3;
            case 3: return body4;
            case 4: return body5;
            case 5: return body6;
            case 6: return body7;
            case 7: return body8;
            default: return null;
        }
    }

    private void manuallyRescaleChildren(ModelPart parent, float inverseScale) {
        if (parent == body5) {
            rescaleIfExists(parent, "body4", inverseScale);
            rescaleIfExists(parent, "body6", inverseScale);
        }
        if (parent == body4) {
            rescaleIfExists(parent, "body3", inverseScale);
        }
        if (parent == body3) {
            rescaleIfExists(parent, "body2", inverseScale);
        }
        if (parent == body2) {
            rescaleIfExists(parent, "body1", inverseScale);
        }
        if (parent == body1) {
            rescaleIfExists(parent, "head", inverseScale);
        }
        if (parent == head) {
            rescaleIfExists(parent, "tong", inverseScale);
            rescaleIfExists(parent, "mouthUp", inverseScale);
            rescaleIfExists(parent, "mouthDown", inverseScale);
        }
        if (parent == body6) {
            rescaleIfExists(parent, "body7", inverseScale);
        }
        if (parent == body7) {
            rescaleIfExists(parent, "body8", inverseScale);
        }
    }

    private void rescaleIfExists(ModelPart parent, String childName, float scale) {
        ModelPart child = parent.getChild(childName);
        if (child != null) {
            child.xScale = scale;
            child.yScale = scale;
            child.zScale = scale;
        }
    }

    @Override
    public void setupAnim(BoaEntity boa, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        if (boa.isTong()) {
            this.animate(boa.tongAnimationState, BoaAnimations.TONG, ageInTicks, 1.0f);
        }
        this.animate(boa.attackAnimationState, BoaAnimations.ATTACK_STRIKE, ageInTicks, 1.0f);


        if (boa.isNips()) {
            this.animate(boa.nipsAnimationState, BoaAnimations.NIPS, ageInTicks, 1.0f);
            return;
        }


        float globalSpeed = 0.6f;
        float globalDegree = 2f;
        limbSwingAmount = 0.5F;

        if (boa.isInWater()) {
            this.body5.xRot = boa.getXRot() * ((float) Math.PI / 180F);
            swing(this.head, 0.5f * globalSpeed, 0.6f * (globalDegree / 4), false, 5.4f, 0, limbSwing, limbSwingAmount, true, false, false);
            swing(this.body1, 0.5f * globalSpeed, 0.8f * (globalDegree / 4), false, 4.4f, 0, limbSwing, limbSwingAmount, true, false, false);
            swing(this.body2, 0.5f * globalSpeed, 0.6f * (globalDegree / 4), false, 3.6f, 0, limbSwing, limbSwingAmount, true, false, false);
            swing(this.body3, 0.5f * globalSpeed, 1f * (globalDegree / 4), false, 2.8f, 0, limbSwing, limbSwingAmount, true, false, false);
            swing(this.body4, 0.5f * globalSpeed, 0.8f * (globalDegree / 4), false, 2f, 0, limbSwing, limbSwingAmount, true, false, false);
            swing(this.body5, 0.5f * globalSpeed, 1f * (globalDegree / 4), false, 0f, 0, limbSwing, limbSwingAmount, true, false, false);
            swing(this.body6, 0.5f * globalSpeed, 0.8f * (globalDegree / 4), false, -2f, 0, limbSwing, limbSwingAmount, true, false, false);
            swing(this.body7, 0.5f * globalSpeed, 1f * (globalDegree / 4), false, -2.8f, 0, limbSwing, limbSwingAmount, true, false, false);
            swing(this.body8, 0.5f * globalSpeed, 0.6f * (globalDegree / 4), false, -3.6f, 0, limbSwing, limbSwingAmount, true, false, false);
        }

        if (boa.isSitting() || boa.isInResurrection()) {
            float sittingMultiplier = 0.55f;
            this.head.yRot = 0.6f * globalDegree * -(sittingMultiplier * 2);
            this.body1.yRot = 0.8f * globalDegree * sittingMultiplier;
            this.body2.yRot = 0.6f * globalDegree * sittingMultiplier;
            this.body3.yRot = 1f * globalDegree * sittingMultiplier;
            this.body4.yRot = 0.8f * globalDegree * sittingMultiplier;
            this.body5.yRot = -(1f * globalDegree * sittingMultiplier);
            this.body6.yRot = 0.8f * globalDegree * sittingMultiplier;
            this.body7.yRot = 1f * globalDegree * sittingMultiplier;
            this.body8.yRot = 0.6f * globalDegree * sittingMultiplier;
        } else {
            swing(this.head, 0.5f * globalSpeed, 0.6f * globalDegree, false, 5.4f, 0, limbSwing, limbSwingAmount, false, true, false);
            swing(this.body1, 0.5f * globalSpeed, 0.8f * globalDegree, false, 4.4f, 0, limbSwing, limbSwingAmount, false, true, false);
            swing(this.body2, 0.5f * globalSpeed, 0.6f * globalDegree, false, 3.6f, 0, limbSwing, limbSwingAmount, false, true, false);
            swing(this.body3, 0.5f * globalSpeed, 1f * globalDegree, false, 2.8f, 0, limbSwing, limbSwingAmount, false, true, false);
            swing(this.body4, 0.5f * globalSpeed, 0.8f * globalDegree, false, 2f, 0, limbSwing, limbSwingAmount, false, true, false);
            swing(this.body5, 0.5f * globalSpeed, 1f * globalDegree, false, 0f, 0, limbSwing, limbSwingAmount, false, true, false);
            swing(this.body6, 0.5f * globalSpeed, 0.8f * globalDegree, false, -2f, 0, limbSwing, limbSwingAmount, false, true, false);
            swing(this.body7, 0.5f * globalSpeed, 1f * globalDegree, false, -2.8f, 0, limbSwing, limbSwingAmount, false, true, false);
            swing(this.body8, 0.5f * globalSpeed, 0.6f * globalDegree, false, -3.6f, 0, limbSwing, limbSwingAmount, false, true, false);
        }




        float maxScale = 1.4f;
        float minScale = 1.0f;

        ModelPart[] allParts = {head, body1, body2, body3, body4, body5, body6, body7, body8};
        for (ModelPart part : allParts) {
            part.xScale = minScale;
            part.yScale = minScale;
            part.zScale = minScale;
        }

        if (boa.getNumberOfBody() == 9) {
            return;
        }

        int currentBodyIndex = boa.getNumberOfBody() - 1;
        if (currentBodyIndex >= 0 && currentBodyIndex < 8) {
            ModelPart targetPart = getBodyPartByIndex(currentBodyIndex);
            if (targetPart != null) {
                float digestProgress = 1.0f;

                if (boa.isDigests()) {
                    int time = boa.getDigestsTime();

                    if (boa.isCanDecreaseDigestTime()) {
                        digestProgress = (float) time / boa.getDigestsTimeMax();
                    } else {
                        digestProgress = (float) time / boa.getDigestsTimeMax();
                    }
                }

                float scale = minScale + (maxScale - minScale) * digestProgress;

                targetPart.xScale = scale;
                targetPart.yScale = scale;
                targetPart.zScale = scale;

                manuallyRescaleChildren(targetPart, minScale / scale);
            }
        }

        if (boa.isBaby()) {
            float maturationPercent = (float) boa.getMaturationPercentage() / 100f;
            float headScale = 1.75f - (1.75f - 1.0f) * maturationPercent;

            this.head.xScale *= headScale;
            this.head.yScale *= headScale;
            this.head.zScale *= headScale;
        }
    }

    private static void swing(ModelPart part, float speed, float degree, boolean invert, float offset, float weight, float limbSwing, float limbSwingAmount, boolean xRot, boolean yRot, boolean zRot) {
        float direction = invert ? -1F : 1F;
        if (xRot) part.xRot += direction * (Mth.sin(limbSwing * speed + offset) * degree * limbSwingAmount + weight * limbSwingAmount);
        if (yRot) part.yRot += direction * (Mth.sin(limbSwing * speed + offset) * degree * limbSwingAmount + weight * limbSwingAmount);
        if (zRot) part.zRot += direction * (Mth.sin(limbSwing * speed + offset) * degree * limbSwingAmount + weight * limbSwingAmount);
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