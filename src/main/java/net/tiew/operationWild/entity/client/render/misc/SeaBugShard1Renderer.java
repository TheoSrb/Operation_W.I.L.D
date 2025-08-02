package net.tiew.operationWild.entity.client.render.misc;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.client.model.misc.SeaBugShard1Model;
import net.tiew.operationWild.entity.custom.object.SeaBugShard1Entity;
import net.tiew.operationWild.entity.variants.SeaBugShardVariant;

import java.util.Map;

public class SeaBugShard1Renderer extends MobRenderer<SeaBugShard1Entity, SeaBugShard1Model<SeaBugShard1Entity>> {

    private static final Map<SeaBugShardVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(SeaBugShardVariant.class), map -> {
        map.put(SeaBugShardVariant.DEFAULT_SHARD_1, ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "textures/entity/seabug_shard/seabug_shard_1.png"));
    });

    public SeaBugShard1Renderer(EntityRendererProvider.Context context) {
        super(context, new SeaBugShard1Model<>(context.bakeLayer(SeaBugShard1Model.LAYER_LOCATION)), 0.6f);;
    }

    @Override
    public ResourceLocation getTextureLocation(SeaBugShard1Entity seabug) {
        return LOCATION_BY_VARIANT.get(seabug.getVariant());
    }

    @Override
    public void render(SeaBugShard1Entity seabug, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        this.setupRotations(seabug, poseStack, seabug.tickCount + partialTicks, entityYaw, partialTicks, 1.0F);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(seabug, poseStack, partialTicks);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        SeaBugShard1Model<SeaBugShard1Entity> model = this.getModel();
        model.setupAnim(seabug, 0.0F, 0.0F, seabug.tickCount + partialTicks, 0.0F, 0.0F);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(model.renderType(this.getTextureLocation(seabug)));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);

        poseStack.popPose();

        if (this.shouldShowName(seabug)) {
            this.renderNameTag(seabug, seabug.getDisplayName(), poseStack, bufferSource, packedLight, partialTicks);
        }
    }

    @Override
    protected void renderNameTag(SeaBugShard1Entity seabug, Component component, PoseStack poseStack, MultiBufferSource bufferSource, int i, float v) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.65D, 0.0D);
        super.renderNameTag(seabug, component, poseStack, bufferSource, i, v);
        poseStack.popPose();
    }
}
