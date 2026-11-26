package net.tiew.operationWild.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.core.OWArena;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants.Flight;
import net.tiew.operationWild.entity.goals.flying.OWAerialWanderGoal;
import net.tiew.operationWild.entity.navigation.OWGroundPathNavigation;
import org.jetbrains.annotations.Nullable;

public abstract class OWSemiFlyingEntity extends OWEntity {

    public static final int FLIGHT_GROUND = 0;
    public static final int FLIGHT_FLAP = 1;
    public static final int FLIGHT_FAST = 2;
    public static final int FLIGHT_CLIMB = 3;
    public static final int FLIGHT_GLIDE = 4;
    public static final int FLIGHT_HOVER = 5;

    private static final EntityDataAccessor<Integer> FLIGHT_STATE = SynchedEntityData.defineId(OWSemiFlyingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FLIGHT_TAKEOFF = SynchedEntityData.defineId(OWSemiFlyingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FLIGHT_LAND = SynchedEntityData.defineId(OWSemiFlyingEntity.class, EntityDataSerializers.INT);

    private static final float AIR_BLEND_IN = 0.12f;
    private static final float AIR_BLEND_OUT = 0.2f;
    private static final float SOAR_BLEND_RATE = 0.06f;
    private static final float CLIMB_BLEND_RATE = 0.1f;
    private static final double ARRIVAL_DISTANCE_SQR = 1.5;
    private static final double LANDING_SLOWDOWN = 0.15;
    private static final int LANDING_SEARCH_RADIUS = 20;
    private static final int LANDING_SEARCH_SAMPLES = 14;
    private static final int KO_FALL_GRACE_TICKS = 10;

    private final OWFlightController flight = new OWFlightController(this);
    private final OWGroundPathNavigation groundNavigation;
    private final OWFlyingNavigation airNavigation;

    private int fallGraceTicks = 0;
    private int koFallTicks = 0;
    private boolean fellFromFlight = false;
    private int staleGroundTicks = 0;
    private int takeoffCooldown = 0;
    private int airTaskTicks = 0;
    private boolean landingRequested = false;
    private Vec3 landingTarget = null;
    private float wildYaw = 0f;
    private double wildSpeed = 0;
    private int wildSteerTick = Integer.MIN_VALUE;

    private int seenTakeoff = 0;
    private int seenLand = 0;
    private int takeoffAge = -1;
    private int landAge = -1;
    private float airBlend, airBlendO;
    private float soarBlend, soarBlendO;
    private float climbBlend, climbBlendO;
    private float hoverBlend, hoverBlendO;
    private float flapClock = 0f;
    private float flapClockO = 0f;

    protected OWSemiFlyingEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
        this.groundNavigation = new OWGroundPathNavigation(this, level);
        this.airNavigation = new OWFlyingNavigation(this, level);
        this.moveControl = new OWFlyMoveControl(this);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLIGHT_STATE, FLIGHT_GROUND);
        builder.define(FLIGHT_TAKEOFF, 0);
        builder.define(FLIGHT_LAND, 0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(9, new OWAerialWanderGoal(this));
    }

    /** Cruising speed in blocks per tick while the rider holds forward. */
    public double vehicleFlySpeed() {
        return Flight.FLY_SPEED;
    }

    /** Fast flight speed in blocks per tick, used while the rider sprints forward. */
    public double vehicleFlyRunSpeed() {
        return Flight.FLY_RUN_SPEED;
    }

    /** Top speed reached in a dive; the steeper the dive, the closer the mount gets to it. */
    public double vehicleDiveSpeed() {
        return Flight.DIVE_SPEED;
    }

    /** Speed above which the mount switches to gliding. Set higher than the fast flight speed so that only a dive can reach it. */
    public double vehicleGlideEntrySpeed() {
        return Flight.GLIDE_ENTRY_SPEED;
    }

    /** Speed below which a glide ends and the mount starts flapping again. */
    public double vehicleGlideExitSpeed() {
        return Flight.GLIDE_EXIT_SPEED;
    }

    /** Speed lost each tick while gliding; a higher value makes glides shorter. */
    public double vehicleGlideDrag() {
        return Flight.GLIDE_DRAG;
    }

    /** Height lost each tick while gliding level; lower values give flatter, longer glides. */
    public double vehicleGlideSink() {
        return Flight.GLIDE_SINK;
    }

    /** Speed gained per tick while gliding nose down, scaled by how steep the dive is. */
    public double vehicleGlideDiveGain() {
        return Flight.GLIDE_DIVE_GAIN;
    }

    /** Speed lost per tick while gliding nose up, scaled by how steep the climb is. */
    public double vehicleGlideClimbLoss() {
        return Flight.GLIDE_CLIMB_LOSS;
    }

    /** How much height a fast glide regains when pulling the nose up, trading speed for altitude. */
    public double vehicleGlideLiftFactor() {
        return Flight.GLIDE_LIFT_FACTOR;
    }

    /** Vertical speed while the rider holds Jump to climb. */
    public double vehicleClimbSpeed() {
        return Flight.CLIMB_SPEED;
    }

    /** Maximum sink speed while hovering, reached by looking straight down. */
    public double vehicleDescendSpeed() {
        return Flight.DESCEND_SPEED;
    }

    /** Initial upward impulse on takeoff, which sets how high the mount jumps off the ground. */
    public double vehicleTakeoffLift() {
        return Flight.TAKEOFF_LIFT;
    }

    /** Fraction of the gap to the target speed recovered each tick when speeding up. Lower values feel heavier. */
    public double vehicleFlyAcceleration() {
        return Flight.FLY_ACCELERATION;
    }

    /** Fraction of the speed lost each tick when the rider releases forward. The Back key brake uses its own constant. */
    public double vehicleFlyDeceleration() {
        return Flight.FLY_DECELERATION;
    }

    /** Maximum turn rate in degrees per tick during normal flight; the actual rate eases in and out toward it. */
    public float vehicleFlyTurnSpeed() {
        return Flight.FLY_TURN_SPEED;
    }

    /** Maximum turn rate in degrees per tick during fast flight, usually wider than normal flight. */
    public float vehicleFlyRunTurnSpeed() {
        return Flight.FLY_RUN_TURN_SPEED;
    }

    /** Maximum turn rate in degrees per tick while gliding. */
    public float vehicleGlideTurnSpeed() {
        return Flight.GLIDE_TURN_SPEED;
    }

    /** Vital energy added each tick while flapping forward (energy is inverted: higher means more tired). */
    public float flyEnergyCost() {
        return Flight.FLY_ENERGY;
    }

    /** Extra vital energy per tick during fast flight, on top of the regular running cost of OWEntity. */
    public float flyRunEnergyCost() {
        return Flight.FLY_RUN_ENERGY;
    }

    /** Vital energy added each tick while hovering in place. */
    public float hoverEnergyCost() {
        return Flight.HOVER_ENERGY;
    }

    /** Vital energy added each tick while climbing with Jump held. */
    public float climbEnergyCost() {
        return Flight.CLIMB_ENERGY;
    }

    /** Vital energy added each tick while gliding; kept very low so gliding is almost free. */
    public float glideEnergyCost() {
        return Flight.GLIDE_ENERGY;
    }

    /** Flat vital energy cost of each takeoff from the ground. */
    public float takeoffEnergyCost() {
        return Flight.TAKEOFF_ENERGY;
    }

    /** Flight speed of the unridden AI, multiplied when fleeing or chasing a target. */
    public double wildFlySpeed() {
        return Flight.WILD_FLY_SPEED;
    }

    /** Speed of the unridden AI while gliding in circles. */
    public double wildGlideSpeed() {
        return Flight.WILD_GLIDE_SPEED;
    }

    /** Turn rate of the unridden AI in degrees per tick. */
    public float wildFlyTurnSpeed() {
        return Flight.WILD_TURN_SPEED;
    }

    /** Half wingspan in blocks, used to place the wind trails at the wing tips. */
    public double wingSpan() {
        return this.getBbWidth() * 1.4 + 0.5;
    }

    /** Sound played on each wing beat, in sync with the flap cycle. Returns null for a silent flyer. */
    @Nullable
    public SoundEvent wingFlapSound() {
        return null;
    }

    /** Length in seconds of one wing beat for the given flight state; match it to the flight animations so sound and wings stay in sync. */
    public float flapCycleSeconds(int state) {
        return state == FLIGHT_FAST ? 0.5f : 1.0f;
    }

    /** Blocks that create a rising thermal under a gliding mount, slowing its descent. */
    public boolean isThermalSource(BlockState state) {
        return state.is(BlockTags.CAMPFIRES) || state.is(BlockTags.FIRE) || state.is(Blocks.LAVA) || state.is(Blocks.MAGMA_BLOCK);
    }

    @Override
    public int arenaTerrainMask() {
        return OWArena.Terrain.TERRESTRIAL.bit();
    }

    // ==================================================
    //                    ÉTAT DE VOL
    // ==================================================

    public int getSyncedFlightState() {
        return this.entityData.get(FLIGHT_STATE);
    }

    public int getFlightState() {
        if (flight.isLocalPilot()) return flight.localState();
        return getSyncedFlightState();
    }

    public boolean isAirborneFlight() {
        return getFlightState() != FLIGHT_GROUND;
    }

    public boolean isSoaring() {
        return getFlightState() == FLIGHT_GLIDE;
    }

    private void setFlightState(int state) {
        if (getSyncedFlightState() == state) return;
        boolean wasAirborne = getSyncedFlightState() != FLIGHT_GROUND;
        this.entityData.set(FLIGHT_STATE, state);
        if (wasAirborne && state == FLIGHT_GROUND) {
            this.entityData.set(FLIGHT_LAND, this.entityData.get(FLIGHT_LAND) + 1);
            this.fallGraceTicks = Flight.FALL_GRACE_TICKS;
            this.landingRequested = false;
            this.landingTarget = null;
            if (this.getNavigation() == airNavigation) airNavigation.stop();
        }
    }

    /** Whether the mount may fly right now. If this turns false mid-air, the mount falls at a capped speed without fall damage. */
    public boolean canFlyNow() {
        return this.isAlive() && !this.isKnockedOut() && !this.isSleeping() && !this.isNapping() && !this.isBaby()
                && !this.isLeashed() && !this.isInResurrection() && !this.isSedationWaking() && !this.isPassenger()
                && !this.isImmobile();
    }

    /** Whether there is enough free space above the mount to take off. */
    public boolean hasTakeoffClearance() {
        return this.level().noCollision(this, this.getBoundingBox().move(0, 1.5, 0));
    }

    /** Whether the mount stands on solid ground with room above to take off. Taking off from water or any other liquid is not allowed. */
    public boolean canTakeoffFromHere() {
        if (!this.onGround() || this.isInWater() || this.isInLava() || !this.level().getFluidState(this.blockPosition()).isEmpty()) return false;
        return hasTakeoffClearance();
    }

    public float takeoffEnergy() {
        return takeoffEnergyCost();
    }

    public float flightEnergyRatio() {
        float capacity = this.getVitalEnergyCapacity();
        return capacity <= 0f ? 1f : this.getVitalEnergy() / capacity;
    }

    public float flightFovModifier() {
        return flight.fovModifier();
    }

    @Override
    public float windRushTarget(Player rider) {
        if (getFlightState() != FLIGHT_GLIDE || !this.hasPassenger(rider)) return super.windRushTarget(rider);
        return Mth.clamp(0.45f + (flight.speedFactor() - 0.6f) * 0.55f, 0.45f, 1f);
    }

    // ==================================================
    //                  PILOTAGE MONTÉ
    // ==================================================

    @Override
    protected boolean ownsRiddenMotion() {
        return flight.ownsMotion();
    }

    @Override
    protected void afterRiddenTick(Player player, float yawOBefore, float yawBefore, float bodyBefore) {
        flight.afterRiddenRotation(player, yawOBefore, yawBefore, bodyBefore);
    }

    public void serverRiderTakeoff(Player rider) {
        if (this.level().isClientSide() || takeoffCooldown > 0 || !canFlyNow() || !canTakeoffFromHere()) return;
        this.setVitalEnergy(Math.min(this.getVitalEnergyCapacity(), this.getVitalEnergy() + takeoffEnergy()));
        beginServerTakeoff(rider);
    }

    public void serverRiderState(int state) {
        if (this.level().isClientSide()) return;
        if (state != FLIGHT_GROUND && !canFlyNow()) return;
        if (state < FLIGHT_GROUND || state > FLIGHT_HOVER) return;
        if (state != FLIGHT_GROUND) staleGroundTicks = 0;
        setFlightState(state);
    }

    public void serverRiderLand() {
        if (this.level().isClientSide()) return;
        setFlightState(FLIGHT_GROUND);
    }

    private void beginServerTakeoff(@Nullable Player rider) {
        takeoffCooldown = Math.max(0, Flight.TAKEOFF_COOLDOWN_TICKS - 4);
        staleGroundTicks = 0;
        fellFromFlight = false;
        this.entityData.set(FLIGHT_STATE, FLIGHT_HOVER);
        this.entityData.set(FLIGHT_TAKEOFF, this.entityData.get(FLIGHT_TAKEOFF) + 1);
        this.resetFallDistance();
        if (this.level() instanceof ServerLevel server) spawnTakeoffDust(server);
        SoundEvent flap = wingFlapSound();
        if (flap != null) {
            this.level().playSound(rider, this.getX(), this.getY(), this.getZ(), flap, SoundSource.NEUTRAL, 1.2f, 0.75f + this.random.nextFloat() * 0.15f);
        }
    }

    void playTakeoffEffectsLocal() {
        SoundEvent flap = wingFlapSound();
        if (flap != null) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), flap, this.getSoundSource(), 1.2f, 0.75f + this.random.nextFloat() * 0.15f, false);
        }
    }

    private void spawnTakeoffDust(ServerLevel server) {
        BlockPos below = this.blockPosition().below();
        BlockState ground = server.getBlockState(below);
        if (!ground.isAir()) {
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), this.getX(), this.getY() + 0.1, this.getZ(),
                    14, this.getBbWidth() * 1.2, 0.1, this.getBbWidth() * 1.2, 0.15);
        }
        server.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 0.2, this.getZ(), 3, this.getBbWidth(), 0.1, this.getBbWidth(), 0.02);
    }

    // ==================================================
    //                 CORPS DU FONCTIONNEMENT
    // ==================================================

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            flight.tickVisuals();
            tickFlightVisuals();
            return;
        }

        if (takeoffCooldown > 0) takeoffCooldown--;
        boolean ridden = this.getControllingPassenger() instanceof Player;
        int state = getSyncedFlightState();

        if (state != FLIGHT_GROUND) {
            this.resetFallDistance();
            fallGraceTicks = Flight.FALL_GRACE_TICKS;
            drainFlightEnergy(state);

            if (!canFlyNow() || this.isInLava()) {
                fellFromFlight = true;
                setFlightState(FLIGHT_GROUND);
            } else if (ridden) {
                boolean grounded = this.onGround() || this.isInWater();
                staleGroundTicks = grounded ? staleGroundTicks + 1 : 0;
                if (staleGroundTicks > Flight.STALE_GROUND_TICKS) setFlightState(FLIGHT_GROUND);
            } else if (this.isEffectiveAi()) {
                tickWildAirborne();
            }
        } else {
            if (fallGraceTicks > 0) fallGraceTicks--;
            if (!ridden && this.isEffectiveAi()) tickWildGrounded();
        }

        if (fellFromFlight) tickKnockedOutFall();
        if (!ridden && this.isEffectiveAi()) switchNavigation();
    }

    private void drainFlightEnergy(int state) {
        float drain = switch (state) {
            case FLIGHT_CLIMB -> climbEnergyCost();
            case FLIGHT_GLIDE -> glideEnergyCost();
            case FLIGHT_HOVER -> hoverEnergyCost();
            case FLIGHT_FAST -> flyEnergyCost() + flyRunEnergyCost();
            default -> flyEnergyCost();
        };
        this.setVitalEnergy(Math.min(this.getVitalEnergyCapacity(), this.getVitalEnergy() + drain));
    }

    private void tickKnockedOutFall() {
        Vec3 motion = this.getDeltaMovement();
        if (motion.y < -Flight.KO_FALL_SPEED_LIMIT) {
            this.setDeltaMovement(motion.x * 0.9, -Flight.KO_FALL_SPEED_LIMIT, motion.z * 0.9);
            this.hasImpulse = true;
        }
        this.resetFallDistance();
        if (this.onGround() || this.isInWater()) {
            if (++koFallTicks > KO_FALL_GRACE_TICKS) {
                fellFromFlight = false;
                koFallTicks = 0;
            }
        }
    }

    @Override
    protected boolean absorbsFallDamage() {
        return isAirborneFlight() || fallGraceTicks > 0 || fellFromFlight;
    }

    @Override
    protected boolean pausesEnergyRecovery() {
        return isAirborneFlight() || super.pausesEnergyRecovery();
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (isAirborneFlight()) {
            boolean piloted = this.getControllingPassenger() instanceof Player;
            if ((piloted && this.isControlledByLocalInstance()) || (!piloted && this.isEffectiveAi())) {
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.calculateEntityAnimation(false);
                return;
            }
        }
        super.travel(travelVector);
    }

    // ==================================================
    //                    VOL SAUVAGE
    // ==================================================

    public void keepAirborne(int ticks) {
        airTaskTicks = Math.max(airTaskTicks, ticks);
        if (ticks > 0) landingRequested = false;
    }

    public boolean requestWildTakeoff() {
        if (this.level().isClientSide() || isAirborneFlight() || takeoffCooldown > 0 || !canFlyNow() || !canTakeoffFromHere()) return false;
        if (this.getVitalEnergy() > this.getVitalEnergyCapacity() - takeoffEnergy()) return false;
        this.setVitalEnergy(this.getVitalEnergy() + takeoffEnergy());
        beginServerTakeoff(null);
        wildYaw = this.getYRot();
        wildSpeed = this.getDeltaMovement().horizontalDistance();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, vehicleTakeoffLift(), motion.z);
        this.hasImpulse = true;
        keepAirborne(Flight.AIR_TASK_GRACE_TICKS * 3);
        return true;
    }

    public void requestWildLanding(@Nullable Vec3 spot) {
        landingRequested = true;
        airTaskTicks = 0;
        landingTarget = spot != null ? spot : findLandingSpot(false);
    }

    private boolean wantsToStayAirborne() {
        if (this.isSedatedFleeing()) return true;
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && target.getY() - this.getY() > 3.0) return true;
        if (this.isTame() && !this.isSitting() && this.isFollowingOwner()) {
            LivingEntity owner = this.getOwner();
            if (owner != null && owner.level() == this.level()) {
                if (isOwnerAirborne(owner)) return true;
                double dx = owner.getX() - this.getX();
                double dz = owner.getZ() - this.getZ();
                return dx * dx + dz * dz > Flight.OWNER_LAND_DISTANCE * Flight.OWNER_LAND_DISTANCE;
            }
        }
        return false;
    }

    private static boolean isOwnerAirborne(LivingEntity owner) {
        if (owner.isFallFlying()) return true;
        if (owner.getRootVehicle() instanceof OWSemiFlyingEntity mount && mount.isAirborneFlight()) return true;
        return owner instanceof Player player && player.getAbilities().flying;
    }

    private void tickWildGrounded() {
        if (takeoffCooldown > 0 || !canFlyNow() || this.isSitting()) return;
        boolean wants = this.isSedatedFleeing();
        LivingEntity target = this.getTarget();
        if (!wants && target != null && target.isAlive() && target.getY() - this.getY() > 4.0) wants = true;
        if (!wants && this.isTame() && this.isFollowingOwner()) {
            LivingEntity owner = this.getOwner();
            if (owner != null && owner.level() == this.level()) {
                double distance = this.distanceTo(owner);
                boolean ownerHigh = owner.getY() - this.getY() > Flight.OWNER_AIRBORNE_HEIGHT && distance > 4.0;
                wants = distance > Flight.OWNER_TAKEOFF_DISTANCE || ownerHigh || (isOwnerAirborne(owner) && distance > 6.0);
            }
        }
        if (wants) requestWildTakeoff();
    }

    private void tickWildAirborne() {
        if (airTaskTicks > 0) airTaskTicks--;
        boolean exhausted = flightEnergyRatio() >= 0.98f;
        boolean task = !exhausted && !this.isSitting() && (airTaskTicks > 0 || wantsToStayAirborne());
        if (!task && !landingRequested) requestWildLanding(null);
        if (task && landingRequested) landingRequested = false;

        steerWild(exhausted);

        if (landingRequested && (this.onGround() || this.isInWater())) {
            setFlightState(FLIGHT_GROUND);
        } else if (!landingRequested && this.onGround() && this.verticalCollisionBelow && this.getDeltaMovement().y <= 0) {
            setFlightState(FLIGHT_GROUND);
        }
    }

    private void steerWild(boolean exhausted) {
        Vec3 target = null;
        double speedModifier = 1.0;
        if (landingRequested) {
            if (landingTarget == null) landingTarget = findLandingSpot(false);
            target = landingTarget;
        } else if (this.moveControl instanceof OWFlyMoveControl fly && fly.hasWanted()) {
            target = new Vec3(fly.getWantedX(), fly.getWantedY(), fly.getWantedZ());
            speedModifier = Mth.clamp(fly.getSpeedModifier(), 0.6, 1.6);
        }

        boolean urgent = this.isSedatedFleeing() || this.getTarget() != null;
        Vec3 motion = this.getDeltaMovement();
        double vx = motion.x;
        double vy = motion.y;
        double vz = motion.z;
        int state;

        if (wildSteerTick != this.tickCount - 1) {
            wildYaw = this.getYRot();
            wildSpeed = motion.horizontalDistance();
        }
        wildSteerTick = this.tickCount;

        if (target == null) {
            wildYaw += wildFlyTurnSpeed() * 0.5f;
            wildSpeed += (wildGlideSpeed() - wildSpeed) * Flight.WILD_ACCEL;
            vy += (-vehicleGlideSink() * 0.6 - vy) * 0.15;
            state = FLIGHT_GLIDE;
        } else {
            double dx = target.x - this.getX();
            double dy = target.y - this.getY();
            double dz = target.z - this.getZ();
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            if (!landingRequested && dx * dx + dy * dy + dz * dz < ARRIVAL_DISTANCE_SQR && this.moveControl instanceof OWFlyMoveControl fly) {
                fly.arrive();
            }

            float desiredYaw = horizontal > 0.3 ? (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f : wildYaw;
            float turn = wildFlyTurnSpeed() * (horizontal < 6.0 ? 2.2f : 1f) * (urgent ? 1.5f : 1f);
            wildYaw = Mth.approachDegrees(wildYaw, desiredYaw, turn);
            double align = Mth.clamp(Mth.cos(Mth.wrapDegrees(desiredYaw - wildYaw) * Mth.DEG_TO_RAD), 0.25, 1.0);

            double cruise = (urgent ? wildFlySpeed() * 1.35 : wildFlySpeed()) * speedModifier;
            if (landingRequested || horizontal < 8.0) cruise = Math.min(cruise, horizontal * LANDING_SLOWDOWN + 0.06);

            double vyTarget = Mth.clamp(dy * 0.12, -0.45, vehicleClimbSpeed());
            boolean climbing = vyTarget > 0.06 && !exhausted;
            boolean glide = exhausted || (!climbing && !urgent && dy <= 1.5);
            if (glide && !exhausted && !landingRequested) vyTarget = Math.min(vyTarget, -vehicleGlideSink() * 0.5);
            if (exhausted) vyTarget = Math.min(vyTarget, -vehicleGlideSink());

            double wanted = glide ? Math.min(cruise, wildGlideSpeed() * speedModifier) : cruise;
            wildSpeed += (wanted * align - wildSpeed) * Flight.WILD_ACCEL;
            vy += (vyTarget - vy) * 0.15;
            state = glide ? FLIGHT_GLIDE : vyTarget > 0.15 ? FLIGHT_CLIMB : urgent && !landingRequested ? FLIGHT_FAST : FLIGHT_FLAP;
        }

        if (this.horizontalCollision) {
            wildSpeed *= 0.6;
            vy = Math.max(vy, 0.12);
        }
        Vec3 dir = Vec3.directionFromRotation(0f, wildYaw);
        vx += (dir.x * wildSpeed - vx) * 0.25;
        vz += (dir.z * wildSpeed - vz) * 0.25;
        this.setDeltaMovement(vx, vy, vz);
        this.hasImpulse = true;
        this.setYRot(wildYaw);
        this.yBodyRot = wildYaw;
        if (this.getTarget() == null) this.yHeadRot = wildYaw;
        setFlightState(state);
    }

    public Vec3 findLandingSpot(boolean preferHigh) {
        int baseX = Mth.floor(this.getX());
        int baseZ = Mth.floor(this.getZ());
        Vec3 best = null;
        int bestY = Integer.MIN_VALUE;
        double bestDistance = Double.MAX_VALUE;
        int samples = preferHigh ? LANDING_SEARCH_SAMPLES : 1;
        for (int i = 0; i < samples; i++) {
            int x = baseX;
            int z = baseZ;
            if (i > 0 || preferHigh) {
                x += this.random.nextInt(LANDING_SEARCH_RADIUS * 2 + 1) - LANDING_SEARCH_RADIUS;
                z += this.random.nextInt(LANDING_SEARCH_RADIUS * 2 + 1) - LANDING_SEARCH_RADIUS;
            }
            int y = groundBelow(x, z);
            BlockPos ground = new BlockPos(x, y - 1, z);
            if (!this.level().getFluidState(ground).isEmpty() && preferHigh) continue;
            double distance = (x - this.getX()) * (x - this.getX()) + (z - this.getZ()) * (z - this.getZ());
            if (best == null || (preferHigh ? y > bestY || (y == bestY && distance < bestDistance) : distance < bestDistance)) {
                best = new Vec3(x + 0.5, y, z + 0.5);
                bestY = y;
                bestDistance = distance;
            }
        }
        if (best == null) {
            int y = groundBelow(baseX, baseZ);
            best = new Vec3(baseX + 0.5, y, baseZ + 0.5);
        }
        return best;
    }

    private int groundBelow(int x, int z) {
        int top = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        int start = Mth.floor(this.getY());
        if (top <= start + 1) return top;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, start, z);
        for (int y = start; y > this.level().getMinBuildHeight(); y--) {
            cursor.setY(y - 1);
            BlockState state = this.level().getBlockState(cursor);
            if (!state.getCollisionShape(this.level(), cursor).isEmpty() || !state.getFluidState().isEmpty()) return y;
        }
        return top;
    }

    private void switchNavigation() {
        PathNavigation wanted = isAirborneFlight() ? airNavigation : groundNavigation;
        if (this.navigation == wanted) return;
        PathNavigation previous = this.navigation;
        BlockPos destination = previous.isDone() ? null : previous.getTargetPos();
        previous.stop();
        this.navigation = wanted;
        if (destination != null) wanted.moveTo(destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5, 1.0);
    }

    // ==================================================
    //                     INCLINAISON
    // ==================================================

    @Override
    protected boolean canLean() {
        return isAirborneFlight() && !this.isSitting();
    }

    @Override
    protected LivingEntity leaningRider() {
        return null;
    }

    @Override
    protected float bankMaxAngle() {
        return Flight.BANK_MAX_ANGLE;
    }

    @Override
    protected float bankReferenceYawRate(boolean ridden) {
        return this.getControllingPassenger() instanceof Player ? 3.2f : 2.0f;
    }

    @Override
    protected float pitchMaxAngle() {
        return Flight.PITCH_MAX_ANGLE;
    }

    @Override
    protected boolean leanPitchWhenFree() {
        return true;
    }

    @Override
    public boolean riderCameraFollowsBodyTilt() {
        return true;
    }

    // ==================================================
    //                 VISUELS DU VOL
    // ==================================================

    private void tickFlightVisuals() {
        int takeoff = this.entityData.get(FLIGHT_TAKEOFF);
        if (takeoff != seenTakeoff) {
            boolean fresh = seenTakeoff != 0 || this.tickCount > 5;
            seenTakeoff = takeoff;
            if (fresh) takeoffAge = 0;
        } else if (takeoffAge >= 0 && ++takeoffAge > 60) {
            takeoffAge = -1;
        }

        int land = this.entityData.get(FLIGHT_LAND);
        if (land != seenLand) {
            boolean fresh = seenLand != 0 || this.tickCount > 5;
            seenLand = land;
            if (fresh) {
                landAge = 0;
                takeoffAge = -1;
            }
        } else if (landAge >= 0 && ++landAge > 60) {
            landAge = -1;
        }

        int state = getFlightState();
        boolean airborne = state != FLIGHT_GROUND;
        airBlendO = airBlend;
        airBlend = approach(airBlend, airborne ? 1f : 0f, airborne ? AIR_BLEND_IN : AIR_BLEND_OUT);
        soarBlendO = soarBlend;
        soarBlend = approach(soarBlend, state == FLIGHT_GLIDE ? 1f : 0f, SOAR_BLEND_RATE);
        climbBlendO = climbBlend;
        climbBlend = approach(climbBlend, state == FLIGHT_CLIMB ? 1f : 0f, CLIMB_BLEND_RATE);
        hoverBlendO = hoverBlend;
        hoverBlend = approach(hoverBlend, state == FLIGHT_HOVER ? 1f : 0f, SOAR_BLEND_RATE);

        flapClockO = flapClock;
        if (airborne && state != FLIGHT_GLIDE) {
            float cycle = flapCycleSeconds(state) / (state == FLIGHT_CLIMB ? 1.35f : 1f);
            flapClock += 0.05f / cycle;
            if ((int) flapClock != (int) flapClockO) playFlapSound(state);
        }
    }

    private void playFlapSound(int state) {
        SoundEvent flap = wingFlapSound();
        if (flap == null) return;
        float volume = state == FLIGHT_CLIMB ? 0.7f : 0.45f;
        float pitch = (state == FLIGHT_FAST ? 1.1f : 0.85f) + this.random.nextFloat() * 0.1f;
        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), flap, this.getSoundSource(), volume, pitch, false);
    }

    private static float approach(float value, float target, float step) {
        return value < target ? Math.min(value + step, target) : Math.max(value - step, target);
    }

    private static float lerpTick(float partialTick, float previous, float current) {
        return previous + (current - previous) * partialTick;
    }

    public float airBlend(float partialTick) { return lerpTick(partialTick, airBlendO, airBlend); }

    public float soarBlend(float partialTick) { return lerpTick(partialTick, soarBlendO, soarBlend); }

    public float climbBlend(float partialTick) { return lerpTick(partialTick, climbBlendO, climbBlend); }

    public float hoverBlend(float partialTick) { return lerpTick(partialTick, hoverBlendO, hoverBlend); }

    public float dashBlend(float partialTick) { return flight.fastBlend(partialTick); }

    public float flapPhase(float partialTick) {
        float clock = lerpTick(partialTick, flapClockO, flapClock);
        return clock - Mth.floor(clock);
    }

    public float takeoffSeconds(float partialTick) { return takeoffAge < 0 ? -1f : (takeoffAge + partialTick) / 20f; }

    public float landingSeconds(float partialTick) { return landAge < 0 ? -1f : (landAge + partialTick) / 20f; }

    // ==================================================
    //                    NAVIGATION
    // ==================================================

    private static class OWFlyingNavigation extends FlyingPathNavigation {
        OWFlyingNavigation(OWSemiFlyingEntity mob, Level level) {
            super(mob, level);
            this.setCanOpenDoors(false);
            this.setCanFloat(true);
            this.setCanPassDoors(true);
        }
    }

    public static class OWFlyMoveControl extends OWMoveControl {

        private final OWSemiFlyingEntity flyer;

        public OWFlyMoveControl(OWSemiFlyingEntity mob) {
            super(mob);
            this.flyer = mob;
        }

        @Override
        public void tick() {
            if (!flyer.isAirborneFlight()) {
                super.tick();
                return;
            }
            this.mob.setZza(0f);
            this.mob.setYya(0f);
            this.mob.setXxa(0f);
        }

        void arrive() {
            this.operation = Operation.WAIT;
        }
    }
}
