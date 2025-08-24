package net.tiew.operationWild.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.tags.EnchantmentTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.enchantment.OWEnchantments;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class OWEnchantmentTagProvider extends EnchantmentTagsProvider {

    public OWEnchantmentTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, OperationWild.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {

        tag(EnchantmentTags.IN_ENCHANTING_TABLE)
                .addOptional(OWEnchantments.PUTRID.location())
                .addOptional(OWEnchantments.VAMPIRISM.location())
                .addOptional(OWEnchantments.LIGHTNESS.location())
                .addOptional(OWEnchantments.SLIDING.location())
                .addOptional(OWEnchantments.FAWNS_PROTECTION.location())
                .addOptional(OWEnchantments.VENOM_PROTECTION.location())
                .addOptional(OWEnchantments.REPTILIAN_CALAMITY.location())
                .addOptional(OWEnchantments.PLANT_PROVIDENCE.location());

    }
}