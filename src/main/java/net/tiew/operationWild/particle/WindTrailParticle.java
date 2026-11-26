package net.tiew.operationWild.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class WindTrailParticle extends TextureSheetParticle {
    private static final float START_ALPHA = 0.5f;

    private final SpriteSet sprites;

    protected WindTrailParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        this.sprites = sprites;
        this.friction = 0.9f;
        this.gravity = 0f;
        this.hasPhysics = false;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        float shade = 0.92f + this.random.nextFloat() * 0.08f;
        this.rCol = shade;
        this.gCol = shade;
        this.bCol = shade;
        this.alpha = START_ALPHA;
        this.quadSize *= 1.5f;
        this.lifetime = 14 + this.random.nextInt(8);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        float life = Mth.clamp((float) this.age / this.lifetime, 0f, 1f);
        this.alpha = START_ALPHA * (1f - life);
        this.setSpriteFromAge(this.sprites);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
            return new WindTrailParticle(level, x, y, z, vx, vy, vz, this.sprites);
        }
    }
}
