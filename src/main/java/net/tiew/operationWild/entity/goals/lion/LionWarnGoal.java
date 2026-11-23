package net.tiew.operationWild.entity.goals.lion;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.clan.LionPride;
import net.tiew.operationWild.sound.OWSounds;

import java.util.EnumSet;

public class LionWarnGoal extends Goal {

    private static final double MAX_DISTANCE = 24.0;
    private static final int GROWL_MIN = 50;
    private static final int GROWL_SPREAD = 40;

    private final LionEntity lion;
    private int growlTimer;

    public LionWarnGoal(LionEntity lion) {
        this.lion = lion;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean active() {
        if (!LionGoals.isFreeWild(lion) || lion.getTarget() != null || lion.isNapping() || lion.isSitting()) return false;
        LivingEntity watched = lion.wild.warnTarget();
        return watched != null && lion.distanceTo(watched) <= MAX_DISTANCE;
    }

    @Override
    public boolean canUse() {
        return active();
    }

    @Override
    public boolean canContinueToUse() {
        return active();
    }

    @Override
    public void start() {
        growlTimer = lion.getRandom().nextInt(20);
        lion.setRunning(false);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity watched = lion.wild.warnTarget();
        if (watched == null) return;
        LionGoals.holdStill(lion);
        lion.setWildPose(LionEntity.WILD_POSE_THREAT);
        lion.getLookControl().setLookAt(watched, 30f, 30f);
        LionGoals.faceTowards(lion, watched.position(), 10f);
        if (--growlTimer <= 0) {
            growlTimer = GROWL_MIN + lion.getRandom().nextInt(GROWL_SPREAD);
            lion.level().playSound(null, lion.getX(), lion.getY(), lion.getZ(), OWSounds.TIGER_IDLE.get(),
                    SoundSource.NEUTRAL, 1.3f, LionPride.growlPitch(lion));
        }
    }

    @Override
    public void stop() {
        lion.setWildPose(LionEntity.WILD_POSE_NONE);
    }
}
