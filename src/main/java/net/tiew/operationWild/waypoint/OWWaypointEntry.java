package net.tiew.operationWild.waypoint;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Instantané d'un waypoint tel qu'il voyage du serveur vers le client.
 *
 * <p>Le nom n'est <b>pas</b> transporté résolu : seuls l'identifiant de type et l'éventuel nom
 * personnalisé le sont. Le serveur ne connaît pas la langue du joueur — un nom d'espèce traduit
 * côté serveur arriverait dans la mauvaise langue, ou pas traduit du tout. Le client reconstitue
 * donc l'affichage lui-même (cf. {@code hasCustomName}).</p>
 */
public record OWWaypointEntry(
        UUID entityUuid,
        String entityTypeId,
        boolean hasCustomName,
        String customName,
        String dimension,
        double x, double y, double z,
        int fillColor, int borderColor, int textColor, int entityColor,
        int iconSize, int maxDist,
        float minDist, float minOpacity, float fontScale,
        boolean enabled) {

    public static final StreamCodec<ByteBuf, OWWaypointEntry> STREAM_CODEC = StreamCodec.of(
            (buf, e) -> {
                UUIDUtil.STREAM_CODEC.encode(buf, e.entityUuid());
                ByteBufCodecs.STRING_UTF8.encode(buf, e.entityTypeId());
                ByteBufCodecs.BOOL.encode(buf, e.hasCustomName());
                ByteBufCodecs.STRING_UTF8.encode(buf, e.customName());
                ByteBufCodecs.STRING_UTF8.encode(buf, e.dimension());
                ByteBufCodecs.DOUBLE.encode(buf, e.x());
                ByteBufCodecs.DOUBLE.encode(buf, e.y());
                ByteBufCodecs.DOUBLE.encode(buf, e.z());
                ByteBufCodecs.INT.encode(buf, e.fillColor());
                ByteBufCodecs.INT.encode(buf, e.borderColor());
                ByteBufCodecs.INT.encode(buf, e.textColor());
                ByteBufCodecs.INT.encode(buf, e.entityColor());
                ByteBufCodecs.INT.encode(buf, e.iconSize());
                ByteBufCodecs.INT.encode(buf, e.maxDist());
                ByteBufCodecs.FLOAT.encode(buf, e.minDist());
                ByteBufCodecs.FLOAT.encode(buf, e.minOpacity());
                ByteBufCodecs.FLOAT.encode(buf, e.fontScale());
                ByteBufCodecs.BOOL.encode(buf, e.enabled());
            },
            buf -> new OWWaypointEntry(
                    UUIDUtil.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)));

    public static final StreamCodec<ByteBuf, List<OWWaypointEntry>> LIST_STREAM_CODEC = StreamCodec.of(
            (buf, list) -> {
                ByteBufCodecs.VAR_INT.encode(buf, list.size());
                for (OWWaypointEntry e : list) STREAM_CODEC.encode(buf, e);
            },
            buf -> {
                int n = ByteBufCodecs.VAR_INT.decode(buf);
                List<OWWaypointEntry> out = new ArrayList<>(n);
                for (int i = 0; i < n; i++) out.add(STREAM_CODEC.decode(buf));
                return out;
            });

    public OWWaypointEntry withEnabled(boolean value) {
        return new OWWaypointEntry(entityUuid, entityTypeId, hasCustomName, customName, dimension,
                x, y, z, fillColor, borderColor, textColor, entityColor,
                iconSize, maxDist, minDist, minOpacity, fontScale, value);
    }
}
