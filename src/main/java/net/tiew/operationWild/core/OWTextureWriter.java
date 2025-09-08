package net.tiew.operationWild.core;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class OWTextureWriter {
    public static final Map<Character, boolean[][]> FONT_DATA = new HashMap<>();

    static {
        FONT_DATA.put('A', createNewLetter(" /// ", "/   /", "/////", "/   /", "/   /", "/   /", "/   /"));
        FONT_DATA.put('B', createNewLetter("//// ", "/   /", "//// ", "/   /", "/   /", "/   /", "//// "));
        FONT_DATA.put('C', createNewLetter(" /// ", "/   /", "/    ", "/    ", "/    ", "/   /", " /// "));
        FONT_DATA.put('D', createNewLetter("//// ", "/   /", "/   /", "/   /", "/   /", "/   /", "//// "));
        FONT_DATA.put('E', createNewLetter("/////", "/    ", "///  ", "/    ", "/    ", "/    ", "/////"));
        FONT_DATA.put('F', createNewLetter("/////", "/    ", "///  ", "/    ", "/    ", "/    ", "/    "));
        FONT_DATA.put('G', createNewLetter(" ////", "/    ", "/  //", "/   /", "/   /", "/   /", " /// "));
        FONT_DATA.put('H', createNewLetter("/   /", "/   /", "/////", "/   /", "/   /", "/   /", "/   /"));
        FONT_DATA.put('I', createNewLetter("///  ", " /   ", " /   ", " /   ", " /   ", " /   ", "///  "));
        FONT_DATA.put('J', createNewLetter("    /", "    /", "    /", "    /", "    /", "/   /", " /// "));
        FONT_DATA.put('K', createNewLetter("/    ", "/   /", "/  / ", "///  ", "/  / ", "/   /", "/   /"));
        FONT_DATA.put('L', createNewLetter("/    ", "/    ", "/    ", "/    ", "/    ", "/    ", "/////"));
        FONT_DATA.put('M', createNewLetter("/   /", "// //", "/ / /", "/   /", "/   /", "/   /", "/   /"));
        FONT_DATA.put('N', createNewLetter("/   /", "//  /", "/ / /", "/  //", "/   /", "/   /", "/   /"));
        FONT_DATA.put('O', createNewLetter(" /// ", "/   /", "/   /", "/   /", "/   /", "/   /", " /// "));




        FONT_DATA.put('a', createNewLetter("     ", "     ", " /// ", "    /", " ////", "/   /", " ////"));
        FONT_DATA.put('b', createNewLetter("/    ", "/    ", "/ // ", "//  /", "/   /", "/   /", "////"));
        FONT_DATA.put('c', createNewLetter("     ", "     ", " /// ", "/   /", "/    ", "/   /", " /// "));
        FONT_DATA.put('d', createNewLetter("    /", "    /", " // /", "/  //", "/   /", "/   /", " ////"));
        FONT_DATA.put('e', createNewLetter("     ", "     ", " /// ", "/   /", "/////", "/    ", " ////"));
        FONT_DATA.put('f', createNewLetter("   //", "  /  ", " ////", "  /  ", "  /  ", "  /  ", "  /  "));
        FONT_DATA.put('g', createNewLetter("     ", "     ", " ////", "/   /", "/   /", " ////", "    /", "//// "));
        FONT_DATA.put('h', createNewLetter("/    ", "/    ", "/ // ", "//  /", "/   /", "/   /", "/   /"));
        FONT_DATA.put('i', createNewLetter(" / ", "   ", " / ", " / ", " / ", " / ", " / "));
        FONT_DATA.put('j', createNewLetter("    /", "     ", "    /", "    /", "    /", "/   /", "/   /", " /// "));
        FONT_DATA.put('k', createNewLetter(" /   ", " /   ", " /  /", " / / ", " //  ", " / / ", " /  / "));
        FONT_DATA.put('l', createNewLetter("/ ", "/ ", "/ ", "/ ", "/ ", "/ ", " /"));
        FONT_DATA.put('m', createNewLetter("     ", "     ", "// / ", "/ / /", "/ / /", "/   /", "/   /"));
        FONT_DATA.put('n', createNewLetter("     ", "     ", "//// ", "/   /", "/   /", "/   /", "/   /"));
        FONT_DATA.put('o', createNewLetter("     ", "     ", " /// ", "/   /", "/   /", "/   /", " /// "));
        FONT_DATA.put('p', createNewLetter("     ", "     ", "/ // ", "//  /", "/   /", "//// ", "/    ", "/    "));
        FONT_DATA.put('q', createNewLetter("     ", "     ", " // /", "/  //", "/   /", " ////", "    /", "    /"));
        FONT_DATA.put('r', createNewLetter("     ", "     ", "/ // ", "//  /", "/    ", "/    ", "/    "));
        FONT_DATA.put('s', createNewLetter("     ", "     ", " ////", "/    ", " /// ", "    /", "//// "));
        FONT_DATA.put('t', createNewLetter(" / ", " / ", "///", " / ", " / ", " / ", "  /"));
        FONT_DATA.put('u', createNewLetter("     ", "     ", "/   /", "/   /", "/   /", "/   /", " ////"));
        FONT_DATA.put('v', createNewLetter("     ", "     ", "/   /", "/   /", "/   /"," / / ", "  /  "));
        FONT_DATA.put('w', createNewLetter("     ", "     ", "/   /", "/   /", "/ / /", "/ / /", " ////"));
        FONT_DATA.put('x', createNewLetter("     ", "     ", "/   /", " / / ", "  /  ", " / / ", "/   /"));
        FONT_DATA.put('y', createNewLetter("     ", "     ", "/   /", "/   /", "/   /", " ////", "    /", "//// "));
        FONT_DATA.put('z', createNewLetter("     ", "     ", "/////", "   / ", "  /  ", " /   ", "/////"));

        FONT_DATA.put('é', createNewLetter("   / ", "  /  ", " /// ", "/   /", "/////", "/    ", " ////"));
        FONT_DATA.put('è', createNewLetter("  /  ", "   / ", " /// ", "/   /", "/////", "/    ", " ////"));
        FONT_DATA.put('ê', createNewLetter("  /  ", " / / ", " /// ", "/   /", "/////", "/    ", " ////"));

        FONT_DATA.put('à', createNewLetter("  /  ", "   / ", " /// ", "    /", " ////", "/   /", " ////"));
        FONT_DATA.put('â', createNewLetter("  /  ", " / / ", " /// ", "    /", " ////", "/   /", " ////"));

        FONT_DATA.put('ô', createNewLetter("  /  ", " / / ", " /// ", "/   /", "/   /", "/   /", " /// "));
        FONT_DATA.put('ö', createNewLetter(" / / ", "     ", " /// ", "/   /", "/   /", "/   /", " /// "));

        FONT_DATA.put('ï', createNewLetter("/ /", "   ", " / ", " / ", " / ", " / ", " / "));




        FONT_DATA.put('0', new boolean[][]{{false, true, true, true, false}, {true, false, false, false, true}, {true, false, false, false, true}, {true, false, false, false, true}, {false, true, true, true, false}});
        FONT_DATA.put('1', new boolean[][]{{false, true, false}, {true, true, false}, {false, true, false}, {false, true, false}, {true, true, true}});
        FONT_DATA.put('2', new boolean[][]{{false, true, true, true, false}, {true, false, false, false, true}, {false, false, true, true, false}, {false, true, false, false, false}, {true, true, true, true, true}});
        FONT_DATA.put('3', new boolean[][]{{false, true, true, true, false}, {true, false, false, false, true}, {false, false, true, true, false}, {true, false, false, false, true}, {false, true, true, true, false}});
        FONT_DATA.put('4', new boolean[][]{{true, false, false, true, false}, {true, false, false, true, false}, {true, true, true, true, true}, {false, false, false, true, false}, {false, false, false, true, false}});
        FONT_DATA.put('5', new boolean[][]{{true, true, true, true, true}, {true, false, false, false, false}, {true, true, true, true, false}, {false, false, false, false, true}, {true, true, true, true, false}});
        FONT_DATA.put('6', new boolean[][]{{false, true, true, true, false}, {true, false, false, false, false}, {true, true, true, true, false}, {true, false, false, false, true}, {false, true, true, true, false}});
        FONT_DATA.put('7', new boolean[][]{{true, true, true, true, true}, {false, false, false, false, true}, {false, false, false, true, false}, {false, false, true, false, false}, {false, true, false, false, false}});
        FONT_DATA.put('8', new boolean[][]{{false, true, true, true, false}, {true, false, false, false, true}, {false, true, true, true, false}, {true, false, false, false, true}, {false, true, true, true, false}});
        FONT_DATA.put('9', new boolean[][]{{false, true, true, true, false}, {true, false, false, false, true}, {false, true, true, true, true}, {false, false, false, false, true}, {false, true, true, true, false}});

        FONT_DATA.put(' ', new boolean[][]{{false, false, false, false}, {false, false, false, false}, {false, false, false, false}, {false, false, false, false}, {false, false, false, false}});

        FONT_DATA.put('À', new boolean[][]{{false, true, false, false, false}, {false, false, false, false, false}, {false, true, true, true, false}, {true, false, false, false, true}, {true, true, true, true, true}, {true, false, false, false, true}});
        FONT_DATA.put('Á', new boolean[][]{{false, false, false, true, false}, {false, false, false, false, false}, {false, true, true, true, false}, {true, false, false, false, true}, {true, true, true, true, true}, {true, false, false, false, true}});
        FONT_DATA.put('Â', new boolean[][]{{false, false, true, false, false}, {false, true, false, true, false}, {false, true, true, true, false}, {true, false, false, false, true}, {true, true, true, true, true}, {true, false, false, false, true}});
        FONT_DATA.put('Ä', new boolean[][]{{false, true, false, true, false}, {false, false, false, false, false}, {false, true, true, true, false}, {true, false, false, false, true}, {true, true, true, true, true}, {true, false, false, false, true}});

        FONT_DATA.put('È', new boolean[][]{{false, true, false, false, false}, {false, false, false, false, false}, {true, true, true, true, true}, {true, false, false, false, false}, {true, true, true, false, false}, {true, false, false, false, false}, {true, true, true, true, true}});
        FONT_DATA.put('É', new boolean[][]{{false, false, false, true, false}, {false, false, false, false, false}, {true, true, true, true, true}, {true, false, false, false, false}, {true, true, true, false, false}, {true, false, false, false, false}, {true, true, true, true, true}});
        FONT_DATA.put('Ê', new boolean[][]{{false, false, true, false, false}, {false, true, false, true, false}, {true, true, true, true, true}, {true, false, false, false, false}, {true, true, true, false, false}, {true, false, false, false, false}, {true, true, true, true, true}});
        FONT_DATA.put('Ë', new boolean[][]{{false, true, false, true, false}, {false, false, false, false, false}, {true, true, true, true, true}, {true, false, false, false, false}, {true, true, true, false, false}, {true, false, false, false, false}, {true, true, true, true, true}});

        FONT_DATA.put('Ì', new boolean[][]{{true, false, false}, {false, false, false}, {true, true, true}, {false, true, false}, {false, true, false}, {false, true, false}, {true, true, true}});
        FONT_DATA.put('Í', new boolean[][]{{false, false, true}, {false, false, false}, {true, true, true}, {false, true, false}, {false, true, false}, {false, true, false}, {true, true, true}});
        FONT_DATA.put('Î', new boolean[][]{{false, true, false}, {true, false, true}, {true, true, true}, {false, true, false}, {false, true, false}, {false, true, false}, {true, true, true}});
        FONT_DATA.put('Ï', new boolean[][]{{true, false, true}, {false, false, false}, {true, true, true}, {false, true, false}, {false, true, false}, {false, true, false}, {true, true, true}});

        FONT_DATA.put('Ò', new boolean[][]{{false, true, false, false, false}, {false, false, false, false, false}, {false, true, true, true, false}, {true, false, false, false, true}, {true, false, false, false, true}, {true, false, false, false, true}, {false, true, true, true, false}});
        FONT_DATA.put('Ó', new boolean[][]{{false, false, false, true, false}, {false, false, false, false, false}, {false, true, true, true, false}, {true, false, false, false, true}, {true, false, false, false, true}, {true, false, false, false, true}, {false, true, true, true, false}});
        FONT_DATA.put('Ô', new boolean[][]{{false, false, true, false, false}, {false, true, false, true, false}, {false, true, true, true, false}, {true, false, false, false, true}, {true, false, false, false, true}, {true, false, false, false, true}, {false, true, true, true, false}});
        FONT_DATA.put('Ö', new boolean[][]{{false, true, false, true, false}, {false, false, false, false, false}, {false, true, true, true, false}, {true, false, false, false, true}, {true, false, false, false, true}, {true, false, false, false, true}, {false, true, true, true, false}});

        FONT_DATA.put('Ù', new boolean[][]{{false, true, false, false, false}, {false, false, false, false, false}, {true, false, false, false, true}, {true, false, false, false, true}, {true, false, false, false, true}, {true, false, false, false, true}, {false, true, true, true, false}});
        FONT_DATA.put('Ú', new boolean[][]{{false, false, false, true, false}, {false, false, false, false, false}, {true, false, false, false, true}, {true, false, false, false, true}, {true, false, false, false, true}, {true, false, false, false, true}, {false, true, true, true, false}});
        FONT_DATA.put('Û', new boolean[][]{{false, false, true, false, false}, {false, true, false, true, false}, {true, false, false, false, true}, {true, false, false, false, true}, {true, false, false, false, true}, {true, false, false, false, true}, {false, true, true, true, false}});
        FONT_DATA.put('Ü', new boolean[][]{{false, true, false, true, false}, {false, false, false, false, false}, {true, false, false, false, true}, {true, false, false, false, true}, {true, false, false, false, true}, {true, false, false, false, true}, {false, true, true, true, false}});

        FONT_DATA.put('Ç', new boolean[][]{{false, true, true, true, false}, {true, false, false, false, true}, {true, false, false, false, false}, {true, false, false, false, true}, {false, true, true, true, false}, {false, false, true, false, false}, {false, true, false, false, false}});

        FONT_DATA.put('á', new boolean[][]{{false, false, false, true}, {false, false, false, false}, {false, true, true, false}, {false, false, false, true}, {false, true, true, true}, {true, false, false, true}, {true, true, true, true}});

        FONT_DATA.put('ë', new boolean[][]{{true, false, true, false}, {false, false, false, false}, {false, true, true, false}, {true, true, true, true}, {true, false, false, false}, {false, true, true, false}});

        FONT_DATA.put('ì', new boolean[][]{{true, false, false}, {false, false, false}, {true, true, false}, {false, true, false}, {false, true, false}, {true, true, true}});
        FONT_DATA.put('í', new boolean[][]{{false, false, true}, {false, false, false}, {true, true, false}, {false, true, false}, {false, true, false}, {true, true, true}});
        FONT_DATA.put('î', new boolean[][]{{false, true, false}, {true, false, true}, {true, true, false}, {false, true, false}, {false, true, false}, {true, true, true}});

        FONT_DATA.put('ò', new boolean[][]{{false, true, false, false}, {false, false, false, false}, {false, true, true, false}, {true, false, false, true}, {true, false, false, true}, {false, true, true, false}});
        FONT_DATA.put('ó', new boolean[][]{{false, false, false, true}, {false, false, false, false}, {false, true, true, false}, {true, false, false, true}, {true, false, false, true}, {false, true, true, false}});

        FONT_DATA.put('ù', new boolean[][]{{false, true, false, false}, {false, false, false, false}, {true, false, false, true}, {true, false, false, true}, {true, false, false, true}, {false, true, true, true}});
        FONT_DATA.put('ú', new boolean[][]{{false, false, false, true}, {false, false, false, false}, {true, false, false, true}, {true, false, false, true}, {true, false, false, true}, {false, true, true, true}});
        FONT_DATA.put('û', new boolean[][]{{false, true, false}, {true, false, true}, {true, false, false, true}, {true, false, false, true}, {true, false, false, true}, {false, true, true, true}});
        FONT_DATA.put('ü', new boolean[][]{{true, false, true, false}, {false, false, false, false}, {true, false, false, true}, {true, false, false, true}, {true, false, false, true}, {false, true, true, true}});

        FONT_DATA.put('ç', new boolean[][]{{false, false, false, false}, {false, true, true, false}, {true, false, false, false}, {true, false, false, false}, {false, true, true, false}, {false, false, true, false}, {false, true, false, false}});

        FONT_DATA.put('ÿ', new boolean[][]{{true, false, true, false}, {false, false, false, false}, {true, false, false, true}, {true, false, false, true}, {false, true, true, true}, {false, false, false, true}, {true, true, true, false}});
        FONT_DATA.put('.', new boolean[][]{{false}, {false}, {false}, {false}, {true}});
        FONT_DATA.put(',', new boolean[][]{{false}, {false}, {false}, {false}, {true}, {true}, {true}, {true}, {false}});
        FONT_DATA.put('!', new boolean[][]{{true}, {true}, {true}, {false}, {true}});
        FONT_DATA.put('?', new boolean[][]{{true, true, true}, {false, false, true}, {false, true, false}, {false, false, false}, {false, true, false}});
        FONT_DATA.put(':', new boolean[][]{{false}, {true}, {false}, {true}, {false}});
        FONT_DATA.put(';', new boolean[][]{{false}, {true}, {false}, {true}, {false}});
        FONT_DATA.put('-', new boolean[][]{{false, false, false}, {false, false, false}, {true, true, true}, {false, false, false}, {false, false, false}});
        FONT_DATA.put('\'', new boolean[][]{{false, true}, {true, false}, {false, false}, {false, false}, {false, false}});
        FONT_DATA.put('\u2019', new boolean[][]{{false, true}, {true, false}, {false, false}, {false, false}, {false, false}});
        FONT_DATA.put('"', new boolean[][]{{true, false, true}, {true, false, true}, {false, false, false}, {false, false, false}, {false, false, false}});
    }

    public static boolean[][] createNewLetter(String line1, String line2, String line3, String line4, String line5, String line6, String line7) {
        String[] lines = {line1, line2, line3, line4, line5, line6, line7};
        int maxWidth = 0;
        for (String line : lines) {
            if (line.length() > maxWidth) {
                maxWidth = line.length();
            }
        }

        boolean[][] matrix = new boolean[lines.length][maxWidth];

        for (int row = 0; row < lines.length; row++) {
            for (int col = 0; col < maxWidth; col++) {
                if (col < lines[row].length()) {
                    matrix[row][col] = (lines[row].charAt(col) == '/');
                } else {
                    matrix[row][col] = false;
                }
            }
        }

        return matrix;
    }

    public static boolean[][] createNewLetter(String line1, String line2, String line3, String line4, String line5, String line6, String line7, String line8) {
        String[] lines = {line1, line2, line3, line4, line5, line6, line7, line8};
        int maxWidth = 0;
        for (String line : lines) {
            if (line.length() > maxWidth) {
                maxWidth = line.length();
            }
        }

        boolean[][] matrix = new boolean[lines.length][maxWidth];

        for (int row = 0; row < lines.length; row++) {
            for (int col = 0; col < maxWidth; col++) {
                if (col < lines[row].length()) {
                    matrix[row][col] = (lines[row].charAt(col) == '/');
                } else {
                    matrix[row][col] = false;
                }
            }
        }

        return matrix;
    }

    public static ResourceLocation writeOutlinedTextOnTexture(ResourceLocation baseTexture, Component text, int x, int y, int textColor, int outlineColor) {
        try {
            Minecraft mc = Minecraft.getInstance();

            NativeImage baseImage = NativeImage.read(mc.getResourceManager().getResource(baseTexture).orElseThrow().open());
            NativeImage newImage = new NativeImage(baseImage.getWidth(), baseImage.getHeight(), false);
            newImage.copyFrom(baseImage);

            String textString = text.getString();

            int currentX = x;
            for (char c : textString.toCharArray()) {
                boolean[][] pattern = FONT_DATA.get(c);
                if (pattern != null) {
                    for (int row = 0; row < pattern.length; row++) {
                        for (int col = 0; col < pattern[row].length; col++) {
                            if (pattern[row][col]) {
                                for (int dy = -1; dy <= 1; dy++) {
                                    for (int dx = -1; dx <= 1; dx++) {
                                        if (dx == 0 && dy == 0) continue;
                                        int outlineX = currentX + col + dx;
                                        int outlineY = y + row + dy;

                                        if (outlineX >= 0 && outlineX < newImage.getWidth() &&
                                                outlineY >= 0 && outlineY < newImage.getHeight()) {
                                            boolean shouldDrawOutline = true;
                                            if (row + dy >= 0 && row + dy < pattern.length &&
                                                    col + dx >= 0 && col + dx < pattern[row].length) {
                                                if (pattern[row + dy][col + dx]) {
                                                    shouldDrawOutline = false;
                                                }
                                            }
                                            if (shouldDrawOutline) {
                                                newImage.setPixelRGBA(outlineX, outlineY, outlineColor);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    for (int row = 0; row < pattern.length; row++) {
                        for (int col = 0; col < pattern[row].length; col++) {
                            if (pattern[row][col] && currentX + col < newImage.getWidth() && y + row < newImage.getHeight()) {
                                newImage.setPixelRGBA(currentX + col, y + row, textColor);
                            }
                        }
                    }

                    currentX += pattern[0].length + 1;
                }
            }

            DynamicTexture dynamicTexture = new DynamicTexture(newImage);
            ResourceLocation result = mc.getTextureManager().register("outlined_text_texture", dynamicTexture);

            baseImage.close();
            return result;

        } catch (Exception e) {
            return baseTexture;
        }
    }
}