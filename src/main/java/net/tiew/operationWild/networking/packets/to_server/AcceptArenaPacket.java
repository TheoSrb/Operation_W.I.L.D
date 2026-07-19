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
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTribeManager;
import net.tiew.operationWild.team.OWTribesSavedData;

/**
 * Le <b>chef</b> accepte le règlement de l'arène pour sa tribu (opération unique et irréversible).
 * Tant qu'elle n'est pas acceptée, l'onglet Arène reste caché aux autres membres.
 */
public record AcceptArenaPacket() implements CustomPacketPayload {

    public static final Type<AcceptArenaPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "accept_arena"));

    public static final StreamCodec<ByteBuf, AcceptArenaPacket> STREAM_CODEC =
            StreamCodec.unit(new AcceptArenaPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(AcceptArenaPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp)) return;
            MinecraftServer server = sp.getServer();
            if (server == null) return;
            OWTribesSavedData data = OWTribesSavedData.get(server);

            OWTeam team = data.findTeamByMember(sp.getUUID());
            if (team == null) return;
            // Le chef seul engage la tribu dans l'arène (ni adjoint, ni membre).
            if (!team.isChief(sp.getUUID())) return;
            if (team.isArenaAccepted()) return; // déjà accepté

            team.setArenaAccepted(true);
            data.putTribe(team);
            // Tous les membres en ligne voient l'onglet apparaître immédiatement.
            OWTribeManager.syncTribeToOnlineMembers(server, team);
        });
    }
}
