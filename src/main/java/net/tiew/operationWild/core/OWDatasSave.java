package net.tiew.operationWild.core;

import net.minecraft.client.Minecraft;
import net.tiew.operationWild.event.ClientEvents;
import net.tiew.operationWild.quests.CosmeticsQuestsRegistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class OWDatasSave {

    public static Properties owDatas = new Properties();

    private static void registerAnimal(Properties properties, String animal) {
        properties.setProperty(animal, "true");
    }

    public static void addToManuscript(Properties properties, String animal) {
        registerAnimal(properties, animal);
        updateFile(properties);
    }

    // =========================================================================
    // Chargement / sauvegarde
    // =========================================================================
    // NOTE : l'Expérience d'Apprivoisement (par joueur → OWTamingXp) et les skins débloqués (par pet,
    // stockés sur l'entité → OWEntity#unlockSkin) ne passent plus par ce fichier local : ils sont
    // désormais serveur-autoritaires. Ce fichier ne conserve que les données purement client
    // (manuscrit, quêtes cosmétiques, avertissement dev).

    public static void loadFromFile() {
        String worldName = ClientEvents.getWorldName(Minecraft.getInstance().player);
        File propertiesFile = new File("saves/" + worldName + "/OWDataCore.properties");

        owDatas.clear();

        if (propertiesFile.exists()) {
            try (FileInputStream input = new FileInputStream(propertiesFile)) {
                owDatas.load(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Charge les quêtes (initialise aussi tigerKillCounts depuis la progression sauvegardée)
        CosmeticsQuestsRegistry.loadAllFromSave(owDatas);
    }

    /** Persiste les états de quêtes dans le fichier de sauvegarde (écrit le fichier une fois). */
    public static void saveQuestStates(Properties properties) {
        updateFile(properties);
    }

    public static boolean hasSeenDevWarning() {
        return "true".equals(owDatas.getProperty("devWarning_seen"));
    }

    public static void markDevWarningSeen() {
        owDatas.setProperty("devWarning_seen", "true");
        updateFile(owDatas);
    }

    private static void updateFile(Properties properties) {
        String worldName = ClientEvents.getWorldName(Minecraft.getInstance().player);

        try (FileOutputStream output = new FileOutputStream("saves/" + worldName + "/" + "OWDataCore.properties")) {
            properties.store(output, "Operation W.I.L.D Local Datas");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
