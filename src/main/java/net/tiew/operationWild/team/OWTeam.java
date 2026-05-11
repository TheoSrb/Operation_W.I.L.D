package net.tiew.operationWild.team;

import net.tiew.operationWild.entity.OWEntity;

import java.util.UUID;

public class OWTeam {

    private int teamId;
    private String teamName;
    private UUID teamOwnerUUID;
    private int teamColor;
    private UUID[] teamPlayersMembers;
    private OWEntity[] teamEntitiesMembers;
    private String teamCreationDate;


    public OWTeam(int teamId, String teamName, UUID teamOwnerUUID, int teamColor, UUID[] teamMembers, OWEntity[] teamEntitiesMembers, String teamCreationDate) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.teamOwnerUUID = teamOwnerUUID;
        this.teamColor = teamColor;
        this.teamPlayersMembers = teamMembers;
        this.teamEntitiesMembers = teamEntitiesMembers;
        this.teamCreationDate = teamCreationDate;
    }


    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public UUID getTeamOwnerUUID() {
        return teamOwnerUUID;
    }

    public void setTeamOwnerUUID(UUID teamOwnerUUID) {
        this.teamOwnerUUID = teamOwnerUUID;
    }

    public int getTeamColor() {
        return teamColor;
    }

    public void setTeamColor(int teamColor) {
        this.teamColor = teamColor;
    }

    public UUID[] getTeamPlayersMembers() {
        return teamPlayersMembers;
    }

    public void setTeamPlayersMembers(UUID[] teamMembers) {
        this.teamPlayersMembers = teamMembers;
    }

    public String getTeamCreationDate() {
        return teamCreationDate;
    }

    public void setTeamCreationDate(String teamCreationDate) {
        this.teamCreationDate = teamCreationDate;
    }

    public OWEntity[] getTeamEntitiesMembers() {
        return teamEntitiesMembers;
    }

    public void setTeamEntitiesMembers(OWEntity[] teamEntitiesMembers) {
        this.teamEntitiesMembers = teamEntitiesMembers;
    }
}
