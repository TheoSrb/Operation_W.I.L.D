package net.tiew.operationWild.datagen.wiki;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.entity.bosses.PlantEmpressEntity;
import net.tiew.operationWild.entity.bosses.plantempress.EmpressAnim;
import net.tiew.operationWild.entity.bosses.plantempress.EmpressBalance;
import net.tiew.operationWild.entity.bosses.plantempress.EmpressRig;
import net.tiew.operationWild.entity.bosses.plantempress.EmpressSeedProjectile;
import net.tiew.operationWild.entity.bosses.plantempress.EmpressThornBush;
import net.tiew.operationWild.entity.bosses.plantempress.EmpressTimings;
import net.tiew.operationWild.entity.bosses.plantempress.EmpressVineSnare;

import java.util.Map;

public final class OWWikiBossSection {

    private static final double TICKS_PER_SECOND = 20.0;

    private OWWikiBossSection() {
    }

    public static JsonArray build(OWWikiLang lang, OWWikiSources sources) {
        JsonArray bosses = new JsonArray();
        bosses.add(plantEmpress(lang, sources));
        return bosses;
    }

    private static JsonObject plantEmpress(OWWikiLang lang, OWWikiSources sources) {
        EntityType<?> type = OWEntityRegistry.PLANT_EMPRESS.get();
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        JsonObject boss = new JsonObject();
        boss.addProperty("id", key.toString());
        boss.addProperty("path", key.getPath());
        boss.addProperty("class", PlantEmpressEntity.class.getName());
        boss.addProperty("simple_class", PlantEmpressEntity.class.getSimpleName());
        boss.addProperty("translation_key", type.getDescriptionId());
        put(boss, "names", lang.translations(type.getDescriptionId()));
        put(boss, "history", sources.history(PlantEmpressEntity.class));

        JsonObject classification = new JsonObject();
        classification.addProperty("boss", true);
        classification.addProperty("family", "terrestrial");
        classification.addProperty("mob_category", type.getCategory().getName());
        classification.addProperty("persistent", true);
        classification.addProperty("fire_immune", type.fireImmune());
        JsonArray hierarchy = new JsonArray();
        for (Class<?> c = PlantEmpressEntity.class; c != null && c != Object.class; c = c.getSuperclass()) hierarchy.add(c.getSimpleName());
        classification.add("hierarchy", hierarchy);
        boss.add("classification", classification);

        JsonObject attributes = new JsonObject();
        AttributeSupplier supplier = PlantEmpressEntity.createAttributes().build();
        for (Map.Entry<net.minecraft.resources.ResourceKey<Attribute>, Attribute> entry : BuiltInRegistries.ATTRIBUTE.entrySet()) {
            var holder = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(entry.getValue());
            if (supplier.hasAttribute(holder)) attributes.addProperty(entry.getKey().location().toString(), supplier.getBaseValue(holder));
        }
        attributes.addProperty("minecraft:generic.max_health", total(1));
        boss.add("attributes", attributes);

        JsonObject size = new JsonObject();
        size.addProperty("width", type.getWidth());
        size.addProperty("height", type.getHeight());
        size.addProperty("render_scale", EmpressRig.RENDER_SCALE);
        size.addProperty("main_head_height_blocks", 9.6);
        size.addProperty("side_head_height_blocks", 7.2);
        boss.add("size", size);

        JsonObject gameplay = new JsonObject();
        gameplay.addProperty("entity_color_hex", "#7B9E4F");
        gameplay.addProperty("fights_in_arena", false);
        gameplay.addProperty("summon", "/summon ow:plant_empress");
        boss.add("gameplay", gameplay);

        JsonArray sounds = new JsonArray();
        for (var field : net.tiew.operationWild.sound.OWSounds.class.getFields()) {
            if (!field.getName().startsWith("PLANT_EMPRESS")) continue;
            if (field.getName().endsWith("_KEY")) continue;
            String path = field.getName().toLowerCase(java.util.Locale.ROOT);
            if (path.equals("plant_empress_defeated_theme")) path = "plant_empress_defeated";
            JsonObject sound = new JsonObject();
            sound.addProperty("constant", field.getName());
            sound.addProperty("id", "ow:" + path);
            sound.addProperty("path", path);
            put(sound, "subtitle", lang.translations("subtitles.ow." + path.replaceFirst("plant_empress_", "plant_empress.")));
            sounds.add(sound);
        }
        boss.add("sounds", sounds);

        boss.add("fight", fight(lang));
        boss.add("constants", constantsBlock());
        return boss;
    }

    private static float total(int players) {
        float factor = 1f + EmpressBalance.HEALTH_PER_EXTRA_PLAYER * Math.max(0, players - 1);
        return (PlantEmpressEntity.MAIN_HEALTH + PlantEmpressEntity.SIDE_HEALTH * 2) * factor;
    }

    private static JsonObject fight(OWWikiLang lang) {
        JsonObject fight = new JsonObject();

        JsonArray heads = new JsonArray();
        heads.add(head("main", "head", PlantEmpressEntity.MAIN_HEALTH, true, EmpressRig.HEAD_SCALE[0]));
        heads.add(head("left", "head2", PlantEmpressEntity.SIDE_HEALTH, false, EmpressRig.HEAD_SCALE[1]));
        heads.add(head("right", "head3", PlantEmpressEntity.SIDE_HEALTH, false, EmpressRig.HEAD_SCALE[2]));
        fight.add("heads", heads);

        JsonArray scaling = new JsonArray();
        for (int players = 1; players <= 6; players++) {
            JsonObject row = new JsonObject();
            row.addProperty("players", players);
            float factor = 1f + EmpressBalance.HEALTH_PER_EXTRA_PLAYER * (players - 1);
            row.addProperty("main", PlantEmpressEntity.MAIN_HEALTH * factor);
            row.addProperty("side", PlantEmpressEntity.SIDE_HEALTH * factor);
            row.addProperty("total", total(players));
            scaling.add(row);
        }
        fight.add("health_scaling", scaling);

        JsonObject arena = new JsonObject();
        arena.addProperty("arena_radius", PlantEmpressEntity.ARENA_RADIUS);
        arena.addProperty("leash_radius", PlantEmpressEntity.LEASH_RADIUS);
        arena.addProperty("awaken_radius", PlantEmpressEntity.AWAKEN_RADIUS);
        arena.addProperty("regen_after_seconds", 10);
        arena.addProperty("regen_percent_per_second", 2);
        arena.addProperty("reset_after_seconds", 30);
        arena.addProperty("rage_wall_radius", PlantEmpressEntity.ARENA_RADIUS);
        arena.addProperty("rage_wall_damage", EmpressBalance.DAMAGE_WALL);
        arena.addProperty("burrow_max_distance_from_home", 14);
        fight.add("arena", arena);

        JsonObject spawn = new JsonObject();
        spawn.addProperty("duration_seconds", EmpressTimings.SPAWN_TICKS / TICKS_PER_SECOND);
        spawn.addProperty("burst_seconds", EmpressTimings.SPAWN_BURST / TICKS_PER_SECOND);
        spawn.addProperty("roar_seconds", EmpressTimings.SPAWN_ROAR / TICKS_PER_SECOND);
        spawn.addProperty("bar_show_seconds", EmpressTimings.SPAWN_BAR_SHOW / TICKS_PER_SECOND);
        spawn.addProperty("bar_fill_start_seconds", EmpressTimings.SPAWN_BAR_FILL_START / TICKS_PER_SECOND);
        spawn.addProperty("bar_fill_end_seconds", EmpressTimings.SPAWN_BAR_FILL_END / TICKS_PER_SECOND);
        JsonArray beats = new JsonArray();
        beat(beats, 0.0, "rumble", "Le sol gronde, le brouillard vert monte, la musique démarre.");
        beat(beats, 1.2, "roots", "Des racines percent le sol en cercle.");
        beat(beats, 3.5, "fingers", "Les lianes de la base sortent et grattent la terre.");
        beat(beats, 6.0, "silence", "Tout se fige : un souffle grave monte.");
        beat(beats, 7.5, "burst", "La plante jaillit sur le coup de la musique et repousse les joueurs.");
        beat(beats, 8.0, "unfurl", "Les trois têtes s'ouvrent l'une après l'autre.");
        beat(beats, 10.0, "stare", "La tête centrale se penche vers le joueur le plus proche.");
        beat(beats, 11.3, "roar", "Rugissement à trois voix ; la barre de vie se remplit.");
        beat(beats, 15.0, "fight", "Le combat commence.");
        spawn.add("timeline", beats);
        spawn.addProperty("invulnerable", true);
        fight.add("spawn", spawn);

        JsonArray stages = new JsonArray();
        stages.add(stage(1, "Trinité", "Trois têtes vivantes ; la tête centrale est scellée (invulnérable) tant qu'une tête latérale vit.",
                EmpressTimings.HEAD_TRANSITION_TICKS));
        stages.add(stage(2, "Veuve", "Une tête latérale est morte : elle pend et laisse une flaque toxique permanente. Les attaques du stade 1 restent.",
                EmpressTimings.HEAD_TRANSITION_TICKS));
        stages.add(stage(3, "Rage", "Les deux têtes latérales sont mortes : la tête centrale devient vulnérable, un mur de ronces ferme l'arène, les têtes mortes servent de massues.",
                EmpressTimings.RAGE_TRANSITION_TICKS));
        fight.add("stages", stages);

        JsonArray moves = new JsonArray();
        for (EmpressBalance.Move move : EmpressBalance.MOVES.values()) {
            JsonObject m = new JsonObject();
            m.addProperty("id", move.id());
            put(m, "names", lang.translations("wiki.ow.plant_empress.move." + move.id()));
            m.addProperty("head", move.head());
            JsonArray weights = new JsonArray();
            JsonArray stageList = new JsonArray();
            for (int i = 0; i < move.weights().length; i++) {
                weights.add(move.weights()[i]);
                if (move.weights()[i] > 0) stageList.add(i + 1);
            }
            m.add("weights", weights);
            m.add("stages", stageList);
            m.addProperty("cooldown_seconds", move.cooldown() / TICKS_PER_SECOND);
            m.addProperty("damage", move.damage());
            m.addProperty("telegraph", move.telegraph());
            m.addProperty("blockable", move.blockable());
            m.addProperty("counterable", move.counterable());
            moves.add(m);
        }
        fight.add("moves", moves);

        JsonObject timings = new JsonObject();
        timings.addProperty("head_tell_seconds", EmpressTimings.HEAD_TELL / TICKS_PER_SECOND);
        timings.addProperty("slam_impact_seconds", (EmpressTimings.HEAD_TELL + EmpressTimings.LUNGE_IMPACT) / TICKS_PER_SECOND);
        timings.addProperty("slam_planted_seconds", (EmpressTimings.LUNGE_PLANT_END - EmpressTimings.LUNGE_IMPACT) / TICKS_PER_SECOND);
        timings.addProperty("slam_miss_extra_seconds", EmpressTimings.LUNGE_MISS_HOLD / TICKS_PER_SECOND);
        timings.addProperty("bite_side_impact_seconds", (EmpressTimings.HEAD_TELL + EmpressTimings.BITE_IMPACT_SIDE) / TICKS_PER_SECOND);
        timings.addProperty("bite_main_impact_seconds", (EmpressTimings.HEAD_TELL + EmpressTimings.BITE_IMPACT_MAIN) / TICKS_PER_SECOND);
        timings.addProperty("plunge_impact_seconds", (EmpressTimings.HEAD_TELL + EmpressTimings.PLUNGE_IMPACT) / TICKS_PER_SECOND);
        timings.addProperty("whip_wave_start_seconds", EmpressTimings.WHIP_WAVE_START / TICKS_PER_SECOND);
        timings.addProperty("whip_radius_end", EmpressTimings.WHIP_RADIUS_END);
        timings.addProperty("quake_slam_seconds", EmpressTimings.QUAKE_SLAM / TICKS_PER_SECOND);
        timings.addProperty("quake_wave_gap_seconds", EmpressTimings.QUAKE_WAVE_GAP / TICKS_PER_SECOND);
        timings.addProperty("quake_wave_speed_blocks_per_second", 0.55 * TICKS_PER_SECOND);
        timings.addProperty("snare_warn_seconds", EmpressVineSnare.WARN_TICKS / TICKS_PER_SECOND);
        timings.addProperty("snare_armed_seconds", EmpressVineSnare.ARMED_TICKS / TICKS_PER_SECOND);
        timings.addProperty("snare_radius", EmpressVineSnare.RADIUS);
        timings.addProperty("chew_bites", EmpressTimings.CHEW_BITES.length);
        timings.addProperty("chew_release_seconds", EmpressTimings.CHEW_RELEASE / TICKS_PER_SECOND);
        timings.addProperty("spore_radius", EmpressSeedProjectile.SPORE_RADIUS);
        timings.addProperty("sprout_arm_seconds", EmpressThornBush.ARM_TICKS / TICKS_PER_SECOND);
        timings.addProperty("sprout_life_seconds", EmpressThornBush.LIFE_TICKS / TICKS_PER_SECOND);
        timings.addProperty("burrow_seconds", EmpressTimings.BURROW_TICKS / TICKS_PER_SECOND);
        timings.addProperty("burrow_eruption_seconds", EmpressTimings.BURROW_BURST / TICKS_PER_SECOND);
        timings.addProperty("burrow_eruption_radius", 4.7);
        timings.addProperty("stagger_seconds", EmpressTimings.STAGGER_DOWN_END / TICKS_PER_SECOND);
        timings.addProperty("stagger_immunity_seconds", EmpressTimings.STAGGER_IMMUNITY / TICKS_PER_SECOND);
        timings.addProperty("counter_window_seconds", EmpressTimings.COUNTER_WINDOW / TICKS_PER_SECOND);
        timings.addProperty("desperation_seconds", EmpressTimings.DESPERATION_TICKS / TICKS_PER_SECOND);
        timings.addProperty("desperation_waves", 5);
        timings.addProperty("desperation_sectors", 8);
        timings.addProperty("final_death_seconds", EmpressTimings.FINAL_DEATH_TICKS / TICKS_PER_SECOND);
        timings.addProperty("dissolve_start_seconds", EmpressTimings.DISSOLVE_START / TICKS_PER_SECOND);
        timings.addProperty("dissolve_end_seconds", EmpressTimings.DISSOLVE_END / TICKS_PER_SECOND);
        fight.add("timings", timings);

        JsonObject damage = new JsonObject();
        damage.addProperty("staggered", EmpressBalance.MULTIPLIER_STAGGERED);
        damage.addProperty("planted", EmpressBalance.MULTIPLIER_PLANTED);
        damage.addProperty("counter", EmpressBalance.MULTIPLIER_COUNTER);
        damage.addProperty("venom", EmpressBalance.MULTIPLIER_VENOM);
        damage.addProperty("head_invulnerability_seconds", 0.5);
        damage.addProperty("weapons", "Toutes : épées, haches, arcs, arbalètes, tridents, lances, frondes, projectiles d'autres mods");
        damage.addProperty("stem_and_base", "Parade : tiges et pied renvoient les coups et les flèches");
        fight.add("damage_multipliers", damage);

        JsonObject sap = new JsonObject();
        sap.addProperty("gain_factor", EmpressBalance.SAP_GAIN_FACTOR);
        sap.addProperty("melee_factor", EmpressBalance.SAP_MELEE_FACTOR);
        sap.addProperty("critical_factor", EmpressBalance.SAP_CRITICAL_FACTOR);
        sap.addProperty("counter_bonus", EmpressBalance.SAP_COUNTER_BONUS);
        sap.addProperty("planted_factor", EmpressBalance.SAP_PLANTED_FACTOR);
        fight.add("sap", sap);

        JsonObject agitation = new JsonObject();
        agitation.addProperty("agitation_start", EmpressBalance.AGITATION_START);
        agitation.addProperty("agitation_full", EmpressBalance.AGITATION_FULL);
        agitation.addProperty("frenzy_start", EmpressBalance.FRENZY_START);
        agitation.addProperty("frenzy_full", EmpressBalance.FRENZY_FULL);
        JsonArray reflexes = new JsonArray();
        reflexes.add(reflex(1, EmpressBalance.REFLEX_ONE));
        reflexes.add(reflex(2, EmpressBalance.REFLEX_TWO));
        reflexes.add(reflex(3, EmpressBalance.REFLEX_THREE));
        agitation.add("reflexes", reflexes);
        agitation.addProperty("reflex_recharge_seconds", 8);
        agitation.addProperty("desperation_threshold", EmpressBalance.DESPERATION_THRESHOLD);
        fight.add("agitation", agitation);

        JsonObject telegraphs = new JsonObject();
        telegraphs.addProperty("red", "Coup direct d'une tête (morsure, écrasement, plongeon) : sortir de la ligne ou du cercle.");
        telegraphs.addProperty("brown", "Ce qui vient du sol (racines, ondes, éruptions) : sauter au passage de l'onde ou s'écarter.");
        telegraphs.addProperty("green", "Poison et spores : quitter la zone avant l'éclatement.");
        telegraphs.addProperty("vine", "Lianes-piège : ne pas entrer dans le cercle vert tant qu'il est armé.");
        fight.add("telegraphs", telegraphs);

        JsonArray animations = new JsonArray();
        EmpressRig rig = EmpressRig.get();
        for (EmpressAnim anim : EmpressAnim.values()) {
            JsonObject a = new JsonObject();
            a.addProperty("id", anim.name());
            a.addProperty("field", anim.key);
            a.addProperty("seconds", rig.lengthSeconds(anim));
            animations.add(a);
        }
        fight.add("animations", animations);
        return fight;
    }

    private static JsonObject head(String id, String bone, float health, boolean core, float scale) {
        JsonObject head = new JsonObject();
        head.addProperty("id", id);
        head.addProperty("bone", bone);
        head.addProperty("health", health);
        head.addProperty("core", core);
        head.addProperty("scale", scale);
        return head;
    }

    private static JsonObject stage(int index, String name, String text, int transitionTicks) {
        JsonObject stage = new JsonObject();
        stage.addProperty("index", index);
        stage.addProperty("name", name);
        stage.addProperty("text", text);
        stage.addProperty("transition_seconds", transitionTicks / TICKS_PER_SECOND);
        JsonArray moves = new JsonArray();
        for (EmpressBalance.Move move : EmpressBalance.MOVES.values()) if (move.weights()[index - 1] > 0) moves.add(move.id());
        stage.add("moves", moves);
        return stage;
    }

    private static void beat(JsonArray beats, double seconds, String id, String text) {
        JsonObject beat = new JsonObject();
        beat.addProperty("seconds", seconds);
        beat.addProperty("id", id);
        beat.addProperty("text", text);
        beats.add(beat);
    }

    private static JsonObject reflex(int count, float below) {
        JsonObject reflex = new JsonObject();
        reflex.addProperty("count", count);
        reflex.addProperty("below_health_fraction", below);
        return reflex;
    }

    private static JsonObject constantsBlock() {
        JsonObject constants = new JsonObject();
        constants.add("entity", OWWikiReflect.constants(PlantEmpressEntity.class, true));
        constants.add("timings", OWWikiReflect.constants(EmpressTimings.class, true));
        constants.add("balance", OWWikiReflect.constants(EmpressBalance.class, true));
        return constants;
    }

    private static void put(JsonObject target, String property, JsonElement value) {
        if (value != null) target.add(property, value);
    }
}
