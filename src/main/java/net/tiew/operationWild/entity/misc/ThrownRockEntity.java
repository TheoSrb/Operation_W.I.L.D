package net.tiew.operationWild.entity.misc;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;

public class ThrownRockEntity extends ThrowableItemProjectile {

    private float impactDamage = 6f;

    private static final int IMPACT_RING_POINTS = 10;
    private static final double ROCK_VISUAL_MARGIN = 0.06;

    private boolean exploded = false;

    private static final double SPIN_DEGREES_PER_BLOCK = 5.5;
    private static final float SPIN_RATE_MIN = 3f;
    private static final float SPIN_RATE_MAX = 12f;
    private static final double SPIN_AXIS_TILT = 0.8;

    private final float spinSeed = (float) (Math.random() * 360.0);

    private Vec3 spinAxis = null;
    private float spinRate = 0f;
    private float spin = 0f;
    private float spinPrev = 0f;

    public float getSpinSeed() {
        return spinSeed;
    }

    public Vec3 getSpinAxis() {
        return spinAxis;
    }

    public float getSpin(float partialTick) {
        return Mth.lerp(partialTick, spinPrev, spin);
    }

    public ThrownRockEntity(EntityType<? extends ThrownRockEntity> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownRockEntity(Level level, LivingEntity thrower, float impactDamage) {
        super(OWEntityRegistry.THROWN_ROCK.get(), thrower, level);
        this.impactDamage = impactDamage;
    }

    @Override
    protected Item getDefaultItem() {
        return Items.COBBLESTONE;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.055;
    }

    @Override
    public void tick() {
        this.spinPrev = this.spin;
        if (this.spinAxis == null) initSpin();
        this.spin += this.spinRate;

        super.tick();

        if (this.tickCount > OWAttacksConstants.Gorilla.ROCK_THROW_MAX_LIFETIME_TICKS) {
            this.discard();
            return;
        }

        if (!this.level().isClientSide() && !exploded) {
            BlockPos grazed = grazedBlock();
            if (grazed != null) {
                explode(this.getBoundingBox().getCenter(), grazed);
                return;
            }
        }

        if (this.level().isClientSide()) {
            Vec3 motion = this.getDeltaMovement();
            for (int i = 0; i < 2; i++) {
                double back = 0.35 * i;
                this.level().addParticle(
                        new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBBLESTONE.defaultBlockState()),
                        this.getX() - motion.x * back,
                        this.getY() - motion.y * back,
                        this.getZ() - motion.z * back,
                        0, 0, 0);
            }
            this.level().addParticle(ParticleTypes.SMOKE,
                    this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        }
    }

    private void initSpin() {
        Vec3 motion = this.getDeltaMovement();
        double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        if (horizontal < 1.0E-4) return;

        double tilt = (this.spinSeed / 360.0 - 0.5) * SPIN_AXIS_TILT;
        this.spinAxis = new Vec3(-motion.z / horizontal, tilt, motion.x / horizontal).normalize();
        this.spinRate = (float) Mth.clamp(motion.length() * SPIN_DEGREES_PER_BLOCK,
                SPIN_RATE_MIN, SPIN_RATE_MAX);
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (!super.canHitEntity(target)) return false;

        Entity owner = this.getOwner();
        if (target == owner) return false;
        if (owner != null && owner.hasPassenger(target)) return false;
        if (owner instanceof OWEntity thrower && thrower.isAlliedTo(target)) return false;
        return true;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (this.level().isClientSide()) {
            net.tiew.operationWild.event.ClientEvents.addGroundShake(this, false);
            return;
        }

        Vec3 at = result.getLocation();
        BlockPos struckPos = result instanceof BlockHitResult block
                ? block.getBlockPos()
                : BlockPos.containing(at.x, at.y - 0.35, at.z);
        explode(at, struckPos);
    }

    private BlockPos grazedBlock() {
        AABB box = this.getBoundingBox().inflate(ROCK_VISUAL_MARGIN);

        if (this.level().noCollision(this, box)) return null;

        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                BlockPos.containing(box.minX, box.minY, box.minZ),
                BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {

            BlockState state = this.level().getBlockState(pos);
            if (state.getCollisionShape(this.level(), pos).isEmpty()) continue;

            double dist = pos.getCenter().distanceToSqr(box.getCenter());
            if (dist < bestDist) {
                bestDist = dist;
                best = pos.immutable();
            }
        }
        return best;
    }

    private void explode(Vec3 at, BlockPos struckPos) {
        if (exploded) return;
        exploded = true;

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            this.discard();
            return;
        }

        double radius = OWAttacksConstants.Gorilla.ROCK_THROW_IMPACT_RADIUS;
        Entity owner = this.getOwner();

        double x = at.x;
        double y = at.y;
        double z = at.z;

        BlockState struck = this.level().getBlockState(struckPos);
        if (struck.isAir()) struck = this.level().getBlockState(BlockPos.containing(x, y - 1.0, z));
        if (struck.isAir()) struck = Blocks.COBBLESTONE.defaultBlockState();

        BlockParticleOption rubble = new BlockParticleOption(ParticleTypes.BLOCK, struck);
        BlockParticleOption dirt = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState());

        serverLevel.sendParticles(ParticleTypes.EXPLOSION, x, y + 0.25, z, 1, 0.0, 0.0, 0.0, 0.0);

        serverLevel.sendParticles(rubble, x, y + 0.15, z, 95, radius * 0.35, 0.20, radius * 0.35, 0.38);
        serverLevel.sendParticles(dirt, x, y + 0.05, z, 55, radius * 0.5, 0.12, radius * 0.5, 0.30);
        serverLevel.sendParticles(ParticleTypes.CLOUD, x, y + 0.2, z, 24, radius * 0.4, 0.08, radius * 0.4, 0.05);

        for (int i = 0; i < IMPACT_RING_POINTS; i++) {
            double angle = (Math.PI * 2.0 * i) / IMPACT_RING_POINTS;
            double rx = x + Math.cos(angle) * radius * 0.85;
            double rz = z + Math.sin(angle) * radius * 0.85;
            serverLevel.sendParticles(dirt, rx, y + 0.08, rz, 3, 0.12, 0.06, 0.12, 0.10);
            serverLevel.sendParticles(ParticleTypes.CLOUD, rx, y + 0.15, rz, 1, 0.08, 0.04, 0.08, 0.015);
        }

        SoundType soundType = struck.getSoundType(this.level(), struckPos, this);
        serverLevel.playSound(null, x, y, z, soundType.getBreakSound(), SoundSource.NEUTRAL, 1.8f, 0.62f);
        serverLevel.playSound(null, x, y, z, SoundEvents.ROOTED_DIRT_HIT, SoundSource.NEUTRAL, 1.4f, 0.5f);

        AABB area = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);

        for (LivingEntity living : serverLevel.getEntitiesOfClass(LivingEntity.class, area)) {
            if (living == owner) continue;
            if (owner instanceof OWEntity gorilla && gorilla.isAlliedTo(living)) continue;

            double distance = living.position().add(0, living.getBbHeight() * 0.5, 0).distanceTo(at);
            if (distance > radius) continue;

            float falloff = (float) (1.0 - distance / radius);
            living.hurt(this.damageSources().thrown(this, owner), impactDamage * falloff);

            Vec3 push = living.position().subtract(at);
            if (push.lengthSqr() < 1.0E-4) push = new Vec3(0, 1, 0);
            push = push.normalize().scale(OWAttacksConstants.Gorilla.ROCK_THROW_KNOCKBACK * falloff);
            living.push(push.x, Math.max(0.35, push.y), push.z);
            living.hurtMarked = true;
        }

        this.discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("impactDamage", this.impactDamage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("impactDamage")) this.impactDamage = tag.getFloat("impactDamage");
    }
}
