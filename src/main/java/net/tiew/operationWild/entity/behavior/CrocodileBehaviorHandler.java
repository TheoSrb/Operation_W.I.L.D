package net.tiew.operationWild.entity.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.food.FoodProperties;
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
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.sound.OWSounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class only manages methods and functions that are useful and necessary for the proper functioning of Crocodile's artificial intelligence.
 * It is a complementary class to the latter.
 */

public class CrocodileBehaviorHandler {

    private CrocodileEntity crocodile;

    public CrocodileBehaviorHandler(CrocodileEntity crocodile) {
        this.crocodile = crocodile;
    }

    public boolean canPlayIdleAnimation() {
        return crocodile.getTarget() == null && !crocodile.isChargingMouth() && !crocodile.isNapping() && !crocodile.isMoving() && !crocodile.isVehicle() && !crocodile.isInWater();
    }

    public boolean canGrowl() {
        return canPlayIdleAnimation();
    }

    public boolean canGrunt() {
        return canPlayIdleAnimation();
    }

    public boolean isAnyIdleAnimationPlaying() {
        return crocodile.growlsAnimationState.isStarted() || crocodile.gruntAnimationState.isStarted();
    }
}
