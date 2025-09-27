package net.tiew.operationWild.entity.goals;

import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.item.crafting.Ingredient;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;

public class KodiakTemptGoal extends TemptGoal {
    private final KodiakEntity kodiak;

    public KodiakTemptGoal(KodiakEntity entity, double speedModifier, Ingredient items, boolean scaredByPlayerMovement) {
        super(entity, speedModifier, items, scaredByPlayerMovement);
        this.kodiak = entity;
    }

    @Override
    public boolean canUse() {
        if (this.kodiak.isNapping() || !this.kodiak.getFoodPick().isEmpty() || this.kodiak.isRolling()) {
            return false;
        }
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.kodiak.isNapping() || !this.kodiak.getFoodPick().isEmpty() || this.kodiak.isRolling()) {
            return false;
        }
        return super.canContinueToUse();
    }
}