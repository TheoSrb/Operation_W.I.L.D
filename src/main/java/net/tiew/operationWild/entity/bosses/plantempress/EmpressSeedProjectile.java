package net.tiew.operationWild.entity.bosses.plantempress;

import net.tiew.operationWild.particle.EmpressFx;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.bosses.PlantEmpressEntity;
import net.tiew.operationWild.sound.OWSounds;

public class EmpressSeedProjectile extends Entity implements EmpressHazardVisual {

    public static final int SPORE = 0;
    public static final int THORN = 1;
    public static final float SPORE_RADIUS = 2.5f;
    public static final double GRAVITY = 0.05;

    private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.defineId(EmpressSeedProjectile.class, EntityDataSerializers.INT);

    private PlantEmpressEntity owner;
    private Vec3 landing = Vec3.ZERO;
    private int flight = 20;

    public EmpressSeedProjectile(EntityType<? extends EmpressSeedProjectile> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static EmpressSeedProjectile launch(Level level, PlantEmpressEntity owner, int kind, Vec3 from, Vec3 landing, int flight) {
        EmpressSeedProjectile seed = new EmpressSeedProjectile(OWEntityRegistry.EMPRESS_SEED.get(), level);
        seed.owner = owner;
        seed.entityData.set(KIND, kind);
        seed.landing = landing;
        seed.flight = flight;
        seed.setPos(from.x, from.y, from.z);
        Vec3 delta = landing.subtract(from);
        seed.setDeltaMovement(delta.x / flight, delta.y / flight + 0.5 * GRAVITY * flight, delta.z / flight);
        if (level instanceof net.minecraft.server.level.ServerLevel server) {
            if (kind == SPORE) EmpressFx.decal(server, EmpressFx.SPORE, landing, SPORE_RADIUS, flight, server.random.nextFloat() * 360f);
            else EmpressFx.decal(server, EmpressFx.SEED, landing, 0.9, flight + 4, server.random.nextFloat() * 360f);
        }
        return seed;
    }

    public int kind() {
        return this.entityData.get(KIND);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(KIND, SPORE);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
        this.setDeltaMovement(motion.x, motion.y - GRAVITY, motion.z);
        if (this.level().isClientSide()) {
            this.level().addParticle(kind() == SPORE ? ParticleTypes.SPORE_BLOSSOM_AIR : ParticleTypes.COMPOSTER,
                    getX(), getY(), getZ(), 0, 0, 0);
            this.level().addParticle(ParticleTypes.FALLING_SPORE_BLOSSOM, getX(), getY(), getZ(), 0, 0, 0);
            return;
        }
        ServerLevel level = (ServerLevel) this.level();
        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }
        if (this.tickCount % 2 == 0) {
            if (kind() == SPORE) {
                EmpressDamage.ring(level, landing, SPORE_RADIUS, EmpressDamage.TOXIC, 0.55);
                level.sendParticles(EmpressDamage.WARNING, landing.x, landing.y + 0.15, landing.z, 3, 0.6, 0, 0.6, 0);
            } else {
                EmpressDamage.ring(level, landing, 0.8, EmpressDamage.WARNING, 0.4);
            }
        }
        if (this.tickCount >= flight || (this.tickCount > 3 && getY() <= landing.y)) land(level);
    }

    private void land(ServerLevel level) {
        BlockState ground = level.getBlockState(net.minecraft.core.BlockPos.containing(landing).below());
        if (ground.isAir()) ground = Blocks.DIRT.defaultBlockState();
        if (kind() == SPORE) {
            level.playSound(null, landing.x, landing.y, landing.z, OWSounds.PLANT_EMPRESS_SPORE_BURST.get(), SoundSource.HOSTILE, 1.6f,
                    0.8f + random.nextFloat() * 0.3f);
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, landing.x, landing.y + 0.6, landing.z, 60, SPORE_RADIUS * 0.6, 0.6, SPORE_RADIUS * 0.6, 0.02);
            level.sendParticles(EmpressDamage.TOXIC, landing.x, landing.y + 0.5, landing.z, 40, SPORE_RADIUS * 0.5, 0.4, SPORE_RADIUS * 0.5, 0);
            level.sendParticles(ParticleTypes.SNEEZE, landing.x, landing.y + 0.4, landing.z, 25, 1.2, 0.3, 1.2, 0.05);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), landing.x, landing.y + 0.2, landing.z, 20, 0.8, 0.2, 0.8, 0.15);
            AABB box = new AABB(landing.x - SPORE_RADIUS, landing.y - 0.5, landing.z - SPORE_RADIUS,
                    landing.x + SPORE_RADIUS, landing.y + 2.5, landing.z + SPORE_RADIUS);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, e -> EmpressDamage.canHit(e, owner))) {
                if (target.position().distanceToSqr(landing.x, target.getY(), landing.z) > SPORE_RADIUS * SPORE_RADIUS) continue;
                EmpressDamage.strike(owner, target, EmpressDamage.roots(this, owner), EmpressBalance.DAMAGE_SPORE, landing, 0.35, 0.3, false);
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
            }
            AreaEffectCloud cloud = new AreaEffectCloud(level, landing.x, landing.y, landing.z);
            cloud.setOwner(owner);
            cloud.setRadius(SPORE_RADIUS);
            cloud.setRadiusPerTick(-SPORE_RADIUS / 70f);
            cloud.setDuration(70);
            cloud.setWaitTime(4);
            cloud.setParticle(ParticleTypes.SPORE_BLOSSOM_AIR);
            cloud.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
            level.addFreshEntity(cloud);
        } else {
            level.playSound(null, landing.x, landing.y, landing.z, OWSounds.PLANT_EMPRESS_SEED_PLANT.get(), SoundSource.HOSTILE, 1.2f,
                    0.9f + random.nextFloat() * 0.3f);
            level.sendParticles(ParticleTypes.COMPOSTER, landing.x, landing.y + 0.3, landing.z, 14, 0.4, 0.2, 0.4, 0.05);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), landing.x, landing.y + 0.2, landing.z, 12, 0.4, 0.2, 0.4, 0.1);
            if (owner.brain().canPlantBush()) {
                EmpressThornBush bush = EmpressThornBush.create(level, owner, landing.x, landing.y, landing.z);
                level.addFreshEntity(bush);
                owner.brain().track(bush);
            }
        }
        this.discard();
    }

    @Override
    public BlockState visualBlock() {
        return kind() == SPORE ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.FLOWERING_AZALEA.defaultBlockState();
    }

    @Override
    public float visualWidth(float partialTick) {
        return kind() == SPORE ? 0.55f : 0.45f;
    }

    @Override
    public float visualHeight(float partialTick) {
        return visualWidth(partialTick);
    }

    @Override
    public float visualSpin(float partialTick) {
        return (this.tickCount + partialTick) * 24f;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}
