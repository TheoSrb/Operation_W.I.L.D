package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.networking.ClientTamingData;

/**
 * Synchronise vers le client le total d'Expérience d'Apprivoisement du joueur (cagnotte par joueur).
 * L'animation « +N » de gain est gérée séparément par {@code OWXpGainPacket(amount, taming=true)}.
 *
 * @param total total d'Expérience d'Apprivoisement du joueur après l'opération
 */
public record OWTamingXpSyncPacket(double total) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OWTamingXpSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ow_taming_xp_sync"));

    public static final StreamCodec<FriendlyByteBuf, OWTamingXpSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, OWTamingXpSyncPacket::total,
            OWTamingXpSyncPacket::new
    );

    @Override
    public CustomPacketPayload.Type<OWTamingXpSyncPacket> type() {
        return TYPE;
    }

    public static void handle(OWTamingXpSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientTamingData.tamingXp = packet.total());
    }
}
