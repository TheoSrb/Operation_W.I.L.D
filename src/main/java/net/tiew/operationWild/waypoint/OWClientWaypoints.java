package net.tiew.operationWild.waypoint;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Miroir client des waypoints du monde, alimenté par {@link net.tiew.operationWild.networking.packets.to_client.SyncWaypointsPacket}.
 *
 * <p>Purement volatile : le client n'écrit plus rien sur disque. Quitter une partie vide cette
 * table, et la partie suivante la remplit depuis SA propre sauvegarde — c'est ce qui empêche
 * définitivement les repères d'un monde de reparaître dans un autre.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class OWClientWaypoints {

    private OWClientWaypoints() {}

    private static final Map<UUID, OWWaypointEntry> entries = new LinkedHashMap<>();

    public static void set(List<OWWaypointEntry> list) {
        entries.clear();
        for (OWWaypointEntry e : list) entries.put(e.entityUuid(), e);
    }

    public static Collection<OWWaypointEntry> all() {
        return entries.values();
    }

    public static void clear() {
        entries.clear();
    }

    public static boolean isEnabled(UUID entityUuid) {
        OWWaypointEntry e = entries.get(entityUuid);
        return e == null || e.enabled();
    }

    /**
     * Bascule locale immédiate, en attendant la confirmation du serveur.
     *
     * <p>Le serveur reste seul juge : sa prochaine synchronisation écrase cette valeur. Elle n'est
     * là que pour que la case de l'écran d'options réponde au clic sans attendre l'aller-retour.</p>
     */
    public static void toggleLocally(UUID entityUuid) {
        OWWaypointEntry e = entries.get(entityUuid);
        if (e != null) entries.put(entityUuid, e.withEnabled(!e.enabled()));
    }

    /**
     * Nom affiché : le nom personnalisé s'il y en a un, sinon le nom d'espèce traduit dans la langue
     * du joueur — que seul le client peut résoudre.
     */
    public static String displayName(OWWaypointEntry entry) {
        if (entry.hasCustomName()) return entry.customName();
        ResourceLocation id = ResourceLocation.tryParse(entry.entityTypeId());
        if (id == null) return "";
        Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id);
        return type.map(t -> t.getDescription().getString()).orElse("");
    }
}
