package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class OWHeadTrackerLayer<T extends OWEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final double MAX_DRIFT = 3.0;
    private static final Vector3f CORNER = new Vector3f();
    private static final float[] BOUNDS = new float[6];

    public OWHeadTrackerLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!entity.isSleeping() || !(this.getParentModel() instanceof HierarchicalModel<?> model)) return;

        BOUNDS[0] = BOUNDS[1] = BOUNDS[2] = Float.POSITIVE_INFINITY;
        BOUNDS[3] = BOUNDS[4] = BOUNDS[5] = Float.NEGATIVE_INFINITY;
        model.root().visit(poseStack, (pose, path, index, cube) -> {
            if (!path.endsWith("/head") && !path.contains("/head/")) return;
            Matrix4f matrix = pose.pose();
            for (int corner = 0; corner < 8; corner++) {
                float x = ((corner & 1) == 0 ? cube.minX : cube.maxX) / 16f;
                float y = ((corner & 2) == 0 ? cube.minY : cube.maxY) / 16f;
                float z = ((corner & 4) == 0 ? cube.minZ : cube.maxZ) / 16f;
                matrix.transformPosition(x, y, z, CORNER);
                BOUNDS[0] = Math.min(BOUNDS[0], CORNER.x);
                BOUNDS[1] = Math.min(BOUNDS[1], CORNER.y);
                BOUNDS[2] = Math.min(BOUNDS[2], CORNER.z);
                BOUNDS[3] = Math.max(BOUNDS[3], CORNER.x);
                BOUNDS[4] = Math.max(BOUNDS[4], CORNER.y);
                BOUNDS[5] = Math.max(BOUNDS[5], CORNER.z);
            }
        });
        if (BOUNDS[0] > BOUNDS[3]) return;

        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vec3 top = new Vec3(camera.x + (BOUNDS[0] + BOUNDS[3]) * 0.5, camera.y + BOUNDS[4], camera.z + (BOUNDS[2] + BOUNDS[5]) * 0.5);
        double reach = entity.getBbWidth() + entity.getBbHeight() + MAX_DRIFT;
        if (top.distanceToSqr(entity.position()) > reach * reach) return;
        entity.setRenderedHeadTop(top);
    }
}
