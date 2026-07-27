package net.tiew.operationWild.entity.goals.crocodile;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.sound.OWSounds;

import java.util.EnumSet;

/**
 * Le crocodile sauvage traîne sa proie jusqu'au point d'eau le plus proche pour l'y noyer.
 *
 * <p>Ce goal ne tournait tout simplement jamais avant : le crocodile qui tenait un joueur était
 * déclaré {@code isImmobile()}, ce qui coupe {@code serverAiStep()} et donc l'IA entière ; celui qui
 * tenait une créature voyait sa victime prise pour un cavalier, ce qui coupait {@code travel()}.
 * Dans les deux cas la bête restait figée sur place, la proie en gueule, sans jamais rejoindre
 * l'eau.</p>
 */
public class CrocodileGoToWaterWithFoodGoal extends Goal {

    private static final int SEARCH_RADIUS = 20;
    /** Ré-évaluation périodique de la cible : le point d'eau visé peut avoir été bouché ou vidé. */
    private static final int RESEARCH_INTERVAL = 60;
    private static final float DRAG_DAMAGE = 2.0f;

    private final CrocodileEntity crocodile;

    private BlockPos targetPos = null;
    private int researchTimer = 0;
    private int dragDamageTimer = 0;

    public CrocodileGoToWaterWithFoodGoal(CrocodileEntity crocodile) {
        this.crocodile = crocodile;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public void start() {
        targetPos = crocodile.crocodileBehaviorHandler.findNearestWaterSource(SEARCH_RADIUS);
        researchTimer = RESEARCH_INTERVAL;
        dragDamageTimer = 0;
        // Une bête qui emporte sa proie ne flâne pas : c'est le goal d'attaque qui levait ce
        // drapeau jusqu'ici, or il est justement mis en sommeil pendant une prise — le crocodile
        // rejoignait donc l'eau avec l'allure d'une promenade.
        crocodile.setRunning(true);
    }

    @Override
    public void stop() {
        crocodile.canGrabOnLand = false;
        crocodile.getNavigation().stop();
        crocodile.setRunning(false);
        targetPos = null;
    }

    @Override
    public void tick() {
        LivingEntity grabbed = this.crocodile.getGrabbedTarget();
        if (grabbed == null) return;

        if (this.crocodile.isInWater()) {
            // Arrivé : la noyade et la roulade prennent le relais, on lâche la navigation.
            this.crocodile.getNavigation().stop();
            return;
        }

        if (--researchTimer <= 0) {
            researchTimer = RESEARCH_INTERVAL;
            BlockPos found = crocodile.crocodileBehaviorHandler.findNearestWaterSource(SEARCH_RADIUS);
            if (found != null) targetPos = found;
        }

        if (targetPos != null) {
            // Regard porté sur l'eau visée, pas sur la proie : c'est le cap de la traînée, et la
            // victime est de toute façon placée dans l'axe du corps.
            this.crocodile.getLookControl().setLookAt(
                    targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, 30.0F, 30.0F);

            if (this.crocodile.canGrabOnLand) {
                // Traînée au sol : poussée directe plutôt que pathfinding, la bête rampe en
                // ligne droite vers l'eau sans lâcher sa prise.
                Vec3 waterPos = new Vec3(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
                Vec3 direction = waterPos.subtract(this.crocodile.position());
                if (direction.horizontalDistanceSqr() > 1.0E-4) {
                    Vec3 movement = direction.normalize().scale(0.075);
                    this.crocodile.setDeltaMovement(movement.x, this.crocodile.getDeltaMovement().y, movement.z);
                }
            } else {
                this.crocodile.getNavigation().moveTo(
                        targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, 1.2D);
            }
        }

        // Hors de l'eau, la proie s'écorche sur le trajet. Compteur propre au goal : indexer sur
        // le tickCount de l'entité faisait démarrer les dégâts à un instant arbitraire de la prise.
        if (++dragDamageTimer >= 20) {
            dragDamageTimer = 0;
            grabbed.invulnerableTime = 0;
            grabbed.hurt(this.crocodile.damageSources().mobAttack(this.crocodile), DRAG_DAMAGE);
            this.crocodile.level().playSound(null, this.crocodile.getX(), this.crocodile.getY(), this.crocodile.getZ(),
                    OWSounds.CROCODILE_HIT_1.get(), SoundSource.HOSTILE, 0.9f,
                    (float) OWUtils.generateRandomInterval(0.8, 1.0));
        }
    }

    @Override
    public boolean canUse() {
        // Réservé au crocodile sauvage : une prise apprivoisée est un maintien de dix secondes,
        // pas un enlèvement — la monture d'un joueur n'a pas à filer vers la mare de son propre chef.
        if (crocodile.isBaby() || crocodile.isTame()) return false;
        return this.crocodile.hasGrabSomething();
    }

    @Override
    public boolean canContinueToUse() {
        if (crocodile.isBaby() || crocodile.isTame()) return false;
        return this.crocodile.hasGrabSomething() && this.crocodile.getHealth() >= 10;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
