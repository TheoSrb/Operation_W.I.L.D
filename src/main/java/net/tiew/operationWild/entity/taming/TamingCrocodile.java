package net.tiew.operationWild.entity.taming;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.entity.animals.terrestrial.KodiakEntity;
import net.tiew.operationWild.entity.behavior.CrocodileBehaviorHandler;
import net.tiew.operationWild.entity.behavior.KodiakBehaviorHandler;

import java.util.List;

/**
 * This class primarily manages the taming process for the Crocodile.
 * It manages the taming method from start to finish.
 */

public class TamingCrocodile {

    private CrocodileEntity crocodile;
    private CrocodileBehaviorHandler crocodileManagement;

    public TamingCrocodile(CrocodileEntity crocodile, CrocodileBehaviorHandler crocodileManagement) {
        this.crocodile = crocodile;
        this.crocodileManagement = crocodileManagement;
    }

    public void tick() {
        handleTamingSystem();
    }

    private void handleTamingSystem() {

    }

    public boolean canBeTamable() {
        return true;
    }
}