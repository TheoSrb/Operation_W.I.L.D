package net.tiew.operationWild.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import org.jetbrains.annotations.Nullable;

/**
 * Goutte du jet de trompe : elle <b>voyage</b>.
 *
 * <p>Aucune particule vanilla ne réunit les deux propriétés nécessaires, et leur bytecode est
 * formel : {@code SPLASH} jette la vitesse qu'on lui passe puis pousse vers le haut,
 * {@code RAIN} l'ignore aussi et se contente de tomber sur place, {@code FISHING} la respecte mais
 * n'a aucune gravité et file donc tout droit. Faute de mieux, le jet était redessiné entier à
 * chaque tick — d'où une eau figée, une zone peinte plutôt qu'un jet.</p>
 *
 * <p>Celle-ci part de la trompe à sa vitesse de lancement et retombe d'elle-même. La forme de l'arc
 * n'est plus dessinée : elle est <i>parcourue</i>.</p>
 *
 * <p>Sa gravité est déduite de {@code WATER_SPRAY_DROOP} et de la vitesse de lancement, de sorte
 * que sa parabole soit exactement celle sur laquelle le serveur calcule les dégâts. Le frottement
 * est neutralisé : la moindre traînée courberait la trajectoire autrement.</p>
 */
public class WaterJetParticle extends TextureSheetParticle {

    protected WaterJetParticle(ClientLevel level, double x, double y, double z,
                               SpriteSet spriteSet, double vx, double vy, double vz) {
        // Constructeur SANS vitesse : celui qui la prend y ajoute une bonne dose d'aléatoire et
        // renormalise le tout, ce qui ruinerait la trajectoire. On la pose donc à la main.
        super(level, x, y, z);

        this.xd = vx;
        this.yd = vy;
        this.zd = vz;

        this.pickSprite(spriteSet);

        this.gravity = OWAttacksConstants.Elephant.WATER_SPRAY_PARTICLE_GRAVITY;
        this.friction = 1.0f;
        this.hasPhysics = true;

        this.lifetime = 24 + this.random.nextInt(22);
        this.quadSize = 0.09f + this.random.nextFloat() * 0.08f;

        this.rCol = 0.58f + this.random.nextFloat() * 0.12f;
        this.gCol = 0.78f + this.random.nextFloat() * 0.12f;
        this.bCol = 0.98f;
        this.alpha = 0.95f;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    private boolean impacted = false;
    private boolean collided = false;

    /**
     * Détecte l'obstacle en comparant le déplacement demandé à celui réellement effectué.
     *
     * <p>{@code Particle} tient bien un drapeau de collision, mais il est privé. On refait donc le
     * même constat depuis l'extérieur : si le pas rendu est plus court que celui réclamé, c'est que
     * quelque chose l'a arrêté.</p>
     */
    // Normale de la surface heurtée : elle pointe vers l'extérieur du bloc, dans le sens d'où la
    // goutte venait. C'est elle qui décolle l'éclaboussure de la paroi.
    private double normalX, normalY, normalZ;

    @Override
    public void move(double x, double y, double z) {
        double beforeX = this.x;
        double beforeY = this.y;
        double beforeZ = this.z;
        super.move(x, y, z);

        if (Math.abs((this.x - beforeX) - x) > 1.0E-5) {
            this.collided = true;
            this.normalX = x > 0 ? -1 : 1;
        }
        if (Math.abs((this.y - beforeY) - y) > 1.0E-5) {
            this.collided = true;
            this.normalY = y > 0 ? -1 : 1;
        }
        if (Math.abs((this.z - beforeZ) - z) > 1.0E-5) {
            this.collided = true;
            this.normalZ = z > 0 ? -1 : 1;
        }
    }

    @Override
    public void tick() {
        super.tick();

        // Elle s'efface sur la fin de sa course plutôt que de disparaître d'un coup.
        if (this.age > this.lifetime * 0.6f) {
            this.alpha = Math.max(0f, this.alpha - 2.4f / this.lifetime);
        }

        // La goutte a rencontré quelque chose : elle cède la place à une éclaboussure qui reste sur
        // la surface. Sans ce relais, la goutte s'arrêtait bien contre le bloc mais s'y éteignait
        // sans rien marquer, et le jet semblait s'évaporer au contact.
        if (!impacted && (this.onGround || this.collided)) {
            impacted = true;
            splatOnSurface();
            this.remove();
        }
    }

    /**
     * Part des gouttes qui laissent une trace.
     *
     * <p>Toutes ne marquent pas : arrosé contre un mur, le jet y envoie une soixantaine de gouttes
     * par tick, et autant d'éclaboussures durables saturerait l'écran autant que le moteur. Une sur
     * trois suffit à couvrir la surface, puisqu'elles s'accumulent pendant près de trois secondes.</p>
     */
    private static final float SPLAT_CHANCE = 0.34f;

    /**
     * Recul de l'éclaboussure hors de la paroi.
     *
     * <p>Une particule est un panneau <b>centré</b> sur sa position : posée pile sur la surface,
     * elle en a la moitié enfouie dans le bloc, et comme elle grandit encore en séchant, elle
     * finissait par ne plus se voir du tout. Ce recul vaut un peu plus que son demi-côté maximal,
     * de sorte qu'elle affleure sans jamais s'enfoncer.</p>
     */
    private static final double SPLAT_SURFACE_OFFSET = 0.26;

    private void splatOnSurface() {
        if (this.random.nextFloat() > SPLAT_CHANCE) return;

        // Aucune face n'a été relevée ce tick-ci (goutte simplement posée au sol) : c'est le
        // dessus d'un bloc, donc une normale vers le haut.
        double nx = normalX, ny = normalY, nz = normalZ;
        if (nx == 0 && ny == 0 && nz == 0) ny = 1;
        double length = Math.sqrt(nx * nx + ny * ny + nz * nz);
        nx /= length; ny /= length; nz /= length;

        int count = 1 + this.random.nextInt(3);
        for (int i = 0; i < count; i++) {
            // Dispersion DANS le plan de la paroi : un bruit sur les trois axes replongerait une
            // partie des éclaboussures dans le bloc qu'on vient de dégager.
            double sx = (this.random.nextDouble() - 0.5) * 0.45;
            double sy = (this.random.nextDouble() - 0.5) * 0.45;
            double sz = (this.random.nextDouble() - 0.5) * 0.45;
            double along = sx * nx + sy * ny + sz * nz;
            sx -= along * nx; sy -= along * ny; sz -= along * nz;

            this.level.addParticle(OWParticles.WATER_SPLAT_PARTICLE.get(),
                    this.x + sx + nx * SPLAT_SURFACE_OFFSET,
                    this.y + sy + ny * SPLAT_SURFACE_OFFSET,
                    this.z + sz + nz * SPLAT_SURFACE_OFFSET,
                    0, 0, 0);
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
            return new WaterJetParticle(level, x, y, z, spriteSet, vx, vy, vz);
        }
    }
}
