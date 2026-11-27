package net.tiew.operationWild.entity.attacks;

import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.piste.OWPisteGraph;
import net.tiew.operationWild.entity.piste.OWPisteGraphs;
import net.tiew.operationWild.entity.piste.OWPisteNode;

/**
 * Passifs débloquables sur la Piste Sauvage (paliers {@link OWPisteNode.Type#ATTACK}).
 * <p>Chaque passif a un id qui correspond à celui déclaré dans
 * {@link net.tiew.operationWild.entity.piste.OWPisteAttacks} (nom / description / image).
 * Un individu possède un passif dès qu'il a débloqué un palier de passif dont l'option choisie
 * pointe sur cet id ({@link #has}).</p>
 */
public final class OWPistePassives {

    private OWPistePassives() {}

    /** Récolte de viande accrue sur les proies tuées (même effet que l'Instinct du Boucher du Kodiak, séparé). */
    public static final int BUTCHER = 0;
    /** Désarmement de la cible après un coup réussi (comme le tigre à l'état sauvage). */
    public static final int DISARM = 1;

    /** Panda roux : l'Orbe de Soin part en salve de deux, espacées d'une demi-seconde. */
    public static final int DOUBLE_RATION = 2;
    /** Panda roux : l'Aura de Vie dure une fois et demie plus longtemps et porte 30 % plus loin. */
    public static final int LONG_TABLE = 3;
    public static final int FEATHER_TEMPEST = 4;
    public static final int GENTLE_BOND = 5;
    public static final int LONG_TONGUE = 6;
    public static final int SLOW_FADE = 7;
    public static final int TAILWIND = 8;
    public static final int IRON_TALONS = 9;
    public static final int SILK_REACH = 10;
    public static final int FAST_SPOOL = 11;

    public static final int TAILWIND_DELAY_TICKS = 100;
    public static final double TAILWIND_SPEED_FACTOR = 1.2;
    public static final float TAILWIND_MIN_ENERGY_LEFT = 0.5f;
    public static final double IRON_TALONS_SIZE_FACTOR = 1.45;

    public static final float FEATHER_TEMPEST_STOCK_FACTOR = 1.3f;
    public static final float FEATHER_TEMPEST_BURST_TICKS = 20f;
    public static final float FEATHER_TEMPEST_DAMAGE_DIVISOR = 2f;
    public static final float GENTLE_BOND_DONOR_FACTOR = 0.5f;
    public static final float GENTLE_BOND_HEAL_FACTOR = 1.25f;

    /** Multiplicateur de viande crue lâchée par les proies (séparé de la valeur innée du Kodiak). */
    public static final float BUTCHER_MULTIPLIER = 1.5f;
    /** Probabilité de désarmement : 1 chance sur {@value} (identique à l'état sauvage du tigre). */
    public static final int DISARM_CHANCE = 7;

    /** Délai entre les deux orbes de la salve, en ticks : un quart de seconde. */
    /**
      * Retard du second en-cas sur le premier : exactement la duree du geste de lancer.
      *
      * <p>Cale la-dessus, le second jet n'a besoin d'AUCUNE animation propre — c'est la premiere,
      * rejouee telle quelle une fois terminee. Un ecart plus court tranchait le premier geste en
      * cours de route ; un geste sur mesure pour deux lancers dupliquait sans raison ce que le
      * lancer ordinaire savait deja faire.</p>
      */
    public static final int DOUBLE_RATION_DELAY_TICKS =
            OWAttacksConstants.RedPanda.HEAL_SNACK_THROW_TICKS;
    /** Allongement de l'aura et de son rayon quand le palier est pris. */
    public static final float LONG_TABLE_DURATION_FACTOR = 1.5f;
    public static final float LONG_TABLE_RADIUS_FACTOR = 1.3f;

    /** DEBUG temporaire : trace la résolution des passifs dans le log. */
    public static boolean DEBUG = false;
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger("OWPistePassives");

    /** {@code true} si {@code entity} a débloqué le passif {@code passiveId} via sa Piste. */
    public static boolean has(OWEntity entity, int passiveId) {
        if (entity == null) return false;
        OWPisteGraph graph = OWPisteGraphs.forEntity(entity);
        if (graph == null) {
            if (DEBUG) LOG.info("[PISTE] has({}): pas de graphe pour {}", passiveId, entity.getClass().getSimpleName());
            return false;
        }
        boolean result = false;
        StringBuilder dbg = DEBUG ? new StringBuilder() : null;
        for (int nodeId : entity.getPisteUnlockedNodes()) {
            OWPisteNode node = graph.get(nodeId);
            if (node == null) continue;
            if (node.getType() != OWPisteNode.Type.ATTACK) continue;
            int chosen = entity.getPisteChoice(nodeId);
            if (dbg != null) dbg.append(" [node=").append(nodeId).append(" attackIds=").append(node.getAttackIds())
                    .append(" chosen=").append(chosen).append("]");
            if (chosen >= 0 && chosen < node.getAttackIds().size()
                    && node.getAttackIds().get(chosen) == passiveId) {
                result = true;
            }
        }
        if (DEBUG) LOG.info("[PISTE] has(passiveId={}) sur {} → {} | unlocked={} choices='{}' ATTACK:{}",
                passiveId, entity.getClass().getSimpleName(), result,
                entity.getPisteUnlockedNodes(), entity.getPisteChoicesRaw(), dbg);
        return result;
    }
}
