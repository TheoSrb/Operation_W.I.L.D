package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.client.model.OWFlagModel;
import net.tiew.operationWild.entity.client.render.misc.OWTribeFlagRenderer;

/**
 * Fait arborer à un <b>champion</b> de tribu la bannière de celle-ci, sur la hampe portée par son
 * modèle ({@link OWFlagModel}).
 *
 * <p>La hampe et la toile restent masquées en dehors de ce layer : elles n'apparaissent donc que si
 * l'entité est apprivoisée, sellée <i>et</i> désignée championne par son chef. Le layer se contente de décider de
 * l'affichage et de fournir les grandeurs qui pilotent l'animation ; tout le reste — chaîne
 * articulée, ondulation, comportement sous l'eau — appartient à {@link OWTribeFlagRenderer}.</p>
 *
 * <p>Ce layer s'applique aux entités d'un seul tenant, dont le porte-drapeau appartient au modèle
 * principal. Le boa fait exception : son porte-drapeau est sur un segment de queue rendu comme une
 * entité indépendante, d'où {@link BoaTailFlagLayer}.</p>
 *
 * <p><b>Multijoueur</b> : la tribu de l'entité est déjà répliquée à tous les joueurs du niveau (cf.
 * {@code OWTribeManager.pushEntityTeam} et {@code OWEntity.startSeenByPlayer}), et l'animation est
 * purement locale. Chaque client voit donc le drapeau de chaque entité, en solo comme en serveur.</p>
 */
public class OWTribeFlagLayer<T extends OWEntity, M extends EntityModel<T> & OWFlagModel> extends RenderLayer<T, M> {

    public OWTribeFlagLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.currentTeam == null || !shouldRenderFlag(entity)) return;

        // Le drapeau traîne derrière la monture qui vire : c'est la rotation du corps qui le pilote.
        float turnRate = Mth.wrapDegrees(entity.yBodyRot - entity.yBodyRotO);
        OWTribeFlagRenderer.render(poseStack, bufferSource, packedLight, entity, entity.currentTeam,
                this.getParentModel(), turnRate, limbSwingAmount, ageInTicks);
    }

    /**
     * Conditions d'affichage propres à l'entité, au-delà de l'appartenance à une tribu.
     * Les sous-classes y ajoutent leurs états d'invisibilité (camouflage, rituel, etc.).
     *
     * <p>Le détail de la règle vit sur {@link OWEntity#carriesTribeFlag()} : seuls les champions
     * désignés par le chef portent l'étendard, et il leur faut encore une selle pour tenir la
     * hampe.</p>
     */
    protected boolean shouldRenderFlag(T entity) {
        return entity.carriesTribeFlag();
    }
}
