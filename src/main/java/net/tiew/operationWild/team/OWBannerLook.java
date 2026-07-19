package net.tiew.operationWild.team;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Apparence complète d'une bannière de tribu, sous une forme transportable telle quelle sur le
 * réseau. Regroupe ce que {@code OWBannerRenderer} réclame pour dessiner un étendard, afin que les
 * paquets cinématiques (choc d'ouverture, victoire) n'aient pas à trimballer sept champs en vrac.
 */
public record OWBannerLook(
        int primary, int secondary, int tertiary, boolean useTertiary,
        int patternId, int shapeId, byte[] paint
) {

    public static final StreamCodec<ByteBuf, OWBannerLook> STREAM_CODEC = StreamCodec.of(
            (buf, look) -> {
                ByteBufCodecs.INT.encode(buf, look.primary());
                ByteBufCodecs.INT.encode(buf, look.secondary());
                ByteBufCodecs.INT.encode(buf, look.tertiary());
                ByteBufCodecs.BOOL.encode(buf, look.useTertiary());
                ByteBufCodecs.INT.encode(buf, look.patternId());
                ByteBufCodecs.INT.encode(buf, look.shapeId());
                byte[] px = look.paint() != null ? look.paint() : new byte[0];
                ByteBufCodecs.INT.encode(buf, px.length);
                for (byte b : px) buf.writeByte(b);
            },
            buf -> {
                int primary = ByteBufCodecs.INT.decode(buf);
                int secondary = ByteBufCodecs.INT.decode(buf);
                int tertiary = ByteBufCodecs.INT.decode(buf);
                boolean useTertiary = ByteBufCodecs.BOOL.decode(buf);
                int patternId = ByteBufCodecs.INT.decode(buf);
                int shapeId = ByteBufCodecs.INT.decode(buf);
                int len = ByteBufCodecs.INT.decode(buf);
                byte[] paint = new byte[len];
                for (int i = 0; i < len; i++) paint[i] = buf.readByte();
                return new OWBannerLook(primary, secondary, tertiary, useTertiary, patternId, shapeId, paint);
            });

    /** Apparence d'une tribu. Valeurs neutres si {@code team} est {@code null}. */
    public static OWBannerLook of(OWTeam team) {
        if (team == null) {
            return new OWBannerLook(0xFFFFFF, 0xFFFFFF, 0xFFFFFF, false, 0, 0, new byte[0]);
        }
        byte[] px = team.getPaintPixels();
        return new OWBannerLook(
                team.getTeamColor(), team.getTeamSecondaryColor(), team.getTertiaryColor(),
                team.isUseTertiary(), team.getTeamMosaicPattern().getId(), team.getBannerShape().getId(),
                px != null ? px : new byte[0]);
    }

    /** Peinture custom, ou {@code null} si absente (ce que le renderer attend dans ce cas). */
    public byte[] paintOrNull() {
        return paint != null && paint.length > 0 ? paint : null;
    }
}
