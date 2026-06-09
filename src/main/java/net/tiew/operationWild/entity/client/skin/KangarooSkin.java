package net.tiew.operationWild.entity.client.skin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.entity.animals.terrestrial.KangarooEntity;
import net.tiew.operationWild.entity.client.model.KangarooModel;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

public class KangarooSkin {

    public enum Mode {
        /** Swaps only the texture. The base model is used. (DEFAULT, ORANGE, BROWN) */
        BASE,
        /** Base model + base texture, then a second model on top with another texture (boxing gloves). */
        OVERLAY,
        /** Replaces the base model AND texture entirely. */
        REPLACEMENT
    }

    private final Mode mode;

    /** For BASE/REPLACEMENT: main texture. For OVERLAY: base kangaroo texture shown on base model. */
    private final ResourceLocation texture;

    /** For OVERLAY only: texture applied to the overlay model. */
    @Nullable
    private final ResourceLocation overlayTexture;

    /** For OVERLAY and REPLACEMENT: the alternative model layer to bake. */
    @Nullable
    private final ModelLayerLocation modelLayer;

    /** Supplier for the LayerDefinition to register on the event bus. Required when modelLayer is non-null. */
    @Nullable
    private final Supplier<LayerDefinition> layerDefinitionSupplier;

    protected KangarooSkin(Mode mode, ResourceLocation texture, @Nullable ResourceLocation overlayTexture,
                           @Nullable ModelLayerLocation modelLayer, @Nullable Supplier<LayerDefinition> layerDefinitionSupplier) {
        this.mode = mode;
        this.texture = texture;
        this.overlayTexture = overlayTexture;
        this.modelLayer = modelLayer;
        this.layerDefinitionSupplier = layerDefinitionSupplier;
    }

    /** Simple texture-only skin. No model change. */
    public static KangarooSkin base(ResourceLocation texture) {
        return new KangarooSkin(Mode.BASE, texture, null, null, null);
    }

    /** Overlay skin: renders the base kangaroo normally, then renders an additional model layer on top. */
    public static KangarooSkin overlay(ResourceLocation baseTexture, ResourceLocation overlayTexture,
                                       ModelLayerLocation overlayModelLayer, Supplier<LayerDefinition> layerDef) {
        return new KangarooSkin(Mode.OVERLAY, baseTexture, overlayTexture, overlayModelLayer, layerDef);
    }

    /** Replacement skin: replaces model and texture entirely. */
    public static KangarooSkin replacement(ResourceLocation texture, ModelLayerLocation modelLayer,
                                           Supplier<LayerDefinition> layerDef) {
        return new KangarooSkin(Mode.REPLACEMENT, texture, null, modelLayer, layerDef);
    }

    public Mode getMode() { return mode; }
    public ResourceLocation getTexture() { return texture; }
    public Optional<ResourceLocation> getOverlayTexture() { return Optional.ofNullable(overlayTexture); }
    public Optional<ModelLayerLocation> getModelLayer() { return Optional.ofNullable(modelLayer); }
    public Optional<Supplier<LayerDefinition>> getLayerDefinitionSupplier() { return Optional.ofNullable(layerDefinitionSupplier); }

    /**
     * Called during the skin render layer pass.
     * Override this to add glowing overlays, animated elements, etc.
     */
    public void renderExtraLayers(PoseStack poseStack, MultiBufferSource bufferSource,
                                  int packedLight, int packedOverlay,
                                  KangarooEntity kangaroo, KangarooModel<KangarooEntity> model) {
        // No extra layers by default
    }

    // --- Shared helpers for subclasses ---

    protected static void renderGlow(PoseStack poseStack, MultiBufferSource bufferSource,
                                     ResourceLocation texture, KangarooModel<KangarooEntity> model) {
        VertexConsumer vc = bufferSource.getBuffer(RenderType.eyes(texture));
        model.renderGeometryOnly(poseStack, vc, 15728640, OverlayTexture.NO_OVERLAY, -1);
    }

    protected static void renderCutout(PoseStack poseStack, MultiBufferSource bufferSource,
                                       ResourceLocation texture, int packedLight, int packedOverlay,
                                       KangarooModel<KangarooEntity> model) {
        VertexConsumer vc = bufferSource.getBuffer(RenderType.entityCutout(texture));
        model.renderGeometryOnly(poseStack, vc, packedLight, packedOverlay, -1);
    }
}
