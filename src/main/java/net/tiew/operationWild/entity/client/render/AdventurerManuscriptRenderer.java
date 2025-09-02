package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.model.AdventurerManuscriptModel;
import net.tiew.operationWild.entity.misc.AdventurerManuscript;

public class AdventurerManuscriptRenderer extends MobRenderer<AdventurerManuscript, AdventurerManuscriptModel<AdventurerManuscript>> {
    public AdventurerManuscriptRenderer(EntityRendererProvider.Context context) {
        super(context, new AdventurerManuscriptModel<>(context.bakeLayer(AdventurerManuscriptModel.LAYER_LOCATION)), 0.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(AdventurerManuscript book) {
        return ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/gui/mob_types.png");
    }

    @Override
    public RenderType getRenderType(AdventurerManuscript jellyfish, boolean bodyVisible, boolean translucent, boolean glowing) {
        ResourceLocation texture = this.getTextureLocation(jellyfish);
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public void render(AdventurerManuscript book, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(book, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }
}
