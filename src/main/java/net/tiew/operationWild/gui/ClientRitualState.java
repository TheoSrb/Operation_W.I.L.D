package net.tiew.operationWild.gui;

import net.tiew.operationWild.networking.packets.to_client.RitualSyncPacket;

/**
 * État client du Rituel de Communion en cours, alimenté par {@link RitualSyncPacket} et lu par
 * l'overlay HUD {@link RitualOverlay}.
 */
public final class ClientRitualState {

    private ClientRitualState() {}

    public static boolean active = false;
    public static float materialization = 0f;
    public static float stability = 0f;
    public static int currentWave = 0;
    public static int totalWaves = 0;
    /** Phase musicale : 0 = intro, 1 = combat, 2 = outro. */
    public static int phase = 0;

    public static void update(RitualSyncPacket packet) {
        active = packet.active();
        materialization = packet.materialization();
        stability = packet.stability();
        currentWave = packet.currentWave();
        totalWaves = packet.totalWaves();
        phase = packet.phase();
    }
}
