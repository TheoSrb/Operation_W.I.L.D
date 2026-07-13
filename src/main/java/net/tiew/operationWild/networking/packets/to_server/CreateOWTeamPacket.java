package net.tiew.operationWild.networking.packets.to_server;

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

public record CreateOWTeamPacket(int entityId) implements CustomPacketPayload {

    public static final Type<CreateOWTeamPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "create_ow_team"));

    public static final StreamCodec<ByteBuf, CreateOWTeamPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.INT, CreateOWTeamPacket::entityId, CreateOWTeamPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(CreateOWTeamPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().level() instanceof ServerLevel serverLevel)) return;
            Entity entity = serverLevel.getEntity(packet.entityId());
            if (!(entity instanceof OWEntity owEntity)) return;
            if (owEntity.currentTeam != null) return;

            String ownerName = context.player().getName().getString();
            String entityDisplayName = owEntity.getNickname().isEmpty()
                    ? owEntity.getType().getDescription().getString()
                    : owEntity.getNickname();

            // Dans handle(), remplacer la création de OWTeam :
            OWTeam team = new OWTeam(
                    1, "Nouvelle Tribu",
                    context.player().getUUID(),
                    0x230b11,
                    0xd12c5c,
                    OWTeamMosaicPattern.GRADIENT_DOWN,
                    new UUID[]{ context.player().getUUID() },
                    new OWEntity[]{ owEntity },
                    LocalDate.now().toString(),
                    new ArrayList<>(List.of(ownerName)),
                    new ArrayList<>(List.of(entityDisplayName))
            );
            team.setEntityUUIDs(new ArrayList<>(List.of(owEntity.getUUID())));
            team.setPlayerUUIDs(new ArrayList<>(List.of(context.player().getUUID())));
            owEntity.currentTeam = team;

            for (ServerPlayer player : serverLevel.players()) {
                OWNetworkHandler.sendToClient(SyncOWTeamPacket.of(owEntity.getId(), team), player);
            }

        });
    }
}