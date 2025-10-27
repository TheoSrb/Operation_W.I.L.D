package net.tiew.operationWild.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.misc.Submarine;
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
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (player.getVehicle() instanceof OWEntity) {
                ci.cancel();
            }
            if (player.getVehicle() instanceof CrocodileEntity crocodile && crocodile.getGrabbedTarget() != null && crocodile.getGrabbedTarget() == player) {
                ci.cancel();
            }
        }
    }

    /**
     * @author Tiew_37
     * @reason X
     */
    @Inject(method = "renderHealthLevel", at = @At("HEAD"), cancellable = true)
    private void cancelPlayerHearts(GuiGraphics guiGraphics, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (player.getVehicle() instanceof Submarine) {
                ci.cancel();
            }
        }
    }

    /**
     * @author Tiew_37
     * @reason X
     */
    @Inject(method = "renderArmorLevel", at = @At("HEAD"), cancellable = true)
    private void cancelArmorLevel(GuiGraphics guiGraphics, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (player.getVehicle() instanceof CrocodileEntity crocodile && crocodile.getGrabbedTarget() != null && crocodile.getGrabbedTarget() == player) return;
            if (player.getVehicle() instanceof Submarine) {
                ci.cancel();
            }
        }
    }

    /**
     * @author Tiew_37
     * @reason X
     */
    @Inject(method = "renderFoodLevel", at = @At("HEAD"), cancellable = true)
    private void cancelFoodLevel(GuiGraphics guiGraphics, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (player.getVehicle() instanceof OWEntity) {
                ci.cancel();
            }
        }
    }

    /**
     * @author Tiew_37
     * @reason X
     */
    @Inject(method = "renderAirLevel", at = @At("HEAD"), cancellable = true)
    private void modifyAirLevelPosition(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (Minecraft.getInstance().player != null) {
            Entity vehicle = Minecraft.getInstance().player.getVehicle();
            if (vehicle instanceof Submarine) {
                ci.cancel();
            } else if (vehicle instanceof OWEntity) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, -20, 0);
            }
        }
    }

    @Inject(method = "renderAirLevel", at = @At("RETURN"))
    private void restoreAirLevelPosition(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getVehicle() instanceof OWEntity) {
            guiGraphics.pose().popPose();
        }
    }

    /**
     * @author Tiew_37
     * @reason X
     */
    @Inject(method = "renderJumpMeter", at = @At("HEAD"), cancellable = true)
    private void cancelJumpBar(PlayerRideableJumping rideableJumping, GuiGraphics graphics, int i, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (player.getVehicle() instanceof Submarine) {
                ci.cancel();
            }
        }
    }

    /**
     * @author Tiew_37
     * @reason X
     */
    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void cancelExperienceBar(GuiGraphics graphics, int i, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (player.getVehicle() instanceof Submarine) {
                ci.cancel();
            }
        }
    }

    /**
     * @author Tiew_37
     * @reason X
     */
    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void cancelSlots(GuiGraphics p_333625_, DeltaTracker p_344796_, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (player.getVehicle() instanceof Submarine) {
                ci.cancel();
            }
        }
    }

    /**
     * @author Tiew_37
     * @reason X
     */
    @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true)
    private void renderCustomHearts(GuiGraphics guiGraphics, Player player, int x, int y, int height,
                                    int offsetHeartIndex, float maxHealth, int currentHealth,
                                    int displayHealth, int absorptionAmount, boolean renderHighlight,
                                    CallbackInfo ci) {
        if (player.hasEffect(OWEffects.VENOM_EFFECT.getDelegate()) || player.hasEffect(OWEffects.WATER_PRESSURE_EFFECT.getDelegate())) {
            ci.cancel();
        }
    }

}
