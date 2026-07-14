package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
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
import net.tiew.operationWild.networking.packets.to_client.SyncOWTeamPacket;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTeamMosaicPattern;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record OWNameEntityPacket(int entityId, String nickname) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OWNameEntityPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ow_name_entity"));

    public static final StreamCodec<FriendlyByteBuf, OWNameEntityPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            OWNameEntityPacket::entityId,
            ByteBufCodecs.STRING_UTF8,
            OWNameEntityPacket::nickname,
            OWNameEntityPacket::new
    );

    @Override
    public CustomPacketPayload.Type<OWNameEntityPacket> type() {
        return TYPE;
    }

    public static void handle(OWNameEntityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().level() instanceof ServerLevel serverLevel)) return;
            Entity entity = serverLevel.getEntity(packet.entityId());
            if (!(entity instanceof OWEntity owEntity)) return;
            // Seul le propriétaire / un membre de la tribu peut renommer ce pet.
            if (!(context.player() instanceof ServerPlayer sp) || !owEntity.canBeControlledBy(sp)) return;

            owEntity.setNickname(packet.nickname());

            // Si l'entité est dans une team, mettre à jour son nom dans entityNames
            // pour que les lookups par nickname restent valides après renommage.
            if (owEntity.currentTeam == null) return;

            OWTeam team = owEntity.currentTeam;
            int idx = team.getEntityUUIDs().indexOf(owEntity.getUUID());
            if (idx >= 0 && idx < team.getEntityNames().size()) {
                team.getEntityNames().set(idx, packet.nickname());
            }

            // Resync chaque membre de la team vers tous les clients, dans TOUTES les dimensions :
            // membres et joueurs peuvent être répartis sur plusieurs mondes (l'ancienne version ne
            // couvrait que la dimension du renommeur).
            for (ServerLevel level : serverLevel.getServer().getAllLevels()) {
                for (Entity e : level.getAllEntities()) {
                    if (!(e instanceof OWEntity m)) continue;
                    if (m.currentTeam == null || m.currentTeam.getTeamId() != team.getTeamId()) continue;

                    // S'assurer que ce membre aussi a le nom à jour (cas objets partagés ou non)
                    int mIdx = m.currentTeam.getEntityUUIDs().indexOf(owEntity.getUUID());
                    if (mIdx >= 0 && mIdx < m.currentTeam.getEntityNames().size()) {
                        m.currentTeam.getEntityNames().set(mIdx, packet.nickname());
                    }

                    SyncOWTeamPacket syncPacket = SyncOWTeamPacket.of(m.getId(), m.currentTeam);
                    for (ServerPlayer player : level.players()) {
                        OWNetworkHandler.sendToClient(syncPacket, player);
                    }
                }
            }
        });
    }
}
