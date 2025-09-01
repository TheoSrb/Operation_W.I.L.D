package net.tiew.operationWild.entity.goals;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.goal.Goal;
import net.tiew.operationWild.entity.OWEntity;

import java.util.EnumSet;
import java.util.function.Supplier;

public class NapGoal extends Goal {
    private final OWEntity mob;
    private final int intervalTimeInTicks;
    private final int napTimeInTicks;
    private final int prepareNapTimeInTicks;
    private final int snoreIntervallInTicks;
    private final SoundEvent snoreSound;
    private final boolean canTurnHead;
    private final Supplier<Boolean> napCondition;

    public NapGoal(OWEntity mob,
                   int intervalTimeInTicks,
                   int napTimeInTicks,
                   int prepareNapTimeInTicks,
                   int snoreIntervallInTicks,
                   SoundEvent snoreSound,
                   boolean canTurnHead,
                   Supplier<Boolean> napCondition) {
        this.mob = mob;
        this.intervalTimeInTicks = intervalTimeInTicks;
        this.napTimeInTicks = napTimeInTicks;
        this.prepareNapTimeInTicks = prepareNapTimeInTicks;
        this.snoreIntervallInTicks = snoreIntervallInTicks;
        this.snoreSound = snoreSound;
        this.canTurnHead = canTurnHead;
        this.napCondition = napCondition;
    }

    @Override
    public boolean canUse() {
        return !this.mob.isInFight();
    }

    @Override
    public void tick() {
        super.tick();
        if (mob.isBaby()) return;
        this.mob.createNapSystem(
                mob,
                this.intervalTimeInTicks,
                this.napTimeInTicks,
                this.prepareNapTimeInTicks,
                this.snoreIntervallInTicks,
                this.snoreSound,
                this.canTurnHead,
                this.napCondition.get()
        );
    }
}
