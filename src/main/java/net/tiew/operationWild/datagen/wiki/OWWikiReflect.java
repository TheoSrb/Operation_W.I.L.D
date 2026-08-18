package net.tiew.operationWild.datagen.wiki;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.OWEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class OWWikiReflect {

    private OWWikiReflect() {}

    private static final List<String> PROBE_FAILURES = new ArrayList<>();

    public static List<String> probeFailures() {
        return List.copyOf(PROBE_FAILURES);
    }

    public static OWEntity probe(EntityType<?> type, Class<? extends OWEntity> implementation) {
        try {
            Object instance = allocate(implementation);
            assign(Entity.class, "type", instance, type);
            assign(Entity.class, "random", instance, RandomSource.create());
            assign(Entity.class, "dimensions", instance, type.getDimensions());
            assign(Entity.class, "eyeHeight", instance, type.getDimensions().eyeHeight());
            assign(Entity.class, "position", instance, Vec3.ZERO);
            assign(Entity.class, "blockPosition", instance, BlockPos.ZERO);
            assign(Entity.class, "chunkPosition", instance, ChunkPos.ZERO);

            SynchedEntityData.Builder builder = new SynchedEntityData.Builder((SyncedDataHolder) instance);
            defineBaseData(builder);
            Method define = Entity.class.getDeclaredMethod("defineSynchedData", SynchedEntityData.Builder.class);
            define.setAccessible(true);
            define.invoke(instance, builder);
            assign(Entity.class, "entityData", instance, builder.build());

            return (OWEntity) instance;
        } catch (Throwable failure) {
            PROBE_FAILURES.add(implementation.getSimpleName() + ": " + failure);
            return null;
        }
    }

    private static Object allocate(Class<?> target) throws Exception {
        try {
            Class<?> factoryClass = Class.forName("sun.reflect.ReflectionFactory");
            Object factory = factoryClass.getMethod("getReflectionFactory").invoke(null);
            Method builder = factoryClass.getMethod("newConstructorForSerialization", Class.class, Constructor.class);
            Constructor<?> constructor = (Constructor<?>) builder.invoke(factory, target, Object.class.getDeclaredConstructor());
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Throwable ignored) {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field holder = unsafeClass.getDeclaredField("theUnsafe");
            holder.setAccessible(true);
            Object unsafe = holder.get(null);
            return unsafeClass.getMethod("allocateInstance", Class.class).invoke(unsafe, target);
        }
    }

    private static void assign(Class<?> owner, String fieldName, Object instance, Object value) {
        try {
            Field field = owner.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (Throwable ignored) {
        }
    }

    private static void defineBaseData(SynchedEntityData.Builder builder) {
        defineBaseEntry(builder, "DATA_SHARED_FLAGS_ID", (byte) 0);
        defineBaseEntry(builder, "DATA_AIR_SUPPLY_ID", 300);
        defineBaseEntry(builder, "DATA_CUSTOM_NAME_VISIBLE", false);
        defineBaseEntry(builder, "DATA_CUSTOM_NAME", java.util.Optional.empty());
        defineBaseEntry(builder, "DATA_SILENT", false);
        defineBaseEntry(builder, "DATA_NO_GRAVITY", false);
        defineBaseEntry(builder, "DATA_POSE", Pose.STANDING);
        defineBaseEntry(builder, "DATA_TICKS_FROZEN", 0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void defineBaseEntry(SynchedEntityData.Builder builder, String fieldName, Object value) {
        try {
            Field field = Entity.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            builder.define((EntityDataAccessor) field.get(null), value);
        } catch (Throwable ignored) {
        }
    }

    public static Optional<Object> call(Object target, String methodName) {
        if (target == null) return Optional.empty();
        try {
            Method method = find(target.getClass(), methodName);
            if (method == null) return Optional.empty();
            method.setAccessible(true);
            return Optional.ofNullable(method.invoke(target));
        } catch (Throwable ignored) {
            return Optional.empty();
        }
    }

    public static boolean hasMethod(Class<?> owner, String methodName) {
        return find(owner, methodName) != null;
    }

    private static Method find(Class<?> from, String methodName) {
        for (Class<?> current = from; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == 0) return method;
            }
        }
        return null;
    }

    public static void putCall(JsonObject target, String property, Object probe, String methodName) {
        call(probe, methodName).ifPresent(value -> put(target, property, value));
    }

    public static void put(JsonObject target, String property, Object value) {
        if (value == null) return;
        if (value instanceof Number number) {
            target.addProperty(property, number);
        } else if (value instanceof Boolean flag) {
            target.addProperty(property, flag);
        } else if (value instanceof Enum<?> constant) {
            target.addProperty(property, constant.name());
        } else if (value instanceof Character character) {
            target.addProperty(property, String.valueOf(character));
        } else {
            target.addProperty(property, String.valueOf(value));
        }
    }

    public static JsonObject constants(Class<?> owner) {
        JsonObject constants = new JsonObject();
        if (owner == null) return constants;
        for (Field field : owner.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (!Modifier.isStatic(modifiers) || !Modifier.isFinal(modifiers) || field.isSynthetic()) continue;
            if (!Modifier.isPublic(modifiers)) continue;
            Class<?> valueType = field.getType();
            if (!valueType.isPrimitive() && valueType != String.class && !valueType.isEnum()) continue;
            try {
                field.setAccessible(true);
                put(constants, field.getName(), field.get(null));
            } catch (Throwable ignored) {
            }
        }
        return constants;
    }

    public static JsonArray enumConstants(Class<?> owner) {
        if (owner == null || !owner.isEnum()) return null;
        JsonArray values = new JsonArray();
        for (Object constant : owner.getEnumConstants()) {
            Enum<?> value = (Enum<?>) constant;
            JsonObject entry = new JsonObject();
            entry.addProperty("name", value.name());
            entry.addProperty("ordinal", value.ordinal());
            call(value, "getId").ifPresent(id -> put(entry, "id", id));
            call(value, "isCosmetic").ifPresent(cosmetic -> put(entry, "cosmetic", cosmetic));
            entry.addProperty("deprecated", isDeprecated(owner, value.name()));
            values.add(entry);
        }
        return values;
    }

    private static boolean isDeprecated(Class<?> owner, String constantName) {
        try {
            return owner.getDeclaredField(constantName).isAnnotationPresent(Deprecated.class);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static Class<?> classOrNull(String name) {
        try {
            return Class.forName(name, false, OWWikiReflect.class.getClassLoader());
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static Class<?> nestedOrNull(Class<?> owner, String simpleName) {
        if (owner == null) return null;
        for (Class<?> nested : owner.getDeclaredClasses()) {
            if (nested.getSimpleName().equals(simpleName)) return nested;
        }
        return null;
    }
}
