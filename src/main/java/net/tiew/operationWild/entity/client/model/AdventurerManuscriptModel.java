
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
import net.tiew.operationWild.entity.animals.aquatic.JellyfishEntity;
import net.tiew.operationWild.entity.client.animation.AdventurerManuscriptAnimations;
import net.tiew.operationWild.entity.client.animation.JellyfishAnimations;
import net.tiew.operationWild.entity.misc.AdventurerManuscript;

public class AdventurerManuscriptModel<T extends AdventurerManuscript> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "adventurer_manuscript"), "main");

	public final ModelPart ALL;
	public final ModelPart root;
	public final ModelPart right;
	public final ModelPart page;
	public final ModelPart left;
	public final ModelPart page2;

    public AdventurerManuscriptModel(ModelPart root) {
		this.ALL = root.getChild("ALL");
		this.root = this.ALL.getChild("root");
		this.right = this.root.getChild("right");
		this.page = this.right.getChild("page");
		this.left = this.root.getChild("left");
		this.page2 = this.left.getChild("page2");
    }

    public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition ALL = partdefinition.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 11.0F, 0.0F));

		PartDefinition root = ALL.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition right = root.addOrReplaceChild("right", CubeListBuilder.create().texOffs(0, 39).mirror().addBox(0.3F, -11.0F, 1.0F, 18.0F, 23.0F, 2.0F, new CubeDeformation(0.3F)).mirror(false)
				.texOffs(0, 0).mirror().addBox(-1.1F, -11.0F, -0.8F, 19.0F, 23.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false)
				.texOffs(72, 34).mirror().addBox(-0.1F, -11.0F, -0.95F, 17.0F, 22.0F, 2.0F, new CubeDeformation(-0.25F)).mirror(false), PartPose.offset(0.0F, 1.0F, 0.0F));

		PartDefinition page = right.addOrReplaceChild("page", CubeListBuilder.create().texOffs(52, 105).addBox(-0.2F, -11.0F, -1.025F, 18.0F, 23.0F, 0.0F, new CubeDeformation(0.05F))
				.texOffs(0, 0).mirror().addBox(-1.1F, -11.0F, -1.0F, 19.0F, 23.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition left = root.addOrReplaceChild("left", CubeListBuilder.create().texOffs(0, 69).addBox(-18.3F, -11.0F, 1.0F, 18.0F, 23.0F, 2.0F, new CubeDeformation(0.3F))
				.texOffs(0, 0).addBox(-17.9F, -11.0F, -0.8F, 19.0F, 23.0F, 0.0F, new CubeDeformation(0.05F))
				.texOffs(72, 34).addBox(-16.9F, -11.0F, -0.95F, 17.0F, 22.0F, 2.0F, new CubeDeformation(-0.25F)), PartPose.offset(0.0F, 1.0F, 0.0F));

		PartDefinition page2 = left.addOrReplaceChild("page2", CubeListBuilder.create().texOffs(0, 0).addBox(-17.9F, -11.0F, -1.0F, 19.0F, 23.0F, 0.0F, new CubeDeformation(0.05F))
				.texOffs(92, 105).addBox(-18.4F, -11.0F, -1.025F, 18.0F, 23.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(AdventurerManuscript book, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

		this.animate(book.openAnimationState, AdventurerManuscriptAnimations.OPEN, ageInTicks, 1f);
		this.animate(book.nextPageAnimationState, AdventurerManuscriptAnimations.NEXT_PAGE, ageInTicks, 0.75f);
		this.animate(book.precedentPageAnimationState, AdventurerManuscriptAnimations.PRECEDENT_PAGE, ageInTicks, 0.75f);
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
