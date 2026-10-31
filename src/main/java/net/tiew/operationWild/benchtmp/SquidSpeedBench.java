package net.tiew.operationWild.benchtmp;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.animals.aquatic.ColossalSquidEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@GameTestHolder("ow")
@PrefixGameTestTemplate(false)
public class SquidSpeedBench {

    @GameTest(template = "squid_speed_bench", timeoutTicks = 400)
    public static void wander(GameTestHelper helper) { run(helper, "errance x1", 1.0); }

    @GameTest(template = "squid_speed_bench", timeoutTicks = 400)
    public static void chase(GameTestHelper helper) { run(helper, "chasse x1.87", 1.87); }

    @GameTest(template = "squid_speed_bench", timeoutTicks = 400)
    public static void tamingFlee(GameTestHelper helper) { run(helper, "fuite apprivoisement x2.8", 2.8); }

    private static void run(GameTestHelper helper, String name, double modifier) {
        ServerLevel level = helper.getLevel();
        AABB bounds = helper.getBounds();
        int x0 = (int) Math.floor(bounds.minX), y0 = level.getSeaLevel() - 16, z0 = (int) Math.floor(bounds.minZ);
        for (int x = 0; x < 96; x++) for (int z = 0; z < 96; z++) for (int y = 0; y < 18; y++) {
            BlockPos pos = new BlockPos(x0 + x, y0 + y, z0 + z);
            boolean wall = x == 0 || z == 0 || x == 95 || z == 95 || y == 0;
            if (wall) level.setBlock(pos, Blocks.BARRIER.defaultBlockState(), 3);
            else if (y <= 14) level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
        }
        ColossalSquidEntity squid = OWEntityRegistry.COLOSSAL_SQUID.get().create(level);
        squid.moveTo(x0 + 48, y0 + 7, z0 + 4, 0f, 0f);
        level.addFreshEntity(squid);
        try {
            Field f = Mob.class.getDeclaredField("goalSelector");
            f.setAccessible(true);
            ((GoalSelector) f.get(squid)).removeAllGoals(g -> true);
            Field t = Mob.class.getDeclaredField("targetSelector");
            t.setAccessible(true);
            ((GoalSelector) t.get(squid)).removeAllGoals(g -> true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        long start = level.getGameTime();
        Vec3[] last = {squid.position()};
        List<Double> speeds = new ArrayList<>();
        helper.onEachTick(() -> {
            long t = level.getGameTime() - start;
            squid.getMoveControl().setWantedPosition(x0 + 48, y0 + 7, z0 + 300, modifier);
            Vec3 now = squid.position();
            double step = Math.sqrt(Math.pow(now.x - last[0].x, 2) + Math.pow(now.z - last[0].z, 2));
            last[0] = now;
            if (t >= 80 && t < 240) speeds.add(step);
            if (t == 240) {
                double avg = speeds.stream().mapToDouble(d -> d).average().orElse(0);
                double min = speeds.stream().mapToDouble(d -> d).min().orElse(0);
                double max = speeds.stream().mapToDouble(d -> d).max().orElse(0);
                System.out.println(String.format(Locale.ROOT, "[SPEED %s] moyenne=%.3f b/t (%.1f b/s) min=%.3f max=%.3f combat=%s",
                        name, avg, avg * 20, min, max, squid.isCombatFacing()));
                helper.succeed();
            }
        });
    }
}
