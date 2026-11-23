package net.tiew.operationWild.entity.misc;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.syncher.SynchedEntityData;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.animals.terrestrial.PeacockEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import org.joml.Vector3f;

public class PeacockFeatherEntity extends Projectile {

    private static final DustParticleOptions TRAIL_TEAL = new DustParticleOptions(new Vector3f(0.12f, 0.62f, 0.66f), 0.7f);
    private static final DustParticleOptions TRAIL_GOLD = new DustParticleOptions(new Vector3f(0.95f, 0.78f, 0.25f), 0.55f);
    private static final DustParticleOptions HEAL_DUST = new DustParticleOptions(new Vector3f(0.36f, 1f, 0.54f), 1.2f);
    private static final DustParticleOptions HEAL_RING = new DustParticleOptions(new Vector3f(0.6f, 1f, 0.4f), 0.9f);

    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(PeacockFeatherEntity.class, net.minecraft.network.syncher.EntityDataSerializers.INT);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> DATA_HOMING =
            SynchedEntityData.defineId(PeacockFeatherEntity.class, net.minecraft.network.syncher.EntityDataSerializers.INT);

    private static final int HOMING_LIFETIME_TICKS = 45;
    private static final double HOMING_TURN = 0.35;

    private int life = 0;

    public PeacockFeatherEntity(EntityType<? extends PeacockFeatherEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public static PeacockFeatherEntity launch(ServerLevel level, PeacockEntity peacock, Vec3 origin, Vec3 direction) {
        PeacockFeatherEntity feather = new PeacockFeatherEntity(OWEntityRegistry.PEACOCK_FEATHER.get(), level);
        feather.setOwner(peacock);
        feather.entityData.set(DATA_VARIANT, peacock.getVariant().getId());
        feather.setPos(origin.x, origin.y, origin.z);
        Vec3 velocity = direction.normalize().scale(OWAttacksConstants.Peacock.FEATHER_SPEED);
        feather.setDeltaMovement(velocity);
        feather.alignToMotion(velocity);
        feather.xRotO = feather.getXRot();
        feather.yRotO = feather.getYRot();
        level.addFreshEntity(feather);
        return feather;
    }

    public static PeacockFeatherEntity launchHoming(ServerLevel level, PeacockEntity peacock, Vec3 origin, Vec3 direction, LivingEntity target) {
        PeacockFeatherEntity feather = launch(level, peacock, origin, direction);
        feather.entityData.set(DATA_HOMING, target.getId());
        return feather;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_VARIANT, 0);
        builder.define(DATA_HOMING, -1);
    }

    public boolean isHoming() {
        return this.entityData.get(DATA_HOMING) >= 0;
    }

    private int lifetime() {
        return isHoming() ? HOMING_LIFETIME_TICKS : OWAttacksConstants.Peacock.FEATHER_LIFETIME_TICKS;
    }

    private Vec3 steer(Vec3 velocity) {
        if (!(this.level().getEntity(this.entityData.get(DATA_HOMING)) instanceof LivingEntity target) || !target.isAlive()) {
            return velocity;
        }
        Vec3 aim = target.position().add(0, target.getBbHeight() * 0.55, 0).subtract(this.position());
        if (aim.lengthSqr() < 1.0E-4) return velocity;
        double speed = OWAttacksConstants.Peacock.FEATHER_SPEED;
        Vec3 current = velocity.lengthSqr() < 1.0E-6 ? aim.normalize() : velocity.normalize();
        Vec3 turned = current.lerp(aim.normalize(), HOMING_TURN);
        if (turned.lengthSqr() < 1.0E-6) turned = aim.normalize();
        return turned.normalize().scale(speed);
    }

    public int getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    private void alignToMotion(Vec3 velocity) {
        double horizontal = velocity.horizontalDistance();
        this.setYRot((float) (Mth.atan2(velocity.x, velocity.z) * Mth.RAD_TO_DEG));
        this.setXRot((float) (Mth.atan2(velocity.y, horizontal) * Mth.RAD_TO_DEG));
    }

    @Override
    public void tick() {
        super.tick();
        life++;

        Vec3 velocity = this.getDeltaMovement();
        if (isHoming()) {
            velocity = steer(velocity);
            this.setDeltaMovement(velocity);
        }
        if (!this.level().isClientSide()) {
            HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hit.getType() != HitResult.Type.MISS) {
                this.onHit(hit);
                if (this.isRemoved()) return;
            }
            if (life >= lifetime()) {
                fade((ServerLevel) this.level());
                return;
            }
        }

        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        this.setPos(this.getX() + velocity.x, this.getY() + velocity.y, this.getZ() + velocity.z);
        alignToMotion(velocity);

        if (this.level().isClientSide()) spawnTrail(velocity);
    }

    private void spawnTrail(Vec3 velocity) {
        Level level = this.level();
        Vec3 back = velocity.normalize().scale(-0.35);
        level.addParticle(TRAIL_TEAL, this.getX() + back.x, this.getY() + back.y, this.getZ() + back.z, 0, 0, 0);
        level.addParticle(TRAIL_GOLD, this.getX() + back.x * 2, this.getY() + back.y * 2, this.getZ() + back.z * 2, 0, 0, 0);
    }

    public float fadeScale(float partialTick) {
        float age = life + partialTick;
        float grow = Mth.clamp(age / 2f, 0f, 1f);
        float remaining = lifetime() - age;
        float shrink = Mth.clamp(remaining / 4f, 0f, 1f);
        return grow * shrink;
    }

    public int getLife() {
        return life;
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (!target.isAlive() || target.isSpectator() || !target.isPickable()) return false;
        Entity owner = this.getOwner();
        boolean hittable = target instanceof LivingEntity || target instanceof net.neoforged.neoforge.entity.PartEntity<?>;
        if (owner == null) return hittable;
        if (target == owner || owner.hasPassenger(target) || target.getRootVehicle() == owner) return false;
        if (isHoming()) {
            return target instanceof LivingEntity living && owner instanceof PeacockEntity peacock && peacock.isFeatherAlly(living);
        }
        return hittable;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!(this.level() instanceof ServerLevel level)) return;
        Entity hit = result.getEntity();
        LivingEntity target = hit instanceof LivingEntity living ? living : null;

        PeacockEntity peacock = this.getOwner() instanceof PeacockEntity owner ? owner : null;
        boolean ally = target != null && peacock != null && peacock.isFeatherAlly(target);

        if (ally) {
            float before = target.getHealth();
            target.heal(OWAttacksConstants.Peacock.FEATHER_HEAL);
            boolean healed = target.getHealth() > before + 0.01f;
            healBurst(level, target, healed);
        } else {
            if (hit instanceof Player player && (player.isCreative() || player.isSpectator())) {
                this.discard();
                return;
            }
            Entity source = peacock != null ? peacock : this.getOwner();
            LivingEntity struck = target != null ? target
                    : hit instanceof net.neoforged.neoforge.entity.PartEntity<?> part && part.getParent() instanceof LivingEntity parent ? parent : null;
            if (struck != null) struck.invulnerableTime = 0;
            float damage = peacock != null
                    ? peacock.getCombatDamage() / peacock.getFeatherDamageDivisor()
                    : 2f;
            hit.hurt(this.damageSources().mobProjectile(this, source instanceof LivingEntity living ? living : null), damage);
            level.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 6, 0.2, 0.2, 0.2, 0.15);
            level.sendParticles(TRAIL_TEAL, this.getX(), this.getY(), this.getZ(), 6, 0.2, 0.2, 0.2, 0.02);
            level.playSound(null, hit.getX(), hit.getY(), hit.getZ(), SoundEvents.ARROW_HIT,
                    SoundSource.NEUTRAL, 0.6f, 1.6f + this.random.nextFloat() * 0.2f);
        }
        this.discard();
    }

    private void healBurst(ServerLevel level, LivingEntity target, boolean healed) {
        double radius = Math.max(0.5, target.getBbWidth() * 0.75);
        double height = target.getBbHeight();
        int points = 18;
        for (int i = 0; i < points; i++) {
            double angle = i * (Math.PI * 2.0 / points);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            level.sendParticles(HEAL_DUST, target.getX() + cos * radius, target.getY() + 0.1, target.getZ() + sin * radius,
                    0, cos * 0.6, 1.0, sin * 0.6, 0.12);
            level.sendParticles(HEAL_RING, target.getX() + cos * radius * 1.3, target.getY() + height * (0.2 + 0.6 * (i % 6) / 5.0),
                    target.getZ() + sin * radius * 1.3, 0, -cos, 0.4, -sin, 0.05);
        }
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + height * 0.5, target.getZ(),
                healed ? 18 : 8, radius * 0.7, height * 0.4, radius * 0.7, 0.0);
        level.sendParticles(ParticleTypes.COMPOSTER, target.getX(), target.getY() + height * 0.6, target.getZ(),
                12, radius * 0.6, height * 0.35, radius * 0.6, 0.0);
        level.sendParticles(ParticleTypes.HEART, target.getX(), target.getY() + height + 0.35, target.getZ(),
                healed ? 3 : 1, 0.35, 0.15, 0.35, 0.0);
        level.sendParticles(HEAL_DUST, this.getX(), this.getY(), this.getZ(), 14, 0.25, 0.25, 0.25, 0.04);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.NEUTRAL, 1.0f, 1.5f + this.random.nextFloat() * 0.3f);
        if (healed) {
            level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE,
                    SoundSource.NEUTRAL, 0.6f, 1.8f);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level() instanceof ServerLevel level) {
            Vec3 at = result.getLocation();
            level.sendParticles(TRAIL_TEAL, at.x, at.y, at.z, 6, 0.15, 0.15, 0.15, 0.02);
            level.sendParticles(ParticleTypes.POOF, at.x, at.y, at.z, 2, 0.05, 0.05, 0.05, 0.01);
            level.playSound(null, at.x, at.y, at.z, SoundEvents.WOOL_HIT, SoundSource.NEUTRAL, 0.5f, 1.4f);
        }
        this.discard();
    }

    private void fade(ServerLevel level) {
        level.sendParticles(TRAIL_TEAL, this.getX(), this.getY(), this.getZ(), 4, 0.15, 0.15, 0.15, 0.01);
        this.discard();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0;
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}
