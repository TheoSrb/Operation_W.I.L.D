package net.tiew.operationWild.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.platform.OWPlatformSpace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerPlatformReachMixin {

    @Inject(method = "canInteractWithBlock", at = @At("HEAD"), cancellable = true)
    private void ow$reachPlatformBlock(BlockPos pos, double distance, CallbackInfoReturnable<Boolean> cir) {
        if (!OWPlatformSpace.isVoidY(pos.getY())) return;
        Player player = (Player) (Object) this;
        Vec3 world = OWPlatformSpace.toWorld(player.level(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        if (world == null) {
            cir.setReturnValue(false);
            return;
        }
        double range = player.blockInteractionRange() + distance + 1.0;
        cir.setReturnValue(player.getEyePosition().distanceToSqr(world) < range * range);
    }
}
