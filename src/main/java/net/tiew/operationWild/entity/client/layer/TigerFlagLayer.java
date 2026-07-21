package net.tiew.operationWild.entity.client.layer;

import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.entity.client.model.TigerModel;
import net.tiew.operationWild.entity.client.render.TigerRenderer;

/**
 * Drapeau de tribu du tigre. N'ajoute au comportement générique que les états où le tigre lui-même
 * s'efface : le drapeau ne doit pas rester planté en l'air pendant un Shadow Strike ni flotter,
 * opaque, au milieu d'un fondu de camouflage.
 */
public class TigerFlagLayer extends OWTribeFlagLayer<TigerEntity, TigerModel<TigerEntity>> {

    public TigerFlagLayer(TigerRenderer renderer) {
        super(renderer);
    }

    @Override
    protected boolean shouldRenderFlag(TigerEntity tiger) {
        return super.shouldRenderFlag(tiger)
                && !tiger.isShadowStrikeActive()
                && TigerRenderer.currentAlpha >= 1f;
    }
}
