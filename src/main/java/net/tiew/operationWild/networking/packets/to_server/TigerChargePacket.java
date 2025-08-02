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

public record TigerChargePacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TigerChargePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "tiger_charge"));

    public static final StreamCodec<FriendlyByteBuf, TigerChargePacket> STREAM_CODEC =
            StreamCodec.unit(new TigerChargePacket());

    @Override
    public CustomPacketPayload.Type<TigerChargePacket> type() {
        return TYPE;
    }

    public static void handle(TigerChargePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Entity entity = player.getRootVehicle();

                if (entity != null) {
                    if (entity instanceof TigerEntity tiger) {
                        tiger.setTameJumping(true);
                        tiger.onPlayerJump(90);
                    }
                }
            }
        });
    }
}

