package net.tiew.operationWild.networking.packets.to_client;

// !! À enregistrer dans OWNetworkHandler (côté to_client) !!

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.client.OWClientBannerUnlocks;

/** Pousse au joueur le bitmask des formes de bannière qu'il a débloquées. */
public record SyncBannerUnlocksPacket(int mask) implements CustomPacketPayload {

    public static final Type<SyncBannerUnlocksPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "sync_banner_unlocks"));

    public static final StreamCodec<ByteBuf, SyncBannerUnlocksPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.INT, SyncBannerUnlocksPacket::mask, SyncBannerUnlocksPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SyncBannerUnlocksPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> OWClientBannerUnlocks.mask = packet.mask());
    }
}
