package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;

import java.util.List;

public record OpenOWInventoryPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenOWInventoryPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "open_ow_inventory"));

    public static final StreamCodec<FriendlyByteBuf, OpenOWInventoryPacket> STREAM_CODEC =
            StreamCodec.unit(new OpenOWInventoryPacket());

    @Override
    public CustomPacketPayload.Type<OpenOWInventoryPacket> type() {
        return TYPE;
    }

    public static void handle(OpenOWInventoryPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Entity entity = player.getRootVehicle();

                if (entity instanceof OWEntity owEntity) {
                    List<Entity> passengers = entity.getPassengers();
                    boolean isDriver = !passengers.isEmpty() && passengers.get(0) == player;
                    // Propriétaire OU membre de la tribu : mêmes droits sur l'inventaire.
                    boolean canControl = owEntity.hasTribePermission(player, net.tiew.operationWild.team.OWTribePermission.INVENTORY);

                    if (isDriver && canControl) {
                        player.openMenu(owEntity);
                    }
                }
            }
        });
    }
}