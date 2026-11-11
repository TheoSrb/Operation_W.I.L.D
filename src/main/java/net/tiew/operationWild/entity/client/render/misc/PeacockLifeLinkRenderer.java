package net.tiew.operationWild.entity.client.render.misc;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.terrestrial.PeacockEntity;
import net.tiew.operationWild.entity.attacks.OWAttackLogic;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import org.joml.Matrix4f;

import java.util.List;

public final class PeacockLifeLinkRenderer {

    private static final ResourceLocation FEATHER = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/item/peacock_feather.png");

    private static final float[] DONOR = {1f, 0.3f, 0.3f};
    private static final float[] RECEIVER = {0.36f, 1f, 0.54f};
    private static final float[] BLOCKED = {0.75f, 0.05f, 0.05f};
    private static final int BLOCKED_TEXT = 0xFF2020;
    private static final int DONOR_TEXT = 0xFF4A4A;
    private static final int RECEIVER_TEXT = 0x5CFF8A;

    private static final int SEGMENTS = 28;
    private static final int RING_SEGMENTS = 40;
    private static final double VIEW_RANGE = 96.0;
    private static final double ARC_HEIGHT = 0.9;
    private static final double WAVE_AMPLITUDE = 0.14;
    private static final float CORE_WIDTH = 0.07f;
    private static final float GLOW_WIDTH = 0.26f;
    private static final float MARKER_SCALE = 0.05f;
    private static final double MARKER_LIFT = 0.9;

    private PeacockLifeLinkRenderer() {
    }

    private record Marker(LivingEntity entity, boolean donor, boolean blocked, float alpha) {}

    public static void render(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui) return;

        Vec3 cam = event.getCamera().getPosition();
        List<PeacockEntity> peacocks = mc.level.getEntitiesOfClass(PeacockEntity.class,
                new AABB(cam, cam).inflate(VIEW_RANGE), peacock -> peacock.getLinkPhase() != PeacockEntity.LINK_NONE || peacock.getSweepLinkTick() > 0);

        PeacockEntity ridden = mc.player.getRootVehicle() instanceof PeacockEntity p && OWAttackLogic.isPeacockLinkAiming() ? p : null;
        OWEntity aimed = ridden != null && !ridden.isLinking() ? ridden.pickLinkTarget(mc.player) : null;
        if (peacocks.isEmpty() && aimed == null) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float time = (mc.level.getGameTime() + partialTick) * 0.05f;
        float pulse = 0.55f + 0.45f * Mth.sin(time * 7f);

        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f matrix = pose.last().pose();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        java.util.List<Marker> markers = new java.util.ArrayList<>();

        for (PeacockEntity peacock : peacocks) {
            LivingEntity donor = peacock.getLinkDonor();
            LivingEntity receiver = peacock.getLinkReceiver();

            if (donor != null) {
                drawRing(buffer, matrix, donor, partialTick, time, DONOR, 1f);
                markers.add(new Marker(donor, true, false, 1f));
            }
            if (receiver != null) {
                drawRing(buffer, matrix, receiver, partialTick, time, RECEIVER, 1f);
                markers.add(new Marker(receiver, false, false, 1f));
            }
            int sweepLinkTick = peacock.getSweepLinkTick();
            if (sweepLinkTick > 0) {
                float life = (sweepLinkTick - partialTick) / OWAttacksConstants.Peacock.SWEEP_LINK_TICKS;
                float fadeIn = Mth.clamp((1f - life) * 6f, 0f, 1f);
                float strength = Mth.clamp(Math.min(fadeIn, life * 2.5f), 0f, 1f);
                Vec3 origin = center(peacock, partialTick);
                for (LivingEntity healed : peacock.getSweepLinkedEntities()) {
                    Vec3 target = center(healed, partialTick);
                    drawRibbon(buffer, matrix, cam, origin, target, time * 1.6f, GLOW_WIDTH * 0.8f, 0.35f, 1f, 0.55f, 0.25f * strength, 0f);
                    drawRibbon(buffer, matrix, cam, origin, target, time * 1.6f, CORE_WIDTH, 0.8f, 1f, 0.85f, 0.9f * strength, 0f);
                }
            }

            if (peacock.isLinking() && donor != null && receiver != null) {
                float fade = Math.max(0.3f, peacock.linkBlend(partialTick));
                Vec3 from = center(donor, partialTick);
                Vec3 to = center(receiver, partialTick);
                drawRibbon(buffer, matrix, cam, from, to, time, GLOW_WIDTH, 0.35f, 1f, 0.55f, 0.22f * fade, 0f);
                drawRibbon(buffer, matrix, cam, from, to, time, CORE_WIDTH, 0.75f, 1f, 0.8f, 0.85f * fade, 0f);
                drawRibbon(buffer, matrix, cam, from, to, time * 1.3f, CORE_WIDTH * 0.6f, 0.55f, 1f, 0.65f, 0.5f * fade, (float) Math.PI);
            }
        }

        if (aimed != null && aimed.getId() != ridden.getLinkDonorId() && aimed.getId() != ridden.getLinkReceiverId()) {
            boolean asDonor = ridden.getLinkDonorId() < 0;
            boolean blocked = asDonor && !PeacockEntity.canDonate(aimed);
            float blink = blocked ? (Mth.sin(time * 18f) > 0 ? 1f : 0.35f) : pulse;
            drawRing(buffer, matrix, aimed, partialTick, time, blocked ? BLOCKED : asDonor ? DONOR : RECEIVER, blink);
            markers.add(new Marker(aimed, asDonor, blocked, blink));
        }

        MeshData mesh = buffer.build();
        if (mesh != null) BufferUploader.drawWithShader(mesh);

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        pose.popPose();

        if (markers.isEmpty()) return;
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        for (Marker marker : markers) {
            drawMarker(pose, buffers, event.getCamera(), marker, partialTick, time);
        }
        buffers.endBatch();
    }

    private static Vec3 center(LivingEntity entity, float partialTick) {
        return entity.getPosition(partialTick).add(0, entity.getBbHeight() * 0.6, 0);
    }

    private static void drawRing(BufferBuilder buffer, Matrix4f matrix, LivingEntity entity, float partialTick, float time,
                                 float[] color, float alpha) {
        Vec3 feet = entity.getPosition(partialTick).add(0, 0.06, 0);
        float radius = Math.max(0.7f, entity.getBbWidth() * 0.8f);
        float breathe = 1f + 0.05f * Mth.sin(time * 5f);

        ring(buffer, matrix, feet, radius * breathe, 0.14f, color, 0.85f * alpha);
        ring(buffer, matrix, feet, radius * breathe - 0.2f, 0.05f, color, 0.5f * alpha);

        float ripple = (time * 0.9f) % 1f;
        ring(buffer, matrix, feet.add(0, ripple * 0.15, 0), radius + ripple * 0.9f, 0.08f, color, 0.6f * alpha * (1f - ripple));
    }

    private static void ring(BufferBuilder buffer, Matrix4f matrix, Vec3 at, float radius, float width, float[] c, float a) {
        if (a <= 0.01f || radius <= 0f) return;
        float inner = Math.max(0f, radius - width * 0.5f);
        float outer = radius + width * 0.5f;
        for (int i = 0; i < RING_SEGMENTS; i++) {
            double a0 = Math.PI * 2 * i / RING_SEGMENTS;
            double a1 = Math.PI * 2 * (i + 1) / RING_SEGMENTS;
            float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0);
            float c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
            vertex(buffer, matrix, at.x + c0 * inner, at.y, at.z + s0 * inner, c, a);
            vertex(buffer, matrix, at.x + c0 * outer, at.y, at.z + s0 * outer, c, a);
            vertex(buffer, matrix, at.x + c1 * outer, at.y, at.z + s1 * outer, c, a);
            vertex(buffer, matrix, at.x + c1 * inner, at.y, at.z + s1 * inner, c, a);
        }
    }

    private static void drawRibbon(BufferBuilder buffer, Matrix4f matrix, Vec3 cam, Vec3 from, Vec3 to, float time,
                                   float width, float r, float g, float b, float alpha, float phase) {
        Vec3 span = to.subtract(from);
        if (span.lengthSqr() < 1.0E-4) return;
        Vec3 direction = span.normalize();
        Vec3 lateral = direction.cross(new Vec3(0, 1, 0));
        if (lateral.lengthSqr() < 1.0E-4) lateral = new Vec3(1, 0, 0);
        lateral = lateral.normalize();
        float[] color = {r, g, b};

        Vec3 previous = null;
        Vec3 previousSide = null;
        float previousAlpha = 0f;

        for (int i = 0; i <= SEGMENTS; i++) {
            float t = (float) i / SEGMENTS;
            double arc = Math.sin(Math.PI * t) * ARC_HEIGHT;
            double wave = Math.sin(t * 14.0 - time * 9.0 + phase) * WAVE_AMPLITUDE * Math.sin(Math.PI * t);
            Vec3 point = from.add(span.scale(t)).add(0, arc, 0).add(lateral.scale(wave));

            Vec3 side = direction.cross(cam.subtract(point));
            if (side.lengthSqr() < 1.0E-6) side = lateral;
            float pulse = 1f + 0.35f * Mth.sin(time * 6f - t * 18f + phase);
            side = side.normalize().scale(width * pulse);
            float a = alpha * (0.55f + 0.45f * Mth.sin((float) Math.PI * t));

            if (previous != null) {
                vertex(buffer, matrix, previous.add(previousSide), color, previousAlpha);
                vertex(buffer, matrix, previous.subtract(previousSide), color, previousAlpha);
                vertex(buffer, matrix, point.subtract(side), color, a);
                vertex(buffer, matrix, point.add(side), color, a);
            }
            previous = point;
            previousSide = side;
            previousAlpha = a;
        }
    }

    private static void drawMarker(PoseStack pose, MultiBufferSource.BufferSource buffers, Camera camera, Marker marker,
                                   float partialTick, float time) {
        LivingEntity entity = marker.entity();
        Vec3 cam = camera.getPosition();
        double bob = Math.sin(time * 4f + entity.getId()) * 0.08;
        Vec3 at = entity.getPosition(partialTick).add(0, entity.getBbHeight() + MARKER_LIFT + bob, 0);

        pose.pushPose();
        pose.translate(at.x - cam.x, at.y - cam.y, at.z - cam.z);
        pose.mulPose(camera.rotation());
        pose.scale(MARKER_SCALE, -MARKER_SCALE, MARKER_SCALE);
        Matrix4f matrix = pose.last().pose();

        int alpha = Mth.clamp((int) (marker.alpha() * 255), 0, 255);
        VertexConsumer feather = buffers.getBuffer(RenderType.textSeeThrough(FEATHER));
        texturedQuad(feather, matrix, -22f, -10f, -2f, 10f, marker.blocked() ? alpha / 2 : alpha);

        Font font = Minecraft.getInstance().font;
        if (marker.blocked()) {
            int red = 0xFF000000 | BLOCKED_TEXT;
            pose.pushPose();
            pose.scale(2.2f, 2.2f, 2.2f);
            font.drawInBatch(Component.literal("\u2716"), -7.5f, -4.5f, red, false,
                    pose.last().pose(), buffers, Font.DisplayMode.SEE_THROUGH, 0, LightTexture.FULL_BRIGHT);
            pose.popPose();
            Component reason = Component.translatable("ow.peacock.link.too_weak");
            font.drawInBatch(reason, -font.width(reason) / 2f, 13f, red, false,
                    pose.last().pose(), buffers, Font.DisplayMode.SEE_THROUGH, 0x90000000, LightTexture.FULL_BRIGHT);
            pose.popPose();
            return;
        }
        int color = (alpha << 24) | ((marker.donor() ? DONOR_TEXT : RECEIVER_TEXT) & 0xFFFFFF);
        pose.pushPose();
        pose.scale(1.6f, 1.6f, 1.6f);
        font.drawInBatch(Component.literal(marker.donor() ? "-❤" : "+❤"), 0f, -4f, color, false,
                pose.last().pose(), buffers, Font.DisplayMode.SEE_THROUGH, 0, LightTexture.FULL_BRIGHT);
        pose.popPose();
        pose.popPose();
    }

    private static void texturedQuad(VertexConsumer consumer, Matrix4f matrix, float x0, float y0, float x1, float y1, int alpha) {
        texturedVertex(consumer, matrix, x0, y1, 0f, 1f, alpha);
        texturedVertex(consumer, matrix, x1, y1, 1f, 1f, alpha);
        texturedVertex(consumer, matrix, x1, y0, 1f, 0f, alpha);
        texturedVertex(consumer, matrix, x0, y0, 0f, 0f, alpha);
    }

    private static void texturedVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float u, float v, int alpha) {
        consumer.addVertex(matrix, x, y, 0f).setColor(255, 255, 255, alpha).setUv(u, v).setLight(LightTexture.FULL_BRIGHT);
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, Vec3 p, float[] c, float a) {
        vertex(buffer, matrix, p.x, p.y, p.z, c, a);
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, double x, double y, double z, float[] c, float a) {
        buffer.addVertex(matrix, (float) x, (float) y, (float) z).setColor(c[0], c[1], c[2], a);
    }
}
