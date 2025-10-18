package net.tiew.operationWild.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.block.OWBlocks;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class OWBlockTagProvider extends BlockTagsProvider {

    public OWBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, OperationWild.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
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
                .add(OWBlocks.JADE_ORE.get());



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
                .add(OWBlocks.ANIMAL_CARCASS.get());

    }
}