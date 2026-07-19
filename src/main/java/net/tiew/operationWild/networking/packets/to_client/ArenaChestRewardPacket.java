package net.tiew.operationWild.networking.packets.to_client;

// !! À enregistrer dans OWNetworkHandler (côté to_client) !!

import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.client.OWClientArenaReward;
import net.tiew.operationWild.core.OWArena;

import java.util.ArrayList;
import java.util.List;

/**
 * Résultat serveur de l'ouverture d'un coffre d'arène, poussé au joueur pour alimenter l'animation
 * d'ouverture. <b>Purement cosmétique</b> : les items et les Pièces Sauvages ont déjà été remis
 * côté serveur — ce paquet ne sert qu'à savoir <i>quoi afficher</i>.
 *
 * <p>Les items voyagent sous forme {@code id + quantité} plutôt qu'en {@code ItemStack} sérialisé :
 * le butin d'arène n'a aucun composant custom, et cela évite d'exiger un {@code RegistryFriendlyByteBuf}.</p>
 */
public record ArenaChestRewardPacket(
        int chestOrdinal, int coins, List<String> itemIds, List<Integer> itemCounts
) implements CustomPacketPayload {

    public static final Type<ArenaChestRewardPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "arena_chest_reward"));

    public static final StreamCodec<ByteBuf, ArenaChestRewardPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                ByteBufCodecs.INT.encode(buf, p.chestOrdinal());
                ByteBufCodecs.INT.encode(buf, p.coins());
                ByteBufCodecs.INT.encode(buf, p.itemIds().size());
                for (int i = 0; i < p.itemIds().size(); i++) {
                    ByteBufCodecs.STRING_UTF8.encode(buf, p.itemIds().get(i));
                    ByteBufCodecs.INT.encode(buf, p.itemCounts().get(i));
                }
            },
            buf -> {
                int tier = ByteBufCodecs.INT.decode(buf);
                int coins = ByteBufCodecs.INT.decode(buf);
                int n = ByteBufCodecs.INT.decode(buf);
                List<String> ids = new ArrayList<>(n);
                List<Integer> counts = new ArrayList<>(n);
                for (int i = 0; i < n; i++) { ids.add(ByteBufCodecs.STRING_UTF8.decode(buf)); counts.add(ByteBufCodecs.INT.decode(buf)); }
                return new ArenaChestRewardPacket(tier, coins, ids, counts);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    /** Construit le paquet depuis le butin réellement octroyé. */
    public static ArenaChestRewardPacket of(OWArena.Chest chest, int coins, List<ItemStack> loot) {
        List<String> ids = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (ItemStack stack : loot) {
            if (stack == null || stack.isEmpty()) continue;
            ids.add(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            counts.add(stack.getCount());
        }
        return new ArenaChestRewardPacket(chest.ordinal(), coins, ids, counts);
    }

    public static void handle(ArenaChestRewardPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            OWArena.Chest chest = OWArena.Chest.byOrdinal(packet.chestOrdinal());
            List<ItemStack> stacks = new ArrayList<>();
            for (int i = 0; i < packet.itemIds().size(); i++) {
                ResourceLocation id = ResourceLocation.tryParse(packet.itemIds().get(i));
                if (id == null) continue;
                var item = BuiltInRegistries.ITEM.get(id);
                if (item == null) continue;
                stacks.add(new ItemStack(item, packet.itemCounts().get(i)));
            }
            OWClientArenaReward.deliver(chest, packet.coins(), stacks);
        });
    }
}
