package net.tiew.operationWild.networking.packets.to_server;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.core.OWArenaVenue;
import net.tiew.operationWild.core.OWArenaVenueUnlocks;
import net.tiew.operationWild.core.OWCurrency;

/** Achat d'un décor d'arène avec des Pièces Sauvages. */
public record BuyArenaVenuePacket(int venueId) implements CustomPacketPayload {

    public static final Type<BuyArenaVenuePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "buy_arena_venue"));

    public static final StreamCodec<ByteBuf, BuyArenaVenuePacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.INT, BuyArenaVenuePacket::venueId, BuyArenaVenuePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(BuyArenaVenuePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            OWArenaVenue venue = OWArenaVenue.byId(packet.venueId());
            if (!venue.isPurchasable() || OWArenaVenueUnlocks.isUnlocked(player, venue)) return;
            if (!OWCurrency.spendWildCoins(player, venue.getPrice())) return;
            OWArenaVenueUnlocks.unlock(player, venue);
            OWCurrency.syncWildCoins(player);
            OWArenaVenueUnlocks.sync(player);
        });
    }
}
