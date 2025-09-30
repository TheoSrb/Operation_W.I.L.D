package net.tiew.operationWild.entity.AI;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;

import java.util.*;

public class AIKodiakManagement {

    private KodiakEntity kodiak;
    private AIKodiak AIKodiak;

    public AIKodiakManagement(KodiakEntity kodiak, AIKodiak AIKodiak) {
        this.kodiak = kodiak;
        this.AIKodiak = AIKodiak;
    }

    public void pickupItemInHisMouth(ItemStack itemStack) {
        kodiak.setFoodPick(itemStack);
        if (!itemStack.isEmpty()) kodiak.playSound(SoundEvents.ITEM_PICKUP);
    }

    public void lookForHoneyInTheBeeNest() {
        pickupItemInHisMouth(Items.HONEYCOMB.getDefaultInstance());
        kodiak.playSound(SoundEvents.HONEY_BLOCK_PLACE);
        kodiak.setDirty(true);
        warnBeesAround(10);

        Vec3 lookDirection = kodiak.getLookAngle();
        double spawnX = kodiak.getX() + lookDirection.x * 2.0;
        double spawnY = kodiak.getY() + 0.8;
        double spawnZ = kodiak.getZ() + lookDirection.z * 2.0;

        OWUtils.spawnItemParticles(kodiak, Items.HONEYCOMB.getDefaultInstance(), spawnX, spawnY, spawnZ);
    }

    public void openChest(ChestBlockEntity chestBlockEntity) {
        kodiak.openChestAnimation(chestBlockEntity);

        int timeForClosingChest = kodiak.getRandom().nextInt(2000) + 3000;

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                closeChest(chestBlockEntity);
            }
        }, timeForClosingChest);
    }

    public void closeChest(ChestBlockEntity chestBlockEntity) {
        List<Integer> foodSlots = new ArrayList<>();

        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                kodiak.closeChestAnimation(chestBlockEntity);
            }
        }, 500);

        for (int i = 0; i < chestBlockEntity.getContainerSize(); i++) {
            ItemStack item = chestBlockEntity.getItem(i);
            if (!item.isEmpty() && item.is(Tags.Items.FOODS)) {
                foodSlots.add(i);
            }
        }

        if (!foodSlots.isEmpty()) {
            int randomSlot = foodSlots.get(kodiak.getRandom().nextInt(foodSlots.size()));
            ItemStack itemChoose = chestBlockEntity.getItem(randomSlot);

            if (!itemChoose.isEmpty() && itemChoose.is(Tags.Items.FOODS)) {
                ItemStack itemCopy = itemChoose.copy();
                itemCopy.setCount(1);

                chestBlockEntity.removeItem(randomSlot, 1);
                chestBlockEntity.setChanged();

                if (chestBlockEntity.getLevel() != null && !chestBlockEntity.getLevel().isClientSide()) {
                    chestBlockEntity.getLevel().sendBlockUpdated(
                            chestBlockEntity.getBlockPos(),
                            chestBlockEntity.getBlockState(),
                            chestBlockEntity.getBlockState(),
                            Block.UPDATE_CLIENTS
                    );
                }

                pickupItemInHisMouth(itemCopy);
            }
        }

        kodiak.isSearchingInsideChest = false;
    }

    public void scheduleNextCropCheck(BlockPos target, int radiusToSearch) {
        kodiak.targetCrop = target;
        kodiak.cropRadiusSearch = radiusToSearch;
        kodiak.cropCheckTimer = 40;
    }

    public void goToNewCropBlock(int radiusToSearch) {
        BlockPos target = findNewCropBlock(radiusToSearch);

        if (target != null) {
            kodiak.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.15f);

            kodiak.level().scheduleTick(kodiak.blockPosition(), Blocks.AIR, 40);

            scheduleNextCropCheck(target, radiusToSearch);

        } else {
            kodiak.numberOfBonusSearching = 0;
            kodiak.getNavigation().stop();
        }
    }

    public BlockPos findNewCropBlock(int radiusToSearch) {
        BlockPos kodiakPos = kodiak.blockPosition();

        for (int x = -radiusToSearch; x <= radiusToSearch; x++) {
            for (int y = -radiusToSearch; y <= radiusToSearch; y++) {
                for (int z = -radiusToSearch; z <= radiusToSearch; z++) {
                    BlockPos pos = kodiakPos.offset(x, y, z);

                    if (OWUtils.distanceRest(kodiak, pos) >= 2 && kodiak.level().getBlockState(pos).is(BlockTags.CROPS)) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    public void warnBeesAround(int radius) {
        List<Bee> bees = kodiak.level().getEntitiesOfClass(Bee.class, kodiak.getBoundingBox().inflate(radius));

        for (Bee bee : bees) {
            bee.setTarget(kodiak);
        }
    }

    public void trampleCrops(BlockPos pos) {
        BlockState blockState = kodiak.level().getBlockState(pos);

        if (blockState.getBlock() instanceof FarmBlock) {
            FarmBlock.turnToDirt(null, blockState, kodiak.level(), pos);
        }
    }

    public void eatFoodInHisMouth(ItemStack itemStack) {
        kodiak.eatingTimer = 0;
        kodiak.startEatingTimer = false;
        kodiak.playSound(SoundEvents.GENERIC_EAT);

        Vec3 lookDirection = kodiak.getLookAngle();
        double spawnX = kodiak.getX() + lookDirection.x * 2.0;
        double spawnY = kodiak.getY() + 0.8;
        double spawnZ = kodiak.getZ() + lookDirection.z * 2.0;

        OWUtils.spawnItemParticles(kodiak, itemStack != null ? itemStack : Items.APPLE.getDefaultInstance(), spawnX, spawnY, spawnZ);

        if (kodiak.getFoodPick().is(OWTags.Items.KODIAK_DANGEROUS_FOOD)) {
            kodiak.addEffect(new MobEffectInstance(MobEffects.POISON, 350, 0));

            if (kodiak.lastPlayerWhoFeedHim != null) {
                kodiak.setTarget(kodiak.lastPlayerWhoFeedHim);
            }
        } else kodiak.lastPlayerWhoFeedHim = null;

        if (kodiak.getFoodPick().is(Items.HONEYCOMB)) {
            kodiak.setDirty(true);
        }

        kodiak.setFoodPick(ItemStack.EMPTY);
    }
}
