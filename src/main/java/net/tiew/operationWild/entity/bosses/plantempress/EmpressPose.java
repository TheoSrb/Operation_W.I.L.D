package net.tiew.operationWild.entity.bosses.plantempress;

import net.minecraft.util.Mth;
import net.tiew.operationWild.entity.bosses.PlantEmpressEntity;

import java.util.ArrayList;
import java.util.List;

public final class EmpressPose {

    public static final int BUSY_FADE_TICKS = 6;
    public static final int AIM_FADE_TICKS = 8;
    public static final int HURT_TICKS = 15;
    public static final int DEATH_FADE_TICKS = 6;
    public static final int DODGE_TICKS = 22;

    public record Layer(EmpressAnim anim, float seconds, float weight, int mask) {}

    public static final class Composition {
        public final List<Layer> layers = new ArrayList<>();
        public final List<Layer> deadLayers = new ArrayList<>();
        public final float[] chainYaw = new float[3];
        public final float[] chainStretch = {1f, 1f, 1f};
        public final float[] chainPitch = new float[3];
        public final float[] deadFade = new float[3];
        public final float[][] procedural = new float[EmpressRig.COUNT][3];
        public float allYaw;

        void add(EmpressAnim anim, float seconds, float weight, int mask) {
            if (weight > 0.0001f) layers.add(new Layer(anim, seconds, weight, mask));
        }

        void rotate(int bone, float x, float y, float z) {
            procedural[bone][0] += x;
            procedural[bone][1] += y;
            procedural[bone][2] += z;
        }
    }

    private EmpressPose() {
    }

    public static Composition compose(PlantEmpressEntity e, float partialTick) {
        EmpressRig rig = EmpressRig.get();
        Composition c = new Composition();
        float now = e.level().getGameTime() + partialTick;
        int phase = e.getPhase();
        float death = phase == PlantEmpressEntity.PHASE_DYING ? smooth((now - e.getPhaseStart()) / DEATH_FADE_TICKS) : 0f;
        float live = 1f - death;

        if (phase == PlantEmpressEntity.PHASE_SPAWN) {
            float spawn = e.getPhaseStart() < 0 ? 0f : now - e.getPhaseStart();
            c.add(EmpressAnim.SPAWN, spawn / 20f, 1f, -1);
            return c;
        }

        float burrow = 0f;
        if (phase == PlantEmpressEntity.PHASE_BURROW) {
            float dt = now - e.getPhaseStart();
            int total = EmpressTimings.BURROW_TICKS;
            burrow = Math.min(smooth(dt / 6f), smooth((total - dt) / 10f));
            float seconds;
            float up = EmpressTimings.BURROW_DOWN + EmpressTimings.BURROW_TRAVEL;
            if (dt < EmpressTimings.BURROW_HIDE) {
                seconds = 10.0f - dt / EmpressTimings.BURROW_HIDE * 2.55f;
            } else if (dt < up) {
                seconds = 2.0f;
            } else if (dt < up + EmpressTimings.BURROW_EMERGE) {
                seconds = 3.4f + (dt - up) / EmpressTimings.BURROW_EMERGE * 4.05f;
            } else {
                seconds = 7.45f + Math.min(1f, (dt - up - EmpressTimings.BURROW_EMERGE) / EmpressTimings.BURROW_UNFURL) * 2.55f;
            }

            c.add(EmpressAnim.SPAWN, seconds, burrow, -1);
        }
        float rage = e.getRageStart() >= 0 ? smooth((now - e.getRageStart()) / 20f) : 0f;
        c.add(EmpressAnim.IDLE, (now - e.getIdleEpoch()) / 20f, (1f - rage) * live * (1f - burrow), -1);
        if (rage > 0f) c.add(EmpressAnim.IDLE_RAGE, (now - e.getRageStart()) / 20f, rage * live * (1f - burrow), -1);
        EmpressAnim stems = EmpressAnim.byId(e.getStemAction());
        if (stems != null) {
            float dt = now - e.getStemStart();
            if (dt >= 0f && dt <= rig.lengthTicks(stems)) c.add(stems, dt / 20f, live, -1);
        }
        if (death > 0f) c.add(EmpressAnim.DEATH, (now - e.getPhaseStart()) / 20f, death, -1);

        float bodyBusy = 0f;
        EmpressAnim body = EmpressAnim.byId(e.getBodyAction());
        if (body != null) {
            float dt = now - e.getBodyStart();
            int length = rig.lengthTicks(body);
            if (dt >= 0f && dt <= length) {
                c.add(body, dt / 20f, live, -1);
                c.allYaw = e.getBodyAim() * envelope(dt, length, AIM_FADE_TICKS);
                bodyBusy = envelope(dt, length, BUSY_FADE_TICKS);
            }
        }

        float[] busy = new float[3];
        for (int chain = 0; chain < 3; chain++) {
            busy[chain] = bodyBusy;
            if (e.isChainDead(chain)) continue;
            int action = e.getChainAction(chain);
            float dt = now - e.getChainStart(chain);
            if (action == PlantEmpressEntity.ACTION_DODGE) {
                if (dt >= 0f && dt <= DODGE_TICKS) {
                    dodge(c, chain, dt, e.getChainAim(chain), live);
                    busy[chain] = Math.max(busy[chain], envelope(dt, DODGE_TICKS, 3));
                }
            } else {
                EmpressAnim anim = EmpressAnim.byId(action);
                if (anim != null) {
                    int length = rig.lengthTicks(anim);
                    if (dt >= 0f && dt <= length) {
                        c.add(anim, dt / 20f, live, chain);
                        float reach = envelope(dt, length, AIM_FADE_TICKS);
                        c.chainYaw[chain] = e.getChainAim(chain) * reach;
                        c.chainStretch[chain] = 1f + (e.getChainStretch(chain) - 1f) * reach;
                        c.chainPitch[chain] = e.getChainPitch(chain) * reach;
                        busy[chain] = Math.max(busy[chain], envelope(dt, length, BUSY_FADE_TICKS));
                    }
                }
            }
            float hurt = now - e.getHurtStart(chain);
            if (hurt >= 0f && hurt < HURT_TICKS) c.add(EmpressAnim.hurt(chain), hurt / 20f, live * (1f - bodyBusy), chain);
        }

        boolean fighting = phase == PlantEmpressEntity.PHASE_FIGHT || phase == PlantEmpressEntity.PHASE_DORMANT;
        if (burrow > 0f) {
            for (int chain = 0; chain < 3; chain++) busy[chain] = Math.max(busy[chain], burrow);
        }
        if (fighting) {
            for (int chain = 0; chain < 3; chain++) {
                if (e.isChainDead(chain) || (chain == 0 && e.isSealed())) continue;
                float fraction = e.getHeadFraction(chain);
                float agitation = Mth.clamp((0.7f - fraction) / 0.3f, 0f, 1f);
                float frenzy = Mth.clamp((0.36f - fraction) / 0.22f, 0f, 1f);
                float weight = agitation * (1f - busy[chain]) * live;
                if (weight > 0.001f) weave(c, chain, now / 20f, weight, frenzy, e.getId());
            }
            float look = (1f - busy[0]) * live;
            if (look > 0f) {
                float yaw = Mth.clamp(Mth.wrapDegrees(e.getYHeadRot() - e.yBodyRot), -30f, 30f) * Mth.DEG_TO_RAD;
                float pitch = Mth.clamp(e.getXRot(), -30f, 30f) * Mth.DEG_TO_RAD;
                c.rotate(EmpressRig.HEAD_BONE[0], pitch * look, yaw * look, 0f);
            }
        }

        for (int chain = 1; chain < 3; chain++) {
            long start = e.getChainDeathStart(chain);
            if (start < 0) continue;
            float dt = now - start;
            int dying = rig.lengthTicks(EmpressAnim.headDead(chain));
            if (dt < dying) {
                float fade = smooth(dt / 4f);
                c.deadFade[chain] = fade;
                c.deadLayers.add(new Layer(EmpressAnim.headDead(chain), Math.max(0f, dt) / 20f, fade, chain));
            } else {
                c.deadFade[chain] = 1f;
                c.deadLayers.add(new Layer(EmpressAnim.headDeadLoop(chain), (dt - dying) / 20f, 1f, chain));
            }
            EmpressAnim action = EmpressAnim.byId(e.getChainAction(chain));
            if (action == EmpressAnim.carcass(chain)) {
                float t = now - e.getChainStart(chain);
                if (t >= 0f && t <= rig.lengthTicks(action)) c.deadLayers.add(new Layer(action, t / 20f, 1f, chain));
            }
        }
        return c;
    }

    public static EmpressRig.Pose evaluate(Composition c) {
        EmpressRig rig = EmpressRig.get();
        EmpressRig.Pose pose = new EmpressRig.Pose();
        rig.reset(pose);
        for (Layer layer : c.layers) rig.apply(pose, layer.anim(), layer.seconds(), layer.weight(), layer.mask());
        pose.ry[0] += c.allYaw;
        for (int chain = 0; chain < 3; chain++) {
            pose.ry[EmpressRig.ROOT_BONE[chain]] += c.chainYaw[chain];
            pose.rx[EmpressRig.ROOT_BONE[chain]] += c.chainPitch[chain];
            float s = c.chainStretch[chain];
            if (s != 1f) {
                int[] segments = EmpressRig.SEGMENTS[chain];
                for (int i = 1; i < segments.length; i++) stretch(pose, segments[i], s);
                stretch(pose, EmpressRig.HEAD_BONE[chain], s);
            }
        }
        for (int bone = 0; bone < EmpressRig.COUNT; bone++) {
            pose.rx[bone] += c.procedural[bone][0];
            pose.ry[bone] += c.procedural[bone][1];
            pose.rz[bone] += c.procedural[bone][2];
        }
        for (int chain = 1; chain < 3; chain++) {
            if (c.deadFade[chain] > 0f) rig.relaxChain(pose, chain, 1f - c.deadFade[chain]);
        }
        for (Layer layer : c.deadLayers) rig.apply(pose, layer.anim(), layer.seconds(), layer.weight(), layer.mask());
        return pose;
    }

    private static void stretch(EmpressRig.Pose pose, int bone, float s) {
        pose.x[bone] *= s;
        pose.y[bone] *= s;
        pose.z[bone] *= s;
    }

    private static void weave(Composition c, int chain, float seconds, float amount, float frenzy, int seed) {
        int[] segments = EmpressRig.SEGMENTS[chain];
        int root = segments[0];
        int middle = segments[segments.length / 2];
        int top = segments[segments.length - 1];
        int head = EmpressRig.HEAD_BONE[chain];
        float phase = chain * 0.37f + (Math.floorMod(seed * 31, 97)) / 97f;
        float period = Mth.lerp(frenzy, 2.4f, 1.1f);
        float side = holdTriangle(seconds / period + phase);
        float forward = holdTriangle(2f * seconds / period + phase * 1.7f);
        float sideAmp = 0.16f + 0.10f * frenzy;
        float forwardAmp = 0.08f + 0.06f * frenzy;
        float snapSide = 0f;
        float snapForward = 0f;
        if (frenzy > 0f) {
            float cell = seconds / 0.55f + phase;
            int k = Mth.floor(cell);
            float blend = smooth((cell - k) * 0.55f / 0.12f);
            snapSide = Mth.lerp(blend, hash(seed, chain, k - 1, 0), hash(seed, chain, k, 0)) * 0.12f * frenzy;
            snapForward = Mth.lerp(blend, hash(seed, chain, k - 1, 1), hash(seed, chain, k, 1)) * 0.10f * frenzy;
        }
        float mirror = chain == 2 ? -1f : 1f;
        c.rotate(root, 0f, 0f, (side * sideAmp + snapSide) * amount * mirror);
        c.rotate(middle, (forward * forwardAmp + snapForward) * amount, 0f, 0f);
        c.rotate(top, 0f, 0f, -side * sideAmp * 0.6f * amount * mirror);
        c.rotate(head, -forward * forwardAmp * 0.5f * amount, side * 0.25f * amount * mirror, 0f);
    }

    public static float dodgeCurve(float dt) {
        if (dt < 4f) return smooth(dt / 4f);
        if (dt < 12f) return 1f;
        if (dt < DODGE_TICKS) return 1f - smooth((dt - 12f) / 10f);
        return 0f;
    }

    private static void dodge(Composition c, int chain, float dt, float direction, float live) {
        float d = dodgeCurve(dt) * live;
        if (d <= 0f) return;
        int[] segments = EmpressRig.SEGMENTS[chain];
        float side = Math.signum(direction == 0f ? 1f : direction);
        c.rotate(segments[0], 0f, 0f, side * 0.42f * d);
        c.rotate(segments[segments.length - 1], -0.15f * d, 0f, -side * 0.12f * d);
        c.rotate(EmpressRig.HEAD_BONE[chain], 0f, -side * 0.3f * d, 0f);
    }

    private static float holdTriangle(float u) {
        float t = u - Mth.floor(u);
        if (t < 0.35f) return -1f + 2f * smooth(t / 0.35f);
        if (t < 0.5f) return 1f;
        if (t < 0.85f) return 1f - 2f * smooth((t - 0.5f) / 0.35f);
        return -1f;
    }

    private static float hash(int seed, int chain, int k, int salt) {
        int h = seed * 73856093 ^ chain * 19349663 ^ k * 83492791 ^ salt * 2654435;
        h ^= h >>> 13;
        h *= 0x5bd1e995;
        h ^= h >>> 15;
        return ((h & 0xFFFF) / 32767.5f) - 1f;
    }

    public static float envelope(float dt, float length, float fade) {
        return smooth(Math.min(dt, length - dt) / fade);
    }

    public static float smooth(float t) {
        t = Mth.clamp(t, 0f, 1f);
        return t * t * (3f - 2f * t);
    }
}
