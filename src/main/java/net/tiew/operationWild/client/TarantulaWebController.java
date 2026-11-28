package net.tiew.operationWild.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.TarantulaEntity;
import net.tiew.operationWild.entity.attacks.OWAttackIds;
import net.tiew.operationWild.entity.attacks.OWAttackLogic;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.client.layer.TarantulaWristLayer;
import net.tiew.operationWild.entity.client.model.TarantulaModel;
import net.tiew.operationWild.event.OWItemUseRestrictions;
import net.tiew.operationWild.networking.packets.to_server.OWAttackPacket;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class TarantulaWebController {

    private static final double GRAVITY_COMPENSATION = 0.0784;
    private static final int REEL_SOUND_INTERVAL = 5;
    private static final float PEEK_SIDE = 0.98f;
    private static final float PEEK_DOWN = -0.6f;
    private static final float PEEK_FORWARD = -0.9f;
    private static final float PEEK_HIDE = 0.45f;
    private static final float PEEK_YAW = 40f;
    private static final float PEEK_PITCH = 12f;
    private static final float PEEK_SCALE = 0.55f;

    private static final boolean[] PRESSED = new boolean[2];
    private static final boolean[] PRESS_STARTED_ATTACHED = new boolean[2];
    private static final long[] PRESS_MS = new long[2];
    private static final double[] ROPE_LENGTH = {-1.0, -1.0};
    private static final int[] PREDICTED_RELEASE = new int[2];
    private static final int[] ANCHORED_ID = {-1, -1};
    private static int reelTicks = 0;
    private static boolean renderingArm = false;
    private static @Nullable TarantulaModel<TarantulaEntity> handModel;

    private TarantulaWebController() {
    }


    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.screen != null) return;

        int slot = event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT ? TarantulaEntity.SLOT_RIGHT
                : event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT ? TarantulaEntity.SLOT_LEFT : TarantulaEntity.SLOT_NONE;
        if (slot == TarantulaEntity.SLOT_NONE) return;

        TarantulaEntity tarantula = TarantulaEntity.carriedOn(player, slot);

        if (event.getAction() == GLFW.GLFW_PRESS && slot == TarantulaEntity.SLOT_RIGHT && player.isShiftKeyDown()
                && player.getMainHandItem().isEmpty() && player.getVehicle() == null
                && (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.ENTITY)) {
            TarantulaEntity leaving = net.tiew.operationWild.entity.OWCarriedCompanions.getCarried(player) instanceof TarantulaEntity selected ? selected : null;
            if (leaving != null && !leaving.isWebAttached() && leaving.canPilotAttacks(player)) {
                PacketDistributor.sendToServer(new OWAttackPacket(OWAttackIds.WEB_GRAPPLE, OWAttackPacket.ACTION_COMPANION_DISMOUNT, leaving.carrySlot()));
                event.setCanceled(true);
                return;
            }
        }

        if (tarantula == null || player.getVehicle() != null || !tarantula.canPilotAttacks(player)) return;

        if (event.getAction() == GLFW.GLFW_RELEASE) {
            if (!PRESSED[slot]) return;
            PRESSED[slot] = false;
            long held = System.currentTimeMillis() - PRESS_MS[slot];
            if (PRESS_STARTED_ATTACHED[slot] && held < OWAttacksConstants.Tarantula.WEB_TAP_MS && tarantula.isWebAttached()) {
                detach(tarantula, player, slot);
            }
            return;
        }
        if (event.getAction() != GLFW.GLFW_PRESS) return;

        if (tarantula.isWebAttached()) {
            press(slot, true);
            event.setCanceled(true);
            return;
        }

        if (slot == TarantulaEntity.SLOT_RIGHT && OWItemUseRestrictions.holdsRightClickItem(player)) return;
        if (mc.hitResult != null && mc.hitResult.getType() != HitResult.Type.MISS) return;

        if (tarantula.isWebOut()) {
            press(slot, false);
            event.setCanceled(true);
            return;
        }

        if (!tarantula.isWebLoaded() && !tarantula.isEating() && tarantula.getWebCooldown() <= 0) {
            autoSelect(player, tarantula, slot);
            PacketDistributor.sendToServer(new OWAttackPacket(OWAttackIds.WEB_GRAPPLE, OWAttackPacket.ACTION_EXECUTE, slot));
            OWAttackLogic.recordAttackClick(OWAttackIds.WEB_GRAPPLE, true);
            event.setCanceled(true);
            return;
        }

        if (!tarantula.canShootWeb()) {
            if (tarantula.getWebCooldown() <= 0 && tarantula.isWebLoaded()) tarantula.canShowVitalEnergyLack = true;
            OWAttackLogic.recordAttackClick(OWAttackIds.WEB_GRAPPLE, true);
            event.setCanceled(true);
            return;
        }

        autoSelect(player, tarantula, slot);
        PacketDistributor.sendToServer(new OWAttackPacket(OWAttackIds.WEB_GRAPPLE, OWAttackPacket.ACTION_EXECUTE, slot));
        OWAttackLogic.recordAttackClick(OWAttackIds.WEB_GRAPPLE, false);
        press(slot, false);
        event.setCanceled(true);
    }

    private static void autoSelect(Player player, TarantulaEntity tarantula, int slot) {
        if (net.tiew.operationWild.entity.OWCarriedCompanions.selectedSlot(player) == slot) return;
        net.tiew.operationWild.entity.OWCarriedCompanions.selectSlot(player, slot);
        if (TarantulaEntity.carriedOn(player, 1 - slot) != null) tarantula.flashSelection();
    }

    private static void press(int slot, boolean startedAttached) {
        PRESSED[slot] = true;
        PRESS_STARTED_ATTACHED[slot] = startedAttached;
        PRESS_MS[slot] = System.currentTimeMillis();
    }

    private static boolean isHeld(int slot) {
        if (!PRESSED[slot]) return false;
        long window = Minecraft.getInstance().getWindow().getWindow();
        int button = slot == TarantulaEntity.SLOT_RIGHT ? GLFW.GLFW_MOUSE_BUTTON_RIGHT : GLFW.GLFW_MOUSE_BUTTON_LEFT;
        if (GLFW.glfwGetMouseButton(window, button) != GLFW.GLFW_PRESS) PRESSED[slot] = false;
        return PRESSED[slot];
    }

    private static void detach(TarantulaEntity tarantula, Player player, int slot) {
        PREDICTED_RELEASE[slot] = OWAttacksConstants.Tarantula.PREDICTION_TIMEOUT_TICKS;
        ROPE_LENGTH[slot] = -1.0;
        hop(player, tarantula.webEnd());
        PacketDistributor.sendToServer(new OWAttackPacket(OWAttackIds.WEB_GRAPPLE, OWAttackPacket.ACTION_EXECUTE, slot));
    }

    private static void hop(Player player, Vec3 anchor) {
        if (player.onGround() || player.isInWater()) return;
        Vec3 toAnchor = anchor.subtract(TarantulaEntity.ropePoint(player));
        if (toAnchor.length() > OWAttacksConstants.Tarantula.DETACH_HOP_DISTANCE) return;
        Vec3 push = toAnchor.multiply(1, 0, 1);
        push = push.lengthSqr() > 1.0e-4 ? push.normalize().scale(OWAttacksConstants.Tarantula.DETACH_HOP_PUSH) : Vec3.ZERO;
        Vec3 velocity = player.getDeltaMovement();
        player.setDeltaMovement(velocity.x + push.x, Math.max(velocity.y, 0) + OWAttacksConstants.Tarantula.DETACH_HOP_LIFT, velocity.z + push.z);
        player.resetFallDistance();
    }


    private static boolean isAnchored(@Nullable TarantulaEntity tarantula, Player player, int slot) {
        return tarantula != null && tarantula.isWebAttached() && PREDICTED_RELEASE[slot] <= 0 && player.getVehicle() == null;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide() || !player.isLocalPlayer()) return;

        TarantulaEntity[] tarantulas = new TarantulaEntity[2];
        boolean[] anchored = new boolean[2];
        Vec3 rope = TarantulaEntity.ropePoint(player);
        Vec3 target = Vec3.ZERO;
        int reeling = 0;
        double speedFactor = 1.0;

        for (int slot = 0; slot < 2; slot++) {
            TarantulaEntity tarantula = TarantulaEntity.carriedOn(player, slot);
            tarantulas[slot] = tarantula;
            if (PREDICTED_RELEASE[slot] > 0 && (tarantula == null || !tarantula.isWebAttached() || --PREDICTED_RELEASE[slot] <= 0)) {
                PREDICTED_RELEASE[slot] = 0;
            }
            anchored[slot] = isAnchored(tarantula, player, slot);
            if (!anchored[slot]) {
                ROPE_LENGTH[slot] = -1.0;
                ANCHORED_ID[slot] = -1;
                if (tarantula == null) PRESSED[slot] = false;
                continue;
            }
            Vec3 anchor = tarantula.webEnd();
            if (ANCHORED_ID[slot] != tarantula.getId() || ROPE_LENGTH[slot] < 0) {
                ANCHORED_ID[slot] = tarantula.getId();
                ROPE_LENGTH[slot] = Math.max(OWAttacksConstants.Tarantula.ROPE_MIN_LENGTH, rope.distanceTo(anchor));
            }
            if (isHeld(slot)) {
                target = target.add(anchor);
                reeling++;
            }
        }

        if (reeling > 0) {
            reel(player, rope, target.scale(1.0 / reeling), speedFactor);
            for (int slot = 0; slot < 2; slot++) {
                if (!anchored[slot]) continue;
                ROPE_LENGTH[slot] = Mth.clamp(rope.add(player.getDeltaMovement()).distanceTo(tarantulas[slot].webEnd()),
                        OWAttacksConstants.Tarantula.ROPE_MIN_LENGTH, tarantulas[slot].webRange());
            }
            return;
        }

        reelTicks = 0;
        for (int slot = 0; slot < 2; slot++) {
            if (anchored[slot]) constrainVelocity(player, rope, tarantulas[slot].webEnd(), ROPE_LENGTH[slot]);
        }
    }

    private static void reel(Player player, Vec3 rope, Vec3 target, double speedFactor) {
        reelTicks++;
        if (reelTicks % REEL_SOUND_INTERVAL == 1) {
            player.playSound(net.tiew.operationWild.sound.OWSounds.TARANTULA_WEB_REEL.get(), 0.5f, 0.9f + player.getRandom().nextFloat() * 0.2f);
        }
        Vec3 toTarget = target.subtract(rope);
        double distance = toTarget.length();
        Vec3 velocity = player.getDeltaMovement();

        if (distance < OWAttacksConstants.Tarantula.REEL_ARRIVE_DISTANCE) {
            Vec3 held = velocity.scale(OWAttacksConstants.Tarantula.REEL_HOLD_DAMPING)
                    .add(toTarget.scale(OWAttacksConstants.Tarantula.REEL_HOLD_GAIN));
            player.setDeltaMovement(held.x, held.y + GRAVITY_COMPENSATION, held.z);
            player.resetFallDistance();
            return;
        }

        Vec3 direction = toTarget.scale(1.0 / distance);
        float ramp = smooth(reelTicks / (float) OWAttacksConstants.Tarantula.REEL_RAMP_TICKS);
        double approach = Mth.clamp(distance / OWAttacksConstants.Tarantula.REEL_SLOW_DISTANCE,
                OWAttacksConstants.Tarantula.REEL_MIN_APPROACH, 1.0);
        double pull = OWAttacksConstants.Tarantula.REEL_PULL * speedFactor * (0.4 + 0.6 * ramp);
        double limit = OWAttacksConstants.Tarantula.REEL_MAX_SPEED * speedFactor * approach * (0.35 + 0.65 * ramp);

        velocity = velocity.add(direction.scale(pull)).add(0, GRAVITY_COMPENSATION * OWAttacksConstants.Tarantula.REEL_GRAVITY_SHARE, 0);
        double along = velocity.dot(direction);
        Vec3 lateral = velocity.subtract(direction.scale(along)).scale(OWAttacksConstants.Tarantula.REEL_LATERAL_KEEP);
        if (along > limit) along = Mth.lerp(0.5, along, limit);
        player.setDeltaMovement(direction.scale(along).add(lateral));
        player.resetFallDistance();
    }

    private static float smooth(float t) {
        t = Mth.clamp(t, 0f, 1f);
        return t * t * (3f - 2f * t);
    }

    private static void constrainVelocity(Player player, Vec3 rope, Vec3 anchor, double length) {
        Vec3 velocity = player.getDeltaMovement();
        Vec3 fromAnchor = rope.add(velocity).subtract(anchor);
        double distance = fromAnchor.length();
        if (distance <= length || distance < 1.0e-4) return;
        Vec3 onRope = anchor.add(fromAnchor.scale(length / distance));
        player.setDeltaMovement(onRope.subtract(rope));
        player.resetFallDistance();
    }

    @SubscribeEvent
    public static void onPlayerTickEnd(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide() || !player.isLocalPlayer()) return;

        for (int slot = 0; slot < 2; slot++) {
            TarantulaEntity tarantula = TarantulaEntity.carriedOn(player, slot);
            if (!isAnchored(tarantula, player, slot) || ROPE_LENGTH[slot] < 0 || isHeld(slot)) continue;

            if (!player.onGround()) keepMomentum(player);
            Vec3 anchor = tarantula.webEnd();
            Vec3 rope = TarantulaEntity.ropePoint(player);
            Vec3 fromAnchor = rope.subtract(anchor);
            double distance = fromAnchor.length();
            double limit = ROPE_LENGTH[slot] + OWAttacksConstants.Tarantula.ROPE_HARD_SLACK;
            if (distance <= limit || distance < 1.0e-4) continue;

            Vec3 normal = fromAnchor.scale(1.0 / distance);
            Vec3 correction = normal.scale(limit - distance);
            player.setPos(player.getX() + correction.x, player.getY() + correction.y, player.getZ() + correction.z);
            Vec3 velocity = player.getDeltaMovement();
            double outward = velocity.dot(normal);
            if (outward > 0) player.setDeltaMovement(velocity.subtract(normal.scale(outward)));
            player.resetFallDistance();
        }
    }

    private static void keepMomentum(Player player) {
        Vec3 velocity = player.getDeltaMovement();
        double factor = OWAttacksConstants.Tarantula.ROPE_AIR_KEEP / OWAttacksConstants.Tarantula.AIR_FRICTION;
        player.setDeltaMovement(velocity.x * factor, velocity.y, velocity.z * factor);
    }


    private static TarantulaModel<TarantulaEntity> handModel() {
        if (handModel == null) {
            handModel = new TarantulaModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(TarantulaModel.LAYER_LOCATION));
        }
        return handModel;
    }

    private static int slotOf(HumanoidArm arm) {
        return arm == HumanoidArm.LEFT ? TarantulaEntity.SLOT_LEFT : TarantulaEntity.SLOT_RIGHT;
    }

    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent event) {
        if (renderingArm) return;
        AbstractClientPlayer player = event.getPlayer();
        int slot = slotOf(event.getArm());
        TarantulaEntity tarantula = TarantulaEntity.carriedOn(player, slot);
        if (tarantula == null || tarantula.isClimbing()) return;

        if (!(Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player) instanceof PlayerRenderer renderer)) return;
        PoseStack poseStack = event.getPoseStack();
        renderArmWithTarantula(renderer, player, tarantula, event.getArm(), poseStack, event.getMultiBufferSource(), event.getPackedLight());
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        AbstractClientPlayer player = mc.player;
        if (player == null || player.isInvisible()) return;

        boolean mainHand = event.getHand() == InteractionHand.MAIN_HAND;
        ItemStack stack = event.getItemStack();
        if (mainHand && stack.isEmpty()) return;
        if (stack.is(Items.FILLED_MAP)) return;
        if (player.isUsingItem() && player.getUsedItemHand() == event.getHand()) return;

        HumanoidArm arm = mainHand ? player.getMainArm() : player.getMainArm().getOpposite();
        TarantulaEntity tarantula = TarantulaEntity.carriedOn(player, slotOf(arm));
        if (tarantula == null || tarantula.isClimbing()) return;
        if (!(mc.getEntityRenderDispatcher().getRenderer(player) instanceof PlayerRenderer renderer)) return;

        float side = arm == HumanoidArm.RIGHT ? 1f : -1f;
        if (!stack.isEmpty()) {
            renderShoulderPeek(tarantula, side, event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
            return;
        }
        float swing = mainHand ? event.getSwingProgress() : 0f;
        float swingRoot = Mth.sqrt(swing);
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(side * (-0.3f * Mth.sin(swingRoot * Mth.PI) + 0.64f),
                0.4f * Mth.sin(swingRoot * Mth.TWO_PI) - 0.6f + event.getEquipProgress() * -0.6f,
                -0.4f * Mth.sin(swing * Mth.PI) - 0.72f);
        poseStack.mulPose(Axis.YP.rotationDegrees(side * 45f));
        poseStack.mulPose(Axis.YP.rotationDegrees(side * Mth.sin(swingRoot * Mth.PI) * 70f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(side * Mth.sin(swing * swing * Mth.PI) * -20f));
        poseStack.translate(side * -1f, 3.6f, 3.5f);
        poseStack.mulPose(Axis.ZP.rotationDegrees(side * 120f));
        poseStack.mulPose(Axis.XP.rotationDegrees(200f));
        poseStack.mulPose(Axis.YP.rotationDegrees(side * -135f));
        poseStack.translate(side * 5.6f, 0f, 0f);
        renderArmWithTarantula(renderer, player, tarantula, arm, poseStack, event.getMultiBufferSource(), event.getPackedLight());
        poseStack.popPose();
    }

    private static void renderShoulderPeek(TarantulaEntity tarantula, float side, PoseStack poseStack, MultiBufferSource buffers, int light) {
        float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        float blend = tarantula.shoulderBlend(partialTick);
        if (blend <= 0.01f) return;
        poseStack.pushPose();
        poseStack.translate(side * PEEK_SIDE, PEEK_DOWN - (1f - blend) * PEEK_HIDE, PEEK_FORWARD);
        poseStack.mulPose(Axis.YP.rotationDegrees(side * PEEK_YAW));
        poseStack.mulPose(Axis.XP.rotationDegrees(PEEK_PITCH));
        poseStack.scale(-PEEK_SCALE, -PEEK_SCALE, PEEK_SCALE);
        TarantulaModel.RENDER_IN_HAND_VIEW = true;
        TarantulaModel.ARM_SCUTTLE = Mth.sin(blend * Mth.PI);
        try {
            TarantulaWristLayer.renderModel(handModel(), tarantula, partialTick, poseStack, buffers, light, 0f);
        } finally {
            TarantulaModel.RENDER_IN_HAND_VIEW = false;
            TarantulaModel.ARM_SCUTTLE = 0f;
        }
        poseStack.popPose();
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.screen != null || player.getVehicle() != null) return;
        if (!net.tiew.operationWild.core.OWKeysBinding.OW_SWITCH_ATTACK.isDown() || event.getScrollDeltaY() == 0) return;

        TarantulaEntity right = TarantulaEntity.carriedOn(player, TarantulaEntity.SLOT_RIGHT);
        TarantulaEntity left = TarantulaEntity.carriedOn(player, TarantulaEntity.SLOT_LEFT);
        if (right == null || left == null) return;

        int next = net.tiew.operationWild.entity.OWCarriedCompanions.selectedSlot(player) == TarantulaEntity.SLOT_LEFT
                ? TarantulaEntity.SLOT_RIGHT : TarantulaEntity.SLOT_LEFT;
        net.tiew.operationWild.entity.OWCarriedCompanions.selectSlot(player, next);
        PacketDistributor.sendToServer(new OWAttackPacket(OWAttackIds.WEB_GRAPPLE, OWAttackPacket.ACTION_COMPANION_SELECT, next));
        (next == TarantulaEntity.SLOT_LEFT ? left : right).flashSelection();
        player.playSound(net.tiew.operationWild.sound.OWSounds.TARANTULA_SCUTTLE.get(), 0.6f, 1.3f);
        event.setCanceled(true);
    }

    private static void renderArmWithTarantula(PlayerRenderer renderer, AbstractClientPlayer player, TarantulaEntity tarantula,
                                               HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffers, int light) {
        renderingArm = true;
        try {
            if (arm == HumanoidArm.RIGHT) renderer.renderRightHand(poseStack, buffers, light, player);
            else renderer.renderLeftHand(poseStack, buffers, light, player);
        } finally {
            renderingArm = false;
        }

        PlayerModel<AbstractClientPlayer> model = renderer.getModel();
        poseStack.pushPose();
        (arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm).translateAndRotate(poseStack);
        TarantulaModel.RENDER_IN_HAND_VIEW = true;
        try {
            float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
            TarantulaWristLayer.renderOnArm(handModel(), tarantula, slotOf(arm), partialTick, poseStack, buffers, light);
        } finally {
            TarantulaModel.RENDER_IN_HAND_VIEW = false;
        }
        poseStack.popPose();
    }
}
