package net.tiew.operationWild.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.custom.living.BoaEntity;
import net.tiew.operationWild.entity.custom.living.PeacockEntity;
import net.tiew.operationWild.entity.custom.living.TigerEntity;
import net.tiew.operationWild.entity.custom.living.TigerSharkEntity;
import net.tiew.operationWild.entity.custom.living.boss.PlantEmpressEntity;
import net.tiew.operationWild.entity.custom.misc.*;
import net.tiew.operationWild.entity.custom.object.SeaBugShard0Entity;
import net.tiew.operationWild.entity.custom.object.SeaBugShard1Entity;
import net.tiew.operationWild.entity.custom.object.SeaBugShard2Entity;
import net.tiew.operationWild.entity.custom.vehicle.SeaBugEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class OWEntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, OperationWild.MOD_ID);
    public static void register(IEventBus eventBus) { ENTITY_TYPES.register(eventBus);}

    public static final DeferredHolder<EntityType<?>, EntityType<TigerEntity>> TIGER = ENTITY_TYPES.register("tiger", () -> EntityType.Builder.<TigerEntity>of((type, world) -> new TigerEntity(type, world, 1.05f, 4000, 2), MobCategory.CREATURE).sized(1.3f, 1.3f).build("tiger"));
    public static final DeferredHolder<EntityType<?>, EntityType<SeaBugEntity>> SEABUG = ENTITY_TYPES.register("seabug", () -> EntityType.Builder.<SeaBugEntity>of((type, world) -> new SeaBugEntity(type, world, 1.1f, 0, 0), MobCategory.WATER_CREATURE).sized(2.0f, 2.1f).build("SeaBug"));
    public static final DeferredHolder<EntityType<?>, EntityType<BoaEntity>> BOA = ENTITY_TYPES.register("boa", () -> EntityType.Builder.<BoaEntity>of((type, world) -> new BoaEntity(type, world, 1.0f, 2000, 4), MobCategory.CREATURE).sized(1.5f, 0.5f).build("boa"));
    public static final DeferredHolder<EntityType<?>, EntityType<PeacockEntity>> PEACOCK = ENTITY_TYPES.register("peacock", () -> EntityType.Builder.<PeacockEntity>of((type, world) -> new PeacockEntity(type, world, 1.1f, 650, 2), MobCategory.CREATURE).sized(0.7f, 1.0f).build("peacock"));
    public static final DeferredHolder<EntityType<?>, EntityType<TigerSharkEntity>> TIGER_SHARK = ENTITY_TYPES.register("tiger_shark", () -> EntityType.Builder.<TigerSharkEntity>of((type, world) -> new TigerSharkEntity(type, world, 1.0f, 2850, 1), MobCategory.WATER_CREATURE).sized(2.1f, 0.8f).build("tiger_shark"));


    public static final DeferredHolder<EntityType<?>, EntityType<SeaBugShard0Entity>> SEABUG_SHARD_0 = ENTITY_TYPES.register("seabug_shard_0", () -> EntityType.Builder.<SeaBugShard0Entity>of((type, world) -> new SeaBugShard0Entity(type, world, 1.1f, 0, 0), MobCategory.WATER_CREATURE).sized(1.8f, 1.6f).build("seabug_shard_0"));
    public static final DeferredHolder<EntityType<?>, EntityType<SeaBugShard1Entity>> SEABUG_SHARD_1 = ENTITY_TYPES.register("seabug_shard_1", () -> EntityType.Builder.<SeaBugShard1Entity>of((type, world) -> new SeaBugShard1Entity(type, world, 1.1f, 0, 0), MobCategory.WATER_CREATURE).sized(1.8f, 1.1f).build("seabug_shard_1"));
    public static final DeferredHolder<EntityType<?>, EntityType<SeaBugShard2Entity>> SEABUG_SHARD_2 = ENTITY_TYPES.register("seabug_shard_2", () -> EntityType.Builder.<SeaBugShard2Entity>of((type, world) -> new SeaBugShard2Entity(type, world, 1.1f, 0, 0), MobCategory.WATER_CREATURE).sized(1.3f, 1.1f).build("seabug_shard_2"));


    public static final DeferredHolder<EntityType<?>, EntityType<PlantEmpressEntity>> PLANT_EMPRESS = ENTITY_TYPES.register("plant_empress", () -> EntityType.Builder.<PlantEmpressEntity>of((type, world) -> new PlantEmpressEntity(type, world, 1.3f, 100000, 0), MobCategory.CREATURE).sized(2.5f, 3).build("plant_empress"));

    public static final DeferredHolder<EntityType<?>, EntityType<PrimitiveSpearProjectileEntity>> PRIMITIVE_SPEAR_PROJECTILE = ENTITY_TYPES.register("primitive_spear", () -> EntityType.Builder.<PrimitiveSpearProjectileEntity>of(PrimitiveSpearProjectileEntity::new, MobCategory.MISC).sized(0.5f, 0.5f).build("primitive_spear_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<TranquilizerArrow>> TRANQUILIZER_ARROW = ENTITY_TYPES.register("tranquilizer_arrow", () -> EntityType.Builder.<TranquilizerArrow>of(TranquilizerArrow::new, MobCategory.MISC).sized(0.5f, 0.5f).build("tranquilizer_arrow"));
    public static final DeferredHolder<EntityType<?>, EntityType<WoodenStinger>> WOODEN_STINGER = ENTITY_TYPES.register("wooden_stinger", () -> EntityType.Builder.<WoodenStinger>of(WoodenStinger::new, MobCategory.MISC).sized(0.25f, 0.25f).build("wooden_stinger"));
    public static final DeferredHolder<EntityType<?>, EntityType<TranquilizerWoodenStinger>> TRANQUILIZER_WOODEN_STINGER = ENTITY_TYPES.register("tranquilizer_wooden_stinger", () -> EntityType.Builder.<TranquilizerWoodenStinger>of(TranquilizerWoodenStinger::new, MobCategory.MISC).sized(0.25f, 0.25f).build("tranquilizer_wooden_stinger"));
    public static final DeferredHolder<EntityType<?>, EntityType<VenomousArrow>> VENOMOUS_ARROW = ENTITY_TYPES.register("venomous_arrow", () -> EntityType.Builder.<VenomousArrow>of(VenomousArrow::new, MobCategory.MISC).sized(0.5f, 0.5f).build("venomous_arrow"));
    public static final DeferredHolder<EntityType<?>, EntityType<SlingshotProjectile>> SLINGSHOT_PROJECTILE = ENTITY_TYPES.register("slingshot_projectile", () -> EntityType.Builder.<SlingshotProjectile>of(SlingshotProjectile::new, MobCategory.MISC).sized(0.5f, 0.5f).build("slingshot_projectile"));
}