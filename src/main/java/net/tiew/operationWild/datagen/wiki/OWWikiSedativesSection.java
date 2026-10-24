package net.tiew.operationWild.datagen.wiki;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.misc.SlingshotProjectile;
import net.tiew.operationWild.entity.misc.TranquilizerArrow;
import net.tiew.operationWild.entity.misc.TranquilizerWoodenStinger;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.item.custom.MayaBlowpipeItem;
import net.tiew.operationWild.item.custom.PrimitiveSlingshotItem;

import java.util.Map;
import java.util.function.IntToDoubleFunction;

public final class OWWikiSedativesSection {

    private static final float FULL_DRAW_ARROW_SPEED = 3.0f;
    private static final double POWER_DAMAGE_BASE = 0.5;
    private static final double POWER_DAMAGE_PER_LEVEL = 0.5;
    private static final int MAX_POWER_LEVEL = 5;
    private static final int MAX_DRAW_SCAN_TICKS = 200;
    private static final String RESISTANCE_SUFFIX = "_SEDATIVE_FACTOR";

    private OWWikiSedativesSection() {}

    public static JsonObject build(OWWikiLang lang) {
        JsonObject sedatives = new JsonObject();
        sedatives.addProperty("headshot_factor", OWEntity.HEADSHOT_SOMNOLENCE_FACTOR);
        sedatives.addProperty("damage_divisor", OWEntity.SEDATIVE_DAMAGE_DIVISOR);
        sedatives.addProperty("min_damage", OWEntity.SEDATIVE_MIN_DAMAGE);
        JsonArray weapons = new JsonArray();
        weapons.add(bow(lang));
        weapons.add(blowpipe(lang));
        weapons.add(slingshot(lang));
        sedatives.add("weapons", weapons);
        sedatives.add("resistances", resistances());
        return sedatives;
    }

    private static JsonObject bow(OWWikiLang lang) {
        JsonObject bow = weapon("bow", "damage", Items.BOW, OWItems.TRANQUILIZER_ARROW.get(),
                TranquilizerArrow.TRANQUILIZER_EFFECTIVENESS, fullDrawTicks(BowItem::getPowerForTime), lang);
        bow.addProperty("base_damage", TranquilizerArrow.BASE_DAMAGE);
        bow.addProperty("full_draw_speed", FULL_DRAW_ARROW_SPEED);
        bow.addProperty("crit_on_full_draw", true);
        JsonArray levels = new JsonArray();
        for (int level = 0; level <= MAX_POWER_LEVEL; level++) {
            double base = TranquilizerArrow.BASE_DAMAGE + (level == 0 ? 0 : POWER_DAMAGE_BASE + POWER_DAMAGE_PER_LEVEL * (level - 1));
            int damage = (int) Math.ceil(FULL_DRAW_ARROW_SPEED * base);
            int critSpan = damage / 2 + 2;
            JsonObject entry = new JsonObject();
            entry.addProperty("power_level", level);
            entry.addProperty("damage", damage);
            entry.addProperty("crit_extra_max", critSpan - 1);
            entry.addProperty("power_min", arrowPower(damage));
            entry.addProperty("power_max", arrowPower(damage + critSpan - 1));
            double average = 0;
            for (int extra = 0; extra < critSpan; extra++) average += arrowPower(damage + extra);
            entry.addProperty("power_avg", average / critSpan);
            levels.add(entry);
            if (level == 0) {
                bow.addProperty("power_uncharged", arrowPower(damage));
                bow.addProperty("power_min", arrowPower(damage));
                bow.addProperty("power_max", arrowPower(damage + critSpan - 1));
                bow.addProperty("power_avg", average / critSpan);
                bow.addProperty("crit_max_chance", 1.0 / critSpan);
            }
        }
        bow.add("power_enchant", levels);
        return bow;
    }

    private static JsonObject blowpipe(OWWikiLang lang) {
        int power = TranquilizerWoodenStinger.TRANQUILIZER_EFFECTIVENESS;
        JsonObject blowpipe = weapon("blowpipe", "flat", OWItems.MAYA_BLOWPIPE.get(), OWItems.TRANQUILIZER_WOODEN_STINGER.get(),
                power, fullDrawTicks(MayaBlowpipeItem::getPowerForTime), lang);
        flatPower(blowpipe, power);
        return blowpipe;
    }

    private static JsonObject slingshot(OWWikiLang lang) {
        JsonObject slingshot = weapon("slingshot", "damage", OWItems.PRIMITIVE_SLINGSHOT.get(), Items.COBBLESTONE,
                SlingshotProjectile.TRANQUILIZER_EFFECTIVENESS, fullDrawTicks(PrimitiveSlingshotItem::getPowerForTime), lang);
        slingshot.addProperty("hit_damage", SlingshotProjectile.HIT_DAMAGE);
        slingshot.addProperty("power_independent_of_draw", true);
        flatPower(slingshot, arrowLikePower(SlingshotProjectile.TRANQUILIZER_EFFECTIVENESS, SlingshotProjectile.HIT_DAMAGE));
        return slingshot;
    }

    private static JsonObject weapon(String key, String formula, Item launcher, Item ammo, int effectiveness, int drawTicks, OWWikiLang lang) {
        JsonObject weapon = new JsonObject();
        weapon.addProperty("key", key);
        weapon.addProperty("formula", formula);
        weapon.addProperty("launcher", BuiltInRegistries.ITEM.getKey(launcher).toString());
        weapon.add("launcher_names", lang.translations(launcher.getDescriptionId()));
        weapon.addProperty("ammo", BuiltInRegistries.ITEM.getKey(ammo).toString());
        weapon.add("ammo_names", lang.translations(ammo.getDescriptionId()));
        weapon.addProperty("effectiveness", effectiveness);
        weapon.addProperty("full_draw_ticks", drawTicks);
        weapon.addProperty("interval_seconds", drawTicks / 20.0);
        return weapon;
    }

    private static void flatPower(JsonObject weapon, int power) {
        weapon.addProperty("power_uncharged", power);
        weapon.addProperty("power_min", power);
        weapon.addProperty("power_avg", (double) power);
        weapon.addProperty("power_max", power);
    }

    private static int arrowPower(int damage) {
        return arrowLikePower(TranquilizerArrow.TRANQUILIZER_EFFECTIVENESS, damage);
    }

    private static int arrowLikePower(int effectiveness, float damage) {
        if (damage <= OWEntity.SEDATIVE_MIN_DAMAGE) return 0;
        return (int) (effectiveness * (damage / OWEntity.SEDATIVE_DAMAGE_DIVISOR));
    }

    private static int fullDrawTicks(IntToDoubleFunction powerForTime) {
        for (int ticks = 1; ticks <= MAX_DRAW_SCAN_TICKS; ticks++) {
            if (powerForTime.applyAsDouble(ticks) >= 1.0) return ticks;
        }
        return MAX_DRAW_SCAN_TICKS;
    }

    private static JsonArray resistances() {
        JsonArray resistances = new JsonArray();
        for (OWWikiSpecies species : OWWikiSpecies.all()) {
            for (Map.Entry<String, JsonElement> constant : OWWikiReflect.constants(species.implementation(), true).entrySet()) {
                if (!constant.getKey().endsWith(RESISTANCE_SUFFIX)) continue;
                JsonObject entry = new JsonObject();
                entry.addProperty("entity", species.key().toString());
                entry.addProperty("condition", constant.getKey().substring(0, constant.getKey().length() - RESISTANCE_SUFFIX.length()).toLowerCase());
                entry.add("factor", constant.getValue());
                resistances.add(entry);
            }
        }
        return resistances;
    }
}
