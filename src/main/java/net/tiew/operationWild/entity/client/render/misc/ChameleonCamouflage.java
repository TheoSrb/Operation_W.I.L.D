package net.tiew.operationWild.entity.client.render.misc;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.util.TriState;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.ChameleonEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class ChameleonCamouflage {

    private static final RandomSource QUAD_RANDOM = RandomSource.create();
    private static final ResourceLocation SKY_SPRITE = ResourceLocation.withDefaultNamespace("block/white_concrete");
    private static final float SKY_SPRITE_BRIGHTNESS = 0.83f;

    private static final double RAY_LENGTH = 64.0;
    private static final double LOD_DISTANCE_SQR = 24.0 * 24.0;
    private static final double CULL_DISTANCE_SQR = 96.0 * 96.0;
    private static final double CONTEXT_TOLERANCE_SQR = 8.0 * 8.0;
    private static final float SKIN_TEXELS_PER_CELL = 2f;
    private static final float MASKED_TEXELS_PER_CELL = 1f;
    private static final int MAX_CELLS_PER_EDGE = 12;
    private static final int MAX_LAYERS = 3;
    private static final float LAYER_PUSH = 1.0e-4f;

    private static final float GRADIENT_WIDTH = 0.45f;
    private static final float MIN_ALPHA = 0.004f;

    private static final float WOBBLE_STILL = 0.0025f;
    private static final float WOBBLE_MOVING = 0.016f;
    private static final float WOBBLE_HURT = 0.06f;
    private static final float WOBBLE_SPEED_REFERENCE = 0.28f;
    private static final float WOBBLE_FREQUENCY = 9f;
    private static final float WOBBLE_RATE = 0.35f;

    private static final Map<ResourceLocation, NativeImage> MASKS = new HashMap<>();
    private static final Map<SurfaceKey, Surface> SURFACES = new HashMap<>();
    private static float surfacesFrame = Float.NaN;
    private static boolean inGui = false;

    private ChameleonCamouflage() {
    }

    private record Layer(TextureAtlasSprite sprite, int color) {
    }

    private record Surface(Layer[] layers, int light, @Nullable BlockPos origin, @Nullable Direction face) {
        boolean flat() {
            return origin == null || face == null;
        }
    }

    private record SurfaceKey(long pos, int face, boolean fluid) {
    }

    private record Hit(Surface surface, Vec3 location) {
    }

    public static void setInGui(boolean value) {
        inGui = value;
    }

    public static @Nullable ChameleonEntity mimicSource(Entity entity) {
        if (entity instanceof Player player) return ChameleonEntity.getMimicking(player);
        if (entity instanceof ChameleonEntity chameleon && chameleon.isMimicryActive()) return chameleon;
        return null;
    }

    public static boolean hidesExtras(Entity entity) {
        return !inGui && mimicSource(entity) != null;
    }

    public static boolean hidesBody(Entity entity) {
        if (inGui) return false;
        ChameleonEntity source = mimicSource(entity);
        return source != null && source.getMimicryCastTimer() <= 0
                && source.getMimicryTimer() > OWAttacksConstants.Chameleon.MIMICRY_FADE_TICKS;
    }

    public static float coverage(ChameleonEntity source, float partialTicks) {
        if (!source.isMimicryActive()) return 0f;
        float cast = OWAttacksConstants.Chameleon.MIMICRY_CAST_TICKS;
        float in = 1f - Mth.clamp((source.getMimicryCastTimer() - partialTicks) / cast, 0f, 1f);
        float out = Mth.clamp((source.getMimicryTimer() - partialTicks) / OWAttacksConstants.Chameleon.MIMICRY_FADE_TICKS, 0f, 1f);
        return Math.min(in, out);
    }

    @SubscribeEvent
    public static void onNameTag(RenderNameTagEvent event) {
        if (hidesExtras(event.getEntity())) event.setCanRender(TriState.FALSE);
    }

    public static void render(LivingEntity entity, ChameleonEntity source, @Nullable ResourceLocation maskTexture,
                              float partialTicks, MultiBufferSource bufferSource, Consumer<VertexConsumer> drawModel) {
        if (inGui || !(entity.level() instanceof ClientLevel level)) return;
        float coverage = coverage(source, partialTicks);
        if (coverage <= 0f) return;

        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vec3 entityPos = entity.getPosition(partialTicks);
        if (entityPos.distanceToSqr(cameraPos) > CULL_DISTANCE_SQR) return;

        float frame = level.getGameTime() + partialTicks;
        if (frame != surfacesFrame) {
            SURFACES.clear();
            surfacesFrame = frame;
        }

        Entity mover = entity.getVehicle() instanceof Player carrier ? carrier : entity;
        Vec3 moverPos = mover.getPosition(partialTicks);
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        org.joml.Vector3f up = camera.getUpVector();
        org.joml.Vector3f left = camera.getLeftVector();
        Vec3 sweepAxis = new Vec3(up.x - left.x, up.y - left.y, up.z - left.z).normalize();
        double halfWidth = mover.getBbWidth() * 0.5;
        double extent = Math.abs(sweepAxis.x) * halfWidth + Math.abs(sweepAxis.y) * mover.getBbHeight() * 0.5
                + Math.abs(sweepAxis.z) * halfWidth;
        Vec3 sweepCenter = moverPos.add(0, mover.getBbHeight() * 0.5, 0).subtract(cameraPos);
        float eased = coverage * coverage * (3f - 2f * coverage);

        Projection projection = new Projection(bufferSource.getBuffer(ChameleonRenderTypes.CAMOUFLAGE), level, cameraPos,
                entityPos.subtract(cameraPos), moverPos.subtract(cameraPos), sweepCenter, sweepAxis, Math.max(extent, 0.1),
                eased * (1f + GRADIENT_WIDTH), wobbleAmplitude(mover, partialTicks), entity.tickCount + partialTicks,
                partialTicks, mask(maskTexture));
        drawModel.accept(projection);
    }

    public static void renderProjectedLayer(LivingEntity entity, float partialTicks, MultiBufferSource bufferSource,
                                            Consumer<MultiBufferSource> drawLayer) {
        ChameleonEntity source = mimicSource(entity);
        if (source == null) return;
        render(entity, source, null, partialTicks, bufferSource,
                projection -> drawLayer.accept(type -> type.toString().contains("glint") ? Discard.INSTANCE : projection));
    }

    private static final class Discard implements VertexConsumer {

        private static final Discard INSTANCE = new Discard();

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            return this;
        }
    }

    private static float wobbleAmplitude(Entity mover, float partialTicks) {
        double dx = mover.getX() - mover.xo;
        double dy = mover.getY() - mover.yo;
        double dz = mover.getZ() - mover.zo;
        float speed = (float) Math.sqrt(dx * dx + dy * dy * 0.25 + dz * dz);
        float amplitude = WOBBLE_STILL + Mth.clamp(speed / WOBBLE_SPEED_REFERENCE, 0f, 1f) * (WOBBLE_MOVING - WOBBLE_STILL);
        if (mover instanceof LivingEntity living && living.hurtTime > 0) {
            amplitude += WOBBLE_HURT * Mth.clamp((living.hurtTime - partialTicks) / Math.max(1, living.hurtDuration), 0f, 1f);
        }
        return amplitude;
    }

    private static @Nullable NativeImage mask(@Nullable ResourceLocation texture) {
        if (texture == null) return null;
        return MASKS.computeIfAbsent(texture, ChameleonCamouflage::loadMask);
    }

    private static @Nullable NativeImage loadMask(ResourceLocation texture) {
        try (InputStream stream = Minecraft.getInstance().getResourceManager().open(texture)) {
            return NativeImage.read(stream);
        } catch (IOException e) {
            return null;
        }
    }

    private static int shaded(int rgb, float shade, int alpha) {
        int r = rgb == -1 ? 255 : FastColor.ARGB32.red(rgb);
        int g = rgb == -1 ? 255 : FastColor.ARGB32.green(rgb);
        int b = rgb == -1 ? 255 : FastColor.ARGB32.blue(rgb);
        return FastColor.ARGB32.color(alpha, (int) (r * shade), (int) (g * shade), (int) (b * shade));
    }

    private static int brightest(int lightA, int lightB) {
        return LightTexture.pack(Math.max(LightTexture.block(lightA), LightTexture.block(lightB)),
                Math.max(LightTexture.sky(lightA), LightTexture.sky(lightB)));
    }

    private static final class Projection implements VertexConsumer {

        private final VertexConsumer out;
        private final ClientLevel level;
        private final Vec3 cameraPos;
        private final Vec3 entityRelative;
        private final Vec3 anchor;
        private final Vec3 sweepCenter;
        private final Vec3 sweepAxis;
        private final double sweepExtent;
        private final float front;
        private final float amplitude;
        private final float time;
        private final float partialTicks;
        private final @Nullable NativeImage mask;
        private final boolean fluidsVisible;

        private final float[] px = new float[4], py = new float[4], pz = new float[4];
        private final float[] tu = new float[4], tv = new float[4];
        private int count = 0;

        private float pendingX, pendingY, pendingZ, pendingU, pendingV;

        private Projection(VertexConsumer out, ClientLevel level, Vec3 cameraPos, Vec3 entityRelative, Vec3 anchor,
                           Vec3 sweepCenter, Vec3 sweepAxis, double sweepExtent, float front, float amplitude, float time,
                           float partialTicks, @Nullable NativeImage mask) {
            this.out = out;
            this.level = level;
            this.cameraPos = cameraPos;
            this.entityRelative = entityRelative;
            this.anchor = anchor;
            this.sweepCenter = sweepCenter;
            this.sweepAxis = sweepAxis;
            this.sweepExtent = sweepExtent;
            this.front = front;
            this.amplitude = amplitude;
            this.time = time;
            this.partialTicks = partialTicks;
            this.mask = mask;
            this.fluidsVisible = level.getFluidState(BlockPos.containing(cameraPos)).isEmpty();
        }

        @Override
        public void addVertex(float x, float y, float z, int color, float u, float v, int overlay, int light,
                              float normalX, float normalY, float normalZ) {
            px[count] = x;
            py[count] = y;
            pz[count] = z;
            tu[count] = u;
            tv[count] = v;
            if (++count == 4) {
                count = 0;
                projectQuad();
            }
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
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            addVertex(pendingX, pendingY, pendingZ, -1, pendingU, pendingV, 0, 0, x, y, z);
            return this;
        }

        private void projectQuad() {
            if (new Vec3(px[0], py[0], pz[0]).distanceToSqr(entityRelative) > CONTEXT_TOLERANCE_SQR) return;

            float textureWidth = mask != null ? mask.getWidth() : 64f;
            float textureHeight = mask != null ? mask.getHeight() : 64f;
            float edgeU = texelLength(0, 1, textureWidth, textureHeight);
            float edgeV = texelLength(1, 2, textureWidth, textureHeight);
            if (edgeU < 0.5f || edgeV < 0.5f) return;

            float perCell = mask != null ? MASKED_TEXELS_PER_CELL : SKIN_TEXELS_PER_CELL;
            int columns = Mth.clamp(Math.round(edgeU / perCell), 1, MAX_CELLS_PER_EDGE);
            int rows = Mth.clamp(Math.round(edgeV / perCell), 1, MAX_CELLS_PER_EDGE);
            if (point(0.5f, 0.5f).lengthSqr() > LOD_DISTANCE_SQR) {
                columns = (columns + 1) / 2;
                rows = (rows + 1) / 2;
            }

            int stride = columns + 1;
            Vec3[] corners = new Vec3[stride * (rows + 1)];
            Vec3[] directions = new Vec3[corners.length];
            float[] alphas = new float[corners.length];
            for (int b = 0; b <= rows; b++) {
                for (int a = 0; a <= columns; a++) {
                    Vec3 corner = point(a / (float) columns, b / (float) rows);
                    corners[b * stride + a] = corner;
                    directions[b * stride + a] = rayDirection(corner);
                    alphas[b * stride + a] = sweepAlpha(corner);
                }
            }

            for (int b = 0; b < rows; b++) {
                for (int a = 0; a < columns; a++) {
                    int[] cell = {b * stride + a, b * stride + a + 1, (b + 1) * stride + a + 1, (b + 1) * stride + a};
                    float strongest = Math.max(Math.max(alphas[cell[0]], alphas[cell[1]]), Math.max(alphas[cell[2]], alphas[cell[3]]));
                    if (strongest < MIN_ALPHA) continue;

                    float s = (a + 0.5f) / columns;
                    float t = (b + 0.5f) / rows;
                    if (mask != null && !opaque(s, t)) continue;

                    Vec3 center = point(s, t);
                    emit(trace(center, rayDirection(center)), corners, directions, alphas, cell);
                }
            }
        }

        private float texelLength(int from, int to, float width, float height) {
            float du = (tu[to] - tu[from]) * width;
            float dv = (tv[to] - tv[from]) * height;
            return (float) Math.sqrt(du * du + dv * dv);
        }

        private Vec3 point(float s, float t) {
            float w0 = (1 - s) * (1 - t), w1 = s * (1 - t), w2 = s * t, w3 = (1 - s) * t;
            return new Vec3(px[0] * w0 + px[1] * w1 + px[2] * w2 + px[3] * w3,
                    py[0] * w0 + py[1] * w1 + py[2] * w2 + py[3] * w3,
                    pz[0] * w0 + pz[1] * w1 + pz[2] * w2 + pz[3] * w3);
        }

        private boolean opaque(float s, float t) {
            float w0 = (1 - s) * (1 - t), w1 = s * (1 - t), w2 = s * t, w3 = (1 - s) * t;
            float u = tu[0] * w0 + tu[1] * w1 + tu[2] * w2 + tu[3] * w3;
            float v = tv[0] * w0 + tv[1] * w1 + tv[2] * w2 + tv[3] * w3;
            int x = Mth.clamp(Mth.floor(u * mask.getWidth()), 0, mask.getWidth() - 1);
            int y = Mth.clamp(Mth.floor(v * mask.getHeight()), 0, mask.getHeight() - 1);
            return (mask.getPixelRGBA(x, y) >>> 24) > 8;
        }

        private float sweepAlpha(Vec3 point) {
            double along = point.subtract(sweepCenter).dot(sweepAxis) / (2.0 * sweepExtent);
            float depth = (float) Mth.clamp(0.5 - along, 0.0, 1.0);
            float t = Mth.clamp((front - depth) / GRADIENT_WIDTH, 0f, 1f);
            return t * t * (3f - 2f * t);
        }

        private Vec3 rayDirection(Vec3 point) {
            double length = point.length();
            if (length < 1.0e-6) return new Vec3(0, 0, 1);
            Vec3 direction = point.scale(1.0 / length);
            Vec3 local = point.subtract(anchor);
            float phase = time * WOBBLE_RATE;
            double ox = Math.sin(local.y * WOBBLE_FREQUENCY + phase);
            double oy = Math.sin((local.x + local.z) * WOBBLE_FREQUENCY * 0.9 + phase * 1.17);
            double oz = Math.sin(local.z * WOBBLE_FREQUENCY - local.x * 3.0 - phase * 0.86);
            return direction.add(ox * amplitude, oy * amplitude, oz * amplitude).normalize();
        }

        private Hit trace(Vec3 center, Vec3 direction) {
            Vec3 from = cameraPos.add(center);
            Vec3 to = from.add(direction.scale(RAY_LENGTH));
            Hit hit = BlockGetter.traverseBlocks(from, to, this, (self, pos) -> self.test(pos, from, to), self -> null);
            return hit != null ? hit : sky(direction);
        }

        private @Nullable Hit test(BlockPos pos, Vec3 from, Vec3 to) {
            BlockState state = level.getBlockState(pos);
            BlockHitResult blockHit = null;
            if (!state.isAir() && state.getRenderShape() != RenderShape.INVISIBLE) {
                VoxelShape shape = state.getShape(level, pos);
                if (!shape.isEmpty()) blockHit = shape.clip(from, to, pos);
            }
            FluidState fluid = state.getFluidState();
            BlockHitResult fluidHit = fluidsVisible && !fluid.isEmpty() ? fluid.getShape(level, pos).clip(from, to, pos) : null;
            if (blockHit == null && fluidHit == null) return null;

            boolean useFluid = fluidHit != null && (blockHit == null
                    || from.distanceToSqr(fluidHit.getLocation()) < from.distanceToSqr(blockHit.getLocation()));
            BlockHitResult chosen = useFluid ? fluidHit : blockHit;
            Surface surface = surface(pos, state, useFluid ? fluid : null, chosen.getDirection());
            return surface == null ? null : new Hit(surface, chosen.getLocation());
        }

        private @Nullable Surface surface(BlockPos pos, BlockState state, @Nullable FluidState fluid, Direction face) {
            SurfaceKey key = new SurfaceKey(pos.asLong(), face.ordinal(), fluid != null);
            if (SURFACES.containsKey(key)) return SURFACES.get(key);
            Surface surface = fluid != null ? fluidSurface(pos, fluid, face) : blockSurface(pos, state, face);
            SURFACES.put(key, surface);
            return surface;
        }

        private Surface fluidSurface(BlockPos pos, FluidState fluid, Direction face) {
            IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid);
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                    .apply(extensions.getStillTexture(fluid, level, pos));
            int tint = extensions.getTintColor(fluid, level, pos) & 0xFFFFFF;
            int color = shaded(tint, level.getShade(face, true), 255);
            return new Surface(new Layer[]{new Layer(sprite, color)}, LevelRenderer.getLightColor(level, pos), pos, face);
        }

        private @Nullable Surface blockSurface(BlockPos pos, BlockState state, Direction face) {
            Minecraft minecraft = Minecraft.getInstance();
            BakedModel model = minecraft.getBlockRenderer().getBlockModel(state);

            QUAD_RANDOM.setSeed(state.getSeed(pos));
            List<BakedQuad> quads = model.getQuads(state, face, QUAD_RANDOM, ModelData.EMPTY, null);
            if (quads.isEmpty()) {
                QUAD_RANDOM.setSeed(state.getSeed(pos));
                List<BakedQuad> loose = model.getQuads(state, null, QUAD_RANDOM, ModelData.EMPTY, null);
                quads = loose.stream().filter(quad -> quad.getDirection() == face).toList();
                if (quads.isEmpty() && !loose.isEmpty()) quads = List.of(loose.get(0));
            }

            float shade = level.getShade(face, true);
            int light = brightest(LevelRenderer.getLightColor(level, pos), LevelRenderer.getLightColor(level, pos.relative(face)));
            if (quads.isEmpty()) {
                TextureAtlasSprite particle = model.getParticleIcon(ModelData.EMPTY);
                if (particle == null) return null;
                return new Surface(new Layer[]{new Layer(particle, shaded(-1, shade, 255))}, light, pos, face);
            }

            Layer[] layers = new Layer[Math.min(quads.size(), MAX_LAYERS)];
            for (int i = 0; i < layers.length; i++) {
                BakedQuad quad = quads.get(i);
                int tint = quad.isTinted() ? minecraft.getBlockColors().getColor(state, level, pos, quad.getTintIndex()) : -1;
                layers[i] = new Layer(quad.getSprite(), shaded(tint, quad.isShade() ? shade : 1f, 255));
            }
            return new Surface(layers, light, pos, face);
        }

        private Hit sky(Vec3 direction) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(SKY_SPRITE);
            Vec3 sky = level.getSkyColor(cameraPos, partialTicks);
            float[] fog = RenderSystem.getShaderFogColor();
            float height = (float) Mth.clamp(direction.y * 2.5 + 0.15, 0.0, 1.0);
            int r = Mth.clamp((int) (Mth.lerp(height, fog[0], (float) sky.x) / SKY_SPRITE_BRIGHTNESS * 255f), 0, 255);
            int g = Mth.clamp((int) (Mth.lerp(height, fog[1], (float) sky.y) / SKY_SPRITE_BRIGHTNESS * 255f), 0, 255);
            int b = Mth.clamp((int) (Mth.lerp(height, fog[2], (float) sky.z) / SKY_SPRITE_BRIGHTNESS * 255f), 0, 255);
            Layer layer = new Layer(sprite, FastColor.ARGB32.color(255, r, g, b));
            return new Hit(new Surface(new Layer[]{layer}, LightTexture.FULL_BRIGHT, null, null), cameraPos.add(direction.scale(RAY_LENGTH)));
        }

        private void emit(Hit hit, Vec3[] corners, Vec3[] directions, float[] alphas, int[] cell) {
            Surface surface = hit.surface();
            float[][] uvs = new float[4][];
            for (int k = 0; k < 4; k++) {
                uvs[k] = surface.flat() ? new float[]{0.5f, 0.5f} : projectUv(surface, hit.location(), directions[cell[k]]);
            }

            int light = surface.light();
            for (int layerIndex = 0; layerIndex < surface.layers().length; layerIndex++) {
                Layer layer = surface.layers()[layerIndex];
                float push = 1f - LAYER_PUSH * layerIndex;
                TextureAtlasSprite sprite = layer.sprite();
                for (int k = 0; k < 4; k++) {
                    Vec3 corner = corners[cell[k]];
                    int color = FastColor.ARGB32.color(Mth.clamp((int) (alphas[cell[k]] * 255f), 0, 255), layer.color());
                    out.addVertex((float) corner.x * push, (float) corner.y * push, (float) corner.z * push, color,
                            sprite.getU(uvs[k][0]), sprite.getV(uvs[k][1]), OverlayTexture.NO_OVERLAY, light, 0f, 1f, 0f);
                }
            }
        }

        private float[] projectUv(Surface surface, Vec3 location, Vec3 direction) {
            Direction face = surface.face();
            Direction.Axis axis = face.getAxis();
            double plane = location.get(axis) - cameraPos.get(axis);
            double along = direction.get(axis);
            Vec3 point = Math.abs(along) < 1.0e-6 || plane / along <= 0 ? location : cameraPos.add(direction.scale(plane / along));

            BlockPos origin = surface.origin();
            float x = (float) (point.x - origin.getX());
            float y = (float) (point.y - origin.getY());
            float z = (float) (point.z - origin.getZ());
            float u;
            float v;
            switch (face) {
                case UP -> { u = x; v = z; }
                case DOWN -> { u = x; v = 1f - z; }
                case NORTH -> { u = 1f - x; v = 1f - y; }
                case SOUTH -> { u = x; v = 1f - y; }
                case WEST -> { u = z; v = 1f - y; }
                default -> { u = 1f - z; v = 1f - y; }
            }
            return new float[]{Mth.clamp(u, 0f, 1f), Mth.clamp(v, 0f, 1f)};
        }
    }
}
