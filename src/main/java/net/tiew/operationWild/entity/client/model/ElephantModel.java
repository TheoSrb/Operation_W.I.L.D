package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.animation.ElephantAnimations;
import net.tiew.operationWild.entity.client.animation.TigerAnimations;
import net.tiew.operationWild.entity.custom.living.ElephantEntity;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.ElephantFootstepPacket;
import net.tiew.operationWild.sound.OWSounds;

public class ElephantModel<T extends ElephantEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "elephant_default"), "main");

	private float lastLimbSwing = 0.0f;

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
				.texOffs(116, 132).mirror().addBox(-8.0F, 11.0F, -17.0F, 3.0F, 3.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(140, 110).mirror().addBox(-8.0F, 3.0F, -8.0F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 12.0F, -9.0F, -0.3442F, 0.0594F, 0.1642F));

		PartDefinition cube_r10 = head.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(116, 132).addBox(5.0F, 11.0F, -17.0F, 3.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
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

		elephant.setLimbSwing(limbSwing);
		elephant.setLimbSwingAmount(limbSwingAmount);

		if (elephant.isCombo(1)) {
			this.animate(elephant.attack1Combo, ElephantAnimations.ATTACK_STRIKE, ageInTicks, 0.925f);
		}
		if (elephant.isCombo(2)) {
			this.animate(elephant.attack2Combo, ElephantAnimations.ATTACK_STRIKE2, ageInTicks, 1.05f);
		}
		if (elephant.isCombo(3)) {
			this.animate(elephant.attack3Combo, ElephantAnimations.ATTACK_STRIKE3, ageInTicks, 1.15f);
		}

		if (elephant.transitionIdleSit.isStarted()) {
			this.animate(elephant.transitionIdleSit, ElephantAnimations.TRANSITION_IDLE_SIT, ageInTicks, 1.0f);
			return;
		}

		if (elephant.transitionSitIdle.isStarted()) {
			this.animate(elephant.transitionSitIdle, ElephantAnimations.TRANSITION_SIT_IDLE, ageInTicks, 1.0f);
			return;
		}


		if (elephant.isSitting()) {
			this.animate(elephant.sittingAnimationState, ElephantAnimations.SIT, ageInTicks, 1.0f);
			return;
		}

		this.animate(elephant.idleAnimationState, ElephantAnimations.MISC_IDLE, ageInTicks, 1.0f);

		this.animateWalk(ElephantAnimations.MOVE_WALK, limbSwing, limbSwingAmount, 10.75f, 8.75f);

		if (elephant.level().isClientSide()) {
			elephant.setBodyZRot((float) Math.toDegrees(this.body.zRot));
			elephant.setBodyXRot((float) Math.toDegrees(this.body.xRot));
		}

		this.head.xRot += (float) Math.toRadians(Math.min(elephant.getAcceleration(), 100) * 0.4f);
	}

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch) {
        pNetHeadYaw = Mth.clamp(pNetHeadYaw, -30.0F, 30.0F);
        pHeadPitch = Mth.clamp(pHeadPitch, -30.0F, 30.0F);

        this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);
    }

    @Override
    public ModelPart root() {
        return this.ALL2;
    }
}
