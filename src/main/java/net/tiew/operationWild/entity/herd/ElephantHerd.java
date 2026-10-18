package net.tiew.operationWild.entity.herd;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public final class ElephantHerd {

    public static final int MAX_SIZE = 8;
    public static final double MATRIARCH_RANGE = 64.0;
    public static final double JOIN_RANGE = 32.0;
    public static final double MERGE_RANGE = 16.0;
    public static final double MOURN_RANGE = 48.0;
    public static final double ALARM_RANGE = 28.0;
    public static final double MATRIARCH_ALARM_RANGE = 40.0;
    public static final int DEFENSE_TICKS = 600;
    public static final int FURY_TICKS = 1200;
    public static final int ALARM_COOLDOWN_TICKS = 1200;

    private static final int MAINTENANCE_INTERVAL = 40;
    private static final int ORPHAN_GRACE_TICKS = 200;

    private ElephantHerd() {
    }

    public static List<ElephantEntity> herdmates(ElephantEntity elephant, double range) {
        if (elephant.getHerdId() == null || elephant.isTame()) return List.of();
        double rangeSqr = range * range;
        return elephant.level().getEntitiesOfClass(ElephantEntity.class, elephant.getBoundingBox().inflate(range),
                other -> other != elephant && other.isAlive() && elephant.isSameHerd(other)
                        && other.distanceToSqr(elephant) <= rangeSqr);
    }

    public static boolean isEngaged(ElephantEntity elephant) {
        return elephant.getTarget() != null || elephant.wild.isDefending() || elephant.isAlarming()
                || elephant.getLastHurtByMob() != null && elephant.tickCount - elephant.getLastHurtByMobTimestamp() < 200;
    }

    public static boolean canAnswer(ElephantEntity elephant) {
        return elephant.isAlive() && !elephant.isTame() && !elephant.isSleeping()
                && !elephant.isKnockedOut() && !elephant.isSedatedFleeing() && !elephant.isVehicle();
    }

    public static boolean isValidThreat(ElephantEntity elephant, @Nullable LivingEntity threat) {
        if (threat == null || threat == elephant || !threat.isAlive() || threat.isRemoved()) return false;
        if (threat.level() != elephant.level() || threat.isInvulnerable()) return false;
        if (threat instanceof Player player && (player.isCreative() || player.isSpectator())) return false;
        if (threat instanceof ElephantEntity other && elephant.isSameHerd(other)) return false;
        return !elephant.isAlliedTo(threat);
    }

    public static boolean outranks(ElephantEntity candidate, ElephantEntity other) {
        if (candidate.isFemale() != other.isFemale()) return candidate.isFemale();
        int scale = Float.compare(candidate.getScale(), other.getScale());
        if (scale != 0) return scale > 0;
        int health = Float.compare(candidate.getBaseHealth(), other.getBaseHealth());
        if (health != 0) return health > 0;
        return candidate.getUUID().compareTo(other.getUUID()) < 0;
    }

    @Nullable
    public static ElephantEntity findMatriarch(ElephantEntity elephant) {
        UUID herdId = elephant.getHerdId();
        if (herdId == null) return null;
        if (elephant.isMatriarch()) return elephant;
        ElephantEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (ElephantEntity other : herdmates(elephant, MATRIARCH_RANGE)) {
            if (!other.isMatriarch()) continue;
            double distance = other.distanceToSqr(elephant);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = other;
            }
        }
        return best;
    }

    public static int size(ElephantEntity matriarch) {
        return herdmates(matriarch, MATRIARCH_RANGE).size() + 1;
    }

    public static void tick(ElephantEntity elephant) {
        if ((elephant.tickCount + elephant.getId()) % MAINTENANCE_INTERVAL != 0) return;
        if (elephant.isTame()) {
            elephant.leaveHerd();
            return;
        }
        if (isEngaged(elephant)) return;
        if (elephant.isMatriarch()) {
            tickMatriarch(elephant);
            return;
        }
        tickMember(elephant);
    }

    private static void tickMatriarch(ElephantEntity matriarch) {
        if (!matriarch.isFemale()) {
            ElephantEntity heir = null;
            for (ElephantEntity mate : herdmates(matriarch, JOIN_RANGE)) {
                if (!mate.isFemale() || !canAnswer(mate)) continue;
                if (heir == null || outranks(mate, heir)) heir = mate;
            }
            if (heir != null) {
                matriarch.setMatriarch(false);
                heir.becomeMatriarch();
                return;
            }
        }

        double rangeSqr = MERGE_RANGE * MERGE_RANGE;
        List<ElephantEntity> rivals = matriarch.level().getEntitiesOfClass(ElephantEntity.class,
                matriarch.getBoundingBox().inflate(MERGE_RANGE),
                other -> other != matriarch && other.isAlive() && !other.isTame() && other.isMatriarch()
                        && other.getHerdId() != null && !other.getHerdId().equals(matriarch.getHerdId())
                        && other.distanceToSqr(matriarch) <= rangeSqr && !isEngaged(other));
        for (ElephantEntity rival : rivals) {
            if (!outranks(rival, matriarch)) continue;
            if (size(rival) + size(matriarch) > MAX_SIZE) continue;
            matriarch.joinHerd(rival.getHerdId());
            return;
        }
    }

    private static void tickMember(ElephantEntity elephant) {
        if (findMatriarch(elephant) != null) {
            elephant.wild.setOrphanTicks(0);
            return;
        }

        int orphan = elephant.wild.getOrphanTicks() + MAINTENANCE_INTERVAL;
        elephant.wild.setOrphanTicks(orphan);
        if (elephant.getHerdId() != null && orphan < ORPHAN_GRACE_TICKS) return;

        ElephantEntity host = nearestOpenMatriarch(elephant);
        if (host != null) {
            elephant.joinHerd(host.getHerdId());
            return;
        }

        ElephantEntity best = elephant;
        for (ElephantEntity peer : herdmates(elephant, JOIN_RANGE)) {
            if (outranks(peer, best)) best = peer;
        }
        if (best == elephant) elephant.becomeMatriarch();
    }

    @Nullable
    private static ElephantEntity nearestOpenMatriarch(ElephantEntity elephant) {
        double rangeSqr = JOIN_RANGE * JOIN_RANGE;
        ElephantEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (ElephantEntity other : elephant.level().getEntitiesOfClass(ElephantEntity.class,
                elephant.getBoundingBox().inflate(JOIN_RANGE),
                other -> other != elephant && other.isAlive() && !other.isTame() && other.isMatriarch()
                        && other.getHerdId() != null && other.distanceToSqr(elephant) <= rangeSqr && !isEngaged(other))) {
            if (size(other) >= MAX_SIZE) continue;
            double distance = other.distanceToSqr(elephant);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = other;
            }
        }
        return best;
    }

    public static void onDeath(ElephantEntity elephant, DamageSource source) {
        if (elephant.getHerdId() == null || elephant.isTame()) return;

        List<ElephantEntity> mates = herdmates(elephant, MOURN_RANGE);
        LivingEntity killer = source.getEntity() instanceof LivingEntity living && living != elephant ? living : null;
        boolean wasMatriarch = elephant.isMatriarch();

        if (wasMatriarch) {
            ElephantEntity successor = null;
            for (ElephantEntity mate : mates) {
                if (successor == null || outranks(mate, successor)) successor = mate;
            }
            if (successor != null) successor.becomeMatriarch();
        }

        boolean fury = wasMatriarch && isValidThreat(elephant, killer);
        for (ElephantEntity mate : mates) {
            mate.wild.startMourning(elephant.position());
            if (killer != null && !(killer instanceof ElephantEntity other && mate.isSameHerd(other))) {
                mate.wild.rememberGrudge(killer);
            }
            if (fury && canAnswer(mate)) {
                mate.wild.answerAlarm(killer, FURY_TICKS, true);
                mate.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, FURY_TICKS, 0));
                mate.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, FURY_TICKS, 0));
            }
        }
    }

    public static void broadcastAlarm(ElephantEntity caller, @Nullable LivingEntity threat, double range) {
        long until = caller.level().getGameTime() + ALARM_COOLDOWN_TICKS;
        caller.wild.setAlarmCooldownUntil(until);
        boolean valid = isValidThreat(caller, threat);

        for (ElephantEntity mate : herdmates(caller, range)) {
            mate.wild.setAlarmCooldownUntil(until);
            if (valid && canAnswer(mate)) mate.wild.answerAlarm(threat, DEFENSE_TICKS, true);
        }
        if (valid && canAnswer(caller)) caller.wild.answerAlarm(threat, DEFENSE_TICKS, false);
    }

    public static boolean hasAnswerers(ElephantEntity caller, LivingEntity threat, double range) {
        for (ElephantEntity mate : herdmates(caller, range)) {
            if (canAnswer(mate) && mate.wild.getDefenseTarget() != threat) return true;
        }
        return false;
    }
}
