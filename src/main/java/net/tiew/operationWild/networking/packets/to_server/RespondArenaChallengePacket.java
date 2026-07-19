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
import net.tiew.operationWild.team.OWArenaManager;

/** Le chef accepte ({@code accept=true}) ou décline le défi d'arène qui vise sa tribu. */
public record RespondArenaChallengePacket(boolean accept) implements CustomPacketPayload {

    public static final Type<RespondArenaChallengePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "respond_arena_challenge"));

    public static final StreamCodec<ByteBuf, RespondArenaChallengePacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> ByteBufCodecs.BOOL.encode(buf, p.accept()),
            buf -> new RespondArenaChallengePacket(ByteBufCodecs.BOOL.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(RespondArenaChallengePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp) || sp.getServer() == null) return;
            OWArenaManager.respond(sp.getServer(), sp, packet.accept());
        });
    }
}
