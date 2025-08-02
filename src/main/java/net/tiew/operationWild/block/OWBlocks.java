package net.tiew.operationWild.block;

import net.minecraft.core.Direction;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.block.custom.OWEgg;
import net.tiew.operationWild.block.custom.SaddlerBlock;
import net.tiew.operationWild.block.custom.SavageBerryBushBlock;
import net.tiew.operationWild.block.custom.ScarifiedWoodLogBlock;
import net.tiew.operationWild.item.OWItems;

import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

import net.neoforged.neoforge.registries.DeferredHolder;

public class OWBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, OperationWild.MOD_ID);
    public static void register(IEventBus bus) { BLOCKS.register(bus);}

    private static <T extends Block> void registerBlockItem(String name, DeferredHolder<Block, T> block) {
        OWItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static Block flowerPot(Block p_278261_) {
        return new FlowerPotBlock(p_278261_, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY));
    }

    private static Block log(MapColor p_285370_, MapColor p_285126_) {
        return new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor((p_152624_) -> p_152624_.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? p_285370_ : p_285126_).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava());
    }

    private static <T extends Block> DeferredHolder<Block, T> registerBlock(String name, Supplier<T> block) {
        DeferredHolder<Block, T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    public static final DeferredHolder<Block, Block> LAVENDER = registerBlock("lavender", () -> new FlowerBlock(MobEffects.NIGHT_VISION, 5.0F, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));
    public static final DeferredHolder<Block, Block> CAMELLIA = registerBlock("camellia", () -> new FlowerBlock(MobEffects.NIGHT_VISION, 5.0F, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));

    public static final DeferredHolder<Block, Block> POTTED_LAVENDER = registerBlock("potted_lavender", () -> flowerPot(LAVENDER.get()));
    public static final DeferredHolder<Block, Block> POTTED_CAMELLIA = registerBlock("potted_camellia", () -> flowerPot(CAMELLIA.get()));

    public static final DeferredHolder<Block, Block> PEACOCK_EGG = registerBlock("peacock_egg", () -> new OWEgg(BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.BANJO).strength(2.5F).noOcclusion()));


    public static final DeferredHolder<Block, Block> JADE_ORE = registerBlock("jade_ore", () -> new DropExperienceBlock(ConstantInt.of(0), BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(4.0F, 4.0F)));
    public static final DeferredHolder<Block, Block> DEEPSLATE_JADE_ORE = registerBlock("deepslate_jade_ore", () -> new DropExperienceBlock(ConstantInt.of(0), BlockBehaviour.Properties.ofLegacyCopy(OWBlocks.JADE_ORE.get()).mapColor(MapColor.DEEPSLATE).strength(6F, 3.0F).sound(SoundType.DEEPSLATE)));

    public static final DeferredHolder<Block, Block> RUBY_ORE = registerBlock("ruby_ore", () -> new DropExperienceBlock(UniformInt.of(3, 7), BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F, 5.0F)));
    public static final DeferredHolder<Block, Block> DEEPSLATE_RUBY_ORE = registerBlock("deepslate_ruby_ore", () -> new DropExperienceBlock(UniformInt.of(3, 7), BlockBehaviour.Properties.ofLegacyCopy(OWBlocks.RUBY_ORE.get()).mapColor(MapColor.DEEPSLATE).strength(7.5F, 3.0F).sound(SoundType.DEEPSLATE)));

    public static final DeferredHolder<Block, Block> SADDLER = registerBlock("saddler", () -> new SaddlerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava()));


    public static final DeferredHolder<Block, Block> SAVAGE_BERRY_BUSH = registerBlock("savage_berry_bush", () -> new SavageBerryBushBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH)));


    public static final DeferredHolder<Block, Block> SCARIFIED_OAK_LOG = registerBlock("scarified_oak_log", () -> new ScarifiedWoodLogBlock(BlockBehaviour.Properties.of().mapColor((state) -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? MapColor.WOOD : MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredHolder<Block, Block> SCARIFIED_DARK_OAK_LOG = registerBlock("scarified_dark_oak_log", () -> new ScarifiedWoodLogBlock(BlockBehaviour.Properties.of().mapColor((state) -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? MapColor.WOOD : MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredHolder<Block, Block> SCARIFIED_SPRUCE_LOG = registerBlock("scarified_spruce_log", () -> new ScarifiedWoodLogBlock(BlockBehaviour.Properties.of().mapColor((state) -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? MapColor.WOOD : MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredHolder<Block, Block> SCARIFIED_MANGROVE_LOG = registerBlock("scarified_mangrove_log", () -> new ScarifiedWoodLogBlock(BlockBehaviour.Properties.of().mapColor((state) -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? MapColor.WOOD : MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredHolder<Block, Block> SCARIFIED_CHERRY_LOG = registerBlock("scarified_cherry_log", () -> new ScarifiedWoodLogBlock(BlockBehaviour.Properties.of().mapColor((state) -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? MapColor.WOOD : MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredHolder<Block, Block> SCARIFIED_BIRCH_LOG = registerBlock("scarified_birch_log", () -> new ScarifiedWoodLogBlock(BlockBehaviour.Properties.of().mapColor((state) -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? MapColor.WOOD : MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredHolder<Block, Block> SCARIFIED_ACACIA_LOG = registerBlock("scarified_acacia_log", () -> new ScarifiedWoodLogBlock(BlockBehaviour.Properties.of().mapColor((state) -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? MapColor.WOOD : MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredHolder<Block, Block> SCARIFIED_JUNGLE_LOG = registerBlock("scarified_jungle_log", () -> new ScarifiedWoodLogBlock(BlockBehaviour.Properties.of().mapColor((state) -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? MapColor.WOOD : MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()));

}
