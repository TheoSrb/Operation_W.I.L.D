package net.tiew.operationWild.mixin;

import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;
import net.tiew.operationWild.entity.platform.OWPlatformSpace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelTicks.class)
public abstract class LevelTicksPlatformMixin<T> {

    @Inject(method = "schedule", at = @At("HEAD"), cancellable = true)
    private void ow$scheduleOnPlatform(ScheduledTick<T> tick, CallbackInfo ci) {
        if (!OWPlatformSpace.isVoidY(tick.pos().getY())) return;
        OWPlatformSpace.scheduleTick(tick);
        ci.cancel();
    }
}
