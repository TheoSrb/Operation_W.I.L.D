package net.tiew.operationWild.client.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.tiew.operationWild.entity.platform.OWPlatformSpace;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.tiew.operationWild.entity.platform.OWPlatform;
import net.tiew.operationWild.entity.platform.OWPlatformProfile;
import net.tiew.operationWild.entity.platform.OWPlatformTransform;

public final class OWPlatformRenderer {

    private static final float TILE = 0.5f;
    private static final float EDGE_V = 0.5f + 3f / 32f;

    private OWPlatformRenderer() {
    }

    public static void renderGrid(OWPlatform platform, float partialTick, PoseStack poseStack, MultiBufferSource buffers) {
        OWPlatformProfile profile = platform.profile();
        OWPlatformTransform transform = platform.interpolated(partialTick);
        if (profile.deckTexture() != null) {
            int deckLight = LevelRenderer.getLightColor(platform.carrier().level(), BlockPos.containing(transform.x, transform.y + 0.5, transform.z));
            renderDeck(poseStack, buffers.getBuffer(RenderType.entityCutout(profile.deckTexture())), profile, deckLight);
        }
        platform.setLightingTransform(transform);
        try {
            renderBlocks(platform, poseStack, buffers);
            renderBlockEntities(platform, partialTick, poseStack, buffers);
        } finally {
            platform.setLightingTransform(null);
        }
    }

    private static void renderBlocks(OWPlatform platform, PoseStack poseStack, MultiBufferSource buffers) {
        if (platform.blockCount() == 0) return;
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        RandomSource random = RandomSource.create();
        BlockPos crackPos = OWPlatformClient.crackCell(platform);
        int crackStage = OWPlatformClient.crackStage(platform);
        platform.forEachBlock((pos, state) -> {
            if (state.getRenderShape() != RenderShape.MODEL) return;
            BlockPos cell = pos.immutable();
            poseStack.pushPose();
            poseStack.translate(cell.getX(), cell.getY(), cell.getZ());
            BakedModel model = dispatcher.getBlockModel(state);
            long seed = state.getSeed(cell);
            random.setSeed(seed);
            for (RenderType renderType : model.getRenderTypes(state, random, ModelData.EMPTY)) {
                VertexConsumer consumer = buffers.getBuffer(RenderTypeHelper.getMovingBlockRenderType(renderType));
                dispatcher.getModelRenderer().tesselateBlock(platform.level(), model, state, cell, poseStack, consumer, true,
                        random, seed, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);
            }
            if (crackStage >= 0 && cell.equals(crackPos)) {
                renderCrack(dispatcher, platform, state, cell, poseStack, buffers, crackStage);
            }
            poseStack.popPose();
        });
    }

    @SuppressWarnings("unchecked")
    private static void renderBlockEntities(OWPlatform platform, float partialTick, PoseStack poseStack, MultiBufferSource buffers) {
        if (!platform.hasBlockEntities()) return;
        BlockEntityRenderDispatcher dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        for (BlockEntity blockEntity : platform.blockEntities()) {
            BlockEntityRenderer<BlockEntity> renderer = dispatcher.getRenderer(blockEntity);
            if (renderer == null) continue;
            BlockPos cell = OWPlatformSpace.toGrid(blockEntity.getBlockPos());
            poseStack.pushPose();
            poseStack.translate(cell.getX(), cell.getY(), cell.getZ());
            int light = LevelRenderer.getLightColor(platform.level(), cell);
            try {
                renderer.render(blockEntity, partialTick, poseStack, buffers, light, OverlayTexture.NO_OVERLAY);
            } catch (RuntimeException ignored) {
            }
            poseStack.popPose();
        }
    }

    private static void renderCrack(BlockRenderDispatcher dispatcher, OWPlatform platform, BlockState state, BlockPos cell,
                                    PoseStack poseStack, MultiBufferSource buffers, int stage) {
        VertexConsumer crumbling = new SheetedDecalTextureGenerator(
                buffers.getBuffer(ModelBakery.DESTROY_TYPES.get(stage)), poseStack.last(), 1.0F);
        dispatcher.renderBreakingTexture(state, cell, platform.level(), poseStack, crumbling, ModelData.EMPTY);
    }

    private static void renderDeck(PoseStack poseStack, VertexConsumer consumer, OWPlatformProfile profile, int light) {
        PoseStack.Pose pose = poseStack.last();
        float bottom = (float) -profile.deckThickness();
        int width = profile.width();
        int length = profile.length();
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                if (!profile.onDeck(x, z)) continue;
                quad(consumer, pose, light, 0, 1, 0,
                        x, 0, z, x, 0, z + 1, x + 1, 0, z + 1, x + 1, 0, z,
                        0f, 0f, TILE, TILE);
                quad(consumer, pose, light, 0, -1, 0,
                        x, bottom, z, x + 1, bottom, z, x + 1, bottom, z + 1, x, bottom, z + 1,
                        TILE, 0f, 1f, TILE);
            }
        }
        for (int x = 0; x < width; x++) {
            quad(consumer, pose, light, 0, 0, -1,
                    x + 1, 0, 0, x + 1, bottom, 0, x, bottom, 0, x, 0, 0,
                    0f, TILE, TILE, EDGE_V);
            quad(consumer, pose, light, 0, 0, 1,
                    x, 0, length, x, bottom, length, x + 1, bottom, length, x + 1, 0, length,
                    0f, TILE, TILE, EDGE_V);
        }
        for (int z = 0; z < length; z++) {
            quad(consumer, pose, light, -1, 0, 0,
                    0, 0, z, 0, bottom, z, 0, bottom, z + 1, 0, 0, z + 1,
                    0f, TILE, TILE, EDGE_V);
            quad(consumer, pose, light, 1, 0, 0,
                    width, 0, z + 1, width, bottom, z + 1, width, bottom, z, width, 0, z,
                    0f, TILE, TILE, EDGE_V);
        }
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, int light, float nx, float ny, float nz,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4,
                             float u0, float v0, float u1, float v1) {
        vertex(consumer, pose, light, nx, ny, nz, x1, y1, z1, u0, v0);
        vertex(consumer, pose, light, nx, ny, nz, x2, y2, z2, u0, v1);
        vertex(consumer, pose, light, nx, ny, nz, x3, y3, z3, u1, v1);
        vertex(consumer, pose, light, nx, ny, nz, x4, y4, z4, u1, v0);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int light, float nx, float ny, float nz,
                               float x, float y, float z, float u, float v) {
        consumer.addVertex(pose, x, y, z)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }
}
