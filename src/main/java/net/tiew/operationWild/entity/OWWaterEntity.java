package net.tiew.operationWild.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.core.OWDamageSources;
import net.tiew.operationWild.event.ClientEvents;

public abstract class OWWaterEntity extends OWEntity implements net.tiew.operationWild.entity.config.IOWDiver {

    /** Créature d'eau : elle n'a rien à faire sur la terre ferme. */
    @Override
    public int arenaTerrainMask() {
        return net.tiew.operationWild.core.OWArena.Terrain.AQUATIC.bit();
    }

    private static final EntityDataAccessor<Float> TARGET_PITCH =
            SynchedEntityData.defineId(OWWaterEntity.class, EntityDataSerializers.FLOAT);

    public float damageTimer = 0;
    public boolean firstTimeToDeep = true;

    public OWWaterEntity(EntityType<? extends TamableAnimal> entityType, Level level,
                         float scale, int maxSleepBar, int sleepBarDownSpeed) {
        super(entityType, level, scale, maxSleepBar, sleepBarDownSpeed);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TARGET_PITCH, 0.0f);
    }

    @Override
    public abstract int getMaxDepth();
    public abstract float getSwimSpeed();

    public float getTargetPitch() { return this.entityData.get(TARGET_PITCH); }
    public void setTargetPitch(float pitch) { this.entityData.set(TARGET_PITCH, pitch); }

    @Override
    protected boolean canLean() {
        return this.isInWater() && !this.isSitting() && !this.isSleeping();
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isInWater() && (this.isEffectiveAi() || this.isControlledByLocalInstance())) {
            this.moveRelative(this.getSpeed(), travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
            // Le cycle de nage se lit sur walkAnimation, que seul calculateEntityAnimation fait
            // avancer — et il vit tout au bout de LivingEntity#travel, qu'on ne traverse pas ici.
            // Tant que cette branche ne servait qu'au serveur, le client passait par la voie vanilla
            // et l'animation suivait ; maintenant qu'elle sert aussi au cavalier, il faut la tenir
            // à jour nous-mêmes, sans quoi la bête glisse dans l'eau parfaitement immobile.
            this.calculateEntityAnimation(false);
        } else {
            super.travel(travelVector);
        }
    }

    /** Une créature entièrement aquatique tient sa profondeur : regard à l'horizontale, elle ne coule pas. */
    @Override
    protected double riddenBuoyancy() {
        return 0.0D;
    }

    /**
     * Remontée à la touche de saut, comme sur le submersible : de quoi regagner la surface sans
     * avoir à piquer du nez vers le haut, ce qui fait perdre le cap et la vitesse d'avance.
     */
    @Override
    protected double riddenAscendSpeed() {
        return 0.12D;
    }

    /**
     * Inertie de nage : la bete met du temps a lancer, et bien plus a s'arreter.
     * Hors de l'eau, elle retrouve la reponse vive d'une monture terrestre.
     */
    @Override
    protected float riddenSpeedResponse(boolean accelerating) {
        if (!this.isInWater()) return super.riddenSpeedResponse(accelerating);
        return accelerating ? RIDDEN_SWIM_ACCEL_RESPONSE : RIDDEN_SWIM_BRAKE_RESPONSE;
    }

    @Override
    public boolean isPushedByFluid() { return false; }

    @Override
    public float getWaterSlowDown() { return 0.98F; }

    /**
     * Envol volontaire en cours : la bride de sortie d'eau doit alors se taire.
     * Faux par défaut — seule une espèce qui sait sauter la relève.
     */
    protected boolean isBreaching() {
        return false;
    }

    /**
     * Vrai quand la bête vient de se donner elle-même une impulsion verticale hors de l'eau, à
     * laisser passer telle quelle : la bride ci-dessous ne doit brider que l'élan de nage.
     */
    protected boolean keepsVerticalImpulseOutOfWater() {
        return false;
    }

    /**
     * Immergée, une créature entièrement aquatique ne perd pas son souffle : c'est son élément.
     *
     * <p>La réserve ne baisse qu'à l'air libre, et c'est {@link #tick()} qui s'en charge — pas cette
     * méthode, que le moteur n'appelle justement que sous l'eau.</p>
     */
    @Override
    protected int decreaseAirSupply(int currentAir) {
        return currentAir;
    }

    @Override
    public void tick() {
        super.tick();

        // Bride de sortie d'eau : sans elle, la moindre poussée de nage catapultait la bête en
        // l'air. Elle est levée pendant un envol volontaire, sinon un saut hors de l'eau serait
        // écrêté à trois dixièmes de bloc par tick et l'animal ne décollerait jamais vraiment.
        if (!this.isInWater() && !this.isBreaching() && !this.keepsVerticalImpulseOutOfWater()) {
            Vec3 mv = this.getDeltaMovement();
            if (mv.y > 0.3) {
                this.setDeltaMovement(mv.x * 0.5, 0.3, mv.z * 0.5);
            }
        }

        if (!this.level().isClientSide()) {
            if (!this.isInWater()) {
                float p = this.getTargetPitch();
                if (Math.abs(p) > 0.1f) this.setTargetPitch(p * 0.85f);
                else if (p != 0f) this.setTargetPitch(0f);
            }
        }

        int depth = (int) (this.level().getSeaLevel() - this.getY());
        if (depth >= this.getMaxDepth()) {
            LivingEntity rider = this.getControllingPassenger();
            if (rider instanceof Player player) {
                if (!player.isCreative()) applyWaterPressureDamage(depth, player);
            } else {
                applyWaterPressureDamage(depth, null);
            }
        } else {
            damageTimer = 0.0f;
            firstTimeToDeep = true;
        }
    }

    public void applyWaterPressureDamage(int depth, Player player) {
        if (!this.isTame() || !this.isInWater()) return;
        int waterPressure = (int) ClientEvents.getWaterPressure(depth);
        float damageInterval = (Math.max((-1.25f * waterPressure + 65) / 30.0f, 0.1f));
        float normalizedPressure = waterPressure / 4.0f;
        float intensity = 0.05f * (float) Math.pow(normalizedPressure, 2f);

        ClientEvents.shakeCamera(intensity, player);

        if (this.level().isClientSide) return;

        damageTimer += 0.05f;
        if (damageTimer >= damageInterval) {
            this.invulnerableTime = 0;
            DamageSource src = OWDamageSources.createWaterPressureDamage((ServerLevel) this.level());
            this.hurt(src, 4);
            this.invulnerableTime = 0;
            damageTimer = 0.0f;
        }

        if (this.tickCount % 100 == 0 || firstTimeToDeep) {
            Component message = Component.translatable("tooHighPressure")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW));
            firstTimeToDeep = false;
        }
    }

    protected boolean isAtSurface() {
        BlockPos posAbove = this.blockPosition().above();
        return this.level().isEmptyBlock(posAbove) || !this.level().getFluidState(posAbove).isEmpty() == false;
    }


    public static class OWSwimMoveControl extends MoveControl {

        /**
         * Écart horizontal en dessous duquel la destination est jugée à l'aplomb, et son cap
         * inexploitable. Un tiers de bloc : en deçà, l'angle n'est plus que du bruit.
         */
        private static final double YAW_DEAD_ZONE = 0.1;

        private final OWWaterEntity entity;

        public OWSwimMoveControl(OWWaterEntity entity) {
            super(entity);
            this.entity = entity;
        }

        @Override
        public void tick() {
            if (this.entity.isInWater()) {
                this.entity.setDeltaMovement(this.entity.getDeltaMovement().add(0.0D, 0.003D, 0.0D));
            }

            if (this.operation == Operation.MOVE_TO) {
                double dx = this.wantedX - this.entity.getX();
                double dy = this.wantedY - this.entity.getY();
                double dz = this.wantedZ - this.entity.getZ();
                double distSq = dx * dx + dy * dy + dz * dz;

                if (distSq < 2.5E-7F) {
                    this.mob.setZza(0.0F);
                    return;
                }

                // Le cap n'est recalculé que si la destination a une direction horizontale DIGNE DE
                // CE NOM.
                //
                // C'est l'origine de la bête qui tourne sur elle-même sans fin. Le garde ci-dessus
                // ne teste que la distance totale, verticale comprise : une destination située à
                // l'aplomb — même dix blocs plus haut — le franchit sans peine alors que {@code dx}
                // et {@code dz} n'y valent plus que du bruit d'arrondi. L'{@code atan2} en tirait un
                // cap différent à chaque tick, et l'orque pivotait indéfiniment sur place au lieu
                // de monter. Sans direction horizontale exploitable, on garde simplement le cap
                // courant : la poussée verticale plus bas fait tout le travail.
                double horizontalSq = dx * dx + dz * dz;
                if (horizontalSq > YAW_DEAD_ZONE) {
                    // Virage confié à la bête, et non plus plafonné ici à dix degrés par tick.
                    //
                    // C'est ce contrôle qui conduit dès qu'un chemin existe, et il s'exécute APRÈS
                    // les goals : le cap qu'ils venaient de poser était réécrit dans la foulée avec
                    // cette allure fixe. Régler {@code getRotationSpeed()} sur l'espèce ne changeait
                    // donc rigoureusement rien tant qu'elle nageait vers quelque chose — le seul
                    // réglage qui comptait était celui-ci, en dur, et invisible depuis l'entité.
                    this.entity.turnTowards(new Vec3(dx, 0.0, dz));
                }

                float speed = (float) (this.speedModifier *
                        this.entity.getAttributeValue(Attributes.MOVEMENT_SPEED));

                if (this.entity.isInWater()) {
                    this.entity.setSpeed(speed * 0.02F);

                    // Pente réellement demandée par le chemin, et non l'assiette de la bête.
                    //
                    // Le terme vertical se déduisait du tangage de l'entité — or celui-ci est remis à
                    // zéro à chaque tick tant qu'elle est dans l'eau : le tangage VISIBLE est
                    // procédural (cf. leanPitch) et ne passe jamais par l'entité elle-même. La
                    // poussée verticale valait donc sin(0), c'est-à-dire rien du tout. La bête ne
                    // pouvait remonter que par la flottabilité de trois millièmes ajoutée plus haut,
                    // et n'atteignait jamais une cible restée en surface.
                    double horizontal = Math.sqrt(dx * dx + dz * dz);
                    float pitch = (float) Mth.atan2(dy, Math.max(horizontal, 1.0E-4));
                    this.entity.zza = Mth.cos(pitch) * speed;
                    this.entity.yya = Mth.sin(pitch) * speed;
                } else {
                    this.entity.setSpeed(speed * 0.1F);
                }
            } else {
                this.entity.setSpeed(0.0F);
                this.entity.setXxa(0.0F);
                this.entity.setYya(0.0F);
                this.entity.setZza(0.0F);

                if (!this.entity.level().isClientSide()) {
                    float p = this.entity.getTargetPitch();
                    if (Math.abs(p) > 0.1f) this.entity.setTargetPitch(p * 0.88f);
                    else if (p != 0f) this.entity.setTargetPitch(0f);
                }
            }
        }
    }
}