package net.tiew.operationWild.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.entity.misc.SeaBugEntity;
import net.tiew.operationWild.item.OWItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public class PlayerModelRotationMixin {

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void rotateHead(LivingEntity p_103395_, float p_103396_, float p_103397_,
                            float p_103398_, float p_103399_, float p_103400_, CallbackInfo ci) {

        Minecraft mc = Minecraft.getInstance();
        PlayerModel<?> model = (PlayerModel<?>) (Object) this;

        if (mc.player != null && mc.player == p_103395_
                && mc.player.getVehicle() instanceof SeaBugEntity seaBug && !seaBug.isOff()) {
            model.head.xRot += (float) Math.toRadians(-seaBug.getLastPlayerPitch());
            model.hat.xRot += (float) Math.toRadians(-seaBug.getLastPlayerPitch());
        }

        if (!(p_103395_ instanceof Player renderedPlayer)) return;

        boolean holdingSeaBug = renderedPlayer.getMainHandItem().is(OWItems.SEABUG.get()) ||
                renderedPlayer.getOffhandItem().is(OWItems.SEABUG.get());
        boolean havingBlowpipeInHand = renderedPlayer.getMainHandItem().getItem() == OWItems.MAYA_BLOWPIPE.get()
                && renderedPlayer.isUsingItem();

        if (havingBlowpipeInHand) {
            model.rightArm.xRot = (float) Math.toRadians(-90);
            model.rightArm.yRot = (float) Math.toRadians(-20);
            model.rightSleeve.xRot = (float) Math.toRadians(-90);
            model.rightSleeve.yRot = (float) Math.toRadians(-20);
            model.leftArm.xRot = (float) Math.toRadians(-90);
            model.leftArm.yRot = (float) Math.toRadians(20);
            model.leftSleeve.xRot = (float) Math.toRadians(-90);
            model.leftSleeve.yRot = (float) Math.toRadians(20);
        }

        if (holdingSeaBug) {
            model.rightArm.yRot = 0; model.rightSleeve.yRot = 0;
            model.rightArm.zRot = 0; model.rightSleeve.zRot = 0;
            model.rightArm.xRot = (float) Math.toRadians(195);
            model.rightSleeve.xRot = (float) Math.toRadians(195);
            model.leftArm.yRot = 0; model.leftSleeve.yRot = 0;
            model.leftArm.zRot = 0; model.leftSleeve.zRot = 0;
            model.leftArm.xRot = (float) Math.toRadians(195);
            model.leftSleeve.xRot = (float) Math.toRadians(195);
            model.body.xRot = (float) Math.toRadians(15);
            model.leftLeg.z = 3.5f; model.leftPants.z = 3.5f;
            model.rightLeg.z = 3.5f; model.rightPants.z = 3.5f;
            model.head.y = 2.5f; model.hat.y = 2.5f;
            model.head.z = -1.5f; model.hat.z = -1.5f;
        } else {
            // Utilise renderedPlayer et non mc.player
            if (!renderedPlayer.isSteppingCarefully()) {
                model.head.y = 0.0f;
                model.hat.y = 0.0f;
                model.head.z = 0.0f;
                model.hat.z = 0.0f;
                model.body.xRot = 0.0f;
                model.leftLeg.z = 0.1f;
                model.leftPants.z = 0.1f;
                model.rightLeg.z = 0.1f;
                model.rightPants.z = 0.1f;
            }
        }
    }
}