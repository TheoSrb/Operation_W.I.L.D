package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.ChameleonEntity;

public record ChameleonUtilsSendToClient(
        int entityId,
        ResourceLocation camouflageTexture,
        ResourceLocation previousCamouflageTexture,
        int camouflageTimer,
        int fadeTimer,
        boolean isTransitioning,
        int climbingTimer,
        boolean isStopingClimbing
) implements CustomPacketPayload {

    public static final Type<ChameleonUtilsSendToClient> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "chameleon_utils_send_to_client"));

    public static final StreamCodec<FriendlyByteBuf, ChameleonUtilsSendToClient> STREAM_CODEC =
            new StreamCodec<FriendlyByteBuf, ChameleonUtilsSendToClient>() {
                @Override
                public void encode(FriendlyByteBuf buf, ChameleonUtilsSendToClient packet) {
                    buf.writeInt(packet.entityId());

                    buf.writeBoolean(packet.camouflageTexture() != null);
                    if (packet.camouflageTexture() != null) {
                        buf.writeResourceLocation(packet.camouflageTexture());
                    }

                    buf.writeBoolean(packet.previousCamouflageTexture() != null);
                    if (packet.previousCamouflageTexture() != null) {
                        buf.writeResourceLocation(packet.previousCamouflageTexture());
                    }

                    buf.writeInt(packet.camouflageTimer());
                    buf.writeInt(packet.fadeTimer());
                    buf.writeBoolean(packet.isTransitioning());
                    buf.writeInt(packet.climbingTimer());
                    buf.writeBoolean(packet.isStopingClimbing());
                }

                @Override
                public ChameleonUtilsSendToClient decode(FriendlyByteBuf buf) {
                    int entityId = buf.readInt();

                    boolean hasTexture = buf.readBoolean();
                    ResourceLocation texture = hasTexture ? buf.readResourceLocation() : null;

                    boolean hasPreviousTexture = buf.readBoolean();
                    ResourceLocation previousTexture = hasPreviousTexture ? buf.readResourceLocation() : null;

                    int camouflageTimer = buf.readInt();
                    int fadeTimer = buf.readInt();
                    boolean isTransitioning = buf.readBoolean();

                    int climbingTimer = buf.readInt();
                    boolean isStopingClimbing = buf.readBoolean();

                    return new ChameleonUtilsSendToClient(
                            entityId,
                            texture,
                            previousTexture,
                            camouflageTimer,
                            fadeTimer,
                            isTransitioning,
                            climbingTimer,
                            isStopingClimbing
                    );
                }
            };

    @Override
    public Type<ChameleonUtilsSendToClient> type() {
        return TYPE;
    }

    public static void handle(ChameleonUtilsSendToClient packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId());
            if (entity instanceof ChameleonEntity chameleon) {
                chameleon.CAMOUFLAGE_TEXTURE = packet.camouflageTexture;
                chameleon.PREVIOUS_CAMOUFLAGE_TEXTURE = packet.previousCamouflageTexture;
                chameleon.camouflageTimer = packet.camouflageTimer;
                chameleon.fadeTimer = packet.fadeTimer;
                chameleon.isTransitioning = packet.isTransitioning;
                chameleon.climbingTimer = packet.climbingTimer;
                chameleon.isStopingClimbing = packet.isStopingClimbing;
            }
        });
    }
}