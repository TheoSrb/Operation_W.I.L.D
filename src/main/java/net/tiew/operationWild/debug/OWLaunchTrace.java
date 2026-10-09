package net.tiew.operationWild.debug;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

public final class OWLaunchTrace {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int FOLLOW_TICKS = 8;

    private static int clientFollow = 0;

    private OWLaunchTrace() {}

    public static void log(String side, String message, Object... args) {
        LOGGER.info("[OW-LAUNCH][" + side + "] " + message, args);
    }

    public static void describe(String side, String label, Entity entity) {
        if (entity == null) {
            log(side, "{} : entity null", label);
            return;
        }
        log(side, "{} : pos=({}, {}, {}) vel=({}, {}, {}) passenger={} vehicle={} onGround={}",
                label,
                String.format("%.2f", entity.getX()), String.format("%.2f", entity.getY()), String.format("%.2f", entity.getZ()),
                String.format("%.3f", entity.getDeltaMovement().x), String.format("%.3f", entity.getDeltaMovement().y),
                String.format("%.3f", entity.getDeltaMovement().z),
                entity.isPassenger(), entity.getVehicle() == null ? "none" : entity.getVehicle().getType().toShortString(),
                entity.onGround());
    }

    public static void startClientFollow() {
        clientFollow = FOLLOW_TICKS;
    }

    public static void tickClient(Entity player) {
        if (clientFollow <= 0) return;
        describe("CLIENT", "tick+" + (FOLLOW_TICKS - clientFollow + 1), player);
        clientFollow--;
    }
}
