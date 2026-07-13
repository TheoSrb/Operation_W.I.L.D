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
 * Popup « +N » avec l'icône d'orbe d'XP qui apparaît brièvement, monte et s'estompe lors d'un gain
 * d'expérience de quête. Pendant de {@link OWCoinGainOverlay}, déclenché par {@code OWXpGainPacket}.
 */
public class OWXpGainOverlay {

    private static ResourceLocation exp(int amount) {
        String p = amount >= 17 ? "textures/misc/exp_4.png"
                 : amount >= 13 ? "textures/misc/exp_3.png"
                 : amount >= 9  ? "textures/misc/exp_2.png"
                 :                "textures/misc/exp.png";
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, p);
    }

    private static long startTime = -1L;
    private static int amount = 0;

    private static final long DURATION_MS = 1600L;
    private static final int TEXT_COLOR = 0x8BE45A;

    public static void trigger(int gained) {
        if (gained <= 0) return;
        if (startTime >= 0 && System.currentTimeMillis() - startTime < DURATION_MS) {
            amount += gained;
        } else {
            amount = gained;
        }
        startTime = System.currentTimeMillis();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.1f));
        }
    }

    public static void render(GuiGraphics g, int screenW, int screenH) {
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

        // Légèrement au-dessus du popup de pièces pour ne jamais se superposer.
        float cx = screenW / 2f;
        float cy = screenH * 0.54f + drift;

        RenderSystem.enableBlend();
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0);
        g.pose().scale(scale, scale, 1f);
        g.pose().translate(-blockW / 2f, 0, 0);

        int a = ((int) (alpha * 255) & 0xFF) << 24;
        g.setColor(1f, 1f, 1f, alpha);
        g.blit(exp(amount), 0, -2, iconSize, iconSize, 0f, 0f, 16, 16, 16, 16);
        g.setColor(1f, 1f, 1f, 1f);
        g.drawString(font, txt, iconSize + gap, 0, TEXT_COLOR | a, true);

        g.pose().popPose();
        RenderSystem.disableBlend();
    }
}
