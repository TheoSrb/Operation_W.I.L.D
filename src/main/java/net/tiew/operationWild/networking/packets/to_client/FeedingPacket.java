package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.event.OWFeedingEffects;

public record FeedingPacket(int entityId, int ticks, int itemId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<FeedingPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "feeding"));

    public static final StreamCodec<FriendlyByteBuf, FeedingPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, FeedingPacket::entityId,
                    ByteBufCodecs.INT, FeedingPacket::ticks,
                    ByteBufCodecs.INT, FeedingPacket::itemId,
                    FeedingPacket::new
            );

    public static FeedingPacket of(LivingEntity target, int ticks, ItemStack food) {
        return new FeedingPacket(target.getId(), ticks, BuiltInRegistries.ITEM.getId(food.getItem()));
    }

    @Override
    public CustomPacketPayload.Type<FeedingPacket> type() {
        return TYPE;
    }

    public static void handle(FeedingPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) return;

            Entity entity = minecraft.level.getEntity(packet.entityId());
            if (!(entity instanceof LivingEntity guest)) return;

            Item item = BuiltInRegistries.ITEM.byId(packet.itemId());
            OWFeedingEffects.begin(guest, packet.ticks(), new ItemStack(item));
        });
    }
}
