package net.tiew.operationWild.entity.bosses;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashMap;
import java.util.Map;

public final class OWBossStrikes {

    private static final double HORIZONTAL_MARGIN = 0.5;
    private static final double VERTICAL_MARGIN = 1.5;
    private static final double GROUP_REACH = 8.0;

    private OWBossStrikes() {
    }

    public static boolean strike(LivingEntity attacker, AABB box, float damage) {
        return strike(attacker, box, attacker.damageSources().mobAttack(attacker), damage);
    }

    public static boolean strike(Entity attacker, AABB box, DamageSource source, float damage) {
        if (attacker == null || attacker.level().isClientSide() || damage <= 0f) return false;
        AABB reach = box.inflate(HORIZONTAL_MARGIN, VERTICAL_MARGIN, HORIZONTAL_MARGIN);
        Vec3 from = attacker.position().add(0, attacker.getBbHeight() * 0.5, 0);
        Map<Entity, Entity> chosen = new LinkedHashMap<>();
        for (Entity entity : attacker.level().getEntities(attacker, reach,
                e -> e.isAlive() && (e instanceof OWBossTarget || (e.isMultipartEntity() && e.getParts() != null)))) {
            Entity group = entity instanceof OWBossTarget target ? target.strikeGroup() : entity;
            if (group == null || group == attacker || attacker.getRootVehicle() == group) continue;
            Entity current = chosen.get(group);
            if (entity instanceof OWBossTarget && (current == null || better(from, entity, current))) chosen.put(group, entity);
            if (group.isMultipartEntity() && group.getParts() != null) {
                AABB wide = reach.inflate(GROUP_REACH);
                for (net.neoforged.neoforge.entity.PartEntity<?> part : group.getParts()) {
                    if (!(part instanceof OWBossTarget) || !part.isAlive() || !wide.intersects(part.getBoundingBox())) continue;
                    Entity best = chosen.get(group);
                    if (best == null || better(from, part, best)) chosen.put(group, part);
                }
            }
        }
        boolean hit = false;
        for (Entity target : chosen.values()) {
            if (target.hurt(source, damage)) hit = true;
        }
        return hit;
    }

    private static boolean better(Vec3 from, Entity candidate, Entity current) {
        int a = ((OWBossTarget) candidate).strikePriority();
        int b = ((OWBossTarget) current).strikePriority();
        if (a != b) return a > b;
        return candidate.getBoundingBox().getCenter().distanceToSqr(from) < current.getBoundingBox().getCenter().distanceToSqr(from);
    }
}
