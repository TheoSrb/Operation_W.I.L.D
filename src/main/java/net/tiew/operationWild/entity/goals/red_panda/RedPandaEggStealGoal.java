package net.tiew.operationWild.entity.goals.red_panda;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class RedPandaEggStealGoal extends Goal {

    private static final double SEARCH_RADIUS = 10.0;
    private static final double REACH_SQR = 1.05 * 1.05;
    private static final int TRAVEL_TIMEOUT = 220;
    private static final int TRY_CHANCE = 40;
    private static final int COOLDOWN_TICKS = 500;
    private static final int MISSED_COOLDOWN_TICKS = 80;
    private static final double CREEP_RANGE_SQR = 2.5 * 2.5;

    private static final int SWALLOW_TICK = 32;
    private static final float EGG_HEAL = 2.0f;
    private static final int NOURISH_PARTICLES_MIN = 5;
    private static final int NOURISH_PARTICLES_SPAN = 14;

    private final RedPandaEntity panda;
    private final double speedModifier;

    private ItemEntity egg;
    private ItemStack meal = ItemStack.EMPTY;
    private int travelTicks;
    private int eatTicks;
    private boolean swallowed;
    private int cooldown;

    public RedPandaEggStealGoal(RedPandaEntity panda, double speedModifier) {
        this.panda = panda;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (!isAvailable()) return false;
        if (panda.getRandom().nextInt(TRY_CHANCE) != 0) return false;

        egg = findEgg();
        return egg != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (!isAvailable()) return false;
        if (eatTicks > 0) return eatTicks < RedPandaEntity.MEAL_TICKS;
        if (egg == null || !egg.isAlive() || egg.getItem().isEmpty()) return false;
        return travelTicks <= TRAVEL_TIMEOUT;
    }

    @Override
    public void start() {
        travelTicks = 0;
        eatTicks = 0;
        swallowed = false;
        if (egg != null) {
            panda.getNavigation().moveTo(egg.getX(), egg.getY(), egg.getZ(), speedModifier);
        }
    }

    @Override
    public void stop() {
        boolean ate = eatTicks > 0;

        panda.setMealTimer(0);
        panda.getNavigation().stop();
        egg = null;
        meal = ItemStack.EMPTY;
        travelTicks = 0;
        eatTicks = 0;
        swallowed = false;

        int base = ate ? COOLDOWN_TICKS : MISSED_COOLDOWN_TICKS;
        cooldown = base + panda.getRandom().nextInt(base);
    }

    @Override
    public void tick() {
        if (eatTicks == 0) {
            if (egg == null) return;
            panda.getLookControl().setLookAt(egg.getX(), egg.getY() + 0.2, egg.getZ());

            if (panda.distanceToSqr(egg) > REACH_SQR) {
                travelTicks++;
                if (!panda.getNavigation().isDone()) return;

                if (panda.distanceToSqr(egg) > CREEP_RANGE_SQR) {
                    panda.getNavigation().moveTo(egg.getX(), egg.getY(), egg.getZ(), speedModifier);
                } else {
                    panda.getMoveControl().setWantedPosition(
                            egg.getX(), egg.getY(), egg.getZ(), speedModifier);
                }
                return;
            }
            beginMeal();
        }

        panda.getNavigation().stop();
        if (panda.onGround()) {
            panda.setDeltaMovement(panda.getDeltaMovement().multiply(0.0, 1.0, 0.0));
        }

        eatTicks++;

        if (eatTicks % 8 == 4) spitShell();

        if (!swallowed && eatTicks >= SWALLOW_TICK) {
            swallowed = true;
            swallow();
        }
    }

    private void beginMeal() {
        meal = egg.getItem().copyWithCount(1);
        panda.setMealTimer(RedPandaEntity.MEAL_TICKS);
        panda.level().playSound(null, panda.getX(), panda.getY(), panda.getZ(),
                SoundEvents.FOX_SNIFF, SoundSource.NEUTRAL, 0.6f,
                (float) OWUtils.generateRandomInterval(1.2, 1.45));
    }

    private void swallow() {
        if (egg == null || !egg.isAlive()) return;

        ItemStack stack = egg.getItem();
        if (stack.isEmpty()) return;

        stack.shrink(1);
        if (stack.isEmpty()) egg.discard();
        else egg.setItem(stack);

        panda.heal(EGG_HEAL);

        panda.level().playSound(null, panda.getX(), panda.getY(), panda.getZ(),
                SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.8f,
                (float) OWUtils.generateRandomInterval(1.15, 1.4));
        panda.level().playSound(null, panda.getX(), panda.getY(), panda.getZ(),
                SoundEvents.PLAYER_BURP, SoundSource.NEUTRAL, 0.35f,
                (float) OWUtils.generateRandomInterval(1.4, 1.7));

        float gained = panda.nourishWithEgg(RedPandaEntity.eggNourishmentGain(meal));
        if (gained > 0f) showNourishment(gained);
    }

    private void showNourishment(float gained) {
        float richness = Mth.clamp(gained / RedPandaEntity.EGG_GAIN_PRIZED, 0f, 1f);

        panda.level().playSound(null, panda.getX(), panda.getY(), panda.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL,
                0.35f + 0.45f * richness, 1.35f + 0.25f * richness);

        if (!(panda.level() instanceof ServerLevel serverLevel)) return;

        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                panda.getX(), panda.getY() + panda.getBbHeight() * 0.9, panda.getZ(),
                NOURISH_PARTICLES_MIN + Math.round(NOURISH_PARTICLES_SPAN * richness),
                0.3, 0.25, 0.3, 0.02);
    }

    private void spitShell() {
        if (!(panda.level() instanceof ServerLevel serverLevel)) return;

        ItemStack stack = meal;
        if (stack.isEmpty()) return;

        Vec3 mouth = panda.position().add(
                panda.getLookAngle().x * 0.35, panda.getBbHeight() * 0.72, panda.getLookAngle().z * 0.35);

        serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack),
                mouth.x, mouth.y, mouth.z, 3, 0.08, 0.06, 0.08, 0.03);
    }

    private boolean isAvailable() {
        return !panda.isTame()
                && !panda.isBaby()
                && panda.isAlive()
                && panda.onGround()
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

    private @Nullable ItemEntity findEgg() {
        List<ItemEntity> candidates = panda.level().getEntitiesOfClass(ItemEntity.class,
                panda.getBoundingBox().inflate(SEARCH_RADIUS),
                item -> item.isAlive()
                        && !item.hasPickUpDelay()
                        && item.getItem().is(OWTags.Items.RED_PANDA_STEALABLE_EGGS));

        return candidates.stream()
                .min(Comparator.comparingDouble(panda::distanceToSqr))
                .orElse(null);
    }
}
