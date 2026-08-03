package net.tiew.operationWild.entity.goals.orca;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;

import java.util.EnumSet;

/**
 * Trou de respiration : prise sous la banquise, l'orque remonte percuter la glace par en dessous
 * et crève un passage définitif vers l'air libre.
 *
 * <p>Le trou reste ouvert : c'est une modification durable du monde, pas un effet. Une banquise
 * fréquentée par des orques finit criblée de brèches, qui servent ensuite à tout le monde — les
 * autres bêtes, et le joueur qui cherche par où sortir.</p>
 *
 * <p>La remontée est pilotée à la main plutôt que confiée à la navigation : l'évaluateur de nage
 * ne sait pas viser un bloc plein, et refuserait tout chemin menant à la glace qu'il s'agit
 * justement d'atteindre.</p>
 */
public class OWOrcaBreathingHoleGoal extends Goal {

    /** Vitesse de remontée sous la calotte. */
    private static final double RISE_SPEED = 0.22;

    /** Recentrage horizontal sous la brèche visée, borné pour rester une correction et non une ruée. */
    private static final double CENTERING_GAIN = 0.10;
    private static final double CENTERING_MAX = 0.25;

    /** Écart vertical sous la glace à partir duquel le coup part. */
    private static final double RAM_REACH = 0.35;

    /** Sécurité : une calotte inatteignable ne doit pas retenir l'orque indéfiniment. */
    private static final int MAX_CLIMB_TICKS = 220;

    private static final int COOLDOWN_MIN = 300;
    private static final int COOLDOWN_MAX = 700;

    private final OrcaEntity orca;
    private BlockPos ice;
    private int cooldown;
    private int climbTicks;

    public OWOrcaBreathingHoleGoal(OrcaEntity orca) {
        this.orca = orca;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        if (!isUsable()) return false;

        this.ice = this.orca.iceCapAbove();
        return this.ice != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.ice == null || this.climbTicks > MAX_CLIMB_TICKS) return false;
        if (!isUsable()) return false;
        // La calotte peut fondre, ou un autre l'avoir percée entre-temps.
        return OrcaEntity.isThinIce(this.orca.level().getBlockState(this.ice));
    }

    @Override
    public void start() {
        this.climbTicks = 0;
        this.orca.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.ice = null;
        this.cooldown = COOLDOWN_MIN + this.orca.getRandom().nextInt(COOLDOWN_MAX - COOLDOWN_MIN);
    }

    @Override
    public void tick() {
        if (this.ice == null) return;
        this.climbTicks++;

        double targetX = this.ice.getX() + 0.5;
        double targetZ = this.ice.getZ() + 0.5;
        // La brèche visée finit juste au-dessus de la tête à mesure que l'orque se recentre : lui
        // demander de la regarder revient alors à lui demander un cap sans direction.
        this.orca.lookAtUnlessOverhead(targetX, this.ice.getY(), targetZ);

        if (this.orca.getY() + this.orca.getBbHeight() >= this.ice.getY() - RAM_REACH) {
            breakHole();
            return;
        }

        this.orca.setDeltaMovement(
                Mth.clamp((targetX - this.orca.getX()) * CENTERING_GAIN, -CENTERING_MAX, CENTERING_MAX),
                RISE_SPEED,
                Mth.clamp((targetZ - this.orca.getZ()) * CENTERING_GAIN, -CENTERING_MAX, CENTERING_MAX));
        this.orca.hasImpulse = true;
    }

    /**
     * Crève la calotte : le bloc visé, plus les voisins orthogonaux encore gelés.
     *
     * <p>Une brèche d'un seul bloc sous une bête de plus de deux mètres de large ne se verrait pas,
     * et ne laisserait pas passer sa tête.</p>
     */
    private void breakHole() {
        Level level = this.orca.level();
        crack(level, this.ice);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos side = this.ice.relative(dir);
            if (OrcaEntity.isThinIce(level.getBlockState(side))) crack(level, side);
        }

        level.playSound(null, this.ice.getX() + 0.5, this.ice.getY(), this.ice.getZ() + 0.5,
                SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 1.4F, 0.6F);
        level.playSound(null, this.ice.getX() + 0.5, this.ice.getY(), this.ice.getZ() + 0.5,
                SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.NEUTRAL, 1.2F, 0.8F);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    this.ice.getX() + 0.5, this.ice.getY() + 0.4, this.ice.getZ() + 0.5,
                    40, 1.0, 0.2, 1.0, 0.3);
        }

        this.ice = null;
    }

    /**
     * Remplace un bloc de glace par de l'eau, avec l'effet de bris.
     *
     * <p>De l'eau et non du vide : la banquise repose sur la ligne de mer, si bien qu'un bloc laissé
     * vide ouvrirait un puits sec au milieu de l'océan. Rempli d'eau, il rétablit la surface libre
     * à son niveau, avec l'air juste au-dessus — soit exactement un trou de respiration.</p>
     */
    private void crack(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        level.levelEvent(2001, pos, Block.getId(state));
        level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
    }

    @Override
    public boolean isInterruptable() {
        return true;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private boolean isUsable() {
        return !this.orca.isTame()
                && !this.orca.isBaby()
                && !this.orca.isBeached()
                && !this.orca.isSleeping()
                && !this.orca.isVehicle()
                && !this.orca.isSpyhopping()
                && this.orca.isInWater();
    }
}
