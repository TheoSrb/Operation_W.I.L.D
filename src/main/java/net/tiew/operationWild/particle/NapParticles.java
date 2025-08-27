package net.tiew.operationWild.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class NapParticles extends TextureSheetParticle {

    protected NapParticles(ClientLevel level, double x, double y, double z, SpriteSet spriteSet, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);

        this.friction = 0.6f;
        this.lifetime = 40;
        this.setSpriteFromAge(spriteSet);

        this.hasPhysics = false;

        this.alpha = 0.0f;

        this.scale(2f);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();

        float progress = (float) this.age / (float) this.lifetime;
        if (progress <= 0.5f) this.alpha = progress * 2.0f;
        else this.alpha = (1.0f - progress) * 2.0f;

        this.yd = 0.035;
        this.xd = Math.sin(this.age * 0.1) * 0.01;
        this.zd = Math.cos(this.age * 0.1) * 0.01;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel,
                                                 double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            return new NapParticles(clientLevel, pX, pY, pZ, this.spriteSet, pXSpeed, pYSpeed, pZSpeed);
        }
    }
}