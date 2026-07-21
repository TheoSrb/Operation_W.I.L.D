package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaTailPart;
import net.tiew.operationWild.entity.client.model.OWFlagModel;
import net.tiew.operationWild.entity.client.render.misc.OWTribeFlagRenderer;

/**
 * Drapeau de tribu du boa.
 *
 * <p>Contrairement aux autres montures, le boa ne porte pas sa hampe sur son modèle principal : en
 * jeu, sa queue n'est pas une suite d'os mais une suite d'<b>entités</b> {@link BoaTailPart}
 * indépendantes, et les os {@code body_*} du modèle de tête y sont masqués — ils restent en pose de
 * repos et ne suivent rien. Un drapeau accroché là dériverait n'importe où. Il est donc greffé sur
 * le modèle du segment porteur (cf. {@code BoaTailPartModel.createBody2Layer}) et rendu ici, depuis
 * le renderer de ce segment, dont il hérite naturellement la position et l'orientation.</p>
 *
 * <p>Tout ce qui relève de la tribu — appartenance, selle, option d'affichage — se lit sur le boa
 * parent, le segment n'étant qu'un morceau de corps.</p>
 */
public class BoaTailFlagLayer extends RenderLayer<BoaTailPart, EntityModel<BoaTailPart>> {

    public BoaTailFlagLayer(RenderLayerParent<BoaTailPart, EntityModel<BoaTailPart>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, BoaTailPart part,
                       float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        // Un seul segment déclare les os du porte-drapeau : les autres modèles répondent false.
        if (!(this.getParentModel() instanceof OWFlagModel model) || !model.hasTribeFlag()) return;

        // getParent() ne repond que sur le serveur : cote client on passe par l'identifiant reseau
        // synchronise (cf. BoaTailPart.getParentForRender).
        Entity parent = part.getParentForRender();
        if (!(parent instanceof OWEntity boa) || boa.currentTeam == null) return;
        if (!boa.isSaddled() || !boa.isShowTribeFlag()
                || !boa.isTame() || !boa.isAlive()
                || boa.isInvisible() || boa.isInResurrection()) return;

        // Le segment porte sa propre orientation (yHeadRot, calculée serveur et synchronisée) :
        // c'est elle qui doit piloter le fouet du drapeau, pas celle de la tête, qui vire avant lui.
        float turnRate = Mth.wrapDegrees(part.yHeadRot - part.yHeadRotO);
        // L'agitation, en revanche, se lit sur le boa : un segment n'a pas de cycle de marche.
        float wind = boa.walkAnimation.speed();

        OWTribeFlagRenderer.render(poseStack, bufferSource, packedLight, part, boa.currentTeam,
                model, turnRate, wind, ageInTicks);
    }
}
