package net.tiew.operationWild.networking.packets.to_server;

// !! À enregistrer dans OWNetworkHandler (côté to_server) !!

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.core.OWArena;
import net.tiew.operationWild.team.OWArenaManager;

/**
 * Le chef défie une autre tribu en arène, sur le terrain qu'il impose. Toutes les vérifications
 * sont faites côté serveur — l'ordinal de terrain reçu est ramené dans les bornes de l'énumération.
 */
public record ChallengeTribePacket(int targetTeamId, int terrainOrdinal) implements CustomPacketPayload {

    public static final Type<ChallengeTribePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "challenge_tribe"));

    public static final StreamCodec<ByteBuf, ChallengeTribePacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                ByteBufCodecs.INT.encode(buf, p.targetTeamId());
                ByteBufCodecs.INT.encode(buf, p.terrainOrdinal());
            },
            buf -> new ChallengeTribePacket(
                    ByteBufCodecs.INT.decode(buf), ByteBufCodecs.INT.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(ChallengeTribePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp) || sp.getServer() == null) return;
            OWArenaManager.challenge(sp.getServer(), sp,
                    packet.targetTeamId(), OWArena.Terrain.byOrdinal(packet.terrainOrdinal()));
        });
    }
}
