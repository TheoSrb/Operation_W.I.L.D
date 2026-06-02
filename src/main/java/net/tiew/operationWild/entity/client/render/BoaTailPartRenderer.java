package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.tiew.operationWild.entity.animals.terrestrial.BoaTailPart;
import net.tiew.operationWild.entity.client.layer.BoaLayer;
import net.tiew.operationWild.entity.client.layer.BoaTailPartLayer;
import net.tiew.operationWild.entity.client.model.BoaTailPartModel;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;
import net.tiew.operationWild.entity.variants.BoaVariant;

/**
 * COPIE STRICTE de RenderAnacondaPart (Alex's Mobs), adaptee a Operation W.I.L.D.
 *
 * LivingEntityRenderer "bete" : ne calcule AUCUNE position de chaine. Il applique
 * juste, dans setupRotations, le yHeadRot + le getXRot() de l'entite (tous deux
 * calcules cote serveur dans BoaTailPart.tickMultipartPosition et synchronises),
 * puis choisit le bon modele selon le bodyIndex (comme Alex choisit selon le type).
 *
 * La fluidite vient de l'interpolation vanilla de LivingEntity (lerp position +
 * rotation), exactement comme l'anaconda.
 */
public class BoaTailPartRenderer extends LivingEntityRenderer<BoaTailPart, EntityModel<BoaTailPart>> {

    private final BoaTailPartModel[] models = new BoaTailPartModel[7];

    public BoaTailPartRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new BoaTailPartModel(ctx.bakeLayer(BoaTailPartModel.LAYER_BODY_0)), 0.3F);
        models[0] = new BoaTailPartModel(ctx.bakeLayer(BoaTailPartModel.LAYER_BODY_0));
        models[1] = new BoaTailPartModel(ctx.bakeLayer(BoaTailPartModel.LAYER_BODY_1));
        models[2] = new BoaTailPartModel(ctx.bakeLayer(BoaTailPartModel.LAYER_BODY_2));
        models[3] = new BoaTailPartModel(ctx.bakeLayer(BoaTailPartModel.LAYER_BODY_3));
        models[4] = new BoaTailPartModel(ctx.bakeLayer(BoaTailPartModel.LAYER_TAIL1));
        models[5] = new BoaTailPartModel(ctx.bakeLayer(BoaTailPartModel.LAYER_TAIL2));
        models[6] = new BoaTailPartModel(ctx.bakeLayer(BoaTailPartModel.LAYER_TAIL3));

        this.addLayer(new BoaTailPartLayer(this));
    }

    @Override
    protected void setupRotations(BoaTailPart entity, PoseStack stack, float ageInTicks,
                                  float rotationYaw, float partialTicks, float scale) {
        // FLUIDITE : on INTERPOLE le yaw et le pitch entre le tick precedent (xRotO /
        // yHeadRotO) et le tick courant, avec partialTicks. Sans ca, les rotations
        // sautaient d'un tick a l'autre (la position, elle, est deja interpolee par le
        // vanilla), ce qui donnait un mouvement de queue legerement hache. Mth.rotLerp
        // gere correctement le passage par 360 deg (chemin le plus court).
        float newYaw = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float newPitch = Mth.rotLerp(partialTicks, entity.xRotO, entity.getXRot());
        Pose pose = entity.getPose();
        if (pose != Pose.SLEEPING) {
            stack.mulPose(Axis.YP.rotationDegrees(180.0F - newYaw));
            stack.mulPose(Axis.XP.rotationDegrees(newPitch));
        }
        if (entity.deathTime > 0) {
            float f = ((float) entity.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
            f = Mth.sqrt(f);
            if (f > 1.0F) f = 1.0F;
            stack.mulPose(Axis.ZP.rotationDegrees(f * this.getFlipDegrees(entity)));
        }
    }

    @Override
    protected void scale(BoaTailPart entity, PoseStack stack, float partialTickTime) {
        // Choix du modele selon le segment (comme RenderAnacondaPart.scale).
        int idx = Mth.clamp(entity.getBodyIndex(), 0, 6);
        this.model = models[idx];
        // Echelle appliquee a la main, exactement comme OWEntityRenderer.render le
        // fait pour la tete (poseStack.scale(scale,...)). Meme valeur que la tete
        // (copiee du parent), donc tete et queue restent a la meme taille.
        float s = entity.getBoaScale();
        stack.scale(s, s, s);
    }

    @Override
    protected boolean shouldShowName(BoaTailPart entity) {
        return false;
    }

    @Override
    public ResourceLocation getTextureLocation(BoaTailPart entity) {
        // La part porte son propre variant (copyDataFrom cote serveur), donc pas
        // besoin de resoudre le parent cote client.
        BoaVariant variant = BoaVariant.byId(entity.getVariant() & 255);
        return SkinRegistry.BoaSkins.get(variant).getTexture();
    }

    // --- ECLAIRAGE : on prend le MAXIMUM entre la lumiere a la tete et celle a la position
    // propre du segment. Ainsi un segment a l'air libre reste eclaire meme si l'echantillon de
    // la tete tombe dans un point sombre (ex : pendant l'enroulement, la tete peut se retrouver
    // sous la cible / dans un bloc apres le recentrage vertical). Les segments ne sont donc
    // jamais noirs des qu'ils — ou la tete — sont a la lumiere.

    @Override
    protected int getBlockLightLevel(BoaTailPart entity, net.minecraft.core.BlockPos pos) {
        return Math.max(super.getBlockLightLevel(entity, entity.getHeadPos()),
                        super.getBlockLightLevel(entity, pos));
    }

    @Override
    protected int getSkyLightLevel(BoaTailPart entity, net.minecraft.core.BlockPos pos) {
        return Math.max(super.getSkyLightLevel(entity, entity.getHeadPos()),
                        super.getSkyLightLevel(entity, pos));
    }
}