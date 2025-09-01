package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntity;

public record ConsumeItemPacket(int entityId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ConsumeItemPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "consume_item"));

    public static final StreamCodec<FriendlyByteBuf, ConsumeItemPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, ConsumeItemPacket::entityId,
                    ConsumeItemPacket::new
            );

    @Override
    public CustomPacketPayload.Type<ConsumeItemPacket> type() {
        return TYPE;
    }

    public static void handle(ConsumeItemPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Entity entity = player.level().getEntity(packet.entityId());
                if (entity instanceof OWEntity owEntity) {
                    net.neoforged.neoforge.items.IItemHandler itemHandler = owEntity.getInventory();

                    if (itemHandler != null) {
                        ItemStack foodStack = itemHandler.getStackInSlot(1);

                        if (!foodStack.isEmpty()) {
                            foodStack.shrink(1);

                            owEntity.setFoodCount(foodStack.getCount());

                            owEntity.healWithFavoriteFood(1.5f, owEntity.preferRawMeat(), owEntity.preferCookedMeat());

                            owEntity.playSound(SoundEvents.CAMEL_EAT, 1.0f, 1.0f);

                            if (foodStack.isEmpty()) {
                                owEntity.setFed(false);
                            }
                        }
                    }
                }
            }
        });
    }
}