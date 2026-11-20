package net.tiew.operationWild.entity.bosses;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class OWBossEntity extends Monster {

    private final Map<UUID, Long> engagement = new HashMap<>();
    private final Map<UUID, Float> threat = new HashMap<>();

    protected OWBossEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 250;
        this.setPersistenceRequired();
    }

    public abstract double arenaRadius();

    public List<Player> fighters(double radius) {
        List<Player> list = new ArrayList<>();
        AABB box = this.getBoundingBox().inflate(radius, radius * 0.6, radius);
        for (Player player : this.level().getEntitiesOfClass(Player.class, box, this::isValidFighter)) {
            if (player.distanceToSqr(this) <= radius * radius) list.add(player);
        }
        return list;
    }

    public boolean isValidFighter(Player player) {
        return player.isAlive() && !player.isSpectator() && !player.isCreative();
    }

    public static Entity credit(Entity entity) {
        if (entity == null || entity instanceof Player) return entity;
        if (entity.getControllingPassenger() instanceof Player rider) return rider;
        if (entity instanceof net.minecraft.world.entity.OwnableEntity ownable && ownable.getOwner() instanceof Player owner) return owner;
        return entity;
    }

    public void engage(Entity source) {
        Entity entity = credit(source);
        if (entity instanceof Player player && isValidFighter(player)) {
            engagement.put(player.getUUID(), this.level().getGameTime());
        }
    }

    public void addThreat(Entity source, float amount) {
        if (!(credit(source) instanceof Player player)) return;
        threat.merge(player.getUUID(), amount, Float::sum);
        engage(player);
    }

    public float threatOf(Player player) {
        return threat.getOrDefault(player.getUUID(), 0f);
    }

    public void decayThreat(float factor) {
        threat.replaceAll((uuid, value) -> value * factor);
    }

    public long lastEngagement() {
        long last = -1;
        for (long value : engagement.values()) last = Math.max(last, value);
        return last;
    }

    public void clearCombat() {
        engagement.clear();
        threat.clear();
    }

    public int playerScaling(int fighters) {
        return Math.max(1, fighters);
    }

    protected boolean isEnvironmentalDamage(DamageSource source) {
        return source.is(DamageTypes.FALL) || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.DROWN)
                || source.is(DamageTypes.CRAMMING) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.SWEET_BERRY_BUSH)
                || source.is(DamageTypes.WITHER) || source.is(DamageTypes.MAGIC) || source.is(DamageTypes.FREEZE)
                || source.is(DamageTypes.HOT_FLOOR) || source.is(DamageTypes.LIGHTNING_BOLT) || source.is(DamageTypeTags.IS_FIRE);
    }

    protected boolean isKillCommand(DamageSource source) {
        return source.is(DamageTypes.GENERIC_KILL) || source.is(DamageTypes.FELL_OUT_OF_WORLD) || source.is(DamageTypes.OUTSIDE_BORDER);
    }

    public static Vec3 horizontal(Vec3 v) {
        return new Vec3(v.x, 0, v.z);
    }

    public static boolean isFighterTarget(LivingEntity entity) {
        return !(entity instanceof ServerPlayer player) || (!player.isCreative() && !player.isSpectator());
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public void push(double x, double y, double z) {
    }

    @Override
    public void knockback(double strength, double x, double z) {
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public boolean canChangeDimensions(Level from, Level to) {
        return false;
    }

    @Override
    protected boolean canRide(Entity vehicle) {
        return false;
    }

    @Override
    public boolean canBeAffected(net.minecraft.world.effect.MobEffectInstance effect) {
        return false;
    }
}
