package net.tiew.operationWild.datagen.wiki;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class OWWikiSources {

    private static final Pattern ANIMATION = Pattern.compile(
            "AnimationDefinition ([A-Z0-9_]+)\\s*=\\s*AnimationDefinition\\.Builder\\.withLength\\(\\s*([0-9.]+)[fFdD]?\\s*\\)\\s*(\\.looping\\(\\))?");

    private static final String CLIENT_PACKAGE = "src/main/java/net/tiew/operationWild/entity/client";

    private final Path root;
    private final Path generated;

    private OWWikiSources(Path root, Path generated) {
        this.root = root;
        this.generated = generated;
    }

    public static OWWikiSources locate(Path packOutput) {
        Path generated = packOutput == null ? null : packOutput.toAbsolutePath();
        Path candidate = generated;
        for (int depth = 0; candidate != null && depth < 6; depth++) {
            if (Files.isDirectory(candidate.resolve("src/main/java")) && Files.isDirectory(candidate.resolve("src/main/resources"))) {
                return new OWWikiSources(candidate, generated);
            }
            candidate = candidate.getParent();
        }
        return new OWWikiSources(null, generated);
    }

    public boolean available() {
        return root != null;
    }

    public Path dataDirectory(String folder) {
        if (generated == null) return null;
        Path directory = generated.resolve("data/ow").resolve(folder);
        return Files.isDirectory(directory) ? directory : null;
    }

    public JsonArray textures(String speciesId) {
        if (root == null) return null;
        Path directory = root.resolve("src/main/resources/assets/ow/textures/entity").resolve(speciesId);
        List<String> files = relativeFiles(directory, ".png");
        if (files.isEmpty()) return null;
        JsonArray textures = new JsonArray();
        for (String file : files) textures.add("textures/entity/" + speciesId + "/" + file);
        return textures;
    }

    public JsonArray wildBehaviors(String speciesId) {
        if (root == null) return null;
        Path directory = root.resolve("src/main/java/net/tiew/operationWild/entity/goals").resolve(speciesId);
        List<String> files = relativeFiles(directory, ".java");
        if (files.isEmpty()) return null;
        JsonArray goals = new JsonArray();
        for (String file : files) goals.add(file.substring(0, file.length() - ".java".length()));
        return goals;
    }

    public JsonObject client(String stem) {
        if (root == null) return null;
        JsonObject client = new JsonObject();
        addIfFound(client, "model", CLIENT_PACKAGE + "/model", stem + "Model");
        addIfFound(client, "renderer", CLIENT_PACKAGE + "/render", stem + "Renderer");
        addIfFound(client, "skin", CLIENT_PACKAGE + "/skin", stem + "Skin");

        JsonArray layers = new JsonArray();
        for (String file : relativeFiles(root.resolve(CLIENT_PACKAGE + "/layer"), ".java")) {
            String name = file.substring(0, file.length() - ".java".length());
            if (name.startsWith(stem)) layers.add(name);
        }
        if (!layers.isEmpty()) client.add("layers", layers);

        JsonArray animations = animations(stem);
        if (animations != null) client.add("animations", animations);
        return client.isEmpty() ? null : client;
    }

    private void addIfFound(JsonObject target, String property, String directory, String className) {
        if (Files.isRegularFile(root.resolve(directory).resolve(className + ".java"))) {
            target.addProperty(property, className);
        }
    }

    private JsonArray animations(String stem) {
        Path file = root.resolve(CLIENT_PACKAGE + "/animation").resolve(stem + "Animations.java");
        if (!Files.isRegularFile(file)) return null;
        String source;
        try {
            source = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        } catch (IOException failure) {
            return null;
        }
        JsonArray animations = new JsonArray();
        Matcher matcher = ANIMATION.matcher(source);
        while (matcher.find()) {
            JsonObject animation = new JsonObject();
            animation.addProperty("name", matcher.group(1));
            animation.addProperty("seconds", Float.parseFloat(matcher.group(2)));
            animation.addProperty("looping", matcher.group(3) != null);
            animations.add(animation);
        }
        return animations.isEmpty() ? null : animations;
    }

    private static List<String> relativeFiles(Path directory, String extension) {
        List<String> files = new ArrayList<>();
        if (directory == null || !Files.isDirectory(directory)) return files;
        try (Stream<Path> walk = Files.walk(directory, 4)) {
            walk.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(extension))
                    .forEach(path -> files.add(directory.relativize(path).toString().replace('\\', '/')));
        } catch (IOException ignored) {
            return files;
        }
        Collections.sort(files);
        return files;
    }
}
