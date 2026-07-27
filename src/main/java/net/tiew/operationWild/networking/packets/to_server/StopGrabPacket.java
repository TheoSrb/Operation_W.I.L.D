package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;

public record StopGrabPacket() implements CustomPacketPayload {

    public static final Type<StopGrabPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "stop_grab"));

    public static final StreamCodec<FriendlyByteBuf, StopGrabPacket> STREAM_CODEC =
            StreamCodec.unit(new StopGrabPacket());

    @Override
    public Type<StopGrabPacket> type() {
        return TYPE;
    }

    public static void handle(StopGrabPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Entity entity = player.getRootVehicle();

                if (entity != null) {
                    // Le crocodile passe par releaseGrab() : rendre la physique, démonter la victime
                    // et rallumer l'IA que l'ultime avait coupée. La sortie manuelle contournait tout
                    // cela — le joueur libéré gardait noPhysics et traversait le décor indéfiniment.
                    // La vérification de cible évite en prime qu'un simple passager déclenche la
                    // libération d'une prise qui ne le concerne pas.
                    if (entity instanceof CrocodileEntity crocodile) {
                        if (crocodile.isGrabbing() && crocodile.getGrabbedTarget() == player
                                && crocodile.getGrabTimeout() <= 0) {
                            crocodile.releaseGrab();
                        }
                    } else if (entity instanceof TigerEntity tiger) {
                        if (tiger.isGrabbing() && tiger.getGrabbedTarget() == player) {
                            tiger.getGrabbedTarget().stopRiding();
                            tiger.setGrabbing(false, null);
                            tiger.setTarget(null);
                        }
                    }
                }

                // Boa : le joueur enroulé n'est pas un passager → recherche de proximité.
                player.level().getEntitiesOfClass(BoaEntity.class, player.getBoundingBox().inflate(5.0))
                        .stream()
                        .filter(b -> b.isGrabbing() && b.getGrabbedTarget() == player)
                        .findFirst()
                        .ifPresent(boa -> {
                            boa.stopConstrict();
                            boa.setTarget(null);
                        });
            }
        });
    }
}

