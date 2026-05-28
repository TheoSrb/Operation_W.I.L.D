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

    /** Skins achetés avec du prestige, par entité (UUID → ensemble d'indices de skin). */
    public static final Map<UUID, Set<Integer>> purchasedSkins = new HashMap<>();

    private static void registerAnimal(Properties properties, String animal) {
        properties.setProperty(animal, "true");
    }

    private static void removeAnimal(Properties properties, String animal) {
        properties.remove(animal);
    }

    public static void addToManuscript(Properties properties, String animal) {
        registerAnimal(properties, animal);
        updateFile(properties);
    }

    public static void saveTamingExperience(Properties properties, double experience) {
        properties.setProperty("tamingExp", String.valueOf(experience));
        updateFile(properties);
    }

    // =========================================================================
    // Skins achetés (prestige)
    // =========================================================================

    /** Retourne {@code true} si ce skin a été acheté avec du prestige pour cette entité. */
    public static boolean hasPurchasedSkin(UUID entityId, int skinIndex) {
        Set<Integer> skins = purchasedSkins.get(entityId);
        return skins != null && skins.contains(skinIndex);
    }

    /** Marque un skin comme acheté pour cette entité et sauvegarde immédiatement. */
    public static void addPurchasedSkin(UUID entityId, int skinIndex) {
        purchasedSkins.computeIfAbsent(entityId, k -> new HashSet<>()).add(skinIndex);
        owDatas.setProperty("skin_purchased_" + entityId + "_" + skinIndex, "true");
        updateFile(owDatas);
    }

    // =========================================================================
    // Chargement / sauvegarde
    // =========================================================================

    public static void loadFromFile() {
        String worldName = ClientEvents.getWorldName(Minecraft.getInstance().player);
        File propertiesFile = new File("saves/" + worldName + "/OWDataCore.properties");

        owDatas.clear();
        purchasedSkins.clear();

        if (propertiesFile.exists()) {
            try (FileInputStream input = new FileInputStream(propertiesFile)) {
                owDatas.load(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String tamingExpStr = owDatas.getProperty("tamingExp");
        if (tamingExpStr != null && !tamingExpStr.trim().isEmpty()) {
            ClientEvents.tamingExperience = Double.parseDouble(tamingExpStr);
        } else {
            ClientEvents.tamingExperience = 0.0;
        }

        // Charge les skins achetés — clés : "skin_purchased_<uuid>_<skinIndex>"
        for (String key : owDatas.stringPropertyNames()) {
            if (!key.startsWith("skin_purchased_")) continue;
            String suffix = key.substring("skin_purchased_".length());
            int sep = suffix.lastIndexOf('_');
            if (sep == -1) continue;
            try {
                UUID uuid      = UUID.fromString(suffix.substring(0, sep));
                int  skinIndex = Integer.parseInt(suffix.substring(sep + 1));
                purchasedSkins.computeIfAbsent(uuid, k -> new HashSet<>()).add(skinIndex);
            } catch (Exception ignored) {}
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
