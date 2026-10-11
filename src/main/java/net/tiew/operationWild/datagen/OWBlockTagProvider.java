package net.tiew.operationWild.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.block.OWBlocks;
import net.tiew.operationWild.core.OWTags;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class OWBlockTagProvider extends BlockTagsProvider {

    public OWBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, OperationWild.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(OWTags.Blocks.RED_PANDA_SWEETS)
                .add(Blocks.CAKE)
                .add(Blocks.HONEY_BLOCK);

        tag(BlockTags.LOGS_THAT_BURN)
                .add(OWBlocks.REDWOOD_LOG.get())
                .add(OWBlocks.REDWOOD_WOOD.get())
                .add(OWBlocks.STRIPPED_REDWOOD_LOG.get())
                .add(OWBlocks.STRIPPED_REDWOOD_WOOD.get());


        tag(BlockTags.FENCES)
                .add(OWBlocks.REDWOOD_FENCE.get());
        tag(BlockTags.FENCE_GATES)
                .add(OWBlocks.REDWOOD_FENCE_GATE.get());
        tag(BlockTags.LEAVES)
                .add(OWBlocks.REDWOOD_LEAVES.get());
        tag(BlockTags.PLANKS)
                .add(OWBlocks.REDWOOD_PLANKS.get());


        tag(BlockTags.FLOWERS)
                .add(OWBlocks.LAVENDER.get())
                .add(OWBlocks.CAMELLIA.get());
        tag(BlockTags.FLOWER_POTS)
                .add(OWBlocks.POTTED_LAVENDER.get())
                .add(OWBlocks.POTTED_CAMELLIA.get());


        tag(BlockTags.ANIMALS_SPAWNABLE_ON)
                .add(Blocks.WATER);



        tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(OWBlocks.RUBY_ORE.get())
                .add(OWBlocks.DEEPSLATE_RUBY_ORE.get());
        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(OWBlocks.DEEPSLATE_JADE_ORE.get());
        tag(BlockTags.NEEDS_STONE_TOOL)
                .add(OWBlocks.JADE_ORE.get())
                .add(OWBlocks.BEAR_TRAP.get());



        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(OWBlocks.REDWOOD_LOG.get())
                .add(OWBlocks.SCARIFIED_ACACIA_LOG.get())
                .add(OWBlocks.SCARIFIED_BIRCH_LOG.get())
                .add(OWBlocks.SCARIFIED_CHERRY_LOG.get())
                .add(OWBlocks.SCARIFIED_JUNGLE_LOG.get())
                .add(OWBlocks.SCARIFIED_OAK_LOG.get())
                .add(OWBlocks.SCARIFIED_DARK_OAK_LOG.get())
                .add(OWBlocks.SCARIFIED_MANGROVE_LOG.get())
                .add(OWBlocks.SCARIFIED_SPRUCE_LOG.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(OWBlocks.JADE_ORE.get())
                .add(OWBlocks.DEEPSLATE_JADE_ORE.get())
                .add(OWBlocks.RUBY_ORE.get())
                .add(OWBlocks.DEEPSLATE_RUBY_ORE.get())
                .add(OWBlocks.ANIMAL_CARCASS.get())
                .add(OWBlocks.BEAR_TRAP.get());


        tag(OWTags.Blocks.BEAST_PROOF)
                .add(Blocks.REINFORCED_DEEPSLATE);
        tag(OWTags.Blocks.BEAST_TOUGHNESS_SOFT)
                .addTag(BlockTags.LEAVES)
                .addTag(BlockTags.WOOL)
                .addTag(BlockTags.ICE)
                .addTag(Tags.Blocks.GLASS_BLOCKS)
                .addTag(Tags.Blocks.GLASS_PANES)
                .add(Blocks.HAY_BLOCK)
                .add(Blocks.SNOW_BLOCK)
                .add(Blocks.SPONGE)
                .add(Blocks.WET_SPONGE)
                .add(Blocks.MOSS_BLOCK);
        tag(OWTags.Blocks.BEAST_TOUGHNESS_WOOD)
                .addTag(BlockTags.PLANKS)
                .addTag(BlockTags.LOGS)
                .addTag(BlockTags.BAMBOO_BLOCKS)
                .addTag(BlockTags.WOODEN_DOORS)
                .addTag(BlockTags.WOODEN_TRAPDOORS)
                .addTag(BlockTags.WOODEN_FENCES)
                .addTag(BlockTags.FENCE_GATES)
                .addTag(BlockTags.WOODEN_SLABS)
                .addTag(BlockTags.WOODEN_STAIRS)
                .add(Blocks.CHEST)
                .add(Blocks.TRAPPED_CHEST)
                .add(Blocks.BARREL)
                .add(Blocks.CRAFTING_TABLE)
                .add(Blocks.BOOKSHELF);
        tag(OWTags.Blocks.BEAST_TOUGHNESS_STONE)
                .addTag(BlockTags.BASE_STONE_OVERWORLD)
                .addTag(BlockTags.BASE_STONE_NETHER)
                .addTag(BlockTags.STONE_BRICKS)
                .addTag(BlockTags.TERRACOTTA)
                .addTag(BlockTags.WALLS)
                .addTag(Tags.Blocks.COBBLESTONES)
                .addTag(Tags.Blocks.SANDSTONE_BLOCKS)
                .add(Blocks.BRICKS)
                .add(Blocks.MUD_BRICKS)
                .add(Blocks.SMOOTH_STONE);
        tag(OWTags.Blocks.BEAST_TOUGHNESS_METAL)
                .addTag(Tags.Blocks.ORES)
                .addTag(Tags.Blocks.STORAGE_BLOCKS_IRON)
                .addTag(Tags.Blocks.STORAGE_BLOCKS_RAW_IRON)
                .addTag(Tags.Blocks.STORAGE_BLOCKS_GOLD)
                .addTag(Tags.Blocks.STORAGE_BLOCKS_COPPER)
                .addTag(BlockTags.ANVIL)
                .add(Blocks.IRON_BARS)
                .add(Blocks.IRON_DOOR)
                .add(Blocks.IRON_TRAPDOOR)
                .add(Blocks.CHAIN)
                .add(Blocks.HOPPER)
                .add(Blocks.CAULDRON)
                .add(Blocks.BELL);
        tag(OWTags.Blocks.BEAST_TOUGHNESS_OBSIDIAN)
                .addTag(Tags.Blocks.STORAGE_BLOCKS_NETHERITE)
                .add(Blocks.OBSIDIAN)
                .add(Blocks.CRYING_OBSIDIAN)
                .add(Blocks.ANCIENT_DEBRIS)
                .add(Blocks.RESPAWN_ANCHOR)
                .add(Blocks.ENDER_CHEST);
    }
}