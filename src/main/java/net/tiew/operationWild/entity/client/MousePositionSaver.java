package net.tiew.operationWild.entity.client;

public class MousePositionSaver {
    public static double savedX = -1;
    public static double savedY = -1;
    public static boolean shouldRestore = false;

    public static void save(double x, double y) {
        savedX = x;
        savedY = y;
        shouldRestore = true;
    }

    public static void clear() {
        shouldRestore = false;
    }
}