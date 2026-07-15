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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Le chef dissout entièrement sa tribu : tous les membres (et leurs entités) sont libérés. */
public record DisbandTribePacket() implements CustomPacketPayload {

    public static final Type<DisbandTribePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "disband_tribe"));

    public static final StreamCodec<ByteBuf, DisbandTribePacket> STREAM_CODEC =
            StreamCodec.unit(new DisbandTribePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(DisbandTribePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp)) return;
            MinecraftServer server = sp.getServer();
            if (server == null) return;
            OWTribesSavedData data = OWTribesSavedData.get(server);

            OWTeam team = data.findTeamByMember(sp.getUUID());
            if (team == null || !sp.getUUID().equals(team.getTeamOwnerUUID())) return;

            List<UUID> members = new ArrayList<>(team.getPlayerUUIDs());
            data.removeTribe(team.getTeamId());

            for (UUID member : members) {
                OWTribeManager.refreshEntitiesOfPlayer(server, member);
                ServerPlayer mp = server.getPlayerList().getPlayer(member);
                if (mp != null) OWTribeManager.syncPlayerTribe(server, mp);
            }
            OWTribeManager.broadcastTribeList(server);
        });
    }
}
