package net.tiew.operationWild.entity.goals.lion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.clan.LionClanData;
import net.tiew.operationWild.entity.clan.LionPride;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;

public class LionFeastGoal extends Goal {

    private static final int CHECK_INTERVAL = 10;
    private static final double SEARCH_RADIUS = 16.0;
    private static final double FEAST_SEARCH_RANGE = 48.0;
    private static final double FEAST_SPOT_RADIUS = 6.0;
    private static final double NEXT_ITEM_RADIUS = 4.0;
    private static final double EAT_RANGE = 1.6;
    private static final int EAT_INTERVAL = 30;
    private static final double WAIT_RING = 3.5;
    private static final int MALE_SHARE = 3;
    private static final int MAX_WAIT_TICKS = 600;
    private static final double MALE_WAIT_RANGE = 40.0;
    private static final double CUB_WAIT_RANGE = 6.0;
    private static final float FULL_RATIO = 0.97f;
    private static final int REPATH_TICKS = 10;
    private static final int GROWL_CHANCE = 90;

    private final LionEntity lion;
    @Nullable
    private ItemEntity meat;
    private int repath;
    private int eatTimer;
    private int waitTicks;
    private boolean waitedOut;

    public LionFeastGoal(LionEntity lion) {
        this.lion = lion;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public static boolean isMeat(ItemEntity item) {
        if (!item.isAlive() || item.getItem().isEmpty()) return false;
        ItemStack stack = item.getItem();
        return stack.is(Tags.Items.FOODS_RAW_MEAT) || stack.is(OWTags.Items.LION_FOOD);
    }

    @Override
    public boolean canUse() {
        if (!LionGoals.onSchedule(lion, CHECK_INTERVAL) || !eligible()) return false;
        meat = findMeat();
        return meat != null;
    }

    private boolean eligible() {
        if (!LionGoals.isFreeWild(lion) || lion.getTarget() != null || lion.isNapping() || lion.isInWater()) return false;
        LionClanData.Clan clan = lion.wildClan();
        if (clan != null && clan.satietyRatio() >= FULL_RATIO) return false;
        return clan == null || LionGoals.huntRole(lion, clan) == null;
    }

    @Nullable
    private ItemEntity findMeat() {
        ItemEntity near = closestMeat(lion.position(), SEARCH_RADIUS);
        if (near != null) return near;
        LionClanData.Clan clan = lion.wildClan();
        if (clan == null || !lion.isMale() || !(lion.level() instanceof ServerLevel level)) return null;
        if (!clan.state.isFeasting(level.getGameTime())) return null;
        Vec3 spot = Vec3.atCenterOf(clan.state.feastPos);
        if (spot.distanceTo(lion.position()) > FEAST_SEARCH_RANGE) return null;
        return closestMeat(spot, FEAST_SPOT_RADIUS);
    }

    @Nullable
    private ItemEntity closestMeat(Vec3 around, double radius) {
        return lion.level().getEntitiesOfClass(ItemEntity.class, new AABB(around, around).inflate(radius), LionFeastGoal::isMeat)
                .stream()
                .min(Comparator.comparingDouble(lion::distanceToSqr))
                .orElse(null);
    }

    @Override
    public boolean canContinueToUse() {
        if (!eligible()) return false;
        if (meat == null || !meat.isAlive()) {
            meat = meat == null ? null : closestMeat(meat.position(), NEXT_ITEM_RADIUS);
        }
        return meat != null;
    }

    @Override
    public void start() {
        repath = 0;
        eatTimer = 0;
        waitTicks = 0;
        waitedOut = false;
        lion.setRunning(false);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (meat == null) return;
        Vec3 food = meat.position();

        if (mustWait()) {
            lion.wild.eating = false;
            waitTicks++;
            Vec3 ring = ringPoint(food);
            if (LionGoals.horizontalDistance(lion, ring) > 1.2) {
                lion.setWildPose(LionEntity.WILD_POSE_NONE);
                if (--repath <= 0) {
                    repath = REPATH_TICKS;
                    lion.getNavigation().moveTo(ring.x, ring.y, ring.z, 1.0);
                }
            } else {
                LionGoals.holdStill(lion);
                lion.setWildPose(LionEntity.WILD_POSE_THREAT);
                if (lion.getRandom().nextInt(GROWL_CHANCE) == 0) {
                    lion.level().playSound(null, lion.getX(), lion.getY(), lion.getZ(), OWSounds.TIGER_IDLE.get(),
                            SoundSource.NEUTRAL, 1.0f, LionPride.growlPitch(lion) * 1.1f);
                }
            }
            lion.getLookControl().setLookAt(food.x, food.y, food.z, 20f, 20f);
            return;
        }

        lion.getLookControl().setLookAt(food.x, food.y, food.z, 30f, 30f);
        if (lion.distanceToSqr(meat) > EAT_RANGE * EAT_RANGE) {
            lion.wild.eating = false;
            lion.setWildPose(LionEntity.WILD_POSE_NONE);
            if (--repath <= 0) {
                repath = REPATH_TICKS;
                lion.getNavigation().moveTo(meat, 1.1);
            }
            return;
        }

        LionGoals.holdStill(lion);
        lion.wild.eating = true;
        lion.setWildPose(LionEntity.WILD_POSE_EAT);
        if (++eatTimer % 6 == 0) chewParticles(meat.getItem());
        if (eatTimer >= EAT_INTERVAL) {
            eatTimer = 0;
            swallow();
        }
    }

    private boolean mustWait() {
        if (waitedOut || meat == null) return false;
        if (waitTicks > MAX_WAIT_TICKS) {
            waitedOut = true;
            return false;
        }
        if (lion.isBaby()) {
            ItemEntity food = meat;
            return !lion.level().getEntitiesOfClass(LionEntity.class, food.getBoundingBox().inflate(CUB_WAIT_RANGE),
                    other -> other != lion && !other.isBaby() && other.wild.eating).isEmpty();
        }
        if (lion.isMale()) return false;
        LionClanData.Clan clan = lion.wildClan();
        if (clan == null || clan.state.maleShare >= MALE_SHARE || !(lion.level() instanceof ServerLevel level)) return false;
        LionEntity leader = LionPride.loadedLeader(level, clan);
        if (leader == null || leader == lion || !leader.isMale() || leader.isKnockedOut() || leader.isSleeping()) return false;
        if (leader.distanceTo(meat) > MALE_WAIT_RANGE || leader.getTarget() != null) return false;
        return !leader.isNapping() || leader.distanceTo(meat) <= 16.0;
    }

    private Vec3 ringPoint(Vec3 food) {
        double angle = (Math.floorMod(lion.getUUID().hashCode(), 360)) * Math.PI / 180.0;
        return food.add(Math.cos(angle) * WAIT_RING, 0, Math.sin(angle) * WAIT_RING);
    }

    private void chewParticles(ItemStack stack) {
        if (!(lion.level() instanceof ServerLevel level) || stack.isEmpty()) return;
        Vec3 look = lion.getLookAngle();
        double reach = 1.1 * lion.getScale();
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack),
                lion.getX() + look.x * reach, lion.getY() + 0.35 * lion.getScale(), lion.getZ() + look.z * reach,
                3, 0.12, 0.08, 0.12, 0.03);
    }

    private void swallow() {
        if (meat == null || !meat.isAlive() || !(lion.level() instanceof ServerLevel level)) return;
        ItemStack stack = meat.getItem();
        ItemStack eaten = stack.copyWithCount(1);
        BlockPos spot = meat.blockPosition();
        stack.shrink(1);
        if (stack.isEmpty()) meat.discard();
        else meat.setItem(stack);
        level.playSound(null, lion.getX(), lion.getY(), lion.getZ(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 1.0f,
                lion.isMale() ? 0.7f : 0.85f);
        LionPride.onMeatEaten(level, lion, eaten, spot);
    }

    @Override
    public void stop() {
        lion.wild.eating = false;
        lion.setWildPose(LionEntity.WILD_POSE_NONE);
        lion.getNavigation().stop();
        meat = null;
    }
}
