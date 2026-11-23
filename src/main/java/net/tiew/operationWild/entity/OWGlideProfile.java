package net.tiew.operationWild.entity;

import net.tiew.operationWild.entity.attacks.OWAttacksConstants.Glide;

public record OWGlideProfile(
        double takeoffLift,
        double takeoffForward,
        int takeoffCooldownTicks,
        float takeoffEnergy,
        double baseSink,
        double minSpeed,
        double cruiseSpeed,
        double maxSpeed,
        double diveGain,
        double climbLoss,
        double drag,
        double liftFactor,
        double stallSpeed,
        double verticalResponse,
        float pitchMin,
        float pitchMax,
        float pitchResponse,
        float turnDegrees,
        float maxBank,
        boolean wildGlide,
        double wildSink,
        double wildMinFall,
        float fovGain) {

    public static final OWGlideProfile DEFAULT = new OWGlideProfile(
            Glide.TAKEOFF_LIFT,
            Glide.TAKEOFF_FORWARD,
            Glide.TAKEOFF_COOLDOWN_TICKS,
            6f,
            Glide.BASE_SINK,
            Glide.MIN_SPEED,
            Glide.CRUISE_SPEED,
            Glide.MAX_SPEED,
            Glide.DIVE_GAIN,
            Glide.CLIMB_LOSS,
            Glide.DRAG,
            Glide.LIFT_FACTOR,
            Glide.STALL_SPEED,
            Glide.VERTICAL_RESPONSE,
            Glide.PITCH_MIN,
            Glide.PITCH_MAX,
            Glide.PITCH_RESPONSE,
            Glide.TURN_DEGREES,
            Glide.MAX_BANK,
            true,
            Glide.WILD_SINK,
            Glide.WILD_MIN_FALL,
            Glide.FOV_GAIN);

    public OWGlideProfile withSpeedScale(double scale) {
        return new OWGlideProfile(takeoffLift, takeoffForward * scale, takeoffCooldownTicks, takeoffEnergy, baseSink,
                minSpeed * scale, cruiseSpeed * scale, maxSpeed * scale, diveGain * scale, climbLoss * scale, drag * scale,
                liftFactor, stallSpeed * scale, verticalResponse, pitchMin, pitchMax, pitchResponse, turnDegrees, maxBank,
                wildGlide, wildSink, wildMinFall, fovGain);
    }

    public OWGlideProfile withSink(double sink) {
        return new OWGlideProfile(takeoffLift, takeoffForward, takeoffCooldownTicks, takeoffEnergy, sink,
                minSpeed, cruiseSpeed, maxSpeed, diveGain, climbLoss, drag, liftFactor, stallSpeed, verticalResponse,
                pitchMin, pitchMax, pitchResponse, turnDegrees, maxBank, wildGlide, wildSink, wildMinFall, fovGain);
    }

    public OWGlideProfile withTakeoff(double lift, float energy) {
        return new OWGlideProfile(lift, takeoffForward, takeoffCooldownTicks, energy, baseSink,
                minSpeed, cruiseSpeed, maxSpeed, diveGain, climbLoss, drag, liftFactor, stallSpeed, verticalResponse,
                pitchMin, pitchMax, pitchResponse, turnDegrees, maxBank, wildGlide, wildSink, wildMinFall, fovGain);
    }

    public OWGlideProfile withoutWildGlide() {
        return new OWGlideProfile(takeoffLift, takeoffForward, takeoffCooldownTicks, takeoffEnergy, baseSink,
                minSpeed, cruiseSpeed, maxSpeed, diveGain, climbLoss, drag, liftFactor, stallSpeed, verticalResponse,
                pitchMin, pitchMax, pitchResponse, turnDegrees, maxBank, false, wildSink, wildMinFall, fovGain);
    }
}
