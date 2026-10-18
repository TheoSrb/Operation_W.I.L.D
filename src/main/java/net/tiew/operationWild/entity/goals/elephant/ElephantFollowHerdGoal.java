package net.tiew.operationWild.entity.goals.elephant;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.herd.ElephantHerd;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ElephantFollowHerdGoal extends Goal {

    private static final double LEASH = 14.0;
    private static final double SETTLE_DISTANCE = 7.0;
    private static final double SLOT_RADIUS = 6.0;
    private static final double SLOT_DRIFT = 6.0;
    private static final double FAR_DISTANCE = 28.0;
    private static final double WALK_SPEED = 1.0;
    private static final double HURRY_SPEED = 1.6;
    private static final int LOOKUP_INTERVAL = 40;
    private static final int REPATH_INTERVAL = 20;

    private final ElephantEntity elephant;
    @Nullable
    private ElephantEntity matriarch;
    @Nullable
    private Vec3 slotOffset;
    @Nullable
    private Vec3 slotAnchor;
    private int lookupCooldown;
    private int repath;

    public ElephantFollowHerdGoal(ElephantEntity elephant) {
        this.elephant = elephant;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (elephant.isMatriarch() || elephant.isMusth() || !ElephantGoals.isCalm(elephant)) return false;
        if (--lookupCooldown > 0) return false;
        lookupCooldown = LOOKUP_INTERVAL / 2;

        matriarch = ElephantHerd.findMatriarch(elephant);
        if (matriarch == null || matriarch == elephant) return false;
        return elephant.distanceToSqr(matriarch) > LEASH * LEASH;
    }

    @Override
    public boolean canContinueToUse() {
        if (matriarch == null || !matriarch.isAlive() || !matriarch.isMatriarch() || !elephant.isSameHerd(matriarch)) return false;
        if (!ElephantGoals.isCalm(elephant)) return false;
        return elephant.distanceToSqr(matriarch) > SETTLE_DISTANCE * SETTLE_DISTANCE;
    }

    @Override
    public void start() {
        double angle = elephant.getRandom().nextDouble() * Math.PI * 2.0;
        double radius = SLOT_RADIUS * (0.5 + elephant.getRandom().nextDouble() * 0.5);
        slotOffset = new Vec3(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
        slotAnchor = null;
        repath = 0;
    }

    @Override
    public void stop() {
        elephant.getNavigation().stop();
        matriarch = null;
        lookupCooldown = LOOKUP_INTERVAL / 2;
    }

    @Override
    public void tick() {
        if (matriarch == null || slotOffset == null) return;
        boolean drifted = slotAnchor == null || slotAnchor.distanceToSqr(matriarch.position()) > SLOT_DRIFT * SLOT_DRIFT;
        if (--repath > 0 && !drifted) return;
        repath = REPATH_INTERVAL;
        slotAnchor = matriarch.position();

        Vec3 slot = slotAnchor.add(slotOffset);
        double speed = elephant.distanceToSqr(matriarch) > FAR_DISTANCE * FAR_DISTANCE ? HURRY_SPEED : WALK_SPEED;
        if (!elephant.getNavigation().moveTo(slot.x, slot.y, slot.z, speed)) {
            elephant.getNavigation().moveTo(matriarch, speed);
        }
    }
}
