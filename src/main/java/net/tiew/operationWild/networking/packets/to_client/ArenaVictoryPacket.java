package net.tiew.operationWild.networking.packets.to_client;

// !! À enregistrer dans OWNetworkHandler (côté to_client) !!

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.gui.OWArenaVictoryOverlay;
import net.tiew.operationWild.team.OWBannerLook;

/**
 * Déclenche la cinématique de verdict : la bannière victorieuse fracasse celle du vaincu.
 *
 * <p>Le même paquet part aux deux camps — seul {@link #viewerWon()} change, ce qui bascule le
 * bandeau entre « Victoire » et « Défaite ». Les deux joueurs voient donc rigoureusement la même
 * scène, ce qui rend le verdict indiscutable.</p>
 */
public record ArenaVictoryPacket(
        String winnerName, OWBannerLook winnerLook,
        String loserName, OWBannerLook loserLook,
        boolean viewerWon
) implements CustomPacketPayload {

    public static final Type<ArenaVictoryPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "arena_victory"));

    public static final StreamCodec<ByteBuf, ArenaVictoryPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, p.winnerName());
                OWBannerLook.STREAM_CODEC.encode(buf, p.winnerLook());
                ByteBufCodecs.STRING_UTF8.encode(buf, p.loserName());
                OWBannerLook.STREAM_CODEC.encode(buf, p.loserLook());
                ByteBufCodecs.BOOL.encode(buf, p.viewerWon());
            },
            buf -> new ArenaVictoryPacket(
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    OWBannerLook.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    OWBannerLook.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(ArenaVictoryPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> OWArenaVictoryOverlay.trigger(packet));
    }
}
