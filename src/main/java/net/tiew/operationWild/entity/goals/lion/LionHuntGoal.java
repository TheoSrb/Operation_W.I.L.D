package net.tiew.operationWild.entity.goals.lion;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.clan.LionClanData;
import net.tiew.operationWild.entity.clan.LionHunt;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class LionHuntGoal extends Goal {

    private static final int CHECK_INTERVAL = 5;
    private static final double STALK_SPEED = 0.55;
    private static final double WALK_SPEED = 1.15;
    private static final double DRIVE_SPEED = 2.3;
    private static final double STALK_START_DISTANCE = 22.0;
    private static final double ARC_REACHED = 3.0;
    private static final double CENTER_HOLD_DISTANCE = 1.5;
    private static final double CLOSE_STRIKE_DISTANCE = 5.0;
    private static final double DRIVE_CONTACT = 3.0;
    private static final int FREEZE_TICKS = 12;
    private static final int REPATH_TICKS = 10;
    private static final int PANIC_INTERVAL = 10;

    private final LionEntity lion;
    private int freezeTicks;
    private int repath;
    private int panicTimer;

    public LionHuntGoal(LionEntity lion) {
        this.lion = lion;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Nullable
    private LionHunt hunt() {
        LionClanData.Clan clan = lion.wildClan();
        if (clan == null || clan.state.hunt == null || clan.state.hunt.isFinished()) return null;
        return clan.state.hunt.roleOf(lion) == null ? null : clan.state.hunt;
    }

    private boolean active() {
        if (!LionGoals.isFreeWild(lion) || lion.getTarget() != null || lion.isInWater()) return false;
        LionHunt hunt = hunt();
        return hunt != null && lion.level() instanceof ServerLevel level && hunt.prey(level) != null;
    }

    @Override
    public boolean canUse() {
        return LionGoals.onSchedule(lion, CHECK_INTERVAL) && active();
    }

    @Override
    public boolean canContinueToUse() {
        return active();
    }

    @Override
    public void start() {
        freezeTicks = 0;
        repath = 0;
        panicTimer = 0;
        if (lion.isNapping()) lion.setNap(false);
        if (lion.isSitting()) lion.setSitting(false);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LionHunt hunt = hunt();
        if (hunt == null || !(lion.level() instanceof ServerLevel level)) return;
        LivingEntity prey = hunt.prey(level);
        if (prey == null) return;
        long now = level.getGameTime();

        if (hunt.phase() == LionHunt.Phase.STRIKE) {
            lion.setWildPose(LionEntity.WILD_POSE_NONE);
            lion.huntTarget(prey);
            return;
        }

        LionHunt.Role role = hunt.roleOf(lion);
        if (role == LionHunt.Role.WING) tickWing(hunt, prey, now);
        else tickCenter(hunt, prey, now);
    }

    private void tickCenter(LionHunt hunt, LivingEntity prey, long now) {
        double distance = lion.distanceTo(prey);
        if (distance <= CLOSE_STRIKE_DISTANCE || (hunt.phase() == LionHunt.Phase.DRIVE && distance <= LionHunt.STRIKE_RANGE)) {
            hunt.strike(now);
            return;
        }

        if (!hunt.hasWings()) {
            if (distance <= LionHunt.SOLO_STRIKE_RANGE && !LionHunt.isWatchedBy(prey, lion)) {
                hunt.strike(now);
                return;
            }
            stalk(prey.position(), prey);
            return;
        }

        if (hunt.phase() == LionHunt.Phase.APPROACH && distance <= LionHunt.STRIKE_RANGE) {
            hunt.strike(now);
            return;
        }

        Vec3 point = hunt.centerPoint(prey, lion);
        if (LionGoals.horizontalDistance(lion, point) <= CENTER_HOLD_DISTANCE) {
            crouchAndWatch(prey);
            return;
        }
        stalk(point, prey);
    }

    private void tickWing(LionHunt hunt, LivingEntity prey, long now) {
        if (hunt.phase() == LionHunt.Phase.DRIVE) {
            lion.setWildPose(LionEntity.WILD_POSE_NONE);
            lion.setRunning(true);
            lion.getLookControl().setLookAt(prey, 30f, 30f);
            if (--repath <= 0) {
                repath = REPATH_TICKS / 2;
                lion.getNavigation().moveTo(prey, DRIVE_SPEED);
            }
            double distance = lion.distanceTo(prey);
            if (distance <= LionHunt.PANIC_RANGE && --panicTimer <= 0) {
                panicTimer = PANIC_INTERVAL;
                LionHunt.panicAway(prey, lion);
            }
            if (distance <= DRIVE_CONTACT) hunt.strike(now);
            return;
        }

        if (!hunt.wingsPastArc.contains(lion.getUUID())) {
            Vec3 arc = hunt.wingArcPoint(prey, lion);
            if (LionGoals.horizontalDistance(lion, arc) <= ARC_REACHED) {
                hunt.wingsPastArc.add(lion.getUUID());
            } else {
                lion.setRunning(false);
                if (lion.distanceTo(prey) <= STALK_START_DISTANCE) {
                    stalk(arc, prey);
                } else {
                    lion.setWildPose(LionEntity.WILD_POSE_NONE);
                    if (--repath <= 0) {
                        repath = REPATH_TICKS;
                        lion.getNavigation().moveTo(arc.x, arc.y, arc.z, WALK_SPEED);
                    }
                }
                return;
            }
        }

        Vec3 post = hunt.wingPoint(prey, lion);
        if (LionGoals.horizontalDistance(lion, post) <= LionHunt.WING_READY_DISTANCE) {
            hunt.markWingReady(lion);
            crouchAndWatch(prey);
            return;
        }
        stalk(post, prey);
    }

    private void stalk(Vec3 point, LivingEntity prey) {
        lion.setRunning(false);
        lion.setWildPose(LionEntity.WILD_POSE_STALK);
        lion.getLookControl().setLookAt(prey, 20f, 20f);
        if (LionHunt.isWatchedBy(prey, lion)) freezeTicks = FREEZE_TICKS;
        if (freezeTicks > 0) {
            freezeTicks--;
            LionGoals.holdStill(lion);
            repath = 0;
            return;
        }
        if (--repath <= 0) {
            repath = REPATH_TICKS;
            lion.getNavigation().moveTo(point.x, point.y, point.z, STALK_SPEED);
        }
    }

    private void crouchAndWatch(LivingEntity prey) {
        lion.setRunning(false);
        lion.setWildPose(LionEntity.WILD_POSE_STALK);
        LionGoals.holdStill(lion);
        lion.getLookControl().setLookAt(prey, 20f, 20f);
        LionGoals.faceTowards(lion, prey.position(), 8f);
    }

    @Override
    public void stop() {
        lion.setWildPose(LionEntity.WILD_POSE_NONE);
        if (lion.getTarget() == null) {
            lion.setRunning(false);
            lion.getNavigation().stop();
        }
    }
}
