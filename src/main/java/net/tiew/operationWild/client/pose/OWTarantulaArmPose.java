package net.tiew.operationWild.client.pose;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.entity.animals.terrestrial.TarantulaEntity;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;

import static net.tiew.operationWild.client.pose.OWPoseMath.blend;
import static net.tiew.operationWild.client.pose.OWPoseMath.pulse;

@OnlyIn(Dist.CLIENT)
public final class OWTarantulaArmPose extends OWPlayerPose<OWTarantulaArmPose.ArmData> {

    public static final OWTarantulaArmPose INSTANCE = new OWTarantulaArmPose();

    private static final float CARRY_PITCH = -1.25f;
    private static final float CARRY_INWARD = 0.1f;
    private static final float LOOK_FOLLOW = 0.6f;
    private static final float CARRY_ROLL_OUT = 0.08f;
    private static final float BOB_SPEED = 0.09f;
    private static final float BOB_AMPLITUDE = 0.035f;
    private static final float AIM_RESPONSE = 0.35f;
    private static final float AIM_YAW_LIMIT = 1.3f;
    private static final float RECOIL_LIFT = 0.45f;
    private static final float WINDUP_DROP = 0.2f;
    private static final float TUCK_RESPONSE = 0.2f;
    private static final double SHOULDER_HEIGHT = 1.29;
    private static final double SHOULDER_SIDE = 0.31;

    private OWTarantulaArmPose() {}

    static final class ArmData {
        final float seed;
        final float[] aim = new float[2];
        final float[] aimO = new float[2];
        float tuck;
        float tuckO;

        ArmData(float seed) {
            this.seed = seed;
        }
    }

    @Override
    protected ArmData createData(Player player) {
        return new ArmData(OWPoseMath.seed(player));
    }

    @Override
    protected boolean tick(Player player, ArmData data, OWPlayerPoseState state) {
        boolean pulled = false;
        for (int slot = 0; slot < 2; slot++) {
            TarantulaEntity tarantula = TarantulaEntity.carriedOn(player, slot);
            data.aimO[slot] = data.aim[slot];
            float target = tarantula != null && tarantula.isWebOut() ? 1f : 0f;
            data.aim[slot] += (target - data.aim[slot]) * AIM_RESPONSE;
            if (target == 0f && data.aim[slot] < 0.005f) data.aim[slot] = 0f;
            if (tarantula != null && tarantula.isWebAttached()) pulled = true;
        }
        data.tuckO = data.tuck;
        data.tuck += ((pulled && !player.onGround() ? 1f : 0f) - data.tuck) * TUCK_RESPONSE;
        return TarantulaEntity.carriesAny(player) && !player.isSleeping();
    }

    @Override
    protected float blendIn() {
        return 0.15f;
    }

    @Override
    protected float blendOut() {
        return 0.12f;
    }

    @Override
    protected void pose(PlayerModel<?> model, Player player, ArmData data, float weight, float partialTick) {
        for (Entity passenger : player.getPassengers()) {
            if (!(passenger instanceof TarantulaEntity tarantula)) continue;
            int slot = tarantula.carrySlot();
            boolean left = slot == TarantulaEntity.SLOT_LEFT;
            ModelPart arm = left ? model.leftArm : model.rightArm;
            float side = left ? 1f : -1f;

            float t = player.tickCount + partialTick;
            float bob = Mth.sin(t * BOB_SPEED + data.seed + slot * 1.7f) * BOB_AMPLITUDE;
            float pitch = CARRY_PITCH + model.head.xRot * LOOK_FOLLOW + bob + shotKick(tarantula, partialTick);
            float yaw = model.head.yRot * LOOK_FOLLOW + side * CARRY_INWARD;

            float aim = Mth.lerp(partialTick, data.aimO[slot], data.aim[slot]);
            if (aim > 0f) {
                float[] toWeb = aimAtWeb(player, tarantula, left, partialTick);
                if (toWeb != null) {
                    pitch = Mth.lerp(aim, pitch, toWeb[0]);
                    yaw = Mth.lerp(aim, yaw, toWeb[1]);
                }
            }
            float carry = weight * (1f - tarantula.shoulderBlend(partialTick));
            float armWeight = Math.max(carry, weight * aim);
            if (armWeight > 0f) blend(arm, armWeight, pitch, yaw, -side * CARRY_ROLL_OUT);
        }

        float tuck = Mth.lerp(partialTick, data.tuckO, data.tuck) * weight;
        if (tuck > 0f) {
            model.rightLeg.xRot = Mth.lerp(tuck, model.rightLeg.xRot, -0.55f);
            model.leftLeg.xRot = Mth.lerp(tuck, model.leftLeg.xRot, -0.2f);
        }
    }

    private static float shotKick(TarantulaEntity tarantula, float partialTick) {
        int timer = tarantula.getWebShotTimer();
        if (timer <= 0) return 0f;
        float total = OWAttacksConstants.Tarantula.WEB_SHOT_TICKS;
        float progress = Mth.clamp((total - timer + partialTick) / total, 0f, 1f);
        float windup = OWAttacksConstants.Tarantula.WEB_WINDUP_TICKS / total;
        if (progress < windup) return WINDUP_DROP * OWPoseMath.smoothStep(progress / windup);
        float recoil = pulse((progress - windup) / (1f - windup), 0.15f);
        return Mth.lerp(OWPoseMath.smoothStep((progress - windup) / 0.1f), WINDUP_DROP, 0f) - RECOIL_LIFT * recoil;
    }

    private static float[] aimAtWeb(Player player, TarantulaEntity tarantula, boolean left, float partialTick) {
        Vec3 end = tarantula.webEnd();
        int targetId = tarantula.getWebTarget();
        if (targetId >= 0 && tarantula.level().getEntity(targetId) instanceof Entity target) {
            end = target.getPosition(partialTick).add(0, target.getBbHeight() * 0.5, 0);
        }
        float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot) * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Mth.sin(bodyYaw), 0, Mth.cos(bodyYaw));
        Vec3 right = new Vec3(-Mth.cos(bodyYaw), 0, -Mth.sin(bodyYaw));
        double scale = player.getScale();
        Vec3 shoulder = player.getPosition(partialTick).add(0, SHOULDER_HEIGHT * scale, 0)
                .add(right.scale((left ? -SHOULDER_SIDE : SHOULDER_SIDE) * scale));
        Vec3 direction = end.subtract(shoulder);
        if (direction.lengthSqr() < 1.0e-4) return null;
        direction = direction.normalize();

        float elevation = (float) Math.asin(Mth.clamp(direction.y, -1.0, 1.0));
        float yaw = (float) Mth.atan2(direction.dot(right), direction.dot(forward));
        return new float[]{-Mth.HALF_PI - elevation, Mth.clamp(yaw, -AIM_YAW_LIMIT, AIM_YAW_LIMIT)};
    }
}
