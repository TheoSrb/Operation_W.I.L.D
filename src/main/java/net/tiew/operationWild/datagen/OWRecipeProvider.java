package net.tiew.operationWild.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.block.OWBlocks;
import net.tiew.operationWild.item.OWItems;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OWRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public OWRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.CAMOUFLAGE_HELMET.get())
                .pattern("LLL")
                .pattern("T T")
                .pattern("   ")
                .define('L', ItemTags.LEAVES)
                .define('T', OWItems.TIGER_FUR.get()).unlockedBy(getHasName(OWItems.TIGER_FUR.get()), has(OWItems.TIGER_FUR.get())).unlockedBy("leaves", has(ItemTags.LEAVES)).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.CAMOUFLAGE_CHESTPLATE.get())
                .pattern("L L")
                .pattern("LLL")
                .pattern("TTT")
                .define('L', ItemTags.LEAVES)
                .define('T', OWItems.TIGER_FUR.get()).unlockedBy(getHasName(OWItems.TIGER_FUR.get()), has(OWItems.TIGER_FUR.get())).unlockedBy("leaves", has(ItemTags.LEAVES)).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.CAMOUFLAGE_LEGGINGS.get())
                .pattern("TTT")
                .pattern("L L")
                .pattern("L L")
                .define('L', ItemTags.LEAVES)
                .define('T', OWItems.TIGER_FUR.get()).unlockedBy(getHasName(OWItems.TIGER_FUR.get()), has(OWItems.TIGER_FUR.get())).unlockedBy("leaves", has(ItemTags.LEAVES)).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.CAMOUFLAGE_BOOTS.get())
                .pattern("T T")
                .pattern("L L")
                .pattern("   ")
                .define('L', ItemTags.LEAVES)
                .define('T', OWItems.TIGER_FUR.get()).unlockedBy(getHasName(OWItems.TIGER_FUR.get()), has(OWItems.TIGER_FUR.get())).unlockedBy("leaves", has(ItemTags.LEAVES)).save(recipeOutput);


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.PRIMITIVE_SPEAR.get())
                .pattern(" TF")
                .pattern(" ST")
                .pattern("S  ")
                .define('S', Items.STICK)
                .define('T', OWItems.TIGER_FUR.get())
                .define('F', OWItems.PREDATOR_TOOTH.get())
                .unlockedBy(getHasName(OWItems.TIGER_FUR.get()), has(OWItems.TIGER_FUR.get())).save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.REPTILIAN_DAGGER.get())
                .pattern("  F")
                .pattern("CF ")
                .pattern("CC ")
                .define('C', OWItems.CROCODILE_SCALE.get())
                .define('F', OWItems.PREDATOR_TOOTH.get())
                .unlockedBy(getHasName(OWItems.CROCODILE_SCALE.get()), has(OWItems.CROCODILE_SCALE.get())).save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.TRANQUILIZER_ARROW.get(), 2)
                .pattern("  T")
                .pattern(" A ")
                .pattern("   ")
                .define('T', OWItems.BOA_TONG.get())
                .define('A', Items.ARROW)
                .unlockedBy(getHasName(OWItems.BOA_TONG.get()), has(OWItems.BOA_TONG.get())).save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.VENOMOUS_TOOTH.get(), 1)
                .pattern(" V ")
                .pattern("VTV")
                .pattern(" V ")
                .define('T', OWItems.PREDATOR_TOOTH.get())
                .define('V', OWItems.VENOMOUS_GLANDS.get())
                .unlockedBy(getHasName(OWItems.PREDATOR_TOOTH.get()), has(OWItems.PREDATOR_TOOTH.get())).save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.VENOMOUS_ARROW.get(), 1)
                .pattern("  V")
                .pattern(" S ")
                .pattern("P  ")
                .define('P', OWItems.PEACOCK_FEATHER.get())
                .define('S', Items.STICK)
                .define('V', OWItems.VENOMOUS_TOOTH.get())
                .unlockedBy(getHasName(OWItems.PEACOCK_FEATHER.get()), has(OWItems.PEACOCK_FEATHER.get())).save(recipeOutput);




        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.JADE_SWORD.get())
                .pattern("  J")
                .pattern(" J ")
                .pattern("S  ")
                .define('S', Items.STICK)
                .define('J', OWItems.JADE.get())
                .unlockedBy(getHasName(OWItems.JADE.get()), has(OWItems.JADE.get())).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.JADE_PICKAXE.get())
                .pattern("JJJ")
                .pattern(" S ")
                .pattern(" S ")
                .define('S', Items.STICK)
                .define('J', OWItems.JADE.get())
                .unlockedBy(getHasName(OWItems.JADE.get()), has(OWItems.JADE.get())).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.JADE_AXE.get())
                .pattern("JJ ")
                .pattern("JS ")
                .pattern(" S ")
                .define('S', Items.STICK)
                .define('J', OWItems.JADE.get())
                .unlockedBy(getHasName(OWItems.JADE.get()), has(OWItems.JADE.get())).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.JADE_SHOVEL.get())
                .pattern(" J ")
                .pattern(" S ")
                .pattern(" S ")
                .define('S', Items.STICK)
                .define('J', OWItems.JADE.get())
                .unlockedBy(getHasName(OWItems.JADE.get()), has(OWItems.JADE.get())).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.JADE_HOE.get())
                .pattern("JJ ")
                .pattern(" S ")
                .pattern(" S ")
                .define('S', Items.STICK)
                .define('J', OWItems.JADE.get())
                .unlockedBy(getHasName(OWItems.JADE.get()), has(OWItems.JADE.get())).save(recipeOutput);


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.RUBY_SWORD.get())
                .pattern("  R")
                .pattern(" R ")
                .pattern("S  ")
                .define('S', Items.STICK)
                .define('R', OWItems.RUBY.get())
                .unlockedBy(getHasName(OWItems.RUBY.get()), has(OWItems.RUBY.get())).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.RUBY_PICKAXE.get())
                .pattern("RRR")
                .pattern(" S ")
                .pattern(" S ")
                .define('S', Items.STICK)
                .define('R', OWItems.RUBY.get())
                .unlockedBy(getHasName(OWItems.RUBY.get()), has(OWItems.RUBY.get())).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.RUBY_AXE.get())
                .pattern("RR ")
                .pattern("RS ")
                .pattern(" S ")
                .define('S', Items.STICK)
                .define('R', OWItems.RUBY.get())
                .unlockedBy(getHasName(OWItems.RUBY.get()), has(OWItems.RUBY.get())).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.RUBY_SHOVEL.get())
                .pattern(" R ")
                .pattern(" S ")
                .pattern(" S ")
                .define('S', Items.STICK)
                .define('R', OWItems.RUBY.get())
                .unlockedBy(getHasName(OWItems.RUBY.get()), has(OWItems.RUBY.get())).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.RUBY_HOE.get())
                .pattern("RR ")
                .pattern(" S ")
                .pattern(" S ")
                .define('S', Items.STICK)
                .define('R', OWItems.RUBY.get())
                .unlockedBy(getHasName(OWItems.RUBY.get()), has(OWItems.RUBY.get())).save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.WOODEN_STINGER.get())
                .pattern("   ")
                .pattern(" S ")
                .pattern("F  ")
                .define('S', Items.STICK)
                .define('F', Items.FEATHER)
                .unlockedBy(getHasName(Items.FEATHER), has(Items.FEATHER)).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.TRANQUILIZER_WOODEN_STINGER.get(), 2)
                .pattern("   ")
                .pattern(" T ")
                .pattern("W  ")
                .define('W', OWItems.WOODEN_STINGER.get())
                .define('T', OWItems.BOA_TONG.get())
                .unlockedBy(getHasName(OWItems.BOA_TONG.get()), has(OWItems.BOA_TONG.get())).save(recipeOutput);



        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.JADE_HELMET.get())
                .pattern("JJJ")
                .pattern("J J")
                .pattern("   ")
                .define('J', OWItems.JADE.get()).unlockedBy(getHasName(OWItems.JADE.get()), has(OWItems.JADE.get())).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.JADE_CHESTPLATE.get())
                .pattern("J J")
                .pattern("JJJ")
                .pattern("JJJ")
                .define('J', OWItems.JADE.get()).unlockedBy(getHasName(OWItems.JADE.get()), has(OWItems.JADE.get())).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.JADE_LEGGINGS.get())
                .pattern("JJJ")
                .pattern("J J")
                .pattern("J J")
                .define('J', OWItems.JADE.get()).unlockedBy(getHasName(OWItems.JADE.get()), has(OWItems.JADE.get())).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.JADE_BOOTS.get())
                .pattern("J J")
                .pattern("J J")
                .pattern("   ")
                .define('J', OWItems.JADE.get()).unlockedBy(getHasName(OWItems.JADE.get()), has(OWItems.JADE.get())).save(recipeOutput);


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Blocks.OAK_PLANKS, 4)
                .pattern("   ")
                .pattern(" L ")
                .pattern("   ")
                .define('L', OWBlocks.SCARIFIED_OAK_LOG)
                .unlockedBy(getHasName(OWBlocks.SCARIFIED_OAK_LOG.get()), has(OWBlocks.SCARIFIED_OAK_LOG.get()))
                .save(recipeOutput, OperationWild.MOD_ID + ":oak_planks_from_scarified_log");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Blocks.DARK_OAK_PLANKS, 4)
                .pattern("   ")
                .pattern(" L ")
                .pattern("   ")
                .define('L', OWBlocks.SCARIFIED_DARK_OAK_LOG)
                .unlockedBy(getHasName(OWBlocks.SCARIFIED_DARK_OAK_LOG.get()), has(OWBlocks.SCARIFIED_DARK_OAK_LOG.get()))
                .save(recipeOutput, OperationWild.MOD_ID + ":dark_oak_planks_from_scarified_log");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Blocks.SPRUCE_PLANKS, 4)
                .pattern("   ")
                .pattern(" L ")
                .pattern("   ")
                .define('L', OWBlocks.SCARIFIED_SPRUCE_LOG)
                .unlockedBy(getHasName(OWBlocks.SCARIFIED_SPRUCE_LOG.get()), has(OWBlocks.SCARIFIED_SPRUCE_LOG.get()))
                .save(recipeOutput, OperationWild.MOD_ID + ":spruce_planks_from_scarified_log");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Blocks.MANGROVE_PLANKS, 4)
                .pattern("   ")
                .pattern(" L ")
                .pattern("   ")
                .define('L', OWBlocks.SCARIFIED_MANGROVE_LOG)
                .unlockedBy(getHasName(OWBlocks.SCARIFIED_MANGROVE_LOG.get()), has(OWBlocks.SCARIFIED_MANGROVE_LOG.get()))
                .save(recipeOutput, OperationWild.MOD_ID + ":mangrove_planks_from_scarified_log");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Blocks.CHERRY_PLANKS, 4)
                .pattern("   ")
                .pattern(" L ")
                .pattern("   ")
                .define('L', OWBlocks.SCARIFIED_CHERRY_LOG)
                .unlockedBy(getHasName(OWBlocks.SCARIFIED_CHERRY_LOG.get()), has(OWBlocks.SCARIFIED_CHERRY_LOG.get()))
                .save(recipeOutput, OperationWild.MOD_ID + ":cherry_planks_from_scarified_log");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Blocks.BIRCH_PLANKS, 4)
                .pattern("   ")
                .pattern(" L ")
                .pattern("   ")
                .define('L', OWBlocks.SCARIFIED_BIRCH_LOG)
                .unlockedBy(getHasName(OWBlocks.SCARIFIED_BIRCH_LOG.get()), has(OWBlocks.SCARIFIED_BIRCH_LOG.get()))
                .save(recipeOutput, OperationWild.MOD_ID + ":birch_planks_from_scarified_log");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Blocks.ACACIA_PLANKS, 4)
                .pattern("   ")
                .pattern(" L ")
                .pattern("   ")
                .define('L', OWBlocks.SCARIFIED_ACACIA_LOG)
                .unlockedBy(getHasName(OWBlocks.SCARIFIED_ACACIA_LOG.get()), has(OWBlocks.SCARIFIED_ACACIA_LOG.get()))
                .save(recipeOutput, OperationWild.MOD_ID + ":acacia_planks_from_scarified_log");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Blocks.JUNGLE_PLANKS, 4)
                .pattern("   ")
                .pattern(" L ")
                .pattern("   ")
                .define('L', OWBlocks.SCARIFIED_JUNGLE_LOG)
                .unlockedBy(getHasName(OWBlocks.SCARIFIED_JUNGLE_LOG.get()), has(OWBlocks.SCARIFIED_JUNGLE_LOG.get()))
                .save(recipeOutput, OperationWild.MOD_ID + ":jungle_planks_from_scarified_log");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWBlocks.TEDDY_BEAR.get(), 1)
                .pattern(" C ")
                .pattern("CCC")
                .pattern("C C")
                .define('C', OWItems.KODIAK_COAT.get())
                .unlockedBy(getHasName(OWItems.KODIAK_COAT.get()), has(OWItems.KODIAK_COAT.get())).save(recipeOutput);


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.PRIMITIVE_SLINGSHOT.get(), 1)
                .pattern("* *")
                .pattern("SFS")
                .pattern(" * ")
                .define('F', OWItems.PLANT_FIBER.get())
                .define('S', Items.STRING)
                .define('*', Items.STICK)
                .unlockedBy(getHasName(OWItems.PLANT_FIBER.get()), has(OWItems.PLANT_FIBER.get())).save(recipeOutput);


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWBlocks.SADDLER.get(), 1)
                .pattern("ISI")
                .pattern("ICI")
                .pattern("III")
                .define('S', Items.SADDLE)
                .define('I', Items.IRON_INGOT)
                .define('C', Blocks.CRAFTING_TABLE)
                .unlockedBy(getHasName(Items.SADDLE), has(Items.SADDLE)).save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.SEABUG.get(), 1)
                .pattern("  P")
                .pattern(" C ")
                .pattern("H  ")
                .define('C', OWItems.SEABUG_COCKPIT.get())
                .define('H', OWItems.SEABUG_HULL.get())
                .define('P', OWItems.SEABUG_PORTHOLE.get())
                .unlockedBy(getHasName(OWItems.SEABUG_COCKPIT.get()), has(OWItems.SEABUG_COCKPIT.get())).save(recipeOutput);


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.PINK_DYE)
                .pattern("   ")
                .pattern(" C ")
                .pattern("   ")
                .define('C', OWBlocks.CAMELLIA.get()).unlockedBy(getHasName(OWBlocks.CAMELLIA.get()), has(OWBlocks.CAMELLIA.get())).save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWItems.LAVENDER_BOUQUET.get())
                .pattern("   ")
                .pattern(" L ")
                .pattern("   ")
                .define('L', OWBlocks.LAVENDER.get()).unlockedBy(getHasName(OWBlocks.LAVENDER.get()), has(OWBlocks.LAVENDER.get())).save(recipeOutput);





        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, OWBlocks.REDWOOD_PLANKS.get(), 4)
                .pattern("   ")
                .pattern(" P ")
                .pattern("   ")
                .define('P', OWBlocks.REDWOOD_LOG.get()).unlockedBy(getHasName(OWBlocks.REDWOOD_LOG.get()), has(OWBlocks.REDWOOD_LOG.get())).save(recipeOutput);

        stairBuilder(OWBlocks.REDWOOD_STAIRS.get(), Ingredient.of(OWBlocks.REDWOOD_PLANKS)).group("redwood_planks")
                .unlockedBy("has_redwood_planks", has(OWBlocks.REDWOOD_PLANKS)).save(recipeOutput);
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, OWBlocks.REDWOOD_SLAB.get(), OWBlocks.REDWOOD_PLANKS.get());

        buttonBuilder(OWBlocks.REDWOOD_BUTTON.get(), Ingredient.of(OWBlocks.REDWOOD_PLANKS.get())).group("redwood_planks")
                .unlockedBy("has_redwood_planks", has(OWBlocks.REDWOOD_PLANKS.get())).save(recipeOutput);
        pressurePlate(recipeOutput, OWBlocks.REDWOOD_PRESSURE_PLATE.get(), OWBlocks.REDWOOD_PLANKS.get());

        fenceBuilder(OWBlocks.REDWOOD_FENCE.get(), Ingredient.of(OWBlocks.REDWOOD_PLANKS.get())).group("redwood_planks")
                .unlockedBy("has_redwood_planks", has(OWBlocks.REDWOOD_PLANKS.get())).save(recipeOutput);
        fenceGateBuilder(OWBlocks.REDWOOD_FENCE_GATE.get(), Ingredient.of(OWBlocks.REDWOOD_PLANKS.get())).group("redwood_planks")
                .unlockedBy("has_redwood_planks", has(OWBlocks.REDWOOD_PLANKS.get())).save(recipeOutput);

        doorBuilder(OWBlocks.REDWOOD_DOOR.get(), Ingredient.of(OWBlocks.REDWOOD_PLANKS.get())).group("redwood_planks")
                .unlockedBy("has_redwood_planks", has(OWBlocks.REDWOOD_PLANKS.get())).save(recipeOutput);
        trapdoorBuilder(OWBlocks.REDWOOD_TRAPDOOR.get(), Ingredient.of(OWBlocks.REDWOOD_PLANKS.get())).group("redwood_planks")
                .unlockedBy("has_redwood_planks", has(OWBlocks.REDWOOD_PLANKS.get())).save(recipeOutput);




        SimpleCookingRecipeBuilder.smelting(Ingredient.of(OWItems.RAW_TIGER.get()),
                        RecipeCategory.FOOD, OWItems.COOKED_TIGER.get(), 0.35F, 200)
                .unlockedBy(getHasName(OWItems.RAW_TIGER.get()), has(OWItems.RAW_TIGER.get()))
                .save(recipeOutput, OperationWild.MOD_ID + ":cooked_tiger_from_smelting");

        SimpleCookingRecipeBuilder.smoking(Ingredient.of(OWItems.RAW_TIGER.get()),
                        RecipeCategory.FOOD, OWItems.COOKED_TIGER.get(), 0.35F, 100)
                .unlockedBy(getHasName(OWItems.RAW_TIGER.get()), has(OWItems.RAW_TIGER.get()))
                .save(recipeOutput, OperationWild.MOD_ID + ":cooked_tiger_from_smoking");

        SimpleCookingRecipeBuilder.campfireCooking(Ingredient.of(OWItems.RAW_TIGER.get()),
                        RecipeCategory.FOOD, OWItems.COOKED_TIGER.get(), 0.35F, 600)
                .unlockedBy(getHasName(OWItems.RAW_TIGER.get()), has(OWItems.RAW_TIGER.get()))
                .save(recipeOutput, OperationWild.MOD_ID + ":cooked_tiger_from_campfire_cooking");


        SimpleCookingRecipeBuilder.smelting(Ingredient.of(OWItems.RAW_KODIAK.get()),
                        RecipeCategory.FOOD, OWItems.COOKED_KODIAK.get(), 0.35F, 200)
                .unlockedBy(getHasName(OWItems.RAW_KODIAK.get()), has(OWItems.RAW_KODIAK.get()))
                .save(recipeOutput, OperationWild.MOD_ID + ":cooked_kodiak_from_smelting");

        SimpleCookingRecipeBuilder.smoking(Ingredient.of(OWItems.RAW_KODIAK.get()),
                        RecipeCategory.FOOD, OWItems.COOKED_KODIAK.get(), 0.35F, 100)
                .unlockedBy(getHasName(OWItems.RAW_KODIAK.get()), has(OWItems.RAW_KODIAK.get()))
                .save(recipeOutput, OperationWild.MOD_ID + ":cooked_kodiak_from_smoking");

        SimpleCookingRecipeBuilder.campfireCooking(Ingredient.of(OWItems.RAW_KODIAK.get()),
                        RecipeCategory.FOOD, OWItems.COOKED_KODIAK.get(), 0.35F, 600)
                .unlockedBy(getHasName(OWItems.RAW_KODIAK.get()), has(OWItems.RAW_KODIAK.get()))
                .save(recipeOutput, OperationWild.MOD_ID + ":cooked_kodiak_from_campfire_cooking");



        SimpleCookingRecipeBuilder.smelting(Ingredient.of(OWItems.RAW_BOA.get()),
                        RecipeCategory.FOOD, OWItems.COOKED_BOA.get(), 0.35F, 200)
                .unlockedBy(getHasName(OWItems.RAW_BOA.get()), has(OWItems.RAW_BOA.get()))
                .save(recipeOutput, OperationWild.MOD_ID + ":cooked_boa_from_smelting");

        SimpleCookingRecipeBuilder.smoking(Ingredient.of(OWItems.RAW_BOA.get()),
                        RecipeCategory.FOOD, OWItems.COOKED_BOA.get(), 0.35F, 100)
                .unlockedBy(getHasName(OWItems.RAW_BOA.get()), has(OWItems.RAW_BOA.get()))
                .save(recipeOutput, OperationWild.MOD_ID + ":cooked_boa_from_smoking");

        SimpleCookingRecipeBuilder.campfireCooking(Ingredient.of(OWItems.RAW_BOA.get()),
                        RecipeCategory.FOOD, OWItems.COOKED_BOA.get(), 0.35F, 600)
                .unlockedBy(getHasName(OWItems.RAW_BOA.get()), has(OWItems.RAW_BOA.get()))
                .save(recipeOutput, OperationWild.MOD_ID + ":cooked_boa_from_campfire_cooking");



        SimpleCookingRecipeBuilder.smelting(Ingredient.of(OWItems.RAW_PEACOCK.get()),
                        RecipeCategory.FOOD, OWItems.COOKED_PEACOCK.get(), 0.35F, 200)
                .unlockedBy(getHasName(OWItems.RAW_PEACOCK.get()), has(OWItems.RAW_PEACOCK.get()))
                .save(recipeOutput, OperationWild.MOD_ID + ":cooked_peacock_from_smelting");

        SimpleCookingRecipeBuilder.smoking(Ingredient.of(OWItems.RAW_PEACOCK.get()),
                        RecipeCategory.FOOD, OWItems.COOKED_PEACOCK.get(), 0.35F, 100)
                .unlockedBy(getHasName(OWItems.RAW_PEACOCK.get()), has(OWItems.RAW_PEACOCK.get()))
                .save(recipeOutput, OperationWild.MOD_ID + ":cooked_peacock_from_smoking");

        SimpleCookingRecipeBuilder.campfireCooking(Ingredient.of(OWItems.RAW_PEACOCK.get()),
                        RecipeCategory.FOOD, OWItems.COOKED_PEACOCK.get(), 0.35F, 600)
                .unlockedBy(getHasName(OWItems.RAW_PEACOCK.get()), has(OWItems.RAW_PEACOCK.get()))
                .save(recipeOutput, OperationWild.MOD_ID + ":cooked_peacock_from_campfire_cooking");


    }

    protected static <T extends AbstractCookingRecipe> void oreCooking(RecipeOutput recipeOutput, RecipeSerializer<T> pCookingSerializer, AbstractCookingRecipe.Factory<T> factory,
                                                                       List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pRecipeName) {
        for(ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult, pExperience, pCookingTime, pCookingSerializer, factory).group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(recipeOutput, OperationWild.MOD_ID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
        }
    }

}