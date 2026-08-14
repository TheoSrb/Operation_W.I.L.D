package net.tiew.operationWild.entity.goals.red_panda;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity;

import java.util.EnumSet;

public class RedPandaPlayGoal extends Goal {

    private static final int TRY_CHANCE = 700;
    private static final int COOLDOWN_TICKS = 900;
    private static final int CHIRP_INTERVAL = 45;

    private final RedPandaEntity panda;

    private int elapsed;
    private int cooldown;

    public RedPandaPlayGoal(RedPandaEntity panda) {
        this.panda = panda;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (!isAvailable()) return false;
        return panda.getRandom().nextInt(TRY_CHANCE) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return panda.isPlaying() && isAvailable();
    }

    @Override
    public void start() {
        elapsed = 0;
        panda.getNavigation().stop();
        panda.setPlayTimer(RedPandaEntity.PLAY_TICKS);

        panda.level().playSound(null, panda.getX(), panda.getY(), panda.getZ(),
                SoundEvents.FOX_AMBIENT, SoundSource.NEUTRAL, 0.6f,
                (float) OWUtils.generateRandomInterval(1.3, 1.5));
    }

    @Override
    public void stop() {
        panda.setPlayTimer(0);
        elapsed = 0;
        cooldown = COOLDOWN_TICKS + panda.getRandom().nextInt(COOLDOWN_TICKS);
    }

    @Override
    public void tick() {
        elapsed++;

        panda.getNavigation().stop();
        if (panda.onGround()) {
            panda.setDeltaMovement(panda.getDeltaMovement().multiply(0.0, 1.0, 0.0));
        }

        if (elapsed % CHIRP_INTERVAL != 0) return;
        panda.level().playSound(null, panda.getX(), panda.getY(), panda.getZ(),
                SoundEvents.FOX_SNIFF, SoundSource.NEUTRAL, 0.35f,
                (float) OWUtils.generateRandomInterval(1.4, 1.7));
    }

    private boolean isAvailable() {
        return panda.isAlive()
                && panda.onGround()
                && !panda.isMoving()
                && !panda.isSitting()
                && !panda.isSleeping()
                && !panda.isNapping()
                && !panda.isInWater()
                && !panda.isInFight()
                && !panda.isPassenger()
                && !panda.isVehicle()
                && !panda.isOnShoulder()
                && !panda.isAlerted()
                && !panda.isCowering()
                && panda.getFearRecoverTimer() <= 0
                && !panda.isIntimidating()
                && !panda.isEatingMeal()
                && !panda.isTreeClimbing()
                && !panda.isClimbing()
                && panda.getTarget() == null;
    }
}
