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

    // --- Animation de digestion (renflement parcourant la queue) ---
    private static final int   DIGEST_SEGMENTS    = 7;    // nb de segments (bodyIndex 0..6)
    private static final float DIGEST_BULGE_WIDTH = 1.1f; // largeur du renflement (en segments)
    // Atténuation par segment : le renflement perd 10 % à chaque segment plus loin
    // (segment 0 = 100 %, 1 = 90 %, 2 = 81 %, …), comme si la proie se réduisait.
    private static final float DIGEST_FALLOFF_PER_SEGMENT = 0.9f;

    // Calibrage selon les PV max de la proie (interpolation linéaire entre deux points) :
    //   1 PV  → circonférence ×1,5 sur 20 s
    //   50 PV → circonférence ×2,0 sur 40 s
    // Plus la proie est puissante (PV max), plus l'onde est lente ET ample.
    private static final float DIGEST_HP_MIN         = 1.0f;
    private static final float DIGEST_HP_MAX         = 50.0f;
    private static final float DIGEST_FACTOR_AT_MIN  = 1.5f;
    private static final float DIGEST_FACTOR_AT_MAX  = 2.0f;
    private static final float DIGEST_SECONDS_AT_MIN = 20.0f;
    private static final float DIGEST_SECONDS_AT_MAX = 40.0f;

    private final ModelPart segment;

    public BoaTailPartModel(ModelPart root) {
        this.segment = root.getChild("segment");
    }

    // --- LayerDefinitions : geometrie par segment (inchangee) ---

    public static LayerDefinition createBody0Layer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition segment = mesh.getRoot().addOrReplaceChild("segment",
                CubeListBuilder.create()
                        .texOffs(48, 0)
                        .addBox(-3.5F, -3.0F, 0.0F, 7.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0f, TAIL_Y, 0f));
        // Coraux/nageoires laterales : repris du BoaModel complet (cube_r3..r6 de body_0)
        // pour que la queue en jeu corresponde a la preview. Z decale de +1 car la boite
        // du segment demarre a z=0 (contre z=-1 dans BoaModel.body_0).
        segment.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(171, 219).addBox(-2.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(4.5F, -1.5F, 9.0F, 0.2533F, -0.7519F, -0.3622F));
        segment.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(171, 219).mirror().addBox(-8.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-4.5F, -1.5F, 9.0F, 0.2533F, 0.7519F, 0.3622F));
        segment.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(179, 239).mirror().addBox(-8.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-4.5F, -1.5F, 10.0F, 0.2533F, 0.7519F, 0.3622F));
        segment.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(179, 239).addBox(-2.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(4.5F, -1.5F, 10.0F, 0.2533F, -0.7519F, -0.3622F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    public static LayerDefinition createBody1Layer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition segment = mesh.getRoot().addOrReplaceChild("segment",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -4.0F, 0.0F, 8.0F, 7.0F, 16.0F, new CubeDeformation(0.0F))
                        .texOffs(135, 138)
                        .addBox(0.0F, -14.0F, 0.0F, 0.0F, 10.0F, 15.0F, new CubeDeformation(0.01F)),
                PartPose.offset(0f, TAIL_Y, 0f));
        // Corail lateral (cube_r7 de BoaModel.body_1). Z decale de +1 (boite a z=0 ici).
        segment.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(205, 180).addBox(5.0F, -11.0F, -1.0F, 11.0F, 11.0F, 0.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(-9.0F, 5.5F, 3.0F, -0.0128F, -0.4635F, -0.5778F));
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
        PartDefinition segment = mesh.getRoot().addOrReplaceChild("segment",
                CubeListBuilder.create()
                        .texOffs(0, 0).mirror()
                        .addBox(-4.0F, -4.0F, 0.0F, 8.0F, 7.0F, 16.0F, new CubeDeformation(0.0F)).mirror(false)
                        .texOffs(135, 138)
                        .addBox(0.0F, -10.0F, 0.0F, 0.0F, 6.0F, 15.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0f, TAIL_Y, 0f));
        // Coraux (cube_r8, cube_r9 de BoaModel.body_3). Pas de decalage Z : la boite de
        // body_3 demarre deja a z=0 dans BoaModel.
        segment.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(157, 199).addBox(6.0F, -11.0F, -1.0F, 10.0F, 11.0F, 0.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(-8.0F, -2.5F, 3.0F, -0.1117F, -0.0577F, 0.2342F));
        segment.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(88, 151).addBox(6.0F, -12.0F, -1.0F, 13.0F, 12.0F, 0.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(3.0F, 1.5F, -5.0F, 1.0653F, -1.3361F, -2.7575F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    public static LayerDefinition createTail1Layer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition segment = mesh.getRoot().addOrReplaceChild("segment",
                CubeListBuilder.create()
                        .texOffs(48, 0)
                        .addBox(-3.5F, -3.0F, 0.0F, 7.0F, 6.0F, 16.0F, new CubeDeformation(0.0F))
                        .texOffs(146, 78)
                        .addBox(0.0F, -7.0F, 3.0F, 0.0F, 4.0F, 9.0F, new CubeDeformation(0.001F)),
                PartPose.offset(0f, TAIL_Y, 0f));
        // Corail (cube_r10 de BoaModel.body_4). Pas de decalage Z (boite a z=0 des deux cotes).
        segment.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(205, 202).addBox(6.0F, -7.0F, -1.0F, 8.0F, 7.0F, 0.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(-3.0F, -5.5F, 1.0F, -0.5628F, -0.7972F, 0.7507F));
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

        applyDigestionBulge(entity, ageInTicks);
    }

    /**
     * Renflement de digestion : une "bosse" (la proie avalée) parcourt la queue de la tête
     * (segment 0) jusqu'au bout (segment 6). Chaque segment gonfle puis dégonfle au passage
     * de l'onde. On ne touche qu'à la circonférence (xScale / yScale) ; la longueur (zScale)
     * reste à 1 pour que les segments restent jointifs.
     */
    private void applyDigestionBulge(BoaTailPart entity, float ageInTicks) {
        long start = entity.getDigestionStartTick();
        if (start < 0) return;

        // Temps écoulé en ticks (avec partialTick pour la fluidité).
        float partialTick = Math.max(0f, Math.min(1f, ageInTicks - entity.tickCount));
        double elapsed = (entity.level().getGameTime() - start) + partialTick;
        if (elapsed < 0) return;

        // Calibrage selon les PV max de la proie. Le facteur d'interpolation t est clampé
        // sur [0, 1] → bornes MIN/MAX garanties sur l'amplitude ET la durée :
        //   proie ≤ 1 PV   → t=0 → ×1,35 sur 5 s   (minimum)
        //   proie ≥ 50 PV  → t=1 → ×1,70 sur 10 s  (maximum)
        float t = (entity.getDigestionPower() - DIGEST_HP_MIN) / (DIGEST_HP_MAX - DIGEST_HP_MIN);
        t = Math.max(0.0f, Math.min(1.0f, t));
        float peakBulge      = (DIGEST_FACTOR_AT_MIN + (DIGEST_FACTOR_AT_MAX - DIGEST_FACTOR_AT_MIN) * t) - 1.0f;
        double durationTicks = (DIGEST_SECONDS_AT_MIN + (DIGEST_SECONDS_AT_MAX - DIGEST_SECONDS_AT_MIN) * t) * 20.0;

        // Progression de l'onde sur toute l'animation [0..1].
        double p = elapsed / durationTicks;
        if (p > 1.0) return; // onde terminée

        // Atténuation : chaque segment plus loin perd 10 % de renflement (sur la part au-dessus
        // de 1). Ex. au max : segment 0 → 1 + 0,7 = ×1,70 ; segment 1 → 1 + 0,7·0,9 = ×1,63 ; etc.
        int   idx     = entity.getBodyIndex();
        float falloff = (float) Math.pow(DIGEST_FALLOFF_PER_SEGMENT, idx);

        // L'onde part juste avant la tête (segment 0) et sort juste après le bout (segment 6).
        double wavePos = p * (DIGEST_SEGMENTS + 2.0 * DIGEST_BULGE_WIDTH) - DIGEST_BULGE_WIDTH;
        double d = idx - wavePos;
        float bulge = (float) (peakBulge * falloff * Math.exp(-(d * d) / (DIGEST_BULGE_WIDTH * DIGEST_BULGE_WIDTH)));
        if (bulge <= 0.001f) return;

        this.segment.xScale = 1.0f + bulge;
        this.segment.yScale = 1.0f + bulge;
        // zScale inchangé : pas de trou ni de chevauchement entre segments.
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer,
                               int packedLight, int packedOverlay, int color) {
        this.segment.render(poseStack, consumer, packedLight, packedOverlay, color);
    }
}