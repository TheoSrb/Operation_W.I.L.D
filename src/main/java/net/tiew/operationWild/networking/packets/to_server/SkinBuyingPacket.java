package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;

public record SkinBuyingPacket(int price, int skinIndex) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SkinBuyingPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "skin_buying"));

    public static final StreamCodec<FriendlyByteBuf, SkinBuyingPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, SkinBuyingPacket::price,
                    ByteBufCodecs.INT, SkinBuyingPacket::skinIndex,
                    SkinBuyingPacket::new
            );

    @Override
    public CustomPacketPayload.Type<SkinBuyingPacket> type() {
        return TYPE;
    }

    public static void handle(SkinBuyingPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            Entity entity = player.getRootVehicle();
            if (!(entity instanceof OWEntity owEntity)) return;

            // Seul le propriétaire / un membre de sa tribu peut acheter un skin pour ce pet.
            if (!owEntity.canBeControlledBy(player)) return;

            // Skin déjà débloqué : on ne débite pas.
            if (owEntity.isSkinUnlocked(packet.skinIndex())) return;

            // La monnaie "Pièces Sauvages" appartient au joueur (porte-monnaie partagé entre tous ses pets).
            if (!net.tiew.operationWild.core.OWCurrency.spendWildCoins(player, packet.price())) return;
            net.tiew.operationWild.core.OWCurrency.syncWildCoins(player);

            // Le déblocage vit sur le pet (serveur-autoritaire) et se synchronise seul à tous les clients.
            owEntity.unlockSkin(packet.skinIndex());
        });
    }
}