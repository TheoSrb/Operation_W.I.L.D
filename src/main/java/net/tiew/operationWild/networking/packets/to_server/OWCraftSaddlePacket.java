package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.screen.blocks.SaddlerMenu;

public record OWCraftSaddlePacket(int recipe, int primaryColor, int secondaryColor) implements CustomPacketPayload {

    public static final Type<OWCraftSaddlePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ow_craft_saddle"));

    public static final StreamCodec<FriendlyByteBuf, OWCraftSaddlePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OWCraftSaddlePacket::recipe,
            ByteBufCodecs.VAR_INT, OWCraftSaddlePacket::primaryColor,
            ByteBufCodecs.VAR_INT, OWCraftSaddlePacket::secondaryColor,
            OWCraftSaddlePacket::new
    );

    @Override
    public Type<OWCraftSaddlePacket> type() {
        return TYPE;
    }

    public static void handle(OWCraftSaddlePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof SaddlerMenu menu)) return;

            menu.craftSaddle(player, packet.recipe(), packet.primaryColor(), packet.secondaryColor());
        });
    }
}
