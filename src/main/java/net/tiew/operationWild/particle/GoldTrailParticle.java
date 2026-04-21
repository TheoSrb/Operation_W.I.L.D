package net.tiew.operationWild.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class GoldTrailParticle extends TextureSheetParticle {

    protected GoldTrailParticle(ClientLevel level, double x, double y, double z,
                                 SpriteSet spriteSet, double vx, double vy, double vz) {
        super(level, x, y, z, vx, vy, vz);

        this.pickSprite(spriteSet);

        this.rCol = 1.0f;
        this.gCol = 0.78f;
        this.bCol = 0.1f;
        this.alpha = 0.85f;

        this.lifetime = 10 + this.random.nextInt(8);
        this.quadSize = 0.07f + this.random.nextFloat() * 0.13f;
        this.hasPhysics = false;
        this.friction = 0.85f;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880;
    }

    @Override
    public void tick() {
        super.tick();
        this.alpha = Math.max(0f, this.alpha - 0.85f / this.lifetime);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType type, ClientLevel level,
                                                  double x, double y, double z,
                                                  double vx, double vy, double vz) {
            return new GoldTrailParticle(level, x, y, z, spriteSet, vx, vy, vz);
        }
    }
}
