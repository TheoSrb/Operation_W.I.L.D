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
import net.minecraft.world.entity.LivingEntity;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.animation.TigerSharkAnimations;
import net.tiew.operationWild.entity.animals.aquatic.TigerSharkEntity;

public class TigerSharkModel<T extends TigerSharkEntity> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "tiger_shark_default"), "main");
    private final ModelPart ALL;
    private final ModelPart body;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart tail4;
    private final ModelPart left_fin_2;
    private final ModelPart right_fin_2;
    private final ModelPart left_Fin;
    private final ModelPart right_Fin;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart mouth_Up;
    private final ModelPart mouth_Dwn;

    private float smoothedXRot = 0.0f;

    public TigerSharkModel(ModelPart root) {
        this.ALL = root.getChild("ALL");
        this.body = this.ALL.getChild("body");
        this.tail1 = this.body.getChild("tail1");
        this.tail2 = this.tail1.getChild("tail2");
        this.tail3 = this.tail2.getChild("tail3");
        this.tail4 = this.tail1.getChild("tail4");
        this.left_fin_2 = this.tail4.getChild("left_fin_2");
        this.right_fin_2 = this.tail4.getChild("right_fin_2");
        this.left_Fin = this.body.getChild("left_Fin");
        this.right_Fin = this.body.getChild("right_Fin");
        this.neck = this.body.getChild("neck");
        this.head = this.neck.getChild("head");
        this.mouth_Up = this.head.getChild("mouth_Up");
        this.mouth_Dwn = this.head.getChild("mouth_Dwn");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL = partdefinition.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 18.0F, 6.0F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -6.0F, -12.0F, 16.0F, 12.0F, 25.0F, new CubeDeformation(0.0F))
                .texOffs(0, 219).addBox(-8.0F, -6.0F, -12.0F, 16.0F, 12.0F, 25.0F, new CubeDeformation(0.5F))
                .texOffs(21, 199).addBox(-5.0F, -7.0F, -11.0F, 10.0F, 1.0F, 9.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, -12.0F));

        PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(24, 17).addBox(-1.0F, -23.0F, -7.0F, 2.0F, 11.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 11.0F, -6.0F, -0.5672F, 0.0F, 0.0F));

        PartDefinition tail1 = body.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(0, 60).addBox(-6.0F, -5.0F, 0.0F, 12.0F, 11.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 13.0F));

        PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(82, 0).addBox(-5.0F, -4.0F, 0.0F, 9.0F, 8.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 1.0F, 16.0F));

        PartDefinition cube_r2 = tail2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(62, 37).addBox(-0.5F, -20.0F, 8.0F, 0.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, -12.0F, -0.48F, 0.0F, 0.0F));

        PartDefinition tail3 = tail2.addOrReplaceChild("tail3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 14.0F, 0.3491F, 0.0F, 0.0F));

        PartDefinition cube_r3 = tail3.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(110, 42).addBox(-0.525F, -22.5F, 5.0F, 0.0F, 12.0F, 23.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -16.0F, -0.9163F, 0.0F, 0.0F));

        PartDefinition cube_r4 = tail3.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(104, 12).addBox(-0.5F, -4.5F, 15.0F, 0.0F, 11.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, -17.0F, 0.3054F, 0.0F, 0.0F));

        PartDefinition tail4 = tail1.addOrReplaceChild("tail4", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_fin_2 = tail4.addOrReplaceChild("left_fin_2", CubeListBuilder.create().texOffs(104, 103).addBox(-1.0F, 0.0F, -3.0F, 9.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 4.0F, 5.0F));

        PartDefinition right_fin_2 = tail4.addOrReplaceChild("right_fin_2", CubeListBuilder.create().texOffs(104, 103).mirror().addBox(-7.0F, 0.0F, -3.0F, 9.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-6.0F, 4.0F, 5.0F));

        PartDefinition left_Fin = body.addOrReplaceChild("left_Fin", CubeListBuilder.create().texOffs(79, 121).addBox(-2.0F, -1.0F, -5.0F, 13.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 4.0F, -2.0F));

        PartDefinition right_Fin = body.addOrReplaceChild("right_Fin", CubeListBuilder.create().texOffs(79, 121).mirror().addBox(-11.0F, -1.0F, -5.0F, 13.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-8.0F, 4.0F, -2.0F));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -13.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition mouth_Up = head.addOrReplaceChild("mouth_Up", CubeListBuilder.create().texOffs(5, 114).addBox(-7.0F, -5.0F, -15.0F, 14.0F, 7.0F, 17.0F, new CubeDeformation(0.0F))
                .texOffs(78, 39).addBox(-4.0F, 2.0F, -10.9F, 0.0F, 2.0F, 12.0F, new CubeDeformation(0.05F))
                .texOffs(74, 55).addBox(-6.0F, 2.0F, -10.9F, 12.0F, 2.0F, 0.0F, new CubeDeformation(0.05F))
                .texOffs(78, 39).mirror().addBox(4.0F, 2.0F, -10.9F, 0.0F, 2.0F, 12.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offset(0.0F, -1.0F, -1.0F));

        PartDefinition mouth_Dwn = head.addOrReplaceChild("mouth_Dwn", CubeListBuilder.create().texOffs(3, 90).addBox(-5.0F, -1.0F, -11.0F, 10.0F, 4.0F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(90, 252).addBox(-8.0F, -2.0F, -10.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(89, 247).addBox(-10.0F, -1.5F, -9.0F, 20.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(79, 39).addBox(-4.0F, -2.0F, -10.0F, 0.0F, 1.0F, 12.0F, new CubeDeformation(0.05F))
                .texOffs(79, 39).mirror().addBox(4.0F, -2.0F, -10.0F, 0.0F, 1.0F, 12.0F, new CubeDeformation(0.05F)).mirror(false)
                .texOffs(75, 58).addBox(-4.0F, -3.0F, -10.0F, 8.0F, 2.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offset(0.0F, 2.0F, -1.0F));

        PartDefinition cube_r5 = mouth_Dwn.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(90, 215).addBox(-8.0F, -15.0F, -23.0F, 0.0F, 15.0F, 14.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-0.5F, 5.0F, 13.0F, 0.0341F, 0.1264F, 0.264F));

        PartDefinition cube_r6 = mouth_Dwn.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(90, 215).mirror().addBox(8.0F, -15.0F, -23.0F, 0.0F, 15.0F, 14.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(0.5F, 5.0F, 13.0F, 0.0341F, -0.1264F, -0.264F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(TigerSharkEntity tigerShark, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.applyHeadRotation(netHeadYaw, headPitch);

        if (!tigerShark.isGoingToHitTarget()) {
            float targetRotation = (float) -(tigerShark.getLookAngle().y * 1.5);
            float smoothingFactor = 0.1f;
            this.smoothedXRot = Mth.lerp(smoothingFactor, this.smoothedXRot, targetRotation);
            this.ALL.xRot = this.smoothedXRot / 2f;
        }

        if (tigerShark.isSmellingBlood) {
            this.mouth_Dwn.xRot += (float) Math.toRadians(10);
        }

        if (tigerShark.isShakingPrey()) {
            long time = System.currentTimeMillis();
            float frequency = 4.0f;
            float amplitude = 60.0f;

            float angle = (float) Math.sin((time / 1000.0) * Math.PI * frequency) * amplitude;
            this.head.yRot = (float) Math.toRadians(angle);
        }

        if (tigerShark.isSleeping()) {
            this.animate(tigerShark.sleepAnimationState, TigerSharkAnimations.SIT, ageInTicks, 1.0f);
            return;
        }

        if (tigerShark.isAttacking() || tigerShark.getState() == 1) {
            this.animate(tigerShark.attackAnimationState, TigerSharkAnimations.ATTACK_STRIKE, ageInTicks, 0.5f);
        } else {
            if (tigerShark.isGoingToHitTarget()) {
                LivingEntity target = tigerShark.getTarget();
                if (target != null) {
                    double distance = tigerShark.distanceTo(target);
                    double $$0 = Math.max(0, -2 * distance + 40);
                    this.mouth_Up.xRot = (float) Math.toRadians(-$$0);
                    this.mouth_Dwn.xRot = (float) Math.toRadians($$0);

                    this.ALL.zRot = (float) Math.toRadians($$0);;
                    this.head.xRot = -(this.smoothedXRot / 2);
                }
            }
        }
        if (tigerShark.isSitting()) {
            this.animate(tigerShark.sittingAnimationState, TigerSharkAnimations.SIT, ageInTicks, 1.0f);
            return;
        }
        this.animateWalk(TigerSharkAnimations.MOVE_WALK, limbSwing, limbSwingAmount, 4f, 2.75f);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        ALL.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch) {
        pNetHeadYaw = Mth.clamp(pNetHeadYaw, -30.0F, 30.0F);
        pHeadPitch = Mth.clamp(pHeadPitch, -30.0F, 30.0F);

        this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);
    }

    @Override
    public ModelPart root() {
        return ALL;
    }
}
