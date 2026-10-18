package net.tiew.operationWild.entity.goals.elephant;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ElephantMournGoal extends Goal {

    private static final double VIGIL_DISTANCE = 3.2;
    private static final double WALK_SPEED = 0.75;
    private static final int RUMBLE_MIN_INTERVAL = 60;
    private static final int RUMBLE_INTERVAL_SPREAD = 50;
    private static final int APPROACH_TIMEOUT = 600;

    private final ElephantEntity elephant;
    @Nullable
    private Vec3 spot;
    private int vigilTicks;
    private int approachTicks;
    private int nextRumble;

    public ElephantMournGoal(ElephantEntity elephant) {
        this.elephant = elephant;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return elephant.wild.getMournPos() != null && ElephantGoals.isCalm(elephant);
    }

    @Override
    public boolean canContinueToUse() {
        return spot != null && elephant.wild.getMournPos() != null && ElephantGoals.isCalm(elephant)
                && vigilTicks < elephant.wild.mournDuration() && approachTicks < APPROACH_TIMEOUT;
    }

    @Override
    public void start() {
        spot = elephant.wild.getMournPos();
        vigilTicks = 0;
        approachTicks = 0;
        nextRumble = 20 + elephant.getRandom().nextInt(RUMBLE_INTERVAL_SPREAD);
    }

    @Override
    public void stop() {
        elephant.stopAiTrunk();
        elephant.getNavigation().stop();
        if (vigilTicks >= elephant.wild.mournDuration() || approachTicks >= APPROACH_TIMEOUT) elephant.wild.endMourning();
        spot = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (spot == null) return;
        double reach = VIGIL_DISTANCE + elephant.getBbWidth() * 0.5;
        if (ElephantGoals.horizontalDistance(elephant, spot) > reach) {
            approachTicks++;
            elephant.stopAiTrunk();
            if (approachTicks % 20 == 1) elephant.getNavigation().moveTo(spot.x, spot.y, spot.z, WALK_SPEED);
            return;
        }

        elephant.getNavigation().stop();
        elephant.turnBodyTowards(spot, 3f);
        elephant.getLookControl().setLookAt(spot.x, spot.y, spot.z, 10f, 30f);
        if (ElephantGoals.yawGap(elephant, spot) > 35f) return;

        if (elephant.startAiTrunk(ElephantEntity.TRUNK_TOUCHING)) elephant.aimAiTrunkAt(spot);
        vigilTicks++;

        if (--nextRumble <= 0) {
            nextRumble = RUMBLE_MIN_INTERVAL + elephant.getRandom().nextInt(RUMBLE_INTERVAL_SPREAD);
            SoundEvent rumble = switch (elephant.getRandom().nextInt(3)) {
                case 0 -> OWSounds.ELEPHANT_IDLE.get();
                case 1 -> OWSounds.ELEPHANT_IDLE_2.get();
                default -> OWSounds.ELEPHANT_IDLE_3.get();
            };
            elephant.level().playSound(null, elephant.getX(), elephant.getY(), elephant.getZ(),
                    rumble, SoundSource.NEUTRAL, 1.3f, 0.55f + elephant.getRandom().nextFloat() * 0.1f);
        }
    }
}
