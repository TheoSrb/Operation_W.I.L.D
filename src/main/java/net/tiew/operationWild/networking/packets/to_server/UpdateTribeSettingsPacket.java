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

/** Le chef modifie la confidentialité (public/privé) et la condition d'entrée (pièces sauvages min). */
public record UpdateTribeSettingsPacket(boolean isPublic, int minWildCoins) implements CustomPacketPayload {

    public static final Type<UpdateTribeSettingsPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "update_tribe_settings"));

    public static final StreamCodec<ByteBuf, UpdateTribeSettingsPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                ByteBufCodecs.BOOL.encode(buf, p.isPublic());
                ByteBufCodecs.INT.encode(buf, p.minWildCoins());
            },
            buf -> new UpdateTribeSettingsPacket(ByteBufCodecs.BOOL.decode(buf), ByteBufCodecs.INT.decode(buf)));

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
            team.setMinWildCoins(packet.minWildCoins());
            data.putTribe(team); // dirty

            OWTribeManager.refreshEntitiesOfTribe(server, team);
            OWTribeManager.syncTribeToOnlineMembers(server, team);
            OWTribeManager.broadcastTribeList(server);
        });
    }
}
