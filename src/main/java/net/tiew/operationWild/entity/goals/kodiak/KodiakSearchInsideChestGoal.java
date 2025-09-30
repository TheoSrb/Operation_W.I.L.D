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
    private boolean hasReachedChest = false;

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
        hasReachedChest = false;
        System.out.println("[KodiakChestGoal] Start - Target: " + targetPos);
    }

    @Override
    public void stop() {
        super.stop();
        System.out.println("[KodiakChestGoal] Stop - HasReached: " + hasReachedChest);
        targetPos = null;
        hasReachedChest = false;
        kodiak.getNavigation().stop();
        kodiak.kodiakAI.resetKodiakState();
    }

    @Override
    public boolean canUse() {
        return cooldownTicks == 0 && kodiak.getRandom().nextInt((int) (200 / attractionFrequencyMultiplier)) == 0 &&
                kodiak.getTarget() == null && kodiak.onGround() &&
                !kodiak.isNapping() && !kodiak.isDirty();
    }

    @Override
    public boolean canContinueToUse() {
        if (cooldownTicks > 0) {
            return false;
        }

        if (targetPos == null) {
            return false;
        }

        // Si le coffre n'existe plus, arrêter
        if (!kodiak.level().getBlockState(targetPos).is(Blocks.CHEST)) {
            System.out.println("[KodiakChestGoal] Chest no longer exists");
            return false;
        }

        // Si on a déjà atteint le coffre et ouvert, on peut arrêter
        if (hasReachedChest) {
            return false;
        }

        // Si on a de la nourriture dans la bouche, arrêter
        if (!kodiak.getFoodPick().isEmpty()) {
            return false;
        }

        return true;
    }

    @Override
    public void tick() {
        super.tick();

        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }

        if (targetPos != null && !hasReachedChest) {
            double distance = OWUtils.distanceRest(kodiak, targetPos);

            // Navigation continue jusqu'à atteindre la distance
            if (distance > 2.5) {
                kodiak.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, speedMultiplier);
            } else {
                // On a atteint le coffre
                kodiak.getNavigation().stop();

                if (kodiak.level().getBlockEntity(targetPos) instanceof ChestBlockEntity chestEntity) {
                    System.out.println("[KodiakChestGoal] Reached chest at distance: " + distance);
                    kodiak.chestBlockEntity = chestEntity;

                    // Faire regarder le Kodiak vers le coffre
                    kodiak.getLookControl().setLookAt(
                            targetPos.getX() + 0.5,
                            targetPos.getY() + 0.5,
                            targetPos.getZ() + 0.5
                    );

                    // Exécuter l'action d'ouverture
                    action.run();

                    // Marquer comme atteint
                    hasReachedChest = true;
                    kodiak.isSearchingInsideChest = true;

                    // Cooldown pour éviter de réutiliser immédiatement ce goal
                    cooldownTicks = 300;

                    System.out.println("[KodiakChestGoal] Chest opened successfully");
                }
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
            System.out.println("[KodiakChestGoal] No chest found in radius " + radiusToSearch);
            return null;
        }

        Random random = new Random();
        int randomIndex = random.nextInt(chestPositions.size());
        BlockPos selected = chestPositions.get(randomIndex);
        System.out.println("[KodiakChestGoal] Found " + chestPositions.size() + " chests, selected: " + selected);
        return selected;
    }
}