package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import org.joml.Vector3f;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;
import net.tiew.operationWild.entity.client.animation.KangarooAnimations;

public class KangarooModel<T extends KangarooEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "kangaroo"), "main");

    // Tornade de Poings : on boucle sur la portion « stable » de l'anim ATTACK_SECONDARY
    // (entre l'intro où les bras se lèvent et l'outro où ils se baissent).
    private static final float SPIN_LOOP_START_MS  = 500f;    // 0,5 s
    private static final float SPIN_LOOP_LENGTH_MS = 14000f;  // jusqu'à 14,5 s
    private static final float SPIN_ANIM_END_MS    = 15000f;  // fin de l'anim (pose de repos)
    private static final Vector3f SPIN_ANIM_VEC = new Vector3f();

    private final ModelPart ALL2;
    private final ModelPart ALL;
    private final ModelPart body;
    private final ModelPart body2;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart left_Ear;
    private final ModelPart right_Ear;
    private final ModelPart left_eyeBall;
    private final ModelPart right_eyeBall;
    private final ModelPart left_arm;
    private final ModelPart right_arm;
    private final ModelPart tail;
    private final ModelPart front_tail;
    private final ModelPart back_tail;
    private final ModelPart left_leg;
    private final ModelPart left_leg2;
    private final ModelPart left_leg3;
    private final ModelPart right_leg;
    private final ModelPart right_leg2;
    private final ModelPart right_leg3;

    public KangarooModel(ModelPart root) {
        this.ALL2 = root.getChild("ALL2");
        this.ALL = this.ALL2.getChild("ALL");
        this.body = this.ALL.getChild("body");
        this.body2 = this.body.getChild("body2");
        this.neck = this.body2.getChild("neck");
        this.head = this.neck.getChild("head");
        this.left_Ear = this.head.getChild("left_Ear");
        this.right_Ear = this.head.getChild("right_Ear");
        this.left_eyeBall = this.head.getChild("left_eyeBall");
        this.right_eyeBall = this.head.getChild("right_eyeBall");
        this.left_arm = this.body2.getChild("left_arm");
        this.right_arm = this.body2.getChild("right_arm");
        this.tail = this.body.getChild("tail");
        this.front_tail = this.tail.getChild("front_tail");
        this.back_tail = this.tail.getChild("back_tail");
        this.left_leg = this.ALL.getChild("left_leg");
        this.left_leg2 = this.left_leg.getChild("left_leg2");
        this.left_leg3 = this.left_leg2.getChild("left_leg3");
        this.right_leg = this.ALL.getChild("right_leg");
        this.right_leg2 = this.right_leg.getChild("right_leg2");
        this.right_leg3 = this.right_leg2.getChild("right_leg3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 3.0F, 0.0F));

        PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -5.0F, -7.0F, 12.0F, 12.0F, 15.0F, new CubeDeformation(0.0F))
        .texOffs(82, 113).addBox(-5.0F, -7.0F, -6.0F, 10.0F, 2.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body2 = body.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(48, 49).addBox(-5.0F, -5.0F, -10.0F, 10.0F, 11.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -7.0F));

        PartDefinition neck = body2.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(30, 85).addBox(-2.6F, -14.0F, -2.0F, 5.0F, 16.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, -10.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(50, 85).addBox(-2.0F, -4.0F, -10.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
        .texOffs(0, 74).addBox(-3.5F, -6.0F, -4.0F, 7.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -14.0F, 0.0F));

        PartDefinition left_Ear = head.addOrReplaceChild("left_Ear", CubeListBuilder.create().texOffs(88, 8).mirror().addBox(-1.5F, -7.0F, -0.5F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(3.0F, -5.0F, 2.5F));

        PartDefinition right_Ear = head.addOrReplaceChild("right_Ear", CubeListBuilder.create().texOffs(88, 8).addBox(-1.5F, -7.0F, -0.5F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, -5.0F, 2.5F));

        PartDefinition left_eyeBall = head.addOrReplaceChild("left_eyeBall", CubeListBuilder.create().texOffs(38, 33).addBox(0.1F, -0.5F, -2.0F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.025F))
        .texOffs(41, 37).addBox(-0.9F, -0.5F, -2.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.025F)), PartPose.offset(3.4F, -4.5F, -2.0F));

        PartDefinition right_eyeBall = head.addOrReplaceChild("right_eyeBall", CubeListBuilder.create().texOffs(38, 33).mirror().addBox(-0.1F, -0.5F, -2.0F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.025F)).mirror(false)
        .texOffs(41, 37).mirror().addBox(-0.1F, -0.5F, -2.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.025F)).mirror(false), PartPose.offset(-3.4F, -4.5F, -2.0F));

        PartDefinition left_arm = body2.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(84, 85).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 13.0F, 3.0F, new CubeDeformation(0.0F))
        .texOffs(112, 0).addBox(-2.0F, 6.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(5.5F, 1.0F, -5.5F));

        PartDefinition right_arm = body2.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(84, 85).mirror().addBox(-1.5F, -1.0F, -1.5F, 3.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
        .texOffs(112, 0).mirror().addBox(-2.0F, 6.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-5.5F, 1.0F, -5.5F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, 8.0F));

        PartDefinition front_tail = tail.addOrReplaceChild("front_tail", CubeListBuilder.create().texOffs(0, 27).addBox(-3.5F, -4.0F, 0.0F, 7.0F, 8.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition back_tail = tail.addOrReplaceChild("back_tail", CubeListBuilder.create().texOffs(48, 27).addBox(-2.5F, -3.0F, 0.0F, 5.0F, 5.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 17.0F));

        PartDefinition left_leg = ALL.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(1, 53).addBox(-2.5F, -5.0F, -6.0F, 5.0F, 10.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 4.0F, 0.0F));

        PartDefinition left_leg2 = left_leg.addOrReplaceChild("left_leg2", CubeListBuilder.create().texOffs(34, 52).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, -1.0F));

        PartDefinition left_leg3 = left_leg2.addOrReplaceChild("left_leg3", CubeListBuilder.create().texOffs(34, 70).addBox(-2.0F, 0.0F, -11.0F, 4.0F, 2.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 10.0F, 0.0F));

        PartDefinition right_leg = ALL.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(1, 53).mirror().addBox(-2.5F, -5.0F, -6.0F, 5.0F, 10.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-5.0F, 4.0F, 0.0F));

        PartDefinition right_leg2 = right_leg.addOrReplaceChild("right_leg2", CubeListBuilder.create().texOffs(34, 52).mirror().addBox(-1.5F, 0.0F, -2.0F, 3.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 5.0F, -1.0F));

        PartDefinition right_leg3 = right_leg2.addOrReplaceChild("right_leg3", CubeListBuilder.create().texOffs(34, 70).mirror().addBox(-2.0F, 0.0F, -11.0F, 4.0F, 2.0F, 13.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 10.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(T kangaroo, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.applyHeadRotation(netHeadYaw, headPitch);

        // Combos 1-2 : rendu actif aussi pendant la phase de fin (isStarted mais !isCombo),
        // ce qui laisse le bras revenir au repos naturellement au lieu de snapper.
        if (kangaroo.isCombo(1) || kangaroo.attack1Combo.isStarted()) {
            this.animate(kangaroo.attack1Combo, KangarooAnimations.ATTACK_STRIKE, ageInTicks, 1.0f * OWEntity.comboSpeedMultiplier);
        }
        if (kangaroo.isCombo(2) || kangaroo.attack2Combo.isStarted()) {
            this.animate(kangaroo.attack2Combo, KangarooAnimations.ATTACK_STRIKE_2, ageInTicks, 1.1f * OWEntity.comboSpeedMultiplier);
        }
        if (kangaroo.isCombo(3) || kangaroo.attack3Combo.isStarted()) {
            this.animate(kangaroo.attack3Combo, KangarooAnimations.ATTACK_STRIKE_3, ageInTicks, 1.25f * OWEntity.comboSpeedMultiplier);
        }

        if (kangaroo.isTelluricStomping() || kangaroo.telluricStompAnim.isStarted()) {
            this.animate(kangaroo.telluricStompAnim, KangarooAnimations.TELLURIC_STOMP, ageInTicks, 1.0f);
            captureBodyState(kangaroo);
            return;
        }

        if (kangaroo.transitionIdleSit.isStarted()) {
            this.animate(kangaroo.transitionIdleSit, KangarooAnimations.TRANSITION_IDLE_SIT, ageInTicks, 1.0f);
            captureBodyState(kangaroo);
            return;
        }
        if (kangaroo.transitionSitIdle.isStarted()) {
            this.animate(kangaroo.transitionSitIdle, KangarooAnimations.TRANSITION_SIT_IDLE, ageInTicks, 1.0f);
            captureBodyState(kangaroo);
            return;
        }

        if (kangaroo.isSitting()) {
            this.animate(kangaroo.sittingAnimationState, KangarooAnimations.SIT, ageInTicks, 1.0f);
            captureBodyState(kangaroo);
            return;
        }

        if (kangaroo.isSpinning()) {
            float partial   = ageInTicks - kangaroo.tickCount;
            float timeMs    = kangaroo.clientAnimTimeMs + 50f * kangaroo.clientSpinSpeed * partial;
            long  loopedMs  = (long) (SPIN_LOOP_START_MS + (timeMs % SPIN_LOOP_LENGTH_MS));
            KeyframeAnimations.animate(this, KangarooAnimations.ATTACK_SECONDARY, loopedMs, 1f, SPIN_ANIM_VEC);
            captureBodyState(kangaroo);
            return;
        }

        if (kangaroo.clientOutroTicks > 0) {
            float partial   = ageInTicks - kangaroo.tickCount;
            float remaining = kangaroo.clientOutroTicks - partial;
            float progress  = Mth.clamp(1f - remaining / KangarooEntity.WHIRLWIND_OUTRO_TICKS, 0f, 1f);
            float loopEnd   = SPIN_LOOP_START_MS + SPIN_LOOP_LENGTH_MS;        
            long  outroMs   = (long) (loopEnd + progress * (SPIN_ANIM_END_MS - loopEnd));
            KeyframeAnimations.animate(this, KangarooAnimations.ATTACK_SECONDARY, outroMs, 1f, SPIN_ANIM_VEC);
            captureBodyState(kangaroo);
            return;
        }

        float s = kangaroo.isRunning() ? 1.25f : 2.0f;
        if (!kangaroo.isRunning()) this.animate(kangaroo.idleAnimationState, KangarooAnimations.MISC_IDLE, ageInTicks, 1.0f);
        if (!kangaroo.isCombo(3)) this.animateWalk(KangarooAnimations.MOVE_WALK, limbSwing, limbSwingAmount, s, s * 1.25f);

        captureBodyState(kangaroo);
    }

    /**
     * Capte l'état animé du corps (chaîne complète ALL2 → ALL → body → body2) et le stocke dans
     * l'entité (client only) pour que le rider et la caméra suivent <em>tout</em> le mouvement du corps :
     * <ul>
     *   <li>{@code bodyAnimY} : déplacement vertical → le rider le suit ({@code getRiderAnimYOffset}).
     *       Inclut le bond porté par le bone {@code ALL} et le mouvement du torse {@code body2}.</li>
     *   <li>{@code bodyZRot}/{@code bodyXRot} : tilt du corps → le rider s'incline (branche générique de
     *       {@code ClientEvents.onPlayerRenderPre}) et la caméra tremble ({@code onCameraSetup}).</li>
     * </ul>
     * restPoseYSum = somme des offsets Y au repos (ALL2=3, ALL=0, body=0, body2=0).
     */
    private void captureBodyState(T kangaroo) {
        if (!kangaroo.level().isClientSide()) return;
        float restPoseYSum = 3.0f;
        float ySum = this.ALL2.y + this.ALL.y + this.body.y;
        kangaroo.bodyAnimY = ySum - (restPoseYSum * kangaroo.getScale());

        kangaroo.setBodyZRot((float) Math.toDegrees(this.ALL.zRot + this.body.zRot));
        kangaroo.setBodyXRot((float) -Math.toDegrees(this.ALL.xRot + this.body.xRot));
    }

    /** Renders only the model geometry. Used by all skin/effect layers. */
    public void renderGeometryOnly(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        renderGeometryOnly(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    private void applyHeadRotation(float netHeadYaw, float headPitch) {
        netHeadYaw = Mth.clamp(netHeadYaw, -30.0F, 30.0F);
        headPitch = Mth.clamp(headPitch, -30.0F, 30.0F);

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
    }

    @Override
    public ModelPart root() {
        return this.ALL2;
    }

    /**
     * Copie la pose résolue de chaque os depuis un autre KangarooModel déjà animé (le modèle de base).
     * Utilisé par le skin OVERLAY (gants de boxe) pour suivre parfaitement la base sans désync ni z-fighting.
     */
    public void copyPoseFrom(KangarooModel<?> src) {
        this.ALL2.copyFrom(src.ALL2);
        this.ALL.copyFrom(src.ALL);
        this.body.copyFrom(src.body);
        this.body2.copyFrom(src.body2);
        this.neck.copyFrom(src.neck);
        this.head.copyFrom(src.head);
        this.left_Ear.copyFrom(src.left_Ear);
        this.right_Ear.copyFrom(src.right_Ear);
        this.left_eyeBall.copyFrom(src.left_eyeBall);
        this.right_eyeBall.copyFrom(src.right_eyeBall);
        this.left_arm.copyFrom(src.left_arm);
        this.right_arm.copyFrom(src.right_arm);
        this.tail.copyFrom(src.tail);
        this.front_tail.copyFrom(src.front_tail);
        this.back_tail.copyFrom(src.back_tail);
        this.left_leg.copyFrom(src.left_leg);
        this.left_leg2.copyFrom(src.left_leg2);
        this.left_leg3.copyFrom(src.left_leg3);
        this.right_leg.copyFrom(src.right_leg);
        this.right_leg2.copyFrom(src.right_leg2);
        this.right_leg3.copyFrom(src.right_leg3);
    }
}
