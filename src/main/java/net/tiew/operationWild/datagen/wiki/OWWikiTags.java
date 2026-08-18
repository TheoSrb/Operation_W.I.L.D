package net.tiew.operationWild.datagen.wiki;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.sound.OWSounds;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class OWWikiTags {

    private OWWikiTags() {}

    private static Map<String, ResourceLocation> soundCache;

    public static Map<String, ResourceLocation> soundEvents() {
        if (soundCache != null) return soundCache;
        Map<String, ResourceLocation> sounds = new LinkedHashMap<>();
        for (Field field : OWSounds.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !DeferredHolder.class.isAssignableFrom(field.getType())) continue;
            try {
                field.setAccessible(true);
                Object holder = field.get(null);
                if (holder instanceof DeferredHolder<?, ?> deferred) {
                    sounds.put(field.getName(), deferred.getId());
                }
            } catch (Throwable ignored) {
            }
        }
        soundCache = sounds;
        return sounds;
    }

    public static JsonArray soundCatalog() {
        JsonArray catalog = new JsonArray();
        soundEvents().forEach((constant, id) -> {
            JsonObject entry = new JsonObject();
            entry.addProperty("constant", constant);
            entry.addProperty("id", id.toString());
            entry.addProperty("path", id.getPath());
            catalog.add(entry);
        });
        return catalog;
    }

    public static JsonArray itemTagsFor(String speciesId) {
        JsonArray tags = new JsonArray();
        String prefix = speciesId.toUpperCase(Locale.ROOT) + "_";
        for (Map.Entry<String, TagKey<?>> tag : tagsOf(OWTags.Items.class).entrySet()) {
            if (!tag.getKey().startsWith(prefix)) continue;
            tags.add(describe(tag.getKey(), tag.getValue(), "item"));
        }
        for (Map.Entry<String, TagKey<?>> tag : tagsOf(OWTags.Blocks.class).entrySet()) {
            if (!tag.getKey().startsWith(prefix)) continue;
            tags.add(describe(tag.getKey(), tag.getValue(), "block"));
        }
        return tags;
    }

    public static JsonArray entityTagsFor(String entityId) {
        JsonArray matching = new JsonArray();
        for (JsonElement element : group(OWTags.Entities.class, "entity_type")) {
            JsonObject tag = element.getAsJsonObject();
            if (!tag.has("entries")) continue;
            for (JsonElement entry : tag.getAsJsonArray("entries")) {
                if (entry.getAsString().equals(entityId)) {
                    matching.add(tag.get("id").getAsString());
                    break;
                }
            }
        }
        return matching;
    }

    public static JsonObject catalog() {
        JsonObject catalog = new JsonObject();
        catalog.add("items", group(OWTags.Items.class, "item"));
        catalog.add("blocks", group(OWTags.Blocks.class, "block"));
        catalog.add("entity_types", group(OWTags.Entities.class, "entity_type"));
        return catalog;
    }

    private static JsonArray group(Class<?> owner, String folder) {
        JsonArray array = new JsonArray();
        tagsOf(owner).forEach((constant, tag) -> array.add(describe(constant, tag, folder)));
        return array;
    }

    private static Map<String, TagKey<?>> tagsOf(Class<?> owner) {
        Map<String, TagKey<?>> tags = new LinkedHashMap<>();
        for (Field field : owner.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !TagKey.class.isAssignableFrom(field.getType())) continue;
            try {
                field.setAccessible(true);
                tags.put(field.getName(), (TagKey<?>) field.get(null));
            } catch (Throwable ignored) {
            }
        }
        return tags;
    }

    private static JsonObject describe(String constant, TagKey<?> tag, String folder) {
        JsonObject entry = new JsonObject();
        entry.addProperty("constant", constant);
        entry.addProperty("id", tag.location().toString());
        JsonArray entries = readEntries(tag.location(), folder);
        if (entries != null) entry.add("entries", entries);
        return entry;
    }

    private static JsonArray readEntries(ResourceLocation tag, String folder) {
        String path = "/data/" + tag.getNamespace() + "/tags/" + folder + "/" + tag.getPath() + ".json";
        try (InputStream stream = OWWikiTags.class.getResourceAsStream(path)) {
            if (stream == null) return null;
            JsonObject json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            if (!json.has("values")) return null;
            JsonArray entries = new JsonArray();
            for (JsonElement value : json.getAsJsonArray("values")) {
                if (value.isJsonPrimitive()) {
                    entries.add(value.getAsString());
                } else if (value.isJsonObject() && value.getAsJsonObject().has("id")) {
                    entries.add(value.getAsJsonObject().get("id").getAsString());
                }
            }
            return entries;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
