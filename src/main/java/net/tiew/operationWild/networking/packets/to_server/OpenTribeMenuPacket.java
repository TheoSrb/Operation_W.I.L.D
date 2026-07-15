package net.tiew.operationWild.networking.packets.to_server;

// !! À enregistrer dans OWNetworkHandler (côté to_server) !!

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.team.OWTribeManager;

/** Le joueur ouvre son menu de tribu (touche T) : le serveur lui renvoie sa tribu + la liste des tribus. */
public record OpenTribeMenuPacket() implements CustomPacketPayload {

    public static final Type<OpenTribeMenuPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "open_tribe_menu"));

    public static final StreamCodec<ByteBuf, OpenTribeMenuPacket> STREAM_CODEC =
            StreamCodec.unit(new OpenTribeMenuPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(OpenTribeMenuPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp)) return;
            MinecraftServer server = sp.getServer();
            if (server == null) return;
            OWTribeManager.syncPlayerTribe(server, sp);
            OWTribeManager.syncTribeList(server, sp);
        });
    }
}
