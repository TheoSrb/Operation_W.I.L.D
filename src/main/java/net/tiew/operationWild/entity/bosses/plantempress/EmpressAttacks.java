package net.tiew.operationWild.entity.bosses.plantempress;

import net.tiew.operationWild.particle.EmpressFx;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.bosses.PlantEmpressEntity;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class EmpressAttacks {

    private static final float AIM_LIMIT = 1.45f;
    private static final float AIM_TRACK = 0.12f;
    public static final float MAX_STRETCH = 2.3f;

    private EmpressAttacks() {
    }

    public abstract static class Attack {
        final EmpressBrain brain;
        final PlantEmpressEntity boss;
        final Set<LivingEntity> hit = new HashSet<>();
        final Set<String> marks = new HashSet<>();
        int t;
        boolean done;

        float facing(Vec3 at) {
            return (float) (Math.atan2(boss.getX() - at.x, -(boss.getZ() - at.z)) * Mth.RAD_TO_DEG);
        }

        void mark(ServerLevel level, String key, int kind, Vec3 at, double radius, int life, float yaw) {
            if (marks.add(key)) EmpressFx.decal(level, kind, ground(level, at), radius, life, yaw);
        }

        void markOn(ServerLevel level, String key, int kind, LivingEntity target, double radius, int life, int lockAfter) {
            if (marks.add(key)) {
                Vec3 at = ground(level, target.position());
                EmpressFx.tracked(level, kind, target, at, radius, life, lockAfter, facing(at));
            }
        }

        Attack(EmpressBrain brain) {
            this.brain = brain;
            this.boss = brain.boss;
        }

        abstract void tick(ServerLevel level);

        boolean usesChain(int chain) {
            return false;
        }

        boolean usesBody() {
            return false;
        }

        boolean locksBody() {
            return false;
        }

        boolean isThreat() {
            return true;
        }

        EmpressRig rig() {
            return EmpressRig.get();
        }

        long now() {
            return boss.now();
        }

        Vec3 ground(ServerLevel level, Vec3 p) {
            return EmpressDamage.onGround(level, p, boss.getY());
        }

        BlockState groundState(ServerLevel level, Vec3 p) {
            BlockState state = level.getBlockState(net.minecraft.core.BlockPos.containing(p).below());
            return state.isAir() ? boss.groundState(level) : state;
        }

        void sound(SoundEvent sound, Vec3 at, float volume, float pitch) {
            boss.level().playSound(null, at.x, at.y, at.z, sound, SoundSource.HOSTILE, volume, pitch);
        }

        float clampAim(float aim) {
            return Mth.clamp(aim, -AIM_LIMIT, AIM_LIMIT);
        }

        float track(float current, int bone, LivingEntity target) {
            float wanted = clampAim(boss.aimFor(bone, target.position()));
            return current + Mth.clamp(Mth.wrapDegrees((wanted - current) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD, -AIM_TRACK, AIM_TRACK);
        }

        static double distanceToBox(Vec3 p, AABB box) {
            double dx = Math.max(Math.max(box.minX - p.x, 0), p.x - box.maxX);
            double dy = Math.max(Math.max(box.minY - p.y, 0), p.y - box.maxY);
            double dz = Math.max(Math.max(box.minZ - p.z, 0), p.z - box.maxZ);
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }

        int strikeSpheres(ServerLevel level, List<Vec3> centers, double radius, float damage, boolean bite, boolean heavy,
                          Vec3 knockFrom, double horizontal, double vertical) {
            int count = 0;
            AABB area = null;
            for (Vec3 c : centers) {
                AABB box = new AABB(c, c).inflate(radius);
                area = area == null ? box : area.minmax(box);
            }
            if (area == null) return 0;
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, e -> EmpressDamage.canHit(e, boss))) {
                if (hit.contains(target)) continue;
                boolean inside = false;
                for (Vec3 c : centers) {
                    if (distanceToBox(c, target.getBoundingBox()) <= radius) {
                        inside = true;
                        break;
                    }
                }
                if (!inside) continue;
                DamageSource source = bite ? EmpressDamage.bite(boss, centers.get(centers.size() - 1)) : EmpressDamage.roots(boss, boss);
                if (EmpressDamage.strike(boss, target, source, damage, knockFrom, horizontal, vertical, heavy)) count++;
                hit.add(target);
            }
            return count;
        }

        void groundBurst(ServerLevel level, Vec3 at, int amount, double spread) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, groundState(level, at)), at.x, at.y + 0.3, at.z,
                    amount, spread, 0.3, spread, 0.25);
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, at.x, at.y + 0.2, at.z, amount / 10, spread * 0.5, 0.1, spread * 0.5, 0.01);
        }

        void chainTelegraph(ServerLevel level, Matrix4f[] predicted, int chain, boolean locked, double skullRadius) {
            net.minecraft.core.particles.ParticleOptions dust = locked ? EmpressDamage.DANGER : EmpressDamage.WARNING;
            int[] segments = EmpressRig.SEGMENTS[chain];
            Vec3 previous = null;
            for (int i = 1; i < segments.length; i++) {
                Vec3 p = ground(level, EmpressRig.origin(predicted, segments[i]));
                if (previous != null) EmpressDamage.line(level, previous, p, dust, 0.5);
                previous = p;
            }
            Vec3 skull = ground(level, rig().skullCenter(predicted, chain));
            if (previous != null) EmpressDamage.line(level, previous, skull, dust, 0.5);
            EmpressDamage.ring(level, skull, skullRadius, dust, 0.5);
            if (locked) level.sendParticles(ParticleTypes.CRIT, skull.x, skull.y + 0.2, skull.z, 4, skullRadius * 0.4, 0.05, skullRadius * 0.4, 0.1);
        }

        List<Vec3> chainSpheres(Matrix4f[] world, int chain) {
            List<Vec3> list = new ArrayList<>();
            int[] segments = EmpressRig.SEGMENTS[chain];
            for (int i = 1; i < segments.length; i++) {
                Vec3 a = EmpressRig.origin(world, segments[i]);
                Vec3 b = i + 1 < segments.length ? EmpressRig.origin(world, segments[i + 1]) : rig().skullCenter(world, chain);
                list.add(a);
                list.add(a.add(b).scale(0.5));
            }
            list.add(rig().skullCenter(world, chain));
            return list;
        }

        float[] solveReach(int chain, int impactIn, LivingEntity target, double heightOffset, @Nullable float[] previous) {
            return solveReach(chain, impactIn, target.position().add(0, heightOffset, 0), true, previous);
        }

        float[] solveReach(int chain, int impactIn, Vec3 goal, boolean adjustPitch, @Nullable float[] previous) {
            return solveReach(chain, impactIn, goal, adjustPitch, previous, false);
        }

        Vec3 reachPoint(Matrix4f[] world, int chain, boolean mouth) {
            return mouth ? rig().mouthPoint(world, chain) : rig().skullCenter(world, chain);
        }

        float[] solveReach(int chain, int impactIn, Vec3 goal, boolean adjustPitch, @Nullable float[] previous, boolean mouth) {
            int root = EmpressRig.ROOT_BONE[chain];
            float aim = previous != null ? previous[0] : clampAim(boss.aimFor(root, goal));
            float stretch = previous != null ? previous[1] : 1f;
            float pitch = previous != null && adjustPitch ? previous[2] : 0f;
            int ahead = Math.max(0, impactIn);
            Vec3 rootPos = EmpressRig.origin(boss.world(), root);
            double want = Math.sqrt(sqr(goal.x - rootPos.x) + sqr(goal.z - rootPos.z));
            double goalAngle = Math.atan2(goal.z - rootPos.z, goal.x - rootPos.x);
            for (int i = 0; i < 4; i++) {
                Vec3 skull = reachPoint(boss.previewReach(chain, aim, stretch, pitch, ahead), chain, mouth);
                Vec3 probe = reachPoint(boss.previewReach(chain, aim + 0.05f, stretch, pitch, ahead), chain, mouth);
                double angle = Math.atan2(skull.z - rootPos.z, skull.x - rootPos.x);
                double probeAngle = Math.atan2(probe.z - rootPos.z, probe.x - rootPos.x);
                double slope = Mth.wrapDegrees((probeAngle - angle) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD / 0.05;
                double error = Mth.wrapDegrees((goalAngle - angle) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
                if (Math.abs(slope) > 0.05) aim = clampAim(aim + (float) Mth.clamp(error / slope, -0.6, 0.6));
                double have = Math.sqrt(sqr(skull.x - rootPos.x) + sqr(skull.z - rootPos.z));
                double k = 1.2;
                stretch = Mth.clamp((float) (stretch * Math.max(0.4, want - k) / Math.max(0.4, have - k)), 1f, MAX_STRETCH);
                if (adjustPitch) {
                    double dy = goal.y - skull.y;
                    pitch = Mth.clamp(pitch - (float) (dy / Math.max(3.0, have)), -0.55f, 0.6f);
                }
            }
            return new float[]{aim, stretch, pitch};
        }

        static double sqr(double v) {
            return v * v;
        }

        void biteTelegraph(ServerLevel level, int chain, Matrix4f[] predicted, boolean locked, double radius) {
            net.minecraft.core.particles.ParticleOptions dust = locked ? EmpressDamage.DANGER : EmpressDamage.WARNING;
            Vec3 root = ground(level, EmpressRig.origin(predicted, EmpressRig.ROOT_BONE[chain]));
            Vec3 skull = ground(level, rig().skullCenter(predicted, chain));
            EmpressDamage.line(level, root, skull, dust, 0.55);
            EmpressDamage.ring(level, skull, radius, dust, 0.45);
            if (locked) level.sendParticles(ParticleTypes.CRIT, skull.x, skull.y + 0.3, skull.z, 5, radius * 0.4, 0.05, radius * 0.4, 0.1);
        }

        void tell(ServerLevel level, int chain) {
            Vec3 mouth = rig().mouthPoint(boss.world(), chain);
            Vec3 skull = rig().skullCenter(boss.world(), chain);
            level.sendParticles(ParticleTypes.END_ROD, mouth.x, mouth.y, mouth.z, 2, 0.35, 0.3, 0.35, 0.01);
            level.sendParticles(EmpressDamage.WARNING, skull.x, skull.y + 0.6, skull.z, 3, 0.7, 0.3, 0.7, 0);
            if (t % 4 == 0) level.sendParticles(ParticleTypes.FALLING_SPORE_BLOSSOM, skull.x, skull.y, skull.z, 3, 0.6, 0.3, 0.6, 0);
        }

        void mouthGather(ServerLevel level, int chain, int count) {

            Vec3 mouth = rig().mouthPoint(boss.world(), chain);
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, mouth.x, mouth.y, mouth.z, count, 0.8, 0.8, 0.8, 0.02);
            level.sendParticles(EmpressDamage.TOXIC, mouth.x, mouth.y, mouth.z, count / 2, 0.5, 0.5, 0.5, 0);
        }
    }

    public static class Lunge extends Attack {
        final int chain;
        final LivingEntity target;
        final int delay;
        float aim;
        float[] reach;
        int extra;

        public Lunge(EmpressBrain brain, int chain, LivingEntity target, int delay) {
            super(brain);
            this.chain = chain;
            this.target = target;
            this.delay = delay;
        }

        @Override
        void tick(ServerLevel level) {
            int r = t - delay;
            if (r < 0) return;
            int s = r - EmpressTimings.HEAD_TELL;
            int length = rig().lengthTicks(EmpressAnim.lunge(chain));
            if (r == 0) {
                if (boss.isChainDead(chain) || !target.isAlive()) {
                    done = true;
                    return;
                }
                boss.setChainAction(chain, EmpressAnim.lunge(chain).ordinal(), clampAim(boss.aimFor(EmpressRig.ROOT_BONE[chain], target.position())));
                boss.delayChain(chain, EmpressTimings.HEAD_TELL);
                reach = solveReach(chain, EmpressTimings.LUNGE_IMPACT - s, target, 0.4, null);
                aim = reach[0];
                boss.setChainReach(chain, reach[0], reach[1], reach[2]);
                brain.reserve(target, now() + EmpressTimings.LUNGE_IMPACT + EmpressTimings.HEAD_TELL);
                brain.markTargeted(target);
                markOn(level, "slam", EmpressFx.SLAM, target, 2.3, EmpressTimings.HEAD_TELL + EmpressTimings.LUNGE_IMPACT,
                        EmpressTimings.HEAD_TELL + EmpressTimings.LUNGE_LOCK);
                Vec3 head = rig().skullCenter(boss.world(), chain);
                sound(OWSounds.PLANT_EMPRESS_LUNGE.get(), head, 2.2f, chain == 1 ? 0.95f : 1.05f);
            }
            if (r > 0 && s < EmpressTimings.LUNGE_LOCK && target.isAlive()) {
                reach = solveReach(chain, EmpressTimings.LUNGE_IMPACT - s, target, 0.4, reach);
                boss.setChainReach(chain, reach[0], reach[1], reach[2]);
            }
            if (s <= EmpressTimings.LUNGE_IMPACT && r % 2 == 0) {
                Matrix4f[] predicted = boss.predictWorld(EmpressTimings.LUNGE_IMPACT - s);
                chainTelegraph(level, predicted, chain, s >= EmpressTimings.LUNGE_LOCK, 1.9);
            }
            if (s < 0) tell(level, chain);
            if (r > 2 && s < EmpressTimings.LUNGE_IMPACT && r % 3 == 0) mouthGather(level, chain, 4);
            if (s == EmpressTimings.LUNGE_IMPACT) {
                Matrix4f[] world = boss.world();
                List<Vec3> spheres = chainSpheres(world, chain);
                strikeSpheres(level, spheres, 1.6, EmpressBalance.DAMAGE_SLAM, true, true, boss.position(), 0.9, 0.45);
                Vec3 skull = rig().skullCenter(world, chain);
                Vec3 impact = ground(level, skull);
                groundBurst(level, impact, 70, 1.2);
                for (Vec3 p : spheres) groundBurst(level, ground(level, p), 10, 0.5);
                level.sendParticles(ParticleTypes.EXPLOSION, impact.x, impact.y + 0.5, impact.z, 2, 0.4, 0.2, 0.4, 0);
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, impact.x, impact.y + 1, impact.z, 30, 1.5, 0.8, 1.5, 0.05);
                EmpressDamage.ring(level, impact, 2.4, EmpressDamage.WARNING, 0.4);
                sound(OWSounds.PLANT_EMPRESS_SLAM.get(), impact, 2.5f, 0.9f);
                sound(OWSounds.PLANT_EMPRESS_BITE.get(), impact, 2.0f, 0.8f);
                boss.broadcastEvent(PlantEmpressEntity.EVENT_SLAM);
            }
            if (s == EmpressTimings.LUNGE_MISS_HOLD_AT && hit.isEmpty() && boss.chainAnim(chain) == EmpressAnim.lunge(chain)) {
                if (boss.collapse(chain)) return;
                boss.delayChain(chain, EmpressTimings.LUNGE_MISS_HOLD);
                extra = EmpressTimings.LUNGE_MISS_HOLD;
            }
            if (s > EmpressTimings.LUNGE_IMPACT && s < EmpressTimings.LUNGE_PLANT_END + extra && s % 4 == 0) {
                Vec3 skull = rig().skullCenter(boss.world(), chain);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, groundState(level, skull)), skull.x, skull.y - 0.6, skull.z,
                        6, 0.8, 0.2, 0.8, 0.1);
                level.sendParticles(ParticleTypes.FALLING_SPORE_BLOSSOM, skull.x, skull.y + 0.8, skull.z, 2, 0.6, 0.2, 0.6, 0);
            }
            if (s == EmpressTimings.LUNGE_PLANT_END + extra) {
                Vec3 skull = rig().skullCenter(boss.world(), chain);
                groundBurst(level, ground(level, skull), 30, 0.8);
                sound(OWSounds.PLANT_EMPRESS_ROOTS.get(), skull, 1.6f, 1.2f);
            }
            if (s >= length + extra) done = true;
        }

        @Override
        boolean locksBody() {
            return true;
        }

        @Override
        boolean usesChain(int c) {
            return c == chain;
        }

        @Override
        boolean isThreat() {
            return t - delay - EmpressTimings.HEAD_TELL <= EmpressTimings.LUNGE_IMPACT;
        }
    }

    public static class Scissors extends Attack {
        final Lunge first;
        final Lunge second;

        public Scissors(EmpressBrain brain, LivingEntity target) {
            super(brain);
            this.first = new Lunge(brain, 1, target, 0);
            this.second = new Lunge(brain, 2, target, 8);
        }

        @Override
        void tick(ServerLevel level) {
            if (!first.done) {
                first.tick(level);
                first.t++;
            }
            if (!second.done) {
                second.tick(level);
                second.t++;
            }
            done = first.done && second.done;
        }

        @Override
        boolean usesChain(int c) {
            return c == 1 || c == 2;
        }

        @Override
        boolean isThreat() {
            return first.isThreat() || second.isThreat();
        }
    }

    public static class Spit extends Attack {
        final LivingEntity target;
        final boolean seeds;
        float aim;

        public Spit(EmpressBrain brain, LivingEntity target, boolean seeds) {
            super(brain);
            this.target = target;
            this.seeds = seeds;
        }

        @Override
        void tick(ServerLevel level) {
            int bone = EmpressRig.ROOT_BONE[0];
            if (t == 0) {
                aim = clampAim(boss.aimFor(bone, target.position()) * 0.6f);
                boss.setChainAction(0, EmpressAnim.SPORE_SPIT.ordinal(), aim);
                brain.markTargeted(target);
                sound(OWSounds.PLANT_EMPRESS_SPIT_CHARGE.get(), rig().skullCenter(boss.world(), 0), 2.5f, seeds ? 1.15f : 0.9f);
            }
            if (t > 6 && t < EmpressTimings.SPIT_RELEASE && t % 2 == 0) mouthGather(level, 0, seeds ? 4 : 7);
            if (t == EmpressTimings.SPIT_RELEASE) {
                Vec3 mouth = rig().mouthPoint(boss.world(), 0);
                List<Vec3> spots = seeds ? seedSpots(level) : sporeSpots(level);
                for (Vec3 spot : spots) {
                    EmpressSeedProjectile seed = EmpressSeedProjectile.launch(level, boss,
                            seeds ? EmpressSeedProjectile.THORN : EmpressSeedProjectile.SPORE, mouth, spot, seeds ? 22 : 20);
                    brain.spawn(level, seed);
                }
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, mouth.x, mouth.y, mouth.z, 50, 0.8, 0.8, 0.8, 0.15);
                level.sendParticles(ParticleTypes.SNEEZE, mouth.x, mouth.y, mouth.z, 20, 0.5, 0.5, 0.5, 0.2);
                level.sendParticles(EmpressDamage.TOXIC, mouth.x, mouth.y, mouth.z, 30, 0.6, 0.6, 0.6, 0);
                sound(OWSounds.PLANT_EMPRESS_SPIT.get(), mouth, 3f, seeds ? 1.1f : 0.85f);
            }
            if (t >= rig().lengthTicks(EmpressAnim.SPORE_SPIT)) done = true;
        }

        private List<Vec3> sporeSpots(ServerLevel level) {
            List<Vec3> spots = new ArrayList<>();
            spots.add(ground(level, target.position()));
            List<Player> others = boss.fighters(PlantEmpressEntity.LEASH_RADIUS);
            others.remove(target);
            Vec3 toTarget = target.position().subtract(boss.position());
            Vec3 side = new Vec3(-toTarget.z, 0, toTarget.x);
            side = side.lengthSqr() < 1e-4 ? new Vec3(1, 0, 0) : side.normalize();
            for (int i = 0; i < 2; i++) {
                if (i < others.size()) {
                    spots.add(ground(level, others.get(i).position()));
                } else {
                    double offset = (i == 0 ? 1 : -1) * (3.6 + boss.getRandom().nextDouble());
                    spots.add(ground(level, target.position().add(side.scale(offset))));
                }
            }
            return spots;
        }

        private List<Vec3> seedSpots(ServerLevel level) {
            List<Vec3> spots = new ArrayList<>();
            List<LivingEntity> fighters = new ArrayList<>(boss.fighters(PlantEmpressEntity.LEASH_RADIUS));
            if (fighters.isEmpty()) fighters.add(target);
            int count = 7;
            for (int i = 0; i < count; i++) {
                LivingEntity around = fighters.get(i % fighters.size());
                double angle = boss.getRandom().nextDouble() * Math.PI * 2;
                double radius = 2.5 + boss.getRandom().nextDouble() * 3.5;
                spots.add(ground(level, around.position().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius)));
            }
            return spots;
        }

        @Override
        boolean usesChain(int c) {
            return c == 0;
        }

        @Override
        boolean isThreat() {
            return t <= EmpressTimings.SPIT_RELEASE + 22;
        }
    }

    public static class RootWhip extends Attack {
        private double previous = EmpressTimings.WHIP_RADIUS_START - 0.5;

        public RootWhip(EmpressBrain brain) {
            super(brain);
        }

        @Override
        void tick(ServerLevel level) {
            Vec3 center = boss.position();
            if (t == 0) {
                boss.setBodyAction(EmpressAnim.ROOT_WHIP, 0f);
                boss.setStemAction(EmpressAnim.STEMS_LASH);
                sound(OWSounds.PLANT_EMPRESS_WHIP.get(), center, 3f, 0.7f);
            }
            if (t == 6) mark(level, "whip", EmpressFx.ROOTS, center, EmpressTimings.WHIP_RADIUS_END, EmpressTimings.WHIP_WAVE_START - 6, 0f);
            if (t >= 6 && t < EmpressTimings.WHIP_WAVE_START && t % 2 == 0) {
                EmpressDamage.ring(level, center, EmpressTimings.WHIP_RADIUS_START, EmpressDamage.EARTH, 0.4);
                EmpressDamage.ring(level, center, EmpressTimings.WHIP_RADIUS_END, EmpressDamage.EARTH, 1.4);
                for (int i = 0; i < 6; i++) {
                    double angle = boss.getRandom().nextDouble() * Math.PI * 2;
                    double r = EmpressTimings.WHIP_RADIUS_START + boss.getRandom().nextDouble() * 1.5;
                    Vec3 p = center.add(Math.cos(angle) * r, 0, Math.sin(angle) * r);
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, groundState(level, p)), p.x, p.y + 0.1, p.z, 3, 0.2, 0, 0.2, 0.1);
                }
                if (t % 6 == 0) boss.broadcastEvent(PlantEmpressEntity.EVENT_TREMOR);
            }
            if (t >= EmpressTimings.WHIP_WAVE_START && t <= EmpressTimings.WHIP_WAVE_END) {
                float f = (t - EmpressTimings.WHIP_WAVE_START) / (float) (EmpressTimings.WHIP_WAVE_END - EmpressTimings.WHIP_WAVE_START);
                double radius = Mth.lerp(f, EmpressTimings.WHIP_RADIUS_START, EmpressTimings.WHIP_RADIUS_END);
                if (t == EmpressTimings.WHIP_WAVE_START) {
                    EmpressFx.wave(level, center, EmpressTimings.WHIP_RADIUS_END, EmpressFx.EARTH, EmpressTimings.WHIP_WAVE_END - EmpressTimings.WHIP_WAVE_START + 2);
                    sound(OWSounds.PLANT_EMPRESS_ROOTS.get(), center, 3f, 0.6f);
                    sound(OWSounds.PLANT_EMPRESS_WHIP.get(), center, 3f, 1.1f);
                    boss.broadcastEvent(PlantEmpressEntity.EVENT_SLAM);
                }
                if (t % 3 == 0) sound(OWSounds.PLANT_EMPRESS_ROOTS.get(), center, 2.2f, 0.7f + f * 0.5f);
                int spikes = 4 + (int) (radius * 0.6);
                for (int i = 0; i < spikes; i++) {
                    double angle = boss.getRandom().nextDouble() * Math.PI * 2;
                    double x = center.x + Math.cos(angle) * radius;
                    double z = center.z + Math.sin(angle) * radius;
                    double y = EmpressDamage.groundY(level, x, z, boss.getY());
                    brain.spawn(level, EmpressRootSpike.create(level, boss, x, y, z, 1, 0f).quiet());
                }
                waveRing(level, center, radius, EmpressBalance.DAMAGE_WHIP, 0.75, 0.5);
                previous = radius;
            }
            if (t >= rig().lengthTicks(EmpressAnim.ROOT_WHIP)) done = true;
        }

        void waveRing(ServerLevel level, Vec3 center, double radius, float damage, double up, double out) {
            int count = (int) (radius * 7);
            for (int i = 0; i < count; i++) {
                double angle = Math.PI * 2 * i / count;
                Vec3 p = new Vec3(center.x + Math.cos(angle) * radius, 0, center.z + Math.sin(angle) * radius);
                double y = EmpressDamage.groundY(level, p.x, p.z, boss.getY());
                if (i % 2 == 0) level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, groundState(level, new Vec3(p.x, y, p.z))),
                        p.x, y + 0.2, p.z, 2, 0.15, 0.25, 0.15, 0.2);
                if (i % 3 == 0) level.sendParticles(EmpressDamage.DANGER, p.x, y + 0.4, p.z, 1, 0.05, 0.15, 0.05, 0);
            }
            AABB box = new AABB(center, center).inflate(radius + 1, 8, radius + 1);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, e -> EmpressDamage.canHit(e, boss))) {
                if (hit.contains(target)) continue;
                double d = Math.sqrt(target.distanceToSqr(center.x, target.getY(), center.z));
                if (d < previous - 0.8 || d > radius + 0.8) continue;
                if (!EmpressDamage.isGrounded(level, target, 0.9)) continue;
                hit.add(target);
                EmpressDamage.strike(boss, target, EmpressDamage.roots(boss, boss), damage, center, out, up, false);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, groundState(level, target.position())),
                        target.getX(), target.getY() + 0.3, target.getZ(), 25, 0.4, 0.3, 0.4, 0.25);
            }
        }

        @Override
        boolean usesBody() {
            return true;
        }
    }

    public static class TripleBite extends Attack {
        final LivingEntity target;
        float aim;

        public TripleBite(EmpressBrain brain, LivingEntity target) {
            super(brain);
            this.target = target;
        }

        @Override
        void tick(ServerLevel level) {
            if (t == 0) {
                aim = clampAim(boss.aimFor(0, target.position()));
                boss.setBodyAction(EmpressAnim.TRIPLE_BITE, aim);
                brain.markTargeted(target);
                sound(OWSounds.PLANT_EMPRESS_LUNGE.get(), boss.position().add(0, 6, 0), 2.5f, 0.8f);
            }
            if (t > 0 && t < EmpressTimings.TRIPLE_LOCK && target.isAlive()) {
                aim = track(aim, 0, target);
                boss.setBodyAim(aim);
            }
            for (int chain = 0; chain < 3; chain++) {
                if (boss.isChainDead(chain)) continue;
                int impact = EmpressTimings.TRIPLE_IMPACT[chain];
                if (t >= impact - 12 && t < impact && t % 2 == 0) {
                    Matrix4f[] predicted = boss.predictWorld(impact - t);
                    Vec3 skull = ground(level, rig().skullCenter(predicted, chain));
                    if (t >= EmpressTimings.TRIPLE_LOCK) mark(level, "triple" + chain, EmpressFx.BITE, skull, 2.2, impact - t, facing(skull));
                    EmpressDamage.disc(level, skull, 2.1, impact - t <= 6 ? EmpressDamage.DANGER : EmpressDamage.WARNING);
                }
                if (t == impact) {
                    hit.clear();
                    Vec3 skull = rig().skullCenter(boss.world(), chain);
                    Vec3 floor = ground(level, skull);
                    List<Vec3> column = new ArrayList<>();
                    for (int i = 0; i <= 4; i++) column.add(floor.add(skull.subtract(floor).scale(i / 4.0)));
                    strikeSpheres(level, column, 2.0, EmpressBalance.DAMAGE_TRIPLE, true, false, boss.position(), 0.8, 0.35);
                    groundBurst(level, floor, 40, 1.0);
                    level.sendParticles(ParticleTypes.SWEEP_ATTACK, skull.x, skull.y, skull.z, 2, 0.4, 0.2, 0.4, 0);
                    sound(OWSounds.PLANT_EMPRESS_BITE.get(), skull, 2.2f, 0.8f + chain * 0.12f);
                    boss.broadcastEvent(PlantEmpressEntity.EVENT_TREMOR);
                }
            }
            if (t >= rig().lengthTicks(EmpressAnim.TRIPLE_BITE)) done = true;
        }

        @Override
        boolean usesBody() {
            return true;
        }

        @Override
        boolean locksBody() {
            return true;
        }
    }

    public static class Snare extends Attack {
        final LivingEntity target;

        public Snare(EmpressBrain brain, LivingEntity target) {
            super(brain);
            this.target = target;
        }

        @Override
        void tick(ServerLevel level) {
            if (t == 0) {
                boss.setChainAction(0, EmpressAnim.VINE_CAST.ordinal(), clampAim(boss.aimFor(EmpressRig.ROOT_BONE[0], target.position()) * 0.5f));
                boss.setStemAction(EmpressAnim.STEMS_DIG);
                brain.markTargeted(target);
                sound(OWSounds.PLANT_EMPRESS_SNARE.get(), boss.position().add(0, 8, 0), 2.5f, 0.7f);
            }
            if (t > 2 && t < EmpressTimings.CAST_RELEASE) mouthGather(level, 0, 5);
            if (t == EmpressTimings.CAST_RELEASE) {
                List<LivingEntity> victims = new ArrayList<>();
                victims.add(target);
                for (Player player : boss.fighters(26)) {
                    if (player != target && victims.size() < 3) victims.add(player);
                }
                for (LivingEntity victim : victims) {
                    Vec3 at = ground(level, victim.position());
                    brain.spawn(level, EmpressVineSnare.create(level, boss, at));
                }
                boss.broadcastEvent(PlantEmpressEntity.EVENT_TREMOR);
            }
            if (t >= rig().lengthTicks(EmpressAnim.VINE_CAST)) done = true;
        }

        @Override
        boolean usesChain(int c) {
            return c == 0;
        }

        @Override
        boolean isThreat() {
            return t <= EmpressTimings.CAST_RELEASE + EmpressVineSnare.WARN_TICKS;
        }
    }

    public static class Plunge extends Attack {
        final LivingEntity target;
        float aim;

        public Plunge(EmpressBrain brain, LivingEntity target) {
            super(brain);
            this.target = target;
        }

        @Override
        void tick(ServerLevel level) {
            int r = t;
            int t = r - EmpressTimings.HEAD_TELL;
            if (r == 0) {
                aim = clampAim(boss.aimFor(0, target.position()));
                boss.setBodyAction(EmpressAnim.PLUNGE, aim);
                boss.delayBody(EmpressTimings.HEAD_TELL);
                brain.reserve(target, now() + EmpressTimings.PLUNGE_IMPACT + EmpressTimings.HEAD_TELL);
                brain.markTargeted(target);
                markOn(level, "plunge", EmpressFx.SLAM, target, 2.9, EmpressTimings.HEAD_TELL + EmpressTimings.PLUNGE_IMPACT,
                        EmpressTimings.HEAD_TELL + EmpressTimings.PLUNGE_LOCK);
                sound(OWSounds.PLANT_EMPRESS_LUNGE.get(), boss.position().add(0, 9, 0), 3f, 0.7f);
            }
            if (r > 0 && t < EmpressTimings.PLUNGE_LOCK && target.isAlive()) {
                aim = track(aim, 0, target);
                boss.setBodyAim(aim);
            }
            if (t <= EmpressTimings.PLUNGE_IMPACT && r % 2 == 0) {
                chainTelegraph(level, boss.predictWorld(EmpressTimings.PLUNGE_IMPACT - t), 0, t >= EmpressTimings.PLUNGE_LOCK, 2.4);
            }
            if (t < 0) tell(level, 0);
            if (r > 4 && t < EmpressTimings.PLUNGE_IMPACT && r % 3 == 0) mouthGather(level, 0, 6);
            if (t == EmpressTimings.PLUNGE_IMPACT) {
                Matrix4f[] world = boss.world();
                strikeSpheres(level, chainSpheres(world, 0), 1.8, EmpressBalance.DAMAGE_PLUNGE, true, true, boss.position(), 1.0, 0.5);
                Vec3 impact = ground(level, rig().skullCenter(world, 0));
                groundBurst(level, impact, 110, 1.6);
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, impact.x, impact.y + 0.5, impact.z, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, impact.x, impact.y + 1, impact.z, 60, 2, 1, 2, 0.08);
                sound(OWSounds.PLANT_EMPRESS_SLAM.get(), impact, 4f, 0.7f);
                sound(OWSounds.PLANT_EMPRESS_BITE.get(), impact, 3f, 0.6f);
                boss.broadcastEvent(PlantEmpressEntity.EVENT_SLAM);
                brain.add(new RootEruption(brain, boss.fighters(PlantEmpressEntity.LEASH_RADIUS), impact));
            }
            if (t > EmpressTimings.PLUNGE_IMPACT && t < EmpressTimings.PLUNGE_PLANT_END && t % 3 == 0) {
                Vec3 skull = rig().skullCenter(boss.world(), 0);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, groundState(level, skull)), skull.x, skull.y - 0.8, skull.z,
                        8, 1, 0.2, 1, 0.1);
            }
            if (t >= rig().lengthTicks(EmpressAnim.PLUNGE)) done = true;
        }

        @Override
        boolean usesBody() {
            return true;
        }

        @Override
        boolean locksBody() {
            return true;
        }

        @Override
        boolean isThreat() {
            return t <= EmpressTimings.PLUNGE_IMPACT;
        }
    }

    public static class Earthquake extends Attack {
        final LivingEntity target;
        Vec3 center;
        final double[] previous = {0, 0, 0};
        final List<Set<LivingEntity>> waveHits = List.of(new HashSet<>(), new HashSet<>(), new HashSet<>());

        public Earthquake(EmpressBrain brain, LivingEntity target) {
            super(brain);
            this.target = target;
        }

        @Override
        void tick(ServerLevel level) {
            int slam = EmpressTimings.QUAKE_SLAM;
            if (t == 0) {
                boss.setBodyAction(EmpressAnim.EARTHQUAKE, clampAim(boss.aimFor(0, target.position())));
                sound(OWSounds.PLANT_EMPRESS_QUAKE_RISE.get(), boss.position().add(0, 6, 0), 3.5f, 0.6f);
            }
            if (t == slam - 10) boss.setStemAction(EmpressAnim.STEMS_DIG);
            if (t > 10 && t < slam && t % 5 == 0) {
                boss.broadcastEvent(PlantEmpressEntity.EVENT_TREMOR);
                sound(OWSounds.PLANT_EMPRESS_RUMBLE.get(), boss.position(), 2f, 0.5f + t / 100f);
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, boss.getX(), boss.getY() + 11, boss.getZ(), 20, 1.5, 1, 1.5, 0.05);
            }
            if (t >= 14 && t < slam && t % 2 == 0) {
                Vec3 predicted = ground(level, rig().skullCenter(boss.predictWorld(slam - t), 0));
                if (t == 14) mark(level, "quake", EmpressFx.QUAKE, predicted, 3.4, slam - 14, 0f);
                EmpressDamage.disc(level, predicted, 3.0, slam - t <= 10 ? EmpressDamage.DANGER : EmpressDamage.WARNING);
                float pulse = (t % 8) / 8f;
                for (int k = 0; k < 3; k++) EmpressDamage.ring(level, predicted, 4 + k * 6 + pulse * 2, EmpressDamage.EARTH, 1.2);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, groundState(level, predicted)), predicted.x, predicted.y + 0.1,
                        predicted.z, 12, 4, 0.05, 4, 0.08);
            }
            if (t == slam) {
                center = ground(level, rig().skullCenter(boss.world(), 0));
                hit.clear();
                strikeSpheres(level, List.of(center, center.add(0, 1.5, 0)), 3.0, EmpressBalance.DAMAGE_QUAKE_SLAM, false, true, center, 1.0, 0.6);
                groundBurst(level, center, 160, 2.0);
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y + 0.5, center.z, 1, 0, 0, 0, 0);
                sound(OWSounds.PLANT_EMPRESS_QUAKE.get(), center, 5f, 0.6f);
                boss.broadcastEvent(PlantEmpressEntity.EVENT_SLAM);
            }
            if (center != null) {
                for (int k = 0; k < 3; k++) {
                    int start = slam + k * EmpressTimings.QUAKE_WAVE_GAP;
                    if (t < start) continue;
                    double radius = 1.5 + (t - start) * 0.55;
                    if (radius > 22) continue;
                    if (t == start) EmpressFx.wave(level, center, 22, EmpressFx.EARTH, (int) Math.ceil((22 - 1.5) / 0.55));
                    if (t == start && k > 0) {
                        sound(OWSounds.PLANT_EMPRESS_QUAKE.get(), center, 3f, 0.7f + k * 0.1f);
                        boss.broadcastEvent(PlantEmpressEntity.EVENT_TREMOR);
                    }
                    wave(level, k, radius);
                    previous[k] = radius;
                }
            }
            if (t > slam + 2 * EmpressTimings.QUAKE_WAVE_GAP + 42) done = true;
        }

        private void wave(ServerLevel level, int k, double radius) {
            int count = (int) (radius * 6);
            for (int i = 0; i < count; i++) {
                double angle = Math.PI * 2 * i / count;
                double x = center.x + Math.cos(angle) * radius;
                double z = center.z + Math.sin(angle) * radius;
                double y = EmpressDamage.groundY(level, x, z, boss.getY());
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, groundState(level, new Vec3(x, y, z))),
                        x, y + 0.5, z, 3, 0.1, 0.45, 0.1, 0.3);
                if (i % 2 == 0) level.sendParticles(EmpressDamage.EARTH, x, y + 0.9, z, 1, 0.05, 0.3, 0.05, 0);
                if (i % 3 == 0) {
                    double lead = radius + 1.1;
                    level.sendParticles(EmpressDamage.DANGER, center.x + Math.cos(angle) * lead, y + 0.2, center.z + Math.sin(angle) * lead, 1, 0, 0, 0, 0);
                }
            }
            AABB box = new AABB(center, center).inflate(radius + 1, 8, radius + 1);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, e -> EmpressDamage.canHit(e, boss))) {
                Set<LivingEntity> already = waveHits.get(k);
                if (already.contains(target)) continue;
                double d = Math.sqrt(target.distanceToSqr(center.x, target.getY(), center.z));
                if (d < previous[k] - 0.8 || d > radius + 0.8) continue;
                if (!EmpressDamage.isGrounded(level, target, 0.7)) continue;
                already.add(target);
                EmpressDamage.strike(boss, target, EmpressDamage.roots(boss, boss), EmpressBalance.DAMAGE_QUAKE_WAVE, center, 0.5, 0.6, false);
            }
        }

        @Override
        boolean usesBody() {
            return t < rig().lengthTicks(EmpressAnim.EARTHQUAKE);
        }

        @Override
        boolean locksBody() {
            return usesBody();
        }
    }

    public static class SporeRain extends Attack {
        final List<Vec3> spots = new ArrayList<>();
        final List<Player> fighters;

        public SporeRain(EmpressBrain brain, List<Player> fighters) {
            super(brain);
            this.fighters = new ArrayList<>(fighters);
        }

        @Override
        void tick(ServerLevel level) {
            if (t == 0) {
                for (int i = 0; i < Math.min(4, fighters.size()); i++) {
                    Player player = fighters.get(i);
                    spots.add(offset(level, player.position(), 0, 1.5));
                    spots.add(offset(level, player.position(), 3, 6));
                }
                for (int i = 0; i < 4; i++) spots.add(offset(level, boss.position(), 5, 20));
                Vec3 head = rig().mouthPoint(boss.world(), 0);
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, head.x, head.y + 1, head.z, 80, 1, 3, 1, 0.2);
                level.sendParticles(EmpressDamage.TOXIC, head.x, head.y + 2, head.z, 40, 1, 3, 1, 0);
                sound(OWSounds.PLANT_EMPRESS_SPIT.get(), head, 3.5f, 0.6f);
            }
            int index = t / 2;
            if (t % 2 == 0 && index < spots.size()) {
                Vec3 spot = spots.get(index);
                Vec3 from = spot.add(boss.getRandom().nextDouble() * 4 - 2, 16, boss.getRandom().nextDouble() * 4 - 2);
                brain.spawn(level, EmpressSeedProjectile.launch(level, boss, EmpressSeedProjectile.SPORE, from, spot, 26));
            }
            if (index > spots.size() + 1) done = true;
        }

        private Vec3 offset(ServerLevel level, Vec3 base, double min, double max) {
            double angle = boss.getRandom().nextDouble() * Math.PI * 2;
            double r = min + boss.getRandom().nextDouble() * (max - min);
            Vec3 p = base.add(Math.cos(angle) * r, 0, Math.sin(angle) * r);
            float wall = boss.wallRadius(0f);
            if (wall > 0f) {
                Vec3 rel = new Vec3(p.x - boss.getX(), 0, p.z - boss.getZ());
                if (rel.length() > wall - 1.5) p = boss.position().add(rel.normalize().scale(wall - 1.5));
            }
            return ground(level, p);
        }

        @Override
        boolean isThreat() {
            return t < spots.size() * 2 + 28;
        }
    }

    public static class RootEruption extends Attack {
        final List<Player> fighters;
        final Vec3 origin;
        final List<List<Vec3>> lines = new ArrayList<>();
        boolean cast;

        public RootEruption(EmpressBrain brain, List<Player> fighters, @Nullable Vec3 origin) {
            super(brain);
            this.fighters = new ArrayList<>(fighters);
            this.origin = origin;
        }

        @Override
        void tick(ServerLevel level) {
            if (t == 0) {
                Vec3 from = origin != null ? origin : boss.position();
                fighters.sort((a, b) -> Double.compare(a.distanceToSqr(from), b.distanceToSqr(from)));
                float wall = boss.wallRadius(0f);
                double reach = wall > 0f ? wall - 1 : 22;
                for (int i = 0; i < Math.min(3, fighters.size()); i++) {
                    Vec3 dir = new Vec3(fighters.get(i).getX() - from.x, 0, fighters.get(i).getZ() - from.z);
                    if (dir.lengthSqr() < 1e-3) continue;
                    dir = dir.normalize();
                    List<Vec3> line = new ArrayList<>();
                    for (double d = origin != null ? 1.6 : 2.6; d < reach; d += 1.3) {
                        Vec3 p = from.add(dir.scale(d));
                        if (new Vec3(p.x - boss.getX(), 0, p.z - boss.getZ()).length() > reach) break;
                        line.add(ground(level, p));
                    }
                    lines.add(line);
                    for (int k = 0; k < line.size(); k += 2) {
                        Vec3 p = line.get(k);
                        EmpressFx.decal(level, EmpressFx.CHEVRON, p, 0.75, k + 4, (float) (Math.atan2(dir.x, -dir.z) * Mth.RAD_TO_DEG));
                    }
                }
                if (origin == null && brain.chainFree(0)) {
                    cast = true;
                    boss.setChainAction(0, EmpressAnim.VINE_CAST.ordinal(), 0f);
                }
                boss.setStemAction(EmpressAnim.STEMS_DIG);
                sound(OWSounds.PLANT_EMPRESS_ROOTS.get(), from, 3f, 0.5f);
                boss.broadcastEvent(PlantEmpressEntity.EVENT_TREMOR);
            }
            int index = t - 3;
            boolean any = false;
            for (List<Vec3> line : lines) {
                if (index >= 0 && index < line.size()) {
                    Vec3 p = line.get(index);
                    brain.spawn(level, EmpressRootSpike.create(level, boss, p.x, p.y, p.z, 14, EmpressBalance.DAMAGE_ROOT_SPIKE));
                    if (index % 3 == 0) sound(OWSounds.PLANT_EMPRESS_ROOTS.get(), p, 1.2f, 0.8f + index * 0.02f);
                }
                if (index < line.size()) any = true;
            }
            if (!any && index > 20) done = true;
        }

        @Override
        boolean usesChain(int c) {
            return cast && c == 0 && t < rig().lengthTicks(EmpressAnim.VINE_CAST);
        }
    }

    public static class Bite extends Attack {
        final int chain;
        final LivingEntity target;
        float[] reach;

        public Bite(EmpressBrain brain, int chain, LivingEntity target) {
            super(brain);
            this.chain = chain;
            this.target = target;
        }

        @Override
        void tick(ServerLevel level) {
            int impact = EmpressTimings.biteImpact(chain);
            int lock = impact - 6;
            EmpressAnim anim = EmpressAnim.bite(chain);
            int r = t;
            int t = r - EmpressTimings.HEAD_TELL;
            if (r == 0) {
                if (boss.isChainDead(chain) || !target.isAlive()) {
                    done = true;
                    return;
                }
                boss.setChainAction(chain, anim.ordinal(), clampAim(boss.aimFor(EmpressRig.ROOT_BONE[chain], target.position())));
                boss.delayChain(chain, EmpressTimings.HEAD_TELL);
                reach = solveReach(chain, impact - t, target, target.getBbHeight() * 0.5, null);
                boss.setChainReach(chain, reach[0], reach[1], reach[2]);
                brain.reserve(target, now() + impact + EmpressTimings.HEAD_TELL);
                brain.markTargeted(target);
                markOn(level, "bite", EmpressFx.BITE, target, chain == 0 ? 2.3 : 2.0, EmpressTimings.HEAD_TELL + impact, EmpressTimings.HEAD_TELL + lock);
                sound(OWSounds.PLANT_EMPRESS_LUNGE.get(), rig().skullCenter(boss.world(), chain), 2.2f, chain == 0 ? 0.8f : 1.15f);
            }
            if (r > 0 && t < lock && target.isAlive()) {
                reach = solveReach(chain, impact - t, target, target.getBbHeight() * 0.5, reach);
                boss.setChainReach(chain, reach[0], reach[1], reach[2]);
            }
            if (t <= impact && r % 2 == 0) {
                biteTelegraph(level, chain, boss.predictWorld(impact - t), t >= lock, chain == 0 ? 2.0 : 1.7);
            }
            if (t < 0) tell(level, chain);
            if (r > 2 && t < impact) {
                Vec3 mouth = rig().mouthPoint(boss.world(), chain);
                level.sendParticles(ParticleTypes.CRIT, mouth.x, mouth.y, mouth.z, 2, 0.4, 0.3, 0.4, 0.05);
            }
            if (t == impact) {
                Matrix4f[] world = boss.world();
                Vec3 skull = rig().skullCenter(world, chain);
                Vec3 mouth = rig().mouthPoint(world, chain);
                int hits = strikeSpheres(level, List.of(skull, mouth), chain == 0 ? 2.0 : 1.7, chain == 0 ? EmpressBalance.DAMAGE_BITE_MAIN : EmpressBalance.DAMAGE_BITE_SIDE, true, chain == 0,
                        boss.position(), 0.8, 0.35);
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, mouth.x, mouth.y, mouth.z, 3, 0.5, 0.3, 0.5, 0);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.MOSS_BLOCK.defaultBlockState()),
                        mouth.x, mouth.y, mouth.z, 20, 0.5, 0.4, 0.5, 0.2);
                if (hits > 0) level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, mouth.x, mouth.y, mouth.z, 8, 0.4, 0.4, 0.4, 0.1);
                sound(OWSounds.PLANT_EMPRESS_BITE.get(), mouth, 2.6f, chain == 0 ? 0.7f : 1.0f);
                boss.broadcastEvent(PlantEmpressEntity.EVENT_TREMOR);
            }
            if (t >= rig().lengthTicks(anim)) done = true;
        }

        @Override
        boolean locksBody() {
            return true;
        }

        @Override
        boolean usesChain(int c) {
            return c == chain;
        }

        @Override
        boolean isThreat() {
            return t - EmpressTimings.HEAD_TELL <= EmpressTimings.biteImpact(chain);
        }
    }

    public static class Devour extends Attack {
        private static final int LIFT = 10;
        final int chain;
        final LivingEntity victim;
        final EmpressVineSnare snare;
        Vec3 anchor;
        float[] reach;
        boolean released;

        public Devour(EmpressBrain brain, int chain, LivingEntity victim, EmpressVineSnare snare) {
            super(brain);
            this.chain = chain;
            this.victim = victim;
            this.snare = snare;
        }

        private Vec3 chest() {
            return victim.position().add(0, victim.getBbHeight() * 0.6, 0);
        }

        private void pin(LivingEntity entity, Vec3 goal) {
            Vec3 delta = goal.subtract(entity.position());
            if (delta.length() > 3.0) {
                entity.teleportTo(goal.x, goal.y, goal.z);
                delta = Vec3.ZERO;
            }
            entity.setDeltaMovement(delta.scale(0.45).add(0, 0.04, 0));
            entity.hurtMarked = true;
            entity.fallDistance = 0f;
            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 4, 10, false, false));
        }

        private void wrap(ServerLevel level) {
            double height = victim.getBbHeight();
            for (int i = 0; i < 8; i++) {
                double angle = t * 0.35 + i * (Math.PI / 4);
                double y = victim.getY() + ((t * 0.08 + i / 8.0) % 1.0) * height;
                double r = 0.45 + 0.1 * Math.sin(t * 0.3 + i);
                level.sendParticles(EmpressDamage.VINE, victim.getX() + Math.cos(angle) * r, y, victim.getZ() + Math.sin(angle) * r, 1, 0, 0, 0, 0);
            }
            if (t % 3 == 0) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.VINE.defaultBlockState()),
                        victim.getX(), victim.getY() + height * 0.5, victim.getZ(), 3, 0.3, height * 0.4, 0.3, 0.02);
            }
        }

        @Override
        void tick(ServerLevel level) {
            EmpressAnim anim = EmpressAnim.chew(chain);
            int grab = EmpressTimings.CHEW_GRAB;
            int release = EmpressTimings.CHEW_RELEASE;
            boolean alive = victim.isAlive() && !victim.isRemoved();
            if (t == 0) {
                anchor = victim.position();
                if (snare != null) snare.hold(release + 4);
                boss.setChainAction(chain, anim.ordinal(), clampAim(boss.aimFor(EmpressRig.ROOT_BONE[chain], anchor)));
                reach = solveReach(chain, grab, chest(), true, null, true);
                boss.setChainReach(chain, reach[0], reach[1], reach[2]);
                sound(OWSounds.PLANT_EMPRESS_SNARE.get(), anchor, 2.2f, 0.6f);
                sound(OWSounds.PLANT_EMPRESS_LUNGE.get(), rig().skullCenter(boss.world(), chain), 2.4f, 0.85f);
            }
            if (alive && !released) {
                float lift = EmpressPose.smooth(Math.min(1f, t / (float) LIFT)) * 0.9f;
                pin(victim, anchor.add(0, lift, 0));
                wrap(level);
                if (t < release) {
                    reach = solveReach(chain, Math.max(1, grab - t), chest(), true, reach, true);
                    boss.setChainReach(chain, reach[0], reach[1], reach[2]);
                }
                if (t < grab && t % 2 == 0) {
                    Vec3 mouth = rig().mouthPoint(boss.world(), chain);
                    level.sendParticles(ParticleTypes.CRIT, mouth.x, mouth.y, mouth.z, 3, 0.4, 0.3, 0.4, 0.05);
                }
                if (t == grab) {
                    Vec3 mouth = rig().mouthPoint(boss.world(), chain);
                    sound(OWSounds.PLANT_EMPRESS_BITE.get(), mouth, 2.6f, 0.75f);
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.MOSS_BLOCK.defaultBlockState()),
                            mouth.x, mouth.y, mouth.z, 24, 0.5, 0.4, 0.5, 0.2);
                    boss.broadcastEvent(PlantEmpressEntity.EVENT_TREMOR);
                }
                for (int bite : EmpressTimings.CHEW_BITES) {
                    if (t != bite) continue;
                    Vec3 mouth = rig().mouthPoint(boss.world(), chain);
                    victim.invulnerableTime = 0;
                    victim.hurt(EmpressDamage.roots(boss, boss), EmpressBalance.DAMAGE_CHEW);
                    level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, victim.getX(), victim.getY() + 1, victim.getZ(), 6, 0.3, 0.4, 0.3, 0.1);
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.MOSS_BLOCK.defaultBlockState()),
                            mouth.x, mouth.y, mouth.z, 10, 0.4, 0.3, 0.4, 0.15);
                    sound(OWSounds.PLANT_EMPRESS_BITE.get(), mouth, 1.8f, 0.9f + boss.getRandom().nextFloat() * 0.3f);
                }
            }
            if (t == release && !released) {
                released = true;
                Vec3 mouth = rig().mouthPoint(boss.world(), chain);
                if (alive) {
                    Vec3 away = new Vec3(victim.getX() - boss.getX(), 0, victim.getZ() - boss.getZ());
                    away = away.lengthSqr() < 1e-3 ? new Vec3(1, 0, 0) : away.normalize();
                    victim.setDeltaMovement(away.x * 0.65, 0.42, away.z * 0.65);
                    victim.hurtMarked = true;
                }
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, mouth.x, mouth.y, mouth.z, 30, 0.6, 0.5, 0.6, 0.1);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.VINE.defaultBlockState()),
                        anchor.x, anchor.y + 0.6, anchor.z, 30, 0.6, 0.6, 0.6, 0.12);
                sound(OWSounds.PLANT_EMPRESS_SPIT.get(), mouth, 2.2f, 1.1f);
                brain.requestWhip();
            }
            if (t >= rig().lengthTicks(anim)) done = true;
        }

        @Override
        boolean locksBody() {
            return true;
        }

        @Override
        boolean usesChain(int c) {
            return c == chain;
        }
    }

    public static class Burrow extends Attack {
        final Vec3 destination;

        public Burrow(EmpressBrain brain, Vec3 destination) {
            super(brain);
            this.destination = destination;
        }

        @Override
        void tick(ServerLevel level) {
            done = true;
            if (boss.canBurrow()) boss.beginBurrow(destination);
        }

        @Override
        boolean usesBody() {
            return true;
        }
    }

    public static class CarcassSlam extends Attack {

        final int chain;

        public CarcassSlam(EmpressBrain brain, int chain) {
            super(brain);
            this.chain = chain;
        }

        @Override
        void tick(ServerLevel level) {
            int impact = EmpressTimings.CARCASS_IMPACT;
            if (t == 0) {
                boss.setChainAction(chain, EmpressAnim.carcass(chain).ordinal(), 0f);
                Vec3 landing = ground(level, rig().skullCenter(boss.predictWorld(impact), chain));
                mark(level, "carcass", EmpressFx.SLAM, landing, 3.3, impact, facing(landing));
                EmpressFx.decal(level, EmpressFx.SKULL, landing, 1.2, impact, facing(landing));
                sound(OWSounds.PLANT_EMPRESS_HEAD_DEATH.get(), rig().skullCenter(boss.world(), chain), 1.8f, 0.6f);
            }
            if (t < impact && t % 2 == 0) {
                Vec3 at = ground(level, rig().skullCenter(boss.predictWorld(impact - t), chain));
                EmpressDamage.disc(level, at, 3.2, impact - t <= 6 ? EmpressDamage.DANGER : EmpressDamage.WARNING);
                Vec3 skull = rig().skullCenter(boss.world(), chain);
                level.sendParticles(EmpressDamage.TOXIC, skull.x, skull.y, skull.z, 4, 0.6, 0.6, 0.6, 0);
            }
            if (t == impact) {
                Vec3 skull = rig().skullCenter(boss.world(), chain);
                Vec3 at = ground(level, skull);
                strikeSpheres(level, List.of(at, at.add(0, 1.2, 0), skull), 3.0, EmpressBalance.DAMAGE_CARCASS, false, false, at, 0.9, 0.6);
                groundBurst(level, at, 90, 1.6);
                level.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y + 0.5, at.z, 3, 0.8, 0.2, 0.8, 0);
                level.sendParticles(EmpressDamage.TOXIC, at.x, at.y + 0.6, at.z, 40, 1.5, 0.5, 1.5, 0);
                sound(OWSounds.PLANT_EMPRESS_SLAM.get(), at, 3f, 0.6f);
                boss.broadcastEvent(PlantEmpressEntity.EVENT_SLAM);
            }
            if (t >= rig().lengthTicks(EmpressAnim.carcass(chain))) done = true;
        }

        @Override
        boolean usesChain(int c) {
            return c == chain;
        }

        @Override
        boolean isThreat() {
            return t <= EmpressTimings.CARCASS_IMPACT;
        }
    }
}
