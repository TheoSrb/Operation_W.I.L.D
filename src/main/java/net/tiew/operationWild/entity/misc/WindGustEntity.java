package net.tiew.operationWild.entity.misc;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;

import java.util.HashSet;
import java.util.Set;

public class WindGustEntity extends Entity {

    private static final EntityDataAccessor<Integer> DATA_AGE = SynchedEntityData.defineId(WindGustEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_OWNER = SynchedEntityData.defineId(WindGustEntity.class, EntityDataSerializers.INT);

    private final Set<Integer> pushed = new HashSet<>();
    private Vec3 direction = Vec3.ZERO;

    public WindGustEntity(EntityType<? extends WindGustEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static WindGustEntity launch(Level level, LivingEntity owner, Vec3 origin, float yaw) {
        WindGustEntity gust = new WindGustEntity(OWEntityRegistry.WIND_GUST.get(), level);
        gust.entityData.set(DATA_OWNER, owner.getId());
        gust.direction = Vec3.directionFromRotation(0f, yaw);
        gust.setYRot(yaw);
        gust.setPos(origin.x, origin.y, origin.z);
        gust.setDeltaMovement(gust.direction.scale(OWAttacksConstants.Peacock.GUST_SPEED));
        level.addFreshEntity(gust);
        return gust;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_AGE, 0);
        builder.define(DATA_OWNER, -1);
    }

    public int getAge() {
        return this.entityData.get(DATA_AGE);
    }

    public float lifeProgress(float partialTick) {
        return Mth.clamp((getAge() + partialTick) / OWAttacksConstants.Peacock.GUST_LIFETIME_TICKS, 0f, 1f);
    }

    public double currentRadius(float partialTick) {
        return Mth.lerp(lifeProgress(partialTick), OWAttacksConstants.Peacock.GUST_START_RADIUS, OWAttacksConstants.Peacock.GUST_END_RADIUS);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);

        if (this.level().isClientSide()) {
            spawnTrail();
            return;
        }

        int age = getAge() + 1;
        this.entityData.set(DATA_AGE, age);
        if (age > OWAttacksConstants.Peacock.GUST_LIFETIME_TICKS) {
            this.discard();
            return;
        }

        pushEntities((ServerLevel) this.level(), age);
    }

    private void pushEntities(ServerLevel level, int age) {
        Entity owner = level.getEntity(this.entityData.get(DATA_OWNER));
        double radius = currentRadius(0f);
        float fade = 1f - (float) age / OWAttacksConstants.Peacock.GUST_LIFETIME_TICKS * 0.55f;
        Vec3 push = flatDirection();

        AABB box = new AABB(this.getX() - radius, this.getY() - radius * 0.6, this.getZ() - radius,
                this.getX() + radius, this.getY() + radius * 1.2, this.getZ() + radius);

        for (Entity target : level.getEntities(this, box, e -> e instanceof LivingEntity && e.isAlive())) {
            if (owner != null && (target == owner || target.isPassengerOfSameVehicle(owner) || owner.hasPassenger(target))) continue;
            if (owner instanceof LivingEntity livingOwner && livingOwner.isAlliedTo(target)) continue;
            if (!pushed.add(target.getId())) continue;

            Entity source = owner != null ? owner : this;
            OWUtils.addVelocityResisted(source, target,
                    push.x * OWAttacksConstants.Peacock.GUST_PUSH * fade,
                    OWAttacksConstants.Peacock.GUST_LIFT * fade,
                    push.z * OWAttacksConstants.Peacock.GUST_PUSH * fade);
            target.hurtMarked = true;
            target.fallDistance = 0f;

            level.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    8, 0.3, 0.3, 0.3, 0.08);
        }
    }

    private Vec3 flatDirection() {
        Vec3 motion = this.getDeltaMovement();
        Vec3 flat = new Vec3(motion.x, 0, motion.z);
        if (flat.lengthSqr() < 1.0E-6) flat = Vec3.directionFromRotation(0f, this.getYRot());
        return flat.normalize();
    }

    private void spawnTrail() {
        double radius = currentRadius(0f);
        float progress = lifeProgress(0f);
        Vec3 forward = flatDirection();
        Vec3 side = new Vec3(-forward.z, 0, forward.x);
        int count = 10;

        for (int i = 0; i < count; i++) {
            double angle = (this.tickCount * 0.9 + i * (Math.PI * 2 / count)) % (Math.PI * 2);
            double s = Math.cos(angle) * radius;
            double u = Math.sin(angle) * radius * 0.7 + radius * 0.35;
            double x = this.getX() + side.x * s;
            double y = this.getY() + u;
            double z = this.getZ() + side.z * s;
            this.level().addParticle(progress > 0.6f ? ParticleTypes.POOF : ParticleTypes.CLOUD,
                    x, y, z, forward.x * 0.25, 0.01, forward.z * 0.25);
        }
        if (this.random.nextFloat() < 0.7f) {
            this.level().addParticle(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY() + radius * 0.4, this.getZ(), 0, 0, 0);
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
