package net.tiew.operationWild.datagen;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntityRegistry;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class OWAdvancementProvider extends AdvancementProvider {

    public OWAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, List.of(new OWAdvancements()));
    }

    public static class OWAdvancements implements AdvancementGenerator {

        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {

            // Advancement racine (root advancement)
            AdvancementHolder oceanExplorer = Advancement.Builder.advancement()
                    .display(Items.TROPICAL_FISH, // Icône
                            Component.translatable("advancement.operationwild.ocean_explorer.title"),
                            Component.translatable("advancement.operationwild.ocean_explorer.description"),
                            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/advancements/backgrounds/adventure.png"), // Background
                            AdvancementType.TASK,
                            true, // showToast
                            true, // announceChat
                            false) // hidden
                    .addCriterion("started", PlayerTrigger.TriggerInstance.tick())
                    .save(saver, (OperationWild.MOD_ID + "ocean_explorer"));

            // Advancement pour tuer une méduse
            Advancement.Builder.advancement()
                    .parent(oceanExplorer)
                    .display(Items.SLIME_BALL,
                            Component.translatable("advancement.operationwild.jellyfish_hunter.title"),
                            Component.translatable("advancement.operationwild.jellyfish_hunter.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false)
                    .addCriterion("kill_jellyfish",
                            KilledTrigger.TriggerInstance.playerKilledEntity(
                                    EntityPredicate.Builder.entity().of(OWEntityRegistry.JELLYFISH.get())))
                    .save(saver, (OperationWild.MOD_ID + "ocean_explorer"));


        }
    }
}