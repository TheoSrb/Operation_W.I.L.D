package net.tiew.operationWild.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.MayaBlowpipeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {

    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void rotateBlowpipe(LivingEntity entity, ItemStack itemStack, ItemDisplayContext transformType,
                                HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, int light, CallbackInfo ci) {

        if (itemStack.getItem() == OWItems.MAYA_BLOWPIPE.get() &&
                Minecraft.getInstance().player != null &&
                Minecraft.getInstance().player.isUsingItem() &&
                Minecraft.getInstance().player.getUseItem() == itemStack) {

            float chargeProgress = MayaBlowpipeItem.getChargeProgress(itemStack, Minecraft.getInstance().player);
            double yTranslate = 1.0D - (chargeProgress * 0.25D);
            double zTranslate = 0.9D - (chargeProgress * 0.125D);

            poseStack.translate(0, -0.6f, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(90));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-70));
            poseStack.mulPose(Axis.XP.rotationDegrees(-20));
            poseStack.translate(-0.35D, yTranslate, zTranslate);
        }
    }
}