package net.tiew.operationWild.entity.goals.elephant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.core.OWPlacedBlocks;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.team.OWArenaManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ElephantBrowseGoal extends Goal {

    private static final int CHANCE = 500;
    private static final int LEAF_RADIUS = 8;
    private static final int LEAF_MIN_RISE = 2;
    private static final int LEAF_MAX_RISE = 6;
    private static final double ARRIVE_SLACK = -0.25;
    private static final double REACH_SLACK = 0.15;
    private static final double GRAB_SLACK = 0.5;
    private static final int GRASS_RADIUS = 6;
    private static final int NEXT_BITE_RADIUS = 3;
    private static final int LEAF_GRAB_TICKS = 40;
    private static final int GRASS_GRAB_TICKS = 24;
    private static final int FEED_TICKS = 26;
    private static final int APPROACH_TIMEOUT = 300;
    private static final int STUCK_TICKS = 40;
    private static final double STAND_TOLERANCE = 0.6;
    private static final int GIVE_UP_TICKS = 700;
    private static final int MAX_BITES = 3;
    private static final float HEAL_PER_BITE = 1.5f;
    private static final int TUG_PARTICLE_INTERVAL = 6;
    private static final int CHEW_INTERVAL = 8;
    private static final double WALK_SPEED = 0.9;

    private enum Phase { APPROACH, GRAB, FEED, DONE }

    private final ElephantEntity elephant;
    @Nullable
    private BlockPos food;
    private boolean leaves;
    private Phase phase = Phase.DONE;
    private int timer;
    private int stuck;
    private int totalTicks;
    private int bites;
    private int maxBites;

    public ElephantBrowseGoal(ElephantEntity elephant) {
        this.elephant = elephant;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!ElephantGoals.isCalm(elephant) || elephant.isMusth()) return false;
        if (elephant.getRandom().nextInt(reducedTickDelay(CHANCE)) != 0) return false;
        if (!canGraze()) return false;

        food = findLeaves(elephant.blockPosition(), LEAF_RADIUS);
        leaves = food != null;
        if (food == null) food = findGrass(elephant.blockPosition(), GRASS_RADIUS);
        return food != null;
    }

    @Override
    public boolean canContinueToUse() {
        return phase != Phase.DONE && totalTicks < GIVE_UP_TICKS && ElephantGoals.isCalm(elephant);
    }

    @Override
    public void start() {
        phase = Phase.APPROACH;
        timer = 0;
        stuck = 0;
        totalTicks = 0;
        bites = 0;
        maxBites = 1 + elephant.getRandom().nextInt(MAX_BITES);
    }

    @Override
    public void stop() {
        elephant.stopAiTrunk();
        elephant.setHeldFood(null);
        elephant.getNavigation().stop();
        phase = Phase.DONE;
        food = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        totalTicks++;
        timer++;
        if (food == null) {
            phase = Phase.DONE;
            return;
        }
        Vec3 target = Vec3.atCenterOf(food);
        switch (phase) {
            case APPROACH -> tickApproach(target);
            case GRAB -> tickGrab(target);
            case FEED -> tickFeed();
            default -> {
            }
        }
    }

    private void tickApproach(Vec3 target) {
        if (!isFood(food)) {
            phase = Phase.DONE;
            return;
        }
        if (elephant.canTrunkReach(food, ARRIVE_SLACK)) {
            beginGrab();
            return;
        }
        if (timer > APPROACH_TIMEOUT) {
            phase = Phase.DONE;
            return;
        }
        Vec3 stand = standingSpot(target);
        if (ElephantGoals.horizontalDistance(elephant, stand) < STAND_TOLERANCE) {
            if (elephant.canTrunkReach(food, REACH_SLACK)) beginGrab();
            else phase = Phase.DONE;
            return;
        }
        if (timer % 20 == 1) elephant.getNavigation().moveTo(stand.x, stand.y, stand.z, WALK_SPEED);
        if (elephant.getNavigation().isDone()) {
            elephant.getMoveControl().setWantedPosition(stand.x, stand.y, stand.z, WALK_SPEED);
            stuck = elephant.getDeltaMovement().horizontalDistanceSqr() < 4.0E-4 ? stuck + 1 : 0;
            if (stuck > STUCK_TICKS) {
                if (elephant.canTrunkReach(food, REACH_SLACK)) beginGrab();
                else phase = Phase.DONE;
            }
        } else {
            stuck = 0;
        }
    }

    private void beginGrab() {
        elephant.getNavigation().stop();
        phase = Phase.GRAB;
        timer = 0;
        stuck = 0;
    }

    private Vec3 standingSpot(Vec3 target) {
        Vec3 flat = new Vec3(target.x - elephant.getX(), 0, target.z - elephant.getZ());
        Vec3 dir = flat.lengthSqr() < 1.0E-4 ? Vec3.directionFromRotation(0f, elephant.yBodyRot) : flat.normalize();
        double standOff = elephant.trunkStandOff(food);
        return new Vec3(target.x - dir.x * standOff, elephant.getY(), target.z - dir.z * standOff);
    }

    private void tickGrab(Vec3 target) {
        elephant.getNavigation().stop();
        elephant.turnBodyTowards(target, 5f);
        elephant.getLookControl().setLookAt(target);
        if (!isFood(food)) {
            phase = Phase.DONE;
            return;
        }
        if (!elephant.canTrunkReach(food, GRAB_SLACK)) {
            elephant.stopAiTrunk();
            phase = Phase.APPROACH;
            timer = 0;
            return;
        }
        if (ElephantGoals.yawGap(elephant, target) > 40f) return;
        if (!elephant.startAiTrunk(ElephantEntity.TRUNK_BROWSING)) {
            phase = Phase.DONE;
            return;
        }
        elephant.aimAiTrunkAt(elephant.trunkGrabPoint(food));

        BlockState grabbed = elephant.level().getBlockState(food);
        if (timer % TUG_PARTICLE_INTERVAL == 0 && elephant.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, grabbed),
                    target.x, target.y, target.z, 4, 0.3, 0.3, 0.3, 0.02);
            elephant.level().playSound(null, food, grabbed.getSoundType().getHitSound(), SoundSource.NEUTRAL, 0.7f, 0.8f);
        }
        if (timer < (leaves ? LEAF_GRAB_TICKS : GRASS_GRAB_TICKS)) return;

        if (canGraze() && !OWPlacedBlocks.isProtectedFrom((ServerLevel) elephant.level(), food, null)) {
            elephant.level().destroyBlock(food, false, elephant);
            elephant.setHeldFood(grabbed);
        }
        phase = Phase.FEED;
        timer = 0;
    }

    private void tickFeed() {
        elephant.getNavigation().stop();
        if (!elephant.startAiTrunk(ElephantEntity.TRUNK_DRINKING)) {
            phase = Phase.DONE;
            return;
        }
        elephant.aimAiTrunk(elephant.yBodyRot, 22f);
        if (timer % CHEW_INTERVAL == CHEW_INTERVAL / 2) {
            elephant.level().playSound(null, elephant.getX(), elephant.getY() + elephant.getBbHeight() * 0.6, elephant.getZ(),
                    SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.9f, 0.6f + elephant.getRandom().nextFloat() * 0.15f);
        }
        if (timer < FEED_TICKS) return;

        swallow();
        elephant.heal(HEAL_PER_BITE);
        bites++;
        timer = 0;
        BlockPos next = bites < maxBites ? nextBite() : null;
        if (next == null) {
            phase = Phase.DONE;
            return;
        }
        food = next;
        phase = Phase.APPROACH;
    }

    private void swallow() {
        BlockState eaten = elephant.getHeldFood();
        elephant.setHeldFood(null);
        if (eaten == null || !(elephant.level() instanceof ServerLevel serverLevel)) return;
        Vec3 forward = Vec3.directionFromRotation(0f, elephant.yBodyRot);
        Vec3 mouth = elephant.position().add(forward.scale(elephant.getBbWidth() * 0.62)).add(0, elephant.getBbHeight() * 0.5, 0);
        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, eaten),
                mouth.x, mouth.y, mouth.z, 14, 0.25, 0.15, 0.25, 0.06);
    }

    @Nullable
    private BlockPos nextBite() {
        BlockPos center = food != null ? food : elephant.blockPosition();
        return leaves ? findLeaves(center, NEXT_BITE_RADIUS) : findGrass(center, NEXT_BITE_RADIUS);
    }

    private boolean canGraze() {
        if (!(elephant.level() instanceof ServerLevel serverLevel)) return false;
        if (OWArenaManager.isArena(serverLevel)) return false;
        return serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    private boolean isFood(@Nullable BlockPos pos) {
        if (pos == null) return false;
        BlockState state = elephant.level().getBlockState(pos);
        return leaves ? isBrowsableLeaves(state) : isGrass(state);
    }

    private static boolean isBrowsableLeaves(BlockState state) {
        return state.getBlock() instanceof LeavesBlock && state.hasProperty(LeavesBlock.PERSISTENT)
                && !state.getValue(LeavesBlock.PERSISTENT);
    }

    private static boolean isGrass(BlockState state) {
        return state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN);
    }

    @Nullable
    private BlockPos findLeaves(BlockPos center, int radius) {
        int feet = elephant.getBlockY();
        int maxRise = Math.min(LEAF_MAX_RISE, Mth.floor(elephant.maxTrunkRise() + REACH_SLACK));
        if (maxRise < LEAF_MIN_RISE) return null;
        List<BlockPos> acacia = new ArrayList<>();
        List<BlockPos> other = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(
                new BlockPos(center.getX() - radius, feet + LEAF_MIN_RISE, center.getZ() - radius),
                new BlockPos(center.getX() + radius, feet + maxRise, center.getZ() + radius))) {
            BlockState state = elephant.level().getBlockState(pos);
            if (!isBrowsableLeaves(state) || !isExposed(pos)) continue;
            if (state.is(Blocks.ACACIA_LEAVES)) acacia.add(pos.immutable());
            else other.add(pos.immutable());
        }
        List<BlockPos> pool = acacia.isEmpty() ? other : acacia;
        return nearest(pool);
    }

    @Nullable
    private BlockPos findGrass(BlockPos center, int radius) {
        int feet = elephant.getBlockY();
        List<BlockPos> pool = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(
                new BlockPos(center.getX() - radius, feet - 1, center.getZ() - radius),
                new BlockPos(center.getX() + radius, feet + 1, center.getZ() + radius))) {
            if (isGrass(elephant.level().getBlockState(pos))) pool.add(pos.immutable());
        }
        return nearest(pool);
    }

    @Nullable
    private BlockPos nearest(List<BlockPos> pool) {
        if (pool.isEmpty()) return null;
        BlockPos origin = elephant.blockPosition();
        BlockPos best = null;
        double bestScore = Double.MAX_VALUE;
        for (BlockPos pos : pool) {
            double score = pos.distSqr(origin) + elephant.getRandom().nextDouble() * 6.0;
            if (score < bestScore) {
                bestScore = score;
                best = pos;
            }
        }
        return best;
    }

    private boolean isExposed(BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (direction == Direction.UP) continue;
            if (elephant.level().getBlockState(pos.relative(direction)).isAir()) return true;
        }
        return false;
    }
}
