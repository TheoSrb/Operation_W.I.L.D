package net.tiew.operationWild.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.misc.Submarine;
import net.tiew.operationWild.entity.quests.daily_quests.DailyQuest;
import net.tiew.operationWild.entity.quests.daily_quests.DailyQuestRegistry;

/**
 * Notificateur client des quêtes quotidiennes de l'entité chevauchée. Surveille la progression
 * (synchronisée au cavalier) et émet des toasts « façon succès » :
 * <ul>
 *   <li>progression : après une courte pause (anti-spam), un toast récapitule le gain « +N » ;</li>
 *   <li>complétion : un toast « Quête Terminée » (la récompense, elle, est animée par les overlays).</li>
 * </ul>
 */
@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class OWQuestNotifier {

    private static final long DEBOUNCE_MS = 800L;

    private static int trackedEntity = -1;
    private static final int[] lastId = {-1, -1, -1};
    private static final int[] lastProg = {-1, -1, -1};
    private static final boolean[] lastLocked = {false, false, false};
    private static final int[] pendingDelta = {0, 0, 0};
    private static final long[] pendingSince = {0, 0, 0};

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) { trackedEntity = -1; return; }

        Entity vehicle = mc.player.getVehicle();
        if (!(vehicle instanceof OWEntity ent) || ent instanceof Submarine) { trackedEntity = -1; return; }

        if (ent.getId() != trackedEntity) { initTracking(ent); return; }

        long now = System.currentTimeMillis();
        for (int s = 0; s < 3; s++) {
            int id = activeId(ent, s);

            // La quête de cet emplacement a changé (reroll / nouveau jour) : on resynchronise la
            // référence sans émettre de toast.
            if (id != lastId[s]) {
                lastId[s] = id;
                lastProg[s] = id < 0 ? -1 : progOf(ent, id);
                lastLocked[s] = id >= 0 && lockedOf(ent, id);
                pendingDelta[s] = 0;
                continue;
            }
            if (id < 0) continue;

            int prog = progOf(ent, id);
            boolean locked = lockedOf(ent, id);

            if (locked && !lastLocked[s]) {
                pendingDelta[s] = 0;   // on abandonne un gain en attente : le toast de complétion prime
                mc.getToasts().addToast(OWDailyQuestToast.completed(nameOf(id), tierColorOf(id)));
            } else if (!locked && lastProg[s] >= 0 && prog > lastProg[s]) {
                pendingDelta[s] += (prog - lastProg[s]);
                pendingSince[s] = now;
            }
            lastProg[s] = prog;
            lastLocked[s] = locked;
        }

        for (int s = 0; s < 3; s++) {
            if (pendingDelta[s] > 0 && now - pendingSince[s] >= DEBOUNCE_MS) flush(s);
        }
    }

    private static void flush(int s) {
        if (pendingDelta[s] <= 0) return;
        int id = lastId[s];
        if (id < 0) { pendingDelta[s] = 0; return; }

        // Pas de doublon : si un toast de CETTE quête est encore affiché/en file, on attend qu'il
        // disparaisse (le gain continue de s'accumuler) plutôt que d'en empiler un second.
        if (Minecraft.getInstance().getToasts().getToast(OWDailyQuestToast.class, Integer.valueOf(id)) != null) return;

        DailyQuest q = DailyQuestRegistry.getById(id);
        int max = q != null ? q.getMaxValue() : 0;
        int prog = Math.min(lastProg[s], max);
        Minecraft.getInstance().getToasts().addToast(
                OWDailyQuestToast.progress(nameOf(id), pendingDelta[s], prog, max, tierColorOf(id), id));
        pendingDelta[s] = 0;
    }

    private static int tierColorOf(int id) {
        DailyQuest q = DailyQuestRegistry.getById(id);
        return (q != null && q.getTier() != null) ? q.getTier().color() : 0x8BE45A;
    }

    private static void initTracking(OWEntity ent) {
        trackedEntity = ent.getId();
        for (int s = 0; s < 3; s++) {
            int id = activeId(ent, s);
            lastId[s] = id;
            lastProg[s] = id < 0 ? -1 : progOf(ent, id);
            lastLocked[s] = id >= 0 && lockedOf(ent, id);
            pendingDelta[s] = 0;
        }
    }

    private static Component nameOf(int id) {
        DailyQuest q = DailyQuestRegistry.getById(id);
        return Component.translatable(q != null ? q.getName() : "");
    }

    private static int activeId(OWEntity e, int slot) {
        return switch (slot) {
            case 0 -> e.activeQuest0;
            case 1 -> e.activeQuest1;
            case 2 -> e.activeQuest2;
            default -> -1;
        };
    }

    private static int progOf(OWEntity e, int id) {
        return switch (id) {
            case 0 -> e.quest0Progression;
            case 1 -> e.quest1Progression;
            case 2 -> e.quest2Progression;
            case 3 -> e.quest3Progression;
            case 4 -> e.quest4Progression;
            case 5 -> e.quest5Progression;
            case 6 -> e.quest6Progression;
            case 7 -> e.quest7Progression;
            case 8 -> e.quest8Progression;
            case 9 -> e.quest9Progression;
            case 10 -> e.quest10Progression;
            default -> 0;
        };
    }

    private static boolean lockedOf(OWEntity e, int id) {
        return switch (id) {
            case 0 -> e.quest0isLocked;
            case 1 -> e.quest1isLocked;
            case 2 -> e.quest2isLocked;
            case 3 -> e.quest3isLocked;
            case 4 -> e.quest4isLocked;
            case 5 -> e.quest5isLocked;
            case 6 -> e.quest6isLocked;
            case 7 -> e.quest7isLocked;
            case 8 -> e.quest8isLocked;
            case 9 -> e.quest9isLocked;
            case 10 -> e.quest10isLocked;
            default -> false;
        };
    }
}
