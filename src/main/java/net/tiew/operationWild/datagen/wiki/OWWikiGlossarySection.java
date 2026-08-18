package net.tiew.operationWild.datagen.wiki;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.attacks.OWAttackIds;
import net.tiew.operationWild.entity.config.OWEntityConfig;
import net.tiew.operationWild.entity.piste.OWPisteAttacks;
import net.tiew.operationWild.entity.piste.OWPisteNode;
import net.tiew.operationWild.entity.quests.daily_quests.DailyQuest;
import net.tiew.operationWild.entity.quests.daily_quests.DailyQuestRegistry;
import net.tiew.operationWild.entity.quests.daily_quests.DailyQuestTier;

public final class OWWikiGlossarySection {

    private static final int MAX_LEVEL = 50;

    private OWWikiGlossarySection() {}

    public static JsonObject build(OWWikiLang lang) {
        JsonObject glossary = new JsonObject();
        glossary.add("archetypes", archetypes());
        glossary.add("diets", diets());
        glossary.add("temperaments", temperaments());
        glossary.add("modes", modes());
        glossary.add("level_curve", levelCurve());
        glossary.add("daily_quests", dailyQuests(lang));
        glossary.add("piste", piste(lang));
        glossary.add("attack_ids", attackIds());
        glossary.add("entity_constants", OWWikiReflect.constants(OWEntity.class));
        return glossary;
    }

    private static JsonArray archetypes() {
        JsonArray archetypes = new JsonArray();
        for (OWEntityConfig.Archetypes archetype : OWEntityConfig.Archetypes.values()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("name", archetype.name());
            entry.addProperty("health_multiplier", archetype.getHealthMultiplier());
            entry.addProperty("damage_multiplier", archetype.getDamageMultiplier());
            entry.addProperty("speed_multiplier", archetype.getSpeedMultiplier());
            entry.addProperty("energy_multiplier", archetype.getEnergyMultiplier());
            archetypes.add(entry);
        }
        return archetypes;
    }

    private static JsonArray diets() {
        JsonArray diets = new JsonArray();
        for (OWEntityConfig.Diet diet : OWEntityConfig.Diet.values()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("name", diet.name());
            entry.addProperty("saturation_multiplier", diet.getSaturationMultiplier());
            diets.add(entry);
        }
        return diets;
    }

    private static JsonArray temperaments() {
        JsonArray temperaments = new JsonArray();
        for (OWEntityConfig.Temperament temperament : OWEntityConfig.Temperament.values()) {
            temperaments.add(temperament.name());
        }
        return temperaments;
    }

    private static JsonArray modes() {
        JsonArray modes = new JsonArray();
        for (OWEntity.Mode mode : OWEntity.Mode.values()) modes.add(mode.name());
        return modes;
    }

    private static JsonObject levelCurve() {
        JsonObject curve = new JsonObject();
        curve.addProperty("max_level", MAX_LEVEL);
        curve.addProperty("formula", "6 + 2 * level + (level * level) / 8");
        JsonArray levels = new JsonArray();
        int cumulative = 0;
        for (int level = 1; level < MAX_LEVEL; level++) {
            int required = OWUtils.xpToNextLevel(level);
            cumulative += required;
            JsonObject entry = new JsonObject();
            entry.addProperty("level", level);
            entry.addProperty("xp_to_next", required);
            entry.addProperty("cumulative_xp", cumulative);
            levels.add(entry);
        }
        curve.addProperty("total_xp", cumulative);
        curve.add("levels", levels);
        return curve;
    }

    private static JsonArray dailyQuests(OWWikiLang lang) {
        DailyQuestRegistry.init();
        JsonArray quests = new JsonArray();
        for (DailyQuest quest : DailyQuestRegistry.ALL) {
            if (quest == null) continue;
            JsonObject entry = new JsonObject();
            entry.addProperty("id", quest.getId());
            entry.addProperty("key", quest.getName());
            entry.addProperty("max_value", quest.getMaxValue());
            entry.addProperty("difficulty", quest.getDifficultyLevel());
            entry.addProperty("reward", quest.getReward());
            entry.addProperty("requires_level_up", quest.requiresLevelUp());
            DailyQuestTier tier = quest.getTier();
            if (tier != null) {
                entry.addProperty("tier", tier.name());
                entry.addProperty("tier_color", String.format(java.util.Locale.ROOT, "#%06X", tier.color() & 0xFFFFFF));
            }
            JsonObject translations = lang.translations(quest.getName());
            if (translations != null) entry.add("names", translations);
            quests.add(entry);
        }
        return quests;
    }

    private static JsonObject piste(OWWikiLang lang) {
        JsonObject piste = new JsonObject();
        JsonArray types = new JsonArray();
        for (OWPisteNode.Type type : OWPisteNode.Type.values()) types.add(type.name());
        piste.add("node_types", types);

        JsonArray passives = new JsonArray();
        for (int id = 0; id < 32; id++) {
            OWPisteAttacks.AttackDef definition = OWPisteAttacks.get(id);
            if (definition == null) continue;
            JsonObject entry = new JsonObject();
            entry.addProperty("id", id);
            entry.addProperty("icon", definition.icon().toString());
            JsonObject name = lang.entry(definition.nameKey());
            if (name != null) entry.add("name", name);
            JsonObject description = lang.entry(definition.descKey());
            if (description != null) entry.add("description", description);
            passives.add(entry);
        }
        piste.add("passives", passives);
        return piste;
    }

    private static JsonObject attackIds() {
        return OWWikiReflect.constants(OWAttackIds.class);
    }
}
