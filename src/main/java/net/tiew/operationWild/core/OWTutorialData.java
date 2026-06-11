package net.tiew.operationWild.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public final class OWTutorialData {

    private OWTutorialData() {}

    private static final File FILE = new File("config/ow_tutorials.properties");
    private static final Properties DATA = new Properties();
    private static boolean loaded = false;

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        if (FILE.exists()) {
            try (FileInputStream in = new FileInputStream(FILE)) {
                DATA.load(in);
            } catch (IOException ignored) {
            }
        }
    }

    private static void save() {
        try {
            File parent = FILE.getParentFile();
            if (parent != null) parent.mkdirs();
            try (FileOutputStream out = new FileOutputStream(FILE)) {
                DATA.store(out, "Operation W.I.L.D - tutorials already seen");
            }
        } catch (IOException ignored) {
        }
    }

    public static boolean hasSeenAttacksTutorial(String speciesId) {
        ensureLoaded();
        return "true".equals(DATA.getProperty("attacks_" + speciesId));
    }

    public static void markAttacksTutorialSeen(String speciesId) {
        ensureLoaded();
        DATA.setProperty("attacks_" + speciesId, "true");
        save();
    }

    public static boolean hasSeenMountTutorial() {
        ensureLoaded();
        return "true".equals(DATA.getProperty("mount_tutorial"));
    }

    public static void markMountTutorialSeen() {
        ensureLoaded();
        DATA.setProperty("mount_tutorial", "true");
        save();
    }

    public static boolean hasSeenLevelTutorial() {
        ensureLoaded();
        return "true".equals(DATA.getProperty("level_tutorial"));
    }

    public static void markLevelTutorialSeen() {
        ensureLoaded();
        DATA.setProperty("level_tutorial", "true");
        save();
    }

    /** Didacticiel d'ouverture d'inventaire (onglets + archétype) déjà vu ? (une fois, global) */
    public static boolean hasSeenInventoryTutorial() {
        ensureLoaded();
        return "true".equals(DATA.getProperty("inventory_tutorial"));
    }

    public static void markInventoryTutorialSeen() {
        ensureLoaded();
        DATA.setProperty("inventory_tutorial", "true");
        save();
    }

    /** Didacticiel d'amélioration de statistiques (boutons +) déjà vu ? (une fois, global) */
    public static boolean hasSeenStatsTutorial() {
        ensureLoaded();
        return "true".equals(DATA.getProperty("stats_tutorial"));
    }

    public static void markStatsTutorialSeen() {
        ensureLoaded();
        DATA.setProperty("stats_tutorial", "true");
        save();
    }
}
