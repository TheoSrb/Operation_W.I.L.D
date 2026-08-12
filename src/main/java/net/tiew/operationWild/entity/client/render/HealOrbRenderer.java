package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.misc.HealOrbEntity;
import org.joml.Matrix4f;

/**
 * Bille de soin : trois disques concentriques face caméra, du halo diffus au cœur presque blanc.
 *
 * <p>Ni modèle ni texture. {@link RenderType#lightning()} rend de la couleur pure, sans lumière ni
 * image — la bille brille donc d'elle-même en pleine nuit comme au fond d'une grotte, ce qu'aucun
 * cube texturé n'aurait fait sans une passe émissive dédiée.</p>
 *
 * <p>Trois couches suffisent à faire une sphère : l'œil lit le dégradé du bord vers le centre comme
 * un volume, là où un disque uni se serait lu comme une pastille collée à l'écran.</p>
 */
public class HealOrbRenderer extends EntityRenderer<HealOrbEntity> {

    private static final int SEGMENTS = 18;

    public HealOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(HealOrbEntity orb, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        float radius = OWAttacksConstants.RedPanda.HEAL_ORB_VISUAL_RADIUS;
        float pulse = 1.0f + 0.12f * Mth.sin((orb.tickCount + partialTicks) * 0.45f);

        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());

        // Rouge et bleu tenus TRÈS bas : le type de rendu mélange en additif, donc chaque couche
        // ajoute sa couleur à celle du dessous. Un cœur à 0,78 de rouge et 0,72 de bleu, empilé sur
        // deux autres couches, saturait les trois canaux — la bille virait au blanc bleuté.
        drawDisc(consumer, matrix, radius * 2.1f * pulse, 0.04f, 0.55f, 0.12f, 0.16f);
        drawDisc(consumer, matrix, radius * 1.35f * pulse, 0.08f, 0.85f, 0.20f, 0.50f);
        drawDisc(consumer, matrix, radius * pulse, 0.16f, 1.00f, 0.30f, 0.95f);

        poseStack.popPose();
    }

    /**
     * Disque en éventail. Le format n'accepte que des quads, chaque part en est donc un dont deux
     * sommets sont confondus au centre — un triangle, sans avoir à changer de type de rendu.
     */
    private static void drawDisc(VertexConsumer consumer, Matrix4f matrix, float radius,
                                 float red, float green, float blue, float alpha) {
        for (int i = 0; i < SEGMENTS; i++) {
            float a0 = (float) (Mth.TWO_PI * i / SEGMENTS);
            float a1 = (float) (Mth.TWO_PI * (i + 1) / SEGMENTS);

            float x0 = Mth.cos(a0) * radius, y0 = Mth.sin(a0) * radius;
            float x1 = Mth.cos(a1) * radius, y1 = Mth.sin(a1) * radius;

            consumer.addVertex(matrix, 0f, 0f, 0f).setColor(red, green, blue, alpha);
            consumer.addVertex(matrix, x0, y0, 0f).setColor(red, green, blue, alpha * 0.35f);
            consumer.addVertex(matrix, x1, y1, 0f).setColor(red, green, blue, alpha * 0.35f);
            consumer.addVertex(matrix, 0f, 0f, 0f).setColor(red, green, blue, alpha);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(HealOrbEntity orb) {
        return net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;
    }
}
