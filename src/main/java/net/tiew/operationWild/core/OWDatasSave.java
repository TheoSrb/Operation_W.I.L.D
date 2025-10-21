package net.tiew.operationWild.core;

import net.minecraft.client.Minecraft;
import net.tiew.operationWild.event.ClientEvents;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class OWDatasSave {

    public static Properties owDatas = new Properties();

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

        String tamingExpStr = owDatas.getProperty("tamingExp");
        if (tamingExpStr != null && !tamingExpStr.trim().isEmpty()) {
            ClientEvents.tamingExperience = Double.parseDouble(tamingExpStr);
        } else {
            ClientEvents.tamingExperience = 0.0;
        }
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