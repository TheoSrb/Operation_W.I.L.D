package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;

/**
 * Envoyé au rider du Boa quand un « tir au cœur » (passif Vision Thermique) touche une entité,
 * pour déclencher la petite animation du cœur (grossit + devient gris, puis revient à la normale).
 */
public record HeartShotPacket(int entityId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<HeartShotPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "heart_shot"));

    public static final StreamCodec<FriendlyByteBuf, HeartShotPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, HeartShotPacket::entityId,
                    HeartShotPacket::new
            );

    @Override
    public CustomPacketPayload.Type<HeartShotPacket> type() {
        return TYPE;
    }

    public static void handle(HeartShotPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> net.tiew.operationWild.client.OWClientHooks.triggerHeartHit(packet.entityId()));
    }
}
