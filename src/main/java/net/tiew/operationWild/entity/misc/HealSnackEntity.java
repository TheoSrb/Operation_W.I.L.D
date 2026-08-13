package net.tiew.operationWild.entity.misc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;

/**
 * Orbe de soin du panda roux : une bille verte lancée en cloche.
 *
 * <p>Elle est <b>jetée</b>, pas tirée. La solution balistique est recalculée à chaque tick sur le
 * temps de vol restant : la bille part vers le haut, culmine, retombe sur la cible, et corrige
 * d'elle-même si celle-ci s'est déplacée entre-temps. Un allié qui fait un pas de côté ne fait donc
 * pas rater le soin — c'est un geste de secours, pas un tir d'adresse —, mais la trajectoire reste
 * une vraie parabole plutôt qu'un trait tendu.</p>
 *
 * <p>Aucune particule : la bille est un objet à part entière, dessinée par {@code HealSnackRenderer}.
 * La nuée de points d'avant se lisait comme une fuite, jamais comme un projectile.</p>
 */
public class HealSnackEntity extends Entity {

    /**
     * Cible et durée de vol partagées avec le client.
     *
     * <p>Sans elles, seul le serveur savait où allait la bille : le client se contentait de la
     * pousser sur son élan, et chaque paquet de position la replaçait d'un coup sec — {@code lerpTo}
     * ne lisse rien sur une entité ordinaire, il téléporte. D'où les à-coups. Les deux côtés
     * déroulent maintenant le MÊME calcul, si bien que les corrections du serveur ne corrigent plus
     * rien de visible.</p>
     */
    private static final EntityDataAccessor<Integer> DATA_TARGET_ID =
            SynchedEntityData.defineId(HealSnackEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FLIGHT_TICKS =
            SynchedEntityData.defineId(HealSnackEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> DATA_SNACK =
            SynchedEntityData.defineId(HealSnackEntity.class, EntityDataSerializers.ITEM_STACK);

    /** Part de l'écart rattrapée par tick quand le serveur corrige : on rejoint sans sauter. */
    private static final double CORRECTION_RATE = 0.25;

    private int ownerId = -1;
    private boolean credits = true;
    private int lifetime = 0;

    public HealSnackEntity(EntityType<? extends HealSnackEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public HealSnackEntity(Level level, RedPandaEntity owner, LivingEntity target, ItemStack snack) {
        this(OWEntityRegistry.HEAL_SNACK.get(), level);
        this.ownerId = owner.getId();
        this.entityData.set(DATA_TARGET_ID, target.getId());
        this.entityData.set(DATA_SNACK, snack.copy());
    }

    public void setCredits(boolean credits) {
        this.credits = credits;
    }

    public ItemStack getSnack() {
        ItemStack snack = this.entityData.get(DATA_SNACK);
        return snack.isEmpty() ? RedPandaEntity.snackForIndex(0) : snack;
    }

    /**
     * Fixe la durée de vol d'après la distance, une fois la bille placée à son point de départ.
     *
     * <p>Durée et non vitesse : c'est elle qui commande la hauteur de l'arc, et la borner des deux
     * côtés garantit qu'un jet à bout portant garde une cloche lisible et qu'un jet à trente blocs
     * n'attend pas dix secondes.</p>
     */
    public void aimAt(LivingEntity target) {
        double horizontal = target.position().subtract(this.position()).multiply(1, 0, 1).length();
        this.entityData.set(DATA_FLIGHT_TICKS, Mth.clamp(
                (int) Math.round(horizontal / OWAttacksConstants.RedPanda.HEAL_SNACK_FLIGHT_SPEED),
                OWAttacksConstants.RedPanda.HEAL_SNACK_MIN_FLIGHT_TICKS,
                OWAttacksConstants.RedPanda.HEAL_SNACK_MAX_FLIGHT_TICKS));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TARGET_ID, -1);
        builder.define(DATA_FLIGHT_TICKS, OWAttacksConstants.RedPanda.HEAL_SNACK_MIN_FLIGHT_TICKS);
        builder.define(DATA_SNACK, ItemStack.EMPTY);
    }

    private int targetId() {
        return this.entityData.get(DATA_TARGET_ID);
    }

    private int flightTicks() {
        return Math.max(1, this.entityData.get(DATA_FLIGHT_TICKS));
    }

    /**
     * Correction du serveur amortie au lieu d'être appliquée d'un bloc.
     *
     * <p>{@code Entity.lerpTo} téléporte : seule {@code LivingEntity} l'étale sur plusieurs ticks. Un
     * projectile qui reçoit une position par tick sautait donc à chaque paquet. Les deux côtés
     * simulant désormais la même parabole, l'écart est minime et se rattrape par quarts sans que
     * l'œil le voie.</p>
     */
    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        this.setPos(
                this.getX() + (x - this.getX()) * CORRECTION_RATE,
                this.getY() + (y - this.getY()) * CORRECTION_RATE,
                this.getZ() + (z - this.getZ()) * CORRECTION_RATE);
    }

    @Override
    public void tick() {
        super.tick();

        lifetime++;

        boolean server = !this.level().isClientSide();
        if (server && lifetime > OWAttacksConstants.RedPanda.HEAL_SNACK_MAX_LIFETIME) {
            this.discard();
            return;
        }

        Entity found = this.level().getEntity(targetId());
        if (!(found instanceof LivingEntity target) || !target.isAlive()) {
            if (server) this.discard();
            return;
        }

        Vec3 center = target.position().add(0, target.getBbHeight() * 0.55, 0);
        Vec3 delta = center.subtract(this.position());

        if (delta.length() <= 0.75 || lifetime >= flightTicks()) {
            if (server) deliver(target);
            return;
        }

        // Solution balistique sur le temps restant : vitesse horizontale constante, vitesse
        // verticale relevée de la moitié de la chute à venir. La bille monte, culmine, retombe.
        //
        // Déroulée des DEUX côtés, à partir des mêmes données : la trajectoire du client est celle
        // du serveur, aux quelques centimètres près que l'amortissement de lerpTo absorbe.
        double remaining = Math.max(1, flightTicks() - lifetime);
        double gravity = OWAttacksConstants.RedPanda.HEAL_SNACK_GRAVITY;

        Vec3 velocity = new Vec3(
                delta.x / remaining,
                delta.y / remaining + 0.5 * gravity * remaining,
                delta.z / remaining);

        this.setDeltaMovement(velocity);
        this.setPos(this.getX() + velocity.x, this.getY() + velocity.y, this.getZ() + velocity.z);
    }

    private void deliver(LivingEntity target) {
        Entity owner = this.level().getEntity(ownerId);
        if (owner instanceof RedPandaEntity redPanda) {
            redPanda.serveSnack(target, getSnack(), credits);
        }
        this.discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.ownerId = tag.getInt("ownerId");
        this.credits = tag.getBoolean("credits");
        this.lifetime = tag.getInt("lifetime");
        this.entityData.set(DATA_TARGET_ID, tag.getInt("targetId"));
        this.entityData.set(DATA_FLIGHT_TICKS, Math.max(1, tag.getInt("flightTicks")));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("ownerId", this.ownerId);
        tag.putBoolean("credits", this.credits);
        tag.putInt("lifetime", this.lifetime);
        tag.putInt("targetId", targetId());
        tag.putInt("flightTicks", flightTicks());
    }
}
