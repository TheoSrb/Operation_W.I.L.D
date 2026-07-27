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

    public static final int MAX_TAMING_TIME = 12000;
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
        if (this.crocodile.level().isClientSide() || this.crocodile.isTame()) return;

        if (crocodile.getSacrificesUnity() > 0 && !this.crocodile.isStartingTaming()) {
            this.crocodile.setSacrificesUnity(Math.max(0f, this.crocodile.getSacrificesUnity() - 0.025f));
        }

        if (this.crocodile.getTamingTime() > 0) {
            this.crocodile.setTamingTime(this.crocodile.getTamingTime() - 1);

            if (this.crocodile.getTamingTime() <= 0) {
                stopTaming(this.crocodile.getEntitiesKilledDuringTaming());
            }
        }
    }

    private void stopTaming(int entitiesKilled) {
        final int minValue = ENTITIES_REQUIRED;
        boolean isSuccessful = entitiesKilled >= minValue;
        int levelPoints = Math.max(0, Math.min((entitiesKilled - minValue) / 4, 5));

        Entity owner = futurOwner;
        if (owner == null) return;

        this.crocodile.setTamingTime(0);
        this.crocodile.setSaddle(false);
        this.crocodile.setStartingTaming(false);
        this.crocodile.setEntitiesKilledDuringTaming(0);
        this.crocodile.setSacrificesUnity(0);
        this.crocodile.setPassive(false);

        if (isSuccessful && owner instanceof Player tamer) {
            this.crocodile.setTame(true, tamer);
            this.crocodile.setLevelPoints(levelPoints);
        } else if (owner instanceof LivingEntity failedTamer) {
            // Échec : le dressage se retourne contre celui qui l'a tenté. On vise le joueur
            // lui-même et non getControllingPassenger(), qui vaut null dès qu'il est descendu.
            this.crocodile.setTarget(failedTamer);
        }

        owner.stopRiding();
        futurOwner = null;
    }

    /**
     * Comptabilise une mise à mort, quelle qu'en soit la manière.
     *
     * <p>Appelé depuis {@code CrocodileEntity.killedEntity}, donc <b>après</b> toutes les façons de
     * tuer : morsure de combo, mais aussi noyade d'une proie agrippée et roulade de la mort. Or près
     * d'une source d'eau le crocodile agrippe au lieu de mordre — les sacrifices offerts au bord de
     * l'eau, comme les proies achevées pendant le dressage, ne passaient jamais par l'ancien point
     * d'entrée lié au seul combo, et n'étaient donc jamais comptés.</p>
     */
    public void onKilledEntity(LivingEntity entity) {
        if (entity == null || this.crocodile.level().isClientSide()) return;

        if (this.crocodile.isStartingTaming()) {
            crocodile.setEntitiesKilledDuringTaming(crocodile.getEntitiesKilledDuringTaming() + 1);
            return;
        }

        if (!this.canBeTamable()) return;
        if (!(entity instanceof TamableAnimal tamableAnimal) || !tamableAnimal.isTame()) return;

        LivingEntity owner = tamableAnimal.getOwner();
        if (!(owner instanceof Player player) || owner == entity) return;
        if (!this.ownerIsNear(player, tamableAnimal)) return;

        crocodile.setSacrificesUnity(Math.min(100f, crocodile.getSacrificesUnity() + entity.getMaxHealth()));
    }

    public void hurtAfterCombo(LivingEntity entity, int comboAttack) {
        // Le décompte des sacrifices vit désormais dans onKilledEntity : il couvre aussi les
        // proies noyées ou déchiquetées, que ce point d'entrée ne voyait pas.
    }

    public InteractionResult mobInteract(Player player, InteractionHand hand) {

        if (hand == InteractionHand.MAIN_HAND) {
            if (!this.crocodile.isTame() && !this.crocodile.isInLava() && this.crocodile.crocodileBehaviorHandler.isReadyForTaming()) {
                // La place du dresseur est occupée par une proie : on la relâche, sinon le joueur
                // monterait en deuxième position et ne piloterait rien.
                if (this.crocodile.isGrabbing()) this.crocodile.releaseGrab();

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
        if (tag.hasUUID("futurOwnerUUID") && crocodile.level() instanceof ServerLevel serverLevel) {
            this.futurOwner = serverLevel.getEntity(tag.getUUID("futurOwnerUUID"));
        }
    }
}