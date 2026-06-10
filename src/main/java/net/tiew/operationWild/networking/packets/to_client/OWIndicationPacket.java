package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.gui.OWIndicationOverlay;

/**
 * Affiche côté client une indication / didacticiel (encart en fondu).
 *
 * @param translationKey clé de traduction du texte à afficher
 * @param durationTicks  durée d'affichage en ticks (fondus d'entrée/sortie inclus)
 */
public record OWIndicationPacket(String translationKey, int durationTicks) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OWIndicationPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ow_indication"));

    public static final StreamCodec<FriendlyByteBuf, OWIndicationPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, OWIndicationPacket::translationKey,
            ByteBufCodecs.VAR_INT, OWIndicationPacket::durationTicks,
            OWIndicationPacket::new
    );

    @Override
    public CustomPacketPayload.Type<OWIndicationPacket> type() {
        return TYPE;
    }

    public static void handle(OWIndicationPacket packet, IPayloadContext context) {
        context.enqueueWork(() ->
                OWIndicationOverlay.show(Component.translatable(packet.translationKey()), packet.durationTicks()));
    }
}
