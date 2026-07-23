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
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.client.animation.BoaAnimations;
import net.tiew.operationWild.entity.client.animation.TigerAnimations;

public class BoaModel<T extends BoaEntity> extends HierarchicalModel<T> implements OWFlagModel {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "boa_default"), "main");

    /** Texture ne contenant que la hampe du drapeau de tribu (tout le reste est transparent). */
    private static final ResourceLocation FLAG_POLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/boa/boa_flag.png");

    /**
     * Rectangle de la toile dans l'espace local de l'os {@code flag}, en pixels modele.
     * Recopie la boite declaree par {@code addFlagParts} : Y dans [-5.75, 5.25], Z dans [0.5, 19.5].
     */
    private static final Anchor FLAG_ANCHOR = new Anchor(-5.75f, 11f, 0.5f, 19f);

    public static boolean RENDER_FULL_BODY = false;

    private static final float GUI_WAVE_AMP = 0.75f;
    private static final float GUI_WAVE_STEP = 0.95f;
    private static final float GUI_BODY_BACK_OFFSET = 48.0f;

    private final ModelPart ALL2;
    private final ModelPart ALL;
    private final ModelPart head;
    private final ModelPart mouth;
    private final ModelPart mouth_up;
    private final ModelPart mouth_down;
    private final ModelPart tong;
    private final ModelPart body_0;
    private final ModelPart body_1;
    private final ModelPart body_2;
    private final ModelPart body_3;
    private final ModelPart body_4;
    private final ModelPart body_5;
    private final ModelPart body_6;
    /** Nuls sur les definitions de skin qui ne declarent pas le porte-drapeau. */
    private final ModelPart mainFlag;
    private final ModelPart flag;

    public BoaModel(ModelPart root) {
        this.ALL2 = root.getChild("ALL2");
        this.ALL = this.ALL2.getChild("ALL");
        this.head = this.ALL.getChild("head");
        this.mouth = this.head.getChild("mouth");
        this.mouth_up = this.mouth.getChild("mouth_up");
        this.mouth_down = this.mouth.getChild("mouth_down");
        this.tong = this.mouth_down.getChild("tong");
        this.body_0 = this.head.getChild("body_0");
        this.body_1 = this.body_0.getChild("body_1");
        this.body_2 = this.body_1.getChild("body_2");
        this.body_3 = this.body_2.getChild("body_3");
        this.body_4 = this.body_3.getChild("body_4");
        this.body_5 = this.body_4.getChild("body_5");
        this.body_6 = this.body_5.getChild("body_6");
        this.mainFlag = this.body_2.hasChild("mainFlag") ? this.body_2.getChild("mainFlag") : null;
        this.flag = this.mainFlag != null ? this.mainFlag.getChild("flag") : null;

        // Le porte-drapeau n'est dessine que par renderTribeFlagPole : masque d'entree, il ne risque
        // pas de l'etre avec la texture des ecailles par le modele de base ni par un modele
        // d'overlay de skin, qui ne rejoue pas setupAnim.
        if (this.mainFlag != null) this.mainFlag.visible = false;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        
        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 20.0F, 102.5F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -49.0F));

        PartDefinition head = ALL.addOrReplaceChild("head", CubeListBuilder.create().texOffs(211, 56).addBox(-5.0F, -0.5F, -3.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, -48.0F));

        PartDefinition mouth = head.addOrReplaceChild("mouth", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition mouth_up = mouth.addOrReplaceChild("mouth_up", CubeListBuilder.create().texOffs(0, 82).addBox(-3.5F, -2.0F, -10.0F, 7.0F, 2.0F, 10.0F, new CubeDeformation(0.001F))
                .texOffs(40, 0).addBox(-5.5F, -2.0F, -7.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.001F))
                .texOffs(40, 0).mirror().addBox(3.5F, -2.0F, -7.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.001F)).mirror(false)
                .texOffs(34, 87).addBox(-3.5F, 0.0F, -10.0F, 7.0F, 1.0F, 10.0F, new CubeDeformation(0.001F))
                .texOffs(108, 114).addBox(-3.4F, 1.0F, -10.0F, 0.0F, 3.0F, 10.0F, new CubeDeformation(0.001F))
                .texOffs(108, 114).mirror().addBox(3.4F, 1.0F, -10.0F, 0.0F, 3.0F, 10.0F, new CubeDeformation(0.001F)).mirror(false)
                .texOffs(101, 37).mirror().addBox(-3.0F, -5.0F, -9.0F, 0.0F, 3.0F, 11.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(101, 37).addBox(3.0F, -5.0F, -9.0F, 0.0F, 3.0F, 11.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, -1.0F, 0.0F));

        PartDefinition cube_r1 = mouth_up.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(171, 219).addBox(-2.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.01F))
                .texOffs(179, 239).addBox(-2.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(4.5F, -0.5F, -1.0F, 0.2533F, -0.7519F, -0.3622F));

        PartDefinition cube_r2 = mouth_up.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(171, 219).mirror().addBox(-8.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(179, 239).mirror().addBox(-8.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-4.5F, -0.5F, -1.0F, 0.2533F, 0.7519F, 0.3622F));

        PartDefinition mouth_down = mouth.addOrReplaceChild("mouth_down", CubeListBuilder.create().texOffs(0, 69).addBox(-3.5F, 0.0F, -10.0F, 7.0F, 3.0F, 10.0F, new CubeDeformation(0.001F))
                .texOffs(0, 4).addBox(-3.5F, -1.0F, 0.0F, 7.0F, 1.0F, 0.0F, new CubeDeformation(0.001F))
                .texOffs(68, 87).addBox(-3.5F, -1.0F, -10.0F, 7.0F, 1.0F, 10.0F, new CubeDeformation(0.001F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition tong = mouth_down.addOrReplaceChild("tong", CubeListBuilder.create().texOffs(-10, 23).addBox(-3.5F, -0.025F, -10.0F, 7.0F, 0.0F, 10.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body_0 = head.addOrReplaceChild("body_0", CubeListBuilder.create().texOffs(48, 0).addBox(-3.5F, -3.0F, -1.0F, 7.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 1.0F));

        PartDefinition cube_r3 = body_0.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(171, 219).addBox(-2.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(4.5F, -1.5F, 8.0F, 0.2533F, -0.7519F, -0.3622F));

        PartDefinition cube_r4 = body_0.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(171, 219).mirror().addBox(-8.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-4.5F, -1.5F, 8.0F, 0.2533F, 0.7519F, 0.3622F));

        PartDefinition cube_r5 = body_0.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(179, 239).mirror().addBox(-8.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-4.5F, -1.5F, 9.0F, 0.2533F, 0.7519F, 0.3622F));

        PartDefinition cube_r6 = body_0.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(179, 239).addBox(-2.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(4.5F, -1.5F, 9.0F, 0.2533F, -0.7519F, -0.3622F));

        PartDefinition cube_r7 = body_0.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(205, 49).mirror().addBox(0.1604F, -3.6831F, -0.1747F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-4.5F, 0.0F, -3.0F, 0.1747F, -0.043F, -0.0076F));

        PartDefinition cube_r8 = body_0.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(205, 49).addBox(-0.1604F, -3.6831F, -0.1747F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(4.5F, 0.0F, -3.0F, 0.1747F, 0.043F, 0.0076F));

        PartDefinition body_1 = body_0.addOrReplaceChild("body_1", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -1.0F, 8.0F, 7.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(202, 7).addBox(-4.0F, -4.0F, -1.0F, 8.0F, 7.0F, 16.0F, new CubeDeformation(0.5F))
                .texOffs(135, 138).addBox(0.0F, -14.0F, -1.0F, 0.0F, 10.0F, 15.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 0.0F, 16.0F));

        PartDefinition cube_r9 = body_1.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(205, 180).addBox(5.0F, -11.0F, -1.0F, 11.0F, 11.0F, 0.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(-9.0F, 5.5F, 2.0F, -0.0128F, -0.4635F, -0.5778F));

        PartDefinition cube_r10 = body_1.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(212, 82).mirror().addBox(0.0F, -2.4362F, -0.3504F, 0.0F, 7.0F, 16.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-5.1F, -4.0F, -3.7F, 0.641F, 0.2116F, 0.4646F));

        PartDefinition cube_r11 = body_1.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(212, 82).addBox(0.0F, -2.4362F, -0.3504F, 0.0F, 7.0F, 16.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(5.1F, -4.0F, -3.7F, 0.641F, -0.2116F, -0.4646F));

        PartDefinition body_2 = body_1.addOrReplaceChild("body_2", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -1.0F, 8.0F, 7.0F, 16.0F, new CubeDeformation(-0.001F))
                .texOffs(125, 0).addBox(-4.0F, -4.0F, -1.0F, 8.0F, 7.0F, 16.0F, new CubeDeformation(0.5F))
                .texOffs(135, 138).addBox(0.0F, -10.0F, -1.0F, 0.0F, 6.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 16.0F));

        PartDefinition body_3 = body_2.addOrReplaceChild("body_3", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-4.0F, -4.0F, 0.0F, 8.0F, 7.0F, 16.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(135, 138).addBox(0.0F, -10.0F, 0.0F, 0.0F, 6.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 15.0F));

        PartDefinition cube_r12 = body_3.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(157, 199).addBox(6.0F, -11.0F, -1.0F, 10.0F, 11.0F, 0.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(-8.0F, -2.5F, 3.0F, -0.1117F, -0.0577F, 0.2342F));

        PartDefinition cube_r13 = body_3.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(88, 151).addBox(6.0F, -12.0F, -1.0F, 13.0F, 12.0F, 0.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(3.0F, 1.5F, -5.0F, 1.0653F, -1.3361F, -2.7575F));

        PartDefinition body_4 = body_3.addOrReplaceChild("body_4", CubeListBuilder.create().texOffs(48, 0).addBox(-3.5F, -3.0F, 0.0F, 7.0F, 6.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(146, 78).addBox(0.0F, -7.0F, 3.0F, 0.0F, 4.0F, 9.0F, new CubeDeformation(0.001F)), PartPose.offset(0.0F, 0.0F, 16.0F));

        PartDefinition cube_r14 = body_4.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(205, 202).addBox(6.0F, -7.0F, -1.0F, 8.0F, 7.0F, 0.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(-3.0F, -5.5F, 1.0F, -0.5628F, -0.7972F, 0.7507F));

        PartDefinition body_5 = body_4.addOrReplaceChild("body_5", CubeListBuilder.create().texOffs(48, 0).mirror().addBox(-3.5F, -3.0F, 0.0F, 7.0F, 6.0F, 16.0F, new CubeDeformation(-0.001F)).mirror(false)
                .texOffs(146, 78).addBox(0.0F, -7.0F, 3.0F, 0.0F, 4.0F, 9.0F, new CubeDeformation(0.001F)), PartPose.offset(0.0F, 0.0F, 16.0F));

        PartDefinition body_6 = body_5.addOrReplaceChild("body_6", CubeListBuilder.create().texOffs(48, 66).addBox(-2.5F, -3.0F, 0.0F, 5.0F, 5.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(135, 138).addBox(0.0F, -9.0F, 0.0F, 0.0F, 6.0F, 15.0F, new CubeDeformation(0.001F))
                .texOffs(14, 171).addBox(-6.0F, 0.0F, 11.0F, 12.0F, 0.0F, 25.0F, new CubeDeformation(0.001F)), PartPose.offset(0.0F, 1.0F, 16.0F));

        addFlagParts(body_2);

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    /**
     * Greffe le porte-drapeau de tribu sur le dos du boa : la hampe ({@code mainFlag}) et sa toile
     * ({@code flag}, quad d'epaisseur nulle de 11 x 19 px). Il est porte par {@code body_2}, le
     * segment qui suit la selle.
     *
     * <p>A appeler depuis <b>chaque</b> definition de calque du boa, skins compris, pour que le
     * drapeau suive l'entite quelle que soit son apparence. Ces os ne sont jamais dessines avec la
     * texture du boa : {@link net.tiew.operationWild.entity.client.layer.OWTribeFlagLayer} les
     * affiche le temps de sa passe, avec la texture de hampe dediee.</p>
     */
    public static void addFlagParts(PartDefinition body_2) {
        addFlagParts(body_2, 10.5F);
    }

    /**
     * @param z position de la hampe le long du segment. Le modele de tete et celui des segments de
     *          queue ne declarent pas leur boite body_2 au meme endroit (z = -1 contre z = 0) : la
     *          queue demande donc 1 px de plus pour que la hampe tombe au meme point du maillage.
     */
    public static void addFlagParts(PartDefinition body_2, float z) {
        PartDefinition mainFlag = body_2.addOrReplaceChild("mainFlag", CubeListBuilder.create().texOffs(186, 144).addBox(-0.5F, -18.75F, -0.5F, 1.0F, 20.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(191, 143).addBox(-0.5F, -18.75F, 0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(191, 143).addBox(-0.5F, -6.75F, 0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(178, 158).addBox(-1.0F, -3.725F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.1F))
                .texOffs(178, 158).addBox(-1.0F, 0.275F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.1F))
                .texOffs(171, 151).addBox(-1.0F, -2.725F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.1F))
                .texOffs(171, 143).addBox(-1.0F, -20.75F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -2.0F, z));

        mainFlag.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(206, 129).addBox(0.0F, -5.75F, 0.5F, 0.0F, 11.0F, 19.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, -12.0F, 0.0F));
    }

    // -- Drapeau de tribu (OWFlagModel) ------------------------------------------

    @Override
    public boolean hasTribeFlag() {
        return this.mainFlag != null && this.flag != null;
    }

    @Override
    public void renderTribeFlagPole(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
        if (!hasTribeFlag()) return;
        poseStack.pushPose();
        translateToFlagCarrier(poseStack);
        // Visible le temps de ce seul appel : le reste du pipeline doit continuer a l'ignorer.
        this.mainFlag.visible = true;
        this.mainFlag.render(poseStack, buffer, packedLight, packedOverlay, 0xFFFFFFFF);
        this.mainFlag.visible = false;
        poseStack.popPose();
    }

    @Override
    public void translateToTribeFlag(PoseStack poseStack) {
        if (!hasTribeFlag()) return;
        translateToFlagCarrier(poseStack);
        this.mainFlag.translateAndRotate(poseStack);
        this.flag.translateAndRotate(poseStack);
    }

    /**
     * Chaine menant a {@code body_2}. Chez le boa les segments du corps descendent de {@code head}
     * et non d'un tronc : la hampe suit donc toute l'ondulation du serpent devant elle.
     */
    private void translateToFlagCarrier(PoseStack poseStack) {
        this.ALL2.translateAndRotate(poseStack);
        this.ALL.translateAndRotate(poseStack);
        this.head.translateAndRotate(poseStack);
        this.body_0.translateAndRotate(poseStack);
        this.body_1.translateAndRotate(poseStack);
        this.body_2.translateAndRotate(poseStack);
    }

    @Override
    public ResourceLocation tribeFlagPoleTexture() {
        return FLAG_POLE_TEXTURE;
    }

    @Override
    public Anchor tribeFlagAnchor() {
        return FLAG_ANCHOR;
    }

    @Override
    public void setupAnim(T boa, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.head.yRot = 0f;
        this.head.xRot = 0f;

        // Les coups sont EMPILES : les transformations s'ajoutent a la pose courante, donc la fin
        // d'un coup et le debut du suivant se melangent d'eux-memes. Un {@code return} par branche
        // n'en laissait jouer qu'un a la fois : le precedent restait fige sur sa derniere image,
        // puis la pose sautait. La clause {@code !isCombo()} annulait par ailleurs le repli qui
        // laisse une animation finir son geste.
        if (boa.isCombo(1) || boa.attack1Combo.isStarted()) {
            this.animate(boa.attack1Combo, BoaAnimations.ATTACK_STRIKE, ageInTicks, 1 * OWEntity.comboSpeedMultiplier);
        }
        if (boa.isCombo(2) || boa.attack2Combo.isStarted()) {
            this.animate(boa.attack2Combo, BoaAnimations.ATTACK_STRIKE_2, ageInTicks, 1 * OWEntity.comboSpeedMultiplier);
        }
        if (boa.isCombo(3) || boa.attack3Combo.isStarted()) {
            this.animate(boa.attack3Combo, BoaAnimations.ATTACK_STRIKE_3, ageInTicks, 1 * OWEntity.comboSpeedMultiplier);
        }

        if (boa.tongAnimationState.isStarted()) {
            this.animate(boa.tongAnimationState, BoaAnimations.TONG, ageInTicks, 1.0f);
        }

        boolean showBody = RENDER_FULL_BODY;
        this.body_0.visible = showBody;
        this.body_1.visible = showBody;
        this.body_2.visible = showBody;
        this.body_3.visible = showBody;
        this.body_4.visible = showBody;
        this.body_5.visible = showBody;
        this.body_6.visible = showBody;

        if (boa.isSitting()) {
            this.animate(boa.sittingAnimationState, BoaAnimations.SIT, ageInTicks, 1.0f);
            if (showBody) applyGuiBodyPose();
            return;
        }

        this.animate(boa.idleAnimationState, BoaAnimations.MISC_IDLE, ageInTicks, 1.0f);
        if (showBody) applyGuiBodyPose();
    }

    private void applyGuiBodyPose() {
        this.ALL2.z -= GUI_BODY_BACK_OFFSET;
        this.body_0.yRot = GUI_WAVE_AMP * (float) Math.sin(GUI_WAVE_STEP * 0f);
        this.body_1.yRot = GUI_WAVE_AMP * (float) Math.sin(GUI_WAVE_STEP * 1f);
        this.body_2.yRot = GUI_WAVE_AMP * (float) Math.sin(GUI_WAVE_STEP * 2f);
        this.body_3.yRot = GUI_WAVE_AMP * (float) Math.sin(GUI_WAVE_STEP * 3f);
        this.body_4.yRot = GUI_WAVE_AMP * (float) Math.sin(GUI_WAVE_STEP * 4f);
        this.body_5.yRot = GUI_WAVE_AMP * (float) Math.sin(GUI_WAVE_STEP * 5f);
        this.body_6.yRot = GUI_WAVE_AMP * (float) Math.sin(GUI_WAVE_STEP * 6f);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root() {
        return this.ALL2;
    }

    public void copyPoseFrom(BoaModel<?> src) {
        this.ALL2.copyFrom(src.ALL2);
        this.ALL.copyFrom(src.ALL);
        this.head.copyFrom(src.head);
        this.mouth.copyFrom(src.mouth);
        this.mouth_up.copyFrom(src.mouth_up);
        this.mouth_down.copyFrom(src.mouth_down);
        this.body_0.copyFrom(src.body_0);
        this.body_1.copyFrom(src.body_1);
        this.body_2.copyFrom(src.body_2);
        this.body_3.copyFrom(src.body_3);
        this.body_4.copyFrom(src.body_4);
        this.body_5.copyFrom(src.body_5);
        this.body_6.copyFrom(src.body_6);
    }
}