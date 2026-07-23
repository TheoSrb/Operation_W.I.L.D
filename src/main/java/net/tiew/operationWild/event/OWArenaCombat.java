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
 * Ce qui se passe dans l'arène ne sort pas de l'arène.
 *
 * <p>Les créatures y <b>meurent pour de bon</b> — sans quoi il n'y aurait pas de combat, juste une
 * simulation — mais rien de cette mort ne les suit dehors : une copie intégrale est prise à
 * l'entrée, et le renvoi de fin de match les recrée chez elles avec la santé qu'elles avaient
 * avant le duel. Elles n'y laissent ni âme ni butin.</p>
 *
 * <p>C'est un choix de conception : perdre définitivement une créature apprivoisée — élevée sur des
 * heures de jeu — serait un prix sans commune mesure avec l'enjeu d'un match, et personne
 * n'engagerait ses meilleures bêtes. Le chef, lui, vient en témoin et ne peut pas y mourir.</p>
 */
@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class OWArenaCombat {

    /** Vrai si l'entité se trouve dans la dimension d'arène. */
    private static boolean inArena(LivingEntity entity) {
        return entity.level().dimension().equals(OWDimensions.ARENA);
    }

    /**
     * Enregistre la chute d'un combattant. La mort <b>suit son cours</b> : animation, disparition,
     * camp qui se vide — un duel doit se jouer pour de vrai.
     *
     * <p>La créature n'est pas perdue pour autant : une copie intégrale a été prise à son entrée
     * dans l'arène, et le renvoi de fin de match la recrée chez elle avec la santé qu'elle avait
     * avant le duel. Elle n'y laisse pas non plus son âme, coupée à l'entrée.</p>
     */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof OWEntity fighter) || !inArena(fighter)) return;

        OWArenaMatch match = OWArenaManager.matchOfCombatant(fighter.getUUID());
        if (match == null) return;

        OWArenaManager.knockOut(match, fighter);
    }

    /**
     * Rend les chefs intouchables dans l'arène pendant leur match, verdict rendu compris — la
     * protection ne les quitte qu'une fois rentrés chez eux.
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
