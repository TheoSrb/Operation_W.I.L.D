package net.tiew.operationWild.entity.goals.global;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.tiew.operationWild.entity.OWEntity;

import java.util.EnumSet;

public class OWSedationRetaliationGoal extends Goal {

    private final OWEntity mob;

    public OWSedationRetaliationGoal(OWEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        return mob.sedationResponse.getChargeTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        lockOn();
    }

    @Override
    public void tick() {
        lockOn();
    }

    private void lockOn() {
        LivingEntity provoker = mob.sedationResponse.getChargeTarget();
        if (provoker != null && mob.getTarget() != provoker) mob.setTarget(provoker);
    }
}
