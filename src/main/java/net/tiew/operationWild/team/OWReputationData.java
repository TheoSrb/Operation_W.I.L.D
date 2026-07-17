package net.tiew.operationWild.team;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.tiew.operationWild.core.OWReputation;
import net.tiew.operationWild.entity.OWEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Registre serveur persistant des contributions à la <b>réputation de tribu</b> — source de vérité
 * indépendante du chargement des chunks / de la présence en ligne des membres.
 *
 * <p>Deux tables&nbsp;:</p>
 * <ul>
 *   <li>{@code entities} : une entrée par créature apprivoisée connue ({@code entityUUID → owner,
 *       espèce, niveau}). Alimentée par {@link OWEntity#tick()} (upsert throttlé côté serveur) et
 *       purgée à la mort / suppression réelle de la créature. Comme la table persiste, les créatures
 *       d'un membre hors-ligne conservent leur dernière valeur connue.</li>
 *   <li>{@code tamingXp} : instantané de l'Expérience d'Apprivoisement par joueur (rafraîchi quand
 *       le joueur est en ligne), pour compter cette composante même hors-ligne.</li>
 * </ul>
 *
 * @see OWReputation#compute(OWReputationData, OWTeam)
 */
public class OWReputationData extends SavedData {

    public static final String DATA_NAME = "ow_reputation";

    /** Entrée d'une créature apprivoisée suivie pour la réputation. */
    public static final class EntityRep {
        public UUID owner;
        public String species;
        public int level;

        EntityRep(UUID owner, String species, int level) {
            this.owner = owner;
            this.species = species;
            this.level = level;
        }
    }

    /** entityUUID → contribution de la créature. */
    private final Map<UUID, EntityRep> entities = new HashMap<>();
    /** playerUUID → instantané d'Expérience d'Apprivoisement. */
    private final Map<UUID, Double> tamingXp = new HashMap<>();

    public OWReputationData() {}

    // ── Accès ────────────────────────────────────────────────────────────────
    public static OWReputationData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(OWReputationData::new, OWReputationData::load, null),
                DATA_NAME);
    }

    // ── Mutations créatures ─────────────────────────────────────────────────────
    /** Insère / met à jour l'entrée d'une créature apprivoisée. Ne marque dirty qu'en cas de changement. */
    public void upsertEntity(OWEntity entity) {
        if (entity == null) return;
        UUID id = entity.getUUID();
        UUID owner = entity.getOwnerUUID();
        if (owner == null) { removeEntity(id); return; }
        String species = OWReputation.speciesKey(entity);
        int level = entity.getLevel();

        EntityRep rep = entities.get(id);
        if (rep == null) {
            entities.put(id, new EntityRep(owner, species, level));
            setDirty();
        } else if (!owner.equals(rep.owner) || level != rep.level || !species.equals(rep.species)) {
            rep.owner = owner;
            rep.species = species;
            rep.level = level;
            setDirty();
        }
    }

    /** Retire l'entrée d'une créature (mort réelle, suppression, ré-ensauvagement). */
    public void removeEntity(UUID entityId) {
        if (entityId != null && entities.remove(entityId) != null) setDirty();
    }

    // ── Instantané d'Expérience d'Apprivoisement ────────────────────────────────
    public void setTamingXp(UUID playerId, double xp) {
        if (playerId == null) return;
        Double prev = tamingXp.get(playerId);
        if (prev == null || Math.abs(prev - xp) > 1.0e-6) {
            tamingXp.put(playerId, Math.max(0.0, xp));
            setDirty();
        }
    }

    public double getTamingXp(UUID playerId) {
        return playerId != null ? tamingXp.getOrDefault(playerId, 0.0) : 0.0;
    }

    // ── Agrégats ────────────────────────────────────────────────────────────────
    /** Somme de la valeur de réputation des créatures possédées par {@code owner}. */
    public double playerEntityValue(UUID owner) {
        if (owner == null) return 0.0;
        double total = 0.0;
        for (EntityRep rep : entities.values()) {
            if (owner.equals(rep.owner)) total += OWReputation.entityValue(rep.species, rep.level);
        }
        return total;
    }

    // ── Sérialisation ──────────────────────────────────────────────────────────
    public static OWReputationData load(CompoundTag tag, HolderLookup.Provider provider) {
        OWReputationData data = new OWReputationData();
        ListTag ents = tag.getList("entities", Tag.TAG_COMPOUND);
        for (int i = 0; i < ents.size(); i++) {
            CompoundTag e = ents.getCompound(i);
            try {
                UUID id = UUID.fromString(e.getString("id"));
                UUID owner = UUID.fromString(e.getString("owner"));
                data.entities.put(id, new EntityRep(owner, e.getString("species"), e.getInt("level")));
            } catch (IllegalArgumentException ignored) {}
        }
        ListTag xps = tag.getList("tamingXp", Tag.TAG_COMPOUND);
        for (int i = 0; i < xps.size(); i++) {
            CompoundTag e = xps.getCompound(i);
            try {
                data.tamingXp.put(UUID.fromString(e.getString("uuid")), e.getDouble("xp"));
            } catch (IllegalArgumentException ignored) {}
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag ents = new ListTag();
        for (Map.Entry<UUID, EntityRep> en : entities.entrySet()) {
            EntityRep rep = en.getValue();
            if (rep.owner == null || rep.species == null) continue;
            CompoundTag e = new CompoundTag();
            e.putString("id", en.getKey().toString());
            e.putString("owner", rep.owner.toString());
            e.putString("species", rep.species);
            e.putInt("level", rep.level);
            ents.add(e);
        }
        tag.put("entities", ents);

        ListTag xps = new ListTag();
        for (Map.Entry<UUID, Double> en : tamingXp.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putString("uuid", en.getKey().toString());
            e.putDouble("xp", en.getValue());
            xps.add(e);
        }
        tag.put("tamingXp", xps);
        return tag;
    }
}
