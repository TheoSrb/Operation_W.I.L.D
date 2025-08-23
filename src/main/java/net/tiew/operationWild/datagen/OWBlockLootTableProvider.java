package net.tiew.operationWild.datagen;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.tiew.operationWild.block.OWBlocks;
import net.tiew.operationWild.block.custom.SavageBerryBushBlock;
import net.tiew.operationWild.item.OWItems;

import java.util.Set;
import java.util.stream.Collectors;

public class OWBlockLootTableProvider extends BlockLootSubProvider {

    protected OWBlockLootTableProvider(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        /*dropSelf(OWBlocks.JADE_ORE.get());
        this.add(OWBlocks.JADE_ORE.get(), block -> createOreDrop(OWBlocks.JADE_ORE.get(), OWItems.JADE.get()));*/

        this.add(OWBlocks.JADE_ORE.get(), block -> createMultipleOreDrops(OWBlocks.JADE_ORE.get(), OWItems.JADE.get(), 1, 1));
        this.add(OWBlocks.DEEPSLATE_JADE_ORE.get(), block -> createMultipleOreDrops(OWBlocks.DEEPSLATE_JADE_ORE.get(), OWItems.JADE.get(), 1, 1));

        this.add(OWBlocks.RUBY_ORE.get(), block -> createMultipleOreDrops(OWBlocks.RUBY_ORE.get(), OWItems.RUBY.get(), 1, 1));
        this.add(OWBlocks.DEEPSLATE_RUBY_ORE.get(), block -> createMultipleOreDrops(OWBlocks.DEEPSLATE_RUBY_ORE.get(), OWItems.RUBY.get(), 1, 1));

        dropSelf(OWBlocks.SADDLER.get());

        dropSelf(OWBlocks.REDWOOD_LOG.get());
        dropSelf(OWBlocks.REDWOOD_PLANKS.get());
        dropSelf(OWBlocks.REDWOOD_SAPLING.get());
        this.add(OWBlocks.REDWOOD_LEAVES.get(), block -> createLeavesDrops(block, OWBlocks.REDWOOD_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));

        this.add(OWBlocks.SCARIFIED_OAK_LOG.get(), block -> createMultipleOreDrops(OWBlocks.SCARIFIED_OAK_LOG.get(), Item.byBlock(Blocks.OAK_LOG), 1, 1));
        this.add(OWBlocks.SCARIFIED_DARK_OAK_LOG.get(), block -> createMultipleOreDrops(OWBlocks.SCARIFIED_DARK_OAK_LOG.get(), Item.byBlock(Blocks.DARK_OAK_LOG), 1, 1));
        this.add(OWBlocks.SCARIFIED_SPRUCE_LOG.get(), block -> createMultipleOreDrops(OWBlocks.SCARIFIED_SPRUCE_LOG.get(), Item.byBlock(Blocks.SPRUCE_LOG), 1, 1));
        this.add(OWBlocks.SCARIFIED_MANGROVE_LOG.get(), block -> createMultipleOreDrops(OWBlocks.SCARIFIED_MANGROVE_LOG.get(), Item.byBlock(Blocks.MANGROVE_LOG), 1, 1));
        this.add(OWBlocks.SCARIFIED_CHERRY_LOG.get(), block -> createMultipleOreDrops(OWBlocks.SCARIFIED_CHERRY_LOG.get(), Item.byBlock(Blocks.CHERRY_LOG), 1, 1));
        this.add(OWBlocks.SCARIFIED_BIRCH_LOG.get(), block -> createMultipleOreDrops(OWBlocks.SCARIFIED_BIRCH_LOG.get(), Item.byBlock(Blocks.BIRCH_LOG), 1, 1));
        this.add(OWBlocks.SCARIFIED_ACACIA_LOG.get(), block -> createMultipleOreDrops(OWBlocks.SCARIFIED_ACACIA_LOG.get(), Item.byBlock(Blocks.ACACIA_LOG), 1, 1));
        this.add(OWBlocks.SCARIFIED_JUNGLE_LOG.get(), block -> createMultipleOreDrops(OWBlocks.SCARIFIED_JUNGLE_LOG.get(), Item.byBlock(Blocks.JUNGLE_LOG), 1, 1));


        dropSelf(OWBlocks.PEACOCK_EGG.get());


        dropSelf(OWBlocks.LAVENDER.get());
        dropSelf(OWBlocks.CAMELLIA.get());
        this.add(OWBlocks.POTTED_LAVENDER.get(), createPotFlowerItemTable(OWBlocks.LAVENDER.get()));
        this.add(OWBlocks.POTTED_CAMELLIA.get(), createPotFlowerItemTable(OWBlocks.CAMELLIA.get()));



        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);

        this.add(OWBlocks.SAVAGE_BERRY_BUSH.get(), block -> this.applyExplosionDecay(
                block,LootTable.lootTable().withPool(LootPool.lootPool().when(
                                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(OWBlocks.SAVAGE_BERRY_BUSH.get())
                                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SavageBerryBushBlock.AGE, 3))
                                ).add(LootItem.lootTableItem(OWItems.SAVAGE_BERRIES.get()))
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 3.0F)))
                                .apply(ApplyBonusCount.addUniformBonusCount(registrylookup.getOrThrow(Enchantments.FORTUNE)))
                ).withPool(LootPool.lootPool().when(
                                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(OWBlocks.SAVAGE_BERRY_BUSH.get())
                                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SavageBerryBushBlock.AGE, 2))
                                ).add(LootItem.lootTableItem(OWItems.SAVAGE_BERRIES.get()))
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
                                .apply(ApplyBonusCount.addUniformBonusCount(registrylookup.getOrThrow(Enchantments.FORTUNE)))
                )));



    }

    protected LootTable.Builder createMultipleOreDrops(Block pBlock, Item item, float minDrops, float maxDrops) {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createSilkTouchDispatchTable(
                pBlock, this.applyExplosionDecay(
                        pBlock, LootItem.lootTableItem(item)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(minDrops, maxDrops)))
                                .apply(ApplyBonusCount.addOreBonusCount(registrylookup.getOrThrow(Enchantments.FORTUNE)))
                )
        );
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return OWBlocks.BLOCKS.getEntries().stream().map(DeferredHolder::get).collect(Collectors.toList());
    }
}
