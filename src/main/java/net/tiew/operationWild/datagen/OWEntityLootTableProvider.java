package net.tiew.operationWild.datagen;

import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.tiew.operationWild.entity.OWEntityRegistry;
import net.tiew.operationWild.item.OWItems;

import java.util.stream.Stream;

public class OWEntityLootTableProvider extends EntityLootSubProvider {

    protected OWEntityLootTableProvider(HolderLookup.Provider provider) {
        super(FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    public void generate() {

        this.add(OWEntityRegistry.KODIAK.get(), LootTable.lootTable()
                .withPool(createBasicPool(OWItems.KODIAK_COAT.get(), 0.0F, 2.0F, true))
                .withPool(createBasicPool(OWItems.RAW_KODIAK.get(), 1.0F, 3.0F, true, true))
                .withPool(createRarePool(OWItems.PREDATOR_TOOTH.get(), 0.4F, 1, false))
        );

        this.add(OWEntityRegistry.TIGER.get(), LootTable.lootTable()
                .withPool(createBasicPool(OWItems.TIGER_FUR.get(), 1.0F, 3.0F, true))
                .withPool(createBasicPool(OWItems.RAW_TIGER.get(), 1.0F, 2.0F, true, true))
                .withPool(createRarePool(OWItems.PREDATOR_TOOTH.get(), 0.65F, 1, false))
        );

        this.add(OWEntityRegistry.CROCODILE.get(), LootTable.lootTable()
                .withPool(createBasicPool(OWItems.CROCODILE_SCALE.get(), 1.0F, 2.0F, true))
        );


        this.add(OWEntityRegistry.SEABUG.get(), LootTable.lootTable());
        this.add(OWEntityRegistry.SEABUG_SHARD_0.get(), LootTable.lootTable());
        this.add(OWEntityRegistry.SEABUG_SHARD_1.get(), LootTable.lootTable());
        this.add(OWEntityRegistry.SEABUG_SHARD_2.get(), LootTable.lootTable());
        this.add(OWEntityRegistry.PLANT_EMPRESS.get(), LootTable.lootTable());

    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return OWEntityRegistry.ENTITY_TYPES.getEntries().stream().map(DeferredHolder::get);
    }

    private LootPool.Builder createBasicPool(Item item, float minCount, float maxCount, boolean withLooting) {
        return createBasicPool(item, minCount, maxCount, withLooting, false);
    }

    private LootPool.Builder createBasicPool(Item item, float minCount, float maxCount, boolean withLooting, boolean withFireSmelting) {
        LootItem.Builder<?> lootItem = LootItem.lootTableItem(item)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(minCount, maxCount)));

        if (withLooting) {
            lootItem.apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.registries, UniformGenerator.between(0.0F, 1.0F)));
        }

        if (withFireSmelting) {
            lootItem.apply(SmeltItemFunction.smelted()
                    .when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                            EntityPredicate.Builder.entity()
                                    .flags(EntityFlagsPredicate.Builder.flags().setOnFire(true))
                                    .build())));
        }

        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(lootItem);
    }

    private LootPool.Builder createRarePool(Item item, float chance, int exactCount, boolean withLooting) {
        LootItem.Builder<?> lootItem = LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(ConstantValue.exactly(exactCount)));

        if (withLooting) {
            lootItem.apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.registries, UniformGenerator.between(0.0F, 1.0F)));
        }

        lootItem.when(LootItemRandomChanceCondition.randomChance(chance));

        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(lootItem);
    }

    private LootPool.Builder createPlayerKillPool(Item item, float chance, int exactCount) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(item)
                        .when(LootItemKilledByPlayerCondition.killedByPlayer())
                        .when(LootItemRandomChanceCondition.randomChance(chance))
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(exactCount)))
                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.registries, UniformGenerator.between(0.0F, 1.0F)))
                );
    }

    private LootPool.Builder createSimplePool(Item item, int count) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(count)))
                );
    }

    private LootPool.Builder createRarePoolWithLooting(Item item, float chance, float minCount, float maxCount) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(item)
                        .when(LootItemRandomChanceCondition.randomChance(chance))
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(minCount, maxCount)))
                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.registries, UniformGenerator.between(0.0F, 1.0F)))
                );
    }
}