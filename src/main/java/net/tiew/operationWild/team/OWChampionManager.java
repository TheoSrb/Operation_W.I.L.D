package net.tiew.operationWild.team;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.tiew.operationWild.core.OWChampions;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.SyncChampionsPacket;
import net.tiew.operationWild.networking.packets.to_client.SyncOWTeamPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Gère les <b>champions</b> d'une tribu : les créatures qui la représentent et portent son étendard.
 *
 * <p>Le chef seul les nomme. C'est une distinction, pas un rôle de combat : les champions n'ont
 * aucun privilège en arène, et un combattant d'arène n'est pas champion pour autant. Les deux
 * listes sont volontairement indépendantes — on choisit ses champions pour l'allure, ses
 * combattants pour l'efficacité.</p>
 */
public final class OWChampionManager {

    private OWChampionManager() {}

    /**
     * Toutes les créatures apprivoisées de la tribu, chargées ou non, comme candidates.
     *
     * <p>Le balayage porte sur les niveaux chargés : une créature dans un chunk endormi n'apparaît
     * pas. Elle reste championne si elle l'était déjà — la liste enregistrée fait foi, ce parcours
     * ne sert qu'à proposer de nouveaux noms.</p>
     */
    public static List<OWArenaFighter> candidatesFor(MinecraftServer server, OWTeam team) {
        List<OWArenaFighter> out = new ArrayList<>();
        if (server == null || team == null) return out;
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity e : level.getAllEntities()) {
                if (!(e instanceof OWEntity owE) || !owE.isTame() || owE.isDeadOrDying()) continue;
                UUID owner = owE.getOwnerUUID();
                if (owner == null || !team.isMember(owner)) continue;
                OWArenaFighter snapshot = OWArenaFighter.of(owE, ownerNameOf(team, owner));
                if (snapshot != null) out.add(snapshot);
            }
        }
        return out;
    }

    private static String ownerNameOf(OWTeam team, UUID owner) {
        int idx = team.getPlayerUUIDs().indexOf(owner);
        return idx >= 0 && idx < team.getPlayerNames().size()
                ? team.getPlayerNames().get(idx) : owner.toString().substring(0, 8);
    }

    /** Nomme ou révoque un champion. Sans effet si le demandeur n'est pas chef. */
    public static void select(ServerPlayer player, UUID entityUuid, boolean add) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        OWTribesSavedData data = OWTribesSavedData.get(server);
        OWTeam team = data.findTeamByMember(player.getUUID());
        if (team == null || !team.isChief(player.getUUID())) return;

        List<UUID> champions = new ArrayList<>(team.getChampionUUIDs());
        if (add) {
            if (champions.contains(entityUuid)) return;
            if (champions.size() >= OWChampions.MAX_CHAMPIONS) {
                player.sendSystemMessage(Component.translatable("owteams.champions.full", OWChampions.MAX_CHAMPIONS)
                        .setStyle(Style.EMPTY.withColor(0xE8956A)));
                return;
            }
            // Revalidation : le client peut envoyer n'importe quel UUID, y compris celui d'une
            // créature étrangère à la tribu.
            boolean eligible = false;
            for (OWArenaFighter c : candidatesFor(server, team)) {
                if (c.entityUuid().equals(entityUuid)) { eligible = true; break; }
            }
            if (!eligible) return;
            champions.add(entityUuid);
        } else if (!champions.remove(entityUuid)) {
            return; // rien n'a changé : inutile de resynchroniser
        }

        team.setChampionUUIDs(champions);
        data.putTribe(team);
        // Deux synchronisations distinctes : l'une porte l'écran du chef, l'autre le rendu des
        // drapeaux chez tous les joueurs — un étendard doit apparaître et disparaître pour tout le
        // monde, pas seulement pour la tribu concernée.
        SyncOWTeamPacket.resyncTeam(server, team);
        syncToTribe(server, team);
    }

    /** Pousse l'écran des champions à un joueur. */
    public static void syncTo(MinecraftServer server, ServerPlayer player) {
        if (server == null || player == null) return;
        OWTeam team = OWTribesSavedData.get(server).findTeamByMember(player.getUUID());
        if (team == null) {
            OWNetworkHandler.sendToClient(SyncChampionsPacket.empty(), player);
            return;
        }
        OWNetworkHandler.sendToClient(SyncChampionsPacket.of(candidatesFor(server, team), team), player);
    }

    /** Pousse l'écran des champions à tous les membres en ligne de la tribu. */
    public static void syncToTribe(MinecraftServer server, OWTeam team) {
        if (server == null || team == null) return;
        for (UUID member : team.getPlayerUUIDs()) {
            ServerPlayer p = server.getPlayerList().getPlayer(member);
            if (p != null) syncTo(server, p);
        }
    }
}
