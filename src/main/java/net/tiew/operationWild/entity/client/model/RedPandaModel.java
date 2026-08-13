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

    private static final float WALK_SPEED = 3.5f;
    private static final long STEP_RIGHT_MS = 240L;
    private static final long STEP_LEFT_MS = 920L;
    private static final float PAW_TRAIL = 0.045f;

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

    /**
     * Vrai le temps d'un apercu d'interface.
     *
     * <p>L'inventaire dessine la VRAIE bete, celle qui se tient sur l'epaule : elle s'y montrait donc
     * repliee en boule, pattes rentrees, comme si le cadre etait pose sur le joueur. Le drapeau lui
     * fait ignorer son perchoir le temps du portrait — meme procede que le boa et le crocodile, qui
     * deplient leur corps entier pour la meme raison.</p>
     */
    public static boolean RENDER_AS_GROUNDED = false;

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

    @Override
    protected AnimationDefinition comboAnimation(int index) {
        return null;
    }

    @Override
    public void setupAnim(T redPanda, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Portrait d'interface : la bete est immobile quoi qu'elle fasse dans le monde. Sans cela
        // l'apercu heriterait du balancement du porteur qui court, et la vignette galoperait sur
        // place dans son cadre.
        if (RENDER_AS_GROUNDED) limbSwingAmount = 0f;

        // Le portrait d'inventaire, lui, doit suivre la souris : c'est tout l'interet de la vignette,
        // et la bete y est immobile, donc sans les a-coups qui avaient fait couper cette rotation.
        if (!redPanda.isOnShoulder() || RENDER_AS_GROUNDED) this.applyHeadRotation(netHeadYaw, headPitch);

        if (redPanda.isClimbing() && !RENDER_AS_GROUNDED) {
            this.animate(redPanda.idleAnimationState, RedPandaAnimations.MISC_IDLE, ageInTicks, 0.4f);
            animateClimb(redPanda, ageInTicks);
            this.prevLimbSwing = limbSwing;
            return;
        }

        if (redPanda.isOnShoulder() && !RENDER_AS_GROUNDED) {
            this.animate(redPanda.shoulderIdleAnimationState, RedPandaAnimations.SHOULDER_IDLE, ageInTicks, 1.0f);
            animatePerchPhysics(redPanda, ageInTicks);
            animateGestures(redPanda, ageInTicks);
            this.prevLimbSwing = limbSwing;
            return;
        }

        if (redPanda.transitionIdleSit.isStarted()) {
            this.animate(redPanda.transitionIdleSit, RedPandaAnimations.TRANSITION_IDLE_SIT, ageInTicks, 1.0f);
            this.prevLimbSwing = limbSwing;
            return;
        }

        if (redPanda.transitionSitIdle.isStarted()) {
            this.animate(redPanda.transitionSitIdle, RedPandaAnimations.TRANSITION_SIT_IDLE, ageInTicks, 1.0f);
            this.prevLimbSwing = limbSwing;
            return;
        }

        if (redPanda.transitionIdleSleep.isStarted()) {
            this.animate(redPanda.transitionIdleSleep, RedPandaAnimations.TRANSITION_IDLE_NAP, ageInTicks, 1.0f);
            this.prevLimbSwing = limbSwing;
            return;
        }

        if (redPanda.transitionSleepIdle.isStarted()) {
            this.animate(redPanda.transitionSleepIdle, RedPandaAnimations.TRANSITION_NAP_IDLE, ageInTicks, 1.0f);
            this.prevLimbSwing = limbSwing;
            return;
        }

        if (redPanda.isNapping() || redPanda.isSleeping()) {
            this.animate(redPanda.napAnimationState, RedPandaAnimations.NAP, ageInTicks, 1.0f);
            this.prevLimbSwing = limbSwing;
            return;
        }

        if (redPanda.isSitting()) {
            this.animate(redPanda.sittingAnimationState, RedPandaAnimations.SIT, ageInTicks, 1.0f);
            animateGestures(redPanda, ageInTicks);
            this.prevLimbSwing = limbSwing;
            return;
        }

        this.animate(redPanda.idleAnimationState, RedPandaAnimations.MISC_IDLE, ageInTicks, 1.0f);
        this.animateWalk(RedPandaAnimations.MOVE_WALK, limbSwing, limbSwingAmount, WALK_SPEED, WALK_SPEED * 1.2f);

        // Les bruits de pas sont pilotes par le RENDU : un portrait d'inventaire les declencherait
        // aussi, et l'ecran se mettrait a marteler le sol.
        if (!RENDER_AS_GROUNDED) {
            if (walkAnimCrossed(RedPandaAnimations.MOVE_WALK, limbSwing, WALK_SPEED, STEP_RIGHT_MS)) redPanda.onRightFootDown();
            if (walkAnimCrossed(RedPandaAnimations.MOVE_WALK, limbSwing, WALK_SPEED, STEP_LEFT_MS)) redPanda.onLeftFootDown();
        }

        animateGestures(redPanda, ageInTicks);

        this.prevLimbSwing = limbSwing;
    }

    private void animateGestures(T redPanda, float ageInTicks) {
        if (redPanda.throwAnimationState.isStarted()) animateOrbThrow(redPanda, ageInTicks);
    }

    private void animateOrbThrow(T redPanda, float ageInTicks) {
        // L'horloge du geste est celle du RENDU, pas le compte a rebours synchronise. Ce dernier
        // n'arrive pas une fois par tick : il reste fige une image puis saute de deux, et le geste
        // se lisait donc par a-coups. L'etat d'animation, lui, ne retient que l'instant du depart
        // et mesure la suite sur {@code ageInTicks}, qui porte la fraction d'image.
        redPanda.throwAnimationState.updateTime(ageInTicks, 1f);

        float total = OWAttacksConstants.RedPanda.HEAL_ORB_THROW_TICKS;
        float elapsed = redPanda.throwAnimationState.getAccumulatedTime() / 50f;
        float progress = Mth.clamp(elapsed / total, 0f, 1f);
        float trailing = Math.max(0f, progress - PAW_TRAIL);

        float rise = band(progress, 0f, 0.18f) - band(progress, 0.80f, 1f);
        float cradle = band(progress, 0.10f, 0.30f) - band(progress, 0.62f, 0.76f);
        float hold = band(progress, 0.38f, 0.48f) - band(progress, 0.48f, 0.56f);
        float launch = band(progress, 0.48f, 0.62f) - band(progress, 0.62f, 0.86f);
        float watch = band(progress, 0.62f, 0.72f) - band(progress, 0.86f, 1f);
        float settle = band(progress, 0.84f, 0.92f) - band(progress, 0.92f, 1f);
        float launchTrail = band(trailing, 0.48f, 0.62f) - band(trailing, 0.62f, 0.86f);

        float knead = Mth.cos(ageInTicks * 0.9f) * 0.10f * cradle;
        float cup = Mth.sin(ageInTicks * 0.9f) * 0.08f * cradle;
        float tremble = Mth.sin(ageInTicks * 2.6f) * 0.04f * hold;

        this.ALL.xRot += -0.42f * rise + 0.16f * hold - 0.34f * launch + 0.12f * settle;
        this.ALL.y += -1.3f * rise + 0.8f * hold - 1.5f * launch + 0.9f * settle;

        this.left_leg.xRot += 0.42f * rise + 0.34f * launch;
        this.right_leg.xRot += 0.42f * rise + 0.34f * launch;
        this.left_leg.zRot += -0.13f * rise;
        this.right_leg.zRot += 0.13f * rise;

        this.left_arm.xRot += -0.78f * cradle + 0.42f * hold - 1.05f * launchTrail
                + 0.30f * watch + 0.26f * settle + knead;
        this.right_arm.xRot += -0.78f * cradle + 0.42f * hold - 1.05f * launch
                + 0.30f * watch + 0.26f * settle - knead;
        this.left_arm.zRot += 0.36f * cradle - 0.30f * launchTrail + cup - tremble;
        this.right_arm.zRot += -0.36f * cradle + 0.30f * launch + cup + tremble;

        this.head.xRot += 0.10f * rise + 0.40f * cradle + 0.18f * hold
                - 0.70f * launch - 0.22f * watch;
        this.head.zRot += 0.16f * cradle - 0.16f * launch;

        this.tail.xRot += -0.42f * rise + 0.22f * hold + 0.70f * launch - 0.20f * settle;
        this.tail.yRot += Mth.sin(ageInTicks * 0.7f) * 0.18f * cradle;

        this.left_Ear.xRot += -0.14f * rise - 0.12f * watch
                + Mth.sin(ageInTicks * 0.5f) * 0.12f * cradle;
        this.right_Ear.xRot += -0.14f * rise - 0.12f * watch;
        this.left_Ear.zRot += 0.26f * launch;
        this.right_Ear.zRot += -0.26f * launch;
        this.tong.xRot += -0.22f * cradle - 0.30f * launch;

        this.body.yScale *= 1f - 0.10f * cradle - 0.16f * hold + 0.16f * launch - 0.08f * settle;
        this.body.xScale *= 1f + 0.07f * cradle + 0.12f * hold - 0.10f * launch + 0.06f * settle;
        this.body.zScale *= 1f + 0.07f * cradle + 0.12f * hold - 0.10f * launch + 0.06f * settle;

        this.head.yScale *= 1f - 0.09f * hold + 0.09f * launch;
        this.head.xScale *= 1f + 0.07f * hold - 0.05f * launch;
        this.head.zScale *= 1f + 0.07f * hold - 0.05f * launch;
    }

    private static float band(float value, float from, float to) {
        if (value <= from) return 0f;
        if (value >= to) return 1f;
        float t = (value - from) / (to - from);
        return t * t * (3f - 2f * t);
    }

    private void animateClimb(T redPanda, float ageInTicks) {
        float progress = redPanda.climbProgress();
        float pitch = redPanda.climbPitch() * Mth.DEG_TO_RAD;
        float effort = Mth.sin(progress * Mth.PI);
        float reach = Mth.sin(ageInTicks * 1.35f);

        this.ALL.xRot += -pitch * 0.85f;
        this.ALL.y += -1.4f * effort;
        this.ALL.zRot += 0.22f * effort;

        this.left_arm.xRot += -1.25f + 0.85f * reach * effort;
        this.right_arm.xRot += -1.25f - 0.85f * reach * effort;
        this.left_leg.xRot += -0.60f - 0.60f * reach * effort;
        this.right_leg.xRot += -0.60f + 0.60f * reach * effort;

        this.left_arm.zRot += -0.30f * effort;
        this.right_arm.zRot += 0.30f * effort;

        this.tail.xRot += pitch * 0.55f - 0.30f * effort;
        this.tail.yRot += 0.28f * reach * effort;

        this.head.xRot += pitch * 0.60f;
        this.left_Ear.zRot += 0.22f * effort;
        this.right_Ear.zRot += -0.22f * effort;

        float gather = band(progress, 0f, 0.13f) - band(progress, 0.13f, 0.32f);
        float arrive = band(progress, 0.80f, 0.93f) - band(progress, 0.93f, 1f);
        float squash = gather + arrive;

        this.body.yScale *= 1f - 0.21f * squash + 0.14f * effort;
        this.body.xScale *= 1f + 0.15f * squash - 0.07f * effort;
        this.body.zScale *= 1f + 0.15f * squash - 0.07f * effort;

        this.head.yScale *= 1f - 0.10f * squash + 0.07f * effort;
        this.head.xScale *= 1f + 0.08f * squash - 0.04f * effort;
        this.head.zScale *= 1f + 0.08f * squash - 0.04f * effort;

        this.ALL.y += 1.5f * squash;
    }

    /**
     * Physique du perchoir : ce que la bete subit du porteur, par-dessus son animation de repos.
     *
     * <p>Trois ressorts amortis, tous nourris par le mouvement du joueur et jamais par une horloge.
     * C'est ce qui les distingue de l'animation elle-meme : celle-ci deroule un cycle immuable, eux
     * repondent a ce qui arrive. Un cycle ne saura jamais qu'on vient de sauter.</p>
     *
     * <p>L'integration se fait sur l'ecart d'age reel et non sur l'age absolu : a plusieurs milliers
     * de ticks, la moindre variation de cadence ferait sauter la phase d'un tour entier.</p>
     */
    private void animatePerchPhysics(T redPanda, float ageInTicks) {
        float dt = Float.isNaN(redPanda.tailLastAge) ? 0f
                : Mth.clamp(ageInTicks - redPanda.tailLastAge, 0f, 2f);
        redPanda.tailLastAge = ageInTicks;

        net.minecraft.world.entity.player.Player carrier = redPanda.getCarrier();

        // ── Virage ────────────────────────────────────────────────────────────
        // Le lacet est mesure D'UNE IMAGE A L'AUTRE, et pas pris sur le couple (yRotO, yRot) du
        // moteur. {@code Entity.turn} ajoute le mouvement de souris aux DEUX : leur difference reste
        // constante pendant qu'on tourne le regard, et la queue ne bougeait donc jamais.
        float carrierYaw = carrier != null ? carrier.getYRot() : redPanda.yBodyRot;
        float yawDelta = Float.isNaN(redPanda.perchLastCarrierYaw)
                ? 0f : Mth.wrapDegrees(carrierYaw - redPanda.perchLastCarrierYaw);
        redPanda.perchLastCarrierYaw = carrierYaw;

        // Ramene en degres PAR TICK : sans cette division, la meme rotation produirait un effet
        // trois fois moindre a soixante images par seconde qu'a vingt.
        float instantRate = dt > 1.0e-4f ? yawDelta / dt : 0f;
        redPanda.perchTurnRate += (instantRate - redPanda.perchTurnRate)
                * Math.min(1f, dt * PERCH_TURN_SMOOTHING);

        float turnRate = redPanda.perchTurnRate;
        float swingTarget = Mth.clamp(-turnRate * TAIL_SWING_GAIN, -TAIL_SWING_MAX, TAIL_SWING_MAX);

        // ── Roulis ────────────────────────────────────────────────────────────
        // Meme facture que le devers de l'elephant, de l'orque et du crocodile : le taux est
        // normalise contre une reference puis mis en forme par une puissance, ce qui donne une
        // courbe qui ignore les micro-corrections et se cabre nettement sur un vrai virage. La
        // reponse est asymetrique — on s'incline vite, on se redresse doucement.
        float normalized = Mth.clamp(Math.abs(turnRate) / PERCH_ROLL_REFERENCE, 0f, 1f);
        float shaped = (float) Math.pow(normalized, PERCH_ROLL_SHARPNESS);
        float rollTarget = -Math.signum(turnRate) * shaped * PERCH_ROLL_MAX;

        float rollResponse = Math.abs(rollTarget) > Math.abs(redPanda.perchRoll)
                ? PERCH_ROLL_RISE : PERCH_ROLL_FALL;
        redPanda.perchRoll += (rollTarget - redPanda.perchRoll) * Math.min(1f, rollResponse * dt);

        redPanda.tailSwingVelocity += (swingTarget - redPanda.tailSwing) * TAIL_STIFFNESS * dt;
        redPanda.tailSwingVelocity *= (float) Math.pow(TAIL_DAMPING, dt);
        redPanda.tailSwing += redPanda.tailSwingVelocity * dt;

        // ── Saut et chute ─────────────────────────────────────────────────────
        // La VALEUR ABSOLUE de la vitesse verticale, et non son signe : monter et tomber cabrent tous
        // deux la bete, qui se ramasse sur ses appuis. Un lean signe l'aurait fait piquer du nez en
        // chute libre, ce qui se lit comme un plongeon volontaire plutot que comme une secousse subie.
        boolean airborne = carrier != null && !carrier.onGround();
        double verticalSpeed = carrier != null ? carrier.getDeltaMovement().y : 0;
        float pitchTarget = airborne
                ? Mth.clamp((float) Math.abs(verticalSpeed) * PERCH_PITCH_GAIN, 0f, PERCH_PITCH_MAX)
                : 0f;

        redPanda.perchPitchVelocity += (pitchTarget - redPanda.perchPitch) * PERCH_PITCH_STIFFNESS * dt;
        redPanda.perchPitchVelocity *= (float) Math.pow(PERCH_PITCH_DAMPING, dt);
        redPanda.perchPitch += redPanda.perchPitchVelocity * dt;

        // ── Reception ─────────────────────────────────────────────────────────
        // L'ecrasement est declenche par la TRANSITION vers le sol, pas par le contact : teste a
        // chaque image, il se redeclencherait tant que le joueur reste pose.
        //
        // Et c'est une IMPULSION dans un ressort, non une valeur posee. Passer de zero a un en une
        // image cassait net la pose au moment meme de l'atterrissage : la bete changeait de forme
        // entre deux images, ce qui se lit comme une animation qui saute. En poussant la VITESSE du
        // ressort, l'ecrasement s'installe en deux ou trois images, rebondit et se resorbe.
        if (redPanda.perchWasAirborne && !airborne) {
            float impact = Mth.clamp(Math.abs(redPanda.perchLastVerticalSpeed) / PERCH_IMPACT_REFERENCE, 0f, 1f);
            redPanda.perchSquashVelocity += PERCH_SQUASH_IMPULSE * impact;
        }

        // Au decollage, la meme impulsion EN NEGATIF : la bete s'etire d'un coup vers le haut. Le
        // ressort etant le meme, l'etirement rebondit ensuite en ecrasement puis se resorbe, ce qui
        // donne le tremblotement de gelatine sans qu'aucune courbe ait a le decrire.
        if (!redPanda.perchWasAirborne && airborne && verticalSpeed > PERCH_LEAP_SPEED) {
            redPanda.perchSquashVelocity -= PERCH_LEAP_IMPULSE;
        }

        redPanda.perchWasAirborne = airborne;
        if (airborne) redPanda.perchLastVerticalSpeed = (float) verticalSpeed;

        redPanda.perchSquashVelocity += (0f - redPanda.perchLandSquash) * PERCH_SQUASH_STIFFNESS * dt;
        redPanda.perchSquashVelocity *= (float) Math.pow(PERCH_SQUASH_DAMPING, dt);
        redPanda.perchLandSquash += redPanda.perchSquashVelocity * dt;

        // La borne basse est NEGATIVE : c'est elle qui autorise le rebond. Bloquee a zero, la
        // reception s'ecrasait puis remontait pile a sa taille, sans le depassement qui fait tout
        // l'interet de la chose.
        redPanda.perchLandSquash = Mth.clamp(redPanda.perchLandSquash, PERCH_STRETCH_LIMIT, 1f);

        // ── Allure ────────────────────────────────────────────────────────────
        // Au repos la queue pend le long du dos ; en course elle se leve presque a l'horizontale,
        // comme celle de toute bete qui se sert de sa queue pour s'equilibrer. La poursuite est
        // molle pour que le passage de la marche au sprint se lise comme une prise d'elan.
        double pace = carrier != null ? carrier.getDeltaMovement().horizontalDistance() : 0;
        float paceTarget = Mth.clamp((float) pace / TAIL_RUN_REFERENCE, 0f, 1f) * TAIL_RUN_LIFT;
        redPanda.perchRunLift += (paceTarget - redPanda.perchRunLift) * Math.min(1f, TAIL_RUN_RESPONSE * dt);

        float pitchRad = redPanda.perchPitch * Mth.DEG_TO_RAD;
        float squash = redPanda.perchLandSquash;

        // Cabrage : l'axe des X abaisse le museau, le cabrage est donc negatif.
        this.ALL.xRot += -pitchRad;
        this.ALL.zRot += redPanda.perchRoll * Mth.DEG_TO_RAD;
        this.ALL.y += 2.8f * squash;

        // Ecrasement puis etirement : le volume se conserve a l'oeil, la bete s'aplatit en
        // s'elargissant. L'elargissement suit la compression de pres — sans lui, la bete maigrirait
        // au lieu de s'ecraser, ce qui se lit comme un defaut d'echelle et non comme un impact.
        //
        // Le terme d'air etire dans l'autre sens, et il pompe DEUX FOIS par saut : il suit la
        // vitesse verticale en valeur absolue, qui culmine a la montee, retombe a zero au sommet et
        // reprend a la descente.
        float air = Mth.clamp(redPanda.perchPitch / PERCH_PITCH_MAX, 0f, 1f);

        this.body.yScale *= 1f - 0.34f * squash + 0.20f * air;
        this.body.xScale *= 1f + 0.23f * squash - 0.10f * air;
        this.body.zScale *= 1f + 0.23f * squash - 0.10f * air;

        this.head.yScale *= 1f - 0.11f * squash + 0.08f * air;
        this.head.xScale *= 1f + 0.08f * squash - 0.04f * air;
        this.head.zScale *= 1f + 0.08f * squash - 0.04f * air;

        // La queue prend tout : l'allure, le virage, le cabrage et le contrecoup de la reception.
        //
        // Le balancement la fait aussi MONTER, proportionnellement a son ampleur. C'est ce qui la
        // sortait du corps du porteur : un balayage purement horizontal la faisait passer au travers
        // en fin de course. Une vraie queue decrit un arc — elle se leve en s'ecartant —, et le
        // roulis suit le meme mouvement pour que la fourrure se vrille au lieu de rester a plat.
        float swingRad = redPanda.tailSwing * Mth.DEG_TO_RAD;
        float swingArc = Math.abs(redPanda.tailSwing) * TAIL_SWING_ARC * Mth.DEG_TO_RAD;

        this.tail.xRot += redPanda.perchRunLift * Mth.DEG_TO_RAD + swingArc
                - pitchRad * 0.75f + 0.48f * squash;
        this.tail.yRot += swingRad;
        this.tail.zRot += swingRad * TAIL_SWING_TWIST;

        // Oreilles rabattues par le vent de la chute, puis un sursaut a l'atterrissage.
        float earFlap = -pitchRad * 0.55f + 0.44f * squash;
        this.left_Ear.xRot += earFlap;
        this.right_Ear.xRot += earFlap;

        // La tete resiste au cabrage : elle cherche a rester d'aplomb, comme tout animal qui tombe.
        this.head.xRot += pitchRad * 0.45f;
    }

    /**
     * Degres de balancement par degre de rotation du regard, et butee.
     *
     * <p>Le ressort est volontairement mou : une raideur elevee ramenerait la queue trop vite et
     * mangerait l'amplitude. C'est le retard qui fait le poids de la fourrure, pas l'angle seul.</p>
     */
    /** Lissage du taux de rotation : assez mou pour ignorer une secousse d'une seule image. */
    private static final float PERCH_TURN_SMOOTHING = 0.55f;

    /**
     * Roulis du corps : degres par tick au-dela desquels il est complet, durete de la courbe, angle
     * maximal, puis vitesses de mise en devers et de redressement.
     */
    private static final float PERCH_ROLL_REFERENCE = 11f;
    private static final float PERCH_ROLL_SHARPNESS = 0.7f;
    private static final float PERCH_ROLL_MAX = 19f;
    private static final float PERCH_ROLL_RISE = 0.35f;
    private static final float PERCH_ROLL_FALL = 0.14f;

    /**
     * Balancement : degres par degre de rotation du regard, butee, puis raideur et amortissement.
     *
     * <p>Volontairement plus mou et moins ample qu'avant. Un ressort vif rend une queue de plastique
     * — elle colle au regard, atteint sa butee et y reste. En ralentissant la reponse tout en
     * gardant l'energie plus longtemps, la fourrure traine, depasse, puis revient : c'est ce retard
     * qui lui donne du poids.</p>
     */
    private static final float TAIL_SWING_GAIN = 6f;
    private static final float TAIL_SWING_MAX = 58f;
    private static final float TAIL_STIFFNESS = 0.26f;
    private static final float TAIL_DAMPING = 0.86f;

    /** Part du balancement reportee en montee et en vrille : l'arc, plutot que le balayage a plat. */
    private static final float TAIL_SWING_ARC = 0.45f;
    private static final float TAIL_SWING_TWIST = 0.65f;

    /** Allure : vitesse a laquelle le relevement est complet, angle gagne, et vitesse de poursuite. */
    private static final float TAIL_RUN_REFERENCE = 0.22f;
    private static final float TAIL_RUN_LIFT = 46f;
    private static final float TAIL_RUN_RESPONSE = 0.12f;

    /** Cabrage : degres par bloc-par-tick de vitesse verticale, butee, puis raideur et amortissement. */
    private static final float PERCH_PITCH_GAIN = 55f;
    private static final float PERCH_PITCH_MAX = 32f;
    private static final float PERCH_PITCH_STIFFNESS = 0.30f;
    private static final float PERCH_PITCH_DAMPING = 0.80f;
    /**
     * Reception : force de l'impulsion, raideur et amortissement du ressort qui la resorbe, et
     * vitesse de chute au-dela de laquelle l'ecrasement est complet.
     *
     * <p>Un simple saut sur place n'ecrase donc presque pas, la ou une chute de plusieurs blocs
     * ramasse franchement la bete — c'est le dosage par la vitesse d'impact qui rend le geste
     * credible, plus que son amplitude.</p>
     */
    private static final float PERCH_SQUASH_IMPULSE = 0.68f;
    private static final float PERCH_SQUASH_STIFFNESS = 0.45f;
    private static final float PERCH_SQUASH_DAMPING = 0.68f;
    private static final float PERCH_IMPACT_REFERENCE = 0.7f;
    private static final float PERCH_LEAP_IMPULSE = 0.34f;
    private static final float PERCH_STRETCH_LIMIT = -0.4f;
    private static final double PERCH_LEAP_SPEED = 0.08;

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
