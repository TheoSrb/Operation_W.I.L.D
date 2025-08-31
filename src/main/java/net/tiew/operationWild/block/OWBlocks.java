package net.tiew.operationWild.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.block.custom.*;
import net.tiew.operationWild.item.OWItems;

import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.tiew.operationWild.worldgen.tree.OWTreeGrowers;

public class OWBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(OperationWild.MOD_ID);

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        OWItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    private static Block flowerPot(Block p_278261_) {
        return new FlowerPotBlock(p_278261_, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY));
    }

    private static Block log(MapColor p_285370_, MapColor p_285126_) {
        return new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor((p_152624_) -> p_152624_.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? p_285370_ : p_285126_).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava());
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static Block leaves(SoundType soundType) {
        return new LeavesBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.2F).randomTicks().sound(soundType).noOcclusion().isValidSpawn(Blocks::ocelotOrParrot).isSuffocating((state, getter, pos) -> false).isViewBlocking((state, getter, pos) -> false).ignitedByLava().pushReaction(PushReaction.DESTROY).isRedstoneConductor((state, getter, pos) -> false));
    }

    public static final DeferredBlock<Block> LAVENDER = registerBlock("lavender", () -> new FlowerBlock(MobEffects.NIGHT_VISION, 5.0F, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<Block> CAMELLIA = registerBlock("camellia", () -> new FlowerBlock(MobEffects.NIGHT_VISION, 5.0F, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<Block> POTTED_LAVENDER = registerBlock("potted_lavender", () -> flowerPot(LAVENDER.get()));
    public static final DeferredBlock<Block> POTTED_CAMELLIA = registerBlock("potted_camellia", () -> flowerPot(CAMELLIA.get()));

    public static final DeferredBlock<Block> PEACOCK_EGG = registerBlock("peacock_egg", () -> new OWEgg(BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.BANJO).strength(2.5F).noOcclusion()));


    public static final DeferredBlock<Block> TEDDY_BEAR = registerBlock("teddy_bear", () -> new TeddyBearBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL).strength(2f).noCollission()));


    public static final DeferredBlock<Block> JADE_ORE = registerBlock("jade_ore", () -> new DropExperienceBlock(ConstantInt.of(0), BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(4.0F, 4.0F)));
    public static final DeferredBlock<Block> DEEPSLATE_JADE_ORE = registerBlock("deepslate_jade_ore", () -> new DropExperienceBlock(ConstantInt.of(0), BlockBehaviour.Properties.ofLegacyCopy(OWBlocks.JADE_ORE.get()).mapColor(MapColor.DEEPSLATE).strength(6F, 3.0F).sound(SoundType.DEEPSLATE)));

    public static final DeferredBlock<Block> RUBY_ORE = registerBlock("ruby_ore", () -> new DropExperienceBlock(UniformInt.of(3, 7), BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F, 5.0F)));
    public static final DeferredBlock<Block> DEEPSLATE_RUBY_ORE = registerBlock("deepslate_ruby_ore", () -> new DropExperienceBlock(UniformInt.of(3, 7), BlockBehaviour.Properties.ofLegacyCopy(OWBlocks.RUBY_ORE.get()).mapColor(MapColor.DEEPSLATE).strength(7.5F, 3.0F).sound(SoundType.DEEPSLATE)));

    public static final DeferredBlock<Block> SADDLER = registerBlock("saddler", () -> new SaddlerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava()));


    public static final DeferredBlock<Block> SAVAGE_BERRY_BUSH = registerBlock("savage_berry_bush", () -> new SavageBerryBushBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH)));


    public static final DeferredBlock<Block> SCARIFIED_OAK_LOG = registerBlock("scarified_oak_log", () -> new ScarifiedWoodLogBlock(BlockBehaviour.Properties.of().mapColor((state) -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? MapColor.WOOD : MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<Block> SCARIFIED_DARK_OAK_LOG = registerBlock("scarified_dark_oak_log", () -> new ScarifiedWoodLogBlock(BlockBehaviour.Properties.of().mapColor((state) -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? MapColor.WOOD : MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<Block> SCARIFIED_SPRUCE_LOG = registerBlock("scarified_spruce_log", () -> new ScarifiedWoodLogBlock(BlockBehaviour.Properties.of().mapColor((state) -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? MapColor.WOOD : MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<Block> SCARIFIED_MANGROVE_LOG = registerBlock("scarified_mangrove_log", () -> new ScarifiedWoodLogBlock(BlockBehaviour.Properties.of().mapColor((state) -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? MapColor.WOOD : MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<Block> SCARIFIED_CHERRY_LOG = registerBlock("scarified_cherry_log", () -> new ScarifiedWoodLogBlock(BlockBehaviour.Properties.of().mapColor((state) -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? MapColor.WOOD : MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<Block> SCARIFIED_BIRCH_LOG = registerBlock("scarified_birch_log", () -> new ScarifiedWoodLogBlock(BlockBehaviour.Properties.of().mapColor((state) -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? MapColor.WOOD : MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<Block> SCARIFIED_ACACIA_LOG = registerBlock("scarified_acacia_log", () -> new ScarifiedWoodLogBlock(BlockBehaviour.Properties.of().mapColor((state) -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? MapColor.WOOD : MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<Block> SCARIFIED_JUNGLE_LOG = registerBlock("scarified_jungle_log", () -> new ScarifiedWoodLogBlock(BlockBehaviour.Properties.of().mapColor((state) -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? MapColor.WOOD : MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()));


    public static final DeferredBlock<Block> REDWOOD_LOG = registerBlock("redwood_log", () -> new OWFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG).strength(3f)));
    public static final DeferredBlock<Block> REDWOOD_WOOD = registerBlock("redwood_wood", () -> new OWFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD).strength(3f)));

    public static final DeferredBlock<Block> STRIPPED_REDWOOD_LOG = registerBlock("stripped_redwood_log", () -> new OWFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG).strength(3f)));
    public static final DeferredBlock<Block> STRIPPED_REDWOOD_WOOD = registerBlock("stripped_redwood_wood", () -> new OWFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_WOOD).strength(3f)));

    public static final DeferredBlock<Block> REDWOOD_PLANKS = registerBlock("redwood_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)) {
                @Override
                public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return true;
                }

                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 20;
                }

                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 5;
                }
            });

    public static final DeferredBlock<Block> REDWOOD_LEAVES = registerBlock("redwood_leaves", () -> leaves(SoundType.GRASS));
    public static final DeferredBlock<Block> REDWOOD_SAPLING = registerBlock("redwood_sapling", () -> new SaplingBlock(OWTreeGrowers.REDWOOD, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING)));
    public static final DeferredBlock<StairBlock> REDWOOD_STAIRS = registerBlock("redwood_stairs", () -> new StairBlock(OWBlocks.REDWOOD_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.of().strength(2f).requiresCorrectToolForDrops().sound(SoundType.WOOD)));
    public static final DeferredBlock<SlabBlock> REDWOOD_SLAB = registerBlock("redwood_slab", () -> new SlabBlock(BlockBehaviour.Properties.of().strength(2f).requiresCorrectToolForDrops().sound(SoundType.WOOD)));
    public static final DeferredBlock<PressurePlateBlock> REDWOOD_PRESSURE_PLATE = registerBlock("redwood_pressure_plate", () -> new PressurePlateBlock(BlockSetType.MANGROVE, BlockBehaviour.Properties.of().strength(2f).requiresCorrectToolForDrops().sound(SoundType.WOOD)));
    public static final DeferredBlock<ButtonBlock> REDWOOD_BUTTON = registerBlock("redwood_button", () -> new ButtonBlock(BlockSetType.OAK, 5, BlockBehaviour.Properties.of().strength(2f).requiresCorrectToolForDrops().noCollission().sound(SoundType.WOOD)));
    public static final DeferredBlock<FenceBlock> REDWOOD_FENCE = registerBlock("redwood_fence", () -> new FenceBlock(BlockBehaviour.Properties.of().strength(2f).requiresCorrectToolForDrops().sound(SoundType.WOOD)));
    public static final DeferredBlock<FenceGateBlock> REDWOOD_FENCE_GATE = registerBlock("redwood_fence_gate", () -> new FenceGateBlock(WoodType.MANGROVE, BlockBehaviour.Properties.of().strength(2f).requiresCorrectToolForDrops().sound(SoundType.WOOD)));
    public static final DeferredBlock<DoorBlock> REDWOOD_DOOR = registerBlock("redwood_door", () -> new DoorBlock(BlockSetType.MANGROVE, BlockBehaviour.Properties.of().strength(2f).requiresCorrectToolForDrops().noOcclusion().sound(SoundType.WOOD)));
    public static final DeferredBlock<TrapDoorBlock> REDWOOD_TRAPDOOR = registerBlock("redwood_trapdoor", () -> new TrapDoorBlock(BlockSetType.MANGROVE, BlockBehaviour.Properties.of().strength(2f).requiresCorrectToolForDrops().noOcclusion().sound(SoundType.WOOD)));

}
