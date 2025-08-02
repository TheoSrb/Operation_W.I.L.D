package net.tiew.operationWild.utils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;

import java.util.Random;

public class OWDamageSources {

    public static Random random = new Random();

    public static DamageSource createWaterPressureDamage(ServerLevel level) {
        return new DamageSource(
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DamageTypes.GENERIC)
        ) {
            @Override
            public Component getLocalizedDeathMessage(LivingEntity entity) {
                Component[] messages = {
                        Component.translatable("death.attack.water_pressure", entity.getDisplayName()),
                        Component.translatable("death.attack.water_pressure2", entity.getDisplayName())
                };
                return messages[random.nextInt(messages.length)];
            }
        };
    }

}
