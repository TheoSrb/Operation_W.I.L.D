package net.tiew.operationWild.entity.goals.lion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.clan.LionClanData;
import net.tiew.operationWild.entity.clan.LionPride;
import net.tiew.operationWild.entity.clan.LionPrideState;
import net.tiew.operationWild.entity.navigation.OWPathProbe;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class LionPerchGoal extends Goal {

    private static final int CHECK_INTERVAL = 40;
    private static final int SEARCH_RADIUS = 14;
    private static final int MIN_RISE = 2;
    private static final long PERCH_LIFETIME = 12000L;
    private static final long SEARCH_COOLDOWN = 2400L;
    private static final int PROBE_CANDIDATES = 4;
    private static final float PROBE_RANGE = 40f;
    private static final int REPATH_TICKS = 20;
    private static final int GIVE_UP_TICKS = 700;
    private static final double ARRIVE_HORIZONTAL = 1.1;
    private static final double ARRIVE_VERTICAL = 1.3;
    private static final int LOOK_AROUND_TICKS = 70;
    private static final int GROWL_MIN = 60;
    private static final int GROWL_SPREAD = 60;

    private final LionEntity lion;
    @Nullable
    private BlockPos perch;
    private boolean arrived;
    private int repath;
    private int ticks;
    private int napTicks;
    private int lookTimer;
    private int growlTimer;

    public LionPerchGoal(LionEntity lion) {
        this.lion = lion;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!LionGoals.onSchedule(lion, CHECK_INTERVAL) || !wantsPerch()) return false;
        LionClanData.Clan clan = lion.wildClan();
        if (clan == null || !(lion.level() instanceof ServerLevel level)) return false;
        perch = resolvePerch(level, clan);
        return perch != null;
    }

    private boolean wantsPerch() {
        if (!lion.isMale() || lion.isBaby() || !LionGoals.isFreeWild(lion) || lion.getTarget() != null || lion.isInWater()) return false;
        if (!(lion.level() instanceof ServerLevel level)) return false;
        LionClanData.Clan clan = lion.wildClan();
        if (clan == null || clan.isNomad() || clan.home == null || !lion.getUUID().equals(clan.male)) return false;
        if (LionPride.isDisturbed(clan, level.getGameTime()) || clan.state.isFeasting(level.getGameTime())) return false;
        LionPride.DayPhase phase = LionPride.phase(level);
        return phase == LionPride.DayPhase.MORNING || phase == LionPride.DayPhase.HEAT || phase == LionPride.DayPhase.EVENING;
    }

    @Nullable
    private BlockPos resolvePerch(ServerLevel level, LionClanData.Clan clan) {
        LionPrideState state = clan.state;
        long now = level.getGameTime();
        if (state.perch != null && now < state.perchExpiry) return state.perch;
        if (now - state.perchSearchedAt < SEARCH_COOLDOWN) return null;
        state.perchSearchedAt = now;
        state.perch = findPerch(level, clan.home);
        state.perchExpiry = now + PERCH_LIFETIME;
        return state.perch;
    }

    @Nullable
    private BlockPos findPerch(ServerLevel level, BlockPos home) {
        if (!level.hasChunkAt(home)) return null;
        int baseY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, home).getY();
        List<BlockPos> candidates = new ArrayList<>();
        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                if (dx * dx + dz * dz > SEARCH_RADIUS * SEARCH_RADIUS) continue;
                BlockPos column = home.offset(dx, 0, dz);
                if (!level.hasChunkAt(column)) continue;
                BlockPos stand = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
                if (stand.getY() - baseY < MIN_RISE) continue;
                if (isStandable(level, stand) && hasRoom(level, stand)) candidates.add(stand);
            }
        }
        candidates.sort(Comparator.<BlockPos>comparingInt(BlockPos::getY).reversed()
                .thenComparingDouble(pos -> pos.distSqr(home)));
        for (int i = 0; i < Math.min(PROBE_CANDIDATES, candidates.size()); i++) {
            BlockPos candidate = candidates.get(i);
            Path path = OWPathProbe.find(lion, candidate, PROBE_RANGE, 1);
            if (path != null && path.canReach()) return candidate;
        }
        return null;
    }

    private static boolean isStandable(ServerLevel level, BlockPos stand) {
        BlockPos below = stand.below();
        BlockState ground = level.getBlockState(below);
        if (!level.getFluidState(below).isEmpty() || !level.getFluidState(stand).isEmpty()) return false;
        if (ground.is(BlockTags.LEAVES) || !ground.isFaceSturdy(level, below, Direction.UP)) return false;
        return level.getBlockState(stand).getCollisionShape(level, stand).isEmpty()
                && level.getBlockState(stand.above()).getCollisionShape(level, stand.above()).isEmpty();
    }

    private static boolean hasRoom(ServerLevel level, BlockPos stand) {
        int flat = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbour = stand.relative(direction);
            if (!level.hasChunkAt(neighbour)) continue;
            int y = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, neighbour).getY();
            if (y >= stand.getY() - 1 && y <= stand.getY()) flat++;
        }
        return flat >= 2;
    }

    @Override
    public boolean canContinueToUse() {
        if (perch == null || !wantsPerch() || lion.getTarget() != null) return false;
        if (!arrived && ticks > GIVE_UP_TICKS) {
            LionClanData.Clan clan = lion.wildClan();
            if (clan != null) clan.state.perch = null;
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        arrived = false;
        repath = 0;
        ticks = 0;
        napTicks = 0;
        lookTimer = 0;
        growlTimer = GROWL_MIN;
        lion.setRunning(false);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (perch == null) return;
        ticks++;
        Vec3 top = Vec3.atBottomCenterOf(perch);
        if (!arrived) {
            boolean close = LionGoals.horizontalDistance(lion, top) <= ARRIVE_HORIZONTAL && Math.abs(lion.getY() - top.y) <= ARRIVE_VERTICAL;
            if (close) {
                arrived = true;
                LionGoals.holdStill(lion);
                return;
            }
            if (--repath <= 0) {
                repath = REPATH_TICKS;
                lion.getNavigation().moveTo(top.x, top.y, top.z, 0.9);
            }
            return;
        }

        LionGoals.holdStill(lion);
        boolean heat = LionPride.phase(lion.level()) == LionPride.DayPhase.HEAT;
        if (heat) {
            lion.wild.perchedWatch = false;
            if (lion.isSitting()) lion.setSitting(false);
            if (!lion.isNapping()) lion.setNap(true);
            LionGoals.napEffects(lion, napTicks++);
            return;
        }

        if (lion.isNapping()) lion.setNap(false);
        if (!lion.isSitting()) lion.setSitting(true);
        lion.wild.perchedWatch = true;

        LivingEntity watched = lion.wild.warnTarget();
        if (watched != null) {
            lion.getLookControl().setLookAt(watched, 20f, 20f);
            if (--growlTimer <= 0) {
                growlTimer = GROWL_MIN + lion.getRandom().nextInt(GROWL_SPREAD);
                lion.level().playSound(null, lion.getX(), lion.getY(), lion.getZ(), OWSounds.TIGER_IDLE.get(),
                        SoundSource.NEUTRAL, 1.4f, LionPride.growlPitch(lion));
            }
            return;
        }
        if (--lookTimer <= 0) {
            lookTimer = LOOK_AROUND_TICKS + lion.getRandom().nextInt(LOOK_AROUND_TICKS);
            float yaw = lion.getYRot() + (lion.getRandom().nextFloat() - 0.5f) * 160f;
            Vec3 gaze = lion.getEyePosition().add(Vec3.directionFromRotation(0f, yaw).scale(12.0));
            lion.getLookControl().setLookAt(gaze.x, gaze.y, gaze.z, 10f, 10f);
        }
    }

    @Override
    public void stop() {
        lion.wild.perchedWatch = false;
        if (lion.isNapping()) lion.setNap(false);
        if (lion.isSitting()) lion.setSitting(false);
        lion.getNavigation().stop();
        arrived = false;
        perch = null;
    }
}
