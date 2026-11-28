package net.tiew.operationWild.entity.client.render.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.cocoon.CocoonClientState;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class TarantulaCocoonRenderer {

    private static final ResourceLocation COCOON = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tarantula/cocoon.png");
    private static final ResourceLocation COCOON_HAT = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/tarantula/cocoon_overlay.png");
    private static final ResourceLocation COBWEB = ResourceLocation.withDefaultNamespace("textures/block/cobweb.png");

    private static final int COCOON_COLOR = 0xE6FFFFFF;
    private static final int HAT_COLOR = 0xFFFFFFFF;
    private static final float HAT_INFLATE = 0.035f;
    private static final int STRAND_COLOR = 0xE6FFFFFF;
    private static final float MARGIN = 0.07f;
    private static final float HEAD_CLEARANCE = 0.78f;
    private static final float SMALL_BODY_HEIGHT = 1.0f;
    private static final float SMALL_BODY_COVER = 0.92f;
    private static final float MIN_HEIGHT = 0.12f;
    private static final float SLAB_HEIGHT = 0.25f;
    private static final float SLAB_BULGE = 0.06f;
    private static final float SLAB_SHIFT = 0.02f;
    private static final float TEXTURE_BLOCK = 1.0f;
    private static final int MIN_STRANDS = 1;
    private static final int MAX_STRANDS = 3;
    private static final float STRAND_MIN_SIZE = 0.35f;
    private static final float STRAND_MAX_SIZE = 0.55f;
    private static final float STRAND_LIFT = 0.047f;
    private static final float BREATH = 0.015f;
    private static final long POP_MS = 250L;
    private static final float POP_SCALE = 0.05f;

    private static Level trackedLevel;

    private TarantulaCocoonRenderer() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != trackedLevel) {
            CocoonClientState.clear();
            trackedLevel = mc.level;
        }
        if (mc.level != null && !mc.isPaused()) CocoonClientState.tick();
    }

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        CocoonClientState.Cocoon cocoon = CocoonClientState.get(entity.getId());
        if (cocoon == null) return;

        float partialTick = event.getPartialTick();
        float shown = Mth.lerp(partialTick, cocoon.shownO, cocoon.shown);
        if (shown <= 0.01f) return;

        float width = entity.getBbWidth();
        float height = entity.getBbHeight();
        float time = entity.tickCount + partialTick;
        long sinceBuilt = System.currentTimeMillis() - cocoon.builtAtMs;
        float pop = sinceBuilt < POP_MS ? Mth.sin((float) sinceBuilt / POP_MS * Mth.PI) * POP_SCALE : 0f;
        float cover = height < SMALL_BODY_HEIGHT ? height * SMALL_BODY_COVER : height * HEAD_CLEARANCE;
        float built = Math.max(MIN_HEIGHT, (cover + MARGIN) * shown);

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        float bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        poseStack.mulPose(Axis.YP.rotationDegrees(-bodyYaw));
        float breath = cocoon.locked ? Mth.sin(time * 0.25f) * BREATH : 0f;
        float scale = 1f + pop + breath;
        poseStack.scale(scale, 1f + pop * 0.5f, scale);
        PoseStack.Pose pose = poseStack.last();
        int light = event.getPackedLight();

        float baseHalf = width * 0.5f + MARGIN;
        int slabs = Math.max(1, Mth.ceil(built / SLAB_HEIGHT));
        float[] halves = new float[slabs];
        Vec3[] shifts = new Vec3[slabs];
        for (int j = 0; j < slabs; j++) {
            long seed = entity.getId() * 7L + j * 13L;
            float rounding = 1f - 0.08f * Math.abs(2f * (j + 0.5f) / slabs - 1f);
            halves[j] = baseHalf * rounding * (1f + SLAB_BULGE * noise(seed));
            shifts[j] = new Vec3(SLAB_SHIFT * noise(seed + 3), 0, SLAB_SHIFT * noise(seed + 5));
        }

        VertexConsumer silk = event.getMultiBufferSource().getBuffer(RenderType.entityTranslucent(COCOON));
        for (int j = 0; j < slabs; j++) {
            float y0 = j * SLAB_HEIGHT;
            drawSlab(pose, silk, shifts[j], halves[j], y0, Math.min(built, y0 + SLAB_HEIGHT), j == 0, true, COCOON_COLOR, light);
        }

        VertexConsumer hat = event.getMultiBufferSource().getBuffer(RenderType.entityCutoutNoCull(COCOON_HAT));
        for (int j = 0; j < slabs; j++) {
            float y0 = j * SLAB_HEIGHT;
            float y1 = Math.min(built, y0 + SLAB_HEIGHT);
            boolean last = j == slabs - 1;
            drawSlab(pose, hat, shifts[j], halves[j] + HAT_INFLATE, y0, last ? y1 + HAT_INFLATE : y1, false, last, HAT_COLOR, light);
        }

        VertexConsumer web = event.getMultiBufferSource().getBuffer(RenderType.entityTranslucent(COBWEB));
        int strands = Math.round(Mth.lerp(shown, MIN_STRANDS, MAX_STRANDS));
        for (int i = 0; i < strands; i++) {
            long seed = entity.getId() * 31L + i * 17L;
            int face = Math.floorMod((int) (seed ^ (seed >>> 7)), 4);
            float y = built * (0.12f + 0.35f * (noise(seed + 1) * 0.5f + 0.5f));
            int slab = Mth.clamp((int) (y / SLAB_HEIGHT), 0, slabs - 1);
            float size = Mth.lerp(noise(seed + 2) * 0.5f + 0.5f, STRAND_MIN_SIZE, STRAND_MAX_SIZE);
            float along = noise(seed + 4) * (halves[slab] - size * 0.5f);
            drawStrand(pose, web, face, halves[slab] + STRAND_LIFT, shifts[slab], along, y, size, light);
        }

        poseStack.popPose();
    }

    private static float noise(long seed) {
        long x = seed * 6364136223846793005L + 1442695040888963407L;
        x ^= x >>> 33;
        return ((x & 0xFFFF) / (float) 0xFFFF) * 2f - 1f;
    }

    private static void drawSlab(PoseStack.Pose pose, VertexConsumer buffer, Vec3 shift, float half, float y0, float y1, boolean bottom, boolean top,
                                 int color, int light) {
        float size = half * 2f;
        float h = y1 - y0;
        Vec3 x = new Vec3(1, 0, 0);
        Vec3 z = new Vec3(0, 0, 1);
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 base = shift.add(0, y0, 0);
        drawFace(pose, buffer, base.add(-half, 0, half), x, up, size, h, y0, new Vec3(0, 0, 1), color, light);
        drawFace(pose, buffer, base.add(half, 0, -half), x.scale(-1), up, size, h, y0, new Vec3(0, 0, -1), color, light);
        drawFace(pose, buffer, base.add(half, 0, half), z.scale(-1), up, size, h, y0, new Vec3(1, 0, 0), color, light);
        drawFace(pose, buffer, base.add(-half, 0, -half), z, up, size, h, y0, new Vec3(-1, 0, 0), color, light);
        if (top) drawFace(pose, buffer, base.add(-half, h, half), x, z.scale(-1), size, size, 0f, up, color, light);
        if (bottom) drawFace(pose, buffer, base.add(-half, 0.001, -half), x, z, size, size, 0f, up.scale(-1), color, light);
    }

    private static void drawFace(PoseStack.Pose pose, VertexConsumer buffer, Vec3 corner, Vec3 right, Vec3 up, float width, float height,
                                 float vOffset, Vec3 normal, int color, int light) {
        float u = 0f;
        while (u < width - 1.0e-4f) {
            float uEnd = Math.min(width, (float) Math.floor(u / TEXTURE_BLOCK + 1f) * TEXTURE_BLOCK);
            float v = 0f;
            while (v < height - 1.0e-4f) {
                float worldV = vOffset + v;
                float vEnd = Math.min(height, (float) Math.floor(worldV / TEXTURE_BLOCK + 1f) * TEXTURE_BLOCK - vOffset);
                float u0 = Mth.frac(u / TEXTURE_BLOCK);
                float u1 = u0 + (uEnd - u) / TEXTURE_BLOCK;
                float v1 = 1f - Mth.frac(worldV / TEXTURE_BLOCK);
                float v0 = v1 - (vEnd - v) / TEXTURE_BLOCK;
                Vec3 a = corner.add(right.scale(u)).add(up.scale(v));
                Vec3 b = corner.add(right.scale(uEnd)).add(up.scale(v));
                Vec3 c = corner.add(right.scale(uEnd)).add(up.scale(vEnd));
                Vec3 d = corner.add(right.scale(u)).add(up.scale(vEnd));
                quad(pose, buffer, a, b, c, d, normal, color, light, u0, u1, v1, v0);
                v = vEnd;
            }
            u = uEnd;
        }
    }

    private static void drawStrand(PoseStack.Pose pose, VertexConsumer buffer, int face, float distance, Vec3 shift, float along, float y,
                                   float size, int light) {
        Vec3 normal = switch (face) {
            case 0 -> new Vec3(0, 0, 1);
            case 1 -> new Vec3(0, 0, -1);
            case 2 -> new Vec3(1, 0, 0);
            default -> new Vec3(-1, 0, 0);
        };
        Vec3 right = new Vec3(-normal.z, 0, normal.x);
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 center = shift.add(normal.scale(distance)).add(right.scale(along)).add(0, y, 0);
        Vec3 r = right.scale(size * 0.5);
        Vec3 u = up.scale(size * 0.5);
        quad(pose, buffer, center.subtract(r).subtract(u), center.add(r).subtract(u), center.add(r).add(u), center.subtract(r).add(u),
                normal, STRAND_COLOR, light, 0f, 1f, 1f, 0f);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, Vec3 a, Vec3 b, Vec3 c, Vec3 d, Vec3 normal, int color, int light,
                             float u0, float u1, float v0, float v1) {
        vertex(pose, buffer, a, u0, v0, normal, color, light);
        vertex(pose, buffer, b, u1, v0, normal, color, light);
        vertex(pose, buffer, c, u1, v1, normal, color, light);
        vertex(pose, buffer, d, u0, v1, normal, color, light);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, Vec3 position, float u, float v, Vec3 normal, int color, int light) {
        buffer.addVertex(pose, (float) position.x, (float) position.y, (float) position.z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
    }
}
