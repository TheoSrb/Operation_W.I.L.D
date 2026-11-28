package net.tiew.operationWild.entity.cocoon;

import java.util.HashMap;
import java.util.Map;

public final class CocoonClientState {

    private static final Map<Integer, Cocoon> COCOONS = new HashMap<>();

    private CocoonClientState() {
    }

    public static final class Cocoon {
        public int layers;
        public int required;
        public boolean locked;
        public float shown;
        public float shownO;
        public long builtAtMs;

        public float target() {
            return locked ? 1f : required <= 0 ? 0f : Math.min(1f, layers / (float) required);
        }
    }

    public static void update(int entityId, int layers, int required, int lockTicks) {
        if (layers <= 0 && lockTicks <= 0) {
            COCOONS.remove(entityId);
            return;
        }
        Cocoon cocoon = COCOONS.computeIfAbsent(entityId, id -> new Cocoon());
        if (layers > cocoon.layers) cocoon.builtAtMs = System.currentTimeMillis();
        cocoon.layers = layers;
        cocoon.required = required;
        cocoon.locked = lockTicks > 0;
    }

    public static Cocoon get(int entityId) {
        return COCOONS.get(entityId);
    }

    public static void tick() {
        for (Cocoon cocoon : COCOONS.values()) {
            cocoon.shownO = cocoon.shown;
            cocoon.shown += (cocoon.target() - cocoon.shown) * 0.25f;
        }
    }

    public static void clear() {
        COCOONS.clear();
    }
}
