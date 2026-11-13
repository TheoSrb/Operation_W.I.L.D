package net.tiew.operationWild.entity.bosses.plantempress;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.bosses.PlantEmpressEntity;

public class EmpressThornBush extends Entity implements EmpressHazardVisual {

    public static final int ARM_TICKS = 30;
    public static final int LIFE_TICKS = 220;
    private static final BlockState BUSH = Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 3);

    private PlantEmpressEntity owner;

    public EmpressThornBush(EntityType<? extends EmpressThornBush> type, Level level) {
        super(type, level);
    }

    public static EmpressThornBush create(Level level, PlantEmpressEntity owner, double x, double y, double z) {
        EmpressThornBush bush = new EmpressThornBush(OWEntityRegistry.EMPRESS_THORN_BUSH.get(), level);
        bush.owner = owner;
        bush.setPos(x, y, z);
        bush.setYRot(level.random.nextFloat() * 360f);
        return bush;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.onGround()) {
            this.setDeltaMovement(0, Math.max(-0.6, this.getDeltaMovement().y - 0.08), 0);
            this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
        }
        if (!(this.level() instanceof ServerLevel level)) return;
        if (this.tickCount >= LIFE_TICKS || owner == null || !owner.isAlive()) {
            wither(level);
            return;
        }
        if (this.tickCount < ARM_TICKS) {
            if (this.tickCount % 3 == 0) level.sendParticles(EmpressDamage.WARNING, getX(), getY() + 0.2, getZ(), 2, 0.4, 0.05, 0.4, 0);
            return;
        }
        if (this.tickCount % 14 == 0) {
            AABB box = getBoundingBox().inflate(0.5, 0.4, 0.5);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, e -> EmpressDamage.canHit(e, owner))) {
                target.hurt(EmpressDamage.bite(owner, position()), EmpressBalance.DAMAGE_SPROUT);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 0));
                level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 0.5, target.getZ(), 6, 0.3, 0.3, 0.3, 0.1);
                level.playSound(null, getX(), getY(), getZ(), net.tiew.operationWild.sound.OWSounds.PLANT_EMPRESS_BITE.get(), SoundSource.HOSTILE, 0.8f, 1.6f);
            }
        }
    }

    private void wither(ServerLevel level) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MOSS_BLOCK.defaultBlockState()), getX(), getY() + 0.4, getZ(), 16, 0.3, 0.4, 0.3, 0.05);
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, getX(), getY() + 0.6, getZ(), 8, 0.3, 0.3, 0.3, 0.02);
        this.discard();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide() || this.isRemoved()) return false;
        if (source.getEntity() instanceof Player || source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile) {
            this.level().playSound(null, getX(), getY(), getZ(), SoundEvents.BIG_DRIPLEAF_BREAK, SoundSource.BLOCKS, 1f, 1.2f);

            wither((ServerLevel) this.level());
            return true;
        }
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return true;
    }

    @Override
    public BlockState visualBlock() {
        return BUSH;
    }

    @Override
    public float visualWidth(float partialTick) {
        float t = Mth.clamp((this.tickCount + partialTick) / ARM_TICKS, 0f, 1f);
        float end = Mth.clamp((LIFE_TICKS - this.tickCount - partialTick) / 10f, 0f, 1f);
        return (0.3f + 0.9f * t) * end;
    }

    @Override
    public float visualHeight(float partialTick) {
        return visualWidth(partialTick) * 1.15f;
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
}
