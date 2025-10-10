package net.tiew.operationWild.entity.goals.walrus;

import com.mojang.datafixers.types.templates.Hook;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.aquatic.WalrusEntity;

import java.util.List;

public class WalrusCaughtGoal extends Goal {

    private final WalrusEntity walrus;

    private LivingEntity surfer = null;
    private FishingHook currentHook = null;

    private boolean isCollideWithFishingHook = false;

    private int surfingTimer = 0;
    private final int MAX_SURFING_TIMER = 600;


    public WalrusCaughtGoal(WalrusEntity walrus) {
        this.walrus = walrus;
    }

    @Override
    public void tick() {
        super.tick();
        surfingTimer++;

        if (this.walrus.isAlive() && this.walrus.isSurfing() && this.surfer != null && this.surfer.isAlive()) {
            Vec3 look = walrus.getLookAngle();

            double walrusX = this.walrus.getX();
            double walrusY = this.walrus.getY();
            double walrusZ = this.walrus.getZ();

            float oscillation = (float) (3 * Math.sin(this.walrus.tickCount));

            double distanceBack = 7;

            this.surfer.teleportTo(walrusX - (look.x * distanceBack), walrusY, walrusZ - (look.z * distanceBack));

            if (this.currentHook != null && this.currentHook.isAlive()) {
                this.currentHook.setPosRaw(walrusX, walrusY, walrusZ);
                this.currentHook.setPos(walrusX, walrusY, walrusZ);
                this.currentHook.setDeltaMovement(Vec3.ZERO);
                this.currentHook.xOld = walrusX;
                this.currentHook.yOld = walrusY;
                this.currentHook.zOld = walrusZ;
            }
        }
    }

    @Override
    public void start() {
        super.start();
        System.out.println("Le Hook est accroché !");

        if (surfer != null) {
            this.walrus.setSurfing(true);
            System.out.println("On commence le surf, avec comme surfer: " + surfer);
        } else {
            this.walrus.setSurfing(false);
            stop();
        }
    }

    @Override
    public void stop() {
        super.stop();
        surfingTimer = 0;
        surfer = null;
        currentHook = null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.surfer == null || !this.surfer.isAlive() || surfingTimer >= MAX_SURFING_TIMER) {
            return false;
        }

        List<FishingHook> fishingHooks = this.walrus.level().getEntitiesOfClass(FishingHook.class, this.walrus.getBoundingBox().inflate(1.0));

        for (FishingHook fishingHook : fishingHooks) {
            if (fishingHook.getOwner() == this.surfer) {
                this.currentHook = fishingHook;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canUse() {
        isCollideWithFishingHook = false;
        List<FishingHook> fishingHooks = this.walrus.level().getEntitiesOfClass(FishingHook.class, this.walrus.getBoundingBox().inflate(1.0));

        for (FishingHook fishingHook : fishingHooks) {
            if (fishingHook.getOwner() instanceof LivingEntity) {
                this.surfer = (LivingEntity) fishingHook.getOwner();
                this.currentHook = fishingHook;
                isCollideWithFishingHook = true;
                break;
            }
        }

        return isCollideWithFishingHook && !walrus.isTame();
    }

    protected boolean isValidSurfer(LivingEntity surfer) {
        if (surfer instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) return false;
        }
        return surfer != null && this.walrus.getTarget() == null;
    }
}