package net.tiew.operationWild.entity.goals.kodiak;

import com.google.common.base.Enums;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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

    public KodiakCatchFishGoal(KodiakEntity kodiak, float attractionFrequencyMultiplier, Runnable action) {
        this.kodiak = kodiak;
        this.attractionFrequencyMultiplier = attractionFrequencyMultiplier;
        this.action = action;

        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public void start() {
        super.start();
        this.fishArrived = false;

        float angle = Mth.nextFloat(kodiak.getRandom(), 0.0F, 360.0F);
        float f = angle * ((float)Math.PI / 180F);
        float f1 = Mth.sin(f);
        float f2 = Mth.cos(f);

        float distance = Mth.nextFloat(kodiak.getRandom(), 8.0F, 15.0F);

        double startX = kodiak.getX() + (double)(f1 * distance);
        double startY = kodiak.getY() + 0.5;
        double startZ = kodiak.getZ() + (double)(f2 * distance);

        this.fishPosition = new Vec3(startX, startY, startZ);

        double dirX = kodiak.getX() - startX;
        double dirY = (kodiak.getY() + 0.5) - startY;
        double dirZ = kodiak.getZ() - startZ;

        double dist = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        double speed = OWUtils.generateRandomInterval(0.075, 0.1);

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

        if (!this.fishArrived) {
            ServerLevel serverLevel = (ServerLevel) kodiak.level();

            if (kodiak.tickCount % 30 == 0) {
                kodiak.level().playSound(null, fishPosition.x, fishPosition.y, fishPosition.z,
                        net.minecraft.sounds.SoundEvents.FISH_SWIM,
                        net.minecraft.sounds.SoundSource.NEUTRAL,
                        0.5F, 1.0F);
            }

            serverLevel.sendParticles(ParticleTypes.FISHING, fishPosition.x, fishPosition.y - 0.5, fishPosition.z, 10, fishVelocity.x, fishVelocity.y, fishVelocity.z, 0.02);

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
        boolean isValidBiome = kodiak.level().getBiome(posBelow).is(Biomes.RIVER) || kodiak.level().getBiome(posBelow).is(OWBiomes.REDWOOD_FOREST_BIOME);

        return kodiak.getRandom().nextInt((int) (200 / attractionFrequencyMultiplier)) == 0 &&
                (kodiak.isInWater() || isWaterBelow) && isValidBiome && kodiak.getTarget() == null && !kodiak.isNapping() && !kodiak.isRolling() && !kodiak.isTame() && kodiak.getFoodPick().isEmpty();
    }

    @Override
    public boolean canContinueToUse() {
        BlockPos posBelow = kodiak.blockPosition().below();
        boolean isWaterBelow = kodiak.level().getBlockState(posBelow).is(Blocks.WATER);
        boolean isValidBiome = kodiak.level().getBiome(posBelow).is(Biomes.RIVER) || kodiak.level().getBiome(posBelow).is(OWBiomes.REDWOOD_FOREST_BIOME);
        return !this.fishArrived && (kodiak.isInWater() || isWaterBelow) && isValidBiome;
    }
}