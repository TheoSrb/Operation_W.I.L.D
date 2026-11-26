package net.tiew.operationWild.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tiew.operationWild.entity.attacks.OWAttackIds;
import net.tiew.operationWild.networking.packets.to_server.OWAttackPacket;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.ChameleonEntity;
import net.tiew.operationWild.entity.attacks.OWAttackLogic;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class ChameleonVineController {

    private static int anchoredChameleonId = -1;
    private static double ropeLength = 0.0;
    private static int lastTongueState = ChameleonEntity.TONGUE_IDLE;
    private static int releaseGraceTicks = 0;
    private static int predictedReleaseTicks = 0;
    private static boolean launched = false;

    private ChameleonVineController() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide() || !player.isLocalPlayer()) return;

        ChameleonEntity chameleon = carriedChameleon(player);
        trackTongueCooldown(chameleon, player);
        if (predictedReleaseTicks > 0) predictedReleaseTicks--;
        if (chameleon == null || player.getVehicle() != null
                || chameleon.getTongueState() != ChameleonEntity.TONGUE_ATTACHED || predictedReleaseTicks > 0) {
            if (predictedReleaseTicks <= 0) anchoredChameleonId = -1;
            return;
        }
        launched = false;

        Vec3 anchor = chameleon.tongueEnd();
        Vec3 rope = ChameleonEntity.ropePoint(player);
        double minLength = OWAttacksConstants.Chameleon.VINE_MIN_LENGTH;
        double maxLength = chameleon.tongueRange();

        if (anchoredChameleonId != chameleon.getId()) {
            anchoredChameleonId = chameleon.getId();
            ropeLength = Math.max(minLength, rope.distanceTo(anchor));
        }

        if (player.isShiftKeyDown()) {
            ropeLength = Math.max(minLength, ropeLength - OWAttacksConstants.Chameleon.VINE_REEL_SPEED);
        } else if (Minecraft.getInstance().options.keyJump.isDown() && !player.onGround()) {
            ropeLength = Math.min(maxLength, ropeLength + OWAttacksConstants.Chameleon.VINE_REEL_SPEED);
        }

        Vec3 velocity = player.getDeltaMovement();
        if (player.zza > 0 && !player.onGround()) {
            Vec3 radial = rope.subtract(anchor).normalize();
            Vec3 look = player.getLookAngle().multiply(1, 0, 1);
            Vec3 tangent = look.subtract(radial.scale(look.dot(radial)));
            if (tangent.lengthSqr() > 1.0e-4) {
                velocity = velocity.add(tangent.normalize().scale(OWAttacksConstants.Chameleon.VINE_PUMP));
                player.setDeltaMovement(velocity);
            }
        }
        Vec3 fromAnchor = rope.add(velocity).subtract(anchor);
        double distance = fromAnchor.length();
        if (distance <= ropeLength || distance < 1.0e-4) return;

        Vec3 onRope = anchor.add(fromAnchor.scale(ropeLength / distance));
        player.setDeltaMovement(onRope.subtract(rope).scale(OWAttacksConstants.Chameleon.VINE_DAMPING));
        player.resetFallDistance();
    }

    @SubscribeEvent
    public static void onPlayerTickEnd(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide() || !player.isLocalPlayer()) return;

        ChameleonEntity chameleon = carriedChameleon(player);
        boolean attached = anchoredChameleonId >= 0 && chameleon != null && chameleon.getId() == anchoredChameleonId
                && chameleon.getTongueState() == ChameleonEntity.TONGUE_ATTACHED && predictedReleaseTicks <= 0;
        if (!attached) {
            if (releaseGraceTicks > 0) {
                releaseGraceTicks--;
                if (player.onGround() || player.isInWater()) releaseGraceTicks = 0;
                else keepMomentum(player, OWAttacksConstants.Chameleon.VINE_RELEASE_AIR_KEEP);
            }
            return;
        }
        releaseGraceTicks = OWAttacksConstants.Chameleon.VINE_RELEASE_GRACE_TICKS;
        if (!player.onGround()) keepMomentum(player, OWAttacksConstants.Chameleon.VINE_AIR_KEEP);

        Vec3 anchor = chameleon.tongueEnd();
        Vec3 rope = ChameleonEntity.ropePoint(player);
        Vec3 fromAnchor = rope.subtract(anchor);
        double distance = fromAnchor.length();
        if (distance <= ropeLength + OWAttacksConstants.Chameleon.VINE_HARD_SLACK || distance < 1.0e-4) return;

        Vec3 normal = fromAnchor.scale(1.0 / distance);
        Vec3 correction = normal.scale(ropeLength + OWAttacksConstants.Chameleon.VINE_HARD_SLACK - distance);
        player.setPos(player.getX() + correction.x, player.getY() + correction.y, player.getZ() + correction.z);

        Vec3 velocity = player.getDeltaMovement();
        double outward = velocity.dot(normal);
        if (outward > 0) player.setDeltaMovement(velocity.subtract(normal.scale(outward)));
        player.resetFallDistance();
    }

    public static void predictRelease(ChameleonEntity chameleon, Player player) {
        if (chameleon.getTongueState() != ChameleonEntity.TONGUE_ATTACHED || chameleon.getId() != anchoredChameleonId) return;
        predictedReleaseTicks = OWAttacksConstants.Chameleon.VINE_PREDICTION_TIMEOUT_TICKS;
        launch(player);
    }

    private static void launch(Player player) {
        if (launched) return;
        launched = true;
        releaseGraceTicks = OWAttacksConstants.Chameleon.VINE_RELEASE_GRACE_TICKS;
        if (player.onGround() || player.isInWater()) return;

        Vec3 velocity = player.getDeltaMovement();
        if (velocity.y < OWAttacksConstants.Chameleon.VINE_LAUNCH_MIN_VERTICAL) return;
        double share = Mth.clamp(velocity.length() / OWAttacksConstants.Chameleon.VINE_LAUNCH_SPEED_REFERENCE,
                OWAttacksConstants.Chameleon.VINE_LAUNCH_MIN_LIFT_SHARE, 1.0);
        double rise = Math.max(velocity.y, 0.0) * OWAttacksConstants.Chameleon.VINE_LAUNCH_VERTICAL_KEEP
                + OWAttacksConstants.Chameleon.VINE_LAUNCH_LIFT * share;
        player.setDeltaMovement(velocity.x, rise, velocity.z);
        player.resetFallDistance();
    }

    private static void keepMomentum(Player player, double keep) {
        Vec3 velocity = player.getDeltaMovement();
        double factor = keep / OWAttacksConstants.Chameleon.AIR_FRICTION;
        double x = velocity.x * factor;
        double z = velocity.z * factor;
        double horizontal = Math.sqrt(x * x + z * z);
        double max = OWAttacksConstants.Chameleon.VINE_MAX_SPEED;
        if (horizontal > max) {
            x *= max / horizontal;
            z *= max / horizontal;
        }
        player.setDeltaMovement(x, velocity.y, z);
    }

    @SubscribeEvent
    public static void onUseKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem()) return;
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        ChameleonEntity chameleon = carriedChameleon(player);
        if (chameleon == null || chameleon.getTongueState() != ChameleonEntity.TONGUE_ATTACHED) return;
        predictRelease(chameleon, player);

        PacketDistributor.sendToServer(new OWAttackPacket(OWAttackIds.TONGUE_VINE, OWAttackPacket.ACTION_EXECUTE, 0f));
        event.setSwingHand(false);
        event.setCanceled(true);
    }

    private static void trackTongueCooldown(@Nullable ChameleonEntity chameleon, Player player) {
        int state = chameleon == null ? ChameleonEntity.TONGUE_IDLE : chameleon.getTongueState();
        if (lastTongueState == ChameleonEntity.TONGUE_ATTACHED && state != ChameleonEntity.TONGUE_ATTACHED) {
            if (!launched && player.getVehicle() == null) launch(player);
            predictedReleaseTicks = 0;
        }
        if (chameleon != null && lastTongueState != ChameleonEntity.TONGUE_IDLE && state == ChameleonEntity.TONGUE_IDLE) {
            OWAttackLogic.startChameleonTongueCooldown(chameleon);
        }
        lastTongueState = state;
    }

    private static @Nullable ChameleonEntity carriedChameleon(Player player) {
        for (Entity passenger : player.getPassengers()) {
            if (passenger instanceof ChameleonEntity chameleon) return chameleon;
        }
        return null;
    }
}
