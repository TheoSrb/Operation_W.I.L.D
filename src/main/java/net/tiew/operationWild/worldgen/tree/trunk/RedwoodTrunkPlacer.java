package net.tiew.operationWild.worldgen.tree.trunk;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.tiew.operationWild.core.OWUtils;

import java.util.List;
import java.util.function.BiConsumer;

public class RedwoodTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<RedwoodTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(instance ->
            trunkPlacerParts(instance).apply(instance, RedwoodTrunkPlacer::new));

    public RedwoodTrunkPlacer(int pBaseHeight, int pHeightRandA, int pHeightRandB) {
        super(pBaseHeight, pHeightRandA, pHeightRandB);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return OWTrunkPlacerTypes.REDWOOD_TRUNK_PLACER.get();
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, int height,
                                                            BlockPos pos, TreeConfiguration config) {

        for (int i = 0; i < height; i++) {
            placeLog(level, blockSetter, random, pos.above(i), config);
        }

        for (int $$0 = -1; $$0 <= 1; $$0++) {
            createLogPillar(level, blockSetter, random, pos, config, (int) (((double) height / 2) + OWUtils.generateRandomInterval(0, 5)), $$0, 0, 1);
            createLogPillar(level, blockSetter, random, pos, config, (int) (((double) height / 2) + OWUtils.generateRandomInterval(0, 5)), $$0, 0, -1);
        }

        for (int $$1 = -1; $$1 <= 1; $$1++) {
            createLogPillar(level, blockSetter, random, pos, config, (int) (((double) height / 2) + OWUtils.generateRandomInterval(0, 5)), 1, 0, $$1);
            createLogPillar(level, blockSetter, random, pos, config, (int) (((double) height / 2) + OWUtils.generateRandomInterval(0, 5)), -1, 0, $$1);
        }


        for (int $$0 = -1; $$0 <= 1; $$0++) {
            createLogPillar(level, blockSetter, random, pos, config, (int) (((double) height / 8) + OWUtils.generateRandomInterval(0, 5)), $$0, 0, 2);
            createLogPillar(level, blockSetter, random, pos, config, (int) (((double) height / 8) + OWUtils.generateRandomInterval(0, 5)), $$0, 0, -2);
        }

        for (int $$1 = -1; $$1 <= 1; $$1++) {
            createLogPillar(level, blockSetter, random, pos, config, (int) (((double) height / 8) + OWUtils.generateRandomInterval(0, 5)), 2, 0, $$1);
            createLogPillar(level, blockSetter, random, pos, config, (int) (((double) height / 8) + OWUtils.generateRandomInterval(0, 5)), -2, 0, $$1);
        }

        // Spawn Bee Nest
        if (random.nextFloat() < 0.05f) {
            int randomSide = random.nextInt(4);
            Direction facing;
            BlockPos beehivePos;
            int above = random.nextInt(3);
            int x = random.nextInt(3) - 1;
            int z = random.nextInt(3) - 1;

            switch (randomSide) {
                case 0:
                    facing = Direction.NORTH;
                    beehivePos = pos.above(above).offset(x, 0, -3);
                    break;
                case 1:
                    facing = Direction.SOUTH;
                    beehivePos = pos.above(above).offset(x, 0, 3);
                    break;
                case 2:
                    facing = Direction.EAST;
                    beehivePos = pos.above(above).offset(3, 0, z);
                    break;
                default:
                    facing = Direction.WEST;
                    beehivePos = pos.above(above).offset(-3, 0, z);
                    break;
            }

            BlockState beehiveState = Blocks.BEE_NEST.defaultBlockState()
                    .setValue(BeehiveBlock.FACING, facing);
            blockSetter.accept(beehivePos, beehiveState);
        }

        return List.of(new FoliagePlacer.FoliageAttachment(pos.above(height), 0, false));
    }

    private void createLogPillar(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, BlockPos pos, TreeConfiguration config,
                                 int height, int x, int y, int z) {
        for (int j = 0; j < height; j++) {
            if (j == 0) setDirtAt(level, blockSetter, random, pos.offset(x, y, z).below(), config);
            placeLog(level, blockSetter, random, pos.above(j).offset(x, y, z), config);
        }
    }
}