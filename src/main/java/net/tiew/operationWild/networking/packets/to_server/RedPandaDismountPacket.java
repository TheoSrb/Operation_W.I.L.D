package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity;

/**
 * Descente du panda roux de l'épaule.
 *
 * <p>Toutes les autres bêtes se quittent par une interaction visée : on regarde la monture et on
 * clique. Le panda roux perché est <b>à côté de la caméra</b> — en vue subjective, aucun rayon de
 * visée ne peut l'atteindre, et l'interaction d'entité vanilla ne part donc jamais. Le geste est
 * ici transmis directement, sans cible à désigner.</p>
 *
 * <p>Le serveur ne fait confiance à rien : il retrouve lui-même le passager du joueur et vérifie
 * que c'est bien un panda roux avant de le reposer.</p>
 */
public record RedPandaDismountPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RedPandaDismountPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "red_panda_dismount"));

    public static final StreamCodec<FriendlyByteBuf, RedPandaDismountPacket> STREAM_CODEC =
            StreamCodec.unit(new RedPandaDismountPacket());

    @Override
    public CustomPacketPayload.Type<RedPandaDismountPacket> type() {
        return TYPE;
    }

    public static void handle(RedPandaDismountPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            RedPandaEntity redPanda = RedPandaEntity.getShoulderPanda(player);
            if (redPanda == null) return;

            redPanda.dismountFromShoulder();
        });
    }
}
