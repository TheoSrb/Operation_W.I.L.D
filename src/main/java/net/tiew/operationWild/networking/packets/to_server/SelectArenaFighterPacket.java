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

import java.util.UUID;

/**
 * Le chef engage ({@code add=true}) ou retire une créature de sa composition. Le serveur revalide
 * l'appartenance, la limite de cinq et l'unicité d'archétype — l'UUID reçu n'est jamais cru sur parole.
 */
public record SelectArenaFighterPacket(String entityUuid, boolean add) implements CustomPacketPayload {

    public static final Type<SelectArenaFighterPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "select_arena_fighter"));

    public static final StreamCodec<ByteBuf, SelectArenaFighterPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, p.entityUuid());
                ByteBufCodecs.BOOL.encode(buf, p.add());
            },
            buf -> new SelectArenaFighterPacket(
                    ByteBufCodecs.STRING_UTF8.decode(buf), ByteBufCodecs.BOOL.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SelectArenaFighterPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp) || sp.getServer() == null) return;
            UUID uuid;
            try { uuid = UUID.fromString(packet.entityUuid()); }
            catch (IllegalArgumentException e) { return; }
            OWArenaManager.selectFighter(sp.getServer(), sp, uuid, packet.add());
        });
    }
}
