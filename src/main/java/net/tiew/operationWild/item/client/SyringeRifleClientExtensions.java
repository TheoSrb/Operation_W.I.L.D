package net.tiew.operationWild.item.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.tiew.operationWild.item.custom.SyringeRifleItem;

public class SyringeRifleClientExtensions implements IClientItemExtensions {

    @Override
    public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
        return SyringeRifleItem.isReloading(stack) ? SyringeRifleArmPoses.RELOAD.getValue() : SyringeRifleArmPoses.HOLD.getValue();
    }

    @Override
    public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;
        InteractionHand hand = arm == player.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack stack = player.getItemInHand(hand);

        float swingX = -0.4F * Mth.sin(Mth.sqrt(swingProcess) * (float) Math.PI);
        float swingY = 0.2F * Mth.sin(Mth.sqrt(swingProcess) * (float) (Math.PI * 2));
        float swingZ = -0.2F * Mth.sin(swingProcess * (float) Math.PI);
        poseStack.translate(side * swingX, swingY, swingZ);
        applyArmTransform(poseStack, side, equipProcess);
        applyAttackTransform(poseStack, side, swingProcess);

        if (SyringeRifleItem.isReloading(stack)) {
            applyReloadTransform(poseStack, side, SyringeRifleItem.getReloadProgress(stack, player.level(), partialTick));
            return true;
        }

        if (player.getUsedItemHand() == hand) {
            float aim = SyringeRifleItem.getAimProgress(player, partialTick);
            aim = aim * aim * (3.0F - 2.0F * aim);
            poseStack.translate(side * -0.56F * aim, 0.2F * aim, 0.1F * aim);
        }

        float recoil = SyringeRifleItem.getRecoil(stack, player.level(), partialTick);
        poseStack.translate(0.0F, 0.02F * recoil, 0.12F * recoil);
        poseStack.mulPose(Axis.XP.rotationDegrees(8.0F * recoil));
        return true;
    }

    private static void applyReloadTransform(PoseStack poseStack, int side, float progress) {
        float envelope = SyringeRifleReloadMotion.envelope(progress);
        float insert = SyringeRifleReloadMotion.insert(progress);
        float snap = SyringeRifleReloadMotion.snap(progress);

        poseStack.translate(side * -0.14F * envelope, -0.12F * envelope + 0.03F * snap, 0.08F * envelope - 0.05F * insert);
        poseStack.mulPose(Axis.YP.rotationDegrees(side * 22.0F * envelope));
        poseStack.mulPose(Axis.XP.rotationDegrees(28.0F * envelope + 5.0F * insert - 9.0F * snap));
        poseStack.mulPose(Axis.ZP.rotationDegrees(side * 24.0F * envelope));
    }

    private static void applyArmTransform(PoseStack poseStack, int side, float equipProcess) {
        poseStack.translate(side * 0.56F, -0.52F + equipProcess * -0.6F, -0.72F);
    }

    private static void applyAttackTransform(PoseStack poseStack, int side, float swingProcess) {
        float swing = Mth.sin(swingProcess * swingProcess * (float) Math.PI);
        poseStack.mulPose(Axis.YP.rotationDegrees(side * (45.0F + swing * -20.0F)));
        float swingRoot = Mth.sin(Mth.sqrt(swingProcess) * (float) Math.PI);
        poseStack.mulPose(Axis.ZP.rotationDegrees(side * swingRoot * -20.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(swingRoot * -80.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(side * -45.0F));
    }
}
