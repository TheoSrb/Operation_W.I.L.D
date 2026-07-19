package net.tiew.operationWild.networking.packets.to_server;

// !! À enregistrer dans OWNetworkHandler (côté to_server) !!

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.team.OWArenaManager;

/**
 * Le chef retire son défi (avant match), annule la composition en cours, ou déclare forfait si le
 * combat est déjà engagé — auquel cas la victoire revient à l'adversaire.
 */
public record CancelArenaPacket() implements CustomPacketPayload {

    public static final Type<CancelArenaPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "cancel_arena"));

    public static final StreamCodec<ByteBuf, CancelArenaPacket> STREAM_CODEC =
            StreamCodec.unit(new CancelArenaPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(CancelArenaPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp) || sp.getServer() == null) return;
            OWArenaManager.cancel(sp.getServer(), sp);
        });
    }
}
