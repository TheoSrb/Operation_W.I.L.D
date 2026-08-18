package net.tiew.operationWild.datagen.wiki;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public final class OWWikiResources {

    private OWWikiResources() {}

    public static List<String> list(String classpathDirectory) {
        List<String> names = new ArrayList<>();
        try {
            URL url = OWWikiResources.class.getResource(classpathDirectory);
            if (url == null) return names;

            if ("file".equals(url.getProtocol())) {
                File[] files = new File(url.toURI()).listFiles();
                if (files == null) return names;
                for (File file : files) names.add(file.getName());
            } else if ("jar".equals(url.getProtocol())) {
                String raw = url.toString();
                int separator = raw.indexOf("!/");
                if (separator < 0) return names;
                URI jar = URI.create(raw.substring(0, separator));
                try (FileSystem fileSystem = FileSystems.newFileSystem(jar, Collections.emptyMap())) {
                    Path directory = fileSystem.getPath(classpathDirectory);
                    if (!Files.isDirectory(directory)) return names;
                    try (Stream<Path> children = Files.list(directory)) {
                        children.forEach(child -> names.add(child.getFileName().toString()));
                    }
                }
            }
        } catch (Exception ignored) {
            return names;
        }
        Collections.sort(names);
        return names;
    }

}
