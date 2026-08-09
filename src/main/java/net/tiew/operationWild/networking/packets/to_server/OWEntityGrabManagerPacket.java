package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;

import java.util.function.IntConsumer;
import java.util.function.Predicate;

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

    private static final double SEARCH_RADIUS = 6.0;
    private static final int CROCODILE_STRUGGLE_REDUCTION = 15;

    @Override
    public Type<OWEntityGrabManagerPacket> type() {
        return TYPE;
    }

    public static void handle(OWEntityGrabManagerPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !packet.isRightClickDown()) return;

            CrocodileEntity crocodile = captor(player, CrocodileEntity.class,
                    c -> c.isGrabbing() && c.getGrabbedTarget() == player);
            if (crocodile != null) {
                struggle(crocodile, crocodile.getGrabTimeout(), crocodile.getGrabMaxTimeout(),
                        CROCODILE_STRUGGLE_REDUCTION, crocodile::setGrabTimeout,
                        !crocodile.isGrabHoldLocked(), crocodile::breakFreeFromGrab);
                return;
            }

            TigerEntity tiger = captor(player, TigerEntity.class,
                    t -> t.isGrabbing() && t.getGrabbedTarget() == player);
            if (tiger != null) {
                struggle(tiger, tiger.getGrabTimeout(), tiger.getGrabMaxTimeout(),
                        TigerEntity.GRAB_STRUGGLE_REDUCTION, tiger::setGrabTimeout,
                        true, tiger::breakFreeFromGrab);
                return;
            }

            BoaEntity boa = captor(player, BoaEntity.class,
                    b -> b.isGrabbing() && b.getGrabbedTarget() == player);
            if (boa != null) {
                struggle(boa, boa.getGrabTimeout(), boa.getGrabMaxTimeout(),
                        OWAttacksConstants.Boa.CONSTRICT_STRUGGLE_REDUCTION, boa::setGrabTimeout,
                        true, boa::breakFreeFromConstrict);
                return;
            }

            KangarooEntity kangaroo = captor(player, KangarooEntity.class,
                    k -> k.getDrownVictim() == player);
            if (kangaroo != null) {
                struggle(kangaroo, kangaroo.getGrabTimeout(), kangaroo.getGrabMaxTimeout(),
                        KangarooEntity.DROWN_STRUGGLE_REDUCTION, kangaroo::setGrabTimeout,
                        true, kangaroo::breakFreeFromDrown);
            }
        });
    }

    private static <T extends LivingEntity> T captor(ServerPlayer player, Class<T> type, Predicate<T> holdsPlayer) {
        return player.level()
                .getEntitiesOfClass(type, player.getBoundingBox().inflate(SEARCH_RADIUS), holdsPlayer)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private static void struggle(LivingEntity captor, int timeout, int maxTimeout, int reduction,
                                 IntConsumer applyTimeout, boolean mayBreakFree, Runnable breakFree) {
        int next = timeout - reduction;
        playStruggleSound(captor, Math.max(0, next), maxTimeout);

        if (next <= 0 && mayBreakFree) breakFree.run();
        else applyTimeout.accept(Math.max(0, next));
    }

    private static void playStruggleSound(LivingEntity captor, int timeout, int maxTimeout) {
        float freedom = maxTimeout <= 0 ? 0f : 1f - Math.min(1f, (float) timeout / maxTimeout);
        captor.level().playSound(null, captor.getX(), captor.getY(), captor.getZ(),
                SoundEvents.PLAYER_ATTACK_WEAK, SoundSource.HOSTILE,
                0.75f, 0.85f + freedom * 0.7f);
    }
}
