package net.tiew.operationWild.entity.goals.kangaroo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;

import java.util.EnumSet;
import java.util.UUID;

public class KangarooAngerTargetGoal extends Goal {

    private static final double REACQUIRE_RANGE = 32.0;

    private final KangarooEntity kangaroo;
    private LivingEntity resolved;

    public KangarooAngerTargetGoal(KangarooEntity kangaroo) {
        this.kangaroo = kangaroo;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (kangaroo.isTame() || kangaroo.isSitting()) return false;
        if (!kangaroo.isAngry()) return false;
        if (kangaroo.getTarget() != null && kangaroo.getTarget().isAlive()) return false;

        UUID uuid = kangaroo.getPersistentAngerTarget();
        if (uuid == null) return false;
        if (!(kangaroo.level() instanceof ServerLevel serverLevel)) return false;
        if (!(serverLevel.getEntity(uuid) instanceof LivingEntity candidate)) return false;
        if (!candidate.isAlive()) return false;
        if (candidate instanceof Player player && (player.isCreative() || player.isSpectator())) return false;
        if (kangaroo.distanceToSqr(candidate) > REACQUIRE_RANGE * REACQUIRE_RANGE) return false;

        resolved = candidate;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        kangaroo.setTarget(resolved);
        resolved = null;
    }
}
