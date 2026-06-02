package net.tiew.operationWild.entity.client.util;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Décale verticalement les UV pour lire une frame précise dans une spritesheet
 * verticale (frames empilées de haut en bas).
 * Hypothèse : le modèle est bake sur la texture de base (1 frame, donc le V du modèle
 * est normalisé [0,1] sur UNE frame), et la sheet contient cette texture répétée
 * {@code frameCount} fois en hauteur.
 */
public class OWFrameOffsetVertexConsumer implements VertexConsumer {

    private final VertexConsumer delegate;
    private final int frame;
    private final int frameCount;

    public OWFrameOffsetVertexConsumer(VertexConsumer delegate, int frame, int frameCount) {
        this.delegate = delegate;
        this.frame = frame;
        this.frameCount = Math.max(1, frameCount);
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        // v est normalisé [0,1] sur UNE frame, on le ramène sur la sheet complète
        delegate.setUv(u, (v + frame) / frameCount);
        return this;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        delegate.setColor(r, g, b, a);
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
