package net.tiew.operationWild.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.tiew.operationWild.entity.platform.OWPlatformPhysics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class PlatformPlayerMovementMixin {

    @Shadow
    public ServerPlayer player;

    @ModifyConstant(method = "handleMovePlayer", constant = @Constant(doubleValue = 0.0625))
    private double ow$relaxPlatformMovedWrongly(double original) {
        if (this.player != null && OWPlatformPhysics.hasRecentContact(this.player)) return Double.MAX_VALUE;
        return original;
    }

    @Inject(method = "noBlocksAround", at = @At("HEAD"), cancellable = true)
    private void ow$platformCountsAsGround(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (OWPlatformPhysics.hasRecentContact(entity)) cir.setReturnValue(false);
    }
}
