package net.tiew.operationWild.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.EventHooks;
import net.tiew.operationWild.entity.OWEntityRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {

    @Inject(method = "isValidPositionForMob", at = @At("HEAD"), cancellable = true)
    private static void allowJellyfishInWater(ServerLevel level, Mob mob, double distance, CallbackInfoReturnable<Boolean> cir) {
        if (mob.getType() == OWEntityRegistry.JELLYFISH.get()) {
            BlockPos pos = mob.blockPosition();
            boolean isValidWaterSpawn = level.getFluidState(pos).is(FluidTags.WATER) &&
                    level.getBlockState(pos.above()).is(Blocks.WATER);

            if (isValidWaterSpawn) {
                boolean validDistance = !(distance > (double)(mob.getType().getCategory().getDespawnDistance() * mob.getType().getCategory().getDespawnDistance()) && mob.removeWhenFarAway(distance));
                cir.setReturnValue(validDistance && EventHooks.checkSpawnPosition(mob, level, MobSpawnType.NATURAL));
            }
        }
    }
}