package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.behavior.lion.LionClan;

public record LionClanSyncPacket(int lionEntityId, LionClan clan, int clanId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<LionClanSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "lion_clan_sync"));

    public static final StreamCodec<FriendlyByteBuf, LionClanSyncPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public void encode(FriendlyByteBuf buf, LionClanSyncPacket packet) {
                    buf.writeInt(packet.lionEntityId);
                    buf.writeInt(packet.clanId);
                }

                @Override
                public LionClanSyncPacket decode(FriendlyByteBuf buf) {
                    int lionEntityId = buf.readInt();
                    int clanId = buf.readInt();

                    return new LionClanSyncPacket(lionEntityId, null, clanId);
                }
            };

    @Override
    public CustomPacketPayload.Type<LionClanSyncPacket> type() {
        return TYPE;
    }

    public static void handle(LionClanSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() != null) {
                Entity entity = context.player().level().getEntity(packet.lionEntityId);

                if (entity instanceof LionEntity lion) {
                    lion.myClanId = packet.clanId;
                    lion.needsClanReconstruction = true;
                }
            }
        });
    }
}