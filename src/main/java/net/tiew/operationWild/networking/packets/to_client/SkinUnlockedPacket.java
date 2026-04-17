package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.core.OWDatasSave;

import java.util.UUID;

public record SkinUnlockedPacket(UUID entityUuid, int skinIndex) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SkinUnlockedPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "skin_unlocked"));

    public static final StreamCodec<FriendlyByteBuf, SkinUnlockedPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> { buf.writeUUID(pkt.entityUuid()); buf.writeInt(pkt.skinIndex()); },
                    buf -> new SkinUnlockedPacket(buf.readUUID(), buf.readInt())
            );

    @Override
    public CustomPacketPayload.Type<SkinUnlockedPacket> type() { return TYPE; }

    public static void handle(SkinUnlockedPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> OWDatasSave.addPurchasedSkin(packet.entityUuid(), packet.skinIndex()));
    }
}
