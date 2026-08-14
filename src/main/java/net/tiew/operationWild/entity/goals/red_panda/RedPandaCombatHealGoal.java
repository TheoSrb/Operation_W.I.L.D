package net.tiew.operationWild.entity.goals.red_panda;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity;
import net.tiew.operationWild.worldgen.dimension.OWDimensions;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class RedPandaCombatHealGoal extends Goal {

    private static final float WOUNDED_RATIO = 0.92f;
    private static final int SCAN_INTERVAL = 10;

    private final RedPandaEntity panda;

    private LivingEntity patient;
    private int scanCooldown;

    public RedPandaCombatHealGoal(RedPandaEntity panda) {
        this.panda = panda;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        if (scanCooldown > 0) {
            scanCooldown--;
            return false;
        }
        scanCooldown = SCAN_INTERVAL;

        if (!isAvailable()) return false;

        patient = findWounded();
        return patient != null;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        panda.getLookControl().setLookAt(patient, 30f, 30f);
        panda.throwHealSnackAt(patient);
        patient = null;
    }

    private boolean isAvailable() {
        return panda.isTame()
                && panda.isAlive()
                && !panda.isBaby()
                && !panda.isOnShoulder()
                && !panda.isSitting()
                && !panda.isPassenger()
                && !panda.isInResurrection()
                && panda.canThrowHealSnack()
                && panda.level().dimension() == OWDimensions.ARENA;
    }

    private @Nullable LivingEntity findWounded() {
        double range = OWAttacksConstants.RedPanda.HEAL_SNACK_RANGE;
        AABB box = panda.getBoundingBox().inflate(range);

        LivingEntity best = null;
        float worst = WOUNDED_RATIO;

        for (LivingEntity candidate : panda.level().getEntitiesOfClass(LivingEntity.class, box)) {
            if (candidate == panda || !candidate.isAlive()) continue;
            if (!panda.isHealAlly(candidate)) continue;

            float ratio = candidate.getHealth() / candidate.getMaxHealth();
            if (ratio >= worst) continue;

            worst = ratio;
            best = candidate;
        }

        if (best == null && panda.getHealth() / panda.getMaxHealth() < WOUNDED_RATIO) return panda;
        return best;
    }
}
