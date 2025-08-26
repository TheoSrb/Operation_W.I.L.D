package net.tiew.operationWild.effect.effects;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.utils.OWUtils;
import org.apache.logging.log4j.core.jmx.Server;

public class FearEffect extends MobEffect {
    public FearEffect(MobEffectCategory effectCategory, int color) {
        super(effectCategory, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
        } else if (entity instanceof Mob mob) {
            mob.setTarget(null);
            mob.setLastHurtByMob(null);
            mob.setAggressive(false);

            Entity rider = mob.getFirstPassenger();

            if (rider != null) {
                if (rider instanceof Player player) {
                    player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, false, false, false));
                }
            }

            if (!mob.getNavigation().isInProgress()) {
                Vec3 randomPos = DefaultRandomPos.getPos((PathfinderMob) mob, 30, 24);
                if (randomPos != null) {
                    float speed = mob instanceof OWEntity owEntity ? owEntity.getSpeed() * 20 : 1.5f;
                    mob.getNavigation().moveTo(randomPos.x, randomPos.y, randomPos.z, speed);
                }
            }
        }
        return super.applyEffectTick(entity, amplifier);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void onEffectAdded(LivingEntity livingEntity, int amplifier) {
        super.onEffectAdded(livingEntity, amplifier);
        if (livingEntity.getFirstPassenger() instanceof ServerPlayer player) {
            OWUtils.showMessage(player, Component.translatable("tooltip.fearStarted"), 0xba6060, true);
        }
    }
}
