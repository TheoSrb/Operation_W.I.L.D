package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.TarantulaEntity;
import net.tiew.operationWild.entity.client.layer.TarantulaLayer;
import net.tiew.operationWild.entity.client.model.TarantulaModel;
import net.tiew.operationWild.entity.variants.TarantulaVariant;

import java.util.EnumMap;
import java.util.Map;

public class TarantulaRenderer extends OWEntityRenderer<TarantulaEntity, TarantulaModel<TarantulaEntity>> {

    private static final Map<TarantulaVariant, ResourceLocation> TEXTURES = new EnumMap<>(TarantulaVariant.class);
    private static final ResourceLocation COBWEB = ResourceLocation.withDefaultNamespace("textures/block/cobweb.png");

    private static final int RIBBON_COLOR = 0x80FFFFFF;
    private static final int WEB_PLANE_COLOR = 0xA6FFFFFF;
    private static final double RIBBON_WIDTH = 0.16;
    private static final float RIBBON_V0 = 0f, RIBBON_V1 = 1f;
    private static final double SEGMENT_LENGTH = RIBBON_WIDTH;
    private static final int MIN_SEGMENTS = 6;
    private static final int MAX_SEGMENTS = 256;
    private static final float ANCHOR_WEB_SIZE = 0.75f;
    private static final float TIP_WEB_SIZE = 0.3f;
    private static final float SPINNERET_WEB_SIZE = 0.18f;
    private static final float EXTEND_OVERSHOOT = 1.7f;
    private static final double WHIP_AMPLITUDE = 0.5;
    private static final double WHIP_RATIO = 0.12;
    private static final float WHIP_SPEED = 1.4f;
    private static final double TAUT_SAG = 0.012;
    private static final double TAUT_VIBRATION = 0.012;
    private static final double LIMP_SAG = 0.14;
    private static final long CAPTURE_FRESH_NS = 100_000_000L;
    private static final double FIRST_PERSON_FORWARD = 0.45;
    private static final double FIRST_PERSON_DROP = 0.32;
    private static final double FIRST_PERSON_SIDE = 0.34;
    private static final float WEB_ITEM_SPIN = 12f;
    private static final double WEB_ITEM_HANG = 0.125;
    private static final float WALL_PIVOT_HEIGHT = 3f / 16f;
    private static final float WALL_BELLY_DEPTH = 2f / 16f;

    static {
        for (TarantulaVariant variant : TarantulaVariant.values()) {
            TEXTURES.put(variant, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID,
                    "textures/entity/tarantula/tarantula_" + variant.getTextureName() + ".png"));
        }
    }

    public TarantulaRenderer(EntityRendererProvider.Context context) {
        super(context, new TarantulaModel<>(context.bakeLayer(TarantulaModel.LAYER_LOCATION)), 0.25f);
        this.addLayer(new TarantulaLayer<>(this));
    }

    public static ResourceLocation textureFor(TarantulaEntity tarantula) {
        return TEXTURES.get(tarantula.getVariant());
    }

    @Override
    public ResourceLocation getTextureLocation(TarantulaEntity tarantula) {
        return textureFor(tarantula);
    }

    @Override
    public boolean shouldRender(TarantulaEntity tarantula, Frustum frustum, double camX, double camY, double camZ) {
        return tarantula.isWebOut() || super.shouldRender(tarantula, frustum, camX, camY, camZ);
    }

    @Override
    public void render(TarantulaEntity tarantula, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        boolean carried = tarantula.isCarried() && !TarantulaModel.RENDER_AS_GROUNDED;
        if (!carried) super.render(tarantula, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        if (!TarantulaModel.RENDER_AS_GROUNDED) renderWeb(tarantula, partialTicks, poseStack, bufferSource, packedLight);
    }

    @Override
    protected void setupRotations(TarantulaEntity tarantula, PoseStack poseStack, float bob, float yBodyRot, float partialTicks, float scale) {
        if (!TarantulaModel.RENDER_AS_GROUNDED && tarantula.getCarrier() == null) {
            float units = poseStack.last().pose().getScale(new org.joml.Vector3f()).y;
            applyWallCling(tarantula, partialTicks, poseStack, yBodyRot, units);
        }
        super.setupRotations(tarantula, poseStack, bob, yBodyRot, partialTicks, scale);
    }

    private static void applyWallCling(TarantulaEntity tarantula, float partialTicks, PoseStack poseStack, float yaw, float units) {
        float pitch = tarantula.wallPitchDegrees(partialTicks);
        if (Math.abs(pitch) < 0.01f || tarantula.lastWallFace < 0 || units <= 0f) return;

        float hug = Math.abs(pitch) / 90f;
        net.minecraft.core.Direction face = net.minecraft.core.Direction.from2DDataValue(tarantula.lastWallFace);
        double reach = Math.max(0.0, tarantula.getBbWidth() * 0.5 - WALL_BELLY_DEPTH * units) * hug / units;
        poseStack.translate(face.getStepX() * reach, 0.0, face.getStepZ() * reach);

        poseStack.translate(0.0, WALL_PIVOT_HEIGHT, 0.0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.translate(0.0, -WALL_PIVOT_HEIGHT, 0.0);
    }

    private static Vec3 webOrigin(TarantulaEntity tarantula, float partialTicks) {
        Player carrier = tarantula.getCarrier();
        Minecraft minecraft = Minecraft.getInstance();
        if (carrier != null && carrier == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) {
            Vec3 look = carrier.getViewVector(partialTicks);
            float yaw = Mth.lerp(partialTicks, carrier.yRotO, carrier.getYRot()) * Mth.DEG_TO_RAD;
            Vec3 right = new Vec3(-Mth.cos(yaw), 0, -Mth.sin(yaw));
            double side = tarantula.carrySlot() == TarantulaEntity.SLOT_LEFT ? -FIRST_PERSON_SIDE : FIRST_PERSON_SIDE;
            return carrier.getEyePosition(partialTicks).add(look.scale(FIRST_PERSON_FORWARD)).add(right.scale(side)).add(0, -FIRST_PERSON_DROP, 0);
        }
        if (tarantula.renderedSpinneret != null && System.nanoTime() - tarantula.renderedSpinneretFrame < CAPTURE_FRESH_NS) {
            return tarantula.renderedSpinneret;
        }
        return tarantula.spinneretPosition(partialTicks);
    }

    private static Vec3 webEnd(TarantulaEntity tarantula, float partialTicks) {
        int targetId = tarantula.getWebTarget();
        if (targetId >= 0) {
            Entity target = tarantula.level().getEntity(targetId);
            if (target != null) return target.getPosition(partialTicks).add(0, target.getBbHeight() * 0.5, 0);
        }
        return tarantula.webEnd();
    }

    private void renderWeb(TarantulaEntity tarantula, float partialTicks, PoseStack poseStack,
                           MultiBufferSource bufferSource, int packedLight) {
        if (!tarantula.isWebOut()) return;

        Vec3 origin = tarantula.getPosition(partialTicks);
        Vec3 start = webOrigin(tarantula, partialTicks);
        Vec3 end = webEnd(tarantula, partialTicks);
        Vec3 toEnd = end.subtract(start);
        double reach = toEnd.length();
        if (reach < 1.0e-3) return;

        int state = tarantula.getWebState();
        float t = Mth.clamp((tarantula.getWebPhaseTick() + partialTicks) / Math.max(1, tarantula.getWebPhaseTicks()), 0f, 1f);
        double length = switch (state) {
            case TarantulaEntity.WEB_EXTENDING -> reach * Math.min(1.12, overshoot(t));
            case TarantulaEntity.WEB_RETRACTING -> Math.min(reach, tarantula.getWebLength()) * (1f - t * t);
            default -> reach;
        };
        if (length < 0.05) return;

        Vec3 direction = toEnd.scale(1.0 / reach);
        Vec3 reference = Math.abs(direction.y) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 side = direction.cross(reference).normalize();
        Vec3 lift = side.cross(direction).normalize();
        float time = tarantula.tickCount + partialTicks;

        int segments = Mth.clamp((int) Math.ceil(length / SEGMENT_LENGTH), MIN_SEGMENTS, MAX_SEGMENTS);
        Vec3[] points = new Vec3[segments + 1];
        for (int i = 0; i <= segments; i++) {
            double u = i / (double) segments;
            points[i] = strandPoint(state, t, time, start, direction, side, lift, length, u);
        }

        poseStack.pushPose();
        poseStack.translate(-origin.x, -origin.y, -origin.z);
        PoseStack.Pose pose = poseStack.last();

        VertexConsumer ribbons = bufferSource.getBuffer(RenderType.entityTranslucent(COBWEB));
        for (int i = 0; i < segments; i++) {
            double u0 = i / (double) segments;
            double u1 = (i + 1) / (double) segments;
            double w0 = RIBBON_WIDTH * 0.5 * taper(u0);
            double w1 = RIBBON_WIDTH * 0.5 * taper(u1);
            drawRibbon(pose, ribbons, points[i], points[i + 1], side, w0, w1, packedLight);
            drawRibbon(pose, ribbons, points[i], points[i + 1], lift, w0, w1, packedLight);
        }

        VertexConsumer web = bufferSource.getBuffer(RenderType.entityTranslucent(COBWEB));
        Vec3 tip = points[segments];
        float spin = tarantula.getId() * 37f;
        drawWebPlane(pose, web, start, direction, SPINNERET_WEB_SIZE, spin, packedLight);
        if (state == TarantulaEntity.WEB_ATTACHED) {
            drawWebPlane(pose, web, end.subtract(direction.scale(0.02)), direction, ANCHOR_WEB_SIZE, spin, packedLight);
            drawWebPlane(pose, web, end.subtract(direction.scale(0.05)), direction, ANCHOR_WEB_SIZE * 0.6f, spin + 45f, packedLight);
        } else if (state == TarantulaEntity.WEB_EXTENDING) {
            drawWebPlane(pose, web, tip, direction, TIP_WEB_SIZE * (0.6f + 0.4f * t), spin + time * 25f, packedLight);
        }

        ItemStack caught = tarantula.getWebItem();
        if (!caught.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(tip.x, tip.y - WEB_ITEM_HANG, tip.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(time * WEB_ITEM_SPIN));
            Minecraft.getInstance().getItemRenderer().renderStatic(caught, ItemDisplayContext.GROUND, packedLight,
                    OverlayTexture.NO_OVERLAY, poseStack, bufferSource, tarantula.level(), tarantula.getId());
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static double taper(double u) {
        return Math.min(1.0, Math.min(u, 1.0 - u) * 5.0 + 0.15);
    }

    private static void drawRibbon(PoseStack.Pose pose, VertexConsumer buffer, Vec3 from, Vec3 to, Vec3 axis, double halfFrom, double halfTo, int light) {
        Vec3 normal = to.subtract(from).cross(axis);
        if (normal.lengthSqr() < 1.0e-8) return;
        normal = normal.normalize();
        vertex(pose, buffer, from.subtract(axis.scale(halfFrom)), 0f, RIBBON_V0, light, normal, RIBBON_COLOR);
        vertex(pose, buffer, from.add(axis.scale(halfFrom)), 1f, RIBBON_V0, light, normal, RIBBON_COLOR);
        vertex(pose, buffer, to.add(axis.scale(halfTo)), 1f, RIBBON_V1, light, normal, RIBBON_COLOR);
        vertex(pose, buffer, to.subtract(axis.scale(halfTo)), 0f, RIBBON_V1, light, normal, RIBBON_COLOR);
    }

    private static float overshoot(float t) {
        float c3 = EXTEND_OVERSHOOT + 1f;
        float u = t - 1f;
        return 1f + c3 * u * u * u + EXTEND_OVERSHOOT * u * u;
    }

    private static Vec3 strandPoint(int state, float t, float time, Vec3 start, Vec3 direction, Vec3 side, Vec3 lift, double length, double u) {
        Vec3 point = start.add(direction.scale(length * u));
        double envelope = Math.sin(Math.PI * u);
        double sagShape = 4.0 * u * (1.0 - u);
        switch (state) {
            case TarantulaEntity.WEB_EXTENDING -> {
                double amplitude = Math.min(WHIP_AMPLITUDE, length * WHIP_RATIO) * (1.0 - t) * (1.0 - t);
                double phase = time * WHIP_SPEED;
                point = point.add(side.scale(amplitude * envelope * Math.sin(u * Math.PI * 3.0 - phase)))
                        .add(lift.scale(amplitude * 0.6 * envelope * Math.cos(u * Math.PI * 2.0 - phase)));
            }
            case TarantulaEntity.WEB_RETRACTING -> {
                double sag = LIMP_SAG * length * Math.min(1.0, t * 1.5);
                point = point.add(0, -sag * sagShape, 0)
                        .add(side.scale(0.05 * envelope * Math.sin(u * Math.PI * 4.0 - time * 0.8)));
            }
            default -> {
                double vibration = TAUT_VIBRATION * envelope * Math.sin(time * 1.3 + u * 10.0);
                point = point.add(0, -TAUT_SAG * length * sagShape, 0).add(side.scale(vibration));
            }
        }
        return point;
    }

    private static void drawWebPlane(PoseStack.Pose pose, VertexConsumer buffer, Vec3 center, Vec3 normal, float size, float rollDegrees, int light) {
        Vec3 reference = Math.abs(normal.y) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 a = normal.cross(reference).normalize();
        Vec3 b = normal.cross(a).normalize();
        double roll = rollDegrees * Mth.DEG_TO_RAD;
        Vec3 u = a.scale(Math.cos(roll)).add(b.scale(Math.sin(roll))).scale(size * 0.5);
        Vec3 v = b.scale(Math.cos(roll)).subtract(a.scale(Math.sin(roll))).scale(size * 0.5);
        Vec3 facing = normal.scale(-1);
        vertex(pose, buffer, center.subtract(u).subtract(v), 0f, 1f, light, facing, WEB_PLANE_COLOR);
        vertex(pose, buffer, center.add(u).subtract(v), 1f, 1f, light, facing, WEB_PLANE_COLOR);
        vertex(pose, buffer, center.add(u).add(v), 1f, 0f, light, facing, WEB_PLANE_COLOR);
        vertex(pose, buffer, center.subtract(u).add(v), 0f, 0f, light, facing, WEB_PLANE_COLOR);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, Vec3 position, float u, float v, int light, Vec3 normal, int color) {
        buffer.addVertex(pose, (float) position.x, (float) position.y, (float) position.z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
    }

    @Override
    protected void renderEntityInfo(TarantulaEntity tarantula, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Player player) {
        if (tarantula.isCarried()) return;
        super.renderEntityInfo(tarantula, poseStack, bufferSource, packedLight, player);
    }

    @Override
    public double distanceToShowRealInfos() {
        return 3;
    }

    @Override
    public double infosUpOffset() {
        return -0.2;
    }
}
