package net.tiew.operationWild.entity.bosses;

import net.tiew.operationWild.particle.EmpressFx;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import net.tiew.operationWild.entity.bosses.plantempress.EmpressAnim;
import net.minecraft.core.BlockPos;
import net.tiew.operationWild.entity.bosses.plantempress.EmpressBalance;
import net.tiew.operationWild.entity.bosses.plantempress.EmpressBrain;
import net.tiew.operationWild.entity.bosses.plantempress.EmpressDamage;

import net.tiew.operationWild.entity.bosses.plantempress.EmpressPose;
import net.tiew.operationWild.entity.bosses.plantempress.EmpressRig;
import net.tiew.operationWild.entity.bosses.plantempress.EmpressTimings;
import net.tiew.operationWild.entity.bosses.plantempress.PlantEmpressPart;
import net.tiew.operationWild.entity.misc.VenomousArrow;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class PlantEmpressEntity extends OWBossEntity {

    public static final int PHASE_SPAWN = 0;
    public static final int PHASE_FIGHT = 1;
    public static final int PHASE_TRANSITION = 2;
    public static final int PHASE_DESPERATION = 3;
    public static final int PHASE_DORMANT = 4;
    public static final int PHASE_AWAKEN = 5;
    public static final int PHASE_DYING = 6;
    public static final int PHASE_BURROW = 7;

    public static final int ACTION_NONE = -1;
    public static final int ACTION_DODGE = -2;

    public static final byte EVENT_BURST = 70;
    public static final byte EVENT_ROAR = 71;
    public static final byte EVENT_SLAM = 72;
    public static final byte EVENT_TREMOR = 73;
    public static final byte EVENT_HEAD_DEATH = 74;
    public static final byte EVENT_STAGGER = 75;
    public static final byte EVENT_DESPERATION = 76;

    public static final float MAIN_HEALTH = 360f;
    public static final float SIDE_HEALTH = 180f;
    public static final double ARENA_RADIUS = 24.0;
    public static final double LEASH_RADIUS = 42.0;
    public static final double AWAKEN_RADIUS = 22.0;

    private static final float SAP_DECAY_PER_TICK = 0.0004f;
    private static final int SAP_DECAY_DELAY = 120;
    private static final int HEAD_INVULNERABLE_TICKS = 10;
    private static final int REFLEX_RECHARGE = 160;
    private static final int REGEN_DELAY = 200;
    private static final int RESET_DELAY = 600;

    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> PHASE_START = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> IDLE_EPOCH = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> RAGE_START = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> WALL_START = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> BODY_ACTION = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> BODY_START = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Float> BODY_AIM = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> REFLEXES = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STEM_ACTION = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> STEM_START = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<BlockPos> HOME = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.BLOCK_POS);
    @SuppressWarnings("unchecked")
    private static final EntityDataAccessor<Float>[] CHAIN_STRETCH = new EntityDataAccessor[3];
    @SuppressWarnings("unchecked")
    private static final EntityDataAccessor<Float>[] CHAIN_PITCH = new EntityDataAccessor[3];
    @SuppressWarnings("unchecked")
    private static final EntityDataAccessor<Float>[] HEALTH = new EntityDataAccessor[3];
    @SuppressWarnings("unchecked")
    private static final EntityDataAccessor<Float>[] MAX = new EntityDataAccessor[3];
    @SuppressWarnings("unchecked")
    private static final EntityDataAccessor<Integer>[] CHAIN_ACTION = new EntityDataAccessor[3];
    @SuppressWarnings("unchecked")
    private static final EntityDataAccessor<Long>[] CHAIN_START = new EntityDataAccessor[3];
    @SuppressWarnings("unchecked")
    private static final EntityDataAccessor<Float>[] CHAIN_AIM = new EntityDataAccessor[3];
    @SuppressWarnings("unchecked")
    private static final EntityDataAccessor<Long>[] HURT_START = new EntityDataAccessor[3];
    @SuppressWarnings("unchecked")
    private static final EntityDataAccessor<Long>[] DEATH_START = new EntityDataAccessor[3];
    @SuppressWarnings("unchecked")
    private static final EntityDataAccessor<Float>[] SAP = new EntityDataAccessor[3];

    static {
        for (int i = 0; i < 3; i++) {
            HEALTH[i] = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.FLOAT);
            MAX[i] = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.FLOAT);
            CHAIN_ACTION[i] = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.INT);
            CHAIN_START[i] = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.LONG);
            CHAIN_AIM[i] = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.FLOAT);
            HURT_START[i] = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.LONG);
            DEATH_START[i] = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.LONG);
            SAP[i] = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.FLOAT);
            CHAIN_STRETCH[i] = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.FLOAT);
            CHAIN_PITCH[i] = SynchedEntityData.defineId(PlantEmpressEntity.class, EntityDataSerializers.FLOAT);
        }
    }

    private final PlantEmpressPart[] parts;
    private final PlantEmpressPart[] heads = new PlantEmpressPart[3];
    private final EmpressBrain brain = new EmpressBrain(this);

    private Matrix4f[] world;
    private final long[] headInvulnerableUntil = new long[3];
    private final float[] lastHeadDamage = new float[3];
    private final long[] lastSapGain = new long[3];
    private final long[] staggerImmuneUntil = new long[3];
    private final long[] nextReflex = new long[3];
    private long venomUntil;
    private long lastFighterSeen;
    private boolean desperationDone;
    private int previewChain = -1;
    private float previewAim;
    private float previewStretch = 1f;
    private float previewPitch;
    private Vec3 burrowFrom = Vec3.ZERO;
    private Vec3 burrowTo = Vec3.ZERO;
    private boolean spawned;
    private int finalDeathTicks;

    public final List<float[]> clientShakes = new ArrayList<>();

    public PlantEmpressEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        List<PlantEmpressPart> list = new ArrayList<>();
        for (int chain = 0; chain < 3; chain++) {
            float size = chain == 0 ? 3.0f : 2.2f;
            heads[chain] = new PlantEmpressPart(this, chain, EmpressRig.HEAD_BONE[chain], size, size * 0.5f);
            list.add(heads[chain]);
        }
        for (int[] segments : EmpressRig.SEGMENTS) {
            for (int bone : segments) list.add(new PlantEmpressPart(this, PlantEmpressPart.STEM, bone, 1.5f, 1.8f));
        }
        this.parts = list.toArray(new PlantEmpressPart[0]);
        this.setId(ENTITY_COUNTER.getAndAdd(this.parts.length + 1) + 1);
        this.noCulling = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAIN_HEALTH + SIDE_HEALTH * 2)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.ATTACK_DAMAGE, 16.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ARMOR, 0.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PHASE, PHASE_SPAWN);
        builder.define(PHASE_START, -1L);
        builder.define(IDLE_EPOCH, 0L);
        builder.define(RAGE_START, -1L);
        builder.define(WALL_START, -1L);
        builder.define(BODY_ACTION, ACTION_NONE);
        builder.define(BODY_START, 0L);
        builder.define(BODY_AIM, 0f);
        builder.define(REFLEXES, 0);
        builder.define(STEM_ACTION, ACTION_NONE);
        builder.define(STEM_START, 0L);
        builder.define(HOME, BlockPos.ZERO);
        for (int i = 0; i < 3; i++) {
            float max = i == 0 ? MAIN_HEALTH : SIDE_HEALTH;
            builder.define(HEALTH[i], max);
            builder.define(MAX[i], max);
            builder.define(CHAIN_ACTION[i], ACTION_NONE);
            builder.define(CHAIN_START[i], 0L);
            builder.define(CHAIN_AIM[i], 0f);
            builder.define(HURT_START[i], -100L);
            builder.define(DEATH_START[i], -1L);
            builder.define(SAP[i], 0f);
            builder.define(CHAIN_STRETCH[i], 1f);
            builder.define(CHAIN_PITCH[i], 0f);
        }
    }

    public int getPhase() { return this.entityData.get(PHASE); }
    public long getPhaseStart() { return this.entityData.get(PHASE_START); }
    public long getIdleEpoch() { return this.entityData.get(IDLE_EPOCH); }
    public long getRageStart() { return this.entityData.get(RAGE_START); }
    public long getWallStart() { return this.entityData.get(WALL_START); }
    public int getBodyAction() { return this.entityData.get(BODY_ACTION); }
    public long getBodyStart() { return this.entityData.get(BODY_START); }
    public float getBodyAim() { return this.entityData.get(BODY_AIM); }
    public int getChainAction(int chain) { return this.entityData.get(CHAIN_ACTION[chain]); }
    public long getChainStart(int chain) { return this.entityData.get(CHAIN_START[chain]); }
    public float getChainAim(int chain) { return chain == previewChain ? previewAim : this.entityData.get(CHAIN_AIM[chain]); }
    public float getChainStretch(int chain) { return chain == previewChain ? previewStretch : this.entityData.get(CHAIN_STRETCH[chain]); }
    public float getChainPitch(int chain) { return chain == previewChain ? previewPitch : this.entityData.get(CHAIN_PITCH[chain]); }
    public int getStemAction() { return this.entityData.get(STEM_ACTION); }
    public long getStemStart() { return this.entityData.get(STEM_START); }

    public Vec3 home() {
        BlockPos pos = this.entityData.get(HOME);
        return pos.equals(BlockPos.ZERO) ? position() : new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
    }
    public long getHurtStart(int chain) { return this.entityData.get(HURT_START[chain]); }
    public long getChainDeathStart(int chain) { return this.entityData.get(DEATH_START[chain]); }
    public float getHeadHealth(int chain) { return this.entityData.get(HEALTH[chain]); }
    public float getHeadMax(int chain) { return this.entityData.get(MAX[chain]); }
    public float getSap(int chain) { return this.entityData.get(SAP[chain]); }
    public int getReflexes(int chain) { return (this.entityData.get(REFLEXES) >> (chain * 2)) & 3; }

    public float getHeadFraction(int chain) {
        float max = getHeadMax(chain);
        return max <= 0f ? 0f : Mth.clamp(getHeadHealth(chain) / max, 0f, 1f);
    }

    public boolean isChainDead(int chain) {
        return chain > 0 && getChainDeathStart(chain) >= 0;
    }

    public boolean isSealed() {
        return !isChainDead(1) || !isChainDead(2);
    }

    public boolean isRaging() {
        return getRageStart() >= 0;
    }

    public int stage() {
        int dead = (isChainDead(1) ? 1 : 0) + (isChainDead(2) ? 1 : 0);
        return dead + 1;
    }

    public long now() {
        return this.level().getGameTime();
    }

    public void setPhase(int phase) {
        this.entityData.set(PHASE, phase);
        this.entityData.set(PHASE_START, now());
    }

    public void setBodyAction(@Nullable EmpressAnim anim, float aim) {
        this.entityData.set(BODY_ACTION, anim == null ? ACTION_NONE : anim.ordinal());
        this.entityData.set(BODY_START, now());
        this.entityData.set(BODY_AIM, aim);
    }

    public void delayBody(int ticks) {
        this.entityData.set(BODY_START, getBodyStart() + ticks);
    }

    public void setBodyAim(float aim) {
        this.entityData.set(BODY_AIM, aim);
    }

    public void setChainAction(int chain, int action, float aim) {
        this.entityData.set(CHAIN_ACTION[chain], action);
        this.entityData.set(CHAIN_START[chain], now());
        this.entityData.set(CHAIN_AIM[chain], aim);
        this.entityData.set(CHAIN_STRETCH[chain], 1f);
        this.entityData.set(CHAIN_PITCH[chain], 0f);
    }

    public void setChainReach(int chain, float aim, float stretch, float pitch) {
        this.entityData.set(CHAIN_AIM[chain], aim);
        this.entityData.set(CHAIN_STRETCH[chain], stretch);
        this.entityData.set(CHAIN_PITCH[chain], pitch);
    }

    public Matrix4f[] previewReach(int chain, float aim, float stretch, float pitch, float ticksAhead) {
        previewChain = chain;
        previewAim = aim;
        previewStretch = stretch;
        previewPitch = pitch;
        try {
            return predictWorld(ticksAhead);
        } finally {
            previewChain = -1;
        }
    }

    public void setStemAction(@Nullable EmpressAnim anim) {
        this.entityData.set(STEM_ACTION, anim == null ? ACTION_NONE : anim.ordinal());
        this.entityData.set(STEM_START, now());
    }

    public void setChainAim(int chain, float aim) {
        this.entityData.set(CHAIN_AIM[chain], aim);
    }

    public void delayChain(int chain, int ticks) {
        this.entityData.set(CHAIN_START[chain], getChainStart(chain) + ticks);
    }

    public boolean isBodyBusy() {
        EmpressAnim anim = EmpressAnim.byId(getBodyAction());
        return anim != null && now() - getBodyStart() < EmpressRig.get().lengthTicks(anim);
    }

    public float bodyTime() {
        return now() - getBodyStart();
    }

    public boolean isChainBusy(int chain) {
        int action = getChainAction(chain);
        long dt = now() - getChainStart(chain);
        if (action == ACTION_DODGE) return dt < EmpressPose.DODGE_TICKS;
        EmpressAnim anim = EmpressAnim.byId(action);
        return anim != null && dt < EmpressRig.get().lengthTicks(anim);
    }

    public EmpressAnim chainAnim(int chain) {
        return isChainBusy(chain) ? EmpressAnim.byId(getChainAction(chain)) : null;
    }

    public float chainTime(int chain) {
        return now() - getChainStart(chain);
    }

    public boolean isStaggered(int chain) {
        EmpressAnim anim = chainAnim(chain);
        if (anim != EmpressAnim.stagger(chain)) return false;
        float t = chainTime(chain);
        return t >= EmpressTimings.STAGGER_DOWN_START - 4 && t < EmpressTimings.STAGGER_DOWN_END;
    }

    public boolean isPlanted(int chain) {
        if (chain > 0 && chainAnim(chain) == EmpressAnim.lunge(chain)) {
            float t = chainTime(chain);
            return t >= EmpressTimings.LUNGE_IMPACT && t < EmpressTimings.LUNGE_PLANT_END;
        }
        if (chain == 0 && EmpressAnim.byId(getBodyAction()) == EmpressAnim.PLUNGE && isBodyBusy()) {
            float t = bodyTime();
            return t >= EmpressTimings.PLUNGE_IMPACT && t < EmpressTimings.PLUNGE_PLANT_END;
        }
        return false;
    }

    public boolean isHeadVulnerable(int chain) {
        if (isChainDead(chain)) return false;
        if (chain == 0 && isSealed()) return false;
        int phase = getPhase();
        return phase == PHASE_FIGHT || (phase == PHASE_DESPERATION && chain != 0);
    }

    public PlantEmpressPart head(int chain) {
        return heads[chain];
    }

    public Matrix4f[] world() {
        if (this.world == null) updateParts();
        return this.world;
    }

    public Matrix4f[] predictWorld(float ticksAhead) {
        EmpressRig.Pose pose = EmpressPose.evaluate(EmpressPose.compose(this, ticksAhead));
        return EmpressRig.get().solve(pose, EmpressRig.entityMatrix(getX(), getY(), getZ(), this.yBodyRot));
    }

    public Matrix4f entityMatrix() {
        return EmpressRig.entityMatrix(getX(), getY(), getZ(), this.yBodyRot);
    }

    public float aimFor(int bone, Vec3 target) {
        EmpressRig rig = EmpressRig.get();
        EmpressRig.Pose rest = new EmpressRig.Pose();
        rig.reset(rest);
        Vector3f local = rig.toModelSpace(entityMatrix(), bone, rest, target);
        return EmpressRig.yawToward(local);
    }

    public EmpressBrain brain() {
        return brain;
    }

    @Override
    public double arenaRadius() {
        return ARENA_RADIUS;
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return this.parts;
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        if (this.parts == null) return;
        for (int i = 0; i < this.parts.length; i++) this.parts[i].setId(id + i + 1);
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this) {
            @Override
            public void clientTick() {
            }
        };
    }

    @Override
    protected float tickHeadTurn(float yRot, float animStep) {
        this.yBodyRot = this.getYRot();
        return animStep;
    }

    @Override
    public int getMaxHeadYRot() {
        return 30;
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(0, Math.min(0, this.getDeltaMovement().y), 0);
        updateParts();
        if (this.level() instanceof ServerLevel serverLevel) {
            serverTick(serverLevel);
        } else {
            clientTick();
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.yBodyRot = this.getYRot();
    }

    private void updateParts() {
        EmpressRig rig = EmpressRig.get();
        this.world = rig.solve(EmpressPose.evaluate(EmpressPose.compose(this, 0f)), entityMatrix());
        for (PlantEmpressPart part : parts) {
            if (part.isHead()) {
                part.fit(rig.skullBox(this.world, part.chain));
            } else {
                part.place(rig.segmentMiddle(this.world, part.bone));
            }
        }
    }

    private void serverTick(ServerLevel level) {
        if (getPhaseStart() < 0) beginSpawn();
        if (this.entityData.get(HOME).equals(BlockPos.ZERO)) this.entityData.set(HOME, blockPosition());
        if (getPhase() == PHASE_DYING) return;

        long now = now();
        int phase = getPhase();
        long elapsed = now - getPhaseStart();

        switch (phase) {
            case PHASE_SPAWN -> tickSpawn(level, (int) elapsed);
            case PHASE_AWAKEN -> tickAwaken(level, (int) elapsed);
            case PHASE_TRANSITION -> tickTransition(level, (int) elapsed);
            case PHASE_DESPERATION -> {
                brain.tickDesperation(level, (int) elapsed);
                if (elapsed >= EmpressTimings.DESPERATION_TICKS) {
                    setPhase(PHASE_FIGHT);
                    stagger(0, true);
                }
            }
            case PHASE_BURROW -> tickBurrow(level, (int) elapsed);
            case PHASE_DORMANT -> {
                if (!fighters(AWAKEN_RADIUS).isEmpty()) beginAwaken();
            }
            default -> {
            }
        }

        if (phase == PHASE_FIGHT || phase == PHASE_TRANSITION || phase == PHASE_DESPERATION || phase == PHASE_BURROW) {
            brain.tickHazards(level);
            if (phase == PHASE_FIGHT) brain.tick(level);
            tickLeash(level);
            tickSap();
            tickReflexes();
        }
        faceTarget();
        this.setHealth(Math.max(0.01f, getHeadHealth(0) + getHeadHealth(1) + getHeadHealth(2)));
        decayThreat(0.995f);
    }

    private void clientTick() {
        for (int chain = 0; chain < 3; chain++) {
            if (isChainDead(chain)) continue;
            int action = getChainAction(chain);
            EmpressAnim anim = chain == 0 && isBodyBusy() ? EmpressAnim.byId(getBodyAction()) : EmpressAnim.byId(action);
            float t = chain == 0 && isBodyBusy() ? bodyTime() : chainTime(chain);
            if (anim == EmpressAnim.TRIPLE_BITE || (isBodyBusy() && EmpressAnim.byId(getBodyAction()) == EmpressAnim.TRIPLE_BITE)) {
                anim = EmpressAnim.TRIPLE_BITE;
                t = bodyTime();
            }
            if (EmpressTimings.inCounterWindow(anim, chain, t) && (chain != 0 || !isSealed())) {
                Vec3 mouth = EmpressRig.get().mouthPoint(world(), chain);
                for (int i = 0; i < 3; i++) {
                    this.level().addParticle(ParticleTypes.END_ROD, mouth.x + (random.nextDouble() - 0.5) * 0.6,
                            mouth.y + (random.nextDouble() - 0.5) * 0.6, mouth.z + (random.nextDouble() - 0.5) * 0.6, 0, 0.02, 0);
                }
                this.level().addParticle(ParticleTypes.FLASH, mouth.x, mouth.y, mouth.z, 0, 0, 0);
            }
            int reflexes = getReflexes(chain);
            if (reflexes > 0 && getPhase() == PHASE_FIGHT && this.tickCount % Math.max(4, 14 - reflexes * 4) == 0) {
                Vec3 eye = EmpressRig.get().skullCenter(world(), chain);
                this.level().addParticle(new DustParticleOptions(new Vector3f(1f, 0.85f, 0.3f), 1.1f),
                        eye.x + (random.nextDouble() - 0.5) * 0.8, eye.y + 0.4, eye.z + (random.nextDouble() - 0.5) * 0.8, 0, 0, 0);
            }
            if (isStaggered(chain) && this.tickCount % 3 == 0) {
                Vec3 eye = EmpressRig.get().skullCenter(world(), chain);
                this.level().addParticle(ParticleTypes.FALLING_SPORE_BLOSSOM, eye.x + (random.nextDouble() - 0.5),
                        eye.y + 0.6, eye.z + (random.nextDouble() - 0.5), 0, 0, 0);
            }
        }
        clientShakes.removeIf(shake -> this.tickCount - shake[0] > shake[2]);
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_BURST -> clientShakes.add(new float[]{this.tickCount, 14f, 34f});
            case EVENT_ROAR -> clientShakes.add(new float[]{this.tickCount, 7f, 40f, 1f});
            case EVENT_SLAM -> clientShakes.add(new float[]{this.tickCount, 9f, 18f});
            case EVENT_TREMOR -> clientShakes.add(new float[]{this.tickCount, 3f, 14f});
            case EVENT_HEAD_DEATH -> clientShakes.add(new float[]{this.tickCount, 6f, 30f, 1f});
            case EVENT_STAGGER -> clientShakes.add(new float[]{this.tickCount, 4f, 12f});
            case EVENT_DESPERATION -> clientShakes.add(new float[]{this.tickCount, 5f, 60f, 1f});
            default -> super.handleEntityEvent(id);
        }
    }

    private void beginSpawn() {
        this.entityData.set(HOME, blockPosition());
        setPhase(PHASE_SPAWN);
        this.entityData.set(IDLE_EPOCH, now());
        this.spawned = true;
    }

    private void tickSpawn(ServerLevel level, int t) {
        BlockState ground = groundState(level);
        if (t < EmpressTimings.SPAWN_BURST - 20 && t % 3 == 0) {
            float radius = 2f + Math.min(10f, t / 11f);
            int count = 6 + t / 10;
            for (int i = 0; i < count; i++) {
                double angle = random.nextDouble() * Math.PI * 2;
                double r = radius * (0.5 + random.nextDouble() * 0.5);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), getX() + Math.cos(angle) * r,
                        getY() + 0.1, getZ() + Math.sin(angle) * r, 2, 0.2, 0.05, 0.2, 0.15);
            }
            if (t > 40) {
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, getX(), getY() + 0.5, getZ(), 4, 1.2, 0.2, 1.2, 0.01);
            }
        }
        if (t % 20 == 0 && t < EmpressTimings.SPAWN_BURST - 20) {
            playBossSound(OWSounds.PLANT_EMPRESS_RUMBLE.get(), 1.4f + t / 100f, 0.6f + t / 400f);
            broadcastEvent(EVENT_TREMOR);
        }
        if (t == 24 || t == 44 || t == 66 || t == 84 || t == 100 || t == 112) {
            double angle = random.nextDouble() * Math.PI * 2;
            double r = 5 + random.nextDouble() * 6;
            brain.spawnCosmeticRoot(level, new Vec3(getX() + Math.cos(angle) * r, getY(), getZ() + Math.sin(angle) * r));
        }
        if (t == 60 || t == 80 || t == 96 || t == 108 || t == 118 || t == 126) {
            playBossSound(OWSounds.PLANT_EMPRESS_HEARTBEAT.get(), 2.5f, 0.8f);
        }
        if (t == 136) playBossSound(OWSounds.PLANT_EMPRESS_BREATH.get(), 2.5f, 0.6f);
        if (t == EmpressTimings.SPAWN_BURST) {
            int players = fighters(48).size();
            scaleHealth(players);
            playBossSound(OWSounds.PLANT_EMPRESS_BURST.get(), 4f, 0.7f);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), getX(), getY() + 0.5, getZ(), 260, 2.2, 1.2, 2.2, 0.5);
            level.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 1, getZ(), 6, 1.5, 0.8, 1.5, 0);
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, getX(), getY() + 4, getZ(), 120, 3, 3, 3, 0.05);
            broadcastEvent(EVENT_BURST);
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(9), e -> e != this)) {
                Vec3 away = horizontal(entity.position().subtract(position()));
                if (away.lengthSqr() < 0.01) away = new Vec3(1, 0, 0);
                away = away.normalize();
                entity.setDeltaMovement(away.x * 1.1, 0.55, away.z * 1.1);
                entity.hurtMarked = true;
            }
        }
        if (t == 164 || t == 178 || t == 192) playBossSound(OWSounds.PLANT_EMPRESS_UNFURL.get(), 2.0f, 0.7f + (t - 164) / 80f);
        if (t == EmpressTimings.SPAWN_ROAR) {
            playBossSound(OWSounds.PLANT_EMPRESS_ROAR.get(), 5f, 0.85f);
            broadcastEvent(EVENT_ROAR);
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, getX(), getY() + 8, getZ(), 80, 4, 2, 4, 0.08);
        }
        if (t >= 150 && t <= EmpressTimings.SPAWN_ROAR) faceNearest(8f);
        if (t >= EmpressTimings.SPAWN_TICKS) {
            this.entityData.set(IDLE_EPOCH, now());
            setPhase(PHASE_FIGHT);
            lastFighterSeen = now();
        }
    }

    private void beginAwaken() {
        int players = fighters(48).size();
        scaleHealth(players);
        setPhase(PHASE_AWAKEN);
        setBodyAction(EmpressAnim.STRETCH, 0f);
        lastFighterSeen = now();
    }

    private void tickAwaken(ServerLevel level, int t) {
        if (t == EmpressTimings.AWAKEN_ROAR) {
            playBossSound(OWSounds.PLANT_EMPRESS_ROAR.get(), 5f, 0.9f);
            broadcastEvent(EVENT_ROAR);
        }
        faceNearest(6f);
        if (t >= EmpressTimings.AWAKEN_TICKS) setPhase(PHASE_FIGHT);
    }

    private void tickTransition(ServerLevel level, int t) {
        if (t == EmpressTimings.TRANSITION_SHOCKWAVE) {
            playBossSound(OWSounds.PLANT_EMPRESS_ROAR.get(), 5f, 0.7f);
            broadcastEvent(EVENT_HEAD_DEATH);
            brain.transitionShockwave(level);
        }
        int length = isRaging() ? EmpressTimings.RAGE_TRANSITION_TICKS : EmpressTimings.HEAD_TRANSITION_TICKS;
        if (isRaging() && t == 40) {
            this.entityData.set(WALL_START, now());
            playBossSound(OWSounds.PLANT_EMPRESS_ROOTS.get(), 4f, 0.5f);
            broadcastEvent(EVENT_SLAM);
        }
        if (t >= length) setPhase(PHASE_FIGHT);
    }

    public boolean canBurrow() {
        return getPhase() == PHASE_FIGHT && !isBodyBusy() && !isStaggered(0) && !isStaggered(1) && !isStaggered(2);
    }

    public void beginBurrow(Vec3 destination) {
        brain.clear();
        for (int chain = 0; chain < 3; chain++) {
            if (!isChainDead(chain) || isChainBusy(chain)) setChainAction(chain, ACTION_NONE, 0f);
        }
        setBodyAction(null, 0f);
        setStemAction(EmpressAnim.STEMS_DIG);
        burrowFrom = position();
        burrowTo = destination;
        setPhase(PHASE_BURROW);
        playBossSound(OWSounds.PLANT_EMPRESS_ROAR.get(), 3.5f, 1.1f);
        playBossSound(OWSounds.PLANT_EMPRESS_RUMBLE.get(), 3f, 0.6f);
    }

    private void tickBurrow(ServerLevel level, int t) {
        BlockState ground = groundState(level);
        int down = EmpressTimings.BURROW_DOWN;
        int travel = EmpressTimings.BURROW_TRAVEL;
        if (t < down) {
            if (t % 2 == 0) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), getX(), getY() + 0.3, getZ(), 30, 1.8, 0.4, 1.8, 0.25);
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY() + 0.2, getZ(), 3, 1.5, 0.1, 1.5, 0.01);
            }
            if (t % 6 == 0) broadcastEvent(EVENT_TREMOR);
            if (t == EmpressTimings.BURROW_HIDE - 2) {
                playBossSound(OWSounds.PLANT_EMPRESS_BURST.get(), 3f, 1.3f);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), getX(), getY() + 0.5, getZ(), 160, 2.2, 0.9, 2.2, 0.4);
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY() + 0.4, getZ(), 20, 1.8, 0.3, 1.8, 0.02);
            }
        } else if (t == down) {
            EmpressFx.decal(level, EmpressFx.BURROW, burrowTo, 4.8, EmpressTimings.BURROW_BURST - down, random.nextFloat() * 360f);
            this.moveTo(burrowTo.x, burrowTo.y, burrowTo.z, getYRot(), 0f);
            this.yBodyRot = getYRot();
            updateParts();
        }
        if (t >= down && t < down + travel) {
            float f = (t - down) / (float) travel;
            Vec3 at = burrowFrom.lerp(burrowTo, f);
            double y = EmpressDamage.groundY(level, at.x, at.z, at.y);
            BlockState under = level.getBlockState(BlockPos.containing(at.x, y - 0.5, at.z));
            if (under.isAir()) under = ground;
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, under), at.x, y + 0.2, at.z, 24, 0.9, 0.3, 0.9, 0.25);
            level.sendParticles(EmpressDamage.EARTH, at.x, y + 0.2, at.z, 6, 0.7, 0.1, 0.7, 0);
            if (t % 3 == 0) {
                Vec3 heading = burrowTo.subtract(burrowFrom);
                EmpressFx.decal(level, EmpressFx.CHEVRON, new Vec3(at.x, y, at.z), 0.9, 14, (float) (Math.atan2(heading.x, -heading.z) * Mth.RAD_TO_DEG));
            }
            if (t % 5 == 0) {
                level.playSound(null, at.x, y, at.z, OWSounds.PLANT_EMPRESS_RUMBLE.get(), SoundSource.HOSTILE, 2f, 0.7f + f * 0.4f);
                broadcastEvent(EVENT_TREMOR);
            }
            if (t % 2 == 0 && f > 0.5f) {
                EmpressDamage.ring(level, burrowTo, 4.5, EmpressDamage.EARTH, 0.5);
            }
        }
        int burst = EmpressTimings.BURROW_BURST;
        if (t >= down + travel && t < burst) {
            float f = (t - down - travel) / (float) (burst - down - travel);
            if (t % 2 == 0) {
                EmpressDamage.ring(level, burrowTo, 4.5, f > 0.4f ? EmpressDamage.DANGER : EmpressDamage.EARTH, 0.45);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), burrowTo.x, burrowTo.y + 0.1, burrowTo.z,
                        (int) (14 + f * 30), 2.6, 0.05, 2.6, 0.15);
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, burrowTo.x, burrowTo.y + 0.4, burrowTo.z, 4, 1.8, 0.2, 1.8, 0.01);
            }
            if (t % 4 == 0) {
                broadcastEvent(EVENT_TREMOR);
                playBossSound(OWSounds.PLANT_EMPRESS_RUMBLE.get(), 2.5f, 0.6f + f * 0.5f);
            }
            if (t == down + travel + 6) playBossSound(OWSounds.PLANT_EMPRESS_HEARTBEAT.get(), 2.5f, 0.9f);
        }
        if (t == burst) {

            playBossSound(OWSounds.PLANT_EMPRESS_BURST.get(), 4f, 0.8f);
            playBossSound(OWSounds.PLANT_EMPRESS_ROAR.get(), 4f, 1.0f);
            broadcastEvent(EVENT_SLAM);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), getX(), getY() + 0.5, getZ(), 220, 2.4, 1.2, 2.4, 0.5);
            level.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 1, getZ(), 5, 1.5, 0.6, 1.5, 0);
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, getX(), getY() + 4, getZ(), 90, 3, 3, 3, 0.05);
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(5, 4, 5), e -> EmpressDamage.canHit(e, this))) {
                double d = Math.sqrt(entity.distanceToSqr(getX(), entity.getY(), getZ()));
                if (d > 4.7) continue;
                EmpressDamage.strike(this, entity, EmpressDamage.roots(this, this), EmpressBalance.DAMAGE_BURROW_ERUPTION, position(), 1.1, 0.8, false);
            }
        }
        if (t >= EmpressTimings.BURROW_TICKS) setPhase(PHASE_FIGHT);
    }

    private void tickLeash(ServerLevel level) {
        long now = now();
        if (!fighters(LEASH_RADIUS).isEmpty()) {
            lastFighterSeen = now;
            return;
        }
        long idle = now - lastFighterSeen;
        if (idle > REGEN_DELAY && idle % 20 == 0) {
            for (int chain = 0; chain < 3; chain++) {
                if (isChainDead(chain)) continue;
                setHeadHealth(chain, Math.min(getHeadMax(chain), getHeadHealth(chain) + getHeadMax(chain) * 0.02f));
            }
        }
        if (idle > RESET_DELAY) resetFight(level);
    }

    public void resetFight(ServerLevel level) {
        brain.reset(level);
        for (int chain = 0; chain < 3; chain++) {
            this.entityData.set(DEATH_START[chain], -1L);
            this.entityData.set(SAP[chain], 0f);
            setHeadHealth(chain, getHeadMax(chain));
            setChainAction(chain, ACTION_NONE, 0f);
        }
        this.entityData.set(RAGE_START, -1L);
        this.entityData.set(WALL_START, -1L);
        this.entityData.set(REFLEXES, 0);
        setBodyAction(null, 0f);
        desperationDone = false;
        clearCombat();
        setPhase(PHASE_DORMANT);
    }

    private void scaleHealth(int players) {
        float factor = 1f + 0.5f * Math.max(0, players - 1);
        for (int chain = 0; chain < 3; chain++) {
            float max = (chain == 0 ? MAIN_HEALTH : SIDE_HEALTH) * factor;
            this.entityData.set(MAX[chain], max);
            if (!isChainDead(chain)) this.entityData.set(HEALTH[chain], max);
        }
        float total = getHeadMax(0) + getHeadMax(1) + getHeadMax(2);
        var attribute = this.getAttribute(Attributes.MAX_HEALTH);
        if (attribute != null) attribute.setBaseValue(total);
    }

    private void setHeadHealth(int chain, float value) {
        this.entityData.set(HEALTH[chain], Math.max(0f, value));
    }

    private void tickSap() {
        long now = now();
        for (int chain = 0; chain < 3; chain++) {
            float sap = getSap(chain);
            if (sap > 0f && now - lastSapGain[chain] > SAP_DECAY_DELAY && !isStaggered(chain)) {
                this.entityData.set(SAP[chain], Math.max(0f, sap - SAP_DECAY_PER_TICK));
            }
        }
    }

    private int maxReflexes(int chain) {
        if (isChainDead(chain) || (chain == 0 && isSealed())) return 0;
        float f = getHeadFraction(chain);
        if (f < 0.2f) return 3;
        if (f < 0.36f) return 2;
        if (f < 0.55f) return 1;
        return 0;
    }

    private void tickReflexes() {
        long now = now();
        for (int chain = 0; chain < 3; chain++) {
            int max = maxReflexes(chain);
            int current = getReflexes(chain);
            if (current > max) {
                setReflexes(chain, max);
            } else if (current < max && now >= nextReflex[chain]) {
                setReflexes(chain, current + 1);
                nextReflex[chain] = now + REFLEX_RECHARGE;
            }
        }
    }

    private void setReflexes(int chain, int value) {
        int packed = this.entityData.get(REFLEXES);
        packed &= ~(3 << (chain * 2));
        packed |= (Mth.clamp(value, 0, 3) << (chain * 2));
        this.entityData.set(REFLEXES, packed);
    }

    public boolean tryDodge(int chain, float direction) {
        if (getReflexes(chain) <= 0 || isChainBusy(chain) || isBodyBusy()) return false;
        setReflexes(chain, getReflexes(chain) - 1);
        nextReflex[chain] = Math.max(nextReflex[chain], now() + REFLEX_RECHARGE);
        setChainAction(chain, ACTION_DODGE, direction);
        playBossSound(OWSounds.PLANT_EMPRESS_DODGE.get(), 1.2f, 1.1f + random.nextFloat() * 0.2f);
        return true;
    }

    private void faceTarget() {
        LivingEntity target = brain.focus();
        if (target == null || getPhase() == PHASE_SPAWN || getPhase() == PHASE_BURROW) return;
        Vec3 eye = position().add(0, 8, 0);
        Vec3 to = target.getEyePosition().subtract(eye);
        float yaw = (float) (Mth.atan2(to.z, to.x) * Mth.RAD_TO_DEG) - 90f;
        float pitch = (float) -(Mth.atan2(to.y, to.horizontalDistance()) * Mth.RAD_TO_DEG);
        if (!isBodyBusy() && !brain.locksBody()) {
            float body = Mth.approachDegrees(getYRot(), yaw, 1.6f);
            this.setYRot(body);
            this.yBodyRot = body;
        }
        this.setYHeadRot(Mth.approachDegrees(this.getYHeadRot(), yaw, 6f));
        this.setXRot(Mth.approach(this.getXRot(), Mth.clamp(pitch, -30f, 30f), 4f));
    }

    private void faceNearest(float speed) {
        Player player = this.level().getNearestPlayer(this, 48);
        if (player == null) return;
        Vec3 to = player.position().subtract(position());
        float yaw = (float) (Mth.atan2(to.z, to.x) * Mth.RAD_TO_DEG) - 90f;
        float body = Mth.approachDegrees(getYRot(), yaw, speed);
        this.setYRot(body);
        this.yBodyRot = body;
        this.setYHeadRot(body);
    }

    public void broadcastEvent(byte event) {
        this.level().broadcastEntityEvent(this, event);
    }

    public void playBossSound(SoundEvent sound, float volume, float pitch) {
        this.level().playSound(null, getX(), getY() + 4, getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }

    public BlockState groundState(Level level) {
        BlockState state = level.getBlockState(blockPosition().below());
        return state.isAir() ? Blocks.DIRT.defaultBlockState() : state;
    }

    public boolean hurtPart(PlantEmpressPart part, DamageSource source, float amount) {
        if (this.level().isClientSide()) return false;
        Entity attacker = source.getEntity();
        if (attacker == this || attacker instanceof PlantEmpressPart) return false;
        if (isKillCommand(source)) return this.hurt(source, amount);
        if (!part.isHead()) {
            parry(part, source, false);
            return false;
        }
        int chain = part.chain;
        if (getPhase() == PHASE_DYING || isChainDead(chain)) {
            parry(part, source, false);
            return false;
        }
        if (!isHeadVulnerable(chain)) {
            parry(part, source, chain == 0 && isSealed());
            return false;
        }
        long now = now();
        float damage = amount;
        boolean melee = source.getDirectEntity() != null && source.getDirectEntity() == attacker && !(source.getDirectEntity() instanceof Projectile);
        if (source.getDirectEntity() instanceof VenomousArrow) venomUntil = now + 200 + random.nextInt(100);
        if (now < venomUntil) damage *= EmpressBalance.MULTIPLIER_VENOM;
        boolean staggered = isStaggered(chain);
        boolean planted = isPlanted(chain);
        boolean counter = inCounter(chain);
        if (staggered) damage *= EmpressBalance.MULTIPLIER_STAGGERED;
        else if (planted) damage *= EmpressBalance.MULTIPLIER_PLANTED;
        if (counter) damage *= EmpressBalance.MULTIPLIER_COUNTER;

        if (now < headInvulnerableUntil[chain]) {
            if (damage <= lastHeadDamage[chain]) return false;
            float extra = damage - lastHeadDamage[chain];
            lastHeadDamage[chain] = damage;
            damage = extra;
        } else {
            lastHeadDamage[chain] = damage;
            headInvulnerableUntil[chain] = now + HEAD_INVULNERABLE_TICKS;
        }

        float health = getHeadHealth(chain) - damage;
        setHeadHealth(chain, health);
        this.entityData.set(HURT_START[chain], now);
        addThreat(attacker, damage);
        this.level().broadcastDamageEvent(this, source);
        if (this.level() instanceof ServerLevel serverLevel) {
            Vec3 c = part.getBoundingBox().getCenter();
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MOSS_BLOCK.defaultBlockState()),
                    c.x, c.y, c.z, 14, 0.5, 0.5, 0.5, 0.2);
            if (counter) {
                serverLevel.sendParticles(ParticleTypes.FLASH, c.x, c.y, c.z, 1, 0, 0, 0, 0);
                playBossSound(OWSounds.PLANT_EMPRESS_COUNTER.get(), 2f, 1.2f);
            }
        }

        if (health <= 0f) {
            killHead(chain, source);
            return true;
        }

        if (!staggered && now >= staggerImmuneUntil[chain]) {
            boolean critical = attacker instanceof Player player && melee && player.fallDistance > 0f && !player.onGround()
                    && !player.onClimbable() && !player.isInWater();
            float gain = damage / getHeadMax(chain) * EmpressBalance.SAP_GAIN_FACTOR * (melee ? EmpressBalance.SAP_MELEE_FACTOR : 1f)
                    * (critical ? EmpressBalance.SAP_CRITICAL_FACTOR : 1f);
            if (counter) gain += EmpressBalance.SAP_COUNTER_BONUS;
            if (planted) gain *= EmpressBalance.SAP_PLANTED_FACTOR;
            float sap = Math.min(1f, getSap(chain) + gain);
            this.entityData.set(SAP[chain], sap);
            lastSapGain[chain] = now;
            if (sap >= 1f) stagger(chain, false);
        }

        if (this.getPhase() == PHASE_FIGHT && chain == 0 && stage() == 3 && !desperationDone && getHeadFraction(0) <= 0.25f) {
            desperationDone = true;
            brain.clear();
            for (int c2 = 0; c2 < 3; c2++) if (!isChainDead(c2)) setChainAction(c2, ACTION_NONE, 0f);
            setPhase(PHASE_DESPERATION);
            setBodyAction(EmpressAnim.DESPERATION, 0f);
            broadcastEvent(EVENT_DESPERATION);
            playBossSound(OWSounds.PLANT_EMPRESS_ROAR.get(), 5f, 0.6f);
        }
        return true;
    }

    private boolean inCounter(int chain) {
        if (chain == 0 && isBodyBusy()) {
            return EmpressTimings.inCounterWindow(EmpressAnim.byId(getBodyAction()), 0, bodyTime());
        }
        if (isBodyBusy() && EmpressAnim.byId(getBodyAction()) == EmpressAnim.TRIPLE_BITE) {
            return EmpressTimings.inCounterWindow(EmpressAnim.TRIPLE_BITE, chain, bodyTime());
        }
        return EmpressTimings.inCounterWindow(chainAnim(chain), chain, chainTime(chain));
    }

    public void stagger(int chain, boolean forced) {
        long now = now();
        brain.cancelChain(chain);
        if (isBodyBusy()) {
            brain.cancelBody();
            setBodyAction(null, 0f);
        }
        setChainAction(chain, EmpressAnim.stagger(chain).ordinal(), 0f);
        this.entityData.set(SAP[chain], 0f);
        staggerImmuneUntil[chain] = now + EmpressRig.get().lengthTicks(EmpressAnim.stagger(chain)) + EmpressTimings.STAGGER_IMMUNITY;
        playBossSound(OWSounds.PLANT_EMPRESS_STAGGER.get(), 3f, forced ? 0.7f : 0.9f);
        broadcastEvent(EVENT_STAGGER);
    }

    public boolean collapse(int chain) {
        long now = now();
        if (isChainDead(chain) || isStaggered(chain) || now < staggerImmuneUntil[chain]) return false;
        EmpressAnim anim = EmpressAnim.stagger(chain);
        brain.cancelChain(chain);
        this.entityData.set(CHAIN_ACTION[chain], anim.ordinal());
        this.entityData.set(CHAIN_START[chain], now - EmpressTimings.STAGGER_COLLAPSE_SKIP);
        this.entityData.set(SAP[chain], 0f);
        staggerImmuneUntil[chain] = now + EmpressRig.get().lengthTicks(anim) - EmpressTimings.STAGGER_COLLAPSE_SKIP + EmpressTimings.STAGGER_IMMUNITY;
        playBossSound(OWSounds.PLANT_EMPRESS_STAGGER.get(), 3f, 0.8f);
        broadcastEvent(EVENT_STAGGER);
        return true;
    }

    private void killHead(int chain, DamageSource source) {
        setHeadHealth(chain, 0f);
        if (chain == 0) {
            finalDeath(source);
            return;
        }
        brain.clear();
        long now = now();
        this.entityData.set(DEATH_START[chain], now);
        this.entityData.set(SAP[chain], 0f);
        setChainAction(chain, ACTION_NONE, 0f);
        for (int other = 0; other < 3; other++) {
            if (other != chain && !isChainDead(other)) setChainAction(other, ACTION_NONE, 0f);
        }
        setBodyAction(EmpressAnim.painRoar(chain), 0f);
        if (isChainDead(1) && isChainDead(2)) this.entityData.set(RAGE_START, now + 30);
        setPhase(PHASE_TRANSITION);
        if (this.level() instanceof ServerLevel serverLevel) brain.onHeadDeath(serverLevel, chain);
        playBossSound(OWSounds.PLANT_EMPRESS_HEAD_DEATH.get(), 4f, 0.8f);
    }

    private void finalDeath(DamageSource source) {
        if (this.level() instanceof ServerLevel serverLevel) brain.reset(serverLevel);
        for (int chain = 0; chain < 3; chain++) if (!isChainDead(chain)) setChainAction(chain, ACTION_NONE, 0f);
        setBodyAction(null, 0f);
        this.entityData.set(WALL_START, -1L);
        setPhase(PHASE_DYING);
        this.setHealth(0f);
        playBossSound(OWSounds.PLANT_EMPRESS_DEATH.get(), 5f, 0.8f);
        broadcastEvent(EVENT_HEAD_DEATH);
        this.die(source);
    }

    private void parry(PlantEmpressPart part, DamageSource source, boolean sealed) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        Entity attacker = source.getEntity();
        engage(attacker);
        Vec3 c = part.getBoundingBox().getCenter();
        if (source.getDirectEntity() != null) c = source.getDirectEntity().position().add(0, source.getDirectEntity().getBbHeight() * 0.5, 0);
        if (sealed) {
            serverLevel.sendParticles(new DustParticleOptions(new Vector3f(0.55f, 0.95f, 0.35f), 1.4f), c.x, c.y, c.z, 12, 0.4, 0.4, 0.4, 0);
            this.level().playSound(null, c.x, c.y, c.z, OWSounds.PLANT_EMPRESS_SEALED.get(), SoundSource.HOSTILE, 1.4f, 1.0f + random.nextFloat() * 0.2f);
        } else {
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MANGROVE_ROOTS.defaultBlockState()), c.x, c.y, c.z, 8, 0.2, 0.2, 0.2, 0.1);
            this.level().playSound(null, c.x, c.y, c.z, OWSounds.PLANT_EMPRESS_PARRY.get(), SoundSource.HOSTILE, 1.2f, 0.8f + random.nextFloat() * 0.3f);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isKillCommand(source)) {
            if (getPhase() != PHASE_DYING) {
                setHeadHealth(1, 0);
                setHeadHealth(2, 0);
                finalDeath(source);
            }
            return true;
        }
        if (this.level().isClientSide()) return false;
        if (source.getEntity() instanceof LivingEntity attacker && source.getDirectEntity() == attacker && attacker != this) {
            parry(parts[parts.length - 1], source, false);
            engage(attacker);
        }
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (isKillCommand(source)) return false;
        return isEnvironmentalDamage(source) || super.isInvulnerableTo(source);
    }

    public float dissolveProgress(float partialTick) {
        if (getPhase() != PHASE_DYING) return 0f;
        float t = now() + partialTick - getPhaseStart();
        return Mth.clamp((t - EmpressTimings.DISSOLVE_START) / (EmpressTimings.DISSOLVE_END - EmpressTimings.DISSOLVE_START), 0f, 1f);
    }

    @Override
    protected void tickDeath() {
        ++this.finalDeathTicks;
        if (this.finalDeathTicks == 40 && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FALLING_SPORE_BLOSSOM, getX(), getY() + 6, getZ(), 120, 3, 4, 3, 0);
        }
        if (this.finalDeathTicks == EmpressTimings.DISSOLVE_START) {
            playBossSound(OWSounds.PLANT_EMPRESS_BREATH.get(), 3f, 0.5f);
        }
        float dissolve = dissolveProgress(0f);
        if (dissolve > 0f && dissolve < 1f && this.level() instanceof ServerLevel serverLevel) {
            Matrix4f[] bones = world();
            int count = 2 + (int) (Math.sin(dissolve * Math.PI) * 6);
            for (int i = 0; i < count; i++) {
                int bone = random.nextInt(EmpressRig.COUNT);
                Vec3 p = EmpressRig.origin(bones, bone);
                double x = p.x + (random.nextDouble() - 0.5) * 2.2;
                double y = p.y + (random.nextDouble() - 0.5) * 1.6;
                double z = p.z + (random.nextDouble() - 0.5) * 2.2;
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, random.nextInt(3) == 0
                        ? Blocks.AZALEA_LEAVES.defaultBlockState() : Blocks.MOSS_BLOCK.defaultBlockState()), x, y, z, 2, 0.2, 0.2, 0.2, 0.02);
                if (i % 2 == 0) serverLevel.sendParticles(ParticleTypes.WHITE_ASH, x, y, z, 4, 0.4, 0.4, 0.4, 0.01);
                if (i % 3 == 0) serverLevel.sendParticles(ParticleTypes.FALLING_SPORE_BLOSSOM, x, y, z, 1, 0.3, 0.2, 0.3, 0);
            }
            if (this.finalDeathTicks % 24 == 0) {
                this.level().playSound(null, getX(), getY() + 2, getZ(), SoundEvents.AZALEA_LEAVES_BREAK, SoundSource.HOSTILE, 1.6f, 0.5f + random.nextFloat() * 0.2f);
            }
        }

        if (this.finalDeathTicks >= EmpressTimings.FINAL_DEATH_TICKS && !this.level().isClientSide() && !this.isRemoved()) {
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MOSS_BLOCK.defaultBlockState()),
                        getX(), getY() + 2, getZ(), 200, 2, 2, 2, 0.2);
                serverLevel.sendParticles(ParticleTypes.POOF, getX(), getY() + 2, getZ(), 40, 2, 2, 2, 0.05);
            }
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public void die(DamageSource source) {
        if (getPhase() != PHASE_DYING) {
            finalDeath(source);
            return;
        }
        super.die(source);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.level() instanceof ServerLevel serverLevel) brain.reset(serverLevel);
        super.remove(reason);
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        return false;
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        return entity instanceof PlantEmpressEntity || entity instanceof PlantEmpressPart || super.isAlliedTo(entity);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(getWallStart() >= 0 ? 34 : 14, 14, getWallStart() >= 0 ? 34 : 14);
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource source) {
        return OWSounds.PLANT_EMPRESS_HURT.get();
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return getPhase() == PHASE_FIGHT || getPhase() == PHASE_DORMANT ? OWSounds.PLANT_EMPRESS_AMBIENT.get() : null;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected float getSoundVolume() {
        return 2.5f;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, data);
        this.setYRot(this.random.nextFloat() * 360f);
        this.yBodyRot = this.getYRot();
        return result;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Spawned", this.spawned || getPhase() != PHASE_SPAWN);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.getBoolean("Spawned")) {
            this.spawned = true;
            this.entityData.set(PHASE, PHASE_DORMANT);
            this.entityData.set(PHASE_START, 0L);
        }
    }

    public float introBarProgress(float partialTick) {
        float t = now() + partialTick - getPhaseStart();
        int phase = getPhase();
        if (phase == PHASE_SPAWN) {
            if (t < EmpressTimings.SPAWN_BAR_SHOW) return -1f;
            return Mth.clamp((t - EmpressTimings.SPAWN_BAR_FILL_START) / (EmpressTimings.SPAWN_BAR_FILL_END - EmpressTimings.SPAWN_BAR_FILL_START), 0f, 1f);
        }
        if (phase == PHASE_AWAKEN) {
            return Mth.clamp((t - EmpressTimings.AWAKEN_BAR_FILL_START) / (EmpressTimings.AWAKEN_BAR_FILL_END - EmpressTimings.AWAKEN_BAR_FILL_START), 0f, 1f);
        }
        if (phase == PHASE_DORMANT) return -1f;
        return 1f;
    }

    public float introBarAlpha(float partialTick) {
        float t = now() + partialTick - getPhaseStart();
        int phase = getPhase();
        if (phase == PHASE_SPAWN) return Mth.clamp((t - EmpressTimings.SPAWN_BAR_SHOW) / 20f, 0f, 1f);
        if (phase == PHASE_AWAKEN) return Mth.clamp(t / 15f, 0f, 1f);
        if (phase == PHASE_DORMANT) return 0f;
        return 1f;
    }

    public float wallRadius(float partialTick) {
        long start = getWallStart();
        if (start < 0) return -1f;
        float t = (now() + partialTick - start) / 60f;
        return Mth.lerp(EmpressPose.smooth(t), (float) LEASH_RADIUS, (float) ARENA_RADIUS);
    }

    public boolean isAboveGround() {
        int phase = getPhase();
        if (phase == PHASE_SPAWN) return now() - getPhaseStart() >= EmpressTimings.SPAWN_BURST;
        if (phase == PHASE_BURROW) {
            long t = now() - getPhaseStart();
            return t < EmpressTimings.BURROW_HIDE || t >= EmpressTimings.BURROW_BURST;
        }
        return true;
    }
}
