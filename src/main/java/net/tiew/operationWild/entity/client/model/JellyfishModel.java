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
import net.tiew.operationWild.entity.client.animation.JellyfishAnimations;
import net.tiew.operationWild.entity.custom.living.JellyfishEntity;

public class JellyfishModel<T extends JellyfishEntity> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "jellyfish_default"), "main");

	private final ModelPart ALL2;
	private final ModelPart ALL;
	private final ModelPart tentacle_7;
	private final ModelPart tentacle_1;
	private final ModelPart tentacle_2;
	private final ModelPart tentacle_3;
	private final ModelPart tentacle_4;
	private final ModelPart tentacle_5;
	private final ModelPart tentacle_6;
	private final ModelPart tentacle_8;
	private final ModelPart body;

    public JellyfishModel(ModelPart root) {
		this.ALL2 = root.getChild("ALL2");
		this.ALL = this.ALL2.getChild("ALL");
		this.tentacle_7 = this.ALL.getChild("tentacle_7");
		this.tentacle_1 = this.ALL.getChild("tentacle_1");
		this.tentacle_2 = this.ALL.getChild("tentacle_2");
		this.tentacle_3 = this.ALL.getChild("tentacle_3");
		this.tentacle_4 = this.ALL.getChild("tentacle_4");
		this.tentacle_5 = this.ALL.getChild("tentacle_5");
		this.tentacle_6 = this.ALL.getChild("tentacle_6");
		this.tentacle_8 = this.ALL.getChild("tentacle_8");
		this.body = this.ALL.getChild("body");
    }

    public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition tentacle_7 = ALL.addOrReplaceChild("tentacle_7", CubeListBuilder.create().texOffs(51, 109).mirror().addBox(-1.0F, 0.0F, 0.0F, 2.0F, 19.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offset(-1.0F, 0.0F, 1.0F));

		PartDefinition cube_r1 = tentacle_7.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(51, 109).mirror().addBox(2.0F, -7.0F, 6.0F, 2.0F, 19.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(6.0F, 7.0F, -3.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition tentacle_1 = ALL.addOrReplaceChild("tentacle_1", CubeListBuilder.create(), PartPose.offset(-3.0F, 0.0F, -4.0F));

		PartDefinition cube_r2 = tentacle_1.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 109).addBox(-5.0F, -7.0F, -6.0F, 4.0F, 19.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-3.0F, 7.0F, -6.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r3 = tentacle_1.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 109).addBox(-5.0F, -7.0F, -6.0F, 4.0F, 19.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(6.0F, 7.0F, -3.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition tentacle_2 = ALL.addOrReplaceChild("tentacle_2", CubeListBuilder.create(), PartPose.offset(3.0F, 0.0F, -4.0F));

		PartDefinition cube_r4 = tentacle_2.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 109).mirror().addBox(1.0F, -7.0F, -6.0F, 4.0F, 19.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(3.0F, 7.0F, -6.0F, 0.0F, -3.1416F, 0.0F));

		PartDefinition cube_r5 = tentacle_2.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 109).mirror().addBox(1.0F, -7.0F, -6.0F, 4.0F, 19.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(-6.0F, 7.0F, -3.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition tentacle_3 = ALL.addOrReplaceChild("tentacle_3", CubeListBuilder.create(), PartPose.offset(3.0F, 0.0F, 4.0F));

		PartDefinition cube_r6 = tentacle_3.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 109).mirror().addBox(1.0F, -7.0F, 6.0F, 4.0F, 19.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(3.0F, 7.0F, 6.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r7 = tentacle_3.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 109).mirror().addBox(1.0F, -7.0F, 6.0F, 4.0F, 19.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(-6.0F, 7.0F, 3.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition tentacle_4 = ALL.addOrReplaceChild("tentacle_4", CubeListBuilder.create(), PartPose.offset(-3.0F, 0.0F, 4.0F));

		PartDefinition cube_r8 = tentacle_4.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, 109).addBox(-5.0F, -7.0F, 6.0F, 4.0F, 19.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-3.0F, 7.0F, 6.0F, 0.0F, -3.1416F, 0.0F));

		PartDefinition cube_r9 = tentacle_4.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 109).addBox(-5.0F, -7.0F, 6.0F, 4.0F, 19.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(6.0F, 7.0F, 3.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition tentacle_5 = ALL.addOrReplaceChild("tentacle_5", CubeListBuilder.create().texOffs(51, 109).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 19.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offset(1.0F, 0.0F, -1.0F));

		PartDefinition cube_r10 = tentacle_5.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(51, 109).addBox(-4.0F, -7.0F, -6.0F, 2.0F, 19.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-6.0F, 7.0F, 3.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition tentacle_6 = ALL.addOrReplaceChild("tentacle_6", CubeListBuilder.create().texOffs(51, 109).mirror().addBox(-1.0F, 0.0F, 0.0F, 2.0F, 19.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offset(-1.0F, 0.0F, -1.0F));

		PartDefinition cube_r11 = tentacle_6.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(51, 109).mirror().addBox(2.0F, -7.0F, -6.0F, 2.0F, 19.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false), PartPose.offsetAndRotation(6.0F, 7.0F, 3.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition tentacle_8 = ALL.addOrReplaceChild("tentacle_8", CubeListBuilder.create().texOffs(51, 109).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 19.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offset(2.0F, 0.0F, 1.0F));

		PartDefinition cube_r12 = tentacle_8.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(51, 109).addBox(-4.0F, -7.0F, 6.0F, 2.0F, 19.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-6.0F, 7.0F, -3.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -7.0F, -6.0F, 12.0F, 7.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(104, 0).addBox(-6.0F, 0.0F, -5.5F, 12.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(104, -12).addBox(-5.5F, 0.0F, -6.0F, 0.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(104, 0).addBox(-6.0F, 0.0F, 5.5F, 12.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(104, -12).addBox(5.5F, 0.0F, -6.5F, 0.0F, 3.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(JellyfishEntity jellyfish, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

		this.animate(jellyfish.idleAnimationState, JellyfishAnimations.MISC_IDLE, ageInTicks, 1.0f);
	}

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root() {
        return this.ALL2;
    }
}
