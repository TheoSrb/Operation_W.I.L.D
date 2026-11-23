package net.tiew.operationWild.entity.clan;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LionHunt {

    public enum Role { CENTER, WING }

    public enum Phase { APPROACH, DRIVE, STRIKE }

    private static final double SCAN_RADIUS = 40.0;
    private static final double PLAYER_SCAN_RADIUS = 24.0;
    private static final double PARTICIPANT_RADIUS = 48.0;
    private static final int MAX_HUNTERS = 5;
    private static final int BIG_MIN_HUNTERS = 3;
    private static final int BIG_MIN_HUNTERS_STARVING = 2;
    private static final float BIG_WIDTH = 1.8f;
    private static final float BIG_HEALTH = 60f;
    private static final float MIN_PREY_HEALTH = 4f;
    private static final long APPROACH_TIMEOUT = 900L;
    private static final long DRIVE_TIMEOUT = 300L;
    private static final long STRIKE_TIMEOUT = 1200L;
    private static final double LOSE_DISTANCE = 64.0;
    private static final long COOLDOWN_TICKS = 600L;
    private static final long FAILED_COOLDOWN_TICKS = 300L;
    private static final double MALE_JOIN_RANGE = 48.0;

    public static final double CENTER_OFFSET = 7.0;
    public static final double CENTER_SPREAD = 3.0;
    public static final double WING_ARC = 14.0;
    public static final double WING_BEHIND = 11.0;
    public static final double WING_SPREAD = 3.0;
    public static final double WING_READY_DISTANCE = 3.0;
    public static final double STRIKE_RANGE = 7.0;
    public static final double SOLO_STRIKE_RANGE = 10.0;
    public static final double PANIC_RANGE = 10.0;
    private static final double PLAYER_WATCH_COSINE = 0.82;
    private static final double MOB_WATCH_COSINE = 0.6;
    private static final double PLAYER_WATCH_RANGE = 40.0;
    private static final double MOB_WATCH_RANGE = 20.0;

    public final int preyId;
    public final boolean big;
    public final boolean playerPrey;
    public final long startTick;
    public final Vec3 ambushDir;
    public final Map<UUID, Role> roles = new HashMap<>();
    public final Map<UUID, Integer> slots = new HashMap<>();
    public final Set<UUID> wingsReady = new HashSet<>();
    public final Set<UUID> wingsPastArc = new HashSet<>();
    private Phase phase = Phase.APPROACH;
    private long phaseTick;
    private boolean finished;
    private boolean succeeded;
    private int centerCount;
    private int wingCount;

    private LionHunt(LivingEntity prey, boolean big, long now, Vec3 ambushDir) {
        this.preyId = prey.getId();
        this.big = big;
        this.playerPrey = prey instanceof Player;
        this.startTick = now;
        this.phaseTick = now;
        this.ambushDir = ambushDir;
    }

    public Phase phase() {
        return phase;
    }

    public boolean isFinished() {
        return finished;
    }

    public void succeed() {
        succeeded = true;
        finished = true;
    }

    @Nullable
    public Role roleOf(LionEntity lion) {
        return roles.get(lion.getUUID());
    }

    public int slotOf(LionEntity lion) {
        return slots.getOrDefault(lion.getUUID(), 0);
    }

    public boolean hasWings() {
        return wingCount > 0;
    }

    @Nullable
    public LivingEntity prey(ServerLevel level) {
        return level.getEntity(preyId) instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    public void strike(long now) {
        if (phase == Phase.STRIKE) return;
        phase = Phase.STRIKE;
        phaseTick = now;
    }

    public void markWingReady(LionEntity lion) {
        wingsReady.add(lion.getUUID());
    }

    public Vec3 lateral() {
        return new Vec3(-ambushDir.z, 0, ambushDir.x);
    }

    public Vec3 centerPoint(LivingEntity prey, LionEntity lion) {
        double spread = (slotOf(lion) - (centerCount - 1) * 0.5) * CENTER_SPREAD;
        return prey.position().add(ambushDir.scale(CENTER_OFFSET)).add(lateral().scale(spread));
    }

    public int wingSide(LionEntity lion) {
        return slotOf(lion) % 2 == 0 ? 1 : -1;
    }

    public Vec3 wingArcPoint(LivingEntity prey, LionEntity lion) {
        return prey.position().add(lateral().scale(wingSide(lion) * WING_ARC));
    }

    public Vec3 wingPoint(LivingEntity prey, LionEntity lion) {
        int rank = slotOf(lion) / 2;
        return prey.position().subtract(ambushDir.scale(WING_BEHIND)).add(lateral().scale(wingSide(lion) * (WING_SPREAD + rank * WING_SPREAD)));
    }

    public static boolean canHunt(LionClanData.Clan clan, LionPride.DayPhase phase) {
        return switch (LionPride.hunger(clan)) {
            case SATED -> false;
            case HUNGRY -> phase == LionPride.DayPhase.EVENING || phase == LionPride.DayPhase.NIGHT || phase == LionPride.DayPhase.DAWN;
            case STARVING -> true;
        };
    }

    public static void update(ServerLevel level, LionClanData.Clan clan, List<LionEntity> members, long now) {
        LionPrideState state = clan.state;
        if (state.hunt != null) {
            state.hunt.step(level, clan, members, now);
            if (state.hunt.finished) {
                state.nextHuntTick = now + (state.hunt.succeeded ? COOLDOWN_TICKS : FAILED_COOLDOWN_TICKS);
                state.hunt = null;
            }
            return;
        }
        if (now < state.nextHuntTick || state.isFeasting(now)) return;
        if (!canHunt(clan, LionPride.phase(level))) return;
        state.hunt = plan(level, clan, members, now);
    }

    public static boolean isHunterCandidate(LionClanData.Clan clan, LionEntity lion) {
        if (lion.isTame() || lion.isBaby() || lion.isKnockedOut() || lion.isSedatedFleeing() || lion.isVehicle()
                || lion.isPassenger() || lion.isLeashed() || lion.isLatchActive() || lion.wild.isConceding()) return false;
        if (lion.getTarget() != null || lion.isFleeingDanger() || lion.isSedationAgitated()) return false;
        return clan.isNomad() ? lion.isMale() : lion.isFemale();
    }

    @Nullable
    private static LionHunt plan(ServerLevel level, LionClanData.Clan clan, List<LionEntity> members, long now) {
        List<LionEntity> hunters = members.stream().filter(lion -> isHunterCandidate(clan, lion)).toList();
        if (hunters.isEmpty()) return null;

        Vec3 centroid = centroid(hunters);
        LionPride.Hunger hunger = LionPride.hunger(clan);
        int bigMinimum = hunger == LionPride.Hunger.STARVING ? BIG_MIN_HUNTERS_STARVING : BIG_MIN_HUNTERS;
        boolean bigAllowed = hunters.size() >= bigMinimum;

        LivingEntity prey = null;
        double bestScore = Double.MAX_VALUE;
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, new AABB(centroid, centroid).inflate(SCAN_RADIUS, 12, SCAN_RADIUS),
                entity -> isPrey(entity, hunters.get(0)))) {
            boolean candidateBig = isBig(candidate);
            if (candidateBig && !bigAllowed) continue;
            double score = candidate.position().distanceTo(centroid) * (candidateBig ? 1.5 : 1.0)
                    * (candidate.getHealth() < candidate.getMaxHealth() * 0.5f ? 0.7 : 1.0);
            if (score < bestScore) {
                bestScore = score;
                prey = candidate;
            }
        }
        if (hunger == LionPride.Hunger.STARVING) {
            for (ServerPlayer player : level.players()) {
                if (!LionPride.isIntrudable(player) || player.position().distanceTo(centroid) > PLAYER_SCAN_RADIUS) continue;
                double score = player.position().distanceTo(centroid) * 0.9;
                if (score < bestScore) {
                    bestScore = score;
                    prey = player;
                }
            }
        }
        if (prey == null) return null;

        LivingEntity chosen = prey;
        List<LionEntity> party = new ArrayList<>(hunters.stream()
                .filter(lion -> lion.distanceTo(chosen) <= PARTICIPANT_RADIUS)
                .sorted(Comparator.comparingDouble(lion -> lion.distanceToSqr(chosen)))
                .limit(MAX_HUNTERS)
                .toList());
        if (party.isEmpty()) return null;

        Vec3 toward = centroid(party).subtract(chosen.position());
        toward = new Vec3(toward.x, 0, toward.z);
        if (toward.lengthSqr() < 1.0E-4) toward = Vec3.directionFromRotation(0f, level.random.nextFloat() * 360f);
        LionHunt hunt = new LionHunt(chosen, isBig(chosen), now, toward.normalize());

        int centers = party.size() == 1 ? 1 : (party.size() + 1) / 2;
        for (int i = 0; i < party.size(); i++) {
            LionEntity lion = party.get(i);
            if (i < centers) {
                hunt.roles.put(lion.getUUID(), Role.CENTER);
                hunt.slots.put(lion.getUUID(), hunt.centerCount++);
            } else {
                hunt.roles.put(lion.getUUID(), Role.WING);
                hunt.slots.put(lion.getUUID(), hunt.wingCount++);
            }
            if (lion.isNapping()) lion.setNap(false);
            if (lion.isSitting()) lion.setSitting(false);
        }
        return hunt;
    }

    private static Vec3 centroid(List<LionEntity> lions) {
        double x = 0, y = 0, z = 0;
        for (LionEntity lion : lions) {
            x += lion.getX();
            y += lion.getY();
            z += lion.getZ();
        }
        int n = Math.max(1, lions.size());
        return new Vec3(x / n, y / n, z / n);
    }

    public static boolean isBig(LivingEntity entity) {
        return entity.getBbWidth() >= BIG_WIDTH || entity.getMaxHealth() >= BIG_HEALTH;
    }

    public static boolean isPrey(LivingEntity entity, LionEntity hunter) {
        if (!entity.isAlive() || !(entity instanceof Animal) || entity instanceof LionEntity) return false;
        if (entity.isInWater() || entity.isPassenger() || entity.isVehicle()) return false;
        if (entity.getMaxHealth() < MIN_PREY_HEALTH) return false;
        if (LionPride.isPredatorKind(entity)) return false;
        if (entity instanceof TamableAnimal tamable && tamable.getOwnerUUID() != null) return false;
        if (entity instanceof AbstractHorse horse && horse.isTamed()) return false;
        if (entity instanceof OWEntity owEntity && (owEntity.isTame() || owEntity.isKnockedOut())) return false;
        return !hunter.isAlliedTo(entity);
    }

    private void step(ServerLevel level, LionClanData.Clan clan, List<LionEntity> members, long now) {
        if (finished) return;
        LivingEntity prey = prey(level);
        if (prey == null || (prey instanceof Player player && !LionPride.isIntrudable(player))) {
            finished = true;
            return;
        }

        List<LionEntity> party = new ArrayList<>();
        for (LionEntity lion : members) {
            if (!roles.containsKey(lion.getUUID())) continue;
            if (lion.isTame() || lion.isKnockedOut() || lion.isSedatedFleeing() || lion.isVehicle() || lion.wild.isConceding()
                    || lion.distanceTo(prey) > LOSE_DISTANCE) {
                roles.remove(lion.getUUID());
                continue;
            }
            party.add(lion);
        }
        if (party.isEmpty()) {
            finished = true;
            return;
        }

        switch (phase) {
            case APPROACH -> {
                boolean timedOut = now - phaseTick > APPROACH_TIMEOUT;
                boolean alerted = prey.getLastHurtByMob() instanceof LionEntity || prey.hurtTime > 0;
                if (alerted) {
                    strike(now);
                } else if (hasWings()) {
                    long readyWings = party.stream().filter(lion -> roles.get(lion.getUUID()) == Role.WING && wingsReady.contains(lion.getUUID())).count();
                    long wings = party.stream().filter(lion -> roles.get(lion.getUUID()) == Role.WING).count();
                    if (wings == 0 || readyWings >= wings || timedOut) {
                        phase = wings == 0 ? Phase.STRIKE : Phase.DRIVE;
                        phaseTick = now;
                    }
                } else if (timedOut) {
                    strike(now);
                }
            }
            case DRIVE -> {
                if (now - phaseTick > DRIVE_TIMEOUT) strike(now);
            }
            case STRIKE -> {
                if (now - phaseTick > STRIKE_TIMEOUT) {
                    finished = true;
                    return;
                }
                for (LionEntity lion : party) {
                    if (lion.getTarget() == null) lion.huntTarget(prey);
                }
                if (big) recruitMale(level, clan, prey);
            }
        }
    }

    private void recruitMale(ServerLevel level, LionClanData.Clan clan, LivingEntity prey) {
        LionEntity male = LionPride.loadedLeader(level, clan);
        if (male == null || male.isFemale() || male.getTarget() != null || male.isKnockedOut() || male.isSedatedFleeing()
                || male.wild.isConceding() || male.distanceTo(prey) > MALE_JOIN_RANGE) return;
        male.huntTarget(prey);
    }

    public static boolean isWatchedBy(LivingEntity prey, LionEntity lion) {
        Vec3 eye = prey.getEyePosition();
        Vec3 toLion = lion.getEyePosition().subtract(eye);
        double distance = toLion.length();
        if (distance < 1.0E-3) return true;
        Vec3 direction = toLion.scale(1.0 / distance);
        if (prey instanceof Player player) {
            if (distance > PLAYER_WATCH_RANGE) return false;
            return player.getViewVector(1f).dot(direction) >= PLAYER_WATCH_COSINE && player.hasLineOfSight(lion);
        }
        if (distance > MOB_WATCH_RANGE) return false;
        Vec3 facing = Vec3.directionFromRotation(0f, prey.getYHeadRot());
        Vec3 flat = new Vec3(direction.x, 0, direction.z);
        if (flat.lengthSqr() < 1.0E-4) return true;
        return facing.dot(flat.normalize()) >= MOB_WATCH_COSINE && prey.hasLineOfSight(lion);
    }

    public static void panicAway(LivingEntity prey, LionEntity chaser) {
        if (!(prey instanceof PathfinderMob mob) || prey instanceof Player) return;
        Vec3 away = new Vec3(prey.getX() - chaser.getX(), 0, prey.getZ() - chaser.getZ());
        if (away.lengthSqr() < 1.0E-4) return;
        Vec3 goal = prey.position().add(away.normalize().scale(10.0));
        mob.getNavigation().moveTo(goal.x, goal.y, goal.z, 1.7);
    }
}
