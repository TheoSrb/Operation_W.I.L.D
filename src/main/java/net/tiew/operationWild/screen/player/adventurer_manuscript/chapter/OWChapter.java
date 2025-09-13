package net.tiew.operationWild.screen.player.adventurer_manuscript.chapter;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.screen.player.adventurer_manuscript.AdventurerManuscriptScreen;
import net.tiew.operationWild.screen.player.adventurer_manuscript.text.OWTextParser;
import net.tiew.operationWild.screen.player.adventurer_manuscript.text.OWTextRenderer;
import net.tiew.operationWild.screen.player.adventurer_manuscript.text.StyledTextSegment;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class OWChapter {
    private static final ConcurrentHashMap<String, NativeImage> imageCache = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 10;

    private static ResourceLocation lastCreatedLeftTexture = null;
    private static ResourceLocation lastCreatedRightTexture = null;
    private static ResourceLocation lastCreatedNextLeftTexture = null;
    private static ResourceLocation lastCreatedNextRightTexture = null;
    private static ResourceLocation lastCreatedPreviousRightTexture = null;
    private static ResourceLocation lastCreatedPreviousLeftTexture = null;

    public static class ImageLayer {
        public final ResourceLocation imageLocation;
        public final int x;
        public final int y;
        public final float scale;
        public final float alpha;
        public final int sourceX;
        public final int sourceY;
        public final int sourceWidth;
        public final int sourceHeight;

        public ImageLayer(ResourceLocation imageLocation, int x, int y, float scale, float alpha, int sourceX, int sourceY, int sourceWidth, int sourceHeight) {
            this.imageLocation = imageLocation;
            this.x = x;
            this.y = y;
            this.scale = scale;
            this.alpha = alpha;
            this.sourceX = sourceX;
            this.sourceY = sourceY;
            this.sourceWidth = sourceWidth;
            this.sourceHeight = sourceHeight;
        }
    }

    public static class TextLayer {
        public final String text;
        public final int x;
        public final int y;
        public final float scale;
        public final float alpha;
        public final int color;

        public TextLayer(String text, int x, int y, float scale, float alpha, int color) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.scale = scale;
            this.alpha = alpha;
            this.color = color;
        }
    }

    public static void writeMultiLayerContentOnModelTexture(TextLayer[] textLayers, ImageLayer[] imageLayers, ResourceLocation textureLocation, boolean reverse, int maxLength) {
        NativeImage workingImage = null;
        try {
            workingImage = loadBaseImageCached(textureLocation);

            if (imageLayers != null) {
                for (ImageLayer imageLayer : imageLayers) {
                    if (imageLayer == null || imageLayer.imageLocation == null) continue;

                    int scaledImageWidth = (int)(imageLayer.sourceWidth * imageLayer.scale);
                    int scaledImageHeight = (int)(imageLayer.sourceHeight * imageLayer.scale);

                    drawImageOnNativeImageWithAlpha(workingImage, imageLayer.imageLocation, imageLayer.x, imageLayer.y,
                            scaledImageWidth, scaledImageHeight, imageLayer.alpha,
                            imageLayer.sourceX, imageLayer.sourceY, imageLayer.sourceWidth, imageLayer.sourceHeight);
                }
            }

            if (textLayers != null) {
                for (TextLayer layer : textLayers) {
                    if (layer == null || layer.text == null) continue;

                    List<StyledTextSegment> segments = OWTextParser.parseStyledText(layer.text, layer.color);

                    if (reverse) {
                        int estimatedLines = Math.max(1, (layer.text.length() * (int)(8 * layer.scale)) / maxLength);
                        int tempImageHeight = Math.max(100, estimatedLines * (int)(12 * layer.scale) + 50);
                        int tempImageWidth = Math.max(maxLength + 50, 200);

                        NativeImage tempImage = new NativeImage(tempImageWidth, tempImageHeight, true);

                        OWTextRenderer.processStyledTextWithLineBreaks(tempImage, segments, 0, 0, layer.alpha, maxLength, layer.scale, layer.color);

                        copyReversedText(workingImage, tempImage, layer.x, layer.y);

                        tempImage.close();
                    } else {
                        OWTextRenderer.processStyledTextWithLineBreaks(workingImage, segments, layer.x, layer.y, layer.alpha, maxLength, layer.scale, layer.color);
                    }
                }
            }

            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            DynamicTexture dynamicTexture = new DynamicTexture(workingImage);
            textureManager.register(textureLocation, dynamicTexture);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (workingImage != null) {
                workingImage.close();
            }
        }
    }

    private static void drawImageOnNativeImageWithAlpha(NativeImage targetImage, ResourceLocation imageLocation, int x, int y, int width, int height, float alpha, int sourceX, int sourceY, int sourceWidth, int sourceHeight) {
        try {
            Minecraft mc = Minecraft.getInstance();
            NativeImage sourceImage = NativeImage.read(mc.getResourceManager().getResource(imageLocation).orElseThrow().open());

            for (int sy = 0; sy < height; sy++) {
                for (int sx = 0; sx < width; sx++) {
                    int actualSourceX = sourceX + (sx * sourceWidth) / width;
                    int actualSourceY = sourceY + (sy * sourceHeight) / height;

                    int targetX = x + sx;
                    int targetY = y + sy;

                    if (targetX >= 0 && targetY >= 0 && targetX < targetImage.getWidth() && targetY < targetImage.getHeight() &&
                            actualSourceX >= 0 && actualSourceY >= 0 && actualSourceX < sourceImage.getWidth() && actualSourceY < sourceImage.getHeight()) {

                        int sourcePixel = sourceImage.getPixelRGBA(actualSourceX, actualSourceY);
                        int sourceAlpha = (sourcePixel >> 24) & 0xFF;

                        if (sourceAlpha > 0) {
                            int newAlpha = Math.min(255, (int)(sourceAlpha * alpha));
                            int newPixel = (newAlpha << 24) | (sourcePixel & 0x00FFFFFF);
                            targetImage.setPixelRGBA(targetX, targetY, newPixel);
                        }
                    }
                }
            }

            sourceImage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void copyReversedText(NativeImage targetImage, NativeImage textImage, int targetX, int targetY) {
        int minX = textImage.getWidth();
        int maxX = 0;
        int minY = textImage.getHeight();
        int maxY = 0;

        for (int y = 0; y < textImage.getHeight(); y++) {
            for (int x = 0; x < textImage.getWidth(); x++) {
                int pixelColor = textImage.getPixelRGBA(x, y);
                int alpha = (pixelColor >> 24) & 0xFF;

                if (alpha > 0) {
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        if (minX > maxX) return;

        int textWidth = maxX - minX + 1;
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                int pixelColor = textImage.getPixelRGBA(x, y);
                int alpha = (pixelColor >> 24) & 0xFF;

                if (alpha > 0) {
                    int reversedX = minX + (textWidth - 1 - (x - minX));

                    int finalX = targetX + (reversedX - minX);
                    int finalY = targetY + (y - minY);

                    if (finalX >= 0 && finalX < targetImage.getWidth() &&
                            finalY >= 0 && finalY < targetImage.getHeight()) {
                        targetImage.setPixelRGBA(finalX, finalY, pixelColor);
                    }
                }
            }
        }
    }

    public static void drawTextOnRightPage(Component text, int x, int y, float scale, float alpha, int maxLength, int color) {
        ResourceLocation emptyTexture = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/adventurer_manuscript/empty.png");
        List<StyledTextSegment> segments = OWTextParser.parseStyledText(text, color);

        int baseX = 430;
        int baseY = 855;
        int scaledX = (int)(x * scale);
        int scaledY = (int)(y * scale);

        NativeImage workingImage = null;
        try {
            workingImage = loadBaseImageCached(emptyTexture);
            OWTextRenderer.processStyledTextWithLineBreaks(workingImage, segments, baseX + scaledX, baseY + scaledY, alpha, maxLength, scale, color);
            ResourceLocation result = createFinalTexture(workingImage, "right_page");
            AdventurerManuscriptScreen.RIGHT_PAGE = String.valueOf(result);
        } catch (Exception e) {
            e.printStackTrace();
            if (workingImage != null) {
                AdventurerManuscriptScreen.RIGHT_PAGE = null;
                workingImage.close();
            }
        }
    }

    public static void drawTextOnLeftPage(Component text, int x, int y, float scale, float alpha, int maxLength, int color) {
        ResourceLocation emptyTexture = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/adventurer_manuscript/empty.png");
        List<StyledTextSegment> segments = OWTextParser.parseStyledText(text, color);

        int baseX = 760;
        int baseY = 855;
        int scaledX = (int)(x * scale);
        int scaledY = (int)(y * scale);

        NativeImage workingImage = null;
        try {
            workingImage = loadBaseImageCached(emptyTexture);
            OWTextRenderer.processStyledTextWithLineBreaks(workingImage, segments, baseX + scaledX, baseY + scaledY, alpha, maxLength, scale, color);
            ResourceLocation result = createFinalTexture(workingImage, "left_page");
            AdventurerManuscriptScreen.LEFT_PAGE = String.valueOf(result);
        } catch (Exception e) {
            e.printStackTrace();
            if (workingImage != null) {
                AdventurerManuscriptScreen.LEFT_PAGE = null;
                workingImage.close();
            }
        }
    }

    public static void drawTextOnNextLeftPage(Component text, int x, int y, float scale, float alpha, int maxLength, int color) {
        ResourceLocation emptyTexture = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/adventurer_manuscript/empty.png");
        List<StyledTextSegment> segments = OWTextParser.parseStyledText(text, color);

        int baseX = 904;
        int baseY = 551;
        int scaledX = (int)(x * scale);
        int scaledY = (int)(y * scale);

        NativeImage workingImage = null;
        try {
            workingImage = loadBaseImageCached(emptyTexture);
            OWTextRenderer.processStyledTextWithLineBreaks(workingImage, segments, baseX + scaledX, baseY + scaledY, alpha, maxLength, scale, color);
            ResourceLocation result = createFinalTexture(workingImage, "next_left_page");
            AdventurerManuscriptScreen.NEXT_LEFT_PAGE = String.valueOf(result);
        } catch (Exception e) {
            e.printStackTrace();
            if (workingImage != null) {
                AdventurerManuscriptScreen.NEXT_LEFT_PAGE = null;
                workingImage.close();
            }
        }
    }

    public static void drawTextOnPreviousLeftPage(Component text, int x, int y, float scale, float alpha, int maxLength, int color) {
        ResourceLocation emptyTexture = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/adventurer_manuscript/empty.png");
        List<StyledTextSegment> segments = OWTextParser.parseStyledText(text, color);

        int baseX = 760;
        int baseY = 15;
        int scaledX = (int)(x * scale);
        int scaledY = (int)(y * scale);

        NativeImage workingImage = null;
        try {
            workingImage = loadBaseImageCached(emptyTexture);
            OWTextRenderer.processStyledTextWithLineBreaks(workingImage, segments, baseX + scaledX, baseY + scaledY, alpha, maxLength, scale, color);
            ResourceLocation result = createFinalTexture(workingImage, "previous_left_page");
            AdventurerManuscriptScreen.PREVIOUS_LEFT_PAGE = String.valueOf(result);
        } catch (Exception e) {
            e.printStackTrace();
            if (workingImage != null) {
                AdventurerManuscriptScreen.PREVIOUS_LEFT_PAGE = null;
                workingImage.close();
            }
        }
    }

    public static void drawTextOnPreviousRightPage(Component text, int x, int y, float scale, float alpha, int maxLength, int color) {
        ResourceLocation emptyTexture = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/adventurer_manuscript/empty.png");
        List<StyledTextSegment> segments = OWTextParser.parseStyledText(text, color);

        int baseX = 542;
        int baseY = 559;
        int scaledX = (int)(x * scale);
        int scaledY = (int)(y * scale);

        NativeImage workingImage = null;
        try {
            workingImage = loadBaseImageCached(emptyTexture);
            OWTextRenderer.processStyledTextWithLineBreaks(workingImage, segments, baseX + scaledX, baseY + scaledY, alpha, maxLength, scale, color);
            ResourceLocation result = createFinalTexture(workingImage, "previous_right_page");
            AdventurerManuscriptScreen.PREVIOUS_RIGHT_PAGE = String.valueOf(result);
        } catch (Exception e) {
            e.printStackTrace();
            if (workingImage != null) {
                AdventurerManuscriptScreen.PREVIOUS_RIGHT_PAGE = null;
                workingImage.close();
            }
        }
    }

    public static void drawTextOnNextRightPage(Component text, int x, int y, float scale, float alpha, int maxLength, int color) {
        ResourceLocation emptyTexture = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/adventurer_manuscript/empty.png");
        List<StyledTextSegment> segments = OWTextParser.parseStyledText(text, color);

        int baseX = 350;
        int baseY = 15;
        int scaledX = (int)(x * scale);
        int scaledY = (int)(y * scale);

        NativeImage workingImage = null;
        try {
            workingImage = loadBaseImageCached(emptyTexture);
            OWTextRenderer.processStyledTextWithLineBreaks(workingImage, segments, baseX + scaledX, baseY + scaledY, alpha, maxLength, scale, color);
            ResourceLocation result = createFinalTexture(workingImage, "next_right_page");
            AdventurerManuscriptScreen.NEXT_RIGHT_PAGE = String.valueOf(result);
        } catch (Exception e) {
            e.printStackTrace();
            if (workingImage != null) {
                AdventurerManuscriptScreen.NEXT_RIGHT_PAGE = null;
                workingImage.close();
            }
        }
    }

    public static void drawTextOnLeftPage(String text, int x, int y, float scale, float alpha, int maxLength, int color) {
        ResourceLocation emptyTexture = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/adventurer_manuscript/empty.png");
        List<StyledTextSegment> segments = OWTextParser.parseStyledText(text, color);

        int baseX = 760;
        int baseY = 855;
        int scaledX = (int)(x * scale);
        int scaledY = (int)(y * scale);

        NativeImage workingImage = null;
        try {
            workingImage = loadBaseImageCached(emptyTexture);
            OWTextRenderer.processStyledTextWithLineBreaks(workingImage, segments, baseX + scaledX, baseY + scaledY, alpha, maxLength, scale, color);
            ResourceLocation result = createFinalTexture(workingImage, "left_page");
            AdventurerManuscriptScreen.LEFT_PAGE = String.valueOf(result);
        } catch (Exception e) {
            e.printStackTrace();
            if (workingImage != null) {
                AdventurerManuscriptScreen.LEFT_PAGE = null;
                workingImage.close();
            }
        }
    }

    public static void drawTextAndImageOnRightPage(Component text, int textX, int textY, float textScale, float textAlpha, int maxLength, int textColor,
                                                   ResourceLocation imageLocation, int imageX, int imageY, float imageScale, float imageAlpha,
                                                   int sourceX, int sourceY, int sourceWidth, int sourceHeight) {
        ResourceLocation emptyTexture = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/adventurer_manuscript/empty.png");
        List<StyledTextSegment> segments = OWTextParser.parseStyledText(text, textColor);

        int baseX = 430;
        int baseY = 855;
        int scaledTextX = (int)(textX * textScale);
        int scaledTextY = (int)(textY * textScale);

        int scaledImageWidth = (int)(sourceWidth * imageScale);
        int scaledImageHeight = (int)(sourceHeight * imageScale);

        NativeImage workingImage = null;
        try {
            workingImage = loadBaseImageCached(emptyTexture);

            drawImageOnNativeImage(workingImage, imageLocation, baseX + imageX, baseY + imageY,
                    scaledImageWidth, scaledImageHeight, imageAlpha, sourceX, sourceY, sourceWidth, sourceHeight);

            OWTextRenderer.processStyledTextWithLineBreaks(workingImage, segments, baseX + scaledTextX, baseY + scaledTextY,
                    textAlpha, maxLength, textScale, textColor);

            ResourceLocation result = createFinalTexture(workingImage, "right_page");
            AdventurerManuscriptScreen.RIGHT_PAGE = String.valueOf(result);
        } catch (Exception e) {
            e.printStackTrace();
            if (workingImage != null) {
                AdventurerManuscriptScreen.RIGHT_PAGE = null;
                workingImage.close();
            }
        }
    }


    public static void drawCombinedTextAndImageOnLeftPage(
            String text1, int x1, int y1, float scale1, float alpha1, int maxLength1, int color1,
            String text2, int x2, int y2, float scale2, float alpha2, int maxLength2, int color2,
            String text3, int x3, int y3, float scale3, float alpha3, int maxLength3, int color3,
            ResourceLocation imageLocation, int imageX, int imageY, float imageScale, float imageAlpha,
            int sourceX, int sourceY, int sourceWidth, int sourceHeight) {

        ResourceLocation emptyTexture = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/adventurer_manuscript/empty.png");

        List<StyledTextSegment> segments1 = OWTextParser.parseStyledText(text1, color1);
        List<StyledTextSegment> segments2 = OWTextParser.parseStyledText(text2, color2);
        List<StyledTextSegment> segments3 = OWTextParser.parseStyledText(text3, color3);

        int baseX = 760;
        int baseY = 855;

        int scaledX1 = (int)(x1 * scale1);
        int scaledY1 = (int)(y1 * scale1);
        int scaledX2 = (int)(x2 * scale2);
        int scaledY2 = (int)(y2 * scale2);
        int scaledX3 = (int)(x3 * scale3);
        int scaledY3 = (int)(y3 * scale3);

        int scaledImageWidth = (int)(sourceWidth * imageScale);
        int scaledImageHeight = (int)(sourceHeight * imageScale);

        NativeImage workingImage = null;
        try {
            workingImage = loadBaseImageCached(emptyTexture);

            drawImageOnNativeImage(workingImage, imageLocation, baseX + imageX, baseY + imageY, scaledImageWidth, scaledImageHeight, imageAlpha, sourceX, sourceY, sourceWidth, sourceHeight);

            OWTextRenderer.processStyledTextWithLineBreaks(workingImage, segments1, baseX + scaledX1, baseY + scaledY1, alpha1, maxLength1, scale1, color1);
            OWTextRenderer.processStyledTextWithLineBreaks(workingImage, segments2, baseX + scaledX2, baseY + scaledY2, alpha2, maxLength2, scale2, color2);
            OWTextRenderer.processStyledTextWithLineBreaks(workingImage, segments3, baseX + scaledX3, baseY + scaledY3, alpha3, maxLength3, scale3, color3);

            ResourceLocation result = createFinalTexture(workingImage, "left_page");
            AdventurerManuscriptScreen.LEFT_PAGE = String.valueOf(result);
        } catch (Exception e) {
            e.printStackTrace();
            if (workingImage != null) {
                AdventurerManuscriptScreen.LEFT_PAGE = null;
                workingImage.close();
            }
        }
    }

    public static void drawImageOnNativeImage(NativeImage targetImage, ResourceLocation imageLocation, int x, int y, int width, int height, float alpha, int sourceX, int sourceY, int sourceWidth, int sourceHeight) {
        try {
            Minecraft mc = Minecraft.getInstance();
            NativeImage sourceImage = NativeImage.read(mc.getResourceManager().getResource(imageLocation).orElseThrow().open());

            for (int sy = 0; sy < height; sy++) {
                for (int sx = 0; sx < width; sx++) {
                    int actualSourceX = sourceX + (sx * sourceWidth) / width;
                    int actualSourceY = sourceY + (sy * sourceHeight) / height;

                    int targetX = x + sx;
                    int targetY = y + sy;

                    if (targetX >= 0 && targetY >= 0 && targetX < targetImage.getWidth() && targetY < targetImage.getHeight() &&
                            actualSourceX >= 0 && actualSourceY >= 0 && actualSourceX < sourceImage.getWidth() && actualSourceY < sourceImage.getHeight()) {

                        int sourcePixel = sourceImage.getPixelRGBA(actualSourceX, actualSourceY);
                        int sourceAlpha = (sourcePixel >> 24) & 0xFF;

                        if (sourceAlpha > 0) {
                            int newAlpha = (int)(sourceAlpha * alpha);
                            int newPixel = (newAlpha << 24) | (sourcePixel & 0x00FFFFFF);
                            targetImage.setPixelRGBA(targetX, targetY, newPixel);
                        }
                    }
                }
            }

            sourceImage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetPageTexts() {
        cleanupPreviousTexture(lastCreatedLeftTexture);
        cleanupPreviousTexture(lastCreatedRightTexture);
        cleanupPreviousTexture(lastCreatedNextLeftTexture);
        cleanupPreviousTexture(lastCreatedNextRightTexture);
        cleanupPreviousTexture(lastCreatedPreviousLeftTexture);
        cleanupPreviousTexture(lastCreatedPreviousRightTexture);
        lastCreatedLeftTexture = null;
        lastCreatedRightTexture = null;
        lastCreatedNextLeftTexture = null;
        lastCreatedNextRightTexture = null;
        lastCreatedPreviousLeftTexture = null;
        lastCreatedPreviousRightTexture = null;

        AdventurerManuscriptScreen.LEFT_PAGE = null;
        AdventurerManuscriptScreen.NEXT_LEFT_PAGE = null;
        AdventurerManuscriptScreen.NEXT_RIGHT_PAGE = null;
        AdventurerManuscriptScreen.PREVIOUS_RIGHT_PAGE = null;
        AdventurerManuscriptScreen.PREVIOUS_LEFT_PAGE = null;
        AdventurerManuscriptScreen.leftChapterPage = null;
        AdventurerManuscriptScreen.RIGHT_PAGE = null;
    }

    private static NativeImage loadBaseImageCached(ResourceLocation texture) throws Exception {
        String cacheKey = texture.toString();
        NativeImage cachedImage = imageCache.get(cacheKey);
        if (cachedImage != null) {
            NativeImage workingImage = new NativeImage(cachedImage.getWidth(), cachedImage.getHeight(), false);
            workingImage.copyFrom(cachedImage);
            return workingImage;
        }

        Minecraft mc = Minecraft.getInstance();
        NativeImage baseImage = null;
        NativeImage workingImage = null;

        try {
            baseImage = NativeImage.read(mc.getResourceManager().getResource(texture).orElseThrow().open());
            workingImage = new NativeImage(baseImage.getWidth(), baseImage.getHeight(), false);
            workingImage.copyFrom(baseImage);

            if (imageCache.size() < MAX_CACHE_SIZE) {
                NativeImage cacheImage = new NativeImage(baseImage.getWidth(), baseImage.getHeight(), false);
                cacheImage.copyFrom(baseImage);
                imageCache.put(cacheKey, cacheImage);
            }

            return workingImage;
        } finally {
            if (baseImage != null) {
                baseImage.close();
            }
        }
    }

    private static ResourceLocation createFinalTexture(NativeImage image, String pageName) {
        if ("left_page".equals(pageName)) {
            cleanupPreviousTexture(lastCreatedLeftTexture);
        } else if ("right_page".equals(pageName)) {
            cleanupPreviousTexture(lastCreatedRightTexture);
        } else if ("next_left_page".equals(pageName)) {
            cleanupPreviousTexture(lastCreatedNextLeftTexture);
        } else if ("next_right_page".equals(pageName)) {
            cleanupPreviousTexture(lastCreatedNextRightTexture);
        } else if ("previous_right_page".equals(pageName)) {
            cleanupPreviousTexture(lastCreatedPreviousRightTexture);
        } else if ("previous_left_page".equals(pageName)) {
            cleanupPreviousTexture(lastCreatedPreviousLeftTexture);
        }

        Minecraft mc = Minecraft.getInstance();
        DynamicTexture dynamicTexture = new DynamicTexture(image);

        String textureName = "multiline_text_texture_" + pageName;
        ResourceLocation result = mc.getTextureManager().register(textureName, dynamicTexture);

        if ("left_page".equals(pageName)) {
            lastCreatedLeftTexture = result;
        } else if ("right_page".equals(pageName)) {
            lastCreatedRightTexture = result;
        } else if ("next_left_page".equals(pageName)) {
            lastCreatedNextLeftTexture = result;
        } else if ("next_right_page".equals(pageName)) {
            lastCreatedNextRightTexture = result;
        } else if ("previous_left_page".equals(pageName)) {
            lastCreatedPreviousLeftTexture = result;
        } else if ("previous_right_page".equals(pageName)) {
            lastCreatedPreviousRightTexture = result;
        }


        return result;
    }

    private static void cleanupPreviousTexture(ResourceLocation textureToClean) {
        if (textureToClean != null) {
            Minecraft mc = Minecraft.getInstance();
            try {
                mc.getTextureManager().release(textureToClean);
            } catch (Exception ignored) {}
        }
    }

    public static void clearCache() {
        for (NativeImage image : imageCache.values()) {
            if (image != null) {
                image.close();
            }
        }
        imageCache.clear();

        cleanupPreviousTexture(lastCreatedLeftTexture);
        cleanupPreviousTexture(lastCreatedRightTexture);
        cleanupPreviousTexture(lastCreatedNextLeftTexture);
        cleanupPreviousTexture(lastCreatedNextRightTexture);
        cleanupPreviousTexture(lastCreatedPreviousRightTexture);
        cleanupPreviousTexture(lastCreatedPreviousLeftTexture);
        lastCreatedLeftTexture = null;
        lastCreatedRightTexture = null;
        lastCreatedNextLeftTexture = null;
        lastCreatedNextRightTexture = null;
        lastCreatedPreviousLeftTexture = null;
        lastCreatedPreviousRightTexture = null;
    }

    public static void drawImageOnLeftPage(GuiGraphics graphics, int x, int y, float scale, float alpha,
                                           ResourceLocation resourceLocation, int xImage, int yImage, int width, int height) {
        graphics.pose().pushPose();
        graphics.pose().mulPose(Axis.XP.rotationDegrees(AdventurerManuscriptScreen.BOOK_ROTATION));
        graphics.pose().mulPose(Axis.YP.rotationDegrees(-10));
        graphics.pose().translate(40, -15, 400.0f);
        graphics.pose().scale(scale, scale, scale);

        int centeredX = (int)((x - 95) / scale);
        int centeredY = (int)((y - 173) / scale);
        int alphaValue = (int)(alpha * 255);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alphaValue);
        graphics.blit(resourceLocation, centeredX, centeredY, xImage, yImage, width, height);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        graphics.pose().popPose();
    }

    public static void drawImageOnRightPage(GuiGraphics graphics, int x, int y, float scale, float alpha,
                                            ResourceLocation resourceLocation, int xImage, int yImage, int width, int height) {
        graphics.pose().pushPose();
        graphics.pose().mulPose(Axis.XP.rotationDegrees(AdventurerManuscriptScreen.BOOK_ROTATION));
        graphics.pose().mulPose(Axis.YP.rotationDegrees(10));
        graphics.pose().translate(40, -15, 400.0f);
        graphics.pose().scale(scale, scale, scale);

        int centeredX = (int)((x - 95) / scale);
        int centeredY = (int)((y - 173) / scale);
        int alphaValue = (int)(alpha * 255);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alphaValue);
        graphics.blit(resourceLocation, centeredX, centeredY, xImage, yImage, width, height);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        graphics.pose().popPose();
    }
}