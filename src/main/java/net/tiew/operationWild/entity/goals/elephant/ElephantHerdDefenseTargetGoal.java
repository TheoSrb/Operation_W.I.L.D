package net.tiew.operationWild.entity.goals.elephant;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;

import java.util.EnumSet;

public class ElephantHerdDefenseTargetGoal extends Goal {

    private final ElephantEntity elephant;

    public ElephantHerdDefenseTargetGoal(ElephantEntity elephant) {
        this.elephant = elephant;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (elephant.isTame() || elephant.isVehicle()) return false;
        LivingEntity threat = elephant.wild.getDefenseTarget();
        return threat != null && !elephant.sedationResponse.blocksTarget(threat);
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
        LivingEntity threat = elephant.wild.getDefenseTarget();
        if (threat != null && elephant.getTarget() != threat) elephant.setTarget(threat);
    }
}
