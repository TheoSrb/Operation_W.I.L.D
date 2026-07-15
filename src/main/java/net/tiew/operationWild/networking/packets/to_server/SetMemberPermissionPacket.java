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

/** Le chef ou un chef adjoint définit le bitmask de permissions d'un membre simple. */
public record SetMemberPermissionPacket(String targetUuid, int mask) implements CustomPacketPayload {

    public static final Type<SetMemberPermissionPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "set_member_permission"));

    public static final StreamCodec<ByteBuf, SetMemberPermissionPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> { ByteBufCodecs.STRING_UTF8.encode(buf, p.targetUuid()); ByteBufCodecs.INT.encode(buf, p.mask()); },
            buf -> new SetMemberPermissionPacket(ByteBufCodecs.STRING_UTF8.decode(buf), ByteBufCodecs.INT.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SetMemberPermissionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp)) return;
            MinecraftServer server = sp.getServer();
            if (server == null) return;
            OWTribesSavedData data = OWTribesSavedData.get(server);

            OWTeam team = data.findTeamByMember(sp.getUUID());
            if (team == null) return;
            boolean isChief = team.isChief(sp.getUUID());
            boolean isDeputy = team.isDeputy(sp.getUUID());
            if (!isChief && !isDeputy) return; // seuls chef & adjoints gèrent les permissions

            UUID target;
            try { target = UUID.fromString(packet.targetUuid()); } catch (IllegalArgumentException e) { return; }
            // Jamais le chef (accès total). Le chef édite adjoints + membres ; un adjoint, les membres seuls.
            if (!team.isMember(target) || team.isChief(target)) return;
            if (!isChief && team.isDeputy(target)) return;

            team.setPermissions(target, packet.mask() & net.tiew.operationWild.team.OWTribePermission.ALL);
            data.putTribe(team);

            OWTribeManager.syncTribeToOnlineMembers(server, team);
        });
    }
}
