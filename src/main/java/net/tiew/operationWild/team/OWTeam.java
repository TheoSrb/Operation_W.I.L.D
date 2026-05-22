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
    private boolean[] paintPixels;

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

    public UUID[] getTeamPlayersMembers() {
        return teamPlayersMembers;
    }

    public void setTeamPlayersMembers(UUID[] v) {
        this.teamPlayersMembers = v;
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

    public boolean[] getPaintPixels() {
        return paintPixels;
    }

    public void setPaintPixels(boolean[] v) {
        this.paintPixels = v;
    }
}