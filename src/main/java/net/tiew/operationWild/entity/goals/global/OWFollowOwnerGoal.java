package net.tiew.operationWild.entity.goals.global;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.goals.AquaticPathNavigator;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class OWFollowOwnerGoal extends Goal {
    private final OWEntity tamable;
    @Nullable
    private LivingEntity owner;
    private final double speedModifier;
    private int timeToRecalcPath;
    private final float stopDistance;
    private final float startDistance;
    private float oldWaterCost;

    public OWFollowOwnerGoal(OWEntity entity, double speed, float distanceMin, float distanceMax) {
        this.tamable = entity;
        this.speedModifier = speed;
        this.startDistance = distanceMin;
        this.stopDistance = distanceMax;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    /**
     * Navigation <b>courante</b> de la bête, relue à chaque usage plutôt que retenue à la construction.
     *
     * <p>Un amphibie change de navigateur en cours de route ({@code OWSemiWaterEntity#switchNavigation}
     * bascule entre sol et eau selon le milieu), et ces deux instances-là naissent <i>après</i>
     * l'enregistrement des goals. La référence figée pointait donc sur le navigateur d'origine, celui
     * que plus personne ne fait avancer : le chemin vers le maître était bien calculé, mais dans le
     * vide. Le crocodile fixait son propriétaire sans jamais faire un pas vers lui, et ne le
     * rejoignait que par téléportation, une fois passée la distance de rappel.</p>
     */
    private PathNavigation navigation() {
        return this.tamable.getNavigation();
    }

    public boolean canUse() {
        LivingEntity $$0 = this.tamable.getOwner();
        if ($$0 == null) {
            return false;
        } else if (!this.tamable.isFollowingOwner()) {
            return false;
        } else if (this.tamable.unableToMoveToOwner()) {
            return false;
        } else if (this.tamable.distanceToSqr($$0) < (double)(this.startDistance * this.startDistance)) {
            return false;
        } else {
            this.owner = $$0;
            return true;
        }
    }

    public boolean canContinueToUse() {
        if (this.owner == null) {
            return false;
        } else if (!this.tamable.isFollowingOwner()) {
            return false;
        } else if (this.navigation().isDone()) {
            return false;
        } else if (this.tamable.unableToMoveToOwner()) {
            return false;
        } else {
            return !(this.tamable.distanceToSqr(this.owner) <= (double)(this.stopDistance * this.stopDistance));
        }
    }

    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.tamable.getPathfindingMalus(PathType.WATER);
        this.tamable.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    public void stop() {
        this.owner = null;
        this.navigation().stop();
        this.tamable.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
        this.tamable.resetState();
    }

    public void tick() {
        if (this.owner == null) return;
        if (this.tamable.isInResurrection() || this.tamable.isBaby()) return;

        boolean $$0 = this.tamable.shouldTryTeleportToOwner();
        if (!$$0) {
            this.tamable.getLookControl().setLookAt(this.owner, 10.0F, (float)this.tamable.getMaxHeadXRot());
        }

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            if ($$0) {
                this.tamable.tryToTeleportToOwner();
            } else {
                this.navigation().moveTo(this.owner, this.speedModifier);
                this.tamable.setState(2);
            }

        }
    }
}