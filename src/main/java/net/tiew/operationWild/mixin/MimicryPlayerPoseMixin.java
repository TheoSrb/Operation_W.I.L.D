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
    private void hideOuterLayersDuringMimicry(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                                     float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof Player player) || ChameleonEntity.getMimicking(player) == null) return;

        PlayerModel<?> model = (PlayerModel<?>) (Object) this;
        for (ModelPart outer : new ModelPart[]{model.hat, model.jacket, model.rightSleeve, model.leftSleeve, model.rightPants, model.leftPants}) {
            outer.visible = false;
        }
    }
}
