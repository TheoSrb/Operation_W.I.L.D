package net.tiew.operationWild.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.minecraft.world.entity.ExperienceOrb;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.block.OWBlocks;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.attacks.OWAttacksHandler;
import net.tiew.operationWild.entity.goals.crocodile.MonstersAvoidCrocodileGoal;
import net.tiew.operationWild.item.OWItems;

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
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        ItemStack heldItem = player.getItemInHand(event.getHand());

        if (player.getVehicle() instanceof OWEntity
                || heldItem.is(OWItems.REPTILIAN_DAGGER.get())) {
            event.setCanceled(true);
        }
    }

    /**
     * Priorité d'absorption des orbes : quand le joueur chevauche une de ses entités apprivoisées
     * qui n'est pas encore au niveau max, l'XP de l'orbe va à la MONTURE (et la fait monter de niveau)
     * plutôt qu'au joueur. Une fois la monture niveau 50, les orbes reviennent au joueur (rider).
     * Pour récupérer l'XP soi-même avant le niveau 50, il suffit de descendre de la monture.
     */
    @SubscribeEvent
    public static void onPlayerPickupXp(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        if (player.getVehicle() instanceof OWEntity pet
                && pet.isTame() && pet.isOwnedBy(player) && pet.getLevel() < 50) {
            ExperienceOrb orb = event.getOrb();
            int value = orb.getValue();
            if (value > 0) {
                pet.gainLevelXp(value);
                player.level().playSound(null, pet.getX(), pet.getY(), pet.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 0.15f,
                        ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.35f + 1.0f) * 2.0f);
            }
            orb.discard();
            event.setCanceled(true);
        }
    }

    /** Synchronise les cagnottes par joueur (Pièces Sauvages + Expérience d'Apprivoisement) à la connexion. */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            net.tiew.operationWild.core.OWCurrency.syncWildCoins(player);
            net.tiew.operationWild.core.OWTamingXp.syncTamingXp(player);
        }
    }

    /** Conserve les cagnottes par joueur (Pièces + Expérience d'Apprivoisement) à travers la mort / le changement de dimension. */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        int coins = net.tiew.operationWild.core.OWCurrency.getWildCoins(event.getOriginal());
        net.tiew.operationWild.core.OWCurrency.setWildCoins(event.getEntity(), coins);

        double tamingXp = net.tiew.operationWild.core.OWTamingXp.getTamingXp(event.getOriginal());
        net.tiew.operationWild.core.OWTamingXp.setTamingXp(event.getEntity(), tamingXp);
    }

    private static int slingshotDecayCounter = 0;

    /** Décai périodique (~10 s) des fissures de la fronde, exécuté sur le thread principal (remplace l'ancien Timer). */
    @SubscribeEvent
    public static void onServerTick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
        if (++slingshotDecayCounter >= 200) {
            slingshotDecayCounter = 0;
            net.tiew.operationWild.entity.misc.SlingshotProjectile.decayTick(event.getServer());
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        // Pet à l'origine du kill : soit l'entité elle-même, soit le rider qui la chevauche.
        net.tiew.operationWild.entity.OWEntity pet = null;
        if (event.getSource().getEntity() instanceof net.tiew.operationWild.entity.OWEntity e) {
            pet = e;
        } else if (event.getSource().getEntity() instanceof Player player
                && player.getVehicle() instanceof net.tiew.operationWild.entity.OWEntity e) {
            pet = e;
        }
        if (pet == null || !pet.isTame()) return;

        float multiplier = 1f;
        // Kodiak : passif inné « Instinct du Boucher ».
        if (pet instanceof KodiakEntity) {
            multiplier = Math.max(multiplier, OWAttacksConstants.Kodiak.BUTCHER_INSTINCT_MULTIPLIER);
        }
        // Piste : passif « Boucher » débloqué (ex. Tigre) — même effet, système séparé.
        if (net.tiew.operationWild.entity.attacks.OWPistePassives.has(pet, net.tiew.operationWild.entity.attacks.OWPistePassives.BUTCHER)) {
            multiplier = Math.max(multiplier, net.tiew.operationWild.entity.attacks.OWPistePassives.BUTCHER_MULTIPLIER);
        }

        if (multiplier <= 1f) return;

        for (ItemEntity itemEntity : event.getDrops()) {
            ItemStack stack = itemEntity.getItem();
            if (!stack.has(net.minecraft.core.component.DataComponents.FOOD) && !stack.is(Tags.Items.FOODS_RAW_MEAT)) continue;

            int base  = stack.getCount();
            float raw = base * (multiplier - 1f);
            int bonus = (int) raw;
            if (event.getEntity().level().random.nextFloat() < (raw - bonus)) bonus++;

            ItemStack newStack = stack.copy();
            newStack.setCount(base + bonus);
            itemEntity.setItem(newStack);
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
}