package net.tiew.operationWild.entity.client.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/**
 * {@link MultiBufferSource} qui enveloppe chaque buffer dans un {@link GhostVertexConsumer},
 * appliquant la teinte/alpha spectral à tout ce qui est dessiné au travers — quel que soit
 * le modèle ou la couche. Utilisé par {@code SoulGhostRenderer}.
 */
public class GhostBufferSource implements MultiBufferSource {

    private final MultiBufferSource delegate;
    private final float alpha;

    public GhostBufferSource(MultiBufferSource delegate, float alpha) {
        this.delegate = delegate;
        this.alpha = alpha;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return new GhostVertexConsumer(delegate.getBuffer(renderType), alpha);
    }
}
