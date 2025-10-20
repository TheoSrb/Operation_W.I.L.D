package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.sound.OWSounds;

public record CrocodileRightClickPacket(boolean isRightClickDown) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CrocodileRightClickPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "crocodile_right_click"));

    public static final StreamCodec<FriendlyByteBuf, CrocodileRightClickPacket> STREAM_CODEC =
            StreamCodec.composite(
                    StreamCodec.of(
                            FriendlyByteBuf::writeBoolean,
                            FriendlyByteBuf::readBoolean
                    ),
                    CrocodileRightClickPacket::isRightClickDown,
                    CrocodileRightClickPacket::new
            );

    @Override
    public CustomPacketPayload.Type<CrocodileRightClickPacket> type() {
        return TYPE;
    }

    private static float clickCounter = 0;

    public static void handle(CrocodileRightClickPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player != null) {
                LivingEntity entity = (LivingEntity) player.getVehicle();

                if (entity instanceof CrocodileEntity crocodile && crocodile.isSaddled() && !crocodile.isInWater() && !crocodile.isCombo()) {
                    if (packet.isRightClickDown()) {
                        if (clickCounter < 60) {
                            clickCounter += 0.5f;
                        }

                        crocodile.setChargingMouth(true);
                        crocodile.setChargingMouthTimer(clickCounter);
                    } else {
                        if (clickCounter >= 30) {
                            crocodile.crocodileBehaviorHandler.makeBigHurt(crocodile.getDamage() * (crocodile.getChargingMouthTimer() / 60), OWSounds.CROCODILE_MOUTH_CRUSH.get(), 3.0f, 2.0f, 2.25f);
                        } else {
                            crocodile.setChargingMouth(false);
                            crocodile.setChargingMouthTimer(0);
                        }
                        clickCounter = 0;
                    }
                }
            }
        });
    }
}