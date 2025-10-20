package net.tiew.operationWild.datagen;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.block.OWBlocks;
import net.tiew.operationWild.block.custom.SavageBerryBushBlock;

import java.util.function.Function;

public class OWBlockStateProvider extends BlockStateProvider {
    public OWBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, OperationWild.MOD_ID, exFileHelper);
    }


    @Override
    protected void registerStatesAndModels() {
        blockWithItem(OWBlocks.JADE_ORE);
        blockWithItem(OWBlocks.DEEPSLATE_JADE_ORE);

        blockWithItem(OWBlocks.RUBY_ORE);
        blockWithItem(OWBlocks.DEEPSLATE_RUBY_ORE);

        simpleBlockWithItem(OWBlocks.LAVENDER.get(), models().cross(blockTexture(OWBlocks.LAVENDER.get()).getPath(), blockTexture(OWBlocks.LAVENDER.get())).renderType("cutout"));
        simpleBlockWithItem(OWBlocks.CAMELLIA.get(), models().cross(blockTexture(OWBlocks.CAMELLIA.get()).getPath(), blockTexture(OWBlocks.CAMELLIA.get())).renderType("cutout"));
        simpleBlockWithItem(OWBlocks.POTTED_LAVENDER.get(), models().singleTexture("potted_lavender", ResourceLocation.fromNamespaceAndPath("minecraft", "flower_pot_cross"), "plant", blockTexture(OWBlocks.LAVENDER.get())).renderType("cutout"));
        simpleBlockWithItem(OWBlocks.POTTED_CAMELLIA.get(), models().singleTexture("potted_camellia", ResourceLocation.fromNamespaceAndPath("minecraft", "flower_pot_cross"), "plant", blockTexture(OWBlocks.CAMELLIA.get())).renderType("cutout"));

        makeBush(((SavageBerryBushBlock) OWBlocks.SAVAGE_BERRY_BUSH.get()), "savage_berry_bush_stage", "savage_berry_bush_stage");

        blockWithItem(OWBlocks.MARKED_MUD);




        logBlock((RotatedPillarBlock) OWBlocks.REDWOOD_LOG.get());
        axisBlock(((RotatedPillarBlock) OWBlocks.REDWOOD_WOOD.get()), blockTexture(OWBlocks.REDWOOD_LOG.get()), blockTexture(OWBlocks.REDWOOD_LOG.get()));
        axisBlock(((RotatedPillarBlock) OWBlocks.STRIPPED_REDWOOD_LOG.get()), blockTexture(OWBlocks.STRIPPED_REDWOOD_LOG.get()), ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "block/stripped_redwood_log_top"));
        axisBlock(((RotatedPillarBlock) OWBlocks.STRIPPED_REDWOOD_WOOD.get()), blockTexture(OWBlocks.STRIPPED_REDWOOD_LOG.get()), blockTexture(OWBlocks.STRIPPED_REDWOOD_LOG.get()));
        blockWithItem(OWBlocks.REDWOOD_PLANKS);
        saplingBlock(OWBlocks.REDWOOD_SAPLING);
        leavesBlock(OWBlocks.REDWOOD_LEAVES);
        stairsBlock(OWBlocks.REDWOOD_STAIRS.get(), blockTexture(OWBlocks.REDWOOD_PLANKS.get()));
        slabBlock(OWBlocks.REDWOOD_SLAB.get(), blockTexture(OWBlocks.REDWOOD_PLANKS.get()), blockTexture(OWBlocks.REDWOOD_PLANKS.get()));
        buttonBlock(OWBlocks.REDWOOD_BUTTON.get(), blockTexture(OWBlocks.REDWOOD_PLANKS.get()));
        pressurePlateBlock(OWBlocks.REDWOOD_PRESSURE_PLATE.get(), blockTexture(OWBlocks.REDWOOD_PLANKS.get()));
        fenceBlock(OWBlocks.REDWOOD_FENCE.get(), blockTexture(OWBlocks.REDWOOD_PLANKS.get()));
        fenceGateBlock(OWBlocks.REDWOOD_FENCE_GATE.get(), blockTexture(OWBlocks.REDWOOD_PLANKS.get()));
        doorBlockWithRenderType(OWBlocks.REDWOOD_DOOR.get(), modLoc("block/redwood_door_bottom"), modLoc("block/redwood_door_top"), "cutout");
        trapdoorBlockWithRenderType(OWBlocks.REDWOOD_TRAPDOOR.get(), modLoc("block/redwood_trapdoor"), true, "cutout");

        blockItem(OWBlocks.REDWOOD_LOG);
        blockItem(OWBlocks.REDWOOD_WOOD);
        blockItem(OWBlocks.STRIPPED_REDWOOD_LOG);
        blockItem(OWBlocks.STRIPPED_REDWOOD_WOOD);
        blockItem(OWBlocks.REDWOOD_STAIRS);
        blockItem(OWBlocks.REDWOOD_SLAB);
        blockItem(OWBlocks.REDWOOD_PRESSURE_PLATE);
        blockItem(OWBlocks.REDWOOD_FENCE_GATE);
        blockItem(OWBlocks.REDWOOD_TRAPDOOR, "_bottom");
    }


    private void blockItem(DeferredBlock<?> deferredBlock) {
        simpleBlockItem(deferredBlock.get(), new ModelFile.UncheckedModelFile("ow:block/" + deferredBlock.getId().getPath()));
    }

    private void blockItem(DeferredBlock<?> deferredBlock, String appendix) {
        simpleBlockItem(deferredBlock.get(), new ModelFile.UncheckedModelFile("ow:block/" + deferredBlock.getId().getPath() + appendix));
    }

    private void leavesBlock(DeferredBlock<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(),
                models().withExistingParent(BuiltInRegistries.BLOCK.getKey(blockRegistryObject.get()).getPath(), "minecraft:block/leaves")
                        .texture("all", blockTexture(blockRegistryObject.get()))
                        .renderType("cutout"));
    }

    private void saplingBlock(DeferredBlock<Block> blockRegistryObject) {
        simpleBlock(blockRegistryObject.get(),
                models().cross(BuiltInRegistries.BLOCK.getKey(blockRegistryObject.get()).getPath(), blockTexture(blockRegistryObject.get())).renderType("cutout"));
    }

    public void makeBush(SavageBerryBushBlock block, String modelName, String textureName) {
        Function<BlockState, ConfiguredModel[]> function = state -> states(state, modelName, textureName);

        getVariantBuilder(block).forAllStates(function);
    }

    private ConfiguredModel[] states(BlockState state, String modelName, String textureName) {
        ConfiguredModel[] models = new ConfiguredModel[1];
        models[0] = new ConfiguredModel(models().cross(modelName + state.getValue(SavageBerryBushBlock.AGE),
                ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "block/" + textureName + state.getValue(SavageBerryBushBlock.AGE))).renderType("cutout"));
        return models;
    }

    private void blockWithItem(DeferredHolder<Block, Block> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
    }
}