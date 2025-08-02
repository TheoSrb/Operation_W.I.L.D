package net.tiew.operationWild.item;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;
import net.tiew.operationWild.utils.OWTags;

public class OWToolsTiers {
    public static final Tier JADE = new SimpleTier(
            OWTags.Blocks.INCORRECT_FOR_JADE_TOOL,
            438,
            5.5f,
            2.0f,
            16,
            () -> Ingredient.of(OWItems.JADE.get())
    );

    public static final Tier RUBY = new SimpleTier(
            OWTags.Blocks.INCORRECT_FOR_RUBY_TOOL,
            1734,
            8f,
            3.5f,
            12,
            () -> Ingredient.of(OWItems.RUBY.get())
    );

    public static final Tier PRIMITIVE = new SimpleTier(
            OWTags.Blocks.INCORRECT_FOR_PRIMITIVE_TOOL,
            211,
            3f,
            0f,
            6,
            () -> Ingredient.of(OWItems.TIGER_FUR.get())
    );
}