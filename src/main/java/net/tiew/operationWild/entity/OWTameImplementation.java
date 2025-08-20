package net.tiew.operationWild.entity;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public interface OWTameImplementation {
    float vehicleRunSpeedMultiplier();
    float vehicleWalkSpeedMultiplier();
    float vehicleComboSpeedMultiplier();
    boolean canIncreasesSpeedDuringSprint();
    Item acceptSaddle();
    List<Class<?>> getEntityType();
    String getTamingAdvancement();
    float getMaxVitalEnergy();
    float getVitalEnergyRecuperation();
}
