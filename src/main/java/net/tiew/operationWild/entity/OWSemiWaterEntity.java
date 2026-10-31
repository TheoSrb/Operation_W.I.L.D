package net.tiew.operationWild.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.tags.FluidTags;
import net.tiew.operationWild.entity.navigation.OWGroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.core.OWDamageSources;
import net.tiew.operationWild.entity.animals.aquatic.CrocodileEntity;
import net.tiew.operationWild.event.ClientEvents;

public abstract class OWSemiWaterEntity extends OWEntity implements net.tiew.operationWild.entity.config.IOWDiver {

    /** Amphibie : à l'aise sur les deux terrains de duel. */
    @Override
    public int arenaTerrainMask() {
        return net.tiew.operationWild.core.OWArena.TERRAIN_BOTH;
    }

    private static final EntityDataAccessor<Float> TARGET_PITCH = SynchedEntityData.defineId(OWSemiWaterEntity.class, EntityDataSerializers.FLOAT);

    private float swimYaw = 0;
    private float targetYaw = 0;
    private float yawChangeTimer = 0;
    private float verticalWave = 0;
    private float targetDepth = 0;
    private float depthChangeTimer = 0;

    public float damageTimer = 0;
    public boolean firstTimeToDeep = true;

    private final float YAW_CHANGE_INTERVAL;
    private final float YAW_SMOOTH_SPEED;
    private final float HORIZONTAL_SPEED;
    private final float VERTICAL_WAVE_SPEED = 0.03f;
    private final float VERTICAL_WAVE_AMPLITUDE = 0.008f;
    private final float DEPTH_CHANGE_INTERVAL;
    private final float DEPTH_CHANGE_SPEED;
    private final float SURFACE_RISE_SPEED;

    private final float TARGET_YAW_SPEED = 0.03f;
    private final float PITCH_SMOOTH_SPEED = 0.08f;
    private static final float WADE_DEPTH_FRACTION = 0.6f;
    private static final float WADE_FRICTION = 0.6f;
    private static final double WADE_SPEED_FACTOR = 0.85;
    private static final double WANTED_ARRIVAL_SQR = 1.0;
    private static final float BANK_STEP = 2.0f;
    private static final double BANK_PROBE_MARGIN = 0.6;
    private static final int BANK_PROBE_HEIGHT = 3;
    private static final double BANK_LIFT_BASE = 0.35;
    private static final double BANK_LIFT_PER_BLOCK = 0.15;
    private static final double BANK_LIFT_MAX = 0.75;
    private static final double OBSTACLE_LIFT = 0.3;
    private static final double BANK_MIN_RISE = 1.0;
    private static final int BANK_CLIMB_TICKS = 12;
    private static final double BANK_PUSH = 0.08;
    private static final double BANK_PUSH_MAX = 0.25;
    private int bankClimbTicks;
    private static final float SWIM_PITCH_SMOOTHING = 0.15f;
    private static final float FLEE_YAW_SPEED = 0.25f;
    private static final int SURFACE_AIR_RESERVE = 200;
    private float swimPitch;
    private float swimPitchPrev;
    private static final int WATER_VISUAL_SETTLE = 6;
    private static final float WATER_BLEND_SPRING = 0.05f;
    private static final float WATER_BLEND_DAMPING = 0.44f;
    private int waterVisualTicks;
    private boolean waterVisual;
    private float waterBlend;
    private float waterBlendO;
    private float waterBlendVelocity;
    private final float TARGET_TRANSITION_SPEED = 0.1f;
    private float targetModeBlend = 0.0f;

    private OWGroundPathNavigation groundNavigation;
    private OWWaterNavigation waterNavigation;

    public OWSemiWaterEntity(EntityType<? extends TamableAnimal> entityType, Level level, float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);

        this.groundNavigation = new OWGroundPathNavigation(this, level);
        this.waterNavigation = new OWWaterNavigation(this, level);

        this.YAW_SMOOTH_SPEED = 0.015f * getSwimSpeed();
        this.HORIZONTAL_SPEED = 0.02f * getSwimSpeed();
        this.DEPTH_CHANGE_SPEED = 0.02f * getSwimSpeed();
        this.SURFACE_RISE_SPEED = 0.02f * getSwimSpeed();

        this.swimYaw = this.random.nextFloat() * 360f;
        this.targetYaw = this.random.nextFloat() * 360f;
        this.yawChangeTimer = this.random.nextFloat() * 200f;
        this.verticalWave = this.random.nextFloat() * (float)(Math.PI * 2);
        this.depthChangeTimer = this.random.nextFloat() * 300f;

        this.YAW_CHANGE_INTERVAL = 150f + this.random.nextFloat() * 100f;
        this.DEPTH_CHANGE_INTERVAL = 250f + this.random.nextFloat() * 100f;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TARGET_PITCH, 0.0f);
    }

    public float getTargetPitch() {
        return this.entityData.get(TARGET_PITCH);
    }

    public void setTargetPitch(float pitch) {
        this.entityData.set(TARGET_PITCH, pitch);
    }

    /**
     * Amphibie : le roulis de virage ne vaut que dans l'eau. Sur la terre ferme, une bête qui marche
     * ne se couche pas dans ses virages.
     */
    @Override
    protected boolean canLean() {
        return this.isVisuallyInWater() && !this.isSitting() && !this.isSleeping();
    }

    @Override
    protected float bankMaxAngle() {
        return 45.0f;
    }

    /**
     * Seuil de saturation abaissé, et c'est lui qui compte le plus ici.
     *
     * <p>Un semi-aquatique tourne bien plus lentement qu'une nageuse : en vadrouille, le lacet se
     * rapproche de sa cible à {@code 0,015 × vitesse de nage} par tick, soit une fraction de degré.
     * Contre les 5,5°/tick réglés pour l'orque, la courbe restait écrasée dans son bas et le roulis
     * ne se voyait pratiquement jamais hors virage serré. À 2,5°/tick, un virage ordinaire donne déjà
     * une inclinaison franche.</p>
     *
     * <p>Descendre plus bas ferait saturer la courbe en permanence et rendrait toutes les
     * inclinaisons identiques : on perdrait la lecture entre une correction de cap et un vrai
     * virage.</p>
     */
    @Override
    protected float bankReferenceYawRate(boolean ridden) {
        // Monté, le crocodile rattrape le regard à un dixième de l'écart par tick, soit jusqu'à
        // 18°/tick sur un demi-tour. Contre les 2,5 réglés pour la vadrouille, le moindre coup de
        // souris saturait la courbe et collait le roulis maximal d'un bloc. À 9, une petite correction
        // de cap ne donne que quelques degrés, et il faut un vrai virage pour coucher la bête.
        return ridden ? 9.0f : 2.5f;
    }

    /**
     * Nage libre laissée à {@code TARGET_PITCH}, qui la pilote déjà bien mieux : il suit la proie en
     * 3D, la profondeur visée et la remontée pour respirer. Le tangage MONTÉ, lui, passe désormais
     * par le canal commun — c'est ce qui donne à l'orque son assiette si lisible quand le cavalier
     * lève ou baisse les yeux, et il n'y a pas de raison d'en priver le crocodile.
     */
    @Override
    protected boolean leanPitchWhenFree() {
        return false;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
    }

    @Override
    public abstract int getMaxDepth();
    public abstract float getSwimSpeed();

    /**
     * Nage propre à l'amphibie, désormais empruntée <b>aussi par le client qui pilote</b>.
     *
     * <p>{@code isEffectiveAi()} vaut faux sur le client : monté, l'amphibie retombait donc dans
     * {@code super.travel}, c'est-à-dire la nage vanilla — avec sa gravité aquatique. Et comme
     * c'est le client du cavalier qui fait autorité sur la position d'une monture, c'est cette
     * simulation-là qui gagnait : la bête s'enfonçait sous son pilote alors que le serveur, lui,
     * la tenait à flot. L'orque n'avait pas le défaut parce que {@code OWWaterEntity} teste
     * justement {@code isControlledByLocalInstance()} en plus — on s'aligne dessus.</p>
     */
    @Override
    public void travel(Vec3 vec3) {
        if (this.isWading() && this.getControllingPassenger() == null && this.isEffectiveAi()) {
            wade(vec3);
            return;
        }
        if (this.isInWater() && (this.isEffectiveAi() || this.isControlledByLocalInstance())) {
            this.moveRelative(0.1F, vec3);
            if (!(this.getControllingPassenger() instanceof Player)) capWildSwimSpeed();
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.75D));

            // Le cycle de nage se lit sur walkAnimation, que seul calculateEntityAnimation fait
            // avancer — et il vit au bout de LivingEntity#travel, qu'on ne traverse plus ici. Sans
            // cet appel, la bête glisserait dans l'eau parfaitement immobile sous son cavalier.
            this.calculateEntityAnimation(false);

            int currentAir = this.getAirSupply();
            int maxAir = this.getMaxAirSupply();
            double airPercentage = (double) currentAir / maxAir * 100.0;
            int depth = (int) (this.level().getSeaLevel() - this.getY());

            if (airPercentage < 10.0 || depth >= getMaxDepth()) {
                if (!this.isAtSurface()) {
                    this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.015D, 0.0D));
                }
            }

            if (this.horizontalCollision && this.getDeltaMovement().y < 0.1) {
                Vec3 motion = this.getDeltaMovement();
                double rise = bankRise();
                boolean bank = rise > BANK_MIN_RISE;
                double lift = bank ? Math.min(BANK_LIFT_MAX, BANK_LIFT_BASE + BANK_LIFT_PER_BLOCK * rise) : 0.0;
                this.setDeltaMovement(motion.x, Math.max(motion.y + OBSTACLE_LIFT, lift), motion.z);
                if (bank && this.getControllingPassenger() == null) bankClimbTicks = BANK_CLIMB_TICKS;
            }
        } else {
            super.travel(vec3);
        }
    }

    /**
     * Inertie de nage : la bete met du temps a lancer, et bien plus a s'arreter.
     * Hors de l'eau, elle retrouve la reponse vive d'une monture terrestre.
     */
    @Override
    protected float riddenSpeedResponse(boolean accelerating) {
        if (!this.isInWater()) return super.riddenSpeedResponse(accelerating);
        return accelerating ? RIDDEN_SWIM_ACCEL_RESPONSE : RIDDEN_SWIM_BRAKE_RESPONSE;
    }

    @Override
    public float maxUpStep() {
        float step = super.maxUpStep();
        boolean footing = this.onGround() || this.isWading();
        return this.isInWater() && footing && this.getControllingPassenger() == null ? Math.max(step, BANK_STEP) : step;
    }

    private void riseWhileSwimming() {
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, Math.max(motion.y, 0.0) + SURFACE_RISE_SPEED, motion.z);
    }

    private void tickWaterVisual() {
        this.waterVisualTicks = Mth.clamp(this.waterVisualTicks + (this.isInWater() ? 1 : -1), 0, WATER_VISUAL_SETTLE);
        if (this.waterVisualTicks >= WATER_VISUAL_SETTLE) this.waterVisual = true;
        else if (this.waterVisualTicks == 0) this.waterVisual = false;
        this.waterBlendO = this.waterBlend;
        this.waterBlendVelocity += ((this.waterVisual ? 1f : 0f) - this.waterBlend) * WATER_BLEND_SPRING
                - this.waterBlendVelocity * WATER_BLEND_DAMPING;
        this.waterBlend += this.waterBlendVelocity;
        if (this.waterBlend <= 0f || this.waterBlend >= 1f) {
            this.waterBlend = Mth.clamp(this.waterBlend, 0f, 1f);
            this.waterBlendVelocity = 0f;
        }
    }

    public boolean isVisuallyInWater() {
        return this.level().isClientSide() ? this.waterVisual : this.isInWater();
    }

    public float waterBlend(float partialTick) {
        float t = Mth.lerp(partialTick, this.waterBlendO, this.waterBlend);
        return t * t * (3f - 2f * t);
    }

    protected float swimPitchLimit() {
        return 40f;
    }

    public float getSwimPitch(float partialTick) {
        return Mth.lerp(partialTick, this.swimPitchPrev, this.swimPitch);
    }

    private void tickSwimPitch() {
        this.swimPitchPrev = this.swimPitch;
        if (this.isTame() && this.isVehicle() && !this.isSitting() && this.isInWater()) {
            this.swimPitch = this.getRidePitch(1f);
            return;
        }
        float limit = this.swimPitchLimit();
        float target = this.isTame() ? 0f : Mth.clamp(this.getTargetPitch(), -limit, limit);
        this.swimPitch += (target - this.swimPitch) * SWIM_PITCH_SMOOTHING;
        if (Math.abs(this.swimPitch) < 0.01f && target == 0f) this.swimPitch = 0f;
    }

    private void tickBankClimb() {
        if (bankClimbTicks <= 0) return;
        bankClimbTicks--;
        if (this.onGround() && !this.isInWater()) {
            bankClimbTicks = 0;
            return;
        }
        if (this.isInWater()) return;
        double yaw = Math.toRadians(this.getYRot());
        Vec3 motion = this.getDeltaMovement();
        double x = motion.x - Math.sin(yaw) * BANK_PUSH;
        double z = motion.z + Math.cos(yaw) * BANK_PUSH;
        double horizontal = Math.sqrt(x * x + z * z);
        if (horizontal > BANK_PUSH_MAX) {
            x *= BANK_PUSH_MAX / horizontal;
            z *= BANK_PUSH_MAX / horizontal;
        }
        this.setDeltaMovement(x, motion.y, z);
    }

    private double bankRise() {
        double yaw = Math.toRadians(this.getYRot());
        double reach = this.getBbWidth() * 0.5 + BANK_PROBE_MARGIN;
        int x = Mth.floor(this.getX() - Math.sin(yaw) * reach);
        int z = Mth.floor(this.getZ() + Math.cos(yaw) * reach);
        int base = Mth.floor(this.getY());
        int clearance = Mth.ceil(this.getBbHeight());
        for (int k = 0; k <= BANK_PROBE_HEIGHT; k++) {
            if (!isOpenColumn(x, base + k, z, clearance)) continue;
            return k == 0 ? -1.0 : base + k - this.getY();
        }
        return -1.0;
    }

    private boolean isOpenColumn(int x, int y, int z, int height) {
        for (int i = 0; i < height; i++) {
            BlockPos pos = new BlockPos(x, y + i, z);
            if (!this.level().getBlockState(pos).getCollisionShape(this.level(), pos).isEmpty()) return false;
        }
        return true;
    }

    public boolean isWading() {
        if (!this.isInWater()) return false;
        if (this.getFluidHeight(FluidTags.WATER) >= this.getBbHeight() * WADE_DEPTH_FRACTION) return false;
        return this.onGround() || !this.level().getBlockState(this.blockPosition().below()).getCollisionShape(this.level(), this.blockPosition().below()).isEmpty();
    }

    private void wade(Vec3 vec3) {
        Vec3 moved = this.handleRelativeFrictionAndCalculateMovement(vec3.scale(WADE_SPEED_FACTOR), WADE_FRICTION);
        double drag = this.onGround() ? WADE_FRICTION * 0.91 : 0.91;
        this.setDeltaMovement(moved.x * drag, (moved.y - this.getGravity()) * 0.98, moved.z * drag);
        this.calculateEntityAnimation(false);
    }

    public float wildSwimSpeedCap() {
        return 0.15f;
    }

    private void capWildSwimSpeed() {
        Vec3 motion = this.getDeltaMovement();
        double horizontal = motion.horizontalDistance();
        double cap = wildSwimSpeedCap() * this.sedationResponse.chaseSpeedFactor() * this.tamingSpeedFactor() * this.fleeSpeedFactor();
        if (horizontal <= cap) return;
        double scale = cap / horizontal;
        this.setDeltaMovement(motion.x * scale, motion.y, motion.z * scale);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public float getWaterSlowDown() {
        return 0.98F;
    }

    @Override
    public void tick() {
        super.tick();
        // La nage lisse est déjà écartée quand une prise est en cours : elle exige
        // !isVehicle(), et la proie compte comme passager. Le retour anticipé qui existait ici
        // faisait en plus sauter les dégâts de pression — un crocodile qui noyait sa victime au
        // fond ne les subissait jamais.

        if (this.level().isClientSide()) {
            tickWaterVisual();
            tickSwimPitch();
        }

        LivingEntity rider = this.getControllingPassenger();
        int depth = (int) (this.level().getSeaLevel() - this.getY());

        if (this.isEffectiveAi() && !this.isVehicle() && !this.isSitting()) {
            switchNavigation();
            tickBankClimb();

            if (this.isInWater() && !this.isWading() && !this.breakout.wantsControl()) {
                handleSmoothSwimming();
            } else if (!this.level().isClientSide()) {
                float p = this.getTargetPitch();
                if (Math.abs(p) > 0.1f) {
                    this.setTargetPitch(p * 0.82f);
                } else if (p != 0f) {
                    this.setTargetPitch(0f);
                }
            }
        }

        if (depth >= this.getMaxDepth()) {
            if (rider != null) {
                if (rider instanceof Player player) {
                    if (!player.isCreative()) {
                        applyWaterPressureDamage(depth, player);
                    }
                }
            } else applyWaterPressureDamage(depth, null);
        } else {
            damageTimer = 0.0f;
            firstTimeToDeep = true;
        }
    }

    protected void handleSmoothSwimming() {
        LivingEntity target = this.getTarget();
        boolean hasTarget = target != null;
        Vec3 pathPoint = hasTarget ? null : currentPathPoint();
        if (!hasTarget && pathPoint == null) pathPoint = wantedPoint();

        if (hasTarget) {
            targetModeBlend = Math.min(1.0f, targetModeBlend + TARGET_TRANSITION_SPEED);
        } else {
            targetModeBlend = Math.max(0.0f, targetModeBlend - TARGET_TRANSITION_SPEED);
        }

        if (hasTarget) {
            double deltaX = target.getX() - this.getX();
            double deltaZ = target.getZ() - this.getZ();
            targetYaw = (float)(Math.toDegrees(Math.atan2(deltaZ, deltaX))) - 90f;
        } else if (pathPoint != null) {
            double deltaX = pathPoint.x - this.getX();
            double deltaZ = pathPoint.z - this.getZ();
            if (deltaX * deltaX + deltaZ * deltaZ > 1.0E-4) {
                targetYaw = (float)(Math.toDegrees(Math.atan2(deltaZ, deltaX))) - 90f;
            }
            yawChangeTimer = 0;
        } else {
            yawChangeTimer++;
            if (yawChangeTimer >= YAW_CHANGE_INTERVAL) {
                targetYaw = this.getRandom().nextFloat() * 360f;
                yawChangeTimer = 0;
            }

            if (this.horizontalCollision) {
                targetYaw = swimYaw + 90f + (this.getRandom().nextFloat() * 180f - 90f);
                yawChangeTimer = 0;
            }
        }

        while (targetYaw > 360f) targetYaw -= 360f;
        while (targetYaw < 0f) targetYaw += 360f;

        float yawDiff = targetYaw - swimYaw;
        while (yawDiff > 180f) yawDiff -= 360f;
        while (yawDiff < -180f) yawDiff += 360f;

        float yawSpeed = hasTarget ? 0.18f : (pathPoint != null ? (this.isFleeingDanger() ? FLEE_YAW_SPEED : 0.14f) : YAW_SMOOTH_SPEED);
        swimYaw += yawDiff * yawSpeed;

        while (swimYaw > 360f) swimYaw -= 360f;
        while (swimYaw < 0f) swimYaw += 360f;

        this.setYRot(swimYaw);
        this.setYHeadRot(swimYaw);
        this.setYBodyRot(swimYaw);

        if (hasTarget || pathPoint != null) {
            double aimX = hasTarget ? target.getX() : pathPoint.x;
            double aimY = hasTarget ? target.getY() : pathPoint.y;
            double aimZ = hasTarget ? target.getZ() : pathPoint.z;
            double deltaY = aimY - this.getY();
            double deltaX = aimX - this.getX();
            double deltaZ = aimZ - this.getZ();
            double distance3D = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

            if (distance3D > 0.1) {
                float desiredPitch = (float)Math.toDegrees(Math.asin(-deltaY / distance3D));
                float currentPitch = this.getTargetPitch();
                float pitchDiff = desiredPitch - currentPitch;
                float newPitch = currentPitch + pitchDiff * PITCH_SMOOTH_SPEED;
                newPitch = Math.max(-90f, Math.min(90f, newPitch));
                this.setTargetPitch(newPitch);
            }
        }

        if (!hasTarget && pathPoint == null) {
            depthChangeTimer++;
            if (depthChangeTimer >= DEPTH_CHANGE_INTERVAL) {
                int maxDepthLimit = Math.max((int)(this.level().getSeaLevel() - getMaxDepth()), -64);
                int surfaceLevel = (int)this.level().getSeaLevel();
                float safeMinY = Math.max(maxDepthLimit, (float)this.getY() - 5f);
                float safeMaxY = Math.min(surfaceLevel, (float)this.getY() + 5f);
                targetDepth = safeMinY + this.getRandom().nextFloat() * (safeMaxY - safeMinY);
                depthChangeTimer = 0;
            }
        }

        int currentAir = this.getAirSupply();
        int maxAir = this.getMaxAirSupply();
        double airPercentage = (double) currentAir / maxAir * 100.0;
        int depth = (int) (this.level().getSeaLevel() - this.getY());

        boolean surfacing = (airPercentage < 15.0 || currentAir < SURFACE_AIR_RESERVE || depth >= getMaxDepth()) && !this.isAtSurface();
        if (surfacing && !hasTarget && pathPoint == null) {
            double yawRadians = Math.toRadians(swimYaw);
            double moveX = -Math.sin(yawRadians) * HORIZONTAL_SPEED * 0.5;
            double moveZ = Math.cos(yawRadians) * HORIZONTAL_SPEED * 0.5;
            this.setDeltaMovement(this.getDeltaMovement().add(moveX, SURFACE_RISE_SPEED, moveZ));
            return;
        }

        if (hasTarget) {
            handleTargetSwimming(target);
            if (surfacing) riseWhileSwimming();
            return;
        }

        if (pathPoint != null) {
            handlePathSwimming(pathPoint);
            if (surfacing) riseWhileSwimming();
            return;
        }

        handleFreeSwimming();
    }

    /**
     * Prochain point du chemin en cours, ou {@code null} si la bête n'en suit aucun.
     *
     * <p>C'est le <b>nœud suivant</b> qu'on vise, et non la destination finale : c'est lui qui porte
     * le contournement des obstacles calculé par la navigation. La nage lisse impose son propre cap
     * ({@code setYRot(swimYaw)}) et écrase donc la direction du {@code MoveControl} ; sans ce point de
     * mire, un amphibie en train de rejoindre son maître nageait au hasard de son cap de vadrouille,
     * en corrigeant seulement sa profondeur.</p>
     */
    @javax.annotation.Nullable
    protected Vec3 currentPathPoint() {
        net.minecraft.world.entity.ai.navigation.PathNavigation nav = this.getNavigation();
        net.minecraft.world.level.pathfinder.Path path = nav.getPath();
        if (path == null || path.isDone()) return null;
        return path.getNextEntityPos(this);
    }

    @javax.annotation.Nullable
    private Vec3 wantedPoint() {
        if (!this.getMoveControl().hasWanted()) return null;
        Vec3 wanted = new Vec3(this.getMoveControl().getWantedX(), this.getMoveControl().getWantedY(), this.getMoveControl().getWantedZ());
        return wanted.distanceToSqr(this.position()) <= WANTED_ARRIVAL_SQR ? null : wanted;
    }

    protected void handleFreeSwimming() {
        // Initialisation paresseuse : targetDepth=0 signifie Y=0 (fond de map), pas encore initialisé
        if (targetDepth == 0) {
            targetDepth = (float) this.getY();
        }

        double yawRadians = Math.toRadians(swimYaw);
        double moveX = -Math.sin(yawRadians) * HORIZONTAL_SPEED;
        double moveZ = Math.cos(yawRadians) * HORIZONTAL_SPEED;

        verticalWave += VERTICAL_WAVE_SPEED;
        double verticalMove = Math.sin(verticalWave) * VERTICAL_WAVE_AMPLITUDE;

        double currentY = this.getY();
        double depth = this.level().getSeaLevel() - this.getY();
        double depthDiff = targetDepth - currentY;

        if (Math.abs(depthDiff) > 1.0) {
            if (depthDiff > 0) {
                verticalMove += DEPTH_CHANGE_SPEED;
            } else {
                verticalMove -= DEPTH_CHANGE_SPEED;
            }
        } else if (Math.abs(depthDiff) > 0.3) {
            double smoothFactor = Math.abs(depthDiff);
            if (depthDiff > 0) {
                verticalMove += DEPTH_CHANGE_SPEED * smoothFactor;
            } else {
                verticalMove -= DEPTH_CHANGE_SPEED * smoothFactor;
            }
        }

        // Pitch visuel uniquement quand le mouvement vertical est effectivement actif (depth > 1)
        float desiredPitch;
        if (depth > 1 && Math.abs(depthDiff) > 0.5) {
            double pitchMagnitude = Math.min(Math.abs(depthDiff) / 6.0, 1.0) * 38.0;
            desiredPitch = (float)(depthDiff > 0 ? -pitchMagnitude : pitchMagnitude);
        } else {
            desiredPitch = 0f;
        }
        float newPitch = this.getTargetPitch() + (desiredPitch - this.getTargetPitch()) * PITCH_SMOOTH_SPEED;
        this.setTargetPitch(newPitch);

        this.setDeltaMovement(this.getDeltaMovement().add(moveX, depth > 1 ? verticalMove + 0.001 : 0, moveZ));
    }

    protected void handleTargetSwimming(LivingEntity target) {
        double deltaX = target.getX() - this.getX();
        double deltaY = target.getY() - this.getY();
        double deltaZ = target.getZ() - this.getZ();
        double horizDist = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double dist3D   = Math.sqrt(horizDist * horizDist + deltaY * deltaY);

        // Burst à courte portée pour le lunge final
        float speedMult = (float)(horizDist < 4.0 ? 3.5 : 2.5);
        double yawRadians = Math.toRadians(swimYaw);
        double moveX = -Math.sin(yawRadians) * HORIZONTAL_SPEED * speedMult;
        double moveZ =  Math.cos(yawRadians) * HORIZONTAL_SPEED * speedMult;

        // Poursuite verticale proportionnelle — réaction immédiate, pas de blend
        double depth = this.level().getSeaLevel() - this.getY();
        double verticalMove = 0;
        if (dist3D > 0.5) {
            verticalMove = (deltaY / dist3D) * HORIZONTAL_SPEED * speedMult;
        }

        // Plonger vers une cible sous l'eau est toujours autorisé (peu importe la profondeur actuelle)
        // Remonter garde la restriction depth > 1 pour éviter de bondir hors de l'eau sans target aérienne
        double yMove = verticalMove < 0 ? verticalMove : (depth > 1 ? verticalMove + 0.001 : 0);
        this.setDeltaMovement(this.getDeltaMovement().add(moveX, yMove, moveZ));
    }

    public void applyWaterPressureDamage(int depth, Player player) {
        if (!this.isTame() || !this.isInWater()) return;
        int waterPressure = (int) ClientEvents.getWaterPressure(depth);
        float damageInterval = (Math.max((-1.25f * waterPressure + 65) / 30.0f, 0.1f));
        float normalizedPressure = waterPressure / 4.0f;
        float intensity = 0.05f * (float) Math.pow(normalizedPressure, 2f);

        ClientEvents.shakeCamera(intensity, player);

        if (this.level().isClientSide) {
            return;
        }

        damageTimer += 0.05f;

        if (damageTimer >= damageInterval) {
            this.invulnerableTime = 0;

            DamageSource waterPressureDamage = OWDamageSources.createWaterPressureDamage((ServerLevel) this.level());
            this.hurt(waterPressureDamage, 4);

            this.invulnerableTime = 0;
            damageTimer = 0.0f;
        }

        // L'alerte est desormais dessinee par le HUD, cote client (cf. OWEntityHud) : construire
        // ici un message qu'on ne peut pas afficher depuis le serveur ne servait a rien.
        firstTimeToDeep = false;
    }

    protected void handlePathSwimming(Vec3 pathPoint) {
        if (pathPoint == null) return;

        double yDiff = pathPoint.y - this.getY();
        double yawRadians = Math.toRadians(swimYaw);
        double depth = this.level().getSeaLevel() - this.getY();
        double moveX = -Math.sin(yawRadians) * HORIZONTAL_SPEED * 1.5;
        double moveZ = Math.cos(yawRadians) * HORIZONTAL_SPEED * 1.5;

        double verticalMove = 0;
        if (yDiff > 0.5D) {
            verticalMove = 0.03D;
        } else if (yDiff < -0.5D) {
            verticalMove = -0.03D;
        }

        // Remonter reste conditionné à la profondeur (pas de bond hors de l'eau), mais descendre
        // vers un nœud plus bas doit toujours passer : c'est ce qui permet de plonger vers un maître
        // qui nage sous la surface.
        double yMove = verticalMove < 0 ? verticalMove : (depth > 1 ? verticalMove + 0.001 : 0);
        this.setDeltaMovement(this.getDeltaMovement().add(moveX, yMove, moveZ));
    }

    protected boolean isAtSurface() {
        BlockPos posAbove = this.blockPosition().above();
        return !this.level().getFluidState(posAbove).isEmpty() == false || this.level().isEmptyBlock(posAbove);
    }

    protected void switchNavigation() {
        PathNavigation wanted = this.isInWater() && !this.isWading() ? this.waterNavigation : this.groundNavigation;
        if (this.navigation == wanted) return;
        PathNavigation previous = this.navigation;
        BlockPos destination = previous.isDone() ? null : previous.getTargetPos();
        double speed = previous instanceof OWGroundPathNavigation ground ? ground.speed()
                : previous instanceof OWWaterNavigation water ? water.speed() : 1.0;
        previous.stop();
        this.navigation = wanted;
        if (destination != null) wanted.moveTo(destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5, speed);
    }

    private static class OWWaterNavigation extends WaterBoundPathNavigation {
        OWWaterNavigation(OWSemiWaterEntity mob, Level level) {
            super(mob, level);
        }

        double speed() {
            return this.speedModifier;
        }
    }

    @Override
    protected double riddenAscendSpeed() {
        return 0.0D;
    }
}