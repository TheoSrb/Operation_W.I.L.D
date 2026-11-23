package net.tiew.operationWild.entity.goals.lion;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.clan.LionClanData;
import net.tiew.operationWild.entity.clan.LionPride;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class LionClanRestGoal extends Goal {

    private static final int CHECK_INTERVAL = 20;
    private static final int REPATH_TICKS = 20;
    private static final double ARRIVE_DISTANCE = 1.2;
    private static final double STUCK_ARRIVE_DISTANCE = 3.0;
    private static final double SLOT_BASE_RADIUS = 1.3;
    private static final double SLOT_RADIUS_STEP = 0.45;
    private static final double GOLDEN_ANGLE = 2.39996;
    private static final int GIVE_UP_TICKS = 900;

    private final LionEntity lion;
    private Vec3 slot = Vec3.ZERO;
    private boolean arrived;
    private int repath;
    private int ticks;
    private int napTicks;

    public LionClanRestGoal(LionEntity lion) {
        this.lion = lion;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!LionGoals.onSchedule(lion, CHECK_INTERVAL)) return false;
        if (!wantsRest()) return false;
        Vec3 found = computeSlot();
        if (found == null) return false;
        slot = found;
        return true;
    }

    private boolean wantsRest() {
        if (!LionGoals.isCalm(lion) || lion.isInWater() || lion.isSitting()) return false;
        if (!(lion.level() instanceof ServerLevel level)) return false;
        LionClanData.Clan clan = lion.wildClan();
        if (clan == null) return false;
        if (LionPride.isDisturbed(clan, level.getGameTime())) return false;
        if (LionGoals.huntRole(lion, clan) != null) return false;
        LionPride.Hunger hunger = LionPride.hunger(clan);
        if (hunger == LionPride.Hunger.STARVING) return false;
        LionPride.DayPhase phase = LionPride.phase(level);
        boolean restTime = phase == LionPride.DayPhase.HEAT || (phase == LionPride.DayPhase.MORNING && hunger == LionPride.Hunger.SATED);
        if (!restTime) return false;
        return !(lion.getUUID().equals(clan.male) && !clan.isNomad() && clan.state.perch != null);
    }

    @Nullable
    private Vec3 computeSlot() {
        LionClanData.Clan clan = lion.wildClan();
        if (clan == null || !(lion.level() instanceof ServerLevel level)) return null;
        Vec3 center;
        if (clan.home != null) {
            center = LionPride.homeCenter(clan);
        } else {
            LionEntity leader = LionPride.loadedLeader(level, clan);
            if (leader == null) return null;
            center = leader.position();
        }
        List<UUID> order = new java.util.ArrayList<>(clan.males());
        order.addAll(clan.lionesses);
        int index = Math.max(0, order.indexOf(lion.getUUID()));
        if (index == 0) return center;
        double radius = SLOT_BASE_RADIUS + SLOT_RADIUS_STEP * Math.sqrt(index);
        double angle = index * GOLDEN_ANGLE;
        return center.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
    }

    @Override
    public boolean canContinueToUse() {
        if (!arrived && ticks > GIVE_UP_TICKS) return false;
        return wantsRest();
    }

    @Override
    public void start() {
        arrived = false;
        repath = 0;
        ticks = 0;
        napTicks = 0;
        lion.setRunning(false);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        ticks++;
        if (!arrived) {
            double distance = LionGoals.horizontalDistance(lion, slot);
            boolean stuck = lion.getNavigation().isDone() && repath > 0 && distance <= STUCK_ARRIVE_DISTANCE;
            if (distance <= ARRIVE_DISTANCE || stuck) {
                arrived = true;
                LionGoals.holdStill(lion);
                lion.setYRot(lion.getYRot() + (lion.getRandom().nextFloat() - 0.5f) * 120f);
                lion.yBodyRot = lion.getYRot();
                lion.setNap(true);
                return;
            }
            if (--repath <= 0) {
                repath = REPATH_TICKS;
                lion.getNavigation().moveTo(slot.x, slot.y, slot.z, 0.85);
            }
            return;
        }
        if (!lion.isNapping()) lion.setNap(true);
        LionGoals.holdStill(lion);
        LionGoals.napEffects(lion, napTicks++);
    }

    @Override
    public void stop() {
        if (lion.isNapping()) lion.setNap(false);
        lion.getNavigation().stop();
        arrived = false;
    }
}
