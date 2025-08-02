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
import net.tiew.operationWild.entity.custom.living.TigerEntity;
import net.tiew.operationWild.entity.custom.vehicle.SeaBugEntity;

public record ClientPressedRightClick() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientPressedRightClick> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "client_pressed_tight_click"));

    public static final StreamCodec<FriendlyByteBuf, ClientPressedRightClick> STREAM_CODEC =
            StreamCodec.unit(new ClientPressedRightClick());

    @Override
    public CustomPacketPayload.Type<ClientPressedRightClick> type() {
        return TYPE;
    }

    public static void handle(ClientPressedRightClick packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Entity entity = player.getRootVehicle();

                if (entity != null) {
                    if (entity instanceof OWEntity owEntities) {
                        if (owEntities instanceof TigerEntity tiger) {
                            if (!tiger.isTameJumping()) {
                                tiger.setTameJumping(true);
                            }
                        }
                        if (owEntities instanceof SeaBugEntity seaBug) {
                            if (seaBug.level().getRawBrightness(seaBug.blockPosition(), 0) <= 3 && !seaBug.isOff()) {
                                seaBug.setLightOn(!seaBug.isLightOn());
                            }
                        }
                    }
                }
            }
        });
    }
}
