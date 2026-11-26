package net.tiew.operationWild.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.entity.animals.terrestrial.ChameleonEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MimicrySprintParticleMixin {

    @Inject(method = "canSpawnSprintParticle()Z", at = @At("HEAD"), cancellable = true)
    private void noSprintDustWhileCamouflaged(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player && ChameleonEntity.getMimicking(player) != null) cir.setReturnValue(false);
    }
}
