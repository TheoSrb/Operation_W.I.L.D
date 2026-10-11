package net.tiew.operationWild.entity.breakout;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;
import net.tiew.operationWild.core.OWPlacedBlocks;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.taming.OWAggressiveTaming;
import net.tiew.operationWild.worldgen.dimension.OWDimensions;
import org.jetbrains.annotations.Nullable;

public class OWBreakout {

    private enum Phase { IDLE, BREACHING, SIEGING, STRUGGLING }

    private static final int AGITATION_MEMORY = 600;
    private static final int SCAN_INTERVAL = 20;
    private static final int REPLAN_DELAY = 40;
    private static final int FAILED_PLAN_DELAY = 200;
    private static final int EMPTY_PLAN_HOLD = 100;
    private static final int PLAN_DONE_HOLD = 10;
    private static final int APPROACH_STUCK_TICKS = 60;
    private static final double APPROACH_PROGRESS = 0.5;
    private static final int APPROACH_REPATH = 10;
    private static final float APPROACH_SPEED_FACTOR = 0.45f;
    private static final int STRIKE_TIMEOUT = 40;
    private static final int STRIKE_PAUSE_MIN = 3;
    private static final int STRIKE_PAUSE_MAX = 9;
    private static final double STRIKE_REACH = 1.1;
    private static final double STUCK_REACH_BONUS = 1.2;
    private static final float TURN_SPEED = 25f;
    private static final float FACING_TOLERANCE = 35f;
    private static final int RAGE_BUILDUP = 1200;
    private static final float RAGE_BONUS = 0.5f;
    private static final float DROWSY_PENALTY = 0.5f;
    private static final float DAMAGE_REFERENCE = 7f;
    private static final float MIN_DAMAGE_FACTOR = 0.8f;
    private static final float MAX_DAMAGE_FACTOR = 1.35f;
    private static final float SPLASH_FACTOR = 0.5f;
    private static final double SPLASH_RADIUS_SQR = 2.25;
    private static final double ESCAPE_MARGIN = 2.0;
    private static final int STRUGGLE_STRIKES_MIN = 3;
    private static final int STRUGGLE_STRIKES_MAX = 5;
    private static final int STRUGGLE_PAUSE_MIN = 15;
    private static final int STRUGGLE_PAUSE_MAX = 35;
    private static final int STRUGGLE_REST_MIN = 160;
    private static final int STRUGGLE_REST_MAX = 280;
    private static final double STRUGGLE_LOOKUP = 4.0;
    private static final double RECOIL = 0.22;
    private static final double SIEGE_MAX_DISTANCE = 40.0;
    private static final int SIEGE_PROBE_TTL = 40;
    private static final int SIEGE_FAILED_TTL = 100;
    private static final int SIEGE_PROBE_MIN_GAP = 20;
    private static final double SIEGE_DRIFT_SQR = 9.0;
    private static final int ASSAULT_COOLDOWN = 12;
    private static final double MIN_HIT_REACH = 2.0;
    private static final double HIT_REACH_MARGIN = 1.2;
    private static final double MIN_HIT_RISE = 1.0;
    private static final double HIT_RISE_MARGIN = 0.5;

    private final OWEntity entity;
    private final OWEnclosureProbe probe;
    private Phase phase = Phase.IDLE;
    @Nullable
    private OWBreachPlanner.Plan plan;
    private int stepIndex;
    @Nullable
    private BlockPos strikeTarget;
    private boolean strikePending;
    private int strikeWait;
    private int strikePause;
    private int agitation;
    private int trappedTicks;
    private int rageStage;
    private int planCooldown;
    private int holdOff;
    private int approachTimer;
    private double approachBest = Double.MAX_VALUE;
    private int approachStrikes;
    @Nullable
    private BlockPos struggleTarget;
    private int struggleStrikes;
    private int struggleBudget;
    private int struggleRest;
    @Nullable
    private LivingEntity siegeTarget;
    private Vec3 siegeAim = Vec3.ZERO;
    @Nullable
    private OWBreachPlanner.Plan siegeProbe;
    private int siegeProbeTick = Integer.MIN_VALUE / 2;
    private int siegeProbeTarget = -1;
    private Vec3 siegeProbeAim = Vec3.ZERO;
    private int assaultCooldown;

    public OWBreakout(OWEntity entity) {
        this.entity = entity;
        this.probe = new OWEnclosureProbe(entity);
    }

    public OWEnclosureProbe probe() {
        return probe;
    }

    public boolean isActive() {
        return phase != Phase.IDLE;
    }

    public boolean wantsControl() {
        return phase == Phase.BREACHING || phase == Phase.SIEGING || (phase == Phase.STRUGGLING && struggleRest <= 0);
    }

    public boolean isStriking() {
        return strikePending;
    }

    public float rage() {
        return Mth.clamp(trappedTicks / (float) RAGE_BUILDUP, 0f, 1f);
    }

    public void tick() {
        if (entity.level().isClientSide()) return;
        if (planCooldown > 0) planCooldown--;
        if (holdOff > 0) holdOff--;
        if (struggleRest > 0) struggleRest--;
        refreshAgitation();
        if (phase == Phase.SIEGING) {
            tickSiege();
            return;
        }
        if (!canBreakOut()) {
            reset();
            return;
        }

        boolean scanDue = phase != Phase.IDLE || (entity.tickCount + entity.getId()) % SCAN_INTERVAL == 0;
        if (scanDue && !probe.scan(danger()).enclosed()) {
            reset();
            return;
        }
        if (phase == Phase.IDLE) {
            if (scanDue && holdOff <= 0) begin();
            return;
        }

        trappedTicks++;
        announceRage();
        if (phase == Phase.STRUGGLING && struggleRest <= 0 && planCooldown <= 0 && !strikePending && struggleStrikes == 0) {
            replan();
        }
    }

    public void drive() {
        if (phase == Phase.BREACHING || phase == Phase.SIEGING) driveBreach();
        else if (phase == Phase.STRUGGLING) driveStruggle();
    }

    public boolean isSieging() {
        return phase == Phase.SIEGING;
    }

    public boolean isSieging(LivingEntity target) {
        return phase == Phase.SIEGING && siegeTarget == target;
    }

    public SiegeOutlook siegeOutlook(LivingEntity target) {
        if (!siegeAllowed(target)) return SiegeOutlook.IMPOSSIBLE;
        OWBreachPlanner.Plan probed = probeSiege(target);
        if (probed == null) return SiegeOutlook.IMPOSSIBLE;
        return probed.steps().isEmpty() ? SiegeOutlook.OPEN : SiegeOutlook.BREACH;
    }

    public enum SiegeOutlook { IMPOSSIBLE, OPEN, BREACH }

    public boolean startSiege(LivingEntity target) {
        if (isSieging(target) && plan != null) return true;
        if (!siegeAllowed(target)) return false;
        OWBreachPlanner.Plan probed = probeSiege(target);
        if (probed == null) return false;
        clearStrike();
        resetApproach();
        plan = probed;
        stepIndex = 0;
        phase = Phase.SIEGING;
        siegeTarget = target;
        siegeAim = target.position();
        planCooldown = REPLAN_DELAY;
        struggleTarget = null;
        struggleStrikes = 0;
        struggleRest = 0;
        return true;
    }

    public void endSiege() {
        if (phase != Phase.SIEGING) return;
        phase = Phase.IDLE;
        plan = null;
        stepIndex = 0;
        siegeTarget = null;
        holdOff = PLAN_DONE_HOLD;
        clearStrike();
        resetApproach();
        entity.getNavigation().stop();
    }

    private void tickSiege() {
        LivingEntity target = siegeTarget;
        boolean valid = target != null && target.isAlive() && !target.isRemoved() && target.level() == entity.level()
                && entity.sedationResponse.getChargeTarget() == target && canBreakOut() && siegeAllowed(target);
        if (!valid) {
            endSiege();
            return;
        }
        trappedTicks++;
        announceRage();
        if (planCooldown <= 0 && target.position().distanceToSqr(siegeAim) > SIEGE_DRIFT_SQR) replanSiege();
    }

    private void replanSiege() {
        LivingEntity target = siegeTarget;
        planCooldown = REPLAN_DELAY;
        if (target == null) {
            endSiege();
            return;
        }
        siegeProbeTick = Integer.MIN_VALUE / 2;
        OWBreachPlanner.Plan next = probeSiege(target);
        if (next == null) {
            endSiege();
            return;
        }
        clearStrike();
        resetApproach();
        plan = next;
        stepIndex = 0;
        siegeAim = target.position();
    }

    private boolean siegeAllowed(LivingEntity target) {
        if (!entity.isAlive() || entity.isTame() || !entity.usesAggressiveTaming()) return false;
        if (entity.effectiveBlockBreakLevel() == OWBreakLevel.NONE) return false;
        if (!(entity.level() instanceof ServerLevel level) || !EventHooks.canEntityGrief(level, entity)) return false;
        if (level.dimension().equals(OWDimensions.ARENA)) return false;
        return entity.distanceToSqr(target) <= SIEGE_MAX_DISTANCE * SIEGE_MAX_DISTANCE;
    }

    @Nullable
    private OWBreachPlanner.Plan probeSiege(LivingEntity target) {
        int age = entity.tickCount - siegeProbeTick;
        boolean sameTarget = siegeProbeTarget == target.getId();
        boolean settled = target.position().distanceToSqr(siegeProbeAim) <= SIEGE_DRIFT_SQR;
        int lifetime = siegeProbe != null ? SIEGE_PROBE_TTL : SIEGE_FAILED_TTL;
        if (sameTarget && ((settled && age < lifetime) || age < SIEGE_PROBE_MIN_GAP)) return siegeProbe;
        siegeProbe = OWBreachPlanner.planTo(entity, entity.effectiveBlockBreakLevel(), strikePower(), target);
        siegeProbeTick = entity.tickCount;
        siegeProbeTarget = target.getId();
        siegeProbeAim = target.position();
        return siegeProbe;
    }

    public void release() {
        clearStrike();
        entity.getNavigation().stop();
    }

    public boolean landStrike(@Nullable SoundEvent sound) {
        if (entity.level().isClientSide() || !strikePending || strikeTarget == null) return false;
        landPendingStrike(sound);
        return true;
    }

    private void refreshAgitation() {
        boolean provoked = entity.getActualSleepingBar() > 0
                || entity.sedationResponse.isAlarmed()
                || entity.getLastHurtByMob() != null;
        if (provoked) agitation = AGITATION_MEMORY;
        else if (agitation > 0) agitation--;
    }

    private boolean canBreakOut() {
        if (!entity.isAlive() || entity.isTame() || !entity.usesAggressiveTaming()) return false;
        if (entity.effectiveBlockBreakLevel() == OWBreakLevel.NONE) return false;
        if (entity.isKnockedOut() || entity.isSleeping() || entity.isSedatedFleeing() || entity.isInResurrection()) return false;
        if (entity.isPassenger() || entity.isVehicle()) return false;
        if (entity.getMaxSleepingBar() > 0 && entity.getSleepBarPercent() >= OWAggressiveTaming.FLEE_THRESHOLD) return false;
        if (entity.level().dimension().equals(OWDimensions.ARENA)) return false;
        return agitation > 0;
    }

    private void begin() {
        trappedTicks = 0;
        rageStage = 0;
        if (!replan() && phase == Phase.IDLE && holdOff <= 0) enterStruggle();
    }

    private boolean replan() {
        planCooldown = REPLAN_DELAY;
        clearStrike();
        plan = null;
        stepIndex = 0;
        resetApproach();
        if (!(entity.level() instanceof ServerLevel level) || !EventHooks.canEntityGrief(level, entity)) {
            planCooldown = FAILED_PLAN_DELAY;
            return false;
        }

        double escape = Math.max(probe.last().reach() + ESCAPE_MARGIN, footprint() + 1.5);
        boolean seek = isSeeking();
        Vec3 anchor = seek ? entity.getTarget().position() : danger();
        OWBreachPlanner.Plan next = OWBreachPlanner.plan(entity, entity.effectiveBlockBreakLevel(), strikePower(), anchor, seek, escape);
        if (next == null) {
            planCooldown = FAILED_PLAN_DELAY;
            return false;
        }
        if (next.steps().isEmpty()) {
            phase = Phase.IDLE;
            holdOff = EMPTY_PLAN_HOLD;
            return false;
        }
        plan = next;
        phase = Phase.BREACHING;
        struggleStrikes = 0;
        struggleTarget = null;
        return true;
    }

    private void enterStruggle() {
        phase = Phase.STRUGGLING;
        plan = null;
        planCooldown = FAILED_PLAN_DELAY;
        struggleTarget = null;
        struggleStrikes = 0;
        struggleRest = 0;
        struggleBudget = STRUGGLE_STRIKES_MIN + entity.getRandom().nextInt(STRUGGLE_STRIKES_MAX - STRUGGLE_STRIKES_MIN + 1);
    }

    private void reset() {
        if (phase != Phase.IDLE || strikePending) entity.getNavigation().stop();
        phase = Phase.IDLE;
        plan = null;
        siegeTarget = null;
        stepIndex = 0;
        clearStrike();
        trappedTicks = 0;
        rageStage = 0;
        struggleTarget = null;
        struggleStrikes = 0;
        struggleRest = 0;
        resetApproach();
    }

    private void driveBreach() {
        if (tickPendingStrike()) return;
        OWBreachPlanner.Step step = currentStep();
        if (step == null) {
            if (phase == Phase.SIEGING) driveAssault();
            else finishPlan();
            return;
        }
        BlockPos block = nextBlock(step);
        if (block == null) return;
        if (!breakableNow(block)) {
            if (planCooldown <= 0) onPlanBroken();
            else entity.getNavigation().stop();
            return;
        }
        if (distanceTo(block) <= strikeReach()) {
            strikeAt(block);
            return;
        }
        if (approachNode(step.from()) && planCooldown <= 0) onPlanBroken();
    }

    private void driveAssault() {
        LivingEntity target = siegeTarget;
        if (target == null || plan == null) {
            endSiege();
            return;
        }
        if (assaultCooldown > 0) assaultCooldown--;
        if (canHit(target)) {
            entity.getNavigation().stop();
            facePoint(target.position());
            entity.getLookControl().setLookAt(target, 30f, 30f);
            if (assaultCooldown > 0) return;
            if (!entity.isCombo()) {
                entity.setCombo(true, 1);
                assaultCooldown = ASSAULT_COOLDOWN;
            } else if (entity.isPauseCombo()) {
                entity.playerContinueCombo = true;
                assaultCooldown = ASSAULT_COOLDOWN;
            }
            return;
        }
        if (approachNode(plan.end()) && planCooldown <= 0) replanSiege();
    }

    private boolean canHit(LivingEntity target) {
        double reach = Math.max(MIN_HIT_REACH, entity.getBbWidth() * 0.5 + HIT_REACH_MARGIN);
        double dx = target.getX() - entity.getX();
        double dz = target.getZ() - entity.getZ();
        double gap = Math.sqrt(dx * dx + dz * dz) - target.getBbWidth() * 0.5;
        double rise = target.getY() - entity.getY();
        if (gap > reach || rise > Math.max(MIN_HIT_RISE, entity.getBbHeight() - HIT_RISE_MARGIN)) return false;
        return rise > -(entity.getBbHeight() + reach) && entity.hasLineOfSight(target);
    }

    private void onPlanBroken() {
        if (phase == Phase.SIEGING) {
            replanSiege();
            return;
        }
        if (!replan() && phase == Phase.BREACHING) enterStruggle();
    }

    private void driveStruggle() {
        if (tickPendingStrike() || struggleRest > 0) return;
        if (struggleStrikes >= struggleBudget) {
            struggleRest = STRUGGLE_REST_MIN + entity.getRandom().nextInt(STRUGGLE_REST_MAX - STRUGGLE_REST_MIN + 1);
            struggleStrikes = 0;
            struggleTarget = null;
            entity.getNavigation().stop();
            return;
        }
        if (struggleTarget == null || !isWall(struggleTarget)) {
            struggleTarget = pickStruggleTarget();
            resetApproach();
        }
        if (struggleTarget == null) {
            struggleRest = STRUGGLE_REST_MIN;
            return;
        }
        if (distanceTo(struggleTarget) <= strikeReach()) {
            if (strikeAt(struggleTarget)) struggleStrikes++;
            return;
        }
        Vec3 wall = Vec3.atBottomCenterOf(struggleTarget);
        if (approach(wall, wall)) struggleTarget = null;
    }

    private boolean tickPendingStrike() {
        if (strikePending) {
            if (strikeTarget != null) faceBlock(strikeTarget);
            if (++strikeWait > STRIKE_TIMEOUT) {
                if (entity.isCombo()) entity.resetCombo(0);
                landPendingStrike(null);
            }
            return true;
        }
        if (strikePause > 0) {
            strikePause--;
            if (strikeTarget != null) faceBlock(strikeTarget);
            return true;
        }
        return false;
    }

    private boolean strikeAt(BlockPos block) {
        entity.getNavigation().stop();
        boolean facing = faceBlock(block);
        if (!facing || entity.isCombo()) return false;
        strikeTarget = block.immutable();
        strikePending = true;
        strikeWait = 0;
        entity.setCombo(true, 1);
        return true;
    }

    private void landPendingStrike(@Nullable SoundEvent sound) {
        BlockPos target = strikeTarget;
        strikePending = false;
        strikeWait = 0;
        strikePause = phase == Phase.STRUGGLING
                ? STRUGGLE_PAUSE_MIN + entity.getRandom().nextInt(STRUGGLE_PAUSE_MAX - STRUGGLE_PAUSE_MIN + 1)
                : STRIKE_PAUSE_MIN + entity.getRandom().nextInt(STRIKE_PAUSE_MAX - STRIKE_PAUSE_MIN + 1);
        if (target != null) applyStrike(target, sound);
    }

    private void applyStrike(BlockPos pos, @Nullable SoundEvent sound) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        BlockState state = level.getBlockState(pos);
        if (!OWBlockToughness.isObstacle(state, level, pos)) return;

        Vec3 impact = impactPoint(pos);
        float pitch = 0.85f + entity.getRandom().nextFloat() * 0.3f;
        if (sound != null) level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, SoundSource.HOSTILE, 0.8f, pitch);
        SoundType soundType = state.getSoundType(level, pos, entity);
        level.playSound(null, pos, soundType.getHitSound(), SoundSource.BLOCKS, 1.0f, soundType.getPitch() * 0.6f);

        int tier = OWBlockToughness.tierOf(state, level, pos);
        int breakLevel = entity.effectiveBlockBreakLevel();
        if (!OWBreakLevel.canBreak(breakLevel, tier) || !CommonHooks.canEntityDestroy(level, pos, entity)) {
            deflect(level, pos, tier, impact);
            return;
        }

        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), impact.x, impact.y, impact.z, 12, 0.2, 0.2, 0.2, 0.15);
        OWBlockDamageTracker tracker = OWBlockDamageTracker.of(level);
        float power = strikePower();
        boolean broken = tracker.damage(pos, state, power * OWBreakLevel.advantage(breakLevel, tier));
        accent(level, pos, tier, broken);
        if (broken) breakBlock(level, pos);
        splash(level, tracker, pos, power, breakLevel);
    }

    private void splash(ServerLevel level, OWBlockDamageTracker tracker, BlockPos origin, float power, int breakLevel) {
        if ((phase != Phase.BREACHING && phase != Phase.SIEGING) || plan == null || footprint() < 2 || stepIndex >= plan.steps().size()) return;
        for (BlockPos pos : plan.steps().get(stepIndex).blocks()) {
            if (pos.equals(origin) || pos.distSqr(origin) > SPLASH_RADIUS_SQR) continue;
            BlockState state = level.getBlockState(pos);
            if (!OWBlockToughness.isObstacle(state, level, pos)) continue;
            int tier = OWBlockToughness.tierOf(state, level, pos);
            if (!OWBreakLevel.canBreak(breakLevel, tier) || !CommonHooks.canEntityDestroy(level, pos, entity)) continue;
            Vec3 center = Vec3.atCenterOf(pos);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), center.x, center.y, center.z, 4, 0.3, 0.3, 0.3, 0.1);
            if (tracker.damage(pos, state, power * OWBreakLevel.advantage(breakLevel, tier) * SPLASH_FACTOR)) {
                accent(level, pos, tier, true);
                breakBlock(level, pos);
            }
        }
    }

    private void breakBlock(ServerLevel level, BlockPos pos) {
        level.destroyBlock(pos, true, entity);
        OWPlacedBlocks.get(level).forget(pos);
        probe.invalidate();
    }

    private void accent(ServerLevel level, BlockPos pos, int tier, boolean broken) {
        SoundEvent accent = null;
        if (tier == OWBreakLevel.WOOD) {
            accent = broken ? SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR : SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR;
        } else if (tier >= OWBreakLevel.METAL && !broken) {
            accent = SoundEvents.ZOMBIE_ATTACK_IRON_DOOR;
        }
        if (accent == null) return;
        level.playSound(null, pos, accent, SoundSource.HOSTILE, broken ? 0.7f : 0.5f, 0.8f + entity.getRandom().nextFloat() * 0.25f);
    }

    private void deflect(ServerLevel level, BlockPos pos, int tier, Vec3 impact) {
        level.sendParticles(ParticleTypes.CRIT, impact.x, impact.y, impact.z, 8, 0.15, 0.15, 0.15, 0.25);
        SoundEvent clang = tier >= OWBreakLevel.METAL ? SoundEvents.ZOMBIE_ATTACK_IRON_DOOR : SoundEvents.PLAYER_ATTACK_NODAMAGE;
        level.playSound(null, pos, clang, SoundSource.HOSTILE, 0.6f, 0.7f + entity.getRandom().nextFloat() * 0.2f);

        Vec3 push = entity.position().subtract(Vec3.atCenterOf(pos)).multiply(1.0, 0.0, 1.0);
        if (push.lengthSqr() > 1.0E-4) {
            push = push.normalize().scale(RECOIL);
            entity.setDeltaMovement(entity.getDeltaMovement().add(push.x, 0.05, push.z));
            entity.hasImpulse = true;
        }
        if (entity.getRandom().nextFloat() < 0.35f) {
            Vec3 head = entity.getSedationHeadPosition();
            level.sendParticles(ParticleTypes.ANGRY_VILLAGER, head.x, head.y + 0.3, head.z, 2, 0.2, 0.1, 0.2, 0.0);
        }
    }

    private void announceRage() {
        int stage = rage() >= 1f ? 2 : rage() >= 0.5f ? 1 : 0;
        if (stage <= rageStage) return;
        rageStage = stage;
        if (!(entity.level() instanceof ServerLevel level)) return;
        Vec3 head = entity.getSedationHeadPosition();
        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, head.x, head.y + 0.3, head.z, 3 + stage * 2, 0.3, 0.15, 0.3, 0.0);
        entity.playSedationVoice(0.65f - stage * 0.05f);
    }

    private float strikePower() {
        float damage = (float) entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float factor = Mth.clamp(damage / DAMAGE_REFERENCE, MIN_DAMAGE_FACTOR, MAX_DAMAGE_FACTOR);
        float drowsy = 1f;
        if (entity.getMaxSleepingBar() > 0) {
            float sleepiness = Mth.clamp(entity.getSleepBarPercent() / (float) OWAggressiveTaming.FLEE_THRESHOLD, 0f, 1f);
            drowsy -= DROWSY_PENALTY * sleepiness;
        }
        return factor * drowsy * (1f + RAGE_BONUS * rage());
    }

    @Nullable
    private OWBreachPlanner.Step currentStep() {
        while (plan != null && stepIndex < plan.steps().size()) {
            OWBreachPlanner.Step step = plan.steps().get(stepIndex);
            if (nextBlock(step) != null) return step;
            stepIndex++;
            resetApproach();
        }
        return null;
    }

    @Nullable
    private BlockPos nextBlock(OWBreachPlanner.Step step) {
        BlockPos best = null;
        double bestScore = Double.MAX_VALUE;
        for (BlockPos pos : step.blocks()) {
            if (!OWBlockToughness.isObstacle(entity.level().getBlockState(pos), entity.level(), pos)) continue;
            double score = (pos.getY() - step.to().getY()) * 2.0 + distanceTo(pos);
            if (score < bestScore) {
                bestScore = score;
                best = pos;
            }
        }
        return best;
    }

    private boolean breakableNow(BlockPos pos) {
        BlockState state = entity.level().getBlockState(pos);
        int tier = OWBlockToughness.tierOf(state, entity.level(), pos);
        return OWBreakLevel.canBreak(entity.effectiveBlockBreakLevel(), tier) && state.canEntityDestroy(entity.level(), pos, entity);
    }

    private void finishPlan() {
        if (phase == Phase.SIEGING) {
            endSiege();
            return;
        }
        plan = null;
        stepIndex = 0;
        phase = Phase.IDLE;
        holdOff = PLAN_DONE_HOLD;
        probe.invalidate();
        entity.getNavigation().stop();
    }

    private boolean approachNode(BlockPos node) {
        Vec3 navigationTarget = new Vec3(node.getX() + 0.5, node.getY(), node.getZ() + 0.5);
        return approach(navigationTarget, OWBreachPlanner.nodeCenter(node, footprint()));
    }

    private boolean approach(Vec3 navigationTarget, Vec3 target) {
        double distance = entity.position().distanceTo(target);
        if (distance < approachBest - APPROACH_PROGRESS) {
            approachBest = distance;
            approachTimer = 0;
        } else if (++approachTimer > APPROACH_STUCK_TICKS) {
            approachTimer = 0;
            approachBest = distance;
            approachStrikes++;
            return approachStrikes >= 2;
        }
        if (entity.getNavigation().isDone() || approachTimer % APPROACH_REPATH == 0) {
            entity.getNavigation().moveTo(navigationTarget.x, navigationTarget.y, navigationTarget.z, approachSpeed());
        }
        return false;
    }

    private void resetApproach() {
        approachTimer = 0;
        approachBest = Double.MAX_VALUE;
        approachStrikes = 0;
    }

    private double approachSpeed() {
        return Math.max(1.0, entity.sedatedFleeSpeed() * APPROACH_SPEED_FACTOR);
    }

    private double strikeReach() {
        return STRIKE_REACH + (approachStrikes > 0 ? STUCK_REACH_BONUS : 0.0);
    }

    private boolean faceBlock(BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        entity.getLookControl().setLookAt(center.x, center.y, center.z, 30f, 30f);
        return facePoint(center);
    }

    private boolean facePoint(Vec3 center) {
        double dx = center.x - entity.getX();
        double dz = center.z - entity.getZ();
        if (dx * dx + dz * dz < 0.25) return true;
        float wanted = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f;
        float yaw = Mth.approachDegrees(entity.getYRot(), wanted, TURN_SPEED);
        entity.setYRot(yaw);
        entity.setYBodyRot(yaw);
        entity.setYHeadRot(yaw);
        return Math.abs(Mth.wrapDegrees(wanted - yaw)) <= FACING_TOLERANCE;
    }

    private double distanceTo(BlockPos pos) {
        AABB box = entity.getBoundingBox();
        double dx = Math.max(0.0, Math.max(pos.getX() - box.maxX, box.minX - (pos.getX() + 1)));
        double dy = Math.max(0.0, Math.max(pos.getY() - box.maxY, box.minY - (pos.getY() + 1)));
        double dz = Math.max(0.0, Math.max(pos.getZ() - box.maxZ, box.minZ - (pos.getZ() + 1)));
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private Vec3 impactPoint(BlockPos pos) {
        double y = entity.getY() + entity.getBbHeight() * 0.5;
        return new Vec3(
                Mth.clamp(entity.getX(), pos.getX(), pos.getX() + 1.0),
                Mth.clamp(y, pos.getY(), pos.getY() + 1.0),
                Mth.clamp(entity.getZ(), pos.getZ(), pos.getZ() + 1.0));
    }

    @Nullable
    private BlockPos pickStruggleTarget() {
        Vec3 danger = danger();
        double base = danger != null
                ? Math.atan2(entity.getZ() - danger.z, entity.getX() - danger.x)
                : entity.getRandom().nextDouble() * Math.PI * 2.0;
        double start = entity.getBbWidth() * 0.5;
        for (int attempt = 0; attempt < 8; attempt++) {
            double spread = attempt < 4 ? Math.PI * 0.5 : Math.PI * 2.0;
            double angle = base + (entity.getRandom().nextDouble() - 0.5) * spread;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            for (double distance = start; distance <= start + STRUGGLE_LOOKUP; distance += 0.5) {
                BlockPos pos = BlockPos.containing(entity.getX() + cos * distance, entity.getY() + 0.5, entity.getZ() + sin * distance);
                if (isWall(pos)) return pos;
            }
        }
        return null;
    }

    private boolean isWall(BlockPos pos) {
        BlockState state = entity.level().getBlockState(pos);
        return OWBlockToughness.isObstacle(state, entity.level(), pos) && !OWBlockToughness.isSteppable(state, entity.level(), pos);
    }

    private boolean isSeeking() {
        return entity.getTarget() instanceof Player && entity.sedationResponse.getThreatPosition() == null;
    }

    @Nullable
    private Vec3 danger() {
        Vec3 threat = entity.sedationResponse.getThreatPosition();
        if (threat != null) return threat;
        Entity culprit = entity.aggressiveTaming.getThreat();
        if (culprit != null) return culprit.position();
        LivingEntity target = entity.getTarget();
        return target != null ? target.position() : null;
    }

    private int footprint() {
        return Mth.floor(entity.getBbWidth() + 1.0f);
    }

    private void clearStrike() {
        strikePending = false;
        strikeTarget = null;
        strikeWait = 0;
        strikePause = 0;
    }
}
