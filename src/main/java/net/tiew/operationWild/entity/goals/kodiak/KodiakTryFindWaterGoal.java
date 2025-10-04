package net.tiew.operationWild.entity.goals.kodiak;

import com.google.common.base.Enums;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;

import java.util.EnumSet;

public class KodiakTryFindWaterGoal extends Goal {

    private final KodiakEntity kodiak;
    private BlockPos blockpos = null;
    private int cooldownTicks = 0;
    private static final int COOLDOWN_DURATION = 600;

    public KodiakTryFindWaterGoal(KodiakEntity kodiak) {
        this.kodiak = kodiak;

        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.cooldownTicks > 0) {
            this.cooldownTicks--;
        }

        if (blockpos != null) {
            this.kodiak.getNavigation().moveTo(blockpos.getX(), blockpos.getY(), blockpos.getZ(), 1.0f);
        }
    }

    public boolean canUse() {
        if (this.cooldownTicks > 0) {
            return false;
        }

        return kodiak.getRandom().nextInt(150) == 0 && this.kodiak.onGround() && !kodiak.isRubs() && !this.kodiak.level().getFluidState(this.kodiak.blockPosition()).is(FluidTags.WATER) && !kodiak.isCatchingSalmon();
    }

    @Override
    public boolean canContinueToUse() {
        return blockpos != null && !kodiak.isRubs();
    }

    public void start() {
        for(BlockPos blockpos1 : BlockPos.betweenClosed(
                Mth.floor(this.kodiak.getX() - 15.0F),
                Mth.floor(this.kodiak.getY() - 15.0F),
                Mth.floor(this.kodiak.getZ() - 15.0F),
                Mth.floor(this.kodiak.getX() + 15.0F),
                Mth.floor(this.kodiak.getY() + 15.0F),
                Mth.floor(this.kodiak.getZ() + 15.0F))) {
            if (this.kodiak.level().getFluidState(blockpos1).is(FluidTags.WATER)) {
                blockpos = blockpos1;
                this.cooldownTicks = COOLDOWN_DURATION;
                break;
            }
        }
    }
}