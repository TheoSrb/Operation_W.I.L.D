package net.tiew.operationWild.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.resources.ResourceLocation;

/**
 * Implémenté par les modèles d'entités qui portent une hampe et une toile de drapeau
 * ({@code mainFlag} / {@code flag} dans Blockbench), sur lesquelles
 * {@link net.tiew.operationWild.entity.client.layer.OWTribeFlagLayer} vient caler la bannière
 * de la tribu de l'entité.
 *
 * <p><b>Contrat</b> : les os du drapeau doivent être <b>masqués par défaut</b> (dès le
 * constructeur du modèle), de sorte qu'aucune passe de rendu de l'entité ne les dessine — ils
 * échantillonneraient sinon la texture du pelage. Seul {@link #renderTribeFlagPole} les dessine,
 * avec {@link #tribeFlagPoleTexture()} et pour ce seul sous-arbre : la texture de hampe et celle
 * de l'entité se recouvrant dans l'atlas, rendre le modèle entier avec l'une tacherait l'autre.</p>
 *
 * <p>Un modèle dont la définition ne contient pas ces os (ex : un skin de remplacement au
 * maillage différent) renvoie {@code false} à {@link #hasTribeFlag()} et est simplement ignoré.</p>
 */
public interface OWFlagModel {

    /**
     * Emplacement de la toile dans l'espace local de l'os {@code flag}, en <b>pixels modèle</b>.
     * La toile est un quad d'épaisseur nulle dans le plan YZ : elle est accrochée à la hampe par
     * son bord vertical (côté {@code poleZ}) et s'étend vers l'arrière sur {@code length}.
     *
     * @param topY   Y du bord haut de la toile (Y modèle : vers le bas = positif)
     * @param height hauteur de la toile, le long de l'axe Y
     * @param poleZ  Z du bord accroché à la hampe
     * @param length longueur disponible de la toile, le long de l'axe Z
     */
    record Anchor(float topY, float height, float poleZ, float length) {}

    /** {@code true} si ce modèle possède réellement les os du drapeau. */
    boolean hasTribeFlag();

    /**
     * Dessine <b>uniquement</b> la hampe (sous-arbre du porte-drapeau), dans l'état animé de la
     * frame courante. Aucun autre os du modèle ne doit être émis dans {@code buffer}.
     */
    void renderTribeFlagPole(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay);

    /**
     * Empile sur {@code poseStack} toute la chaîne de transformations menant à l'espace local
     * de l'os {@code flag} (racine → … → mainFlag → flag), dans l'état animé de la frame courante.
     * L'unité de l'espace obtenu est le <b>bloc</b> (comme tout espace d'os), pas le pixel modèle.
     */
    void translateToTribeFlag(PoseStack poseStack);

    /** Texture ne contenant que la hampe, tout le reste transparent. */
    ResourceLocation tribeFlagPoleTexture();

    /** Rectangle de la toile, dans l'espace local de l'os {@code flag}. */
    Anchor tribeFlagAnchor();
}
