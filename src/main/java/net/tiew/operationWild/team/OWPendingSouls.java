package net.tiew.operationWild.team;

import com.mojang.serialization.Dynamic;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Âmes en attente de remise à un joueur hors ligne.
 *
 * <p>Sert un cas précis : une créature meurt en arène et lâche son âme alors que son propriétaire
 * n'est pas connecté. On ne peut ni la lui donner, ni la laisser sur place — <b>l'arène est
 * inaccessible</b>, une âme abandonnée là-bas est donc perdue autant que si on l'avait détruite, en
 * encombrant la dimension par-dessus le marché. Elle est mise de côté ici et remise à la connexion
 * suivante.</p>
 */
public class OWPendingSouls extends SavedData {

    public static final String DATA_NAME = "ow_pending_souls";

    private final Map<UUID, List<ItemStack>> pending = new HashMap<>();

    public OWPendingSouls() {}

    public static OWPendingSouls get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(OWPendingSouls::new, OWPendingSouls::load, null), DATA_NAME);
    }

    /** Met une âme de côté pour {@code owner}. */
    public void stash(UUID owner, ItemStack stack) {
        if (owner == null || stack == null || stack.isEmpty()) return;
        pending.computeIfAbsent(owner, k -> new ArrayList<>()).add(stack.copy());
        setDirty();
    }

    /**
     * Remet à {@code player} tout ce qui l'attendait. Ce qui ne tient pas dans l'inventaire est
     * lâché à ses pieds plutôt que perdu.
     */
    public void deliver(ServerPlayer player) {
        List<ItemStack> stacks = pending.remove(player.getUUID());
        if (stacks == null || stacks.isEmpty()) return;
        for (ItemStack stack : stacks) {
            if (!player.getInventory().add(stack.copy())) player.drop(stack.copy(), false);
        }
        setDirty();
        player.sendSystemMessage(net.minecraft.network.chat.Component
                .translatable("owteams.arena.soul_returned")
                .setStyle(net.minecraft.network.chat.Style.EMPTY.withColor(0x86DBFF)));
    }

    // ── Sérialisation ────────────────────────────────────────────────────────
    public static OWPendingSouls load(CompoundTag tag, HolderLookup.Provider provider) {
        OWPendingSouls data = new OWPendingSouls();
        ListTag owners = tag.getList("owners", Tag.TAG_COMPOUND);
        for (int i = 0; i < owners.size(); i++) {
            CompoundTag entry = owners.getCompound(i);
            UUID owner;
            try { owner = UUID.fromString(entry.getString("uuid")); }
            catch (IllegalArgumentException e) { continue; }

            List<ItemStack> stacks = new ArrayList<>();
            ListTag items = entry.getList("items", Tag.TAG_COMPOUND);
            for (int j = 0; j < items.size(); j++) {
                // Un item enregistré peut devenir invalide (mod retiré, renommage) : on ignore
                // l'entrée plutôt que de faire échouer le chargement de toutes les autres.
                ItemStack.CODEC
                        .parse(new Dynamic<>(provider.createSerializationContext(NbtOps.INSTANCE), items.get(j)))
                        .result().ifPresent(stacks::add);
            }
            if (!stacks.isEmpty()) data.pending.put(owner, stacks);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag owners = new ListTag();
        for (Map.Entry<UUID, List<ItemStack>> e : pending.entrySet()) {
            ListTag items = new ListTag();
            for (ItemStack stack : e.getValue()) {
                ItemStack.CODEC
                        .encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), stack)
                        .result().ifPresent(items::add);
            }
            if (items.isEmpty()) continue;
            CompoundTag entry = new CompoundTag();
            entry.putString("uuid", e.getKey().toString());
            entry.put("items", items);
            owners.add(entry);
        }
        tag.put("owners", owners);
        return tag;
    }
}
