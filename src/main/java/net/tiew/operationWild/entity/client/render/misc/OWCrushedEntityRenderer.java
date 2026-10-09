package net.tiew.operationWild.entity.client.render.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.tiew.operationWild.OperationWild;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class OWCrushedEntityRenderer {

    private static final float IMPACT_TICKS = 2.5f;
    private static final float SETTLE_TICKS = 4f;
    private static final float RELEASE_TICKS = 8f;
    private static final float SQUASH_HEIGHT = 0.75f;
    private static final float SQUASH_WIDTH = 0.35f;
    private static final float STRETCH_HEIGHT = 0.5f;
    private static final float STRETCH_WIDTH = 0.25f;
    private static final float MIN_HEIGHT = 0.15f;

    private static final Map<Integer, Crush> CRUSHED = new HashMap<>();
    private static final Set<Integer> PUSHED = new HashSet<>();

    private OWCrushedEntityRenderer() {}

    private record Crush(long startTick, int durationTicks) {}

    public static void crush(int entityId, int durationTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        CRUSHED.put(entityId, new Crush(mc.level.getGameTime(), durationTicks));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            CRUSHED.clear();
            return;
        }
        long now = mc.level.getGameTime();
        CRUSHED.entrySet().removeIf(entry -> {
            long elapsed = now - entry.getValue().startTick();
            return elapsed < 0 || elapsed > entry.getValue().durationTicks() + RELEASE_TICKS + 2;
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        Crush crush = CRUSHED.get(event.getEntity().getId());
        if (crush == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float elapsed = (mc.level.getGameTime() - crush.startTick()) + event.getPartialTick();
        float squash = squashAmount(elapsed, crush.durationTicks());
        if (Math.abs(squash) < 0.001f) return;

        float height;
        float width;
        if (squash >= 0f) {
            height = 1f - SQUASH_HEIGHT * squash;
            width = 1f + SQUASH_WIDTH * squash;
        } else {
            height = 1f - STRETCH_HEIGHT * squash;
            width = 1f + STRETCH_WIDTH * squash;
        }

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.scale(width, Math.max(MIN_HEIGHT, height), width);
        PUSHED.add(event.getEntity().getId());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        if (!PUSHED.remove(event.getEntity().getId())) return;
        event.getPoseStack().popPose();
    }

    private static float squashAmount(float elapsed, int duration) {
        if (elapsed < 0f) return 0f;

        if (elapsed < IMPACT_TICKS) {
            float t = elapsed / IMPACT_TICKS;
            return 1.12f * (1f - (1f - t) * (1f - t));
        }

        if (elapsed < duration) {
            float settle = Mth.clamp((elapsed - IMPACT_TICKS) / SETTLE_TICKS, 0f, 1f);
            float overshoot = 0.12f * (1f - settle * settle * (3f - 2f * settle));
            float tremble = 0.035f * Mth.sin(elapsed * 1.4f) * (float) Math.exp(-(elapsed - IMPACT_TICKS) / 10f);
            return 1f + overshoot + tremble;
        }

        float u = (elapsed - duration) / RELEASE_TICKS;
        if (u >= 1f) return 0f;
        float decay = (1f - u) * (1f - u);
        return Mth.cos(u * (float) Math.PI * 2.5f) * decay;
    }
}
