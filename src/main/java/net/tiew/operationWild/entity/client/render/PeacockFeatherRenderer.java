package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.misc.PeacockFeatherEntity;
import net.tiew.operationWild.entity.variants.PeacockVariant;

public class PeacockFeatherRenderer extends EntityRenderer<PeacockFeatherEntity> {

    private static final float LENGTH = 1.35f;
    private static final float WIDTH = LENGTH * 0.5f;
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[PeacockVariant.values().length];

    static {
        for (PeacockVariant variant : PeacockVariant.values()) {
            TEXTURES[variant.getId()] = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID,
                    "textures/entity/peacock/feather/" + variant.name().toLowerCase(java.util.Locale.ROOT) + ".png");
        }
    }

    public PeacockFeatherRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public static ResourceLocation texture(int variant) {
        return variant >= 0 && variant < TEXTURES.length && TEXTURES[variant] != null ? TEXTURES[variant] : TEXTURES[0];
    }

    public static void quad(PoseStack.Pose pose, VertexConsumer consumer, int light,
                            Vec3 tip, Vec3 tail, Vec3 halfWidth, Vec3 normal) {
        vertex(pose, consumer, light, tip.subtract(halfWidth), 0f, 0f, normal);
        vertex(pose, consumer, light, tail.subtract(halfWidth), 0f, 1f, normal);
        vertex(pose, consumer, light, tail.add(halfWidth), 1f, 1f, normal);
        vertex(pose, consumer, light, tip.add(halfWidth), 1f, 0f, normal);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, int light, Vec3 at, float u, float v, Vec3 normal) {
        consumer.addVertex(pose, (float) at.x, (float) at.y, (float) at.z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
    }

    @Override
    public void render(PeacockFeatherEntity entity, float yaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        float scale = entity.fadeScale(partialTick);
        if (scale <= 0.01f) return;

        float yRot = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot()) * Mth.DEG_TO_RAD;
        float xRot = Mth.lerp(partialTick, entity.xRotO, entity.getXRot()) * Mth.DEG_TO_RAD;
        Vec3 dir = new Vec3(Mth.sin(yRot) * Mth.cos(xRot), Mth.sin(xRot), Mth.cos(yRot) * Mth.cos(xRot));

        Vec3 position = new Vec3(Mth.lerp(partialTick, entity.xo, entity.getX()),
                Mth.lerp(partialTick, entity.yo, entity.getY()),
                Mth.lerp(partialTick, entity.zo, entity.getZ())).add(0, entity.getBbHeight() * 0.5, 0);
        Vec3 center = new Vec3(0, entity.getBbHeight() * 0.5, 0);
        Vec3 toCamera = this.entityRenderDispatcher.camera.getPosition().subtract(position);
        Vec3 side = dir.cross(toCamera);
        if (side.lengthSqr() < 1.0E-6) side = dir.cross(new Vec3(0, 1, 0));
        if (side.lengthSqr() < 1.0E-6) side = new Vec3(1, 0, 0);
        side = side.normalize();
        Vec3 normal = side.cross(dir).normalize();

        Vec3 half = dir.scale(LENGTH * 0.5f * scale);
        Vec3 halfWidth = side.scale(WIDTH * 0.5f * scale);
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(texture(entity.getVariant())));
        quad(pose.last(), consumer, light, center.add(half), center.subtract(half), halfWidth, normal);

        super.render(entity, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(PeacockFeatherEntity entity) {
        return texture(entity.getVariant());
    }
}
