package net.tiew.operationWild.entity.goals.boa;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;

import java.util.EnumSet;

/**
 * Constriction du Boa NON monté : il s'approche d'une cible petite/moyenne, s'enroule
 * autour d'elle et la serre de plus en plus fort jusqu'à l'asphyxie.
 *
 * Découpage :
 *   - Ce Goal gère l'éligibilité, l'approche et la décision de saisie.
 *   - Une fois saisie (startConstrict), tout le serrage (maintien, dégâts croissants,
 *     asphyxie, timeout 10 s) et l'enroulement visuel sont pilotés dans BoaEntity.tick()
 *     (car le positionnement de la queue multipart y vit déjà).
 *
 * Le Boa relâche au bout de 10 s si la cible n'est pas morte (géré côté entité).
 */
public class BoaConstrictGoal extends Goal {

    private final BoaEntity boa;
    private LivingEntity target;

    public BoaConstrictGoal(BoaEntity boa) {
        this.boa = boa;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (boa.isVehicle()) return false;                 // pas si chevauché
        if (boa.isConstricting()) return false;
        if (boa.isSleeping() || boa.isNapping()) return false;
        if (boa.getConstrictCooldown() > 0) return false;
        LivingEntity t = boa.getTarget();
        return t != null && t.isAlive() && boa.canConstrict(t);
    }

    @Override
    public void start() {
        this.target = boa.getTarget();
    }

    @Override
    public boolean canContinueToUse() {
        // ⚠ isConstricting d'ABORD : quand on enroule un joueur, il chevauche le boa (isVehicle == true),
        // ce qui ne doit PAS interrompre le serrage.
        if (boa.isConstricting()) return true;             // serrage en cours : on continue
        if (boa.isVehicle()) return false;                 // chevauché hors serrage (monture) : on arrête
        if (boa.getConstrictCooldown() > 0) return false;  // relâchée sans la tuer (timeout) : cooldown armé → on arrête le goal
        return target != null && target.isAlive() && boa.canConstrict(target);
    }

    @Override
    public void tick() {
        if (target == null) return;

        if (boa.isConstricting()) {
            // Le serrage est piloté par l'entité ; on garde juste la navigation à l'arrêt.
            boa.getNavigation().stop();
            return;
        }

        // Phase d'approche.
        boa.getLookControl().setLookAt(target, 30f, 30f);
        boa.getNavigation().moveTo(target, 2);

        double range = OWAttacksConstants.Boa.CONSTRICT_ENGAGE_RANGE;
        if (boa.distanceToSqr(target) <= range * range && boa.getSensing().hasLineOfSight(target)) {
            boa.startConstrict(target);
        }
    }

    @Override
    public void stop() {
        if (boa.isConstricting()) boa.stopConstrict();
        this.target = null;
    }
}
