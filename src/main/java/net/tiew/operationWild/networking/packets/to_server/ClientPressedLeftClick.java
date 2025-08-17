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
import net.tiew.operationWild.utils.OWUtils;

public record ClientPressedLeftClick() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientPressedLeftClick> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "client_pressed_left_click"));

    public static final StreamCodec<FriendlyByteBuf, ClientPressedLeftClick> STREAM_CODEC =
            StreamCodec.unit(new ClientPressedLeftClick());

    @Override
    public CustomPacketPayload.Type<ClientPressedLeftClick> type() {
        return TYPE;
    }

    public static boolean $$0 = true;

    public static void handle(ClientPressedLeftClick packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Entity entity = player.getRootVehicle();

                if (entity != null) {
                    if (entity instanceof OWEntity owEntity) {
                        float vitalEnergyRestant = (float) (owEntity.getVitalEnergy() / owEntity.getMaxVitalEnergy());

                        if (owEntity.hasReachedAttackEnergyLimit() && vitalEnergyRestant <= 0.8f) {
                            owEntity.setHasReachedAttackEnergyLimit(false);
                        }

                        boolean canAttack = true;
                        if (owEntity.hasReachedAttackEnergyLimit()) {
                            canAttack = false;
                        }

                        if (vitalEnergyRestant >= 1.0f) {
                            owEntity.setHasReachedAttackEnergyLimit(true);
                            canAttack = false;

                            if ($$0) {
                                OWUtils.showMessage(player, Component.translatable("tooltip.entityIsTired", Component.translatable("entity.ow." + entity.getClass().getSimpleName().split("Entity")[0].toLowerCase())), 0xd2c7e8, true);
                                $$0 = false;
                            }
                        }

                        if (canAttack && entity.getRandom().nextInt(3) == 0) $$0 = true;

                        if (canAttack && owEntity.getVitalEnergy() <= (owEntity.getMaxVitalEnergy() - 15)) {
                            owEntity.setAttacking(true);
                            owEntity.setVitalEnergy(owEntity.getVitalEnergy() + 15);
                        }
                    }
                }
            }
        });
    }
}
