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

/** Le chef (ou un adjoint) active le suivi de la réputation de sa tribu (opération unique). */
public record EnableTribeReputationPacket() implements CustomPacketPayload {

    public static final Type<EnableTribeReputationPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "enable_tribe_reputation"));

    public static final StreamCodec<ByteBuf, EnableTribeReputationPacket> STREAM_CODEC =
            StreamCodec.unit(new EnableTribeReputationPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(EnableTribeReputationPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp)) return;
            MinecraftServer server = sp.getServer();
            if (server == null) return;
            OWTribesSavedData data = OWTribesSavedData.get(server);

            OWTeam team = data.findTeamByMember(sp.getUUID());
            if (team == null) return;
            // Seuls le chef et les adjoints peuvent activer la réputation.
            if (!team.isChief(sp.getUUID()) && !team.isDeputy(sp.getUUID())) return;
            if (team.isReputationEnabled()) return; // déjà activée

            team.setReputationEnabled(true);
            data.putTribe(team);
            // Instantané de l'XP d'apprivoisement des membres en ligne pour un score correct dès l'activation.
            net.tiew.operationWild.team.OWReputationData rep = net.tiew.operationWild.team.OWReputationData.get(server);
            for (java.util.UUID member : team.getPlayerUUIDs()) {
                ServerPlayer mp = server.getPlayerList().getPlayer(member);
                if (mp != null) rep.setTamingXp(member, net.tiew.operationWild.core.OWTamingXp.getTamingXp(mp));
            }
            OWTribeManager.syncTribeToOnlineMembers(server, team);
            OWTribeManager.broadcastTribeList(server);
        });
    }
}
