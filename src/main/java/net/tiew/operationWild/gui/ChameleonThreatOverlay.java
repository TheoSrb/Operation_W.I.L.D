package net.tiew.operationWild.gui;

import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.ChameleonEntity;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class ChameleonThreatOverlay {

    private static final int COLOR = 0xE0482E;
    private static final int OUTLINE = 0x2B1A10;
    private static final float RING_RATIO = 0.36f;
    private static final float PULSE_PERIOD_MS = 700f;
    private static final float FADE_IN_MS = 180f;

    private static int shownThreatId = -1;
    private static long shownSinceMs = 0L;

    private ChameleonThreatOverlay() {
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui || minecraft.screen != null) return;

        Entity threat = findThreat(player);
        if (threat == null) {
            shownThreatId = -1;
            return;
        }

        long now = Util.getMillis();
        if (threat.getId() != shownThreatId) {
            shownThreatId = threat.getId();
            shownSinceMs = now;
        }

        float partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        double dx = Mth.lerp(partialTicks, threat.xo, threat.getX()) - Mth.lerp(partialTicks, player.xo, player.getX());
        double dz = Mth.lerp(partialTicks, threat.zo, threat.getZ()) - Mth.lerp(partialTicks, player.zo, player.getZ());
        float yaw = player.getViewYRot(partialTicks) * Mth.DEG_TO_RAD;
        double forward = -dx * Mth.sin(yaw) + dz * Mth.cos(yaw);
        double right = -dx * Mth.cos(yaw) - dz * Mth.sin(yaw);
        float angle = (float) Mth.atan2(right, forward);

        GuiGraphics graphics = event.getGuiGraphics();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        float radius = Math.min(width, height) * RING_RATIO;
        float x = width / 2f + Mth.sin(angle) * radius;
        float y = height / 2f - Mth.cos(angle) * radius;

        float fadeIn = Mth.clamp((now - shownSinceMs) / FADE_IN_MS, 0f, 1f);
        float pulse = 0.65f + 0.35f * Mth.sin((now % (long) PULSE_PERIOD_MS) / PULSE_PERIOD_MS * Mth.TWO_PI);
        int alpha = (int) (255 * fadeIn * pulse);
        float scale = 1f + 0.12f * (1f - fadeIn);

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().mulPose(Axis.ZP.rotation(angle));
        graphics.pose().scale(scale, scale, 1f);
        drawChevron(graphics, 0, OUTLINE | (alpha << 24), 1);
        drawChevron(graphics, 0, COLOR | (alpha << 24), 0);
        graphics.pose().popPose();
    }

    private static void drawChevron(GuiGraphics graphics, int offsetY, int color, int grow) {
        for (int row = 0; row < 6; row++) {
            int half = row + 1 + grow;
            int top = offsetY - 6 + row * 2 - grow;
            graphics.fill(-half, top, -half + 3 + grow * 2, top + 2 + grow * 2, color);
            graphics.fill(half - 3 - grow * 2, top, half, top + 2 + grow * 2, color);
        }
    }

    private static Entity findThreat(Player player) {
        for (Entity passenger : player.getPassengers()) {
            if (passenger instanceof ChameleonEntity chameleon && chameleon.isTame()) {
                int id = chameleon.getWatchTarget();
                return id >= 0 ? player.level().getEntity(id) : null;
            }
        }
        return null;
    }
}
