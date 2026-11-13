package net.tiew.operationWild.entity.bosses.plantempress;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.entity.bosses.PlantEmpressEntity;
import net.tiew.operationWild.sound.OWSounds;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class EmpressBrain {

    private static final int MAX_BUSHES = 14;
    private static final double LUNGE_MIN = 3.0;
    private static final double LUNGE_MAX = 13.0;
    private static final float AIM_LIMIT = 1.45f;

    final PlantEmpressEntity boss;
    private final List<EmpressAttacks.Attack> running = new ArrayList<>();
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Map<UUID, List<Long>> reservations = new HashMap<>();
    private final Map<UUID, Long> lastTargeted = new HashMap<>();
    private final Map<UUID, Integer> hugging = new HashMap<>();
    private final List<Entity> hazards = new ArrayList<>();
    private final List<Puddle> puddles = new ArrayList<>();
    private final List<Burst> bursts = new ArrayList<>();
    private LivingEntity focus;
    private int globalCooldown = 30;
    private int salvo;
    private String lastAttack = "";
    private long lastGuard;
    private long lastBody;
    private float desperationAngle;
    private boolean pendingWhip;
    private long fightStart = -1;

    record Puddle(Vec3 center, float radius) {}

    static final class Burst {
        final Vec3 center;
        final float radius;
        final long fireAt;
        final int warn;
        final float damage;
        final boolean poison;

        Burst(Vec3 center, float radius, long fireAt, int warn, float damage, boolean poison) {
            this.center = center;
            this.radius = radius;
            this.fireAt = fireAt;
            this.warn = warn;
            this.damage = damage;
            this.poison = poison;
        }
    }

    record Candidate(String id, int weight, int cooldown, Supplier<EmpressAttacks.Attack> factory, boolean body) {
        Candidate(String id, int weight, int cooldown, Supplier<EmpressAttacks.Attack> factory) {
            this(id, weight, cooldown, factory, false);
        }
    }

    public EmpressBrain(PlantEmpressEntity boss) {
        this.boss = boss;
    }

    @Nullable
    public LivingEntity focus() {
        return focus;
    }

    public boolean locksBody() {
        for (EmpressAttacks.Attack attack : running) if (attack.locksBody()) return true;
        return false;
    }

    public void tick(ServerLevel level) {
        List<Player> fighters = boss.fighters(PlantEmpressEntity.LEASH_RADIUS);
        updateFocus(fighters);
        tickRunning(level);
        detectThreats(level);
        if (fighters.isEmpty()) return;
        if (fightStart < 0) fightStart = boss.now();
        if (pendingWhip && bodyFree()) {
            pendingWhip = false;
            running.add(new EmpressAttacks.RootWhip(this));
            lastBody = boss.now();
            cooldowns.put("whip", boss.now() + 170);
            globalCooldown = Math.max(globalCooldown, 30);
            return;
        }
        if (globalCooldown > 0) {
            globalCooldown--;
            return;
        }
        if (threats() >= maxThreats()) return;
        startNext(level, fighters);
    }

    private void tickRunning(ServerLevel level) {
        List<EmpressAttacks.Attack> snapshot = new ArrayList<>(running);
        for (EmpressAttacks.Attack attack : snapshot) {
            if (attack.done) continue;
            attack.tick(level);
            attack.t++;
        }
        running.removeIf(attack -> attack.done);
    }

    public void add(EmpressAttacks.Attack attack) {
        running.add(attack);
    }

    private int threats() {
        int count = 0;
        for (EmpressAttacks.Attack attack : running) if (!attack.done && attack.isThreat()) count++;
        return count;
    }

    private int maxThreats() {
        return boss.stage() == 3 ? 3 : 2;
    }

    private void updateFocus(List<Player> fighters) {
        long now = boss.now();
        LivingEntity best = null;
        double bestScore = -1;
        for (Player player : fighters) {
            double distance = player.distanceTo(boss);
            double since = Math.min(1.0, (now - lastTargeted.getOrDefault(player.getUUID(), 0L)) / 200.0);
            double score = 1.0 / (1.0 + distance / 12.0) + boss.threatOf(player) / 60.0 + since * 0.6;
            if (score > bestScore) {
                bestScore = score;
                best = player;
            }
        }
        focus = best;
    }

    public void markTargeted(LivingEntity target) {
        lastTargeted.put(target.getUUID(), boss.now());
    }

    public boolean canReserve(LivingEntity target, long impact) {
        List<Long> list = reservations.get(target.getUUID());
        if (list == null) return true;
        list.removeIf(t -> t < boss.now() - 20);
        for (long t : list) if (Math.abs(t - impact) < 10) return false;
        return true;
    }

    public void reserve(LivingEntity target, long impact) {
        reservations.computeIfAbsent(target.getUUID(), k -> new ArrayList<>()).add(impact);
    }

    public boolean chainFree(int chain) {
        if (boss.isChainBusy(chain) || boss.isBodyBusy()) return false;
        for (EmpressAttacks.Attack attack : running) if (!attack.done && (attack.usesChain(chain) || attack.usesBody())) return false;
        return true;
    }

    public boolean bodyFree() {
        if (boss.isBodyBusy()) return false;
        for (int chain = 0; chain < 3; chain++) if (!boss.isChainDead(chain) && boss.isChainBusy(chain)) return false;
        for (EmpressAttacks.Attack attack : running) {
            if (attack.done) continue;
            if (attack.usesBody()) return false;
            for (int chain = 0; chain < 3; chain++) if (attack.usesChain(chain) && !boss.isChainDead(chain)) return false;
        }
        return true;
    }

    private boolean ready(String id) {
        return boss.now() >= cooldowns.getOrDefault(id, 0L) && !id.equals(lastAttack);
    }

    @Nullable
    private Player lungeTarget(int chain, List<Player> fighters) {
        Matrix4f[] world = boss.world();
        Vec3 root = EmpressRig.origin(world, EmpressRig.ROOT_BONE[chain]);
        Player best = null;
        double bestScore = Double.MAX_VALUE;
        for (Player player : fighters) {
            double d = Math.sqrt(player.distanceToSqr(root.x, player.getY(), root.z));
            if (d < LUNGE_MIN || d > LUNGE_MAX) continue;
            float aim = boss.aimFor(EmpressRig.ROOT_BONE[chain], player.position());
            if (Math.abs(aim) > AIM_LIMIT) continue;
            double score = d + (player == focus ? -3 : 0);
            if (score < bestScore) {
                bestScore = score;
                best = player;
            }
        }
        return best;
    }

    @Nullable
    private Player nearest(List<Player> fighters, double max) {
        Player best = null;
        double bestDistance = max * max;
        for (Player player : fighters) {
            double d = player.distanceToSqr(boss.getX(), player.getY(), boss.getZ());
            if (d < bestDistance) {
                bestDistance = d;
                best = player;
            }
        }
        return best;
    }

    private void startNext(ServerLevel level, List<Player> fighters) {
        int stage = boss.stage();
        LivingEntity target = focus;
        if (target == null) return;
        List<Candidate> candidates = new ArrayList<>();
        long now = boss.now();

        for (int chain = 1; chain <= 2; chain++) {
            if (boss.isChainDead(chain)) continue;
            int c = chain;
            Player prey = lungeTarget(c, fighters);
            if (prey != null && chainFree(c) && canReserve(prey, now + EmpressTimings.BITE_IMPACT_SIDE)) {
                candidates.add(new Candidate("bite" + c, EmpressBalance.weight("bite" + c, stage), EmpressBalance.cooldown("bite" + c), () -> new EmpressAttacks.Bite(this, c, prey)));
                candidates.add(new Candidate("slam" + c, EmpressBalance.weight("slam" + c, stage), EmpressBalance.cooldown("slam" + c), () -> new EmpressAttacks.Lunge(this, c, prey, 0)));
            }
        }
        if (stage == 1) {
            Player both = lungeTarget(1, fighters);
            if (both != null && lungeTarget(2, fighters) == both && chainFree(1) && chainFree(2)) {
                candidates.add(new Candidate("scissors", EmpressBalance.weight("scissors", stage), EmpressBalance.cooldown("scissors"), () -> new EmpressAttacks.Scissors(this, both)));
            }
        }
        Player close = nearest(fighters, 13);
        if (close != null) candidates.add(new Candidate("whip", EmpressBalance.weight("whip", stage), EmpressBalance.cooldown("whip"), () -> new EmpressAttacks.RootWhip(this), true));
        if (chainFree(0)) {
            candidates.add(new Candidate("spit", EmpressBalance.weight("spit", stage), EmpressBalance.cooldown("spit"), () -> new EmpressAttacks.Spit(this, target, false)));
            if (stage >= 2) {
                if (bushCount() < MAX_BUSHES - 4) candidates.add(new Candidate("seeds", EmpressBalance.weight("seeds", stage), EmpressBalance.cooldown("seeds"), () -> new EmpressAttacks.Spit(this, target, true)));
                candidates.add(new Candidate("snare", EmpressBalance.weight("snare", stage), EmpressBalance.cooldown("snare"), () -> new EmpressAttacks.Snare(this, target)));
            }
            if (stage == 3 && !boss.isStaggered(0)) {
                Player prey = null;
                for (Player player : fighters) {
                    double d = Math.sqrt(player.distanceToSqr(boss.getX(), player.getY(), boss.getZ()));
                    if (d >= 4 && d <= 16 && (prey == null || player == focus)) prey = player;
                }
                Player mainPrey = prey;
                if (mainPrey != null && canReserve(mainPrey, now + EmpressTimings.BITE_IMPACT_MAIN)) {
                    candidates.add(new Candidate("bite0", 22, 60, () -> new EmpressAttacks.Bite(this, 0, mainPrey)));
                }
            }
        }
        if (stage >= 2) {
            Player near = nearest(fighters, 9);
            if (near != null) candidates.add(new Candidate("triple", EmpressBalance.weight("triple", stage), EmpressBalance.cooldown("triple"), () -> new EmpressAttacks.TripleBite(this, near), true));
        }
        if (stage == 3) {
            Player plunge = null;
            for (Player player : fighters) {
                double d = Math.sqrt(player.distanceToSqr(boss.getX(), player.getY(), boss.getZ()));
                if (d >= 3.5 && d <= 10.5 && (plunge == null || player == focus)) plunge = player;
            }
            Player plungeTarget = plunge;
            if (plungeTarget != null && !boss.isStaggered(0)) {
                candidates.add(new Candidate("plunge", EmpressBalance.weight("plunge", stage), EmpressBalance.cooldown("plunge"), () -> new EmpressAttacks.Plunge(this, plungeTarget), true));
            }
            if (!boss.isStaggered(0)) candidates.add(new Candidate("quake", EmpressBalance.weight("quake", stage), EmpressBalance.cooldown("quake"), () -> new EmpressAttacks.Earthquake(this, target), true));
            candidates.add(new Candidate("rain", EmpressBalance.weight("rain", stage), EmpressBalance.cooldown("rain"), () -> new EmpressAttacks.SporeRain(this, fighters)));
            candidates.add(new Candidate("eruption", EmpressBalance.weight("eruption", stage), EmpressBalance.cooldown("eruption"), () -> new EmpressAttacks.RootEruption(this, fighters, null)));
            for (int chain = 1; chain <= 2; chain++) {
                int c = chain;
                if (!boss.isChainBusy(c) && running.stream().noneMatch(a -> !a.done && a.usesChain(c))) {
                    candidates.add(new Candidate("carcass" + c, EmpressBalance.weight("carcass" + c, stage), EmpressBalance.cooldown("carcass" + c), () -> new EmpressAttacks.CarcassSlam(this, c)));
                }
            }
        }
        if (now - fightStart > 300 && boss.canBurrow()) {
            Vec3 destination = burrowDestination(level, fighters);
            if (destination != null) candidates.add(new Candidate("burrow", EmpressBalance.weight("burrow", stage), EmpressBalance.cooldown("burrow"), () -> new EmpressAttacks.Burrow(this, destination), true));
        }

        boolean wantBody = now - lastBody > (stage == 3 ? 130 : 150) && candidates.stream().anyMatch(c -> c.body() && ready(c.id()));
        if (wantBody) {
            if (bodyFree()) {
                candidates.removeIf(c -> !c.body());
            } else {
                candidates.removeIf(c -> c.body() || c.id().startsWith("bite") || c.id().startsWith("slam") || c.id().equals("spit")
                        || c.id().equals("seeds") || c.id().equals("snare") || c.id().equals("scissors"));
            }
        } else {
            boolean free = bodyFree();
            candidates.removeIf(c -> c.body() && !free);
        }
        candidates.removeIf(c -> c.weight() <= 0);
        List<Candidate> fresh = new ArrayList<>(candidates);
        fresh.removeIf(c -> !ready(c.id()));
        if (fresh.isEmpty()) {
            fresh = candidates;
            fresh.removeIf(c -> now < cooldowns.getOrDefault(c.id(), 0L));
        }
        candidates = fresh;
        if (candidates.isEmpty()) return;
        int total = 0;
        for (Candidate c : candidates) total += c.weight();
        int roll = boss.getRandom().nextInt(total);
        Candidate chosen = candidates.get(0);
        for (Candidate c : candidates) {
            roll -= c.weight();
            if (roll < 0) {
                chosen = c;
                break;
            }
        }
        EmpressAttacks.Attack attack = chosen.factory().get();
        running.add(attack);
        cooldowns.put(chosen.id(), now + chosen.cooldown());
        lastAttack = chosen.id();
        if (chosen.body()) lastBody = now;
        salvo++;
        globalCooldown = stage == 1 ? 34 : stage == 2 ? 28 : 20;
        if (salvo % 3 == 0) globalCooldown += 40;
    }

    private void detectThreats(ServerLevel level) {
        Matrix4f[] world = boss.world();
        EmpressRig rig = EmpressRig.get();
        for (int chain = 0; chain < 3; chain++) {
            if (boss.getReflexes(chain) <= 0 || !chainFree(chain)) continue;
            AABB skull = rig.skullBox(world, chain).inflate(0.6);
            List<Projectile> projectiles = level.getEntitiesOfClass(Projectile.class, skull.inflate(14),
                    p -> p.getOwner() instanceof Player && p.getDeltaMovement().lengthSqr() > 0.3);
            for (Projectile projectile : projectiles) {
                Vec3 from = projectile.position();
                Vec3 to = from.add(projectile.getDeltaMovement().scale(7));
                if (skull.contains(from) || skull.clip(from, to).isEmpty()) continue;
                float direction = bestDodge(chain, from, projectile.getDeltaMovement());
                if (boss.tryDodge(chain, direction)) {
                    Vec3 c = skull.getCenter();
                    level.sendParticles(ParticleTypes.CLOUD, c.x, c.y, c.z, 10, 0.6, 0.4, 0.6, 0.05);
                    level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, c.x, c.y, c.z, 14, 0.8, 0.6, 0.8, 0.02);
                }
                break;
            }
        }
    }

    private float bestDodge(int chain, Vec3 from, Vec3 velocity) {
        EmpressRig rig = EmpressRig.get();
        float best = 1f;
        double bestDistance = -1;
        for (float side : new float[]{1f, -1f}) {
            EmpressRig.Pose pose = EmpressPose.evaluate(EmpressPose.compose(boss, 0f));
            int[] segments = EmpressRig.SEGMENTS[chain];
            pose.rz[segments[0]] += side * 0.42f;
            Vec3 center = rig.skullCenter(rig.solve(pose, boss.entityMatrix()), chain);
            Vec3 dir = velocity.normalize();
            Vec3 rel = center.subtract(from);
            double distance = rel.subtract(dir.scale(rel.dot(dir))).length();
            if (distance > bestDistance) {
                bestDistance = distance;
                best = side;
            }
        }
        return best;
    }

    public void tickHazards(ServerLevel level) {
        long now = boss.now();
        hazards.removeIf(Entity::isRemoved);

        if (now % 10 == 0) {
            for (Puddle puddle : puddles) {
                AABB box = new AABB(puddle.center().x - puddle.radius(), puddle.center().y - 1, puddle.center().z - puddle.radius(),
                        puddle.center().x + puddle.radius(), puddle.center().y + 1.6, puddle.center().z + puddle.radius());
                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, e -> EmpressDamage.canHit(e, boss))) {
                    if (target.position().distanceToSqr(puddle.center().x, target.getY(), puddle.center().z) > puddle.radius() * puddle.radius()) continue;
                    target.hurt(EmpressDamage.roots(boss, boss), EmpressBalance.DAMAGE_PUDDLE);
                    target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
                }
            }
        }
        if (now % 3 == 0) {
            for (Puddle puddle : puddles) {
                Vec3 c = puddle.center();
                level.sendParticles(EmpressDamage.TOXIC, c.x, c.y + 0.1, c.z, 6, puddle.radius() * 0.5, 0.02, puddle.radius() * 0.5, 0);
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, c.x, c.y + 0.3, c.z, 2, puddle.radius() * 0.5, 0.2, puddle.radius() * 0.5, 0);
                if (boss.getRandom().nextInt(4) == 0) {
                    level.sendParticles(ParticleTypes.BUBBLE_POP, c.x, c.y + 0.1, c.z, 3, puddle.radius() * 0.4, 0, puddle.radius() * 0.4, 0);
                }
            }
        }

        float wall = boss.wallRadius(0f);
        Vec3 home = boss.home();
        if (wall > 0f) {
            if (now % 4 == 0) {
                int count = (int) (wall * 2.2);
                for (int i = 0; i < count; i++) {
                    double angle = boss.getRandom().nextDouble() * Math.PI * 2;
                    double x = home.x + Math.cos(angle) * wall;
                    double z = home.z + Math.sin(angle) * wall;
                    level.sendParticles(ParticleTypes.FALLING_SPORE_BLOSSOM, x, home.y + 2.5, z, 1, 0.3, 0.6, 0.3, 0);
                }
            }
            if (now % 10 == 0) {
                for (Player player : boss.fighters(wall + 40)) {
                    double d = Math.sqrt(player.distanceToSqr(home.x, player.getY(), home.z));
                    if (d < wall - 0.6) continue;
                    player.hurt(EmpressDamage.roots(boss, boss), EmpressBalance.DAMAGE_WALL);
                    Vec3 in = new Vec3(home.x - player.getX(), 0, home.z - player.getZ()).normalize();

                    player.setDeltaMovement(in.x * 0.7, 0.3, in.z * 0.7);
                    player.hurtMarked = true;
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SWEET_BERRY_BUSH.defaultBlockState()),
                            player.getX(), player.getY() + 0.8, player.getZ(), 12, 0.3, 0.4, 0.3, 0.1);
                }
            }
        }

        tickBursts(level, now);
        tickGuard(level, now);
    }

    private void tickBursts(ServerLevel level, long now) {
        Iterator<Burst> iterator = bursts.iterator();
        while (iterator.hasNext()) {
            Burst burst = iterator.next();
            long left = burst.fireAt - now;
            if (left > burst.warn) continue;
            if (left > 0) {
                if (left % 2 == 0) {
                    EmpressDamage.ring(level, burst.center, burst.radius, left < 6 ? EmpressDamage.DANGER : EmpressDamage.WARNING, 0.6);
                    level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, burst.center.x, burst.center.y + 0.3, burst.center.z, 2,
                            burst.radius * 0.5, 0.1, burst.radius * 0.5, 0);
                }
                continue;
            }
            Vec3 c = burst.center;
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, c.x, c.y + 0.8, c.z, 40, burst.radius * 0.6, 0.8, burst.radius * 0.6, 0.04);
            level.sendParticles(EmpressDamage.TOXIC, c.x, c.y + 0.6, c.z, 30, burst.radius * 0.5, 0.6, burst.radius * 0.5, 0);
            level.sendParticles(ParticleTypes.EXPLOSION, c.x, c.y + 0.6, c.z, 1, 0, 0, 0, 0);
            level.sendParticles(ParticleTypes.SNEEZE, c.x, c.y + 0.5, c.z, 16, burst.radius * 0.5, 0.3, burst.radius * 0.5, 0.05);
            level.playSound(null, c.x, c.y, c.z, OWSounds.PLANT_EMPRESS_POLLEN.get(), SoundSource.HOSTILE, 1.4f, 0.8f + boss.getRandom().nextFloat() * 0.4f);
            AABB box = new AABB(c.x - burst.radius, c.y - 0.5, c.z - burst.radius, c.x + burst.radius, c.y + 3, c.z + burst.radius);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, e -> EmpressDamage.canHit(e, boss))) {
                if (target.position().distanceToSqr(c.x, target.getY(), c.z) > burst.radius * burst.radius) continue;
                EmpressDamage.strike(boss, target, EmpressDamage.roots(boss, boss), burst.damage, c, 0.5, 0.45, false);
                if (burst.poison) target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
            }
            iterator.remove();
        }
    }

    public void addBurst(Vec3 center, float radius, long fireAt, int warn, float damage, boolean poison) {
        bursts.add(new Burst(center, radius, fireAt, warn, damage, poison));
    }

    private void tickGuard(ServerLevel level, long now) {
        if (boss.getPhase() != PlantEmpressEntity.PHASE_FIGHT) return;
        for (Player player : boss.fighters(4.0)) {
            hugging.merge(player.getUUID(), 1, Integer::sum);
        }
        hugging.replaceAll((uuid, ticks) -> {
            Player player = level.getPlayerByUUID(uuid);
            boolean close = player != null && player.distanceToSqr(boss.getX(), player.getY(), boss.getZ()) < 16;
            return close ? ticks : Math.max(0, ticks - 3);
        });
        boolean trigger = hugging.values().stream().anyMatch(t -> t > 80);
        if (trigger && now - lastGuard > 110) {
            lastGuard = now;
            hugging.clear();
            boss.playBossSound(OWSounds.PLANT_EMPRESS_ROOTS.get(), 2.5f, 0.6f);
            boss.broadcastEvent(PlantEmpressEntity.EVENT_TREMOR);
            boss.setStemAction(EmpressAnim.STEMS_DIG);
            for (int ring = 0; ring < 2; ring++) {
                double radius = ring == 0 ? 2.0 : 3.4;
                int count = ring == 0 ? 7 : 11;
                for (int i = 0; i < count; i++) {
                    double angle = Math.PI * 2 * i / count + ring * 0.3;
                    double x = boss.getX() + Math.cos(angle) * radius;
                    double z = boss.getZ() + Math.sin(angle) * radius;
                    double y = EmpressDamage.groundY(level, x, z, boss.getY());
                    spawn(level, EmpressRootSpike.create(level, boss, x, y, z, 16 + ring * 3, EmpressBalance.DAMAGE_GUARD_THORNS).lift(0.9));
                }
            }
        }
    }

    private static final int[] SECTOR_WAVES = {34, 62, 90, 118, 146};
    private static final int SECTOR_WARN = 24;
    private static final int SECTORS = 8;

    private boolean doomed(double angle, int wave) {
        float offset = desperationAngle + (wave / 2) * (Mth.PI / SECTORS);
        double rel = Mth.positiveModulo((float) (angle - offset), Mth.TWO_PI);
        int sector = (int) (rel / (Mth.TWO_PI / SECTORS));
        return (sector + wave) % 2 == 0;
    }

    public void tickDesperation(ServerLevel level, int t) {
        if (t == 0) desperationAngle = boss.getRandom().nextFloat() * Mth.TWO_PI;
        if (t % 6 == 0 && t < 150) {
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, boss.getX(), boss.getY() + 9, boss.getZ(), 24, 3, 2, 3, 0.06);
        }
        if (t % 20 == 10) boss.broadcastEvent(PlantEmpressEntity.EVENT_TREMOR);
        Vec3 c = boss.position();
        for (int w = 0; w < SECTOR_WAVES.length; w++) {
            int fire = SECTOR_WAVES[w];
            if (t >= fire - SECTOR_WARN && t < fire && t % 2 == 0) {
                float charge = 1f - (fire - t) / (float) SECTOR_WARN;
                for (int edge = 0; edge < SECTORS; edge++) {
                    float angle = desperationAngle + (w / 2) * (Mth.PI / SECTORS) + edge * (Mth.TWO_PI / SECTORS);
                    Vec3 dir = new Vec3(Mth.cos(angle), 0, Mth.sin(angle));
                    EmpressDamage.line(level, c.add(dir.scale(2.5)), c.add(dir.scale(21)), EmpressDamage.VINE, 1.2);
                }
                int count = (int) (40 + charge * 80);
                for (int i = 0; i < count; i++) {
                    double angle = boss.getRandom().nextDouble() * Math.PI * 2;
                    if (!doomed(angle, w)) continue;
                    double r = 2.5 + Math.sqrt(boss.getRandom().nextDouble()) * 18.5;
                    double x = c.x + Math.cos(angle) * r;
                    double z = c.z + Math.sin(angle) * r;
                    double y = EmpressDamage.groundY(level, x, z, boss.getY());
                    level.sendParticles(charge > 0.7f ? EmpressDamage.DANGER : EmpressDamage.TOXIC, x, y + 0.2 + charge, z, 1, 0.1, 0.2, 0.1, 0);
                    if (i % 4 == 0) level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, x, y + 0.5, z, 1, 0.2, 0.3, 0.2, 0.01);
                }
                EmpressDamage.ring(level, c, 2.5, EmpressDamage.TOXIC, 0.4);
                if (t == fire - SECTOR_WARN) boss.playBossSound(OWSounds.PLANT_EMPRESS_SPIT_CHARGE.get(), 3f, 0.6f + w * 0.08f);
            }
            if (t == fire) {
                boss.playBossSound(OWSounds.PLANT_EMPRESS_POLLEN.get(), 4f, 0.6f + w * 0.05f);
                boss.broadcastEvent(PlantEmpressEntity.EVENT_SLAM);
                for (int i = 0; i < 90; i++) {
                    double angle = boss.getRandom().nextDouble() * Math.PI * 2;
                    if (!doomed(angle, w)) continue;
                    double r = 2.5 + boss.getRandom().nextDouble() * 18.5;
                    double x = c.x + Math.cos(angle) * r;
                    double z = c.z + Math.sin(angle) * r;
                    double y = EmpressDamage.groundY(level, x, z, boss.getY());
                    level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, x, y + 0.8, z, 4, 0.4, 0.6, 0.4, 0.05);
                    if (i % 6 == 0) level.sendParticles(ParticleTypes.EXPLOSION, x, y + 0.6, z, 1, 0, 0, 0, 0);
                }
                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, boss.getBoundingBox().inflate(22, 6, 22), e -> EmpressDamage.canHit(e, boss))) {
                    double dx = target.getX() - c.x;
                    double dz = target.getZ() - c.z;
                    double d = Math.sqrt(dx * dx + dz * dz);
                    if (d > 21.5) continue;
                    if (d > 2.5 && !doomed(Math.atan2(dz, dx), w)) continue;
                    EmpressDamage.strike(boss, target, EmpressDamage.roots(boss, boss), EmpressBalance.DAMAGE_POLLEN, c, 0.4, 0.5, false);
                    target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
                }
            }
        }
    }

    public void transitionShockwave(ServerLevel level) {
        Vec3 c = boss.position();
        for (int ring = 0; ring < 3; ring++) {
            EmpressDamage.ring(level, c, 2.5 + ring * 2.2, EmpressDamage.WARNING, 0.5);
        }
        BlockState ground = boss.groundState(level);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), c.x, c.y + 0.3, c.z, 120, 3, 0.3, 3, 0.4);
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, c.x, c.y + 5, c.z, 80, 4, 3, 4, 0.08);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, boss.getBoundingBox().inflate(7), e -> EmpressDamage.canHit(e, boss))) {
            double d = target.distanceTo(boss);
            if (d < 4.5) {
                EmpressDamage.strike(boss, target, EmpressDamage.roots(boss, boss), EmpressBalance.DAMAGE_SHOCKWAVE, c, 1.0, 0.5, false);
            } else {
                Vec3 away = new Vec3(target.getX() - c.x, 0, target.getZ() - c.z).normalize();
                target.setDeltaMovement(away.x * 0.9, 0.45, away.z * 0.9);
                target.hurtMarked = true;
            }
        }
    }

    public void onHeadDeath(ServerLevel level, int chain) {
        Matrix4f[] predicted = boss.predictWorld(EmpressRig.get().lengthTicks(EmpressAnim.headDead(chain)) + 4);
        Vec3 skull = EmpressRig.get().skullCenter(predicted, chain);
        Vec3 ground = EmpressDamage.onGround(level, skull, boss.getY());
        puddles.add(new Puddle(ground, 2.7f));
        Vec3 c = EmpressRig.get().skullCenter(boss.world(), chain);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MOSS_BLOCK.defaultBlockState()), c.x, c.y, c.z, 80, 1, 1, 1, 0.3);
        level.sendParticles(ParticleTypes.FALLING_SPORE_BLOSSOM, c.x, c.y, c.z, 60, 1.5, 1, 1.5, 0);
        level.sendParticles(EmpressDamage.TOXIC, c.x, c.y, c.z, 50, 1, 1, 1, 0);
    }

    public boolean onSnared(ServerLevel level, LivingEntity target, Vec3 at) {
        int best = -1;
        double bestDistance = Double.MAX_VALUE;
        Matrix4f[] world = boss.world();
        for (int chain = 0; chain < 3; chain++) {
            if (boss.isChainDead(chain) || boss.isStaggered(chain)) continue;
            if (running.stream().anyMatch(a -> !a.done && a instanceof EmpressAttacks.Devour)) return false;
            double d = EmpressRig.origin(world, EmpressRig.ROOT_BONE[chain]).distanceToSqr(at);
            if (chain == 0 && boss.isSealed()) d += 9.0;
            if (d < bestDistance) {
                bestDistance = d;
                best = chain;
            }
        }
        if (best < 0) return false;
        cancelChain(best);
        if (boss.isBodyBusy()) {
            cancelBody();
            boss.setBodyAction(null, 0f);
        }
        running.add(new EmpressAttacks.Devour(this, best, target, at));

        reserve(target, boss.now() + EmpressTimings.CHEW_RELEASE);
        return true;
    }

    public void requestWhip() {
        pendingWhip = true;
    }

    @Nullable
    private Vec3 burrowDestination(ServerLevel level, List<Player> fighters) {
        Vec3 home = boss.home();
        for (int attempt = 0; attempt < 16; attempt++) {
            Player around = fighters.get(boss.getRandom().nextInt(fighters.size()));
            double angle = boss.getRandom().nextDouble() * Math.PI * 2;
            double r = 6 + boss.getRandom().nextDouble() * 3;
            Vec3 p = around.position().add(Math.cos(angle) * r, 0, Math.sin(angle) * r);
            Vec3 fromHome = new Vec3(p.x - home.x, 0, p.z - home.z);
            if (fromHome.length() > 14) p = home.add(fromHome.normalize().scale(14));
            if (new Vec3(p.x - boss.getX(), 0, p.z - boss.getZ()).length() < 6) continue;
            double y = EmpressDamage.groundY(level, p.x, p.z, boss.getY());
            net.minecraft.core.BlockPos feet = net.minecraft.core.BlockPos.containing(p.x, y + 0.01, p.z);
            if (level.getBlockState(feet.below()).getCollisionShape(level, feet.below()).isEmpty()) continue;
            boolean clear = true;
            for (int h = 0; h < 3; h++) {
                if (!level.getBlockState(feet.above(h)).getCollisionShape(level, feet.above(h)).isEmpty()) clear = false;
            }
            if (!clear) continue;
            return new Vec3(p.x, y, p.z);
        }
        return null;
    }

    public void spawnCosmeticRoot(ServerLevel level, Vec3 at) {
        double y = EmpressDamage.groundY(level, at.x, at.z, boss.getY());
        spawn(level, EmpressRootSpike.create(level, boss, at.x, y, at.z, 8, 0f));
    }

    public void spawn(ServerLevel level, Entity entity) {
        level.addFreshEntity(entity);
        hazards.add(entity);
    }

    public void track(Entity entity) {
        hazards.add(entity);
    }

    public boolean canPlantBush() {
        return bushCount() < MAX_BUSHES;
    }

    private int bushCount() {
        int count = 0;
        for (Entity entity : hazards) if (entity instanceof EmpressThornBush && !entity.isRemoved()) count++;
        return count;
    }

    public void cancelChain(int chain) {
        for (EmpressAttacks.Attack attack : running) if (attack.usesChain(chain)) attack.done = true;
    }

    public void cancelBody() {
        for (EmpressAttacks.Attack attack : running) if (attack.usesBody()) attack.done = true;
    }

    public void clear() {
        for (EmpressAttacks.Attack attack : running) attack.done = true;
        running.clear();
        reservations.clear();
        bursts.clear();
        globalCooldown = 40;
    }

    public void reset(ServerLevel level) {
        clear();
        for (Entity entity : hazards) if (!entity.isRemoved()) entity.discard();
        hazards.clear();
        puddles.clear();
        hugging.clear();
        cooldowns.clear();
        lastAttack = "";
        salvo = 0;
    }
}
