package net.tiew.operationWild.screen.player.adventurer_manuscript.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;
import java.util.List;

public class OWTextParser {
    public static List<StyledTextSegment> parseStyledText(Component component, int color) {
        List<StyledTextSegment> segments = new ArrayList<>();
        String fullText = component.getString();

        if (fullText.contains("§")) {
            parseMinecraftFormattingCodes(fullText, segments, color);
        } else {
            parseComponentRecursive(component, segments, Style.EMPTY);
        }
        return segments;
    }

    public static List<StyledTextSegment> parseStyledText(String string, int color) {
        List<StyledTextSegment> segments = new ArrayList<>();

        if (string.contains("§")) {
            parseMinecraftFormattingCodes(string, segments, color);
        } else {
            parseComponentRecursive(string, segments, Style.EMPTY);
        }
        return segments;
    }

    private static void parseMinecraftFormattingCodes(String text, List<StyledTextSegment> segments, int color) {
        boolean isBold = false;
        boolean isUnderlined = false;
        TextColor currentColor = null;
        StringBuilder currentText = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '§' && i + 1 < text.length()) {
                if (currentText.length() > 0) {
                    segments.add(new StyledTextSegment(currentText.toString(), isBold, isUnderlined, currentColor));
                    currentText.setLength(0);
                }

                char formatCode = text.charAt(i + 1);
                switch (formatCode) {
                    case 'l': isBold = true; break;
                    case 'n': isUnderlined = true; break;
                    case 'r': isBold = false; isUnderlined = false; currentColor = null; break;
                    case '0': currentColor = TextColor.fromRgb(blendColors(0xFFFFFF, color)); break;
                    case '1': currentColor = TextColor.fromRgb(blendColors(0xFFFF55, color)); break;
                    case '2': currentColor = TextColor.fromRgb(blendColors(0xFF55FF, color)); break;
                    case '3': currentColor = TextColor.fromRgb(blendColors(0xFF5555, color)); break;
                    case '4': currentColor = TextColor.fromRgb(blendColors(0x55FFFF, color)); break;
                    case '5': currentColor = TextColor.fromRgb(blendColors(0x55FF55, color)); break;
                    case '6': currentColor = TextColor.fromRgb(blendColors(0x0055FF, color)); break;
                    case '7': currentColor = TextColor.fromRgb(blendColors(0x555555, color)); break;
                    case '8': currentColor = TextColor.fromRgb(blendColors(0xAAAAAA, color)); break;
                    case '9': currentColor = TextColor.fromRgb(blendColors(0xAA0000, color)); break;
                    case 'a': currentColor = TextColor.fromRgb(blendColors(0xAA00AA, color)); break;
                    case 'b': currentColor = TextColor.fromRgb(blendColors(0xAA0000, color)); break;
                    case 'c': currentColor = TextColor.fromRgb(blendColors(0x00AAAA, color)); break;
                    case 'd': currentColor = TextColor.fromRgb(blendColors(0x00AA00, color)); break;
                    case 'e': currentColor = TextColor.fromRgb(blendColors(0x0000AA, color)); break;
                    case 'f': currentColor = TextColor.fromRgb(blendColors(0x000000, color)); break;
                }
                i++;
            } else {
                currentText.append(c);
            }
        }

        if (currentText.length() > 0) {
            segments.add(new StyledTextSegment(currentText.toString(), isBold, isUnderlined, currentColor));
        }
    }

    private static int blendColors(int minecraftColor, int parameterColor) {
        int mcR = (minecraftColor >> 16) & 0xFF;
        int mcG = (minecraftColor >> 8) & 0xFF;
        int mcB = minecraftColor & 0xFF;

        int paramR = (parameterColor >> 16) & 0xFF;
        int paramG = (parameterColor >> 8) & 0xFF;
        int paramB = parameterColor & 0xFF;

        float mcWeight = 0.6f;
        float paramWeight = 0.4f;

        int blendedR = (int)(mcR * mcWeight + paramR * paramWeight);
        int blendedG = (int)(mcG * mcWeight + paramG * paramWeight);
        int blendedB = (int)(mcB * mcWeight + paramB * paramWeight);

        blendedR = Math.max(0, Math.min(255, blendedR));
        blendedG = Math.max(0, Math.min(255, blendedG));
        blendedB = Math.max(0, Math.min(255, blendedB));

        return (blendedR << 16) | (blendedG << 8) | blendedB;
    }

    private static void parseComponentRecursive(Component component, List<StyledTextSegment> segments, Style parentStyle) {
        Style currentStyle = parentStyle.applyTo(component.getStyle());
        String content = component.getContents().toString();

        if (!content.isEmpty()) {
            boolean isBold = currentStyle.isBold();
            boolean isUnderlined = currentStyle.isUnderlined();
            TextColor color = currentStyle.getColor();
            segments.add(new StyledTextSegment(content, isBold, isUnderlined, color));
        }

        for (Component sibling : component.getSiblings()) {
            parseComponentRecursive(sibling, segments, currentStyle);
        }
    }
    private static void parseComponentRecursive(String text, List<StyledTextSegment> segments, Style parentStyle) {
        if (!text.isEmpty()) {
            boolean isBold = parentStyle.isBold();
            boolean isUnderlined = parentStyle.isUnderlined();
            TextColor color = parentStyle.getColor();
            segments.add(new StyledTextSegment(text, isBold, isUnderlined, color));
        }
    }
}
