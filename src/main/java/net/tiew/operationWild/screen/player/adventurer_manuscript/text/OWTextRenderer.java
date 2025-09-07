package net.tiew.operationWild.screen.player.adventurer_manuscript.text;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.network.chat.TextColor;
import net.tiew.operationWild.core.OWTextureWriter;

import java.util.List;

public class OWTextRenderer {

    public static void processStyledTextWithLineBreaks(NativeImage image, List<StyledTextSegment> segments,
                                                       int startX, int startY, float alpha, int maxLineWidth, float scale, int colorWanted) {
        int scaledLineSpacing = (int)(12 * scale);
        int currentX = startX;
        int currentY = startY;

        for (StyledTextSegment segment : segments) {
            String[] words = segment.text.split(" ");

            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                int wordWidth = calculateStyledTextWidth(word, segment.isBold, segment.isUnderlined, scale);
                int spaceWidth = (currentX > startX) ? calculateStyledTextWidth(" ", segment.isBold, segment.isUnderlined, scale) : 0;

                if (currentX + spaceWidth + wordWidth > startX + maxLineWidth && currentX > startX) {
                    currentX = startX;
                    currentY += scaledLineSpacing;
                }

                if (currentX > startX) {
                    currentX += spaceWidth;
                }

                int color = createColorWithAlpha(alpha, segment.color, segment.isBold, colorWanted);

                int wordStartX = currentX;
                writeStyledTextDirectlyOnImage(image, word, currentX, currentY, color, segment.isBold, false, scale);

                if (segment.isUnderlined) {
                    drawWordUnderline(image, wordStartX, currentY, wordWidth, color, segment.isBold, scale);
                }

                currentX += wordWidth;
            }
        }
    }

    private static int createColorWithAlpha(float alpha, TextColor textColor, boolean isBold, int color) {
        int alphaValue = (int)(alpha * 255);
        int baseColor = (textColor != null) ? textColor.getValue() : swapRedBlue(color);
        if (isBold) baseColor = darkenColor(baseColor, 0.8f);
        return (alphaValue << 24) | (baseColor & 0x00FFFFFF);
    }

    private static int swapRedBlue(int color) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        return (blue << 16) | (green << 8) | red;
    }

    private static int darkenColor(int color, float factor) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        red = Math.max(0, Math.min(255, (int)(red * factor)));
        green = Math.max(0, Math.min(255, (int)(green * factor)));
        blue = Math.max(0, Math.min(255, (int)(blue * factor)));
        return (red << 16) | (green << 8) | blue;
    }

    private static void writeStyledTextDirectlyOnImage(NativeImage image, String text, int x, int y, int color, boolean isBold, boolean isUnderlined, float scale) {
        int currentX = x;
        for (char c : text.toCharArray()) {
            boolean[][] pattern = OWTextureWriter.FONT_DATA.get(c);
            if (pattern != null) {
                drawCharacterPattern(image, pattern, currentX, y, color, isBold, scale);

                currentX += (int)(pattern[0].length * scale) + (int)(1 * scale);
                if (isBold) currentX += (int)(1 * scale);
            }
        }
    }

    private static void drawWordUnderline(NativeImage image, int startX, int baseY, int wordWidth, int color, boolean isBold, float scale) {
        int charHeight = getCharacterHeight(scale);
        int underlineY = baseY + charHeight + (int)(1 * scale);

        int thickness = isBold ? (int)Math.max(1, 2 * scale) : (int)Math.max(1, 1 * scale);

        for (int t = 0; t < thickness; t++) {
            for (int x = startX; x < startX + wordWidth; x++) {
                int yPos = underlineY + t;
                if (isValidPixelPosition(image, x, yPos)) {
                    image.setPixelRGBA(x, yPos, color);
                }
            }
        }
    }

    private static int getCharacterHeight(float scale) {
        boolean[][] pattern = OWTextureWriter.FONT_DATA.get('A');
        if (pattern != null) {
            return (int)(pattern.length * scale);
        }
        return (int)(8 * scale);
    }

    private static void drawUnderline(NativeImage image, int startX, int underlineY, int width, int color, boolean isBold, float scale) {
        int thickness = isBold ? (int)Math.max(1, 2 * scale) : (int)Math.max(1, 1 * scale);

        for (int t = 0; t < thickness; t++) {
            for (int x = startX; x < startX + width; x++) {
                int yPos = underlineY + t;
                if (isValidPixelPosition(image, x, yPos)) {
                    image.setPixelRGBA(x, yPos, color);
                }
            }
        }
    }

    private static void drawCharacterPattern(NativeImage image, boolean[][] pattern, int startX, int startY, int color, boolean isBold, float scale) {
        for (int row = 0; row < pattern.length; row++) {
            for (int col = 0; col < pattern[row].length; col++) {
                if (pattern[row][col]) {
                    int scaledPixelSize = Math.max(1, (int)scale);

                    for (int sy = 0; sy < scaledPixelSize; sy++) {
                        for (int sx = 0; sx < scaledPixelSize; sx++) {
                            int pixelX = startX + (int)(col * scale) + sx;
                            int pixelY = startY + (int)(row * scale) + sy;

                            if (isValidPixelPosition(image, pixelX, pixelY)) {
                                image.setPixelRGBA(pixelX, pixelY, color);
                            }

                            if (isBold) {
                                int boldPixelX = pixelX + scaledPixelSize;
                                if (isValidPixelPosition(image, boldPixelX, pixelY)) {
                                    image.setPixelRGBA(boldPixelX, pixelY, color);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isValidPixelPosition(NativeImage image, int x, int y) {
        return x >= 0 && y >= 0 && x < image.getWidth() && y < image.getHeight();
    }

    private static int calculateStyledTextWidth(String text, boolean isBold, boolean isUnderlined) {
        return calculateStyledTextWidth(text, isBold, isUnderlined, 1.0f);
    }

    private static int calculateStyledTextWidth(String text, boolean isBold, boolean isUnderlined, float scale) {
        int totalWidth = 0;
        for (char c : text.toCharArray()) {
            boolean[][] pattern = OWTextureWriter.FONT_DATA.get(c);
            if (pattern != null) {
                int charWidth = (int)(pattern[0].length * scale);
                if (isBold) charWidth += (int)(1 * scale);
                totalWidth += charWidth + (int)(1 * scale);
            }
        }
        return totalWidth > 0 ? totalWidth - (int)(1 * scale) : 0;
    }
}