package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;

public record OWQuestProgressToClient(
        int entityId,
        int quest0Progression, int quest1Progression, int quest2Progression, int quest3Progression,
        int quest4Progression, int quest5Progression, int quest6Progression, int quest7Progression,
        int quest8Progression, int quest9Progression, int quest10Progression,
        boolean quest0isLocked, boolean quest1isLocked, boolean quest2isLocked, boolean quest3isLocked,
        boolean quest4isLocked, boolean quest5isLocked, boolean quest6isLocked, boolean quest7isLocked,
        boolean quest8isLocked, boolean quest9isLocked, boolean quest10isLocked
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OWQuestProgressToClient> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ow_quest_progress_to_client"));

    public static final StreamCodec<FriendlyByteBuf, OWQuestProgressToClient> STREAM_CODEC =
            StreamCodec.of(OWQuestProgressToClient::encode, OWQuestProgressToClient::decode);

    private static void encode(FriendlyByteBuf buffer, OWQuestProgressToClient packet) {
        buffer.writeInt(packet.entityId());
        buffer.writeInt(packet.quest0Progression());
        buffer.writeInt(packet.quest1Progression());
        buffer.writeInt(packet.quest2Progression());
        buffer.writeInt(packet.quest3Progression());
        buffer.writeInt(packet.quest4Progression());
        buffer.writeInt(packet.quest5Progression());
        buffer.writeInt(packet.quest6Progression());
        buffer.writeInt(packet.quest7Progression());
        buffer.writeInt(packet.quest8Progression());
        buffer.writeInt(packet.quest9Progression());
        buffer.writeInt(packet.quest10Progression());

        buffer.writeBoolean(packet.quest0isLocked());
        buffer.writeBoolean(packet.quest1isLocked());
        buffer.writeBoolean(packet.quest2isLocked());
        buffer.writeBoolean(packet.quest3isLocked());
        buffer.writeBoolean(packet.quest4isLocked());
        buffer.writeBoolean(packet.quest5isLocked());
        buffer.writeBoolean(packet.quest6isLocked());
        buffer.writeBoolean(packet.quest7isLocked());
        buffer.writeBoolean(packet.quest8isLocked());
        buffer.writeBoolean(packet.quest9isLocked());
        buffer.writeBoolean(packet.quest10isLocked());
    }

    private static OWQuestProgressToClient decode(FriendlyByteBuf buffer) {
        return new OWQuestProgressToClient(
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),

                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean()
        );
    }

    @Override
    public CustomPacketPayload.Type<OWQuestProgressToClient> type() {
        return TYPE;
    }

    public static void handle(OWQuestProgressToClient packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId());
            if (entity instanceof OWEntity owEntity) {
                owEntity.quest0Progression = packet.quest0Progression();
                owEntity.quest1Progression = packet.quest1Progression();
                owEntity.quest2Progression = packet.quest2Progression();
                owEntity.quest3Progression = packet.quest3Progression();
                owEntity.quest4Progression = packet.quest4Progression();
                owEntity.quest5Progression = packet.quest5Progression();
                owEntity.quest6Progression = packet.quest6Progression();
                owEntity.quest7Progression = packet.quest7Progression();
                owEntity.quest8Progression = packet.quest8Progression();
                owEntity.quest9Progression = packet.quest9Progression();
                owEntity.quest10Progression = packet.quest10Progression();

                owEntity.quest0isLocked = packet.quest0isLocked();
                owEntity.quest1isLocked = packet.quest1isLocked();
                owEntity.quest2isLocked = packet.quest2isLocked();
                owEntity.quest3isLocked = packet.quest3isLocked();
                owEntity.quest4isLocked = packet.quest4isLocked();
                owEntity.quest5isLocked = packet.quest5isLocked();
                owEntity.quest6isLocked = packet.quest6isLocked();
                owEntity.quest7isLocked = packet.quest7isLocked();
                owEntity.quest8isLocked = packet.quest8isLocked();
                owEntity.quest9isLocked = packet.quest9isLocked();
                owEntity.quest10isLocked = packet.quest10isLocked();
            }
        });
    }
}
