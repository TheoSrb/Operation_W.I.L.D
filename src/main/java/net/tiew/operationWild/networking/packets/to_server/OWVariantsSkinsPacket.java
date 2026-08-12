package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;

public record OWVariantsSkinsPacket(int skinIndex) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OWVariantsSkinsPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ow_variants_skins"));

    public static final StreamCodec<FriendlyByteBuf, OWVariantsSkinsPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, OWVariantsSkinsPacket::skinIndex,
                    OWVariantsSkinsPacket::new
            );

    @Override
    public CustomPacketPayload.Type<OWVariantsSkinsPacket> type() {
        return TYPE;
    }

    public static void handle(OWVariantsSkinsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Entity entity = net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity.resolveControlledEntity(player);

                if (entity instanceof OWEntity owEntity && owEntity.hasTribePermission(player, net.tiew.operationWild.team.OWTribePermission.SKINS)) {
                    owEntity.changeSkin(packet.skinIndex(), true);
                }
            }
        });
    }
}
