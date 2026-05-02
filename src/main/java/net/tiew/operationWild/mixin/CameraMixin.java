package net.tiew.operationWild.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Camera.class)
public class CameraMixin {

    @ModifyConstant(method = "setup", constant = @Constant(floatValue = 4.0F))
    private float modifyCameraDistance(float original) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getVehicle() instanceof OWEntity) {
            return chooseFOVAnimal(mc.player.getVehicle());
        }
        return original;
    }

    private float chooseFOVAnimal(Entity rider) {
        return switch (rider) {
            case OrcaEntity e -> 8.0F;
            default -> 4.0F;
        };
    }
}