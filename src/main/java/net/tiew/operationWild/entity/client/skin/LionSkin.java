package net.tiew.operationWild.entity.client.skin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.entity.animals.terrestrial.LionEntity;
import net.tiew.operationWild.entity.client.model.LionModel;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

public class LionSkin {

    public enum Mode {
        BASE,
        OVERLAY,
        REPLACEMENT
    }

    private final Mode mode;
    private final ResourceLocation maleTexture;
    private final ResourceLocation femaleTexture;

    @Nullable
    private final ResourceLocation overlayTexture;

    @Nullable
    private final ModelLayerLocation modelLayer;

    @Nullable
    private final Supplier<LayerDefinition> layerDefinitionSupplier;

    protected LionSkin(Mode mode, ResourceLocation maleTexture, ResourceLocation femaleTexture,
                       @Nullable ResourceLocation overlayTexture, @Nullable ModelLayerLocation modelLayer,
                       @Nullable Supplier<LayerDefinition> layerDefinitionSupplier) {
        this.mode = mode;
        this.maleTexture = maleTexture;
        this.femaleTexture = femaleTexture;
        this.overlayTexture = overlayTexture;
        this.modelLayer = modelLayer;
        this.layerDefinitionSupplier = layerDefinitionSupplier;
    }

    public static LionSkin base(ResourceLocation maleTexture, ResourceLocation femaleTexture) {
        return new LionSkin(Mode.BASE, maleTexture, femaleTexture, null, null, null);
    }

    public static LionSkin overlay(ResourceLocation maleTexture, ResourceLocation femaleTexture, ResourceLocation overlayTexture,
                                   ModelLayerLocation overlayModelLayer, Supplier<LayerDefinition> layerDef) {
        return new LionSkin(Mode.OVERLAY, maleTexture, femaleTexture, overlayTexture, overlayModelLayer, layerDef);
    }

    public static LionSkin replacement(ResourceLocation maleTexture, ResourceLocation femaleTexture,
                                       ModelLayerLocation modelLayer, Supplier<LayerDefinition> layerDef) {
        return new LionSkin(Mode.REPLACEMENT, maleTexture, femaleTexture, null, modelLayer, layerDef);
    }

    public Mode getMode() { return mode; }
    public ResourceLocation getTexture(LionEntity lion) { return lion.isMale() ? maleTexture : femaleTexture; }
    public ResourceLocation getMaleTexture() { return maleTexture; }
    public Optional<ResourceLocation> getOverlayTexture() { return Optional.ofNullable(overlayTexture); }
    public Optional<ModelLayerLocation> getModelLayer() { return Optional.ofNullable(modelLayer); }
    public Optional<Supplier<LayerDefinition>> getLayerDefinitionSupplier() { return Optional.ofNullable(layerDefinitionSupplier); }

    public void renderExtraLayers(PoseStack poseStack, MultiBufferSource bufferSource,
                                  int packedLight, int packedOverlay,
                                  LionEntity lion, LionModel<LionEntity> model) {
    }

    protected static void renderGlow(PoseStack poseStack, MultiBufferSource bufferSource,
                                     ResourceLocation texture, LionModel<LionEntity> model) {
        VertexConsumer vc = bufferSource.getBuffer(RenderType.eyes(texture));
        model.renderToBuffer(poseStack, vc, 15728640, OverlayTexture.NO_OVERLAY, -1);
    }

    protected static void renderCutout(PoseStack poseStack, MultiBufferSource bufferSource,
                                       ResourceLocation texture, int packedLight, int packedOverlay,
                                       LionModel<LionEntity> model) {
        VertexConsumer vc = bufferSource.getBuffer(RenderType.entityCutout(texture));
        model.renderToBuffer(poseStack, vc, packedLight, packedOverlay, -1);
    }
}
