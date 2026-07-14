package net.tiew.operationWild.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.component.SoulData;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.event.ClientEvents;
import net.tiew.operationWild.screen.entity.OWTeamsInterface;
import net.tiew.operationWild.screen.entity.RitualCommunionScreen;

/**
 * Actions purement client déclenchées depuis du code commun (items, entités). Regrouper ici les
 * références aux classes client (Screen, Minecraft…) évite de les référencer directement dans une
 * classe commune : sinon la vérification du bytecode de cette classe force le chargement d'une
 * classe client et fait planter le serveur dédié (RuntimeDistCleaner).
 *
 * <p>Appeler ces méthodes uniquement derrière un garde de côté (ex. {@code level.isClientSide}) :
 * la résolution de l'appel est paresseuse, donc {@code OWClientHooks} n'est jamais chargé sur le
 * serveur tant que la méthode n'est pas exécutée.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class OWClientHooks {

    private OWClientHooks() {}

    /** Ouvre l'Écran de Communion pour lancer le Rituel de Résurrection. */
    public static void openRitualCommunion(SoulData data) {
        Minecraft.getInstance().setScreen(new RitualCommunionScreen(data));
    }

    /** Marque le carnet comme notifié côté client. */
    public static void setBookNotified() {
        ClientEvents.isNotifiedOWBook = true;
    }

    /** Déclenche l'effet visuel de tir au cœur sur l'entité donnée. */
    public static void triggerHeartHit(int entityId) {
        ClientEvents.triggerHeartHit(entityId);
    }

    /** Prévient l'écran de tribu ouvert qu'une entité est déjà dans une autre tribu. */
    public static void notifyEntityAlreadyInTeam(String targetNickname, String currentTeamName) {
        Screen current = Minecraft.getInstance().screen;
        if (current instanceof OWTeamsInterface teamsInterface) {
            teamsInterface.onEntityAlreadyInTeam(targetNickname, currentTeamName);
        }
    }

    /** Résout une entité par son id réseau dans le monde client, ou {@code null}. */
    public static net.minecraft.world.entity.Entity clientEntity(int entityId) {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? null : level.getEntity(entityId);
    }

    /** Résout une {@link OWEntity} par son id réseau dans le monde client, ou {@code null}. */
    public static OWEntity clientOWEntity(int entityId) {
        return clientEntity(entityId) instanceof OWEntity e ? e : null;
    }

    /** Retire l'entité (côté client) de sa tribu. */
    public static void clearClientTeam(int entityId) {
        OWEntity e = clientOWEntity(entityId);
        if (e != null) e.currentTeam = null;
    }
}
