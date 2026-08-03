package net.tiew.operationWild.entity.goals.orca;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;

import java.util.EnumSet;

/**
 * Jeu de la proie : l'orque sauvage qui a déjà eu le dessus cesse de tuer et se met à projeter sa
 * victime hors de l'eau, à coups de nageoire caudale, avant de la relâcher.
 *
 * <p>Comportement documenté et de très mauvaise réputation : les orques envoient réellement phoques
 * et raies valser en l'air, parfois sans les manger ensuite. C'est ce qui en fait une bonne feature
 * de jeu plutôt qu'une cruauté gratuite — <b>la proie n'est pas achevée</b>. Chaque coup est
 * plafonné pour ne jamais descendre en dessous d'un point de vie, et au bout de quelques
 * projections l'orque se désintéresse et rend la liberté.</p>
 *
 * <p>Le joueur y gagne une seconde chance là où il n'en avait aucune, et la scène la plus marquante
 * que la bête puisse lui offrir. Il y perd le contrôle de ses déplacements le temps que ça dure :
 * la séquence est donc courte et strictement bornée.</p>
 *
 * <p>Déclenchée par la faiblesse de la proie et non par une quelconque satiété : l'orque n'a pas de
 * jauge de faim, et « elle a gagné, elle s'amuse » se lit tout aussi bien en jeu.</p>
 */
public class OWOrcaPreyToyGoal extends Goal {

    /**
     * Part de vie en dessous de laquelle la proie est considérée comme vaincue.
     *
     * <p>Relevé d'un tiers à la moitié. L'orque frappe pour quinze points : un joueur passe de vingt
     * à cinq en un coup, et il n'existe donc qu'un seul état intermédiaire entre indemne et mort. Un
     * palier trop bas ne laissait qu'une poignée de ticks pour l'attraper — le temps qu'une seule
     * frappe de plus arrive.</p>
     */
    private static final float BEATEN_FRACTION = 0.5F;

    /** Distance maximale à laquelle l'orque daigne jouer. */
    private static final double PLAY_RANGE = 14.0;

    /** Écart horizontal en deçà duquel l'orque est jugée sous la proie. */
    private static final double UNDER_TOLERANCE = 2.4;

    private static final int MAX_TOSSES = 4;

    /** Force de la projection : franchement vers le haut, un peu vers l'avant. */
    private static final double TOSS_VERTICAL = 1.15;
    private static final double TOSS_HORIZONTAL = 0.45;

    /**
     * Dernière projection : envoyée au loin, et non plus en l'air sur place.
     *
     * <p>C'est ce qui donne du poids au relâchement. L'orque reprend sa chasse presque aussitôt —
     * son goal de ciblage la lui rend en une poignée de ticks — si bien que sans cette distance, la
     * seconde chance offerte à la proie n'aurait duré qu'une seconde.</p>
     */
    private static final double FINAL_TOSS_HORIZONTAL = 1.7;
    private static final double FINAL_TOSS_VERTICAL = 0.95;

    /** Morsure d'accompagnement, toujours plafonnée pour laisser la proie vivante. */
    private static final float TOSS_DAMAGE = 2.0F;

    private static final int APPROACH_TIMEOUT = 120;
    private static final int WATCH_TIMEOUT = 90;
    private static final int COOLDOWN_MIN = 600;
    private static final int COOLDOWN_MAX = 1200;

    private enum Phase { APPROACH, FLICK, WATCH }

    private final OrcaEntity orca;
    private LivingEntity prey;
    private Phase phase = Phase.APPROACH;
    private int phaseTicks;
    private int tosses;
    private int cooldown;

    public OWOrcaPreyToyGoal(OrcaEntity orca) {
        this.orca = orca;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        if (!isUsable()) return false;

        LivingEntity target = this.orca.getTarget();
        if (!isBeatenPrey(target)) return false;
        if (this.orca.distanceTo(target) > PLAY_RANGE) return false;

        this.prey = target;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!isUsable()) return false;
        if (this.prey == null || !this.prey.isAlive()) return false;
        if (this.tosses >= MAX_TOSSES) return false;
        if (this.orca.distanceTo(this.prey) > PLAY_RANGE + 8.0) return false;
        return this.phaseTicks <= timeoutFor(this.phase);
    }

    @Override
    public void start() {
        this.phase = Phase.APPROACH;
        this.phaseTicks = 0;
        this.tosses = 0;
        this.orca.setPlayingWithPrey(true);
        // Un combo déjà lancé continue de porter ses coups tout seul, du côté de l'entité et non du
        // goal : préempter la chasse ne suffisait pas à l'arrêter, et il achevait la proie au beau
        // milieu du jeu. On le coupe net.
        this.orca.setCombo(false, 0);
    }

    /**
     * L'orque se désintéresse : la cible est relâchée, pas achevée.
     *
     * <p>Sans cet oubli, la chasse ordinaire reprendrait à l'instant même où le jeu s'arrête, et la
     * seconde chance offerte à la proie n'aurait duré qu'un tick.</p>
     */
    @Override
    public void stop() {
        this.orca.setPlayingWithPrey(false);
        if (this.prey != null && this.prey.isAlive() && this.tosses >= MAX_TOSSES) {
            this.orca.forceSetTarget(null);
        }
        this.prey = null;
        this.orca.getNavigation().stop();
        this.cooldown = COOLDOWN_MIN + this.orca.getRandom().nextInt(COOLDOWN_MAX - COOLDOWN_MIN);
    }

    @Override
    public void tick() {
        if (this.prey == null) return;
        this.phaseTicks++;
        this.orca.getLookControl().setLookAt(this.prey.getX(), this.prey.getY(), this.prey.getZ());
        // Une proie ballottée ne doit pas mourir de sa retombée, fût-ce sur la banquise.
        this.prey.fallDistance = 0.0F;

        switch (this.phase) {
            case APPROACH -> tickApproach();
            case FLICK -> tickFlick();
            case WATCH -> tickWatch();
        }
    }

    /** Se glisser sous la proie : le coup part de la nageoire caudale, donc par en dessous. */
    private void tickApproach() {
        double dx = this.prey.getX() - this.orca.getX();
        double dz = this.prey.getZ() - this.orca.getZ();
        boolean under = Math.sqrt(dx * dx + dz * dz) <= UNDER_TOLERANCE
                && this.orca.getY() < this.prey.getY();

        if (under) {
            this.orca.getNavigation().stop();
            this.orca.startTailFlick();
            this.phase = Phase.FLICK;
            this.phaseTicks = 0;
            return;
        }
        if (this.phaseTicks % 8 == 0) {
            this.orca.getNavigation().moveTo(
                    this.prey.getX(), this.prey.getY() - this.orca.getBbHeight(), this.prey.getZ(), 1.4);
        }
    }

    /** Le geste tourne côté modèle ; on n'intervient qu'à l'image du contact. */
    private void tickFlick() {
        if (this.orca.isTailFlickImpact()) {
            launchPrey();
            return;
        }
        if (!this.orca.isTailFlicking()) {
            this.phase = Phase.WATCH;
            this.phaseTicks = 0;
        }
    }

    private void launchPrey() {
        this.tosses++;

        double dx = this.prey.getX() - this.orca.getX();
        double dz = this.prey.getZ() - this.orca.getZ();
        Vec3 away = new Vec3(dx, 0.0, dz);
        away = away.lengthSqr() > 1.0E-4 ? away.normalize() : this.orca.getLookAngle();

        // La morsure d'abord, la projection ensuite : {@code hurt} applique son propre recul, qui
        // aurait écrasé l'impulsion si on l'avait posée avant. Plafonnée à ce qui laisse la proie
        // debout — elle ressort de la séance rouée, jamais morte.
        float damage = Math.min(TOSS_DAMAGE, Math.max(0.0F, this.prey.getHealth() - 1.0F));
        if (damage > 0.0F) {
            this.prey.hurt(this.orca.damageSources().mobAttack(this.orca), damage);
        }

        boolean lastToss = this.tosses >= MAX_TOSSES;
        double horizontal = lastToss ? FINAL_TOSS_HORIZONTAL : TOSS_HORIZONTAL;
        double vertical = lastToss ? FINAL_TOSS_VERTICAL : TOSS_VERTICAL;

        this.prey.setDeltaMovement(away.x * horizontal, vertical, away.z * horizontal);
        this.prey.hurtMarked = true;
        this.prey.hasImpulse = true;
        this.prey.fallDistance = 0.0F;

        this.orca.level().playSound(null, this.prey.getX(), this.prey.getY(), this.prey.getZ(),
                SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.NEUTRAL, 1.8F, 0.9F);
        if (this.orca.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    this.prey.getX(), this.prey.getY(), this.prey.getZ(), 30, 0.6, 0.2, 0.6, 0.35);
        }

        this.phase = Phase.WATCH;
        this.phaseTicks = 0;
    }

    /**
     * L'orque regarde retomber ce qu'elle vient de lancer, puis recommence.
     *
     * <p>Ce temps mort est ce qui rend la scène lisible : sans lui, les projections
     * s'enchaîneraient sans qu'on ait le temps de comprendre ce qui se passe.</p>
     */
    private void tickWatch() {
        this.orca.getNavigation().stop();
        boolean landed = this.prey.onGround() || this.prey.isInWater();
        if (landed && this.phaseTicks > 20) {
            this.phase = Phase.APPROACH;
            this.phaseTicks = 0;
        }
    }

    private static int timeoutFor(Phase phase) {
        return switch (phase) {
            case APPROACH -> APPROACH_TIMEOUT;
            case FLICK -> OrcaEntity.FLICK_ANIM_DURATION + 20;
            case WATCH -> WATCH_TIMEOUT;
        };
    }

    /**
     * Proie vaincue : affaiblie, dans l'eau, et pas des nôtres.
     *
     * <p>L'exigence d'immersion n'est pas un détail. Une proie restée sur la glace ou dans un bateau
     * relève de la vague ou de la percussion, et une orque n'a de toute façon pas de quoi la
     * soulever de là.</p>
     */
    private boolean isBeatenPrey(LivingEntity target) {
        if (target == null || !target.isAlive() || target instanceof OrcaEntity) return false;
        if (!target.isInWater()) return false;
        if (this.orca.isAlliedTo(target)) return false;
        if (target instanceof Player player && (player.isSpectator() || player.isCreative())) return false;
        return target.getHealth() <= target.getMaxHealth() * BEATEN_FRACTION;
    }

    @Override
    public boolean isInterruptable() {
        return true;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private boolean isUsable() {
        return !this.orca.isTame()
                && !this.orca.isBaby()
                && !this.orca.isBeached()
                && !this.orca.isSleeping()
                && !this.orca.isVehicle()
                && !this.orca.isSpyhopping()
                && !this.orca.isWaveCharging()
                && this.orca.isInWater();
    }
}
