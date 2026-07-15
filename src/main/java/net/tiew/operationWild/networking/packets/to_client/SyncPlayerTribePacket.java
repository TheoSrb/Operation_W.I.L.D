package net.tiew.operationWild.networking.packets.to_client;

// !! À enregistrer dans OWNetworkHandler (côté to_client) !!

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.client.OWClientTribeData;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTeamBannerShape;
import net.tiew.operationWild.team.OWTeamMosaicPattern;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Pousse au joueur SA tribu (chef ou membre) pour ses écrans de tribu. {@code hasTribe=false} = aucune. */
public record SyncPlayerTribePacket(
        boolean hasTribe,
        int teamId, String teamName, String ownerUUID,
        int primaryColor, int secondaryColor, int mosaicPatternId,
        int bannerShapeId, boolean isPublic, int minWildCoins,
        String creationDate,
        List<String> playerNames, List<String> playerUUIDs,
        byte[] paintPixels,
        int tertiaryColor, boolean useTertiary,
        List<String> deputyUUIDs, List<String> permUUIDs, List<Integer> permMasks
) implements CustomPacketPayload {

    public static final Type<SyncPlayerTribePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "sync_player_tribe"));

    public static final StreamCodec<ByteBuf, SyncPlayerTribePacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                ByteBufCodecs.BOOL.encode(buf, p.hasTribe());
                ByteBufCodecs.INT.encode(buf, p.teamId());
                ByteBufCodecs.STRING_UTF8.encode(buf, p.teamName());
                ByteBufCodecs.STRING_UTF8.encode(buf, p.ownerUUID());
                ByteBufCodecs.INT.encode(buf, p.primaryColor());
                ByteBufCodecs.INT.encode(buf, p.secondaryColor());
                ByteBufCodecs.INT.encode(buf, p.mosaicPatternId());
                ByteBufCodecs.INT.encode(buf, p.bannerShapeId());
                ByteBufCodecs.BOOL.encode(buf, p.isPublic());
                ByteBufCodecs.INT.encode(buf, p.minWildCoins());
                ByteBufCodecs.STRING_UTF8.encode(buf, p.creationDate());
                ByteBufCodecs.INT.encode(buf, p.playerNames().size());
                for (String s : p.playerNames()) ByteBufCodecs.STRING_UTF8.encode(buf, s);
                ByteBufCodecs.INT.encode(buf, p.playerUUIDs().size());
                for (String s : p.playerUUIDs()) ByteBufCodecs.STRING_UTF8.encode(buf, s);
                byte[] px = p.paintPixels() != null ? p.paintPixels() : new byte[0];
                ByteBufCodecs.INT.encode(buf, px.length);
                for (byte b : px) buf.writeByte(b);
                ByteBufCodecs.INT.encode(buf, p.tertiaryColor());
                ByteBufCodecs.BOOL.encode(buf, p.useTertiary());
                ByteBufCodecs.INT.encode(buf, p.deputyUUIDs().size());
                for (String s : p.deputyUUIDs()) ByteBufCodecs.STRING_UTF8.encode(buf, s);
                ByteBufCodecs.INT.encode(buf, p.permUUIDs().size());
                for (int i = 0; i < p.permUUIDs().size(); i++) {
                    ByteBufCodecs.STRING_UTF8.encode(buf, p.permUUIDs().get(i));
                    ByteBufCodecs.INT.encode(buf, p.permMasks().get(i));
                }
            },
            buf -> {
                boolean has = ByteBufCodecs.BOOL.decode(buf);
                int teamId = ByteBufCodecs.INT.decode(buf);
                String name = ByteBufCodecs.STRING_UTF8.decode(buf);
                String owner = ByteBufCodecs.STRING_UTF8.decode(buf);
                int primary = ByteBufCodecs.INT.decode(buf);
                int secondary = ByteBufCodecs.INT.decode(buf);
                int patternId = ByteBufCodecs.INT.decode(buf);
                int bannerShapeId = ByteBufCodecs.INT.decode(buf);
                boolean isPublic = ByteBufCodecs.BOOL.decode(buf);
                int minCoins = ByteBufCodecs.INT.decode(buf);
                String date = ByteBufCodecs.STRING_UTF8.decode(buf);
                int pc = ByteBufCodecs.INT.decode(buf);
                List<String> pNames = new ArrayList<>(pc);
                for (int i = 0; i < pc; i++) pNames.add(ByteBufCodecs.STRING_UTF8.decode(buf));
                int puc = ByteBufCodecs.INT.decode(buf);
                List<String> pUuids = new ArrayList<>(puc);
                for (int i = 0; i < puc; i++) pUuids.add(ByteBufCodecs.STRING_UTF8.decode(buf));
                int pxLen = ByteBufCodecs.INT.decode(buf);
                byte[] pixels = new byte[pxLen];
                for (int i = 0; i < pxLen; i++) pixels[i] = buf.readByte();
                int tertiary = ByteBufCodecs.INT.decode(buf);
                boolean useTertiary = ByteBufCodecs.BOOL.decode(buf);
                int dc = ByteBufCodecs.INT.decode(buf);
                List<String> deputies = new ArrayList<>(dc);
                for (int i = 0; i < dc; i++) deputies.add(ByteBufCodecs.STRING_UTF8.decode(buf));
                int prc = ByteBufCodecs.INT.decode(buf);
                List<String> permU = new ArrayList<>(prc);
                List<Integer> permM = new ArrayList<>(prc);
                for (int i = 0; i < prc; i++) { permU.add(ByteBufCodecs.STRING_UTF8.decode(buf)); permM.add(ByteBufCodecs.INT.decode(buf)); }
                return new SyncPlayerTribePacket(has, teamId, name, owner, primary, secondary,
                        patternId, bannerShapeId, isPublic, minCoins, date, pNames, pUuids, pixels,
                        tertiary, useTertiary, deputies, permU, permM);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static SyncPlayerTribePacket of(OWTeam team) {
        if (team == null) {
            return new SyncPlayerTribePacket(false, 0, "", "",
                    0xFFFFFF, 0xFFFFFF, 0, 0, false, 0, "",
                    new ArrayList<>(), new ArrayList<>(), new byte[0], 0xFFFFFF, false,
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
        List<String> pUuids = new ArrayList<>();
        for (UUID u : team.getPlayerUUIDs()) pUuids.add(u.toString());
        List<String> deputies = new ArrayList<>();
        for (UUID u : team.getDeputyUUIDs()) deputies.add(u.toString());
        List<String> permU = new ArrayList<>();
        List<Integer> permM = new ArrayList<>();
        for (var e : team.getMemberPermissions().entrySet()) { permU.add(e.getKey().toString()); permM.add(e.getValue()); }
        return new SyncPlayerTribePacket(
                true,
                team.getTeamId(),
                team.getTeamName(),
                team.getTeamOwnerUUID().toString(),
                team.getTeamColor(),
                team.getTeamSecondaryColor(),
                team.getTeamMosaicPattern().getId(),
                team.getBannerShape().getId(),
                team.isPublic(),
                team.getMinWildCoins(),
                team.getTeamCreationDate() != null ? team.getTeamCreationDate() : "",
                new ArrayList<>(team.getPlayerNames()),
                pUuids,
                OWTeamMosaicPattern.packPixels3(team.getPaintPixels() != null ? team.getPaintPixels() : new byte[0]),
                team.getTertiaryColor(),
                team.isUseTertiary(),
                deputies, permU, permM);
    }

    public static void handle(SyncPlayerTribePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!packet.hasTribe()) {
                OWClientTribeData.set(null);
                return;
            }
            OWTeamMosaicPattern pattern = OWTeamMosaicPattern.byId(packet.mosaicPatternId());
            byte[] pixels = null;
            if (pattern == OWTeamMosaicPattern.CUSTOM_PAINT
                    && packet.paintPixels() != null && packet.paintPixels().length > 0) {
                pixels = OWTeamMosaicPattern.unpackPixels3(packet.paintPixels(),
                        OWTeamMosaicPattern.CUSTOM_PAINT_PIXEL_COUNT);
            }
            OWTeam team = new OWTeam(
                    packet.teamId(), packet.teamName(),
                    UUID.fromString(packet.ownerUUID()),
                    packet.primaryColor(), packet.secondaryColor(), pattern,
                    new UUID[]{}, new OWEntity[]{},
                    packet.creationDate(),
                    packet.playerNames(), new ArrayList<>(), pixels);
            List<UUID> pUuids = new ArrayList<>();
            for (String s : packet.playerUUIDs()) {
                try { pUuids.add(UUID.fromString(s)); } catch (IllegalArgumentException ignored) {}
            }
            team.setPlayerUUIDs(pUuids);
            team.setBannerShape(OWTeamBannerShape.byId(packet.bannerShapeId()));
            team.setPublic(packet.isPublic());
            team.setMinWildCoins(packet.minWildCoins());
            team.setTertiaryColor(packet.tertiaryColor());
            team.setUseTertiary(packet.useTertiary());
            List<UUID> deps = new ArrayList<>();
            for (String s : packet.deputyUUIDs()) {
                try { deps.add(UUID.fromString(s)); } catch (IllegalArgumentException ignored) {}
            }
            team.setDeputyUUIDs(deps);
            for (int i = 0; i < packet.permUUIDs().size(); i++) {
                try { team.setPermissions(UUID.fromString(packet.permUUIDs().get(i)), packet.permMasks().get(i)); }
                catch (IllegalArgumentException ignored) {}
            }
            OWClientTribeData.set(team);
        });
    }
}
