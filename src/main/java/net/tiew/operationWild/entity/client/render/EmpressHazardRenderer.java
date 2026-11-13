package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.tiew.operationWild.entity.bosses.plantempress.EmpressHazardVisual;

public class EmpressHazardRenderer<T extends Entity & EmpressHazardVisual> extends EntityRenderer<T> {

    private final BlockRenderDispatcher blocks;

    public EmpressHazardRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blocks = context.getBlockRenderDispatcher();
        this.shadowRadius = 0f;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float width = entity.visualWidth(partialTicks);
        float height = entity.visualHeight(partialTicks);
        if (width <= 0.01f || height <= 0.01f) return;
        int copies = Math.max(1, entity.visualCopies());
        float ring = entity.visualRing();
        float spin = entity.visualSpin(partialTicks);
        boolean projectile = entity instanceof net.tiew.operationWild.entity.bosses.plantempress.EmpressSeedProjectile;
        for (int i = 0; i < copies; i++) {
            poseStack.pushPose();
            if (copies > 1) {
                poseStack.mulPose(Axis.YP.rotationDegrees(360f * i / copies));
                poseStack.translate(ring, 0, 0);
            }
            poseStack.mulPose(Axis.YP.rotationDegrees(spin + i * 33f));
            if (projectile) {
                poseStack.mulPose(Axis.XP.rotationDegrees(spin * 0.7f));
            }
            poseStack.scale(width, height, width);
            poseStack.translate(-0.5, projectile ? -0.5 : 0, -0.5);
            blocks.renderSingleBlock(entity.visualBlock(), poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
