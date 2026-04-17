package net.tiew.operationWild.entity.client.model.skin;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;

public class TigerModelSkins {

    public static final ModelLayerLocation LAYER_BOSS_CHIEF = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "tiger_boss"), "main");
    public static final ModelLayerLocation LAYER_VIRUS = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "tiger_virus"), "main");
    public static final ModelLayerLocation LAYER_SEVEN_SEAS = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "tiger_seven_seas"), "main");
    public static final ModelLayerLocation LAYER_SCARLET_PIRATE = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "tiger_scarlet_pirate"), "main");
    public static final ModelLayerLocation LAYER_CARTOON = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "tiger_cartoon"), "main");
    public static final ModelLayerLocation LAYER_PIZZA_CHEF = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "tiger_pizza_chef"), "main");

    public static LayerDefinition createSkinBossChief() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 2.0F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -7.0F, -11.0F, 10.0F, 12.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(60, 57).addBox(-5.0F, -7.0F, -11.0F, 10.0F, 12.0F, 24.0F, new CubeDeformation(0.25F))
                .texOffs(0, 56).addBox(5.0F, 5.0F, -11.0F, 0.0F, 2.0F, 24.0F, new CubeDeformation(0.01F))
                .texOffs(0, 56).mirror().addBox(-5.0F, 5.0F, -11.0F, 0.0F, 2.0F, 24.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(76, 110).addBox(-4.0F, -7.75F, -6.0F, 8.0F, 1.0F, 9.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 1.0F, -1.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 36).addBox(-5.0F, -4.0F, -7.0F, 10.0F, 8.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(57, 66).addBox(-5.0F, -4.0F, -1.2F, 10.0F, 8.0F, 1.0F, new CubeDeformation(0.25F))
                .texOffs(28, 87).addBox(-7.0F, -6.0F, -0.7F, 14.0F, 12.0F, 0.0F, new CubeDeformation(0.05F))
                .texOffs(70, 28).addBox(4.0F, -4.0F, -4.0F, 3.0F, 9.0F, 0.0F, new CubeDeformation(0.01F))
                .texOffs(70, 28).mirror().addBox(-7.0F, -4.0F, -4.0F, 3.0F, 9.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(68, 13).addBox(-2.5F, 0.0F, -10.0F, 5.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(109, 126).addBox(-3.5F, 4.0F, -9.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(124, 122).addBox(-5.5F, 4.0F, -9.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.01F))
                .texOffs(124, 122).mirror().addBox(3.5F, 4.0F, -9.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(0, 90).addBox(-4.0F, -1.0F, -7.5F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.025F))
                .texOffs(0, 98).addBox(-5.0F, -2.0F, -7.5F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.025F))
                .texOffs(0, 96).addBox(-5.0F, -2.0F, -7.5F, 0.0F, 1.0F, 7.0F, new CubeDeformation(0.025F))
                .texOffs(0, 96).mirror().addBox(5.0F, -2.0F, -7.5F, 0.0F, 1.0F, 7.0F, new CubeDeformation(0.025F)).mirror(false)
                .texOffs(4, 98).addBox(-1.0F, -2.0F, -7.5F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.025F))
                .texOffs(17, 99).mirror().addBox(1.0F, -2.0F, -7.5F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.025F)).mirror(false)
                .texOffs(17, 91).mirror().addBox(1.0F, -1.0F, -7.5F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.025F)).mirror(false), PartPose.offset(0.0F, -4.0F, -11.0F));

        PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(48, 86).mirror().addBox(0.0F, -12.5F, 0.0F, 0.0F, 12.0F, 17.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(5.5F, 5.5F, -9.0F, 0.0F, 0.1309F, -0.1745F));

        PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(48, 86).addBox(0.0F, -12.5F, 0.0F, 0.0F, 12.0F, 17.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-5.5F, 5.5F, -9.0F, 0.0F, -0.1309F, 0.1745F));

        PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(18, 51).addBox(-1.0F, -2.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -3.5F, -4.5F));

        PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(26, 51).addBox(-2.0F, -2.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -3.5F, -4.5F));

        PartDefinition left_eyeBall = head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(0, 54).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.025F)), PartPose.offset(2.0F, -1.5F, -7.0F));

        PartDefinition right_eyeBall = head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(0, 54).mirror().addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.025F)).mirror(false), PartPose.offset(-2.0F, -1.5F, -7.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.5F, -3.5F, 13.0F));

        PartDefinition front_tail = tail.addOrReplaceChild("front_tail", CubeListBuilder.create().texOffs(18, 58).addBox(-2.0F, -1.5F, 0.0F, 3.0F, 3.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition back_tail = front_tail.addOrReplaceChild("back_tail", CubeListBuilder.create().texOffs(68, 0).addBox(-2.0F, -1.5F, 0.0F, 3.0F, 3.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 10.0F));

        PartDefinition left_arm = ALL.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(34, 36).addBox(-2.5F, -1.0F, -2.5F, 4.0F, 17.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(4.5F, 0.0F, -7.5F));

        PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(52, 36).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, 3.0F, 8.5F));

        PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(52, 36).mirror().addBox(-2.5F, -1.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.5F, 3.0F, 8.5F));

        PartDefinition right_arm = ALL.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(34, 36).mirror().addBox(-1.5F, -1.0F, -2.5F, 4.0F, 17.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-4.5F, 0.0F, -7.5F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public static LayerDefinition createSkinVirus() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 2.0F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -7.0F, -11.0F, 10.0F, 12.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(60, 57).addBox(-5.0F, -7.0F, -11.0F, 10.0F, 12.0F, 24.0F, new CubeDeformation(0.25F))
                .texOffs(0, 56).addBox(5.0F, 5.0F, -11.0F, 0.0F, 2.0F, 24.0F, new CubeDeformation(0.01F))
                .texOffs(0, 56).mirror().addBox(-5.0F, 5.0F, -11.0F, 0.0F, 2.0F, 24.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(76, 110).addBox(-4.0F, -7.75F, -6.0F, 8.0F, 1.0F, 9.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 1.0F, -1.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 36).addBox(-5.0F, -4.0F, -7.0F, 10.0F, 8.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(57, 66).addBox(-5.0F, -4.0F, -1.2F, 10.0F, 8.0F, 1.0F, new CubeDeformation(0.25F))
                .texOffs(28, 87).addBox(-7.0F, -6.0F, -0.7F, 14.0F, 12.0F, 0.0F, new CubeDeformation(0.05F))
                .texOffs(70, 28).addBox(4.0F, -4.0F, -4.0F, 3.0F, 9.0F, 0.0F, new CubeDeformation(0.01F))
                .texOffs(70, 28).mirror().addBox(-7.0F, -4.0F, -4.0F, 3.0F, 9.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(68, 13).addBox(-2.5F, 0.0F, -10.0F, 5.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(109, 126).addBox(-3.5F, 4.0F, -9.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(124, 122).addBox(-5.5F, 4.0F, -9.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.01F))
                .texOffs(124, 122).mirror().addBox(3.5F, 4.0F, -9.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(83, 37).addBox(-7.0F, -7.0F, -7.0F, 14.0F, 13.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(114, 29).mirror().addBox(3.0F, -9.0F, -4.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(114, 29).addBox(-7.0F, -9.0F, -4.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, -11.0F));

        PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(48, 86).mirror().addBox(0.0F, -12.5F, 0.0F, 0.0F, 12.0F, 17.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(5.5F, 5.5F, -9.0F, 0.0F, 0.1309F, -0.1745F));

        PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(48, 86).addBox(0.0F, -12.5F, 0.0F, 0.0F, 12.0F, 17.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-5.5F, 5.5F, -9.0F, 0.0F, -0.1309F, 0.1745F));

        PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(18, 51).addBox(-1.0F, -2.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -3.5F, -4.5F));

        PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(26, 51).addBox(-2.0F, -2.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -3.5F, -4.5F));

        PartDefinition left_eyeBall = head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(0, 54).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.025F)), PartPose.offset(2.0F, -1.5F, -7.0F));

        PartDefinition right_eyeBall = head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(0, 54).mirror().addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.025F)).mirror(false), PartPose.offset(-2.0F, -1.5F, -7.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.5F, -3.5F, 13.0F));

        PartDefinition front_tail = tail.addOrReplaceChild("front_tail", CubeListBuilder.create().texOffs(18, 58).addBox(-2.0F, -1.5F, 0.0F, 3.0F, 3.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition back_tail = front_tail.addOrReplaceChild("back_tail", CubeListBuilder.create().texOffs(68, 0).addBox(-2.0F, -1.5F, 0.0F, 3.0F, 3.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 10.0F));

        PartDefinition left_arm = ALL.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(34, 36).addBox(-2.5F, -1.0F, -2.5F, 4.0F, 17.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(4.5F, 0.0F, -7.5F));

        PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(52, 36).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, 3.0F, 8.5F));

        PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(52, 36).mirror().addBox(-2.5F, -1.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.5F, 3.0F, 8.5F));

        PartDefinition right_arm = ALL.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(34, 36).mirror().addBox(-1.5F, -1.0F, -2.5F, 4.0F, 17.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-4.5F, 0.0F, -7.5F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public static LayerDefinition createSkinSevenSeas() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 2.0F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -7.0F, -11.0F, 10.0F, 12.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(76, 110).addBox(-4.0F, -7.75F, -6.0F, 8.0F, 1.0F, 9.0F, new CubeDeformation(0.5F))
                .texOffs(0, 56).addBox(5.0F, 5.0F, -11.0F, 0.0F, 2.0F, 24.0F, new CubeDeformation(0.01F))
                .texOffs(0, 56).mirror().addBox(-5.0F, 5.0F, -11.0F, 0.0F, 2.0F, 24.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(60, 57).addBox(-5.0F, -7.0F, -11.0F, 10.0F, 12.0F, 24.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 1.0F, -1.0F));

        PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 96).mirror().addBox(-4.0F, -23.0F, -1.0F, 16.0F, 16.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-9.0F, -9.0F, 6.0F, -0.5978F, 1.2406F, 1.4879F));

        PartDefinition cube_r2 = body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 112).addBox(-12.0F, -23.0F, -1.0F, 16.0F, 16.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(5.0F, 7.0F, 1.0F, -1.2327F, -1.1128F, 0.6955F));

        PartDefinition cube_r3 = body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(100, 42).addBox(6.0F, -14.0F, 15.0F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, 0.0F, -10.0F, 0.0F, -0.1745F, 0.1745F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 36).addBox(-5.0F, -4.0F, -7.0F, 10.0F, 8.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(94, 12).addBox(-5.0F, -4.0F, -7.0F, 10.0F, 8.0F, 7.0F, new CubeDeformation(0.25F))
                .texOffs(82, 42).addBox(4.0F, -4.0F, -4.0F, 3.0F, 9.0F, 0.0F, new CubeDeformation(0.01F))
                .texOffs(82, 42).mirror().addBox(-7.0F, -4.0F, -4.0F, 3.0F, 9.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(68, 13).addBox(-2.5F, 0.0F, -10.0F, 5.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(118, 0).addBox(-2.5F, 4.0F, -10.0F, 5.0F, 6.0F, 0.0F, new CubeDeformation(0.01F))
                .texOffs(109, 126).addBox(-3.5F, 4.0F, -9.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(124, 122).addBox(-5.5F, 4.0F, -9.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.01F))
                .texOffs(124, 122).mirror().addBox(3.5F, 4.0F, -9.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(57, 66).addBox(-5.0F, -4.0F, -1.2F, 10.0F, 8.0F, 1.0F, new CubeDeformation(0.25F))
                .texOffs(28, 87).addBox(-7.0F, -6.0F, -0.7F, 14.0F, 12.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offset(0.0F, -4.0F, -11.0F));

        PartDefinition cube_r4 = head.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(48, 86).addBox(0.0F, -12.5F, 0.0F, 0.0F, 12.0F, 17.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-5.5F, 5.5F, -9.0F, 0.0F, -0.1309F, 0.1745F));

        PartDefinition cube_r5 = head.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(48, 86).mirror().addBox(0.0F, -12.5F, 0.0F, 0.0F, 12.0F, 17.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(5.5F, 5.5F, -9.0F, 0.0F, 0.1309F, -0.1745F));

        PartDefinition cube_r6 = head.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(118, 0).mirror().addBox(-1.0F, 2.0F, -4.0F, 5.0F, 6.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-1.5F, 2.0F, -9.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r7 = head.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(118, 0).addBox(-4.0F, 2.0F, -4.0F, 5.0F, 6.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(1.5F, 2.0F, -9.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(18, 51).addBox(-1.0F, -2.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -3.5F, -4.5F));

        PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(26, 51).addBox(-2.0F, -2.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -3.5F, -4.5F));

        PartDefinition left_eyeBall = head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(0, 54).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.025F)), PartPose.offset(2.0F, -1.5F, -7.0F));

        PartDefinition right_eyeBall = head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(0, 54).mirror().addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.025F)).mirror(false), PartPose.offset(-2.0F, -1.5F, -7.0F));

        PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(94, 0).addBox(-6.0F, -5.5F, -2.5F, 12.0F, 6.0F, 0.0F, new CubeDeformation(0.01F))
                .texOffs(94, 6).addBox(-6.0F, -5.5F, 2.5F, 12.0F, 6.0F, 0.0F, new CubeDeformation(0.01F))
                .texOffs(118, 1).addBox(-6.0F, -3.5F, -2.5F, 0.0F, 4.0F, 5.0F, new CubeDeformation(0.01F))
                .texOffs(118, 1).mirror().addBox(6.0F, -3.5F, -2.5F, 0.0F, 4.0F, 5.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(85, 40).addBox(-6.0F, 0.5F, -2.5F, 3.0F, 0.0F, 5.0F, new CubeDeformation(0.01F))
                .texOffs(85, 45).mirror().addBox(3.0F, 0.5F, -2.5F, 3.0F, 0.0F, 5.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(85, 32).addBox(-3.0F, -2.5F, -2.5F, 6.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.5F, -3.5F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.5F, -3.5F, 13.0F));

        PartDefinition front_tail = tail.addOrReplaceChild("front_tail", CubeListBuilder.create().texOffs(18, 58).addBox(-2.0F, -1.5F, 0.0F, 3.0F, 3.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition back_tail = front_tail.addOrReplaceChild("back_tail", CubeListBuilder.create().texOffs(72, 37).addBox(-0.5F, -1.5F, 0.0F, 0.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 10.0F));

        PartDefinition cube_r8 = back_tail.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(67, 33).addBox(0.0F, -2.5F, 12.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.5F, -0.025F, 2.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition cube_r9 = back_tail.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(68, 30).addBox(0.0F, -6.5F, 6.0F, 0.0F, 2.0F, 4.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(1.0F, 0.025F, 4.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition cube_r10 = back_tail.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(79, 33).addBox(0.0F, -5.5F, 10.0F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.01F))
                .texOffs(67, 30).addBox(0.0F, -1.5F, 1.0F, 0.0F, 2.0F, 9.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition cube_r11 = back_tail.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(68, 30).mirror().addBox(0.0F, -6.5F, 6.0F, 0.0F, 2.0F, 4.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-2.0F, 0.025F, 4.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition cube_r12 = back_tail.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(79, 33).mirror().addBox(0.0F, -5.5F, 10.0F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-1.0F, 0.0F, 4.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition cube_r13 = back_tail.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(73, 33).addBox(0.0F, -1.5F, 0.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 2.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition left_arm = ALL.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(34, 36).addBox(-2.5F, -1.0F, -2.5F, 4.0F, 11.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(34, 61).addBox(-2.5F, 10.0F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(97, 40).addBox(-0.5F, 12.0F, -2.5F, 0.0F, 4.0F, 5.0F, new CubeDeformation(0.01F))
                .texOffs(110, 27).addBox(-2.5F, -1.0F, -2.5F, 4.0F, 10.0F, 5.0F, new CubeDeformation(0.25F)), PartPose.offset(4.5F, 0.0F, -7.5F));

        PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(52, 36).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(110, 27).addBox(-1.925F, -1.0F, -2.5F, 4.0F, 10.0F, 5.0F, new CubeDeformation(0.75F)), PartPose.offset(3.5F, 3.0F, 8.5F));

        PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(52, 36).mirror().addBox(-2.5F, -1.0F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(72, 44).mirror().addBox(-1.0F, 6.0F, -1.5F, 2.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(110, 27).mirror().addBox(-2.5F, -1.0F, -2.5F, 4.0F, 10.0F, 5.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(-3.5F, 3.0F, 8.5F));

        PartDefinition right_arm = ALL.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(34, 36).mirror().addBox(-1.5F, -1.0F, -2.5F, 4.0F, 17.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(110, 27).mirror().addBox(-1.5F, -1.0F, -2.5F, 4.0F, 10.0F, 5.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(-4.5F, 0.0F, -7.5F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public static LayerDefinition createSkinScarletPirate() {
        return createSkinSevenSeas();
    }

    public static LayerDefinition createSkinCartoon() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 2.0F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -7.0F, -11.0F, 10.0F, 12.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(60, 57).addBox(-5.0F, -7.0F, -11.0F, 10.0F, 12.0F, 24.0F, new CubeDeformation(0.25F))
                .texOffs(0, 56).addBox(5.0F, 5.0F, -11.0F, 0.0F, 2.0F, 24.0F, new CubeDeformation(0.01F))
                .texOffs(0, 56).mirror().addBox(-5.0F, 5.0F, -11.0F, 0.0F, 2.0F, 24.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(76, 110).addBox(-4.0F, -7.75F, -6.0F, 8.0F, 1.0F, 9.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 1.0F, -1.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 36).addBox(-5.0F, -4.0F, -7.0F, 10.0F, 8.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(57, 66).addBox(-5.0F, -4.0F, -1.2F, 10.0F, 8.0F, 1.0F, new CubeDeformation(0.25F))
                .texOffs(28, 87).addBox(-7.0F, -6.0F, -0.7F, 14.0F, 12.0F, 0.0F, new CubeDeformation(0.05F))
                .texOffs(70, 28).addBox(4.0F, -4.0F, -4.0F, 3.0F, 9.0F, 0.0F, new CubeDeformation(0.01F))
                .texOffs(70, 28).mirror().addBox(-7.0F, -4.0F, -4.0F, 3.0F, 9.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(68, 13).addBox(-2.5F, 0.0F, -10.0F, 5.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(109, 126).addBox(-3.5F, 4.0F, -9.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(124, 122).addBox(-5.5F, 4.0F, -9.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.01F))
                .texOffs(124, 122).mirror().addBox(3.5F, 4.0F, -9.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(0.0F, -4.0F, -11.0F));

        PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(48, 86).mirror().addBox(0.0F, -12.5F, 0.0F, 0.0F, 12.0F, 17.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(5.5F, 5.5F, -9.0F, 0.0F, 0.1309F, -0.1745F));

        PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(48, 86).addBox(0.0F, -12.5F, 0.0F, 0.0F, 12.0F, 17.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-5.5F, 5.5F, -9.0F, 0.0F, -0.1309F, 0.1745F));

        PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(18, 51).addBox(-1.0F, -2.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -3.5F, -4.5F));

        PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(26, 51).addBox(-2.0F, -2.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -3.5F, -4.5F));

        PartDefinition left_eyeBall = head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(0, 54).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.025F)), PartPose.offset(2.0F, -1.5F, -7.0F));

        PartDefinition right_eyeBall = head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(0, 54).mirror().addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.025F)).mirror(false), PartPose.offset(-2.0F, -1.5F, -7.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.5F, -3.5F, 13.0F));

        PartDefinition front_tail = tail.addOrReplaceChild("front_tail", CubeListBuilder.create().texOffs(18, 58).addBox(-2.0F, -1.5F, 0.0F, 3.0F, 3.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition back_tail = front_tail.addOrReplaceChild("back_tail", CubeListBuilder.create().texOffs(68, 0).addBox(-2.0F, -1.5F, 0.0F, 3.0F, 3.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 10.0F));

        PartDefinition left_arm = ALL.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(34, 36).addBox(-2.5F, -1.0F, -2.5F, 4.0F, 17.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(4.5F, 0.0F, -7.5F));

        PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(52, 36).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, 3.0F, 8.5F));

        PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(52, 36).mirror().addBox(-2.5F, -1.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.5F, 3.0F, 8.5F));

        PartDefinition right_arm = ALL.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(34, 36).mirror().addBox(-1.5F, -1.0F, -2.5F, 4.0F, 17.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-4.5F, 0.0F, -7.5F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public static LayerDefinition createSkinPizzaChef() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 2.0F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -7.0F, -11.0F, 10.0F, 12.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(60, 57).addBox(-5.0F, -7.0F, -11.0F, 10.0F, 12.0F, 24.0F, new CubeDeformation(0.25F))
                .texOffs(0, 56).addBox(5.0F, 5.0F, -11.0F, 0.0F, 2.0F, 24.0F, new CubeDeformation(0.01F))
                .texOffs(0, 56).mirror().addBox(-5.0F, 5.0F, -11.0F, 0.0F, 2.0F, 24.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(76, 110).addBox(-4.0F, -7.75F, -6.0F, 8.0F, 1.0F, 9.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 1.0F, -1.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 36).addBox(-5.0F, -4.0F, -7.0F, 10.0F, 8.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(57, 66).addBox(-5.0F, -4.0F, -1.2F, 10.0F, 8.0F, 1.0F, new CubeDeformation(0.25F))
                .texOffs(28, 87).addBox(-7.0F, -6.0F, -0.7F, 14.0F, 12.0F, 0.0F, new CubeDeformation(0.05F))
                .texOffs(70, 28).addBox(4.0F, -4.0F, -4.0F, 3.0F, 9.0F, 0.0F, new CubeDeformation(0.01F))
                .texOffs(70, 28).mirror().addBox(-7.0F, -4.0F, -4.0F, 3.0F, 9.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(68, 13).addBox(-2.5F, 0.0F, -10.0F, 5.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(109, 126).addBox(-3.5F, 4.0F, -9.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(124, 122).addBox(-5.5F, 4.0F, -9.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.01F))
                .texOffs(124, 122).mirror().addBox(3.5F, 4.0F, -9.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(64, 120).addBox(2.5F, -1.0F, -10.0F, 5.0F, 6.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(64, 120).mirror().addBox(-7.5F, -1.0F, -10.0F, 5.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(96, 4).addBox(-3.5F, -15.0F, -7.0F, 7.0F, 11.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, -11.0F));

        PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(21, 116).addBox(-4.0F, -1.0F, -19.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.5F, 4.5F, -22.0F, 0.0F, -2.3562F, 0.0F));

        PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(48, 86).mirror().addBox(0.0F, -12.5F, 0.0F, 0.0F, 12.0F, 17.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(5.5F, 5.5F, -9.0F, 0.0F, 0.1309F, -0.1745F));

        PartDefinition cube_r3 = head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(48, 86).addBox(0.0F, -12.5F, 0.0F, 0.0F, 12.0F, 17.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-5.5F, 5.5F, -9.0F, 0.0F, -0.1309F, 0.1745F));

        PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(18, 51).addBox(-1.0F, -2.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -3.5F, -4.5F));

        PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(26, 51).addBox(-2.0F, -2.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -3.5F, -4.5F));

        PartDefinition left_eyeBall = head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(0, 54).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.025F)), PartPose.offset(2.0F, -1.5F, -7.0F));

        PartDefinition right_eyeBall = head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(0, 54).mirror().addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.025F)).mirror(false), PartPose.offset(-2.0F, -1.5F, -7.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.5F, -3.5F, 13.0F));

        PartDefinition front_tail = tail.addOrReplaceChild("front_tail", CubeListBuilder.create().texOffs(18, 58).addBox(-2.0F, -1.5F, 0.0F, 3.0F, 3.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition back_tail = front_tail.addOrReplaceChild("back_tail", CubeListBuilder.create().texOffs(68, 0).addBox(-2.0F, -1.5F, 0.0F, 3.0F, 3.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 10.0F));

        PartDefinition left_arm = ALL.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(34, 36).addBox(-2.5F, -1.0F, -2.5F, 4.0F, 17.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(4.5F, 0.0F, -7.5F));

        PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(52, 36).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, 3.0F, 8.5F));

        PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(52, 36).mirror().addBox(-2.5F, -1.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.5F, 3.0F, 8.5F));

        PartDefinition right_arm = ALL.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(34, 36).mirror().addBox(-1.5F, -1.0F, -2.5F, 4.0F, 17.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-4.5F, 0.0F, -7.5F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }
}