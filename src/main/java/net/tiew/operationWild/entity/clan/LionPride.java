package net.tiew.operationWild.entity.clan;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class LionPride {

    public enum DayPhase { MORNING, HEAT, EVENING, NIGHT, DAWN }

    public enum Hunger { SATED, HUNGRY, STARVING }

    public static final int BRAIN_INTERVAL = 20;
    private static final float BRAIN_SECONDS = BRAIN_INTERVAL / 20f;

    private static final long HEAT_START = 4000L;
    private static final long EVENING_START = 10000L;
    private static final long NIGHT_START = 13000L;
    private static final long DAWN_START = 23000L;

    public static final float SATED_RATIO = 0.6f;
    public static final float STARVING_RATIO = 0.2f;

    private static final double TERRITORY_BASE_RADIUS = 32.0;
    private static final double TERRITORY_RADIUS_PER_MEMBER = 6.0;
    private static final double TERRITORY_MAX_RADIUS = 80.0;
    public static final double CORE_FRACTION = 0.35;
    public static final double HUNT_ZONE_FRACTION = 0.7;
    private static final double REHOME_FACTOR = 2.5;
    private static final double TERRITORY_NOTICE_FACTOR = 1.15;

    public static final float INTRUSION_FULL = 100f;
    private static final float INTRUSION_WAKE = 50f;
    private static final float HUNT_ZONE_RATE = 6f;
    private static final float CORE_RATE = 15f;
    private static final float FEAST_RATE = 40f;
    private static final float DECAY_RATE = 8f;
    private static final float SNEAK_RATE_FACTOR = 0.4f;
    private static final float ESCORT_RATE_FACTOR = 3f;
    private static final float PERCH_RATE_FACTOR = 1.5f;
    private static final double DETECT_RANGE = 20.0;
    private static final double SLEEPER_DETECT_RANGE = 8.0;
    private static final double PERCH_DETECT_RANGE = 48.0;
    private static final double SNEAK_DETECT_FACTOR = 0.5;
    private static final double MOUNTED_DETECT_FACTOR = 1.4;
    private static final double WARN_RANGE = 20.0;
    private static final int WARN_TICKS = 60;
    private static final double ATTACK_JOIN_RANGE = 40.0;
    private static final double MALE_JOIN_RANGE = 16.0;
    private static final double FEAST_DEFENSE_RANGE = 8.0;
    private static final double CUB_RANGE = 16.0;
    private static final double ESCORT_RANGE = 12.0;
    public static final int DISTURBED_TICKS = 2400;
    private static final long INTRUDER_MEMORY = 200L;
    private static final double RIVAL_MALE_SCAN_FACTOR = 1.0;

    private static final long CHORUS_WINDOW = 600L;
    private static final int CHORUS_MAX_DELAY = 300;
    private static final float CHORUS_VOLUME = 6f;
    public static final float CHORUS_REPLY_VOLUME = 4.5f;
    private static final double CHORUS_REPLY_RANGE = 48.0;
    private static final double CHORUS_HEARING_RANGE = 160.0;
    private static final int CHORUS_FIRST_REPLY = 12;
    private static final int CHORUS_REPLY_SPACING = 7;
    private static final long RIVAL_TICKS = 6000L;
    private static final double CHORUS_PUSH_DISTANCE = 20.0;
    private static final double NOMAD_AVOID_DISTANCE = 48.0;
    private static final long NOMAD_AVOID_TICKS = 2400L;

    private static final float KILL_SATIETY_PER_HEALTH = 500f;
    private static final int KILL_SATIETY_MIN = 1500;
    private static final int KILL_SATIETY_MAX = 30000;
    private static final int MEAT_SATIETY_PER_NUTRITION = 1500;
    private static final int MEAT_DEFAULT_NUTRITION = 2;
    private static final long FEAST_TICKS = 1200L;
    public static final long FEAST_REFRESH_TICKS = 400L;

    private static final float CONCEDE_HEALTH_RATIO = 0.25f;
    private static final int CONCEDE_FLEE_TICKS = 1200;

    private LionPride() {
    }

    public static DayPhase phase(Level level) {
        long time = Math.floorMod(level.getDayTime(), 24000L);
        if (time < HEAT_START) return DayPhase.MORNING;
        if (time < EVENING_START) return DayPhase.HEAT;
        if (time < NIGHT_START) return DayPhase.EVENING;
        if (time < DAWN_START) return DayPhase.NIGHT;
        return DayPhase.DAWN;
    }

    public static Hunger hunger(LionClanData.Clan clan) {
        float ratio = clan.satietyRatio();
        if (ratio >= SATED_RATIO) return Hunger.SATED;
        if (ratio >= STARVING_RATIO) return Hunger.HUNGRY;
        return Hunger.STARVING;
    }

    public static double territoryRadius(LionClanData.Clan clan) {
        return Math.min(TERRITORY_MAX_RADIUS, TERRITORY_BASE_RADIUS + TERRITORY_RADIUS_PER_MEMBER * clan.size());
    }

    public static boolean isDisturbed(LionClanData.Clan clan, long gameTime) {
        return gameTime < clan.state.disturbedUntil;
    }

    public static float aggression(LionEntity lion, @Nullable LionClanData.Clan clan, long gameTime) {
        float value = lion.isFemale() ? 1.3f : 1.0f;
        value *= switch (phase(lion.level())) {
            case MORNING -> 0.8f;
            case HEAT -> 0.5f;
            case EVENING -> 1.2f;
            case NIGHT -> 1.5f;
            case DAWN -> 1.0f;
        };
        if (clan != null) {
            value *= switch (hunger(clan)) {
                case SATED -> 0.7f;
                case HUNGRY -> 1.2f;
                case STARVING -> 1.8f;
            };
            if (isDisturbed(clan, gameTime)) value *= 2f;
        }
        if (hasCubNearby(lion, lion.position())) value *= 2f;
        return Mth.clamp(value, 0.3f, 6f);
    }

    private static boolean hasCubNearby(LionEntity lion, Vec3 around) {
        return !lion.level().getEntitiesOfClass(LionEntity.class, new AABB(around, around).inflate(CUB_RANGE),
                cub -> cub.isAlive() && cub.isBaby() && !cub.isTame()).isEmpty();
    }

    public static Vec3 homeCenter(LionClanData.Clan clan) {
        return clan.home == null ? Vec3.ZERO : Vec3.atBottomCenterOf(clan.home);
    }

    public static double horizontalDistance(Vec3 a, Vec3 b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static boolean isInTerritory(LionClanData.Clan clan, Vec3 position, double fraction) {
        return clan.home != null && horizontalDistance(position, homeCenter(clan)) <= territoryRadius(clan) * fraction;
    }

    public static List<LionEntity> loadedMembers(ServerLevel level, LionClanData.Clan clan) {
        List<LionEntity> members = new ArrayList<>(clan.size());
        for (UUID male : clan.males()) addLoaded(level, male, members);
        for (UUID lioness : clan.lionesses) addLoaded(level, lioness, members);
        return members;
    }

    private static void addLoaded(ServerLevel level, UUID id, List<LionEntity> into) {
        if (level.getEntity(id) instanceof LionEntity lion && lion.isAlive()) into.add(lion);
    }

    @Nullable
    public static LionEntity loadedLeader(ServerLevel level, LionClanData.Clan clan) {
        return level.getEntity(clan.male) instanceof LionEntity lion && lion.isAlive() ? lion : null;
    }

    public static boolean isAwake(LionEntity lion) {
        return !lion.isNapping() && !lion.isSleeping() && !lion.isKnockedOut();
    }

    public static void tick(ServerLevel level, LionClanData.Clan clan, LionClanData data) {
        if (clan.tamed) return;
        long now = level.getGameTime();
        LionPrideState state = clan.state;
        if (now - state.lastBrainTick < BRAIN_INTERVAL) return;
        state.lastBrainTick = now;

        List<LionEntity> members = loadedMembers(level, clan);
        if (members.isEmpty()) return;

        data.feed(clan, -BRAIN_INTERVAL);
        maintainHome(clan, data, members);
        if (state.rival != null && now >= state.rivalUntil) state.clearRival();
        if (state.feastPos != null && now >= state.feastUntil) {
            state.feastPos = null;
            state.maleShare = 0;
        }

        if (clan.home != null) {
            evaluateIntruders(level, clan, members, now);
            detectRivalMales(level, clan, members, now);
        }
        LionHunt.update(level, clan, members, now);
        scheduleChorus(level, clan, now);
    }

    private static void maintainHome(LionClanData.Clan clan, LionClanData data, List<LionEntity> members) {
        if (clan.isNomad()) {
            if (clan.home != null) data.relocate(clan, null);
            return;
        }
        LionEntity anchor = members.stream().filter(lion -> lion.getUUID().equals(clan.male)).findFirst().orElse(members.get(0));
        if (clan.home == null) {
            data.relocate(clan, anchor.blockPosition());
            return;
        }
        if (anchor.getTarget() == null && horizontalDistance(anchor.position(), homeCenter(clan)) > territoryRadius(clan) * REHOME_FACTOR) {
            data.relocate(clan, anchor.blockPosition());
        }
    }

    private static void evaluateIntruders(ServerLevel level, LionClanData.Clan clan, List<LionEntity> members, long now) {
        LionPrideState state = clan.state;
        double radius = territoryRadius(clan);
        Vec3 home = homeCenter(clan);
        LionEntity leader = loadedLeader(level, clan);
        boolean perched = leader != null && leader.wild.perchedWatch && isAwake(leader);
        DayPhase phase = phase(level);
        Set<UUID> seen = new HashSet<>();

        for (ServerPlayer player : level.players()) {
            if (!isIntrudable(player)) continue;
            double distance = horizontalDistance(player.position(), home);
            if (distance > radius * TERRITORY_NOTICE_FACTOR) continue;
            seen.add(player.getUUID());

            LionEntity spotter = null;
            double best = Double.MAX_VALUE;
            boolean anyAsleep = false;
            for (LionEntity lion : members) {
                if (lion.isKnockedOut() || lion.isSedatedFleeing() || lion.isVehicle()) continue;
                boolean asleep = lion.isNapping() || lion.isSleeping();
                double range = asleep ? SLEEPER_DETECT_RANGE : DETECT_RANGE * Mth.clamp(lion.wild.aggression(), 0.6f, 2f);
                if (lion == leader && perched) range = Math.max(range, PERCH_DETECT_RANGE);
                if (player.isShiftKeyDown()) range *= SNEAK_DETECT_FACTOR;
                if (player.isPassenger()) range *= MOUNTED_DETECT_FACTOR;
                double gap = lion.distanceTo(player);
                if (gap > range) continue;
                if (!asleep && !lion.getSensing().hasLineOfSight(player)) continue;
                if (gap < best) {
                    best = gap;
                    spotter = lion;
                    anyAsleep = asleep;
                }
            }

            float gauge = state.intrusion.getOrDefault(player.getUUID(), 0f);
            if (spotter == null) {
                state.intrusion.put(player.getUUID(), Math.max(0f, gauge - DECAY_RATE * BRAIN_SECONDS));
                continue;
            }

            boolean core = distance <= radius * CORE_FRACTION;
            boolean huntZone = distance <= radius * HUNT_ZONE_FRACTION;
            boolean nearFeast = state.isFeasting(now) && player.position().distanceTo(Vec3.atCenterOf(state.feastPos)) <= FEAST_DEFENSE_RANGE;
            float rate = core ? CORE_RATE : huntZone ? HUNT_ZONE_RATE : 0f;
            if (nearFeast) rate = Math.max(rate, FEAST_RATE);
            rate *= spotter.wild.aggression();
            if (player.isShiftKeyDown()) rate *= SNEAK_RATE_FACTOR;
            if (hasPredatorEscort(player)) rate *= ESCORT_RATE_FACTOR;
            if (spotter == leader && perched) rate *= PERCH_RATE_FACTOR;
            if (anyAsleep) rate *= 0.5f;

            if (core && (phase == DayPhase.NIGHT || hasCubNearby(spotter, player.position()))) {
                gauge = INTRUSION_FULL;
            } else if (rate > 0f) {
                gauge = Math.min(INTRUSION_FULL, gauge + rate * BRAIN_SECONDS);
            } else {
                gauge = Math.max(0f, gauge - DECAY_RATE * 0.5f * BRAIN_SECONDS);
            }
            state.intrusion.put(player.getUUID(), gauge);

            if (gauge >= INTRUSION_WAKE && members.stream().anyMatch(LionEntity::isNapping)) disturb(level, clan, now);

            if (gauge >= INTRUSION_FULL) {
                designate(clan, members, player, core, now);
            } else {
                for (LionEntity lion : members) {
                    if (!isAwake(lion) || lion.getTarget() != null || lion.distanceTo(player) > WARN_RANGE) continue;
                    if (!lion.getSensing().hasLineOfSight(player)) continue;
                    lion.wild.warn(player, WARN_TICKS);
                }
            }
        }

        Iterator<Map.Entry<UUID, Float>> iterator = state.intrusion.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Float> entry = iterator.next();
            if (seen.contains(entry.getKey())) continue;
            float value = entry.getValue() - DECAY_RATE * BRAIN_SECONDS;
            if (value <= 0f) iterator.remove();
            else entry.setValue(value);
        }
    }

    private static void designate(LionClanData.Clan clan, List<LionEntity> members, Player player, boolean core, long now) {
        for (LionEntity lion : members) {
            if (lion.isKnockedOut() || lion.isBaby() || lion.isVehicle()) continue;
            double gap = lion.distanceTo(player);
            boolean joins = lion.isFemale() ? gap <= ATTACK_JOIN_RANGE : (core || gap <= MALE_JOIN_RANGE);
            if (!joins) continue;
            if (lion.isNapping()) lion.setNap(false);
            lion.wild.designateIntruder(player, now + INTRUDER_MEMORY);
        }
        clan.state.disturbedUntil = Math.max(clan.state.disturbedUntil, now + DISTURBED_TICKS / 2);
    }

    public static boolean isIntrudable(Player player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator();
    }

    public static boolean hasPredatorEscort(Player player) {
        Entity mount = player.getRootVehicle();
        if (mount != player && mount instanceof LivingEntity living && isPredatorKind(living)) return true;
        return !player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(ESCORT_RANGE),
                entity -> entity instanceof TamableAnimal tamable && player.getUUID().equals(tamable.getOwnerUUID())
                        && isPredatorKind(entity)).isEmpty();
    }

    public static boolean isPredatorKind(LivingEntity entity) {
        return entity instanceof TigerEntity || entity instanceof KodiakEntity || entity instanceof CrocodileEntity
                || entity instanceof BoaEntity || entity instanceof Wolf
                || (entity instanceof LionEntity lion && lion.isTame());
    }

    public static boolean isRivalPredator(LionEntity lion, LivingEntity entity) {
        if (!entity.isAlive() || !isPredatorKind(entity) || lion.isClanmate(entity)) return false;
        if (entity instanceof OWEntity owEntity && (owEntity.isKnockedOut() || owEntity.isInResurrection())) return false;
        if (entity instanceof CrocodileEntity && entity.isInWater()) return false;
        if (entity instanceof LionEntity other && other.isBaby()) return false;
        return true;
    }

    public static void disturb(ServerLevel level, LionClanData.Clan clan, long now) {
        clan.state.disturbedUntil = now + DISTURBED_TICKS;
        for (LionEntity lion : loadedMembers(level, clan)) {
            if (lion.isNapping()) lion.setNap(false);
            if (lion.isSitting() && !lion.isTame()) lion.setSitting(false);
        }
    }

    private static void detectRivalMales(ServerLevel level, LionClanData.Clan clan, List<LionEntity> members, long now) {
        if (clan.isNomad()) return;
        double radius = territoryRadius(clan) * RIVAL_MALE_SCAN_FACTOR;
        Vec3 home = homeCenter(clan);
        List<LionEntity> intruders = level.getEntitiesOfClass(LionEntity.class, new AABB(home, home).inflate(radius, 16, radius),
                other -> isChallenger(clan, other, now) && horizontalDistance(other.position(), home) <= radius);
        if (intruders.isEmpty()) return;
        if (members.stream().anyMatch(lion -> lion.isMale() && (lion.isNapping() || lion.isSitting()))) disturb(level, clan, now);
    }

    public static boolean isChallenger(LionClanData.Clan clan, LionEntity other, long now) {
        if (!other.isAlive() || !other.isMale() || other.isBaby() || other.isTame() || other.wild.isConceding()) return false;
        UUID otherClan = other.getClanId();
        if (otherClan == null || otherClan.equals(clan.id)) return false;
        if (clan.state.hasRival(now) && otherClan.equals(clan.state.rival)) return true;
        LionClanData data = LionClanData.get(other.getServer());
        LionClanData.Clan theirs = data.getClan(otherClan);
        return theirs != null && theirs.isNomad();
    }

    private static void scheduleChorus(ServerLevel level, LionClanData.Clan clan, long now) {
        if (clan.isNomad()) return;
        long dayTime = level.getDayTime();
        long time = Math.floorMod(dayTime, 24000L);
        long day = Math.floorDiv(dayTime, 24000L);
        long slot;
        if (time >= NIGHT_START && time < NIGHT_START + CHORUS_WINDOW) slot = day * 2;
        else if (time >= DAWN_START && time < DAWN_START + CHORUS_WINDOW) slot = day * 2 + 1;
        else {
            clan.state.chorusDueTick = -1L;
            return;
        }
        LionPrideState state = clan.state;
        if (state.lastChorusSlot == slot) return;
        if (state.chorusDueTick < 0L) {
            state.chorusDueTick = now + level.random.nextInt(CHORUS_MAX_DELAY);
            return;
        }
        if (now < state.chorusDueTick) return;

        LionEntity leader = loadedLeader(level, clan);
        if (leader != null && leader.getTarget() == null && !leader.isKnockedOut() && !leader.isSedatedFleeing()
                && !leader.isRoaring() && !leader.wild.isConceding()) {
            if (leader.isNapping()) leader.setNap(false);
            if (leader.isSitting()) leader.setSitting(false);
            leader.startChorus();
        }
        state.lastChorusSlot = slot;
        state.chorusDueTick = -1L;
    }

    public static void onChorusShout(ServerLevel level, LionEntity caller) {
        level.playSound(null, caller.getX(), caller.getY(), caller.getZ(), OWSounds.TIGER_ROAR.get(),
                SoundSource.NEUTRAL, CHORUS_VOLUME, 0.72f);

        LionClanData data = LionClanData.get(level.getServer());
        LionClanData.Clan clan = data.getClan(caller.getClanId());
        if (clan == null || clan.tamed) return;

        List<LionEntity> responders = new ArrayList<>();
        for (LionEntity lion : loadedMembers(level, clan)) {
            if (lion == caller || lion.isKnockedOut() || lion.isSedatedFleeing() || lion.distanceTo(caller) > CHORUS_REPLY_RANGE) continue;
            responders.add(lion);
        }
        responders.sort((a, b) -> Double.compare(a.distanceToSqr(caller), b.distanceToSqr(caller)));
        for (int i = 0; i < responders.size(); i++) {
            responders.get(i).wild.scheduleChorusReply(CHORUS_FIRST_REPLY + i * CHORUS_REPLY_SPACING + level.random.nextInt(5));
        }

        int voices = 1 + responders.size();
        clan.state.lastVoices = voices;
        assessHearers(level, data, clan, caller, voices);
    }

    private static void assessHearers(ServerLevel level, LionClanData data, LionClanData.Clan caller, LionEntity voice, int voices) {
        long now = level.getGameTime();
        for (LionClanData.Clan other : new ArrayList<>(data.clans())) {
            if (other == caller || other.tamed) continue;
            LionEntity hearer = loadedLeader(level, other);
            if (hearer == null || hearer.distanceTo(voice) > CHORUS_HEARING_RANGE) continue;
            int own = other.size();

            if (!other.isNomad() && !caller.isNomad() && other.home != null && caller.home != null) {
                Vec3 theirHome = homeCenter(other);
                Vec3 ourHome = homeCenter(caller);
                double gap = horizontalDistance(theirHome, ourHome);
                double reach = territoryRadius(caller) + territoryRadius(other);
                if (voices > own && gap < reach + CHORUS_PUSH_DISTANCE) {
                    Vec3 away = awayFrom(theirHome, ourHome, level);
                    data.relocate(other, surface(level, theirHome.add(away.scale(CHORUS_PUSH_DISTANCE))));
                } else if (voices == own && gap < reach) {
                    other.state.declareRival(caller.id, now + RIVAL_TICKS);
                    caller.state.declareRival(other.id, now + RIVAL_TICKS);
                    caller.state.waypoint = other.home;
                    caller.state.waypointUntil = now + RIVAL_TICKS;
                    caller.state.waypointIsChallenge = true;
                }
            } else if (other.isNomad() && !caller.isNomad() && caller.home != null) {
                if (other.maleCount() >= caller.maleCount()) {
                    other.state.declareRival(caller.id, now + RIVAL_TICKS);
                    other.state.waypoint = caller.home;
                    other.state.waypointUntil = now + RIVAL_TICKS;
                    other.state.waypointIsChallenge = true;
                } else {
                    Vec3 away = awayFrom(hearer.position(), homeCenter(caller), level);
                    other.state.waypoint = surface(level, hearer.position().add(away.scale(NOMAD_AVOID_DISTANCE)));
                    other.state.waypointUntil = now + NOMAD_AVOID_TICKS;
                    other.state.waypointIsChallenge = false;
                }
            }
        }
    }

    private static Vec3 awayFrom(Vec3 from, Vec3 threat, Level level) {
        Vec3 away = new Vec3(from.x - threat.x, 0, from.z - threat.z);
        if (away.lengthSqr() < 1.0E-4) away = Vec3.directionFromRotation(0f, level.random.nextFloat() * 360f);
        return away.normalize();
    }

    public static BlockPos surface(Level level, Vec3 point) {
        BlockPos column = BlockPos.containing(point.x, point.y, point.z);
        if (!level.hasChunkAt(column)) return column;
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
    }

    public static void onKill(ServerLevel level, LionEntity killer, LivingEntity prey) {
        if (prey instanceof LionEntity || prey instanceof Player) return;
        LionClanData data = LionClanData.get(level.getServer());
        LionClanData.Clan clan = data.getClan(killer.getClanId());
        if (clan == null || clan.tamed) return;
        int value = Mth.clamp((int) (prey.getMaxHealth() * KILL_SATIETY_PER_HEALTH), KILL_SATIETY_MIN, KILL_SATIETY_MAX);
        data.feed(clan, value);
        long now = level.getGameTime();
        LionPrideState state = clan.state;
        state.feastPos = prey.blockPosition();
        state.feastStartTick = now;
        state.feastUntil = now + FEAST_TICKS;
        state.maleShare = 0;
        if (state.hunt != null && state.hunt.preyId == prey.getId()) state.hunt.succeed();
    }

    public static void onMeatEaten(ServerLevel level, LionEntity lion, ItemStack stack, @Nullable BlockPos at) {
        FoodProperties food = stack.getFoodProperties(lion);
        int nutrition = food == null ? MEAT_DEFAULT_NUTRITION : Math.max(1, food.nutrition());
        lion.heal(2f);
        LionClanData data = LionClanData.get(level.getServer());
        LionClanData.Clan clan = data.getClan(lion.getClanId());
        if (clan == null || clan.tamed) return;
        data.feed(clan, nutrition * MEAT_SATIETY_PER_NUTRITION);
        long now = level.getGameTime();
        LionPrideState state = clan.state;
        if (at != null) {
            if (state.feastPos == null) state.feastStartTick = now;
            state.feastPos = at.immutable();
        }
        state.feastUntil = Math.max(state.feastUntil, now + FEAST_REFRESH_TICKS);
        if (lion.isMale()) state.maleShare++;
    }

    public static void onHurt(ServerLevel level, LionEntity victim, Entity attacker) {
        LionClanData data = LionClanData.get(level.getServer());
        LionClanData.Clan clan = data.getClan(victim.getClanId());
        if (clan == null || clan.tamed) return;
        long now = level.getGameTime();
        if (attacker instanceof LionEntity other && other.isMale() && victim.isMale() && !other.isTame()) {
            checkConcession(level, data, clan, victim, other);
            return;
        }
        disturb(level, clan, now);
        if (attacker instanceof Player player && isIntrudable(player)) {
            clan.state.intrusion.put(player.getUUID(), INTRUSION_FULL);
            designate(clan, loadedMembers(level, clan), player, true, now);
        }
    }

    private static void checkConcession(ServerLevel level, LionClanData data, LionClanData.Clan clan, LionEntity loser, LionEntity victor) {
        if (loser.getHealth() > loser.getMaxHealth() * CONCEDE_HEALTH_RATIO || loser.wild.isConceding()) return;
        UUID victorClanId = victor.getClanId();
        LionClanData.Clan victorClan = data.getClan(victorClanId);
        if (victorClan == null || victorClan == clan || victorClan.tamed) return;

        loser.wild.flee(victor, CONCEDE_FLEE_TICKS);
        loser.setTarget(null);
        if (victor.getTarget() == loser) victor.setTarget(null);
        level.playSound(null, loser.getX(), loser.getY(), loser.getZ(), OWSounds.TIGER_HURT.get(), SoundSource.NEUTRAL, 1.4f, 0.7f);

        boolean lastMale = clan.maleCount() <= 1;
        if (lastMale) {
            victor.claimClan(level, clan.id);
        } else {
            data.leave(loser.getUUID());
            loser.refreshClan(level);
        }
    }

    public static float concedeHealthRatio() {
        return CONCEDE_HEALTH_RATIO;
    }

    public static double randomOffset(LionEntity lion, double spread) {
        return (lion.getRandom().nextDouble() - 0.5) * spread;
    }

    public static float growlPitch(LionEntity lion) {
        return (float) OWUtils.generateRandomInterval(0.5, 0.62) * (lion.isMale() ? 0.85f : 1f);
    }
}
