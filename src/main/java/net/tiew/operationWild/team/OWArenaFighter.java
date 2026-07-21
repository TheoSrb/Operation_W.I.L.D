package net.tiew.operationWild.team;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.config.OWEntityConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Instantané d'une créature candidate ou engagée comme combattant d'arène.
 *
 * <p>C'est une <b>photo</b> et non une référence vivante : elle voyage jusqu'au client pour alimenter
 * l'écran de sélection (aperçu 3D, nom, niveau, archétype) même si la créature n'est chargée nulle
 * part près du joueur. Le serveur retrouve la vraie entité par {@link #entityUuid()} au moment de la
 * téléportation.</p>
 */
public record OWArenaFighter(
        UUID entityUuid, UUID ownerUuid, String ownerName,
        String entityTypeId, String name, int level, int archetypeOrdinal, int skinIndex, int typeVariant,
        boolean male
) {

    public static final StreamCodec<ByteBuf, OWArenaFighter> STREAM_CODEC = StreamCodec.of(
            (buf, f) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, f.entityUuid().toString());
                ByteBufCodecs.STRING_UTF8.encode(buf, f.ownerUuid().toString());
                ByteBufCodecs.STRING_UTF8.encode(buf, f.ownerName());
                ByteBufCodecs.STRING_UTF8.encode(buf, f.entityTypeId());
                ByteBufCodecs.STRING_UTF8.encode(buf, f.name());
                ByteBufCodecs.INT.encode(buf, f.level());
                ByteBufCodecs.INT.encode(buf, f.archetypeOrdinal());
                ByteBufCodecs.INT.encode(buf, f.skinIndex());
                ByteBufCodecs.INT.encode(buf, f.typeVariant());
                ByteBufCodecs.BOOL.encode(buf, f.male());
            },
            buf -> new OWArenaFighter(
                    parseUuid(ByteBufCodecs.STRING_UTF8.decode(buf)),
                    parseUuid(ByteBufCodecs.STRING_UTF8.decode(buf)),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)));

    public static final StreamCodec<ByteBuf, List<OWArenaFighter>> LIST_STREAM_CODEC = StreamCodec.of(
            (buf, list) -> {
                ByteBufCodecs.INT.encode(buf, list.size());
                for (OWArenaFighter f : list) STREAM_CODEC.encode(buf, f);
            },
            buf -> {
                int n = ByteBufCodecs.INT.decode(buf);
                List<OWArenaFighter> out = new ArrayList<>(n);
                for (int i = 0; i < n; i++) out.add(STREAM_CODEC.decode(buf));
                return out;
            });

    private static UUID parseUuid(String s) {
        try { return UUID.fromString(s); } catch (IllegalArgumentException e) { return new UUID(0L, 0L); }
    }

    /** Photographie une créature apprivoisée. {@code null} si elle n'a pas de propriétaire. */
    public static OWArenaFighter of(OWEntity entity, String ownerName) {
        if (entity == null || entity.getOwnerUUID() == null) return null;
        String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        String name = entity.hasCustomName() && entity.getCustomName() != null
                ? entity.getCustomName().getString()
                : entity.getType().getDescription().getString();
        // getArchetype() vaut null sur OWEntity et n'est renseigné que par les sous-classes qui le
        // surchargent : une créature qui n'en déclare pas est un cas normal, pas une anomalie. On
        // la range en NORMAL plutôt que de planter ou de l'escamoter de la liste.
        OWEntityConfig.Archetypes archetype = entity.getArchetype();
        int archetypeOrdinal = (archetype != null ? archetype : OWEntityConfig.Archetypes.NORMAL).ordinal();
        return new OWArenaFighter(
                entity.getUUID(), entity.getOwnerUUID(), ownerName != null ? ownerName : "",
                typeId, name, entity.getLevel(),
                archetypeOrdinal, entity.getSkinIndex(), entity.getTypeVariant(),
                entity.isMale());
    }

    public OWEntityConfig.Archetypes archetype() {
        OWEntityConfig.Archetypes[] all = OWEntityConfig.Archetypes.values();
        return all[Math.max(0, Math.min(all.length - 1, archetypeOrdinal))];
    }

    /** Clé de traduction du genre, telle qu'affichée au-dessus de la créature dans le monde. */
    public String genderKey() { return male ? "tooltip.genderMale" : "tooltip.genderFemale"; }

    /** Clé de traduction du nom de l'archétype (ex. {@code owteams.arena.archetype.tank}). */
    public String archetypeKey() {
        return "owteams.arena.archetype." + archetype().name().toLowerCase();
    }
}
