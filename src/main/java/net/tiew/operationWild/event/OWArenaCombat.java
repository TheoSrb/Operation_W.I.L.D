package net.tiew.operationWild.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.team.OWArenaManager;
import net.tiew.operationWild.team.OWArenaMatch;
import net.tiew.operationWild.worldgen.dimension.OWDimensions;

/**
 * L'arène ne tue personne.
 *
 * <p>Un duel de tribus est une <b>joute</b> : on y met l'adversaire hors de combat, on ne l'abat
 * pas. Une créature vaincue est retirée de la mêlée puis rendue vivante à son propriétaire avec la
 * santé qu'elle avait avant le duel ; un chef venu assister au combat ne peut pas y mourir.</p>
 *
 * <p>C'est un choix de conception, pas un garde-fou : perdre définitivement une créature
 * apprivoisée — élevée sur des heures de jeu — serait un prix sans commune mesure avec l'enjeu
 * d'un match, et personne n'engagerait ses meilleures bêtes.</p>
 *
 * <p>Le verdict n'en souffre pas : {@code knockOut} sort le vaincu des ensembles de vivants, donc
 * le camp vidé perd exactement comme si ses combattants étaient tombés.</p>
 */
@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class OWArenaCombat {

    /** Vrai si l'entité se trouve dans la dimension d'arène. */
    private static boolean inArena(LivingEntity entity) {
        return entity.level().dimension().equals(OWDimensions.ARENA);
    }

    /**
     * Intercepte le coup fatal porté à un combattant : au lieu de mourir, il est mis hors de combat.
     *
     * <p>Annuler la mort plutôt que la laisser survenir évite d'un même geste la perte de la
     * créature, la chute de son âme au sol et le butin qu'elle laisserait dans l'arène.</p>
     */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof OWEntity fighter) || !inArena(fighter)) return;

        OWArenaMatch match = OWArenaManager.matchOfCombatant(fighter.getUUID());
        if (match == null) return;

        event.setCanceled(true);
        OWArenaManager.knockOut(match, fighter);
    }

    /**
     * Rend les chefs intouchables dans l'arène pendant leur match.
     *
     * <p>Un chef y vient en témoin, pas en combattant. S'il mourait, il réapparaîtrait au point de
     * réapparition du monde et le renvoi de fin de match le ramènerait chez lui depuis un endroit
     * qui n'est plus le sien — c'est précisément ce qui faisait « disparaître » le perdant.</p>
     *
     * <p>Passer par l'événement plutôt que par {@code setInvulnerable} évite de laisser un joueur
     * invulnérable pour de bon si un match se termine mal : l'immunité s'évapore avec le match.</p>
     */
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer chief) || !inArena(chief)) return;
        if (OWArenaManager.isSpectatingChief(chief.getUUID())) event.setCanceled(true);
    }
}
