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

/**
 * Le chef confirme ({@code ready=true}) ou retire sa composition. Le combat ne démarre que lorsque
 * les <b>deux</b> chefs ont confirmé — c'est le serveur qui constate l'accord, pas le client.
 */
public record ConfirmArenaFightersPacket(boolean ready) implements CustomPacketPayload {

    public static final Type<ConfirmArenaFightersPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "confirm_arena_fighters"));

    public static final StreamCodec<ByteBuf, ConfirmArenaFightersPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> ByteBufCodecs.BOOL.encode(buf, p.ready()),
            buf -> new ConfirmArenaFightersPacket(ByteBufCodecs.BOOL.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(ConfirmArenaFightersPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp) || sp.getServer() == null) return;
            OWArenaManager.confirm(sp.getServer(), sp, packet.ready());
        });
    }
}
