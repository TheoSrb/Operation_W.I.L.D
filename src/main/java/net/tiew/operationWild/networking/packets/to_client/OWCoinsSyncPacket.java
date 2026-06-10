package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.gui.OWCoinGainOverlay;
import net.tiew.operationWild.networking.ClientCoinData;

/**
 * Synchronise vers le client le total de "Pièces Sauvages" du joueur. Si {@code gained > 0}, déclenche
 * aussi l'animation/son de gain.
 *
 * @param total  total de pièces du joueur après l'opération
 * @param gained nombre de pièces gagnées à l'instant (0 = simple resynchronisation, pas de feedback)
 */
public record OWCoinsSyncPacket(int total, int gained) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OWCoinsSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ow_coins_sync"));

    public static final StreamCodec<FriendlyByteBuf, OWCoinsSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OWCoinsSyncPacket::total,
            ByteBufCodecs.VAR_INT, OWCoinsSyncPacket::gained,
            OWCoinsSyncPacket::new
    );

    @Override
    public CustomPacketPayload.Type<OWCoinsSyncPacket> type() {
        return TYPE;
    }

    public static void handle(OWCoinsSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientCoinData.wildCoins = packet.total();
            if (packet.gained() > 0) {
                OWCoinGainOverlay.trigger(packet.gained());
            }
        });
    }
}
