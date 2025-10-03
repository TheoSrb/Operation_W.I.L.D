package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.component.OWDataComponentTypes;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.AddEntityToManuscriptPacket;

public record CheckManuscriptEntityPacket(String entityName, boolean hasEntity) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CheckManuscriptEntityPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "check_manuscript_entity"));

    public static final StreamCodec<FriendlyByteBuf, CheckManuscriptEntityPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            CheckManuscriptEntityPacket::entityName,
            ByteBufCodecs.BOOL,
            CheckManuscriptEntityPacket::hasEntity,
            CheckManuscriptEntityPacket::new
    );

    @Override
    public CustomPacketPayload.Type<CheckManuscriptEntityPacket> type() {
        return TYPE;
    }

    public static void handle(CheckManuscriptEntityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);

                if (stack.isEmpty()) {
                    stack = player.getItemInHand(InteractionHand.OFF_HAND);
                }

                Component fragmentEntityComponent = stack.get(OWDataComponentTypes.MANUSCRIPT_FRAGMENT_ENTITY.get());
                if (fragmentEntityComponent != null) {
                    String entityName = fragmentEntityComponent.getString();

                    if (entityName.equals(packet.entityName())) {
                        if (packet.hasEntity()) {
                        } else {
                            EntityType<? extends OWEntity> entityType = OWEntityRegistry.getEntityTypeFromName(entityName);
                            if (entityType != null) {
                                OWNetworkHandler.sendToClient(new AddEntityToManuscriptPacket(entityName), player);
                                stack.shrink(1);
                            }
                        }
                    }
                }
            }
        });
    }
}