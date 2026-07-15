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

/** Le chef promeut / rétrograde un membre au rang de Chef Adjoint. */
public record SetDeputyPacket(String targetUuid, boolean deputy) implements CustomPacketPayload {

    public static final Type<SetDeputyPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "set_deputy"));

    public static final StreamCodec<ByteBuf, SetDeputyPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> { ByteBufCodecs.STRING_UTF8.encode(buf, p.targetUuid()); ByteBufCodecs.BOOL.encode(buf, p.deputy()); },
            buf -> new SetDeputyPacket(ByteBufCodecs.STRING_UTF8.decode(buf), ByteBufCodecs.BOOL.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SetDeputyPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp)) return;
            MinecraftServer server = sp.getServer();
            if (server == null) return;
            OWTribesSavedData data = OWTribesSavedData.get(server);

            OWTeam team = data.findTeamByMember(sp.getUUID());
            if (team == null || !team.isChief(sp.getUUID())) return; // chef uniquement

            UUID target;
            try { target = UUID.fromString(packet.targetUuid()); } catch (IllegalArgumentException e) { return; }
            if (target.equals(team.getTeamOwnerUUID()) || !team.isMember(target)) return;

            team.setDeputy(target, packet.deputy());
            // Réinitialise les permissions au défaut du nouveau rôle.
            team.setPermissions(target, packet.deputy()
                    ? net.tiew.operationWild.team.OWTribePermission.DEPUTY_DEFAULT
                    : net.tiew.operationWild.team.OWTribePermission.MEMBER_DEFAULT);
            data.putTribe(team);

            OWTribeManager.syncTribeToOnlineMembers(server, team);
        });
    }
}
