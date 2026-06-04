package net.tiew.operationWild.entity.misc;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Fantôme spectral ancré au sol pendant le Rituel de Communion. Il ne bouge pas et ne
 * riposte pas : il est la CIBLE des vagues de monstres. Sa "vie" représente la
 * <b>Stabilité de l'âme</b> ; si elle tombe à 0, le rituel échoue.
 *
 * <p>Le rendu (générique pour tout OWEntity) est délégué à {@code SoulGhostRenderer} qui
 * reconstruit le modèle du compagnon à partir du type/variant/skin/échelle synchronisés.</p>
 */
public class SoulGhostEntity extends PathfinderMob {

    private static final EntityDataAccessor<String> DATA_TYPE =
            SynchedEntityData.defineId(SoulGhostEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(SoulGhostEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SKIN =
            SynchedEntityData.defineId(SoulGhostEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(SoulGhostEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_MATERIALIZATION =
            SynchedEntityData.defineId(SoulGhostEntity.class, EntityDataSerializers.FLOAT);

    /** Dernier tick où le gestionnaire de rituel a "pingé" ce fantôme. Sert à se nettoyer si orphelin. */
    private int lastManagerPing = 0;

    /** UUID du maître d'origine de l'âme : ses propres créatures ne peuvent pas la blesser. */
    private java.util.UUID soulOwner = null;
    public void setSoulOwner(java.util.UUID owner) { this.soulOwner = owner; }

    /** Position verrouillée : le fantôme est totalement immobile (ancré au sol par le rituel). */
    private boolean positionLocked = false;
    private double lockX, lockY, lockZ;

    public SoulGhostEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        // Immobile et ancré : pas de gravité (le rituel le pose déjà au sol), aucun goal,
        // et toute poussée/knockback est annulée (cf. lockPosition / knockback / push).
        this.setNoGravity(true);
        this.setPersistenceRequired();
        this.noCulling = true;
    }

    /** Ancre définitivement le fantôme à cette position (il n'en bougera plus). */
    public void lockPosition(double x, double y, double z) {
        this.lockX = x;
        this.lockY = y;
        this.lockZ = z;
        this.positionLocked = true;
        this.setPos(x, y, z);
        this.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TYPE, "");
        builder.define(DATA_VARIANT, 0);
        builder.define(DATA_SKIN, 0);
        builder.define(DATA_SCALE, 1.0f);
        builder.define(DATA_MATERIALIZATION, 0.0f);
    }

    @Override
    protected void registerGoals() {
        // Aucun goal : le fantôme est totalement passif.
    }

    // ── Données synchronisées ────────────────────────────────────────────────
    public void setGhostType(ResourceLocation type) { this.entityData.set(DATA_TYPE, type.toString()); }
    public ResourceLocation getGhostType() {
        String s = this.entityData.get(DATA_TYPE);
        return s.isEmpty() ? null : ResourceLocation.tryParse(s);
    }
    public EntityType<?> resolveEntityType() {
        ResourceLocation id = getGhostType();
        return id == null ? null : BuiltInRegistries.ENTITY_TYPE.get(id);
    }

    public void setGhostVariant(int v) { this.entityData.set(DATA_VARIANT, v); }
    public int getGhostVariant() { return this.entityData.get(DATA_VARIANT); }

    public void setGhostSkin(int v) { this.entityData.set(DATA_SKIN, v); }
    public int getGhostSkin() { return this.entityData.get(DATA_SKIN); }

    public void setGhostScale(float v) { this.entityData.set(DATA_SCALE, v); }
    public float getGhostScale() { return this.entityData.get(DATA_SCALE); }

    public void setMaterialization(float v) { this.entityData.set(DATA_MATERIALIZATION, Math.max(0f, Math.min(1f, v))); }
    public float getMaterialization() { return this.entityData.get(DATA_MATERIALIZATION); }

    // ── Stabilité = points de vie ────────────────────────────────────────────
    public float getStabilityFraction() {
        return this.getMaxHealth() <= 0 ? 0f : this.getHealth() / this.getMaxHealth();
    }

    public void pingFromManager() { this.lastManagerPing = this.tickCount; }

    // ── Comportement passif / invulnérabilités ───────────────────────────────
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide()) return false;
        Entity attacker = source.getEntity();
        // Seules les créatures hostiles (pas le joueur, pas l'environnement) entament la stabilité.
        if (!(attacker instanceof LivingEntity) || attacker instanceof Player) return false;
        // Les créatures appartenant au MÊME maître que l'âme ne peuvent pas la blesser.
        if (soulOwner != null && attacker instanceof net.minecraft.world.entity.TamableAnimal tamable
                && tamable.getOwnerUUID() != null && tamable.getOwnerUUID().equals(soulOwner)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            // Immobilité totale : on annule toute vitesse et on ré-ancre la position chaque tick,
            // pour que les attaques (knockback, poussées custom) ne déplacent jamais l'âme.
            if (this.positionLocked) {
                this.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                this.setPos(this.lockX, this.lockY, this.lockZ);
                this.hasImpulse = false;
                this.hurtMarked = false;
            }
            // Auto-nettoyage si plus aucun gestionnaire ne s'occupe de ce fantôme (ex : après un reload monde).
            if (this.tickCount - this.lastManagerPing > 40) {
                this.discard();
            }
        }
    }

    @Override
    public boolean isPushable() { return false; }

    @Override
    protected void doPush(Entity entity) { }

    @Override
    public void push(double x, double y, double z) {
        // Immobile : aucune poussée.
    }

    @Override
    public void knockback(double strength, double x, double z) {
        // Immobile : aucun recul sur les attaques.
    }

    @Override
    public boolean removeWhenFarAway(double distance) { return false; }

    @Override
    public boolean shouldDropExperience() { return false; }

    @Override
    public boolean shouldBeSaved() { return false; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) { }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) { }
}
