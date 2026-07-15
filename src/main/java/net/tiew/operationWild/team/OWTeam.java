package net.tiew.operationWild.team;

import net.tiew.operationWild.entity.OWEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OWTeam {

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
    private boolean[] paintPixels;

    // ── Nouveaux champs (refonte player-centric) ─────────────────────────────
    /** Forme de la bannière (silhouette). Par défaut : classique. */
    private OWTeamBannerShape bannerShape = OWTeamBannerShape.CLASSIC;
    /** Confidentialité : privée par défaut. Une tribu publique est rejoignable par n'importe quel joueur. */
    private boolean isPublic = false;
    /** Condition d'entrée d'une tribu publique : nombre minimum de Pièces Sauvages du joueur. */
    private int minWildCoins = 0;
    /** 3ᵉ couleur optionnelle (utilisée par certains motifs si {@link #useTertiary} est vrai). */
    private int tertiaryColor = 0x30B030;
    private boolean useTertiary = false;

    // Constructeur complet
    public OWTeam(int teamId, String teamName, UUID teamOwnerUUID,
                  int teamColor, int teamSecondaryColor, OWTeamMosaicPattern mosaicPattern,
                  UUID[] teamMembers, OWEntity[] teamEntitiesMembers, String teamCreationDate,
                  List<String> playerNames, List<String> entityNames,
                  boolean[] paintPixels) {
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

    public boolean[] getPaintPixels() {
        return paintPixels;
    }

    public void setPaintPixels(boolean[] v) {
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

    public int getMinWildCoins() {
        return minWildCoins;
    }

    public void setMinWildCoins(int v) {
        this.minWildCoins = Math.max(0, v);
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
    }
}