package net.tiew.operationWild.entity.goals.lion;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.clan.LionClanData;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

public class LionDuelWatchGoal extends Goal {

    private static final int CHECK_INTERVAL = 10;
    private static final double DUEL_RANGE = 40.0;
    private static final double RING_RADIUS = 7.0;
    private static final double SLOT_REACHED = 1.5;
    private static final int REPATH_TICKS = 10;

    private final LionEntity lion;
    @Nullable
    private LionEntity champion;
    @Nullable
    private LionEntity challenger;
    private int repath;

    public LionDuelWatchGoal(LionEntity lion) {
        this.lion = lion;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public static boolean isDueling(LionEntity male) {
        return male.isAlive() && male.isMale() && !male.isTame() && male.getTarget() instanceof LionEntity rival
                && rival.isMale() && !rival.isTame() && !male.isClanmate(rival);
    }

    private boolean findDuel() {
        champion = null;
        challenger = null;
        LionClanData.Clan clan = lion.wildClan();
        if (clan == null || !(lion.level() instanceof ServerLevel level)) return false;
        for (UUID id : clan.males()) {
            if (!(level.getEntity(id) instanceof LionEntity male) || male.distanceTo(lion) > DUEL_RANGE || !isDueling(male)) continue;
            champion = male;
            challenger = (LionEntity) male.getTarget();
            return true;
        }
        return false;
    }

    private boolean eligible() {
        if (!lion.isFemale() || lion.isBaby() || !LionGoals.isFreeWild(lion) || lion.isNapping()) return false;
        return lion.getTarget() == null || lion.getTarget() instanceof LionEntity;
    }

    @Override
    public boolean canUse() {
        return LionGoals.onSchedule(lion, CHECK_INTERVAL) && eligible() && findDuel();
    }

    @Override
    public boolean canContinueToUse() {
        return eligible() && champion != null && challenger != null && isDueling(champion) && champion.getTarget() == challenger;
    }

    @Override
    public void start() {
        repath = 0;
        if (lion.getTarget() instanceof LionEntity) lion.setTarget(null);
        lion.setRunning(false);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (champion == null || challenger == null) return;
        if (lion.getTarget() instanceof LionEntity) lion.setTarget(null);
        Vec3 middle = champion.position().add(challenger.position()).scale(0.5);
        double angle = Math.floorMod(lion.getUUID().hashCode(), 360) * Math.PI / 180.0;
        Vec3 slot = middle.add(Math.cos(angle) * RING_RADIUS, 0, Math.sin(angle) * RING_RADIUS);
        lion.getLookControl().setLookAt(middle.x, middle.y + 0.8, middle.z, 20f, 20f);
        if (LionGoals.horizontalDistance(lion, slot) > SLOT_REACHED) {
            lion.setGuardPosed(false);
            if (--repath <= 0) {
                repath = REPATH_TICKS;
                lion.getNavigation().moveTo(slot.x, slot.y, slot.z, 1.2);
            }
            return;
        }
        LionGoals.holdStill(lion);
        LionGoals.faceTowards(lion, middle, 10f);
        lion.setGuardPosed(true);
    }

    @Override
    public void stop() {
        lion.setGuardPosed(false);
        lion.getNavigation().stop();
        champion = null;
        challenger = null;
    }
}
