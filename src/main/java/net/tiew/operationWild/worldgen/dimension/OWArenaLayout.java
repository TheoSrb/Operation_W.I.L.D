package net.tiew.operationWild.worldgen.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.tiew.operationWild.core.OWArena;

public final class OWArenaLayout {

    private OWArenaLayout() {}

    private static final int FLOOR_RADIUS = 40;
    private static final int PODIUM_R0 = 41, PODIUM_R1 = 42;
    private static final int INNER_AMB_R0 = 43, INNER_AMB_R1 = 48;
    private static final int CROSS_R0 = 49, CROSS_R1 = 50;
    private static final int OUTER_AMB_R0 = 51, OUTER_AMB_R1 = 56;
    private static final int BALCONY_R = 51;
    private static final int UPPER_AMB_R0 = 52, UPPER_AMB_R1 = 56;
    private static final int ARCADE_R0 = 57, ARCADE_R1 = 60;
    private static final int BALCONY2_R = 57;
    private static final int THIRD_GAL_R0 = 58, THIRD_GAL_R1 = 63;
    private static final int THIRD_SEAT_R0 = 58, THIRD_SEAT_R1 = 66;
    private static final int THIRD_AMB_R0 = 61, THIRD_AMB_R1 = 66;
    private static final int FACADE_R0 = 67, FACADE_R1 = 70;
    private static final int CLEAR_R = 76;
    private static final int WIPE_R = 88;

    private static final int Y = OWArena.ARENA_FLOOR_Y;
    private static final int GROUND_FLOOR = Y + 1;
    private static final int LOWER_SEAT_Y0 = Y + 7;
    private static final int UPPER_FLOOR = Y + 10;
    private static final int BALCONY_TOP = Y + 16;
    private static final int UPPER_SEAT_Y0 = Y + 17;
    private static final int THIRD_FLOOR = Y + 19;
    private static final int BALCONY2_TOP = Y + 26;
    private static final int THIRD_SEAT_Y0 = Y + 27;

    private static final int FACADE_Y0 = Y + 1;
    private static final int FACADE_LEVELS = 3, LEVEL_H = 16;
    private static final int ARCADE_TOP = FACADE_Y0 + FACADE_LEVELS * LEVEL_H;
    private static final int CLEAR_TOP = 120;

    private static final int FACADE_SECTORS = 24;
    private static final int OUTER_SECTORS = 28;
    private static final int LOW_ARCH_SECTORS = 24;
    private static final int DOOR_SECTORS = 12;
    private static final int STAIR_SECTORS = 6;
    private static final int PODIUM_SECTORS = 16;

    private static final double ARCH_HALF = 0.34;
    private static final int ARCH_STRAIGHT = 7, ARCH_RISE = 5;
    private static final int GATE_HALF = 3;

    private static final int STATUE_FRONT_R = 51;
    private static final int STATUE_BASE_Y = Y + 20;

    private static final int VINE_CHANCE = 21;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState MOSSY_BRICKS = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
    private static final BlockState CRACKED_BRICKS = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
    private static final BlockState BRICKS = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState MOSSY_COBBLE = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
    private static final BlockState TUFF_BRICKS = Blocks.TUFF_BRICKS.defaultBlockState();
    private static final BlockState CHISELED_TUFF = Blocks.CHISELED_TUFF.defaultBlockState();
    private static final BlockState CHISELED_TUFF_BRICKS = Blocks.CHISELED_TUFF_BRICKS.defaultBlockState();

    private static final BlockState GRASS_BLOCK = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState MOSS = Blocks.MOSS_BLOCK.defaultBlockState();
    private static final BlockState MOSS_CARPET = Blocks.MOSS_CARPET.defaultBlockState();
    private static final BlockState COARSE_DIRT = Blocks.COARSE_DIRT.defaultBlockState();
    private static final BlockState ROOTED_DIRT = Blocks.ROOTED_DIRT.defaultBlockState();
    private static final BlockState SHORT_GRASS = Blocks.SHORT_GRASS.defaultBlockState();
    private static final BlockState FERN = Blocks.FERN.defaultBlockState();
    private static final BlockState AZALEA_LEAVES = Blocks.AZALEA_LEAVES.defaultBlockState()
            .setValue(LeavesBlock.PERSISTENT, true);

    private static final Direction[] HORIZONTAL = {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    public static void generate(ServerLevel level) {
        clear(level);
        arenaFloor(level);
        shell(level);
        podiumFace(level);
        podiumArches(level);
        hollow(level);
        doorways(level);
        facadeArches(level);
        stairways(level);
        seatDetails(level);
        gates(level);
        statues(level);
        rubbleField(level);
        interiorDressing(level);
        greenery(level);
        floorFlora(level);
        overgrowth(level);
    }

    private static void clear(ServerLevel level) {
        int ceiling = level.getMaxBuildHeight() - 1;
        forEachColumn(WIPE_R, (x, z) -> {
            for (int y = Y + 1; y <= ceiling; y++) {
                BlockPos pos = new BlockPos(x, y, z);
                if (!level.getBlockState(pos).isAir()) level.setBlock(pos, AIR, Block.UPDATE_CLIENTS);
            }
        });
    }

    private static void arenaFloor(ServerLevel level) {
        forEachColumn(FLOOR_RADIUS, (x, z) -> {
            int ax = Math.abs(x), az = Math.abs(z);
            double n = patchNoise(ax, az) + (noise(ax, az, 9) - 4) * 0.05;
            double towardAlcoves = (az - ax) / (double) FLOOR_RADIUS;
            double soil = n + towardAlcoves * 1.4;

            BlockState ground;
            if (soil > 0.85) ground = COARSE_DIRT;
            else if (soil > 0.45) ground = ROOTED_DIRT;
            else if (n < -0.62) ground = MOSS;
            else ground = GRASS_BLOCK;
            set(level, x, Y, z, ground);
        });
    }

    private static double patchNoise(int ax, int az) {
        return 0.62 * Math.sin(ax * 0.137 + 0.7) * Math.cos(az * 0.119 - 0.3)
                + 0.38 * Math.sin((ax + az) * 0.081)
                + 0.24 * Math.cos((ax - az) * 0.067 + 1.1);
    }

    private static void shell(ServerLevel level) {
        forEachColumn(FACADE_R1, (x, z) -> {
            int r = ringOf(x, z);
            if (r < PODIUM_R0) return;
            int top = profileTop(x, z, r);
            boolean pier = (r >= ARCADE_R0 && r <= ARCADE_R1 && sectorBand(x, z, FACADE_SECTORS) > 0.44)
                    || (r >= FACADE_R0 && sectorBand(x, z, OUTER_SECTORS) > 0.44);
            for (int y = Y; y <= top; y++) {
                set(level, x, y, z, pier ? pierStone(x, y, z) : weathered(x, y, z));
            }
        });
    }

    private static int profileTop(int x, int z, int r) {
        if (r <= PODIUM_R1) return Y + podiumHeight(x, z);
        if (r <= CROSS_R1) return LOWER_SEAT_Y0 + (r - INNER_AMB_R0) / 2;
        if (r == BALCONY_R) return BALCONY_TOP;
        if (r <= UPPER_AMB_R1) return UPPER_SEAT_Y0 + (r - UPPER_AMB_R0) / 2;
        if (r == BALCONY2_R) return BALCONY2_TOP;
        if (r <= THIRD_SEAT_R1) return THIRD_SEAT_Y0 + (r - THIRD_SEAT_R0) / 2;
        return facadeCrest(x, z);
    }

    private static void podiumArches(ServerLevel level) {
        forEachRing(PODIUM_R0, PODIUM_R1, (x, z) -> {
            double band = sectorBand(x, z, LOW_ARCH_SECTORS);
            if (band >= 0.15) return;
            int top = GROUND_FLOOR + 2 + (band < 0.05 ? 2 : (band < 0.10 ? 1 : 0));
            carve(level, x, z, GROUND_FLOOR, top);
            set(level, x, top + 1, z, band < 0.03 ? CHISELED_TUFF : CHISELED_TUFF_BRICKS);
            set(level, x, Y, z, TUFF_BRICKS);
        });
    }

    private static void podiumFace(ServerLevel level) {
        forEachRing(PODIUM_R0, PODIUM_R1, (x, z) -> {
            int top = Y + podiumHeight(x, z);
            if (sectorBand(x, z, PODIUM_SECTORS) > 0.42) {
                set(level, x, top + 1, z, CHISELED_TUFF);
                set(level, x, top + 2, z, CHISELED_TUFF_BRICKS);
            } else if (noise(Math.abs(x) * 3, z * 5, 4) > 0) {
                set(level, x, top + 1, z, Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState()
                        .setValue(SlabBlock.TYPE, SlabType.BOTTOM));
            }
        });
    }

    private static int podiumHeight(int x, int z) {
        int h = 7 + (int) Math.round(ruin(x, z) * 3.0);
        h -= noise(Math.abs(x) * 2, z * 7 + 3, 5) == 0 ? 1 : 0;
        return Math.max(7, Math.min(11, h));
    }

    private static int facadeCrest(int x, int z) {
        int span = ARCADE_TOP - FACADE_Y0 - 34;
        int crest = FACADE_Y0 + 34 + (int) Math.round(ruinOuter(x, z) * span);
        crest -= noise(Math.abs(x) * 7 + 5, z * 13, 4);
        return Math.max(FACADE_Y0 + 34, Math.min(ARCADE_TOP, crest));
    }

    private static double ruinOuter(int x, int z) {
        double a = Math.atan2(z, Math.abs(x));
        double v = 0.58
                + 0.28 * Math.sin(a * 4.7 - 1.1)
                + 0.22 * Math.sin(a * 9.3 + 2.4)
                + 0.14 * Math.cos(a * 15.1 - 0.5);
        return Math.max(0.0, Math.min(1.0, v));
    }

    private static void hollow(ServerLevel level) {
        forEachColumn(FACADE_R1, (x, z) -> {
            int r = ringOf(x, z);
            if (r >= INNER_AMB_R0 && r <= INNER_AMB_R1) {
                int ceil = Math.min(vaultTop(r, INNER_AMB_R0, INNER_AMB_R1, Y, 4, 3),
                        profileTop(x, z, r) - 2);
                carve(level, x, z, GROUND_FLOOR, ceil);
            }
            if (r >= OUTER_AMB_R0 && r <= OUTER_AMB_R1) {
                carve(level, x, z, GROUND_FLOOR, vaultTop(r, OUTER_AMB_R0, OUTER_AMB_R1, Y, 4, 3));
            }
            if (r >= UPPER_AMB_R0 && r <= UPPER_AMB_R1) {
                carve(level, x, z, UPPER_FLOOR + 1,
                        vaultTop(r, UPPER_AMB_R0, UPPER_AMB_R1, UPPER_FLOOR, 4, 2));
            }
            if (r >= THIRD_AMB_R0 && r <= THIRD_AMB_R1) {
                carve(level, x, z, GROUND_FLOOR, vaultTop(r, THIRD_AMB_R0, THIRD_AMB_R1, Y, 5, 3));
            }
            if (r >= THIRD_GAL_R0 && r <= THIRD_GAL_R1) {
                int ceil = Math.min(vaultTop(r, THIRD_GAL_R0, THIRD_GAL_R1, THIRD_FLOOR, 4, 2),
                        profileTop(x, z, r) - 2);
                carve(level, x, z, THIRD_FLOOR + 1, ceil);
            }
        });
    }

    private static int vaultTop(int r, int r0, int r1, int floorY, int base, int amp) {
        double center = (r0 + r1) / 2.0;
        double half = (r1 - r0) / 2.0 + 0.7;
        double d = Math.min(1.0, Math.abs(r - center) / half);
        return floorY + base + (int) Math.round(amp * Math.sqrt(1.0 - d * d));
    }

    private static void doorways(ServerLevel level) {
        forEachColumn(FACADE_R1, (x, z) -> {
            int r = ringOf(x, z);
            double low = sectorBand(x, z, LOW_ARCH_SECTORS);
            if (r >= CROSS_R0 && r <= CROSS_R1 && low < 0.15) {
                int top = GROUND_FLOOR + 3 + (low < 0.05 ? 2 : (low < 0.10 ? 1 : 0));
                carve(level, x, z, GROUND_FLOOR, top);
                set(level, x, top + 1, z, low < 0.05 ? CHISELED_TUFF : CHISELED_TUFF_BRICKS);
            }
            double band = sectorBand(x, z, DOOR_SECTORS);
            if (r == BALCONY_R && band < 0.15) {
                int top = band < 0.08 ? UPPER_FLOOR + 5 : UPPER_FLOOR + 4;
                carve(level, x, z, UPPER_FLOOR + 1, top);
            }
            if (r == BALCONY2_R && band < 0.15) {
                int top = band < 0.08 ? THIRD_FLOOR + 5 : THIRD_FLOOR + 4;
                carve(level, x, z, THIRD_FLOOR + 1, top);
            }
        });
    }

    private static void facadeArches(ServerLevel level) {
        archRing(level, ARCADE_R0, ARCADE_R1, FACADE_SECTORS, 1, false);
        archRing(level, FACADE_R0, FACADE_R1, OUTER_SECTORS, FACADE_LEVELS, true);
    }

    private static void archRing(ServerLevel level, int r0, int r1, int sectors, int levels, boolean outer) {
        forEachRing(r0, r1, (x, z) -> {
            int r = ringOf(x, z);
            double band = sectorBand(x, z, sectors);
            int crest = outer ? facadeCrest(x, z) : profileTop(x, z, r);
            for (int lvl = 0; lvl < levels; lvl++) {
                int base = FACADE_Y0 + lvl * LEVEL_H;
                if (base > crest) break;
                int archTop = archHeight(band, base);
                if (archTop < base) continue;
                if (outer && lvl == 0 && r < r1 - 1) continue;
                if (outer && lvl > 0 && archTop + 2 > crest) continue;
                carve(level, x, z, base, Math.min(archTop, crest));
                if (band < 0.05 && archTop < crest) set(level, x, archTop + 1, z, CHISELED_TUFF);
                if (base - 1 >= Y) set(level, x, base - 1, z, CHISELED_TUFF_BRICKS);
                int cornice = base + LEVEL_H - 1;
                if (cornice <= crest) set(level, x, cornice, z, CHISELED_TUFF_BRICKS);
            }
            if (!level.getBlockState(new BlockPos(x, crest, z)).isAir()) {
                set(level, x, crest, z, noise(Math.abs(x) + 3, z * 2, 3) == 0 ? MOSSY_COBBLE : MOSSY_BRICKS);
            }
        });
    }

    private static int archHeight(double band, int base) {
        if (band >= ARCH_HALF) return base - 1;
        double u = band / ARCH_HALF;
        return base + ARCH_STRAIGHT + (int) Math.round(ARCH_RISE * Math.sqrt(1.0 - u * u));
    }

    private static void stairways(ServerLevel level) {
        for (int s = 0; s < STAIR_SECTORS; s++) {
            double a = 2 * Math.PI * (s + 0.5) / STAIR_SECTORS;
            stairway(level, a, INNER_AMB_R1, INNER_AMB_R0, GROUND_FLOOR + 1);
            stairway(level, a, UPPER_AMB_R1, UPPER_AMB_R0, UPPER_FLOOR + 2);
            stairway(level, a, THIRD_GAL_R1, THIRD_GAL_R0, THIRD_FLOOR + 2);
        }
    }

    private static void stairway(ServerLevel level, double angle, int rFrom, int rTo, int yFrom) {
        double c = Math.cos(angle), s = Math.sin(angle);
        for (int i = 0; i <= rFrom - rTo; i++) {
            int r = rFrom - i;
            int y = yFrom + i;
            for (int t = -1; t <= 1; t++) {
                int x = (int) Math.round(c * r - s * t);
                int z = (int) Math.round(s * r + c * t);
                carve(level, x, z, y, y + 3);
                set(level, x, y, z, stairs(Blocks.TUFF_BRICK_STAIRS.defaultBlockState(), inward(x, z)));
                for (int below = y - 1; below > Y; below--) {
                    BlockPos pos = new BlockPos(x, below, z);
                    if (!level.getBlockState(pos).isAir()) break;
                    set(level, x, below, z, TUFF_BRICKS);
                }
            }
        }
    }

    private static void seatDetails(ServerLevel level) {
        forEachColumn(THIRD_SEAT_R1, (x, z) -> {
            int r = ringOf(x, z);
            boolean lower = r >= INNER_AMB_R0 && r <= CROSS_R1;
            boolean upper = r >= UPPER_AMB_R0 && r <= UPPER_AMB_R1;
            boolean third = r >= THIRD_SEAT_R0 && r <= THIRD_SEAT_R1;
            if (!lower && !upper && !third) return;

            int top = profileTop(x, z, r);
            BlockPos pos = new BlockPos(x, top, z);
            if (level.getBlockState(pos).isAir()) return;
            if (level.getBlockState(pos).getBlock() instanceof StairBlock) return;
            if (!level.getBlockState(pos.above()).isAir()) return;

            int step = lower ? (r - INNER_AMB_R0) % 2
                    : upper ? (r - UPPER_AMB_R0) % 2 : (r - THIRD_SEAT_R0) % 2;
            if (step == 0) {
                set(level, x, top, z, stairs(Blocks.TUFF_BRICK_STAIRS.defaultBlockState(),
                        inward(x, z).getOpposite()));
            } else {
                set(level, x, top, z, noise(Math.abs(x) * 3, z + 11, 5) == 0 ? MOSSY_COBBLE : TUFF_BRICKS);
            }
        });
    }

    private static void gates(ServerLevel level) {
        BlockState bars = Blocks.IRON_BARS.defaultBlockState();
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int r = PODIUM_R0; r <= THIRD_AMB_R1; r++) {
                int x = sx * r;
                for (int z = -GATE_HALF; z <= GATE_HALF; z++) {
                    int ring = ringOf(x, z);
                    int roof = ring <= FACADE_R1 ? profileTop(x, z, ring) - 1 : Y + 7;
                    int h = Math.min(Y + vaultHeight(Math.abs(z)), roof);
                    carve(level, x, z, GROUND_FLOOR, h);
                    set(level, x, Y, z, noise(Math.abs(x), z * 3, 4) == 0 ? MOSSY_COBBLE : TUFF_BRICKS);
                    if (Math.abs(z) == GATE_HALF && h + 1 <= roof) {
                        set(level, x, h + 1, z, CHISELED_TUFF);
                    }
                }
            }
            int gateX = sx * PODIUM_R0;
            for (int z = -GATE_HALF; z <= GATE_HALF; z++) {
                for (int dy = 1; dy <= vaultHeight(Math.abs(z)); dy++) set(level, gateX, Y + dy, z, bars);
            }
        }
    }

    private static int vaultHeight(int az) {
        return switch (az) {
            case 0, 1 -> 6;
            case 2 -> 5;
            default -> 4;
        };
    }

    private static void statues(ServerLevel level) {
        alcove(level, 1);
        alcove(level, -1);
    }

    private static void alcove(ServerLevel level, int zSign) {
        final int uHalf = 5, depth = 10, height = 12;
        for (int u = -uHalf; u <= uHalf; u++) {
            for (int v = 0; v <= depth; v++) {
                for (int h = -1; h <= height; h++) {
                    set(level, u, STATUE_BASE_Y + h, zSign * (STATUE_FRONT_R + v), AIR);
                }
            }
        }
        for (int u = -uHalf; u <= uHalf; u++) {
            for (int v = 0; v <= depth; v++) {
                set(level, u, STATUE_BASE_Y - 1, zSign * (STATUE_FRONT_R + v), weathered(u, STATUE_BASE_Y - 1, v));
            }
        }
        statueBacking(level, zSign, uHalf, depth, height);
    }


    private static void statueBacking(ServerLevel level, int zSign, int uHalf, int depth, int height) {
        int back = STATUE_FRONT_R + depth;
        for (int u = -uHalf; u <= uHalf; u++) {
            for (int v = back; v <= back + 2; v++) {
                int zz = zSign * v;
                int top = STATUE_BASE_Y + height - noise(Math.abs(u) * 5, v * 3, 5);
                for (int y = STATUE_BASE_Y - 2; y <= top; y++) {
                    if (level.getBlockState(new BlockPos(u, y, zz)).isAir()) {
                        set(level, u, y, zz, weathered(u, y, zz));
                    }
                }
            }
        }
    }


    private static void rubbleField(ServerLevel level) {
        int[][] piles = {
                {19, 24}, {26, -11}, {14, -33}, {33, 12}, {9, 30},
                {30, -27}, {22, 34}, {36, -6}, {12, 12}, {28, 21}, {17, -21}
        };
        for (int[] p : piles) {
            rubblePile(level, p[0], p[1]);
            rubblePile(level, -p[0], p[1]);
        }
        int[][] columns = {
                {24, 16}, {13, -25}, {31, -18}, {20, 30}, {34, 2}, {10, -14}
        };
        for (int[] c : columns) {
            boolean alongX = Math.floorMod(c[0] + c[1], 2) == 0;
            fallenColumn(level, c[0], c[1], alongX);
            fallenColumn(level, -c[0], c[1], alongX);
        }
    }

    private static void rubblePile(ServerLevel level, int cx, int cz) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                int x = cx + dx, z = cz + dz;
                if (x * x + z * z > (FLOOR_RADIUS - 2) * (FLOOR_RADIUS - 2)) continue;
                int d = Math.abs(dx) + Math.abs(dz);
                if (d > 3) continue;
                int h = d == 0 ? 2 : (d == 1 ? 1 : 0);
                if (h == 0 && noise(Math.abs(x), z, 3) != 0) continue;
                for (int y = Y + 1; y <= Y + h; y++) {
                    set(level, x, y, z, noise(Math.abs(x) + y, z * 3, 4) == 0 ? CRACKED_BRICKS : MOSSY_COBBLE);
                }
                if (h == 0) {
                    set(level, x, Y + 1, z, Blocks.MOSSY_COBBLESTONE_SLAB.defaultBlockState()
                            .setValue(SlabBlock.TYPE, SlabType.BOTTOM));
                }
            }
        }
    }

    private static void fallenColumn(ServerLevel level, int cx, int cz, boolean alongX) {
        for (int i = -2; i <= 2; i++) {
            int x = alongX ? cx + i : cx;
            int z = alongX ? cz : cz + i;
            if (x * x + z * z > (FLOOR_RADIUS - 2) * (FLOOR_RADIUS - 2)) continue;
            set(level, x, Y + 1, z, Math.abs(i) == 2 ? MOSSY_COBBLE : CHISELED_TUFF);
        }
        int hx = alongX ? cx + 3 * Integer.signum(cx) : cx;
        int hz = alongX ? cz : cz + 3;
        if (hx * hx + hz * hz <= (FLOOR_RADIUS - 2) * (FLOOR_RADIUS - 2)) {
            set(level, hx, Y + 1, hz, Blocks.TUFF_BRICK_SLAB.defaultBlockState()
                    .setValue(SlabBlock.TYPE, SlabType.BOTTOM));
        }
    }

    private static void interiorDressing(ServerLevel level) {
        forEachColumn(FACADE_R1, (x, z) -> {
            int r = ringOf(x, z);
            if (r < INNER_AMB_R0) return;
            dressRoom(level, x, z, GROUND_FLOOR);
            dressRoom(level, x, z, UPPER_FLOOR + 1);
            dressRoom(level, x, z, THIRD_FLOOR + 1);
        });
    }

    private static void dressRoom(ServerLevel level, int x, int z, int y0) {
        BlockPos floor = new BlockPos(x, y0 - 1, z);
        if (!solid(level, floor) || !level.getBlockState(floor.above()).isAir()) return;

        int y1 = Integer.MIN_VALUE;
        for (int y = y0; y <= y0 + 10; y++) {
            if (solid(level, new BlockPos(x, y, z))) {
                y1 = y - 1;
                break;
            }
        }
        if (y1 == Integer.MIN_VALUE) return;

        int n = noise(Math.abs(x) * 5 + 7, z * 3 + 1, 100);
        if (n < 22) set(level, x, y0 - 1, z, MOSS);
        else if (n < 34) set(level, x, y0 - 1, z, MOSSY_COBBLE);
        if (n < 14) set(level, x, y0, z, MOSS_CARPET);
        else if (n < 18) set(level, x, y0, z, SHORT_GRASS);
        else if (n < 20) set(level, x, y0, z, FERN);
        else if (n < 24) set(level, x, y0, z, Blocks.MOSSY_COBBLESTONE_SLAB.defaultBlockState()
                .setValue(SlabBlock.TYPE, SlabType.BOTTOM));

        for (int y = y0; y <= y1; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.getBlockState(pos).isAir()) continue;
            int l = noise(Math.abs(x) * 13 + y * 7, z * 5 + 3, 100);
            if (l >= 16) continue;
            BlockState lichen = lichenAt(level, pos);
            if (lichen != null) set(level, x, y, z, lichen);
        }
    }

    private static BlockState lichenAt(ServerLevel level, BlockPos pos) {
        BlockState state = Blocks.GLOW_LICHEN.defaultBlockState();
        boolean any = false;
        for (Direction d : Direction.values()) {
            if (!solid(level, pos.relative(d))) continue;
            state = state.setValue(MultifaceBlock.getFaceProperty(d), true);
            any = true;
        }
        return any ? state : null;
    }

    private static void greenery(ServerLevel level) {
        forEachColumn(CLEAR_R, (x, z) -> {
            if (x * x + z * z < (FLOOR_RADIUS - 1) * (FLOOR_RADIUS - 1)) return;
            int top = topSolid(level, x, z, CLEAR_TOP, Y + 1);
            if (top == Integer.MIN_VALUE) return;
            BlockPos pos = new BlockPos(x, top, z);
            if (!level.getBlockState(pos).isCollisionShapeFullBlock(level, pos)) return;
            if (!level.getBlockState(pos.above()).isAir()) return;

            int n = noise(Math.abs(x) * 3 + 1, z * 5 + top, 100);
            if (n < 44) {
                set(level, x, top, z, MOSS);
                int f = noise(Math.abs(x) + top, z * 3 + 5, 100);
                if (f < 42) set(level, x, top + 1, z, SHORT_GRASS);
                else if (f < 66) set(level, x, top + 1, z, FERN);
                else if (f < 80) leafClump(level, x, top + 1, z);
            } else if (n < 66) {
                set(level, x, top + 1, z, MOSS_CARPET);
            } else if (n < 80) {
                set(level, x, top, z, mossy(level.getBlockState(pos)));
            }
        });
    }

    private static void leafClump(ServerLevel level, int cx, int cy, int cz) {
        BlockState foliage = AZALEA_LEAVES;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy <= 1; dy++) {
                    if (Math.abs(dx) + Math.abs(dz) + dy > 2) continue;
                    int x = cx + dx, y = cy + dy, z = cz + dz;
                    if (!level.getBlockState(new BlockPos(x, y, z)).isAir()) continue;
                    set(level, x, y, z, foliage);
                }
            }
        }
    }

    private static void floorFlora(ServerLevel level) {
        forEachColumn(FLOOR_RADIUS - 1, (x, z) -> {
            int top = topSolid(level, x, z, Y + 4, Y);
            if (top == Integer.MIN_VALUE) return;
            BlockPos ground = new BlockPos(x, top, z);
            if (!level.getBlockState(ground).is(BlockTags.DIRT)) return;
            if (!level.getBlockState(ground.above()).isAir()) return;

            int n = noise(Math.abs(x) * 7 + 3, z * 11 + 5, 100);
            if (n < 54) set(level, x, top + 1, z, SHORT_GRASS);
            else if (n < 70) set(level, x, top + 1, z, FERN);
            else if (n < 72) doublePlant(level, x, top + 1, z, Blocks.TALL_GRASS.defaultBlockState());
            else if (n < 73) doublePlant(level, x, top + 1, z, Blocks.LARGE_FERN.defaultBlockState());
            else if (n < 80) set(level, x, top + 1, z, MOSS_CARPET);
        });
    }

    private static void doublePlant(ServerLevel level, int x, int y, int z, BlockState plant) {
        if (!level.getBlockState(new BlockPos(x, y + 1, z)).isAir()) return;
        set(level, x, y, z, plant.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER));
        set(level, x, y + 1, z, plant.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
    }

    private static void overgrowth(ServerLevel level) {
        int inner = FLOOR_RADIUS - 2;
        for (int x = -CLEAR_R; x <= CLEAR_R; x++) {
            for (int z = -CLEAR_R; z <= CLEAR_R; z++) {
                int d2 = x * x + z * z;
                if (d2 > CLEAR_R * CLEAR_R || d2 < inner * inner) continue;
                drapeColumn(level, x, z);
            }
        }
    }

    private static void drapeColumn(ServerLevel level, int x, int z) {
        int[] left = new int[HORIZONTAL.length];
        int lowest = x * x + z * z < PODIUM_R0 * PODIUM_R0 ? Y + 5 : Y;
        for (int y = CLEAR_TOP; y > lowest; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.getBlockState(pos).isAir()) {
                java.util.Arrays.fill(left, 0);
                continue;
            }
            int mask = 0;
            for (int i = 0; i < HORIZONTAL.length; i++) {
                if (!solid(level, pos.relative(HORIZONTAL[i]))) {
                    left[i] = 0;
                    continue;
                }
                if (left[i] <= 0) {
                    int key = mirroredFace(i, x);
                    if (noise(Math.abs(x) * 7 + key, z * 11 + y, 100) >= VINE_CHANCE) continue;
                    left[i] = 5 + noise(Math.abs(x) + y, z + key * 3, 10);
                }
                left[i]--;
                mask |= 1 << i;
            }
            if (mask != 0) set(level, x, y, z, vine(mask));
        }
    }

    private static int mirroredFace(int index, int x) {
        if (index < 2 || x >= 0) return index;
        return 5 - index;
    }

    private static BlockState vine(int mask) {
        BlockState state = Blocks.VINE.defaultBlockState();
        for (int i = 0; i < HORIZONTAL.length; i++) {
            if ((mask & (1 << i)) != 0) state = state.setValue(vineProperty(HORIZONTAL[i]), true);
        }
        return state;
    }

    private static BooleanProperty vineProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> VineBlock.NORTH;
            case SOUTH -> VineBlock.SOUTH;
            case EAST -> VineBlock.EAST;
            default -> VineBlock.WEST;
        };
    }

    private static BlockState weathered(int x, int y, int z) {
        int n = noise(Math.abs(x) * 3 + y, z * 5 + y * 2, 100);
        if (n < 26) return MOSSY_BRICKS;
        if (n < 52) return TUFF_BRICKS;
        if (n < 78) return MOSSY_COBBLE;
        if (n < 86) return CRACKED_BRICKS;
        if (n < 93) return BRICKS;
        return MOSS;
    }

    private static BlockState pierStone(int x, int y, int z) {
        int n = noise(Math.abs(x) * 7 + y * 3, z + y, 100);
        if (n < 30) return CHISELED_TUFF;
        if (n < 52) return CHISELED_TUFF_BRICKS;
        if (n < 74) return TUFF_BRICKS;
        if (n < 88) return MOSSY_BRICKS;
        return MOSSY_COBBLE;
    }

    private static BlockState mossy(BlockState state) {
        if (state.is(Blocks.STONE_BRICKS) || state.is(Blocks.CRACKED_STONE_BRICKS)) return MOSSY_BRICKS;
        if (state.is(Blocks.TUFF_BRICKS)) return MOSSY_COBBLE;
        return state;
    }

    private static BlockState stairs(BlockState state, Direction facing) {
        return state.setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, Half.BOTTOM);
    }

    private static double ruin(int x, int z) {
        double a = Math.atan2(z, Math.abs(x));
        double v = 0.55
                + 0.30 * Math.sin(a * 6.1 + 0.6)
                + 0.20 * Math.sin(a * 11.7 - 1.4)
                + 0.12 * Math.cos(a * 3.1 + 2.2);
        return Math.max(0.0, Math.min(1.0, v));
    }

    private static double sectorBand(int x, int z, int sectors) {
        double angle = Math.atan2(z, x);
        if (angle < 0) angle += 2 * Math.PI;
        double sector = 2 * Math.PI / sectors;
        double m = angle % sector;
        return Math.min(m, sector - m) / sector;
    }

    private static Direction inward(int x, int z) {
        if (Math.abs(x) >= Math.abs(z)) return x > 0 ? Direction.WEST : Direction.EAST;
        return z > 0 ? Direction.NORTH : Direction.SOUTH;
    }

    private static int ringOf(int x, int z) {
        int d2 = x * x + z * z;
        int r = (int) Math.sqrt(d2);
        while (r * r < d2) r++;
        return r;
    }

    private static int topSolid(ServerLevel level, int x, int z, int fromY, int minY) {
        for (int y = fromY; y >= minY; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && state.isCollisionShapeFullBlock(level, pos)) return y;
        }
        return Integer.MIN_VALUE;
    }

    private static boolean solid(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && state.isCollisionShapeFullBlock(level, pos);
    }

    private static int noise(int a, int b, int mod) {
        int h = a * 73856093 ^ b * 19349663;
        h = (h ^ (h >>> 13)) * 1274126177;
        return Math.floorMod(h ^ (h >>> 16), mod);
    }

    private interface Column { void at(int x, int z); }

    private static void forEachColumn(int radius, Column action) {
        int r2 = radius * radius;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= r2) action.at(x, z);
            }
        }
    }

    private static void forEachRing(int r0, int r1, Column action) {
        int inner = (r0 - 1) * (r0 - 1), outer = r1 * r1;
        for (int x = -r1; x <= r1; x++) {
            for (int z = -r1; z <= r1; z++) {
                int d2 = x * x + z * z;
                if (d2 <= outer && d2 > inner) action.at(x, z);
            }
        }
    }

    private static void carve(ServerLevel level, int x, int z, int y0, int y1) {
        for (int y = y0; y <= y1; y++) set(level, x, y, z, AIR);
    }

    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
    }

}
