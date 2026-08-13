package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.tiew.operationWild.entity.misc.HealSnackEntity;

public class HealSnackRenderer extends EntityRenderer<HealSnackEntity> {

    private static final float SCALE = 0.78f;
    private static final float SPIN_PER_TICK = 17f;
    private static final float TUMBLE_PER_TICK = 11f;

    private final ItemRenderer itemRenderer;

    public HealSnackRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(HealSnackEntity snack, float yaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        float age = snack.tickCount + partialTick;

        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(age * SPIN_PER_TICK));
        pose.mulPose(Axis.XP.rotationDegrees(age * TUMBLE_PER_TICK));
        pose.scale(SCALE, SCALE, SCALE);

        this.itemRenderer.renderStatic(snack.getSnack(), ItemDisplayContext.GROUND, light,
                OverlayTexture.NO_OVERLAY, pose, buffers, snack.level(), snack.getId());
        pose.popPose();

        super.render(snack, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(HealSnackEntity snack) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
