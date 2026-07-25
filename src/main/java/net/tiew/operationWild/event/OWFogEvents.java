package net.tiew.operationWild.event;

import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.worldgen.biome.OWBiomes;
import net.tiew.operationWild.worldgen.dimension.OWDimensions;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class OWFogEvents {

    private static final float FOG_RED   = 18f / 255f;
    private static final float FOG_GREEN = 32f / 255f;
    private static final float FOG_BLUE  = 62f / 255f;
    private static final float FOG_NEAR  = 0f;
    private static final float FOG_FAR   = 25f;

    private static final float ARENA_RED   = 52f / 255f;
    private static final float ARENA_GREEN = 92f / 255f;
    private static final float ARENA_BLUE  = 48f / 255f;
    private static final float ARENA_NEAR  = 24f;
    private static final float ARENA_FAR   = 168f;

    private static final float BLEND_DURATION = 2.0f; // secondes pour transition complète

    private static float blendFactor = 0f;
    private static float arenaBlend  = 0f;
    private static long  lastNanos   = 0L;

    private static float deltaSeconds() {
        long now = System.nanoTime();
        float dt = lastNanos == 0L ? 0f : (now - lastNanos) / 1_000_000_000f;
        lastNanos = now;
        return dt;
    }

    private static float advance(float current, boolean targetActive, float dt) {
        float step = dt / BLEND_DURATION;
        return Math.clamp(current + (targetActive ? step : -step), 0f, 1f);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static boolean inArena(Entity entity) {
        return entity != null && entity.level().dimension().equals(OWDimensions.ARENA);
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        boolean underwater = event.getCamera().getFluidInCamera() == FogType.WATER;
        Entity entity = event.getCamera().getEntity();
        boolean inBiome = entity != null && entity.level().getBiome(entity.blockPosition()).is(OWBiomes.MINE_FIELD_BIOME);

        float dt = deltaSeconds();
        blendFactor = advance(blendFactor, underwater && inBiome, dt);
        arenaBlend = advance(arenaBlend, !underwater && inArena(entity), dt);

        if (arenaBlend > 0f && !underwater) {
            event.setRed(lerp(event.getRed(), ARENA_RED, arenaBlend));
            event.setGreen(lerp(event.getGreen(), ARENA_GREEN, arenaBlend));
            event.setBlue(lerp(event.getBlue(), ARENA_BLUE, arenaBlend));
        }

        if (blendFactor <= 0f || !underwater) return;

        event.setRed(lerp(event.getRed(), FOG_RED, blendFactor));
        event.setGreen(lerp(event.getGreen(), FOG_GREEN, blendFactor));
        event.setBlue(lerp(event.getBlue(), FOG_BLUE, blendFactor));
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        FogType fluid = event.getCamera().getFluidInCamera();

        if (blendFactor > 0f && fluid == FogType.WATER) {
            event.setNearPlaneDistance(lerp(event.getNearPlaneDistance(), FOG_NEAR, blendFactor));
            event.setFarPlaneDistance(lerp(event.getFarPlaneDistance(), FOG_FAR, blendFactor));
            event.setCanceled(true);
            return;
        }

        if (arenaBlend <= 0f || fluid != FogType.NONE) return;
        if (event.getMode() != FogRenderer.FogMode.FOG_TERRAIN) return;

        event.setNearPlaneDistance(lerp(event.getNearPlaneDistance(), ARENA_NEAR, arenaBlend));
        event.setFarPlaneDistance(lerp(event.getFarPlaneDistance(), ARENA_FAR, arenaBlend));
        event.setCanceled(true);
    }
}
