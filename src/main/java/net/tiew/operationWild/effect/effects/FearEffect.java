package net.tiew.operationWild.effect.effects;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.OWEntity;

public class FearEffect extends MobEffect {
    public FearEffect(MobEffectCategory effectCategory, int color) {
        super(effectCategory, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return super.applyEffectTick(entity, amplifier);

        if (entity instanceof OWEntity) return super.applyEffectTick(entity, amplifier);

        if (entity instanceof PathfinderMob mob) {
            mob.setTarget(null);
            mob.setLastHurtByMob(null);
            mob.setAggressive(false);

            if (!mob.getNavigation().isInProgress()) {
                Vec3 randomPos = DefaultRandomPos.getPos(mob, 30, 24);
                if (randomPos != null) {
                    mob.getNavigation().moveTo(randomPos.x, randomPos.y, randomPos.z, 1.5f);
                }
            }
        } else if (entity instanceof Mob mob) {
            mob.setTarget(null);
            mob.setLastHurtByMob(null);
            mob.setAggressive(false);
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
        if (livingEntity instanceof OWEntity) return;
        if (livingEntity.getFirstPassenger() instanceof ServerPlayer player) {
            OWUtils.showMessage(player, Component.translatable("tooltip.fearStarted", livingEntity.getType().getDescription()), 0xE04A2F, true);
        }
    }
}
