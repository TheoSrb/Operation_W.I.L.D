package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;
import net.tiew.operationWild.entity.attacks.OWAttackLogic;
import net.tiew.operationWild.entity.attacks.OWAttacksHandler;
import net.tiew.operationWild.entity.client.animation.CrocodileAnimations;
import net.tiew.operationWild.entity.client.animation.OrcaAnimations;

import javax.swing.text.html.parser.Entity;

public class OrcaModel<T extends OrcaEntity> extends HierarchicalModel<T> implements OWFlagModel {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "orca_default"), "main");

	/** Texture ne contenant que la hampe du drapeau de tribu (tout le reste est transparent). */
	private static final ResourceLocation FLAG_POLE_TEXTURE =
			ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/orca/orca_flag.png");

	/**
	 * Rectangle de la toile dans l'espace local de l'os {@code flag}, en pixels modele.
	 * Recopie la boite declaree par {@code createBodyLayer} : Y dans [-5.75, 5.25], Z dans [0.5, 19.5].
	 */
	private static final Anchor FLAG_ANCHOR = new Anchor(-5.75f, 11f, 0.5f, 19f);

	private float prevLimbSwing = 0f;
	private int groundRotationTimer = 0;

	public float externalRiderPitch = 0f;

	private final ModelPart ALL2;
	private final ModelPart ALL;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart mouth;
	private final ModelPart mouth_down;
	private final ModelPart mouth_up;
	private final ModelPart tail;
	private final ModelPart front_tail;
	private final ModelPart back_tail;
	private final ModelPart left_fan;
	private final ModelPart right_fan;
	/** Nuls sur les definitions de skin qui ne declarent pas le porte-drapeau. */
	private final ModelPart mainFlag;
	private final ModelPart flag;

	public OrcaModel(ModelPart root) {
		this.ALL2 = root.getChild("ALL2");
		this.ALL = this.ALL2.getChild("ALL");
		this.body = this.ALL.getChild("body");
		this.head = this.body.getChild("head");
		this.mouth = this.head.getChild("mouth");
		this.mouth_down = this.mouth.getChild("mouth_down");
		this.mouth_up = this.mouth.getChild("mouth_up");
		this.tail = this.body.getChild("tail");
		this.front_tail = this.tail.getChild("front_tail");
		this.back_tail = this.tail.getChild("back_tail");
		this.left_fan = this.body.getChild("left_fan");
		this.right_fan = this.body.getChild("right_fan");
		this.mainFlag = this.body.hasChild("mainFlag") ? this.body.getChild("mainFlag") : null;
		this.flag = this.mainFlag != null ? this.mainFlag.getChild("flag") : null;

		// Le porte-drapeau n'est dessine que par renderTribeFlagPole : masque d'entree, il ne risque
		// pas de l'etre avec la texture de la peau par le modele de base ni par un modele d'overlay
		// de skin, qui ne rejoue pas setupAnim.
		if (this.mainFlag != null) this.mainFlag.visible = false;
	}

	// -- Drapeau de tribu (OWFlagModel) ------------------------------------------

	@Override
	public boolean hasTribeFlag() {
		return this.mainFlag != null && this.flag != null;
	}

	@Override
	public void renderTribeFlagPole(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
		if (!hasTribeFlag()) return;
		poseStack.pushPose();
		this.ALL2.translateAndRotate(poseStack);
		this.ALL.translateAndRotate(poseStack);
		this.body.translateAndRotate(poseStack);
		// Visible le temps de ce seul appel : le reste du pipeline doit continuer a l'ignorer.
		this.mainFlag.visible = true;
		this.mainFlag.render(poseStack, buffer, packedLight, packedOverlay, 0xFFFFFFFF);
		this.mainFlag.visible = false;
		poseStack.popPose();
	}

	@Override
	public void translateToTribeFlag(PoseStack poseStack) {
		if (!hasTribeFlag()) return;
		this.ALL2.translateAndRotate(poseStack);
		this.ALL.translateAndRotate(poseStack);
		this.body.translateAndRotate(poseStack);
		this.mainFlag.translateAndRotate(poseStack);
		this.flag.translateAndRotate(poseStack);
	}

	@Override
	public ResourceLocation tribeFlagPoleTexture() {
		return FLAG_POLE_TEXTURE;
	}

	@Override
	public Anchor tribeFlagAnchor() {
		return FLAG_ANCHOR;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 7.0F, -2.0F));

		PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -14.0F, -19.0F, 24.0F, 30.0F, 38.0F, new CubeDeformation(0.0F))
				.texOffs(237, 0).addBox(-12.0F, -14.0F, -19.0F, 24.0F, 30.0F, 38.0F, new CubeDeformation(0.5F))
				.texOffs(161, 38).addBox(-11.0F, -16.0F, 5.0F, 10.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(178, 79).addBox(-11.0F, -28.0F, 17.0F, 10.0F, 14.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(178, 79).mirror().addBox(1.0F, -28.0F, 17.0F, 10.0F, 14.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(148, 79).mirror().addBox(-6.0F, -28.0F, -8.0F, 12.0F, 14.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(161, 38).mirror().addBox(1.0F, -16.0F, 5.0F, 10.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(154, 21).addBox(-6.0F, -16.0F, -19.0F, 12.0F, 2.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(124, 28).addBox(-1.0F, -44.0F, 20.0F, 3.0F, 17.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 6.0F, -35.6F, -0.3054F, 0.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(252, 136).addBox(-15.0F, -2.4F, -9.0F, 30.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, -19.0F));

		PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(228, 92).mirror().addBox(0.0F, -28.0F, -1.0F, 0.0F, 29.0F, 12.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(15.0F, -2.0F, -7.0F, -0.2712F, -0.0482F, -0.1276F));

		PartDefinition cube_r3 = head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(228, 92).addBox(0.0F, -28.0F, -1.0F, 0.0F, 29.0F, 12.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-15.0F, -2.0F, -7.0F, -0.2712F, 0.0482F, 0.1276F));

		PartDefinition mouth = head.addOrReplaceChild("mouth", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition mouth_down = mouth.addOrReplaceChild("mouth_down", CubeListBuilder.create().texOffs(9, 159).addBox(-9.0F, -2.0F, -24.0F, 18.0F, 7.0F, 24.0F, new CubeDeformation(0.0F))
				.texOffs(5, 115).addBox(-8.0F, -4.0F, -23.9F, 0.0F, 3.0F, 23.0F, new CubeDeformation(0.025F))
				.texOffs(3, 132).addBox(-8.0F, -4.0F, -23.5F, 16.0F, 3.0F, 0.0F, new CubeDeformation(0.025F))
				.texOffs(5, 115).mirror().addBox(8.0F, -4.0F, -23.9F, 0.0F, 3.0F, 23.0F, new CubeDeformation(0.025F)).mirror(false), PartPose.offset(0.0F, 2.0F, 0.0F));

		PartDefinition mouth_up = mouth.addOrReplaceChild("mouth_up", CubeListBuilder.create().texOffs(123, 168).addBox(-9.0F, -9.0F, -21.0F, 18.0F, 13.0F, 24.0F, new CubeDeformation(0.0F))
				.texOffs(60, 120).addBox(-8.0F, 4.0F, -20.9F, 0.0F, 3.0F, 23.0F, new CubeDeformation(0.025F))
				.texOffs(60, 120).mirror().addBox(8.0F, 4.0F, -20.9F, 0.0F, 3.0F, 23.0F, new CubeDeformation(0.025F)).mirror(false)
				.texOffs(60, 135).addBox(-8.0F, 4.0F, -20.5F, 16.0F, 3.0F, 0.0F, new CubeDeformation(0.025F)), PartPose.offset(0.0F, -4.0F, -3.0F));

		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 19.0F));

		PartDefinition front_tail = tail.addOrReplaceChild("front_tail", CubeListBuilder.create().texOffs(0, 68).addBox(-8.0F, -11.0F, 0.0F, 16.0F, 23.0F, 35.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition back_tail = tail.addOrReplaceChild("back_tail", CubeListBuilder.create().texOffs(102, 113).addBox(-15.0F, -1.0F, -3.0F, 30.0F, 2.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, 34.0F));

		PartDefinition left_fan = body.addOrReplaceChild("left_fan", CubeListBuilder.create().texOffs(124, 0).addBox(0.0F, -1.0F, -6.0F, 21.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(12.0F, 10.0F, -10.0F));

		PartDefinition right_fan = body.addOrReplaceChild("right_fan", CubeListBuilder.create().texOffs(124, 0).mirror().addBox(-21.0F, -1.0F, -6.0F, 21.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-12.0F, 10.0F, -10.0F));

		PartDefinition mainFlag = body.addOrReplaceChild("mainFlag", CubeListBuilder.create().texOffs(55, 288).addBox(-0.5F, -18.75F, -0.5F, 1.0F, 20.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(60, 287).addBox(-0.5F, -18.75F, 0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(60, 287).addBox(-0.5F, -6.75F, 0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(47, 302).addBox(-1.0F, -3.725F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.1F))
				.texOffs(47, 302).addBox(-1.0F, 0.275F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.1F))
				.texOffs(40, 295).addBox(-1.0F, -2.725F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.1F))
				.texOffs(40, 287).addBox(-1.0F, -20.75F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(12.5F, -13.0F, 10.5F));

		PartDefinition flag = mainFlag.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(7, 240).addBox(0.0F, -5.75F, 0.5F, 0.0F, 11.0F, 19.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 512, 512);
	}

	@Override
	public void setupAnim(OrcaEntity orca, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		if (!orca.isInWater()) {
			float yVel = (float) orca.getDeltaMovement().y;
			if (yVel > 0.3f && yVel < 0.7f) {
				groundRotationTimer = 25;
			}
			if (groundRotationTimer > 0) {
				groundRotationTimer--;
				this.ALL2.zRot = (float) Math.toRadians(90);
			}
		} else {
			groundRotationTimer = 0;
		}

		if (Math.abs(externalRiderPitch) > 0.01f) {
			this.ALL2.xRot = (float) Math.toRadians(externalRiderPitch);
		}

		if (orca.isBaby()) {
			float maturationPercent = (float) orca.getMaturationPercentage() / 100f;
			float headScale = 1.5f - (1.6f - 1.0f) * maturationPercent;

			this.head.xScale *= headScale;
			this.head.yScale *= headScale;
			this.head.zScale *= headScale;
		}

		this.applyHeadRotation(netHeadYaw, headPitch);

		if (orca.isCombo(1) || (orca.attack1Combo.isStarted() && !orca.isCombo())) {
			this.animate(orca.attack1Combo, OrcaAnimations.ATTACK_STRIKE, ageInTicks, 1.0f);
			captureBodyState(orca, 7f, 1.0f, this.ALL2, this.ALL, this.body);
			return;
		}
		if (orca.isCombo(2) || (orca.attack2Combo.isStarted() && !orca.isCombo())) {
			this.animate(orca.attack2Combo, OrcaAnimations.ATTACK_STRIKE_2, ageInTicks, 1.0f);
			captureBodyState(orca, 7f, 1.0f, this.ALL2, this.ALL, this.body);
			return;
		}
		if (orca.isCombo(3)) {
			this.animate(orca.attack3Combo, OrcaAnimations.ATTACK_STRIKE_3, ageInTicks, 1.0f);
			captureBodyState(orca, 7f, 1.0f, this.ALL2, this.ALL, this.body);
			return;
		}

		if ((orca.isRunning() || orca.getState() == 2)) {
			float speed = orca.getControllingPassenger() != null ? 1.2f : 1.0f;
			this.animateWalk(OrcaAnimations.MOVE_SWIM, limbSwing, limbSwingAmount, speed, speed);

			if (walkAnimCrossed(OrcaAnimations.MOVE_SWIM, limbSwing, speed, 300L)) orca.onRightFootDown();
			if (walkAnimCrossed(OrcaAnimations.MOVE_SWIM, limbSwing, speed, 300L)) orca.onLeftFootDown();

		} else {
			float speed = orca.getControllingPassenger() != null ? 1.5f : 0.85f;
			this.animateWalk(OrcaAnimations.MOVE_SWIM, limbSwing, limbSwingAmount, speed, speed);

			if (walkAnimCrossed(OrcaAnimations.MOVE_SWIM, limbSwing, speed, 300L)) orca.onRightFootDown();
			if (walkAnimCrossed(OrcaAnimations.MOVE_SWIM, limbSwing, speed, 300L)) orca.onLeftFootDown();
		}

		this.animate(orca.idleAnimationState, OrcaAnimations.MISC_IDLE, ageInTicks, 1.0f);

		this.prevLimbSwing = limbSwing;

		captureBodyState(orca, 7f, 1.0f, this.ALL2, this.ALL, this.body);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		this.ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	private void captureBodyState(OrcaEntity orca, float restPoseYSum, float riderRotIntensity, ModelPart... boneChain) {
		if (!orca.level().isClientSide()) return;

		orca.setBodyZRot((float) Math.toDegrees((this.ALL2.zRot + this.ALL.zRot + this.body.zRot) * riderRotIntensity));
		orca.setBodyXRot((float) -Math.toDegrees((this.ALL2.xRot + this.ALL.xRot + this.body.xRot) * riderRotIntensity));
		orca.bodyAnimXRot = this.ALL2.xRot + this.ALL.xRot + this.body.xRot;

		orca.bodyZRot_passenger = orca.getBodyZRot();
		orca.bodyXRot_passenger = orca.getBodyXRot();

		float xSum = this.ALL2.x + this.ALL.x + this.body.x;
		orca.bodyAnimX = -xSum;
		orca.bodyAnimX_passenger = -xSum;

		float ySum = 0f;
		for (ModelPart bone : boneChain) ySum += bone.y;
		orca.bodyAnimY = ySum - restPoseYSum;
		orca.bodyAnimY_passenger = orca.bodyAnimY;
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

	public void copyPoseFrom(OrcaModel<?> src) {
		this.ALL2.copyFrom(src.ALL2);
		this.ALL.copyFrom(src.ALL);
		this.body.copyFrom(src.body);
		this.head.copyFrom(src.head);
		this.mouth.copyFrom(src.mouth);
		this.mouth_down.copyFrom(src.mouth_down);
		this.mouth_up.copyFrom(src.mouth_up);
		this.tail.copyFrom(src.tail);
		this.front_tail.copyFrom(src.front_tail);
		this.back_tail.copyFrom(src.back_tail);
		this.left_fan.copyFrom(src.left_fan);
		this.right_fan.copyFrom(src.right_fan);
	}

	private boolean walkAnimCrossed(AnimationDefinition animation, float limbSwing, float speedScale, long triggerTimeMs) {
		long durationMs = (long)(animation.lengthInSeconds() * 1000f);
		if (durationMs <= 0) return false;

		long cur  = ((long)(limbSwing     * 50f * speedScale)) % durationMs;
		long prev = ((long)(prevLimbSwing * 50f * speedScale)) % durationMs;

		if (prev <= cur) return prev < triggerTimeMs && cur >= triggerTimeMs;
		return triggerTimeMs <= cur || triggerTimeMs > prev;
	}
}
