package net.tiew.operationWild.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.cocoon.CocoonClientState;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class TarantulaCocoonOverlay {

    private static final int LAYERS = 3;
    private static final int CORNER_SIZE = 128;
    private static final int SIDE_WIDTH = 56;
    private static final int SIDE_HEIGHT = 112;
    private static final ResourceLocation[] TOP_CORNERS = textures("corner_a", 1);
    private static final ResourceLocation[] BOTTOM_CORNERS = textures("corner_b", 1);
    private static final ResourceLocation[] SIDES = textures("side", 2);
    private static final long FLASH_MS = 300L;
    private static final float FLASH_BOOST = 0.35f;
    private static final float LOCK_PULSE = 0.12f;
    private static final float BASE_ALPHA = 0.85f;

    private TarantulaCocoonOverlay() {
    }

    private static ResourceLocation[] textures(String name, int first) {
        ResourceLocation[] result = new ResourceLocation[LAYERS];
        for (int i = first; i <= LAYERS; i++) {
            result[i - 1] = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/hud/cocoon/" + name + "_" + i + ".png");
        }
        return result;
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        CocoonClientState.Cocoon cocoon = CocoonClientState.get(mc.player.getId());
        if (cocoon == null) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float stage = Mth.lerp(partialTick, cocoon.shownO, cocoon.shown);
        if (stage <= 0.01f) return;

        long sinceBuilt = System.currentTimeMillis() - cocoon.builtAtMs;
        float flash = sinceBuilt < FLASH_MS ? (1f - sinceBuilt / (float) FLASH_MS) * FLASH_BOOST : 0f;
        float pulse = cocoon.locked ? (Mth.sin((mc.player.tickCount + partialTick) * 0.25f) * 0.5f + 0.5f) * LOCK_PULSE : 0f;

        GuiGraphics graphics = event.getGuiGraphics();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        for (int layer = 0; layer < LAYERS; layer++) {
            float alpha = Mth.clamp(stage * LAYERS - layer, 0f, 1f) * (BASE_ALPHA + flash + pulse);
            if (alpha <= 0.002f) continue;
            graphics.setColor(1f, 1f, 1f, Mth.clamp(alpha, 0f, 1f));
            drawMirrored(graphics, TOP_CORNERS[layer], 0, 0, CORNER_SIZE, CORNER_SIZE, false, false);
            drawMirrored(graphics, TOP_CORNERS[layer], width, 0, CORNER_SIZE, CORNER_SIZE, true, false);
            drawMirrored(graphics, BOTTOM_CORNERS[layer], 0, height, CORNER_SIZE, CORNER_SIZE, false, true);
            drawMirrored(graphics, BOTTOM_CORNERS[layer], width, height, CORNER_SIZE, CORNER_SIZE, true, true);
            if (SIDES[layer] != null) {
                int top = (height - SIDE_HEIGHT) / 2;
                drawMirrored(graphics, SIDES[layer], 0, top, SIDE_WIDTH, SIDE_HEIGHT, false, false);
                drawMirrored(graphics, SIDES[layer], width, top, SIDE_WIDTH, SIDE_HEIGHT, true, false);
            }
        }
        graphics.setColor(1f, 1f, 1f, 1f);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void drawMirrored(GuiGraphics graphics, ResourceLocation texture, int x, int y, int w, int h, boolean flipX, boolean flipY) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(flipX ? -1f : 1f, flipY ? -1f : 1f, 1f);
        graphics.blit(texture, 0, 0, 0f, 0f, w, h, w, h);
        graphics.pose().popPose();
    }
}
