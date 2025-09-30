package net.tiew.operationWild.entity.goals.kodiak;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class KodiakSearchInsideChestGoal extends Goal {

    private final KodiakEntity kodiak;
    private final float attractionFrequencyMultiplier;
    private final int radius;
    private final float speedMultiplier;
    private final Runnable action;

    private BlockPos targetPos;
    private int cooldownTicks = 0;

    public KodiakSearchInsideChestGoal(KodiakEntity kodiak, float attractionFrequencyMultiplier, int radius, float speedMultiplier, Runnable action) {
        this.kodiak = kodiak;
        this.attractionFrequencyMultiplier = attractionFrequencyMultiplier;
        this.radius = radius;
        this.speedMultiplier = speedMultiplier;
        this.action = action;

        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    @Override
    public void start() {
        super.start();
        targetPos = findRandomChestPos(radius);
    }

    @Override
    public void stop() {
        super.stop();
        targetPos = null;
    }

    @Override
    public boolean canUse() {
        return cooldownTicks == 0 && kodiak.getRandom().nextInt((int) (200 / attractionFrequencyMultiplier)) == 0 &&
                kodiak.getTarget() == null && kodiak.onGround() &&
                !kodiak.isNapping() && !kodiak.isDirty();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && targetPos != null && cooldownTicks == 0 && kodiak.level().getBlockState(targetPos).is(Blocks.CHEST) && OWUtils.distanceRest(kodiak, targetPos) >= 1 &&
                kodiak.getFoodPick().isEmpty() ;
    }

    @Override
    public void tick() {
        super.tick();

        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }

        if (targetPos != null) {
            if (!kodiak.level().getBlockState(targetPos).is(Blocks.CHEST)) {
                stop();
                return;
            }

            kodiak.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), speedMultiplier);

            if (OWUtils.distanceRest(kodiak, targetPos) <= 4) {
                if (kodiak.level().getBlockEntity(targetPos) instanceof ChestBlockEntity chestEntity) {
                    kodiak.chestBlockEntity = chestEntity;
                }
                action.run();
                cooldownTicks = 300;
                kodiak.isSearchingInsideChest = true;
            }
        }
    }

    private BlockPos findRandomChestPos(int radiusToSearch) {
        BlockPos kodiakPos = kodiak.blockPosition();
        List<BlockPos> chestPositions = new ArrayList<>();

        for (int x = -radiusToSearch; x <= radiusToSearch; x++) {
            for (int y = -radiusToSearch; y <= radiusToSearch; y++) {
                for (int z = -radiusToSearch; z <= radiusToSearch; z++) {
                    BlockPos pos = kodiakPos.offset(x, y, z);
                    if (kodiak.level().getBlockState(pos).is(Blocks.CHEST)) {
                        chestPositions.add(pos);
                    }
                }
            }
        }

        if (chestPositions.isEmpty()) {
            return null;
        }

        Random random = new Random();
        int randomIndex = random.nextInt(chestPositions.size());
        return chestPositions.get(randomIndex);
    }
}