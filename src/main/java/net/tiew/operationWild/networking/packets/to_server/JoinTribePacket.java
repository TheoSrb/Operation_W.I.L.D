package net.tiew.operationWild.networking.packets.to_server;

// !! À enregistrer dans OWNetworkHandler (côté to_server) !!

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTribeJoinCheck;
import net.tiew.operationWild.team.OWTribeJoinRequests;
import net.tiew.operationWild.team.OWTribeJoinRequirement;
import net.tiew.operationWild.team.OWTribeManager;
import net.tiew.operationWild.team.OWTribesSavedData;

import java.util.List;

/** Le joueur rejoint une tribu PUBLIQUE (par id). Refusé si privée, déjà en tribu, ou conditions non remplies. */
public record JoinTribePacket(int teamId) implements CustomPacketPayload {

    public static final Type<JoinTribePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "join_tribe"));

    public static final StreamCodec<ByteBuf, JoinTribePacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.INT, JoinTribePacket::teamId, JoinTribePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(JoinTribePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp)) return;
            MinecraftServer server = sp.getServer();
            if (server == null) return;
            OWTribesSavedData data = OWTribesSavedData.get(server);

            if (data.findTeamByMember(sp.getUUID()) != null) {
                sp.sendSystemMessage(Component.translatable("owteams.error.already_in_tribe"));
                return;
            }
            OWTeam team = data.findTeamById(packet.teamId());
            if (team == null) return;
            if (!team.isPublic()) {
                sp.sendSystemMessage(Component.translatable("owteams.error.tribe_private"));
                return;
            }
            // Revérification serveur : le verdict envoyé au client n'est qu'un affichage.
            OWTribeJoinCheck.Snapshot snapshot = OWTribeJoinCheck.snapshot(server, sp);
            List<OWTribeJoinRequirement> unmet = OWTribeJoinCheck.unmet(snapshot, team);
            if (!unmet.isEmpty()) {
                // Une ligne par condition manquante : le joueur voit tout ce qu'il lui reste à faire.
                for (OWTribeJoinRequirement r : unmet) {
                    sp.sendSystemMessage(Component.translatable("owteams.error.condition_not_met",
                            r.describe(), snapshot.valueOf(r.condition())));
                }
                return;
            }

            if (team.getPlayerUUIDs().size() >= team.getMaxPlayers()) {
                sp.sendSystemMessage(Component.translatable("owteams.invite.full")
                        .setStyle(Style.EMPTY.withColor(0xFF6666)));
                return;
            }

            // Conditions remplies : soit on entre, soit la demande part en validation.
            if (!team.isDirectJoin()) {
                OWTribeJoinRequests.sendRequest(server, sp, team);
                return;
            }

            team.addPlayerMember(sp.getUUID(), sp.getName().getString());
            team.setPermissions(sp.getUUID(), net.tiew.operationWild.team.OWTribePermission.MEMBER_DEFAULT);
            data.putTribe(team); // marque dirty

            OWTribeManager.refreshEntitiesOfPlayer(server, sp.getUUID());
            OWTribeManager.syncTribeToOnlineMembers(server, team);
            OWTribeManager.broadcastTribeList(server);
        });
    }
}
