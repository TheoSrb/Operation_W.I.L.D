package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.SelectMusicEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.tiew.operationWild.OperationWild;

/**
 * HUD du Rituel de Communion : jauge de matérialisation + jauge de stabilité de l'âme + vague
 * en cours. Affiché en haut de l'écran tant qu'un rituel est actif ({@link ClientRitualState}).
 */
@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class RitualOverlay {

    private static final int BAR_W = 182;
    private static final int BAR_H = 9;

    /** Aucune musique vanilla pendant le rituel : seule la musique de Communion joue. */
    @SubscribeEvent
    public static void onSelectMusic(SelectMusicEvent event) {
        if (ClientRitualState.active) {
            event.overrideMusic(null);
        }
    }

    /**
     * Pilotage de la musique au tick CLIENT (hors tick du moteur audio → {@code play()} sûr).
     * Gère le démarrage par phase et la boucle de combat anticipée.
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (ClientRitualState.active) {
            RitualMusicInstance.update(ClientRitualState.phase);
            RitualMusicInstance.onCombatLoopTick();
        } else {
            RitualMusicInstance.stopAll();
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiLayerEvent.Post event) {
        if (!ClientRitualState.active) return;
        if (!event.getName().equals(VanillaGuiLayers.HOTBAR)) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        // Phase outro (succès) : l'animal est déjà revenu → on masque entièrement le HUD du rituel.
        if (ClientRitualState.phase == 2) return;

        GuiGraphics g = event.getGuiGraphics();
        Font font = mc.font;
        int cx = g.guiWidth() / 2;
        int top = 12;

        Component title = Component.translatable("ritual.ow.hud_title");
        g.drawCenteredString(font, title, cx, top, 0x86DBFF);

        // Phase intro : message d'attente, pas de barres ni de vague.
        if (ClientRitualState.phase == 0) {
            g.drawCenteredString(font, Component.translatable("ritual.ow.hud_intro"), cx, top + 11, 0xC8C8C8);
            return;
        }

        // Phase combat : vague + jauges.
        Component wave = Component.translatable("ritual.ow.hud_wave", ClientRitualState.currentWave, ClientRitualState.totalWaves);
        g.drawCenteredString(font, wave, cx, top + 11, 0xFF8888);

        int barX = cx - BAR_W / 2;
        int matY = top + 24;
        int stabY = matY + BAR_H + 8;

        // Matérialisation (cyan).
        drawBar(g, barX, matY, ClientRitualState.materialization, 0xFF101418, 0xFF86DBFF);
        g.drawString(font, Component.translatable("ritual.ow.hud_materialization"), barX, matY - 9, 0x86DBFF, true);

        // Stabilité de l'âme (vert → rouge selon le niveau).
        float stab = Mth.clamp(ClientRitualState.stability, 0f, 1f);
        int stabColor = lerpColor(0xFFD94747, 0xFF6AE36A, stab);
        drawBar(g, barX, stabY, stab, 0xFF101418, stabColor);
        g.drawString(font, Component.translatable("ritual.ow.hud_stability"), barX, stabY - 9, 0xCCCCCC, true);
    }

    private static void drawBar(GuiGraphics g, int x, int y, float fraction, int bgColor, int fgColor) {
        fraction = Mth.clamp(fraction, 0f, 1f);
        g.fill(x - 1, y - 1, x + BAR_W + 1, y + BAR_H + 1, 0xFF000000);
        g.fill(x, y, x + BAR_W, y + BAR_H, bgColor);
        g.fill(x, y, x + (int) (BAR_W * fraction), y + BAR_H, fgColor);
    }

    private static int lerpColor(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = (int) (ar + (br - ar) * t);
        int gg = (int) (ag + (bg - ag) * t);
        int bl = (int) (ab + (bb - ab) * t);
        return 0xFF000000 | (r << 16) | (gg << 8) | bl;
    }
}
