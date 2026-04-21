package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.attacks.OWAttack;
import net.tiew.operationWild.entity.attacks.OWChargedAttack;

import java.util.HashMap;
import java.util.Map;

/**
 * Universal attack packet — one packet covers all attack types.
 *
 * Actions:
 *   ACTION_EXECUTE        (0) — trigger an instant attack
 *   ACTION_CHARGE_START   (1) — player pressed the key, start charging
 *   ACTION_CHARGE_CANCEL  (2) — released too early, cancel
 *   ACTION_CHARGE_RELEASE (3) — released after min charge; value = chargeFactor [0..1]
 */
public record OWAttackPacket(int attackId, byte action, float value) implements CustomPacketPayload {

    public static final byte ACTION_EXECUTE        = 0;
    public static final byte ACTION_CHARGE_START   = 1;
    public static final byte ACTION_CHARGE_CANCEL  = 2;
    public static final byte ACTION_CHARGE_RELEASE = 3;

    public static final CustomPacketPayload.Type<OWAttackPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ow_attack"));

    public static final StreamCodec<FriendlyByteBuf, OWAttackPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> { buf.writeInt(p.attackId()); buf.writeByte(p.action()); buf.writeFloat(p.value()); },
            buf -> new OWAttackPacket(buf.readInt(), buf.readByte(), buf.readFloat())
    );

    private static final Map<Integer, OWAttack> REGISTRY = new HashMap<>();

    public static void register(OWAttack attack) {
        REGISTRY.put(attack.getId(), attack);
    }

    public static OWAttack getAttack(int id) {
        return REGISTRY.get(id);
    }

    @Override
    public CustomPacketPayload.Type<OWAttackPacket> type() {
        return TYPE;
    }

    public static void handle(OWAttackPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.getRootVehicle() instanceof OWEntity entity)) return;

            OWAttack attack = REGISTRY.get(packet.attackId());
            if (attack == null) return;

            switch (packet.action()) {
                case ACTION_EXECUTE -> attack.trigger(entity);

                case ACTION_CHARGE_START -> {
                    if (attack instanceof OWChargedAttack c) c.onChargeStart(entity);
                }

                case ACTION_CHARGE_CANCEL -> {
                    if (attack instanceof OWChargedAttack c) c.onChargeCancel(entity);
                }

                case ACTION_CHARGE_RELEASE -> {
                    if (attack instanceof OWChargedAttack c) c.onChargeRelease(entity, packet.value());
                }
            }
        });
    }
}
