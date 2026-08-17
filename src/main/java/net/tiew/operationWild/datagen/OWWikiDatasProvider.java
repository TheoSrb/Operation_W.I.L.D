package net.tiew.operationWild.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntity;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class OWWikiDatasProvider implements DataProvider {
    private final PackOutput output;

    public OWWikiDatasProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        JsonObject root = new JsonObject();

        JsonArray items = new JsonArray();
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (!id.getNamespace().equals(OperationWild.MOD_ID)) continue;

            JsonObject obj = new JsonObject();
            obj.addProperty("id", id.toString());
            obj.addProperty("name", item.getDescription().getString());
            items.add(obj);
        }
        root.add("items", items);

        JsonArray entities = new JsonArray();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (!id.getNamespace().equals(OperationWild.MOD_ID)) continue;

            JsonObject obj = new JsonObject();

            obj.addProperty("id", id.toString());
            obj.addProperty("name", type.getDescription().getString());
            obj.addProperty("width", type.getWidth());
            obj.addProperty("height", type.getHeight());

            entities.add(obj);
        }
        root.add("entities", entities);

        Path target = output.getOutputFolder().resolve("wiki_data.json");
        return DataProvider.saveStable(cache, root, target);
    }

    @Override
    public String getName() { return "OW Wiki Data"; }
}