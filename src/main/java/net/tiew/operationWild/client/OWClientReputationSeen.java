package net.tiew.operationWild.client;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Mémorise, <b>côté client et par joueur</b>, le dernier palier de badge vu pour chaque tribu
 * (teamId → ordinal du badge). Persisté dans un petit fichier du dossier de jeu, afin de ne déclencher
 * l'animation de changement de palier de l'écran Réputation qu'une fois par changement réel.
 */
public final class OWClientReputationSeen {

    private static final String FILE_NAME = "ow_reputation_seen.properties";
    private static Properties props;

    private OWClientReputationSeen() {}

    private static File file() {
        return new File(Minecraft.getInstance().gameDirectory, FILE_NAME);
    }

    private static Properties props() {
        if (props == null) {
            props = new Properties();
            File f = file();
            if (f.exists()) {
                try (FileInputStream in = new FileInputStream(f)) { props.load(in); }
                catch (IOException ignored) {}
            }
        }
        return props;
    }

    /** Ordinal du dernier badge vu pour {@code teamId}, ou {@code -1} si jamais vu. */
    public static int get(int teamId) {
        try { return Integer.parseInt(props().getProperty(Integer.toString(teamId), "-1")); }
        catch (NumberFormatException e) { return -1; }
    }

    public static void set(int teamId, int badgeOrdinal) {
        props().setProperty(Integer.toString(teamId), Integer.toString(badgeOrdinal));
        try (FileOutputStream out = new FileOutputStream(file())) {
            props().store(out, "Operation W.I.L.D - derniers paliers de reputation vus");
        } catch (IOException ignored) {}
    }
}
