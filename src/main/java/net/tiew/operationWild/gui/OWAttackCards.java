package net.tiew.operationWild.gui;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.tiew.operationWild.OperationWild;

import java.io.IOException;

public final class OWAttackCards {

    public static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath(
            OperationWild.MOD_ID, "textures/overlay/ow_entity_attacks_cards.png");
    private static final ResourceLocation GREYED_ATLAS = ResourceLocation.fromNamespaceAndPath(
            OperationWild.MOD_ID, "dynamic/ow_entity_attacks_cards_greyed");

    public static final int CARD_SIZE = 20;
    public static final int TEX_SIZE = 256;

    private static final int CONTENT_INSET = 3;
    private static final int CONTENT_SIZE = CARD_SIZE - CONTENT_INSET * 2;
    private static final float GREYED_BRIGHTNESS = 0.75f;

    private static boolean greyedRegistered;

    private OWAttackCards() {
    }

    public static void drawLit(GuiGraphics g, int x, int y, int texX, int texY) {
        draw(g, x, y, texX, texY, 0);
    }

    public static void drawGreyed(GuiGraphics g, int x, int y, int texX, int texY) {
        draw(g, x, y, texX, texY, CARD_SIZE);
    }

    public static void draw(GuiGraphics g, int x, int y, int texX, int texY, int greyedRows) {
        g.blit(ATLAS, x, y, texX, texY, CARD_SIZE, CARD_SIZE, TEX_SIZE, TEX_SIZE);

        int contentRows = Mth.clamp(Math.round(greyedRows * (float) CONTENT_SIZE / CARD_SIZE), 0, CONTENT_SIZE);
        if (contentRows <= 0) return;

        g.blit(greyedAtlas(),
                x + CONTENT_INSET, y + CONTENT_INSET,
                texX + CONTENT_INSET, texY + CONTENT_INSET,
                CONTENT_SIZE, contentRows,
                TEX_SIZE, TEX_SIZE);
    }

    private static ResourceLocation greyedAtlas() {
        if (!greyedRegistered) {
            Minecraft.getInstance().getTextureManager().register(GREYED_ATLAS, new GreyedAtlasTexture());
            greyedRegistered = true;
        }
        return GREYED_ATLAS;
    }

    private static void greyOut(NativeImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int abgr = image.getPixelRGBA(x, y);
                int alpha = abgr >>> 24;
                if (alpha == 0) continue;

                float luminance = 0.2126f * (abgr & 0xFF)
                        + 0.7152f * ((abgr >> 8) & 0xFF)
                        + 0.0722f * ((abgr >> 16) & 0xFF);
                int grey = Mth.clamp((int) (luminance * GREYED_BRIGHTNESS), 0, 255);
                image.setPixelRGBA(x, y, (alpha << 24) | (grey << 16) | (grey << 8) | grey);
            }
        }
    }

    private static final class GreyedAtlasTexture extends SimpleTexture {

        GreyedAtlasTexture() {
            super(ATLAS);
        }

        @Override
        protected TextureImage getTextureImage(ResourceManager resourceManager) {
            TextureImage texture = super.getTextureImage(resourceManager);
            try {
                greyOut(texture.getImage());
            } catch (IOException ignored) {
            }
            return texture;
        }
    }
}
