package net.tiew.operationWild.entity.utils;
import net.minecraft.world.item.Item;

public interface IOWRideable {
    float vehicleRunSpeedMultiplier();
    float vehicleWalkSpeedMultiplier();
    float vehicleComboSpeedMultiplier();
    float vehicleWaterSpeedDivider();
    boolean canIncreasesSpeedDuringSprint();
    Item acceptSaddle();
    float getMaxVitalEnergy();
    float getVitalEnergyRecuperation();
}
