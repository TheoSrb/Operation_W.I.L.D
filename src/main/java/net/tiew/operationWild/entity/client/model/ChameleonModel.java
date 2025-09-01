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
import net.tiew.operationWild.entity.client.animation.ChameleonAnimations;
import net.tiew.operationWild.entity.animals.terrestrial.ChameleonEntity;

public class ChameleonModel<T extends ChameleonEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "chameleon_default"), "main");

    private final ModelPart ALL;
    private final ModelPart left_leg;
    private final ModelPart right_leg;
    private final ModelPart left_arm;
    private final ModelPart right_arm;
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart head;

    public ChameleonModel(ModelPart root) {
        this.ALL = root.getChild("ALL");
        this.left_leg = this.ALL.getChild("left_leg");
        this.right_leg = this.ALL.getChild("right_leg");
        this.left_arm = this.ALL.getChild("left_arm");
        this.right_arm = this.ALL.getChild("right_arm");
        this.body = this.ALL.getChild("body");
        this.tail = this.body.getChild("tail");
        this.head = this.body.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL = partdefinition.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 19.0F, 0.0F));

        PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(42, 14).addBox(0.0F, 0.0F, -1.5F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.01F))
                .texOffs(43, 16).addBox(2.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.01F))
                .texOffs(43, 19).addBox(5.0F, 0.0F, 0.5F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.01F))
                .texOffs(42, 17).addBox(4.0F, 0.0F, -1.5F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.01F))
                .texOffs(43, 20).addBox(5.0F, 0.0F, -2.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.01F))
                .texOffs(43, 21).addBox(6.0F, 0.0F, -0.5F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(3.0F, 2.0F, 3.5F, 0.0F, 0.0F, 0.4363F));

        PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(42, 14).mirror().addBox(-2.0F, 0.0F, -1.5F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(43, 16).mirror().addBox(-4.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(43, 19).mirror().addBox(-6.0F, 0.0F, 0.5F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(42, 17).mirror().addBox(-6.0F, 0.0F, -1.5F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(43, 20).mirror().addBox(-7.0F, 0.0F, -2.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(43, 21).mirror().addBox(-7.0F, 0.0F, -0.5F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-3.0F, 2.0F, 3.5F, 0.0F, 0.0F, -0.4363F));

        PartDefinition left_arm = ALL.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(42, 14).addBox(0.0F, 0.0F, -1.5F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.01F))
                .texOffs(43, 16).addBox(2.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.01F))
                .texOffs(43, 19).addBox(5.0F, 0.0F, 0.5F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.01F))
                .texOffs(42, 17).addBox(4.0F, 0.0F, -1.5F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.01F))
                .texOffs(43, 20).addBox(5.0F, 0.0F, -2.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.01F))
                .texOffs(43, 21).addBox(6.0F, 0.0F, -0.5F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(3.0F, 2.0F, -2.5F, 0.0F, 0.0F, 0.4363F));

        PartDefinition right_arm = ALL.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(42, 14).mirror().addBox(-2.0F, 0.0F, -1.5F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(43, 16).mirror().addBox(-4.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(43, 19).mirror().addBox(-6.0F, 0.0F, 0.5F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(42, 17).mirror().addBox(-6.0F, 0.0F, -1.5F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(43, 20).mirror().addBox(-7.0F, 0.0F, -2.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(43, 21).mirror().addBox(-7.0F, 0.0F, -0.5F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-3.0F, 2.0F, -2.5F, 0.0F, 0.0F, -0.4363F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -5.0F, 6.0F, 5.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(32, 25).addBox(0.0F, -4.0F, -5.0F, 0.0F, 1.0F, 3.0F, new CubeDeformation(0.05F))
                .texOffs(24, 24).addBox(0.0F, -4.0F, 1.0F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.05F)), PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 28).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 6.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, -3.0F, -6.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(20, 37).addBox(0.0F, -4.0F, -5.0F, 0.0F, 1.0F, 5.0F, new CubeDeformation(0.01F))
                .texOffs(19, 34).addBox(0.0F, -7.0F, -4.0F, 0.0F, 3.0F, 5.0F, new CubeDeformation(0.01F))
                .texOffs(19, 35).addBox(0.0F, -8.0F, -2.0F, 0.0F, 1.0F, 3.0F, new CubeDeformation(0.01F))
                .texOffs(34, 5).addBox(3.0F, -1.5F, -4.5F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(34, 5).mirror().addBox(-4.0F, -1.5F, -4.5F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -1.0F, -5.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(ChameleonEntity chameleon, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        if (chameleon.isBaby()) {
            float maturationPercent = (float) chameleon.getMaturationPercentage() / 100f;
            float headScale = 2f - (2f - 1.0f) * maturationPercent;

            this.head.xScale *= headScale;
            this.head.yScale *= headScale;
            this.head.zScale *= headScale;
        }

        if (chameleon.isClimbing() || chameleon.isStopingClimbing) this.ALL.xRot = (float) Math.toRadians(-9 * chameleon.climbingTimer);

        this.applyHeadRotation(netHeadYaw, headPitch);
        this.animate(chameleon.idleAnimationState, ChameleonAnimations.MISC_IDLE, ageInTicks, 1.0f);

        float walkLimbSwing = limbSwing;
        float walkLimbSwingAmount = limbSwingAmount;

        if (chameleon.isClimbing() && chameleon.getDeltaMovement().y > 0.01) {
            walkLimbSwing = ageInTicks * 0.3332f;
            walkLimbSwingAmount = 0.6f;
        }

        this.animateWalk(ChameleonAnimations.MOVE_WALK, walkLimbSwing, walkLimbSwingAmount, 5f, 5f);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.ALL.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch) {
        pNetHeadYaw = Mth.clamp(pNetHeadYaw, -30.0F, 30.0F);
        pHeadPitch = Mth.clamp(pHeadPitch, -30.0F, 30.0F);

        this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);
    }

    @Override
    public ModelPart root() {
        return this.ALL;
    }
}
