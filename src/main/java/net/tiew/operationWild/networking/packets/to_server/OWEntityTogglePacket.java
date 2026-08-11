package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;

public record OWEntityTogglePacket(String option) implements CustomPacketPayload {

    public static final Type<OWEntityTogglePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ow_entity_toggle"));

    public static final StreamCodec<FriendlyByteBuf, OWEntityTogglePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, OWEntityTogglePacket::option,
                    OWEntityTogglePacket::new
            );

    @Override
    public Type<OWEntityTogglePacket> type() { return TYPE; }

    public static void handle(OWEntityTogglePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.getRootVehicle() instanceof OWEntity entity) {
                // L'option bascule est toujours celle de la monture du joueur : aucun identifiant
                // d'entité n'est accepté depuis le client, donc aucune entité tierce n'est joignable.
                switch (packet.option()) {
                    case "passive"    -> entity.switchMode(player);
                    case "autoPickup" -> entity.setAutoPickup(!entity.isAutoPickup());
                    case "tribeFlag"  -> entity.setShowTribeFlag(!entity.isShowTribeFlag());
                    case "followOwner" -> entity.setFollowingOwner(!entity.isFollowingOwner());
                    default -> {}
                }
            }
        });
    }
}
