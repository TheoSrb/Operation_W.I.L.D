package net.tiew.operationWild.component;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tiew.operationWild.OperationWild;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class OWDataComponentTypes {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, OperationWild.MOD_ID);
    public static void register(IEventBus bus) { DATA_COMPONENT_TYPES.register(bus);}
    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builderUnaryOperator) {
        return DATA_COMPONENT_TYPES.register(name, () -> builderUnaryOperator.apply(DataComponentType.builder()).build());
    }

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Component>> TAMED_ENTITY_TYPE = register("tamed_entity_type", builder -> builder.persistent(ComponentSerialization.FLAT_CODEC).networkSynchronized(ComponentSerialization.STREAM_CODEC).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Component>> TAMED_ENTITY_OWNER = register("tamed_entity_owner", builder -> builder.persistent(ComponentSerialization.FLAT_CODEC).networkSynchronized(ComponentSerialization.STREAM_CODEC).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> TAMED_ENTITY_GENDER = register("tamed_entity_gender", builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> TAMED_ENTITY_CAN_DROP = register("tamed_entity_can_drop", builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Float>> TAMED_ENTITY_MAX_HEALTH = register("tamed_entity_max_health", builder -> builder.persistent(ExtraCodecs.POSITIVE_FLOAT).networkSynchronized(ByteBufCodecs.FLOAT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Float>> TAMED_ENTITY_DAMAGES = register("tamed_entity_damages", builder -> builder.persistent(ExtraCodecs.POSITIVE_FLOAT).networkSynchronized(ByteBufCodecs.FLOAT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Float>> TAMED_ENTITY_SPEED = register("tamed_entity_speed", builder -> builder.persistent(ExtraCodecs.POSITIVE_FLOAT).networkSynchronized(ByteBufCodecs.FLOAT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Float>> TAMED_ENTITY_SCALE = register("tamed_entity_scale", builder -> builder.persistent(ExtraCodecs.POSITIVE_FLOAT).networkSynchronized(ByteBufCodecs.FLOAT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TAMED_ENTITY_LEVEL = register("tamed_entity_level", builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TAMED_ENTITY_VARIANT = register("tamed_entity_variant", builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT));



    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Float>> BABY_EGG_MAX_HEALTH = register("baby_egg_max_health", builder -> builder.persistent(ExtraCodecs.POSITIVE_FLOAT).networkSynchronized(ByteBufCodecs.FLOAT));


    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<Item>>> SADDLE_WOOLS = register("saddle_wools", builder -> builder.persistent(Codec.list(BuiltInRegistries.ITEM.byNameCodec())).networkSynchronized(ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.registry(Registries.ITEM))));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> PLATINUM_RANDOM_ATTRIBUTES = register("platinum_random_attributes", builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT));
}