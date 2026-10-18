package net.tiew.operationWild.entity.herd;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ElephantWildMind {

    public static final int DISPLAY_NONE = 0;
    public static final int DISPLAY_THREAT = 1;
    public static final int DISPLAY_TOSS = 2;
    public static final int DISPLAY_MUSTH = 3;

    public static final int MOCK_IDLE = 0;
    public static final int MOCK_DISPLAY = 1;
    public static final int MOCK_RUSH = 2;
    public static final int MOCK_HOLD = 3;

    public static final int MAX_THIRST = 24000;
    private static final int THIRST_CRITICAL = 6000;
    private static final int THIRST_HOT_HOURS = 16000;
    private static final int HOT_HOURS_START = 4000;
    private static final int HOT_HOURS_END = 9000;
    private static final int WET_TICKS = 1200;

    private static final double DEFENSE_GIVE_UP_DISTANCE = 48.0;
    private static final int FLARE_TICKS = 30;
    private static final int CHORUS_MIN_DELAY = 4;
    private static final int CHORUS_DELAY_SPREAD = 18;

    private static final int TACTICS_INTERVAL = 20;
    private static final double QUAKE_TRIGGER_DISTANCE = 14.0;
    private static final double QUAKE_DODGE_DISTANCE = 10.0;
    private static final int QUAKE_COOLDOWN_MIN = 2400;
    private static final int QUAKE_COOLDOWN_SPREAD = 1200;
    private static final int QUAKE_FIRST_COOLDOWN = 600;
    private static final int QUAKE_MIN_ENGAGEMENT = 240;
    private static final double QUAKE_HERD_RANGE = 64.0;
    private static final int QUAKE_DODGE_TICKS = 160;
    private static final float QUAKE_CROWD_CHANCE = 0.12f;
    private static final float QUAKE_WOUNDED_CHANCE = 0.07f;
    private static final float QUAKE_DODGE_CHANCE = 0.07f;
    private static final int BASH_COOLDOWN_MIN = 140;
    private static final int BASH_COOLDOWN_SPREAD = 60;
    private static final double BASH_MAX_GAP = 4.0;
    private static final float BASH_FLANK_ANGLE = 50f;
    private static final float BASH_MAX_ANGLE = 150f;
    private static final float BASH_FRONT_ANGLE = 15f;
    private static final int BASH_EVADE_TICKS = 30;
    private static final float BASH_CIRCLING_TRAVEL = 90f;
    private static final float BASH_ORBIT_TRAVEL = 60f;
    private static final double ORBIT_TRACK_DISTANCE = 8.0;
    private static final float YAW_TRAVEL_DECAY = 0.96f;
    private static final float ORBIT_TRAVEL_DECAY = 0.95f;

    private static final int AWARENESS_INTERVAL = 10;
    private static final double FEEL_SPRINT_RANGE = 24.0;
    private static final double FEEL_RIDE_RANGE = 28.0;
    private static final double FEEL_WALK_RANGE = 12.0;
    private static final double SEE_RANGE = 16.0;
    private static final double SEE_SNEAK_RANGE = 6.0;
    private static final double WALK_MOVE_SQR = 0.25 * 0.25;
    private static final double RIDE_MOVE_SQR = 1.5 * 1.5;
    private static final int LISTEN_TICKS = 64;
    private static final int AWARE_TICKS = 200;

    private static final long WARNING_MEMORY_TICKS = 1200L;
    private static final int MOURN_TICKS = 400;
    private static final int MOURN_EXPIRE_TICKS = 2400;
    private static final long GRUDGE_TICKS = 72000L;
    private static final int MAX_GRUDGES = 8;

    private static final int MUSTH_ROLL_INTERVAL = 1200;
    private static final int MUSTH_ROLL_ODDS = 90;
    private static final int MUSTH_MIN_TICKS = 3600;
    private static final int MUSTH_TICKS_SPREAD = 2400;
    private static final int MUSTH_COOLDOWN_TICKS = 36000;
    private static final ResourceLocation MUSTH_DAMAGE_ID = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "elephant_musth_damage");
    private static final ResourceLocation MUSTH_SPEED_ID = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "elephant_musth_speed");
    private static final double MUSTH_DAMAGE_BONUS = 0.3;
    private static final double MUSTH_SPEED_BONUS = 0.15;

    private final ElephantEntity elephant;

    @Nullable
    private UUID herdId;
    private int orphanTicks;

    private long alarmCooldownUntil;
    @Nullable
    private LivingEntity alarmTarget;

    @Nullable
    private LivingEntity defenseTarget;
    private int defenseTicks;
    private int chorusDelay = -1;
    private int flareTicks;

    private int earthquakeCooldown = QUAKE_FIRST_COOLDOWN;
    private int engagedTicks;
    private int bashCooldown;
    private int lastLandedHit;
    private float yawTravel;
    private float lastBodyYaw = Float.NaN;
    private float orbitTravel;
    private float orbitDirection;
    private float lastBearing = Float.NaN;

    @Nullable
    private Player awareOf;
    private int awareTicks;
    private int listenTicks;
    private boolean listeningActive;
    @Nullable
    private Vec3 listenSource;
    private final Map<UUID, Vec3> lastPlayerPositions = new HashMap<>();

    private int mockPhase = MOCK_IDLE;
    private int mockChargeCooldown;
    @Nullable
    private UUID warnedPlayer;
    private long warnedAt = Long.MIN_VALUE;

    @Nullable
    private Vec3 mournPos;
    private long mournExpireAt;
    private final Map<UUID, Long> grudges = new HashMap<>();

    private int thirst = -1;
    private int wetTicks;

    private int musthTicks;
    private int musthCooldown;

    private boolean wasTame;

    public ElephantWildMind(ElephantEntity elephant) {
        this.elephant = elephant;
    }

    @Nullable
    public UUID getHerdId() {
        return herdId;
    }

    public void setHerdId(@Nullable UUID herdId) {
        this.herdId = herdId;
        this.orphanTicks = 0;
    }

    public int getOrphanTicks() {
        return orphanTicks;
    }

    public void setOrphanTicks(int orphanTicks) {
        this.orphanTicks = orphanTicks;
    }

    public void setAlarmCooldownUntil(long until) {
        alarmCooldownUntil = Math.max(alarmCooldownUntil, until);
    }

    @Nullable
    public LivingEntity getAlarmTarget() {
        return alarmTarget;
    }

    @Nullable
    public LivingEntity getDefenseTarget() {
        return defenseTarget;
    }

    public boolean isDefending() {
        return defenseTarget != null;
    }

    @Nullable
    public Player getAwareOf() {
        return awareTicks > 0 ? awareOf : null;
    }

    public int getListenTicks() {
        return listenTicks;
    }

    @Nullable
    public Vec3 getListenSource() {
        return listenSource;
    }

    public void stopListening() {
        listenTicks = 0;
        listeningActive = false;
    }

    public void setListeningActive(boolean active) {
        listeningActive = active;
    }

    public int getMockPhase() {
        return mockPhase;
    }

    public void setMockPhase(int phase) {
        this.mockPhase = phase;
    }

    public boolean canMockCharge() {
        return mockChargeCooldown <= 0;
    }

    public void setMockChargeCooldown(int ticks) {
        mockChargeCooldown = ticks;
    }

    public boolean wasRecentlyWarned(Player player) {
        return player.getUUID().equals(warnedPlayer)
                && elephant.level().getGameTime() - warnedAt <= WARNING_MEMORY_TICKS;
    }

    public void recordWarning(Player player) {
        warnedPlayer = player.getUUID();
        warnedAt = elephant.level().getGameTime();
    }

    @Nullable
    public Vec3 getMournPos() {
        return mournPos;
    }

    public int mournDuration() {
        return MOURN_TICKS;
    }

    public void startMourning(Vec3 pos) {
        if (elephant.isTame()) return;
        mournPos = pos;
        mournExpireAt = elephant.level().getGameTime() + MOURN_EXPIRE_TICKS;
    }

    public void endMourning() {
        mournPos = null;
    }

    public boolean isInMusth() {
        return musthTicks > 0;
    }

    public boolean isThirsty() {
        if (thirst < 0) return false;
        if (thirst < THIRST_CRITICAL) return true;
        return isHotHours() && thirst < THIRST_HOT_HOURS;
    }

    public void makeThirsty() {
        thirst = Math.min(thirst, THIRST_CRITICAL - 1);
    }

    public void quench() {
        thirst = MAX_THIRST;
    }

    public void soak() {
        wetTicks = WET_TICKS;
        elephant.clearFire();
    }

    public boolean isWet() {
        return wetTicks > 0;
    }

    private boolean isHotHours() {
        long time = elephant.level().getDayTime() % 24000L;
        return time >= HOT_HOURS_START && time < HOT_HOURS_END && !elephant.level().isRaining();
    }

    public void rememberGrudge(LivingEntity killer) {
        if (elephant.isTame()) return;
        grudges.put(killer.getUUID(), elephant.level().getGameTime() + GRUDGE_TICKS);
        while (grudges.size() > MAX_GRUDGES) {
            UUID oldest = null;
            long oldestExpiry = Long.MAX_VALUE;
            for (Map.Entry<UUID, Long> entry : grudges.entrySet()) {
                if (entry.getValue() < oldestExpiry) {
                    oldestExpiry = entry.getValue();
                    oldest = entry.getKey();
                }
            }
            grudges.remove(oldest);
        }
    }

    public boolean holdsGrudgeAgainst(LivingEntity entity) {
        Long expiry = grudges.get(entity.getUUID());
        return expiry != null && expiry > elephant.level().getGameTime();
    }

    public boolean hasGrudges() {
        return !grudges.isEmpty();
    }

    public void answerAlarm(@Nullable LivingEntity threat, int ticks, boolean chorus) {
        if (!ElephantHerd.canAnswer(elephant) || !ElephantHerd.isValidThreat(elephant, threat)) return;
        if (elephant.sedationResponse.blocksTarget(threat)) return;

        boolean fresh = defenseTarget != threat;
        defenseTarget = threat;
        defenseTicks = fresh ? ticks : Math.max(defenseTicks, ticks);
        listenTicks = 0;
        elephant.setNap(false);
        elephant.interruptCalmTrunk();
        if (fresh && chorus) chorusDelay = CHORUS_MIN_DELAY + elephant.getRandom().nextInt(CHORUS_DELAY_SPREAD);
    }

    public void clearDefense() {
        LivingEntity previous = defenseTarget;
        defenseTarget = null;
        defenseTicks = 0;
        if (previous != null && elephant.getTarget() == previous) elephant.setTarget(null);
    }

    public void onLandedHit(LivingEntity victim) {
        if (victim == elephant.getTarget()) lastLandedHit = elephant.tickCount;
    }

    public void onHurt(DamageSource source) {
        if (elephant.isTame()) return;
        LivingEntity attacker = source.getEntity() instanceof LivingEntity living ? living : null;
        if (!ElephantHerd.isValidThreat(elephant, attacker)) return;

        listenTicks = 0;
        elephant.interruptCalmTrunk();
        tryAlarm(attacker);
    }

    public boolean tryAlarm(LivingEntity threat) {
        if (!canRaiseAlarm()) return false;
        if (elephant.level().getGameTime() < alarmCooldownUntil) return false;
        if (defenseTarget == threat) return false;
        if (!ElephantHerd.isValidThreat(elephant, threat)) return false;
        if (!ElephantHerd.hasAnswerers(elephant, threat, alarmRange())) return false;

        alarmTarget = threat;
        setAlarmCooldownUntil(elephant.level().getGameTime() + ElephantHerd.ALARM_COOLDOWN_TICKS);
        for (ElephantEntity mate : ElephantHerd.herdmates(elephant, alarmRange())) {
            mate.wild.setAlarmCooldownUntil(elephant.level().getGameTime() + ElephantHerd.ALARM_COOLDOWN_TICKS);
        }
        elephant.startAlarm();
        return true;
    }

    public boolean canRaiseAlarm() {
        return elephant.isAlive() && !elephant.isTame() && !elephant.isSleeping()
                && !elephant.isKnockedOut() && !elephant.isSedatedFleeing() && !elephant.isVehicle()
                && !elephant.isPassenger() && !elephant.isAlarming() && !elephant.isEarthquakeGesture()
                && !elephant.isShoulderBashing() && elephant.getFluidHeight(FluidTags.WATER) < 1.0;
    }

    public double alarmRange() {
        return elephant.isMatriarch() ? ElephantHerd.MATRIARCH_ALARM_RANGE : ElephantHerd.ALARM_RANGE;
    }

    public void onAlarmSignal() {
        ElephantHerd.broadcastAlarm(elephant, alarmTarget, alarmRange());
    }

    public void onAlarmEnd() {
        alarmTarget = null;
    }

    public void tick() {
        if (elephant.isTame()) {
            if (!wasTame) reset();
            wasTame = true;
            return;
        }
        wasTame = false;

        if (thirst < 0) thirst = MAX_THIRST / 3 + elephant.getRandom().nextInt(MAX_THIRST * 2 / 3);

        ElephantHerd.tick(elephant);
        tickDefense();
        tickCooldowns();
        tickYawTravel();
        tickAwareness();
        tickNeeds();
        tickMusth();
        elephant.setThreatDisplay(currentDisplay());
        elephant.setListening(listeningActive && listenTicks > 0 && !elephant.isVehicle());
    }

    private void tickDefense() {
        if (defenseTarget != null) {
            boolean expired = --defenseTicks <= 0;
            boolean gone = !ElephantHerd.isValidThreat(elephant, defenseTarget)
                    || elephant.distanceToSqr(defenseTarget) > DEFENSE_GIVE_UP_DISTANCE * DEFENSE_GIVE_UP_DISTANCE
                    || elephant.sedationResponse.blocksTarget(defenseTarget);
            if (expired || gone) clearDefense();
        }

        if (chorusDelay > 0 && --chorusDelay == 0) {
            chorusDelay = -1;
            flareTicks = FLARE_TICKS;
            elephant.level().playSound(null, elephant.getX(), elephant.getY(), elephant.getZ(),
                    OWSounds.ELEPHANT_CALL.get(), SoundSource.NEUTRAL, 2.5f,
                    (float) OWUtils.generateRandomInterval(0.95, 1.15));
        }
        if (flareTicks > 0) flareTicks--;
    }

    private void tickCooldowns() {
        if (earthquakeCooldown > 0) earthquakeCooldown--;
        if (bashCooldown > 0) bashCooldown--;
        if (mockChargeCooldown > 0) mockChargeCooldown--;
        if (awareTicks > 0 && --awareTicks == 0) awareOf = null;
        if (listenTicks > 0) listenTicks--;

        if (mournPos != null && elephant.level().getGameTime() > mournExpireAt) mournPos = null;

        if ((elephant.tickCount + elephant.getId()) % 200 == 0 && !grudges.isEmpty()) {
            long now = elephant.level().getGameTime();
            grudges.values().removeIf(expiry -> expiry <= now);
        }
    }

    private void tickYawTravel() {
        float yaw = elephant.yBodyRot;
        if (!Float.isNaN(lastBodyYaw)) {
            yawTravel = yawTravel * YAW_TRAVEL_DECAY + Math.abs(Mth.wrapDegrees(yaw - lastBodyYaw));
        }
        lastBodyYaw = yaw;
    }

    private void tickAwareness() {
        if ((elephant.tickCount + elephant.getId()) % AWARENESS_INTERVAL != 0) return;

        if (elephant.isSleeping() || elephant.isKnockedOut() || elephant.isVehicle()) {
            awareTicks = 0;
            awareOf = null;
            listenTicks = 0;
            lastPlayerPositions.clear();
            return;
        }

        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        boolean nearestFelt = false;
        Map<UUID, Vec3> seen = new HashMap<>();

        for (Player player : elephant.level().getEntitiesOfClass(Player.class,
                elephant.getBoundingBox().inflate(FEEL_RIDE_RANGE))) {
            if (!player.isAlive() || player.isCreative() || player.isSpectator()) continue;

            Vec3 position = player.position();
            Vec3 previous = lastPlayerPositions.get(player.getUUID());
            seen.put(player.getUUID(), position);
            double movedSqr = previous == null ? 0.0 : previous.distanceToSqr(position);
            double distance = elephant.distanceTo(player);

            boolean grounded = player.onGround() || player.getVehicle() != null && player.getVehicle().onGround();
            boolean felt;
            if (player.getVehicle() != null) {
                felt = grounded && movedSqr > RIDE_MOVE_SQR && distance <= FEEL_RIDE_RANGE;
            } else if (player.isCrouching()) {
                felt = false;
            } else if (player.isSprinting()) {
                felt = grounded && distance <= FEEL_SPRINT_RANGE;
            } else {
                felt = grounded && movedSqr > WALK_MOVE_SQR && distance <= FEEL_WALK_RANGE;
            }

            boolean seenByEye = false;
            if (distance <= SEE_RANGE && elephant.getSensing().hasLineOfSight(player)) {
                seenByEye = !player.isCrouching() || distance <= SEE_SNEAK_RANGE;
            }
            if (!felt && !seenByEye) continue;

            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = player;
                nearestFelt = felt && !seenByEye;
            }
        }

        lastPlayerPositions.clear();
        lastPlayerPositions.putAll(seen);

        if (nearest == null) return;

        boolean newcomer = awareOf != nearest || awareTicks <= 0;
        awareOf = nearest;
        awareTicks = AWARE_TICKS;
        if (newcomer && nearestFelt && defenseTarget == null && elephant.getTarget() == null) {
            listenTicks = LISTEN_TICKS;
            listenSource = nearest.position();
        }
    }

    private void tickNeeds() {
        if (thirst > 0) thirst--;

        if (wetTicks > 0) {
            wetTicks--;
            if (elephant.isOnFire()) elephant.clearFire();
            if (wetTicks % 6 == 0 && elephant.level() instanceof ServerLevel serverLevel) {
                double halfWidth = elephant.getBbWidth() * 0.45;
                serverLevel.sendParticles(ParticleTypes.DRIPPING_WATER,
                        elephant.getX() + (elephant.getRandom().nextDouble() - 0.5) * 2 * halfWidth,
                        elephant.getY() + elephant.getBbHeight() * (0.35 + elephant.getRandom().nextDouble() * 0.5),
                        elephant.getZ() + (elephant.getRandom().nextDouble() - 0.5) * 2 * halfWidth,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }

    private void tickMusth() {
        if (musthTicks > 0) {
            applyMusthModifiers(true);
            if (--musthTicks == 0) endMusth();
            return;
        }
        applyMusthModifiers(false);
        if (musthCooldown > 0) {
            musthCooldown--;
            return;
        }
        if ((elephant.tickCount + elephant.getId()) % MUSTH_ROLL_INTERVAL != 0) return;
        if (elephant.isFemale() || elephant.isMatriarch() || elephant.isSleeping()
                || elephant.isKnockedOut() || elephant.isVehicle()) return;
        if (elephant.getRandom().nextInt(MUSTH_ROLL_ODDS) != 0) return;

        musthTicks = MUSTH_MIN_TICKS + elephant.getRandom().nextInt(MUSTH_TICKS_SPREAD);
        elephant.setMusth(true);
        elephant.level().playSound(null, elephant.getX(), elephant.getY(), elephant.getZ(),
                OWSounds.ELEPHANT_SCREAM.get(), SoundSource.NEUTRAL, 2.5f, 0.75f);
    }

    private void endMusth() {
        musthTicks = 0;
        musthCooldown = MUSTH_COOLDOWN_TICKS;
        elephant.setMusth(false);
        applyMusthModifiers(false);
    }

    private void applyMusthModifiers(boolean active) {
        AttributeInstance damage = elephant.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance speed = elephant.getAttribute(Attributes.MOVEMENT_SPEED);
        if (damage != null) {
            boolean has = damage.hasModifier(MUSTH_DAMAGE_ID);
            if (active && !has) damage.addTransientModifier(new AttributeModifier(MUSTH_DAMAGE_ID,
                    MUSTH_DAMAGE_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            else if (!active && has) damage.removeModifier(MUSTH_DAMAGE_ID);
        }
        if (speed != null) {
            boolean has = speed.hasModifier(MUSTH_SPEED_ID);
            if (active && !has) speed.addTransientModifier(new AttributeModifier(MUSTH_SPEED_ID,
                    MUSTH_SPEED_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            else if (!active && has) speed.removeModifier(MUSTH_SPEED_ID);
        }
    }

    private int currentDisplay() {
        if (elephant.isVehicle() || elephant.isSleeping()) return DISPLAY_NONE;
        if (mockPhase == MOCK_HOLD) return DISPLAY_TOSS;
        if (mockPhase != MOCK_IDLE || flareTicks > 0) return DISPLAY_THREAT;
        if (musthTicks > 0) return DISPLAY_MUSTH;
        return DISPLAY_NONE;
    }

    public void tickCombatTactics(@Nullable LivingEntity target) {
        trackOrbit(target);
        engagedTicks = target == null ? 0 : engagedTicks + 1;
        if (target == null || elephant.isTame() || elephant.isVehicle()) return;
        if (elephant.isEarthquaking() || elephant.isShoulderBashing() || elephant.isAlarming()) return;
        if (tryWildEarthquake(target)) return;
        tryWildShoulderBash(target);
    }

    private boolean tryWildEarthquake(LivingEntity target) {
        if (earthquakeCooldown > 0 || engagedTicks < QUAKE_MIN_ENGAGEMENT) return false;
        if ((elephant.tickCount + elephant.getId()) % TACTICS_INTERVAL != 0) return false;
        if (!elephant.onGround() || elephant.isInWater()) return false;

        double distance = elephant.distanceTo(target);
        if (distance > QUAKE_TRIGGER_DISTANCE) return false;

        float chance = 0f;
        if (countThreats(target) >= 2) chance += QUAKE_CROWD_CHANCE;
        if (elephant.getHealth() < elephant.getMaxHealth() * 0.5f) chance += QUAKE_WOUNDED_CHANCE;
        if (elephant.tickCount - lastLandedHit > QUAKE_DODGE_TICKS && distance <= QUAKE_DODGE_DISTANCE) chance += QUAKE_DODGE_CHANCE;
        if (chance <= 0f || elephant.getRandom().nextFloat() >= chance) return false;

        if (!elephant.startWildEarthquake()) return false;
        int cooldown = QUAKE_COOLDOWN_MIN + elephant.getRandom().nextInt(QUAKE_COOLDOWN_SPREAD);
        earthquakeCooldown = cooldown;
        for (ElephantEntity mate : ElephantHerd.herdmates(elephant, QUAKE_HERD_RANGE)) mate.wild.shareEarthquakeCooldown(cooldown);
        bashCooldown = Math.max(bashCooldown, OWAttacksConstants.Elephant.EARTHQUAKE_TOTAL_TICKS);
        return true;
    }

    public void shareEarthquakeCooldown(int ticks) {
        earthquakeCooldown = Math.max(earthquakeCooldown, ticks);
    }

    private int countThreats(LivingEntity target) {
        int threats = 0;
        AABB box = elephant.getBoundingBox().inflate(QUAKE_TRIGGER_DISTANCE, 4.0, QUAKE_TRIGGER_DISTANCE);
        for (LivingEntity other : elephant.level().getEntitiesOfClass(LivingEntity.class, box)) {
            if (!ElephantHerd.isValidThreat(elephant, other)) continue;
            if (other == target || other instanceof Player) {
                threats++;
            } else if (other instanceof Mob mob) {
                LivingEntity aim = mob.getTarget();
                if (aim == elephant || aim instanceof ElephantEntity mate && elephant.isSameHerd(mate)) threats++;
            }
        }
        return threats;
    }

    private void trackOrbit(@Nullable LivingEntity target) {
        orbitTravel *= ORBIT_TRAVEL_DECAY;
        if (target == null || elephant.distanceToSqr(target) > ORBIT_TRACK_DISTANCE * ORBIT_TRACK_DISTANCE) {
            lastBearing = Float.NaN;
            return;
        }
        float bearing = elephant.yawTowards(target.position());
        if (!Float.isNaN(lastBearing)) {
            float step = Mth.wrapDegrees(bearing - lastBearing);
            orbitTravel += Math.abs(step);
            orbitDirection = orbitDirection * 0.9f + step;
        }
        lastBearing = bearing;
    }

    private void tryWildShoulderBash(LivingEntity target) {
        if (bashCooldown > 0 || !elephant.onGround()) return;
        if (elephant.isCombo() && !elephant.isPauseCombo()) return;

        double dx = target.getX() - elephant.getX();
        double dz = target.getZ() - elephant.getZ();
        double gap = Math.sqrt(dx * dx + dz * dz) - (elephant.getBbWidth() + target.getBbWidth()) * 0.5;
        if (gap > BASH_MAX_GAP) return;
        if (Math.abs(target.getY() - elephant.getY()) > elephant.getBbHeight() * 0.6) return;
        if (elephant.tickCount - lastLandedHit <= BASH_EVADE_TICKS) return;

        float relative = Mth.wrapDegrees(elephant.yawTowards(target.position()) - elephant.yBodyRot);
        float angle = Math.abs(relative);
        if (angle > BASH_MAX_ANGLE) return;

        boolean flanked = angle >= BASH_FLANK_ANGLE && yawTravel > BASH_CIRCLING_TRAVEL;
        boolean orbited = orbitTravel > BASH_ORBIT_TRAVEL;
        if (!flanked && !orbited) return;

        int side;
        if (angle >= BASH_FRONT_ANGLE) side = relative >= 0 ? 1 : -1;
        else side = orbitDirection >= 0 ? 1 : -1;

        if (!elephant.startWildShoulderBash(side)) return;
        bashCooldown = BASH_COOLDOWN_MIN + elephant.getRandom().nextInt(BASH_COOLDOWN_SPREAD);
        yawTravel = 0f;
        orbitTravel = 0f;
    }

    public void reset() {
        herdId = null;
        orphanTicks = 0;
        alarmTarget = null;
        defenseTarget = null;
        defenseTicks = 0;
        chorusDelay = -1;
        flareTicks = 0;
        awareOf = null;
        awareTicks = 0;
        listenTicks = 0;
        listeningActive = false;
        listenSource = null;
        lastPlayerPositions.clear();
        mockPhase = MOCK_IDLE;
        mournPos = null;
        grudges.clear();
        wetTicks = 0;
        if (musthTicks > 0) endMusth();
        musthCooldown = 0;
        elephant.setMatriarch(false);
        elephant.setThreatDisplay(DISPLAY_NONE);
        elephant.setListening(false);
        elephant.setMusth(false);
        elephant.setHeldFood(null);
        elephant.interruptCalmTrunk();
    }

    public void save(CompoundTag tag) {
        CompoundTag wild = new CompoundTag();
        if (herdId != null) wild.putUUID("herd", herdId);
        wild.putInt("thirst", thirst);
        wild.putInt("musth", musthTicks);
        wild.putInt("musthCooldown", musthCooldown);
        wild.putLong("alarmCooldown", alarmCooldownUntil);
        ListTag grudgeList = new ListTag();
        for (Map.Entry<UUID, Long> entry : grudges.entrySet()) {
            CompoundTag grudge = new CompoundTag();
            grudge.putUUID("id", entry.getKey());
            grudge.putLong("until", entry.getValue());
            grudgeList.add(grudge);
        }
        wild.put("grudges", grudgeList);
        tag.put("elephantWild", wild);
    }

    public void load(CompoundTag tag) {
        if (!tag.contains("elephantWild")) return;
        CompoundTag wild = tag.getCompound("elephantWild");
        herdId = wild.hasUUID("herd") ? wild.getUUID("herd") : null;
        thirst = wild.contains("thirst") ? wild.getInt("thirst") : -1;
        musthTicks = Math.max(0, wild.getInt("musth"));
        musthCooldown = Math.max(0, wild.getInt("musthCooldown"));
        alarmCooldownUntil = wild.getLong("alarmCooldown");
        grudges.clear();
        ListTag grudgeList = wild.getList("grudges", Tag.TAG_COMPOUND);
        for (int i = 0; i < grudgeList.size(); i++) {
            CompoundTag grudge = grudgeList.getCompound(i);
            if (grudge.hasUUID("id")) grudges.put(grudge.getUUID("id"), grudge.getLong("until"));
        }
        elephant.setMusth(musthTicks > 0);
    }
}
