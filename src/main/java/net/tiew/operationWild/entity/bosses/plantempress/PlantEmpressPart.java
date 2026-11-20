package net.tiew.operationWild.entity.bosses.plantempress;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import net.tiew.operationWild.entity.bosses.OWBossTarget;
import net.tiew.operationWild.entity.bosses.PlantEmpressEntity;
import org.jetbrains.annotations.Nullable;

public class PlantEmpressPart extends PartEntity<PlantEmpressEntity> implements OWBossTarget {

    public static final int STEM = -1;

    public final int chain;
    public final int bone;
    private EntityDimensions size;

    public PlantEmpressPart(PlantEmpressEntity parent, int chain, int bone, float width, float height) {
        super(parent);
        this.chain = chain;
        this.bone = bone;
        this.size = EntityDimensions.scalable(width, height);
        this.refreshDimensions();
    }

    public boolean isHead() {
        return this.chain >= 0;
    }

    @Override
    public Entity strikeGroup() {
        return this.getParent();
    }

    @Override
    public int strikePriority() {
        if (!isHead()) return 0;
        PlantEmpressEntity boss = this.getParent();
        if (boss.isChainDead(this.chain)) return 0;
        return boss.isHeadVulnerable(this.chain) ? 3 : 1;
    }

    public void place(Vec3 center) {
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        this.xOld = this.xo;
        this.yOld = this.yo;
        this.zOld = this.zo;
        this.setPos(center.x, center.y - this.size.height() * 0.5, center.z);
    }

    public void fit(AABB box) {
        float width = (float) Math.max(0.8, Math.max(box.getXsize(), box.getZsize()));
        float height = (float) Math.max(0.8, box.getYsize());
        if (Math.abs(width - this.size.width()) > 0.05f || Math.abs(height - this.size.height()) > 0.05f) {
            this.size = EntityDimensions.scalable(width, height);
            this.refreshDimensions();
        }
        this.place(box.getCenter());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return this.isAlive() && this.getParent().isAlive();
    }

    @Nullable
    @Override
    public ItemStack getPickResult() {
        return this.getParent().getPickResult();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) return false;
        return this.getParent().hurtPart(this, source, amount);
    }

    @Override
    public boolean is(Entity entity) {
        return this == entity || this.getParent() == entity;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.size;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(double x, double y, double z) {
    }
}
