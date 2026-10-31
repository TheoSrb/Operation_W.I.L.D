package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.LivingEntity;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.aquatic.ColossalSquidEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.client.animation.ColossalSquidAnimations;
import net.tiew.operationWild.entity.client.animation.OWSedationAnimationSet;
import net.tiew.operationWild.entity.client.animation.OWSedationAnimator;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ColossalSquidModel<T extends ColossalSquidEntity> extends OWComboModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "colossal_squid_default"), "main");

	private static final OWSedationAnimationSet SEDATION = new OWSedationAnimationSet(
			ColossalSquidAnimations.SEDATED_FALL, ColossalSquidAnimations.SEDATED_REST, ColossalSquidAnimations.SEDATED_WAKE);

	private static final Vector3f ANIM_VECTOR = new Vector3f();
	private static final float SWIM_ANIM_SPEED = 1.0f;
	private static final float SWIM_WEIGHT_FACTOR = 2.5f;
	private static final float TURN_FADE_TICKS = 7f;
	private static final float TURN_TO_SWIM_FADE_TICKS = 12f;
	private static final float TURN_ANIM_TICKS = 20f;
	private static final float BABY_HEAD_SCALE = 1.5f;

	public float externalRiderPitch = 0f;
	public float externalBankRoll = 0f;

	private boolean holdingPrey = false;

	private final ModelPart ALL2;
	private final ModelPart ALL;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart left_eye;
	private final ModelPart left_pupil;
	private final ModelPart right_eye;
	private final ModelPart right_pupil;
	private final ModelPart front_head;
	private final ModelPart front_head_fin;
	private final ModelPart tentacles;
	private final ModelPart thick_left_tentacle_0_0;
	private final ModelPart thick_left_tentacle_0_1;
	private final ModelPart thick_left_tentacle_0_2;
	private final ModelPart thick_right_tentacle_0_0;
	private final ModelPart thick_right_tentacle_0_1;
	private final ModelPart thick_right_tentacle_0_2;
	private final ModelPart left_tentacle_0_0;
	private final ModelPart left_tentacle_0_1;
	private final ModelPart left_tentacle_0_2;
	private final ModelPart left_tentacle_0_3;
	private final ModelPart right_tentacle_0_0;
	private final ModelPart right_tentacle_0_1;
	private final ModelPart right_tentacle_0_2;
	private final ModelPart right_tentacle_0_3;
	private final ModelPart left_tentacle_1_0;
	private final ModelPart left_tentacle_1_1;
	private final ModelPart left_tentacle_1_2;
	private final ModelPart left_tentacle_1_3;
	private final ModelPart right_tentacle_1_0;
	private final ModelPart right_tentacle_1_1;
	private final ModelPart right_tentacle_1_2;
	private final ModelPart right_tentacle_1_3;
	private final ModelPart left_tentacle_2_0;
	private final ModelPart left_tentacle_2_1;
	private final ModelPart left_tentacle_2_2;
	private final ModelPart left_tentacle_2_3;
	private final ModelPart right_tentacle_2_0;
	private final ModelPart right_tentacle_2_1;
	private final ModelPart right_tentacle_2_2;
	private final ModelPart right_tentacle_2_3;
	private final ModelPart long_left_tentacle_0_0;
	private final ModelPart long_left_tentacle_0_1;
	private final ModelPart long_left_tentacle_0_2;
	private final ModelPart long_left_tentacle_0_3;
	private final ModelPart long_left_tentacle_0_4;
	private final ModelPart long_left_tentacle_0_5;
	private final ModelPart long_left_tentacle_0_6;
	private final ModelPart long_left_tentacle_0_7;
	private final ModelPart long_right_tentacle_0_0;
	private final ModelPart long_right_tentacle_0_1;
	private final ModelPart long_right_tentacle_0_2;
	private final ModelPart long_right_tentacle_0_3;
	private final ModelPart long_right_tentacle_0_4;
	private final ModelPart long_right_tentacle_0_5;
	private final ModelPart long_right_tentacle_0_6;
	private final ModelPart long_right_tentacle_0_7;

	public ColossalSquidModel(ModelPart root) {
		this.ALL2 = root.getChild("ALL2");
		this.ALL = this.ALL2.getChild("ALL");
		this.body = this.ALL.getChild("body");
		this.head = this.body.getChild("head");
		this.left_eye = this.head.getChild("left_eye");
		this.left_pupil = this.left_eye.getChild("left_pupil");
		this.right_eye = this.head.getChild("right_eye");
		this.right_pupil = this.right_eye.getChild("right_pupil");
		this.front_head = this.body.getChild("front_head");
		this.front_head_fin = this.front_head.getChild("front_head_fin");
		this.tentacles = this.body.getChild("tentacles");
		this.thick_left_tentacle_0_0 = this.tentacles.getChild("thick_left_tentacle_0_0");
		this.thick_left_tentacle_0_1 = this.thick_left_tentacle_0_0.getChild("thick_left_tentacle_0_1");
		this.thick_left_tentacle_0_2 = this.thick_left_tentacle_0_1.getChild("thick_left_tentacle_0_2");
		this.thick_right_tentacle_0_0 = this.tentacles.getChild("thick_right_tentacle_0_0");
		this.thick_right_tentacle_0_1 = this.thick_right_tentacle_0_0.getChild("thick_right_tentacle_0_1");
		this.thick_right_tentacle_0_2 = this.thick_right_tentacle_0_1.getChild("thick_right_tentacle_0_2");
		this.left_tentacle_0_0 = this.tentacles.getChild("left_tentacle_0_0");
		this.left_tentacle_0_1 = this.left_tentacle_0_0.getChild("left_tentacle_0_1");
		this.left_tentacle_0_2 = this.left_tentacle_0_1.getChild("left_tentacle_0_2");
		this.left_tentacle_0_3 = this.left_tentacle_0_2.getChild("left_tentacle_0_3");
		this.right_tentacle_0_0 = this.tentacles.getChild("right_tentacle_0_0");
		this.right_tentacle_0_1 = this.right_tentacle_0_0.getChild("right_tentacle_0_1");
		this.right_tentacle_0_2 = this.right_tentacle_0_1.getChild("right_tentacle_0_2");
		this.right_tentacle_0_3 = this.right_tentacle_0_2.getChild("right_tentacle_0_3");
		this.left_tentacle_1_0 = this.tentacles.getChild("left_tentacle_1_0");
		this.left_tentacle_1_1 = this.left_tentacle_1_0.getChild("left_tentacle_1_1");
		this.left_tentacle_1_2 = this.left_tentacle_1_1.getChild("left_tentacle_1_2");
		this.left_tentacle_1_3 = this.left_tentacle_1_2.getChild("left_tentacle_1_3");
		this.right_tentacle_1_0 = this.tentacles.getChild("right_tentacle_1_0");
		this.right_tentacle_1_1 = this.right_tentacle_1_0.getChild("right_tentacle_1_1");
		this.right_tentacle_1_2 = this.right_tentacle_1_1.getChild("right_tentacle_1_2");
		this.right_tentacle_1_3 = this.right_tentacle_1_2.getChild("right_tentacle_1_3");
		this.left_tentacle_2_0 = this.tentacles.getChild("left_tentacle_2_0");
		this.left_tentacle_2_1 = this.left_tentacle_2_0.getChild("left_tentacle_2_1");
		this.left_tentacle_2_2 = this.left_tentacle_2_1.getChild("left_tentacle_2_2");
		this.left_tentacle_2_3 = this.left_tentacle_2_2.getChild("left_tentacle_2_3");
		this.right_tentacle_2_0 = this.tentacles.getChild("right_tentacle_2_0");
		this.right_tentacle_2_1 = this.right_tentacle_2_0.getChild("right_tentacle_2_1");
		this.right_tentacle_2_2 = this.right_tentacle_2_1.getChild("right_tentacle_2_2");
		this.right_tentacle_2_3 = this.right_tentacle_2_2.getChild("right_tentacle_2_3");
		this.long_left_tentacle_0_0 = this.tentacles.getChild("long_left_tentacle_0_0");
		this.long_left_tentacle_0_1 = this.long_left_tentacle_0_0.getChild("long_left_tentacle_0_1");
		this.long_left_tentacle_0_2 = this.long_left_tentacle_0_1.getChild("long_left_tentacle_0_2");
		this.long_left_tentacle_0_3 = this.long_left_tentacle_0_2.getChild("long_left_tentacle_0_3");
		this.long_left_tentacle_0_4 = this.long_left_tentacle_0_3.getChild("long_left_tentacle_0_4");
		this.long_left_tentacle_0_5 = this.long_left_tentacle_0_4.getChild("long_left_tentacle_0_5");
		this.long_left_tentacle_0_6 = this.long_left_tentacle_0_5.getChild("long_left_tentacle_0_6");
		this.long_left_tentacle_0_7 = this.long_left_tentacle_0_6.getChild("long_left_tentacle_0_7");
		this.long_right_tentacle_0_0 = this.tentacles.getChild("long_right_tentacle_0_0");
		this.long_right_tentacle_0_1 = this.long_right_tentacle_0_0.getChild("long_right_tentacle_0_1");
		this.long_right_tentacle_0_2 = this.long_right_tentacle_0_1.getChild("long_right_tentacle_0_2");
		this.long_right_tentacle_0_3 = this.long_right_tentacle_0_2.getChild("long_right_tentacle_0_3");
		this.long_right_tentacle_0_4 = this.long_right_tentacle_0_3.getChild("long_right_tentacle_0_4");
		this.long_right_tentacle_0_5 = this.long_right_tentacle_0_4.getChild("long_right_tentacle_0_5");
		this.long_right_tentacle_0_6 = this.long_right_tentacle_0_5.getChild("long_right_tentacle_0_6");
		this.long_right_tentacle_0_7 = this.long_right_tentacle_0_6.getChild("long_right_tentacle_0_7");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 1.0F));

		PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(132, 75).addBox(-7.0F, -7.0F, -7.0F, 14.0F, 14.0F, 13.0F, new CubeDeformation(0.0F))
		.texOffs(220, 126).addBox(-4.0F, -2.0F, -11.0F, 8.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 0.0F));

		PartDefinition left_eye = head.addOrReplaceChild("left_eye", CubeListBuilder.create().texOffs(160, 247).addBox(0.0F, -5.0F, -5.0F, 3.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, 0.0F, 0.0F));

		PartDefinition left_pupil = left_eye.addOrReplaceChild("left_pupil", CubeListBuilder.create().texOffs(225, 81).addBox(0.0F, -3.0F, -3.0F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 0.0F, 0.0F));

		PartDefinition right_eye = head.addOrReplaceChild("right_eye", CubeListBuilder.create().texOffs(160, 247).mirror().addBox(-3.0F, -5.0F, -5.0F, 3.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-7.0F, 0.0F, 0.0F));

		PartDefinition right_pupil = right_eye.addOrReplaceChild("right_pupil", CubeListBuilder.create().texOffs(225, 81).mirror().addBox(-1.0F, -3.0F, -3.0F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.0F, 0.0F, 0.0F));

		PartDefinition front_head = body.addOrReplaceChild("front_head", CubeListBuilder.create().texOffs(0, 75).addBox(-10.0F, -9.0F, 0.0F, 20.0F, 18.0F, 46.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 6.0F));

		PartDefinition front_head_fin = front_head.addOrReplaceChild("front_head_fin", CubeListBuilder.create().texOffs(-15, -10).addBox(-22.0F, 0.0F, 0.0F, 44.0F, 0.0F, 85.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition tentacles = body.addOrReplaceChild("tentacles", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition thick_left_tentacle_0_0 = tentacles.addOrReplaceChild("thick_left_tentacle_0_0", CubeListBuilder.create().texOffs(96, 139).addBox(-1.0F, -2.0F, -15.0F, 2.0F, 4.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, -2.0F, -5.0F));

		PartDefinition thick_left_tentacle_0_1 = thick_left_tentacle_0_0.addOrReplaceChild("thick_left_tentacle_0_1", CubeListBuilder.create().texOffs(96, 158).addBox(-1.0F, -2.0F, -15.0F, 2.0F, 4.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -15.0F));

		PartDefinition thick_left_tentacle_0_2 = thick_left_tentacle_0_1.addOrReplaceChild("thick_left_tentacle_0_2", CubeListBuilder.create().texOffs(96, 139).addBox(-1.0F, -2.0F, -15.0F, 2.0F, 4.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -15.0F));

		PartDefinition thick_right_tentacle_0_0 = tentacles.addOrReplaceChild("thick_right_tentacle_0_0", CubeListBuilder.create().texOffs(96, 139).mirror().addBox(-1.0F, -2.0F, -15.0F, 2.0F, 4.0F, 15.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-8.0F, -2.0F, -5.0F));

		PartDefinition thick_right_tentacle_0_1 = thick_right_tentacle_0_0.addOrReplaceChild("thick_right_tentacle_0_1", CubeListBuilder.create().texOffs(96, 158).mirror().addBox(-1.0F, -2.0F, -15.0F, 2.0F, 4.0F, 15.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, -15.0F));

		PartDefinition thick_right_tentacle_0_2 = thick_right_tentacle_0_1.addOrReplaceChild("thick_right_tentacle_0_2", CubeListBuilder.create().texOffs(96, 139).mirror().addBox(-1.0F, -2.0F, -15.0F, 2.0F, 4.0F, 15.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, -15.0F));

		PartDefinition left_tentacle_0_0 = tentacles.addOrReplaceChild("left_tentacle_0_0", CubeListBuilder.create().texOffs(170, 215).addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, -5.0F, -7.0F));

		PartDefinition left_tentacle_0_1 = left_tentacle_0_0.addOrReplaceChild("left_tentacle_0_1", CubeListBuilder.create().texOffs(170, 215).addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, -14.0F));

		PartDefinition left_tentacle_0_2 = left_tentacle_0_1.addOrReplaceChild("left_tentacle_0_2", CubeListBuilder.create().texOffs(170, 215).addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, -14.0F));

		PartDefinition left_tentacle_0_3 = left_tentacle_0_2.addOrReplaceChild("left_tentacle_0_3", CubeListBuilder.create().texOffs(170, 215).addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, -14.0F));

		PartDefinition right_tentacle_0_0 = tentacles.addOrReplaceChild("right_tentacle_0_0", CubeListBuilder.create().texOffs(170, 215).mirror().addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(-5.0F, -5.0F, -7.0F));

		PartDefinition right_tentacle_0_1 = right_tentacle_0_0.addOrReplaceChild("right_tentacle_0_1", CubeListBuilder.create().texOffs(170, 215).mirror().addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(0.0F, 0.0F, -14.0F));

		PartDefinition right_tentacle_0_2 = right_tentacle_0_1.addOrReplaceChild("right_tentacle_0_2", CubeListBuilder.create().texOffs(170, 215).mirror().addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(0.0F, 0.0F, -14.0F));

		PartDefinition right_tentacle_0_3 = right_tentacle_0_2.addOrReplaceChild("right_tentacle_0_3", CubeListBuilder.create().texOffs(170, 215).mirror().addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(0.0F, 0.0F, -14.0F));

		PartDefinition left_tentacle_1_0 = tentacles.addOrReplaceChild("left_tentacle_1_0", CubeListBuilder.create().texOffs(170, 215).addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.25F)), PartPose.offset(6.0F, 3.0F, -7.0F));

		PartDefinition left_tentacle_1_1 = left_tentacle_1_0.addOrReplaceChild("left_tentacle_1_1", CubeListBuilder.create().texOffs(170, 215).addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, -14.0F));

		PartDefinition left_tentacle_1_2 = left_tentacle_1_1.addOrReplaceChild("left_tentacle_1_2", CubeListBuilder.create().texOffs(170, 215).addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, -14.0F));

		PartDefinition left_tentacle_1_3 = left_tentacle_1_2.addOrReplaceChild("left_tentacle_1_3", CubeListBuilder.create().texOffs(170, 215).addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, -14.0F));

		PartDefinition right_tentacle_1_0 = tentacles.addOrReplaceChild("right_tentacle_1_0", CubeListBuilder.create().texOffs(170, 215).mirror().addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(-6.0F, 3.0F, -7.0F));

		PartDefinition right_tentacle_1_1 = right_tentacle_1_0.addOrReplaceChild("right_tentacle_1_1", CubeListBuilder.create().texOffs(170, 215).mirror().addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(0.0F, 0.0F, -14.0F));

		PartDefinition right_tentacle_1_2 = right_tentacle_1_1.addOrReplaceChild("right_tentacle_1_2", CubeListBuilder.create().texOffs(170, 215).mirror().addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(0.0F, 0.0F, -14.0F));

		PartDefinition right_tentacle_1_3 = right_tentacle_1_2.addOrReplaceChild("right_tentacle_1_3", CubeListBuilder.create().texOffs(170, 215).mirror().addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(0.0F, 0.0F, -14.0F));

		PartDefinition left_tentacle_2_0 = tentacles.addOrReplaceChild("left_tentacle_2_0", CubeListBuilder.create().texOffs(170, 215).addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 7.0F, -7.0F));

		PartDefinition left_tentacle_2_1 = left_tentacle_2_0.addOrReplaceChild("left_tentacle_2_1", CubeListBuilder.create().texOffs(170, 215).addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, -14.0F));

		PartDefinition left_tentacle_2_2 = left_tentacle_2_1.addOrReplaceChild("left_tentacle_2_2", CubeListBuilder.create().texOffs(170, 215).addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, -14.0F));

		PartDefinition left_tentacle_2_3 = left_tentacle_2_2.addOrReplaceChild("left_tentacle_2_3", CubeListBuilder.create().texOffs(170, 215).addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, -14.0F));

		PartDefinition right_tentacle_2_0 = tentacles.addOrReplaceChild("right_tentacle_2_0", CubeListBuilder.create().texOffs(170, 215).mirror().addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(-5.0F, 7.0F, -7.0F));

		PartDefinition right_tentacle_2_1 = right_tentacle_2_0.addOrReplaceChild("right_tentacle_2_1", CubeListBuilder.create().texOffs(170, 215).mirror().addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(0.0F, 0.0F, -14.0F));

		PartDefinition right_tentacle_2_2 = right_tentacle_2_1.addOrReplaceChild("right_tentacle_2_2", CubeListBuilder.create().texOffs(170, 215).mirror().addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(0.0F, 0.0F, -14.0F));

		PartDefinition right_tentacle_2_3 = right_tentacle_2_2.addOrReplaceChild("right_tentacle_2_3", CubeListBuilder.create().texOffs(170, 215).mirror().addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(0.0F, 0.0F, -14.0F));

		PartDefinition long_left_tentacle_0_0 = tentacles.addOrReplaceChild("long_left_tentacle_0_0", CubeListBuilder.create().texOffs(172, 136).addBox(-1.0F, -1.0F, -17.0F, 2.0F, 2.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, -1.0F, -7.0F));

		PartDefinition long_left_tentacle_0_1 = long_left_tentacle_0_0.addOrReplaceChild("long_left_tentacle_0_1", CubeListBuilder.create().texOffs(172, 136).addBox(-1.0F, -1.0F, -17.0F, 2.0F, 2.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -17.0F));

		PartDefinition long_left_tentacle_0_2 = long_left_tentacle_0_1.addOrReplaceChild("long_left_tentacle_0_2", CubeListBuilder.create().texOffs(172, 136).addBox(-1.0F, -1.0F, -17.0F, 2.0F, 2.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -17.0F));

		PartDefinition long_left_tentacle_0_3 = long_left_tentacle_0_2.addOrReplaceChild("long_left_tentacle_0_3", CubeListBuilder.create().texOffs(172, 136).addBox(-1.0F, -1.0F, -17.0F, 2.0F, 2.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -17.0F));

		PartDefinition long_left_tentacle_0_4 = long_left_tentacle_0_3.addOrReplaceChild("long_left_tentacle_0_4", CubeListBuilder.create().texOffs(172, 136).addBox(-1.0F, -1.0F, -17.0F, 2.0F, 2.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -17.0F));

		PartDefinition long_left_tentacle_0_5 = long_left_tentacle_0_4.addOrReplaceChild("long_left_tentacle_0_5", CubeListBuilder.create().texOffs(172, 136).addBox(-1.0F, -1.0F, -17.0F, 2.0F, 2.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -17.0F));

		PartDefinition long_left_tentacle_0_6 = long_left_tentacle_0_5.addOrReplaceChild("long_left_tentacle_0_6", CubeListBuilder.create().texOffs(172, 136).addBox(-1.0F, -1.0F, -17.0F, 2.0F, 2.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -17.0F));

		PartDefinition long_left_tentacle_0_7 = long_left_tentacle_0_6.addOrReplaceChild("long_left_tentacle_0_7", CubeListBuilder.create().texOffs(172, 136).addBox(-1.0F, -1.0F, -17.0F, 2.0F, 2.0F, 17.0F, new CubeDeformation(0.0F))
		.texOffs(132, 102).addBox(-2.0F, -6.0F, -34.0F, 3.0F, 12.0F, 17.0F, new CubeDeformation(0.0F))
		.texOffs(0, 139).addBox(-9.0F, -5.0F, -34.0F, 7.0F, 0.0F, 17.0F, new CubeDeformation(0.01F))
		.texOffs(0, 139).addBox(-9.0F, 5.0F, -34.0F, 7.0F, 0.0F, 17.0F, new CubeDeformation(0.01F))
		.texOffs(0, 139).addBox(-9.0F, 2.0F, -34.0F, 7.0F, 0.0F, 17.0F, new CubeDeformation(0.01F))
		.texOffs(0, 139).addBox(-9.0F, -2.0F, -34.0F, 7.0F, 0.0F, 17.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 0.0F, -17.0F));

		PartDefinition long_right_tentacle_0_0 = tentacles.addOrReplaceChild("long_right_tentacle_0_0", CubeListBuilder.create().texOffs(172, 136).mirror().addBox(-1.0F, -1.0F, -17.0F, 2.0F, 2.0F, 17.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-5.0F, -1.0F, -7.0F));

		PartDefinition long_right_tentacle_0_1 = long_right_tentacle_0_0.addOrReplaceChild("long_right_tentacle_0_1", CubeListBuilder.create().texOffs(172, 136).mirror().addBox(-1.0F, -1.0F, -17.0F, 2.0F, 2.0F, 17.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, -17.0F));

		PartDefinition long_right_tentacle_0_2 = long_right_tentacle_0_1.addOrReplaceChild("long_right_tentacle_0_2", CubeListBuilder.create().texOffs(172, 136).mirror().addBox(-1.0F, -1.0F, -17.0F, 2.0F, 2.0F, 17.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, -17.0F));

		PartDefinition long_right_tentacle_0_3 = long_right_tentacle_0_2.addOrReplaceChild("long_right_tentacle_0_3", CubeListBuilder.create().texOffs(172, 136).mirror().addBox(-1.0F, -1.0F, -17.0F, 2.0F, 2.0F, 17.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, -17.0F));

		PartDefinition long_right_tentacle_0_4 = long_right_tentacle_0_3.addOrReplaceChild("long_right_tentacle_0_4", CubeListBuilder.create().texOffs(172, 136).mirror().addBox(-1.0F, -1.0F, -17.0F, 2.0F, 2.0F, 17.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, -17.0F));

		PartDefinition long_right_tentacle_0_5 = long_right_tentacle_0_4.addOrReplaceChild("long_right_tentacle_0_5", CubeListBuilder.create().texOffs(172, 136).mirror().addBox(-1.0F, -1.0F, -17.0F, 2.0F, 2.0F, 17.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, -17.0F));

		PartDefinition long_right_tentacle_0_6 = long_right_tentacle_0_5.addOrReplaceChild("long_right_tentacle_0_6", CubeListBuilder.create().texOffs(172, 136).mirror().addBox(-1.0F, -1.0F, -17.0F, 2.0F, 2.0F, 17.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, -17.0F));

		PartDefinition long_right_tentacle_0_7 = long_right_tentacle_0_6.addOrReplaceChild("long_right_tentacle_0_7", CubeListBuilder.create().texOffs(172, 136).mirror().addBox(-1.0F, -1.0F, -17.0F, 2.0F, 2.0F, 17.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(132, 102).mirror().addBox(-1.0F, -6.0F, -34.0F, 3.0F, 12.0F, 17.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 139).mirror().addBox(2.0F, -5.0F, -34.0F, 7.0F, 0.0F, 17.0F, new CubeDeformation(0.01F)).mirror(false)
		.texOffs(0, 139).mirror().addBox(2.0F, 5.0F, -34.0F, 7.0F, 0.0F, 17.0F, new CubeDeformation(0.01F)).mirror(false)
		.texOffs(0, 139).mirror().addBox(2.0F, 2.0F, -34.0F, 7.0F, 0.0F, 17.0F, new CubeDeformation(0.01F)).mirror(false)
		.texOffs(0, 139).mirror().addBox(2.0F, -2.0F, -34.0F, 7.0F, 0.0F, 17.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(0.0F, 0.0F, -17.0F));

		return LayerDefinition.create(meshdefinition, 512, 512);
	}

	@Override
	protected AnimationDefinition comboAnimation(int index) {
		if (this.holdingPrey) {
			return switch (index) {
				case 1, 3 -> ColossalSquidAnimations.GRAB_STRIKE;
				case 2 -> ColossalSquidAnimations.GRAB_STRIKE_2;
				default -> null;
			};
		}
		return switch (index) {
			case 1 -> ColossalSquidAnimations.ATTACK_STRIKE;
			case 2 -> ColossalSquidAnimations.ATTACK_STRIKE_2;
			case 3 -> ColossalSquidAnimations.ATTACK_STRIKE_3;
			default -> null;
		};
	}

	@Override
	public void setupAnim(T squid, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		float sedation = OWSedationAnimator.weight(squid, SEDATION, ageInTicks);

		if (Math.abs(this.externalRiderPitch) > 0.01f) this.ALL2.xRot = (float) Math.toRadians(this.externalRiderPitch);
		if (Math.abs(this.externalBankRoll) > 0.01f) this.ALL2.zRot = (float) Math.toRadians(this.externalBankRoll);

		if (sedation < 1f) this.animateAwake(squid, limbSwing, limbSwingAmount, ageInTicks);

		OWSedationAnimator.apply(this, squid, SEDATION, ageInTicks);
		this.animate(squid.eyesAnimationState, ColossalSquidAnimations.EYES, ageInTicks, 1.0f);

		if (squid.isBaby()) {
			float maturation = (float) squid.getMaturationPercentage() / 100f;
			float headScale = BABY_HEAD_SCALE - (BABY_HEAD_SCALE - 1.0f) * maturation;
			this.head.xScale *= headScale;
			this.head.yScale *= headScale;
			this.head.zScale *= headScale;
		}

		if (sedation < 1f) this.guideTentacles(squid, ageInTicks, 1f - sedation);
		this.captureBodyState(squid);
	}

	private static final float GUIDE_LIMIT = 1.2f;
	private static final float[] LONG_GUIDE = {0.42f, 0.2f, 0.12f, 0.08f, 0.06f, 0.05f, 0.04f, 0.03f};
	private static final float[] ARM_GUIDE = {0.32f, 0.16f, 0.1f, 0.06f};
	private static final float[] THICK_GUIDE = {0.4f, 0.2f, 0.1f};

	/**
	 * Oriente les tentacules vers la proie pendant la détente, la traction et la tenue : la saisie
	 * ne frappe plus droit devant quand la cible est sur le côté, au-dessus ou en dessous.
	 */
	private void guideTentacles(ColossalSquidEntity squid, float ageInTicks, float strength) {
		if (!squid.isCombatFacingVisual() || squid.isTurning()) return;
		float weight = squid.getGrabGuideWeight(ageInTicks) * strength;
		if (weight <= 0.001f) return;
		LivingEntity prey = squid.getGrabFocus();
		if (prey == null) return;

		float pt = Mth.clamp(ageInTicks - squid.tickCount, 0f, 1f);
		float yaw = Mth.rotLerp(pt, squid.yBodyRotO, squid.yBodyRot);
		float scale = squid.getScale();
		net.minecraft.world.phys.Vec3 forward = net.minecraft.world.phys.Vec3.directionFromRotation(0f, yaw);
		net.minecraft.world.phys.Vec3 root = squid.getPosition(pt).add(forward.scale(1.4 * scale)).add(0, 0.5 * scale, 0);
		net.minecraft.world.phys.Vec3 toPrey = prey.getPosition(pt).add(0, prey.getBbHeight() * 0.5, 0).subtract(root);
		net.minecraft.world.phys.Vec3 local = toPrey.yRot((float) Math.toRadians(yaw - 180f));
		double mx = -local.x, my = -local.y, mz = local.z;
		float guideYaw = Mth.clamp((float) Mth.atan2(-mx, -mz), -GUIDE_LIMIT, GUIDE_LIMIT) * weight;
		float guidePitch = Mth.clamp((float) Mth.atan2(my, Math.sqrt(mx * mx + mz * mz)) - this.ALL2.xRot, -GUIDE_LIMIT, GUIDE_LIMIT) * weight;

		guideChain(guideYaw, guidePitch, LONG_GUIDE, 1f, this.long_left_tentacle_0_0, this.long_left_tentacle_0_1, this.long_left_tentacle_0_2,
				this.long_left_tentacle_0_3, this.long_left_tentacle_0_4, this.long_left_tentacle_0_5, this.long_left_tentacle_0_6, this.long_left_tentacle_0_7);
		guideChain(guideYaw, guidePitch, LONG_GUIDE, 1f, this.long_right_tentacle_0_0, this.long_right_tentacle_0_1, this.long_right_tentacle_0_2,
				this.long_right_tentacle_0_3, this.long_right_tentacle_0_4, this.long_right_tentacle_0_5, this.long_right_tentacle_0_6, this.long_right_tentacle_0_7);
		guideChain(guideYaw, guidePitch, THICK_GUIDE, 1f, this.thick_left_tentacle_0_0, this.thick_left_tentacle_0_1, this.thick_left_tentacle_0_2);
		guideChain(guideYaw, guidePitch, THICK_GUIDE, 1f, this.thick_right_tentacle_0_0, this.thick_right_tentacle_0_1, this.thick_right_tentacle_0_2);
		guideChain(guideYaw, guidePitch, ARM_GUIDE, 1f, this.left_tentacle_0_0, this.left_tentacle_0_1, this.left_tentacle_0_2, this.left_tentacle_0_3);
		guideChain(guideYaw, guidePitch, ARM_GUIDE, 1f, this.right_tentacle_0_0, this.right_tentacle_0_1, this.right_tentacle_0_2, this.right_tentacle_0_3);
		guideChain(guideYaw, guidePitch, ARM_GUIDE, 1f, this.left_tentacle_1_0, this.left_tentacle_1_1, this.left_tentacle_1_2, this.left_tentacle_1_3);
		guideChain(guideYaw, guidePitch, ARM_GUIDE, 1f, this.right_tentacle_1_0, this.right_tentacle_1_1, this.right_tentacle_1_2, this.right_tentacle_1_3);
		guideChain(guideYaw, guidePitch, ARM_GUIDE, 1f, this.left_tentacle_2_0, this.left_tentacle_2_1, this.left_tentacle_2_2, this.left_tentacle_2_3);
		guideChain(guideYaw, guidePitch, ARM_GUIDE, 1f, this.right_tentacle_2_0, this.right_tentacle_2_1, this.right_tentacle_2_2, this.right_tentacle_2_3);
	}

	private static void guideChain(float yaw, float pitch, float[] shares, float amount, ModelPart... chain) {
		for (int i = 0; i < chain.length; i++) {
			chain[i].yRot += yaw * shares[i] * amount;
			chain[i].xRot += pitch * shares[i] * amount;
		}
	}

	private void animateAwake(T squid, float limbSwing, float limbSwingAmount, float ageInTicks) {
		this.holdingPrey = squid.getGrabPhase() == ColossalSquidEntity.GRAB_HOLD;

		if (squid.isInkAnimating()) {
			this.animate(squid.inkAnimationState, ColossalSquidAnimations.ULTIMATE_INK, ageInTicks, 1.0f);
			return;
		}

		if (squid.isTurning()) {
			boolean toCombat = squid.turnToCombatAnimationState.isStarted();
			float elapsed = ageInTicks - squid.turnStartTick;
			float duration = ColossalSquidEntity.getTurnDuration(toCombat);
			float fade = toCombat ? TURN_FADE_TICKS : TURN_TO_SWIM_FADE_TICKS;
			float fadeIn = smoothStep(Mth.clamp(elapsed / fade, 0f, 1f));
			float fadeOut = smoothStep(Mth.clamp((duration - elapsed) / fade, 0f, 1f));
			boolean leaving = fadeIn < 1f;
			float settle = leaving ? 1f - fadeIn : 1f - fadeOut;
			if (settle > 0.001f) {
				if (toCombat != leaving) this.animateCombatFacing(squid, limbSwing, limbSwingAmount, ageInTicks, settle);
				else this.animateSwimFacing(squid, limbSwing, limbSwingAmount, ageInTicks, settle);
			}
			AnimationState state = toCombat ? squid.turnToCombatAnimationState : squid.turnToSwimAnimationState;
			this.animateWeighted(state, toCombat ? ColossalSquidAnimations.TURN_TO_COMBAT : ColossalSquidAnimations.TURN_TO_SWIM,
					ageInTicks, TURN_ANIM_TICKS / duration, Math.min(fadeIn, fadeOut));
			if (toCombat) this.animateCombos(squid, ageInTicks, 1.0f);
			return;
		}

		if (squid.isCombatFacingVisual()) {
			this.animateCombatFacing(squid, limbSwing, limbSwingAmount, ageInTicks, 1f);
			this.animateCombos(squid, ageInTicks, 1.0f);
		} else {
			this.animateSwimFacing(squid, limbSwing, limbSwingAmount, ageInTicks, 1f);
		}
	}

	private void animateSwimFacing(T squid, float limbSwing, float limbSwingAmount, float ageInTicks, float weight) {
		if (squid.isSitting()) {
			this.animateWeighted(squid.sittingAnimationState, ColossalSquidAnimations.SIT, ageInTicks, 1.0f, weight);
			return;
		}
		if (squid.isNapping()) {
			this.animateWeighted(squid.napAnimationState, ColossalSquidAnimations.NAP, ageInTicks, 1.0f, weight);
			return;
		}
		float beached = squid.getBeachedBlend(Mth.clamp(ageInTicks - squid.tickCount, 0f, 1f));
		if (beached > 0.001f) {
			this.animateWeighted(squid.beachedAnimationState, ColossalSquidAnimations.BEACHED, ageInTicks, 1.0f, weight * beached);
			if (beached >= 0.999f) return;
			weight *= 1f - beached;
		}

		float moving = Math.min(limbSwingAmount * SWIM_WEIGHT_FACTOR, 1f);
		AnimationDefinition swim = ColossalSquidAnimations.MOVE_SWIM;
		float swimWeight = moving * weight;
		if (swimWeight > 0.001f) {
			float partialTick = ageInTicks - Mth.floor(ageInTicks);
			long timeMs = (long) (squid.getSwimPhase(partialTick) * swim.lengthInSeconds() * 1000f);
			KeyframeAnimations.animate(this, swim, timeMs, swimWeight, ANIM_VECTOR);
		}
		this.animateWeighted(squid.idleAnimationState, ColossalSquidAnimations.MISC_IDLE, ageInTicks, 1.0f, (1f - moving) * weight);
	}

	private void animateCombatFacing(T squid, float limbSwing, float limbSwingAmount, float ageInTicks, float weight) {
		switch (squid.getGrabPhase()) {
			case ColossalSquidEntity.GRAB_PREPARE -> {
				this.animateWeighted(squid.grabPrepareAnimationState, ColossalSquidAnimations.GRAB_PREPARE, ageInTicks, 1.0f, weight);
				return;
			}
			case ColossalSquidEntity.GRAB_LUNGE -> {
				this.animateWeighted(squid.grabLungeAnimationState, ColossalSquidAnimations.GRAB_LUNGE, ageInTicks, 1.0f, weight);
				return;
			}
			case ColossalSquidEntity.GRAB_PULL -> {
				float pullSpeed = (float) OWAttacksConstants.ColossalSquid.GRAB_PULL_TICKS / squid.getGrabPullTicks();
				this.animateWeighted(squid.grabPullAnimationState, ColossalSquidAnimations.GRAB_PULL, ageInTicks, pullSpeed, weight);
				return;
			}
			case ColossalSquidEntity.GRAB_HOLD -> {
				this.animateWeighted(squid.grabHoldAnimationState, ColossalSquidAnimations.GRAB_HOLD, ageInTicks, 1.0f, weight);
				return;
			}
			case ColossalSquidEntity.GRAB_RELEASE -> {
				this.animateWeighted(squid.grabReleaseAnimationState, ColossalSquidAnimations.GRAB_RELEASE, ageInTicks, 1.0f, weight);
				return;
			}
			case ColossalSquidEntity.GRAB_MISS -> {
				this.animateWeighted(squid.grabMissAnimationState, ColossalSquidAnimations.GRAB_MISS, ageInTicks, 1.0f, weight);
				return;
			}
			default -> { }
		}

		float moving = Math.min(limbSwingAmount * SWIM_WEIGHT_FACTOR, 1f);
		if (moving * weight > 0.001f) {
			KeyframeAnimations.animate(this, ColossalSquidAnimations.COMBAT_SWIM, (long) (limbSwing * 50f * SWIM_ANIM_SPEED), moving * weight, ANIM_VECTOR);
		}
		this.animateWeighted(squid.combatIdleAnimationState, ColossalSquidAnimations.COMBAT_IDLE, ageInTicks, 1.0f, (1f - moving) * weight);
	}

	private static float smoothStep(float t) {
		return t * t * (3f - 2f * t);
	}

	private void captureBodyState(ColossalSquidEntity squid) {
		if (!squid.level().isClientSide()) return;

		squid.setBodyZRot((float) Math.toDegrees(this.ALL2.zRot));
		squid.setBodyXRot((float) -Math.toDegrees(this.ALL2.xRot));

		PoseStack bones = new PoseStack();
		this.ALL2.translateAndRotate(bones);
		squid.riderMatrix = new Matrix4f(bones.last().pose());
		this.ALL.translateAndRotate(bones);
		this.body.translateAndRotate(bones);
		this.front_head.translateAndRotate(bones);
		squid.seatMatrix = new Matrix4f(bones.last().pose());
		squid.bodyAnimY = 0f;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		this.ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return this.ALL2;
	}

	public void copyPoseFrom(ColossalSquidModel<?> src) {
		java.util.List<ModelPart> mine = this.root().getAllParts().toList();
		java.util.List<ModelPart> theirs = src.root().getAllParts().toList();
		for (int i = 0; i < Math.min(mine.size(), theirs.size()); i++) mine.get(i).copyFrom(theirs.get(i));
	}
}
