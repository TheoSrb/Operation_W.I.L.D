package net.tiew.operationWild.entity.goals.peacock;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.terrestrial.PeacockEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class PeacockAutoHealGoal extends Goal {

    private static final int SCAN_INTERVAL = 8;
    private static final double FEATHER_RANGE = 16.0;
    private static final float FEATHER_WOUNDED_RATIO = 0.85f;
    private static final float FEATHER_MIN_MISSING = 2f;
    private static final int FEATHER_RESERVE = 2;
    private static final float LINK_RECEIVER_RATIO = 0.5f;
    private static final float LINK_DONOR_RATIO = 0.8f;
    private static final float SWEEP_WOUNDED_RATIO = 0.6f;
    private static final int SWEEP_COOLDOWN_TICKS = OWAttacksConstants.Peacock.SWEEP_COOLDOWN_TICKS;

    private enum Action { NONE, SWEEP, LINK, FEATHER }

    private final PeacockEntity peacock;

    private Action action = Action.NONE;
    private LivingEntity patient;
    private OWEntity donor;
    private int scanCooldown;
    private long nextSweepTick;

    public PeacockAutoHealGoal(PeacockEntity peacock) {
        this.peacock = peacock;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        if (scanCooldown > 0) {
            scanCooldown--;
            return false;
        }
        scanCooldown = SCAN_INTERVAL;
        action = Action.NONE;
        patient = null;
        donor = null;

        if (!peacock.canAutoHealNow() || peacock.isSweeping() || peacock.isCombo() || peacock.isGliding()) return false;

        List<LivingEntity> allies = nearbyAllies(OWAttacksConstants.Peacock.SWEEP_SHARE_RADIUS);

        if (shouldSweep(allies)) {
            action = Action.SWEEP;
            return true;
        }

        if (!peacock.isLinking() && pickLink(allies)) {
            action = Action.LINK;
            return true;
        }

        if (peacock.getFeatherStock() > FEATHER_RESERVE && !peacock.isLinking()) {
            patient = pickFeatherPatient(allies);
            if (patient != null) {
                action = Action.FEATHER;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        switch (action) {
            case SWEEP -> {
                if (peacock.activateRoyalSweep()) {
                    nextSweepTick = peacock.level().getGameTime() + SWEEP_COOLDOWN_TICKS;
                }
            }
            case LINK -> {
                if (patient instanceof OWEntity receiver && donor != null) {
                    peacock.getLookControl().setLookAt(receiver, 30f, 30f);
                    peacock.startAutoLink(donor, receiver);
                }
            }
            case FEATHER -> {
                peacock.getLookControl().setLookAt(patient, 60f, 60f);
                peacock.autoShootFeatherAt(patient);
            }
            default -> { }
        }
        action = Action.NONE;
        patient = null;
        donor = null;
    }

    private List<LivingEntity> nearbyAllies(double radius) {
        return peacock.level().getEntitiesOfClass(LivingEntity.class, peacock.getBoundingBox().inflate(radius),
                e -> e.isAlive() && e != peacock && e.distanceTo(peacock) <= radius && peacock.isFeatherAlly(e)
                        && !(e instanceof Player player && (player.isCreative() || player.isSpectator())));
    }

    private static float ratio(LivingEntity entity) {
        return entity.getHealth() / entity.getMaxHealth();
    }

    private boolean shouldSweep(List<LivingEntity> allies) {
        if (peacock.level().getGameTime() < nextSweepTick) return false;
        if (peacock.getUltimateKillCount() < OWAttacksConstants.Peacock.SWEEP_KILLS_REQUIRED) return false;
        if (peacock.getVitalEnergy() > peacock.getVitalEnergyCapacity() - OWAttacksConstants.Peacock.SWEEP_ENERGY) return false;

        boolean wounded = ratio(peacock) < SWEEP_WOUNDED_RATIO;
        for (LivingEntity ally : allies) {
            if (ratio(ally) < SWEEP_WOUNDED_RATIO) {
                wounded = true;
                break;
            }
        }
        if (!wounded) return false;

        double radius = OWAttacksConstants.Peacock.SWEEP_RADIUS * peacock.getScale();
        int enemies = 0;
        for (LivingEntity other : peacock.level().getEntitiesOfClass(LivingEntity.class,
                peacock.getBoundingBox().inflate(radius, 2.0, radius), e -> e != peacock && e.isAlive())) {
            if (peacock.isAlliedTo(other) || peacock.isFeatherAlly(other) || peacock.hasPassenger(other)) continue;
            double dx = other.getX() - peacock.getX();
            double dz = other.getZ() - peacock.getZ();
            if (dx * dx + dz * dz > radius * radius) continue;
            if (other instanceof Player player && (player.isCreative() || player.isSpectator())) continue;
            if (!isHostile(other)) return false;
            enemies++;
        }
        return enemies > 0;
    }

    private boolean isHostile(LivingEntity other) {
        if (other instanceof Enemy) return true;
        if (other instanceof Mob mob && mob.getTarget() != null) {
            LivingEntity target = mob.getTarget();
            return target == peacock || peacock.isFeatherAlly(target);
        }
        return false;
    }

    private boolean pickLink(List<LivingEntity> allies) {
        if (peacock.isSecondaryOnCooldown() || peacock.isFeatherAiming()) return false;
        if (peacock.getVitalEnergy() > peacock.getVitalEnergyCapacity() - OWAttacksConstants.Peacock.LINK_START_ENERGY) return false;

        double reach = OWAttacksConstants.Peacock.LINK_MAX_DISTANCE - 4.0;
        OWEntity receiver = null;
        float worst = LINK_RECEIVER_RATIO;
        OWEntity giver = null;
        float best = LINK_DONOR_RATIO;

        for (LivingEntity ally : allies) {
            if (!(ally instanceof OWEntity beast) || beast.distanceTo(peacock) > reach) continue;
            if (!peacock.isLinkCandidate(beast)) continue;
            float r = ratio(beast);
            if (r < worst) {
                worst = r;
                receiver = beast;
            }
            if (r > best && PeacockEntity.canDonate(beast)) {
                best = r;
                giver = beast;
            }
        }
        if (receiver == null || giver == null || receiver == giver) return false;
        patient = receiver;
        donor = giver;
        return true;
    }

    private @Nullable LivingEntity pickFeatherPatient(List<LivingEntity> allies) {
        LivingEntity chosen = null;
        float worst = FEATHER_WOUNDED_RATIO;
        for (LivingEntity ally : allies) {
            if (ally.distanceTo(peacock) > FEATHER_RANGE) continue;
            if (ally.getMaxHealth() - ally.getHealth() < FEATHER_MIN_MISSING) continue;
            if (ally.getRootVehicle() == peacock) continue;
            if (!peacock.hasLineOfSight(ally)) continue;
            float r = ratio(ally);
            if (r >= worst) continue;
            worst = r;
            chosen = ally;
        }
        return chosen;
    }
}
