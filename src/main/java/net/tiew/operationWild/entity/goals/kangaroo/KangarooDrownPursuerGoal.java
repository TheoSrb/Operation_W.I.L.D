package net.tiew.operationWild.entity.goals.kangaroo;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;

import java.util.EnumSet;

public class KangarooDrownPursuerGoal extends Goal {

    private static final double ENGAGE_RANGE = 3.0;

    private final KangarooEntity kangaroo;
    private LivingEntity victim;

    public KangarooDrownPursuerGoal(KangarooEntity kangaroo) {
        this.kangaroo = kangaroo;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!kangaroo.canStartDrowning()) return false;

        victim = findVictim();
        return victim != null;
    }

    @Override
    public boolean canContinueToUse() {
        return kangaroo.isDrownWindingUp() || kangaroo.isDrowningSomeone();
    }

    @Override
    public void start() {
        kangaroo.startDrownWindup(victim);
    }

    @Override
    public void stop() {
        kangaroo.cancelDrownWindup();
        if (kangaroo.isDrowningSomeone()) kangaroo.releaseDrownVictim();
        victim = null;
    }

    @Override
    public void tick() {
        LivingEntity held = kangaroo.getDrownVictim();
        if (held != null) {
            kangaroo.getLookControl().setLookAt(held, 30.0f, 30.0f);
            kangaroo.getNavigation().stop();
        }
    }

    private LivingEntity findVictim() {
        LivingEntity candidate = kangaroo.getLastHurtByMob();
        if (candidate == null || !candidate.isAlive()) candidate = kangaroo.getTarget();
        if (candidate == null || !candidate.isAlive()) return null;

        if (candidate.getType().is(EntityTypeTags.CAN_BREATHE_UNDER_WATER)) return null;
        if (candidate instanceof Player player && (player.isCreative() || player.isSpectator())) return null;
        if (kangaroo.isAlliedTo(candidate)) return null;
        if (!candidate.isInWater()) return null;
        if (kangaroo.distanceToSqr(candidate) > ENGAGE_RANGE * ENGAGE_RANGE) return null;

        return candidate;
    }
}
