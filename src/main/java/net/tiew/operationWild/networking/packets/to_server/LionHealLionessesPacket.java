package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;

import java.util.List;

public record LionHealLionessesPacket(int lionId, boolean isShortEffect) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<LionHealLionessesPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "lion_heal_lionesses"));

    public static final StreamCodec<FriendlyByteBuf, LionHealLionessesPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    LionHealLionessesPacket::lionId,
                    ByteBufCodecs.BOOL,
                    LionHealLionessesPacket::isShortEffect,
                    LionHealLionessesPacket::new
            );

    @Override
    public CustomPacketPayload.Type<LionHealLionessesPacket> type() {
        return TYPE;
    }

    public static void handle(LionHealLionessesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Entity entity = player.level().getEntity(packet.lionId);

                if (entity instanceof LionEntity lion && lion.isMale() && lion.clan != null) {
                    List<LionEntity> lionesses = lion.clan.getLionesses();

                    if (lionesses != null) {
                        for (LionEntity lioness : lionesses) {
                            if (packet.isShortEffect()) {
                                lioness.heal(0.1f);
                            } else {
                                lioness.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 1));
                            }
                        }
                    }
                }
            }
        });
    }
}