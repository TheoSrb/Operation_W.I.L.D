package net.tiew.operationWild.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.taming.OWAggressiveTaming;
import org.jetbrains.annotations.Nullable;

public class DizzyStarParticle extends TextureSheetParticle {

    private static final int RING_SIZE = OWAggressiveTaming.DIZZY_STAR_RING;
    private static final int LIFETIME = OWAggressiveTaming.DIZZY_STAR_LIFETIME;
    private static final float ORBIT_SPEED = 0.17f;
    private static final int FADE_TICKS = 10;
    private static final int FADE_OUT_ON_WAKE = 6;

    private final int entityId;
    private final float phase;
    private final float baseSize;
    private final float twinkleOffset;
    private boolean fadingOut;

    protected DizzyStarParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, int entityId, int ringIndex) {
        super(level, x, y, z);
        this.entityId = entityId;
        this.phase = (ringIndex % RING_SIZE) * Mth.TWO_PI / RING_SIZE;
        this.lifetime = LIFETIME;
        this.hasPhysics = false;
        this.gravity = 0f;
        this.alpha = 0f;
        this.twinkleOffset = this.random.nextFloat() * Mth.TWO_PI;
        this.roll = this.random.nextFloat() * Mth.TWO_PI;
        this.oRoll = this.roll;
        this.setSprite(sprites.get(this.random));

        Entity entity = level.getEntity(entityId);
        float width = entity != null ? entity.getBbWidth() : 1f;
        this.baseSize = 0.065f + width * 0.021f;
        this.quadSize = this.baseSize;

        float warm = 0.85f + this.random.nextFloat() * 0.15f;
        this.setColor(1f, warm, 0.55f + this.random.nextFloat() * 0.25f);

        this.follow(true);
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
        this.oRoll = this.roll;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        if (!this.fadingOut && !this.follow(false)) {
            this.fadingOut = true;
            this.lifetime = Math.min(this.lifetime, this.age + FADE_OUT_ON_WAKE);
        }

        this.roll += 0.12f;

        float fadeIn = Math.min(1f, this.age / (float) FADE_TICKS);
        float fadeOut = Math.min(1f, (this.lifetime - this.age) / (float) FADE_TICKS);
        float twinkle = 0.8f + 0.2f * Mth.sin(this.age * 0.55f + this.twinkleOffset);
        this.alpha = Mth.clamp(Math.min(fadeIn, fadeOut) * twinkle, 0f, 1f);
        this.quadSize = this.baseSize * (0.85f + 0.25f * Math.min(fadeIn, fadeOut)) * (0.92f + 0.08f * twinkle);
    }

    private boolean follow(boolean snap) {
        Entity entity = this.level.getEntity(this.entityId);
        if (!(entity instanceof OWEntity owEntity) || !owEntity.isAlive() || !owEntity.isKnockedOut()) return false;

        Vec3 head = owEntity.getSedationHeadPosition();
        float radius = 0.28f + owEntity.getBbWidth() * 0.12f;
        float angle = (this.level.getGameTime() % 72000L) * ORBIT_SPEED + this.phase;
        double px = head.x + Mth.cos(angle) * radius;
        double py = head.y + 0.3 + Mth.sin(angle) * radius * 0.28;
        double pz = head.z + Mth.sin(angle) * radius;
        this.setPos(px, py, pz);
        if (snap) {
            this.xo = px;
            this.yo = py;
            this.zo = pz;
        }
        return true;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType type, ClientLevel level,
                                                 double x, double y, double z, double entityId, double ringIndex, double unused) {
            return new DizzyStarParticle(level, x, y, z, this.sprites, (int) entityId, (int) ringIndex);
        }
    }
}
