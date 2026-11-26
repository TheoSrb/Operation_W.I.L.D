package net.tiew.operationWild.mixin;

import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.tiew.operationWild.entity.OWSemiFlyingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Une monture semi-volante tient l'air pour de bon : sans ce garde, le serveur la prenait pour un
 * véhicule qui flotte et expulsait son cavalier au bout de 80 ticks (« Flying is not enabled »)
 * dès que {@code allow-flight} est désactivé.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class FlyingVehicleFloatMixin {

    @Shadow
    public ServerPlayer player;

    @Shadow
    private boolean clientVehicleIsFloating;

    @Shadow
    private int aboveGroundVehicleTickCount;

    @Inject(method = "handleMoveVehicle", at = @At("RETURN"))
    private void ow$allowSemiFlyingVehicle(ServerboundMoveVehiclePacket packet, CallbackInfo ci) {
        if (this.player != null && this.player.getRootVehicle() instanceof OWSemiFlyingEntity flyer && flyer.isAirborneFlight()) {
            this.clientVehicleIsFloating = false;
            this.aboveGroundVehicleTickCount = 0;
        }
    }
}
