package net.tiew.operationWild.screen.player.adventurer_manuscript.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;
import java.util.List;

public class OWTextParser {
    public static List<StyledTextSegment> parseStyledText(Component component) {
        List<StyledTextSegment> segments = new ArrayList<>();
        String fullText = component.getString();

        if (fullText.contains("§")) {
            parseMinecraftFormattingCodes(fullText, segments);
        } else {
            parseComponentRecursive(component, segments, Style.EMPTY);
        }
        return segments;
    }

    public static List<StyledTextSegment> parseStyledText(String string) {
        List<StyledTextSegment> segments = new ArrayList<>();

        if (string.contains("§")) {
            parseMinecraftFormattingCodes(string, segments);
        } else {
            parseComponentRecursive(string, segments, Style.EMPTY);
        }
        return segments;
    }

    private static void parseMinecraftFormattingCodes(String text, List<StyledTextSegment> segments) {
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
                    case '0': currentColor = TextColor.fromRgb(0xFFFFFF); break;
                    case '1': currentColor = TextColor.fromRgb(0xFFFF55); break;
                    case '2': currentColor = TextColor.fromRgb(0xFF55FF); break;
                    case '3': currentColor = TextColor.fromRgb(0xFF5555); break;
                    case '4': currentColor = TextColor.fromRgb(0x55FFFF); break;
                    case '5': currentColor = TextColor.fromRgb(0x55FF55); break;
                    case '6': currentColor = TextColor.fromRgb(0x0055FF); break;
                    case '7': currentColor = TextColor.fromRgb(0x555555); break;
                    case '8': currentColor = TextColor.fromRgb(0xAAAAAA); break;
                    case '9': currentColor = TextColor.fromRgb(0xAA0000); break;
                    case 'a': currentColor = TextColor.fromRgb(0xAA00AA); break;
                    case 'b': currentColor = TextColor.fromRgb(0xAA0000); break;
                    case 'c': currentColor = TextColor.fromRgb(0x00AAAA); break;
                    case 'd': currentColor = TextColor.fromRgb(0x00AA00); break;
                    case 'e': currentColor = TextColor.fromRgb(0x0000AA); break;
                    case 'f': currentColor = TextColor.fromRgb(0x000000); break;
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
