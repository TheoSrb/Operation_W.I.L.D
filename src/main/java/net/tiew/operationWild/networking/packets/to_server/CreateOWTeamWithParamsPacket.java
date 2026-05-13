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
import net.tiew.operationWild.networking.packets.to_client.SyncOWTeamPacket;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTeamMosaicPattern;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record CreateOWTeamWithParamsPacket(
        int entityId,
        String teamName,
        int primaryColor,
        int secondaryColor,
        int mosaicPatternId
) implements CustomPacketPayload {

    public static final Type<CreateOWTeamWithParamsPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "create_ow_team_with_params"));

    public static final StreamCodec<ByteBuf, CreateOWTeamWithParamsPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        ByteBufCodecs.INT.encode(buf, p.entityId());
                        ByteBufCodecs.STRING_UTF8.encode(buf, p.teamName());
                        ByteBufCodecs.INT.encode(buf, p.primaryColor());
                        ByteBufCodecs.INT.encode(buf, p.secondaryColor());
                        ByteBufCodecs.INT.encode(buf, p.mosaicPatternId());
                    },
                    buf -> new CreateOWTeamWithParamsPacket(
                            ByteBufCodecs.INT.decode(buf),
                            ByteBufCodecs.STRING_UTF8.decode(buf),
                            ByteBufCodecs.INT.decode(buf),
                            ByteBufCodecs.INT.decode(buf),
                            ByteBufCodecs.INT.decode(buf)
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(CreateOWTeamWithParamsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().level() instanceof ServerLevel serverLevel)) return;
            Entity entity = serverLevel.getEntity(packet.entityId());
            if (!(entity instanceof OWEntity owEntity)) return;
            if (owEntity.currentTeam != null) return; // tribu déjà existante

            String ownerName = context.player().getName().getString();
            String entityDisplayName = owEntity.getNickname() == null || owEntity.getNickname().isEmpty()
                    ? owEntity.getType().getDescription().getString()
                    : owEntity.getNickname();

            String name = (packet.teamName() == null || packet.teamName().trim().isEmpty())
                    ? "Nouvelle Tribu"
                    : packet.teamName().trim();

            OWTeam team = new OWTeam(
                    1,
                    name,
                    context.player().getUUID(),
                    packet.primaryColor(),
                    packet.secondaryColor(),
                    OWTeamMosaicPattern.byId(packet.mosaicPatternId()),
                    new UUID[]{ context.player().getUUID() },
                    new OWEntity[]{ owEntity },
                    LocalDate.now().toString(),
                    new ArrayList<>(List.of(ownerName)),
                    new ArrayList<>(List.of(entityDisplayName))
            );
            owEntity.currentTeam = team;

            for (ServerPlayer player : serverLevel.players()) {
                OWNetworkHandler.sendToClient(new SyncOWTeamPacket(
                        owEntity.getId(),
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
        });
    }
}