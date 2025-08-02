package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.animation.SeaBugAnimations;
import net.tiew.operationWild.entity.custom.vehicle.SeaBugEntity;

public class SeaBugModel<T extends SeaBugEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "seabug_default"), "main");

	private final ModelPart ALL;
	private final ModelPart main_cage;
	private final ModelPart wheel;
	private final ModelPart front_screw;
	private final ModelPart back_screw;

    public SeaBugModel(ModelPart root) {

		this.ALL = root.getChild("ALL");
		this.main_cage = this.ALL.getChild("main_cage");
		this.wheel = this.main_cage.getChild("wheel");
		this.front_screw = this.ALL.getChild("front_screw");
		this.back_screw = this.ALL.getChild("back_screw");
    }

    public static LayerDefinition createBodyLayer() {

		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition ALL = partdefinition.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 13.0F, -3.0F));
		PartDefinition main_cage = ALL.addOrReplaceChild("main_cage", CubeListBuilder.create().texOffs(0, 0).addBox(-11.0F, -5.0F, -19.0F, 22.0F, 16.0F, 37.0F, new CubeDeformation(0.0F))
		.texOffs(138, 0).addBox(-11.0F, -5.0F, -19.0F, 22.0F, 16.0F, 37.0F, new CubeDeformation(-0.1F))
		.texOffs(196, 88).addBox(-11.0F, -5.0F, -19.0F, 22.0F, 16.0F, 8.0F, new CubeDeformation(-0.2F))
		.texOffs(46, 195).addBox(-7.0F, -15.025F, 7.0F, 14.0F, 26.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(118, 223).addBox(-7.0F, 0.975F, -1.0F, 14.0F, 10.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(52, 106).addBox(-3.0F, 1.0F, 18.025F, 6.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(0, 106).addBox(-6.0F, -20.0F, 11.0F, 12.0F, 15.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(80, 56).addBox(-6.0F, -22.0F, -3.0F, 12.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
		.texOffs(38, 93).addBox(11.025F, 2.0F, -8.0F, 8.0F, 2.0F, 11.0F, new CubeDeformation(0.0F))
		.texOffs(38, 93).mirror().addBox(-19.025F, 2.0F, -8.0F, 8.0F, 2.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(33, 122).addBox(-2.0F, 1.0F, -25.025F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 56).addBox(-9.0F, -20.0F, -11.0F, 18.0F, 15.0F, 22.0F, new CubeDeformation(-0.001F)), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition wheel = main_cage.addOrReplaceChild("wheel", CubeListBuilder.create().texOffs(217, 146).addBox(-1.5F, -1.5F, 0.175F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(216, 156).addBox(-4.5F, -4.5F, 2.0F, 9.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.5F, -10.2F));
		PartDefinition front_screw = ALL.addOrReplaceChild("front_screw", CubeListBuilder.create().texOffs(86, 131).addBox(-7.0F, -7.0F, 0.0F, 14.0F, 14.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, -22.0F));
		PartDefinition back_screw = ALL.addOrReplaceChild("back_screw", CubeListBuilder.create().texOffs(93, 96).addBox(-12.0F, -12.0F, -2.0F, 24.0F, 24.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 24.0F));
		return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(SeaBugEntity seabug, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
		this.animateWalk(SeaBugAnimations.MOVE_RUN, limbSwing, limbSwingAmount, 1, 2f);
	}

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.ALL.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root() {
        return this.ALL;
    }
}
