package net.tiew.operationWild.datagen.wiki;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.tiew.operationWild.core.OWSaddleRecipe;
import net.tiew.operationWild.core.OWSaddleRecipes;
import net.tiew.operationWild.entity.IOWWaypointEntity;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.OWSemiWaterEntity;
import net.tiew.operationWild.entity.OWWaterEntity;
import net.tiew.operationWild.entity.config.IOWDiver;
import net.tiew.operationWild.entity.config.IOWGrabberEntity;
import net.tiew.operationWild.entity.config.IOWRideable;
import net.tiew.operationWild.entity.config.IOWTamable;
import net.tiew.operationWild.entity.piste.OWPisteAttacks;
import net.tiew.operationWild.entity.piste.OWPisteGraph;
import net.tiew.operationWild.entity.piste.OWPisteGraphs;
import net.tiew.operationWild.entity.piste.OWPisteNode;
import net.tiew.operationWild.worldgen.OWBiomeModifiers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class OWWikiEntitySection {

    private OWWikiEntitySection() {}

    private static final String[][] PROBED_VALUES = {
            {"temperament", "getTemperament"},
            {"entity_color", "getEntityColor"},
            {"theoretical_scale", "getTheoreticalScale"},
            {"taming_experience", "getTamingExperience"},
            {"rotation_speed", "getRotationSpeed"},
            {"max_depth", "getMaxDepth"},
            {"grab_max_timeout", "getGrabMaxTimeout"},
            {"prefers_raw_meat", "preferRawMeat"},
            {"prefers_cooked_meat", "preferCookedMeat"},
            {"prefers_vegetables", "preferVegetables"},
            {"secondary_attack_count", "getSecondaryAttackCount"},
            {"secondary_cooldown_ticks", "getSecondaryCooldownDuration"},
            {"combo_pause_delay", "getComboPauseDelay"},
            {"arena_terrain_mask", "arenaTerrainMask"},
            {"rolling_figure", "isRollingFigure"},
            {"rider_camera_follows_body_tilt", "riderCameraFollowsBodyTilt"},
            {"rider_seat_frame_accurate", "riderSeatIsFrameAccurate"},
            {"initial_variant", "getInitialTypeVariant"},
            {"default_skin_index", "getDefaultSkinIndex"},
            {"allows_unowned_piloting", "allowsUnownedPiloting"},
            {"follow_owner_speed_factor", "followOwnerSpeedFactor"},
            {"fights_in_arena", "canFightInArena"},
            {"max_up_step", "maxUpStep"},
            {"default_gravity", "getDefaultGravity"},
            {"can_lean", "canLean"},
            {"bank_max_angle", "bankMaxAngle"},
            {"pitch_max_angle", "pitchMaxAngle"},
            {"teleports_to_owner", "shouldTryTeleportToOwner"},
            {"nap_particle_height", "napParticleHeight"},
            {"nap_particle_forward", "napParticleForward"},
    };

    private static final String[][] PROBED_MOUNT_VALUES = {
            {"run_speed_multiplier", "vehicleRunSpeedMultiplier"},
            {"walk_speed_multiplier", "vehicleWalkSpeedMultiplier"},
            {"combo_speed_multiplier", "vehicleComboSpeedMultiplier"},
            {"water_speed_divider", "vehicleWaterSpeedDivider"},
            {"sprint_acceleration_multiplier", "sprintAccelerationMultiplier"},
            {"increases_speed_during_sprint", "canIncreasesSpeedDuringSprint"},
            {"changes_speed_during_combo", "isChangeSpeedDuringCombo"},
            {"leaping_vehicle", "isLeapingVehicle"},
            {"max_vital_energy", "getMaxVitalEnergy"},
            {"vital_energy_recuperation", "getVitalEnergyRecuperation"},
            {"base_rider_y_offset", "getBaseRiderYOffset"},
            {"rider_anim_y_offset", "getRiderAnimYOffset"},
    };

    public static JsonArray build(HolderLookup.Provider registries, OWWikiLang lang, OWWikiSources sources,
                                  List<OWWikiRecipes.Recipe> recipes) {
        Map<ResourceLocation, JsonObject> lootTables = OWWikiLoot.collect(registries);
        List<String> animalIds = knownAnimalIds(lang);

        JsonArray entities = new JsonArray();
        for (OWWikiSpecies species : OWWikiSpecies.all()) {
            entities.add(entity(species, lang, sources, recipes, lootTables, animalIds));
        }
        return entities;
    }

    private static JsonObject entity(OWWikiSpecies species, OWWikiLang lang, OWWikiSources sources,
                                     List<OWWikiRecipes.Recipe> recipes,
                                     Map<ResourceLocation, JsonObject> lootTables, List<String> animalIds) {
        EntityType<?> type = species.type();
        Class<? extends OWEntity> implementation = species.implementation();
        ResourceLocation key = species.key();
        OWEntity probe = OWWikiReflect.probe(type, implementation);

        JsonObject entity = new JsonObject();
        entity.addProperty("id", key.toString());
        entity.addProperty("path", key.getPath());
        entity.addProperty("class", implementation.getName());
        entity.addProperty("simple_class", implementation.getSimpleName());
        entity.addProperty("translation_key", type.getDescriptionId());
        entity.addProperty("probe_resolved", probe != null);
        addIfPresent(entity, "names", lang.translations(type.getDescriptionId()));
        addIfPresent(entity, "chapter_names", lang.translations("chapter." + type.getDescriptionId()));

        entity.add("classification", classification(species));
        entity.add("traits", traits(implementation));
        entity.add("size", size(type, probe));
        entity.add("tracking", tracking(type));
        entity.add("attributes", attributes(implementation));
        entity.add("gameplay", gameplay(species, probe));
        entity.add("mount", mount(species, probe));
        entity.add("taming", taming(species, probe, lang));
        addIfPresent(entity, "variants", variants(species));
        addIfPresent(entity, "cosmetics", cosmetics(species));
        entity.add("sounds", sounds(species, animalIds));
        addIfPresent(entity, "wild_behaviors", sources.wildBehaviors(species.id()));
        entity.add("abilities", abilities(species, lang));
        addIfPresent(entity, "attack_constants", nonEmpty(OWWikiReflect.constants(species.attackConstants())));
        addIfPresent(entity, "constants", nonEmpty(OWWikiReflect.constants(implementation)));
        JsonObject loot = loot(type, lootTables);
        entity.add("loot", loot);
        JsonArray crafting = OWWikiRecipes.usedBy(recipes, dropItems(loot), lang);
        if (!crafting.isEmpty()) entity.add("crafting_uses", crafting);
        JsonArray entityTags = OWWikiTags.entityTagsFor(key.toString());
        if (!entityTags.isEmpty()) entity.add("entity_tags", entityTags);
        addIfPresent(entity, "spawn", spawn(type));
        addIfPresent(entity, "piste", piste(species, lang));
        addIfPresent(entity, "lore", lore(species, lang));
        addIfPresent(entity, "textures", sources.textures(species.id()));
        addIfPresent(entity, "client", sources.client(species.stem()));

        return entity;
    }

    private static JsonObject classification(OWWikiSpecies species) {
        Class<? extends OWEntity> implementation = species.implementation();
        JsonObject classification = new JsonObject();
        MobCategory category = species.type().getCategory();
        classification.addProperty("mob_category", category.getName());
        classification.addProperty("max_instances_per_chunk", category.getMaxInstancesPerChunk());
        classification.addProperty("friendly", category.isFriendly());
        classification.addProperty("persistent", category.isPersistent());
        classification.addProperty("family", family(implementation));
        classification.addProperty("boss", implementation.getName().contains(".bosses."));

        JsonArray hierarchy = new JsonArray();
        for (Class<?> current = implementation; current != null && current != Object.class; current = current.getSuperclass()) {
            hierarchy.add(current.getSimpleName());
        }
        classification.add("hierarchy", hierarchy);

        JsonArray interfaces = new JsonArray();
        for (Class<?> contract : implementation.getInterfaces()) interfaces.add(contract.getSimpleName());
        classification.add("interfaces", interfaces);
        return classification;
    }

    private static String family(Class<? extends OWEntity> implementation) {
        if (OWWaterEntity.class.isAssignableFrom(implementation)) return "aquatic";
        if (OWSemiWaterEntity.class.isAssignableFrom(implementation)) return "semi_aquatic";
        return "terrestrial";
    }

    private static JsonObject traits(Class<? extends OWEntity> implementation) {
        JsonObject traits = new JsonObject();
        traits.addProperty("tameable", IOWTamable.class.isAssignableFrom(implementation));
        traits.addProperty("rideable", IOWRideable.class.isAssignableFrom(implementation));
        traits.addProperty("grabber", IOWGrabberEntity.class.isAssignableFrom(implementation));
        traits.addProperty("diver", IOWDiver.class.isAssignableFrom(implementation));
        traits.addProperty("neutral_mob", NeutralMob.class.isAssignableFrom(implementation));
        traits.addProperty("rideable_jumping", PlayerRideableJumping.class.isAssignableFrom(implementation));
        traits.addProperty("waypoint", IOWWaypointEntity.class.isAssignableFrom(implementation));
        return traits;
    }

    private static JsonObject size(EntityType<?> type, OWEntity probe) {
        JsonObject size = new JsonObject();
        size.addProperty("width", type.getWidth());
        size.addProperty("height", type.getHeight());
        EntityDimensions dimensions = type.getDimensions();
        size.addProperty("eye_height", dimensions.eyeHeight());
        size.addProperty("fixed", dimensions.fixed());
        OWWikiReflect.putCall(size, "theoretical_scale", probe, "getTheoreticalScale");
        return size;
    }

    private static JsonObject tracking(EntityType<?> type) {
        JsonObject tracking = new JsonObject();
        tracking.addProperty("client_tracking_range", type.clientTrackingRange());
        tracking.addProperty("update_interval", type.updateInterval());
        tracking.addProperty("serializable", type.canSerialize());
        tracking.addProperty("summonable", type.canSummon());
        tracking.addProperty("fire_immune", type.fireImmune());
        tracking.addProperty("spawns_far_from_player", type.canSpawnFarFromPlayer());
        tracking.addProperty("loot_table", type.getDefaultLootTable().location().toString());
        return tracking;
    }

    private static JsonObject attributes(Class<? extends OWEntity> implementation) {
        JsonObject attributes = new JsonObject();
        AttributeSupplier supplier = attributeSupplier(implementation);
        if (supplier == null) return attributes;
        BuiltInRegistries.ATTRIBUTE.holders().forEach(holder -> {
            if (supplier.hasAttribute(holder)) {
                attributes.addProperty(holder.key().location().toString(), supplier.getBaseValue(holder));
            }
        });
        return attributes;
    }

    private static AttributeSupplier attributeSupplier(Class<? extends OWEntity> implementation) {
        try {
            Method builder = implementation.getDeclaredMethod("createAttributes");
            builder.setAccessible(true);
            AttributeSupplier.Builder value = (AttributeSupplier.Builder) builder.invoke(null);
            return value.build();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static JsonObject gameplay(OWWikiSpecies species, OWEntity probe) {
        JsonObject gameplay = new JsonObject();
        probeGroup(gameplay, probe, species.implementation(), PROBED_VALUES);

        OWWikiReflect.call(probe, "getEntityColor").ifPresent(color -> {
            if (color instanceof Number number) {
                gameplay.addProperty("entity_color_hex", String.format(Locale.ROOT, "#%06X", number.intValue() & 0xFFFFFF));
            }
        });

        OWWikiReflect.call(probe, "getArchetype").ifPresent(archetype -> {
            JsonObject object = new JsonObject();
            object.addProperty("name", ((Enum<?>) archetype).name());
            OWWikiReflect.putCall(object, "health_multiplier", archetype, "getHealthMultiplier");
            OWWikiReflect.putCall(object, "damage_multiplier", archetype, "getDamageMultiplier");
            OWWikiReflect.putCall(object, "speed_multiplier", archetype, "getSpeedMultiplier");
            OWWikiReflect.putCall(object, "energy_multiplier", archetype, "getEnergyMultiplier");
            gameplay.add("archetype", object);
        });

        OWWikiReflect.call(probe, "getDiet").ifPresent(diet -> {
            JsonObject object = new JsonObject();
            object.addProperty("name", ((Enum<?>) diet).name());
            OWWikiReflect.putCall(object, "saturation_multiplier", diet, "getSaturationMultiplier");
            gameplay.add("diet", object);
        });

        AttributeSupplier supplier = attributeSupplier(species.implementation());
        if (supplier != null && supplier.hasAttribute(Attributes.MOVEMENT_SPEED)) {
            double base = supplier.getBaseValue(Attributes.MOVEMENT_SPEED);
            gameplay.addProperty("walk_speed_blocks_per_second", base * 20.0);
            Object run = OWWikiReflect.call(probe, "vehicleRunSpeedMultiplier").orElse(null);
            Object sprint = OWWikiReflect.call(probe, "canIncreasesSpeedDuringSprint").orElse(null);
            if (run instanceof Number multiplier) {
                gameplay.addProperty("mounted_speed_blocks_per_second",
                        speedBlocksPerSecond(base, multiplier.floatValue(), Boolean.TRUE.equals(sprint)));
            }
        }
        return gameplay;
    }

    private static double speedBlocksPerSecond(double base, float runMultiplier, boolean acceleratesWhileSprinting) {
        if (runMultiplier <= 0f) return base * 20.0;
        if (acceleratesWhileSprinting) return ((base * 20.0) / 3.0) * (runMultiplier * 0.5 * 1.75);
        return (base * 20.0) * (runMultiplier / 1.75);
    }

    private static JsonObject mount(OWWikiSpecies species, OWEntity probe) {
        JsonObject mount = new JsonObject();
        mount.addProperty("rideable", IOWRideable.class.isAssignableFrom(species.implementation()));
        probeGroup(mount, probe, species.implementation(), PROBED_MOUNT_VALUES);
        OWWikiReflect.call(probe, "acceptSaddle").ifPresent(saddle -> {
            if (saddle instanceof Item item) {
                mount.addProperty("saddle_item", BuiltInRegistries.ITEM.getKey(item).toString());
            }
        });
        mount.addProperty("vital_energy_level_gain", OWEntity.VITAL_ENERGY_LEVEL_GAIN);
        return mount;
    }

    private static JsonObject taming(OWWikiSpecies species, OWEntity probe, OWWikiLang lang) {
        JsonObject taming = new JsonObject();
        taming.addProperty("tameable", IOWTamable.class.isAssignableFrom(species.implementation()));
        OWWikiReflect.putCall(taming, "experience", probe, "getTamingExperience");
        OWWikiReflect.call(probe, "getTamingAdvancement")
                .ifPresent(advancement -> taming.addProperty("advancement", String.valueOf(advancement)));

        JsonArray tags = OWWikiTags.itemTagsFor(species.id());
        if (!tags.isEmpty()) taming.add("food_tags", tags);

        OWSaddleRecipe recipe = OWSaddleRecipes.all().stream()
                .filter(candidate -> candidate.id().equals(species.id()))
                .findFirst().orElse(null);
        if (recipe != null) taming.add("saddle_recipe", saddleRecipe(recipe, lang));

        JsonArray manual = lang.pages(species.id() + ".taming.page");
        if (manual != null) taming.add("manual", manual);
        return taming;
    }

    private static JsonObject saddleRecipe(OWSaddleRecipe recipe, OWWikiLang lang) {
        JsonObject object = new JsonObject();
        object.addProperty("id", recipe.id());
        object.addProperty("entity_key", recipe.entityKey());
        object.addProperty("result", BuiltInRegistries.ITEM.getKey(recipe.result().get()).toString());
        object.addProperty("taming_threshold", recipe.tamingThreshold());
        object.addProperty("accent_color", String.format(Locale.ROOT, "#%06X", recipe.accentColor() & 0xFFFFFF));
        object.addProperty("needs_colors", recipe.needsColors());
        JsonArray needs = new JsonArray();
        for (OWSaddleRecipe.Need need : recipe.needs()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("count", need.count());
            JsonArray items = new JsonArray();
            for (ItemStack stack : need.ingredient().getItems()) {
                items.add(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            }
            entry.add("items", items);
            if (need.labelKey() != null) {
                entry.addProperty("label_key", need.labelKey());
                addIfPresent(entry, "label", lang.translations(need.labelKey()));
            }
            if (need.colorSlot() != OWSaddleRecipe.NO_COLOR_SLOT) entry.addProperty("color_slot", need.colorSlot());
            needs.add(entry);
        }
        object.add("needs", needs);
        return object;
    }

    private static JsonArray variants(OWWikiSpecies species) {
        return OWWikiReflect.enumConstants(species.variantEnum());
    }

    private static JsonArray cosmetics(OWWikiSpecies species) {
        Class<?> cosmetics = species.cosmeticEnum();
        if (cosmetics == null || !cosmetics.isEnum()) return null;
        JsonArray array = new JsonArray();
        for (Object constant : cosmetics.getEnumConstants()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("name", ((Enum<?>) constant).name());
            try {
                Object variant = cosmetics.getField("variant").get(constant);
                if (variant instanceof Enum<?> value) {
                    entry.addProperty("variant", value.name());
                    OWWikiReflect.call(value, "getId").ifPresent(id -> OWWikiReflect.put(entry, "variant_id", id));
                }
            } catch (Throwable ignored) {
            }
            array.add(entry);
        }
        return array;
    }

    private static JsonArray sounds(OWWikiSpecies species, List<String> animalIds) {
        JsonArray sounds = new JsonArray();
        for (Map.Entry<String, ResourceLocation> sound : OWWikiTags.soundEvents().entrySet()) {
            String path = sound.getValue().getPath();
            if (!belongsTo(path, species.id(), animalIds)) continue;
            JsonObject entry = new JsonObject();
            entry.addProperty("constant", sound.getKey());
            entry.addProperty("id", sound.getValue().toString());
            entry.addProperty("path", path);
            sounds.add(entry);
        }
        return sounds;
    }

    private static boolean belongsTo(String soundPath, String speciesId, List<String> animalIds) {
        if (!soundPath.equals(speciesId) && !soundPath.startsWith(speciesId + "_")) return false;
        for (String other : animalIds) {
            if (other.equals(speciesId) || !other.startsWith(speciesId + "_")) continue;
            if (soundPath.equals(other) || soundPath.startsWith(other + "_")) return false;
        }
        return true;
    }

    private static List<String> knownAnimalIds(OWWikiLang lang) {
        List<String> ids = new ArrayList<>();
        for (String key : lang.keysWithPrefix("entity.ow.")) ids.add(key.substring("entity.ow.".length()));
        return ids;
    }

    private static JsonObject abilities(OWWikiSpecies species, OWWikiLang lang) {
        JsonObject abilities = new JsonObject();

        String prefix = "ow.attacks." + species.id() + ".";
        JsonObject cards = new JsonObject();
        for (String key : lang.keysWithPrefix(prefix)) {
            String remainder = key.substring(prefix.length());
            int separator = remainder.lastIndexOf('.');
            if (separator <= 0) continue;
            String slug = remainder.substring(0, separator);
            String field = remainder.substring(separator + 1);
            JsonObject card = cards.getAsJsonObject(slug);
            if (card == null) {
                card = new JsonObject();
                card.addProperty("slug", slug);
                cards.add(slug, card);
            }
            addIfPresent(card, field, lang.entry(key));
        }
        if (!cards.isEmpty()) {
            JsonArray array = new JsonArray();
            for (String slug : cards.keySet()) array.add(cards.getAsJsonObject(slug));
            abilities.add("cards", array);
        }

        JsonArray legacy = new JsonArray();
        for (int index = 1; index <= 6; index++) {
            String titleKey = "attacks.title" + index + "." + species.implementation().getSimpleName();
            String descriptionKey = "attacks.description" + index + "." + species.implementation().getSimpleName();
            if (!lang.has(titleKey) && !lang.has(descriptionKey)) continue;
            JsonObject entry = new JsonObject();
            entry.addProperty("slot", index);
            addIfPresent(entry, "title", lang.entry(titleKey));
            addIfPresent(entry, "description", lang.entry(descriptionKey));
            legacy.add(entry);
        }
        if (!legacy.isEmpty()) abilities.add("legacy_cards", legacy);

        return abilities;
    }

    private static JsonObject loot(EntityType<?> type, Map<ResourceLocation, JsonObject> lootTables) {
        JsonObject loot = new JsonObject();
        ResourceLocation table = type.getDefaultLootTable().location();
        loot.addProperty("table", table.toString());
        JsonObject raw = lootTables.get(table);
        if (raw != null) {
            loot.add("drops", OWWikiLoot.summarize(raw));
            loot.add("table_json", raw);
        }
        return loot;
    }

    private static List<String> dropItems(JsonObject loot) {
        List<String> items = new ArrayList<>();
        if (!loot.has("drops")) return items;
        for (JsonElement element : loot.getAsJsonArray("drops")) {
            JsonObject drop = element.getAsJsonObject();
            if (drop.has("item")) items.add(drop.get("item").getAsString());
        }
        return items;
    }

    private static JsonObject spawn(EntityType<?> type) {
        for (OWBiomeModifiers.SpawnRule rule : OWBiomeModifiers.SPAWN_RULES) {
            if (rule.entity().get() != type) continue;
            JsonObject spawn = new JsonObject();
            spawn.addProperty("biome_modifier", rule.modifier().location().toString());
            spawn.addProperty("weight", rule.weight());
            spawn.addProperty("min_count", rule.minCount());
            spawn.addProperty("max_count", rule.maxCount());
            JsonArray biomes = new JsonArray();
            rule.biomes().forEach(biome -> biomes.add(biome.location().toString()));
            spawn.add("biomes", biomes);
            return spawn;
        }
        return null;
    }

    private static JsonObject piste(OWWikiSpecies species, OWWikiLang lang) {
        OWPisteGraph graph = OWPisteGraphs.forSpecies(species.implementation().getSimpleName());
        JsonObject piste = new JsonObject();
        piste.addProperty("available", graph != null);
        if (graph == null) return piste;

        piste.addProperty("start_node", graph.getStartId());
        piste.addProperty("node_count", graph.nodes().size());

        int totalCost = 0;
        JsonArray nodes = new JsonArray();
        for (OWPisteNode node : graph.nodes()) {
            totalCost += node.getCost();
            JsonObject entry = new JsonObject();
            entry.addProperty("id", node.getId());
            entry.addProperty("x", node.getX());
            entry.addProperty("y", node.getY());
            entry.addProperty("type", node.getType().name());
            entry.addProperty("reward", node.getRewardAmount());
            entry.addProperty("cost", node.getCost());
            entry.addProperty("required_level", node.getRequiredLevel());
            entry.addProperty("exclusive_fork", node.isExclusiveFork());
            entry.addProperty("requires_all", node.isRequiresAll());
            JsonArray children = new JsonArray();
            node.getChildren().forEach(children::add);
            entry.add("children", children);
            if (!node.getOptions().isEmpty()) {
                JsonArray options = new JsonArray();
                for (OWPisteNode.Option option : node.getOptions()) {
                    JsonObject value = new JsonObject();
                    value.addProperty("type", option.type().name());
                    value.addProperty("amount", option.amount());
                    options.add(value);
                }
                entry.add("options", options);
            }
            if (!node.getAttackIds().isEmpty()) {
                JsonArray passives = new JsonArray();
                for (int id : node.getAttackIds()) {
                    OWPisteAttacks.AttackDef definition = OWPisteAttacks.get(id);
                    JsonObject value = new JsonObject();
                    value.addProperty("id", id);
                    if (definition != null) {
                        value.addProperty("icon", definition.icon().toString());
                        addIfPresent(value, "name", lang.entry(definition.nameKey()));
                        addIfPresent(value, "description", lang.entry(definition.descKey()));
                    }
                    passives.add(value);
                }
                entry.add("passives", passives);
            }
            nodes.add(entry);
        }
        piste.addProperty("total_cost", totalCost);
        piste.add("nodes", nodes);
        return piste;
    }

    private static JsonObject lore(OWWikiSpecies species, OWWikiLang lang) {
        JsonObject lore = new JsonObject();
        JsonArray manuscript = lang.pages("adventurer_manuscript.entity." + species.id() + ".page");
        if (manuscript != null) lore.add("adventurer_manuscript", manuscript);
        return lore.isEmpty() ? null : lore;
    }

    private static void probeGroup(JsonObject target, OWEntity probe, Class<?> implementation, String[][] table) {
        JsonArray unresolved = new JsonArray();
        for (String[] entry : table) {
            Object value = OWWikiReflect.call(probe, entry[1]).orElse(null);
            if (value != null) {
                OWWikiReflect.put(target, entry[0], value);
            } else if (OWWikiReflect.hasMethod(implementation, entry[1])) {
                unresolved.add(entry[1]);
            }
        }
        if (!unresolved.isEmpty()) target.add("unresolved", unresolved);
    }

    private static JsonObject nonEmpty(JsonObject object) {
        return object == null || object.isEmpty() ? null : object;
    }

    private static void addIfPresent(JsonObject target, String property, JsonElement value) {
        if (value != null) target.add(property, value);
    }
}
