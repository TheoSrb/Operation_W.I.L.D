package net.tiew.operationWild.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.block.OWBlocks;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.item.custom.*;
import net.tiew.operationWild.item.custom.platinum_tools.PlatinumPlatedSwordItem;
import net.tiew.operationWild.item.custom.projectile.TranquilizerArrowItem;
import net.tiew.operationWild.item.custom.projectile.TranquilizerWoodenStingerItem;
import net.tiew.operationWild.item.custom.projectile.VenomousArrowItem;
import net.tiew.operationWild.item.custom.projectile.WoodenStingerItem;
import net.tiew.operationWild.sound.OWSounds;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

public class OWItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, OperationWild.MOD_ID);






    public static final DeferredHolder<Item, Item> MEDAL_0 = ITEMS.register("medal_0", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> MEDAL_1 = ITEMS.register("medal_1", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> MEDAL_2 = ITEMS.register("medal_2", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> MEDAL_3 = ITEMS.register("medal_3", () -> new Item(new Item.Properties()));


    public static final DeferredHolder<Item, Item> SEABUG_COCKPIT = ITEMS.register("seabug_cockpit", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> SEABUG_HULL = ITEMS.register("seabug_hull", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> SEABUG_PORTHOLE = ITEMS.register("seabug_porthole", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final DeferredHolder<Item, Item> SEABUG = ITEMS.register("seabug", () -> new SeaBugItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));





    public static final DeferredHolder<Item, Item> PLANT_FIBER = ITEMS.register("plant_fiber", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> LAVENDER_BOUQUET = ITEMS.register("lavender_bouquet", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> JADE = ITEMS.register("jade", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> RUBY = ITEMS.register("ruby", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PLATINUM_INGOT = ITEMS.register("platinum_ingot", () -> new Item(new Item.Properties()));


    public static final DeferredHolder<Item, Item> BATTERY = ITEMS.register("battery", () -> new Item(new Item.Properties().stacksTo(16).durability(100)));


    public static final DeferredHolder<Item, Item> JADE_SWORD = ITEMS.register("jade_sword", () -> new SwordItem(OWToolsTiers.JADE,  new Item.Properties().attributes(SwordItem.createAttributes(OWToolsTiers.JADE, 3, -2.4f))));
    public static final DeferredHolder<Item, Item> JADE_PICKAXE = ITEMS.register("jade_pickaxe", () -> new PickaxeItem(OWToolsTiers.JADE,  new Item.Properties().attributes(PickaxeItem.createAttributes(OWToolsTiers.JADE, 1, -2.8f))));
    public static final DeferredHolder<Item, Item> JADE_AXE = ITEMS.register("jade_axe", () -> new AxeItem(OWToolsTiers.JADE,  new Item.Properties().attributes(AxeItem.createAttributes(OWToolsTiers.JADE, 6, -3.1f))));
    public static final DeferredHolder<Item, Item> JADE_SHOVEL = ITEMS.register("jade_shovel", () -> new ShovelItem(OWToolsTiers.JADE,  new Item.Properties().attributes(ShovelItem.createAttributes(OWToolsTiers.JADE, 1.5F, -3.0f))));
    public static final DeferredHolder<Item, Item> JADE_HOE = ITEMS.register("jade_hoe", () -> new HoeItem(OWToolsTiers.JADE,  new Item.Properties().attributes(HoeItem.createAttributes(OWToolsTiers.JADE, -2.0f, -1.0f))));

    public static final DeferredHolder<Item, Item> RUBY_SWORD = ITEMS.register("ruby_sword", () -> new SwordItem(OWToolsTiers.RUBY,  new Item.Properties().attributes(SwordItem.createAttributes(OWToolsTiers.RUBY, 3, -2.4f))));
    public static final DeferredHolder<Item, Item> RUBY_PICKAXE = ITEMS.register("ruby_pickaxe", () -> new PickaxeItem(OWToolsTiers.RUBY,  new Item.Properties().attributes(PickaxeItem.createAttributes(OWToolsTiers.RUBY, 1, -2.8f))));
    public static final DeferredHolder<Item, Item> RUBY_AXE = ITEMS.register("ruby_axe", () -> new AxeItem(OWToolsTiers.RUBY,  new Item.Properties().attributes(AxeItem.createAttributes(OWToolsTiers.RUBY, 5, -3.0f))));
    public static final DeferredHolder<Item, Item> RUBY_SHOVEL = ITEMS.register("ruby_shovel", () -> new ShovelItem(OWToolsTiers.RUBY,  new Item.Properties().attributes(ShovelItem.createAttributes(OWToolsTiers.RUBY, 1.5F, -3.0f))));
    public static final DeferredHolder<Item, Item> RUBY_HOE = ITEMS.register("ruby_hoe", () -> new HoeItem(OWToolsTiers.RUBY,  new Item.Properties().attributes(HoeItem.createAttributes(OWToolsTiers.RUBY, -3.0f, 0.0f))));



    public static final DeferredHolder<Item, Item> PLATINUM_PLATED_RUBY_SWORD = ITEMS.register("platinum_plated_ruby_sword", () -> new PlatinumPlatedSwordItem(OWToolsTiers.RUBY,  new Item.Properties().attributes(SwordItem.createAttributes(OWToolsTiers.RUBY, 3, -2.4f))));
    public static final DeferredHolder<Item, Item> PLATINUM_PLATED_RUBY_PICKAXE = ITEMS.register("platinum_plated_ruby_pickaxe", () -> new PickaxeItem(OWToolsTiers.RUBY,  new Item.Properties().attributes(PickaxeItem.createAttributes(OWToolsTiers.RUBY, 1, -2.8f))));


    public static final DeferredHolder<Item, Item> SAVAGE_BERRIES = ITEMS.register("savage_berries", () -> new ItemNameBlockItem(OWBlocks.SAVAGE_BERRY_BUSH.get(), new Item.Properties().food(OWFoodsProperties.SAVAGE_BERRIES)));




    public static final DeferredHolder<Item, Item> PLANT_EMPRESS_MUSIC_DISC = ITEMS.register("plant_empress_music_disc", () -> new Item(new Item.Properties().jukeboxPlayable(OWSounds.PLANT_EMPRESS_THEME_LITE_KEY).stacksTo(1).rarity(Rarity.RARE)));




    // Mob Loots
    public static final DeferredHolder<Item, Item> TIGER_FUR = ITEMS.register("tiger_fur", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> BOA_TONG = ITEMS.register("boa_tong", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> FAWN_TOOTH = ITEMS.register("fawn_tooth", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> VENOMOUS_TOOTH = ITEMS.register("venomous_tooth", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> VENOMOUS_GLANDS = ITEMS.register("venomous_glands", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> PEACOCK_FEATHER = ITEMS.register("peacock_feather", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SHARK_FIN = ITEMS.register("shark_fin", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> STINGING_FILAMENT = ITEMS.register("stinging_filament", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> BIOLUMINESCENT_JELLY = ITEMS.register("bioluminescent_jelly", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ANIMAL_SOUL = ITEMS.register("animal_soul", () -> new AnimalSoulItem(new Item.Properties().stacksTo(1).fireResistant()));

    public static final DeferredHolder<Item, Item> TIGER_SADDLE = ITEMS.register("tiger_saddle", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> BOA_SADDLE = ITEMS.register("boa_saddle", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> PEACOCK_SADDLE = ITEMS.register("peacock_saddle", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> TIGER_SHARK_SADDLE = ITEMS.register("tiger_shark_saddle", () -> new Item(new Item.Properties().stacksTo(1)));

    // Armors
    public static final DeferredHolder<Item, Item> CAMOUFLAGE_HELMET = ITEMS.register("camouflage_helmet", () -> new OWArmorItem(OWArmorMaterials.CAMOUFLAGE_ARMOR_MATERIAL, ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(3))));
    public static final DeferredHolder<Item, Item> CAMOUFLAGE_CHESTPLATE = ITEMS.register("camouflage_chestplate", () -> new OWArmorItem(OWArmorMaterials.CAMOUFLAGE_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(3))));
    public static final DeferredHolder<Item, Item> CAMOUFLAGE_LEGGINGS = ITEMS.register("camouflage_leggings", () -> new OWArmorItem(OWArmorMaterials.CAMOUFLAGE_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(3))));
    public static final DeferredHolder<Item, Item> CAMOUFLAGE_BOOTS = ITEMS.register("camouflage_boots", () -> new OWArmorItem(OWArmorMaterials.CAMOUFLAGE_ARMOR_MATERIAL, ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(3))));

    public static final DeferredHolder<Item, Item> RESURRECTION_AMULET = ITEMS.register("resurrection_amulet", () -> new OWArmorItem(OWArmorMaterials.RESURRECTION_AMULET_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(3)).rarity(Rarity.UNCOMMON)));

    public static final DeferredHolder<Item, Item> JADE_HELMET = ITEMS.register("jade_helmet", () -> new OWArmorItem(OWArmorMaterials.JADE_ARMOR_MATERIAL, ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(24))));
    public static final DeferredHolder<Item, Item> JADE_CHESTPLATE = ITEMS.register("jade_chestplate", () -> new OWArmorItem(OWArmorMaterials.JADE_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(24))));
    public static final DeferredHolder<Item, Item> JADE_LEGGINGS = ITEMS.register("jade_leggings", () -> new OWArmorItem(OWArmorMaterials.JADE_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(24))));
    public static final DeferredHolder<Item, Item> JADE_BOOTS = ITEMS.register("jade_boots", () -> new OWArmorItem(OWArmorMaterials.JADE_ARMOR_MATERIAL, ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(24))));


    public static final DeferredHolder<Item, Item> PRIMITIVE_SPEAR = ITEMS.register("primitive_spear", () -> new PrimitiveSpearItem(new Item.Properties().stacksTo(1).durability(211).attributes(SwordItem.createAttributes(OWToolsTiers.PRIMITIVE, 5, -3.0f))));
    public static final DeferredHolder<Item, Item> PRIMITIVE_SLINGSHOT = ITEMS.register("primitive_slingshot", () -> new PrimitiveSlingshotItem(new Item.Properties().stacksTo(1).durability(211)));
    public static final DeferredHolder<Item, Item> PRIMITIVE_SICKLE = ITEMS.register("primitive_sickle", () -> new PrimitiveSickleItem(new Item.Properties().stacksTo(1).durability(211)));
    public static final DeferredHolder<Item, Item> MAYA_BLOWPIPE = ITEMS.register("maya_blowpipe", () -> new MayaBlowpipeItem(new Item.Properties().stacksTo(1).durability(211)));


    public static final DeferredHolder<Item, Item> WOODEN_STINGER = ITEMS.register("wooden_stinger", () -> new WoodenStingerItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> TRANQUILIZER_WOODEN_STINGER = ITEMS.register("tranquilizer_wooden_stinger", () -> new TranquilizerWoodenStingerItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> TRANQUILIZER_ARROW = ITEMS.register("tranquilizer_arrow", () -> new TranquilizerArrowItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> VENOMOUS_ARROW = ITEMS.register("venomous_arrow", () -> new VenomousArrowItem(new Item.Properties()));


    public static final DeferredHolder<Item, Item> RAW_TIGER = ITEMS.register("raw_tiger", () -> new Item(new Item.Properties().food(OWFoodsProperties.RAW_TIGER)));
    public static final DeferredHolder<Item, Item> COOKED_TIGER = ITEMS.register("cooked_tiger", () -> new Item(new Item.Properties().food(OWFoodsProperties.COOKED_TIGER)));

    public static final DeferredHolder<Item, Item> RAW_KODIAK = ITEMS.register("raw_kodiak", () -> new Item(new Item.Properties().food(OWFoodsProperties.RAW_TIGER)));
    public static final DeferredHolder<Item, Item> COOKED_KODIAK = ITEMS.register("cooked_kodiak", () -> new Item(new Item.Properties().food(OWFoodsProperties.COOKED_TIGER)));

    public static final DeferredHolder<Item, Item> RAW_BOA = ITEMS.register("raw_boa", () -> new Item(new Item.Properties().food(OWFoodsProperties.RAW_BOA)));
    public static final DeferredHolder<Item, Item> COOKED_BOA = ITEMS.register("cooked_boa", () -> new Item(new Item.Properties().food(OWFoodsProperties.COOKED_BOA)));

    public static final DeferredHolder<Item, Item> RAW_PEACOCK = ITEMS.register("raw_peacock", () -> new Item(new Item.Properties().food(OWFoodsProperties.RAW_PEACOCK)));
    public static final DeferredHolder<Item, Item> COOKED_PEACOCK = ITEMS.register("cooked_peacock", () -> new Item(new Item.Properties().food(OWFoodsProperties.COOKED_PEACOCK)));


    // Mob Spawn Eggs
    public static final DeferredHolder<Item, Item> TIGER_SPAWN_EGG = ITEMS.register("tiger_spawn_egg",
            () -> new DeferredSpawnEggItem(OWEntityRegistry.TIGER, 0xc47037, 0x482716, new Item.Properties()));
    public static final DeferredHolder<Item, Item> BOA_SPAWN_EGG = ITEMS.register("boa_spawn_egg",
            () -> new DeferredSpawnEggItem(OWEntityRegistry.BOA, 0x9c9e5d, 0x534612, new Item.Properties()));
    public static final DeferredHolder<Item, Item> PEACOCK_SPAWN_EGG = ITEMS.register("peacock_spawn_egg",
            () -> new DeferredSpawnEggItem(OWEntityRegistry.PEACOCK, 0x506acf, 0x73c656, new Item.Properties()));
    public static final DeferredHolder<Item, Item> TIGER_SHARK_SPAWN_EGG = ITEMS.register("tiger_shark_spawn_egg",
            () -> new DeferredSpawnEggItem(OWEntityRegistry.TIGER_SHARK, 0x565047, 0xc6bca4, new Item.Properties()));
    public static final DeferredHolder<Item, Item> WALRUS_SPAWN_EGG = ITEMS.register("walrus_spawn_egg",
            () -> new DeferredSpawnEggItem(OWEntityRegistry.WALRUS, 0x8b745f, 0x5f4539, new Item.Properties()));
    public static final DeferredHolder<Item, Item> CHAMELEON_SPAWN_EGG = ITEMS.register("chameleon_spawn_egg",
            () -> new DeferredSpawnEggItem(OWEntityRegistry.CHAMELEON, 0x7c9550, 0x567134, new Item.Properties()));
    public static final DeferredHolder<Item, Item> HYENA_SPAWN_EGG = ITEMS.register("hyena_spawn_egg",
            () -> new DeferredSpawnEggItem(OWEntityRegistry.HYENA, 0x936e49, 0x653f29, new Item.Properties()));
    public static final DeferredHolder<Item, Item> JELLYFISH_SPAWN_EGG = ITEMS.register("jellyfish_spawn_egg",
            () -> new DeferredSpawnEggItem(OWEntityRegistry.JELLYFISH, 0x648fce, 0x5568ba, new Item.Properties()));
    public static final DeferredHolder<Item, Item> MANTA_SPAWN_EGG = ITEMS.register("manta_spawn_egg",
            () -> new DeferredSpawnEggItem(OWEntityRegistry.MANTA, 0x3d4c60, 0xcbe3e8, new Item.Properties()));
    public static final DeferredHolder<Item, Item> RED_PANDA_SPAWN_EGG = ITEMS.register("red_panda_spawn_egg",
            () -> new DeferredSpawnEggItem(OWEntityRegistry.RED_PANDA, 0xcc7e4e, 0xe7ddd7, new Item.Properties()));
    public static final DeferredHolder<Item, Item> KODIAK_SPAWN_EGG = ITEMS.register("kodiak_spawn_egg",
            () -> new DeferredSpawnEggItem(OWEntityRegistry.KODIAK, 0x7d5a45, 0x4e3225, new Item.Properties()));


    public static void register(IEventBus eventBus) { ITEMS.register(eventBus);}
}
