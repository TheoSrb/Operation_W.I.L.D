package net.tiew.operationWild.client.pose;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class OWPlayerPose<D> {

    protected abstract D createData(Player player);

    protected abstract boolean tick(Player player, D data, OWPlayerPoseState state);

    protected abstract void pose(PlayerModel<?> model, Player player, D data, float weight, float partialTick);

    protected void body(PoseStack poseStack, Player player, D data, float weight, float partialTick, float bodyYaw) {}

    protected float blendIn() {
        return 0.2f;
    }

    protected float blendOut() {
        return 0.16f;
    }
}
