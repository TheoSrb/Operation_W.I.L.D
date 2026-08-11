package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.client.model.ElephantModel;
import org.joml.Vector3f;

/**
 * Panache de gouttes de la trompe, ancré sur la <b>pointe réellement dessinée</b>.
 *
 * <p>La position n'est pas déduite des angles synchronisés mais lue sur la pile de transformations
 * de l'image en cours, en rejouant la chaîne d'os jusqu'au bout du second segment. C'est la seule
 * façon de faire suivre les gouttes à une trompe qui ondule : toute autre méthode les décalerait de
 * tout ce que les animations clés ajoutent par-dessus la visée, et d'un tick entier entre deux
 * mises à jour réseau.</p>
 *
 * <p>La matrice de rendu place la pointe en coordonnées <i>relatives à la caméra</i> : on y ajoute
 * la position de celle-ci pour retrouver le monde, seul repère qu'accepte le gestionnaire de
 * particules.</p>
 *
 * <p><b>Multijoueur</b> : l'état de trompe est synchronisé sur l'entité, donc chaque client voit le
 * jet de chaque éléphant, qu'il le pilote ou non. Le rendu, lui, reste purement local.</p>
 */
public class ElephantTrunkSprayLayer extends RenderLayer<ElephantEntity, ElephantModel<ElephantEntity>> {

    // Émissions par TICK et non par image : sans ce cadrage, un joueur à 200 images par seconde
    // recevrait trois fois plus d'eau qu'un joueur à 60, et le panache changerait de densité avec
    // la fréquence d'affichage.
    // Un faisceau élargi doit être rempli à proportion, sinon il se lit comme un jet qui MAIGRIT :
    // la même eau étalée sur une section trois fois plus large paraît trois fois plus rare.
    private static final int JET_PER_TICK  = 88;  // les gouttes lancées, qui parcourent l'arc
    private static final int DRIP_PER_TICK = 7;   // ce qui coule le long de la trompe

    /**
     * Largeur du jet à la sortie de trompe.
     *
     * <p>Déduite du rayon de dégâts, comme la dispersion ci-dessous : le rendu ne lisait plus
     * {@code WATER_SPRAY_RADIUS} depuis que les gouttes partent toutes de la buse, si bien
     * qu'élargir la zone touchée ne changeait plus rien à ce qu'on voyait. Les deux sont désormais
     * liés par construction.</p>
     */
    private static final double NOZZLE_RADIUS = ElephantEntity.sprayRadiusAt(0) * 0.55;

    /**
     * Vitesse d'écartement latéral, calibrée pour que le panache atteigne exactement le rayon de
     * dégâts au bout de sa course : une goutte lancée à {@code v} met {@code s / v} ticks pour
     * parcourir {@code s} blocs, et dérive donc de {@code u × s / v} sur le côté.
     */
    private static final double SPREAD_SPEED =
            (ElephantEntity.sprayRadiusAt(OWAttacksConstants.Elephant.WATER_SPRAY_RANGE)
                    - ElephantEntity.sprayRadiusAt(0))
                    * OWAttacksConstants.Elephant.WATER_SPRAY_LAUNCH_SPEED
                    / OWAttacksConstants.Elephant.WATER_SPRAY_RANGE;

    public ElephantTrunkSprayLayer(RenderLayerParent<ElephantEntity, ElephantModel<ElephantEntity>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ElephantEntity elephant,
                       float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {

        boolean spraying = elephant.isSprayingWater();
        boolean filling = elephant.isFillingTrunk();
        if (!spraying && !filling) return;

        Level level = elephant.level();
        if (!level.isClientSide()) return;

        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        ElephantModel<ElephantEntity> model = this.getParentModel();
        if (model == null) return;

        // Pointe ET axe sont lus sur la même matrice : le jet part donc exactement dans le sens où
        // la trompe est dessinée cette image-ci, ondulation comprise. Une direction prise ailleurs
        // (le regard, par exemple) donnerait un panache qui part de travers dès que la trompe bouge.
        poseStack.pushPose();
        model.translateToTrunkTip(poseStack);
        Vector3f base = new Vector3f(0f, 0f, 0f);
        Vector3f tipLocal = new Vector3f(0f, ElephantModel.TRUNK_TIP_LENGTH / 16f, 0f);
        poseStack.last().pose().transformPosition(base);
        poseStack.last().pose().transformPosition(tipLocal);
        poseStack.popPose();

        Vec3 tip = new Vec3(camera.x + tipLocal.x(), camera.y + tipLocal.y(), camera.z + tipLocal.z());
        Vec3 axis = new Vec3(tipLocal.x() - base.x(), tipLocal.y() - base.y(), tipLocal.z() - base.z());
        if (axis.lengthSqr() < 1.0E-6) return;

        // Relevé AVANT le cadencement des particules, et à chaque image : c'est cette mesure qui
        // permet à l'aspiration de corriger sa visée sur la position RÉELLE de la pointe, plutôt
        // que sur une estimation qui ignore l'échelle du modèle et la courbure de la trompe.
        elephant.clientTrunkTip = tip;

        if (elephant.lastSprayParticleTick == elephant.tickCount) return;
        elephant.lastSprayParticleTick = elephant.tickCount;
        axis = axis.normalize();

        if (filling) {
            spawnSuction(level, tip);
            return;
        }

        spawnJet(level, tip, axis);
    }

    /**
     * Tout part de la <b>trompe</b>, et rien d'autre.
     *
     * <p>L'arc n'est plus dessiné : il est parcouru. Chaque goutte naît à la pointe avec sa vitesse
     * de sortie, puis retombe d'elle-même le long de la parabole. La forme du jet n'est donc que la
     * trace de gouttes qui voyagent, et non un chapelet repeint au même endroit à chaque tick —
     * c'est ce qui manquait pour qu'on voie l'eau <i>aller</i> quelque part.</p>
     *
     * <p>Il n'y a du même coup plus rien à borner à l'obstacle : les gouttes ont leur propre
     * collision et s'arrêtent d'elles-mêmes sur ce qu'elles rencontrent.</p>
     */
    private void spawnJet(Level level, Vec3 tip, Vec3 dir) {
        var random = level.getRandom();
        double speed = OWAttacksConstants.Elephant.WATER_SPRAY_LAUNCH_SPEED;

        for (int i = 0; i < JET_PER_TICK; i++) {
            // Émission répartie sur le trajet du tick écoulé : sans cela les gouttes naîtraient
            // toutes au même point et le jet avancerait par paquets bien visibles.
            double lead = speed * random.nextDouble();

            // Une même direction latérale sert à la position ET à la vitesse : chaque goutte
            // s'écarte donc franchement de l'axe, au lieu de partir de travers puis de revenir.
            // L'exposant tasse légèrement le tirage vers le centre pour garder un cœur dense.
            Vec3 perp = perpendicular(random, dir);
            double offset = Math.pow(random.nextDouble(), 0.7);

            Vec3 at = tip.add(dir.scale(lead)).add(perp.scale(NOZZLE_RADIUS * offset));
            Vec3 v = dir.scale(speed * (0.92 + 0.16 * random.nextDouble()))
                    .add(perp.scale(SPREAD_SPEED * offset));

            level.addParticle(net.tiew.operationWild.particle.OWParticles.WATER_JET_PARTICLE.get(),
                    at.x, at.y, at.z, v.x, v.y, v.z);
        }

        // ── Ce qui coule le long de la trompe ─────────────────────────────────
        for (int i = 0; i < DRIP_PER_TICK; i++) {
            Vec3 at = scatter(random, tip, 0.18);
            level.addParticle(ParticleTypes.FALLING_WATER, at.x, at.y, at.z, 0, 0, 0);
        }
    }

    private void spawnSuction(Level level, Vec3 tip) {
        var random = level.getRandom();
        for (int i = 0; i < 5; i++) {
            Vec3 at = scatter(random, tip.add(0, 0.1, 0), 0.35);
            // Aspirées VERS la trompe : la vitesse pointe de la goutte vers la pointe.
            Vec3 pull = tip.subtract(at).scale(0.35);
            level.addParticle(ParticleTypes.FISHING, at.x, at.y, at.z, pull.x, pull.y, pull.z);
        }
        Vec3 drip = scatter(random, tip, 0.2);
        level.addParticle(ParticleTypes.FALLING_WATER, drip.x, drip.y, drip.z, 0, 0, 0);
    }

    private static Vec3 scatter(net.minecraft.util.RandomSource random, Vec3 origin, double radius) {
        return origin.add(
                Mth.nextDouble(random, -radius, radius),
                Mth.nextDouble(random, -radius, radius),
                Mth.nextDouble(random, -radius, radius));
    }

    /**
     * Tire une direction unitaire au hasard, perpendiculaire à l'axe du jet.
     *
     * <p>Un simple bruit sur les trois coordonnées donnait un pavé et non un cône : il écartait la
     * goutte le long de l'axe autant que sur les côtés, si bien que le jet paraissait à la fois
     * étroit et flou. Ici l'écart est strictement latéral.</p>
     */
    private static Vec3 perpendicular(net.minecraft.util.RandomSource random, Vec3 dir) {
        Vec3 reference = Math.abs(dir.y) > 0.95 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = dir.cross(reference).normalize();
        Vec3 up = right.cross(dir).normalize();

        double angle = random.nextDouble() * Math.PI * 2.0;
        return right.scale(Math.cos(angle)).add(up.scale(Math.sin(angle)));
    }
}
