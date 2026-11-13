package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.bosses.PlantEmpressEntity;
import net.tiew.operationWild.entity.bosses.plantempress.EmpressRig;
import net.tiew.operationWild.entity.client.model.PlantEmpressModel;

public class PlantEmpressRenderer extends MobRenderer<PlantEmpressEntity, PlantEmpressModel<PlantEmpressEntity>> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/plant_empress/plant_empress.png");
    private static final BlockState BUSH = Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 3);
    private static final BlockState ROOTS = Blocks.MANGROVE_ROOTS.defaultBlockState();

    public PlantEmpressRenderer(EntityRendererProvider.Context context) {
        super(context, new PlantEmpressModel<>(context.bakeLayer(PlantEmpressModel.LAYER_LOCATION)), 2.2f);
    }

    @Override
    public ResourceLocation getTextureLocation(PlantEmpressEntity entity) {
        float progress = entity.dissolveProgress(Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true));
        return progress > 0f ? PlantEmpressDissolve.texture(TEXTURE, progress) : TEXTURE;
    }

    @Override
    public boolean shouldRender(PlantEmpressEntity entity, Frustum frustum, double x, double y, double z) {
        return entity.getWallStart() >= 0 || super.shouldRender(entity, frustum, x, y, z);
    }

    @Override
    public void render(PlantEmpressEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(EmpressRig.RENDER_SCALE, EmpressRig.RENDER_SCALE, EmpressRig.RENDER_SCALE);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
        float wall = entity.wallRadius(partialTicks);
        if (wall > 0f) renderWall(entity, wall, partialTicks, poseStack, buffer);
    }

    private void renderWall(PlantEmpressEntity entity, float radius, float partialTicks, PoseStack poseStack, MultiBufferSource buffer) {
        BlockRenderDispatcher blocks = Minecraft.getInstance().getBlockRenderer();
        float grow = Mth.clamp((entity.level().getGameTime() + partialTicks - entity.getWallStart()) / 40f, 0f, 1f);
        int count = Mth.ceil(Mth.TWO_PI * radius / 1.15f);
        double ex = Mth.lerp(partialTicks, entity.xo, entity.getX());
        double ey = Mth.lerp(partialTicks, entity.yo, entity.getY());
        double ez = Mth.lerp(partialTicks, entity.zo, entity.getZ());
        net.minecraft.world.phys.Vec3 home = entity.home();
        double ox = home.x - ex;
        double oy = home.y - ey;
        double oz = home.z - ez;
        ex = home.x;
        ey = home.y;
        ez = home.z;
        for (int i = 0; i < count; i++) {
            float angle = Mth.TWO_PI * i / count;
            float wobble = Mth.sin(i * 12.9898f) * 0.35f;
            double x = Mth.cos(angle) * (radius + wobble);
            double z = Mth.sin(angle) * (radius + wobble);
            BlockPos pos = BlockPos.containing(ex + x, ey + 0.5, ez + z);
            int light = LevelRenderer.getLightColor(entity.level(), pos);
            float height = (1.1f + Math.abs(Mth.sin(i * 4.1f)) * 0.9f) * grow;
            if (height <= 0.02f) continue;
            poseStack.pushPose();
            poseStack.translate(x + ox, oy, z + oz);
            poseStack.mulPose(Axis.YP.rotationDegrees(i * 47f));

            poseStack.scale(1.25f, height, 1.25f);
            poseStack.translate(-0.5, 0, -0.5);
            blocks.renderSingleBlock(i % 3 == 0 ? ROOTS : BUSH, poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }

    @Override
    protected float getFlipDegrees(PlantEmpressEntity entity) {
        return 0f;
    }

    @Override
    protected boolean shouldShowName(PlantEmpressEntity entity) {
        return false;
    }
}
