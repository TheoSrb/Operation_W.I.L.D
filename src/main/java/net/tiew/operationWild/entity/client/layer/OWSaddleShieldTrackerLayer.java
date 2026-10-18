package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWSaddleShield;
import net.tiew.operationWild.entity.client.render.OWEntityRenderer;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_server.SaddleShieldPosePacket;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public class OWSaddleShieldTrackerLayer<T extends OWEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final double MAX_DRIFT = 3.0;

    public OWSaddleShieldTrackerLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        List<OWSaddleShield> shields = entity.saddleShields();
        if (shields.isEmpty() || !entity.isSaddled() || OWEntityRenderer.SUPPRESS_INFO_IN_GUI) return;
        if (!(this.getParentModel() instanceof HierarchicalModel<?> model)) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !OWSaddleShield.isPoseReporter(entity, minecraft.player)) return;

        long tick = entity.level().getGameTime();
        if (entity.lastShieldPoseReport == tick) return;

        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        Vec3 origin = new Vec3(Mth.lerp(partialTick, entity.xo, entity.getX()),
                Mth.lerp(partialTick, entity.yo, entity.getY()),
                Mth.lerp(partialTick, entity.zo, entity.getZ()));
        float bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        double reach = entity.getBbWidth() + entity.getBbHeight() + MAX_DRIFT;

        float[] data = new float[shields.size() * OWSaddleShield.FLOATS_PER_POSE];
        boolean[] found = new boolean[shields.size()];
        boolean[] drifted = new boolean[1];

        model.root().visit(poseStack, (pose, path, cubeIndex, cube) -> {
            for (int index = 0; index < shields.size(); index++) {
                OWSaddleShield shield = shields.get(index);
                if (found[index] || !path.endsWith("/" + shield.part())) continue;
                if (!shield.matchesCube(cube.maxX - cube.minX, cube.maxY - cube.minY, cube.maxZ - cube.minZ)) continue;

                Matrix4f matrix = pose.pose();
                Vector3f center = matrix.transformPosition((cube.minX + cube.maxX) / 32f,
                        (cube.minY + cube.maxY) / 32f, (cube.minZ + cube.maxZ) / 32f, new Vector3f());
                Vector3f axisX = matrix.transformDirection(1f, 0f, 0f, new Vector3f());
                Vector3f axisY = matrix.transformDirection(0f, 1f, 0f, new Vector3f());

                Vec3 localCenter = OWSaddleShield.toLocal(origin, bodyYaw, camera.add(center.x, center.y, center.z));
                if (localCenter.lengthSqr() > reach * reach) drifted[0] = true;

                int offset = index * OWSaddleShield.FLOATS_PER_POSE;
                write(data, offset, localCenter);
                write(data, offset + 3, OWSaddleShield.rotateToLocal(bodyYaw, new Vec3(axisX.x, axisX.y, axisX.z)));
                write(data, offset + 6, OWSaddleShield.rotateToLocal(bodyYaw, new Vec3(axisY.x, axisY.y, axisY.z)));
                found[index] = true;
                return;
            }
        });

        if (drifted[0]) return;
        for (boolean shieldFound : found) if (!shieldFound) return;

        entity.lastShieldPoseReport = tick;
        OWNetworkHandler.sendToServer(new SaddleShieldPosePacket(entity.getId(), data));
    }

    private static void write(float[] data, int offset, Vec3 vector) {
        data[offset] = (float) vector.x;
        data[offset + 1] = (float) vector.y;
        data[offset + 2] = (float) vector.z;
    }
}
