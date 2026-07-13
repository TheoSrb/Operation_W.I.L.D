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
 * Déclenche l'animation « +N » d'obtention de récompense, pendant du gain de pièces.
 *
 * @param amount montant gagné (orbes d'XP, ou expérience d'apprivoisement si {@code taming})
 * @param taming {@code true} → animation « Expérience d'Apprivoisement » (entité niveau max) ;
 *               {@code false} → animation d'XP classique
 */
public record OWXpGainPacket(int amount, boolean taming) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OWXpGainPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ow_xp_gain"));

    public static final StreamCodec<FriendlyByteBuf, OWXpGainPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OWXpGainPacket::amount,
            ByteBufCodecs.BOOL,    OWXpGainPacket::taming,
            OWXpGainPacket::new
    );

    @Override
    public CustomPacketPayload.Type<OWXpGainPacket> type() {
        return TYPE;
    }

    public static void handle(OWXpGainPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (packet.taming()) {
                net.tiew.operationWild.gui.OWTamingXpGainOverlay.trigger(packet.amount());
            } else {
                OWXpGainOverlay.trigger(packet.amount());
            }
        });
    }
}
