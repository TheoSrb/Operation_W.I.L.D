package net.tiew.operationWild.item;

import net.minecraft.world.food.FoodProperties;

public class OWFoodsProperties {
    public static final FoodProperties RAW_TIGER = new FoodProperties.Builder().nutrition(3).saturationModifier(0.4f).build();
    public static final FoodProperties COOKED_TIGER = new FoodProperties.Builder().nutrition(9).saturationModifier(0.9f).build();

    public static final FoodProperties RAW_KODIAK = new FoodProperties.Builder().nutrition(2).saturationModifier(0.2f).build();
    public static final FoodProperties COOKED_KODIAK = new FoodProperties.Builder().nutrition(10).saturationModifier(1f).build();

    public static final FoodProperties RAW_BOA = new FoodProperties.Builder().nutrition(2).saturationModifier(0.1f).build();
    public static final FoodProperties COOKED_BOA = new FoodProperties.Builder().nutrition(6).saturationModifier(0.6f).build();

    public static final FoodProperties RAW_PEACOCK = new FoodProperties.Builder().nutrition(3).saturationModifier(0.3f).build();
    public static final FoodProperties COOKED_PEACOCK = new FoodProperties.Builder().nutrition(5).saturationModifier(1.2f).build();

    public static final FoodProperties SAVAGE_BERRIES = new FoodProperties.Builder().nutrition(2).saturationModifier(0.15f).build();
}
