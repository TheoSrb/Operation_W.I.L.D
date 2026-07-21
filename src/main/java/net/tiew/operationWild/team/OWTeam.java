package net.tiew.operationWild.team;

import net.tiew.operationWild.entity.OWEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OWTeam {

    /** Nombre maximum de conditions d'entrée cumulables sur une tribu publique. */
    public static final int MAX_JOIN_REQUIREMENTS = 3;

    private int teamId;
    private String teamName;
    private UUID teamOwnerUUID;
    private int teamColor;
    private int teamSecondaryColor;
    private OWTeamMosaicPattern teamMosaicPattern;
    private UUID[] teamPlayersMembers;
    private OWEntity[] teamEntitiesMembers;
    private String teamCreationDate;
    private List<String> playerNames;
    private List<String> entityNames;
    private List<UUID> entityUUIDs;
    private List<UUID> playerUUIDs;
    /** Peinture custom : 1 valeur par pixel (0 = teinte 1, 1 = teinte 2, 2 = teinte 3). */
    private byte[] paintPixels;

    // ── Nouveaux champs (refonte player-centric) ─────────────────────────────
    /** Forme de la bannière (silhouette). Par défaut : classique. */
    private OWTeamBannerShape bannerShape = OWTeamBannerShape.CLASSIC;
    /** Confidentialité : privée par défaut. Une tribu publique est rejoignable par n'importe quel joueur. */
    private boolean isPublic = false;
    /** Conditions d'entrée d'une tribu publique : 0 à {@link #MAX_JOIN_REQUIREMENTS}, toutes requises
     *  simultanément. Liste vide = tribu publique ouverte à tous. */
    private List<OWTribeJoinRequirement> joinRequirements = new ArrayList<>();
    /** Tribu publique : un joueur qui remplit les conditions entre-t-il directement ({@code true}), ou
     *  sa demande part-elle au chef et aux adjoints pour validation ({@code false}, défaut) ? */
    private boolean directJoin = false;
    /** 3ᵉ couleur optionnelle (utilisée par certains motifs si {@link #useTertiary} est vrai). */
    private int tertiaryColor = 0x30B030;
    private boolean useTertiary = false;
    /** Chefs adjoints (par UUID) : rôle intermédiaire (peut virer les membres simples + gérer leurs permissions). */
    private List<UUID> deputyUUIDs = new ArrayList<>();
    /** Permissions granulaires par membre simple (UUID → bitmask {@link net.tiew.operationWild.team.OWTribePermission}). */
    private Map<UUID, Integer> memberPermissions = new HashMap<>();
    /** Réputation de tribu (score de prestige) — calculée côté serveur, poussée au client pour l'affichage.
     *  Non sérialisée : la source de vérité est {@link net.tiew.operationWild.team.OWReputationData}. */
    private transient int reputation = 0;
    /** Réputation forcée par admin (commande) : -1 = pas d'override (réputation calculée normalement). Persistée. */
    private int reputationOverride = -1;
    /** La réputation doit être activée une fois par la tribu (dans l'onglet) avant d'être suivie et affichée. */
    private boolean reputationEnabled = false;

    // ── Champions ────────────────────────────────────────────────────────────
    /**
     * Les créatures qui représentent la tribu, dans l'ordre choisi par le chef.
     *
     * <p>Elles seules arborent l'étendard : le drapeau n'est plus un attribut d'appartenance porté
     * par tout le monde, mais une distinction accordée à cinq bêtes au plus. La liste est
     * <b>répliquée aux clients</b> avec le reste de la tribu ({@code SyncOWTeamPacket}), car c'est
     * elle que consulte le layer de rendu — un drapeau doit se voir sur les créatures des autres
     * tribus aussi.</p>
     *
     * <p>Les UUID sont conservés même si la créature est déchargée ou dans une autre dimension :
     * un champion reste un champion tant que le chef ne l'a pas révoqué.</p>
     */
    private List<UUID> championUUIDs = new ArrayList<>();

    // ── Arène ────────────────────────────────────────────────────────────────
    /** Le chef a accepté le règlement de l'arène : tant que c'est faux, l'onglet reste caché aux membres. */
    private boolean arenaAccepted = false;
    /** Points de prestige d'arène — compteur <b>partagé par la tribu</b>, alimenté par les victoires. */
    private int arenaPrestige = 0;
    /** Coffres d'arène déjà réclamés, par membre. Cf. {@link net.tiew.operationWild.core.OWArena}. */
    private Map<UUID, Integer> arenaChestsClaimed = new HashMap<>();
    /** Coffres de <b>tribu</b> déjà réclamés, par membre (récompense d'assiduité, hors badge). */
    private Map<UUID, Integer> arenaTribeChestsClaimed = new HashMap<>();
    /** Réputation cumulée gagnée en arène — s'ajoute au score calculé depuis les créatures. Persistée. */
    private int arenaReputationBonus = 0;

    // Constructeur complet
    public OWTeam(int teamId, String teamName, UUID teamOwnerUUID,
                  int teamColor, int teamSecondaryColor, OWTeamMosaicPattern mosaicPattern,
                  UUID[] teamMembers, OWEntity[] teamEntitiesMembers, String teamCreationDate,
                  List<String> playerNames, List<String> entityNames,
                  byte[] paintPixels) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.teamOwnerUUID = teamOwnerUUID;
        this.teamColor = teamColor;
        this.teamSecondaryColor = teamSecondaryColor;
        this.teamMosaicPattern = mosaicPattern != null ? mosaicPattern : OWTeamMosaicPattern.GRADIENT_DOWN;
        this.teamPlayersMembers = teamMembers;
        this.teamEntitiesMembers = teamEntitiesMembers;
        this.teamCreationDate = teamCreationDate;
        this.playerNames = playerNames != null ? playerNames : new ArrayList<>();
        this.entityNames = entityNames != null ? entityNames : new ArrayList<>();
        this.entityUUIDs = new ArrayList<>();
        // playerUUIDs est la source de vérité de l'appartenance des joueurs (rename-safe,
        // mais surtout pseudo-safe : on ne se fie jamais au pseudo pour l'appartenance).
        // On l'initialise depuis le tableau UUID[] fourni (création de tribu) ; les chemins
        // de sync / NBT passent un tableau vide puis appellent setPlayerUUIDs().
        this.playerUUIDs = new ArrayList<>();
        if (teamMembers != null) {
            for (UUID u : teamMembers) if (u != null) this.playerUUIDs.add(u);
        }
        this.paintPixels = paintPixels;
    }

    // Constructeur sans paintPixels (rétrocompatibilité)
    public OWTeam(int teamId, String teamName, UUID teamOwnerUUID,
                  int teamColor, int teamSecondaryColor, OWTeamMosaicPattern mosaicPattern,
                  UUID[] teamMembers, OWEntity[] teamEntitiesMembers, String teamCreationDate,
                  List<String> playerNames, List<String> entityNames) {
        this(teamId, teamName, teamOwnerUUID, teamColor, teamSecondaryColor, mosaicPattern,
                teamMembers, teamEntitiesMembers, teamCreationDate, playerNames, entityNames, null);
    }

    public OWTeam(int teamId, String teamName, UUID teamOwnerUUID, int teamColor,
                  UUID[] teamMembers, OWEntity[] teamEntitiesMembers, String teamCreationDate,
                  List<String> playerNames, List<String> entityNames) {
        this(teamId, teamName, teamOwnerUUID, teamColor, 0xFFFFFF, OWTeamMosaicPattern.GRADIENT_DOWN,
                teamMembers, teamEntitiesMembers, teamCreationDate, playerNames, entityNames, null);
    }

    public OWTeam(int teamId, String teamName, UUID teamOwnerUUID, int teamColor,
                  UUID[] teamMembers, OWEntity[] teamEntitiesMembers, String teamCreationDate) {
        this(teamId, teamName, teamOwnerUUID, teamColor, teamMembers, teamEntitiesMembers,
                teamCreationDate, new ArrayList<>(), new ArrayList<>());
    }

    // ── Getters / Setters ────────────────────────────────────────────────────
    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int v) {
        this.teamId = v;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String v) {
        this.teamName = v;
    }

    public UUID getTeamOwnerUUID() {
        return teamOwnerUUID;
    }

    public void setTeamOwnerUUID(UUID v) {
        this.teamOwnerUUID = v;
    }

    public int getTeamColor() {
        return teamColor;
    }

    public int getMaxPlayers()  { return 10; }
    public int getMaxEntities() { return 20; }

    public String getFormattedCreationDate() {
        if (this.teamCreationDate == null || this.teamCreationDate.isEmpty()) return "?";
        try {
            String[] parts = this.teamCreationDate.split("-");
            return parts[2] + "/" + parts[1] + "/" + parts[0];
        } catch (Exception e) {
            return this.teamCreationDate;
        }
    }

    public void setTeamColor(int v) {
        this.teamColor = v;
    }

    public int getTeamSecondaryColor() {
        return teamSecondaryColor;
    }

    public void setTeamSecondaryColor(int v) {
        this.teamSecondaryColor = v;
    }

    public OWTeamMosaicPattern getTeamMosaicPattern() {
        return teamMosaicPattern;
    }

    public void setTeamMosaicPattern(OWTeamMosaicPattern v) {
        this.teamMosaicPattern = v;
    }

    /** Dérivé de {@link #getPlayerUUIDs()} : l'appartenance des joueurs est canoniquement portée
     *  par la liste d'UUID. Conservé pour compatibilité avec le code existant. */
    public UUID[] getTeamPlayersMembers() {
        return getPlayerUUIDs().toArray(new UUID[0]);
    }

    public void setTeamPlayersMembers(UUID[] v) {
        this.playerUUIDs = new ArrayList<>();
        if (v != null) for (UUID u : v) if (u != null) this.playerUUIDs.add(u);
    }

    public List<UUID> getPlayerUUIDs() {
        if (playerUUIDs == null) playerUUIDs = new ArrayList<>();
        return playerUUIDs;
    }

    public void setPlayerUUIDs(List<UUID> v) {
        this.playerUUIDs = v != null ? v : new ArrayList<>();
    }

    /** Vrai si le joueur (par UUID) est membre de la tribu — chef inclus. */
    public boolean isMember(UUID playerUuid) {
        if (playerUuid == null) return false;
        if (playerUuid.equals(teamOwnerUUID)) return true;
        return getPlayerUUIDs().contains(playerUuid);
    }

    public OWEntity[] getTeamEntitiesMembers() {
        return teamEntitiesMembers;
    }

    public void setTeamEntitiesMembers(OWEntity[] v) {
        this.teamEntitiesMembers = v;
    }

    public String getTeamCreationDate() {
        return teamCreationDate;
    }

    public void setTeamCreationDate(String v) {
        this.teamCreationDate = v;
    }

    public List<String> getPlayerNames() {
        return playerNames;
    }

    public void setPlayerNames(List<String> v) {
        this.playerNames = v;
    }

    public List<String> getEntityNames() {
        return entityNames;
    }

    public void setEntityNames(List<String> v) {
        this.entityNames = v;
    }

    public List<UUID> getEntityUUIDs() {
        if (entityUUIDs == null) entityUUIDs = new ArrayList<>();
        return entityUUIDs;
    }

    public void setEntityUUIDs(List<UUID> v) {
        this.entityUUIDs = v != null ? v : new ArrayList<>();
    }

    public byte[] getPaintPixels() {
        return paintPixels;
    }

    public void setPaintPixels(byte[] v) {
        this.paintPixels = v;
    }

    // ── Nouveaux champs (refonte player-centric) ─────────────────────────────
    public OWTeamBannerShape getBannerShape() {
        return bannerShape != null ? bannerShape : OWTeamBannerShape.CLASSIC;
    }

    public void setBannerShape(OWTeamBannerShape v) {
        this.bannerShape = v != null ? v : OWTeamBannerShape.CLASSIC;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean v) {
        this.isPublic = v;
    }

    public List<OWTribeJoinRequirement> getJoinRequirements() {
        if (joinRequirements == null) joinRequirements = new ArrayList<>();
        return joinRequirements;
    }

    /** Remplace les conditions d'entrée. La liste est nettoyée (doublons, {@code NONE}, seuils, nombre max). */
    public void setJoinRequirements(List<OWTribeJoinRequirement> v) {
        this.joinRequirements = OWTribeJoinRequirement.sanitize(v);
    }

    /** Vrai si la tribu impose au moins une condition d'entrée. */
    public boolean hasJoinRequirements() {
        return !getJoinRequirements().isEmpty();
    }

    public boolean isDirectJoin() {
        return directJoin;
    }

    public void setDirectJoin(boolean v) {
        this.directJoin = v;
    }

    /** L'exigence portant sur {@code condition}, ou {@code null} si la tribu ne l'impose pas. */
    public OWTribeJoinRequirement requirementFor(OWTribeJoinCondition condition) {
        for (OWTribeJoinRequirement r : getJoinRequirements()) {
            if (r.condition() == condition) return r;
        }
        return null;
    }

    public int getTertiaryColor() {
        return tertiaryColor;
    }

    public void setTertiaryColor(int v) {
        this.tertiaryColor = v;
    }

    public boolean isUseTertiary() {
        return useTertiary;
    }

    public void setUseTertiary(boolean v) {
        this.useTertiary = v;
    }

    /**
     * Ajoute un joueur comme membre (par UUID + nom d'affichage). Idempotent.
     * L'appartenance des joueurs est la source de vérité ; les entités suivent leur propriétaire.
     */
    public void addPlayerMember(UUID uuid, String name) {
        if (uuid == null) return;
        if (!getPlayerUUIDs().contains(uuid)) {
            getPlayerUUIDs().add(uuid);
            if (playerNames == null) playerNames = new ArrayList<>();
            playerNames.add(name != null ? name : uuid.toString().substring(0, 8));
        }
    }

    /** Retire un joueur membre (par UUID) et son nom d'affichage à l'index correspondant. */
    public void removePlayerMember(UUID uuid) {
        if (uuid == null) return;
        int idx = getPlayerUUIDs().indexOf(uuid);
        if (idx >= 0) {
            getPlayerUUIDs().remove(idx);
            if (playerNames != null && idx < playerNames.size()) playerNames.remove(idx);
        }
        getDeputyUUIDs().remove(uuid);
        getMemberPermissions().remove(uuid);
    }

    // ── Rôles & permissions ─────────────────────────────────────────────────────
    public boolean isChief(UUID u) { return u != null && u.equals(teamOwnerUUID); }

    public List<UUID> getDeputyUUIDs() {
        if (deputyUUIDs == null) deputyUUIDs = new ArrayList<>();
        return deputyUUIDs;
    }

    public void setDeputyUUIDs(List<UUID> v) { this.deputyUUIDs = v != null ? v : new ArrayList<>(); }

    public boolean isDeputy(UUID u) { return u != null && getDeputyUUIDs().contains(u); }

    public void setDeputy(UUID u, boolean deputy) {
        if (u == null || u.equals(teamOwnerUUID)) return; // le chef n'est jamais « adjoint »
        if (deputy) { if (!getDeputyUUIDs().contains(u)) getDeputyUUIDs().add(u); }
        else getDeputyUUIDs().remove(u);
    }

    public Map<UUID, Integer> getMemberPermissions() {
        if (memberPermissions == null) memberPermissions = new HashMap<>();
        return memberPermissions;
    }

    public void setMemberPermissions(Map<UUID, Integer> v) { this.memberPermissions = v != null ? v : new HashMap<>(); }

    public int getPermissions(UUID u) { return getMemberPermissions().getOrDefault(u, 0); }

    public void setPermissions(UUID u, int mask) { if (u != null) getMemberPermissions().put(u, mask); }

    public boolean hasPermissionBit(UUID u, int bit) { return (getPermissions(u) & bit) != 0; }

    // ── Réputation (affichage client) ────────────────────────────────────────────
    /**
     * Réputation de la tribu — <b>valable côté client uniquement</b>.
     *
     * <p>Le champ est transitoire et n'est renseigné que par le paquet de synchronisation : sur le
     * serveur il vaut toujours 0. Côté serveur, passer par
     * {@code OWReputation.compute(OWReputationData.get(server), team)}.</p>
     */
    public int getReputation() { return reputation; }

    public void setReputation(int v) { this.reputation = Math.max(0, v); }

    /** -1 = pas d'override (réputation calculée). Sinon, valeur forcée par la commande admin. */
    // ── Champions ────────────────────────────────────────────────────────────
    public List<UUID> getChampionUUIDs() { return championUUIDs; }

    public void setChampionUUIDs(List<UUID> v) {
        this.championUUIDs = v != null ? new ArrayList<>(v) : new ArrayList<>();
    }

    /** Cette créature porte-t-elle l'étendard de la tribu ? */
    public boolean isChampion(UUID entityUuid) {
        return entityUuid != null && championUUIDs.contains(entityUuid);
    }

    public int getReputationOverride() { return reputationOverride; }

    public void setReputationOverride(int v) { this.reputationOverride = v < 0 ? -1 : v; }

    public boolean isReputationEnabled() { return reputationEnabled; }

    public void setReputationEnabled(boolean v) { this.reputationEnabled = v; }

    // ── Arène ────────────────────────────────────────────────────────────────
    public boolean isArenaAccepted() { return arenaAccepted; }

    public void setArenaAccepted(boolean v) { this.arenaAccepted = v; }

    public int getArenaPrestige() { return arenaPrestige; }

    public void setArenaPrestige(int v) { this.arenaPrestige = Math.max(0, v); }

    public void addArenaPrestige(int v) { setArenaPrestige(arenaPrestige + v); }

    public Map<UUID, Integer> getArenaChestsClaimed() {
        if (arenaChestsClaimed == null) arenaChestsClaimed = new HashMap<>();
        return arenaChestsClaimed;
    }

    public void setArenaChestsClaimed(Map<UUID, Integer> v) {
        this.arenaChestsClaimed = v != null ? v : new HashMap<>();
    }

    /** Nombre de coffres d'arène déjà réclamés par {@code u}. */
    public int getArenaChestsClaimed(UUID u) { return getArenaChestsClaimed().getOrDefault(u, 0); }

    /** Coffres d'arène qu'il reste à réclamer à {@code u} (0 si aucun). */
    public int pendingArenaChests(UUID u) {
        return net.tiew.operationWild.core.OWArena.pendingChests(arenaPrestige, getArenaChestsClaimed(u));
    }

    public int getArenaReputationBonus() { return arenaReputationBonus; }

    public void setArenaReputationBonus(int v) { this.arenaReputationBonus = Math.max(0, v); }

    public void addArenaReputationBonus(int v) { setArenaReputationBonus(arenaReputationBonus + v); }

    public Map<UUID, Integer> getArenaTribeChestsClaimed() {
        if (arenaTribeChestsClaimed == null) arenaTribeChestsClaimed = new HashMap<>();
        return arenaTribeChestsClaimed;
    }

    public void setArenaTribeChestsClaimed(Map<UUID, Integer> v) {
        this.arenaTribeChestsClaimed = v != null ? v : new HashMap<>();
    }

    public int getArenaTribeChestsClaimed(UUID u) { return getArenaTribeChestsClaimed().getOrDefault(u, 0); }

    /**
     * Coffres de tribu qu'il reste à réclamer à {@code u}, gagnés au rythme d'un pour
     * {@link net.tiew.operationWild.core.OWArena#CHESTS_PER_TRIBE_CHEST} coffres de badge ouverts.
     *
     * <p>Ne tient pas compte du badge minimal exigé : cette condition dépend de la réputation, qui
     * est calculée par le serveur, et est vérifiée au moment de la réclamation.</p>
     */
    public int pendingTribeChests(UUID u) {
        return net.tiew.operationWild.core.OWArena.pendingTribeChests(
                getArenaChestsClaimed(u), getArenaTribeChestsClaimed(u));
    }

    /** Enregistre l'ouverture d'un coffre de tribu par {@code u}. */
    public boolean claimTribeChest(UUID u) {
        if (u == null || pendingTribeChests(u) <= 0) return false;
        getArenaTribeChestsClaimed().merge(u, 1, Integer::sum);
        return true;
    }

    /** Enregistre l'ouverture d'un coffre par {@code u}. Sans effet s'il n'en a aucun en attente. */
    public boolean claimArenaChest(UUID u) {
        if (u == null || pendingArenaChests(u) <= 0) return false;
        getArenaChestsClaimed().merge(u, 1, Integer::sum);
        return true;
    }
}