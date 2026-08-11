package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;

public record OWSelectSecondaryAttackPacket(int index) implements CustomPacketPayload {

    public static final Type<OWSelectSecondaryAttackPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ow_select_secondary_attack"));

    public static final StreamCodec<FriendlyByteBuf, OWSelectSecondaryAttackPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, OWSelectSecondaryAttackPacket::index,
                    OWSelectSecondaryAttackPacket::new
            );

    @Override
    public Type<OWSelectSecondaryAttackPacket> type() { return TYPE; }

    public static void handle(OWSelectSecondaryAttackPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            // La carte changée est toujours celle de la monture du joueur : aucun identifiant
            // d'entité n'est accepté depuis le client, donc aucune bête tierce n'est joignable.
            if (!(player.getRootVehicle() instanceof OWEntity entity)) return;
            if (entity.getPassengers().indexOf(player) != 0) return;
            if (!entity.canPilotAttacks(player)) return;

            // trySwitchSecondaryAttack replie l'index sur le nombre de cartes de l'espèce — un
            // client qui enverrait n'importe quel entier ne peut pas désigner une carte
            // inexistante — et fait respecter le délai entre deux changements, qu'on ne peut pas
            // confier au seul client.
            entity.trySwitchSecondaryAttack(packet.index());
        });
    }
}
