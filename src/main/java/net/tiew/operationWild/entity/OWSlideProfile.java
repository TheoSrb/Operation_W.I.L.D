package net.tiew.operationWild.entity;

import net.minecraft.util.Mth;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants.Slide;

public record OWSlideProfile(
        float strength,
        float energyCost,
        int cooldownTicks,
        int minTicks,
        int maxTicks,
        int iceMaxTicks,
        double minEntryRatio,
        double sprintBoostRatio,
        double walkBoostRatio,
        double maxSpeedRatio,
        double iceMaxSpeedBonus,
        double exitSpeedRatio,
        double groundDrag,
        double slickDrag,
        double airDrag,
        double downhillGain,
        double uphillLoss,
        float steerDegrees,
        double iceSteerGrip,
        boolean canHop,
        double hopLift,
        double hopCarry,
        int handoffTicks,
        double shovePower,
        float fovGain) {

    public static final float MIN_STRENGTH = 0.25f;

    public static final OWSlideProfile DEFAULT = new OWSlideProfile(
            1.0f,
            Slide.ENERGY,
            Slide.COOLDOWN_TICKS,
            Slide.MIN_TICKS,
            Slide.MAX_TICKS,
            Slide.ICE_MAX_TICKS,
            Slide.MIN_ENTRY_RATIO,
            Slide.SPRINT_BOOST_RATIO,
            Slide.WALK_BOOST_RATIO,
            Slide.MAX_SPEED_RATIO,
            Slide.ICE_MAX_SPEED_BONUS,
            Slide.EXIT_SPEED_RATIO,
            Slide.GROUND_DRAG,
            Slide.SLICK_DRAG,
            Slide.AIR_DRAG,
            Slide.DOWNHILL_GAIN,
            Slide.UPHILL_LOSS,
            Slide.STEER_DEGREES,
            Slide.ICE_STEER_GRIP,
            true,
            Slide.HOP_LIFT,
            Slide.HOP_CARRY,
            Slide.HANDOFF_TICKS,
            Slide.SHOVE_POWER,
            Slide.FOV_GAIN);

    public OWSlideProfile withStrength(float value) {
        return new OWSlideProfile(Math.max(MIN_STRENGTH, value), energyCost, cooldownTicks, minTicks, maxTicks, iceMaxTicks,
                minEntryRatio, sprintBoostRatio, walkBoostRatio, maxSpeedRatio, iceMaxSpeedBonus, exitSpeedRatio,
                groundDrag, slickDrag, airDrag, downhillGain, uphillLoss, steerDegrees, iceSteerGrip,
                canHop, hopLift, hopCarry, handoffTicks, shovePower, fovGain);
    }

    public OWSlideProfile withEnergyCost(float value) {
        return new OWSlideProfile(strength, value, cooldownTicks, minTicks, maxTicks, iceMaxTicks,
                minEntryRatio, sprintBoostRatio, walkBoostRatio, maxSpeedRatio, iceMaxSpeedBonus, exitSpeedRatio,
                groundDrag, slickDrag, airDrag, downhillGain, uphillLoss, steerDegrees, iceSteerGrip,
                canHop, hopLift, hopCarry, handoffTicks, shovePower, fovGain);
    }

    public OWSlideProfile withCooldown(int ticks) {
        return new OWSlideProfile(strength, energyCost, ticks, minTicks, maxTicks, iceMaxTicks,
                minEntryRatio, sprintBoostRatio, walkBoostRatio, maxSpeedRatio, iceMaxSpeedBonus, exitSpeedRatio,
                groundDrag, slickDrag, airDrag, downhillGain, uphillLoss, steerDegrees, iceSteerGrip,
                canHop, hopLift, hopCarry, handoffTicks, shovePower, fovGain);
    }

    public OWSlideProfile withShovePower(double value) {
        return new OWSlideProfile(strength, energyCost, cooldownTicks, minTicks, maxTicks, iceMaxTicks,
                minEntryRatio, sprintBoostRatio, walkBoostRatio, maxSpeedRatio, iceMaxSpeedBonus, exitSpeedRatio,
                groundDrag, slickDrag, airDrag, downhillGain, uphillLoss, steerDegrees, iceSteerGrip,
                canHop, hopLift, hopCarry, handoffTicks, value, fovGain);
    }

    public OWSlideProfile withoutHop() {
        return new OWSlideProfile(strength, energyCost, cooldownTicks, minTicks, maxTicks, iceMaxTicks,
                minEntryRatio, sprintBoostRatio, walkBoostRatio, maxSpeedRatio, iceMaxSpeedBonus, exitSpeedRatio,
                groundDrag, slickDrag, airDrag, downhillGain, uphillLoss, steerDegrees, iceSteerGrip,
                false, hopLift, hopCarry, handoffTicks, shovePower, fovGain);
    }

    public double boostRatio(boolean sprinting) {
        return (sprinting ? sprintBoostRatio : walkBoostRatio) * strength;
    }

    public double topSpeedRatio(float slick) {
        return maxSpeedRatio * strength * Mth.lerp(slick, 1.0, iceMaxSpeedBonus);
    }

    public double drag(float slick) {
        return 1.0 - (1.0 - Mth.lerp(slick, groundDrag, slickDrag)) / strength;
    }

    public int durationTicks(float slick) {
        return Math.round(Mth.lerp(slick, (float) maxTicks, (float) iceMaxTicks) * strength);
    }

    public double shove() {
        return shovePower * strength;
    }
}
