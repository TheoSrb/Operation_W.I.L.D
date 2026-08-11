package net.tiew.operationWild.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

/**
 * Éclaboussure laissée là où une goutte du jet a frappé.
 *
 * <p>Elle ne tombe pas et ne dérive pas : posée à l'impact, elle s'y étale et sèche sur place. La
 * goutte qui l'a engendrée, elle, disparaît — c'est le passage de relais qui donne l'impression que
 * l'eau <i>marque</i> ce qu'elle touche au lieu de le traverser.</p>
 *
 * <p>Elle ne produit aucun impact à son tour : sans quoi chaque éclaboussure en engendrerait
 * d'autres, indéfiniment.</p>
 */
public class WaterSplatParticle extends TextureSheetParticle {

    protected WaterSplatParticle(ClientLevel level, double x, double y, double z, SpriteSet spriteSet) {
        super(level, x, y, z);

        this.xd = 0;
        this.yd = 0;
        this.zd = 0;

        this.pickSprite(spriteSet);

        this.gravity = 0f;
        this.friction = 1f;
        // Elle est déjà posée sur la surface : la collision l'empêche seulement d'être poussée
        // dedans si le bloc bouge sous elle.
        this.hasPhysics = true;

        this.lifetime = 30 + this.random.nextInt(26);
        this.quadSize = 0.12f + this.random.nextFloat() * 0.10f;

        this.rCol = 0.60f + this.random.nextFloat() * 0.10f;
        this.gCol = 0.79f + this.random.nextFloat() * 0.10f;
        this.bCol = 0.98f;
        this.alpha = 0.85f;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        // Elle s'étale un peu en séchant, et s'efface sur la fin.
        this.quadSize += 0.0016f;
        if (this.age > this.lifetime * 0.45f) {
            this.alpha = Math.max(0f, this.alpha - 1.6f / this.lifetime);
        }
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
            return new WaterSplatParticle(level, x, y, z, spriteSet);
        }
    }
}
