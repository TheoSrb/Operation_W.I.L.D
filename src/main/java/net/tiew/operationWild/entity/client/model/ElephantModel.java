package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.client.animation.ElephantAnimations;
import net.tiew.operationWild.entity.variants.ElephantVariant;

public class ElephantModel<T extends ElephantEntity> extends OWComboModel<T> implements OWFlagModel {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "elephant_default"), "main");

    /** Texture ne contenant que la hampe du drapeau de tribu (tout le reste est transparent). */
    private static final ResourceLocation FLAG_POLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/elephant/elephant_flag.png");

    /** Rectangle de la toile dans l'espace local de l'os {@code flag}, en pixels modèle. */
    private static final Anchor FLAG_ANCHOR = new Anchor(-5.75f, 11f, 0.5f, 19f);

    /**
     * Somme des {@code y} de repos de la chaîne {@code ALL2 → ALL → body}, relevée dans
     * {@link #createBodyLayer()} : -7 + 0 + -2. C'est l'origine à partir de laquelle
     * {@code captureBodyState} mesure le déplacement animé de l'assise.
     */
    private static final float REST_POSE_Y_SUM = -9f;

    // Tracks the limbSwing value from the previous frame to detect animation crossings.
    private float prevLimbSwing = 0f;

    private final ModelPart ALL2;
    private final ModelPart ALL;
    private final ModelPart right_arm;
    private final ModelPart right_leg;
    private final ModelPart left_leg;
    private final ModelPart left_arm;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart left_ear;
    private final ModelPart right_ear;
    private final ModelPart trunk;
    private final ModelPart trunk2;
    private final ModelPart left_eyeBall;
    private final ModelPart right_eyeBall;
    private final ModelPart tail;
    private final ModelPart tail2;
    private final ModelPart demon_left_wing;
    private final ModelPart demon_right_wing;
    private final ModelPart mainFlag;
    private final ModelPart flag;

    public ElephantModel(ModelPart root) {
        this.ALL2 = root.getChild("ALL2");
        this.ALL = this.ALL2.getChild("ALL");
        this.right_arm = this.ALL.getChild("right_arm");
        this.right_leg = this.ALL.getChild("right_leg");
        this.left_leg = this.ALL.getChild("left_leg");
        this.left_arm = this.ALL.getChild("left_arm");
        this.body = this.ALL.getChild("body");
        this.head = this.body.getChild("head");
        this.left_ear = this.head.getChild("left_ear");
        this.right_ear = this.head.getChild("right_ear");
        this.trunk = this.head.getChild("trunk");
        this.trunk2 = this.trunk.getChild("trunk2");
        this.left_eyeBall = this.head.getChild("left_eyeBall");
        this.right_eyeBall = this.head.getChild("right_eyeBall");
        this.tail = this.body.getChild("tail");
        this.tail2 = this.tail.getChild("tail2");
        this.demon_left_wing = this.body.getChild("demon_left_wing");
        this.demon_right_wing = this.body.getChild("demon_right_wing");

        this.mainFlag = this.body.hasChild("mainFlag") ? this.body.getChild("mainFlag") : null;
        this.flag = this.mainFlag != null ? this.mainFlag.getChild("flag") : null;
        if (this.mainFlag != null) this.mainFlag.visible = false;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, -7.0F, 2.0F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition right_arm = ALL.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(60, 70).mirror().addBox(-4.5F, 0.0F, -6.0F, 9.0F, 20.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-6.0F, 11.0F, -14.0F));

        PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(60, 70).mirror().addBox(-4.5F, 0.0F, -6.0F, 9.0F, 20.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-8.0F, 11.0F, 14.0F));

        PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(60, 70).addBox(-4.5F, 0.0F, -6.0F, 9.0F, 20.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 11.0F, 14.0F));

        PartDefinition left_arm = ALL.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(60, 70).addBox(-4.5F, 0.0F, -6.0F, 9.0F, 20.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 11.0F, -14.0F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-13.5F, -13.0F, -21.0F, 27.0F, 26.0F, 44.0F, new CubeDeformation(0.0F))
        .texOffs(114, 186).addBox(-13.5F, -13.0F, -21.0F, 27.0F, 26.0F, 44.0F, new CubeDeformation(0.5F))
        .texOffs(242, 108).addBox(9.5F, -43.0F, -15.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(0, 220).addBox(-11.5F, -45.0F, -15.0F, 23.0F, 2.0F, 34.0F, new CubeDeformation(0.0F))
        .texOffs(121, 132).addBox(-13.5F, -47.0F, -18.0F, 27.0F, 2.0F, 40.0F, new CubeDeformation(0.0F))
        .texOffs(160, 111).addBox(-13.5F, -45.0F, 22.0F, 27.0F, 11.0F, 0.0F, new CubeDeformation(0.0F))
        .texOffs(160, 111).addBox(-13.5F, -45.0F, -18.0F, 27.0F, 11.0F, 0.0F, new CubeDeformation(0.0F))
        .texOffs(242, 108).mirror().addBox(-11.5F, -43.0F, -15.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
        .texOffs(242, 108).mirror().addBox(-11.5F, -43.0F, 17.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
        .texOffs(242, 108).addBox(9.5F, -43.0F, 17.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(176, 0).addBox(-12.5F, -15.0F, -6.0F, 25.0F, 2.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, -1.0F));

        PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(160, 96).mirror().addBox(-11.0F, 13.0F, -28.0F, 27.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-14.5F, -58.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r2 = body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(160, 96).addBox(-16.0F, 13.0F, -28.0F, 27.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.5F, -58.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition cube_r3 = body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(222, 47).mirror().addBox(0.0F, -5.0102F, -3.29F, 0.0F, 23.0F, 8.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(0.6F, 7.75F, -22.25F, 0.0F, 1.5708F, -1.8326F));

        PartDefinition cube_r4 = body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(204, 41).mirror().addBox(0.1881F, -11.6264F, -0.2154F, 0.0F, 14.0F, 8.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(16.65F, -1.0F, -22.25F, -0.0918F, -0.3457F, 0.0316F));

        PartDefinition cube_r5 = body.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(120, -11).mirror().addBox(0.1158F, -8.8537F, -5.3647F, 0.0F, 9.0F, 30.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(15.7F, -11.0F, -18.3F, 0.3752F, -0.3683F, -0.5972F));

        PartDefinition cube_r6 = body.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(222, 47).addBox(0.0F, -5.0102F, -3.29F, 0.0F, 23.0F, 8.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-0.6F, 7.75F, -22.25F, 0.0F, -1.5708F, 1.8326F));

        PartDefinition cube_r7 = body.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(204, 41).addBox(-0.1881F, -11.6264F, -0.2154F, 0.0F, 14.0F, 8.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-16.65F, -1.0F, -22.25F, -0.0918F, 0.3457F, -0.0316F));

        PartDefinition cube_r8 = body.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(120, -11).addBox(-0.1158F, -8.8537F, -5.3647F, 0.0F, 9.0F, 30.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-15.7F, -11.0F, -18.3F, 0.3752F, 0.3683F, 0.5972F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 70).addBox(-7.5F, -9.0F, -15.0F, 15.0F, 18.0F, 15.0F, new CubeDeformation(0.0F))
        .texOffs(52, 108).addBox(-7.5F, -7.0F, -0.5F, 15.0F, 17.0F, 1.0F, new CubeDeformation(0.4F))
        .texOffs(98, 108).addBox(-7.5F, -12.9F, 0.0F, 15.0F, 6.0F, 0.0F, new CubeDeformation(0.05F))
        .texOffs(98, 114).addBox(-7.5F, 10.5F, -0.1F, 15.0F, 6.0F, 0.0F, new CubeDeformation(0.05F))
        .texOffs(99, 127).addBox(-13.4F, -9.0F, -0.1F, 6.0F, 20.0F, 0.0F, new CubeDeformation(0.05F))
        .texOffs(99, 127).mirror().addBox(7.4F, -9.0F, -0.1F, 6.0F, 20.0F, 0.0F, new CubeDeformation(0.05F)).mirror(false)
        .texOffs(142, 37).addBox(-7.5F, -9.0F, -15.0F, 15.0F, 18.0F, 15.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, -7.0F, -21.0F));

        PartDefinition cube_r9 = head.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(140, 83).mirror().addBox(-9.0F, -5.0F, -9.0F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
        .texOffs(116, 132).mirror().addBox(-8.0F, 11.0F, -17.0F, 3.0F, 3.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false)
        .texOffs(140, 110).mirror().addBox(-8.0F, 3.0F, -8.0F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 12.0F, -9.0F, -0.3442F, 0.0594F, 0.1642F));

        PartDefinition cube_r10 = head.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(223, 211).mirror().addBox(-8.0F, 3.0F, -8.0F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
        .texOffs(223, 184).mirror().addBox(-9.0F, -5.0F, -9.0F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
        .texOffs(61, 163).mirror().addBox(-8.0F, 11.0F, -17.0F, 3.0F, 3.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.0F, 11.0F, -6.0F, -0.2898F, 0.1975F, 0.582F));

        PartDefinition cube_r11 = head.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(61, 163).addBox(5.0F, 11.0F, -17.0F, 3.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
        .texOffs(223, 211).addBox(5.0F, 3.0F, -8.0F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.0F))
        .texOffs(223, 184).addBox(4.0F, -5.0F, -9.0F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 11.0F, -6.0F, -0.2898F, -0.1975F, -0.582F));

        PartDefinition cube_r12 = head.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(116, 132).addBox(5.0F, 11.0F, -17.0F, 3.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
        .texOffs(140, 110).addBox(5.0F, 3.0F, -8.0F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.0F))
        .texOffs(140, 83).addBox(4.0F, -5.0F, -9.0F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 12.0F, -9.0F, -0.3442F, -0.0594F, -0.1642F));

        PartDefinition cube_r13 = head.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(144, 187).addBox(6.0F, -10.5F, -1.5F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5F, -5.5F, -11.5F, 0.1666F, -0.0859F, -0.3564F));

        PartDefinition cube_r14 = head.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(144, 187).mirror().addBox(-9.0F, -10.5F, -1.5F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.5F, -5.5F, -11.5F, 0.1666F, 0.0859F, 0.3564F));

        PartDefinition cube_r15 = head.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(24, 166).mirror().addBox(-9.0F, -1.5F, -1.5F, 9.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-6.5F, -7.5F, -11.5F, 0.139F, 0.126F, 0.0962F));

        PartDefinition cube_r16 = head.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(24, 166).addBox(0.0F, -1.5F, -1.5F, 9.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.5F, -7.5F, -11.5F, 0.139F, -0.126F, -0.0962F));

        PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(0, 129).addBox(-1.0F, -12.0F, -1.0F, 19.0F, 24.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(7.5F, -1.0F, -7.0F));

        PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(0, 103).mirror().addBox(-18.0F, -12.0F, -1.0F, 19.0F, 24.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-7.5F, -1.0F, -7.0F));

        PartDefinition trunk = head.addOrReplaceChild("trunk", CubeListBuilder.create().texOffs(42, 132).addBox(-3.5F, -3.0F, -4.0F, 7.0F, 16.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, -16.0F));

        PartDefinition trunk2 = trunk.addOrReplaceChild("trunk2", CubeListBuilder.create().texOffs(70, 132).addBox(-2.5F, 0.0F, -3.0F, 5.0F, 16.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 13.0F, 0.0F));

        PartDefinition left_eyeBall = head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(44, 105).addBox(-2.5F, -0.5F, 0.0F, 5.0F, 1.0F, 0.0F, new CubeDeformation(0.1F)), PartPose.offset(5.0F, -1.5F, -15.0F));

        PartDefinition right_eyeBall = head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(44, 105).mirror().addBox(-2.5F, -0.5F, 0.0F, 5.0F, 1.0F, 0.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offset(-5.0F, -1.5F, -15.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 185).addBox(-1.5F, -1.0F, 0.05F, 3.0F, 22.0F, 0.0F, new CubeDeformation(0.1F))
        .texOffs(0, 182).addBox(0.05F, -1.0F, -1.5F, 0.0F, 22.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, 23.0F, 0.0873F, 0.0F, 0.0F));

        PartDefinition tail2 = tail.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(45, 190).addBox(-3.5F, -1.0F, 0.05F, 7.0F, 28.0F, 0.0F, new CubeDeformation(0.1F))
        .texOffs(45, 183).addBox(0.05F, -1.0F, -3.5F, 0.0F, 28.0F, 7.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 15.0F, 0.0F));

        PartDefinition demon_left_wing = body.addOrReplaceChild("demon_left_wing", CubeListBuilder.create().texOffs(44, 191).addBox(-30.0F, 0.0F, -16.0F, 30.0F, 0.0F, 36.0F, new CubeDeformation(0.01F)), PartPose.offset(-13.5F, -8.0F, -10.0F));

        PartDefinition demon_right_wing = body.addOrReplaceChild("demon_right_wing", CubeListBuilder.create().texOffs(44, 191).mirror().addBox(0.0F, 0.0F, -16.0F, 30.0F, 0.0F, 36.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(13.5F, -8.0F, -10.0F));

        addFlagParts(body);

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    /**
     * Hampe et toile de la bannière de tribu, plantées sur la nacelle plutôt que sur l'échine :
     * c'est la seule surface plate du dos, et l'étendard y tient debout sans traverser le cuir.
     *
     * <p>Ces pièces restent invisibles hors de la passe de
     * {@link net.tiew.operationWild.entity.client.layer.OWTribeFlagLayer}, qui les affiche avec sa
     * texture de hampe dédiée. Leur empreinte UV recouvre celle du corps.</p>
     */
    public static void addFlagParts(PartDefinition body) {
        PartDefinition mainFlag = body.addOrReplaceChild("mainFlag", CubeListBuilder.create().texOffs(15, 1).addBox(-0.5F, -18.75F, -0.5F, 1.0F, 20.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-0.5F, -18.75F, 0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-0.5F, -6.75F, 0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(7, 15).addBox(-1.0F, -3.725F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.1F))
                .texOffs(7, 15).addBox(-1.0F, 0.275F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.1F))
                .texOffs(0, 8).addBox(-1.0F, -2.725F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.1F))
                .texOffs(0, 0).addBox(-1.0F, -20.75F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, -47.0F, 12.0F));

        mainFlag.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(0, 98).addBox(0.0F, -5.75F, 0.5F, 0.0F, 11.0F, 19.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, -12.0F, 0.0F));
    }

    // ── Drapeau de tribu (OWFlagModel) ───────────────────────────────────────────

    @Override
    public boolean hasTribeFlag() {
        return this.mainFlag != null && this.flag != null;
    }

    @Override
    public void renderTribeFlagPole(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
        if (!hasTribeFlag()) return;
        poseStack.pushPose();
        this.ALL2.translateAndRotate(poseStack);
        this.ALL.translateAndRotate(poseStack);
        this.body.translateAndRotate(poseStack);
        // Visible le temps de ce seul appel : le reste du pipeline doit continuer à l'ignorer.
        this.mainFlag.visible = true;
        this.mainFlag.render(poseStack, buffer, packedLight, packedOverlay, 0xFFFFFFFF);
        this.mainFlag.visible = false;
        poseStack.popPose();
    }

    @Override
    public void translateToTribeFlag(PoseStack poseStack) {
        if (!hasTribeFlag()) return;
        this.ALL2.translateAndRotate(poseStack);
        this.ALL.translateAndRotate(poseStack);
        this.body.translateAndRotate(poseStack);
        this.mainFlag.translateAndRotate(poseStack);
        this.flag.translateAndRotate(poseStack);
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
    protected AnimationDefinition comboAnimation(int index) {
        return switch (index) {
            case 1 -> ElephantAnimations.ATTACK_STRIKE;
            case 2 -> ElephantAnimations.ATTACK_STRIKE_2;
            case 3 -> ElephantAnimations.ATTACK_STRIKE_3;
            default -> null;
        };
    }

    @Override
    protected float comboSpeed(int index) {
        return switch (index) {
            case 1 -> 0.925f;
            case 2 -> 1.05f;
            case 3 -> 1.15f;
            default -> 1.0f;
        };
    }

    @Override
    public void setupAnim(ElephantEntity elephant, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Les ailes n'appartiennent qu'au skin Démoniaque : elles vivent dans le maillage de base
        // pour éviter un second modèle, mais restent effacées partout ailleurs.
        boolean demon = elephant.getVariant() == ElephantVariant.Cosmetics.DEMON.variant;
        this.demon_left_wing.visible = demon;
        this.demon_right_wing.visible = demon;

        if (elephant.isBaby()) {
            float maturationPercent = (float) elephant.getMaturationPercentage() / 100f;
            float headScale = 1.6f - (1.6f - 1.0f) * maturationPercent;

            this.head.xScale *= headScale;
            this.head.yScale *= headScale;
            this.head.zScale *= headScale;
        }
        this.applyHeadRotation(netHeadYaw, headPitch);

        animateCombos(elephant, ageInTicks);
        captureBodyState(elephant, REST_POSE_Y_SUM, this.ALL2, this.ALL, this.body);

        if (elephant.isMad()) {
            this.left_eyeBall.xScale = 0;
            this.left_eyeBall.yScale = 0;
            this.left_eyeBall.zScale = 0;

            this.right_eyeBall.xScale = 0;
            this.right_eyeBall.yScale = 0;
            this.right_eyeBall.zScale = 0;
        }

        if (elephant.isEarthquaking()) {
            this.animate(elephant.earthquakeAnimationState, ElephantAnimations.EARTHQUAKE, ageInTicks, 1.0f);
            captureBodyState(elephant, REST_POSE_Y_SUM, this.ALL2, this.ALL, this.body);
            return;
        }

        if (elephant.transitionSitIdle.isStarted()) {
            this.animate(elephant.transitionSitIdle, ElephantAnimations.TRANSITION_SIT_IDLE, ageInTicks, 1.0f);
            captureBodyState(elephant, REST_POSE_Y_SUM, this.ALL2, this.ALL, this.body);
            return;
        }

        if (elephant.transitionIdleSit.isStarted()) {
            this.animate(elephant.transitionIdleSit, ElephantAnimations.TRANSITION_IDLE_SIT, ageInTicks, 1.0f);
            captureBodyState(elephant, REST_POSE_Y_SUM, this.ALL2, this.ALL, this.body);
            return;
        }

        // La sieste et le sommeil n'ont pas d'animation propre : l'éléphant dort debout, sur la
        // pose assise, qui est la seule posture de repos exportée.
        if (elephant.isNapping() || elephant.isSleeping() || elephant.isSitting()) {
            this.animate(elephant.sittingAnimationState, ElephantAnimations.SIT, ageInTicks, 1.0f);
            captureBodyState(elephant, REST_POSE_Y_SUM, this.ALL2, this.ALL, this.body);
            return;
        }

        this.animate(elephant.idleAnimationState, ElephantAnimations.MISC_IDLE, ageInTicks, 1.0f);

        applyShoulderBashLean(elephant);

        // Il n'existe qu'un cycle de déplacement : la course le rejoue plus vite et plus ample
        // plutôt que d'emprunter un galop qui n'a pas été animé.
        if (elephant.isRunning() || elephant.getState() == 2) {
            this.animateWalk(ElephantAnimations.MOVE_WALK, limbSwing, limbSwingAmount, 3.0f, 3.5f);

            if (walkAnimCrossed(ElephantAnimations.MOVE_WALK, limbSwing, 3.0f, 1200L)) elephant.onRightFootDown();
            if (walkAnimCrossed(ElephantAnimations.MOVE_WALK, limbSwing, 3.0f, 2900L)) elephant.onLeftFootDown();

        } else {
            this.animateWalk(ElephantAnimations.MOVE_WALK, limbSwing, limbSwingAmount, 5.0f, 2.5f);

            if (walkAnimCrossed(ElephantAnimations.MOVE_WALK, limbSwing, 5.0f, 1200L)) elephant.onRightFootDown();
            if (walkAnimCrossed(ElephantAnimations.MOVE_WALK, limbSwing, 5.0f, 2900L)) elephant.onLeftFootDown();
        }

        // Must be updated AFTER the event checks above, so they see the previous frame's value.
        this.prevLimbSwing = limbSwing;

        captureBodyState(elephant, REST_POSE_Y_SUM, this.ALL2, this.ALL, this.body);
    }

    /**
     * Repli procédural du Coup d'Épaule, faute d'animation exportée : l'éléphant s'incline
     * brutalement du côté du déport puis se redresse. Le pic est atteint au premier tiers du geste,
     * ce qui fait coïncider l'inclinaison maximale avec le tick où partent les dégâts.
     *
     * <p>À remplacer par un {@code AnimationState} dès que le geste existe dans Blockbench.</p>
     */
    private void applyShoulderBashLean(ElephantEntity elephant) {
        int timer = elephant.getShoulderBashTimer();
        if (timer <= 0) return;

        int duration = OWAttacksConstants.Elephant.SHOULDER_BASH_DURATION_TICKS;
        float progress = 1f - ((float) timer / duration);
        float envelope = progress < 0.33f
                ? progress / 0.33f
                : 1f - (progress - 0.33f) / 0.67f;

        float lean = envelope * 0.45f * elephant.getShoulderBashSide();
        this.ALL.zRot += lean;
        this.head.zRot -= lean * 0.5f;
    }

    /**
     * Captures the animated bone-chain Y delta into {@code elephant.bodyAnimY} so that
     * {@code positionRider()} (game thread) can read it without re-running setupAnim.
     * Must be called at every exit point of setupAnim, including early returns.
     */
    private void captureBodyState(ElephantEntity elephant, float restPoseYSum, ModelPart... boneChain) {
        if (!elephant.level().isClientSide()) return;
        elephant.setBodyZRot((float) Math.toDegrees(this.ALL.zRot + this.body.zRot));
        elephant.setBodyXRot((float) -Math.toDegrees(this.ALL.xRot + this.body.xRot));
        float ySum = 0f;
        for (ModelPart bone : boneChain) ySum += bone.y;
        elephant.bodyAnimY = ySum - (restPoseYSum * elephant.getScale());
    }

    /**
     * Returns {@code true} on the <em>exact frame</em> a looping walk animation crosses a keyframe time.
     *
     * <p>Les deux contacts au sol de {@code move.walk} sont relevés sur les canaux de position des
     * membres : les pattes avant-droite et arrière-gauche touchent à 1,2 s, les deux autres à 2,9 s.</p>
     */
    private boolean walkAnimCrossed(AnimationDefinition animation, float limbSwing, float speedScale, long triggerTimeMs) {
        long durationMs = (long) (animation.lengthInSeconds() * 1000f);
        if (durationMs <= 0) return false;

        long cur = ((long) (limbSwing * 50f * speedScale)) % durationMs;
        long prev = ((long) (prevLimbSwing * 50f * speedScale)) % durationMs;

        if (prev <= cur) return prev < triggerTimeMs && cur >= triggerTimeMs;
        return triggerTimeMs <= cur || triggerTimeMs > prev;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    /**
     * Nuque épaisse : ±22° seulement, contre ±30° pour le tigre. Un éléphant qui regarde derrière
     * lui tourne tout le corps, il ne dévisse pas la tête.
     */
    private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch) {
        pNetHeadYaw = Mth.clamp(pNetHeadYaw, -22.0F, 22.0F);
        pHeadPitch = Mth.clamp(pHeadPitch, -22.0F, 22.0F);

        this.head.yRot = pNetHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = pHeadPitch * ((float) Math.PI / 180F);
    }

    @Override
    public ModelPart root() {
        return this.ALL2;
    }

    /** Copie la pose résolue depuis un modèle déjà animé (skins en mode OVERLAY). */
    public void copyPoseFrom(ElephantModel<?> src) {
        this.ALL2.copyFrom(src.ALL2);
        this.ALL.copyFrom(src.ALL);
        this.right_arm.copyFrom(src.right_arm);
        this.right_leg.copyFrom(src.right_leg);
        this.left_leg.copyFrom(src.left_leg);
        this.left_arm.copyFrom(src.left_arm);
        this.body.copyFrom(src.body);
        this.head.copyFrom(src.head);
        this.left_ear.copyFrom(src.left_ear);
        this.right_ear.copyFrom(src.right_ear);
        this.trunk.copyFrom(src.trunk);
        this.trunk2.copyFrom(src.trunk2);
        this.left_eyeBall.copyFrom(src.left_eyeBall);
        this.right_eyeBall.copyFrom(src.right_eyeBall);
        this.tail.copyFrom(src.tail);
        this.tail2.copyFrom(src.tail2);
        this.demon_left_wing.copyFrom(src.demon_left_wing);
        this.demon_right_wing.copyFrom(src.demon_right_wing);
    }
}
