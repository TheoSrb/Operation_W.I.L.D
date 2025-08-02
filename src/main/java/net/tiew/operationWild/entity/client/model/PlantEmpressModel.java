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
import net.tiew.operationWild.entity.client.animation.PlantEmpressAnimations;
import net.tiew.operationWild.entity.custom.living.boss.PlantEmpressEntity;

public class PlantEmpressModel<T extends PlantEmpressEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "plant_empress_default"), "main");

	private final ModelPart ALL;
	private final ModelPart body_1;
	private final ModelPart body_2;
	private final ModelPart body_3;
	private final ModelPart body_10;
	private final ModelPart head;
	private final ModelPart mouthUp;
	private final ModelPart mouthDown;
	private final ModelPart body_4;
	private final ModelPart body_5;
	private final ModelPart body_6;
	private final ModelPart head2;
	private final ModelPart mouthUp2;
	private final ModelPart mouthDown2;
	private final ModelPart body_7;
	private final ModelPart body_8;
	private final ModelPart body_9;
	private final ModelPart head3;
	private final ModelPart mouthUp3;
	private final ModelPart mouthDown3;
	private final ModelPart stem_10;
	private final ModelPart stem_11;
	private final ModelPart stem_12;
	private final ModelPart stem_4;
	private final ModelPart stem_5;
	private final ModelPart stem_6;
	private final ModelPart stem_2;
	private final ModelPart stem_3;
	private final ModelPart stem_13;
	private final ModelPart stem_14;
	private final ModelPart stem_15;
	private final ModelPart stem_16;
	private final ModelPart stem_17;
	private final ModelPart stem_18;
	private final ModelPart stem_19;
	private final ModelPart stem_7;
	private final ModelPart stem_8;
	private final ModelPart stem_9;

    public PlantEmpressModel(ModelPart root) {

		this.ALL = root.getChild("ALL");
		this.body_1 = this.ALL.getChild("body_1");
		this.body_2 = this.body_1.getChild("body_2");
		this.body_3 = this.body_2.getChild("body_3");
		this.body_10 = this.body_3.getChild("body_10");
		this.head = this.body_10.getChild("head");
		this.mouthUp = this.head.getChild("mouthUp");
		this.mouthDown = this.head.getChild("mouthDown");
		this.body_4 = this.ALL.getChild("body_4");
		this.body_5 = this.body_4.getChild("body_5");
		this.body_6 = this.body_5.getChild("body_6");
		this.head2 = this.body_6.getChild("head2");
		this.mouthUp2 = this.head2.getChild("mouthUp2");
		this.mouthDown2 = this.head2.getChild("mouthDown2");
		this.body_7 = this.ALL.getChild("body_7");
		this.body_8 = this.body_7.getChild("body_8");
		this.body_9 = this.body_8.getChild("body_9");
		this.head3 = this.body_9.getChild("head3");
		this.mouthUp3 = this.head3.getChild("mouthUp3");
		this.mouthDown3 = this.head3.getChild("mouthDown3");
		this.stem_10 = this.ALL.getChild("stem_10");
		this.stem_11 = this.stem_10.getChild("stem_11");
		this.stem_12 = this.stem_11.getChild("stem_12");
		this.stem_4 = this.ALL.getChild("stem_4");
		this.stem_5 = this.stem_4.getChild("stem_5");
		this.stem_6 = this.stem_5.getChild("stem_6");
		this.stem_2 = this.ALL.getChild("stem_2");
		this.stem_3 = this.stem_2.getChild("stem_3");
		this.stem_13 = this.stem_3.getChild("stem_13");
		this.stem_14 = this.ALL.getChild("stem_14");
		this.stem_15 = this.stem_14.getChild("stem_15");
		this.stem_16 = this.stem_15.getChild("stem_16");
		this.stem_17 = this.ALL.getChild("stem_17");
		this.stem_18 = this.stem_17.getChild("stem_18");
		this.stem_19 = this.stem_18.getChild("stem_19");
		this.stem_7 = this.ALL.getChild("stem_7");
		this.stem_8 = this.stem_7.getChild("stem_8");
		this.stem_9 = this.stem_8.getChild("stem_9");
    }

    public static LayerDefinition createBodyLayer() {

		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition ALL = partdefinition.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
		PartDefinition body_1 = ALL.addOrReplaceChild("body_1", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 0.0F));
		PartDefinition cube_r1 = body_1.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(228, 172).mirror().addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(0.0F, -16.0F, -1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r2 = body_1.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(228, 172).addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.0F, -16.0F, -1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition body_2 = body_1.addOrReplaceChild("body_2", CubeListBuilder.create(), PartPose.offset(0.0F, -32.0F, 0.0F));
		PartDefinition cube_r3 = body_2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(228, 224).mirror().addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(0.0F, -16.0F, -1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r4 = body_2.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(228, 224).addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.0F, -16.0F, -1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition body_3 = body_2.addOrReplaceChild("body_3", CubeListBuilder.create(), PartPose.offset(0.0F, -32.0F, 0.0F));
		PartDefinition cube_r5 = body_3.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(228, 224).mirror().addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(0.0F, -16.0F, -1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r6 = body_3.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(228, 224).addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.0F, -16.0F, -1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition body_10 = body_3.addOrReplaceChild("body_10", CubeListBuilder.create(), PartPose.offset(0.0F, -32.0F, 0.0F));
		PartDefinition cube_r7 = body_10.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(228, 224).mirror().addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(0.0F, -16.0F, -1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r8 = body_10.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(228, 224).addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.0F, -16.0F, -1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition head = body_10.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -30.0F, -2.0F));
		PartDefinition mouthUp = head.addOrReplaceChild("mouthUp", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, 0.0F));
		PartDefinition cube_r9 = mouthUp.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 245).mirror().addBox(-20.0F, -4.0F, -16.0F, 35.0F, 11.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(0.7F, 4.0F, -17.4F, 0.0F, 0.7854F, 0.0F));
		PartDefinition cube_r10 = mouthUp.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(0, 245).addBox(-15.0F, -4.0F, -16.0F, 35.0F, 11.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-0.7F, 4.0F, -17.4F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r11 = mouthUp.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(0, 0).addBox(-15.0F, -9.0F, -15.0F, 35.0F, 11.0F, 35.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -19.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition mouthDown = head.addOrReplaceChild("mouthDown", CubeListBuilder.create(), PartPose.offset(0.0F, 3.0F, -2.0F));
		PartDefinition cube_r12 = mouthDown.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(186, 0).mirror().addBox(-20.0F, -14.0F, -14.0F, 35.0F, 8.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(-0.5F, 3.0F, -17.5F, 0.0F, 0.7854F, 0.0F));
		PartDefinition cube_r13 = mouthDown.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(186, 0).addBox(-15.0F, -14.0F, -14.0F, 35.0F, 8.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(0.5F, 3.0F, -17.5F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r14 = mouthDown.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(0, 46).addBox(-15.0F, -6.0F, -15.0F, 35.0F, 7.0F, 35.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, -17.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition body_4 = ALL.addOrReplaceChild("body_4", CubeListBuilder.create(), PartPose.offset(-14.0F, -2.0F, 0.0F));
		PartDefinition cube_r15 = body_4.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(228, 172).mirror().addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(0.0F, -16.0F, -1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r16 = body_4.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(228, 172).addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.0F, -16.0F, -1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition body_5 = body_4.addOrReplaceChild("body_5", CubeListBuilder.create(), PartPose.offset(0.0F, -32.0F, 0.0F));
		PartDefinition cube_r17 = body_5.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(228, 224).mirror().addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(0.0F, -16.0F, -1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r18 = body_5.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(228, 224).addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.0F, -16.0F, -1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition body_6 = body_5.addOrReplaceChild("body_6", CubeListBuilder.create(), PartPose.offset(0.0F, -32.0F, 0.0F));
		PartDefinition cube_r19 = body_6.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(228, 224).mirror().addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(0.0F, -16.0F, -1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r20 = body_6.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(228, 224).addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.0F, -16.0F, -1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition head2 = body_6.addOrReplaceChild("head2", CubeListBuilder.create(), PartPose.offset(-1.0F, -30.0F, 2.0F));
		PartDefinition mouthUp2 = head2.addOrReplaceChild("mouthUp2", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, 0.0F));
		PartDefinition cube_r21 = mouthUp2.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(0, 245).mirror().addBox(-20.0F, -4.0F, -16.0F, 35.0F, 11.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(0.7F, 4.0F, -17.4F, 0.0F, 0.7854F, 0.0F));
		PartDefinition cube_r22 = mouthUp2.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(0, 245).addBox(-15.0F, -4.0F, -16.0F, 35.0F, 11.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-0.7F, 4.0F, -17.4F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r23 = mouthUp2.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(0, 0).addBox(-15.0F, -9.0F, -15.0F, 35.0F, 11.0F, 35.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -19.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition mouthDown2 = head2.addOrReplaceChild("mouthDown2", CubeListBuilder.create(), PartPose.offset(0.0F, 3.0F, -2.0F));
		PartDefinition cube_r24 = mouthDown2.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(186, 0).mirror().addBox(-20.0F, -14.0F, -14.0F, 35.0F, 8.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(-0.5F, 3.0F, -17.5F, 0.0F, 0.7854F, 0.0F));
		PartDefinition cube_r25 = mouthDown2.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(186, 0).addBox(-15.0F, -14.0F, -14.0F, 35.0F, 8.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(0.5F, 3.0F, -17.5F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r26 = mouthDown2.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(0, 46).addBox(-15.0F, -6.0F, -15.0F, 35.0F, 7.0F, 35.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, -17.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition body_7 = ALL.addOrReplaceChild("body_7", CubeListBuilder.create(), PartPose.offset(14.0F, -2.0F, 0.0F));
		PartDefinition cube_r27 = body_7.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(228, 172).addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(0.0F, -16.0F, -1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition cube_r28 = body_7.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(228, 172).mirror().addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(1.0F, -16.0F, -1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition body_8 = body_7.addOrReplaceChild("body_8", CubeListBuilder.create(), PartPose.offset(0.0F, -32.0F, 0.0F));
		PartDefinition cube_r29 = body_8.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(228, 224).addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(0.0F, -16.0F, -1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition cube_r30 = body_8.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(228, 224).mirror().addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(1.0F, -16.0F, -1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition body_9 = body_8.addOrReplaceChild("body_9", CubeListBuilder.create(), PartPose.offset(0.0F, -32.0F, 0.0F));
		PartDefinition cube_r31 = body_9.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(228, 224).addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(0.0F, -16.0F, -1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition cube_r32 = body_9.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(228, 224).mirror().addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(1.0F, -16.0F, -1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition head3 = body_9.addOrReplaceChild("head3", CubeListBuilder.create(), PartPose.offset(1.0F, -30.0F, 2.0F));
		PartDefinition mouthUp3 = head3.addOrReplaceChild("mouthUp3", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, 0.0F));
		PartDefinition cube_r33 = mouthUp3.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(0, 245).addBox(-15.0F, -4.0F, -16.0F, 35.0F, 11.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-0.7F, 4.0F, -17.4F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r34 = mouthUp3.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(0, 245).mirror().addBox(-20.0F, -4.0F, -16.0F, 35.0F, 11.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(0.7F, 4.0F, -17.4F, 0.0F, 0.7854F, 0.0F));
		PartDefinition cube_r35 = mouthUp3.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-20.0F, -9.0F, -15.0F, 35.0F, 11.0F, 35.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 2.0F, -19.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition mouthDown3 = head3.addOrReplaceChild("mouthDown3", CubeListBuilder.create(), PartPose.offset(0.0F, 3.0F, -2.0F));
		PartDefinition cube_r36 = mouthDown3.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(186, 0).addBox(-15.0F, -14.0F, -14.0F, 35.0F, 8.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(0.5F, 3.0F, -17.5F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r37 = mouthDown3.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(186, 0).mirror().addBox(-20.0F, -14.0F, -14.0F, 35.0F, 8.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(-0.5F, 3.0F, -17.5F, 0.0F, 0.7854F, 0.0F));
		PartDefinition cube_r38 = mouthDown3.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(0, 46).mirror().addBox(-20.0F, -6.0F, -15.0F, 35.0F, 7.0F, 35.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 3.0F, -17.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition stem_10 = ALL.addOrReplaceChild("stem_10", CubeListBuilder.create(), PartPose.offsetAndRotation(6.0F, 0.0F, 6.0F, -0.1745F, 0.0F, 0.1745F));
		PartDefinition cube_r39 = stem_10.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(228, 172).addBox(-7.0F, -14.0F, -1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.0F, -18.0F, 1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r40 = stem_10.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(228, 172).mirror().addBox(-7.0F, -14.0F, -1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(0.0F, -18.0F, 1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition stem_11 = stem_10.addOrReplaceChild("stem_11", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -32.0F, 0.0F, -0.2022F, 0.2716F, 0.4352F));
		PartDefinition cube_r41 = stem_11.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(228, 224).addBox(-7.0F, -14.0F, -1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.0F, -18.0F, 1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r42 = stem_11.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(228, 224).mirror().addBox(-7.0F, -14.0F, -1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(0.0F, -18.0F, 1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition stem_12 = stem_11.addOrReplaceChild("stem_12", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -32.0F, 0.0F, -0.195F, 0.1758F, 0.7245F));
		PartDefinition cube_r43 = stem_12.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(228, 224).addBox(-7.0F, -14.0F, -1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.0F, -18.0F, 1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r44 = stem_12.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(228, 224).mirror().addBox(-7.0F, -14.0F, -1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(0.0F, -18.0F, 1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition stem_4 = ALL.addOrReplaceChild("stem_4", CubeListBuilder.create(), PartPose.offsetAndRotation(-6.0F, 0.0F, -6.0F, 0.2228F, -0.3158F, -0.6363F));
		PartDefinition cube_r45 = stem_4.addOrReplaceChild("cube_r45", CubeListBuilder.create().texOffs(228, 172).mirror().addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(1.0F, -18.0F, -1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r46 = stem_4.addOrReplaceChild("cube_r46", CubeListBuilder.create().texOffs(228, 172).addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(0.0F, -18.0F, -1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition stem_5 = stem_4.addOrReplaceChild("stem_5", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -32.0F, 0.0F, 0.2022F, 0.2716F, -0.4352F));
		PartDefinition cube_r47 = stem_5.addOrReplaceChild("cube_r47", CubeListBuilder.create().texOffs(228, 224).mirror().addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(1.0F, -18.0F, -1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r48 = stem_5.addOrReplaceChild("cube_r48", CubeListBuilder.create().texOffs(228, 224).addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(0.0F, -18.0F, -1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition stem_6 = stem_5.addOrReplaceChild("stem_6", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -32.0F, 0.0F, 0.195F, 0.1758F, -0.7245F));
		PartDefinition cube_r49 = stem_6.addOrReplaceChild("cube_r49", CubeListBuilder.create().texOffs(228, 224).mirror().addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(1.0F, -18.0F, -1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r50 = stem_6.addOrReplaceChild("cube_r50", CubeListBuilder.create().texOffs(228, 224).addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(0.0F, -18.0F, -1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition stem_2 = ALL.addOrReplaceChild("stem_2", CubeListBuilder.create(), PartPose.offsetAndRotation(6.0F, 0.0F, -6.0F, 0.2228F, 0.3158F, 0.6363F));
		PartDefinition cube_r51 = stem_2.addOrReplaceChild("cube_r51", CubeListBuilder.create().texOffs(228, 172).addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.0F, -18.0F, -1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition cube_r52 = stem_2.addOrReplaceChild("cube_r52", CubeListBuilder.create().texOffs(228, 172).mirror().addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(0.0F, -18.0F, -1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition stem_3 = stem_2.addOrReplaceChild("stem_3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -32.0F, 0.0F, 0.2022F, -0.2716F, 0.4352F));
		PartDefinition cube_r53 = stem_3.addOrReplaceChild("cube_r53", CubeListBuilder.create().texOffs(228, 224).addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.0F, -18.0F, -1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition cube_r54 = stem_3.addOrReplaceChild("cube_r54", CubeListBuilder.create().texOffs(228, 224).mirror().addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(0.0F, -18.0F, -1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition stem_13 = stem_3.addOrReplaceChild("stem_13", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -32.0F, 0.0F, 0.195F, -0.1758F, 0.7245F));
		PartDefinition cube_r55 = stem_13.addOrReplaceChild("cube_r55", CubeListBuilder.create().texOffs(228, 224).addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.0F, -18.0F, -1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition cube_r56 = stem_13.addOrReplaceChild("cube_r56", CubeListBuilder.create().texOffs(228, 224).mirror().addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(0.0F, -18.0F, -1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition stem_14 = ALL.addOrReplaceChild("stem_14", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, -6.0F, 0.3491F, 0.0F, 0.0F));
		PartDefinition cube_r57 = stem_14.addOrReplaceChild("cube_r57", CubeListBuilder.create().texOffs(228, 172).addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.0F, -18.0F, -1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition cube_r58 = stem_14.addOrReplaceChild("cube_r58", CubeListBuilder.create().texOffs(228, 172).mirror().addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(0.0F, -18.0F, -1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition stem_15 = stem_14.addOrReplaceChild("stem_15", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -32.0F, 0.0F, 0.6545F, 0.0F, 0.0F));
		PartDefinition cube_r59 = stem_15.addOrReplaceChild("cube_r59", CubeListBuilder.create().texOffs(228, 224).addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.0F, -18.0F, -1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition cube_r60 = stem_15.addOrReplaceChild("cube_r60", CubeListBuilder.create().texOffs(228, 224).mirror().addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(0.0F, -18.0F, -1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition stem_16 = stem_15.addOrReplaceChild("stem_16", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -32.0F, 0.0F, 1.1337F, 0.4346F, 0.6548F));
		PartDefinition cube_r61 = stem_16.addOrReplaceChild("cube_r61", CubeListBuilder.create().texOffs(228, 224).addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.0F, -18.0F, -1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition cube_r62 = stem_16.addOrReplaceChild("cube_r62", CubeListBuilder.create().texOffs(228, 224).mirror().addBox(-7.0F, -14.0F, 1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(0.0F, -18.0F, -1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition stem_17 = ALL.addOrReplaceChild("stem_17", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 6.0F, -0.3491F, 0.0F, 0.0F));
		PartDefinition cube_r63 = stem_17.addOrReplaceChild("cube_r63", CubeListBuilder.create().texOffs(228, 172).addBox(-7.0F, -14.0F, -1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.0F, -18.0F, 1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r64 = stem_17.addOrReplaceChild("cube_r64", CubeListBuilder.create().texOffs(228, 172).mirror().addBox(-7.0F, -14.0F, -1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(0.0F, -18.0F, 1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition stem_18 = stem_17.addOrReplaceChild("stem_18", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -32.0F, 0.0F, -0.6545F, 0.0F, 0.0F));
		PartDefinition cube_r65 = stem_18.addOrReplaceChild("cube_r65", CubeListBuilder.create().texOffs(228, 224).addBox(-7.0F, -14.0F, -1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.0F, -18.0F, 1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r66 = stem_18.addOrReplaceChild("cube_r66", CubeListBuilder.create().texOffs(228, 224).mirror().addBox(-7.0F, -14.0F, -1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(0.0F, -18.0F, 1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition stem_19 = stem_18.addOrReplaceChild("stem_19", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -32.0F, 0.0F, -1.1337F, -0.4346F, 0.6548F));
		PartDefinition cube_r67 = stem_19.addOrReplaceChild("cube_r67", CubeListBuilder.create().texOffs(228, 224).addBox(-7.0F, -14.0F, -1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.0F, -18.0F, 1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r68 = stem_19.addOrReplaceChild("cube_r68", CubeListBuilder.create().texOffs(228, 224).mirror().addBox(-7.0F, -14.0F, -1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(0.0F, -18.0F, 1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition stem_7 = ALL.addOrReplaceChild("stem_7", CubeListBuilder.create(), PartPose.offsetAndRotation(-6.0F, 0.0F, 6.0F, -0.1745F, 0.0F, -0.1745F));
		PartDefinition cube_r69 = stem_7.addOrReplaceChild("cube_r69", CubeListBuilder.create().texOffs(228, 172).mirror().addBox(-7.0F, -14.0F, -1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(1.0F, -18.0F, 1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition cube_r70 = stem_7.addOrReplaceChild("cube_r70", CubeListBuilder.create().texOffs(228, 172).addBox(-7.0F, -14.0F, -1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(0.0F, -18.0F, 1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition stem_8 = stem_7.addOrReplaceChild("stem_8", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -32.0F, 0.0F, -0.2022F, -0.2716F, -0.4352F));
		PartDefinition cube_r71 = stem_8.addOrReplaceChild("cube_r71", CubeListBuilder.create().texOffs(228, 224).mirror().addBox(-7.0F, -14.0F, -1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(1.0F, -18.0F, 1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition cube_r72 = stem_8.addOrReplaceChild("cube_r72", CubeListBuilder.create().texOffs(228, 224).addBox(-7.0F, -14.0F, -1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(0.0F, -18.0F, 1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition stem_9 = stem_8.addOrReplaceChild("stem_9", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -32.0F, 0.0F, -0.195F, -0.1758F, -0.7245F));
		PartDefinition cube_r73 = stem_9.addOrReplaceChild("cube_r73", CubeListBuilder.create().texOffs(228, 224).mirror().addBox(-7.0F, -14.0F, -1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(1.0F, -18.0F, 1.0F, 0.0F, 0.7854F, 0.0F));
		PartDefinition cube_r74 = stem_9.addOrReplaceChild("cube_r74", CubeListBuilder.create().texOffs(228, 224).addBox(-7.0F, -14.0F, -1.0F, 14.0F, 32.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(0.0F, -18.0F, 1.0F, 0.0F, -0.7854F, 0.0F));
		return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(PlantEmpressEntity plant_empress, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.applyHeadRotation(netHeadYaw, headPitch);

		if ((plant_empress.isHead2Dead() && plant_empress.isHead3DeadLoop()) || (plant_empress.isHead3Dead() && plant_empress.isHead2DeadLoop())) {
			this.animate(plant_empress.idleAnimationState, PlantEmpressAnimations.MISC_IDLE_HEADS_DEATH, ageInTicks, 1.0f);
		}

		if (!plant_empress.isHead2Dead() && !plant_empress.isHead2DeadLoop() && !plant_empress.isHead3Dead() && !plant_empress.isHead3DeadLoop()) {
			this.animate(plant_empress.idleAnimationState, PlantEmpressAnimations.MISC_IDLE, ageInTicks, 1.0f);
		} else if (!plant_empress.isHead2Dead() && !plant_empress.isHead2DeadLoop()) {
			this.animate(plant_empress.idleAnimationState, PlantEmpressAnimations.MISC_IDLE_HEAD3_DEATH, ageInTicks, 1.0f);
		} else if (!plant_empress.isHead3Dead() && !plant_empress.isHead3DeadLoop()) {
			this.animate(plant_empress.idleAnimationState, PlantEmpressAnimations.MISC_IDLE_HEAD2_DEATH, ageInTicks, 1.0f);
		}


		if (plant_empress.isHead2DeadLoop() && plant_empress.isHead3DeadLoop()) {
			this.animate(plant_empress.head2DeathLoopAnimationState, PlantEmpressAnimations.HEADS_DEAD_LOOP, ageInTicks, 1.0f);
			this.animate(plant_empress.idleAnimationState, PlantEmpressAnimations.MISC_IDLE_HEADS_DEATH, ageInTicks, 1.0f);
		} else {
			if (plant_empress.isHead2Dead())
				this.animate(plant_empress.head2DeathAnimationState, PlantEmpressAnimations.HEAD_2_DEAD, ageInTicks, 1.0f);
			if (plant_empress.isHead2DeadLoop())
				this.animate(plant_empress.head2DeathLoopAnimationState, PlantEmpressAnimations.HEAD_2_DEAD_LOOP, ageInTicks, 1.0f);

			if (plant_empress.isHead3Dead())
				this.animate(plant_empress.head3DeathAnimationState, PlantEmpressAnimations.HEAD_3_DEAD, ageInTicks, 1.0f);
			if (plant_empress.isHead3DeadLoop())
				this.animate(plant_empress.head3DeathLoopAnimationState, PlantEmpressAnimations.HEAD_3_DEAD_LOOP, ageInTicks, 1.0f);
		}


		if (plant_empress.getHead1Life() <= 0) {
			this.head.xScale = 0;
			this.head.yScale = 0;
			this.head.zScale = 0;
		}

		this.head2.xScale *= 0.7f;
		this.head2.yScale *= 0.7f;
		this.head2.zScale *= 0.7f;

		this.head3.xScale *= 0.7f;
		this.head3.yScale *= 0.7f;
		this.head3.zScale *= 0.7f;
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
