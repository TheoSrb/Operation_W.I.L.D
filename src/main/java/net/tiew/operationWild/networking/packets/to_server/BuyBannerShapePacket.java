package net.tiew.operationWild.networking.packets.to_server;

// !! À enregistrer dans OWNetworkHandler (côté to_server) !!

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.core.OWBannerUnlocks;
import net.tiew.operationWild.core.OWCurrency;
import net.tiew.operationWild.team.OWTeamBannerShape;

/** Achat d'une forme de bannière à prix avec des Pièces Sauvages. */
public record BuyBannerShapePacket(int shapeId) implements CustomPacketPayload {

    public static final Type<BuyBannerShapePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "buy_banner_shape"));

    public static final StreamCodec<ByteBuf, BuyBannerShapePacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.INT, BuyBannerShapePacket::shapeId, BuyBannerShapePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(BuyBannerShapePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            OWTeamBannerShape shape = OWTeamBannerShape.byId(packet.shapeId());
            if (!shape.isPurchasable() || OWBannerUnlocks.isUnlocked(player, shape)) return; // déjà débloquée
            if (!OWCurrency.spendWildCoins(player, shape.getPrice())) return; // pièces insuffisantes
            OWBannerUnlocks.unlock(player, shape);
            OWCurrency.syncWildCoins(player);
            OWBannerUnlocks.sync(player);
        });
    }
}
