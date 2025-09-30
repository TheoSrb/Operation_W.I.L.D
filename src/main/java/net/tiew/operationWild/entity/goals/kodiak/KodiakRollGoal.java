package net.tiew.operationWild.entity.goals.kodiak;

import net.minecraft.world.entity.ai.goal.Goal;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.sound.OWSounds;

import java.util.EnumSet;

public class KodiakRollGoal extends Goal {

    private final KodiakEntity kodiak;
    private final float rollFrequencyMultiplier;

    public KodiakRollGoal(KodiakEntity kodiak, float rollFrequencyMultiplier) {
        this.kodiak = kodiak;
        this.rollFrequencyMultiplier = rollFrequencyMultiplier;

        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    @Override
    public void start() {
        super.start();
        float pitch = (float) OWUtils.generateRandomInterval(0.8f, 1.1f);
        kodiak.setRolling(true);
        if (kodiak.getRandom().nextInt(2) == 0) {
            kodiak.playSound(OWSounds.KODIAK_MISC.get(), 1.5f, pitch);
        }
        if (kodiak.getFoodPick() != null && !kodiak.getFoodPick().isEmpty()) {
            kodiak.kodiakManagement.eatFoodInHisMouth(kodiak.getFoodPick());
        }
    }

    @Override
    public void stop() {
        super.stop();
        kodiak.kodiakAI.resetKodiakState();
    }

    @Override
    public boolean canUse() {
        return kodiak.getRandom().nextInt((int) ((kodiak.isDirty() ? 350 : 550) / rollFrequencyMultiplier)) == 0 && !kodiak.isTame() &&
                !kodiak.isDeadOrDying() &&
                kodiak.getTarget() == null &&
                !kodiak.isInWater() &&
                kodiak.onGround() &&
                kodiak.getHealth() > (kodiak.getMaxHealth() * 0.5f) &&
                !kodiak.isNapping() &&
                !kodiak.isSearchingInsideChest;
    }

    @Override
    public boolean canContinueToUse() {
        if (kodiak.isSearchingInsideChest) return false;
        return super.canContinueToUse();
    }
}