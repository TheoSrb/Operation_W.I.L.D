package net.tiew.operationWild.worldgen.tree;

import net.minecraft.world.level.block.grower.TreeGrower;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.worldgen.OWConfiguredFeatures;

import java.util.Optional;

public class OWTreeGrowers {
    public static final TreeGrower REDWOOD = new TreeGrower(OperationWild.MOD_ID + ":redwood",
            Optional.empty(), Optional.of(OWConfiguredFeatures.REDWOOD), Optional.empty());

}
