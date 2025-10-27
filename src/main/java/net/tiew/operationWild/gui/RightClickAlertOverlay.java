package net.tiew.operationWild.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import org.joml.Matrix4f;

public class RightClickAlertOverlay {

    private static final float ALERT_SCALE = 1.15f;
    private static final int PADDING = 8;
    private static final int LINE_SPACING = 6;
    private static final float GRAB_BY_SCALE = 1.65f;

    private static final ResourceLocation GAUGE_TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/grab_alert.png");
    private static int maxProgress;
    private static final int GAUGE_SIZE = 50;
    private static final int FILL_WIDTH = 43;
    private static final int FILL_HEIGHT = 23;

    public static boolean hasClicked = false;
    public static int clickAnimationTimer = 0;
    private static int lastTickCount = -1;

    public static void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        Player player = Minecraft.getInstance().player;
        Entity vehicle = player != null ? player.getVehicle() : null;

        if (vehicle == null) return;

        int currentTick = player.tickCount;
        if (currentTick != lastTickCount) {
            lastTickCount = currentTick;

            if (clickAnimationTimer > 0) {
                clickAnimationTimer--;

                if (clickAnimationTimer <= 0) {
                    clickAnimationTimer = 0;
                    hasClicked = false;
                }
            }
        }

        Font font = Minecraft.getInstance().font;
        int color = getBlinkingColor(player.tickCount);

        Component grabBy = createGrabByText(vehicle);
        Component[] alertLines = createAlertLines(color);

        int centerY = screenHeight / 2 - 10;
        renderScaledText(guiGraphics, font, grabBy, screenWidth, centerY - 50, GRAB_BY_SCALE, 0xFF0000);

        int alertY = centerY - 45 + (int)(font.lineHeight * GRAB_BY_SCALE) + 20;
        renderAlertWithBackground(guiGraphics, font, alertLines, screenWidth, alertY, color);

        int gaugeY = alertY + (int)(font.lineHeight * ALERT_SCALE * 2) + LINE_SPACING + (PADDING * 2) + 20;
        renderGauge(guiGraphics, screenWidth, gaugeY, vehicle.tickCount, (OWEntity) vehicle);
    }

    private static int getBlinkingColor(int tickCount) {
        return (tickCount / 5) % 2 == 0 ? 0xFFFFFF : 0x888888;
    }

    private static Component createGrabByText(Entity vehicle) {
        String entityName = vehicle.getClass().getSimpleName().split("Entity")[0].toLowerCase();
        Component grabber = Component.translatable("entity.ow." + entityName);
        String grabberUpperCase = grabber.getString().toUpperCase();

        return Component.translatable("tooltip.grabBy", Component.literal(grabberUpperCase))
                .setStyle(Style.EMPTY.withBold(false).withColor(0xFF0000));
    }

    private static Component[] createAlertLines(int color) {
        String alertText = Component.translatable("tooltip.rightClickAlert").getString();
        String[] words = alertText.split(" ");

        String line1 = String.join(" ", java.util.Arrays.copyOfRange(words, 0, Math.min(3, words.length)));
        String line2 = words.length > 3 ? String.join(" ", java.util.Arrays.copyOfRange(words, 3, words.length)) : "";

        Style style = Style.EMPTY.withBold(false).withColor(color);
        return new Component[]{
                Component.literal(line1).setStyle(style),
                Component.literal(line2).setStyle(style)
        };
    }

    private static void renderScaledText(GuiGraphics guiGraphics, Font font, Component text,
                                         int screenWidth, int y, float scale, int color) {
        int width = font.width(text);
        int x = (int)((screenWidth - width * scale) / 2);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().scale(scale, scale, 1.0f);
        guiGraphics.drawString(font, text, 0, 0, color);
        guiGraphics.pose().popPose();
    }

    private static void renderAlertWithBackground(GuiGraphics guiGraphics, Font font, Component[] lines,
                                                  int screenWidth, int y, int color) {
        int maxWidth = Math.max(font.width(lines[0]), font.width(lines[1]));
        int height = font.lineHeight;

        renderBackground(guiGraphics, screenWidth, y, maxWidth, height);
        renderScaledText(guiGraphics, font, lines[0], screenWidth, y, ALERT_SCALE, color);
        renderScaledText(guiGraphics, font, lines[1], screenWidth, y + (int)(height * ALERT_SCALE) + LINE_SPACING, ALERT_SCALE, color);
    }

    private static void renderBackground(GuiGraphics guiGraphics, int screenWidth, int y, int textWidth, int lineHeight) {
        int bgX = (int)((screenWidth - textWidth * ALERT_SCALE) / 2f - PADDING);
        int bgY = (int)(y - PADDING);
        int bgWidth = (int)(textWidth * ALERT_SCALE + (PADDING * 2));
        int bgHeight = (int)(lineHeight * ALERT_SCALE * 2 + LINE_SPACING + (PADDING * 2));

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 771);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.1f);

        Matrix4f matrix = guiGraphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        buffer.addVertex(matrix, bgX, bgY + bgHeight, 0).setColor(0.125f, 0.125f, 0.125f, 0.2f);
        buffer.addVertex(matrix, bgX + bgWidth, bgY + bgHeight, 0).setColor(0.125f, 0.125f, 0.125f, 0.2f);
        buffer.addVertex(matrix, bgX + bgWidth, bgY, 0).setColor(0.125f, 0.125f, 0.125f, 0.2f);
        buffer.addVertex(matrix, bgX, bgY, 0).setColor(0.125f, 0.125f, 0.125f, 0.2f);

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void renderGauge(GuiGraphics guiGraphics, int screenWidth, int y, int tickCount, OWEntity entity) {
        if (entity instanceof CrocodileEntity crocodile) {
            maxProgress = crocodile.getGrabMaxTimeout();

            int x = (screenWidth - GAUGE_SIZE) / 2;

            float speed = 0.02f * ((float) crocodile.getGrabTimeout() / 30);
            float oscillation = Mth.sin(tickCount * speed) * (2.0f * ((float) crocodile.getGrabTimeout() / ((float) crocodile.getGrabMaxTimeout() / 5.0f)));

            float scale = 1.0f;
            if (clickAnimationTimer > 0) {
                if (clickAnimationTimer == 3) scale = 1.15f;
                else if (clickAnimationTimer == 2) scale = 1.3f;
                else if (clickAnimationTimer == 1) scale = 1.15f;
            }

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(x + (GAUGE_SIZE / 2f), (y - 15) + oscillation + (GAUGE_SIZE / 2f), 0);
            guiGraphics.pose().scale(scale, scale, 1.0f);
            guiGraphics.pose().translate(-(GAUGE_SIZE / 2f), -(GAUGE_SIZE / 2f), 0);

            guiGraphics.blit(GAUGE_TEXTURE, 0, 0, 0, 50, GAUGE_SIZE, GAUGE_SIZE, GAUGE_SIZE, 100);

            float rawProgress = crocodile.getGrabTimeout();
            if (rawProgress > 0) {
                float progress = Mth.clamp(rawProgress / (float) maxProgress, 0.0f, 1.0f);
                int fillWidth = (int) (FILL_WIDTH * progress);

                applyGaugeColor(progress);

                guiGraphics.blit(GAUGE_TEXTURE, 4, 4, 0, 0, fillWidth, FILL_HEIGHT, GAUGE_SIZE, 100);

                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            }

            guiGraphics.pose().popPose();
        }
    }

    private static void applyGaugeColor(float progress) {
        float r, g, b;

        if (progress <= 0.33f) {
            float t = progress / 0.33f;
            r = 0.0f;
            g = 0.4f + (0.6f * t);
            b = 0.0f;
        } else if (progress <= 0.66f) {
            float t = (progress - 0.33f) / 0.33f;
            r = t;
            g = 1.0f;
            b = 0.0f;
        } else {
            float t = (progress - 0.66f) / 0.34f;
            r = 1.0f - (0.5f * t);
            g = 1.0f - t;
            b = 0.0f + (0.3f * t);
        }

        RenderSystem.setShaderColor(r, g, b, 1.0f);
    }
}