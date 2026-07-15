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
                              OWTeamBannerShape shape, boolean[] paintPixels) {
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
        tag.putInt("minWildCoins", t.getMinWildCoins());
        tag.putString("teamCreationDate", t.getTeamCreationDate() != null ? t.getTeamCreationDate() : "");

        ListTag pUUIDs = new ListTag();
        for (UUID u : t.getPlayerUUIDs()) pUUIDs.add(StringTag.valueOf(u.toString()));
        tag.put("playerUUIDs", pUUIDs);

        ListTag pNames = new ListTag();
        for (String n : t.getPlayerNames()) pNames.add(StringTag.valueOf(n));
        tag.put("playerNames", pNames);

        boolean[] pixels = t.getPaintPixels();
        tag.putByteArray("paintPixels", OWTeamMosaicPattern.packPixels(pixels != null ? pixels : new boolean[0]));
        return tag;
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

            boolean[] pixels = OWTeamMosaicPattern.unpackPixels(
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
            team.setMinWildCoins(tag.getInt("minWildCoins"));
            return team;
        } catch (Exception e) {
            return null;
        }
    }
}
