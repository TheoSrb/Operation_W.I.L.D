package net.tiew.operationWild.entity.taming;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
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
import java.util.UUID;

/**
 * This class primarily manages the taming process for the Crocodile.
 * It manages the taming method from start to finish.
 */

public class TamingCrocodile {

    private CrocodileEntity crocodile;
    private CrocodileBehaviorHandler crocodileManagement;

    private static final int MAX_TAMING_TIME = 12000;
    public static final int ENTITIES_REQUIRED = 40;

    public Entity futurOwner = null;

    public TamingCrocodile(CrocodileEntity crocodile, CrocodileBehaviorHandler crocodileManagement) {
        this.crocodile = crocodile;
        this.crocodileManagement = crocodileManagement;
    }

    public void tick() {
        handleTamingSystem();
    }

    private void handleTamingSystem() {

        if (!this.crocodile.isTame()) {
            if (crocodile.getSacrificesUnity() > 0 && !this.crocodile.isStartingTaming()) {
                this.crocodile.setSacrificesUnity(this.crocodile.getSacrificesUnity() - 0.025f);
            }


            if (this.crocodile.tickCount % 20 == 0) {
                if (this.crocodile.crocodileBehaviorHandler.isReadyForTaming()) {
                    System.out.println("Ready");
                } else {
                    System.out.println(this.crocodile.getSacrificesUnity() + " %");
                }

                System.out.println("Entitées tuées: " + this.crocodile.getEntitiesKilledDuringTaming());
            }

            if (this.crocodile.getTamingTime() > 0) {
                this.crocodile.setTamingTime(this.crocodile.getTamingTime() - 1);

                if (this.crocodile.getTamingTime() <= 0) {
                    stopTaming(this.crocodile.getEntitiesKilledDuringTaming());
                }

                System.out.println(this.crocodile.getTamingTime());
            }
        }
    }

    private void stopTaming(int entitiesKilled) {
        final int minValue = ENTITIES_REQUIRED;
        boolean isSuccessful = entitiesKilled >= minValue;
        int levelPoints = Math.min((entitiesKilled - minValue) / 4, 5);

        if (futurOwner != null) {

            this.crocodile.setTamingTime(0);
            this.crocodile.setSaddle(false);
            this.crocodile.setStartingTaming(false);
            this.crocodile.setEntitiesKilledDuringTaming(0);
            this.crocodile.setSacrificesUnity(0);
            this.crocodile.setPassive(false);

            if (isSuccessful) {
                Player tamer = (Player) futurOwner;
                this.crocodile.setTame(true, tamer);

                this.crocodile.setLevelPoints(levelPoints);
            } else {
                this.crocodile.setTarget(this.crocodile.getControllingPassenger());
            }

            futurOwner.stopRiding();
        }
    }

    public void hurtAfterCombo(LivingEntity entity, int comboAttack) {
        if (!entity.isAlive()) {
            if (this.crocodile.isStartingTaming()) {
                crocodile.setEntitiesKilledDuringTaming(crocodile.getEntitiesKilledDuringTaming() + 1);
            } else if (this.canBeTamable()) {
                if (entity instanceof TamableAnimal tamableAnimal) {
                    if (tamableAnimal.isTame()) {
                        LivingEntity owner = tamableAnimal.getOwner();

                        if (owner != null && owner != entity && owner instanceof Player player) {
                            if (this.ownerIsNear(player, tamableAnimal)) {
                                float entityHealth = entity.getMaxHealth();

                                crocodile.setSacrificesUnity(crocodile.getSacrificesUnity() + entityHealth);
                            }
                        }
                    }
                }
            }
        }
    }

    public InteractionResult mobInteract(Player player, InteractionHand hand) {

        if (hand == InteractionHand.MAIN_HAND) {
            if (!this.crocodile.isTame() && !this.crocodile.isInLava() && this.crocodile.crocodileBehaviorHandler.isReadyForTaming()) {
                this.crocodile.setStartingTaming(true);

                this.crocodile.setSaddle(true);
                this.crocodile.setPassive(true);

                if (this.crocodile.getTamingTime() <= 0) {
                    this.crocodile.setTamingTime(MAX_TAMING_TIME);
                }

                futurOwner = player;

                player.startRiding(this.crocodile);
            }
        }

        return InteractionResult.SUCCESS;
    }

    public boolean canBeTamable() {
        return !crocodile.isTame();
    }

    public boolean ownerIsNear(Player player, TamableAnimal animal) {
        return crocodile.distanceTo(player) <= 20 && player.distanceTo(animal) <= 20;
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        if (this.futurOwner != null) {
            tag.putUUID("futurOwnerUUID", this.futurOwner.getUUID());
        }
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("futurOwnerUUID")) {
            UUID ownerUUID = tag.getUUID("futurOwnerUUID");
            if (crocodile.level() != null) {
                this.futurOwner = ((ServerLevel) crocodile.level()).getEntity(ownerUUID);
            }
        }
    }
}