package net.tiew.operationWild.entity.cocoon;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.networking.packets.to_client.TarantulaCocoonPacket;
import net.tiew.operationWild.sound.OWSounds;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class TarantulaCocoons {

    private static final ResourceLocation SLOW_ID = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "tarantula_cocoon_slow");
    private static final ResourceLocation JUMP_ID = ResourceLocation.fromNamespaceAndPath(OperationWild.MOD_ID, "tarantula_cocoon_jump");
    private static final Map<LivingEntity, State> ACTIVE = new IdentityHashMap<>();

    private TarantulaCocoons() {
    }

    private static final class State {
        final LivingEntity target;
        final int required;
        int layers;
        int lockTicks;
        int sinceHit;
        int decayTimer;
        LivingEntity previousTarget;

        State(LivingEntity target, int required) {
            this.target = target;
            this.required = required;
        }

        float progress() {
            return Mth.clamp(layers / (float) required, 0f, 1f);
        }
    }

    public static int requiredLayers(LivingEntity target) {
        double width = target.getBbWidth();
        double size = width * width * target.getBbHeight();
        return Mth.clamp(Mth.ceil(size / OWAttacksConstants.Tarantula.COCOON_SIZE_PER_LAYER),
                OWAttacksConstants.Tarantula.COCOON_MIN_LAYERS, OWAttacksConstants.Tarantula.COCOON_MAX_LAYERS);
    }

    public static boolean canWrap(LivingEntity target) {
        return target.isAlive() && !(target instanceof net.tiew.operationWild.entity.bosses.OWBossEntity);
    }

    public static void wrap(LivingEntity target) {
        if (!(target.level() instanceof ServerLevel level) || !canWrap(target)) return;
        State state = ACTIVE.computeIfAbsent(target, t -> new State(t, requiredLayers(t)));
        if (state.lockTicks > 0) {
            state.lockTicks = Math.max(state.lockTicks, OWAttacksConstants.Tarantula.COCOON_LOCK_TICKS / 2);
            silk(level, target, 6, 0.6);
            sync(state);
            return;
        }
        rememberTarget(state);
        state.layers = Math.min(state.required, state.layers + 1);
        state.sinceHit = 0;
        state.decayTimer = 0;
        float progress = state.progress();
        silk(level, target, 6 + state.layers * 2, 0.45 + 0.35 * progress);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), OWSounds.TARANTULA_WEB_STICK.get(), SoundSource.NEUTRAL,
                0.9f, 0.8f + 0.6f * progress);
        if (state.layers >= state.required) {
            state.lockTicks = OWAttacksConstants.Tarantula.COCOON_LOCK_TICKS;
            silk(level, target, 30, 0.9);
            level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WOOL_PLACE, SoundSource.NEUTRAL, 1.0f, 0.7f);
            level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.HONEY_BLOCK_PLACE, SoundSource.NEUTRAL, 0.8f, 0.8f);
        }
        applyModifiers(state);
        sync(state);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE.isEmpty()) return;
        for (State state : new ArrayList<>(ACTIVE.values())) {
            LivingEntity target = state.target;
            if (target.isRemoved() || !target.isAlive()) {
                clear(state, false);
                continue;
            }
            if (state.lockTicks > 0) {
                rememberTarget(state);
                holdStill(target);
                if (target.level() instanceof ServerLevel level && target.tickCount % 5 == 0) silk(level, target, 2, 0.5);
                if (--state.lockTicks <= 0) clear(state, true);
                continue;
            }
            if (++state.sinceHit < OWAttacksConstants.Tarantula.COCOON_DECAY_DELAY) continue;
            if (++state.decayTimer < OWAttacksConstants.Tarantula.COCOON_DECAY_INTERVAL) continue;
            state.decayTimer = 0;
            state.layers--;
            if (state.layers <= 0) {
                clear(state, false);
            } else {
                applyModifiers(state);
                sync(state);
            }
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getTarget() instanceof LivingEntity living) || !(event.getEntity() instanceof ServerPlayer player)) return;
        State state = ACTIVE.get(living);
        if (state != null) PacketDistributor.sendToPlayer(player, packet(state));
    }

    private static void rememberTarget(State state) {
        if (state.target instanceof Mob mob && mob.getTarget() != null && mob.getTarget() != mob) state.previousTarget = mob.getTarget();
    }

    private static void restoreTarget(State state) {
        LivingEntity previous = state.previousTarget;
        state.previousTarget = null;
        if (!(state.target instanceof Mob mob) || previous == null) return;
        if (!previous.isAlive() || previous.isRemoved() || previous.level() != mob.level()) return;
        if (previous instanceof net.minecraft.world.entity.player.Player player && (player.isCreative() || player.isSpectator())) return;
        if (mob.canAttack(previous)) mob.setTarget(previous);
    }

    private static void holdStill(LivingEntity target) {
        Vec3 velocity = target.getDeltaMovement();
        target.setDeltaMovement(0, Math.min(velocity.y, 0), 0);
        target.hurtMarked = true;
        if (target instanceof Mob mob) {
            mob.getNavigation().stop();
            mob.setTarget(null);
            mob.setAggressive(false);
        }
        if (target instanceof net.minecraft.world.entity.monster.Creeper creeper) creeper.setSwellDir(-1);
        if (target.isUsingItem()) target.stopUsingItem();
    }

    public static boolean isLocked(net.minecraft.world.entity.Entity entity) {
        if (!(entity instanceof LivingEntity living)) return false;
        State state = ACTIVE.get(living);
        return state != null && state.lockTicks > 0;
    }

    @SubscribeEvent
    public static void onIncomingDamage(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) {
        if (ACTIVE.isEmpty()) return;
        net.minecraft.world.damagesource.DamageSource source = event.getSource();
        if (isLocked(source.getEntity()) || isLocked(source.getDirectEntity())) event.setCanceled(true);
    }

    private static void applyModifiers(State state) {
        double slow = state.lockTicks > 0 ? -1.0 : -OWAttacksConstants.Tarantula.COCOON_MAX_SLOW * state.progress();
        setModifier(state.target.getAttribute(Attributes.MOVEMENT_SPEED), SLOW_ID, slow);
        setModifier(state.target.getAttribute(Attributes.FLYING_SPEED), SLOW_ID, slow);
        setModifier(state.target.getAttribute(Attributes.JUMP_STRENGTH), JUMP_ID, state.lockTicks > 0 ? -1.0 : 0.0);
    }

    private static void setModifier(AttributeInstance instance, ResourceLocation id, double amount) {
        if (instance == null) return;
        if (amount == 0.0) {
            instance.removeModifier(id);
            return;
        }
        instance.addOrUpdateTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    private static void clear(State state, boolean burst) {
        ACTIVE.remove(state.target);
        state.layers = 0;
        state.lockTicks = 0;
        setModifier(state.target.getAttribute(Attributes.MOVEMENT_SPEED), SLOW_ID, 0.0);
        setModifier(state.target.getAttribute(Attributes.FLYING_SPEED), SLOW_ID, 0.0);
        setModifier(state.target.getAttribute(Attributes.JUMP_STRENGTH), JUMP_ID, 0.0);
        if (burst) restoreTarget(state);
        if (burst && state.target.level() instanceof ServerLevel level) {
            silk(level, state.target, 40, 1.0);
            level.playSound(null, state.target.getX(), state.target.getY(), state.target.getZ(), OWSounds.TARANTULA_WEB_UNSTICK.get(),
                    SoundSource.NEUTRAL, 1.0f, 0.7f);
        }
        if (!state.target.isRemoved()) sync(state);
    }

    private static void silk(ServerLevel level, LivingEntity target, int count, double spread) {
        double width = target.getBbWidth() * spread;
        double height = target.getBbHeight() * 0.5;
        double x = target.getX();
        double y = target.getY() + height;
        double z = target.getZ();
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBWEB.defaultBlockState()), x, y, z, count, width, height * spread, width, 0.05);
        level.sendParticles(ParticleTypes.WHITE_ASH, x, y, z, count, width, height, width, 0.02);
    }

    private static TarantulaCocoonPacket packet(State state) {
        return new TarantulaCocoonPacket(state.target.getId(), state.layers, state.required, state.lockTicks);
    }

    private static void sync(State state) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(state.target, packet(state));
    }
}
