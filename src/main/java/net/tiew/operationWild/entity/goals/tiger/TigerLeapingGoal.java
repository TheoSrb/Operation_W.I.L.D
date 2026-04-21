package net.tiew.operationWild.entity.goals.tiger;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.networking.OWNetworkHandler;
import net.tiew.operationWild.networking.packets.to_client.TigerLeapStatePacket;

import java.util.EnumSet;

public class TigerLeapingGoal extends Goal {

    private final TigerEntity tiger;
    private final float minDistance;
    private final float maxDistance;
    private int cooldown = 0;

    private boolean isLeaping = false;
    private boolean isPreparing = false;

    private boolean leapSuccess = false;
    private int leapTick = 0;
    private int prepareTick = 0;
    private static final int PREPARE_DURATION = 10;

    public TigerLeapingGoal(TigerEntity tiger, float minDistance, float maxDistance) {
        this.tiger = tiger;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }

        LivingEntity target = tiger.getTarget();
        if (target == null || !target.isAlive()) return false;
        if (!tiger.onGround()) return false;

        double dist = tiger.distanceTo(target);
        if (dist < minDistance || dist > maxDistance) return false;

        if (isTargetLookingAtTiger(target)) {
            if (OWUtils.RANDOM(3)) {
                if (!tiger.isRoaring()) {
                    tiger.setRoar(true);
                }
            }
            cooldown = 150;
            return false;
        }

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return isLeaping || isPreparing;
    }

    @Override
    public void start() {
        LivingEntity target = tiger.getTarget();
        if (target == null) return;

        isPreparing = true;
        isLeaping = false;
        leapSuccess = false;
        leapTick = 0;
        prepareTick = 0;
        cooldown = 150;

        tiger.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = tiger.getTarget();

        tiger.isLeaping = isLeaping;
        tiger.isPreparing = isPreparing;

        if (tiger.level() instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.players()) {
                OWNetworkHandler.sendToClient(new TigerLeapStatePacket(tiger.getId(), isLeaping, isPreparing), player);
            }
        }

        if (target == null) {
            isPreparing = false;
            isLeaping = false;
            return;
        }

        if (isPreparing) {
            prepareTick++;
            tiger.getNavigation().stop();
            tiger.getLookControl().setLookAt(target, 30f, 30f);
            tiger.setLookAt(target.getX(), target.getY(), target.getZ());

            if (prepareTick >= PREPARE_DURATION) {
                isPreparing = false;
                isLeaping = true;
                leapTick = 0;
                tiger.isPreparing = false;
                tiger.isLeaping = true;
                tiger.leapToTarget(target, 30f, 0.25f);
            }
            return;
        }

        if (isLeaping) {
            leapTick++;

            if (tiger.distanceTo(target) < (tiger.getBbWidth() + target.getBbWidth() + 1.0f)) {
                leapSuccess = true;
                isLeaping = false;
                tiger.isLeaping = false;
                if (tiger.canGrabEntity(target)) {
                    tiger.setGrabbing(true, target);
                }
            }

            if (leapTick > 5 && tiger.onGround()) {
                isLeaping = false;
                tiger.isLeaping = false;
            }
        }
    }

    @Override
    public void stop() {
        isPreparing = false;
        isLeaping = false;
        leapSuccess = false;
        leapTick = 0;
        prepareTick = 0;
        tiger.isLeaping = false;
        tiger.isPreparing = false;

        if (tiger.level() instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.players()) {
                OWNetworkHandler.sendToClient(new TigerLeapStatePacket(tiger.getId(), false, false), player);
            }
        }
    }

    private boolean isTargetLookingAtTiger(LivingEntity target) {
        Vec3 lookVec = target.getLookAngle();
        Vec3 toTiger = tiger.position().subtract(target.position()).normalize();
        double dot = lookVec.dot(toTiger);
        return dot > 0.75;
    }
}