package net.tiew.operationWild.entity.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.tiew.operationWild.entity.custom.living.KodiakEntity;
import net.tiew.operationWild.item.OWItems;

import java.util.EnumSet;

public class KodiakRubTreeGoal extends Goal {

    private final KodiakEntity kodiak;
    private final float speedModifier;

    private BlockPos treePos = null;

    private int rubTimer = 0;

    public KodiakRubTreeGoal(KodiakEntity kodiak, float speedModifier) {
        this.kodiak = kodiak;
        this.speedModifier = speedModifier;

        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public void tick() {
        super.tick();

        if (isNextToTheTree(this.treePos)) {
            startRub();
            if (this.kodiak.isRub()) {
                if (this.kodiak.getRandom().nextInt(75) == 0) {
                    this.kodiak.spawnAtLocation(OWItems.KODIAK_COAT.get());
                }

                rubTimer++;

                System.out.println(rubTimer);
            } else stop();
        }
    }

    @Override
    public void start() {
        super.start();

        BlockPos treePos = findNearestTree(20);

        if (treePos != null) {
            this.treePos = treePos;
            this.kodiak.getNavigation().moveTo(treePos.getX(), treePos.getY(), treePos.getZ(), speedModifier);
        }
    }

    @Override
    public void stop() {
        super.stop();
        treePos = null;
        kodiak.setRub(false);
        rubTimer = 0;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.treePos == null) return false;

        BlockState blockState = this.kodiak.level().getBlockState(this.treePos);
        if (!blockState.is(BlockTags.LOGS)) {
            return false;
        }

        BlockState aboveState = this.kodiak.level().getBlockState(this.treePos.above());
        if (!aboveState.is(BlockTags.LOGS)) {
            return false;
        }

        if (rubTimer >= 200) return false;

        return true;
    }

    @Override
    public boolean canUse() {
        return this.kodiak.getRandom().nextInt(100) == 0;
    }

    private BlockPos findNearestLog(int searchRadius) {
        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos pos = this.kodiak.blockPosition().offset(x, y, z);
                    BlockState blockState = this.kodiak.level().getBlockState(pos);

                    if (blockState.is(BlockTags.LOGS)) return pos;
                }
            }
        }

        return null;
    }

    private BlockPos findNearestTree(int searchRadius) {
        BlockPos logPos = findNearestLog(searchRadius);
        if (logPos == null) return null;

        if (kodiak.level().getBlockState(logPos.above()).is(BlockTags.LOGS)) return logPos;

        return null;
    }

    private boolean isNextToTheTree(BlockPos treePos) {
        if (this.treePos == null) return false;
        return kodiak.distanceToSqr(treePos.getX(), treePos.getY(), treePos.getZ()) <= 7;
    }

    private void startRub() {
        kodiak.setRub(true);
        this.kodiak.getLookControl().setLookAt(treePos.getX(), treePos.getY() + 2, treePos.getZ());
    }
}
