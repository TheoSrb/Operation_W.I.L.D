package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.entity.animals.terrestrial.GorillaEntity;
import net.tiew.operationWild.entity.client.layer.GorillaLayer;
import net.tiew.operationWild.entity.client.layer.skins.GorillaSkinRenderLayer;
import net.tiew.operationWild.entity.client.model.GorillaModel;
import net.tiew.operationWild.entity.client.skin.GorillaSkin;
import net.tiew.operationWild.entity.client.skin.SkinRegistry;

public class GorillaRenderer extends OWEntityRenderer<GorillaEntity, GorillaModel<GorillaEntity>> {

    public GorillaRenderer(EntityRendererProvider.Context context) {
        super(context, new GorillaModel<>(context.bakeLayer(GorillaModel.LAYER_LOCATION)), 1.1f);
        this.addLayer(new GorillaLayer(this));
        this.addLayer(new GorillaSkinRenderLayer(this, context));
    }

    @Override
    public ResourceLocation getTextureLocation(GorillaEntity gorilla) {
        GorillaSkin skin = SkinRegistry.GorillaSkins.get(gorilla.getVariant());
        if (skin.getMode() == GorillaSkin.Mode.OVERLAY) {
            return SkinRegistry.GorillaSkins.get(gorilla.getInitialVariant()).getTexture();
        }
        return skin.getTexture();
    }

    @Override
    public void render(GorillaEntity gorilla, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        this.getModel().setBufferSource(bufferSource);
        super.render(gorilla, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    @Override
    public double distanceToShowRealInfos() {
        return 4;
    }

    @Override
    public double infosUpOffset() {
        return 0.15;
    }
}
