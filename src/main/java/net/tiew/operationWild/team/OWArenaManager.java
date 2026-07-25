package net.tiew.operationWild.team;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
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

    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    /** Clé du point de retour de secours dans les données persistantes du joueur. */
    private static final String RETURN_KEY = "ow_arena_return";

    /**
     * Tenue de la créature avant le duel — mode passif/agressif et position assise —, mémorisée le
     * temps du match pour lui être rendue une fois rentrée chez elle.
     *
     * <p>L'arène impose sa discipline : debout, réveillée et agressive. Rien de tout cela ne doit
     * lui rester une fois le duel fini. Une créature qu'on avait laissée assise en garde d'un coffre
     * revenait sinon debout, et se remettait à courir après tout ce qui passe.</p>
     */
    private static final String MODE_KEY = "ow_arena_prev_passive";
    private static final String SITTING_KEY = "ow_arena_prev_sitting";

    /** Copie de l'inventaire du chef, prise à son départ pour l'arène et rendue à son retour. */
    private static final String INVENTORY_KEY = "ow_arena_inventory";
    private static final String SELECTED_SLOT_KEY = "ow_arena_selected_slot";

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
    /**
     * Le chef de {@code player} défie la tribu {@code targetTeamId} sur le terrain de son choix.
     *
     * <p>Le terrain fait partie du défi, pas d'une négociation ultérieure : c'est l'avantage du
     * défiant, et l'information que le défié pèse avant d'accepter.</p>
     */
    public static void challenge(MinecraftServer server, ServerPlayer player, int targetTeamId,
                                 OWArena.Terrain terrain) {
        OWTribesSavedData data = OWTribesSavedData.get(server);
        OWTeam mine = data.findTeamByMember(player.getUUID());
        OWTeam target = data.findTeamById(targetTeamId);
        if (mine == null || target == null || mine.getTeamId() == targetTeamId) return;
        if (!mine.isChief(player.getUUID())) return;                       // chef uniquement
        if (!mine.isArenaAccepted() || !target.isArenaAccepted()) return;  // les deux doivent être inscrites
        if (MATCHES.containsKey(mine.getTeamId()) || MATCHES.containsKey(targetTeamId)) return; // déjà en combat

        OWArena.Terrain chosen = terrain != null ? terrain : OWArena.Terrain.TERRESTRIAL;
        OWArenaChallenges.put(targetTeamId, mine.getTeamId(), mine.getTeamName(), player.getUUID(), chosen);

        Component terrainName = Component.translatable(chosen.translationKey());
        ServerPlayer targetChief = server.getPlayerList().getPlayer(target.getTeamOwnerUUID());
        if (targetChief != null) {
            targetChief.sendSystemMessage(Component.translatable("owteams.arena.challenge.received.terrain",
                            mine.getTeamName(), terrainName)
                    .setStyle(Style.EMPTY.withColor(0xE8956A)));
            syncTo(server, targetChief);
        }
        player.sendSystemMessage(Component.translatable("owteams.arena.challenge.sent.terrain",
                        target.getTeamName(), terrainName)
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
                OWReputation.compute(repData, mine),
                challenge.terrain());
        MATCHES.put(challenger.getTeamId(), match);
        MATCHES.put(mine.getTeamId(), match);
        OWArenaChallenges.clearFor(mine.getTeamId());
        OWArenaChallenges.clearFor(challenger.getTeamId());

        for (ServerPlayer p : List.of(player, otherChief)) {
            p.sendSystemMessage(Component.translatable("owteams.arena.match.started.terrain",
                            Component.translatable(match.getTerrain().translationKey()))
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
            // Créature étrangère à l'élément du duel : on le dit, plutôt que d'ignorer le clic en
            // silence — l'écran la grise déjà, mais un client trafiqué doit se heurter au serveur.
            if (!fighter.fits(match.getTerrain())) {
                player.sendSystemMessage(Component.translatable(match.getTerrain().unfitKey(), fighter.name())
                        .setStyle(Style.EMPTY.withColor(0xE04444)));
                return;
            }
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
        // Décor livré avec le mod : posé à la première ouverture de duel, ignoré ensuite.
        net.tiew.operationWild.worldgen.dimension.OWArenaBuilder.ensureBuilt(arena);

        // Le duel reste suspendu le temps de l'animation d'ouverture : tout le monde arrive figé,
        // face à l'adversaire.
        match.startOpening(OWArena.OPENING_FREEZE_MS);

        // Les camps se font face selon l'axe Z : A côté −Z, B côté +Z.
        int fightersA = -OWArena.ARENA_FIGHTER_Z;
        int fightersB = OWArena.ARENA_FIGHTER_Z;
        placeSide(server, arena, match, match.getTeamAId(), fightersA, OWArena.facingOpponent(fightersA));
        placeSide(server, arena, match, match.getTeamBId(), fightersB, OWArena.facingOpponent(fightersB));

        // Les chefs assistent au combat, en retrait derrière leur ligne.
        int chiefA = -OWArena.ARENA_CHIEF_Z;
        int chiefB = OWArena.ARENA_CHIEF_Z;
        placeChief(server, arena, match, match.getChiefA(), chiefA, OWArena.facingOpponent(chiefA));
        placeChief(server, arena, match, match.getChiefB(), chiefB, OWArena.facingOpponent(chiefB));

        // Un camp sans aucun combattant posé traduit un échec de téléportation, jamais une victoire
        // adverse légitime. On annule plutôt que de décerner un verdict absurde.
        if (match.getAliveA().isEmpty() || match.getAliveB().isEmpty()) {
            LOGGER.warn(
                    "[Arène] Démarrage annulé : {} combattant(s) posé(s) côté A sur {}, {} côté B sur {}.",
                    match.getAliveA().size(), match.fightersOf(match.getTeamAId()).size(),
                    match.getAliveB().size(), match.fightersOf(match.getTeamBId()).size());
            broadcastToMatch(server, match, Component.translatable("owteams.arena.error.start_failed")
                    .setStyle(Style.EMPTY.withColor(0xE04444)));
            endMatch(server, match, 0, true);
            return;
        }

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
        int radius = OWArena.ARENA_CHIEF_Z / 16 + 1;
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
                                  int teamId, int lineZ, float yRot) {
        List<OWArenaFighter> fighters = match.fightersOf(teamId);
        for (int i = 0; i < fighters.size(); i++) {
            OWArenaFighter f = fighters.get(i);
            Entity entity = findEntity(server, f.entityUuid());
            if (!(entity instanceof OWEntity owE)) {
                // Créature déchargée entre la composition et le coup d'envoi : elle ne combattra pas.
                LOGGER.warn("[Arène] Combattant introuvable au placement : {} ({})",
                        f.name(), f.entityUuid());
                continue;
            }

            match.getReturnPoints().put(f.entityUuid(), new OWArenaMatch.ReturnPoint(
                    owE.level().dimension(), owE.getX(), owE.getY(), owE.getZ()));
            // Santé d'avant-combat : elle sera rendue telle quelle à la fin du duel.
            match.getPreFightHealth().put(f.entityUuid(), owE.getHealth());
            // Doublé dans les données persistantes de la créature : elles survivent au changement
            // de dimension (qui recrée l'entité) et permettent au balayage de sécurité de la
            // ramener même si le renvoi de fin de match n'a pas abouti.
            saveEntityRescuePoint(owE);

            // Étalés sur l'axe X, centrés sur 0 : une équipe pleine (5) occupe −10, −5, 0, 5, 10.
            int n = fighters.size();
            double spacing = n > 1 ? (2.0 * OWArena.ARENA_FIGHTER_SPREAD) / (n - 1) : 0.0;
            int x = (int) Math.round((i - (n - 1) / 2.0) * spacing);
            Entity moved = owE.changeDimension(new DimensionTransition(
                    arena, new Vec3(x + 0.5, arenaY(arena, x, lineZ), lineZ + 0.5), Vec3.ZERO, yRot, 0f,
                    DimensionTransition.DO_NOTHING));
            if (moved instanceof OWEntity arrived) {
                readyForBattle(arrived, match, teamId);
                match.getSnapshots().put(f.entityUuid(), snapshotOf(arrived));
                // Aucune âme ne tombe dans l'arène : la créature n'est pas perdue, elle rentrera.
                arrived.setCanDropSoul(false);
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
     *
     * <p>Tout le monde entre en <b>mode agressif</b>. Une créature laissée en passif rendrait les
     * coups sans jamais en porter : le camp qui aurait oublié de basculer ses bêtes perdrait le
     * duel avant de l'avoir joué. Le mode d'avant-combat est mémorisé sur la créature et lui est
     * rendu au retour — l'arène ne change rien à la façon dont elle se tient chez elle.</p>
     */
    private static void readyForBattle(OWEntity entity, OWArenaMatch match, int teamId) {
        entity.currentTeam = OWTribesSavedData.get(entity.getServer()).findTeamById(teamId);
        // Tenue d'avant-duel mise de côté avant qu'on ne la remplace par celle du combat.
        entity.getPersistentData().putBoolean(SITTING_KEY, entity.isSitting());
        // Une créature laissée assise à la base arriverait assise et ne se battrait jamais.
        entity.setSitting(false);
        entity.setNap(false);
        entity.setSleeping(false);
        entity.ejectPassengers();
        entity.setTarget(null);
        // Repart vulnérable : un match précédent interrompu a pu la laisser mise hors de combat.
        entity.setInvulnerable(false);
        entity.getPersistentData().putBoolean(MODE_KEY, entity.isPassive());
        entity.setCurrentMode(OWEntity.Mode.Aggressive);
        entity.setPassive(false);
        // Figée le temps de l'animation d'ouverture : elle tient sa place et son cap, face à
        // l'adversaire. L'intelligence lui est rendue par unfreezeFighters au coup de gong.
        freeze(entity, true);
    }

    /**
     * Immobilise (ou relâche) un combattant pendant la phase d'ouverture.
     *
     * <p>Couper l'intelligence suffit à le clouer sur place — plus de navigation, plus de goals, donc
     * plus de pas ni de coups — tout en laissant la gravité et le rendu faire leur travail : la
     * créature reste posée au sol et continue de respirer à l'écran.</p>
     */
    private static void freeze(OWEntity entity, boolean frozen) {
        entity.setNoAi(frozen);
        if (frozen) {
            entity.setTarget(null);
            entity.getNavigation().stop();
            entity.setDeltaMovement(Vec3.ZERO);
        }
    }

    /** Rend l'initiative aux deux camps : l'animation d'ouverture est finie, le duel commence. */
    private static void unfreezeFighters(ServerLevel arena, OWArenaMatch match) {
        match.endOpening();
        for (UUID uuid : allCombatants(match)) {
            if (arena.getEntity(uuid) instanceof OWEntity fighter) freeze(fighter, false);
        }
    }

    /**
     * Fige les combattants encore debout, en les rendant au besoin intouchables.
     *
     * <p>Sert aux deux bouts du duel : avant qu'il ne commence, et une fois qu'il est joué. Dans les
     * deux cas la règle est la même — ce qui se passe à l'écran n'est pas du combat, donc rien ne
     * doit s'y jouer.</p>
     */
    private static void holdCombatants(MinecraftServer server, OWArenaMatch match, boolean invulnerable) {
        ServerLevel arena = server.getLevel(OWDimensions.ARENA);
        if (arena == null) return;
        for (UUID uuid : allCombatants(match)) {
            if (!(arena.getEntity(uuid) instanceof OWEntity fighter) || !fighter.isAlive()) continue;
            freeze(fighter, true);
            if (invulnerable) fighter.setInvulnerable(true);
        }
    }

    /**
     * Rend à une créature la tenue qu'elle avait avant d'entrer dans l'arène : son mode
     * passif/agressif et sa position assise. Sans marque mémorisée, on ne touche à rien : mieux vaut
     * ne rien changer que deviner.
     *
     * <p>{@code consume} distingue les deux moments où l'on passe ici. Avant la téléportation, on
     * applique sans effacer : {@code changeDimension} <b>recrée</b> l'entité, et il faut pouvoir
     * rejouer la restauration sur la nouvelle instance — c'est elle qui rentre à la maison. Le
     * souvenir n'est effacé qu'une fois arrivée.</p>
     */
    private static void restorePreFightMode(OWEntity entity, boolean consume) {
        CompoundTag data = entity.getPersistentData();
        if (data.contains(MODE_KEY)) {
            boolean wasPassive = data.getBoolean(MODE_KEY);
            entity.setPassive(wasPassive);
            entity.setCurrentMode(wasPassive ? OWEntity.Mode.Passive : OWEntity.Mode.Aggressive);
            if (consume) data.remove(MODE_KEY);
        }
        if (data.contains(SITTING_KEY)) {
            entity.setSitting(data.getBoolean(SITTING_KEY));
            if (consume) data.remove(SITTING_KEY);
        }
    }

    private static void placeChief(MinecraftServer server, ServerLevel arena, OWArenaMatch match,
                                   UUID chief, int z, float yRot) {
        ServerPlayer p = server.getPlayerList().getPlayer(chief);
        if (p == null) return;
        match.getReturnPoints().put(chief, new OWArenaMatch.ReturnPoint(
                p.level().dimension(), p.getX(), p.getY(), p.getZ()));
        saveRescuePoint(p);
        saveInventory(p);
        // Descendre de monture avant le voyage : un joueur téléporté à cheval sur une créature
        // qui part ailleurs se retrouve dans un état incohérent.
        p.stopRiding();
        // Plain-pied imposé (y=64) : les camps sont sur l'axe Z au niveau du sol de l'aire, et non
        // sur les gradins. Laisser la recherche de hauteur libre grimperait sur la première marche.
        p.changeDimension(new DimensionTransition(
                arena, new Vec3(0.5, OWArena.ARENA_Y, z + 0.5), Vec3.ZERO, yRot, 0f,
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

        // Animation d'ouverture : le duel est suspendu. Rien ne se cible, rien ne se tranche, la
        // zone ne bouge pas — sans quoi un camp encaisserait des coups pendant que son chef regarde
        // encore les bannières s'entrechoquer.
        if (match.isOpening()) return;
        // Premier passage après l'animation : chacun retrouve son initiative.
        if (match.hasPendingOpening()) unfreezeFighters(arena, match);

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
        pruneDead(arena, match, match.getAliveA());
        pruneDead(arena, match, match.getAliveB());
        retarget(arena, match);

        // Répit initial : le temps que tout le monde soit indexé dans la dimension. Aucun verdict
        // ne peut tomber avant, sans quoi un camp encore en cours d'arrivée perdrait d'office.
        if (match.elapsedInStateMs() < OWArena.FIGHT_GRACE_MS) return;

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

    /**
     * Retire les combattants réellement morts.
     *
     * <p>Une entité <b>introuvable</b> n'est pas comptée comme morte : juste après un changement de
     * dimension, elle n'est pas encore dans l'index du niveau — le chargement des chunks est
     * asynchrone et sa section doit d'abord devenir visible. C'est ce raccourci qui faisait perdre
     * un camp entier dès le coup d'envoi. Il faut désormais {@link OWArena#MISSING_TOLERANCE}
     * relevés consécutifs sans la voir ; une mort constatée sur l'entité, elle, compte tout de suite.</p>
     */
    private static void pruneDead(ServerLevel arena, OWArenaMatch match, java.util.Set<UUID> alive) {
        alive.removeIf(uuid -> {
            Entity e = arena.getEntity(uuid);
            if (e == null) {
                int streak = match.getMissingStreak().merge(uuid, 1, Integer::sum);
                return streak >= OWArena.MISSING_TOLERANCE;
            }
            match.getMissingStreak().remove(uuid);
            return !e.isAlive() || (e instanceof LivingEntity le && le.isDeadOrDying());
        });
    }

    /** Redonne une cible aux combattants désœuvrés, pour que le combat ne s'enlise pas. */
    private static void retarget(ServerLevel arena, OWArenaMatch match) {
        assignTargets(arena, match.getAliveA(), match.getAliveB());
        assignTargets(arena, match.getAliveB(), match.getAliveA());
    }

    /**
     * Redonne une cible aux combattants qui n'en ont plus — mais seulement à l'<b>ennemi le plus
     * proche encore à portée de détection</b>, la même {@code FOLLOW_RANGE} qui régit le repérage
     * dans le monde normal.
     *
     * <p>Auparavant chaque bête se voyait attribuer un adversaire quelle que soit la distance : au
     * coup de gong, tout le monde fonçait en ligne droite d'un bout à l'autre de l'aire, ce qu'aucune
     * créature ne fait dehors. En bornant l'attribution à la portée de suivi, l'arène repère comme
     * l'overworld : les camps se rapprochent (poussés par la zone qui se referme) avant de
     * s'engager, et une bête n'attaque que ce qu'elle pourrait voir n'importe où ailleurs.</p>
     */
    private static void assignTargets(ServerLevel arena, java.util.Set<UUID> side, java.util.Set<UUID> enemies) {
        if (enemies.isEmpty()) return;
        for (UUID uuid : side) {
            if (!(arena.getEntity(uuid) instanceof OWEntity owE)) continue;
            LivingEntity current = owE.getTarget();
            if (current != null && current.isAlive() && enemies.contains(current.getUUID())) continue;

            double range = owE.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE);
            double bestSq = range * range;
            LivingEntity best = null;
            for (UUID enemyId : enemies) {
                if (!(arena.getEntity(enemyId) instanceof LivingEntity le) || !le.isAlive()) continue;
                double d = owE.distanceToSqr(le);
                if (d <= bestSq) { bestSq = d; best = le; }
            }
            if (best != null) owE.setTarget(best);
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
        // Le verdict est tombé : plus personne ne se bat. Les survivants sont figés et rendus
        // intouchables jusqu'à leur renvoi, comme au coup d'envoi — un vainqueur n'a plus rien à
        // gagner en achevant ce qui reste debout, et il serait absurde d'y laisser une bête après
        // avoir gagné. L'invulnérabilité est levée au retour par restoreFighter.
        holdCombatants(server, match, true);

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
                restoreInventory(player);
                continue;
            }
            // Créature tombée pendant le duel : elle est bien morte dans l'arène, on la recrée
            // chez elle telle qu'elle y était entrée.
            Entity entity = findEntity(server, uuid);
            if (entity == null || !entity.isAlive()) {
                reviveFighter(server, match, uuid, dest, rp.x(), y, rp.z());
                continue;
            }

            if (entity instanceof OWEntity fighter) restoreFighter(match, fighter, false);
            Entity back = entity.changeDimension(new DimensionTransition(dest, new Vec3(rp.x(), y, rp.z()),
                    Vec3.ZERO, entity.getYRot(), entity.getXRot(), DimensionTransition.DO_NOTHING));
            // Nouvelle instance, donc currentTeam de nouveau vide : on le rétablit, sinon les
            // permissions de tribu resteraient cassées sur la créature après son retour.
            if (back instanceof OWEntity owE) {
                owE.setTarget(null);
                // Rejoué sur la nouvelle instance : la santé se recopie bien au changement de
                // dimension, mais c'est la promesse centrale du duel — on ne la laisse pas
                // dépendre d'un détail d'implémentation du moteur.
                restoreFighter(match, owE, true);   // arrivee chez elle : la tenue d'avant-duel est rendue pour de bon
                OWTribeManager.refreshEntityTeam(server, owE);
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
    /**
     * À appeler à <b>chaque</b> tick serveur : la phase d'ouverture se compte en fractions de
     * seconde, et la cadence d'une fois par seconde de {@link #tick} rendrait aussi bien le
     * relâchement tardif que le maintien approximatif.
     */
    public static void tickOpenings(MinecraftServer server) {
        if (MATCHES.isEmpty()) return;
        ServerLevel arena = server.getLevel(OWDimensions.ARENA);
        if (arena == null) return;
        for (OWArenaMatch match : new java.util.LinkedHashSet<>(MATCHES.values())) {
            switch (match.getState()) {
                case FIGHTING -> {
                    if (match.isOpening()) holdStill(server, arena, match);
                    else if (match.hasPendingOpening()) unfreezeFighters(arena, match);
                }
                // Verdict rendu : tout le monde reste en place jusqu'au renvoi, comme au coup d'envoi.
                case ENDED -> holdStill(server, arena, match);
                default -> { }
            }
        }
    }

    /**
     * Cloue tout le monde sur place pendant l'animation d'ouverture.
     *
     * <p>Les créatures sont déjà privées d'intelligence ; on leur reprend tout de même leur élan
     * horizontal, au cas où elles arriveraient lancées. Les chefs, eux, sont retenus ici <b>en plus</b>
     * du verrouillage des commandes côté client : celui-ci suffit en temps normal, mais il vit sur une
     * machine qu'on ne contrôle pas — le serveur reste le seul juge de qui bouge et de quand.</p>
     */
    private static void holdStill(MinecraftServer server, ServerLevel arena, OWArenaMatch match) {
        for (UUID uuid : allCombatants(match)) {
            if (arena.getEntity(uuid) instanceof OWEntity fighter) {
                Vec3 m = fighter.getDeltaMovement();
                fighter.setDeltaMovement(0, Math.min(m.y, 0), 0);   // la gravité seule est laissée passer
            }
        }
        for (int teamId : new int[]{ match.getTeamAId(), match.getTeamBId() }) {
            ServerPlayer chief = server.getPlayerList().getPlayer(match.chiefOf(teamId));
            if (chief == null || !chief.level().dimension().equals(OWDimensions.ARENA)) continue;
            Vec3 m = chief.getDeltaMovement();
            chief.setDeltaMovement(0, Math.min(m.y, 0), 0);
            chief.hurtMarked = true;   // sans quoi le client garderait sa propre vitesse
        }
    }

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

        // Un bâtisseur en créatif (ou un spectateur) travaille sur le décor : on le laisse. Même
        // échappatoire que pour la protection des blocs — le changement de mode de jeu vaut geste
        // explicite. Sans cela, l'arène était strictement invisitable : le balayage renvoyait chez
        // lui quiconque y arrivait, dans le tick suivant.
        boolean builderPresent = false;
        for (ServerPlayer p : new ArrayList<>(arena.players())) {
            if (p.isCreative() || p.isSpectator()) { builderPresent = true; continue; }
            if (matchOfPlayer(server, p) != null) continue;   // combat en cours : sa place est ici
            rescueStrandedPlayer(server, p);
        }
        // Tant qu'on y travaille, rien n'est balayé : une créature amenée pour un essai de cadrage
        // doit pouvoir rester, et les objets posés au sol aussi.
        if (builderPresent) return;

        // Intrus : tout ce qui n'est ni un combattant, ni un chef, ni un morceau de combattant est
        // retiré, y compris PENDANT un duel. Le filtre d'entrée (cf. OWArenaProtection) empêche les
        // nouveaux venus, mais pas ceux qui traînaient déjà là — et le décompte des survivants n'a
        // de sens que si rien d'autre ne bouge sur le terrain.
        List<Entity> intruders = new ArrayList<>();
        arena.getAllEntities().forEach(e -> {
            if (e instanceof ServerPlayer) return;
            if (e instanceof OWEntity) return;                     // combattants : traités plus bas
            if (e instanceof net.tiew.operationWild.entity.animals.terrestrial.BoaTailPart) return;
            if (e instanceof net.tiew.operationWild.entity.animals.aquatic.CrocodileTailPart) return;
            if (e instanceof net.minecraft.world.entity.projectile.Projectile) return;   // une attaque en vol
            intruders.add(e);
        });
        for (Entity e : intruders) e.discard();

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

    /**
     * Copie intégrale d'une créature, prête à être rejouée telle quelle.
     *
     * <p>L'identifiant de type est ajouté à la main : {@code saveWithoutId} ne l'écrit pas, et il
     * est indispensable pour reconstruire l'entité. L'UUID, lui, fait partie de la copie — la
     * créature ressuscitée est donc la <b>même</b> aux yeux de la tribu, du registre des champions
     * et de son propriétaire, et non un sosie.</p>
     */
    private static CompoundTag snapshotOf(OWEntity entity) {
        CompoundTag tag = entity.saveWithoutId(new CompoundTag());
        ResourceLocation typeId = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
                .getKey(entity.getType());
        if (typeId != null) tag.putString("id", typeId.toString());
        return tag;
    }

    /**
     * Recrée un combattant tombé pendant le duel, à son point de départ et dans l'état où il est
     * entré dans l'arène.
     *
     * <p>Sans instantané — cas d'un serveur redémarré en plein combat — il n'y a rien à faire :
     * mieux vaut une créature perdue qu'une créature inventée.</p>
     */
    private static void reviveFighter(MinecraftServer server, OWArenaMatch match, UUID uuid,
                                      ServerLevel dest, double x, double y, double z) {
        CompoundTag tag = match.getSnapshots().get(uuid);
        if (tag == null) {
            LOGGER.warn("[Arène] Aucun instantané pour {} : impossible de la ramener.", uuid);
            return;
        }
        Entity revived = EntityType.loadEntityRecursive(tag, dest, e -> {
            e.moveTo(x, y, z, e.getYRot(), e.getXRot());
            return e;
        });
        if (!(revived instanceof OWEntity owE)) return;

        owE.setCanDropSoul(true);
        owE.setDeltaMovement(Vec3.ZERO);
        restoreFighter(match, owE, true);   // recreee directement chez elle : rien de plus a voyager
        dest.addFreshEntity(owE);
        OWTribeManager.refreshEntityTeam(server, owE);
    }

    /**
     * Rend à un combattant son état d'avant-duel : santé initiale, invulnérabilité levée, plus
     * aucune cible. Le combat n'aura donc laissé aucune trace sur la créature.
     */
    private static void restoreFighter(OWArenaMatch match, OWEntity entity, boolean home) {
        Float before = match.getPreFightHealth().get(entity.getUUID());
        // Une créature ne rentre jamais chez elle privée d'intelligence : un duel interrompu pendant
        // l'animation d'ouverture la laisserait sinon inerte pour toujours.
        freeze(entity, false);
        entity.setInvulnerable(false);
        entity.setCanDropSoul(true); // coupée à l'entrée dans l'arène, rendue à la sortie
        entity.setTarget(null);
        entity.setLastHurtByMob(null);
        restorePreFightMode(entity, home);
        if (before != null) entity.setHealth(Math.min(before, entity.getMaxHealth()));
        else entity.setHealth(entity.getMaxHealth());
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
        // Rapatriement de secours : la créature a pu être sauvegardée figée, l'arrêt du serveur ayant
        // coupé le duel en pleine animation d'ouverture. On lui rend son intelligence quoi qu'il arrive.
        freeze(entity, false);
        restorePreFightMode(entity, false);
        y = safeY(dest, x, z, y);
        Entity back = entity.changeDimension(new DimensionTransition(dest, new Vec3(x, y, z),
                Vec3.ZERO, entity.getYRot(), entity.getXRot(), DimensionTransition.DO_NOTHING));
        if (back instanceof OWEntity owE) {
            owE.setTarget(null);
            // Rapatriement de secours : la santé d'origine n'est plus connue (le match a pu
            // disparaître avec un redémarrage), on rend donc la créature intacte.
            owE.setInvulnerable(false);
            owE.setHealth(owE.getMaxHealth());
            freeze(owE, false);
            restorePreFightMode(owE, true);   // rentrée : la tenue d'avant-duel est rendue pour de bon
            OWTribeManager.refreshEntityTeam(server, owE);
        }
    }

    // ── Affaires du chef ─────────────────────────────────────────────────────────
    /**
     * Met les affaires du chef de côté avant son départ pour l'arène.
     *
     * <p>Les créatures ont leur instantané, pas lui : une mort par une voie que l'invulnérabilité
     * ne couvre pas (commande, vide, redémarrage en plein duel) lui faisait perdre son équipement
     * pour de bon, ses affaires tombant dans une dimension qui se vide à la fin du match. La copie
     * est rangée dans le sous-registre <b>persistant</b> du joueur, le seul qui survive à une mort
     * et à une réapparition.</p>
     */
    private static void saveInventory(ServerPlayer player) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.put(INVENTORY_KEY, player.getInventory().save(new ListTag()));
        persisted.putInt(SELECTED_SLOT_KEY, player.getInventory().selected);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }

    /**
     * Rend au chef les affaires qu'il avait en partant, puis oublie la copie.
     *
     * <p>Le remplacement est <b>intégral</b>, à l'image de la santé rendue aux créatures : rien ne
     * s'acquiert dans l'arène — le butin passe par les coffres de prestige, hors du duel — et le sol
     * y est balayé à la fermeture. Rendre exactement l'inventaire de départ est donc à la fois la
     * garantie la plus forte et la seule qui interdise de rapporter quoi que ce soit du champ de
     * bataille.</p>
     */
    private static void restoreInventory(ServerPlayer player) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (!persisted.contains(INVENTORY_KEY)) return;
        ListTag saved = persisted.getList(INVENTORY_KEY, Tag.TAG_COMPOUND);
        int selected = persisted.getInt(SELECTED_SLOT_KEY);
        persisted.remove(INVENTORY_KEY);
        persisted.remove(SELECTED_SLOT_KEY);

        player.getInventory().load(saved);
        if (selected >= 0 && selected < Inventory.getSelectionSize()) {
            player.getInventory().selected = selected;
        }
        player.inventoryMenu.broadcastChanges();
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
        if (!player.level().dimension().equals(OWDimensions.ARENA)) {
            clearRescuePoint(player);
            // Sorti de l'arène par une autre porte (mort et réapparition au lit, redémarrage) :
            // ses affaires lui reviennent quand même, tant qu'aucun match ne les attend encore.
            if (matchOfPlayer(server, player) == null) restoreInventory(player);
            return;
        }
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
        restoreInventory(player);
        player.sendSystemMessage(Component.translatable("owteams.arena.rescued")
                .setStyle(Style.EMPTY.withColor(0xE8956A)));
    }

    /**
     * Enregistre la chute d'un combattant. La créature <b>meurt réellement</b> dans l'arène ; elle
     * sera recréée à partir de son instantané au moment du renvoi.
     *
     * <p>Appelé depuis l'événement de mort plutôt que laissé au balayage de {@code pruneDead} : le
     * verdict tombe ainsi dans le tick même de la chute, sans le battement d'un tour de boucle.</p>
     */
    public static void knockOut(OWArenaMatch match, OWEntity entity) {
        UUID id = entity.getUUID();
        match.getAliveA().remove(id);
        match.getAliveB().remove(id);
        match.getDefeated().add(id);

        // Les adversaires doivent cesser de viser un combattant déjà hors jeu.
        if (entity.level() instanceof ServerLevel level) {
            for (UUID other : allCombatants(match)) {
                if (level.getEntity(other) instanceof OWEntity o && o.getTarget() == entity) o.setTarget(null);
            }
        }
    }

    /** Tous les combattants du match, vivants ou mis hors de combat. */
    private static List<UUID> allCombatants(OWArenaMatch match) {
        List<UUID> all = new ArrayList<>(match.getAliveA());
        all.addAll(match.getAliveB());
        all.addAll(match.getDefeated());
        return all;
    }

    /** Match en cours dont {@code entityUuid} est un combattant, ou {@code null}. */
    public static OWArenaMatch matchOfCombatant(UUID entityUuid) {
        for (OWArenaMatch m : MATCHES.values()) {
            if (m.getState() == OWArenaMatch.State.FIGHTING && m.isCombatant(entityUuid)) return m;
        }
        return null;
    }

    /**
     * Vrai si {@code playerUuid} est un chef présent dans l'arène pour un match, du coup d'envoi
     * jusqu'à son renvoi chez lui.
     *
     * <p>La protection ne s'arrête <b>pas</b> au verdict. Pendant le temps de contemplation, le
     * vaincu reste au milieu du champ de bataille avec les créatures survivantes du vainqueur, qui
     * n'ont aucune raison de s'arrêter : il se faisait rouer de coups en attendant sa
     * téléportation. Le match est joué, plus personne n'a rien à y perdre.</p>
     */
    public static boolean isSpectatingChief(UUID playerUuid) {
        for (OWArenaMatch m : MATCHES.values()) {
            OWArenaMatch.State state = m.getState();
            if (state != OWArenaMatch.State.FIGHTING && state != OWArenaMatch.State.ENDED) continue;
            if (playerUuid.equals(m.getChiefA()) || playerUuid.equals(m.getChiefB())) return true;
        }
        return false;
    }

    /** Match en cours (combat ou verdict) où {@code playerUuid} est un chef, ou {@code null}. */
    public static OWArenaMatch matchOfChief(UUID playerUuid) {
        for (OWArenaMatch m : MATCHES.values()) {
            OWArenaMatch.State state = m.getState();
            if (state != OWArenaMatch.State.FIGHTING && state != OWArenaMatch.State.ENDED) continue;
            if (playerUuid.equals(m.getChiefA()) || playerUuid.equals(m.getChiefB())) return m;
        }
        return null;
    }

    /**
     * L'owner d'un camp est tombé : son camp perd sur-le-champ, quelles que soient les créatures qu'il
     * lui reste sur le terrain.
     *
     * <p>Appelé à la place de la mort réelle du chef : celle-ci le ferait réapparaître au spawn du
     * monde, hors de portée du renvoi de fin de match. On tranche donc le duel proprement, et le
     * renvoi habituel le ramène chez lui avec son inventaire.</p>
     */
    public static void chiefDefeated(MinecraftServer server, OWArenaMatch match, UUID chiefUuid) {
        if (match.getState() != OWArenaMatch.State.FIGHTING) return;
        int loserTeam = chiefUuid.equals(match.getChiefA()) ? match.getTeamAId()
                : chiefUuid.equals(match.getChiefB()) ? match.getTeamBId() : 0;
        if (loserTeam == 0) return;
        broadcastToMatch(server, match, Component.translatable("owteams.arena.chief_fell")
                .setStyle(Style.EMPTY.withColor(0xE04444)));
        endMatch(server, match, match.opponentOf(loserTeam), false);
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

    /**
     * Espèces piochées pour l'adversaire fictif, par ordre de préférence.
     *
     * <p>La liste couvre les deux terrains : le tri se fait au moment de faire apparaître les
     * créatures, en écartant celles que le terrain du duel exclut et celles dont l'archétype est
     * déjà pris. Un duel aquatique compose donc autour de l'orque et du crocodile, un duel terrestre
     * autour du tigre et du kodiak, sans qu'il y ait deux listes à tenir à jour.</p>
     */
    private static final java.util.function.Supplier<net.minecraft.world.entity.EntityType<?>>[] SPARRING_SPECIES =
            new java.util.function.Supplier[]{
                    () -> net.tiew.operationWild.entity.OWEntityRegistry.TIGER.get(),      // ASSASSIN, terre
                    () -> net.tiew.operationWild.entity.OWEntityRegistry.KANGAROO.get(),   // SCOUT
                    () -> net.tiew.operationWild.entity.OWEntityRegistry.KODIAK.get(),     // BERSERKER, terre
                    () -> net.tiew.operationWild.entity.OWEntityRegistry.CROCODILE.get(),  // MARAUDER
                    () -> net.tiew.operationWild.entity.OWEntityRegistry.ORCA.get(),       // BERSERKER, eau
                    () -> net.tiew.operationWild.entity.OWEntityRegistry.BOA.get(),        // MARAUDER
            };

    /**
     * Ouvre un match contre la tribu d'entraînement : crée la tribu fictive au besoin, fait
     * apparaître ses combattants autour du joueur, compose son équipe et la confirme. Il ne reste
     * plus au joueur qu'à choisir la sienne et à lancer le combat.
     */
    public static boolean startSparring(MinecraftServer server, ServerPlayer player, int fighterCount,
                                        OWArena.Terrain terrain) {
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

        OWArena.Terrain ground = terrain != null ? terrain : OWArena.Terrain.TERRESTRIAL;
        OWTeam bot = findOrCreateSparringTribe(server, data);
        spawnSparringFighters(server, player, bot, fighterCount, ground);

        OWReputationData repData = OWReputationData.get(server);
        OWArenaMatch match = new OWArenaMatch(
                NEXT_MATCH_ID.getAndIncrement(),
                bot.getTeamId(), bot.getTeamName(), SPARRING_CHIEF, OWReputation.compute(repData, bot),
                mine.getTeamId(), mine.getTeamName(), mine.getTeamOwnerUUID(), OWReputation.compute(repData, mine),
                ground);
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
                                              OWTeam bot, int count, OWArena.Terrain terrain) {
        if (!(player.level() instanceof ServerLevel level)) return;

        // On ne recrée que ce qui manque : relancer la commande ne doit pas inonder le monde.
        List<OWArenaFighter> already = candidatesFor(server, bot);
        int missing = Math.min(OWArena.MAX_FIGHTERS, count) - already.size();
        if (missing <= 0) return;

        // Archétypes déjà couverts : en faire apparaître un deuxième serait peine perdue, la règle
        // d'unicité le refuserait à l'engagement.
        java.util.Set<Integer> taken = new java.util.HashSet<>();
        for (OWArenaFighter f : already) taken.add(f.archetypeOrdinal());

        int made = 0;
        for (int i = 0; i < SPARRING_SPECIES.length && made < missing; i++) {
            net.minecraft.world.entity.EntityType<?> type = SPARRING_SPECIES[i].get();
            if (!(type.create(level) instanceof OWEntity owE)) continue;

            // Espèce étrangère au terrain, ou archétype déjà pourvu : on la remballe sans la poser.
            int archetype = OWArenaFighter.archetypeOrdinalOf(owE);
            if (!OWArena.fitsTerrain(owE.arenaTerrainMask(), terrain) || !taken.add(archetype)) {
                owE.discard();
                continue;
            }
            made++;

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
