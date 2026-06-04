package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.component.OWDataComponentTypes;
import net.tiew.operationWild.component.SoulData;
import net.tiew.operationWild.entity.resurrection.ResurrectionRitualManager;
import net.tiew.operationWild.item.OWItems;

/** Demande de lancement du Rituel de Communion (validée serveur). */
public record StartRitualPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<StartRitualPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "start_ritual"));

    public static final StreamCodec<FriendlyByteBuf, StartRitualPacket> STREAM_CODEC =
            StreamCodec.unit(new StartRitualPacket());

    @Override
    public CustomPacketPayload.Type<StartRitualPacket> type() {
        return TYPE;
    }

    public static void handle(StartRitualPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            // Trouve l'Âme tenue (main puis main secondaire).
            ItemStack soulStack = player.getItemInHand(InteractionHand.MAIN_HAND);
            SoulData data = soulStack.get(OWDataComponentTypes.SOUL_DATA.get());
            if (data == null || data.isEmpty()) {
                soulStack = player.getItemInHand(InteractionHand.OFF_HAND);
                data = soulStack.get(OWDataComponentTypes.SOUL_DATA.get());
            }
            if (data == null || data.isEmpty()) return;

            // Propriétaire d'origine uniquement.
            if (!data.ownerUuid().equals(player.getUUID())) {
                feedback(player, "ritual.ow.deny.not_owner", 0xD94747);
                return;
            }
            // Amulette portée (slot torse).
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chest.isEmpty() || !chest.is(OWItems.RESURRECTION_AMULET.get())) {
                feedback(player, "ritual.ow.deny.no_amulet", 0xD94747);
                return;
            }
            // Assez de niveaux d'XP.
            if (player.experienceLevel < data.xpLevelCost()) {
                feedback(player, "ritual.ow.deny.no_xp", 0xD94747);
                return;
            }
            // Pas déjà un rituel en cours.
            if (ResurrectionRitualManager.hasActiveRitual(player.getUUID())) {
                feedback(player, "ritual.ow.deny.busy", 0xD94747);
                return;
            }

            ResurrectionRitualManager.startRitual(player, data);
        });
    }

    private static void feedback(ServerPlayer player, String key, int color) {
        player.displayClientMessage(Component.translatable(key).setStyle(Style.EMPTY.withColor(color)), true);
    }
}
