package net.tiew.operationWild.entity.client.render.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.tiew.operationWild.entity.client.model.OWFlagModel;
import net.tiew.operationWild.team.OWTeam;
import net.tiew.operationWild.team.OWTeamBannerShape;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Animation et rendu de la bannière de tribu portée par une entité, indépendamment du layer qui
 * l'appelle : {@link net.tiew.operationWild.entity.client.layer.OWTribeFlagLayer} pour les entités
 * d'un seul tenant, {@link net.tiew.operationWild.entity.client.layer.BoaTailFlagLayer} pour le boa
 * dont le porte-drapeau appartient à un segment de queue rendu comme une entité à part.
 *
 * <p>La toile est traitée en deux couches superposées :</p>
 * <ul>
 *   <li>sa <b>ligne moyenne</b> est une chaîne de pendules — chaque segment poursuit l'orientation
 *       du précédent via un ressort amorti, jamais une consigne globale. Un coup de volant n'agit
 *       donc que sur le segment attaché à la hampe, les suivants le rattrapent l'un après l'autre,
 *       et le mouvement se propage en fouet jusqu'à la pointe au lieu de faire pivoter la toile
 *       d'un bloc. Deux chaînes tournent en parallèle : le <b>lacet</b> (virages) et le
 *       <b>tangage</b> (retombée sous son poids, allègement à la course, inertie aux sauts) ;</li>
 *   <li>l'<b>ondulation</b> proprement dite vient s'y superposer, confiée à
 *       {@link OWWavingBannerConsumer}.</li>
 * </ul>
 *
 * <p><b>Sous l'eau</b>, le milieu change et l'animation avec lui : ondes ralenties mais plus amples,
 * oscillations qui s'éteignent sans rebondir, virages amortis, retombée remplacée par une flottaison
 * vers la surface, et surtout un bercement permanent du courant — immergée, une étoffe n'est jamais
 * immobile. La bascule suit l'immersion de la <i>toile</i>, pas celle de la monture.</p>
 *
 * <p><b>Multijoueur</b> : rien n'est calculé côté serveur, l'état est purement local et reconstruit
 * à la volée. Le rendu est donc identique en solo et en serveur.</p>
 */
public final class OWTribeFlagRenderer {

    private OWTribeFlagRenderer() {}

    /** Débattement de l'extrémité libre, en unités bannière : au repos, puis vent au maximum. */
    private static final float AMPLITUDE_CALM = 5.5f;
    private static final float AMPLITUDE_WIND = 13.0f;
    /** Pas de tesselation de la toile, en unités bannière (dégradé quand l'entité est loin). */
    private static final float STEP_NEAR = 3.5f;
    private static final float STEP_FAR = 9.0f;
    private static final double LOD_DISTANCE = 20.0;

    /** Vitesse de l'onde, en radians par tick : au repos, puis vent au maximum. */
    private static final float WAVE_SPEED_CALM = 0.22f;
    private static final float WAVE_SPEED_WIND = 0.42f;
    /** Constante de temps du lissage du vent, en ticks : évite la saute au départ en sprint. */
    private static final float WIND_SMOOTHING = 0.12f;

    /**
     * Nombre de segments articulés de la ligne moyenne. Six suffisent à lire la propagation sans
     * alourdir l'intégration ; la toile, elle, reste tesselée bien plus finement.
     */
    private static final int SEGMENTS = 6;

    /**
     * Débattement d'un segment en lacet, en radians, et gain sur la vitesse de rotation du corps.
     * Cumulé sur toute la chaîne, un virage serré couche donc la toile d'environ 40° — au-delà,
     * vers l'intérieur du virage, elle commence à traverser l'arrière du corps de la monture.
     */
    private static final float SWING_MAX = 0.72f;
    private static final float SWING_GAIN = 0.105f;

    /** Ressort de la base (attache à la hampe) : réagit vite, le tissu y est tendu. */
    private static final float BASE_STIFFNESS = 0.95f;
    private static final float BASE_DAMPING = 0.75f;
    /** Ressort entre segments : plus mou, c'est lui qui crée le retard de proche en proche. */
    private static final float CHAIN_STIFFNESS = 0.55f;
    private static final float CHAIN_DAMPING = 0.72f;
    /**
     * Pliure maximale entre deux segments voisins, en radians. Le tissu résiste aux plis cassants,
     * et surtout : sans cette butée, les dépassements de ressort se cumulent d'un segment au
     * suivant et la pointe finit par se replier sur la toile.
     *
     * <p>Le tangage est un peu plus permissif que le lacet (~19° contre ~16°) : le drapeau se
     * cabre et plonge davantage à la verticale, sans pour autant élargir le fouet horizontal.</p>
     */
    private static final float MAX_BEND_YAW = 0.28f;
    private static final float MAX_BEND_PITCH = 0.34f;
    /** Marge de dépassement tolérée à la base, au-delà de la consigne de virage. */
    private static final float BASE_OVERSHOOT = 1.2f;
    /** Pas d'intégration maximal, en ticks : au-delà, on sous-échantillonne pour rester stable. */
    private static final float MAX_SUBSTEP = 0.5f;
    private static final int MAX_SUBSTEPS = 4;

    // ── Sous l'eau ───────────────────────────────────────────────────────────────
    // L'eau est ~800× plus dense que l'air : le tissu y perd tout claquement, ses ondes s'allongent
    // et se ralentissent, ses oscillations s'éteignent sans rebondir, et la poussée d'Archimède
    // remplace la retombée par une lente dérive vers la surface.
    private static final float WATER_WAVE_SPEED = 0.55f;
    /** L'eau porte le tissu : l'ondulation y est plus <b>ample</b>, quoique bien plus lente. */
    private static final float WATER_AMPLITUDE = 1.35f;
    private static final float WATER_FREQUENCY = 0.55f;
    /** Facteurs sur les ressorts : plus amortis (aucun rebond), sans pour autant figer la toile. */
    private static final float WATER_BASE_DAMPING = 1.15f;
    private static final float WATER_CHAIN_DAMPING = 1.25f;
    private static final float WATER_CHAIN_STIFFNESS = 0.90f;
    /**
     * Dérive du courant, en radians. Hors de l'eau, la chaîne n'est mise en mouvement que par les
     * virages et les sauts : au repos elle est immobile, ce qui est juste — un drapeau pendouille.
     * Sous l'eau, en revanche, le tissu n'est jamais au repos, le courant le berce en permanence.
     */
    private static final float WATER_SWAY = 0.30f;
    /** L'eau freine aussi le fouet des virages. */
    private static final float WATER_SWING = 0.60f;
    /** Flottaison : angle par segment vers le haut (l'inverse d'une retombée). */
    private static final float WATER_FLOAT_PER_SEGMENT = -0.035f;
    /** Constante de temps de la transition air ↔ eau, en ticks. */
    private static final float SUBMERSION_SMOOTHING = 0.15f;

    /** Retombée par segment à l'arrêt, en radians (cumulée : la toile décrit un arc). */
    private static final float DROOP_PER_SEGMENT = 0.09f;
    /** Sensibilité du tangage à la vitesse verticale de l'entité, et débattement associé. */
    private static final float LIFT_GAIN = 1.4f;
    private static final float LIFT_MAX = 0.70f;

    /**
     * État d'animation, indexé par l'entité qui porte réellement la hampe. Purement client et local :
     * il est reconstruit à la volée et disparaît avec l'entité (clés faibles).
     */
    private static final Map<Entity, FlagState> STATES = new WeakHashMap<>();

    // ── Point d'entrée ───────────────────────────────────────────────────────────

    /**
     * Dessine la hampe et sa toile animée.
     *
     * @param carrier  entité qui porte physiquement la hampe — elle sert de clé d'état, et c'est sa
     *                 position qui décide de l'immersion et du niveau de détail. Ce n'est pas
     *                 forcément la monture : chez le boa c'est un segment de queue.
     * @param team     tribu dont la bannière est arborée
     * @param model    modèle porteur, déjà animé pour cette frame
     * @param turnRate vitesse de rotation du porteur, en degrés par tick
     * @param wind     agitation du porteur, 0 (immobile) à 1 (pleine course)
     */
    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                              Entity carrier, OWTeam team, OWFlagModel model,
                              float turnRate, float wind, float ageInTicks) {
        if (team == null || model == null || !model.hasTribeFlag()) return;

        FlagState state = advance(carrier, turnRate, wind, ageInTicks);

        VertexConsumer pole = bufferSource.getBuffer(RenderType.entityCutoutNoCull(model.tribeFlagPoleTexture()));
        model.renderTribeFlagPole(poseStack, pole, packedLight, OverlayTexture.NO_OVERLAY);

        renderCloth(poseStack, bufferSource, packedLight, carrier, model, team, state);
    }

    // ── Animation ────────────────────────────────────────────────────────────────

    /**
     * Fait avancer l'animation d'une image.
     *
     * <p>La phase est <b>intégrée</b> (<code>phase += Δt × vitesse</code>) et non recalculée en
     * <code>âge × vitesse</code> : avec un âge de plusieurs milliers de ticks, la moindre variation
     * de vitesse — passage à la course, par exemple — ferait sauter la phase de plusieurs tours
     * d'un coup et le drapeau se déchirerait visuellement. Le vent est lissé pour la même raison.</p>
     */
    private static FlagState advance(Entity carrier, float turnRate, float wind, float ageInTicks) {
        FlagState s = STATES.computeIfAbsent(carrier, e -> new FlagState());

        // Δt nul si la frame est déjà comptée (l'entité peut être rendue plusieurs fois par image :
        // passe fantôme, aperçu d'écran…), borné si l'entité revient d'un déchargement.
        float dt = Float.isNaN(s.lastAge) ? 0f : Mth.clamp(ageInTicks - s.lastAge, 0f, 2f);
        s.lastAge = ageInTicks;

        s.wind += (Mth.clamp(wind, 0f, 1f) - s.wind) * Math.min(1f, dt * WIND_SMOOTHING);

        // Immersion de la toile elle-même, pas de la monture : un crocodile à demi immergé garde son
        // drapeau au sec au-dessus de l'eau, et celui-ci ne s'alourdit qu'en passant sous la surface.
        float targetSubmersion = isFlagInWater(carrier) ? 1f : 0f;
        s.submerged += (targetSubmersion - s.submerged) * Math.min(1f, dt * SUBMERSION_SMOOTHING);
        float water = Mth.clamp(s.submerged, 0f, 1f);

        s.phase += dt * Mth.lerp(s.wind, WAVE_SPEED_CALM, WAVE_SPEED_WIND)
                * Mth.lerp(water, 1f, WATER_WAVE_SPEED);

        // Sous l'eau, les ressorts deviennent nettement plus visqueux : plus aucun rebond.
        s.baseDamping = BASE_DAMPING * Mth.lerp(water, 1f, WATER_BASE_DAMPING);
        s.chainDamping = CHAIN_DAMPING * Mth.lerp(water, 1f, WATER_CHAIN_DAMPING);
        s.chainStiffness = CHAIN_STIFFNESS * Mth.lerp(water, 1f, WATER_CHAIN_STIFFNESS);

        // Bercement du courant : deux périodes lentes et volontairement incommensurables (0,55 et
        // 0,37) pour que le lacet et le tangage ne repassent jamais ensemble au même endroit.
        // Le décalage par entité évite que deux montures voisines ondulent à l'unisson.
        float swayPhase = s.phase + carrier.getId() * 1.31f;
        float sway = water * WATER_SWAY;
        float yawSway = sway * Mth.sin(swayPhase * 0.55f);
        float pitchSway = sway * 0.6f * Mth.sin(swayPhase * 0.37f + 1.3f);

        // Lacet : le drapeau traîne derrière la monture qui vire, vers l'extérieur du virage.
        float yawTarget = Mth.clamp(-turnRate * SWING_GAIN * Mth.lerp(water, 1f, WATER_SWING),
                -SWING_MAX, SWING_MAX) + yawSway;

        // Tangage : la base encaisse l'inertie verticale — la monture s'élève, le tissu reste en
        // arrière et plonge ; elle retombe, il se relève. (yOld est la position du tick précédent,
        // tenue à jour côté client aussi.)
        float verticalSpeed = (float) (carrier.getY() - carrier.yOld);
        float pitchTarget = Mth.clamp(verticalSpeed * LIFT_GAIN, -LIFT_MAX, LIFT_MAX) + pitchSway;
        // …et le reste de la chaîne retombe sous son poids, d'autant moins que l'entité file vite.
        // Sous l'eau, la poussée d'Archimède l'emporte : la retombée s'inverse en lente flottaison.
        float droop = Mth.lerp(water, DROOP_PER_SEGMENT * (1f - s.wind), WATER_FLOAT_PER_SEGMENT);

        // Intégration sous-échantillonnée : au-delà d'un demi-tick, l'Euler explicite des ressorts
        // diverge (une frame longue ferait exploser la chaîne).
        int steps = Mth.clamp(Mth.ceil(dt / MAX_SUBSTEP), 1, MAX_SUBSTEPS);
        float h = dt / steps;
        for (int i = 0; i < steps; i++) s.step(h, yawTarget, pitchTarget, droop);
        s.resolve();

        return s;
    }

    /**
     * {@code true} si la toile baigne dans l'eau. On échantillonne le fluide au sommet de la boîte
     * de collision du porteur, là où flotte la hampe, et non à ses pieds : c'est la hauteur du
     * drapeau qui décide.
     */
    private static boolean isFlagInWater(Entity carrier) {
        BlockPos pos = BlockPos.containing(carrier.getX(), carrier.getY() + carrier.getBbHeight(), carrier.getZ());
        return carrier.level().getFluidState(pos).is(FluidTags.WATER);
    }

    // ── Rendu de la toile ────────────────────────────────────────────────────────

    /** Dessine la toile ondulante, calée dans le rectangle déclaré par le modèle. */
    private static void renderCloth(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                    Entity carrier, OWFlagModel model, OWTeam team, FlagState state) {
        OWFlagModel.Anchor anchor = model.tribeFlagAnchor();
        // Pixels modèle par pixel bannière : le plus petit des deux rapports, pour tenir dans la
        // toile sans déformation (le reliquat éventuel est laissé libre à la pointe).
        float scale = Math.min(anchor.height() / OWTeamBannerShape.BASE_W,
                               anchor.length() / OWTeamBannerShape.BASE_H);

        // Plus l'entité se déplace, plus le drapeau claque — sauf sous l'eau, où l'ondulation
        // s'assagit et ses ondes s'allongent.
        float water = Mth.clamp(state.submerged, 0f, 1f);
        float amplitude = Mth.lerp(state.wind, AMPLITUDE_CALM, AMPLITUDE_WIND)
                * Mth.lerp(water, 1f, WATER_AMPLITUDE);
        float frequency = Mth.lerp(water, 1f, WATER_FREQUENCY);
        // L'identifiant déphase les entités nées au même tick : deux montures côte à côte
        // n'ondulent jamais à l'unisson.
        float phase = state.phase + carrier.getId() * 0.73f;

        // De loin, une toile finement tesselée ne se distingue plus : on allège la découpe.
        double distSqr = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition()
                .distanceToSqr(carrier.getX(), carrier.getY(), carrier.getZ());
        float step = distSqr > LOD_DISTANCE * LOD_DISTANCE ? STEP_FAR : STEP_NEAR;

        poseStack.pushPose();
        model.translateToTribeFlag(poseStack);

        // L'espace d'un os est en blocs : on repasse en pixels modèle, puis on oriente le repère
        // de la bannière (X vers le bas du monde, Y vers la hampe, Z pour l'ondulation latérale).
        poseStack.scale(1f / 16f, 1f / 16f, 1f / 16f);
        poseStack.translate(0f, anchor.topY(), anchor.poleZ() + OWTeamBannerShape.BASE_H * scale);
        poseStack.mulPose(Axis.ZP.rotationDegrees(90f));
        poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
        poseStack.scale(scale, scale, scale);

        VertexConsumer cloth = bufferSource.getBuffer(RenderType.entityTranslucent(OWTeamBannerShape.TEXTURE));
        OWWavingBannerConsumer waving = new OWWavingBannerConsumer(cloth, amplitude, phase, frequency, step,
                state.offX, state.offY, state.offZ);
        // Pas de calque d'ombrage sur une toile portée : il est peint pour une bannière droite
        // et fige un éclairage qui contredit l'ondulation.
        OWRendererUtils.renderTeamBanner(waving, poseStack.last().pose(), team,
                0f, 0f, OWTeamBannerShape.BASE_W, OWTeamBannerShape.BASE_H, packedLight, false);

        poseStack.popPose();
    }

    // ── État ─────────────────────────────────────────────────────────────────────

    private static final class FlagState {
        /** Phase de l'onde, <b>accumulée</b> image par image. */
        float phase;
        float wind;
        /** Part d'immersion de la toile, lissée : 0 = à l'air libre, 1 = sous l'eau. */
        float submerged;
        float lastAge = Float.NaN;

        /** Coefficients des ressorts pour l'image courante (air ou eau), résolus par advance(). */
        float baseDamping = BASE_DAMPING;
        float chainStiffness = CHAIN_STIFFNESS;
        float chainDamping = CHAIN_DAMPING;

        /** Orientation absolue de chaque segment, et sa vitesse angulaire. */
        final float[] yaw = new float[SEGMENTS], yawVel = new float[SEGMENTS];
        final float[] pitch = new float[SEGMENTS], pitchVel = new float[SEGMENTS];

        /** Décalages cumulés aux extrémités de segment, recalculés à chaque image pour le rendu. */
        final float[] offX = new float[SEGMENTS + 1];
        final float[] offY = new float[SEGMENTS + 1];
        final float[] offZ = new float[SEGMENTS + 1];

        /** Intègre les deux chaînes sur {@code h} ticks. */
        void step(float h, float yawTarget, float pitchTarget, float droop) {
            // Segment 0 : tenu par la hampe, il suit directement la consigne.
            yawVel[0] += (yawTarget - yaw[0]) * BASE_STIFFNESS * h;
            yawVel[0] *= Math.max(0f, 1f - baseDamping * h);
            yaw[0] = Mth.clamp(yaw[0] + yawVel[0] * h,
                    -SWING_MAX * BASE_OVERSHOOT, SWING_MAX * BASE_OVERSHOOT);

            pitchVel[0] += (pitchTarget - pitch[0]) * BASE_STIFFNESS * h;
            pitchVel[0] *= Math.max(0f, 1f - baseDamping * h);
            pitch[0] += pitchVel[0] * h;

            // Segments suivants : chacun poursuit son prédécesseur, le tangage y ajoutant sa part
            // de retombée — d'où un arc, et non une pliure raide.
            for (int i = 1; i < SEGMENTS; i++) {
                yawVel[i] += (yaw[i - 1] - yaw[i]) * chainStiffness * h;
                yawVel[i] *= Math.max(0f, 1f - chainDamping * h);
                yaw[i] += yawVel[i] * h;

                pitchVel[i] += (pitch[i - 1] + droop - pitch[i]) * chainStiffness * h;
                pitchVel[i] *= Math.max(0f, 1f - chainDamping * h);
                pitch[i] += pitchVel[i] * h;
            }
            limitBend(yaw, yawVel, MAX_BEND_YAW);
            limitBend(pitch, pitchVel, MAX_BEND_PITCH);
        }

        /**
         * Borne la pliure de chaque articulation. Une butée atteinte annule la vitesse angulaire du
         * segment : il se cale sur son prédécesseur au lieu de rebondir contre la limite.
         */
        private static void limitBend(float[] angles, float[] velocities, float maxBend) {
            for (int i = 1; i < SEGMENTS; i++) {
                float lo = angles[i - 1] - maxBend, hi = angles[i - 1] + maxBend;
                if (angles[i] < lo) {
                    angles[i] = lo;
                    velocities[i] = 0f;
                } else if (angles[i] > hi) {
                    angles[i] = hi;
                    velocities[i] = 0f;
                }
            }
        }

        /** Déroule la chaîne en décalages cumulés, en unités bannière. */
        void resolve() {
            float segLen = OWTeamBannerShape.BASE_H / (float) SEGMENTS;
            offX[0] = offY[0] = offZ[0] = 0f;
            for (int i = 0; i < SEGMENTS; i++) {
                float cy = Mth.cos(yaw[i]), sy = Mth.sin(yaw[i]);
                float cp = Mth.cos(pitch[i]), sp = Mth.sin(pitch[i]);
                offX[i + 1] = offX[i] + segLen * sp;
                offZ[i + 1] = offZ[i] + segLen * sy * cp;
                // Ce que le segment gagne en inclinaison, il le perd en portée : le tissu ne
                // s'étire pas, sa pointe se rapproche de la hampe.
                offY[i + 1] = offY[i] + segLen * (1f - cy * cp);
            }
        }
    }
}
