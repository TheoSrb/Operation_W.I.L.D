package net.tiew.operationWild.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.entity.OWEntity;

/**
 * Rattrape le retard du cavalier sur l'os qui le porte.
 *
 * <p><b>Le problème.</b> La place d'un cavalier est calculée par {@code positionRider}, appelé une
 * fois par <b>tick</b> — vingt fois par seconde. L'os qui la définit, lui, est animé à chaque
 * <b>image</b>, et son mouvement est relevé au moment du rendu du modèle de la monture. Le siège
 * s'appuie donc sur une animation vieille d'un tick, puis le moteur interpole en ligne droite entre
 * deux positions de tick un mouvement qui, lui, oscille. Deux erreurs se cumulent, et elles se
 * voient d'autant plus que la bête est grande et que le siège est loin de son centre : sur l'orque,
 * quelques centimètres de bois deviennent un flottement visible.</p>
 *
 * <p><b>Le rattrapage.</b> À l'image, on redemande à la monture où serait ce siège <i>maintenant</i>,
 * avec les valeurs d'animation de la frame courante, et on translate le modèle du cavalier de
 * l'écart avec l'endroit où le moteur s'apprête à le dessiner. La position réelle de l'entité n'est
 * pas touchée : rien ne change côté collisions, réseau ou logique de jeu, c'est une correction
 * purement visuelle.</p>
 *
 * <p>Le calcul est repris de {@code positionRider} de chaque espèce, appelé avec une fonction qui se
 * contente de <b>noter</b> la position au lieu de déplacer le cavalier. Aucune formule de siège
 * n'est donc recopiée ici, et une espèce ajoutée plus tard en bénéficie sans rien écrire.</p>
 *
 * <p><b>Limite connue.</b> Les valeurs d'animation datent du dernier rendu du modèle de la monture.
 * L'ordre de rendu des entités n'étant pas garanti, elles peuvent avoir une image de retard si le
 * cavalier passe avant sa monture — au lieu d'un tick entier, soit environ trois images de moins à
 * 60 par seconde.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class OWRiderSmoothing {

    private OWRiderSmoothing() {}

    /**
     * Au-delà de cet écart, on ne corrige rien : un tel bond ne vient pas d'un décalage d'animation
     * mais d'un changement de siège, d'une téléportation ou d'une monte qui vient d'avoir lieu.
     * Le rattraper d'un coup ferait bien plus laid que le laisser passer.
     */
    private static final double MAX_CORRECTION_SQR = 4.0D;

    /**
     * Interrupteur de diagnostic. À {@code false}, plus aucune correction n'est appliquée et le
     * cavalier retombe sur le placement d'origine du moteur. Si les saccades disparaissent, elles
     * viennent d'ici ; si elles restent, elles viennent des rotations.
     */
    public static final boolean ENABLED = true;

    /**
     * Constante de temps du second étage de lissage, en secondes. Ne traite que le résidu : le
     * battement d'alternance, lui, est annulé en amont par la moyenne à deux prises. Assez court pour
     * que la correction colle encore aux gestes rapides d'un combo.
     */
    private static final double SMOOTHING_TAU_SECONDS = 0.045D;

    /**
     * Correction en cours, prise brute précédente, et instant du dernier calcul, par cavalier.
     * Trois valeurs lissées, trois valeurs brutes.
     */
    private static final Map<UUID, double[]> SMOOTHED = new HashMap<>();
    private static final Map<UUID, double[]> PREVIOUS_RAW = new HashMap<>();
    private static final Map<UUID, Long> SMOOTHED_AT = new HashMap<>();

    /** Oublie l'historique d'un cavalier : à appeler quand il descend ou change de monture. */
    public static void forget(UUID rider) {
        SMOOTHED.remove(rider);
        PREVIOUS_RAW.remove(rider);
        SMOOTHED_AT.remove(rider);
    }

    /**
     * Translation à appliquer au modèle du cavalier, ou {@code null} s'il n'y a rien à corriger.
     * Exprimée en coordonnées du monde, à poser avant toute rotation.
     */
    public static Vec3 seatCorrection(Entity rider, OWEntity mount, float partialTick) {
        if (!ENABLED) return null;

        // Le calcul de siège fait tourner son décalage par le lacet de la monture. Celui-ci vaut, au
        // moment où on l'interroge, la valeur du dernier TICK — alors que la bête est dessinée à un
        // lacet interpolé. Tant qu'elle vire, la place calculée pointe donc légèrement à côté de la
        // selle visible, et le cavalier glisse d'avant en arrière : assez pour lui enfoncer la tête
        // dans le siège. On prête à la monture son lacet d'affichage le temps du calcul, puis on le
        // lui rend — rien de tout cela ne doit survivre à cette mesure.
        float savedBodyYaw = mount.yBodyRot;
        float savedYaw = mount.getYRot();

        // Le lacet d'affichage se déduit du CORPS et de lui seul : yRotO est réécrit chaque tick par
        // OWEntity.smoothRotation, donc l'interpoler ne rendrait que la valeur de tick. On applique à
        // yRot le même écart qu'au corps — exact tant que les deux suivent le cavalier, sans effet
        // quand le corps ne tourne pas.
        float preciseBody = Mth.rotLerp(partialTick, mount.yBodyRotO, savedBodyYaw);
        float bodyDelta = Mth.wrapDegrees(preciseBody - savedBodyYaw);
        mount.yBodyRot = preciseBody;
        mount.setYRot(savedYaw + bodyDelta);

        Vec3 seat;
        try {
            seat = mount.captureSeatPosition(rider);
        } finally {
            mount.yBodyRot = savedBodyYaw;
            mount.setYRot(savedYaw);
        }
        if (seat == null) return null;

        // La monture elle-même est dessinée interpolée : on ramène le siège, calculé sur sa position
        // de tick, jusqu'à celle réellement affichée.
        double mx = Mth.lerp(partialTick, mount.xOld, mount.getX()) - mount.getX();
        double my = Mth.lerp(partialTick, mount.yOld, mount.getY()) - mount.getY();
        double mz = Mth.lerp(partialTick, mount.zOld, mount.getZ()) - mount.getZ();

        // Là où le moteur s'apprête à poser le cavalier.
        double px = Mth.lerp(partialTick, rider.xOld, rider.getX());
        double py = Mth.lerp(partialTick, rider.yOld, rider.getY());
        double pz = Mth.lerp(partialTick, rider.zOld, rider.getZ());

        double dx = seat.x + mx - px;
        double dy = seat.y + my - py;
        double dz = seat.z + mz - pz;

        if (dx * dx + dy * dy + dz * dz > MAX_CORRECTION_SQR) {
            forget(rider.getUUID());
            return null;
        }
        return smooth(rider.getUUID(), dx, dy, dz);
    }

    private static Vec3 smooth(UUID id, double dx, double dy, double dz) {
        long now = System.nanoTime();
        double[] previous = SMOOTHED.get(id);
        double[] previousRaw = PREVIOUS_RAW.get(id);
        Long previousAt = SMOOTHED_AT.get(id);
        SMOOTHED_AT.put(id, now);

        if (previous == null || previousRaw == null || previousAt == null) {
            SMOOTHED.put(id, new double[] { dx, dy, dz });
            PREVIOUS_RAW.put(id, new double[] { dx, dy, dz });
            return new Vec3(dx, dy, dz);
        }

        // Premier étage : moyenne des deux dernières prises BRUTES.
        //
        // Le défaut visé n'est pas un bruit quelconque, c'est une alternance à deux états — l'os de
        // l'image courante une image sur deux, celui de l'image précédente le reste du temps, selon
        // que le tri par distance a placé la monture avant ou après son cavalier. Sur une suite
        // A, B, A, B, la moyenne de deux prises consécutives vaut (A+B)/2 à chaque image : constante,
        // le battement disparaît exactement plutôt que d'être atténué. Un passe-bas seul, lui, en
        // laisse toujours passer une part — d'où les saccades résiduelles. Retard payé : une
        // demi-image, contre trois pour un filtre assez agressif pour obtenir le même résultat.
        double rawX = dx, rawY = dy, rawZ = dz;
        dx = (dx + previousRaw[0]) * 0.5D;
        dy = (dy + previousRaw[1]) * 0.5D;
        dz = (dz + previousRaw[2]) * 0.5D;
        previousRaw[0] = rawX; previousRaw[1] = rawY; previousRaw[2] = rawZ;

        // Une image perdue (chargement, pause) ne doit pas traîner une correction périmée derrière
        // elle : au-delà d'un quart de seconde, on repart de la valeur mesurée.
        double dt = (now - previousAt) / 1.0E9D;
        if (dt <= 0.0D || dt > 0.25D) {
            SMOOTHED.put(id, new double[] { rawX, rawY, rawZ });
            previousRaw[0] = rawX; previousRaw[1] = rawY; previousRaw[2] = rawZ;
            return new Vec3(rawX, rawY, rawZ);
        }

        // Second étage : passe-bas sur le résidu.

        double alpha = 1.0D - Math.exp(-dt / SMOOTHING_TAU_SECONDS);
        double sx = previous[0] + (dx - previous[0]) * alpha;
        double sy = previous[1] + (dy - previous[1]) * alpha;
        double sz = previous[2] + (dz - previous[2]) * alpha;
        previous[0] = sx; previous[1] = sy; previous[2] = sz;
        return new Vec3(sx, sy, sz);
    }
}
