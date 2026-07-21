package net.tiew.operationWild.team;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Registre serveur persistant de toutes les tribus ({@link OWTeam}) — <b>source de vérité unique</b>
 * de la refonte player-centric. Stocké sur l'overworld via {@link net.minecraft.world.level.storage.DimensionDataStorage}.
 *
 * <p>Une tribu appartient à un joueur-chef, contient des joueurs-membres (par UUID) ; les entités ne
 * sont <b>pas</b> stockées : la tribu d'une entité est dérivée à l'exécution de la tribu de son
 * propriétaire (cf. {@link #findTeamByMember(UUID)}). Cela permet à une tribu d'exister sans aucune
 * entité et à une entité de « suivre » automatiquement son propriétaire.</p>
 */
public class OWTribesSavedData extends SavedData {

    public static final String DATA_NAME = "ow_tribes";

    /** teamId → tribu. */
    private final Map<Integer, OWTeam> tribes = new HashMap<>();

    public OWTribesSavedData() {}

    // ── Accès ────────────────────────────────────────────────────────────────
    public static OWTribesSavedData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(OWTribesSavedData::new, OWTribesSavedData::load, null),
                DATA_NAME);
    }

    public Collection<OWTeam> getAllTribes() {
        return tribes.values();
    }

    public OWTeam findTeamById(int teamId) {
        return tribes.get(teamId);
    }

    /** Tribu dont {@code playerUuid} est chef OU membre, ou {@code null}. */
    public OWTeam findTeamByMember(UUID playerUuid) {
        if (playerUuid == null) return null;
        for (OWTeam t : tribes.values()) {
            if (t.isMember(playerUuid)) return t;
        }
        return null;
    }

    /** Vrai si un nom de tribu (insensible à la casse) est déjà pris. */
    public boolean isNameTaken(String name) {
        if (name == null) return false;
        for (OWTeam t : tribes.values()) {
            if (t.getTeamName() != null && t.getTeamName().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    // ── Mutations ──────────────────────────────────────────────────────────────
    /** Enregistre / met à jour une tribu et marque les données à sauvegarder. */
    public void putTribe(OWTeam team) {
        if (team == null) return;
        tribes.put(team.getTeamId(), team);
        setDirty();
    }

    public void removeTribe(int teamId) {
        if (tribes.remove(teamId) != null) setDirty();
    }

    /** Supprime TOUTES les tribus (debug/tests). Renvoie le nombre supprimé. */
    public int clearAll() {
        int n = tribes.size();
        if (n > 0) { tribes.clear(); setDirty(); }
        return n;
    }

    /** Génère un id de tribu unique. */
    public int nextTeamId() {
        int id;
        do {
            id = (int) (System.nanoTime() & 0x7FFFFFFF);
        } while (id == 0 || tribes.containsKey(id));
        return id;
    }

    /** Crée une tribu neuve (chef = créateur, seul membre au départ) et l'enregistre. */
    public OWTeam createTribe(UUID chief, String chiefName, String name,
                              int primary, int secondary, OWTeamMosaicPattern pattern,
                              OWTeamBannerShape shape, byte[] paintPixels) {
        int id = nextTeamId();
        List<String> playerNames = new ArrayList<>();
        playerNames.add(chiefName);
        OWTeam team = new OWTeam(
                id, name, chief, primary, secondary, pattern,
                new UUID[]{ chief }, new net.tiew.operationWild.entity.OWEntity[]{},
                LocalDate.now().toString(),
                playerNames, new ArrayList<>(), paintPixels);
        team.setPlayerUUIDs(new ArrayList<>(List.of(chief)));
        team.setBannerShape(shape);
        putTribe(team);
        return team;
    }

    // ── Sérialisation ──────────────────────────────────────────────────────────
    public static OWTribesSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        OWTribesSavedData data = new OWTribesSavedData();
        ListTag list = tag.getList("tribes", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            OWTeam team = readTeam(list.getCompound(i));
            if (team != null) data.tribes.put(team.getTeamId(), team);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (OWTeam team : tribes.values()) {
            list.add(writeTeam(team));
        }
        tag.put("tribes", list);
        return tag;
    }

    private static CompoundTag writeTeam(OWTeam t) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("teamId", t.getTeamId());
        tag.putString("teamName", t.getTeamName() != null ? t.getTeamName() : "");
        tag.putString("teamOwnerUUID", t.getTeamOwnerUUID().toString());
        tag.putInt("teamColor", t.getTeamColor());
        tag.putInt("teamSecondaryColor", t.getTeamSecondaryColor());
        tag.putInt("teamMosaicPatternId", t.getTeamMosaicPattern().getId());
        tag.putInt("bannerShapeId", t.getBannerShape().getId());
        tag.putBoolean("isPublic", t.isPublic());
        ListTag joinReqs = new ListTag();
        for (OWTribeJoinRequirement r : t.getJoinRequirements()) {
            CompoundTag rt = new CompoundTag();
            rt.putInt("id", r.condition().getId());
            rt.putInt("threshold", r.threshold());
            joinReqs.add(rt);
        }
        tag.put("joinRequirements", joinReqs);
        tag.putBoolean("directJoin", t.isDirectJoin());
        tag.putInt("tertiaryColor", t.getTertiaryColor());
        tag.putBoolean("useTertiary", t.isUseTertiary());
        tag.putInt("reputationOverride", t.getReputationOverride());
        tag.putBoolean("reputationEnabled", t.isReputationEnabled());
        tag.putString("teamCreationDate", t.getTeamCreationDate() != null ? t.getTeamCreationDate() : "");

        ListTag pUUIDs = new ListTag();
        for (UUID u : t.getPlayerUUIDs()) pUUIDs.add(StringTag.valueOf(u.toString()));
        tag.put("playerUUIDs", pUUIDs);

        ListTag pNames = new ListTag();
        for (String n : t.getPlayerNames()) pNames.add(StringTag.valueOf(n));
        tag.put("playerNames", pNames);

        byte[] pixels = t.getPaintPixels();
        tag.putByteArray("paintPixels", OWTeamMosaicPattern.packPixels3(pixels != null ? pixels : new byte[0]));

        ListTag deputies = new ListTag();
        for (UUID u : t.getDeputyUUIDs()) deputies.add(StringTag.valueOf(u.toString()));
        tag.put("deputies", deputies);

        ListTag perms = new ListTag();
        for (Map.Entry<UUID, Integer> e : t.getMemberPermissions().entrySet()) {
            CompoundTag pe = new CompoundTag();
            pe.putString("uuid", e.getKey().toString());
            pe.putInt("mask", e.getValue());
            perms.add(pe);
        }
        tag.put("memberPermissions", perms);

        ListTag champions = new ListTag();
        for (UUID u : t.getChampionUUIDs()) champions.add(StringTag.valueOf(u.toString()));
        tag.put("champions", champions);

        tag.putBoolean("arenaAccepted", t.isArenaAccepted());
        tag.putInt("arenaPrestige", t.getArenaPrestige());
        tag.putInt("arenaReputationBonus", t.getArenaReputationBonus());
        ListTag arenaClaims = new ListTag();
        for (Map.Entry<UUID, Integer> e : t.getArenaChestsClaimed().entrySet()) {
            CompoundTag ce = new CompoundTag();
            ce.putString("uuid", e.getKey().toString());
            ce.putInt("count", e.getValue());
            arenaClaims.add(ce);
        }
        tag.put("arenaChestsClaimed", arenaClaims);
        ListTag tribeClaims = new ListTag();
        for (Map.Entry<UUID, Integer> e : t.getArenaTribeChestsClaimed().entrySet()) {
            CompoundTag ce = new CompoundTag();
            ce.putString("uuid", e.getKey().toString());
            ce.putInt("count", e.getValue());
            tribeClaims.add(ce);
        }
        tag.put("arenaTribeChestsClaimed", tribeClaims);
        return tag;
    }

    /**
     * Lit les conditions d'entrée, en rattrapant les deux formats antérieurs : {@code joinConditionId}
     * (condition unique) puis, plus ancien, {@code minWildCoins} (seule condition qui ait existé).
     * Une tribu sauvegardée avant ces réglages ressort simplement sans condition.
     */
    private static List<OWTribeJoinRequirement> readJoinRequirements(CompoundTag tag) {
        List<OWTribeJoinRequirement> out = new ArrayList<>();
        if (tag.contains("joinRequirements")) {
            ListTag list = tag.getList("joinRequirements", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag rt = list.getCompound(i);
                out.add(new OWTribeJoinRequirement(
                        OWTribeJoinCondition.byId(rt.getInt("id")), rt.getInt("threshold")));
            }
        } else if (tag.contains("joinConditionId")) {
            out.add(new OWTribeJoinRequirement(
                    OWTribeJoinCondition.byId(tag.getInt("joinConditionId")), tag.getInt("joinThreshold")));
        } else if (tag.getInt("minWildCoins") > 0) {
            out.add(new OWTribeJoinRequirement(OWTribeJoinCondition.WILD_COINS, tag.getInt("minWildCoins")));
        }
        return out;
    }

    private static OWTeam readTeam(CompoundTag tag) {
        try {
            UUID owner = UUID.fromString(tag.getString("teamOwnerUUID"));

            List<String> pNames = new ArrayList<>();
            ListTag pnTag = tag.getList("playerNames", Tag.TAG_STRING);
            for (int i = 0; i < pnTag.size(); i++) pNames.add(pnTag.getString(i));

            List<UUID> pUUIDs = new ArrayList<>();
            ListTag puTag = tag.getList("playerUUIDs", Tag.TAG_STRING);
            for (int i = 0; i < puTag.size(); i++) {
                try { pUUIDs.add(UUID.fromString(puTag.getString(i))); }
                catch (IllegalArgumentException ignored) {}
            }
            if (pUUIDs.isEmpty()) pUUIDs.add(owner);

            byte[] pixels = OWTeamMosaicPattern.unpackPixels3(
                    tag.contains("paintPixels") ? tag.getByteArray("paintPixels") : new byte[0],
                    OWTeamMosaicPattern.CUSTOM_PAINT_PIXEL_COUNT);

            OWTeam team = new OWTeam(
                    tag.getInt("teamId"),
                    tag.getString("teamName"),
                    owner,
                    tag.getInt("teamColor"),
                    tag.contains("teamSecondaryColor") ? tag.getInt("teamSecondaryColor") : 0xFFFFFF,
                    OWTeamMosaicPattern.byId(tag.getInt("teamMosaicPatternId")),
                    new UUID[]{}, new net.tiew.operationWild.entity.OWEntity[]{},
                    tag.getString("teamCreationDate"),
                    pNames, new ArrayList<>(), pixels);
            team.setPlayerUUIDs(pUUIDs);
            team.setBannerShape(OWTeamBannerShape.byId(tag.getInt("bannerShapeId")));
            team.setPublic(tag.getBoolean("isPublic"));
            team.setJoinRequirements(readJoinRequirements(tag));
            // Absent des sauvegardes antérieures → false : validation par le chef, le défaut voulu.
            team.setDirectJoin(tag.getBoolean("directJoin"));
            if (tag.contains("tertiaryColor")) team.setTertiaryColor(tag.getInt("tertiaryColor"));
            team.setUseTertiary(tag.getBoolean("useTertiary"));
            team.setReputationOverride(tag.contains("reputationOverride") ? tag.getInt("reputationOverride") : -1);
            team.setReputationEnabled(tag.getBoolean("reputationEnabled"));

            List<UUID> deputies = new ArrayList<>();
            ListTag depTag = tag.getList("deputies", Tag.TAG_STRING);
            for (int i = 0; i < depTag.size(); i++) {
                try { deputies.add(UUID.fromString(depTag.getString(i))); } catch (IllegalArgumentException ignored) {}
            }
            team.setDeputyUUIDs(deputies);

            ListTag permTag = tag.getList("memberPermissions", Tag.TAG_COMPOUND);
            for (int i = 0; i < permTag.size(); i++) {
                CompoundTag pe = permTag.getCompound(i);
                try { team.setPermissions(UUID.fromString(pe.getString("uuid")), pe.getInt("mask")); }
                catch (IllegalArgumentException ignored) {}
            }

            // Arène — absente des sauvegardes antérieures : la tribu repart d'un état « non accepté ».
            List<UUID> champions = new ArrayList<>();
            ListTag champTag = tag.getList("champions", Tag.TAG_STRING);
            for (int i = 0; i < champTag.size(); i++) {
                try { champions.add(UUID.fromString(champTag.getString(i))); } catch (IllegalArgumentException ignored) {}
            }
            team.setChampionUUIDs(champions);

            team.setArenaAccepted(tag.getBoolean("arenaAccepted"));
            team.setArenaPrestige(tag.getInt("arenaPrestige"));
            team.setArenaReputationBonus(tag.getInt("arenaReputationBonus"));
            ListTag claimTag = tag.getList("arenaChestsClaimed", Tag.TAG_COMPOUND);
            for (int i = 0; i < claimTag.size(); i++) {
                CompoundTag ce = claimTag.getCompound(i);
                try { team.getArenaChestsClaimed().put(UUID.fromString(ce.getString("uuid")), ce.getInt("count")); }
                catch (IllegalArgumentException ignored) {}
            }
            ListTag tribeTag = tag.getList("arenaTribeChestsClaimed", Tag.TAG_COMPOUND);
            for (int i = 0; i < tribeTag.size(); i++) {
                CompoundTag ce = tribeTag.getCompound(i);
                try { team.getArenaTribeChestsClaimed().put(UUID.fromString(ce.getString("uuid")), ce.getInt("count")); }
                catch (IllegalArgumentException ignored) {}
            }
            return team;
        } catch (Exception e) {
            return null;
        }
    }
}
