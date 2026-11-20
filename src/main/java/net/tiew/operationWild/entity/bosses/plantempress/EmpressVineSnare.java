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
    public static final int WARN_TICKS = 40;
    public static final int ARMED_TICKS = 50;
    public static final int ROOT_TICKS = 20;
    private static final int RETRACT_TICKS = 8;

    private static final EntityDataAccessor<Integer> SPRUNG = SynchedEntityData.defineId(EmpressVineSnare.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HOLD_UNTIL = SynchedEntityData.defineId(EmpressVineSnare.class, EntityDataSerializers.INT);

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
        if (level instanceof net.minecraft.server.level.ServerLevel server) {
            EmpressFx.decal(server, EmpressFx.SNARE, at, RADIUS + 0.4, WARN_TICKS + ARMED_TICKS, server.random.nextFloat() * 360f);
        }
        return snare;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SPRUNG, -1);
        builder.define(HOLD_UNTIL, -1);
    }

    private int sprung() {
        return this.entityData.get(SPRUNG);
    }

    private int holdUntil() {
        return this.entityData.get(HOLD_UNTIL);
    }

    public void hold(int ticks) {
        this.entityData.set(HOLD_UNTIL, this.tickCount + ticks);
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
                float charge = t / (float) WARN_TICKS;
                if (t % 2 == 0) {
                    EmpressDamage.ring(level, position(), RADIUS, EmpressDamage.VINE, 0.45);
                    EmpressDamage.ring(level, position(), RADIUS * (0.35 + 0.6 * ((t % 10) / 10.0)), EmpressDamage.TOXIC, 0.7);
                    level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, getX(), getY() + 0.3, getZ(), 3, RADIUS * 0.5, 0.1, RADIUS * 0.5, 0);
                    level.sendParticles(EmpressDamage.VINE, getX(), getY() + 0.5 + charge * 4, getZ(), 4, 0.15, 1.2 * charge, 0.15, 0);
                }
                if (t % 4 == 0) {
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ROOTED_DIRT.defaultBlockState()), getX(), getY() + 0.1, getZ(),
                            6, RADIUS * 0.6, 0.05, RADIUS * 0.6, 0.05);
                }
                if (t == 0 || t == WARN_TICKS / 2) {
                    level.playSound(null, getX(), getY(), getZ(), OWSounds.PLANT_EMPRESS_SNARE.get(), SoundSource.HOSTILE, 1.4f, t == 0 ? 0.7f : 1.0f);
                }
                if (t == WARN_TICKS - 1) {
                    level.playSound(null, getX(), getY(), getZ(), OWSounds.PLANT_EMPRESS_ROOTS.get(), SoundSource.HOSTILE, 1.6f, 1.3f);
                }
                return;
            }
            if (t >= WARN_TICKS + ARMED_TICKS) {
                spring(t);
                return;
            }
            if (t % 3 == 0) {
                EmpressDamage.ring(level, position(), RADIUS, EmpressDamage.VINE, 0.4);
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
                this.setPos(target.getX(), getY(), target.getZ());
                if (!owner.brain().onSnared(level, target, this)) {
                    rooted = target;
                    anchor = target.position();
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ROOT_TICKS, 6, false, false));
                }
                spring(t);
                return;
            }
            return;
        }
        if (rooted != null && rooted.isAlive() && t - sprung <= ROOT_TICKS) {
            Vec3 delta = anchor.subtract(rooted.position());
            rooted.setDeltaMovement(delta.scale(0.5).add(0, Math.min(0, rooted.getDeltaMovement().y), 0));
            rooted.hurtMarked = true;
        }
        int end = Math.max(holdUntil(), sprung + (rooted != null ? ROOT_TICKS : 0));
        if (t > end + RETRACT_TICKS + 2) this.discard();
    }

    private void spring(int t) {
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
            int hold = Math.max(holdUntil(), sprung + 3);
            if (after < 3) return 1.8f + after * 0.25f;
            if (t < hold) return 2.6f + 0.2f * Mth.sin(t * 0.45f);
            return Math.max(0f, 2.6f * (1f - (t - hold) / RETRACT_TICKS));
        }
        if (t < WARN_TICKS) return 0.15f + 0.45f * (t / WARN_TICKS);
        if (t < WARN_TICKS + 4) return 0.6f + 1.2f * (t - WARN_TICKS) / 4f;
        return 1.8f + 0.15f * Mth.sin(t * 0.4f);
    }

    @Override
    public float visualRing() {
        float t = this.tickCount;
        int sprung = sprung();
        if (sprung >= 0 && t < Math.max(holdUntil(), sprung + 3)) return Mth.lerp(Mth.clamp((t - sprung) / 5f, 0f, 1f), RADIUS * 0.75f, 0.75f);
        return RADIUS * 0.75f;
    }

    @Override
    public int visualCopies() {
        return 9;
    }

    @Override
    public float visualSpin(float partialTick) {
        return (this.tickCount + partialTick) * (sprung() >= 0 ? 6f : 1.5f);
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
