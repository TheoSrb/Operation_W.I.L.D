package net.tiew.operationWild.entity.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.tiew.operationWild.OperationWild;
import net.tiew.operationWild.entity.animals.terrestrial.GorillaEntity;
import net.tiew.operationWild.entity.attacks.OWAttackLogic;
import net.tiew.operationWild.entity.attacks.OWAttacksConstants;
import net.tiew.operationWild.entity.attacks.OWAttacksHandler;
import net.tiew.operationWild.entity.misc.ThrownRockEntity;
import net.tiew.operationWild.particle.OWParticles;

@EventBusSubscriber(modid = OperationWild.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class GorillaRockAimPreview {

    private static final int RING_POINTS = 28;
    private static final int CROSS_POINTS = 4;
    private static final double CROSS_RADIUS = 0.45;
    private static final float RING_SPIN_DEGREES_PER_TICK = 1.5f;
    private static final double SCAN_UP = 1.5;
    private static final double SCAN_DOWN = 3.0;
    private static final double SURFACE_LIFT = 0.06;

    private GorillaRockAimPreview() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.isPaused()) return;
        if (!(mc.player.getVehicle() instanceof GorillaEntity gorilla) || gorilla.getControllingPassenger() != mc.player) return;
        if (!OWAttackLogic.isCharging || OWAttackLogic.getCurrentAttackId() != OWAttacksHandler.ROCK_THROW_ID) return;

        Vec3 look = gorilla.rockAimDirection(mc.player);
        Vec3 impact = ThrownRockEntity.predictImpact(mc.level, gorilla.rockLaunchOrigin(look),
                GorillaEntity.rockLaunchVelocity(look, currentChargeFactor()));
        if (impact == null) return;

        drawMarker(mc.level, impact, gorilla.tickCount);
    }

    private static float currentChargeFactor() {
        long minMs = OWAttacksConstants.Gorilla.ROCK_THROW_CHARGE_MIN_MS;
        long maxMs = OWAttacksConstants.Gorilla.ROCK_THROW_CHARGE_MAX_MS;
        long elapsed = System.currentTimeMillis() - OWAttackLogic.chargeStartMs;
        return Mth.clamp((float) (elapsed - minMs) / (maxMs - minMs), 0f, 1f);
    }

    private static void drawMarker(Level level, Vec3 impact, int tick) {
        double radius = OWAttacksConstants.Gorilla.ROCK_THROW_IMPACT_RADIUS;
        double spin = Math.toRadians(tick * RING_SPIN_DEGREES_PER_TICK);

        for (int i = 0; i < RING_POINTS; i++) {
            if ((i + tick) % 2 != 0) continue;
            double angle = spin + i * Math.PI * 2.0 / RING_POINTS;
            double x = impact.x + Math.cos(angle) * radius;
            double z = impact.z + Math.sin(angle) * radius;
            level.addParticle(OWParticles.AIM_MARKER.get(), x, surfaceY(level, x, z, impact.y), z, 0.0, 0.0, 0.0);
        }

        for (int i = 0; i < CROSS_POINTS; i++) {
            double angle = -spin + i * Math.PI * 2.0 / CROSS_POINTS;
            double x = impact.x + Math.cos(angle) * CROSS_RADIUS;
            double z = impact.z + Math.sin(angle) * CROSS_RADIUS;
            level.addParticle(OWParticles.AIM_MARKER.get(), x, surfaceY(level, x, z, impact.y), z, 0.0, 0.0, 0.0);
        }
        level.addParticle(OWParticles.AIM_MARKER.get(), impact.x, surfaceY(level, impact.x, impact.z, impact.y), impact.z, 1.0, 0.0, 0.0);
    }

    private static double surfaceY(Level level, double x, double z, double aroundY) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int top = Mth.floor(aroundY + SCAN_UP);
        int bottom = Mth.floor(aroundY - SCAN_DOWN);
        for (int y = top; y >= bottom; y--) {
            cursor.set(x, y, z);
            BlockState state = level.getBlockState(cursor);
            VoxelShape shape = state.getCollisionShape(level, cursor);
            if (shape.isEmpty()) continue;
            if (!level.getBlockState(cursor.above()).getCollisionShape(level, cursor.above()).isEmpty()) continue;
            return y + shape.max(Direction.Axis.Y) + SURFACE_LIFT;
        }
        return aroundY + SURFACE_LIFT;
    }
}
