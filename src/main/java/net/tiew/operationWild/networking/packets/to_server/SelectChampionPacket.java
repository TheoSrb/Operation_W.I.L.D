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
import net.tiew.operationWild.team.OWChampionManager;

import java.util.UUID;

/**
 * Le <b>chef</b> nomme ou révoque une créature championne de sa tribu.
 *
 * <p>Le paquet ne dit que l'intention : le serveur revalide tout (rang du demandeur, appartenance
 * de la créature à la tribu, place disponible), car un client peut envoyer n'importe quel UUID.</p>
 */
public record SelectChampionPacket(UUID entityUuid, boolean add) implements CustomPacketPayload {

    public static final Type<SelectChampionPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "select_champion"));

    public static final StreamCodec<ByteBuf, SelectChampionPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, p.entityUuid().toString());
                ByteBufCodecs.BOOL.encode(buf, p.add());
            },
            buf -> {
                UUID uuid;
                try {
                    uuid = UUID.fromString(ByteBufCodecs.STRING_UTF8.decode(buf));
                } catch (IllegalArgumentException e) {
                    uuid = new UUID(0L, 0L); // rejeté par le gestionnaire : aucune créature ne le porte
                }
                return new SelectChampionPacket(uuid, ByteBufCodecs.BOOL.decode(buf));
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SelectChampionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp)) return;
            OWChampionManager.select(sp, packet.entityUuid(), packet.add());
        });
    }
}
