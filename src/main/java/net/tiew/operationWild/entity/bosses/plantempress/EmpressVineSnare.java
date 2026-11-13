package net.tiew.operationWild.entity.bosses.plantempress;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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

public class EmpressVineSnare extends Entity implements EmpressHazardVisual {

    public static final float RADIUS = 1.8f;
    public static final int WARN_TICKS = 30;
    public static final int ARMED_TICKS = 50;
    public static final int ROOT_TICKS = 20;
    private static final int RETRACT_TICKS = 8;

    private static final EntityDataAccessor<Integer> SPRUNG = SynchedEntityData.defineId(EmpressVineSnare.class, EntityDataSerializers.INT);

    private PlantEmpressEntity owner;
    private LivingEntity rooted;
    private Vec3 anchor;

    public EmpressVineSnare(EntityType<? extends EmpressVineSnare> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static EmpressVineSnare create(Level level, PlantEmpressEntity owner, Vec3 at) {
        EmpressVineSnare snare = new EmpressVineSnare(OWEntityRegistry.EMPRESS_VINE_SNARE.get(), level);
        snare.owner = owner;
        snare.setPos(at.x, at.y, at.z);
        return snare;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SPRUNG, -1);
    }

    private int sprung() {
        return this.entityData.get(SPRUNG);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;
        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }
        int t = this.tickCount;
        int sprung = sprung();
        if (sprung < 0) {
            if (t < WARN_TICKS) {
                if (t % 2 == 0) {
                    EmpressDamage.ring(level, position(), RADIUS, EmpressDamage.VINE, 0.55);
                    level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, getX(), getY() + 0.3, getZ(), 2, RADIUS * 0.5, 0.1, RADIUS * 0.5, 0);
                }
                if (t == WARN_TICKS - 1) {
                    level.playSound(null, getX(), getY(), getZ(), OWSounds.PLANT_EMPRESS_SNARE.get(), SoundSource.HOSTILE, 1.2f, 1.2f);
                }
                return;
            }
            if (t >= WARN_TICKS + ARMED_TICKS) {
                spring(level, t);
                return;
            }
            if (t % 3 == 0) {
                EmpressDamage.ring(level, position(), RADIUS, EmpressDamage.VINE, 0.45);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.VINE.defaultBlockState()), getX(), getY() + 0.6, getZ(),
                        3, RADIUS * 0.5, 0.4, RADIUS * 0.5, 0.02);
            }
            AABB box = new AABB(getX() - RADIUS, getY() - 0.5, getZ() - RADIUS, getX() + RADIUS, getY() + 2.5, getZ() + RADIUS);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, e -> EmpressDamage.canHit(e, owner))) {
                double dx = target.getX() - getX();
                double dz = target.getZ() - getZ();
                if (dx * dx + dz * dz > RADIUS * RADIUS) continue;
                level.playSound(null, getX(), getY(), getZ(), OWSounds.PLANT_EMPRESS_SNARE.get(), SoundSource.HOSTILE, 2f, 0.8f);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.VINE.defaultBlockState()), target.getX(), target.getY() + 0.8,
                        target.getZ(), 30, 0.4, 0.6, 0.4, 0.1);
                if (!owner.brain().onSnared(level, target, position())) {
                    rooted = target;
                    anchor = target.position();
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ROOT_TICKS, 6, false, false));
                }
                spring(level, t);
                return;
            }
            return;
        }
        if (rooted != null && rooted.isAlive() && t - sprung <= ROOT_TICKS) {
            if (rooted.position().distanceToSqr(anchor) > 0.04) rooted.teleportTo(anchor.x, anchor.y, anchor.z);
            rooted.setDeltaMovement(0, Math.min(0, rooted.getDeltaMovement().y), 0);
            rooted.hurtMarked = true;
        }
        if (t - sprung > Math.max(RETRACT_TICKS, rooted != null ? ROOT_TICKS : 0) + 2) this.discard();
    }

    private void spring(ServerLevel level, int t) {
        this.entityData.set(SPRUNG, t);
    }

    @Override
    public BlockState visualBlock() {
        return Blocks.TWISTING_VINES.defaultBlockState();
    }

    @Override
    public float visualWidth(float partialTick) {
        return 0.9f;
    }

    @Override
    public float visualHeight(float partialTick) {
        float t = this.tickCount + partialTick;
        int sprung = sprung();
        if (sprung >= 0) {
            float after = t - sprung;
            if (after < 3) return 2.0f + after * 0.4f;
            return Math.max(0f, 3.2f * (1f - (after - 3) / RETRACT_TICKS));
        }
        if (t < WARN_TICKS - 10) return 0.25f * Mth.clamp(t / (WARN_TICKS - 10), 0f, 1f);
        if (t < WARN_TICKS) return 0.25f;
        if (t < WARN_TICKS + 4) return 0.25f + 1.55f * (t - WARN_TICKS) / 4f;
        return 1.8f + 0.15f * Mth.sin(t * 0.4f);
    }

    @Override
    public float visualRing() {
        return RADIUS * 0.75f;
    }

    @Override
    public int visualCopies() {
        return 9;
    }

    @Override
    public float visualSpin(float partialTick) {
        return (this.tickCount + partialTick) * 1.5f;
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
