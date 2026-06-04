package net.tiew.operationWild.networking.packets.to_client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.gui.ClientRitualState;

/** Synchronise l'état du Rituel de Communion vers le propriétaire, pour le HUD (jauges + vague). */
public record RitualSyncPacket(boolean active, float materialization, float stability,
                               int currentWave, int totalWaves, int phase) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RitualSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ritual_sync"));

    public static final StreamCodec<FriendlyByteBuf, RitualSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, RitualSyncPacket::active,
            ByteBufCodecs.FLOAT, RitualSyncPacket::materialization,
            ByteBufCodecs.FLOAT, RitualSyncPacket::stability,
            ByteBufCodecs.VAR_INT, RitualSyncPacket::currentWave,
            ByteBufCodecs.VAR_INT, RitualSyncPacket::totalWaves,
            ByteBufCodecs.VAR_INT, RitualSyncPacket::phase,
            RitualSyncPacket::new
    );

    @Override
    public CustomPacketPayload.Type<RitualSyncPacket> type() {
        return TYPE;
    }

    public static void handle(RitualSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientRitualState.update(packet));
    }
}
