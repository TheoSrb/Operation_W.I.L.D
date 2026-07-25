package net.tiew.operationWild.worldgen.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.tiew.operationWild.core.OWArena;

public final class OWAquaticArenaLayout {

    private OWAquaticArenaLayout() {}

    private static final int FLOOR_RADIUS = 30;
    private static final int PODIUM_R0 = 31, PODIUM_R1 = 32;
    private static final int INNER_AMB_R0 = 33, INNER_AMB_R1 = 38;
    private static final int CROSS_R0 = 39, CROSS_R1 = 40;
    private static final int OUTER_AMB_R0 = 41, OUTER_AMB_R1 = 46;
    private static final int BALCONY_R = 41;
    private static final int UPPER_AMB_R0 = 42, UPPER_AMB_R1 = 46;
    private static final int ARCADE_R0 = 47, ARCADE_R1 = 50;
    private static final int BALCONY2_R = 47;
    private static final int THIRD_GAL_R0 = 48, THIRD_GAL_R1 = 53;
    private static final int THIRD_SEAT_R0 = 48, THIRD_SEAT_R1 = 56;
    private static final int THIRD_AMB_R0 = 51, THIRD_AMB_R1 = 56;
    private static final int FACADE_R0 = 57, FACADE_R1 = 60;
    private static final int CLEAR_R = 66;
    private static final int HALF = 100;
    private static final int PURGE_HALF = 115;

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
    private static final int WATER_TOP = 126;

    private static final int FACADE_SECTORS = 16;
    private static final int OUTER_SECTORS = 18;
    private static final int LOW_ARCH_SECTORS = 16;
    private static final int DOOR_SECTORS = 10;
    private static final int STAIR_SECTORS = 6;
    private static final int PODIUM_SECTORS = 12;

    private static final double ARCH_HALF = 0.36;
    private static final int ARCH_STRAIGHT = 8, ARCH_RISE = 6;
    private static final int GATE_HALF = 3;

    private static final int STATUE_FRONT_R = 41;
    private static final int STATUE_BASE_Y = Y + 20;

    private static int cx;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();
    private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();

    private static final BlockState MOSSY_BRICKS = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
    private static final BlockState CRACKED_BRICKS = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
    private static final BlockState BRICKS = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState MOSSY_COBBLE = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
    private static final BlockState TUFF_BRICKS = Blocks.TUFF_BRICKS.defaultBlockState();
    private static final BlockState CHISELED_TUFF = Blocks.CHISELED_TUFF.defaultBlockState();
    private static final BlockState CHISELED_TUFF_BRICKS = Blocks.CHISELED_TUFF_BRICKS.defaultBlockState();
    private static final BlockState PRISMARINE = Blocks.PRISMARINE.defaultBlockState();
    private static final BlockState MOSS = Blocks.MOSS_BLOCK.defaultBlockState();
    private static final BlockState MOSS_CARPET = Blocks.MOSS_CARPET.defaultBlockState();
    private static final BlockState SEA_LANTERN = Blocks.SEA_LANTERN.defaultBlockState();

    private static final BlockState SEAGRASS = Blocks.SEAGRASS.defaultBlockState();
    private static final BlockState TALL_SEAGRASS = Blocks.TALL_SEAGRASS.defaultBlockState();
    private static final BlockState KELP_PLANT = Blocks.KELP_PLANT.defaultBlockState();
    private static final BlockState KELP = Blocks.KELP.defaultBlockState();

    private static final BlockState[] DEAD_CORAL = {
            Blocks.DEAD_TUBE_CORAL_BLOCK.defaultBlockState(),
            Blocks.DEAD_BRAIN_CORAL_BLOCK.defaultBlockState(),
            Blocks.DEAD_BUBBLE_CORAL_BLOCK.defaultBlockState(),
            Blocks.DEAD_FIRE_CORAL_BLOCK.defaultBlockState(),
            Blocks.DEAD_HORN_CORAL_BLOCK.defaultBlockState()
    };
    private static final BlockState[] LIVE_CORAL = {
            Blocks.TUBE_CORAL_BLOCK.defaultBlockState(),
            Blocks.BRAIN_CORAL_BLOCK.defaultBlockState(),
            Blocks.BUBBLE_CORAL_BLOCK.defaultBlockState(),
            Blocks.FIRE_CORAL_BLOCK.defaultBlockState(),
            Blocks.HORN_CORAL_BLOCK.defaultBlockState()
    };
    private static final BlockState[] DEAD_FAN = {
            fan(Blocks.DEAD_TUBE_CORAL_FAN.defaultBlockState()),
            fan(Blocks.DEAD_BRAIN_CORAL_FAN.defaultBlockState()),
            fan(Blocks.DEAD_BUBBLE_CORAL_FAN.defaultBlockState()),
            fan(Blocks.DEAD_FIRE_CORAL_FAN.defaultBlockState()),
            fan(Blocks.DEAD_HORN_CORAL_FAN.defaultBlockState())
    };
    private static final BlockState[] LIVE_FAN = {
            fan(Blocks.TUBE_CORAL_FAN.defaultBlockState()),
            fan(Blocks.BRAIN_CORAL_FAN.defaultBlockState()),
            fan(Blocks.BUBBLE_CORAL_FAN.defaultBlockState()),
            fan(Blocks.FIRE_CORAL_FAN.defaultBlockState()),
            fan(Blocks.HORN_CORAL_FAN.defaultBlockState())
    };

    private static final Direction[] HORIZONTAL = {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    private static BlockState fan(BlockState state) {
        return state.setValue(BlockStateProperties.WATERLOGGED, true);
    }

    public static void base(ServerLevel level, int centerX) {
        cx = centerX;
        purge(level);
        flood(level);
    }

    public static void generate(ServerLevel level, int centerX) {
        cx = centerX;
        purge(level);
        flood(level);
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
        blendStairs(level);
        rubbleField(level);
        galleryDressing(level);
        seabedGreenery(level);
        arenaFlora(level);
        wallEncrust(level);
    }

    private static void purge(ServerLevel level) {
        int ceiling = level.getMaxBuildHeight() - 1;
        forEachSquare(PURGE_HALF, (x, z) -> {
            for (int y = Y + 1; y <= ceiling; y++) {
                if (!getState(level, x, y, z).isAir()) set(level, x, y, z, AIR);
            }
            int ax = Math.abs(x), az = Math.abs(z);
            if (Math.max(ax, az) <= HALF) {
                set(level, x, Y, z, noise(ax * 3 + 1, az * 3 + 7, 100) < 12 ? GRAVEL : SAND);
            } else {
                set(level, x, Y, z, GRASS);
            }
        });
    }

    private static void flood(ServerLevel level) {
        forEachSquare(HALF, (x, z) -> {
            for (int y = Y + 1; y <= WATER_TOP; y++) set(level, x, y, z, WATER);
        });
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
                set(level, x, top + 1, z, wl(Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState()
                        .setValue(SlabBlock.TYPE, SlabType.BOTTOM)));
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
            if (!getState(level, x, crest, z).isAir()) {
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
                    if (!getState(level, x, below, z).isAir() && !getState(level, x, below, z).is(Blocks.WATER)) break;
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
            BlockState here = getState(level, x, top, z);
            if (here.isAir() || here.is(Blocks.WATER)) return;
            if (here.getBlock() instanceof StairBlock) return;
            if (!getState(level, x, top + 1, z).is(Blocks.WATER) && !getState(level, x, top + 1, z).isAir()) return;

            int step = lower ? (r - INNER_AMB_R0) % 2
                    : upper ? (r - UPPER_AMB_R0) % 2 : (r - THIRD_SEAT_R0) % 2;
            if (step == 0) {
                set(level, x, top, z, stairs(Blocks.TUFF_BRICK_STAIRS.defaultBlockState(),
                        inward(x, z).getOpposite()));
            } else {
                set(level, x, top, z, seatSurface(x, z));
            }
        });
    }

    private static void gates(ServerLevel level) {
        BlockState bars = Blocks.IRON_BARS.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true);
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
                    set(level, u, STATUE_BASE_Y + h, zSign * (STATUE_FRONT_R + v), WATER);
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
                    if (getState(level, u, y, zz).is(Blocks.WATER) || getState(level, u, y, zz).isAir()) {
                        set(level, u, y, zz, weathered(u, y, zz));
                    }
                }
            }
        }
    }

    private static void blendStairs(ServerLevel level) {
        forEachColumn(THIRD_SEAT_R1, (x, z) -> {
            int r = ringOf(x, z);
            if (r < INNER_AMB_R0) return;
            for (int y = GROUND_FLOOR; y <= THIRD_SEAT_Y0 + 6; y++) {
                BlockState state = getState(level, x, y, z);
                if (!(state.getBlock() instanceof StairBlock)) continue;

                Direction facing = state.getValue(StairBlock.FACING);
                boolean onSeatRow = isSeatRing(r) && y == profileTop(x, z, r);
                boolean flushRight = onSeatRow && flushSurface(level, bp(x, y, z).relative(facing.getClockWise()));
                boolean flushLeft = onSeatRow && flushSurface(level, bp(x, y, z).relative(facing.getCounterClockWise()));

                if (flushRight && flushLeft) {
                    set(level, x, y, z, seatSurface(x, z));
                    continue;
                }
                StairsShape shape = flushRight ? StairsShape.INNER_RIGHT
                        : flushLeft ? StairsShape.INNER_LEFT
                        : stairShape(level, bp(x, y, z), state);
                if (state.getValue(StairBlock.SHAPE) != shape) {
                    set(level, x, y, z, state.setValue(StairBlock.SHAPE, shape));
                }
            }
        });
    }

    private static boolean isSeatRing(int r) {
        return (r >= INNER_AMB_R0 && r <= CROSS_R1)
                || (r >= UPPER_AMB_R0 && r <= UPPER_AMB_R1)
                || (r >= THIRD_SEAT_R0 && r <= THIRD_SEAT_R1);
    }

    private static boolean flushSurface(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && !state.is(Blocks.WATER)
                && state.isCollisionShapeFullBlock(level, pos)
                && (level.getBlockState(pos.above()).isAir() || level.getBlockState(pos.above()).is(Blocks.WATER));
    }

    private static BlockState seatSurface(int x, int z) {
        return noise(Math.abs(x) * 3, z + 11, 5) == 0 ? MOSSY_COBBLE : TUFF_BRICKS;
    }

    private static StairsShape stairShape(ServerLevel level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(StairBlock.FACING);

        BlockState front = level.getBlockState(pos.relative(facing));
        if (sameStairHalf(front, state)) {
            Direction other = front.getValue(StairBlock.FACING);
            if (other.getAxis() != facing.getAxis() && freeSide(level, pos, state, other.getOpposite())) {
                return other == facing.getCounterClockWise() ? StairsShape.OUTER_LEFT : StairsShape.OUTER_RIGHT;
            }
        }

        BlockState back = level.getBlockState(pos.relative(facing.getOpposite()));
        if (sameStairHalf(back, state)) {
            Direction other = back.getValue(StairBlock.FACING);
            if (other.getAxis() != facing.getAxis() && freeSide(level, pos, state, other)) {
                return other == facing.getCounterClockWise() ? StairsShape.INNER_LEFT : StairsShape.INNER_RIGHT;
            }
        }
        return StairsShape.STRAIGHT;
    }

    private static boolean sameStairHalf(BlockState neighbour, BlockState state) {
        return neighbour.getBlock() instanceof StairBlock
                && neighbour.getValue(StairBlock.HALF) == state.getValue(StairBlock.HALF);
    }

    private static boolean freeSide(ServerLevel level, BlockPos pos, BlockState state, Direction side) {
        BlockState neighbour = level.getBlockState(pos.relative(side));
        return !(neighbour.getBlock() instanceof StairBlock)
                || neighbour.getValue(StairBlock.FACING) != state.getValue(StairBlock.FACING)
                || neighbour.getValue(StairBlock.HALF) != state.getValue(StairBlock.HALF);
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

    private static void rubblePile(ServerLevel level, int cxr, int czr) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                int x = cxr + dx, z = czr + dz;
                if (x * x + z * z > (FLOOR_RADIUS - 2) * (FLOOR_RADIUS - 2)) continue;
                int d = Math.abs(dx) + Math.abs(dz);
                if (d > 3) continue;
                int h = d == 0 ? 2 : (d == 1 ? 1 : 0);
                if (h == 0 && noise(Math.abs(x), z, 3) != 0) continue;
                for (int y = Y + 1; y <= Y + h; y++) {
                    set(level, x, y, z, noise(Math.abs(x) + y, z * 3, 4) == 0 ? CRACKED_BRICKS : MOSSY_COBBLE);
                }
                if (h == 0) {
                    set(level, x, Y + 1, z, wl(Blocks.MOSSY_COBBLESTONE_SLAB.defaultBlockState()
                            .setValue(SlabBlock.TYPE, SlabType.BOTTOM)));
                }
            }
        }
    }

    private static void fallenColumn(ServerLevel level, int cxr, int czr, boolean alongX) {
        for (int i = -2; i <= 2; i++) {
            int x = alongX ? cxr + i : cxr;
            int z = alongX ? czr : czr + i;
            if (x * x + z * z > (FLOOR_RADIUS - 2) * (FLOOR_RADIUS - 2)) continue;
            set(level, x, Y + 1, z, Math.abs(i) == 2 ? MOSSY_COBBLE : CHISELED_TUFF);
        }
        int hx = alongX ? cxr + 3 * Integer.signum(cxr) : cxr;
        int hz = alongX ? czr : czr + 3;
        if (hx * hx + hz * hz <= (FLOOR_RADIUS - 2) * (FLOOR_RADIUS - 2)) {
            set(level, hx, Y + 1, hz, wl(Blocks.TUFF_BRICK_SLAB.defaultBlockState()
                    .setValue(SlabBlock.TYPE, SlabType.BOTTOM)));
        }
    }

    private static void galleryDressing(ServerLevel level) {
        forEachColumn(FACADE_R1, (x, z) -> {
            int r = ringOf(x, z);
            if (r < INNER_AMB_R0) return;
            dressGallery(level, x, z, GROUND_FLOOR);
            dressGallery(level, x, z, UPPER_FLOOR + 1);
            dressGallery(level, x, z, THIRD_FLOOR + 1);
        });
    }

    private static void dressGallery(ServerLevel level, int x, int z, int y0) {
        if (!solid(level, x, y0 - 1, z) || !getState(level, x, y0, z).is(Blocks.WATER)) return;

        int y1 = Integer.MIN_VALUE;
        for (int y = y0; y <= y0 + 10; y++) {
            if (solid(level, x, y, z)) { y1 = y - 1; break; }
        }
        if (y1 == Integer.MIN_VALUE) return;

        int n = noise(Math.abs(x) * 5 + 7, z * 3 + 1, 100);
        if (n < 20) set(level, x, y0 - 1, z, MOSS);
        else if (n < 30) set(level, x, y0 - 1, z, MOSSY_COBBLE);
        if (n < 12) set(level, x, y0, z, MOSS_CARPET);
        else if (n < 17) set(level, x, y0, z, SEAGRASS);
        else if (n < 21) set(level, x, y0, z, DEAD_FAN[n % 5]);
        else if (n < 24) set(level, x, y0, z, seaPickle(1 + n % 3));

        for (int y = y0; y <= y1; y++) {
            if (!getState(level, x, y, z).is(Blocks.WATER)) continue;
            int l = noise(Math.abs(x) * 13 + y * 7, z * 5 + 3, 100);
            if (l < 4) { set(level, x, y, z, seaPickle(1 + l)); continue; }
            if (l >= 16) continue;
            BlockState lichen = glowLichen(level, x, y, z);
            if (lichen != null) set(level, x, y, z, lichen);
        }
    }

    private static BlockState glowLichen(ServerLevel level, int x, int y, int z) {
        BlockState state = Blocks.GLOW_LICHEN.defaultBlockState();
        boolean any = false;
        for (Direction d : Direction.values()) {
            if (!solid(level, x + d.getStepX(), y + d.getStepY(), z + d.getStepZ())) continue;
            state = state.setValue(MultifaceBlock.getFaceProperty(d), true);
            any = true;
        }
        return any ? state.setValue(BlockStateProperties.WATERLOGGED, true) : null;
    }

    private static void seabedGreenery(ServerLevel level) {
        forEachSquare(HALF, (x, z) -> {
            int r = ringOf(x, z);
            if (r < FLOOR_RADIUS) return;
            int top = topSolid(level, x, z, WATER_TOP, Y);
            if (top < Y) return;
            BlockState below = getState(level, x, top, z);
            if (!below.is(Blocks.SAND) && !below.is(Blocks.GRAVEL)) return;
            if (!getState(level, x, top + 1, z).is(Blocks.WATER)) return;

            int ax = Math.abs(x), az = Math.abs(z);
            int n = noise(ax * 3 + 1, az * 5 + top, 100);
            if (r > CLEAR_R && n < 14) kelpColumn(level, x, top + 1, z, 4 + noise(ax, az + r, 10));
            else if (n < 22) set(level, x, top + 1, z, SEAGRASS);
            else if (n < 30) tallSeagrass(level, x, top + 1, z);
            else if (n < 36) coralClump(level, x, top, z);
            else if (n < 40) set(level, x, top + 1, z, DEAD_FAN[n % 5]);
            else if (n < 42) set(level, x, top + 1, z, seaPickle(1 + n % 3));
        });
    }

    private static void arenaFlora(ServerLevel level) {
        forEachColumn(FLOOR_RADIUS - 1, (x, z) -> {
            int top = topSolid(level, x, z, Y + 4, Y);
            if (top < Y) return;
            BlockState below = getState(level, x, top, z);
            boolean sandy = below.is(Blocks.SAND) || below.is(Blocks.GRAVEL);
            boolean rubble = below.is(MOSSY_COBBLE.getBlock()) || below.is(CRACKED_BRICKS.getBlock())
                    || below.is(CHISELED_TUFF.getBlock());
            if (!sandy && !rubble) return;
            if (!getState(level, x, top + 1, z).is(Blocks.WATER)) return;

            int ax = Math.abs(x), az = Math.abs(z);
            int n = noise(ax * 7 + 3, az * 11 + 5, 100);
            if (n < 10) set(level, x, top + 1, z, SEAGRASS);
            else if (n < 16 && rubble) set(level, x, top + 1, z, DEAD_FAN[n % 5]);
            else if (n < 19) tallSeagrass(level, x, top + 1, z);
            else if (n < 21 && rubble) set(level, x, top + 1, z, seaPickle(1 + n % 3));
        });
    }

    private static void wallEncrust(ServerLevel level) {
        forEachColumn(CLEAR_R, (x, z) -> {
            int r = ringOf(x, z);
            if (r < PODIUM_R0) return;
            int top = topSolid(level, x, z, WATER_TOP, GROUND_FLOOR);
            if (top < GROUND_FLOOR) return;
            if (!isStone(getState(level, x, top, z))) return;
            if (!getState(level, x, top + 1, z).is(Blocks.WATER)) return;

            int ax = Math.abs(x), az = Math.abs(z);
            int n = noise(ax * 7 + top, az * 5 + 3, 100);
            if (n < 5) set(level, x, top + 1, z, seaPickle(1 + noise(ax, az + top, 4)));
            else if (n < 22) set(level, x, top + 1, z, DEAD_FAN[n % 5]);
            else if (n < 28) set(level, x, top + 1, z, LIVE_FAN[n % 5]);
            else if (n < 36) set(level, x, top + 1, z, MOSS_CARPET);
            else if (r > CLEAR_R - 6 && n < 42) kelpColumn(level, x, top + 1, z, 2 + noise(ax + top, az, 4));
        });
    }

    private static void coralClump(ServerLevel level, int x, int top, int z) {
        int n = noise(Math.abs(x) * 9 + 1, Math.abs(z) * 5 + top, 100);
        set(level, x, top, z, n < 70 ? DEAD_CORAL[n % 5] : LIVE_CORAL[n % 5]);
        int m = noise(Math.abs(x) + top, Math.abs(z) * 7, 100);
        if (m < 40) set(level, x, top + 1, z, seaPickle(1 + m % 3));
        else set(level, x, top + 1, z, m < 78 ? DEAD_FAN[m % 5] : LIVE_FAN[m % 5]);
    }

    private static void kelpColumn(ServerLevel level, int x, int y0, int z, int height) {
        int h = Math.max(1, Math.min(height, WATER_TOP - y0 - 2));
        for (int i = 0; i < h; i++) {
            if (!getState(level, x, y0 + i, z).is(Blocks.WATER)) return;
            set(level, x, y0 + i, z, i == h - 1 ? KELP : KELP_PLANT);
        }
    }

    private static void tallSeagrass(ServerLevel level, int x, int y0, int z) {
        if (!getState(level, x, y0 + 1, z).is(Blocks.WATER)) {
            set(level, x, y0, z, SEAGRASS);
            return;
        }
        set(level, x, y0, z, TALL_SEAGRASS.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER));
        set(level, x, y0 + 1, z, TALL_SEAGRASS.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
    }

    private static BlockState seaPickle(int pickles) {
        return Blocks.SEA_PICKLE.defaultBlockState()
                .setValue(SeaPickleBlock.PICKLES, Math.max(1, Math.min(4, pickles)))
                .setValue(SeaPickleBlock.WATERLOGGED, true);
    }

    private static boolean isStone(BlockState state) {
        return state.is(MOSSY_BRICKS.getBlock()) || state.is(CRACKED_BRICKS.getBlock())
                || state.is(BRICKS.getBlock()) || state.is(MOSSY_COBBLE.getBlock())
                || state.is(TUFF_BRICKS.getBlock()) || state.is(CHISELED_TUFF.getBlock())
                || state.is(CHISELED_TUFF_BRICKS.getBlock()) || state.is(PRISMARINE.getBlock());
    }

    private static BlockState weathered(int x, int y, int z) {
        int n = noise(Math.abs(x) * 3 + y, z * 5 + y * 2, 100);
        if (n < 24) return MOSSY_BRICKS;
        if (n < 48) return TUFF_BRICKS;
        if (n < 72) return MOSSY_COBBLE;
        if (n < 80) return CRACKED_BRICKS;
        if (n < 88) return BRICKS;
        if (n < 94) return PRISMARINE;
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

    private static BlockState stairs(BlockState state, Direction facing) {
        return wl(state.setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, Half.BOTTOM));
    }

    private static BlockState wl(BlockState state) {
        return state.setValue(BlockStateProperties.WATERLOGGED, true);
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
            BlockState state = getState(level, x, y, z);
            if (!state.isAir() && !state.is(Blocks.WATER) && state.isCollisionShapeFullBlock(level, bp(x, y, z))) return y;
        }
        return minY - 1;
    }

    private static boolean solid(ServerLevel level, int x, int y, int z) {
        BlockState state = getState(level, x, y, z);
        return !state.isAir() && !state.is(Blocks.WATER) && state.isCollisionShapeFullBlock(level, bp(x, y, z));
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

    private static void forEachSquare(int half, Column action) {
        for (int x = -half; x <= half; x++) {
            for (int z = -half; z <= half; z++) {
                action.at(x, z);
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
        for (int y = y0; y <= y1; y++) set(level, x, y, z, WATER);
    }

    private static BlockState getState(ServerLevel level, int x, int y, int z) {
        return level.getBlockState(bp(x, y, z));
    }

    private static BlockPos bp(int x, int y, int z) {
        return new BlockPos(cx + x, y, z);
    }

    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(bp(x, y, z), state, Block.UPDATE_CLIENTS);
    }
}
