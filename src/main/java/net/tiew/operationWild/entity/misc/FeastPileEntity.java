package net.tiew.operationWild.entity.misc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.animals.terrestrial.RedPandaEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;

public class FeastPileEntity extends Entity {

    private static final EntityDataAccessor<Integer> DATA_PORTIONS =
            SynchedEntityData.defineId(FeastPileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PORTIONS_MAX =
            SynchedEntityData.defineId(FeastPileEntity.class, EntityDataSerializers.INT);
    /**
     * Le semeur, connu du client.
     *
     * <p>{@code ownerId} restait un champ serveur : le rendu ne pouvait donc pas savoir d'ou faire
     * partir la nourriture, et la faisait jaillir du centre du tas. Elle part maintenant des pattes
     * de la bete qui la lance, et le suit si le porteur se deplace pendant le geste.</p>
     */
    private static final EntityDataAccessor<Integer> DATA_OWNER =
            SynchedEntityData.defineId(FeastPileEntity.class, EntityDataSerializers.INT);

    private int ownerId = -1;
    private double radius = OWAttacksConstants.RedPanda.FEAST_RADIUS;
    private int pulseTimer = 0;
    private int lifetime = 0;
    private int maxLifetime = OWAttacksConstants.RedPanda.FEAST_DURATION_TICKS;
    private double edgeSoundAngle = 0;
    private final java.util.List<Integer> diners = new java.util.ArrayList<>();

    public FeastPileEntity(EntityType<? extends FeastPileEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /**
     * Pose le tas sur le premier sol sous le point vise, puis l'y cloue.
     *
     * <p>Il tombait auparavant sous sa propre gravite, ce qui le faisait glisser sur les pentes et
     * deriver quand on le jetait en marchant. Un festin est un lieu : il doit rester ou on l'a mis.
     * Le point de depart est pris un peu au-dessus pour que le rayon accroche aussi une marche.</p>
     */
    public void plantOnGround() {
        Vec3 from = this.position().add(0, 1.2, 0);
        Vec3 to = this.position().add(0, -6.0, 0);

        BlockHitResult hit = this.level().clip(new ClipContext(
                from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        double ground = hit.getType() == HitResult.Type.MISS ? this.getY() : hit.getLocation().y;
        this.setPos(this.getX(), ground + 0.02, this.getZ());
        this.setDeltaMovement(Vec3.ZERO);
    }

    /**
     * Le rendu deborde tres largement de la boite de collision : les tas sont eparpilles sur tout le
     * rayon, mais ils appartiennent tous a cette entite. Sans cet elargissement, le festin
     * disparaissait entierement des que son centre sortait du champ.
     */
    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(radius + 2.0, 3.0, radius + 2.0);
    }

    public FeastPileEntity(Level level, RedPandaEntity owner, int portions, double radius) {
        this(OWEntityRegistry.FEAST_PILE.get(), level);
        this.ownerId = owner.getId();
        this.entityData.set(DATA_OWNER, owner.getId());
        this.radius = radius;
        this.maxLifetime = portions * OWAttacksConstants.RedPanda.FEAST_PULSE_INTERVAL
                * OWAttacksConstants.RedPanda.FEAST_LIFETIME_FACTOR_NUM
                / OWAttacksConstants.RedPanda.FEAST_LIFETIME_FACTOR_DEN;
        this.entityData.set(DATA_PORTIONS, portions);
        this.entityData.set(DATA_PORTIONS_MAX, portions);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_PORTIONS, OWAttacksConstants.RedPanda.FEAST_PORTIONS);
        builder.define(DATA_PORTIONS_MAX, OWAttacksConstants.RedPanda.FEAST_PORTIONS);
        builder.define(DATA_OWNER, -1);
    }

    public int portions() {
        return this.entityData.get(DATA_PORTIONS);
    }

    public int portionsMax() {
        return Math.max(1, this.entityData.get(DATA_PORTIONS_MAX));
    }

    public double radius() {
        return this.radius;
    }

    public int ownerId() {
        return this.entityData.get(DATA_OWNER);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        this.setDeltaMovement(Vec3.ZERO);

        if (this.level().isClientSide()) return;

        if (this.tickCount % OWAttacksConstants.RedPanda.FEAST_EDGE_INTERVAL == 0) markEdge();
        if (this.tickCount % 2 == 0) spawnFeastWaves();
        if (this.tickCount % OWAttacksConstants.RedPanda.FEAST_EDGE_SOUND_INTERVAL == 0) soundEdge();

        lifetime++;
        if (portions() <= 0 || lifetime > maxLifetime) {
            closeFeast();
            return;
        }

        if (!(this.level().getEntity(ownerId) instanceof RedPandaEntity panda) || !panda.isAlive()) {
            closeFeast();
            return;
        }

        if (this.tickCount % OWAttacksConstants.RedPanda.FEAST_DINER_SCAN_INTERVAL == 0) scanDiners(panda);
        if (this.tickCount % OWAttacksConstants.RedPanda.FEAST_SWIRL_INTERVAL == 0) swirlAroundDiners();

        if (++pulseTimer < OWAttacksConstants.RedPanda.FEAST_PULSE_INTERVAL) return;
        pulseTimer = 0;

        boolean served = false;

        for (LivingEntity guest : eligibleGuests(panda)) {
            guest.heal(OWAttacksConstants.RedPanda.FEAST_HEAL_PER_PULSE);
            panda.beginFeeding(guest, OWAttacksConstants.RedPanda.FEAST_PULSE_INTERVAL);
            served = true;
        }

        if (!served) return;

        this.entityData.set(DATA_PORTIONS, portions() - 1);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.COMPOSTER,
                    this.getX(), this.getY() + 0.3, this.getZ(), 6, 0.4, 0.15, 0.4, 0.02);
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GRASS_BREAK, SoundSource.NEUTRAL, 0.5f,
                (float) OWUtils.generateRandomInterval(1.1, 1.4));
    }

    /**
     * Une couronne de miettes au bord de la nappe.
     *
     * <p>Le rayon etait invisible : on ne savait pas ou se placer pour manger. Ce sont les MEMES
     * miettes que celles du tas, posees au sol sans vitesse — pas un cercle lumineux, juste la
     * limite ou la nourriture s'arrete. Chaque point est descendu sur le sol qu'il surplombe, pour
     * que la couronne epouse le terrain au lieu de flotter au-dessus d'une pente.</p>
     */
    private void markEdge() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        int points = OWAttacksConstants.RedPanda.FEAST_EDGE_POINTS;
        double spin = this.tickCount * 0.004;

        for (int i = 0; i < points; i++) {
            double angle = spin + (Math.PI * 2 * i) / points;
            double x = this.getX() + Math.cos(angle) * radius;
            double z = this.getZ() + Math.sin(angle) * radius;

            Vec3 from = new Vec3(x, this.getY() + 2.5, z);
            Vec3 to = new Vec3(x, this.getY() - 4.0, z);
            BlockHitResult hit = this.level().clip(new ClipContext(
                    from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (hit.getType() == HitResult.Type.MISS) continue;

            serverLevel.sendParticles(ParticleTypes.COMPOSTER,
                    x, hit.getLocation().y + 0.08, z, 1, 0.06, 0.0, 0.06, 0.0);
        }
    }

    /**
     * Le bruit de la nappe fait le tour du bord.
     *
     * <p>Le pas angulaire vaut l'angle d'or en radians : deux emissions successives ne tombent
     * jamais au meme endroit et le tour ne se referme jamais sur lui-meme, si bien qu'aucun rythme
     * ne s'installe. On entend le festin s'etendre au lieu d'entendre un point.</p>
     */
    private void soundEdge() {
        edgeSoundAngle += OWAttacksConstants.RedPanda.FEAST_EDGE_SOUND_STEP;

        double reach = radius * (0.72 + this.random.nextDouble() * 0.28);
        double x = this.getX() + Math.cos(edgeSoundAngle) * reach;
        double z = this.getZ() + Math.sin(edgeSoundAngle) * reach;

        SoundEvent rustle = switch (this.random.nextInt(3)) {
            case 0 -> SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES;
            case 1 -> SoundEvents.BAMBOO_HIT;
            default -> SoundEvents.COMPOSTER_FILL;
        };

        this.level().playSound(null, x, this.getY() + 0.2, z, rustle, SoundSource.NEUTRAL,
                0.5f, (float) OWUtils.generateRandomInterval(0.85, 1.25));
    }

    /**
     * Les convives eligibles, ceux que le tas nourrit vraiment.
     *
     * <p>Un seul endroit decide : la pulsation qui soigne et le tourbillon qui l'annonce lisent la
     * meme liste. Sans cela, les deux auraient tot ou tard diverge et des particules auraient tourne
     * autour de betes qui ne recevaient rien.</p>
     */
    private java.util.List<LivingEntity> guestsInRange(RedPandaEntity panda) {
        java.util.List<LivingEntity> found = new java.util.ArrayList<>();
        AABB box = this.getBoundingBox().inflate(radius);

        for (LivingEntity guest : this.level().getEntitiesOfClass(LivingEntity.class, box)) {
            if (!guest.isAlive() || guest == panda) continue;
            if (this.distanceTo(guest) > radius) continue;
            if (!panda.isHealAlly(guest)) continue;
            found.add(guest);
        }
        return found;
    }

    private java.util.List<LivingEntity> eligibleGuests(RedPandaEntity panda) {
        java.util.List<LivingEntity> found = new java.util.ArrayList<>();
        for (LivingEntity guest : guestsInRange(panda)) {
            if (guest.getHealth() >= guest.getMaxHealth()) continue;
            found.add(guest);
        }
        return found;
    }

    /**
     * La ronde marque TOUS les convives admis, blesses ou non.
     *
     * <p>La lier au soin la faisait disparaitre des qu'une bete etait remise d'aplomb : elle
     * clignotait au lieu de designer. Elle repond a une question de placement — qui est a portee du
     * tas — et cette question ne cesse pas d'avoir une reponse parce qu'on est en pleine sante.</p>
     */
    private void scanDiners(RedPandaEntity panda) {
        diners.clear();
        for (LivingEntity guest : guestsInRange(panda)) diners.add(guest.getId());
    }

    /**
     * Une ronde de miettes autour de chaque convive.
     *
     * <p>Le tas disait ou aller, il ne disait pas QUI en profitait : dans une melee, rien ne
     * distinguait la bete nourrie de celle qui passait a cote. La ronde emploie la particule du
     * festin, pas une lueur : c'est de la nourriture qui tourne, pas un halo de soin.</p>
     *
     * <p>La liste est rafraichie moins souvent qu'elle n'est dessinee — balayer la zone a chaque
     * emission aurait coute un recensement complet cinq fois par seconde pour un resultat qui ne
     * change pas d'une image a l'autre.</p>
     */
    private void swirlAroundDiners() {
        if (diners.isEmpty() || !(this.level() instanceof ServerLevel serverLevel)) return;

        int points = OWAttacksConstants.RedPanda.FEAST_SWIRL_POINTS;

        for (int id : diners) {
            if (!(this.level().getEntity(id) instanceof LivingEntity guest) || !guest.isAlive()) continue;

            // Chaque bete tourne a sa propre phase : sans ce decalage, tout un groupe pivotait a
            // l'unisson, ce qui se lit comme un effet plaque et non comme une ronde par convive.
            double phase = this.tickCount * OWAttacksConstants.RedPanda.FEAST_SWIRL_SPEED + id * 1.7;
            double ring = guest.getBbWidth() * 0.55 + 0.25;
            double climb = ((this.tickCount * 0.03 + id * 0.31) % 1.0) * guest.getBbHeight();

            for (int i = 0; i < points; i++) {
                double angle = phase + (Math.PI * 2 * i) / points;

                serverLevel.sendParticles(ParticleTypes.COMPOSTER,
                        guest.getX() + Math.cos(angle) * ring,
                        guest.getY() + climb,
                        guest.getZ() + Math.sin(angle) * ring,
                        1, 0.03, 0.03, 0.03, 0.01);
            }
        }
    }

    private void spawnFeastWaves() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        int travel = OWAttacksConstants.RedPanda.FEAST_WAVE_TRAVEL_TICKS;

        for (int wave = 0; wave < OWAttacksConstants.RedPanda.FEAST_WAVE_COUNT; wave++) {
            int offset = wave * OWAttacksConstants.RedPanda.FEAST_WAVE_INTERVAL;
            double progress = ((lifetime + offset) % travel) / (double) travel;
            double ring = progress * radius;
            if (ring < 0.6) continue;

            int points = Math.max(8, (int) (ring * 3));
            double spin = lifetime * 0.05 + wave * 1.7;

            for (int i = 0; i < points; i++) {
                double angle = spin + (Math.PI * 2 * i) / points;
                double x = this.getX() + Math.cos(angle) * ring;
                double z = this.getZ() + Math.sin(angle) * ring;

                serverLevel.sendParticles(ParticleTypes.COMPOSTER,
                        x, this.getY() + 0.12, z, 1, 0.02, 0.01, 0.02, 0.0);
            }
        }
    }

    private void closeFeast() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.COMPOSTER,
                    this.getX(), this.getY() + 0.25, this.getZ(), 18, 0.5, 0.2, 0.5, 0.06);
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GRASS_BREAK, SoundSource.NEUTRAL, 0.7f, 0.7f);
        this.discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.ownerId = tag.getInt("ownerId");
        this.radius = tag.getDouble("radius");
        this.lifetime = tag.getInt("lifetime");
        this.maxLifetime = Math.max(1, tag.getInt("maxLifetime"));
        this.entityData.set(DATA_PORTIONS, tag.getInt("portions"));
        this.entityData.set(DATA_PORTIONS_MAX, Math.max(1, tag.getInt("portionsMax")));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("ownerId", this.ownerId);
        tag.putDouble("radius", this.radius);
        tag.putInt("lifetime", this.lifetime);
        tag.putInt("maxLifetime", this.maxLifetime);
        tag.putInt("portions", portions());
        tag.putInt("portionsMax", portionsMax());
    }
}
