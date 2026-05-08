package net.tiew.operationWild.worldgen.biome.surface;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.tiew.operationWild.block.OWBlocks;
import net.tiew.operationWild.worldgen.biome.OWBiomeUtils;
import net.tiew.operationWild.worldgen.biome.OWBiomes;

public class OWSurfaceRules {
    private static final SurfaceRules.RuleSource DIRT = makeStateRule(Blocks.DIRT);
    private static final SurfaceRules.RuleSource PACKED_MUD = makeStateRule(Blocks.PACKED_MUD);
    private static final SurfaceRules.RuleSource GRASS_BLOCK = makeStateRule(Blocks.GRASS_BLOCK);
    private static final SurfaceRules.RuleSource STONE = makeStateRule(Blocks.STONE);
    private static final SurfaceRules.RuleSource PODZOL = makeStateRule(Blocks.PODZOL);
    private static final SurfaceRules.RuleSource SAND = makeStateRule(Blocks.SAND);
    private static final SurfaceRules.RuleSource GRAVEL = makeStateRule(Blocks.GRAVEL);

    public static SurfaceRules.RuleSource makeRules() {
        SurfaceRules.ConditionSource isRedwoodBiome = SurfaceRules.isBiome(OWBiomes.REDWOOD_FOREST_BIOME);
        SurfaceRules.ConditionSource isMineFieldBiome = SurfaceRules.isBiome(OWBiomes.MINE_FIELD_BIOME);
        SurfaceRules.ConditionSource isAboveWater = SurfaceRules.waterBlockCheck(-1, 0);
        SurfaceRules.ConditionSource isOnFloor = SurfaceRules.ON_FLOOR;
        SurfaceRules.ConditionSource isUnderFloor = SurfaceRules.UNDER_FLOOR;

        SurfaceRules.RuleSource redwoodSurface = SurfaceRules.sequence(
                SurfaceRules.ifTrue(isOnFloor,
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(OWBiomeUtils.OWNoises.PODZOL_PATCH, 0.015D), PODZOL),
                                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(OWBiomeUtils.OWNoises.ROOTED_PATCH, 0.001D), PACKED_MUD),
                                GRASS_BLOCK
                        )

                ),
                SurfaceRules.ifTrue(isUnderFloor,
                        SurfaceRules.sequence(
                                DIRT,
                                STONE
                        )
                )
        );

        SurfaceRules.RuleSource defaultSurface = SurfaceRules.sequence(
                SurfaceRules.ifTrue(isOnFloor,
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(isAboveWater, GRASS_BLOCK),
                                DIRT
                        )
                ),
                SurfaceRules.ifTrue(isUnderFloor, STONE)
        );

        SurfaceRules.RuleSource mineFieldSurface = SurfaceRules.sequence(
                SurfaceRules.ifTrue(isOnFloor, SAND),
                SurfaceRules.ifTrue(isUnderFloor, GRAVEL)
        );

        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(isRedwoodBiome, redwoodSurface),
                SurfaceRules.ifTrue(isMineFieldBiome, mineFieldSurface),
                defaultSurface
        );
    }

    private static SurfaceRules.RuleSource makeStateRule(Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }
}
