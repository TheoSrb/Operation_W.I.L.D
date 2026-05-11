package net.tiew.operationWild.entity;

import net.minecraft.world.entity.Entity;
import net.tiew.operationWild.entity.OWEntity;

/**
 * Interface à implémenter sur toute entité OW devant afficher un waypoint losange HUD
 * avec distance en mètres, quand son owner s'éloigne.
 * <p>
 * Toutes les méthodes ont une valeur par défaut.
 * Il suffit d'implémenter l'interface et de surcharger uniquement ce que l'on veut personnaliser.
 * </p>
 * Usage : {@code public class MonEntite extends OWEntity implements IOWWaypointEntity { ... }}
 */
public interface IOWWaypointEntity {

    /** Couleur de remplissage du losange, format RRGGBB. */
    default int getWaypointFillColor() {
        if (this instanceof OWEntity e) return e.getEntityColor();
        return 0x1A8FFF;
    }

    /** Couleur de la bordure du losange, format RRGGBB — version très claire de getEntityColor(). */
    default int getWaypointBorderColor() {
        if (this instanceof OWEntity e) {
            int c = e.getEntityColor();
            int r = (c >> 16) & 0xFF;
            int g = (c >> 8)  & 0xFF;
            int b =  c        & 0xFF;
            r = r + (int) ((255 - r) * 0.7f);
            g = g + (int) ((255 - g) * 0.7f);
            b = b + (int) ((255 - b) * 0.7f);
            return (r << 16) | (g << 8) | b;
        }
        return 0xAADDFF;
    }

    /** Couleur du texte de distance et du nom, format RRGGBB. */
    default int getWaypointTextColor() { return 0xFFFFFF; }

    /** Demi-taille du losange en pixels GUI. */
    default int getWaypointIconSize() { return 5; }

    /** Distance max d'affichage en blocs. */
    default int getWaypointMaxDistance() { return 500; }

    /** Distance min en blocs sous laquelle le waypoint se masque. */
    default float getWaypointMinDistance() { return 10.0f; }

    /**
     * Nom affiché sur le waypoint HUD.
     * Par défaut : nom traduit de l'entité via sa clé entity.ow.&lt;nom&gt;,
     * ou son nom custom si défini.
     */
    default String getWaypointName() {
        if (this instanceof Entity entity) return entity.getName().getString();
        return "";
    }

    /** Opacité minimale quand le joueur ne regarde pas dessus [0.0 – 1.0]. */
    default float getWaypointMinOpacity() { return 0.3f; }

    /** Facteur d'échelle de la police pour le texte de distance. */
    default float getWaypointDistanceFontScale() { return 0.5f; }
}
