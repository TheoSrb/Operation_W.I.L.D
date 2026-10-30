package net.tiew.operationWild.client.pose;

import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class OWStruggle {

    private static final float IMPULSE = 1.1f;
    private static final float FERVOUR_GAIN = 0.35f;
    private static final float FERVOUR_DECAY = 0.025f;

    private final OWPoseSpring right = new OWPoseSpring(0.28f, 0.62f);
    private final OWPoseSpring left = new OWPoseSpring(0.28f, 0.62f);
    private float fervour;
    private float fervourO;
    private int lastTimeout = -1;
    private boolean rightNext = true;

    public void track(int timeout) {
        if (lastTimeout >= 0 && timeout < lastTimeout) {
            (rightNext ? right : left).kick(IMPULSE);
            rightNext = !rightNext;
            fervour = Math.min(1f, fervour + FERVOUR_GAIN);
        }
        lastTimeout = timeout;
        fervourO = fervour;
        fervour = Math.max(0f, fervour - FERVOUR_DECAY);
        right.step(0f);
        left.step(0f);
    }

    public void reset() {
        lastTimeout = -1;
    }

    public float right(float partialTick) {
        return Math.max(0f, right.get(partialTick));
    }

    public float left(float partialTick) {
        return Math.max(0f, left.get(partialTick));
    }

    public float rightSigned(float partialTick) {
        return right.get(partialTick);
    }

    public float leftSigned(float partialTick) {
        return left.get(partialTick);
    }

    public float fervour(float partialTick) {
        return Mth.lerp(partialTick, fervourO, fervour);
    }
}
