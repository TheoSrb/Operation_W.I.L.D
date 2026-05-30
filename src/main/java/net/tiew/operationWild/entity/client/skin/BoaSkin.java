package net.tiew.operationWild.entity.client.skin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.entity.animals.terrestrial.BoaEntity;
import net.tiew.operationWild.entity.client.model.BoaModel;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

public class BoaSkin {

    public enum Mode {
        BASE,
        OVERLAY,
        REPLACEMENT
    }

    private final Mode mode;
    private final ResourceLocation texture;

    @Nullable
    private final ResourceLocation overlayTexture;

    @Nullable
    private final ModelLayerLocation modelLayer;

    @Nullable
    private final Supplier<LayerDefinition> layerDefinitionSupplier;

    protected BoaSkin(Mode mode, ResourceLocation texture, @Nullable ResourceLocation overlayTexture,
                      @Nullable ModelLayerLocation modelLayer, @Nullable Supplier<LayerDefinition> layerDefinitionSupplier) {
        this.mode = mode;
        this.texture = texture;
        this.overlayTexture = overlayTexture;
        this.modelLayer = modelLayer;
        this.layerDefinitionSupplier = layerDefinitionSupplier;
    }

    public static BoaSkin base(ResourceLocation texture) {
        return new BoaSkin(Mode.BASE, texture, null, null, null);
    }

    public static BoaSkin overlay(ResourceLocation baseTexture, ResourceLocation overlayTexture,
                                   ModelLayerLocation overlayModelLayer, Supplier<LayerDefinition> layerDef) {
        return new BoaSkin(Mode.OVERLAY, baseTexture, overlayTexture, overlayModelLayer, layerDef);
    }

    public static BoaSkin replacement(ResourceLocation texture, ModelLayerLocation modelLayer,
                                       Supplier<LayerDefinition> layerDef) {
        return new BoaSkin(Mode.REPLACEMENT, texture, null, modelLayer, layerDef);
    }

    public Mode getMode() { return mode; }
    public ResourceLocation getTexture() { return texture; }
    public Optional<ResourceLocation> getOverlayTexture() { return Optional.ofNullable(overlayTexture); }
    public Optional<ModelLayerLocation> getModelLayer() { return Optional.ofNullable(modelLayer); }
    public Optional<Supplier<LayerDefinition>> getLayerDefinitionSupplier() { return Optional.ofNullable(layerDefinitionSupplier); }

    public void renderExtraLayers(PoseStack poseStack, MultiBufferSource bufferSource,
                                   int packedLight, int packedOverlay,
                                   BoaEntity boa, BoaModel<BoaEntity> model) {
        // No extra layers by default
    }

    protected static void renderGlow(PoseStack poseStack, MultiBufferSource bufferSource,
                                      ResourceLocation texture, BoaModel<BoaEntity> model) {
        VertexConsumer vc = bufferSource.getBuffer(RenderType.eyes(texture));
        model.renderToBuffer(poseStack, vc, 15728640, OverlayTexture.NO_OVERLAY);
    }

    protected static void renderCutout(PoseStack poseStack, MultiBufferSource bufferSource,
                                        ResourceLocation texture, int packedLight, int packedOverlay,
                                        BoaModel<BoaEntity> model) {
        VertexConsumer vc = bufferSource.getBuffer(RenderType.entityCutout(texture));
        model.renderToBuffer(poseStack, vc, packedLight, packedOverlay);
    }
}
