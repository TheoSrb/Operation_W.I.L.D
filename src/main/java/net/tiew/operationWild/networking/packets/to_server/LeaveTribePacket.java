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

/**
 * Le joueur quitte sa tribu. Membre : simple retrait. Chef : transfert automatique au membre le plus
 * ancien, ou dissolution s'il est seul.
 */
public record LeaveTribePacket() implements CustomPacketPayload {

    public static final Type<LeaveTribePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "leave_tribe"));

    public static final StreamCodec<ByteBuf, LeaveTribePacket> STREAM_CODEC =
            StreamCodec.unit(new LeaveTribePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(LeaveTribePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp)) return;
            MinecraftServer server = sp.getServer();
            if (server == null) return;
            OWTribesSavedData data = OWTribesSavedData.get(server);

            OWTeam team = data.findTeamByMember(sp.getUUID());
            if (team == null) return;

            boolean isChief = sp.getUUID().equals(team.getTeamOwnerUUID());
            List<UUID> remainingMembers = new ArrayList<>(team.getPlayerUUIDs());

            if (isChief) {
                UUID successor = OWTribeManager.oldestMemberExcludingChief(team);
                if (successor == null) {
                    // Chef seul → dissolution complète de la tribu.
                    data.removeTribe(team.getTeamId());
                    OWTribeManager.refreshEntitiesOfPlayer(server, sp.getUUID());
                    OWTribeManager.syncPlayerTribe(server, sp);
                    OWTribeManager.broadcastTribeList(server);
                    return;
                }
                team.setTeamOwnerUUID(successor);
            }

            team.removePlayerMember(sp.getUUID());
            data.putTribe(team); // dirty

            // Les entités du partant redeviennent sans tribu ; il reçoit « aucune tribu ».
            OWTribeManager.refreshEntitiesOfPlayer(server, sp.getUUID());
            OWTribeManager.syncPlayerTribe(server, sp);
            // Les membres restants voient la liste des membres (et éventuellement le nouveau chef) changer.
            OWTribeManager.refreshEntitiesOfTribe(server, team);
            OWTribeManager.syncTribeToOnlineMembers(server, team);
            OWTribeManager.broadcastTribeList(server);
        });
    }
}
