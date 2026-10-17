package net.tiew.operationWild.entity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.tiew.operationWild.OperationWild;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class OWHeadHitboxes extends SimpleJsonResourceReloadListener {

    public static final String DIRECTORY = "head_hitbox";

    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile Map<ResourceLocation, OWHeadHitbox> hitboxes = Map.of();

    private OWHeadHitboxes() {
        super(new Gson(), DIRECTORY);
    }

    @Nullable
    public static OWHeadHitbox get(ResourceLocation entityType) {
        return hitboxes.get(entityType);
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new OWHeadHitboxes());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, OWHeadHitbox> loaded = new HashMap<>();
        entries.forEach((id, json) -> {
            try {
                loaded.put(id, OWHeadHitbox.fromJson(json.getAsJsonObject()));
            } catch (RuntimeException e) {
                LOGGER.error("Invalid head hitbox {}: {}", id, e.getMessage());
            }
        });
        hitboxes = Map.copyOf(loaded);
        LOGGER.info("Loaded {} head hitboxes", loaded.size());
    }
}
