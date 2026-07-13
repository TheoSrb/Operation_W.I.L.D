package net.tiew.operationWild.networking.packets.to_server;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.networking.packets.to_client.SyncOWTeamPacket;
import net.tiew.operationWild.team.OWTeam;

import java.util.List;
import java.util.UUID;

/**
 * Retire un joueur de la tribu par son index dans la liste des membres. Autorisé au chef (exclure
 * un membre) ou au joueur lui-même (quitter la tribu). Le chef (index de l'owner) n'est jamais
 * retirable par ce chemin.
 */
public record RemovePlayerFromTeamPacket(int entityId, int playerIndex) implements CustomPacketPayload {

    public static final Type<RemovePlayerFromTeamPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "remove_player_from_team"));

    public static final StreamCodec<ByteBuf, RemovePlayerFromTeamPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, RemovePlayerFromTeamPacket::entityId,
                    ByteBufCodecs.INT, RemovePlayerFromTeamPacket::playerIndex,
                    RemovePlayerFromTeamPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(RemovePlayerFromTeamPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().level() instanceof ServerLevel serverLevel)) return;
            Entity entity = serverLevel.getEntity(packet.entityId());
            if (!(entity instanceof OWEntity owEntity) || owEntity.currentTeam == null) return;

            OWTeam team = owEntity.currentTeam;
            List<UUID> playerUUIDs = team.getPlayerUUIDs();
            List<String> playerNames = team.getPlayerNames();

            int idx = packet.playerIndex();
            if (idx < 0 || idx >= playerUUIDs.size()) return;

            UUID removedUUID = playerUUIDs.get(idx);
            // Le chef ne peut pas être retiré via ce chemin.
            if (removedUUID.equals(team.getTeamOwnerUUID())) return;

            // Autorisation : chef de la tribu OU le joueur qui se retire lui-même.
            boolean isOwner = context.player().getUUID().equals(team.getTeamOwnerUUID());
            boolean isSelf = context.player().getUUID().equals(removedUUID);
            if (!isOwner && !isSelf) return;

            playerUUIDs.remove(idx);
            if (idx < playerNames.size()) playerNames.remove(idx);

            SyncOWTeamPacket.resyncTeam(serverLevel.getServer(), team);
        });
    }
}
