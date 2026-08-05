package net.tiew.operationWild.waypoint;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.tiew.operationWild.entity.IOWWaypointEntity;
import net.tiew.operationWild.entity.OWEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Registre serveur persistant des waypoints — <b>source de vérité unique</b>, stockée dans la
 * sauvegarde du monde via {@link net.minecraft.world.level.storage.DimensionDataStorage}.
 *
 * <p>C'est ce qui corrige le bug critique. Les waypoints vivaient auparavant dans un fichier du
 * dossier {@code config/} du client, nommé d'après une clé de monde <i>devinée</i> côté client
 * (nom du dossier de sauvegarde en solo, adresse du serveur en multi). Cette clé pouvait être
 * résolue au mauvais moment — serveur intégré en cours d'arrêt ou de démarrage — ou tout
 * simplement se répéter d'un monde à l'autre : le monde B rouvrait alors le fichier du monde A et
 * ressuscitait des repères pointant vers des créatures qui n'existaient pas chez lui.</p>
 *
 * <p>Il n'y a plus rien à deviner : la donnée est <b>dans</b> le monde. Un monde ne peut pas lire
 * la sauvegarde d'un autre, et le multijoueur devient correct par construction — le serveur ne
 * pousse à chaque joueur que les entrées dont il est propriétaire, et le client n'écrit plus
 * aucun fichier partagé entre les serveurs qu'il fréquente.</p>
 *
 * <p>La dimension de chaque créature est enregistrée : un compagnon laissé dans l'Overworld ne
 * s'affiche pas depuis le Nether, mais son entrée reste en mémoire et reparaît au retour.</p>
 */
public class OWWaypointData extends SavedData {

    public static final String DATA_NAME = "ow_waypoints";

    /**
     * Déplacement, en blocs, à partir duquel la position stockée vaut la peine d'être renvoyée au
     * client. En deçà, l'écart est invisible à l'échelle d'un waypoint : les créatures proches sont
     * de toute façon chargées chez le client, qui lit leur position réelle image par image.
     */
    private static final double SYNC_MOVE_THRESHOLD = 4.0;

    /** Déplacement à partir duquel la sauvegarde du monde mérite d'être remarquée comme modifiée. */
    private static final double SAVE_MOVE_THRESHOLD = 0.5;

    /** Une créature suivie, telle que le monde la retient entre deux sessions. */
    public static final class Waypoint {
        public UUID owner;
        public String entityTypeId = "";
        public boolean hasCustomName;
        public String customName = "";
        public String dimension = "";
        public double x, y, z;
        public int fillColor = 0x1A8FFF, borderColor = 0xAADDFF, textColor = 0xFFFFFF, entityColor = 0xFFFFFF;
        public int iconSize = 5, maxDist = 2000;
        public float minDist = 20.0f, minOpacity = 0.3f, fontScale = 0.5f;
        public boolean enabled = true;
    }

    /** entityUUID → waypoint. */
    private final Map<UUID, Waypoint> waypoints = new HashMap<>();
    /** playerUUID → compteur de version, incrémenté dès qu'une de ses entrées mérite d'être renvoyée. */
    private final Map<UUID, Integer> revisions = new HashMap<>();

    public OWWaypointData() {}

    public static OWWaypointData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(OWWaypointData::new, OWWaypointData::load, null),
                DATA_NAME);
    }

    // ── Mutations ──────────────────────────────────────────────────────────────
    /**
     * Insère / met à jour l'entrée d'une créature apprivoisée depuis son état vivant.
     *
     * <p>Appelée de façon throttlée depuis le tick serveur de la créature. L'apparence est figée
     * ici — le serveur y a accès sans ambiguïté —, mais jamais le nom traduit : seul l'identifiant
     * de type part vers le client, qui le traduira dans SA langue.</p>
     */
    public void upsert(OWEntity entity) {
        if (entity == null || entity.level().isClientSide()) return;
        UUID id = entity.getUUID();
        UUID owner = entity.getOwnerUUID();
        // Le sous-marin et ses dérivés n'ont jamais eu de repère à l'écran : les inscrire ici les y
        // ferait apparaître dès qu'ils sortent du champ du client, ce qui serait un ajout, pas une
        // correction.
        boolean eligible = entity.isTame() && owner != null
                && entity.isAlive() && !entity.isDeadOrDying() && !entity.isRemoved()
                && !(entity instanceof net.tiew.operationWild.entity.misc.Submarine);
        if (!eligible || !(entity instanceof IOWWaypointEntity w)) {
            remove(id);
            return;
        }

        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        String dimension = entity.level().dimension().location().toString();
        boolean hasCustomName = entity.getCustomName() != null;
        String customName = hasCustomName ? entity.getCustomName().getString() : "";

        Waypoint wp = waypoints.get(id);
        boolean isNew = wp == null;
        if (isNew) {
            wp = new Waypoint();
            waypoints.put(id, wp);
        }

        UUID previousOwner = wp.owner;
        double moved = isNew ? Double.MAX_VALUE
                : Math.sqrt(sqr(wp.x - entity.getX()) + sqr(wp.y - entity.getY()) + sqr(wp.z - entity.getZ()));

        boolean attributesChanged = isNew
                || !owner.equals(wp.owner)
                || !dimension.equals(wp.dimension)
                || hasCustomName != wp.hasCustomName
                || !customName.equals(wp.customName)
                || !String.valueOf(typeId).equals(wp.entityTypeId)
                || wp.fillColor != w.getWaypointFillColor()
                || wp.borderColor != w.getWaypointBorderColor()
                || wp.textColor != w.getWaypointTextColor()
                || wp.entityColor != entity.getEntityColor()
                || wp.iconSize != w.getWaypointIconSize()
                || wp.maxDist != w.getWaypointMaxDistance()
                || wp.minDist != w.getWaypointMinDistance()
                || wp.minOpacity != w.getWaypointMinOpacity()
                || wp.fontScale != w.getWaypointDistanceFontScale();

        wp.owner = owner;
        wp.entityTypeId = String.valueOf(typeId);
        wp.hasCustomName = hasCustomName;
        wp.customName = customName;
        wp.dimension = dimension;
        wp.x = entity.getX();
        wp.y = entity.getY() + entity.getBbHeight() * 0.5;
        wp.z = entity.getZ();
        wp.fillColor = w.getWaypointFillColor();
        wp.borderColor = w.getWaypointBorderColor();
        wp.textColor = w.getWaypointTextColor();
        wp.entityColor = entity.getEntityColor();
        wp.iconSize = w.getWaypointIconSize();
        wp.maxDist = w.getWaypointMaxDistance();
        wp.minDist = w.getWaypointMinDistance();
        wp.minOpacity = w.getWaypointMinOpacity();
        wp.fontScale = w.getWaypointDistanceFontScale();

        if (attributesChanged || moved > SAVE_MOVE_THRESHOLD) setDirty();
        if (attributesChanged || moved > SYNC_MOVE_THRESHOLD) {
            bump(owner);
            if (previousOwner != null && !previousOwner.equals(owner)) bump(previousOwner);
        }
    }

    /** Retire l'entrée d'une créature (mort réelle, suppression, ré-ensauvagement). */
    public void remove(UUID entityId) {
        if (entityId == null) return;
        Waypoint wp = waypoints.remove(entityId);
        if (wp == null) return;
        bump(wp.owner);
        setDirty();
    }

    /**
     * Bascule l'affichage d'un waypoint, à la demande de {@code requester}.
     *
     * <p>L'identifiant de créature vient du client : la propriété est donc revérifiée ici. Sans
     * cela, n'importe quel client pourrait éteindre les repères d'un autre joueur.</p>
     *
     * @return vrai si la bascule a eu lieu.
     */
    public boolean toggle(UUID entityId, UUID requester) {
        Waypoint wp = entityId != null ? waypoints.get(entityId) : null;
        if (wp == null || requester == null || !requester.equals(wp.owner)) return false;
        wp.enabled = !wp.enabled;
        bump(wp.owner);
        setDirty();
        return true;
    }

    private void bump(UUID owner) {
        if (owner == null) return;
        revisions.merge(owner, 1, Integer::sum);
    }

    // ── Accès ────────────────────────────────────────────────────────────────
    public int revisionOf(UUID owner) {
        return owner != null ? revisions.getOrDefault(owner, 0) : 0;
    }

    /** Instantané des waypoints appartenant à {@code owner}, prêt à partir sur le réseau. */
    public List<OWWaypointEntry> entriesFor(UUID owner) {
        List<OWWaypointEntry> out = new ArrayList<>();
        if (owner == null) return out;
        for (Map.Entry<UUID, Waypoint> e : waypoints.entrySet()) {
            Waypoint wp = e.getValue();
            if (!owner.equals(wp.owner)) continue;
            out.add(new OWWaypointEntry(
                    e.getKey(), wp.entityTypeId, wp.hasCustomName, wp.customName, wp.dimension,
                    wp.x, wp.y, wp.z,
                    wp.fillColor, wp.borderColor, wp.textColor, wp.entityColor,
                    wp.iconSize, wp.maxDist, wp.minDist, wp.minOpacity, wp.fontScale,
                    wp.enabled));
        }
        return out;
    }

    private static double sqr(double v) { return v * v; }

    // ── Sérialisation ──────────────────────────────────────────────────────────
    public static OWWaypointData load(CompoundTag tag, HolderLookup.Provider provider) {
        OWWaypointData data = new OWWaypointData();
        ListTag list = tag.getList("waypoints", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag e = list.getCompound(i);
            try {
                UUID id = UUID.fromString(e.getString("id"));
                Waypoint wp = new Waypoint();
                wp.owner = UUID.fromString(e.getString("owner"));
                wp.entityTypeId = e.getString("type");
                wp.hasCustomName = e.getBoolean("hasCustomName");
                wp.customName = e.getString("customName");
                wp.dimension = e.getString("dimension");
                wp.x = e.getDouble("x");
                wp.y = e.getDouble("y");
                wp.z = e.getDouble("z");
                wp.fillColor = e.getInt("fillColor");
                wp.borderColor = e.getInt("borderColor");
                wp.textColor = e.getInt("textColor");
                wp.entityColor = e.getInt("entityColor");
                wp.iconSize = e.getInt("iconSize");
                wp.maxDist = e.getInt("maxDist");
                wp.minDist = e.getFloat("minDist");
                wp.minOpacity = e.getFloat("minOpacity");
                wp.fontScale = e.getFloat("fontScale");
                wp.enabled = !e.contains("enabled") || e.getBoolean("enabled");
                if (wp.dimension.isEmpty()) continue;
                data.waypoints.put(id, wp);
            } catch (IllegalArgumentException ignored) {}
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, Waypoint> en : waypoints.entrySet()) {
            Waypoint wp = en.getValue();
            if (wp.owner == null || wp.dimension.isEmpty()) continue;
            CompoundTag e = new CompoundTag();
            e.putString("id", en.getKey().toString());
            e.putString("owner", wp.owner.toString());
            e.putString("type", wp.entityTypeId);
            e.putBoolean("hasCustomName", wp.hasCustomName);
            e.putString("customName", wp.customName);
            e.putString("dimension", wp.dimension);
            e.putDouble("x", wp.x);
            e.putDouble("y", wp.y);
            e.putDouble("z", wp.z);
            e.putInt("fillColor", wp.fillColor);
            e.putInt("borderColor", wp.borderColor);
            e.putInt("textColor", wp.textColor);
            e.putInt("entityColor", wp.entityColor);
            e.putInt("iconSize", wp.iconSize);
            e.putInt("maxDist", wp.maxDist);
            e.putFloat("minDist", wp.minDist);
            e.putFloat("minOpacity", wp.minOpacity);
            e.putFloat("fontScale", wp.fontScale);
            e.putBoolean("enabled", wp.enabled);
            list.add(e);
        }
        tag.put("waypoints", list);
        return tag;
    }
}
