package net.tiew.operationWild.datagen.wiki;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.fml.ModList;
import net.tiew.operationWild.OperationWild;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class OWWikiDatasProvider implements DataProvider {

    public static final int SCHEMA_VERSION = 2;

    public static final boolean ALL_LANGUAGES = false;

    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public OWWikiDatasProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.output = output;
        this.registries = registries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path target = output.getOutputFolder().resolve("wiki_data.json");
        return registries.thenCompose(provider -> DataProvider.saveStable(cache, build(provider), target));
    }

    private JsonObject build(HolderLookup.Provider provider) {
        OWWikiLang lang = OWWikiLang.load(ALL_LANGUAGES);
        OWWikiSources sources = OWWikiSources.locate(output.getOutputFolder());
        java.util.List<OWWikiRecipes.Recipe> recipes = OWWikiRecipes.load(sources);

        JsonObject root = new JsonObject();
        root.add("entities", OWWikiEntitySection.build(provider, lang, sources, recipes));
        root.add("meta", meta(lang));
        root.add("glossary", OWWikiGlossarySection.build(lang));
        root.add("items", OWWikiCatalogSection.items(lang));
        root.add("blocks", OWWikiCatalogSection.blocks(lang));
        root.add("saddles", OWWikiCatalogSection.saddles(lang));
        root.add("recipes", OWWikiRecipes.catalog(recipes, lang));
        root.add("sounds", OWWikiTags.soundCatalog());
        root.add("tags", OWWikiTags.catalog());
        root.add("worldgen", OWWikiCatalogSection.worldgen());
        root.add("advancements", OWWikiCatalogSection.advancements(lang));
        return root;
    }

    private JsonObject meta(OWWikiLang lang) {
        JsonObject meta = new JsonObject();
        meta.addProperty("mod_id", OperationWild.MOD_ID);
        meta.addProperty("schema_version", SCHEMA_VERSION);
        meta.addProperty("generator", getName());
        try {
            ModList.get().getModContainerById(OperationWild.MOD_ID)
                    .ifPresent(container -> meta.addProperty("mod_version", container.getModInfo().getVersion().toString()));
        } catch (Throwable ignored) {
        }

        JsonObject languages = new JsonObject();
        for (Map.Entry<String, Integer> entry : lang.sizes().entrySet()) {
            languages.addProperty(entry.getKey(), entry.getValue());
        }
        meta.add("languages", languages);

        JsonArray failures = new JsonArray();
        OWWikiReflect.probeFailures().forEach(failures::add);
        if (!failures.isEmpty()) meta.add("probe_failures", failures);
        return meta;
    }

    @Override
    public String getName() {
        return "OW Wiki Data";
    }
}
