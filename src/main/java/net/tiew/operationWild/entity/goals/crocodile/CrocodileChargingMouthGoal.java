package net.tiew.operationWild.entity.goals.crocodile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.sound.OWSounds;

import java.util.List;

public class CrocodileChargingMouthGoal extends Goal {

    private final CrocodileEntity crocodile;

    private final int MAX_COOLDOWN = 400;
    private int cooldown = 0;

    public CrocodileChargingMouthGoal(CrocodileEntity crocodile) {
        this.crocodile = crocodile;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.crocodile.getTarget() == null) {
            stop();
            return;
        }

        if (this.crocodile.isChargingMouth()) {
            this.crocodile.setChargingMouthTimer(Math.min(60, this.crocodile.getChargingMouthTimer() + 1.5f));
        } else this.crocodile.setChargingMouthTimer(0);

        if (this.crocodile.distanceTo(this.crocodile.getTarget()) <= 3) {
            if (this.crocodile.getChargingMouthTimer() >= 30) {
                crocodile.crocodileBehaviorHandler.makeBigHurt(this.crocodile.getDamage() * (crocodile.getChargingMouthTimer() / 60), OWSounds.CROCODILE_MOUTH_CRUSH.get(), 3.0f, 2.0f, 2.25f);
                stop();
            }
        }
    }

    @Override
    public void start() {
        super.start();
        float pitch = (float) (OWUtils.generateRandomInterval(1.0, 1.2));
        this.crocodile.setChargingMouth(true);
        this.crocodile.playSound(OWSounds.CROCODILE_IDLE_2.get(), 1.0F, pitch);
    }

    @Override
    public void stop() {
        super.stop();
        crocodile.setChargingMouth(false);
        crocodile.setChargingMouthTimer(0);
        cooldown = MAX_COOLDOWN;
    }

    @Override
    public boolean canContinueToUse() {
        if (crocodile.isBaby()) return false;
        return crocodile.getTarget() != null && crocodile.isChargingMouth() && !this.crocodile.isInWater() && this.crocodile.getTarget().distanceTo(this.crocodile) <= 20
                && !crocodile.isNapping();
    }

    @Override
    public boolean canUse() {
        if (crocodile.isBaby()) return false;
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        return isValidTarget(this.crocodile.getTarget()) && !crocodile.isTame() && !crocodile.isSitting() && !crocodile.isNapping();
    }

    private boolean isValidTarget(LivingEntity target) {
        if (target == null) return false;
        if (target.isInWater() || this.crocodile.isInWater()) return false;
        return crocodile.getRandom().nextInt(20) == 0 && this.crocodile.getHealth() >= this.crocodile.getMaxHealth() / 2 && this.crocodile.distanceTo(target) >= 8;
    }
}