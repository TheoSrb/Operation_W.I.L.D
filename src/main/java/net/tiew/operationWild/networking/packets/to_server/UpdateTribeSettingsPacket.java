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
import net.tiew.operationWild.team.OWTribeJoinRequirement;
import net.tiew.operationWild.team.OWTribeManager;
import net.tiew.operationWild.team.OWTribesSavedData;

import java.util.List;

/** Le chef modifie la confidentialité (public/privé) et les conditions d'entrée (0 à 3, cumulables). */
public record UpdateTribeSettingsPacket(boolean isPublic, List<OWTribeJoinRequirement> joinRequirements,
                                        boolean directJoin)
        implements CustomPacketPayload {

    public static final Type<UpdateTribeSettingsPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "update_tribe_settings"));

    public static final StreamCodec<ByteBuf, UpdateTribeSettingsPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                ByteBufCodecs.BOOL.encode(buf, p.isPublic());
                OWTribeJoinRequirement.LIST_STREAM_CODEC.encode(buf, p.joinRequirements());
                ByteBufCodecs.BOOL.encode(buf, p.directJoin());
            },
            buf -> new UpdateTribeSettingsPacket(ByteBufCodecs.BOOL.decode(buf),
                    OWTribeJoinRequirement.LIST_STREAM_CODEC.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(UpdateTribeSettingsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp)) return;
            MinecraftServer server = sp.getServer();
            if (server == null) return;
            OWTribesSavedData data = OWTribesSavedData.get(server);

            OWTeam team = data.findTeamByMember(sp.getUUID());
            if (team == null || !sp.getUUID().equals(team.getTeamOwnerUUID())) return;

            team.setPublic(packet.isPublic());
            // setJoinRequirements nettoie la liste (doublons, seuils, nombre max) : un client trafiqué
            // ne peut ni dépasser MAX_JOIN_REQUIREMENTS ni imposer un seuil hors bornes.
            team.setJoinRequirements(packet.joinRequirements());
            team.setDirectJoin(packet.directJoin());
            data.putTribe(team); // dirty

            OWTribeManager.syncTribeToOnlineMembers(server, team);
            OWTribeManager.broadcastTribeList(server);
        });
    }
}
