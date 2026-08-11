package net.tiew.operationWild.entity.behavior;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.effect.OWEffects;
import net.tiew.operationWild.entity.OWEntity;

public class OWFearHandler {

    public static final int BUCK_TICKS = 14;
    public static final float BUCK_PITCH_DEGREES = 26f;
    public static final float BUCK_ROLL_DEGREES = 14f;

    private static final int BUCK_INTERVAL_MIN = 26;
    private static final int BUCK_INTERVAL_MAX = 46;
    private static final int BUCK_INTERVAL_FLOOR = 16;
    private static final int FIRST_BUCK_DELAY = 10;

    private static final int RAMP_TICKS = 12;
    private static final int FADE_TICKS = 16;

    private static final float PANIC_ENERGY_PER_TICK = 0.28f;

    private static final int FLEE_INTERVAL_MIN = 10;
    private static final int FLEE_INTERVAL_SPREAD = 14;
    private static final int FLEE_RANGE = 24;
    private static final int FLEE_VERTICAL = 14;

    private final OWEntity entity;

    private boolean panicking;
    private int elapsed;
    private int buckCooldown;
    private int fleeCooldown;

    public OWFearHandler(OWEntity entity) {
        this.entity = entity;
    }

    public boolean isPanicking() {
        return this.panicking;
    }

    public static float buckCurve(OWEntity entity, float partialTick) {
        int remaining = entity.getBuckTicks();
        if (remaining <= 0) return 0f;
        float progress = Mth.clamp((BUCK_TICKS - remaining + partialTick) / BUCK_TICKS, 0f, 1f);
        return Mth.sin(progress * Mth.PI);
    }

    public void tick() {
        boolean feared = this.entity.hasEffect(OWEffects.FEAR_EFFECT.getDelegate());

        if (feared && !this.panicking) {
            begin();
        } else if (!feared && this.panicking) {
            end();
            return;
        }

        if (this.panicking) panicTick();
    }

    private void begin() {
        this.panicking = true;
        this.elapsed = 0;
        this.buckCooldown = FIRST_BUCK_DELAY;
        this.fleeCooldown = 0;

        this.entity.setPanicBuck(0);
        this.entity.setPanicLevel(0.05f);

        if (this.entity.isCombo()) {
            this.entity.resetCombo(0);
            this.entity.actualAttackNumber = 0;
        }
        this.entity.setAttacking(false);

        this.entity.playPanicVoice(0.65f);
    }

    private void end() {
        this.panicking = false;
        this.entity.setPanicLevel(0f);
        this.entity.setPanicBuck(0);
    }

    private void panicTick() {
        this.elapsed++;

        MobEffectInstance instance = this.entity.getEffect(OWEffects.FEAR_EFFECT.getDelegate());
        int amplifier = instance == null ? 0 : instance.getAmplifier();
        int remaining = instance == null ? 0 : instance.getDuration();
        boolean endless = instance != null && instance.isInfiniteDuration();

        this.entity.setTarget(null);
        this.entity.setLastHurtByMob(null);
        this.entity.setAggressive(false);

        int buckTicks = this.entity.getBuckTicks();
        float ramp = Math.min(1f, this.elapsed / (float) RAMP_TICKS);
        float fade = endless ? 1f : Math.min(1f, remaining / (float) FADE_TICKS);
        float buck = buckTicks / (float) BUCK_TICKS;
        this.entity.setPanicLevel(Mth.clamp(Math.min(ramp, fade) + buck * 0.25f, 0f, 1f));

        flee(amplifier);
        drainEnergy();
        spawnPanicParticles();

        if (buckTicks > 0) {
            this.entity.setPanicBuck(this.entity.getPanicBuck() - this.entity.getBuckSide());
        } else if (--this.buckCooldown <= 0) {
            startBuck(amplifier);
        }
    }

    private void flee(int amplifier) {
        if (--this.fleeCooldown > 0 && this.entity.getNavigation().isInProgress()) return;

        Vec3 destination = DefaultRandomPos.getPos(this.entity, FLEE_RANGE, FLEE_VERTICAL);
        if (destination != null) {
            float speed = this.entity.getSpeed() * 20f * (1.15f + 0.15f * amplifier);
            this.entity.getNavigation().moveTo(destination.x, destination.y, destination.z, speed);
        }
        this.fleeCooldown = FLEE_INTERVAL_MIN + this.entity.getRandom().nextInt(FLEE_INTERVAL_SPREAD);
    }

    private void drainEnergy() {
        float capacity = this.entity.getVitalEnergyCapacity();
        if (capacity <= 0f) return;
        this.entity.setVitalEnergy(Math.min(capacity, this.entity.getVitalEnergy() + PANIC_ENERGY_PER_TICK));
    }

    private void spawnPanicParticles() {
        if (!(this.entity.level() instanceof ServerLevel level)) return;
        if (this.elapsed % 3 != 0) return;

        float width = this.entity.getBbWidth();
        float height = this.entity.getBbHeight();
        level.sendParticles(ParticleTypes.SPLASH,
                this.entity.getX(), this.entity.getY() + height * 0.75, this.entity.getZ(),
                2, width * 0.45, height * 0.2, width * 0.45, 0.01);

        if (this.elapsed % 9 == 0) {
            Vec3 muzzle = this.entity.getEyePosition().add(this.entity.getLookAngle().scale(width * 0.6));
            level.sendParticles(ParticleTypes.CLOUD, muzzle.x, muzzle.y, muzzle.z,
                    3, 0.08, 0.08, 0.08, 0.015);
        }
    }

    private void startBuck(int amplifier) {
        int side = this.entity.getRandom().nextBoolean() ? 1 : -1;
        this.entity.setPanicBuck(BUCK_TICKS * side);
        this.buckCooldown = Math.max(BUCK_INTERVAL_FLOOR,
                BUCK_INTERVAL_MIN + this.entity.getRandom().nextInt(BUCK_INTERVAL_MAX - BUCK_INTERVAL_MIN) - amplifier * 6);

        float yaw = this.entity.getYRot() * Mth.DEG_TO_RAD;
        double lateralX = Mth.cos(yaw) * side * 0.32;
        double lateralZ = Mth.sin(yaw) * side * 0.32;
        double lift = this.entity.onGround() ? 0.46 : 0.12;

        this.entity.setDeltaMovement(this.entity.getDeltaMovement().add(lateralX, lift, lateralZ));
        this.entity.hasImpulse = true;

        this.entity.playPanicVoice(0.8f);
    }
}
