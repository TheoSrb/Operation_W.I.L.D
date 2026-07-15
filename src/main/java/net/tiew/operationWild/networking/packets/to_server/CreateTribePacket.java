package net.tiew.operationWild.networking.packets.to_server;

// !! À enregistrer dans OWNetworkHandler (côté to_server) !!

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
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

/** Le joueur crée sa tribu (chef = lui). Refusé s'il est déjà dans une tribu ou si le nom est pris. */
public record CreateTribePacket(
        String teamName, int primaryColor, int secondaryColor,
        int mosaicPatternId, int bannerShapeId, byte[] paintPixels
) implements CustomPacketPayload {

    public static final Type<CreateTribePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "create_tribe"));

    public static final StreamCodec<ByteBuf, CreateTribePacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, p.teamName());
                ByteBufCodecs.INT.encode(buf, p.primaryColor());
                ByteBufCodecs.INT.encode(buf, p.secondaryColor());
                ByteBufCodecs.INT.encode(buf, p.mosaicPatternId());
                ByteBufCodecs.INT.encode(buf, p.bannerShapeId());
                byte[] px = p.paintPixels() != null ? p.paintPixels() : new byte[0];
                ByteBufCodecs.INT.encode(buf, px.length);
                for (byte b : px) buf.writeByte(b);
            },
            buf -> {
                String name = ByteBufCodecs.STRING_UTF8.decode(buf);
                int primary = ByteBufCodecs.INT.decode(buf);
                int secondary = ByteBufCodecs.INT.decode(buf);
                int patternId = ByteBufCodecs.INT.decode(buf);
                int bannerShapeId = ByteBufCodecs.INT.decode(buf);
                int len = ByteBufCodecs.INT.decode(buf);
                byte[] px = new byte[len];
                for (int i = 0; i < len; i++) px[i] = buf.readByte();
                return new CreateTribePacket(name, primary, secondary, patternId, bannerShapeId, px);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(CreateTribePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp)) return;
            MinecraftServer server = sp.getServer();
            if (server == null) return;
            OWTribesSavedData data = OWTribesSavedData.get(server);

            if (data.findTeamByMember(sp.getUUID()) != null) {
                sp.sendSystemMessage(Component.translatable("owteams.error.already_in_tribe"));
                return;
            }

            String name = (packet.teamName() == null || packet.teamName().trim().isEmpty())
                    ? Component.translatable("owteams.creation.default_name", sp.getName().getString()).getString()
                    : packet.teamName().trim();
            if (name.length() > 15) name = name.substring(0, 15);

            if (data.isNameTaken(name)) {
                sp.sendSystemMessage(Component.translatable("owteams.error.name_taken", name));
                return;
            }

            OWTeamMosaicPattern pattern = OWTeamMosaicPattern.byId(packet.mosaicPatternId());
            boolean[] pixels = pattern == OWTeamMosaicPattern.CUSTOM_PAINT
                    ? OWTeamMosaicPattern.unpackPixels(packet.paintPixels(), OWTeamMosaicPattern.CUSTOM_PAINT_PIXEL_COUNT)
                    : null;

            OWTeam team = data.createTribe(sp.getUUID(), sp.getName().getString(), name,
                    packet.primaryColor(), packet.secondaryColor(), pattern,
                    OWTeamBannerShape.byId(packet.bannerShapeId()), pixels);

            OWTribeManager.refreshEntitiesOfPlayer(server, sp.getUUID());
            OWTribeManager.syncPlayerTribe(server, sp);
            OWTribeManager.broadcastTribeList(server);
        });
    }
}
