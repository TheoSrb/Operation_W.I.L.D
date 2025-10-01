package net.tiew.operationWild.entity.AI;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.goals.kodiak.KodiakAttractedToGoal;
import net.tiew.operationWild.entity.goals.kodiak.KodiakSearchInsideChestGoal;

public class AIKodiak extends AIOWEntity {

    private KodiakEntity kodiak;
    public AIKodiakManagement kodiakManagement;
    private static final int STATE_CHANGE_INTERVAL = 400;

    private KodiakSearchInsideChestGoal chestGoal;
    private KodiakAttractedToGoal<?> campfireGoal;
    private KodiakAttractedToGoal<?> beeNestGoal;
    private KodiakAttractedToGoal<?> cropsGoal;

    private boolean forceGoalActivation = false;

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
        initializeGoals();
    }

    private void initializeGoals() {
        chestGoal = new KodiakSearchInsideChestGoal(kodiak, 20.0f, 35,
                1.5f, () -> kodiakManagement.openChest(kodiak.chestBlockEntity));

        campfireGoal = new KodiakAttractedToGoal<>(kodiak, Blocks.CAMPFIRE,
                1.0f, 60, 200.0f, () -> kodiakManagement.pickupItemInHisMouth(kodiak.foodPick),
                true);

        beeNestGoal = new KodiakAttractedToGoal<>(kodiak, Blocks.BEE_NEST,
                1.75f, 25, 200.0f, kodiakManagement::lookForHoneyInTheBeeNest,
                true);

        cropsGoal = new KodiakAttractedToGoal<>(kodiak, BlockTags.CROPS,
                1.15f, 80, 200.0f, () -> kodiakManagement.goToNewCropBlock(20),
                true);
    }

    @Override
    public Temperament getTemperament() {
        return Temperament.NEUTRAL;
    }

    public void tick() {
        /*if (!kodiak.level().isClientSide()) {
            if (kodiak.tickCount % STATE_CHANGE_INTERVAL == 0) {
                changeToRandomState();
            }
        }*/
    }

    private void changeToRandomState() {
        KodiakState newState = getRandomState();
        kodiak.setKodiakState(newState);
        adaptGoalsToState(newState);
    }

    private void adaptGoalsToState(KodiakState state) {
        kodiak.goalSelector.removeGoal(chestGoal);
        kodiak.goalSelector.removeGoal(campfireGoal);
        kodiak.goalSelector.removeGoal(beeNestGoal);
        kodiak.goalSelector.removeGoal(cropsGoal);

        switch (state) {
            case GOING_TO_CHEST:
                kodiak.goalSelector.addGoal(1, chestGoal);
                chestGoal.start();
                break;
            case GOING_TO_CAMPFIRE:
                kodiak.goalSelector.addGoal(1, campfireGoal);
                campfireGoal.start();
                break;
            case GOING_TO_BEE_NEST:
                kodiak.goalSelector.addGoal(1, beeNestGoal);
                beeNestGoal.start();
                break;
            case GOING_TO_CROPS:
                kodiak.goalSelector.addGoal(1, cropsGoal);
                cropsGoal.start();
                break;
            case IDLE:
            default:
                break;
        }

        System.out.println("Etat changé -> " + kodiak.getKodiakState());
    }

    public void resetKodiakState() {
        kodiak.goalSelector.removeGoal(chestGoal);
        kodiak.goalSelector.removeGoal(campfireGoal);
        kodiak.goalSelector.removeGoal(beeNestGoal);
        kodiak.goalSelector.removeGoal(cropsGoal);
        kodiak.setKodiakState(KodiakState.IDLE);
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