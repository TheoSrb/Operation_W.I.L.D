package net.tiew.operationWild.networking.packets.to_server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.utils.OWUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record LevelUpOWInventoryPacket(String attributeName) implements CustomPacketPayload {

    private static final Logger log = LoggerFactory.getLogger(LevelUpOWInventoryPacket.class);

    public static final CustomPacketPayload.Type<LevelUpOWInventoryPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "level_up_ow_inventory"));

    public static final StreamCodec<FriendlyByteBuf, LevelUpOWInventoryPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, LevelUpOWInventoryPacket::attributeName,
                    LevelUpOWInventoryPacket::new
            );

    @Override
    public CustomPacketPayload.Type<LevelUpOWInventoryPacket> type() {
        return TYPE;
    }

    public static void handle(LevelUpOWInventoryPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Entity entity = player.getRootVehicle();
                float pitch = (float) OWUtils.generateRandomInterval(0.8, 1.2);

                if (entity != null && entity instanceof OWEntity owEntity) {
                    if (packet.attributeName().equals("MaxHealth")) {
                        owEntity.upgradeAttributes(
                                owEntity,
                                Attributes.MAX_HEALTH,
                                owEntity.isTank() ? 1 : owEntity.isAssassin() ? 2 : owEntity.isMarauder() ? 3 : 4
                        );
                    } else if (packet.attributeName().equals("AttackDamage")) {
                        owEntity.upgradeAttributes(
                                owEntity,
                                Attributes.ATTACK_DAMAGE,
                                owEntity.isAssassin() ? 1 : owEntity.isMarauder() ? 2 : owEntity.isTank() ? 3 : 4
                        );

                        System.out.println((owEntity.getDamage() / owEntity.getBaseDamage()) * 100);
                    } else if (packet.attributeName().equals("MovementSpeed")) {
                        owEntity.upgradeAttributes(
                                owEntity,
                                Attributes.MOVEMENT_SPEED,
                                owEntity.isMarauder() ? 1 : owEntity.isAssassin() ? 2 : owEntity.isTank() ? 3 : 4
                        );
                    }
                    owEntity.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, pitch);
                }
            }
        });
    }
}


