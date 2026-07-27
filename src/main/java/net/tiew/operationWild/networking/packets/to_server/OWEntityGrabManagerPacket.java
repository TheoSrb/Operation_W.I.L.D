package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public record OWEntityGrabManagerPacket(boolean isRightClickDown) implements CustomPacketPayload {

    public static final Type<OWEntityGrabManagerPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "ow_entity_grab_manager"));

    public static final StreamCodec<FriendlyByteBuf, OWEntityGrabManagerPacket> STREAM_CODEC =
            StreamCodec.composite(
                    StreamCodec.of(
                            FriendlyByteBuf::writeBoolean,
                            FriendlyByteBuf::readBoolean
                    ),
                    OWEntityGrabManagerPacket::isRightClickDown,
                    OWEntityGrabManagerPacket::new
            );

    @Override
    public Type<OWEntityGrabManagerPacket> type() {
        return TYPE;
    }

    /**
     * Retour sonore d'un coup de débattement réussi.
     *
     * <p>Un cri de douleur de la bête laissait croire qu'on la blessait, alors qu'on ne fait que
     * desserrer sa prise. Un impact mat convient mieux, et sa hauteur monte à mesure que l'étreinte
     * cède : l'oreille suit la progression sans quitter la jauge des yeux.</p>
     */
    private static void playStruggleSound(LivingEntity captor, int timeout, int maxTimeout) {
        float freedom = maxTimeout <= 0 ? 0f : 1f - Math.min(1f, (float) timeout / maxTimeout);
        captor.level().playSound(null, captor.getX(), captor.getY(), captor.getZ(),
                SoundEvents.PLAYER_ATTACK_WEAK, SoundSource.HOSTILE,
                0.75f, 0.85f + freedom * 0.7f);
    }

    public static void handle(OWEntityGrabManagerPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player == null || !packet.isRightClickDown()) return;

            // Recherche de proximité — fiable même si le sync passenger n'est pas encore arrivé
            player.level().getEntitiesOfClass(CrocodileEntity.class, player.getBoundingBox().inflate(5.0))
                    .stream()
                    .filter(c -> c.isGrabbing() && c.getGrabbedTarget() == player)
                    .findFirst()
                    .ifPresent(croc -> {
                        croc.setGrabTimeout(Math.max(0, croc.getGrabTimeout() - 15));
                        playStruggleSound(croc, croc.getGrabTimeout(), croc.getGrabMaxTimeout());
                    });

            // Boa : le joueur enroulé ne monte pas le boa → recherche de proximité comme le crocodile.
            player.level().getEntitiesOfClass(BoaEntity.class, player.getBoundingBox().inflate(5.0))
                    .stream()
                    .filter(b -> b.isGrabbing() && b.getGrabbedTarget() == player)
                    .findFirst()
                    .ifPresent(boa -> {
                        boa.setGrabTimeout(Math.max(0, boa.getGrabTimeout()
                                - net.tiew.operationWild.entity.attacks.OWAttacksConstants.Boa.CONSTRICT_STRUGGLE_REDUCTION));
                        playStruggleSound(boa, boa.getGrabTimeout(), boa.getGrabMaxTimeout());
                    });

            LivingEntity vehicle = (LivingEntity) player.getVehicle();
            if (vehicle instanceof TigerEntity tiger && tiger.getGrabbedTarget() != null && tiger.getGrabbedTarget() == player) {
                tiger.setGrabTimeout(Math.max(0, tiger.getGrabTimeout() - 15));
                playStruggleSound(tiger, tiger.getGrabTimeout(), tiger.getGrabMaxTimeout());
            }
        });
    }
}