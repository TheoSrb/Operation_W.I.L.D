package net.tiew.operationWild.entity.client.render.misc;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.tiew.operationWild.entity.animals.terrestrial.GiraffeEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.attacks.OWAttacksHandler;
import org.joml.Matrix4f;

import java.util.List;

public final class GiraffeSentinelRenderer {

    private static final float[] GOLD = {1f, 0.82f, 0.32f};
    private static final float[] LIGHT = {1f, 0.95f, 0.7f};
    private static final float[] AIM = {1f, 0.88f, 0.45f};

    private static final int SEGMENTS = 32;
    private static final int RING_SEGMENTS = 48;
    private static final double VIEW_RANGE = 112.0;
    private static final double ARC_HEIGHT = 1.4;
    private static final double WAVE_AMPLITUDE = 0.12;
    private static final double BEAM_HEIGHT = 9.0;
    private static final float CORE_WIDTH = 0.06f;
    private static final float GLOW_WIDTH = 0.24f;

    private GiraffeSentinelRenderer() {
    }

    public static void render(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui) return;

        Vec3 cam = event.getCamera().getPosition();
        List<GiraffeEntity> giraffes = mc.level.getEntitiesOfClass(GiraffeEntity.class, new AABB(cam, cam).inflate(VIEW_RANGE),
                giraffe -> giraffe.isSentinelActive() || giraffe.getBlessingTargetId() >= 0);

        GiraffeEntity ridden = mc.player.getRootVehicle() instanceof GiraffeEntity g && g.getControllingPassenger() == mc.player
                && OWAttacksHandler.getActiveSecondary(g) == OWAttacksHandler.GiraffeAttacks.BLESSING && g.canBless() ? g : null;
        LivingEntity aimed = ridden != null ? ridden.pickBlessingTarget(mc.player) : null;
        if (giraffes.isEmpty() && aimed == null) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float time = (mc.level.getGameTime() + partialTick) * 0.05f;

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

        for (GiraffeEntity giraffe : giraffes) {
            if (giraffe.isSentinelActive()) {
                float remaining = giraffe.getSentinelActive() - partialTick;
                float strength = Mth.clamp(remaining / 20f, 0f, 1f);
                Vec3 origin = giraffe.hornTop().add(giraffe.getPosition(partialTick).subtract(giraffe.position()));
                Vec3 feet = giraffe.getPosition(partialTick).add(0, 0.08, 0);
                float breathe = 1f + 0.02f * Mth.sin(time * 3f);
                ring(buffer, matrix, feet, (float) giraffe.sentinelRadius() * breathe, 0.16f, GOLD, 0.55f * strength);
                ring(buffer, matrix, feet, 2.2f, 0.12f, LIGHT, 0.7f * strength);
                for (LivingEntity ally : giraffe.getSentinelLinkedEntities()) {
                    Vec3 target = center(ally, partialTick);
                    drawRibbon(buffer, matrix, cam, origin, target, time, GLOW_WIDTH, GOLD, 0.22f * strength, 0f);
                    drawRibbon(buffer, matrix, cam, origin, target, time, CORE_WIDTH, LIGHT, 0.9f * strength, 0f);
                    ring(buffer, matrix, ally.getPosition(partialTick).add(0, 0.06, 0), Math.max(0.6f, ally.getBbWidth() * 0.75f), 0.08f, GOLD, 0.6f * strength);
                }
            }

            LivingEntity blessed = giraffe.getBlessingTarget();
            if (blessed != null) {
                Vec3 horns = giraffe.hornTop().add(giraffe.getPosition(partialTick).subtract(giraffe.position()));
                if (giraffe.isBlessing()) {
                    float remaining = Mth.clamp((giraffe.getBlessingTick() - partialTick) / OWAttacksConstants.Giraffe.BLESSING_MAX_TICKS, 0f, 1f);
                    float fade = Mth.clamp(remaining * 6f, 0f, 1f);
                    drawRibbon(buffer, matrix, cam, horns, center(blessed, partialTick), time * 1.5f, GLOW_WIDTH * 0.8f, GOLD, 0.25f * fade, 0f);
                    drawRibbon(buffer, matrix, cam, horns, center(blessed, partialTick), time * 1.5f, CORE_WIDTH, LIGHT, 0.85f * fade, 0f);
                    ring(buffer, matrix, blessed.getPosition(partialTick).add(0, 0.06, 0), Math.max(0.7f, blessed.getBbWidth() * 0.8f), 0.12f, GOLD, 0.8f * fade);
                    drawBeam(buffer, matrix, cam, blessed, partialTick, 0.5f + 0.1f * Mth.sin(time * 6f), fade);
                }
            }
        }

        if (aimed != null) {
            float pulse = 0.6f + 0.4f * Mth.sin(time * 7f);
            ring(buffer, matrix, aimed.getPosition(partialTick).add(0, 0.06, 0), Math.max(0.7f, aimed.getBbWidth() * 0.8f), 0.14f, AIM, 0.8f * pulse);
        }

        MeshData mesh = buffer.build();
        if (mesh != null) BufferUploader.drawWithShader(mesh);

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        pose.popPose();
    }

    private static Vec3 center(LivingEntity entity, float partialTick) {
        return entity.getPosition(partialTick).add(0, entity.getBbHeight() * 0.6, 0);
    }

    private static void drawBeam(BufferBuilder buffer, Matrix4f matrix, Vec3 cam, LivingEntity target, float partialTick, float life, float strength) {
        Vec3 base = target.getPosition(partialTick);
        float radius = Math.max(0.45f, target.getBbWidth() * 0.6f);
        Vec3 bottom = base;
        Vec3 top = base.add(0, target.getBbHeight() + BEAM_HEIGHT, 0);
        Vec3 axis = top.subtract(bottom).normalize();
        Vec3 side = axis.cross(cam.subtract(bottom.add(0, target.getBbHeight() * 0.5, 0)));
        if (side.lengthSqr() < 1.0E-6) side = new Vec3(1, 0, 0);
        side = side.normalize();

        quad(buffer, matrix, bottom, top, side.scale(radius * 1.6), GOLD, 0.18f * strength, 0f);
        quad(buffer, matrix, bottom, top, side.scale(radius * 0.7), GOLD, 0.45f * strength, 0.1f);
        quad(buffer, matrix, bottom, top, side.scale(radius * 0.25), LIGHT, 0.9f * strength, 0.2f);
        ring(buffer, matrix, base.add(0, 0.06, 0), radius * 1.8f * (1.2f - life * 0.4f), 0.14f, GOLD, 0.7f * strength);
    }

    private static void quad(BufferBuilder buffer, Matrix4f matrix, Vec3 bottom, Vec3 top, Vec3 half, float[] c, float a, float topFade) {
        vertex(buffer, matrix, bottom.add(half), c, a);
        vertex(buffer, matrix, bottom.subtract(half), c, a);
        vertex(buffer, matrix, top.subtract(half), c, a * topFade);
        vertex(buffer, matrix, top.add(half), c, a * topFade);
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
                                   float width, float[] color, float alpha, float phase) {
        Vec3 span = to.subtract(from);
        if (span.lengthSqr() < 1.0E-4 || alpha <= 0.01f) return;
        Vec3 direction = span.normalize();
        Vec3 lateral = direction.cross(new Vec3(0, 1, 0));
        if (lateral.lengthSqr() < 1.0E-4) lateral = new Vec3(1, 0, 0);
        lateral = lateral.normalize();

        Vec3 previous = null;
        Vec3 previousSide = null;
        float previousAlpha = 0f;

        for (int i = 0; i <= SEGMENTS; i++) {
            float t = (float) i / SEGMENTS;
            double arc = Math.sin(Math.PI * t) * ARC_HEIGHT;
            double wave = Math.sin(t * 12.0 - time * 7.0 + phase) * WAVE_AMPLITUDE * Math.sin(Math.PI * t);
            Vec3 point = from.add(span.scale(t)).add(0, arc, 0).add(lateral.scale(wave));

            Vec3 side = direction.cross(cam.subtract(point));
            if (side.lengthSqr() < 1.0E-6) side = lateral;
            float pulse = 1f + 0.3f * Mth.sin(time * 5f - t * 16f + phase);
            side = side.normalize().scale(width * pulse);
            float a = alpha * (0.6f + 0.4f * Mth.sin((float) Math.PI * t));

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

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, Vec3 p, float[] c, float a) {
        vertex(buffer, matrix, p.x, p.y, p.z, c, a);
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, double x, double y, double z, float[] c, float a) {
        buffer.addVertex(matrix, (float) x, (float) y, (float) z).setColor(c[0], c[1], c[2], a);
    }
}
