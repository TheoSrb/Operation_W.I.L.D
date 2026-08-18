package net.tiew.operationWild.datagen.wiki;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.tiew.operationWild.OperationWild;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OWWikiLang {

    public static final String FRENCH = "fr_fr";
    public static final String ENGLISH = "en_us";

    private static final List<String> FALLBACK_CODES = List.of(
            "en_us", "en_gb", "fr_fr", "de_de", "de_ch", "es_es", "es_mx",
            "pt_br", "pt_pt", "ru_ru", "ja_jp", "ko_kr", "zh_cn", "zh_tw");

    private static final Pattern FORMATTING = Pattern.compile("\u00a7[0-9a-fk-orA-FK-OR]");
    private static final Pattern TRAILING_INDEX = Pattern.compile("([0-9]+)$");

    private final Map<String, Map<String, String>> byCode;
    private final String reference;

    private OWWikiLang(Map<String, Map<String, String>> byCode, String reference) {
        this.byCode = byCode;
        this.reference = reference;
    }

    public static OWWikiLang load(boolean allLanguages) {
        String reference = allLanguages ? ENGLISH : FRENCH;
        Map<String, Map<String, String>> loaded = new LinkedHashMap<>();
        for (String code : discoverCodes(allLanguages, reference)) {
            Map<String, String> entries = read(code);
            if (!entries.isEmpty()) loaded.put(code, entries);
        }
        return new OWWikiLang(loaded, reference);
    }

    private static List<String> discoverCodes(boolean allLanguages, String reference) {
        if (!allLanguages) return List.of(reference);
        List<String> found = new ArrayList<>();
        for (String file : OWWikiResources.list("/assets/" + OperationWild.MOD_ID + "/lang")) {
            if (file.endsWith(".json")) found.add(file.substring(0, file.length() - 5));
        }
        if (found.isEmpty()) return FALLBACK_CODES;
        found.sort((a, b) -> {
            if (a.equals(reference)) return -1;
            if (b.equals(reference)) return 1;
            return a.compareTo(b);
        });
        return found;
    }

    private static Map<String, String> read(String code) {
        Map<String, String> entries = new LinkedHashMap<>();
        String path = "/assets/" + OperationWild.MOD_ID + "/lang/" + code + ".json";
        try (InputStream stream = OWWikiLang.class.getResourceAsStream(path)) {
            if (stream == null) return entries;
            JsonObject json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            for (String key : json.keySet()) {
                if (json.get(key).isJsonPrimitive()) entries.put(key, json.get(key).getAsString());
            }
        } catch (Exception ignored) {
        }
        return entries;
    }

    public List<String> codes() {
        return List.copyOf(byCode.keySet());
    }

    public boolean has(String key) {
        for (Map<String, String> entries : byCode.values()) {
            if (entries.containsKey(key)) return true;
        }
        return false;
    }

    public String referenceText(String key) {
        Map<String, String> entries = byCode.get(reference);
        return entries == null ? null : entries.get(key);
    }

    public JsonObject translations(String key) {
        JsonObject object = new JsonObject();
        for (Map.Entry<String, Map<String, String>> entry : byCode.entrySet()) {
            String value = entry.getValue().get(key);
            if (value != null) object.addProperty(entry.getKey(), value);
        }
        return object.isEmpty() ? null : object;
    }

    public JsonObject entry(String key) {
        JsonObject translations = translations(key);
        if (translations == null) return null;
        JsonObject object = new JsonObject();
        object.addProperty("key", key);
        object.add("translations", translations);
        String text = referenceText(key);
        if (text != null) object.addProperty("plain", plain(text));
        return object;
    }

    public Set<String> keysWithPrefix(String prefix) {
        Set<String> keys = new LinkedHashSet<>();
        Map<String, String> preferred = byCode.get(reference);
        if (preferred != null) {
            for (String key : preferred.keySet()) {
                if (key.startsWith(prefix)) keys.add(key);
            }
        }
        for (Map<String, String> entries : byCode.values()) {
            for (String key : entries.keySet()) {
                if (key.startsWith(prefix)) keys.add(key);
            }
        }
        return keys;
    }

    public JsonArray pages(String prefix) {
        List<String> keys = new ArrayList<>(keysWithPrefix(prefix));
        keys.sort((a, b) -> {
            int indexA = trailingIndex(a);
            int indexB = trailingIndex(b);
            return indexA != indexB ? Integer.compare(indexA, indexB) : a.compareTo(b);
        });
        JsonArray array = new JsonArray();
        for (String key : keys) {
            JsonObject page = entry(key);
            if (page != null) array.add(page);
        }
        return array.isEmpty() ? null : array;
    }

    private static int trailingIndex(String key) {
        Matcher matcher = TRAILING_INDEX.matcher(key);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : Integer.MAX_VALUE;
    }

    public static String plain(String raw) {
        return raw == null ? null : FORMATTING.matcher(raw).replaceAll("");
    }

    public Map<String, Integer> sizes() {
        Map<String, Integer> sizes = new LinkedHashMap<>();
        byCode.forEach((code, entries) -> sizes.put(code, entries.size()));
        return Collections.unmodifiableMap(sizes);
    }
}
