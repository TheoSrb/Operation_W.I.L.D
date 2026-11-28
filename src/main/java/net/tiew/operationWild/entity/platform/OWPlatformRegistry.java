package net.tiew.operationWild.entity.platform;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

public final class OWPlatformRegistry {

    private static final Set<OWPlatform> CLIENT = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Set<OWPlatform> SERVER = Collections.newSetFromMap(new WeakHashMap<>());

    private OWPlatformRegistry() {
    }

    private static Set<OWPlatform> side(boolean client) {
        return client ? CLIENT : SERVER;
    }

    public static void track(OWPlatform platform) {
        side(platform.carrier().level().isClientSide()).add(platform);
    }

    public static void untrack(OWPlatform platform) {
        side(platform.carrier().level().isClientSide()).remove(platform);
    }

    @javax.annotation.Nullable
    public static OWPlatform findServer(int carrierId) {
        for (OWPlatform platform : SERVER) {
            if (platform != null && platform.isActive() && platform.carrier().getId() == carrierId) return platform;
        }
        return null;
    }

    public static boolean isEmpty(Level level) {
        return side(level.isClientSide()).isEmpty();
    }

    public static List<OWPlatform> near(Level level, AABB area) {
        Set<OWPlatform> set = side(level.isClientSide());
        if (set.isEmpty()) return List.of();
        List<OWPlatform> result = null;
        Iterator<OWPlatform> iterator = set.iterator();
        while (iterator.hasNext()) {
            OWPlatform platform = iterator.next();
            if (platform == null) continue;
            if (!platform.isActive()) {
                iterator.remove();
                continue;
            }
            if (platform.carrier().level() != level) continue;
            if (!platform.worldBounds().intersects(area)) continue;
            if (result == null) result = new ArrayList<>(2);
            result.add(platform);
        }
        return result == null ? List.of() : result;
    }
}
