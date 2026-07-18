package net.tiew.operationWild.team;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

/**
 * Une exigence d'entrée : une {@link OWTribeJoinCondition} et le seuil à atteindre. Une tribu en
 * porte de 0 à {@link OWTeam#MAX_JOIN_REQUIREMENTS}, toutes requises simultanément.
 *
 * <p>Le constructeur canonique normalise : condition nulle → {@link OWTribeJoinCondition#NONE}, et
 * seuil ramené dans les bornes de la condition. Un client trafiqué ne peut donc pas imposer de
 * valeur hors limites, quelle que soit la porte d'entrée (réseau ou NBT).</p>
 */
public record OWTribeJoinRequirement(OWTribeJoinCondition condition, int threshold) {

    public OWTribeJoinRequirement {
        condition = condition != null ? condition : OWTribeJoinCondition.NONE;
        threshold = condition.hasThreshold() ? condition.clamp(threshold) : 0;
    }

    /** Exigence au seuil par défaut de la condition. */
    public static OWTribeJoinRequirement of(OWTribeJoinCondition condition) {
        return new OWTribeJoinRequirement(condition, condition.getDefaultThreshold());
    }

    /** Même condition, seuil décalé de {@code delta} (borné). */
    public OWTribeJoinRequirement withThresholdShift(int delta) {
        return new OWTribeJoinRequirement(condition, threshold + delta);
    }

    /** Exigence formatée (ex. « 3 créatures niveau 50 »). */
    public Component describe() {
        return condition.getRequirement(threshold);
    }

    public static final StreamCodec<ByteBuf, OWTribeJoinRequirement> STREAM_CODEC = StreamCodec.of(
            (buf, r) -> {
                ByteBufCodecs.INT.encode(buf, r.condition().getId());
                ByteBufCodecs.INT.encode(buf, r.threshold());
            },
            buf -> new OWTribeJoinRequirement(
                    OWTribeJoinCondition.byId(ByteBufCodecs.INT.decode(buf)),
                    ByteBufCodecs.INT.decode(buf)));

    public static final StreamCodec<ByteBuf, List<OWTribeJoinRequirement>> LIST_STREAM_CODEC =
            STREAM_CODEC.apply(ByteBufCodecs.list(OWTeam.MAX_JOIN_REQUIREMENTS));

    /**
     * Nettoie une liste reçue de l'extérieur (réseau, NBT) : retire {@link OWTribeJoinCondition#NONE},
     * dédoublonne par condition (la première gagne) et tronque à {@link OWTeam#MAX_JOIN_REQUIREMENTS}.
     */
    public static List<OWTribeJoinRequirement> sanitize(List<OWTribeJoinRequirement> input) {
        List<OWTribeJoinRequirement> out = new ArrayList<>();
        if (input == null) return out;
        for (OWTribeJoinRequirement r : input) {
            if (r == null || !r.condition().hasThreshold()) continue;
            if (out.stream().anyMatch(o -> o.condition() == r.condition())) continue;
            out.add(r);
            if (out.size() >= OWTeam.MAX_JOIN_REQUIREMENTS) break;
        }
        return out;
    }
}
