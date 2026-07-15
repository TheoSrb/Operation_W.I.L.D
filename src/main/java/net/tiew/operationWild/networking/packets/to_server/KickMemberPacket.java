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

/** Le chef exclut un membre (par UUID). Le membre exclu et ses entités quittent la tribu. */
public record KickMemberPacket(String targetUuid) implements CustomPacketPayload {

    public static final Type<KickMemberPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "kick_member"));

    public static final StreamCodec<ByteBuf, KickMemberPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, KickMemberPacket::targetUuid, KickMemberPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(KickMemberPacket packet, IPayloadContext context) {
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

            team.removePlayerMember(target);
            data.putTribe(team); // dirty

            OWTribeManager.refreshEntitiesOfPlayer(server, target);
            ServerPlayer kicked = server.getPlayerList().getPlayer(target);
            if (kicked != null) OWTribeManager.syncPlayerTribe(server, kicked);
            OWTribeManager.refreshEntitiesOfTribe(server, team);
            OWTribeManager.syncTribeToOnlineMembers(server, team);
            OWTribeManager.broadcastTribeList(server);
        });
    }
}
