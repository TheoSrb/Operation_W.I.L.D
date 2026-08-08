package net.tiew.operationWild.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class OWWindRushOverlay {

    private OWWindRushOverlay() {}

    private static final int STREAKS = 34;
    private static final float FADE_PER_TICK = 0.12f;
    private static final float TRAVEL_PER_SECOND = 1.35f;
    private static final float INNER_RADIUS = 0.45f;
    private static final float OUTER_RADIUS = 1.20f;
    private static final int STREAK_COLOR = 0xE8F4FF;
    private static final int MAX_ALPHA = 85;
    private static final float VIGNETTE_STRENGTH = 0.45f;

    private static float intensity = 0f;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        float step = isMountAtFullCharge(mc) ? FADE_PER_TICK : -FADE_PER_TICK;
        intensity = Mth.clamp(intensity + step, 0f, 1f);
    }

    public static float rushIntensity() {
        return intensity;
    }

    public static boolean isMountAtFullCharge(Minecraft mc) {
        if (mc.player == null) return false;
        if (!(mc.player.getRootVehicle() instanceof OWEntity mount)) return false;
        if (!mount.canIncreasesSpeedDuringSprint()) return false;
        if (mount.getPassengers().indexOf(mc.player) != 0) return false;

        return mount.isRunning() && mount.accelerationIsAtMax();
    }

    public static void render(GuiGraphics g) {
        if (intensity <= 0.01f) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;
        if (!mc.options.getCameraType().isFirstPerson()) return;

        int w = g.guiWidth();
        int h = g.guiHeight();
        float cx = w * 0.5f;
        float cy = h * 0.5f;
        float span = Math.max(w, h) * 0.5f;

        float time = (System.currentTimeMillis() % 100000L) / 1000f;

        RenderSystem.enableBlend();
        OWCinematicFx.drawVignette(g, w, h, intensity * VIGNETTE_STRENGTH);

        for (int i = 0; i < STREAKS; i++) {
            float offset = hash(i, 1);
            float speedVar = hash(i, 2);
            float lengthVar = hash(i, 3);
            float angleVar = hash(i, 4);
            float alphaVar = hash(i, 5);

            float sector = 360f / STREAKS;
            float angle = i * sector + (angleVar - 0.5f) * sector * 1.6f;

            float phase = (time * TRAVEL_PER_SECOND * (0.55f + speedVar * 0.9f) + offset) % 1f;
            float radius = span * (INNER_RADIUS + phase * (OUTER_RADIUS - INNER_RADIUS));
            float length = span * (0.08f + lengthVar * 0.26f) * (0.4f + phase * 0.6f);

            float appear = Math.min(phase / 0.30f, 1f);
            float toEdge = 0.25f + 0.75f * phase;
            int alpha = (int) (MAX_ALPHA * intensity * appear * toEdge * (0.45f + alphaVar * 0.55f));
            if (alpha <= 2) continue;

            int thickness = 1 + (int) (hash(i, 6) * 3f);

            g.pose().pushPose();
            g.pose().translate(cx, cy, 0);
            g.pose().mulPose(Axis.ZP.rotationDegrees(angle));
            g.fill((int) radius, -thickness, (int) (radius + length), thickness, (alpha << 24) | STREAK_COLOR);
            g.pose().popPose();
        }

        RenderSystem.disableBlend();
    }

    private static float hash(int index, int salt) {
        int n = index * 374761393 + salt * 668265263;
        n = (n ^ (n >>> 13)) * 1274126177;
        return ((n ^ (n >>> 16)) & 0x7FFFFFFF) / (float) 0x7FFFFFFF;
    }
}
