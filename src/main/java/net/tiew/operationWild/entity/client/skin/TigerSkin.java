package net.tiew.operationWild.entity.client.skin;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.tiew.operationWild.entity.animals.terrestrial.TigerEntity;
import net.tiew.operationWild.entity.client.model.TigerModel;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

public class TigerSkin {

    public enum Mode {
        /**
         * Swaps only the texture. The base model is used.
         * Example: DEFAULT, LIGHT_ORANGE, GOLDEN, WHITE, SKIN_GOLD
         */
        BASE,

        /**
         * Renders the base model + base texture first, then renders
         * a second model on top with a different texture.
         * Example: SKIN_BOSS (cape/crown accessories over the tiger)
         */
        OVERLAY,

        /**
         * Replaces the base model AND texture entirely.
         * Example: SKIN_VIRUS
         */
        REPLACEMENT
    }

    private final Mode mode;

    /**
     * For BASE/REPLACEMENT: the main texture.
     * For OVERLAY: the BASE tiger texture (shown on the base model).
     */
    private final ResourceLocation texture;

    /**
     * For OVERLAY only: the texture applied to the overlay model.
     */
    @Nullable
    private final ResourceLocation overlayTexture;

    /**
     * For OVERLAY and REPLACEMENT: the alternative model layer to bake.
     */
    @Nullable
    private final ModelLayerLocation modelLayer;

    /**
     * Supplier for the LayerDefinition to register on the event bus.
     * Required when modelLayer is non-null.
     */
    @Nullable
    private final Supplier<LayerDefinition> layerDefinitionSupplier;

    // --- Constructor ---

    protected TigerSkin(Mode mode, ResourceLocation texture, @Nullable ResourceLocation overlayTexture, @Nullable ModelLayerLocation modelLayer, @Nullable Supplier<LayerDefinition> layerDefinitionSupplier) {
        this.mode = mode;
        this.texture = texture;
        this.overlayTexture = overlayTexture;
        this.modelLayer = modelLayer;
        this.layerDefinitionSupplier = layerDefinitionSupplier;
    }

    // --- Factory Methods ---

    /**
     * Simple texture-only skin. No model change.
     */
    public static TigerSkin base(ResourceLocation texture) {
        return new TigerSkin(Mode.BASE, texture, null, null, null);
    }

    /**
     * Overlay skin: renders the base tiger normally, then renders an
     * additional model layer on top with the given overlay texture.
     */
    public static TigerSkin overlay(
            ResourceLocation baseTexture,
            ResourceLocation overlayTexture,
            ModelLayerLocation overlayModelLayer,
            Supplier<LayerDefinition> layerDef
    ) {
        return new TigerSkin(Mode.OVERLAY, baseTexture, overlayTexture, overlayModelLayer, layerDef);
    }

    /**
     * Replacement skin: replaces model and texture entirely.
     */
    public static TigerSkin replacement(
            ResourceLocation texture,
            ModelLayerLocation modelLayer,
            Supplier<LayerDefinition> layerDef
    ) {
        return new TigerSkin(Mode.REPLACEMENT, texture, null, modelLayer, layerDef);
    }

    // --- Getters ---

    public Mode getMode() { return mode; }

    public ResourceLocation getTexture() { return texture; }

    public Optional<ResourceLocation> getOverlayTexture() { return Optional.ofNullable(overlayTexture); }

    public Optional<ModelLayerLocation> getModelLayer() { return Optional.ofNullable(modelLayer); }

    public Optional<Supplier<LayerDefinition>> getLayerDefinitionSupplier() {
        return Optional.ofNullable(layerDefinitionSupplier);
    }

    // --- Override for custom rendering (glow, animated textures, etc.) ---

    /**
     * Called during the skin render layer pass.
     * Override this to add glowing overlays, animated faces, etc.
     *
     * Default: does nothing (no extra layers).
     */
    public void renderExtraLayers(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay,
            TigerEntity tiger,
            TigerModel<TigerEntity> model
    ) {
        // No extra layers by default
    }

    // --- Shared helpers for subclasses ---

    protected static void renderGlow(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            ResourceLocation texture,
            TigerModel<TigerEntity> model
    ) {
        VertexConsumer vc = bufferSource.getBuffer(RenderType.eyes(texture));
        model.renderToBuffer(poseStack, vc, 15728640, OverlayTexture.NO_OVERLAY);
    }

    protected static void renderCutout(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            ResourceLocation texture,
            int packedLight,
            int packedOverlay,
            TigerModel<TigerEntity> model
    ) {
        VertexConsumer vc = bufferSource.getBuffer(RenderType.entityCutout(texture));
        model.renderToBuffer(poseStack, vc, packedLight, packedOverlay);
    }
}