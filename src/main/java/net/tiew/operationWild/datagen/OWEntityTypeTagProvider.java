package net.tiew.operationWild.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.block.OWBlocks;
import net.tiew.operationWild.core.OWTags;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.item.OWItems;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class OWEntityTypeTagProvider extends EntityTypeTagsProvider {

    public OWEntityTypeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, OperationWild.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {

        tag(EntityTypeTags.AQUATIC)
                .add(OWEntityRegistry.JELLYFISH.get())
                .add(OWEntityRegistry.TIGER_SHARK.get())
                .add(OWEntityRegistry.MANTA.get());

        tag(OWTags.Entities.REPTILES)
                .add(OWEntityRegistry.BOA.get())
                .add(OWEntityRegistry.CROCODILE.get());

        tag(OWTags.Entities.DROP_CARCASS)
                .add(EntityType.HORSE)
                .add(EntityType.PIG)
                .add(EntityType.COW)
                .add(EntityType.SHEEP)
                .add(EntityType.LLAMA)
                .add(OWEntityRegistry.MANDRILL.get())
                .add(OWEntityRegistry.CROCODILE.get())
                .add(OWEntityRegistry.TIGER.get())
                .add(OWEntityRegistry.WALRUS.get())
                .add(OWEntityRegistry.HYENA.get())
                .add(OWEntityRegistry.KODIAK.get());

    }
}