package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.minecraft.world.entity.EntityType;

public record AddEntityToManuscriptPacket(String entityName) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<AddEntityToManuscriptPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "add_entity_to_manuscript"));

    public static final StreamCodec<FriendlyByteBuf, AddEntityToManuscriptPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            AddEntityToManuscriptPacket::entityName,
            AddEntityToManuscriptPacket::new
    );

    @Override
    public CustomPacketPayload.Type<AddEntityToManuscriptPacket> type() {
        return TYPE;
    }

    public static void handle(AddEntityToManuscriptPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();

            EntityType<? extends OWEntity> entityType = OWEntityRegistry.getEntityTypeFromName(packet.entityName());
            if (entityType != null) {
                OperationWild.addEntityToManuscript(entityType, OperationWild.getMaxPageForEntityInManuscript(entityType), player);
                player.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
            }
        });
    }
}