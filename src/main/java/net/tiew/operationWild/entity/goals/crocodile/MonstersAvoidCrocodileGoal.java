package net.tiew.operationWild.entity.goals.crocodile;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;

public class MonstersAvoidCrocodileGoal extends AvoidEntityGoal<CrocodileEntity> {

    public MonstersAvoidCrocodileGoal(PathfinderMob monster) {
        super(monster, CrocodileEntity.class, 12.0F, 1.0D, 1.3D);
    }
}