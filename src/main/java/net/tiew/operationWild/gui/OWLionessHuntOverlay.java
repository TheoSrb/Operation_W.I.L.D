package net.tiew.operationWild.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class OWLionessHuntOverlay {

    private OWLionessHuntOverlay() {}

    private static final int COLOR_AIM = 0xE8B04A;
    private static final int COLOR_LOCK = 0xFF6A3D;
    private static final int COLOR_SHADOW = 0x000000;
    private static final long ACQUIRE_MS = 180L;

    private static final Matrix4f projection = new Matrix4f();
    private static boolean markerVisible = false;
    private static float markerX, markerY, markerRadius;
    private static double markerDistance;
    private static int markerTargetId = -1;
    private static long markerAcquiredMs = 0L;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            projection.set(RenderSystem.getProjectionMatrix());
            return;
        }
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        markerVisible = false;
        LionEntity lioness = ridingLioness();
        if (lioness == null) return;

        int targetId = lioness.getClientLeapTargetId();
        Minecraft mc = Minecraft.getInstance();
        if (targetId < 0 || mc.level == null) {
            markerTargetId = -1;
            return;
        }
        Entity entity = mc.level.getEntity(targetId);
        if (!(entity instanceof LivingEntity target)) return;

        if (targetId != markerTargetId) {
            markerTargetId = targetId;
            markerAcquiredMs = System.currentTimeMillis();
        }

        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        Vec3 base = target.getPosition(partial);
        Vec3 center = base.add(0, target.getBbHeight() * 0.5, 0);
        Vec3 top = base.add(0, target.getBbHeight(), 0);

        Camera camera = event.getCamera();
        Matrix4f projView = new Matrix4f(projection).rotate(new Quaternionf(camera.rotation()).conjugate());
        float[] centerScreen = project(projView, camera, center);
        float[] topScreen = project(projView, camera, top);
        if (centerScreen == null || topScreen == null) return;

        markerX = centerScreen[0];
        markerY = centerScreen[1];
        markerRadius = Mth.clamp(Math.abs(centerScreen[1] - topScreen[1]) * 1.05f, 9f, 48f);
        markerDistance = camera.getPosition().distanceTo(center);
        markerVisible = true;
    }

    private static float[] project(Matrix4f projView, Camera camera, Vec3 point) {
        Vec3 relative = point.subtract(camera.getPosition());
        Vector4f clip = new Vector4f((float) relative.x, (float) relative.y, (float) relative.z, 1f);
        projView.transform(clip);
        if (clip.w <= 0.001f) return null;
        Minecraft mc = Minecraft.getInstance();
        float width = mc.getWindow().getGuiScaledWidth();
        float height = mc.getWindow().getGuiScaledHeight();
        return new float[]{(clip.x / clip.w + 1f) * 0.5f * width, (1f - clip.y / clip.w) * 0.5f * height};
    }

    private static LionEntity ridingLioness() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return null;
        if (!(mc.player.getVehicle() instanceof LionEntity lioness) || lioness.isMale()) return null;
        if (lioness.getControllingPassenger() != mc.player) return null;
        return lioness;
    }

    public static void render(GuiGraphics g, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;
        LionEntity lioness = ridingLioness();
        if (lioness == null || !markerVisible || lioness.getClientLeapTargetId() < 0) return;

        boolean locked = lioness.getLatchPhase() == LionEntity.LATCH_RUSH;
        drawReticle(g, markerX, markerY, markerRadius, locked);
    }

    private static void drawReticle(GuiGraphics g, float x, float y, float radius, boolean locked) {
        long now = System.currentTimeMillis();
        float appear = Mth.clamp((now - markerAcquiredMs) / (float) ACQUIRE_MS, 0f, 1f);
        float eased = 1f - (1f - appear) * (1f - appear) * (1f - appear);
        float pulse = 1f + 0.06f * (float) Math.sin(now / 140.0);
        float r = radius * Mth.lerp(eased, 1.9f, locked ? 0.82f : 1f) * pulse;
        float spin = (now % 100000L) / 1000f * (locked ? 220f : 45f);
        int alpha = (int) (eased * 235f);
        int color = (alpha << 24) | (locked ? COLOR_LOCK : COLOR_AIM);
        int shadow = ((alpha / 2) << 24) | COLOR_SHADOW;
        int arm = Math.max(4, Math.round(r * 0.45f));

        g.pose().pushPose();
        g.pose().translate(x, y, 0f);
        g.pose().mulPose(Axis.ZP.rotationDegrees(spin));
        for (int i = 0; i < 4; i++) {
            g.pose().pushPose();
            g.pose().mulPose(Axis.ZP.rotationDegrees(i * 90f));
            int corner = Math.round(r);
            g.fill(-corner - 1, -corner - 1, -corner + arm + 1, -corner + 2, shadow);
            g.fill(-corner - 1, -corner - 1, -corner + 2, -corner + arm + 1, shadow);
            g.fill(-corner, -corner, -corner + arm, -corner + 1, color);
            g.fill(-corner, -corner, -corner + 1, -corner + arm, color);
            g.fill(-corner + 3, -corner + 3, -corner + 4, -corner + 4, color);
            g.pose().popPose();
        }
        g.pose().popPose();

        g.pose().pushPose();
        g.pose().translate(x, y, 0f);
        g.pose().mulPose(Axis.ZP.rotationDegrees(45f));
        g.fill(-2, -2, 2, 2, shadow);
        g.fill(-1, -1, 1, 1, color);
        g.pose().popPose();

        Font font = Minecraft.getInstance().font;
        String text = Math.round(markerDistance) + " m";
        float scale = 0.7f;
        g.pose().pushPose();
        g.pose().translate(x - font.width(text) * scale / 2f, y + r + 4f, 0f);
        g.pose().scale(scale, scale, 1f);
        g.drawString(font, text, 0, 0, color, true);
        g.pose().popPose();
    }
}
