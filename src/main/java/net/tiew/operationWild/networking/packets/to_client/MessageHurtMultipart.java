package net.tiew.operationWild.networking.packets.to_client;

// !! A enregistrer dans OWNetworkHandler (cote to_client) !!

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.BoaTailPart;

/**
 * Copie fonctionnelle de MessageHurtMultipart (Alex's Mobs), au format reseau OW.
 * Propage l'effet de degats (clignotement rouge) du parent vers un segment de queue.
 */
public record MessageHurtMultipart(int partId, int parentId) implements CustomPacketPayload {

    public static final Type<MessageHurtMultipart> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "hurt_multipart"));

    public static final StreamCodec<ByteBuf, MessageHurtMultipart> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, MessageHurtMultipart::partId,
                    ByteBufCodecs.INT, MessageHurtMultipart::parentId,
                    MessageHurtMultipart::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(MessageHurtMultipart packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity part = net.tiew.operationWild.client.OWClientHooks.clientEntity(packet.partId());
            Entity parent = net.tiew.operationWild.client.OWClientHooks.clientEntity(packet.parentId());
            if (part instanceof BoaTailPart boaPart && parent instanceof LivingEntity livingParent) {
                // Cote client : reproduit l'effet de degats sur le segment.
                boaPart.hurtTime = livingParent.hurtTime > 0 ? livingParent.hurtTime : 10;
                boaPart.hurtDuration = 10;
                boaPart.deathTime = livingParent.deathTime;
            }
        });
    }
}