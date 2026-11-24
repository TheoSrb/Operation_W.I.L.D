package net.tiew.operationWild.entity.client.render.misc;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.tiew.operationWild.entity.animals.terrestrial.ChameleonEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class ChameleonCamouflage {

    private static final RandomSource QUAD_RANDOM = RandomSource.create();
    private static final double WALL_TOUCH_DISTANCE = 0.35;
    private static final int FLOOR_SCAN_DEPTH = 3;
    private static final long FORGET_AFTER_TICKS = 100L;

    private static final Map<Integer, Fade> FADES = new HashMap<>();

    private ChameleonCamouflage() {
    }

    public record Sample(TextureAtlasSprite sprite, int tint, BlockState state) {
    }

    private static final class Fade {
        private Sample current;
        private Sample previous;
        private float changedAt;
        private long lastSeen;
    }

    public static float envelope(ChameleonEntity chameleon) {
        if (!chameleon.isMimicryActive()) return 0f;
        float fadeTicks = OWAttacksConstants.Chameleon.MIMICRY_FADE_TICKS;
        float in = 1f - Mth.clamp((chameleon.getMimicryCastTimer() - (OWAttacksConstants.Chameleon.MIMICRY_CAST_TICKS - fadeTicks)) / fadeTicks, 0f, 1f);
        float out = Mth.clamp(chameleon.getMimicryTimer() / fadeTicks, 0f, 1f);
        return OWAttacksConstants.Chameleon.MIMICRY_OPACITY * Math.min(in, out);
    }

    public static void render(Entity anchor, float partialTicks, float opacity, int packedLight, int packedOverlay,
                              MultiBufferSource bufferSource, Consumer<RemapTarget> drawModel) {
        if (opacity <= 0.01f) return;

        Fade fade = fadeFor(anchor, partialTicks);
        if (fade == null || fade.current == null) return;

        float age = anchor.tickCount + partialTicks;
        float progress = fade.previous == null ? 1f
                : Mth.clamp((age - fade.changedAt) / OWAttacksConstants.Chameleon.MIMICRY_FADE_TICKS, 0f, 1f);

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        if (fade.previous != null && progress < 1f) {
            drawModel.accept(new RemapTarget(new SpriteRemapConsumer(buffer, fade.previous.sprite()),
                    color(fade.previous.tint(), opacity * (1f - progress)), packedLight, packedOverlay));
        }
        drawModel.accept(new RemapTarget(new SpriteRemapConsumer(buffer, fade.current.sprite()),
                color(fade.current.tint(), opacity * progress), packedLight, packedOverlay));
    }

    public record RemapTarget(VertexConsumer consumer, int color, int light, int overlay) {
    }

    private static int color(int tint, float alpha) {
        int a = Mth.clamp((int) (alpha * 255f), 0, 255);
        return FastColor.ARGB32.color(a, FastColor.ARGB32.red(tint), FastColor.ARGB32.green(tint), FastColor.ARGB32.blue(tint));
    }

    private static @Nullable Fade fadeFor(Entity anchor, float partialTicks) {
        Level level = anchor.level();
        long now = level.getGameTime();
        purge(now);

        Sample sample = sampleAround(level, anchor);
        Fade fade = FADES.computeIfAbsent(anchor.getId(), id -> new Fade());
        fade.lastSeen = now;
        if (sample == null) return fade;

        if (fade.current == null) {
            fade.current = sample;
        } else if (fade.current.state() != sample.state() || fade.current.sprite() != sample.sprite()) {
            fade.previous = fade.current;
            fade.current = sample;
            fade.changedAt = anchor.tickCount + partialTicks;
        }
        return fade;
    }

    private static void purge(long now) {
        if (FADES.size() < 32) return;
        Iterator<Map.Entry<Integer, Fade>> iterator = FADES.entrySet().iterator();
        while (iterator.hasNext()) {
            if (now - iterator.next().getValue().lastSeen > FORGET_AFTER_TICKS) iterator.remove();
        }
    }

    public static @Nullable Sample sampleAround(Level level, Entity anchor) {
        double bodyY = anchor.getY() + anchor.getBbHeight() * 0.5;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos pos = BlockPos.containing(anchor.getX() + direction.getStepX() * (anchor.getBbWidth() * 0.5 + WALL_TOUCH_DISTANCE),
                    bodyY, anchor.getZ() + direction.getStepZ() * (anchor.getBbWidth() * 0.5 + WALL_TOUCH_DISTANCE));
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.getCollisionShape(level, pos).isEmpty()) continue;
            return sample(level, pos, state, direction.getOpposite());
        }

        BlockPos below = BlockPos.containing(anchor.getX(), anchor.getY() - 0.2, anchor.getZ());
        for (int depth = 0; depth < FLOOR_SCAN_DEPTH; depth++) {
            BlockState state = level.getBlockState(below);
            if (!state.isAir()) return sample(level, below, state, Direction.UP);
            below = below.below();
        }
        return null;
    }

    private static Sample sample(Level level, BlockPos pos, BlockState state, Direction face) {
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = minecraft.getBlockRenderer().getBlockModel(state);

        QUAD_RANDOM.setSeed(state.getSeed(pos));
        List<BakedQuad> quads = model.getQuads(state, face, QUAD_RANDOM, ModelData.EMPTY, null);
        if (quads.isEmpty()) {
            QUAD_RANDOM.setSeed(state.getSeed(pos));
            quads = model.getQuads(state, null, QUAD_RANDOM, ModelData.EMPTY, null);
        }

        if (quads.isEmpty()) return new Sample(model.getParticleIcon(ModelData.EMPTY), -1, state);

        BakedQuad quad = quads.get(0);
        int tint = quad.isTinted() ? minecraft.getBlockColors().getColor(state, level, pos, quad.getTintIndex()) : -1;
        return new Sample(quad.getSprite(), tint | 0xFF000000, state);
    }

    public static @Nullable ChameleonEntity mimicSource(Entity entity) {
        if (entity instanceof Player player) return ChameleonEntity.getMimicking(player);
        if (entity instanceof ChameleonEntity chameleon && chameleon.isMimicryActive()) return chameleon;
        return null;
    }

    private static final class SpriteRemapConsumer implements VertexConsumer {

        private final VertexConsumer out;
        private final TextureAtlasSprite sprite;
        private final float[] positions = new float[12];
        private final float[] uvs = new float[8];
        private final float[] normals = new float[12];
        private final int[] colors = new int[4];
        private final int[] overlays = new int[4];
        private final int[] lights = new int[4];
        private int count = 0;

        private float pendingX, pendingY, pendingZ, pendingU, pendingV;
        private int pendingColor = -1, pendingOverlay, pendingLight;

        private SpriteRemapConsumer(VertexConsumer out, TextureAtlasSprite sprite) {
            this.out = out;
            this.sprite = sprite;
        }

        @Override
        public void addVertex(float x, float y, float z, int color, float u, float v, int overlay, int light,
                              float normalX, float normalY, float normalZ) {
            positions[count * 3] = x;
            positions[count * 3 + 1] = y;
            positions[count * 3 + 2] = z;
            uvs[count * 2] = u;
            uvs[count * 2 + 1] = v;
            normals[count * 3] = normalX;
            normals[count * 3 + 1] = normalY;
            normals[count * 3 + 2] = normalZ;
            colors[count] = color;
            overlays[count] = overlay;
            lights[count] = light;
            if (++count == 4) flush();
        }

        private void flush() {
            float minU = Float.MAX_VALUE, maxU = -Float.MAX_VALUE, minV = Float.MAX_VALUE, maxV = -Float.MAX_VALUE;
            for (int i = 0; i < 4; i++) {
                minU = Math.min(minU, uvs[i * 2]);
                maxU = Math.max(maxU, uvs[i * 2]);
                minV = Math.min(minV, uvs[i * 2 + 1]);
                maxV = Math.max(maxV, uvs[i * 2 + 1]);
            }
            float spanU = maxU - minU;
            float spanV = maxV - minV;

            for (int i = 0; i < 4; i++) {
                float fu = spanU < 1.0e-6f ? (i == 1 || i == 2 ? 1f : 0f) : (uvs[i * 2] - minU) / spanU;
                float fv = spanV < 1.0e-6f ? (i >= 2 ? 1f : 0f) : (uvs[i * 2 + 1] - minV) / spanV;
                out.addVertex(positions[i * 3], positions[i * 3 + 1], positions[i * 3 + 2], colors[i],
                        sprite.getU(fu), sprite.getV(fv), overlays[i], lights[i],
                        normals[i * 3], normals[i * 3 + 1], normals[i * 3 + 2]);
            }
            count = 0;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            pendingX = x;
            pendingY = y;
            pendingZ = z;
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            pendingColor = FastColor.ARGB32.color(alpha, red, green, blue);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            pendingU = u;
            pendingV = v;
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            pendingOverlay = (v << 16) | (u & 0xFFFF);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            pendingLight = (v << 16) | (u & 0xFFFF);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            addVertex(pendingX, pendingY, pendingZ, pendingColor, pendingU, pendingV, pendingOverlay, pendingLight, x, y, z);
            return this;
        }
    }
}
