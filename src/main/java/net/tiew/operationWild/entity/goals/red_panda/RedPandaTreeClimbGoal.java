package net.tiew.operationWild.entity.goals.red_panda;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class RedPandaTreeClimbGoal extends Goal {

    private enum Phase { APPROACH, ATTACH, ASCEND, SIDESTEP, TO_BRANCH, PERCH, TO_TRUNK, TOP_PAUSE, FLIP, DESCEND }

    private static final int SEARCH_RADIUS = 6;
    private static final int SEARCH_SAMPLES = 48;
    private static final int MIN_TRUNK_HEIGHT = 4;
    private static final int MAX_CLIMB_HEIGHT = 18;
    private static final int MIN_BRANCH_HEIGHT = 3;
    private static final int TRY_CHANCE = 90;

    private static final int APPROACH_TIMEOUT = 220;
    private static final double CONTACT_SLACK = 0.12;
    private static final double STALLED_CONTACT_SLACK = 0.55;
    private static final double APPROACH_CREEP_RANGE_SQR = 2.6 * 2.6;
    private static final double APPROACH_LOOK_RANGE_SQR = 4.0 * 4.0;
    private static final double APPROACH_PROGRESS_EPSILON = 0.01;
    private static final int APPROACH_STALL_TICKS = 50;
    private static final int RETARGET_REACH = 2;
    private static final double ATTACH_HEIGHT_TOLERANCE = 1.6;
    private static final int FAILED_APPROACH_COOLDOWN = 100;
    private static final double ESCAPE_SPEED_FACTOR = 2.2;
    private static final double ESCAPE_ASCEND_FACTOR = 1.9;
    private static final double ESCAPE_SAFE_DISTANCE_SQR = 16.0 * 16.0;
    private static final int ESCAPE_ALERT_REFRESH = 20;
    private static final float PACE_ATTACH = 0.7f;
    private static final float PACE_ASCEND = 1.0f;
    private static final float PACE_SHUFFLE = 0.8f;
    private static final float PACE_DESCEND = 1.8f;
    private static final int MIN_BAMBOO_LEFT = MIN_TRUNK_HEIGHT;
    private static final double HARVEST_PICKUP_RANGE = 3.0;
    private static final int HARVEST_DELAY_TICKS = 22;
    private static final int SWIPE_STRIKE_TICKS = 7;
    private static final double STALK_CLAIM_REACH = 2.0;
    private static final double STALK_CLAIM_HEIGHT = 24.0;

    private static final double HUG_LOG = 0.70;
    private static final double HUG_BAMBOO = 0.30;
    private static final double RELEASE_CLEARANCE = 0.25;

    private static final double ASCEND_SPEED = 0.075;
    private static final double DESCEND_SPEED = 0.135;

    private static final double ATTACH_GLIDE_SPEED = 0.11;
    private static final int ATTACH_TICKS_MIN = 8;
    private static final int ATTACH_TICKS_MAX = 24;
    private static final int STEP_OUT_TICKS = 20;
    private static final int RETURN_TICKS = 18;
    private static final int ORBIT_TICKS_PER_QUARTER = 13;
    private static final int LEAN_LEAD_TICKS = 6;
    private static final int LEAN_SPAN_TICKS = 12;
    private static final float RETURN_LEAN_LEAD = 1.6f;
    private static final double SIDESTEP_LOOKAHEAD = 0.9;
    private static final double BRANCH_RADIUS = 1.0;
    private static final int FLIP_TICKS = 18;
    private static final int PERCH_MIN = 180;
    private static final int PERCH_MAX = 460;
    private static final int PERCH_NAP_CHANCE = 2;
    private static final int PERCH_LOOK_INTERVAL = 50;
    private static final int PERCH_SETTLE_TICKS = 60;
    private static final int PERCH_LEAVE_CHANCE = 800;

    private static final double LATERAL_MAX = 0.24;
    private static final double LATERAL_MAX_BAMBOO = 0.08;
    private static final float LATERAL_RESPONSE = 0.08f;
    private static final int LATERAL_MIN_INTERVAL = 14;
    private static final int LATERAL_MAX_INTERVAL = 32;
    private static final float LATERAL_YAW_GAIN = 26f;
    private static final float YAW_TURN_RATE = 14f;

    private static final int FALL_GRACE_TICKS = 80;
    private static final int COOLDOWN_TICKS = 450;
    private static final int TOP_PAUSE_MIN = 100;
    private static final int TOP_PAUSE_MAX = 200;
    private static final int TOP_LOOK_INTERVAL = 35;

    private final RedPandaEntity panda;
    private final double speedModifier;

    private Phase phase = Phase.APPROACH;
    private boolean finished;
    private boolean escapeClimb;
    private LivingEntity escapeThreat;
    private boolean harvested;
    private int harvestDelay;
    private int harvestStrike;
    private int cooldown;

    private BlockPos column;
    private Direction face;
    private double columnCx;
    private double columnCz;
    private double hugDistance;
    private double lateralRange;
    private boolean bambooColumn;
    private int columnTop;
    private int climbCeiling;

    private double climbY;
    private int approachTicks;
    private double lastApproachDistSqr;
    private int stalledTicks;
    private int phaseTicks;

    private Vec3 attachFrom = Vec3.ZERO;
    private int attachDuration;

    private BlockPos branch;
    private Direction branchFace;
    private double orbitFrom;
    private double orbitDelta;
    private double orbitStartY;
    private int orbitTicks;
    private int branchDuration;

    private Direction sidestepFace;
    private double sidestepFrom;
    private double sidestepDelta;
    private int sidestepTicks;
    private Phase sidestepResume;

    private float turnFrom;
    private float turnDelta;

    private Vec3 stepFrom = Vec3.ZERO;
    private Vec3 stepTo = Vec3.ZERO;
    private int perchTicks;
    private boolean perchNapping;

    private float lateral;
    private float lateralTarget;
    private int lateralTimer;

    public RedPandaTreeClimbGoal(RedPandaEntity panda, double speedModifier) {
        this.panda = panda;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        this.escapeThreat = panda.getAlertSource();
        this.escapeClimb = panda.isAlerted() && escapeThreat != null;

        if (cooldown > 0) {
            cooldown--;
            if (!escapeClimb) return false;
        }

        if (!isAvailable() || !panda.onGround()) return false;
        if (panda.isNapping()) return false;
        if (!escapeClimb && panda.getRandom().nextInt(TRY_CHANCE) != 0) return false;

        BlockPos base = findColumnBase();
        if (base == null) return false;

        Direction chosen = pickFace(base);
        if (chosen == null) return false;

        adoptColumn(base, chosen);
        return true;
    }

    private void adoptColumn(BlockPos base, Direction chosen) {
        BlockState baseState = panda.level().getBlockState(base);
        Vec3 offset = baseState.getOffset(panda.level(), base);

        this.column = base;
        this.face = chosen;
        this.columnCx = base.getX() + 0.5 + offset.x;
        this.columnCz = base.getZ() + 0.5 + offset.z;
        this.columnTop = findColumnTop(base);
        this.bambooColumn = baseState.is(Blocks.BAMBOO);
        this.hugDistance = bambooColumn ? HUG_BAMBOO : HUG_LOG;
        this.lateralRange = bambooColumn ? LATERAL_MAX_BAMBOO : LATERAL_MAX;
        this.climbCeiling = canHarvest() ? columnTop - 1 : columnTop;
    }

    private boolean retargetNearbyColumn() {
        Level level = panda.level();
        BlockPos origin = panda.blockPosition();

        for (int dx = -RETARGET_REACH; dx <= RETARGET_REACH; dx++) {
            for (int dz = -RETARGET_REACH; dz <= RETARGET_REACH; dz++) {
                for (int dy = 1; dy >= -1; dy--) {
                    BlockPos probe = origin.offset(dx, dy, dz);
                    if (!isTrunkBlock(level.getBlockState(probe))) continue;

                    BlockPos base = probe;
                    for (int step = 0; step < 6; step++) {
                        BlockPos below = base.below();
                        if (!isTrunkBlock(level.getBlockState(below))) break;
                        base = below;
                    }
                    if (base.equals(column)) continue;
                    if (findColumnTop(base) - base.getY() + 1 < MIN_TRUNK_HEIGHT) continue;
                    if (level.getBlockState(base).is(Blocks.BAMBOO) && stalkTaken(base)) continue;

                    Direction chosen = pickFace(base);
                    if (chosen == null) continue;

                    adoptColumn(base.immutable(), chosen);
                    panda.setClimbColumn(bambooColumn ? column : null);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (finished || column == null) return false;
        if (!isAvailable()) return false;
        if (phase == Phase.APPROACH && approachTicks > APPROACH_TIMEOUT) return false;
        return hasSupport();
    }

    @Override
    public void start() {
        phase = Phase.APPROACH;
        finished = false;
        approachTicks = 0;
        phaseTicks = 0;
        lastApproachDistSqr = Double.MAX_VALUE;
        stalledTicks = 0;
        branch = null;
        harvested = false;
        lateral = 0f;
        lateralTarget = 0f;
        lateralTimer = 0;

        panda.setClimbColumn(bambooColumn ? column : null);
        panda.setSitting(false);
        panda.getNavigation().moveTo(
                columnCx + face.getStepX() * 0.9,
                column.getY(),
                columnCz + face.getStepZ() * 0.9,
                approachSpeed());
    }

    private boolean touchingColumn(double slack) {
        BlockState state = panda.level().getBlockState(column);
        VoxelShape shape = state.getCollisionShape(panda.level(), column);

        AABB box = shape.isEmpty()
                ? new AABB(column)
                : shape.bounds().move(column);

        return panda.getBoundingBox().intersects(box.inflate(slack));
    }

    private boolean hasSupport() {
        if (column == null) return false;
        if (phase == Phase.APPROACH) return true;

        if (phase == Phase.PERCH || phase == Phase.TO_BRANCH || phase == Phase.TO_TRUNK) {
            return branch != null && panda.level().getBlockState(branch).is(BlockTags.LOGS);
        }

        int level = Mth.floor(climbY);
        return gripAt(level) || gripAt(level - 1);
    }

    private boolean gripAt(int level) {
        return isTrunkBlock(panda.level().getBlockState(
                new BlockPos(column.getX(), level, column.getZ())));
    }

    private double approachSpeed() {
        return escapeClimb ? speedModifier * ESCAPE_SPEED_FACTOR : speedModifier;
    }

    private double ascendSpeed() {
        return escapeClimb ? ASCEND_SPEED * ESCAPE_ASCEND_FACTOR : ASCEND_SPEED;
    }

    private float ascendPace() {
        return escapeClimb ? (float) (PACE_ASCEND * ESCAPE_ASCEND_FACTOR) : PACE_ASCEND;
    }

    private double attachGlideSpeed() {
        return escapeClimb ? ATTACH_GLIDE_SPEED * ESCAPE_ASCEND_FACTOR : ATTACH_GLIDE_SPEED;
    }

    @Override
    public void stop() {
        boolean reachedTrunk = phase != Phase.APPROACH && !escapeClimb;

        release();
        panda.setClimbColumn(null);
        column = null;
        branch = null;
        phase = Phase.APPROACH;
        finished = false;
        escapeClimb = false;
        escapeThreat = null;
        harvested = false;

        int base = reachedTrunk ? COOLDOWN_TICKS : FAILED_APPROACH_COOLDOWN;
        cooldown = base + panda.getRandom().nextInt(base);
    }

    @Override
    public void tick() {
        if (escapeClimb && panda.tickCount % ESCAPE_ALERT_REFRESH == 0 && holdingHigh()) {
            panda.raiseAlert(escapeThreat);
        }

        switch (phase) {
            case APPROACH -> tickApproach();
            case ATTACH -> tickAttach();
            case ASCEND -> tickAscend();
            case SIDESTEP -> tickSidestep();
            case TO_BRANCH -> tickBranchMove();
            case TO_TRUNK -> tickTrunkReturn();
            case PERCH -> tickPerch();
            case TOP_PAUSE -> tickTopPause();
            case FLIP -> tickFlip();
            case DESCEND -> tickDescend();
        }
    }

    private void tickApproach() {
        approachTicks++;

        double dx = panda.getX() - columnCx;
        double dz = panda.getZ() - columnCz;
        double distSqr = dx * dx + dz * dz;

        if (distSqr <= APPROACH_LOOK_RANGE_SQR) {
            panda.getLookControl().setLookAt(columnCx, column.getY() + 2.0, columnCz);
        }

        double slack = stalledTicks >= APPROACH_STALL_TICKS ? STALLED_CONTACT_SLACK : CONTACT_SLACK;
        boolean levelWithBase = Math.abs(panda.getY() - column.getY()) <= ATTACH_HEIGHT_TOLERANCE;

        if (levelWithBase && touchingColumn(slack)) {
            Direction reached = nearestClearFace();
            if (reached != null) face = reached;
            beginAttach();
            return;
        }

        if (distSqr < lastApproachDistSqr - APPROACH_PROGRESS_EPSILON) {
            lastApproachDistSqr = distSqr;
            stalledTicks = 0;
        } else if (++stalledTicks == APPROACH_STALL_TICKS && retargetNearbyColumn()) {
            lastApproachDistSqr = Double.MAX_VALUE;
            stalledTicks = 0;
            panda.getNavigation().stop();
            return;
        }

        if (!panda.getNavigation().isDone()) return;

        if (distSqr > APPROACH_CREEP_RANGE_SQR) {
            panda.getNavigation().moveTo(
                    columnCx + face.getStepX() * 0.9,
                    column.getY(),
                    columnCz + face.getStepZ() * 0.9,
                    approachSpeed());
        } else {
            panda.getMoveControl().setWantedPosition(columnCx, column.getY(), columnCz, approachSpeed());
        }
    }

    private @Nullable Direction nearestClearFace() {
        double dx = panda.getX() - columnCx;
        double dz = panda.getZ() - columnCz;

        Direction best = null;
        double bestDot = -Double.MAX_VALUE;

        for (Direction candidate : Direction.Plane.HORIZONTAL) {
            if (!faceClear(candidate, column.getY()) || !faceClear(candidate, column.getY() + 1)) continue;

            double dot = dx * candidate.getStepX() + dz * candidate.getStepZ();
            if (dot > bestDot) {
                bestDot = dot;
                best = candidate;
            }
        }
        return best;
    }

    private void beginAttach() {
        panda.getNavigation().stop();
        panda.noPhysics = true;
        panda.setNoGravity(true);
        panda.setTreeClimbing(true);
        panda.setTreeLeanTarget(1f);
        panda.setTreeFlipTarget(0f);

        panda.setClimbPace(PACE_ATTACH);
        climbY = column.getY() + 0.35;
        lateral = 0f;
        lateralTarget = 0f;

        attachFrom = panda.position();
        attachDuration = Mth.clamp(
                (int) Math.round(attachFrom.distanceTo(attachPoint(climbY)) / attachGlideSpeed()),
                ATTACH_TICKS_MIN, ATTACH_TICKS_MAX);

        phase = Phase.ATTACH;
        phaseTicks = 0;

        panda.level().playSound(null, panda.getX(), panda.getY(), panda.getZ(),
                SoundEvents.WOOD_STEP, SoundSource.NEUTRAL, 0.55f,
                (float) OWUtils.generateRandomInterval(1.3, 1.6));
    }

    private void tickAttach() {
        phaseTicks++;
        float progress = Mth.clamp(phaseTicks / (float) attachDuration, 0f, 1f);
        float eased = progress * progress * (3f - 2f * progress);

        Vec3 target = attachPoint(climbY);
        pin(new Vec3(
                Mth.lerp(eased, attachFrom.x, target.x),
                Mth.lerp(eased, attachFrom.y, target.y) + arc(progress) * 0.55f,
                Mth.lerp(eased, attachFrom.z, target.z)));
        turnTowards(columnCx, columnCz);

        if (progress < 1f) return;
        panda.setClimbPace(ascendPace());
        phase = Phase.ASCEND;
    }

    private void tickAscend() {
        climbY += ascendSpeed();
        applyClimbPosition(climbY);

        if (panda.tickCount % 7 == 0) playGripSound(0.35f);

        int level = Mth.floor(climbY);
        boolean overTop = climbY > climbCeiling
                || climbY - column.getY() > MAX_CLIMB_HEIGHT
                || !isTrunkBlock(panda.level().getBlockState(new BlockPos(column.getX(), level, column.getZ())));

        BlockPos found = level - column.getY() >= MIN_BRANCH_HEIGHT ? findBranch(level) : null;
        Direction toFace = found != null ? directionOf(found) : null;
        if (toFace != null) {
            beginBranchMove(found, toFace);
            return;
        }

        if (overTop) {
            if (holdingHigh()) {
                climbY = Math.min(climbY, climbCeiling);
                applyClimbPosition(climbY);
                return;
            }
            beginTopPause();
            return;
        }

        int ahead = Mth.floor(climbY + SIDESTEP_LOOKAHEAD);
        if (!faceClear(face, ahead)) beginSidestep(ahead, Phase.ASCEND);
    }

    private boolean beginSidestep(int level, Phase resume) {
        Direction clear = findClearFace(level);
        if (clear == null) return false;

        sidestepFace = clear;
        sidestepResume = resume;
        sidestepFrom = angleOf(face);
        sidestepDelta = Mth.wrapDegrees((float) Math.toDegrees(angleOf(clear) - sidestepFrom)) * Mth.DEG_TO_RAD;
        sidestepTicks = Math.max(ORBIT_TICKS_PER_QUARTER, (int) Math.round(
                Math.abs(sidestepDelta) / (Math.PI / 2.0) * ORBIT_TICKS_PER_QUARTER));

        panda.setClimbPace(PACE_SHUFFLE);
        phase = Phase.SIDESTEP;
        phaseTicks = 0;
        return true;
    }

    private void tickSidestep() {
        phaseTicks++;
        float progress = Mth.clamp(phaseTicks / (float) sidestepTicks, 0f, 1f);
        float eased = smooth(progress);

        double angle = sidestepFrom + sidestepDelta * eased;
        pin(new Vec3(
                columnCx + Math.cos(angle) * hugDistance,
                climbY,
                columnCz + Math.sin(angle) * hugDistance));
        turnTowards(columnCx, columnCz);

        if (panda.tickCount % 6 == 0) playGripSound(0.30f);
        if (progress < 1f) return;

        face = sidestepFace;
        lateral = 0f;
        lateralTarget = 0f;

        if (sidestepResume == Phase.FLIP) {
            beginFlip();
            return;
        }

        panda.setClimbPace(sidestepResume == Phase.DESCEND ? PACE_DESCEND : ascendPace());
        phase = sidestepResume;
    }

    private double routeAround(double delta, int level) {
        if (Math.abs(delta) <= Math.PI / 2.0 + 1.0e-3) return delta;

        Direction via = delta > 0 ? face.getClockWise() : face.getCounterClockWise();
        if (faceClear(via, level)) return delta;

        Direction other = delta > 0 ? face.getCounterClockWise() : face.getClockWise();
        if (!faceClear(other, level)) return delta;

        return delta - Math.signum(delta) * 2.0 * Math.PI;
    }

    private boolean faceClear(Direction direction, int level) {
        return isPassable(new BlockPos(
                column.getX() + direction.getStepX(), level, column.getZ() + direction.getStepZ()));
    }

    private @Nullable Direction findClearFace(int level) {
        Direction best = null;
        double bestTurn = Double.MAX_VALUE;

        for (Direction candidate : Direction.Plane.HORIZONTAL) {
            if (candidate == face || !faceClear(candidate, level)) continue;

            double turn = Math.abs(Mth.wrapDegrees(candidate.toYRot() - face.toYRot()));
            if (turn < bestTurn) {
                bestTurn = turn;
                best = candidate;
            }
        }
        return best;
    }

    private void beginBranchMove(BlockPos foundBranch, Direction toFace) {
        this.branch = foundBranch;
        this.branchFace = toFace;
        this.orbitFrom = angleOf(face);
        this.orbitDelta = routeAround(
                Mth.wrapDegrees((float) Math.toDegrees(angleOf(toFace) - orbitFrom)) * Mth.DEG_TO_RAD,
                foundBranch.getY());
        this.orbitStartY = climbY;
        this.orbitTicks = (int) Math.round(Math.abs(orbitDelta) / (Math.PI / 2.0) * ORBIT_TICKS_PER_QUARTER);
        this.branchDuration = orbitTicks + STEP_OUT_TICKS;

        beginTurn(toFace.toYRot());
        panda.setTreeFlipTarget(0f);
        panda.setClimbPace(PACE_SHUFFLE);

        this.phase = Phase.TO_BRANCH;
        this.phaseTicks = 0;
    }

    private void tickBranchMove() {
        phaseTicks++;

        float orbit = orbitTicks <= 0 ? 1f : Mth.clamp(phaseTicks / (float) orbitTicks, 0f, 1f);
        float out = Mth.clamp((phaseTicks - orbitTicks) / (float) STEP_OUT_TICKS, 0f, 1f);
        float rise = Mth.clamp(phaseTicks / (orbitTicks <= 0 ? STEP_OUT_TICKS * 0.4f : orbitTicks * 0.5f), 0f, 1f);

        int leanStart = Math.max(1, orbitTicks - LEAN_LEAD_TICKS);
        float leanPhase = Mth.clamp((phaseTicks - leanStart) / (float) LEAN_SPAN_TICKS, 0f, 1f);
        panda.setTreeLeanTarget(1f - smooth(leanPhase));

        double angle = orbitFrom + orbitDelta * smooth(orbit);
        double radius = Mth.lerp(smooth(out), hugDistance, BRANCH_RADIUS);
        double y = Mth.lerp(smooth(rise), orbitStartY, branch.getY() + 1.0) + arc(out) * 0.7f;

        pin(new Vec3(columnCx + Math.cos(angle) * radius, y, columnCz + Math.sin(angle) * radius));
        applyTurn(smooth(Mth.clamp(phaseTicks / (float) branchDuration, 0f, 1f)));

        if (panda.tickCount % 6 == 0) playGripSound(0.30f);
        if (phaseTicks < branchDuration) return;

        face = branchFace;
        lateral = 0f;
        lateralTarget = 0f;
        climbY = branch.getY() + 1.0;
        beginPerch();
    }

    private void beginTrunkReturn() {
        stepFrom = panda.position();
        stepTo = attachPoint(branch.getY() + 1.0);

        beginTurn(face.getOpposite().toYRot());
        panda.setTreeFlipTarget(0f);
        panda.setClimbPace(PACE_SHUFFLE);

        phase = Phase.TO_TRUNK;
        phaseTicks = 0;
    }

    private void tickTrunkReturn() {
        phaseTicks++;
        float progress = Mth.clamp(phaseTicks / (float) RETURN_TICKS, 0f, 1f);
        float eased = smooth(progress);

        pin(new Vec3(
                Mth.lerp(eased, stepFrom.x, stepTo.x),
                Mth.lerp(eased, stepFrom.y, stepTo.y) + arc(progress) * 0.7f,
                Mth.lerp(eased, stepFrom.z, stepTo.z)));
        applyTurn(eased);
        panda.setTreeLeanTarget(smooth(Mth.clamp(progress * RETURN_LEAN_LEAD, 0f, 1f)));

        if (progress < 1f) return;

        climbY = stepTo.y;

        int below = Mth.floor(climbY - SIDESTEP_LOOKAHEAD);
        if (!faceClear(face, below) && beginSidestep(below, Phase.FLIP)) return;

        beginFlip();
    }

    private void beginTurn(float targetYaw) {
        turnFrom = panda.getYRot();
        turnDelta = Mth.wrapDegrees(targetYaw - turnFrom);
    }

    private void applyTurn(float progress) {
        float yaw = turnFrom + turnDelta * progress;
        panda.setYRot(yaw);
        panda.yBodyRot = yaw;
        panda.setYHeadRot(yaw);
    }

    private static float smooth(float t) {
        return t * t * (3f - 2f * t);
    }

    private void beginPerch() {
        phase = Phase.PERCH;
        phaseTicks = 0;
        perchTicks = PERCH_MIN + panda.getRandom().nextInt(PERCH_MAX - PERCH_MIN);
        perchNapping = panda.getRandom().nextInt(PERCH_NAP_CHANCE) == 0;

        panda.setTreeLeanTarget(0f);
        panda.setTreeFlipTarget(0f);
        panda.setClimbPace(0f);
        if (perchNapping) panda.setNap(true);

        panda.level().playSound(null, panda.getX(), panda.getY(), panda.getZ(),
                SoundEvents.FOX_AMBIENT, SoundSource.NEUTRAL, 0.5f,
                (float) OWUtils.generateRandomInterval(1.15, 1.35));
    }

    private void tickPerch() {
        pin(new Vec3(branch.getX() + 0.5, branch.getY() + 1.0, branch.getZ() + 0.5));
        phaseTicks++;

        if (holdingHigh()) return;

        if (!perchNapping) {
            if (phaseTicks % PERCH_LOOK_INTERVAL == 0) {
                double angle = panda.getRandom().nextDouble() * Math.PI * 2;
                panda.getLookControl().setLookAt(
                        panda.getX() + Math.cos(angle) * 6,
                        panda.getEyeY() + panda.getRandom().nextDouble() * 2 - 1,
                        panda.getZ() + Math.sin(angle) * 6);
            }

            if (phaseTicks > PERCH_SETTLE_TICKS && panda.getRandom().nextInt(PERCH_LEAVE_CHANCE) == 0) {
                leavePerch();
                return;
            }
        }

        if (--perchTicks > 0) return;
        leavePerch();
    }

    private boolean holdingHigh() {
        if (!escapeClimb || escapeThreat == null) return false;
        if (!escapeThreat.isAlive() || escapeThreat.level() != panda.level()) return false;
        return panda.distanceToSqr(escapeThreat) <= ESCAPE_SAFE_DISTANCE_SQR;
    }

    private boolean canHarvest() {
        return bambooColumn
                && !escapeClimb
                && columnTop - column.getY() + 1 > MIN_BAMBOO_LEFT
                && panda.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    private void beginHarvestSwipe() {
        panda.setSwipeTimer(RedPandaEntity.SWIPE_TICKS);
        harvestStrike = SWIPE_STRIKE_TICKS;

        panda.level().playSound(null, panda.getX(), panda.getY(), panda.getZ(),
                SoundEvents.BAMBOO_HIT, SoundSource.NEUTRAL, 0.5f,
                (float) OWUtils.generateRandomInterval(1.1, 1.3));
    }

    private void harvestBambooTip() {
        if (harvested || !canHarvest()) return;

        BlockPos tip = new BlockPos(column.getX(), columnTop, column.getZ());
        if (!panda.level().getBlockState(tip).is(Blocks.BAMBOO)) return;

        harvested = true;
        panda.level().removeBlock(tip, false);
        columnTop--;

        ItemEntity cutting = new ItemEntity(panda.level(),
                columnCx + face.getStepX() * 0.35, tip.getY() + 0.2, columnCz + face.getStepZ() * 0.35,
                new ItemStack(Blocks.BAMBOO));
        cutting.setDeltaMovement(face.getStepX() * 0.06, 0.05, face.getStepZ() * 0.06);
        cutting.setDefaultPickUpDelay();
        panda.level().addFreshEntity(cutting);

        panda.level().playSound(null, tip.getX(), tip.getY(), tip.getZ(),
                SoundEvents.BAMBOO_BREAK, SoundSource.NEUTRAL, 0.9f,
                (float) OWUtils.generateRandomInterval(0.9, 1.1));
    }

    private void pickUpHarvest() {
        if (!harvested || panda.hasMouthItem()) return;

        List<ItemEntity> nearby = panda.level().getEntitiesOfClass(ItemEntity.class,
                panda.getBoundingBox().inflate(HARVEST_PICKUP_RANGE),
                item -> item.isAlive() && item.getItem().is(Items.BAMBOO));
        if (nearby.isEmpty()) return;

        ItemEntity cutting = nearby.get(0);
        ItemStack stack = cutting.getItem();

        panda.setMouthItem(stack.copyWithCount(1));
        stack.shrink(1);
        if (stack.isEmpty()) cutting.discard();
        else cutting.setItem(stack);

        panda.level().playSound(null, panda.getX(), panda.getY(), panda.getZ(),
                SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.7f,
                (float) OWUtils.generateRandomInterval(1.2, 1.5));
    }

    private void leavePerch() {
        panda.setNap(false);
        perchNapping = false;
        beginTrunkReturn();
    }

    private void beginTopPause() {
        phase = Phase.TOP_PAUSE;
        panda.setClimbPace(0f);
        harvestDelay = canHarvest() ? HARVEST_DELAY_TICKS : 0;
        harvestStrike = 0;
        phaseTicks = TOP_PAUSE_MIN + panda.getRandom().nextInt(TOP_PAUSE_MAX - TOP_PAUSE_MIN);

        panda.level().playSound(null, panda.getX(), panda.getY(), panda.getZ(),
                SoundEvents.FOX_SNIFF, SoundSource.NEUTRAL, 0.5f,
                (float) OWUtils.generateRandomInterval(1.15, 1.35));
    }

    private void tickTopPause() {
        applyClimbPosition(climbY);

        if (phaseTicks % TOP_LOOK_INTERVAL == 0) {
            double angle = panda.getRandom().nextDouble() * Math.PI * 2;
            panda.getLookControl().setLookAt(
                    panda.getX() + Math.cos(angle) * 6,
                    panda.getEyeY() + panda.getRandom().nextDouble() * 2 - 1,
                    panda.getZ() + Math.sin(angle) * 6);
        }

        if (holdingHigh()) return;
        if (harvestDelay > 0 && --harvestDelay == 0) beginHarvestSwipe();
        if (harvestStrike > 0 && --harvestStrike == 0) harvestBambooTip();
        if (--phaseTicks > 0) return;
        beginFlip();
    }

    private void beginFlip() {
        phase = Phase.FLIP;
        phaseTicks = FLIP_TICKS;

        panda.setTreeLeanTarget(1f);
        panda.setTreeFlipTarget(1f);
        panda.setClimbPace(0f);

        panda.level().playSound(null, panda.getX(), panda.getY(), panda.getZ(),
                SoundEvents.FOX_SNIFF, SoundSource.NEUTRAL, 0.6f,
                (float) OWUtils.generateRandomInterval(1.2, 1.45));
    }

    private void tickFlip() {
        applyClimbPosition(climbY);
        if (--phaseTicks > 0) return;

        phase = Phase.DESCEND;
        panda.setClimbPace(PACE_DESCEND);
        panda.grantFallGrace(FALL_GRACE_TICKS);
    }

    private void tickDescend() {
        climbY -= DESCEND_SPEED;
        panda.grantFallGrace(FALL_GRACE_TICKS);
        applyClimbPosition(climbY);

        if (panda.tickCount % 5 == 0) playGripSound(0.45f);

        int level = Mth.floor(climbY);
        boolean stillOnTrunk = level > column.getY()
                && isTrunkBlock(panda.level().getBlockState(new BlockPos(column.getX(), level, column.getZ())));

        if (stillOnTrunk && climbY > column.getY() + 0.05) {
            int ahead = Mth.floor(climbY - SIDESTEP_LOOKAHEAD);
            if (ahead > column.getY() && !faceClear(face, ahead)) beginSidestep(ahead, Phase.DESCEND);
            return;
        }

        release();
        pickUpHarvest();
        finished = true;
    }

    private void release() {
        panda.setClimbPace(0f);
        panda.setTreeClimbing(false);
        panda.setTreeLeanTarget(0f);
        panda.setTreeFlipTarget(0f);
        panda.setNap(false);
        perchNapping = false;

        if (!panda.noPhysics && !panda.isNoGravity()) return;

        panda.noPhysics = false;
        panda.setNoGravity(false);
        panda.setDeltaMovement(Vec3.ZERO);
        panda.fallDistance = 0f;
        panda.grantFallGrace(FALL_GRACE_TICKS);

        if (column == null || face == null) return;
        panda.setPos(
                columnCx + face.getStepX() * (hugDistance + RELEASE_CLEARANCE),
                panda.getY(),
                columnCz + face.getStepZ() * (hugDistance + RELEASE_CLEARANCE));
    }

    private void applyClimbPosition(double y) {
        if (--lateralTimer <= 0) {
            lateralTarget = (float) ((panda.getRandom().nextDouble() * 2 - 1) * lateralRange);
            lateralTimer = LATERAL_MIN_INTERVAL
                    + panda.getRandom().nextInt(LATERAL_MAX_INTERVAL - LATERAL_MIN_INTERVAL);
        }
        lateral += (lateralTarget - lateral) * LATERAL_RESPONSE;

        pin(attachPoint(y));
        turnTo(face.getOpposite().toYRot() + lateral * LATERAL_YAW_GAIN);
    }

    private Vec3 attachPoint(double y) {
        double tangentX = -face.getStepZ();
        double tangentZ = face.getStepX();
        return new Vec3(
                columnCx + face.getStepX() * hugDistance + tangentX * lateral,
                y,
                columnCz + face.getStepZ() * hugDistance + tangentZ * lateral);
    }

    private void pin(Vec3 position) {
        panda.setPos(position.x, position.y, position.z);
        panda.setDeltaMovement(Vec3.ZERO);
        panda.fallDistance = 0f;
        panda.getNavigation().stop();
    }

    private void turnTowards(double x, double z) {
        double dx = x - panda.getX();
        double dz = z - panda.getZ();
        if (dx * dx + dz * dz < 1.0e-6) return;
        turnTo((float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f);
    }

    private void turnTo(float targetYaw) {
        float yaw = Mth.approachDegrees(panda.getYRot(), targetYaw, YAW_TURN_RATE);
        panda.setYRot(yaw);
        panda.yBodyRot = yaw;
        panda.setYHeadRot(yaw);
    }

    private static float arc(float progress) {
        return Mth.sin(progress * Mth.PI) * 0.18f;
    }

    private void playGripSound(float volume) {
        panda.level().playSound(null, panda.getX(), panda.getY(), panda.getZ(),
                bambooColumn ? SoundEvents.BAMBOO_STEP : SoundEvents.WOOD_STEP,
                SoundSource.NEUTRAL, volume, (float) OWUtils.generateRandomInterval(1.3, 1.7));
    }

    private boolean isAvailable() {
        return !panda.isTame()
                && !panda.isBaby()
                && panda.isAlive()
                && !panda.isSitting()
                && !panda.isSleeping()
                && !panda.isPassenger()
                && !panda.isVehicle()
                && !panda.isInWater()
                && (escapeClimb || !panda.isInFight())
                && (escapeClimb || !panda.isAlerted())
                && !panda.isIntimidating()
                && !panda.isClimbing()
                && !panda.isOnShoulder()
                && (escapeClimb || panda.getTarget() == null);
    }

    private @Nullable BlockPos findColumnBase() {
        Level level = panda.level();
        BlockPos origin = panda.blockPosition();

        BlockPos nearest = null;
        BlockPos fallback = null;
        double bestDistance = Double.MAX_VALUE;

        for (int i = 0; i < SEARCH_SAMPLES; i++) {
            int dx = panda.getRandom().nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int dz = panda.getRandom().nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            if (dx == 0 && dz == 0) continue;

            for (int dy = 1; dy >= -2; dy--) {
                BlockPos probe = origin.offset(dx, dy, dz);
                if (!isTrunkBlock(level.getBlockState(probe))) continue;

                BlockPos base = probe;
                for (int step = 0; step < 6; step++) {
                    BlockPos below = base.below();
                    if (!isTrunkBlock(level.getBlockState(below))) break;
                    base = below;
                }

                int height = findColumnTop(base) - base.getY() + 1;
                if (height < MIN_TRUNK_HEIGHT) break;
                if (level.getBlockState(base).is(Blocks.BAMBOO) && stalkTaken(base)) break;

                if (escapeClimb) {
                    double distance = panda.distanceToSqr(base.getX() + 0.5, panda.getY(), base.getZ() + 0.5);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        nearest = base.immutable();
                    }
                    break;
                }

                if (level.getBlockState(base).is(Blocks.BAMBOO) && height > MIN_BAMBOO_LEFT) {
                    return base.immutable();
                }
                if (fallback == null) fallback = base.immutable();
                break;
            }
        }
        return escapeClimb ? nearest : fallback;
    }

    private boolean stalkTaken(BlockPos base) {
        AABB around = new AABB(base).inflate(STALK_CLAIM_REACH, STALK_CLAIM_HEIGHT, STALK_CLAIM_REACH);

        for (RedPandaEntity other : panda.level().getEntitiesOfClass(RedPandaEntity.class, around)) {
            if (other != panda && base.equals(other.getClimbColumn())) return true;
        }
        return false;
    }

    private int findColumnTop(BlockPos base) {
        Level level = panda.level();
        int top = base.getY();
        for (int step = 1; step <= MAX_CLIMB_HEIGHT + 4; step++) {
            if (!isTrunkBlock(level.getBlockState(base.above(step)))) break;
            top = base.getY() + step;
        }
        return top;
    }

    private @Nullable Direction pickFace(BlockPos base) {
        Direction best = null;
        double bestScore = Double.MAX_VALUE;

        for (Direction candidate : Direction.Plane.HORIZONTAL) {
            BlockPos side = base.relative(candidate);
            if (!isPassable(side) || !isPassable(side.above())) continue;

            double x = base.getX() + 0.5 + candidate.getStepX();
            double z = base.getZ() + 0.5 + candidate.getStepZ();
            double score = panda.distanceToSqr(x, panda.getY(), z);
            if (score < bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    private @Nullable BlockPos findBranch(int fromLevel) {
        for (int y = fromLevel; y <= fromLevel + 1; y++) {
            BlockPos onFace = branchAt(y, face);
            if (onFace != null) return onFace;

            for (Direction candidate : Direction.Plane.HORIZONTAL) {
                if (candidate == face) continue;
                BlockPos pos = branchAt(y, candidate);
                if (pos != null) return pos;
            }
        }
        return null;
    }

    private @Nullable BlockPos branchAt(int y, Direction direction) {
        BlockPos pos = new BlockPos(
                column.getX() + direction.getStepX(), y, column.getZ() + direction.getStepZ());

        if (!panda.level().getBlockState(pos).is(BlockTags.LOGS)) return null;
        if (!isPassable(pos.above()) || !isPassable(pos.above(2))) return null;
        return pos.immutable();
    }

    private boolean isPassable(BlockPos pos) {
        BlockState state = panda.level().getBlockState(pos);
        return state.isAir() || state.is(BlockTags.LEAVES) || state.is(Blocks.BAMBOO) || !state.blocksMotion();
    }

    private @Nullable Direction directionOf(BlockPos pos) {
        int dx = pos.getX() - column.getX();
        int dz = pos.getZ() - column.getZ();

        for (Direction candidate : Direction.Plane.HORIZONTAL) {
            if (candidate.getStepX() == dx && candidate.getStepZ() == dz) return candidate;
        }
        return null;
    }

    private static double angleOf(Direction direction) {
        return Mth.atan2(direction.getStepZ(), direction.getStepX());
    }

    private static boolean isTrunkBlock(BlockState state) {
        if (state.is(Blocks.BAMBOO)) return true;
        if (!state.is(BlockTags.LOGS)) return false;
        return !state.hasProperty(RotatedPillarBlock.AXIS)
                || state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y;
    }
}
