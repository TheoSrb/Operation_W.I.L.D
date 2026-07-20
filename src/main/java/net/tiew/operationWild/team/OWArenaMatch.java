package net.tiew.operationWild.team;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.tiew.operationWild.core.OWArena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Un affrontement d'arène entre deux tribus, de la sélection des combattants au verdict.
 *
 * <p>État <b>purement en mémoire</b> : un match ne survit pas à un redémarrage du serveur. Les
 * points de retour sont en revanche recopiés dans les données persistantes de chaque joueur pour
 * qu'un redémarrage en plein combat ne laisse personne bloqué dans la dimension d'arène
 * (cf. {@code OWArenaManager#rescueStrandedPlayer}).</p>
 */
public class OWArenaMatch {

    /** Étapes de vie d'un match. */
    public enum State { SELECTION, FIGHTING, ENDED }

    /** Où renvoyer une entité ou un joueur à la fin du combat. */
    public record ReturnPoint(ResourceKey<Level> dimension, double x, double y, double z) {}

    private final int matchId;
    private final int teamAId, teamBId;
    private final String teamAName, teamBName;
    private final UUID chiefA, chiefB;
    /** Réputations figées à l'ouverture du match : le gain ne doit pas bouger pendant le combat. */
    private final int reputationA, reputationB;

    private final List<OWArenaFighter> fightersA = new ArrayList<>();
    private final List<OWArenaFighter> fightersB = new ArrayList<>();
    private boolean readyA = false, readyB = false;

    private State state = State.SELECTION;
    private long stateStartMs = System.currentTimeMillis();

    /** Combattants encore en vie, par camp (UUID d'entité). */
    private final Set<UUID> aliveA = new LinkedHashSet<>();
    private final Set<UUID> aliveB = new LinkedHashSet<>();
    /** Points de retour des entités <i>et</i> des chefs téléportés. */
    private final Map<UUID, ReturnPoint> returnPoints = new HashMap<>();
    /** Entités effectivement téléportées (pour le nettoyage même si elles meurent). */
    private final Set<UUID> teleportedEntities = new HashSet<>();

    /** 0 = match nul / non tranché. */
    private int winnerTeamId = 0;

    /** Le rétrécissement de la zone de combat a-t-il déjà été lancé ? */
    private boolean borderShrinking = false;

    public OWArenaMatch(int matchId, int teamAId, String teamAName, UUID chiefA, int reputationA,
                        int teamBId, String teamBName, UUID chiefB, int reputationB) {
        this.matchId = matchId;
        this.teamAId = teamAId; this.teamAName = teamAName; this.chiefA = chiefA; this.reputationA = reputationA;
        this.teamBId = teamBId; this.teamBName = teamBName; this.chiefB = chiefB; this.reputationB = reputationB;
    }

    // ── Identité ─────────────────────────────────────────────────────────────────
    public int getMatchId() { return matchId; }
    public int getTeamAId() { return teamAId; }
    public int getTeamBId() { return teamBId; }
    public String getTeamAName() { return teamAName; }
    public String getTeamBName() { return teamBName; }
    public UUID getChiefA() { return chiefA; }
    public UUID getChiefB() { return chiefB; }
    public int getReputationA() { return reputationA; }
    public int getReputationB() { return reputationB; }

    public boolean involves(int teamId) { return teamId == teamAId || teamId == teamBId; }
    public boolean isSideA(int teamId) { return teamId == teamAId; }
    public int opponentOf(int teamId) { return isSideA(teamId) ? teamBId : teamAId; }
    public String opponentNameOf(int teamId) { return isSideA(teamId) ? teamBName : teamAName; }

    public UUID chiefOf(int teamId) { return isSideA(teamId) ? chiefA : chiefB; }

    // ── Sélection ────────────────────────────────────────────────────────────────
    public List<OWArenaFighter> fightersOf(int teamId) { return isSideA(teamId) ? fightersA : fightersB; }
    public List<OWArenaFighter> opponentFightersOf(int teamId) { return isSideA(teamId) ? fightersB : fightersA; }

    public boolean isReady(int teamId) { return isSideA(teamId) ? readyA : readyB; }
    public boolean isOpponentReady(int teamId) { return isSideA(teamId) ? readyB : readyA; }

    public void setReady(int teamId, boolean ready) {
        if (isSideA(teamId)) readyA = ready; else readyB = ready;
    }

    public boolean bothReady() {
        return readyA && readyB && !fightersA.isEmpty() && !fightersB.isEmpty();
    }

    /**
     * Ajoute un combattant à un camp. Refusé si la limite est atteinte, si l'archétype est déjà
     * représenté (règle « un archétype unique par combattant », façon Paladins), ou si la créature
     * est déjà engagée.
     */
    public boolean addFighter(int teamId, OWArenaFighter fighter) {
        if (state != State.SELECTION || fighter == null) return false;
        List<OWArenaFighter> side = fightersOf(teamId);
        if (side.size() >= OWArena.MAX_FIGHTERS) return false;
        for (OWArenaFighter f : side) {
            if (f.entityUuid().equals(fighter.entityUuid())) return false;
            if (f.archetypeOrdinal() == fighter.archetypeOrdinal()) return false;
        }
        side.add(fighter);
        setReady(teamId, false); // toute modification invalide la confirmation
        return true;
    }

    public boolean removeFighter(int teamId, UUID entityUuid) {
        if (state != State.SELECTION) return false;
        boolean removed = fightersOf(teamId).removeIf(f -> f.entityUuid().equals(entityUuid));
        if (removed) setReady(teamId, false);
        return removed;
    }

    /** Vrai si {@code archetypeOrdinal} est déjà pris dans ce camp (pour griser l'entrée côté client). */
    public boolean archetypeTaken(int teamId, int archetypeOrdinal) {
        for (OWArenaFighter f : fightersOf(teamId)) {
            if (f.archetypeOrdinal() == archetypeOrdinal) return true;
        }
        return false;
    }

    // ── Combat ───────────────────────────────────────────────────────────────────
    public State getState() { return state; }

    public void setState(State s) {
        this.state = s;
        this.stateStartMs = System.currentTimeMillis();
    }

    public long elapsedInStateMs() { return System.currentTimeMillis() - stateStartMs; }

    public Set<UUID> aliveOf(int teamId) { return isSideA(teamId) ? aliveA : aliveB; }
    public Set<UUID> getAliveA() { return aliveA; }
    public Set<UUID> getAliveB() { return aliveB; }

    public Map<UUID, ReturnPoint> getReturnPoints() { return returnPoints; }
    public Set<UUID> getTeleportedEntities() { return teleportedEntities; }

    public int getWinnerTeamId() { return winnerTeamId; }
    public void setWinnerTeamId(int v) { this.winnerTeamId = v; }

    public boolean isBorderShrinking() { return borderShrinking; }
    public void setBorderShrinking(boolean v) { this.borderShrinking = v; }

    /**
     * Décalage en X de l'aire de combat. <b>Toujours 0</b> : l'arène n'accueille qu'un combat à la
     * fois (une seule zone par dimension), et une aire fixe permet d'y bâtir un décor permanent
     * plutôt que de se battre sur du plat régénéré ailleurs à chaque match.
     */
    public int arenaOffsetX() { return 0; }
}
