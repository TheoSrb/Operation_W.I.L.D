package net.tiew.operationWild.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class AimMarkerParticle extends TextureSheetParticle {

    private static final int LIFETIME = 5;
    private static final float RING_SIZE = 0.085f;
    private static final float CORE_SIZE = 0.15f;
    private static final float START_ALPHA = 0.9f;
    private static final float END_SHRINK = 0.35f;

    private final float baseSize;

    protected AimMarkerParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, boolean core) {
        super(level, x, y, z);
        this.lifetime = LIFETIME;
        this.hasPhysics = false;
        this.gravity = 0f;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.baseSize = core ? CORE_SIZE : RING_SIZE;
        this.quadSize = this.baseSize;
        this.alpha = START_ALPHA;
        this.setSprite(sprites.get(this.random));
        if (core) this.setColor(1f, 0.93f, 0.72f);
        else this.setColor(1f, 0.6f, 0.18f);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        float life = (float) this.age / this.lifetime;
        this.alpha = START_ALPHA * (1f - life * life);
        this.quadSize = this.baseSize * (1f - END_SHRINK * life);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType type, ClientLevel level,
                                                 double x, double y, double z, double core, double unusedY, double unusedZ) {
            return new AimMarkerParticle(level, x, y, z, this.sprites, core >= 0.5);
        }
    }
}
