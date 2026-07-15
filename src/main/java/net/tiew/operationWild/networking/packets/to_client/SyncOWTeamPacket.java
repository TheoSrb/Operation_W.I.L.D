package net.tiew.operationWild.networking.packets.to_client;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
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
        List<String> playerNames, List<String> entityNames,
        List<String> entityUUIDs, // UUID strings — canonical entity membership, rename-safe
        List<String> playerUUIDs, // UUID strings — canonical player membership, pseudo-safe
        byte[] paintPixels,
        int bannerShapeId, boolean isPublic, int minWildCoins
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
                ByteBufCodecs.INT.encode(buf, p.teamSecondaryColor());
                ByteBufCodecs.INT.encode(buf, p.mosaicPatternId());
                ByteBufCodecs.STRING_UTF8.encode(buf, p.creationDate());
                ByteBufCodecs.INT.encode(buf, p.playerNames().size());
                for (String s : p.playerNames()) ByteBufCodecs.STRING_UTF8.encode(buf, s);
                ByteBufCodecs.INT.encode(buf, p.entityNames().size());
                for (String s : p.entityNames()) ByteBufCodecs.STRING_UTF8.encode(buf, s);
                List<String> uuids = p.entityUUIDs() != null ? p.entityUUIDs() : List.of();
                ByteBufCodecs.INT.encode(buf, uuids.size());
                for (String s : uuids) ByteBufCodecs.STRING_UTF8.encode(buf, s);
                List<String> pUuids = p.playerUUIDs() != null ? p.playerUUIDs() : List.of();
                ByteBufCodecs.INT.encode(buf, pUuids.size());
                for (String s : pUuids) ByteBufCodecs.STRING_UTF8.encode(buf, s);
                // Pixel data (compact : ~651 octets max pour 56×93 pixels)
                byte[] px = p.paintPixels() != null ? p.paintPixels() : new byte[0];
                ByteBufCodecs.INT.encode(buf, px.length);
                for (byte b : px) buf.writeByte(b);
                ByteBufCodecs.INT.encode(buf, p.bannerShapeId());
                ByteBufCodecs.BOOL.encode(buf, p.isPublic());
                ByteBufCodecs.INT.encode(buf, p.minWildCoins());
            },
            buf -> {
                int entityId = ByteBufCodecs.INT.decode(buf);
                int teamId = ByteBufCodecs.INT.decode(buf);
                String name = ByteBufCodecs.STRING_UTF8.decode(buf);
                String owner = ByteBufCodecs.STRING_UTF8.decode(buf);
                int color = ByteBufCodecs.INT.decode(buf);
                int secondaryColor = ByteBufCodecs.INT.decode(buf);
                int patternId = ByteBufCodecs.INT.decode(buf);
                String date = ByteBufCodecs.STRING_UTF8.decode(buf);
                int pc = ByteBufCodecs.INT.decode(buf);
                List<String> playerNames = new ArrayList<>(pc);
                for (int i = 0; i < pc; i++) playerNames.add(ByteBufCodecs.STRING_UTF8.decode(buf));
                int ec = ByteBufCodecs.INT.decode(buf);
                List<String> entityNames = new ArrayList<>(ec);
                for (int i = 0; i < ec; i++) entityNames.add(ByteBufCodecs.STRING_UTF8.decode(buf));
                int uc = ByteBufCodecs.INT.decode(buf);
                List<String> entityUUIDs = new ArrayList<>(uc);
                for (int i = 0; i < uc; i++) entityUUIDs.add(ByteBufCodecs.STRING_UTF8.decode(buf));
                int puc = ByteBufCodecs.INT.decode(buf);
                List<String> playerUUIDs = new ArrayList<>(puc);
                for (int i = 0; i < puc; i++) playerUUIDs.add(ByteBufCodecs.STRING_UTF8.decode(buf));
                // Pixel data
                int pxLen = ByteBufCodecs.INT.decode(buf);
                byte[] paintPixels = new byte[pxLen];
                for (int i = 0; i < pxLen; i++) paintPixels[i] = buf.readByte();
                int bannerShapeId = ByteBufCodecs.INT.decode(buf);
                boolean isPublic = ByteBufCodecs.BOOL.decode(buf);
                int minWildCoins = ByteBufCodecs.INT.decode(buf);
                return new SyncOWTeamPacket(entityId, teamId, name, owner,
                        color, secondaryColor, patternId, date,
                        playerNames, entityNames, entityUUIDs, playerUUIDs, paintPixels,
                        bannerShapeId, isPublic, minWildCoins);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Resynchronise une tribu vers tous les clients : parcourt toutes les entités chargées de tous
     * les niveaux, et pour chaque membre de la tribu (même {@code teamId}) réaligne sa référence
     * partagée et pousse un {@link SyncOWTeamPacket} aux joueurs de son niveau.
     */
    public static void resyncTeam(net.minecraft.server.MinecraftServer server, OWTeam team) {
        if (server == null || team == null) return;
        for (net.minecraft.server.level.ServerLevel level : server.getAllLevels()) {
            for (net.minecraft.world.entity.Entity e : level.getAllEntities()) {
                if (!(e instanceof OWEntity m)) continue;
                if (m.currentTeam == null || m.currentTeam.getTeamId() != team.getTeamId()) continue;
                m.currentTeam = team; // garantit une référence partagée unique
                SyncOWTeamPacket pkt = of(m.getId(), team);
                for (net.minecraft.server.level.ServerPlayer p : level.players()) {
                    net.tiew.operationWild.networking.OWNetworkHandler.sendToClient(pkt, p);
                }
            }
        }
    }

    /** Fabrique un packet de synchronisation pour l'entité {@code entityId} à partir d'une tribu. */
    public static SyncOWTeamPacket of(int entityId, OWTeam team) {
        List<String> eUuids = new ArrayList<>();
        for (UUID u : team.getEntityUUIDs()) eUuids.add(u.toString());
        List<String> pUuids = new ArrayList<>();
        for (UUID u : team.getPlayerUUIDs()) pUuids.add(u.toString());
        return new SyncOWTeamPacket(
                entityId,
                team.getTeamId(),
                team.getTeamName(),
                team.getTeamOwnerUUID().toString(),
                team.getTeamColor(),
                team.getTeamSecondaryColor(),
                team.getTeamMosaicPattern().getId(),
                team.getTeamCreationDate(),
                new ArrayList<>(team.getPlayerNames()),
                new ArrayList<>(team.getEntityNames()),
                eUuids,
                pUuids,
                OWTeamMosaicPattern.packPixels(
                        team.getPaintPixels() != null ? team.getPaintPixels() : new boolean[0]),
                team.getBannerShape().getId(),
                team.isPublic(),
                team.getMinWildCoins()
        );
    }

    public static void handle(SyncOWTeamPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            OWEntity owEntity = net.tiew.operationWild.client.OWClientHooks.clientOWEntity(packet.entityId());
            if (owEntity != null) {
                OWTeamMosaicPattern pattern = OWTeamMosaicPattern.byId(packet.mosaicPatternId());

                // Décompacter les pixels si le pattern est CUSTOM_PAINT
                boolean[] pixels = null;
                if (pattern == OWTeamMosaicPattern.CUSTOM_PAINT
                        && packet.paintPixels() != null
                        && packet.paintPixels().length > 0) {
                    pixels = OWTeamMosaicPattern.unpackPixels(
                            packet.paintPixels(), 56 * 93);
                }

                OWTeam newTeam = new OWTeam(
                        packet.teamId(), packet.teamName(),
                        UUID.fromString(packet.ownerUUID()),
                        packet.teamColor(),
                        packet.teamSecondaryColor(),
                        pattern,
                        new UUID[]{}, new OWEntity[]{},
                        packet.creationDate(),
                        packet.playerNames(), packet.entityNames(),
                        pixels
                );
                if (packet.entityUUIDs() != null) {
                    List<UUID> uuids = new ArrayList<>(packet.entityUUIDs().size());
                    for (String s : packet.entityUUIDs()) {
                        try { uuids.add(UUID.fromString(s)); } catch (IllegalArgumentException ignored) {}
                    }
                    newTeam.setEntityUUIDs(uuids);
                }
                if (packet.playerUUIDs() != null) {
                    List<UUID> pUuids = new ArrayList<>(packet.playerUUIDs().size());
                    for (String s : packet.playerUUIDs()) {
                        try { pUuids.add(UUID.fromString(s)); } catch (IllegalArgumentException ignored) {}
                    }
                    newTeam.setPlayerUUIDs(pUuids);
                }
                newTeam.setBannerShape(net.tiew.operationWild.team.OWTeamBannerShape.byId(packet.bannerShapeId()));
                newTeam.setPublic(packet.isPublic());
                newTeam.setMinWildCoins(packet.minWildCoins());
                owEntity.currentTeam = newTeam;
            }
        });
    }
}