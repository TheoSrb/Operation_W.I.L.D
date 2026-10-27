package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.animation.TheBiologicalErrorAnimations;
import org.joml.Vector3f;

public class TheBiologicalErrorModel<T extends Entity> extends HierarchicalModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "the_biological_error"), "main");

	private static final float WALK_ANIM_SPEED = 1.5f;
	private static final float WALK_WEIGHT_FACTOR = 3.0f;
	private static final Vector3f ANIM_VECTOR = new Vector3f();

	private final ModelPart ALL2;
	private final ModelPart ALL;
	private final ModelPart body;
	private final ModelPart body_1;
	private final ModelPart neck;
	private final ModelPart head;
	private final ModelPart head_2;
	private final ModelPart left_chelicer;
	private final ModelPart left_chelicer_2;
	private final ModelPart right_chelicer;
	private final ModelPart right_chelicer_2;
	private final ModelPart left_ant_chelicer;
	private final ModelPart left_ant_chelicer_2;
	private final ModelPart right_ant_chelicer;
	private final ModelPart right_ant_chelicer_2;
	private final ModelPart left_ant_antenna;
	private final ModelPart left_ant_antenna_2;
	private final ModelPart left_ant_antenna_3;
	private final ModelPart right_ant_antenna;
	private final ModelPart right_ant_antenna_2;
	private final ModelPart right_ant_antenna_3;
	private final ModelPart right_eye;
	private final ModelPart left_eye;
	private final ModelPart ratings;
	private final ModelPart left_rating_0;
	private final ModelPart right_rating_0;
	private final ModelPart left_rating_1;
	private final ModelPart right_rating_1;
	private final ModelPart left_rating_2;
	private final ModelPart right_rating_2;
	private final ModelPart left_arm;
	private final ModelPart left_arm_0;
	private final ModelPart left_arm_1;
	private final ModelPart claw;
	private final ModelPart claw_Up;
	private final ModelPart claw_Down;
	private final ModelPart right_arm;
	private final ModelPart right_arm_0;
	private final ModelPart right_arm_1;
	private final ModelPart hand;
	private final ModelPart finger_0;
	private final ModelPart phalange_0;
	private final ModelPart finger_1;
	private final ModelPart phalange_1;
	private final ModelPart finger_2;
	private final ModelPart phalange_2;
	private final ModelPart finger_3;
	private final ModelPart phalange_3;
	private final ModelPart tentacles;
	private final ModelPart left_tentacle_0_0;
	private final ModelPart left_tentacle_0_1;
	private final ModelPart left_tentacle_0_2;
	private final ModelPart left_tentacle_0_3;
	private final ModelPart left_tentacle_0_4;
	private final ModelPart left_tentacle_1_0;
	private final ModelPart left_tentacle_1_1;
	private final ModelPart left_tentacle_1_2;
	private final ModelPart left_tentacle_1_3;
	private final ModelPart left_tentacle_0_5;
	private final ModelPart right_tentacle_0_0;
	private final ModelPart right_tentacle_0_1;
	private final ModelPart right_tentacle_0_2;
	private final ModelPart right_tentacle_0_3;
	private final ModelPart right_tentacle_0_4;
	private final ModelPart right_tentacle_1_0;
	private final ModelPart right_tentacle_1_1;
	private final ModelPart right_tentacle_1_2;
	private final ModelPart right_tentacle_1_3;
	private final ModelPart right_tentacle_0_5;
	private final ModelPart body_2;
	private final ModelPart tail;
	private final ModelPart tail_0;
	private final ModelPart tail_1;
	private final ModelPart tail_2;
	private final ModelPart tail_3;
	private final ModelPart legs;
	private final ModelPart left_legs;
	private final ModelPart left_leg_0_0;
	private final ModelPart left_leg_0_1;
	private final ModelPart left_leg_0_2;
	private final ModelPart left_leg_1_0;
	private final ModelPart left_leg_1_1;
	private final ModelPart left_leg_1_2;
	private final ModelPart left_leg_2_0;
	private final ModelPart left_leg_2_1;
	private final ModelPart left_leg_2_2;
	private final ModelPart left_leg_3_0;
	private final ModelPart left_leg_3_1;
	private final ModelPart left_leg_3_2;
	private final ModelPart right_legs;
	private final ModelPart right_leg_0_0;
	private final ModelPart right_leg_0_1;
	private final ModelPart right_leg_0_2;
	private final ModelPart right_leg_1_0;
	private final ModelPart right_leg_1_1;
	private final ModelPart right_leg_1_2;
	private final ModelPart right_leg_2_0;
	private final ModelPart right_leg_2_1;
	private final ModelPart right_leg_2_2;
	private final ModelPart right_leg_3_0;
	private final ModelPart right_leg_3_1;
	private final ModelPart right_leg_3_2;
	private final ModelPart left_chelicer_3;
	private final ModelPart right_chelicer_3;
	private final ModelPart left_tentacle_0_7;
	private final ModelPart left_tentacle_0_8;
	private final ModelPart left_tentacle_0_9;
	private final ModelPart left_tentacle_0_10;
	private final ModelPart left_tentacle_1_5;
	private final ModelPart left_tentacle_1_6;
	private final ModelPart left_tentacle_1_7;
	private final ModelPart left_tentacle_0_6;

	public TheBiologicalErrorModel(ModelPart root) {
		this.ALL2 = root.getChild("ALL2");
		this.ALL = this.ALL2.getChild("ALL");
		this.body = this.ALL.getChild("body");
		this.body_1 = this.body.getChild("body_1");
		this.neck = this.body_1.getChild("neck");
		this.head = this.neck.getChild("head");
		this.head_2 = this.head.getChild("head_2");
		this.left_chelicer = this.head.getChild("left_chelicer");
		this.left_chelicer_2 = this.left_chelicer.getChild("left_chelicer_2");
		this.right_chelicer = this.head.getChild("right_chelicer");
		this.right_chelicer_2 = this.right_chelicer.getChild("right_chelicer_2");
		this.left_ant_chelicer = this.head.getChild("left_ant_chelicer");
		this.left_ant_chelicer_2 = this.left_ant_chelicer.getChild("left_ant_chelicer_2");
		this.right_ant_chelicer = this.head.getChild("right_ant_chelicer");
		this.right_ant_chelicer_2 = this.right_ant_chelicer.getChild("right_ant_chelicer_2");
		this.left_ant_antenna = this.head.getChild("left_ant_antenna");
		this.left_ant_antenna_2 = this.left_ant_antenna.getChild("left_ant_antenna_2");
		this.left_ant_antenna_3 = this.left_ant_antenna_2.getChild("left_ant_antenna_3");
		this.right_ant_antenna = this.head.getChild("right_ant_antenna");
		this.right_ant_antenna_2 = this.right_ant_antenna.getChild("right_ant_antenna_2");
		this.right_ant_antenna_3 = this.right_ant_antenna_2.getChild("right_ant_antenna_3");
		this.right_eye = this.head.getChild("right_eye");
		this.left_eye = this.head.getChild("left_eye");
		this.ratings = this.body_1.getChild("ratings");
		this.left_rating_0 = this.ratings.getChild("left_rating_0");
		this.right_rating_0 = this.ratings.getChild("right_rating_0");
		this.left_rating_1 = this.ratings.getChild("left_rating_1");
		this.right_rating_1 = this.ratings.getChild("right_rating_1");
		this.left_rating_2 = this.ratings.getChild("left_rating_2");
		this.right_rating_2 = this.ratings.getChild("right_rating_2");
		this.left_arm = this.body_1.getChild("left_arm");
		this.left_arm_0 = this.left_arm.getChild("left_arm_0");
		this.left_arm_1 = this.left_arm.getChild("left_arm_1");
		this.claw = this.left_arm_1.getChild("claw");
		this.claw_Up = this.claw.getChild("claw_Up");
		this.claw_Down = this.claw.getChild("claw_Down");
		this.right_arm = this.body_1.getChild("right_arm");
		this.right_arm_0 = this.right_arm.getChild("right_arm_0");
		this.right_arm_1 = this.right_arm.getChild("right_arm_1");
		this.hand = this.right_arm_1.getChild("hand");
		this.finger_0 = this.hand.getChild("finger_0");
		this.phalange_0 = this.finger_0.getChild("phalange_0");
		this.finger_1 = this.hand.getChild("finger_1");
		this.phalange_1 = this.finger_1.getChild("phalange_1");
		this.finger_2 = this.hand.getChild("finger_2");
		this.phalange_2 = this.finger_2.getChild("phalange_2");
		this.finger_3 = this.hand.getChild("finger_3");
		this.phalange_3 = this.finger_3.getChild("phalange_3");
		this.tentacles = this.body_1.getChild("tentacles");
		this.left_tentacle_0_0 = this.tentacles.getChild("left_tentacle_0_0");
		this.left_tentacle_0_1 = this.left_tentacle_0_0.getChild("left_tentacle_0_1");
		this.left_tentacle_0_2 = this.left_tentacle_0_1.getChild("left_tentacle_0_2");
		this.left_tentacle_0_3 = this.left_tentacle_0_2.getChild("left_tentacle_0_3");
		this.left_tentacle_0_4 = this.left_tentacle_0_3.getChild("left_tentacle_0_4");
		this.left_tentacle_1_0 = this.tentacles.getChild("left_tentacle_1_0");
		this.left_tentacle_1_1 = this.left_tentacle_1_0.getChild("left_tentacle_1_1");
		this.left_tentacle_1_2 = this.left_tentacle_1_1.getChild("left_tentacle_1_2");
		this.left_tentacle_1_3 = this.left_tentacle_1_2.getChild("left_tentacle_1_3");
		this.left_tentacle_0_5 = this.left_tentacle_1_3.getChild("left_tentacle_0_5");
		this.right_tentacle_0_0 = this.tentacles.getChild("right_tentacle_0_0");
		this.right_tentacle_0_1 = this.right_tentacle_0_0.getChild("right_tentacle_0_1");
		this.right_tentacle_0_2 = this.right_tentacle_0_1.getChild("right_tentacle_0_2");
		this.right_tentacle_0_3 = this.right_tentacle_0_2.getChild("right_tentacle_0_3");
		this.right_tentacle_0_4 = this.right_tentacle_0_3.getChild("right_tentacle_0_4");
		this.right_tentacle_1_0 = this.tentacles.getChild("right_tentacle_1_0");
		this.right_tentacle_1_1 = this.right_tentacle_1_0.getChild("right_tentacle_1_1");
		this.right_tentacle_1_2 = this.right_tentacle_1_1.getChild("right_tentacle_1_2");
		this.right_tentacle_1_3 = this.right_tentacle_1_2.getChild("right_tentacle_1_3");
		this.right_tentacle_0_5 = this.right_tentacle_1_3.getChild("right_tentacle_0_5");
		this.body_2 = this.body.getChild("body_2");
		this.tail = this.body_2.getChild("tail");
		this.tail_0 = this.tail.getChild("tail_0");
		this.tail_1 = this.tail_0.getChild("tail_1");
		this.tail_2 = this.tail_1.getChild("tail_2");
		this.tail_3 = this.tail_2.getChild("tail_3");
		this.legs = this.ALL.getChild("legs");
		this.left_legs = this.legs.getChild("left_legs");
		this.left_leg_0_0 = this.left_legs.getChild("left_leg_0_0");
		this.left_leg_0_1 = this.left_leg_0_0.getChild("left_leg_0_1");
		this.left_leg_0_2 = this.left_leg_0_1.getChild("left_leg_0_2");
		this.left_leg_1_0 = this.left_legs.getChild("left_leg_1_0");
		this.left_leg_1_1 = this.left_leg_1_0.getChild("left_leg_1_1");
		this.left_leg_1_2 = this.left_leg_1_1.getChild("left_leg_1_2");
		this.left_leg_2_0 = this.left_legs.getChild("left_leg_2_0");
		this.left_leg_2_1 = this.left_leg_2_0.getChild("left_leg_2_1");
		this.left_leg_2_2 = this.left_leg_2_1.getChild("left_leg_2_2");
		this.left_leg_3_0 = this.left_legs.getChild("left_leg_3_0");
		this.left_leg_3_1 = this.left_leg_3_0.getChild("left_leg_3_1");
		this.left_leg_3_2 = this.left_leg_3_1.getChild("left_leg_3_2");
		this.right_legs = this.legs.getChild("right_legs");
		this.right_leg_0_0 = this.right_legs.getChild("right_leg_0_0");
		this.right_leg_0_1 = this.right_leg_0_0.getChild("right_leg_0_1");
		this.right_leg_0_2 = this.right_leg_0_1.getChild("right_leg_0_2");
		this.right_leg_1_0 = this.right_legs.getChild("right_leg_1_0");
		this.right_leg_1_1 = this.right_leg_1_0.getChild("right_leg_1_1");
		this.right_leg_1_2 = this.right_leg_1_1.getChild("right_leg_1_2");
		this.right_leg_2_0 = this.right_legs.getChild("right_leg_2_0");
		this.right_leg_2_1 = this.right_leg_2_0.getChild("right_leg_2_1");
		this.right_leg_2_2 = this.right_leg_2_1.getChild("right_leg_2_2");
		this.right_leg_3_0 = this.right_legs.getChild("right_leg_3_0");
		this.right_leg_3_1 = this.right_leg_3_0.getChild("right_leg_3_1");
		this.right_leg_3_2 = this.right_leg_3_1.getChild("right_leg_3_2");
		this.left_chelicer_3 = root.getChild("left_chelicer_3");
		this.right_chelicer_3 = root.getChild("right_chelicer_3");
		this.left_tentacle_0_7 = root.getChild("left_tentacle_0_7");
		this.left_tentacle_0_8 = root.getChild("left_tentacle_0_8");
		this.left_tentacle_0_9 = root.getChild("left_tentacle_0_9");
		this.left_tentacle_0_10 = root.getChild("left_tentacle_0_10");
		this.left_tentacle_1_5 = root.getChild("left_tentacle_1_5");
		this.left_tentacle_1_6 = root.getChild("left_tentacle_1_6");
		this.left_tentacle_1_7 = root.getChild("left_tentacle_1_7");
		this.left_tentacle_0_6 = root.getChild("left_tentacle_0_6");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 74.0F, 0.0F));

		PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, -95.0F, 0.0F));

		PartDefinition body_1 = body.addOrReplaceChild("body_1", CubeListBuilder.create().texOffs(119, 203).addBox(-20.0F, -24.0F, -9.0F, 40.0F, 25.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -29.0F, 0.0F));

		PartDefinition neck = body_1.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(119, 134).addBox(-9.0F, -24.0F, -11.0F, 18.0F, 27.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -23.0F, 5.0F, 0.3927F, 0.0F, 0.0F));

		PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -9.0F, -5.0F, 16.0F, 20.0F, 13.0F, new CubeDeformation(0.0F))
		.texOffs(58, 0).addBox(-8.0F, -9.0F, -7.0F, 16.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(82, 7).addBox(-3.0F, -4.0F, -7.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(58, 24).addBox(-1.0F, -4.0F, -7.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(70, 54).addBox(-8.0F, 2.0F, -7.0F, 9.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(80, 46).addBox(-7.0F, 0.0F, -7.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(80, 50).addBox(-5.0F, 1.0F, -7.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(60, 72).addBox(4.0F, 1.0F, -7.0F, 1.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(80, 65).addBox(3.0F, 2.0F, -7.0F, 1.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(80, 76).addBox(2.0F, 3.0F, -7.0F, 1.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(66, 24).addBox(1.0F, 3.0F, -7.0F, 1.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(36, 72).addBox(5.0F, 0.0F, -7.0F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(82, 10).addBox(1.0F, -4.0F, -7.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(72, 24).addBox(-8.0F, -4.0F, -7.0F, 1.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(30, 72).addBox(7.0F, -4.0F, -7.0F, 1.0F, 15.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -31.0F, -7.0F));

		PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(40, 33).addBox(4.0F, -9.0F, -6.0F, 9.0F, 10.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, 0.5F, -0.3265F, -0.4303F, -0.2708F));

		PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(40, 33).addBox(4.0F, -9.0F, -6.0F, 9.0F, 10.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 7.0F, 3.5F, 0.1282F, 0.2385F, 0.2567F));

		PartDefinition head_2 = head.addOrReplaceChild("head_2", CubeListBuilder.create(), PartPose.offsetAndRotation(-11.0F, 1.0F, 1.0F, 0.0F, -0.0873F, 0.0F));

		PartDefinition cube_r3 = head_2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(58, 7).addBox(-21.0F, -10.0F, -4.0F, 0.0F, 5.0F, 12.0F, new CubeDeformation(0.025F))
		.texOffs(40, 54).addBox(-21.0F, -5.0F, -4.0F, 3.0F, 6.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(0, 33).addBox(-18.0F, -13.0F, -4.0F, 8.0F, 14.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.0F, 3.0F, 0.0F, -0.0567F, -0.1719F, -0.1772F));

		PartDefinition left_chelicer = head.addOrReplaceChild("left_chelicer", CubeListBuilder.create().texOffs(20, 59).mirror().addBox(-2.5F, -1.0F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.5F, 5.0F, -8.5F, -0.7888F, 1.0875F, -0.9433F));

		PartDefinition left_chelicer_2 = left_chelicer.addOrReplaceChild("left_chelicer_2", CubeListBuilder.create().texOffs(0, 77).mirror().addBox(-2.5F, 0.0F, 0.0F, 5.0F, 13.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false)
		.texOffs(0, 72).mirror().addBox(0.0F, 0.0F, -2.5F, 0.0F, 13.0F, 5.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, -0.2618F, 0.0F, 0.5236F));

		PartDefinition right_chelicer = head.addOrReplaceChild("right_chelicer", CubeListBuilder.create().texOffs(20, 59).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, 5.0F, -8.5F, -0.7888F, -1.0875F, 0.9433F));

		PartDefinition right_chelicer_2 = right_chelicer.addOrReplaceChild("right_chelicer_2", CubeListBuilder.create().texOffs(0, 77).addBox(-2.5F, 0.0F, 0.0F, 5.0F, 13.0F, 0.0F, new CubeDeformation(0.01F))
		.texOffs(0, 72).addBox(0.0F, 0.0F, -2.5F, 0.0F, 13.0F, 5.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, -0.2618F, 0.0F, -0.5236F));

		PartDefinition left_ant_chelicer = head.addOrReplaceChild("left_ant_chelicer", CubeListBuilder.create().texOffs(44, 72).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 6.0F, -7.0F, -1.021F, 0.5302F, -1.0033F));

		PartDefinition left_ant_chelicer_2 = left_ant_chelicer.addOrReplaceChild("left_ant_chelicer_2", CubeListBuilder.create().texOffs(80, 24).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 8.0F, 0.0F, -0.4492F, -0.3903F, 0.9176F));

		PartDefinition right_ant_chelicer = head.addOrReplaceChild("right_ant_chelicer", CubeListBuilder.create().texOffs(44, 72).mirror().addBox(-1.0F, -1.0F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-8.0F, 6.0F, -7.0F, -1.021F, -0.5302F, 1.0033F));

		PartDefinition right_ant_chelicer_2 = right_ant_chelicer.addOrReplaceChild("right_ant_chelicer_2", CubeListBuilder.create().texOffs(80, 24).mirror().addBox(-0.5F, 0.0F, -1.0F, 1.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.5F, 8.0F, 0.0F, -0.4492F, 0.3903F, -0.9176F));

		PartDefinition left_ant_antenna = head.addOrReplaceChild("left_ant_antenna", CubeListBuilder.create().texOffs(73, 91).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, -9.0F, -6.0F, 0.8878F, -0.7286F, -0.1422F));

		PartDefinition left_ant_antenna_2 = left_ant_antenna.addOrReplaceChild("left_ant_antenna_2", CubeListBuilder.create().texOffs(58, 91).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(-0.33F)), PartPose.offsetAndRotation(0.0F, -15.0F, 0.0F, 1.3452F, 0.2555F, -0.0579F));

		PartDefinition left_ant_antenna_3 = left_ant_antenna_2.addOrReplaceChild("left_ant_antenna_3", CubeListBuilder.create().texOffs(44, 91).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -14.0F, 0.0F, 1.0494F, 0.3843F, -0.2121F));

		PartDefinition right_ant_antenna = head.addOrReplaceChild("right_ant_antenna", CubeListBuilder.create().texOffs(73, 91).mirror().addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.0F, -9.0F, -6.0F, 0.8878F, 0.7286F, 0.1422F));

		PartDefinition right_ant_antenna_2 = right_ant_antenna.addOrReplaceChild("right_ant_antenna_2", CubeListBuilder.create().texOffs(58, 91).mirror().addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(-0.33F)).mirror(false), PartPose.offsetAndRotation(0.0F, -15.0F, 0.0F, 1.3452F, -0.2555F, 0.0579F));

		PartDefinition right_ant_antenna_3 = right_ant_antenna_2.addOrReplaceChild("right_ant_antenna_3", CubeListBuilder.create().texOffs(44, 91).mirror().addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(-0.5F)).mirror(false), PartPose.offsetAndRotation(0.0F, -14.0F, 0.0F, 1.0494F, -0.3843F, 0.2121F));

		PartDefinition right_eye = head.addOrReplaceChild("right_eye", CubeListBuilder.create().texOffs(16, 106).addBox(-1.5F, -1.5F, -0.025F, 3.0F, 3.0F, 0.0F, new CubeDeformation(-0.9F)), PartPose.offset(-2.5F, -0.5F, -5.0F));

		PartDefinition left_eye = head.addOrReplaceChild("left_eye", CubeListBuilder.create().texOffs(16, 106).mirror().addBox(-1.5F, -1.5F, -0.025F, 3.0F, 3.0F, 0.0F, new CubeDeformation(-0.9F)).mirror(false), PartPose.offset(2.5F, -0.5F, -5.0F));

		PartDefinition ratings = body_1.addOrReplaceChild("ratings", CubeListBuilder.create(), PartPose.offset(0.0F, -11.0F, 0.0F));

		PartDefinition left_rating_0 = ratings.addOrReplaceChild("left_rating_0", CubeListBuilder.create().texOffs(204, 0).addBox(-21.0F, -2.0F, -25.0F, 2.0F, 4.0F, 24.0F, new CubeDeformation(0.0F))
		.texOffs(208, 67).addBox(-21.0F, -2.0F, -1.0F, 22.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(224, 131).addBox(-19.0F, -2.0F, -25.0F, 14.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -8.0F, 14.0F, 0.0F, 0.0F, 0.0873F));

		PartDefinition right_rating_0 = ratings.addOrReplaceChild("right_rating_0", CubeListBuilder.create().texOffs(204, 0).mirror().addBox(19.0F, -2.0F, -25.0F, 2.0F, 4.0F, 24.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(208, 67).mirror().addBox(-1.0F, -2.0F, -1.0F, 22.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(224, 131).mirror().addBox(5.0F, -2.0F, -25.0F, 14.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, -8.0F, 14.0F, 0.0F, 0.0F, -0.0873F));

		PartDefinition left_rating_1 = ratings.addOrReplaceChild("left_rating_1", CubeListBuilder.create().texOffs(204, 0).addBox(-24.0F, -2.0F, -25.0F, 2.0F, 4.0F, 24.0F, new CubeDeformation(0.0F))
		.texOffs(208, 67).addBox(-24.0F, -2.0F, -1.0F, 22.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(220, 110).addBox(-22.0F, -2.0F, -25.0F, 16.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -1.0F, 14.0F));

		PartDefinition right_rating_1 = ratings.addOrReplaceChild("right_rating_1", CubeListBuilder.create().texOffs(204, 0).mirror().addBox(22.0F, -2.0F, -25.0F, 2.0F, 4.0F, 24.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(208, 67).mirror().addBox(2.0F, -2.0F, -1.0F, 22.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(220, 110).mirror().addBox(6.0F, -2.0F, -25.0F, 16.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-2.0F, -1.0F, 14.0F));

		PartDefinition left_rating_2 = ratings.addOrReplaceChild("left_rating_2", CubeListBuilder.create().texOffs(204, 0).addBox(-21.0F, -2.0F, -25.0F, 2.0F, 4.0F, 24.0F, new CubeDeformation(0.0F))
		.texOffs(208, 67).addBox(-21.0F, -2.0F, -1.0F, 22.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(224, 131).addBox(-19.0F, -2.0F, -25.0F, 14.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 6.0F, 14.0F, 0.0F, 0.0F, -0.0873F));

		PartDefinition right_rating_2 = ratings.addOrReplaceChild("right_rating_2", CubeListBuilder.create().texOffs(204, 0).mirror().addBox(19.0F, -2.0F, -25.0F, 2.0F, 4.0F, 24.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(208, 67).mirror().addBox(-1.0F, -2.0F, -1.0F, 22.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(224, 131).mirror().addBox(5.0F, -2.0F, -25.0F, 14.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, 6.0F, 14.0F, 0.0F, 0.0F, 0.0873F));

		PartDefinition left_arm = body_1.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(20.0F, -14.0F, 1.0F));

		PartDefinition left_arm_0 = left_arm.addOrReplaceChild("left_arm_0", CubeListBuilder.create().texOffs(0, 269).addBox(0.0F, -7.0F, -6.0F, 40.0F, 13.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition left_arm_1 = left_arm.addOrReplaceChild("left_arm_1", CubeListBuilder.create(), PartPose.offset(40.0F, 0.0F, 0.0F));

		PartDefinition claw = left_arm_1.addOrReplaceChild("claw", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition claw_Up = claw.addOrReplaceChild("claw_Up", CubeListBuilder.create().texOffs(114, 267).addBox(0.0F, -3.0F, -6.0F, 25.0F, 8.0F, 13.0F, new CubeDeformation(-0.01F))
		.texOffs(200, 294).addBox(0.0F, 5.0F, -5.0F, 18.0F, 4.0F, 0.0F, new CubeDeformation(0.01F))
		.texOffs(200, 294).addBox(0.0F, 5.0F, 6.0F, 18.0F, 4.0F, 0.0F, new CubeDeformation(0.01F))
		.texOffs(0, 336).addBox(18.0F, 5.0F, -6.0F, 7.0F, 4.0F, 13.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, -4.0F, 0.0F));

		PartDefinition claw_Down = claw.addOrReplaceChild("claw_Down", CubeListBuilder.create().texOffs(90, 300).addBox(0.0F, -2.0F, -6.0F, 20.0F, 5.0F, 13.0F, new CubeDeformation(-0.02F))
		.texOffs(163, 298).addBox(0.0F, -6.0F, -5.0F, 13.0F, 4.0F, 0.0F, new CubeDeformation(0.01F))
		.texOffs(169, 322).addBox(8.0F, -10.0F, -5.0F, 12.0F, 4.0F, 0.0F, new CubeDeformation(0.01F))
		.texOffs(169, 322).addBox(8.0F, -10.0F, 6.0F, 12.0F, 4.0F, 0.0F, new CubeDeformation(0.01F))
		.texOffs(163, 298).addBox(0.0F, -6.0F, 6.0F, 13.0F, 4.0F, 0.0F, new CubeDeformation(0.01F))
		.texOffs(94, 330).addBox(13.0F, -6.0F, -6.0F, 7.0F, 4.0F, 13.0F, new CubeDeformation(-0.02F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition right_arm = body_1.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-20.0F, -16.0F, 1.0F));

		PartDefinition right_arm_0 = right_arm.addOrReplaceChild("right_arm_0", CubeListBuilder.create().texOffs(148, 395).addBox(-30.0F, -4.0F, -4.0F, 30.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition right_arm_1 = right_arm.addOrReplaceChild("right_arm_1", CubeListBuilder.create().texOffs(148, 377).addBox(-30.0F, -4.0F, -4.0F, 30.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(-30.0F, 0.0F, 0.0F));

		PartDefinition hand = right_arm_1.addOrReplaceChild("hand", CubeListBuilder.create().texOffs(117, 413).addBox(-8.0F, -4.0F, -4.0F, 9.0F, 9.0F, 9.0F, new CubeDeformation(-0.002F)), PartPose.offset(-31.0F, 0.0F, 0.0F));

		PartDefinition finger_0 = hand.addOrReplaceChild("finger_0", CubeListBuilder.create().texOffs(177, 351).addBox(-1.5F, -7.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(-0.001F)), PartPose.offsetAndRotation(-6.5F, -4.0F, -2.5F, 0.6109F, 0.0F, 0.0F));

		PartDefinition phalange_0 = finger_0.addOrReplaceChild("phalange_0", CubeListBuilder.create().texOffs(82, 406).addBox(-1.5F, -15.0F, -1.5F, 3.0F, 16.0F, 3.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, 0.0F, 0.0F, -1.5708F));

		PartDefinition finger_1 = hand.addOrReplaceChild("finger_1", CubeListBuilder.create().texOffs(177, 351).addBox(-1.5F, -7.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(-0.001F)), PartPose.offset(-6.5F, -4.0F, 0.5F));

		PartDefinition phalange_1 = finger_1.addOrReplaceChild("phalange_1", CubeListBuilder.create().texOffs(82, 406).addBox(-1.5F, -15.0F, -1.5F, 3.0F, 15.0F, 3.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, 0.0F, 0.0F, -1.5708F));

		PartDefinition finger_2 = hand.addOrReplaceChild("finger_2", CubeListBuilder.create().texOffs(177, 351).addBox(-1.5F, -7.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(-0.001F)), PartPose.offsetAndRotation(-6.5F, -4.0F, 3.5F, -0.6109F, 0.0F, 0.0F));

		PartDefinition phalange_2 = finger_2.addOrReplaceChild("phalange_2", CubeListBuilder.create().texOffs(82, 406).addBox(-1.5F, -15.0F, -1.5F, 3.0F, 15.0F, 3.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, 0.0F, 0.0F, -1.5708F));

		PartDefinition finger_3 = hand.addOrReplaceChild("finger_3", CubeListBuilder.create().texOffs(177, 351).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(-0.001F)), PartPose.offset(-6.5F, 5.0F, 0.5F));

		PartDefinition phalange_3 = finger_3.addOrReplaceChild("phalange_3", CubeListBuilder.create().texOffs(94, 406).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 15.0F, 3.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition tentacles = body_1.addOrReplaceChild("tentacles", CubeListBuilder.create(), PartPose.offset(0.0F, -13.0F, 13.0F));

		PartDefinition left_tentacle_0_0 = tentacles.addOrReplaceChild("left_tentacle_0_0", CubeListBuilder.create().texOffs(355, 178).addBox(-2.0F, -3.0F, -1.0F, 4.0F, 4.0F, 28.0F, new CubeDeformation(0.0F)), PartPose.offset(-14.0F, -4.0F, 1.0F));

		PartDefinition left_tentacle_0_1 = left_tentacle_0_0.addOrReplaceChild("left_tentacle_0_1", CubeListBuilder.create().texOffs(346, 220).addBox(-2.0F, -3.0F, -1.0F, 4.0F, 4.0F, 28.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 28.0F));

		PartDefinition left_tentacle_0_2 = left_tentacle_0_1.addOrReplaceChild("left_tentacle_0_2", CubeListBuilder.create().texOffs(346, 220).addBox(-2.0F, -3.0F, -1.0F, 4.0F, 4.0F, 28.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 28.0F));

		PartDefinition left_tentacle_0_3 = left_tentacle_0_2.addOrReplaceChild("left_tentacle_0_3", CubeListBuilder.create().texOffs(346, 220).addBox(-2.0F, -3.0F, -1.0F, 4.0F, 4.0F, 28.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 28.0F));

		PartDefinition left_tentacle_0_4 = left_tentacle_0_3.addOrReplaceChild("left_tentacle_0_4", CubeListBuilder.create().texOffs(358, 260).addBox(-1.5F, -6.5F, 0.0F, 3.0F, 12.0F, 19.0F, new CubeDeformation(0.0F))
		.texOffs(359, 119).addBox(1.5F, -5.5F, 0.5F, 3.0F, 0.0F, 19.0F, new CubeDeformation(0.01F))
		.texOffs(359, 119).addBox(1.5F, -2.5F, 0.5F, 3.0F, 0.0F, 19.0F, new CubeDeformation(0.01F))
		.texOffs(359, 119).addBox(1.5F, 0.5F, 0.5F, 3.0F, 0.0F, 19.0F, new CubeDeformation(0.01F))
		.texOffs(359, 119).addBox(1.5F, 3.5F, 0.5F, 3.0F, 0.0F, 19.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, -0.5F, 27.0F));

		PartDefinition left_tentacle_1_0 = tentacles.addOrReplaceChild("left_tentacle_1_0", CubeListBuilder.create().texOffs(355, 178).addBox(-2.0F, -3.0F, -1.0F, 4.0F, 4.0F, 28.0F, new CubeDeformation(0.0F)), PartPose.offset(-9.0F, 7.0F, 1.0F));

		PartDefinition left_tentacle_1_1 = left_tentacle_1_0.addOrReplaceChild("left_tentacle_1_1", CubeListBuilder.create().texOffs(346, 220).addBox(-2.0F, -3.0F, -1.0F, 4.0F, 4.0F, 28.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 28.0F));

		PartDefinition left_tentacle_1_2 = left_tentacle_1_1.addOrReplaceChild("left_tentacle_1_2", CubeListBuilder.create().texOffs(346, 220).addBox(-2.0F, -3.0F, -1.0F, 4.0F, 4.0F, 28.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 28.0F));

		PartDefinition left_tentacle_1_3 = left_tentacle_1_2.addOrReplaceChild("left_tentacle_1_3", CubeListBuilder.create().texOffs(346, 220).addBox(-2.0F, -3.0F, -1.0F, 4.0F, 4.0F, 28.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 28.0F));

		PartDefinition left_tentacle_0_5 = left_tentacle_1_3.addOrReplaceChild("left_tentacle_0_5", CubeListBuilder.create().texOffs(358, 260).addBox(-1.5F, -6.5F, 0.0F, 3.0F, 12.0F, 19.0F, new CubeDeformation(0.0F))
		.texOffs(359, 119).addBox(1.5F, -5.5F, 0.5F, 3.0F, 0.0F, 19.0F, new CubeDeformation(0.01F))
		.texOffs(359, 119).addBox(1.5F, -2.5F, 0.5F, 3.0F, 0.0F, 19.0F, new CubeDeformation(0.01F))
		.texOffs(359, 119).addBox(1.5F, 0.5F, 0.5F, 3.0F, 0.0F, 19.0F, new CubeDeformation(0.01F))
		.texOffs(359, 119).addBox(1.5F, 3.5F, 0.5F, 3.0F, 0.0F, 19.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, -0.5F, 27.0F));

		PartDefinition right_tentacle_0_0 = tentacles.addOrReplaceChild("right_tentacle_0_0", CubeListBuilder.create().texOffs(355, 178).mirror().addBox(-2.0F, -3.0F, -1.0F, 4.0F, 4.0F, 28.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(14.0F, -4.0F, 1.0F));

		PartDefinition right_tentacle_0_1 = right_tentacle_0_0.addOrReplaceChild("right_tentacle_0_1", CubeListBuilder.create().texOffs(346, 220).mirror().addBox(-2.0F, -3.0F, -1.0F, 4.0F, 4.0F, 28.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 28.0F));

		PartDefinition right_tentacle_0_2 = right_tentacle_0_1.addOrReplaceChild("right_tentacle_0_2", CubeListBuilder.create().texOffs(346, 220).mirror().addBox(-2.0F, -3.0F, -1.0F, 4.0F, 4.0F, 28.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 28.0F));

		PartDefinition right_tentacle_0_3 = right_tentacle_0_2.addOrReplaceChild("right_tentacle_0_3", CubeListBuilder.create().texOffs(346, 220).mirror().addBox(-2.0F, -3.0F, -1.0F, 4.0F, 4.0F, 28.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 28.0F));

		PartDefinition right_tentacle_0_4 = right_tentacle_0_3.addOrReplaceChild("right_tentacle_0_4", CubeListBuilder.create().texOffs(358, 260).mirror().addBox(-1.5F, -6.5F, 0.0F, 3.0F, 12.0F, 19.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(359, 119).mirror().addBox(-4.5F, -5.5F, 0.5F, 3.0F, 0.0F, 19.0F, new CubeDeformation(0.01F)).mirror(false)
		.texOffs(359, 119).mirror().addBox(-4.5F, -2.5F, 0.5F, 3.0F, 0.0F, 19.0F, new CubeDeformation(0.01F)).mirror(false)
		.texOffs(359, 119).mirror().addBox(-4.5F, 0.5F, 0.5F, 3.0F, 0.0F, 19.0F, new CubeDeformation(0.01F)).mirror(false)
		.texOffs(359, 119).mirror().addBox(-4.5F, 3.5F, 0.5F, 3.0F, 0.0F, 19.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(0.0F, -0.5F, 27.0F));

		PartDefinition right_tentacle_1_0 = tentacles.addOrReplaceChild("right_tentacle_1_0", CubeListBuilder.create().texOffs(355, 178).mirror().addBox(-2.0F, -3.0F, -1.0F, 4.0F, 4.0F, 28.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(9.0F, 7.0F, 1.0F));

		PartDefinition right_tentacle_1_1 = right_tentacle_1_0.addOrReplaceChild("right_tentacle_1_1", CubeListBuilder.create().texOffs(346, 220).mirror().addBox(-2.0F, -3.0F, -1.0F, 4.0F, 4.0F, 28.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 28.0F));

		PartDefinition right_tentacle_1_2 = right_tentacle_1_1.addOrReplaceChild("right_tentacle_1_2", CubeListBuilder.create().texOffs(346, 220).mirror().addBox(-2.0F, -3.0F, -1.0F, 4.0F, 4.0F, 28.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 28.0F));

		PartDefinition right_tentacle_1_3 = right_tentacle_1_2.addOrReplaceChild("right_tentacle_1_3", CubeListBuilder.create().texOffs(346, 220).mirror().addBox(-2.0F, -3.0F, -1.0F, 4.0F, 4.0F, 28.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 28.0F));

		PartDefinition right_tentacle_0_5 = right_tentacle_1_3.addOrReplaceChild("right_tentacle_0_5", CubeListBuilder.create().texOffs(358, 260).mirror().addBox(-1.5F, -6.5F, 0.0F, 3.0F, 12.0F, 19.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(359, 119).mirror().addBox(-4.5F, -5.5F, 0.5F, 3.0F, 0.0F, 19.0F, new CubeDeformation(0.01F)).mirror(false)
		.texOffs(359, 119).mirror().addBox(-4.5F, -2.5F, 0.5F, 3.0F, 0.0F, 19.0F, new CubeDeformation(0.01F)).mirror(false)
		.texOffs(359, 119).mirror().addBox(-4.5F, 0.5F, 0.5F, 3.0F, 0.0F, 19.0F, new CubeDeformation(0.01F)).mirror(false)
		.texOffs(359, 119).mirror().addBox(-4.5F, 3.5F, 0.5F, 3.0F, 0.0F, 19.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(0.0F, -0.5F, 27.0F));

		PartDefinition body_2 = body.addOrReplaceChild("body_2", CubeListBuilder.create().texOffs(96, 36).addBox(-15.0F, -28.0F, -9.0F, 30.0F, 35.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition tail = body_2.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 12.0F));

		PartDefinition tail_0 = tail.addOrReplaceChild("tail_0", CubeListBuilder.create().texOffs(414, 0).addBox(-10.0F, -10.0F, 1.0F, 20.0F, 16.0F, 29.0F, new CubeDeformation(0.0F))
		.texOffs(337, -22).addBox(7.0F, -13.0F, 1.0F, 0.0F, 3.0F, 29.0F, new CubeDeformation(0.01F))
		.texOffs(337, -22).mirror().addBox(-7.0F, -13.0F, 1.0F, 0.0F, 3.0F, 29.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition tail_1 = tail_0.addOrReplaceChild("tail_1", CubeListBuilder.create().texOffs(426, 60).addBox(-7.0F, -8.0F, 1.0F, 14.0F, 14.0F, 29.0F, new CubeDeformation(0.0F))
		.texOffs(337, -22).addBox(4.0F, -11.0F, 1.0F, 0.0F, 3.0F, 29.0F, new CubeDeformation(0.01F))
		.texOffs(337, -22).mirror().addBox(-4.0F, -11.0F, 1.0F, 0.0F, 3.0F, 29.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(0.0F, 0.0F, 29.0F));

		PartDefinition tail_2 = tail_1.addOrReplaceChild("tail_2", CubeListBuilder.create().texOffs(434, 137).addBox(-5.0F, -6.0F, 1.0F, 10.0F, 12.0F, 29.0F, new CubeDeformation(0.0F))
		.texOffs(337, -22).addBox(2.0F, -9.0F, 1.0F, 0.0F, 3.0F, 29.0F, new CubeDeformation(0.01F))
		.texOffs(337, -22).mirror().addBox(-2.0F, -9.0F, 1.0F, 0.0F, 3.0F, 29.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(0.0F, 0.0F, 29.0F));

		PartDefinition tail_3 = tail_2.addOrReplaceChild("tail_3", CubeListBuilder.create().texOffs(442, 277).addBox(-3.0F, -4.0F, 1.0F, 6.0F, 10.0F, 29.0F, new CubeDeformation(0.0F))
		.texOffs(337, -22).addBox(2.0F, -7.0F, 1.0F, 0.0F, 3.0F, 29.0F, new CubeDeformation(0.01F))
		.texOffs(337, -22).mirror().addBox(-2.0F, -7.0F, 1.0F, 0.0F, 3.0F, 29.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(0.0F, 0.0F, 29.0F));

		PartDefinition legs = ALL.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(0.0F, -94.0F, 2.0F));

		PartDefinition left_legs = legs.addOrReplaceChild("left_legs", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 1.0F));

		PartDefinition left_leg_0_0 = left_legs.addOrReplaceChild("left_leg_0_0", CubeListBuilder.create().texOffs(7, 246).addBox(7.0F, -3.0F, -2.4F, 42.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(0, 156).addBox(0.0F, -4.0F, -3.4F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(15.0F, 0.0F, -11.6F));

		PartDefinition left_leg_0_1 = left_leg_0_0.addOrReplaceChild("left_leg_0_1", CubeListBuilder.create().texOffs(7, 237).addBox(7.0F, -2.5F, -1.9F, 42.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 156).addBox(0.0F, -4.0F, -3.4F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(49.0F, 0.0F, 0.0F));

		PartDefinition left_leg_0_2 = left_leg_0_1.addOrReplaceChild("left_leg_0_2", CubeListBuilder.create().texOffs(5, 228).addBox(5.0F, -2.0F, -1.4F, 29.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 142).addBox(-0.5F, -3.5F, -2.9F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(49.0F, 0.0F, 0.0F));

		PartDefinition left_leg_1_0 = left_legs.addOrReplaceChild("left_leg_1_0", CubeListBuilder.create().texOffs(7, 246).addBox(7.0F, -3.0F, -2.4F, 42.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(0, 156).addBox(0.0F, -4.0F, -3.4F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(15.0F, 0.0F, -4.6F));

		PartDefinition left_leg_1_1 = left_leg_1_0.addOrReplaceChild("left_leg_1_1", CubeListBuilder.create().texOffs(7, 237).addBox(7.0F, -2.5F, -1.9F, 42.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 156).addBox(0.0F, -4.0F, -3.4F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(49.0F, 0.0F, 0.0F));

		PartDefinition left_leg_1_2 = left_leg_1_1.addOrReplaceChild("left_leg_1_2", CubeListBuilder.create().texOffs(5, 228).addBox(5.0F, -2.0F, -1.4F, 29.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 142).addBox(-0.5F, -3.5F, -2.9F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(49.0F, 0.0F, 0.0F));

		PartDefinition left_leg_2_0 = left_legs.addOrReplaceChild("left_leg_2_0", CubeListBuilder.create().texOffs(7, 246).addBox(7.0F, -3.0F, -2.4F, 42.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(0, 156).addBox(0.0F, -4.0F, -3.4F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(15.0F, 0.0F, 2.4F));

		PartDefinition left_leg_2_1 = left_leg_2_0.addOrReplaceChild("left_leg_2_1", CubeListBuilder.create().texOffs(7, 237).addBox(7.0F, -2.5F, -1.9F, 42.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 156).addBox(0.0F, -4.0F, -3.4F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(49.0F, 0.0F, 0.0F));

		PartDefinition left_leg_2_2 = left_leg_2_1.addOrReplaceChild("left_leg_2_2", CubeListBuilder.create().texOffs(5, 228).addBox(5.0F, -2.0F, -1.4F, 29.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 142).addBox(-0.5F, -3.5F, -2.9F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(49.0F, 0.0F, 0.0F));

		PartDefinition left_leg_3_0 = left_legs.addOrReplaceChild("left_leg_3_0", CubeListBuilder.create().texOffs(7, 246).addBox(7.0F, -3.0F, -2.4F, 42.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(0, 156).addBox(0.0F, -4.0F, -3.4F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(15.0F, 0.0F, 9.4F));

		PartDefinition left_leg_3_1 = left_leg_3_0.addOrReplaceChild("left_leg_3_1", CubeListBuilder.create().texOffs(7, 237).addBox(7.0F, -2.5F, -1.9F, 42.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 156).addBox(0.0F, -4.0F, -3.4F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(49.0F, 0.0F, 0.0F));

		PartDefinition left_leg_3_2 = left_leg_3_1.addOrReplaceChild("left_leg_3_2", CubeListBuilder.create().texOffs(5, 228).addBox(5.0F, -2.0F, -1.4F, 29.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 142).addBox(-0.5F, -3.5F, -2.9F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(49.0F, 0.0F, 0.0F));

		PartDefinition right_legs = legs.addOrReplaceChild("right_legs", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 1.0F));

		PartDefinition right_leg_0_0 = right_legs.addOrReplaceChild("right_leg_0_0", CubeListBuilder.create().texOffs(7, 246).mirror().addBox(-49.0F, -3.0F, -2.4F, 42.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 156).mirror().addBox(-7.0F, -4.0F, -3.4F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-15.0F, 0.0F, -11.6F));

		PartDefinition right_leg_0_1 = right_leg_0_0.addOrReplaceChild("right_leg_0_1", CubeListBuilder.create().texOffs(7, 237).mirror().addBox(-49.0F, -2.5F, -1.9F, 42.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 156).mirror().addBox(-7.0F, -4.0F, -3.4F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-49.0F, 0.0F, 0.0F));

		PartDefinition right_leg_0_2 = right_leg_0_1.addOrReplaceChild("right_leg_0_2", CubeListBuilder.create().texOffs(5, 228).mirror().addBox(-34.0F, -2.0F, -1.4F, 29.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 142).mirror().addBox(-5.5F, -3.5F, -2.9F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-49.0F, 0.0F, 0.0F));

		PartDefinition right_leg_1_0 = right_legs.addOrReplaceChild("right_leg_1_0", CubeListBuilder.create().texOffs(7, 246).mirror().addBox(-49.0F, -3.0F, -2.4F, 42.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 156).mirror().addBox(-7.0F, -4.0F, -3.4F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-15.0F, 0.0F, -4.6F));

		PartDefinition right_leg_1_1 = right_leg_1_0.addOrReplaceChild("right_leg_1_1", CubeListBuilder.create().texOffs(7, 237).mirror().addBox(-49.0F, -2.5F, -1.9F, 42.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 156).mirror().addBox(-7.0F, -4.0F, -3.4F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-49.0F, 0.0F, 0.0F));

		PartDefinition right_leg_1_2 = right_leg_1_1.addOrReplaceChild("right_leg_1_2", CubeListBuilder.create().texOffs(5, 228).mirror().addBox(-34.0F, -2.0F, -1.4F, 29.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 142).mirror().addBox(-5.5F, -3.5F, -2.9F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-49.0F, 0.0F, 0.0F));

		PartDefinition right_leg_2_0 = right_legs.addOrReplaceChild("right_leg_2_0", CubeListBuilder.create().texOffs(7, 246).mirror().addBox(-49.0F, -3.0F, -2.4F, 42.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 156).mirror().addBox(-7.0F, -4.0F, -3.4F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-15.0F, 0.0F, 2.4F));

		PartDefinition right_leg_2_1 = right_leg_2_0.addOrReplaceChild("right_leg_2_1", CubeListBuilder.create().texOffs(7, 237).mirror().addBox(-49.0F, -2.5F, -1.9F, 42.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 156).mirror().addBox(-7.0F, -4.0F, -3.4F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-49.0F, 0.0F, 0.0F));

		PartDefinition right_leg_2_2 = right_leg_2_1.addOrReplaceChild("right_leg_2_2", CubeListBuilder.create().texOffs(5, 228).mirror().addBox(-34.0F, -2.0F, -1.4F, 29.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 142).mirror().addBox(-5.5F, -3.5F, -2.9F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-49.0F, 0.0F, 0.0F));

		PartDefinition right_leg_3_0 = right_legs.addOrReplaceChild("right_leg_3_0", CubeListBuilder.create().texOffs(7, 246).mirror().addBox(-49.0F, -3.0F, -2.4F, 42.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 156).mirror().addBox(-7.0F, -4.0F, -3.4F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-15.0F, 0.0F, 9.4F));

		PartDefinition right_leg_3_1 = right_leg_3_0.addOrReplaceChild("right_leg_3_1", CubeListBuilder.create().texOffs(7, 237).mirror().addBox(-49.0F, -2.5F, -1.9F, 42.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 156).mirror().addBox(-7.0F, -4.0F, -3.4F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-49.0F, 0.0F, 0.0F));

		PartDefinition right_leg_3_2 = right_leg_3_1.addOrReplaceChild("right_leg_3_2", CubeListBuilder.create().texOffs(5, 228).mirror().addBox(-34.0F, -2.0F, -1.4F, 29.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 142).mirror().addBox(-5.5F, -3.5F, -2.9F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-49.0F, 0.0F, 0.0F));

		PartDefinition left_chelicer_3 = partdefinition.addOrReplaceChild("left_chelicer_3", CubeListBuilder.create(), PartPose.offsetAndRotation(3.5F, -127.0F, -8.5F, -0.2618F, 0.0F, 0.5236F));

		PartDefinition right_chelicer_3 = partdefinition.addOrReplaceChild("right_chelicer_3", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.5F, -127.0F, -8.5F, -0.2618F, 0.0F, -0.5236F));

		PartDefinition left_tentacle_0_7 = partdefinition.addOrReplaceChild("left_tentacle_0_7", CubeListBuilder.create(), PartPose.offsetAndRotation(-14.0F, -67.0F, 42.0F, 0.0F, 0.5236F, 0.0F));

		PartDefinition left_tentacle_0_8 = partdefinition.addOrReplaceChild("left_tentacle_0_8", CubeListBuilder.create(), PartPose.offsetAndRotation(-14.0F, -67.0F, 70.0F, 0.0F, 0.6545F, 0.0F));

		PartDefinition left_tentacle_0_9 = partdefinition.addOrReplaceChild("left_tentacle_0_9", CubeListBuilder.create(), PartPose.offsetAndRotation(-14.0F, -67.0F, 98.0F, 0.0F, 0.7418F, 0.0F));

		PartDefinition left_tentacle_0_10 = partdefinition.addOrReplaceChild("left_tentacle_0_10", CubeListBuilder.create(), PartPose.offsetAndRotation(-14.0F, -67.5F, 125.0F, 0.0F, 0.5672F, 0.0F));

		PartDefinition left_tentacle_1_5 = partdefinition.addOrReplaceChild("left_tentacle_1_5", CubeListBuilder.create(), PartPose.offsetAndRotation(-9.0F, -56.0F, 42.0F, 0.0F, 0.6981F, 0.0F));

		PartDefinition left_tentacle_1_6 = partdefinition.addOrReplaceChild("left_tentacle_1_6", CubeListBuilder.create(), PartPose.offsetAndRotation(-9.0F, -56.0F, 70.0F, 0.0F, 0.48F, 0.0F));

		PartDefinition left_tentacle_1_7 = partdefinition.addOrReplaceChild("left_tentacle_1_7", CubeListBuilder.create(), PartPose.offsetAndRotation(-9.0F, -56.0F, 98.0F, 0.0F, 0.9599F, 0.0F));

		PartDefinition left_tentacle_0_6 = partdefinition.addOrReplaceChild("left_tentacle_0_6", CubeListBuilder.create(), PartPose.offset(-9.0F, -56.5F, 125.0F));

		return LayerDefinition.create(meshdefinition, 512, 512);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		float moving = Math.min(limbSwingAmount * WALK_WEIGHT_FACTOR, 1f);
		if (moving > 0.001f) KeyframeAnimations.animate(this, TheBiologicalErrorAnimations.MOVE_WALK, (long) (limbSwing * 50f * WALK_ANIM_SPEED), moving, ANIM_VECTOR);
		if (moving < 0.999f) KeyframeAnimations.animate(this, TheBiologicalErrorAnimations.MISC_IDLE, (long) (ageInTicks * 50f), 1f - moving, ANIM_VECTOR);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		left_chelicer_3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		right_chelicer_3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		left_tentacle_0_7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		left_tentacle_0_8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		left_tentacle_0_9.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		left_tentacle_0_10.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		left_tentacle_1_5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		left_tentacle_1_6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		left_tentacle_1_7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		left_tentacle_0_6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return this.ALL2;
	}
}
