package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.animation.ElephantAnimations;
import net.tiew.operationWild.entity.client.animation.PeacockAnimations;
import net.tiew.operationWild.entity.custom.living.ElephantEntity;
import net.tiew.operationWild.event.ClientEvents;
import org.joml.Vector3f;

import java.util.List;

public class ElephantModel<T extends ElephantEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "elephant_default"), "main");

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

    public ElephantModel(ModelPart root) {
		this.ALL = root.getChild("ALL");
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
    }

    public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition ALL = partdefinition.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, -7.0F, 2.0F));

		PartDefinition right_arm = ALL.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(60, 70).mirror().addBox(-4.5F, 0.0F, -6.0F, 9.0F, 20.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-6.0F, 11.0F, -14.0F));

		PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(60, 70).mirror().addBox(-4.5F, 0.0F, -6.0F, 9.0F, 20.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-8.0F, 11.0F, 14.0F));

		PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(60, 70).addBox(-4.5F, 0.0F, -6.0F, 9.0F, 20.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 11.0F, 14.0F));

		PartDefinition left_arm = ALL.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(60, 70).addBox(-4.5F, 0.0F, -6.0F, 9.0F, 20.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 11.0F, -14.0F));

		PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-13.5F, -13.0F, -21.0F, 27.0F, 26.0F, 44.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, -1.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 70).addBox(-7.5F, -9.0F, -15.0F, 15.0F, 18.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.0F, -21.0F));

		PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(140, 83).mirror().addBox(-9.0F, -5.0F, -9.0F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(116, 132).mirror().addBox(-8.0F, 11.0F, -17.0F, 3.0F, 3.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(140, 110).mirror().addBox(-8.0F, 3.0F, -8.0F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 12.0F, -9.0F, -0.3442F, 0.0594F, 0.1642F));

		PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(116, 132).addBox(5.0F, 11.0F, -17.0F, 3.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(140, 110).addBox(5.0F, 3.0F, -8.0F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(140, 83).addBox(4.0F, -5.0F, -9.0F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 12.0F, -9.0F, -0.3442F, -0.0594F, -0.1642F));

		PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(0, 103).addBox(-1.0F, -12.0F, -1.0F, 19.0F, 24.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(7.5F, -1.0F, -7.0F));

		PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(0, 103).mirror().addBox(-18.0F, -12.0F, -1.0F, 19.0F, 24.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-7.5F, -1.0F, -7.0F));

		PartDefinition trunk = head.addOrReplaceChild("trunk", CubeListBuilder.create().texOffs(42, 132).addBox(-3.5F, -3.0F, -4.0F, 7.0F, 16.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, -16.0F));

		PartDefinition trunk2 = trunk.addOrReplaceChild("trunk2", CubeListBuilder.create().texOffs(70, 132).addBox(-2.5F, 0.0F, -3.0F, 5.0F, 16.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 13.0F, 0.0F));

		PartDefinition left_eyeBall = head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(44, 105).addBox(-2.5F, -0.5F, 0.0F, 5.0F, 1.0F, 0.0F, new CubeDeformation(0.1F)), PartPose.offset(5.0F, -1.5F, -15.0F));

		PartDefinition right_eyeBall = head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(44, 105).mirror().addBox(-2.5F, -0.5F, 0.0F, 5.0F, 1.0F, 0.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offset(-5.0F, -1.5F, -15.0F));

		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 185).addBox(-1.5F, -1.0F, 0.05F, 3.0F, 22.0F, 0.0F, new CubeDeformation(0.1F))
				.texOffs(0, 182).addBox(0.05F, -1.0F, -1.5F, 0.0F, 22.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, 23.0F, 0.0873F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(ElephantEntity elephant, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        if (elephant.isBaby()) {
            float maturationPercent = (float) elephant.getMaturationPercentage() / 100f;
            float headScale = 2f - (2f - 1.0f) * maturationPercent;

            this.head.xScale *= headScale;
            this.head.yScale *= headScale;
            this.head.zScale *= headScale;
        }
        this.applyHeadRotation(netHeadYaw, headPitch);

		this.animate(elephant.idleAnimationState, ElephantAnimations.MISC_IDLE, ageInTicks, 1.0f);

		this.animateWalk(ElephantAnimations.MOVE_WALK, limbSwing, limbSwingAmount, 10.75f, 8.75f);
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
