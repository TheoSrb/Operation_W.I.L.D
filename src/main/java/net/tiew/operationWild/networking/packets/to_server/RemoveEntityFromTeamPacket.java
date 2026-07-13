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
import net.tiew.operationWild.team.OWTeamMosaicPattern;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

            OWTeam team = owEntity.currentTeam;
            boolean isTeamOwner = context.player().getUUID().equals(team.getTeamOwnerUUID());

            int idx = packet.entityIndex();
            List<UUID> entityUUIDs = team.getEntityUUIDs();
            List<String> entityNames = team.getEntityNames();

            // idx == -1 → « self-leave » : l'entité identifiée par entityId quitte elle-même la tribu.
            // On la retire par son identité propre, ce qui reste robuste même si son UUID n'est pas
            // (ou plus) présent dans la liste synchronisée — cas des entrées ajoutées avant le suivi
            // par UUID, ou d'une désynchronisation. Sinon (bouton « − » du chef) : retrait par index.
            final boolean selfLeave = idx < 0;
            UUID removedUUID;
            if (selfLeave) {
                removedUUID = owEntity.getUUID();
                idx = entityUUIDs.indexOf(removedUUID); // peut rester -1 (entrée héritée sans UUID)
            } else {
                if (idx >= entityUUIDs.size()) return;
                removedUUID = entityUUIDs.get(idx);
            }

            // ── Autorisation : chef OU propriétaire de l'entité retirée ──────────
            if (!isTeamOwner) {
                boolean playerOwnsTarget = false;
                for (Entity e : serverLevel.getAllEntities()) {
                    if (e instanceof OWEntity owE && owE.getUUID().equals(removedUUID)) {
                        playerOwnsTarget = context.player().getUUID().equals(owE.getOwnerUUID());
                        break;
                    }
                }
                if (!playerOwnsTarget) return;
            }

            if (idx >= 0) {
                entityUUIDs.remove(idx);
                if (idx < entityNames.size()) entityNames.remove(idx);
            } else {
                // Entrée héritée : UUID absent de la liste. On nettoie le nom d'affichage par pseudo
                // et on s'assure quand même de détacher l'entité de la tribu (nettoyage ci-dessous).
                entityNames.remove(owEntity.getNickname());
            }

            // ── Trouver l'entité retirée et effacer sa référence d'équipe ────────
            OWEntity removedEntity = null;
            for (Entity e : serverLevel.getAllEntities()) {
                if (e instanceof OWEntity owE && owE.getUUID().equals(removedUUID)) {
                    owE.currentTeam = null;
                    removedEntity = owE;
                    break;
                }
            }

            // ── Collecter les membres restants (par UUID) ─────────────────────
            List<OWEntity> remainingMembers = new ArrayList<>();
            for (UUID memberUUID : new ArrayList<>(entityUUIDs)) {
                for (Entity e : serverLevel.getAllEntities()) {
                    if (e instanceof OWEntity m && m.getUUID().equals(memberUUID)) {
                        remainingMembers.add(m);
                        break;
                    }
                }
            }

            final OWEntity finalRemovedEntity = removedEntity;

            for (ServerPlayer player : serverLevel.players()) {
                if (finalRemovedEntity != null) {
                    OWNetworkHandler.sendToClient(
                            new ClearOWTeamPacket(finalRemovedEntity.getId()), player);
                }
                for (OWEntity member : remainingMembers) {
                    OWNetworkHandler.sendToClient(SyncOWTeamPacket.of(member.getId(), team), player);
                }
            }
        });
    }
}