package net.tiew.operationWild.client.pose;

import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class OWPoseSpring {

    private final float stiffness;
    private final float damping;
    private float value;
    private float valueO;
    private float velocity;

    public OWPoseSpring(float stiffness, float damping) {
        this.stiffness = stiffness;
        this.damping = damping;
    }

    public void step(float target) {
        valueO = value;
        velocity += (target - value) * stiffness;
        velocity *= damping;
        value += velocity;
    }

    public void kick(float impulse) {
        velocity += impulse;
    }

    public void snap(float target) {
        value = valueO = target;
        velocity = 0f;
    }

    public float get(float partialTick) {
        return Mth.lerp(partialTick, valueO, value);
    }

    public float value() {
        return value;
    }
}
