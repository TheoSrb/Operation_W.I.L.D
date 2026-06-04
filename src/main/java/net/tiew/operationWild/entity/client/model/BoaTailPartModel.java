package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaTailPart;
import net.tiew.operationWild.entity.client.animation.BoaAnimations;
import org.joml.Vector3f;

/**
 * Modele unique d'un segment, sur le principe de ModelAnaconda (Alex's Mobs) :
 * UN seul modele qui change de geometrie selon le bodyIndex passe au constructeur.
 *
 * Le renderer cree 7 instances (une par index 0..6) et choisit laquelle utiliser
 * selon part.getBodyIndex(), exactement comme RenderAnacondaPart cree une instance
 * par type et choisit dans scale().
 *
 * HIERARCHIE : chaque segment est structure root -> "ALL" -> "body_{index}" pour
 * COLLER A CELLE DU MODELE DE TETE (BoaModel). Ainsi on peut rejouer DIRECTEMENT les
 * memes animations de strike (BoaAnimations.ATTACK_STRIKE*) sur la queue : seuls les
 * canaux "ALL" et "body_{index}" correspondant a ce segment s'appliquent (les autres
 * bones de l'anim — head, body_1 pour le segment 0, etc. — n'existent pas dans ce
 * modele et sont ignores par KeyframeAnimations). La tete et la queue jouent donc la
 * meme animation, bone par bone.
 */
public class BoaTailPartModel extends HierarchicalModel<BoaTailPart> {

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

    private final ModelPart root;
    private final ModelPart all;
    private final ModelPart segment;

    /**
     * @param root     le ModelPart racine bake (contient "ALL" -> "body_{index}")
     * @param boneName nom du bone du segment ("body_0".."body_6"), choisi par le renderer
     *                 selon l'index. Il doit matcher le nom utilise dans la LayerDefinition
     *                 ET dans les canaux des animations de strike.
     */
    public BoaTailPartModel(ModelPart root, String boneName) {
        this.root = root;
        this.all = root.getChild("ALL");
        this.segment = this.all.getChild(boneName);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    // --- LayerDefinitions : geometrie par segment (inchangee, juste re-parentee sous ALL) ---
    //
    // Chaque layer construit root -> "ALL" (vide, porte le canal d'anim ALL = levee globale)
    // -> "body_{index}" (la geometrie du segment). Le nom du segment matche le canal "body_N"
    // de l'animation de strike, et est aussi celui passe au constructeur par le renderer.

    private static PartDefinition all(MeshDefinition mesh) {
        return mesh.getRoot().addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.ZERO);
    }

    public static LayerDefinition createBody0Layer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition segment = all(mesh).addOrReplaceChild("body_0",
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
        // Nageoires laterales verticales (cube_r7/r8 de BoaModel.body_0). Z decale de +1 (-3.0 -> -2.0).
        segment.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(205, 49).mirror().addBox(0.1604F, -3.6831F, -0.1747F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-4.5F, 0.0F, -2.0F, 0.1747F, -0.043F, -0.0076F));
        segment.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(205, 49).addBox(-0.1604F, -3.6831F, -0.1747F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(4.5F, 0.0F, -2.0F, 0.1747F, 0.043F, 0.0076F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    public static LayerDefinition createBody1Layer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition segment = all(mesh).addOrReplaceChild("body_1",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -4.0F, 0.0F, 8.0F, 7.0F, 16.0F, new CubeDeformation(0.0F))
                        .texOffs(202, 7)
                        .addBox(-4.0F, -4.0F, 0.0F, 8.0F, 7.0F, 16.0F, new CubeDeformation(0.5F))
                        .texOffs(135, 138)
                        .addBox(0.0F, -14.0F, 0.0F, 0.0F, 10.0F, 15.0F, new CubeDeformation(0.01F)),
                PartPose.offset(0f, TAIL_Y, 0f));
        // Corail lateral (cube_r7 de BoaModel.body_1). Z decale de +1 (boite a z=0 ici).
        segment.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(205, 180).addBox(5.0F, -11.0F, -1.0F, 11.0F, 11.0F, 0.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(-9.0F, 5.5F, 3.0F, -0.0128F, -0.4635F, -0.5778F));
        // Nageoires laterales (cube_r10/r11 de BoaModel.body_1). Z decale de +1 (-3.7 -> -2.7).
        segment.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(212, 82).mirror().addBox(0.0F, -2.4362F, -0.3504F, 0.0F, 7.0F, 16.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-5.1F, -4.0F, -2.7F, 0.641F, 0.2116F, 0.4646F));
        segment.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(212, 82).addBox(0.0F, -2.4362F, -0.3504F, 0.0F, 7.0F, 16.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(5.1F, -4.0F, -2.7F, 0.641F, -0.2116F, -0.4646F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    public static LayerDefinition createBody2Layer() {
        MeshDefinition mesh = new MeshDefinition();
        all(mesh).addOrReplaceChild("body_2",
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
        PartDefinition segment = all(mesh).addOrReplaceChild("body_3",
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
        PartDefinition segment = all(mesh).addOrReplaceChild("body_4",
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
        all(mesh).addOrReplaceChild("body_5",
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
        all(mesh).addOrReplaceChild("body_6",
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
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.segment.visible = true;

        applyDigestionBulge(entity, ageInTicks);

        if (entity.isBoaConstricting()) {
            this.animate(entity.constrictBreathAnimationState, BoaAnimations.CONSTRICT_BREATH, ageInTicks, 1.0f);
        }

        // Animation de strike, bone par bone, EXACTEMENT comme la tete (BoaModel) : on
        // rejoue la meme AnimationDefinition que la tete ; seuls les canaux "ALL" et
        // "body_{index}" presents dans CE segment s'appliquent. La levee ALL + la rotation
        // du bone de ce segment se calquent donc sur la tete.
        AnimationDefinition strike = strikeFor(entity.getComboNumber());
        if (strike != null) {
            this.animate(entity.comboAnimationState, strike, ageInTicks, OWEntity.comboSpeedMultiplier);
            // Translation cumulee des ancetres : voir GhostChain. Rejoue le meme strike,
            // au MEME instant (accumulatedTime du state, deja avance par this.animate juste
            // au-dessus), sur un squelette de bones vides, et ajoute a CE segment la somme des
            // positions de "head" + "body_0".."body_{index-1}" qu'il aurait heritees dans la
            // hierarchie imbriquee de la tete.
            addAncestorTranslation(entity.getBodyIndex(), strike,
                    entity.comboAnimationState.getAccumulatedTime());
        }

        // Le 2e segment (bodyIndex 1) est le siege du rider : c'est lui qui capture sa pose
        // animee. BoaEntity.positionRider relit ce delta pour faire suivre le rider et la
        // camera au cabrage du combo. Chaine root -> ALL -> body_1, Y de repos = TAIL_Y.
        if (entity.getBodyIndex() == 1) {
            captureBodyState(entity, TAIL_Y, this.all, this.segment);
        }
    }

    // -------------------------------------------------------------------------
    //  COMPENSATION DE TRANSLATION DES ANCETRES (strike)
    // -------------------------------------------------------------------------
    //
    // Dans le modele de la TETE (BoaModel) les bones sont IMBRIQUES :
    //   head -> body_0 -> body_1 -> ... -> body_6
    // Une position posee sur "head" (la grande fente vers l'avant du strike) se propage
    // donc a body_0, body_1, ... Dans la queue, chaque segment est un modele PLAT
    // (ALL -> body_{index}) : il ne recoit QUE le canal position de SON bone, jamais celui
    // de ses ancetres. Resultat : body_0 et body_1 jouaient leur rotation mais pas la
    // translation vers l'avant heritee de "head".
    //
    // On recree donc ici un squelette "fantome" (bones vides, offsets zero), on y rejoue le
    // MEME strike au MEME instant, et on SOMME les positions des ancetres du segment pour les
    // ajouter a sa pose. On ne prend QUE la position (.x/.y/.z) : la rotation locale du
    // segment, deja correcte, n'est pas touchee.

    private static final String[] CHAIN =
            {"head", "body_0", "body_1", "body_2", "body_3", "body_4", "body_5", "body_6"};

    private static final GhostChain GHOST = new GhostChain();
    private static final Vector3f GHOST_VEC = new Vector3f();

    private static final class GhostChain extends HierarchicalModel<BoaTailPart> {
        private final ModelPart root;
        private final ModelPart[] bones = new ModelPart[CHAIN.length];

        GhostChain() {
            MeshDefinition mesh = new MeshDefinition();
            for (String name : CHAIN) {
                mesh.getRoot().addOrReplaceChild(name, CubeListBuilder.create(), PartPose.ZERO);
            }
            this.root = mesh.getRoot().bake(16, 16);
            for (int i = 0; i < CHAIN.length; i++) this.bones[i] = this.root.getChild(CHAIN[i]);
        }

        @Override public ModelPart root() { return this.root; }
        @Override public void setupAnim(BoaTailPart e, float a, float b, float c, float d, float f) {}
        @Override public void renderToBuffer(PoseStack ps, VertexConsumer vc, int l, int o, int col) {}
    }

    /**
     * Ajoute a CE segment la translation que ses ancetres (head + body_0..body_{index-1})
     * lui auraient transmise dans la hierarchie imbriquee de la tete. Position uniquement.
     */
    private void addAncestorTranslation(int index, AnimationDefinition strike, long accumulatedTime) {
        GHOST.root().getAllParts().forEach(ModelPart::resetPose);
        KeyframeAnimations.animate(GHOST, strike, accumulatedTime, 1.0F, GHOST_VEC);
        // head (CHAIN[0]) + body_0..body_{index-1} (CHAIN[1+k]).
        float dx = GHOST.bones[0].x, dy = GHOST.bones[0].y, dz = GHOST.bones[0].z;
        for (int k = 0; k < index; k++) {
            ModelPart b = GHOST.bones[1 + k];
            dx += b.x; dy += b.y; dz += b.z;
        }
        this.segment.x += dx;
        this.segment.y += dy;
        this.segment.z += dz;
    }

    /** Mappe le numero de combo (1..3) vers l'animation de strike correspondante. */
    private static AnimationDefinition strikeFor(int comboNumber) {
        return switch (comboNumber) {
            case 1 -> BoaAnimations.ATTACK_STRIKE;
            case 2 -> BoaAnimations.ATTACK_STRIKE_2;
            case 3 -> BoaAnimations.ATTACK_STRIKE_3;
            default -> null;
        };
    }

    /**
     * Capture le delta Y (px modele) de la pose animee du segment par rapport a sa pose de
     * repos, et l'ecrit dans entity.bodyAnimY pour que positionRider (thread jeu) suive le
     * cabrage sans rejouer setupAnim. Au repos (hors combo) le delta vaut 0. Calque sur le
     * captureBodyState du Tiger/Kodiak.
     *
     * @param restPoseYSum somme des Y de repos des bones de la chaine (ALL=0 + body_0=TAIL_Y)
     * @param boneChain    bones de la racine jusqu'au bone du siege
     */
    private void captureBodyState(BoaTailPart entity, float restPoseYSum, ModelPart... boneChain) {
        if (!entity.level().isClientSide()) return;
        float xSum = 0f, ySum = 0f, zSum = 0f;
        for (ModelPart bone : boneChain) {
            xSum += bone.x;
            ySum += bone.y;
            zSum += bone.z;
        }
        // X et Z de repos = 0 (bones centres), donc le delta = la somme brute. Pour Y, on
        // soustrait la pose de repos (ALL=0 + body_0=TAIL_Y). Inclut deja la translation des
        // ancetres ajoutee par addAncestorTranslation (la fente avant de "head").
        entity.bodyAnimX = xSum;
        entity.bodyAnimY = ySum - restPoseYSum;
        entity.bodyAnimZ = zSum;
        // Renflement de digestion du segment-siege (applyDigestionBulge a deja regle yScale en
        // amont dans setupAnim). Relu par positionRider pour remonter le rider quand ca gonfle.
        entity.bodyBulge = this.segment.yScale - 1f;
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
}
