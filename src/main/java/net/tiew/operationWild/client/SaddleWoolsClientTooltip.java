package net.tiew.operationWild.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.item.custom.SaddleWoolsTooltip;

@OnlyIn(Dist.CLIENT)
public class SaddleWoolsClientTooltip implements ClientTooltipComponent {

    private static final int SQUARE = 9;
    private static final int GAP = 3;

    private final SaddleWoolsTooltip tooltip;

    public SaddleWoolsClientTooltip(SaddleWoolsTooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public int getHeight() {
        return SQUARE + 3;
    }

    @Override
    public int getWidth(Font font) {
        return SQUARE * 2 + GAP;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        drawSquare(graphics, x, y, tooltip.primary());
        drawSquare(graphics, x + SQUARE + GAP, y, tooltip.secondary());
    }

    private void drawSquare(GuiGraphics graphics, int x, int y, int rgb) {
        graphics.fill(x - 1, y - 1, x + SQUARE + 1, y + SQUARE + 1, 0xFF2B2B34);
        graphics.fill(x, y, x + SQUARE, y + SQUARE, 0xFF000000 | rgb);
        graphics.fill(x, y, x + SQUARE, y + 1, 0x55FFFFFF);
        graphics.fill(x, y + SQUARE - 1, x + SQUARE, y + SQUARE, 0x44000000);
    }
}
