package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.client.animation.BoaAnimations;

public class BoaModel<T extends BoaEntity> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "boa_default"), "main");

    private final ModelPart ALL2;
    private final ModelPart ALL;
    private final ModelPart head;
    private final ModelPart mouth;
    private final ModelPart mouth_up;
    private final ModelPart mouth_down;
    private final ModelPart body_0;
    private final ModelPart body_1;
    private final ModelPart body_2;
    private final ModelPart body_3;
    private final ModelPart body_4;
    private final ModelPart body_5;
    private final ModelPart body_6;

    public BoaModel(ModelPart root) {
        this.ALL2 = root.getChild("ALL2");
        this.ALL = this.ALL2.getChild("ALL");
        this.head = this.ALL.getChild("head");
        this.mouth = this.head.getChild("mouth");
        this.mouth_up = this.mouth.getChild("mouth_up");
        this.mouth_down = this.mouth.getChild("mouth_down");
        this.body_0 = this.head.getChild("body_0");
        this.body_1 = this.body_0.getChild("body_1");
        this.body_2 = this.body_1.getChild("body_2");
        this.body_3 = this.body_2.getChild("body_3");
        this.body_4 = this.body_3.getChild("body_4");
        this.body_5 = this.body_4.getChild("body_5");
        this.body_6 = this.body_5.getChild("body_6");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // CORRECTIF JONCTION : le cumul d'offsets Z (ALL2 + ALL + head) placait la
        // tete ~47px DEVANT la hitbox. Visible depuis que body_0..6 sont des entites
        // separees (queue) et ne "tirent" plus le modele vers l'arriere. On ramene
        // ALL2.z pour recaler la tete sur sa hitbox. Ajuste cette seule valeur si
        // besoin : augmente pour avancer la tete, diminue pour la reculer.
        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 20.0F, 102.5F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -49.0F));

        PartDefinition head = ALL.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, -48.0F));

        PartDefinition mouth = head.addOrReplaceChild("mouth", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition mouth_up = mouth.addOrReplaceChild("mouth_up", CubeListBuilder.create().texOffs(0, 82).addBox(-3.5F, -2.0F, -10.0F, 7.0F, 2.0F, 10.0F, new CubeDeformation(0.001F))
                .texOffs(40, 0).addBox(-5.5F, -2.0F, -7.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.001F))
                .texOffs(40, 0).mirror().addBox(3.5F, -2.0F, -7.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.001F)).mirror(false)
                .texOffs(34, 87).addBox(-3.5F, 0.0F, -10.0F, 7.0F, 1.0F, 10.0F, new CubeDeformation(0.001F))
                .texOffs(108, 114).addBox(-3.4F, 1.0F, -10.0F, 0.0F, 3.0F, 10.0F, new CubeDeformation(0.001F))
                .texOffs(108, 114).mirror().addBox(3.4F, 1.0F, -10.0F, 0.0F, 3.0F, 10.0F, new CubeDeformation(0.001F)).mirror(false)
                .texOffs(101, 37).mirror().addBox(-3.0F, -5.0F, -9.0F, 0.0F, 3.0F, 11.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(101, 37).addBox(3.0F, -5.0F, -9.0F, 0.0F, 3.0F, 11.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, -1.0F, 0.0F));

        PartDefinition cube_r1 = mouth_up.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(171, 219).addBox(-2.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.01F))
                .texOffs(179, 239).addBox(-2.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(4.5F, -0.5F, -1.0F, 0.2533F, -0.7519F, -0.3622F));

        PartDefinition cube_r2 = mouth_up.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(171, 219).mirror().addBox(-8.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(179, 239).mirror().addBox(-8.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-4.5F, -0.5F, -1.0F, 0.2533F, 0.7519F, 0.3622F));

        PartDefinition mouth_down = mouth.addOrReplaceChild("mouth_down", CubeListBuilder.create().texOffs(0, 69).addBox(-3.5F, 0.0F, -10.0F, 7.0F, 3.0F, 10.0F, new CubeDeformation(0.001F))
                .texOffs(0, 4).addBox(-3.5F, -1.0F, 0.0F, 7.0F, 1.0F, 0.0F, new CubeDeformation(0.001F))
                .texOffs(68, 87).addBox(-3.5F, -1.0F, -10.0F, 7.0F, 1.0F, 10.0F, new CubeDeformation(0.001F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body_0 = head.addOrReplaceChild("body_0", CubeListBuilder.create().texOffs(48, 0).addBox(-3.5F, -3.0F, -1.0F, 7.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 1.0F));

        PartDefinition cube_r3 = body_0.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(171, 219).addBox(-2.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(4.5F, -1.5F, 8.0F, 0.2533F, -0.7519F, -0.3622F));

        PartDefinition cube_r4 = body_0.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(171, 219).mirror().addBox(-8.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-4.5F, -1.5F, 8.0F, 0.2533F, 0.7519F, 0.3622F));

        PartDefinition cube_r5 = body_0.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(179, 239).mirror().addBox(-8.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-4.5F, -1.5F, 9.0F, 0.2533F, 0.7519F, 0.3622F));

        PartDefinition cube_r6 = body_0.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(179, 239).addBox(-2.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(4.5F, -1.5F, 9.0F, 0.2533F, -0.7519F, -0.3622F));

        PartDefinition body_1 = body_0.addOrReplaceChild("body_1", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -1.0F, 8.0F, 7.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(135, 138).addBox(0.0F, -14.0F, -1.0F, 0.0F, 10.0F, 15.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 0.0F, 16.0F));

        PartDefinition cube_r7 = body_1.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(205, 180).addBox(5.0F, -11.0F, -1.0F, 11.0F, 11.0F, 0.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(-9.0F, 5.5F, 2.0F, -0.0128F, -0.4635F, -0.5778F));

        PartDefinition body_2 = body_1.addOrReplaceChild("body_2", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -1.0F, 8.0F, 7.0F, 16.0F, new CubeDeformation(-0.001F))
                .texOffs(135, 138).addBox(0.0F, -10.0F, -1.0F, 0.0F, 6.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 16.0F));

        PartDefinition body_3 = body_2.addOrReplaceChild("body_3", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-4.0F, -4.0F, 0.0F, 8.0F, 7.0F, 16.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(135, 138).addBox(0.0F, -10.0F, 0.0F, 0.0F, 6.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 15.0F));

        PartDefinition cube_r8 = body_3.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(157, 199).addBox(6.0F, -11.0F, -1.0F, 10.0F, 11.0F, 0.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(-8.0F, -2.5F, 3.0F, -0.1117F, -0.0577F, 0.2342F));

        PartDefinition cube_r9 = body_3.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(88, 151).addBox(6.0F, -12.0F, -1.0F, 13.0F, 12.0F, 0.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(3.0F, 1.5F, -5.0F, 1.0653F, -1.3361F, -2.7575F));

        PartDefinition body_4 = body_3.addOrReplaceChild("body_4", CubeListBuilder.create().texOffs(48, 0).addBox(-3.5F, -3.0F, 0.0F, 7.0F, 6.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(146, 78).addBox(0.0F, -7.0F, 3.0F, 0.0F, 4.0F, 9.0F, new CubeDeformation(0.001F)), PartPose.offset(0.0F, 0.0F, 16.0F));

        PartDefinition cube_r10 = body_4.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(205, 202).addBox(6.0F, -7.0F, -1.0F, 8.0F, 7.0F, 0.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(-3.0F, -5.5F, 1.0F, -0.5628F, -0.7972F, 0.7507F));

        PartDefinition body_5 = body_4.addOrReplaceChild("body_5", CubeListBuilder.create().texOffs(48, 0).mirror().addBox(-3.5F, -3.0F, 0.0F, 7.0F, 6.0F, 16.0F, new CubeDeformation(-0.001F)).mirror(false)
                .texOffs(146, 78).addBox(0.0F, -7.0F, 3.0F, 0.0F, 4.0F, 9.0F, new CubeDeformation(0.001F)), PartPose.offset(0.0F, 0.0F, 16.0F));

        PartDefinition body_6 = body_5.addOrReplaceChild("body_6", CubeListBuilder.create().texOffs(48, 66).addBox(-2.5F, -3.0F, 0.0F, 5.0F, 5.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(135, 138).addBox(0.0F, -9.0F, 0.0F, 0.0F, 6.0F, 15.0F, new CubeDeformation(0.001F))
                .texOffs(14, 171).addBox(-6.0F, 0.0F, 11.0F, 12.0F, 0.0F, 25.0F, new CubeDeformation(0.001F)), PartPose.offset(0.0F, 1.0F, 16.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(T boa, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Copie d'Alex : la tete ne fait pas d'oscillation laterale custom. Elle
        // suit le corps (yHeadRot gere par l'entite). Pas de balancement artificiel.
        this.head.yRot = 0f;
        this.head.xRot = 0f;

        // body_0-6 sont rendus comme des entites BoaTailPart separees
        this.body_0.visible = false;
        this.body_1.visible = false;
        this.body_2.visible = false;
        this.body_3.visible = false;
        this.body_4.visible = false;
        this.body_5.visible = false;
        this.body_6.visible = false;

        if (boa.isSitting()) {
            this.animate(boa.sittingAnimationState, BoaAnimations.SIT, ageInTicks, 1.0f);
            return;
        }

        this.animate(boa.idleAnimationState, BoaAnimations.MISC_IDLE, ageInTicks, 1.0f);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root() {
        return this.ALL2;
    }
}