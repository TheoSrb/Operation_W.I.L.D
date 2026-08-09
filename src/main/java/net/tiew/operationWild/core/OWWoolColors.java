package net.tiew.operationWild.core;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public final class OWWoolColors {

    private OWWoolColors() {}

    private static final int[] WOOL_RGB = {
            0xE9ECEC, 0xF07613, 0xBD44B3, 0x3AAFD9,
            0xF8C627, 0x70B919, 0xED8DAC, 0x3E4447,
            0x8E8E86, 0x158991, 0x792AAC, 0x35399D,
            0x724728, 0x546D1B, 0xA12722, 0x141519
    };

    private static final class Holder {
        static final Item[] WOOLS = new Item[DyeColor.values().length];
        static final Map<Item, DyeColor> BY_ITEM = new HashMap<>();

        static {
            for (DyeColor color : DyeColor.values()) {
                Item wool = BuiltInRegistries.ITEM.get(
                        ResourceLocation.withDefaultNamespace(color.getName() + "_wool"));
                WOOLS[color.getId()] = wool;
                BY_ITEM.put(wool, color);
            }
        }
    }

    public static Item wool(DyeColor color) {
        return Holder.WOOLS[color.getId()];
    }

    public static DyeColor colorOf(Item item) {
        return Holder.BY_ITEM.get(item);
    }

    public static DyeColor byId(int id) {
        return id >= 0 && id < DyeColor.values().length ? DyeColor.byId(id) : null;
    }

    public static int rgb(DyeColor color) {
        return WOOL_RGB[color.getId()];
    }
}
