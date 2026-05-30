package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.BoaTailPart;

/**
 * Modele unique d'un segment, sur le principe de ModelAnaconda (Alex's Mobs) :
 * UN seul modele qui change de geometrie selon le bodyIndex passe au constructeur.
 *
 * Le renderer cree 7 instances (une par index 0..6) et choisit laquelle utiliser
 * selon part.getBodyIndex(), exactement comme RenderAnacondaPart cree une instance
 * par type et choisit dans scale().
 *
 * Les geometries body_0..body_6 sont celles du bbmodel d'origine, inchangees.
 * Chaque cube va de z=0 a z=+16px DERRIERE le pivot.
 */
public class BoaTailPartModel extends EntityModel<BoaTailPart> {

    // 7 layers, une par segment (comme avant, le renderer les bake toutes).
    public static final ModelLayerLocation LAYER_BODY_0 = layer("boa_body_0");
    public static final ModelLayerLocation LAYER_BODY_1 = layer("boa_body_1");
    public static final ModelLayerLocation LAYER_BODY_2 = layer("boa_body_2");
    public static final ModelLayerLocation LAYER_BODY_3 = layer("boa_body_3");
    public static final ModelLayerLocation LAYER_TAIL1  = layer("boa_tail_1");
    public static final ModelLayerLocation LAYER_TAIL2  = layer("boa_tail_2");
    public static final ModelLayerLocation LAYER_TAIL3  = layer("boa_tail_3");

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(
                ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, name), "main");
    }

    private static final float TAIL_Y  = 20.0f;
    private static final float TAIL3_Y = 21.0f;

    private final ModelPart segment;

    public BoaTailPartModel(ModelPart root) {
        this.segment = root.getChild("segment");
    }

    // --- LayerDefinitions : geometrie par segment (inchangee) ---

    public static LayerDefinition createBody0Layer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("segment",
                CubeListBuilder.create()
                        .texOffs(48, 0)
                        .addBox(-3.5F, -3.0F, 0.0F, 7.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0f, TAIL_Y, 0f));
        return LayerDefinition.create(mesh, 256, 256);
    }

    public static LayerDefinition createBody1Layer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("segment",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -4.0F, 0.0F, 8.0F, 7.0F, 16.0F, new CubeDeformation(0.0F))
                        .texOffs(135, 138)
                        .addBox(0.0F, -14.0F, 0.0F, 0.0F, 10.0F, 15.0F, new CubeDeformation(0.01F)),
                PartPose.offset(0f, TAIL_Y, 0f));
        return LayerDefinition.create(mesh, 256, 256);
    }

    public static LayerDefinition createBody2Layer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("segment",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -4.0F, 0.0F, 8.0F, 7.0F, 16.0F, new CubeDeformation(-0.001F))
                        .texOffs(135, 138)
                        .addBox(0.0F, -10.0F, 0.0F, 0.0F, 6.0F, 15.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0f, TAIL_Y, 0f));
        return LayerDefinition.create(mesh, 256, 256);
    }

    public static LayerDefinition createBody3Layer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("segment",
                CubeListBuilder.create()
                        .texOffs(0, 0).mirror()
                        .addBox(-4.0F, -4.0F, 0.0F, 8.0F, 7.0F, 16.0F, new CubeDeformation(0.0F)).mirror(false)
                        .texOffs(135, 138)
                        .addBox(0.0F, -10.0F, 0.0F, 0.0F, 6.0F, 15.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0f, TAIL_Y, 0f));
        return LayerDefinition.create(mesh, 256, 256);
    }

    public static LayerDefinition createTail1Layer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("segment",
                CubeListBuilder.create()
                        .texOffs(48, 0)
                        .addBox(-3.5F, -3.0F, 0.0F, 7.0F, 6.0F, 16.0F, new CubeDeformation(0.0F))
                        .texOffs(146, 78)
                        .addBox(0.0F, -7.0F, 3.0F, 0.0F, 4.0F, 9.0F, new CubeDeformation(0.001F)),
                PartPose.offset(0f, TAIL_Y, 0f));
        return LayerDefinition.create(mesh, 256, 256);
    }

    public static LayerDefinition createTail2Layer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("segment",
                CubeListBuilder.create()
                        .texOffs(48, 0).mirror()
                        .addBox(-3.5F, -3.0F, 0.0F, 7.0F, 6.0F, 16.0F, new CubeDeformation(-0.001F)).mirror(false)
                        .texOffs(146, 78)
                        .addBox(0.0F, -7.0F, 3.0F, 0.0F, 4.0F, 9.0F, new CubeDeformation(0.001F)),
                PartPose.offset(0f, TAIL_Y, 0f));
        return LayerDefinition.create(mesh, 256, 256);
    }

    public static LayerDefinition createTail3Layer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("segment",
                CubeListBuilder.create()
                        .texOffs(48, 66)
                        .addBox(-2.5F, -3.0F, 0.0F, 5.0F, 5.0F, 16.0F, new CubeDeformation(0.0F))
                        .texOffs(135, 138)
                        .addBox(0.0F, -9.0F, 0.0F, 0.0F, 6.0F, 15.0F, new CubeDeformation(0.001F))
                        .texOffs(14, 171)
                        .addBox(-6.0F, 0.0F, 11.0F, 12.0F, 0.0F, 25.0F, new CubeDeformation(0.001F)),
                PartPose.offset(0f, TAIL3_Y, 0f));
        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(BoaTailPart entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Comme ModelAnaconda cote "body" : aucune animation locale, le segment est
        // pose tel quel. Toute la deformation de la chaine vient de la position/rotation
        // de l'entite (calculee serveur), appliquee par le renderer (setupRotations).
        this.segment.resetPose();
        this.segment.visible = true;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer,
                               int packedLight, int packedOverlay, int color) {
        this.segment.render(poseStack, consumer, packedLight, packedOverlay, color);
    }
}