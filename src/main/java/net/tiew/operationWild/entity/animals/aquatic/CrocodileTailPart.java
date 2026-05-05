package net.tiew.operationWild.entity.animals.aquatic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class CrocodileTailPart extends Entity {

    private double velX = 0, velZ = 0;
    public float velY = 0f;
    private static final double GRAVITY  = 0.04;
    private static final double DAMPING  = 0.82;
    private static final double SEG_DIST = 1.25;
    private static final float  BODY_OFF = 0.75f;
    private static final float DAMPING_Y = 0.85f;

    private int parentMissingTicks = 0;
    private static final int MAX_MISSING_TICKS = 100; // 5 secondes de grâce

    private static final EntityDataAccessor<Integer> PARENT_ID =
            SynchedEntityData.defineId(CrocodileTailPart.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SEGMENT_INDEX =
            SynchedEntityData.defineId(CrocodileTailPart.class, EntityDataSerializers.INT);

    // Dimensions de base à scale=1 pour chaque segment (tail1, tail2, tail3)
    private static final float[] BASE_WIDTHS  = { 0.70f, 0.55f, 0.40f };
    private static final float[] BASE_HEIGHTS = { 0.55f, 0.50f, 0.35f };

    @Nullable private CrocodileEntity parent;

    // ─── Constructeur factory (réseau / EntityType) ───────────────────────
    public CrocodileTailPart(EntityType<CrocodileTailPart> type, Level level) {
        super(type, level);
    }

    // ─── Constructeur serveur ─────────────────────────────────────────────
    public CrocodileTailPart(EntityType<CrocodileTailPart> type, Level level,
                             CrocodileEntity parent, int segmentIndex) {
        super(type, level);
        this.parent = parent;
        this.entityData.set(PARENT_ID, parent.getId());
        this.entityData.set(SEGMENT_INDEX, segmentIndex);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(PARENT_ID, -1);
        builder.define(SEGMENT_INDEX, 0);
    }

    // ─── Cycle de vie ─────────────────────────────────────────────────────
    @Override
    public void tick() {
        super.tick();

        if (parent == null || parent.isRemoved()) {
            parent = null;
            int pid = this.entityData.get(PARENT_ID);
            Entity e = this.level().getEntity(pid);
            if (e instanceof CrocodileEntity croc) {
                parent = croc;
                parentMissingTicks = 0;
            } else {
                parentMissingTicks++;
                if (parentMissingTicks > MAX_MISSING_TICKS && !this.level().isClientSide()) {
                    this.discard();
                }
                return;
            }
        }

        if (level().isClientSide()) return;

        velY -= GRAVITY;
        velY *= DAMPING_Y;
        double newY = getY() + velY;

        int groundY = findGroundY((int) getX(), (int) getY() + 2, (int) getZ());
        if (newY < groundY) {
            newY = groundY;
            velY = 0;
        }

        setPos(getX(), newY, getZ());
    }

    private double[] computeAnchor(float scale) {
        if (getSegmentIndex() == 0) {
            float yaw = (float) Math.toRadians(parent.yBodyRot);
            double bodyAnimY = -parent.bodyAnimY / 16.0f * scale;
            return new double[]{
                    parent.getX() + Math.sin(yaw) * BODY_OFF * scale,
                    parent.getY() + bodyAnimY,
                    parent.getZ() - Math.cos(yaw) * BODY_OFF * scale
            };
        } else {
            CrocodileTailPart prev = parent.tailParts[getSegmentIndex() - 1];
            if (prev == null || prev.isRemoved()) return null;
            return new double[]{ prev.getX(), prev.getY(), prev.getZ() };
        }
    }

    private int findGroundY(int x, int startY, int z) {
        for (int y = startY; y >= startY - 30; y--) {
            if (level().getBlockState(new net.minecraft.core.BlockPos(x, y, z)).isSolid()) return y + 1;
        }
        return startY - 30;
    }

    @Override
    public boolean shouldBeSaved() {
        return false; // recréées par le croco au chargement, jamais persistées
    }

    // ─── Dégâts → redirigés vers le croco parent ──────────────────────────
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) return false;
        if (parent == null) return false;
        return parent.hurt(source, amount);
    }

    @Override public boolean isPickable() { return true; }
    @Override public boolean skipAttackInteraction(Entity entity) { return false; }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        int seg = this.entityData.get(SEGMENT_INDEX);
        float scale = (parent != null) ? parent.getScale() : 1f;
        return EntityDimensions.scalable(BASE_WIDTHS[seg] * scale, BASE_HEIGHTS[seg] * scale);
    }

    // ─── Accesseurs ───────────────────────────────────────────────────────
    @Nullable public CrocodileEntity getParent()  { return parent; }
    public int getSegmentIndex() { return this.entityData.get(SEGMENT_INDEX); }

    // ─── NBT ──────────────────────────────────────────────────────────────
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(PARENT_ID,     tag.getInt("ParentId"));
        this.entityData.set(SEGMENT_INDEX, tag.getInt("SegmentIndex"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("ParentId",     this.entityData.get(PARENT_ID));
        tag.putInt("SegmentIndex", this.entityData.get(SEGMENT_INDEX));
    }
}