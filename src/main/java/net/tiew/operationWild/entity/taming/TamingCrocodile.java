package net.tiew.operationWild.entity.taming;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
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

        if (!this.crocodile.isTame()) {
            if (crocodile.getSacrificesUnity() > 0 && crocodile.getSacrificesUnity() < 100) {
                this.crocodile.setSacrificesUnity(this.crocodile.getSacrificesUnity() - 0.025f);
            }
            System.out.println(this.crocodile.getSacrificesUnity() + " %");
        }
    }

    public boolean canBeTamable() {
        return !crocodile.isTame();
    }

    public boolean ownerIsNear(Player player, TamableAnimal animal) {
        return crocodile.distanceTo(player) <= 20 && player.distanceTo(animal) <= 20;
    }
}