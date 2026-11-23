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
    private static final float AUTO_WOUNDED_RATIO = 0.8f;
    private static final float FEAST_WOUNDED_RATIO = 0.6f;
    private static final float FEAST_CRITICAL_RATIO = 0.35f;
    private static final int FEAST_MIN_PATIENTS = 2;
    private static final int SCAN_INTERVAL = 10;
    private static final double AUTO_RANGE = 16.0;

    private final RedPandaEntity panda;

    private LivingEntity patient;
    private boolean feast;
    private int scanCooldown;
    private long nextFeastTick;

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

        boolean arena = isArenaHealer();
        boolean auto = !arena && isAutoHealer();
        if (!arena && !auto) return false;

        feast = auto && shouldFeast();
        if (feast) return true;

        if (!panda.canThrowHealSnack()) return false;
        patient = findWounded(arena ? OWAttacksConstants.RedPanda.HEAL_SNACK_RANGE : AUTO_RANGE,
                arena ? WOUNDED_RATIO : AUTO_WOUNDED_RATIO, !arena);
        return patient != null;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        if (feast) {
            feast = false;
            if (panda.activateFeast()) {
                nextFeastTick = panda.level().getGameTime() + OWAttacksConstants.RedPanda.FEAST_COOLDOWN_TICKS;
            }
            return;
        }
        panda.getLookControl().setLookAt(patient, 30f, 30f);
        panda.throwHealSnackAt(patient);
        patient = null;
    }

    private boolean isArenaHealer() {
        return panda.isTame()
                && panda.isAlive()
                && !panda.isBaby()
                && !panda.isOnShoulder()
                && !panda.isSitting()
                && !panda.isPassenger()
                && !panda.isInResurrection()
                && panda.level().dimension() == OWDimensions.ARENA;
    }

    private boolean isAutoHealer() {
        return !panda.isOnShoulder() && panda.canAutoHealNow();
    }

    private boolean shouldFeast() {
        if (panda.isAuraActive() || panda.getFeastCastTimer() > 0) return false;
        if (panda.level().getGameTime() < nextFeastTick) return false;
        if (panda.getFeastCharge() < OWAttacksConstants.RedPanda.FEAST_SNACKS_REQUIRED) return false;
        if (panda.getVitalEnergy() > panda.getVitalEnergyCapacity() - OWAttacksConstants.RedPanda.FEAST_ENERGY) return false;

        double radius = OWAttacksConstants.RedPanda.FEAST_RADIUS;
        int patients = 0;
        boolean critical = false;
        for (LivingEntity candidate : panda.level().getEntitiesOfClass(LivingEntity.class,
                panda.getBoundingBox().inflate(radius))) {
            if (!candidate.isAlive() || candidate.distanceTo(panda) > radius) continue;
            if (candidate != panda && !panda.isHealAlly(candidate)) continue;
            float ratio = candidate.getHealth() / candidate.getMaxHealth();
            if (ratio < FEAST_WOUNDED_RATIO) patients++;
            if (ratio < FEAST_CRITICAL_RATIO) critical = true;
        }
        return patients >= FEAST_MIN_PATIENTS || critical;
    }

    private @Nullable LivingEntity findWounded(double range, float threshold, boolean needSight) {
        AABB box = panda.getBoundingBox().inflate(range);

        LivingEntity best = null;
        float worst = threshold;

        for (LivingEntity candidate : panda.level().getEntitiesOfClass(LivingEntity.class, box)) {
            if (candidate == panda || !candidate.isAlive()) continue;
            if (candidate.distanceTo(panda) > range) continue;
            if (!panda.isHealAlly(candidate)) continue;
            if (needSight && !panda.hasLineOfSight(candidate)) continue;

            float ratio = candidate.getHealth() / candidate.getMaxHealth();
            if (ratio >= worst) continue;

            worst = ratio;
            best = candidate;
        }

        if (best == null && panda.getHealth() / panda.getMaxHealth() < threshold) return panda;
        return best;
    }
}
