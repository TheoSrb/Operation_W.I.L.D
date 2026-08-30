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
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import net.tiew.operationWild.entity.misc.ThrownRockEntity;

public class ThrownRockRenderer extends EntityRenderer<ThrownRockEntity> {

    private static final float SCALE = 4.5f;

    private final ItemRenderer itemRenderer;

    public ThrownRockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ThrownRockEntity rock, float yaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        Vec3 axis = rock.getSpinAxis();

        pose.pushPose();
        pose.translate(0.0, rock.getBbHeight() * 0.5, 0.0);

        if (axis != null) {
            pose.mulPose(new Quaternionf().rotateAxis(
                    (float) Math.toRadians(rock.getSpin(partialTick)),
                    (float) axis.x, (float) axis.y, (float) axis.z));
        }

        pose.mulPose(Axis.YP.rotationDegrees(rock.getSpinSeed()));
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
