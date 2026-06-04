package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.client.util.GhostBufferSource;
import net.tiew.operationWild.entity.misc.SoulGhostEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Rendu GÉNÉRIQUE du fantôme spectral : reconstruit côté client le modèle réel du compagnon
 * (n'importe quel OWEntity) à partir du type/variant/skin/échelle portés par le
 * {@link SoulGhostEntity}, puis le dessine via le dispatcher en mode spectral
 * ({@code OWEntityRenderer.RENDER_AS_GHOST} + {@link GhostBufferSource}).
 *
 * <p>Aucune texture ni code dédiés par espèce : tout nouvel OWEntity est automatiquement
 * supporté.</p>
 */
public class SoulGhostRenderer extends EntityRenderer<SoulGhostEntity> {

    private static final ResourceLocation FALLBACK = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    /** Cache d'entités-modèles temporaires (jamais ajoutées au monde), par id de fantôme. */
    private final Map<Integer, OWEntity> renderEntities = new HashMap<>();

    public SoulGhostRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0f;
    }

    @Override
    public void render(SoulGhostEntity ghost, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        EntityType<?> type = ghost.resolveEntityType();
        Level level = Minecraft.getInstance().level;
        if (type == null || level == null) return;

        OWEntity temp = renderEntities.get(ghost.getId());
        if (temp == null || temp.getType() != type) {
            Entity created = type.create(level);
            if (!(created instanceof OWEntity ow)) return;
            temp = ow;
            renderEntities.put(ghost.getId(), temp);
        }

        // Recopie l'apparence stockée dans le fantôme.
        temp.setVariant(temp, ghost.getGhostVariant());
        if (ghost.getGhostSkin() != 0) temp.changeSkinSilent(ghost.getGhostSkin());
        temp.setScale(ghost.getGhostScale());
        temp.tickCount = ghost.tickCount;

        float rot = ghost.getYRot();
        temp.setYRot(rot);
        temp.yRotO = rot;
        temp.yBodyRot = rot;
        temp.yBodyRotO = rot;
        temp.yHeadRot = rot;
        temp.yHeadRotO = rot;
        temp.setPos(ghost.getX(), ghost.getY(), ghost.getZ());

        // L'opacité monte avec la matérialisation : à peine perceptible au début, presque solide à la fin.
        float mat = ghost.getMaterialization();
        float alpha = 0.12f + 0.6f * mat;

        boolean prevSuppress = OWEntityRenderer.SUPPRESS_INFO_IN_GUI;
        OWEntityRenderer.SUPPRESS_INFO_IN_GUI = true;
        OWEntityRenderer.RENDER_AS_GHOST = true;
        TigerRenderer.ghostAlpha = alpha; // chemin alpha spécifique au modèle Tigre
        // Boa et crocodile ont leur queue en entités séparées : on affiche le corps entier sur le fantôme.
        net.tiew.operationWild.entity.client.model.BoaModel.RENDER_FULL_BODY = true;
        net.tiew.operationWild.entity.client.model.CrocodileModel.RENDER_FULL_BODY = true;
        GhostBufferSource ghostBuffers = new GhostBufferSource(bufferSource, alpha);
        try {
            Minecraft.getInstance().getEntityRenderDispatcher().render(
                    temp, 0, 0, 0, rot, partialTick, poseStack, ghostBuffers, LightTexture.FULL_BRIGHT);
        } finally {
            OWEntityRenderer.RENDER_AS_GHOST = false;
            OWEntityRenderer.SUPPRESS_INFO_IN_GUI = prevSuppress;
            TigerRenderer.ghostAlpha = 0f;
            net.tiew.operationWild.entity.client.model.BoaModel.RENDER_FULL_BODY = false;
            net.tiew.operationWild.entity.client.model.CrocodileModel.RENDER_FULL_BODY = false;
        }

        super.render(ghost, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SoulGhostEntity entity) {
        return FALLBACK;
    }
}
