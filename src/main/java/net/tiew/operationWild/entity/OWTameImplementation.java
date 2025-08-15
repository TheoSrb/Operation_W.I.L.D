package net.tiew.operationWild.entity;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public interface OWTameImplementation {
    float vehicleRunSpeedMultiplier();
    float vehicleWalkSpeedMultiplier();
    Item acceptSaddle();
    List<Class<?>> getEntityType();
    List<Object> getEntityDiet();
    String getTamingAdvancement();
    float getMaxVitalEnergy();
    float getVitalEnergyRecuperation();
}
