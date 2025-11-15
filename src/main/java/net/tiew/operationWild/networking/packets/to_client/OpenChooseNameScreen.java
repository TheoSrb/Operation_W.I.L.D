package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.screen.entity.OWChooseNameScreen;

public record OpenChooseNameScreen(int entityId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenChooseNameScreen> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "open_choose_name_screen"));

    public static final StreamCodec<FriendlyByteBuf, OpenChooseNameScreen> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            OpenChooseNameScreen::entityId,
            OpenChooseNameScreen::new
    );

    @Override
    public CustomPacketPayload.Type<OpenChooseNameScreen> type() {
        return TYPE;
    }

    public static void handle(OpenChooseNameScreen packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new OWChooseNameScreen(packet.entityId()));
        });
    }
}