package net.tiew.operationWild.entity.client.model.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.misc.SeaBugShard0Entity;

public class SeaBugShard0Model<T extends SeaBugShard0Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "seabug_shard_0"), "main");

	private final ModelPart ALL;
	private final ModelPart main_cage;
	private final ModelPart wheel;

    public SeaBugShard0Model(ModelPart root) {
		this.ALL = root.getChild("ALL");
		this.main_cage = this.ALL.getChild("main_cage");
		this.wheel = this.main_cage.getChild("wheel");
	}

    public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition ALL = partdefinition.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 13.0F, 0.0F));

		PartDefinition main_cage = ALL.addOrReplaceChild("main_cage", CubeListBuilder.create().texOffs(0, 0).addBox(-11.0F, -5.0F, -19.0F, 22.0F, 16.0F, 37.0F, new CubeDeformation(0.0F))
				.texOffs(138, 0).addBox(-11.0F, -5.0F, -19.0F, 22.0F, 16.0F, 37.0F, new CubeDeformation(-0.1F))
				.texOffs(196, 88).addBox(-11.0F, -5.0F, -19.0F, 22.0F, 16.0F, 8.0F, new CubeDeformation(-0.2F))
				.texOffs(46, 195).addBox(-7.0F, -15.025F, 7.0F, 14.0F, 26.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(118, 223).addBox(-7.0F, 0.975F, -1.0F, 14.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition wheel = main_cage.addOrReplaceChild("wheel", CubeListBuilder.create().texOffs(217, 146).addBox(-1.5F, -1.5F, 0.175F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(216, 156).addBox(-4.5F, -4.5F, 2.0F, 9.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.5F, -10.2F));

		return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(SeaBugShard0Entity seabug, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
		this.ALL.yRot = seabug.getYRotShard();
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
