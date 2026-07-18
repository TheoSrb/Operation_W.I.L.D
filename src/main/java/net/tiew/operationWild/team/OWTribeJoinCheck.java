package net.tiew.operationWild.team;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.tiew.operationWild.core.OWCurrency;
import net.tiew.operationWild.core.OWReputation;
import net.tiew.operationWild.core.OWTamingXp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Évaluation <b>serveur-autoritative</b> des {@link OWTribeJoinRequirement conditions d'entrée} d'une
 * tribu publique — elles se cumulent : il faut les remplir toutes. Le client ne mesure jamais rien
 * lui-même : il reçoit le verdict déjà calculé (cf. {@code SyncTribeListPacket}), et le serveur
 * revérifie de toute façon à l'adhésion (cf. {@code JoinTribePacket}).
 *
 * <p>Les mesures liées aux créatures sont lues dans {@link OWReputationData}, le registre persistant
 * des créatures apprivoisées : elles restent donc exactes sans dépendre des chunks chargés.</p>
 */
public final class OWTribeJoinCheck {

    private OWTribeJoinCheck() {}

    private static final int TICKS_PER_HOUR = 20 * 60 * 60;

    /**
     * Photographie des mesures d'un joueur, indexée par {@link OWTribeJoinCondition#ordinal()}.
     * Calculée une fois puis confrontée à autant de tribus que nécessaire (la liste des tribus est
     * évaluée par joueur), ce qui évite de reparcourir le registre des créatures pour chaque tribu.
     */
    public record Snapshot(long[] values) {

        public long valueOf(OWTribeJoinCondition condition) {
            return condition == null ? 0L : values[condition.ordinal()];
        }
    }

    /** Mesure toutes les conditions pour {@code player} en un seul parcours du registre. */
    public static Snapshot snapshot(MinecraftServer server, ServerPlayer player) {
        long[] v = new long[OWTribeJoinCondition.values().length];
        if (server == null || player == null) return new Snapshot(v);

        List<OWReputationData.EntityRep> owned =
                OWReputationData.get(server).entitiesOf(player.getUUID());

        int count = 0, maxLevel = 0, atMaxLevel = 0, totalLevels = 0;
        double repValue = 0.0;
        Set<String> species = new HashSet<>();
        for (OWReputationData.EntityRep rep : owned) {
            count++;
            totalLevels += rep.level;
            maxLevel = Math.max(maxLevel, rep.level);
            if (rep.level >= 50) atMaxLevel++;
            if (rep.species != null) species.add(rep.species);
            repValue += OWReputation.entityValue(rep.species, rep.level);
        }

        v[OWTribeJoinCondition.WILD_COINS.ordinal()]         = OWCurrency.getWildCoins(player);
        v[OWTribeJoinCondition.ENTITY_COUNT.ordinal()]       = count;
        v[OWTribeJoinCondition.MAX_LEVEL_ENTITIES.ordinal()] = atMaxLevel;
        v[OWTribeJoinCondition.BEST_ENTITY_LEVEL.ordinal()]  = maxLevel;
        v[OWTribeJoinCondition.TOTAL_LEVELS.ordinal()]       = totalLevels;
        v[OWTribeJoinCondition.SPECIES_COUNT.ordinal()]      = species.size();
        v[OWTribeJoinCondition.REPUTATION_VALUE.ordinal()]   = Math.round(repValue);
        v[OWTribeJoinCondition.TAMING_XP.ordinal()]          = (long) OWTamingXp.getTamingXp(player);
        v[OWTribeJoinCondition.XP_LEVEL.ordinal()]           = player.experienceLevel;
        v[OWTribeJoinCondition.PLAYTIME_HOURS.ordinal()]     =
                player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)) / TICKS_PER_HOUR;
        return new Snapshot(v);
    }

    /** Vrai si le joueur photographié atteint le seuil de cette exigence. */
    public static boolean meets(Snapshot snapshot, OWTribeJoinRequirement requirement) {
        if (requirement == null || !requirement.condition().hasThreshold()) return true;
        return snapshot.valueOf(requirement.condition()) >= requirement.threshold();
    }

    /**
     * Vrai si le joueur photographié remplit <b>toutes</b> les conditions d'entrée de {@code team}
     * (elles se cumulent). Une tribu sans condition accepte tout le monde.
     */
    public static boolean meets(Snapshot snapshot, OWTeam team) {
        if (team == null) return false;
        for (OWTribeJoinRequirement r : team.getJoinRequirements()) {
            if (!meets(snapshot, r)) return false;
        }
        return true;
    }

    /** Exigences que le joueur ne remplit pas (vide s'il peut entrer), dans l'ordre d'affichage. */
    public static List<OWTribeJoinRequirement> unmet(Snapshot snapshot, OWTeam team) {
        List<OWTribeJoinRequirement> out = new ArrayList<>();
        if (team == null) return out;
        for (OWTribeJoinRequirement r : team.getJoinRequirements()) {
            if (!meets(snapshot, r)) out.add(r);
        }
        return out;
    }
}
