package net.tiew.operationWild.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.tiew.operationWild.entity.client.render.misc.ChameleonCamouflage;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class MimicryInventoryPreviewMixin {

    @Inject(method = "renderEntityInInventory(Lnet/minecraft/client/gui/GuiGraphics;FFFLorg/joml/Vector3f;Lorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/world/entity/LivingEntity;)V", at = @At("HEAD"))
    private static void enterPreview(GuiGraphics guiGraphics, float x, float y, float scale, Vector3f translate, Quaternionf pose,
                                     Quaternionf cameraOrientation, LivingEntity entity, CallbackInfo ci) {
        ChameleonCamouflage.setInGui(true);
    }

    @Inject(method = "renderEntityInInventory(Lnet/minecraft/client/gui/GuiGraphics;FFFLorg/joml/Vector3f;Lorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/world/entity/LivingEntity;)V", at = @At("RETURN"))
    private static void leavePreview(GuiGraphics guiGraphics, float x, float y, float scale, Vector3f translate, Quaternionf pose,
                                     Quaternionf cameraOrientation, LivingEntity entity, CallbackInfo ci) {
        ChameleonCamouflage.setInGui(false);
    }
}
