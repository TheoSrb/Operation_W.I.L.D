package net.tiew.operationWild.entity.goals.red_panda;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity;

import java.util.EnumSet;

public class RedPandaSweetLureGoal extends Goal {

    private static final double EAT_RANGE_SQR = 1.3 * 1.3;
    private static final double FINAL_APPROACH_SQR = 5.0 * 5.0;
    private static final double CREEP_RANGE_SQR = 2.5 * 2.5;
    private static final int CRUMB_INTERVAL = 8;

    private static final int HOP_RADIUS = 10;
    private static final int HOP_Y = 4;
    private static final double HOP_ANGLE = Math.PI / 3.0;
    private static final int PAUSE_MIN = 20;
    private static final int PAUSE_MAX = 60;
    private static final int GLANCE_INTERVAL = 25;

    private static final int BITE_INTERVAL = 70;
    private static final int HONEY_LICKS = 5;
    private static final double SHARE_RADIUS = 16.0;
    private static final int SHARE_INTERVAL = 40;

    private static final int MAX_STAY_TICKS = 2400;
    private static final int STUCK_TIMEOUT = 900;

    private final RedPandaEntity panda;
    private final double speedModifier;

    private BlockPos lure;
    private int stayTicks;
    private int travelTicks;
    private int pauseTicks;
    private int biteTicks;
    private int licks;

    public RedPandaSweetLureGoal(RedPandaEntity panda, double speedModifier) {
        this.panda = panda;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!isAvailable()) return false;

        lure = panda.getSweetLure();
        if (lure == null) return false;

        if (!isSweet(lure)) {
            panda.forgetSweetLure();
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!isAvailable() || lure == null) return false;
        if (stayTicks > MAX_STAY_TICKS || travelTicks > STUCK_TIMEOUT) return false;
        return isSweet(lure);
    }

    @Override
    public void start() {
        stayTicks = 0;
        travelTicks = 0;
        pauseTicks = 0;
        biteTicks = 0;
        licks = 0;
    }

    @Override
    public void stop() {
        if (lure == null || !isSweet(lure)) panda.forgetSweetLure();

        panda.setMealTimer(0);
        panda.getNavigation().stop();
        lure = null;
        stayTicks = 0;
        travelTicks = 0;
        licks = 0;
    }

    @Override
    public void tick() {
        stayTicks++;

        Vec3 target = Vec3.atCenterOf(lure);
        if (stayTicks % GLANCE_INTERVAL == 0) {
            panda.getLookControl().setLookAt(target.x, target.y, target.z);
        }
        if (stayTicks % SHARE_INTERVAL == 0) shareLure();

        if (panda.distanceToSqr(target) > EAT_RANGE_SQR) {
            drift(target);
            return;
        }

        travelTicks = 0;
        panda.getNavigation().stop();
        panda.getLookControl().setLookAt(target.x, target.y, target.z);
        if (panda.onGround()) {
            panda.setDeltaMovement(panda.getDeltaMovement().multiply(0.0, 1.0, 0.0));
        }

        if (panda.isEatingMeal()) {
            if (panda.getMealTimer() % CRUMB_INTERVAL == 0) spawnCrumbs();
            return;
        }

        if (++biteTicks < BITE_INTERVAL) return;
        biteTicks = 0;
        bite();
    }

    private void spawnCrumbs() {
        if (!(panda.level() instanceof ServerLevel serverLevel)) return;

        ItemStack crumbs = lureStack();
        if (crumbs.isEmpty()) return;

        Vec3 mouth = panda.position().add(
                panda.getLookAngle().x * 0.35, panda.getBbHeight() * 0.72, panda.getLookAngle().z * 0.35);

        serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, crumbs),
                mouth.x, mouth.y, mouth.z, 3, 0.08, 0.06, 0.08, 0.03);
    }

    private ItemStack lureStack() {
        return new ItemStack(panda.level().getBlockState(lure).getBlock());
    }

    private void drift(Vec3 target) {
        travelTicks++;

        if (panda.distanceToSqr(target) <= FINAL_APPROACH_SQR) {
            if (!panda.getNavigation().isDone()) return;

            if (panda.distanceToSqr(target) > CREEP_RANGE_SQR) {
                panda.getNavigation().moveTo(target.x, target.y, target.z, speedModifier);
            } else {
                panda.getMoveControl().setWantedPosition(target.x, target.y, target.z, speedModifier);
            }
            return;
        }

        if (pauseTicks > 0) {
            pauseTicks--;
            return;
        }
        if (!panda.getNavigation().isDone()) return;

        Vec3 hop = DefaultRandomPos.getPosTowards(panda, HOP_RADIUS, HOP_Y, target, HOP_ANGLE);
        if (hop == null) {
            pauseTicks = PAUSE_MIN;
            return;
        }

        panda.getNavigation().moveTo(hop.x, hop.y, hop.z, speedModifier);
        pauseTicks = PAUSE_MIN + panda.getRandom().nextInt(PAUSE_MAX - PAUSE_MIN);
    }

    private void bite() {
        BlockState state = panda.level().getBlockState(lure);

        panda.setMealTimer(RedPandaEntity.MEAL_TICKS);
        panda.level().playSound(null, lure, SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.8f,
                (float) OWUtils.generateRandomInterval(1.15, 1.45));

        if (panda.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    lure.getX() + 0.5, lure.getY() + 0.9, lure.getZ() + 0.5,
                    3, 0.25, 0.15, 0.25, 0.02);
        }

        if (!panda.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return;

        if (state.is(Blocks.CAKE)) {
            int bites = state.getValue(CakeBlock.BITES);
            if (bites >= CakeBlock.MAX_BITES) panda.level().removeBlock(lure, false);
            else panda.level().setBlockAndUpdate(lure, state.setValue(CakeBlock.BITES, bites + 1));
            return;
        }

        if (++licks >= HONEY_LICKS) panda.level().removeBlock(lure, false);
    }

    private void shareLure() {
        if (lure == null) return;

        AABB around = panda.getBoundingBox().inflate(SHARE_RADIUS);
        for (RedPandaEntity other : panda.level().getEntitiesOfClass(RedPandaEntity.class, around)) {
            if (other == panda || other.getSweetLure() != null) continue;
            other.noticeSweetLure(lure);
        }
    }

    private boolean isSweet(BlockPos pos) {
        return panda.level().getBlockState(pos).is(OWTags.Blocks.RED_PANDA_SWEETS);
    }

    private boolean isAvailable() {
        return !panda.isTame()
                && !panda.isBaby()
                && panda.isAlive()
                && !panda.isSitting()
                && !panda.isSleeping()
                && !panda.isNapping()
                && !panda.isInWater()
                && !panda.isInFight()
                && !panda.isPassenger()
                && !panda.isVehicle()
                && !panda.isAlerted()
                && !panda.isIntimidating()
                && !panda.isTreeClimbing()
                && !panda.isClimbing()
                && panda.getTarget() == null;
    }
}
