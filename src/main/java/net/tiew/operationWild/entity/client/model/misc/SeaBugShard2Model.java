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
import net.tiew.operationWild.entity.misc.SeaBugShard2Entity;

public class SeaBugShard2Model<T extends SeaBugShard2Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "seabug_shard_2"), "main");

	private final ModelPart ALL;
	private final ModelPart main_cage;

    public SeaBugShard2Model(ModelPart root) {
		this.ALL = root.getChild("ALL");
		this.main_cage = this.ALL.getChild("main_cage");
    }

    public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition ALL = partdefinition.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 29.0F, -1.0F));

		PartDefinition main_cage = ALL.addOrReplaceChild("main_cage", CubeListBuilder.create().texOffs(0, 106).addBox(-6.0F, -20.0F, 11.0F, 12.0F, 15.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(80, 56).addBox(-6.0F, -22.0F, -3.0F, 12.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(0, 56).addBox(-9.0F, -20.0F, -11.0F, 18.0F, 15.0F, 22.0F, new CubeDeformation(-0.001F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(SeaBugShard2Entity seabug, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
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
