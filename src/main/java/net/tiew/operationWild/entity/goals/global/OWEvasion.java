package net.tiew.operationWild.entity.goals.global;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;
import org.jetbrains.annotations.Nullable;

public class OWEvasion {

    private static final double SCAN_RADIUS = 24.0;
    private static final double LOOKAHEAD_TICKS = 14.0;
    private static final double MISS_MARGIN = 0.9;
    private static final double MIN_PROJECTILE_SPEED_SQR = 0.04;
    private static final int DODGE_COOLDOWN = 50;
    private static final float DODGE_CHANCE = 0.45f;
    private static final double DODGE_IMPULSE = 0.55;
    private static final double DODGE_HOP = 0.32;
    private static final int WEAVE_MIN = 14;
    private static final int WEAVE_MAX = 24;
    private static final double WEAVE_IMPULSE = 0.16;
    private static final double AIM_RANGE = 40.0;
    private static final float DROWSY_DULLING = 0.75f;
    private static final int JUDGED_LIMIT = 64;

    private final OWEntity mob;
    private final IntOpenHashSet judged = new IntOpenHashSet();
    private int dodgeCooldown;
    private int weaveTimer;
    private int weaveSide = 1;

    public OWEvasion(OWEntity mob) {
        this.mob = mob;
    }

    public void reset() {
        judged.clear();
        weaveTimer = 0;
    }

    public void tick(@Nullable Entity threat, float drowsiness) {
        if (dodgeCooldown > 0) dodgeCooldown--;
        if (!mob.onGround() || mob.isInWater()) return;
        float agility = agility() * (1f - DROWSY_DULLING * drowsiness);
        if (agility <= 0.05f) return;
        if (dodgeCooldown <= 0 && tryDodge(agility)) return;
        tickWeave(threat, agility);
    }

    private float agility() {
        return Mth.clamp(1.5f - mob.getBbWidth() * 0.5f, 0.15f, 1f);
    }

    private boolean tryDodge(float agility) {
        if (judged.size() > JUDGED_LIMIT) judged.clear();
        Vec3 center = mob.getBoundingBox().getCenter();
        for (Projectile projectile : mob.level().getEntitiesOfClass(Projectile.class, mob.getBoundingBox().inflate(SCAN_RADIUS),
                candidate -> candidate.isAlive() && candidate.getOwner() != mob)) {
            Vec3 velocity = projectile.getDeltaMovement();
            if (velocity.lengthSqr() < MIN_PROJECTILE_SPEED_SQR) continue;
            Vec3 offset = center.subtract(projectile.position());
            double time = offset.dot(velocity) / velocity.lengthSqr();
            if (time <= 0 || time > LOOKAHEAD_TICKS) continue;
            Vec3 closest = projectile.position().add(velocity.scale(time));
            if (closest.distanceTo(center) > mob.getBbWidth() * 0.5 + MISS_MARGIN) continue;
            if (!judged.add(projectile.getId())) continue;
            if (mob.getRandom().nextFloat() >= DODGE_CHANCE * agility) continue;

            Vec3 side = new Vec3(-velocity.z, 0, velocity.x);
            if (side.lengthSqr() < 1.0E-6) continue;
            side = side.normalize();
            if (side.dot(center.subtract(closest)) < 0) side = side.scale(-1);
            Vec3 motion = mob.getDeltaMovement().add(side.scale(DODGE_IMPULSE * agility));
            mob.setDeltaMovement(motion.x, Math.max(motion.y, DODGE_HOP), motion.z);
            mob.hasImpulse = true;
            dodgeCooldown = DODGE_COOLDOWN;
            return true;
        }
        return false;
    }

    private void tickWeave(@Nullable Entity threat, float agility) {
        if (!(threat instanceof Player player) || !isAiming(player)
                || player.distanceToSqr(mob) > AIM_RANGE * AIM_RANGE || !mob.hasLineOfSight(player)) {
            weaveTimer = 0;
            return;
        }
        if (--weaveTimer > 0) return;
        weaveTimer = WEAVE_MIN + mob.getRandom().nextInt(WEAVE_MAX - WEAVE_MIN + 1);
        weaveSide = -weaveSide;
        Vec3 away = new Vec3(mob.getX() - player.getX(), 0, mob.getZ() - player.getZ());
        if (away.lengthSqr() < 1.0E-6) return;
        away = away.normalize();
        Vec3 lateral = new Vec3(-away.z, 0, away.x).scale(weaveSide * WEAVE_IMPULSE * agility);
        mob.setDeltaMovement(mob.getDeltaMovement().add(lateral));
        mob.hasImpulse = true;
    }

    private static boolean isAiming(Player player) {
        if (!player.isUsingItem()) return false;
        ItemStack stack = player.getUseItem();
        if (stack.getItem() instanceof ProjectileWeaponItem) return true;
        UseAnim animation = stack.getUseAnimation();
        return animation == UseAnim.BOW || animation == UseAnim.CROSSBOW || animation == UseAnim.SPEAR;
    }
}
