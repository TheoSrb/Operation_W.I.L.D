package net.tiew.operationWild.entity.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.tiew.operationWild.entity.OWSemiWaterEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class OWPathProbe {

    private static final int REGION_MARGIN = 8;

    private OWPathProbe() {
    }

    @Nullable
    public static Path find(Mob mob, BlockPos target, float range, int accuracy) {
        BlockPos origin = mob.blockPosition();
        int radius = (int) range + REGION_MARGIN;
        PathNavigationRegion region = new PathNavigationRegion(mob.level(),
                origin.offset(-radius, -radius, -radius), origin.offset(radius, radius, radius));
        boolean amphibious = mob instanceof OWSemiWaterEntity;
        NodeEvaluator evaluator = amphibious ? new AmphibiousNodeEvaluator(false) : mob.getNavigation().getNodeEvaluator();
        float waterMalus = mob.getPathfindingMalus(PathType.WATER);
        try {
            PathFinder finder = new PathFinder(evaluator, Mth.floor(range * 16.0f));
            return finder.findPath(region, mob, Set.of(target), range, accuracy, 1.0f);
        } finally {
            if (amphibious) mob.setPathfindingMalus(PathType.WATER, waterMalus);
        }
    }
}
