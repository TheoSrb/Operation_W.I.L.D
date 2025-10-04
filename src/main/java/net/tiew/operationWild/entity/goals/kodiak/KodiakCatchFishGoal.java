package net.tiew.operationWild.entity.goals.kodiak;

import com.google.common.base.Enums;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.worldgen.biome.OWBiomes;

import java.util.EnumSet;

public class KodiakCatchFishGoal extends Goal {

    private final KodiakEntity kodiak;
    private final float attractionFrequencyMultiplier;
    private final Runnable action;

    private Vec3 fishPosition;
    private Vec3 fishVelocity;
    private boolean fishArrived = false;
    private int cooldownTicks = 0;

    private static final int COOLDOWN_DURATION = 400;
    private static final int MAX_ATTEMPTS = 20;

    public KodiakCatchFishGoal(KodiakEntity kodiak, float attractionFrequencyMultiplier, Runnable action) {
        this.kodiak = kodiak;
        this.attractionFrequencyMultiplier = attractionFrequencyMultiplier;
        this.action = action;

        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    private Vec3 findValidWaterPosition() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            float angle = Mth.nextFloat(kodiak.getRandom(), 0.0F, 360.0F);
            float f = angle * ((float)Math.PI / 180F);
            float f1 = Mth.sin(f);
            float f2 = Mth.cos(f);

            float distance = Mth.nextFloat(kodiak.getRandom(), 6.0F, 12.0F);

            double startX = kodiak.getX() + (double)(f1 * distance);
            double startY = kodiak.getY() + 0.5;
            double startZ = kodiak.getZ() + (double)(f2 * distance);

            BlockPos checkPos = new BlockPos((int)startX, (int)startY, (int)startZ);

            if (kodiak.level().getFluidState(checkPos).is(FluidTags.WATER)) {
                return new Vec3(startX, startY, startZ);
            }
        }

        return null;
    }

    @Override
    public void start() {
        super.start();
        this.fishArrived = false;

        Vec3 validPosition = findValidWaterPosition();

        if (validPosition == null) {
            return;
        }

        this.fishPosition = validPosition;

        double dirX = kodiak.getX() - fishPosition.x;
        double dirY = (kodiak.getY() + 0.5) - fishPosition.y;
        double dirZ = kodiak.getZ() - fishPosition.z;

        double dist = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        double speed = OWUtils.generateRandomInterval(0.1, 0.125);

        this.fishVelocity = new Vec3(
                (dirX / dist) * speed,
                (dirY / dist) * speed,
                (dirZ / dist) * speed
        );
    }

    @Override
    public void tick() {
        super.tick();

        if (this.cooldownTicks > 0) {
            this.cooldownTicks--;
            return;
        }

        if (!this.fishArrived && this.fishPosition != null) {
            ServerLevel serverLevel = (ServerLevel) kodiak.level();

            if (kodiak.tickCount % 30 == 0) {
                kodiak.level().playSound(null, fishPosition.x, fishPosition.y, fishPosition.z,
                        net.minecraft.sounds.SoundEvents.FISH_SWIM,
                        net.minecraft.sounds.SoundSource.NEUTRAL,
                        0.5F, 1.0F);
            }

            BlockPos particlePos = new BlockPos((int)fishPosition.x, (int)fishPosition.y, (int)fishPosition.z);
            if (kodiak.level().getFluidState(particlePos).is(FluidTags.WATER)) {
                serverLevel.sendParticles(ParticleTypes.FISHING, fishPosition.x, fishPosition.y - 0.15, fishPosition.z, 10, fishVelocity.x, fishVelocity.y, fishVelocity.z, 0.02);
            }

            this.fishPosition = this.fishPosition.add(this.fishVelocity);

            double distanceToKodiak = this.fishPosition.distanceTo(new Vec3(kodiak.getX(), kodiak.getY() + 0.5, kodiak.getZ()));

            if (distanceToKodiak < 2) {
                this.fishArrived = true;
                action.run();
                this.cooldownTicks = COOLDOWN_DURATION;
            } else if (distanceToKodiak < 10) {
                this.kodiak.setLookAt(this.fishPosition.x, this.fishPosition.y, this.fishPosition.z);
            }
        }
    }

    @Override
    public boolean canUse() {
        if (this.cooldownTicks > 0) {
            return false;
        }

        BlockPos posBelow = kodiak.blockPosition().below();
        boolean isWaterBelow = kodiak.level().getBlockState(posBelow).is(Blocks.WATER);

        if (kodiak.getRandom().nextInt((int) (200 / attractionFrequencyMultiplier)) == 0 &&
                (kodiak.isInWater() || isWaterBelow) && !kodiak.isTame() && kodiak.getTarget() == null
                && !kodiak.isNapping() && !kodiak.isRolling() && !kodiak.isTame() && kodiak.getFoodPick().isEmpty() && !kodiak.isCatchingSalmon() && !kodiak.isRubs()) {

            return findValidWaterPosition() != null;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.fishPosition == null) {
            return false;
        }

        BlockPos posBelow = kodiak.blockPosition().below();
        boolean isWaterBelow = kodiak.level().getBlockState(posBelow).is(Blocks.WATER);
        return !this.fishArrived && (kodiak.isInWater() || isWaterBelow) && !kodiak.isCatchingSalmon() && !kodiak.isTame() && !kodiak.isRubs();
    }
}