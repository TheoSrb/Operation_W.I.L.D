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

        // Prime à l'élimination : la bête qui abat un adversaire regagne 20 % de sa vie maximale.
        // Récompenser le tombeur pousse à conclure plutôt qu'à temporiser, et évite qu'un duel
        // s'enlise entre deux combattants à moitié morts.
        OWEntity killer = resolveKiller(event.getSource().getEntity());
        if (killer != null && killer != fighter && killer.isAlive()
                && OWArenaManager.matchOfCombatant(killer.getUUID()) == match) {
            killer.heal(killer.getMaxHealth() * 0.20f);
        }

        OWArenaManager.knockOut(match, fighter);
    }

    /**
     * Remonte du responsable d'un dégât jusqu'au combattant à créditer.
     *
     * <p>Les segments de queue du boa et du crocodile frappent en leur nom : sans cette résolution,
     * une mise à mort par la queue ne récompenserait personne.</p>
     */
    private static OWEntity resolveKiller(net.minecraft.world.entity.Entity source) {
        if (source instanceof OWEntity owE) return owE;
        if (source instanceof net.tiew.operationWild.entity.animals.terrestrial.BoaTailPart part
                && part.getParent() instanceof OWEntity parent) return parent;
        if (source instanceof net.tiew.operationWild.entity.animals.aquatic.CrocodileTailPart part
                && part.getParent() instanceof OWEntity parent) return parent;
        return null;
    }

    /**
     * L'owner d'un camp est <b>mortel pendant le combat</b> : s'il tombe, son camp perd aussitôt,
     * même s'il lui reste des créatures debout.
     *
     * <p>Le coup fatal ne le tue pas vraiment — il déclenche la défaite, puis le renvoi de fin de
     * match le ramène chez lui. Le laisser mourir pour de bon le ferait réapparaître au spawn du
     * monde, hors de portée du renvoi, ce qui le ferait « disparaître » du duel.</p>
     *
     * <p>Il reste <b>intouchable</b> aux deux moments où rien ne doit se jouer : l'animation
     * d'ouverture (tout le monde est figé) et le temps de contemplation du verdict (le vaincu
     * attend son rapatriement au milieu des créatures adverses).</p>
     */
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer chief) || !inArena(chief)) return;

        OWArenaMatch match = OWArenaManager.matchOfChief(chief.getUUID());
        if (match == null) return;

        // Ouverture ou verdict rendu : intouchable.
        if (match.isOpening() || match.getState() == OWArenaMatch.State.ENDED) {
            event.setCanceled(true);
            return;
        }

        // Combat engagé : le chef encaisse normalement, mais le coup qui l'achèverait est intercepté
        // et transformé en défaite de son camp.
        if (match.getState() == OWArenaMatch.State.FIGHTING
                && event.getAmount() >= chief.getHealth() + chief.getAbsorptionAmount()) {
            event.setCanceled(true);
            OWArenaManager.chiefDefeated(chief.getServer(), match, chief.getUUID());
        }
    }
}
