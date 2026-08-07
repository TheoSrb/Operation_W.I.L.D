package net.tiew.operationWild.core;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTribesSavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Qui a posé quel bloc, par niveau.
 *
 * <p>Le mod n'avait aucun moyen de distinguer un bloc de terrain d'un bloc de construction : les
 * destructions d'ultimes rasaient donc les bases aussi bien que les collines. Ce registre existe
 * pour qu'une attaque puisse épargner l'ouvrage de sa propre tribu.</p>
 *
 * <p><b>Ce n'est pas un système de claim.</b> Il ne protège rien de lui-même et n'interdit aucune
 * action de joueur : il répond à une question, {@link #isProtectedFrom}, et laisse chaque attaque
 * décider. Les explosions vanilla, la pioche et le feu continuent d'ignorer complètement ces
 * enregistrements.</p>
 *
 * <p><b>Coût.</b> Une entrée par bloc posé et jamais repris : huit octets de position et un renvoi
 * vers une table de propriétaires, elle-même dédupliquée — un même bâtisseur ne stocke son UUID
 * qu'une fois. Les entrées disparaissent dès que le bloc est cassé, quel que soit le casseur.</p>
 */
public class OWPlacedBlocks extends SavedData {

    public static final String DATA_NAME = "ow_placed_blocks";

    /** Position compactée → indice dans {@link #owners}. */
    private final Map<Long, Integer> placedBy = new HashMap<>();

    /** Bâtisseurs rencontrés, dédupliqués : un UUID de seize octets pour des milliers de blocs. */
    private final Map<UUID, Integer> ownerIndex = new HashMap<>();
    private final Map<Integer, UUID> owners = new HashMap<>();

    public OWPlacedBlocks() {}

    public static OWPlacedBlocks get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(OWPlacedBlocks::new, OWPlacedBlocks::load, null),
                DATA_NAME);
    }

    // ── Écriture ─────────────────────────────────────────────────────────────

    public void record(BlockPos pos, UUID placer) {
        if (placer == null) return;
        int index = ownerIndex.computeIfAbsent(placer, uuid -> {
            int next = owners.size();
            owners.put(next, uuid);
            return next;
        });
        placedBy.put(pos.asLong(), index);
        setDirty();
    }

    public void forget(BlockPos pos) {
        if (placedBy.remove(pos.asLong()) != null) setDirty();
    }

    // ── Lecture ──────────────────────────────────────────────────────────────

    /** UUID du bâtisseur, ou {@code null} si le bloc n'a pas été posé par un joueur. */
    public UUID placerOf(BlockPos pos) {
        Integer index = placedBy.get(pos.asLong());
        return index == null ? null : owners.get(index);
    }

    /**
     * Ce bloc doit-il être épargné par une attaque agissant pour le compte de {@code actingFor} ?
     *
     * <p>Vrai si le bâtisseur est ce joueur lui-même, ou un membre de sa tribu. Un bloc de terrain,
     * ou l'ouvrage d'un inconnu, ne l'est pas — la bête d'un étranger reste libre de le disloquer.</p>
     */
    public static boolean isProtectedFrom(ServerLevel level, BlockPos pos, UUID actingFor) {
        if (actingFor == null) return false;

        UUID placer = get(level).placerOf(pos);
        if (placer == null) return false;
        if (placer.equals(actingFor)) return true;

        MinecraftServer server = level.getServer();
        if (server == null) return false;

        OWTeam tribe = OWTribesSavedData.get(server).findTeamByMember(actingFor);
        return tribe != null && tribe.isMember(placer);
    }

    // ── Sérialisation ────────────────────────────────────────────────────────

    public static OWPlacedBlocks load(CompoundTag tag, HolderLookup.Provider provider) {
        OWPlacedBlocks data = new OWPlacedBlocks();

        ListTag ownerList = tag.getList("owners", Tag.TAG_COMPOUND);
        for (int i = 0; i < ownerList.size(); i++) {
            CompoundTag entry = ownerList.getCompound(i);
            UUID uuid = entry.getUUID("uuid");
            int index = entry.getInt("index");
            data.owners.put(index, uuid);
            data.ownerIndex.put(uuid, index);
        }

        long[] positions = tag.getLongArray("positions");
        int[] indices = tag.getIntArray("indices");
        for (int i = 0; i < positions.length && i < indices.length; i++) {
            data.placedBy.put(positions[i], indices[i]);
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag ownerList = new ListTag();
        for (Map.Entry<Integer, UUID> entry : owners.entrySet()) {
            CompoundTag owner = new CompoundTag();
            owner.putInt("index", entry.getKey());
            owner.putUUID("uuid", entry.getValue());
            ownerList.add(owner);
        }
        tag.put("owners", ownerList);

        long[] positions = new long[placedBy.size()];
        int[] indices = new int[placedBy.size()];
        int i = 0;
        for (Map.Entry<Long, Integer> entry : placedBy.entrySet()) {
            positions[i] = entry.getKey();
            indices[i] = entry.getValue();
            i++;
        }
        tag.putLongArray("positions", positions);
        tag.putIntArray("indices", indices);

        return tag;
    }
}
