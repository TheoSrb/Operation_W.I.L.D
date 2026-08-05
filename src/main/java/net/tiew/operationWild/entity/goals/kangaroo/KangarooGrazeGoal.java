package net.tiew.operationWild.entity.goals.kangaroo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;

import java.util.EnumSet;

public class KangarooGrazeGoal extends Goal {

    private static final int SEARCH_RADIUS = 8;
    private static final int SEARCH_SAMPLES = 32;
    private static final int GRAZE_DURATION = 60;
    private static final int BITE_TICK = 36;
    private static final int TRAVEL_TIMEOUT = 200;
    private static final double REACH_SQR = 2.75 * 2.75;
    private static final float GRAZE_HEAL = 0.5f;
    private static final int STRIP_CHANCE = 3;

    private final KangarooEntity kangaroo;
    private final double speedModifier;
    private final int cooldownTicks;

    private BlockPos spot;
    private int travelTicks;
    private int grazeTicks;
    private int cooldown;
    private boolean biteDone;

    public KangarooGrazeGoal(KangarooEntity kangaroo, double speedModifier, int cooldownTicks) {
        this.kangaroo = kangaroo;
        this.speedModifier = speedModifier;
        this.cooldownTicks = cooldownTicks;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (!isAvailable()) return false;
        if (kangaroo.getRandom().nextInt(60) != 0) return false;

        spot = findGrazingSpot();
        return spot != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (spot == null || !isAvailable()) return false;
        if (grazeTicks == 0 && travelTicks > TRAVEL_TIMEOUT) return false;
        return isEdible(kangaroo.level().getBlockState(spot));
    }

    @Override
    public void start() {
        travelTicks = 0;
        grazeTicks = 0;
        biteDone = false;
        if (spot != null) {
            kangaroo.getNavigation().moveTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, speedModifier);
        }
    }

    @Override
    public void stop() {
        kangaroo.setGrazing(false);
        kangaroo.getNavigation().stop();
        spot = null;
        travelTicks = 0;
        grazeTicks = 0;
        biteDone = false;
        cooldown = cooldownTicks + kangaroo.getRandom().nextInt(cooldownTicks);
    }

    @Override
    public void tick() {
        if (spot == null) return;

        Vec3 center = Vec3.atCenterOf(spot);
        kangaroo.getLookControl().setLookAt(center.x, center.y, center.z);

        if (kangaroo.distanceToSqr(center) > REACH_SQR) {
            travelTicks++;
            if (kangaroo.getNavigation().isDone()) {
                kangaroo.getNavigation().moveTo(center.x, center.y, center.z, speedModifier);
            }
            return;
        }

        kangaroo.getNavigation().stop();
        if (kangaroo.onGround()) {
            kangaroo.setDeltaMovement(kangaroo.getDeltaMovement().multiply(0.0, 1.0, 0.0));
        }

        if (!kangaroo.isGrazing()) kangaroo.setGrazing(true);
        grazeTicks++;

        if (grazeTicks % 12 == 6) spawnMouthfulParticles();

        if (!biteDone && grazeTicks >= BITE_TICK) {
            biteDone = true;
            consumeSpot();
        }

        if (grazeTicks >= GRAZE_DURATION) {
            kangaroo.setGrazing(false);
            spot = null;
        }
    }

    private boolean isAvailable() {
        return !kangaroo.isTame()
                && !kangaroo.isBaby()
                && !kangaroo.isAngry()
                && !kangaroo.isAlerted()
                && !kangaroo.isThumping()
                && !kangaroo.isNapping()
                && !kangaroo.isSleeping()
                && !kangaroo.isSitting()
                && !kangaroo.isVehicle()
                && !kangaroo.isInWater()
                && !kangaroo.isInFight()
                && kangaroo.getTarget() == null
                && kangaroo.onGround();
    }

    private BlockPos findGrazingSpot() {
        BlockPos origin = kangaroo.blockPosition();

        for (int i = 0; i < SEARCH_SAMPLES; i++) {
            BlockPos pos = origin.offset(
                    kangaroo.getRandom().nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS,
                    kangaroo.getRandom().nextInt(3) - 1,
                    kangaroo.getRandom().nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS);

            if (isEdible(kangaroo.level().getBlockState(pos))) return pos.immutable();
        }
        return null;
    }

    private boolean isEdible(BlockState state) {
        if (state.is(BlockTags.CROPS)) return false;
        return state.is(Blocks.SHORT_GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN)
                || state.is(BlockTags.SMALL_FLOWERS);
    }

    private void consumeSpot() {
        BlockState state = kangaroo.level().getBlockState(spot);
        if (!isEdible(state)) return;

        kangaroo.level().playSound(null, spot, SoundEvents.GRASS_BREAK, SoundSource.NEUTRAL, 0.7f,
                0.8f + kangaroo.getRandom().nextFloat() * 0.3f);
        kangaroo.heal(GRAZE_HEAL);

        if (!kangaroo.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return;

        kangaroo.level().destroyBlock(spot, false);

        BlockPos ground = spot.below();
        if (kangaroo.level().getBlockState(ground).is(Blocks.GRASS_BLOCK)
                && kangaroo.getRandom().nextInt(STRIP_CHANCE) == 0) {
            kangaroo.level().setBlockAndUpdate(ground, Blocks.DIRT.defaultBlockState());
        }
    }

    private void spawnMouthfulParticles() {
        if (!(kangaroo.level() instanceof ServerLevel serverLevel)) return;

        BlockState state = kangaroo.level().getBlockState(spot);
        if (state.isAir()) return;

        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                spot.getX() + 0.5, spot.getY() + 0.35, spot.getZ() + 0.5,
                4, 0.2, 0.15, 0.2, 0.02);
    }
}
