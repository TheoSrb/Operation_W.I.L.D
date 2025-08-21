package net.tiew.operationWild.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.custom.living.JellyfishEntity;
import net.tiew.operationWild.entity.custom.living.KodiakEntity;
import net.tiew.operationWild.entity.custom.object.SeabugShard;
import net.tiew.operationWild.entity.custom.vehicle.Submarine;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.BookNotificationPacket;
import net.tiew.operationWild.networking.packets.to_server.SyncKillDataPacket;
import net.tiew.operationWild.screen.player.OWEntityJournalScreen;

import java.util.List;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ServerEvents {

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ClientEvents.tamingExperience >= 165) {

        } else if (ClientEvents.tamingExperience >= 80) {

        } else if (ClientEvents.tamingExperience >= 25) {

        }
    }

    @SubscribeEvent
    public static void onPlayerKill(LivingDeathEvent event) {
        Entity source = event.getSource().getEntity();
        if (source == null) {
            source = event.getSource().getDirectEntity();
        }
        Entity target = event.getEntity();

        if (source instanceof ServerPlayer player) {
            if (target instanceof OWEntity owEntity && !owEntity.isTame() && !(owEntity instanceof Submarine) && !(owEntity instanceof SeabugShard)) {
                boolean wasAlreadyKilled = false;
                String entityType = "";

                ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
                if (entityKey != null && entityKey.getNamespace().equals("ow")) {
                    entityType = entityKey.getPath();

                    wasAlreadyKilled = ClientEvents.hasPlayerKilledOWEntity(player, entityType);
                }

                if (!entityType.isEmpty()) {
                    String worldName = ClientEvents.getWorldName(player);
                    SyncKillDataPacket packet = new SyncKillDataPacket(entityType, worldName);
                    OWNetworkHandler.sendToClient(packet, player);
                }

                if (!wasAlreadyKilled) sendNotificationBook(player, entityType, false);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();

        if (source.getEntity() instanceof KodiakEntity) {
            for (ItemEntity itemEntity : event.getDrops()) {
                ItemStack drop = itemEntity.getItem();
                if (isMeat(drop.getItem())) {
                    int additionalDrops = entity.getRandom().nextInt(2);
                    for (int i = 0; i < additionalDrops; i++) {
                        entity.spawnAtLocation(drop.copy());
                    }
                    break;
                }
            }
        }
    }

    private static boolean isMeat(Item item) {
        return OWEntity.FOOD_FOR_HEALING_MEAT.contains(item);
    }

    public static void sendNotificationBook(ServerPlayer player, String entityType, boolean isTaming) {
        BookNotificationPacket packet = new BookNotificationPacket(entityType, isTaming);
        OWNetworkHandler.sendToClient(packet, player);
    }
}