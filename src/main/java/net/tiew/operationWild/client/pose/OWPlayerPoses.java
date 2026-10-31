package net.tiew.operationWild.client.pose;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.tiew.operationWild.OperationWild;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class OWPlayerPoses {

    private static final List<OWPlayerPose<?>> POSES = List.of(
            OWGrabbedPoses.CROCODILE,
            OWGrabbedPoses.TIGER,
            OWGrabbedPoses.BOA,
            OWGrabbedPoses.KANGAROO,
            OWAirbornePoses.FLIGHT,
            OWSpearThrowPose.INSTANCE,
            OWAssailedPoses.LATCH,
            OWRiderPoses.RODEO,
            OWRiderPoses.SURF,
            OWRiderPoses.PRONE,
            OWAssailedPoses.HYPNOSIS,
            OWAssailedPoses.DIZZY,
            OWShoulderPetPose.INSTANCE);

    private static final int SPEAR_THROW = POSES.indexOf(OWSpearThrowPose.INSTANCE);
    private static final int FORGET_AFTER_TICKS = 40;
    private static final float MIN_WEIGHT = 0.001f;

    private static final Map<Integer, OWPlayerPoseState> STATES = new HashMap<>();
    private static final Set<Integer> PUSHED = new HashSet<>();
    private static Level trackedLevel;
    private static int clock;

    private OWPlayerPoses() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != trackedLevel) {
            STATES.clear();
            trackedLevel = mc.level;
        }
        if (mc.level == null || mc.isPaused()) return;

        clock++;
        for (Player player : mc.level.players()) {
            OWPlayerPoseState state = STATES.computeIfAbsent(player.getId(), id -> create(player));
            state.lastSeen = clock;
            state.observe(player);

            boolean claimed = !player.isAlive() || player.isSpectator();
            for (int i = 0; i < POSES.size(); i++) {
                OWPlayerPose<?> pose = POSES.get(i);
                boolean active = tick(pose, player, state, i) && !claimed;
                if (active) claimed = true;
                state.weightO[i] = state.weight[i];
                state.weight[i] = OWPoseMath.approach(state.weight[i], active ? 1f : 0f, active ? pose.blendIn() : pose.blendOut());
            }
        }
        STATES.values().removeIf(state -> clock - state.lastSeen > FORGET_AFTER_TICKS);
    }

    public static void applyPose(PlayerModel<?> model, Player player, float ageInTicks) {
        if (ageInTicks <= 0f) return;
        OWPlayerPoseState state = STATES.get(player.getId());
        if (state == null) return;

        float partialTick = Mth.clamp(ageInTicks - player.tickCount, 0f, 1f);
        boolean posed = false;
        for (int i = 0; i < POSES.size(); i++) {
            float weight = state.weight(i, partialTick);
            if (weight <= MIN_WEIGHT) continue;
            pose(POSES.get(i), model, player, state, i, weight, partialTick);
            posed = true;
        }
        if (posed) OWPoseMath.syncLayers(model);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        OWPlayerPoseState state = STATES.get(player.getId());
        if (state == null) return;

        float partialTick = event.getPartialTick();
        float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
        PoseStack poseStack = event.getPoseStack();
        boolean pushed = false;
        for (int i = 0; i < POSES.size(); i++) {
            float weight = state.weight(i, partialTick);
            if (weight <= MIN_WEIGHT) continue;
            if (!pushed) {
                poseStack.pushPose();
                PUSHED.add(player.getId());
                pushed = true;
            }
            body(POSES.get(i), poseStack, player, state, i, weight, partialTick, bodyYaw);
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        OWPlayerPoseState state = STATES.get(player.getId());
        if (state == null) return;
        OWSpearThrowPose.INSTANCE.firstPerson(event.getPoseStack(), (OWSpearThrowPose.ThrowData) state.data[SPEAR_THROW],
                event.getHand(), event.getPartialTick());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        if (PUSHED.remove(event.getEntity().getId())) event.getPoseStack().popPose();
    }

    private static OWPlayerPoseState create(Player player) {
        OWPlayerPoseState state = new OWPlayerPoseState(POSES.size());
        for (int i = 0; i < POSES.size(); i++) state.data[i] = POSES.get(i).createData(player);
        return state;
    }

    @SuppressWarnings("unchecked")
    private static <D> boolean tick(OWPlayerPose<D> pose, Player player, OWPlayerPoseState state, int index) {
        return pose.tick(player, (D) state.data[index], state);
    }

    @SuppressWarnings("unchecked")
    private static <D> void pose(OWPlayerPose<D> pose, PlayerModel<?> model, Player player, OWPlayerPoseState state,
                                 int index, float weight, float partialTick) {
        pose.pose(model, player, (D) state.data[index], weight, partialTick);
    }

    @SuppressWarnings("unchecked")
    private static <D> void body(OWPlayerPose<D> pose, PoseStack poseStack, Player player, OWPlayerPoseState state,
                                 int index, float weight, float partialTick, float bodyYaw) {
        pose.body(poseStack, player, (D) state.data[index], weight, partialTick, bodyYaw);
    }
}
