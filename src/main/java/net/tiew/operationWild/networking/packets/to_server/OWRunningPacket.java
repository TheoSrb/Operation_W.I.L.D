package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.core.OWUtils;

public record OWRunningPacket(boolean isSprintKeyDown) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OWRunningPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ow_running_packet"));

    public static final StreamCodec<FriendlyByteBuf, OWRunningPacket> STREAM_CODEC =
            StreamCodec.composite(
                    StreamCodec.of(
                            (buf, value) -> buf.writeBoolean(value),
                            buf -> buf.readBoolean()
                    ),
                    OWRunningPacket::isSprintKeyDown,
                    OWRunningPacket::new
            );

    @Override
    public CustomPacketPayload.Type<OWRunningPacket> type() {
        return TYPE;
    }

    public static boolean $$0 = true;

    public static void handle(OWRunningPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Entity entity = player.getRootVehicle();

                if (entity instanceof OWEntity owEntity) {
                    float vitalEnergyRestant = (float) (owEntity.getVitalEnergy() / owEntity.getMaxVitalEnergy());

                    if (owEntity.hasReachedEnergyLimit() && vitalEnergyRestant <= 0.8f) {
                        owEntity.setHasReachedEnergyLimit(false);
                    }

                    boolean canSprint = true;
                    if (owEntity.hasReachedEnergyLimit()) {
                        canSprint = false;
                    }

                    if (vitalEnergyRestant >= 1.0f) {
                        owEntity.setHasReachedEnergyLimit(true);
                        canSprint = false;

                        if ($$0) {
                            OWUtils.showMessage(player, Component.translatable("tooltip.entityIsTired", Component.translatable("entity.ow." + entity.getClass().getSimpleName().split("Entity")[0].toLowerCase())), 0xd2c7e8, false);
                            $$0 = false;
                        }
                    }

                    if (canSprint && entity.getRandom().nextInt(3) == 0) $$0 = true;

                    owEntity.canShowVitalEnergyLack = !canSprint;

                    if (canSprint && packet.isSprintKeyDown() && owEntity.getVitalEnergy() < owEntity.getMaxVitalEnergy() && owEntity.isSaddled()
                            && owEntity.getControllingPassenger() != null && owEntity.getControllingPassenger().zza != 0) {
                        owEntity.setRunning(true);
                        owEntity.setAcceleration(owEntity.getAcceleration() + 1);
                    } else {
                        owEntity.setRunning(false);
                        owEntity.setAcceleration(0);
                    }
                }
            }
        });
    }
}