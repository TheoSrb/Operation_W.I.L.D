package net.tiew.operationWild.entity.client.animation;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.tiew.operationWild.entity.OWEntity;
import net.tiew.operationWild.entity.taming.OWSedationAnimState;
import net.tiew.operationWild.entity.taming.OWSedationAnimState.Phase;
import org.joml.Vector3f;

import java.util.Map;
import java.util.WeakHashMap;

public final class OWSedationAnimator {
    private static final float BLEND_IN_MS = 300f;
    private static final float HANDOFF_MS = 300f;
    private static final float SETTLE_MS = 250f;
    private static final float BLEND_OUT_MS = 400f;
    private static final float TWITCH_EYE_OPENING = 0.6f;

    private static final float[] FALL_EYES = {0f, 0f, 0.35f, 0.6f, 0.5f, 0.35f, 0.85f, 1f};
    private static final float[] WAKE_EYES = {0f, 1f, 0.12f, 0.4f, 0.22f, 1f, 0.34f, 0.25f, 0.44f, 0.85f, 0.6f, 0f};

    private static final int FIELDS = 9;
    private static final Vector3f CACHE = new Vector3f();
    private static final Map<HierarchicalModel<?>, ModelPart[]> PARTS = new WeakHashMap<>();
    private static float[] snapshot = new float[0];

    private OWSedationAnimator() {
    }

    public static float weight(OWEntity entity, OWSedationAnimationSet set, float ageInTicks) {
        OWSedationAnimState state = entity.sedationAnim;
        long ms = state.elapsedMs(ageInTicks);
        return switch (state.phase()) {
            case NONE -> 0f;
            case SLEEP -> state.skipsFall() || wakeInterrupted(state, set) ? 1f : smooth(ms / BLEND_IN_MS);
            case WAKE -> ms >= set.wakeMs() ? 0f : smooth((set.wakeMs() - ms) / BLEND_OUT_MS);
        };
    }

    public static float apply(HierarchicalModel<?> model, OWEntity entity, OWSedationAnimationSet set, float ageInTicks, ModelPart... eyes) {
        float weight = weight(entity, set, ageInTicks);
        if (weight <= 0f) return 0f;

        ModelPart[] parts = PARTS.computeIfAbsent(model, m -> m.root().getAllParts().toArray(ModelPart[]::new));
        boolean blend = weight < 1f;
        if (blend) capture(parts);
        for (ModelPart part : parts) part.resetPose();

        animateSedation(model, entity.sedationAnim, set, ageInTicks);
        if (blend) mix(parts, weight);

        float closure = eyeClosure(entity, set, ageInTicks);
        for (ModelPart eye : eyes) eye.yScale *= 1f - closure;
        return weight;
    }

    public static float eyeClosure(OWEntity entity, OWSedationAnimationSet set, float ageInTicks) {
        OWSedationAnimState state = entity.sedationAnim;
        long ms = state.elapsedMs(ageInTicks);
        switch (state.phase()) {
            case SLEEP -> {
                float closure = sleepClosure(set, ms, state.skipsFall());
                if (wakeInterrupted(state, set)) {
                    float handoff = smooth(ms / HANDOFF_MS);
                    closure = Mth.lerp(handoff, wakeClosure(set, state.previousElapsedMs()), closure);
                }
                float partialTick = Mth.clamp(ageInTicks - entity.tickCount, 0f, 1f);
                return closure * (1f - TWITCH_EYE_OPENING * entity.getWakeTwitchAmount(partialTick));
            }
            case WAKE -> {
                float handoff = smooth(ms / HANDOFF_MS);
                float from = sleepClosure(set, state.previousElapsedMs(), state.previousSkippedFall());
                return Mth.lerp(handoff, from, wakeClosure(set, ms));
            }
            default -> {
                return 0f;
            }
        }
    }

    private static void animateSedation(HierarchicalModel<?> model, OWSedationAnimState state, OWSedationAnimationSet set, float ageInTicks) {
        long ms = state.elapsedMs(ageInTicks);
        if (state.phase() == Phase.SLEEP) {
            float handoff = wakeInterrupted(state, set) ? smooth(ms / HANDOFF_MS) : 1f;
            if (handoff < 1f) animate(model, set.wake(), state.previousElapsedMs(), 1f - handoff);
            animateSleep(model, set, ms, state.skipsFall(), handoff);
        } else if (state.phase() == Phase.WAKE) {
            float handoff = smooth(ms / HANDOFF_MS);
            if (handoff < 1f) animateSleep(model, set, state.previousElapsedMs(), state.previousSkippedFall(), 1f - handoff);
            animate(model, set.wake(), ms, handoff);
        }
    }

    private static void animateSleep(HierarchicalModel<?> model, OWSedationAnimationSet set, long ms, boolean skipFall, float scale) {
        if (skipFall) {
            animate(model, set.rest(), ms, scale);
            return;
        }
        long fallMs = set.fallMs();
        if (ms < fallMs) {
            animate(model, set.fall(), ms, scale);
            return;
        }
        long restMs = ms - fallMs;
        float settle = smooth(restMs / SETTLE_MS);
        if (settle < 1f) animate(model, set.fall(), fallMs, scale * (1f - settle));
        animate(model, set.rest(), restMs, scale * settle);
    }

    private static void animate(HierarchicalModel<?> model, AnimationDefinition definition, long ms, float scale) {
        if (scale <= 0f) return;
        KeyframeAnimations.animate(model, definition, ms, scale, CACHE);
    }

    private static boolean wakeInterrupted(OWSedationAnimState state, OWSedationAnimationSet set) {
        return state.previous() == Phase.WAKE && state.previousElapsedMs() < set.wakeMs();
    }

    private static float sleepClosure(OWSedationAnimationSet set, long ms, boolean skipFall) {
        if (skipFall) return 1f;
        return curve(FALL_EYES, ms / (float) Math.max(1L, set.fallMs()));
    }

    private static float wakeClosure(OWSedationAnimationSet set, long ms) {
        return curve(WAKE_EYES, ms / (float) Math.max(1L, set.wakeMs()));
    }

    private static float curve(float[] points, float progress) {
        if (progress <= points[0]) return points[1];
        for (int i = 2; i < points.length; i += 2) {
            if (progress <= points[i]) {
                float t = (progress - points[i - 2]) / (points[i] - points[i - 2]);
                return Mth.lerp(smooth(t), points[i - 1], points[i + 1]);
            }
        }
        return points[points.length - 1];
    }

    private static void capture(ModelPart[] parts) {
        if (snapshot.length < parts.length * FIELDS) snapshot = new float[parts.length * FIELDS];
        for (int i = 0; i < parts.length; i++) {
            ModelPart part = parts[i];
            int o = i * FIELDS;
            snapshot[o] = part.x;
            snapshot[o + 1] = part.y;
            snapshot[o + 2] = part.z;
            snapshot[o + 3] = part.xRot;
            snapshot[o + 4] = part.yRot;
            snapshot[o + 5] = part.zRot;
            snapshot[o + 6] = part.xScale;
            snapshot[o + 7] = part.yScale;
            snapshot[o + 8] = part.zScale;
        }
    }

    private static void mix(ModelPart[] parts, float weight) {
        for (int i = 0; i < parts.length; i++) {
            ModelPart part = parts[i];
            int o = i * FIELDS;
            part.x = Mth.lerp(weight, snapshot[o], part.x);
            part.y = Mth.lerp(weight, snapshot[o + 1], part.y);
            part.z = Mth.lerp(weight, snapshot[o + 2], part.z);
            part.xRot = Mth.lerp(weight, snapshot[o + 3], part.xRot);
            part.yRot = Mth.lerp(weight, snapshot[o + 4], part.yRot);
            part.zRot = Mth.lerp(weight, snapshot[o + 5], part.zRot);
            part.xScale = Mth.lerp(weight, snapshot[o + 6], part.xScale);
            part.yScale = Mth.lerp(weight, snapshot[o + 7], part.yScale);
            part.zScale = Mth.lerp(weight, snapshot[o + 8], part.zScale);
        }
    }

    private static float smooth(float t) {
        float x = Mth.clamp(t, 0f, 1f);
        return x * x * (3f - 2f * x);
    }
}
