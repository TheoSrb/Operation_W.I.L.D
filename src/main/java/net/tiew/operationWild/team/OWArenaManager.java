package net.tiew.operationWild.team;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.core.OWArena;
import net.tiew.operationWild.core.OWReputation;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.ArenaClashPacket;
import net.tiew.operationWild.networking.packets.to_client.ArenaVictoryPacket;
import net.tiew.operationWild.networking.packets.to_client.SyncArenaStatePacket;
import net.tiew.operationWild.worldgen.dimension.OWDimensions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Orchestrateur serveur de l'arène : défis entre tribus, sélection des combattants, téléportation
 * dans la dimension d'arène, verdict et récompenses.
 *
 * <p><b>Serveur-autoritatif de bout en bout</b> : le client ne fait qu'émettre des intentions et
 * afficher l'état qu'on lui pousse ({@link SyncArenaStatePacket}). Aucune décision — sélection,
 * démarrage, victoire, gain de réputation — n'est prise côté client.</p>
 *
 * <p>Les matchs vivent en mémoire uniquement. Pour qu'un redémarrage en plein combat ne laisse
 * personne coincé dans la dimension d'arène, le point de retour de chaque joueur téléporté est aussi
 * recopié dans ses données persistantes ({@link #RETURN_KEY}) et rejoué à la connexion par
 * {@link #rescueStrandedPlayer}.</p>
 */
public final class OWArenaManager {

    private OWArenaManager() {}

    /** Clé du point de retour de secours dans les données persistantes du joueur. */
    private static final String RETURN_KEY = "ow_arena_return";

    /** teamId → match en cours (les deux tribus pointent sur le même objet). */
    private static final Map<Integer, OWArenaMatch> MATCHES = new ConcurrentHashMap<>();
    private static final AtomicInteger NEXT_MATCH_ID = new AtomicInteger(1);

    // ── Accès ────────────────────────────────────────────────────────────────────
    public static OWArenaMatch matchOf(int teamId) { return MATCHES.get(teamId); }

    public static OWArenaMatch matchOfPlayer(MinecraftServer server, ServerPlayer player) {
        OWTeam team = OWTribesSavedData.get(server).findTeamByMember(player.getUUID());
        return team != null ? MATCHES.get(team.getTeamId()) : null;
    }

    // ── Défis ────────────────────────────────────────────────────────────────────
    /** Le chef de {@code player} défie la tribu {@code targetTeamId}. */
    public static void challenge(MinecraftServer server, ServerPlayer player, int targetTeamId) {
        OWTribesSavedData data = OWTribesSavedData.get(server);
        OWTeam mine = data.findTeamByMember(player.getUUID());
        OWTeam target = data.findTeamById(targetTeamId);
        if (mine == null || target == null || mine.getTeamId() == targetTeamId) return;
        if (!mine.isChief(player.getUUID())) return;                       // chef uniquement
        if (!mine.isArenaAccepted() || !target.isArenaAccepted()) return;  // les deux doivent être inscrites
        if (MATCHES.containsKey(mine.getTeamId()) || MATCHES.containsKey(targetTeamId)) return; // déjà en combat

        OWArenaChallenges.put(targetTeamId, mine.getTeamId(), mine.getTeamName(), player.getUUID());

        ServerPlayer targetChief = server.getPlayerList().getPlayer(target.getTeamOwnerUUID());
        if (targetChief != null) {
            targetChief.sendSystemMessage(Component.translatable("owteams.arena.challenge.received", mine.getTeamName())
                    .setStyle(Style.EMPTY.withColor(0xE8956A)));
            syncTo(server, targetChief);
        }
        player.sendSystemMessage(Component.translatable("owteams.arena.challenge.sent", target.getTeamName())
                .setStyle(Style.EMPTY.withColor(0x7ddd73)));
        syncTo(server, player);
    }

    /** Le chef de {@code player} accepte ou décline le défi qui vise sa tribu. */
    public static void respond(MinecraftServer server, ServerPlayer player, boolean accept) {
        OWTribesSavedData data = OWTribesSavedData.get(server);
        OWTeam mine = data.findTeamByMember(player.getUUID());
        if (mine == null || !mine.isChief(player.getUUID())) return;

        OWArenaChallenges.Challenge challenge = OWArenaChallenges.consume(mine.getTeamId());
        if (challenge == null) return;
        OWTeam challenger = data.findTeamById(challenge.challengerTeamId());
        if (challenger == null) return;

        ServerPlayer otherChief = server.getPlayerList().getPlayer(challenger.getTeamOwnerUUID());

        if (!accept) {
            if (otherChief != null) {
                otherChief.sendSystemMessage(Component.translatable("owteams.arena.challenge.declined", mine.getTeamName())
                        .setStyle(Style.EMPTY.withColor(0xE04444)));
                syncTo(server, otherChief);
            }
            syncTo(server, player);
            return;
        }

        // Les deux chefs doivent être en ligne pour composer leur équipe.
        if (otherChief == null) {
            player.sendSystemMessage(Component.translatable("owteams.arena.challenge.chief_offline")
                    .setStyle(Style.EMPTY.withColor(0xE04444)));
            syncTo(server, player);
            return;
        }
        if (MATCHES.containsKey(mine.getTeamId()) || MATCHES.containsKey(challenger.getTeamId())) return;

        OWReputationData repData = OWReputationData.get(server);
        OWArenaMatch match = new OWArenaMatch(
                NEXT_MATCH_ID.getAndIncrement(),
                challenger.getTeamId(), challenger.getTeamName(), challenger.getTeamOwnerUUID(),
                OWReputation.compute(repData, challenger),
                mine.getTeamId(), mine.getTeamName(), mine.getTeamOwnerUUID(),
                OWReputation.compute(repData, mine));
        MATCHES.put(challenger.getTeamId(), match);
        MATCHES.put(mine.getTeamId(), match);
        OWArenaChallenges.clearFor(mine.getTeamId());
        OWArenaChallenges.clearFor(challenger.getTeamId());

        for (ServerPlayer p : List.of(player, otherChief)) {
            p.sendSystemMessage(Component.translatable("owteams.arena.match.started")
                    .setStyle(Style.EMPTY.withColor(0xFFD257)));
        }
        syncMatch(server, match);
    }

    // ── Sélection des combattants ────────────────────────────────────────────────
    /**
     * Créatures éligibles comme combattantes pour une tribu : créatures apprivoisées <b>chargées</b>
     * appartenant à un de ses membres. La contrainte de chargement est volontaire — on ne peut
     * téléporter que ce qui existe réellement dans le monde à cet instant.
     */
    public static List<OWArenaFighter> candidatesFor(MinecraftServer server, OWTeam team) {
        List<OWArenaFighter> out = new ArrayList<>();
        if (team == null) return out;
        for (ServerLevel level : server.getAllLevels()) {
            if (level.dimension().equals(OWDimensions.ARENA)) continue; // pas de recrutement dans l'arène
            for (Entity e : level.getAllEntities()) {
                if (!(e instanceof OWEntity owE) || !owE.isTame()) continue;
                UUID owner = owE.getOwnerUUID();
                if (owner == null || !team.isMember(owner)) continue;
                if (owE.isDeadOrDying()) continue;
                out.add(OWArenaFighter.of(owE, ownerNameOf(team, owner)));
            }
        }
        out.removeIf(java.util.Objects::isNull);
        return out;
    }

    private static String ownerNameOf(OWTeam team, UUID owner) {
        int idx = team.getPlayerUUIDs().indexOf(owner);
        return idx >= 0 && idx < team.getPlayerNames().size()
                ? team.getPlayerNames().get(idx) : owner.toString().substring(0, 8);
    }

    /** Ajoute ou retire une créature de l'équipe du chef. */
    public static void selectFighter(MinecraftServer server, ServerPlayer player, UUID entityUuid, boolean add) {
        OWTeam team = OWTribesSavedData.get(server).findTeamByMember(player.getUUID());
        if (team == null || !team.isChief(player.getUUID())) return;
        OWArenaMatch match = MATCHES.get(team.getTeamId());
        if (match == null || match.getState() != OWArenaMatch.State.SELECTION) return;

        if (add) {
            // On revalide la créature côté serveur : le client peut envoyer n'importe quel UUID.
            OWArenaFighter fighter = null;
            for (OWArenaFighter c : candidatesFor(server, team)) {
                if (c.entityUuid().equals(entityUuid)) { fighter = c; break; }
            }
            if (fighter == null) return;
            match.addFighter(team.getTeamId(), fighter);
        } else {
            match.removeFighter(team.getTeamId(), entityUuid);
        }
        syncMatch(server, match);
    }

    /** Le chef confirme (ou retire) sa composition. Quand les deux ont confirmé, le combat démarre. */
    public static void confirm(MinecraftServer server, ServerPlayer player, boolean ready) {
        OWTeam team = OWTribesSavedData.get(server).findTeamByMember(player.getUUID());
        if (team == null || !team.isChief(player.getUUID())) return;
        OWArenaMatch match = MATCHES.get(team.getTeamId());
        if (match == null || match.getState() != OWArenaMatch.State.SELECTION) return;
        if (ready && match.fightersOf(team.getTeamId()).isEmpty()) return; // au moins un combattant

        match.setReady(team.getTeamId(), ready);
        if (!match.bothReady()) { syncMatch(server, match); return; }

        // Un seul combat à la fois dans l'arène (une seule zone par dimension) : si elle est prise,
        // on garde le match prêt et le tick le lancera dès qu'elle se libère.
        if (arenaBusy(match)) {
            broadcastToMatch(server, match, Component.translatable("owteams.arena.zone.busy")
                    .setStyle(Style.EMPTY.withColor(0xE8956A)));
            match.setState(OWArenaMatch.State.SELECTION);   // relance le minuteur d'attente
            syncMatch(server, match);
            return;
        }
        beginFight(server, match);
    }

    /** Annulation : retire un défi émis, ou déclare forfait si le combat est engagé. */
    public static void cancel(MinecraftServer server, ServerPlayer player) {
        OWTeam team = OWTribesSavedData.get(server).findTeamByMember(player.getUUID());
        if (team == null || !team.isChief(player.getUUID())) return;

        OWArenaMatch match = MATCHES.get(team.getTeamId());
        if (match != null) {
            if (match.getState() == OWArenaMatch.State.SELECTION) {
                endMatch(server, match, 0, true);   // annulation à l'amiable, aucun gain
            } else if (match.getState() == OWArenaMatch.State.FIGHTING) {
                endMatch(server, match, match.opponentOf(team.getTeamId()), false); // forfait
            }
            return;
        }
        OWArenaChallenges.clearFor(team.getTeamId());
        syncTo(server, player);
    }

    // ── Combat ───────────────────────────────────────────────────────────────────
    /** Téléporte les combattants et les deux chefs dans la dimension d'arène. */
    private static void beginFight(MinecraftServer server, OWArenaMatch match) {
        ServerLevel arena = server.getLevel(OWDimensions.ARENA);
        if (arena == null) {
            // Datapack absent ou dimension non chargée : on annule proprement plutôt que de planter.
            broadcastToMatch(server, match, Component.translatable("owteams.arena.error.no_dimension")
                    .setStyle(Style.EMPTY.withColor(0xE04444)));
            endMatch(server, match, 0, true);
            return;
        }

        int ox = match.arenaOffsetX();
        match.setState(OWArenaMatch.State.FIGHTING);

        // Le terrain doit exister AVANT d'y poser qui que ce soit : sans cela on téléporte dans le
        // vide et la génération referme le sol par-dessus (d'où les spawns « dans le sol »).
        // Les chunks sont aussi forcés pendant tout le combat, pour que les créatures continuent
        // de tourner même si un chef se déconnecte.
        forceArenaChunks(arena, match, ox, true);

        placeSide(server, arena, match, match.getTeamAId(), ox - OWArena.ARENA_HALF_SPAN, 90f);
        placeSide(server, arena, match, match.getTeamBId(), ox + OWArena.ARENA_HALF_SPAN, -90f);

        // Les chefs assistent au combat, en retrait derrière leur ligne.
        placeChief(server, arena, match, match.getChiefA(),
                ox - OWArena.ARENA_HALF_SPAN - OWArena.ARENA_CHIEF_BACK, 90f);
        placeChief(server, arena, match, match.getChiefB(),
                ox + OWArena.ARENA_HALF_SPAN + OWArena.ARENA_CHIEF_BACK, -90f);

        openBattleZone(server, arena, match, ox);

        broadcastToMatch(server, match, Component.translatable("owteams.arena.fight.begin")
                .setStyle(Style.EMPTY.withColor(0xFFD257)));
        sendClashAnimation(server, match);
        syncMatch(server, match);
    }

    /**
     * Force (ou libère) les chunks de l'aire de combat. Forcer sert deux fois : garantir que le
     * terrain est généré avant la téléportation, et que les créatures continuent d'être simulées
     * pendant tout le combat même sans joueur à proximité.
     */
    private static void forceArenaChunks(ServerLevel arena, OWArenaMatch match, int ox, boolean forced) {
        int radius = (OWArena.ARENA_HALF_SPAN + OWArena.ARENA_CHIEF_BACK) / 16 + 1;
        int centerX = ox >> 4;
        for (int cx = centerX - radius; cx <= centerX + radius; cx++) {
            for (int cz = -radius; cz <= radius; cz++) {
                arena.setChunkForced(cx, cz, forced);
            }
        }
    }

    /**
     * Ordonnée sûre au-dessus du sol, lue sur le <b>heightmap réel</b> plutôt que sur une constante :
     * si la configuration du monde plat change, le placement suit sans qu'on ait à y penser.
     */
    /**
     * Ordonnée sûre pour déposer quelqu'un en {@code (x, z)}, sur <b>n'importe quel</b> niveau.
     *
     * <p>Utilisé pour tous les trajets, aller <i>comme</i> retour. Un point de retour mémorisé peut
     * être devenu invalide (terrain modifié, coordonnée légèrement enterrée) : arriver encastré
     * bloque toutes les interactions, le raycast partant de l'intérieur d'un bloc.</p>
     *
     * <p>Trois filets successifs : la position souhaitée si elle est libre, sinon la surface lue sur
     * le heightmap, et dans tous les cas une remontée tant que les deux blocs occupés ne sont pas
     * dégagés — le heightmap pouvant mentir sur un chunk fraîchement généré.</p>
     */
    private static double safeY(ServerLevel level, double x, double z, double preferredY) {
        int bx = net.minecraft.util.Mth.floor(x), bz = net.minecraft.util.Mth.floor(z);
        level.getChunk(bx >> 4, bz >> 4);   // force la génération : sans terrain, tout le reste ment

        int min = level.getMinBuildHeight() + 1;
        int max = level.getMaxBuildHeight() - 2;
        int y = net.minecraft.util.Mth.clamp(net.minecraft.util.Mth.floor(preferredY), min, max);

        if (!isFree(level, bx, y, bz)) {
            int surface = level.getHeight(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz);
            y = net.minecraft.util.Mth.clamp(surface, min, max);
        }
        int guard = 0;
        while (y < max && !isFree(level, bx, y, bz) && guard++ < 48) y++;
        return y;
    }

    /** Vrai si les deux blocs occupés par une créature de taille standard sont libres de collision. */
    private static boolean isFree(ServerLevel level, int x, int y, int z) {
        net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(x, y, z);
        net.minecraft.core.BlockPos above = pos.above();
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                && level.getBlockState(above).getCollisionShape(level, above).isEmpty();
    }

    /** Ordonnée d'arrivée dans l'arène : on vise le plain-pied de la dimension. */
    private static double arenaY(ServerLevel arena, int x, int z) {
        return safeY(arena, x, z, OWArena.ARENA_Y);
    }

    private static void placeSide(MinecraftServer server, ServerLevel arena, OWArenaMatch match,
                                  int teamId, int x, float yRot) {
        List<OWArenaFighter> fighters = match.fightersOf(teamId);
        for (int i = 0; i < fighters.size(); i++) {
            OWArenaFighter f = fighters.get(i);
            Entity entity = findEntity(server, f.entityUuid());
            if (!(entity instanceof OWEntity owE)) continue;

            match.getReturnPoints().put(f.entityUuid(), new OWArenaMatch.ReturnPoint(
                    owE.level().dimension(), owE.getX(), owE.getY(), owE.getZ()));
            // Doublé dans les données persistantes de la créature : elles survivent au changement
            // de dimension (qui recrée l'entité) et permettent au balayage de sécurité de la
            // ramener même si le renvoi de fin de match n'a pas abouti.
            saveEntityRescuePoint(owE);

            int z = (int) Math.round((i - (fighters.size() - 1) / 2.0) * 4.0);
            Entity moved = owE.changeDimension(new DimensionTransition(
                    arena, new Vec3(x + 0.5, arenaY(arena, x, z), z + 0.5), Vec3.ZERO, yRot, 0f,
                    DimensionTransition.DO_NOTHING));
            if (moved instanceof OWEntity arrived) {
                readyForBattle(arrived, match, teamId);
                match.aliveOf(teamId).add(f.entityUuid());
                match.getTeleportedEntities().add(f.entityUuid());
            }
        }
    }

    /**
     * Remet une créature en état de combattre à son arrivée dans l'arène : debout, réveillée,
     * sans cavalier, et sa tribu rétablie.
     *
     * <p>Le rétablissement de la tribu est indispensable : {@code changeDimension} <b>recrée</b>
     * l'entité, et {@code currentTeam} est un champ transitoire qui repart donc à {@code null} —
     * sans quoi les permissions de tribu et les alliances ne fonctionnent plus dans l'arène.</p>
     */
    private static void readyForBattle(OWEntity entity, OWArenaMatch match, int teamId) {
        entity.currentTeam = OWTribesSavedData.get(entity.getServer()).findTeamById(teamId);
        // Une créature laissée assise à la base arriverait assise et ne se battrait jamais.
        entity.setSitting(false);
        entity.setNap(false);
        entity.setSleeping(false);
        entity.ejectPassengers();
        entity.setTarget(null);
    }

    private static void placeChief(MinecraftServer server, ServerLevel arena, OWArenaMatch match,
                                   UUID chief, int x, float yRot) {
        ServerPlayer p = server.getPlayerList().getPlayer(chief);
        if (p == null) return;
        match.getReturnPoints().put(chief, new OWArenaMatch.ReturnPoint(
                p.level().dimension(), p.getX(), p.getY(), p.getZ()));
        saveRescuePoint(p);
        // Descendre de monture avant le voyage : un joueur téléporté à cheval sur une créature
        // qui part ailleurs se retrouve dans un état incohérent.
        p.stopRiding();
        p.changeDimension(new DimensionTransition(
                arena, new Vec3(x + 0.5, arenaY(arena, x, 0), 0.5), Vec3.ZERO, yRot, 0f,
                DimensionTransition.DO_NOTHING));
    }

    /**
     * Envoie l'animation de confrontation aux deux chefs. Chacun reçoit un paquet construit de
     * <b>son</b> point de vue : sa propre bannière est toujours celle de gauche.
     */
    private static void sendClashAnimation(MinecraftServer server, OWArenaMatch match) {
        OWTribesSavedData data = OWTribesSavedData.get(server);
        OWTeam teamA = data.findTeamById(match.getTeamAId());
        OWTeam teamB = data.findTeamById(match.getTeamBId());
        if (teamA == null || teamB == null) return;

        ServerPlayer chiefA = server.getPlayerList().getPlayer(match.getChiefA());
        if (chiefA != null) OWNetworkHandler.sendToClient(ArenaClashPacket.of(teamA, teamB), chiefA);
        ServerPlayer chiefB = server.getPlayerList().getPlayer(match.getChiefB());
        if (chiefB != null) OWNetworkHandler.sendToClient(ArenaClashPacket.of(teamB, teamA), chiefB);
    }

    /**
     * Envoie la cinématique de verdict aux deux chefs. Les deux voient la même scène — la bannière
     * du vainqueur brise celle du vaincu — seul le bandeau « Victoire »/« Défaite » diffère.
     * Aucun envoi en cas de match nul : il n'y a pas de bannière à briser.
     */
    private static void sendVictoryAnimation(MinecraftServer server, OWArenaMatch match, int winnerTeamId) {
        if (winnerTeamId == 0) return;
        OWTribesSavedData data = OWTribesSavedData.get(server);
        OWTeam winner = data.findTeamById(winnerTeamId);
        OWTeam loser = data.findTeamById(match.opponentOf(winnerTeamId));
        if (winner == null || loser == null) return;

        String winnerName = winner.getTeamName() != null ? winner.getTeamName() : "";
        String loserName = loser.getTeamName() != null ? loser.getTeamName() : "";
        for (int teamId : new int[]{ match.getTeamAId(), match.getTeamBId() }) {
            ServerPlayer chief = server.getPlayerList().getPlayer(match.chiefOf(teamId));
            if (chief == null) continue;
            OWNetworkHandler.sendToClient(new ArenaVictoryPacket(
                    winnerName, OWBannerLook.of(winner),
                    loserName, OWBannerLook.of(loser),
                    teamId == winnerTeamId), chief);
        }
    }

    /**
     * Ouvre la zone de combat autour de l'aire courante, à sa taille de départ.
     *
     * <p>On s'appuie sur la <b>world border</b> vanilla : elle est déjà affichée par le client, déjà
     * synchronisée, et inflige d'elle-même des dégâts hors zone. Conséquence directe : un niveau
     * n'en possède qu'une seule, donc l'arène ne peut accueillir qu'<b>un combat à la fois</b>
     * (cf. {@link #arenaBusy}).</p>
     */
    private static void openBattleZone(MinecraftServer server, ServerLevel arena,
                                       OWArenaMatch match, int centerX) {
        net.minecraft.world.level.border.WorldBorder border = arena.getWorldBorder();
        border.setCenter(centerX + 0.5, 0.5);
        border.setSize(OWArena.BORDER_START);
        border.setDamagePerBlock(0.6);
        border.setDamageSafeZone(0.5);
        border.setWarningBlocks(6);
        border.setWarningTime(12);
        pushZoneToChiefs(server, match, border);
    }

    /**
     * Pousse l'état de la zone aux chefs présents dans l'arène.
     *
     * <p>Indispensable : le vanilla n'envoie au client que la bordure de l'<b>overworld</b>
     * ({@code PlayerList#sendLevelInfo}) et n'installe son écouteur de diffusion que sur celle-ci.
     * Modifier la bordure d'une autre dimension n'atteindrait donc jamais l'écran de personne.</p>
     */
    private static void pushZoneToChiefs(MinecraftServer server, OWArenaMatch match,
                                         net.minecraft.world.level.border.WorldBorder border) {
        var packet = new net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket(border);
        for (int teamId : new int[]{ match.getTeamAId(), match.getTeamBId() }) {
            ServerPlayer chief = server.getPlayerList().getPlayer(match.chiefOf(teamId));
            if (chief != null) chief.connection.send(packet);
        }
    }

    /**
     * Inflige les dégâts de zone aux <b>créatures</b> restées dehors.
     *
     * <p>Le vanilla ne blesse hors bordure que les {@code Player} (cf. {@code LivingEntity#baseTick}) :
     * sans ce relais, la zone repousserait les combattants sans jamais les sanctionner, et son
     * rétrécissement ne trancherait aucun combat. Les chefs, eux, sont couverts par le vanilla.</p>
     */
    private static void applyZoneDamage(ServerLevel arena, OWArenaMatch match) {
        net.minecraft.world.level.border.WorldBorder border = arena.getWorldBorder();
        double perBlock = border.getDamagePerBlock();
        if (perBlock <= 0.0) return;

        List<UUID> combatants = new ArrayList<>(match.getAliveA());
        combatants.addAll(match.getAliveB());
        for (UUID uuid : combatants) {
            if (!(arena.getEntity(uuid) instanceof LivingEntity le) || !le.isAlive()) continue;
            if (border.isWithinBounds(le.getBoundingBox())) continue;
            double overshoot = border.getDistanceToBorder(le) + border.getDamageSafeZone();
            if (overshoot >= 0.0) continue;
            le.hurt(le.damageSources().outOfBorder(),
                    (float) Math.max(1, net.minecraft.util.Mth.floor(-overshoot * perBlock)));
        }
    }

    /** Rouvre la zone en grand : l'arène redevient neutre entre deux combats. */
    private static void resetBattleZone(MinecraftServer server, ServerLevel arena, OWArenaMatch match) {
        net.minecraft.world.level.border.WorldBorder border = arena.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(net.minecraft.world.level.border.WorldBorder.MAX_SIZE);
        border.setDamagePerBlock(0.2);
        border.setDamageSafeZone(5);
        // Les chefs ont reçu la zone d'arène : on leur rend celle de l'overworld, sans quoi ils
        // garderaient un cercle fantôme une fois rentrés chez eux.
        var overworldBorder = server.overworld().getWorldBorder();
        var packet = new net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket(overworldBorder);
        for (int teamId : new int[]{ match.getTeamAId(), match.getTeamBId() }) {
            ServerPlayer chief = server.getPlayerList().getPlayer(match.chiefOf(teamId));
            if (chief != null) chief.connection.send(packet);
        }
    }

    /**
     * Vrai si l'arène est déjà occupée par un autre combat — y compris pendant le temps de
     * contemplation du verdict, où les combattants sont encore sur place.
     */
    private static boolean arenaBusy(OWArenaMatch except) {
        for (OWArenaMatch m : MATCHES.values()) {
            if (m == except) continue;
            if (m.getState() == OWArenaMatch.State.FIGHTING
                    || m.getState() == OWArenaMatch.State.ENDED) return true;
        }
        return false;
    }

    /**
     * Vide l'arène des objets tombés à terre en fin de combat.
     *
     * <p><b>Sauf les âmes.</b> Une créature apprivoisée qui meurt lâche son âme
     * ({@code OWEntity#shouldDropSoulOnDeath}), seul moyen de la ressusciter : la détruire avec le
     * reste du butin ferait perdre définitivement le compagnon à son propriétaire. Les âmes sont
     * donc rendues à leur propriétaire s'il est connecté, et laissées intactes sinon — mieux vaut
     * un objet oublié dans l'arène qu'un compagnon effacé.</p>
     */
    private static void clearArenaGroundItems(MinecraftServer server, ServerLevel arena) {
        List<Entity> snapshot = new ArrayList<>();
        arena.getAllEntities().forEach(snapshot::add);

        for (Entity e : snapshot) {
            if (!(e instanceof net.minecraft.world.entity.item.ItemEntity item) || !item.isAlive()) continue;
            net.minecraft.world.item.ItemStack stack = item.getItem();

            if (stack.is(net.tiew.operationWild.item.OWItems.ANIMAL_SOUL.get())) {
                UUID owner = net.tiew.operationWild.item.custom.AnimalSoulItem.getSoul(stack).ownerUuid();
                boolean real = owner != null
                        && !net.tiew.operationWild.component.SoulData.NO_UUID.equals(owner);
                ServerPlayer p = real ? server.getPlayerList().getPlayer(owner) : null;

                if (p != null) {
                    if (!p.getInventory().add(stack.copy())) p.drop(stack.copy(), false);
                    p.sendSystemMessage(Component.translatable("owteams.arena.soul_returned")
                            .setStyle(Style.EMPTY.withColor(0x86DBFF)));
                } else if (real) {
                    // Propriétaire hors ligne : mise de côté pour sa prochaine connexion. La laisser
                    // au sol reviendrait à la détruire — l'arène n'est accessible à personne.
                    OWPendingSouls.get(server).stash(owner, stack);
                }
                // Âme sans propriétaire réel (créature du mode entraînement) : rien à conserver.
                item.discard();
                continue;
            }
            item.discard();
        }
    }

    /** Boucle de combat : cadencée par {@link #tick}, ~1 fois par seconde. */
    private static void tickFight(MinecraftServer server, OWArenaMatch match) {
        ServerLevel arena = server.getLevel(OWDimensions.ARENA);
        if (arena == null) { endMatch(server, match, 0, true); return; }

        // La zone se referme après un répit, puis met neuf minutes à atteindre sa taille finale.
        if (!match.isBorderShrinking() && match.elapsedInStateMs() >= OWArena.BORDER_HOLD_MS) {
            match.setBorderShrinking(true);
            arena.getWorldBorder().lerpSizeBetween(
                    OWArena.BORDER_START, OWArena.BORDER_END, OWArena.BORDER_SHRINK_MS);
            // Le lerp doit être renvoyé aux clients, sinon leur bordure resterait figée.
            pushZoneToChiefs(server, match, arena.getWorldBorder());
            broadcastToMatch(server, match, Component.translatable("owteams.arena.zone.shrinking")
                    .setStyle(Style.EMPTY.withColor(0xE8956A)));
        }

        applyZoneDamage(arena, match);
        pruneDead(arena, match.getAliveA());
        pruneDead(arena, match.getAliveB());
        retarget(arena, match);

        boolean aDown = match.getAliveA().isEmpty();
        boolean bDown = match.getAliveB().isEmpty();
        if (aDown || bDown) {
            int winner = aDown && bDown ? 0 : (aDown ? match.getTeamBId() : match.getTeamAId());
            endMatch(server, match, winner, false);
            return;
        }

        // Combat qui s'éternise : le camp qui compte le plus de survivants l'emporte, sinon nul.
        if (match.elapsedInStateMs() >= OWArena.FIGHT_TIMEOUT_MS) {
            int a = match.getAliveA().size(), b = match.getAliveB().size();
            int winner = a == b ? 0 : (a > b ? match.getTeamAId() : match.getTeamBId());
            endMatch(server, match, winner, false);
        }
    }

    private static void pruneDead(ServerLevel arena, java.util.Set<UUID> alive) {
        alive.removeIf(uuid -> {
            Entity e = arena.getEntity(uuid);
            return e == null || !e.isAlive() || (e instanceof LivingEntity le && le.isDeadOrDying());
        });
    }

    /** Redonne une cible aux combattants désœuvrés, pour que le combat ne s'enlise pas. */
    private static void retarget(ServerLevel arena, OWArenaMatch match) {
        assignTargets(arena, match.getAliveA(), match.getAliveB());
        assignTargets(arena, match.getAliveB(), match.getAliveA());
    }

    private static void assignTargets(ServerLevel arena, java.util.Set<UUID> side, java.util.Set<UUID> enemies) {
        if (enemies.isEmpty()) return;
        List<UUID> enemyList = new ArrayList<>(enemies);
        int i = 0;
        for (UUID uuid : side) {
            if (!(arena.getEntity(uuid) instanceof OWEntity owE)) continue;
            LivingEntity current = owE.getTarget();
            if (current != null && current.isAlive() && enemies.contains(current.getUUID())) continue;
            Entity enemy = arena.getEntity(enemyList.get(i++ % enemyList.size()));
            if (enemy instanceof LivingEntity le) owE.setTarget(le);
        }
    }

    // ── Fin de match ─────────────────────────────────────────────────────────────
    /**
     * Clôt le match, applique les gains puis renvoie tout le monde chez soi.
     * {@code winnerTeamId == 0} = match nul ; {@code cancelled} = aucun gain ni message de victoire.
     */
    private static void endMatch(MinecraftServer server, OWArenaMatch match, int winnerTeamId, boolean cancelled) {
        match.setWinnerTeamId(winnerTeamId);
        match.setState(OWArenaMatch.State.ENDED);

        if (!cancelled && winnerTeamId != 0) awardVictory(server, match, winnerTeamId);
        else if (!cancelled) broadcastToMatch(server, match, Component.translatable("owteams.arena.result.draw")
                    .setStyle(Style.EMPTY.withColor(0xBBBBBB)));

        // Le match reste enregistré pendant {@link OWArena#ENDED_LINGER_MS} : on laisse le temps de
        // voir le verdict et le champ de bataille avant d'être renvoyé chez soi. Le renvoi effectif
        // est fait par le tick (cf. tickEnded), pas ici.
        syncMatch(server, match);
        if (cancelled) closeEndedMatch(server, match);   // annulation : aucun verdict à contempler
        else celebrate(server, match, winnerTeamId);
    }

    /**
     * Fin du temps de contemplation : tout le monde rentre et le match est oublié. Séparé de
     * {@link #endMatch} pour que le verdict reste affiché quelques secondes.
     */
    private static void closeEndedMatch(MinecraftServer server, OWArenaMatch match) {
        sendEveryoneHome(server, match);
        // Les chunks forcés doivent être libérés, sinon l'aire de combat reste chargée pour toujours.
        ServerLevel arena = server.getLevel(OWDimensions.ARENA);
        if (arena != null) {
            // Nettoyage AVANT de relâcher les chunks : sinon les objets seraient hors de portée.
            clearArenaGroundItems(server, arena);
            forceArenaChunks(arena, match, match.arenaOffsetX(), false);
            resetBattleZone(server, arena, match);
        }
        MATCHES.remove(match.getTeamAId());
        MATCHES.remove(match.getTeamBId());
        syncMatch(server, match);
    }

    /**
     * Verdict en images : cinématique d'écrasement de bannière chez les deux chefs, puis feu
     * d'artifice dans l'arène pendant le temps de contemplation.
     */
    private static void celebrate(MinecraftServer server, OWArenaMatch match, int winnerTeamId) {
        sendVictoryAnimation(server, match, winnerTeamId);

        ServerLevel arena = server.getLevel(OWDimensions.ARENA);
        if (arena == null) return;
        for (int teamId : new int[]{ match.getTeamAId(), match.getTeamBId() }) {
            ServerPlayer chief = server.getPlayerList().getPlayer(match.chiefOf(teamId));
            if (chief == null || !chief.level().dimension().equals(OWDimensions.ARENA)) continue;
            boolean won = teamId == winnerTeamId;
            arena.sendParticles(
                    won ? net.minecraft.core.particles.ParticleTypes.TOTEM_OF_UNDYING
                        : net.minecraft.core.particles.ParticleTypes.SMOKE,
                    chief.getX(), chief.getY() + 1.4, chief.getZ(), won ? 90 : 30,
                    1.4, 1.0, 1.4, won ? 0.28 : 0.02);
            chief.playNotifySound(
                    won ? net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE
                        : net.minecraft.sounds.SoundEvents.ITEM_BREAK,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, won ? 1.0f : 0.8f);
        }
    }

    private static void awardVictory(MinecraftServer server, OWArenaMatch match, int winnerTeamId) {
        OWTribesSavedData data = OWTribesSavedData.get(server);
        OWTeam winner = data.findTeamById(winnerTeamId);
        OWTeam loser = data.findTeamById(match.opponentOf(winnerTeamId));
        if (winner == null || loser == null) return;

        int winnerRep = match.isSideA(winnerTeamId) ? match.getReputationA() : match.getReputationB();
        int loserRep = match.isSideA(winnerTeamId) ? match.getReputationB() : match.getReputationA();
        int gain = OWArena.reputationGain(winnerRep, loserRep);
        // Réputations figées à l'ouverture du match : le prestige promis avant le duel est
        // exactement celui qui sera versé, même si les scores ont bougé entre-temps.
        int prestige = OWArena.prestigeGain(winnerRep, loserRep);
        int consolation = OWArena.prestigeConsolation(prestige);

        winner.addArenaReputationBonus(gain);
        winner.addArenaPrestige(prestige);
        loser.addArenaPrestige(consolation);
        data.putTribe(winner);
        data.putTribe(loser);

        announce(server, winner, Component.translatable("owteams.arena.result.win",
                loser.getTeamName(), gain, prestige).setStyle(Style.EMPTY.withColor(0x7ddd73)));
        announce(server, loser, Component.translatable("owteams.arena.result.loss",
                winner.getTeamName(), consolation).setStyle(Style.EMPTY.withColor(0xE04444)));

        OWTribeManager.syncTribeToOnlineMembers(server, winner);
        OWTribeManager.syncTribeToOnlineMembers(server, loser);
        OWTribeManager.broadcastTribeList(server);
    }

    private static void announce(MinecraftServer server, OWTeam team, Component message) {
        for (UUID member : team.getPlayerUUIDs()) {
            ServerPlayer p = server.getPlayerList().getPlayer(member);
            if (p != null) p.sendSystemMessage(message);
        }
    }

    /** Renvoie combattants et chefs à leur point de départ (ou au spawn en dernier recours). */
    private static void sendEveryoneHome(MinecraftServer server, OWArenaMatch match) {
        for (Map.Entry<UUID, OWArenaMatch.ReturnPoint> e : match.getReturnPoints().entrySet()) {
            UUID uuid = e.getKey();
            OWArenaMatch.ReturnPoint rp = e.getValue();
            ServerLevel dest = server.getLevel(rp.dimension());
            if (dest == null) dest = server.overworld();

            double y = safeY(dest, rp.x(), rp.z(), rp.y());

            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player != null) {
                player.changeDimension(new DimensionTransition(dest, new Vec3(rp.x(), y, rp.z()),
                        Vec3.ZERO, player.getYRot(), player.getXRot(), DimensionTransition.DO_NOTHING));
                clearRescuePoint(player);
                continue;
            }
            // Entités : seules les survivantes sont encore là ; les mortes n'ont rien à ramener.
            Entity entity = findEntity(server, uuid);
            if (entity != null && entity.isAlive()) {
                Entity back = entity.changeDimension(new DimensionTransition(dest, new Vec3(rp.x(), y, rp.z()),
                        Vec3.ZERO, entity.getYRot(), entity.getXRot(), DimensionTransition.DO_NOTHING));
                // Nouvelle instance, donc currentTeam de nouveau vide : on le rétablit, sinon les
                // permissions de tribu resteraient cassées sur la créature après son retour.
                if (back instanceof OWEntity owE) {
                    owE.setTarget(null);
                    OWTribeManager.refreshEntityTeam(server, owE);
                }
            }
        }
        match.getReturnPoints().clear();

        // Deuxième passe, indépendante des points de retour : toute créature encore présente dans
        // l'arène et appartenant à l'une des deux tribus rentre aussi. Si une créature avait échappé
        // au recensement (téléportation partielle, entité rechargée sous une autre instance), elle
        // ne resterait pas abandonnée sur place — vainqueur comme vaincu.
        ServerLevel arena = server.getLevel(OWDimensions.ARENA);
        if (arena == null) return;
        OWTribesSavedData data = OWTribesSavedData.get(server);
        OWTeam teamA = data.findTeamById(match.getTeamAId());
        OWTeam teamB = data.findTeamById(match.getTeamBId());
        List<Entity> leftovers = new ArrayList<>();
        arena.getAllEntities().forEach(leftovers::add);
        for (Entity e : leftovers) {
            if (!(e instanceof OWEntity owE) || !owE.isAlive()) continue;
            UUID owner = owE.getOwnerUUID();
            if (owner == null) continue;
            boolean ours = (teamA != null && teamA.isMember(owner)) || (teamB != null && teamB.isMember(owner));
            if (ours) returnEntityHome(server, owE);
        }
    }

    // ── Boucle serveur ───────────────────────────────────────────────────────────
    /** À appeler ~1 fois par seconde depuis le tick serveur. */
    public static void tick(MinecraftServer server) {
        rescueStragglers(server);
        if (MATCHES.isEmpty()) return;
        for (OWArenaMatch match : new ArrayList<>(new java.util.LinkedHashSet<>(MATCHES.values()))) {
            switch (match.getState()) {
                case SELECTION -> {
                    // Les deux camps sont prêts et attendaient que l'arène se libère : on démarre.
                    if (match.bothReady()) {
                        if (!arenaBusy(match)) beginFight(server, match);
                        break;   // en attente : surtout pas d'expiration, ils n'y sont pour rien
                    }
                    // Composition qui traîne : on libère les deux tribus plutôt que de les bloquer.
                    if (match.elapsedInStateMs() >= OWArena.SELECTION_TIMEOUT_MS) {
                        broadcastToMatch(server, match, Component.translatable("owteams.arena.selection.timeout")
                                .setStyle(Style.EMPTY.withColor(0xE8956A)));
                        endMatch(server, match, 0, true);
                    }
                }
                case FIGHTING -> tickFight(server, match);
                case ENDED -> {
                    // Temps de contemplation du verdict écoulé : retour à la maison.
                    if (match.elapsedInStateMs() >= OWArena.ENDED_LINGER_MS) closeEndedMatch(server, match);
                }
            }
        }
    }

    /**
     * <b>Invariant de sécurité</b> : personne ne reste dans la dimension d'arène sans match en cours.
     *
     * <p>Vérifié à chaque seconde plutôt qu'uniquement à la connexion. Le renvoi de fin de combat
     * peut échouer pour des raisons qu'on ne maîtrise pas (téléportation refusée, joueur mort au
     * mauvais moment, match retiré par un autre chemin) ; plutôt que de dépendre de la réussite d'un
     * unique appel, on rattrape ici tout joueur resté en arrière. C'est ce qui garantit qu'un
     * vainqueur finit toujours par rentrer chez lui.</p>
     */
    private static void rescueStragglers(MinecraftServer server) {
        ServerLevel arena = server.getLevel(OWDimensions.ARENA);
        if (arena == null) return;

        for (ServerPlayer p : new ArrayList<>(arena.players())) {
            if (matchOfPlayer(server, p) != null) continue;   // combat en cours : sa place est ici
            rescueStrandedPlayer(server, p);
        }

        // Même invariant pour les créatures. On ne s'arrête surtout pas à « plus aucun joueur dans
        // l'arène » : c'est précisément le cas où des animaux oubliés y tourneraient en rond sans
        // que personne ne le voie.
        //
        // Seul un match qui OCCUPE réellement l'arène (combat en cours, ou verdict en cours de
        // contemplation) rend ses créatures légitimes ; un match encore en composition, lui, n'a
        // rien à y faire.
        for (OWArenaMatch m : MATCHES.values()) {
            if (m.getState() == OWArenaMatch.State.FIGHTING
                    || m.getState() == OWArenaMatch.State.ENDED) return;
        }
        List<Entity> snapshot = new ArrayList<>();
        arena.getAllEntities().forEach(snapshot::add);
        for (Entity e : snapshot) {
            if (e instanceof OWEntity owE && owE.isAlive()) returnEntityHome(server, owE);
        }

        // Et les objets au sol : hors combat, l'arène doit être parfaitement vide. Le nettoyage de
        // fin de match s'en charge déjà, mais un match clos par un chemin détourné (tribu dissoute,
        // redémarrage) le contournerait — cette passe garantit l'invariant dans tous les cas.
        clearArenaGroundItems(server, arena);
    }

    // ── Synchronisation client ───────────────────────────────────────────────────
    /** Pousse l'état d'arène à un joueur (aucune tribu / aucun match = état vide). */
    public static void syncTo(MinecraftServer server, ServerPlayer player) {
        OWTeam team = OWTribesSavedData.get(server).findTeamByMember(player.getUUID());
        OWNetworkHandler.sendToClient(SyncArenaStatePacket.of(server, team, player.getUUID()), player);
    }

    /** Pousse l'état aux membres en ligne des deux tribus du match. */
    public static void syncMatch(MinecraftServer server, OWArenaMatch match) {
        OWTribesSavedData data = OWTribesSavedData.get(server);
        for (int teamId : new int[]{ match.getTeamAId(), match.getTeamBId() }) {
            OWTeam team = data.findTeamById(teamId);
            if (team == null) continue;
            for (UUID member : team.getPlayerUUIDs()) {
                ServerPlayer p = server.getPlayerList().getPlayer(member);
                if (p != null) syncTo(server, p);
            }
        }
    }

    private static void broadcastToMatch(MinecraftServer server, OWArenaMatch match, Component message) {
        OWTribesSavedData data = OWTribesSavedData.get(server);
        for (int teamId : new int[]{ match.getTeamAId(), match.getTeamBId() }) {
            OWTeam team = data.findTeamById(teamId);
            if (team != null) announce(server, team, message);
        }
    }

    // ── Utilitaires ──────────────────────────────────────────────────────────────
    /** Retrouve une entité par UUID dans n'importe quel niveau chargé. */
    private static Entity findEntity(MinecraftServer server, UUID uuid) {
        for (ServerLevel level : server.getAllLevels()) {
            Entity e = level.getEntity(uuid);
            if (e != null) return e;
        }
        return null;
    }

    /** Purge tout ce qui concerne une tribu dissoute. */
    public static void onTribeRemoved(MinecraftServer server, int teamId) {
        OWArenaChallenges.clearFor(teamId);
        OWArenaMatch match = MATCHES.get(teamId);
        if (match == null) return;
        // La tribu disparaît dans la foulée : on clôt sans temps de contemplation, sinon le tick
        // travaillerait sur une tribu qui n'existe plus.
        endMatch(server, match, match.opponentOf(teamId), false);
        closeEndedMatch(server, match);
    }

    /** Mémorise dans la créature d'où elle vient, pour pouvoir l'y renvoyer en dernier recours. */
    private static void saveEntityRescuePoint(OWEntity entity) {
        CompoundTag tag = new CompoundTag();
        tag.putString("dim", entity.level().dimension().location().toString());
        tag.putDouble("x", entity.getX());
        tag.putDouble("y", entity.getY());
        tag.putDouble("z", entity.getZ());
        entity.getPersistentData().put(RETURN_KEY, tag);
    }

    /**
     * Renvoie une créature à son point de retour mémorisé. Repli sur le spawn de l'overworld si la
     * donnée manque — mieux vaut une créature mal placée qu'une créature abandonnée dans l'arène.
     */
    private static void returnEntityHome(MinecraftServer server, OWEntity entity) {
        CompoundTag tag = entity.getPersistentData().getCompound(RETURN_KEY);
        ServerLevel dest = null;
        double x = 0, y = 0, z = 0;
        if (tag.contains("dim")) {
            ResourceLocation id = ResourceLocation.tryParse(tag.getString("dim"));
            if (id != null) dest = server.getLevel(ResourceKey.create(Registries.DIMENSION, id));
            x = tag.getDouble("x"); y = tag.getDouble("y"); z = tag.getDouble("z");
        }
        if (dest == null) {
            dest = server.overworld();
            net.minecraft.core.BlockPos spawn = dest.getSharedSpawnPos();
            x = spawn.getX() + 0.5; y = spawn.getY(); z = spawn.getZ() + 0.5;
        }
        entity.getPersistentData().remove(RETURN_KEY);
        entity.setTarget(null);
        y = safeY(dest, x, z, y);
        Entity back = entity.changeDimension(new DimensionTransition(dest, new Vec3(x, y, z),
                Vec3.ZERO, entity.getYRot(), entity.getXRot(), DimensionTransition.DO_NOTHING));
        if (back instanceof OWEntity owE) {
            owE.setTarget(null);
            OWTribeManager.refreshEntityTeam(server, owE);
        }
    }

    // ── Filet de sécurité redémarrage ────────────────────────────────────────────
    private static void saveRescuePoint(ServerPlayer player) {
        CompoundTag tag = new CompoundTag();
        tag.putString("dim", player.level().dimension().location().toString());
        tag.putDouble("x", player.getX());
        tag.putDouble("y", player.getY());
        tag.putDouble("z", player.getZ());
        player.getPersistentData().put(RETURN_KEY, tag);
    }

    private static void clearRescuePoint(ServerPlayer player) {
        player.getPersistentData().remove(RETURN_KEY);
    }

    /**
     * À la connexion : si un joueur réapparaît dans la dimension d'arène sans match en cours (serveur
     * redémarré en plein combat), on le renvoie à son point de départ mémorisé.
     */
    public static void rescueStrandedPlayer(MinecraftServer server, ServerPlayer player) {
        if (!player.level().dimension().equals(OWDimensions.ARENA)) { clearRescuePoint(player); return; }
        if (matchOfPlayer(server, player) != null) return; // combat toujours en cours : rien à faire

        CompoundTag tag = player.getPersistentData().getCompound(RETURN_KEY);
        ServerLevel dest = null;
        double x = 0, y = 0, z = 0;
        if (tag.contains("dim")) {
            ResourceLocation id = ResourceLocation.tryParse(tag.getString("dim"));
            if (id != null) dest = server.getLevel(ResourceKey.create(Registries.DIMENSION, id));
            x = tag.getDouble("x"); y = tag.getDouble("y"); z = tag.getDouble("z");
        }
        if (dest == null) {
            dest = server.overworld();
            net.minecraft.core.BlockPos spawn = dest.getSharedSpawnPos();
            x = spawn.getX() + 0.5; y = spawn.getY(); z = spawn.getZ() + 0.5;
        }
        y = safeY(dest, x, z, y);
        player.changeDimension(new DimensionTransition(dest, new Vec3(x, y, z), Vec3.ZERO,
                player.getYRot(), player.getXRot(), DimensionTransition.DO_NOTHING));
        clearRescuePoint(player);
        player.sendSystemMessage(Component.translatable("owteams.arena.rescued")
                .setStyle(Style.EMPTY.withColor(0xE8956A)));
    }

    /** Vrai si {@code level} est la dimension d'arène (utilisé pour désactiver certaines règles). */
    public static boolean isArena(Level level) {
        return level != null && level.dimension().equals(OWDimensions.ARENA);
    }

    // ── Mode entraînement (test en solo) ─────────────────────────────────────────
    /**
     * Adversaire fictif permettant de dérouler un combat complet <b>sans second joueur</b>.
     *
     * <p>Le chef de cette tribu n'existe pas : c'est un UUID stable, dérivé d'une chaîne fixe, qui
     * ne correspondra jamais à un compte réel. Tout le reste du système fonctionne tel quel — le
     * camp adverse compose son équipe et confirme immédiatement, puis le combat suit le chemin
     * normal (téléportation, ciblage, verdict, récompenses).</p>
     */
    public static final UUID SPARRING_CHIEF =
            UUID.nameUUIDFromBytes("ow_arena_sparring_bot".getBytes(java.nio.charset.StandardCharsets.UTF_8));

    private static final String SPARRING_TRIBE_NAME = "Entraînement";

    /** Espèces couvrant les archétypes distincts actuellement disponibles dans le mod. */
    private static final java.util.function.Supplier<net.minecraft.world.entity.EntityType<?>>[] SPARRING_SPECIES =
            new java.util.function.Supplier[]{
                    () -> net.tiew.operationWild.entity.OWEntityRegistry.TIGER.get(),      // ASSASSIN
                    () -> net.tiew.operationWild.entity.OWEntityRegistry.KANGAROO.get(),   // SCOUT
                    () -> net.tiew.operationWild.entity.OWEntityRegistry.KODIAK.get(),     // BERSERKER
                    () -> net.tiew.operationWild.entity.OWEntityRegistry.CROCODILE.get(),  // MARAUDER
            };

    /**
     * Ouvre un match contre la tribu d'entraînement : crée la tribu fictive au besoin, fait
     * apparaître ses combattants autour du joueur, compose son équipe et la confirme. Il ne reste
     * plus au joueur qu'à choisir la sienne et à lancer le combat.
     */
    public static boolean startSparring(MinecraftServer server, ServerPlayer player, int fighterCount) {
        OWTribesSavedData data = OWTribesSavedData.get(server);
        OWTeam mine = data.findTeamByMember(player.getUUID());
        if (mine == null) {
            player.sendSystemMessage(Component.translatable("owteams.arena.spar.no_tribe")
                    .setStyle(Style.EMPTY.withColor(0xE04444)));
            return false;
        }
        if (!mine.isChief(player.getUUID())) {
            player.sendSystemMessage(Component.translatable("owteams.arena.combat.chief_only")
                    .setStyle(Style.EMPTY.withColor(0xE04444)));
            return false;
        }
        if (!mine.isArenaAccepted()) {
            player.sendSystemMessage(Component.translatable("owteams.arena.spar.not_accepted")
                    .setStyle(Style.EMPTY.withColor(0xE04444)));
            return false;
        }
        if (MATCHES.containsKey(mine.getTeamId())) {
            player.sendSystemMessage(Component.translatable("owteams.arena.spar.already")
                    .setStyle(Style.EMPTY.withColor(0xE8956A)));
            return false;
        }

        OWTeam bot = findOrCreateSparringTribe(server, data);
        spawnSparringFighters(server, player, bot, fighterCount);

        OWReputationData repData = OWReputationData.get(server);
        OWArenaMatch match = new OWArenaMatch(
                NEXT_MATCH_ID.getAndIncrement(),
                bot.getTeamId(), bot.getTeamName(), SPARRING_CHIEF, OWReputation.compute(repData, bot),
                mine.getTeamId(), mine.getTeamName(), mine.getTeamOwnerUUID(), OWReputation.compute(repData, mine));
        MATCHES.put(bot.getTeamId(), match);
        MATCHES.put(mine.getTeamId(), match);

        // Le camp fictif compose et confirme tout de suite : le joueur reste maître du tempo.
        for (OWArenaFighter f : candidatesFor(server, bot)) {
            if (!match.addFighter(bot.getTeamId(), f)) continue;
        }
        if (match.fightersOf(bot.getTeamId()).isEmpty()) {
            MATCHES.remove(bot.getTeamId());
            MATCHES.remove(mine.getTeamId());
            player.sendSystemMessage(Component.translatable("owteams.arena.spar.spawn_failed")
                    .setStyle(Style.EMPTY.withColor(0xE04444)));
            return false;
        }
        match.setReady(bot.getTeamId(), true);

        player.sendSystemMessage(Component.translatable("owteams.arena.spar.ready",
                match.fightersOf(bot.getTeamId()).size()).setStyle(Style.EMPTY.withColor(0x7ddd73)));
        syncMatch(server, match);
        return true;
    }

    private static OWTeam findOrCreateSparringTribe(MinecraftServer server, OWTribesSavedData data) {
        OWTeam existing = data.findTeamByMember(SPARRING_CHIEF);
        if (existing != null) {
            existing.setArenaAccepted(true);
            data.putTribe(existing);
            return existing;
        }
        OWTeam bot = data.createTribe(SPARRING_CHIEF, SPARRING_TRIBE_NAME, SPARRING_TRIBE_NAME,
                0xB04A3A, 0x2A2A30, OWTeamMosaicPattern.GRADIENT_DOWN, OWTeamBannerShape.CLASSIC, null);
        bot.setArenaAccepted(true);
        bot.setPublic(false);   // personne ne doit pouvoir la rejoindre
        data.putTribe(bot);
        return bot;
    }

    /** Fait apparaître des créatures apprivoisées appartenant au chef fictif, autour du joueur. */
    private static void spawnSparringFighters(MinecraftServer server, ServerPlayer player,
                                              OWTeam bot, int count) {
        if (!(player.level() instanceof ServerLevel level)) return;

        // On ne recrée que ce qui manque : relancer la commande ne doit pas inonder le monde.
        int existing = candidatesFor(server, bot).size();
        int missing = Math.min(OWArena.MAX_FIGHTERS, count) - existing;
        if (missing <= 0) return;

        for (int i = existing; i < existing + missing && i < SPARRING_SPECIES.length; i++) {
            net.minecraft.world.entity.EntityType<?> type = SPARRING_SPECIES[i].get();
            if (!(type.create(level) instanceof OWEntity owE)) continue;

            double angle = i * (Math.PI * 2 / SPARRING_SPECIES.length);
            owE.moveTo(player.getX() + Math.cos(angle) * 5, player.getY(), player.getZ() + Math.sin(angle) * 5,
                    player.getYRot(), 0f);
            level.addFreshEntity(owE);

            // On apprivoise avec le joueur (pour tous les effets de bord du taming), puis on
            // bascule la propriété vers le chef fictif.
            owE.setTame(true, player);
            owE.setOwnerUUID(SPARRING_CHIEF);
            owE.setCachedOwnerName(SPARRING_TRIBE_NAME);
            owE.setLevel(20);
            owE.currentTeam = bot;
        }
        OWTribeManager.refreshEntitiesOfPlayer(server, SPARRING_CHIEF);
    }

    /** Met fin à l'entraînement : match annulé, créatures fictives supprimées, tribu dissoute. */
    public static int stopSparring(MinecraftServer server) {
        OWTribesSavedData data = OWTribesSavedData.get(server);
        OWTeam bot = data.findTeamByMember(SPARRING_CHIEF);
        if (bot == null) return 0;

        OWArenaMatch match = MATCHES.get(bot.getTeamId());
        if (match != null) endMatch(server, match, 0, true);

        int removed = 0;
        for (ServerLevel level : server.getAllLevels()) {
            // Copie préalable : on supprime des entités pendant le parcours.
            List<Entity> snapshot = new ArrayList<>();
            level.getAllEntities().forEach(snapshot::add);
            for (Entity e : snapshot) {
                if (e instanceof OWEntity owE && SPARRING_CHIEF.equals(owE.getOwnerUUID())) {
                    owE.discard();
                    removed++;
                }
            }
        }
        OWArenaChallenges.clearFor(bot.getTeamId());
        data.removeTribe(bot.getTeamId());
        OWTribeManager.broadcastTribeList(server);
        return removed;
    }
}
