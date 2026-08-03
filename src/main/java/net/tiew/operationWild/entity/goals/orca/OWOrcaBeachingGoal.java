package net.tiew.operationWild.entity.goals.orca;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;

import java.util.EnumSet;
import java.util.List;

/**
 * Goal d'échouage volontaire de l'orque pour chasser sur la côte.
 * Exclusivement actif sur les orques sauvages (sans owner).
 *
 * Phases :
 *   APPROACHING → nage vers la target à la surface
 *   LEAPING     → bond hors de l'eau vers la côte
 *   BEACHED     → combat au corps à corps sur la plage
 *
 * Enregistrer dans OrcaEntity.registerGoals() avec addGoal(5, new OWOrcaBeachingGoal(this))
 */
public class OWOrcaBeachingGoal extends Goal {

    // ── Phases internes ──────────────────────────────────────────────────────

    private enum Phase { SEARCHING, APPROACHING, LEAPING, BEACHED }

    // ── Constantes ───────────────────────────────────────────────────────────

    /** Distance max (blocs) entre la target et le bord de l'océan. */
    private static final double COAST_MAX_DIST = 6.0;

    /** Rayon de scan eau autour du point côtier le plus proche. */
    private static final int COAST_CHECK_RADIUS = 5;

    /** Nombre minimum de blocs d'eau dans le rayon pour valider une vraie côte. */
    private static final int MIN_WATER_BLOCKS = 20;

    /** Profondeur d'eau verticale minimale pour valider une vraie côte. */
    private static final int MIN_WATER_DEPTH = 3;

    /** Distance horizontale (blocs) déclenchant le bond. */
    private static final double LEAP_TRIGGER_DIST_H = 13.0;

    /** Distance max (blocs) avant abandon en phase BEACHED. */
    private static final double ABANDON_DIST = 8.0;

    /** Ticks entre deux attaques en phase BEACHED. */
    private static final int ATTACK_COOLDOWN = 28;

    /** Ticks max autorisés en phase LEAPING avant abort (sécurité si l'orque retombe à l'eau). */
    private static final int LEAP_MAX_TICKS = 80;

    /** Cooldown entre deux déclenchements du goal (300 ticks = 15 secondes). */
    private static final int TRIGGER_COOLDOWN = 300;

    // ── État ─────────────────────────────────────────────────────────────────

    private final OrcaEntity orca;
    private LivingEntity target;
    private Phase phase = Phase.SEARCHING;
    private int attackCooldownTicks = 0;
    private int leapTicks = 0;
    private int cooldown = 0;

    // ── Constructeur ─────────────────────────────────────────────────────────

    public OWOrcaBeachingGoal(OrcaEntity orca) {
        this.orca = orca;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    // ── Cycle de vie du goal ─────────────────────────────────────────────────

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (this.orca.getOwner() != null) return false;
        if (!this.orca.isInWater()) return false;

        LivingEntity found = findCoastalTarget();
        System.out.println(found);
        if (found == null) return false;

        this.target = found;
        this.phase = Phase.APPROACHING;
        this.attackCooldownTicks = 0;
        this.leapTicks = 0;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.orca.getOwner() != null) return false;
        if (target == null || !target.isAlive()) return false;
        // Le souffle passe avant la proie : sans ce renoncement, l'orque tenait sa position sur le
        // sable jusqu'à en mourir. C'est le goal qui la retenait, rien d'autre.
        if (this.orca.isDryingOut()) return false;

        return switch (phase) {
            // Si la target quitte la côte pendant l'approche → abandon immédiat
            case APPROACHING -> isNearRealCoast(target);
            // Pendant le bond la physique gère, on attend juste LEAP_MAX_TICKS
            case LEAPING     -> leapTicks < LEAP_MAX_TICKS;
            // Sur la plage, on continue tant que la target est accessible
            case BEACHED     -> orca.distanceTo(target) <= ABANDON_DIST;
            default          -> false;
        };
    }

    @Override
    public void start() {
        this.attackCooldownTicks = 0;
        this.leapTicks = 0;
    }

    @Override
    public void stop() {
        this.orca.forceSetTarget(null);
        this.orca.setAggressive(false);
        this.orca.setRunning(false);
        this.orca.setBeached(false);
        // Le tonneau ne se déclenche que sur le front montant : le drapeau doit retomber pour qu'un
        // prochain bond puisse le relever.
        this.orca.setDashing(false);
        this.target = null;
        this.phase = Phase.SEARCHING;
        this.leapTicks = 0;
        this.attackCooldownTicks = 0;
        this.cooldown = TRIGGER_COOLDOWN;
        this.orca.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (target == null || !target.isAlive()) return;
        switch (phase) {
            case APPROACHING -> tickApproaching();
            case LEAPING     -> tickLeaping();
            case BEACHED     -> tickBeached();
            case SEARCHING   -> {} // état transitoire, ne devrait pas se produire ici
        }
    }

    // ── Phase APPROACHING ────────────────────────────────────────────────────

    private void tickApproaching() {
        orca.getLookControl().setLookAt(target, 30f, 30f);

        // Nage en surface vers la target
        double seaY = orca.level().getSeaLevel() - 1.5;
        orca.getNavigation().moveTo(target.getX(), seaY, target.getZ(), orca.getSwimSpeed());

        // Déclenchement du bond à 13 blocs horizontaux
        if (horizontalDist(orca, target) <= LEAP_TRIGGER_DIST_H) {
            phase = Phase.LEAPING;
            leapTicks = 0;
            doLeap();
        }
    }

    // ── Phase LEAPING ────────────────────────────────────────────────────────

    private void tickLeaping() {
        leapTicks++;
        // L'orque est hors de l'eau depuis suffisamment longtemps → elle a atterri
        if (leapTicks > 8 && !orca.isInWater()) {
            phase = Phase.BEACHED;
            orca.setBeached(true);
            orca.setDashing(false);
        }
        // On oriente visuellement vers la target pendant le vol (cosmétique)
        orca.getLookControl().setLookAt(target, 30f, 30f);
    }

    // ── Phase BEACHED ────────────────────────────────────────────────────────

    private void tickBeached() {
        orca.getLookControl().setLookAt(target, 30f, 30f);

        // Le système de combo lit orca.getTarget() pour savoir qui frapper.
        // Sans ce setter, setCombo() se déclenche mais ne fait aucun dégât.
        orca.setTarget(target);
        orca.setAggressive(true);

        if (attackCooldownTicks > 0) attackCooldownTicks--;

        // Attaque au corps à corps — même mécanisme qu'OWAttackGoal
        if (orca.distanceTo(target) <= 4.5 && attackCooldownTicks <= 0) {
            if (!orca.isCombo()) {
                orca.setCombo(true, 1);
            } else if (orca.isPauseCombo()) {
                orca.playerContinueCombo = true;
            }
            attackCooldownTicks = ATTACK_COOLDOWN;
        }
    }

    // ── Bond hors de l'eau ───────────────────────────────────────────────────

    private void doLeap() {
        if (target == null || !target.isAlive()) return;

        double dist = orca.distanceTo(target);

        // Orientation vers la target (repris de TigerEntity.leapToTarget)
        Vec3 vec = target.position().subtract(orca.position());
        orca.setYRot(-((float) Mth.atan2(vec.x, vec.z)) * Mth.RAD_TO_DEG);
        orca.yBodyRot = orca.getYRot();

        Vec3 direction = new Vec3(
                target.getX() - orca.getX(), 0.0, target.getZ() - orca.getZ()
        );
        if (direction.lengthSqr() > 1.0E-7) {
            direction = direction.normalize().scale(Math.min(dist, 30.0) * 0.2);
        }

        // verticalBoost plus élevé que le Tiger (0.25) pour aller plus haut
        float verticalBoost = 1.5f;
        float clampedVertical = (float) Mth.clamp(target.getEyeY() - orca.getY(), 0, 2);
        orca.setDeltaMovement(
                direction.x,
                direction.y + verticalBoost + 0.1f * clampedVertical,
                direction.z
        );
        orca.hasImpulse = true;
        orca.getNavigation().stop();

        // Même drapeau que la ruée du cavalier : c'est lui qui enclenche le tonneau côté client, et
        // il est déjà synchronisé. Le bond sauvage tourne donc sur lui-même comme la ruée montée,
        // sans un paquet de plus.
        orca.setDashing(true);

        // Particules d'éclaboussure
        OWUtils.spawnServerParticles(orca, ParticleTypes.SPLASH, 0, 0.5, 0, 30, 1.5);

        // TODO: Remplacer SoundEvents.PLAYER_SPLASH_HIGH_SPEED par OWSounds.ORCA_BEACH_LEAP.get()
        //       quand le son sera enregistré dans OWSounds.
        orca.level().playSound(
                null, orca.getX(), orca.getY(), orca.getZ(),
                SoundEvents.PLAYER_SPLASH_HIGH_SPEED,
                SoundSource.AMBIENT, 2.5f, 0.8f
        );

        // TODO: Remplacer SoundEvents.DOLPHIN_SPLASH par OWSounds.ORCA_ROAR.get() (cri de charge)
        //       quand le son sera enregistré dans OWSounds.
        orca.level().playSound(
                null, orca.getX(), orca.getY(), orca.getZ(),
                SoundEvents.DOLPHIN_SPLASH,
                SoundSource.AMBIENT, 1.8f, 0.7f
        );
    }

    // ── Recherche de target côtière ──────────────────────────────────────────

    private LivingEntity findCoastalTarget() {
        List<LivingEntity> candidates = orca.level().getEntitiesOfClass(
                LivingEntity.class,
                orca.getBoundingBox().inflate(30.0),
                e -> isValidTarget(e) && isNearRealCoast(e)
        );
        if (candidates.isEmpty()) return null;

        LivingEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity e : candidates) {
            double d = orca.distanceTo(e);
            if (d < best) { best = d; nearest = e; }
        }
        return nearest;
    }

    /**
     * Cibles valides : Player (monté ou non), Pig, Sheep, Cow, Horse, et toutes les sous-classes de Monster.
     */
    private boolean isValidTarget(LivingEntity e) {
        if (!e.isAlive() || e == orca) return false;
        if (e instanceof Player p) return !p.isSpectator() && !p.isCreative();
        return e instanceof Pig
                || e instanceof Sheep
                || e instanceof Cow
                || e instanceof Horse
                || e instanceof Monster;
    }

    // ── Détection de vraie côte océanique ────────────────────────────────────

    /**
     * Retourne true si l'entité est à moins de COAST_MAX_DIST blocs d'une vraie côte.
     *
     * Algorithme en deux étapes :
     *   1) Cherche le bloc d'eau le plus proche qui a déjà MIN_WATER_DEPTH blocs d'eau
     *      consécutifs en dessous de lui. On utilise ce point "profond" comme référence
     *      pour éviter l'eau de lisière de plage (fond sableux à 1-2 blocs).
     *   2) Compte les blocs d'eau dans un volume autour de ce point. Un océan réel en
     *      a facilement 20+, une petite flaque ou un cube artificiel non.
     *
     * Utilise level.isWaterAt() pour capturer eau source, eau courante et blocs waterlogged.
     */
    private boolean isNearRealCoast(LivingEntity entity) {
        Level level = orca.level();
        BlockPos ePos = entity.blockPosition();
        int hRange = (int) COAST_MAX_DIST;

        // Étape 1 : trouver le bloc d'eau le plus proche qui est ASSEZ PROFOND.
        // L'eau de lisière de plage a souvent du sable à 1-2 blocs → on la saute.
        BlockPos deepRef = null;
        double bestSq = COAST_MAX_DIST * COAST_MAX_DIST + 1.0;

        outer:
        for (BlockPos p : BlockPos.betweenClosed(
                ePos.offset(-hRange, -5, -hRange),
                ePos.offset(hRange, 1, hRange))) {

            if (!level.isWaterAt(p)) continue;

            // Ce bloc d'eau doit avoir MIN_WATER_DEPTH blocs d'eau consécutifs en dessous
            for (int i = 1; i <= MIN_WATER_DEPTH; i++) {
                if (!level.isWaterAt(p.below(i))) continue outer;
            }

            double dx = p.getX() - ePos.getX();
            double dy = p.getY() - ePos.getY();
            double dz = p.getZ() - ePos.getZ();
            double dsq = dx * dx + dy * dy + dz * dz;
            if (dsq < bestSq) {
                bestSq = dsq;
                deepRef = p.immutable();
            }
        }
        if (deepRef == null) return false;

        // Étape 2 : compter les blocs d'eau dans un volume autour du point de référence.
        // On inclut la profondeur (Y-5) pour capturer le volume océanique, pas juste la surface.
        int waterCount = 0;
        for (BlockPos p : BlockPos.betweenClosed(
                deepRef.offset(-COAST_CHECK_RADIUS, -5, -COAST_CHECK_RADIUS),
                deepRef.offset(COAST_CHECK_RADIUS, 2, COAST_CHECK_RADIUS))) {
            if (level.isWaterAt(p)) waterCount++;
        }
        return waterCount >= MIN_WATER_BLOCKS;
    }

    // ── Utilitaire ───────────────────────────────────────────────────────────

    private static double horizontalDist(LivingEntity a, LivingEntity b) {
        double dx = b.getX() - a.getX();
        double dz = b.getZ() - a.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }
}
