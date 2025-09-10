package net.tiew.operationWild.entity;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public interface OWTameImplementation {
    float vehicleRunSpeedMultiplier();
    float vehicleWalkSpeedMultiplier();
    float vehicleComboSpeedMultiplier();
    float vehicleWaterSpeedDivider();
    boolean canIncreasesSpeedDuringSprint();
    Item acceptSaddle();
    List<Class<?>> getEntityType();
    ResourceLocation getTamingAdvancement();
    float getMaxVitalEnergy();
    float getVitalEnergyRecuperation();
}
