package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.tiew.operationWild.OperationWild;
import org.slf4j.Logger;

import java.io.InputStream;

public final class PlantEmpressDissolve {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int STEPS = 48;
    private static final float EDGE = 0.045f;
    private static final float CHAR = 0.12f;

    private static ResourceLocation[] steps;
    private static boolean failed;

    private PlantEmpressDissolve() {
    }

    public static ResourceLocation texture(ResourceLocation base, float progress) {
        if (progress <= 0f || failed) return base;
        if (steps == null) build(base);
        if (steps == null) return base;
        return steps[Mth.clamp((int) (progress * STEPS), 0, STEPS - 1)];
    }

    private static void build(ResourceLocation base) {
        Minecraft minecraft = Minecraft.getInstance();
        try (InputStream in = minecraft.getResourceManager().open(base); NativeImage source = NativeImage.read(in)) {
            int width = source.getWidth();
            int height = source.getHeight();
            float[] noise = noise(width, height);
            ResourceLocation[] built = new ResourceLocation[STEPS];
            for (int step = 0; step < STEPS; step++) {
                float threshold = (step + 1) / (float) STEPS * (1f + EDGE);
                NativeImage image = new NativeImage(width, height, true);
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int color = source.getPixelRGBA(x, y);
                        int alpha = color >>> 24;
                        float n = noise[y * width + x];
                        if (alpha == 0 || n < threshold) {
                            image.setPixelRGBA(x, y, 0);
                        } else if (n < threshold + EDGE) {
                            float glow = 1f - (n - threshold) / EDGE;
                            image.setPixelRGBA(x, y, blend(color, 0xFF5AE6C8, 0.55f + 0.45f * glow));
                        } else if (n < threshold + CHAR) {
                            float dark = 1f - (n - threshold - EDGE) / (CHAR - EDGE);
                            image.setPixelRGBA(x, y, blend(color, 0xFF0A1A12, 0.45f * dark));
                        } else {
                            image.setPixelRGBA(x, y, color);
                        }
                    }
                }
                ResourceLocation location = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "dynamic/plant_empress_dissolve_" + step);
                minecraft.getTextureManager().register(location, new DynamicTexture(image));
                built[step] = location;
            }
            steps = built;
        } catch (Exception e) {
            failed = true;
            LOGGER.error("Unable to build the Plant Empress dissolve textures", e);
        }
    }

    private static float[] noise(int width, int height) {
        RandomSource random = RandomSource.create(7351L);
        float[] out = new float[width * height];
        int[] cells = {32, 16, 8, 4};
        float[] weights = {0.45f, 0.28f, 0.17f, 0.10f};
        for (int octave = 0; octave < cells.length; octave++) {
            int cell = cells[octave];
            int gw = width / cell + 2;
            int gh = height / cell + 2;
            float[] grid = new float[gw * gh];
            for (int i = 0; i < grid.length; i++) grid[i] = random.nextFloat();
            for (int y = 0; y < height; y++) {
                float fy = y / (float) cell;
                int iy = (int) fy;
                float ty = smooth(fy - iy);
                for (int x = 0; x < width; x++) {
                    float fx = x / (float) cell;
                    int ix = (int) fx;
                    float tx = smooth(fx - ix);
                    float a = grid[iy * gw + ix];
                    float b = grid[iy * gw + ix + 1];
                    float c = grid[(iy + 1) * gw + ix];
                    float d = grid[(iy + 1) * gw + ix + 1];
                    out[y * width + x] += Mth.lerp(ty, Mth.lerp(tx, a, b), Mth.lerp(tx, c, d)) * weights[octave];
                }
            }
        }
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        for (int i = 0; i < out.length; i++) {
            out[i] += (random.nextFloat() - 0.5f) * 0.06f;
            min = Math.min(min, out[i]);
            max = Math.max(max, out[i]);
        }
        for (int i = 0; i < out.length; i++) out[i] = (out[i] - min) / Math.max(1e-4f, max - min);
        return out;
    }

    private static float smooth(float t) {
        return t * t * (3f - 2f * t);
    }

    private static int blend(int from, int to, float amount) {
        amount = Mth.clamp(amount, 0f, 1f);
        int a = from >>> 24;
        int r = (int) Mth.lerp(amount, from & 0xFF, to & 0xFF);
        int g = (int) Mth.lerp(amount, (from >> 8) & 0xFF, (to >> 8) & 0xFF);
        int b = (int) Mth.lerp(amount, (from >> 16) & 0xFF, (to >> 16) & 0xFF);
        return (a << 24) | (b << 16) | (g << 8) | r;
    }
}
