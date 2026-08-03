package net.tiew.operationWild.entity.goals.orca;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;

import java.util.EnumSet;
import java.util.List;

/**
 * Descente aux abysses — la Grande Gueule à l'état sauvage.
 *
 * <p>L'orque happe sa proie exactement comme sous les ordres d'un cavalier, puis fait ce qu'aucune
 * orque montée ne fait : elle pique droit vers le fond en la gardant en gueule, et ne la relâche
 * qu'une fois posée sur le sable.</p>
 *
 * <h2>Le danger n'est pas la mâchoire</h2>
 * <p>Tant qu'elle est tenue, la proie ne subit rien : ni morsure, ni asphyxie, ni pression. Toute la
 * menace est différée à l'instant du relâchement — la victime se retrouve intacte à quatre-vingts
 * blocs sous la surface, avec une réserve d'air pleine et une remontée à négocier. C'est un piège
 * qui ne tue pas, il déplace ; ce qu'il en coûte se paie ensuite, et cela vaut bien mieux qu'une
 * mort en gueule dont on n'aurait rien vu.</p>
 *
 * <p>Volontairement rare : quelques minutes de repos, et un tirage par-dessus. C'est une scène
 * qu'on doit raconter, pas un aléa de traversée.</p>
 */
public class OWOrcaAbyssalDiveGoal extends Goal {

    /** Portée de repérage d'une proie happable. */
    private static final double SEARCH_RANGE = 18.0;

    /** Distance à laquelle la happe est tentée — en deçà de la portée de gueule, qui vaut six. */
    private static final double BITE_RANGE = 4.2;

    /**
     * Hauteur d'eau libre exigée sous l'orque.
     *
     * <p>Sans elle, la manœuvre se déclencherait dans un lagon de six blocs de fond et n'aurait
     * aucun sens : la proie serait relâchée là où elle a pied.</p>
     *
     * <p>Ramenée de vingt-quatre à seize blocs. C'était en pratique le vrai verrou : un océan
     * vanilla creuse rarement plus d'une vingtaine de blocs sous la surface, si bien que la
     * condition n'était presque jamais remplie et que le reste des réglages ne servait à rien.
     * Seize blocs suffisent amplement à ce que la remontée coûte quelque chose.</p>
     */
    private static final double MIN_WATER_COLUMN = 16.0;

    /** Marge au-dessus du fond à laquelle la proie est rendue. */
    private static final double FLOOR_MARGIN = 2.5;

    private static final double DIVE_SPEED = 0.48;

    private static final int STALK_TIMEOUT = 220;
    private static final int BITE_TIMEOUT = 45;
    private static final int DIVE_TIMEOUT = 400;

    /**
     * Une chance sur autant, par tick où tout est réuni.
     *
     * <p>Resserré : la conjonction exigée — eau profonde, proie happable à portée, orque libre de
     * tout le reste — est déjà rare en elle-même, et un tirage trop sec par-dessus faisait de la
     * descente une curiosité qu'on pouvait ne jamais voir.</p>
     */
    private static final int TRIGGER_ODDS = 60;

    private static final int COOLDOWN_MIN = 700;
    private static final int COOLDOWN_MAX = 1800;

    private enum Phase { STALK, BITE, DIVE }

    private final OrcaEntity orca;
    private LivingEntity prey;
    private Phase phase = Phase.STALK;
    private int phaseTicks;
    private int cooldown;
    private boolean done;

    public OWOrcaAbyssalDiveGoal(OrcaEntity orca) {
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
        if (this.orca.getRandom().nextInt(TRIGGER_ODDS) != 0) return false;
        if (this.orca.waterColumnBelow() < MIN_WATER_COLUMN) return false;

        this.prey = findPrey();
        return this.prey != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.done || !isUsable()) return false;
        if (this.prey == null || !this.prey.isAlive()) return false;
        // Une fois la proie en gueule, seule la descente compte : elle peut s'éloigner de son point
        // de capture autant qu'il le faut.
        if (this.phase != Phase.DIVE && this.orca.distanceTo(this.prey) > SEARCH_RANGE + 8.0) return false;
        return this.phaseTicks <= timeoutFor(this.phase);
    }

    @Override
    public void start() {
        this.phase = Phase.STALK;
        this.phaseTicks = 0;
        this.done = false;
    }

    @Override
    public void stop() {
        // Interrompue en route, elle rend sa prise plutôt que de la garder indéfiniment : la durée
        // de prise a été rallongée pour la descente, et rien d'autre ne viendrait y mettre fin.
        if (this.orca.hasSwallowed()) this.orca.beginSpit();
        this.orca.setAbyssalHold(false);
        this.orca.getNavigation().stop();
        this.prey = null;
        this.cooldown = COOLDOWN_MIN + this.orca.getRandom().nextInt(COOLDOWN_MAX - COOLDOWN_MIN);
    }

    @Override
    public void tick() {
        if (this.prey == null) return;
        this.phaseTicks++;

        switch (this.phase) {
            case STALK -> tickStalk();
            case BITE -> tickBite();
            case DIVE -> tickDive();
        }
    }

    /** Se placer dans l'axe, à portée de gueule : la happe ne part que d'un cône étroit. */
    private void tickStalk() {
        this.orca.getLookControl().setLookAt(
                this.prey.getX(), this.prey.getEyeY(), this.prey.getZ());

        if (this.orca.distanceTo(this.prey) <= BITE_RANGE && this.orca.hasMouthTarget()) {
            this.orca.getNavigation().stop();
            this.orca.activateBigMouth();
            this.phase = Phase.BITE;
            this.phaseTicks = 0;
            return;
        }
        if (this.phaseTicks % 8 == 0) {
            this.orca.getNavigation().moveTo(
                    this.prey.getX(), this.prey.getY(), this.prey.getZ(), 1.5);
        }
    }

    /** La happe se joue toute seule côté entité ; on attend seulement de savoir si elle a pris. */
    private void tickBite() {
        this.orca.getLookControl().setLookAt(
                this.prey.getX(), this.prey.getEyeY(), this.prey.getZ());

        if (this.orca.hasSwallowed()) {
            this.orca.setAbyssalHold(true);
            this.orca.holdSwallowedFor(DIVE_TIMEOUT + 80);
            this.phase = Phase.DIVE;
            this.phaseTicks = 0;

            this.orca.level().playSound(null, this.orca.getX(), this.orca.getY(), this.orca.getZ(),
                    SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 2.0F, 0.5F);
            return;
        }
        // Mâchoire refermée sur du vide : la proie a bougé pendant l'armement.
        if (this.orca.getMouthLungeTicks() <= 0) this.done = true;
    }

    private void tickDive() {
        this.orca.getNavigation().stop();

        double seabed = this.orca.seabedYBelow();
        boolean touched = !Double.isNaN(seabed) && this.orca.getY() <= seabed + FLOOR_MARGIN;
        if (touched || this.phaseTicks >= DIVE_TIMEOUT || !this.orca.hasSwallowed()) {
            releaseAtDepth();
            return;
        }

        Vec3 mv = this.orca.getDeltaMovement();
        this.orca.setDeltaMovement(mv.x * 0.7, -DIVE_SPEED, mv.z * 0.7);
        this.orca.hasImpulse = true;

        if (this.orca.level() instanceof ServerLevel serverLevel && this.phaseTicks % 3 == 0) {
            serverLevel.sendParticles(ParticleTypes.BUBBLE,
                    this.orca.getX(), this.orca.getY() + this.orca.getBbHeight(), this.orca.getZ(),
                    8, 0.8, 0.4, 0.8, 0.02);
        }
        if (this.phaseTicks % 40 == 0) {
            this.orca.level().playSound(null, this.orca.getX(), this.orca.getY(), this.orca.getZ(),
                    SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 1.6F, 0.45F);
        }
    }

    /** Rendue au fond, intacte, avec tout le chemin du retour devant elle. */
    private void releaseAtDepth() {
        this.done = true;
        this.orca.setAbyssalHold(false);

        double x = this.orca.getX(), y = this.orca.getY(), z = this.orca.getZ();
        this.orca.beginSpit();

        this.orca.level().playSound(null, x, y, z,
                SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1.2F, 0.6F);
        if (this.orca.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.BUBBLE,
                    x, y, z, 60, 1.6, 0.8, 1.6, 0.12);
        }
    }

    private static int timeoutFor(Phase phase) {
        return switch (phase) {
            case STALK -> STALK_TIMEOUT;
            case BITE -> BITE_TIMEOUT;
            case DIVE -> DIVE_TIMEOUT + 40;
        };
    }

    private boolean isUsable() {
        return !this.orca.isTame()
                && !this.orca.isBaby()
                && !this.orca.isBeached()
                && !this.orca.isSleeping()
                && !this.orca.isVehicle()
                && !this.orca.isSpyhopping()
                && !this.orca.isWaveEngaged()
                && this.orca.isInWater();
    }

    /**
     * Une proie qui tient dans la gueule, dans l'eau, et qui n'est pas des nôtres.
     *
     * <p>{@code canSwallow} porte déjà l'essentiel du filtre — gabarit, créatif, spectateur, monture
     * — et c'est lui que la happe consultera de nouveau à la fermeture. Le répéter ici évite
     * d'engager une traque dont on sait d'avance qu'elle se refermera sur du vide.</p>
     */
    private LivingEntity findPrey() {
        AABB box = this.orca.getBoundingBox().inflate(SEARCH_RANGE);
        List<LivingEntity> candidates = this.orca.level().getEntitiesOfClass(
                LivingEntity.class, box,
                e -> e.isInWater()
                        && !(e instanceof OrcaEntity)
                        && !this.orca.isAlliedTo(e)
                        && this.orca.canSwallow(e));

        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity candidate : candidates) {
            double dist = this.orca.distanceToSqr(candidate);
            if (dist < bestDist) {
                bestDist = dist;
                best = candidate;
            }
        }
        return best;
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
