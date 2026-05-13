package net.tiew.operationWild.networking.packets.to_server;

// !! À enregistrer dans OWNetworkHandler (côté to_server) !!

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.ClearOWTeamPacket;
import net.tiew.operationWild.networking.packets.to_client.SyncOWTeamPacket;
import net.tiew.operationWild.team.OWTeam;

import java.util.ArrayList;
import java.util.List;

public record RemoveEntityFromTeamPacket(int entityId, int entityIndex) implements CustomPacketPayload {

    public static final Type<RemoveEntityFromTeamPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "remove_entity_from_team"));

    public static final StreamCodec<ByteBuf, RemoveEntityFromTeamPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, RemoveEntityFromTeamPacket::entityId,
                    ByteBufCodecs.INT, RemoveEntityFromTeamPacket::entityIndex,
                    RemoveEntityFromTeamPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(RemoveEntityFromTeamPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().level() instanceof ServerLevel serverLevel)) return;

            Entity entity = serverLevel.getEntity(packet.entityId());
            if (!(entity instanceof OWEntity owEntity)) return;
            if (owEntity.currentTeam == null) return;
            if (!context.player().getUUID().equals(owEntity.currentTeam.getTeamOwnerUUID())) return;

            OWTeam team = owEntity.currentTeam;
            List<String> entityNames = team.getEntityNames();
            int idx = packet.entityIndex();
            if (idx < 0 || idx >= entityNames.size()) return;

            String removedName = entityNames.get(idx);
            entityNames.remove(idx);

            // ── Trouver l'entité retirée et effacer sa référence d'équipe ────────
            OWEntity removedEntity = null;
            for (Entity e : serverLevel.getAllEntities()) {
                if (e instanceof OWEntity owE && owE.getNickname().equals(removedName)) {
                    owE.currentTeam = null;
                    removedEntity = owE;
                    break;
                }
            }

            // ── Collecter les membres restants dans l'équipe ─────────────────────
            List<OWEntity> remainingMembers = new ArrayList<>();
            for (Entity e : serverLevel.getAllEntities()) {
                if (e instanceof OWEntity m
                        && m.currentTeam != null
                        && m.currentTeam.getTeamId() == team.getTeamId()) {
                    remainingMembers.add(m);
                }
            }

            final OWEntity finalRemovedEntity = removedEntity;

            for (ServerPlayer player : serverLevel.players()) {
                // ── Envoyer ClearOWTeamPacket à l'entité retirée ─────────────────
                if (finalRemovedEntity != null) {
                    OWNetworkHandler.sendToClient(
                            new ClearOWTeamPacket(finalRemovedEntity.getId()), player);
                }

                // ── Resynchroniser TOUS les membres restants ──────────────────────
                for (OWEntity member : remainingMembers) {
                    OWNetworkHandler.sendToClient(new SyncOWTeamPacket(
                            member.getId(),
                            team.getTeamId(),
                            team.getTeamName(),
                            team.getTeamOwnerUUID().toString(),
                            team.getTeamColor(),
                            team.getTeamSecondaryColor(),
                            team.getTeamMosaicPattern().getId(),
                            team.getTeamCreationDate(),
                            team.getPlayerNames(),
                            team.getEntityNames()
                    ), player);
                }
            }
        });
    }
}