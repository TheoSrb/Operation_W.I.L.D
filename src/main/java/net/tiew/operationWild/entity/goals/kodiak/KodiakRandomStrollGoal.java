package net.tiew.operationWild.entity.goals.kodiak;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;

public class KodiakRandomStrollGoal extends RandomStrollGoal {
    public KodiakRandomStrollGoal(PathfinderMob mob, double speedModifier) {
        super(mob, speedModifier, 60);
    }
}
