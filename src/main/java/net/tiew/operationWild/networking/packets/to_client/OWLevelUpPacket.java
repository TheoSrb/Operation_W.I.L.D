package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.gui.OWLevelUpOverlay;

/**
 * Déclenche côté client l'animation de passage de niveau (anneaux concentriques + chiffre).
 *
 * @param level       le niveau atteint à afficher
 * @param entityColor couleur de l'entité (couleur des anneaux)
 */
public record OWLevelUpPacket(int level, int entityColor) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OWLevelUpPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ow_level_up"));

    public static final StreamCodec<FriendlyByteBuf, OWLevelUpPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OWLevelUpPacket::level,
            ByteBufCodecs.INT, OWLevelUpPacket::entityColor,
            OWLevelUpPacket::new
    );

    @Override
    public CustomPacketPayload.Type<OWLevelUpPacket> type() {
        return TYPE;
    }

    public static void handle(OWLevelUpPacket packet, IPayloadContext context) {
        context.enqueueWork(() ->
                OWLevelUpOverlay.trigger(packet.level(), packet.entityColor()));
    }
}
