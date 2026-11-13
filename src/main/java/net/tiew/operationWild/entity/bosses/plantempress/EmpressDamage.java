package net.tiew.operationWild.entity.bosses.plantempress;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.core.OWUtils;
import net.tiew.operationWild.entity.bosses.PlantEmpressEntity;
import org.joml.Vector3f;

public final class EmpressDamage {

    public static final ResourceKey<DamageType> BITE = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "plant_empress_bite"));
    public static final ResourceKey<DamageType> ROOTS = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "plant_empress_roots"));

    public static final DustParticleOptions WARNING = new DustParticleOptions(new Vector3f(1f, 0.32f, 0.08f), 1.5f);
    public static final DustParticleOptions DANGER = new DustParticleOptions(new Vector3f(1f, 0.1f, 0.05f), 2.0f);
    public static final DustParticleOptions TOXIC = new DustParticleOptions(new Vector3f(0.55f, 0.95f, 0.2f), 1.5f);
    public static final DustParticleOptions EARTH = new DustParticleOptions(new Vector3f(0.42f, 0.27f, 0.12f), 1.8f);
    public static final DustParticleOptions VINE = new DustParticleOptions(new Vector3f(0.18f, 0.55f, 0.12f), 1.6f);

    private EmpressDamage() {
    }

    private static Holder<DamageType> type(Level level, ResourceKey<DamageType> key) {
        return level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key);
    }

    public static DamageSource bite(PlantEmpressEntity boss, Vec3 from) {
        return new DamageSource(type(boss.level(), BITE), boss, boss, from);
    }

    public static DamageSource roots(Entity source, PlantEmpressEntity boss) {
        return new DamageSource(type(source.level(), ROOTS), source, boss != null ? boss : source, null);
    }

    public static boolean canHit(Entity entity, PlantEmpressEntity boss) {
        if (!(entity instanceof LivingEntity living) || !living.isAlive()) return false;
        if (entity == boss || entity instanceof PlantEmpressEntity) return false;
        if (entity instanceof Player player) return !player.isCreative() && !player.isSpectator();
        return true;
    }

    public static boolean strike(PlantEmpressEntity boss, LivingEntity target, DamageSource source, float damage,
                                 Vec3 knockFrom, double horizontal, double vertical, boolean heavy) {
        boolean wasBlocking = target instanceof Player player && player.isBlocking() && player.isDamageSourceBlocked(source);
        boolean hurt = target.hurt(source, damage);
        if (wasBlocking && heavy && target instanceof Player player) player.disableShield();
        if (!hurt && !wasBlocking) return false;
        Vec3 away = target.position().subtract(knockFrom);
        away = new Vec3(away.x, 0, away.z);
        if (away.lengthSqr() < 1.0E-4) away = new Vec3(0, 0, 1);
        away = away.normalize();
        double factor = wasBlocking ? 0.4 : 1.0;
        if (boss != null) {
            OWUtils.launchResisted(boss, target, away.x * horizontal * factor, vertical * factor, away.z * horizontal * factor);
        } else {
            target.setDeltaMovement(away.x * horizontal * factor, vertical * factor, away.z * horizontal * factor);
        }
        target.hurtMarked = true;
        return true;
    }

    public static double groundY(Level level, double x, double z, double reference) {
        int bx = net.minecraft.util.Mth.floor(x);
        int bz = net.minecraft.util.Mth.floor(z);
        int top = net.minecraft.util.Mth.floor(reference) + 4;
        int bottom = net.minecraft.util.Mth.floor(reference) - 8;
        net.minecraft.core.BlockPos.MutableBlockPos pos = new net.minecraft.core.BlockPos.MutableBlockPos();
        for (int y = top; y >= bottom; y--) {
            pos.set(bx, y, bz);
            BlockState state = level.getBlockState(pos);
            if (!state.getCollisionShape(level, pos).isEmpty()) return y + state.getCollisionShape(level, pos).max(net.minecraft.core.Direction.Axis.Y);
        }
        return reference;
    }

    public static double floorUnder(Level level, net.minecraft.world.entity.Entity entity) {
        return groundY(level, entity.getX(), entity.getZ(), entity.getY() - 0.5);
    }

    public static boolean isGrounded(Level level, net.minecraft.world.entity.Entity entity, double tolerance) {
        return entity.onGround() || entity.getY() - floorUnder(level, entity) <= tolerance;
    }

    public static Vec3 onGround(Level level, Vec3 point, double reference) {
        return new Vec3(point.x, groundY(level, point.x, point.z, reference), point.z);
    }

    public static void ring(ServerLevel level, Vec3 center, double radius, DustParticleOptions dust, double spacing) {
        int count = Math.max(8, (int) (Math.PI * 2 * radius / spacing));
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2 * i / count;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            level.sendParticles(dust, x, center.y + 0.15, z, 1, 0, 0, 0, 0);
        }
    }

    public static void disc(ServerLevel level, Vec3 center, double radius, DustParticleOptions dust) {
        ring(level, center, radius, dust, 0.7);
        int inner = (int) (radius * radius * 1.2);
        for (int i = 0; i < inner; i++) {
            double r = radius * Math.sqrt(level.random.nextDouble());
            double angle = level.random.nextDouble() * Math.PI * 2;
            level.sendParticles(dust, center.x + Math.cos(angle) * r, center.y + 0.15, center.z + Math.sin(angle) * r, 1, 0, 0, 0, 0);
        }
    }

    public static void line(ServerLevel level, Vec3 from, Vec3 to, DustParticleOptions dust, double spacing) {
        Vec3 delta = to.subtract(from);
        int count = Math.max(2, (int) (delta.length() / spacing));
        for (int i = 0; i <= count; i++) {
            Vec3 p = from.add(delta.scale(i / (double) count));
            level.sendParticles(dust, p.x, p.y + 0.15, p.z, 1, 0.05, 0, 0.05, 0);
        }
    }
}
