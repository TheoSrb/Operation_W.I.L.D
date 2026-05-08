package net.tiew.operationWild.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.worldgen.biome.OWBiomes;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class OWFogEvents {

    private static final float FOG_RED   = 18f / 255f;
    private static final float FOG_GREEN = 32f / 255f;
    private static final float FOG_BLUE  = 62f / 255f;
    private static final float FOG_NEAR  = 0f;
    private static final float FOG_FAR   = 25f;

    private static final float BLEND_DURATION = 2.0f; // secondes pour transition complète

    private static float blendFactor = 0f;
    private static long  lastNanos   = 0L;

    private static void updateBlend(boolean targetActive) {
        long now = System.nanoTime();
        float dt = lastNanos == 0L ? 0f : (now - lastNanos) / 1_000_000_000f;
        lastNanos = now;
        float step = dt / BLEND_DURATION;
        blendFactor = Math.clamp(blendFactor + (targetActive ? step : -step), 0f, 1f);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        boolean underwater = event.getCamera().getFluidInCamera() == FogType.WATER;
        Entity entity = event.getCamera().getEntity();
        boolean inBiome = entity != null && entity.level().getBiome(entity.blockPosition()).is(OWBiomes.MINE_FIELD_BIOME);

        updateBlend(underwater && inBiome);

        if (blendFactor <= 0f || !underwater) return;

        event.setRed(lerp(event.getRed(), FOG_RED, blendFactor));
        event.setGreen(lerp(event.getGreen(), FOG_GREEN, blendFactor));
        event.setBlue(lerp(event.getBlue(), FOG_BLUE, blendFactor));
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (blendFactor <= 0f) return;
        if (event.getCamera().getFluidInCamera() != FogType.WATER) return;

        event.setNearPlaneDistance(lerp(event.getNearPlaneDistance(), FOG_NEAR, blendFactor));
        event.setFarPlaneDistance(lerp(event.getFarPlaneDistance(), FOG_FAR, blendFactor));
        event.setCanceled(true);
    }
}
