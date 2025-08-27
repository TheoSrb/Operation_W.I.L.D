package net.tiew.operationWild.entity.AI;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.Tags;
import net.tiew.operationWild.entity.custom.living.KodiakEntity;

import java.util.List;

public class KodiakSniffPlayerFoodGoal extends Goal {

    private final KodiakEntity kodiak;
    private Player playerTarget = null;

    public static int cooldown = 0;

    public KodiakSniffPlayerFoodGoal(KodiakEntity kodiak) {
        this.kodiak = kodiak;
    }

    @Override
    public void tick() {
        super.tick();

        if (cooldown >= 1) {
            cooldown++;

            if (cooldown >= 601) {
                cooldown = 0;
            }
            return;
        }


        if (cooldown <= 0) {
            List<Player> playersInTerritory = kodiak.level().getEntitiesOfClass(Player.class, kodiak.getBoundingBox().inflate(30));

            if (playerTarget == null) {
                for (Player player : playersInTerritory) {
                    if (player.isCreative()) continue;

                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        if (player.getInventory().getItem(i).is(Tags.Items.FOODS)) {
                            playerTarget = player;
                            break;
                        }
                    }
                }
            } else {
                float speed = 0;

                if (kodiak.distanceTo(playerTarget) > 15) {
                    kodiak.setRunning(true);
                    speed = kodiak.getSpeed() * 25;
                } else {
                    kodiak.setRunning(false);
                    speed = kodiak.getSpeed() * 10;
                }

                kodiak.setSniffing(true);
                kodiak.setLookAt(playerTarget.getX(), playerTarget.getY(), playerTarget.getZ());
                kodiak.getNavigation().moveTo(playerTarget.getX(), playerTarget.getY(), playerTarget.getZ(), speed);
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        kodiak.setRunning(false);
        kodiak.setSniffing(false);
        cooldown = 1;
        playerTarget = null;
    }

    @Override
    public boolean canUse() {
        boolean hasLivedLongEnough = kodiak.tickCount > (10 * kodiak.getMaxHealth());
        int cycleLength = 600;
        int availableWindow = 600;
        int currentCycleTime = kodiak.tickCount % cycleLength;
        boolean sometimes = currentCycleTime < availableWindow;
        return !kodiak.isTame() &&
                sometimes &&
                hasLivedLongEnough &&
                !kodiak.isBaby() &&
                kodiak.getTarget() == null &&
                !kodiak.isNapping() &&
                kodiak.getFoodChooseFromChest().isEmpty() &&
                !kodiak.isHungry();
    }
}
