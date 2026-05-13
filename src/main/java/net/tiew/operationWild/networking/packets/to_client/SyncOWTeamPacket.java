package net.tiew.operationWild.networking.packets.to_client;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTeamMosaicPattern;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record SyncOWTeamPacket(
        int entityId, int teamId, String teamName, String ownerUUID,
        int teamColor, int teamSecondaryColor, int mosaicPatternId,
        String creationDate,
        List<String> playerNames, List<String> entityNames
) implements CustomPacketPayload {

    public static final Type<SyncOWTeamPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "sync_ow_team"));

    public static final StreamCodec<ByteBuf, SyncOWTeamPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                ByteBufCodecs.INT.encode(buf, p.entityId());
                ByteBufCodecs.INT.encode(buf, p.teamId());
                ByteBufCodecs.STRING_UTF8.encode(buf, p.teamName());
                ByteBufCodecs.STRING_UTF8.encode(buf, p.ownerUUID());
                ByteBufCodecs.INT.encode(buf, p.teamColor());
                ByteBufCodecs.INT.encode(buf, p.teamSecondaryColor());   // ← nouveau
                ByteBufCodecs.INT.encode(buf, p.mosaicPatternId());      // ← nouveau
                ByteBufCodecs.STRING_UTF8.encode(buf, p.creationDate());
                ByteBufCodecs.INT.encode(buf, p.playerNames().size());
                for (String s : p.playerNames()) ByteBufCodecs.STRING_UTF8.encode(buf, s);
                ByteBufCodecs.INT.encode(buf, p.entityNames().size());
                for (String s : p.entityNames()) ByteBufCodecs.STRING_UTF8.encode(buf, s);
            },
            buf -> {
                int    entityId        = ByteBufCodecs.INT.decode(buf);
                int    teamId          = ByteBufCodecs.INT.decode(buf);
                String name            = ByteBufCodecs.STRING_UTF8.decode(buf);
                String owner           = ByteBufCodecs.STRING_UTF8.decode(buf);
                int    color           = ByteBufCodecs.INT.decode(buf);
                int    secondaryColor  = ByteBufCodecs.INT.decode(buf);   // ← nouveau
                int    patternId       = ByteBufCodecs.INT.decode(buf);   // ← nouveau
                String date            = ByteBufCodecs.STRING_UTF8.decode(buf);
                int    pc = ByteBufCodecs.INT.decode(buf);
                List<String> playerNames = new ArrayList<>(pc);
                for (int i = 0; i < pc; i++) playerNames.add(ByteBufCodecs.STRING_UTF8.decode(buf));
                int    ec = ByteBufCodecs.INT.decode(buf);
                List<String> entityNames = new ArrayList<>(ec);
                for (int i = 0; i < ec; i++) entityNames.add(ByteBufCodecs.STRING_UTF8.decode(buf));
                return new SyncOWTeamPacket(entityId, teamId, name, owner,
                        color, secondaryColor, patternId, date, playerNames, entityNames);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SyncOWTeamPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;
            Entity entity = level.getEntity(packet.entityId());
            if (entity instanceof OWEntity owEntity) {
                owEntity.currentTeam = new OWTeam(
                        packet.teamId(), packet.teamName(),
                        UUID.fromString(packet.ownerUUID()),
                        packet.teamColor(),
                        packet.teamSecondaryColor(),
                        OWTeamMosaicPattern.byId(packet.mosaicPatternId()),
                        new UUID[]{}, new OWEntity[]{},
                        packet.creationDate(),
                        packet.playerNames(), packet.entityNames()
                );
            }
        });
    }
}