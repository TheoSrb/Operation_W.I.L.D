package net.tiew.operationWild.particle;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class EmpressFx {

    public static final int BITE = 0;
    public static final int SLAM = 1;
    public static final int ROOTS = 2;
    public static final int QUAKE = 3;
    public static final int SPORE = 4;
    public static final int SNARE = 5;
    public static final int BURROW = 6;
    public static final int BLOOM = 7;
    public static final int RING = 8;
    public static final int FILL = 9;
    public static final int CHEVRON = 10;
    public static final int SEED = 13;
    public static final int THORNS = 14;
    public static final int SKULL = 15;
    public static final int WAVE = 40;

    public static final int RED = 0xFF3A1E;
    public static final int EARTH = 0xD08A3A;
    public static final int TOXIC = 0x9BEA3A;
    public static final int VINE = 0x45B032;
    public static final int PINK = 0xFF7AB8;
    public static final int WHITE = 0xFFFFFF;

    private static final double VIEW_DISTANCE = 96.0;

    private EmpressFx() {
    }

    public static boolean isDecal(int kind) {
        return kind <= BLOOM;
    }

    public static int colorOf(int kind) {
        return switch (kind) {
            case BITE, SLAM, SKULL, CHEVRON -> RED;
            case ROOTS, QUAKE, BURROW, THORNS -> EARTH;
            case SPORE, SEED -> TOXIC;
            case SNARE -> VINE;
            case BLOOM -> PINK;
            default -> WHITE;
        };
    }

    private static void send(ServerLevel level, EmpressFxOptions options, Vec3 at) {
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(at) <= VIEW_DISTANCE * VIEW_DISTANCE) {
                level.sendParticles(player, options, true, at.x, at.y, at.z, 1, 0, 0, 0, 0);
            }
        }
    }

    public static void decal(ServerLevel level, int kind, Vec3 at, double radius, int life, float yaw) {
        send(level, new EmpressFxOptions(kind, colorOf(kind), (float) radius, Math.max(2, life), yaw), at);
    }

    public static void tracked(ServerLevel level, int kind, Entity target, Vec3 at, double radius, int life, int lockAfter, float yaw) {
        int color = colorOf(kind) | (Math.max(0, Math.min(255, lockAfter)) << 24);
        send(level, new EmpressFxOptions(kind, color, (float) radius, Math.max(2, life), yaw, target.getId()), at);
    }

    public static void wave(ServerLevel level, Vec3 at, double radius, int color, int life) {
        send(level, new EmpressFxOptions(WAVE, color, (float) radius, life, 0f), at);
    }
}
