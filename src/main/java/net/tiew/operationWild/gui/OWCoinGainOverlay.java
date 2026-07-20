package net.tiew.operationWild.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.tiew.operationWild.OperationWild;

/**
 * Petit popup "+N" avec l'icône Pièce Sauvage qui apparaît brièvement, monte et s'estompe lors d'un gain.
 * Déclenché par {@code OWCoinsSyncPacket} (gained &gt; 0).
 */
public class OWCoinGainOverlay {

    private static final ResourceLocation COIN =
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/misc/coin.png");

    private static long startTime = -1L;
    private static int amount = 0;

    private static final long DURATION_MS = 1600L;
    private static final int TEXT_COLOR = 0x6E8751;

    /** Gain reçu pendant une cinématique, mis en attente jusqu'à ce qu'elle s'achève. */
    private static int deferred = 0;

    public static void trigger(int gained) {
        if (gained <= 0) return;

        // Un coffre crédite ses Pièces Sauvages dès l'instant où le serveur valide l'ouverture,
        // c'est-à-dire pendant le tremblement — le popup révélerait donc le montant bien avant que
        // le coffre ne s'ouvre. On attend la fin de la cinématique pour l'annoncer.
        if (OWCinematicState.anyPlaying()) {
            deferred += gained;
            return;
        }
        start(gained);
    }

    private static void start(int gained) {
        // Cumule si une animation est déjà en cours.
        if (startTime >= 0 && System.currentTimeMillis() - startTime < DURATION_MS) {
            amount += gained;
        } else {
            amount = gained;
        }
        startTime = System.currentTimeMillis();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.4f));
        }
    }

    public static void render(GuiGraphics g, int screenW, int screenH) {
        // Cinématique terminée : on libère le gain mis en attente.
        if (deferred > 0 && !OWCinematicState.anyPlaying()) {
            int pending = deferred;
            deferred = 0;
            start(pending);
        }
        if (startTime < 0) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        long el = System.currentTimeMillis() - startTime;
        if (el > DURATION_MS) {
            startTime = -1L;
            return;
        }
        float t = el / (float) DURATION_MS;

        float alpha;
        if (t < 0.10f)      alpha = t / 0.10f;
        else if (t > 0.60f) alpha = Mth.clamp(1f - (t - 0.60f) / 0.40f, 0f, 1f);
        else                alpha = 1f;

        float pop = 1f - (float) Math.pow(1f - Math.min(t / 0.18f, 1f), 3);
        float scale = 0.85f + 0.35f * pop;
        float drift = -t * 22f;

        Font font = mc.font;
        String txt = "+" + amount;
        int iconSize = 11;
        int gap = 2;
        float blockW = iconSize + gap + font.width(txt);

        float cx = screenW / 2f;
        float cy = screenH * 0.60f + drift;

        RenderSystem.enableBlend();
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0);
        g.pose().scale(scale, scale, 1f);
        g.pose().translate(-blockW / 2f, 0, 0);

        int a = ((int) (alpha * 255) & 0xFF) << 24;
        g.setColor(1f, 1f, 1f, alpha);
        g.blit(COIN, 0, -2, iconSize, iconSize, 0f, 0f, 16, 16, 16, 16);
        g.setColor(1f, 1f, 1f, 1f);
        g.drawString(font, txt, iconSize + gap, 0, TEXT_COLOR | a, true);

        g.pose().popPose();
        RenderSystem.disableBlend();
    }
}
