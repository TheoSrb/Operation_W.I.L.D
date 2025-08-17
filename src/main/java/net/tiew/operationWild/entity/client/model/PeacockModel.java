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
import net.tiew.operationWild.entity.client.animation.ElephantAnimations;
import net.tiew.operationWild.entity.client.animation.PeacockAnimations;
import net.tiew.operationWild.entity.custom.living.PeacockEntity;

public class PeacockModel<T extends PeacockEntity> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "peacock_default"), "main");
    private final ModelPart ALL2;
    private final ModelPart ALL;
    private final ModelPart left_leg;
    private final ModelPart right_leg;
    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart left_wing;
    private final ModelPart right_wing;
    private final ModelPart wheel;
    private final ModelPart wheel_right;
    private final ModelPart wheel_right2;
    private final ModelPart wheel_left;
    private final ModelPart wheel_left2;

    public PeacockModel(ModelPart root) {
        this.ALL2 = root.getChild("ALL2");
        this.ALL = this.ALL2.getChild("ALL");
        this.left_leg = this.ALL.getChild("left_leg");
        this.right_leg = this.ALL.getChild("right_leg");
        this.body = this.ALL.getChild("body");
        this.neck = this.body.getChild("neck");
        this.head = this.neck.getChild("head");
        this.left_wing = this.body.getChild("left_wing");
        this.right_wing = this.body.getChild("right_wing");
        this.wheel = this.body.getChild("wheel");
        this.wheel_right = this.wheel.getChild("wheel_right");
        this.wheel_right2 = this.wheel_right.getChild("wheel_right2");
        this.wheel_left = this.wheel.getChild("wheel_left");
        this.wheel_left2 = this.wheel_left.getChild("wheel_left2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 17.0F, -1.0F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 33).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 29).addBox(-1.5F, 3.9F, -4.0F, 3.0F, 0.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(1.5F, 3.0F, 0.0F));

        PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 29).mirror().addBox(-1.5F, 3.9F, -4.0F, 3.0F, 0.0F, 4.0F, new CubeDeformation(0.1F)).mirror(false)
                .texOffs(0, 33).mirror().addBox(-0.5F, 0.0F, 0.0F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.5F, 3.0F, 0.0F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 5.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(0, 114).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 5.0F, 9.0F, new CubeDeformation(0.05F))
                .texOffs(0, 47).addBox(-3.0F, -2.5F, -1.0F, 6.0F, 1.0F, 5.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(30, 4).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 51).addBox(1.525F, -8.0F, -2.0F, 0.0F, 6.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(3, 57).addBox(-1.5F, -7.025F, -2.0F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 51).mirror().addBox(-1.525F, -8.0F, -2.0F, 0.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, -3.5F));

        PartDefinition cube_r1 = neck.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(28, 14).addBox(-2.0F, -6.0F, -3.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.75F, 3.0F, 1.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 21).addBox(-1.0F, -3.0F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(115, 116).addBox(-2.0F, -2.0F, -2.5F, 5.0F, 6.0F, 0.0F, new CubeDeformation(0.05F))
                .texOffs(42, 10).addBox(-1.0F, -1.0F, -3.5F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, -6.0F, 0.0F));

        PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 14).addBox(0.9F, -8.0F, -1.0F, 0.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4F, -2.0F, 0.5F, -0.3927F, 0.0F, 0.0F));

        PartDefinition cube_r3 = head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(82, 116).addBox(-2.1F, -4.0F, -2.0F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1F, -2.5F, -0.5F, 0.1299F, -0.252F, 0.1878F));

        PartDefinition cube_r4 = head.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(115, 100).addBox(-0.1F, -7.0F, 0.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.1F, -5.5F, -2.0F, -0.6373F, -0.5475F, 0.2073F));

        PartDefinition cube_r5 = head.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(85, 105).addBox(-1.1F, -6.0F, -1.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1F, -3.0F, -1.0F, -0.2982F, -0.3723F, 0.4249F));

        PartDefinition cube_r6 = head.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(46, 118).addBox(-4.1F, -1.0F, -3.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6F, -2.5F, -1.0F, 0.2123F, -0.045F, 0.1707F));

        PartDefinition left_wing = body.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(14, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(83, 107).addBox(-0.5F, 1.5F, -11.5F, 1.0F, 1.0F, 20.0F, new CubeDeformation(-0.1F))
                .texOffs(109, 118).addBox(-0.5F, -1.3F, -11.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.1F))
                .texOffs(110, 109).addBox(-1.0F, 0.2F, -13.8F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(109, 118).addBox(-0.5F, -1.3F, -15.1F, 1.0F, 4.0F, 1.0F, new CubeDeformation(-0.1F))
                .texOffs(100, 124).addBox(-0.5F, -1.3F, -14.3F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.1F)), PartPose.offset(3.5F, -1.5F, -4.0F));

        PartDefinition right_wing = body.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(14, 14).mirror().addBox(-0.5F, -0.5F, 0.0F, 1.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.5F, -1.5F, -4.0F));

        PartDefinition wheel = body.addOrReplaceChild("wheel", CubeListBuilder.create().texOffs(31, 47).mirror().addBox(-2.5F, -25.0F, 0.0F, 5.0F, 13.0F, 0.0F, new CubeDeformation(0.1F)).mirror(false)
                .texOffs(31, 47).mirror().addBox(-2.5F, -15.0F, 0.05F, 5.0F, 13.0F, 0.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.0F, 2.0F, -1.8326F, 0.0F, 0.0F));

        PartDefinition wheel_right = wheel.addOrReplaceChild("wheel_right", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0436F, 0.0F, 0.3927F));

        PartDefinition cube_r7 = wheel_right.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(31, 47).addBox(-2.5F, -13.0F, 0.0F, 5.0F, 13.0F, 0.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3927F));

        PartDefinition cube_r8 = wheel_right.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(31, 47).mirror().addBox(-2.5F, -13.0F, 0.0F, 5.0F, 13.0F, 0.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, -0.0213F, -0.2609F));

        PartDefinition cube_r9 = wheel_right.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(31, 47).mirror().addBox(-2.5F, -13.0F, 0.0F, 5.0F, 13.0F, 0.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(-4.0F, -9.0F, 0.05F, 0.0F, 0.0F, -0.3927F));

        PartDefinition wheel_right2 = wheel_right.addOrReplaceChild("wheel_right2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0436F, 0.0F, 0.5236F));

        PartDefinition cube_r10 = wheel_right2.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(31, 47).mirror().addBox(-2.5F, -13.0F, 0.0F, 5.0F, 13.0F, 0.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(-4.0F, -4.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition cube_r11 = wheel_right2.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(31, 47).addBox(-2.5F, -13.0F, 0.0F, 5.0F, 13.0F, 0.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.1781F));

        PartDefinition wheel_left = wheel.addOrReplaceChild("wheel_left", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3927F));

        PartDefinition cube_r12 = wheel_left.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(31, 47).addBox(-2.5F, -13.0F, 0.0F, 5.0F, 13.0F, 0.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.0213F, 0.2609F));

        PartDefinition cube_r13 = wheel_left.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(31, 47).addBox(-2.5F, -13.0F, 0.0F, 5.0F, 13.0F, 0.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(4.0F, -9.0F, 0.05F, 0.0F, 0.0F, 0.3927F));

        PartDefinition cube_r14 = wheel_left.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(31, 47).mirror().addBox(-2.5F, -13.0F, 0.0F, 5.0F, 13.0F, 0.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F));

        PartDefinition wheel_left2 = wheel_left.addOrReplaceChild("wheel_left2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0436F, 0.0F, -0.5236F));

        PartDefinition cube_r15 = wheel_left2.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(31, 47).addBox(-2.5F, -13.0F, 0.0F, 5.0F, 13.0F, 0.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(4.0F, -4.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition cube_r16 = wheel_left2.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(31, 47).mirror().addBox(-2.5F, -13.0F, 0.0F, 5.0F, 13.0F, 0.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.1781F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(PeacockEntity peacock, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        if (peacock.isBaby()) {
            float maturationPercent = (float) peacock.getMaturationPercentage() / 100f;
            float headScale = 2f - (2f - 1.0f) * maturationPercent;

            this.head.xScale *= headScale;
            this.head.yScale *= headScale;
            this.head.zScale *= headScale;
        }
        this.applyHeadRotation(netHeadYaw, headPitch);

        if (peacock.transitionIdleSit.isStarted()) {
            this.animate(peacock.transitionIdleSit, PeacockAnimations.TRANSITION_IDLE_SIT, ageInTicks, 1.0f);
            return;
        }

        if (peacock.transitionSitIdle.isStarted()) {
            this.animate(peacock.transitionSitIdle, PeacockAnimations.TRANSITION_SIT_IDLE, ageInTicks, 1.0f);
            return;
        }

        if (peacock.isDeploying()) {
            this.animate(peacock.deployingAnimationState, PeacockAnimations.DEPLOY, ageInTicks, 1.0f);
            return;
        }
        if (peacock.isAttacking()) {
            this.animate(peacock.attackAnimationState, PeacockAnimations.ATTACK_STRIKE, ageInTicks, 1.0f);
        }
        if (peacock.isStayingDeploying()) this.animate(peacock.stayingDeployingAnimationState, PeacockAnimations.WHEEL, ageInTicks, 1.0f);
        if (peacock.isStoppingDeploying()) this.animate(peacock.stoppingDeployingAnimationState, PeacockAnimations.STOP_DEPLOY, ageInTicks, 1.0f);
        if (peacock.isSitting()) {
            this.animate(peacock.sittingAnimationState, PeacockAnimations.SIT, ageInTicks, 1.0f);
            return;
        }
        this.animate(peacock.idleAnimationState, PeacockAnimations.MISC_IDLE, ageInTicks, 1.0f);
        this.animateWalk(PeacockAnimations.MOVE_WALK, limbSwing, limbSwingAmount, 4.25f, 3.5f);
        if (peacock.isCanFlee() || peacock.isRunning() || peacock.getState() == 2) this.animateWalk(PeacockAnimations.MOVE_RUN, limbSwing, limbSwingAmount, 0.01f, 1.0f);

        if (peacock.isFearEntities()) {
            this.animate(peacock.fearAnimationState, PeacockAnimations.FEAR, ageInTicks, 1.0f);
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch) {
        pNetHeadYaw = Mth.clamp(pNetHeadYaw, -30.0F, 30.0F);
        pHeadPitch = Mth.clamp(pHeadPitch, -30.0F, 30.0F);

        this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);
    }

    @Override
    public ModelPart root() {
        return ALL2;
    }
}
