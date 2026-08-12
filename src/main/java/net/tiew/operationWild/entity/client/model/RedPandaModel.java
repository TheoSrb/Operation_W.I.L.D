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
import net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.client.animation.RedPandaAnimations;

public class RedPandaModel<T extends RedPandaEntity> extends OWComboModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "red_panda"), "main");

    private static final float WALK_SPEED = 2.4f;
    private static final long STEP_RIGHT_MS = 240L;
    private static final long STEP_LEFT_MS = 920L;

    private final ModelPart ALL2;
    private final ModelPart ALL;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart left_Ear;
    private final ModelPart right_Ear;
    private final ModelPart left_Eyeball;
    private final ModelPart right_Eyeball;
    private final ModelPart tong;
    private final ModelPart tail;
    private final ModelPart left_arm;
    private final ModelPart left_leg;
    private final ModelPart right_leg;
    private final ModelPart right_arm;

    private float prevLimbSwing = 0f;

    public RedPandaModel(ModelPart root) {
        this.ALL2 = root.getChild("ALL2");
        this.ALL = this.ALL2.getChild("ALL");
        this.body = this.ALL.getChild("body");
        this.head = this.body.getChild("head");
        this.left_Ear = this.head.getChild("left_Ear");
        this.right_Ear = this.head.getChild("right_Ear");
        this.left_Eyeball = this.head.getChild("left_Eyeball");
        this.right_Eyeball = this.head.getChild("right_Eyeball");
        this.tong = this.head.getChild("tong");
        this.tail = this.body.getChild("tail");
        this.left_arm = this.ALL.getChild("left_arm");
        this.left_leg = this.ALL.getChild("left_leg");
        this.right_leg = this.ALL.getChild("right_leg");
        this.right_arm = this.ALL.getChild("right_arm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 17.0F, 0.0F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -4.0F, -6.0F, 7.0F, 7.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.5F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(34, 20).addBox(-4.0F, -3.0F, -5.0F, 8.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
        .texOffs(38, 7).addBox(-2.0F, 0.0F, -7.0F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.5F, -6.0F));

        PartDefinition left_Ear = head.addOrReplaceChild("left_Ear", CubeListBuilder.create().texOffs(38, 12).mirror().addBox(-1.5F, -1.5F, -0.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(3.5F, -2.5F, -1.5F));

        PartDefinition right_Ear = head.addOrReplaceChild("right_Ear", CubeListBuilder.create().texOffs(38, 12).addBox(-1.5F, -1.5F, -0.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.5F, -2.5F, -1.5F));

        PartDefinition left_Eyeball = head.addOrReplaceChild("left_Eyeball", CubeListBuilder.create().texOffs(0, 30).addBox(-1.0F, -0.5F, -0.025F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.offset(3.0F, -0.5F, -5.0F));

        PartDefinition right_Eyeball = head.addOrReplaceChild("right_Eyeball", CubeListBuilder.create().texOffs(0, 30).mirror().addBox(-1.0F, -0.5F, -0.025F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(-3.0F, -0.5F, -5.0F));

        PartDefinition tong = head.addOrReplaceChild("tong", CubeListBuilder.create().texOffs(59, 44).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 2.0F, -5.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 20).addBox(-3.0F, -2.0F, 0.0F, 6.0F, 5.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 6.0F));

        PartDefinition left_arm = ALL.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(14, 36).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(1.75F, 3.5F, -3.5F));

        PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(34, 31).addBox(-1.5F, -0.5F, -2.0F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.75F, 3.5F, 3.0F));

        PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(34, 31).mirror().addBox(-1.5F, -0.5F, -2.0F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.75F, 3.5F, 3.0F));

        PartDefinition right_arm = ALL.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(14, 36).mirror().addBox(-1.5F, -0.5F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.75F, 3.5F, -3.5F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    /**
     * Le panda roux n'enchaîne rien : il n'a ni combo ni animation de frappe. Renvoyer {@code null}
     * suffit à neutraliser {@code animateCombos}, qui saute toute frappe sans définition.
     */
    @Override
    protected AnimationDefinition comboAnimation(int index) {
        return null;
    }

    @Override
    public void setupAnim(T redPanda, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Perchée, la bête ne suit pas le regard de son porteur : elle regarde droit devant elle.
        //
        // L'écart tête/corps dont dépend cette rotation est recopié du porteur à chaque tick, et le
        // corps d'un joueur ne suit son regard que par à-coups — il reste immobile jusqu'à
        // quarante-cinq degrés puis rattrape d'un bloc. La tête du panda encaissait ces sauts en
        // plein champ de vision, ce qui se lisait comme une animation qui bégaie. L'oisiveté suffit
        // à la faire vivre.
        if (!redPanda.isOnShoulder()) this.applyHeadRotation(netHeadYaw, headPitch);

        if (redPanda.isClimbing()) {
            this.animate(redPanda.idleAnimationState, RedPandaAnimations.MISC_IDLE, ageInTicks, 0.4f);
            animateClimb(redPanda, ageInTicks);
            this.prevLimbSwing = limbSwing;
            return;
        }

        if (redPanda.isOnShoulder()) {
            this.animate(redPanda.idleAnimationState, RedPandaAnimations.MISC_IDLE, ageInTicks, 0.6f);
            animateShoulderPerch(ageInTicks);
            animateGestures(redPanda, ageInTicks);
            this.prevLimbSwing = limbSwing;
            return;
        }

        if (redPanda.isNapping() || redPanda.isSleeping()) {
            this.animate(redPanda.napAnimationState, RedPandaAnimations.MISC_IDLE, ageInTicks, 0.35f);
            animateCurledUp(ageInTicks);
            this.prevLimbSwing = limbSwing;
            return;
        }

        if (redPanda.isSitting()) {
            this.animate(redPanda.sittingAnimationState, RedPandaAnimations.MISC_IDLE, ageInTicks, 0.7f);
            animateSit();
            animateGestures(redPanda, ageInTicks);
            this.prevLimbSwing = limbSwing;
            return;
        }

        this.animate(redPanda.idleAnimationState, RedPandaAnimations.MISC_IDLE, ageInTicks, 1.0f);
        this.animateWalk(RedPandaAnimations.MOVE_WALK, limbSwing, limbSwingAmount, WALK_SPEED, WALK_SPEED * 1.2f);

        if (walkAnimCrossed(RedPandaAnimations.MOVE_WALK, limbSwing, WALK_SPEED, STEP_RIGHT_MS)) redPanda.onRightFootDown();
        if (walkAnimCrossed(RedPandaAnimations.MOVE_WALK, limbSwing, WALK_SPEED, STEP_LEFT_MS)) redPanda.onLeftFootDown();

        animateGestures(redPanda, ageInTicks);

        this.prevLimbSwing = limbSwing;
    }

    /**
     * Les deux gestes d'attaque sont écrits à la main : l'export Blockbench ne fournit que le repos
     * et la marche. Ils s'ADDITIONNENT à la pose en cours plutôt que de la remplacer, ce qui les rend
     * lisibles aussi bien depuis l'épaule qu'assis au sol.
     */
    private void animateGestures(T redPanda, float ageInTicks) {
        if (redPanda.getThrowTimer() > 0) animateOrbThrow(redPanda, ageInTicks);
        if (redPanda.isAuraActive()) animateLifeAura(redPanda, ageInTicks);
    }

    /**
     * Trois temps en douze ticks : la patte part en arrière, fouette vers l'avant au tiers du geste
     * — l'instant où l'orbe quitte la main —, puis retombe.
     */
    private void animateOrbThrow(T redPanda, float ageInTicks) {
        float total = OWAttacksConstants.RedPanda.HEAL_ORB_THROW_TICKS;
        float partial = Mth.clamp(ageInTicks - redPanda.tickCount, 0f, 1f);
        float remaining = Math.max(0f, redPanda.getThrowTimer() - partial);
        float progress = Mth.clamp(1f - remaining / total, 0f, 1f);

        float swing;
        if (progress < 0.35f) {
            swing = -1.35f * (progress / 0.35f);
        } else if (progress < 0.6f) {
            swing = Mth.lerp((progress - 0.35f) / 0.25f, -1.35f, 0.95f);
        } else {
            swing = 0.95f * (1f - (progress - 0.6f) / 0.4f);
        }

        this.right_arm.xRot += swing;
        this.right_arm.zRot += -0.25f * Math.abs(swing);
        this.left_arm.xRot += swing * 0.35f;

        this.body.xRot += -0.10f * swing;
        this.head.xRot += -0.18f * swing;
        this.tail.xRot += 0.22f * Math.abs(swing);

        float earFlick = Mth.sin(progress * Mth.PI) * 0.35f;
        this.left_Ear.zRot += earFlick;
        this.right_Ear.zRot -= earFlick;
    }

    /**
     * Pendant l'aura, le panda roux se dresse et tient les deux pattes levées : il ne frappe pas,
     * il porte quelque chose au-dessus de lui. La respiration lente distingue la posture d'un simple
     * geste de lancer.
     */
    private void animateLifeAura(T redPanda, float ageInTicks) {
        float breathe = Mth.sin(ageInTicks * 0.35f);

        this.ALL.xRot += -0.30f;
        this.ALL.y += -1.2f - 0.4f * breathe;

        this.left_arm.xRot += -2.15f + 0.12f * breathe;
        this.left_arm.zRot += -0.30f;
        this.right_arm.xRot += -2.15f - 0.12f * breathe;
        this.right_arm.zRot += 0.30f;

        this.head.xRot += -0.45f + 0.08f * breathe;
        this.body.xRot += -0.18f;
        this.tail.xRot += 0.35f + 0.10f * breathe;

        this.left_Ear.zRot += 0.20f;
        this.right_Ear.zRot += -0.20f;
    }

    /**
     * Pose d'escalade : la bete se dresse et brasse des quatre pattes.
     *
     * <p>Les membres avancent par paires croisees, comme un vrai grimpeur, et non tous ensemble. Le
     * redressement suit l'avancement du trajet plutot qu'une valeur fixe : elle part a plat ventre,
     * se cabre au plus fort de la montee, puis se remet d'aplomb en arrivant sur l'epaule — sans
     * quoi la derniere image aurait juxtapose une bete cabree et une bete assise.</p>
     */
    private void animateClimb(T redPanda, float ageInTicks) {
        float progress = redPanda.climbProgress();
        // Cloche : nulle au depart, pleine a mi-parcours, nulle a l'arrivee.
        float effort = Mth.sin(progress * Mth.PI);
        float reach = Mth.sin(ageInTicks * 1.15f);

        this.ALL.xRot += -0.85f * effort;
        this.ALL.y += -1.2f * effort;

        this.left_arm.xRot += -1.15f + 0.75f * reach;
        this.right_arm.xRot += -1.15f - 0.75f * reach;
        this.left_leg.xRot += -0.55f - 0.55f * reach;
        this.right_leg.xRot += -0.55f + 0.55f * reach;

        // Queue relevee en balancier : c'est elle qui vend l'equilibre precaire.
        this.tail.xRot += -0.65f * effort;
        this.tail.yRot += 0.22f * reach;

        this.head.xRot += 0.45f * effort;
        this.left_Ear.zRot += 0.18f;
        this.right_Ear.zRot += -0.18f;
    }

    /** Assise compacte sur l'épaule : pattes repliées, queue enroulée le long du dos du porteur. */
    private void animateShoulderPerch(float ageInTicks) {
        float sway = Mth.sin(ageInTicks * 0.12f);

        this.ALL.y += 1.4f;
        this.ALL.xRot += 0.12f;
        this.ALL.zRot += 0.05f * sway;

        this.left_leg.xRot += -1.35f;
        this.right_leg.xRot += -1.35f;
        this.left_leg.z += -1.0f;
        this.right_leg.z += -1.0f;

        this.left_arm.xRot += -0.55f;
        this.right_arm.xRot += -0.55f;

        this.tail.xRot += 0.75f + 0.06f * sway;
        this.tail.yRot += 0.28f;
    }

    /** Position assise au sol : arrière-train posé, avant-train redressé. */
    private void animateSit() {
        this.ALL.xRot += -0.35f;
        this.ALL.y += 1.8f;

        this.left_leg.xRot += -1.15f;
        this.right_leg.xRot += -1.15f;
        this.left_arm.xRot += 0.20f;
        this.right_arm.xRot += 0.20f;

        this.tail.xRot += 0.55f;
        this.head.xRot += 0.18f;
    }

    /** Sommeil : la bête se roule en boule et se couvre du panache de sa queue. */
    private void animateCurledUp(float ageInTicks) {
        float breathe = Mth.sin(ageInTicks * 0.10f);

        this.ALL.y += 3.0f;
        this.ALL.xRot += 0.10f;

        this.head.xRot += 0.85f;
        this.head.yRot += 0.55f;
        this.head.y += 1.2f;

        this.left_leg.xRot += -1.55f;
        this.right_leg.xRot += -1.55f;
        this.left_arm.xRot += -1.45f;
        this.right_arm.xRot += -1.45f;

        this.tail.xRot += 0.30f + 0.03f * breathe;
        this.tail.yRot += 1.15f;

        this.left_Ear.zRot += 0.35f;
        this.right_Ear.zRot += -0.35f;
    }

    private boolean walkAnimCrossed(AnimationDefinition animation, float limbSwing, float speedScale, long triggerTimeMs) {
        long durationMs = (long) (animation.lengthInSeconds() * 1000f);
        if (durationMs <= 0) return false;

        long cur = ((long) (limbSwing * 50f * speedScale)) % durationMs;
        long prev = ((long) (prevLimbSwing * 50f * speedScale)) % durationMs;

        if (prev <= cur) return prev < triggerTimeMs && cur >= triggerTimeMs;
        return triggerTimeMs <= cur || triggerTimeMs > prev;
    }

    private void applyHeadRotation(float netHeadYaw, float headPitch) {
        netHeadYaw = Mth.clamp(netHeadYaw, -35.0F, 35.0F);
        headPitch = Mth.clamp(headPitch, -30.0F, 30.0F);

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
    }

    public void renderGeometryOnly(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        renderGeometryOnly(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root() {
        return this.ALL2;
    }

    public void copyPoseFrom(RedPandaModel<?> src) {
        this.ALL2.copyFrom(src.ALL2);
        this.ALL.copyFrom(src.ALL);
        this.body.copyFrom(src.body);
        this.head.copyFrom(src.head);
        this.left_Ear.copyFrom(src.left_Ear);
        this.right_Ear.copyFrom(src.right_Ear);
        this.left_Eyeball.copyFrom(src.left_Eyeball);
        this.right_Eyeball.copyFrom(src.right_Eyeball);
        this.tong.copyFrom(src.tong);
        this.tail.copyFrom(src.tail);
        this.left_arm.copyFrom(src.left_arm);
        this.left_leg.copyFrom(src.left_leg);
        this.right_leg.copyFrom(src.right_leg);
        this.right_arm.copyFrom(src.right_arm);
    }
}
