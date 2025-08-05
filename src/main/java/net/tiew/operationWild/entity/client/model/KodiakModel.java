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
import net.tiew.operationWild.entity.client.animation.KodiakAnimations;
import net.tiew.operationWild.entity.custom.living.KodiakEntity;

public class KodiakModel<T extends KodiakEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "kodiak_default"), "main");

	private final ModelPart ALL;
	private final ModelPart right_leg;
	private final ModelPart left_leg;
	private final ModelPart body;
	private final ModelPart body_2;
	private final ModelPart body_1;
	private final ModelPart head;
	private final ModelPart left_ear;
	private final ModelPart right_ear;
	private final ModelPart left_eyeBall;
	private final ModelPart right_eyeBall;
	private final ModelPart right_arm;
	private final ModelPart left_arm;

    public KodiakModel(ModelPart root) {

		this.ALL = root.getChild("ALL");
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
		this.right_arm = this.body_1.getChild("right_arm");
		this.left_arm = this.body_1.getChild("left_arm");
    }

    public static LayerDefinition createBodyLayer() {

		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition ALL = partdefinition.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 6.0F, 0.0F));
		PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(68, 105).addBox(-2.5F, 0.0F, -3.5F, 7.0F, 10.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-7.5F, 8.0F, 13.5F));
		PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(68, 105).mirror().addBox(-4.5F, 0.0F, -3.5F, 7.0F, 10.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(7.5F, 8.0F, 13.5F));
		PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, 0.0F));
		PartDefinition body_2 = body.addOrReplaceChild("body_2", CubeListBuilder.create().texOffs(0, 0).addBox(-11.0F, -15.0F, -12.0F, 22.0F, 19.0F, 20.0F, new CubeDeformation(0.0F))
		.texOffs(125, 38).addBox(-11.0F, 4.0F, 8.0F, 22.0F, 5.0F, 0.0F, new CubeDeformation(0.05F))
		.texOffs(121, -20).addBox(-11.0F, 4.0F, -12.0F, 0.0F, 5.0F, 20.0F, new CubeDeformation(0.05F))
		.texOffs(121, -20).mirror().addBox(11.0F, 4.0F, -12.0F, 0.0F, 5.0F, 20.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offset(0.0F, 5.0F, 12.0F));
		PartDefinition body_1 = body_2.addOrReplaceChild("body_1", CubeListBuilder.create().texOffs(0, 39).addBox(-9.0F, -9.0F, -14.0F, 18.0F, 17.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(125, 4).addBox(-9.0F, 8.0F, -14.0F, 0.0F, 5.0F, 14.0F, new CubeDeformation(0.05F))
		.texOffs(125, 4).mirror().addBox(9.0F, 8.0F, -14.0F, 0.0F, 5.0F, 14.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offset(0.0F, -4.0F, -12.0F));
		PartDefinition head = body_1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(64, 39).addBox(-6.0F, -5.0F, -12.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(84, 24).addBox(-4.0F, 1.0F, -17.0F, 8.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, -14.0F));
		PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(-8, 94).addBox(-15.0F, 0.0F, -29.0F, 16.0F, 0.0F, 16.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(20.0F, 1.25F, -7.0F, 0.5521F, 0.6566F, 0.3705F));
		PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(40, 70).addBox(-2.0F, -2.0F, -0.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, -5.0F, -4.5F));
		PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(50, 70).addBox(-2.0F, -2.0F, -0.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, -5.0F, -4.5F));
		PartDefinition left_eyeBall = head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(77, 63).addBox(-1.5F, -0.5F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offset(3.5F, -0.5F, -12.025F));
		PartDefinition right_eyeBall = head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(77, 63).mirror().addBox(-1.5F, -0.5F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offset(-3.5F, -0.5F, -12.025F));
		PartDefinition right_arm = body_1.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(68, 88).mirror().addBox(-3.5F, 0.0F, -3.5F, 7.0F, 10.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-4.5F, 8.0F, -10.5F));
		PartDefinition left_arm = body_1.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(68, 88).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 10.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(4.5F, 8.0F, -10.5F));
		return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(KodiakEntity kodiak, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        if (kodiak.isBaby()) {
            float maturationPercent = (float) kodiak.getMaturationPercentage() / 100f;
            float headScale = 2f - (2f - 1.0f) * maturationPercent;

            this.head.xScale *= headScale;
            this.head.yScale *= headScale;
            this.head.zScale *= headScale;
        }
        this.applyHeadRotation(netHeadYaw, headPitch);
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
