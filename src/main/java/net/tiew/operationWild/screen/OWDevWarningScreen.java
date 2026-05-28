package net.tiew.operationWild.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.tiew.operationWild.core.OWDatasSave;

import java.util.List;

public class OWDevWarningScreen extends Screen {

    private static final int   BG_COLOR       = 0xF0000000;
    private static final int   TITLE_COLOR    = 0x55FF55;
    private static final int   SUBTITLE_COLOR = 0x3DB558;
    private static final int   LINE_COLOR     = 0x44FFFFFF;
    private static final int   TEXT_COLOR     = 0xCCCCCC;
    private static final float TITLE_SCALE    = 2.5f;
    private static final float SUB_SCALE      = 1.4f;

    private static final String[] PARA_KEYS = {
            "screen.ow.dev_warning.para1",
            "screen.ow.dev_warning.para2",
            "screen.ow.dev_warning.para3",
            "screen.ow.dev_warning.para4"
    };

    public OWDevWarningScreen() {
        super(Component.empty());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        int bw = 230, bh = 24;
        addRenderableWidget(Button.builder(
                Component.translatable("screen.ow.dev_warning.button"),
                btn -> {
                    OWDatasSave.markDevWarningSeen();
                    onClose();
                }
        ).bounds((width - bw) / 2, height - 44, bw, bh).build());
    }

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        // Custom background is drawn in render(); suppress the default screen tint
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        // ── Background ─────────────────────────────────────────────────────────
        g.fill(0, 0, width, height, BG_COLOR);

        int cx = width / 2;
        int y  = Math.max(28, height / 7);

        // ── Title "Operation W.I.L.D" ──────────────────────────────────────────
        g.pose().pushPose();
        g.pose().scale(TITLE_SCALE, TITLE_SCALE, 1f);
        g.drawCenteredString(font,
                Component.translatable("screen.ow.dev_warning.title"),
                (int) (cx / TITLE_SCALE), (int) (y / TITLE_SCALE),
                TITLE_COLOR);
        g.pose().popPose();
        y += (int) (font.lineHeight * TITLE_SCALE) + 5;

        // ── Subtitle "EARLY ACCESS / ACCÈS ANTICIPÉ" ───────────────────────────
        g.pose().pushPose();
        g.pose().scale(SUB_SCALE, SUB_SCALE, 1f);
        g.drawCenteredString(font,
                Component.translatable("screen.ow.dev_warning.subtitle"),
                (int) (cx / SUB_SCALE), (int) (y / SUB_SCALE),
                SUBTITLE_COLOR);
        g.pose().popPose();
        y += (int) (font.lineHeight * SUB_SCALE) + 14;

        // ── Separator ──────────────────────────────────────────────────────────
        g.fill(cx - 100, y, cx + 100, y + 1, LINE_COLOR);
        y += 13;

        // ── Body text (each paragraph is word-wrapped and centered) ────────────
        int textW = Math.min(width - 80, 380);
        for (String key : PARA_KEYS) {
            List<FormattedCharSequence> lines = font.split(Component.translatable(key), textW);
            for (FormattedCharSequence line : lines) {
                g.drawString(font, line, cx - font.width(line) / 2, y, TEXT_COLOR);
                y += font.lineHeight + 1;
            }
            y += 7;
        }

        // ── Widgets (button) ───────────────────────────────────────────────────
        super.render(g, mx, my, pt);
    }
}
