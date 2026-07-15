package net.tiew.operationWild.networking.packets.to_server;

// !! À enregistrer dans OWNetworkHandler (côté to_server) !!

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
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

import java.util.UUID;

/** Le chef transfère son rôle à un autre membre (par UUID). */
public record TransferChiefPacket(String targetUuid) implements CustomPacketPayload {

    public static final Type<TransferChiefPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "transfer_chief"));

    public static final StreamCodec<ByteBuf, TransferChiefPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, TransferChiefPacket::targetUuid, TransferChiefPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(TransferChiefPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp)) return;
            MinecraftServer server = sp.getServer();
            if (server == null) return;
            OWTribesSavedData data = OWTribesSavedData.get(server);

            OWTeam team = data.findTeamByMember(sp.getUUID());
            if (team == null || !sp.getUUID().equals(team.getTeamOwnerUUID())) return;

            UUID target;
            try { target = UUID.fromString(packet.targetUuid()); }
            catch (IllegalArgumentException e) { return; }
            if (target.equals(team.getTeamOwnerUUID()) || !team.isMember(target)) return;

            team.setTeamOwnerUUID(target);
            data.putTribe(team); // dirty

            OWTribeManager.refreshEntitiesOfTribe(server, team);
            OWTribeManager.syncTribeToOnlineMembers(server, team);
            OWTribeManager.broadcastTribeList(server);
        });
    }
}
