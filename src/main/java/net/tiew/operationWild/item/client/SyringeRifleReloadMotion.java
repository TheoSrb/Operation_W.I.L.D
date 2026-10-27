package net.tiew.operationWild.item.client;

import net.minecraft.util.Mth;
import net.tiew.operationWild.item.custom.SyringeRifleItem;

public final class SyringeRifleReloadMotion {
    private static final float INSERT_AT = (float) SyringeRifleItem.LOAD_INSERT_TICK / SyringeRifleItem.RELOAD_DURATION;
    private static final float SNAP_AT = (float) SyringeRifleItem.LOAD_SNAP_TICK / SyringeRifleItem.RELOAD_DURATION;

    private SyringeRifleReloadMotion() {}

    public static float envelope(float progress) {
        return smooth(0.0F, 0.22F, progress) * (1.0F - smooth(0.84F, 1.0F, progress));
    }

    public static float insert(float progress) {
        return pulse(INSERT_AT, 0.07F, progress);
    }

    public static float snap(float progress) {
        return pulse(SNAP_AT, 0.05F, progress);
    }

    private static float smooth(float from, float to, float x) {
        float t = Mth.clamp((x - from) / (to - from), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private static float pulse(float center, float halfWidth, float x) {
        float d = (x - center) / halfWidth;
        if (d * d >= 1.0F) return 0.0F;
        float falloff = 1.0F - d * d;
        return falloff * falloff;
    }
}
