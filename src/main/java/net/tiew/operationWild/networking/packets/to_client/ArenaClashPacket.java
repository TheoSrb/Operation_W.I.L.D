package net.tiew.operationWild.networking.packets.to_client;

// !! À enregistrer dans OWNetworkHandler (côté to_client) !!

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.gui.OWArenaClashOverlay;
import net.tiew.operationWild.team.OWBannerLook;
import net.tiew.operationWild.team.OWTeam;

/**
 * Déclenche l'animation d'ouverture d'un duel d'arène chez les deux chefs : les bannières des deux
 * tribus s'élancent l'une vers l'autre et s'entrechoquent.
 *
 * <p>La bannière du <b>joueur qui reçoit</b> est toujours celle de gauche : chacun se voit du même
 * côté, ce qui rend l'écran lisible sans avoir à chercher son camp.</p>
 */
public record ArenaClashPacket(
        String leftName, OWBannerLook leftLook,
        String rightName, OWBannerLook rightLook
) implements CustomPacketPayload {

    public static final Type<ArenaClashPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "arena_clash"));

    public static final StreamCodec<ByteBuf, ArenaClashPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, p.leftName());
                OWBannerLook.STREAM_CODEC.encode(buf, p.leftLook());
                ByteBufCodecs.STRING_UTF8.encode(buf, p.rightName());
                OWBannerLook.STREAM_CODEC.encode(buf, p.rightLook());
            },
            buf -> new ArenaClashPacket(
                    ByteBufCodecs.STRING_UTF8.decode(buf), OWBannerLook.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf), OWBannerLook.STREAM_CODEC.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    /** Construit le paquet du point de vue de {@code mine} (qui sera affichée à gauche). */
    public static ArenaClashPacket of(OWTeam mine, OWTeam other) {
        return new ArenaClashPacket(
                mine != null && mine.getTeamName() != null ? mine.getTeamName() : "", OWBannerLook.of(mine),
                other != null && other.getTeamName() != null ? other.getTeamName() : "", OWBannerLook.of(other));
    }

    public static void handle(ArenaClashPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> OWArenaClashOverlay.trigger(packet));
    }
}
