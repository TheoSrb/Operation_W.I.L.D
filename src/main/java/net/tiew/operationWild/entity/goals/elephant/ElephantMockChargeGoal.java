package net.tiew.operationWild.entity.goals.elephant;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.herd.ElephantHerd;
import net.tiew.operationWild.entity.herd.ElephantWildMind;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ElephantMockChargeGoal extends Goal {

    private static final double PERSONAL_RANGE = 6.0;
    private static final int DISPLAY_TICKS = 14;
    private static final int RUSH_MAX_TICKS = 22;
    private static final int HOLD_TICKS = 32;
    private static final double RUSH_SPEED = 7.0;
    private static final double STOP_MARGIN = 2.2;
    private static final int COOLDOWN_MIN = 240;
    private static final int COOLDOWN_SPREAD = 120;
    private static final double ESCALATE_FACTOR = 0.8;
    private static final int ESCALATE_TICKS = 400;

    private final ElephantEntity elephant;
    @Nullable
    private Player intruder;
    private int phase = ElephantWildMind.MOCK_IDLE;
    private int timer;
    private boolean finished;

    public ElephantMockChargeGoal(ElephantEntity elephant) {
        this.elephant = elephant;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (elephant.isMusth() || !elephant.wild.canMockCharge()) return false;
        if (!ElephantGoals.isCalm(elephant) || !elephant.onGround()) return false;

        Player player = elephant.wild.getAwareOf();
        if (player == null || !ElephantHerd.isValidThreat(elephant, player)) return false;
        if (!elephant.getSensing().hasLineOfSight(player)) return false;

        if (gapTo(player) > PERSONAL_RANGE) return false;

        intruder = player;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (finished || intruder == null || !ElephantHerd.isValidThreat(elephant, intruder)) return false;
        return ElephantGoals.isFreeWild(elephant) && elephant.getTarget() == null && !elephant.wild.isDefending();
    }

    @Override
    public void start() {
        finished = false;
        timer = 0;
        setPhase(ElephantWildMind.MOCK_DISPLAY);
        elephant.getNavigation().stop();
        elephant.wild.stopListening();
    }

    @Override
    public void stop() {
        setPhase(ElephantWildMind.MOCK_IDLE);
        elephant.getNavigation().stop();
        if (!finished) elephant.wild.setMockChargeCooldown(COOLDOWN_MIN);
        intruder = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (intruder == null || finished) return;
        timer++;
        elephant.getLookControl().setLookAt(intruder, 30f, 30f);

        switch (phase) {
            case ElephantWildMind.MOCK_DISPLAY -> tickDisplay();
            case ElephantWildMind.MOCK_RUSH -> tickRush();
            case ElephantWildMind.MOCK_HOLD -> tickHold();
            default -> {
            }
        }
    }

    private void tickDisplay() {
        elephant.getNavigation().stop();
        elephant.turnBodyTowards(intruder.position(), 9f);
        if (timer == 3) {
            elephant.level().playSound(null, elephant.getX(), elephant.getY(), elephant.getZ(),
                    OWSounds.ELEPHANT_SCREAM.get(), SoundSource.NEUTRAL, 2.5f, 1.1f);
        }
        if (timer < DISPLAY_TICKS) return;
        timer = 0;
        setPhase(ElephantWildMind.MOCK_RUSH);
    }

    private void tickRush() {
        double stop = (elephant.getBbWidth() + intruder.getBbWidth()) * 0.5 + STOP_MARGIN;
        Vec3 delta = intruder.position().subtract(elephant.position());
        double distance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        if (distance <= stop || timer >= RUSH_MAX_TICKS) {
            elephant.getNavigation().stop();
            Vec3 motion = elephant.getDeltaMovement();
            elephant.setDeltaMovement(motion.x * 0.2, motion.y, motion.z * 0.2);
            elephant.heavyStomp();
            elephant.level().playSound(null, elephant.getX(), elephant.getY(), elephant.getZ(),
                    OWSounds.ELEPHANT_CALL.get(), SoundSource.NEUTRAL, 2.5f, 1.15f);
            timer = 0;
            setPhase(ElephantWildMind.MOCK_HOLD);
            return;
        }
        if (timer % 5 == 1) elephant.getNavigation().moveTo(intruder, RUSH_SPEED);
    }

    private void tickHold() {
        elephant.getNavigation().stop();
        elephant.turnBodyTowards(intruder.position(), 4f);
        if (timer < HOLD_TICKS) return;

        boolean stayed = gapTo(intruder) <= PERSONAL_RANGE * ESCALATE_FACTOR;
        if (stayed && elephant.wild.wasRecentlyWarned(intruder)) {
            elephant.wild.answerAlarm(intruder, ESCALATE_TICKS, false);
        } else {
            elephant.wild.recordWarning(intruder);
        }
        elephant.wild.setMockChargeCooldown(COOLDOWN_MIN + elephant.getRandom().nextInt(COOLDOWN_SPREAD));
        finished = true;
        setPhase(ElephantWildMind.MOCK_IDLE);
    }

    private double gapTo(Player player) {
        double dx = player.getX() - elephant.getX();
        double dz = player.getZ() - elephant.getZ();
        return Math.sqrt(dx * dx + dz * dz) - elephant.getBbWidth() * 0.5;
    }

    private void setPhase(int phase) {
        this.phase = phase;
        elephant.wild.setMockPhase(phase);
    }
}
