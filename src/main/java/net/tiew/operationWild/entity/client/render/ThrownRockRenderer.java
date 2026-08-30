package net.tiew.operationWild.entity.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.tiew.operationWild.entity.misc.ThrownRockEntity;

public class ThrownRockRenderer extends EntityRenderer<ThrownRockEntity> {

    private static final float SCALE = 4.5f;
    private static final float SPIN_PER_TICK = 9f;
    private static final float TUMBLE_PER_TICK = 13f;

    private final ItemRenderer itemRenderer;

    public ThrownRockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ThrownRockEntity rock, float yaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        float age = rock.tickCount + partialTick;

        pose.pushPose();
        pose.translate(0.0, rock.getBbHeight() * 0.5, 0.0);
        pose.mulPose(Axis.YP.rotationDegrees(age * SPIN_PER_TICK + rock.getSpinSeed()));
        pose.mulPose(Axis.XP.rotationDegrees(age * TUMBLE_PER_TICK));
        pose.scale(SCALE, SCALE, SCALE);

        this.itemRenderer.renderStatic(rock.getItem(), ItemDisplayContext.GROUND, light,
                OverlayTexture.NO_OVERLAY, pose, buffers, rock.level(), rock.getId());
        pose.popPose();

        super.render(rock, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownRockEntity rock) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
