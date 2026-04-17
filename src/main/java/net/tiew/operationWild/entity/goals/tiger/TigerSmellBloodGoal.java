package net.tiew.operationWild.entity.goals.tiger;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;

import java.util.EnumSet;
import java.util.List;

public class TigerSmellBloodGoal extends Goal {

    private final TigerEntity tiger;
    private final double detectionRadius;
    private LivingEntity target;
    private int searchCooldown = 0;

    private static final double HP_THRESHOLD = 0.70;

    public TigerSmellBloodGoal(TigerEntity tiger, double detectionRadius) {
        this.tiger = tiger;
        this.detectionRadius = detectionRadius;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (tiger.isLeaping || tiger.isPreparing) return false;
        if (tiger.isGrabbing()) return false;
        if (tiger.isRoaring()) return false;
        if (tiger.isNapping()) return false;
        if (tiger.getTarget() != null) return false;

        if (searchCooldown > 0) {
            searchCooldown--;
            return false;
        }

        target = findInjuredEntity();
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (target == null || !target.isAlive()) return false;
        if (tiger.isGrabbing()) return false;
        if (tiger.isNapping()) return false;
        if (tiger.getTarget() != null) return false;
        if (target.getHealth() / target.getMaxHealth() >= HP_THRESHOLD) return false;
        if (tiger.distanceTo(target) > detectionRadius + 8) return false;

        return true;
    }

    @Override
    public void start() {
        if (target == null) return;
        tiger.getNavigation().moveTo(target, 0.6D);
    }

    @Override
    public void tick() {
        if (target == null) return;

        if (tiger.tickCount % 20 == 0) {
            tiger.getNavigation().moveTo(target, 0.6D);
        }

        tiger.getLookControl().setLookAt(
                target.getX(),
                target.getEyeY(),
                target.getZ(),
                10f, 10f
        );

        if (tiger.distanceTo(target) < 3.0) {
            tiger.setTarget(target);
            stop();
        }
    }

    @Override
    public void stop() {
        tiger.getNavigation().stop();
        target = null;
        searchCooldown = 100;
    }

    private LivingEntity findInjuredEntity() {
        List<LivingEntity> candidates = tiger.level().getEntitiesOfClass(
                LivingEntity.class,
                tiger.getBoundingBox().inflate(detectionRadius),
                entity -> isValidTarget(entity)
        );

        return candidates.stream()
                .filter(e -> e.getHealth() / e.getMaxHealth() < HP_THRESHOLD)
                .min((a, b) -> Float.compare(
                        a.getHealth() / a.getMaxHealth(),
                        b.getHealth() / b.getMaxHealth()
                ))
                .orElse(null);
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (entity == tiger) return false;
        if (!entity.isAlive()) return false;
        if (entity.isSpectator()) return false;
        if (tiger.isAlliedTo(entity)) return false;
        return entity instanceof Animal || entity instanceof Player || entity instanceof Monster;
    }
}