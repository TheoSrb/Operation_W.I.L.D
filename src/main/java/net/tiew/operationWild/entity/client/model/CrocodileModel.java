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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.item.ItemDisplayContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.client.animation.CrocodileAnimations;
import net.tiew.operationWild.entity.client.animation.KodiakAnimations;
import net.tiew.operationWild.entity.client.animation.WalrusAnimations;

public class CrocodileModel<T extends CrocodileEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "crocodile_default"), "main");

	private final ModelPart ALL2;
	private final ModelPart ALL;
	private final ModelPart body;
	private final ModelPart neck;
	private final ModelPart head;
	private final ModelPart right_eyeball;
	private final ModelPart left_eyeball;
	private final ModelPart mouth;
	private final ModelPart mouth_down;
	private final ModelPart mouth_up;
	private final ModelPart tail1;
	private final ModelPart tail2;
	private final ModelPart left_arm;
	private final ModelPart left_leg;
	private final ModelPart right_leg;
	private final ModelPart right_arm;

    public CrocodileModel(ModelPart root) {
		this.ALL2 = root.getChild("ALL2");
		this.ALL = this.ALL2.getChild("ALL");
		this.body = this.ALL.getChild("body");
		this.neck = this.body.getChild("neck");
		this.head = this.neck.getChild("head");
		this.right_eyeball = this.head.getChild("right_eyeball");
		this.left_eyeball = this.head.getChild("left_eyeball");
		this.mouth = this.head.getChild("mouth");
		this.mouth_down = this.mouth.getChild("mouth_down");
		this.mouth_up = this.mouth.getChild("mouth_up");
		this.tail1 = this.body.getChild("tail1");
		this.tail2 = this.tail1.getChild("tail2");
		this.left_arm = this.ALL.getChild("left_arm");
		this.left_leg = this.ALL.getChild("left_leg");
		this.right_leg = this.ALL.getChild("right_leg");
		this.right_arm = this.ALL.getChild("right_arm");
    }

    public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 14.0F, 3.0F));

		PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.5F, -5.0F, -14.0F, 17.0F, 12.0F, 26.0F, new CubeDeformation(0.0F))
				.texOffs(0, 67).addBox(-5.5F, -8.0F, -14.0F, 0.0F, 3.0F, 26.0F, new CubeDeformation(0.05F))
				.texOffs(0, 67).mirror().addBox(5.5F, -8.0F, -14.0F, 0.0F, 3.0F, 26.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offset(0.0F, -1.0F, 3.0F));

		PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(116, 23).addBox(-6.5F, -6.0F, -7.0F, 13.0F, 10.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, -14.0F));

		PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(114, 65).addBox(-5.5F, -5.0F, -9.0F, 11.0F, 9.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(40, 96).addBox(1.5F, -7.0F, -7.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(40, 96).mirror().addBox(-3.5F, -7.0F, -7.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, -7.0F));

		PartDefinition right_eyeball = head.addOrReplaceChild("right_eyeball", CubeListBuilder.create().texOffs(44, 100).mirror().addBox(0.0F, 0.0F, -2.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.025F)).mirror(false)
				.texOffs(42, 96).mirror().addBox(0.0F, 0.0F, -2.0F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.025F)).mirror(false), PartPose.offset(-3.5F, -7.0F, -5.0F));

		PartDefinition left_eyeball = head.addOrReplaceChild("left_eyeball", CubeListBuilder.create().texOffs(44, 100).addBox(-2.0F, 0.0F, -2.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.025F))
				.texOffs(42, 96).addBox(0.0F, 0.0F, -2.0F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.025F)), PartPose.offset(3.5F, -7.0F, -5.0F));

		PartDefinition mouth = head.addOrReplaceChild("mouth", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, -9.0F));

		PartDefinition mouth_down = mouth.addOrReplaceChild("mouth_down", CubeListBuilder.create().texOffs(40, 117).addBox(-3.5F, -2.0F, -10.0F, 7.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(91, -10).addBox(-3.4F, -5.0F, -10.0F, 0.0F, 3.0F, 10.0F, new CubeDeformation(0.05F))
				.texOffs(91, -10).mirror().addBox(3.4F, -5.0F, -10.0F, 0.0F, 3.0F, 10.0F, new CubeDeformation(0.05F)).mirror(false)
				.texOffs(94, 13).addBox(-3.5F, -5.0F, -9.5F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offset(0.0F, 2.0F, 0.0F));

		PartDefinition mouth_up = mouth.addOrReplaceChild("mouth_up", CubeListBuilder.create().texOffs(116, 40).addBox(-3.5F, -2.0F, -10.0F, 7.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(97, 128).addBox(-3.5F, -2.0F, -10.5F, 7.0F, 4.0F, 3.0F, new CubeDeformation(0.5F))
				.texOffs(94, 9).addBox(-3.5F, 2.0F, -10.5F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.025F))
				.texOffs(91, -6).addBox(-3.4F, 2.0F, -10.0F, 0.0F, 3.0F, 10.0F, new CubeDeformation(0.05F))
				.texOffs(91, -6).mirror().addBox(3.4F, 2.0F, -10.0F, 0.0F, 3.0F, 10.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offset(0.0F, -2.0F, 0.0F));

		PartDefinition tail1 = body.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(0, 38).addBox(-5.5F, -4.0F, 0.0F, 11.0F, 9.0F, 20.0F, new CubeDeformation(0.0F))
				.texOffs(3, 70).addBox(-3.5F, -7.0F, 0.0F, 0.0F, 3.0F, 20.0F, new CubeDeformation(0.05F))
				.texOffs(3, 70).mirror().addBox(3.5F, -7.0F, 0.0F, 0.0F, 3.0F, 20.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offset(0.0F, 2.0F, 12.0F));

		PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(62, 38).addBox(-3.5F, -2.0F, 0.0F, 7.0F, 7.0F, 20.0F, new CubeDeformation(0.0F))
				.texOffs(3, 70).addBox(-2.5F, -5.0F, 0.0F, 0.0F, 3.0F, 20.0F, new CubeDeformation(0.05F))
				.texOffs(3, 70).mirror().addBox(2.5F, -5.0F, 0.0F, 0.0F, 3.0F, 20.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offset(0.0F, 0.0F, 20.0F));

		PartDefinition left_arm = ALL.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(86, 23).addBox(-2.5F, -2.0F, -3.0F, 5.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(114, 83).addBox(-2.5F, 4.9F, -7.0F, 8.0F, 0.0F, 10.0F, new CubeDeformation(0.05F)), PartPose.offset(9.0F, 5.0F, -7.0F));

		PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 119).addBox(-2.5F, -3.0F, -3.0F, 6.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(114, 83).addBox(-2.5F, 5.9F, -7.0F, 8.0F, 0.0F, 10.0F, new CubeDeformation(0.05F)), PartPose.offset(9.0F, 4.0F, 9.0F));

		PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(114, 83).mirror().addBox(-5.5F, 5.9F, -7.0F, 8.0F, 0.0F, 10.0F, new CubeDeformation(0.05F)).mirror(false)
				.texOffs(0, 119).mirror().addBox(-3.5F, -3.0F, -3.0F, 6.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-9.0F, 4.0F, 9.0F));

		PartDefinition right_arm = ALL.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(114, 83).mirror().addBox(-5.5F, 4.9F, -7.0F, 8.0F, 0.0F, 10.0F, new CubeDeformation(0.05F)).mirror(false)
				.texOffs(86, 23).mirror().addBox(-2.5F, -2.0F, -3.0F, 5.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-9.0F, 5.0F, -7.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(CrocodileEntity crocodile, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        if (crocodile.isBaby()) {
            float maturationPercent = (float) crocodile.getMaturationPercentage() / 100f;
            float headScale = 1.6f - (1.6f - 1.0f) * maturationPercent;

            this.head.xScale *= headScale;
            this.head.yScale *= headScale;
            this.head.zScale *= headScale;
        }
        this.applyHeadRotation(netHeadYaw, headPitch);

		if (crocodile.isCombo(1)) {
			this.animate(crocodile.attack1Combo, CrocodileAnimations.ATTACK_STRIKE, ageInTicks, 1.0f);
		}
		if (crocodile.isCombo(2)) {
			this.animate(crocodile.attack2Combo, CrocodileAnimations.ATTACK_STRIKE2, ageInTicks, 1.0f);
		}
		if (crocodile.isCombo(3)) {
			this.animate(crocodile.attack3Combo, CrocodileAnimations.ATTACK_STRIKE3, ageInTicks, 1.0f);
		}

		if (crocodile.isMad()) {
			this.left_eyeball.xScale = 0;
			this.left_eyeball.yScale = 0;
			this.left_eyeball.zScale = 0;

			this.right_eyeball.xScale = 0;
			this.right_eyeball.yScale = 0;
			this.right_eyeball.zScale = 0;
		}

		/*if (crocodile.transitionIdleSit.isStarted()) {
			this.animate(crocodile.transitionIdleSit, CrocodileAnimations.TRANSITION_IDLE_SIT, ageInTicks, 1.0f);
			return;
		}

		if (crocodile.transitionSitIdle.isStarted()) {
			this.animate(crocodile.transitionSitIdle, CrocodileAnimations.TRANSITION_SIT_IDLE, ageInTicks, 1.0f);
			return;
		}

		if (crocodile.transitionIdleSleep.isStarted()) {
			this.animate(crocodile.transitionIdleSleep, CrocodileAnimations.TRANSITION_IDLE_SLEEP, ageInTicks, 2.0f);
			return;
		}

		if (crocodile.transitionSleepIdle.isStarted()) {
			this.animate(crocodile.transitionSleepIdle, CrocodileAnimations.TRANSITION_SLEEP_IDLE, ageInTicks, 2.0f);
			return;
		}

		if (crocodile.transitionIdleStandingUp.isStarted()) {
			this.animate(crocodile.transitionIdleStandingUp, CrocodileAnimations.TRANSITION_IDLE_STAND_UP, ageInTicks, 1.0f);
			return;
		}

		if (crocodile.transitionStandingUpIdle.isStarted()) {
			this.animate(crocodile.transitionStandingUpIdle, CrocodileAnimations.TRANSITION_STAND_UP_IDLE, ageInTicks, 1.0f);
			return;
		}


		if (crocodile.isSitting()) {
			this.animate(crocodile.sittingAnimationState, CrocodileAnimations.SIT, ageInTicks, 1.0f);
			return;
		}*/

		if (crocodile.growlsAnimationState.isStarted()) {
			this.animate(crocodile.growlsAnimationState, CrocodileAnimations.MISC_IDLE2, ageInTicks, 1.0f);
		}


		if (!crocodile.isInWater()) {
			this.animate(crocodile.idleAnimationState, CrocodileAnimations.MISC_IDLE, ageInTicks, 1.0f);

			if (crocodile.isRunning() || crocodile.getState() == 2) {
				if (crocodile.isVehicle()) {
					this.animateWalk(CrocodileAnimations.MOVE_RUN, limbSwing, limbSwingAmount, 1.3f, 1.4f);
				} else {
					this.animateWalk(CrocodileAnimations.MOVE_RUN, limbSwing, limbSwingAmount, 1.85f, 1.9f);
				}
			} else {
				this.animateWalk(CrocodileAnimations.MOVE_WALK, limbSwing, limbSwingAmount, 6f, 6f);
			}
		} else {
			this.animate(crocodile.idleWaterAnimationState, CrocodileAnimations.MOVE_SWIM, ageInTicks, 1.0f);
			this.animateWalk(CrocodileAnimations.MOVE_SWIM, limbSwing, limbSwingAmount, 5f, 8f);
		}


		if (crocodile.level().isClientSide()) {
			crocodile.setBodyZRot((float) Math.toDegrees(this.body.zRot));
			crocodile.setBodyXRot((float) Math.toDegrees(this.body.xRot));
		}
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
