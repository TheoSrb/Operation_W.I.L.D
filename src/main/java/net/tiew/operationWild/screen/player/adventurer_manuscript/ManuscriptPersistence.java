package net.tiew.operationWild.screen.player.adventurer_manuscript;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.event.ClientEvents;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ManuscriptPersistence {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "adventurer_manuscript.json";

    private ManuscriptPersistence() {}

    private static Path getFilePath() {
        Path configDir = Minecraft.getInstance().gameDirectory.toPath().resolve("saves/" + ClientEvents.getWorldName(Minecraft.getInstance().player)).resolve("datas_ow");
        try {
            Files.createDirectories(configDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return configDir.resolve(FILE_NAME);
    }

    public static void saveClient() {
        try {
            Path file = getFilePath();
            JsonObject root = new JsonObject();
            root.add("tempMap", serialize(AdventurerManuscriptScreen.tempMap));
            root.add("owEntities", serialize(AdventurerManuscriptScreen.OW_ENTITIES));
            try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(root, w);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadClient() {
        try {
            Path file = getFilePath();
            if (!Files.exists(file)) return;
            try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
                Map<EntityType<? extends OWEntity>, Integer> loadedTemp = deserialize(root.getAsJsonObject("tempMap"));
                Map<EntityType<? extends OWEntity>, Integer> loadedOwEntities = deserialize(root.getAsJsonObject("owEntities"));

                AdventurerManuscriptScreen.tempMap.clear();
                AdventurerManuscriptScreen.tempMap.putAll(loadedTemp);

                AdventurerManuscriptScreen.OW_ENTITIES.clear();
                AdventurerManuscriptScreen.OW_ENTITIES.putAll(loadedOwEntities);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JsonObject serialize(Map<EntityType<? extends OWEntity>, Integer> map) {
        JsonObject obj = new JsonObject();
        for (Map.Entry<EntityType<? extends OWEntity>, Integer> e : map.entrySet()) {
            ResourceLocation id = EntityType.getKey(e.getKey());
            if (id != null) {
                obj.addProperty(id.toString(), e.getValue());
            }
        }
        return obj;
    }

    @SuppressWarnings("unchecked")
    private static Map<EntityType<? extends OWEntity>, Integer> deserialize(JsonObject json) {
        Map<EntityType<? extends OWEntity>, Integer> out = new LinkedHashMap<>();
        if (json == null) return out;
        for (Map.Entry<String, JsonElement> e : json.entrySet()) {
            try {
                ResourceLocation id = ResourceLocation.parse(e.getKey());
                EntityType<?> raw = BuiltInRegistries.ENTITY_TYPE.get(id);
                if (raw != null) {
                    EntityType<? extends OWEntity> casted = (EntityType<? extends OWEntity>) raw;
                    out.put(casted, e.getValue().getAsInt());
                }
            } catch (Exception ignored) {
            }
        }
        return out;
    }
}