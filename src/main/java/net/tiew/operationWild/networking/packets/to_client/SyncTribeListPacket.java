package net.tiew.operationWild.networking.packets.to_client;

// !! À enregistrer dans OWNetworkHandler (côté to_client) !!

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.client.OWClientTribeList;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTeamBannerShape;
import net.tiew.operationWild.team.OWTeamMosaicPattern;
import net.tiew.operationWild.team.OWTribesSavedData;

import java.util.ArrayList;
import java.util.List;

/** Liste de toutes les tribus du serveur, pour l'écran de découverte / adhésion. */
public record SyncTribeListPacket(List<Entry> entries) implements CustomPacketPayload {

    public record Entry(int teamId, String name, String chiefName, int memberCount,
                        int primaryColor, int secondaryColor, int mosaicPatternId,
                        int bannerShapeId, boolean isPublic, int minWildCoins, byte[] paintPixels,
                        int tertiaryColor, boolean useTertiary, int reputation) {}

    public static final Type<SyncTribeListPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "sync_tribe_list"));

    public static final StreamCodec<ByteBuf, SyncTribeListPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                ByteBufCodecs.INT.encode(buf, p.entries().size());
                for (Entry e : p.entries()) {
                    ByteBufCodecs.INT.encode(buf, e.teamId());
                    ByteBufCodecs.STRING_UTF8.encode(buf, e.name());
                    ByteBufCodecs.STRING_UTF8.encode(buf, e.chiefName());
                    ByteBufCodecs.INT.encode(buf, e.memberCount());
                    ByteBufCodecs.INT.encode(buf, e.primaryColor());
                    ByteBufCodecs.INT.encode(buf, e.secondaryColor());
                    ByteBufCodecs.INT.encode(buf, e.mosaicPatternId());
                    ByteBufCodecs.INT.encode(buf, e.bannerShapeId());
                    ByteBufCodecs.BOOL.encode(buf, e.isPublic());
                    ByteBufCodecs.INT.encode(buf, e.minWildCoins());
                    byte[] px = e.paintPixels() != null ? e.paintPixels() : new byte[0];
                    ByteBufCodecs.INT.encode(buf, px.length);
                    for (byte b : px) buf.writeByte(b);
                    ByteBufCodecs.INT.encode(buf, e.tertiaryColor());
                    ByteBufCodecs.BOOL.encode(buf, e.useTertiary());
                    ByteBufCodecs.INT.encode(buf, e.reputation());
                }
            },
            buf -> {
                int n = ByteBufCodecs.INT.decode(buf);
                List<Entry> entries = new ArrayList<>(n);
                for (int i = 0; i < n; i++) {
                    int teamId = ByteBufCodecs.INT.decode(buf);
                    String name = ByteBufCodecs.STRING_UTF8.decode(buf);
                    String chief = ByteBufCodecs.STRING_UTF8.decode(buf);
                    int members = ByteBufCodecs.INT.decode(buf);
                    int primary = ByteBufCodecs.INT.decode(buf);
                    int secondary = ByteBufCodecs.INT.decode(buf);
                    int patternId = ByteBufCodecs.INT.decode(buf);
                    int bannerShapeId = ByteBufCodecs.INT.decode(buf);
                    boolean isPublic = ByteBufCodecs.BOOL.decode(buf);
                    int minCoins = ByteBufCodecs.INT.decode(buf);
                    int pxLen = ByteBufCodecs.INT.decode(buf);
                    byte[] px = new byte[pxLen];
                    for (int j = 0; j < pxLen; j++) px[j] = buf.readByte();
                    int tertiary = ByteBufCodecs.INT.decode(buf);
                    boolean useTertiary = ByteBufCodecs.BOOL.decode(buf);
                    int reputation = ByteBufCodecs.INT.decode(buf);
                    entries.add(new Entry(teamId, name, chief, members, primary, secondary,
                            patternId, bannerShapeId, isPublic, minCoins, px, tertiary, useTertiary, reputation));
                }
                return new SyncTribeListPacket(entries);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static SyncTribeListPacket of(net.minecraft.server.MinecraftServer server) {
        OWTribesSavedData data = OWTribesSavedData.get(server);
        net.tiew.operationWild.team.OWReputationData repData = net.tiew.operationWild.team.OWReputationData.get(server);
        List<Entry> entries = new ArrayList<>();
        for (OWTeam t : data.getAllTribes()) {
            int reputation = net.tiew.operationWild.core.OWReputation.compute(repData, t);
            String chiefName = "?";
            int chiefIdx = t.getPlayerUUIDs().indexOf(t.getTeamOwnerUUID());
            if (chiefIdx >= 0 && chiefIdx < t.getPlayerNames().size()) {
                chiefName = t.getPlayerNames().get(chiefIdx);
            } else if (!t.getPlayerNames().isEmpty()) {
                chiefName = t.getPlayerNames().get(0);
            }
            byte[] px = t.getTeamMosaicPattern() == OWTeamMosaicPattern.CUSTOM_PAINT
                    ? OWTeamMosaicPattern.packPixels3(t.getPaintPixels() != null ? t.getPaintPixels() : new byte[0])
                    : new byte[0];
            entries.add(new Entry(t.getTeamId(), t.getTeamName(), chiefName,
                    t.getPlayerUUIDs().size(), t.getTeamColor(), t.getTeamSecondaryColor(),
                    t.getTeamMosaicPattern().getId(), t.getBannerShape().getId(),
                    t.isPublic(), t.getMinWildCoins(), px, t.getTertiaryColor(), t.isUseTertiary(), reputation));
        }
        return new SyncTribeListPacket(entries);
    }

    public static void handle(SyncTribeListPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            List<OWClientTribeList.Entry> out = new ArrayList<>();
            for (Entry e : packet.entries()) {
                OWTeamMosaicPattern pattern = OWTeamMosaicPattern.byId(e.mosaicPatternId());
                byte[] pixels = null;
                if (pattern == OWTeamMosaicPattern.CUSTOM_PAINT && e.paintPixels() != null && e.paintPixels().length > 0) {
                    pixels = OWTeamMosaicPattern.unpackPixels3(e.paintPixels(),
                            OWTeamMosaicPattern.CUSTOM_PAINT_PIXEL_COUNT);
                }
                out.add(new OWClientTribeList.Entry(e.teamId(), e.name(), e.chiefName(), e.memberCount(),
                        e.primaryColor(), e.secondaryColor(), pattern,
                        OWTeamBannerShape.byId(e.bannerShapeId()), e.isPublic(), e.minWildCoins(), pixels,
                        e.tertiaryColor(), e.useTertiary(), e.reputation()));
            }
            OWClientTribeList.set(out);
        });
    }
}
