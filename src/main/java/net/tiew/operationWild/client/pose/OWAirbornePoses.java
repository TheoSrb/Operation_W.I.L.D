package net.tiew.operationWild.client.pose;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.tiew.operationWild.entity.animals.terrestrial.GorillaEntity;

import static net.tiew.operationWild.client.pose.OWPoseMath.blend;
import static net.tiew.operationWild.client.pose.OWPoseMath.euler;
import static net.tiew.operationWild.client.pose.OWPoseMath.inBodyFrame;

@OnlyIn(Dist.CLIENT)
public final class OWAirbornePoses {

    public static final OWPlayerPose<FlightData> FLIGHT = new Flight();

    private OWAirbornePoses() {}

    private static boolean landed(Player player, int airTicks) {
        return player.isPassenger() || player.isInWater() || player.isFallFlying()
                || (player.onGround() && airTicks > 3);
    }

    static final class FlightData {
        final OWPoseSpring climb = new OWPoseSpring(0.3f, 0.6f);
        final float seed;
        int ticks;
        int air;

        FlightData(float seed) {
            this.seed = seed;
        }
    }

    private static final class Flight extends OWPlayerPose<FlightData> {

        private static final int MAX_TICKS = 80;

        @Override
        protected FlightData createData(Player player) {
            return new FlightData(OWPoseMath.seed(player));
        }

        @Override
        protected boolean tick(Player player, FlightData data, OWPlayerPoseState state) {
            Vec3 velocity = state.velocity();
            if (state.justDismounted() && state.previousVehicle() instanceof GorillaEntity gorilla
                    && (gorilla.getRiderLaunchTick() > 0 || velocity.y > 0.25)) {
                data.ticks = MAX_TICKS;
                data.air = 0;
                data.climb.snap((float) velocity.y);
            }
            data.climb.step((float) velocity.y);
            if (data.ticks <= 0) return false;
            data.ticks--;
            data.air++;
            if (landed(player, data.air)) data.ticks = 0;
            return data.ticks > 0;
        }

        @Override
        protected void pose(PlayerModel<?> model, Player player, FlightData data, float weight, float partialTick) {
            float t = player.tickCount + partialTick;
            float climb = data.climb.get(partialTick);
            float rise = OWPoseMath.smoothStep((climb - 0.02f) / 0.3f);
            float fall = OWPoseMath.smoothStep((-climb - 0.05f) / 0.35f);
            float apex = Math.max(0f, 1f - rise - fall);

            float mill = t * 0.9f + data.seed;
            float flail = t * 1.3f + data.seed;
            float pedal = t + data.seed;

            mix(model.rightArm, weight, rise, apex, fall,
                    -0.2f, 0f, 2.1f,
                    -1.6f + 1.5f * Mth.sin(mill), 0f, 1.0f + 0.8f * Mth.cos(mill),
                    -2.8f + 0.45f * Mth.sin(flail), 0f, 0.4f + 0.2f * Mth.cos(flail));
            mix(model.leftArm, weight, rise, apex, fall,
                    -0.2f, 0f, -2.1f,
                    -1.6f + 1.5f * Mth.sin(mill + Mth.PI), 0f, -1.0f - 0.8f * Mth.cos(mill + Mth.PI),
                    -2.8f + 0.45f * Mth.sin(flail + 2f), 0f, -0.4f - 0.2f * Mth.cos(flail + 2f));
            mix(model.rightLeg, weight, rise, apex, fall,
                    0.15f, 0f, 0.5f,
                    -0.5f + 0.6f * Mth.sin(pedal), 0f, 0.15f,
                    0.5f * Mth.sin(t * 1.1f + data.seed), 0f, 0.2f);
            mix(model.leftLeg, weight, rise, apex, fall,
                    0.15f, 0f, -0.5f,
                    -0.5f + 0.6f * Mth.sin(pedal + Mth.PI), 0f, -0.15f,
                    -0.5f * Mth.sin(t * 1.1f + data.seed), 0f, -0.2f);
            mix(model.head, weight, rise, apex, fall,
                    -0.4f, 0f, 0f,
                    0.1f, 0f, Mth.sin(t * 0.5f) * 0.2f,
                    0.5f, 0f, 0f);
        }

        @Override
        protected void body(PoseStack poseStack, Player player, FlightData data, float weight, float partialTick, float bodyYaw) {
            float t = player.tickCount + partialTick;
            float climb = data.climb.get(partialTick);
            float rise = OWPoseMath.smoothStep((climb - 0.02f) / 0.3f);
            float fall = OWPoseMath.smoothStep((-climb - 0.05f) / 0.35f);
            float apex = Math.max(0f, 1f - rise - fall);

            float pitch = -12f * rise + 6f * apex + 24f * fall;
            float roll = Mth.sin(t * 0.4f + data.seed) * 6f;
            inBodyFrame(poseStack, bodyYaw, 0f, 0.9f, 0f, euler(pitch * weight, 0f, roll * weight), 0f, 0f, 0f);
        }

        private static void mix(ModelPart part, float weight, float a, float b, float c,
                                float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz) {
            blend(part, weight, a * ax + b * bx + c * cx, a * ay + b * by + c * cy, a * az + b * bz + c * cz);
        }
    }
}
