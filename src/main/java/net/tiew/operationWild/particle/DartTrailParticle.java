package net.tiew.operationWild.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class DartTrailParticle extends TextureSheetParticle {
    private static final float ALPHA = 0.25f;
    private static final float RED = 0.85f;
    private static final float GREEN = 0.25f;
    private static final float BLUE = 0.25f;
    private static final float SCALE = 0.6f;

    private final SpriteSet sprites;

    protected DartTrailParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z, 0.0, 0.0, 0.0);
        this.sprites = sprites;
        this.friction = 0.96f;
        this.speedUpWhenYMotionIsBlocked = true;
        this.xd *= 0.1;
        this.yd *= 0.1;
        this.zd *= 0.1;

        float shade = this.random.nextFloat() * 0.4f + 0.6f;
        this.rCol = randomizeColor(RED, shade);
        this.gCol = randomizeColor(GREEN, shade);
        this.bCol = randomizeColor(BLUE, shade);
        this.alpha = ALPHA;

        this.quadSize *= 0.75f * SCALE;
        this.lifetime = Math.max((int) (8.0 / (this.random.nextDouble() * 0.8 + 0.2) * SCALE), 1);
        this.setSpriteFromAge(sprites);
    }

    private float randomizeColor(float color, float shade) {
        return (this.random.nextFloat() * 0.2f + 0.8f) * color * shade;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float partialTick) {
        return this.quadSize * Mth.clamp((this.age + partialTick) / this.lifetime * 32.0f, 0.0f, 1.0f);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
            return new DartTrailParticle(level, x, y, z, this.sprites);
        }
    }
}
