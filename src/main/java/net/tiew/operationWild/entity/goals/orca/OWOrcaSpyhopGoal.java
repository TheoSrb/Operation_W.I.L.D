package net.tiew.operationWild.entity.goals.orca;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;

import java.util.EnumSet;
import java.util.List;

/**
 * Spyhopping : l'orque sauvage se dresse à la verticale pour observer ce qui se passe au-dessus de
 * la surface — un joueur, un bateau.
 *
 * <p>Le comportement <b>interrompt l'approche</b> au lieu d'attendre une accalmie. L'orque est
 * agressive de tempérament : elle prend pour cible tout joueur qui passe à portée, si bien qu'une
 * condition « aucune cible en cours » ne s'ouvrait jamais en survie — et ne semblait fonctionner
 * qu'en créatif, où le sélecteur de cibles ignore le joueur. C'est aussi le comportement réel :
 * l'orque jauge sa proie avant de charger, elle n'attend pas d'avoir renoncé.</p>
 *
 * <p>La cible n'est jamais retirée, seulement suspendue : ce goal est prioritaire sur la chasse et
 * lui prend le déplacement le temps de la dressée. Nuller la cible aurait laissé le goal de ciblage
 * tourner à vide sur sa référence de repli, sans jamais la réarmer — l'orque serait restée
 * définitivement inoffensive tant que le joueur ne se serait pas éloigné.</p>
 *
 * <p>Réservé aux orques sans propriétaire : une orque apprivoisée a mieux à faire, et son cavalier
 * n'apprécierait pas d'être planté à la verticale sans l'avoir demandé.</p>
 */
public class OWOrcaSpyhopGoal extends Goal {

    /** Portée à laquelle un objet d'intérêt attire l'attention. */
    private static final double WATCH_RANGE = 20.0;

    /** En deçà, l'orque est au contact : on n'interrompt pas un corps à corps pour observer. */
    private static final double MIN_RANGE = 6.0;

    /** Marge de rupture : l'observation ne s'arrête pas au premier pas de recul. */
    private static final double BREAK_RANGE = 30.0;

    /** Écart horizontal au carré en deçà duquel le cap vers l'observé n'est plus exploitable. */
    private static final double FLAT_LOOK_DEAD_ZONE = 4.0;

    private static final int HOLD_MIN = 50;
    private static final int HOLD_MAX = 95;

    /**
     * Temps de repos entre deux observations : de une à trois minutes.
     *
     * <p>Le spyhop est un moment, pas une habitude. À l'ancien rythme — treize à trente et une
     * secondes — une orque qui croisait la route du joueur passait le plus clair de son temps
     * dressée, et le geste perdait tout ce qui en faisait le prix.</p>
     */
    private static final int COOLDOWN_MIN = 1200;
    private static final int COOLDOWN_MAX = 3600;

    /**
     * Une chance sur autant, par tick où tout est réuni.
     *
     * <p>Le repos seul ne suffit pas : une fois écoulé, l'orque se dressait à la première seconde
     * où le joueur repassait à portée, donc toujours au même moment — à son approche. Ce tirage
     * étale le déclenchement sur quelques secondes d'éligibilité et lui rend son caractère
     * fortuit.</p>
     */
    private static final int TRIGGER_ODDS = 80;

    private final OrcaEntity orca;
    private Entity watched;
    private int cooldown;
    private int hurtStamp;

    public OWOrcaSpyhopGoal(OrcaEntity orca) {
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
        // Une orque qu'on vient de frapper ne s'arrête pas pour regarder.
        if (this.orca.getLastHurtByMob() != null) return false;
        if (this.orca.getRandom().nextInt(TRIGGER_ODDS) != 0) return false;

        this.watched = findWatchable();
        if (this.watched == null) return false;
        if (this.orca.distanceTo(this.watched) < MIN_RANGE) return false;
        return this.orca.canSpyhopHere();
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.orca.isSpyhopping()) return false;
        if (!isUsable()) return false;
        // Un coup reçu rompt l'observation sur-le-champ : c'est le prix de la fenêtre d'approche.
        if (this.orca.getLastHurtByMobTimestamp() != this.hurtStamp) return false;
        return this.watched != null
                && this.watched.isAlive()
                && this.orca.distanceTo(this.watched) <= BREAK_RANGE;
    }

    @Override
    public void start() {
        this.hurtStamp = this.orca.getLastHurtByMobTimestamp();
        this.orca.getNavigation().stop();
        this.orca.startSpyhop(HOLD_MIN + this.orca.getRandom().nextInt(HOLD_MAX - HOLD_MIN));
    }

    @Override
    public void stop() {
        this.orca.stopSpyhop();
        this.watched = null;
        this.cooldown = COOLDOWN_MIN + this.orca.getRandom().nextInt(COOLDOWN_MAX - COOLDOWN_MIN);
    }

    @Override
    public void tick() {
        if (this.watched == null) return;

        // La dressée ne se déplace pas : on referme toute route qu'un autre goal aurait rouverte,
        // sans quoi le pilotage de nage réoriente la bête pendant l'observation.
        if (!this.orca.getNavigation().isDone()) this.orca.getNavigation().stop();

        double dx = this.watched.getX() - this.orca.getX();
        double dz = this.watched.getZ() - this.orca.getZ();

        // Cible quasiment à l'aplomb — le cas d'un joueur posté sur la berge juste au-dessus.
        //
        // L'écart horizontal n'est alors plus que du bruit d'arrondi, et le cap qu'on en tire change
        // à chaque tick. Le contrôle de nage fait suivre le corps à la tête : l'orque tournait donc
        // sur elle-même sans fin au lieu de se dresser. On fige le cap et on ne règle plus que la
        // hauteur du regard, ce qui est exactement ce que le spyhop demande.
        if (dx * dx + dz * dz < FLAT_LOOK_DEAD_ZONE) {
            Vec3 ahead = Vec3.directionFromRotation(0f, this.orca.getYRot());
            this.orca.getLookControl().setLookAt(
                    this.orca.getX() + ahead.x * 4.0,
                    this.watched.getEyeY(),
                    this.orca.getZ() + ahead.z * 4.0);
            return;
        }

        this.orca.getLookControl().setLookAt(
                this.watched.getX(), this.watched.getEyeY(), this.watched.getZ());
    }

    @Override
    public boolean isInterruptable() {
        return true;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /** Conditions communes à l'entrée et au maintien : rien de tout cela ne doit changer en route. */
    private boolean isUsable() {
        return !this.orca.isTame()
                && !this.orca.isBaby()
                && !this.orca.isBeached()
                && !this.orca.isSleeping()
                && !this.orca.isVehicle()
                // Une orque prise dans une vague de chasse ne s'arrête pas pour regarder : la ligne
                // se défaisait à l'instant même où elle arrivait sur la proie. Le rang de priorité
                // suffirait, mais l'écrire ici évite que l'ordre d'enregistrement ne redevienne, un
                // jour, le seul rempart.
                && !this.orca.isWaveEngaged()
                && this.orca.isInWater();
    }

    /**
     * Ce qui mérite d'être observé : un joueur, ou un bateau — occupé ou non.
     *
     * <p>Les orques ibériques s'intéressent aux coques vides autant qu'à leurs occupants ; garder
     * les deux évite en prime qu'un joueur immobile dans son bateau ne soit jamais remarqué.</p>
     *
     * <p>Un joueur en créatif ou en spectateur n'existe pas pour la faune, comme partout ailleurs
     * dans le jeu : le sélecteur de cibles l'ignore déjà, la curiosité doit l'ignorer aussi.</p>
     */
    private Entity findWatchable() {
        AABB box = this.orca.getBoundingBox().inflate(WATCH_RANGE);
        List<Entity> candidates = this.orca.level().getEntities(this.orca, box,
                e -> (e instanceof Player player && !player.isSpectator() && !player.isCreative())
                        || e instanceof Boat);

        Entity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity candidate : candidates) {
            double dist = this.orca.distanceToSqr(candidate);
            if (dist < bestDist) {
                bestDist = dist;
                best = candidate;
            }
        }
        return best;
    }
}
