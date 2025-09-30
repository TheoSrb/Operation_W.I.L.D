package net.tiew.operationWild.entity.AI;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.goals.NapGoal;
import net.tiew.operationWild.entity.goals.kodiak.KodiakAttractedToGoal;
import net.tiew.operationWild.entity.goals.kodiak.KodiakRollGoal;
import net.tiew.operationWild.entity.goals.kodiak.KodiakSearchInsideChestGoal;

/**
 * This class mainly manages the Kodiak's artificial intelligence.
 * In particular, it manages how goals are distributed based on the Kodiak's current status and assigns the appropriate behavior to it.
 */

public class AIKodiak extends AIOWEntity {

    private KodiakEntity kodiak;
    public AIKodiakManagement kodiakManagement;
    private final int UPDATE_STATE_COOLDOWN = 20;
    private final int GOAL_START_TIMEOUT = 600;

    private int goalTimeoutTimer = 0;
    private boolean goalHasStarted = false;

    public enum KodiakState {
        IDLE(-1),
        GOING_TO_CHEST(1),
        GOING_TO_CAMPFIRE(2),
        GOING_TO_BEE_NEST(1),
        GOING_TO_CROPS(2);

        private int priority;

        KodiakState(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }

    public AIKodiak(KodiakEntity kodiak) {
        super(kodiak);
        this.kodiak = kodiak;
        this.kodiakManagement = new AIKodiakManagement(kodiak, this);
    }

    @Override
    public Temperament getTemperament() {
        return Temperament.NEUTRAL;
    }

    public void tick() {
        if (!kodiak.level().isClientSide()) {
            if (kodiak.getKodiakState() != KodiakState.IDLE && !goalHasStarted) {
                goalTimeoutTimer++;

                boolean isGoalRunning = kodiak.goalSelector.getAvailableGoals().stream()
                        .anyMatch(goal -> (goal.getGoal() instanceof NapGoal ||
                                goal.getGoal() instanceof KodiakRollGoal ||
                                goal.getGoal() instanceof KodiakSearchInsideChestGoal ||
                                goal.getGoal() instanceof KodiakAttractedToGoal)
                                && goal.isRunning());

                if (isGoalRunning) {
                    goalHasStarted = true;
                    System.out.println("Goal démarré !");
                }

                if (goalTimeoutTimer >= GOAL_START_TIMEOUT) {
                    System.out.println("TIMEOUT: Le goal n'a pas démarré après 30s, reset...");
                    resetKodiakState();
                }
            }

            if (kodiak.tickCount % this.UPDATE_STATE_COOLDOWN == 0) {
                updateState();
            }
        }
    }

    private void updateState() {
        KodiakState currentState = kodiak.getKodiakState();

        if (currentState == KodiakState.IDLE) {
            KodiakState state = getRandomState();
            kodiak.setKodiakState(state);

            System.out.println("----- Changement d'état -----");
            System.out.println(kodiak.getKodiakState());

            adaptGoalsToStatement(state);

            goalTimeoutTimer = 0;
            goalHasStarted = false;
        }
    }

    public void resetKodiakState() {
        kodiak.goalSelector.getAvailableGoals().removeIf(goal ->
                        goal.getGoal() instanceof KodiakSearchInsideChestGoal ||
                        goal.getGoal() instanceof KodiakAttractedToGoal
        );

        kodiak.setKodiakState(KodiakState.IDLE);

        goalTimeoutTimer = 0;
        goalHasStarted = false;
    }

    private void adaptGoalsToStatement(KodiakState state) {
        kodiak.goalSelector.getAvailableGoals().removeIf(goal ->
                        goal.getGoal() instanceof KodiakSearchInsideChestGoal ||
                        goal.getGoal() instanceof KodiakAttractedToGoal
        );

        System.out.println("State: " + state);

        switch (state) {
            case IDLE:
                break;

            case GOING_TO_CHEST:
                kodiak.goalSelector.addGoal(2, new KodiakSearchInsideChestGoal(kodiak, 5.0f, 35,
                        1.5f, () -> kodiakManagement.openChest(kodiak.chestBlockEntity)));
                break;

            case GOING_TO_CAMPFIRE:
                kodiak.goalSelector.addGoal(4, new KodiakAttractedToGoal<>(kodiak, Blocks.CAMPFIRE,
                        1.0f, 60, 2.0f, () -> kodiakManagement.pickupItemInHisMouth(kodiak.foodPick), kodiak.getFoodPick().isEmpty()));
                break;

            case GOING_TO_BEE_NEST:
                kodiak.goalSelector.addGoal(6, new KodiakAttractedToGoal<>(kodiak, Blocks.BEE_NEST,
                        1.75f, 25, 3.0f, kodiakManagement::lookForHoneyInTheBeeNest, kodiak.getFoodPick().isEmpty()));
                break;

            case GOING_TO_CROPS:
                kodiak.goalSelector.addGoal(8, new KodiakAttractedToGoal<>(kodiak, BlockTags.CROPS,
                        1.15f, 80, 1.5f, () -> kodiakManagement.goToNewCropBlock(20), kodiak.getFoodPick().isEmpty()));
                break;
        }
    }

    public KodiakState getRandomState() {
        KodiakState[] states = KodiakState.values();
        int maxValue = states.length - 1;

        int totalWeight = 0;
        for (KodiakState state : states) {
            if (state.getPriority() >= 0) {
                totalWeight += (maxValue - state.getPriority());
            }
        }

        int randomValue = kodiak.getRandom().nextInt(totalWeight);
        int cumulative = 0;

        for (KodiakState state : states) {
            if (state.getPriority() >= 0) {
                cumulative += (maxValue - state.getPriority());
                if (randomValue < cumulative) {
                    return state;
                }
            }
        }

        return KodiakState.IDLE;
    }
}