package net.tiew.operationWild.entity.goals.orca;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;

import java.util.EnumSet;
import java.util.List;

/**
 * L'orque sauvage s'en prend à la coque plutôt qu'à ses occupants : elle percute le bateau par en
 * dessous jusqu'à le briser, et jette ses passagers à l'eau.
 *
 * <p>Comportement observé chez les orques ibériques, qui s'acharnent sur les gouvernails sans
 * s'intéresser aux marins. Le gain de jeu est là : la menace ne vise pas la barre de vie mais le
 * moyen de transport, et transforme une traversée en prise de risque. Une fois à l'eau, le joueur
 * redevient une proie ordinaire — la chasse habituelle reprend la main.</p>
 *
 * <p>Les dégâts passent par {@code Boat#hurt}, donc par le compteur d'endommagement vanilla : le
 * bateau encaisse, se fend, puis se brise en rendant son objet comme sous n'importe quel autre
 * coup. Rien n'est réimplémenté, et un bateau modé qui suit le contrat vanilla se casse pareil.</p>
 */
public class OWOrcaBoatStrikeGoal extends Goal {

    /** Portée de repérage d'une coque. */
    private static final double SEARCH_RANGE = 24.0;

    /** Distance à laquelle le coup part. */
    private static final double STRIKE_RANGE = 3.6;

    /** Au-delà, la poursuite est abandonnée : l'orque ne traverse pas l'océan pour une planche. */
    private static final double GIVE_UP_RANGE = 40.0;

    /** Temps entre deux percussions — la coque doit avoir le temps de tanguer. */
    private static final int STRIKE_INTERVAL = 34;

    /** Nombre de percussions avant que la coque ne cède. */
    private static final int STRIKES_TO_BREAK = 3;

    /**
     * Le compte des coups est tenu ici, et non laissé au compteur d'endommagement vanilla.
     *
     * <p>Celui-ci perd un point par tick : entre deux percussions espacées d'une seconde et demie,
     * il se vide plus vite qu'il ne se remplit et le seuil de rupture n'est jamais franchi — la
     * coque encaisserait indéfiniment. Les premiers coups ne servent donc qu'à la faire tanguer,
     * et le dernier porte de quoi dépasser le seuil d'un seul tenant.</p>
     */
    private static final float CHIP_DAMAGE = 1.5F;
    private static final float BREAK_DAMAGE = 12.0F;

    /** Le chemin n'est recalculé que par intervalles : une cible flottante bouge peu. */
    private static final int REPATH_INTERVAL = 10;

    private static final int COOLDOWN_MIN = 200;
    private static final int COOLDOWN_MAX = 460;

    /** Sécurité : une coque coincée sous un surplomb ne doit pas immobiliser l'orque indéfiniment. */
    private static final int MAX_PURSUIT_TICKS = 400;

    private final OrcaEntity orca;
    private Boat boat;
    private int cooldown;
    private int strikeDelay;
    private int pursuitTicks;
    private int strikesLanded;
    private int repathDelay;

    public OWOrcaBoatStrikeGoal(OrcaEntity orca) {
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

        this.boat = findBoat();
        return this.boat != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (!isUsable()) return false;
        if (this.pursuitTicks > MAX_PURSUIT_TICKS) return false;
        return this.boat != null
                && this.boat.isAlive()
                && this.orca.distanceTo(this.boat) <= GIVE_UP_RANGE;
    }

    @Override
    public void start() {
        this.strikeDelay = 0;
        this.pursuitTicks = 0;
        this.strikesLanded = 0;
        this.repathDelay = 0;
    }

    @Override
    public void stop() {
        this.boat = null;
        this.orca.getNavigation().stop();
        this.cooldown = COOLDOWN_MIN + this.orca.getRandom().nextInt(COOLDOWN_MAX - COOLDOWN_MIN);
    }

    @Override
    public void tick() {
        if (this.boat == null) return;
        this.pursuitTicks++;

        this.orca.getLookControl().setLookAt(this.boat.getX(), this.boat.getY(), this.boat.getZ());
        if (this.strikeDelay > 0) this.strikeDelay--;

        if (this.repathDelay > 0) this.repathDelay--;

        double distance = this.orca.distanceTo(this.boat);
        if (distance > STRIKE_RANGE) {
            if (this.repathDelay <= 0) {
                this.repathDelay = REPATH_INTERVAL;
                // Visée sous la coque : la cible de nage est posée sous le bateau, sinon le chemin
                // s'arrête au dernier bloc d'eau et l'orque tourne autour sans jamais remonter.
                this.orca.getNavigation().moveTo(
                        this.boat.getX(),
                        this.boat.getY() - this.orca.getBbHeight(),
                        this.boat.getZ(),
                        1.2);
            }
            return;
        }

        this.orca.getNavigation().stop();
        if (this.strikeDelay > 0) return;
        strike();
    }

    /** Percussion : poussée verticale de l'orque, secousse de la coque, gerbe d'écume. */
    private void strike() {
        this.strikeDelay = STRIKE_INTERVAL;
        this.strikesLanded++;
        boolean finalBlow = this.strikesLanded >= STRIKES_TO_BREAK;

        Vec3 push = new Vec3(
                this.boat.getX() - this.orca.getX(),
                0.0,
                this.boat.getZ() - this.orca.getZ());
        push = push.lengthSqr() > 1.0E-4 ? push.normalize() : this.orca.getLookAngle();

        this.orca.setDeltaMovement(push.x * 0.55, 0.42, push.z * 0.55);
        this.orca.hasImpulse = true;

        double boatX = this.boat.getX(), boatY = this.boat.getY(), boatZ = this.boat.getZ();

        this.boat.setDeltaMovement(this.boat.getDeltaMovement().add(push.x * 0.35, 0.32, push.z * 0.35));
        this.boat.hurtMarked = true;
        this.boat.hurt(this.orca.damageSources().mobAttack(this.orca),
                finalBlow ? BREAK_DAMAGE : CHIP_DAMAGE);

        this.orca.level().playSound(null, boatX, boatY, boatZ,
                SoundEvents.BOAT_PADDLE_WATER, SoundSource.NEUTRAL, 1.6F, 0.55F);
        this.orca.level().playSound(null, boatX, boatY, boatZ,
                SoundEvents.DOLPHIN_ATTACK, SoundSource.NEUTRAL, 1.2F, 0.6F);

        if (this.orca.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    boatX, boatY + 0.2, boatZ,
                    finalBlow ? 45 : 25, 0.8, 0.25, 0.8, 0.25);
        }
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

    /** La coque la plus proche, occupée ou non — les orques ibériques ne font pas le tri. */
    private Boat findBoat() {
        List<Boat> boats = this.orca.level().getEntitiesOfClass(
                Boat.class, this.orca.getBoundingBox().inflate(SEARCH_RANGE), Boat::isAlive);

        Boat best = null;
        double bestDist = Double.MAX_VALUE;
        for (Boat candidate : boats) {
            double dist = this.orca.distanceToSqr(candidate);
            if (dist < bestDist) {
                bestDist = dist;
                best = candidate;
            }
        }
        return best;
    }
}
