package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.gui.OWXpGainOverlay;

/**
 * Déclenche l'animation « +N orbes » d'obtention d'XP (récompense de quête), pendant du gain de pièces.
 *
 * @param amount nombre d'orbes gagnés
 */
public record OWXpGainPacket(int amount) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OWXpGainPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ow_xp_gain"));

    public static final StreamCodec<FriendlyByteBuf, OWXpGainPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OWXpGainPacket::amount,
            OWXpGainPacket::new
    );

    @Override
    public CustomPacketPayload.Type<OWXpGainPacket> type() {
        return TYPE;
    }

    public static void handle(OWXpGainPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> OWXpGainOverlay.trigger(packet.amount()));
    }
}
