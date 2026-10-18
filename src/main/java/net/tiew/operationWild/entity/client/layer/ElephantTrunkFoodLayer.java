package net.tiew.operationWild.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.animals.terrestrial.ElephantEntity;
import net.tiew.operationWild.entity.client.model.ElephantModel;
import org.joml.Vector3f;

public class ElephantTrunkFoodLayer extends RenderLayer<ElephantEntity, ElephantModel<ElephantEntity>> {

    private static final float FOOD_SCALE = 0.42f;
    private static final float GRIP_DEPTH = 0.06f;
    private static final int CRUMB_INTERVAL = 3;
    private static final double CRUMB_SPREAD = 0.12;

    private final BlockRenderDispatcher blockRenderer;

    public ElephantTrunkFoodLayer(RenderLayerParent<ElephantEntity, ElephantModel<ElephantEntity>> parent,
                                  BlockRenderDispatcher blockRenderer) {
        super(parent);
        this.blockRenderer = blockRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ElephantEntity elephant,
                       float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        BlockState food = elephant.getHeldFood();
        if (food == null) return;

        ElephantModel<ElephantEntity> model = this.getParentModel();
        float sway = Mth.sin(ageInTicks * 0.35f) * 9f;

        poseStack.pushPose();
        model.translateToTrunkTip(poseStack);
        poseStack.translate(0.0f, ElephantModel.TRUNK_TIP_LENGTH / 16f - GRIP_DEPTH, 0.0f);

        if (elephant.level().isClientSide() && elephant.lastFoodCrumbTick != elephant.tickCount
                && elephant.tickCount % CRUMB_INTERVAL == 0) {
            elephant.lastFoodCrumbTick = elephant.tickCount;
            Vector3f tip = new Vector3f(0f, 0f, 0f);
            poseStack.last().pose().transformPosition(tip);
            Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            spawnCrumbs(elephant.level(), food, new Vec3(camera.x + tip.x(), camera.y + tip.y(), camera.z + tip.z()));
        }

        poseStack.mulPose(Axis.ZP.rotationDegrees(sway));
        poseStack.mulPose(Axis.XP.rotationDegrees(25f));
        poseStack.scale(-FOOD_SCALE, -FOOD_SCALE, FOOD_SCALE);
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        this.blockRenderer.renderSingleBlock(food, poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static void spawnCrumbs(Level level, BlockState food, Vec3 at) {
        var random = level.getRandom();
        BlockParticleOption crumb = new BlockParticleOption(ParticleTypes.BLOCK, food);
        int count = 1 + random.nextInt(2);
        for (int i = 0; i < count; i++) {
            level.addParticle(crumb,
                    at.x + Mth.nextDouble(random, -CRUMB_SPREAD, CRUMB_SPREAD),
                    at.y + Mth.nextDouble(random, -CRUMB_SPREAD, CRUMB_SPREAD),
                    at.z + Mth.nextDouble(random, -CRUMB_SPREAD, CRUMB_SPREAD),
                    Mth.nextDouble(random, -0.02, 0.02), -0.04, Mth.nextDouble(random, -0.02, 0.02));
        }
    }
}
