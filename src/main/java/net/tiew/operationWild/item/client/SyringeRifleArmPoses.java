package net.tiew.operationWild.item.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;
import net.tiew.operationWild.item.custom.SyringeRifleItem;

public final class SyringeRifleArmPoses {
    public static final EnumProxy<HumanoidModel.ArmPose> HOLD = new EnumProxy<>(HumanoidModel.ArmPose.class, true, (IArmPoseTransformer) SyringeRifleArmPoses::applyHold);
    public static final EnumProxy<HumanoidModel.ArmPose> RELOAD = new EnumProxy<>(HumanoidModel.ArmPose.class, true, (IArmPoseTransformer) SyringeRifleArmPoses::applyReload);

    private static final float LOW_READY_DROP = 0.55F;

    private SyringeRifleArmPoses() {}

    private static float partialTick() {
        return Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
    }

    private static ItemStack rifleInArm(LivingEntity entity, HumanoidArm arm) {
        return arm == entity.getMainArm() ? entity.getMainHandItem() : entity.getOffhandItem();
    }

    private static void applyHold(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        float partialTick = partialTick();
        float aim = SyringeRifleItem.getAimProgress(entity, partialTick);
        poseHold(model, arm, aim * aim * (3.0F - 2.0F * aim));

        float recoil = SyringeRifleItem.getRecoil(rifleInArm(entity, arm), entity.level(), partialTick);
        holdingArm(model, arm).xRot -= 0.3F * recoil;
        supportArm(model, arm).xRot -= 0.25F * recoil;
    }

    private static void applyReload(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        float progress = SyringeRifleItem.getReloadProgress(rifleInArm(entity, arm), entity.level(), partialTick());
        float envelope = SyringeRifleReloadMotion.envelope(progress);
        float insert = SyringeRifleReloadMotion.insert(progress);
        float snap = SyringeRifleReloadMotion.snap(progress);

        poseHold(model, arm, 0.0F);
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;
        ModelPart holding = holdingArm(model, arm);
        ModelPart support = supportArm(model, arm);

        holding.xRot = Mth.lerp(envelope, holding.xRot, -0.75F) + 0.18F * snap;
        holding.yRot = Mth.lerp(envelope, holding.yRot, side * -0.15F);

        support.xRot = Mth.lerp(envelope, support.xRot, -1.15F) - 0.35F * insert + 0.25F * snap;
        support.yRot = Mth.lerp(envelope, support.yRot, side * 0.75F);
    }

    private static void poseHold(HumanoidModel<?> model, HumanoidArm arm, float aim) {
        AnimationUtils.animateCrossbowHold(model.rightArm, model.leftArm, model.head, arm == HumanoidArm.RIGHT);
        float lowered = (1.0F - aim) * (LOW_READY_DROP - model.head.xRot);
        holdingArm(model, arm).xRot += lowered;
        supportArm(model, arm).xRot += lowered;
    }

    private static ModelPart holdingArm(HumanoidModel<?> model, HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
    }

    private static ModelPart supportArm(HumanoidModel<?> model, HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? model.leftArm : model.rightArm;
    }
}
