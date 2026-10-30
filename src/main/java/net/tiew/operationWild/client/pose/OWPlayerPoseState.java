package net.tiew.operationWild.client.pose;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class OWPlayerPoseState {

    final float[] weight;
    final float[] weightO;
    final Object[] data;
    int lastSeen;

    private Vec3 position;
    private Vec3 velocity = Vec3.ZERO;
    private Entity vehicle;
    private Entity previousVehicle;

    int dizzyRequest;

    OWPlayerPoseState(int poses) {
        this.weight = new float[poses];
        this.weightO = new float[poses];
        this.data = new Object[poses];
    }

    void observe(Player player) {
        Vec3 now = player.position();
        velocity = position == null ? Vec3.ZERO : now.subtract(position);
        position = now;

        previousVehicle = vehicle;
        vehicle = player.getVehicle();
    }

    public Vec3 velocity() {
        return velocity;
    }

    public Entity previousVehicle() {
        return previousVehicle;
    }

    public boolean justDismounted() {
        return previousVehicle != null && vehicle == null;
    }

    public void requestDizzy(int ticks) {
        dizzyRequest = Math.max(dizzyRequest, ticks);
    }

    public float weight(int index, float partialTick) {
        return OWPoseMath.smoothStep(Mth.lerp(partialTick, weightO[index], weight[index]));
    }
}
