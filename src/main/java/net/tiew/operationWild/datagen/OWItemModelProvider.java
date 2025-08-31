package net.tiew.operationWild.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.block.OWBlocks;
import net.tiew.operationWild.item.OWItems;

import java.util.LinkedHashMap;

public class OWItemModelProvider extends ItemModelProvider {

    public OWItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, OperationWild.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

        basicItem(OWItems.MEDAL_0.get());
        basicItem(OWItems.MEDAL_1.get());
        basicItem(OWItems.MEDAL_2.get());
        basicItem(OWItems.MEDAL_3.get());


        basicItem(OWItems.SEABUG_COCKPIT.get());
        basicItem(OWItems.SEABUG_HULL.get());
        basicItem(OWItems.SEABUG_PORTHOLE.get());


        basicItem(OWItems.PLANT_FIBER.get());
        basicItem(OWItems.LAVENDER_BOUQUET.get());


        basicItem(OWItems.PLANT_EMPRESS_MUSIC_DISC.get());


        basicItem(OWItems.SAVAGE_BERRIES.get());

        basicItem(OWItems.JADE.get());
        basicItem(OWItems.RUBY.get());
        basicItem(OWItems.PLATINUM_INGOT.get());

        basicItem(OWItems.BATTERY.get());

        handheldItem(OWItems.JADE_SWORD);
        handheldItem(OWItems.JADE_PICKAXE);
        handheldItem(OWItems.JADE_AXE);
        handheldItem(OWItems.JADE_SHOVEL);
        handheldItem(OWItems.JADE_HOE);

        handheldItem(OWItems.RUBY_SWORD);
        handheldItem(OWItems.RUBY_PICKAXE);
        handheldItem(OWItems.RUBY_AXE);
        handheldItem(OWItems.RUBY_SHOVEL);
        handheldItem(OWItems.RUBY_HOE);

        handheldItem(OWItems.PLATINUM_PLATED_RUBY_SWORD);

        handheldItem(OWItems.PRIMITIVE_SLINGSHOT);
        handheldItem(OWItems.PRIMITIVE_SICKLE);
        handheldItem(OWItems.MAYA_BLOWPIPE);

        basicItem(OWItems.TIGER_FUR.get());
        basicItem(OWItems.BOA_TONG.get());
        basicItem(OWItems.PREDATOR_TOOTH.get());
        basicItem(OWItems.VENOMOUS_TOOTH.get());
        basicItem(OWItems.VENOMOUS_GLANDS.get());
        basicItem(OWItems.PEACOCK_FEATHER.get());
        basicItem(OWItems.SHARK_FIN.get());
        basicItem(OWItems.STINGING_FILAMENT.get());
        basicItem(OWItems.BIOLUMINESCENT_JELLY.get());
        basicItem(OWItems.KODIAK_COAT.get());

        basicItem(OWItems.ANIMAL_SOUL.get());

        basicItem(OWItems.TIGER_SADDLE.get());
        basicItem(OWItems.KODIAK_SADDLE.get());
        basicItem(OWItems.BOA_SADDLE.get());
        basicItem(OWItems.PEACOCK_SADDLE.get());
        basicItem(OWItems.TIGER_SHARK_SADDLE.get());
        basicItem(OWItems.ELEPHANT_SADDLE.get());

        trimmedArmorItem(OWItems.CAMOUFLAGE_HELMET);
        trimmedArmorItem(OWItems.CAMOUFLAGE_CHESTPLATE);
        trimmedArmorItem(OWItems.CAMOUFLAGE_LEGGINGS);
        trimmedArmorItem(OWItems.CAMOUFLAGE_BOOTS);

        regularArmorItem(OWItems.RESURRECTION_AMULET);

        trimmedArmorItem(OWItems.JADE_HELMET);
        trimmedArmorItem(OWItems.JADE_CHESTPLATE);
        trimmedArmorItem(OWItems.JADE_LEGGINGS);
        trimmedArmorItem(OWItems.JADE_BOOTS);


        basicItem(OWItems.TRANQUILIZER_ARROW.get());
        basicItem(OWItems.VENOMOUS_ARROW.get());
        basicItem(OWItems.WOODEN_STINGER.get());
        basicItem(OWItems.TRANQUILIZER_WOODEN_STINGER.get());

        basicItem(OWItems.RAW_TIGER.get());
        basicItem(OWItems.COOKED_TIGER.get());
        basicItem(OWItems.RAW_KODIAK.get());
        basicItem(OWItems.COOKED_KODIAK.get());
        basicItem(OWItems.RAW_BOA.get());
        basicItem(OWItems.COOKED_BOA.get());
        basicItem(OWItems.RAW_PEACOCK.get());
        basicItem(OWItems.COOKED_PEACOCK.get());

        simpleBlockItemBlockTexture(OWBlocks.CAMELLIA);
        simpleBlockItemBlockTexture(OWBlocks.LAVENDER);

        withExistingParent(OWItems.TIGER_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(OWItems.BOA_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(OWItems.PEACOCK_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(OWItems.TIGER_SHARK_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(OWItems.CHAMELEON_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(OWItems.HYENA_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(OWItems.MANTA_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(OWItems.RED_PANDA_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(OWItems.KODIAK_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(OWItems.ELEPHANT_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(OWItems.MANDRILL_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(OWItems.JELLYFISH_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(OWItems.WALRUS_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));


        saplingItem(OWBlocks.REDWOOD_SAPLING);
        buttonItem(OWBlocks.REDWOOD_BUTTON, OWBlocks.REDWOOD_PLANKS);
        fenceItem(OWBlocks.REDWOOD_FENCE, OWBlocks.REDWOOD_PLANKS);
        basicItem(OWBlocks.REDWOOD_DOOR.asItem());

    }

    public void buttonItem(DeferredBlock<?> block, DeferredBlock<Block> baseBlock) {
        this.withExistingParent(block.getId().getPath(), mcLoc("block/button_inventory"))
                .texture("texture", ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID,
                        "block/" + baseBlock.getId().getPath()));
    }

    public void fenceItem(DeferredBlock<?> block, DeferredBlock<Block> baseBlock) {
        this.withExistingParent(block.getId().getPath(), mcLoc("block/fence_inventory"))
                .texture("texture", ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID,
                        "block/" + baseBlock.getId().getPath()));
    }

    public void wallItem(DeferredBlock<?> block, DeferredBlock<Block> baseBlock) {
        this.withExistingParent(block.getId().getPath(), mcLoc("block/wall_inventory"))
                .texture("wall", ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID,
                        "block/" + baseBlock.getId().getPath()));
    }

    private ItemModelBuilder saplingItem(DeferredBlock<Block> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "block/" + item.getId().getPath()));
    }

    private static LinkedHashMap<ResourceKey<TrimMaterial>, Float> trimMaterials = new LinkedHashMap<>();

    static {
        trimMaterials.put(TrimMaterials.QUARTZ, 0.1F);
        trimMaterials.put(TrimMaterials.IRON, 0.2F);
        trimMaterials.put(TrimMaterials.NETHERITE, 0.3F);
        trimMaterials.put(TrimMaterials.REDSTONE, 0.4F);
        trimMaterials.put(TrimMaterials.COPPER, 0.5F);
        trimMaterials.put(TrimMaterials.GOLD, 0.6F);
        trimMaterials.put(TrimMaterials.EMERALD, 0.7F);
        trimMaterials.put(TrimMaterials.DIAMOND, 0.8F);
        trimMaterials.put(TrimMaterials.LAPIS, 0.9F);
        trimMaterials.put(TrimMaterials.AMETHYST, 1.0F);
    }

    private ItemModelBuilder simpleBlockItemBlockTexture(DeferredHolder<Block, Block> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "block/" + item.getId().getPath()));
    }

    private ItemModelBuilder handheldItem(DeferredHolder<Item, Item> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/handheld")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "item/" + item.getId().getPath()));
    }

    private void trimmedArmorItem(DeferredHolder<Item, Item> itemRegistryObject) {
        final String MOD_ID = OperationWild.MOD_ID;

        if (itemRegistryObject.get() instanceof ArmorItem armorItem) {
            trimMaterials.forEach((trimMaterial, value) -> {
                float trimValue = value;

                String armorType = switch (armorItem.getEquipmentSlot()) {
                    case HEAD -> "helmet";
                    case CHEST -> "chestplate";
                    case LEGS -> "leggings";
                    case FEET -> "boots";
                    default -> "";
                };

                String armorItemPath = armorItem.toString();
                String trimPath = "trims/items/" + armorType + "_trim_" + trimMaterial.location().getPath();
                String currentTrimName = armorItemPath + "_" + trimMaterial.location().getPath() + "_trim";
                ResourceLocation armorItemResLoc = ResourceLocation.parse(armorItemPath);
                ResourceLocation trimResLoc = ResourceLocation.parse(trimPath);
                ResourceLocation trimNameResLoc = ResourceLocation.parse(currentTrimName);

                existingFileHelper.trackGenerated(trimResLoc, PackType.CLIENT_RESOURCES, ".png", "textures");

                getBuilder(currentTrimName)
                        .parent(new ModelFile.UncheckedModelFile("item/generated"))
                        .texture("layer0", armorItemResLoc.getNamespace() + ":item/" + armorItemResLoc.getPath())
                        .texture("layer1", trimResLoc);

                this.withExistingParent(itemRegistryObject.getId().getPath(),
                                mcLoc("item/generated"))
                        .override()
                        .model(new ModelFile.UncheckedModelFile(trimNameResLoc.getNamespace() + ":item/" + trimNameResLoc.getPath()))
                        .predicate(mcLoc("trim_type"), trimValue).end()
                        .texture("layer0",
                                ResourceLocation.fromNamespaceAndPath(MOD_ID,
                                        "item/" + itemRegistryObject.getId().getPath()));
            });
        }
    }

    private void regularArmorItem(DeferredHolder<Item, Item> itemRegistryObject) {
        final String MOD_ID = OperationWild.MOD_ID;

        if (itemRegistryObject.get() instanceof ArmorItem armorItem) {
            String armorType = switch (armorItem.getEquipmentSlot()) {
                case HEAD -> "helmet";
                case CHEST -> "chestplate";
                case LEGS -> "leggings";
                case FEET -> "boots";
                default -> "";
            };

            this.withExistingParent(itemRegistryObject.getId().getPath(),
                            mcLoc("item/generated"))
                    .texture("layer0",
                            ResourceLocation.fromNamespaceAndPath(MOD_ID,
                                    "item/" + itemRegistryObject.getId().getPath()));
        }
    }
}