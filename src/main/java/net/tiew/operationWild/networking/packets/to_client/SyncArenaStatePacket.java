package net.tiew.operationWild.networking.packets.to_client;

// !! À enregistrer dans OWNetworkHandler (côté to_client) !!

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.client.OWClientArenaState;
import net.tiew.operationWild.core.OWArena;
import net.tiew.operationWild.core.OWReputation;
import net.tiew.operationWild.team.OWArenaChallenges;
import net.tiew.operationWild.team.OWArenaFighter;
import net.tiew.operationWild.team.OWArenaManager;
import net.tiew.operationWild.team.OWArenaMatch;
import net.tiew.operationWild.team.OWReputationData;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTribesSavedData;

import java.util.ArrayList;
import java.util.List;

/**
 * État complet du système de combat d'arène pour la tribu du joueur, poussé à chaque changement.
 *
 * <p>C'est la <b>seule</b> source de l'écran de combat : le client ne déduit rien et n'a aucune
 * vision de l'adversaire au-delà de ce que le serveur consent à révéler. En particulier,
 * {@link #opponentFighters()} reste vide pendant la sélection — on n'envoie que
 * {@link #opponentFighterCount()}, ce qui permet d'afficher « ? » tout en montrant sa progression.</p>
 */
public record SyncArenaStatePacket(
        int phaseOrdinal,
        int myTeamId,
        String opponentName,
        int incomingChallengerTeamId,
        String incomingChallengerName,
        String outgoingTargetName,
        List<OWArenaFighter> myFighters,
        List<OWArenaFighter> opponentFighters,
        int opponentFighterCount,
        boolean myReady, boolean opponentReady,
        List<OWArenaFighter> candidates,
        int myReputation, int opponentReputation,
        int aliveMine, int aliveOpponent,
        int resultOrdinal,
        /** Terrain du duel en cours ou proposé (cf. {@link OWArena.Terrain}). */
        int terrainOrdinal
) implements CustomPacketPayload {

    public static final Type<SyncArenaStatePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "sync_arena_state"));

    public static final StreamCodec<ByteBuf, SyncArenaStatePacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                ByteBufCodecs.INT.encode(buf, p.phaseOrdinal());
                ByteBufCodecs.INT.encode(buf, p.myTeamId());
                ByteBufCodecs.STRING_UTF8.encode(buf, p.opponentName());
                ByteBufCodecs.INT.encode(buf, p.incomingChallengerTeamId());
                ByteBufCodecs.STRING_UTF8.encode(buf, p.incomingChallengerName());
                ByteBufCodecs.STRING_UTF8.encode(buf, p.outgoingTargetName());
                OWArenaFighter.LIST_STREAM_CODEC.encode(buf, p.myFighters());
                OWArenaFighter.LIST_STREAM_CODEC.encode(buf, p.opponentFighters());
                ByteBufCodecs.INT.encode(buf, p.opponentFighterCount());
                ByteBufCodecs.BOOL.encode(buf, p.myReady());
                ByteBufCodecs.BOOL.encode(buf, p.opponentReady());
                OWArenaFighter.LIST_STREAM_CODEC.encode(buf, p.candidates());
                ByteBufCodecs.INT.encode(buf, p.myReputation());
                ByteBufCodecs.INT.encode(buf, p.opponentReputation());
                ByteBufCodecs.INT.encode(buf, p.aliveMine());
                ByteBufCodecs.INT.encode(buf, p.aliveOpponent());
                ByteBufCodecs.INT.encode(buf, p.resultOrdinal());
                ByteBufCodecs.INT.encode(buf, p.terrainOrdinal());
            },
            buf -> new SyncArenaStatePacket(
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    OWArenaFighter.LIST_STREAM_CODEC.decode(buf),
                    OWArenaFighter.LIST_STREAM_CODEC.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    OWArenaFighter.LIST_STREAM_CODEC.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.INT.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    /** État vide : pas de tribu, ou tribu non inscrite à l'arène. */
    public static SyncArenaStatePacket empty() {
        return new SyncArenaStatePacket(OWArena.Phase.IDLE.ordinal(), 0, "", 0, "", "",
                List.of(), List.of(), 0, false, false, List.of(), 0, 0, 0, 0,
                OWArena.Result.NONE.ordinal(), OWArena.Terrain.TERRESTRIAL.ordinal());
    }

    /** Construit l'état vu par {@code viewer}, membre de {@code team}. */
    public static SyncArenaStatePacket of(MinecraftServer server, OWTeam team, java.util.UUID viewer) {
        if (team == null || !team.isArenaAccepted()) return empty();
        int teamId = team.getTeamId();
        // Recenser les créatures éligibles balaie toutes les entités de tous les niveaux : on ne le
        // fait que pour le chef, seul habilité à composer l'équipe.
        boolean isChief = team.isChief(viewer);
        OWTribesSavedData data = OWTribesSavedData.get(server);
        OWReputationData repData = OWReputationData.get(server);
        int myRep = OWReputation.compute(repData, team);

        OWArenaMatch match = OWArenaManager.matchOf(teamId);
        if (match != null) {
            OWArena.Phase phase = switch (match.getState()) {
                case SELECTION -> OWArena.Phase.SELECTION;
                case FIGHTING -> OWArena.Phase.FIGHTING;
                case ENDED -> OWArena.Phase.ENDED;
            };
            boolean revealOpponent = match.getState() != OWArenaMatch.State.SELECTION;
            List<OWArenaFighter> opponents = match.opponentFightersOf(teamId);
            OWTeam opponentTeam = data.findTeamById(match.opponentOf(teamId));

            OWArena.Result result = OWArena.Result.NONE;
            if (match.getState() == OWArenaMatch.State.ENDED) {
                int w = match.getWinnerTeamId();
                result = w == 0 ? OWArena.Result.DRAW
                        : (w == teamId ? OWArena.Result.WIN : OWArena.Result.LOSS);
            }

            return new SyncArenaStatePacket(
                    phase.ordinal(), teamId, match.opponentNameOf(teamId),
                    0, "", "",
                    new ArrayList<>(match.fightersOf(teamId)),
                    revealOpponent ? new ArrayList<>(opponents) : List.of(),
                    opponents.size(),
                    match.isReady(teamId), match.isOpponentReady(teamId),
                    isChief && match.getState() == OWArenaMatch.State.SELECTION
                            ? OWArenaManager.candidatesFor(server, team) : List.of(),
                    myRep,
                    opponentTeam != null ? OWReputation.compute(repData, opponentTeam) : 0,
                    match.aliveOf(teamId).size(),
                    match.aliveOf(match.opponentOf(teamId)).size(),
                    result.ordinal(), match.getTerrain().ordinal());
        }

        // Hors match : défi reçu ou défi émis en attente.
        OWArenaChallenges.Challenge incoming = OWArenaChallenges.get(teamId);
        if (incoming != null) {
            OWTeam challenger = data.findTeamById(incoming.challengerTeamId());
            return new SyncArenaStatePacket(
                    OWArena.Phase.CHALLENGE_RECEIVED.ordinal(), teamId, incoming.challengerTeamName(),
                    incoming.challengerTeamId(), incoming.challengerTeamName(), "",
                    List.of(), List.of(), 0, false, false, List.of(),
                    myRep, challenger != null ? OWReputation.compute(repData, challenger) : 0,
                    0, 0, OWArena.Result.NONE.ordinal(), incoming.terrain().ordinal());
        }

        OWArenaChallenges.Challenge outgoing = OWArenaChallenges.findOutgoing(teamId);
        int outgoingTarget = OWArenaChallenges.outgoingTarget(teamId);
        if (outgoingTarget != 0) {
            OWTeam target = data.findTeamById(outgoingTarget);
            String name = target != null ? target.getTeamName() : "";
            OWArena.Terrain sent = outgoing != null ? outgoing.terrain() : OWArena.Terrain.TERRESTRIAL;
            return new SyncArenaStatePacket(
                    OWArena.Phase.CHALLENGE_SENT.ordinal(), teamId, name, 0, "", name,
                    List.of(), List.of(), 0, false, false, List.of(),
                    myRep, target != null ? OWReputation.compute(repData, target) : 0,
                    0, 0, OWArena.Result.NONE.ordinal(), sent.ordinal());
        }

        return new SyncArenaStatePacket(OWArena.Phase.IDLE.ordinal(), teamId, "", 0, "", "",
                List.of(), List.of(), 0, false, false, List.of(), myRep, 0, 0, 0,
                OWArena.Result.NONE.ordinal(), OWArena.Terrain.TERRESTRIAL.ordinal());
    }

    public static void handle(SyncArenaStatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> OWClientArenaState.set(packet));
    }
}
