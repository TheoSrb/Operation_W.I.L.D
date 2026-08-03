package net.tiew.operationWild.entity.goals.orca;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.aquatic.OrcaEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * Vague de chasse : plusieurs orques sauvages s'alignent, chargent de front et lèvent une vague qui
 * balaie la proie de son radeau de glace — ou de son bateau — pour la jeter à l'eau.
 *
 * <p>Comportement documenté chez les orques antarctiques, qui décrochent ainsi les phoques des
 * plaques de banquise. Le gain de jeu tient à ce renversement : la cible n'est pas blessée, elle est
 * <b>déplacée</b>. Se réfugier sur la glace cesse d'être un abri, et une fois à l'eau la proie
 * redevient justiciable de la chasse ordinaire.</p>
 *
 * <p>Le comportement complète le trou de respiration sans le recouper : l'un ouvre la banquise
 * par en dessous pour l'orque, l'autre en décroche ce qui s'y tient.</p>
 *
 * <h2>Coordination sans chef</h2>
 * <p>La meute de {@code OrcaBehaviorHandler} ne convient pas ici : elle est bâtie autour d'une proie
 * <i>dans</i> l'eau, ce que la cible d'une vague n'est précisément jamais. Plutôt que de tordre ce
 * système ou d'en poser un second avec son chef et son état partagé, chaque orque déduit sa place
 * du même calcul : la liste des participantes, triée par identifiant, donne à chacune son rang, donc
 * son couloir. Aucune donnée n'est échangée, aucune ne peut se désynchroniser.</p>
 *
 * <p>Le déferlement n'a pas besoin d'être compté non plus : dès que la première vague met la proie à
 * l'eau, elle cesse d'être balayable et toutes les participantes s'arrêtent d'elles-mêmes.</p>
 */
public class OWOrcaWaveWashGoal extends Goal {

    /** Portée de repérage d'une proie perchée. */
    private static final double SEARCH_RANGE = 32.0;

    /**
     * Rayon de recensement des congénères, <b>centré sur la proie</b> et non sur soi.
     *
     * <p>C'est ce qui rend la coordination possible sans rien échanger : centré sur chaque orque,
     * le recensement donnait une liste — donc un ordre, donc des couloirs — légèrement différente
     * d'une participante à l'autre. Centré sur la proie, que toutes voient au même endroit, il rend
     * exactement la même liste pour tout le monde, à chaque tick.</p>
     *
     * <p>Doit dépasser largement le recul : les participantes sortiraient sinon du rayon en allant
     * se poster, la liste changerait sous leurs pieds, et les couloirs se réattribueraient en pleine
     * mise en place.</p>
     */
    private static final double PACK_RADIUS = 48.0;

    /**
     * Nombre d'orques nécessaires à une vague.
     *
     * <p>Ramené à une seule. La contrainte de groupe était fidèle au comportement réel, mais elle
     * rendait la manœuvre invisible en jeu : il fallait que deux bêtes se trouvent au même moment
     * près de la même proie, libres de tout le reste et hors temps de repos. Seule, l'orque décrit
     * simplement un couloir unique et charge droit — toute la machinerie de couloirs et de départ
     * simultané continue de fonctionner, elle n'a qu'une place à distribuer.</p>
     */
    private static final int MIN_WAVE_ORCAS = 1;

    /** Recul pris derrière la cible avant de s'élancer. La charge doit se voir venir de loin. */
    private static final double STAGING_DISTANCE = 30.0;

    /** Écartement entre deux couloirs de charge : un front large, pas une file. */
    private static final double LANE_SPACING = 3.4;

    /** Tolérance d'arrivée en place, et délai au-delà duquel on s'élance sans attendre les autres. */
    private static final double LINE_TOLERANCE = 3.5;

    /** Le recul est plus long qu'avant : la mise en place a besoin de plus de temps avant son filet. */
    private static final int MAX_LINEUP_TICKS = 320;

    /**
     * Vitesse de charge.
     *
     * <p>Redescendue de 1,05. Le client n'interpole la position d'une créature que sur trois ticks ;
     * au-delà d'environ quinze blocs par seconde, chaque paquet arrive avant que le lissage du
     * précédent ne soit fini et la course se voit par à-coups. C'est vif sans être hachué.</p>
     */
    private static final double CHARGE_SPEED = 0.78;
    private static final int MAX_CHARGE_TICKS = 150;

    /**
     * Allure de virage propre à la charge, indépendante de celle de l'espèce.
     *
     * <p>La charge partait du poste de repli, donc dos à la proie : il y a un demi-tour complet à
     * rattraper. À l'allure de nage — quelques centièmes de l'écart par tick — il n'était pas
     * terminé que la vague avait déjà déferlé, et les orques arrivaient sans jamais regarder leur
     * cible. Pire, le rendu retourne la carcasse tant que le cap s'écarte de plus d'un quart de
     * tour du déplacement : on les voyait charger à reculons.</p>
     *
     * <p>La moitié de l'écart par tick aligne le corps en quatre ou cinq ticks, sans être la
     * volte-face instantanée qu'on avait retirée parce qu'elle sautait à l'image. Trois dixièmes
     * laissaient encore un décalage visible : la proie bouge pendant la course, et l'orque restait
     * en permanence quelques degrés derrière son cap réel.</p>
     */
    private static final float WAVE_TURN_FACTOR = 0.5f;

    /**
     * Distance à la cible à laquelle l'orque quitte l'eau.
     *
     * <p>Prise plus au large qu'auparavant : le bond doit décrire un arc qui ARRIVE sur la proie,
     * pas décoller sous son nez.</p>
     */
    private static final double WAVE_RANGE = 6.5;

    /** Impulsion du bond, franchement verticale — c'est une sortie d'eau, pas un saut de carpe. */
    private static final double LEAP_UP = 0.92;
    private static final double LEAP_FORWARD = 0.52;

    /**
     * Rayon balayé, et force du déferlement.
     *
     * <p>Nettement adoucie : la vague doit décoller la proie de son radeau et la mettre à l'eau, pas
     * l'expédier à l'autre bout de l'océan. Une bousculade franche de quelques blocs suffit à faire
     * comprendre ce qui vient d'arriver, et laisse la suite du combat jouable.</p>
     */
    private static final double WAVE_RADIUS = 4.5;
    private static final double WAVE_PUSH = 0.7;
    private static final double WAVE_LIFT = 0.36;

    private static final int COOLDOWN_MIN = 400;
    private static final int COOLDOWN_MAX = 900;

    /** Épaisseur de glace maximale sous laquelle on cherche encore de l'eau. */
    private static final int ICE_THICKNESS_LIMIT = 4;

    private enum Phase { LINING_UP, CHARGING, BREACHING }

    private final OrcaEntity orca;
    private LivingEntity prey;
    private Phase phase = Phase.LINING_UP;
    private Vec3 runDirection = Vec3.ZERO;
    private Vec3 stagingPos = Vec3.ZERO;
    private int cooldown;
    private int phaseTicks;
    private boolean done;

    public OWOrcaWaveWashGoal(OrcaEntity orca) {
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

        this.prey = findPerchedPrey();
        if (this.prey == null) return false;

        List<OrcaEntity> line = gatherLine();
        return line.size() >= MIN_WAVE_ORCAS && line.contains(this.orca);
    }

    @Override
    public boolean canContinueToUse() {
        if (this.done) return false;

        // En l'air, on ne réexamine plus rien. Les conditions d'entrée exigent d'être DANS l'eau,
        // ce que le bond cesse d'être par définition : les appliquer telles quelles coupait le goal
        // en plein vol, et le déferlement — qu'il est seul à déclencher — n'arrivait jamais.
        if (this.phase == Phase.BREACHING) {
            return this.phaseTicks <= OrcaEntity.WAVE_BREACH_DURATION + 20;
        }

        if (!isUsable()) return false;
        if (this.prey == null || !this.prey.isAlive()) return false;
        // La proie tombée à l'eau n'est plus à balayer : c'est ce qui arrête toute la ligne d'un coup,
        // sans avoir à compter les vagues ni à se concerter.
        if (!isPerched(this.prey)) return false;
        return this.phaseTicks <= (this.phase == Phase.LINING_UP ? MAX_LINEUP_TICKS : MAX_CHARGE_TICKS);
    }

    @Override
    public void start() {
        this.done = false;
        this.phase = Phase.LINING_UP;
        this.phaseTicks = 0;
        // Dès le premier repli, et non à la charge : la mise en place est le moment le plus fragile
        // de la manœuvre, et c'est précisément là qu'une autre envie venait défaire la ligne.
        this.orca.setWaveEngaged(true);
        List<OrcaEntity> line = gatherLine();
        this.runDirection = approachDirection(line);
        this.stagingPos = stagingFor(this.orca, line, this.runDirection);
    }

    @Override
    public void stop() {
        this.prey = null;
        this.done = false;
        this.orca.setWaveEngaged(false);
        this.orca.setWaveCharging(false);
        this.orca.getNavigation().stop();
        this.cooldown = COOLDOWN_MIN + this.orca.getRandom().nextInt(COOLDOWN_MAX - COOLDOWN_MIN);
    }

    @Override
    public void tick() {
        if (this.prey == null) return;
        this.phaseTicks++;

        if (this.phase == Phase.LINING_UP) {
            this.orca.getLookControl().setLookAt(
                    this.prey.getX(), this.prey.getY(), this.prey.getZ());
            tickLineUp();
            return;
        }

        // Cap imposé à la main sur la colonne de la proie, sans passer par le contrôle du regard.
        //
        // Celui-ci s'exécute APRÈS les goals et relisse ce qu'on vient d'imposer : les deux se
        // disputaient l'orientation d'un tick à l'autre, ce qui se voyait comme des saccades. Et
        // viser l'axe de course figé plutôt que la proie faisait qu'elles ne la regardaient jamais.
        // Le déplacement, lui, reste engagé sur l'axe : c'est le corps qui suit la cible, pas la
        // trajectoire.
        faceHorizontally(this.prey.getX(), this.prey.getZ());

        if (this.phase == Phase.CHARGING) {
            tickCharge();
            return;
        }
        tickBreach();
    }

    /**
     * Mise en place, puis départ <b>simultané</b>.
     *
     * <p>Personne ne s'élance tant que toute la ligne n'est pas à son poste. Comme chacune recalcule
     * la formation à partir des mêmes données vivantes — la liste recensée autour de la proie, triée
     * — toutes parviennent à la même conclusion au même tick et partent ensemble, sans qu'aucune
     * n'ait eu besoin de prévenir les autres.</p>
     *
     * <p>La direction de course est <b>figée</b> à cet instant précis. Recalculée pendant la
     * charge, elle aurait suivi la dérive des positions et les trajectoires se seraient incurvées :
     * ce n'est plus une ligne qui arrive de front, c'est un banc qui converge.</p>
     */
    private void tickLineUp() {
        List<OrcaEntity> line = gatherLine();
        this.runDirection = approachDirection(line);
        this.stagingPos = stagingFor(this.orca, line, this.runDirection);

        boolean timedOut = this.phaseTicks >= MAX_LINEUP_TICKS;
        if (timedOut || lineIsFormed(line, this.runDirection)) {
            this.phase = Phase.CHARGING;
            this.phaseTicks = 0;
            this.orca.getNavigation().stop();
            this.orca.setWaveCharging(true);
            return;
        }

        if (this.phaseTicks % 10 == 0) {
            this.orca.getNavigation().moveTo(
                    this.stagingPos.x, this.stagingPos.y, this.stagingPos.z, 1.3);
        }
    }

    /**
     * Vrai quand assez de monde est en place — soi comprise.
     *
     * <p>Exiger que <b>toute</b> la ligne soit à son poste bloquait le départ en pratique. Le
     * recensement ramène les orques par leur seule proximité à la proie, sans savoir si elles
     * participent : une consœur encore en temps de repos, ou occupée ailleurs, n'allait jamais à son
     * couloir et retenait les autres jusqu'à l'expiration du délai. Un seuil suffit : les
     * participantes s'attendent, les absentes ne comptent plus.</p>
     */
    private boolean lineIsFormed(List<OrcaEntity> line, Vec3 direction) {
        if (line.size() < MIN_WAVE_ORCAS) return false;
        if (this.orca.position().distanceTo(stagingFor(this.orca, line, direction)) > LINE_TOLERANCE) {
            return false;
        }
        int inPlace = 0;
        for (OrcaEntity member : line) {
            if (member.position().distanceTo(stagingFor(member, line, direction)) <= LINE_TOLERANCE) {
                inPlace++;
            }
        }
        return inPlace >= MIN_WAVE_ORCAS;
    }

    /** Sens de la course : du centre de gravité de la ligne vers la proie, à plat. */
    private Vec3 approachDirection(List<OrcaEntity> line) {
        Vec3 origin = Vec3.ZERO;
        for (OrcaEntity member : line) origin = origin.add(member.position());
        origin = origin.scale(1.0 / Math.max(1, line.size()));

        Vec3 approach = new Vec3(
                this.prey.getX() - origin.x, 0.0, this.prey.getZ() - origin.z);
        return approach.lengthSqr() > 1.0E-4 ? approach.normalize() : new Vec3(1.0, 0.0, 0.0);
    }

    /**
     * Poste de départ d'une participante : son rang dans la ligne triée lui donne son couloir.
     *
     * <p>Calculable pour n'importe quelle membre, et pas seulement pour soi — c'est ce qui permet à
     * chacune de vérifier que les autres sont en place sans rien leur demander.</p>
     */
    private Vec3 stagingFor(OrcaEntity member, List<OrcaEntity> line, Vec3 direction) {
        int index = Math.max(0, line.indexOf(member));
        double lane = (index - (line.size() - 1) / 2.0) * LANE_SPACING;
        Vec3 lateral = new Vec3(-direction.z, 0.0, direction.x);

        // Trois blocs sous la proie, et non un seul : celle-ci se tient sur la glace, donc au niveau
        // de la mer. Un poste posé à sa hauteur tombait dans la banquise ou dans l'air, où
        // l'évaluateur de nage ne sait construire aucun chemin — les orques n'arrivaient jamais en
        // place et n'attendaient plus que l'expiration du délai pour s'élancer n'importe comment.
        return this.prey.position()
                .subtract(direction.scale(STAGING_DISTANCE))
                .add(lateral.scale(lane))
                .add(0.0, -3.0, 0.0);
    }

    /**
     * Amène le corps dans l'axe de la course, d'un mouvement vif mais pas instantané.
     *
     * <p>Le cap est imposé au corps ({@code yBodyRot}) et non seulement à la tête : c'est lui que le
     * rendu utilise pour orienter la carcasse, et lui seul décide si l'orque arrive de face ou de
     * dos. Un quart de tour par seconde environ — de quoi voir la ligne pivoter d'un bloc avant de
     * s'élancer, sans qu'elle paraisse téléportée dans le bon sens.</p>
     */
    /**
     * Oriente le corps vers la colonne de la proie, par petits pas.
     *
     * <p>La volte-face immédiate qui figurait ici a été retirée : elle réglait bien le problème de
     * la ligne arrivant à reculons, mais au prix d'un pivot d'une image à l'autre, qui se voyait.
     * Le virage progressif suffit, parce que la charge, elle, vise désormais la proie à chaque tick
     * — le cap et la trajectoire convergent donc ensemble au lieu de se contredire.</p>
     */
    private void faceHorizontally(double x, double z) {
        this.orca.turnTowards(new Vec3(x - this.orca.getX(), 0.0, z - this.orca.getZ()),
                WAVE_TURN_FACTOR);
        // Assiette tenue à plat : la proie est perchée plus haut, et laisser le tangage la viser
        // ferait à nouveau lever le nez à la ligne au moment de percuter.
        this.orca.setXRot(0f);
    }

    private void tickCharge() {
        // Cap réajusté sur la proie à chaque tick, au lieu de courir sur un axe figé au départ.
        //
        // C'est ce qui manquait pour qu'elles la REGARDENT vraiment. Le corps affiché par le rendu
        // ne vient pas de {@code yBodyRot}, qui n'est pas synchronisé : le client le reconstitue
        // depuis la direction de déplacement observée. Imposer une orientation côté serveur ne
        // pouvait donc rien changer tant que la trajectoire, elle, ne visait pas la proie.
        Vec3 toPrey = new Vec3(
                this.prey.getX() - this.orca.getX(), 0.0, this.prey.getZ() - this.orca.getZ());
        if (toPrey.lengthSqr() > 1.0E-4) this.runDirection = toPrey.normalize();

        // Nage juste sous la surface : une vague levée depuis le fond ne se verrait pas, et
        // l'orque manquerait la plaque en passant dessous. La composante verticale reste calée sur
        // la ligne d'eau, jamais sur la proie — c'est ce qui garde le nez à l'horizontale.
        double wantedY = this.prey.getY() - 1.0;
        double climb = Math.max(-0.15, Math.min(0.15, (wantedY - this.orca.getY()) * 0.2));

        this.orca.setDeltaMovement(
                this.runDirection.x * CHARGE_SPEED,
                climb,
                this.runDirection.z * CHARGE_SPEED);

        spawnBowWave();

        double dx = this.prey.getX() - this.orca.getX();
        double dz = this.prey.getZ() - this.orca.getZ();
        if (Math.sqrt(dx * dx + dz * dz) <= WAVE_RANGE) {
            launchBreach();
        }
    }

    /**
     * L'élan de la charge est converti en bond : l'orque sort de l'eau et part en vrille.
     *
     * <p>La charge s'éteint ici — son animation de propulsion n'a plus lieu d'être une fois en
     * l'air, et celle du bond prend le relais.</p>
     */
    private void launchBreach() {
        this.phase = Phase.BREACHING;
        this.phaseTicks = 0;

        this.orca.setWaveCharging(false);
        this.orca.startWaveBreach();
        this.orca.setDeltaMovement(
                this.runDirection.x * LEAP_FORWARD, LEAP_UP, this.runDirection.z * LEAP_FORWARD);
        this.orca.hasImpulse = true;

        if (this.orca.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    this.orca.getX(), this.orca.getY() + this.orca.getBbHeight(), this.orca.getZ(),
                    35, 1.2, 0.3, 1.2, 0.35);
        }
    }

    /**
     * En l'air : plus rien à piloter, la balistique fait le travail.
     *
     * <p>Le déferlement est déclenché par l'entité, à l'image exacte du claquement de caudale —
     * c'est le coup de queue qui lève la vague, et il doit partir avec elle.</p>
     */
    private void tickBreach() {
        if (this.orca.hasWaveBreachSlammed()) {
            breakWave();
            return;
        }
        if (!this.orca.isWaveBreaching()) this.done = true;
    }

    /**
     * Écume de charge : le bourrelet poussé devant, et l'empreinte de nageoire laissée derrière.
     *
     * <p>Les deux comptent autant l'un que l'autre. Le bourrelet dit où l'orque va, la traîne dit
     * d'où elle vient et à quelle vitesse — c'est elle qui rend la ligne lisible depuis la glace,
     * bien avant qu'on ne distingue les corps sous l'eau.</p>
     */
    private void spawnBowWave() {
        if (!(this.orca.level() instanceof ServerLevel serverLevel)) return;

        double surfaceY = this.orca.getY() + this.orca.getBbHeight();
        Vec3 front = this.orca.position().add(this.runDirection.scale(this.orca.getBbWidth()));
        serverLevel.sendParticles(ParticleTypes.SPLASH,
                front.x, surfaceY, front.z, 5, 0.7, 0.05, 0.7, 0.06);

        Vec3 wake = this.orca.position().subtract(this.runDirection.scale(this.orca.getBbWidth() * 1.6));
        serverLevel.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP,
                wake.x, this.orca.getY() + this.orca.getBbHeight() * 0.4, wake.z,
                3, 0.5, 0.2, 0.5, 0.02);
        if (this.phaseTicks % 3 == 0) {
            serverLevel.sendParticles(ParticleTypes.BUBBLE,
                    wake.x, this.orca.getY() + this.orca.getBbHeight() * 0.5, wake.z,
                    6, 0.9, 0.3, 0.9, 0.03);
        }
    }

    /**
     * Déferlement : tout ce qui est perché dans le rayon part à l'eau, dans le sens de la charge.
     *
     * <p>La poussée est franche et légèrement relevée — il s'agit de décoller la proie de son
     * support, pas de la faire glisser dessus.</p>
     */
    private void breakWave() {
        this.done = true;

        Vec3 center = this.prey.position();
        AABB sweep = new AABB(center, center).inflate(WAVE_RADIUS, 2.0, WAVE_RADIUS);

        for (Entity entity : this.orca.level().getEntities(this.orca, sweep, e -> true)) {
            if (entity instanceof OrcaEntity) continue;
            boolean washable = entity instanceof Boat
                    || (entity instanceof LivingEntity living && isPerched(living) && !this.orca.isAlliedTo(living));
            if (!washable) continue;

            entity.setDeltaMovement(entity.getDeltaMovement().add(
                    this.runDirection.x * WAVE_PUSH, WAVE_LIFT, this.runDirection.z * WAVE_PUSH));
            entity.hurtMarked = true;
            entity.hasImpulse = true;
        }

        this.orca.level().playSound(null, center.x, center.y, center.z,
                SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.NEUTRAL, 2.2F, 0.65F);
        this.orca.level().playSound(null, center.x, center.y, center.z,
                SoundEvents.DOLPHIN_ATTACK, SoundSource.NEUTRAL, 1.3F, 0.6F);

        if (this.orca.level() instanceof ServerLevel serverLevel) {
            Vec3 lateral = new Vec3(-this.runDirection.z, 0.0, this.runDirection.x);
            for (int i = -4; i <= 4; i++) {
                Vec3 crest = center.add(lateral.scale(i * 0.9));
                serverLevel.sendParticles(ParticleTypes.SPLASH,
                        crest.x, center.y + 0.3, crest.z, 12, 0.3, 0.35, 0.3, 0.3);
            }
        }
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
                && this.orca.isInWater();
    }

    /**
     * Les orques susceptibles de charger de front, dans un ordre que toutes calculent pareil.
     *
     * <p>Recensement centré sur la proie : c'est le seul point de vue que toutes les participantes
     * partagent. Centré sur soi, chacune aurait obtenu une liste un peu différente — donc un ordre
     * différent, donc des couloirs qui ne s'emboîtent pas.</p>
     */
    private List<OrcaEntity> gatherLine() {
        AABB around = new AABB(this.prey.position(), this.prey.position()).inflate(PACK_RADIUS);
        List<OrcaEntity> line = new ArrayList<>(this.orca.level().getEntitiesOfClass(
                OrcaEntity.class, around,
                o -> o.isAlive() && !o.isTame() && !o.isBaby() && !o.isVehicle()
                        && !o.isSleeping() && !o.isBeached() && o.isInWater()));
        if (!line.contains(this.orca)) line.add(this.orca);
        line.sort(Comparator.comparing(o -> o.getUUID().toString()));
        return line;
    }

    private LivingEntity findPerchedPrey() {
        AABB box = this.orca.getBoundingBox().inflate(SEARCH_RANGE);
        List<LivingEntity> candidates = this.orca.level().getEntitiesOfClass(
                LivingEntity.class, box, e -> isValidPrey(e) && isPerched(e));

        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity candidate : candidates) {
            double dist = this.orca.distanceToSqr(candidate);
            if (dist < bestDist) {
                bestDist = dist;
                best = candidate;
            }
        }
        return best;
    }

    private boolean isValidPrey(LivingEntity e) {
        if (e == null || !e.isAlive() || e instanceof OrcaEntity) return false;
        if (this.orca.isAlliedTo(e)) return false;
        if (e instanceof Player player) return !player.isSpectator() && !player.isCreative();
        return e instanceof Animal || e instanceof Monster;
    }

    /**
     * Perchée : hors de l'eau, sur de la glace bordée d'eau.
     *
     * <p>Volontairement borné au radeau de glace, entre deux voisinages. Une proie réfugiée sur le
     * rivage relève de {@code OWOrcaBeachingGoal}, qui la traite déjà par l'échouage ; une proie en
     * bateau relève de {@code OWOrcaBoatStrikeGoal}, qui brise la coque sous elle. Élargir la vague
     * à l'un ou l'autre ferait trois comportements pour un seul résultat.</p>
     *
     * <p>Un bateau qui traîne dans le rayon reste bousculé par le déferlement — c'est de la
     * physique, pas une seconde manière de s'en prendre à lui.</p>
     */
    private static boolean isPerched(LivingEntity e) {
        if (e.isInWater()) return false;
        if (e.isPassenger()) return false;

        BlockPos below = BlockPos.containing(e.getX(), e.getY() - 0.15, e.getZ());
        if (!e.level().getBlockState(below).is(BlockTags.ICE)) return false;

        // De l'eau SOUS la glace, et non à côté.
        //
        // La règle d'origine réclamait un bord d'eau à hauteur du pied, ce qui ne décrit qu'un
        // radeau isolé de quelques blocs. Sur une banquise d'un seul tenant — le cas ordinaire d'un
        // océan gelé, et celui où l'on se tient quand on monte sur la glace — la proie n'était
        // jamais reconnue : la vague ne se déclenchait pas du tout. Ce qui compte pour l'orque
        // n'est pas d'avoir un bord à proximité, c'est de pouvoir passer dessous.
        BlockPos.MutableBlockPos cursor = below.mutable();
        for (int i = 0; i < ICE_THICKNESS_LIMIT; i++) {
            cursor.move(Direction.DOWN);
            if (e.level().getFluidState(cursor).is(FluidTags.WATER)) return true;
            if (!e.level().getBlockState(cursor).is(BlockTags.ICE)) return false;
        }
        return false;
    }
}
