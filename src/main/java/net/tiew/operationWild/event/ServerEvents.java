package net.tiew.operationWild.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.block.OWBlocks;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.aquatic.WalrusEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.goals.crocodile.MonstersAvoidCrocodileGoal;
import net.tiew.operationWild.entity.misc.SeabugShard;
import net.tiew.operationWild.entity.misc.Submarine;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.BookNotificationPacket;
import net.tiew.operationWild.networking.packets.to_server.SyncKillDataPacket;
import net.tiew.operationWild.screen.player.adventurer_manuscript.AdventurerManuscriptScreen;

import java.util.List;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ServerEvents {

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();

        if (source.getEntity() instanceof CrocodileEntity) {
            if (!victim.isInWater() && (victim.getType().is(OWTags.Entities.DROP_CARCASS) || victim instanceof Player) && !victim.isBaby()) {
                if (victim.level().getRandom().nextFloat() < 0.25f) {
                    BlockPos pos = victim.blockPosition();
                    Level level = victim.level();

                    if (level.getBlockState(pos.below()).isSolid() &&
                            level.getBlockState(pos).canBeReplaced()) {

                        Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(level.getRandom());
                        BlockState carcassState = OWBlocks.ANIMAL_CARCASS.get().defaultBlockState()
                                .setValue(HorizontalDirectionalBlock.FACING, facing);

                        level.setBlock(pos, carcassState, 3);

                        level.playSound(null, pos, SoundEvents.SKELETON_DEATH,
                                SoundSource.BLOCKS, 1.0f, 0.8f);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntitySpawn(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Monster monster) {
            monster.goalSelector.addGoal(3, new MonstersAvoidCrocodileGoal(monster));
        }
    }

    @SubscribeEvent
    public static void onLivingEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            if (livingEntity.hasEffect(OWEffects.FRACTURE.getDelegate())) {
                if (livingEntity.onGround()) {
                    if (livingEntity.tickCount % 20 <= 15 && livingEntity.tickCount >= 0) {
                        livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().multiply(0.025, 1.0, 0.025));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            if (attacker.hasEffect(OWEffects.FRACTURE.getDelegate())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onBeeIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Bee bee)) {
            return;
        }

        DamageSource source = event.getSource();
        if (source == null) {
            return;
        }

        LivingEntity attacker = null;

        if (source.getDirectEntity() instanceof LivingEntity directEntity) {
            attacker = directEntity;
        }
        else if (source.getEntity() instanceof LivingEntity sourceEntity) {
            attacker = sourceEntity;
        }

        if (attacker == null) {
            return;
        }

        if (attacker instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return;
        }

        List<KodiakEntity> kodiaks = bee.level().getEntitiesOfClass(KodiakEntity.class,
                bee.getBoundingBox().inflate(18));

        for (KodiakEntity kodiak : kodiaks) {
            kodiak.setTarget(attacker);
        }
    }

    @SubscribeEvent
    public static void onPlayerTrySleep(CanPlayerSleepEvent event) {
        Player player = event.getEntity();
        Level level = player.level();

        if (hasTeddyBearNearby(player, level)) {
            event.setProblem(null);
        }
    }

    private static boolean hasTeddyBearNearby(Player player, Level level) {
        BlockPos playerPos = player.blockPosition();
        int radius = 3;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos checkPos = playerPos.offset(x, y, z);
                    if (level.getBlockState(checkPos).is(OWBlocks.TEDDY_BEAR.get())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        DamageSource source = event.getSource();

        if (source.getEntity() instanceof KodiakEntity) createMultiDrop(event, Tags.Items.FOODS, 2);
        else if (source.getEntity() instanceof WalrusEntity) createMultiDrop(event, OWItems.STINGING_FILAMENT.get(), 3);
    }

    private static void createMultiDrop(LivingDropsEvent event, Item item, int amount) {
        DamageSource source = event.getSource();
        LivingEntity entity = event.getEntity();
        if (source.getEntity() instanceof WalrusEntity) {
            for (ItemEntity itemEntity : event.getDrops()) {
                ItemStack drop = itemEntity.getItem();
                if (drop.is(item)) {
                    int additionalDrops = entity.getRandom().nextInt(amount);
                    for (int i = 0; i < additionalDrops; i++) {
                        entity.spawnAtLocation(drop.copy());
                    }
                    break;
                }
            }
        }
    }

    private static void createMultiDrop(LivingDropsEvent event, TagKey<Item> item, int amount) {
        DamageSource source = event.getSource();
        LivingEntity entity = event.getEntity();
        if (source.getEntity() instanceof WalrusEntity) {
            for (ItemEntity itemEntity : event.getDrops()) {
                ItemStack drop = itemEntity.getItem();
                if (drop.is(item)) {
                    int additionalDrops = entity.getRandom().nextInt(amount);
                    for (int i = 0; i < additionalDrops; i++) {
                        entity.spawnAtLocation(drop.copy());
                    }
                    break;
                }
            }
        }
    }
}