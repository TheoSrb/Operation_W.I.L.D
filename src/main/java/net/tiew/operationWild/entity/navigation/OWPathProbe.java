package net.tiew.operationWild.entity.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
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
        PathFinder finder = new PathFinder(mob.getNavigation().getNodeEvaluator(), Mth.floor(range * 16.0f));
        return finder.findPath(region, mob, Set.of(target), range, accuracy, 1.0f);
    }
}
