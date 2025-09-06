package net.tiew.operationWild.screen.player.adventurer_manuscript.text;

import net.minecraft.network.chat.TextColor;

public class StyledTextSegment {
    public final String text;
    public final boolean isBold;
    public final boolean isUnderlined;
    public final TextColor color;

    public StyledTextSegment(String text, boolean isBold, boolean isUnderlined, TextColor color) {
        this.text = text;
        this.isBold = isBold;
        this.isUnderlined = isUnderlined;
        this.color = color;
    }
}
