package net.tiew.operationWild.mixin;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.entity.animals.terrestrial.ChameleonEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerModel.class, priority = 2000)
public class MimicryPlayerPoseMixin {

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void freezeDuringMimicry(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                                     float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof Player player) || ChameleonEntity.getMimicking(player) == null) return;

        PlayerModel<?> model = (PlayerModel<?>) (Object) this;
        for (ModelPart part : new ModelPart[]{model.head, model.rightArm, model.leftArm, model.rightLeg, model.leftLeg}) {
            part.xRot = 0f;
            part.yRot = 0f;
            part.zRot = 0f;
        }
        model.hat.copyFrom(model.head);
        model.jacket.copyFrom(model.body);
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightPants.copyFrom(model.rightLeg);
        model.leftPants.copyFrom(model.leftLeg);
    }
}
