package net.tiew.operationWild.team;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stockage serveur transitoire des <b>demandes d'adhésion</b> : le miroir de {@link OWTribeInvites}.
 * Ici c'est le joueur qui demande à rejoindre une tribu publique dont il remplit les conditions, et
 * le chef ou un adjoint qui valide (cf. {@code /owtribeapprove}, {@code /owtribereject}).
 *
 * <p>Non persisté : une demande ne survit pas à un redémarrage du serveur, et expire au bout de
 * {@link #TTL_MS} — plus long que pour une invitation, car il faut laisser à un responsable le temps
 * de se connecter. Une seule demande en attente par joueur (un joueur ne peut viser qu'une tribu à la
 * fois, puisqu'il ne peut appartenir qu'à une seule).</p>
 */
public final class OWTribeJoinRequests {

    public static final long TTL_MS = 300_000L; // 5 minutes

    public record Request(int teamId, UUID requesterUUID, String requesterName, long expiresAt) {}

    /** requesterUUID → demande. */
    private static final Map<UUID, Request> PENDING = new ConcurrentHashMap<>();

    private OWTribeJoinRequests() {}

    public static void put(UUID requester, String requesterName, int teamId) {
        PENDING.put(requester, new Request(teamId, requester, requesterName,
                System.currentTimeMillis() + TTL_MS));
    }

    public static Request get(UUID requester) {
        Request req = PENDING.get(requester);
        if (req == null) return null;
        if (System.currentTimeMillis() > req.expiresAt()) {
            PENDING.remove(requester, req);
            return null;
        }
        return req;
    }

    public static Request consume(UUID requester) {
        Request req = get(requester);
        if (req != null) PENDING.remove(requester, req);
        return req;
    }

    /** Demandes en cours pour une tribu (les expirées sont purgées au passage). */
    public static List<Request> forTeam(int teamId) {
        List<Request> out = new ArrayList<>();
        for (UUID requester : new ArrayList<>(PENDING.keySet())) {
            Request req = get(requester); // purge les expirées
            if (req != null && req.teamId() == teamId) out.add(req);
        }
        return out;
    }

    /**
     * Demande en cours d'un joueur <b>nommé</b> vers {@code teamId} — le nom est ce que le
     * responsable a sous la main (bouton de chat, autocomplétion). Insensible à la casse.
     */
    public static Request findByName(int teamId, String requesterName) {
        for (Request req : forTeam(teamId)) {
            if (req.requesterName().equalsIgnoreCase(requesterName)) return req;
        }
        return null;
    }

    /** Chef + adjoints de la tribu actuellement connectés — les seuls à pouvoir valider. */
    public static List<ServerPlayer> onlineApprovers(MinecraftServer server, OWTeam team) {
        List<ServerPlayer> out = new ArrayList<>();
        if (server == null || team == null) return out;
        for (UUID member : team.getPlayerUUIDs()) {
            if (!team.isChief(member) && !team.isDeputy(member)) continue;
            ServerPlayer p = server.getPlayerList().getPlayer(member);
            if (p != null) out.add(p);
        }
        return out;
    }

    /**
     * Enregistre la demande de {@code requester} pour {@code team} et prévient les responsables en
     * ligne (message cliquable Accepter / Refuser).
     *
     * <p>Si aucun responsable n'est connecté, rien n'est enregistré : la demande n'aurait personne
     * pour la traiter et expirerait en silence. Le joueur est invité à réessayer plus tard.</p>
     */
    public static void sendRequest(MinecraftServer server, ServerPlayer requester, OWTeam team) {
        Request existing = get(requester.getUUID());
        if (existing != null && existing.teamId() == team.getTeamId()) {
            requester.sendSystemMessage(Component.translatable("owteams.request.pending")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDD8844))));
            return;
        }

        List<ServerPlayer> approvers = onlineApprovers(server, team);
        if (approvers.isEmpty()) {
            requester.sendSystemMessage(Component.translatable("owteams.request.none_online")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDD8844))));
            return;
        }

        String name = requester.getName().getString();
        put(requester.getUUID(), name, team.getTeamId());

        Component approve = Component.translatable("owteams.request.approve_button")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7DDD73)).withBold(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/owtribeapprove " + name)));
        Component reject = Component.translatable("owteams.request.reject_button")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDD4444)).withBold(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/owtribereject " + name)));
        for (ServerPlayer approver : approvers) {
            approver.sendSystemMessage(Component.translatable("owteams.request.received", name, team.getTeamName())
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFD700))));
            approver.sendSystemMessage(Component.literal("  ").append(approve)
                    .append(Component.literal("   ")).append(reject));
        }

        requester.sendSystemMessage(Component.translatable("owteams.request.sent", team.getTeamName())
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7DDD73))));
    }
}
