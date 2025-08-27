package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.animation.ElephantAnimations;
import net.tiew.operationWild.entity.client.animation.KodiakAnimations;
import net.tiew.operationWild.entity.custom.living.KodiakEntity;

import java.util.List;

public class KodiakModel<T extends KodiakEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "kodiak_default"), "main");

	private final ModelPart ALL2;
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
	private final ModelPart muzzle;
	private final ModelPart left_arm;
	private final ModelPart right_arm;

	private KodiakEntity currentEntity;

    public KodiakModel(ModelPart root) {
		this.ALL2 = root.getChild("ALL2");
		this.ALL = this.ALL2.getChild("ALL");
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
		this.muzzle = this.head.getChild("muzzle");
		this.left_arm = this.body_1.getChild("left_arm");
		this.right_arm = this.body_1.getChild("right_arm");
    }

    public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(68, 105).addBox(-2.5F, 0.0F, -3.5F, 7.0F, 10.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-7.5F, 8.0F, 13.5F));

		PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(68, 105).mirror().addBox(-4.5F, 0.0F, -3.5F, 7.0F, 10.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(7.5F, 8.0F, 13.5F));

		PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, 0.0F));

		PartDefinition body_2 = body.addOrReplaceChild("body_2", CubeListBuilder.create().texOffs(0, 0).addBox(-11.0F, -15.0F, -12.0F, 22.0F, 19.0F, 20.0F, new CubeDeformation(0.0F))
				.texOffs(82, 137).addBox(-11.0F, -15.0F, -12.0F, 22.0F, 19.0F, 20.0F, new CubeDeformation(0.5F))
				.texOffs(125, 38).addBox(-11.0F, 4.0F, 8.0F, 22.0F, 5.0F, 0.0F, new CubeDeformation(0.05F))
				.texOffs(121, -20).addBox(-11.0F, 4.0F, -12.0F, 0.0F, 5.0F, 20.0F, new CubeDeformation(0.05F))
				.texOffs(121, -20).mirror().addBox(11.0F, 4.0F, -12.0F, 0.0F, 5.0F, 20.0F, new CubeDeformation(0.05F)).mirror(false)
				.texOffs(0, 127).addBox(-7.0F, -18.0F, -10.0F, 14.0F, 5.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, 12.0F));

		PartDefinition body_1 = body_2.addOrReplaceChild("body_1", CubeListBuilder.create().texOffs(0, 39).addBox(-9.0F, -9.0F, -14.0F, 18.0F, 17.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(0, 170).addBox(-9.0F, -9.0F, -14.0F, 18.0F, 17.0F, 14.0F, new CubeDeformation(0.5F))
				.texOffs(0, 127).addBox(-7.0F, -12.0F, -14.0F, 14.0F, 5.0F, 14.0F, new CubeDeformation(-0.1F))
				.texOffs(125, 4).addBox(-9.0F, 8.0F, -14.0F, 0.0F, 5.0F, 14.0F, new CubeDeformation(0.05F))
				.texOffs(125, 4).mirror().addBox(9.0F, 8.0F, -14.0F, 0.0F, 5.0F, 14.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offset(0.0F, -4.0F, -12.0F));

		PartDefinition head = body_1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(64, 39).addBox(-6.0F, -5.0F, -12.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(230, 243).addBox(-6.0F, -5.0F, -1.0F, 12.0F, 12.0F, 1.0F, new CubeDeformation(0.5F))
				.texOffs(232, 224).addBox(6.0F, -5.0F, -1.0F, 12.0F, 12.0F, 0.0F, new CubeDeformation(0.05F))
				.texOffs(231, 198).addBox(-6.0F, -17.0F, -0.5F, 12.0F, 12.0F, 0.0F, new CubeDeformation(0.05F))
				.texOffs(232, 224).mirror().addBox(-18.0F, -5.0F, -1.0F, 12.0F, 12.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false)
				.texOffs(69, 202).addBox(-6.0F, -5.0F, -12.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.5F))
				.texOffs(62, 126).addBox(-6.0F, -5.0F, -16.0F, 0.0F, 12.0F, 4.0F, new CubeDeformation(0.05F))
				.texOffs(62, 126).mirror().addBox(6.0F, -5.0F, -16.0F, 0.0F, 12.0F, 4.0F, new CubeDeformation(0.05F)).mirror(false)
				.texOffs(84, 24).addBox(-4.0F, 1.0F, -17.0F, 8.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(113, 58).addBox(-8.5F, 5.5F, -30.0F, 16.0F, 0.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(4, 158).addBox(-7.0F, 5.0F, -17.0F, 14.0F, 1.0F, 1.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, -1.0F, -14.0F));

		PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 229).mirror().addBox(6.0F, -13.0F, -9.0F, 0.0F, 11.0F, 14.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(2.0F, 8.0F, -8.0F, 0.0F, 0.2618F, 0.0F));

		PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 213).mirror().addBox(6.0F, -13.0F, -9.0F, 0.0F, 11.0F, 14.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(1.25F, -1.5F, 1.0F, 0.0F, -0.2618F, 0.0F));

		PartDefinition cube_r3 = head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 213).addBox(-6.0F, -13.0F, -9.0F, 0.0F, 11.0F, 14.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.25F, -1.5F, 1.0F, 0.0F, 0.2618F, 0.0F));

		PartDefinition cube_r4 = head.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 229).addBox(-6.0F, -13.0F, -9.0F, 0.0F, 11.0F, 14.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-2.0F, 8.0F, -8.0F, 0.0F, -0.2618F, 0.0F));

		PartDefinition cube_r5 = head.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(231, 181).addBox(-6.0F, -12.0F, 6.0F, 12.0F, 12.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(0.0F, 7.0F, -6.5F, 0.0F, 0.0F, -3.1416F));

		PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(40, 70).addBox(-2.0F, -2.0F, -0.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, -5.0F, -4.5F));

		PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(50, 70).addBox(-2.0F, -2.0F, -0.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, -5.0F, -4.5F));

		PartDefinition left_eyeBall = head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(77, 63).addBox(-1.5F, -0.5F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offset(3.5F, -0.5F, -12.025F));

		PartDefinition right_eyeBall = head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(77, 63).mirror().addBox(-1.5F, -0.5F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offset(-3.5F, -0.5F, -12.025F));

		PartDefinition muzzle = head.addOrReplaceChild("muzzle", CubeListBuilder.create().texOffs(89, 27).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, -17.0F));

		PartDefinition left_arm = body_1.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(68, 88).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 10.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(4.5F, 8.0F, -10.5F));

		PartDefinition right_arm = body_1.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(68, 88).mirror().addBox(-3.5F, 0.0F, -3.5F, 7.0F, 10.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-4.5F, 8.0F, -10.5F));

		return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(KodiakEntity kodiak, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

		this.currentEntity = kodiak;

        if (kodiak.isBaby()) {
            float maturationPercent = (float) kodiak.getMaturationPercentage() / 100f;
            float headScale = 1.6f - (1.6f - 1.0f) * maturationPercent;

            this.head.xScale *= headScale;
            this.head.yScale *= headScale;
            this.head.zScale *= headScale;
        }
		if ((kodiak.getMaturationPercentage() < 60 && kodiak.getMaturationPercentage() > 0) || (kodiak.isMad())) {
			this.left_eyeBall.xScale = 0;
			this.left_eyeBall.yScale = 0;
			this.left_eyeBall.zScale = 0;
			this.right_eyeBall.xScale = 0;
			this.right_eyeBall.yScale = 0;
			this.right_eyeBall.zScale = 0;
		}
        this.applyHeadRotation(netHeadYaw, headPitch);

		if (kodiak.isCombo(1)) {
			this.animate(kodiak.attack1Combo, KodiakAnimations.ATTACK_STRIKE, ageInTicks, 0.925f);
		}
		if (kodiak.isCombo(2)) {
			this.animate(kodiak.attack2Combo, KodiakAnimations.ATTACK_STRIKE2, ageInTicks, 1.05f);
		}
		if (kodiak.isCombo(3)) {
			this.animate(kodiak.attack3Combo, KodiakAnimations.ATTACK_STRIKE3, ageInTicks, 1.15f);
		}

		if (kodiak.isSniffing()) {
			this.animate(kodiak.sniffsAnimationState, KodiakAnimations.SNIFFS, ageInTicks, 1.0f);
		}

		if (kodiak.transitionIdleSit.isStarted()) {
			this.animate(kodiak.transitionIdleSit, KodiakAnimations.TRANSITION_IDLE_SIT, ageInTicks, 1.0f);
			return;
		}

		if (kodiak.transitionSitIdle.isStarted()) {
			this.animate(kodiak.transitionSitIdle, KodiakAnimations.TRANSITION_SIT_IDLE, ageInTicks, 1.0f);
			return;
		}

		if (kodiak.transitionIdleSleep.isStarted()) {
			this.animate(kodiak.transitionIdleSleep, KodiakAnimations.TRANSITION_IDLE_SLEEP, ageInTicks, 2.0f);
			return;
		}

		if (kodiak.transitionSleepIdle.isStarted()) {
			this.animate(kodiak.transitionSleepIdle, KodiakAnimations.TRANSITION_SLEEP_IDLE, ageInTicks, 2.0f);
			return;
		}

		if (kodiak.isSitting()) {
			this.animate(kodiak.sittingAnimationState, KodiakAnimations.SIT, ageInTicks, 1.0f);
			return;
		}

		if (kodiak.isNapping()) {
			this.animate(kodiak.sleepingAnimationState, KodiakAnimations.SLEEP, ageInTicks, 1.0f);
			return;
		}

		List<Player> players = kodiak.level().getEntitiesOfClass(Player.class, kodiak.getBoundingBox().inflate(30));

		for (Player player : players) {
			if (kodiak.isSniffing()) {
				double distance = kodiak.distanceTo(player);

				if (distance < 10) {
					this.head.xRot -= (float) (Math.toRadians((10 - distance) * 2.5f));
				}
			}
		}

		this.animate(kodiak.idleAnimationState, KodiakAnimations.MISC_IDLE, ageInTicks, 1.0f);

		if (kodiak.isRunning() || kodiak.getState() == 2) {
			this.animateWalk(KodiakAnimations.MOVE_RUN, limbSwing, limbSwingAmount, 1.0f, 1.0f);
		} else {
			this.animateWalk(kodiak.getFoodChooseFromChest().isEmpty() ? KodiakAnimations.MOVE_WALK : KodiakAnimations.MOVE_WALK_WITH_ITEM, limbSwing, limbSwingAmount, 4.5f, 4.5f);
		}


		if (kodiak.level().isClientSide()) {
			kodiak.setBodyZRot((float) Math.toDegrees(this.body_2.zRot));
			kodiak.setBodyXRot((float) Math.toDegrees(this.body_2.xRot));
		}
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);

		if (this.currentEntity != null && !this.currentEntity.getFoodChooseFromChest().isEmpty()) {
			renderItemOnHead(this.currentEntity, poseStack, packedLight);
		}
    }

	private void renderItemOnHead(KodiakEntity kodiak, PoseStack poseStack, int packedLight) {
		poseStack.pushPose();

		this.head.translateAndRotate(poseStack);

		poseStack.translate(0.0D, 0.75, -1.3D);
		poseStack.scale(1f, 1f, 1f);

		poseStack.mulPose(Axis.XP.rotationDegrees(90));

		MultiBufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

		Minecraft.getInstance().getItemRenderer().renderStatic(kodiak.getFoodChooseFromChest(), ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, kodiak.level(), 0);

		poseStack.popPose();
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
