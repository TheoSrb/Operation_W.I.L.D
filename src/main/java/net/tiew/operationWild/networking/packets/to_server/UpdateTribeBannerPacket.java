package net.tiew.operationWild.networking.packets.to_server;

// !! À enregistrer dans OWNetworkHandler (côté to_server) !!

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTeamBannerShape;
import net.tiew.operationWild.team.OWTeamMosaicPattern;
import net.tiew.operationWild.team.OWTribeManager;
import net.tiew.operationWild.team.OWTribesSavedData;

/** Le chef modifie l'apparence de la bannière (forme + couleurs + motif) depuis le dashboard. */
public record UpdateTribeBannerPacket(
        int primaryColor, int secondaryColor, int mosaicPatternId, int bannerShapeId, byte[] paintPixels,
        int tertiaryColor, boolean useTertiary
) implements CustomPacketPayload {

    public static final Type<UpdateTribeBannerPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "update_tribe_banner"));

    public static final StreamCodec<ByteBuf, UpdateTribeBannerPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                ByteBufCodecs.INT.encode(buf, p.primaryColor());
                ByteBufCodecs.INT.encode(buf, p.secondaryColor());
                ByteBufCodecs.INT.encode(buf, p.mosaicPatternId());
                ByteBufCodecs.INT.encode(buf, p.bannerShapeId());
                byte[] px = p.paintPixels() != null ? p.paintPixels() : new byte[0];
                ByteBufCodecs.INT.encode(buf, px.length);
                for (byte b : px) buf.writeByte(b);
                ByteBufCodecs.INT.encode(buf, p.tertiaryColor());
                ByteBufCodecs.BOOL.encode(buf, p.useTertiary());
            },
            buf -> {
                int primary = ByteBufCodecs.INT.decode(buf);
                int secondary = ByteBufCodecs.INT.decode(buf);
                int patternId = ByteBufCodecs.INT.decode(buf);
                int bannerShapeId = ByteBufCodecs.INT.decode(buf);
                int len = ByteBufCodecs.INT.decode(buf);
                byte[] px = new byte[len];
                for (int i = 0; i < len; i++) px[i] = buf.readByte();
                int tertiary = ByteBufCodecs.INT.decode(buf);
                boolean useTertiary = ByteBufCodecs.BOOL.decode(buf);
                return new UpdateTribeBannerPacket(primary, secondary, patternId, bannerShapeId, px, tertiary, useTertiary);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(UpdateTribeBannerPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp)) return;
            MinecraftServer server = sp.getServer();
            if (server == null) return;
            OWTribesSavedData data = OWTribesSavedData.get(server);

            OWTeam team = data.findTeamByMember(sp.getUUID());
            if (team == null || !sp.getUUID().equals(team.getTeamOwnerUUID())) return;

            OWTeamMosaicPattern pattern = OWTeamMosaicPattern.byId(packet.mosaicPatternId());
            team.setTeamColor(packet.primaryColor());
            team.setTeamSecondaryColor(packet.secondaryColor());
            team.setTeamMosaicPattern(pattern);
            team.setBannerShape(OWTeamBannerShape.byId(packet.bannerShapeId()));
            team.setTertiaryColor(packet.tertiaryColor());
            team.setUseTertiary(packet.useTertiary());
            team.setPaintPixels(pattern == OWTeamMosaicPattern.CUSTOM_PAINT
                    ? OWTeamMosaicPattern.unpackPixels3(packet.paintPixels(), OWTeamMosaicPattern.CUSTOM_PAINT_PIXEL_COUNT)
                    : null);
            data.putTribe(team); // dirty

            OWTribeManager.syncTribeToOnlineMembers(server, team);
            OWTribeManager.broadcastTribeList(server);
        });
    }
}
