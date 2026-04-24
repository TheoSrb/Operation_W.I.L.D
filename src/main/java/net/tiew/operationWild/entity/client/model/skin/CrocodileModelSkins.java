package net.tiew.operationWild.entity.client.model.skin;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;

public class CrocodileModelSkins {

    public static final ModelLayerLocation LAYER_VERMILION_GUARDIAN = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "crocodile_vermilion_guardian"), "main");
    public static final ModelLayerLocation LAYER_TOY = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "crocodile_toy"), "main");


    public static LayerDefinition createSkinVermilionGuardian() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 14.0F, 3.0F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(64, 84).addBox(0.0F, -7.0F, -14.0F, 0.0F, 4.0F, 27.0F, new CubeDeformation(0.01F))
                .texOffs(4, 0).addBox(-4.5F, -3.0F, -14.0F, 9.0F, 9.0F, 26.0F, new CubeDeformation(0.0F))
                .texOffs(170, 218).addBox(-8.5F, -5.0F, -14.0F, 17.0F, 12.0F, 26.0F, new CubeDeformation(-2.0F))
                .texOffs(0, 239).addBox(-6.5F, -5.0F, -11.0F, 13.0F, 2.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.8383F, -1.4547F, 3.0F));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(118, 23).addBox(-4.5F, -5.0F, -7.0F, 9.0F, 9.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(32, 148).addBox(0.0F, -9.0F, -7.0F, 0.0F, 4.0F, 7.0F, new CubeDeformation(0.01F))
                .texOffs(75, 219).addBox(-6.5F, -6.0F, -7.0F, 13.0F, 10.0F, 7.0F, new CubeDeformation(-0.75F))
                .texOffs(190, 29).addBox(-7.5F, -7.0F, -1.5F, 15.0F, 12.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(220, 170).addBox(7.5F, -7.0F, -1.0F, 4.0F, 12.0F, 0.0F, new CubeDeformation(0.01F))
                .texOffs(220, 170).mirror().addBox(-11.5F, -7.0F, -1.0F, 4.0F, 12.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(184, 171).addBox(-7.5F, -11.0F, -1.0F, 15.0F, 4.0F, 0.0F, new CubeDeformation(0.01F))
                .texOffs(184, 178).addBox(-7.5F, 5.0F, -1.0F, 15.0F, 4.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 2.0F, -14.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(114, 65).addBox(-5.5F, -5.0F, -9.0F, 11.0F, 9.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(77, 78).addBox(-5.5F, -7.0F, -6.0F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(77, 78).mirror().addBox(1.5015F, -7.0F, -6.0F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(144, 168).addBox(-5.5F, -5.0F, -9.0F, 11.0F, 9.0F, 9.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, -7.0F));

        PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(123, 80).mirror().addBox(0.3659F, -13.9072F, -3.4705F, 0.0F, 16.0F, 21.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(3.1765F, -6.0F, -1.0F, -0.6137F, 0.6253F, -0.1501F));

        PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(123, 80).addBox(-0.3659F, -13.9072F, -3.4705F, 0.0F, 16.0F, 21.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-3.5F, -6.0F, -1.0F, -0.6137F, -0.6253F, 0.1501F));

        PartDefinition mouth = head.addOrReplaceChild("mouth", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, -9.0F));

        PartDefinition mouth_down = mouth.addOrReplaceChild("mouth_down", CubeListBuilder.create().texOffs(40, 117).addBox(-3.5F, -2.0F, -10.0F, 7.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(41, 135).addBox(-3.5F, 1.0F, -10.0F, 7.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(91, -10).addBox(-3.4F, -5.0F, -10.0F, 0.0F, 3.0F, 10.0F, new CubeDeformation(0.05F))
                .texOffs(91, -10).mirror().addBox(3.4F, -5.0F, -10.0F, 0.0F, 3.0F, 10.0F, new CubeDeformation(0.05F)).mirror(false)
                .texOffs(94, 13).addBox(-3.5F, -5.0F, -9.5F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition mouth_up = mouth.addOrReplaceChild("mouth_up", CubeListBuilder.create().texOffs(116, 40).addBox(-3.5F, -3.0F, -10.0F, 7.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 170).addBox(-3.5F, -3.0F, -10.0F, 7.0F, 4.0F, 10.0F, new CubeDeformation(0.25F))
                .texOffs(123, 252).addBox(-7.5F, 0.0F, -2.0F, 15.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(97, 128).addBox(-3.5F, -3.0F, -10.5F, 7.0F, 4.0F, 3.0F, new CubeDeformation(0.5F))
                .texOffs(94, 9).addBox(-3.5F, 1.0F, -10.5F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.025F))
                .texOffs(91, -6).addBox(-3.4F, 1.0F, -10.0F, 0.0F, 3.0F, 10.0F, new CubeDeformation(0.05F))
                .texOffs(91, -6).mirror().addBox(3.4F, 1.0F, -10.0F, 0.0F, 3.0F, 10.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offset(0.0F, -2.0F, 0.0F));

        PartDefinition cube_r3 = mouth_up.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(205, 123).mirror().addBox(0.0F, -14.0F, -1.0F, 0.0F, 15.0F, 22.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(-6.5F, 1.0F, -1.0F, -0.1262F, -0.3435F, 0.4134F));

        PartDefinition cube_r4 = mouth_up.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(205, 123).addBox(0.0F, -14.0F, -1.0F, 0.0F, 15.0F, 22.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(6.5F, 1.0F, -1.0F, -0.1262F, 0.3435F, -0.4134F));

        PartDefinition left_mustache = mouth_up.addOrReplaceChild("left_mustache", CubeListBuilder.create(), PartPose.offset(3.6765F, 13.5F, -10.0F));

        PartDefinition cube_r5 = left_mustache.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(100, 153).mirror().addBox(0.0F, -3.0F, 0.0F, 12.0F, 5.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(0.0F, -14.5F, 1.5F, -0.0352F, -0.9113F, -0.3783F));

        PartDefinition right_mustache = mouth_up.addOrReplaceChild("right_mustache", CubeListBuilder.create(), PartPose.offset(-4.0F, 13.5F, -10.0F));

        PartDefinition cube_r6 = right_mustache.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(100, 153).addBox(-12.0F, -3.0F, 0.0F, 12.0F, 5.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.0F, -14.5F, 1.5F, -0.0352F, 0.9113F, 0.3783F));

        PartDefinition right_eyeball = head.addOrReplaceChild("right_eyeball", CubeListBuilder.create().texOffs(77, 85).addBox(-5.5F, -3.0F, -1.075F, 7.0F, 4.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.offset(-3.0F, -6.0F, -5.0F));

        PartDefinition left_eyeball = head.addOrReplaceChild("left_eyeball", CubeListBuilder.create().texOffs(77, 85).mirror().addBox(-1.175F, -3.0F, -1.075F, 7.0F, 4.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(2.6765F, -6.0F, -5.0F));

        PartDefinition tail1 = body.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(1, 38).addBox(-4.5F, -5.0F, 0.0F, 9.0F, 9.0F, 20.0F, new CubeDeformation(0.0F))
                .texOffs(72, 84).addBox(0.0F, -9.0F, 0.0F, 0.0F, 4.0F, 20.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 2.0F, 12.0F));

        PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(62, 38).addBox(-3.5F, -4.0F, 0.0F, 7.0F, 7.0F, 20.0F, new CubeDeformation(0.0F))
                .texOffs(0, 51).addBox(0.0F, -8.0F, 0.0F, 0.0F, 4.0F, 20.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 0.0F, 20.0F));

        PartDefinition tail3 = tail2.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(153, 32).addBox(0.0F, -8.0F, 0.0F, 0.0F, 9.0F, 22.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 2.0F, 20.0F));

        PartDefinition left_arm = ALL.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(86, 23).addBox(-2.5F, -2.0F, -3.0F, 3.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(114, 83).addBox(-3.5F, 4.9F, -7.0F, 8.0F, 0.0F, 10.0F, new CubeDeformation(0.05F)), PartPose.offset(5.1617F, 4.5453F, -7.0F));

        PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 119).addBox(-2.5F, -3.0F, -3.0F, 4.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(114, 83).addBox(-3.5F, 5.9F, -7.0F, 8.0F, 0.0F, 10.0F, new CubeDeformation(0.05F)), PartPose.offset(4.1617F, 3.5453F, 9.0F));

        PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(114, 83).mirror().addBox(-4.5F, 5.9F, -7.0F, 8.0F, 0.0F, 10.0F, new CubeDeformation(0.05F)).mirror(false)
                .texOffs(2, 119).mirror().addBox(-1.5F, -3.0F, -3.0F, 4.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-5.8383F, 3.5453F, 9.0F));

        PartDefinition right_arm = ALL.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(114, 83).mirror().addBox(-4.5F, 4.9F, -7.0F, 8.0F, 0.0F, 10.0F, new CubeDeformation(0.05F)).mirror(false)
                .texOffs(88, 23).mirror().addBox(-0.5F, -2.0F, -3.0F, 3.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-6.8383F, 4.5453F, -7.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    public static LayerDefinition createSkinToy() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 14.0F, 3.0F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.5F, -5.5F, -14.0F, 17.0F, 12.0F, 26.0F, new CubeDeformation(-0.5F))
                .texOffs(170, 218).addBox(-8.5F, -5.5F, -14.0F, 17.0F, 12.0F, 26.0F, new CubeDeformation(0.5F))
                .texOffs(0, 239).addBox(-6.5F, -7.0F, -11.0F, 13.0F, 2.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(0, 67).addBox(-5.5F, -8.0F, -14.0F, 0.0F, 3.0F, 26.0F, new CubeDeformation(0.01F))
                .texOffs(0, 67).mirror().addBox(5.5F, -8.0F, -14.0F, 0.0F, 3.0F, 26.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(-0.8383F, -1.4547F, 3.0F));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(116, 23).addBox(-6.5F, -6.0F, -7.0F, 13.0F, 10.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(75, 219).addBox(-6.5F, -6.0F, -7.0F, 13.0F, 10.0F, 7.0F, new CubeDeformation(0.5F))
                .texOffs(190, 29).addBox(-7.5F, -7.0F, -1.5F, 15.0F, 12.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(220, 170).addBox(7.5F, -7.0F, -1.0F, 4.0F, 12.0F, 0.0F, new CubeDeformation(0.01F))
                .texOffs(220, 170).mirror().addBox(-11.5F, -7.0F, -1.0F, 4.0F, 12.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(184, 171).addBox(-7.5F, -11.0F, -1.0F, 15.0F, 4.0F, 0.0F, new CubeDeformation(0.01F))
                .texOffs(184, 178).addBox(-7.5F, 5.0F, -1.0F, 15.0F, 4.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 2.0F, -13.5F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(114, 65).addBox(-5.5F, -5.0F, -9.0F, 11.0F, 9.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(141, 96).addBox(-5.5F, -9.0F, -6.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(141, 96).mirror().addBox(0.1765F, -9.0F, -6.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(144, 168).addBox(-5.5F, -5.0F, -9.0F, 11.0F, 9.0F, 9.0F, new CubeDeformation(0.75F)), PartPose.offset(0.0F, 0.0F, -7.0F));

        PartDefinition mouth = head.addOrReplaceChild("mouth", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, -9.0F));

        PartDefinition mouth_down = mouth.addOrReplaceChild("mouth_down", CubeListBuilder.create().texOffs(38, 117).addBox(-5.5F, -2.0F, -10.0F, 11.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(59, 181).addBox(-3.5F, -2.0F, -10.0F, 7.0F, 3.0F, 10.0F, new CubeDeformation(0.25F))
                .texOffs(91, -10).addBox(-5.375F, -5.0F, -9.4F, 0.0F, 3.0F, 10.0F, new CubeDeformation(0.05F))
                .texOffs(91, -10).mirror().addBox(5.375F, -5.0F, -9.5F, 0.0F, 3.0F, 10.0F, new CubeDeformation(0.05F)).mirror(false)
                .texOffs(92, 13).addBox(-5.5F, -5.0F, -9.5F, 11.0F, 3.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offset(0.0F, 2.0F, 0.0F));

        PartDefinition mouth_up = mouth.addOrReplaceChild("mouth_up", CubeListBuilder.create().texOffs(114, 40).addBox(-5.5F, -2.0F, -10.0F, 11.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 170).addBox(-3.5F, -2.0F, -10.0F, 7.0F, 4.0F, 10.0F, new CubeDeformation(0.5F))
                .texOffs(123, 252).addBox(-7.5F, 1.0F, -2.0F, 15.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(95, 128).addBox(-5.5F, -2.0F, -10.5F, 11.0F, 4.0F, 3.0F, new CubeDeformation(0.5F))
                .texOffs(92, 9).addBox(-5.5F, 2.0F, -10.5F, 11.0F, 3.0F, 0.0F, new CubeDeformation(0.025F))
                .texOffs(91, -6).addBox(-5.4F, 2.0F, -10.0F, 0.0F, 3.0F, 10.0F, new CubeDeformation(0.05F))
                .texOffs(91, -6).mirror().addBox(5.4F, 2.0F, -10.0F, 0.0F, 3.0F, 10.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offset(0.0F, -4.0F, 0.0F));

        PartDefinition cube_r1 = mouth_up.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(205, 123).mirror().addBox(0.0F, -14.0F, -1.0F, 0.0F, 15.0F, 22.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(-6.5F, 2.0F, -1.0F, -0.1262F, -0.3435F, 0.4134F));

        PartDefinition cube_r2 = mouth_up.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(205, 123).addBox(0.0F, -14.0F, -1.0F, 0.0F, 15.0F, 22.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(6.5F, 2.0F, -1.0F, -0.1262F, 0.3435F, -0.4134F));

        PartDefinition right_eyeball = head.addOrReplaceChild("right_eyeball", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_eyeball = head.addOrReplaceChild("left_eyeball", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition tail1 = body.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(0, 38).addBox(-5.5F, -4.0F, 0.0F, 11.0F, 9.0F, 20.0F, new CubeDeformation(0.0F))
                .texOffs(67, 84).addBox(-3.5F, -7.0F, 0.0F, 0.0F, 3.0F, 20.0F, new CubeDeformation(0.01F))
                .texOffs(67, 84).mirror().addBox(3.5F, -7.0F, 0.0F, 0.0F, 3.0F, 20.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(0.0F, 2.0F, 11.5F));

        PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(62, 38).addBox(-3.5F, -2.0F, 0.0F, 7.0F, 7.0F, 20.0F, new CubeDeformation(0.0F))
                .texOffs(67, 84).addBox(-2.5F, -5.0F, 0.0F, 0.0F, 3.0F, 20.0F, new CubeDeformation(0.01F))
                .texOffs(67, 84).mirror().addBox(2.5F, -5.0F, 0.0F, 0.0F, 3.0F, 20.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(0.0F, 0.0F, 20.0F));

        PartDefinition tail3 = tail2.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(153, 32).addBox(0.0F, -6.0F, 0.0F, 0.0F, 9.0F, 22.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 2.0F, 20.0F));

        PartDefinition left_arm = ALL.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(86, 23).addBox(-2.5F, -2.0F, -3.0F, 5.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(114, 83).addBox(-2.5F, 4.9F, -7.0F, 8.0F, 0.0F, 10.0F, new CubeDeformation(0.05F)), PartPose.offset(9.1617F, 4.5453F, -7.0F));

        PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 119).addBox(-2.5F, -3.0F, -3.0F, 6.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(114, 83).addBox(-2.5F, 5.9F, -7.0F, 8.0F, 0.0F, 10.0F, new CubeDeformation(0.05F)), PartPose.offset(8.1617F, 3.5453F, 9.0F));

        PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(114, 83).mirror().addBox(-5.5F, 5.9F, -7.0F, 8.0F, 0.0F, 10.0F, new CubeDeformation(0.05F)).mirror(false)
                .texOffs(0, 119).mirror().addBox(-3.5F, -3.0F, -3.0F, 6.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-9.8383F, 3.5453F, 9.0F));

        PartDefinition right_arm = ALL.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(114, 83).mirror().addBox(-5.5F, 4.9F, -7.0F, 8.0F, 0.0F, 10.0F, new CubeDeformation(0.05F)).mirror(false)
                .texOffs(86, 23).mirror().addBox(-2.5F, -2.0F, -3.0F, 5.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-10.8383F, 4.5453F, -7.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }
}