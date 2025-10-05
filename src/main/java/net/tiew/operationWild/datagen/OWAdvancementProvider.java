package net.tiew.operationWild.datagen;

import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.item.OWItems;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class OWAdvancementProvider extends AdvancementProvider {

    public OWAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, List.of(new OWAdvancements()));
    }

    public static class OWAdvancements implements AdvancementGenerator {

        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {

            AdvancementHolder rootOW = Advancement.Builder.advancement()
                    .display(OWItems.PLANT_FIBER.get(),
                            Component.translatable("ow.advancements.ow.title"),
                            Component.translatable("ow.advancements.ow.description"),
                            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/block/redwood_log.png"),
                            AdvancementType.TASK,
                            true,
                            true,
                            false)
                    .addCriterion("tick", PlayerTrigger.TriggerInstance.tick())
                    .save(saver, (OperationWild.MOD_ID + ":ow"));

            AdvancementHolder rootTaming = Advancement.Builder.advancement()
                    .display(OWItems.TRANQUILIZER_ARROW.get(),
                            Component.translatable("ow.advancements.ow_taming.title"),
                            Component.translatable("ow.advancements.ow_taming.description"),
                            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/block/redwood_planks.png"),
                            AdvancementType.TASK,
                            true,
                            true,
                            false)
                    .addCriterion("tick", PlayerTrigger.TriggerInstance.tick())
                    .save(saver, (OperationWild.MOD_ID + ":ow_taming"));



            AdvancementHolder noviceTamer =
                    createImpossibleAdvancement(rootTaming, saver, OWItems.MEDAL_3.get(), "novice_tamer", AdvancementType.CHALLENGE);
            AdvancementHolder renownedTamer =
                    createImpossibleAdvancement(noviceTamer, saver, OWItems.MEDAL_2.get(), "renowned_tamer", AdvancementType.CHALLENGE);
            AdvancementHolder professionalTamer =
                    createImpossibleAdvancement(renownedTamer, saver, OWItems.MEDAL_1.get(), "professional_tamer", AdvancementType.CHALLENGE);
            AdvancementHolder legendaryTamer =
                    createImpossibleAdvancement(professionalTamer, saver, OWItems.MEDAL_0.get(), "legendary_tamer", AdvancementType.CHALLENGE);

            AdvancementHolder youSeeMe =
                    createItemChangedAdvancement(rootOW, saver, OWItems.CAMOUFLAGE_HELMET.get(), new ItemLike[]{
                            OWItems.CAMOUFLAGE_HELMET.get(),
                            OWItems.CAMOUFLAGE_CHESTPLATE.get(),
                            OWItems.CAMOUFLAGE_LEGGINGS.get(),
                            OWItems.CAMOUFLAGE_BOOTS.get()
                    }, "camouflage_armor", "you_see_me", AdvancementType.TASK);

            AdvancementHolder friendDead =
                    createItemChangedAdvancement(rootOW, saver, OWItems.ANIMAL_SOUL.get(), OWItems.ANIMAL_SOUL.get(), "friend_dead", AdvancementType.GOAL);
            AdvancementHolder goodNight =
                    createItemChangedAdvancement(rootOW, saver, OWItems.TRANQUILIZER_ARROW.get(), OWItems.TRANQUILIZER_ARROW.get(), "good_night", AdvancementType.TASK);
            AdvancementHolder upgradedArrows =
                    createItemChangedAdvancement(rootOW, saver, OWItems.VENOMOUS_ARROW.get(), OWItems.VENOMOUS_ARROW.get(), "upgraded_arrows", AdvancementType.TASK);
            AdvancementHolder captain =
                    createImpossibleAdvancement(rootOW, saver, OWItems.SEABUG.get(), "captain", AdvancementType.CHALLENGE);


            AdvancementHolder tigerTamingAdvancement =
                    createNewTamingAdvancement(renownedTamer, saver, OWItems.TIGER_SPAWN_EGG.get(), OWEntityRegistry.TIGER.get(), "wild_meow", AdvancementType.GOAL);
            AdvancementHolder peacockTamingAdvancement =
                    createNewTamingAdvancement(noviceTamer, saver, OWItems.PEACOCK_SPAWN_EGG.get(), OWEntityRegistry.PEACOCK.get(), "colors", AdvancementType.GOAL);
            AdvancementHolder boaTamingAdvancement =
                    createNewTamingAdvancement(noviceTamer, saver, OWItems.BOA_SPAWN_EGG.get(), OWEntityRegistry.BOA.get(), "arms_what_for", AdvancementType.GOAL);
            AdvancementHolder kodiakTamingAdvancement =
                    createNewTamingAdvancement(renownedTamer, saver, OWItems.KODIAK_SPAWN_EGG.get(), OWEntityRegistry.KODIAK.get(), "big_fuzzball", AdvancementType.GOAL);
            AdvancementHolder walrusTamingAdvancement =
                    createNewTamingAdvancement(renownedTamer, saver, OWItems.KODIAK_SPAWN_EGG.get(), OWEntityRegistry.WALRUS.get(), "large_teeth", AdvancementType.GOAL);
            AdvancementHolder elephantTamingAdvancement =
                    createNewTamingAdvancement(renownedTamer, saver, OWItems.ELEPHANT_SPAWN_EGG.get(), OWEntityRegistry.ELEPHANT.get(), "trailer", AdvancementType.GOAL);


        }
    }

    public static AdvancementHolder createNewTamingAdvancement(AdvancementHolder parent, Consumer<AdvancementHolder> saver,
                                                               ItemLike itemIcon, EntityType<? extends LivingEntity> entityType, String title, AdvancementType type) {
        return Advancement.Builder.advancement()
                .parent(parent)
                .display(itemIcon,
                        Component.translatable("ow.advancements." + title + ".title"),
                        Component.translatable("ow.advancements." + title + ".description"),
                        null,
                        type,
                        true,
                        true,
                        false)
                .addCriterion("tamed_" + (entityType.toString().split("entity.ow.")[1]), TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().of(entityType)))
                .save(saver, "ow:" + title);
    }

    public static AdvancementHolder createImpossibleAdvancement(AdvancementHolder parent, Consumer<AdvancementHolder> saver,
                                                               ItemLike itemIcon, String title, AdvancementType type) {
        return Advancement.Builder.advancement()
                .parent(parent)
                .display(itemIcon,
                        Component.translatable("ow.advancements." + title + ".title"),
                        Component.translatable("ow.advancements." + title + ".description"),
                        null,
                        type,
                        true,
                        true,
                        false)
                .addCriterion("impossible", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()))
                .save(saver, (OperationWild.MOD_ID + ":" + title));
    }

    public static AdvancementHolder createItemChangedAdvancement(AdvancementHolder parent, Consumer<AdvancementHolder> saver,
                                                                 ItemLike itemIcon, ItemLike itemChanged, String title, AdvancementType type) {
        String itemDescId = itemChanged.asItem().getDescriptionId();
        return Advancement.Builder.advancement()
                .parent(parent)
                .display(itemIcon,
                        Component.translatable("ow.advancements." + title + ".title"),
                        Component.translatable("ow.advancements." + title + ".description"),
                        null,
                        type,
                        true,
                        true,
                        false)
                .addCriterion(itemDescId, InventoryChangeTrigger.TriggerInstance.hasItems(
                        ItemPredicate.Builder.item().of(itemChanged).build()
                ))
                .save(saver, OperationWild.MOD_ID + ":" + title);
    }

    public static AdvancementHolder createItemChangedAdvancement(AdvancementHolder parent, Consumer<AdvancementHolder> saver,
                                                                 ItemLike itemIcon, ItemLike[] itemChanged, String criterion, String title, AdvancementType type) {

        ItemPredicate[] predicates = new ItemPredicate[itemChanged.length];
        for (int i = 0; i < itemChanged.length; i++) {
            predicates[i] = ItemPredicate.Builder.item().of(itemChanged[i]).build();
        }

        return Advancement.Builder.advancement()
                .parent(parent)
                .display(itemIcon,
                        Component.translatable("ow.advancements." + title + ".title"),
                        Component.translatable("ow.advancements." + title + ".description"),
                        null,
                        type,
                        true,
                        true,
                        false)
                .addCriterion(criterion, InventoryChangeTrigger.TriggerInstance.hasItems(predicates))
                .save(saver, OperationWild.MOD_ID + ":" + title);
    }
}