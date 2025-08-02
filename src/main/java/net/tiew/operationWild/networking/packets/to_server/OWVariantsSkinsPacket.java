package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.custom.living.BoaEntity;
import net.tiew.operationWild.entity.custom.living.PeacockEntity;
import net.tiew.operationWild.entity.custom.living.TigerEntity;

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
                Entity entity = player.getRootVehicle();

                if (entity != null && entity instanceof OWEntity owEntity) {
                    if (owEntity instanceof TigerEntity tiger) tiger.changeSkin(packet.skinIndex());
                    if (owEntity instanceof BoaEntity boa) boa.changeSkin(packet.skinIndex());
                    if (owEntity instanceof PeacockEntity peacock) peacock.changeSkin(packet.skinIndex());
                    owEntity.level().playLocalSound(owEntity.getX(), owEntity.getY(), owEntity.getZ(), SoundEvents.SLIME_JUMP, SoundSource.NEUTRAL, 1.0F, 1.0f, false);
                }
            }
        });
    }
}
