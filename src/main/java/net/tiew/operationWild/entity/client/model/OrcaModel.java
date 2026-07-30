package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;
import net.tiew.operationWild.entity.attacks.OWAttackLogic;
import net.tiew.operationWild.entity.attacks.OWAttacksHandler;
import net.tiew.operationWild.entity.client.animation.CrocodileAnimations;
import net.tiew.operationWild.entity.client.animation.OrcaAnimations;

import javax.swing.text.html.parser.Entity;

public class OrcaModel<T extends OrcaEntity> extends OWComboModel<T> implements OWFlagModel {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "orca_default"), "main");

	/** Texture ne contenant que la hampe du drapeau de tribu (tout le reste est transparent). */
	private static final ResourceLocation FLAG_POLE_TEXTURE =
			ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/orca/orca_flag.png");

	/**
	 * Rectangle de la toile dans l'espace local de l'os {@code flag}, en pixels modele.
	 * Recopie la boite declaree par {@code createBodyLayer} : Y dans [-5.75, 5.25], Z dans [0.5, 19.5].
	 */
	private static final Anchor FLAG_ANCHOR = new Anchor(-5.75f, 11f, 0.5f, 19f);

	private float prevLimbSwing = 0f;

	private static final float FLOP_TAIL_SWING = 0.675f;
	private static final float FLOP_TAIL_SPEED = 0.6f;

	public float externalRiderPitch = 0f;
	public float externalBankRoll = 0f;

	private final ModelPart ALL2;
	private final ModelPart ALL;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart mouth;
	private final ModelPart mouth_down;
	private final ModelPart mouth_up;
	private final ModelPart tail;
	private final ModelPart front_tail;
	private final ModelPart back_tail;
	private final ModelPart left_fan;
	private final ModelPart right_fan;
	/** Nuls sur les definitions de skin qui ne declarent pas le porte-drapeau. */
	private final ModelPart mainFlag;
	private final ModelPart flag;

	public OrcaModel(ModelPart root) {
		this.ALL2 = root.getChild("ALL2");
		this.ALL = this.ALL2.getChild("ALL");
		this.body = this.ALL.getChild("body");
		this.head = this.body.getChild("head");
		this.mouth = this.head.getChild("mouth");
		this.mouth_down = this.mouth.getChild("mouth_down");
		this.mouth_up = this.mouth.getChild("mouth_up");
		this.tail = this.body.getChild("tail");
		this.front_tail = this.tail.getChild("front_tail");
		this.back_tail = this.tail.getChild("back_tail");
		this.left_fan = this.body.getChild("left_fan");
		this.right_fan = this.body.getChild("right_fan");
		this.mainFlag = this.body.hasChild("mainFlag") ? this.body.getChild("mainFlag") : null;
		this.flag = this.mainFlag != null ? this.mainFlag.getChild("flag") : null;

		// Le porte-drapeau n'est dessine que par renderTribeFlagPole : masque d'entree, il ne risque
		// pas de l'etre avec la texture de la peau par le modele de base ni par un modele d'overlay
		// de skin, qui ne rejoue pas setupAnim.
		if (this.mainFlag != null) this.mainFlag.visible = false;
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
		this.ALL2.translateAndRotate(poseStack);
		this.ALL.translateAndRotate(poseStack);
		this.body.translateAndRotate(poseStack);
		// Visible le temps de ce seul appel : le reste du pipeline doit continuer a l'ignorer.
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

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition ALL2 = partdefinition.addOrReplaceChild("ALL2", CubeListBuilder.create(), PartPose.offset(0.0F, 7.0F, -2.0F));

		PartDefinition ALL = ALL2.addOrReplaceChild("ALL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition body = ALL.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -14.0F, -19.0F, 24.0F, 30.0F, 38.0F, new CubeDeformation(0.0F))
				.texOffs(237, 0).addBox(-12.0F, -14.0F, -19.0F, 24.0F, 30.0F, 38.0F, new CubeDeformation(0.5F))
				.texOffs(161, 38).addBox(-11.0F, -16.0F, 5.0F, 10.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(178, 79).addBox(-11.0F, -28.0F, 17.0F, 10.0F, 14.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(178, 79).mirror().addBox(1.0F, -28.0F, 17.0F, 10.0F, 14.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(148, 79).mirror().addBox(-6.0F, -28.0F, -8.0F, 12.0F, 14.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(161, 38).mirror().addBox(1.0F, -16.0F, 5.0F, 10.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(154, 21).addBox(-6.0F, -16.0F, -19.0F, 12.0F, 2.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(124, 28).addBox(-1.0F, -44.0F, 20.0F, 3.0F, 17.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 6.0F, -35.6F, -0.3054F, 0.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(252, 136).addBox(-15.0F, -2.4F, -9.0F, 30.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, -19.0F));

		PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(228, 92).mirror().addBox(0.0F, -28.0F, -1.0F, 0.0F, 29.0F, 12.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(15.0F, -2.0F, -7.0F, -0.2712F, -0.0482F, -0.1276F));

		PartDefinition cube_r3 = head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(228, 92).addBox(0.0F, -28.0F, -1.0F, 0.0F, 29.0F, 12.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-15.0F, -2.0F, -7.0F, -0.2712F, 0.0482F, 0.1276F));

		PartDefinition mouth = head.addOrReplaceChild("mouth", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition mouth_down = mouth.addOrReplaceChild("mouth_down", CubeListBuilder.create().texOffs(9, 159).addBox(-9.0F, -2.0F, -24.0F, 18.0F, 7.0F, 24.0F, new CubeDeformation(0.0F))
				.texOffs(5, 115).addBox(-8.0F, -4.0F, -23.9F, 0.0F, 3.0F, 23.0F, new CubeDeformation(0.025F))
				.texOffs(3, 132).addBox(-8.0F, -4.0F, -23.5F, 16.0F, 3.0F, 0.0F, new CubeDeformation(0.025F))
				.texOffs(5, 115).mirror().addBox(8.0F, -4.0F, -23.9F, 0.0F, 3.0F, 23.0F, new CubeDeformation(0.025F)).mirror(false), PartPose.offset(0.0F, 2.0F, 0.0F));

		PartDefinition mouth_up = mouth.addOrReplaceChild("mouth_up", CubeListBuilder.create().texOffs(123, 168).addBox(-9.0F, -9.0F, -21.0F, 18.0F, 13.0F, 24.0F, new CubeDeformation(0.0F))
				.texOffs(60, 120).addBox(-8.0F, 4.0F, -20.9F, 0.0F, 3.0F, 23.0F, new CubeDeformation(0.025F))
				.texOffs(60, 120).mirror().addBox(8.0F, 4.0F, -20.9F, 0.0F, 3.0F, 23.0F, new CubeDeformation(0.025F)).mirror(false)
				.texOffs(60, 135).addBox(-8.0F, 4.0F, -20.5F, 16.0F, 3.0F, 0.0F, new CubeDeformation(0.025F)), PartPose.offset(0.0F, -4.0F, -3.0F));

		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 19.0F));

		PartDefinition front_tail = tail.addOrReplaceChild("front_tail", CubeListBuilder.create().texOffs(0, 68).addBox(-8.0F, -11.0F, 0.0F, 16.0F, 23.0F, 35.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition back_tail = tail.addOrReplaceChild("back_tail", CubeListBuilder.create().texOffs(102, 113).addBox(-15.0F, -1.0F, -3.0F, 30.0F, 2.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, 34.0F));

		PartDefinition left_fan = body.addOrReplaceChild("left_fan", CubeListBuilder.create().texOffs(124, 0).addBox(0.0F, -1.0F, -6.0F, 21.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(12.0F, 10.0F, -10.0F));

		PartDefinition right_fan = body.addOrReplaceChild("right_fan", CubeListBuilder.create().texOffs(124, 0).mirror().addBox(-21.0F, -1.0F, -6.0F, 21.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-12.0F, 10.0F, -10.0F));

		PartDefinition mainFlag = body.addOrReplaceChild("mainFlag", CubeListBuilder.create().texOffs(55, 288).addBox(-0.5F, -18.75F, -0.5F, 1.0F, 20.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(60, 287).addBox(-0.5F, -18.75F, 0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(60, 287).addBox(-0.5F, -6.75F, 0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(47, 302).addBox(-1.0F, -3.725F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.1F))
				.texOffs(47, 302).addBox(-1.0F, 0.275F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.1F))
				.texOffs(40, 295).addBox(-1.0F, -2.725F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.1F))
				.texOffs(40, 287).addBox(-1.0F, -20.75F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(12.5F, -13.0F, 10.5F));

		PartDefinition flag = mainFlag.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(7, 240).addBox(0.0F, -5.75F, 0.5F, 0.0F, 11.0F, 19.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 512, 512);
	}

	    @Override
    protected AnimationDefinition comboAnimation(int index) {
        return switch (index) {
            case 1 -> OrcaAnimations.ATTACK_STRIKE;
            case 2 -> OrcaAnimations.ATTACK_STRIKE_2;
            case 3 -> OrcaAnimations.ATTACK_STRIKE_3;
            default -> null;
        };
    }

    @Override
    protected float comboSpeed(int index) {
        return switch (index) {
            case 1 -> 1.0f;
            case 2 -> 1.0f;
            case 3 -> 1.0f;
            default -> 1.0f;
        };
    }

    @Override
	public void setupAnim(OrcaEntity orca, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		if (Math.abs(externalRiderPitch) > 0.01f) {
			this.ALL2.xRot = (float) Math.toRadians(externalRiderPitch);
		}

		if (Math.abs(externalBankRoll) > 0.01f) {
			this.ALL2.zRot = (float) Math.toRadians(externalBankRoll);
		}

		if (orca.isBaby()) {
			float maturationPercent = (float) orca.getMaturationPercentage() / 100f;
			float headScale = 1.5f - (1.6f - 1.0f) * maturationPercent;

			this.head.xScale *= headScale;
			this.head.yScale *= headScale;
			this.head.zScale *= headScale;
		}

		this.applyHeadRotation(netHeadYaw, headPitch);

		// Les trois coups sont EMPILÉS, sans sortie anticipée : les transformations d'une animation
		// s'ajoutent à la pose courante, donc la fin du coup précédent et le début du suivant se
		// mélangent d'eux-mêmes le temps de leur recouvrement. C'est là toute la fluidité du
		// crocodile, dont ce bloc est la copie.
		//
		// Chaque branche coupait auparavant le rendu par un {@code return} : une seule animation
		// pouvait donc s'appliquer à la fois. Le coup précédent restait figé sur sa dernière image
		// jusqu'à son arrêt, puis la pose sautait d'un bloc à l'attaque suivante. La nage était par
		// la même occasion suspendue pendant toute la durée du combo.
		animateCombos(orca, ageInTicks);

		if ((orca.isRunning() || orca.getState() == 2)) {
			float speed = orca.getControllingPassenger() != null ? 1.2f : 1.0f;
			this.animateWalk(OrcaAnimations.MOVE_SWIM, limbSwing, limbSwingAmount, speed, speed);

			if (walkAnimCrossed(OrcaAnimations.MOVE_SWIM, limbSwing, speed, 300L)) orca.onRightFootDown();
			if (walkAnimCrossed(OrcaAnimations.MOVE_SWIM, limbSwing, speed, 300L)) orca.onLeftFootDown();

		} else {
			float speed = orca.getControllingPassenger() != null ? 1.5f : 0.85f;
			this.animateWalk(OrcaAnimations.MOVE_SWIM, limbSwing, limbSwingAmount, speed, speed);

			if (walkAnimCrossed(OrcaAnimations.MOVE_SWIM, limbSwing, speed, 300L)) orca.onRightFootDown();
			if (walkAnimCrossed(OrcaAnimations.MOVE_SWIM, limbSwing, speed, 300L)) orca.onLeftFootDown();
		}

		this.animate(orca.idleAnimationState, OrcaAnimations.MISC_IDLE, ageInTicks, 1.0f);

		applyFlop(orca, ageInTicks);

		applyBigMouth(orca, ageInTicks);

		this.prevLimbSwing = limbSwing;

		captureBodyState(orca, 7f, 1.0f, this.ALL2, this.ALL, this.body);
	}

	private void applyFlop(OrcaEntity orca, float ageInTicks) {
		if (!orca.isFlopping()) return;

		float wave = FLOP_TAIL_SPEED * ageInTicks;
		this.tail.yRot -= FLOP_TAIL_SWING * Mth.sin(wave);
		this.front_tail.yRot -= FLOP_TAIL_SWING * 0.6f * Mth.sin(wave - 0.4f);
		this.back_tail.yRot -= FLOP_TAIL_SWING * 0.4f * Mth.sin(wave - 0.8f);
	}

	/**
	 * Grande Gueule — happe puis gorge pleine.
	 *
	 * <p>Deux temps distincts. La happe ouvre les mâchoires en grand puis les referme d'un coup sec,
	 * en une douzaine de ticks, le cou fouettant vers l'avant. Ensuite, tant qu'une proie est dedans,
	 * la gorge reste <b>gonflée</b> : c'est le seul indice visible, la victime étant masquée à
	 * l'intérieur.</p>
	 *
	 * <p>Le renflement passe par une mise à l'échelle des mâchoires plutôt que par un cube ajouté au
	 * modèle : un nouveau volume aurait exigé sa propre plage de texture, et faute de savoir quelle
	 * zone de la peau lui attribuer il serait sorti bariolé. Étirer l'os existant réutilise ses
	 * coordonnées, donc sa peau — sur le noir lisse d'une orque, la déformation ne se lit que comme
	 * un volume.</p>
	 */
	private void applyBigMouth(OrcaEntity orca, float ageInTicks) {
		// Progression continue, et non par paliers d'un tick.
		//
		// Le décompte de la happe est un entier décrémenté une fois par tick : lu tel quel, il
		// donnait dix-huit positions figées, chacune tenue trois ou quatre images d'affilée — le
		// geste avançait par à-coups quel que soit le soin mis dans les courbes. On lui retire la
		// fraction d'image écoulée, que porte déjà {@code ageInTicks}, et la course redevient lisse.
		float partial = ageInTicks - (float) Math.floor(ageInTicks);
		float lunge = Math.max(0f, (orca.getMouthLungeTicks() - partial)
				/ (float) OrcaEntity.getMouthLungeDuration());

		if (lunge > 0f) {
			// Temps écoulé depuis l'armement, en ticks fractionnaires.
			float elapsed = OrcaEntity.getMouthLungeDuration() - (orca.getMouthLungeTicks() - partial);

			final float wEnd = OrcaEntity.MOUTH_ANIM_WINDUP;
			final float tEnd = wEnd + OrcaEntity.MOUTH_ANIM_TENSE;
			final float sEnd = tEnd + OrcaEntity.MOUTH_ANIM_STRIKE;
			final float rEnd = sEnd + OrcaEntity.MOUTH_ANIM_RECOVER;

			// ── Armement : monte de 0 à 1, se TIENT pendant le temps d'arrêt, puis retombe d'un
			// coup sur la frappe. C'est ce palier immobile qui fait exister le « BAM » d'après.
			float charge;
			if (elapsed < wEnd)      charge = smooth(elapsed / wEnd);
			else if (elapsed < tEnd) charge = 1f;
			else if (elapsed < sEnd) charge = 1f - smooth((elapsed - tEnd) / (sEnd - tEnd));
			else                     charge = 0f;

			// Frappe : 0 → 1 sur les quatre ticks du coup, puis relâchement en roue libre.
			float strike = elapsed <= tEnd ? 0f
					: (elapsed >= sEnd ? 1f : smooth((elapsed - tEnd) / (sEnd - tEnd)));
			float release = elapsed <= sEnd ? 0f : smooth((elapsed - sEnd) / (rEnd - sEnd));
			// Élan porté par la frappe, qui s'éteint pendant la récupération.
			float thrust = strike * (1f - release);
			// Pic bref au contact : la sur-fermeture des mâchoires.
			float impact = bell(elapsed, sEnd, OrcaEntity.MOUTH_ANIM_STRIKE * 0.8f);
			// Frémissement du palier : la bête n'est pas une statue pendant qu'elle vise. Amplitude
			// volontairement basse — au-delà, la tension du temps d'arrêt tourne à la vibration et
			// l'orque paraît grelotter au lieu de retenir son coup.
			float tremble = (elapsed >= wEnd && elapsed < tEnd)
					? (float) Math.sin(elapsed * 2.6) * 0.22f : 0f;

			// Contre-mouvement d'amorce : la bête recule d'un rien avant de se cabrer. C'est le
			// vieux principe de l'anticipation — sans lui, le geste démarre sans avoir été annoncé.
			float anticip = bell(elapsed, wEnd * 0.30f, wEnd * 0.55f);

			// ── CORPS ENTIER (ALL / ALL2) ────────────────────────────────────────────────
			// Le tronc n'est pas seul à bouger : la bête se cabre en bloc, se tord légèrement,
			// puis se détend d'une pièce vers l'avant. C'est ce que le geste avait de plus
			// manquant — la carcasse restait droite pendant que la tête faisait tout le travail.
			this.ALL.xRot  -= (float) Math.toRadians(10f * charge - 7f * thrust - 2.5f * anticip);
			this.ALL.zRot  += (float) Math.toRadians(5f * charge + 0.8f * tremble - 4f * thrust);
			this.ALL.yRot  += (float) Math.toRadians(3f * charge - 2.5f * thrust);
			this.ALL.y     -= 1.8f * charge - 0.7f * anticip;
			this.ALL.z     -= 4.5f * thrust - 1.2f * anticip;
			// Secousse sèche à l'instant du contact, sur la racine : tout le corps encaisse.
			this.ALL2.xRot += (float) Math.toRadians(2.0f * impact);
			this.ALL2.zRot += (float) Math.toRadians(1.0f * impact * (float) Math.sin(elapsed * 3.1));

			// ── TRONC ───────────────────────────────────────────────────────────────────
			this.body.xRot -= (float) Math.toRadians(7f * charge - 4f * thrust);
			this.body.zRot += (float) Math.toRadians(1.2f * tremble - 3f * thrust);
			this.body.z    -= 3.6f * thrust - 0.8f * anticip;
			this.body.y    -= 1.3f * charge;

			// ── TÊTE ────────────────────────────────────────────────────────────────────
			this.head.xRot -= (float) Math.toRadians(16f * charge - 9f * thrust - 5f * impact);
			this.head.yRot += (float) Math.toRadians(1.5f * tremble - 2.2f * impact * (float) Math.sin(elapsed * 3.0));
			this.head.zRot += (float) Math.toRadians(3f * charge - 3f * thrust);
			this.head.z    -= 1.5f * thrust;

			// ── BLOC MÂCHOIRE ───────────────────────────────────────────────────────────
			// L'ensemble des deux mâchoires avance d'une pièce au moment de mordre : c'est ce qui
			// distingue un coup de dents porté d'une simple ouverture sur place.
			this.mouth.z    -= 2.4f * thrust;
			this.mouth.xRot += (float) Math.toRadians(4f * charge - 3f * impact);
			this.mouth.y    -= 0.8f * charge;

			// ── MÂCHOIRES : béantes tant que l'armement tient, claquées sur la frappe.
			this.mouth_down.xRot += (float) Math.toRadians(48f * charge + 0.7f * tremble - 10f * impact);
			this.mouth_up.xRot   -= (float) Math.toRadians(38f * charge + 0.7f * tremble - 8f * impact);
			this.mouth_down.yScale *= 1f + 0.10f * charge;
			this.mouth_up.yScale   *= 1f + 0.08f * charge;
			this.mouth_down.zScale *= 1f + 0.06f * charge;
			this.mouth_down.y      += 0.9f * charge;
			this.mouth_up.y        -= 0.7f * charge;

			// ── PECTORALES : repliées le long du corps pendant l'armement, ouvertes en frein
			// une fois le coup porté. Elles battent aussi une fois à l'impact.
			float fin = charge - 0.5f * thrust;
			this.left_fan.zRot  -= (float) Math.toRadians(26f * fin + 8f * impact);
			this.right_fan.zRot += (float) Math.toRadians(26f * fin + 8f * impact);
			this.left_fan.yRot  += (float) Math.toRadians(12f * charge - 6f * thrust);
			this.right_fan.yRot -= (float) Math.toRadians(12f * charge - 6f * thrust);
			this.left_fan.xRot  += (float) Math.toRadians(7f * thrust);
			this.right_fan.xRot += (float) Math.toRadians(7f * thrust);

			// ── QUEUE : elle s'arme du côté opposé, puis fouette. La vague court du tronc à la
			// caudale avec un tick de retard sur chaque segment.
			float coil = charge - 1.6f * thrust;
			this.tail.yRot       += (float) Math.toRadians(20f * coil);
			this.tail.xRot       += (float) Math.toRadians(6f * charge - 5f * thrust);
			this.tail.z          += 1.4f * charge;
			this.front_tail.yRot += (float) Math.toRadians(15f * (charge - 1.6f * lag(thrust, release, 0.15f)));
			this.front_tail.xRot += (float) Math.toRadians(5f * charge - 4f * thrust);
			this.back_tail.yRot  += (float) Math.toRadians(10f * (charge - 1.6f * lag(thrust, release, 0.30f)));
			this.back_tail.xRot  += (float) Math.toRadians(9f * thrust - 4f * charge);
		}

		// ── Recrachage : même grammaire que la happe, à l'envers. La bête se contracte sur sa
		// prise, puis ouvre d'un coup. Ce bloc tourne AVANT la sortie ci-dessous, car il continue
		// de jouer une fois la proie partie — la gueule doit finir de s'ouvrir et de se refermer.
		float spitTicks = orca.getMouthSpitTicks();
		if (spitTicks > 0f) {
			float elapsed = OrcaEntity.getMouthSpitDuration() - (spitTicks - partial);

			final float hEnd = OrcaEntity.MOUTH_SPIT_HEAVE;
			final float bEnd = hEnd + OrcaEntity.MOUTH_SPIT_BURST;
			final float rEnd = bEnd + OrcaEntity.MOUTH_SPIT_RECOVER;

			// Compression : monte pendant la contraction, se relâche à mesure que la gueule ouvre.
			float heave = elapsed < hEnd ? smooth(elapsed / hEnd)
					: (elapsed < bEnd ? 1f - smooth((elapsed - hEnd) / (bEnd - hEnd)) : 0f);
			float burst = elapsed <= hEnd ? 0f
					: (elapsed >= bEnd ? 1f : smooth((elapsed - hEnd) / (bEnd - hEnd)));
			float fade = elapsed <= bEnd ? 0f : smooth((elapsed - bEnd) / (rEnd - bEnd));
			float open = burst * (1f - fade);

			// La bête se ramasse sur sa prise, mâchoires serrées.
			this.mouth_down.xRot -= (float) Math.toRadians(7f * heave);
			this.mouth_up.xRot   += (float) Math.toRadians(5f * heave);
			this.body.xRot       += (float) Math.toRadians(7f * heave);
			this.body.z          += 1.8f * heave;

			// Puis tout s'ouvre : la proie part avec.
			this.mouth_down.xRot += (float) Math.toRadians(54f * open);
			this.mouth_up.xRot   -= (float) Math.toRadians(42f * open);
			this.head.xRot       -= (float) Math.toRadians(13f * open);
			this.body.xRot       -= (float) Math.toRadians(6f * open);
			this.body.z          -= 2.6f * open;

			// Secousse de tête : on ne recrache pas proprement.
			this.head.yRot += (float) Math.toRadians(8f * open * (float) Math.sin(elapsed * 2.2));

			// Pectorales en frein, queue en contre-appui : la bête freine ce qu'elle expulse.
			this.left_fan.zRot  += (float) Math.toRadians(20f * open);
			this.right_fan.zRot -= (float) Math.toRadians(20f * open);
			this.tail.yRot      -= (float) Math.toRadians(15f * open);
			this.front_tail.yRot -= (float) Math.toRadians(10f * open);
		}

		if (!orca.hasSwallowed()) return;

		// Respiration lente du renflement : une gorge pleine qui travaille, pas un volume figé.
		float swell = 1.0f + 0.06f * (float) Math.sin(ageInTicks * 0.18f);
		// Déglutition : toutes les deux secondes et demie, la bête fait descendre sa prise.
		float gulp = bell((ageInTicks % 50f) / 50f, 0.15f, 0.14f);

		// Élargie : le renflement doit déborder sur les côtés du crâne pour se lire de profil
		// comme de face, et pas seulement s'affaisser vers le bas.
		this.mouth_down.xScale *= 1.48f * swell;
		this.mouth_down.yScale *= (1.55f + 0.22f * gulp) * swell;
		this.mouth_down.zScale *= 1.10f;

		this.mouth_up.xScale *= 1.30f * swell;
		this.mouth_up.yScale *= 1.14f + 0.08f * gulp;

		// La mâchoire pleine ne ferme plus tout à fait. Elle est portée un peu haut : posée trop
		// bas, la poche pendait sous la tête au lieu de la remplir. Y descend dans ce repère.
		this.mouth_down.xRot += (float) Math.toRadians(7f + 4f * gulp);
		this.mouth_down.y += -0.6f + 0.8f * gulp;

		// Le cou se tend vers le haut à chaque déglutition, comme un oiseau qui avale.
		this.head.xRot -= (float) Math.toRadians(8f * gulp);
		this.body.xRot += (float) Math.toRadians(3f * gulp);
	}

	/**
	 * Trapèze lissé : 0 avant {@code riseStart}, 1 entre {@code riseEnd} et {@code fallStart},
	 * 0 après {@code fallEnd}. Les deux flancs sont adoucis pour éviter les cassures.
	 */
	private static float trapezoid(float t, float riseStart, float riseEnd, float fallStart, float fallEnd) {
		if (t <= riseStart || t >= fallEnd) return 0f;
		if (t < riseEnd)   return smooth((t - riseStart) / (riseEnd - riseStart));
		if (t <= fallStart) return 1f;
		return smooth(1f - (t - fallStart) / (fallEnd - fallStart));
	}

	/**
	 * Retarde un élan d'une fraction de sa course : le segment suivant de la queue démarre après le
	 * précédent, ce qui donne la vague plutôt qu'un balai rigide.
	 */
	private static float lag(float thrust, float release, float amount) {
		return Mth.clamp((thrust - amount) / (1f - amount), 0f, 1f) * (1f - release);
	}

	/** Cloche centrée sur {@code center}, de demi-largeur {@code halfWidth}. */
	private static float bell(float t, float center, float halfWidth) {
		float d = Math.abs(t - center) / halfWidth;
		return d >= 1f ? 0f : smooth(1f - d);
	}

	private static float smooth(float x) {
		x = Mth.clamp(x, 0f, 1f);
		return x * x * (3f - 2f * x);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		this.ALL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	private void captureBodyState(OrcaEntity orca, float restPoseYSum, float riderRotIntensity, ModelPart... boneChain) {
		if (!orca.level().isClientSide()) return;

		// Modele du cavalier : orientation VISIBLE complete, ALL2 compris — il porte le pique
		// commande au regard, et sans lui le joueur resterait droit pendant que sa monture plonge.
		orca.setBodyZRot((float) Math.toDegrees((this.ALL2.zRot + this.ALL.zRot + this.body.zRot) * riderRotIntensity));
		orca.setBodyXRot((float) -Math.toDegrees((this.ALL2.xRot + this.ALL.xRot + this.body.xRot) * riderRotIntensity));
		orca.bodyYRot = (float) Math.toDegrees((this.ALL2.yRot + this.ALL.yRot + this.body.yRot) * riderRotIntensity);
		orca.bodyAnimXRot = this.ALL2.xRot + this.ALL.xRot + this.body.xRot;

		// Matrice REELLE de la chaine ALL2 -> ALL -> body, telle que le renderer la compose. On ne
		// redecrit plus la transformation a la main : on la releve. Pivots, ordre des rotations et
		// translations d'animation y sont deja, sans convention a deviner.
		PoseStack bones = new PoseStack();
		this.ALL2.translateAndRotate(bones);
		this.ALL.translateAndRotate(bones);
		this.body.translateAndRotate(bones);
		orca.boneMatrix = new Matrix4f(bones.last().pose());

		orca.bodyZRot_passenger = orca.getBodyZRot();
		orca.bodyXRot_passenger = orca.getBodyXRot();
		orca.bodyYRot_passenger = orca.bodyYRot;

		// Camera : ALL + body seuls. Le pique de ALL2 vient du regard du pilote, qui l'applique deja.
		orca.camZRot = (float)  Math.toDegrees(this.ALL.zRot + this.body.zRot);
		orca.camXRot = (float) -Math.toDegrees(this.ALL.xRot + this.body.xRot);
		orca.camYRot = (float)  Math.toDegrees(this.ALL.yRot + this.body.yRot);

		float xSum = this.ALL2.x + this.ALL.x + this.body.x;
		orca.bodyAnimX = -xSum;
		orca.bodyAnimX_passenger = -xSum;

		float ySum = 0f;
		for (ModelPart bone : boneChain) ySum += bone.y;
		orca.bodyAnimY = ySum - restPoseYSum;
		orca.bodyAnimY_passenger = orca.bodyAnimY;
	}

	private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch) {
		pNetHeadYaw = Mth.clamp(pNetHeadYaw, -30.0F, 30.0F);
		pHeadPitch = Mth.clamp(pHeadPitch, -30.0F, 30.0F);

		this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
		this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);
	}

	@Override
	public ModelPart root() {
		return this.ALL2;
	}

	public void copyPoseFrom(OrcaModel<?> src) {
		this.ALL2.copyFrom(src.ALL2);
		this.ALL.copyFrom(src.ALL);
		this.body.copyFrom(src.body);
		this.head.copyFrom(src.head);
		this.mouth.copyFrom(src.mouth);
		this.mouth_down.copyFrom(src.mouth_down);
		this.mouth_up.copyFrom(src.mouth_up);
		this.tail.copyFrom(src.tail);
		this.front_tail.copyFrom(src.front_tail);
		this.back_tail.copyFrom(src.back_tail);
		this.left_fan.copyFrom(src.left_fan);
		this.right_fan.copyFrom(src.right_fan);
	}

	private boolean walkAnimCrossed(AnimationDefinition animation, float limbSwing, float speedScale, long triggerTimeMs) {
		long durationMs = (long)(animation.lengthInSeconds() * 1000f);
		if (durationMs <= 0) return false;

		long cur  = ((long)(limbSwing     * 50f * speedScale)) % durationMs;
		long prev = ((long)(prevLimbSwing * 50f * speedScale)) % durationMs;

		if (prev <= cur) return prev < triggerTimeMs && cur >= triggerTimeMs;
		return triggerTimeMs <= cur || triggerTimeMs > prev;
	}
}
