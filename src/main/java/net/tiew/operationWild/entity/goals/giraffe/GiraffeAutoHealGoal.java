package net.tiew.operationWild.entity.goals.giraffe;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.entity.animals.terrestrial.GiraffeEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class GiraffeAutoHealGoal extends Goal {

    private static final int SCAN_INTERVAL = 10;

    private final GiraffeEntity giraffe;

    private int scanCooldown;
    private long nextSentinelTick;

    public GiraffeAutoHealGoal(GiraffeEntity giraffe) {
        this.giraffe = giraffe;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        if (scanCooldown > 0) {
            scanCooldown--;
            return false;
        }
        scanCooldown = SCAN_INTERVAL;

        if (!giraffe.canAutoHealNow() || giraffe.isCombo() || giraffe.isSentinelCasting()) return false;

        List<LivingEntity> allies = nearbyAllies();
        if (shouldRaiseSentinel(allies)) {
            if (giraffe.activateSentinel()) {
                nextSentinelTick = giraffe.level().getGameTime() + OWAttacksConstants.Giraffe.AUTO_SENTINEL_COOLDOWN_TICKS;
            }
            return false;
        }

        LivingEntity patient = pickPatient(allies);
        if (patient != null) {
            if (patient != giraffe) giraffe.getLookControl().setLookAt(patient, 30f, 30f);
            giraffe.autoBless(patient);
        }
        return false;
    }

    private List<LivingEntity> nearbyAllies() {
        double range = OWAttacksConstants.Giraffe.AUTO_HEAL_RANGE;
        return giraffe.level().getEntitiesOfClass(LivingEntity.class, giraffe.getBoundingBox().inflate(range),
                e -> e.isAlive() && e != giraffe && e.distanceTo(giraffe) <= range && giraffe.isBlessingCandidate(e)
                        && !(e instanceof Player player && (player.isCreative() || player.isSpectator())));
    }

    private static float ratio(LivingEntity entity) {
        return entity.getHealth() / entity.getMaxHealth();
    }

    private boolean shouldRaiseSentinel(List<LivingEntity> allies) {
        if (giraffe.isSentinelActive() || giraffe.level().getGameTime() < nextSentinelTick) return false;
        if (giraffe.getSentinelCharge() < OWAttacksConstants.Giraffe.SENTINEL_BLESSINGS_REQUIRED) return false;
        if (giraffe.getVitalEnergy() > giraffe.getVitalEnergyCapacity() - OWAttacksConstants.Giraffe.SENTINEL_ENERGY) return false;

        int wounded = 0;
        for (LivingEntity ally : allies) {
            float r = ratio(ally);
            if (r < OWAttacksConstants.Giraffe.AUTO_SENTINEL_SINGLE_THRESHOLD) return true;
            if (r < OWAttacksConstants.Giraffe.AUTO_SENTINEL_GROUP_THRESHOLD) wounded++;
        }
        return wounded >= 2;
    }

    private @Nullable LivingEntity pickPatient(List<LivingEntity> allies) {
        if (!giraffe.canBless()) return null;
        if (giraffe.getVitalEnergy() > giraffe.getVitalEnergyCapacity() - OWAttacksConstants.Giraffe.BLESSING_START_ENERGY) return null;

        LivingEntity chosen = null;
        float worst = OWAttacksConstants.Giraffe.AUTO_BLESS_THRESHOLD;
        for (LivingEntity ally : allies) {
            if (!giraffe.hasLineOfSight(ally)) continue;
            float r = ratio(ally);
            if (r >= worst) continue;
            worst = r;
            chosen = ally;
        }
        if (chosen == null && ratio(giraffe) < OWAttacksConstants.Giraffe.AUTO_BLESS_THRESHOLD) chosen = giraffe;
        return chosen;
    }
}
