package net.tiew.operationWild.entity.goals.colossal_squid;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.tiew.operationWild.entity.animals.aquatic.ColossalSquidEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;

import java.util.EnumSet;

public class ColossalSquidWildGrabGoal extends Goal {

    private static final int CHANCE_PER_TICK = 30;
    private static final double MIN_RANGE = 3.0;

    private final ColossalSquidEntity squid;

    public ColossalSquidWildGrabGoal(ColossalSquidEntity squid) {
        this.squid = squid;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.squid.isTame() || this.squid.isVehicle() || this.squid.isSleeping() || !this.squid.isInWater()) return false;
        if (this.squid.getGrabPhase() != ColossalSquidEntity.GRAB_NONE || this.squid.isCombo()) return false;
        LivingEntity target = this.squid.getTarget();
        if (target == null || !target.isAlive() || !target.isInWater()) return false;
        double distance = this.squid.distanceTo(target);
        if (distance < MIN_RANGE || distance > OWAttacksConstants.ColossalSquid.GRAB_RANGE * this.squid.getScale()) return false;
        if (!this.squid.getSensing().hasLineOfSight(target)) return false;
        return this.squid.getRandom().nextInt(CHANCE_PER_TICK) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        int phase = this.squid.getGrabPhase();
        return phase == ColossalSquidEntity.GRAB_PREPARE || phase == ColossalSquidEntity.GRAB_LUNGE;
    }

    @Override
    public void start() {
        LivingEntity target = this.squid.getTarget();
        if (target != null) this.squid.getLookControl().setLookAt(target, 30f, 30f);
        this.squid.startAbyssalGrab();
    }

    @Override
    public void tick() {
        LivingEntity target = this.squid.getTarget();
        if (target != null) this.squid.getLookControl().setLookAt(target, 30f, 30f);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
