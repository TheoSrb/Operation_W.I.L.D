package net.tiew.operationWild.entity.AI;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Blocks;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.goals.kodiak.KodiakAttractedToGoal;
import net.tiew.operationWild.entity.goals.kodiak.KodiakSearchInsideChestGoal;

public class AIKodiak extends AIOWEntity {

    private KodiakEntity kodiak;
    public AIKodiakManagement kodiakManagement;
    private final int UPDATE_STATE_COOLDOWN = 20;
    private final int GOAL_START_TIMEOUT = 600;

    private int goalTimeoutTimer = 0;
    private boolean goalHasStarted = false;

    private KodiakSearchInsideChestGoal chestGoal;
    private KodiakAttractedToGoal<?> campfireGoal;
    private KodiakAttractedToGoal<?> beeNestGoal;
    private KodiakAttractedToGoal<?> cropsGoal;

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

    private void initializeGoals() {
        chestGoal = new KodiakSearchInsideChestGoal(kodiak, 6.0f, 35,
                1.5f, () -> kodiakManagement.openChest(kodiak.chestBlockEntity));

        campfireGoal = new KodiakAttractedToGoal<>(kodiak, Blocks.CAMPFIRE,
                1.0f, 60, 8.0f, () -> kodiakManagement.pickupItemInHisMouth(kodiak.foodPick),
                kodiak.getFoodPick().isEmpty());

        beeNestGoal = new KodiakAttractedToGoal<>(kodiak, Blocks.BEE_NEST,
                1.75f, 25, 4.0f, kodiakManagement::lookForHoneyInTheBeeNest,
                kodiak.getFoodPick().isEmpty());

        cropsGoal = new KodiakAttractedToGoal<>(kodiak, BlockTags.CROPS,
                1.15f, 80, 10f, () -> kodiakManagement.goToNewCropBlock(20),
                kodiak.getFoodPick().isEmpty());
    }

    @Override
    public Temperament getTemperament() {
        return Temperament.NEUTRAL;
    }

    public void tick() {
        if (!kodiak.level().isClientSide()) {
            if (kodiak.getKodiakState() != KodiakState.IDLE && !goalHasStarted) {
                goalTimeoutTimer++;

                boolean isGoalRunning = isCurrentGoalRunning();

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

    private boolean isCurrentGoalRunning() {
        KodiakState state = kodiak.getKodiakState();

        switch (state) {
            case GOING_TO_CHEST:
                return isGoalRunning(chestGoal);
            case GOING_TO_CAMPFIRE:
                return isGoalRunning(campfireGoal);
            case GOING_TO_BEE_NEST:
                return isGoalRunning(beeNestGoal);
            case GOING_TO_CROPS:
                return isGoalRunning(cropsGoal);
            default:
                return false;
        }
    }

    private boolean isGoalRunning(Goal goal) {
        if (goal == null) return false;

        try {
            for (var wrappedGoal : kodiak.goalSelector.getAvailableGoals()) {
                if (wrappedGoal != null && wrappedGoal.getGoal() == goal) {
                    return wrappedGoal.isRunning();
                }
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    private void updateState() {
        KodiakState currentState = kodiak.getKodiakState();

        if (currentState == KodiakState.IDLE) {
            initializeGoals();

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
        kodiak.goalSelector.removeGoal(chestGoal);
        kodiak.goalSelector.removeGoal(campfireGoal);
        kodiak.goalSelector.removeGoal(beeNestGoal);
        kodiak.goalSelector.removeGoal(cropsGoal);

        kodiak.setKodiakState(KodiakState.IDLE);

        goalTimeoutTimer = 0;
        goalHasStarted = false;
    }

    private void adaptGoalsToStatement(KodiakState state) {
        kodiak.goalSelector.removeGoal(chestGoal);
        kodiak.goalSelector.removeGoal(campfireGoal);
        kodiak.goalSelector.removeGoal(beeNestGoal);
        kodiak.goalSelector.removeGoal(cropsGoal);

        System.out.println("State: " + state);

        switch (state) {
            case IDLE:
                break;

            case GOING_TO_CHEST:
                kodiak.goalSelector.addGoal(2, chestGoal);
                break;

            case GOING_TO_CAMPFIRE:
                kodiak.goalSelector.addGoal(4, campfireGoal);
                break;

            case GOING_TO_BEE_NEST:
                kodiak.goalSelector.addGoal(6, beeNestGoal);
                break;

            case GOING_TO_CROPS:
                kodiak.goalSelector.addGoal(8, cropsGoal);
                break;
        }
    }

    public KodiakState getRandomState() {
        KodiakState[] states = KodiakState.values();
        int maxValue = states.length;

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