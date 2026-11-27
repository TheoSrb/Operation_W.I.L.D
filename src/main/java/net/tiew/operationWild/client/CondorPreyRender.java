package net.tiew.operationWild.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.flying.CondorEntity;
import org.joml.Matrix4f;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class CondorPreyRender {

    private static final float MODEL_ORIGIN_Y = 1.501f;
    private static final Set<Integer> PUSHED = new HashSet<>();

    private CondorPreyRender() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity prey = event.getEntity();
        if (!(prey.getVehicle() instanceof CondorEntity condor) || condor.getFirstPassenger() == prey) return;
        Matrix4f frame = condor.talonFrame;
        if (frame == null) return;

        float partialTick = event.getPartialTick();
        double dx = Mth.lerp(partialTick, condor.xOld, condor.getX()) - Mth.lerp(partialTick, prey.xOld, prey.getX());
        double dy = Mth.lerp(partialTick, condor.yOld, condor.getY()) - Mth.lerp(partialTick, prey.yOld, prey.getY());
        double dz = Mth.lerp(partialTick, condor.zOld, condor.getZ()) - Mth.lerp(partialTick, prey.zOld, prey.getZ());

        float scale = condor.getScale();
        float yaw = condor.getPreciseBodyRotation(partialTick);
        Matrix4f modelToWorld = new Matrix4f()
                .scale(scale)
                .rotateY((180f - yaw) * Mth.DEG_TO_RAD)
                .scale(-1f, -1f, 1f)
                .translate(0f, -MODEL_ORIGIN_Y, 0f);
        Matrix4f carried = new Matrix4f(modelToWorld).mul(frame).mul(new Matrix4f(modelToWorld).invert());

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(dx, dy, dz);
        poseStack.mulPose(carried);
        poseStack.translate(-dx, -dy, -dz);
        PUSHED.add(prey.getId());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        if (!PUSHED.remove(event.getEntity().getId())) return;
        event.getPoseStack().popPose();
    }
}
