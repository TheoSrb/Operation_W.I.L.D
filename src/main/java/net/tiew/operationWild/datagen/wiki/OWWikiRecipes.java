package net.tiew.operationWild.datagen.wiki;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class OWWikiRecipes {

    private OWWikiRecipes() {}

    public record Recipe(String id, String type, String result, int count, Set<String> ingredients, JsonObject json) {}

    public static List<Recipe> load(OWWikiSources sources) {
        List<Recipe> recipes = new ArrayList<>();
        Path directory = sources.dataDirectory("recipe");
        if (directory == null) return recipes;
        try (Stream<Path> walk = Files.walk(directory, 4)) {
            walk.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .forEach(path -> {
                        Recipe recipe = parse(directory, path);
                        if (recipe != null) recipes.add(recipe);
                    });
        } catch (IOException ignored) {
            return recipes;
        }
        return recipes;
    }

    private static Recipe parse(Path root, Path file) {
        try {
            String raw = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            JsonObject json = JsonParser.parseString(raw).getAsJsonObject();

            String relative = root.relativize(file).toString().replace('\\', '/');
            String id = "ow:" + relative.substring(0, relative.length() - ".json".length());
            String type = json.has("type") ? json.get("type").getAsString() : "unknown";

            String result = null;
            int count = 1;
            JsonElement resultElement = json.get("result");
            if (resultElement != null) {
                if (resultElement.isJsonPrimitive()) {
                    result = resultElement.getAsString();
                } else if (resultElement.isJsonObject()) {
                    JsonObject resultObject = resultElement.getAsJsonObject();
                    if (resultObject.has("id")) result = resultObject.get("id").getAsString();
                    else if (resultObject.has("item")) result = resultObject.get("item").getAsString();
                    if (resultObject.has("count")) count = resultObject.get("count").getAsInt();
                }
            }

            Set<String> ingredients = new LinkedHashSet<>();
            for (String property : json.keySet()) {
                if (property.equals("result") || property.equals("type") || property.equals("category")) continue;
                collect(json.get(property), ingredients);
            }

            return new Recipe(id, type, result, count, ingredients, json);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void collect(JsonElement element, Set<String> ingredients) {
        if (element == null) return;
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String value = element.getAsString();
            if (value.contains(":")) ingredients.add(value);
            return;
        }
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) collect(child, ingredients);
            return;
        }
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            for (String property : object.keySet()) {
                if (property.equals("pattern")) continue;
                collect(object.get(property), ingredients);
            }
        }
    }

    public static JsonArray catalog(List<Recipe> recipes, OWWikiLang lang) {
        JsonArray catalog = new JsonArray();
        for (Recipe recipe : recipes) catalog.add(describe(recipe, lang, true));
        return catalog;
    }

    public static JsonArray usedBy(List<Recipe> recipes, Collection<String> items, OWWikiLang lang) {
        JsonArray uses = new JsonArray();
        for (Recipe recipe : recipes) {
            boolean matches = false;
            for (String item : items) {
                if (recipe.ingredients().contains(item)) {
                    matches = true;
                    break;
                }
            }
            if (matches) uses.add(describe(recipe, lang, false));
        }
        return uses;
    }

    private static JsonObject describe(Recipe recipe, OWWikiLang lang, boolean includeJson) {
        JsonObject entry = new JsonObject();
        entry.addProperty("id", recipe.id());
        entry.addProperty("type", recipe.type());
        if (recipe.result() != null) {
            entry.addProperty("result", recipe.result());
            entry.addProperty("result_count", recipe.count());
            JsonObject names = lang.translations(translationKey(recipe.result()));
            if (names != null) entry.add("result_names", names);
        }
        JsonArray ingredients = new JsonArray();
        recipe.ingredients().forEach(ingredients::add);
        entry.add("ingredients", ingredients);
        if (includeJson) entry.add("json", recipe.json());
        return entry;
    }

    private static String translationKey(String item) {
        int separator = item.indexOf(':');
        if (separator < 0) return "item.minecraft." + item;
        return "item." + item.substring(0, separator) + "." + item.substring(separator + 1);
    }
}
