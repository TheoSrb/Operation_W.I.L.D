package net.tiew.operationWild.client;

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
     * Translation à appliquer au modèle du cavalier, ou {@code null} s'il n'y a rien à corriger.
     * Exprimée en coordonnées du monde, à poser avant toute rotation.
     */
    public static Vec3 seatCorrection(Entity rider, OWEntity mount, float partialTick) {
        // Le calcul de siège fait tourner son décalage par le lacet de la monture. Celui-ci vaut, au
        // moment où on l'interroge, la valeur du dernier TICK — alors que la bête est dessinée à un
        // lacet interpolé. Tant qu'elle vire, la place calculée pointe donc légèrement à côté de la
        // selle visible, et le cavalier glisse d'avant en arrière : assez pour lui enfoncer la tête
        // dans le siège. On prête à la monture son lacet d'affichage le temps du calcul, puis on le
        // lui rend — rien de tout cela ne doit survivre à cette mesure.
        float savedBodyYaw = mount.yBodyRot;
        float savedYaw = mount.getYRot();
        mount.yBodyRot = Mth.rotLerp(partialTick, mount.yBodyRotO, mount.yBodyRot);
        mount.setYRot(Mth.rotLerp(partialTick, mount.yRotO, mount.getYRot()));

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

        if (dx * dx + dy * dy + dz * dz > MAX_CORRECTION_SQR) return null;
        return new Vec3(dx, dy, dz);
    }
}
