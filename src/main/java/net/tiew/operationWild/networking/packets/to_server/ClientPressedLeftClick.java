package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;

public record ClientPressedLeftClick() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientPressedLeftClick> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "client_pressed_left_click"));

    public static final StreamCodec<FriendlyByteBuf, ClientPressedLeftClick> STREAM_CODEC =
            StreamCodec.unit(new ClientPressedLeftClick());

    @Override
    public CustomPacketPayload.Type<ClientPressedLeftClick> type() {
        return TYPE;
    }

    public static void handle(ClientPressedLeftClick packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Entity entity = player.getRootVehicle();

                if (entity != null) {
                    if (entity instanceof OWEntity owEntity) {
                        if (owEntity.getVitalEnergy() > (owEntity.getMaxVitalEnergy() - 15)) return;

                        owEntity.setAttacking(true);
                        owEntity.setVitalEnergy(owEntity.getVitalEnergy() + 15);
                    }
                }
            }
        });
    }
}
