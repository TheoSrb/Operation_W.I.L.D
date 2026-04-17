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
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.SkinUnlockedPacket;

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
            if (owEntity.getPrestigeLevel() < packet.price()) return;

            owEntity.setPrestigeLevel(owEntity.getPrestigeLevel() - packet.price());
            OWNetworkHandler.sendToClient(new SkinUnlockedPacket(owEntity.getUUID(), packet.skinIndex()), player);
        });
    }
}