package net.tiew.operationWild.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.block.OWBlocks;
import net.tiew.operationWild.item.OWItems;
import net.tiew.operationWild.core.OWTags;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class OWItemTagProvider extends ItemTagsProvider {

    public OWItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                             CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, OperationWild.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {

        tag(ItemTags.PLANKS)
                .add(OWBlocks.REDWOOD_PLANKS.get().asItem());
        tag(ItemTags.LOGS_THAT_BURN)
                .add(OWBlocks.REDWOOD_WOOD.get().asItem())
                .add(OWBlocks.REDWOOD_LOG.get().asItem())
                .add(OWBlocks.STRIPPED_REDWOOD_LOG.get().asItem())
                .add(OWBlocks.STRIPPED_REDWOOD_WOOD.get().asItem());



        tag(ItemTags.ARROWS)
                .add(OWItems.TRANQUILIZER_ARROW.get())
                .add(OWItems.VENOMOUS_ARROW.get());



        tag(ItemTags.SWORDS)
                .add(OWItems.JADE_SWORD.get())
                .add(OWItems.RUBY_SWORD.get())
                .add(OWItems.REPTILIAN_DAGGER.get());
        tag(ItemTags.PICKAXES)
                .add(OWItems.JADE_PICKAXE.get())
                .add(OWItems.RUBY_PICKAXE.get());
        tag(ItemTags.AXES)
                .add(OWItems.JADE_AXE.get())
                .add(OWItems.RUBY_AXE.get());
        tag(ItemTags.SHOVELS)
                .add(OWItems.JADE_SHOVEL.get())
                .add(OWItems.RUBY_SHOVEL.get());
        tag(ItemTags.HOES)
                .add(OWItems.PRIMITIVE_SICKLE.get())
                .add(OWItems.JADE_HOE.get())
                .add(OWItems.RUBY_HOE.get());


        tag(ItemTags.HEAD_ARMOR)
                .add(OWItems.JADE_HELMET.get())
                .add(OWItems.CAMOUFLAGE_HELMET.get());
        tag(ItemTags.CHEST_ARMOR)
                .add(OWItems.JADE_CHESTPLATE.get())
                .add(OWItems.CAMOUFLAGE_CHESTPLATE.get());
        tag(ItemTags.LEG_ARMOR)
                .add(OWItems.JADE_LEGGINGS.get())
                .add(OWItems.CAMOUFLAGE_LEGGINGS.get());
        tag(ItemTags.FOOT_ARMOR)
                .add(OWItems.JADE_BOOTS.get())
                .add(OWItems.CAMOUFLAGE_BOOTS.get());



        tag(ItemTags.DURABILITY_ENCHANTABLE)
                .add(OWItems.PRIMITIVE_SPEAR.get())
                .add(OWItems.PRIMITIVE_SICKLE.get())
                .add(OWItems.PRIMITIVE_SLINGSHOT.get())
                .add(OWItems.MAYA_BLOWPIPE.get())
                .add(OWItems.REPTILIAN_DAGGER.get());
        tag(ItemTags.WEAPON_ENCHANTABLE)
                .add(OWItems.PRIMITIVE_SPEAR.get())
                .add(OWItems.PRIMITIVE_SICKLE.get())
                .add(OWItems.PRIMITIVE_SLINGSHOT.get())
                .add(OWItems.MAYA_BLOWPIPE.get())
                .add(OWItems.JADE_SWORD.get())
                .add(OWItems.RUBY_SWORD.get())
                .add(OWItems.REPTILIAN_DAGGER.get())
                .add(OWItems.JADE_AXE.get())
                .add(OWItems.RUBY_AXE.get())
                .add(OWItems.JADE_PICKAXE.get())
                .add(OWItems.RUBY_PICKAXE.get())
                .add(OWItems.JADE_SHOVEL.get())
                .add(OWItems.RUBY_SHOVEL.get())
                .add(OWItems.JADE_HOE.get())
                .add(OWItems.RUBY_HOE.get());
        tag(ItemTags.ARMOR_ENCHANTABLE)
                .add(OWItems.JADE_CHESTPLATE.get())
                .add(OWItems.CAMOUFLAGE_CHESTPLATE.get())
                .add(OWItems.JADE_BOOTS.get())
                .add(OWItems.CAMOUFLAGE_BOOTS.get())
                .add(OWItems.JADE_HELMET.get())
                .add(OWItems.CAMOUFLAGE_HELMET.get())
                .add(OWItems.JADE_LEGGINGS.get())
                .add(OWItems.CAMOUFLAGE_LEGGINGS.get());

        tag(Tags.Items.FOODS)
                .add(OWItems.RAW_KODIAK.get())
                .add(OWItems.RAW_PEACOCK.get())
                .add(OWItems.RAW_TIGER.get())
                .add(OWItems.RAW_BOA.get())
                .add(OWItems.COOKED_PEACOCK.get())
                .add(OWItems.COOKED_KODIAK.get())
                .add(OWItems.COOKED_TIGER.get())
                .add(OWItems.COOKED_BOA.get())
                .add(OWItems.SAVAGE_BERRIES.get());

        tag(Tags.Items.FOODS_RAW_MEAT)
                .add(OWItems.RAW_KODIAK.get())
                .add(OWItems.RAW_PEACOCK.get())
                .add(OWItems.RAW_TIGER.get())
                .add(OWItems.RAW_BOA.get());

        tag(Tags.Items.FOODS_COOKED_MEAT)
                .add(OWItems.COOKED_PEACOCK.get())
                .add(OWItems.COOKED_KODIAK.get())
                .add(OWItems.COOKED_TIGER.get())
                .add(OWItems.COOKED_BOA.get());

        tag(ItemTags.MEAT)
                .add(OWItems.RAW_KODIAK.get())
                .add(OWItems.RAW_PEACOCK.get())
                .add(OWItems.RAW_TIGER.get())
                .add(OWItems.RAW_BOA.get())
                .add(OWItems.COOKED_PEACOCK.get())
                .add(OWItems.COOKED_KODIAK.get())
                .add(OWItems.COOKED_TIGER.get())
                .add(OWItems.COOKED_BOA.get());

        tag(Tags.Items.FOODS_VEGETABLE)
                .add(OWItems.SAVAGE_BERRIES.get());





        tag(OWTags.Items.KODIAK_FOOD)
                .add(Items.SALMON);

        tag(OWTags.Items.KODIAK_DANGEROUS_FOOD)
                .add(Items.POISONOUS_POTATO)
                .add(Items.PUFFERFISH);

        tag(OWTags.Items.WALRUS_FOOD)
                .add(Items.NAUTILUS_SHELL);

        tag(OWTags.Items.CROCODILE_FOOD)
                .add(OWItems.RAW_TIGER.get());

        tag(OWTags.Items.LION_FOOD)
                .add(OWItems.RAW_TIGER.get());


        tag(OWTags.Items.OW_EGGS)
                .add(OWBlocks.PEACOCK_EGG.asItem());
    }
}