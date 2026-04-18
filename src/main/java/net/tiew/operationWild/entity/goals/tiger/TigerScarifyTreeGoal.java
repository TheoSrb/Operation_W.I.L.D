package net.tiew.operationWild.entity.goals.tiger;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.block.OWBlocks;
import net.tiew.operationWild.block.custom.ScarifiedWoodLogBlock;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.entity.variants.TigerVariant;
import net.tiew.operationWild.sound.OWSounds;

import java.util.EnumSet;

public class TigerScarifyTreeGoal extends Goal {
    private final TigerEntity mob;
    private double distanceToSeeTree;
    private double speedModifier;
    private BlockPos choosenLog = null;
    private BlockPos awayPos = null;
    private Block block = null;
    private boolean haveAlreadySeeLog = false;
    private boolean hasScarified = false;
    private int scarifyAnimationTimer = 0;
    private int beforeScarifyTimer = 0;
    private static final int SCARIFY_ANIMATION_DURATION = 19;

    public TigerScarifyTreeGoal(TigerEntity tiger, double distanceToSeeTree, double speedModifier) {
        this.mob = tiger;
        this.distanceToSeeTree = distanceToSeeTree;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return (this.mob.getDayOrNightTimeInterval(6, 10) || this.mob.getDayOrNightTimeInterval(17, 23))
                && !this.mob.isTame()
                && !this.mob.isNapping()
                && !this.mob.isPreparingNapping()
                && !this.mob.isInFight()
                && !this.mob.isInWater()
                && (this.mob.wantToScarifyWood || this.mob.goAway);
    }

    public boolean canGoToLog(BlockState block, BlockPos blockPos) {
        return isLogBlock(block) && !isScarifiedLogAbove(blockPos) && !isScarifiedLogBelow(blockPos) && canAccessToTrunk(blockPos);
    }

    public boolean isLogBlock(BlockState block) {
        return block.is(Blocks.ACACIA_LOG) || block.is(Blocks.BIRCH_LOG) || block.is(Blocks.CHERRY_LOG) || block.is(Blocks.JUNGLE_LOG) ||
                block.is(Blocks.SPRUCE_LOG) || block.is(Blocks.MANGROVE_LOG) || block.is(Blocks.OAK_LOG) || block.is(Blocks.DARK_OAK_LOG);
    }

    public BlockPos findClosestLog() {
        for (BlockPos blockPos : BlockPos.betweenClosed(
                Mth.floor(this.mob.getX() - distanceToSeeTree),
                Mth.floor(this.mob.getY() - distanceToSeeTree),
                Mth.floor(this.mob.getZ() - distanceToSeeTree),
                Mth.floor(this.mob.getX() + distanceToSeeTree),
                this.mob.getBlockY(),
                Mth.floor(this.mob.getZ() + distanceToSeeTree))) {

            BlockState blockState = this.mob.level().getBlockState(blockPos);

            if (canGoToLog(blockState, blockPos)) {
                haveAlreadySeeLog = true;
                return blockPos;
            }
        }
        return null;
    }

    public Block chooseScarifiedBlock(BlockPos targetPos) {
        BlockState blockState = this.mob.level().getBlockState(targetPos);

        if (blockState.is(Blocks.OAK_LOG)) block = OWBlocks.SCARIFIED_OAK_LOG.get();
        else if (blockState.is(Blocks.DARK_OAK_LOG)) block = OWBlocks.SCARIFIED_DARK_OAK_LOG.get();
        else if (blockState.is(Blocks.CHERRY_LOG)) block = OWBlocks.SCARIFIED_CHERRY_LOG.get();
        else if (blockState.is(Blocks.JUNGLE_LOG)) block = OWBlocks.SCARIFIED_JUNGLE_LOG.get();
        else if (blockState.is(Blocks.ACACIA_LOG)) block = OWBlocks.SCARIFIED_ACACIA_LOG.get();
        else if (blockState.is(Blocks.MANGROVE_LOG)) block = OWBlocks.SCARIFIED_MANGROVE_LOG.get();
        else if (blockState.is(Blocks.BIRCH_LOG)) block = OWBlocks.SCARIFIED_BIRCH_LOG.get();
        else if (blockState.is(Blocks.SPRUCE_LOG)) block = OWBlocks.SCARIFIED_SPRUCE_LOG.get();

        return block;
    }

    public boolean isScarifiedLogAbove(BlockPos blockPos) {
        if (blockPos == null) return false;
        return this.mob.level().getBlockState(blockPos.above()).is(OWBlocks.SCARIFIED_OAK_LOG.get()) ||
                this.mob.level().getBlockState(blockPos.above()).is(OWBlocks.SCARIFIED_DARK_OAK_LOG.get()) ||
                this.mob.level().getBlockState(blockPos.above()).is(OWBlocks.SCARIFIED_CHERRY_LOG.get()) ||
                this.mob.level().getBlockState(blockPos.above()).is(OWBlocks.SCARIFIED_JUNGLE_LOG.get()) ||
                this.mob.level().getBlockState(blockPos.above()).is(OWBlocks.SCARIFIED_ACACIA_LOG.get()) ||
                this.mob.level().getBlockState(blockPos.above()).is(OWBlocks.SCARIFIED_MANGROVE_LOG.get()) ||
                this.mob.level().getBlockState(blockPos.above()).is(OWBlocks.SCARIFIED_BIRCH_LOG.get()) ||
                this.mob.level().getBlockState(blockPos.above()).is(OWBlocks.SCARIFIED_SPRUCE_LOG.get());
    }

    public boolean isScarifiedLogBelow(BlockPos blockPos) {
        if (blockPos == null) return false;
        return this.mob.level().getBlockState(blockPos.below()).is(OWBlocks.SCARIFIED_OAK_LOG.get()) ||
                this.mob.level().getBlockState(blockPos.below()).is(OWBlocks.SCARIFIED_DARK_OAK_LOG.get()) ||
                this.mob.level().getBlockState(blockPos.below()).is(OWBlocks.SCARIFIED_CHERRY_LOG.get()) ||
                this.mob.level().getBlockState(blockPos.below()).is(OWBlocks.SCARIFIED_JUNGLE_LOG.get()) ||
                this.mob.level().getBlockState(blockPos.below()).is(OWBlocks.SCARIFIED_ACACIA_LOG.get()) ||
                this.mob.level().getBlockState(blockPos.below()).is(OWBlocks.SCARIFIED_MANGROVE_LOG.get()) ||
                this.mob.level().getBlockState(blockPos.below()).is(OWBlocks.SCARIFIED_BIRCH_LOG.get()) ||
                this.mob.level().getBlockState(blockPos.below()).is(OWBlocks.SCARIFIED_SPRUCE_LOG.get());
    }

    public boolean canAccessToTrunk(BlockPos targetPos) {
        if (targetPos == null) return false;
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (this.mob.level().getBlockState(targetPos.offset(x, y, z)).is(BlockTags.LEAVES)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        if (!haveAlreadySeeLog) {
            choosenLog = findClosestLog();
            awayPos = choosenLog != null ? choosenLog.offset((int) distanceToSeeTree, 0, (int) distanceToSeeTree) : null;
            this.mob.playSound(OWSounds.TIGER_3.get());
        }

        if (!this.mob.goAway) {
            if (choosenLog != null) {
                if (canGoToLog(this.mob.level().getBlockState(choosenLog), choosenLog)) {
                    double distanceBetweenMobAndChoosenLog = this.mob.distanceToSqr(Vec3.atCenterOf(choosenLog));

                    if (distanceBetweenMobAndChoosenLog > 10) {
                        this.mob.getNavigation().moveTo(choosenLog.getX(), choosenLog.getY(), choosenLog.getZ(), speedModifier);
                    } else {
                        chooseScarifiedBlock(choosenLog);
                        this.mob.getLookControl().setLookAt(choosenLog.getX(), choosenLog.getY(), choosenLog.getZ());
                        this.mob.getNavigation().stop();

                        if (!hasScarified) {
                            this.mob.setScarifying(true);
                            this.mob.setAttacking(true);
                            hasScarified = true;
                        }


                        if (hasScarified) {
                            scarifyAnimationTimer++;
                            beforeScarifyTimer++;

                            if (beforeScarifyTimer == 7) {
                                BlockPos aboveBlock = choosenLog.above();
                                BlockState existingBlockState = this.mob.level().getBlockState(aboveBlock);
                                Block existingBlock = existingBlockState.getBlock();

                                Vec3 lookVec = this.mob.getLookAngle();
                                Direction lookDirection = Direction.getNearest(lookVec.x, lookVec.y, lookVec.z);
                                BlockPos frontPos = aboveBlock.relative(lookDirection.getOpposite());

                                if (this.mob.level().getBlockState(frontPos).is(Blocks.VINE)) {
                                    this.mob.level().destroyBlock(frontPos, true);
                                }

                                if (existingBlock != Blocks.AIR && block != null) {
                                    this.mob.level().destroyBlock(aboveBlock, false);
                                    Item barkItem = null;

                                    if (existingBlockState.is(OWBlocks.SCARIFIED_OAK_LOG.get())) barkItem = Items.OAK_LOG;
                                    else if (existingBlockState.is(OWBlocks.SCARIFIED_SPRUCE_LOG.get())) barkItem = Items.SPRUCE_LOG;
                                    else if (existingBlockState.is(OWBlocks.SCARIFIED_BIRCH_LOG.get())) barkItem = Items.BIRCH_LOG;
                                    else if (existingBlockState.is(OWBlocks.SCARIFIED_JUNGLE_LOG.get())) barkItem = Items.JUNGLE_LOG;
                                    else if (existingBlockState.is(OWBlocks.SCARIFIED_ACACIA_LOG.get())) barkItem = Items.ACACIA_LOG;
                                    else if (existingBlockState.is(OWBlocks.SCARIFIED_DARK_OAK_LOG.get())) barkItem = Items.DARK_OAK_LOG;
                                    else if (existingBlockState.is(OWBlocks.SCARIFIED_MANGROVE_LOG.get())) barkItem = Items.MANGROVE_LOG;
                                    else if (existingBlockState.is(OWBlocks.SCARIFIED_CHERRY_LOG.get())) barkItem = Items.CHERRY_LOG;

                                    if (barkItem != null) {
                                        Vec3 dropPos = Vec3.atCenterOf(aboveBlock);
                                        ItemEntity itemEntity = new ItemEntity(
                                                this.mob.level(),
                                                dropPos.x, dropPos.y, dropPos.z,
                                                new ItemStack(barkItem)
                                        );
                                        this.mob.level().addFreshEntity(itemEntity);
                                    }

                                    Vec3 mobPosition = this.mob.position();
                                    Vec3 targetPosition = Vec3.atCenterOf(choosenLog);
                                    Vec3 direction = targetPosition.subtract(mobPosition).normalize();
                                    Direction facing = Direction.getNearest(direction.x, 0.0, direction.z);

                                    if (block == OWBlocks.SCARIFIED_OAK_LOG.get()) {
                                        BlockState scarifiedState = block.defaultBlockState()
                                                .setValue(ScarifiedWoodLogBlock.FACING, facing)
                                                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
                                        this.mob.level().setBlock(aboveBlock, scarifiedState, 3);
                                    } else {
                                        this.mob.level().setBlock(aboveBlock, block.defaultBlockState(), 3);
                                    }

                                    this.mob.playSound(this.mob.getVariant() == TigerVariant.Cosmetics.VIRUS.variant ? OWSounds.TIGER_IDLE_VIRUS.get() : OWSounds.TIGER_IDLE.get(), 2.0f, 1.1f);
                                    this.mob.playSound(SoundEvents.AXE_STRIP);
                                }
                            }

                            if (scarifyAnimationTimer >= SCARIFY_ANIMATION_DURATION) {
                                this.mob.setScarifying(false);
                                this.mob.goAway = true;
                                choosenLog = null;
                            }
                        }

                        scarifyAnimationTimer++;
                        if (scarifyAnimationTimer >= SCARIFY_ANIMATION_DURATION) {
                            this.mob.setScarifying(false);
                            this.mob.goAway = true;
                            choosenLog = null;
                        }
                    }
                } else {
                    this.mob.getNavigation().stop();
                    this.mob.wantToScarifyWood = false;
                    choosenLog = null;
                    haveAlreadySeeLog = false;
                    awayPos = null;
                    this.mob.goAway = false;
                    beforeScarifyTimer = 0;
                }
            }
        } else {
            if (awayPos != null) {
                double distAway = this.mob.distanceToSqr(Vec3.atCenterOf(awayPos));
                this.mob.getNavigation().moveTo(awayPos.getX(), awayPos.getY(), awayPos.getZ(), this.speedModifier);

                if (distAway <= 10) {
                    this.mob.getNavigation().stop();
                    this.mob.wantToScarifyWood = false;
                    choosenLog = null;
                    haveAlreadySeeLog = false;
                    awayPos = null;
                    hasScarified = false;
                    scarifyAnimationTimer = 0;
                    this.mob.goAway = false;
                    beforeScarifyTimer = 0;
                }
            }
        }
    }
}