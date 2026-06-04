package net.tiew.operationWild.entity.client.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;

/**
 * Enveloppe un {@link VertexConsumer} pour transformer n'importe quel modèle en
 * fantôme spectral GÉNÉRIQUE : chaque couleur de sommet est teintée vers une couleur
 * d'âme (cyan spectral) et son alpha est forcé. Combiné au {@code RenderType} translucide
 * imposé par {@code OWEntityRenderer.RENDER_AS_GHOST}, ça donne un rendu fantôme cohérent
 * pour tout OWEntity sans code ni texture dédiés.
 */
public class GhostVertexConsumer implements VertexConsumer {

    // Couleur d'âme (cyan spectral) vers laquelle on tinte le modèle.
    private static final int SOUL_R = 0x86;
    private static final int SOUL_G = 0xDB;
    private static final int SOUL_B = 0xFF;
    private static final float TINT_STRENGTH = 0.5f;

    private final VertexConsumer delegate;
    private final int alpha255;

    public GhostVertexConsumer(VertexConsumer delegate, float alpha) {
        this.delegate = delegate;
        this.alpha255 = Mth.clamp((int) (alpha * 255f), 0, 255);
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        int rr = (int) (r * (1 - TINT_STRENGTH) + SOUL_R * TINT_STRENGTH);
        int gg = (int) (g * (1 - TINT_STRENGTH) + SOUL_G * TINT_STRENGTH);
        int bb = (int) (b * (1 - TINT_STRENGTH) + SOUL_B * TINT_STRENGTH);
        delegate.setColor(Mth.clamp(rr, 0, 255), Mth.clamp(gg, 0, 255), Mth.clamp(bb, 0, 255), this.alpha255);
        return this;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        delegate.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        delegate.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        delegate.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        delegate.setNormal(x, y, z);
        return this;
    }
}
