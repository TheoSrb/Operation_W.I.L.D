package net.tiew.operationWild.networking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ClientKillData {

    public static void saveKillData(String worldName, String entity) {
        Properties props = new Properties();
        String filePath = "saves/" + worldName + "/owDatas.properties";

        File file = new File(filePath);
        file.getParentFile().mkdirs();

        if (file.exists()) {
            try (FileInputStream input = new FileInputStream(filePath)) {
                props.load(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        props.setProperty(entity, "true");

        try (FileOutputStream output = new FileOutputStream(filePath)) {
            props.store(output, "Operation Wild - Kill Data");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void createEmptyFile(String worldName) {
        File file = new File("saves/" + worldName + "/owDatas.properties");
        if (!file.exists()) {
            Properties props = new Properties();
            try (FileOutputStream output = new FileOutputStream(file)) {
                props.store(output, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}