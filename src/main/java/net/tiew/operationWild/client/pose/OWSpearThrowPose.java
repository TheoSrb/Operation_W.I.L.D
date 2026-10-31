package net.tiew.operationWild.client.pose;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.item.custom.PrimitiveSpearItem;

import static net.tiew.operationWild.client.pose.OWPoseMath.add;
import static net.tiew.operationWild.client.pose.OWPoseMath.blend;
import static net.tiew.operationWild.client.pose.OWPoseMath.euler;
import static net.tiew.operationWild.client.pose.OWPoseMath.inBodyFrame;
import static net.tiew.operationWild.client.pose.OWPoseMath.smoothStep;

@OnlyIn(Dist.CLIENT)
public final class OWSpearThrowPose extends OWPlayerPose<OWSpearThrowPose.ThrowData> {

    public static final OWSpearThrowPose INSTANCE = new OWSpearThrowPose();

    private static final int THROW_TICKS = 14;
    private static final float WHIP_END = 0.32f;
    private static final float RECOVER_START = 0.5f;
    private static final float RAISED = -3.0f;
    private static final float RELEASED = -0.55f;
    private static final float CHARGE_OUTWARD = 0.22f;
    private static final float CHARGE_RAMP_TICKS = 3f;

    private static final float FP_PUNCH_END = 0.28f;
    private static final float FP_RETURN_START = 0.4f;
    private static final float FP_REACH = 0.32f;
    private static final float FP_TO_CENTER = 0.12f;
    private static final float FP_ARC = 0.06f;
    private static final float FP_DROP = 0.08f;
    private static final float FP_TILT = 14f;
    private static final float FP_HAND_X = 0.56f;
    private static final float FP_HAND_Y = -0.52f;
    private static final float FP_HAND_Z = -0.72f;

    private OWSpearThrowPose() {}

    static final class ThrowData {
        ItemStack spear = ItemStack.EMPTY;
        HumanoidArm arm = HumanoidArm.RIGHT;
        InteractionHand hand = InteractionHand.MAIN_HAND;
        boolean charging;
        int charge;
        int age = -1;

        float progress(float partialTick) {
            return age < 0 ? -1f : Mth.clamp((age + partialTick) / THROW_TICKS, 0f, 1f);
        }
    }

    @Override
    protected ThrowData createData(Player player) {
        return new ThrowData();
    }

    @Override
    protected boolean tick(Player player, ThrowData data, OWPlayerPoseState state) {
        boolean charging = player.isUsingItem() && player.getUseItem().getItem() instanceof PrimitiveSpearItem;
        if (charging) {
            if (!data.charging) data.charge = 0;
            data.charge++;
            data.spear = player.getUseItem();
            data.hand = player.getUsedItemHand();
            data.arm = data.hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
            data.age = -1;
        } else if (data.charging && data.charge >= PrimitiveSpearItem.minChargeTicks(data.spear, player.level())) {
            data.age = 0;
        } else if (data.age >= 0) {
            data.age++;
            if (data.age >= THROW_TICKS) data.age = -1;
        }
        data.charging = charging;
        return charging || data.age >= 0;
    }

    @Override
    protected float blendIn() {
        return 1f;
    }

    @Override
    protected void pose(PlayerModel<?> model, Player player, ThrowData data, float weight, float partialTick) {
        int side = data.arm == HumanoidArm.RIGHT ? 1 : -1;
        ModelPart throwing = data.arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;

        float progress = data.progress(partialTick);
        if (progress < 0f) {
            if (data.charging) {
                float ramp = smoothStep((data.charge - 1 + partialTick) / CHARGE_RAMP_TICKS);
                add(throwing, weight, 0f, 0f, -CHARGE_OUTWARD * side * ramp);
            }
            return;
        }

        float whip = smoothStep(progress / WHIP_END);
        float recover = smoothStep((progress - RECOVER_START) / (1f - RECOVER_START));
        float follow = Mth.sin(Mth.clamp((progress - 0.22f) / 0.38f, 0f, 1f) * Mth.PI);
        float strength = weight * (1f - recover);

        ModelPart other = data.arm == HumanoidArm.RIGHT ? model.leftArm : model.rightArm;
        ModelPart throwingLeg = data.arm == HumanoidArm.RIGHT ? model.rightLeg : model.leftLeg;
        ModelPart otherLeg = data.arm == HumanoidArm.RIGHT ? model.leftLeg : model.rightLeg;

        blend(throwing, strength, Mth.lerp(whip, RAISED, RELEASED) + 0.3f * follow, -0.35f * side * whip,
                Mth.lerp(whip, -CHARGE_OUTWARD, 0.12f) * side);
        blend(other, strength, 0.55f * whip, 0.1f * side * whip, -0.3f * side * whip);
        add(throwingLeg, strength, 0.25f * whip, 0f, 0f);
        add(otherLeg, strength, -0.35f * whip, 0f, 0f);
        add(model.head, strength, 0.15f * whip, 0f, 0f);
    }

    @Override
    protected void body(PoseStack poseStack, Player player, ThrowData data, float weight, float partialTick, float bodyYaw) {
        float progress = data.progress(partialTick);
        if (progress < 0f) return;

        float whip = smoothStep(progress / WHIP_END);
        float recover = smoothStep((progress - RECOVER_START) / (1f - RECOVER_START));
        float strength = weight * whip * (1f - recover);
        int side = data.arm == HumanoidArm.RIGHT ? 1 : -1;
        inBodyFrame(poseStack, bodyYaw, 0f, 0f, 0f, euler(14f * strength, 16f * side * strength, 0f), 0f, 0f, 0f);
    }

    void firstPerson(PoseStack poseStack, ThrowData data, InteractionHand hand, float partialTick) {
        float progress = data.progress(partialTick);
        if (progress < 0f || hand != data.hand) return;

        float out = smoothStep(progress / FP_PUNCH_END);
        float back = smoothStep((progress - FP_RETURN_START) / (1f - FP_RETURN_START));
        float thrust = out * (1f - back);
        float arc = Mth.sin(thrust * Mth.PI) * (1f - back);
        int side = data.arm == HumanoidArm.RIGHT ? 1 : -1;

        poseStack.translate(side * -FP_TO_CENTER * thrust, FP_ARC * arc - FP_DROP * thrust, -FP_REACH * thrust);
        poseStack.translate(side * FP_HAND_X, FP_HAND_Y, FP_HAND_Z);
        poseStack.mulPose(Axis.XP.rotationDegrees(-FP_TILT * thrust));
        poseStack.translate(side * -FP_HAND_X, -FP_HAND_Y, -FP_HAND_Z);
    }
}
