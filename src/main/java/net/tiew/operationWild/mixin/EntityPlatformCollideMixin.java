package net.tiew.operationWild.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.platform.OWPlatformPhysics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityPlatformCollideMixin {

    @Inject(method = "collide", at = @At("RETURN"), cancellable = true)
    private void ow$collideWithPlatforms(Vec3 vec, CallbackInfoReturnable<Vec3> cir) {
        Vec3 result = cir.getReturnValue();
        Vec3 adjusted = OWPlatformPhysics.collide((Entity) (Object) this, result);
        if (adjusted != result) cir.setReturnValue(adjusted);
    }
}
