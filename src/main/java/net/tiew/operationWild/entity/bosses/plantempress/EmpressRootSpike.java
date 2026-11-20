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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.bosses.PlantEmpressEntity;
import net.tiew.operationWild.sound.OWSounds;

public class EmpressRootSpike extends Entity implements EmpressHazardVisual {

    private static final EntityDataAccessor<Integer> WARN = SynchedEntityData.defineId(EmpressRootSpike.class, EntityDataSerializers.INT);
    private static final int RISE = 3;
    private static final int HOLD = 16;
    private static final int RETRACT = 8;

    private PlantEmpressEntity owner;
    private float damage;
    private double lift = 0.8;
    private boolean quiet;

    public EmpressRootSpike(EntityType<? extends EmpressRootSpike> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static EmpressRootSpike create(Level level, PlantEmpressEntity owner, double x, double y, double z, int warn, float damage) {
        EmpressRootSpike spike = new EmpressRootSpike(OWEntityRegistry.EMPRESS_ROOT_SPIKE.get(), level);
        spike.owner = owner;
        spike.damage = damage;
        spike.entityData.set(WARN, warn);
        spike.setPos(x, y, z);
        spike.setYRot(level.random.nextFloat() * 360f);
        return spike;
    }

    public EmpressRootSpike lift(double lift) {
        this.lift = lift;
        return this;
    }

    public EmpressRootSpike quiet() {
        this.quiet = true;
        return this;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(WARN, 14);
    }

    public int warn() {
        return this.entityData.get(WARN);
    }

    @Override
    public void tick() {
        super.tick();
        int warn = warn();
        if (!(this.level() instanceof ServerLevel level)) return;
        BlockState ground = this.level().getBlockState(blockPosition().below());
        if (ground.isAir()) ground = Blocks.DIRT.defaultBlockState();
        if (this.tickCount < warn) {
            if (this.tickCount % 2 == 0) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), getX(), getY() + 0.1, getZ(),
                        3, 0.3, 0.02, 0.3, 0.05);
                if (damage > 0f) level.sendParticles(EmpressDamage.EARTH, getX(), getY() + 0.15, getZ(), 3, 0.35, 0, 0.35, 0);
            }
        } else if (this.tickCount == warn) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), getX(), getY() + 0.4, getZ(),
                    18, 0.3, 0.4, 0.3, 0.2);
            if (!quiet) level.playSound(null, getX(), getY(), getZ(), OWSounds.PLANT_EMPRESS_ROOTS.get(), SoundSource.HOSTILE,
                    0.9f, 0.8f + random.nextFloat() * 0.4f);
            if (damage > 0f) {
                AABB box = new AABB(getX() - 0.9, getY() - 0.2, getZ() - 0.9, getX() + 0.9, getY() + 2.4, getZ() + 0.9);
                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, e -> EmpressDamage.canHit(e, owner))) {
                    EmpressDamage.strike(owner, target, EmpressDamage.roots(this, owner), damage, position(), 0.25, lift, false);
                }
            }
        } else if (this.tickCount > warn + RISE + HOLD + RETRACT) {
            this.discard();
        }
    }

    @Override
    public BlockState visualBlock() {
        return Blocks.MANGROVE_ROOTS.defaultBlockState();
    }

    @Override
    public float visualWidth(float partialTick) {
        return 0.9f;
    }

    @Override
    public float visualHeight(float partialTick) {
        float t = this.tickCount + partialTick - warn();
        if (t <= 0f) return 0f;
        if (t < RISE) return 2.6f * Mth.sin(t / RISE * Mth.HALF_PI);
        if (t < RISE + HOLD) return 2.6f;
        return 2.6f * Mth.clamp(1f - (t - RISE - HOLD) / RETRACT, 0f, 1f);
    }

    @Override
    public float visualSpin(float partialTick) {
        return this.getYRot();
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
