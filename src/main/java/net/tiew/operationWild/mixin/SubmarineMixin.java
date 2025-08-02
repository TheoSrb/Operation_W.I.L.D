package net.tiew.operationWild.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.tiew.operationWild.entity.custom.vehicle.Submarine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class SubmarineMixin {

    /**
     * @author Tiew_37
     * @reason X
     */
    @Inject(method = "renderVehicleHealth", at = @At("HEAD"), cancellable = true)
    private void cancelSubmarineHearts(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getVehicle() instanceof Submarine) {
            ci.cancel();
        }
    }

    /**
     * @author Tiew_37
     * @reason X
     */
    @Inject(method = "renderPlayerHealth", at = @At("HEAD"), cancellable = true)
    private void cancelPlayerHearts(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getVehicle() instanceof Submarine) {
            ci.cancel();
        }
    }

    /**
     * @author Tiew_37
     * @reason X
     */
    @Inject(method = "renderJumpMeter", at = @At("HEAD"), cancellable = true)
    private void cancelJumpBar(PlayerRideableJumping rideableJumping, GuiGraphics graphics, int i, CallbackInfo ci) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getVehicle() instanceof Submarine) {
            ci.cancel();
        }
    }

    /**
     * @author Tiew_37
     * @reason X
     */
    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void cancelExperienceBar(GuiGraphics graphics, int i, CallbackInfo ci) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getVehicle() instanceof Submarine) {
            ci.cancel();
        }
    }

    /**
     * @author Tiew_37
     * @reason X
     */
    @Inject(method = "renderHotbarAndDecorations", at = @At("HEAD"), cancellable = true)
    private void cancelSlots(GuiGraphics p_333625_, DeltaTracker p_344796_, CallbackInfo ci) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getVehicle() instanceof Submarine) {
            ci.cancel();
        }
    }


}
