package net.tiew.operationWild.networking.packets.to_server;

// !! À enregistrer dans OWNetworkHandler (côté to_server) !!

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.core.OWArena;
import net.tiew.operationWild.core.OWCurrency;
import net.tiew.operationWild.core.OWReputation;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.ArenaChestRewardPacket;
import net.tiew.operationWild.team.OWReputationData;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTribeManager;
import net.tiew.operationWild.team.OWTribesSavedData;

import java.util.ArrayList;
import java.util.List;

/**
 * Un membre ouvre <b>son</b> coffre d'arène. Entièrement serveur-autoritatif : le client ne fait que
 * demander, le serveur vérifie qu'un coffre est réellement dû, tire le butin, le remet, puis renvoie
 * le contenu pour l'animation d'ouverture.
 */
public record ClaimArenaChestPacket(boolean tribeChest) implements CustomPacketPayload {

    public static final Type<ClaimArenaChestPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "claim_arena_chest"));

    public static final StreamCodec<ByteBuf, ClaimArenaChestPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> net.minecraft.network.codec.ByteBufCodecs.BOOL.encode(buf, p.tribeChest()),
            buf -> new ClaimArenaChestPacket(net.minecraft.network.codec.ByteBufCodecs.BOOL.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(ClaimArenaChestPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp)) return;
            MinecraftServer server = sp.getServer();
            if (server == null || !(sp.level() instanceof ServerLevel level)) return;
            OWTribesSavedData data = OWTribesSavedData.get(server);

            OWTeam team = data.findTeamByMember(sp.getUUID());
            if (team == null || !team.isArenaAccepted()) return;
            int reputation = OWReputation.compute(OWReputationData.get(server), team);

            // Consomme le coffre AVANT de tirer le butin : un double-clic ne peut pas doubler la mise.
            OWArena.Chest chest;
            if (packet.tribeChest()) {
                // Le rang minimal est revérifié ici : le client peut demander n'importe quoi.
                if (!OWArena.tribeChestUnlocked(reputation)) return;
                if (!team.claimTribeChest(sp.getUUID())) return;
                chest = OWArena.Chest.TRIBE;
            } else {
                if (!team.claimArenaChest(sp.getUUID())) return;
                chest = OWArena.chestForReputation(reputation);
            }
            data.putTribe(team);

            List<ItemStack> loot = rollLoot(server, level, sp, chest);
            for (ItemStack stack : loot) {
                if (!sp.getInventory().add(stack.copy())) sp.drop(stack.copy(), false);
            }

            int coins = chest.rollCoins(level.getRandom());
            OWCurrency.grantWildCoins(sp, coins);

            OWNetworkHandler.sendToClient(ArenaChestRewardPacket.of(chest, coins, loot), sp);
            // Le compteur de coffres réclamés a changé : tous les membres en ligne se resynchronisent.
            OWTribeManager.syncTribeToOnlineMembers(server, team);
        });
    }

    /** Tire la table de butin {@code ow:chests/arena_*} du palier. Liste vide si la table est absente. */
    private static List<ItemStack> rollLoot(MinecraftServer server, ServerLevel level,
                                            ServerPlayer player, OWArena.Chest chest) {
        try {
            LootTable table = server.reloadableRegistries().getLootTable(chest.lootTable());
            if (table == LootTable.EMPTY) return new ArrayList<>();
            LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.ORIGIN, player.position())
                    .withParameter(LootContextParams.THIS_ENTITY, player)
                    .withLuck(player.getLuck())
                    .create(LootContextParamSets.CHEST);
            return new ArrayList<>(table.getRandomItems(params));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
